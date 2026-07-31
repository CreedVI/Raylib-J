package com.raylib.java.structs;

public class Color implements Cloneable {

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
     * String value of Color
     * @return R, G, B, A
     */
    public String toString() {
        return r + ", " + g + ", " + b + ", " + a;
    }

    public static final Color LIGHTGRAY = new Color(200, 200, 200, 255);
    public static final Color GRAY = new Color(130, 130, 130, 255);
    public static final Color DARKGRAY = new Color(80, 80, 80, 255);
    public static final Color YELLOW = new Color(253, 249, 0, 255);
    public static final Color GOLD = new Color(255, 203, 0, 255);
    public static final Color ORANGE = new Color(255, 161, 0, 255);
    public static final Color PINK = new Color(255, 109, 194, 255);
    public static final Color RED = new Color(230, 41, 55, 255);
    public static final Color MAROON = new Color(190, 33, 55, 255);
    public static final Color GREEN = new Color(0, 228, 48, 255);
    public static final Color LIME = new Color(0, 158, 47, 255);
    public static final Color DARKGREEN = new Color(0, 117, 44, 255);
    public static final Color SKYBLUE = new Color(102, 191, 255, 255);
    public static final Color BLUE = new Color(0, 121, 241, 255);
    public static final Color DARKBLUE = new Color(0, 82, 172, 255);
    public static final Color PURPLE = new Color(200, 122, 255, 255);
    public static final Color VIOLET = new Color(135, 60, 190, 255);
    public static final Color DARKPURPLE = new Color(112, 31, 126, 255);
    public static final Color BEIGE = new Color(211, 176, 131, 255);
    public static final Color BROWN = new Color(127, 106, 79, 255);
    public static final Color DARKBROWN = new Color(76, 63, 47, 255);
    public static final Color WHITE = new Color(255, 255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0, 255);
    public static final Color BLANK = new Color(0, 0, 0, 0);
    public static final Color MAGENTA = new Color(255, 0, 255, 255);
    public static final Color RAYWHITE = new Color(245, 245, 245, 255);

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

    /**
     * Converts an array of raw pixel data into an array of colors
     * @param pixels Raw pixel data (RGBA format)
     * @return Array of colors
     */
    public static Color[] FromPixels(byte[] pixels) {
        Color[] result = new Color[pixels.length/4];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Color();
        }

        for (int i = 0; i < result.length; i++) {
            result[i].r = Byte.toUnsignedInt(pixels[4 * i]);
            result[i].g = Byte.toUnsignedInt(pixels[4 * i + 1]);
            result[i].b = Byte.toUnsignedInt(pixels[4 * i + 2]);
            result[i].a = Byte.toUnsignedInt(pixels[4 * i + 3]);
        }

        return result;
    }

    /**
     * Converts a Color object to an array of bytes
     * @param color Color
     * @return byte array of values (length = 4, RGBA format)
     */
    public static byte[] ToPixels(Color color) {
        byte[] result = new byte[4];
        result[0] = (byte) color.r;
        result[1] = (byte) color.g;
        result[2] = (byte) color.b;
        result[3] = (byte) color.a;
        return result;
    }

    @Override
    public Color clone() {
        try {
            Color clone = (Color) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

