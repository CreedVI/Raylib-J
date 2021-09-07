package com.creedvi.raylib.java.rlj.textures;

import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.creedvi.raylib.java.rlj.textures.Textures.LoadTexture;
import static com.creedvi.raylib.java.rlj.textures.Textures.LoadTextureFromImage;

public class Texture2D{

    public int id;                 // OpenGL texture id
    public int width;              // Texture base width
    public int height;             // Texture base height
    int mipmaps;            // Mipmap levels, 1 by default
    int format;             // Data format (PixelFormat type)

    public Texture2D(){
        this.width = 1;
        this.height = 1;
        this.mipmaps = 1;
        this.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
    }

    public Texture2D(Image image){
        this.id = LoadTextureFromImage(image).id;
        this.width = LoadTextureFromImage(image).width;
        this.height = LoadTextureFromImage(image).height;
        this.mipmaps = LoadTextureFromImage(image).mipmaps;
        this.format = LoadTextureFromImage(image).format;
    }

    public Texture2D(String filepath){
        Texture2D t = LoadTexture(filepath);
        this.id = t.id;
        this.width = t.width;
        this.height = t.height;
        this.mipmaps = t.mipmaps;
        this.format = t.format;
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
