package com.raylib.java.core.rcamera;

import com.raylib.java.raymath.Vector2;

public class Camera2D {

    public Vector2 target;          // rCamera target (rotation and zoom origin)
    public Vector2 offset;          // rCamera offset (displacement from target)
    public float rotation;         // rCamera rotation in degrees
    public float zoom;             // rCamera zoom (scaling), should be 1.0f by default

    public Camera2D() {
        target = new Vector2();
        offset = new Vector2();
        rotation = 0.0f;
        zoom = 1.0f;
    }

    public Camera2D(Vector2 offset, Vector2 target, float rotation, float zoom) {
        this.offset = offset;
        this.target = target;
        this.rotation = rotation;
        this.zoom = zoom;
    }

    public Vector2 getTarget() {
        return target;
    }

    public void setTarget(Vector2 target) {
        this.target = target;
    }

    public Vector2 getOffset() {
        return offset;
    }

    public void setOffset(Vector2 offset) {
        this.offset = offset;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
}
