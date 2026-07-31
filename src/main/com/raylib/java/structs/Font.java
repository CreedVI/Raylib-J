package com.raylib.java.structs;

public class Font implements Cloneable{

    public int baseSize;           // Base size (default chars height)
    public int glyphCount;         // Number of characters
    public int glyphPadding;       // Padding around the chars
    public Texture2D texture;      // Characters texture atlas
    public Rectangle[] recs;        // Characters rectangles in texture
    public GlyphInfo[] glyphs;        // Characters info data

    public Font(){
        baseSize = 0;
        glyphCount = 0;
        glyphPadding = 0;
        texture = new Texture2D();
        recs = new Rectangle[256];
        glyphs = new GlyphInfo[256];
    }

    public int getBaseSize(){
        return baseSize;
    }

    public void setBaseSize(int baseSize){
        this.baseSize = baseSize;
    }

    public int getGlyphCount(){
        return glyphCount;
    }

    public void setGlyphCount(int glyphCount){
        this.glyphCount = glyphCount;
    }

    public int getGlyphPadding(){
        return glyphPadding;
    }

    public void setGlyphPadding(int glyphPadding){
        this.glyphPadding = glyphPadding;
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

    public GlyphInfo[] getGlyphs(){
        return glyphs;
    }

    public void setGlyphs(GlyphInfo[] glyphs){
        this.glyphs = glyphs;
    }

    @Override
    public Font clone() {
        try {
            Font clone = (Font) super.clone();

            clone.texture = texture.clone();
            clone.recs = new Rectangle[recs.length];
            System.arraycopy(recs, 0, clone.recs, 0, recs.length);
            clone.glyphs = new GlyphInfo[glyphs.length];
            System.arraycopy(glyphs, 0, clone.glyphs, 0, glyphs.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
