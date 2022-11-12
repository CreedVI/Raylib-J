package com.raylib.java.models.iqm;

public class IQMJoint {

    public int name;
    public int parent;
    public float[] translate, rotate, scale;

    public IQMJoint() {
        translate = new float[3];
        rotate = new float[4];
        scale = new float[3];
    }

}
