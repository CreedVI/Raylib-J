package com.raylib.core.camera;

import com.raylib.raymath.Vector2;

public class Camera2D extends Camera{

    public Vector2 target;          // Camera target (rotation and zoom origin)
    public Vector2 offset;          // Camera offset (displacement from target)
    public float rotation;         // Camera rotation in degrees
    public float zoom;             // Camera zoom (scaling), should be 1.0f by default

    public Camera2D(){
        target = new Vector2();
        offset = new Vector2();
    }

    public Camera2D(Vector2 offset, Vector2 target, float rotation, float zoom){
        this.offset = offset;
        this.target = target;
        this.rotation = rotation;
        this.zoom = zoom;
    }

    @Override
    public void update(){
    }

    public Vector2 getTarget(){
        return target;
    }

    public void setTarget(Vector2 target){
        this.target = target;
    }

    public Vector2 getOffset(){
        return offset;
    }

    public void setOffset(Vector2 offset){
        this.offset = offset;
    }

    public float getRotation(){
        return rotation;
    }

    public void setRotation(float rotation){
        this.rotation = rotation;
    }

    public float getZoom(){
        return zoom;
    }

    public void setZoom(float zoom){
        this.zoom = zoom;
    }
}
