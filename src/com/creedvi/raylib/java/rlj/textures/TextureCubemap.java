package com.creedvi.raylib.java.rlj.textures;

import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.creedvi.raylib.java.rlj.textures.Textures.LoadTexture;
import static com.creedvi.raylib.java.rlj.textures.Textures.LoadTextureFromImage;

public class TextureCubemap{

    int id;                 // OpenGL texture id
    int width;              // Texture base width
    int height;             // Texture base height
    int mipmaps;            // Mipmap levels, 1 by default
    int format;             // Data format (PixelFormat type)

    public TextureCubemap(){
        this.width = 1;
        this.height = 1;
        this.mipmaps = 1;
        this.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
    }

    public TextureCubemap(Image image){
        this.id = LoadTextureFromImage(image).id;
        this.width = LoadTextureFromImage(image).width;
        this.height = LoadTextureFromImage(image).height;
        this.mipmaps = LoadTextureFromImage(image).mipmaps;
        this.format = LoadTextureFromImage(image).format;
    }

    public TextureCubemap(String filepath){
        this.id = LoadTexture(filepath).id;
        this.width = LoadTexture(filepath).width;
        this.height = LoadTexture(filepath).height;
        this.mipmaps = LoadTexture(filepath).mipmaps;
        this.format = LoadTexture(filepath).format;
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
