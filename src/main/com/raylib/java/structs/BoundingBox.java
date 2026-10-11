package com.raylib.java.structs;

/**
 * Bounding box
 */
public class BoundingBox implements Cloneable {

    /**
     * Minimum vertex box-corner
     */
    public Vector3 min;
    /**
     * Maximum vertex box-corner
     */
    public Vector3 max;

    public BoundingBox() {
        min = new Vector3();
        max = new Vector3();
    }

    public BoundingBox(Vector3 min, Vector3 max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public BoundingBox clone() {
        try {
            BoundingBox clone = (BoundingBox) super.clone();
            clone.min = min.clone();
            clone.max = max.clone();
            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
