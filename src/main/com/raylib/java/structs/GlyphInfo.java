package com.raylib.java.structs;

/**
 * GlyphInfo, font characters glyphs info
 */
public class GlyphInfo implements Cloneable{

    /**
     * Character value (Unicode)
     */
    public int value;
    /**
     * Character offset X when drawing
     */
    public int offsetX;
    /**
     * Character offset Y when drawing
     */
    public int offsetY;
    /**
     * Character advance position X
     */
    public int advanceX;
    /**
     * Character image data
     */
    public Image image;

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

    @Override
    public GlyphInfo clone() {
        try {
            GlyphInfo clone = (GlyphInfo) super.clone();
            clone.image = image.clone();

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
