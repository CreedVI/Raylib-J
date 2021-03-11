package com.creedvi.raylib.java.rlj.textures;

public class Image{

    int[] data;             // Image raw data
    int width;              // Image base width
    int height;             // Image base height
    int mipmaps;            // Mipmap levels, 1 by default
    int format;             // Data format (PixelFormat type)

    public Image(){
    }

    public int[] getData(){
        return data;
    }

    public void setData(int[] data){
        this.data = data;
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

    public int getMipmaps(){
        return mipmaps;
    }

    public void setMipmaps(int mipmaps){
        this.mipmaps = mipmaps;
    }

    public int getFormat(){
        return format;
    }

    public void setFormat(int format){
        this.format = format;
    }

}
