package com.raylib.java.raymath;

//Float3 / Float16?
class Float3{
    public float v[] = new float[3];

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

    public Float3(Float3 f){
        v[0] = f.v[0];
        v[1] = f.v[1];
        v[2] = f.v[2];
    }
}
