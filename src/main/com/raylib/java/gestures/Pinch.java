package com.raylib.java.gestures;

import com.raylib.java.structs.Vector2;

public class Pinch {

    /**
     * PINCH vector (between first and second touch points)
     */
    Vector2 vector;

    /**
     * PINCH angle (relative to x-axis)
     */
    float angle;

    /**
     * PINCH displacement distance (normalized [0..1])
     */
    float distance;

    public Pinch() {
        this.vector = new Vector2();
        this.angle = 0.0f;
        this.distance = 0.0f;
    }

}
