package com.creedvi.raylib.java.rlj.rlgl.vr;

import com.creedvi.raylib.java.rlj.raymath.Matrix;
import com.creedvi.raylib.java.rlj.rlgl.shader.Shader;

public class VrStereoConfig{

    Shader distortionShader;        // VR stereo rendering distortion shader
    Matrix eyesProjection[];       // VR stereo rendering eyes projection matrices
    Matrix eyesViewOffset[];       // VR stereo rendering eyes view offset matrices
    int eyeViewportRight[];        // VR stereo rendering right eye viewport [x, y, w, h]
    int eyeViewportLeft[];         // VR stereo rendering left eye viewport [x, y, w, h]

    public Shader getDistortionShader(){
        return distortionShader;
    }

    public void setDistortionShader(Shader distortionShader){
        this.distortionShader = distortionShader;
    }

    public Matrix[] getEyesProjection(){
        return eyesProjection;
    }

    public void setEyesProjection(Matrix[] eyesProjection){
        this.eyesProjection = eyesProjection;
    }

    public Matrix[] getEyesViewOffset(){
        return eyesViewOffset;
    }

    public void setEyesViewOffset(Matrix[] eyesViewOffset){
        this.eyesViewOffset = eyesViewOffset;
    }

    public int[] getEyeViewportRight(){
        return eyeViewportRight;
    }

    public void setEyeViewportRight(int[] eyeViewportRight){
        this.eyeViewportRight = eyeViewportRight;
    }

    public int[] getEyeViewportLeft(){
        return eyeViewportLeft;
    }

    public void setEyeViewportLeft(int[] eyeViewportLeft){
        this.eyeViewportLeft = eyeViewportLeft;
    }
}
