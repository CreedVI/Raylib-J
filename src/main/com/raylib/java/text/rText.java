package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.GlyphInfo;
import org.jetbrains.annotations.Contract;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static com.raylib.java.Config.*;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.rlgl.RLGL.rlTextureFilterMode.TEXTURE_FILTER_POINT;
import static com.raylib.java.text.rText.FontType.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.libc.LibCString.memcpy;

public class rText {

    /**********************************************************************************************
     *
     *   rtext - Basic functions to load fonts and draw text
     *
     *   CONFIGURATION:
     *       #define SUPPORT_MODULE_RTEXT        1
     *           rtext module is included in the build
     *
     *       #define SUPPORT_FILEFORMAT_FNT      1
     *       #define SUPPORT_FILEFORMAT_TTF      1
     *       #define SUPPORT_FILEFORMAT_BDF      0
     *           Selected desired fileformats to be supported for loading. Some of those formats are
     *           supported by default, to remove support, #define as 0 in this module or your build system
     *
     *       #define MAX_TEXT_BUFFER_LENGTH   1024
     *           Text functions using static buffer max size
     *
     *       #define MAX_TEXTSPLIT_COUNT       128
     *           TextSplit() function static substrings pointers array (pointing to static buffer)
     *
     *       #define FONT_ATLAS_CORNER_REC_SIZE  3
     *           On font atlas image generation [GenImageFontAtlas()], add a NxN pixels white rectangle
     *           at the bottom-right corner of the atlas. It can be useful to for shapes drawing, to allow
     *           drawing text and shapes with a single draw call [SetShapesTexture()]
     *
     *   DEPENDENCIES:
     *       stb_truetype  - Load TTF file and rasterize characters data
     *       stb_rect_pack - Rectangles packing algorithms, required for font atlas generation
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2013-2026 Ramon Santamaria (@raysan5)
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     **********************************************************************************************/

    /**
     * Font type, defines generation method
     */
    public enum FontType {

        /**
         * Default font generation, anti-aliased
         */
        FONT_DEFAULT(0),
        /**
         * Bitmap font generation, no anti-aliasing
         */
        FONT_BITMAP(1),
        /**
         * SDF font generation, requires external shader
         */
        FONT_SDF(2);

        private final int value;

        FontType(int value) {
            this.value = value;
        }
    }

    private final int MAX_TEXTFORMAT_BUFFERS = 4;             // Maximum number of static buffers for text formatting
    private final int GLYPH_NOTFOUND_CHAR_FALLBACK = 63;      // Character used if requested codepoint is not found: '?'
    private int textLineSpacing = 15;        // Text vertical line spacing in pixels

    // Default values for ttf font generation
    final int FONT_TTF_DEFAULT_SIZE = 32;          // TTF font generation default char size (char-height)
    final int FONT_TTF_DEFAULT_NUMCHARS = 95;      // TTF font generation default charset: 95 glyphs (ASCII 32..126)
    final int FONT_TTF_DEFAULT_FIRST_CHAR = 32;    // TTF font generation default first char for image sprite font (32-Space)
    final int FONT_TTF_DEFAULT_CHARS_PADDING = 4;  // TTF font generation default chars padding
    final int MAX_GLYPHS_FROM_IMAGE = 256;         // Maximum number of glyphs supported on image scan

    final int MAX_TEXTSPLIT_COUNT = 128;           // Maximum number of substrings to split: TextSplit()

    final int FONT_ATLAS_CORNER_REC_SIZE = 3;         // Size of white rectangle drawn on font atlas on font loading

    private Font defaultFont;
    private final Raylib context;

    public rText(Raylib raylib) {
        this.context = raylib;

        if (SUPPORT_DEFAULT_FONT) {
            defaultFont = new Font();
        }
    }

    /**
     * Check if the bth binary bit is 1 in an integer
     *
     * @param a the value to check
     * @param b position of bit to examine
     * @return <code>true</code> if bit at b is set to 1
     */
    private boolean BitCheck(int a, int b) {
        return ((a) & (1L << (b))) == (long) Math.pow(2, b);
    }

    /**
     * Check if col1 is equal in RGBA value to col2
     *
     * @param col1
     * @param col2
     * @return
     */
    private boolean ColorEqual(Color col1, Color col2) {
        return ((col1.r == col2.r) && (col1.g == col2.g) && (col1.b == col2.b) && (col1.a == col2.a));
    }

    /**
     * Load raylib default font
     */
    public void LoadFontDefault() {
        // Check to see if the font for an image has already been allocated,
        // and if no need to upload, then return
        if (defaultFont.glyphs != null) {
            return;
        }

        // NOTE: Using UTF8 encoding table for Unicode U+0000..U+00FF Basic Latin + Latin-1 Supplement
        // REF: http://www.utf8-chartable.de/unicode-utf8-table.pl

        defaultFont.setGlyphCount(224);   // Number of glyphs included in our default font
        defaultFont.setGlyphPadding(0);    // Characters padding

        // Default font is directly defined here (data generated from a sprite font image)
        // This way, reconstructing Font without creating large global variables
        // This data is automatically allocated to Stack and automatically deallocated at the end of this function
        int[] defaultFontData = new int[]{
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00200020, 0x0001b000, 0x00000000, 0x00000000, 0x8ef92520, 0x00020a00, 0x7dbe8000, 0x1f7df45f,
                0x4a2bf2a0, 0x0852091e, 0x41224000, 0x10041450, 0x2e292020, 0x08220812, 0x41222000, 0x10041450, 0x10f92020, 0x3efa084c, 0x7d22103c, 0x107df7de,
                0xe8a12020, 0x08220832, 0x05220800, 0x10450410, 0xa4a3f000, 0x08520832, 0x05220400, 0x10450410, 0xe2f92020, 0x0002085e, 0x7d3e0281, 0x107df41f,
                0x00200000, 0x8001b000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xc0000fbe, 0xfbf7e00f, 0x5fbf7e7d, 0x0050bee8, 0x440808a2, 0x0a142fe8, 0x50810285, 0x0050a048,
                0x49e428a2, 0x0a142828, 0x40810284, 0x0048a048, 0x10020fbe, 0x09f7ebaf, 0xd89f3e84, 0x0047a04f, 0x09e48822, 0x0a142aa1, 0x50810284, 0x0048a048,
                0x04082822, 0x0a142fa0, 0x50810285, 0x0050a248, 0x00008fbe, 0xfbf42021, 0x5f817e7d, 0x07d09ce8, 0x00008000, 0x00000fe0, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x000c0180,
                0xdfbf4282, 0x0bfbf7ef, 0x42850505, 0x004804bf, 0x50a142c6, 0x08401428, 0x42852505, 0x00a808a0, 0x50a146aa, 0x08401428, 0x42852505, 0x00081090,
                0x5fa14a92, 0x0843f7e8, 0x7e792505, 0x00082088, 0x40a15282, 0x08420128, 0x40852489, 0x00084084, 0x40a16282, 0x0842022a, 0x40852451, 0x00088082,
                0xc0bf4282, 0xf843f42f, 0x7e85fc21, 0x3e0900bf, 0x00000000, 0x00000004, 0x00000000, 0x000c0180, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x04000402, 0x41482000, 0x00000000, 0x00000800,
                0x04000404, 0x4100203c, 0x00000000, 0x00000800, 0xf7df7df0, 0x514bef85, 0xbefbefbe, 0x04513bef, 0x14414500, 0x494a2885, 0xa28a28aa, 0x04510820,
                0xf44145f0, 0x474a289d, 0xa28a28aa, 0x04510be0, 0x14414510, 0x494a2884, 0xa28a28aa, 0x02910a00, 0xf7df7df0, 0xd14a2f85, 0xbefbe8aa, 0x011f7be0,
                0x00000000, 0x00400804, 0x20080000, 0x00000000, 0x00000000, 0x00600f84, 0x20080000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0xac000000, 0x00000f01, 0x00000000, 0x00000000, 0x24000000, 0x00000f01, 0x00000000, 0x06000000, 0x24000000, 0x00000f01, 0x00000000, 0x09108000,
                0x24fa28a2, 0x00000f01, 0x00000000, 0x013e0000, 0x2242252a, 0x00000f52, 0x00000000, 0x038a8000, 0x2422222a, 0x00000f29, 0x00000000, 0x010a8000,
                0x2412252a, 0x00000f01, 0x00000000, 0x010a8000, 0x24fbe8be, 0x00000f01, 0x00000000, 0x0ebe8000, 0xac020000, 0x00000f01, 0x00000000, 0x00048000,
                0x0003e000, 0x00000f00, 0x00000000, 0x00008000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000038, 0x8443b80e, 0x00203a03,
                0x02bea080, 0xf0000020, 0xc452208a, 0x04202b02, 0xf8029122, 0x07f0003b, 0xe44b388e, 0x02203a02, 0x081e8a1c, 0x0411e92a, 0xf4420be0, 0x01248202,
                0xe8140414, 0x05d104ba, 0xe7c3b880, 0x00893a0a, 0x283c0e1c, 0x04500902, 0xc4400080, 0x00448002, 0xe8208422, 0x04500002, 0x80400000, 0x05200002,
                0x083e8e00, 0x04100002, 0x804003e0, 0x07000042, 0xf8008400, 0x07f00003, 0x80400000, 0x04000022, 0x00000000, 0x00000000, 0x80400000, 0x04000002,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00800702, 0x1848a0c2, 0x84010000, 0x02920921, 0x01042642, 0x00005121, 0x42023f7f, 0x00291002,
                0xefc01422, 0x7efdfbf7, 0xefdfa109, 0x03bbbbf7, 0x28440f12, 0x42850a14, 0x20408109, 0x01111010, 0x28440408, 0x42850a14, 0x2040817f, 0x01111010,
                0xefc78204, 0x7efdfbf7, 0xe7cf8109, 0x011111f3, 0x2850a932, 0x42850a14, 0x2040a109, 0x01111010, 0x2850b840, 0x42850a14, 0xefdfbf79, 0x03bbbbf7,
                0x001fa020, 0x00000000, 0x00001000, 0x00000000, 0x00002070, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x08022800, 0x00012283, 0x02430802, 0x01010001, 0x8404147c, 0x20000144, 0x80048404, 0x00823f08, 0xdfbf4284, 0x7e03f7ef, 0x142850a1, 0x0000210a,
                0x50a14684, 0x528a1428, 0x142850a1, 0x03efa17a, 0x50a14a9e, 0x52521428, 0x142850a1, 0x02081f4a, 0x50a15284, 0x4a221428, 0xf42850a1, 0x03efa14b,
                0x50a16284, 0x4a521428, 0x042850a1, 0x0228a17a, 0xdfbf427c, 0x7e8bf7ef, 0xf7efdfbf, 0x03efbd0b, 0x00000000, 0x04000000, 0x00000000, 0x00000008,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00200508, 0x00840400, 0x11458122, 0x00014210,
                0x00514294, 0x51420800, 0x20a22a94, 0x0050a508, 0x00200000, 0x00000000, 0x00050000, 0x08000000, 0xfefbefbe, 0xfbefbefb, 0xfbeb9114, 0x00fbefbe,
                0x20820820, 0x8a28a20a, 0x8a289114, 0x3e8a28a2, 0xfefbefbe, 0xfbefbe0b, 0x8a289114, 0x008a28a2, 0x228a28a2, 0x08208208, 0x8a289114, 0x088a28a2,
                0xfefbefbe, 0xfbefbefb, 0xfa2f9114, 0x00fbefbe, 0x00000000, 0x00000040, 0x00000000, 0x00000000, 0x00000000, 0x00000020, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00210100, 0x00000004, 0x00000000, 0x00000000, 0x14508200, 0x00001402, 0x00000000, 0x00000000,
                0x00000010, 0x00000020, 0x00000000, 0x00000000, 0xa28a28be, 0x00002228, 0x00000000, 0x00000000, 0xa28a28aa, 0x000022e8, 0x00000000, 0x00000000,
                0xa28a28aa, 0x000022a8, 0x00000000, 0x00000000, 0xa28a28aa, 0x000022e8, 0x00000000, 0x00000000, 0xbefbefbe, 0x00003e2f, 0x00000000, 0x00000000,
                0x00000004, 0x00002028, 0x00000000, 0x00000000, 0x80000000, 0x00003e0f, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000
        };

        int charsHeight = 10;
        int charsDivisor = 1;    // Every char is separated from the consecutive by a 1 pixel divisor, horizontally and vertically

        int[] charsWidth = {
                3, 1, 4, 6, 5, 7, 6, 2, 3, 3, 5, 5, 2, 4, 1, 7, 5, 2, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 3, 4, 3, 6,
                7, 6, 6, 6, 6, 6, 6, 6, 6, 3, 5, 6, 5, 7, 6, 6, 6, 6, 6, 6, 7, 6, 7, 7, 6, 6, 6, 2, 7, 2, 3, 5,
                2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 2, 5, 2, 5, 5, 5, 5, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5, 3, 1, 3, 4, 4,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 5, 5, 5, 7, 1, 5, 3, 7, 3, 5, 4, 1, 7, 4, 3, 5, 3, 3, 2, 5, 6, 1, 2, 2, 3, 5, 6, 6, 6, 6,
                6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 3, 3, 3, 3, 7, 6, 6, 6, 6, 6, 6, 5, 6, 6, 6, 6, 6, 6, 4, 6,
                5, 5, 5, 5, 5, 5, 9, 5, 5, 5, 5, 5, 2, 2, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5
        };

        // Re-construct image from defaultFontData and generate OpenGL texture
        //----------------------------------------------------------------------
        Image imFont = new Image();
        imFont.setWidth(128);
        imFont.setHeight(128);
        imFont.setFormat(PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA);
        imFont.setMipmaps(1);

        short[] fontdata = new short[128 * 128 * 2];  // 2 bytes per pixel (gray + alpha)

        //Fill image.data with defaultFontData (convert from bit to pixel!)
        for (int i = 0, counter = 0; i < imFont.getWidth() * imFont.getHeight(); i += 32) {
            for (int j = 31; j >= 0; j--) {
                if (BitCheck(defaultFontData[counter], j)) {
                    // NOTE: Unreferencing data as short, so,
                    // considering data as little-endian (alpha + gray)
                    fontdata[i + j] = (short) 0xffff;
                }
                else {
                    fontdata[i + j] = (short) 0x0000;
                }
            }
            counter++;
        }

        imFont.setData(fontdata);

        defaultFont.texture = context.textures.LoadTextureFromImage(imFont);

        // Check again if font glyph data has been already loaded
        // to avoid reallocating the glyphs and rects
        if (defaultFont.glyphs != null) {
            context.textures.UnloadImage(imFont);
            return;
        }

        // Reconstruct charSet using charsWidth[], charsHeight, charsDivisor, glyphCount
        //------------------------------------------------------------------------------

        // Allocate space for our characters info data
        // NOTE: This memory must be freed at end! --> Done by CloseWindow()
        defaultFont.glyphs = new GlyphInfo[defaultFont.glyphCount];
        for (int i = 0; i < defaultFont.glyphCount; i++) {
            defaultFont.glyphs[i] = new GlyphInfo();
        }
        defaultFont.recs = new Rectangle[defaultFont.glyphCount];
        for (int i = 0; i < defaultFont.glyphCount; i++) {
            defaultFont.recs[i] = new Rectangle();
        }

        int currentLine = 0;
        int currentPosX = charsDivisor;
        int testPosX = charsDivisor;

        for (int i = 0; i < defaultFont.glyphCount; i++) {
            defaultFont.glyphs[i].value = 32 + i;  // First char is 32

            defaultFont.recs[i].x = (float) currentPosX;
            defaultFont.recs[i].y = (float) (charsDivisor + currentLine * (charsHeight + charsDivisor));
            defaultFont.recs[i].width = (float) charsWidth[i];
            defaultFont.recs[i].height = (float) charsHeight;

            testPosX += (int) (defaultFont.recs[i].width + (float) charsDivisor);

            if (testPosX >= imFont.width) {
                currentLine++;
                currentPosX = 2 * charsDivisor + charsWidth[i];
                testPosX = currentPosX;

                defaultFont.recs[i].x = (float) charsDivisor;
                defaultFont.recs[i].y = (float) (charsDivisor + currentLine * (charsHeight + charsDivisor));
            }

            else {
                currentPosX = testPosX;
            }

            // NOTE: On default font character offsets and xAdvance are not required
            defaultFont.glyphs[i].offsetX = 0;
            defaultFont.glyphs[i].offsetY = 0;
            defaultFont.glyphs[i].advanceX = 0;

            // Fill character image data from fontClear data
            defaultFont.glyphs[i].image = context.textures.ImageFromImage(imFont, defaultFont.recs[i]);
        }

        context.textures.UnloadImage(imFont);

        defaultFont.baseSize = (int) defaultFont.recs[0].height;

        context.tracelog.TRACELOG(LOG_INFO, "FONT: Default font loaded successfully (" + defaultFont.glyphCount + " glyphs)");
    }

    /**
     * Unload raylib default font
     */
    public void UnloadFontDefault() {
        for (int i = 0; i < defaultFont.glyphCount; i++) {
            defaultFont.glyphs[i].image = context.textures.UnloadImage(defaultFont.glyphs[i].image);
        }
        defaultFont.texture = null;
        defaultFont.glyphs = null;
        defaultFont.glyphCount = 0;
        defaultFont.recs = null;
    }

    /**
     * Get the default font, useful to be used with extended parameters
     *
     * @return Default raylib font
     */
    public Font GetFontDefault() {
        return defaultFont;
    }

    /**
     * Load Font from file into GPU memory (VRAM)
     *
     * @param fileName Path to font file
     * @return Font defined by file
     */
    public Font LoadFont(String fileName) {
        Font font = null;

        if (SUPPORT_FILEFORMAT_TTF) {
            if (context.files.IsFileExtension(fileName, ".ttf") || context.files.IsFileExtension(fileName, ".otf")) {
                font = LoadFontEx(fileName, FONT_TTF_DEFAULT_SIZE, null, FONT_TTF_DEFAULT_NUMCHARS);
            }
        }
        if (SUPPORT_FILEFORMAT_FNT) {
            if (context.files.IsFileExtension(fileName, ".fnt")) {
                font = LoadBMFont(fileName);
            }
        }
        if (SUPPORT_FILEFORMAT_BDF) {
            if (context.files.IsFileExtension(fileName, ".bdf")) {
                font = LoadFontEx(fileName, FONT_TTF_DEFAULT_SIZE, null, FONT_TTF_DEFAULT_NUMCHARS);
            }
        }
        if (font == null) {
            Image image = context.textures.LoadImage(fileName);
            if (image.getData() != null) {
                font = LoadFontFromImage(image, Color.MAGENTA, FONT_TTF_DEFAULT_FIRST_CHAR);
            }
            else {
                font = GetFontDefault();
            }
            context.textures.UnloadImage(image);
        }

        if (font.texture.getId() == 0) {
            context.tracelog.TRACELOG(LOG_WARNING, "FONT: [" + fileName + "] Failed to load font texture -> Using default font");
            font = GetFontDefault();
        }
        else {
            context.textures.SetTextureFilter(font.texture, TEXTURE_FILTER_POINT); // By default we set point filter (best performance)
            context.tracelog.TRACELOG(LOG_INFO, "FONT: Data loaded successfully (" + font.baseSize + " pixel size | " + font.glyphCount + " glyphs)");
        }

        return font;
    }

    //
    // NOTE: You can pass an array with desired characters,

    /**
     * Load Font from TTF of BDF font file with generation parameters
     *
     * @param fileName       Name of font file to load, must be .fnt or .bdf format
     * @param fontSize       Default size of font
     * @param codepoints     {@code int[]} of codepoints to load. those characters should be available in the font. If the array is null, default char set is selected [32..126]
     * @param codepointCount Number of codepoints to load.
     * @return {@code Font} defined by font file
     */
    public Font LoadFontEx(String fileName, int fontSize, int[] codepoints, int codepointCount) {
        Font font = new Font();

        // Loading file to memory
        byte[] fileData = null;
        try {
            fileData = context.files.LoadFileData(fileName);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        if (fileData != null) {
            // Loading font from memory data
            font = LoadFontFromMemory(context.files.GetFileExtension(fileName), fileData, fontSize, codepoints, codepointCount);
        }

        return font;
    }

    /**
     * Load an Image font file (XNA style)
     *
     * @param image     Image containing font characters
     * @param key       Color used to exclude pixels from glyph images
     * @param firstChar codepoint of the fist glyph in the image
     * @return {@code Font} from pixel data
     */
    public Font LoadFontFromImage(Image image, Color key, int firstChar) {
        Font font = GetFontDefault();

        int charSpacing;
        int lineSpacing;

        int x = 0;
        int y;

        // Allocate a temporal arrays for glyphs data measures,
        // once the actual number of glyphs is obtained, copy data to a sized array
        int[] tempCharValues = new int[MAX_GLYPHS_FROM_IMAGE];
        Rectangle[] tempCharRecs = new Rectangle[MAX_GLYPHS_FROM_IMAGE];
        for (int i = 0; i < MAX_GLYPHS_FROM_IMAGE; i++) {
            tempCharRecs[i] = new Rectangle();
        }

        Color[] pixels = Color.FromPixels(context.textures.LoadImageColors(image));

        // Parse image data to get charSpacing and lineSpacing
        for (y = 0; y < image.getHeight(); y++) {
            for (x = 0; x < image.getWidth(); x++) {
                if (!ColorEqual(pixels != null ? pixels[y * image.getWidth() + x] : null, key)) {
                    break;
                }
            }

            if (!ColorEqual(pixels != null ? pixels[y * image.getWidth() + x] : null, key)) {
                break;
            }
        }

        if ((x == 0) || (y == 0)) {
            return font; // Security check
        }

        charSpacing = x;
        lineSpacing = y;

        int charHeight;
        int j = 0;

        while (((lineSpacing + j) < image.height) && !ColorEqual(pixels[(lineSpacing + j) * image.width + charSpacing], key)) {
            j++;
        }

        charHeight = j;

        // Check array values to get characters: value, x, y, w, h
        int index = 0;
        int lineToRead = 0;
        int xPosToRead = charSpacing;

        // Parse image data to get rectangle sizes
        while ((lineSpacing + lineToRead * (charHeight + lineSpacing)) < image.getHeight()) {
            while ((xPosToRead < image.getWidth()) &&
                    !ColorEqual((pixels[(lineSpacing + (charHeight + lineSpacing) * lineToRead) * image.getWidth() + xPosToRead]),
                                key)) {
                tempCharValues[index] = firstChar + index;

                tempCharRecs[index].x = (float) xPosToRead;
                tempCharRecs[index].y = (float) (lineSpacing + lineToRead * (charHeight + lineSpacing));
                tempCharRecs[index].height = (float) charHeight;

                int charWidth = 0;

                while (((xPosToRead + charWidth) < image.width) && !ColorEqual(pixels[(lineSpacing + (charHeight + lineSpacing) * lineToRead) * image.width + xPosToRead + charWidth], key)) {
                    charWidth++;
                }

                tempCharRecs[index].width = (float) charWidth;

                index++;

                xPosToRead += (charWidth + charSpacing);
            }

            lineToRead++;
            xPosToRead = charSpacing;
        }

        // NOTE: Key color borders need to be removed from image to avoid weird
        // artifacts on texture scaling when using TEXTURE_FILTER_BILINEAR or TEXTURE_FILTER_TRILINEAR
        for (int i = 0; i < image.getHeight() * image.getWidth(); i++) {
            if (ColorEqual(pixels[i], key)) {
                pixels[i] = Color.BLANK;
            }
        }

        // Create a new image with the processed color data (key color replaced by BLANK)
        Image fontClear = new Image(pixels, image.getWidth(), image.getHeight(), PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);

        // Set font with all data parsed from image
        font.texture = context.textures.LoadTextureFromImage(fontClear); // Convert processed image to OpenGL texture
        font.glyphCount = index;
        font.glyphPadding = 0;

        // Populate tempCharValues and tempCharsRecs with glyphs data
        // Move temp data to sized charValues and charRecs arrays
        font.glyphs = new GlyphInfo[font.glyphCount];
        for (int i = 0; i < font.glyphs.length; i++) {
            font.glyphs[i] = new GlyphInfo();
        }
        font.recs = new Rectangle[font.glyphCount];
        for (int i = 0; i < font.recs.length; i++) {
            font.recs[i] = new Rectangle();
        }

        for (int i = 0; i < font.glyphCount; i++) {
            font.glyphs[i].value = tempCharValues[i];

            // Get character rectangle in the font atlas texture
            font.recs[i] = tempCharRecs[i];

            // NOTE: On image based fonts (XNA style), character offsets and xAdvance are not required (set to 0)
            font.glyphs[i].offsetX = 0;
            font.glyphs[i].offsetY = 0;
            font.glyphs[i].advanceX = 0;

            // Fill character image data from fontClear data
            font.glyphs[i].image = context.textures.ImageFromImage(fontClear, tempCharRecs[i]);
        }

        context.textures.UnloadImage(fontClear);     // Unload processed image once converted to texture

        font.baseSize = (int) font.recs[0].height;

        return font;
    }

    /**
     * Load font from memory buffer
     *
     * @param fileType       File extension, used to determine data structure.
     * @param fileData       Buffer containing file data.
     * @param fontSize       Base size of the font, in pixels.
     * @param codepoints     Array of codepoints to be loaded from the font, pass {@code null} to load default codepoints.
     * @param codepointCount Number of codepoints to load, should be either {@code codepoints.length}, or {@code 0} if loading default codepoints.
     * @return {@code Font} from memory buffer.
     */
    public Font LoadFontFromMemory(String fileType, byte[] fileData, int fontSize, int[] codepoints, int codepointCount) {
        Font font = new Font();

        String fileExtLower = fileType.toLowerCase();

        font.baseSize = fontSize;
        font.glyphPadding = 0;

        if (SUPPORT_FILEFORMAT_TTF) {
            if (fileExtLower.equals(".ttf") || fileExtLower.equals(".otf")) {
                font.glyphs = LoadFontData(fileData, font.baseSize, codepoints, (codepointCount > 0) ? codepointCount : 95, FONT_DEFAULT);
            }
        }
        if (SUPPORT_FILEFORMAT_BDF) {
            if (fileExtLower.equals(".bdf")) {
                font.glyphs = LoadFontDataBDF(fileData, codepoints, (codepointCount > 0) ? codepointCount : 95);
            }
        }
        else {
            font.glyphs = null;
        }

        if (SUPPORT_FILEFORMAT_TTF || SUPPORT_FILEFORMAT_BDF) {
            if (font.glyphs != null) {
                font.glyphPadding = FONT_TTF_DEFAULT_CHARS_PADDING;

                Image atlas = GenImageFontAtlas(font, 0, 0);
                font.texture = context.textures.LoadTextureFromImage(atlas);

                // Update glyphs[i].image to use alpha, required to be used on ImageDrawText()
                for (int i = 0; i < font.glyphCount; i++) {
                    context.textures.UnloadImage(font.glyphs[i].image);
                    font.glyphs[i].image = context.textures.ImageFromImage(atlas, font.recs[i]);
                }

                context.textures.UnloadImage(atlas);

                context.tracelog.TRACELOG(LOG_INFO, "FONT: Data loaded successfully (%d pixel size | %d glyphs)", font.baseSize, font.glyphCount);
            }
            else {
                font = GetFontDefault();
            }
        }
        else {
            font = GetFontDefault();
        }

        return font;
    }

    /**
     * Check if a font is valid (font data loaded) <br/>
     * WARNING: GPU texture not checked
     *
     * @param font Font to be evaluated.
     * @return {@code true} if {@code font} is valid for use
     */
    public boolean IsFontValid(Font font) {
        return ((font.baseSize > 0) &&      // Validate font size
                (font.glyphCount > 0) &&    // Validate font contains some glyph
                (font.recs != null) &&      // Validate font recs defining glyphs on texture atlas
                (font.glyphs != null));     // Validate glyph data is loaded

        // NOTE: Further validations could be done to verify if recs and glyphs contain valid data (glyphs values, metrics...)
    }

    /**
     * Load font data for further use <br/>
     * NOTE: Requires TTF font memory data and can generate SDF data
     *
     * @param fileData       Buffer containing font data
     * @param fontSize       Base size of the font, in pixels
     * @param codepoints     Array of codepoints to be loaded from the font, pass {@code null} to load default codepoints.
     * @param codepointCount Number of codepoints to load, should be either {@code codepoints.length}, or {@code 0} if loading default codepoints.
     * @param type           {@code FontType} to define data loading
     * @return {@code GlyphInfo[]} defining glyphs for each value of {@code codepoints}
     * @see FontType
     */
    public GlyphInfo[] LoadFontData(byte[] fileData, int fontSize, int[] codepoints, int codepointCount, FontType type) {
        // NOTE: Using some SDF generation default values,
        // trades off precision with ability to handle *smaller* sizes

        final int FONT_SDF_CHAR_PADDING = 4;      // SDF font generation char padding
        final byte FONT_SDF_ON_EDGE_VALUE = (byte) 128;      // SDF font generation on edge value
        final float FONT_SDF_PIXEL_DIST_SCALE = 64.0f;     // SDF font generation pixel distance scale
        final int FONT_BITMAP_ALPHA_THRESHOLD = 80;      // Bitmap (B&W) font generation alpha threshold

        GlyphInfo[] glyphs = null;
        int glyphCounter = 0;

        if (SUPPORT_FILEFORMAT_TTF) {
            // Load font data (including pixel data) from TTF memory file
            // NOTE: Loaded information should be enough to generate font image atlas, using any packaging method
            if (fileData != null) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    ByteBuffer dataBuffer = ByteBuffer.allocateDirect(fileData.length);
                    dataBuffer.put(fileData).flip();
                    boolean genFontChars = false;
                    STBTTFontinfo fontInfo = STBTTFontinfo.create();

                    // TODO: Should a shallow copy be created to avoid "dealing" with a const user array?
                    int[] requiredCodepoints = codepoints;

                    // Initialize font for data reading
                    if (stbtt_InitFont(fontInfo, dataBuffer, 0)) {
                        // Calculate font scale factor
                        float scaleFactor = stbtt_ScaleForPixelHeight(fontInfo, (float) fontSize);

                        // Calculate font basic metrics
                        // NOTE: ascent is equivalent to font baseline
                        IntBuffer ascent = stack.callocInt(1);
                        IntBuffer descent = stack.callocInt(1);
                        IntBuffer lineGap = stack.callocInt(1);
                        stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);

                        // In case no chars count provided, default to 95
                        codepointCount = (codepointCount > 0) ? codepointCount : 95;

                        // Fill fontChars in case not provided externally
                        // NOTE: By default filling glyphCount consecutively, starting at 32 (Space)
                        if (requiredCodepoints == null) {
                            requiredCodepoints = new int[codepointCount];
                            for (int i = 0; i < codepointCount; i++) {
                                requiredCodepoints[i] = i + 32;
                            }
                            genFontChars = true;
                        }

                        // Check available glyphs on provided font before loading them
                        for (int i = 0, index; i < codepointCount; i++) {
                            index = stbtt_FindGlyphIndex(fontInfo, requiredCodepoints[i]);
                            if (index > 0) {
                                glyphCounter++;
                            }
                        }

                        // WARNING: Allocating space for maximum number of codepoints
                        glyphs = new GlyphInfo[glyphCounter];
                        for (int i = 0; i < glyphs.length; i++) {
                            glyphs[i] = new GlyphInfo();
                        }
                        glyphCounter = 0; // Reset to reuse

                        int k = 0;
                        for (int i = 0; i < codepointCount; i++) {
                            IntBuffer widthBuffer = stack.callocInt(1);
                            IntBuffer heightBuffer = stack.callocInt(1);
                            IntBuffer xOffsetBuffer = stack.callocInt(1);
                            IntBuffer yOffsetBuffer = stack.callocInt(1);
                            int cpWidth = 0, cpHeight = 0;   // Codepoint width and height (on generation)
                            int cp = requiredCodepoints[i];  // Codepoint value to get info for

                            //  Render a unicode codepoint to a bitmap
                            //      stbtt_GetCodepointBitmap()           -- allocates and returns a bitmap
                            //      stbtt_GetCodepointBitmapBox()        -- how big the bitmap must be
                            //      stbtt_MakeCodepointBitmap()          -- renders into a provided bitmap

                            // Check if a glyph is available in the font
                            // WARNING: if (index == 0), glyph not found, it could fallback to default .notdef glyph (if defined in font)
                            int index = stbtt_FindGlyphIndex(fontInfo, cp);

                            if (index > 0) {
                                // NOTE: Only storing glyphs for codepoints found in the font
                                glyphs[k].value = cp;

                                switch (type) {
                                    case FONT_DEFAULT:
                                    case FONT_BITMAP: {
                                        glyphs[k].image.data = stbtt_GetCodepointBitmap(fontInfo, scaleFactor, scaleFactor, cp, widthBuffer, heightBuffer, xOffsetBuffer, yOffsetBuffer);
                                        cpWidth = widthBuffer.get(0);
                                        cpHeight = heightBuffer.get(0);
                                        glyphs[k].offsetX = xOffsetBuffer.get(0);
                                        glyphs[k].offsetY = yOffsetBuffer.get(0);
                                    }
                                    break;
                                    case FONT_SDF: {
                                        if (cp != 32) {
                                            glyphs[k].image.data = stbtt_GetCodepointSDF(fontInfo, scaleFactor, cp, FONT_SDF_CHAR_PADDING, FONT_SDF_ON_EDGE_VALUE, FONT_SDF_PIXEL_DIST_SCALE, widthBuffer, heightBuffer, xOffsetBuffer, yOffsetBuffer);
                                            cpWidth = widthBuffer.get(0);
                                            cpHeight = heightBuffer.get(0);
                                            glyphs[k].offsetX = xOffsetBuffer.get(0);
                                            glyphs[k].offsetY = yOffsetBuffer.get(0);
                                        }
                                    }
                                    break;
                                    //case FONT_MSDF:
                                    default:
                                        break;
                                }

                                // Glyph data has been found in the font
                                if (glyphs[k].image.data != null) {
                                    IntBuffer xAdvanceBuffer = stack.callocInt(1);
                                    stbtt_GetCodepointHMetrics(fontInfo, cp, xAdvanceBuffer, null);
                                    glyphs[k].advanceX = (int) ((float) xAdvanceBuffer.get() * scaleFactor);

                                    // WARNING: If requested SDF font, sdf-glyph height is definitely bigger than fontSize due to FONT_SDF_CHAR_PADDING
                                    if ((type != FONT_SDF) && (cpHeight > fontSize)) {
                                        context.tracelog.TRACELOG(LOG_WARNING, "FONT: [0x%04x] Glyph height is bigger than requested font size: %i > %i", cp, cpHeight, (int) fontSize);
                                    }

                                    // Load glyph image
                                    glyphs[k].image.width = cpWidth;
                                    glyphs[k].image.height = cpHeight;
                                    glyphs[k].image.mipmaps = 1;
                                    glyphs[k].image.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;

                                    glyphs[k].offsetY += (int) ((float) ascent.get(0) * scaleFactor);
                                }
                                //else TRACELOG(LOG_WARNING, "FONT: Glyph [0x%08x] has no image data available", cp); // Only reported for 0x20 and 0x3000

                                // Create an empty image for Space character (0x20), useful for sprite font generation
                                // NOTE: Another space to consider: 0x3000 (CJK - Ideographic Space)
                                if ((cp == 0x20) || (cp == 0x3000)) {
                                    IntBuffer xAdvanceBuffer = stack.callocInt(1);
                                    stbtt_GetCodepointHMetrics(fontInfo, cp, xAdvanceBuffer, null);
                                    glyphs[k].advanceX = (int) ((float) xAdvanceBuffer.get(0) * scaleFactor);

                                    Image imSpace = new Image((byte[]) null, glyphs[k].advanceX, fontSize, PIXELFORMAT_UNCOMPRESSED_GRAYSCALE, 1);

                                    // Only allocate space image if required
                                    if (glyphs[k].advanceX > 0) {
                                        imSpace.setData(new byte[glyphs[k].advanceX * fontSize]);
                                    }
                                    else {
                                        glyphs[k].advanceX = 0;
                                    }

                                    glyphs[k].image = imSpace;
                                }

                                if (type == FONT_BITMAP) {
                                    // Aliased bitmap (black & white) font generation, avoiding anti-aliasing
                                    // NOTE: For optimum results, bitmap font should be generated at base pixel size
                                    for (int p = 0; p < cpWidth * cpHeight; p++) {
                                        if (glyphs[k].image.data.get(p) < FONT_BITMAP_ALPHA_THRESHOLD) {
                                            glyphs[k].image.data.put(p, (byte) 0);
                                        }
                                        else {
                                            glyphs[k].image.data.put(p, (byte) 255);
                                        }
                                    }
                                }

                                k++;
                                glyphCounter++;
                            }
                            else {
                                // WARNING: Glyph not found on font, optionally use a fallback glyph
                            }
                        }

                        if (glyphCounter < codepointCount) {
                            context.tracelog.TRACELOG(LOG_WARNING, "FONT: Requested codepoints glyphs found: [%i/%i]", k, codepointCount);
                        }
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "FONT: Failed to process TTF font data");
                    }

                    if (genFontChars) {
                        requiredCodepoints = null;
                    }
                }
            }
        }

        return glyphs;
    }

    //
    // NOTE: Packing method

    /**
     * Generate image font atlas using chars info
     *
     * @param font       {@code Font} to be used to generate atlas
     * @param padding    Pixels between glyphs
     * @param packMethod Packing method to use, either {@code 0} for Default, or {@code 1} for Skyline
     * @return {@code Image} containing an atlas of all loaded glyphs
     */
    public Image GenImageFontAtlas(Font font, int padding, int packMethod) {
        Image atlas = new Image();

        int fontSize = font.baseSize;
        Rectangle[] glyphRecs;
        int glyphCount = font.glyphCount;
        GlyphInfo[] glyphs = font.glyphs;

        if (font.glyphs == null) {
            context.tracelog.TRACELOG(LOG_WARNING, "FONT: Provided glyphs info not valid, returning empty image atlas");
            return atlas;
        }

        glyphRecs = null;

        // In case no chars count provided, suppose default of 95
        glyphCount = (glyphCount > 0) ? glyphCount : 95;

        // NOTE: Rectangles memory is loaded here!
        Rectangle[] recs = new Rectangle[glyphCount];
        for (int i = 0; i < recs.length; i++) {
            recs[i] = new Rectangle();
        }

        // Calculate image size based on total glyph width and glyph row count
        int totalWidth = 0;
        int maxGlyphWidth = 0;

        for (int i = 0; i < glyphCount; i++) {
            if (glyphs[i].image.width > maxGlyphWidth) {
                maxGlyphWidth = glyphs[i].image.width;
            }
            totalWidth += glyphs[i].image.width + 2 * padding;
        }

        int paddedFontSize = fontSize + 2 * padding;

        // Estimate image atlas size from available data
        // NOTE: Multiplying total expected area by 1.2f scale factor but in case
        // some glyphs do not fit, the atlas height is scaled x2 to fit them
        float totalArea = totalWidth * paddedFontSize * 1.2f;
        float imageMinSize = (float) Math.sqrt(totalArea);
        int imageSize = (int) Math.pow(2, Math.ceil(Math.log(imageMinSize) / Math.log(2)));

        if (totalArea < ((imageSize * imageSize) / 2)) {
            atlas.width = imageSize;    // Atlas bitmap width
            atlas.height = imageSize / 2; // Atlas bitmap height
        }
        else {
            atlas.width = imageSize;   // Atlas bitmap width
            atlas.height = imageSize;  // Atlas bitmap height
        }

        int atlasDataSize = atlas.width * atlas.height; // Save total size for bounds checking
        byte[] atlasData = new byte[atlasDataSize]; // Create a bitmap to store characters (8 bpp)
        atlas.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
        atlas.mipmaps = 1;

        // DEBUG: View padding in the generated image setting a gray background...
        //for (int i = 0; i < atlas.width*atlas.height; i++) ((unsigned char *)atlas.data)[i] = 100;

        // Use basic packing algorithm
        if (packMethod == 0) {
            int offsetX = padding;
            int offsetY = padding;

            // NOTE: Using simple packaging, one char after another
            for (int i = 0; i < glyphCount; i++) {
                // Check remaining space for glyph
                if (offsetX >= (atlas.width - glyphs[i].image.width - 2 * padding)) {
                    offsetX = padding;

                    // NOTE: Be careful on offsetY for SDF fonts, by default SDF
                    // use an internal padding of 4 pixels, it means char rectangle
                    // height is bigger than fontSize, it could be up to (fontSize + 8)
                    offsetY += (fontSize + 2 * padding);

                    if (offsetY > (atlas.height - fontSize - padding)) {
                        context.tracelog.TRACELOG(LOG_WARNING, "FONT: Updating atlas size to fit all characters");

                        // Update atlas size to fit all characters
                        int updatedAtlasHeight = atlas.height * 2;
                        int updatedAtlasDataSize = atlas.width * updatedAtlasHeight;
                        byte[] updatedAtlasData = new byte[updatedAtlasDataSize];
                        Arrays.fill(updatedAtlasData, (byte) 1);

                        System.arraycopy(atlasData, 0, updatedAtlasData, 0, atlasData.length);
                        atlasData = updatedAtlasData;
                        atlas.height = updatedAtlasHeight;
                        atlasDataSize = updatedAtlasDataSize;
                    }
                }

                // Copy pixel data from glyph image to atlas
                for (int y = 0; y < glyphs[i].image.height; y++) {
                    for (int x = 0; x < glyphs[i].image.width; x++) {
                        int destX = offsetX + x;
                        int destY = offsetY + y;

                        // Security: check both lower and upper bounds
                        if ((destX >= 0) && (destX < atlas.width) && (destY >= 0) && (destY < atlas.height)) {
                            atlasData[destY * atlas.width + destX] = glyphs[i].image.data.get(y * glyphs[i].image.width + x);
                        }
                    }
                }

                // Fill chars rectangles in atlas info
                recs[i].x = (float) offsetX;
                recs[i].y = (float) offsetY;
                recs[i].width = (float) glyphs[i].image.width;
                recs[i].height = (float) glyphs[i].image.height;

                // Move atlas position X for next character drawing
                offsetX += (glyphs[i].image.width + 2 * padding);
            }
        }
        // Use Skyline rect packing algorithm (stb_pack_rect)
        else if (packMethod == 1) {
            STBRPContext stbrpContext = STBRPContext.create();

            ByteBuffer nBB = ByteBuffer.allocateDirect(font.glyphCount * STBRPNode.SIZEOF);
            STBRPNode.Buffer nodes = new STBRPNode.Buffer(nBB);

            for (int i = 0; i < font.glyphCount; i++) {
                nodes.put(i, STBRPNode.create());
            }

            STBRectPack.stbrp_init_target(stbrpContext, atlas.width, atlas.height, nodes);

            ByteBuffer rBB = ByteBuffer.allocateDirect(font.glyphCount * STBRPRect.SIZEOF);
            STBRPRect.Buffer rects = new STBRPRect.Buffer(rBB);

            for (int i = 0; i < font.glyphCount; i++) {
                rects.put(i, STBRPRect.create());
            }

            // Fill rectangles for packaging
            for (int i = 0; i < font.glyphCount; i++) {
                rects.get(i).id(i);
                rects.get(i).w((short) (font.glyphs[i].image.width + 2 * font.glyphPadding));
                rects.get(i).h((short) (font.glyphs[i].image.height + 2 * font.glyphPadding));
            }

            // Package rectangles into atlas
            STBRectPack.stbrp_pack_rects(stbrpContext, rects);

            for (int i = 0; i < font.glyphCount; i++) {
                // It returns char rectangles in atlas
                recs[i].x = rects.get(i).x() + font.glyphPadding;
                recs[i].y = rects.get(i).y() + font.glyphPadding;
                recs[i].width = font.glyphs[i].image.width;
                recs[i].height = font.glyphs[i].image.height;

                if (rects.get(i).was_packed()) {
                    byte[] fcData = font.glyphs[i].image.getData();
                    // Copy pixel data from fc.data to atlas
                    for (int y = 0; y < font.glyphs[i].image.height; y++) {
                        for (int x = 0; x < font.glyphs[i].image.width; x++) {
                            atlasData[(rects.get(i).y() + font.glyphPadding + y) * atlas.width + (rects.get(i).x() + font.glyphPadding + x)] =
                                    fcData[y * font.glyphs[i].image.width + x];
                        }
                    }
                }
                else {
                    context.tracelog.TRACELOG(LOG_WARNING, "FONT: Failed to package character (" + i + ")");
                }
            }
        }

        // Add a 3x3 white rectangle at the bottom-right corner of the generated atlas,
        // useful to use as the white texture to draw shapes with raylib
        // Security: ensure the atlas is large enough to hold a 3x3 rectangle
        if ((FONT_ATLAS_CORNER_REC_SIZE > 0) && (atlas.width >= 3) && (atlas.height >= 3)) {
            for (int i = 0, k = atlas.width * atlas.height - 1; i < FONT_ATLAS_CORNER_REC_SIZE; i++) {
                atlasData[k - 0] = (byte) 255;
                atlasData[k - 1] = (byte) 255;
                atlasData[k - 2] = (byte) 255;
                k -= atlas.width;
            }
        }

        // Convert image data from GRAYSCALE to GRAY_ALPHA
        byte[] dataGrayAlpha = new byte[atlas.width * atlas.height * 2]; // Two channels

        for (int i = 0, k = 0; i < atlas.width * atlas.height; i++, k += 2) {
            dataGrayAlpha[k] = (byte) 255;
            dataGrayAlpha[k + 1] = atlasData[i];
        }

        atlas.setData(dataGrayAlpha);
        atlas.format = PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;

        font.recs = recs;

        return atlas;
    }

    /**
     * Unload font glyphs info data (RAM)
     *
     * @param glyphs Glyph data to unload
     */
    @Contract(mutates = "param")
    public void UnloadFontData(GlyphInfo[] glyphs) {
        if (glyphs != null) {
            for (int i = 0; i < glyphs.length; i++) {
                context.textures.UnloadImage(glyphs[i].image);
            }
            glyphs = null;
        }
    }

    /**
     * Unload Font from GPU memory (VRAM)
     *
     * @param font {@code Font} to unload
     */
    public void UnloadFont(Font font) {
        // NOTE: Make sure font is not default font (fallback)
        if (font.texture.id != GetFontDefault().texture.id) {
            UnloadFontData(font.glyphs);
            context.textures.UnloadTexture(font.texture);
            font.recs = null;

            context.tracelog.TRACELOG(LOG_DEBUG, "FONT: Unloaded font data from RAM and VRAM");
        }
    }

    /**
     * Export font as code file
     *
     * @param font     Font to export
     * @param fileName Location to save file data
     * @return {@code true} on successful file operation
     */
    public boolean ExportFontAsCode(Font font, String fileName) {
        boolean result = false;

        int TEXT_BYTES_PER_LINE = 20;
        int MAX_FONT_DATA_SIZE = 1024 * 1024;

        // Get file name from path
        String fileNamePascal = TextToPascal(context.files.GetFileNameWithoutExt(fileName));

        // Get font atlas image and size, required to estimate code file size
        // NOTE: This mechanism is highly coupled to raylib
        Image image = context.textures.LoadImageFromTexture(font.texture);
        if (image.format != PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA) {
            context.tracelog.TRACELOG(LOG_WARNING, "Font export as code: Font image format is not GRAY+ALPHA!");
        }
        int imageDataSize = context.textures.GetPixelDataSize(image.width, image.height, image.format);

        StringBuilder txtData = new StringBuilder(MAX_FONT_DATA_SIZE);

        int byteCount = 0;
        txtData.append("////////////////////////////////////////////////////////////////////////////////////////\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// FontAsCode exporter v1.0 - Font data exported as an array of bytes                 //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// more info and bugs-report:  github.com/raysan5/raylib                              //\n");
        txtData.append("// feedback and support:       ray[at]raylib.com                                      //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// Copyright (c) 2018-2026 Ramon Santamaria (@raysan5)                                //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// ---------------------------------------------------------------------------------- //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// TODO: Fill the information and license of the exported font here:                  //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// Font name:    ....                                                                 //\n");
        txtData.append("// Font creator: ....                                                                 //\n");
        txtData.append("// Font LICENSE: ....                                                                 //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("////////////////////////////////////////////////////////////////////////////////////////\n\n");
        byteCount = txtData.length();

        boolean SUPPORT_COMPRESSED_FONT_ATLAS = true;

        if (SUPPORT_COMPRESSED_FONT_ATLAS) {
            // WARNING: Data is compressed using raylib CompressData() DEFLATE,
            // it requires to be decompressed with raylib DecompressData(), that requires
            // compiling raylib with SUPPORT_COMPRESSION_API config flag enabled

            // Compress font image data
            int compDataSize = 0;
            byte[] compData = context.core.CompressData(image.getData());

            // Save font image data (compressed)
            txtData.append("#define COMPRESSED_DATA_SIZE_FONT_" + TextToUpper(fileNamePascal) + " compDataSize" + "\n\n");
            txtData.append("// Font image pixels data compressed (DEFLATE)\n");
            txtData.append("// NOTE: Original pixel data simplified to GRAYSCALE\n");
            txtData.append("static unsigned char fontData_" + fileNamePascal + "[COMPRESSED_DATA_SIZE_FONT_" + TextToUpper(fileNamePascal) + "] = { ");

            for (int i = 0; i < compDataSize - 1; i++) {
                txtData.append((i % TEXT_BYTES_PER_LINE == 0) ? "0x" + String.format("%02x", compData[i]) + ",\n    " : "0x" + String.format("%02x", compData[i]) + ", ");
            }

            txtData.append("0x" + compData[compDataSize - 1] + " };\n\n");
        }
        else {
            // Save font image data (uncompressed)
            txtData.append("// Font image pixels data\n");
            txtData.append("// NOTE: 2 bytes per pixel, GRAY + ALPHA channels\n");
            txtData.append("static unsigned char fontImageData_" + fileNamePascal + "[" + imageDataSize + "] = { ");
            for (int i = 0; i < imageDataSize - 1; i++) {
                txtData.append((i % TEXT_BYTES_PER_LINE == 0) ? "0x" + String.format("%02x", image.getData()[i]) + ",\n    " : "0x" + String.format("%02x", image.getData()[i]) + ", ");
            }
            txtData.append("0x" + String.format("%02x", image.getData()[imageDataSize - 1]) + " };\n\n");
        }

        // Save font recs data
        txtData.append("// Font characters rectangles data\n");
        txtData.append("static const Rectangle fontRecs_" + fileNamePascal + "[" + font.glyphCount + "] = {\n");
        for (int i = 0; i < font.glyphCount; i++) {
            txtData.append("    { " + String.format("%1.0f", font.recs[i].x) + ", " + String.format("%1.0f", font.recs[i].y) + ", " + String.format("%1.0f", font.recs[i].width) + " , " + String.format("%1.0f", font.recs[i].height) + " },\n");
        }
        txtData.append("};\n\n");

        // Save font glyphs data
        // NOTE: Glyphs image data not saved (grayscale pixels),
        // it could be generated from image and recs
        txtData.append("// Font glyphs info data\n");
        txtData.append("// NOTE: No glyphs.image data provided\n");
        txtData.append("static const GlyphInfo fontGlyphs_" + fileNamePascal + "[" + font.glyphCount + "] = {\n");
        for (int i = 0; i < font.glyphCount; i++) {
            txtData.append("    { " + font.glyphs[i].value + ", " + font.glyphs[i].offsetX + ", " + font.glyphs[i].offsetY + ", " + font.glyphs[i].advanceX + ", { 0 }},\n");
        }
        txtData.append("};\n\n");

        // Custom font loading function
        txtData.append("// Font loading function: " + fileNamePascal + "\n");
        txtData.append("static Font LoadFont_" + fileNamePascal + "(void)\n{\n");
        txtData.append("    Font font = { 0 };\n\n");
        txtData.append("    font.baseSize = " + font.baseSize + ";\n");
        txtData.append("    font.glyphCount = " + font.glyphCount + ";\n");
        txtData.append("    font.glyphPadding = " + font.glyphPadding + ";\n\n");
        txtData.append("    // Custom font loading\n");
        if (SUPPORT_COMPRESSED_FONT_ATLAS) {
            txtData.append("    // NOTE: Compressed font image data (DEFLATE), it requires DecompressData() function\n");
            txtData.append("    int fontDataSize_" + fileNamePascal + " = 0;\n");
            txtData.append("    unsigned char *data = DecompressData(fontData_" + fileNamePascal + ", COMPRESSED_DATA_SIZE_FONT_" + TextToUpper(fileNamePascal) + ", &fontDataSize_" + fileNamePascal + ");\n");
            txtData.append("    Image imFont = { data, " + image.width + ", " + image.height + ", 1, " + image.format + " };\n\n");
        }
        else {
            txtData.append("    Image imFont = { fontImageData_" + fileName + ", " + image.width + ", " + image.height + ", 1, " + image.format + " };\n\n");
        }

        txtData.append("    // Load texture from image\n");
        txtData.append("    font.texture = LoadTextureFromImage(imFont);\n");

        if (SUPPORT_COMPRESSED_FONT_ATLAS) {
            txtData.append("    UnloadImage(imFont);  // Uncompressed data can be unloaded from memory\n\n");
        }

        // We have two possible mechanisms to assign font.recs and font.glyphs data,
        // that data is already available as global arrays, we two options to assign that data:
        //  - 1. Data copy. This option consumes more memory and Font MUST be unloaded by user, requiring additional code.
        //  - 2. Data assignment. This option consumes less memory and Font MUST NOT be unloaded by user because data is on protected DATA segment
        boolean SUPPORT_FONT_DATA_COPY = false;

        if (SUPPORT_FONT_DATA_COPY) {
            txtData.append("    // Copy glyph recs data from global fontRecs\n");
            txtData.append("    // NOTE: Required to avoid issues if trying to free font\n");
            txtData.append("    font.recs = (Rectangle *)malloc(font.glyphCount*sizeof(Rectangle));\n");
            txtData.append("    memcpy(font.recs, fontRecs_" + fileNamePascal + ", font.glyphCount*sizeof(Rectangle));\n\n");

            txtData.append("    // Copy font glyph info data from global codepoints\n");
            txtData.append("    // NOTE: Required to avoid issues if trying to free font\n");
            txtData.append("    font.glyphs = (GlyphInfo *)malloc(font.glyphCount*sizeof(GlyphInfo));\n");
            txtData.append("    memcpy(font.glyphs, fontGlyphs_" + fileNamePascal + ", font.glyphCount*sizeof(GlyphInfo));\n\n");
        }
        else {
            txtData.append("    // Assign glyph recs and info data directly\n");
            txtData.append("    // WARNING: This font data must not be unloaded\n");
            txtData.append("    font.recs = fontRecs_" + fileNamePascal + ";\n");
            txtData.append("    font.glyphs = fontGlyphs_" + fileNamePascal + ";\n\n");
        }

        txtData.append("    return font;\n");
        txtData.append("}\n");

        context.textures.UnloadImage(image);

        // NOTE: Text data size exported is determined by '\0' (NULL) character
        try {
            result = context.files.SaveFileText(fileName, txtData.toString());
            context.tracelog.TRACELOG(LOG_INFO, "FILEIO: [" + fileName + "] Font as code exported successfully");
        }
        catch (IOException e) {
            result = false;
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export font as code");
        }

        return result;
    }

    /**
     * Draw current FPS using default font
     *
     * @param posX X position to draw text, with respect to the upper-left-hand corner
     * @param posY Y position to draw text, with respect to the upper-left-hand corner
     */
    public void DrawFPS(int posX, int posY) {
        Color color = Color.LIME; // Good fps
        int fps = context.core.GetFPS();

        if (fps < 30 && fps >= 15) {
            color = Color.ORANGE;  // Warning FPS
        }
        else if (fps < 15) {
            color = Color.RED;    // Low FPS
        }

        DrawText(TextFormat("%2d FPS", fps), posX, posY, 20, color);
    }

    /**
     * Draw current FPS using default font
     *
     * @param posX      X position to draw text, with respect to the upper-left-hand corner
     * @param posY      Y position to draw text, with respect to the upper-left-hand corner
     * @param textColor Color to draw text
     */
    public void DrawFPS(int posX, int posY, Color textColor) {
        int fps = context.core.GetFPS();
        DrawText(TextFormat("%2d FPS", fps), posX, posY, 20, textColor);
    }

    /**
     * Draw text using the default font
     *
     * @param text     String to draw
     * @param posX     X position to draw text, with respect to the upper-left-hand corner
     * @param posY     Y position to draw text, with respect to the upper-left-hand corner
     * @param fontSize Size to draw text in pixels. If {@code fontSize} is less than the default font's {@code baseSize} then {@code baseSize} is used.
     * @param color    Color to draw text
     */
    public void DrawText(String text, int posX, int posY, int fontSize, Color color) {
        // Check if default font has been loaded
        if (GetFontDefault().texture.getId() != 0) {
            Vector2 position = new Vector2(posX, posY);

            int defaultFontSize = 10;   // Default Font chars height in pixel
            if (fontSize < defaultFontSize) {
                fontSize = defaultFontSize;
            }
            int spacing = fontSize / defaultFontSize;

            DrawTextEx(GetFontDefault(), text, position, (float) fontSize, (float) spacing, color);
        }
    }

    /**
     * Draw text using a specified font
     *
     * @param font     Font to use when drawing text
     * @param text     String to draw
     * @param position (x, y) coordinate pair where text is to be drawn, with respect to the upper-left-hand corner
     * @param fontSize Size to draw text in pixels. If {@code fontSize} is less than the default font's {@code baseSize} then {@code baseSize} is used.
     * @param spacing  Space between characters
     * @param tint     Color to draw text
     */
    public void DrawTextEx(Font font, String text, Vector2 position, float fontSize, float spacing, Color tint) {

        if (font.texture.id == 0) {
            font = GetFontDefault();  // Security check in case of not valid font
        }

        int length = TextLength(text);

        float textOffsetY = 0;            // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;       // Offset X to next character to draw

        float scaleFactor = fontSize / font.baseSize;     // Character quad scaling factor

        for (int i = 0; i < length; ) {
            // Get next codepoint from byte string and glyph index in font
            int codepoint = GetCodepointNext(text, i);
            int codepointByteCount = GetCodePointByteCount(codepoint);
            int index = GetGlyphIndex(font, codepoint);

            if (codepoint == '\n') {
                // NOTE: Line spacing is a global variable, use SetTextLineSpacing() to set up
                textOffsetY += (fontSize + textLineSpacing);
                textOffsetX = 0.0f;
            }
            else {
                if ((codepoint != ' ') && (codepoint != '\t')) {
                    DrawTextCodepoint(font, codepoint, new Vector2(position.getX() + textOffsetX,
                                                                   position.getY() + textOffsetY), fontSize, tint);
                }

                if (font.glyphs[index].advanceX == 0) {
                    textOffsetX += (font.recs[index].getWidth() * scaleFactor + spacing);
                }
                else {
                    textOffsetX += ((float) font.glyphs[index].advanceX * scaleFactor + spacing);
                }
            }

            i += codepointByteCount;   // Move text bytes counter to next codepoint
        }
    }

    /**
     * Draw text using pro parameters
     *
     * @param font     Font to use when drawing text
     * @param text     String to draw
     * @param position (x, y) coordinate pair where text is to be drawn, with respect to the upper-left-hand corner
     * @param origin   (x, y) coordinate pair defining the center point of the text
     * @param rotation Degrees to rotate about {@code center}
     * @param fontSize Size to draw text in pixels. If {@code fontSize} is less than the default font's {@code baseSize} then {@code baseSize} is used.
     * @param spacing  Space between characters
     * @param tint     Color to draw text
     */
    public void DrawTextPro(Font font, String text, Vector2 position, Vector2 origin, float rotation, float fontSize,
                            float spacing, Color tint) {
        context.rlgl.rlPushMatrix();

        context.rlgl.rlTranslatef(position.x, position.y, 0.0f);
        context.rlgl.rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
        context.rlgl.rlTranslatef(-origin.x, -origin.y, 0.0f);

        DrawTextEx(font, text, new Vector2(), fontSize, spacing, tint);

        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw one character (i.e. codepoint)
     *
     * @param font      Font to use when drawing codepoint
     * @param codepoint codepoint to draw
     * @param position  (x, y) coordinate pair where codepoint is to be drawn, with respect to the upper-left-hand corner
     * @param fontSize  Size to draw codepoint in pixels. If {@code fontSize} is less than the default font's {@code baseSize} then {@code baseSize} is used.
     * @param tint      Color to draw codepoint
     */
    public void DrawTextCodepoint(Font font, int codepoint, Vector2 position, float fontSize, Color tint) {
        // Character index position in sprite font
        // NOTE: In case a codepoint is not available in the font, index returned points to '?'
        int index = GetGlyphIndex(font, codepoint);
        float scaleFactor = fontSize / font.baseSize;     // Character quad scaling factor

        // Character destination rectangle on screen
        // NOTE: Considering glyph padding on drawing
        Rectangle dstRec = new Rectangle(
                position.getX() + font.glyphs[index].offsetX * scaleFactor - (float) font.glyphPadding * scaleFactor,
                position.getY() + font.glyphs[index].offsetY * scaleFactor - (float) font.glyphPadding * scaleFactor,
                (font.recs[index].getWidth() + 2.0f * font.glyphPadding) * scaleFactor,
                (font.recs[index].getHeight() + 2.0f * font.glyphPadding) * scaleFactor);

        // Character source rectangle from font texture atlas
        // NOTE: Considering glyphs padding when drawing, it could be required for outline/glow shader effects
        Rectangle srcRec = new Rectangle(
                font.recs[index].getX() - (float) font.glyphPadding,
                font.recs[index].getY() - (float) font.glyphPadding,
                font.recs[index].getWidth() + 2.0f * font.glyphPadding,
                font.recs[index].getHeight() + 2.0f * font.glyphPadding);

        // Draw the character texture on the screen
        context.textures.DrawTexturePro(font.texture, srcRec, dstRec, new Vector2(), 0.0f, tint);
    }

    /**
     * Draw multiple characters (i.e. codepoints)
     *
     * @param font       Font to use when drawing codepoints
     * @param codepoints Array of codepoints to draw
     * @param position   (x, y) coordinate pair where codepoint is to be drawn, with respect to the upper-left-hand corner
     * @param fontSize   Size to draw codepoint in pixels. If {@code fontSize} is less than the default font's {@code baseSize} then {@code baseSize} is used.
     * @param spacing    Space between codepoints
     * @param tint       Color to draw codepoint
     */
    public void DrawTextCodepoints(Font font, int[] codepoints, Vector2 position, float fontSize, float spacing, Color tint) {
        float textOffsetY = 0;            // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;       // Offset X to next character to draw
        float scaleFactor = fontSize / font.baseSize;         // Character quad scaling factor

        for (int i = 0; i < codepoints.length; i++) {
            int index = GetGlyphIndex(font, codepoints[i]);
            if (codepoints[i] == '\n') {
                // NOTE: Line spacing is a global variable, use SetTextLineSpacing() to setup
                textOffsetY += (fontSize + textLineSpacing);
                textOffsetX = 0.0f;
            }
            else {
                if ((codepoints[i] != ' ') && (codepoints[i] != '\t')) {
                    DrawTextCodepoint(font, codepoints[i], new Vector2(position.x + textOffsetX, position.y + textOffsetY), fontSize, tint);
                }

                if (font.glyphs[index].advanceX == 0) {
                    textOffsetX += (font.recs[index].width * scaleFactor + spacing);
                }
                else {
                    textOffsetX += ((float) font.glyphs[index].advanceX * scaleFactor + spacing);
                }
            }
        }
    }

    /**
     * Set vertical line spacing when drawing with line-breaks
     *
     * @param spacing Space between lines, in pixels
     */
    public void SetTextLineSpacing(int spacing) {
        textLineSpacing = spacing;
    }

    /**
     * Measure string width for default font
     *
     * @param text     String to measure
     * @param fontSize Size of characters
     * @return Length of {@code text} in pixels
     */
    public int MeasureText(String text, int fontSize) {
        Vector2 vec = new Vector2();

        // Check if default font has been loaded
        if (GetFontDefault().texture.getId() != 0) {
            int defaultFontSize = 10;   // Default Font glyph height in pixels
            if (fontSize < defaultFontSize) {
                fontSize = defaultFontSize;
            }
            int spacing = fontSize / defaultFontSize;

            vec = MeasureTextEx(GetFontDefault(), text, (float) fontSize, (float) spacing);
        }

        return (int) vec.getX();
    }

    // Measure string size for Font

    /**
     * Measure string size
     *
     * @param font     Font to use when measuring characters
     * @param text     String to measure
     * @param fontSize Size of characters
     * @param spacing  Space between characters
     * @return (Width, Height) pair as (X, Y) of a {@code Vector2}
     */
    public Vector2 MeasureTextEx(Font font, String text, float fontSize, float spacing) {
        Vector2 textSize = new Vector2();

        // Security check
        if ((font.texture.id == 0) || (text == null) || text.isEmpty()) {
            return textSize;
        }

        int size = text.length();       // Get size in bytes of text
        int tempByteCounter = 0;        // Used to count longer text line num chars
        int byteCounter = 0;

        float textWidth = 0.0f;
        float tempTextWidth = 0.0f;     // Used to count longer text line width

        float textHeight = fontSize;
        float scaleFactor = fontSize / (float) font.baseSize;

        int letter = 0;                 // Current character
        int index = 0;                  // Index position in sprite font

        for (int i = 0; i < size; ) {
            byteCounter++;

            int codepointByteCount = GetCodePointByteCount(text.charAt(i));
            letter = GetCodepointNext(text, i);
            index = GetGlyphIndex(font, letter);

            i += codepointByteCount;

            if (letter != '\n') {
                if (font.glyphs[index].advanceX > 0) {
                    textWidth += font.glyphs[index].advanceX;
                }
                else {
                    textWidth += (font.recs[index].width + font.glyphs[index].offsetX);
                }
            }
            else {
                if (tempTextWidth < textWidth) {
                    tempTextWidth = textWidth;
                }
                byteCounter = 0;
                textWidth = 0;

                // NOTE: Line spacing is a global variable, use SetTextLineSpacing() to setup
                textHeight += (fontSize + textLineSpacing);
            }

            if (tempByteCounter < byteCounter) {
                tempByteCounter = byteCounter;
            }
        }

        if (tempTextWidth < textWidth) {
            tempTextWidth = textWidth;
        }

        textSize.x = tempTextWidth * scaleFactor + ((tempByteCounter - 1) * spacing);
        textSize.y = textHeight;

        return textSize;
    }

    /**
     * Measure string size for an array of codepoints
     *
     * @param font       Font to use when measuring codepoints
     * @param codepoints array of codepoints to measure
     * @param fontSize   size of characters
     * @param spacing    space between characters
     * @return (Width, Height) pair as (X, Y) of a {@code Vector2}
     */
    public Vector2 MeasureTextCodepoints(Font font, int[] codepoints, float fontSize, float spacing) {
        Vector2 textSize = new Vector2();

        // Security check
        if ((font.texture.id == 0) || (codepoints == null) || (codepoints.length == 0)) {
            return textSize;
        }

        float textWidth = 0.0f;
        // Used to count longer text line width
        float tempTextWidth = 0.0f;

        // Used to count longer text line num chars
        int tempGlyphCounter = 0;
        int glyphCounter = 0;

        float textHeight = fontSize;
        float scaleFactor = fontSize / (float) font.baseSize;

        // Current character
        int letter = 0;
        // Index position in sprite font
        int index = 0;

        for (int i = 0; i < codepoints.length; i++) {
            letter = codepoints[i];
            index = GetGlyphIndex(font, letter);

            if (letter != '\n') {
                glyphCounter++;

                if (font.glyphs[index].advanceX > 0) {
                    textWidth += font.glyphs[index].advanceX;
                }
                else {
                    textWidth += (font.recs[index].width + font.glyphs[index].offsetX);
                }
            }
            else {
                if (tempTextWidth < textWidth) {
                    tempTextWidth = textWidth;
                }

                textWidth = 0;
                glyphCounter = 0;

                // NOTE: Line spacing is a global variable, use SetTextLineSpacing() to setup
                textHeight += (fontSize + textLineSpacing);
            }

            if (tempGlyphCounter < glyphCounter) {
                tempGlyphCounter = glyphCounter;
            }
        }

        if (tempTextWidth < textWidth) {
            tempTextWidth = textWidth;
        }

        textSize.x = tempTextWidth * scaleFactor + (float) ((tempGlyphCounter - 1) * spacing);
        textSize.y = textHeight;

        return textSize;
    }

    /**
     * Returns index position for a Unicode character in spritefont
     *
     * @param font      Font to query
     * @param codepoint codepoint to locate
     * @return index of {@code codepoint} in {@code font}
     */
    public int GetGlyphIndex(Font font, int codepoint) {
        int index = 0;
        int fallbackIndex = 0;      // Get index of fallback glyph '?'

        if (!IsFontValid(font)) {
            return index;
        }

        // Look for character index in the unordered charset
        for (int i = 0; i < font.glyphCount; i++) {
            if (font.glyphs[i].value == 63) {
                fallbackIndex = i;
            }

            if (font.glyphs[i].value == codepoint) {
                index = i;
                break;
            }
        }

        if ((index == 0) && (font.glyphs[0].value != codepoint)) {
            index = fallbackIndex;
        }

        return index;
    }

    /**
     * Get glyph font info data for a codepoint (unicode character)
     *
     * @param font      Font to query
     * @param codepoint codepoint to locate
     * @return {@code GlyphInfo} for {@code codepoint}. If codepoint is not found in the font it fallbacks to '?'
     */
    GlyphInfo GetGlyphInfo(Font font, int codepoint) {
        return font.glyphs[GetGlyphIndex(font, codepoint)];
    }

    /**
     * Get glyph rectangle in font atlas for a codepoint (unicode character)
     *
     * @param font      Font to query
     * @param codepoint codepoint to locate
     * @return {@code Rectangle} defining {@code codepoint}. If codepoint is not found in the font it fallbacks to '?'
     */
    Rectangle GetGlyphAtlasRec(Font font, int codepoint) {
        return font.recs[GetGlyphIndex(font, codepoint)];
    }

    //----------------------------------------------------------------------------------
    // Text strings management functions
    //----------------------------------------------------------------------------------


    /**
     * Get text length in bytes
     *
     * @param text text to be evaluated
     * @return length of string in bytes
     */
    public int TextLength(String text) {
        return text.getBytes().length;
    }

    /**
     * Formatting of text with variables to 'embed'
     *
     * @param format a format string
     * @param args   arguments referenced by {@code format}
     * @return Formatted text
     * @see String#format(String, Object...)
     */
    public String TextFormat(String format, Object... args) {
        return String.format(format, args);
    }

    /**
     * Get integer value from text
     *
     * @param text String representation of an {@code int}
     * @return {@code int} defined by {@code String}
     * @see Integer#parseInt(String)
     */
    public int TextToInteger(String text) {
        return Integer.parseInt(text);
    }

    //TextCopy
    //can't really copy from one memory address to another...

    /**
     * Check if two text string are equal
     *
     * @param text1
     * @param text2
     * @return {true} if {@code text1} equals {@code text2}
     * @see String#equals(Object)
     */
    public boolean TextIsEqual(String text1, String text2) {
        return text1.equals(text2);
    }

    /**
     * Get a piece of a text string
     *
     * @param text
     * @param position
     * @param length
     * @return
     * @see String#substring(int, int)
     */
    public String TextSubtext(String text, int position, int length) {
        if (length < text.length()) {
            return text.substring(position, length);
        }
        else {
            return text.substring(position);
        }
    }

    /**
     * Replace text string
     *
     * @param text
     * @param replace
     * @param by
     * @return
     * @see String#replace(CharSequence, CharSequence)
     */
    public String TextReplace(String text, String replace, String by) {
        return text.replace(replace, by);
    }

    /**
     * Insert text in a specific position and move remaining text forward
     *
     * @param text
     * @param insert
     * @param position
     * @return
     */
    public String TextInsert(String text, String insert, int position) {
        String result;

        result = text.substring(0, position);
        result += insert;
        result += text.substring(position);

        return result;
    }

    /**
     * Join text strings with a delimiter
     *
     * @param text      {@code String[]} array to join
     * @param delimiter Character used to separate strings
     * @return Strings of {@code text} spliced with {@code delimiter}
     */
    public String TextJoin(String[] text, String delimiter) {
        StringBuilder result = new StringBuilder();

        for (String s : text) {
            result.append(s);
            result.append(delimiter);
        }

        return result.toString();
    }

    /**
     * Split String into multiple Strings
     *
     * @param text
     * @param delimiter
     * @return
     * @see String#split(String)
     */
    public String[] TextSplit(String text, String delimiter) {
        return text.split(delimiter);
    }

    /**
     * Append text to the end of a string.
     *
     * @param text
     * @param append
     * @return
     */
    public String TextAppend(String text, String append) {
        return text + append;
    }

    /**
     * Find first text occurrence within a string
     *
     * @param text
     * @param find
     * @return
     * @see String#indexOf(int)
     */
    public int TextFindIndex(String text, String find) {
        return text.indexOf(find);
    }

    /**
     * Get upper case version of provided string
     *
     * @param text
     * @return {@code text} AS AN UPPERCASED STRING
     * @see String#toUpperCase()
     */
    public String TextToUpper(String text) {
        return text.toUpperCase();
    }

    /**
     * Get lower case version of provided string
     *
     * @param text
     * @return {@code text} as a lowercased string
     * @see String#toLowerCase()
     */
    public String TextToLower(String text) {
        return text.toLowerCase();
    }

    /**
     * Get Pascal case notation version of provided string
     *
     * @param text
     * @return {@code text} AsAPascalCasedString
     */
    public String TextToPascal(String text) {
        char[] buffer = new char[MAX_TEXT_BUFFER_LENGTH];

        buffer[0] = text.toUpperCase().charAt(0);

        for (int i = 1, j = 1; i < MAX_TEXT_BUFFER_LENGTH; i++, j++) {
            if (text.charAt(j) != '\0') {
                if (text.charAt(j) != '_') {
                    buffer[i] = text.charAt(j);
                }
                else {
                    j++;
                    buffer[i] = text.toUpperCase().charAt(j);
                }
            }
            else {
                buffer[i] = '\0';
                break;
            }
        }

        return Arrays.toString(buffer);
    }

    /**
     * Encode text codepoint into UTF-8 text
     *
     * @param codepoints
     * @param length
     * @return
     */
    public String LoadUTF8(int[] codepoints, int length) {
        // We allocate enough memory to fit all possible codepoints
        // NOTE: 5 bytes for every codepoint should be enough
        StringBuilder text = new StringBuilder();
        String utf8;
        int size = 0;

        for (int i = 0, bytes; i < length; i++) {
            utf8 = CodepointToUTF8(codepoints[i]);
            bytes = utf8.length();
            text.append(utf8);
            size += bytes;
        }

        return text.toString();
    }

    /**
     * Encode codepoint into UTF-8 text (char array length returned as parameter)
     *
     * @param codepoint
     * @return
     */
    public String CodepointToUTF8(int codepoint) {
        char[] utf8 = new char[6];

        if (codepoint <= 0x7f) {
            utf8[0] = (char) codepoint;
        }
        else if (codepoint <= 0x7ff) {
            utf8[0] = (char) (((codepoint >> 6) & 0x1f) | 0xc0);
            utf8[1] = (char) ((codepoint & 0x3f) | 0x80);
        }
        else if (codepoint <= 0xffff) {
            utf8[0] = (char) (((codepoint >> 12) & 0x0f) | 0xe0);
            utf8[1] = (char) (((codepoint >> 6) & 0x3f) | 0x80);
            utf8[2] = (char) ((codepoint & 0x3f) | 0x80);
        }
        else if (codepoint <= 0x10ffff) {
            utf8[0] = (char) (((codepoint >> 18) & 0x07) | 0xf0);
            utf8[1] = (char) (((codepoint >> 12) & 0x3f) | 0x80);
            utf8[2] = (char) (((codepoint >> 6) & 0x3f) | 0x80);
            utf8[3] = (char) ((codepoint & 0x3f) | 0x80);
        }

        return String.valueOf(utf8);
    }

    /**
     * Get next codepoint in a UTF-8 encoded text, scanning until '\0' is found. <br/>
     * When an invalid UTF-8 byte is encountered we exit as soon as possible and a '?'(0x3f) codepoint is returned <br/>
     * <br/>
     * NOTE: The standard says U+FFFD should be returned in case of errors but that character is not supported by the default font in raylib
     * @param text
     * @param ptr
     * @return
     */
    public int GetCodepoint(String text, int ptr) {
        /*
            UTF-8 specs from https://www.ietf.org/rfc/rfc3629.txt

            Char. number range  |        UTF-8 octet sequence
              (hexadecimal)    |              (binary)
            --------------------+---------------------------------------------
            0000 0000-0000 007F | 0xxxxxxx
            0000 0080-0000 07FF | 110xxxxx 10xxxxxx
            0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
            0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        */
        // NOTE: on decode errors we return as soon as possible

        byte[] textData = text.getBytes();

        // Security check
        if (ptr >= textData.length) {
            return 0;
        }

        int codepoint = 0x3f;   // Codepoint (defaults to '?')
        byte octet = textData[0 + ptr]; // The first UTF8 octet

        if (octet <= 0x7f) {
            // Only one octet (ASCII range x00-7F)
            codepoint = textData[0 + ptr];
        }
        else if ((octet & 0xe0) == 0xc0) {
            // Two octets

            // [0]xC2-DF    [1]UTF8-tail(x80-BF)
            byte octet1 = textData[1 + ptr];

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)) {
                // Unexpected sequence
                return codepoint;
            }

            if ((octet >= 0xc2) && (octet <= 0xdf)) {
                codepoint = ((octet & 0x1f) << 6) | (octet1 & 0x3f);
            }
        }
        else if ((octet & 0xf0) == 0xe0) {
            // Three octets
            byte octet1 = textData[1 + ptr];
            byte octet2 = '\0';

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)) {
                return codepoint;
            } // Unexpected sequence

            octet2 = textData[2 + ptr];

            if ((octet2 == '\0') || ((octet2 >> 6) != 2)) {
                return codepoint;
            } // Unexpected sequence

            // [0]xE0    [1]xA0-BF       [2]UTF8-tail(x80-BF)
            // [0]xE1-EC [1]UTF8-tail    [2]UTF8-tail(x80-BF)
            // [0]xED    [1]x80-9F       [2]UTF8-tail(x80-BF)
            // [0]xEE-EF [1]UTF8-tail    [2]UTF8-tail(x80-BF)

            if (((octet == 0xe0) && !((octet1 >= 0xa0) && (octet1 <= 0xbf))) ||
                    ((octet == 0xed) && !((octet1 >= 0x80) && (octet1 <= 0x9f)))) {
                return codepoint;
            }

            if ((octet >= 0xe0) && (octet <= 0xef)) {
                codepoint = ((octet & 0xf) << 12) | ((octet1 & 0x3f) << 6) | (octet2 & 0x3f);
            }
        }
        else if ((octet & 0xf8) == 0xf0) {
            // Four octets
            if (octet > 0xf4) {
                return codepoint;
            }

            byte octet1 = textData[1 + ptr];
            byte octet2 = '\0';
            byte octet3 = '\0';

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)) {
                return codepoint;
            }  // Unexpected sequence

            octet2 = textData[2 + ptr];

            if ((octet2 == '\0') || ((octet2 >> 6) != 2)) {
                return codepoint;
            }  // Unexpected sequence

            octet3 = textData[3 + ptr];

            if ((octet3 == '\0') || ((octet3 >> 6) != 2)) {
                return codepoint;
            }  // Unexpected sequence

            // [0]xF0       [1]x90-BF       [2]UTF8-tail  [3]UTF8-tail
            // [0]xF1-F3    [1]UTF8-tail    [2]UTF8-tail  [3]UTF8-tail
            // [0]xF4       [1]x80-8F       [2]UTF8-tail  [3]UTF8-tail

            if (((octet == 0xf0) && !((octet1 >= 0x90) && (octet1 <= 0xbf))) ||
                    ((octet == 0xf4) && !((octet1 >= 0x80) && (octet1 <= 0x8f)))) {
                return codepoint;
            } // Unexpected sequence

            if (octet >= 0xf0) {
                codepoint = ((octet & 0x7) << 18) | ((octet1 & 0x3f) << 12) | ((octet2 & 0x3f) << 6) | (octet3 & 0x3f);
            }
        }

        if (codepoint > 0x10ffff) {
            codepoint = 0x3f;     // Codepoints after U+10ffff are invalid
        }

        return codepoint;
    }


    /**
     * Get all codepoints in a string
     * @param text
     * @return
     */
    public int[] LoadCodepoints(String text) {
        return text.codePoints().toArray();
    }

    /**
     * Unload codepoints data from memory
     * @param codepoints Codepoints to unload
     */
    @Contract(mutates = "param")
    public void UnloadCodepoints(int[] codepoints) {
        codepoints = null;
    }

    /**
     * Returns total number of characters(codepoints) in a UTF8 encoded text, until '\0' is found<br/>
     * <br/>
     * NOTE: If an invalid UTF8 sequence is encountered a '?'(0x3f) codepoint is counted instead
     *
     * @param text
     * @return
     */
    public int GetCodepointCount(String text) {
        return text.codePointCount(0, text.length() - 1);
    }

    /**
     * Get next codepoint in a byte sequence
     *
     * @param text Bytes of character
     * @param ptr  position of codepoint
     * @return UTF-8 codepoint
     */
    public int GetCodepointNext(String text, int ptr) {
        byte[] textData = text.getBytes();
        int codepoint = 0x3f;

        // Get current codepoint and bytes processed
        if (0xf0 == (0xf8 & textData[ptr])) {
            // 4 byte UTF-8 codepoint
            codepoint = ((0x07 & textData[ptr]) << 18) | ((0x3f & textData[ptr + 1]) << 12) | ((0x3f & textData[ptr + 2]) << 6) | (0x3f & textData[ptr + 3]);
        }
        else if (0xe0 == (0xf0 & textData[ptr])) {
            // 3 byte UTF-8 codepoint */
            codepoint = ((0x0f & textData[ptr]) << 12) | ((0x3f & textData[ptr + 1]) << 6) | (0x3f & textData[ptr + 2]);
        }
        else if (0xc0 == (0xe0 & textData[ptr])) {
            // 2 byte UTF-8 codepoint
            codepoint = ((0x1f & textData[ptr]) << 6) | (0x3f & textData[ptr + 1]);
        }
        else {
            // 1 byte UTF-8 codepoint
            codepoint = textData[ptr];
        }

        return codepoint;
    }

    /**
     * Get previous codepoint in a byte sequence and bytes processed
     *
     * @param text Bytes of character
     * @param ptr  position of codepoint
     * @return UTF-8 codepoint
     */
    public int GetCodepointPrevious(String text, int ptr) {
        return text.codePointBefore(ptr);
    }

    /**
     * Get the number of bytes occupied by a codepoint
     *
     * @param codepoint
     * @return
     */
    public int GetCodePointByteCount(int codepoint) {
        int size = 0;

        if (codepoint <= 0x7f) {
            size = 1;
        }
        else if (codepoint <= 0x7ff) {
            size = 2;
        }
        else if (codepoint <= 0xffff) {
            size = 3;
        }
        else {
            size = 4;
        }

        return size;
    }

    /**
     * Read a line from memory
     *
     * @param origin
     * @param maxLength
     * @return
     */
    public String GetLine(String origin, int maxLength) {
        int count = 0;
        for (; count < maxLength; count++) {
            if (count >= origin.length()) {
                break;
            }
            if (origin.charAt(count) == '\n') {
                break;
            }
        }
        return origin.substring(0, count);
    }

    /**
     * Load a BMFont file (AngelCode font file)
     *
     * @param fileName
     * @return
     */
    public Font LoadBMFont(String fileName) {
        int fontSize, imWidth, imHeight, codepointCount;
        int lineTracker = 1;
        String fileText = null, imFileName = null;
        String[] fileLines;

        Font font = new Font();

        try {
            fileText = context.files.LoadFileText(fileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        fileLines = fileText.split("\n");

        //start at line 1 because there's no useful info in line 0
        fontSize = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("lineHeight=") + 11,
                                                                     fileLines[lineTracker].indexOf("base=") - 1));
        imWidth = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("scaleW=") + 7,
                                                                    fileLines[lineTracker].indexOf("scaleH=") - 1));
        imHeight = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("scaleH=") + 7,
                                                                     fileLines[lineTracker].indexOf("pages=") - 1));
        lineTracker++;
        context.tracelog.TRACELOG(null, "FONT: [" + fileName + "] Loaded font info:");
        context.tracelog.TRACELOG(null, "    > Base size: " + fontSize);
        context.tracelog.TRACELOG(null, "    > Texture scale: " + imWidth + "x" + imHeight);

        imFileName = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("file=\"") + 6,
                                                      fileLines[lineTracker].lastIndexOf("\""));
        lineTracker++;
        context.tracelog.TRACELOG(null, "    > Texture filename: " + imFileName);

        String linesCount = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("=") + 1);
        linesCount = linesCount.trim();

        codepointCount = Integer.parseInt(linesCount);
        lineTracker++;
        context.tracelog.TRACELOG(null, "    > Chars count: " + codepointCount);

        String imPath = fileName.substring(0, fileName.lastIndexOf('/') + 1) + imFileName;

        Image imFont = context.textures.LoadImage(imPath);

        if (imFont.format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
            // Convert image to GRAYSCALE + ALPHA, using the mask as the alpha channel
            Image imFontAlpha = new Image();

            byte[] ifaData = new byte[imFont.width * imFont.height * 2];
            byte[] imData = imFont.getData();
            for (int p = 0, i = 0; p < (imFont.width * imFont.height * 2); p += 2, i++) {
                ifaData[p] = (byte) 0xff;
                ifaData[p + 1] = imData[i];
            }

            imFontAlpha.setData(ifaData);
            imFontAlpha.width = imFont.width;
            imFontAlpha.height = imFont.height;
            imFontAlpha.format = PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
            imFontAlpha.mipmaps = 1;

            context.textures.UnloadImage(imFont);
            imFont = imFontAlpha;
        }

        font.texture = context.textures.LoadTextureFromImage(imFont);

        // Fill font characters info data
        font.baseSize = fontSize;
        font.glyphCount = codepointCount;
        font.glyphPadding = 0;
        font.glyphs = new GlyphInfo[codepointCount];
        for (int i = 0; i < font.glyphs.length; i++) {
            font.glyphs[i] = new GlyphInfo();
        }
        font.recs = new Rectangle[codepointCount];
        for (int i = 0; i < font.recs.length; i++) {
            font.recs[i] = new Rectangle();
        }

        int charId, charX, charY, charWidth, charHeight, charOffsetX, charOffsetY, charAdvanceX;

        for (int i = 0; ; i++) {
            String tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("id=") + 3,
                                                          fileLines[lineTracker].indexOf("x="));
            charId = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("x=") + 2,
                                                   fileLines[lineTracker].indexOf("y="));
            charX = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("y=") + 2,
                                                   fileLines[lineTracker].indexOf("width="));
            charY = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("width=") + 6,
                                                   fileLines[lineTracker].indexOf("height="));
            charWidth = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("height=") + 7,
                                                   fileLines[lineTracker].indexOf("xoffset="));
            charHeight = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("xoffset=") + 8,
                                                   fileLines[lineTracker].indexOf("yoffset="));
            charOffsetX = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));


            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("yoffset=") + 8,
                                                   fileLines[lineTracker].indexOf("xadvance="));
            charOffsetY = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            tmp = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("xadvance=") + 9,
                                                   fileLines[lineTracker].indexOf("page="));
            charAdvanceX = Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));

            // Get character rectangle in the font atlas texture
            font.recs[i] = new Rectangle((float) charX, (float) charY, (float) charWidth, (float) charHeight);

            // Save data properly in sprite font
            font.glyphs[i].value = charId;
            font.glyphs[i].offsetX = charOffsetX;
            font.glyphs[i].offsetY = charOffsetY;
            font.glyphs[i].advanceX = charAdvanceX;


            // Fill character image data from imFont data
            font.glyphs[i].image = context.textures.ImageFromImage(imFont, font.recs[i]);

            lineTracker++;

            if (lineTracker == fileLines.length) {
                break;
            }
        }

        context.textures.UnloadImage(imFont);

        if (font.texture.getId() == 0) {
            UnloadFont(font);
            font = GetFontDefault();
            context.tracelog.TRACELOG(LOG_WARNING, "FONT: [" + fileName + "] Failed to load texture, reverted to default font");
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "FONT: [" + fileName + "] Font loaded successfully");
        }

        return font;
    }

    /**
     * Convert hexadecimal to decimal (single digit)
     * @param hex
     * @return
     */
    private byte HexToInt(char hex) {
        if ((hex >= '0') && (hex <= '9')) {
            return (byte) (hex - '0');
        }
        else if ((hex >= 'a') && (hex <= 'f')) {
            return (byte) (hex - 'a' + 10);
        }
        else if ((hex >= 'A') && (hex <= 'F')) {
            return (byte) (hex - 'A' + 10);
        }
        else {
            return 0;
        }
    }

    /**
     * Load a font from a BDF font file
     *
     * @param fileData
     * @param codepoints
     * @param codepointCount
     * @return
     */
    private GlyphInfo[] LoadFontDataBDF(byte[] fileData, int[] codepoints, int codepointCount) {
        int MAX_BUFFER_SIZE = 256;
        int outFontSize = 0;

        GlyphInfo[] glyphs = null;
        boolean internalCodepoints = false;

        int totalReadBytes = 0;         // Data bytes read (total)
        int readBytes = 0;              // Data bytes read (line)
        int readVars = 0;               // Variables filled by sscanf()
        int fileTextPtr = 0;

        String fileText = "";
        for (byte b : fileData) {
            fileText += (char) b;
        }

        boolean fontMalformed = false;     // Is the font malformed
        boolean fontStarted = false;       // Has font started (STARTFONT)
        int fontBBw = 0;                // Font base character bounding box width
        int fontBBh = 0;                // Font base character bounding box height
        int fontBBxoff0 = 0;            // Font base character bounding box X0 offset
        int fontBByoff0 = 0;            // Font base character bounding box Y0 offset
        int fontAscent = 0;             // Font ascent

        boolean charStarted = false;       // Has character started (STARTCHAR)
        boolean charBitmapStarted = false; // Has bitmap data started (BITMAP)
        int charBitmapNextRow = 0;      // Y position for the next row of bitmap data
        int charEncoding = -1;          // The unicode value of the character (-1 if not set)
        int charBBw = 0;                // Character bounding box width
        int charBBh = 0;                // Character bounding box height
        int charBBxoff0 = 0;            // Character bounding box X0 offset
        int charBByoff0 = 0;            // Character bounding box Y0 offset
        int charDWidthX = 0;            // Character advance X
        int charDWidthY = 0;            // Character advance Y (unused)

        int[] requiredCodepoints = new int[codepointCount];

        if (fileData == null) {
            return glyphs;
        }

        // In case no chars count provided, default to 95
        if (codepoints != null) {
            codepointCount = (codepoints.length > 0) ? codepoints.length : 95;
        }

        if (codepoints == null) {
            // Fill internal codepoints array in case not provided externally
            // NOTE: By default, filling glyph count consecutively, starting at 32 (Space)
            for (int i = 0; i < codepointCount; i++) {
                requiredCodepoints[i] = i + 32;
            }
            internalCodepoints = true;
        }
        else {
            System.arraycopy(codepoints, 0, requiredCodepoints, 0, codepointCount);
        }

        glyphs = new GlyphInfo[codepointCount];
        for (int i = 0; i < codepointCount; i++) {
            glyphs[i] = new GlyphInfo();
        }
        int glyphsIndex = -1;

        while (totalReadBytes <= fileText.length()) {
            String buffer = GetLine(fileText.substring(fileTextPtr), MAX_BUFFER_SIZE);
            readBytes = buffer.length();
            totalReadBytes += (readBytes + 1);
            fileTextPtr += (readBytes + 1);

            // Line: COMMENT
            if (buffer.contains("COMMENT")) {
                continue; // Ignore line
            }

            if (charStarted) {
                // Line: ENDCHAR
                if (buffer.contains("ENDCHAR")) {
                    charStarted = false;
                    continue;
                }

                if (charBitmapStarted) {
                    if (glyphsIndex != -1) {
                        int pixelY = charBitmapNextRow++;

                        if (pixelY >= glyphs[glyphsIndex].image.height) {
                            break;
                        }

                        for (int x = 0; x < readBytes; x++) {
                            byte b = HexToInt(buffer.charAt(x));

                            for (int bitX = 0; bitX < 4; bitX++) {
                                int pixelX = ((x * 4) + bitX);

                                if (pixelX >= glyphs[glyphsIndex].image.width) {
                                    break;
                                }

                                if ((b & (8 >> bitX)) > 0) {
                                    glyphs[glyphsIndex].image.data.put((pixelY * glyphs[glyphsIndex].image.width) + pixelX, (byte) 255);
                                }
                            }
                        }
                    }
                    continue;
                }

                // Line: ENCODING
                if (buffer.contains("ENCODING")) {
                    String[] tmp = buffer.split(" ");
                    charEncoding = Integer.parseInt(tmp[1]);
                    continue;
                }

                // Line: BBX
                if (buffer.contains("BBX")) {
                    String[] tmp = buffer.split(" ");
                    charBBw = Integer.parseInt(tmp[1]);
                    charBBh = Integer.parseInt(tmp[2]);
                    charBBxoff0 = Integer.parseInt(tmp[3]);
                    charBByoff0 = Integer.parseInt(tmp[4]);
                    continue;
                }

                // Line: DWIDTH
                if (buffer.contains("DWIDTH")) {
                    String[] tmp = buffer.split(" ");
                    charDWidthX = Integer.parseInt(tmp[1]);
                    charDWidthY = Integer.parseInt(tmp[2]);
                    continue;
                }

                // Line: BITMAP
                if (buffer.contains("BITMAP")) {
                    // Search for glyph index in codepoints
                    glyphsIndex = -1;

                    for (int index = 0; index < codepointCount; index++) {
                        if (requiredCodepoints[index] == charEncoding) {
                            glyphsIndex = index;
                            break;
                        }
                    }

                    // Init glyph info
                    if (glyphsIndex != -1) {
                        glyphs[glyphsIndex].value = charEncoding;
                        glyphs[glyphsIndex].offsetX = charBBxoff0 + fontBByoff0;
                        glyphs[glyphsIndex].offsetY = fontBBh - (charBBh + charBByoff0 + fontBByoff0 + fontAscent);
                        glyphs[glyphsIndex].advanceX = charDWidthX;

                        byte[] glyphData = new byte[charBBw * charBBh];
                        Arrays.fill(glyphData, (byte) 1);

                        glyphs[glyphsIndex].image.setData(glyphData);
                        glyphs[glyphsIndex].image.width = charBBw;
                        glyphs[glyphsIndex].image.height = charBBh;
                        glyphs[glyphsIndex].image.mipmaps = 1;
                        glyphs[glyphsIndex].image.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
                    }

                    charBitmapStarted = true;
                    charBitmapNextRow = 0;

                    continue;
                }
            }
            else if (fontStarted) {
                // Line: ENDFONT
                if (buffer.contains("ENDFONT")) {
                    fontStarted = false;
                    break;
                }

                // Line: SIZE
                if (buffer.contains("SIZE")) {
                    if (outFontSize != 0) {
                        String[] tmp = buffer.split(" ");
                        outFontSize = Integer.parseInt(tmp[1]);
                    }
                    continue;
                }

                // PIXEL_SIZE
                if (buffer.contains("PIXEL_SIZE")) {
                    if (outFontSize != 0) {
                        String[] tmp = buffer.split(" ");
                        outFontSize = Integer.parseInt(tmp[1]);
                    }
                    continue;
                }

                // FONTBOUNDINGBOX
                if (buffer.contains("FONTBOUNDINGBOX")) {
                    String[] tmp = buffer.split(" ");
                    fontBBw = Integer.parseInt(tmp[1]);
                    fontBBh = Integer.parseInt(tmp[2]);
                    fontBBxoff0 = Integer.parseInt(tmp[3]);
                    fontBByoff0 = Integer.parseInt(tmp[4]);
                    continue;
                }

                // FONT_ASCENT
                if (buffer.contains("FONT_ASCENT")) {
                    String[] tmp = buffer.split(" ");
                    fontAscent = Integer.parseInt(tmp[1]);
                    continue;
                }

                // STARTCHAR
                if (buffer.contains("STARTCHAR")) {
                    charStarted = true;
                    charEncoding = -1;
                    charBBw = 0;
                    charBBh = 0;
                    charBBxoff0 = 0;
                    charBByoff0 = 0;
                    charDWidthX = 0;
                    charDWidthY = 0;
                    charBitmapStarted = false;
                    charBitmapNextRow = 0;
                    continue;
                }
            }
            else {
                // STARTFONT
                if (buffer.contains("STARTFONT")) {
                    if (fontStarted) {
                        fontMalformed = true;
                        break;
                    }
                    else {
                        fontStarted = true;
                        continue;
                    }
                }
            }
        }

        requiredCodepoints = null;

        if (fontMalformed) {
            glyphs = null;
        }

        return glyphs;
    }

}