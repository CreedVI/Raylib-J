package com.raylib.java.raymath;

class Float16{
    public float v[] = new float[16];

    public Float16(){
        v[0] = 0;
        v[1] = 0;
        v[2] = 0;
        v[3] = 0;
        v[4] = 0;
        v[5] = 0;
        v[6] = 0;
        v[7] = 0;
        v[8] = 0;
        v[9] = 0;
        v[10] = 0;
        v[11] = 0;
        v[12] = 0;
        v[13] = 0;
        v[14] = 0;
        v[15] = 0;
    }

    public Float16(float v0, float v1, float v2, float v3,
                   float v4, float v5, float v6, float v7,
                   float v8, float v9, float v10, float v11,
                   float v12, float v13, float v14, float v15){
        v[0] = v0;
        v[1] = v1;
        v[2] = v2;
        v[3] = v3;
        v[4] = v4;
        v[5] = v5;
        v[6] = v6;
        v[7] = v7;
        v[8] = v8;
        v[9] = v9;
        v[10] = v10;
        v[11] = v11;
        v[12] = v12;
        v[13] = v13;
        v[14] = v14;
        v[15] = v15;
    }

    public float[] getV(){
        return v;
    }

    public void setV(float[] v){
        this.v = v;
    }
}
