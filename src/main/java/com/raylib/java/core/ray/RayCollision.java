package com.raylib.java.core.ray;

import com.raylib.java.raymath.Vector3;

public class RayCollision{

    public boolean hit;            // Did the ray hit something?
    public float distance;         // Distance to nearest hit
    public Vector3 point;       // Position of nearest hit
    public Vector3 normal;         // Surface normal of hit

    public RayCollision(){
        point = new Vector3();
        normal = new Vector3();
    }

    public boolean isHit(){
        return hit;
    }

    public void setHit(boolean hit){
        this.hit = hit;
    }

    public float getDistance(){
        return distance;
    }

    public void setDistance(float distance){
        this.distance = distance;
    }

    public Vector3 getPoint(){
        return point;
    }

    public void setPoint(Vector3 point){
        this.point = point;
    }

    public Vector3 getNormal(){
        return normal;
    }

    public void setNormal(Vector3 normal){
        this.normal = normal;
    }
}
