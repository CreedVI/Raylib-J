package com.raylib.java.textures;

import com.raylib.java.core.Color;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;

public class Image{

    protected DataBuffer data;               // Image raw data
    public int width;                        // Image base width
    public int height;                       // Image base height
    public int mipmaps;                      // Mipmap levels, 1 by default
    public int format;                       // Data format (rlPixelFormat type)

    public Image(){
        width = 0;
        height = 0;
        mipmaps = 0;
        format = 0;
    }

    public Image(Color[] pixels, int width, int height, int pixForInt, int mipmaps){
        byte[] dataB = new byte[pixels.length*4];
        for(int i = 0, j = 0; j < pixels.length; i+=4, j++){
            dataB[i] = (byte) pixels[j].getR();
            dataB[i + 1] = (byte) pixels[j].getG();
            dataB[i + 2] = (byte) pixels[j].getB();
            dataB[i + 3] = (byte) pixels[j].getA();
        }
        data = new DataBufferByte(dataB,dataB.length);
        this.width = width;
        this.height = height;
        this.format = pixForInt;
        this.mipmaps = mipmaps;
    }

    public Image(byte[] data, int width, int height, int pixForInt, int mipmaps){
        this.data = new DataBufferByte(data,data.length);
        this.width = width;
        this.height = height;
        this.format = pixForInt;
        this.mipmaps = mipmaps;
    }

    public byte[] getData(){
        byte[] dataB = new byte[data.getSize()];
        for(int s = 0;s < dataB.length; s++){
            dataB[s] = (byte) data.getElem(s);
        }
        return dataB;
    }

    public void setData(short[] data){
        this.data = new DataBufferShort(data, data.length);
    }
    public void setData(Color[] data){
        byte[] dataB = new byte[data.length*4];
        int g=0;
        for (int i = 0; i < data.length; i++){
            dataB[g] = (byte) data[i].r;
            dataB[g+1] = (byte) data[i].g;
            dataB[g+2] = (byte) data[i].b;
            dataB[g+3] = (byte) data[i].a;
            g+=4;
        }
        this.data = new DataBufferByte(dataB, dataB.length);
    }
    public void setData(byte[] data){
        this.data = new DataBufferByte(data, data.length);
    }
    public void setData(int[] data){
        this.data = new DataBufferInt(data, data.length);
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
