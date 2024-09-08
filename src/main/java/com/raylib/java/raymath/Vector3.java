package com.raylib.java.raymath;

public class Vector3{
    public float x, y, z;

    public Vector3(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v3) {
        this.x = v3.x;
        this.y = v3.y;
        this.z = v3.z;
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
}
