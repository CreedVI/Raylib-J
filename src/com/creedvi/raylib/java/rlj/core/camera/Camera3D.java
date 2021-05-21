package com.creedvi.raylib.java.rlj.core.camera;

import com.creedvi.raylib.java.rlj.raymath.Vector3;

public class Camera3D{

    Vector3 position;       // Camera position
    Vector3 target;         // Camera target it looks-at
    Vector3 up;             // Camera up vector (rotation over its axis)
    float fovy;             // Camera field-of-view apperture in Y (degrees) in perspective, used as near plane width in orthographic
    int type;               // Camera type, defines projection type: CAMERA_PERSPECTIVE or CAMERA_ORTHOGRAPHIC

    public Camera3D(){

    }

    public Camera3D(Vector3 position, Vector3 target, Vector3 up, Float fovy, int type){
        this.position = position;
        this.target = target;
        this.up = up;
        this.fovy = fovy;
        this.type = type;
    }

    public float getFovy(){
        return fovy;
    }

    public int getType(){
        return type;
    }

    public Vector3 getPosition(){
        return position;
    }

    public Vector3 getTarget(){
        return target;
    }

    public Vector3 getUp(){
        return up;
    }

    public void setTarget(Vector3 target){
        this.target = target;
    }

    public void setFovy(float fovy){
        this.fovy = fovy;
    }

    public void setPosition(Vector3 position){
        this.position = position;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setUp(Vector3 up){
        this.up = up;
    }

}
