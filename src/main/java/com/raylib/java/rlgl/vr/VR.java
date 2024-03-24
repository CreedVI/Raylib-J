package com.raylib.java.rlgl.vr;

public class VR {
    VrStereoConfig config;              // VR stereo configuration for simulator
    int stereoFboId;           // VR stereo rendering framebuffer id
    int stereoTexId;           // VR stereo color texture (attached to framebuffer)
    boolean simulatorReady;                // VR simulator ready flag
    boolean stereoRender;                  // VR stereo rendering enabled/disabled flag

    public VR(){

    }

    public VrStereoConfig getConfig(){
        return config;
    }

    public void setConfig(VrStereoConfig config){
        this.config = config;
    }

    public int getStereoFboId(){
        return stereoFboId;
    }

    public void setStereoFboId(int stereoFboId){
        this.stereoFboId = stereoFboId;
    }

    public int getStereoTexId(){
        return stereoTexId;
    }

    public void setStereoTexId(int stereoTexId){
        this.stereoTexId = stereoTexId;
    }

    public boolean isSimulatorReady(){
        return simulatorReady;
    }

    public void setSimulatorReady(boolean simulatorReady){
        this.simulatorReady = simulatorReady;
    }

    public boolean isStereoRender(){
        return stereoRender;
    }

    public void setStereoRender(boolean stereoRender){
        this.stereoRender = stereoRender;
    }
}