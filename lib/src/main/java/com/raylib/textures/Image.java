package com.raylib.textures;

import com.raylib.core.Color;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;

public class Image{

    DataBuffer data;            // Image raw data
    int width;                        // Image base width
    int height;                       // Image base height
    int mipmaps;                      // Mipmap levels, 1 by default
    int format;                       // Data format (PixelFormat type)

    public Image(){
    }

    public Image(Color[] pixels, int width, int height, int pixForInt, int mipmaps){
        byte[] dataB = new byte[pixels.length*4];
        for(int i = 0; i < pixels.length; i += 4){
            dataB[i] = (byte) pixels[i].getR();
            dataB[i + 1] = (byte) pixels[i].getG();
            dataB[i + 2] = (byte) pixels[i].getB();
            dataB[i + 3] = (byte) pixels[i].getA();
        }
        data = new DataBufferByte(dataB,dataB.length);
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
