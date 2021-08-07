package com.creedvi.raylib.java.rlj.text;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.Textures;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_DEFAULT_FONT;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.creedvi.raylib.java.rlj.textures.Textures.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;

public class Text{


    final static int MAX_TEXTFORMAT_BUFFERS = 4;        // Maximum number of static buffers for text formatting
    final static int GLYPH_NOTFOUND_CHAR_FALLBACK = 63;      // Character used if requested codepoint is not found: '?'

    // Default values for ttf font generation
    final int FONT_TTF_DEFAULT_SIZE = 32;      // TTF font generation default char size (char-height)
    final int FONT_TTF_DEFAULT_NUMCHARS = 95;      // TTF font generation default charset: 95 glyphs (ASCII 32..126)
    final int FONT_TTF_DEFAULT_FIRST_CHAR = 32;      // TTF font generation default first char for image sprite font
    // (32-Space)
    final int FONT_TTF_DEFAULT_CHARS_PADDING = 4;      // TTF font generation default chars padding
    final int MAX_GLYPHS_FROM_IMAGE = 256;     // Maximum number of glyphs supported on image scan

    static int codepointByteCount;

    static Font defaultFont;

    public Text(){
        if (SUPPORT_DEFAULT_FONT){
            defaultFont = new Font();
        }
    }

    //TODO Figure out why collum 31, 63, 95, 127 of the buffer loads incorrectly
    //By all accounts it maths out.
    //Maybe something to do with the negative?

    /**
     * Check if the bth binary bit is 1 in an integer
     * @param a the value to check
     * @param b position of bit to examine
     * @return <code>true</code> if bit at b is set to 1
     */
    private static boolean BitCheck(int a, int b){
        /*if (a == 0x0002085e){
            System.out.print(a +", "+ b);
            System.out.print(" | "+((a) & (1 << (b))) +", "+(int)Math.pow(2,b)+" | ");
            System.out.println(((a) & (1 << (b))) == (int)Math.pow(2,b));
        }*/
        return ((a) & (1 << (b))) == (int)Math.pow(2,b);
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
        imFont.setWidth(128);
        imFont.setHeight(128);
        imFont.setFormat(PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA);
        imFont.setMipmaps(1);

        short[] fontdata = new short[128 * 128];  // 2 bytes per pixel (gray + alpha)
        StringBuilder sb = new StringBuilder();

        //Fill image.data with defaultFontData (convert from bit to pixel!)
        for (int i = 0, counter = 0; i < imFont.getWidth() * imFont.getHeight(); i += 32){
            for (int j = 31; j >= 0; j--){
                if (BitCheck(defaultFontData[counter], j)){
                    // NOTE: We are unreferencing data as short, so,
                    // we must consider data as little-endian order (alpha + gray)
                    fontdata[i + j] = (short) 0xffff;
                    sb.append("1, 1 ").append(BitCheck(defaultFontData[counter], j)).append(defaultFontData[counter]).append(", ").append(j).append(" ").append(1 << j).append(" | ");
                }
                else{
                    fontdata[i + j] = (short) 0x00ff;
                    sb.append("1, 0 ").append(BitCheck(defaultFontData[counter], j)).append(defaultFontData[counter]).append(", ").append(j).append(" ").append(1 << j).append(" | ");

                }
            }
            counter++;
            if (counter%4 == 0)
                sb.append("\n");
        }

        imFont.setData(fontdata);

        //System.out.println(sb.toString());

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

        UnloadImage(imFont);

        defaultFont.baseSize = (int) defaultFont.recs[0].getHeight();

        Tracelog(LOG_INFO, "FONT: Default font loaded successfully");
    }

    public static void UnloadFontDefault(){
        for (int i = 0; i < defaultFont.charsCount; i++){
           defaultFont.chars[i].image = UnloadImage(defaultFont.chars[i].image);
        }
        defaultFont.texture = null;
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

    /*
    // Load Font from file into GPU memory (VRAM)
    public Font LoadFont(String fileName)
    {
        Font font = new Font();

        if(SUPPORT_FILEFORMAT_TTF){
            if (IsFileExtension(fileName, ".ttf;.otf")){
                font = LoadFontEx(fileName, FONT_TTF_DEFAULT_SIZE, null, FONT_TTF_DEFAULT_NUMCHARS);
            }
        }

        else if(SUPPORT_FILEFORMAT_FNT){
            if (IsFileExtension(fileName, ".fnt")){
                font = LoadBMFont(fileName);
            }
        }
        else {
            Image image = LoadImage(fileName);
            if (image.getData() != null){
                font = LoadFontFromImage(image, Color.MAGENTA, FONT_TTF_DEFAULT_FIRST_CHAR);
            }
            UnloadImage(image);
        }

        if (font.texture.getId() == 0)
        {
            Tracelog(LOG_WARNING, "FONT: [" + fileName + "] Failed to load font texture -> Using default font");
            font = GetFontDefault();
        }
        else SetTextureFilter(font.texture, TEXTURE_FILTER_POINT.getTextureFilterInt());    // By default we set point filter (best
        // performance)

        return font;
    }

    // Load Font from TTF font file with generation parameters
    // NOTE: You can pass an array with desired characters, those characters should be available in the font
    // if array is NULL, default char set is selected 32..126
    Font LoadFontEx(String fileName, int fontSize, int[] fontChars, int charsCount)
    {
        Font font = new Font();

        // Loading file to memory
        int fileSize = 0;
        byte[] fileData = LoadFileData(fileName);

        if (fileData != null) {
            // Loading font from memory data
            font = LoadFontFromMemory(GetFileExtension(fileName), fileData, fileSize, fontSize, fontChars, charsCount);

        }
        else {
            font = GetFontDefault();
        }

        return font;
    }
    */

    boolean ColorEqual(Color col1, Color col2){
        return ((col1.r == col2.r)&&(col1.g == col2.g)&&(col1.b == col2.b)&&(col1.a == col2.a));
    }

    // Load an Image font file (XNA style)
    Font LoadFontFromImage(Image image, Color key, int firstChar) {

        int charSpacing;
        int lineSpacing;

        int x = 0;
        int y;

        // We allocate a temporal arrays for chars data measures,
        // once we get the actual number of chars, we copy data to a sized arrays
        int[] tempCharValues = new int[MAX_GLYPHS_FROM_IMAGE];
        Rectangle[] tempCharRecs = new Rectangle[MAX_GLYPHS_FROM_IMAGE];

        Color[] pixels = Textures.LoadImageColors(image);

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

        while (!ColorEqual(pixels[(lineSpacing + j)*image.getWidth() + charSpacing], key)) j++;

        charHeight = j;

        // Check array values to get characters: value, x, y, w, h
        int index = 0;
        int lineToRead = 0;
        int xPosToRead = charSpacing;

        // Parse image data to get rectangle sizes
        while ((lineSpacing + lineToRead*(charHeight + lineSpacing)) < image.getHeight())
        {
            while ((xPosToRead < image.getWidth()) &&
                    !ColorEqual((pixels[(lineSpacing + (charHeight+lineSpacing)*lineToRead)*image.getWidth() + xPosToRead]),
                            key))
            {
                tempCharValues[index] = firstChar + index;

                tempCharRecs[index].x = (float)xPosToRead;
                tempCharRecs[index].y = (float)(lineSpacing + lineToRead*(charHeight + lineSpacing));
                tempCharRecs[index].height = (float)charHeight;

                int charWidth = 0;

                while (!ColorEqual(pixels[(lineSpacing + (charHeight+lineSpacing)*lineToRead)*image.getWidth() + xPosToRead + charWidth], key)) charWidth++;

                tempCharRecs[index].width = (float)charWidth;

                index++;

                xPosToRead += (charWidth + charSpacing);
            }

            lineToRead++;
            xPosToRead = charSpacing;
        }

        // NOTE: We need to remove key color borders from image to avoid weird
        // artifacts on texture scaling when using TEXTURE_FILTER_BILINEAR or TEXTURE_FILTER_TRILINEAR
        for (int i = 0; i < image.getHeight()*image.getWidth(); i++) if (ColorEqual(pixels[i], key)) pixels[i] =
                Color.BLANK;

        // Create a new image with the processed color data (key color replaced by BLANK)
        Image fontClear = new Image(pixels, image.getWidth(), image.getHeight(),
                PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);

        // Create spritefont with all data parsed from image
        Font font = new Font();

        font.texture = LoadTextureFromImage(fontClear); // Convert processed image to OpenGL texture
        font.charsCount = index;
        font.charsPadding = 0;

        // We got tempCharValues and tempCharsRecs populated with chars data
        // Now we move temp data to sized charValues and charRecs arrays
        font.chars = new CharInfo[font.charsCount];
        font.recs = new Rectangle[font.charsCount];

        for (int i = 0; i < font.charsCount; i++)
        {
            font.chars[i].value = tempCharValues[i];

            // Get character rectangle in the font atlas texture
            font.recs[i] = tempCharRecs[i];

            // NOTE: On image based fonts (XNA style), character offsets and xAdvance are not required (set to 0)
            font.chars[i].offsetX = 0;
            font.chars[i].offsetY = 0;
            font.chars[i].advanceX = 0;

            // Fill character image data from fontClear data
            font.chars[i].image = ImageFromImage(fontClear, tempCharRecs[i]);
        }

        UnloadImage(fontClear);     // Unload processed image once converted to texture

        font.baseSize = (int)font.recs[0].height;

        return font;
    }

    /*TODO
    // Load font from memory buffer, fileType refers to extension: i.e. ".ttf"
    Font LoadFontFromMemory(String fileType, byte[] fileData, int dataSize, int fontSize, int[] fontChars,
                            int charsCount)
    {
        Font font = new Font();

        String fileExtLower = fileType.toLowerCase();

        if(SUPPORT_FILEFORMAT_TTF){
            if (fileExtLower.equals(".ttf") || fileExtLower.equals(".otf")){
                font.baseSize = fontSize;
                font.charsCount = (charsCount > 0) ? charsCount : 95;
                font.charsPadding = 0;
                font.chars = LoadFontData(fileData, dataSize, font.baseSize, fontChars, font.charsCount, defaultFont.charsCount);

                if (font.chars != null){
                    font.charsPadding = FONT_TTF_DEFAULT_CHARS_PADDING;

                    Image atlas = GenImageFontAtlas(font.chars, font.recs, font.
                    charsCount, font.baseSize, font.charsPadding, 0);
                    font.texture = LoadTextureFromImage(atlas);

                    // Update chars[i].image to use alpha, required to be used on ImageDrawText()
                    for (int i = 0; i < font.charsCount; i++){
                        UnloadImage(font.chars[i].image);
                        font.chars[i].image = ImageFromImage(atlas, font.recs[i]);
                    }

                    UnloadImage(atlas);
                }
                else font = GetFontDefault();
            }
        }
        else{
            font = GetFontDefault();
        }
        return font;
    }

    // Load font data for further use
    // NOTE: Requires TTF font memory data and can generate SDF data
    CharInfo[] LoadFontData(byte[] fileData, int dataSize, int fontSize, int[] fontChars, int charsCount, int type) {
        // NOTE: Using some SDF generation default values,
        // trades off precision with ability to handle *smaller* sizes
            CharInfo[] chars = null;

        if(SUPPORT_FILEFORMAT_TTF){
            // Load font data (including pixel data) from TTF memory file
            // NOTE: Loaded information should be enough to generate font image atlas, using any packaging method
            if (fileData != null){
                int genFontChars = 0;
                stbtt_fontinfo fontInfo = {0};

                if (stbtt_InitFont( & fontInfo,(unsigned char *)fileData, 0))     // Init font for data reading
                {
                    // Calculate font scale factor
                    float scaleFactor = stbtt_ScaleForPixelHeight( & fontInfo, (float) fontSize);

                    // Calculate font basic metrics
                    // NOTE: ascent is equivalent to font baseline
                    int ascent, descent, lineGap;
                    stbtt_GetFontVMetrics(&fontInfo, &ascent, &descent, &lineGap);

                    // In case no chars count provided, default to 95
                    charsCount = (charsCount > 0) ? charsCount : 95;

                    // Fill fontChars in case not provided externally
                    // NOTE: By default we fill charsCount consecutevely, starting at 32 (Space)

                    if (fontChars == null){
                        fontChars = ( int *)RL_MALLOC(charsCount * sizeof( int));
                        for (int i = 0; i < charsCount; i++) fontChars[i] = i + 32;
                        genFontChars = true;
                    }

                    chars = (CharInfo *)RL_MALLOC(charsCount * sizeof(CharInfo));

                    // NOTE: Using simple packaging, one char after another
                    for (int i = 0; i < charsCount; i++){
                        int chw = 0, chh = 0;   // Character width and height (on generation)
                        int ch = fontChars[i];  // Character value to get info for
                        chars[i].value = ch;

                        //  Render a unicode codepoint to a bitmap
                        //      stbtt_GetCodepointBitmap()           -- allocates and returns a bitmap
                        //      stbtt_GetCodepointBitmapBox()        -- how big the bitmap must be
                        //      stbtt_MakeCodepointBitmap()          -- renders into bitmap you provide

                        if (type != FONT_SDF)
                            chars[i].image.data = stbtt_GetCodepointBitmap( & fontInfo, scaleFactor, scaleFactor, ch, &
                        chw, &chh, &chars[i].offsetX, &chars[i].offsetY);
                else if (ch != 32)
                            chars[i].image.data = stbtt_GetCodepointSDF( & fontInfo, scaleFactor, ch, FONT_SDF_CHAR_PADDING, FONT_SDF_ON_EDGE_VALUE, FONT_SDF_PIXEL_DIST_SCALE, &
                        chw, &chh, &chars[i].offsetX, &chars[i].offsetY);
                else chars[i].image.data = NULL;

                        stbtt_GetCodepointHMetrics(&fontInfo, ch, &chars[i].advanceX, NULL);
                        chars[i].advanceX = (int) ((float) chars[i].advanceX * scaleFactor);

                        // Load characters images
                        chars[i].image.width = chw;
                        chars[i].image.height = chh;
                        chars[i].image.mipmaps = 1;
                        chars[i].image.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;

                        chars[i].offsetY += (int) ((float) ascent * scaleFactor);

                        // NOTE: We create an empty image for space character, it could be further required for atlas packing
                        if (ch == 32){
                            Image imSpace = {
                                    .data = calloc(chars[i].advanceX * fontSize, 2),
                        .width = chars[i].advanceX,
                        .height = fontSize,
                        .format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE,
                        .mipmaps = 1
                    };

                            chars[i].image = imSpace;
                        }

                        if (type == FONT_BITMAP){
                            // Aliased bitmap (black & white) font generation, avoiding anti-aliasing
                            // NOTE: For optimum results, bitmap font should be generated at base pixel size
                            for (int p = 0; p < chw * chh; p++){
                                if (((unsigned char *)chars[i].image.data)[p] <FONT_BITMAP_ALPHA_THRESHOLD)((unsigned
                                char *)chars[i].image.data)[p] =0;
                        else((unsigned char *)chars[i].image.data)[p] =255;
                            }
                        }

                        // Get bounding box for character (may be offset to account for chars that dip above or below the line)

                        int chX1, chY1, chX2, chY2;
                        stbtt_GetCodepointBitmapBox(&fontInfo, ch, scaleFactor, scaleFactor, &chX1, &chY1, &chX2, &chY2);

                        TRACELOGD("FONT: Character box measures: %i, %i, %i, %i", chX1, chY1, chX2 - chX1, chY2 - chY1);
                        TRACELOGD("FONT: Character offsetY: %i", (int)((float)ascent*scaleFactor) + chY1);

                    }
                }
                 else {
                    Tracelog(LOG_WARNING, "FONT: Failed to process TTF font data");
                }

            }
        }

        return chars;
    }
    */

    public static int getCPBC(){
        return codepointByteCount;
    }

    public void DrawFPS(int posX, int posY){
        Color color = Color.LIME; // good fps
        int fps = Core.GetFPS();

        if (fps < 30 && fps >= 15) color = Color.ORANGE;  // warning FPS
        else if (fps < 15) color = Color.RED;    // bad FPS

        DrawText(fps + " + FPS", posX, posY, 20, color);
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
    public int MeasureText(String text, int fontSize){
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
            i += 1;

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
 z
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

    // Get a piece of a text string
    public String TextSubtext(String text, int position, int length){
        if (length < text.length())
            return text.substring(position, length);
        else
            return text.substring(position);
    }


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
            char octet2;

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

    /*
    // Generate image font atlas using chars info
    // NOTE: Packing method: 0-Default, 1-Skyline
    Image GenImageFontAtlas(CharInfo[] chars, Rectangle[] charRecs, int charsCount, int fontSize, int padding,
                            int packMethod) {
        Image atlas = new Image();
        if(SUPPORT_FILEFORMAT_TTF){
            if (chars == null){
                Tracelog(LOG_WARNING, "FONT: Provided chars info not valid, returning empty image atlas");
                return atlas;
            }

            charRecs = null;

            // In case no chars count provided we suppose default of 95
            charsCount = (charsCount > 0) ? charsCount : 95;

            // NOTE: Rectangles memory is loaded here!
            Rectangle[] recs = new Rectangle[charsCount];

            // Calculate image size based on required pixel area
            // NOTE 1: Image is forced to be squared and POT... very conservative!
            // NOTE 2: SDF font characters already contain an internal padding,
            // so image size would result bigger than default font type
            float requiredArea = 0;
            for (int i = 0; i < charsCount; i++)
                requiredArea += ((chars[i].image.getWidth() + 2 * padding) * (chars[i].image.getHeight() + 2 * padding));
            float guessSize = (float) (Math.sqrt(requiredArea) * 1.3f);
            int imageSize = (int) Math.pow(2, Math.ceil(Math.log(guessSize) / Math.log(2)));  // Calculate next
            // POT

            atlas.setWidth(imageSize);  // Atlas bitmap width
            atlas.setHeight(imageSize);  // Atlas bitmap height
            atlas.setData(new byte[atlas.getWidth() * atlas.getHeight()]);
            // Create a bitmap to store characters (8 bpp)
            atlas.setFormat(PIXELFORMAT_UNCOMPRESSED_GRAYSCALE);
            atlas.setMipmaps(1);

            // DEBUG: We can see padding in the generated image setting a gray background...
            //for (int i = 0; i < atlas.width*atlas.height; i++) ((unsigned char *)atlas.data)[i] = 100;

            if (packMethod == 0)   // Use basic packing algorythm
            {
                int offsetX = padding;
                int offsetY = padding;

                // NOTE: Using simple packaging, one char after another
                for (int i = 0; i < charsCount; i++){
                    // Copy pixel data from fc.data to atlas
                    for (int y = 0; y < chars[i].image.getHeight(); y++){
                        for (int x = 0; x < chars[i].image.getWidth(); x++){
                            (atlas.getData())[(offsetY + y)*atlas.getWidth() + (offsetX + x)] =
                                    (chars[i].image.getData())[y * chars[i].image.getWidth() + x];
                        }
                    }

                    // Fill chars rectangles in atlas info
                    recs[i].x = (float) offsetX;
                    recs[i].y = (float) offsetY;
                    recs[i].width = (float) chars[i].image.getWidth();
                    recs[i].height = (float) chars[i].image.getHeight();

                    // Move atlas position X for next character drawing
                    offsetX += (chars[i].image.getWidth() + 2 * padding);

                    if (offsetX >= (atlas.getWidth() - chars[i].image.getWidth() - 2 * padding)){
                        offsetX = padding;

                        // NOTE: Be careful on offsetY for SDF fonts, by default SDF
                        // use an internal padding of 4 pixels, it means char rectangle
                        // height is bigger than fontSize, it could be up to (fontSize + 8)
                        offsetY += (fontSize + 2 * padding);

                        if (offsetY > (atlas.getHeight() - fontSize - padding)) break;
                    }
                }
            }
            else if (packMethod == 1)  // Use Skyline rect packing algorythm (stb_pack_rect)
            {
                stbrp_context * context = (stbrp_context *)RL_MALLOC(sizeof( * context));
                stbrp_node * nodes = (stbrp_node *)RL_MALLOC(charsCount * sizeof( * nodes));

                stbrp_init_target(context, atlas.width, atlas.height, nodes, charsCount);
                stbrp_rect * rects = (stbrp_rect *)RL_MALLOC(charsCount * sizeof(stbrp_rect));

                // Fill rectangles for packaging
                for (int i = 0; i < charsCount; i++){
                    rects[i].id = i;
                    rects[i].w = chars[i].image.width + 2 * padding;
                    rects[i].h = chars[i].image.height + 2 * padding;
                }

                // Package rectangles into atlas
                stbrp_pack_rects(context, rects, charsCount);

                for (int i = 0; i < charsCount; i++){
                    // It return char rectangles in atlas
                    recs[i].x = rects[i].x + (float) padding;
                    recs[i].y = rects[i].y + (float) padding;
                    recs[i].width = (float) chars[i].image.width;
                    recs[i].height = (float) chars[i].image.height;

                    if (rects[i].was_packed){
                        // Copy pixel data from fc.data to atlas
                        for (int y = 0; y < chars[i].image.height; y++){
                            for (int x = 0; x < chars[i].image.width; x++){
                                ((unsigned char *)atlas.data)[(rects[i].y + padding + y)*
                                atlas.width + (rects[i].x + padding + x)] =((unsigned char *)chars[i].image.data)[
                                y * chars[i].image.width + x];
                            }
                        }
                    }
                    else Tracelog(LOG_WARNING, "FONT: Failed to package character (" + i + ")");
                }
            }

            // TODO: Crop image if required for smaller size

            // Convert image data from GRAYSCALE to GRAY_ALPHA
            unsigned char *dataGrayAlpha = (unsigned char *)RL_MALLOC(atlas.width * atlas.height * sizeof(unsigned char)*
            2); // Two channels

            for (int i = 0, k = 0; i < atlas.width * atlas.height; i++, k += 2){
                dataGrayAlpha[k] = 255;
                dataGrayAlpha[k + 1] = ((unsigned char *)atlas.data)[i];
            }

            atlas.setData(dataGrayAlpha);
            atlas.setFormat(PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA);

            charRecs = recs;
        }

        return atlas;
    }
    */

    public static void UnloadFont(Font f){
        f = null;
    }

}
