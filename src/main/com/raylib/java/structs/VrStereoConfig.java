package com.raylib.java.structs;

public class VrStereoConfig implements Cloneable {

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

    @Override
    public VrStereoConfig clone() {
        try {
            VrStereoConfig clone = (VrStereoConfig) super.clone();

            clone.projection = new Matrix[projection.length];
            System.arraycopy(projection, 0, clone.projection, 0, projection.length);
            clone.viewOffset = new Matrix[viewOffset.length];
            System.arraycopy(viewOffset, 0, clone.viewOffset, 0, viewOffset.length);
            clone.leftLensCenter = new float[leftLensCenter.length];
            System.arraycopy(leftLensCenter, 0, leftLensCenter, 0, leftLensCenter.length);
            clone.rightLensCenter = new float[rightLensCenter.length];
            System.arraycopy(rightLensCenter, 0, clone.rightLensCenter, 0, rightLensCenter.length);
            clone.leftScreenCenter = new float[leftScreenCenter.length];
            System.arraycopy(leftScreenCenter, 0, clone.leftScreenCenter, 0, leftScreenCenter.length);
            clone.rightScreenCenter = new float[rightScreenCenter.length];
            System.arraycopy(rightScreenCenter, 0, clone.rightScreenCenter, 0, rightScreenCenter.length);
            clone.scale = new float[scale.length];
            System.arraycopy(scale, 0, clone.scale, 0, scale.length);
            clone.scaleIn = new float[scaleIn.length];
            System.arraycopy(scaleIn, 0, clone.scaleIn, 0, scaleIn.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
