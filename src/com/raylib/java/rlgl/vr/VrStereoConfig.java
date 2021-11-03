package com.raylib.java.rlgl.vr;

import com.raylib.java.raymath.Matrix;

public class VrStereoConfig{

    public Matrix[] projection;           // VR projection matrices (per eye)
    public Matrix[] viewOffset;           // VR view offset matrices (per eye)
    public float[] leftLensCenter;        // VR left lens center
    public float[] rightLensCenter;       // VR right lens center
    public float[] leftScreenCenter;      // VR left screen center
    public float[] rightScreenCenter;     // VR right screen center
    public float[] scale;                 // VR distortion scale
    public float[] scaleIn;               // VR distortion scale in

    public VrStereoConfig(){
        projection = new Matrix[2];
        viewOffset = new Matrix[2];
        leftLensCenter = new float[2];
        rightLensCenter = new float[2];
        leftScreenCenter = new float[2];
        rightScreenCenter = new float[2];
        scale = new float[2];
        scaleIn = new float[2];
    }

    public Matrix[] getProjection(){
        return projection;
    }

    public void setProjection(Matrix[] projection){
        this.projection = projection;
    }

    public Matrix[] getViewOffset(){
        return viewOffset;
    }

    public void setViewOffset(Matrix[] viewOffset){
        this.viewOffset = viewOffset;
    }

    public float[] getLeftLensCenter(){
        return leftLensCenter;
    }

    public void setLeftLensCenter(float[] leftLensCenter){
        this.leftLensCenter = leftLensCenter;
    }

    public float[] getRightLensCenter(){
        return rightLensCenter;
    }

    public void setRightLensCenter(float[] rightLensCenter){
        this.rightLensCenter = rightLensCenter;
    }

    public float[] getLeftScreenCenter(){
        return leftScreenCenter;
    }

    public void setLeftScreenCenter(float[] leftScreenCenter){
        this.leftScreenCenter = leftScreenCenter;
    }

    public float[] getRightScreenCenter(){
        return rightScreenCenter;
    }

    public void setRightScreenCenter(float[] rightScreenCenter){
        this.rightScreenCenter = rightScreenCenter;
    }

    public float[] getScale(){
        return scale;
    }

    public void setScale(float[] scale){
        this.scale = scale;
    }

    public float[] getScaleIn(){
        return scaleIn;
    }

    public void setScaleIn(float[] scaleIn){
        this.scaleIn = scaleIn;
    }
}
