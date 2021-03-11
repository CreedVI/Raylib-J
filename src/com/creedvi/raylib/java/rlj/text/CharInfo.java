package com.creedvi.raylib.java.rlj.text;

import com.creedvi.raylib.java.rlj.textures.Image;

public class CharInfo{

    int value;              // Character value (Unicode)
    int offsetX;            // Character offset X when drawing
    int offsetY;            // Character offset Y when drawing
    int advanceX;           // Character advance position X
    Image image;            // Character image data

    public CharInfo(){
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
