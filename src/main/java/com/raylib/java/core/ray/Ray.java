package com.raylib.java.core.ray;

import com.raylib.java.raymath.Vector3;

public class Ray{

    public Vector3 position;       // Ray position (origin)
    public Vector3 direction;      // Ray direction

    public Ray(){
        position = new Vector3();
        direction = new Vector3();
    }

    public Ray(Vector3 position, Vector3 direction){
        this.position = position;
        this.direction = direction;
    }

    public Vector3 getPosition(){
        return position;
    }

    public void setPosition(Vector3 position){
        this.position = position;
    }

    public Vector3 getDirection(){
        return direction;
    }

    public void setDirection(Vector3 direction){
        this.direction = direction;
    }
}
