package com.raylib.java.structs;

public class MaterialMap implements Cloneable{

    public Texture2D texture;      // Material map texture
    public Color color;            // Material map color
    public float value;            // Material map value

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
