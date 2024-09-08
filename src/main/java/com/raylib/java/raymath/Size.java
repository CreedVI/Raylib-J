package com.raylib.java.raymath;

public class Size{

    public int width;
    public int height;

    public Size(){
        this.width = 0;
        this.height = 0;
    }

    public Size(int width, int height){
        this.width = width;
        this.height = height;
    }

    public Size(Size size){
        this.width = size.width;
        this.height = size.height;
    }

    public int getWidth(){
        return width;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public int getHeight(){
        return height;
    }

    public void setHeight(int height){
        this.height = height;
    }
}
