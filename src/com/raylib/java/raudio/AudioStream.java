package com.raylib.java.raudio;

public class AudioStream{

    AudioBuffer buffer;
    int sampleRate;
    int sampleSize;
    int channels;

    public AudioStream(){
        buffer = new AudioBuffer();
    }

    public AudioStream(int sr, int ss, int c){
        sampleRate = sr;
        sampleSize = ss;
        channels = c;
        buffer = new AudioBuffer();
    }

}
