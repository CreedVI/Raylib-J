package com.raylib.java.structs;

import static com.raylib.java.rlgl.RLGL.rlPixelFormat;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

public class TextureCubemap{

    public int id;                 // OpenGL texture id
    public int width;              // Texture base width
    public int height;             // Texture base height
    public int mipmaps;            // Mipmap levels, 1 by default
    public rlPixelFormat format;             // Data format (rlPixelFormat type)

    public TextureCubemap(){
        this.width = 1;
        this.height = 1;
        this.mipmaps = 1;
        this.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
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

    public rlPixelFormat getFormat() {
        return format;
    }

    public void setFormat(rlPixelFormat format) {
        this.format = format;
    }

}
