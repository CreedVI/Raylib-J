package com.raylib.java.structs;

public class Transform {

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
}
