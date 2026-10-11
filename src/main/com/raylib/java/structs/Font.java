package com.raylib.java.structs;

/**
 * Font, font texture and GlyphInfo array data
 */
public class Font implements Cloneable{

    /**
     * Base size (default chars height)
     */
    public int baseSize;
    /**
     * Number of glyph characters
     */
    public int glyphCount;
    /**
     * Padding around the glyph characters
     */
    public int glyphPadding;
    /**
     * Texture atlas containing the glyphs
     */
    public Texture2D texture;
    /**
     * Rectangles in texture for the glyphs
     */
    public Rectangle[] recs;
    /**
     * Glyphs info data
     */
    public GlyphInfo[] glyphs;

    public Font(){
        baseSize = 0;
        glyphCount = 0;
        glyphPadding = 0;
        texture = new Texture2D();
        recs = new Rectangle[256];
        glyphs = null;
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
