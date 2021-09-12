package com.raylib.audio;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

class AudioStream extends AudioInputStream {

    public AudioStream(InputStream stream, AudioFormat format, long length) {
        super(stream, format, length);
        //TODO Auto-generated constructor stub
    }
    
}
