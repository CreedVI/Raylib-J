package com.raylib.java.structs;

public class NPatchInfo implements Cloneable{

    public Rectangle source;   // Region in the texture
    public int left;              // left border offset
    public int top;               // top border offset
    public int right;             // right border offset
    public int bottom;            // bottom border offset
    public NPatchType layout;              // layout of the n-patch: 3x3, 1x3 or 3x1

    public enum NPatchType {
        NPATCH_NINE_PATCH(0),         // Npatch defined by 3x3 tiles
        NPATCH_THREE_PATCH_VERTICAL(1),    // Npatch defined by 1x3 tiles
        NPATCH_THREE_PATCH_HORIZONTAL(2);   // Npatch defined by 3x1 tiles

        private final int value;

        NPatchType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public NPatchInfo() {
        source = new Rectangle(0,0,1,1);
        left = 0;
        right = 0;
        top = 0;
        bottom = 0;
    }

    public NPatchInfo(Rectangle source, int left, int top, int right, int bottom, NPatchType type) {
        this.source = source;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.layout = type;
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

    public NPatchType getLayout() {
        return layout;
    }

    public void setLayout(NPatchType layout) {
        this.layout = layout;
    }

    @Override
    public NPatchInfo clone() {
        try {
            NPatchInfo clone = (NPatchInfo) super.clone();

            clone.source = source.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}