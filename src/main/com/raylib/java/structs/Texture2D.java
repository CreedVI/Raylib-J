package com.raylib.java.structs;

import com.raylib.java.rlgl.RLGL;

/**
 * Texture2D, Texture alias
 */
public class Texture2D extends Texture {

    public Texture2D() {
        super();
    }

    public Texture2D(int id, int width, int height, int mipmaps, RLGL.rlPixelFormat format) {
        super(id, width, height, mipmaps, format);
    }

    @Override
    public Texture2D clone() {
        return (Texture2D) super.clone();
    }
}
