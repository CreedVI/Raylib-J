package com.creedvi.raylib.java.rlj.text;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.Textures;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_DEFAULT_FONT;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.UNCOMPRESSED_GRAY_ALPHA;
import static com.creedvi.raylib.java.rlj.textures.Textures.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;

public class Text{


    int MAX_TEXTFORMAT_BUFFERS = 4;        // Maximum number of static buffers for text formatting
    static int GLYPH_NOTFOUND_CHAR_FALLBACK = 63;      // Character used if requested codepoint is not found: '?'

    static int codepointByteCount;

    static Font defaultFont;

    public Text(){
        if (SUPPORT_DEFAULT_FONT){
            defaultFont = new Font();
        }
    }

    public static void LoadFontDefault(){
        // NOTE: Using UTF8 encoding table for Unicode U+0000..U+00FF Basic Latin + Latin-1 Supplement
        // Ref: http://www.utf8-chartable.de/unicode-utf8-table.pl

        defaultFont.setCharsCount(224);   // Number of chars included in our default font
        defaultFont.setCharsPadding(0);    // Characters padding

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
        imFont.setData(new int[128 * 128]);  // 2 bytes per pixel (gray + alpha)
        imFont.setWidth(128);
        imFont.setHeight(128);
        imFont.setFormat(UNCOMPRESSED_GRAY_ALPHA.getPixForInt());
        imFont.setMipmaps(1);

        // Fill image.data with defaultFontData (convert from bit to pixel!)
        for (int i = 0, counter = 0; i < imFont.getWidth() * imFont.getHeight(); i += 32){
            for (int j = 31; j >= 0; j--){
                if (((defaultFontData[counter]) & 1 << j) == 1){
                    // NOTE: We are unreferencing data as short, so,
                    // we must consider data as little-endian order (alpha + gray)
                    imFont.getData()[i + j] = 0xffff;
                }
                else{
                    imFont.getData()[i + j] = 0x00ff;
                }
                if(counter < 511){
                    counter++;
                }
            }
        }
        defaultFont.texture = LoadTextureFromImage(imFont);

        // Reconstruct charSet using charsWidth[], charsHeight, charsDivisor, charsCount
        //------------------------------------------------------------------------------

        // Allocate space for our characters info data
        // NOTE: This memory should be freed at end! --> CloseWindow()
        defaultFont.chars = new CharInfo[defaultFont.charsCount];
        defaultFont.recs = new Rectangle[defaultFont.charsCount];

        for (int q = 0; q < defaultFont.chars.length; q++){
            defaultFont.chars[q] = new CharInfo();
        }
        for (int q = 0; q < defaultFont.recs.length; q++){
            defaultFont.recs[q] = new Rectangle();
        }

        int currentLine = 0;
        int currentPosX = charsDivisor;
        int testPosX = charsDivisor;

        for (int j = 0; j < defaultFont.charsCount; j++){
            defaultFont.chars[j].setValue(32 + j);  // First char is 32

            defaultFont.recs[j].setX((float) currentPosX);
            defaultFont.recs[j].setY((float) (charsDivisor + currentLine * (charsHeight + charsDivisor)));
            defaultFont.recs[j].setWidth((float) charsWidth[j]);
            defaultFont.recs[j].setHeight((float) charsHeight);

            testPosX += (int) (defaultFont.recs[j].getWidth() + (float) charsDivisor);

            if (testPosX >= defaultFont.texture.getWidth()){
                currentLine++;
                currentPosX = 2 * charsDivisor + charsWidth[j];
                testPosX = currentPosX;

                defaultFont.recs[j].setX((float) charsDivisor);
                defaultFont.recs[j].setY((float) (charsDivisor + currentLine * (charsHeight + charsDivisor)));
            }
            else{
                currentPosX = testPosX;
            }

            // NOTE: On default font character offsets and xAdvance are not required
            defaultFont.chars[j].offsetX = 0;
            defaultFont.chars[j].offsetY = 0;
            defaultFont.chars[j].advanceX = 0;

            // Fill character image data from fontClear data
            defaultFont.chars[j].image = ImageFromImage(imFont, defaultFont.recs[j]);
        }

        imFont = UnloadImage(imFont);

        defaultFont.baseSize = (int) defaultFont.recs[0].getHeight();

        Tracelog(LOG_INFO, "FONT: Default font loaded successfully");
    }

    public static void UnloadFontDefault(){
        for (int i = 0; i < defaultFont.charsCount; i++){
           defaultFont.chars[i].image = UnloadImage(defaultFont.chars[i].image);
        }
        Textures.UnloadTexture(defaultFont.texture);
        defaultFont.chars = null;
        defaultFont.recs = null;
    }

    public static Font GetFontDefault(){
        if (SUPPORT_DEFAULT_FONT){
            return defaultFont;
        }
        else{
            return new Font();
        }
    }

    public static int getCPBC(){
        return codepointByteCount;
    }

    public void DrawFPS(int posX, int posY){
        DrawText((Core.GetFPS() + " FPS"), posX, posY, 20, Color.LIME);
        //DrawText(FormatText(Core.GetFPS() + " FPS"), posX, posY, 20, Color.LIME);
    }

    public void DrawFPS(int posX, int posY, Color textColor){
        DrawText((Core.GetFPS() + " FPS"), posX, posY, 20, textColor);
        //DrawText(FormatText(Core.GetFPS() + " FPS"), posX, posY, 20, textColor);
    }

    // Draw text (using default font)
    // NOTE: fontSize work like in any drawing program but if fontSize is lower than font-base-size, then font-base-size is used
    // NOTE: chars spacing is proportional to fontSize
    public void DrawText(String text, int posX, int posY, int fontSize, Color color){
        // Check if default font has been loaded
        if (GetFontDefault().texture.getId() != 0){
            Vector2 position = new Vector2(posX, posY);

            int defaultFontSize = 10;   // Default Font chars height in pixel
            if (fontSize < defaultFontSize) fontSize = defaultFontSize;
            int spacing = fontSize / defaultFontSize;

            DrawTextEx(GetFontDefault(), text, position, (float) fontSize, (float) spacing, color);
        }
    }

    // Draw text using Font
    // NOTE: chars spacing is NOT proportional to fontSize
    void DrawTextEx(Font font, String text, Vector2 position, float fontSize, float spacing, Color tint){
        int length = TextLength(text);      // Total length in bytes of the text, scanned by codepoints in loop

        int textOffsetY = 0;            // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;       // Offset X to next character to draw

        float scaleFactor = fontSize / font.baseSize;     // Character quad scaling factor

        for (int i = 0; i < length; ){
            // Get next codepoint from byte string and glyph index in font
            codepointByteCount = 0;
            int codepoint = GetNextCodepoint(String.valueOf(text.charAt(i)), codepointByteCount);
            int index = GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f){
                codepointByteCount = 1;
            }

            if (codepoint == '\n'){
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += (int) ((font.baseSize + font.baseSize / 2) * scaleFactor);
                textOffsetX = 0.0f;
            }
            else{
                if ((codepoint != ' ') && (codepoint != '\t')){
                    DrawTextCodepoint(font, codepoint, new Vector2(position.getX() + textOffsetX,
                            position.getY() + textOffsetY), fontSize, tint);
                }

                if (font.chars[index].advanceX == 0){
                    textOffsetX += (font.recs[index].getWidth() * scaleFactor + spacing);
                }
                else{
                    textOffsetX += ((float) font.chars[index].advanceX * scaleFactor + spacing);
                }
            }

            i += codepointByteCount;   // Move text bytes counter to next codepoint
        }
    }

    void DrawTextCodepoint(Font font, int codepoint, Vector2 position, float fontSize, Color tint){
        // Character index position in sprite font
        // NOTE: In case a codepoint is not available in the font, index returned points to '?'
        int index = GetGlyphIndex(font, codepoint);
        float scaleFactor = fontSize / font.baseSize;     // Character quad scaling factor

        // Character destination rectangle on screen
        // NOTE: We consider charsPadding on drawing
        Rectangle dstRec = new Rectangle(
                position.getX() + font.chars[index].offsetX * scaleFactor - (float) font.charsPadding * scaleFactor,
                position.getY() + font.chars[index].offsetY * scaleFactor - (float) font.charsPadding * scaleFactor,
                (font.recs[index].getWidth() + 2.0f * font.charsPadding) * scaleFactor,
                (font.recs[index].getHeight() + 2.0f * font.charsPadding) * scaleFactor);

        // Character source rectangle from font texture atlas
        // NOTE: We consider chars padding when drawing, it could be required for outline/glow shader effects
        Rectangle srcRec = new Rectangle(
                font.recs[index].getX() - (float) font.charsPadding,
                font.recs[index].getY() - (float) font.charsPadding,
                font.recs[index].getWidth() + 2.0f * font.charsPadding,
                font.recs[index].getHeight() + 2.0f * font.charsPadding);

        // Draw the character texture on the screen
        Textures.DrawTexturePro(font.texture, srcRec, dstRec, new Vector2(), 0.0f, tint);
    }

    // Measure string width for default font
    int MeasureText(String text, int fontSize){
        Vector2 vec = new Vector2();

        // Check if default font has been loaded
        if (GetFontDefault().texture.getId() != 0){
            int defaultFontSize = 10;   // Default Font chars height in pixel
            if (fontSize < defaultFontSize) fontSize = defaultFontSize;
            int spacing = fontSize / defaultFontSize;

            vec = MeasureTextEx(GetFontDefault(), text, (float) fontSize, (float) spacing);
        }

        return (int) vec.getX();
    }

    // Measure string size for Font
    public static Vector2 MeasureTextEx(Font font, String text, float fontSize, float spacing){
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

            int next = 0;
            letter = GetNextCodepoint(String.valueOf(text.charAt(i)), next);
            index = GetGlyphIndex(font, letter);

            // NOTE: normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol so to not skip any we set next = 1
            if (letter == 0x3f) next = 1;
            i += next - 1;

            if (letter != '\n'){
                if (font.chars[index].advanceX != 0){
                    textWidth += font.chars[index].advanceX;
                }
                else{
                    textWidth += (font.recs[index].getWidth() + font.chars[index].offsetX);
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
    public static int GetGlyphIndex(Font font, int codepoint){
        // Support charsets with any characters order
        int index = GLYPH_NOTFOUND_CHAR_FALLBACK;
        for (int i = 0; i < font.charsCount; i++){
            if (font.chars[i].value == codepoint){
                index = i;
                break;
            }
        }
        return index;
    }

    static int TextLength(String text){
        return text.length();
    }

    //TODO - va_list?
    /*String TextFormat(String text) {

        // We create an array of buffers so strings don't expire until MAX_TEXTFORMAT_BUFFERS invocations
        char[][] buffers = new char[MAX_TEXTFORMAT_BUFFERS][MAX_TEXT_BUFFER_LENGTH];
        int  index = 0;

        char currentBuffer = buffers[index][index];

        memset(currentBuffer, 0, MAX_TEXT_BUFFER_LENGTH);   // Clear buffer before using

        va_list args;
        va_start(args, text);
        vsnprintf(currentBuffer, MAX_TEXT_BUFFER_LENGTH, text, args);
        va_end(args);

        index += 1;     // Move to next buffer for next function call
        if (index >= MAX_TEXTFORMAT_BUFFERS){
            index = 0;
        }

        return currentBuffer;
    }*/

    // Returns next codepoint in a UTF8 encoded text, scanning until '\0' is found
    // When a invalid UTF8 byte is encountered we exit as soon as possible and a '?'(0x3f) codepoint is returned
    // Total number of bytes processed are returned as a parameter
    // NOTE: the standard says U+FFFD should be returned in case of errors
    // but that character is not supported by the default font in raylib
    // TODO: optimize this code for speed!!
    public static int GetNextCodepoint(String text, int bytesProcessed){
/*
    UTF8 specs from https://www.ietf.org/rfc/rfc3629.txt
    Char. number range  |        UTF-8 octet sequence
      (hexadecimal)    |              (binary)
    --------------------+---------------------------------------------
    0000 0000-0000 007F | 0xxxxxxx
    0000 0080-0000 07FF | 110xxxxx 10xxxxxx
    0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
    0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
*/
        // NOTE: on decode errors we return as soon as possible

        int code = 0x3f;   // Codepoint (defaults to '?')
        int octet = (text.toCharArray()[0]); // The first UTF8 octet
        bytesProcessed = 1;

        if (octet <= 0x7f){
            // Only one octet (ASCII range x00-7F)
            code = text.toCharArray()[0];
        }
        else if ((octet & 0xe0) == 0xc0){
            // Two octets
            // [0]xC2-DF    [1]UTF8-tail(x80-BF)
            char octet1 = text.toCharArray()[1];

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
            char octet1 = text.toCharArray()[1];
            char octet2 = '\0';

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)){
                bytesProcessed = 2;
                return code;
            } // Unexpected sequence

            octet2 = text.toCharArray()[2];

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

            if ((octet >= 0xe0) && (0 <= 0xef)){
                code = ((octet & 0xf) << 12) | ((octet1 & 0x3f) << 6) | (octet2 & 0x3f);
                bytesProcessed = 3;
            }
        }
        else if ((octet & 0xf8) == 0xf0){
            // Four octets
            if (octet > 0xf4){
                return code;
            }

            char octet1 = text.toCharArray()[1];
            char octet2 = '\0';
            char octet3 = '\0';

            if ((octet1 == '\0') || ((octet1 >> 6) != 2)){
                bytesProcessed = 2;
                return code;
            }  // Unexpected sequence

            octet2 = text.toCharArray()[2];

            if ((octet2 == '\0') || ((octet2 >> 6) != 2)){
                bytesProcessed = 3;
                return code;
            }  // Unexpected sequence

            octet3 = text.toCharArray()[3];

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

    public static void UnloadFont(Font f){
        f = null;
    }

}
