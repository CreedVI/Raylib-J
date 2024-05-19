package com.raylib.java.textures;

import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

public class Texture2D{

    public int id;                 // OpenGL texture id
    public int width;              // Texture base width
    public int height;             // Texture base height
    int mipmaps;            // Mipmap levels, 1 by default
    public int format;             // Data format (rlPixelFormat type)

    public Texture2D(){
        this.width = 1;
        this.height = 1;
        this.mipmaps = 1;
        this.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
    }

    public Texture2D(int id, int width, int height, int mipmaps, int format){
        this.id = id;
        this.width = width;
        this.height = height;
        this.mipmaps = mipmaps;
        this.format = format;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
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
