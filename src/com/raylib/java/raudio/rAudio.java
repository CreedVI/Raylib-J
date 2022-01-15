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
    // depending on whether or not data is streamed (Music vs Sound)
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
        else {
            Tracelog(LOG_WARNING, "AUDIO: Audio buffer already playing");
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
        int fileSize = 0;
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
            //TODO: WHy crash
            try(MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer errorBuffer = stack.mallocInt(1);
                ByteBuffer dataBuffer = ByteBuffer.allocateDirect(fileData.length);
                dataBuffer.put(fileData).flip();

                long oggData = stb_vorbis_open_memory(dataBuffer, errorBuffer, null);

                if(oggData != 0) {
                    ByteBuffer infoBuffer = ByteBuffer.allocateDirect(STBVorbisInfo.SIZEOF);
                    STBVorbisInfo vorbisInfo = new STBVorbisInfo(infoBuffer);
                    stb_vorbis_get_info(oggData, vorbisInfo);

                    wave.sampleRate = vorbisInfo.sample_rate();
                    wave.sampleSize = 16;
                    wave.channels = vorbisInfo.channels();
                    wave.frameCount = stb_vorbis_stream_length_in_samples(oggData);
                    wave.data = ShortBuffer.allocate(wave.frameCount * wave.channels);
                    stb_vorbis_get_samples_short_interleaved(oggData, vorbisInfo.channels(), (ShortBuffer) wave.data);

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

    /*
    void UpdateSound(Sound sound, const void *data, int samplesCount);// Update sound buffer with new data
    */

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

    /*
    bool ExportWave(Wave wave, const char *fileName);               // Export wave data to file, returns true on success
    bool ExportWaveAsCode(Wave wave, const char *fileName);         // Export wave sample data to code (.h), returns true on success
    */

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
    
    /*
    void PlaySoundMulti(Sound sound);                               // Play a sound (using multichannel buffer pool)
    void StopSoundMulti(void);                                      // Stop any sound playing (using multichannel buffer pool)
    int GetSoundsPlaying(void);                                     // Get number of sounds playing in the multichannel
    
    void WaveFormat(Wave *wave, int sampleRate, int sampleSize, int channels);  // Convert wave data to desired format
    Wave WaveCopy(Wave wave);                                       // Copy a wave to a new wave
    void WaveCrop(Wave *wave, int initSample, int finalSample);     // Crop a wave to defined samples range
    float *LoadWaveSamples(Wave wave);                              // Load samples data from wave as a floats array
    void UnloadWaveSamples(float *samples);                         // Unload samples data loaded with LoadWaveSamples()
    */

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
        //TODO: flac
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
        //TODO: xm
        //TODO: mod
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
        //xm
        //mod
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
        //TODO
    }

    //Unload music Stream
    public void UnloadMusicStream(Music music) {
        UnloadAudioStream(music.stream);
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

    /*
    float GetMusicTimeLength(Music music);                          // Get music time length (in seconds)
    float GetMusicTimePlayed(Music music);                          // Get current music time played (in seconds)
    */

    /*
    AudioStream management functions
    */

    AudioStream LoadAudioStream(int sampleRate, int sampleSize, int channels) {
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

    /*
    void UpdateAudioStream(AudioStream stream, const void *data, int samplesCount); // Update audio stream buffers with data
    */

    // Check if any audio stream buffers requires refill
    public boolean IsAudioStreamProcessed(AudioStream stream) {
        if (stream.buffer == null) return false;

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
