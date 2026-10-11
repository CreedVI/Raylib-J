package com.raylib.java.structs;

import com.raylib.java.rlgl.RLGL;

import static com.raylib.java.rlgl.RLGL.rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

/**
 * Texture, tex data stored in GPU memory (VRAM)
 */
public class Texture implements Cloneable {

    /**
     * OpenGL texture id
     */
    public int id;
    /**
     * Texture base width
     */
    public int width;
    /**
     * Texture base height
     */
    public int height;
    /**
     * Mipmap levels, 1 by default
     */
    public int mipmaps;
    /**
     * Data format (rlPixelFormat type)
     */
    public RLGL.rlPixelFormat format;

    public Texture(){
        this.width = 1;
        this.height = 1;
        this.mipmaps = 1;
        this.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
    }

    public Texture(int id, int width, int height, int mipmaps, RLGL.rlPixelFormat format){
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

    public RLGL.rlPixelFormat getFormat(){
        return format;
    }

    public void setFormat(RLGL.rlPixelFormat format){
        this.format = format;
    }

    @Override
    public Texture clone() {
        try {
            Texture clone = (Texture) super.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
