package com.raylib.textures;

import com.raylib.shapes.Rectangle;

public class NPatchInfo{

    Rectangle source;   // Region in the texture
    int left;              // left border offset
    int top;               // top border offset
    int right;             // right border offset
    int bottom;            // bottom border offset
    int type;              // layout of the n-patch: 3x3, 1x3 or 3x1

    enum NPatchType{
        NPT_9PATCH(0),         // Npatch defined by 3x3 tiles
        NPT_3PATCH_VERTICAL(1),    // Npatch defined by 1x3 tiles
        NPT_3PATCH_HORIZONTAL(2)   // Npatch defined by 3x1 tiles
        ;

        private final int patchType;

        NPatchType(int i){
            patchType = i;
        }

        public int getPatchType(){
            return patchType;
        }
    }

    public NPatchInfo(){
        source = new Rectangle(0,0,1,1);
        left = 0;
        right = 0;
        top = 0;
        bottom = 0;
    }

    public Rectangle getSource(){
        return source;
    }

    public void setSource(Rectangle source){
        this.source = source;
    }

    public int getLeft(){
        return left;
    }

    public void setLeft(int left){
        this.left = left;
    }

    public int getTop(){
        return top;
    }

    public void setTop(int top){
        this.top = top;
    }

    public int getRight(){
        return right;
    }

    public void setRight(int right){
        this.right = right;
    }

    public int getBottom(){
        return bottom;
    }

    public void setBottom(int bottom){
        this.bottom = bottom;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setType(NPatchType type){
        this.type = type.getPatchType();
    }
}