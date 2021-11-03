package com.raylib.java.textures;

public class RenderTexture{

    int id;
    public Texture2D texture;
    Texture2D depth;

    public RenderTexture(){
        id = 0;
        texture = new Texture2D();
        depth = new Texture2D();
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public Texture2D getTexture(){
        return texture;
    }

    public void setTexture(Texture2D texture){
        this.texture = texture;
    }

    public Texture2D getDepth(){
        return depth;
    }

    public void setDepth(Texture2D depth){
        this.depth = depth;
    }
}
