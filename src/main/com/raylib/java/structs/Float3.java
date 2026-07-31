package com.raylib.java.structs;

//Float3 / Float16?
public class Float3 implements Cloneable {
    public float[] v = new float[3];

    public Float3(){
        v[0] = 0;
        v[1] = 0;
        v[2] = 0;
    }

    public Float3(float v0, float v1, float v2){
        v[0] = v0;
        v[1] = v1;
        v[2] = v2;
    }

    @Override
    public Float3 clone() {
        try {
            Float3 clone = (Float3) super.clone();
            clone.v = new float[v.length];
            System.arraycopy(v, 0, clone.v, 0, v.length);
            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
