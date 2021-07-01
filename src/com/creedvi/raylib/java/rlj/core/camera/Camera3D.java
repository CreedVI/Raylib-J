package com.creedvi.raylib.java.rlj.core.camera;

import com.creedvi.raylib.java.rlj.raymath.Vector3;

public class Camera3D extends Camera{

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

    @Override
    public void update(){

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
