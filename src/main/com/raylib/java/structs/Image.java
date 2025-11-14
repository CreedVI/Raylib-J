package com.raylib.java.structs;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.nio.ByteBuffer;

import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R32;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

public class Image{

    public ByteBuffer data;               // Image raw data
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
        this.data = ByteBuffer.allocate(dataB.length);
        this.data.put(dataB);
        this.data.flip();
        this.width = width;
        this.height = height;
        this.format = pixForInt;
        this.mipmaps = mipmaps;
    }

    public Image(byte[] data, int width, int height, int pixForInt, int mipmaps){
        this.data = ByteBuffer.allocate(data.length);
        this.data.put(data);
        this.data.flip();
        this.width = width;
        this.height = height;
        this.format = pixForInt;
        this.mipmaps = mipmaps;
    }

    public byte[] getData(){
        byte[] array = new byte[data.capacity()];
        for (int i = 0; i < array.length; i++) {
            array[i] = data.get();
        }
        data.flip();
        return array;
    }

    public void setData(Color[] data){
        byte[] dataB = new byte[data.length*4];
        int i = 0;
        for (Color color: data) {
            dataB[i] = (byte) color.r;
            dataB[i + 1] = (byte) color.g;
            dataB[i + 2] = (byte) color.b;
            dataB[i + 3] = (byte) color.a;
            i += 4;
        }
        if (this.data != null) {
            this.data.clear();
        }
        this.data = ByteBuffer.allocate(dataB.length);
        this.data.put(dataB);
        this.data.flip();
    }
    public void setData(byte[] data){
        if (this.data != null) {
            this.data.clear();
        }
        this.data = ByteBuffer.allocateDirect(data.length);
        this.data.put(data.clone());
        this.data.flip();
    }

    public void setData(short[] data){
        if (this.data != null) {
            this.data.clear();
        }
        this.data = ByteBuffer.allocateDirect(data.length * Short.BYTES);
        for (short datum : data) {
            this.data.putShort(datum);
        }
        this.data.flip();
    }

    public void setData(int[] data){
        if (this.data != null) {
            this.data.clear();
        }
        this.data = ByteBuffer.allocateDirect(data.length * Integer.BYTES);
        for (int datum : data) {
            this.data.putInt(datum);
        }
        this.data.flip();
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
