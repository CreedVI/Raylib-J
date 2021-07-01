package com.creedvi.raylib.java.rlj.core.camera;

import com.creedvi.raylib.java.rlj.raymath.Vector3;

public class Camera2D extends Camera{

    public Camera2D(){
    }

    public Camera2D(Vector3 offset, Vector3 target, float rotation, float zoom){
        this.offset = offset;
        this.target = target;
        this.rotation = rotation;
        this.zoom = zoom;
    }

    @Override
    public void update(){
    }
}
