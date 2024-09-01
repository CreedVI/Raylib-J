package com.raylib.java.core;

public class Color{

    public int r, g, b, a;

    /**
     * Creates new color (Black)
     */
    public Color() {
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.a = 255;
    }

    /**
     * Creates new color with desired RGBA
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @param a Alpha (0-255)
     */
    public Color(int r, int g, int b, int a) {
        this.r = (r > 0 ? Math.min(r, 255) : 0);
        this.g = (g > 0 ? Math.min(g, 255) : 0);
        this.b = (b > 0 ? Math.min(b, 255) : 0);
        this.a = (a > 0 ? Math.min(a, 255) : 0);
    }

    /**
     * Creates new color from existing color
     * @param c Color
     */
    public Color(Color c) {
        this.r = c.r;
        this.g = c.g;
        this.b = c.b;
        this.a = c.a;
    }

    /**
     * String value of Color
     * @return R, G, B, A
     */
    public String toString() {
        return r + ", " + g + ", " + b + ", " + a;
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

    /**
     * Returns red value
     * @return Red
     */
    public int getR() {
        return r;
    }

    /**
     * Sets the red value
     * @param r Red (0-255)
     */
    public void setR(int r) {
        this.r = r;
    }

    /**
     * Returns green value
     * @return green
     */
    public int getG() {
        return g;
    }

    /**
     * Sets the green value
     * @param g Green (0-255)
     */
    public void setG(int g) {
        this.g = g;
    }

    /**
     * Returns the blue value
     * @return blue
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the blue value
     * @param b Blue (0-255)
     */
    public void setB(int b) {
        this.b = b;
    }

    /**
     * Returns the Alpha value
     * @return Alpha
     */
    public int getA(){
        return a;
    }

    /**
     * Sets that Alpha value
     * @param a Alpha (0-255)
     */
    public void setA(int a){
        this.a = a;
    }

}

