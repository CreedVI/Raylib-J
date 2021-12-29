package com.raylib.java.raudio;

public class Music{

    AudioStream stream;
    int frameCount;
    boolean looping;

    int ctxType;
    long ctxData;

    public Music(){
        stream = new AudioStream();
        stream.buffer.usage = rAudio.AudioBufferUsage.AUDIO_BUFFER_USAGE_STREAM;
    }

}
