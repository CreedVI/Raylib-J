package com.raylib.java.structs;

import com.raylib.java.rlgl.RLGL;

/**
 * TextureCubemap, Texture alias
 */
public class TextureCubemap extends Texture {

    public TextureCubemap() {
        super();
    }

    public TextureCubemap(int id, int width, int height, int mipmaps, RLGL.rlPixelFormat format) {
        super(id, width, height, mipmaps, format);
    }

    @Override
    public TextureCubemap clone() {
        return (TextureCubemap) super.clone();
    }
}
