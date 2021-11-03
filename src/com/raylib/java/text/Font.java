package com.raylib.java.text;

import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;

public class Font{

    public int baseSize;           // Base size (default chars height)
    public int charsCount;         // Number of characters
    public int charsPadding;       // Padding around the chars
    public Texture2D texture;      // Characters texture atlas
    public Rectangle[] recs;        // Characters rectangles in texture
    public CharInfo[] chars;        // Characters info data

    public Font(){
        baseSize = 0;
        charsCount = 0;
        charsPadding = 0;
        recs = new Rectangle[256];
        chars = new CharInfo[256];
    }

    public int getBaseSize(){
        return baseSize;
    }

    public void setBaseSize(int baseSize){
        this.baseSize = baseSize;
    }

    public int getCharsCount(){
        return charsCount;
    }

    public void setCharsCount(int charsCount){
        this.charsCount = charsCount;
    }

    public int getCharsPadding(){
        return charsPadding;
    }

    public void setCharsPadding(int charsPadding){
        this.charsPadding = charsPadding;
    }

    public Texture2D getTexture(){
        return texture;
    }

    public void setTexture(Texture2D texture){
        this.texture = texture;
    }

    public Rectangle[] getRecs(){
        return recs;
    }

    public void setRecs(Rectangle[] recs){
        this.recs = recs;
    }

    public CharInfo[] getChars(){
        return chars;
    }

    public void setChars(CharInfo[] chars){
        this.chars = chars;
    }
}
