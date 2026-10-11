package com.raylib.java.structs;

/**
 * RenderTexture, fbo for texture rendering
 */
public class RenderTexture implements Cloneable {

    /**
     * OpenGL framebuffer object id
     */
    public int id;
    /**
     * Color buffer attachment texture
     */
    public Texture2D texture;
    /**
     * Depth buffer attachment texture
     */
    public Texture2D depth;

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

    @Override
    public RenderTexture clone() {
        try {
            RenderTexture clone = (RenderTexture) super.clone();

            clone.texture = texture.clone();
            clone.depth = depth.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
