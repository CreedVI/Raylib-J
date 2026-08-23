package com.raylib.java.structs;

import com.raylib.java.rlgl.RLGL.rlPixelFormat;

import java.nio.ByteBuffer;

public class Image implements Cloneable{

    public ByteBuffer data;               // Image raw data
    public int width;                     // Image base width
    public int height;                    // Image base height
    public int mipmaps;                   // Mipmap levels, 1 by default
    public rlPixelFormat format;          // Data format (rlPixelFormat type)

    public Image(){
        width = 0;
        height = 0;
        mipmaps = 0;
        format = null;
    }

    public Image(Color[] pixels, int width, int height, rlPixelFormat pixelFormat, int mipmaps){
        byte[] dataB = new byte[pixels.length*4];
        for(int i = 0, j = 0; j < pixels.length; i+=4, j++){
            dataB[i] = (byte) pixels[j].getR();
            dataB[i + 1] = (byte) pixels[j].getG();
            dataB[i + 2] = (byte) pixels[j].getB();
            dataB[i + 3] = (byte) pixels[j].getA();
        }
        this.data = ByteBuffer.allocateDirect(dataB.length);
        this.data.put(dataB);
        this.data.flip();
        this.width = width;
        this.height = height;
        this.format = pixelFormat;
        this.mipmaps = mipmaps;
    }

    public Image(byte[] data, int width, int height, rlPixelFormat pixelFormat, int mipmaps){
        this.data = ByteBuffer.allocateDirect(data.length);
        this.data.put(data);
        this.data.flip();
        this.width = width;
        this.height = height;
        this.format = pixelFormat;
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

    public void setData(ByteBuffer data) {
        this.data = data;
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
        this.data.put(data);
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

    public rlPixelFormat getFormat(){
        return format;
    }

    public void setFormat(rlPixelFormat format){
        this.format = format;
    }

    @Override
    public Image clone() {
        try {
            Image clone = (Image) super.clone();
            clone.data = data.duplicate();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
