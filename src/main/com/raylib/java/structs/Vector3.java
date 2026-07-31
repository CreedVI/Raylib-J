package com.raylib.java.structs;

public class Vector3 implements Cloneable {
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

    public Vector3(double x, double y, double z){
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
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

    @Override
    public Vector3 clone() {
        try {
            Vector3 clone = (Vector3) super.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
