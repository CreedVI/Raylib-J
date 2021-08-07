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
import com.creedvi.raylib.java.rlj.utils.FileIO;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.creedvi.raylib.java.rlj.Config.*;
import static com.creedvi.raylib.java.rlj.core.Color.BLANK;
import static com.creedvi.raylib.java.rlj.raymath.RayMath.DEG2RAD;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.*;
import static com.creedvi.raylib.java.rlj.text.Text.GetFontDefault;
import static com.creedvi.raylib.java.rlj.textures.NPatchInfo.NPatchType.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogS;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8;

public class Textures{

    final int UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD = 50;

    // Cubemap layouts
    enum CubemapLayoutType{
        CUBEMAP_AUTO_DETECT(0),        // Automatically detect layout type
        CUBEMAP_LINE_VERTICAL(1),          // Layout is defined by a vertical line with faces
        CUBEMAP_LINE_HORIZONTAL(2),        // Layout is defined by an horizontal line with faces
        CUBEMAP_CROSS_THREE_BY_FOUR(3),    // Layout is defined by a 3x4 cross with cubemap faces
        CUBEMAP_CROSS_FOUR_BY_THREE(4),    // Layout is defined by a 4x3 cross with cubemap faces
        CUBEMAP_PANORAMA(5)                // Layout is defined by a panorama image (equirectangular map)
        ;

        private final int layoutInt;

        CubemapLayoutType(int i){
            layoutInt = i;
        }

        public int getLayoutInt(){
            return layoutInt;
        }
    }

    public static Image LoadImage(String fileName){
        Image image = new Image();
        // Loading file to memory
        int fileSize = 0;
        byte[] fileData = null;
        try{
            fileData = FileIO.LoadFileData(fileName);
        } catch (IOException exception){
            exception.printStackTrace();
        }

        if (fileData != null){
            // Loading image from memory data
            image = LoadImageFromMemory(Core.GetFileExtension(fileName), fileData, fileSize);

            try{
                BufferedImage img = ImageIO.read(new File(fileName));
                image.width = img.getWidth();
                image.height = img.getHeight();
            } catch (IOException exception){
                exception.printStackTrace();
            }

            if (image.data != null){
                Tracelog(LOG_INFO, "IMAGE: [" + fileName + "] Data loaded successfully (" +
                        image.width + "x" + image.height + ")");
            }
            else{
                Tracelog(LOG_WARNING, "IMAGE: [" + fileName + "] Failed to load data");
            }

        }

        return image;
    }

    //LoadImageRaw

    //LoadImageAnim

    // Load image from memory buffer, fileType refers to extension: i.e. ".png"
    static Image LoadImageFromMemory(String fileType, byte[] fileData, int dataSize){
        Image image = new Image();

        String fileExtLower = fileType.toLowerCase();

        if (SUPPORT_FILEFORMAT_PNG || SUPPORT_FILEFORMAT_BMP || SUPPORT_FILEFORMAT_TGA || SUPPORT_FILEFORMAT_JPG ||
                SUPPORT_FILEFORMAT_GIF || SUPPORT_FILEFORMAT_PIC || SUPPORT_FILEFORMAT_PSD){
            if (fileExtLower.equals(".png") || fileExtLower.equals(".bmp") || fileExtLower.equals(".tga") ||
                    (fileExtLower.equals(".jpeg") || fileExtLower.equals(".jpg")) || fileExtLower.equals(".gif") ||
                    fileExtLower.equals(".pic") || fileExtLower.equals(".psd")){

                if (fileData != null){
                    int comp = 3;
                    //TODO: figure out setting the img properties
                    image.setData(fileData);
                    image.mipmaps = 1;

                    if (comp == 1){
                        image.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
                    }
                    else if (comp == 2){
                        image.format = PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
                    }
                    else if (comp == 3){
                        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8;
                    }
                    else if (comp == 4) image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
                }
            }
        }
        return image;
    }

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

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
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
            if (width < 1){
                width = 1;
            }
            if (height < 1){
                height = 1;
            }
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
            if ((image.format < PIXELFORMAT_COMPRESSED_DXT1_RGB) && (newFormat < PIXELFORMAT_COMPRESSED_DXT1_RGB)){
                Vector4[] pixels = LoadImageDataNormalized(image);     // Supports 8 to 32 bit per channel

                // WARNING! We loose mipmaps data --> Regenerated at the end...
                image.data = null;
                image.format = newFormat;

                int k = 0;

                switch (image.getFormat()){
                    case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                        short[] data = new short[image.width * image.height * Character.SIZE];

                        for (int i = 0; i < image.width * image.height; i++){
                            data[i] =
                                    (short) ((pixels[i].getX() * 0.299f + pixels[i].getY() * 0.587f + pixels[i].getZ() * 0.114f) * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                        short[] data = new short[image.width * image.height * 2 * Character.SIZE];

                        for (int i = 0; i < image.width * image.height * 2; i += 2, k++){
                            data[i] =
                                    (short) ((pixels[k].getX() * 0.299f + pixels[k].getY() * 0.587f + pixels[k].getZ() * 0.114f) * 255.0f);
                            data[i + 1] = (short) (pixels[k].getW() * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                        short[] data = new short[image.width * image.height * Short.SIZE];

                        short r, g, b;

                        for (int i = 0; i < image.width * image.height; i++){
                            r = (short) Math.round(pixels[i].getX() * 31.0f);
                            g = (short) Math.round(pixels[i].getY() * 63.0f);
                            b = (short) Math.round(pixels[i].getZ() * 31.0f);

                            data[i] = (short) (r << 11 | g << 5 | b);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                        short[] data = new short[image.width * image.height * 3 * Character.SIZE];
                        k = 0;
                        for (int i = 0; i < image.width * image.height * 3; i += 3, k++){
                            data[i] = (short) (pixels[k].getX() * 255.0f);
                            data[i + 1] = (short) (pixels[k].getY() * 255.0f);
                            data[i + 2] = (short) (pixels[k].getZ() * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                        short[] data = new short[image.width * image.height * Short.SIZE];

                        short r, g, b, a;

                        for (int i = 0; i < image.width * image.height; i++){
                            r = (short) (Math.round(pixels[i].getX() * 31.0f));
                            g = (short) (Math.round(pixels[i].getY() * 31.0f));
                            b = (short) (Math.round(pixels[i].getZ() * 31.0f));
                            a = (short) ((pixels[i].getW() > ((float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f)) ?
                                    1 : 0);

                            data[i] = (short) (r << 11 | g << 6 | b << 1 | a);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                        short[] data = new short[image.width * image.height * Short.SIZE];

                        short r, g, b, a;

                        for (int i = 0; i < image.width * image.height; i++){
                            r = (short) (Math.round(pixels[i].getX() * 15.0f));
                            g = (short) (Math.round(pixels[i].getY() * 15.0f));
                            b = (short) (Math.round(pixels[i].getZ() * 15.0f));
                            a = (short) (Math.round(pixels[i].getW() * 15.0f));

                            data[i] = (short) (r << 12 | g << 8 | b << 4 | a);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                        short[] data = new short[image.width * image.height * 4 * Character.SIZE];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 4; i += 4, k++){
                            data[i] = (short) (pixels[k].getX() * 255.0f);
                            data[i + 1] = (short) (pixels[k].getY() * 255.0f);
                            data[i + 2] = (short) (pixels[k].getZ() * 255.0f);
                            data[i + 3] = (short) (pixels[k].getW() * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32:{
                        // WARNING: Image is converted to GRAYSCALE eqeuivalent 32bit

                        short[] data = new short[image.width * image.height * Float.SIZE];

                        for (int i = 0; i < image.width * image.height; i++){
                            data[i] =
                                    (short) (pixels[i].getX() * 0.299f + pixels[i].getY() * 0.587f + pixels[i].getZ() * 0.114f);
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                        short[] data = new short[image.width * image.height * 3 * Float.SIZE];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 3; i += 3, k++){
                            data[i] = (short) pixels[k].getX();
                            data[i + 1] = (short) pixels[k].getY();
                            data[i + 2] = (short) pixels[k].getZ();
                        }
                        image.setData(data);
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                        short[] data = new short[image.width * image.height * 4 * Float.SIZE];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 4; i += 4, k++){
                            data[i] = (short) pixels[k].getX();
                            data[i + 1] = (short) pixels[k].getY();
                            data[i + 2] = (short) pixels[k].getZ();
                            data[i + 3] = (short) pixels[k].getW();
                        }
                        image.setData(data);
                    }
                    break;
                    default:
                        break;
                }

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
        if (fontSize < defaultFontSize){
            fontSize = defaultFontSize;
        }
        int spacing = fontSize / defaultFontSize;

        return ImageTextEx(GetFontDefault(), text, (float) fontSize, (float) spacing, color);
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

    /* TODO: 4/2/2021
    // Crop image depending on alpha value
    // NOTE: Threshold is defined as a percentatge: 0.0f -> 1.0f
    void ImageAlphaCrop(Image image, float threshold)
    {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        Rectangle crop = GetImageAlphaBorder(image, threshold);

        // Crop if rectangle is valid
        if (((int)crop.width != 0) && ((int)crop.height != 0)) {
            ImageCrop(image, crop);
        }
    }

    // Clear alpha channel to desired color
    // NOTE: Threshold defines the alpha limit, 0.0f to 1.0f
    void ImageAlphaClear(Image *image, Color color, float threshold)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (image->mipmaps > 1) TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image->format >= COMPRESSED_DXT1_RGB) TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        else
        {
            switch (image->format)
            {
                case UNCOMPRESSED_GRAY_ALPHA:
                {
                    unsigned char thresholdValue = (unsigned char)(threshold*255.0f);
                    for (int i = 1; i < image->width*image->height*2; i += 2)
                    {
                        if (((unsigned char *)image->data)[i] <= thresholdValue)
                        {
                            ((unsigned char *)image->data)[i - 1] = color.r;
                            ((unsigned char *)image->data)[i] = color.a;
                        }
                    }
                } break;
                case UNCOMPRESSED_R5G5B5A1:
                {
                    unsigned char thresholdValue = ((threshold < 0.5f)? 0 : 1);

                    unsigned char r = (unsigned char)(round((float)color.r*31.0f));
                    unsigned char g = (unsigned char)(round((float)color.g*31.0f));
                    unsigned char b = (unsigned char)(round((float)color.b*31.0f));
                    unsigned char a = (color.a < 128)? 0 : 1;

                    for (int i = 0; i < image->width*image->height; i++)
                    {
                        if ((((unsigned short *)image->data)[i] & 0b0000000000000001) <= thresholdValue)
                        {
                            ((unsigned short *)image->data)[i] = (unsigned short)r << 11 | (unsigned short)g << 6 | (unsigned short)b << 1 | (unsigned short)a;
                        }
                    }
                } break;
                case UNCOMPRESSED_R4G4B4A4:
                {
                    unsigned char thresholdValue = (unsigned char)(threshold*15.0f);

                    unsigned char r = (unsigned char)(round((float)color.r*15.0f));
                    unsigned char g = (unsigned char)(round((float)color.g*15.0f));
                    unsigned char b = (unsigned char)(round((float)color.b*15.0f));
                    unsigned char a = (unsigned char)(round((float)color.a*15.0f));

                    for (int i = 0; i < image->width*image->height; i++)
                    {
                        if ((((unsigned short *)image->data)[i] & 0x000f) <= thresholdValue)
                        {
                            ((unsigned short *)image->data)[i] = (unsigned short)r << 12 | (unsigned short)g << 8 | (unsigned short)b << 4 | (unsigned short)a;
                        }
                    }
                } break;
                case UNCOMPRESSED_R8G8B8A8:
                {
                    unsigned char thresholdValue = (unsigned char)(threshold*255.0f);
                    for (int i = 3; i < image->width*image->height*4; i += 4)
                    {
                        if (((unsigned char *)image->data)[i] <= thresholdValue)
                        {
                            ((unsigned char *)image->data)[i - 3] = color.r;
                            ((unsigned char *)image->data)[i - 2] = color.g;
                            ((unsigned char *)image->data)[i - 1] = color.b;
                            ((unsigned char *)image->data)[i] = color.a;
                        }
                    }
                } break;
                case UNCOMPRESSED_R32G32B32A32:
                {
                    for (int i = 3; i < image->width*image->height*4; i += 4)
                    {
                        if (((float *)image->data)[i] <= threshold)
                        {
                            ((float *)image->data)[i - 3] = (float)color.r/255.0f;
                            ((float *)image->data)[i - 2] = (float)color.g/255.0f;
                            ((float *)image->data)[i - 1] = (float)color.b/255.0f;
                            ((float *)image->data)[i] = (float)color.a/255.0f;
                        }
                    }
                } break;
                default: break;
            }
        }
    }

    // Apply alpha mask to image
    // NOTE 1: Returned image is GRAY_ALPHA (16bit) or RGBA (32bit)
    // NOTE 2: alphaMask should be same size as image
    void ImageAlphaMask(Image *image, Image alphaMask)
    {
        if ((image->width != alphaMask.width) || (image->height != alphaMask.height))
        {
            TRACELOG(LOG_WARNING, "IMAGE: Alpha mask must be same size as image");
        }
        else if (image->format >= COMPRESSED_DXT1_RGB)
        {
            TRACELOG(LOG_WARNING, "IMAGE: Alpha mask can not be applied to compressed data formats");
        }
        else
        {
            // Force mask to be Grayscale
            Image mask = ImageCopy(alphaMask);
            if (mask.format != UNCOMPRESSED_GRAYSCALE) ImageFormat(&mask, UNCOMPRESSED_GRAYSCALE);

            // In case image is only grayscale, we just add alpha channel
            if (image->format == UNCOMPRESSED_GRAYSCALE)
            {
                unsigned char *data = (unsigned char *)RL_MALLOC(image->width*image->height*2);

                // Apply alpha mask to alpha channel
                for (int i = 0, k = 0; (i < mask.width*mask.height) || (i < image->width*image->height); i++, k += 2)
                {
                    data[k] = ((unsigned char *)image->data)[i];
                    data[k + 1] = ((unsigned char *)mask.data)[i];
                }

                RL_FREE(image->data);
                image->data = data;
                image->format = UNCOMPRESSED_GRAY_ALPHA;
            }
            else
            {
                // Convert image to RGBA
                if (image->format != UNCOMPRESSED_R8G8B8A8) ImageFormat(image, UNCOMPRESSED_R8G8B8A8);

                // Apply alpha mask to alpha channel
                for (int i = 0, k = 3; (i < mask.width*mask.height) || (i < image->width*image->height); i++, k += 4)
                {
                    ((unsigned char *)image->data)[k] = ((unsigned char *)mask.data)[i];
                }
            }

            UnloadImage(mask);
        }
    }

    // Premultiply alpha channel
    void ImageAlphaPremultiply(Image *image)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        float alpha = 0.0f;
        Color *pixels = LoadImageColors(*image);

        for (int i = 0; i < image->width*image->height; i++)
        {
            if (pixels[i].a == 0)
            {
                pixels[i].r = 0;
                pixels[i].g = 0;
                pixels[i].b = 0;
            }
            else if (pixels[i].a < 255)
            {
                alpha = (float)pixels[i].a/255.0f;
                pixels[i].r = (unsigned char)((float)pixels[i].r*alpha);
                pixels[i].g = (unsigned char)((float)pixels[i].g*alpha);
                pixels[i].b = (unsigned char)((float)pixels[i].b*alpha);
            }
        }

        RL_FREE(image->data);

        int format = image->format;
        image->data = pixels;
        image->format = UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }
    */

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
        if ((image.format != PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) && (image.format != PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA)
                && (image.format != PIXELFORMAT_UNCOMPRESSED_R8G8B8) && (image.format != PIXELFORMAT_UNCOMPRESSED_R8G8B8A8)){
            fastPath = true;
        }

        ByteBuffer tmpb = MemoryUtil.memAlloc(image.data.getSize());

        for (int i = 0; i < image.data.getSize(); i++){
            tmpb.put(i, (byte) image.data.getElem(i));
        }

        int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
        ByteBuffer output = MemoryUtil.memAlloc(newWidth * newHeight * bytesPerPixel);
        if (fastPath){

            switch (image.getFormat()){
                case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                            newWidth, newHeight, 0, 1);
                    break;
                case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                            newWidth, newHeight, 0, 2);
                    break;
                case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                            newWidth, newHeight, 0, 3);
                    break;
                case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                            newWidth, newHeight, 0, 4);
                    break;
                default:
                    break;
            }

            short[] outputi = new short[output.capacity()];

            for (int i = 0; i < outputi.length; i++){
                outputi[i] = output.get(i);
            }

            image.data = null;
            image.setData(outputi);
            image.width = newWidth;
            image.height = newHeight;
        }
        else{
            // Get data as Color pixels array to work with it

            // NOTE: Color data is casted to ( char *), there shouldn't been any problem...
            stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                    newWidth, newHeight, 0, 4);

            int format = image.format;

            image.data = null;

            short[] outputi = new short[output.capacity()];

            for (int i = 0; i < outputi.length; i++){
                outputi[i] = output.get(i);
            }

            image.setData(outputi);
            image.width = newWidth;
            image.height = newHeight;
            image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

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
        short[] output = new short[newWidth * newHeight * 4];

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

        image.setData(output);
        image.width = newWidth;
        image.height = newHeight;
        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);  // Reformat 32bit RGBA image to original format

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
            if (mipWidth != 1){
                mipWidth /= 2;
            }
            if (mipHeight != 1){
                mipHeight /= 2;
            }

            // Security check for NPOT textures
            if (mipWidth < 1){
                mipWidth = 1;
            }
            if (mipHeight < 1){
                mipHeight = 1;
            }

            TracelogS("IMAGE: Next mipmap level: " + mipWidth + " x " + mipHeight + " - current size " + mipSize);

            mipCount++;
            mipSize += GetPixelDataSize(mipWidth, mipHeight, image.format);       // Add mipmap size (in bytes)
        }

        if (image.mipmaps < mipCount){
            DataBuffer temp = image.data;

            if (temp != null){
                image.data = temp;      // Assign new pointer (new size) to store mipmaps data
            }
            else{
                Tracelog(LOG_WARNING, "IMAGE: Mipmaps required memory could not be allocated");
            }

            // Pointer to allocated memory point where store next mipmap level data
            int nextmip = image.data.getSize() + GetPixelDataSize(image.width, image.height, image.format);

            mipWidth = image.width / 2;
            mipHeight = image.height / 2;
            mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);
            Image imCopy = ImageCopy(image);

            for (int i = 1; i < mipCount; i++){
                TracelogS("IMAGE: Generating mipmap level: " + i + " (" + mipWidth + " x " + mipHeight + ")" +
                        " - size: " + mipSize + " - offset: " + nextmip);

                ImageResize(imCopy, mipWidth, mipHeight);  // Uses internally Mitchell cubic downscale filter

                nextmip = imCopy.data.getSize();
                nextmip += mipSize;
                image.mipmaps++;

                mipWidth /= 2;
                mipHeight /= 2;

                // Security check for NPOT textures
                if (mipWidth < 1){
                    mipWidth = 1;
                }
                if (mipHeight < 1){
                    mipHeight = 1;
                }

                mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);
            }

            UnloadImage(imCopy);
        }
        else{
            Tracelog(LOG_WARNING, "IMAGE: Mipmaps already available");
        }
    }

    /* TODO: 4/2/2021
    // Dither image data to 16bpp or lower (Floyd-Steinberg dithering)
    // NOTE: In case selected bpp do not represent an known 16bit format,
    // dithered data is stored in the LSB part of the unsigned short
    void ImageDither(Image *image, int rBpp, int gBpp, int bBpp, int aBpp)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (image->format >= COMPRESSED_DXT1_RGB)
        {
            TRACELOG(LOG_WARNING, "IMAGE: Compressed data formats can not be dithered");
            return;
        }

        if ((rBpp + gBpp + bBpp + aBpp) > 16)
        {
            TRACELOG(LOG_WARNING, "IMAGE: Unsupported dithering bpps (%ibpp), only 16bpp or lower modes supported", (rBpp+gBpp+bBpp+aBpp));
        }
        else
        {
            Color *pixels = LoadImageColors(*image);

            RL_FREE(image->data);      // free old image data

            if ((image->format != UNCOMPRESSED_R8G8B8) && (image->format != UNCOMPRESSED_R8G8B8A8))
            {
                TRACELOG(LOG_WARNING, "IMAGE: Format is already 16bpp or lower, dithering could have no effect");
            }

            // Define new image format, check if desired bpp match internal known format
            if ((rBpp == 5) && (gBpp == 6) && (bBpp == 5) && (aBpp == 0)) image->format = UNCOMPRESSED_R5G6B5;
            else if ((rBpp == 5) && (gBpp == 5) && (bBpp == 5) && (aBpp == 1)) image->format = UNCOMPRESSED_R5G5B5A1;
            else if ((rBpp == 4) && (gBpp == 4) && (bBpp == 4) && (aBpp == 4)) image->format = UNCOMPRESSED_R4G4B4A4;
            else
            {
                image->format = 0;
                TRACELOG(LOG_WARNING, "IMAGE: Unsupported dithered OpenGL internal format: %ibpp (R%iG%iB%iA%i)", (rBpp+gBpp+bBpp+aBpp), rBpp, gBpp, bBpp, aBpp);
            }

            // NOTE: We will store the dithered data as unsigned short (16bpp)
            image->data = (unsigned short *)RL_MALLOC(image->width*image->height*sizeof(unsigned short));

            Color oldPixel = WHITE;
            Color newPixel = WHITE;

            int rError, gError, bError;
            unsigned short rPixel, gPixel, bPixel, aPixel;   // Used for 16bit pixel composition

        #define MIN(a,b) (((a)<(b))?(a):(b))

            for (int y = 0; y < image->height; y++)
            {
                for (int x = 0; x < image->width; x++)
                {
                    oldPixel = pixels[y*image->width + x];

                    // NOTE: New pixel obtained by bits truncate, it would be better to round values (check ImageFormat())
                    newPixel.r = oldPixel.r >> (8 - rBpp);     // R bits
                    newPixel.g = oldPixel.g >> (8 - gBpp);     // G bits
                    newPixel.b = oldPixel.b >> (8 - bBpp);     // B bits
                    newPixel.a = oldPixel.a >> (8 - aBpp);     // A bits (not used on dithering)

                    // NOTE: Error must be computed between new and old pixel but using same number of bits!
                    // We want to know how much color precision we have lost...
                    rError = (int)oldPixel.r - (int)(newPixel.r << (8 - rBpp));
                    gError = (int)oldPixel.g - (int)(newPixel.g << (8 - gBpp));
                    bError = (int)oldPixel.b - (int)(newPixel.b << (8 - bBpp));

                    pixels[y*image->width + x] = newPixel;

                    // NOTE: Some cases are out of the array and should be ignored
                    if (x < (image->width - 1))
                    {
                        pixels[y*image->width + x+1].r = MIN((int)pixels[y*image->width + x+1].r + (int)((float)rError*7.0f/16), 0xff);
                        pixels[y*image->width + x+1].g = MIN((int)pixels[y*image->width + x+1].g + (int)((float)gError*7.0f/16), 0xff);
                        pixels[y*image->width + x+1].b = MIN((int)pixels[y*image->width + x+1].b + (int)((float)bError*7.0f/16), 0xff);
                    }

                    if ((x > 0) && (y < (image->height - 1)))
                    {
                        pixels[(y+1)*image->width + x-1].r = MIN((int)pixels[(y+1)*image->width + x-1].r + (int)((float)rError*3.0f/16), 0xff);
                        pixels[(y+1)*image->width + x-1].g = MIN((int)pixels[(y+1)*image->width + x-1].g + (int)((float)gError*3.0f/16), 0xff);
                        pixels[(y+1)*image->width + x-1].b = MIN((int)pixels[(y+1)*image->width + x-1].b + (int)((float)bError*3.0f/16), 0xff);
                    }

                    if (y < (image->height - 1))
                    {
                        pixels[(y+1)*image->width + x].r = MIN((int)pixels[(y+1)*image->width + x].r + (int)((float)rError*5.0f/16), 0xff);
                        pixels[(y+1)*image->width + x].g = MIN((int)pixels[(y+1)*image->width + x].g + (int)((float)gError*5.0f/16), 0xff);
                        pixels[(y+1)*image->width + x].b = MIN((int)pixels[(y+1)*image->width + x].b + (int)((float)bError*5.0f/16), 0xff);
                    }

                    if ((x < (image->width - 1)) && (y < (image->height - 1)))
                    {
                        pixels[(y+1)*image->width + x+1].r = MIN((int)pixels[(y+1)*image->width + x+1].r + (int)((float)rError*1.0f/16), 0xff);
                        pixels[(y+1)*image->width + x+1].g = MIN((int)pixels[(y+1)*image->width + x+1].g + (int)((float)gError*1.0f/16), 0xff);
                        pixels[(y+1)*image->width + x+1].b = MIN((int)pixels[(y+1)*image->width + x+1].b + (int)((float)bError*1.0f/16), 0xff);
                    }

                    rPixel = (unsigned short)newPixel.r;
                    gPixel = (unsigned short)newPixel.g;
                    bPixel = (unsigned short)newPixel.b;
                    aPixel = (unsigned short)newPixel.a;

                    ((unsigned short *)image->data)[y*image->width + x] = (rPixel << (gBpp + bBpp + aBpp)) | (gPixel << (bBpp + aBpp)) | (bPixel << aBpp) | aPixel;
                }
            }

            UnloadImageColors(pixels);
        }
    }

    // Flip image vertically
    void ImageFlipVertical(Image *image)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (image->mipmaps > 1) TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image->format >= COMPRESSED_DXT1_RGB) TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        else
        {
            int bytesPerPixel = GetPixelDataSize(1, 1, image->format);
            unsigned char *flippedData = (unsigned char *)RL_MALLOC(image->width*image->height*bytesPerPixel);

            for (int i = (image->height - 1), offsetSize = 0; i >= 0; i--)
            {
                memcpy(flippedData + offsetSize, ((unsigned char *)image->data) + i*image->width*bytesPerPixel, image->width*bytesPerPixel);
                offsetSize += image->width*bytesPerPixel;
            }

            RL_FREE(image->data);
            image->data = flippedData;
        }
    }

    // Flip image horizontally
    void ImageFlipHorizontal(Image *image)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (image->mipmaps > 1) TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image->format >= COMPRESSED_DXT1_RGB) TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        else
        {
            int bytesPerPixel = GetPixelDataSize(1, 1, image->format);
            unsigned char *flippedData = (unsigned char *)RL_MALLOC(image->width*image->height*bytesPerPixel);

            for (int y = 0; y < image->height; y++)
            {
                for (int x = 0; x < image->width; x++)
                {
                    // OPTION 1: Move pixels with memcopy()
                    //memcpy(flippedData + (y*image->width + x)*bytesPerPixel, ((unsigned char *)image->data) + (y*image->width + (image->width - 1 - x))*bytesPerPixel, bytesPerPixel);

                    // OPTION 2: Just copy data pixel by pixel
                    for (int i = 0; i < bytesPerPixel; i++) flippedData[(y*image->width + x)*bytesPerPixel + i] = ((unsigned char *)image->data)[(y*image->width + (image->width - 1 - x))*bytesPerPixel + i];
                }
            }

            RL_FREE(image->data);
            image->data = flippedData;

        /*
        // OPTION 3: Faster implementation (specific for 32bit pixels)
        // NOTE: It does not require additional allocations
        uint32_t *ptr = (uint32_t *)image->data;
        for (int y = 0; y < image->height; y++)
        {
            for (int x = 0; x < image->width/2; x++)
            {
                uint32_t backup = ptr[y*image->width + x];
                ptr[y*image->width + x] = ptr[y*image->width + (image->width - 1 - x)];
                ptr[y*image->width + (image->width - 1 - x)] = backup;
            }
        }
        *
        }
    }

    // Rotate image clockwise 90deg
    void ImageRotateCW(Image *image)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (image->mipmaps > 1) TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image->format >= COMPRESSED_DXT1_RGB) TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        else
        {
            int bytesPerPixel = GetPixelDataSize(1, 1, image->format);
            unsigned char *rotatedData = (unsigned char *)RL_MALLOC(image->width*image->height*bytesPerPixel);

            for (int y = 0; y < image->height; y++)
            {
                for (int x = 0; x < image->width; x++)
                {
                    //memcpy(rotatedData + (x*image->height + (image->height - y - 1))*bytesPerPixel, ((unsigned char *)image->data) + (y*image->width + x)*bytesPerPixel, bytesPerPixel);
                    for (int i = 0; i < bytesPerPixel; i++) rotatedData[(x*image->height + (image->height - y - 1))*bytesPerPixel + i] = ((unsigned char *)image->data)[(y*image->width + x)*bytesPerPixel + i];
                }
            }

            RL_FREE(image->data);
            image->data = rotatedData;
            int width = image->width;
            int height = image-> height;

            image->width = height;
            image->height = width;
        }
    }

    // Rotate image counter-clockwise 90deg
    void ImageRotateCCW(Image *image)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (image->mipmaps > 1) TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image->format >= COMPRESSED_DXT1_RGB) TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        else
        {
            int bytesPerPixel = GetPixelDataSize(1, 1, image->format);
            unsigned char *rotatedData = (unsigned char *)RL_MALLOC(image->width*image->height*bytesPerPixel);

            for (int y = 0; y < image->height; y++)
            {
                for (int x = 0; x < image->width; x++)
                {
                    //memcpy(rotatedData + (x*image->height + y))*bytesPerPixel, ((unsigned char *)image->data) + (y*image->width + (image->width - x - 1))*bytesPerPixel, bytesPerPixel);
                    for (int i = 0; i < bytesPerPixel; i++) rotatedData[(x*image->height + y)*bytesPerPixel + i] = ((unsigned char *)image->data)[(y*image->width + (image->width - x - 1))*bytesPerPixel + i];
                }
            }

            RL_FREE(image->data);
            image->data = rotatedData;
            int width = image->width;
            int height = image-> height;

            image->width = height;
            image->height = width;
        }
    }

    // Modify image color: tint
    void ImageColorTint(Image *image, Color color)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        Color *pixels = LoadImageColors(*image);

        float cR = (float)color.r/255;
        float cG = (float)color.g/255;
        float cB = (float)color.b/255;
        float cA = (float)color.a/255;

        for (int y = 0; y < image->height; y++)
        {
            for (int x = 0; x < image->width; x++)
            {
                int index = y * image->width + x;
                unsigned char r = (unsigned char)(((float)pixels[index].r/255*cR)*255.0f);
                unsigned char g = (unsigned char)(((float)pixels[index].g/255*cG)*255.0f);
                unsigned char b = (unsigned char)(((float)pixels[index].b/255*cB)*255.0f);
                unsigned char a = (unsigned char)(((float)pixels[index].a/255*cA)*255.0f);

                pixels[y*image->width + x].r = r;
                pixels[y*image->width + x].g = g;
                pixels[y*image->width + x].b = b;
                pixels[y*image->width + x].a = a;
            }
        }

        int format = image->format;
        RL_FREE(image->data);

        image->data = pixels;
        image->format = UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: invert
    void ImageColorInvert(Image *image)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        Color *pixels = LoadImageColors(*image);

        for (int y = 0; y < image->height; y++)
        {
            for (int x = 0; x < image->width; x++)
            {
                pixels[y*image->width + x].r = 255 - pixels[y*image->width + x].r;
                pixels[y*image->width + x].g = 255 - pixels[y*image->width + x].g;
                pixels[y*image->width + x].b = 255 - pixels[y*image->width + x].b;
            }
        }

        int format = image->format;
        RL_FREE(image->data);

        image->data = pixels;
        image->format = UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: grayscale
    void ImageColorGrayscale(Image *image)
    {
        ImageFormat(image, UNCOMPRESSED_GRAYSCALE);
    }

    // Modify image color: contrast
// NOTE: Contrast values between -100 and 100
    void ImageColorContrast(Image *image, float contrast)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (contrast < -100) contrast = -100;
        if (contrast > 100) contrast = 100;

        contrast = (100.0f + contrast)/100.0f;
        contrast *= contrast;

        Color *pixels = LoadImageColors(*image);

        for (int y = 0; y < image->height; y++)
        {
            for (int x = 0; x < image->width; x++)
            {
                float pR = (float)pixels[y*image->width + x].r/255.0f;
                pR -= 0.5;
                pR *= contrast;
                pR += 0.5;
                pR *= 255;
                if (pR < 0) pR = 0;
                if (pR > 255) pR = 255;

                float pG = (float)pixels[y*image->width + x].g/255.0f;
                pG -= 0.5;
                pG *= contrast;
                pG += 0.5;
                pG *= 255;
                if (pG < 0) pG = 0;
                if (pG > 255) pG = 255;

                float pB = (float)pixels[y*image->width + x].b/255.0f;
                pB -= 0.5;
                pB *= contrast;
                pB += 0.5;
                pB *= 255;
                if (pB < 0) pB = 0;
                if (pB > 255) pB = 255;

                pixels[y*image->width + x].r = (unsigned char)pR;
                pixels[y*image->width + x].g = (unsigned char)pG;
                pixels[y*image->width + x].b = (unsigned char)pB;
            }
        }

        int format = image->format;
        RL_FREE(image->data);

        image->data = pixels;
        image->format = UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: brightness
// NOTE: Brightness values between -255 and 255
    void ImageColorBrightness(Image *image, int brightness)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        if (brightness < -255) brightness = -255;
        if (brightness > 255) brightness = 255;

        Color *pixels = LoadImageColors(*image);

        for (int y = 0; y < image->height; y++)
        {
            for (int x = 0; x < image->width; x++)
            {
                int cR = pixels[y*image->width + x].r + brightness;
                int cG = pixels[y*image->width + x].g + brightness;
                int cB = pixels[y*image->width + x].b + brightness;

                if (cR < 0) cR = 1;
                if (cR > 255) cR = 255;

                if (cG < 0) cG = 1;
                if (cG > 255) cG = 255;

                if (cB < 0) cB = 1;
                if (cB > 255) cB = 255;

                pixels[y*image->width + x].r = (unsigned char)cR;
                pixels[y*image->width + x].g = (unsigned char)cG;
                pixels[y*image->width + x].b = (unsigned char)cB;
            }
        }

        int format = image->format;
        RL_FREE(image->data);

        image->data = pixels;
        image->format = UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: replace color
    void ImageColorReplace(Image *image, Color color, Color replace)
    {
        // Security check to avoid program crash
        if ((image->data == NULL) || (image->width == 0) || (image->height == 0)) return;

        Color *pixels = LoadImageColors(*image);

        for (int y = 0; y < image->height; y++)
        {
            for (int x = 0; x < image->width; x++)
            {
                if ((pixels[y*image->width + x].r == color.r) &&
                        (pixels[y*image->width + x].g == color.g) &&
                        (pixels[y*image->width + x].b == color.b) &&
                        (pixels[y*image->width + x].a == color.a))
                {
                    pixels[y*image->width + x].r = replace.r;
                    pixels[y*image->width + x].g = replace.g;
                    pixels[y*image->width + x].b = replace.b;
                    pixels[y*image->width + x].a = replace.a;
                }
            }
        }

        int format = image->format;
        RL_FREE(image->data);

        image->data = pixels;
        image->format = UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }
    */

    // Load color data from image as a Color array (RGBA - 32bit)
    // NOTE: Memory allocated should be freed using UnloadImageColors();
    public static Color[] LoadImageColors(Image image){
        if ((image.width == 0) || (image.height == 0)){
            return null;
        }

        Color[] pixels = new Color[image.width * image.height];

        if (image.format >= PIXELFORMAT_COMPRESSED_DXT1_RGB){
            Tracelog(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else{
            if ((image.format == PIXELFORMAT_UNCOMPRESSED_R32) || (image.format == PIXELFORMAT_UNCOMPRESSED_R32G32B32) ||
                    (image.format == PIXELFORMAT_UNCOMPRESSED_R32G32B32A32)){
                Tracelog(LOG_WARNING, "IMAGE: Pixel format converted from 32bit to 8bit per channel");
            }

            for (int i = 0, k = 0; i < image.width * image.height; i++){
                switch (image.getFormat()){
                    case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                        pixels[i].setR(image.data.getElem(i));
                        pixels[i].setG(image.data.getElem(i));
                        pixels[i].setB(image.data.getElem(i));
                        pixels[i].setA(255);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                        pixels[i].setR(image.data.getElem(k));
                        pixels[i].setG(image.data.getElem(k));
                        pixels[i].setB(image.data.getElem(k));
                        pixels[i].setA(image.data.getElem(k + 1));

                        k += 2;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                        short pixel = (short) image.data.getElem(i);

                        pixels[i].setR(((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                        pixels[i].setG(((pixel & 0b0000011111000000) >> 6) * (255 / 31));
                        pixels[i].setB(((pixel & 0b0000000000111110) >> 1) * (255 / 31));
                        pixels[i].setA((pixel & 0b0000000000000001) * 255);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                        short pixel = (short) image.data.getElem(i);

                        pixels[i].setR(((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                        pixels[i].setG(((pixel & 0b0000011111100000) >> 5) * (255 / 63));
                        pixels[i].setB((pixel & 0b0000000000011111) * (255 / 31));
                        pixels[i].setA(255);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                        short pixel = (short) image.data.getElem(i);

                        pixels[i].setR(((pixel & 0b1111000000000000) >> 12) * (255 / 15));
                        pixels[i].setG(((pixel & 0b0000111100000000) >> 8) * (255 / 15));
                        pixels[i].setB(((pixel & 0b0000000011110000) >> 4) * (255 / 15));
                        pixels[i].setA((pixel & 0b0000000000001111) * (255 / 15));

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                        pixels[i].setR(image.data.getElem(k));
                        pixels[i].setG(image.data.getElem(k + 1));
                        pixels[i].setB(image.data.getElem(k + 2));
                        pixels[i].setA(image.data.getElem(k + 3));

                        k += 4;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                        pixels[i].setR(image.data.getElem(k));
                        pixels[i].setG(image.data.getElem(k + 1));
                        pixels[i].setB(image.data.getElem(k + 2));
                        pixels[i].setA(255);

                        k += 3;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32:{
                        pixels[i].setR((int) (image.data.getElem(k) * 255.0f));
                        pixels[i].setG(0);
                        pixels[i].setB(0);
                        pixels[i].setA(255);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                        pixels[i].setR((int) (image.data.getElem(k) * 255.0f));
                        pixels[i].setG((int) (image.data.getElem(k + 1) * 255.0f));
                        pixels[i].setB((int) (image.data.getElem(k + 2) * 255.0f));
                        pixels[i].setA(255);

                        k += 3;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                        pixels[i].setR((int) (image.data.getElem(k) * 255.0f));
                        pixels[i].setG((int) (image.data.getElem(k) * 255.0f));
                        pixels[i].setB((int) (image.data.getElem(k) * 255.0f));
                        pixels[i].setA((int) (image.data.getElem(k) * 255.0f));

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


    // Load colors palette from image as a Color array (RGBA - 32bit)
    // NOTE: Memory allocated should be freed using UnloadImagePalette()
    Color[] LoadImagePalette(Image image, int maxPaletteSize, int colorsCount){

        int palCount = 0;
        Color[] palette = null;
        Color[] pixels = LoadImageColors(image);

        if (pixels != null){
            palette = new Color[maxPaletteSize];

            for (int i = 0; i < maxPaletteSize; i++) palette[i] = BLANK;   // Set all colors to BLANK

            for (int i = 0; i < image.width * image.height; i++){
                if (pixels[i].a > 0){
                    boolean colorInPalette = false;

                    // Check if the color is already on palette
                    for (int j = 0; j < maxPaletteSize; j++){
                        if ((pixels[i].r == palette[j].r) && (pixels[i].g == palette[j].g) && (pixels[i].b == palette[j].b) && (pixels[i].a == palette[j].a)){
                            colorInPalette = true;
                            break;
                        }
                    }

                    // Store color if not on the palette
                    if (!colorInPalette){
                        palette[palCount] = pixels[i];      // Add pixels[i] to palette
                        palCount++;

                        // We reached the limit of colors supported by palette
                        if (palCount >= maxPaletteSize){
                            i = image.width * image.height;   // Finish palette get
                            Tracelog(LOG_WARNING, "IMAGE: Palette is greater than " + maxPaletteSize + " colors");
                        }
                    }
                }
            }

            UnloadImageColors(pixels);
        }

        colorsCount = palCount;

        return palette;
    }

    Color[] UnloadImageColors(Color[] color){
        return null;
    }

    Color unloadImagePalette(Color colors){
        return null;
    }

    // Get pixel data from image as Vector4 array (float normalized)
    static Vector4[] LoadImageDataNormalized(Image image){
        Vector4[] pixels = new Vector4[image.width * image.height];

        if (image.format >= PIXELFORMAT_COMPRESSED_DXT1_RGB){
            Tracelog(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else{
            for (int i = 0, k = 0; i < image.width * image.height; i++){
                switch (image.getFormat()){
                    case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                        pixels[i].setX((float) image.data.getElem(i) / 255.0f);
                        pixels[i].setY((float) image.data.getElem(i) / 255.0f);
                        pixels[i].setZ((float) image.data.getElem(i) / 255.0f);
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                        pixels[i].setX((float) image.data.getElem(k) / 255.0f);
                        pixels[i].setY((float) image.data.getElem(k) / 255.0f);
                        pixels[i].setZ((float) image.data.getElem(k) / 255.0f);
                        pixels[i].setW((float) image.data.getElem(k + 1) / 255.0f);

                        k += 2;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                        short pixel = (short) image.data.getElem(i);

                        pixels[i].setX((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].setY((float) ((pixel & 0b0000011111000000) >> 6) * (1.0f / 31));
                        pixels[i].setZ((float) ((pixel & 0b0000000000111110) >> 1) * (1.0f / 31));
                        pixels[i].setW(((pixel & 0b0000000000000001) == 0) ? 0.0f : 1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                        short pixel = (short) image.data.getElem(i);

                        pixels[i].setX((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].setY((float) ((pixel & 0b0000011111100000) >> 5) * (1.0f / 63));
                        pixels[i].setZ((float) (pixel & 0b0000000000011111) * (1.0f / 31));
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                        short pixel = (short) image.data.getElem(i);

                        pixels[i].setX((float) ((pixel & 0b1111000000000000) >> 12) * (1.0f / 15));
                        pixels[i].setY((float) ((pixel & 0b0000111100000000) >> 8) * (1.0f / 15));
                        pixels[i].setZ((float) ((pixel & 0b0000000011110000) >> 4) * (1.0f / 15));
                        pixels[i].setW((float) (pixel & 0b0000000000001111) * (1.0f / 15));

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                        pixels[i].setX((float) image.data.getElem(k) / 255.0f);
                        pixels[i].setY((float) image.data.getElem(k + 1) / 255.0f);
                        pixels[i].setZ((float) image.data.getElem(k + 2) / 255.0f);
                        pixels[i].setW((float) image.data.getElem(k + 3) / 255.0f);

                        k += 4;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                        pixels[i].setX((float) image.data.getElem(k) / 255.0f);
                        pixels[i].setY((float) image.data.getElem(k + 1) / 255.0f);
                        pixels[i].setZ((float) image.data.getElem(k + 2) / 255.0f);
                        pixels[i].setW(1.0f);

                        k += 3;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32:{
                        pixels[i].setX((float) image.data.getElem(k));
                        pixels[i].setY(0.0f);
                        pixels[i].setZ(0.0f);
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                        pixels[i].setX((float) image.data.getElem(k));
                        pixels[i].setY((float) image.data.getElem(k + 1));
                        pixels[i].setZ((float) image.data.getElem(k + 2));
                        pixels[i].setW(1.0f);

                        k += 3;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                        pixels[i].setX((float) image.data.getElem(i));
                        pixels[i].setY((float) image.data.getElem(k + 1));
                        pixels[i].setZ((float) image.data.getElem(k + 2));
                        pixels[i].setW((float) image.data.getElem(k + 3));

                        k += 4;
                    }
                    default:
                        break;
                }
            }
        }

        return pixels;
    }

    // Get image alpha border rectangle
    // NOTE: Threshold is defined as a percentatge: 0.0f -> 1.0f
    Rectangle GetImageAlphaBorder(Image image, float threshold){
        Rectangle crop = new Rectangle();

        Color[] pixels = LoadImageColors(image);

        if (pixels != null){
            int xMin = 65536;   // Define a big enough number
            int xMax = 0;
            int yMin = 65536;
            int yMax = 0;

            for (int y = 0; y < image.height; y++){
                for (int x = 0; x < image.width; x++){
                    if (pixels[y * image.width + x].a > (threshold * 255.0f)){
                        if (x < xMin) xMin = x;
                        if (x > xMax) xMax = x;
                        if (y < yMin) yMin = y;
                        if (y > yMax) yMax = y;
                    }
                }
            }

            // Check for empty blank image
            if ((xMin != 65536) && (xMax != 65536)){
                crop = new Rectangle((float) xMin, (float) yMin, (float) ((xMax + 1) - xMin), (float) ((yMax + 1) - yMin));
            }

            UnloadImageColors(pixels);
        }

        return crop;
    }

    //IMAGE DRAWING FUNCTIONS

    void ImageClearBackground(Image dst, Color color){
        for (int i = 0; i < dst.width * dst.height; ++i){
            ImageDrawPixel(dst, i % dst.width, i / dst.width, color);
        }
    }

    void ImageDrawPixel(Image dst, int x, int y, Color color){
        // Security check to avoid program crash
        if ((dst.data == null) || (x < 0) || (x >= dst.getWidth()) || (y < 0) || (y >= dst.getHeight())){
            return;
        }

        switch (dst.getFormat()){
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);
                short gray = (short) ((coln.getX() * 0.299f + coln.getY() * 0.587f + coln.getZ() * 0.114f) * 255.0f);

                dst.data.setElem(y * dst.getWidth() + x, gray);

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);
                short gray = (short) ((coln.getX() * 0.299f + coln.getY() * 0.587f + coln.getZ() * 0.114f) * 255.0f);

                dst.data.setElem((y * dst.getWidth() + x) * 2, gray);
                dst.data.setElem(y * dst.getWidth() + x * 2 + 1, color.getA());

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                // NOTE: Calculate R5G6B5 equivalent color
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);

                int r = (Math.round(coln.getX() * 31.0f));
                int g = (Math.round(coln.getY() * 63.0f));
                int b = (Math.round(coln.getZ() * 31.0f));

                dst.data.setElem(y * dst.getWidth() + x, (r << 11 | g << 5 | b));

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);

                short r = (short) (Math.round(coln.getX() * 31.0f));
                short g = (short) (Math.round(coln.getY() * 31.0f));
                short b = (short) (Math.round(coln.getZ() * 31.0f));
                short a = (short) ((coln.getX() > (float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f) ? 1 : 0);

                dst.data.setElem(y * dst.getWidth() + x, (r << 11 | g << 6 | b << 1 | a));

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);

                short r = (short) (Math.round(coln.getX() * 15.0f));
                short g = (short) (Math.round(coln.getY() * 15.0f));
                short b = (short) (Math.round(coln.getZ() * 15.0f));
                short a = (short) (Math.round(coln.getW() * 15.0f));

                dst.data.setElem(y * dst.getWidth() + x, (r << 12 | g << 8 | b << 4 | a));

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                dst.data.setElem((y * dst.getWidth() + x) * 3, color.getR());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 1, color.getG());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 2, color.getB());

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                dst.data.setElem((y * dst.getWidth() + x) * 4, color.getR());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 1, color.getG());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 2, color.getB());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 3, color.getA());

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R32:{
                // NOTE: Calculate grayscale equivalent color (normalized to 32bit)
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);

                dst.data.setElem(y * dst.width + x,
                        (int) (coln.getX() * 0.299f + coln.getY() * 0.587f + coln.getZ() * 0.114f));

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                // NOTE: Calculate R32G32B32 equivalent color (normalized to 32bit)
                Vector3 coln = new Vector3((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f);

                dst.data.setElem((y * dst.getWidth() + x) * 3, (int) coln.getX());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 1, (int) coln.getY());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 2, (int) coln.getZ());
            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                // NOTE: Calculate R32G32B32A32 equivalent color (normalized to 32bit)
                Vector4 coln = new Vector4((float) color.getR() / 255.0f, (float) color.getG() / 255.0f,
                        (float) color.getB() / 255.0f, (float) color.getA() / 255.0f);

                dst.data.setElem((y * dst.getWidth() + x) * 4, (int) coln.getX());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 1, (int) coln.getY());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 2, (int) coln.getZ());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 3, (int) coln.getW());

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
        if ((dst.data == null) || (dst.width == 0) || (dst.height == 0)){
            return;
        }

        int sy = (int) rec.y;
        int ey = sy + (int) rec.height;

        int sx = (int) rec.x;
        int ex = sx + (int) rec.width;

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
        if (dst.format >= COMPRESSED_DXT1_RGB){
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
            if ((tint.getA() == 255) && ((srcPtr.getFormat() == UNCOMPRESSED_GRAYSCALE) ||
                    (srcPtr.getFormat() == UNCOMPRESSED_R8G8B8) || (srcPtr.getFormat() == UNCOMPRESSED_R5G6B5))){
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

    public static Texture2D LoadTexture(String fileName){
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
        //TODO: When loading img format is not set.
        texture.format = image.format;

        return texture;
    }

    // Load cubemap from image, multiple image cubemap layouts supported
    /* TODO : MISSING METHODS
    TextureCubemap LoadTextureCubemap(Image image, int layoutType)
    {
        TextureCubemap cubemap = new TextureCubemap();

        if (layoutType == CUBEMAP_AUTO_DETECT.getLayoutInt())      // Try to automatically guess layout type
        {
            // Check image width/height to determine the type of cubemap provided
            if (image.width > image.height)
            {
                if ((image.width/6) == image.height) { layoutType = CUBEMAP_LINE_HORIZONTAL.getLayoutInt(); cubemap.width = image.width/6; }
                else if ((image.width/4) == (image.height/3)) { layoutType = CUBEMAP_CROSS_FOUR_BY_THREE.getLayoutInt(); cubemap.width = image.width/4; }
                else if (image.width >= (int)((float)image.height*1.85f)) { layoutType = CUBEMAP_PANORAMA.getLayoutInt(); cubemap.width = image.width/4; }
            }
            else if (image.height > image.width)
            {
                if ((image.height/6) == image.width) { layoutType = CUBEMAP_LINE_VERTICAL.getLayoutInt(); cubemap.width = image.height/6; }
                else if ((image.width/3) == (image.height/4)) { layoutType = CUBEMAP_CROSS_THREE_BY_FOUR.getLayoutInt(); cubemap.width = image.width/3; }
            }

            cubemap.height = cubemap.width;
        }

        if (layoutType != CUBEMAP_AUTO_DETECT.getLayoutInt())
        {
            int size = cubemap.width;

            Image faces = new Image();                // Vertical column image
            Rectangle[] faceRecs = new Rectangle[6];      // Face source rectangles
            for (int i = 0; i < 6; i++) faceRecs[i] = new Rectangle(0, 0, (float)size, (float)size);

            if (layoutType == CUBEMAP_LINE_VERTICAL.getLayoutInt())
            {
                faces = image;
                for (int i = 0; i < 6; i++) faceRecs[i].y = (float)size*i;
            }
            else if (layoutType == CUBEMAP_PANORAMA.getLayoutInt())
            {
                // TODO: Convert panorama image to square faces...
                // Ref: https://github.com/denivip/panorama/blob/master/panorama.cpp
            }
            else
            {
                if (layoutType == CUBEMAP_LINE_HORIZONTAL.getLayoutInt()){
                    for (int i = 0; i < 6; i++) faceRecs[i].x = (float)size*i;
                }
                else if (layoutType == CUBEMAP_CROSS_THREE_BY_FOUR.getLayoutInt())
                {
                    faceRecs[0].x = (float)size; faceRecs[0].y = (float)size;
                    faceRecs[1].x = (float)size; faceRecs[1].y = (float)size*3;
                    faceRecs[2].x = (float)size; faceRecs[2].y = 0;
                    faceRecs[3].x = (float)size; faceRecs[3].y = (float)size*2;
                    faceRecs[4].x = 0; faceRecs[4].y = (float)size;
                    faceRecs[5].x = (float)size*2; faceRecs[5].y = (float)size;
                }
                else if (layoutType == CubemapLayoutType.CUBEMAP_CROSS_FOUR_BY_THREE.getLayoutInt())
                {
                    faceRecs[0].x = (float)size*2; faceRecs[0].y = (float)size;
                    faceRecs[1].x = 0; faceRecs[1].y = (float)size;
                    faceRecs[2].x = (float)size; faceRecs[2].y = 0;
                    faceRecs[3].x = (float)size; faceRecs[3].y = (float)size*2;
                    faceRecs[4].x = (float)size; faceRecs[4].y = (float)size;
                    faceRecs[5].x = (float)size*3; faceRecs[5].y = (float)size;
                }

                // Convert image data to 6 faces in a vertical column, that's the optimum layout for loading
                faces = GenImageColor(size, size*6, Color.MAGENTA);
                ImageFormat(faces, image.format);

                // TODO: Image formating does not work with compressed textures!
            }

            for (int i = 0; i < 6; i++){
                ImageDraw(faces, image, faceRecs[i], new Rectangle(0, (float) size * i, (float) size, (float) size),
                        Color.WHITE);
            }

            cubemap.id = rlLoadTextureCubemap(faces.data, size, faces.format);
            if (cubemap.id == 0) {
                Tracelog(LOG_WARNING, "IMAGE: Failed to load cubemap image");
            }

            UnloadImage(faces);
        }
        else {
            Tracelog(LOG_WARNING, "IMAGE: Failed to detect cubemap image layout");
        }

        return cubemap;
    }
    // Load texture for rendering (framebuffer)
    // NOTE: Render texture is loaded by default with RGBA color attachment and depth RenderBuffer
    RenderTexture LoadRenderTexture(int width, int height)
    {
        RenderTexture target = new RenderTexture();

        target.id = rlLoadFramebuffer(width, height);   // Load an empty framebuffer

        if (target.id > 0)
        {
            rlEnableFramebuffer(target.id);

            // Create color texture (default to RGBA)
            target.texture.id = rlLoadTexture(null, width, height, UNCOMPRESSED_R8G8B8A8, 1);
            target.texture.width = width;
            target.texture.height = height;
            target.texture.format = UNCOMPRESSED_R8G8B8A8;
            target.texture.mipmaps = 1;

            // Create depth renderbuffer/texture
            target.depth.id = rlLoadTextureDepth(width, height, true);
            target.depth.width = width;
            target.depth.height = height;
            target.depth.format = 19;       //DEPTH_COMPONENT_24BIT?
            target.depth.mipmaps = 1;

            // Attach color texture and depth renderbuffer/texture to FBO
            rlFramebufferAttach(target.id, target.texture.id, RL_ATTACHMENT_COLOR_CHANNEL0, RL_ATTACHMENT_TEXTURE2D);
            rlFramebufferAttach(target.id, target.depth.id, RL_ATTACHMENT_DEPTH, RL_ATTACHMENT_RENDERBUFFER);

            // Check if fbo is complete with attachments (valid)
            if(rlFramebufferComplete(target.id)){
                Tracelog(LOG_INFO, "FBO: [ID " + target.id + "] Framebuffer object created successfully");
            }

            rlDisableFramebuffer();
        }
        else {
            Tracelog(LOG_WARNING, "FBO: Framebuffer object can not be created");
        }

        return target;
    }*/
    public void UnloadTexture(Texture2D texture){
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

        if (texture.format < PIXELFORMAT_COMPRESSED_DXT1_RGB){
            image.setData(rlReadTexturePixels(texture));

            if (image.data != null){
                image.width = texture.width;
                image.height = texture.height;
                image.format = texture.format;
                image.mipmaps = 1;

                if (RLGL.rlGetVersion() == GlVersion.OPENGL_ES_20){
                    // NOTE: Data retrieved on OpenGL ES 2.0 should be RGBA,
                    // coming from FBO color buffer attachment, but it seems
                    // original texture format is retrieved on RPI...
                    image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
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
        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
        image.setData(rlReadScreenPixels(image.width, image.height));

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
                rlTextureParameters(texture.id, RL_TEXTURE_FILTER_ANISOTROPIC, 4);
                break;
            case 4:
                rlTextureParameters(texture.id, RL_TEXTURE_FILTER_ANISOTROPIC, 8);
                break;
            case 5:
                rlTextureParameters(texture.id, RL_TEXTURE_FILTER_ANISOTROPIC, 16);
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
    public void DrawTexture(Texture2D texture, int posX, int posY, Color tint){
        DrawTextureEx(texture, new Vector2((float) posX, (float) posY), 0.0f, 1.0f, tint);
    }

    // Draw a Texture2D with position defined as Vector2
    public void DrawTextureV(Texture2D texture, Vector2 position, Color tint){
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
    public void DrawTextureRec(Texture2D texture, Rectangle source, Vector2 position, Color tint){
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

            if (source.width < 0){
                flipX = true;
                source.width *= -1;
            }
            if (source.height < 0) source.y -= source.height;

            Vector2 topLeft = new Vector2();
            Vector2 topRight = new Vector2();
            Vector2 bottomLeft = new Vector2();
            Vector2 bottomRight = new Vector2();

            // Only calculate rotation if needed
            if (rotation == 0.0f){
                float x = dest.x - origin.x;
                float y = dest.y - origin.y;
                topLeft = new Vector2(x, y);
                topRight = new Vector2(x + dest.width, y);
                bottomLeft = new Vector2(x, y + dest.height);
                bottomRight = new Vector2(x + dest.width, y + dest.height);
            }
            else{
                float sinRotation = (float) Math.sin(rotation * DEG2RAD);
                float cosRotation = (float) Math.cos(rotation * DEG2RAD);
                float x = dest.x;
                float y = dest.y;
                float dx = -origin.x;
                float dy = -origin.y;

                topLeft.x = x + dx * cosRotation - dy * sinRotation;
                topLeft.y = y + dx * sinRotation + dy * cosRotation;

                topRight.x = x + (dx + dest.width) * cosRotation - dy * sinRotation;
                topRight.y = y + (dx + dest.width) * sinRotation + dy * cosRotation;

                bottomLeft.x = x + dx * cosRotation - (dy + dest.height) * sinRotation;
                bottomLeft.y = y + dx * sinRotation + (dy + dest.height) * cosRotation;

                bottomRight.x = x + (dx + dest.width) * cosRotation - (dy + dest.height) * sinRotation;
                bottomRight.y = y + (dx + dest.width) * sinRotation + (dy + dest.height) * cosRotation;
            }

            rlCheckRenderBatchLimit(4);     // Make sure there is enough free space on the batch buffer

            rlSetTexture(texture.id);
            rlBegin(RL_QUADS);

            rlColor4ub(tint.r, tint.g, tint.b, tint.a);
            rlNormal3f(0.0f, 0.0f, 1.0f);                          // Normal vector pointing towards viewer

            // Top-left corner for texture and quad
            if (flipX){
                rlTexCoord2f((source.x + source.width) / width, source.y / height);
            }
            else{
                rlTexCoord2f(source.x / width, source.y / height);
            }
            rlVertex2f(topLeft.x, topLeft.y);

            // Bottom-left corner for texture and quad
            if (flipX){
                rlTexCoord2f((source.x + source.width) / width, (source.y + source.height) / height);
            }
            else{
                rlTexCoord2f(source.x / width, (source.y + source.height) / height);
            }
            rlVertex2f(bottomLeft.x, bottomLeft.y);

            // Bottom-right corner for texture and quad
            if (flipX){
                rlTexCoord2f(source.x / width, (source.y + source.height) / height);
            }
            else{
                rlTexCoord2f((source.x + source.width) / width, (source.y + source.height) / height);
            }
            rlVertex2f(bottomRight.x, bottomRight.y);

            // Top-right corner for texture and quad
            if (flipX){
                rlTexCoord2f(source.x / width, source.y / height);
            }
            else{
                rlTexCoord2f((source.x + source.width) / width, source.y / height);
            }
            rlVertex2f(topRight.x, topRight.y);

            rlEnd();
            rlSetTexture(0);
        }
    }

    // Draws a texture (or part of it) that stretches or shrinks nicely using n-patch info
    void DrawTextureNPatch(Texture2D texture, NPatchInfo nPatchInfo, Rectangle dest, Vector2 origin, float rotation, Color tint){
        if (texture.id > 0){
            float width = (float) texture.width;
            float height = (float) texture.height;

            float patchWidth = Math.max(dest.getWidth(), 0.0f);
            float patchHeight = Math.max(dest.getHeight(), 0.0f);

            if (nPatchInfo.source.getWidth() < 0){
                nPatchInfo.source.x -= nPatchInfo.source.getWidth();
            }
            if (nPatchInfo.source.getHeight() < 0){
                nPatchInfo.source.y -= nPatchInfo.source.getHeight();
            }
            if (nPatchInfo.getType() == NPT_3PATCH_HORIZONTAL.getPatchType()){
                patchHeight = nPatchInfo.source.getHeight();
            }
            if (nPatchInfo.getType() == NPT_3PATCH_VERTICAL.getPatchType()){
                patchWidth = nPatchInfo.source.getWidth();
            }

            boolean drawCenter = true;
            boolean drawMiddle = true;
            float leftBorder = (float) nPatchInfo.left;
            float topBorder = (float) nPatchInfo.top;
            float rightBorder = (float) nPatchInfo.right;
            float bottomBorder = (float) nPatchInfo.bottom;

            // adjust the lateral (left and right) border widths in case patchWidth < texture.width
            if (patchWidth <= (leftBorder + rightBorder) && nPatchInfo.type != NPT_3PATCH_VERTICAL.getPatchType()){
                drawCenter = false;
                leftBorder = (leftBorder / (leftBorder + rightBorder)) * patchWidth;
                rightBorder = patchWidth - leftBorder;
            }
            // adjust the lateral (top and bottom) border heights in case patchHeight < texture.height
            if (patchHeight <= (topBorder + bottomBorder) && nPatchInfo.type != NPT_3PATCH_HORIZONTAL.getPatchType()){
                drawMiddle = false;
                topBorder = (topBorder / (topBorder + bottomBorder)) * patchHeight;
                bottomBorder = patchHeight - topBorder;
            }

            Vector2 vertA, vertB, vertC, vertD;
            vertA = new Vector2(0.0f, 0.0f); //outer left, outer top
            vertB = new Vector2(leftBorder, topBorder); //inner loft, inner top
            vertC = new Vector2(patchWidth - rightBorder, patchHeight - bottomBorder); //inner right, inner bottom
            vertD = new Vector2(patchWidth, patchHeight); // outer right, outer bottom


            Vector2 coordA, coordB, coordC, coordD;
            coordA = new Vector2(nPatchInfo.source.x / width, nPatchInfo.source.y / height);
            coordB = new Vector2((nPatchInfo.source.x + leftBorder) / width, (nPatchInfo.source.y + topBorder) / height);
            coordC = new Vector2((nPatchInfo.source.x + nPatchInfo.source.width - rightBorder) / width, (nPatchInfo.source.y + nPatchInfo.source.height - bottomBorder) / height);
            coordD = new Vector2((nPatchInfo.source.x + nPatchInfo.source.width) / width, (nPatchInfo.source.y + nPatchInfo.source.height) / height);

            RLGL.rlSetTexture(texture.id);

            rlPushMatrix();
            rlTranslatef(dest.x, dest.y, 0.0f);
            rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
            rlTranslatef(-origin.x, -origin.y, 0.0f);

            rlBegin(RL_QUADS);
            rlColor4ub(tint.r, tint.g, tint.b, tint.a);
            rlNormal3f(0.0f, 0.0f, 1.0f);               // Normal vector pointing towards viewer

            if (nPatchInfo.type == NPT_9PATCH.getPatchType()){
                // ------------------------------------------------------------
                // TOP-LEFT QUAD
                rlTexCoord2f(coordA.x, coordB.y);
                rlVertex2f(vertA.x, vertB.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordB.x, coordB.y);
                rlVertex2f(vertB.x, vertB.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordB.x, coordA.y);
                rlVertex2f(vertB.x, vertA.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordA.x, coordA.y);
                rlVertex2f(vertA.x, vertA.y);  // Top-left corner for texture and quad
                if (drawCenter){
                    // TOP-CENTER QUAD
                    rlTexCoord2f(coordB.x, coordB.y);
                    rlVertex2f(vertB.x, vertB.y);  // Bottom-left corner for texture and quad
                    rlTexCoord2f(coordC.x, coordB.y);
                    rlVertex2f(vertC.x, vertB.y);  // Bottom-right corner for texture and quad
                    rlTexCoord2f(coordC.x, coordA.y);
                    rlVertex2f(vertC.x, vertA.y);  // Top-right corner for texture and quad
                    rlTexCoord2f(coordB.x, coordA.y);
                    rlVertex2f(vertB.x, vertA.y);  // Top-left corner for texture and quad
                }
                // TOP-RIGHT QUAD
                rlTexCoord2f(coordC.x, coordB.y);
                rlVertex2f(vertC.x, vertB.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordD.x, coordB.y);
                rlVertex2f(vertD.x, vertB.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordD.x, coordA.y);
                rlVertex2f(vertD.x, vertA.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordC.x, coordA.y);
                rlVertex2f(vertC.x, vertA.y);  // Top-left corner for texture and quad
                if (drawMiddle){
                    // ------------------------------------------------------------
                    // MIDDLE-LEFT QUAD
                    rlTexCoord2f(coordA.x, coordC.y);
                    rlVertex2f(vertA.x, vertC.y);  // Bottom-left corner for texture and quad
                    rlTexCoord2f(coordB.x, coordC.y);
                    rlVertex2f(vertB.x, vertC.y);  // Bottom-right corner for texture and quad
                    rlTexCoord2f(coordB.x, coordB.y);
                    rlVertex2f(vertB.x, vertB.y);  // Top-right corner for texture and quad
                    rlTexCoord2f(coordA.x, coordB.y);
                    rlVertex2f(vertA.x, vertB.y);  // Top-left corner for texture and quad
                    if (drawCenter){
                        // MIDDLE-CENTER QUAD
                        rlTexCoord2f(coordB.x, coordC.y);
                        rlVertex2f(vertB.x, vertC.y);  // Bottom-left corner for texture and quad
                        rlTexCoord2f(coordC.x, coordC.y);
                        rlVertex2f(vertC.x, vertC.y);  // Bottom-right corner for texture and quad
                        rlTexCoord2f(coordC.x, coordB.y);
                        rlVertex2f(vertC.x, vertB.y);  // Top-right corner for texture and quad
                        rlTexCoord2f(coordB.x, coordB.y);
                        rlVertex2f(vertB.x, vertB.y);  // Top-left corner for texture and quad
                    }

                    // MIDDLE-RIGHT QUAD
                    rlTexCoord2f(coordC.x, coordC.y);
                    rlVertex2f(vertC.x, vertC.y);  // Bottom-left corner for texture and quad
                    rlTexCoord2f(coordD.x, coordC.y);
                    rlVertex2f(vertD.x, vertC.y);  // Bottom-right corner for texture and quad
                    rlTexCoord2f(coordD.x, coordB.y);
                    rlVertex2f(vertD.x, vertB.y);  // Top-right corner for texture and quad
                    rlTexCoord2f(coordC.x, coordB.y);
                    rlVertex2f(vertC.x, vertB.y);  // Top-left corner for texture and quad
                }

                // ------------------------------------------------------------
                // BOTTOM-LEFT QUAD
                rlTexCoord2f(coordA.x, coordD.y);
                rlVertex2f(vertA.x, vertD.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordB.x, coordD.y);
                rlVertex2f(vertB.x, vertD.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordB.x, coordC.y);
                rlVertex2f(vertB.x, vertC.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordA.x, coordC.y);
                rlVertex2f(vertA.x, vertC.y);  // Top-left corner for texture and quad
                if (drawCenter){
                    // BOTTOM-CENTER QUAD
                    rlTexCoord2f(coordB.x, coordD.y);
                    rlVertex2f(vertB.x, vertD.y);  // Bottom-left corner for texture and quad
                    rlTexCoord2f(coordC.x, coordD.y);
                    rlVertex2f(vertC.x, vertD.y);  // Bottom-right corner for texture and quad
                    rlTexCoord2f(coordC.x, coordC.y);
                    rlVertex2f(vertC.x, vertC.y);  // Top-right corner for texture and quad
                    rlTexCoord2f(coordB.x, coordC.y);
                    rlVertex2f(vertB.x, vertC.y);  // Top-left corner for texture and quad
                }

                // BOTTOM-RIGHT QUAD
                rlTexCoord2f(coordC.x, coordD.y);
                rlVertex2f(vertC.x, vertD.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordD.x, coordD.y);
                rlVertex2f(vertD.x, vertD.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordD.x, coordC.y);
                rlVertex2f(vertD.x, vertC.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordC.x, coordC.y);
                rlVertex2f(vertC.x, vertC.y);  // Top-left corner for texture and quad
            }
            else if (nPatchInfo.type == NPT_3PATCH_VERTICAL.getPatchType()){
                // TOP QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                rlTexCoord2f(coordA.x, coordB.y);
                rlVertex2f(vertA.x, vertB.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordD.x, coordB.y);
                rlVertex2f(vertD.x, vertB.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordD.x, coordA.y);
                rlVertex2f(vertD.x, vertA.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordA.x, coordA.y);
                rlVertex2f(vertA.x, vertA.y);  // Top-left corner for texture and quad
                if (drawCenter){
                    // MIDDLE QUAD
                    // -----------------------------------------------------------
                    // Texture coords                 Vertices
                    rlTexCoord2f(coordA.x, coordC.y);
                    rlVertex2f(vertA.x, vertC.y);  // Bottom-left corner for texture and quad
                    rlTexCoord2f(coordD.x, coordC.y);
                    rlVertex2f(vertD.x, vertC.y);  // Bottom-right corner for texture and quad
                    rlTexCoord2f(coordD.x, coordB.y);
                    rlVertex2f(vertD.x, vertB.y);  // Top-right corner for texture and quad
                    rlTexCoord2f(coordA.x, coordB.y);
                    rlVertex2f(vertA.x, vertB.y);  // Top-left corner for texture and quad
                }
                // BOTTOM QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                rlTexCoord2f(coordA.x, coordD.y);
                rlVertex2f(vertA.x, vertD.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordD.x, coordD.y);
                rlVertex2f(vertD.x, vertD.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordD.x, coordC.y);
                rlVertex2f(vertD.x, vertC.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordA.x, coordC.y);
                rlVertex2f(vertA.x, vertC.y);  // Top-left corner for texture and quad
            }
            else if (nPatchInfo.type == NPT_3PATCH_HORIZONTAL.getPatchType()){
                // LEFT QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                rlTexCoord2f(coordA.x, coordD.y);
                rlVertex2f(vertA.x, vertD.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordB.x, coordD.y);
                rlVertex2f(vertB.x, vertD.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordB.x, coordA.y);
                rlVertex2f(vertB.x, vertA.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordA.x, coordA.y);
                rlVertex2f(vertA.x, vertA.y);  // Top-left corner for texture and quad
                if (drawCenter){
                    // CENTER QUAD
                    // -----------------------------------------------------------
                    // Texture coords                 Vertices
                    rlTexCoord2f(coordB.x, coordD.y);
                    rlVertex2f(vertB.x, vertD.y);  // Bottom-left corner for texture and quad
                    rlTexCoord2f(coordC.x, coordD.y);
                    rlVertex2f(vertC.x, vertD.y);  // Bottom-right corner for texture and quad
                    rlTexCoord2f(coordC.x, coordA.y);
                    rlVertex2f(vertC.x, vertA.y);  // Top-right corner for texture and quad
                    rlTexCoord2f(coordB.x, coordA.y);
                    rlVertex2f(vertB.x, vertA.y);  // Top-left corner for texture and quad
                }
                // RIGHT QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                rlTexCoord2f(coordC.x, coordD.y);
                rlVertex2f(vertC.x, vertD.y);  // Bottom-left corner for texture and quad
                rlTexCoord2f(coordD.x, coordD.y);
                rlVertex2f(vertD.x, vertD.y);  // Bottom-right corner for texture and quad
                rlTexCoord2f(coordD.x, coordA.y);
                rlVertex2f(vertD.x, vertA.y);  // Top-right corner for texture and quad
                rlTexCoord2f(coordC.x, coordA.y);
                rlVertex2f(vertC.x, vertA.y);  // Top-left corner for texture and quad
            }
            rlEnd();
            rlPopMatrix();

            rlDisableTexture();
        }
    }

    // Draw textured polygon, defined by vertex and texturecoordinates
    // NOTE: Polygon center must have straight line path to all points
    // without crossing perimeter, points must be in anticlockwise order
    void DrawTexturePoly(Texture2D texture, Vector2 center, Vector2[] points, Vector2[] texcoords, int pointsCount,
                         Color tint){
        RLGL.rlCheckRenderBatchLimit((pointsCount - 1) * 4);

        RLGL.rlSetTexture(texture.id);

        // Texturing is only supported on QUADs
        rlBegin(RL_QUADS);

        rlColor4ub(tint.r, tint.g, tint.b, tint.a);

        for (int i = 0; i < pointsCount - 1; i++){
            rlTexCoord2f(0.5f, 0.5f);
            rlVertex2f(center.x, center.y);

            rlTexCoord2f(texcoords[i].x, texcoords[i].y);
            rlVertex2f(points[i].x + center.x, points[i].y + center.y);

            rlTexCoord2f(texcoords[i + 1].x, texcoords[i + 1].y);
            rlVertex2f(points[i + 1].x + center.x, points[i + 1].y + center.y);

            rlTexCoord2f(texcoords[i + 1].x, texcoords[i + 1].y);
            rlVertex2f(points[i + 1].x + center.x, points[i + 1].y + center.y);
        }
        rlEnd();

        RLGL.rlSetTexture(0);
    }


    // Returns color with alpha applied, alpha goes from 0.0f to 1.0f
    public static Color Fade(Color color, float alpha){
        if (alpha < 0.0f){
            alpha = 0.0f;
        }
        else if (alpha > 1.0f){
            alpha = 1.0f;
        }

        return new Color(color.getR(), color.getG(), color.getB(), (int) (255.0f * alpha));
    }

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

    // Returns HSV values for a Color
    // NOTE: Hue is returned as degrees [0..360]
    Vector3 ColorToHSV(Color color){
        Vector3 hsv = new Vector3();
        Vector3 rgb = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);
        float min, max, delta;

        min = Math.min(rgb.x, rgb.y);
        min = Math.min(min, rgb.z);

        max = Math.max(rgb.x, rgb.y);
        max = Math.max(max, rgb.z);

        hsv.z = max;            // Value
        delta = max - min;

        if (delta < 0.00001f){
            hsv.y = 0.0f;
            hsv.x = 0.0f;       // Undefined, maybe NAN?
            return hsv;
        }

        if (max > 0.0f){
            // NOTE: If max is 0, this divide would cause a crash
            hsv.y = (delta / max);    // Saturation
        }
        else{
            // NOTE: If max is 0, then r = g = b = 0, s = 0, h is undefined
            hsv.y = 0.0f;
            hsv.x = 0;        // Undefined
            return hsv;
        }

        // NOTE: Comparing float values could not work properly
        if (rgb.x >= max){
            hsv.x = (rgb.y - rgb.z) / delta;    // Between yellow & magenta
        }
        else{
            if (rgb.y >= max){
                hsv.x = 2.0f + (rgb.z - rgb.x) / delta;  // Between cyan & yellow
            }
            else{
                hsv.x = 4.0f + (rgb.x - rgb.y) / delta;      // Between magenta & cyan
            }
        }

        hsv.x *= 60.0f;     // Convert to degrees

        if (hsv.x < 0.0f) hsv.x += 360.0f;

        return hsv;
    }

    // Returns a Color from HSV values
    // Implementation reference: https://en.wikipedia.org/wiki/HSL_and_HSV#Alternative_HSV_conversion
    // NOTE: Color->HSV->Color conversion will not yield exactly the same color due to rounding errors
    // Hue is provided in degrees: [0..360]
    // Saturation/Value are provided normalized: [0.0f..1.0f]
    Color ColorFromHSV(float hue, float saturation, float value){
        Color color = new Color(0, 0, 0, 255);

        // Red channel
        float k = (5.0f + hue / 60.0f) % 6;
        float t = 4.0f - k;
        k = Math.min(t, k);
        k = (k < 1) ? k : 1;
        k = (k > 0) ? k : 0;
        color.r = (int) ((value - value * saturation * k) * 255.0f);

        // Green channel
        k = (3.0f + hue / 60.0f) % 6;
        t = 4.0f - k;
        k = Math.min(t, k);
        k = (k < 1) ? k : 1;
        k = (k > 0) ? k : 0;
        color.g = (int) ((value - value * saturation * k) * 255.0f);

        // Blue channel
        k = (1.0f + hue / 60.0f) % 6;
        t = 4.0f - k;
        k = Math.min(t, k);
        k = (k < 1) ? k : 1;
        k = (k > 0) ? k : 0;
        color.b = (int) ((value - value * saturation * k) * 255.0f);

        return color;
    }

    // Returns color with alpha applied, alpha goes from 0.0f to 1.0f
    Color ColorAlpha(Color color, float alpha){
        if (alpha < 0.0f){
            alpha = 0.0f;
        }
        else if (alpha > 1.0f){
            alpha = 1.0f;
        }

        return new Color(color.getR(), color.getG(), color.getB(), (int) (255.0f * alpha));
    }

    // Returns src alpha-blended into dst color with tint
    Color ColorAlphaBlend(Color dst, Color src, Color tint){
        Color out = Color.WHITE;

        // Apply color tint to source color
        src.setR((src.getR() * tint.getR()) >> 8);
        src.setG((src.getG() * tint.getG()) >> 8);
        src.setB((src.getB() * tint.getB()) >> 8);
        src.setA((src.getA() * tint.getA()) >> 8);

        boolean COLORALPHABLEND_FLOAT = false;
        boolean COLORALPHABLEND_INTEGERS = true;
        if (COLORALPHABLEND_INTEGERS){
            if (src.getA() == 0){
                out = dst;
            }
            else if (src.getA() == 255){
                out = src;
            }
            else{
                int alpha = src.getA() + 1;
                // We are shifting by 8 (dividing by 256), so we need to take that excess into account

                out.setA((alpha * 256 + dst.getA() * (256 - alpha)) >> 8);

                if (out.getA() > 0){
                    out.setR(((src.getR() * alpha * 256 + dst.getR() * dst.getA() * (256 - alpha)) / out.getA()) >> 8);
                    out.setG(((src.getG() * alpha * 256 + dst.getG() * dst.getA() * (256 - alpha)) / out.getA()) >> 8);
                    out.setB(((src.getB() * alpha * 256 + dst.getB() * dst.getA() * (256 - alpha)) / out.getA()) >> 8);
                }
            }
        }
        if (COLORALPHABLEND_FLOAT){
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

        switch (format){
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                col = new Color(srcPtr[0], srcPtr[0], srcPtr[0], 255);
                break;
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                col = new Color(srcPtr[0], srcPtr[0], srcPtr[0], srcPtr[1]);
                break;
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                col.setR((srcPtr[0] >> 11) * 255 / 31);
                col.setG(((srcPtr[0] >> 5) & 0b0000000000111111) * 255 / 63);
                col.setB((srcPtr[0] & 0b0000000000011111) * 255 / 31);
                col.setA(255);

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                col.setR((srcPtr[0] >> 11) * 255 / 31);
                col.setG(((srcPtr[0] >> 6) & 0b0000000000011111) * 255 / 31);
                col.setB((srcPtr[0] & 0b0000000000011111) * 255 / 31);
                col.setA((srcPtr[0] & 0b0000000000000001) == 1 ? 255 : 0);

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                col.setR((srcPtr[0] >> 11) * 255 / 15);
                col.setG(((srcPtr[0] >> 8) & 0b0000000000001111) * 255 / 15);
                col.setB(((srcPtr[0] >> 4) & 0b0000000000001111) * 255 / 15);
                col.setA((srcPtr[0] & 0b0000000000001111) * 255 / 15);

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                col = new Color(srcPtr[0], srcPtr[1], srcPtr[2], srcPtr[3]);
                break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
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
        int dataSize;       // Size in bytes
        int bpp = 0;            // Bits per pixel

        switch (format){
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                bpp = 8;
                break;
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                bpp = 16;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                bpp = 32;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                bpp = 24;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R32:
                bpp = 32;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                bpp = 32 * 3;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                bpp = 32 * 4;
                break;
            case PIXELFORMAT_COMPRESSED_DXT1_RGB:
            case PIXELFORMAT_COMPRESSED_DXT1_RGBA:
            case PIXELFORMAT_COMPRESSED_ETC1_RGB:
            case PIXELFORMAT_COMPRESSED_ETC2_RGB:
            case PIXELFORMAT_COMPRESSED_PVRT_RGB:
            case PIXELFORMAT_COMPRESSED_PVRT_RGBA:
                bpp = 4;
                break;
            case PIXELFORMAT_COMPRESSED_DXT3_RGBA:
            case PIXELFORMAT_COMPRESSED_DXT5_RGBA:
            case PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA:
            case PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA:
                bpp = 8;
                break;
            case PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA:
                bpp = 2;
                break;
            default:
                break;
        }

        dataSize = width * height * bpp / 8;  // Total data size in bytes

        // Most compressed formats works on 4x4 blocks,
        // if texture is smaller, minimum dataSize is 8 or 16
        if ((width < 4) && (height < 4)){
            if ((format >= PIXELFORMAT_COMPRESSED_DXT1_RGB) && (format < PIXELFORMAT_COMPRESSED_DXT3_RGBA)){
                dataSize = 8;
            }
            else if ((format >= PIXELFORMAT_COMPRESSED_DXT3_RGBA) && (format < PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA)){
                dataSize = 16;
            }
        }

        return dataSize;
    }

    //Load specific file formats

}
