package com.raylib.audio;

import java.io.File;

public class Audio {
    public static final AudioDevice audioDevice = AudioDevice.getInstance();
    public Audio() {

    }
    // Audio device management
    static void InitAudioDevice() {
        AudioDevice audioDevice = AudioDevice.getInstance();
    }

    static void CloseAudioDevice() {
        AudioDevice audioDevice = AudioDevice.getInstance();
        audioDevice.destruct();
    }

    static boolean isAudioDeviceReady() {
        return audioDevice.ready();
    }

    static void SetMasterVolume(float volume) {
        audioDevice.setMasterVolume(volume);
    }

    // Wave/Sound loading/unloading functions
    static Wave LoadWave(String filename) {
        File file = new File(filename);
        return null;
    }

    static Sound LoadSound(String filename){ return null;}
    static Sound LoadSoundFromWave(Wave wave){return null;}
    static void UpdateSound(Sound sound, Object data, int samplesCount) {}
    static void UnloadWave(Wave wave) {}
    static void UnloadSound(Sound sound) {}
    static boolean ExportWave(Wave wave, String fileName) {return false;}
    static boolean ExportWaveAsCode(Wave wave, String fileName) {return false;}

    // Wave/Sound management functions
    static void PlaySound(Sound sound) {}
    static void StopSound(Sound sound) {}
    static void PauseSound(Sound sound) {}
    static void ResumeSound(Sound sound) {}
    static void PlaySoundMulti(Sound sound) {}
    static void StopSoundMulti(Sound sound) {}
    static int GetSoundsPlaying() {return 0;}
    static boolean IsSoundPlaying(Sound sound) {return false;}
    static void SetSoundVolume(Sound sound, float volume) {}
    static void SetSoundPitch(Sound sound, float pitch) {}
    static void WaveFormat(Wave wave, int sampleRate, int sampleSize, int channels) {}
    static Wave WaveCopy(Wave wave) {return null;}
    static void WaveCrop(Wave wave, int initSample, int finalSample) {}
    static float LoadWaveSamples(Wave wave) {return 0.0f;}

    // Music management functions
    static Music LoadMusicStream(String fileName) {return null;}                                    // Load music stream from file
    static Music LoadMusicStreamFromMemory(String fileType, String data, int dataSize) {return null;} // Load music stream from data
    static void UnloadMusicStream(Music music) {}                                           // Unload music stream
    static void PlayMusicStream(Music music) {}                                            // Start music playing
    static boolean IsMusicPlaying(Music music) {return false;}                                            // Check if music is playing
    static void UpdateMusicStream(Music music) {}                                         // Updates buffers for music streaming
    static void StopMusicStream(Music music) {}                                            // Stop music playing
    static void PauseMusicStream(Music music) {}                                          // Pause music playing
    static void ResumeMusicStream(Music music) {}                                           // Resume playing paused music
    static void SetMusicVolume(Music music, float volume) {}                                // Set volume for music (1.0 is max level)
    static void SetMusicPitch(Music music, float pitch) {}                              // Set pitch for a music (1.0 is base level)
    static float GetMusicTimeLength(Music music) {return 0.0f;}                                         // Get music time length (in seconds)
    static float GetMusicTimePlayed(Music music) {return 0.0f;}                                        // Get current music time played (in seconds)

    // AudioStream management functions
    static AudioStream InitAudioStream(int sampleRate, int sampleSize, int channels) {return null;} // Init audio stream (to stream raw audio pcm data)
    static void UpdateAudioStream(AudioStream stream, Object data, int samplesCount) {} // Update audio stream buffers with data
    static void CloseAudioStream(AudioStream stream) {}                                      // Close audio stream and free memory
    static boolean IsAudioStreamProcessed(AudioStream stream) {return false;}              // Check if any audio stream buffers requires refill
    static void PlayAudioStream(AudioStream stream) {}                                     // Play audio stream
    void PauseAudioStream(AudioStream stream) {}                                      // Pause audio stream
    void ResumeAudioStream(AudioStream stream) {}                                    // Resume audio stream
    boolean IsAudioStreamPlaying(AudioStream stream) {return false;}                                 // Check if audio stream is playing
    void StopAudioStream(AudioStream stream) {}                                      // Stop audio stream
    void SetAudioStreamVolume(AudioStream stream, float volume) {}                 // Set volume for audio stream (1.0 is max level)
    void SetAudioStreamPitch(AudioStream stream, float pitch) {}                     // Set pitch for audio stream (1.0 is base level)
    void SetAudioStreamBufferSizeDefault(int size) {}                                 // Default size for new audio streams

}

