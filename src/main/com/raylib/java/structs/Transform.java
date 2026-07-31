package com.raylib.java.structs;

public class Transform implements Cloneable {

    public Vector3 translation;    // Translation
    public Quaternion rotation;    // Rotation
    public Vector3 scale;          // Scale

    public Transform() {
        translation = new Vector3();
        scale = new Vector3();
        rotation = new Quaternion();
    }

    public Transform(Vector3 translation, Quaternion rotation, Vector3 scale) {
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
    }

    @Override
    public Transform clone() {
        try {
            Transform clone = (Transform) super.clone();

            clone.translation = translation.clone();
            clone.rotation = rotation.clone();
            clone.scale = scale.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
