package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;
import com.raylib.java.raymath.Vector4;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.text.Font;
import com.raylib.java.utils.FileIO;
import com.raylib.java.utils.Tracelog;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.raylib.java.Config.*;
import static com.raylib.java.core.Color.BLANK;
import static com.raylib.java.core.Color.WHITE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.RL_ATTACHMENT_RENDERBUFFER;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.RL_ATTACHMENT_TEXTURE2D;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL0;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.RL_ATTACHMENT_DEPTH;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.textures.NPatchInfo.NPatchType.*;
import static com.raylib.java.textures.rTextures.CubemapLayoutType.*;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8;

public class rTextures{

    final int UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD = 50;

    // Cubemap layouts
    public static class CubemapLayoutType{
        public static final int
                CUBEMAP_AUTO_DETECT = 0,            // Automatically detect layout type
                CUBEMAP_LINE_VERTICAL = 1,          // Layout is defined by a vertical line with faces
                CUBEMAP_LINE_HORIZONTAL = 2,        // Layout is defined by an horizontal line with faces
                CUBEMAP_CROSS_THREE_BY_FOUR = 3,    // Layout is defined by a 3x4 cross with cubemap faces
                CUBEMAP_CROSS_FOUR_BY_THREE = 4,    // Layout is defined by a 4x3 cross with cubemap faces
                CUBEMAP_PANORAMA = 5;               // Layout is defined by a panorama image (equirectangular map)
    }

    private final Raylib context;

    public rTextures(Raylib context) {
        this.context = context;
    }

    // Load image to memory
    public Image LoadImage(String fileName) {
        Image image = new Image();
        // Loading file to memory
        int fileSize = 0;
        byte[] fileData = null;

        try{
            fileData = FileIO.LoadFileData(fileName);
        } catch(IOException e) {
            e.printStackTrace();
        }

        if (fileData != null) {
            String fileType = rCore.GetFileExtension(fileName).toLowerCase();
            // Loading image from memory data
            image = LoadImageFromMemory(fileType, fileData, fileSize);

            if (image.data != null) {
                Tracelog(LOG_INFO, "IMAGE: [" + fileName + "] Data loaded successfully (" +
                        image.width + "x" + image.height + ")");
            }
            else{
                Tracelog(LOG_WARNING, "IMAGE: [" + fileName + "] Failed to load data");
            }

        }

        return image;
    }

    // Load an image from RAW file data
    public Image LoadImageRaw(String fileName, int width, int height, int format, int headerSize) {
        Image image = new Image();

        int dataSize = 0;
        byte[] fileData = null;

        try{
            fileData = FileIO.LoadFileData(fileName);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (fileData != null) {
            int size = GetPixelDataSize(width, height, format);

            image.setData(fileData);      // Copy required data to image
            image.width = width;
            image.height = height;
            image.mipmaps = 1;
            image.format = format;

            fileData = null;
        }

        return image;
    }

    // Load animated image data
    //  - Image.data buffer includes all frames: [image#0][image#1][image#2][...]
    //  - Number of frames is returned through 'frames' parameter
    //  - All frames are returned in RGBA format
    //  - Frames delay data is discarded
    public Image LoadImageAnim(String fileName, int frames) {
        Image image = new Image();
        int framesCount = 1;

        if (SUPPORT_FILEFORMAT_GIF) {
            if (context.core.IsFileExtension(fileName, ".gif")) {
                byte[] fileData = null;
                try{
                    BufferedImage tmpImg = ImageIO.read(new File(fileName));
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(tmpImg, context.core.GetFileExtension(fileName).substring(1), os);
                    fileData = os.toByteArray();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                if (fileData != null) {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer widthBuffer = stack.mallocInt(1);
                        IntBuffer heightBuffer = stack.mallocInt(1);
                        IntBuffer compBuffer = stack.mallocInt(1);
                        PointerBuffer delaysBuffer = null;

                        IntBuffer framesBuffer = stack.mallocInt(1);
                        framesBuffer.put(framesCount).flip();

                        ByteBuffer fileDataBuffer = MemoryUtil.memAlloc(fileData.length);
                        fileDataBuffer.put(fileData).flip();

                        ByteBuffer imgBuffer = STBImage.stbi_load_gif_from_memory(fileDataBuffer, delaysBuffer,
                                                                                  widthBuffer, heightBuffer, framesBuffer, compBuffer, 4);

                        image.width = widthBuffer.get();
                        image.height = heightBuffer.get();
                        image.mipmaps = 1;
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

                        if (imgBuffer != null) {
                            byte[] bytes = new byte[imgBuffer.capacity()];
                            for (int i = 0; i < bytes.length; i++) {
                                bytes[i] = imgBuffer.get();
                            }
                            image.setData(bytes);
                        }
                        fileData = null;
                    }
                }
            }
        }
        else{
            image = LoadImage(fileName);
        }

        // TODO: Support APNG animated images?
        frames = framesCount;

        return image;
    }


    // Load image from memory buffer, fileType refers to extension: i.e. ".png"
    // WARNING: File extension must be provided in lower-case
    public Image LoadImageFromMemory(String fileType, byte[] fileData, int dataSize) {
        Image image = new Image();

        if (SUPPORT_FILEFORMAT_PNG || SUPPORT_FILEFORMAT_BMP || SUPPORT_FILEFORMAT_TGA || SUPPORT_FILEFORMAT_JPG ||
                SUPPORT_FILEFORMAT_GIF || SUPPORT_FILEFORMAT_PIC || SUPPORT_FILEFORMAT_PSD) {
            if (fileType.equals(".png") || fileType.equals(".bmp") || fileType.equals(".tga") ||
                    (fileType.equals(".jpeg") || fileType.equals(".jpg")) || fileType.equals(".gif") ||
                    fileType.equals(".pic") || fileType.equals(".psd")) {

                if (fileData != null) {
                    int comp = 0;
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer widthBuffer = stack.mallocInt(1);
                        IntBuffer heightBuffer = stack.mallocInt(1);
                        IntBuffer compBuffer = stack.mallocInt(1);

                        ByteBuffer fileDataBuffer = MemoryUtil.memAlloc(fileData.length);
                        fileDataBuffer.put(fileData).flip();

                        ByteBuffer imgBuffer = STBImage.stbi_load_from_memory(fileDataBuffer, widthBuffer,
                                                                              heightBuffer, compBuffer, 0);
                        if (imgBuffer == null) {
                            Tracelog(LOG_WARNING, "Failed to load image: " + fileType + "\t" + STBImage.stbi_failure_reason());
                        }
                        image.width = widthBuffer.get();
                        image.height = heightBuffer.get();
                        comp = compBuffer.get();
                        if (imgBuffer != null) {
                            byte[] bytes = new byte[imgBuffer.capacity()];
                            for (int i = 0; i < bytes.length; i++) {
                                bytes[i] = imgBuffer.get();
                            }
                            image.setData(bytes);
                            //STBImage.stbi_image_free(imgBuffer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    image.mipmaps = 1;

                    if (comp == 1) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
                    }
                    else if (comp == 2) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
                    }
                    else if (comp == 3) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8;
                    }
                    else if (comp == 4) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
                    }
                }
            }
        }
        else if(SUPPORT_FILEFORMAT_HDR) {
            if (fileType.equals(".hdr")) {
                if (fileData != null) {
                    int comp = 0;
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer widthBuffer = stack.mallocInt(1);
                        IntBuffer heightBuffer = stack.mallocInt(1);
                        IntBuffer compBuffer = stack.mallocInt(1);

                        ByteBuffer fileDataBuffer = MemoryUtil.memAlloc(fileData.length);
                        fileDataBuffer.put(fileData).flip();

                        ByteBuffer imgBuffer = STBImage.stbi_load_from_memory(fileDataBuffer, widthBuffer,
                                                                              heightBuffer, compBuffer, 0);
                        if (imgBuffer == null) {
                            Tracelog(LOG_WARNING, "Failed to load image " + fileType + "\n\t" + STBImage.stbi_failure_reason());
                        }
                        image.width = widthBuffer.get();
                        image.height = heightBuffer.get();
                        comp = compBuffer.get();
                        if (imgBuffer != null) {
                            byte[] bytes = new byte[imgBuffer.capacity()];
                            for (int i = 0; i < bytes.length; i++) {
                                bytes[i] = imgBuffer.get();
                            }
                            image.setData(bytes);
                            //STBImage.stbi_image_free(imgBuffer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    image.mipmaps = 1;

                    if (comp == 1) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R32;
                    }
                    else if (comp == 3) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32;
                    }
                    else if (comp == 4) {
                        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32;
                    }
                    else {
                        Tracelog(LOG_WARNING, "IMAGE: HDR file format not supported");
                        UnloadImage(image);
                    }
                }
            }
        }
        /*
            TODO:
                * QOI
                * DDS
                * PKM
                * KTX
                * PVR
                * ASTC
            */
        else {
            Tracelog(LOG_WARNING, "IMAGE: Data format not supported");
        }

        if (image.data != null) {
            Tracelog(LOG_INFO, "IMAGE: Data loaded successfully (" + image.width + "x" + image.height + " | " +
                    rlGetPixelFormatName(image.format) + " | " + image.mipmaps + " mipmaps)");
        }
        else {
            Tracelog(LOG_WARNING, "IMAGE: Failed to load image data");
        }

        return image;
    }

    public Image UnloadImage(Image image) {
        image.data = null;
        return image;
    }

    // Export image data to file
    // NOTE: File format depends on fileName extension
    public boolean ExportImage(Image image, String fileName) {
        boolean success = false;

        if (SUPPORT_IMAGE_EXPORT) {
            int channels = 4;
            boolean allocatedData = false;
            byte[] imgData = image.getData();

            if (image.format == RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                channels = 1;
            }
            else if (image.format == RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA) {
                channels = 2;
            }
            else if (image.format == RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8) {
                channels = 3;
            }
            else if (image.format == RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
                channels = 4;
            }
            else{
                // NOTE: Getting Color array as RGBA unsigned char values
                Color[] data = LoadImageColors(image);

                byte[] dataB = new byte[data.length * 4];
                int g = 0;
                for (Color datum: data) {
                    dataB[g] = (byte) datum.r;
                    dataB[g + 1] = (byte) datum.g;
                    dataB[g + 2] = (byte) datum.b;
                    dataB[g + 3] = (byte) datum.a;
                    g += 4;
                }
                imgData = dataB;
                allocatedData = true;
            }

            ByteBuffer imgBuffer = ByteBuffer.allocateDirect(imgData.length);
            imgBuffer.put(imgData).flip();

            if (SUPPORT_FILEFORMAT_PNG) {
                if (context.core.IsFileExtension(fileName, ".png")) {
                    success = STBImageWrite.stbi_write_png(fileName, image.width, image.height,
                                                           channels, imgBuffer, image.width*channels);
                }
            }
            if (SUPPORT_FILEFORMAT_BMP) {
                if (context.core.IsFileExtension(fileName, ".bmp")) {
                    success = STBImageWrite.stbi_write_bmp(fileName, image.width, image.height, channels, imgBuffer);
                }
            }
            if (SUPPORT_FILEFORMAT_TGA) {
                if (context.core.IsFileExtension(fileName, ".tga")) {
                    success = STBImageWrite.stbi_write_tga(fileName, image.width, image.height, channels, imgBuffer);
                }
            }
            if (SUPPORT_FILEFORMAT_JPG) {
                if (context.core.IsFileExtension(fileName, ".jpeg")) {
                    success = STBImageWrite.stbi_write_jpg(fileName, image.width, image.height, channels, imgBuffer, 90);  // JPG quality: between 1 and 100
                }
            }
            /*
            TODO:
                * QOI
                * DDS
                * PKM
                * KTX
                * PVR
                * ASTC
            */
            else if (context.core.IsFileExtension(fileName, ".raw")) {
                // Export raw pixel data (without header)
                // NOTE: It's up to the user to track image parameters
                try{
                    success = FileIO.SaveFileData(fileName, image.getData(), GetPixelDataSize(image.width, image.height,
                                                                                              image.format));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            if (allocatedData) {
                imgData = null;
            }
        }    // SUPPORT_IMAGE_EXPORT

        if (success) {
            Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Image exported successfully");
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export image");
        }

        return success;
    }

    // Export image as code file (.h) defining an array of bytes
    public boolean ExportImageAsCode(Image image, String fileName) {
        boolean success = false;

        if(SUPPORT_IMAGE_EXPORT) {

            int TEXT_BYTES_PER_LINE = 20;

            int dataSize = GetPixelDataSize(image.width, image.height, image.format);

            // NOTE: Text data buffer size is estimated considering image data size in bytes
            // and requiring 6 char bytes for every byte: "0x00, "
            String txtData = "";

            int byteCount = 0;
            txtData += "////////////////////////////////////////////////////////////////////////////////////////\n";
            txtData += "//                                                                                    //\n";
            txtData += "// ImageAsCode exporter v1.0 - Image pixel data exported as an array of bytes         //\n";
            txtData += "//                                                                                    //\n";
            txtData += "// more info and bugs-report:  github.com/raysan5/raylib                              //\n";
            txtData += "// feedback and support:       ray[at]raylib.com                                      //\n";
            txtData += "//                                                                                    //\n";
            txtData += "// Copyright (c) 2018-2022 Ramon Santamaria (@raysan5)                                //\n";
            txtData += "//                                                                                    //\n";
            txtData += "////////////////////////////////////////////////////////////////////////////////////////\n\n";
            byteCount = txtData.length();

            // Get file name from path and convert variable name to uppercase
            String varFileName = fileName.substring(0, fileName.lastIndexOf(".")).toUpperCase();


            // Add image information
            txtData += "// Image data information\n";
            txtData += "#define " + varFileName + "_WIDTH    " + image.width + "\n";
            txtData += "#define " + varFileName + "_HEIGHT   " + image.height + "\n";
            txtData += "#define " + varFileName + "_FORMAT   " + image.format + "          // raylib internal pixel format\n\n";

            txtData += "static unsigned char " + varFileName + "_DATA[" + dataSize + "] = { ";
            byte[] imgData = image.getData();
            for (int i = 0; i < dataSize - 1; i++) {
                if (i % TEXT_BYTES_PER_LINE == 0) {
                    txtData += "0x" + String.format("h", imgData[i]) + "\n";
                }
                else {
                    txtData += "0x" + String.format("h", imgData[i]) + ", ";
                }
            }
            txtData += "0x" + String.format("h", imgData[dataSize - 1]) + " };\n";

            // NOTE: Text data size exported is determined by '\0' (NULL) character
            try {
                success = FileIO.SaveFileText(fileName, txtData);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }     // SUPPORT_IMAGE_EXPORT

        if (success) {
            Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Image as code exported successfully");
        }
        else {
            Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export image as code");
        }

        return success;
    }

    //------------------------------------------------------------------------------------
    // Image generation functions
    //------------------------------------------------------------------------------------
    // Generate image: plain color
    public Image GenImageColor(int width, int height, Color color) {
        Color[] pixels = new Color[width * height];

        for (int i = 0; i < width * height; i++) {
            pixels[i] = color;
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    //Support image generation

    // Generate image: vertical gradient
    public Image GenImageGradientV(int width, int height, Color top, Color bottom) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        for (int j = 0; j < height; j++) {
            float factor = (float) j / (float) height;
            for (int i = 0; i < width; i++) {
                pixels[j * width + i].r = (byte) ((float) bottom.r * factor + (float) top.r * (1.f - factor));
                pixels[j * width + i].g = (byte) ((float) bottom.g * factor + (float) top.g * (1.f - factor));
                pixels[j * width + i].b = (byte) ((float) bottom.b * factor + (float) top.b * (1.f - factor));
                pixels[j * width + i].a = (byte) ((float) bottom.a * factor + (float) top.a * (1.f - factor));
            }
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    // Generate image: horizontal gradient
    public Image GenImageGradientH(int width, int height, Color left, Color right) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        for (int i = 0; i < width; i++) {
            float factor = (float) i / (float) width;
            for (int j = 0; j < height; j++) {
                pixels[j * width + i].r = (byte) ((float) right.r * factor + (float) left.r * (1.f - factor));
                pixels[j * width + i].g = (byte) ((float) right.g * factor + (float) left.g * (1.f - factor));
                pixels[j * width + i].b = (byte) ((float) right.b * factor + (float) left.b * (1.f - factor));
                pixels[j * width + i].a = (byte) ((float) right.a * factor + (float) left.a * (1.f - factor));
            }
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    // Generate image: radial gradient
    public Image GenImageGradientRadial(int width, int height, float density, Color inner, Color outer) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        float radius = (width < height) ? (float) width / 2.0f : (float) height / 2.0f;

        float centerX = (float) width / 2.0f;
        float centerY = (float) height / 2.0f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dist = (float) Math.hypot((float) x - centerX, (float) y - centerY);
                float factor = (dist - radius * density) / (radius * (1.0f - density));

                factor = Math.max(factor, 0.0f);
                factor = Math.min(factor, 1.f); // dist can be bigger than radius so we have to check

                pixels[y * width + x].r = (byte) ((float) outer.r * factor + (float) inner.r * (1.0f - factor));
                pixels[y * width + x].g = (byte) ((float) outer.g * factor + (float) inner.g * (1.0f - factor));
                pixels[y * width + x].b = (byte) ((float) outer.b * factor + (float) inner.b * (1.0f - factor));
                pixels[y * width + x].a = (byte) ((float) outer.a * factor + (float) inner.a * (1.0f - factor));
            }
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    // Generate image: checked
    public Image GenImageChecked(int width, int height, int checksX, int checksY, Color col1, Color col2) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((x / checksX + y / checksY) % 2 == 0) {
                    pixels[y * width + x] = col1;
                }
                else{
                    pixels[y * width + x] = col2;
                }
            }
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    // Generate image: white noise
    public Image GenImageWhiteNoise(int width, int height, float factor) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        for (int i = 0; i < width * height; i++) {
            if ((Math.random() * (99 - 0 + 1) + 0)< (int) (factor * 100.0f)) {
                pixels[i] = WHITE;
            }
            else{
                pixels[i] = Color.BLACK;
            }
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    // Generate image: cellular algorithm. Bigger tileSize means bigger cells
    public Image GenImageCellular(int width, int height, int tileSize) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        int seedsPerRow = width / tileSize;
        int seedsPerCol = height / tileSize;
        int seedsCount = seedsPerRow * seedsPerCol;

        Vector2[] seeds = new Vector2[seedsCount];

        for (int i = 0; i < seedsCount; i++) {
            int y = (int) ((i / seedsPerRow) * tileSize + ((Math.random() * (tileSize - 1) - 0 + 1) + 0));
            int x = (int) ((i % seedsPerRow) * tileSize + ((Math.random() * (tileSize - 1) - 0 + 1) + 0));
            seeds[i] = new Vector2((float) x, (float) y);
        }

        for (int y = 0; y < height; y++) {
            int tileY = y / tileSize;

            for (int x = 0; x < width; x++) {
                int tileX = x / tileSize;

                float minDistance = 65536.0f;

                // Check all adjacent tiles
                for (int i = -1; i < 2; i++) {
                    if ((tileX + i < 0) || (tileX + i >= seedsPerRow)) continue;

                    for (int j = -1; j < 2; j++) {
                        if ((tileY + j < 0) || (tileY + j >= seedsPerCol)) continue;

                        Vector2 neighborSeed = seeds[(tileY + j) * seedsPerRow + tileX + i];

                        float dist = (float) Math.hypot(x - (int) neighborSeed.x, y - (int) neighborSeed.y);
                        minDistance = Math.min(minDistance, dist);
                    }
                }

                // I made this up but it seems to give good results at all tile sizes
                int intensity = (int) (minDistance * 256.0f / tileSize);
                if (intensity > 255) intensity = 255;

                pixels[y * width + x] = new Color(intensity, intensity, intensity, 255);
            }
        }

        return new Image(pixels, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    //End Support image generation

    //------------------------------------------------------------------------------------
    // Image manipulation functions
    //------------------------------------------------------------------------------------
    // Copy an image to a new image
    public Image ImageCopy(Image image) {
        Image newImage = new Image();

        int width = image.width;
        int height = image.height;
        int size = 0;

        for (int i = 0; i < image.mipmaps; i++) {
            size += GetPixelDataSize(width, height, image.format);

            width /= 2;
            height /= 2;

            // Security check for NPOT textures
            if (width < 1) {
                width = 1;
            }            if (height < 1) {
                height = 1;
            }
        }

        newImage.setData(new byte[size]);

        if (newImage.data != null) {
            // NOTE: Size must be provided in bytes
            newImage.setData(image.getData());

            newImage.width = image.width;
            newImage.height = image.height;
            newImage.mipmaps = image.mipmaps;
            newImage.format = image.format;
        }

        return newImage;
    }

    public Image ImageFromImage(Image image, Rectangle rectangle) {
        Image result = new Image();
        int bytesPerPixel = GetPixelDataSize(1, 1, image.format);

        // TODO: Check rec is valid?
        result.width = (int) rectangle.getWidth();
        result.height = (int) rectangle.getHeight();
        result.format = image.format;
        result.mipmaps = 1;

        byte[] data = new byte[(int) (rectangle.width*rectangle.height*bytesPerPixel)];
        byte[] srcData = image.getData();

        for(int y = 0; y < rectangle.height; y++) {
            for (int x = 0; x < rectangle.width*bytesPerPixel; x++) {
                data[(int) ((y*rectangle.width*bytesPerPixel)+x)] = srcData[(((y + (int)rectangle.y)*image.width + (int)rectangle.x)*bytesPerPixel)+x];
            }
        }

        result.setData(data);

        return result;
    }

    // Crop an image to area defined by a rectangle
    // NOTE: Security checks are performed in case rectangle goes out of bounds
    public void ImageCrop(Image image, Rectangle crop) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        // Security checks to validate crop rectangle
        if (crop.x < 0) {
            crop.width += crop.x;
            crop.x = 0;
        }
        if (crop.y < 0) {
            crop.height += crop.y;
            crop.y = 0;
        }
        if ((crop.x + crop.width) > image.width) crop.width = image.width - crop.x;
        if ((crop.y + crop.height) > image.height) crop.height = image.height - crop.y;
        if ((crop.x > image.width) || (crop.y > image.height)) {
            Tracelog(LOG_WARNING, "IMAGE: Failed to crop, rectangle out of bounds");
            return;
        }

        if (image.mipmaps > 1) Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else{
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);

            byte[] croppedData = new byte[(int) (crop.width * crop.height) * bytesPerPixel];

            // Move cropped data pixel-by-pixel or byte-by-byte
            for (int y = (int) crop.y; y < (int) (crop.y + crop.height); y++) {
                for (int x = (int) crop.x; x < (int) (crop.x + crop.width); x++) {
                    for (int i = 0; i < bytesPerPixel; i++) {
                        croppedData[((y - (int) crop.y) * (int) crop.width + (x - (int) crop.x)) * bytesPerPixel + i] =
                                image.getData()[(y * image.width + x) * bytesPerPixel + i];
                    }
                }
            }

            image.setData(croppedData);
            image.width = (int) crop.width;
            image.height = (int) crop.height;
        }
    }

    // Convert image data to desired format
    public void ImageFormat(Image image, int newFormat) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        if ((newFormat != 0) && (image.format != newFormat)) {
            if ((image.format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) && (newFormat < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB)) {
                Vector4[] pixels = LoadImageDataNormalized(image);     // Supports 8 to 32 bit per channel

                // WARNING! We loose mipmaps data --> Regenerated at the end...
                image.data = null;
                image.format = newFormat;

                int k = 0;

                switch (image.getFormat()) {
                    case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                        byte[] data = new byte[image.width * image.height];

                        for (int i = 0; i < image.width * image.height; i++) {
                            data[i] =
                                    (byte)(((pixels[i].x * 0.299f) + (pixels[i].y * 0.587f) + (pixels[i].z * 0.114f))*255);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                        byte[] data = new byte[image.width * image.height * 2];

                        for (int i = 0; i < image.width * image.height * 2; i += 2, k++) {
                            data[i] =
                                    (byte) ((pixels[k].x * 0.299f + pixels[k].y * 0.587f + pixels[k].z * 0.114f) * 255.0f);
                            data[i + 1] = (byte) (pixels[k].w * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                        byte[] data = new byte[image.width * image.height];

                        short r, g, b;

                        for (int i = 0; i < image.width * image.height; i++) {
                            r = (byte) Math.round(pixels[i].x * 31.0f);
                            g = (byte) Math.round(pixels[i].y * 63.0f);
                            b = (byte) Math.round(pixels[i].z * 31.0f);

                            data[i] = (byte) (r << 11 | g << 5 | b);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                        byte[] data = new byte[image.width * image.height * 3];
                        k = 0;
                        for (int i = 0; i < image.width * image.height * 3; i += 3, k++) {
                            data[i] = (byte) (pixels[k].x * 255.0f);
                            data[i + 1] = (byte) (pixels[k].y * 255.0f);
                            data[i + 2] = (byte) (pixels[k].z * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                        byte[] data = new byte[image.width * image.height];

                        short r, g, b, a;

                        for (int i = 0; i < image.width * image.height; i++) {
                            r = (byte) (Math.round(pixels[i].x * 31.0f));
                            g = (byte) (Math.round(pixels[i].y * 31.0f));
                            b = (byte) (Math.round(pixels[i].z * 31.0f));
                            a = (byte) ((pixels[i].w > ((float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f)) ?
                                    1 : 0);

                            data[i] = (byte) (r << 11 | g << 6 | b << 1 | a);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                        byte[] data = new byte[image.width * image.height];

                        short r, g, b, a;

                        for (int i = 0; i < image.width * image.height; i++) {
                            r = (byte) (Math.round(pixels[i].x * 15.0f));
                            g = (byte) (Math.round(pixels[i].y * 15.0f));
                            b = (byte) (Math.round(pixels[i].z * 15.0f));
                            a = (byte) (Math.round(pixels[i].w * 15.0f));

                            data[i] = (byte) (r << 12 | g << 8 | b << 4 | a);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                        byte[] data = new byte[image.width * image.height * 4];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 4; i += 4, k++) {
                            data[i] = (byte) (pixels[k].x * 255.0f);
                            data[i + 1] = (byte) (pixels[k].y * 255.0f);
                            data[i + 2] = (byte) (pixels[k].z * 255.0f);
                            data[i + 3] = (byte) (pixels[k].w * 255.0f);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32:{
                        // WARNING: Image is converted to GRAYSCALE eqeuivalent 32bit

                        byte[] data = new byte[image.width * image.height];

                        for (int i = 0; i < image.width * image.height; i++) {
                            data[i] =
                                    (byte) (pixels[i].x * 0.299f + pixels[i].y * 0.587f + pixels[i].z * 0.114f);
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                        byte[] data = new byte[image.width * image.height * 3];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 3; i += 3, k++) {
                            data[i] = (byte) pixels[k].x;
                            data[i + 1] = (byte) pixels[k].y;
                            data[i + 2] = (byte) pixels[k].z;
                        }
                        image.setData(data);
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                        byte[] data = new byte[image.width * image.height * 4];

                        k = 0;
                        for (int i = 0; i < image.width * image.height * 4; i += 4, k++) {
                            data[i] = (byte) pixels[k].x;
                            data[i + 1] = (byte) pixels[k].y;
                            data[i + 2] = (byte) pixels[k].z;
                            data[i + 3] = (byte) pixels[k].w;
                        }
                        image.setData(data);
                    }
                    break;
                    default:
                        break;
                }

                // In case original image had mipmaps, generate mipmaps for formated image
                // NOTE: Original mipmaps are replaced by new ones, if custom mipmaps were used, they are lost
                if (image.mipmaps > 1) {
                    image.mipmaps = 1;
                    if (SUPPORT_IMAGE_MANIPULATION) {
                        if (image.data != null) {
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

    // Convert image to POT (power-of-two)
    // NOTE: It could be useful on OpenGL ES 2.0 (RPI, HTML5)
    public void ImageToPOT(Image image, Color fill) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        // Calculate next power-of-two values
        // NOTE: Just add the required amount of pixels at the right and bottom sides of image...
        int potWidth = (int) Math.pow(2, Math.ceil(Math.log((float) image.width) / Math.log(2)));
        int potHeight = (int) Math.pow(2, Math.ceil(Math.log((float) image.height) / Math.log(2)));

        // Check if POT texture generation is required (if texture is not already POT)
        if ((potWidth != image.width) || (potHeight != image.height)) {
            ImageResizeCanvas(image, potWidth, potHeight, 0, 0, fill);
        }
    }

    //support image manipulation

    // Create an image from text (default font)
    public Image ImageText(String text, int fontSize, Color color) {
        int defaultFontSize = 10;   // Default Font chars height in pixel
        if (fontSize < defaultFontSize) {
            fontSize = defaultFontSize;
        }
        int spacing = fontSize / defaultFontSize;

        return ImageTextEx(context.text.GetFontDefault(), text, (float) fontSize, (float) spacing, color);
    }

    // Create an image from text (custom sprite font)
    public Image ImageTextEx(Font font, String text, float fontSize, float spacing, Color tint) {
        int length = text.length();

        int textOffsetX = 0;            // Image drawing position X
        int textOffsetY = 0;            // Offset between lines (on line break '\n')

        // NOTE: rText image is generated at font base size, later scaled to desired font size
        Vector2 imSize = context.text.MeasureTextEx(font, text, (float) font.getBaseSize(), spacing);

        // Create image to store text
        Image imText = GenImageColor((int) imSize.x, (int) imSize.y, BLANK);

        for (int i = 0; i < length; i++) {
            // Get next codepoint from byte string and glyph index in font
            int codepointByteCount = 0;
            int codepoint = context.text.GetCodepoint(text.toCharArray(), i);
            int index = context.text.GetGlyphIndex(font, codepoint);

            codepointByteCount = context.text.getCPBC();

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f) {
                codepointByteCount = 1;
            }

            if (codepoint == '\n') {
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += (font.getBaseSize() + font.getBaseSize() / 2);
                textOffsetX = 0;
            }
            else{
                if ((codepoint != ' ') && (codepoint != '\t')) {
                    Rectangle rec = new Rectangle((float) (textOffsetX + font.getGlyphs()[index].getOffsetX()),
                                                  (float) (textOffsetY + font.getGlyphs()[index].getOffsetY()),
                                                  font.getRecs()[index].getWidth(), font.getRecs()[index].getHeight());


                    ImageDraw(imText, font.getGlyphs()[index].getImage(), new Rectangle(0, 0,
                                                                                        (float) font.getGlyphs()[index].getImage().getWidth(),
                                                                                        (float) font.getGlyphs()[index].getImage().getHeight()), rec, tint);

                }

                if (font.getGlyphs()[index].getAdvanceX() == 0) {
                    textOffsetX += (int) (font.getRecs()[index].getWidth() + spacing);
                }
                else{
                    textOffsetX += font.getGlyphs()[index].getAdvanceX() + (int) spacing;
                }
            }

            i += (codepointByteCount - 1);   // Move text bytes counter to next codepoint
        }

        // Scale image depending on text size
        if (fontSize > imSize.getY()) {
            float scaleFactor = fontSize / imSize.y;
            Tracelog(LOG_INFO, "IMAGE: rText scaled by factor: " + scaleFactor);

            // Using nearest-neighbor scaling algorithm for default font
            if (font.getTexture().getId() == context.text.GetFontDefault().getTexture().getId()) {
                ImageResizeNN(imText, (int) (imSize.x * scaleFactor), (int) (imSize.y * scaleFactor));
            }
            else{
                ImageResize(imText, (int) (imSize.x * scaleFactor), (int) (imSize.y * scaleFactor));
            }
        }

        return imText;
    }

    // Crop image depending on alpha value
    // NOTE: Threshold is defined as a percentatge: 0.0f -> 1.0f
    public void ImageAlphaCrop(Image image, float threshold) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        Rectangle crop = GetImageAlphaBorder(image, threshold);

        // Crop if rectangle is valid
        if (((int) crop.width != 0) && ((int) crop.height != 0)) {
            ImageCrop(image, crop);
        }
    }

    // Clear alpha channel to desired color
    // NOTE: Threshold defines the alpha limit, 0.0f to 1.0f
    public void ImageAlphaClear(Image image, Color color, float threshold) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        if (image.mipmaps > 1) {
            Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else{
            switch (image.format) {
                case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                    char thresholdValue = (char) (threshold * 255.0f);
                    for (int i = 1; i < image.width * image.height * 2; i += 2) {
                        if (image.getData()[i] <= thresholdValue) {
                            image.getData()[i - 1] = (byte) color.r;
                            image.getData()[i] = (byte) color.a;
                        }
                    }
                }
                break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                    byte thresholdValue = (byte) ((threshold < 0.5f) ? 0 : 1);

                    byte r = (byte) (Math.round((float) color.r * 31.0f));
                    byte g = (byte) (Math.round((float) color.g * 31.0f));
                    byte b = (byte) (Math.round((float) color.b * 31.0f));
                    byte a = (byte) ((color.a < 128) ? 0 : 1);

                    for (int i = 0; i < image.width * image.height; i++) {
                        if ((image.getData()[i] & 0b0000000000000001) <= thresholdValue) {
                            image.getData()[i] = (byte) (r << 11 | g << 6 | b << 1 | a);
                        }
                    }
                }
                break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                    char thresholdValue = (char) (threshold * 15.0f);

                    char r = (char) (Math.round((float) color.r * 15.0f));
                    char g = (char) (Math.round((float) color.g * 15.0f));
                    char b = (char) (Math.round((float) color.b * 15.0f));
                    char a = (char) (Math.round((float) color.a * 15.0f));

                    for (int i = 0; i < image.width * image.height; i++) {
                        if ((image.getData()[i] & 0x000f) <= thresholdValue) {
                            image.getData()[i] = (byte) (r << 12 | g << 8 | b << 4 | a);
                        }
                    }
                }
                break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                    char thresholdValue = (char) (threshold * 255.0f);
                    for (int i = 3; i < image.width * image.height * 4; i += 4) {
                        if (image.getData()[i] <= thresholdValue) {
                            image.getData()[i - 3] = (byte) color.r;
                            image.getData()[i - 2] = (byte) color.g;
                            image.getData()[i - 1] = (byte) color.b;
                            image.getData()[i] = (byte) color.a;
                        }
                    }
                }
                break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                    for (int i = 3; i < image.width * image.height * 4; i += 4) {
                        if (image.getData()[i] <= threshold) {
                            image.getData()[i - 3] = (byte) ((float) color.r / 255.0f);
                            image.getData()[i - 2] = (byte) ((float) color.g / 255.0f);
                            image.getData()[i - 1] = (byte) ((float) color.b / 255.0f);
                            image.getData()[i] = (byte) ((float) color.a / 255.0f);
                        }
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    // Apply alpha mask to image
    // NOTE 1: Returned image is GRAY_ALPHA (16bit) or RGBA (32bit)
    // NOTE 2: alphaMask should be same size as image
    public void ImageAlphaMask(Image image, Image alphaMask) {
        if ((image.width != alphaMask.width) || (image.height != alphaMask.height)) {
            Tracelog(LOG_WARNING, "IMAGE: Alpha mask must be same size as image");
        }
        else if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "IMAGE: Alpha mask can not be applied to compressed data formats");
        }
        else{
            // Force mask to be Grayscale
            Image mask = ImageCopy(alphaMask);
            if (mask.format != RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                ImageFormat(mask, RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE)
                ;
            }

            // In case image is only grayscale, we just add alpha channel
            if (image.format == RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                byte[] data = new byte[image.width * image.height * 2];

                // Apply alpha mask to alpha channel
                for (int i = 0, k = 0; (i < mask.width * mask.height) || (i < image.width * image.height); i++, k += 2) {
                    data[k] = image.getData()[i];
                    data[k + 1] = image.getData()[i];
                }

                image.setData(data);
                image.format = RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
            }
            else{
                // Convert image to RGBA
                if (image.format != RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
                    ImageFormat(image, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
                }

                // Apply alpha mask to alpha channel
                for (int i = 0, k = 3; (i < mask.width * mask.height) || (i < image.width * image.height); i++, k += 4) {
                    image.getData()[k] = mask.getData()[i];
                }
            }

            UnloadImage(mask);
        }
    }

    // Premultiply alpha channel
    public void ImageAlphaPremultiply(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        float alpha = 0.0f;
        Color[] pixels = LoadImageColors(image);

        for (int i = 0; i < image.width * image.height; i++) {
            if (pixels[i].a == 0) {
                pixels[i].r = 0;
                pixels[i].g = 0;
                pixels[i].b = 0;
            }
            else if (pixels[i].a < 255) {
                alpha = (float) pixels[i].a / 255.0f;
                pixels[i].r = (byte) ((float) pixels[i].r * alpha);
                pixels[i].g = (byte) ((float) pixels[i].g * alpha);
                pixels[i].b = (byte) ((float) pixels[i].b * alpha);
            }
        }

        int format = image.format;
        image.setData(pixels);
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Resize and image to new size
    // NOTE: Uses stb default scaling filters (both bicubic):
    // STBIR_DEFAULT_FILTER_UPSAMPLE    STBIR_FILTER_CATMULLROM
    // STBIR_DEFAULT_FILTER_DOWNSAMPLE  STBIR_FILTER_MITCHELL   (high-quality Catmull-Rom)
    public void ImageResize(Image image, int newWidth, int newHeight) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        boolean fastPath = (image.format != RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) && (image.format != RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA)
                && (image.format != RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8) && (image.format != RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);

        ByteBuffer tmpb = MemoryUtil.memAlloc(image.data.getSize());

        for (int i = 0; i < image.data.getSize(); i++) {
            tmpb.put(i, (byte) image.data.getElem(i));
        }

        int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
        ByteBuffer output = MemoryUtil.memAlloc(newWidth * newHeight * bytesPerPixel);
        if (fastPath) {

            switch (image.getFormat()) {
                case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                                       newWidth, newHeight, 0, 1);
                    break;
                case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                                       newWidth, newHeight, 0, 2);
                    break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                                       newWidth, newHeight, 0, 3);
                    break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                    stbir_resize_uint8(tmpb, image.width, image.height, 0, output,
                                       newWidth, newHeight, 0, 4);
                    break;
                default:
                    break;
            }

            byte[] outputi = new byte[output.capacity()];

            for (int i = 0; i < outputi.length; i++) {
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

            byte[] outputi = new byte[output.capacity()];

            for (int i = 0; i < outputi.length; i++) {
                outputi[i] = output.get(i);
            }

            image.setData(outputi);
            image.width = newWidth;
            image.height = newHeight;
            image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

            ImageFormat(image, format);  // Reformat 32bit RGBA image to original format
        }
    }

    // Resize and image to new size using Nearest-Neighbor scaling algorithm
    public void ImageResizeNN(Image image, int newWidth, int newHeight) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        Color[] pixels = LoadImageColors(image);
        byte[] output = new byte[newWidth * newHeight * 4];

        // EDIT: added +1 to account for an early rounding problem
        int xRatio = ((image.width << 16) / newWidth) + 1;
        int yRatio = ((image.height << 16) / newHeight) + 1;

        int x2, y2;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
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
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);  // Reformat 32bit RGBA image to original format

    }

    // Resize canvas and fill with color
    // NOTE: Resize offset is relative to the top-left corner of the original image
    public void ImageResizeCanvas(Image image, int newWidth, int newHeight, int offsetX, int offsetY, Color fill) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (image.mipmaps > 1) Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else if ((newWidth != image.width) || (newHeight != image.height)) {
            Rectangle srcRec = new Rectangle(0, 0, (float) image.width, (float) image.height);
            Vector2 dstPos = new Vector2((float) offsetX, (float) offsetY);

            if (offsetX < 0) {
                srcRec.x = (float) -offsetX;
                srcRec.width += (float) offsetX;
                dstPos.x = 0;
            }
            else if ((offsetX + image.width) > newWidth) srcRec.width = (float) (newWidth - offsetX);

            if (offsetY < 0) {
                srcRec.y = (float) -offsetY;
                srcRec.height += (float) offsetY;
                dstPos.y = 0;
            }
            else if ((offsetY + image.height) > newHeight) srcRec.height = (float) (newHeight - offsetY);

            if (newWidth < srcRec.width) srcRec.width = (float) newWidth;
            if (newHeight < srcRec.height) srcRec.height = (float) newHeight;

            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] resizedData = new byte[newWidth * newHeight * bytesPerPixel];

            // TODO: Fill resizedData with fill color (must be formatted to image.format)

            int dstOffsetSize = ((int) dstPos.y * newWidth + (int) dstPos.x) * bytesPerPixel;
            for (int x = 0; x < srcRec.width; x++) {
                for (int y = 0; y < (int) srcRec.height; y++) {
                    //memcpy(resizedData + dstOffsetSize, ((unsigned char *)image.data) + ((y + (int)srcRec.y)*image.width + (int)srcRec.x)*bytesPerPixel, (int)srcRec.width*bytesPerPixel);
                    //dstOffsetSize += (newWidth*bytesPerPixel);
                    for (int i = 0; i < bytesPerPixel; i++) {
                        resizedData[(y * image.width + x) * bytesPerPixel + i] =
                                image.getData()[(y * image.width + (image.width - 1 - x)) * bytesPerPixel + i];
                    }
                }
            }


            image.setData(resizedData);
            image.width = newWidth;
            image.height = newHeight;
        }
    }

    // Generate all mipmap levels for a provided image
    // NOTE 1: Supports POT and NPOT images
    // NOTE 2: image.data is scaled to include mipmap levels
    // NOTE 3: Mipmaps format is the same as base image
    public void ImageMipmaps(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        int mipCount = 1;                   // Required mipmap levels count (including base level)
        int mipWidth = image.width;        // Base image width
        int mipHeight = image.height;      // Base image height
        int mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);  // Image data size (in bytes)

        // Count mipmap levels required
        while ((mipWidth != 1) || (mipHeight != 1)) {
            if (mipWidth != 1) {
                mipWidth /= 2;
            }
            if (mipHeight != 1) {
                mipHeight /= 2;
            }

            // Security check for NPOT textures
            if (mipWidth < 1) {
                mipWidth = 1;
            }
            if (mipHeight < 1) {
                mipHeight = 1;
            }

            Tracelog.Tracelog("IMAGE: Next mipmap level: " + mipWidth + " x " + mipHeight + " - current size " + mipSize);

            mipCount++;
            mipSize += GetPixelDataSize(mipWidth, mipHeight, image.format);       // Add mipmap size (in bytes)
        }

        if (image.mipmaps < mipCount) {
            DataBuffer temp = image.data;

            if (temp != null) {
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

            for (int i = 1; i < mipCount; i++) {
                Tracelog.Tracelog("IMAGE: Generating mipmap level: " + i + " (" + mipWidth + " x " + mipHeight + ")" +
                                  " - size: " + mipSize + " - offset: " + nextmip);

                ImageResize(imCopy, mipWidth, mipHeight);  // Uses internally Mitchell cubic downscale filter

                nextmip = imCopy.data.getSize();
                nextmip += mipSize;
                image.mipmaps++;

                mipWidth /= 2;
                mipHeight /= 2;

                // Security check for NPOT textures
                if (mipWidth < 1) {
                    mipWidth = 1;
                }
                if (mipHeight < 1) {
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

    // Dither image data to 16bpp or lower (Floyd-Steinberg dithering)
    // NOTE: In case selected bpp do not represent an known 16bit format,
    // dithered data is stored in the LSB part of the  short
    public void ImageDither(Image image, int rBpp, int gBpp, int bBpp, int aBpp) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "IMAGE: Compressed data formats can not be dithered");
            return;
        }

        if ((rBpp + gBpp + bBpp + aBpp) > 16) {
            Tracelog(LOG_WARNING, "IMAGE: Unsupported dithering bpps (" + (rBpp + gBpp + bBpp + aBpp) + "bpp), only 16bpp or lower modes supported");
        }
        else{
            Color[] pixels = LoadImageColors(image);


            if ((image.format != RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8) && (image.format != RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8)) {
                Tracelog(LOG_WARNING, "IMAGE: Format is already 16bpp or lower, dithering could have no effect");
            }

            // Define new image format, check if desired bpp match internal known format
            if ((rBpp == 5) && (gBpp == 6) && (bBpp == 5) && (aBpp == 0)) {
                image.format = RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5;
            }
            else if ((rBpp == 5) && (gBpp == 5) && (bBpp == 5) && (aBpp == 1)) {
                image.format = RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1;
            }
            else if ((rBpp == 4) && (gBpp == 4) && (bBpp == 4) && (aBpp == 4)) {
                image.format = RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4;
            }
            else{
                image.format = 0;
                Tracelog(LOG_WARNING, "IMAGE: Unsupported dithered OpenGL internal format: " +
                        (rBpp + gBpp + bBpp + aBpp) + "bpp (R" + rBpp + "G" + gBpp + "B" + bBpp + "A" + aBpp + ")");
            }

            // NOTE: We will store the dithered data as  short (16bpp)
            image.setData(new byte[image.width * image.height * Short.BYTES]);

            Color oldPixel = WHITE;
            Color newPixel = WHITE;

            int rError, gError, bError;
            short rPixel, gPixel, bPixel, aPixel;   // Used for 16bit pixel composition

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    oldPixel = pixels[y * image.width + x];

                    // NOTE: New pixel obtained by bits truncate, it would be better to round values (check ImageFormat())
                    newPixel.r = (byte) (oldPixel.r >> (8 - rBpp));     // R bits
                    newPixel.g = (byte) (oldPixel.g >> (8 - gBpp));     // G bits
                    newPixel.b = (byte) (oldPixel.b >> (8 - bBpp));     // B bits
                    newPixel.a = (byte) (oldPixel.a >> (8 - aBpp));     // A bits (not used on dithering)

                    // NOTE: Error must be computed between new and old pixel but using same number of bits!
                    // We want to know how much color precision we have lost...
                    rError = oldPixel.r - (newPixel.r << (8 - rBpp));
                    gError = oldPixel.g - (newPixel.g << (8 - gBpp));
                    bError = oldPixel.b - (newPixel.b << (8 - bBpp));

                    pixels[y * image.width + x] = newPixel;

                    // NOTE: Some cases are out of the array and should be ignored
                    if (x < (image.width - 1)) {
                        pixels[y * image.width + x + 1].r = (byte) Math.min(pixels[y * image.width + x + 1].r + (int) ((float) rError * 7.0f / 16), 0xff);
                        pixels[y * image.width + x + 1].g = (byte) Math.min(pixels[y * image.width + x + 1].g + (int) ((float) gError * 7.0f / 16), 0xff);
                        pixels[y * image.width + x + 1].b = (byte) Math.min(pixels[y * image.width + x + 1].b + (int) ((float) bError * 7.0f / 16), 0xff);
                    }

                    if ((x > 0) && (y < (image.height - 1))) {
                        pixels[(y + 1) * image.width + x - 1].r = (byte) Math.min(pixels[(y + 1) * image.width + x - 1].r + (int) ((float) rError * 3.0f / 16), 0xff);
                        pixels[(y + 1) * image.width + x - 1].g = (byte) Math.min(pixels[(y + 1) * image.width + x - 1].g + (int) ((float) gError * 3.0f / 16), 0xff);
                        pixels[(y + 1) * image.width + x - 1].b = (byte) Math.min(pixels[(y + 1) * image.width + x - 1].b + (int) ((float) bError * 3.0f / 16), 0xff);
                    }

                    if (y < (image.height - 1)) {
                        pixels[(y + 1) * image.width + x].r = (byte) Math.min(pixels[(y + 1) * image.width + x].r + (int) ((float) rError * 5.0f / 16), 0xff);
                        pixels[(y + 1) * image.width + x].g = (byte) Math.min(pixels[(y + 1) * image.width + x].g + (int) ((float) gError * 5.0f / 16), 0xff);
                        pixels[(y + 1) * image.width + x].b = (byte) Math.min(pixels[(y + 1) * image.width + x].b + (int) ((float) bError * 5.0f / 16), 0xff);
                    }

                    if ((x < (image.width - 1)) && (y < (image.height - 1))) {
                        pixels[(y + 1) * image.width + x + 1].r = (byte) Math.min(pixels[(y + 1) * image.width + x + 1].r + (int) ((float) rError * 1.0f / 16), 0xff);
                        pixels[(y + 1) * image.width + x + 1].g = (byte) Math.min(pixels[(y + 1) * image.width + x + 1].g + (int) ((float) gError * 1.0f / 16), 0xff);
                        pixels[(y + 1) * image.width + x + 1].b = (byte) Math.min(pixels[(y + 1) * image.width + x + 1].b + (int) ((float) bError * 1.0f / 16), 0xff);
                    }

                    rPixel = (byte) newPixel.r;
                    gPixel = (byte) newPixel.g;
                    bPixel = (byte) newPixel.b;
                    aPixel = (byte) newPixel.a;

                    image.getData()[y * image.width + x] = (byte) ((rPixel << (gBpp + bBpp + aBpp)) |
                            (gPixel << (bBpp + aBpp)) | (bPixel << aBpp) | aPixel);
                }
            }

            UnloadImageColors(pixels);
        }
    }

    // Flip image vertically
    public void ImageFlipVertical(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (image.mipmaps > 1) Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else{
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] flippedData = new byte[image.width * image.height * bytesPerPixel];
            byte[] imgData = image.getData();

            for (int i = (image.height - 1), offsetSize = 0; i >= 0; i--) {
                System.arraycopy(imgData, i*image.width*bytesPerPixel, flippedData, offsetSize, image.width*bytesPerPixel);
                offsetSize += image.width*bytesPerPixel;
            }

            image.setData(flippedData);
        }
    }

    // Flip image horizontally
    public void ImageFlipHorizontal(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (image.mipmaps > 1) Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else{
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] flippedData = new byte[image.width * image.height * bytesPerPixel];
            byte[] imgData = image.getData();

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    //copy data pixel by pixel
                    for (int i = 0; i < bytesPerPixel; i++) {
                        flippedData[(y * image.width + x) * bytesPerPixel + i] = imgData[(y * image.width + (image.width - 1 - x)) * bytesPerPixel + i];
                    }
                }
            }

            image.setData(flippedData);
        }
    }

    // Rotate image clockwise 90deg
    public void ImageRotateCW(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (image.mipmaps > 1) Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else{
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] rotatedData = new byte[image.width * image.height * bytesPerPixel];

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    //memcpy(rotatedData + (x*image.height + (image.height - y - 1))*bytesPerPixel, (( char *)image.data) + (y*image.width + x)*bytesPerPixel, bytesPerPixel);
                    for (int i = 0; i < bytesPerPixel; i++) {
                        rotatedData[(x * image.height + (image.height - y - 1)) * bytesPerPixel + i] =
                                image.getData()[(y * image.width + x) * bytesPerPixel + i];
                    }
                }
            }

            image.setData(rotatedData);
            int width = image.width;

            image.width = image.height;
            image.height = width;
        }
    }

    // Rotate image counter-clockwise 90deg
    public void ImageRotateCCW(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (image.mipmaps > 1) Tracelog(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else{
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] rotatedData = new byte[image.width * image.height * bytesPerPixel];

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    //memcpy(rotatedData + (x*image.height + y))*bytesPerPixel, (( char *)image.data) + (y*image.width + (image.width - x - 1))*bytesPerPixel, bytesPerPixel);
                    for (int i = 0; i < bytesPerPixel; i++) {
                        rotatedData[(x * image.height + y) * bytesPerPixel + i] =
                                image.getData()[(y * image.width + (image.width - x - 1)) * bytesPerPixel + i];
                    }
                }
            }

            image.setData(rotatedData);
            int width = image.width;

            image.width = image.height;
            image.height = width;
        }
    }

    // Modify image color: tint
    public void ImageColorTint(Image image, Color color) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        Color[] pixels = LoadImageColors(image);

        float cR = (float) color.r / 255;
        float cG = (float) color.g / 255;
        float cB = (float) color.b / 255;
        float cA = (float) color.a / 255;

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int index = y * image.width + x;
                byte r = (byte) (((float) pixels[index].r / 255 * cR) * 255.0f);
                byte g = (byte) (((float) pixels[index].g / 255 * cG) * 255.0f);
                byte b = (byte) (((float) pixels[index].b / 255 * cB) * 255.0f);
                byte a = (byte) (((float) pixels[index].a / 255 * cA) * 255.0f);

                pixels[index].r = r;
                pixels[index].g = g;
                pixels[index].b = b;
                pixels[index].a = a;
            }
        }

        int format = image.format;

        image.setData(pixels);
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: invert
    public void ImageColorInvert(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        Color[] pixels = LoadImageColors(image);

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                pixels[y * image.width + x].r = (byte) (255 - pixels[y * image.width + x].r);
                pixels[y * image.width + x].g = (byte) (255 - pixels[y * image.width + x].g);
                pixels[y * image.width + x].b = (byte) (255 - pixels[y * image.width + x].b);
            }
        }

        int format = image.format;

        image.setData(pixels);
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: grayscale
    public void ImageColorGrayscale(Image image) {
        ImageFormat(image, RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE);
    }

    // Modify image color: contrast
    // NOTE: Contrast values between -100 and 100
    public void ImageColorContrast(Image image, float contrast) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (contrast < -100) contrast = -100;
        if (contrast > 100) contrast = 100;

        contrast = (100.0f + contrast) / 100.0f;
        contrast *= contrast;

        Color[] pixels = LoadImageColors(image);

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                float pR = pixels[y * image.width + x].r / 255.0f;
                pR -= 0.5f;
                pR *= contrast;
                pR += 0.5f;
                pR *= 255;
                if (pR < 0) pR = 0;
                if (pR > 255) pR = 255;

                float pG = pixels[y * image.width + x].g / 255.0f;
                pG -= 0.5f;
                pG *= contrast;
                pG += 0.5f;
                pG *= 255;
                if (pG < 0) pG = 0;
                if (pG > 255) pG = 255;

                float pB = pixels[y * image.width + x].b / 255.0f;
                pB -= 0.5f;
                pB *= contrast;
                pB += 0.5f;
                pB *= 255;
                if (pB < 0) pB = 0;
                if (pB > 255) pB = 255;

                pixels[y * image.width + x].r = (byte) pR;
                pixels[y * image.width + x].g = (byte) pG;
                pixels[y * image.width + x].b = (byte) pB;
            }
        }

        int format = image.format;

        image.setData(pixels);
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: brightness
    // NOTE: Brightness values between -255 and 255
    public void ImageColorBrightness(Image image, int brightness) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        if (brightness < -255) brightness = -255;
        if (brightness > 255) brightness = 255;

        Color[] pixels = LoadImageColors(image);

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int cR = pixels[y * image.width + x].r + brightness;
                int cG = pixels[y * image.width + x].g + brightness;
                int cB = pixels[y * image.width + x].b + brightness;

                if (cR < 0) cR = 1;
                if (cR > 255) cR = 255;

                if (cG < 0) cG = 1;
                if (cG > 255) cG = 255;

                if (cB < 0) cB = 1;
                if (cB > 255) cB = 255;

                pixels[y * image.width + x].r = (byte) cR;
                pixels[y * image.width + x].g = (byte) cG;
                pixels[y * image.width + x].b = (byte) cB;
            }
        }

        int format = image.format;

        image.setData(pixels);
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Modify image color: replace color
    public void ImageColorReplace(Image image, Color color, Color replace) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) return;

        Color[] pixels = LoadImageColors(image);

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                if ((pixels[y * image.width + x].r == color.r) &&
                        (pixels[y * image.width + x].g == color.g) &&
                        (pixels[y * image.width + x].b == color.b) &&
                        (pixels[y * image.width + x].a == color.a)) {
                    pixels[y * image.width + x].r = replace.r;
                    pixels[y * image.width + x].g = replace.g;
                    pixels[y * image.width + x].b = replace.b;
                    pixels[y * image.width + x].a = replace.a;
                }
            }
        }

        int format = image.format;

        image.setData(pixels);
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        ImageFormat(image, format);
    }

    // Load color data from image as a Color array (RGBA - 32bit)
    // NOTE: Memory allocated should be freed using UnloadImageColors();
    public Color[] LoadImageColors(Image image) {
        if ((image.width == 0) || (image.height == 0)) {
            return null;
        }

        Color[] pixels = new Color[image.width * image.height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else{
            if ((image.format == RL_PIXELFORMAT_UNCOMPRESSED_R32) || (image.format == RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32) ||
                    (image.format == RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32)) {
                Tracelog(LOG_WARNING, "IMAGE: Pixel format converted from 32bit to 8bit per channel");
            }

            byte[] imgData = image.getData();

            for (int i = 0, k = 0; i < image.width * image.height; i++) {
                switch (image.getFormat()) {
                    case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                        pixels[i].setR(Byte.toUnsignedInt(imgData[i]));
                        pixels[i].setG(Byte.toUnsignedInt(imgData[i]));
                        pixels[i].setB(Byte.toUnsignedInt(imgData[i]));
                        pixels[i].setA(Byte.toUnsignedInt((byte) 255));

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                        pixels[i].setR(Byte.toUnsignedInt(imgData[k]));
                        pixels[i].setG(Byte.toUnsignedInt(imgData[k]));
                        pixels[i].setB(Byte.toUnsignedInt(imgData[k]));
                        pixels[i].setA(Byte.toUnsignedInt(imgData[k + 1]));

                        k += 2;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                        short pixel = imgData[i];

                        pixels[i].setR(Byte.toUnsignedInt((byte) (((pixel & 0b1111100000000000) >> 11) * (255 / 31))));
                        pixels[i].setG(Byte.toUnsignedInt((byte) (((pixel & 0b0000011111000000) >> 6) * (255 / 31))));
                        pixels[i].setB(Byte.toUnsignedInt((byte) (((pixel & 0b0000000000111110) >> 1) * (255 / 31))));
                        pixels[i].setA(Byte.toUnsignedInt((byte) ((pixel & 0b0000000000000001) * 255)));

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                        short pixel = imgData[i];

                        pixels[i].setR(Byte.toUnsignedInt((byte) (((pixel & 0b1111100000000000) >> 11) * (255 / 31))));
                        pixels[i].setG(Byte.toUnsignedInt((byte) (((pixel & 0b0000011111100000) >> 5) * (255 / 63))));
                        pixels[i].setB(Byte.toUnsignedInt((byte) ((pixel & 0b0000000000011111) * (255 / 31))));
                        pixels[i].setA(Byte.toUnsignedInt((byte) 255));

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                        short pixel = imgData[i];

                        pixels[i].setR(Byte.toUnsignedInt((byte) (((pixel & 0b1111000000000000) >> 12) * (255 / 15))));
                        pixels[i].setG(Byte.toUnsignedInt((byte) (((pixel & 0b0000111100000000) >> 8) * (255 / 15))));
                        pixels[i].setB(Byte.toUnsignedInt((byte) (((pixel & 0b0000000011110000) >> 4) * (255 / 15))));
                        pixels[i].setA(Byte.toUnsignedInt((byte) ((pixel & 0b0000000000001111) * (255 / 15))));

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                        pixels[i].setR(Byte.toUnsignedInt(imgData[k]));
                        pixels[i].setG(Byte.toUnsignedInt(imgData[k + 1]));
                        pixels[i].setB(Byte.toUnsignedInt(imgData[k + 2]));
                        pixels[i].setA(Byte.toUnsignedInt(imgData[k + 3]));

                        k += 4;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                        pixels[i].setR(Byte.toUnsignedInt(imgData[k]));
                        pixels[i].setG(Byte.toUnsignedInt(imgData[k + 1]));
                        pixels[i].setB(Byte.toUnsignedInt(imgData[k + 2]));
                        pixels[i].setA(255);

                        k += 3;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32:{
                        pixels[i].setR(Byte.toUnsignedInt((byte) (imgData[k] * 255.0f)));
                        pixels[i].setG(Byte.toUnsignedInt((byte) 0));
                        pixels[i].setB(Byte.toUnsignedInt((byte) 0));
                        pixels[i].setA(Byte.toUnsignedInt((byte) 255));

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                        pixels[i].setR(Byte.toUnsignedInt((byte) (imgData[k] * 255.0f)));
                        pixels[i].setG(Byte.toUnsignedInt((byte) (imgData[k + 1] * 255.0f)));
                        pixels[i].setB(Byte.toUnsignedInt((byte) (imgData[k + 2] * 255.0f)));
                        pixels[i].setA(Byte.toUnsignedInt((byte)255));

                        k += 3;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                        pixels[i].setR(Byte.toUnsignedInt((byte)(imgData[k] * 255.0f)));
                        pixels[i].setG(Byte.toUnsignedInt((byte)(imgData[k] * 255.0f)));
                        pixels[i].setB(Byte.toUnsignedInt((byte)(imgData[k] * 255.0f)));
                        pixels[i].setA(Byte.toUnsignedInt((byte)(imgData[k] * 255.0f)));

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
    public Color[] LoadImagePalette(Image image, int maxPaletteSize, int colorsCount) {

        int palCount = 0;
        Color[] palette = null;
        Color[] pixels = LoadImageColors(image);

        if (pixels != null) {
            palette = new Color[maxPaletteSize];

            for (int i = 0; i < maxPaletteSize; i++) palette[i] = BLANK;   // Set all colors to BLANK

            for (int i = 0; i < image.width * image.height; i++) {
                if (pixels[i].a > 0) {
                    boolean colorInPalette = false;

                    // Check if the color is already on palette
                    for (int j = 0; j < maxPaletteSize; j++) {
                        if ((pixels[i].r == palette[j].r) && (pixels[i].g == palette[j].g) && (pixels[i].b == palette[j].b) && (pixels[i].a == palette[j].a)) {
                            colorInPalette = true;
                            break;
                        }
                    }

                    // Store color if not on the palette
                    if (!colorInPalette) {
                        palette[palCount] = pixels[i];      // Add pixels[i] to palette
                        palCount++;

                        // We reached the limit of colors supported by palette
                        if (palCount >= maxPaletteSize) {
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

    public Color[] UnloadImageColors(Color[] color) {
        return null;
    }

    public Color unloadImagePalette(Color colors) {
        return null;
    }

    // Get pixel data from image as Vector4 array (float normalized)
    public Vector4[] LoadImageDataNormalized(Image image) {
        Vector4[] pixels = new Vector4[image.width * image.height];

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Vector4();
        }

        if (image.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else{
            byte[] imgData = image.getData();
            for (int i = 0, k = 0; i < image.width * image.height; i++) {
                switch (image.getFormat()) {
                    case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                        pixels[i].setX(imgData[i] / 255.0f);
                        pixels[i].setY(imgData[i] / 255.0f);
                        pixels[i].setZ(imgData[i] / 255.0f);
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                        pixels[i].setX(imgData[k] / 255.0f);
                        pixels[i].setY(imgData[k] / 255.0f);
                        pixels[i].setZ(imgData[k] / 255.0f);
                        pixels[i].setW(imgData[k + 1] / 255.0f);

                        k += 2;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                        short pixel = imgData[i];

                        pixels[i].setX((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].setY((float) ((pixel & 0b0000011111000000) >> 6) * (1.0f / 31));
                        pixels[i].setZ((float) ((pixel & 0b0000000000111110) >> 1) * (1.0f / 31));
                        pixels[i].setW(((pixel & 0b0000000000000001) == 0) ? 0.0f : 1.0f);

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                        short pixel = imgData[i];

                        pixels[i].setX((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].setY((float) ((pixel & 0b0000011111100000) >> 5) * (1.0f / 63));
                        pixels[i].setZ((float) (pixel & 0b0000000000011111) * (1.0f / 31));
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                        short pixel = imgData[i];

                        pixels[i].setX((float) ((pixel & 0b1111000000000000) >> 12) * (1.0f / 15));
                        pixels[i].setY((float) ((pixel & 0b0000111100000000) >> 8) * (1.0f / 15));
                        pixels[i].setZ((float) ((pixel & 0b0000000011110000) >> 4) * (1.0f / 15));
                        pixels[i].setW((float) (pixel & 0b0000000000001111) * (1.0f / 15));

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                        pixels[i].setX(imgData[k] / 255.0f);
                        pixels[i].setY(imgData[k + 1] / 255.0f);
                        pixels[i].setZ(imgData[k + 2] / 255.0f);
                        pixels[i].setW(imgData[k + 3] / 255.0f);

                        k += 4;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                        pixels[i].setX(imgData[k] / 255.0f);
                        pixels[i].setY(imgData[k + 1] / 255.0f);
                        pixels[i].setZ(imgData[k + 2] / 255.0f);
                        pixels[i].setW(1.0f);

                        k += 3;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32:{
                        pixels[i].setX(imgData[k]);
                        pixels[i].setY(0.0f);
                        pixels[i].setZ(0.0f);
                        pixels[i].setW(1.0f);

                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                        pixels[i].setX(imgData[k]);
                        pixels[i].setY(imgData[k + 1]);
                        pixels[i].setZ(imgData[k + 2]);
                        pixels[i].setW(1.0f);

                        k += 3;
                    }
                    break;
                    case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                        pixels[i].setX(imgData[i]);
                        pixels[i].setY(imgData[k + 1]);
                        pixels[i].setZ(imgData[k + 2]);
                        pixels[i].setW(imgData[k + 3]);

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
    public Rectangle GetImageAlphaBorder(Image image, float threshold) {
        Rectangle crop = new Rectangle();

        Color[] pixels = LoadImageColors(image);

        if (pixels != null) {
            int xMin = 65536;   // Define a big enough number
            int xMax = 0;
            int yMin = 65536;
            int yMax = 0;

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    if (pixels[y * image.width + x].a > (threshold * 255.0f)) {
                        if (x < xMin) xMin = x;
                        if (x > xMax) xMax = x;
                        if (y < yMin) yMin = y;
                        if (y > yMax) yMax = y;
                    }
                }
            }

            // Check for empty blank image
            if ((xMin != 65536) && (xMax != 65536)) {
                crop = new Rectangle((float) xMin, (float) yMin, (float) ((xMax + 1) - xMin), (float) ((yMax + 1) - yMin));
            }

            UnloadImageColors(pixels);
        }

        return crop;
    }

    // Get image pixel color at (x, y) position
    Color GetImageColor(Image image, int x, int y) {
        Color color = new Color();
        byte[] imgData = image.getData();

        if ((x >=0) && (x < image.width) && (y >= 0) && (y < image.height)) {
            switch (image.format) {
                case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE: {
                    color.r = imgData[y*image.width + x];
                    color.g = imgData[y*image.width + x];
                    color.b = imgData[y*image.width + x];
                    color.a = (byte) 255;

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA: {
                    color.r = imgData[(y*image.width + x)*2];
                    color.g = imgData[(y*image.width + x)*2];
                    color.b = imgData[(y*image.width + x)*2];
                    color.a = imgData[(y*image.width + x)*2 + 1];

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1: {
                    short pixel = imgData[y*image.width + x];

                    color.r = (byte) (((pixel & 0b1111100000000000) >> 11)*(255/31));
                    color.g = (byte) (((pixel & 0b0000011111000000) >> 6)*(255/31));
                    color.b = (byte) (((pixel & 0b0000000000111110) >> 1)*(255/31));
                    color.a = (byte) ((pixel & 0b0000000000000001)*255);

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5: {
                    short pixel = imgData[y*image.width + x];

                    color.r = (byte) (((pixel & 0b1111100000000000) >> 11)*(255/31));
                    color.g = (byte) (((pixel & 0b0000011111100000) >> 5)*(255/63));
                    color.b = (byte) ((pixel & 0b0000000000011111)*(255/31));
                    color.a = (byte) 255;

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4: {
                    short pixel = imgData[y*image.width + x];

                    color.r = (byte) (((pixel & 0b1111000000000000) >> 12)*(255/15));
                    color.g = (byte) (((pixel & 0b0000111100000000) >> 8)*(255/15));
                    color.b = (byte) (((pixel & 0b0000000011110000) >> 4)*(255/15));
                    color.a = (byte) ((pixel & 0b0000000000001111)*(255/15));

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8: {
                    color.r = imgData[(y*image.width + x)*4];
                    color.g = imgData[(y*image.width + x)*4 + 1];
                    color.b = imgData[(y*image.width + x)*4 + 2];
                    color.a = imgData[(y*image.width + x)*4 + 3];

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8: {
                    color.r = imgData[(y*image.width + x)*3];
                    color.g = imgData[(y*image.width + x)*3 + 1];
                    color.b = imgData[(y*image.width + x)*3 + 2];
                    color.a = (byte) 255;

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R32: {
                    color.r = (byte) (imgData[y*image.width + x]*255.0f);
                    color.g = 0;
                    color.b = 0;
                    color.a = (byte) 255;

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32: {
                    color.r = (byte) (imgData[(y*image.width + x)*3]*255.0f);
                    color.g = (byte) (imgData[(y*image.width + x)*3 + 1]*255.0f);
                    color.b = (byte) (imgData[(y*image.width + x)*3 + 2]*255.0f);
                    color.a = (byte) 255;

                } break;
                case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32: {
                    color.r = (byte) (imgData[(y*image.width + x)*4]*255.0f);
                    color.g = (byte) (imgData[(y*image.width + x)*4]*255.0f);
                    color.b = (byte) (imgData[(y*image.width + x)*4]*255.0f);
                    color.a = (byte) (imgData[(y*image.width + x)*4]*255.0f);

                } break;
                default:
                    Tracelog(LOG_WARNING, "Compressed image format does not support color reading");
                    break;
            }
        }
        else Tracelog(LOG_WARNING, "Requested image pixel (" + x + ", " + y + ") out of bounds");

        return color;
    }

    //IMAGE DRAWING FUNCTIONS

    void ImageClearBackground(Image dst, Color color) {

        // Security check to avoid program crash
        if ((dst.data == null) || (dst.width == 0) || (dst.height == 0)) {
            return;
        }

        for (int i = 0; i < dst.width * dst.height; ++i) {
            ImageDrawPixel(dst, i % dst.width, i / dst.width, color);
        }
    }

    void ImageDrawPixel(Image dst, int x, int y, Color color) {
        // Security check to avoid program crash
        if ((dst.data == null) || (x < 0) || (x >= dst.getWidth()) || (y < 0) || (y >= dst.getHeight())) {
            return;
        }

        switch (dst.getFormat()) {
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);
                short gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                dst.data.setElem(y * dst.getWidth() + x, gray);

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);
                short gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                dst.data.setElem((y * dst.getWidth() + x) * 2, gray);
                dst.data.setElem(y * dst.getWidth() + x * 2 + 1, color.getA());

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                // NOTE: Calculate R5G6B5 equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);

                int r = (Math.round(coln.x * 31.0f));
                int g = (Math.round(coln.y * 63.0f));
                int b = (Math.round(coln.z * 31.0f));

                dst.data.setElem(y * dst.getWidth() + x, (r << 11 | g << 5 | b));

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f, (float) color.a / 255.0f);

                short r = (byte) (Math.round(coln.x * 31.0f));
                short g = (byte) (Math.round(coln.y * 31.0f));
                short b = (byte) (Math.round(coln.z * 31.0f));
                short a = (byte) ((coln.x > (float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f) ? 1 : 0);

                dst.data.setElem(y * dst.getWidth() + x, (r << 11 | g << 6 | b << 1 | a));

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f, (float) color.a / 255.0f);

                short r = (byte) (Math.round(coln.x * 15.0f));
                short g = (byte) (Math.round(coln.y * 15.0f));
                short b = (byte) (Math.round(coln.z * 15.0f));
                short a = (byte) (Math.round(coln.w * 15.0f));

                dst.data.setElem(y * dst.getWidth() + x, (r << 12 | g << 8 | b << 4 | a));

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                dst.data.setElem((y * dst.getWidth() + x) * 3, color.getR());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 1, color.getG());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 2, color.getB());

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                dst.data.setElem((y * dst.getWidth() + x) * 4, color.getR());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 1, color.getG());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 2, color.getB());
                dst.data.setElem((y * dst.getWidth() + x) * 4 + 3, color.getA());

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32:{
                // NOTE: Calculate grayscale equivalent color (normalized to 32bit)
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);

                dst.data.setElem(y * dst.width + x,
                                 (int) (coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f));

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:{
                // NOTE: Calculate R32G32B32 equivalent color (normalized to 32bit)
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);

                dst.data.setElem((y * dst.getWidth() + x) * 3, (int) coln.getX());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 1, (int) coln.getY());
                dst.data.setElem((y * dst.getWidth() + x) * 3 + 2, (int) coln.getZ());
            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:{
                // NOTE: Calculate R32G32B32A32 equivalent color (normalized to 32bit)
                Vector4 coln = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f, (float) color.a / 255.0f);

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
    void ImageDrawPixelV(Image dst, Vector2 position, Color color) {
        ImageDrawPixel(dst, (int) position.x, (int) position.y, color);
    }

    // Draw line within an image
    void ImageDrawLine(Image dst, int startPosX, int startPosY, int endPosX, int endPosY, Color color) {
        int m = 2 * (endPosY - startPosY);
        int slopeError = m - (endPosX - startPosX);

        for (int x = startPosX, y = startPosY; x <= endPosX; x++) {
            ImageDrawPixel(dst, x, y, color);
            slopeError += m;

            if (slopeError >= 0) {
                y++;
                slopeError -= 2 * (endPosX - startPosX);
            }
        }
    }

    // Draw line within an image (Vector version)
    void ImageDrawLineV(Image dst, Vector2 start, Vector2 end, Color color) {
        ImageDrawLine(dst, (int) start.x, (int) start.y, (int) end.x, (int) end.y, color);
    }

    // Draw circle within an image
    void ImageDrawCircle(Image dst, int centerX, int centerY, int radius, Color color) {
        int x = 0, y = radius;
        int decesionParameter = 3 - 2 * radius;

        while (y >= x) {
            ImageDrawPixel(dst, centerX + x, centerY + y, color);
            ImageDrawPixel(dst, centerX - x, centerY + y, color);
            ImageDrawPixel(dst, centerX + x, centerY - y, color);
            ImageDrawPixel(dst, centerX - x, centerY - y, color);
            ImageDrawPixel(dst, centerX + y, centerY + x, color);
            ImageDrawPixel(dst, centerX - y, centerY + x, color);
            ImageDrawPixel(dst, centerX + y, centerY - x, color);
            ImageDrawPixel(dst, centerX - y, centerY - x, color);
            x++;

            if (decesionParameter > 0) {
                y--;
                decesionParameter = decesionParameter + 4 * (x - y) + 10;
            }
            else{
                decesionParameter = decesionParameter + 4 * x + 6;
            }
        }
    }

    // Draw circle within an image (Vector version)
    void ImageDrawCircleV(Image dst, Vector2 center, int radius, Color color) {
        ImageDrawCircle(dst, (int) center.x, (int) center.y, radius, color);
    }

    // Draw rectangle within an image
    void ImageDrawRectangle(Image dst, int posX, int posY, int width, int height, Color color) {
        ImageDrawRectangleRec(dst, new Rectangle((float) posX, (float) posY, (float) width, (float) height), color);
    }

    // Draw rectangle within an image (Vector version)
    void ImageDrawRectangleV(Image dst, Vector2 position, Vector2 size, Color color) {
        ImageDrawRectangle(dst, (int) position.x, (int) position.y, (int) size.x, (int) size.y, color);
    }

    // Draw rectangle within an image
    void ImageDrawRectangleRec(Image dst, Rectangle rec, Color color) {
        // Security check to avoid program crash
        if ((dst.data == null) || (dst.width == 0) || (dst.height == 0)) {
            return;
        }

        int sy = (int) rec.y;
        int ey = sy + (int) rec.height;

        int sx = (int) rec.x;
        int ex = sx + (int) rec.width;

        for (int y = sy; y < ey; y++) {
            for (int x = sx; x < ex; x++) {
                ImageDrawPixel(dst, x, y, color);
            }
        }
    }

    // Draw rectangle lines within an image
    void ImageDrawRectangleLines(Image dst, Rectangle rec, int thick, Color color) {
        ImageDrawRectangle(dst, (int) rec.x, (int) rec.y, (int) rec.getWidth(), thick, color);
        ImageDrawRectangle(dst, (int) rec.x, (int) (rec.y + thick), thick, (int) (rec.getHeight() - thick * 2), color);
        ImageDrawRectangle(dst, (int) (rec.x + rec.getWidth() - thick), (int) (rec.y + thick), thick,
                           (int) (rec.getHeight() - thick * 2), color);
        ImageDrawRectangle(dst, (int) rec.x, (int) (rec.y + rec.getHeight() - thick), (int) rec.getWidth(), thick,
                           color);
    }

    //This function uses pointers.
    // Draw an image (source) within an image (destination)
    // NOTE: Color tint is applied to source image
    void ImageDraw(Image dst, Image src, Rectangle srcRec, Rectangle dstRec, Color tint) {
        // Security check to avoid program crash
        if ((dst.data == null) || (dst.width == 0) || (dst.height == 0) ||
                (src.data == null) || (src.width == 0) || (src.height == 0)) {
            return;
        }

        if (dst.mipmaps > 1) {
            Tracelog(LOG_WARNING, "Image drawing only applied to base mipmap level");
        }
        if (dst.format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            Tracelog(LOG_WARNING, "Image drawing not supported for compressed formats");
        }
        else{
            Image srcMod = new Image();       // Source copy (in case it was required)
            Image srcPtr = src;              // Pointer to source image
            boolean useSrcMod = false;     // Track source copy required

            // Source rectangle out-of-bounds security checks
            if (srcRec.x < 0) {
                srcRec.setWidth(srcRec.getWidth() + srcRec.getX());
                srcRec.setX(0);
            }
            if (srcRec.y < 0) {
                srcRec.setHeight(srcRec.getHeight() + srcRec.getY());
                srcRec.setY(0);
            }
            if ((srcRec.x + srcRec.getWidth()) > src.width) {
                srcRec.setWidth(src.getWidth() - srcRec.getX());
            }
            if ((srcRec.y + srcRec.getHeight()) > src.getHeight()) {
                srcRec.setHeight(src.getHeight() - srcRec.getY());
            }

            // Check if source rectangle needs to be resized to destination rectangle
            // In that case, we make a copy of source and we apply all required transform
            if (((int) srcRec.getWidth() != (int) dstRec.getWidth()) || ((int) srcRec.getHeight() != (int) dstRec.getHeight())) {
                srcMod = ImageFromImage(src, srcRec);   // Create image from another image
                ImageResize(srcMod, (int) dstRec.getWidth(), (int) dstRec.getHeight());   // Resize to destination rectangle
                srcRec = new Rectangle(0, 0, (float) srcMod.width, (float) srcMod.height);

                srcPtr = srcMod;
                useSrcMod = true;
            }

            // Destination rectangle out-of-bounds security checks
            if (dstRec.x < 0) {
                srcRec.setX(-dstRec.getX());
                srcRec.setWidth(srcRec.getWidth() + dstRec.getX());
                dstRec.setX(0);
            }
            else if ((dstRec.x + srcRec.getWidth()) > dst.width) {
                srcRec.setWidth(dst.getWidth() - dstRec.getX());
            }

            if (dstRec.y < 0) {
                srcRec.setY(-dstRec.getY());
                srcRec.setHeight(srcRec.getHeight() + dstRec.getY());
                dstRec.setY(0);
            }
            else if ((dstRec.y + srcRec.getHeight()) > dst.getHeight()) {
                srcRec.setHeight(dst.getHeight() - dstRec.getY());
            }

            if (dst.getWidth() < srcRec.getWidth()) {
                srcRec.setWidth((float) dst.getWidth());
            }
            if (dst.getHeight() < srcRec.getHeight()) {
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
            boolean blendRequired = (tint.a != 255) || ((srcPtr.getFormat() != RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) &&
                    (srcPtr.getFormat() != RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8) && (srcPtr.getFormat() != RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5));

            // Fast path: Avoid blend if source has no alpha to blend

            int strideDst = GetPixelDataSize(dst.width, 1, dst.format);
            int bytesPerPixelDst = strideDst / (dst.width);

            int strideSrc = GetPixelDataSize(srcPtr.width, 1, srcPtr.format);
            int bytesPerPixelSrc = strideSrc / (srcPtr.width);

            byte[] pSrcBase = srcPtr.getData();
            byte[] pDstBase = dst.getData();

            for (int y = 0; y < (int) srcRec.getHeight(); y++) {
                byte[] pSrc = pSrcBase;
                byte[] pDst = pDstBase;

                // Fast path: Avoid moving pixel by pixel if no blend required and same format
                if (!blendRequired && (srcPtr.format == dst.format)) {
                    //memcpy(pDst, pSrc, (int) (srcRec.getWidth()) * bytesPerPixelSrc);
                    pDst = pSrc;
                }
                else{
                    for (int x = 0; x < (int) srcRec.getWidth(); x++) {
                        colSrc = GetPixelColor(pSrc, srcPtr.getFormat());
                        colDst = GetPixelColor(pDst, dst.getFormat());

                        // Fast path: Avoid blend if source has no alpha to blend
                        if (blendRequired) {
                            blend = ColorAlphaBlend(colDst, colSrc, tint);
                        }
                        else{
                            blend = colSrc;
                        }

                        SetPixelColor(pDst, blend, dst.getFormat());
                    }
                }

            }

            if (useSrcMod) UnloadImage(srcMod);     // Unload source modified image
        }
    }

    // Draw text (default font) within an image (destination)
    public void ImageDrawText(Image dst, String text, int posX, int posY, int fontSize, Color color) {
        Vector2 position = new Vector2((float) posX, (float) posY);

        // NOTE: For default font, sapcing is set to desired font size / default font size (10)
        ImageDrawTextEx(dst, context.text.GetFontDefault(), text, position, (float) fontSize, (float) fontSize / 10, color);
    }

    // Draw text (custom sprite font) within an image (destination)
    public void ImageDrawTextEx(Image dst, Font font, String text, Vector2 position, float fontSize, float spacing, Color tint) {
        Image imText = ImageTextEx(font, text, fontSize, spacing, tint);

        Rectangle srcRec = new Rectangle(0.0f, 0.0f, (float) imText.width, (float) imText.height);
        Rectangle dstRec = new Rectangle(position.x, position.y, (float) imText.width, (float) imText.height);

        ImageDraw(dst, imText, srcRec, dstRec, Color.WHITE);

        UnloadImage(imText);
    }

    public Texture2D LoadTexture(String fileName) {
        Texture2D texture = new Texture2D();
        Image image = LoadImage(fileName);

        if (image.data != null) {
            texture = LoadTextureFromImage(image);
            UnloadImage(image);
        }

        return texture;
    }

    public Texture2D LoadTextureFromImage(Image image) {
        Texture2D texture = new Texture2D();

        if ((image.data != null) && (image.width != 0) && (image.height != 0)) {
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

    // Load cubemap from image, multiple image cubemap layouts supported
    public TextureCubemap LoadTextureCubemap(Image image, int layoutType) {
        TextureCubemap cubemap = new TextureCubemap();
        if (layoutType == CUBEMAP_AUTO_DETECT)      // Try to automatically guess layout type
        {
            // Check image width/height to determine the type of cubemap provided
            if (image.width > image.height) {
                if ((image.width / 6) == image.height) {
                    layoutType = CUBEMAP_LINE_HORIZONTAL;
                    cubemap.width = image.width / 6;
                }
                else if ((image.width / 4) == (image.height / 3)) {
                    layoutType = CUBEMAP_CROSS_FOUR_BY_THREE;
                    cubemap.width = image.width / 4;
                }
                else if (image.width >= (int) ((float) image.height * 1.85f)) {
                    layoutType = CUBEMAP_PANORAMA;
                    cubemap.width = image.width / 4;
                }
            }
            else if (image.height > image.width) {
                if ((image.height / 6) == image.width) {
                    layoutType = CUBEMAP_LINE_VERTICAL;
                    cubemap.width = image.height / 6;
                }
                else if ((image.width / 3) == (image.height / 4)) {
                    layoutType = CUBEMAP_CROSS_THREE_BY_FOUR;
                    cubemap.width = image.width / 3;
                }
            }

            cubemap.height = cubemap.width;
        }

        if (layoutType != CUBEMAP_AUTO_DETECT) {
            int size = cubemap.width;

            Image faces = new Image();                // Vertical column image
            Rectangle[] faceRecs = new Rectangle[6];      // Face source rectangles
            for (int i = 0; i < 6; i++) faceRecs[i] = new Rectangle(0, 0, (float) size, (float) size);

            if (layoutType == CUBEMAP_LINE_VERTICAL) {
                faces = image;
                for (int i = 0; i < 6; i++) faceRecs[i].y = (float) size * i;
            }
            else if (layoutType == CUBEMAP_PANORAMA) {
                // TODO: Convert panorama image to square faces...
                // Ref: https://github.com/denivip/panorama/blob/master/panorama.cpp
            }
            else{
                if (layoutType == CUBEMAP_LINE_HORIZONTAL) {
                    for (int i = 0; i < 6; i++) faceRecs[i].x = (float) size * i;
                }
                else if (layoutType == CUBEMAP_CROSS_THREE_BY_FOUR) {
                    faceRecs[0].x = (float) size;
                    faceRecs[0].y = (float) size;
                    faceRecs[1].x = (float) size;
                    faceRecs[1].y = (float) size * 3;
                    faceRecs[2].x = (float) size;
                    faceRecs[2].y = 0;
                    faceRecs[3].x = (float) size;
                    faceRecs[3].y = (float) size * 2;
                    faceRecs[4].x = 0;
                    faceRecs[4].y = (float) size;
                    faceRecs[5].x = (float) size * 2;
                    faceRecs[5].y = (float) size;
                }
                else if (layoutType == CubemapLayoutType.CUBEMAP_CROSS_FOUR_BY_THREE) {
                    faceRecs[0].x = (float) size * 2;
                    faceRecs[0].y = (float) size;
                    faceRecs[1].x = 0;
                    faceRecs[1].y = (float) size;
                    faceRecs[2].x = (float) size;
                    faceRecs[2].y = 0;
                    faceRecs[3].x = (float) size;
                    faceRecs[3].y = (float) size * 2;
                    faceRecs[4].x = (float) size;
                    faceRecs[4].y = (float) size;
                    faceRecs[5].x = (float) size * 3;
                    faceRecs[5].y = (float) size;
                }

                // Convert image data to 6 faces in a vertical column, that's the optimum layout for loading
                faces = GenImageColor(size, size * 6, Color.MAGENTA);
                ImageFormat(faces, image.format);

                // TODO: Image formating does not work with compressed textures!
            }

            for (int i = 0; i < 6; i++) {
                ImageDraw(faces, image, faceRecs[i], new Rectangle(0, (float) size * i, (float) size, (float) size),
                          Color.WHITE);
            }

            cubemap.id = RLGL.rlLoadTextureCubemap(faces.getData(), size, faces.format);
            if (cubemap.id == 0) {
                Tracelog(LOG_WARNING, "IMAGE: Failed to load cubemap image");
            }

            UnloadImage(faces);
        }
        else{
            Tracelog(LOG_WARNING, "IMAGE: Failed to detect cubemap image layout");
        }

        return cubemap;
    }

    // Load texture for rendering (framebuffer)
    // NOTE: Render texture is loaded by default with RGBA color attachment and depth RenderBuffer
    public RenderTexture LoadRenderTexture(int width, int height) {
        RenderTexture target = new RenderTexture();

        target.id = rlLoadFramebuffer(width, height);   // Load an empty framebuffer

        if (target.id > 0) {
            rlEnableFramebuffer(target.id);

            // Create color texture (default to RGBA)
            target.texture.id = rlLoadTexture(null, width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
            target.texture.width = width;
            target.texture.height = height;
            target.texture.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
            target.texture.mipmaps = 1;

            // Create depth renderbuffer/texture
            target.depth.id = RLGL.rlLoadTextureDepth(width, height, true);
            target.depth.width = width;
            target.depth.height = height;
            target.depth.format = 19;       //DEPTH_COMPONENT_24BIT?
            target.depth.mipmaps = 1;

            // Attach color texture and depth renderbuffer/texture to FBO
            RLGL.rlFramebufferAttach(target.id, target.texture.id, RL_ATTACHMENT_COLOR_CHANNEL0,
                                     RL_ATTACHMENT_TEXTURE2D);
            RLGL.rlFramebufferAttach(target.id, target.depth.id, RL_ATTACHMENT_DEPTH, RL_ATTACHMENT_RENDERBUFFER);

            // Check if fbo is complete with attachments (valid)
            if (RLGL.rlFramebufferComplete(target.id)) {
                Tracelog(LOG_INFO, "FBO: [ID " + target.id + "] Framebuffer object created successfully");
            }

            rlDisableFramebuffer();
        }
        else{
            Tracelog(LOG_WARNING, "FBO: Framebuffer object can not be created");
        }

        return target;
    }

    public void UnloadTexture(Texture2D texture) {
        if (texture.getId() > 0) {
            RLGL.rlUnloadTexture(texture.getId());

            Tracelog(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Unloaded texture data from VRAM (GPU)");
        }
    }

    //Unload render texture from GPU memory (VRAM)
    public void UnloadRenderTexture(RenderTexture target) {
        if (target.getId() > 0) {
            // Color texture attached to FBO is deleted
            rlUnloadTexture(target.getTexture().getId());

            // NOTE: Depth texture/renderbuffer is automatically
            // queried and deleted before deleting framebuffer
            rlUnloadFramebuffer(target.getId());
        }
    }

    // Update GPU texture with new data
    // NOTE: pixels data must match texture.format
    public void UpdateTexture(Texture2D texture, Color[] pixels) {
        byte[] arri = new byte[pixels.length*4];
        int g = 0;

        for (int i = 0; g < arri.length; i++) {
            arri[g] = (byte) pixels[i].r;
            arri[g + 1] = (byte) pixels[i].g;
            arri[g + 2] = (byte) pixels[i].b;
            arri[g + 3] = (byte) pixels[i].a;
            g += 4;
        }

        rlUpdateTexture(texture.id, 0, 0, texture.width, texture.height, texture.format, arri);
    }

    // Update GPU texture rectangle with new data
    // NOTE: pixels data must match texture.format
    public void UpdateTextureRec(Texture2D texture, Rectangle rec, byte[] pixels) {
        rlUpdateTexture(texture.id, (int) rec.x, (int) rec.y, (int) rec.getWidth(), (int) rec.getHeight(),
                        texture.format, pixels);
    }

    // Get pixel data from GPU texture and return an Image
    // NOTE: Compressed texture formats not supported
    public Image LoadImageFromTexture(Texture2D texture) {
        Image image = new Image();

        if (texture.format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) {
            image.setData(rlReadTexturePixels(texture.id, texture.width, texture.height, texture.format));

            if (image.data != null) {
                image.width = texture.width;
                image.height = texture.height;
                image.format = texture.format;
                image.mipmaps = 1;

                if (RLGL.rlGetVersion() == rlGlVersion.OPENGL_ES_20) {
                    // NOTE: Data retrieved on OpenGL ES 2.0 should be RGBA,
                    // coming from FBO color buffer attachment, but it seems
                    // original texture format is retrieved on RPI...
                    image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
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
    public Image LoadImageFromScreen() {
        Image image = new Image();

        image.width = context.core.GetScreenWidth();
        image.height = context.core.GetScreenHeight();
        image.mipmaps = 1;
        image.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
        image.setData(rlReadScreenPixels(image.width, image.height));

        return image;
    }

    //------------------------------------------------------------------------------------
    // Texture configuration functions
    //------------------------------------------------------------------------------------
    // Generate GPU mipmaps for a texture
    public void GenTextureMipmaps(Texture2D texture) {
        // NOTE: NPOT textures support check inside function
        // On WebGL (OpenGL ES 2.0) NPOT textures support is limited
        rlGenTextureMipmaps(texture);
    }

    // Set texture scaling filter mode
    public void SetTextureFilter(Texture2D texture, int filterMode) {
        switch (filterMode) {

            case 0:{
                if (texture.mipmaps > 1) {
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
                if (texture.mipmaps > 1) {
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
                if (texture.mipmaps > 1) {
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
    public void SetTextureWrap(Texture2D texture, int wrapMode) {
        switch (wrapMode) {
            case RL_TEXTURE_WRAP_REPEAT:{
                // NOTE: It only works if NPOT textures are supported, i.e. OpenGL ES 2.0 could not support it
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
    public void DrawTexture(Texture2D texture, int posX, int posY, Color tint) {
        DrawTextureEx(texture, new Vector2((float) posX, (float) posY), 0.0f, 1.0f, tint);
    }

    // Draw a Texture2D with position defined as Vector2
    public void DrawTextureV(Texture2D texture, Vector2 position, Color tint) {
        DrawTextureEx(texture, position, 0, 1.0f, tint);
    }

    // Draw a Texture2D with extended parameters
    public void DrawTextureEx(Texture2D texture, Vector2 position, float rotation, float scale, Color tint) {
        Rectangle source = new Rectangle(0.0f, 0.0f, (float) texture.width, (float) texture.height);
        Rectangle dest = new Rectangle(position.x, position.y, (float) texture.width * scale,
                                       (float) texture.height * scale);
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, dest, origin, rotation, tint);
    }

    // Draw a part of a texture (defined by a rectangle)
    public void DrawTextureRec(Texture2D texture, Rectangle source, Vector2 position, Color tint) {
        Rectangle dest = new Rectangle(position.x, position.y, Math.abs(source.getWidth()),
                                       Math.abs(source.getHeight()));
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, dest, origin, 0.0f, tint);
    }

    // Draw texture quad with tiling and offset parameters
    // NOTE: Tiling and offset should be provided considering normalized texture values [0..1]
    // i.e tiling = { 1.0f, 1.0f } refers to all texture, offset = { 0.5f, 0.5f } moves texture origin to center
    public void DrawTextureQuad(Texture2D texture, Vector2 tiling, Vector2 offset, Rectangle quad, Color tint) {
        // WARNING: This solution only works if TEXTURE_WRAP_REPEAT is supported,
        // NPOT textures supported is required and OpenGL ES 2.0 could not support it
        Rectangle source = new Rectangle(offset.x * texture.getWidth(), offset.y * texture.getHeight(),
                                         tiling.x * texture.getWidth(), tiling.y * texture.getHeight());
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, quad, origin, 0.0f, tint);
    }

    // Draw part of a texture (defined by a rectangle) with rotation and scale tiled into dest.
    // NOTE: For tilling a whole texture DrawTextureQuad() is better
    public void DrawTextureTiled(Texture2D texture, Rectangle source, Rectangle dest, Vector2 origin, float rotation, float scale, Color tint) {
        if ((texture.id <= 0) || (scale <= 0.0f)) return;  // Wanna see an infinite loop?!...just delete this line!
        if ((source.width == 0) || (source.height == 0)) return;

        int tileWidth = (int) (source.width * scale), tileHeight = (int) (source.height * scale);
        if ((dest.width < tileWidth) && (dest.height < tileHeight)) {
            // Can fit only one tile
            DrawTexturePro(texture,
                           new Rectangle(source.x, source.y, (dest.width / tileWidth) * source.width, (dest.height / tileHeight) * source.height),
                           new Rectangle(dest.x, dest.y, dest.width, dest.height), origin, rotation, tint);
        }
        else if (dest.width <= tileWidth) {
            // Tiled vertically (one column)
            int dy = 0;
            for (; dy + tileHeight < dest.height; dy += tileHeight) {
                DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, (dest.width / tileWidth) * source.width, source.height),
                               new Rectangle(dest.x, dest.y + dy, dest.width, (float) tileHeight), origin, rotation, tint);
            }

            // Fit last tile
            if (dy < dest.height) {
                DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, (dest.width / tileWidth) * source.width, ((dest.height - dy) / tileHeight) * source.height),
                               new Rectangle(dest.x, dest.y + dy, dest.width, dest.height - dy), origin, rotation, tint);
            }
        }
        else if (dest.height <= tileHeight) {
            // Tiled horizontally (one row)
            int dx = 0;
            for (; dx + tileWidth < dest.width; dx += tileWidth) {
                DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, source.width, (dest.height / tileHeight) * source.height),
                               new Rectangle(dest.x + dx, dest.y, (float) tileWidth, dest.height), origin, rotation, tint);
            }

            // Fit last tile
            if (dx < dest.width) {
                DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, ((dest.width - dx) / tileWidth) * source.width,
                                             (dest.height / tileHeight) * source.height),
                               new Rectangle(dest.x + dx, dest.y, dest.width - dx, dest.height), origin, rotation, tint);
            }
        }
        else{
            // Tiled both horizontally and vertically (rows and columns)
            int dx = 0;
            for (; dx + tileWidth < dest.width; dx += tileWidth) {
                int dy = 0;
                for (; dy + tileHeight < dest.height; dy += tileHeight) {
                    DrawTexturePro(texture, source,
                                   new Rectangle(dest.x + dx, dest.y + dy, (float) tileWidth, (float) tileHeight),
                                   origin, rotation, tint);
                }

                if (dy < dest.height) {
                    DrawTexturePro(texture,
                                   new Rectangle(source.x, source.y, source.width, ((dest.height - dy) / tileHeight) * source.height),
                                   new Rectangle(dest.x + dx, dest.y + dy, (float) tileWidth, dest.height - dy),
                                   origin, rotation, tint);
                }
            }

            // Fit last column of tiles
            if (dx < dest.width) {
                int dy = 0;
                for (; dy + tileHeight < dest.height; dy += tileHeight) {
                    DrawTexturePro(texture,
                                   new Rectangle(source.x, source.y, ((dest.width - dx) / tileWidth) * source.width, source.height),
                                   new Rectangle(dest.x + dx, dest.y + dy, dest.width - dx, (float) tileHeight),
                                   origin, rotation, tint);
                }

                // Draw final tile in the bottom right corner
                if (dy < dest.height) {
                    DrawTexturePro(texture,
                                   new Rectangle(source.x, source.y, ((dest.width - dx) / tileWidth) * source.width,
                                                 ((dest.height - dy) / tileHeight) * source.height),
                                   new Rectangle(dest.x + dx, dest.y + dy, dest.width - dx, dest.height - dy),
                                   origin, rotation, tint);
                }
            }
        }
    }

    // Draw a part of a texture (defined by a rectangle) with 'pro' parameters
    // NOTE: origin is relative to destination rectangle size
    public void DrawTexturePro(Texture2D texture, Rectangle source, Rectangle dest, Vector2 origin,
                                      float rotation, Color tint) {
        // Check if texture is valid
        if (texture.id > 0) {
            float width = (float) texture.width;
            float height = (float) texture.height;

            boolean flipX = false;

            if (source.width < 0) {
                flipX = true;
                source.width *= -1;
            }
            if (source.height < 0) source.y -= source.height;

            Vector2 topLeft = new Vector2();
            Vector2 topRight = new Vector2();
            Vector2 bottomLeft = new Vector2();
            Vector2 bottomRight = new Vector2();

            // Only calculate rotation if needed
            if (rotation == 0.0f) {
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
            if (flipX) {
                rlTexCoord2f((source.x + source.width) / width, source.y / height);
            }
            else{
                rlTexCoord2f(source.x / width, source.y / height);
            }
            rlVertex2f(topLeft.x, topLeft.y);

            // Bottom-left corner for texture and quad
            if (flipX) {
                rlTexCoord2f((source.x + source.width) / width, (source.y + source.height) / height);
            }
            else{
                rlTexCoord2f(source.x / width, (source.y + source.height) / height);
            }
            rlVertex2f(bottomLeft.x, bottomLeft.y);

            // Bottom-right corner for texture and quad
            if (flipX) {
                rlTexCoord2f(source.x / width, (source.y + source.height) / height);
            }
            else{
                rlTexCoord2f((source.x + source.width) / width, (source.y + source.height) / height);
            }
            rlVertex2f(bottomRight.x, bottomRight.y);

            // Top-right corner for texture and quad
            if (flipX) {
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
    public void DrawTextureNPatch(Texture2D texture, NPatchInfo nPatchInfo, Rectangle dest, Vector2 origin, float rotation, Color tint) {
        if (texture.id > 0) {
            float width = (float) texture.width;
            float height = (float) texture.height;

            float patchWidth = Math.max(dest.getWidth(), 0.0f);
            float patchHeight = Math.max(dest.getHeight(), 0.0f);

            if (nPatchInfo.source.getWidth() < 0) {
                nPatchInfo.source.x -= nPatchInfo.source.getWidth();
            }
            if (nPatchInfo.source.getHeight() < 0) {
                nPatchInfo.source.y -= nPatchInfo.source.getHeight();
            }
            if (nPatchInfo.getType() == NPATCH_THREE_PATCH_HORIZONTAL) {
                patchHeight = nPatchInfo.source.getHeight();
            }
            if (nPatchInfo.getType() == NPATCH_THREE_PATCH_VERTICAL) {
                patchWidth = nPatchInfo.source.getWidth();
            }

            boolean drawCenter = true;
            boolean drawMiddle = true;
            float leftBorder = (float) nPatchInfo.left;
            float topBorder = (float) nPatchInfo.top;
            float rightBorder = (float) nPatchInfo.right;
            float bottomBorder = (float) nPatchInfo.bottom;

            // adjust the lateral (left and right) border widths in case patchWidth < texture.width
            if (patchWidth <= (leftBorder + rightBorder) && nPatchInfo.type != NPATCH_THREE_PATCH_VERTICAL) {
                drawCenter = false;
                leftBorder = (leftBorder / (leftBorder + rightBorder)) * patchWidth;
                rightBorder = patchWidth - leftBorder;
            }
            // adjust the lateral (top and bottom) border heights in case patchHeight < texture.height
            if (patchHeight <= (topBorder + bottomBorder) && nPatchInfo.type != NPATCH_THREE_PATCH_HORIZONTAL) {
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

            rlCheckRenderBatchLimit(9 * 3 * 2);         // Maxium number of verts that could happen

            RLGL.rlSetTexture(texture.id);

            rlPushMatrix();
            rlTranslatef(dest.x, dest.y, 0.0f);
            rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
            rlTranslatef(-origin.x, -origin.y, 0.0f);

            rlBegin(RL_QUADS);
            rlColor4ub(tint.r, tint.g, tint.b, tint.a);
            rlNormal3f(0.0f, 0.0f, 1.0f);               // Normal vector pointing towards viewer

            if (nPatchInfo.type == NPATCH_NINE_PATCH) {
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
                if (drawCenter) {
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
                if (drawMiddle) {
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
                    if (drawCenter) {
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
                if (drawCenter) {
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
            else if (nPatchInfo.type == NPATCH_THREE_PATCH_VERTICAL) {
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
                if (drawCenter) {
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
            else if (nPatchInfo.type == NPATCH_THREE_PATCH_HORIZONTAL) {
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
                if (drawCenter) {
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
    public void DrawTexturePoly(Texture2D texture, Vector2 center, Vector2[] points, Vector2[] texcoords, int pointsCount,
                                Color tint) {
        RLGL.rlCheckRenderBatchLimit((pointsCount - 1) * 4);

        RLGL.rlSetTexture(texture.id);

        // Texturing is only supported on RL_QUADs
        rlBegin(RL_QUADS);

        rlColor4ub(tint.r, tint.g, tint.b, tint.a);

        for (int i = 0; i < pointsCount - 1; i++) {
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
    public Color Fade(Color color, float alpha) {
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        else if (alpha > 1.0f) {
            alpha = 1.0f;
        }

        return new Color(color.r, color.g, color.b, (int) (255.0f * alpha));
    }

    // Returns hexadecimal value for a Color
    public int ColorToInt(Color color) {
        return ((color.r << 24) | (color.g << 16) | (color.b << 8) | color.getA());
    }

    // Returns color normalized as float [0..1]
    public Vector4 ColorNormalize(Color color) {
        return new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f,
                           (float) color.b / 255.0f, (float) color.a / 255.0f);
    }

    // Returns color from normalized values [0..1]
    public Color ColorFromNormalized(Vector4 normalized) {
        return new Color((int) normalized.x * 255, (int) normalized.y * 255, (int) normalized.z * 255, (int) normalized.z * 255);
    }

    // Returns HSV values for a Color
    // NOTE: Hue is returned as degrees [0..360]
    public Vector3 ColorToHSV(Color color) {
        Vector3 hsv = new Vector3();
        Vector3 rgb = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);
        float min, max, delta;

        min = Math.min(rgb.x, rgb.y);
        min = Math.min(min, rgb.z);

        max = Math.max(rgb.x, rgb.y);
        max = Math.max(max, rgb.z);

        hsv.z = max;            // Value
        delta = max - min;

        if (delta < 0.00001f) {
            hsv.y = 0.0f;
            hsv.x = 0.0f;       // Undefined, maybe NAN?
            return hsv;
        }

        if (max > 0.0f) {
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
        if (rgb.x >= max) {
            hsv.x = (rgb.y - rgb.z) / delta;    // Between yellow & magenta
        }
        else{
            if (rgb.y >= max) {
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
    public Color ColorFromHSV(float hue, float saturation, float value) {
        Color color = new Color(0, 0, 0, 255);

        // Red channel
        float k = (5.0f + hue / 60.0f) % 6;
        float t = 4.0f - k;
        k = Math.min(t, k);
        k = (k < 1) ? k : 1;
        k = (k > 0) ? k : 0;
        color.r = (byte) ((value - value * saturation * k) * 255.0f);

        // Green channel
        k = (3.0f + hue / 60.0f) % 6;
        t = 4.0f - k;
        k = Math.min(t, k);
        k = (k < 1) ? k : 1;
        k = (k > 0) ? k : 0;
        color.g = (byte) ((value - value * saturation * k) * 255.0f);

        // Blue channel
        k = (1.0f + hue / 60.0f) % 6;
        t = 4.0f - k;
        k = Math.min(t, k);
        k = (k < 1) ? k : 1;
        k = (k > 0) ? k : 0;
        color.b = (byte) ((value - value * saturation * k) * 255.0f);

        return color;
    }

    // Returns color with alpha applied, alpha goes from 0.0f to 1.0f
    public Color ColorAlpha(Color color, float alpha) {
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        else if (alpha > 1.0f) {
            alpha = 1.0f;
        }

        return new Color(color.r, color.g, color.b, (int) (255.0f * alpha));
    }

    // Returns src alpha-blended into dst color with tint
    public Color ColorAlphaBlend(Color dst, Color src, Color tint) {
        Color out = Color.WHITE;

        // Apply color tint to source color
        src.setR((byte) ((src.r * tint.r + 1) >> 8));
        src.setG((byte) ((src.g * tint.g + 1) >> 8));
        src.setB((byte) ((src.b * tint.b + 1) >> 8));
        src.setA((byte) ((src.a * tint.a + 1) >> 8));

        boolean COLORALPHABLEND_FLOAT = false;
        boolean COLORALPHABLEND_INTEGERS = true;
        if (COLORALPHABLEND_INTEGERS) {
            if (src.a == 0) {
                out = dst;
            }
            else if (src.a == 255) {
                out = src;
            }
            else{
                int alpha = src.a + 1;
                // We are shifting by 8 (dividing by 256), so we need to take that excess into account

                out.setA((byte) ((alpha * 256 + dst.a * (256 - alpha)) >> 8));

                if (out.a > 0) {
                    out.setR((byte) (((src.r * alpha * 256 + dst.r * dst.a * (256 - alpha)) / out.getA()) >> 8));
                    out.setG((byte) (((src.g * alpha * 256 + dst.g * dst.a * (256 - alpha)) / out.getA()) >> 8));
                    out.setB((byte) (((src.b * alpha * 256 + dst.b * dst.a * (256 - alpha)) / out.getA()) >> 8));
                }
            }
        }
        if (COLORALPHABLEND_FLOAT) {
            if (src.a == 0) {
                out = dst;
            }
            else if (src.a == 255) {
                out = src;
            }
            else{
                Vector4 fdst = ColorNormalize(dst);
                Vector4 fsrc = ColorNormalize(src);
                Vector4 ftint = ColorNormalize(tint);
                Vector4 fout = new Vector4();

                fout.setW(fsrc.w + fdst.w * (1.0f - fsrc.getW()));

                if (fout.w > 0.0f) {
                    fout.setX(fsrc.x * fsrc.w + fdst.x * fdst.w * (1 - fsrc.getW()) / fout.getW());
                    fout.setY(fsrc.y * fsrc.w + fdst.y * fdst.w * (1 - fsrc.getW()) / fout.getW());
                    fout.setZ(fsrc.z * fsrc.w + fdst.z * fdst.w * (1 - fsrc.getW()) / fout.getW());
                }

                out = new Color((int) (fout.x * 255.0f), (int) (fout.y * 255.0f),
                                (int) (fout.z * 255.0f), (int) (fout.w * 255.0f));
            }
        }

        return out;
    }

    // Returns a Color struct from hexadecimal value
    public Color GetColor(int hexValue) {
        return new Color((hexValue >> 24) & 0xFF, (hexValue >> 16) & 0xFF, (hexValue >> 8) & 0xFF, hexValue & 0xFF);
    }

    // Get color from a pixel from certain format
    public Color GetPixelColor(byte[] srcPtr, int format) {
        Color color = new Color();

        switch (format) {
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                color = new Color(srcPtr[0], srcPtr[0], srcPtr[0], 255);
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                color = new Color(srcPtr[0], srcPtr[0], srcPtr[0], srcPtr[1]);
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                color.setR((byte) ((srcPtr[0] >> 11) * 255 / 31));
                color.setG((byte) (((srcPtr[0] >> 5) & 0b0000000000111111) * 255 / 63));
                color.setB((byte) ((srcPtr[0] & 0b0000000000011111) * 255 / 31));
                color.setA((byte) 255);

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                color.setR((byte) ((srcPtr[0] >> 11) * 255 / 31));
                color.setG((byte) (((srcPtr[0] >> 6) & 0b0000000000011111) * 255 / 31));
                color.setB((byte) ((srcPtr[0] & 0b0000000000011111) * 255 / 31));
                color.setA((byte) ((srcPtr[0] & 0b0000000000000001) == 1 ? 255 : 0));

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                color.setR((byte) ((srcPtr[0] >> 11) * 255 / 15));
                color.setG((byte) (((srcPtr[0] >> 8) & 0b0000000000001111) * 255 / 15));
                color.setB((byte) (((srcPtr[0] >> 4) & 0b0000000000001111) * 255 / 15));
                color.setA((byte) ((srcPtr[0] & 0b0000000000001111) * 255 / 15));

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                color = new Color(srcPtr[0], srcPtr[1], srcPtr[2], srcPtr[3]);
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                color = new Color(srcPtr[0], srcPtr[1], srcPtr[2], 255);
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32:
            {
                // NOTE: Pixel normalized float value is converted to [0..255]
                color.r = (int) (srcPtr[0]*255.0f);
                color.g = (int) (srcPtr[0]*255.0f);
                color.b = (int) (srcPtr[0]*255.0f);
                color.a = 255;

            } break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:
            {
                // NOTE: Pixel normalized float value is converted to [0..255]
                color.r = (int)(srcPtr[0]*255.0f);
                color.g = (int)(srcPtr[1]*255.0f);
                color.b = (int)(srcPtr[2]*255.0f);
                color.a = 255;

            } break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
            {
                // NOTE: Pixel normalized float value is converted to [0..255]
                color.r = (int)(srcPtr[0]*255.0f);
                color.g = (int)(srcPtr[1]*255.0f);
                color.b = (int)(srcPtr[2]*255.0f);
                color.a = (int)(srcPtr[3]*255.0f);

            } break;
            default:
                break;
        }

        return color;
    }

    //Set pixel color formatted into destination pointer
    public byte[] SetPixelColor(byte[] dstPtr, Color color, int format) {
        byte[] result = new byte[dstPtr.length];
        switch (format) {
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);
                byte gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                result[0] = gray;

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:{
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);
                byte gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                result[0] = gray;
                result[1] = (byte) color.a;

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:{
                // NOTE: Calculate R5G6B5 equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);

                char r = (char) (Math.round(coln.x * 31.0f));
                char g = (char) (Math.round(coln.y * 63.0f));
                char b = (char) (Math.round(coln.z * 31.0f));

                result[0] = (byte) (r << 11 | g << 5 | b);

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f, (float) color.a / 255.0f);

                byte r = (byte) (Math.round(coln.x * 31.0f));
                byte g = (byte) (Math.round(coln.y * 31.0f));
                byte b = (byte) (Math.round(coln.z * 31.0f));
                byte a = (byte) ((coln.w > ((float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f)) ? 1 : 0);


                result[0] = (byte) (r << 11 | g << 6 | b << 1 | a);

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:{
                // NOTE: Calculate R5G5B5A1 equivalent color
                Vector4 coln = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f,
                                           (float) color.a / 255.0f);

                byte r = (byte) (Math.round(coln.x * 15.0f));
                byte g = (byte) (Math.round(coln.y * 15.0f));
                byte b = (byte) (Math.round(coln.z * 15.0f));
                byte a = (byte) (Math.round(coln.w * 15.0f));

                result[0] = (byte) (r << 12 | g << 8 | b << 4 | a);

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:{
                result[0] = (byte) color.r;
                result[1] = (byte) color.g;
                result[2] = (byte) color.b;

            }
            break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:{
                result[0] = (byte) color.r;
                result[1] = (byte) color.g;
                result[2] = (byte) color.b;
                result[3] = (byte) color.a;

            }
            break;
            default:
                break;
        }
        return result;
    }

    // Get pixel data size in bytes for certain format
    // NOTE: Size can be requested for Image or Texture data
    public int GetPixelDataSize(int width, int height, int format) {
        int dataSize;       // Size in bytes
        int bpp = 0;            // Bits per pixel

        switch (format) {
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                bpp = 8;
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
            case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                bpp = 16;
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                bpp = 32;
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                bpp = 24;
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32:
                bpp = 32;
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                bpp = 32 * 3;
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                bpp = 32 * 4;
                break;
            case RL_PIXELFORMAT_COMPRESSED_DXT1_RGB:
            case RL_PIXELFORMAT_COMPRESSED_DXT1_RGBA:
            case RL_PIXELFORMAT_COMPRESSED_ETC1_RGB:
            case RL_PIXELFORMAT_COMPRESSED_ETC2_RGB:
            case RL_PIXELFORMAT_COMPRESSED_PVRT_RGB:
            case RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA:
                bpp = 4;
                break;
            case RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA:
            case RL_PIXELFORMAT_COMPRESSED_DXT5_RGBA:
            case RL_PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA:
            case RL_PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA:
                bpp = 8;
                break;
            case RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA:
                bpp = 2;
                break;
            default:
                break;
        }

        dataSize = width * height * bpp / 8;  // Total data size in bytes

        // Most compressed formats works on 4x4 blocks,
        // if texture is smaller, minimum dataSize is 8 or 16
        if ((width < 4) && (height < 4)) {
            if ((format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) && (format < RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA)) {
                dataSize = 8;
            }
            else if ((format >= RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA) && (format < RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA)) {
                dataSize = 16;
            }
        }

        return dataSize;
    }

    //Load specific file formats
    //LoadDDS
    //LoadPKM
    //LoadKTX
    //SaveKTX
    //LoadPVR
    //LoadASTC
}
