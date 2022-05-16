package com.raylib.java.raudioal;

public class Music {

    int ctxType;  // Type of music context (OGG, XM, MOD)

    long ctxOgg;                 // OGG audio context
    /*
    drflac *ctxFlac;                    // FLAC audio context
    jar_xm_context_t *ctxXm;            // XM chiptune context
    jar_mod_context_t ctxMod;           // MOD chiptune context
    */
    AudioStream stream;                 // Audio stream (double buffering)

    public int loopCount;                      // Loops count (times music repeats), -1 means infinite loop
    public int totalSamples;                   // Total number of samples
    public int samplesLeft;                    // Number of samples left to end

    // Music type (file streaming from memory)
    public Music() {

    }

}
