package com.raylib.java.structs;

public class Vector2 implements Cloneable {

    public float x, y;

    public Vector2(){
        this.x = 0;
        this.y = 0;
    }

    public Vector2(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Vector2(double x, double y){
        this.x = (float) x;
        this.y = (float) y;
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

    @Override
    public Vector2 clone() {
        try {
            Vector2 clone = (Vector2) super.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
