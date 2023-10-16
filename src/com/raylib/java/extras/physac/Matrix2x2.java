package com.raylib.java.extras.physac;

public class Matrix2x2{

    float m00;
    float m01;
    float m10;
    float m11;

    Matrix2x2(){
        m00 = 0;
        m01 = 0;
        m10 = 0;
        m11 = 0;
    }

    public Matrix2x2(float m00, float m01, float m10, float m11){
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }
}
