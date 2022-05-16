package com.raylib.java.raudioal;

import java.nio.Buffer;

public class Wave {


    class WAVRiffHeader {
        char[] chunkID;
        int chunkSize;
        char[] format;

        public WAVRiffHeader() {
            chunkID = new char[4];
            format = new char[4];
        }
    }

    class WAVFormat {
        char[] subChunkID;
        int subChunkSize;
        short audioFormat;
        short numChannels;
        int sampleRate;
        int byteRate;
        short blockAlign;
        short bitsPerSample;

        public WAVFormat() {
            subChunkID = new char[4];
        }
    }

    class WAVData {
        char[] subChunkID;
        int subChunkSize;

        public WAVData() {
            subChunkID = new char[4];
        }
    }

    int sampleCount;   // Number of samples
    int sampleRate;    // Frequency (samples per second)
    int sampleSize;    // Bit depth (bits per sample): 8, 16, 32 (24 not supported)
    int channels;      // Number of channels (1-mono, 2-stereo)
    Buffer data;                 // Buffer data pointer

}
