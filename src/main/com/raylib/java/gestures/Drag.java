package com.raylib.java.gestures;

import com.raylib.java.structs.Vector2;

public class Drag {

    /**
     * DRAG vector (between initial and current position)
     */
    Vector2 vector;

    /**
     * DRAG angle (relative to x-axis)
     */
    float angle;

    /**
     * Normalised DRAG distance from initial touch point to final.
     */
    float distance;

    /**
     * DRAG intensity, how far why did the DRAG (pixels per frame)
     */
    float intensity;

    public Drag() {
        this.vector = new Vector2();
        this.angle = 0.0f;
        this.distance = 0.0f;
        this.intensity = 0.0f;
    }

}
