package com.raylib.java.raudio;

import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

public class AudioBuffer{

    int bufferId;                      // OpenAL buffer ID
    int sourceId;                      // OpenAL source ID

    float volume;                      //Audio buffer volume
    float pitch;                       //audio buffer pitch

    boolean playing;                   //audio buffer state: AUDIO_PLAYING
    boolean paused;                    //Audio buffer state: AUDIO_PAUSED
    boolean looping;                   //Audio buffer looping, always true for AudioStreams

    int usage;                         // Audio buffer usage mode: STATIC or STREAM

    boolean[] isSubBufferProcessed;    //SubBuffer processed (virtual double buffer)
    int sizeInFrames;                  //Total buffer size in frames
    int frameCursorPos;                //Frame cursor position
    int framesProcessed;               //Total frames processed in this buffer (required for play timing)

    ShortBuffer data;                       //Data buffer, on music stream keeps filling


    public AudioBuffer(){
        this.bufferId = alGenBuffers();
        this.sourceId = alGenSources();
    }

    void bufferData(int format, int samplerate) {
        alBufferData(bufferId, format, data, samplerate);

        alSourcei(sourceId, AL_BUFFER, bufferId);
        alSourcei(sourceId, AL_LOOPING, (this.looping ? 1 : 0));
        alSourcei(sourceId, AL_POSITION, 0);
        alSourcef(sourceId, AL_GAIN, volume);
        alSourcef(sourceId, AL_FREQUENCY, pitch);
    }

}
