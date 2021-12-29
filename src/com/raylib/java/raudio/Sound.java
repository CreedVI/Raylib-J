package com.raylib.java.raudio;

public class Sound{

    AudioStream stream;    // Audio Stream
    int frameCount;        // Total number of frames (considering channels)

    public Sound(){
        stream = new AudioStream();
        stream.buffer.usage = rAudio.AudioBufferUsage.AUDIO_BUFFER_USAGE_STATIC;
    }

}
