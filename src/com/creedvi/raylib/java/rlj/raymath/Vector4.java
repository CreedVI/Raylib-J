package com.creedvi.raylib.java.rlj.raymath;

public class Vector4{
    public float x, y, z, w;

    public Vector4(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    public Vector4(float x, float y, float z, float w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float getX(){
        return x;
    }

    public void setX(float x){
        this.x = x;
    }

    public float getY(){
        return y;
    }

    public void setY(float y){
        this.y = y;
    }

    public float getZ(){
        return z;
    }

    public void setZ(float z){
        this.z = z;
    }

    public float getW(){
        return w;
    }

    public void setW(float w){
        this.w = w;
    }
}
