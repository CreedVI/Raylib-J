package com.raylib.java.structs;

/**
 * Size, two components
 */
public class Size implements Cloneable {

    /**
     * Size width
     */
    public int width;
    /**
     * Size height
     */
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

    @Override
    public Size clone() {
        try {
            Size clone = (Size) super.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
