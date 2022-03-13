package com.raylib.java.raudio;

import com.raylib.java.core.rCore;
import com.raylib.java.utils.FileIO;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.raylib.java.Config.*;
import static com.raylib.java.raudio.rAudio.AudioBufferUsage.AUDIO_BUFFER_USAGE_STATIC;
import static com.raylib.java.raudio.rAudio.AudioBufferUsage.AUDIO_BUFFER_USAGE_STREAM;
import static com.raylib.java.raudio.rAudio.MusicContextType.*;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.*;

public class rAudio{

    // Music context type
    // NOTE: Depends on data structure provided by the library
    // in charge of reading the different file types
    public static class MusicContextType{
        public static final int
                MUSIC_AUDIO_NONE = 0,       // No audio context loaded
                MUSIC_AUDIO_WAV = 1,        // WAV audio context
                MUSIC_AUDIO_OGG = 2,        // OGG audio context
                MUSIC_AUDIO_FLAC = 3,       // FLAC audio context
                MUSIC_AUDIO_MP3 = 4,        // MP3 audio context
                MUSIC_MODULE_XM = 5,        // XM module audio context
                MUSIC_MODULE_MOD = 6;       // MOD module audio context
    }

    // NOTE: Different logic is used when feeding data to the playback device
    // depending on whether data is streamed (Music vs Sound)
    public static class AudioBufferUsage{
        public static final int
                AUDIO_BUFFER_USAGE_STATIC = 0,
                AUDIO_BUFFER_USAGE_STREAM = 1;
    }

    // Global AUDIO context
    static AudioData audioData;
    final int AUDIO_DEVICE_CHANNELS = 2;    // Device output channels: stereo
    final int AUDIO_DEVICE_SAMPLE_RATE = 0;    // Device output sample rate
    final int MAX_AUDIO_BUFFER_POOL_CHANNELS = 16;    // Audio pool channels
    final int DEFAULT_AUDIO_BUFFER_SIZE = 4096;    // Default audio buffer size

    public rAudio() {
        audioData = new AudioData();
    }

    // Initialize audio device and context
    public void InitAudioDevice() {
        String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
         audioData.system.device = alcOpenDevice(defaultDevice);

        int[] attrib = {0};
        audioData.system.context = alcCreateContext(audioData.system.device, attrib);

        alcMakeContextCurrent(audioData.system.context);

        ALCCapabilities audioCapabilities = ALC.createCapabilities(audioData.system.device);
        ALCapabilities alCapabilities = AL.createCapabilities(audioCapabilities);

        if (!alCapabilities.OpenAL10) {
            Tracelog(LOG_WARNING, "AUDIO: Failed to initialize playback device");
        }
        else {
            Tracelog(LOG_INFO, "AUDIO: Device initialized successfully");
            Tracelog(LOG_INFO, "    > Backend:       OpenAL / " + alcGetInteger(audioData.system.device, ALC_MAJOR_VERSION) + "." + alcGetInteger(audioData.system.device, ALC_MINOR_VERSION));
            //Tracelog(LOG_INFO, "    > Format:        %s -> %s", alcGetString(audioData.system.device, ALC_, ma_get_format_name(audioData.system.device.playback.internalFormat));
            //Tracelog(LOG_INFO, "    > Channels:      " + alGetInteger(AL_CHANNELS));
            Tracelog(LOG_INFO, "    > Channels:      2");
            Tracelog(LOG_INFO, "    > Sample rate:   " + alcGetInteger(audioData.system.device, ALC_FREQUENCY));
            //Tracelog(LOG_INFO, "    > Periods size:  %d", audioData.system.device.playback.internalPeriodSizeInFrames*audioData.system.device.playback.internalPeriods);

            for(int i = 0; i < audioData.multiChannel.pool.length; i++) {
                audioData.multiChannel.pool[i] = new AudioBuffer();
            }

            audioData.system.isReady = true;
        }
    }

    // Close the audio device and context
    public void CloseAudioDevice() {
        if(audioData.system.isReady) {
            alcDestroyContext(audioData.system.context);
            alcCloseDevice(audioData.system.device);

            Tracelog(LOG_INFO, "AUDIO: Device closed successfully");
        }
        else {
            Tracelog(LOG_WARNING, "AUDIO: Device could not be closed, not currently initialized");
        }
    }

    // Check if device has been initialized successfully
    public boolean IsAudioDeviceReady() {
        return audioData.system.isReady;
    }

    //Set Master Volume (listener)
    public void SetMasterVolume(float volume) {
        alListenerf(AL_GAIN, volume);
    }

    private AudioBuffer LoadAudioBuffer(int channels, int sampleRate, int sizeInFrames, int usage) {
        AudioBuffer audioBuffer = new AudioBuffer();

        // Init audio buffer values
        audioBuffer.volume = 1.0f;
        audioBuffer.pitch = 1.0f;
        audioBuffer.playing = false;
        audioBuffer.paused = false;
        audioBuffer.looping = false;
        audioBuffer.usage = usage;
        audioBuffer.frameCursorPos = 0;
        audioBuffer.sizeInFrames = sizeInFrames;

        // Buffers should be marked as processed by default so that a call to
        // UpdateAudioStream() immediately after initialization works correctly
        audioBuffer.isSubBufferProcessed = new boolean[2];
        audioBuffer.isSubBufferProcessed[0] = true;
        audioBuffer.isSubBufferProcessed[1] = true;

        // Track audio buffer to linked list next position
        TrackAudioBuffer(audioBuffer);

        return audioBuffer;
    }

    private void UnloadAudioBuffer(AudioBuffer buffer) {
        buffer.data = null;
        alDeleteBuffers(buffer.bufferId);
        alDeleteSources(buffer.sourceId);
    }

    private boolean IsAudioBufferPlaying(AudioBuffer buffer) {
        return buffer.playing;
    }

    private void PlayAudioBuffer(AudioBuffer buffer) {
        if(!buffer.playing) {
            alSourcePlay(buffer.bufferId);
            buffer.playing = true;
        }
    }

    private void StopAudioBuffer(AudioBuffer buffer) {
        buffer.playing = false;
        alSourceStop(buffer.bufferId);
        alSourcei(buffer.bufferId, AL_POSITION, 0);
    }

    private void PauseAudioBuffer(AudioBuffer buffer) {
        buffer.playing = false;
        alSourcePause(buffer.bufferId);
    }

    private void ResumeAudioBuffer(AudioBuffer buffer) {
        if(!buffer.playing) {
            alSourcePlay(buffer.bufferId);
            buffer.playing = true;
        }
    }

    private void SetAudioBufferVolume(AudioBuffer buffer, float volume) {
        alSourcef(buffer.bufferId, AL_GAIN, volume);
    }

    private void SetAudioBufferPitch(AudioBuffer buffer, float pitch) {
        alSourcef(buffer.bufferId, AL_PITCH, pitch);
    }

    private void TrackAudioBuffer(AudioBuffer buffer) {
        audioData.buffer.buffers.add(buffer);
    }

    private void UntrackAudioBuffer(AudioBuffer buffer) {
        audioData.buffer.buffers.remove(buffer);
    }

    /*
    Wave/Sound loading/unloading functions
     */

    // Load wave data from file
    public Wave LoadWave(String fileName) {
        Wave wave = new Wave();

        // Loading file to memory
        int fileSize;
        byte[] fileData = new byte[0];
        try{
            fileData = FileIO.LoadFileData(fileName);
        } catch (IOException e){
            e.printStackTrace();
        }
        fileSize = fileData.length;

        // Loading wave from memory data
        if (fileData != null) {
            wave = LoadWaveFromMemory(rCore.GetFileExtension(fileName), fileData, fileSize);

        }

        return wave;
    }

    // Load wave from memory buffer, fileType refers to extension: i.e. ".wav"
    public Wave LoadWaveFromMemory(String fileType, byte[] fileData, int dataSize) {
        Wave wave = new Wave();

        if (SUPPORT_FILEFORMAT_WAV && fileType.equalsIgnoreCase(".wav")) {
            wave.frameCount = fileData.length * 2;
            wave.sampleRate = alcGetInteger(audioData.system.device, ALC_FREQUENCY);
            wave.sampleSize = 16;
            wave.channels = 2;
            ByteBuffer data = ByteBuffer.allocateDirect(fileData.length);
            data.put(fileData).flip();
            wave.data = data;
        }
        else if (SUPPORT_FILEFORMAT_OGG && fileType.equalsIgnoreCase(".ogg")) {
            try(MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer errorBuffer = stack.mallocInt(1);
                IntBuffer ChannelsBuffer = stack.mallocInt(1);
                IntBuffer SamplesBuffer = stack.mallocInt(1);
                ByteBuffer dataBuffer = ByteBuffer.allocateDirect(fileData.length);
                dataBuffer.put(fileData).flip();

                long oggData = stb_vorbis_open_memory(dataBuffer, errorBuffer, null);

                if(oggData != 0) {
                    ByteBuffer infoBuffer = ByteBuffer.allocateDirect(STBVorbisInfo.SIZEOF);
                    STBVorbisInfo vorbisInfo = new STBVorbisInfo(infoBuffer);
                    stb_vorbis_get_info(oggData, vorbisInfo);

                    ShortBuffer oggAudio = stb_vorbis_decode_memory(dataBuffer, ChannelsBuffer, SamplesBuffer);
                    wave.sampleRate = SamplesBuffer.get(0);
                    wave.sampleSize = 16;
                    wave.channels = ChannelsBuffer.get(0);
                    wave.frameCount = stb_vorbis_stream_length_in_samples(oggData);
                    wave.data = oggAudio;

                    stb_vorbis_close(oggData);
                }
            }
        }
        //elif flac
        else if(SUPPORT_FILEFORMAT_MP3 && fileType.equalsIgnoreCase(".mp3")) {
            try {
                fr.delthas.javamp3.Sound mp3Sound = new fr.delthas.javamp3.Sound(new ByteInputStream(fileData, fileData.length));

                byte[] mp3Data = new byte[fileData.length];
                int read = mp3Sound.read(mp3Data);
                ByteBuffer rawAudio = ByteBuffer.allocateDirect(read);
                rawAudio.put(mp3Data).flip();

                wave.channels = 2;
                wave.sampleSize = 16;
                wave.sampleRate = alcGetInteger(audioData.system.device, ALC_FREQUENCY);
                wave.frameCount = read * 2;
                wave.data = rawAudio;
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        return wave;
    }

    // Load sound from file
    public Sound LoadSound(String fileName) {
        Wave wave = LoadWave(fileName);

        Sound sound = LoadSoundFromWave(wave);

        UnloadWave(wave);

        return sound;
    }

    // Load sound from wave data
    public Sound LoadSoundFromWave(Wave wave) {
        Sound sound = new Sound();

        if (wave.data != null) {
            int frameCount = wave.frameCount;

            AudioBuffer audioBuffer = LoadAudioBuffer(AUDIO_DEVICE_CHANNELS, alcGetInteger(audioData.system.device, ALC_FREQUENCY), frameCount, AUDIO_BUFFER_USAGE_STATIC);
            if (audioBuffer == null) {
                Tracelog(LOG_WARNING, "SOUND: Failed to create buffer");
                return sound; // early return to avoid dereferencing the audioBuffer null pointer
            }

            //todo: recalc framecount

            sound.frameCount = frameCount;
            sound.stream.sampleRate = alcGetInteger(audioData.system.device, ALC_FREQUENCY);
            sound.stream.sampleSize = 32;
            sound.stream.channels = AUDIO_DEVICE_CHANNELS;
            sound.stream.buffer = audioBuffer;
            sound.stream.buffer.data = wave.data;

            sound.stream.buffer.bufferData("wav", AL_FORMAT_STEREO16, sound.stream.sampleRate);
        }

        return sound;
    }

    // Update sound buffer with new data
    public void UpdateSound(Sound sound, byte[] data, int sampleCount)  {
        if (sound.stream.buffer != null)  {
            StopAudioBuffer(sound.stream.buffer);
            if(sound.stream.buffer.data.position() - sound.stream.buffer.data.capacity() > data.length) {
                ((ByteBuffer) sound.stream.buffer.data).put(data).flip();
            }
        }
    }
    // Unload wave data
    public void UnloadWave(Wave wave) {
        wave.data = null;
        Tracelog(LOG_INFO, "WAVE: Unloaded wave data from RAM");
    }

    // Unload sound
    public void UnloadSound(Sound sound) {
        UnloadAudioBuffer(sound.stream.buffer);

        Tracelog(LOG_INFO, "WAVE: Unloaded sound data from RAM");
    }

    // Export wave data to file
    public boolean ExportWave(Wave wave, String fileName) {
        boolean success = false;

        if(SUPPORT_FILEFORMAT_WAV && rCore.IsFileExtension(fileName, ".wav")) {
            //TODO: format header for wav saving
            //success = SaveFileData(fileName, (unsigned char *)fileData, (unsigned int)fileDataSize);
        }
        else if (rCore.IsFileExtension(fileName, ".raw")) {
            // Export raw sample data (without header)
            // NOTE: It's up to the user to track wave parameters
            byte[] waveData = new byte[wave.data.capacity()];
            for(int i = 0; i < waveData.length; i++) {
                waveData[i] = ((ByteBuffer)wave.data).get(i);
            }

            try {
                success = FileIO.SaveFileData(fileName, waveData, wave.frameCount*wave.channels*wave.sampleSize/8);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        if (success) {
            Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Wave data exported successfully");
        }
        else {
            Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export wave data");
        }

        return success;
    }

    // Export wave sample data to code (.h)
    public boolean ExportWaveAsCode(Wave wave, String fileName) {
        boolean success = false;

        int TEXT_BYTES_PER_LINE = 20;

        int waveDataSize = wave.frameCount*wave.channels*wave.sampleSize/8;

        // NOTE: Text data buffer size is estimated considering wave data size in bytes
        // and requiring 6 char bytes for every byte: "0x00, "
        StringBuilder txtData = new StringBuilder();
        //char *txtData = (char *)RL_CALLOC(waveDataSize*6 + 2000, sizeof(char));

        int byteCount = 0;
        txtData.append("\n//////////////////////////////////////////////////////////////////////////////////\n");
        txtData.append("//                                                                              //\n");
        txtData.append("// WaveAsCode exporter v1.0 - Wave data exported as an array of bytes           //\n");
        txtData.append("//                                                                              //\n");
        txtData.append("// more info and bugs-report:  github.com/raysan5/raylib                        //\n");
        txtData.append("// feedback and support:       ray[at]raylib.com                                //\n");
        txtData.append("//                                                                              //\n");
        txtData.append("// Copyright (c) 2018-2021 Ramon Santamaria (@raysan5)                          //\n");
        txtData.append("//                                                                              //\n");
        txtData.append("//////////////////////////////////////////////////////////////////////////////////\n\n");

        // Get file name from path and convert variable name to uppercase
        char[] varFileName = rCore.GetFileNameWithoutExt(fileName).toCharArray();
        for (int i = 0; varFileName[i] != '\0'; i++) {
            if (varFileName[i] >= 'a' && varFileName[i] <= 'z') {
                varFileName[i] = Character.highSurrogate(varFileName[i] - 32);
            }
        }


        txtData.append("// Wave data information\n");
        txtData.append("#define " + Arrays.toString(varFileName) + "_FRAME_COUNT      " + wave.frameCount + "\n");
        txtData.append("#define " + Arrays.toString(varFileName) + "_FRAME_COUNT      " + wave.frameCount + "\n");
        txtData.append("#define " + Arrays.toString(varFileName) + "_SAMPLE_RATE      " + wave.sampleRate + "\n");
        txtData.append("#define " + Arrays.toString(varFileName) + "_SAMPLE_SIZE      " + wave.sampleSize + "\n");
        txtData.append("#define " + Arrays.toString(varFileName) + "_CHANNELS         " + wave.channels + "\n\n");

        byte[] waveData = new byte[wave.data.capacity()];
        for(int i = 0; i < waveData.length; i++) {
            waveData[i] = ((ByteBuffer)wave.data).get(i);
        }

        // Write byte data as hexadecimal text
        // NOTE: Frame data exported is interlaced: Frame01[Sample-Channel01, Sample-Channel02, ...], Frame02[], Frame03[]
        txtData.append("static unsigned char " + Arrays.toString(varFileName) + "_DATA[" + waveDataSize + "] = { ");
        for (int i = 0; i < waveDataSize - 1; i++) {
            txtData.append((i%TEXT_BYTES_PER_LINE == 0)? "0x" + waveData[i] + ",\n" : "0x" + waveData[i] + ", ");
        }
        txtData.append("0x" + waveData[waveDataSize-1] + " };\n");

        // NOTE: Text data length exported is determined by '\0' (NULL) character
        try {
            success = FileIO.SaveFileText(fileName, String.valueOf(txtData));
        } catch(IOException e) {
            e.printStackTrace();
        }

        return success;
    }


    /*
    Sound Management functions
     */

    // Play a sound
    public void PlaySound(Sound sound) {
        PlayAudioBuffer(sound.stream.buffer);
    }

    // Pause a sound
    public void PauseSound(Sound sound) {
        PauseAudioBuffer(sound.stream.buffer);
    }

    // Resume a paused sound
    public void ResumeSound(Sound sound) {
        ResumeAudioBuffer(sound.stream.buffer);
    }

    // Stop reproducing a sound
    public void StopSound(Sound sound) {
        StopAudioBuffer(sound.stream.buffer);
    }

    // Check if a sound is playing
    public boolean IsSoundPlaying(Sound sound) {
        return IsAudioBufferPlaying(sound.stream.buffer);
    }

    // Set volume for a sound (1.0f is the max)
    public void SetSoundVolume(Sound sound, float volume) {
        SetAudioBufferVolume(sound.stream.buffer, volume);
    }

    // Set pitch for a sound (1.0f is the base)
    public void SetSoundPitch(Sound sound, float pitch) {
        SetAudioBufferPitch(sound.stream.buffer, pitch);
    }

    // Play a sound in the multichannel buffer pool
    public void PlaySoundMulti(Sound sound) {
        int index = -1;
        int oldAge = 0;
        int oldIndex = -1;

        // find the first non playing pool entry
        for (int i = 0; i < MAX_AUDIO_BUFFER_POOL_CHANNELS; i++) {
            if (audioData.multiChannel.channels[i] > oldAge) {
                oldAge = audioData.multiChannel.channels[i];
                oldIndex = i;
            }

            if (!IsAudioBufferPlaying(audioData.multiChannel.pool[i])) {
                index = i;
                break;
            }
        }

        // If no none playing pool members can be index choose the oldest
        if (index == -1) {
            Tracelog(LOG_WARNING, "SOUND: Buffer pool is already full, count: " + audioData.multiChannel.poolCounter);

            if (oldIndex == -1) {
                // Shouldn't be able to get here... but just in case something odd happens!
                Tracelog(LOG_WARNING, "SOUND: Buffer pool could not determine oldest buffer not playing sound");
                return;
            }

            index = oldIndex;

            // Just in case...
            StopAudioBuffer(audioData.multiChannel.pool[index]);
        }

        // Experimentally mutex lock doesn't seem to be needed this makes sense
        // as pool[index] isn't playing and the only stuff we're copying
        // shouldn't be changing...

        audioData.multiChannel.channels[index] = audioData.multiChannel.poolCounter;
        audioData.multiChannel.poolCounter++;

        audioData.multiChannel.pool[index].volume = sound.stream.buffer.volume;
        audioData.multiChannel.pool[index].pitch = sound.stream.buffer.pitch;
        audioData.multiChannel.pool[index].looping = sound.stream.buffer.looping;
        audioData.multiChannel.pool[index].usage = sound.stream.buffer.usage;
        audioData.multiChannel.pool[index].isSubBufferProcessed[0] = false;
        audioData.multiChannel.pool[index].isSubBufferProcessed[1] = false;
        audioData.multiChannel.pool[index].sizeInFrames = sound.stream.buffer.sizeInFrames;
        audioData.multiChannel.pool[index].data = sound.stream.buffer.data;

        audioData.multiChannel.pool[index].bufferData("multi", AL_FORMAT_STEREO16, sound.stream.sampleRate);

        PlayAudioBuffer(audioData.multiChannel.pool[index]);
    }

    // Stop any sound played with PlaySoundMulti()
    public void StopSoundMulti() {
        for (int i = 0; i < MAX_AUDIO_BUFFER_POOL_CHANNELS; i++) {
            StopAudioBuffer(audioData.multiChannel.pool[i]);
        }
    }

    // Get number of sounds playing in the multichannel buffer pool
    public int GetSoundsPlaying() {
        int counter = 0;

        for (int i = 0; i < MAX_AUDIO_BUFFER_POOL_CHANNELS; i++) {
            if (IsAudioBufferPlaying(audioData.multiChannel.pool[i])) {
                counter++;
            }
        }

        return counter;
    }

    /*
    TODO:
    void WaveFormat(Wave *wave, int sampleRate, int sampleSize, int channels);  // Convert wave data to desired format
    */

    // Copy a wave to a new wave
    public Wave WaveCopy(Wave wave) {
        Wave newWave = new Wave();

        if (wave.data != null) {
            // NOTE: Size must be provided in bytes
            newWave.data = wave.data;
            newWave.frameCount = wave.frameCount;
            newWave.sampleRate = wave.sampleRate;
            newWave.sampleSize = wave.sampleSize;
            newWave.channels = wave.channels;
        }

        return newWave;
    }

    // Crop a wave to defined samples range
    // NOTE: Security check in case of out-of-range
    public void WaveCrop(Wave wave, int initSample, int finalSample) {
        if ((initSample >= 0) && (initSample < finalSample) && (finalSample > 0) && (finalSample < (wave.frameCount*wave.channels))) {
            int sampleCount = finalSample - initSample;

            byte[] data = new byte[sampleCount*wave.sampleSize/8];

            for(int i = initSample, j = 0; i < finalSample; i++, j++) {
                data[j] = ((ByteBuffer)wave.data).get(i);
            }

            ByteBuffer bb = ByteBuffer.allocateDirect(data.length);
            bb.put(data).flip();

            wave.data = bb;
        }
        else {
            Tracelog(LOG_WARNING, "WAVE: Crop range out of bounds");
        }
    }

    // Load samples data from wave as a floats array
    // NOTE 1: Returned sample values are normalized to range [-1..1]
    // NOTE 2: Sample data allocated should be freed with UnloadWaveSamples()
    public float[] LoadWaveSamples(Wave wave) {
        float[] samples = new float[wave.frameCount*wave.channels];
        byte[] waveData = new byte[wave.data.capacity()];
        for(int i = 0; i < waveData.length; i++) {
            waveData[i] = ((ByteBuffer)wave.data).get(i);
        }

        // NOTE: sampleCount is the total number of interlaced samples (including channels)

        for (int i = 0; i < wave.frameCount*wave.channels; i++) {
            if (wave.sampleSize == 8) {
                samples[i] = (float)(waveData[i] - 127)/256.0f;
            }
            else if (wave.sampleSize == 16) {
                samples[i] = (float)(waveData[i])/32767.0f;
            }
            else if (wave.sampleSize == 32) {
                samples[i] = waveData[i];
            }
        }

        return samples;
    }

    // Unload samples data loaded with LoadWaveSamples()
    public void UnloadWaveSamples(float[] samples) {
        samples = null;
    }

    /*
    Music management functions
    */

    // Load music stream from file
    public Music LoadMusicStream(String fileName) {
        Music music = new Music();
        boolean musicLoaded = false;

        if (SUPPORT_FILEFORMAT_WAV && rCore.IsFileExtension(fileName, ".wav")){
            music.ctxType = MUSIC_AUDIO_WAV;

            try {
                AudioInputStream audioInputStream;
                File audioFile = new File(fileName);
                byte[] audioBytes;
                if (audioFile != null){
                    audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                    audioBytes = new byte[(int) audioFile.length()];
                }
                else {
                    audioInputStream = AudioSystem.getAudioInputStream(rAudio.class.getResourceAsStream(fileName));
                    audioBytes = new byte[audioInputStream.getFormat().getSampleSizeInBits()];
                }

                audioInputStream.read(audioBytes);

                ByteBuffer rawAudio = ByteBuffer.allocateDirect(audioBytes.length);
                rawAudio.put(audioBytes).flip();

                music.stream = LoadAudioStream((int) audioInputStream.getFormat().getSampleRate(), 16, audioInputStream.getFormat().getChannels());
                music.stream.buffer.data = rawAudio;

                int format = (audioInputStream.getFormat().getChannels() < 1) ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
                music.stream.buffer.bufferData("wav", format, (int) audioInputStream.getFormat().getSampleRate());

                music.frameCount = (int) audioInputStream.getFrameLength();
                music.looping = true;
                musicLoaded = true;
            }
            catch (IOException | UnsupportedAudioFileException e){
                e.printStackTrace();
            }
        }
        else if(SUPPORT_FILEFORMAT_OGG && rCore.IsFileExtension(fileName, ".ogg")){
            IntBuffer errorBuffer = IntBuffer.allocate(1);
            music.ctxType = MUSIC_AUDIO_OGG;
            music.ctxData = stb_vorbis_open_filename(fileName, errorBuffer, null);

            if (music.ctxData != 0){
                try (MemoryStack stack = MemoryStack.stackPush()){
                    IntBuffer channels = stack.mallocInt(1);
                    IntBuffer sampleRate = stack.mallocInt(1);
                    ByteBuffer infoBuffer = stack.malloc(STBVorbisInfo.SIZEOF);
                    STBVorbisInfo info = new STBVorbisInfo(infoBuffer);
                    stb_vorbis_get_info(music.ctxData, info);

                    music.stream = LoadAudioStream(info.sample_rate(), 16, info.channels());
                    music.stream.buffer.data = stb_vorbis_decode_filename(fileName, channels, sampleRate);

                    int format = (info.channels() < 1) ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
                    music.stream.buffer.bufferData("ogg", format, info.sample_rate());

                    music.frameCount = stb_vorbis_stream_length_in_samples(music.ctxData);
                    music.looping = true;
                    musicLoaded = true;
                }
            }
        }
        else if (SUPPORT_FILEFORMAT_MP3 && rCore.IsFileExtension(fileName, ".flac")) {
            //TODO: FLAC SUPPORT
        }
        else if (SUPPORT_FILEFORMAT_MP3 && rCore.IsFileExtension(fileName, ".mp3")) {
            music.ctxType = MUSIC_AUDIO_MP3;

            try {
                Path path = Paths.get(fileName);
                fr.delthas.javamp3.Sound mp3Sound = new fr.delthas.javamp3.Sound(new BufferedInputStream(Files.newInputStream(path)));

                byte[] audioData = new byte[(int) path.toFile().length()];
                int read = mp3Sound.read(audioData);
                ByteBuffer rawAudio = ByteBuffer.allocateDirect(read);
                rawAudio.put(audioData).flip();

                music.stream = LoadAudioStream(mp3Sound.getSamplingFrequency(), 16, 2);
                music.stream.buffer.data = rawAudio;

                music.stream.buffer.bufferData("mp3", AL_FORMAT_STEREO16, mp3Sound.getSamplingFrequency());

                music.frameCount = read * 2;
                music.looping = true;
                musicLoaded = true;
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else if (SUPPORT_FILEFORMAT_MP3 && rCore.IsFileExtension(fileName, ".xm")) {
            //TODO: XM SUPPORT
        }
        else if (SUPPORT_FILEFORMAT_MP3 && rCore.IsFileExtension(fileName, ".mod")) {
            //TODO: MOD SUPPORT
        }
        else {
            Tracelog(LOG_WARNING, "STREAM: [" + fileName + "] File format not supported");
        }

        if(!musicLoaded) {
            Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Music file could not be opened");
        }
        else {
            // Show some music stream info
            Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Music file loaded successfully");
            Tracelog(LOG_INFO, "    > Sample rate:   " + music.stream.sampleRate + " Hz");
            Tracelog(LOG_INFO, "    > Sample size:   " + music.stream.sampleSize + " bits");
            Tracelog(LOG_INFO, "    > Channels:      " + music.stream.channels + ((music.stream.channels == 1)? " Mono" : (music.stream.channels == 2)? " Stereo" : " Multi"));
            Tracelog(LOG_INFO, "    > Total frames:  " + music.frameCount);
        }
        return music;
    }

    //TODO: THIS
    //Load music stream from data
    public Music LoadMusicStreamFromMemory(String filetype, Buffer data) {
        Music music = new Music();
        boolean musicLoaded = false;

        if (SUPPORT_FILEFORMAT_WAV && filetype.equalsIgnoreCase(".wav")){
            //TODO
        }
        else if (SUPPORT_FILEFORMAT_OGG && filetype.equalsIgnoreCase(".ogg")){
            //TODO
        }
        else if(SUPPORT_FILEFORMAT_MP3 && filetype.equalsIgnoreCase(".mp3")) {
            //TODO
        }
        else if(SUPPORT_FILEFORMAT_MP3 && filetype.equalsIgnoreCase(".xm")) {
            //TODO
        }
        else if(SUPPORT_FILEFORMAT_MP3 && filetype.equalsIgnoreCase(".mod")) {
            //TODO
        }
        else {
            Tracelog(LOG_WARNING, "STREAM: Data format not supported");
        }

        if(!musicLoaded) {
            Tracelog(LOG_WARNING, "FILEIO: Music data could not be loaded");
        }
        else {
            // Show some music stream info
            Tracelog(LOG_INFO, "FILEIO: Music data loaded successfully");
            Tracelog(LOG_INFO, "    > Sample rate:   " + music.stream.sampleRate + " Hz");
            Tracelog(LOG_INFO, "    > Sample size:   " + music.stream.sampleSize + " bits");
            Tracelog(LOG_INFO, "    > Channels:       " + music.stream.channels + ((music.stream.channels == 1)? "Mono" : (music.stream.channels == 2)? "Stereo" : "Multi"));
            Tracelog(LOG_INFO, "    > Total frames:  " + music.frameCount);
        }
        return music;

    }

    // Update buffers for music streaming
    public void UpdateMusicStream(Music music) {
        if(music.stream.buffer == null) {
            return;
        }

        boolean streamEnding = false;
        int subBufferSizeInFrames = music.stream.buffer.sizeInFrames/2;
        int frameCountToStream;
        int framesLeft = music.frameCount - music.stream.buffer.framesProcessed;

        Buffer pcm = ByteBuffer.allocateDirect(subBufferSizeInFrames*music.stream.channels*music.stream.sampleSize/8);

        while(IsAudioStreamProcessed(music.stream)) {
            frameCountToStream = Math.min(framesLeft, subBufferSizeInFrames);

            pcm = music.stream.buffer.data;

            UpdateAudioStream(music.stream, pcm, frameCountToStream);

            framesLeft -= frameCountToStream;

            if (framesLeft <= 0) {
                streamEnding = true;
                break;
            }

        }

        // Reset audio stream for looping
        if(streamEnding) {
            StopMusicStream(music);      // Stop music (and reset)
            if(music.looping) {
                PlayMusicStream(music);  // Play again
            }
        }
        else  {
            // NOTE: In case window is minimized, music stream is stopped,
            // just make sure to play again on window restore
            if(IsMusicStreamPlaying(music)) {
                PlayMusicStream(music);
            }
        }
    }

    //Unload music Stream
    public void UnloadMusicStream(Music music) {
        UnloadAudioStream(music.stream);
    }

    // Start music playing (open stream)
    public void PlayMusicStream(Music music)  {
        if (music.stream.buffer != null)  {
            // For music streams, we need to make sure we maintain the frame cursor position
            // This is a hack for this section of code in UpdateMusicStream()
            // NOTE: In case window is minimized, music stream is stopped, just make sure to
            // play again on window restore: if (IsMusicStreamPlaying(music)) PlayMusicStream(music);
            int frameCursorPos = music.stream.buffer.frameCursorPos;
            PlayAudioStream(music.stream);  // WARNING: This resets the cursor position.
            music.stream.buffer.frameCursorPos = frameCursorPos;
        }
    }

    //Start music playing
    public void StartMusicStream(Music music) {
        PlayAudioStream(music.stream);
    }

    // Pause music playing
    public void PauseMusicStream(Music music) {
        PauseAudioStream(music.stream);
    }

    // Resume music playing
    public void ResumeMusicStream(Music music) {
        ResumeAudioStream(music.stream);
    }

    //Stop music playing (reset track to beginning)
    public void StopMusicStream(Music music) {
        StopAudioStream(music.stream);
    }
    
    // Check if any music is playing
    public boolean IsMusicStreamPlaying(Music music) {
        return IsAudioStreamPlaying(music.stream);
    }

    // Set volume for music
    public void SetMusicVolume(Music music, float volume) {
        SetAudioStreamVolume(music.stream, volume);
    }

    // Set pitch for music
    public void SetMusicPitch(Music music, float pitch) {
        SetAudioBufferPitch(music.stream.buffer, pitch);
    }

    // Get music time length (in seconds)
    public float GetMusicTimeLength(Music music)  {
        return (float) music.frameCount/music.stream.sampleRate;
    }

    // Get current music time played (in seconds)
    public float GetMusicTimePlayed(Music music)  {
        float secondsPlayed = 0.0f;

        if (music.stream.buffer != null)  {
            if(SUPPORT_FILEFORMAT_XM && music.ctxType == MUSIC_MODULE_XM) {
                int framesPlayed = 0;
                //TODO: XM
                //jar_xm_get_position(music.ctxData, NULL, NULL, NULL, &framesPlayed);
                secondsPlayed = (float) framesPlayed / music.stream.sampleRate;
            }
            else  {
                int framesPlayed = music.stream.buffer.framesProcessed;
                secondsPlayed = (float)framesPlayed/music.stream.sampleRate;
            }
        }

        return secondsPlayed;
    }

    /*
    AudioStream management functions
    */
    public AudioStream LoadAudioStream(int sampleRate, int sampleSize, int channels) {
        AudioStream stream = new AudioStream();

        stream.sampleRate = sampleRate;
        stream.sampleSize = sampleSize;
        stream.channels = channels;

        // If the buffer is not set, compute one that would give us a buffer good enough for a decent frame rate
        int subBufferSize = (audioData.buffer.defaultSize == 0)? alcGetInteger(audioData.system.device, ALC_FREQUENCY)/30 : audioData.buffer.defaultSize;

        stream.buffer = LoadAudioBuffer( stream.channels, stream.sampleRate, subBufferSize*2, AUDIO_BUFFER_USAGE_STREAM);

        if (stream.buffer != null) {
            stream.buffer.looping = true;    // Always loop for streaming buffers
            Tracelog(LOG_INFO, "STREAM: Initialized successfully (" + stream.sampleRate + " Hz, " + stream.sampleSize + " bit, " + ((stream.channels == 1)? "Mono" : "Stereo") + ")");
        }
        else {
            Tracelog(LOG_WARNING, "STREAM: Failed to load audio buffer, stream could not be created");
        }

        return stream;
    }

    // Unload audio stream and free memory
    public void UnloadAudioStream(AudioStream stream) {
        UnloadAudioBuffer(stream.buffer);

        Tracelog(LOG_INFO, "STREAM: Unloaded audio stream data from RAM");
    }

    // Update audio stream buffers with data
    // NOTE 1: Only updates one buffer of the stream source: unqueue -> update -> queue
    // NOTE 2: To unqueue a buffer it needs to be processed: IsAudioStreamProcessed()
    public void UpdateAudioStream(AudioStream stream, Buffer data, int frameCount) {
        if (stream.buffer != null) {
            if(stream.buffer.isSubBufferProcessed[0] || stream.buffer.isSubBufferProcessed[1]){
                int subBufferToUpdate;

                if(stream.buffer.isSubBufferProcessed[0] && stream.buffer.isSubBufferProcessed[1]){
                    // Both buffers are available for updating.
                    // Update the first one and make sure the cursor is moved back to the front.
                    subBufferToUpdate = 0;
                    stream.buffer.frameCursorPos = 0;
                }
                else{
                    // Just update whichever sub-buffer is processed.
                    subBufferToUpdate = (stream.buffer.isSubBufferProcessed[0]) ? 0 : 1;
                }

                int subBufferSizeInFrames = stream.buffer.sizeInFrames / 2;
                int subBuffer = ((subBufferSizeInFrames * stream.channels * (stream.sampleSize / 8)) * subBufferToUpdate);

                // TODO: Get total frames processed on this buffer... DOES NOT WORK.
                stream.buffer.framesProcessed += subBufferSizeInFrames;

                // Does this API expect a whole buffer to be updated in one go?
                // Assuming so, but if not will need to change this logic.
                if(subBufferSizeInFrames >= frameCount) {
                    int framesToWrite = subBufferSizeInFrames;

                    if(framesToWrite > frameCount) {
                        framesToWrite = frameCount;
                    }

                    int bytesToWrite = framesToWrite * stream.channels * (stream.sampleSize / 8);
                    for(int i = 0; i < data.capacity(); i++) {
                        //((ByteBuffer) stream.buffer.data).put(subBuffer+i, ((ByteBuffer)data).get(i));
                        ((ByteBuffer) stream.buffer.data).put(subBuffer + i, ((ByteBuffer)data).get(i));
                    }

                    // Any leftover frames should be filled with zeros.
                    int leftoverFrameCount = subBufferSizeInFrames - framesToWrite;

                    if(leftoverFrameCount > 0) {
                        for(int i = 0; i < leftoverFrameCount * stream.channels * (stream.sampleSize / 8); i++) {
                            ((ByteBuffer) stream.buffer.data).put(subBuffer+bytesToWrite+i, (byte) 0);
                        }
                    }

                    stream.buffer.bufferData("raw", AL_FORMAT_STEREO16, stream.sampleRate);

                    stream.buffer.isSubBufferProcessed[subBufferToUpdate] = false;
                }
                else {
                    Tracelog(LOG_WARNING, "STREAM: Attempting to write too many frames to buffer");
                }
            }
        }
        else {
            Tracelog(LOG_WARNING, "STREAM: Buffer not available for updating");
        }

    }


    // Check if any audio stream buffers requires refill
    public boolean IsAudioStreamProcessed(AudioStream stream) {
        if (stream.buffer == null) {
            return false;
        }

        return (stream.buffer.isSubBufferProcessed[0] || stream.buffer.isSubBufferProcessed[1]);
    }

    // Play audio stream
    public void PlayAudioStream(AudioStream stream) {
        PlayAudioBuffer(stream.buffer);
    }

    // Play audio stream
    public void PauseAudioStream(AudioStream stream) {
        PauseAudioBuffer(stream.buffer);
    }

    // Resume audio stream playing
    public void ResumeAudioStream(AudioStream stream) {
        ResumeAudioBuffer(stream.buffer);
    }

    // Check if audio stream is playing.
    public boolean IsAudioStreamPlaying(AudioStream stream) {
        return IsAudioBufferPlaying(stream.buffer);
    }

    // Stop audio stream
    public void StopAudioStream(AudioStream stream) {
        StopAudioBuffer(stream.buffer);
    }

    // Set volume for audio stream (1.0 is max level)
    public void SetAudioStreamVolume(AudioStream stream, float volume) {
        SetAudioBufferVolume(stream.buffer, volume);
    }

    // Set pitch for audio stream (1.0 is base level)
    public void SetAudioStreamPitch(AudioStream stream, float pitch) {
        SetAudioBufferPitch(stream.buffer, pitch);
    }

    // Default size for new audio streams
    public void SetAudioStreamBufferSizeDefault(int size) {
        audioData.buffer.defaultSize = size;
    }

}
