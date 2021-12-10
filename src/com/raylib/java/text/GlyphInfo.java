package com.raylib.java.text;

import com.raylib.java.textures.Image;

public class GlyphInfo{

    public int value;              // Character value (Unicode)
    public int offsetX;            // Character offset X when drawing
    public int offsetY;            // Character offset Y when drawing
    public int advanceX;           // Character advance position X
    public Image image;            // Character image data

    public GlyphInfo(){
        value = 0;
        offsetX = 0;
        offsetY = 0;
        advanceX = 0;
        image = new Image();
    }

    public int getValue(){
        return value;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getOffsetX(){
        return offsetX;
    }

    public void setOffsetX(int offsetX){
        this.offsetX = offsetX;
    }

    public int getOffsetY(){
        return offsetY;
    }

    public void setOffsetY(int offsetY){
        this.offsetY = offsetY;
    }

    public int getAdvanceX(){
        return advanceX;
    }

    public void setAdvanceX(int advanceX){
        this.advanceX = advanceX;
    }

    public Image getImage(){
        return image;
    }

    public void setImage(Image image){
        this.image = image;
    }
}
