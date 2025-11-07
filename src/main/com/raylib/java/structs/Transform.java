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

}
