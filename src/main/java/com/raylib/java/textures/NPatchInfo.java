package com.raylib.java.textures;

import com.raylib.java.shapes.Rectangle;

public class NPatchInfo{

    public Rectangle source;   // Region in the texture
    public int left;              // left border offset
    public int top;               // top border offset
    public int right;             // right border offset
    public int bottom;            // bottom border offset
    public int type;              // layout of the n-patch: 3x3, 1x3 or 3x1

    public static class NPatchType{
        public static final int
                NPATCH_NINE_PATCH = 0,         // Npatch defined by 3x3 tiles
                NPATCH_THREE_PATCH_VERTICAL = 1,    // Npatch defined by 1x3 tiles
                NPATCH_THREE_PATCH_HORIZONTAL = 2;   // Npatch defined by 3x1 tiles
    }

    public NPatchInfo() {
        source = new Rectangle(0,0,1,1);
        left = 0;
        right = 0;
        top = 0;
        bottom = 0;
    }

    public NPatchInfo(Rectangle source, int left, int top, int right, int bottom, int type) {
        this.source = source;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.type = type;
    }

    public Rectangle getSource() {
        return source;
    }

    public void setSource(Rectangle source) {
        this.source = source;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}