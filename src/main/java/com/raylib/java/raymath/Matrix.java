package com.raylib.java.raymath;

//OpenGL 4x4 - Right handed, Column Major
public class Matrix{
    public float m0, m4, m8, m12;
    public float m1, m5, m9, m13;
    public float m2, m6, m10, m14;
    public float m3, m7, m11, m15;

    public Matrix(){
        this.m0 = 0;
        this.m4 = 0;
        this.m8 = 0;
        this.m12 = 0;
        this.m1 = 0;
        this.m5 = 0;
        this.m9 = 0;
        this.m13 = 0;
        this.m2 = 0;
        this.m6 = 0;
        this.m10 = 0;
        this.m14 = 0;
        this.m3 = 0;
        this.m7 = 0;
        this.m11 = 0;
        this.m15 = 0;
    }

    public Matrix(float m0, float m4, float m8, float m12,
                  float m1, float m5, float m9, float m13,
                  float m2, float m6, float m10, float m14,
                  float m3, float m7, float m11, float m15){
        this.m0 = m0;
        this.m4 = m4;
        this.m8 = m8;
        this.m12 = m12;
        this.m1 = m1;
        this.m5 = m5;
        this.m9 = m9;
        this.m13 = m13;
        this.m2 = m2;
        this.m6 = m6;
        this.m10 = m10;
        this.m14 = m14;
        this.m3 = m3;
        this.m7 = m7;
        this.m11 = m11;
        this.m15 = m15;
    }

    public Matrix(Matrix m){
        this.m0 = m.m0;
        this.m4 = m.m4;
        this.m8 = m.m8;
        this.m12 = m.m12;
        this.m1 = m.m1;
        this.m5 = m.m5;
        this.m9 = m.m9;
        this.m13 = m.m13;
        this.m2 = m.m2;
        this.m6 = m.m6;
        this.m10 = m.m10;
        this.m14 = m.m14;
        this.m3 = m.m3;
        this.m7 = m.m7;
        this.m11 = m.m11;
        this.m15 = m.m15;
    }

    public Matrix(float[] mat){
        this.m0  = mat[0];     this.m4  = mat[4];     this.m8  = mat[8];     this.m12 = mat[12];
        this.m1  = mat[1];     this.m5  = mat[5];     this.m9  = mat[9];     this.m13 = mat[13];
        this.m2  = mat[2];     this.m6  = mat[6];     this.m10 = mat[10];    this.m14 = mat[14];
        this.m3 = mat[3];      this.m7  = mat[7];     this.m11 = mat[11];    this.m15 = mat[15];
    }

    public String toString() {
        return this.m0 + ", " + this.m4 + ", " + this.m8 + ", " + this.m12 + "\n" +
                this.m1 + ", " + this.m5 + ", " + this.m9 + ", " + this.m13 + "\n" +
                this.m2 + ", " + this.m6 + ", " + this.m10 + ", " + this.m14 + "\n" +
                this.m3 + ", " + this.m7 + ", " + this.m11 + ", " + this.m15;
    }

    public float getM0(){
        return m0;
    }

    public void setM0(float m0){
        this.m0 = m0;
    }

    public float getM4(){
        return m4;
    }

    public void setM4(float m4){
        this.m4 = m4;
    }

    public float getM8(){
        return m8;
    }

    public void setM8(float m8){
        this.m8 = m8;
    }

    public float getM12(){
        return m12;
    }

    public void setM12(float m12){
        this.m12 = m12;
    }

    public float getM1(){
        return m1;
    }

    public void setM1(float m1){
        this.m1 = m1;
    }

    public float getM5(){
        return m5;
    }

    public void setM5(float m5){
        this.m5 = m5;
    }

    public float getM9(){
        return m9;
    }

    public void setM9(float m9){
        this.m9 = m9;
    }

    public float getM13(){
        return m13;
    }

    public void setM13(float m13){
        this.m13 = m13;
    }

    public float getM2(){
        return m2;
    }

    public void setM2(float m2){
        this.m2 = m2;
    }

    public float getM6(){
        return m6;
    }

    public void setM6(float m6){
        this.m6 = m6;
    }

    public float getM10(){
        return m10;
    }

    public void setM10(float m10){
        this.m10 = m10;
    }

    public float getM14(){
        return m14;
    }

    public void setM14(float m14){
        this.m14 = m14;
    }

    public float getM3(){
        return m3;
    }

    public void setM3(float m3){
        this.m3 = m3;
    }

    public float getM7(){
        return m7;
    }

    public void setM7(float m7){
        this.m7 = m7;
    }

    public float getM11(){
        return m11;
    }

    public void setM11(float m11){
        this.m11 = m11;
    }

    public float getM15(){
        return m15;
    }

    public void setM15(float m15){
        this.m15 = m15;
    }
}
