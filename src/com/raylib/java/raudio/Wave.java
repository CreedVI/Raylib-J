package com.raylib.java.raudio;

import java.nio.Buffer;

public class Wave{

    int frameCount;    // Total number of frames (considering channels)
    int sampleRate;    // Frequency (samples per second)
    int sampleSize;    // Bit depth (bits per sample): 8, 16, 32 (24 not supported)
    int channels;      // Number of channels (1-mono, 2-stereo, ...)
    Buffer data;       // Buffer data

    public Wave(){

    }

}
