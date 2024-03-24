package com.raylib.java.rlgl.vr;

public class VrDeviceInfo{

    // Head-Mounted-Display device parameters

    public int hResolution;                // Horizontal resolution in pixels
    public int vResolution;                // Vertical resolution in pixels
    public float hScreenSize;              // Horizontal size in meters
    public float vScreenSize;              // Vertical size in meters
    public float vScreenCenter;            // Screen center in meters
    public float eyeToScreenDistance;      // Distance between eye and display in meters
    public float lensSeparationDistance;   // Lens separation distance in meters
    public float interpupillaryDistance;   // IPD (distance between pupils) in meters
    public float[] lensDistortionValues;  // Lens distortion constant parameters
    public float[] chromaAbCorrection;    // Chromatic aberration correction parameters

    public VrDeviceInfo(){
        lensDistortionValues = new float[4];
        chromaAbCorrection = new float[4];
    }

    public int gethResolution(){
        return hResolution;
    }

    public void sethResolution(int hResolution){
        this.hResolution = hResolution;
    }

    public int getvResolution(){
        return vResolution;
    }

    public void setvResolution(int vResolution){
        this.vResolution = vResolution;
    }

    public float gethScreenSize(){
        return hScreenSize;
    }

    public void sethScreenSize(float hScreenSize){
        this.hScreenSize = hScreenSize;
    }

    public float getvScreenSize(){
        return vScreenSize;
    }

    public void setvScreenSize(float vScreenSize){
        this.vScreenSize = vScreenSize;
    }

    public float getvScreenCenter(){
        return vScreenCenter;
    }

    public void setvScreenCenter(float vScreenCenter){
        this.vScreenCenter = vScreenCenter;
    }

    public float getEyeToScreenDistance(){
        return eyeToScreenDistance;
    }

    public void setEyeToScreenDistance(float eyeToScreenDistance){
        this.eyeToScreenDistance = eyeToScreenDistance;
    }

    public float getLensSeparationDistance(){
        return lensSeparationDistance;
    }

    public void setLensSeparationDistance(float lensSeparationDistance){
        this.lensSeparationDistance = lensSeparationDistance;
    }

    public float getInterpupillaryDistance(){
        return interpupillaryDistance;
    }

    public void setInterpupillaryDistance(float interpupillaryDistance){
        this.interpupillaryDistance = interpupillaryDistance;
    }

    public float[] getLensDistortionValues(){
        return lensDistortionValues;
    }

    public void setLensDistortionValues(float[] lensDistortionValues){
        this.lensDistortionValues = lensDistortionValues;
    }

    public float[] getChromaAbCorrection(){
        return chromaAbCorrection;
    }

    public void setChromaAbCorrection(float[] chromaAbCorrection){
        this.chromaAbCorrection = chromaAbCorrection;
    }
}
