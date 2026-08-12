package com.raylib.java.textures;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.raylib.java.Raylib;
import com.raylib.java.structs.*;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Rectangle;
import org.jetbrains.annotations.Contract;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import static com.raylib.java.Config.*;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_INFO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.ATTACHMENT_RENDERBUFFER;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.ATTACHMENT_TEXTURE2D;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.ATTACHMENT_COLOR_CHANNEL0;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.ATTACHMENT_DEPTH;
import static com.raylib.java.rlgl.RLGL.rlGlVersion.OPENGL_ES_20;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.structs.NPatchInfo.NPatchType.*;
import static com.raylib.java.textures.rTextures.CubemapLayoutType.*;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8_linear;
import static org.lwjgl.stb.STBPerlin.stb_perlin_fbm_noise3;

public class rTextures {

    /**********************************************************************************************
     *
     *   rtextures - Basic functions to load and draw textures
     *
     *   CONFIGURATION:
     *       #define SUPPORT_MODULE_RTEXTURES
     *           rtextures module is included in the build
     *
     *       #define SUPPORT_FILEFORMAT_BMP
     *       #define SUPPORT_FILEFORMAT_PNG
     *       #define SUPPORT_FILEFORMAT_TGA
     *       #define SUPPORT_FILEFORMAT_JPG
     *       #define SUPPORT_FILEFORMAT_GIF
     *       #define SUPPORT_FILEFORMAT_QOI
     *       #define SUPPORT_FILEFORMAT_PSD
     *       #define SUPPORT_FILEFORMAT_HDR
     *       #define SUPPORT_FILEFORMAT_PIC
     *       #define SUPPORT_FILEFORMAT_PNM
     *       #define SUPPORT_FILEFORMAT_DDS
     *       #define SUPPORT_FILEFORMAT_PKM
     *       #define SUPPORT_FILEFORMAT_KTX
     *       #define SUPPORT_FILEFORMAT_PVR
     *       #define SUPPORT_FILEFORMAT_ASTC
     *           Select desired fileformats to be supported for image data loading. Some of those formats are
     *           supported by default, to remove support, just comment unrequired #define in this module
     *
     *       #define SUPPORT_IMAGE_EXPORT
     *           Support image export in multiple file formats
     *
     *       #define SUPPORT_IMAGE_MANIPULATION
     *           Support multiple image editing functions to scale, adjust colors, flip, draw on images, crop...
     *           If not defined only some image editing functions supported: ImageFormat(), ImageAlphaMask(), ImageResize*()
     *
     *       #define SUPPORT_IMAGE_GENERATION
     *           Support procedural image generation functionality (gradient, spot, perlin-noise, cellular)
     *
     *   DEPENDENCIES:
     *       stb_image        - Multiple image formats loading (JPEG, PNG, BMP, TGA, PSD, GIF, PIC)
     *                          NOTE: stb_image has been slightly modified to support Android platform.
     *       stb_image_resize - Multiple image resize algorithms
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2013-2023 Ramon Santamaria (@raysan5)
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

    private final int UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD = 50;
    private final int GAUSSIAN_BLUR_ITERATIONS = 4;

    // Cubemap layouts
    public enum CubemapLayoutType {
        CUBEMAP_AUTO_DETECT(0),            // Automatically detect layout type
        CUBEMAP_LINE_VERTICAL(1),          // Layout is defined by a vertical line with faces
        CUBEMAP_LINE_HORIZONTAL(2),        // Layout is defined by an horizontal line with faces
        CUBEMAP_CROSS_THREE_BY_FOUR(3),    // Layout is defined by a 3x4 cross with cubemap faces
        CUBEMAP_CROSS_FOUR_BY_THREE(4),    // Layout is defined by a 4x3 cross with cubemap faces
        CUBEMAP_PANORAMA(5);               // Layout is defined by a panorama image (equirectangular map)

        private final int value;

        CubemapLayoutType(int value) {
            this.value = value;
        }

        public int GetValue() {
            return value;
        }
    }

    private final Raylib context;

    public rTextures(Raylib context) {
        this.context = context;
    }

    /**
     * Load image to memory
     *
     * @param fileName Path of file to load
     * @return {@code Image} object from file provided.
     */
    public Image LoadImage(String fileName) {
        Image image = new Image();

        // Loading file to memory
        byte[] fileData = null;
        try {
            fileData = context.files.LoadFileData(fileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (fileData != null) {
            // Loading image from memory data
            image = LoadImageFromMemory(fileName.substring(fileName.lastIndexOf('.')), fileData);

            if (image.data != null) {
                context.tracelog.TRACELOG(LOG_INFO, "IMAGE: [" + fileName + "] Data loaded successfully (" + image.width + "x" + image.height + ")");
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: [" + fileName + "] Failed to load data");
            }

        }

        return image;
    }

    /**
     * Load an image from RAW file data
     *
     * @param fileName
     * @param width
     * @param height
     * @param format
     * @param headerSize
     * @return
     */
    public Image LoadImageRaw(String fileName, int width, int height, rlPixelFormat format, int headerSize) {
        Image image = new Image();

        int dataSize = 0;
        byte[] fileData = null;

        try {
            fileData = context.files.LoadFileData(fileName);
        }
        catch (IOException exception) {
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

    /**
     * Load an image from an SVG file or string with custom size
     *
     * @param fileNameOrString
     * @param width
     * @param height
     * @return
     */
    public Image LoadImageSvg(String fileNameOrString, int width, int height) {
        Image image = new Image();
        boolean isSvgStringValid = false;

        if (SUPPORT_FILEFORMAT_SVG) {
            String fileText = "";
            if (context.files.FileExists(fileNameOrString)) {
                try {
                    fileText = context.files.LoadFileText(fileNameOrString);
                    isSvgStringValid = true;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                if (fileNameOrString.substring(0, 3).equalsIgnoreCase("<svg")) {
                    fileText = fileNameOrString;
                    isSvgStringValid = true;
                }
            }

            if (isSvgStringValid) {
                //TODO: Load SVG data and convert to raylib datatype
                SVGUniverse universe = new SVGUniverse();
                universe.loadSVG(new StringReader(fileText), "svg");
                SVGDiagram diagram = universe.getDiagram(universe.getLoadedDocumentURIs().get(0));
                diagram.setDeviceViewport(new java.awt.Rectangle(0, 0, width, height));

                BufferedImage svg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics2D = svg.createGraphics();
                graphics2D.setClip(0, 0, width, height);

                try {
                    diagram.render(null, graphics2D);
                    Color[] pixels = new Color[width * height];

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int argb = svg.getRGB(x, y);

                            int alpha = (argb >> 24) & 0xff;
                            int red = (argb >> 16) & 0xff;
                            int green = (argb >> 8) & 0xff;
                            int blue = argb & 0xff;

                            pixels[(y * width) + x] = new Color(red, green, blue, alpha);
                        }
                    }

                    image = new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
                    graphics2D.dispose();
                }
                catch (SVGException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return image;
    }

    /**
     * Load animated image data<br/>
     * - Image data buffer includes all frames: [image#0][image#1][image#2][...]<br/>
     * - Number of frames is returned through 'frames' parameter<br/>
     * - All frames are returned in RGBA format<br/>
     * - Frames delay data is discarded
     *
     * @param fileName
     * @return
     */
    public Image LoadImageAnim(String fileName) {
        Image image = new Image();
        int framesCount = 1;

        if (SUPPORT_FILEFORMAT_GIF) {
            if (context.files.IsFileExtension(fileName, ".gif")) {
                byte[] fileData = null;
                try {
                    fileData = context.files.LoadFileData(fileName);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }

                if (fileData != null) {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer widthBuffer = stack.mallocInt(1);
                        IntBuffer heightBuffer = stack.mallocInt(1);
                        IntBuffer compBuffer = stack.mallocInt(1);
                        PointerBuffer delaysBuffer = stack.callocPointer(1);

                        IntBuffer framesBuffer = stack.mallocInt(1);
                        framesBuffer.put(framesCount).flip();

                        ByteBuffer fileDataBuffer = ByteBuffer.allocateDirect(fileData.length);
                        fileDataBuffer.put(fileData).flip();

                        ByteBuffer imgBuffer = STBImage.stbi_load_gif_from_memory(fileDataBuffer, delaysBuffer,
                                                                                  widthBuffer, heightBuffer, framesBuffer, compBuffer, 4);

                        image.width = widthBuffer.get();
                        image.height = heightBuffer.get();
                        image.mipmaps = 1;
                        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

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
        else {
            image = LoadImage(fileName);
        }

        return image;
    }

    /**
     * Load image from memory buffer, fileType refers to extension: i.e. ".png" <br/>
     *
     * @param fileType File type of how the image is provided in the buffer
     * @param fileData buffer of data
     * @return {@code Image} object from data based on schema provided
     */
    public Image LoadImageFromMemory(String fileType, byte[] fileData) {
        Image image = new Image();

        if (SUPPORT_FILEFORMAT_PNG || SUPPORT_FILEFORMAT_BMP || SUPPORT_FILEFORMAT_TGA || SUPPORT_FILEFORMAT_JPG ||
                SUPPORT_FILEFORMAT_GIF || SUPPORT_FILEFORMAT_PIC || SUPPORT_FILEFORMAT_PNM || SUPPORT_FILEFORMAT_PSD) {
            if (fileType.equalsIgnoreCase(".png") || fileType.equalsIgnoreCase(".bmp") || fileType.equalsIgnoreCase(".tga") ||
                    (fileType.equalsIgnoreCase(".jpeg") || fileType.equalsIgnoreCase(".jpg")) || fileType.equalsIgnoreCase(".gif") ||
                    fileType.equalsIgnoreCase(".pic") || fileType.equalsIgnoreCase(".ppm") || fileType.equalsIgnoreCase(".pgm") || fileType.equalsIgnoreCase(".psd")) {

                if (fileData != null) {
                    int comp = 0;
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer widthBuffer = stack.mallocInt(1);
                        IntBuffer heightBuffer = stack.mallocInt(1);
                        IntBuffer compBuffer = stack.mallocInt(1);

                        ByteBuffer fileDataBuffer = ByteBuffer.allocateDirect(fileData.length);
                        fileDataBuffer.put(fileData).flip();

                        ByteBuffer imgBuffer = STBImage.stbi_load_from_memory(fileDataBuffer, widthBuffer,
                                                                              heightBuffer, compBuffer, 0);
                        if (imgBuffer == null) {
                            context.tracelog.TRACELOG(LOG_WARNING, "Failed to load image: " + fileType + "\t" + STBImage.stbi_failure_reason());
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
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    image.mipmaps = 1;

                    if (comp == 1) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
                    }
                    else if (comp == 2) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA;
                    }
                    else if (comp == 3) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8;
                    }
                    else if (comp == 4) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
                    }
                }
            }
        }
        if (SUPPORT_FILEFORMAT_HDR) {
            if (fileType.equalsIgnoreCase(".hdr")) {
                if (fileData != null) {
                    int comp = 0;
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer widthBuffer = stack.mallocInt(1);
                        IntBuffer heightBuffer = stack.mallocInt(1);
                        IntBuffer compBuffer = stack.mallocInt(1);

                        ByteBuffer fileDataBuffer = ByteBuffer.allocateDirect(fileData.length);
                        fileDataBuffer.put(fileData).flip();

                        ByteBuffer imgBuffer = STBImage.stbi_load_from_memory(fileDataBuffer, widthBuffer,
                                                                              heightBuffer, compBuffer, 0);
                        if (imgBuffer == null) {
                            context.tracelog.TRACELOG(LOG_WARNING, "Failed to load image " + fileType + "\n\t" + STBImage.stbi_failure_reason());
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
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    image.mipmaps = 1;

                    if (comp == 1) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_R32;
                    }
                    else if (comp == 3) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_R32G32B32;
                    }
                    else if (comp == 4) {
                        image.format = PIXELFORMAT_UNCOMPRESSED_R32G32B32A32;
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: HDR file format not supported");
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
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Data format not supported");
        }

        if (image.data != null) {
            context.tracelog.TRACELOG(LOG_INFO, "IMAGE: Data loaded successfully (" + image.width + "x" + image.height + " | " +
                    context.rlgl.rlGetPixelFormatName(image.format) + " | " + image.mipmaps + " mipmaps)");
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Failed to load image data");
        }

        return image;
    }

    /**
     * Check if an image is ready
     *
     * @param image Image to check
     * @return {@code true} if all requisite data for the image is present
     */
    public boolean IsImageReady(Image image) {
        return image.data != null && image.width > 0 && image.height > 0 && image.format.GetFormat() > 0;
    }

    /**
     * Unload image from memory
     *
     * @param image Image to unload. Mutates {@code image} to remove data, set width and height to {@code image}
     * @return {@code null} for object assignment
     */
    @Contract(mutates = "param1")
    public Image UnloadImage(Image image) {
        image.data = null;
        image.width = 0;
        image.height = 0;
        return null;
    }

    /**
     * Export image data to file <br/>
     * NOTE: File format depends on fileName extension
     *
     * @param image    Image to export
     * @param fileName Location to save file data
     * @return {@code true} if file was written successfully
     */
    public boolean ExportImage(Image image, String fileName) {
        boolean result = false;

        if (SUPPORT_IMAGE_EXPORT) {
            int channels = 4;
            boolean allocatedData = false;
            byte[] imgData = image.getData();

            if (image.format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                channels = 1;
            }
            else if (image.format == PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA) {
                channels = 2;
            }
            else if (image.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8) {
                channels = 3;
            }
            else if (image.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
                channels = 4;
            }
            else {
                // NOTE: Getting Color array as RGBA unsigned char values
                byte[] data = LoadImageColors(image);

                imgData = data;
                allocatedData = true;
            }

            ByteBuffer imgBuffer = ByteBuffer.allocateDirect(imgData.length);
            imgBuffer.put(imgData).flip();

            if (SUPPORT_FILEFORMAT_PNG) {
                if (context.files.IsFileExtension(fileName, ".png")) {
                    result = STBImageWrite.stbi_write_png(fileName, image.width, image.height,
                                                          channels, imgBuffer, image.width * channels);
                }
            }
            if (SUPPORT_FILEFORMAT_BMP) {
                if (context.files.IsFileExtension(fileName, ".bmp")) {
                    result = STBImageWrite.stbi_write_bmp(fileName, image.width, image.height, channels, imgBuffer);
                }
            }
            if (SUPPORT_FILEFORMAT_TGA) {
                if (context.files.IsFileExtension(fileName, ".tga")) {
                    result = STBImageWrite.stbi_write_tga(fileName, image.width, image.height, channels, imgBuffer);
                }
            }
            if (SUPPORT_FILEFORMAT_JPG) {
                if (context.files.IsFileExtension(fileName, ".jpeg")) {
                    result = STBImageWrite.stbi_write_jpg(fileName, image.width, image.height, channels, imgBuffer, 90);  // JPG quality: between 1 and 100
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
            else if (context.files.IsFileExtension(fileName, ".raw")) {
                // Export raw pixel data (without header)
                // NOTE: It's up to the user to track image parameters
                try {
                    result = context.files.SaveFileData(fileName, image.getData());
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            if (allocatedData) {
                imgData = null;
            }
        }    // SUPPORT_IMAGE_EXPORT

        if (result) {
            context.tracelog.TRACELOG(LOG_INFO, "FILEIO: [" + fileName + "] Image exported successfully");
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export image");
        }

        return result;
    }

    /**
     * Export image to memory buffer
     *
     * @param image    Image to export
     * @param fileType image format to write
     * @return image data as a byte array
     */
    public byte[] ExportImageToMemory(Image image, String fileType) {
        byte[] fileData = null;

        if ((image.width == 0) || (image.height == 0) || (image.data == null)) {
            return null;
        }

        if (SUPPORT_IMAGE_EXPORT) {
            int channels = 4;

            if (image.format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                channels = 1;
            }
            else if (image.format == PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA) {
                channels = 2;
            }
            else if (image.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8) {
                channels = 3;
            }
            else if (image.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
                channels = 4;
            }

            if (SUPPORT_FILEFORMAT_PNG) {
                if (fileType.equalsIgnoreCase(".png")) {
                    fileData = image.getData();
                }
            }
        }

        return fileData;
    }

    /**
     * Export image as code file (.h) defining an array of bytes
     *
     * @param image    Image to export
     * @param fileName Location to write file
     * @return {@code true} if file is written successfully
     */
    public boolean ExportImageAsCode(Image image, String fileName) {
        boolean success = false;

        if (SUPPORT_IMAGE_EXPORT) {

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
                success = context.files.SaveFileText(fileName, txtData);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }     // SUPPORT_IMAGE_EXPORT

        if (success) {
            context.tracelog.TRACELOG(LOG_INFO, "FILEIO: [" + fileName + "] Image as code exported successfully");
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to export image as code");
        }

        return success;
    }

    //------------------------------------------------------------------------------------
    // Image generation functions
    //------------------------------------------------------------------------------------

    /**
     * Generate image: plain color
     *
     * @param width  Width of generated image
     * @param height Height of generated image
     * @param color  Fill color for generated image
     * @return {@code Image} generated by specified parameters
     */
    public Image GenImageColor(int width, int height, Color color) {
        Color[] pixels = new Color[width * height];

        for (int i = 0; i < width * height; i++) {
            pixels[i] = color;
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: linear gradient
     *
     * @param width     Width of the generated image
     * @param height    Height of the generated image
     * @param direction Direction in degrees of the gradient
     * @param start     Starting color of the gradient
     * @param end       Ending color of the gradient
     * @return {@code Image} generated by specified parameters
     */
    public Image GenImageGradientLinear(int width, int height, int direction, Color start, Color end) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        float radianDirection = (float) (90 - direction) / 180.f * 3.14159f;
        float cosDir = (float) Math.cos(radianDirection);
        float sinDir = (float) Math.sin(radianDirection);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Calculate the relative position of the pixel along the gradient direction
                float pos = (i * cosDir + j * sinDir) / (width * cosDir + height * sinDir);

                float factor = pos;
                factor = Math.min(factor, 1.0f);  // Clamp to [0,1]
                factor = Math.max(factor, 0.0f);  // Clamp to [0,1]

                // Generate the color for this pixel
                pixels[j * width + i].r = (int) ((float) end.r * factor + (float) start.r * (1.0f - factor));
                pixels[j * width + i].g = (int) ((float) end.g * factor + (float) start.g * (1.0f - factor));
                pixels[j * width + i].b = (int) ((float) end.b * factor + (float) start.b * (1.0f - factor));
                pixels[j * width + i].a = (int) ((float) end.a * factor + (float) start.a * (1.0f - factor));
            }
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: radial gradient
     *
     * @param width   Width of the generated image
     * @param height  Height of the generated image
     * @param density
     * @param inner   Center color of the gradient
     * @param outer   Edge color of the gradient
     * @return {@code Image} generated by specified parameters
     */
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

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: square gradient
     *
     * @param width   Width of the generated image
     * @param height  Height of the generated image
     * @param density
     * @param inner   Center color of the gradient
     * @param outer   Edge color of the gradient
     * @return {@code Image} generated by specified parameters
     */
    public Image GenImageGradientSquare(int width, int height, float density, Color inner, Color outer) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        float centerX = (float) width / 2.0f;
        float centerY = (float) height / 2.0f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calculate the Manhattan distance from the center
                float distX = Math.abs(x - centerX);
                float distY = Math.abs(y - centerY);

                // Normalize the distances by the dimensions of the gradient rectangle
                float normalizedDistX = distX / centerX;
                float normalizedDistY = distY / centerY;

                // Calculate the total normalized Manhattan distance
                float manhattanDist = Math.max(normalizedDistX, normalizedDistY);

                // Subtract the density from the manhattanDist, then divide by (1 - density)
                // This makes the gradient start from the center when density is 0, and from the edge when density is 1
                float factor = (manhattanDist - density) / (1.0f - density);

                // Clamp the factor between 0 and 1
                factor = Math.min(Math.min(factor, 0.0f), 1.0f);

                // Blend the colors based on the calculated factor
                pixels[y * width + x].r = (int) ((float) outer.r * factor + (float) inner.r * (1.0f - factor));
                pixels[y * width + x].g = (int) ((float) outer.g * factor + (float) inner.g * (1.0f - factor));
                pixels[y * width + x].b = (int) ((float) outer.b * factor + (float) inner.b * (1.0f - factor));
                pixels[y * width + x].a = (int) ((float) outer.a * factor + (float) inner.a * (1.0f - factor));
            }
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: checked
     *
     * @param width   Width of the generated image
     * @param height  Height of the generated image
     * @param checksX ?
     * @param checksY ?
     * @param col1    Primary color of checker pattern
     * @param col2    Secondary color of checker pattern
     * @return {@code Image} generated by specified parameters
     */
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
                else {
                    pixels[y * width + x] = col2;
                }
            }
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: white noise
     *
     * @param width  Width of the generated image
     * @param height Height of the generated image
     * @param factor ?
     * @return {@code Image} generated by specified parameters
     */
    public Image GenImageWhiteNoise(int width, int height, float factor) {
        Color[] pixels = new Color[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Color();
        }

        for (int i = 0; i < width * height; i++) {
            if ((Math.random() * (99 - 0 + 1) + 0) < (int) (factor * 100.0f)) {
                pixels[i] = Color.WHITE;
            }
            else {
                pixels[i] = Color.BLACK;
            }
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: Perlin noise
     *
     * @param width   Width of the generated image
     * @param height  Height of the generated image
     * @param offsetX ?
     * @param offsetY ?
     * @param scale   ?
     * @return {@code Image} generated by specified parameters
     */
    public Image GenImagePerlinNoise(int width, int height, int offsetX, int offsetY, int scale) {
        Color[] pixels = new Color[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 4) {
                float nx = (float) ((x + offsetX) * (scale / width));
                float ny = (float) ((y + offsetY) * (scale / height));

                // Basic perlin noise implementation (not used)
                //float p = (stb_perlin_noise3(nx, ny, 0.0f, 0, 0, 0);

                // Calculate a better perlin noise using fbm (fractal brownian motion)
                // Typical values to start playing with:
                //   lacunarity = ~2.0   -- spacing between successive octaves (use exactly 2.0 for wrapping output)
                //   gain       =  0.5   -- relative weighting applied to each successive octave
                //   octaves    =  6     -- number of "octaves" of noise3() to sum
                float p = stb_perlin_fbm_noise3(nx, ny, 1.0f, 2.0f, 0.5f, 6);

                // Clamp between -1.0f and 1.0f
                if (p < -1.0f) {
                    p = -1.0f;
                }
                if (p > 1.0f) {
                    p = 1.0f;
                }

                // We need to normalize the data from [-1..1] to [0..1]
                float np = (p + 1.0f) / 2.0f;

                int intensity = (int) (np * 255.0f);
                pixels[y * width + x] = new Color(intensity, intensity, intensity, 255);
            }
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: cellular algorithm
     *
     * @param width    Width of the generated image
     * @param height   Height of the generated image
     * @param tileSize Size of cells
     * @return {@code Image} generated by specified parameters
     */
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
                    if ((tileX + i < 0) || (tileX + i >= seedsPerRow)) {
                        continue;
                    }

                    for (int j = -1; j < 2; j++) {
                        if ((tileY + j < 0) || (tileY + j >= seedsPerCol)) {
                            continue;
                        }

                        Vector2 neighborSeed = seeds[(tileY + j) * seedsPerRow + tileX + i];

                        float dist = (float) Math.hypot(x - (int) neighborSeed.x, y - (int) neighborSeed.y);
                        minDistance = Math.min(minDistance, dist);
                    }
                }

                // I made this up but it seems to give good results at all tile sizes
                int intensity = (int) (minDistance * 256.0f / tileSize);
                if (intensity > 255) {
                    intensity = 255;
                }

                pixels[y * width + x] = new Color(intensity, intensity, intensity, 255);
            }
        }

        return new Image(pixels, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
    }

    /**
     * Generate image: grayscale image from text data
     *
     * @param width  Width of the generated image
     * @param height Height of the generated image
     * @param text   String transform into
     * @return {@code Image} generated by specified parameters
     */
    public Image GenImageText(int width, int height, String text) {
        Image image = new Image();

        image.width = width;
        image.height = height;
        image.format = PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
        image.mipmaps = 1;
        image.setData(text.getBytes(StandardCharsets.UTF_8));

        return image;
    }

    //------------------------------------------------------------------------------------
    // Image manipulation functions
    //------------------------------------------------------------------------------------

    /**
     * Copy an image to a new image
     *
     * @param image Source image
     * @return Duplicated image
     */
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
            }
            if (height < 1) {
                height = 1;
            }
        }

        if (image.data != null) {
            byte[] imgData = image.getData();

            newImage.setData(imgData);
            newImage.width = image.width;
            newImage.height = image.height;
            newImage.mipmaps = image.mipmaps;
            newImage.format = image.format;
        }

        return newImage;
    }

    /**
     * Create an image from another image piece
     *
     * @param image     Source image
     * @param rectangle Area of source image to extract
     * @return Extracted section of the source image
     */
    public Image ImageFromImage(Image image, Rectangle rectangle) {
        Image result = GenImageColor(image.width, image.height, Color.BLANK);
        int bytesPerPixel = GetPixelDataSize(1, 1, image.format);

        result.width = (int) rectangle.width;
        result.height = (int) rectangle.height;
        result.format = image.format;
        result.mipmaps = 1;

        byte[] data = new byte[(int) (rectangle.width * rectangle.height * bytesPerPixel)];
        byte[] srcData = image.getData();

        for (int y = 0; y < rectangle.height; y++) {
            System.arraycopy(srcData, (int) (((y + rectangle.y) * image.width + rectangle.x) * bytesPerPixel), data, (int) (y * rectangle.width * bytesPerPixel), (int) (rectangle.width * bytesPerPixel));
        }

        result.setData(data);
        return result;
    }

    /**
     * Crop an image to area defined by a rectangle <br/>
     * NOTE: Security checks are performed in case rectangle goes out of bounds
     *
     * @param image Source image
     * @param crop  Bounds of crop area
     * @return {@code Image} cropped to defined bounds
     */
    public Image ImageCrop(Image image, Rectangle crop) {
        Image result = new Image();

        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        // Security checks to validate crop rectangle
        if (crop.x < 0) {
            crop.width += crop.x;
            crop.x = 0;
        }

        if (crop.y < 0) {
            crop.height += crop.y;
            crop.y = 0;
        }

        if ((crop.x + crop.width) > image.width) {
            crop.width = image.width - crop.x;
        }

        if ((crop.y + crop.height) > image.height) {
            crop.height = image.height - crop.y;
        }

        if ((crop.x > image.width) || (crop.y > image.height)) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Failed to crop, rectangle out of bounds");
            return image;
        }

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
            return image;
        }
        else {
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

            result.setData(croppedData);
            result.width = (int) crop.width;
            result.height = (int) crop.height;
            result.mipmaps = image.mipmaps;
            result.format = image.format;
        }

        return result;
    }

    /**
     * Convert image data to desired format
     *
     * @param image     Source image
     * @param newFormat Desired output {@code rlPixelFormat}
     * @return Image in specified {@code rlPixelFormat}
     */
    public Image ImageFormat(Image image, rlPixelFormat newFormat) {
        Image result = new Image();
        result.width = image.width;
        result.height = image.height;
        result.format = newFormat;

        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if ((newFormat != null) && (image.format != newFormat)) {
            if ((image.format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) && (newFormat.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat())) {
                Vector4[] pixels = LoadImageDataNormalized(image);     // Supports 8 to 32 bit per channel
                short r, g, b, a;

                switch (newFormat) {
                    case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height);

                        for (int i = 0; i < result.width * result.height; i++) {
                            result.data.put((byte) (((pixels[i].x * 0.299f) + (pixels[i].y * 0.587f) + (pixels[i].z * 0.114f)) * 255));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 2);

                        for (int i = 0, k = 0; i < result.width * result.height * 2; i += 2, k++) {
                            result.data.put((byte) ((pixels[k].x * 0.299f + pixels[k].y * 0.587f + pixels[k].z * 0.114f) * 255.0f));
                            result.data.put((byte) (pixels[k].w * 255.0f));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 3 * Short.BYTES);

                        for (int i = 0; i < result.width * result.height; i++) {
                            r = (byte) Math.round(pixels[i].x * 31.0f);
                            g = (byte) Math.round(pixels[i].y * 63.0f);
                            b = (byte) Math.round(pixels[i].z * 31.0f);

                            result.data.putShort((short) (r << 11 | g << 5 | b));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 3);

                        for (int i = 0, k = 0; i < result.width * result.height * 3; i += 3, k++) {
                            result.data.put((byte) (pixels[k].x * 255.0f));
                            result.data.put((byte) (pixels[k].y * 255.0f));
                            result.data.put((byte) (pixels[k].z * 255.0f));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 4 * Short.BYTES);

                        for (int i = 0; i < result.width * result.height; i++) {
                            r = (byte) (Math.round(pixels[i].x * 31.0f));
                            g = (byte) (Math.round(pixels[i].y * 31.0f));
                            b = (byte) (Math.round(pixels[i].z * 31.0f));
                            a = (byte) ((pixels[i].w > ((float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f)) ? 1 : 0);

                            result.data.putShort((short) (r << 11 | g << 6 | b << 1 | a));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 4 * Short.BYTES);

                        for (int i = 0; i < result.width * result.height; i++) {
                            r = (byte) (Math.round(pixels[i].x * 15.0f));
                            g = (byte) (Math.round(pixels[i].y * 15.0f));
                            b = (byte) (Math.round(pixels[i].z * 15.0f));
                            a = (byte) (Math.round(pixels[i].w * 15.0f));

                            result.data.putShort((short) (r << 12 | g << 8 | b << 4 | a));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 4);

                        for (int i = 0, k = 0; i < result.width * result.height * 4; i += 4, k++) {
                            result.data.put((byte) (pixels[k].x * 255.0f));
                            result.data.put((byte) (pixels[k].y * 255.0f));
                            result.data.put((byte) (pixels[k].z * 255.0f));
                            result.data.put((byte) (pixels[k].w * 255.0f));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R32:
                        // WARNING: Image is converted to GRAYSCALE eqeuivalent 32bit
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * Float.BYTES);

                        for (int i = 0; i < result.width * result.height; i++) {
                            result.data.putFloat(pixels[i].x * 0.299f + pixels[i].y * 0.587f + pixels[i].z * 0.114f);
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 3 * Float.BYTES);

                        for (int i = 0, k = 0; i < result.width * result.height * 3; i += 3, k++) {
                            result.data.putFloat(pixels[k].x);
                            result.data.putFloat(pixels[k].y);
                            result.data.putFloat(pixels[k].z);
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 4 * Float.BYTES);

                        for (int i = 0, k = 0; i < result.width * result.height * 4; i += 4, k++) {
                            result.data.putFloat(pixels[k].x);
                            result.data.putFloat(pixels[k].y);
                            result.data.putFloat(pixels[k].z);
                            result.data.putFloat(pixels[k].w);
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R16:
                        // WARNING: Image is converted to GRAYSCALE equivalent 16bit
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * Short.BYTES);

                        for (int i = 0; i < result.width * result.height; i++) {
                            result.data.asShortBuffer().put(FloatToHalf((float) (pixels[i].x * 0.299f + pixels[i].y * 0.587f + pixels[i].z * 0.114f)));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R16G16B16:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 3 * Short.BYTES);

                        for (int i = 0, k = 0; i < result.width * result.height * 3; i += 3, k++) {
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].x));
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].y));
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].z));
                        }
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                        result.data = ByteBuffer.allocateDirect(result.width * result.height * 4 * Short.BYTES);

                        for (int i = 0, k = 0; i < result.width * result.height * 4; i += 4, k++) {
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].x));
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].y));
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].z));
                            result.data.asShortBuffer().put(FloatToHalf(pixels[k].w));
                        }
                        break;

                    default:
                        break;
                }
                result.data.flip();

                // In case original image had mipmaps, generate mipmaps for formated image
                // NOTE: Original mipmaps are replaced by new ones, if custom mipmaps were used, they are lost
                if (image.mipmaps >= 1) {
                    result.mipmaps = 1;
                    if (SUPPORT_IMAGE_MANIPULATION) {
                        if (result.data != null) {
                            ImageMipmaps(result);
                        }
                    }
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Data format is compressed, can not be converted");
            }
        }
        else {
            return image;
        }

        return result;
    }

    /**
     * Create an image from text using raylib default font
     *
     * @param text     Text to be used
     * @param fontSize Size of font in pixels
     * @param color    Color to use when drawing characters
     * @return Text as image using specified size and color
     */
    public Image ImageText(String text, int fontSize, Color color) {
        int defaultFontSize = 10;   // Default Font chars height in pixel
        if (fontSize < defaultFontSize) {
            fontSize = defaultFontSize;
        }
        int spacing = fontSize / defaultFontSize;

        return ImageTextEx(context.text.GetFontDefault(), text, (float) fontSize, (float) spacing, color);
    }

    /**
     * Create an image from text using a custom sprite font
     *
     * @param font     Font to use for image generation
     * @param text     Text to be used
     * @param fontSize Size of font in pixels
     * @param spacing  Spacing between characters in pixels
     * @param tint     Color to use when drawing characters
     * @return Text as an image using specified parameters
     */
    public Image ImageTextEx(Font font, String text, float fontSize, float spacing, Color tint) {
        Image imText;

        if (SUPPORT_MODULE_RTEXT) {
            int size = text.length();   // Get size in bytes of text

            int textOffsetX = 0;            // Image drawing position X
            int textOffsetY = 0;            // Offset between lines (on linebreak '\n')

            // NOTE: Text image is generated at font base size, later scaled to desired font size
            Vector2 imSize = context.text.MeasureTextEx(font, text, (float) font.baseSize, spacing);  // WARNING: Module required: rtext
            Vector2 textSize = context.text.MeasureTextEx(font, text, fontSize, spacing);

            // Create image to store text
            imText = GenImageColor((int) imSize.x, (int) imSize.y, Color.BLANK);

            for (int i = 0; i < size; ) {
                // Get next codepoint from byte string and glyph index in font
                int codepoint = Character.codePointAt(text.toCharArray(), i);
                int codepointByteCount = context.text.GetCodePointByteCount(codepoint);
                int index = context.text.GetGlyphIndex(font, codepoint);

                if (codepoint == '\n') {
                    // NOTE: Fixed line spacing of 1.5 line-height
                    // TODO: Support custom line spacing defined by user
                    textOffsetY += (font.baseSize + font.baseSize / 2);
                    textOffsetX = 0;
                }
                else {
                    if ((codepoint != ' ') && (codepoint != '\t')) {
                        Rectangle rec = new Rectangle(
                                (textOffsetX + font.glyphs[index].offsetX),
                                (textOffsetY + font.glyphs[index].offsetY),
                                font.recs[index].width,
                                font.recs[index].height
                        );

                        imText = ImageDraw(
                                imText,
                                font.glyphs[index].image,
                                new Rectangle(0, 0, font.glyphs[index].image.width, font.glyphs[index].image.height),
                                rec,
                                tint
                        );
                    }

                    if (font.glyphs[index].advanceX == 0) {
                        textOffsetX += (int) (font.recs[index].width + spacing);
                    }
                    else {
                        textOffsetX += font.glyphs[index].advanceX + (int) spacing;
                    }
                }

                i += codepointByteCount;   // Move text bytes counter to next codepoint
            }

            // Scale image depending on text size
            if (textSize.y != imSize.y) {
                float scaleFactor = textSize.y / imSize.y;
                context.tracelog.TRACELOG(LOG_INFO, "IMAGE: Text scaled by factor: " + scaleFactor);

                // Using nearest-neighbor scaling algorithm for default font
                // TODO: Allow defining the preferred scaling mechanism externally
                if (font.texture.id == context.text.GetFontDefault().texture.id) {
                    imText = ImageResizeNN(imText, (int) (imSize.x * scaleFactor), (int) (imSize.y * scaleFactor));
                }
                else {
                    imText = ImageResize(imText, (int) (imSize.x * scaleFactor), (int) (imSize.y * scaleFactor));
                }
            }
        }
        else {
            imText = GenImageColor(200, 60, Color.BLACK);     // Generating placeholder black image rectangle
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: ImageTextEx() requires module: rtext");
        }

        return imText;
    }

    /**
     * Resize an image to new size using Nearest-Neighbor scaling algorithm
     *
     * @param image     Image to be resized
     * @param newWidth  Width to be resized to
     * @param newHeight Height to be resized to
     * @return Resized image
     */
    public Image ImageResizeNN(Image image, int newWidth, int newHeight) {
        Image result = new Image(image.getData(), image.width, image.height, image.format, image.mipmaps);

        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return result;
        }

        Color[] pixels = Color.FromPixels(LoadImageColors(image));
        Color[] output = new Color[newWidth * newHeight];

        // EDIT: added +1 to account for an early rounding problem
        int xRatio = ((result.width << 16) / newWidth) + 1;
        int yRatio = ((result.height << 16) / newHeight) + 1;

        int x2, y2;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                x2 = ((x * xRatio) >> 16);
                y2 = ((y * yRatio) >> 16);

                output[(y * newWidth) + x] = pixels[(y2 * result.width) + x2];
            }
        }

        rlPixelFormat format = result.format;

        result.setData(output);
        result.width = newWidth;
        result.height = newHeight;
        result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        result = ImageFormat(result, format);  // Reformat 32bit RGBA image to original format

        return result;
    }

    /**
     * Resize and image to new size using stb scaling filters:<br/>
     * STBIR_DEFAULT_FILTER_UPSAMPLE <br/>
     * STBIR_DEFAULT_FILTER_DOWNSAMPLE <br/>
     * STBIR_FILTER_CATMULLROM <br/>
     * STBIR_FILTER_MITCHELL   (high-quality Catmull-Rom)
     *
     * @param image     Image to resize
     * @param newWidth  Width to be resized to
     * @param newHeight Height to be resized to
     * @return Resized image
     */
    public Image ImageResize(Image image, int newWidth, int newHeight) {
        Image result = new Image();

        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if ((image.format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) ||
                (image.format == PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA) ||
                (image.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8) ||
                (image.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8A8)) {

            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(newWidth * newHeight * bytesPerPixel);

            switch (image.format) {
                case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                    stbir_resize_uint8_linear(image.data, image.width, image.height, 0, outputBuffer,
                                              newWidth, newHeight, 0, 1);
                    break;
                case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                    stbir_resize_uint8_linear(image.data, image.width, image.height, 0, outputBuffer,
                                              newWidth, newHeight, 0, 2);
                    break;
                case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                    stbir_resize_uint8_linear(image.data, image.width, image.height, 0, outputBuffer,
                                              newWidth, newHeight, 0, 3);
                    break;
                case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                    stbir_resize_uint8_linear(image.data, image.width, image.height, 0, outputBuffer,
                                              newWidth, newHeight, 0, 4);
                    break;
                default:
                    break;
            }

            result.setData(outputBuffer);
            result.width = newWidth;
            result.height = newHeight;
            result.format = image.format;
        }
        else {
            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(newWidth * newHeight * 4);

            stbir_resize_uint8_linear(image.data, image.width, image.height, 0, outputBuffer,
                                      newWidth, newHeight, 0, 4);

            rlPixelFormat format = image.format;

            result.setData(outputBuffer);
            result.width = newWidth;
            result.height = newHeight;
            result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

            result = ImageFormat(result, format);  // Reformat 32bit RGBA image to original format
        }

        return result;
    }

    /**
     * Resize canvas and fill with color
     *
     * @param image     Image to resize
     * @param newWidth  Width to be resized to
     * @param newHeight Height to be resized to
     * @param offsetX   Resize x-axis offset relative to the top-left corner of the image
     * @param offsetY   Resize y-axis offset relative to the top-left corner of the image
     * @param fill      Color used to fill canvas
     * @return Resized image
     */
    public Image ImageResizeCanvas(Image image, int newWidth, int newHeight, int offsetX, int offsetY, Color fill) {
        Image result = new Image(image.getData(), image.width, image.height, image.format, image.mipmaps);

        // Security check to avoid program crash
        if ((result.data == null) || (result.width == 0) || (result.height == 0)) {
            return result;
        }

        if (result.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }
        if (result.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else if ((newWidth != result.width) || (newHeight != result.height)) {
            Rectangle srcRec = new Rectangle(0, 0, (float) result.width, (float) result.height);
            Vector2 dstPos = new Vector2((float) offsetX, (float) offsetY);

            if (offsetX < 0) {
                srcRec.x = (float) -offsetX;
                srcRec.width += (float) offsetX;
                dstPos.x = 0;
            }
            else if ((offsetX + result.width) > newWidth) {
                srcRec.width = (float) (newWidth - offsetX);
            }

            if (offsetY < 0) {
                srcRec.y = (float) -offsetY;
                srcRec.height += (float) offsetY;
                dstPos.y = 0;
            }
            else if ((offsetY + result.height) > newHeight) {
                srcRec.height = (float) (newHeight - offsetY);
            }

            if (newWidth < srcRec.width) {
                srcRec.width = (float) newWidth;
            }
            if (newHeight < srcRec.height) {
                srcRec.height = (float) newHeight;
            }

            int bytesPerPixel = GetPixelDataSize(1, 1, result.format);
            byte[] resizedData = new byte[newWidth * newHeight * bytesPerPixel];

            // TODO: Fill resizedData with fill color (must be formatted to image.format)

            int dstOffsetSize = ((int) dstPos.y * newWidth + (int) dstPos.x) * bytesPerPixel;
            for (int x = 0; x < srcRec.width; x++) {
                for (int y = 0; y < (int) srcRec.height; y++) {
                    for (int i = 0; i < bytesPerPixel; i++) {
                        resizedData[(y * result.width + x) * bytesPerPixel + i] = result.getData()[(y * result.width + (result.width - 1 - x)) * bytesPerPixel + i];
                    }
                }
            }

            result.setData(resizedData);
            result.width = newWidth;
            result.height = newHeight;
        }

        return result;
    }

    /**
     * Convert image to POT (power-of-two)
     *
     * @param image Image to resize
     * @param fill  Color used to fill canvas
     * @return Resized image
     */
    public Image ImageToPOT(Image image, Color fill) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        // Calculate next power-of-two values
        // NOTE: Just add the required amount of pixels at the right and bottom sides of image...
        int potWidth = (int) Math.pow(2, Math.ceil(Math.log((float) image.width) / Math.log(2)));
        int potHeight = (int) Math.pow(2, Math.ceil(Math.log((float) image.height) / Math.log(2)));

        // Check if POT texture generation is required (if texture is not already POT)
        if ((potWidth != image.width) || (potHeight != image.height)) {
            return ImageResizeCanvas(image, potWidth, potHeight, 0, 0, fill);
        }
        else {
            return image;
        }
    }

    /**
     * Crop image depending on alpha value
     *
     * @param image     Image to crop
     * @param threshold Threshold for alpha crop defined as [0.0f ... 1.0f]
     * @return Cropped Image
     */
    public Image ImageAlphaCrop(Image image, float threshold) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        Rectangle crop = GetImageAlphaBorder(image, threshold);

        // Crop if rectangle is valid
        if (((int) crop.width != 0) && ((int) crop.height != 0)) {
            return ImageCrop(image, crop);
        }
        else {
            return image;
        }
    }

    /**
     * Clear alpha channel to desired color
     *
     * @param image     Image to clear
     * @param color     Color to use in clearing
     * @param threshold Threshold for alpha crop defined as [0.0f ... 1.0f]
     * @return Cleared Image
     */
    public Image ImageAlphaClear(Image image, Color color, float threshold) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        byte[] imData = image.getData();
        byte thresholdValue, r, g, b, a;

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }
        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else {
            switch (image.format) {
                case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                    thresholdValue = (byte) (threshold * 255.0f);
                    for (int i = 1; i < image.width * image.height * 2; i += 2) {
                        if (imData[i] <= thresholdValue) {
                            imData[i - 1] = (byte) color.r;
                            imData[i] = (byte) color.a;
                        }
                    }
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                    thresholdValue = (byte) ((threshold < 0.5f) ? 0 : 1);

                    r = (byte) (Math.round((float) color.r * 31.0f));
                    g = (byte) (Math.round((float) color.g * 31.0f));
                    b = (byte) (Math.round((float) color.b * 31.0f));
                    a = (byte) ((color.a < 128) ? 0 : 1);

                    for (int i = 0; i < image.width * image.height; i++) {
                        if ((imData[i] & 0b0000000000000001) <= thresholdValue) {
                            imData[i] = (byte) (r << 11 | g << 6 | b << 1 | a);
                        }
                    }
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                    thresholdValue = (byte) (threshold * 15.0f);

                    r = (byte) Math.round((float) color.r * 15.0f);
                    g = (byte) Math.round((float) color.g * 15.0f);
                    b = (byte) Math.round((float) color.b * 15.0f);
                    a = (byte) Math.round((float) color.a * 15.0f);

                    for (int i = 0; i < image.width * image.height; i++) {
                        if ((imData[i] & 0x000f) <= thresholdValue) {
                            imData[i] = (byte) (r << 12 | g << 8 | b << 4 | a);
                        }
                    }
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                    thresholdValue = (byte) (threshold * 255.0f);
                    for (int i = 3; i < image.width * image.height * 4; i += 4) {
                        if (imData[i] <= thresholdValue) {
                            imData[i - 3] = (byte) color.r;
                            imData[i - 2] = (byte) color.g;
                            imData[i - 1] = (byte) color.b;
                            imData[i] = (byte) color.a;
                        }
                    }
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                    for (int i = 3; i < image.width * image.height * 4; i += 4) {
                        if (imData[i] <= threshold) {
                            imData[i - 3] = (byte) ((float) color.r / 255.0f);
                            imData[i - 2] = (byte) ((float) color.g / 255.0f);
                            imData[i - 1] = (byte) ((float) color.b / 255.0f);
                            imData[i] = (byte) ((float) color.a / 255.0f);
                        }
                    }
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                    for (int i = 3; i < image.width * image.height * 4; i += 4) {
                        if (HalfToFloat(imData[i]) <= threshold) {
                            imData[i - 3] = (byte) FloatToHalf((float) color.r / 255.0f);
                            imData[i - 2] = (byte) FloatToHalf((float) color.g / 255.0f);
                            imData[i - 1] = (byte) FloatToHalf((float) color.b / 255.0f);
                            imData[i] = (byte) FloatToHalf((float) color.a / 255.0f);
                        }
                    }
                    break;

                default:
                    break;
            }
        }

        return new Image(imData, image.width, image.height, image.format, image.mipmaps);
    }

    /**
     * Apply alpha mask to image
     *
     * @param image     Image to modify
     * @param alphaMask Alpha mask to be applied
     * @return Image with alpha mask applied. Image returned is GRAY_ALPHA (16bit) or RGBA (32bit)
     */
    public Image ImageAlphaMask(Image image, Image alphaMask) {
        if ((image.width != alphaMask.width) || (image.height != alphaMask.height)) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Alpha mask must be same size as image");
        }
        else if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Alpha mask can not be applied to compressed data formats");
        }
        else {
            // Force mask to be Grayscale
            Image mask = ImageCopy(alphaMask);
            if (mask.format != PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                mask = ImageFormat(mask, PIXELFORMAT_UNCOMPRESSED_GRAYSCALE);
            }

            byte[] data, imageData, maskData;
            imageData = image.getData();
            maskData = mask.getData();

            // In case image is only grayscale, we just add alpha channel
            if (image.format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) {
                data = new byte[image.width * image.height * 2];

                // Apply alpha mask to alpha channel
                for (int i = 0, k = 0; (i < mask.width * mask.height) || (i < image.width * image.height); i++, k += 2) {
                    data[k] = imageData[i];
                    data[k + 1] = maskData[i];
                }
            }
            else {
                data = new byte[image.width * image.height * 4];

                // Convert image to RGBA
                if (image.format != PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
                    image = ImageFormat(image, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
                }

                // Apply alpha mask to alpha channel
                for (int i = 0, k = 3; (i < mask.width * mask.height) || (i < image.width * image.height); i++, k += 4) {
                    data[k] = maskData[i];
                }
            }
            UnloadImage(mask);

            return new Image(data, image.width, image.height, image.format, image.mipmaps);
        }

        return image;
    }

    /**
     * Premultiply alpha channel
     *
     * @param image Image
     * @return Image post premultiply
     */
    public Image ImageAlphaPremultiply(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        float alpha;
        Color[] pixels = Color.FromPixels(LoadImageColors(image));

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

        Image result = new Image(pixels, image.width, image.height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, image.mipmaps);

        return ImageFormat(result, image.format);
    }


    /**
     * Apply box blur
     *
     * @param image    Image to apply blur to
     * @param blurSize
     * @return Image with Gaussian blur applied
     */
    public Image ImageBlurGaussian(Image image, int blurSize) {
        Image result = ImageCopy(image);
        // Security check to avoid program crash
        if ((result.data == null) || (result.width == 0) || (result.height == 0)) {
            return result;
        }

        result = ImageAlphaPremultiply(result);

        Color[] pixels = Color.FromPixels(LoadImageColors(result));

        // Loop switches between pixelsCopy1 and pixelsCopy2
        Vector4[] pixelsCopy1 = new Vector4[image.height * image.width];
        Vector4[] pixelsCopy2 = new Vector4[image.height * image.width];

        for (int i = 0; i < (image.height) * (image.width); i++) {
            pixelsCopy1[i].x = pixels[i].r;
            pixelsCopy1[i].y = pixels[i].g;
            pixelsCopy1[i].z = pixels[i].b;
            pixelsCopy1[i].w = pixels[i].a;
        }

        // Repeated convolution of rectangular window signal by itself converges to a gaussian distribution
        for (int j = 0; j < GAUSSIAN_BLUR_ITERATIONS; j++) {
            // Horizontal motion blur
            for (int row = 0; row < image.height; row++) {
                float avgR = 0.0f;
                float avgG = 0.0f;
                float avgB = 0.0f;
                float avgAlpha = 0.0f;
                int convolutionSize = blurSize + 1;

                for (int i = 0; i < blurSize + 1; i++) {
                    avgR += pixelsCopy1[row * image.width + i].x;
                    avgG += pixelsCopy1[row * image.width + i].y;
                    avgB += pixelsCopy1[row * image.width + i].z;
                    avgAlpha += pixelsCopy1[row * image.width + i].w;
                }

                pixelsCopy2[row * image.width].x = avgR / convolutionSize;
                pixelsCopy2[row * image.width].y = avgG / convolutionSize;
                pixelsCopy2[row * image.width].z = avgB / convolutionSize;
                pixelsCopy2[row * image.width].w = avgAlpha / convolutionSize;

                for (int x = 1; x < image.width; x++) {
                    if (x - blurSize >= 0) {
                        avgR -= pixelsCopy1[row * image.width + x - blurSize].x;
                        avgG -= pixelsCopy1[row * image.width + x - blurSize].y;
                        avgB -= pixelsCopy1[row * image.width + x - blurSize].z;
                        avgAlpha -= pixelsCopy1[row * image.width + x - blurSize].w;
                        convolutionSize--;
                    }

                    if (x + blurSize < image.width) {
                        avgR += pixelsCopy1[row * image.width + x + blurSize].x;
                        avgG += pixelsCopy1[row * image.width + x + blurSize].y;
                        avgB += pixelsCopy1[row * image.width + x + blurSize].z;
                        avgAlpha += pixelsCopy1[row * image.width + x + blurSize].w;
                        convolutionSize++;
                    }

                    pixelsCopy2[row * image.width + x].x = avgR / convolutionSize;
                    pixelsCopy2[row * image.width + x].y = avgG / convolutionSize;
                    pixelsCopy2[row * image.width + x].z = avgB / convolutionSize;
                    pixelsCopy2[row * image.width + x].w = avgAlpha / convolutionSize;
                }
            }

            // Vertical motion blur
            for (int col = 0; col < image.width; col++) {
                float avgR = 0.0f;
                float avgG = 0.0f;
                float avgB = 0.0f;
                float avgAlpha = 0.0f;
                int convolutionSize = blurSize + 1;

                for (int i = 0; i < blurSize + 1; i++) {
                    avgR += pixelsCopy2[i * image.width + col].x;
                    avgG += pixelsCopy2[i * image.width + col].y;
                    avgB += pixelsCopy2[i * image.width + col].z;
                    avgAlpha += pixelsCopy2[i * image.width + col].w;
                }

                pixelsCopy1[col].x = (avgR / convolutionSize);
                pixelsCopy1[col].y = (avgG / convolutionSize);
                pixelsCopy1[col].z = (avgB / convolutionSize);
                pixelsCopy1[col].w = (avgAlpha / convolutionSize);

                for (int y = 1; y < image.height; y++) {
                    if (y - blurSize >= 0) {
                        avgR -= pixelsCopy2[(y - blurSize) * image.width + col].x;
                        avgG -= pixelsCopy2[(y - blurSize) * image.width + col].y;
                        avgB -= pixelsCopy2[(y - blurSize) * image.width + col].z;
                        avgAlpha -= pixelsCopy2[(y - blurSize) * image.width + col].w;
                        convolutionSize--;
                    }
                    if (y + blurSize < image.height) {
                        avgR += pixelsCopy2[(y + blurSize) * image.width + col].x;
                        avgG += pixelsCopy2[(y + blurSize) * image.width + col].y;
                        avgB += pixelsCopy2[(y + blurSize) * image.width + col].z;
                        avgAlpha += pixelsCopy2[(y + blurSize) * image.width + col].w;
                        convolutionSize++;
                    }

                    pixelsCopy1[y * image.width + col].x = (avgR / convolutionSize);
                    pixelsCopy1[y * image.width + col].y = (avgG / convolutionSize);
                    pixelsCopy1[y * image.width + col].z = (avgB / convolutionSize);
                    pixelsCopy1[y * image.width + col].w = (avgAlpha / convolutionSize);
                }
            }
        }


        // Reverse premultiply
        for (int i = 0; i < (image.width) * (image.height); i++) {
            if (pixelsCopy1[i].w == 0.0f) {
                pixels[i].r = 0;
                pixels[i].g = 0;
                pixels[i].b = 0;
                pixels[i].a = 0;
            }
            else if (pixelsCopy1[i].w <= 255.0f) {
                float alpha = pixelsCopy1[i].w / 255.0f;
                pixels[i].r = (int) (pixelsCopy1[i].x / alpha);
                pixels[i].g = (int) (pixelsCopy1[i].y / alpha);
                pixels[i].b = (int) (pixelsCopy1[i].z / alpha);
                pixels[i].a = (int) pixelsCopy1[i].w;
            }
        }

        rlPixelFormat format = image.format;

        result.setData(pixels);
        result.width = image.width;
        result.height = image.height;
        result.format = image.format;
        result.mipmaps = image.mipmaps;

        result = ImageFormat(result, format);

        return result;
    }

    /**
     * Generate all mipmap levels for a provided image <br/>
     * Supports POT and NPOT images <br/>
     * image.data is scaled to include mipmap levels
     * Mipmaps format is the same as base image
     *
     * @param image Image to generate mipmaps for
     */
    @Contract(mutates = "param")
    public void ImageMipmaps(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return;
        }

        int mipCount = 1;                  // Required mipmap levels count (including base level)
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

            context.tracelog.TRACELOG(null, "IMAGE: Next mipmap level: " + mipWidth + " x " + mipHeight + " - current size " + mipSize);

            mipCount++;
            mipSize += GetPixelDataSize(mipWidth, mipHeight, image.format);       // Add mipmap size (in bytes)
        }

        if (image.mipmaps < mipCount) {
            ByteBuffer temp = ByteBuffer.allocateDirect(image.data.capacity() + mipSize);
            for (int i = 0; i < image.data.capacity(); i++) {
                temp.put(i, image.data.get(i));
            }

            if (temp != null) {
                image.data = temp;      // Assign new pointer (new size) to store mipmaps data
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Mipmaps required memory could not be allocated");
            }

            // Pointer to allocated memory point where store next mipmap level data
            int nextmip = image.data.capacity() + GetPixelDataSize(image.width, image.height, image.format);

            mipWidth = image.width / 2;
            mipHeight = image.height / 2;
            mipSize = GetPixelDataSize(mipWidth, mipHeight, image.format);
            Image imCopy = ImageCopy(image);

            for (int i = 1; i < mipCount; i++) {
                context.tracelog.TRACELOG(null, "IMAGE: Generating mipmap level: " + i + " (" + mipWidth + " x " + mipHeight + ")" +
                        " - size: " + mipSize + " - offset: " + nextmip);

                imCopy = ImageResize(imCopy, mipWidth, mipHeight);  // Uses internally Mitchell cubic downscale filter

                nextmip = imCopy.data.capacity();
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
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Mipmaps already available");
        }
    }

    /**
     * Dither image data to 16bpp or lower (Floyd-Steinberg dithering) <br/>
     * NOTE: In case selected bpp does not represent a known 16bit format, dithered data is stored in the LSB part of the short
     *
     * @param image
     * @param rBpp
     * @param gBpp
     * @param bBpp
     * @param aBpp
     * @return
     */
    public Image ImageDither(Image image, int rBpp, int gBpp, int bBpp, int aBpp) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Compressed data formats can not be dithered");
            return image;
        }

        if ((rBpp + gBpp + bBpp + aBpp) > 16) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Unsupported dithering bpps (" + (rBpp + gBpp + bBpp + aBpp) + "bpp), only 16bpp or lower modes supported");
            return image;
        }
        else {
            Image result = ImageCopy(image);
            Color[] pixels = Color.FromPixels(LoadImageColors(result));


            if ((result.format != PIXELFORMAT_UNCOMPRESSED_R8G8B8) && (result.format != PIXELFORMAT_UNCOMPRESSED_R8G8B8A8)) {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Format is already 16bpp or lower, dithering could have no effect");
            }

            // Define new image format, check if desired bpp match internal known format
            if ((rBpp == 5) && (gBpp == 6) && (bBpp == 5) && (aBpp == 0)) {
                result.format = PIXELFORMAT_UNCOMPRESSED_R5G6B5;
            }
            else if ((rBpp == 5) && (gBpp == 5) && (bBpp == 5) && (aBpp == 1)) {
                result.format = PIXELFORMAT_UNCOMPRESSED_R5G5B5A1;
            }
            else if ((rBpp == 4) && (gBpp == 4) && (bBpp == 4) && (aBpp == 4)) {
                result.format = PIXELFORMAT_UNCOMPRESSED_R4G4B4A4;
            }
            else {
                result.format = null;
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Unsupported dithered OpenGL internal format: " +
                        (rBpp + gBpp + bBpp + aBpp) + "bpp (R" + rBpp + "G" + gBpp + "B" + bBpp + "A" + aBpp + ")");
            }

            // NOTE: We will store the dithered data as  short (16bpp)
            byte[] resultData = new byte[result.width * result.height * Short.BYTES];

            Color oldPixel = Color.WHITE;
            Color newPixel = Color.WHITE;

            int rError, gError, bError;
            short rPixel, gPixel, bPixel, aPixel;   // Used for 16bit pixel composition

            for (int y = 0; y < result.height; y++) {
                for (int x = 0; x < result.width; x++) {
                    oldPixel = pixels[y * result.width + x];

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

                    pixels[y * result.width + x] = newPixel;

                    // NOTE: Some cases are out of the array and should be ignored
                    if (x < (result.width - 1)) {
                        pixels[y * result.width + x + 1].r = (byte) Math.min(pixels[y * result.width + x + 1].r + (int) ((float) rError * 7.0f / 16), 0xff);
                        pixels[y * result.width + x + 1].g = (byte) Math.min(pixels[y * result.width + x + 1].g + (int) ((float) gError * 7.0f / 16), 0xff);
                        pixels[y * result.width + x + 1].b = (byte) Math.min(pixels[y * result.width + x + 1].b + (int) ((float) bError * 7.0f / 16), 0xff);
                    }

                    if ((x > 0) && (y < (result.height - 1))) {
                        pixels[(y + 1) * result.width + x - 1].r = (byte) Math.min(pixels[(y + 1) * result.width + x - 1].r + (int) ((float) rError * 3.0f / 16), 0xff);
                        pixels[(y + 1) * result.width + x - 1].g = (byte) Math.min(pixels[(y + 1) * result.width + x - 1].g + (int) ((float) gError * 3.0f / 16), 0xff);
                        pixels[(y + 1) * result.width + x - 1].b = (byte) Math.min(pixels[(y + 1) * result.width + x - 1].b + (int) ((float) bError * 3.0f / 16), 0xff);
                    }

                    if (y < (result.height - 1)) {
                        pixels[(y + 1) * result.width + x].r = (byte) Math.min(pixels[(y + 1) * result.width + x].r + (int) ((float) rError * 5.0f / 16), 0xff);
                        pixels[(y + 1) * result.width + x].g = (byte) Math.min(pixels[(y + 1) * result.width + x].g + (int) ((float) gError * 5.0f / 16), 0xff);
                        pixels[(y + 1) * result.width + x].b = (byte) Math.min(pixels[(y + 1) * result.width + x].b + (int) ((float) bError * 5.0f / 16), 0xff);
                    }

                    if ((x < (result.width - 1)) && (y < (result.height - 1))) {
                        pixels[(y + 1) * result.width + x + 1].r = (byte) Math.min(pixels[(y + 1) * result.width + x + 1].r + (int) ((float) rError * 1.0f / 16), 0xff);
                        pixels[(y + 1) * result.width + x + 1].g = (byte) Math.min(pixels[(y + 1) * result.width + x + 1].g + (int) ((float) gError * 1.0f / 16), 0xff);
                        pixels[(y + 1) * result.width + x + 1].b = (byte) Math.min(pixels[(y + 1) * result.width + x + 1].b + (int) ((float) bError * 1.0f / 16), 0xff);
                    }

                    rPixel = (byte) newPixel.r;
                    gPixel = (byte) newPixel.g;
                    bPixel = (byte) newPixel.b;
                    aPixel = (byte) newPixel.a;

                    resultData[y * result.width + x] = (byte) ((rPixel << (gBpp + bBpp + aBpp)) | (gPixel << (bBpp + aBpp)) | (bPixel << aBpp) | aPixel);
                }
            }

            UnloadImageColors(pixels);

            result.setData(resultData);
            return result;
        }
    }

    /**
     * Flip image vertically
     *
     * @param image Image to flip
     * @return Image flipped on the vertical axis
     */
    public Image ImageFlipVertical(Image image) {
        Image result = new Image();

        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }
        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else {
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] flippedData = new byte[image.width * image.height * bytesPerPixel];
            byte[] imgData = image.getData();

            for (int i = (image.height - 1), offsetSize = 0; i >= 0; i--) {
                System.arraycopy(imgData, i * image.width * bytesPerPixel, flippedData, offsetSize, image.width * bytesPerPixel);
                offsetSize += image.width * bytesPerPixel;
            }

            result.setData(flippedData);
            result.width = image.width;
            result.height = image.height;
            result.mipmaps = image.mipmaps;
            result.format = image.format;
        }
        return result;
    }

    /**
     * Flip image horizontally
     *
     * @param image Image to flip
     * @return Image flipped on the horizontal axis
     */
    public Image ImageFlipHorizontal(Image image) {
        Image result = new Image();

        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
        }
        else {
            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] flippedData = new byte[image.width * image.height * bytesPerPixel];
            byte[] imgData = image.getData();

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    //copy data pixel by pixel
                    if (bytesPerPixel >= 0) {
                        System.arraycopy(imgData, (y * image.width + (image.width - 1 - x)) * bytesPerPixel, flippedData, (y * image.width + x) * bytesPerPixel, bytesPerPixel);
                    }
                }
            }

            result.setData(flippedData);
            result.width = image.width;
            result.height = image.height;
            result.mipmaps = image.mipmaps;
            result.format = image.format;
        }

        return result;
    }

    /**
     * Rotate image by degrees
     *
     * @param image
     * @param degrees
     * @return Rotated image
     */
    public Image ImageRotate(Image image, int degrees) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
            return image;
        }
        else {
            float rad = (float) (degrees * Math.PI / 180.0f);
            float sinRadius = (float) Math.sin(rad);
            float cosRadius = (float) Math.cos(rad);

            int width = (int) (Math.abs(image.width * cosRadius) + Math.abs(image.height * sinRadius));
            int height = (int) (Math.abs(image.height * cosRadius) + Math.abs(image.width * sinRadius));

            int bytesPerPixel = GetPixelDataSize(1, 1, image.format);
            byte[] rotatedData = new byte[width * height * bytesPerPixel];
            byte[] imageData = image.getData();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    float oldX = ((x - width / 2.0f) * cosRadius + (y - height / 2.0f) * sinRadius) + image.width / 2.0f;
                    float oldY = ((y - height / 2.0f) * cosRadius - (x - width / 2.0f) * sinRadius) + image.height / 2.0f;

                    if ((oldX >= 0) && (oldX < image.width) && (oldY >= 0) && (oldY < image.height)) {
                        int x1 = (int) Math.floor(oldX);
                        int y1 = (int) Math.floor(oldY);
                        int x2 = Math.min(x1 + 1, image.width - 1);
                        int y2 = Math.min(y1 + 1, image.height - 1);

                        float px = oldX - x1;
                        float py = oldY - y1;

                        for (int i = 0; i < bytesPerPixel; i++) {
                            float f1 = imageData[(y1 * image.width + x1) * bytesPerPixel + i];
                            float f2 = imageData[(y1 * image.width + x2) * bytesPerPixel + i];
                            float f3 = imageData[(y2 * image.width + x1) * bytesPerPixel + i];
                            float f4 = imageData[(y2 * image.width + x2) * bytesPerPixel + i];

                            float val = f1 * (1 - px) * (1 - py) + f2 * px * (1 - py) + f3 * (1 - px) * py + f4 * px * py;

                            rotatedData[(y * width + x) * bytesPerPixel + i] = (byte) val;
                        }
                    }
                }
            }

            Image result = ImageCopy(image);
            result.setData(rotatedData);

            return result;
        }
    }

    /**
     * Rotate image clockwise by 90 deg
     *
     * @param image Image to rotate
     * @return Rotated image
     */
    public Image ImageRotateCW(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
            return image;
        }
        else {
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

            Image result = ImageCopy(image);
            result.setData(rotatedData);

            return result;
        }
    }

    /**
     * Rotate image counter-clockwise by 90 deg
     *
     * @param image Image to rotate
     * @return Rotated Image
     */
    public Image ImageRotateCCW(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (image.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation only applied to base mipmap level");
        }

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image manipulation not supported for compressed formats");
            return image;
        }
        else {
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

            Image result = ImageCopy(image);
            result.setData(rotatedData);

            return result;
        }
    }

    /**
     * Modify image color: tint
     *
     * @param image Base Image
     * @param color Tint to be applied
     * @return Image with tint applied
     */
    public Image ImageColorTint(Image image, Color color) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        Color[] pixels = Color.FromPixels(LoadImageColors(image));

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

        rlPixelFormat format = image.format;

        Image result = ImageCopy(image);
        result.setData(pixels);
        result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        return ImageFormat(result, format);
    }

    /**
     * Modify image color: invert
     *
     * @param image Base image
     * @return Image with colors inverted
     */
    public Image ImageColorInvert(Image image) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        Color[] pixels = Color.FromPixels(LoadImageColors(image));

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                pixels[y * image.width + x].r = (byte) (255 - pixels[y * image.width + x].r);
                pixels[y * image.width + x].g = (byte) (255 - pixels[y * image.width + x].g);
                pixels[y * image.width + x].b = (byte) (255 - pixels[y * image.width + x].b);
            }
        }

        rlPixelFormat format = image.format;

        Image result = ImageCopy(image);
        result.setData(pixels);
        result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        return ImageFormat(result, format);
    }

    /**
     * Modify image color: grayscale
     *
     * @param image Base image
     * @return Grayscaled image
     */
    public Image ImageColorGrayscale(Image image) {
        return ImageFormat(image, PIXELFORMAT_UNCOMPRESSED_GRAYSCALE);
    }

    /**
     * Modify image color: contrast
     *
     * @param image    Base image
     * @param contrast Amount to adjust contrast. Range of [-100 ... 100]
     * @return Image with contrast adjusted
     */
    public Image ImageColorContrast(Image image, float contrast) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (contrast < -100) {
            contrast = -100;
        }
        if (contrast > 100) {
            contrast = 100;
        }

        contrast = (100.0f + contrast) / 100.0f;
        contrast *= contrast;

        Color[] pixels = Color.FromPixels(LoadImageColors(image));

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                float pR = pixels[y * image.width + x].r / 255.0f;
                pR -= 0.5f;
                pR *= contrast;
                pR += 0.5f;
                pR *= 255;
                if (pR < 0) {
                    pR = 0;
                }
                if (pR > 255) {
                    pR = 255;
                }

                float pG = pixels[y * image.width + x].g / 255.0f;
                pG -= 0.5f;
                pG *= contrast;
                pG += 0.5f;
                pG *= 255;
                if (pG < 0) {
                    pG = 0;
                }
                if (pG > 255) {
                    pG = 255;
                }

                float pB = pixels[y * image.width + x].b / 255.0f;
                pB -= 0.5f;
                pB *= contrast;
                pB += 0.5f;
                pB *= 255;
                if (pB < 0) {
                    pB = 0;
                }
                if (pB > 255) {
                    pB = 255;
                }

                pixels[y * image.width + x].r = (byte) pR;
                pixels[y * image.width + x].g = (byte) pG;
                pixels[y * image.width + x].b = (byte) pB;
            }
        }

        rlPixelFormat format = image.format;

        Image result = ImageCopy(image);
        result.setData(pixels);
        result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        return ImageFormat(result, format);
    }

    /**
     * Modify image color: brightness
     *
     * @param image      Base Image
     * @param brightness Amount to adjust brightness by. Range of [-255 ... 255]
     * @return Image with brightness adjusted
     */
    public Image ImageColorBrightness(Image image, int brightness) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        if (brightness < -255) {
            brightness = -255;
        }
        if (brightness > 255) {
            brightness = 255;
        }

        Color[] pixels = Color.FromPixels(LoadImageColors(image));

        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int cR = pixels[y * image.width + x].r + brightness;
                int cG = pixels[y * image.width + x].g + brightness;
                int cB = pixels[y * image.width + x].b + brightness;

                if (cR < 0) {
                    cR = 1;
                }
                if (cR > 255) {
                    cR = 255;
                }

                if (cG < 0) {
                    cG = 1;
                }
                if (cG > 255) {
                    cG = 255;
                }

                if (cB < 0) {
                    cB = 1;
                }
                if (cB > 255) {
                    cB = 255;
                }

                pixels[y * image.width + x].r = (byte) cR;
                pixels[y * image.width + x].g = (byte) cG;
                pixels[y * image.width + x].b = (byte) cB;
            }
        }

        rlPixelFormat format = image.format;

        Image result = ImageCopy(image);
        result.setData(pixels);
        result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        return ImageFormat(result, format);
    }

    /**
     * Modify image color: replace color
     *
     * @param image   Base image
     * @param color   Color to replace
     * @param replace Replacement color
     * @return Image with specified color replaced
     */
    public Image ImageColorReplace(Image image, Color color, Color replace) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        Color[] pixels = Color.FromPixels(LoadImageColors(image));

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

        rlPixelFormat format = image.format;

        Image result = ImageCopy(image);
        result.setData(pixels);
        result.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;

        return ImageFormat(result, format);
    }

    // Load color data from image as a Color array (RGBA - 32bit)
    // NOTE: Memory allocated should be freed using UnloadImageColors();

    /**
     * Load color data from an image as an array of bytes (RGBA - 32bit) <br/>
     * Result can be transformed to an array of {@code Color} by use of the helper function: <br/>
     * {@code byte[] pixels = LoadImageColors(image);}<br/>{@code Color[] colors = Color.FromPixels(pixels);}
     *
     * @param image Source image
     * @return Color values formatted RGBA in an array
     */
    public byte[] LoadImageColors(Image image) {
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return null;
        }

        byte[] pixels = new byte[image.width * image.height * 4];

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else {
            if (
                    (image.format == PIXELFORMAT_UNCOMPRESSED_R32) ||
                            (image.format == PIXELFORMAT_UNCOMPRESSED_R32G32B32) ||
                            (image.format == PIXELFORMAT_UNCOMPRESSED_R32G32B32A32)
            ) {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Pixel format converted from 32bit to 8bit per channel");
            }
            if (
                    (image.format == PIXELFORMAT_UNCOMPRESSED_R16) ||
                            (image.format == PIXELFORMAT_UNCOMPRESSED_R16G16B16) ||
                            (image.format == PIXELFORMAT_UNCOMPRESSED_R16G16B16A16)
            ) {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Pixel format converted from 16bit to 8bit per channel");
            }

            byte[] imageData = image.getData();
            short pixel;

            for (int i = 0, k = 0; i < image.width * image.height * 4; i += 4) {
                switch (image.format) {
                    case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                        pixels[i] = imageData[k];
                        pixels[i + 1] = imageData[k];
                        pixels[i + 2] = imageData[k];
                        pixels[i + 3] = (byte) 255;

                        k++;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                        pixels[i] = imageData[k];
                        pixels[i + 1] = imageData[k];
                        pixels[i + 2] = imageData[k];
                        pixels[i + 3] = imageData[k + 1];

                        k += 2;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                        pixel = image.data.getShort(k);

                        pixels[i] = (byte) (((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                        pixels[i + 1] = (byte) (((pixel & 0b0000011111000000) >> 6) * (255 / 31));
                        pixels[i + 2] = (byte) (((pixel & 0b0000000000111110) >> 1) * (255 / 31));
                        pixels[i + 3] = (byte) ((pixel & 0b0000000000000001) * 255);

                        k += 2;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
                        pixel = image.data.getShort(k);

                        pixels[i] = (byte) (((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                        pixels[i + 1] = (byte) (((pixel & 0b0000011111000000) >> 6) * (255 / 31));
                        pixels[i + 2] = (byte) (((pixel & 0b0000000000111110)) * (255 / 31));
                        pixels[i + 3] = (byte) 255;

                        k += 2;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                        pixel = image.data.getShort(k);

                        pixels[i] = (byte) (((pixel & 0b1111100000000000) >> 12) * (255 / 15));
                        pixels[i + 1] = (byte) (((pixel & 0b0000011111000000) >> 8) * (255 / 15));
                        pixels[i + 2] = (byte) (((pixel & 0b0000000000111110) >> 4) * (255 / 15));
                        pixels[i + 3] = (byte) ((pixel & 0b0000000000000001) * (255 / 15));

                        k += 2;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                        pixels[i] = imageData[k];
                        pixels[i + 1] = imageData[k + 1];
                        pixels[i + 2] = imageData[k + 2];
                        pixels[i + 3] = imageData[k + 3];

                        k += 4;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                        pixels[i] = imageData[k];
                        pixels[i + 1] = imageData[k + 1];
                        pixels[i + 2] = imageData[k + 2];
                        pixels[i + 3] = (byte) 255;

                        k += 3;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R32:
                        pixels[i] = (byte) (image.data.getFloat(k) * 255.0f);
                        pixels[i + 1] = 0;
                        pixels[i + 2] = 0;
                        pixels[i + 3] = (byte) 255;

                        k += 4;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                        pixels[i] = (byte) (image.data.getFloat(k) * 255.0f);
                        pixels[i + 1] = (byte) (image.data.getFloat(k + 1 * Float.BYTES) * 255.0f);
                        pixels[i + 2] = (byte) (image.data.getFloat(k + 2 * Float.BYTES) * 255.0f);
                        pixels[i + 3] = (byte) 255;

                        k += 3 * Float.BYTES;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                        pixels[i] = (byte) (image.data.getFloat(k) * 255.0f);
                        pixels[i + 1] = (byte) (image.data.getFloat(k + 1 * Float.BYTES) * 255.0f);
                        pixels[i + 2] = (byte) (image.data.getFloat(k + 2 * Float.BYTES) * 255.0f);
                        pixels[i + 3] = (byte) (image.data.getFloat(k + 3 * Float.BYTES) * 255.0f);

                        k += 4 * Float.BYTES;
                        break;

                    case PIXELFORMAT_UNCOMPRESSED_R16:
                        pixels[i] = (byte) (HalfToFloat((short) (image.data.getShort(k) * 255.0f)));
                        pixels[i + 1] = 0;
                        pixels[i + 2] = 0;
                        pixels[i + 3] = (byte) 255;
                        break;
                    case PIXELFORMAT_UNCOMPRESSED_R16G16B16:
                        pixels[i] = (byte) (HalfToFloat((short) (image.data.getShort(k) * 255.0f)));
                        pixels[i + 1] = (byte) (HalfToFloat((short) (image.data.getShort(k + 1 * Short.BYTES) * 255.0f)));
                        pixels[i + 2] = (byte) (HalfToFloat((short) (image.data.getShort(k + 2 * Short.BYTES) * 255.0f)));
                        pixels[i + 3] = (byte) 255;
                        break;
                    case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                        pixels[i] = (byte) (HalfToFloat((short) (image.data.getShort(k) * 255.0f)));
                        pixels[i + 1] = (byte) (HalfToFloat((short) (image.data.getShort(k + 1 * Short.BYTES) * 255.0f)));
                        pixels[i + 2] = (byte) (HalfToFloat((short) (image.data.getShort(k + 2 * Short.BYTES) * 255.0f)));
                        pixels[i + 3] = (byte) (HalfToFloat((short) (image.data.getShort(k + 3 * Short.BYTES) * 255.0f)));
                        break;
                    default:
                        break;
                }
            }
        }

        return pixels;
    }

    /**
     * Load colors palette from image as a Color array (RGBA - 32bit)
     *
     * @param image          Image to load colors from
     * @param maxPaletteSize number of unique colors to load
     * @return Array of Colors from the source image
     */
    public Color[] LoadImagePalette(Image image, int maxPaletteSize) {
        int palCount = 0;
        Color[] palette = null;
        Color[] pixels = Color.FromPixels(LoadImageColors(image));

        if (pixels != null) {
            palette = new Color[maxPaletteSize];

            for (int i = 0; i < maxPaletteSize; i++) {
                palette[i] = new Color();   // Set all colors to BLANK
            }

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
                            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Palette is greater than " + maxPaletteSize + " colors");
                        }
                    }
                }
            }

            UnloadImageColors(pixels);
        }

        return palette;
    }

    /**
     * Unload color data loaded with {@code LoadImageColors()}
     *
     * @param colors color data to unload from memory
     */
    @Contract(mutates = "param")
    public void UnloadImageColors(Color[] colors) {
        colors = null;
    }

    /**
     * Unload color data loaded with {@code LoadImagePallete()}
     *
     * @param colors
     */
    @Contract(mutates = "param")
    public void unloadImagePalette(Color[] colors) {
        colors = null;
    }

    /**
     * Get image alpha border rectangle
     *
     * @param image     Source image
     * @param threshold Alpha threshold defined as a range of [0.0f ... 1.0f]
     * @return Border defined by alpha threshold
     */
    public Rectangle GetImageAlphaBorder(Image image, float threshold) {
        Rectangle crop = new Rectangle();

        Color[] pixels = Color.FromPixels(LoadImageColors(image));

        if (pixels != null) {
            int xMin = 65536;   // Define a big enough number
            int xMax = 0;
            int yMin = 65536;
            int yMax = 0;

            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    if (pixels[y * image.width + x].a > (threshold * 255.0f)) {
                        if (x < xMin) {
                            xMin = x;
                        }
                        if (x > xMax) {
                            xMax = x;
                        }
                        if (y < yMin) {
                            yMin = y;
                        }
                        if (y > yMax) {
                            yMax = y;
                        }
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

    /**
     * Get image pixel color at (x, y) position
     *
     * @param image Source image
     * @param x     Pixel X coordinate with respect to the top-left
     * @param y     Pixel Y coordinate with respect to the top-left
     * @return Color at (x, y)
     */
    public Color GetImageColor(Image image, int x, int y) {
        Color color = new Color();
        byte[] imgData = image.getData();
        short pixel;

        if ((x >= 0) && (x < image.width) && (y >= 0) && (y < image.height)) {
            switch (image.format) {
                case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                    color.r = imgData[y * image.width + x];
                    color.g = imgData[y * image.width + x];
                    color.b = imgData[y * image.width + x];
                    color.a = (byte) 255;
                    break;

                case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                    color.r = imgData[(y * image.width + x) * 2];
                    color.g = imgData[(y * image.width + x) * 2];
                    color.b = imgData[(y * image.width + x) * 2];
                    color.a = imgData[(y * image.width + x) * 2 + 1];
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                    pixel = image.data.getShort(y * image.width + x);

                    color.r = (byte) (((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                    color.g = (byte) (((pixel & 0b0000011111000000) >> 6) * (255 / 31));
                    color.b = (byte) (((pixel & 0b0000000000111110) >> 1) * (255 / 31));
                    color.a = (byte) ((pixel & 0b0000000000000001) * 255);
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
                    pixel = image.data.getShort(y * image.width + x);

                    color.r = (byte) (((pixel & 0b1111100000000000) >> 11) * (255 / 31));
                    color.g = (byte) (((pixel & 0b0000011111100000) >> 5) * (255 / 63));
                    color.b = (byte) ((pixel & 0b0000000000011111) * (255 / 31));
                    color.a = (byte) 255;

                    break;

                case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                    pixel = image.data.getShort(y * image.width + x);

                    color.r = (byte) (((pixel & 0b1111000000000000) >> 12) * (255 / 15));
                    color.g = (byte) (((pixel & 0b0000111100000000) >> 8) * (255 / 15));
                    color.b = (byte) (((pixel & 0b0000000011110000) >> 4) * (255 / 15));
                    color.a = (byte) ((pixel & 0b0000000000001111) * (255 / 15));
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                    color.r = imgData[(y * image.width + x) * 4];
                    color.g = imgData[(y * image.width + x) * 4 + 1];
                    color.b = imgData[(y * image.width + x) * 4 + 2];
                    color.a = imgData[(y * image.width + x) * 4 + 3];
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                    color.r = imgData[(y * image.width + x) * 3];
                    color.g = imgData[(y * image.width + x) * 3 + 1];
                    color.b = imgData[(y * image.width + x) * 3 + 2];
                    color.a = (byte) 255;
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R32:
                    color.r = (byte) (imgData[y * image.width + x] * 255.0f);
                    color.g = 0;
                    color.b = 0;
                    color.a = (byte) 255;
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                    color.r = (byte) (imgData[(y * image.width + x) * 3] * 255.0f);
                    color.g = (byte) (imgData[(y * image.width + x) * 3 + 1] * 255.0f);
                    color.b = (byte) (imgData[(y * image.width + x) * 3 + 2] * 255.0f);
                    color.a = (byte) 255;
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                    color.r = (byte) (imgData[(y * image.width + x) * 4] * 255.0f);
                    color.g = (byte) (imgData[(y * image.width + x) * 4] * 255.0f);
                    color.b = (byte) (imgData[(y * image.width + x) * 4] * 255.0f);
                    color.a = (byte) (imgData[(y * image.width + x) * 4] * 255.0f);
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R16:
                    color.r = (int) HalfToFloat((short) (image.data.getShort(y * image.width + x) * 255.0f));
                    color.g = 0;
                    color.b = 0;
                    color.a = 255;
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R16G16B16:
                    color.r = (int) HalfToFloat((short) (image.data.getShort(y * image.width + x) * 255.0f));
                    color.g = (int) HalfToFloat((short) (image.data.getShort((y * image.width + x) + 1 * Short.BYTES) * 255.0f));
                    color.b = (int) HalfToFloat((short) (image.data.getShort((y * image.width + x) + 2 * Short.BYTES) * 255.0f));
                    color.a = 255;
                    break;

                case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                    color.r = (int) HalfToFloat((short) (image.data.getShort(y * image.width + x) * 255.0f));
                    color.g = (int) HalfToFloat((short) (image.data.getShort((y * image.width + x) + 1 * Short.BYTES) * 255.0f));
                    color.b = (int) HalfToFloat((short) (image.data.getShort((y * image.width + x) + 2 * Short.BYTES) * 255.0f));
                    color.a = (int) HalfToFloat((short) (image.data.getShort((y * image.width + x) + 3 * Short.BYTES) * 255.0f));
                    break;

                default:
                    context.tracelog.TRACELOG(LOG_WARNING, "Compressed image format does not support color reading");
                    break;
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "Requested image pixel (" + x + ", " + y + ") out of bounds");
        }

        return color;
    }

    //------------------------------------------------------------------------------------
    // Image drawing functions
    //------------------------------------------------------------------------------------

    /**
     * Clear image background with given color
     *
     * @param image
     * @param color
     * @return
     */
    public Image ImageClearBackground(Image image, Color color) {
        // Security check to avoid program crash
        if ((image.getData() == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        Color[] data = new Color[image.width * image.height];

        for (int i = 0; i < image.width * image.height; ++i) {
            data[i] = color;
        }

        Image result = ImageCopy(image);
        result.setData(data);

        return result;
    }

    /**
     * Draw pixel within an image
     *
     * @param image
     * @param x
     * @param y
     * @param color
     * @return
     */
    public Image ImageDrawPixel(Image image, int x, int y, Color color) {
        Image result = ImageCopy(image);
        Vector4 col;
        Vector3 coln;
        int r, g, b, a;
        short gray;

        // Security check to avoid program crash
        if ((result.getData() == null) || (x < 0) || (x >= result.width) || (y < 0) || (y >= result.height)) {
            return null;
        }

        switch (result.format) {
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                // NOTE: Calculate grayscale equivalent color
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);
                gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                result.data.putShort(y * result.width + x, gray);
                break;

            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                // NOTE: Calculate grayscale equivalent color
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);
                gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                result.data.putShort((y * result.width + x) * 2, gray);
                result.data.put(y * result.width + x * 2 + 1, (byte) color.a);
                break;

            case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
                // NOTE: Calculate R5G6B5 equivalent color
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);

                r = (Math.round(coln.x * 31.0f));
                g = (Math.round(coln.y * 63.0f));
                b = (Math.round(coln.z * 31.0f));

                result.data.putInt(y * result.width + x, (r << 11 | g << 5 | b));
                break;

            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                // NOTE: Calculate R5G5B5A1 equivalent color
                col = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f, (float) color.a / 255.0f);

                r = (byte) (Math.round(col.x * 31.0f));
                g = (byte) (Math.round(col.y * 31.0f));
                b = (byte) (Math.round(col.z * 31.0f));
                a = (byte) ((col.x > (float) UNCOMPRESSED_R5G5B5A1_ALPHA_THRESHOLD / 255.0f) ? 1 : 0);

                result.data.putInt(y * result.width + x, (r << 11 | g << 6 | b << 1 | a));
                break;

            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                // NOTE: Calculate R5G5B5A1 equivalent color
                col = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f, (float) color.a / 255.0f);

                r = (byte) (Math.round(col.x * 15.0f));
                g = (byte) (Math.round(col.y * 15.0f));
                b = (byte) (Math.round(col.z * 15.0f));
                a = (byte) (Math.round(col.w * 15.0f));

                result.data.putInt(y * result.width + x, (r << 12 | g << 8 | b << 4 | a));
                break;

            case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                result.data.putInt((y * result.width + x) * 3, color.r);
                result.data.putInt((y * result.width + x) * 3 + 1, color.g);
                result.data.putInt((y * result.width + x) * 3 + 2, color.b);
                break;

            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                result.data.put((y * result.width + x) * 4, (byte) color.r);
                result.data.put((y * result.width + x) * 4 + 1, (byte) color.g);
                result.data.put((y * result.width + x) * 4 + 2, (byte) color.b);
                result.data.put((y * result.width + x) * 4 + 3, (byte) color.a);
                break;

            case PIXELFORMAT_UNCOMPRESSED_R32:
                // NOTE: Calculate grayscale equivalent color (normalized to 32bit)
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);
                result.data.putInt(y * result.width + x, (int) (coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f));
                break;

            case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                // NOTE: Calculate R32G32B32 equivalent color (normalized to 32bit)
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);

                result.data.putFloat((y * result.width + x) * 3, coln.x);
                result.data.putFloat((y * result.width + x) * 3 + 1, coln.y);
                result.data.putFloat((y * result.width + x) * 3 + 2, coln.z);
                break;

            case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                // NOTE: Calculate R32G32B32A32 equivalent color (normalized to 32bit)
                col = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f, (float) color.a / 255.0f);

                result.data.putFloat((y * result.width + x) * 4, col.x);
                result.data.putFloat((y * result.width + x) * 4 + 1, col.y);
                result.data.putFloat((y * result.width + x) * 4 + 2, col.z);
                result.data.putFloat((y * result.width + x) * 4 + 3, col.w);
                break;

            case PIXELFORMAT_UNCOMPRESSED_R16:
                // NOTE: Calculate grayscale equivalent color (normalized to 32bit)
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);
                result.data.putShort(y * result.width + x, FloatToHalf(coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f));
                break;

            case PIXELFORMAT_UNCOMPRESSED_R16G16B16:
                // NOTE: Calculate R32G32B32 equivalent color (normalized to 32bit)
                coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f);

                result.data.putShort((y * result.width + x) * 3, FloatToHalf(coln.x));
                result.data.putShort((y * result.width + x) * 3 + 1, FloatToHalf(coln.y));
                result.data.putShort((y * result.width + x) * 3 + 2, FloatToHalf(coln.z));
                break;

            case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                // NOTE: Calculate R32G32B32A32 equivalent color (normalized to 32bit)
                col = new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f, (float) color.b / 255.0f, (float) color.a / 255.0f);

                result.data.putShort((y * result.width + x) * 3, FloatToHalf(col.x));
                result.data.putShort((y * result.width + x) * 3 + 1, FloatToHalf(col.y));
                result.data.putShort((y * result.width + x) * 3 + 2, FloatToHalf(col.z));
                result.data.putShort((y * result.width + x) * 3 + 3, FloatToHalf(col.w));
                break;

            default:
                break;
        }

        return result;
    }

    /**
     * Draw pixel within an image
     *
     * @param image    Source image
     * @param position (x, y) position
     * @param color    Color pixel to draw
     * @return Drawn image
     */
    public Image ImageDrawPixelV(Image image, Vector2 position, Color color) {
        return ImageDrawPixel(image, (int) position.x, (int) position.y, color);
    }

    /**
     * Draw line within an image
     *
     * @param image     Source image
     * @param startPosX Start position x coordinate
     * @param startPosY Start position y coordinate
     * @param endPosX   End position x coordinate
     * @param endPosY   End position y coordinate
     * @param color     Color to draw line
     * @return Drawn image
     */
    public Image ImageDrawLine(Image image, int startPosX, int startPosY, int endPosX, int endPosY, Color color) {
        Image result = ImageCopy(image);

        int m = 2 * (endPosY - startPosY);
        int slopeError = m - (endPosX - startPosX);

        for (int x = startPosX, y = startPosY; x <= endPosX; x++) {
            result = ImageDrawPixel(result, x, y, color);

            slopeError += m;

            if (slopeError >= 0) {
                y++;
                slopeError -= 2 * (endPosX - startPosX);
            }
        }

        return result;
    }

    /**
     * Draw line within an image
     *
     * @param image Source image
     * @param start Start position (x, y) pair
     * @param end   End position (x, y) pair
     * @param color Color to draw line
     * @return Drawn Image
     */
    public Image ImageDrawLineV(Image image, Vector2 start, Vector2 end, Color color) {
        return ImageDrawLine(image, (int) start.x, (int) start.y, (int) end.x, (int) end.y, color);
    }

    /**
     * Draw circle within an image
     *
     * @param image   Source image
     * @param centerX Center x coordinate of the circle
     * @param centerY Center y coordinate of the circle
     * @param radius  Radius of circle in pixels
     * @param color   Color to draw the circle
     * @return Drawn image
     */
    public Image ImageDrawCircle(Image image, int centerX, int centerY, int radius, Color color) {
        Image result = new Image(image.getData(), image.width, image.height, image.format, image.mipmaps);

        int x = 0, y = radius;
        int decesionParameter = 3 - 2 * radius;

        while (y >= x) {
            result = ImageDrawRectangle(result, centerX - x, centerY + y, x * 2, 1, color);
            result = ImageDrawRectangle(result, centerX - x, centerY - y, x * 2, 1, color);
            result = ImageDrawRectangle(result, centerX - y, centerY + x, y * 2, 1, color);
            result = ImageDrawRectangle(result, centerX - y, centerY - x, y * 2, 1, color);
            x++;

            if (decesionParameter > 0) {
                y--;
                decesionParameter = decesionParameter + 4 * (x - y) + 10;
            }
            else {
                decesionParameter = decesionParameter + 4 * x + 6;
            }
        }

        return result;
    }

    /**
     * Draw circle within an image
     *
     * @param image  Source image
     * @param center Center position (x, y) coordinate
     * @param radius Radius of circle in pixels
     * @param color  Color to draw the circle
     * @return Drawn image
     */
    public Image ImageDrawCircleV(Image image, Vector2 center, int radius, Color color) {
        return ImageDrawCircle(image, (int) center.x, (int) center.y, radius, color);
    }

    // Draw circle outline within an image
    public Image ImageDrawCircleLines(Image image, int centerX, int centerY, int radius, Color color) {
        Image result = new Image(image.getData(), image.width, image.height, image.format, image.mipmaps);

        int x = 0;
        int y = radius;
        int decesionParameter = 3 - 2 * radius;

        while (y >= x) {
            result = ImageDrawPixel(result, centerX + x, centerY + y, color);
            result = ImageDrawPixel(result, centerX - x, centerY + y, color);
            result = ImageDrawPixel(result, centerX + x, centerY - y, color);
            result = ImageDrawPixel(result, centerX - x, centerY - y, color);
            result = ImageDrawPixel(result, centerX + y, centerY + x, color);
            result = ImageDrawPixel(result, centerX - y, centerY + x, color);
            result = ImageDrawPixel(result, centerX + y, centerY - x, color);
            result = ImageDrawPixel(result, centerX - y, centerY - x, color);
            x++;

            if (decesionParameter > 0) {
                y--;
                decesionParameter = decesionParameter + 4 * (x - y) + 10;
            }
            else {
                decesionParameter = decesionParameter + 4 * x + 6;
            }
        }

        return result;
    }

    /**
     * Draw rectangle within an image
     *
     * @param image  Source image
     * @param posX   X coordinate of the top left corner
     * @param posY   Y coordinate of the top left corner
     * @param width  Width of the recangle in pixels
     * @param height Height of the rectangle in pixels
     * @param color  Color to the draw the rectangle
     * @return Drawn image
     */
    public Image ImageDrawRectangle(Image image, int posX, int posY, int width, int height, Color color) {
        return ImageDrawRectangleRec(image, new Rectangle((float) posX, (float) posY, (float) width, (float) height), color);
    }

    /**
     * Draw rectangle within an image
     *
     * @param image    Source image
     * @param position (x, y) coordinate pair for the top left corner
     * @param size     (width, height) pair of the rectangle
     * @param color    Color to draw the rectangle
     * @return Drawn image
     */
    public Image ImageDrawRectangleV(Image image, Vector2 position, Vector2 size, Color color) {
        return ImageDrawRectangle(image, (int) position.x, (int) position.y, (int) size.x, (int) size.y, color);
    }

    /**
     * Draw rectangle within an image
     *
     * @param image Source image
     * @param rec   {@code Rectangle} defining the shape to be drawn
     * @param color Color to draw the rectangle
     * @return Drawn image
     */
    public Image ImageDrawRectangleRec(Image image, Rectangle rec, Color color) {
        // Security check to avoid program crash
        if ((image.data == null) || (image.width == 0) || (image.height == 0)) {
            return image;
        }

        // Security check to avoid drawing out of bounds in case of bad user data
        if (rec.x < 0) {
            rec.width -= rec.x;
            rec.x = 0;
        }
        if (rec.y < 0) {
            rec.height -= rec.y;
            rec.y = 0;
        }
        if (rec.width < 0) {
            rec.width = 0;
        }
        if (rec.height < 0) {
            rec.height = 0;
        }

        // Clamp the size to the image bounds
        if ((rec.x + rec.width) >= image.width) {
            rec.width = image.width - rec.x;
        }
        if ((rec.y + rec.height) >= image.height) {
            rec.height = image.height - rec.y;
        }

        // Check if the rect is even inside the image
        if ((rec.x > image.width) || (rec.y > image.height)) {
            return image;
        }
        if (((rec.x + rec.width) < 0) || (rec.y + rec.height < 0)) {
            return image;
        }

        int sy = (int) rec.y;
        int sx = (int) rec.x;

        int bytesPerPixel = GetPixelDataSize(1, 1, image.format);

        // Fill in the first pixel of the first row based on image format
        Image result = ImageDrawPixel(image, sx, sy, color);

        int bytesOffset = ((sy * result.width) + sx) * bytesPerPixel;
        byte[] srcData = image.getData();
        byte[] dstData = result.getData();

        // Repeat the first pixel data throughout the row
        for (int x = 1; x < (int) rec.width; x++) {
            System.arraycopy(srcData, x * bytesPerPixel, dstData, x * bytesPerPixel, bytesPerPixel);
        }

        // Repeat the first row data for all other rows
        int bytesPerRow = bytesPerPixel * (int) rec.width;
        for (int y = 1; y < (int) rec.height; y++) {
            System.arraycopy(srcData, y * bytesPerPixel, dstData, y * bytesPerPixel, bytesPerRow);
        }

        return result;
    }

    /**
     * Draw rectangle lines within an image
     *
     * @param image Source image
     * @param rec   {@code Rectangle} defining the shape to be drawn
     * @param thick Thickness of the lines in pixels
     * @param color Color to draw the lines
     * @return Drawn image
     */
    public Image ImageDrawRectangleLines(Image image, Rectangle rec, int thick, Color color) {
        Image result = new Image(image.getData(), image.width, image.height, image.format, image.mipmaps);

        result = ImageDrawRectangle(result, (int) rec.x, (int) rec.y, (int) rec.width, thick, color);
        result = ImageDrawRectangle(result, (int) rec.x, (int) (rec.y + thick), thick, (int) (rec.height - thick * 2), color);
        result = ImageDrawRectangle(result, (int) (rec.x + rec.width - thick), (int) (rec.y + thick), thick, (int) (rec.height - thick * 2), color);
        result = ImageDrawRectangle(result, (int) rec.x, (int) (rec.y + rec.height - thick), (int) rec.width, thick, color);

        return result;
    }

    /**
     * Draw an image (source) within an image (destination)
     *
     * @param dst    Destination image for image drawing
     * @param src    Source image to be drawn within the destination image
     * @param srcRec {@code Rectangle} defining the area in the source image to draw
     * @param dstRec {@code Rectangle} defining the area for where to draw in destination image
     * @param tint   Tint to apply to the source image
     * @return Drawn image
     */
    public Image ImageDraw(Image dst, Image src, Rectangle srcRec, Rectangle dstRec, Color tint) {
        Image result = new Image();

        // Security check to avoid program crash
        if ((dst.data == null) || (dst.width == 0) || (dst.height == 0) || (src.data == null) || (src.width == 0) || (src.height == 0)) {
            return result;
        }

        if (dst.mipmaps > 1) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image drawing only applied to base mipmap level");
        }

        if (dst.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "Image drawing not supported for compressed formats");
            return dst;
        }
        else {
            Image srcMod = src;       // Source copy (in case it was required)
            Image srcPtr = src;       // Pointer to source image
            boolean useSrcMod = false;     // Track source copy required

            // Source rectangle out-of-bounds security checks
            if (srcRec.x < 0) {
                srcRec.width += srcRec.x;
                srcRec.x = 0;
            }
            if (srcRec.y < 0) {
                srcRec.height += srcRec.y;
                srcRec.y = 0;
            }
            if ((srcRec.x + srcRec.width) > src.width) {
                srcRec.width = src.width - srcRec.x;
            }
            if ((srcRec.y + srcRec.height) > src.height) {
                srcRec.height = src.height - srcRec.y;
            }

            // Check if source rectangle needs to be resized to destination rectangle
            // In that case, we make a copy of source, and we apply all required transform
            if (((int) srcRec.width != (int) dstRec.width) || ((int) srcRec.height != (int) dstRec.height)) {
                srcMod = ImageFromImage(src, srcRec);   // Create image from another image
                srcMod = ImageResize(srcMod, (int) dstRec.width, (int) dstRec.height);   // Resize to destination rectangle
                srcRec = new Rectangle(0, 0, (float) srcMod.width, (float) srcMod.height);

                srcPtr = srcMod;
                useSrcMod = true;
            }

            // Destination rectangle out-of-bounds security checks
            if (dstRec.x < 0) {
                srcRec.x -= dstRec.x;
                srcRec.width += dstRec.x;
                dstRec.x = 0;
            }
            else if ((dstRec.x + srcRec.width) > dst.width) {
                srcRec.width = dst.width - dstRec.x;
            }

            if (dstRec.y < 0) {
                srcRec.y -= dstRec.y;
                srcRec.height += dstRec.y;
                dstRec.y = 0;
            }
            else if ((dstRec.y + srcRec.height) > dst.height) {
                srcRec.height = dst.height - dstRec.y;
            }

            if (dst.width < srcRec.width) {
                srcRec.width = (float) dst.width;
            }
            if (dst.height < srcRec.height) {
                srcRec.height = (float) dst.height;
            }

            // This blitting method is quite fast! The process followed is:
            // for every pixel -> [get_src_format/get_dst_format -> blend -> format_to_dst]
            // Some optimization ideas:
            //    [x] Avoid creating source copy if not required (no resize required)
            //    [x] Optimize ImageResize() for pixel format (alternative: ImageResizeNN())
            //    [x] Optimize ColorAlphaBlend() to avoid processing (alpha = 0) and (alpha = 1)
            //    [x] Optimize ColorAlphaBlend() for faster operations (maybe avoiding divs?)
            //    [x] Consider fast path: no alpha blending required cases (src has no alpha)
            //    [x] Consider fast path: same src/dst format with no alpha . direct line copy
            //    [-] GetPixelColor(): Get Vector4 instead of Color, easier for ColorAlphaBlend()
            //    [ ] Support f32bit channels drawing

            // TODO: Support PIXELFORMAT_UNCOMPRESSED_R32, PIXELFORMAT_UNCOMPRESSED_R32G32B32, PIXELFORMAT_UNCOMPRESSED_R32G32B32A32 and 16-bit equivalents

            Color colSrc, colDst, blend;
            boolean blendRequired = true;

            // Fast path: Avoid blend if source has no alpha to blend
            if ((tint.a == 255) && ((srcPtr.format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE) || (srcPtr.format == PIXELFORMAT_UNCOMPRESSED_R8G8B8) || (srcPtr.format == PIXELFORMAT_UNCOMPRESSED_R5G6B5))) {
                blendRequired = false;
            }

            int strideDst = GetPixelDataSize(dst.width, 1, dst.format);
            int bytesPerPixelDst = strideDst / (dst.width);

            int strideSrc = GetPixelDataSize(srcPtr.width, 1, srcPtr.format);
            int bytesPerPixelSrc = strideSrc / (srcPtr.width);

            int srcIndexBase = (int) ((srcRec.y * srcPtr.width + srcRec.x) * bytesPerPixelSrc);
            int dstIndexBase = (int) ((dstRec.y * dst.width + dstRec.x) * bytesPerPixelDst);

            byte[] dstData = dst.getData();
            byte[] srcData = srcPtr.getData();

            for (int y = 0; y < srcRec.height; y++) {

                int dstIndex = dstIndexBase;
                int srcIndex = srcIndexBase;

                if (!blendRequired && (srcPtr.format == dst.format)) {
                    System.arraycopy(srcData, srcIndex, dstData, dstIndex, (int) (srcRec.width) * bytesPerPixelSrc);
                }
                else {
                    for (int x = 0; x < srcRec.width; x++) {
                        byte[] srcPixelBuffer = new byte[bytesPerPixelSrc];
                        System.arraycopy(srcData, srcIndex, srcPixelBuffer, 0, bytesPerPixelSrc);

                        byte[] dstPixelBuffer = new byte[bytesPerPixelDst];
                        System.arraycopy(dstData, dstIndex, dstPixelBuffer, 0, bytesPerPixelDst);

                        colSrc = GetPixelColor(srcPixelBuffer, srcPtr.format);
                        colDst = GetPixelColor(dstPixelBuffer, dst.format);

                        // Fast path: Avoid blend if source has no alpha to blend
                        if (blendRequired) {
                            blend = ColorAlphaBlend(colDst, colSrc, tint);
                        }
                        else {
                            blend = colSrc;
                        }

                        byte[] pixelBuffer = SetPixelColor(blend, dst.format);
                        System.arraycopy(pixelBuffer, 0, dstData, dstIndex, pixelBuffer.length);

                        dstIndex += bytesPerPixelDst;
                        srcIndex += bytesPerPixelSrc;
                    }
                }

                srcIndexBase += strideSrc;
                dstIndexBase += strideDst;
            }

            result.setData(dstData);
            result.width = dst.width;
            result.height = dst.height;
            result.mipmaps = dst.mipmaps;
            result.format = dst.format;

            if (useSrcMod) {
                UnloadImage(srcMod);     // Unload source modified image
            }
        }

        return result;
    }

    /**
     * Draw text using raylib default font within an image
     *
     * @param image    Source image
     * @param text     String to draw on source image
     * @param posX     X coordinate for where to draw text, with respect to the top left of the first character
     * @param posY     Y coordinate for where to draw text, with respect to the top left of the first character
     * @param fontSize Font size in pixels
     * @param color    Color to draw font
     * @return Drawn image
     */
    public Image ImageDrawText(Image image, String text, int posX, int posY, int fontSize, Color color) {
        Vector2 position = new Vector2((float) posX, (float) posY);

        // NOTE: For default font, sapcing is set to desired font size / default font size (10)
        return ImageDrawTextEx(image, context.text.GetFontDefault(), text, position, (float) fontSize, (float) fontSize / 10, color);
    }

    /**
     * Draw text using raylib default font within an image
     *
     * @param image    Source image
     * @param font     Font to draw
     * @param text     String to draw on the source image
     * @param position (x, y) coordinate pair for where to draw text, with respect to the top left of the first character
     * @param fontSize Font size in pixels
     * @param spacing  Spacing between characters in pixels
     * @param tint     Color to tint the font
     * @return Drawn image
     */
    public Image ImageDrawTextEx(Image image, Font font, String text, Vector2 position, float fontSize, float spacing, Color tint) {
        Image imText = ImageTextEx(font, text, fontSize, spacing, tint);

        Rectangle srcRec = new Rectangle(0.0f, 0.0f, (float) imText.width, (float) imText.height);
        Rectangle dstRec = new Rectangle(position.x, position.y, (float) imText.width, (float) imText.height);

        return ImageDraw(image, imText, srcRec, dstRec, new Color(255, 255, 255, 255));
    }

    //------------------------------------------------------------------------------------
    // Texture loading functions
    //------------------------------------------------------------------------------------

    /**
     * Load texture from file into GPU memory (VRAM)
     *
     * @param fileName File location to load
     * @return
     */
    public Texture2D LoadTexture(String fileName) {
        Texture2D texture = new Texture2D();
        Image image = LoadImage(fileName);

        if (image.data != null) {
            texture = LoadTextureFromImage(image);
            UnloadImage(image);
        }

        return texture;
    }

    /**
     * Load texture from {@code Image} data
     *
     * @param image
     * @return
     */
    public Texture2D LoadTextureFromImage(Image image) {
        Texture2D texture = new Texture2D();

        if ((image.data != null) && (image.width != 0) && (image.height != 0)) {
            texture.id = context.rlgl.rlLoadTexture(image.getData(), image.width, image.height, image.format, image.mipmaps);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Data is not valid to load texture");
        }

        texture.width = image.width;
        texture.height = image.height;
        texture.mipmaps = image.mipmaps;
        texture.format = image.format;

        return texture;
    }

    /**
     * Load cubemap from image, multiple image cubemap layouts supported
     *
     * @param image
     * @param layoutType
     * @return
     */
    public Texture2D LoadTextureCubemap(Image image, CubemapLayoutType layoutType) {
        Texture2D cubemap = new Texture2D();

        // Try to automatically guess layout type
        if (layoutType == CUBEMAP_AUTO_DETECT) {
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
        }
        else {
            if (layoutType == CUBEMAP_LINE_VERTICAL) {
                cubemap.width = image.height / 6;
            }

            if (layoutType == CUBEMAP_LINE_HORIZONTAL) {
                cubemap.width = image.width / 6;
            }

            if (layoutType == CUBEMAP_CROSS_THREE_BY_FOUR) {
                cubemap.width = image.width / 3;
            }

            if (layoutType == CUBEMAP_CROSS_FOUR_BY_THREE) {
                cubemap.width = image.width / 4;
            }

            if (layoutType == CUBEMAP_PANORAMA) {
                cubemap.width = image.width / 4;
            }
        }

        cubemap.height = cubemap.width;

        if (layoutType != CUBEMAP_AUTO_DETECT) {
            int size = cubemap.width;

            Image faces = new Image();                // Vertical column image
            Rectangle[] faceRecs = new Rectangle[6];      // Face source rectangles
            for (int i = 0; i < 6; i++) {
                faceRecs[i] = new Rectangle(0, 0, (float) size, (float) size);
            }

            if (layoutType == CUBEMAP_LINE_VERTICAL) {
                faces = image;
                for (int i = 0; i < 6; i++) {
                    faceRecs[i].y = (float) size * i;
                }
            }
            else if (layoutType == CUBEMAP_PANORAMA) {
                // TODO: Convert panorama image to square faces...
                // Ref: https://github.com/denivip/panorama/blob/master/panorama.cpp
            }
            else {
                if (layoutType == CUBEMAP_LINE_HORIZONTAL) {
                    for (int i = 0; i < 6; i++) {
                        faceRecs[i].x = (float) size * i;
                    }
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
                faces = ImageFormat(faces, image.format);

                // NOTE: Image formating does not work with compressed textures!
            }

            for (int i = 0; i < 6; i++) {
                faces = ImageDraw(faces, image, faceRecs[i], new Rectangle(0, (float) size * i, (float) size, (float) size), Color.WHITE);
            }

            // NOTE: Cubemap data is expected to be provided as 6 images in a single data array,
            // one after the other (that's a vertical image), following convention: +X, -X, +Y, -Y, +Z, -Z
            cubemap.id = context.rlgl.rlLoadTextureCubemap(faces.getData(), size, faces.format, faces.mipmaps);
            if (cubemap.id == 0) {
                context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Failed to load cubemap image");
            }

            UnloadImage(faces);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Failed to detect cubemap image layout");
        }

        return cubemap;
    }

    /**
     * Load texture for rendering (framebuffer)
     *
     * @param width  Width of render texture
     * @param height Height of render texture
     * @return Render texture loaded with RGBA color attachment and depth RenderBuffer
     */
    public RenderTexture LoadRenderTexture(int width, int height) {
        RenderTexture target = new RenderTexture();

        target.id = context.rlgl.rlLoadFramebuffer();   // Load an empty framebuffer

        if (target.id > 0) {
            context.rlgl.rlEnableFramebuffer(target.id);

            // Create color texture (default to RGBA)
            target.texture.id = context.rlgl.rlLoadTexture(null, width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
            target.texture.width = width;
            target.texture.height = height;
            target.texture.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
            target.texture.mipmaps = 1;

            // Create depth renderbuffer/texture
            target.depth.id = context.rlgl.rlLoadTextureDepth(width, height, true);
            target.depth.width = width;
            target.depth.height = height;
            target.depth.format = PIXELFORMAT_COMPRESSED_PVRT_RGBA;       //DEPTH_COMPONENT_24BIT?
            target.depth.mipmaps = 1;

            // Attach color texture and depth renderbuffer/texture to FBO
            context.rlgl.rlFramebufferAttach(target.id, target.texture.id, ATTACHMENT_COLOR_CHANNEL0,
                                             ATTACHMENT_TEXTURE2D);
            context.rlgl.rlFramebufferAttach(target.id, target.depth.id, ATTACHMENT_DEPTH, ATTACHMENT_RENDERBUFFER);

            // Check if fbo is complete with attachments (valid)
            if (context.rlgl.rlFramebufferComplete(target.id)) {
                context.tracelog.TRACELOG(LOG_INFO, "FBO: [ID " + target.id + "] Framebuffer object created successfully");
            }

            context.rlgl.rlDisableFramebuffer();
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FBO: Framebuffer object can not be created");
        }

        return target;
    }

    /**
     * Check if a texture is ready
     *
     * @param texture Texture to evaluate
     * @return {@code true} if texture has all requisite data
     */
    public boolean IsTextureReady(Texture2D texture) {
        return texture.id > 0 && texture.width > 0 && texture.height > 0 && texture.format.GetFormat() > 0;
    }

    /**
     * Unload texture from GPU memory (VRAM)
     *
     * @param texture Texture to unload from VRAM
     */
    public void UnloadTexture(Texture2D texture) {
        if (texture.getId() > 0) {
            context.rlgl.rlUnloadTexture(texture.getId());

            context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Unloaded texture data from VRAM (GPU)");
        }
    }

    /**
     * Check if a render texture is ready
     *
     * @param target Reder texture to evaluate
     * @return {@code true} if the render texture has all requisite data
     */
    public boolean IsRenderTextureReady(RenderTexture target) {
        return target.id > 0 && IsTextureReady(target.depth) && IsTextureReady(target.texture);
    }

    /**
     * Unload render texture from GPU memory (VRAM)
     *
     * @param target Texture to unload from VRAM
     */
    public void UnloadRenderTexture(RenderTexture target) {
        if (target.texture.id > 0) {
            // Color texture attached to FBO is deleted
            context.rlgl.rlUnloadTexture(target.texture.id);

            // NOTE: Depth texture/renderbuffer is automatically
            // queried and deleted before deleting framebuffer
            context.rlgl.rlUnloadFramebuffer(target.id);
        }
    }

    /**
     * Update GPU texture with new data
     *
     * @param texture Texture to update
     * @param pixels  Pixel data to upload. Must be the same format as the texture.
     */
    public void UpdateTexture(Texture2D texture, byte[] pixels) {
        context.rlgl.rlUpdateTexture(texture.id, 0, 0, texture.width, texture.height, texture.format, pixels);
    }

    /**
     * Update GPU texture rectangle with new data
     *
     * @param texture Texture to update
     * @param rec     Region of texture to update
     * @param pixels  Pixel data to upload. Must be the same format as the texture.
     */
    public void UpdateTextureRec(Texture2D texture, Rectangle rec, byte[] pixels) {
        context.rlgl.rlUpdateTexture(texture.id, (int) rec.x, (int) rec.y, (int) rec.width, (int) rec.height,
                                     texture.format, pixels);
    }

    /**
     * Get pixel data from GPU texture
     *
     * @param texture Texture data to retrieve
     * @return Image from texture data
     */
    public Image LoadImageFromTexture(Texture2D texture) {
        Image image = new Image();

        if (texture.format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            image.setData(context.rlgl.rlReadTexturePixels(texture.id, texture.width, texture.height, texture.format));

            if (image.data != null) {
                image.width = texture.width;
                image.height = texture.height;
                image.format = texture.format;
                image.mipmaps = 1;

                if (context.rlgl.rlGetVersion() == OPENGL_ES_20) {
                    // NOTE: Data retrieved on OpenGL ES 2.0 should be RGBA,
                    // coming from FBO color buffer attachment, but it seems
                    // original texture format is retrieved on RPI...
                    image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
                }
                context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + texture.id + "] Pixel data retrieved successfully");
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: [ID " + texture.id + "] Failed to retrieve pixel data");
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: [ID " + texture.id + "] Failed to retrieve compressed pixel data");
        }

        return image;
    }

    /**
     * Get pixel data from GPU front buffer and return an Image (screenshot)
     *
     * @return Image from screen data
     */
    public Image LoadImageFromScreen() {
        Image image = new Image();

        image.width = context.core.GetScreenWidth();
        image.height = context.core.GetScreenHeight();
        image.mipmaps = 1;
        image.format = PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
        image.setData(context.rlgl.rlReadScreenPixels(image.width, image.height));

        return image;
    }

    //------------------------------------------------------------------------------------
    // Texture configuration functions
    //------------------------------------------------------------------------------------

    /**
     * Generate GPU mipmaps for a texture
     *
     * @param texture Texture to generate Mipmaps for
     */
    public void GenTextureMipmaps(Texture2D texture) {
        // NOTE: NPOT textures support check inside function
        // On WebGL (OpenGL ES 2.0) NPOT textures support is limited
        context.rlgl.rlGenTextureMipmaps(texture);
    }

    /**
     * Set texture scaling filter mode
     *
     * @param texture
     * @param filterMode
     */
    public void SetTextureFilter(Texture2D texture, rlTextureFilterMode filterMode) {
        switch (filterMode) {

            case TEXTURE_FILTER_POINT: {
                if (texture.mipmaps > 1) {
                    // RL_FILTER_MIP_NEAREST - tex filter: POINT, mipmaps filter: POINT (sharp switching between mipmaps)
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_MIP_NEAREST);

                    // RL_FILTER_NEAREST - tex filter: POINT (no filter), no mipmaps
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_NEAREST);
                }
                else {
                    // RL_FILTER_NEAREST - tex filter: POINT (no filter), no mipmaps
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_NEAREST);
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_NEAREST);
                }
            }
            break;
            case TEXTURE_FILTER_BILINEAR: {
                if (texture.mipmaps > 1) {
                    // RL_FILTER_LINEAR_MIP_NEAREST - tex filter: BILINEAR, mipmaps filter: POINT (sharp switching between mipmaps)
                    // Alternative: RL_FILTER_NEAREST_MIP_LINEAR - tex filter: POINT, mipmaps filter: BILINEAR (smooth transition between mipmaps)
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR_MIP_NEAREST);

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
                else {
                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR);
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
            }
            break;
            case TEXTURE_FILTER_TRILINEAR: {
                if (texture.mipmaps > 1) {
                    // RL_FILTER_MIP_LINEAR - tex filter: BILINEAR, mipmaps filter: BILINEAR (smooth transition between mipmaps)
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_MIP_LINEAR);

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
                else {
                    context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: [ID " + texture.id
                            + "] No mipmaps available for TRILINEAR texture filtering");

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR);
                    context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
                }
            }
            break;
            case TEXTURE_FILTER_ANISOTROPIC_4X:
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_FILTER_ANISOTROPIC, 4);
                break;
            case TEXTURE_FILTER_ANISOTROPIC_8X:
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_FILTER_ANISOTROPIC, 8);
                break;
            case TEXTURE_FILTER_ANISOTROPIC_16X:
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_FILTER_ANISOTROPIC, 16);
                break;
            default:
                break;
        }
    }

    // Set texture wrapping mode
    public void SetTextureWrap(Texture2D texture, int wrapMode) {
        switch (wrapMode) {
            case RL_TEXTURE_WRAP_REPEAT: {
                // NOTE: It only works if NPOT textures are supported, i.e. OpenGL ES 2.0 could not support it
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_REPEAT);
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_REPEAT);
            }
            break;
            case RL_TEXTURE_WRAP_CLAMP: {
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_CLAMP);
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_CLAMP);
            }
            break;
            case RL_TEXTURE_WRAP_MIRROR_REPEAT: {
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_MIRROR_REPEAT);
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_MIRROR_REPEAT);
            }
            break;
            case RL_TEXTURE_WRAP_MIRROR_CLAMP: {
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_S, RL_TEXTURE_WRAP_MIRROR_CLAMP);
                context.rlgl.rlTextureParameters(texture.id, RL_TEXTURE_WRAP_T, RL_TEXTURE_WRAP_MIRROR_CLAMP);
            }
            break;
            default:
                break;
        }
    }

    //------------------------------------------------------------------------------------
    // Texture drawing functions
    //------------------------------------------------------------------------------------

    /**
     * Draw a Texture2D
     *
     * @param texture Texture to render
     * @param posX    X position to render the texture with respect to the top left of the texture
     * @param posY    Y position to render the texture with respect to the top left of the texture
     * @param tint    Tint to be applied to the texture
     */
    public void DrawTexture(Texture2D texture, int posX, int posY, Color tint) {
        DrawTextureEx(texture, new Vector2((float) posX, (float) posY), 0.0f, 1.0f, tint);
    }

    /**
     * Draw a Texture2D with position defined as Vector2
     *
     * @param texture  Texture to render
     * @param position (x, y) coordinate pair to render the texture with respect to the top left of the texture
     * @param tint     Tint to be applied to the texture
     */
    public void DrawTextureV(Texture2D texture, Vector2 position, Color tint) {
        DrawTextureEx(texture, position, 0, 1.0f, tint);
    }

    /**
     * Draw a Texture2D with extended parameters
     *
     * @param texture  Texture to render
     * @param position (x, y) coordinate pair to render the texture with respect to the top left of the texture
     * @param rotation Degrees to rotate the texture by
     * @param scale    Value to scale the texture by
     * @param tint     Tint to be applied to the texture
     */
    public void DrawTextureEx(Texture2D texture, Vector2 position, float rotation, float scale, Color tint) {
        Rectangle source = new Rectangle(0.0f, 0.0f, (float) texture.width, (float) texture.height);
        Rectangle dest = new Rectangle(position.x, position.y, (float) texture.width * scale,
                                       (float) texture.height * scale);
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, dest, origin, rotation, tint);
    }

    /**
     * Draw a part of a texture (defined by a rectangle)
     *
     * @param texture  Source texture
     * @param source   Area of the texture to render
     * @param position (x, y) coordinate pair to render the texture with respect to the top left of the texture
     * @param tint     Tint to be applied to the texture
     */
    public void DrawTextureRec(Texture2D texture, Rectangle source, Vector2 position, Color tint) {
        Rectangle dest = new Rectangle(position.x, position.y, Math.abs(source.width),
                                       Math.abs(source.height));
        Vector2 origin = new Vector2(0.0f, 0.0f);

        DrawTexturePro(texture, source, dest, origin, 0.0f, tint);
    }

    /**
     * Draw a part of a texture (defined by a rectangle) with 'pro' parameters <br/>
     * NOTE: origin is relative to destination rectangle size
     *
     * @param texture  Source texture
     * @param source
     * @param dest
     * @param origin
     * @param rotation Degrees to rotate the texture by
     * @param tint     Tint to be applied to the texture
     */
    public void DrawTexturePro(Texture2D texture, Rectangle source, Rectangle dest, Vector2 origin, float rotation, Color tint) {
        // Check if texture is valid
        if (texture.id > 0) {
            float width = (float) texture.width;
            float height = (float) texture.height;

            boolean flipX = false;

            if (source.width < 0) {
                flipX = true;
                source.width *= -1;
            }
            if (source.height < 0) {
                source.y -= source.height;
            }

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
            else {
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

            context.rlgl.rlCheckRenderBatchLimit(4);     // Make sure there is enough free space on the batch buffer

            context.rlgl.rlSetTexture(texture.id);
            context.rlgl.rlBegin(RL_QUADS);

            context.rlgl.rlColor4ub(tint.r, tint.g, tint.b, tint.a);
            context.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);                          // Normal vector pointing towards viewer

            // Top-left corner for texture and quad
            if (flipX) {
                context.rlgl.rlTexCoord2f((source.x + source.width) / width, source.y / height);
            }
            else {
                context.rlgl.rlTexCoord2f(source.x / width, source.y / height);
            }
            context.rlgl.rlVertex2f(topLeft.x, topLeft.y);

            // Bottom-left corner for texture and quad
            if (flipX) {
                context.rlgl.rlTexCoord2f((source.x + source.width) / width, (source.y + source.height) / height);
            }
            else {
                context.rlgl.rlTexCoord2f(source.x / width, (source.y + source.height) / height);
            }
            context.rlgl.rlVertex2f(bottomLeft.x, bottomLeft.y);

            // Bottom-right corner for texture and quad
            if (flipX) {
                context.rlgl.rlTexCoord2f(source.x / width, (source.y + source.height) / height);
            }
            else {
                context.rlgl.rlTexCoord2f((source.x + source.width) / width, (source.y + source.height) / height);
            }
            context.rlgl.rlVertex2f(bottomRight.x, bottomRight.y);

            // Top-right corner for texture and quad
            if (flipX) {
                context.rlgl.rlTexCoord2f(source.x / width, source.y / height);
            }
            else {
                context.rlgl.rlTexCoord2f((source.x + source.width) / width, source.y / height);
            }
            context.rlgl.rlVertex2f(topRight.x, topRight.y);

            context.rlgl.rlEnd();
            context.rlgl.rlSetTexture(0);
        }
    }

    /**
     * Draws a texture (or part of it) that stretches or shrinks nicely using n-patch info
     *
     * @param texture    Texture to render
     * @param nPatchInfo N-patch configuration
     * @param dstRec
     * @param origin
     * @param rotation   Degrees to rotate the texture by
     * @param tint       Tint to be applied to the texture
     */
    public void DrawTextureNPatch(Texture2D texture, NPatchInfo nPatchInfo, Rectangle dstRec, Vector2 origin, float rotation, Color tint) {
        if (texture.id > 0) {
            float width = (float) texture.width;
            float height = (float) texture.height;

            float patchWidth = ((int) dstRec.width <= 0) ? 0.0f : dstRec.width;
            float patchHeight = ((int) dstRec.height <= 0) ? 0.0f : dstRec.height;

            if (nPatchInfo.source.width < 0) {
                nPatchInfo.source.x -= nPatchInfo.source.width;
            }
            if (nPatchInfo.source.height < 0) {
                nPatchInfo.source.y -= nPatchInfo.source.height;
            }
            if (nPatchInfo.layout == NPATCH_THREE_PATCH_HORIZONTAL) {
                patchHeight = nPatchInfo.source.height;
            }
            if (nPatchInfo.layout == NPATCH_THREE_PATCH_VERTICAL) {
                patchWidth = nPatchInfo.source.width;
            }

            boolean drawCenter = true;
            boolean drawMiddle = true;
            float leftBorder = (float) nPatchInfo.left;
            float topBorder = (float) nPatchInfo.top;
            float rightBorder = (float) nPatchInfo.right;
            float bottomBorder = (float) nPatchInfo.bottom;

            // Adjust the lateral (left and right) border widths in case patchWidth < texture.width
            if (patchWidth <= (leftBorder + rightBorder) && nPatchInfo.layout != NPATCH_THREE_PATCH_VERTICAL) {
                drawCenter = false;
                leftBorder = (leftBorder / (leftBorder + rightBorder)) * patchWidth;
                rightBorder = patchWidth - leftBorder;
            }

            // Adjust the lateral (top and bottom) border heights in case patchHeight < texture.height
            if (patchHeight <= (topBorder + bottomBorder) && nPatchInfo.layout != NPATCH_THREE_PATCH_HORIZONTAL) {
                drawMiddle = false;
                topBorder = (topBorder / (topBorder + bottomBorder)) * patchHeight;
                bottomBorder = patchHeight - topBorder;
            }


            Vector2 vertA = new Vector2();
            Vector2 vertB = new Vector2();
            Vector2 vertC = new Vector2();
            Vector2 vertD = new Vector2();
            vertA.x = 0.0f;                             // Outer left
            vertA.y = 0.0f;                             // Outer top
            vertB.x = leftBorder;                       // Inner left
            vertB.y = topBorder;                        // Inner top
            vertC.x = patchWidth - rightBorder;        // Inner right
            vertC.y = patchHeight - bottomBorder;       // Inner bottom
            vertD.x = patchWidth;                       // Outer right
            vertD.y = patchHeight;                      // Outer bottom

            Vector2 coordA = new Vector2();
            Vector2 coordB = new Vector2();
            Vector2 coordC = new Vector2();
            Vector2 coordD = new Vector2();
            coordA.x = nPatchInfo.source.x / width;
            coordA.y = nPatchInfo.source.y / height;
            coordB.x = (nPatchInfo.source.x + leftBorder) / width;
            coordB.y = (nPatchInfo.source.y + topBorder) / height;
            coordC.x = (nPatchInfo.source.x + nPatchInfo.source.width - rightBorder) / width;
            coordC.y = (nPatchInfo.source.y + nPatchInfo.source.height - bottomBorder) / height;
            coordD.x = (nPatchInfo.source.x + nPatchInfo.source.width) / width;
            coordD.y = (nPatchInfo.source.y + nPatchInfo.source.height) / height;

            context.rlgl.rlSetTexture(texture.id);

            context.rlgl.rlPushMatrix();
            context.rlgl.rlTranslatef(dstRec.x, dstRec.y, 0.0f);
            context.rlgl.rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
            context.rlgl.rlTranslatef(-origin.x, -origin.y, 0.0f);

            context.rlgl.rlBegin(RL_QUADS);
            context.rlgl.rlColor4ub(tint.r, tint.g, tint.b, tint.a);
            context.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);               // Normal vector pointing towards viewer

            if (nPatchInfo.layout == NPATCH_NINE_PATCH) {
                // ------------------------------------------------------------
                // TOP-LEFT QUAD
                context.rlgl.rlTexCoord2f(coordA.x, coordB.y); context.rlgl.rlVertex2f(vertA.x, vertB.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordB.x, coordB.y); context.rlgl.rlVertex2f(vertB.x, vertB.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordB.x, coordA.y); context.rlgl.rlVertex2f(vertB.x, vertA.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordA.x, coordA.y); context.rlgl.rlVertex2f(vertA.x, vertA.y);  // Top-left corner for texture and quad
                if (drawCenter) {
                    // TOP-CENTER QUAD
                    context.rlgl.rlTexCoord2f(coordB.x, coordB.y); context.rlgl.rlVertex2f(vertB.x, vertB.y);  // Bottom-left corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordB.y); context.rlgl.rlVertex2f(vertC.x, vertB.y);  // Bottom-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordA.y); context.rlgl.rlVertex2f(vertC.x, vertA.y);  // Top-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordB.x, coordA.y); context.rlgl.rlVertex2f(vertB.x, vertA.y);  // Top-left corner for texture and quad
                }
                // TOP-RIGHT QUAD
                context.rlgl.rlTexCoord2f(coordC.x, coordB.y); context.rlgl.rlVertex2f(vertC.x, vertB.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordB.y); context.rlgl.rlVertex2f(vertD.x, vertB.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordA.y); context.rlgl.rlVertex2f(vertD.x, vertA.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordC.x, coordA.y); context.rlgl.rlVertex2f(vertC.x, vertA.y);  // Top-left corner for texture and quad
                if (drawMiddle) {
                    // ------------------------------------------------------------
                    // MIDDLE-LEFT QUAD
                    context.rlgl.rlTexCoord2f(coordA.x, coordC.y); context.rlgl.rlVertex2f(vertA.x, vertC.y);  // Bottom-left corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordB.x, coordC.y); context.rlgl.rlVertex2f(vertB.x, vertC.y);  // Bottom-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordB.x, coordB.y); context.rlgl.rlVertex2f(vertB.x, vertB.y);  // Top-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordA.x, coordB.y); context.rlgl.rlVertex2f(vertA.x, vertB.y);  // Top-left corner for texture and quad
                    if (drawCenter) {
                        // MIDDLE-CENTER QUAD
                        context.rlgl.rlTexCoord2f(coordB.x, coordC.y); context.rlgl.rlVertex2f(vertB.x, vertC.y);  // Bottom-left corner for texture and quad
                        context.rlgl.rlTexCoord2f(coordC.x, coordC.y); context.rlgl.rlVertex2f(vertC.x, vertC.y);  // Bottom-right corner for texture and quad
                        context.rlgl.rlTexCoord2f(coordC.x, coordB.y); context.rlgl.rlVertex2f(vertC.x, vertB.y);  // Top-right corner for texture and quad
                        context.rlgl.rlTexCoord2f(coordB.x, coordB.y); context.rlgl.rlVertex2f(vertB.x, vertB.y);  // Top-left corner for texture and quad
                    }

                    // MIDDLE-RIGHT QUAD
                    context.rlgl.rlTexCoord2f(coordC.x, coordC.y); context.rlgl.rlVertex2f(vertC.x, vertC.y);  // Bottom-left corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordD.x, coordC.y); context.rlgl.rlVertex2f(vertD.x, vertC.y);  // Bottom-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordD.x, coordB.y); context.rlgl.rlVertex2f(vertD.x, vertB.y);  // Top-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordB.y); context.rlgl.rlVertex2f(vertC.x, vertB.y);  // Top-left corner for texture and quad
                }

                // ------------------------------------------------------------
                // BOTTOM-LEFT QUAD
                context.rlgl.rlTexCoord2f(coordA.x, coordD.y); context.rlgl.rlVertex2f(vertA.x, vertD.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordB.x, coordD.y); context.rlgl.rlVertex2f(vertB.x, vertD.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordB.x, coordC.y); context.rlgl.rlVertex2f(vertB.x, vertC.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordA.x, coordC.y); context.rlgl.rlVertex2f(vertA.x, vertC.y);  // Top-left corner for texture and quad
                if (drawCenter) {
                    // BOTTOM-CENTER QUAD
                    context.rlgl.rlTexCoord2f(coordB.x, coordD.y); context.rlgl.rlVertex2f(vertB.x, vertD.y);  // Bottom-left corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordD.y); context.rlgl.rlVertex2f(vertC.x, vertD.y);  // Bottom-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordC.y); context.rlgl.rlVertex2f(vertC.x, vertC.y);  // Top-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordB.x, coordC.y); context.rlgl.rlVertex2f(vertB.x, vertC.y);  // Top-left corner for texture and quad
                }

                // BOTTOM-RIGHT QUAD
                context.rlgl.rlTexCoord2f(coordC.x, coordD.y); context.rlgl.rlVertex2f(vertC.x, vertD.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordD.y); context.rlgl.rlVertex2f(vertD.x, vertD.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordC.y); context.rlgl.rlVertex2f(vertD.x, vertC.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordC.x, coordC.y); context.rlgl.rlVertex2f(vertC.x, vertC.y);  // Top-left corner for texture and quad
            }
            else if (nPatchInfo.layout == NPATCH_THREE_PATCH_VERTICAL) {
                // TOP QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                context.rlgl.rlTexCoord2f(coordA.x, coordB.y); context.rlgl.rlVertex2f(vertA.x, vertB.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordB.y); context.rlgl.rlVertex2f(vertD.x, vertB.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordA.y); context.rlgl.rlVertex2f(vertD.x, vertA.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordA.x, coordA.y); context.rlgl.rlVertex2f(vertA.x, vertA.y);  // Top-left corner for texture and quad
                if (drawCenter) {
                    // MIDDLE QUAD
                    // -----------------------------------------------------------
                    // Texture coords                 Vertices
                    context.rlgl.rlTexCoord2f(coordA.x, coordC.y); context.rlgl.rlVertex2f(vertA.x, vertC.y);  // Bottom-left corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordD.x, coordC.y); context.rlgl.rlVertex2f(vertD.x, vertC.y);  // Bottom-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordD.x, coordB.y); context.rlgl.rlVertex2f(vertD.x, vertB.y);  // Top-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordA.x, coordB.y); context.rlgl.rlVertex2f(vertA.x, vertB.y);  // Top-left corner for texture and quad
                }
                // BOTTOM QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                context.rlgl.rlTexCoord2f(coordA.x, coordD.y); context.rlgl.rlVertex2f(vertA.x, vertD.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordD.y); context.rlgl.rlVertex2f(vertD.x, vertD.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordC.y); context.rlgl.rlVertex2f(vertD.x, vertC.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordA.x, coordC.y); context.rlgl.rlVertex2f(vertA.x, vertC.y);  // Top-left corner for texture and quad
            }
            else if (nPatchInfo.layout == NPATCH_THREE_PATCH_HORIZONTAL) {
                // LEFT QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                context.rlgl.rlTexCoord2f(coordA.x, coordD.y); context.rlgl.rlVertex2f(vertA.x, vertD.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordB.x, coordD.y); context.rlgl.rlVertex2f(vertB.x, vertD.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordB.x, coordA.y); context.rlgl.rlVertex2f(vertB.x, vertA.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordA.x, coordA.y); context.rlgl.rlVertex2f(vertA.x, vertA.y);  // Top-left corner for texture and quad
                if (drawCenter) {
                    // CENTER QUAD
                    // -----------------------------------------------------------
                    // Texture coords                 Vertices
                    context.rlgl.rlTexCoord2f(coordB.x, coordD.y); context.rlgl.rlVertex2f(vertB.x, vertD.y);  // Bottom-left corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordD.y); context.rlgl.rlVertex2f(vertC.x, vertD.y);  // Bottom-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordC.x, coordA.y); context.rlgl.rlVertex2f(vertC.x, vertA.y);  // Top-right corner for texture and quad
                    context.rlgl.rlTexCoord2f(coordB.x, coordA.y); context.rlgl.rlVertex2f(vertB.x, vertA.y);  // Top-left corner for texture and quad
                }
                // RIGHT QUAD
                // -----------------------------------------------------------
                // Texture coords                 Vertices
                context.rlgl.rlTexCoord2f(coordC.x, coordD.y); context.rlgl.rlVertex2f(vertC.x, vertD.y);  // Bottom-left corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordD.y); context.rlgl.rlVertex2f(vertD.x, vertD.y);  // Bottom-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordD.x, coordA.y); context.rlgl.rlVertex2f(vertD.x, vertA.y);  // Top-right corner for texture and quad
                context.rlgl.rlTexCoord2f(coordC.x, coordA.y); context.rlgl.rlVertex2f(vertC.x, vertA.y);  // Top-left corner for texture and quad
            }
            context.rlgl.rlEnd();
            context.rlgl.rlPopMatrix();

            context.rlgl.rlSetTexture(0);
        }
    }

    /**
     * Returns color with alpha applied,
     *
     * @param color Source color
     * @param alpha Transparency factor ranging from [0.0f - 1.0f]
     * @return Faded color
     */
    public Color Fade(Color color, float alpha) {
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        else if (alpha > 1.0f) {
            alpha = 1.0f;
        }

        return new Color(color.r, color.g, color.b, (int) (255.0f * alpha));
    }

    /**
     * Returns hexadecimal value for a Color
     *
     * @param color Source Color
     * @return Color value in hex format
     */
    public int ColorToInt(Color color) {
        return ((color.r << 24) | (color.g << 16) | (color.b << 8) | color.a);
    }

    /**
     * Returns color normalized as float [0..1]
     *
     * @param color Source color
     * @return Normalized value
     */
    public Vector4 ColorNormalize(Color color) {
        return new Vector4((float) color.r / 255.0f, (float) color.g / 255.0f,
                           (float) color.b / 255.0f, (float) color.a / 255.0f);
    }

    /**
     * Returns color from normalized values
     *
     * @param normalized Source value, normalized from [0.0f ... 1.0f]
     * @return {@code Color} defined by value
     */
    public Color ColorFromNormalized(Vector4 normalized) {
        return new Color((int) normalized.x * 255, (int) normalized.y * 255, (int) normalized.z * 255, (int) normalized.z * 255);
    }

    /**
     * Returns HSV values for a Color
     *
     * @param color Source color
     * @return {@code Vector3} defined (Hue, Saturation, Value)
     */
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
        else {
            // NOTE: If max is 0, then r = g = b = 0, s = 0, h is undefined
            hsv.y = 0.0f;
            hsv.x = 0;        // Undefined
            return hsv;
        }

        // NOTE: Comparing float values could not work properly
        if (rgb.x >= max) {
            hsv.x = (rgb.y - rgb.z) / delta;    // Between yellow & magenta
        }
        else {
            if (rgb.y >= max) {
                hsv.x = 2.0f + (rgb.z - rgb.x) / delta;  // Between cyan & yellow
            }
            else {
                hsv.x = 4.0f + (rgb.x - rgb.y) / delta;      // Between magenta & cyan
            }
        }

        hsv.x *= 60.0f;     // Convert to degrees

        if (hsv.x < 0.0f) {
            hsv.x += 360.0f;
        }

        return hsv;
    }

    /**
     * Returns a Color from HSV values
     *
     * @param hue        Hue of color in degrees [0 ... 360]
     * @param saturation Normalized saturation value [0.0f ... 1.0f]
     * @param value      Normalized color value [0.0f ... 1.0f]
     * @return {@code Color} from provided values. Color->HSV->Color conversion will not yield exactly the same color due to rounding errors
     * @see <a href="https://en.wikipedia.org/wiki/HSL_and_HSV#Alternative_HSV_conversion">HSV Conversion</a>
     */
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

    /**
     * Get color multiplied with another color
     *
     * @param color Primary color
     * @param tint  Secondary color
     * @return {@code Color} resulting from parameters
     */
    public Color ColorTint(Color color, Color tint) {
        Color result = new Color();

        float cR = (float) tint.r / 255;
        float cG = (float) tint.g / 255;
        float cB = (float) tint.b / 255;
        float cA = (float) tint.a / 255;

        int r = (int) (((float) color.r / 255 * cR) * 255.0f);
        int g = (int) (((float) color.g / 255 * cG) * 255.0f);
        int b = (int) (((float) color.b / 255 * cB) * 255.0f);
        int a = (int) (((float) color.a / 255 * cA) * 255.0f);

        result.r = r;
        result.g = g;
        result.b = b;
        result.a = a;

        return result;
    }

    /**
     * Returns color with alpha applied
     *
     * @param color Source color
     * @param alpha Transparency value provided as a normalized float [0.0f ... 1.0f]
     * @return Color defined by source and alpha provided
     */
    public Color ColorAlpha(Color color, float alpha) {
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        else if (alpha > 1.0f) {
            alpha = 1.0f;
        }

        return new Color(color.r, color.g, color.b, (int) (255.0f * alpha));
    }

    /**
     * Returns src alpha-blended into dst color with tint
     *
     * @param dst
     * @param src  Source color
     * @param tint Tint to apply to src
     * @return {@code Color} defined by the parameters
     */
    public Color ColorAlphaBlend(Color dst, Color src, Color tint) {
        Color out = new Color(255, 255, 255, 255);
        Color source = new Color();

        // Apply color tint to source color
        source.r = ((src.r * (tint.r + 1)) >> 8);
        source.g = ((src.g * (tint.g + 1)) >> 8);
        source.b = ((src.b * (tint.b + 1)) >> 8);
        source.a = ((src.a * (tint.a + 1)) >> 8);

        boolean COLORALPHABLEND_FLOAT = false;
        boolean COLORALPHABLEND_INTEGERS = true;
        if (COLORALPHABLEND_INTEGERS) {
            if (source.a == 0) {
                out = new Color(dst.r, dst.g, dst.b, dst.a);
            }
            else if (source.a == 255) {
                out = source;
            }
            else {
                int alpha = source.a + 1;
                // We are shifting by 8 (dividing by 256), so we need to take that excess into account

                out.a = ((byte) ((alpha * 256 + dst.a * (256 - alpha)) >> 8));

                if (out.a > 0) {
                    out.r = ((byte) (((source.r * alpha * 256 + dst.r * dst.a * (256 - alpha)) / out.a) >> 8));
                    out.g = ((byte) (((source.g * alpha * 256 + dst.g * dst.a * (256 - alpha)) / out.a) >> 8));
                    out.b = ((byte) (((source.b * alpha * 256 + dst.b * dst.a * (256 - alpha)) / out.a) >> 8));
                }
            }
        }
        if (COLORALPHABLEND_FLOAT) {
            if (source.a == 0) {
                out = dst;
            }
            else if (source.a == 255) {
                out = source;
            }
            else {
                Vector4 fdst = ColorNormalize(dst);
                Vector4 fsrc = ColorNormalize(src);
                Vector4 ftint = ColorNormalize(tint);
                Vector4 fout = new Vector4();

                fout.w = (fsrc.w + fdst.w * (1.0f - fsrc.w));

                if (fout.w > 0.0f) {
                    fout.x = (fsrc.x * fsrc.w + fdst.x * fdst.w * (1 - fsrc.w) / fout.w);
                    fout.y = (fsrc.y * fsrc.w + fdst.y * fdst.w * (1 - fsrc.w) / fout.w);
                    fout.z = (fsrc.z * fsrc.w + fdst.z * fdst.w * (1 - fsrc.w) / fout.w);
                }

                out = new Color((int) (fout.x * 255.0f), (int) (fout.y * 255.0f), (int) (fout.z * 255.0f), (int) (fout.w * 255.0f));
            }
        }

        return out;
    }

    /**
     * Returns a Color from hexadecimal value
     *
     * @param hexValue Hex value of color, as RRGGBBAA
     * @return {@code Color} defined by hex value
     */
    public Color GetColor(int hexValue) {
        return new Color((hexValue >> 24) & 0xFF, (hexValue >> 16) & 0xFF, (hexValue >> 8) & 0xFF, hexValue & 0xFF);
    }

    /**
     * Get Color from a pixel, defined by format
     *
     * @param srcPtr Pixel data
     * @param format {@code rlPixelFormat} defining pixel structure
     * @return Color defined by pixel format
     */
    public Color GetPixelColor(byte[] srcPtr, rlPixelFormat format) {
        Color color = new Color();

        switch (format) {
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                color = new Color(Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[0]), 255);
                break;
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                color = new Color(Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[1]));
                break;
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
                color.r = Byte.toUnsignedInt((byte) ((srcPtr[0] >> 11) * 255 / 31));
                color.g = Byte.toUnsignedInt((byte) (((srcPtr[0] >> 5) & 0b0000000000111111) * 255 / 63));
                color.b = Byte.toUnsignedInt((byte) ((srcPtr[0] & 0b0000000000011111) * 255 / 31));
                color.a = Byte.toUnsignedInt((byte) 255);
                break;
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                color.r = Byte.toUnsignedInt((byte) ((srcPtr[0] >> 11) * 255 / 31));
                color.g = Byte.toUnsignedInt((byte) (((srcPtr[0] >> 6) & 0b0000000000011111) * 255 / 31));
                color.b = Byte.toUnsignedInt((byte) ((srcPtr[0] & 0b0000000000011111) * 255 / 31));
                color.a = Byte.toUnsignedInt((byte) ((srcPtr[0] & 0b0000000000000001) == 1 ? 255 : 0));
                break;
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                color.r = Byte.toUnsignedInt((byte) ((srcPtr[0] >> 11) * 255 / 15));
                color.g = Byte.toUnsignedInt((byte) (((srcPtr[0] >> 8) & 0b0000000000001111) * 255 / 15));
                color.b = Byte.toUnsignedInt((byte) (((srcPtr[0] >> 4) & 0b0000000000001111) * 255 / 15));
                color.a = Byte.toUnsignedInt((byte) ((srcPtr[0] & 0b0000000000001111) * 255 / 15));
                break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                color = new Color(Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[1]), Byte.toUnsignedInt(srcPtr[2]), Byte.toUnsignedInt(srcPtr[3]));
                break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                color = new Color(Byte.toUnsignedInt(srcPtr[0]), Byte.toUnsignedInt(srcPtr[1]), Byte.toUnsignedInt(srcPtr[2]), 255);
                break;
            case PIXELFORMAT_UNCOMPRESSED_R32:
                // NOTE: Pixel normalized float value is converted to [0..255]
                color.r = (int) (srcPtr[0] * 255.0f);
                color.g = (int) (srcPtr[0] * 255.0f);
                color.b = (int) (srcPtr[0] * 255.0f);
                color.a = 255;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                // NOTE: Pixel normalized float value is converted to [0..255]
                color.r = (int) (srcPtr[0] * 255.0f);
                color.g = (int) (srcPtr[1] * 255.0f);
                color.b = (int) (srcPtr[2] * 255.0f);
                color.a = 255;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                // NOTE: Pixel normalized float value is converted to [0..255]
                color.r = (int) (srcPtr[0] * 255.0f);
                color.g = (int) (srcPtr[1] * 255.0f);
                color.b = (int) (srcPtr[2] * 255.0f);
                color.a = (int) (srcPtr[3] * 255.0f);
                break;
            default:
                break;
        }

        return color;
    }

    /**
     * Set pixel color formatted into destination pointer
     *
     * @param color
     * @param format
     * @return
     */
    private byte[] SetPixelColor(Color color, rlPixelFormat format) {
        byte[] result = null;
        switch (format) {
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE: {
                result = new byte[1];
                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);
                byte gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                result[0] = gray;

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA: {
                result = new byte[2];

                // NOTE: Calculate grayscale equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);
                byte gray = (byte) ((coln.x * 0.299f + coln.y * 0.587f + coln.z * 0.114f) * 255.0f);

                result[0] = gray;
                result[1] = (byte) color.a;

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5: {
                result = new byte[1];

                // NOTE: Calculate R5G6B5 equivalent color
                Vector3 coln = new Vector3((float) color.r / 255.0f, (float) color.g / 255.0f,
                                           (float) color.b / 255.0f);

                char r = (char) (Math.round(coln.x * 31.0f));
                char g = (char) (Math.round(coln.y * 63.0f));
                char b = (char) (Math.round(coln.z * 31.0f));

                result[0] = (byte) (r << 11 | g << 5 | b);

            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1: {
                result = new byte[1];

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
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4: {
                result = new byte[1];

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
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8: {
                result = new byte[3];

                result[0] = (byte) color.r;
                result[1] = (byte) color.g;
                result[2] = (byte) color.b;
            }
            break;
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8: {
                result = new byte[4];

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

    /**
     * Get pixel data size in bytes for certain format
     *
     * @param width  Width of pixels
     * @param height Height of pixels
     * @param format {@code rlPixelFormat} defining pixel format
     * @return Data size in bytes
     */
    public int GetPixelDataSize(int width, int height, rlPixelFormat format) {
        int dataSize;       // Size in bytes
        int bpp = 0;            // Bits per pixel

        switch (format) {
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
            case PIXELFORMAT_UNCOMPRESSED_R16:
                bpp = 16;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R16G16B16:
                bpp = 16 * 3;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                bpp = 16 * 4;
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
        if ((width < 4) && (height < 4)) {
            if ((format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) && (format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT3_RGBA.GetFormat())) {
                dataSize = 8;
            }
            else if ((format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT3_RGBA.GetFormat()) && (format.GetFormat() < PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA.GetFormat())) {
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

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    /**
     * Convert f16 to f32
     *
     * @param x f16 to convert, 2 bytes of data
     * @return f32
     */
    private float HalfToFloat(short x) {
        int mant = x & 0x03ff; // 10 bits mantissa
        int exp = x & 0x7c00;  // 5 bits exponent

        if (exp == 0x7c00) {       // NaN or Infinity
            exp = 0x3fc00;
        }
        else if (exp != 0) {     // Normalized number
            exp += 0x1c000;        // Exponent bias adjustment
            if (mant == 0 && exp == 0x1c400) {
                return Float.intBitsToFloat((x & 0x8000) << 16 | exp << 13 | 0x3ff);
            }
        }
        else if (mant != 0) {    // Subnormal number
            exp = 0x1c400;
            do {
                mant <<= 1;
                exp -= 0x400;
            }
            while ((mant & 0x400) == 0);
            mant &= 0x3ff;
        }

        return Float.intBitsToFloat((x & 0x8000) << 16 | (exp | mant) << 13);
    }

    /**
     * Convert f32 to f16
     *
     * @param x float to convert
     * @return f16, 2 bytes of data
     */
    private short FloatToHalf(float x) {
        int fbits = Float.floatToIntBits(x);
        int sign = (fbits >>> 16) & 0x8000;          // Extract sign bit
        int val = (fbits & 0x7FFFFFFF) + 0x1000;     // Extract mantissa + round-to-nearest bit

        if (val >= 0x47800000) {                     // Check for overflow / NaN
            if ((fbits & 0x7FFFFFFF) >= 0x47800000) {
                if (val < 0x7F800000 && (fbits & 0x7FFFFF) != 0) {
                    return (short) (sign | 0x7E00);  // Preserve quiet NaN
                }
                return (short) (sign | 0x7C00);      // Return Infinity
            }
            return (short) (sign | 0x7BFF);          // Clamp to max half-float value
        }
        if (val >= 0x38800000) {                     // Normal case
            return (short) (sign | ((val - 0x38000000) >>> 13));
        }
        if (val < 0x33000000) {                      // Underflow case
            return (short) sign;                     // Return signed zero
        }
        val = (fbits & 0x7FFFFFFF) >>> 23;           // Handle subnormal spacing
        return (short) (sign | ((((fbits & 0x7FFFFF) | 0x800000) + (0x800000 >>> (val - 102))) >>> (126 - val)));
    }

    /**
     * Get pixel data from image as Vector4 array (float normalized)
     *
     * @param image Image to load data from
     * @return Array of {@code Vector4}s containing float normalised pixel data
     */
    public Vector4[] LoadImageDataNormalized(Image image) {
        Vector4[] pixels = new Vector4[image.width * image.height];

        if (image.format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: Pixel data retrieval not supported for compressed image formats");
        }
        else {
            byte[] imgData = image.getData();
            for (int i = 0, k = 0; i < image.width * image.height; i++) {
                pixels[i] = new Vector4();
                switch (image.format) {
                    case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE: {
                        pixels[i].x = (Byte.toUnsignedInt(imgData[i]) / 255.0f);
                        pixels[i].y = (Byte.toUnsignedInt(imgData[i]) / 255.0f);
                        pixels[i].z = (Byte.toUnsignedInt(imgData[i]) / 255.0f);
                        pixels[i].w = (1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA: {
                        pixels[i].x = (Byte.toUnsignedInt(imgData[k]) / 255.0f);
                        pixels[i].y = (Byte.toUnsignedInt(imgData[k]) / 255.0f);
                        pixels[i].z = (Byte.toUnsignedInt(imgData[k]) / 255.0f);
                        pixels[i].w = (Byte.toUnsignedInt(imgData[k + 1]) / 255.0f);

                        k += 2;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1: {
                        short pixel = imgData[i];

                        pixels[i].x = ((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].y = ((float) ((pixel & 0b0000011111000000) >> 6) * (1.0f / 31));
                        pixels[i].z = ((float) ((pixel & 0b0000000000111110) >> 1) * (1.0f / 31));
                        pixels[i].w = (((pixel & 0b0000000000000001) == 0) ? 0.0f : 1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R5G6B5: {
                        short pixel = imgData[i];

                        pixels[i].x = ((float) ((pixel & 0b1111100000000000) >> 11) * (1.0f / 31));
                        pixels[i].y = ((float) ((pixel & 0b0000011111100000) >> 5) * (1.0f / 63));
                        pixels[i].z = ((float) (pixel & 0b0000000000011111) * (1.0f / 31));
                        pixels[i].w = (1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4: {
                        short pixel = imgData[i];

                        pixels[i].x = ((float) ((pixel & 0b1111000000000000) >> 12) * (1.0f / 15));
                        pixels[i].y = ((float) ((pixel & 0b0000111100000000) >> 8) * (1.0f / 15));
                        pixels[i].z = ((float) ((pixel & 0b0000000011110000) >> 4) * (1.0f / 15));
                        pixels[i].w = ((float) (pixel & 0b0000000000001111) * (1.0f / 15));

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8: {
                        pixels[i].x = (Byte.toUnsignedInt(imgData[k]) / 255.0f);
                        pixels[i].y = (Byte.toUnsignedInt(imgData[k + 1]) / 255.0f);
                        pixels[i].z = (Byte.toUnsignedInt(imgData[k + 2]) / 255.0f);
                        pixels[i].w = (Byte.toUnsignedInt(imgData[k + 3]) / 255.0f);

                        k += 4;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R8G8B8: {
                        pixels[i].x = (Byte.toUnsignedInt(imgData[k]) / 255.0f);
                        pixels[i].y = (Byte.toUnsignedInt(imgData[k + 1]) / 255.0f);
                        pixels[i].z = (Byte.toUnsignedInt(imgData[k + 2]) / 255.0f);
                        pixels[i].w = (1.0f);

                        k += 3;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32: {
                        pixels[i].x = (imgData[k]);
                        pixels[i].y = (0.0f);
                        pixels[i].z = (0.0f);
                        pixels[i].w = (1.0f);

                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32: {
                        pixels[i].x = (Byte.toUnsignedInt(imgData[k]));
                        pixels[i].y = (Byte.toUnsignedInt(imgData[k + 1]));
                        pixels[i].z = (Byte.toUnsignedInt(imgData[k + 2]));
                        pixels[i].w = (1.0f);

                        k += 3;
                    }
                    break;
                    case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32: {
                        pixels[i].x = (Byte.toUnsignedInt(imgData[i]));
                        pixels[i].y = (Byte.toUnsignedInt(imgData[k + 1]));
                        pixels[i].z = (Byte.toUnsignedInt(imgData[k + 2]));
                        pixels[i].w = (Byte.toUnsignedInt(imgData[k + 3]));

                        k += 4;
                    }
                    default:
                        break;
                }
            }
        }

        return pixels;
    }
}
