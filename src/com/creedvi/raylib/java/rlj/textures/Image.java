package com.creedvi.raylib.java.rlj.textures;

import com.creedvi.raylib.java.rlj.core.Color;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class Image{

    IntBuffer data;             // Image raw data
    int width;              // Image base width
    int height;             // Image base height
    int mipmaps;            // Mipmap levels, 1 by default
    int format;             // Data format (PixelFormat type)

    public Image(){
    }

    public Image(Color[] pixels, int width, int height, int pixForInt, int mipmaps){
        data = MemoryUtil.memAllocInt(pixels.length*4);
        for(int i = 0; i < pixels.length; i += 4){
            data.put(i, pixels[i].getR());
            data.put(i + 1, pixels[i].getG());
            data.put(i + 2, pixels[i].getB());
            data.put(i + 3, pixels[i].getA());
        }
        this.width = width;
        this.height = height;
        this.format = pixForInt;
        this.mipmaps = mipmaps;
    }

    public IntBuffer getData(){
        return data;
    }

    public void setData(IntBuffer data){
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
