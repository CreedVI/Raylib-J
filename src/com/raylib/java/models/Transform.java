package com.raylib.java.models;

import com.raylib.java.raymath.Quaternion;
import com.raylib.java.raymath.Vector3;

public class Transform {

    Vector3 translation;    // Translation
    Quaternion rotation;    // Rotation
    Vector3 scale;          // Scale

    public Transform() {
        translation = new Vector3();
        scale = new Vector3();
        rotation = new Quaternion();
    }

}
