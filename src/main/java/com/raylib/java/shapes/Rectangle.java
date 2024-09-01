package com.raylib.java.shapes;

import com.raylib.java.raymath.Vector2;

public class Rectangle{

    public float x;
    public float y;
    public float width;
    public float height;

    public Rectangle(){
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public Rectangle(float x, float y, float w, float h){
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public Rectangle(Vector2 pos, float w, float h){
        this.x = pos.getX();
        this.y = pos.getY();
        this.width = w;
        this.height = h;
    }

    public Rectangle(Rectangle r){
        this.x = r.x;
        this.y = r.y;
        this.width = r.width;
        this.height = r.height;
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

    public float getWidth(){
        return width;
    }

    public void setWidth(float width){
        this.width = width;
    }

    public float getHeight(){
        return height;
    }

    public void setHeight(float height){
        this.height = height;
    }
}
