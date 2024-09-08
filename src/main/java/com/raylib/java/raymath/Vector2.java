package com.raylib.java.raymath;

public class Vector2{

    public float x, y;

    public Vector2(){
        this.x = 0;
        this.y = 0;
    }

    public Vector2(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 v2) {
        this.x = v2.x;
        this.y = v2.y;
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

}
