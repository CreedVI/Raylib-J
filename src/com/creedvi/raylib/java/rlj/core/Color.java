package com.creedvi.raylib.java.rlj.core;

public class Color{

    int r, g, b, a;

    public Color(){
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.a = 0;
    }

    public Color(int r, int g, int b, int a){
        this.r = Math.min(r, 255);
        this.g = Math.min(g, 255);
        this.b = Math.min(b, 255);;
        this.a = Math.min(a, 255);;
    }

    public String ToString(){
        return r + " " + g + " " + b + " " + a;
    }

    public static  Color LIGHTGRAY = new Color(200, 200, 200, 255);
    public static  Color GRAY = new Color(130, 130, 130, 255);
    public static  Color DARKGRAY = new Color(80, 80, 80, 255);
    public static  Color YELLOW = new Color(253, 249, 0, 255);
    public static  Color GOLD = new Color(255, 203, 0, 255);
    public static  Color ORANGE = new Color(255, 161, 0, 255);
    public static  Color PINK = new Color(255, 109, 194, 255);
    public static  Color RED = new Color(230, 41, 55, 255);
    public static  Color MAROON = new Color(190, 33, 55, 255);
    public static  Color GREEN = new Color(0, 228, 48, 255);
    public static  Color LIME = new Color(0, 158, 47, 255);
    public static  Color DARKGREEN = new Color(0, 117, 44, 255);
    public static  Color SKYBLUE = new Color(102, 191, 255, 255);
    public static  Color BLUE = new Color(0, 121, 241, 255);
    public static  Color DARKBLUE = new Color(0, 82, 172, 255);
    public static  Color PURPLE = new Color(200, 122, 255, 255);
    public static  Color VIOLET = new Color(135, 60, 190, 255);
    public static  Color DARKPURPLE = new Color(112, 31, 126, 255);
    public static  Color BEIGE = new Color(211, 176, 131, 255);
    public static  Color BROWN = new Color(127, 106, 79, 255);
    public static  Color DARKBROWN = new Color(76, 63, 47, 255);
    public static  Color WHITE = new Color(255, 255, 255, 255);
    public static  Color BLACK = new Color(0, 0, 0, 255);
    public static  Color BLANK = new Color(0, 0, 0, 0);
    public static Color MAGENTA = new Color(255, 0, 255, 255);
    public static Color RAYWHITE = new Color(245, 245, 245, 255);

    public int getR(){
        return r;
    }

    public void setR(int r){
        this.r = r;
    }

    public int getG(){
        return g;
    }

    public void setG(int g){
        this.g = g;
    }

    public int getB(){
        return b;
    }

    public void setB(int b){
        this.b = b;
    }

    public int getA(){
        return a;
    }

    public void setA(int a){
        this.a = a;
    }
}

