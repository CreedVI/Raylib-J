package com.creedvi.raylib.java.rlj.textures;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.raymath.Vector3;
import com.creedvi.raylib.java.rlj.raymath.Vector4;
import com.creedvi.raylib.java.rlj.rlgl.RLGL;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.text.Font;
import com.creedvi.raylib.java.rlj.text.Text;

import java.nio.ByteBuffer;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_IMAGE_MANIPULATION;
import static com.creedvi.raylib.java.rlj.core.Color.BLANK;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.*;
import static com.creedvi.raylib.java.rlj.text.Text.GetFontDefault;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_WARNING;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.*;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8;

public class Textures{

    final int UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD = 50;

    public static Image LoadImage(String path){
        return null;
    }

    //LoadImageRaw

    //LoadImageAnim

    //LoadImageFromMemory

    public static Image UnloadImage(Image image){
        image.data = null;
        return image;
    }

    //ExportImage

    //ExportImageAsCode

    //------------------------------------------------------------------------------------
    // Image generation functions
    //------------------------------------------------------------------------------------
    // Generate image: plain color
    Image GenImageColor(int width, int height, Color color){
        Color[] pixels = new Color[width * height];

        for (int i = 0; i < width * height; i++){
            pixels[i] = color;
        }

        return new Image(pixels, width, height, UNCOMPRESSED_R8G8B8A8.getPixForInt(), 1);
    }

    //Support image generation
    //GenImageGradientV

    //GenImageGradientH

    //GenImageGradientRadial

    //GenImageChecked

    //GenImageWhiteNoise

    //GenImagePerlinNoise

    //GenImageCellular
    //Support image generation

    //------------------------------------------------------------------------------------
    // Image manipulation functions
    //------------------------------------------------------------------------------------
    // Copy an image to a new image
    Image ImageCopy(Image image){
        Image newImage = new Image();

        int width = image.width;
        int height = image.height;
        int size = 0;

        for (int i = 0; i < image.mipmaps; i++){
            size += GetPixelDataSize(width, height, image.format);

            width /= 2;
            height /= 2;

            // Security check for NPOT textures
            if (width < 1) width = 1;
            if (height < 1) height = 1;
        }

        if (newImage.data != null){
            // NOTE: Size must be provided in bytes
            newImage.data = image.data;

            newImage.width = image.width;
            newImage.height = image.height;
            newImage.mipmaps = image.mipmaps;
            newImage.format = image.format;
        }

        return newImage;
    }


    public static Image ImageFromImage(Image image, Rectangle rectangle){
        Image result = new Image();

        // TODO: Check rec is valid?
        result.width = (int) rectangle.getWidth();
        result.height = (int) rectangle.getHeight();
        result.data = image.data;
        result.format = image.format;
        result.mipmaps = 1;

        return result;
    }

    //ImageCrop

    // Convert image data to desired format
    void ImageFormat(Image image, int newFormat){
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)){
            return;
        }

        if ((newFormat != 0) && (image.format != newFormat)){
            if ((image.format < COMPRESSED_DXT1_RGB.getPixForInt()) && (newFormat < COMPRESSED_DXT1_RGB.getPixForInt())){
                Vector4[] pixels = LoadImageDataNormalized(image);     // Supports 8 to 32 bit per channel

                // WARNING! We loose mipmaps data --> Regenerated at the end...
                image.data = null;
                image.format = newFormat;

                int k = 0;

                PixelFormat tmppf = PixelFormat.getByInt(image.getFormat());

                switch (tmppf){
                    case UNCOMPRESSED_GRAYSCALE:{
                        image.data = new int[image.width * image.height * Character.SIZE];

                        for (int i = 0; i < image.width * image.height; i++){
                            image.data[i] =
                                    (int) ((pixels[i].getX() * 0.299f + pixels[i].getY() * 0.587f + pixels[i].getZ() * 0.114f) * 255.0f);
                        }

                    }
                    break;
                    case UNCOMPRESSED_GRAY_ALPHA:{
                        image.data = new int[image.width * image.height * 2 * Character.SIZE];

                        for (int i = 0; i < image.width * image.height * 2; i += 2, k++){
                            image.data[i] =
                                    (int) ((pixels[k].getX() * 0.299f + pixels[k].getY() * 0.587f + pixels[k].getZ() * 0.114f) * 255.0f);
                            image.data[i + 1] = (int) (pixels[k].getW() * 255.0f);
                        }

                    }
                    break;
                    case UNCOMPRESSED_R5G6B5:{
                        image.data = new int[image.width * image.height * Short.SIZE];

                        char r;
                        char g;
                        char b;

                        for (int i = 0; i < image.width * image.height; i++){
                            r = (char) Math.round(pixels[i].getX() * 31.0f);
                            g = (char) Math.round(pixels[i].getY() * 63.0f);
                            b = (char) Math.round(pixels[i].getZ() * 31.0f);

                            image.data[i] = r << 11 | g << 5 | b;
                        }

                    }
                    break;
                    case UNCOMPRESSED_R8G8B8:{
                        image.data = new int[image.width * image.height * 3 * Character.SIZE];
                        k = 0;
                        for (int i = 0; i < image.width * image.height * 3; i += 3, k++){
                            image.data[i] = (int) (pixels[k].getX() * 255.0f);
                            image.data[i + 1] = (int) (pixels[k].getY() * 255.0f);
                            image.data[i + 2] = (int) (pixels[k].getZ() * 255.0f);
                        }
                    }
                    break;
                    case UNCOMPRESSED_R5G5B5A1:{
                        image.data = new int[image.width * image.height * Short.SIZE];

                        char r;
                        char g;
                        char b;
                        char a;

                        for (int i = 0; i < image.width * image.height; i++){
                            r = (char) (Math.round(pixels[i].getX() * 31.0f));
                            g = (char) (Math.round(pixels[i].getY() * 31.0f));
                            b = (char) (Math.round(pixels[i].getZ() * 31.0f));
                            a = (char) ((pixels[i].getW() > ((float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f)) ? 1 : 0);

                            image.data[i] = r << 11 | g << 6 | b << 1 | a;
                        }

                    }
                    break;
                    case UNCOMPRESSED_R4G4B4A4:{
                        image.data = new int[image.width * image.height * Short.SIZE];

                        char r;
                        char g;
                        char b;
                        char a;

                        for (int i = 0; i < image.width * image.height; i++){
                            r = (char) (Math.round(pixels[i].getX() * 15.0f));
                            g = (char) (Math.round(pixels[i].getY() * 15.0f));
                            b = (char) (Math.round(pixels[i].getZ() * 15.0f));
                            a = (char) (Math.round(pixels[i].getW() * 15.0f));

                            image.data[i] = r << 12 | g << 8 | b << 4 | a;
                        }

                    }
                    break;
                    case UNCOMPRESSED_R8G8B8A8:{
                        image.data = new int[image.width * image.height * 4 * Character.SIZE];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 4; i += 4, k++){
                            image.data[i] = (int) (pixels[k].getX() * 255.0f);
                            image.data[i + 1] = (int) (pixels[k].getY() * 255.0f);
                            image.data[i + 2] = (int) (pixels[k].getZ() * 255.0f);
                            image.data[i + 3] = (int) (pixels[k].getW() * 255.0f);
                        }
                    }
                    break;
                    case UNCOMPRESSED_R32:{
                        // WARNING: Image is converted to GRAYSCALE eqeuivalent 32bit

                        image.data = new int[image.width * image.height * Float.SIZE];

                        for (int i = 0; i < image.width * image.height; i++){
                            image.data[i] = (int) (pixels[i].getX() * 0.299f + pixels[i].getY() * 0.587f + pixels[i].getZ() * 0.114f);
                        }
                    }
                    break;
                    case UNCOMPRESSED_R32G32B32:{
                        image.data = new int[image.width * image.height * 3 * Float.SIZE];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 3; i += 3, k++){
                            image.data[i] = (int) pixels[k].getX();
                            image.data[i + 1] = (int) pixels[k].getY();
                            image.data[i + 2] = (int) pixels[k].getZ();
                        }
                    }
                    break;
                    case UNCOMPRESSED_R32G32B32A32:{
                        image.data = new int[image.width * image.height * 4 * Float.SIZE];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 4; i += 4, k++){
                            image.data[i] = (int) pixels[k].getX();
                            image.data[i + 1] = (int) pixels[k].getY();
                            image.data[i + 2] = (int) pixels[k].getZ();
                            image.data[i + 3] = (int) pixels[k].getW();
                        }
                    }
                    break;
                    default:
                        break;
                }

                pixels = null;

                // In case original image had mipmaps, generate mipmaps for formated image
                // NOTE: Original mipmaps are replaced by new ones, if custom mipmaps were used, they are lost
                if (image.mipmaps > 1){
                    image.mipmaps = 1;
                    if (SUPPORT_IMAGE_MANIPULATION){
                        if (image.data != null){
                            ImageMipmaps(image);
                        }
                    }
                }
            }
            else{
                Tracelog(LOG_WARNING, "IMAGE: Data format is compressed, can not be converted");
            }
        }
    }

    //ImageToPOT

    //support image manipulation

    // Create an image from text (default font)
    Image ImageText(String text, int fontSize, Color color){
        int defaultFontSize = 10;   // Default Font chars height in pixel
        if (fontSize < defaultFontSize) fontSize = defaultFontSize;
        int spacing = fontSize / defaultFontSize;

        Image imText = ImageTextEx(GetFontDefault(), text, (float) fontSize, (float) spacing, color);

        return imText;
    }


    // Create an image from text (custom sprite font)
    Image ImageTextEx(Font font, String text, float fontSize, float spacing, Color tint){
        int length = text.length();

        int textOffsetX = 0;            // Image drawing position X
        int textOffsetY = 0;            // Offset between lines (on line break '\n')

        // NOTE: Text image is generated at font base size, later scaled to desired font size
        Vector2 imSize = Text.MeasureTextEx(font, text, (float) font.getBaseSize(), spacing);

        // Create image to store text
        Image imText = GenImageColor((int) imSize.getX(), (int) imSize.getY(), BLANK);

        for (int i = 0; i < length; i++){
            // Get next codepoint from byte string and glyph index in font
            int codepointByteCount = 0;
            int codepoint = Text.GetNextCodepoint(text, codepointByteCount);
            int index = Text.GetGlyphIndex(font, codepoint);

            codepointByteCount = Text.getCPBC();

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f){
                codepointByteCount = 1;
            }

            if (codepoint == '\n'){
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += (font.getBaseSize() + font.getBaseSize() / 2);
                textOffsetX = 0;
            }
            else{
                if ((codepoint != ' ') && (codepoint != '\t')){
                    Rectangle rec = new Rectangle((float) (textOffsetX + font.getChars()[index].getOffsetX()),
                            (float) (textOffsetY + font.getChars()[index].getOffsetY()),
                            font.getRecs()[index].getWidth(), font.getRecs()[index].getHeight());

                    /* TODO ImageDraw
                    ImageDraw(imText, font.getChars()[index].getImage(), new Rectangle(0, 0,
                            (float) font.getChars()[index].getImage().getWidth(),
                            (float) font.getChars()[index].getImage().getHeight()), rec, tint);
                    */
                }

                if (font.getChars()[index].getAdvanceX() == 0){
                    textOffsetX += (int) (font.getRecs()[index].getWidth() + spacing);
                }
                else{
                    textOffsetX += font.getChars()[index].getAdvanceX() + (int) spacing;
                }
            }

            i += (codepointByteCount - 1);   // Move text bytes counter to next codepoint
        }

        // Scale image depending on text size
        if (fontSize > imSize.getY()){
            float scaleFactor = fontSize / imSize.getY();
            Tracelog(LOG_INFO, "IMAGE: Text scaled by factor: " + scaleFactor);

            // Using nearest-neighbor scaling algorithm for default font
            if (font.getTexture().getId() == Text.GetFontDefault().getTexture().getId()){
                ImageResizeNN(imText, (int) (imSize.getX() * scaleFactor), (int) (imSize.getY() * scaleFactor));
            }
            else{
                ImageResize(imText, (int) (imSize.getX() * scaleFactor), (int) (imSize.getY() * scaleFactor));
            }
        }

        return imText;
    }

    //ImageAlphaCrop

    //ImageAlphaClear

    //ImageAlphaMask

    //ImageAlphaPreMultiply

    // Resize and image to new size
    // NOTE: Uses stb default scaling filters (both bicubic):
    // STBIR_DEFAULT_FILTER_UPSAMPLE    STBIR_FILTER_CATMULLROM
    // STBIR_DEFAULT_FILTER_DOWNSAMPLE  STBIR_FILTER_MITCHELL   (high-quality Catmull-Rom)
    void ImageResize(Image image, int newWidth, int newHeight){
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)){
            return;
        }

        boolean fastPath = false;
        if ((image.format != UNCOMPRESSED_GRAYSCALE.getPixForInt()) && (image.format != UNCOMPRESSED_GRAY_ALPHA.getPixForInt())
                && (image.format != UNCOMPRESSED_R8G8B8.getPixForInt()) && (image.format != UNCOMPRESSED_R8G8B8A8.getPixForInt())){
            fastPath = true;
        }

        byte[] tmpb = new byte[image.data.length];

        for (int i = 0; i < image.data.length; i++){
            tmpb[i] = (byte) Math.min(image.data[i], 255);
        }

        int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
        byte[] output = new byte[newWidth * newHeight * bytesPerPixel];
        if (fastPath){

            PixelFormat tmppf = PixelFormat.getByInt(image.getFormat());

            switch (tmppf){
                case UNCOMPRESSED_GRAYSCALE:
                    stbir_resize_uint8(ByteBuffer.wrap(tmpb), image.width, image.height, 0, ByteBuffer.wrap(output),
                            newWidth, newHeight, 0, 1);
                    break;
                case UNCOMPRESSED_GRAY_ALPHA:
                    stbir_resize_uint8(ByteBuffer.wrap(tmpb), image.width, image.height, 0, ByteBuffer.wrap(output),
                            newWidth, newHeight, 0, 2);
                    break;
                case UNCOMPRESSED_R8G8B8:
                    stbir_resize_uint8(ByteBuffer.wrap(tmpb), image.width, image.height, 0, ByteBuffer.wrap(output),
                            newWidth, newHeight, 0, 3);
                    break;
                case UNCOMPRESSED_R8G8B8A8:
                    stbir_resize_uint8(ByteBuffer.wrap(tmpb), image.width, image.height, 0, ByteBuffer.wrap(output),
                            newWidth, newHeight, 0, 4);
                    break;
                default:
                    break;
            }

            int[] outputi = new int[output.length];

            for (int i = 0; i < outputi.length; i++){
                outputi[i] = output[i];
            }

            image.data = null;
            image.data = outputi;
            image.width = newWidth;
            image.height = newHeight;
        }
        else{
            // Get data as Color pixels array to work with it

            // NOTE: Color data is casted to ( char *), there shouldn't been any problem...
            stbir_resize_uint8(ByteBuffer.wrap(tmpb), image.width, image.height, 0, ByteBuffer.wrap(output),
                    newWidth, newHeight, 0, 4);

            int format = image.format;

            image.data = null;

            int[] outputi = new int[output.length];

            for (int i = 0; i < outputi.length; i++){
                outputi[i] = output[i];
            }

            image.data = outputi;
            image.width = newWidth;
            image.height = newHeight;
            image.format = UNCOMPRESSED_R8G8B8A8.getPixForInt();

            ImageFormat(image, format);  // Reformat 32bit RGBA image to original format
        }
    }

    // Resize and image to new size using Nearest-Neighbor scaling algorithm
    void ImageResizeNN(Image image, int newWidth, int newHeight){
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)){
            return;
        }

        Color[] pixels = LoadImageColors(image);
        int[] output = new int[newWidth * newHeight * 4];

        // EDIT: added +1 to account for an early rounding problem
        int xRatio = ((image.width << 16) / newWidth) + 1;
        int yRatio = ((image.height << 16) / newHeight) + 1;

        int x2, y2;
        for (int y = 0; y < newHeight; y++){
            for (int x = 0; x < newWidth; x++){
                x2 = ((x * xRatio) >> 16);
                y2 = ((y * yRatio) >> 16);

                //output[(y * newWidth) + x] = pixels[(y2 * image.width) + x2];
            }
        }

        int format = image.format;

        image.data = null;

        image.data = output;
        image.width = newWidth;
        image.height = newHeight;
        image.format = UNCOMPRESSED_R8G8B8A8.getPixForInt();

        ImageFormat(image, format);  // Reformat 32bit RGBA image to original format

        pixels = UnloadImageColors(pixels);
    }


    //ImageResizeCanvas

    // Generate all mipmap levels for a provided image
    // NOTE 1: Supports POT and NPOT images
    // NOTE 2: image.data is scaled to include mipmap levels
    // NOTE 3: Mipmaps format is the same as base image
    void ImageMipmaps(Image image){
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)){
            return;
        }

        int mipCount = 1;                   // Required mipmap levels count (including base level)
        int mipWidth = image.width;        // Base image width
        int mipHeight = image.height;      // Base image height
        int mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);  // Image data size (in bytes)

        // Count mipmap levels required
        while ((mipWidth != 1) || (mipHeight != 1)){
            if (mipWidth != 1) mipWidth /= 2;
            if (mipHeight != 1) mipHeight /= 2;

            // Security check for NPOT textures
            if (mipWidth < 1) mipWidth = 1;
            if (mipHeight < 1) mipHeight = 1;

            TracelogS("IMAGE: Next mipmap level: " + mipWidth + " x " + mipHeight + " - current size " + mipSize);

            mipCount++;
            mipSize += GetPixelDataSize(mipWidth, mipHeight, image.format);       // Add mipmap size (in bytes)
        }

        if (image.mipmaps < mipCount){
            int[] temp = image.data;

            if (temp != null){
                image.data = temp;      // Assign new pointer (new size) to store mipmaps data
            }
            else{
                Tracelog(LOG_WARNING, "IMAGE: Mipmaps required memory could not be allocated");
            }

            // Pointer to allocated memory point where store next mipmap level data
            int nextmip = image.data.length + GetPixelDataSize(image.width, image.height, image.format);

            mipWidth = image.width / 2;
            mipHeight = image.height / 2;
            mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);
            Image imCopy = ImageCopy(image);

            for (int i = 1; i < mipCount; i++){
                TracelogS("IMAGE: Generating mipmap level: " + i + " (" + mipWidth + " x " + mipHeight + ")" +
                        " - size: " + mipSize + " - offset: " + nextmip);

                ImageResize(imCopy, mipWidth, mipHeight);  // Uses internally Mitchell cubic downscale filter

                nextmip = imCopy.data.length;
                nextmip += mipSize;
                image.mipmaps++;

                mipWidth /= 2;
                mipHeight /= 2;

                // Security check for NPOT textures
                if (mipWidth < 1) mipWidth = 1;
                if (mipHeight < 1) mipHeight = 1;

                mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);
            }

            UnloadImage(imCopy);
        }
        else{
            Tracelog(LOG_WARNING, "IMAGE: Mipmaps already available");
        }
    }


    //ImageDither

    //ImageFlipVertical

    //ImageFlipHorizontal

    //ImageRotateCW

    //ImageRotateCCW

    //ImageColourTint

    //ImageColourInvert

    //ImageColourGreyscale

    //ImageColourContrast

    //ImageColourBrightness

    //ImageColourReplace

    // Load color data from image as a Color array (RGBA - 32bit)
// NOTE: Memory allocated should be freed using UnloadImageColors();
    Color[] LoadImageColors(Image image){
        if ((image.width == 0) || (image.height == 0)){
            return null;
        }

        Color[] pixels = new Color[image.width * image.height];

        PixelFormat tmpPixFmt = PixelFormat.getByInt(image.getFormat());

        if (image.format >= COMPRESSED_DXT1_RGB.getPixForInt()){
            Tracelog(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else{
            if ((image.format == UNCOMPRESSED_R32.getPixForInt()) || (image.format == UNCOMPRESSED_R32G32B32.getPixForInt()) ||
                    (image.format == UNCOMPRESSED_R32G32B32A32.getPixForInt())){
                Tracelog(LOG_WARNING, "IMAGE: Pixel format converted from 32bit to 8bit per channel");
            }

            for (int i = 0, k = 0; i < image.width * image.height; i++){
                switch (tmpPixFmt){
                    case UNCOMPRESSED_GRAYSCALE:{
                        pixels[i].setR(image.data[i]);
                        pixels[i].setG(image.data[i]);
                        pixels[i].setB(image.data[i]);
                        pixels[i].setA(255);

                    }
                    break;
                    case UNCOMPRESSED_GRAY_ALPHA:{
                        pixels[i].setR(image.data[k]);
                        pixels[i].setG(image.data[k]);
                        pixels[i].setB(image.data[k]);
                        pixels[i].setA(image.data[k + 1]);

                        k += 2;
                    }
                    break;
                    case UNCOMPRESSED_R5G5B5A1:{
                        short pixel = (short) image.data[i];

                        pixels[i].setR(((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                        pixels[i].setG(((pixel & 0b0000011111000000) >> 6) * (255 / 31));
                        pixels[i].setB(((pixel & 0b0000000000111110) >> 1) * (255 / 31));
                        pixels[i].setA((pixel & 0b0000000000000001) * 255);

                    }
                    break;
                    case UNCOMPRESSED_R5G6B5:{
                        short pixel = (short) image.data[i];

                        pixels[i].setR(((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                        pixels[i].setG(((pixel & 0b0000011111100000) >> 5) * (255 / 63));
                        pixels[i].setB(((pixel & 0b0000000000011111) * (255 / 31)));
                        pixels[i].setA(255);

                    }
                    break;
                    case UNCOMPRESSED_R4G4B4A4:{
                        short pixel = (short) image.data[i];

                        pixels[i].setR(((pixel & 0b1111000000000000) >> 12) * (255 / 15));
                        pixels[i].setG(((pixel & 0b0000111100000000) >> 8) * (255 / 15));
                        pixels[i].setB(((pixel & 0b0000000011110000) >> 4) * (255 / 15));
                        pixels[i].setA((pixel & 0b0000000000001111) * (255 / 15));

                    }
                    break;
                    case UNCOMPRESSED_R8G8B8A8:{
                        pixels[i].setR(image.data[k]);
                        pixels[i].setG(image.data[k + 1]);
                        pixels[i].setB(image.data[k + 2]);
                        pixels[i].setA(image.data[k + 3]);

                        k += 4;
                    }
                    break;
                    case UNCOMPRESSED_R8G8B8:{
                        pixels[i].setR(image.data[k]);
                        pixels[i].setG(image.data[k + 1]);
                        pixels[i].setB(image.data[k + 2]);
                        pixels[i].setA(255);

                        k += 3;
                    }
                    break;
                    case UNCOMPRESSED_R32:{
                        pixels[i].setR((int) (image.data[k] * 255.0f));
                        pixels[i].setG(0);
                        pixels[i].setB(0);
                        pixels[i].setA(255);

                    }
                    break;
                    case UNCOMPRESSED_R32G32B32:{
                        pixels[i].setR((int) (image.data[k] * 255.0f));
                        pixels[i].setG((int) (image.data[k + 1] * 255.0f));
                        pixels[i].setB((int) (image.data[k + 2] * 255.0f));
                        pixels[i].setA(255);

                        k += 3;
                    }
                    break;
                    case UNCOMPRESSED_R32G32B32A32:{
                        pixels[i].setR((int) (image.data[k] * 255.0f));
                        pixels[i].setG((int) (image.data[k] * 255.0f));
                        pixels[i].setB((int) (image.data[k] * 255.0f));
                        pixels[i].setA((int) (image.data[k] * 255.0f));

                        k += 4;
                    }
                    break;
                    default:
                        break;
                }
            }
        }

        return pixels;
    }


    //LoadImagePallette

    Color[] UnloadImageColors(Color[] color){
        return null;
    }

    Color unloadImagePalette(Color colors){
        return null;
    }

    // Get pixel data from image as Vector4 array (float normalized)
    static Vector4[] LoadImageDataNormalized(Image image){
        Vector4[] pixels = new Vector4[image.width * image.height];

        PixelFormat tmpPixFmt = PixelFormat.getByInt(image.getFormat());

        if (image.format >= COMPRESSED_DXT1_RGB.getPixForInt()){
            Tracelog(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else{
            for (int i = 0, k = 0; i < image.width * image.height; i++){
                switch (tmpPixFmt){
                    case UNCOMPRESSED_GRAYSCALE:{
                        pixels[i].setX((float) (image.data)[i] / 255.0f);
                        pixels[i].setY((float) (image.data)[i] / 255.0f);
                        pixels[i].setZ((float) (image.data)[i] / 255.0f);
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case UNCOMPRESSED_GRAY_ALPHA:{
                        pixels[i].setX((float) (image.data)[k] / 255.0f);
                        pixels[i].setY((float) (image.data)[k] / 255.0f);
                        pixels[i].setZ((float) (image.data)[k] / 255.0f);
                        pixels[i].setW((float) (image.data)[k + 1] / 255.0f);

                        k += 2;
                    }
                    break;
                    case UNCOMPRESSED_R5G5B5A1:{
                        short pixel = (short) image.data[i];

                        pixels[i].setX((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].setY((float) ((pixel & 0b0000011111000000) >> 6) * (1.0f / 31));
                        pixels[i].setZ((float) ((pixel & 0b0000000000111110) >> 1) * (1.0f / 31));
                        pixels[i].setW(((pixel & 0b0000000000000001) == 0) ? 0.0f : 1.0f);

                    }
                    break;
                    case UNCOMPRESSED_R5G6B5:{
                        short pixel = (short) image.data[i];

                        pixels[i].setX((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].setY((float) ((pixel & 0b0000011111100000) >> 5) * (1.0f / 63));
                        pixels[i].setZ((float) (pixel & 0b0000000000011111) * (1.0f / 31));
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case UNCOMPRESSED_R4G4B4A4:{
                        short pixel = (short) image.data[i];

                        pixels[i].setX((float) ((pixel & 0b1111000000000000) >> 12) * (1.0f / 15));
                        pixels[i].setY((float) ((pixel & 0b0000111100000000) >> 8) * (1.0f / 15));
                        pixels[i].setZ((float) ((pixel & 0b0000000011110000) >> 4) * (1.0f / 15));
                        pixels[i].setW((float) (pixel & 0b0000000000001111) * (1.0f / 15));

                    }
                    break;
                    case UNCOMPRESSED_R8G8B8A8:{
                        pixels[i].setX((float) (image.data)[k] / 255.0f);
                        pixels[i].setY((float) (image.data)[k + 1] / 255.0f);
                        pixels[i].setZ((float) (image.data)[k + 2] / 255.0f);
                        pixels[i].setW((float) (image.data)[k + 3] / 255.0f);

                        k += 4;
                    }
                    break;
                    case UNCOMPRESSED_R8G8B8:{
                        pixels[i].setX((float) (image.data)[k] / 255.0f);
                        pixels[i].setY((float) (image.data)[k + 1] / 255.0f);
                        pixels[i].setZ((float) (image.data)[k + 2] / 255.0f);
                        pixels[i].setW(1.0f);

                        k += 3;
                    }
                    break;
                    case UNCOMPRESSED_R32:{
                        pixels[i].setX((float) image.data[k]);
                        pixels[i].setY(0.0f);
                        pixels[i].setZ(0.0f);
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case UNCOMPRESSED_R32G32B32:{
                        pixels[i].setX((float) image.data[k]);
                        pixels[i].setY((float) image.data[k + 1]);
                        pixels[i].setZ((float) image.data[k + 2]);
                        pixels[i].setW(1.0f);

                        k += 3;
                    }
                    break;
                    case UNCOMPRESSED_R32G32B32A32:{
                        pixels[i].setX((float) image.data[k]);
                        pixels[i].setY((float) image.data[k + 1]);
                        pixels[i].setZ((float) image.data[k + 2]);
                        pixels[i].setW((float) image.data[k + 3]);

                        k += 4;
                    }
                    default:
                        break;
                }
            }
        }

        return pixels;
    }

    //GetImageAlphaBoarder

    //IMAGE DRAWING FUNCTIONS

    void ImageClearBackground(Image dst, Color color){
        for (int i = 0; i < dst.getWidth() * dst.getHeight(); ++i){
            ImageDrawPixel(dst, i % dst.getWidth(), i / dst.getHeight(), color);
        }
    }

    void ImageDrawPixel(Image dst, int x, int y, Color color){
        // Security check to avoid program crash
        if ((dst.data == null) || (x < 0) || (x >= dst.getWidth()) || (y < 0) || (y >= dst.getHeight())){
            return;
        }

        PixelFormat format = PixelFormat.getByInt(dst.getFormat());

        switch (format){
            case UNCOMPRESSED_GRAYSCALE:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);
                int gray = (int) ((coln.getX() * 0.299f + coln.getY() * 0.587f + coln.getZ() * 0.114f) * 255.0f);

                dst.data[y * dst.getWidth() + x] = gray;

            }
            break;
            case UNCOMPRESSED_GRAY_ALPHA:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);
                int gray = (int) ((coln.getX() * 0.299f + coln.getY() * 0.587f + coln.getZ() * 0.114f) * 255.0f);

                dst.data[(y * dst.getWidth() + x) * 2] = gray;
                dst.data[(y * dst.getWidth() + x) * 2 + 1] = color.getA();

            }
            break;
            case UNCOMPRESSED_R5G6B5:{
                // NOTE: Calculate R5G6B5 equivalent color
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);

                int r = (Math.round(coln.getX() * 31.0f));
                int g = (Math.round(coln.getY() * 63.0f));
                int b = (Math.round(coln.getZ() * 31.0f));

                dst.data[y * dst.getWidth() + x] = r << 11 | g << 5 | b;

            }
            break;
            case UNCOMPRESSED_R5G5B5A1:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);

                char r = (char) (Math.round(coln.getX() * 31.0f));
                char g = (char) (Math.round(coln.getY() * 31.0f));
                char b = (char) (Math.round(coln.getZ() * 31.0f));
                char a = (char) ((coln.getX() > (float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f) ? 1 : 0);

                dst.data[y * dst.getWidth() + x] = r << 11 | g << 6 | b << 1 | a;

            }
            break;
            case UNCOMPRESSED_R4G4B4A4:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);

                char r = (char) (Math.round(coln.getX() * 15.0f));
                char g = (char) (Math.round(coln.getY() * 15.0f));
                char b = (char) (Math.round(coln.getZ() * 15.0f));
                char a = (char) (Math.round(coln.getW() * 15.0f));

                dst.data[y * dst.getWidth() + x] = r << 12 | g << 8 | b << 4 | a;

            }
            break;
            case UNCOMPRESSED_R8G8B8:{
                (dst.data)[(y * dst.getWidth() + x) * 3] = color.getR();
                (dst.data)[(y * dst.getWidth() + x) * 3 + 1] = color.getG();
                (dst.data)[(y * dst.getWidth() + x) * 3 + 2] = color.getB();

            }
            break;
            case UNCOMPRESSED_R8G8B8A8:{
                (dst.data)[(y * dst.getWidth() + x) * 4] = color.getR();
                (dst.data)[(y * dst.getWidth() + x) * 4 + 1] = color.getG();
                (dst.data)[(y * dst.getWidth() + x) * 4 + 2] = color.getB();
                (dst.data)[(y * dst.getWidth() + x) * 4 + 3] = color.getA();

            }
            break;
            case UNCOMPRESSED_R32:{
                // NOTE: Calculate grayscale equivalent color (normalized to 32bit)
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);

                dst.data[y * dst.width + x] = (int) (coln.getX() * 0.299f + coln.getY() * 0.587f + coln.getZ() * 0.114f);

            }
            break;
            case UNCOMPRESSED_R32G32B32:{
                // NOTE: Calculate R32G32B32 equivalent color (normalized to 32bit)
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);

                dst.data[(y * dst.getWidth() + x) * 3] = (int) coln.getX();
                dst.data[(y * dst.getWidth() + x) * 3 + 1] = (int) coln.getY();
                dst.data[(y * dst.getWidth() + x) * 3 + 2] = (int) coln.getZ();
            }
            break;
            case UNCOMPRESSED_R32G32B32A32:{
                // NOTE: Calculate R32G32B32A32 equivalent color (normalized to 32bit)
                Vector4 coln = new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);

                dst.data[(y * dst.getWidth() + x) * 4] = (int) coln.getX();
                dst.data[(y * dst.getWidth() + x) * 4 + 1] = (int) coln.getY();
                dst.data[(y * dst.getWidth() + x) * 4 + 2] = (int) coln.getZ();
                dst.data[(y * dst.getWidth() + x) * 4 + 3] = (int) coln.getW();

            }
            break;
            default:
                break;
        }
    }

    // Draw pixel within an image (Vector version)
    void ImageDrawPixelV(Image dst, Vector2 position, Color color){
        ImageDrawPixel(dst, (int) position.getX(), (int) position.getY(), color);
    }

    // Draw line within an image
    void ImageDrawLine(Image dst, int startPosX, int startPosY, int endPosX, int endPosY, Color color){
        int m = 2 * (endPosY - startPosY);
        int slopeError = m - (endPosX - startPosX);

        for (int x = startPosX, y = startPosY; x <= endPosX; x++){
            ImageDrawPixel(dst, x, y, color);
            slopeError += m;

            if (slopeError >= 0){
                y++;
                slopeError -= 2 * (endPosX - startPosX);
            }
        }
    }

    // Draw line within an image (Vector version)
    void ImageDrawLineV(Image dst, Vector2 start, Vector2 end, Color color){
        ImageDrawLine(dst, (int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY(), color);
    }

    // Draw circle within an image
    void ImageDrawCircle(Image dst, int centerX, int centerY, int radius, Color color){
        int x = 0, y = radius;
        int decesionParameter = 3 - 2 * radius;

        while (y >= x){
            ImageDrawPixel(dst, centerX + x, centerY + y, color);
            ImageDrawPixel(dst, centerX - x, centerY + y, color);
            ImageDrawPixel(dst, centerX + x, centerY - y, color);
            ImageDrawPixel(dst, centerX - x, centerY - y, color);
            ImageDrawPixel(dst, centerX + y, centerY + x, color);
            ImageDrawPixel(dst, centerX - y, centerY + x, color);
            ImageDrawPixel(dst, centerX + y, centerY - x, color);
            ImageDrawPixel(dst, centerX - y, centerY - x, color);
            x++;

            if (decesionParameter > 0){
                y--;
                decesionParameter = decesionParameter + 4 * (x - y) + 10;
            }
            else{
                decesionParameter = decesionParameter + 4 * x + 6;
            }
        }
    }

    // Draw circle within an image (Vector version)
    void ImageDrawCircleV(Image dst, Vector2 center, int radius, Color color){
        ImageDrawCircle(dst, (int) center.getX(), (int) center.getY(), radius, color);
    }

    // Draw rectangle within an image
    void ImageDrawRectangle(Image dst, int posX, int posY, int width, int height, Color color){
        ImageDrawRectangleRec(dst, new Rectangle((float) posX, (float) posY, (float) width, (float) height), color);
    }

    // Draw rectangle within an image (Vector version)
    void ImageDrawRectangleV(Image dst, Vector2 position, Vector2 size, Color color){
        ImageDrawRectangle(dst, (int) position.getX(), (int) position.getY(), (int) size.getX(), (int) size.getY(), color);
    }

    // Draw rectangle within an image
    void ImageDrawRectangleRec(Image dst, Rectangle rec, Color color){
        // Security check to avoid program crash
        if ((dst.data == null) || (dst.getWidth() == 0) || (dst.getHeight() == 0)){
            return;
        }

        int sy = (int) rec.getY();
        int ey = sy + (int) rec.getHeight();

        int sx = (int) rec.getX();
        int ex = sx + (int) rec.getWidth();

        for (int y = sy; y < ey; y++){
            for (int x = sx; x < ex; x++){
                ImageDrawPixel(dst, x, y, color);
            }
        }
    }

    // Draw rectangle lines within an image
    void ImageDrawRectangleLines(Image dst, Rectangle rec, int thick, Color color){
        ImageDrawRectangle(dst, (int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), thick, color);
        ImageDrawRectangle(dst, (int) rec.getX(), (int) (rec.getY() + thick), thick, (int) (rec.getHeight() - thick * 2), color);
        ImageDrawRectangle(dst, (int) (rec.getX() + rec.getWidth() - thick), (int) (rec.getY() + thick), thick,
                (int) (rec.getHeight() - thick * 2), color);
        ImageDrawRectangle(dst, (int) rec.getX(), (int) (rec.getY() + rec.getHeight() - thick), (int) rec.getWidth(), thick,
                color);
    }

    /* TODO: 3/11/21
        This function uses pointers.
    // Draw an image (source) within an image (destination)
    // NOTE: Color tint is applied to source image
    void ImageDraw(Image dst, Image src, Rectangle srcRec, Rectangle dstRec, Color tint){
        // Security check to avoid program crash
        if ((dst.data == null) || (dst.width == 0) || (dst.height == 0) ||
                (src.data == null) || (src.width == 0) || (src.height == 0)){
            return;
        }

        if (dst.mipmaps > 1){
            Tracelog(LOG_WARNING, "Image drawing only applied to base mipmap level");
        }
        if (dst.format >= COMPRESSED_DXT1_RGB.getPixForInt()){
            Tracelog(LOG_WARNING, "Image drawing not supported for compressed formats");
        }
        else{
            Image srcMod = new Image();       // Source copy (in case it was required)
            Image srcPtr = src;              // Pointer to source image
            boolean useSrcMod = false;     // Track source copy required

            // Source rectangle out-of-bounds security checks
            if (srcRec.getX() < 0){
                srcRec.setWidth(srcRec.getWidth() + srcRec.getX());
                srcRec.setX(0);
            }
            if (srcRec.getY() < 0){
                srcRec.setHeight(srcRec.getHeight() + srcRec.getY());
                srcRec.setY(0);
            }
            if ((srcRec.getX() + srcRec.getWidth()) > src.width){
                srcRec.setWidth(src.getWidth() - srcRec.getX());
            }
            if ((srcRec.getY() + srcRec.getHeight()) > src.getHeight()){
                srcRec.setHeight(src.getHeight() - srcRec.getY());
            }

            // Check if source rectangle needs to be resized to destination rectangle
            // In that case, we make a copy of source and we apply all required transform
            if (((int) srcRec.getWidth() != (int) dstRec.getWidth()) || ((int) srcRec.getHeight() != (int) dstRec.getHeight())){
                srcMod = ImageFromImage(src, srcRec);   // Create image from another image
                ImageResize(srcMod, (int) dstRec.getWidth(), (int) dstRec.getHeight());   // Resize to destination rectangle
                srcRec = new Rectangle(0, 0, (float) srcMod.width, (float) srcMod.height);

                srcPtr = srcMod;
                useSrcMod = true;
            }

            // Destination rectangle out-of-bounds security checks
            if (dstRec.getX() < 0){
                srcRec.setX(-dstRec.getX());
                srcRec.setWidth(srcRec.getWidth() + dstRec.getX());
                dstRec.setX(0);
            }
            else if ((dstRec.getX() + srcRec.getWidth()) > dst.width){
                srcRec.setWidth(dst.getWidth() - dstRec.getX());
            }

            if (dstRec.getY() < 0){
                srcRec.setY(-dstRec.getY());
                srcRec.setHeight(srcRec.getHeight() + dstRec.getY());
                dstRec.setY(0);
            }
            else if ((dstRec.getY() + srcRec.getHeight()) > dst.getHeight()){
                srcRec.setHeight(dst.getHeight() - dstRec.getY());
            }

            if (dst.getWidth() < srcRec.getWidth()){
                srcRec.setWidth((float) dst.getWidth());
            }
            if (dst.getHeight() < srcRec.getHeight()){
                srcRec.setHeight((float) dst.height);
            }

            // This blitting method is quite fast! The process followed is:
            // for every pixel -> [get_src_format/get_dst_format -> blend -> format_to_dst]
            // Some optimization ideas:
            //    [x] Avoid creating source copy if not required (no resize required)
            //    [x] Optimize ImageResize() for pixel format (alternative: ImageResizeNN())
            //    [x] Optimize ColorAlphaBlend() to avoid processing (alpha = 0) and (alpha = 1)
            //    [x] Optimize ColorAlphaBlend() for faster operations (maybe avoiding divs?)
            //    [x] Consider fast path: no alpha blending required cases (src has no alpha)
            //    [x] Consider fast path: same src/dst format with no alpha -> direct line copy
            //    [-] GetPixelColor(): Return Vector4 instead of Color, easier for ColorAlphaBlend()

            Color colSrc, colDst, blend;
            boolean blendRequired = true;

            // Fast path: Avoid blend if source has no alpha to blend
            if ((tint.getA() == 255) && ((srcPtr.getFormat() == UNCOMPRESSED_GRAYSCALE.getPixForInt()) ||
                    (srcPtr.getFormat() == UNCOMPRESSED_R8G8B8.getPixForInt()) || (srcPtr.getFormat() == UNCOMPRESSED_R5G6B5.getPixForInt()))){
                blendRequired = false;
            }

            int strideDst = GetPixelDataSize(dst.width, 1, dst.format);
            int bytesPerPixelDst = strideDst / (dst.width);

            int strideSrc = GetPixelDataSize(srcPtr.width, 1, srcPtr.format);
            int bytesPerPixelSrc = strideSrc / (srcPtr.width);

            int[] pSrcBase = srcPtr.getData();
            int[] pDstBase = dst.getData();

            for (int y = 0; y < (int) srcRec.getHeight(); y++){
                int[] pSrc = pSrcBase;
                int[] pDst = pDstBase;

                // Fast path: Avoid moving pixel by pixel if no blend required and same format
                if (!blendRequired && (srcPtr.format == dst.format)){
                    //memcpy(pDst, pSrc, (int) (srcRec.getWidth()) * bytesPerPixelSrc);
                    pDst = pSrc;
                }
                else{
                    for (int x = 0; x < (int) srcRec.getWidth(); x++){
                        colSrc = GetPixelColor(pSrc, srcPtr.getFormat());
                        colDst = GetPixelColor(pDst, dst.getFormat());

                        // Fast path: Avoid blend if source has no alpha to blend
                        if (blendRequired){
                            blend = ColorAlphaBlend(colDst, colSrc, tint);
                        }
                        else{
                            blend = colSrc;
                        }

                        SetPixelColor(pDst, blend, dst.getFormat());

                        pDst += bytesPerPixelDst;
                        pSrc += bytesPerPixelSrc;
                    }
                }

                pSrcBase += strideSrc;
                pDstBase += strideDst;
            }

            if (useSrcMod) UnloadImage(srcMod);     // Unload source modified image
        }
    }
     */
    // Draw text (default font) within an image (destination)
    void ImageDrawText(Image dst, String text, int posX, int posY, int fontSize, Color color){
        Vector2 position = new Vector2((float) posX, (float) posY);

        // NOTE: For default font, sapcing is set to desired font size / default font size (10)
        ImageDrawTextEx(dst, GetFontDefault(), text, position, (float) fontSize, (float) fontSize / 10, color);
    }

    // Draw text (custom sprite font) within an image (destination)
    void ImageDrawTextEx(Image dst, Font font, String text, Vector2 position, float fontSize, float spacing, Color tint){
        Image imText = ImageTextEx(font, text, fontSize, spacing, tint);

        Rectangle srcRec = new Rectangle(0.0f, 0.0f, (float) imText.width, (float) imText.height);
        Rectangle dstRec = new Rectangle(position.getX(), position.getY(), (float) imText.width, (float) imText.height);

        //TODO ImageDraw
        //ImageDraw(dst, imText, srcRec, dstRec, Color.WHITE);

        UnloadImage(imText);
    }

    Texture2D LoadTexture(String fileName){
        Texture2D texture = new Texture2D();

        Image image = LoadImage(fileName);

        if (image.data != null){
            texture = LoadTextureFromImage(image);
            UnloadImage(image);
        }

        return texture;
    }

    public static Texture2D LoadTextureFromImage(Image image){
        Texture2D texture = new Texture2D();

        if ((image.data != null) && (image.width != 0) && (image.height != 0)){
            texture.id = rlLoadTexture(image.data, image.width, image.height, image.format, image.mipmaps);
        }
        else{
            Tracelog(LOG_WARNING, "IMAGE: Data is not valid to load texture");
        }

        texture.width = image.width;
        texture.height = image.height;
        texture.mipmaps = image.mipmaps;
        texture.format = image.format;

        return texture;
    }

    //LoadTextureCubemap

    //LoadRenderTexture

    public static void UnloadTexture(Texture2D texture){
        if (texture.getId() > 0){
            RLGL.rlUnloadTexture(texture.getId());

            Tracelog(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Unloaded texture data from VRAM (GPU)");
        }
    }

    //Unload render texture from GPU memory (VRAM)
    void UnloadRenderTexture(RenderTexture target){
        if (target.getId() > 0){
            // Color texture attached to FBO is deleted
            rlUnloadTexture(target.getTexture().getId());

            // NOTE: Depth texture/renderbuffer is automatically
            // queried and deleted before deleting framebuffer
            rlUnloadFramebuffer(target.getId());
        }
    }

    // Update GPU texture with new data
    // NOTE: pixels data must match texture.format
    void UpdateTexture(Texture2D texture, int[] pixels){
        rlUpdateTexture(texture.id, 0, 0, texture.width, texture.height, texture.format, pixels);
    }

    // Update GPU texture rectangle with new data
    // NOTE: pixels data must match texture.format
    void UpdateTextureRec(Texture2D texture, Rectangle rec, int[] pixels){
        rlUpdateTexture(texture.id, (int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), (int) rec.getHeight(),
                texture.format, pixels);
    }

    // Get pixel data from GPU texture and return an Image
    // NOTE: Compressed texture formats not supported
    Image GetTextureData(Texture2D texture){
        Image image = new Image();

        if (texture.format < COMPRESSED_DXT1_RGB.getPixForInt()){
            image.data = rlReadTexturePixels(texture);

            if (image.data != null){
                image.width = texture.width;
                image.height = texture.height;
                image.format = texture.format;
                image.mipmaps = 1;

                if (GRAPHICS_API_OPENGL_ES2){
                    // NOTE: Data retrieved on OpenGL ES 2.0 should be RGBA,
                    // coming from FBO color buffer attachment, but it seems
                    // original texture format is retrieved on RPI...
                    image.format = UNCOMPRESSED_R8G8B8A8.getPixForInt();
                }
                Tracelog(LOG_INFO, "TEXTURE: [ID " + texture.id + "] Pixel data retrieved successfully");
            }
            else{
                Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.id + "] Failed to retrieve pixel data");
            }
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.id + "] Failed to retrieve compressed pixel data");
        }

        return image;
    }

    // Get pixel data from GPU frontbuffer and return an Image (screenshot)
    Image GetScreenData(){
        Image image = new Image();

        image.width = Core.GetScreenWidth();
        image.height = Core.GetScreenHeight();
        image.mipmaps = 1;
        image.format = UNCOMPRESSED_R8G8B8A8.getPixForInt();
        image.data = rlReadScreenPixels(image.width, image.height);

        return image;
    }

    //------------------------------------------------------------------------------------
    // Texture configuration functions
    //------------------------------------------------------------------------------------
    // Generate GPU mipmaps for a texture
    void GenTextureMipmaps(Texture2D texture){
        // NOTE: NPOT textures support check inside function
        // On WebGL (OpenGL ES 2.0) NPOT textures support is limited
        rlGenerateMipmaps(texture);
    }

    public static void SetTextureFilter(Texture2D texture, int filterMode){
        switch (filterMode){

            case 0:{
                if (texture.mipmaps > 1){
                    // RL_FILTER_MIP_NEAREST - tex filter: POINT, mipmaps filter: POINT (sharp switching between mipmaps)
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_MIP_NEAREST);

                    // RL_FILTER_NEAREST - tex filter: POINT (no filter), no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_NEAREST);
                }
                else{
                    // RL_FILTER_NEAREST - tex filter: POINT (no filter), no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_NEAREST);
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_NEAREST);
                }
            }
            break;
            case 1:{
                if (texture.mipmaps > 1){
                    // RL_FILTER_LINEAR_MIP_NEAREST - tex filter: BILINEAR, mipmaps filter: POINT (sharp switching between mipmaps)
                    // Alternative: RL_FILTER_NEAREST_MIP_LINEAR - tex filter: POINT, mipmaps filter: BILINEAR (smooth transition between mipmaps)
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR_MIP_NEAREST);

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
                else{
                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR);
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
            }
            break;
            case 2:{
                if (texture.mipmaps > 1){
                    // RL_FILTER_MIP_LINEAR - tex filter: BILINEAR, mipmaps filter: BILINEAR (smooth transition between mipmaps)
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_MIP_LINEAR);

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
                else{
                    Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.id
                            + "] No mipmaps available for TRILINEAR texture filtering");

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR);
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
            }
            break;
            case 3:
                rlTextureParameters(texture.id, RL_TEXTURE_ANISOTROPIC_FILTER, 4);
                break;
            case 4:
                rlTextureParameters(texture.id, RL_TEXTURE_ANISOTROPIC_FILTER, 8);
                break;
            case 5:
                rlTextureParameters(texture.id, RL_TEXTURE_ANISOTROPIC_FILTER, 16);
                break;
            default:
                break;
        }
    }

    // Set texture wrapping mode
    void SetTextureWrap(Texture2D texture, int wrapMode){
        switch (wrapMode){
            case RL_TEXTURE_WRAP_REPEAT:{
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_REPEAT);
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_REPEAT);
            }
            break;
            case RL_TEXTURE_WRAP_CLAMP:{
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_CLAMP);
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_CLAMP);
            }
            break;
            case RL_TEXTURE_WRAP_MIRROR_REPEAT:{
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_MIRROR_REPEAT);
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_MIRROR_REPEAT);
            }
            break;
            case RL_TEXTURE_WRAP_MIRROR_CLAMP:{
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_MIRROR_CLAMP);
                rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_MIRROR_CLAMP);
            }
            break;
            default:
                break;
        }
    }

    //------------------------------------------------------------------------------------
    // Texture drawing functions
    //------------------------------------------------------------------------------------
    // Draw a Texture2D
    void DrawTexture(Texture2D texture, int posX, int posY, Color tint){
        DrawTextureEx(texture, new Vector2((float) posX, (float) posY), 0.0f, 1.0f, tint);
    }

    // Draw a Texture2D with position defined as Vector2
    void DrawTextureV(Texture2D texture, Vector2 position, Color tint){
        DrawTextureEx(texture, position, 0, 1.0f, tint);
    }

    // Draw a Texture2D with extended parameters
    void DrawTextureEx(Texture2D texture, Vector2 position, float rotation, float scale, Color tint){
        Rectangle source = new Rectangle(0.0f, 0.0f, (float) texture.width, (float) texture.height);
        Rectangle dest = new Rectangle(position.getX(), position.getY(), (float) texture.width * scale,
                (float) texture.height * scale);
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, dest, origin, rotation, tint);
    }

    // Draw a part of a texture (defined by a rectangle)
    void DrawTextureRec(Texture2D texture, Rectangle source, Vector2 position, Color tint){
        Rectangle dest = new Rectangle(position.getX(), position.getY(), Math.abs(source.getWidth()),
                Math.abs(source.getHeight()));
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, dest, origin, 0.0f, tint);
    }

    // Draw texture quad with tiling and offset parameters
    // NOTE: Tiling and offset should be provided considering normalized texture values [0..1]
    // i.e tiling = { 1.0f, 1.0f } refers to all texture, offset = { 0.5f, 0.5f } moves texture origin to center
    void DrawTextureQuad(Texture2D texture, Vector2 tiling, Vector2 offset, Rectangle quad, Color tint){
        Rectangle source = new Rectangle(offset.getX() * texture.getWidth(), offset.getY() * texture.getHeight(),
                tiling.getX() * texture.getWidth(), tiling.getY() * texture.getHeight());
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, quad, origin, 0.0f, tint);
    }

    //DrawTextureTiled

    // Draw a part of a texture (defined by a rectangle) with 'pro' parameters
    // NOTE: origin is relative to destination rectangle size
    public static void DrawTexturePro(Texture2D texture, Rectangle source, Rectangle dest, Vector2 origin,
                                      float rotation, Color tint){
        // Check if texture is valid
        if (texture.id > 0){
            float width = (float) texture.width;
            float height = (float) texture.height;

            boolean flipX = false;

            if (source.getWidth() < 0){
                flipX = true;
                source.setWidth(source.getWidth() * -1);
            }
            if (source.getHeight() < 0){
                source.setY(source.getY() - source.getHeight());
            }

            rlEnableTexture(texture.getId());

            rlPushMatrix();
            rlTranslatef(dest.getX(), dest.getY(), 0.0f);
            rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
            rlTranslatef(-origin.getX(), -origin.getY(), 0.0f);

            rlBegin(RL_QUADS);
            rlColor4ub(tint.getR(), tint.getG(), tint.getB(), tint.getA());
            rlNormal3f(0.0f, 0.0f, 1.0f);                          // Normal vector pointing towards viewer

            // Bottom-left corner for texture and quad
            if (flipX){
                rlTexCoord2f((source.getX() + source.getWidth()) / width, source.getY() / height);
            }
            else{
                rlTexCoord2f(source.getX() / width, source.getY() / height);
            }
            rlVertex2f(0.0f, 0.0f);

            // Bottom-right corner for texture and quad
            if (!flipX){
                rlTexCoord2f(source.getX() / width, (source.getY() + source.getHeight()) / height);
            }
            else{
                rlTexCoord2f((source.getX() + source.getWidth()) / width, (source.getY() + source.getHeight()) / height);
            }
            rlVertex2f(0.0f, dest.getHeight());

            // Top-right corner for texture and quad
            if (!flipX){
                rlTexCoord2f((source.getX() + source.getWidth()) / width, (source.getY() + source.getHeight()) / height);
            }
            else{
                rlTexCoord2f(source.getX() / width, (source.getY() + source.getHeight()) / height);
            }
            rlVertex2f(dest.getWidth(), dest.getHeight());

            // Top-left corner for texture and quad
            if (flipX){
                rlTexCoord2f(source.getX() / width, source.getY() / height);
            }
            else{
                rlTexCoord2f((source.getX() + source.getWidth()) / width, source.getY() / height);
            }
            rlVertex2f(dest.getWidth(), 0.0f);
            rlEnd();
            rlPopMatrix();

            rlDisableTexture();
        }
    }

    //DrawTextureNPatch

    //Fade

    // Returns hexadecimal value for a Color
    int ColorToInt(Color color){
        return ((color.getR() << 24) | (color.getG() << 16) | (color.getB() << 8) | color.getA());
    }

    // Returns color normalized as float [0..1]
    Vector4 ColorNormalize(Color color){
        return new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);
    }

    // Returns color from normalized values [0..1]
    Color ColorFromNormalized(Vector4 normalized){
        return new Color((int) normalized.getX() * 255, (int) normalized.getY() * 255, (int) normalized.getZ() * 255, (int) normalized.getZ() * 255);
    }

    //ColourToHSV

    //ColourFromHSV

    // Returns color with alpha applied, alpha goes from 0.0f to 1.0f
    Color ColorAlpha(Color color, float alpha){
        if (alpha < 0.0f){
            alpha = 0.0f;
        }
        else if (alpha > 1.0f) alpha = 1.0f;

        return new Color(color.getR(), color.getG(), color.getB(), (int) (255.0f * alpha));
    }

    // Returns src alpha-blended into dst color with tint
    Color ColorAlphaBlend(Color dst, Color src, Color tint)
    {
        Color out = Color.WHITE;

        // Apply color tint to source color
        src.setR((src.getR()*tint.getR()) >> 8);
        src.setG((src.getG()*tint.getG()) >> 8);
        src.setB((src.getB()*tint.getB()) >> 8);
        src.setA((src.getA()*tint.getA()) >> 8);

        boolean COLORALPHABLEND_FLOAT = false;
        boolean COLORALPHABLEND_INTEGERS = true;
        if(COLORALPHABLEND_INTEGERS){
            if (src.getA() == 0){
                out = dst;
            }
            else if (src.getA() == 255){
                out = src;
            }
            else{
                int alpha = src.getA() + 1;
                // We are shifting by 8 (dividing by 256), so we need to take that excess into account

                out.setA((alpha * 256 + dst.getA() * (256 - alpha)) >>8);

                if (out.getA() > 0){
                    out.setR(((src.getR() * alpha * 256 + dst.getR() * dst.getA() * (256 - alpha))/out.getA()) >>8);
                    out.setG(((src.getG() * alpha * 256 + dst.getG() * dst.getA() * (256 - alpha))/out.getA()) >>8);
                    out.setB(((src.getB() * alpha * 256 + dst.getB() * dst.getA() * (256 - alpha))/out.getA()) >>8);
                }
            }
        }
        if(COLORALPHABLEND_FLOAT){
            if (src.getA() == 0){
                out = dst;
            }
            else if (src.getA() == 255){
                out = src;
            }
            else{
                Vector4 fdst = ColorNormalize(dst);
                Vector4 fsrc = ColorNormalize(src);
                Vector4 ftint = ColorNormalize(tint);
                Vector4 fout = new Vector4();

                fout.setW(fsrc.getW() + fdst.getW() * (1.0f - fsrc.getW()));

                if (fout.getW() > 0.0f){
                    fout.setX(fsrc.getX() * fsrc.getW() + fdst.getX() * fdst.getW() * (1 - fsrc.getW()) / fout.getW());
                    fout.setY(fsrc.getY() * fsrc.getW() + fdst.getY() * fdst.getW() * (1 - fsrc.getW()) / fout.getW());
                    fout.setZ(fsrc.getZ() * fsrc.getW() + fdst.getZ() * fdst.getW() * (1 - fsrc.getW()) / fout.getW());
                }

                out = new Color((int) (fout.getX() * 255.0f), (int) (fout.getY() * 255.0f),
                        (int) (fout.getZ() * 255.0f), (int) (fout.getW() * 255.0f));
            }
        }

        return out;
    }


    // Returns a Color struct from hexadecimal value
    Color GetColor(int hexValue){
        return new Color((hexValue >> 24) & 0xFF, (hexValue >> 16) & 0xFF, (hexValue >> 8) & 0xFF, hexValue & 0xFF);
    }

    /*todo : 15MAR21
        these functions use pointers to set and receive data. Need to think of workaround
        since pointers don't exist in Java
    */
    // Get color from a pixel from certain format
    Color GetPixelColor(int[] srcPtr, int format){
        Color col = new Color();

        PixelFormat tmpPix = PixelFormat.getByInt(format);

        switch (tmpPix){
            case UNCOMPRESSED_GRAYSCALE:
                col = new Color(srcPtr[0], srcPtr[0], srcPtr[0], 255);
                break;
            case UNCOMPRESSED_GRAY_ALPHA:
                col = new Color(srcPtr[0], srcPtr[0], srcPtr[0], srcPtr[1]);
                break;
            case UNCOMPRESSED_R5G6B5:{
                col.setR((srcPtr[0] >>11)*255/31);
                col.setG(((srcPtr[0] >>5) &0b0000000000111111)*255/63);
                col.setB((srcPtr[0] &0b0000000000011111)*255/31);
                col.setA(255);

            } break;
            case UNCOMPRESSED_R5G5B5A1:{
                col.setR((srcPtr[0] >>11)*255/31);
                col.setG(((srcPtr[0] >>6) &0b0000000000011111)*255/31);
                col.setB((srcPtr[0] &0b0000000000011111)*255/31);
                col.setA((srcPtr[0] & 0b0000000000000001) == 1 ? 255 : 0);

            } break;
            case UNCOMPRESSED_R4G4B4A4:{
                col.setR((srcPtr[0] >>11)*255/15);
                col.setG(((srcPtr[0] >>8) &0b0000000000001111)*255/15);
                col.setB(((srcPtr[0] >>4) &0b0000000000001111)*255/15);
                col.setA((srcPtr[0] &0b0000000000001111)*255/15);

            } break;
            case UNCOMPRESSED_R8G8B8A8:
                col = new Color(srcPtr[0], srcPtr[1], srcPtr[2], srcPtr[3]);
                break;
            case UNCOMPRESSED_R8G8B8:
                col = new Color(srcPtr[0], srcPtr[1], srcPtr[2], 255);
                break;
            // TODO: case UNCOMPRESSED_R32: break;
            // TODO: case UNCOMPRESSED_R32G32B32: break;
            // TODO: case UNCOMPRESSED_R32G32B32A32: break;
            default:
                break;
        }

        return col;
    }

    /* Set pixel color formatted into destination pointer
    void SetPixelColor(void *dstPtr, Color color, int format){
        switch (format){
            case UNCOMPRESSED_GRAYSCALE:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = {(float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f};
                unsigned char gray = (unsigned char)((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                ((unsigned char *)dstPtr)[0] =gray;

            } break;
            case UNCOMPRESSED_GRAY_ALPHA:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = {(float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f};
                unsigned char gray = (unsigned char)((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                ((unsigned char *)dstPtr)[0] =gray;
                ((unsigned char *)dstPtr)[1] =color.a;

            } break;
            case UNCOMPRESSED_R5G6B5:{
                // NOTE: Calculate R5G6B5 equivalent color
                Vector3 coln = {(float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f};

                unsigned char r = (unsigned char)(round(coln.x * 31.0f));
                unsigned char g = (unsigned char)(round(coln.y * 63.0f));
                unsigned char b = (unsigned char)(round(coln.z * 31.0f));

                ((unsigned short *)dstPtr)[0] =(unsigned short)r << 11 | (unsigned short)g << 5 | (unsigned short)b;

            } break;
            case UNCOMPRESSED_R5G5B5A1:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = {(float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f, (float) color.a / 255.0f};

                unsigned char r = (unsigned char)(round(coln.x * 31.0f));
                unsigned char g = (unsigned char)(round(coln.y * 31.0f));
                unsigned char b = (unsigned char)(round(coln.z * 31.0f));
                unsigned char a = (coln.w > ((float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f)) ? 1 : 0;
                ;

                ((unsigned short *)dstPtr)[0] =(unsigned short)r << 11 | (unsigned short)g << 6 | (unsigned short)
                b << 1 | (unsigned short)a;

            } break;
            case UNCOMPRESSED_R4G4B4A4:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = {(float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f, (float) color.a / 255.0f};

                unsigned char r = (unsigned char)(round(coln.x * 15.0f));
                unsigned char g = (unsigned char)(round(coln.y * 15.0f));
                unsigned char b = (unsigned char)(round(coln.z * 15.0f));
                unsigned char a = (unsigned char)(round(coln.w * 15.0f));

                ((unsigned short *)dstPtr)[0] =(unsigned short)r << 12 | (unsigned short)g << 8 | (unsigned short)
                b << 4 | (unsigned short)a;

            } break;
            case UNCOMPRESSED_R8G8B8:{
                ((unsigned char *)dstPtr)[0] =color.r;
                ((unsigned char *)dstPtr)[1] =color.g;
                ((unsigned char *)dstPtr)[2] =color.b;

            } break;
            case UNCOMPRESSED_R8G8B8A8:{
                ((unsigned char *)dstPtr)[0] =color.r;
                ((unsigned char *)dstPtr)[1] =color.g;
                ((unsigned char *)dstPtr)[2] =color.b;
                ((unsigned char *)dstPtr)[3] =color.a;

            } break;
            default:
                break;
        }
    }
     */

    // Get pixel data size in bytes for certain format
    // NOTE: Size can be requested for Image or Texture data
    int GetPixelDataSize(int width, int height, int format){
        int dataSize = 0;       // Size in bytes
        int bpp = 0;            // Bits per pixel

        PixelFormat tmpPix = PixelFormat.getByInt(format);
        switch (tmpPix){
            case UNCOMPRESSED_GRAYSCALE:
                bpp = 8;
                break;
            case UNCOMPRESSED_GRAY_ALPHA:
            case UNCOMPRESSED_R5G6B5:
            case UNCOMPRESSED_R5G5B5A1:
            case UNCOMPRESSED_R4G4B4A4:
                bpp = 16;
                break;
            case UNCOMPRESSED_R8G8B8A8:
                bpp = 32;
                break;
            case UNCOMPRESSED_R8G8B8:
                bpp = 24;
                break;
            case UNCOMPRESSED_R32:
                bpp = 32;
                break;
            case UNCOMPRESSED_R32G32B32:
                bpp = 32 * 3;
                break;
            case UNCOMPRESSED_R32G32B32A32:
                bpp = 32 * 4;
                break;
            case COMPRESSED_DXT1_RGB:
            case COMPRESSED_DXT1_RGBA:
            case COMPRESSED_ETC1_RGB:
            case COMPRESSED_ETC2_RGB:
            case COMPRESSED_PVRT_RGB:
            case COMPRESSED_PVRT_RGBA:
                bpp = 4;
                break;
            case COMPRESSED_DXT3_RGBA:
            case COMPRESSED_DXT5_RGBA:
            case COMPRESSED_ETC2_EAC_RGBA:
            case COMPRESSED_ASTC_4x4_RGBA:
                bpp = 8;
                break;
            case COMPRESSED_ASTC_8x8_RGBA:
                bpp = 2;
                break;
            default:
                break;
        }

        dataSize = width * height * bpp / 8;  // Total data size in bytes

        // Most compressed formats works on 4x4 blocks,
        // if texture is smaller, minimum dataSize is 8 or 16
        if ((width < 4) && (height < 4)){
            if ((format >= COMPRESSED_DXT1_RGB.getPixForInt()) && (format < COMPRESSED_DXT3_RGBA.getPixForInt())){
                dataSize = 8;
            }
            else if ((format >= COMPRESSED_DXT3_RGBA.getPixForInt()) && (format < COMPRESSED_ASTC_8x8_RGBA.getPixForInt())){
                dataSize = 16;
            }
        }

        return dataSize;
    }

    //Load specific file formats

}
