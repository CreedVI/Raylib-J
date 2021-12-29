package com.raylib.java.raudio;

import java.util.LinkedList;

import static com.raylib.java.Config.MAX_AUDIO_BUFFER_POOL_CHANNELS;

public class AudioData{

    protected static class System{
        long context;          // OpenAL context data
        long device;           // OpenAL Audio device

        public boolean isReady;               // Check if audio device is ready
    }

    protected static class Buffer{
        public LinkedList<AudioBuffer> buffers; // List of AudioBuffers for tracking
        public int defaultSize;                 // Default audio buffer size for audio streams

        public Buffer(){
            buffers = new LinkedList<>();
        }
    }

    protected static class MultiChannel{
        public int poolCounter;         // AudioBuffer pointers pool counter
        public AudioBuffer[] pool;      // Multichannel AudioBuffer pointers pool
        public int[] channels;          // AudioBuffer pool channels

        public MultiChannel(){
            pool = new AudioBuffer[MAX_AUDIO_BUFFER_POOL_CHANNELS];
            channels = new int[MAX_AUDIO_BUFFER_POOL_CHANNELS];
        }
    }

    public System system;
    public Buffer buffer;
    public MultiChannel multiChannel;

    public AudioData(){
        system = new System();
        buffer = new Buffer();
        multiChannel = new MultiChannel();

        // NOTE: Music buffer size is defined by number of samples, independent of sample size and channels number
        // After some math, considering a sampleRate of 48000, a buffer refill rate of 1/60 seconds and a
        // standard double-buffering system, a 4096 samples buffer has been chosen, it should be enough
        // In case of music-stalls, just increase this number
        buffer.defaultSize = 0;

    }

}
