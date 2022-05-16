package com.raylib.java.raudioal;

public class AudioStream {

    int sampleRate;    // Frequency (samples per second)
    int sampleSize;    // Bit depth (bits per sample): 8, 16, 32 (24 not supported)
    int channels;      // Number of channels (1-mono, 2-stereo)

    int format;       // OpenAL audio format specifier
    int source;       // OpenAL audio source id
    int[] buffers;    // OpenAL audio buffers (double buffering)

    public AudioStream() {
        buffers = new int[2];
    }

}
