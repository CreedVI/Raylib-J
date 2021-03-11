package com.creedvi.raylib.java.rlj.core.camera;

import com.creedvi.raylib.java.rlj.raymath.Vector2;

public class Camera2D{

    Vector2 offset;         // Camera offset (displacement from target)
    Vector2 target;         // Camera target (rotation and zoom origin)
    float rotation;         // Camera rotation in degrees
    float zoom;             // Camera zoom (scaling), should be 1.0f by default

    public Camera2D(){

    }

    public Camera2D(Vector2 offset, Vector2 target, float rotation, float zoom){
        this.offset = offset;
        this.target = target;
        this.rotation = rotation;
        this.zoom = zoom;
    }

    public Vector2 getOffset(){
        return offset;
    }

    public void setOffset(Vector2 offset){
        this.offset = offset;
    }

    public Vector2 getTarget(){
        return target;
    }

    public void setTarget(Vector2 target){
        this.target = target;
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
