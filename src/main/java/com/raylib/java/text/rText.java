package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Image;
import com.raylib.java.textures.rTextures;
import com.raylib.java.utils.FileIO;
import com.raylib.java.utils.Tracelog;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.raylib.java.Config.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.rlgl.RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_POINT;
import static com.raylib.java.text.rText.FontType.*;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;

public class rText{

    /**********************************************************************************************
     *
     *   rtext - Basic functions to load fonts and draw text
     *
     *   CONFIGURATION:
     *
     *   #define SUPPORT_MODULE_RTEXT
     *       rtext module is included in the build
     *
     *   #define SUPPORT_FILEFORMAT_FNT
     *   #define SUPPORT_FILEFORMAT_TTF
     *       Selected desired fileformats to be supported for loading. Some of those formats are
     *       supported by default, to remove support, just comment unrequired #define in this module
     *
     *   #define SUPPORT_DEFAULT_FONT
     *       Load default raylib font on initialization to be used by DrawText() and MeasureText().
     *       If no default font loaded, DrawTextEx() and MeasureTextEx() are required.
     *
     *   #define TEXTSPLIT_MAX_TEXT_BUFFER_LENGTH
     *       TextSplit() function static buffer max size
     *
     *   #define MAX_TEXTSPLIT_COUNT
     *       TextSplit() function static substrings pointers array (pointing to static buffer)
     *
     *
     *   DEPENDENCIES:
     *       stb_truetype  - Load TTF file and rasterize characters data
     *       stb_rect_pack - Rectangles packing algorithms, required for font atlas generation
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2013-2022 Ramon Santamaria (@raysan5)
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

    public static class FontType{ // Font type, defines generation method

        public final static int
                FONT_DEFAULT = 0,       // Default font generation, anti-aliased
                FONT_BITMAP = 1,                 // Bitmap font generation, no anti-aliasing
                FONT_SDF = 2;                    // SDF font generation, requires external shader
    }

    final int MAX_TEXTFORMAT_BUFFERS = 4;             // Maximum number of static buffers for text formatting
    final int GLYPH_NOTFOUND_CHAR_FALLBACK = 63;      // Character used if requested codepoint is not found: '?'
    private int next;

    // Default values for ttf font generation
    final int FONT_TTF_DEFAULT_SIZE = 32;          // TTF font generation default char size (char-height)
    final int FONT_TTF_DEFAULT_NUMCHARS = 95;      // TTF font generation default charset: 95 glyphs (ASCII 32..126)
    final int FONT_TTF_DEFAULT_FIRST_CHAR = 32;    // TTF font generation default first char for image sprite font (32-Space)
    final int FONT_TTF_DEFAULT_CHARS_PADDING = 4;  // TTF font generation default chars padding
    final int MAX_GLYPHS_FROM_IMAGE = 256;         // Maximum number of glyphs supported on image scan

    final int MAX_TEXT_UNICODE_CHARS = 512;        // Maximum number of unicode codepoints: GetCodepoints()
    final int MAX_TEXTSPLIT_COUNT = 128;           // Maximum number of substrings to split: TextSplit()

    int codepointByteCount;

    Font defaultFont;
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

    //Check if col1 is equal in RGBA value to col2
    private boolean ColorEqual(Color col1, Color col2) {
        return ((col1.r == col2.r) && (col1.g == col2.g) && (col1.b == col2.b) && (col1.a == col2.a));
    }

    // Load raylib default font
    public void LoadFontDefault() {
        // NOTE: Using UTF8 encoding table for Unicode U+0000..U+00FF Basic Latin + Latin-1 Supplement
        // Ref: http://www.utf8-chartable.de/unicode-utf8-table.pl

        defaultFont.setGlyphCount(224);   // Number of chars included in our default font
        defaultFont.setGlyphPadding(0);    // Characters padding

        // Default font is directly defined here (data generated from a sprite font image)
        // This way, we reconstruct Font without creating large global variables
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
        imFont.setFormat(RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA);
        imFont.setMipmaps(1);

        short[] fontdata = new short[128 * 128 * 2];  // 2 bytes per pixel (gray + alpha)

        //Fill image.data with defaultFontData (convert from bit to pixel!)
        for (int i = 0, counter = 0; i < imFont.getWidth() * imFont.getHeight(); i += 32) {
            for (int j = 31; j >= 0; j--) {
                if (BitCheck(defaultFontData[counter], j)){
                    // NOTE: We are unreferencing data as short, so,
                    // we must consider data as little-endian order (alpha + gray)
                    fontdata[i + j] = (short) 0xffff;
                }
                else{
                    fontdata[i + j] = (short) 0x00ff;
                }
            }
            counter++;
        }

        imFont.setData(fontdata);

        defaultFont.texture = context.textures.LoadTextureFromImage(imFont);

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

            defaultFont.recs[i].x = (float)currentPosX;
            defaultFont.recs[i].y = (float)(charsDivisor + currentLine*(charsHeight + charsDivisor));
            defaultFont.recs[i].width = (float)charsWidth[i];
            defaultFont.recs[i].height = (float)charsHeight;

            testPosX += (int)(defaultFont.recs[i].width + (float)charsDivisor);

            if (testPosX >= defaultFont.texture.width) {
                currentLine++;
                currentPosX = 2*charsDivisor + charsWidth[i];
                testPosX = currentPosX;

                defaultFont.recs[i].x = (float)charsDivisor;
                defaultFont.recs[i].y = (float)(charsDivisor + currentLine*(charsHeight + charsDivisor));
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

        defaultFont.baseSize = (int)defaultFont.recs[0].height;

        Tracelog(LOG_INFO, "FONT: Default font loaded successfully (" + defaultFont.glyphCount + " glyphs)");
    }

    // Unload raylib default font
    public void UnloadFontDefault() {
        for (int i = 0; i < defaultFont.glyphCount; i++) {
            defaultFont.glyphs[i].image = context.textures.UnloadImage(defaultFont.glyphs[i].image);
        }
        defaultFont.texture = null;
        defaultFont.glyphs = null;
        defaultFont.recs = null;
    }

    // Get the default font, useful to be used with extended parameters
    public Font GetFontDefault() {
        if (SUPPORT_DEFAULT_FONT) {
            return defaultFont;
        }
        else{
            return new Font();
        }
    }

    // Load Font from file into GPU memory (VRAM)
    public Font LoadFont(String fileName) {
        Font font = null;

        if (SUPPORT_FILEFORMAT_TTF) {
            if (rCore.IsFileExtension(fileName, ".ttf") || rCore.IsFileExtension(fileName, ".otf")) {
                font = LoadFontEx(fileName, FONT_TTF_DEFAULT_SIZE, null, FONT_TTF_DEFAULT_NUMCHARS);
            }
        }
        if (SUPPORT_FILEFORMAT_FNT) {
            if (rCore.IsFileExtension(fileName, ".fnt")) {
                font = LoadBMFont(fileName);
            }
        }
        if (font == null) {
            Image image = context.textures.LoadImage(fileName);
            if (image.getData() != null) {
                font = LoadFontFromImage(image, Color.MAGENTA, FONT_TTF_DEFAULT_FIRST_CHAR);
            }
            context.textures.UnloadImage(image);
        }

        if (font.texture.getId() == 0) {
            Tracelog(LOG_WARNING, "FONT: [" + fileName + "] Failed to load font texture -> Using default font");
            font = GetFontDefault();
        }
        else{
            context.textures.SetTextureFilter(font.texture, RL_TEXTURE_FILTER_POINT); // By default we set point filter (best performance)
            Tracelog(LOG_INFO, "FONT: Data loaded successfully (" + FONT_TTF_DEFAULT_SIZE + " pixel size | " + FONT_TTF_DEFAULT_NUMCHARS + " glyphs)");
        }

        return font;
    }

    // Load Font from TTF font file with generation parameters
    // NOTE: You can pass an array with desired characters, those characters should be available in the font
    // if array is null, default char set is selected 32..126
    public Font LoadFontEx(String fileName, int fontSize, int[] fontChars, int charsCount) {
        Font font;

        // Loading file to memory
        int fileSize = 0;
        byte[] fileData = null;
        try{
            fileData = FileIO.LoadFileData(fileName);
            fileSize = fileData != null ? fileData.length : 0;
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (fileData != null) {
            // Loading font from memory data
            font = LoadFontFromMemory(rCore.GetFileExtension(fileName), fileData, fileSize, fontSize, fontChars, charsCount);
        }
        else{
            font = GetFontDefault();
        }

        return font;
    }

    // Load an Image font file (XNA style)
    public Font LoadFontFromImage(Image image, Color key, int firstChar) {

        int charSpacing;
        int lineSpacing;

        int x = 0;
        int y;

        // We allocate a temporal arrays for chars data measures,
        // once we get the actual number of chars, we copy data to a sized arrays
        int[] tempCharValues = new int[MAX_GLYPHS_FROM_IMAGE];
        Rectangle[] tempCharRecs = new Rectangle[MAX_GLYPHS_FROM_IMAGE];
        for (int i = 0; i < MAX_GLYPHS_FROM_IMAGE; i++) {
            tempCharRecs[i] = new Rectangle();
        }

        Color[] pixels = context.textures.LoadImageColors(image);

        // Parse image data to get charSpacing and lineSpacing
        for (y = 0; y < image.getHeight(); y++) {
            for (x = 0; x < image.getWidth(); x++) {
                if (!ColorEqual(pixels != null ? pixels[y * image.getWidth() + x] : null, key)){
                    break;
                }
            }

            if (!ColorEqual(pixels != null ? pixels[y * image.getWidth() + x] : null, key)){
                break;
            }
        }

        charSpacing = x;
        lineSpacing = y;

        int charHeight;
        int j = 0;

        while (!ColorEqual(pixels[(lineSpacing + j) * image.getWidth() + charSpacing], key)) j++;

        charHeight = j;

        // Check array values to get characters: value, x, y, w, h
        int index = 0;
        int lineToRead = 0;
        int xPosToRead = charSpacing;

        // Parse image data to get rectangle sizes
        while ((lineSpacing + lineToRead * (charHeight + lineSpacing)) < image.getHeight()){
            while ((xPosToRead < image.getWidth()) &&
                    !ColorEqual((pixels[(lineSpacing + (charHeight + lineSpacing) * lineToRead) * image.getWidth() + xPosToRead]),
                                key)){
                tempCharValues[index] = firstChar + index;

                tempCharRecs[index].x = (float) xPosToRead;
                tempCharRecs[index].y = (float) (lineSpacing + lineToRead * (charHeight + lineSpacing));
                tempCharRecs[index].height = (float) charHeight;

                int charWidth = 0;

                while (!ColorEqual(pixels[(lineSpacing + (charHeight + lineSpacing) * lineToRead) * image.getWidth() + xPosToRead + charWidth], key)){
                    charWidth++;
                }

                tempCharRecs[index].width = (float) charWidth;

                index++;

                xPosToRead += (charWidth + charSpacing);
            }

            lineToRead++;
            xPosToRead = charSpacing;
        }

        // NOTE: We need to remove key color borders from image to avoid weird
        // artifacts on texture scaling when using TEXTURE_FILTER_BILINEAR or TEXTURE_FILTER_TRILINEAR
        for (int i = 0; i < image.getHeight() * image.getWidth(); i++) {
            if (ColorEqual(pixels[i], key)){
                pixels[i] =
                        Color.BLANK;
            }
        }

        // Create a new image with the processed color data (key color replaced by BLANK)
        Image fontClear = new Image(pixels, image.getWidth(), image.getHeight(),
                                    RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);

        // Create spritefont with all data parsed from image
        Font font = new Font();

        font.texture = context.textures.LoadTextureFromImage(fontClear); // Convert processed image to OpenGL texture
        font.glyphCount = index;
        font.glyphPadding = 0;

        // We got tempCharValues and tempCharsRecs populated with chars data
        // Now we move temp data to sized charValues and charRecs arrays
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

    // Load font from memory buffer, fileType refers to extension: i.e. ".ttf"
    public Font LoadFontFromMemory(String fileType, byte[] fileData, int dataSize, int fontSize, int[] fontChars, int charsCount) {
        Font font = new Font();

        String fileExtLower = fileType.toLowerCase();

        if (SUPPORT_FILEFORMAT_TTF) {
            if (fileExtLower.equals(".ttf") || fileExtLower.equals(".otf")){
                font.baseSize = fontSize;
                font.glyphCount = (charsCount > 0) ? charsCount : 95;
                font.glyphPadding = 0;
                font.glyphs = LoadFontData(fileData, dataSize, font.baseSize, fontChars, font.glyphCount, FONT_DEFAULT);

                if (font.glyphs != null) {
                    font.glyphPadding = FONT_TTF_DEFAULT_CHARS_PADDING;

                    Image atlas = GenImageFontAtlas(font, 0);
                    font.texture = context.textures.LoadTextureFromImage(atlas);

                    // Update chars[i].image to use alpha, required to be used on ImageDrawText()
                    for (int i = 0; i < font.glyphCount; i++) {
                        context.textures.UnloadImage(font.glyphs[i].image);
                        font.glyphs[i].image = context.textures.ImageFromImage(atlas, font.recs[i]);
                    }

                    context.textures.UnloadImage(atlas);
                    Tracelog(LOG_INFO, "FONT: Data loaded successfully (" + font.baseSize + " pixel size | " + font.glyphCount + " glyphs)");
                }
                else {
                    font = GetFontDefault();
                }
            }
        }
        else {
            font = GetFontDefault();
        }

        return font;
    }

    // Load font data for further use
    // NOTE: Requires TTF font memory data and can generate SDF data
    public GlyphInfo[] LoadFontData(byte[] fileData, int dataSize, int fontSize, int[] fontChars, int charsCount, int type) {
        // NOTE: Using some SDF generation default values,
        // trades off precision with ability to handle *smaller* sizes
        GlyphInfo[] chars = null;

        final int FONT_SDF_CHAR_PADDING = 4;      // SDF font generation char padding
        final byte FONT_SDF_ON_EDGE_VALUE = (byte) 128;      // SDF font generation on edge value
        final float FONT_SDF_PIXEL_DIST_SCALE = 64.0f;     // SDF font generation pixel distance scale
        final int FONT_BITMAP_ALPHA_THRESHOLD = 80;      // Bitmap (B&W) font generation alpha threshold

        if (SUPPORT_FILEFORMAT_TTF) {
            // Load font data (including pixel data) from TTF memory file
            // NOTE: Loaded information should be enough to generate font image atlas, using any packaging method
            if (fileData != null) {
                boolean genFontChars = false;
                ByteBuffer fontBuffer;
                IntBuffer ascent, descent, lineGap;
                STBTTFontinfo fontInfo = STBTTFontinfo.create();

                try (MemoryStack stack = MemoryStack.stackPush()){
                    ascent = stack.mallocInt(1);
                    descent = stack.mallocInt(1);
                    lineGap = stack.mallocInt(1);

                    fontBuffer = MemoryUtil.memAlloc(fileData.length);
                    fontBuffer.put(fileData).flip();


                    if (STBTruetype.stbtt_InitFont(fontInfo, fontBuffer)){    // Init font for data reading

                        // Calculate font scale factor
                        float scaleFactor = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, (float) fontSize);

                        // Calculate font basic metrics
                        // NOTE: ascent is equivalent to font baseline
                        STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);

                        // In case no chars count provided, default to 95
                        charsCount = (charsCount > 0) ? charsCount : 95;

                        // Fill fontChars in case not provided externally
                        // NOTE: By default we fill charsCount consecutively, starting at 32 (Space)

                        if (fontChars == null) {
                            fontChars = new int[charsCount];
                            for (int i = 0; i < charsCount; i++) {
                                fontChars[i] = i + 32;
                            }
                            genFontChars = true;
                        }

                        chars = new GlyphInfo[charsCount];
                        for (int i = 0; i < charsCount; i++) {
                            chars[i] = new GlyphInfo();
                        }

                        // NOTE: Using simple packaging, one char after another
                        for (int i = 0; i < charsCount; i++) {
                            IntBuffer chw, chh; // Character width and height (on generation)
                            chw = stack.mallocInt(1);
                            chh = stack.mallocInt(1);

                            int ch = fontChars[i];  // Character value to get info for
                            chars[i].value = ch;

                            //  Render a unicode codepoint to a bitmap
                            //      stbtt_GetCodepointBitmap()           -- allocates and returns a bitmap
                            //      stbtt_GetCodepointBitmapBox()        -- how big the bitmap must be
                            //      stbtt_MakeCodepointBitmap()          -- renders into bitmap you provide

                            if (type != FONT_SDF) {
                                IntBuffer xoff, yoff;
                                xoff = stack.mallocInt(1);
                                yoff = stack.mallocInt(1);

                                ByteBuffer codepointBitmap = STBTruetype.stbtt_GetCodepointBitmap(
                                        fontInfo, scaleFactor, scaleFactor, ch, chw, chh, xoff, yoff);
                                byte[] codepointarray = new byte[(chw.get(0) * chh.get(0))];
                                for (int j = 0; j < codepointarray.length; j++) {
                                    codepointarray[j] = codepointBitmap.get(j);
                                }

                                chars[i].image.setData(codepointarray);
                                chars[i].offsetX = xoff.get(0);
                                chars[i].offsetY = yoff.get(0);
                            }
                            else if (ch != 32) {
                                IntBuffer xoff, yoff;
                                xoff = stack.mallocInt(1);
                                yoff = stack.mallocInt(1);

                                ByteBuffer codepointBitmap = STBTruetype.stbtt_GetCodepointSDF(
                                        fontInfo, scaleFactor, ch, FONT_SDF_CHAR_PADDING, FONT_SDF_ON_EDGE_VALUE,
                                        FONT_SDF_PIXEL_DIST_SCALE, chw, chh, xoff, yoff);

                                byte[] codepointarray = new byte[(chw.get(0) * chh.get(0))];
                                for (int j = 0; j < codepointarray.length; j++) {
                                    codepointarray[j] = codepointBitmap.get(j);
                                }
                                chars[i].image.setData(codepointarray);
                                chars[i].offsetX = xoff.get(0);
                                chars[i].offsetY = yoff.get(0);
                            }

                            IntBuffer adX = stack.mallocInt(1);

                            STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, ch, adX, null);
                            chars[i].advanceX = (int) ((float) adX.get(0) * scaleFactor);

                            int asc = ascent.get(0);
                            int chwidth = chw.get(0);
                            int chheight = chh.get(0);

                            // Load characters images
                            chars[i].image.width = chwidth;
                            chars[i].image.height = chheight;
                            chars[i].image.mipmaps = 1;
                            chars[i].image.format = RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;

                            chars[i].offsetY += (int) ((float) asc * scaleFactor);

                            // NOTE: We create an empty image for space character, it could be further required for atlas packing
                            if (ch == 32) {
                                Color[] c = new Color[chars[i].advanceX * fontSize];
                                for (int j = 0; j < c.length; j++) {
                                    c[j] = new Color(0, 0, 0, (byte) 255);
                                }

                                chars[i].image = new Image(c, chars[i].advanceX, fontSize, RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE, 1);
                            }

                            if (type == FONT_BITMAP) {
                                // Aliased bitmap (black & white) font generation, avoiding anti-aliasing
                                // NOTE: For optimum results, bitmap font should be generated at base pixel size
                                for (int p = 0; p < chwidth * chheight; p++) {
                                    if (chars[i].image.getData()[p] < FONT_BITMAP_ALPHA_THRESHOLD) {
                                        chars[i].image.getData()[p] = 0;
                                    }
                                    else{
                                        chars[i].image.getData()[p] = (byte) 255;
                                    }
                                }
                            }

                            /* Get bounding box for character (may be offset to account for chars that dip above or below the
                             line)

                            int chX1i, chX2i, chY1i, chY2i;
                            IntBuffer chX1, chY1, chX2, chY2;
                            chX1 = stack.mallocInt(1);
                            chX2 = stack.mallocInt(1);
                            chY1 = stack.mallocInt(1);
                            chY2 = stack.mallocInt(1);

                            STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, ch, scaleFactor, scaleFactor, chX1, chY1, chX2, chY2);
                            chX1i = chX1.get(0);
                            chX2i = chX2.get(0);
                            chY1i = chY1.get(0);
                            chY2i = chY2.get(0);
                            TracelogS("FONT: Character box measures: " + chX1i + ", " + chY1i + ", " + (chX2i - chX1i) +
                                              ", " + (chY2i - chY1i));
                            TracelogS("FONT: Character offsetY: " + ((int) ((float) asc * scaleFactor) + chY1i));
                            */
                        }
                    }
                    else{
                        Tracelog(LOG_WARNING, "FONT: Failed to process TTF font data");
                    }
                }
            }
        }

        return chars;
    }

    // Generate image font atlas using chars info
    // NOTE: Packing method: 0-Default, 1-Skyline
    public Image GenImageFontAtlas(Font font, int packMethod) {
        Image atlas = new Image();
        if (SUPPORT_FILEFORMAT_TTF) {
            if (font.glyphs == null) {
                Tracelog(LOG_WARNING, "FONT: Provided chars info not valid, returning empty image atlas");
                return atlas;
            }

            font.recs = null;

            // In case no chars count provided we suppose default of 95
            font.glyphCount = (font.glyphCount > 0) ? font.glyphCount : 95;

            // NOTE: Rectangles memory is loaded here!
            Rectangle[] recs = new Rectangle[font.glyphCount];
            for (int i = 0; i < recs.length; i++) {
                recs[i] = new Rectangle();
            }

            // Calculate image size based on required pixel area
            // NOTE 1: Image is forced to be squared and POT... very conservative!
            // NOTE 2: SDF font characters already contain an internal padding,
            // so image size would result bigger than default font type
            float requiredArea = 0;
            for (int i = 0; i < font.glyphCount; i++) {
                requiredArea += ((font.glyphs[i].image.width + 2*font.glyphPadding)*(font.baseSize + 2*font.glyphPadding));
            }
            float guessSize = (float) (Math.sqrt(requiredArea) * 1.4f);
            int imageSize = (int) Math.pow(2, Math.ceil(Math.log(guessSize) / Math.log(2)));  // Calculate next POT

            atlas.setWidth(imageSize);  // Atlas bitmap width
            atlas.setHeight(imageSize);  // Atlas bitmap height
            byte[] atlasData = new byte[atlas.width * atlas.height];
            // Create a bitmap to store characters (8 bpp)
            atlas.setFormat(RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE);
            atlas.setMipmaps(1);

            // DEBUG: We can see padding in the generated image setting a gray background...
            //byte[] greyBG = new byte[atlas.width*atlas.height];
            //for (int i = 0; i < atlas.width*atlas.height; i++) greyBG[i] = 100;
            //atlas.setData(greyBG);

            if (packMethod == 0) {  // Use basic packing algorythm

                int offsetX = font.glyphPadding;
                int offsetY = font.glyphPadding;

                // NOTE: Using simple packaging, one char after another
                for (int i = 0; i < font.glyphCount; i++) {
                    byte[] fcData = font.glyphs[i].image.getData();
                    // Copy pixel data from fc.data to atlas
                    for (int y = 0; y < font.glyphs[i].image.height; y++) {
                        for (int x = 0; x < font.glyphs[i].image.width; x++) {
                            atlasData[(offsetY + y) * atlas.width + (offsetX + x)] = fcData[y * font.glyphs[i].image.width + x];
                        }
                    }

                    // Fill chars rectangles in atlas info
                    recs[i].x = (float) offsetX;
                    recs[i].y = (float) offsetY;
                    recs[i].width = (float) font.glyphs[i].image.width;
                    recs[i].height = (float) font.glyphs[i].image.height;

                    // Move atlas position X for next character drawing
                    offsetX += (font.glyphs[i].image.getWidth() + 2 * font.glyphPadding);

                    if (offsetX >= (atlas.getWidth() - font.glyphs[i].image.getWidth() - 2 * font.glyphPadding)) {
                        offsetX = font.glyphPadding;

                        // NOTE: Be careful on offsetY for SDF fonts, by default SDF
                        // use an internal padding of 4 pixels, it means char rectangle
                        // height is bigger than fontSize, it could be up to (fontSize + 8)
                        offsetY += (font.baseSize + 2 * font.glyphPadding);

                        if (offsetY > (atlas.height - font.baseSize - font.glyphPadding)) {
                            for (int j = i + 1; j < font.glyphCount; j++) {
                                Tracelog(LOG_WARNING, "FONT: Failed to package character (" + j + ")");
                                // make sure remaining recs contain valid data
                                recs[j].x = 0;
                                recs[j].y = 0;
                                recs[j].width = 0;
                                recs[j].height = 0;
                            }
                            break;
                        }
                    }
                }
            }
            else if (packMethod == 1) { // Use Skyline rect packing algorithm (stb_pack_rect)
                STBRPContext context = STBRPContext.create();

                ByteBuffer nBB = ByteBuffer.allocateDirect(font.glyphCount * STBRPNode.SIZEOF);
                STBRPNode.Buffer nodes = new STBRPNode.Buffer(nBB);

                for (int i = 0; i < font.glyphCount; i++) {
                    nodes.put(i, STBRPNode.create());
                }

                STBRectPack.stbrp_init_target(context, atlas.width, atlas.height, nodes);

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
                STBRectPack.stbrp_pack_rects(context, rects);

                for (int i = 0; i < font.glyphCount; i++) {
                    // It return char rectangles in atlas
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
                    else{
                        Tracelog(LOG_WARNING, "FONT: Failed to package character (" + i + ")");
                    }
                }
            }

            // Convert image data from GRAYSCALE to GRAY_ALPHA
            byte[] dataGrayAlpha = new byte[atlas.width * atlas.height * 2]; // Two channels
            for (int i = 0, k = 0; i < atlas.width * atlas.height; i++, k += 2) {
                dataGrayAlpha[k] = (byte) 255;
                dataGrayAlpha[k + 1] = atlasData[i];
            }

            atlas.setData(dataGrayAlpha);
            atlas.setFormat(RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA);

            font.recs = recs;
        }

        return atlas;
    }

    // Unload font chars info data (RAM)
    public void UnloadFontData(GlyphInfo[] chars, int charsCount) {
        for (int i = 0; i < charsCount; i++) {
            context.textures.UnloadImage(chars[i].image);
        }
    }

    // Unload Font from GPU memory (VRAM)
    public void UnloadFont(Font f) {
        f = null;
    }

    // Export font as code file, returns true on success
    public boolean ExportFontAsCode(Font font, String fileName) {
        boolean success = false;

        int TEXT_BYTES_PER_LINE = 20;
        int MAX_FONT_DATA_SIZE = 1024*1024;

        // Get file name from path
        String fileNamePascal = TextToPascal(rCore.GetFileNameWithoutExt(fileName));

        // NOTE: Text data buffer size is estimated considering image data size in bytes
        // and requiring 6 char bytes for every byte: "0x00, "
        StringBuilder txtData = new StringBuilder(MAX_FONT_DATA_SIZE);

        int byteCount = 0;
        txtData.append("////////////////////////////////////////////////////////////////////////////////////////\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// FontAsCode exporter v1.0 - Font data exported as an array of bytes                 //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// more info and bugs-report:  github.com/raysan5/raylib                              //\n");
        txtData.append("// feedback and support:       ray[at]raylib.com                                      //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// Copyright (c) 2018-2022 Ramon Santamaria (@raysan5)                                //\n");
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

        // Support font export and initialization
        // NOTE: This mechanism is highly coupled to raylib
        Image image = context.textures.LoadImageFromTexture(font.texture);
        if (image.format != RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA) {
            Tracelog(LOG_WARNING, "Font export as code: Font image format is not GRAY+ALPHA!");
        }
        int imageDataSize = context.textures.GetPixelDataSize(image.width, image.height, image.format);

        // Image data is usually GRAYSCALE + ALPHA and can be reduced to GRAYSCALE
        //ImageFormat(&image, PIXELFORMAT_UNCOMPRESSED_GRAYSCALE);

        boolean SUPPORT_COMPRESSED_FONT_ATLAS = true;

        if (SUPPORT_COMPRESSED_FONT_ATLAS) {
            // WARNING: Data is compressed using raylib CompressData() DEFLATE,
            // it requires to be decompressed with raylib DecompressData(), that requires
            // compiling raylib with SUPPORT_COMPRESSION_API config flag enabled

            // Compress font image data
            int compDataSize = 0;
            // TODO: 11/14/23 rCore CompressData
            //    byte[] compData = rCore.CompressData(image.getData(), imageDataSize);
            byte[] compData = new byte[0];

            // Save font image data (compressed)
            txtData.append("#define COMPRESSED_DATA_SIZE_FONT_" + TextToUpper(fileNamePascal) + " compDataSize" + "\n\n");
            txtData.append("// Font image pixels data compressed (DEFLATE)\n");
            txtData.append("// NOTE: Original pixel data simplified to GRAYSCALE\n");
            txtData.append("static unsigned char fontData_" + fileNamePascal + "[COMPRESSED_DATA_SIZE_FONT_" + TextToUpper(fileNamePascal) + "] = { ");

            for (int i = 0; i < compDataSize - 1; i++) {
                txtData.append ((i % TEXT_BYTES_PER_LINE == 0) ? "0x" + String.format( "%02x", compData[i]) + ",\n    " : "0x" + String.format( "%02x", compData[i]) + ", ");
            }

            txtData.append( "0x" + compData[compDataSize - 1] + " };\n\n");
        }
        else {
            // Save font image data (uncompressed)
            txtData.append("// Font image pixels data\n");
            txtData.append("// NOTE: 2 bytes per pixel, GRAY + ALPHA channels\n");
            txtData.append("static unsigned char fontImageData_" + fileNamePascal + "[" + imageDataSize + "] = { ");
            for (int i = 0; i < imageDataSize - 1; i++) {
                txtData.append ((i % TEXT_BYTES_PER_LINE == 0) ? "0x" + String.format( "%02x", image.getData()[i]) + ",\n    " : "0x" + String.format( "%02x", image.getData()[i]) + ", ");
            }
            txtData.append("0x" + String.format( "%02x", image.getData()[imageDataSize - 1]) + " };\n\n");
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
        if(SUPPORT_COMPRESSED_FONT_ATLAS) {
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

        if(SUPPORT_COMPRESSED_FONT_ATLAS) {
            txtData.append("    UnloadImage(imFont);  // Uncompressed data can be unloaded from memory\n\n");
        }

        // We have two possible mechanisms to assign font.recs and font.glyphs data,
        // that data is already available as global arrays, we two options to assign that data:
        //  - 1. Data copy. This option consumes more memory and Font MUST be unloaded by user, requiring additional code.
        //  - 2. Data assignment. This option consumes less memory and Font MUST NOT be unloaded by user because data is on protected DATA segment
        boolean SUPPORT_FONT_DATA_COPY = false;

        if(SUPPORT_FONT_DATA_COPY) {
            txtData.append("    // Copy glyph recs data from global fontRecs\n");
            txtData.append("    // NOTE: Required to avoid issues if trying to free font\n");
            txtData.append("    font.recs = (Rectangle *)malloc(font.glyphCount*sizeof(Rectangle));\n");
            txtData.append("    memcpy(font.recs, fontRecs_" + fileNamePascal + ", font.glyphCount*sizeof(Rectangle));\n\n");

            txtData.append("    // Copy font glyph info data from global fontChars\n");
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
            success = FileIO.SaveFileText(fileName, txtData.toString());
            Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Font as code exported successfully");
        }
        catch (IOException e) {
            success = false;
            Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export font as code");
        }

        return success;
    }

    public int getCPBC() {
        return codepointByteCount;
    }

    // Draw current FPS
    // NOTE: Uses default font
    public void DrawFPS(int posX, int posY) {
        Color color = Color.LIME; // Good fps
        int fps = context.core.GetFPS();

        if (fps < 30 && fps >= 15) {
            color = Color.ORANGE;  // Warning FPS
        }
        else if (fps < 15) {
            color = Color.RED;    // Low FPS
        }

        DrawText(fps + " FPS", posX, posY, 20, color);
    }

    // Draw current FPS
    // NOTE: Uses default font and custom colour
    public void DrawFPS(int posX, int posY, Color textColor) {
        DrawText((context.core.GetFPS() + " FPS"), posX, posY, 20, textColor);
    }

    // Draw text (using default font)
    // NOTE: fontSize work like in any drawing program but if fontSize is lower than font-base-size, then font-base-size is used
    // NOTE: chars spacing is proportional to fontSize
    public void DrawText(String text, int posX, int posY, int fontSize, Color color) {
        // Check if default font has been loaded
        if (GetFontDefault().texture.getId() != 0) {
            Vector2 position = new Vector2(posX, posY);

            int defaultFontSize = 10;   // Default Font chars height in pixel
            if (fontSize < defaultFontSize) fontSize = defaultFontSize;
            int spacing = fontSize / defaultFontSize;

            DrawTextEx(GetFontDefault(), text, position, (float) fontSize, (float) spacing, color);
        }
    }

    // Draw text using Font
    // NOTE: chars spacing is NOT proportional to fontSize
    public void DrawTextEx(Font font, String text, Vector2 position, float fontSize, float spacing, Color tint) {
        if (font.texture.id == 0) {
            font = GetFontDefault();  // Security check in case of not valid font
        }

        int length = TextLength(text);      // Total length in bytes of the text, scanned by codepoints in loop

        int textOffsetY = 0;            // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;       // Offset X to next character to draw

        float scaleFactor = fontSize / font.baseSize;     // Character quad scaling factor

        for (int i = 0; i < length; ) {
            // Get next codepoint from byte string and glyph index in font
            codepointByteCount = 0;
            int codepoint = GetCodepoint(text.substring(i).toCharArray(), codepointByteCount);
            int index = GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f) {
                codepointByteCount = 1;
            }

            if (codepoint == '\n') {
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += (int)((font.baseSize + font.baseSize/2.0f)*scaleFactor);
                textOffsetX = 0.0f;
            }
            else{
                if ((codepoint != ' ') && (codepoint != '\t')){
                    DrawTextCodepoint(font, codepoint, new Vector2(position.getX() + textOffsetX,
                                                                   position.getY() + textOffsetY), fontSize, tint);
                }

                if (font.glyphs[index].advanceX == 0) {
                    textOffsetX += (font.recs[index].getWidth() * scaleFactor + spacing);
                }
                else{
                    textOffsetX += ((float) font.glyphs[index].advanceX * scaleFactor + spacing);
                }
            }

            i += codepointByteCount;   // Move text bytes counter to next codepoint
        }
    }

    // Draw text using Font and pro parameters (rotation)
    public void DrawTextPro(Font font, String text, Vector2 position, Vector2 origin, float rotation, float fontSize,
                         float spacing, Color tint) {
        rlPushMatrix();

        rlTranslatef(position.x, position.y, 0.0f);
        rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
        rlTranslatef(-origin.x, -origin.y, 0.0f);

        DrawTextEx(font, text, new Vector2(), fontSize, spacing, tint);

        rlPopMatrix();
    }

    // Draw one character (codepoint)
    public void DrawTextCodepoint(Font font, int codepoint, Vector2 position, float fontSize, Color tint) {
        // Character index position in sprite font
        // NOTE: In case a codepoint is not available in the font, index returned points to '?'
        int index = GetGlyphIndex(font, codepoint);
        float scaleFactor = fontSize / font.baseSize;     // Character quad scaling factor

        // Character destination rectangle on screen
        // NOTE: We consider charsPadding on drawing
        Rectangle dstRec = new Rectangle(
                position.getX() + font.glyphs[index].offsetX * scaleFactor - (float) font.glyphPadding * scaleFactor,
                position.getY() + font.glyphs[index].offsetY * scaleFactor - (float) font.glyphPadding * scaleFactor,
                (font.recs[index].getWidth() + 2.0f * font.glyphPadding) * scaleFactor,
                (font.recs[index].getHeight() + 2.0f * font.glyphPadding) * scaleFactor);

        // Character source rectangle from font texture atlas
        // NOTE: We consider chars padding when drawing, it could be required for outline/glow shader effects
        Rectangle srcRec = new Rectangle(
                font.recs[index].getX() - (float) font.glyphPadding,
                font.recs[index].getY() - (float) font.glyphPadding,
                font.recs[index].getWidth() + 2.0f * font.glyphPadding,
                font.recs[index].getHeight() + 2.0f * font.glyphPadding);

        // Draw the character texture on the screen
        context.textures.DrawTexturePro(font.texture, srcRec, dstRec, new Vector2(), 0.0f, tint);
    }

    // Draw multiple characters (codepoints)
    public void DrawTextCodepoints(Font font, int[] codepoints, Vector2 position, float fontSize, float spacing, Color tint) {
        int textOffsetY = 0;            // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;       // Offset X to next character to draw
        float scaleFactor = fontSize/font.baseSize;         // Character quad scaling factor

        for (int i = 0; i < codepoints.length; i++) {
            int index = GetGlyphIndex(font, codepoints[i]);
            if (codepoints[i] == '\n') {
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += (int)((font.baseSize + font.baseSize/2.0f)*scaleFactor);
                textOffsetX = 0.0f;
            }
            else {
                if ((codepoints[i] != ' ') && (codepoints[i] != '\t')) {
                    DrawTextCodepoint(font, codepoints[i], new Vector2(position.x + textOffsetX, position.y + textOffsetY), fontSize, tint);
                }

                if (font.glyphs[index].advanceX == 0) {
                    textOffsetX += ((float)font.recs[index].width*scaleFactor + spacing);
                }
                else {
                    textOffsetX += ((float)font.glyphs[index].advanceX*scaleFactor + spacing);
                }
            }
        }
    }

    // Measure string width for default font
    public int MeasureText(String text, int fontSize) {
        Vector2 vec = new Vector2();

        // Check if default font has been loaded
        if (GetFontDefault().texture.getId() != 0){
            int defaultFontSize = 10;   // Default Font chars height in pixel
            if (fontSize < defaultFontSize){
                fontSize = defaultFontSize;
            }
            int spacing = fontSize / defaultFontSize;

            vec = MeasureTextEx(GetFontDefault(), text, (float) fontSize, (float) spacing);
        }

        return (int) vec.getX();
    }

    // Measure string size for Font
    public Vector2 MeasureTextEx(Font font, String text, float fontSize, float spacing){
        int len = TextLength(text);
        int tempLen = 0;                // Used to count longer text line num chars
        int lenCounter = 0;

        float textWidth = 0.0f;
        float tempTextWidth = 0.0f;     // Used to count longer text line width

        float textHeight = (float) font.baseSize;
        float scaleFactor = fontSize / (float) font.baseSize;

        int letter;                 // Current character
        int index;                  // Index position in sprite font


        for (int i = 0; i < len; i++){
            lenCounter++;

            next = 0;
            letter = GetCodepoint(text.substring(i).toCharArray(), next);
            next = codepointByteCount;
            index = GetGlyphIndex(font, letter);

            // NOTE: normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol so to not skip any we set next = 1
            if (letter == 0x3f){
                next = 1;
            }
            i += next - 1;

            if (letter != '\n'){
                if (font.glyphs[index].advanceX != 0){
                    textWidth += font.glyphs[index].advanceX;
                }
                else{
                    textWidth += (font.recs[index].getWidth() + font.glyphs[index].offsetX);
                }
            }
            else{
                if (tempTextWidth < textWidth) tempTextWidth = textWidth;
                lenCounter = 0;
                textWidth = 0;
                textHeight += ((float) font.baseSize * 1.5f); // NOTE: Fixed line spacing of 1.5 lines
            }

            if (tempLen < lenCounter) tempLen = lenCounter;
        }

        if (tempTextWidth < textWidth) tempTextWidth = textWidth;

        Vector2 vec = new Vector2();
        vec.setX(tempTextWidth * scaleFactor + ((tempLen - 1) * spacing)); // Adds chars spacing to measure
        vec.setY(textHeight * scaleFactor);

        return vec;
    }

    // Returns index position for a unicode character on spritefont
    public int GetGlyphIndex(Font font, int codepoint){
        // Support charsets with any characters order
        int index = GLYPH_NOTFOUND_CHAR_FALLBACK;
        for (int i = 0; i < font.glyphCount; i++){
            if (font.glyphs[i].value == codepoint){
                index = i;
                break;
            }
        }
        return index;
    }

    // Get glyph font info data for a codepoint (unicode character)
    // NOTE: If codepoint is not found in the font it fallbacks to '?'
    GlyphInfo GetGlyphInfo(Font font, int codepoint) {
        return font.glyphs[GetGlyphIndex(font, codepoint)];
    }

    // Get glyph rectangle in font atlas for a codepoint (unicode character)
    // NOTE: If codepoint is not found in the font it fallbacks to '?'
    Rectangle GetGlyphAtlasRec(Font font, int codepoint) {
        return font.recs[GetGlyphIndex(font, codepoint)];
    }

    public int TextLength(String text){
        return text.length();
    }

    // Formatting of text with variables to 'embed'
    // Note: Calls String.format()
    public String TextFormat(String text, Object args){
        return String.format(text, args);
    }

    // Get integer value from text
    public int TextToInteger(String text){
        return Integer.parseInt(text);
    }

    //TextCopy
    //can't really copy from one memory address to another...

    // Check if two text string are equal
    public boolean TextIsEqual(String text1, String text2){
        return text1.equals(text2);
    }

    // Get a piece of a text string
    public String TextSubtext(String text, int position, int length){
        if (length < text.length()){
            return text.substring(position, length);
        }
        else{
            return text.substring(position);
        }
    }

    // Replace text string
    public String TextReplace(String text, String replace, String by){
        return text.replace(replace, by);
    }

    // Insert text in a specific position, moves all text forward
    public String TextInsert(String text, String insert, int position){
        String result;

        result = text.substring(0, position);
        result += insert;
        result += text.substring(position);

        return result;
    }

    // Join text strings with delimiter
    public String TextJoin(String[] text, char delimiter){
        StringBuilder result = new StringBuilder();

        for (String s: text){
            result.append(s);
            result.append(delimiter);
        }

        return result.toString();
    }

    // Split string into multiple strings
    public String[] TextSplit(String text, char delimiter){
        String[] result = new String[MAX_TEXTSPLIT_COUNT];

        for (int i = 0, j = 0; i < text.length(); i++){
            if (text.charAt(i) != delimiter){
                result[j] += text.charAt(i);
            }
            else{
                j++;
            }
        }

        return result;
    }

    // Append text at specific position
    public String TextAppend(String text, String append){
        return text + append;
    }

    // Find first text occurrence within a string
    public int TextFindIndex(String text, String find){
        return text.indexOf(find);
    }

    //Get upper case version of provided string
    public String TextToUpper(String text){
        return text.toUpperCase();
    }

    //Get lower case version of provided string
    public String TextToLower(String text){
        return text.toLowerCase();
    }

    // Get Pascal case notation version of provided string
    public String TextToPascal(String text){
        char[] buffer = new char[MAX_TEXT_BUFFER_LENGTH];

        buffer[0] = text.toUpperCase().charAt(0);

        for (int i = 1, j = 1; i < MAX_TEXT_BUFFER_LENGTH; i++, j++){
            if (text.charAt(j) != '\0'){
                if (text.charAt(j) != '_'){
                    buffer[i] = text.charAt(j);
                }
                else{
                    j++;
                    buffer[i] = text.toUpperCase().charAt(j);
                }
            }
            else{
                buffer[i] = '\0';
                break;
            }
        }

        return Arrays.toString(buffer);
    }

    // Encode text codepoint into UTF-8 text
    public String TextCodepointsToUTF8(int[] codepoints, int length){
        // We allocate enough memory fo fit all possible codepoints
        // NOTE: 5 bytes for every codepoint should be enough
        StringBuilder text = new StringBuilder();
        String utf8;
        int size = 0;

        for (int i = 0, bytes; i < length; i++){
            utf8 = CodepointToUTF8(codepoints[i]);
            bytes = utf8.length();
            text.append(utf8);
            size += bytes;
        }

        return text.toString();
    }

    // Encode codepoint into UTF-8 text (char array length returned as parameter)
    public String CodepointToUTF8(int codepoint){
        char[] utf8 = new char[6];
        int length = 0;

        if (codepoint <= 0x7f){
            utf8[0] = (char) codepoint;
            length = 1;
        }
        else if (codepoint <= 0x7ff){
            utf8[0] = (char) (((codepoint >> 6) & 0x1f) | 0xc0);
            utf8[1] = (char) ((codepoint & 0x3f) | 0x80);
            length = 2;
        }
        else if (codepoint <= 0xffff){
            utf8[0] = (char) (((codepoint >> 12) & 0x0f) | 0xe0);
            utf8[1] = (char) (((codepoint >> 6) & 0x3f) | 0x80);
            utf8[2] = (char) ((codepoint & 0x3f) | 0x80);
            length = 3;
        }
        else if (codepoint <= 0x10ffff){
            utf8[0] = (char) (((codepoint >> 18) & 0x07) | 0xf0);
            utf8[1] = (char) (((codepoint >> 12) & 0x3f) | 0x80);
            utf8[2] = (char) (((codepoint >> 6) & 0x3f) | 0x80);
            utf8[3] = (char) ((codepoint & 0x3f) | 0x80);
            length = 4;
        }

        return String.valueOf(utf8);
    }

    // Get all codepoints in a string, codepoints count returned by parameters
    public int[] LoadCodepoints(String text){
        int[] codepoints = new int[MAX_TEXT_UNICODE_CHARS];
        Arrays.fill(codepoints, 0);

        int bytesProcessed = 0;
        int textLength = TextLength(text);
        int codepointsCount = 0;

        for (int i = 0; i < textLength; codepointsCount++){
            codepoints[codepointsCount] = GetCodepoint(text.toCharArray(), i);
            i += bytesProcessed;
        }

        return codepoints;
    }

    // Unload codepoints data from memory
    public void UnloadCodepoints(int[] codepoints) {
        codepoints = null;
    }

    // Returns total number of characters(codepoints) in a UTF8 encoded text, until '\0' is found
    // NOTE: If an invalid UTF8 sequence is encountered a '?'(0x3f) codepoint is counted instead
    public int GetCodepointsCount(String text){
        int len = 0;
        int ptr = 0;

        while (ptr < text.length() && text.charAt(ptr) != '\0'){
            next = 0;
            int letter = GetCodepoint(text.toCharArray(), ptr);

            if (letter == 0x3f){
                ptr += 1;
            }
            else{
                ptr += next;
            }

            len++;
        }

        return len;
    }

    // Returns next codepoint in a UTF8 encoded text, scanning until '\0' is found
    // When a invalid UTF8 byte is encountered we exit as soon as possible and a '?'(0x3f) codepoint is returned
    // Total number of bytes processed are returned as a parameter
    // NOTE: the standard says U+FFFD should be returned in case of errors
    // but that character is not supported by the default font in raylib
    // TODO: optimize this code for speed!!
    public int GetCodepoint(char[] text, int bytesProcessed){
        /*
            UTF8 specs from https://www.ietf.org/rfc/rfc3629.txt
            Char. number range  |        UTF-8 octet sequence
              (hexadecimal)     |              (binary)
            --------------------+---------------------------------------------
            0000 0000-0000 007F | 0xxxxxxx
            0000 0080-0000 07FF | 110xxxxx 10xxxxxx
            0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
            0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        */
        // NOTE: on decode errors we return as soon as possible

        int code = 0x3f;   // Codepoint (defaults to '?')
        int octet = text.toString().getBytes(StandardCharsets.UTF_8)[0]; // The first UTF8 octet
        bytesProcessed = 1;

        if (octet <= 0x7f){
            // Only one octet (ASCII range x00-7F)
            code = text[0];
        }
        else if ((octet & 0xe0) == 0xc0){
            // Two octets
            // [0]xC2-DF    [1]UTF8-tail(x80-BF)
            char octet1 = text[1];

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)){
                bytesProcessed = 2;
                return code;
            } // Unexpected sequence

            if ((octet >= 0xc2) && (octet <= 0xdf)){
                code = ((octet & 0x1f) << 6) | (octet1 & 0x3f);
                bytesProcessed = 2;
            }
        }
        else if ((octet & 0xf0) == 0xe0){
            // Three octets
            char octet1 = text[1];
            char octet2;

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)){
                bytesProcessed = 2;
                return code;
            } // Unexpected sequence

            octet2 = text[2];

            if ((octet2 == '\0') || ((octet2 >> 6) != 2)){
                bytesProcessed = 3;
                return code;
            } // Unexpected sequence

        /*
            [0]xE0    [1]xA0-BF       [2]UTF8-tail(x80-BF)
            [0]xE1-EC [1]UTF8-tail    [2]UTF8-tail(x80-BF)
            [0]xED    [1]x80-9F       [2]UTF8-tail(x80-BF)
            [0]xEE-EF [1]UTF8-tail    [2]UTF8-tail(x80-BF)
        */

            if (((octet == 0xe0) && !((octet1 >= 0xa0) && (octet1 <= 0xbf))) ||
                    ((octet == 0xed) && !((octet1 >= 0x80) && (octet1 <= 0x9f)))){
                bytesProcessed = 2;
                return code;
            }

            if ((octet >= 0xe0) && (octet <= 0xef)){
                code = ((octet & 0xf) << 12) | ((octet1 & 0x3f) << 6) | (octet2 & 0x3f);
                bytesProcessed = 3;
            }
        }
        else if ((octet & 0xf8) == 0xf0){
            // Four octets
            if (octet > 0xf4){
                return code;
            }

            char octet1 = text[1];
            char octet2 = '\0';
            char octet3 = '\0';

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)){
                bytesProcessed = 2;
                return code;
            }  // Unexpected sequence

            octet2 = text[2];

            if ((octet2 == '\0') || ((octet2 >> 6) != 2)){
                bytesProcessed = 3;
                return code;
            }  // Unexpected sequence

            octet3 = text[3];

            if ((octet3 == '\0') || ((octet3 >> 6) != 2)){
                bytesProcessed = 4;
                return code;
            }  // Unexpected sequence

        /*
            [0]xF0       [1]x90-BF       [2]UTF8-tail  [3]UTF8-tail
            [0]xF1-F3    [1]UTF8-tail    [2]UTF8-tail  [3]UTF8-tail
            [0]xF4       [1]x80-8F       [2]UTF8-tail  [3]UTF8-tail
        */

            if (((octet == 0xf0) && !((octet1 >= 0x90) && (octet1 <= 0xbf))) ||
                    ((octet == 0xf4) && !((octet1 >= 0x80) && (octet1 <= 0x8f)))){
                bytesProcessed = 2;
                return code;
            } // Unexpected sequence

            if (octet >= 0xf0){
                code = ((octet & 0x7) << 18) | ((octet1 & 0x3f) << 12) | ((octet2 & 0x3f) << 6) | (octet3 & 0x3f);
                bytesProcessed = 4;
            }
        }

        if (code > 0x10ffff){
            code = 0x3f;     // Codepoints after U+10ffff are invalid
        }

        codepointByteCount = bytesProcessed;

        return code;
    }

    // Read a line from memory
    public int GetLine(String origin, String buffer, int maxLength){
        int count = 0;
        for (; count < maxLength; count++){
            if (origin.charAt(count) == '\n'){
                break;
            }
        }
        buffer = origin.substring(0, count);
        return count;
    }

    // Load a BMFont file (AngelCode font file)
    public Font LoadBMFont(String fileName){
        int fontSize, imWidth, imHeight, charsCount;
        int lineTracker = 1;
        String fileText = null, imFileName = null;
        String[] fileLines;

        Font font = new Font();

        try{
            fileText = FileIO.LoadFileText(fileName);
        } catch (IOException e){
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
        Tracelog.Tracelog("FONT: [" + fileName + "] Loaded font info:");
        Tracelog.Tracelog("    > Base size: " + fontSize);
        Tracelog.Tracelog("    > Texture scale: " + imWidth + "x" + imHeight);

        imFileName = fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("file=\"") + 6,
                                                      fileLines[lineTracker].lastIndexOf("\""));
        lineTracker++;
        Tracelog.Tracelog("    > Texture filename: " + imFileName);

        charsCount = Integer.parseInt(fileLines[lineTracker].substring(fileLines[lineTracker].indexOf("count=") + 6));
        lineTracker++;
        Tracelog.Tracelog("    > Chars count: " + charsCount);

        String imPath = fileName.substring(0, fileName.lastIndexOf('/') + 1) + imFileName;

        Image imFont = context.textures.LoadImage(imPath);

        if (imFont.format == RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE){
            // Convert image to GRAYSCALE + ALPHA, using the mask as the alpha channel
            Image imFontAlpha = new Image();

            byte[] ifaData = new byte[imFont.width * imFont.height * 2];
            byte[] imData = imFont.getData();
            for (int p = 0, i = 0; p < (imFont.width * imFont.height * 2); p += 2, i++){
                ifaData[p] = (byte) 0xff;
                ifaData[p + 1] = imData[i];
            }

            imFontAlpha.setData(ifaData);
            imFontAlpha.width = imFont.width;
            imFontAlpha.height = imFont.height;
            imFontAlpha.format = RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
            imFontAlpha.mipmaps = 1;

            context.textures.UnloadImage(imFont);
            imFont = imFontAlpha;
        }

        font.texture = context.textures.LoadTextureFromImage(imFont);

        // Fill font characters info data
        font.baseSize = fontSize;
        font.glyphCount = charsCount;
        font.glyphPadding = 0;
        font.glyphs = new GlyphInfo[charsCount];
        for (int i = 0; i < font.glyphs.length; i++){
            font.glyphs[i] = new GlyphInfo();
        }
        font.recs = new Rectangle[charsCount];
        for (int i = 0; i < font.recs.length; i++){
            font.recs[i] = new Rectangle();
        }

        int charId, charX, charY, charWidth, charHeight, charOffsetX, charOffsetY, charAdvanceX;

        for (int i = 0; ; i++){
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

            if (lineTracker == fileLines.length){
                break;
            }
        }

        context.textures.UnloadImage(imFont);

        if (font.texture.getId() == 0){
            UnloadFont(font);
            font = GetFontDefault();
            Tracelog(LOG_WARNING, "FONT: [" + fileName + "] Failed to load texture, reverted to default font");
        }
        else{
            Tracelog(LOG_INFO, "FONT: [" + fileName + "] Font loaded successfully");
        }

        return font;
    }

}