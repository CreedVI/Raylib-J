package com.raylib.java.raudioal;

import com.raylib.java.Raylib;
import com.raylib.java.utils.FileIO;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import static com.raylib.java.Config.SUPPORT_FILEFORMAT_MP3;
import static com.raylib.java.Config.SUPPORT_FILEFORMAT_OGG;
import static com.raylib.java.core.rCore.IsFileExtension;
import static com.raylib.java.raudioal.rAudioAL.MusicContextType.MUSIC_AUDIO_OGG;
import static com.raylib.java.raudioal.rAudioAL.MusicContextType.MUSIC_MODULE_MP3;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC11.*;
import static org.lwjgl.stb.STBVorbis.*;

public class rAudioAL {

    /**********************************************************************************************
     *
     *   raylib.audio - Basic funtionality to work with audio
     *
     *   FEATURES:
     *       - Manage audio device (init/close)
     *       - Load and unload audio files
     *       - Format wave data (sample rate, size, channels)
     *       - Play/Stop/Pause/Resume loaded audio
     *       - Manage mixing channels
     *       - Manage raw audio context
     *
     *   CONFIGURATION:
     *
     *   #define AUDIO_STANDALONE
     *       Define to use the module as standalone library (independently of raylib).
     *       Required types and functions are defined in the same module.
     *
     *   #define SUPPORT_FILEFORMAT_WAV
     *   #define SUPPORT_FILEFORMAT_OGG
     *   #define SUPPORT_FILEFORMAT_XM
     *   #define SUPPORT_FILEFORMAT_MOD
     *   #define SUPPORT_FILEFORMAT_FLAC
     *       Selected desired fileformats to be supported for loading. Some of those formats are
     *       supported by default, to remove support, just comment unrequired #define in this module
     *
     *   LIMITATIONS:
     *       Only up to two channels supported: MONO and STEREO (for additional channels, use AL_EXT_MCFORMATS)
     *       Only the following sample sizes supported: 8bit PCM, 16bit PCM, 32-bit float PCM (using AL_EXT_FLOAT32)
     *
     *   DEPENDENCIES:
     *       OpenAL Soft - Audio device management (http://kcat.strangesoft.net/openal.html)
     *       stb_vorbis  - OGG audio files loading (http://www.nothings.org/stb_vorbis/)
     *       jar_xm      - XM module file loading
     *       jar_mod     - MOD audio file loading
     *       dr_flac     - FLAC audio file loading
     *
     *   CONTRIBUTORS:
     *       Joshua Reisenauer (github: @kd7tck):
     *           - XM audio module support (jar_xm)
     *           - MOD audio module support (jar_mod)
     *           - Mixing channels support
     *           - Raw audio context support
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2014-2017 Ramon Santamaria (@raysan5)
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     **********************************************************************************************/

    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    final static int MAX_STREAM_BUFFERS = 2;    // Number of buffers for each audio stream

    // NOTE: Music buffer size is defined by number of samples, independent of sample size and channels number
    // After some math, considering a sampleRate of 48000, a buffer refill rate of 1/60 seconds
    // and double-buffering system, I concluded that a 4096 samples buffer should be enough
    // In case of music-stalls, just increase this number
    final static int AUDIO_BUFFER_SIZE = 4096;    // PCM data samples (i.e. 16bit, Mono: 8Kb)

    // Support uncompressed PCM data in 32-bit float IEEE format
    // NOTE: This definition is included in "AL/alext.h", but some OpenAL implementations
    // could not provide the extensions header (Android), so its defined here

    final static int AL_EXT_float32 = 1;
    final static int AL_FORMAT_MONO_FLOAT32 = 0x10010;
    final static int AL_FORMAT_STEREO_FLOAT32 = 0x10011;

    final private Raylib context;

    //----------------------------------------------------------------------------------
    // Types and Structures Definition
    //----------------------------------------------------------------------------------

    static class MusicContextType { final static int MUSIC_AUDIO_OGG = 0, MUSIC_AUDIO_FLAC = 1, MUSIC_MODULE_XM = 2, MUSIC_MODULE_MOD = 3, MUSIC_MODULE_MP3 = 4; }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Audio Device initialization and Closing
    //----------------------------------------------------------------------------------

    public rAudioAL(Raylib context) {
        this.context = context;
    }

    // Initialize audio device
    public void InitAudioDevice() {
        // Open and initialize a device with default settings
        String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        long device = alcOpenDevice(defaultDevice);

        if (device == 0) {
            Tracelog(LOG_WARNING, "Failed to open default audio device. Trying alternative devices...");
            List<String> devices = ALUtil.getStringList(0, ALC_ALL_DEVICES_SPECIFIER);
            System.out.println("Available audio devices: " + devices);
            for (int i = 0; i < devices.size(); i++) {
                device = alcOpenDevice(devices.get(i));
                if (device != 0) {
                    Tracelog(LOG_INFO, "Fallback device opened. Using " + devices.get(i));
                    break;
                }
            }
        }

        if (device==0) {
            Tracelog(LOG_ERROR, "Audio device could not be opened");
        }
        else {
            int[] attrib = {0};
            long context = alcCreateContext(device, attrib);

            alcMakeContextCurrent(context);

            ALCCapabilities audioCapabilities = ALC.createCapabilities(device);
            ALCapabilities alCapabilities = AL.createCapabilities(audioCapabilities);

            if ((context == 0) || (!alCapabilities.OpenAL10)) {
                if (context != 0) {
                    alcDestroyContext(context);
                }

                alcCloseDevice(device);

                Tracelog(LOG_ERROR, "Could not initialize audio context");
            }
            else {
                Tracelog(LOG_INFO, "Audio device and context initialized successfully: " + alcGetString(device, ALC_DEVICE_SPECIFIER));

                // Listener definition (just for 2D)
                alListener3f(AL_POSITION, 0.0f, 0.0f, 0.0f);
                alListener3f(AL_VELOCITY, 0.0f, 0.0f, 0.0f);
                alListener3f(AL_ORIENTATION, 0.0f, 0.0f, -1.0f);

                alListenerf(AL_GAIN, 1.0f);
            }
        }
    }

    // Close the audio device for all contexts
    public void CloseAudioDevice() {
        long device;
        long context = alcGetCurrentContext();

        if (context == 0) {
            Tracelog(LOG_WARNING, "Could not get current audio context for closing");
        }

        device = alcGetContextsDevice(context);

        alcMakeContextCurrent(0);
        alcDestroyContext(context);
        alcCloseDevice(device);

        Tracelog(LOG_INFO, "Audio device closed successfully");
    }

    // Check if device has been initialized successfully
    public boolean IsAudioDeviceReady() {
        long context = alcGetCurrentContext();

        if (context == 0) {
            return false;
        }
        else {
            long device = alcGetContextsDevice(context);

            return device != 0;
        }
    }

    // Set master volume (listener)
    public void SetMasterVolume(float volume) {
        if (volume < 0.0f) volume = 0.0f;
        else if (volume > 1.0f) volume = 1.0f;

        alListenerf(AL_GAIN, volume);
    }

    //----------------------------------------------------------------------------------
// Module Functions Definition - Sounds loading and playing (.WAV)
//----------------------------------------------------------------------------------

    // Load wave data from file
    public Wave LoadWave(String fileName) {
        Wave wave = null;

        if (IsFileExtension(fileName, ".wav")) {
            wave = LoadWAV(fileName);
        }
        else if (IsFileExtension(fileName, ".ogg")) {
            wave = LoadOGG(fileName);
        }
        else if (IsFileExtension(fileName, ".mp3")) {
            wave = LoadMP3(fileName);
        }
        else if (IsFileExtension(fileName, ".flac")) {
            //wave = LoadFLAC(fileName);
        }
        /*else if (IsFileExtension(fileName, ".rres")) {
            RRES rres = LoadResource(fileName, 0);


            // NOTE: Parameters for RRES_TYPE_WAVE are: sampleCount, sampleRate, sampleSize, channels

            if (rres[0].type == RRES_TYPE_WAVE) wave = LoadWaveEx(rres[0].data, rres[0].param1, rres[0].param2, rres[0].param3, rres[0].param4);
            else Tracelog(LOG_WARNING, "["+fileName+"] Resource file does not contain wave data");

            UnloadResource(rres);
        }*/

        else {
            Tracelog(LOG_WARNING, "["+fileName+"] Audio fileformat not supported, it can't be loaded");
        }

        return wave;
    }

    // Load wave data from raw array data
    public Wave LoadWaveEx(ByteBuffer data, int sampleCount, int sampleRate, int sampleSize, int channels) {
        Wave wave = new Wave();

        wave.data = data;
        wave.sampleCount = sampleCount;
        wave.sampleRate = sampleRate;
        wave.sampleSize = sampleSize;
        wave.channels = channels;

        // NOTE: Copy wave data to work with, user is responsible of input data to free
        Wave cwave = WaveFormat(wave, sampleRate, sampleSize, channels);

        return cwave;
    }

    // Load sound from file
    // NOTE: The entire file is loaded to memory to be played (no-streaming)
    public Sound LoadSound(String fileName) {
        Wave wave = LoadWave(fileName);

        Sound sound = LoadSoundFromWave(wave);

        UnloadWave(wave);       // Sound is loaded, we can unload wave

        return sound;
    }

    // Load sound from wave data
    // NOTE: Wave data must be unallocated manually
    public Sound LoadSoundFromWave(Wave wave) {
        Sound sound = new Sound();

        if (wave.data != null) {
            int format = 0;

            // The OpenAL format is worked out by looking at the number of channels and the sample size (bits per sample)
            if (wave.channels == 1) {
                switch (wave.sampleSize) {
                    case 8:
                        format = AL_FORMAT_MONO8;
                        break;
                    case 16:
                        format = AL_FORMAT_MONO16;
                        break;
                    case 32:
                        format = AL_FORMAT_MONO_FLOAT32;
                        break;  // Requires OpenAL extension: AL_EXT_FLOAT32
                    default:
                        Tracelog(LOG_WARNING, "Wave sample size not supported: " + wave.sampleSize);
                        break;
                }
            }
            else if (wave.channels == 2) {
                switch (wave.sampleSize) {
                    case 8:
                        format = AL_FORMAT_STEREO8;
                        break;
                    case 16:
                        format = AL_FORMAT_STEREO16;
                        break;
                    case 32:
                        format = AL_FORMAT_STEREO_FLOAT32;
                        break;  // Requires OpenAL extension: AL_EXT_FLOAT32
                    default:
                        Tracelog(LOG_WARNING, "Wave sample size not supported: " + wave.sampleSize);
                        break;
                }
            }
            else {
                Tracelog(LOG_WARNING, "Wave number of channels not supported: " + wave.channels);
            }

            // Create an audio source
            int source = alGenSources();            // Generate pointer to audio source

            alSourcef(source, AL_PITCH, 1.0f);
            alSourcef(source, AL_GAIN, 1.0f);
            alSource3f(source, AL_POSITION, 0.0f, 0.0f, 0.0f);
            alSource3f(source, AL_VELOCITY, 0.0f, 0.0f, 0.0f);
            alSourcei(source, AL_LOOPING, AL_FALSE);

            // Convert loaded data to OpenAL buffer
            //----------------------------------------
            int buffer = alGenBuffers();            // Generate pointer to buffer

            int dataSize = wave.sampleCount*wave.channels*wave.sampleSize/8;    // Size in bytes

            // Upload sound data to buffer
            if(wave.data instanceof ByteBuffer) {
                alBufferData(buffer, format, ((ByteBuffer) wave.data), wave.sampleRate);
            }
            else if(wave.data instanceof ShortBuffer) {
                alBufferData(buffer, format, ((ShortBuffer) wave.data), wave.sampleRate);
            }

            // Attach sound buffer to source
            alSourcei(source, AL_BUFFER, buffer);

            Tracelog(LOG_INFO, "[SND ID "+source+"][BUFR ID "+buffer+"] Sound data loaded successfully ("+wave.sampleRate+" Hz, "+wave.sampleSize+" bit, "+((wave.channels == 1) ? "Mono" : "Stereo")+")");

            sound.source = source;
            sound.buffer = buffer;
            sound.format = format;
        }

        return sound;
    }

    // Unload wave data
    public void UnloadWave(Wave wave) {
        if (wave.data != null) {
           wave.data.clear();
        }

        Tracelog(LOG_INFO, "Unloaded wave data from RAM");
    }

    // Unload sound
    public void UnloadSound(Sound sound) {
        alSourceStop(sound.source);

        alDeleteSources(sound.source);
        alDeleteBuffers(sound.buffer);

        Tracelog(LOG_INFO, "[SND ID "+sound.source+"][BUFR ID "+sound.buffer+"] Unloaded sound data from RAM");
    }

    // Update sound buffer with new data
    // NOTE: data must match sound.format
    public void UpdateSound(Sound sound, ByteBuffer data, int samplesCount) {
        int sampleRate, sampleSize, channels;
        sampleRate = alGetBufferi(sound.buffer, AL_FREQUENCY);
        sampleSize = alGetBufferi(sound.buffer, AL_BITS);           // It could also be retrieved from sound.format
        channels = alGetBufferi(sound.buffer, AL_CHANNELS);         // It could also be retrieved from sound.format

        Tracelog(LOG_DEBUG, "UpdateSound() : AL_FREQUENCY: " + sampleRate);
        Tracelog(LOG_DEBUG, "UpdateSound() : AL_BITS: " + sampleSize);
        Tracelog(LOG_DEBUG, "UpdateSound() : AL_CHANNELS: " + channels);

        int dataSize = samplesCount*channels*sampleSize/8;   // Size of data in bytes

        alSourceStop(sound.source);                 // Stop sound
        alSourcei(sound.source, AL_BUFFER, 0);      // Unbind buffer from sound to update
        //alDeleteBuffers(1, &sound.buffer);          // Delete current buffer data
        //alGenBuffers(1, &sound.buffer);             // Generate new buffer

        // Upload new data to sound buffer
        alBufferData(sound.buffer, sound.format, data, sampleRate);

        // Attach sound buffer to source again
        alSourcei(sound.source, AL_BUFFER, sound.buffer);
    }

    // Play a sound
    public void PlaySound(Sound sound) {
        alSourcePlay(sound.source);        // Play the sound

        //Tracelog(LOG_INFO, "Playing sound");

        // Find the current position of the sound being played
        // NOTE: Only work when the entire file is in a single buffer
        //int byteOffset;
        //alGetSourcei(sound.source, AL_BYTE_OFFSET, &byteOffset);
        //
        //int sampleRate;
        //alGetBufferi(sound.buffer, AL_FREQUENCY, &sampleRate);    // AL_CHANNELS, AL_BITS (bps)

        //float seconds = (float)byteOffset/sampleRate;      // Number of seconds since the beginning of the sound
        //or
        //float result;
        //alGetSourcef(sound.source, AL_SEC_OFFSET, &result);   // AL_SAMPLE_OFFSET
    }

    // Pause a sound
    public void PauseSound(Sound sound) {
        alSourcePause(sound.source);
    }

    // Resume a paused sound
    public void ResumeSound(Sound sound) {
        int state = alGetSourcei(sound.source, AL_SOURCE_STATE);

        if (state == AL_PAUSED) {
            alSourcePlay(sound.source);
        }
    }

    // Stop reproducing a sound
    public void StopSound(Sound sound) {
        alSourceStop(sound.source);
    }

    // Check if a sound is playing
    public boolean IsSoundPlaying(Sound sound) {
        boolean playing = false;
        int state = alGetSourcei(sound.source, AL_SOURCE_STATE);

        if (state == AL_PLAYING) {
            playing = true;
        }

        return playing;
    }

    // Set volume for a sound
    public void SetSoundVolume(Sound sound, float volume) {
        alSourcef(sound.source, AL_GAIN, volume);
    }

    // Set pitch for a sound
    public void SetSoundPitch(Sound sound, float pitch) {
        alSourcef(sound.source, AL_PITCH, pitch);
    }

    // Convert wave data to desired format
    public Wave WaveFormat(Wave wave, int sampleRate, int sampleSize, int channels) {
        // Format sample rate
        // NOTE: Only supported 22050 <--> 44100
        if (wave.sampleRate != sampleRate) {
            // TODO: Resample wave data (upsampling or downsampling)
            // NOTE 1: To downsample, you have to drop samples or average them.
            // NOTE 2: To upsample, you have to interpolate new samples.

            wave.sampleRate = sampleRate;

            return wave;
        }

        // Format sample size
        // NOTE: Only supported 8 bit <--> 16 bit <--> 32 bit
        if (wave.sampleSize != sampleSize) {
            ByteBuffer data = ByteBuffer.allocateDirect(wave.sampleCount*wave.channels*sampleSize/8);

            for (int i = 0; i < wave.sampleCount; i++) {
                for (int j = 0; j < wave.channels; j++) {
                    if (sampleSize == 8) {
                        if (wave.sampleSize == 16) {
                            data.put(wave.channels * i + j, (byte) ((((ByteBuffer)wave.data).get(wave.channels * i + j)/32767.0f)*256));
                        }
                        else if (wave.sampleSize == 32) {
                            data.put(wave.channels * i + j, (byte) (((ByteBuffer)wave.data).get(wave.channels * i + j)*127.0f + 127));
                        }
                    }
                    else if (sampleSize == 16) {
                        if (wave.sampleSize == 8) {
                            data.put(wave.channels * i + j, (byte) (((((ByteBuffer)wave.data).get(wave.channels * i + j)-127)/256.0f)*32767));
                        }
                        else if (wave.sampleSize == 32) {
                            data.put(wave.channels * i + j, (byte) (((ByteBuffer)wave.data).get(wave.channels * i + j)*32767));
                        }
                    }
                    else if (sampleSize == 32) {
                        if (wave.sampleSize == 8) {
                            data.put(wave.channels * i + j, (byte) ((((ByteBuffer)wave.data).get(wave.channels * i + j)-127)/256.0f));
                        }
                        else if (wave.sampleSize == 16) {
                            data.put(wave.channels * i + j, (byte) (((ByteBuffer)wave.data).get(wave.channels * i + j)/32767.0f));
                        }
                    }
                }
            }

            wave.sampleSize = sampleSize;
            wave.data.clear();
            wave.data = data;
        }

        // Format channels (interlaced mode)
        // NOTE: Only supported mono <--> stereo
        if (wave.channels != channels) {
            ByteBuffer data = ByteBuffer.allocateDirect(wave.sampleCount*wave.sampleSize/8*channels);

            if ((wave.channels == 1) && (channels == 2)) {       // mono ---> stereo (duplicate mono information)
                for (int i = 0; i < wave.sampleCount; i++) {
                    for (int j = 0; j < channels; j++) {
                        if (wave.sampleSize == 8) {
                            data.put(channels * i + j, ((ByteBuffer)wave.data).get(i));
                        }
                        else if (wave.sampleSize == 16) {
                            data.put(channels * i + j, ((ByteBuffer)wave.data).get(i));
                        }
                        else if (wave.sampleSize == 32) {
                            data.put(channels * i + j, ((ByteBuffer)wave.data).get(i));
                        }
                    }
                }
            }
            else if ((wave.channels == 2) && (channels == 1)) { // stereo ---> mono (mix stereo channels)
                for (int i = 0, j = 0; i < wave.sampleCount; i++, j += 2) {
                    if (wave.sampleSize == 8) {
                        data.put(i,(((ByteBuffer)wave.data).get((j+j + 1)/2)));
                    }
                    else if (wave.sampleSize == 16) {
                        data.put(i,(((ByteBuffer)wave.data).get((j+j + 1)/2)));
                    }
                    else if (wave.sampleSize == 32) {
                        data.put(i,(((ByteBuffer)wave.data).get((j+j + 1)/2)));
                    }
                }
            }

            // TODO: Add/remove additional interlaced channels

            wave.channels = channels;
            wave.data.clear();
            wave.data = data;
        }

        return wave;
    }

    // Copy a wave to a new wave
    public Wave WaveCopy(Wave wave) {
        Wave newWave = new Wave();

        newWave.data = ByteBuffer.allocateDirect(wave.sampleCount*wave.sampleSize/8*wave.channels);

        if (newWave.data != null) {
            if(wave.data instanceof ByteBuffer) {
                ((ByteBuffer) newWave.data).put((ByteBuffer) wave.data);
            }
            else if(wave.data instanceof ShortBuffer) {
                ((ShortBuffer) newWave.data).put((ShortBuffer) wave.data);
            }
            newWave.sampleCount = wave.sampleCount;
            newWave.sampleRate = wave.sampleRate;
            newWave.sampleSize = wave.sampleSize;
            newWave.channels = wave.channels;
        }

        return newWave;
    }

    // Crop a wave to defined samples range
    // NOTE: Security check in case of out-of-range
    public void WaveCrop(Wave wave, int initSample, int finalSample) {
        if ((initSample >= 0) && (initSample < finalSample) && (finalSample > 0) && (finalSample < wave.sampleCount)) {
            int sampleCount = finalSample - initSample;

            ByteBuffer data = ByteBuffer.allocateDirect(sampleCount*wave.sampleSize/8*wave.channels);

            for(int i = initSample*wave.channels*wave.sampleSize/8; i < sampleCount*wave.channels*wave.sampleSize/8; i++) {
                data.put((byte)i);
            }

            wave.data.clear();
            wave.data = data;
        }
        else {
            Tracelog(LOG_WARNING, "Wave crop range out of bounds");
        }
    }

    // Get samples data from wave as a floats array
    // NOTE: Returned sample values are normalized to range [-1..1]
    public float[] GetWaveData(Wave wave) {
        float[] samples = new float[wave.sampleCount*wave.channels];

        for (int i = 0; i < wave.sampleCount; i++) {
            for (int j = 0; j < wave.channels; j++) {
                if (wave.sampleSize == 8) {
                    samples[wave.channels * i + j] = (((ByteBuffer)wave.data).get(wave.channels * i + j)-127)/256.0f;
                }
                else if (wave.sampleSize == 16) {
                    samples[wave.channels * i + j] = (((ShortBuffer)wave.data).get(wave.channels * i + j)/32767.0f);
                }
                else if (wave.sampleSize == 32) {
                    samples[wave.channels * i + j] = (((ByteBuffer)wave.data).getFloat(wave.channels * i + j));
                }
            }
        }

        return samples;
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Music loading and stream playing (.OGG)
    //----------------------------------------------------------------------------------

    // Load music stream from file
    public Music LoadMusicStream(String fileName) {
        Music music = new Music();

        //TODO: .wav ?

        if (SUPPORT_FILEFORMAT_OGG && IsFileExtension(fileName, ".ogg")) {
            // Open ogg audio stream
            IntBuffer errorBuffer = IntBuffer.allocate(1);
            music.ctxOgg = stb_vorbis_open_filename(fileName, errorBuffer, null);

            if (music.ctxOgg == 0) {
                Tracelog(LOG_WARNING, "["+fileName+"] OGG audio file could not be opened");
            }
            else {
                 // Get Ogg file info
                STBVorbisInfo info = null;
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer channels = stack.mallocInt(1);
                    IntBuffer sampleRate = stack.mallocInt(1);
                    ByteBuffer infoBuffer = stack.malloc(STBVorbisInfo.SIZEOF);
                    info = new STBVorbisInfo(infoBuffer);
                    stb_vorbis_get_info(music.ctxOgg, info);

                    // OGG bit rate defaults to 16 bit, it's enough for compressed format
                    music.stream = InitAudioStream(info.sample_rate(), 16, info.channels());
                    music.totalSamples = stb_vorbis_stream_length_in_samples(music.ctxOgg); // Independent by channel
                    music.samplesLeft = music.totalSamples;
                    music.ctxType = MUSIC_AUDIO_OGG;
                    music.loopCount = -1;                       // Infinite loop by default
                }
                finally {

                    Tracelog(LOG_DEBUG, "["+fileName+"] FLAC total samples: " + music.totalSamples);
                    Tracelog(LOG_DEBUG, "["+fileName+"] OGG sample rate: " + info.sample_rate());
                    Tracelog(LOG_DEBUG, "["+fileName+"] OGG channels: " + info.channels());
                    Tracelog(LOG_DEBUG, "["+fileName+"] OGG memory required: " + info.temp_memory_required());
                }

            }
        }
        else if(SUPPORT_FILEFORMAT_MP3 && IsFileExtension(fileName, ".mp3")) {
            try {
                byte[] fileData = FileIO.LoadFileData(fileName);
                fr.delthas.javamp3.Sound mp3Sound = new fr.delthas.javamp3.Sound(new ByteArrayInputStream(fileData));

                byte[] mp3Data = new byte[fileData.length];
                int read = mp3Sound.read(mp3Data);
                ByteBuffer rawAudio = ByteBuffer.allocateDirect(read);
                rawAudio.put(mp3Data).flip();

                music.stream = InitAudioStream(mp3Sound.getSamplingFrequency(), 16, mp3Sound.isStereo() ? 2 : 1);
                for (int i = 0; i < MAX_STREAM_BUFFERS; i++) {
                    alBufferData(music.stream.buffers[i], music.stream.format, rawAudio, music.stream.sampleRate);
                }
                music.totalSamples = read * 2;
                music.samplesLeft = music.totalSamples;
                music.ctxType = MUSIC_MODULE_MP3;
                music.loopCount = -1;
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        /*
        else if (IsFileExtension(fileName, ".flac")) {
            music.ctxFlac = drflac_open_file(fileName);


            if (music.ctxFlac == null) Tracelog(LOG_WARNING, "["+fileName+"] FLAC audio file could not be opened");
            else
            {
                music.stream = InitAudioStream(music.ctxFlac.sampleRate, music.ctxFlac.bitsPerSample, music.ctxFlac.channels);
                music.totalSamples = (unsigned int)music.ctxFlac.totalSampleCount/music.ctxFlac.channels;
                music.samplesLeft = music.totalSamples;
                music.ctxType = MUSIC_AUDIO_FLAC;
                music.loopCount = -1;                       // Infinite loop by default

                Tracelog(LOG_DEBUG, "["+music.totalSamples+"] FLAC total samples: " + fileName);
                Tracelog(LOG_DEBUG, "["+music.ctxFlac.sampleRate+"] FLAC sample rate: " + fileName);
                Tracelog(LOG_DEBUG, "["+music.ctxFlac.bitsPerSample+"] FLAC bits per sample: " + fileName);
                Tracelog(LOG_DEBUG, "["+ music.ctxFlac.channels+"] FLAC channels: " + fileName);
            }
        }
        /*
        #if defined(SUPPORT_FILEFORMAT_XM)
            else if (IsFileExtension(fileName, ".xm")) {
                int result = jar_xm_create_context_from_file(&music.ctxXm, 48000, fileName);

                if (!result)    // XM context created successfully
                {
                    jar_xm_set_max_loop_count(music.ctxXm, 0); // Set infinite number of loops

                    // NOTE: Only stereo is supported for XM
                    music.stream = InitAudioStream(48000, 16, 2);
                    music.totalSamples = (unsigned int)jar_xm_get_remaining_samples(music.ctxXm);
                    music.samplesLeft = music.totalSamples;
                    music.ctxType = MUSIC_MODULE_XM;
                    music.loopCount = -1;                       // Infinite loop by default

                    Tracelog(LOG_DEBUG, "["++"] XM number of samples: " + fileName, music.totalSamples);
                    Tracelog(LOG_DEBUG, "["++"] XM track length: %11.6f sec", fileName, (float)music.totalSamples/48000.0f);
                }
                else Tracelog(LOG_WARNING, "["++"] XM file could not be opened", fileName);
            }
        #endif
        #if defined(SUPPORT_FILEFORMAT_MOD)
            else if (IsFileExtension(fileName, ".mod")) {
                jar_mod_init(&music.ctxMod);

                if (jar_mod_load_file(&music.ctxMod, fileName))
                {
                    music.stream = InitAudioStream(48000, 16, 2);
                    music.totalSamples = (unsigned int)jar_mod_max_samples(&music.ctxMod);
                    music.samplesLeft = music.totalSamples;
                    music.ctxType = MUSIC_MODULE_MOD;
                    music.loopCount = -1;                       // Infinite loop by default

                    Tracelog(LOG_DEBUG, "["++"] MOD number of samples: " + fileName, music.samplesLeft);
                    Tracelog(LOG_DEBUG, "["++"] MOD track length: %11.6f sec", fileName, (float)music.totalSamples/48000.0f);
                }
                else Tracelog(LOG_WARNING, "["++"] MOD file could not be opened", fileName);
            }
        #endif
        */
    else {
        Tracelog(LOG_WARNING, "["+fileName+"] Audio fileformat not supported, it can't be loaded");
        }

        return music;
    }

    // Unload music stream
    public void UnloadMusicStream(Music music) {
        CloseAudioStream(music.stream);

        if (music.ctxType == MUSIC_AUDIO_OGG) {
            stb_vorbis_close(music.ctxOgg);
        }
        /*
        #if defined(SUPPORT_FILEFORMAT_FLAC)
            else if (music.ctxType == MUSIC_AUDIO_FLAC) drflac_free(music.ctxFlac);
        #endif
        #if defined(SUPPORT_FILEFORMAT_XM)
            else if (music.ctxType == MUSIC_MODULE_XM) jar_xm_free_context(music.ctxXm);
        #endif
        #if defined(SUPPORT_FILEFORMAT_MOD)
            else if (music.ctxType == MUSIC_MODULE_MOD) jar_mod_unload(&music.ctxMod);
        #endif
         */
    }

    // Start music playing (open stream)
    public void PlayMusicStream(Music music) {
        alSourcePlay(music.stream.source);
    }

    // Pause music playing
    public void PauseMusicStream(Music music) {
        alSourcePause(music.stream.source);
    }

    // Resume music playing
    public void ResumeMusicStream(Music music) {
        int state = alGetSourcei(music.stream.source, AL_SOURCE_STATE);

        if (state == AL_PAUSED) {
            Tracelog(LOG_INFO, "[AUD ID "+music.stream.source+"] Resume music stream playing");
            alSourcePlay(music.stream.source);
        }
    }

    // Stop music playing (close stream)
    // TODO: To clear a buffer, make sure they have been already processed!
    public void StopMusicStream(Music music) {
        alSourceStop(music.stream.source);

        /*
        // Clear stream buffers
        // WARNING: Queued buffers must have been processed before unqueueing and reloaded with data!!!
        void *pcm = calloc(AUDIO_BUFFER_SIZE*music->stream.sampleSize/8*music.stream.channels, 1);

        for (int i = 0; i < MAX_STREAM_BUFFERS; i++) {
            //UpdateAudioStream(music->stream, pcm, AUDIO_BUFFER_SIZE);       // Update one buffer at a time
            alBufferData(music->stream.buffers[i], music->stream.format, pcm, AUDIO_BUFFER_SIZE*music->stream.sampleSize/8*music->stream.channels, music->stream.sampleRate);
        }
        free(pcm);
        */

        // Restart music context
        switch (music.ctxType) {
            case MUSIC_AUDIO_OGG:
                stb_vorbis_seek_start(music.ctxOgg);
                break;
            //case MUSIC_MODULE_FLAC: /* TODO: Restart FLAC context */ break;
            //case MUSIC_MODULE_XM: /* TODO: Restart XM context */ break;
            //case MUSIC_MODULE_MOD: jar_mod_seek_start(&music.ctxMod); break;
            default:
                break;
        }

        music.samplesLeft = music.totalSamples;
    }

    // Update (re-fill) music buffers if data already processed
    // TODO: Make sure buffers are ready for update... check music state
    public void UpdateMusicStream(Music music) {
        int state;
        int processed = 0;

        state = alGetSourcei(music.stream.source, AL_SOURCE_STATE);          // Get music stream state
        processed = alGetSourcei(music.stream.source, AL_BUFFERS_PROCESSED); // Get processed buffers

        if (processed > 0) {
            boolean streamEnding = false;

            // NOTE: Using dynamic allocation because it could require more than 16KB
            ByteBuffer pcm = ByteBuffer.allocateDirect(AUDIO_BUFFER_SIZE*music.stream.sampleSize/8*music.stream.channels);

            int numBuffersToProcess = processed;
            int samplesCount = 0;    // Total size of data steamed in L+R samples for xm floats,
            // individual L or R for ogg shorts

            for (int i = 0; i < numBuffersToProcess; i++) {
                if (music.samplesLeft >= AUDIO_BUFFER_SIZE) {
                    samplesCount = AUDIO_BUFFER_SIZE;
                }
                else {
                    samplesCount = music.samplesLeft;
                }

                // TODO: Really don't like ctxType thingy...
                switch (music.ctxType) {
                    case MUSIC_AUDIO_OGG: {
                        // NOTE: Returns the number of samples to process (be careful! we ask for number of shorts!)
                        int numSamplesOgg = stb_vorbis_get_samples_short_interleaved(music.ctxOgg, music.stream.channels, pcm.asShortBuffer());
                        break;
                    }
                    /*
                    #if defined(SUPPORT_FILEFORMAT_FLAC)
                            case MUSIC_AUDIO_FLAC:
                            {
                                // NOTE: Returns the number of samples to process
                                unsigned int numSamplesFlac = (unsigned int)drflac_read_s16(music.ctxFlac, samplesCount*music.stream.channels, (short *)pcm);

                            } break;
                    #endif
                    #if defined(SUPPORT_FILEFORMAT_XM)
                            case MUSIC_MODULE_XM: jar_xm_generate_samples_16bit(music.ctxXm, pcm, samplesCount); break;
                    #endif
                    #if defined(SUPPORT_FILEFORMAT_MOD)
                            case MUSIC_MODULE_MOD: jar_mod_fillbuffer(&music.ctxMod, pcm, samplesCount, 0); break;
                    #endif
                     */
                    default:
                        break;
                }

                UpdateAudioStream(music.stream, pcm, samplesCount);
                music.samplesLeft -= samplesCount;

                if (music.samplesLeft <= 0)
                {
                    streamEnding = true;
                    break;
                }
            }

            // Free allocated pcm data
            pcm.clear();

            // Reset audio stream for looping
            if (streamEnding) {
                StopMusicStream(music);        // Stop music (and reset)

                // Decrease loopCount to stop when required
                if (music.loopCount > 0) {
                    music.loopCount--;        // Decrease loop count
                    PlayMusicStream(music);    // Play again
                }
            }
            else {
                // NOTE: In case window is minimized, music stream is stopped,
                // just make sure to play again on window restore
                if (state != AL_PLAYING) {
                    PlayMusicStream(music);
                }
            }
        }
    }

    // Check if any music is playing
    public boolean IsMusicPlaying(Music music) {
        boolean playing = false;
        int state;

        state = alGetSourcei(music.stream.source, AL_SOURCE_STATE);

        if (state == AL_PLAYING) playing = true;

        return playing;
    }

    // Set volume for music
    public void SetMusicVolume(Music music, float volume) {
        alSourcef(music.stream.source, AL_GAIN, volume);
    }

    // Set pitch for music
    public void SetMusicPitch(Music music, float pitch) {
        alSourcef(music.stream.source, AL_PITCH, pitch);
    }

    // Set music loop count (loop repeats)
    // NOTE: If set to -1, means infinite loop
    public void SetMusicLoopCount(Music music, int count) {
        music.loopCount = count;
    }

    // Get music time length (in seconds)
    public float GetMusicTimeLength(Music music) {
        float totalSeconds = (float)music.totalSamples/music.stream.sampleRate;

        return totalSeconds;
    }

    // Get current music time played (in seconds)
    public float GetMusicTimePlayed(Music music) {
        float secondsPlayed = 0.0f;

        int samplesPlayed = music.totalSamples - music.samplesLeft;
        secondsPlayed = (float)samplesPlayed/music.stream.sampleRate;

        return secondsPlayed;
    }

    // Init audio stream (to stream audio pcm data)
    public AudioStream InitAudioStream(int sampleRate, int sampleSize, int channels) {
        AudioStream stream = new AudioStream();

        stream.sampleRate = sampleRate;
        stream.sampleSize = sampleSize;

        // Only mono and stereo channels are supported, more channels require AL_EXT_MCFORMATS extension
        if ((channels > 0) && (channels < 3)) {
            stream.channels = channels;
        }
        else {
            Tracelog(LOG_WARNING, "Init audio stream: Number of channels not supported: " + channels);
            stream.channels = 1;  // Fallback to mono channel
        }

        // Setup OpenAL format
        if (stream.channels == 1) {
            switch (sampleSize) {
                case 8: stream.format = AL_FORMAT_MONO8; break;
                case 16: stream.format = AL_FORMAT_MONO16; break;
                case 32: stream.format = AL_FORMAT_MONO_FLOAT32; break;     // Requires OpenAL extension: AL_EXT_FLOAT32
                default: Tracelog(LOG_WARNING, "Init audio stream: Sample size not supported: " + sampleSize); break;
            }
        }
        else if (stream.channels == 2) {
            switch (sampleSize) {
                case 8:
                    stream.format = AL_FORMAT_STEREO8;
                    break;
                case 16:
                    stream.format = AL_FORMAT_STEREO16;
                    break;
                case 32:
                    stream.format = AL_FORMAT_STEREO_FLOAT32;
                    break;   // Requires OpenAL extension: AL_EXT_FLOAT32
                default:
                    Tracelog(LOG_WARNING, "Init audio stream: Sample size not supported: " + sampleSize);
                    break;
            }
        }

        // Create an audio source
        stream.source = alGenSources();
        alSourcef(stream.source, AL_PITCH, 1.0f);
        alSourcef(stream.source, AL_GAIN, 1.0f);
        alSource3f(stream.source, AL_POSITION, 0.0f, 0.0f, 0.0f);
        alSource3f(stream.source, AL_VELOCITY, 0.0f, 0.0f, 0.0f);

        // Create Buffers (double buffering)
        for(int i = 0; i < MAX_STREAM_BUFFERS; i++) {
            stream.buffers[i] = alGenBuffers();
        }

        // Initialize buffer with zeros by default
        // NOTE: Using dynamic allocation because it requires more than 16KB
        ByteBuffer pcm = ByteBuffer.allocateDirect(AUDIO_BUFFER_SIZE*stream.sampleSize/8*stream.channels);

        for (int i = 0; i < MAX_STREAM_BUFFERS; i++) {
            alBufferData(stream.buffers[i], stream.format, pcm, stream.sampleRate);
        }

        pcm.clear();

        alSourceQueueBuffers(stream.source, stream.buffers);

        Tracelog(LOG_INFO, "[AUD ID "+ stream.source+"] Audio stream loaded successfully ("+stream.sampleRate+" Hz, "+stream.sampleSize+" bit, "+((stream.channels == 1) ? "Mono" : "Stereo")+")");

        return stream;
    }

    // Close audio stream and free memory
    public void CloseAudioStream(AudioStream stream) {
        // Stop playing channel
        alSourceStop(stream.source);

        // Flush out all queued buffers
        int queued = alGetSourcei(stream.source, AL_BUFFERS_QUEUED);

        int buffer = 0;

        while (queued > 0) {
            buffer = alSourceUnqueueBuffers(stream.source);
            queued--;
        }

        // Delete source and buffers
        alDeleteSources(stream.source);
        alDeleteBuffers(stream.buffers);

        Tracelog(LOG_INFO, "[AUD ID "+stream.source+"] Unloaded audio stream data");
    }

    // Update audio stream buffers with data
    // NOTE 1: Only updates one buffer of the stream source: unqueue -> update -> queue
    // NOTE 2: To unqueue a buffer it needs to be processed: IsAudioBufferProcessed()
    public void UpdateAudioStream(AudioStream stream, ByteBuffer data, int samplesCount) {
        int buffer = alSourceUnqueueBuffers(stream.source);

        // Check if any buffer was available for unqueue
        if (alGetError() != AL_INVALID_VALUE) {
            alBufferData(buffer, stream.format, data, stream.sampleRate);
            alSourceQueueBuffers(stream.source, buffer);
        }
        else {
            Tracelog(LOG_WARNING, "[AUD ID "+stream.source+"] Audio buffer not available for unqueuing");
        }
    }

    // Check if any audio stream buffers requires refill
    public boolean IsAudioBufferProcessed(AudioStream stream) {
        // Determine if music stream is ready to be written
        int processed = alGetSourcei(stream.source, AL_BUFFERS_PROCESSED);

        return (processed > 0);
    }

    // Play audio stream
    public void PlayAudioStream(AudioStream stream) {
        alSourcePlay(stream.source);
    }

    // Play audio stream
    public void PauseAudioStream(AudioStream stream) {
        alSourcePause(stream.source);
    }

    // Resume audio stream playing
    public void ResumeAudioStream(AudioStream stream) {
        int state = alGetSourcei(stream.source, AL_SOURCE_STATE);

        if (state == AL_PAUSED) {
            alSourcePlay(stream.source);
        }
    }

    // Stop audio stream
    public void StopAudioStream(AudioStream stream) {
        alSourceStop(stream.source);
    }

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    // Load WAV file into Wave structure
    Wave LoadWAV(String fileName) {
        Wave wave = new Wave();

        // Loading file to memory
        byte[] fileData = new byte[0];
        try{
            fileData = FileIO.LoadFileData(fileName);
        } catch (IOException e){
            e.printStackTrace();
        }

        // Loading wave from memory data
        if (fileData != null) {
            wave.sampleRate = alcGetInteger(alcGetContextsDevice(alcGetCurrentContext()), ALC_FREQUENCY);
            wave.sampleSize = 16;
            wave.channels = 2;
            ByteBuffer data = ByteBuffer.allocateDirect(fileData.length);
            data.put(fileData).flip();
            wave.data = data;
            wave.sampleCount = (wave.data.capacity()/(wave.sampleSize/8))/wave.channels;
        }
        return wave;
    }


    // Load OGG file into Wave structure
    // NOTE: Using stb_vorbis library
    Wave LoadOGG(String fileName) {
        Wave wave = new Wave();

        // Loading file to memory
        byte[] fileData = new byte[0];
        try{
            fileData = FileIO.LoadFileData(fileName);
        } catch (IOException e){
            e.printStackTrace();
        }

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
                wave.sampleCount = stb_vorbis_stream_length_in_samples(oggData);
                wave.data = oggAudio;

                stb_vorbis_close(oggData);
            }
        }

        return wave;
    }

    public Wave LoadMP3(String filename) {
        Wave wave = new Wave();
        try {
            byte[] fileData = FileIO.LoadFileData(filename);
            fr.delthas.javamp3.Sound mp3Sound = new fr.delthas.javamp3.Sound(new ByteArrayInputStream(fileData));

            byte[] mp3Data = new byte[fileData.length];
            int read = mp3Sound.read(mp3Data);
            ByteBuffer rawAudio = ByteBuffer.allocateDirect(read);
            rawAudio.put(mp3Data).flip();

            wave.sampleSize = 16;
            wave.channels = 2;
            wave.sampleRate = alcGetInteger(alcGetContextsDevice(alcGetCurrentContext()), ALC_FREQUENCY);
            wave.data = rawAudio;
            wave.sampleCount = (wave.data.capacity()/(wave.sampleSize/8))/wave.channels;
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return wave;
    }
    /*

    #if defined(SUPPORT_FILEFORMAT_FLAC)
        // Load FLAC file into Wave structure
    // NOTE: Using dr_flac library
        static Wave LoadFLAC(String fileName) {
            Wave wave;

            // Decode an entire FLAC file in one go
            uint64_t totalSampleCount;
            wave.data = drflac_open_and_decode_file_s16(fileName, &wave.channels, &wave.sampleRate, &totalSampleCount);

            wave.sampleCount = (int)totalSampleCount/wave.channels;
            wave.sampleSize = 16;

            // NOTE: Only support up to 2 channels (mono, stereo)
            if (wave.channels > 2) Tracelog(LOG_WARNING, "["++"] FLAC channels number ("++") not supported", fileName, wave.channels);

            if (wave.data == null) Tracelog(LOG_WARNING, "["++"] FLAC data could not be loaded", fileName);
            else Tracelog(LOG_INFO, "["++"] FLAC file loaded successfully ("++" Hz, "++" bit, %s)", fileName, wave.sampleRate, wave.sampleSize, (wave.channels == 1) ? "Mono" : "Stereo");

            return wave;
        }
    #endif
    */


}
