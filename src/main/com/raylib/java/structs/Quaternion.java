package com.raylib.java.structs;

/**
 * Quaternion, 4 components (Vector4 alias)
 */
public class Quaternion extends Vector4 {

    public Quaternion() {
        super();
    }

    public Quaternion(float x, float y, float z, float w) {
        super(x, y, z, w);
    }

    @Override
    public Quaternion clone() {
        return (Quaternion) super.clone();
    }
}
