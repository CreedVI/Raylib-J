package com.raylib.java.structs;

/**
 * X, Y coordinate pair
 */
public class Point extends Vector2 {

    public Point() {
        super();
    }

    public Point(int x, int y) {
        super(x, y);
    }

    @Override
    public Point clone() {
        return (Point) super.clone();
    }
}
