package com.raylib.java.structs;

/**
 * MaterialMap
 */
public class MaterialMap implements Cloneable{

    /**
     * Material map texture
     */
    public Texture2D texture;
    /**
     * Material map color
     */
    public Color color;
    /**
     * Material map value
     */
    public float value;

    public MaterialMap(){
        texture = new Texture2D();
        color = new Color();
        value = 0f;
    }

    @Override
    public MaterialMap clone() {
        try {
            MaterialMap clone = (MaterialMap) super.clone();
            clone.texture = texture.clone();
            clone.color = color.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
