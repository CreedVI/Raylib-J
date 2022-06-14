package com.raylib.java.models;

import com.raylib.java.raymath.Vector3;

public class BoundingBox {

    public Vector3 min;
    public Vector3 max;

    public BoundingBox() {
        min = new Vector3();
        max = new Vector3();
    }

    public BoundingBox(Vector3 min, Vector3 max) {
        this.min = min;
        this.max = max;
    }

}
