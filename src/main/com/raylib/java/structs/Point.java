package com.raylib.java.structs;

public class Point implements Cloneable {

    public float x;
    public float y;

    public Point(){
        this.x = 0;
        this.y = 0;
    }

    public Point(float x, float y){
        this.x = x;
        this.y = y;
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
    public Point clone() {
        try {
            Point clone = (Point) super.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
