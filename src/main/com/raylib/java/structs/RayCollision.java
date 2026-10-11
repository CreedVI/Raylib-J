package com.raylib.java.structs;

/**
 * RayCollision, ray hit information
 */
public class RayCollision implements Cloneable {

    /**
     * Did the ray hit something?
     */
    public boolean hit;
    /**
     * Distance to nearest hit
     */
    public float distance;
    /**
     * Position of nearest hit
     */
    public Vector3 point;
    /**
     * Surface normal of hit
     */
    public Vector3 normal;

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

    @Override
    public RayCollision clone() {
        try {
            RayCollision clone = (RayCollision) super.clone();

            clone.point = point.clone();
            clone.normal = normal.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
