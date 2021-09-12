package com.raylib.audio;

public class AudioDevice {

    private static AudioDevice audioDevice = null;

    private AudioDevice() {

    }

    public static AudioDevice getInstance() {
        if (audioDevice == null) {
            audioDevice = new AudioDevice();
            

        }
        return audioDevice;
    }

    public void destruct() {
        audioDevice = null;
    }

    private void init() {

    }

    public boolean ready() {
        return audioDevice != null;
    }

    public void setMasterVolume(float volume) {
    
    }

}
