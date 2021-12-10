package com.raylib.java.core.camera;

import com.raylib.java.raymath.Vector3;

public class Camera3D extends rCamera{


    public Vector3 position;                // rCamera position
    public Vector3 up;                      // rCamera up vector (rotation over its axis)
    public Vector3 target;
    public float fovy;                      // rCamera field-of-view apperture in Y (degrees) in perspective, used as near plane width in orthographic
    public int projection;                  // rCamera type, defines projection type: CAMERA_PERSPECTIVE or
                                            // CAMERA_ORTHOGRAPHIC
    CameraData cameraData;

    public Camera3D(){
        position = new Vector3();
        target = new Vector3();
        up = new Vector3();
        cameraData = new CameraData();
    }

    public Camera3D(CameraData cameraData){
        position = new Vector3();
        target = new Vector3();
        up = new Vector3();
        this.cameraData = cameraData;
    }

    public Camera3D(Vector3 position, Vector3 target, Vector3 up, Float fovy, int type,
                    CameraData cameraData){
        this.position = position;
        this.target = target;
        this.up = up;
        this.fovy = fovy;
        this.projection = type;
        this.cameraData = cameraData;
    }

    public float getFovy(){
        return fovy;
    }

    public int getType(){
        return projection;
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
        this.projection = type;
    }

    public void setUp(Vector3 up){
        this.up = up;
    }

}
