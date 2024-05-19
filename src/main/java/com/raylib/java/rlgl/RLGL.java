package com.raylib.java.rlgl;

import com.raylib.java.raymath.Matrix;
import com.raylib.java.rlgl.data.rlglData;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.utils.Tracelog;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.raylib.java.Config.*;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.*;
import static com.raylib.java.rlgl.RLGL.rlGlVersion.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_SWIZZLE_RGBA;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL41.GL_RGB565;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengles.GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
import static org.lwjgl.opengles.OESDepth24.GL_DEPTH_COMPONENT24_OES;
import static org.lwjgl.opengles.OESDepth32.GL_DEPTH_COMPONENT32_OES;

public class RLGL{

    //*********************
    //GL API VERSION
    //*********************
    public static boolean GRAPHICS_API_OPENGL_11 = false;
    private static boolean GRAPHICS_API_OPENGL_21 = false;
    public static boolean GRAPHICS_API_OPENGL_33 = true;
    public static boolean GRAPHICS_API_OPENGL_43 = false;
    public static boolean GRAPHICS_API_OPENGL_ES2 = false;
    private static final boolean RLGL_RENDER_TEXTURES_HINT = true;

    public static final int DEFAULT_BATCH_BUFFERS = 1;
    // Default number of batch buffers (multi-buffering)

    public static final int DEFAULT_BATCH_DRAWCALLS = 256;
    // Default number of batch draw calls (by state changes: mode, texture)

    public static final int MAX_BATCH_ACTIVE_TEXTURES = 4;
    // Maximum number of additional textures that can be activated on batch drawing (SetShaderValueTexture())

    // Internal Matrix stack
    public static final int MAX_MATRIX_STACK_SIZE = 32;
    // Maximum size of Matrix stack

    // Vertex buffers id limit
    public static final int MAX_MESH_VERTEX_BUFFERS = 7;
    // Maximum vertex buffers (VBO) per mesh

    // Shader and material limits
    public static final int MAX_SHADER_LOCATIONS = 32;
    // Maximum number of shader locations supported

    public static final int MAX_MATERIAL_MAPS = 12;
    // Maximum number of shader maps supported

    // Projection matrix culling
    final float RL_CULL_DISTANCE_NEAR = 0.01f;
    // Default near cull distance

    final float RL_CULL_DISTANCE_FAR = 1000.0f;
    // Default far cull distance

    static final int GL_SHADING_LANGUAGE_VERSION = 0x8B8C;
    static final int GL_COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0;
    static final int GL_COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1;
    static final int GL_COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83F2;
    static final int GL_COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83F3;
    static final int GL_ETC1_RGB8_OES = 0x8D64;
    static final int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
    static final int GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278;
    static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
    static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
    static final int GL_COMPRESSED_RGBA_ASTC_4x4_KHR = 0x93b0;
    static final int GL_COMPRESSED_RGBA_ASTC_8x8_KHR = 0x93b7;
    static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;
    static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
    static final int GL_UNSIGNED_SHORT_5_6_5 = 0x8363;
    static final int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034;
    static final int GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033;
    static final int GL_LUMINANCE = 0x1909;
    static final int GL_LUMINANCE_ALPHA = 0x190A;

    // Texture parameters (equivalent to OpenGL defines)
    public static final int RL_TEXTURE_WRAP_S = 0x2802;      // GL_TEXTURE_WRAP_S
    public static final int RL_TEXTURE_WRAP_T = 0x2803;      // GL_TEXTURE_WRAP_T
    public static final int RL_TEXTURE_MAG_FILTER = 0x2800;      // GL_TEXTURE_MAG_FILTER
    public static final int RL_TEXTURE_MIN_FILTER = 0x2801;      // GL_TEXTURE_MIN_FILTER

    public static final int RL_TEXTURE_FILTER_NEAREST = 0x2600;     // GL_NEAREST
    public static final int RL_TEXTURE_FILTER_LINEAR = 0x2601;     // GL_LINEAR
    public static final int RL_TEXTURE_FILTER_MIP_NEAREST = 0x2700;     // GL_NEAREST_MIPMAP_NEAREST
    public static final int RL_TEXTURE_FILTER_NEAREST_MIP_LINEAR = 0x2702;     // GL_NEAREST_MIPMAP_LINEAR
    public static final int RL_TEXTURE_FILTER_LINEAR_MIP_NEAREST = 0x2701;     // GL_LINEAR_MIPMAP_NEAREST
    public static final int RL_TEXTURE_FILTER_MIP_LINEAR = 0x2703;      // GL_LINEAR_MIPMAP_LINEAR
    public static final int RL_TEXTURE_FILTER_ANISOTROPIC = 0x3000;      // Anisotropic filter (custom identifier)

    public static final int RL_TEXTURE_WRAP_REPEAT = 0x2901;      // GL_REPEAT
    public static final int RL_TEXTURE_WRAP_CLAMP = 0x812F;      // GL_CLAMP_TO_EDGE
    public static final int RL_TEXTURE_WRAP_MIRROR_REPEAT = 0x8370;      // GL_MIRRORED_REPEAT
    public static final int RL_TEXTURE_WRAP_MIRROR_CLAMP = 0x8742;      // GL_MIRROR_CLAMP_EXT

    // Matrix modes (equivalent to OpenGL)=
    public static final int RL_MODELVIEW = 0x1700;      // GL_MODELVIEW
    public static final int RL_PROJECTION = 0x1701;      // GL_PROJECTION
    public static final int RL_TEXTURE = 0x1702;      // GL_TEXTURE

    public static final int RLJ_TRANSFORM = 0x1703;

    // Primitive assembly draw modes
    public static final int RL_LINES = 0x0001;      // GL_LINES
    public static final int RL_TRIANGLES = 0x0004;      // GL_TRIANGLES
    public static final int RL_QUADS = 0x0007;      // GL_QUADS

    // GL equivalent data types
    public static final int RL_UNSIGNED_BYTE = 0x1401;      // GL_UNSIGNED_BYTE
    public static final int RL_FLOAT         = 0x1406;      // GL_FLOAT

    // Buffer usage hint
    public static final int RL_STREAM_DRAW  = 0x88E0;      // GL_STREAM_DRAW
    public static final int RL_STREAM_READ  = 0x88E1;      // GL_STREAM_READ
    public static final int RL_STREAM_COPY  = 0x88E2;      // GL_STREAM_COPY
    public static final int RL_STATIC_DRAW  = 0x88E4;      // GL_STATIC_DRAW
    public static final int RL_STATIC_READ  = 0x88E5;      // GL_STATIC_READ
    public static final int RL_STATIC_COPY  = 0x88E6;      // GL_STATIC_COPY
    public static final int RL_DYNAMIC_DRAW = 0x88E8;      // GL_DYNAMIC_DRAW
    public static final int RL_DYNAMIC_READ = 0x88E9;      // GL_DYNAMIC_READ
    public static final int RL_DYNAMIC_COPY = 0x88EA;      // GL_DYNAMIC_COPY

    // GL Shader type
    public static final int RL_FRAGMENT_SHADER = 0x8B30;      // GL_FRAGMENT_SHADER
    public static final int RL_VERTEX_SHADER   = 0x8B31;      // GL_VERTEX_SHADER
    public static final int RL_COMPUTE_SHADER  = 0x91B9;      // GL_COMPUTE_SHADER

    static int glInternalFormat = 0, glFormat = 0, glType = 0;

    static rlglData rlglData;

    /**
     * Version of OpenGL being used by Raylib-J
     */
    public static class rlGlVersion{
        public static final int
            OPENGL_11 = 1,
            OPENGL_21 = 2,
            OPENGL_33 = 3,
            OPENGL_43 = 4,
            OPENGL_ES_20 = 5;
    }

    public static class rlFramebufferAttachType{
        public static final int
            RL_ATTACHMENT_COLOR_CHANNEL0 = 0,
            RL_ATTACHMENT_COLOR_CHANNEL1 = 1,
            RL_ATTACHMENT_COLOR_CHANNEL2 = 2,
            RL_ATTACHMENT_COLOR_CHANNEL3 = 3,
            RL_ATTACHMENT_COLOR_CHANNEL4 = 4,
            RL_ATTACHMENT_COLOR_CHANNEL5 = 5,
            RL_ATTACHMENT_COLOR_CHANNEL6 = 6,
            RL_ATTACHMENT_COLOR_CHANNEL7 = 7,
            RL_ATTACHMENT_DEPTH = 100,
            RL_ATTACHMENT_STENCIL = 200;
    }

    public static class rlFramebufferAttachTextureType{
        public static final int
            RL_ATTACHMENT_CUBEMAP_POSITIVE_X = 0,
            RL_ATTACHMENT_CUBEMAP_NEGATIVE_X = 1,
            RL_ATTACHMENT_CUBEMAP_POSITIVE_Y = 2,
            RL_ATTACHMENT_CUBEMAP_NEGATIVE_Y = 3,
            RL_ATTACHMENT_CUBEMAP_POSITIVE_Z = 4,
            RL_ATTACHMENT_CUBEMAP_NEGATIVE_Z = 5,
            RL_ATTACHMENT_TEXTURE2D = 100,
            RL_ATTACHMENT_RENDERBUFFER = 200;
    }

    /**
     * Texture formats (support depends on OpenGL version)
     */
    public static class rlPixelFormat{
        public static final int
                RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE = 1,  // 8 bit per pixel (no alpha)
                RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA = 2,  // 8*2 bpp (2 channels)
                RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5 = 3,  // 16 bpp
                RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8 = 4,  // 24 bpp
                RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1 = 5,  // 16 bpp (1 bit alpha)
                RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4 = 6,  // 16 bpp (4 bit alpha)
                RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8 = 7,  // 32 bpp
                RL_PIXELFORMAT_UNCOMPRESSED_R32 = 8,  // 32 bpp (1 channel - float)
                RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32 = 9,  // 32*3 bpp (3 channels - float)
                RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32 = 10, // 32*4 bpp (4 channels - float)
                RL_PIXELFORMAT_COMPRESSED_DXT1_RGB = 11, // 4 bpp (no alpha)
                RL_PIXELFORMAT_COMPRESSED_DXT1_RGBA = 12, // 4 bpp (1 bit alpha)
                RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA = 13, // 8 bpp
                RL_PIXELFORMAT_COMPRESSED_DXT5_RGBA = 14, // 8 bpp
                RL_PIXELFORMAT_COMPRESSED_ETC1_RGB = 15, // 4 bpp
                RL_PIXELFORMAT_COMPRESSED_ETC2_RGB = 16, // 4 bpp
                RL_PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA = 17, // 8 bpp
                RL_PIXELFORMAT_COMPRESSED_PVRT_RGB = 18, // 4 bpp
                RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA = 19, // 4 bpp
                RL_PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA = 20, // 8 bpp
                RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA = 21; // 2 bpp
    }

    // Texture parameters: filter mode
    // NOTE 1: Filtering considers mipmaps if available in the texture
    // NOTE 2: Filter is accordingly set for minification and magnification
    public static class rlTextureFilterMode{
        public static final int
                RL_TEXTURE_FILTER_POINT = 0,                   // No filter, just pixel approximation
                RL_TEXTURE_FILTER_BILINEAR = 1,                // Linear filtering
                RL_TEXTURE_FILTER_TRILINEAR = 2,               // Trilinear filtering  = linear with mipmaps)
                RL_TEXTURE_FILTER_ANISOTROPIC_4X = 3,          // Anisotropic filtering 4x
                RL_TEXTURE_FILTER_ANISOTROPIC_8X = 4,          // Anisotropic filtering 8x
                RL_TEXTURE_FILTER_ANISOTROPIC_16X = 5;         // Anisotropic filtering 16x
    }

    // Color blending modes (pre-defined)
    public static class rlBlendMode{
        public static final int
                RL_BLEND_ALPHA = 0,                // Blend textures considering alpha  = default)
                RL_BLEND_ADDITIVE = 1,                 // Blend textures adding colors
                RL_BLEND_MULTIPLIED = 2,               // Blend textures multiplying colors
                RL_BLEND_ADD_COLORS = 3,               // Blend textures adding colors (alternative)
                RL_BLEND_SUBTRACT_COLORS = 4,          // Blend textures subtracting colors (alternative)
                RL_BLEND_ALPHA_PREMULTIPLY = 5,         // Blend premultiplied textures considering alpha
                RL_BLEND_CUSTOM = 6;                   // Blend textures using custom src/dst factors (use SetBlendModeCustom())

    }

    // Shader location point type
    public static class rlShaderLocationIndex{
        public static final int
                RL_SHADER_LOC_VERTEX_POSITION = 0, // Shader location: vertex attribute: position
                RL_SHADER_LOC_VERTEX_TEXCOORD01 = 1, // Shader location: vertex attribute: texcoord01
                RL_SHADER_LOC_VERTEX_TEXCOORD02 = 2, // Shader location: vertex attribute: texcoord02
                RL_SHADER_LOC_VERTEX_NORMAL = 3, // Shader location: vertex attribute: normal
                RL_SHADER_LOC_VERTEX_TANGENT = 4, // Shader location: vertex attribute: tangent
                RL_SHADER_LOC_VERTEX_COLOR = 5, // Shader location: vertex attribute: color
                RL_SHADER_LOC_MATRIX_MVP = 6, // Shader location: matrix uniform: model-view-projection
                RL_SHADER_LOC_MATRIX_VIEW = 7, // Shader location: matrix uniform: view (camera transform)
                RL_SHADER_LOC_MATRIX_PROJECTION = 8, // Shader location: matrix uniform: projection
                RL_SHADER_LOC_MATRIX_MODEL = 9, // Shader location: matrix uniform: model (transform)
                RL_SHADER_LOC_MATRIX_NORMAL = 10, // Shader location: matrix uniform: normal
                RL_SHADER_LOC_VECTOR_VIEW = 11, // Shader location: vector uniform: view
                RL_SHADER_LOC_COLOR_DIFFUSE = 12, // Shader location: vector uniform: diffuse color
                RL_SHADER_LOC_COLOR_SPECULAR = 13, // Shader location: vector uniform: specular color
                RL_SHADER_LOC_COLOR_AMBIENT = 14, // Shader location: vector uniform: ambient color
                RL_SHADER_LOC_MAP_ALBEDO = 15, // Shader location: sampler2d texture: albedo (same as: SHADER_LOC_MAP_DIFFUSE)
                RL_SHADER_LOC_MAP_METALNESS = 16, // Shader location: sampler2d texture: metalness (same as: SHADER_LOC_MAP_SPECULAR)
                RL_SHADER_LOC_MAP_NORMAL = 17, // Shader location: sampler2d texture: normal
                RL_SHADER_LOC_MAP_ROUGHNESS = 18, // Shader location: sampler2d texture: roughness
                RL_SHADER_LOC_MAP_OCCLUSION = 19, // Shader location: sampler2d texture: occlusion
                RL_SHADER_LOC_MAP_EMISSION = 20, // Shader location: sampler2d texture: emission
                RL_SHADER_LOC_MAP_HEIGHT = 21, // Shader location: sampler2d texture: height
                RL_SHADER_LOC_MAP_CUBEMAP = 22, // Shader location: samplerCube texture: cubemap
                RL_SHADER_LOC_MAP_IRRADIANCE = 23, // Shader location: samplerCube texture: irradiance
                RL_SHADER_LOC_MAP_PREFILTER = 24, // Shader location: samplerCube texture: prefilter
                RL_SHADER_LOC_MAP_BRDF = 25; // Shader location: sampler2d texture: brdf

        public final static int
                RL_SHADER_LOC_MAP_DIFFUSE = RL_SHADER_LOC_MAP_ALBEDO,
                RL_SHADER_LOC_MAP_SPECULAR = RL_SHADER_LOC_MAP_METALNESS;
    }

    // Shader uniform data types
    public static class rlShaderUniformDataType{
        public static final int
                RL_SHADER_UNIFORM_FLOAT     = 0, // Shader uniform type: float
                RL_SHADER_UNIFORM_VEC2      = 1, // Shader uniform type: vec2 (2 float)
                RL_SHADER_UNIFORM_VEC3      = 2, // Shader uniform type: vec3 (3 float)
                RL_SHADER_UNIFORM_VEC4      = 3, // Shader uniform type: vec4 (4 float)
                RL_SHADER_UNIFORM_INT       = 4, // Shader uniform type: int
                RL_SHADER_UNIFORM_IVEC2     = 5, // Shader uniform type: ivec2 (2 int)
                RL_SHADER_UNIFORM_IVEC3     = 6, // Shader uniform type: ivec3 (3 int)
                RL_SHADER_UNIFORM_IVEC4     = 7, // Shader uniform type: ivec4 (4 int)
                RL_SHADER_UNIFORM_SAMPLER2D = 8; // Shader uniform type: sampler2d
    }

    public static class rlShaderAttributeDataType{
        public static final int
                RL_SHADER_ATTRIB_FLOAT = 0,        // Shader attribute type: float
                RL_SHADER_ATTRIB_VEC2 = 1,        // Shader attribute type: vec2 (2 float)
                RL_SHADER_ATTRIB_VEC3 = 2,        // Shader attribute type: vec3 (3 float)
                RL_SHADER_ATTRIB_VEC4 = 3;        // Shader attribute type: vec4 (4 float)
    }

    public RLGL(){
        rlglData = new rlglData();
    }

    public static rlglData getRlglData(){
        return rlglData;
    }

    public static void rlMatrixMode(int mode){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlMatrixMode(mode);
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlMatrixMode(mode);
        }
    }

    public static void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlFrustum(left, right, bottom, top, znear, zfar);
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlFrustum(left, right, bottom, top, znear, zfar);
        }
    }

    public static void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlOrtho(left, right, bottom, top, znear, zfar);
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlOrtho(left, right, bottom, top, znear, zfar);
        }
    }

    public static void rlPushMatrix(){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlPushMatrix();
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlPushMatrix();
        }
    }

    public static void rlPopMatrix(){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlPopMatrix();
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlPopMatrix();
        }
    }

    public static void rlLoadIdentity(){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlLoadIdentity();
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlLoadIdentity();
        }
    }

    public static void rlTranslatef(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlTranslatef(x, y, z);
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlTranslatef(x, y, z);
        }
    }

    public static void rlRotatef(float angle, float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlRotatef(angle, x, y, z);
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlRotatef(angle, x, y, z);
        }
    }

    public static void rlScalef(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlScalef(x, y, z);

        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlScalef(x, y, z);
        }
    }

    public static void rlMultMatrixf(float[] matf){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlMultMatrixf(matf);
        }
        else if (GRAPHICS_API_OPENGL_11){
            GL_11.rlMultMatrixf(matf);
        }

    }

    // Set the viewport area (transformation from normalized device coordinates to window coordinates)
    // NOTE: We store current viewport dimensions
    public static void rlViewport(int x, int y, int width, int height){
        glViewport(x, y, width, height);
    }

    public static void rlBegin(int mode){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlBegin(mode);
        }
        else{
            GL_11.rlBegin(mode);
        }
    }

    public static void rlEnd(){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlEnd();
        }
        else{
            GL_11.rlEnd();
        }
    }

    // Define one vertex (position)
    // NOTE: Vertex position data is the basic information required for drawing
    public static void rlVertex3f(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlVertex3f(x, y, z);
        }
        else{
            GL_11.rlVertex3f(x, y, z);
        }
    }

    // Define one vertex (position)
    public static void rlVertex2f(float x, float y){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlVertex2f(x, y);
        }
        else{
            GL_11.rlVertex2f(x, y);
        }
    }

    // Define one vertex (position)
    public static void rlVertex2i(int x, int y){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlVertex2i(x, y);
        }
        else{
            GL_11.rlVertex2i(x, y);
        }
    }

    // Define one vertex (texture coordinate)
    // NOTE: Texture coordinates are limited to QUADS only
    public static void rlTexCoord2f(float x, float y){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlTexCoord2f(x, y);
        }
        else{
            GL_11.rlTexCoord2f(x, y);
        }
    }

    // Define one vertex (normal)
    // NOTE: Normals limited to TRIANGLES only?
    public static void rlNormal3f(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlNormal3f(x, y, z);
        }
        else{
            GL_11.rlNormal3f(x, y, z);
        }
    }

    // Define one vertex (color)
    public static void rlColor4ub(int x, int y, int z, int w){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlColor4ub((byte)x, (byte)y, (byte)z, (byte)w);
        }
        else{
            GL_11.rlColor4ub(x, y, z, w);
        }
    }

    // Define one vertex (color)
    void rlColor4f(float r, float g, float b, float a){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlColor4f(r, g, b, a);
        }
        else{
            GL_11.rlColor4f(r, g, b, a);
        }
    }

    // Define one vertex (color)
    public static void rlColor3f(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            GL_33.rlColor3f(x, y, z);
        }
        else{
            GL_11.rlColor3f(x, y, z);
        }
    }

    public static void rlSetTexture(int id){
        if (id == 0){
            if (GRAPHICS_API_OPENGL_11){
                rlDisableTexture();
            }
            else{
                // NOTE: If quads batch limit is reached, we force a draw call and next batch starts
                if (rlglData.getState().vertexCounter >= rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].elementCount*4) {
                    rlDrawRenderBatch(rlglData.getCurrentBatch());
                }
            }
        }
        else{
            if (GRAPHICS_API_OPENGL_11){
                rlSetTexture(id);
            }
            else{
                if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId != id){
                    if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount > 0){
                        // Make sure current rlglData.getCurrentBatch().draws[i].vertexCount is aligned a multiple of 4,
                        // that way, following QUADS drawing will keep aligned with index processing
                        // It implies adding some extra alignment vertex at the end of the draw,
                        // those vertex are not processed but they are considered as an additional offset
                        // for the next set of vertex to be drawn
                        if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_LINES){
                            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount < 4) ? rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount : rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount % 4);
                        }
                        else if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_TRIANGLES){
                            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount < 4) ? 1 : (4 - (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount % 4)));
                        }
                        else{
                            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = 0;
                        }

                        if (!rlCheckRenderBatchLimit(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment)) {
                            rlglData.getState().vertexCounter += rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment;
                            rlglData.getCurrentBatch().drawCounter++;
                        }
                    }

                    if (rlglData.getCurrentBatch().drawCounter >= DEFAULT_BATCH_DRAWCALLS){
                        rlDrawRenderBatch(rlglData.getCurrentBatch());
                    }

                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId = id;
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount = 0;
                }
            }
        }
    }

    // Select and active a texture slot
    public static void rlActiveTextureSlot(int slot) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2)
            glActiveTexture(GL_TEXTURE0 + slot);
    }

    // Enable texture
    public static void rlEnableTexture(int id)
    {
        if(GRAPHICS_API_OPENGL_11){
            glEnable(GL_TEXTURE_2D);
        }

        glBindTexture(GL_TEXTURE_2D, id);
    }

    public static void rlDisableTexture(){
        if (GRAPHICS_API_OPENGL_11){
            glDisable(GL_TEXTURE_2D);
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Enable texture cubemap
    public static void rlEnableTextureCubemap(int id){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glEnable(GL_TEXTURE_CUBE_MAP);   // rCore in OpenGL 1.4
            glBindTexture(GL_TEXTURE_CUBE_MAP, id);
        }
    }

    // Disable texture cubemap
    public static void rlDisableTextureCubemap(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDisable(GL_TEXTURE_CUBE_MAP);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        }
    }

    // Set texture parameters (wrap mode/filter mode)
    public static void rlTextureParameters(int id, int param, int value){
        glBindTexture(GL_TEXTURE_2D, id);

        if (!GRAPHICS_API_OPENGL_11) {
            // Reset anisotropy filer, in case it was set
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 1.0f);
        }

        switch (param){
            case RL_TEXTURE_WRAP_S:
            case RL_TEXTURE_WRAP_T:{
                if (value == RL_TEXTURE_WRAP_MIRROR_CLAMP){
                    if (GRAPHICS_API_OPENGL_11){
                        if (rlglData.getExtSupported().isTexMirrorClamp()){
                            glTexParameteri(GL_TEXTURE_2D, param, value);
                        }
                        else{
                            Tracelog(LOG_WARNING, "GL: Clamp mirror wrap mode not supported (GL_MIRROR_CLAMP_EXT)");
                        }
                    }
                }
                else{
                    glTexParameteri(GL_TEXTURE_2D, param, value);
                }

            }
            break;
            case RL_TEXTURE_MAG_FILTER:
            case RL_TEXTURE_MIN_FILTER:
                glTexParameteri(GL_TEXTURE_2D, param, value);
                break;
            case RL_TEXTURE_FILTER_ANISOTROPIC:{
                if (GRAPHICS_API_OPENGL_11){
                    if (value <= rlglData.getExtSupported().getMaxAnisotropyLevel()){
                        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else if (rlglData.getExtSupported().getMaxAnisotropyLevel() > 0.0f){
                        Tracelog(LOG_WARNING, "GL: Maximum anisotropic filter level supported is " +
                                rlglData.getExtSupported().getMaxAnisotropyLevel());
                        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else{
                        Tracelog(LOG_WARNING, "GL: Anisotropic filtering not supported");
                    }
                }
            }
            break;
            default:
                break;
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Enable shader program usage
    public static void rlEnableShader(int id){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(id);
        }
    }

    // Disable shader program usage
    public static void rlDisableShader(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(0);
        }
    }

    // Enable rendering to texture (fbo)
    public static void rlEnableFramebuffer(int id){
        if ((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && RLGL_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, id);
        }
    }

    // Disable rendering to texture
    public static void rlDisableFramebuffer(){
        if ((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && RLGL_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    // Activate multiple draw color buffers
    // NOTE: One color buffer is always active by default
    void rlActiveDrawBuffers(int count){
        if(GRAPHICS_API_OPENGL_33 && RLGL_RENDER_TEXTURES_HINT){
            // NOTE: Maximum number of draw buffers supported is implementation dependant,
            // it can be queried with glGet*() but it must be at least 8
            //GLint maxDrawBuffers = 0;
            //glGetIntegerv(GL_MAX_DRAW_BUFFERS, &maxDrawBuffers);

            if (count > 0){
                if (count > 8){
                    Tracelog(LOG_WARNING, "GL: Max color buffers limited to 8");
                }
                else{
                    int[] buffers ={
                        GL_COLOR_ATTACHMENT0,
                                GL_COLOR_ATTACHMENT1,
                                GL_COLOR_ATTACHMENT2,
                                GL_COLOR_ATTACHMENT3,
                                GL_COLOR_ATTACHMENT4,
                                GL_COLOR_ATTACHMENT5,
                                GL_COLOR_ATTACHMENT6,
                                GL_COLOR_ATTACHMENT7,
                    } ;

                    glDrawBuffers(buffers);
                }
            }
            else Tracelog(LOG_WARNING, "GL: One color buffer active by default");
        }
    }

    // Enable color blending
    public void rlEnableColorBlend() {
        glEnable(GL_BLEND);
    }

    // Disable color blending
    public void rlDisableColorBlend() {
        glDisable(GL_BLEND);
    }

    // Enable depth test
    public void rlEnableDepthTest(){
        glEnable(GL_DEPTH_TEST);
    }

    // Disable depth test
    public void rlDisableDepthTest(){
        glDisable(GL_DEPTH_TEST);
    }

    // Enable depth write
    void rlEnableDepthMask(){
        glDepthMask(true);
    }

    // Disable depth write
    void rlDisableDepthMask(){
        glDepthMask(false);
    }

    // Enable backface culling
    void rlEnableBackfaceCulling(){
        glEnable(GL_CULL_FACE);
    }

    // Disable backface culling
    void rlDisableBackfaceCulling(){
        glDisable(GL_CULL_FACE);
    }

    // Enable scissor test
    public void rlEnableScissorTest(){
        glEnable(GL_SCISSOR_TEST);
    }

    // Disable scissor test
    public void rlDisableScissorTest(){
        glDisable(GL_SCISSOR_TEST);
    }

    // Scissor test
    public void rlScissor(int x, int y, int width, int height){
        glScissor(x, y, width, height);
    }

    // Enable wire mode
    public static void rlEnableWireMode(){
        if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
    }

    // Disable wire mode
    public static void rlDisableWireMode(){
        if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    // Set the line drawing width
    public void rlSetLineWidth(float width){
        glLineWidth(width);
    }

    // Get the line drawing width
    public float rlGetLineWidth(){
        return glGetFloat(GL_LINE_WIDTH);
    }

    // Enable line aliasing
    public void rlEnableSmoothLines(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_11){
            glEnable(GL_LINE_SMOOTH);
        }
    }

    // Disable line aliasing
    public void rlDisableSmoothLines(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_11){
            glDisable(GL_LINE_SMOOTH);
        }
    }

    public void rlEnableStereoRenderer(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setStereoRender(true);
        }
    }

    public void rlDisableStereoRenderer(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setStereoRender(false);
        }
    }

    public static boolean rlIsStereoRendererEnabled(){
        return rlglData.getState().isStereoRender();
    }

    public static void rlClearColor(int r, int g, int b, int a){
        float cr = (float) r / 255;
        float cg = (float) g / 255;
        float cb = (float) b / 255;
        float ca = (float) a / 255;

        glClearColor(cr, cg, cb, ca);
    }

    public static void rlClearScreenBuffers(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // Clear used buffers: Color and Depth (Depth is used for 3D)
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);     // Stencil buffer not used...
    }

    // Check and log OpenGL error codes
    void rlCheckErrors(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            boolean check = true;
            while (check){
            int err = glGetError();
                switch (err){
                    case GL_NO_ERROR:
                        check = false;
                        break;
                    case 0x0500:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_INVALID_ENUM");
                        break;
                    case 0x0501:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_INVALID_VALUE");
                        break;
                    case 0x0502:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_INVALID_OPERATION");
                        break;
                    case 0x0503:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_STACK_OVERFLOW");
                        break;
                    case 0x0504:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_STACK_UNDERFLOW");
                        break;
                    case 0x0505:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_OUT_OF_MEMORY");
                        break;
                    case 0x0506:
                        Tracelog(LOG_WARNING, "GL: Error detected: GL_INVALID_FRAMEBUFFER_OPERATION");
                        break;
                    default:
                        Tracelog(LOG_WARNING, "GL: Error detected: Unknown error code: " + err);
                        break;
                }
            }
        }
    }

    // Set blend mode
    public void rlSetBlendMode(int mode){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getState().getCurrentBlendMode() != mode){
                rlDrawRenderBatch(rlglData.getCurrentBatch());

                switch (mode){
                    case rlBlendMode.RL_BLEND_ALPHA:
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case rlBlendMode.RL_BLEND_ADDITIVE:
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case rlBlendMode.RL_BLEND_MULTIPLIED:
                        glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case rlBlendMode.RL_BLEND_ADD_COLORS:
                        glBlendFunc(GL_ONE, GL_ONE);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case rlBlendMode.RL_BLEND_SUBTRACT_COLORS:
                        glBlendFunc(GL_ONE, GL_ONE);
                        glBlendEquation(GL_FUNC_SUBTRACT);
                        break;
                    case rlBlendMode.RL_BLEND_ALPHA_PREMULTIPLY:
                        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case rlBlendMode.RL_BLEND_CUSTOM:
                        // NOTE: Using GL blend src/dst factors and GL equation configured with rlSetBlendFactors()
                        glBlendFunc(rlglData.getState().glBlendSrcFactor, rlglData.getState().glBlendDstFactor);
                        glBlendEquation(rlglData.getState().glBlendEquation);
                        break;
                    default:
                        break;
                }

                rlglData.getState().setCurrentBlendMode(mode);
            }
        }
    }

    // Set blending mode factor and equation
    public void rlSetBlendFactors(int glSrcFactor, int glDstFactor, int glEquation){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setGlBlendSrcFactor(glSrcFactor);
            rlglData.getState().setGlBlendDstFactor(glDstFactor);
            rlglData.getState().setGlBlendEquation(glEquation);
        }
    }

    public static void rlglInit(int width, int height){
        // Check OpenGL information and capabilities
        //------------------------------------------------------------------------------
        String glVersion = glGetString(GL_VERSION);
        float glVersionF = Float.parseFloat(glVersion.substring(0, 3));

        if (glVersionF >= 4.3f) {
            GRAPHICS_API_OPENGL_43 = true;
            GRAPHICS_API_OPENGL_33 = true;
            GRAPHICS_API_OPENGL_21 = false;
            GRAPHICS_API_OPENGL_11 = false;
        }
        else if (glVersionF < 4.3f && glVersionF >= 3.3f) {
            GRAPHICS_API_OPENGL_43 = false;
            GRAPHICS_API_OPENGL_33 = true;
            GRAPHICS_API_OPENGL_21 = false;
            GRAPHICS_API_OPENGL_11 = false;
        }
        else if (glVersionF < 3.3f && glVersionF >= 2.1f) {
            GRAPHICS_API_OPENGL_43 = false;
            GRAPHICS_API_OPENGL_33 = false;
            GRAPHICS_API_OPENGL_21 = true;
            GRAPHICS_API_OPENGL_11 = false;
        }
        else {
            GRAPHICS_API_OPENGL_43 = false;
            GRAPHICS_API_OPENGL_33 = false;
            GRAPHICS_API_OPENGL_21 = false;
            GRAPHICS_API_OPENGL_11 = true;
        }

        //TODO: OPENGL_DEBUG_CONTEXT

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // Init default white texture
            DataBuffer pixels = new DataBufferByte(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255}, 4);
            rlglData.getState().setDefaultTextureId(rlLoadTexture(pixels, 1, 1, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1));

            if(rlglData.getState().getDefaultTextureId() != 0) {
                Tracelog(LOG_INFO, "TEXTURE: [ID " + rlglData.getState().getDefaultTextureId() + "] Default texture loaded successfully");
            }
            else{
                Tracelog(LOG_WARNING, "TEXTURE: Failed to load default texture");
            }

            // Init default Shader (customized for GL 3.3 and ES2)
            // Loaded: RLGL.State.defaultShaderId + RLGL.State.defaultShaderLocs
            rlLoadShaderDefault();
            rlglData.getState().currentShaderId = rlglData.getState().defaultShaderId;
            rlglData.getState().currentShaderLocs = rlglData.getState().defaultShaderLocs;

            // Init default vertex arrays buffers
            rlglData.setDefaultBatch(rlLoadRenderBatch(DEFAULT_BATCH_BUFFERS, RL_DEFAULT_BATCH_BUFFER_ELEMENTS));
            rlglData.setCurrentBatch(rlglData.getDefaultBatch());

            // Init stack matrices (emulating OpenGL 1.1)
            for(int i = 0; i < MAX_MATRIX_STACK_SIZE; i++){
                rlglData.getState().getStack()[i] = MatrixIdentity();
            }

            // Init internal matrices
            rlglData.getState().setTransform(MatrixIdentity());
            rlglData.getState().setProjection(MatrixIdentity());
            rlglData.getState().setModelview(MatrixIdentity());
            rlglData.getState().setCurrentMatrix(rlglData.getState().getModelview());

        } // GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2

        // Initialize OpenGL default states
        //----------------------------------------------------------
        // Init state: Depth test
        glDepthFunc(GL_LEQUAL);                                 // Type of depth testing to apply
        glDisable(GL_DEPTH_TEST);                               // Disable depth testing for 2D (only used for 3D)

        // Init state: Blending mode
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);      // Color blending function (how colors are mixed)
        glEnable(GL_BLEND);                                     // Enable color blending (required to work with transparencies)

        // Init state: Culling
        // NOTE: All rShapes/models triangles are drawn CCW
        glCullFace(GL_BACK);                                    // Cull the back face (default)
        glFrontFace(GL_CCW);                                    // Front face are defined counter clockwise (default)
        glEnable(GL_CULL_FACE);                                 // Enable backface culling

        // Init state: Cubemap seamless
        if (GRAPHICS_API_OPENGL_33){
            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);                 // Seamless cubemaps (not supported on OpenGL ES 2.0)
        }

        if (GRAPHICS_API_OPENGL_11){
            // Init state: Color hints (deprecated in OpenGL 3.0+)
            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);      // Improve quality of color and texture coordinate interpolation
            glShadeModel(GL_SMOOTH);                                // Smooth shading between vertex (vertex colors interpolation)
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // Store screen size into global variables
            rlglData.getState().setFramebufferWidth(width);
            rlglData.getState().setFramebufferHeight(height);

            Tracelog(LOG_INFO, "RLGL: Default OpenGL state initialized successfully");
        }

        // Init state: Color/Depth buffers clear
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Set clear color (black)
        glClearDepth(1.0f);                                           // Set clear depth value (default)
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // Clear color and depth buffers (depth buffer required for 3D)

    }

    // Vertex Buffer Object deinitialization (memory free)
    public static void rlglClose(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            UnloadRenderBatch(rlglData.getDefaultBatch());

            rlUnloadShaderDefault();          // Unload default shader
            glDeleteTextures(rlglData.getState().getDefaultTextureId()); // Unload default texture

            Tracelog(LOG_INFO, "TEXTURE: [ID " + rlglData.getState().getDefaultTextureId() + "] Default texture unloaded successfully");
        }
    }

    // Load OpenGL extensions
    // NOTE: External loader function could be passed as a pointer
    public static void rlLoadExtensions(){
        if(GRAPHICS_API_OPENGL_33) {     // Also defined for GRAPHICS_API_OPENGL_21
            // NOTE: glad is generated and contains only required OpenGL 3.3 rCore extensions (and lower versions)
            if(!__APPLE__) {
                //if (!gladLoadGLLoader((GLADloadproc) loader))
                //    Tracelog(LOG_WARNING, "GLAD: Cannot load OpenGL extensions");
                //else
                //Tracelog(LOG_INFO, "GLAD: OpenGL extensions loaded successfully");
            }

            // Get number of supported extensions
            int numExt = glGetInteger(GL_NUM_EXTENSIONS);
            Tracelog(LOG_INFO, "GL: Supported extensions count: " + numExt);

            if (SUPPORT_GL_DETAILS_INFO){
                // Get supported extensions list
                // WARNING: glGetStringi() not available on OpenGL 2.1
                String[] extList = new String[numExt];
                Tracelog(LOG_INFO, "GL: OpenGL extensions:");
                for (int i = 0; i < numExt; i++){
                    extList[i] = glGetStringi(GL_EXTENSIONS, i);
                    Tracelog(LOG_INFO, "    " + extList[i]);
                }
                extList = null;       // Free extensions pointers
            }

            // Register supported extensions flags
            // OpenGL 3.3 extensions supported by default (core)
            rlglData.getExtSupported().vao = true;
            rlglData.getExtSupported().instancing = true;
            rlglData.getExtSupported().texNPOT = true;
            rlglData.getExtSupported().texFloat32 = true;
            rlglData.getExtSupported().texDepth = true;
            rlglData.getExtSupported().maxDepthBits = 32;
            rlglData.getExtSupported().texAnisoFilter = true;
            rlglData.getExtSupported().texMirrorClamp = true;
            if (GRAPHICS_API_OPENGL_43){
                rlglData.getExtSupported().computeShader = true;
                rlglData.getExtSupported().ssbo = true;
            }
            if (!__APPLE__){
                // NOTE: With GLAD, we can check if an extension is supported using the GLAD_GL_xxx booleans
                //if (GLAD_GL_EXT_texture_compression_s3tc)
                rlglData.getExtSupported().setTexCompDXT(true);  // Texture compression: DXT
                //if (GLAD_GL_ARB_ES3_compatibility)
                rlglData.getExtSupported().setTexCompETC2(true); // Texture compression: ETC2/EAC
            }
        } // GRAPHICS_API_OPENGL_33

        if (GRAPHICS_API_OPENGL_ES2){
            // Get supported extensions list
            int numExt = 0;
            String[] extList = new String[512]; // Allocate 512 strings pointers (2 KB)
            String extensions = glGetString(GL_EXTENSIONS);  // One big const string

            // NOTE: We have to duplicate string because glGetString() returns a const string
            int len = extensions.length() + 1;
            String extensionsDup = extensions;
            extList[numExt] = extensionsDup;

            for (int i = 0; i < len; i++){
                if (extensionsDup.charAt(i) == ' '){
                    extensionsDup.toCharArray()[i] = '\0';
                    numExt++;
                    extList[numExt] = extensionsDup;
                }
            }

            Tracelog(LOG_INFO, "GL: Supported extensions count: " + numExt);

            if (SUPPORT_GL_DETAILS_INFO){
                Tracelog(LOG_INFO, "GL: OpenGL extensions:");
                for (int i = 0; i < numExt; i++) Tracelog(LOG_INFO, "    " + extList[i]);
            }

            // Check required extensions
            for (int i = 0; i < numExt; i++){
                /*
                // Check VAO support
                // NOTE: Only check on OpenGL ES, OpenGL 3.3 has VAO support as core feature
                if (extList[i].equals("GL_OES_vertex_array_object")) {
                    // The extension is supported by our hardware and driver, try to get related functions pointers
                    // NOTE: emscripten does not support VAOs natively, it uses emulation and it reduces overall performance...
                    glGenVertexArrays = (PFNGLGENVERTEXARRAYSOESPROC) eglGetProcAddress("glGenVertexArraysOES");
                    glBindVertexArray = (PFNGLBINDVERTEXARRAYOESPROC) eglGetProcAddress("glBindVertexArrayOES");
                    glDeleteVertexArrays = (PFNGLDELETEVERTEXARRAYSOESPROC) eglGetProcAddress("glDeleteVertexArraysOES");
                    //glIsVertexArray = (PFNGLISVERTEXARRAYOESPROC)eglGetProcAddress("glIsVertexArrayOES");     // NOTE: Fails in WebGL, omitted

                    if ((glGenVertexArrays != null) && (glBindVertexArray != null) && (glDeleteVertexArrays != null))
                        rlglData.getExtSupported().setVao(true);
                }

                // Check instanced rendering support
                if (extList[i].equals("GL_ANGLE_instanced_arrays")){        // Web ANGLE

                    glDrawArraysInstanced = (PFNGLDRAWARRAYSINSTANCEDEXTPROC) eglGetProcAddress("glDrawArraysInstancedANGLE");
                    glDrawElementsInstanced = (PFNGLDRAWELEMENTSINSTANCEDEXTPROC) eglGetProcAddress("glDrawElementsInstancedANGLE");
                    glVertexAttribDivisor = (PFNGLVERTEXATTRIBDIVISOREXTPROC) eglGetProcAddress("glVertexAttribDivisorANGLE");

                    if ((glDrawArraysInstanced != null) && (glDrawElementsInstanced != null) && (glVertexAttribDivisor != null))
                        rlglData.getExtSupported().setInstancing(true);
                }
                else{
                    if(extList[i].equals("GL_EXT_draw_instanced") && extList[i].equals("GL_EXT_instanced_arrays")){ // Standard EXT
                        glDrawArraysInstanced = (PFNGLDRAWARRAYSINSTANCEDEXTPROC) eglGetProcAddress("glDrawArraysInstancedEXT");
                        glDrawElementsInstanced = (PFNGLDRAWELEMENTSINSTANCEDEXTPROC) eglGetProcAddress("glDrawElementsInstancedEXT");
                        glVertexAttribDivisor = (PFNGLVERTEXATTRIBDIVISOREXTPROC) eglGetProcAddress("glVertexAttribDivisorEXT");

                        if ((glDrawArraysInstanced != null) && (glDrawElementsInstanced != null) && (glVertexAttribDivisor != null))
                            rlglData.getExtSupported().setInstancing(true);
                    }
                }
                */
                //TODO: Figure out a method for getting GLAD. Until then...
                rlglData.getExtSupported().setVao(true);
                rlglData.getExtSupported().setInstancing(true);

                // Check NPOT textures support
                // NOTE: Only check on OpenGL ES, OpenGL 3.3 has NPOT textures full support as core feature
                if (extList[i].equals("GL_OES_texture_npot"))
                    rlglData.getExtSupported().setTexNPOT(true);

                // Check texture float support
                if (extList[i].equals("GL_OES_texture_float"))
                    rlglData.getExtSupported().setTexFloat32(true);

                // Check depth texture support
                if (extList[i].equals("GL_OES_depth_texture") || extList[i].equals("GL_WEBGL_depth_texture"))
                    rlglData.getExtSupported().setTexDepth(true);

                if (extList[i].equals("GL_OES_depth24"))
                    rlglData.getExtSupported().setMaxDepthBits(24);
                if (extList[i].equals("GL_OES_depth32"))
                    rlglData.getExtSupported().setMaxDepthBits(32);

                // Check texture compression support: DXT
                if (extList[i].equals("GL_EXT_texture_compression_s3tc") || extList[i].equals(
                        "GL_WEBGL_compressed_texture_s3tc")  || extList[i].equals("GL_WEBKIT_WEBGL_compressed_texture_s3tc"))
                    rlglData.getExtSupported().setTexCompDXT(true);

                // Check texture compression support: ETC1
                if (extList[i].equals("GL_OES_compressed_ETC1_RGB8_texture") || extList[i].equals("GL_WEBGL_compressed_texture_etc1"))
                    rlglData.getExtSupported().setTexCompETC1(true);

                // Check texture compression support: ETC2/EAC
                if (extList[i].equals("GL_ARB_ES3_compatibility"))
                    rlglData.getExtSupported().setTexCompETC2(true);

                // Check texture compression support: PVR
                if (extList[i].equals("GL_IMG_texture_compression_pvrtc"))
                    rlglData.getExtSupported().setTexCompPVRT(true);

                // Check texture compression support: ASTC
                if (extList[i].equals("GL_KHR_texture_compression_astc_hdr"))
                    rlglData.getExtSupported().setTexCompASTC(true);

                // Check anisotropic texture filter support
                if (extList[i].equals("GL_EXT_texture_filter_anisotropic"))
                    rlglData.getExtSupported().setTexAnisoFilter(true);

                // Check clamp mirror wrap mode support
                if (extList[i].equals("GL_EXT_texture_mirror_clamp"))
                    rlglData.getExtSupported().setTexMirrorClamp(true);
            }

            // Free extensions pointers
            extList = null;
            extensionsDup = null;    // Duplicated string must be deallocated
        }  // GRAPHICS_API_OPENGL_ES2

        // Check OpenGL information and capabilities
        //------------------------------------------------------------------------------
        // Show current OpenGL and GLSL version
        Tracelog(LOG_INFO, "GL: OpenGL device information:");
        Tracelog(LOG_INFO, "    > Vendor:   " + glGetString(GL_VENDOR));
        Tracelog(LOG_INFO, "    > Renderer: " + glGetString(GL_RENDERER));
        Tracelog(LOG_INFO, "    > Version:  " + glGetString(GL_VERSION));
        Tracelog(LOG_INFO, "    > GLSL:     " +  glGetString(GL_SHADING_LANGUAGE_VERSION));

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // NOTE: Anisotropy levels capability is an extension
            int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;

            rlglData.getExtSupported().setMaxAnisotropyLevel(glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

            if (SUPPORT_GL_DETAILS_INFO){
                // Show some OpenGL GPU capabilities
                Tracelog(LOG_INFO, "GL: OpenGL capabilities:");
                int capability = 0;
                capability = glGetInteger(GL_MAX_TEXTURE_SIZE);
                Tracelog(LOG_INFO, "    GL_MAX_TEXTURE_SIZE: " + capability);
                capability = glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE);
                Tracelog(LOG_INFO, "    GL_MAX_CUBE_MAP_TEXTURE_SIZE: " + capability);
                capability = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS);
                Tracelog(LOG_INFO, "    GL_MAX_TEXTURE_IMAGE_UNITS: " + capability);
                capability = glGetInteger(GL_MAX_VERTEX_ATTRIBS);
                Tracelog(LOG_INFO, "    GL_MAX_VERTEX_ATTRIBS: " + capability);

                if (!GRAPHICS_API_OPENGL_ES2){
                    capability = glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE);
                    Tracelog(LOG_INFO, "    GL_MAX_UNIFORM_BLOCK_SIZE: " + capability);
                    capability = glGetInteger(GL_MAX_DRAW_BUFFERS);
                    Tracelog(LOG_INFO, "    GL_MAX_DRAW_BUFFERS: " + capability);
                    if (rlglData.getExtSupported().isTexAnisoFilter())
                        Tracelog(LOG_INFO, "    GL_MAX_TEXTURE_MAX_ANISOTROPY: " + rlglData.getExtSupported().getMaxAnisotropyLevel());
                }
                capability = glGetInteger(GL_NUM_COMPRESSED_TEXTURE_FORMATS);
                Tracelog(LOG_INFO, "    GL_NUM_COMPRESSED_TEXTURE_FORMATS: " + capability);
                int[] format = new int[32];
                glGetInteger(GL_COMPRESSED_TEXTURE_FORMATS);
                for (int i = 0; i < capability; i++) {
                    Tracelog(LOG_INFO, "        " + rlGetCompressedFormatName(format[i]));
                }

                if (GRAPHICS_API_OPENGL_43) {
                    IntBuffer capabilityIB = IntBuffer.allocate(1);
                    glGetIntegerv(GL_MAX_VERTEX_ATTRIB_BINDINGS, capabilityIB);
                    Tracelog(LOG_INFO, "    GL_MAX_VERTEX_ATTRIB_BINDINGS: " + capabilityIB.get());
                    glGetIntegerv(GL_MAX_UNIFORM_LOCATIONS, capabilityIB);
                    Tracelog(LOG_INFO, "    GL_MAX_UNIFORM_LOCATIONS: " +  capabilityIB.get());
                }
            }
            else{   // SUPPORT_GL_DETAILS_INFO
                // Show some basic info about GL supported features
                if (GRAPHICS_API_OPENGL_ES2){
                    if (rlglData.getExtSupported().isVao())
                        Tracelog(LOG_INFO, "GL: VAO extension detected, VAO functions loaded successfully");
                    else
                        Tracelog(LOG_WARNING, "GL: VAO extension not found, VAO not supported");

                    if (rlglData.getExtSupported().isTexNPOT())
                        Tracelog(LOG_INFO, "GL: NPOT textures extension detected, full NPOT textures supported");
                    else
                        Tracelog(LOG_WARNING, "GL: NPOT textures extension not found, limited NPOT support (no-mipmaps, no-repeat)");
                }
                if (rlglData.getExtSupported().isTexCompDXT())
                    Tracelog(LOG_INFO, "GL: DXT compressed textures supported");
                if (rlglData.getExtSupported().isTexCompETC1())
                    Tracelog(LOG_INFO, "GL: ETC1 compressed textures supported");
                if (rlglData.getExtSupported().isTexCompETC2())
                    Tracelog(LOG_INFO, "GL: ETC2/EAC compressed textures supported");
                if (rlglData.getExtSupported().isTexCompPVRT())
                    Tracelog(LOG_INFO, "GL: PVRT compressed textures supported");
                if (rlglData.getExtSupported().isTexCompASTC())
                    Tracelog(LOG_INFO, "GL: ASTC compressed textures supported");
            }  // SUPPORT_GL_DETAILS_INFO
        }  // GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2
    }

    public static int rlGetVersion(){
        int version = 0;

        if (GRAPHICS_API_OPENGL_11){
            version = OPENGL_11;
        }
        else if (GRAPHICS_API_OPENGL_21){
            if (__APPLE__){
                version = OPENGL_33;           // NOTE: Force OpenGL 3.3 on OSX
            }
            else{
                version = OPENGL_21;
            }
        }
        else if (GRAPHICS_API_OPENGL_33){
            version = OPENGL_33;
        }
        else if (GRAPHICS_API_OPENGL_ES2){
            version = OPENGL_ES_20;
        }
        return version;
    }

    // Set current framebuffer width
    public static void rlSetFramebufferWidth(int width) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            rlglData.getState().framebufferWidth = width;
        }
    }

    // Set current framebuffer height
    public static void rlSetFramebufferHeight(int height) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            rlglData.getState().framebufferHeight = height;
        }
    }

    // Get current framebuffer width
    public static int rlGetFramebufferWidth(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            return rlglData.getState().getFramebufferWidth();
        }
        else{
            return 0;
        }
    }

    // Get current framebuffer height
    public static int rlGetFramebufferHeight(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            return rlglData.getState().getFramebufferHeight();
        }
        else{
            return 0;
        }
    }

    // Get default internal texture (white texture)
    // NOTE: Default texture is a 1x1 pixel UNCOMPRESSED_R8G8B8A8
    public static int rlGetTextureIdDefault() {
        int id = 0;
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = rlglData.getState().defaultTextureId;
        }
        return id;
    }

    // Get default shader id
    public static int rlGetShaderIdDefault() {
        int id = 0;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = rlglData.getState().defaultShaderId;
        }
        return id;
    }

    // Get default shader locs
    public static int[] rlGetShaderLocsDefault() {
        int[] locs = null;
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            locs = rlglData.getState().defaultShaderLocs;
        }
        return locs;
    }


    //Load render batch
    static rlRenderBatch rlLoadRenderBatch(int numBuffers, int bufferElements){
        rlRenderBatch batch = new rlRenderBatch();

        // Initialize CPU (RAM) vertex buffers (position, texcoord, color data and indexes)
        //--------------------------------------------------------------------------------------------
        batch.rlVertexBuffer = new rlVertexBuffer[numBuffers];

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            for(int i = 0; i < batch.rlVertexBuffer.length; i++){
                batch.rlVertexBuffer[i] = new rlVertexBuffer();
            }

            for(int i = 0; i < numBuffers; i++){
                batch.rlVertexBuffer[i].elementCount = bufferElements;

                batch.rlVertexBuffer[i].setVertices(new float[bufferElements * 3 * 4 * Float.BYTES]);
                // 3 float by vertex, 4 vertex by quad
                batch.rlVertexBuffer[i].setTexcoords(new float[bufferElements * 4 * 2 * Float.BYTES]);
                // 2 float by texcoord, 4 texcoord by quad
                batch.rlVertexBuffer[i].setColors(new byte[bufferElements * 4 * 4 * Byte.BYTES]);
                // 4 float by color, 4 colors by quad
                if(GRAPHICS_API_OPENGL_33){
                    batch.getVertexBuffer()[i].setIndices_GL11(new int[bufferElements * 6 * Integer.BYTES]);
                }// 6 int by quad (indices)
                else if(GRAPHICS_API_OPENGL_ES2){
                    batch.rlVertexBuffer[i].setIndices_ES20(new short[bufferElements * 6 * Short.SIZE]);
                    // 6 int by quad (indices)
                }

                for(int j = 0; j < (3 * 4 * bufferElements); j++){
                    batch.rlVertexBuffer[i].vertices[j] = 0.0f;
                }
                for(int j = 0; j < (2 * 4 * bufferElements); j++){
                    batch.rlVertexBuffer[i].texcoords[j] = 0.0f;
                }
                for(int j = 0; j < (4 * 4 * bufferElements); j++){
                    batch.rlVertexBuffer[i].colors[j] = 0;
                }

                int k = 0;

                if(GRAPHICS_API_OPENGL_33){
                    // Indices can be initialized right now
                    for(int j = 0; j < (6 * bufferElements); j += 6){
                        batch.rlVertexBuffer[i].getIndices_GL11()[j] = 4 * k;
                        batch.rlVertexBuffer[i].getIndices_GL11()[j + 1] = 4 * k + 1;
                        batch.rlVertexBuffer[i].getIndices_GL11()[j + 2] = 4 * k + 2;
                        batch.rlVertexBuffer[i].getIndices_GL11()[j + 3] = 4 * k;
                        batch.rlVertexBuffer[i].getIndices_GL11()[j + 4] = 4 * k + 2;
                        batch.rlVertexBuffer[i].getIndices_GL11()[j + 5] = 4 * k + 3;

                        k++;
                    }
                }

                if(GRAPHICS_API_OPENGL_ES2){
                    // Indices can be initialized right now
                    for(int j = 0; j < (6 * bufferElements); j += 6){
                        batch.rlVertexBuffer[i].getIndices_ES20()[j] = (short) (4 * k);
                        batch.rlVertexBuffer[i].getIndices_ES20()[j + 1] = (short) (4 * k + 1);
                        batch.rlVertexBuffer[i].getIndices_ES20()[j + 2] = (short) (4 * k + 2);
                        batch.rlVertexBuffer[i].getIndices_ES20()[j + 3] = (short) (4 * k);
                        batch.rlVertexBuffer[i].getIndices_ES20()[j + 4] = (short) (4 * k + 2);
                        batch.rlVertexBuffer[i].getIndices_ES20()[j + 5] = (short) (4 * k + 3);

                        k++;
                    }
                }

                rlglData.getState().vertexCounter = 0;
            }

            Tracelog(LOG_INFO, "RLGL: Internal vertex buffers initialized successfully in RAM (CPU)");
            //--------------------------------------------------------------------------------------------
            // Upload to GPU (VRAM) vertex data and initialize VAOs/VBOs
            //--------------------------------------------------------------------------------------------

            for(int i = 0; i < numBuffers; i++){
                if(rlglData.getExtSupported().isVao()){
                    // Initialize Quads VAO
                    batch.rlVertexBuffer[i].setVaoId(glGenVertexArrays());
                    glBindVertexArray(batch.rlVertexBuffer[i].getVaoId());
                }

                // Quads - Vertex buffers binding and attributes enable
                // Vertex position buffer (shader-location = 0)
                batch.rlVertexBuffer[i].vboId[0] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[i].vboId[0]);
                glBufferData(GL_ARRAY_BUFFER, batch.rlVertexBuffer[i].vertices, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_POSITION]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_POSITION],
                        3, GL_FLOAT, false, 0, 0);

                // Vertex texcoord buffer (shader-location = 1)
                batch.rlVertexBuffer[i].vboId[1] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[i].vboId[1]);
                glBufferData(GL_ARRAY_BUFFER, batch.rlVertexBuffer[i].texcoords, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_TEXCOORD01]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_TEXCOORD01],
                        2, GL_FLOAT, false, 0, 0);

                // Vertex color buffer (shader-location = 3)
                ByteBuffer colours = ByteBuffer.allocateDirect(batch.rlVertexBuffer[i].colors.length);
                colours.put(batch.rlVertexBuffer[i].colors).flip();
                batch.rlVertexBuffer[i].vboId[2] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[i].vboId[2]);
                glBufferData(GL_ARRAY_BUFFER, colours, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_COLOR]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_COLOR],
                        4, GL_UNSIGNED_BYTE, true, 0, 0);

                // Fill index buffer
                batch.rlVertexBuffer[i].vboId[3] = glGenBuffers();
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batch.rlVertexBuffer[i].vboId[3]);
                if(GRAPHICS_API_OPENGL_33){
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, batch.rlVertexBuffer[i].getIndices_GL11(), GL_STATIC_DRAW);
                }
                else if(GRAPHICS_API_OPENGL_ES2){
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, batch.rlVertexBuffer[i].getIndices_ES20(), GL_STATIC_DRAW);
                }
            }

            Tracelog(LOG_INFO, "RLGL: Render batch vertex buffers loaded successfully in VRAM (GPU)");

            // Unbind the current VAO
            if(rlglData.getExtSupported().isVao()){
                glBindVertexArray(0);
            }

            //--------------------------------------------------------------------------------------------
            // Init draw calls tracking system
            //--------------------------------------------------------------------------------------------
            batch.draws = new rlDrawCall[DEFAULT_BATCH_DRAWCALLS];
            for(int i = 0; i < batch.draws.length; i++){
                batch.draws[i] = new rlDrawCall();
            }
            for(int i = 0; i < DEFAULT_BATCH_DRAWCALLS; i++){
                batch.draws[i].mode = RL_QUADS;
                batch.draws[i].vertexCount = 0;
                batch.draws[i].vertexAlignment = 0;
                batch.draws[i].textureId = rlglData.getState().getDefaultTextureId();
            }

            batch.bufferCount = numBuffers;    // Record buffer count
            batch.drawCounter = 1;             // Reset draws counter
            batch.currentDepth = -1.0f;         // Reset depth value
            //--------------------------------------------------------------------------------------------
        }

        return batch;
    }

    // Unload default internal buffers vertex data from CPU and GPU
    static void UnloadRenderBatch(rlRenderBatch batch){
        // Unbind everything
        if (rlglData.getExtSupported().isVao()){
            glBindVertexArray(0);
        }
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Unload all vertex buffers data
        for (int i = 0; i < batch.bufferCount; i++){
            // Delete VBOs from GPU (VRAM)
            glDeleteBuffers(batch.rlVertexBuffer[i].vboId[0]);
            glDeleteBuffers(batch.rlVertexBuffer[i].vboId[1]);
            glDeleteBuffers(batch.rlVertexBuffer[i].vboId[2]);
            glDeleteBuffers(batch.rlVertexBuffer[i].vboId[3]);

            // Delete VAOs from GPU (VRAM)
            if (rlglData.getExtSupported().isVao()){
                glDeleteVertexArrays(batch.rlVertexBuffer[i].vaoId);
            }
        }

        // Unload arrays
        batch.setVertexBuffer(null);
        batch.setDraws(null);
    }

    //Draw render batch
    // NOTE: We require a pointer to reset batch and increase current buffer (multi-buffer)
    static void rlDrawRenderBatch(rlRenderBatch batch){
        // Update batch vertex buffers
        //------------------------------------------------------------------------------------------------------------
        // NOTE: If there is not vertex data, buffers doesn't need to be updated (vertexCount > 0)
        // TODO: If no data changed on the CPU arrays --> No need to re-update GPU arrays (change flag required)
        if (rlglData.getState().vertexCounter > 0){
            // Activate elements VAO
            if (rlglData.getExtSupported().isVao()){
                glBindVertexArray(batch.rlVertexBuffer[batch.currentBuffer].vaoId);
            }

            // Vertex positions buffer
            glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[batch.getCurrentBuffer()].vboId[0]);
            glBufferSubData(GL_ARRAY_BUFFER, 0, batch.rlVertexBuffer[batch.getCurrentBuffer()].vertices);
            //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*3*4*batch->rlVertexBuffer[batch->currentBuffer].elementsCount,
            // batch->rlVertexBuffer[batch->currentBuffer].vertices, GL_DYNAMIC_DRAW);
            // Update all buffer

            // Texture coordinates buffer
            glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[batch.getCurrentBuffer()].vboId[1]);
            glBufferSubData(GL_ARRAY_BUFFER, 0, batch.rlVertexBuffer[batch.getCurrentBuffer()].texcoords);
            //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*2*4*batch->rlVertexBuffer[batch->currentBuffer].elementsCount,
            // batch->rlVertexBuffer[batch->currentBuffer].texcoords, GL_DYNAMIC_DRAW);
            // Update all buffer

            ByteBuffer colours = ByteBuffer.allocateDirect(batch.rlVertexBuffer[batch.getCurrentBuffer()].colors.length);
            colours.put(batch.rlVertexBuffer[batch.getCurrentBuffer()].colors).flip();

            // Colors buffer
            glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[batch.getCurrentBuffer()].vboId[2]);
            glBufferSubData(GL_ARRAY_BUFFER, 0, colours);
            //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*4*4*batch->rlVertexBuffer[batch->currentBuffer].elementsCount,
            // batch->rlVertexBuffer[batch->currentBuffer].colors, GL_DYNAMIC_DRAW);
            // Update all buffer

            // NOTE: glMapBuffer() causes sync issue.
            // If GPU is working with this buffer, glMapBuffer() will wait(stall) until GPU to finish its job.
            // To avoid waiting (idle), you can call first glBufferData() with null pointer before glMapBuffer().
            // If you do that, the previous data in PBO will be discarded and glMapBuffer() returns a new
            // allocated pointer immediately even if GPU is still working with the previous data.

            // Another option: map the buffer object into client's memory
            // Probably this code could be moved somewhere else...
            // batch.rlVertexBuffer[batch.currentBuffer].vertices = (float *)glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
            // if (batch.rlVertexBuffer[batch.currentBuffer].vertices)
            // {
            // Update vertex data
            // }
            // glUnmapBuffer(GL_ARRAY_BUFFER);

            // Unbind the current VAO
            if (rlglData.getExtSupported().isVao()){
                glBindVertexArray(0);
            }
        }

        //------------------------------------------------------------------------------------------------------------
        // Draw batch vertex buffers (considering VR stereo if required)
        //------------------------------------------------------------------------------------------------------------
        Matrix matProjection = rlglData.getState().getProjection();
        Matrix matModelView = rlglData.getState().getModelview();

        int eyesCount = rlglData.getState().isStereoRender() ? 2 : 1;

        for (int eye = 0; eye < eyesCount; eye++){
            if (eyesCount == 2){
                // Setup current eye viewport (half screen width)
                rlViewport(eye * rlglData.getState().getFramebufferWidth() / 2, 0, rlglData.getState().getFramebufferWidth() / 2, rlglData.getState().getFramebufferHeight());

                // Set current eye view offset to modelview matrix
                rlSetMatrixModelview(MatrixMultiply(matModelView, rlglData.getState().getViewOffsetStereo()[eye]));
                // Set current eye projection matrix
                rlSetMatrixProjection(rlglData.getState().getProjectionStereo()[eye]);
            }
            // Draw buffers
            if (rlglData.getState().vertexCounter > 0){
                // Set current shader and upload current MVP matrix
                glUseProgram(rlglData.getState().currentShaderId);

                // Create modelview-projection matrix and upload to shader
                Matrix matMVP = MatrixMultiply(rlglData.getState().getModelview(), rlglData.getState().getProjection());
                glUniformMatrix4fv(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_MATRIX_MVP],false, MatrixToFloat(matMVP));

                if (rlglData.getExtSupported().isVao()){
                    glBindVertexArray(batch.rlVertexBuffer[batch.currentBuffer].vaoId);
                }
                else{
                    // Bind vertex attrib: position (shader-location = 0)
                    glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[batch.currentBuffer].vboId[0]);
                    glVertexAttribPointer(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_POSITION],
                            3, GL_FLOAT, false, 0, 0);
                    glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_POSITION]);

                    // Bind vertex attrib: texcoord (shader-location = 1)
                    glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[batch.currentBuffer].vboId[1]);
                    glVertexAttribPointer(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_TEXCOORD01],
                            2, GL_FLOAT, false, 0, 0);
                    glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_TEXCOORD01]);

                    // Bind vertex attrib: color (shader-location = 3)
                    glBindBuffer(GL_ARRAY_BUFFER, batch.rlVertexBuffer[batch.currentBuffer].vboId[2]);
                    glVertexAttribPointer(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_COLOR],
                            4, GL_UNSIGNED_BYTE, true, 0, 0);
                    glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_VERTEX_COLOR]);

                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batch.rlVertexBuffer[batch.currentBuffer].vboId[3]);
                }

                // Setup some default shader values
                glUniform4f(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_COLOR_DIFFUSE], 1.0f, 1.0f, 1.0f, 1.0f);
                glUniform1i(rlglData.getState().currentShaderLocs[RL_SHADER_LOC_MAP_DIFFUSE], 0); // Active default sampler2D: texture0

                // Activate additional sampler textures
                // Those additional textures will be common for all draw calls of the batch
                for (int i = 0; i < RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS; i++){
                    if (rlglData.getState().getActiveTextureId()[i] > 0){
                        glActiveTexture(GL_TEXTURE0 + 1 + i);
                        glBindTexture(GL_TEXTURE_2D, rlglData.getState().getActiveTextureId()[i]);
                    }
                }

                // Activate default sampler2D texture0 (one texture is always active for default batch shader)
                // NOTE: Batch system accumulates calls by texture0 changes, additional textures are enabled for all the draw calls
                glActiveTexture(GL_TEXTURE0);

                for (int i = 0, vertexOffset = 0; i < batch.drawCounter; i++){
                    // Bind current draw call texture, activated as GL_TEXTURE0 and binded to sampler2D texture0 by default
                    glBindTexture(GL_TEXTURE_2D, batch.draws[i].textureId);

                    if ((batch.draws[i].mode == RL_LINES) || (batch.draws[i].mode == RL_TRIANGLES)){
                        glDrawArrays(batch.draws[i].mode, vertexOffset, batch.draws[i].vertexCount);
                    }
                    else{
                        if (GRAPHICS_API_OPENGL_33){
                            // We need to define the number of indices to be processed: elementCount*6
                            // NOTE: The final parameter tells the GPU the offset in bytes from the
                            // start of the index buffer to the location of the first index to process
                            glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_INT,
                                    (vertexOffset/4* 6L *Integer.BYTES));
                        }
                        else if (GRAPHICS_API_OPENGL_ES2){
                            glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_SHORT,
                                    (vertexOffset/4*6L*Short.BYTES));
                        }
                    }

                    vertexOffset += (batch.draws[i].vertexCount + batch.draws[i].vertexAlignment);
                }

                if (!rlglData.getExtSupported().isVao()){
                    glBindBuffer(GL_ARRAY_BUFFER, 0);
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                }

                glBindTexture(GL_TEXTURE_2D, 0);    // Unbind textures
            }

            if (rlglData.getExtSupported().isVao()){
                glBindVertexArray(0); // Unbind VAO
            }

            glUseProgram(0);    // Unbind shader program
        }

        // Restore viewport to default measures
        if (eyesCount == 2) {
            rlViewport(0, 0, rlglData.getState().framebufferWidth, rlglData.getState().framebufferHeight);
        }
        //------------------------------------------------------------------------------------------------------------
        // Reset batch buffers
        //------------------------------------------------------------------------------------------------------------
        // Reset vertex counter for next frame
        rlglData.getState().vertexCounter = 0;

        // Reset depth for next draw
        batch.currentDepth = -1.0f;

        // Restore projection/modelview matrices
        rlglData.getState().setProjection(matProjection);
        rlglData.getState().setModelview(matModelView);

        // Reset rlglData.getCurrentBatch().draws array
        for (int i = 0; i < RL_DEFAULT_BATCH_DRAWCALLS; i++){
            batch.draws[i].mode = RL_QUADS;
            batch.draws[i].vertexCount = 0;
            batch.draws[i].textureId = rlglData.getState().defaultTextureId;
        }

        // Reset active texture units for next batch
        for (int i = 0; i < RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS; i++){
            rlglData.getState().getActiveTextureId()[i] = 0;
        }

        // Reset draws counter to one draw for the batch
        batch.drawCounter = 1;
        //------------------------------------------------------------------------------------------------------------

        // Change to next buffer in the list (in case of multi-buffering)
        batch.currentBuffer++;
        if (batch.currentBuffer >= batch.bufferCount){
            batch.currentBuffer = 0;
        }
    }

    // Set the active render batch for rlgl
    void rlSetRenderBatchActive(rlRenderBatch batch){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlDrawRenderBatch(rlglData.getCurrentBatch());

            if (batch != null){
                rlglData.setCurrentBatch(batch);
            }
            else{
                rlglData.setCurrentBatch(rlglData.getDefaultBatch());
            }
        }
    }

    // Update and draw internal render batch
    public void rlDrawRenderBatchActive(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlDrawRenderBatch(rlglData.getCurrentBatch());    // NOTE: Stereo rendering is checked inside
        }

    }

    // Check internal buffer overflow for a given number of vertex
    // and force a rlRenderBatch draw call if required
    public static boolean rlCheckRenderBatchLimit(int vCount){
        boolean overflow = false;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if((rlglData.getState().vertexCounter + vCount) >=
                    (rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].elementCount * 4)) {
                 int currentMode = rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode;
                 int currentTexture = rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId;

                 overflow = true;
                 rlDrawRenderBatch(rlglData.getCurrentBatch());

                 rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode = currentMode;
                 rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId = currentTexture;
            }
        }

        return overflow;
    }


    // Convert image data to OpenGL texture (returns OpenGL valid Id)
    public static int rlLoadTexture(DataBuffer data, int width, int height, int format, int mipmapCount){
        glBindTexture(GL_TEXTURE_2D, 0);    // Free any old binding

        int id = 0;

        // Check texture format support by OpenGL 1.1 (compressed textures not supported)
        if (GRAPHICS_API_OPENGL_11){
            if (format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB){
                Tracelog(LOG_WARNING, "GL: OpenGL 1.1 does not support GPU compressed texture formats");
                return id;
            }
        }
        else{
            if ((!rlglData.getExtSupported().isTexCompDXT()) && ((format == RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) ||
                    (format == RL_PIXELFORMAT_COMPRESSED_DXT1_RGBA) || (format == RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA) ||
                    (format == RL_PIXELFORMAT_COMPRESSED_DXT5_RGBA))){
                Tracelog(LOG_WARNING, "GL: DXT compressed texture format not supported");
                return id;
            }
            if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
                if ((!rlglData.getExtSupported().isTexCompETC1()) && (format == RL_PIXELFORMAT_COMPRESSED_ETC1_RGB)){
                    Tracelog(LOG_WARNING, "GL: ETC1 compressed texture format not supported");
                    return id;
                }

                if ((!rlglData.getExtSupported().isTexCompETC2()) && ((format == RL_PIXELFORMAT_COMPRESSED_ETC2_RGB) ||
                        (format == RL_PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA))){
                    Tracelog(LOG_WARNING, "GL: ETC2 compressed texture format not supported");
                    return id;
                }

                if ((!rlglData.getExtSupported().isTexCompPVRT()) && ((format == RL_PIXELFORMAT_COMPRESSED_PVRT_RGB) ||
                        (format == RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA))){
                    Tracelog(LOG_WARNING, "GL: PVRT compressed texture format not supported");
                    return id;
                }

                if ((!rlglData.getExtSupported().isTexCompASTC()) && ((format == RL_PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA) ||
                        (format == RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA))){
                    Tracelog(LOG_WARNING, "GL: ASTC compressed texture format not supported");
                    return id;
                }
            }
        }      // GRAPHICS_API_OPENGL_11

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        id = glGenTextures();              // Generate texture id

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // glActiveTexture(GL_TEXTURE0);     // If not defined, using GL_TEXTURE0 by default (shader texture)
        }

        glBindTexture(GL_TEXTURE_2D, id);

        int mipWidth = width;
        int mipHeight = height;
        int mipOffset = 0;          // Mipmap data offset

        // Load the different mipmap levels
        for (int i = 0; i < mipmapCount; i++){
            int mipSize = rlGetPixelDataSize(mipWidth, mipHeight, format);

            //using globals here to get around pointers
            rlGetGlTextureFormats(format);

            Tracelog.Tracelog("TEXTURE: Load mipmap level " + i + " (" + mipWidth + " x " + mipHeight + "), size: " +
                    mipSize + ", offset: " + mipOffset);

            if (glInternalFormat != -1){
                if (format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB){
                    if(data == null){
                        glTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, glFormat,
                                     glType, (ByteBuffer) null);
                    }
                    else{
                        switch (data.getDataType()){
                        /*
                         0 = byte
                         1 = uShort
                         2 = Short
                         3 = int
                         4 = float
                         5 = double
                         */
                            case 0:{
                                ByteBuffer buffer = ByteBuffer.allocateDirect(data.getSize());
                                for (int s = 0; s < data.getSize(); s++){
                                    buffer.put((byte) data.getElem(s));
                                }
                                buffer.flip();
                                glTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, glFormat,
                                        glType, buffer);
                                break;
                            }
                            case 1:
                            case 2:{
                                short[] dataS = new short[data.getSize()];
                                for (int s = 0; s < dataS.length; s++){
                                    dataS[s] = (short) data.getElem(s);
                                }
                                glTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, glFormat, glType,
                                        dataS);
                                break;
                            }
                            case 4:
                            case 5:
                            case 6:
                            default:{
                                int[] dataI = new int[data.getSize()];
                                for (int s = 0; s < dataI.length; s++){
                                    dataI[s] = data.getElem(s);
                                }
                                glTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, glFormat, glType,
                                        dataI);
                                break;
                            }

                        }
                    }

                }
                else{
                    if (!GRAPHICS_API_OPENGL_11){
                        glCompressedTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, mipSize,
                                data.getSize() + mipOffset);
                    }
                }

                int[] swizzleMask = new int[4];
                if (GRAPHICS_API_OPENGL_33){
                    if (format == RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE){
                        swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ONE};
                        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                    }
                    else if (format == RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA){
                        if (GRAPHICS_API_OPENGL_21){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ALPHA};
                        }
                        else if (GRAPHICS_API_OPENGL_33){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_GREEN};
                        }
                        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                    }
                }
            }

            mipWidth /= 2;
            mipHeight /= 2;
            mipOffset += mipSize;

            // Security check for NPOT textures
            if (mipWidth < 1){
                mipWidth = 1;
            }
            if (mipHeight < 1){
                mipHeight = 1;
            }
        }


        // Texture parameters configuration
        // NOTE: glTexParameteri does NOT affect texture uploading, just the way it's used
        if (GRAPHICS_API_OPENGL_ES2){
            // NOTE: OpenGL ES 2.0 with no GL_OES_texture_npot support (i.e. WebGL) has limited NPOT support, so CLAMP_TO_EDGE must be used
            if (rlglData.getExtSupported().isTexNPOT()){
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);       // Set texture to repeat on x-axis
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);       // Set texture to repeat on y-axis
            }
            else{
                // NOTE: If using negative texture coordinates (LoadOBJ()), it does not work!
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);       // Set texture to clamp on x-axis
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);       // Set texture to clamp on y-axis
            }
        }
        else{
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);       // Set texture to repeat on x-axis
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);       // Set texture to repeat on y-axis
        }

        // Magnification and minification filters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);  // Alternative: GL_LINEAR
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);  // Alternative: GL_LINEAR

        if (GRAPHICS_API_OPENGL_33){
            if (mipmapCount > 1){
                // Activate Trilinear filtering if mipmaps are available
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            }
        }

        // At this point we have the texture loaded in GPU and texture parameters configured

        // NOTE: If mipmaps were not in data, they are not generated automatically

        // Unbind current texture
        glBindTexture(GL_TEXTURE_2D, 0);

        if (id > 0){
            Tracelog(LOG_INFO, "TEXTURE: [ID " + id + "] Texture loaded successfully (" + width + "x" + height +
                    " | " + rlGetPixelFormatName(format) + " | " + mipmapCount + " mipmaps)");
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: Failed to load texture");
        }

        return id;
    }

    // Load depth texture/renderbuffer (to be attached to fbo)
    // WARNING: OpenGL ES 2.0 requires GL_OES_depth_texture/WEBGL_depth_texture extensions
    public static int rlLoadTextureDepth(int width, int height, boolean useRenderBuffer){
        int id = 0;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // In case depth textures not supported, we force renderbuffer usage
            if (!rlglData.getExtSupported().isTexDepth()){
                useRenderBuffer = true;
            }

            // NOTE: We let the implementation to choose the best bit-depth
            // Possible formats: GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT32 and GL_DEPTH_COMPONENT32F
            int glInternalFormat = GL_DEPTH_COMPONENT;

            if (GRAPHICS_API_OPENGL_ES2){
                if (rlglData.getExtSupported().getMaxDepthBits() == 32){
                    glInternalFormat = GL_DEPTH_COMPONENT32_OES;
                }
                else if (rlglData.getExtSupported().getMaxDepthBits() == 24){
                    glInternalFormat = GL_DEPTH_COMPONENT24_OES;
                }
                else{
                    glInternalFormat = GL_DEPTH_COMPONENT16;
                }
            }

            if (!useRenderBuffer && rlglData.getExtSupported().isTexDepth()){
                id = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, id);
                glTexImage2D(GL_TEXTURE_2D, 0, glInternalFormat, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (int[]) null);

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

                glBindTexture(GL_TEXTURE_2D, 0);

                Tracelog(LOG_INFO, "TEXTURE: Depth texture loaded successfully");
            }
            else{
                // Create the renderbuffer that will serve as the depth attachment for the framebuffer
                // NOTE: A renderbuffer is simpler than a texture and could offer better performance on embedded devices
                id = glGenRenderbuffers();
                glBindRenderbuffer(GL_RENDERBUFFER, id);
                glRenderbufferStorage(GL_RENDERBUFFER, glInternalFormat, width, height);

                glBindRenderbuffer(GL_RENDERBUFFER, 0);
                int logTmp = rlglData.getExtSupported().getMaxDepthBits() >= 24 ? rlglData.getExtSupported().getMaxDepthBits() : 16;
                Tracelog(LOG_INFO, "TEXTURE: [ID " + id + "] Depth renderbuffer loaded successfully (" + logTmp + " bits)");

            }
        }
        return id;
    }

    // Load texture cubemap
    // NOTE: Cubemap data is expected to be 6 images in a single data array (one after the other),
    // expected the following convention: +X, -X, +Y, -Y, +Z, -Z
    public static int rlLoadTextureCubemap(byte[] data, int size, int format){
        int id = 0;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int dataSize = rlGetPixelDataSize(size, size, format);

            id = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, id);

            rlGetGlTextureFormats(format);

            if (glInternalFormat != -1){
                // Load cubemap faces
                for (int i = 0; i < 6; i++){
                    if (data == null){
                        if (format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB){
                            if (format == RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32){
                                // Instead of using a sized internal texture format (GL_RGB16F, GL_RGB32F), we let the driver to choose the better format for us (GL_RGB)
                                if (rlglData.getExtSupported().isTexFloat32()){
                                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, size, size,
                                            0, GL_RGB, GL_FLOAT, (int[]) null);
                                }
                                else{
                                    Tracelog(LOG_WARNING, "TEXTURES: Cubemap requested format not supported");
                                }
                            }
                            else if ((format == RL_PIXELFORMAT_UNCOMPRESSED_R32) || (format == RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32)){
                                Tracelog(LOG_WARNING, "TEXTURES: Cubemap requested format not supported");
                            }
                            else{
                                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, size, size, 0,
                                        glFormat, glType, (int[]) null);
                            }
                        }
                        else{
                            Tracelog(LOG_WARNING, "TEXTURES: Empty cubemap creation does not support compressed " +
                                    "format");
                        }
                    }
                    else{
                        if (format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB){
                            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, size, size, 0,
                                         glFormat, glType, (long) data[i] * dataSize);
                        }
                        else{
                            glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, size,
                                    size, 0, dataSize, (long) data[i] * dataSize);
                        }
                    }
                    int[] swizzleMask = new int[16];

                    if (GRAPHICS_API_OPENGL_33){
                        if (format == RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ONE};
                            glTexParameteriv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                        }
                        else if (format == RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA){
                            if (GRAPHICS_API_OPENGL_21){
                                swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ALPHA};
                            }
                            else if (GRAPHICS_API_OPENGL_33){
                                swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_GREEN};
                            }
                            glTexParameteriv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                        }
                    }
                }
            }

            // Set cubemap texture sampling parameters
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            if (GRAPHICS_API_OPENGL_33){
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);  // Flag not supported on OpenGL ES 2.0
            }

            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        }

        if (id > 0){
            Tracelog(LOG_INFO, "TEXTURE: [ID " + id + "] Cubemap texture created successfully (" + size + "x" + size + ")");
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: Failed to load cubemap texture");
        }

        return id;
    }

    public static void rlUpdateTexture(int id, int offsetX, int offsetY, int width, int height, int format, byte[] data){
        glBindTexture(GL_TEXTURE_2D, id);

        rlGetGlTextureFormats(format);

        if ((glInternalFormat != -1) && (format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB)){
            ByteBuffer bb = ByteBuffer.allocateDirect(data.length);
            bb.put(data).flip();
            glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, width, height, glFormat, glType, bb);
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: [ID " + id + "] Failed to update for current texture format (" + format + ")");
        }
    }

    // Get OpenGL internal formats and data type from raylib rlPixelFormat
    public static void rlGetGlTextureFormats(int format){
        glInternalFormat = 0;
        glFormat = 0;
        glType = 0;

        switch (format){
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_LUMINANCE;
                    glFormat = GL_LUMINANCE;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_R8;
                    glFormat = GL_RED;
                    glType = GL_UNSIGNED_BYTE;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_LUMINANCE_ALPHA;
                    glFormat = GL_LUMINANCE_ALPHA;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RG8;
                    glFormat = GL_RG;
                    glType = GL_UNSIGNED_BYTE;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGB;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_SHORT_5_6_5;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGB565;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_SHORT_5_6_5;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGB;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGB8;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_BYTE;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGBA;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_5_5_5_1;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGB5_A1;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_5_5_5_1;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGBA;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_4_4_4_4;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGBA4;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_4_4_4_4;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGBA;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if (GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGBA8;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_BYTE;
                }
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_LUMINANCE;
                        glFormat = GL_LUMINANCE;
                        glType = GL_FLOAT;
                    }
                }
                if (GRAPHICS_API_OPENGL_33){
                    if (rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_R32F;
                        glFormat = GL_RED;
                        glType = GL_FLOAT;
                    }
                } // NOTE: Requires extension OES_texture_float
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGB;
                        glFormat = GL_RGB;
                        glType = GL_FLOAT;
                    }
                }
                if (GRAPHICS_API_OPENGL_33){
                    if (rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGB32F;
                        glFormat = GL_RGB;
                        glType = GL_FLOAT;
                    }
                }    // NOTE: Requires extension OES_texture_float
                break;
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGBA;
                        glFormat = GL_RGBA;
                        glType = GL_FLOAT;
                    }
                }
                if (GRAPHICS_API_OPENGL_33){
                    if (rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGBA32F;
                    }
                    glFormat = GL_RGBA;
                    glType = GL_FLOAT;
                } // NOTE: Requires extension OES_texture_float
                break;
            case RL_PIXELFORMAT_COMPRESSED_DXT1_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                    }
                }
                break;
            case RL_PIXELFORMAT_COMPRESSED_DXT1_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
                    }
                }
                break;
            case RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                    }
                }
                break;
            case RL_PIXELFORMAT_COMPRESSED_DXT5_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                    }
                }
                break;
            case RL_PIXELFORMAT_COMPRESSED_ETC1_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompETC1()){
                        glInternalFormat = GL_ETC1_RGB8_OES;
                    }
                }  // NOTE: Requires OpenGL ES 2.0 or OpenGL 4.3
                break;
            case RL_PIXELFORMAT_COMPRESSED_ETC2_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompETC2()){
                        glInternalFormat = GL_COMPRESSED_RGB8_ETC2;
                    }
                }      // NOTE: Requires OpenGL ES 3.0 or OpenGL 4.3
                break;
            case RL_PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompETC2()){
                        glInternalFormat = GL_COMPRESSED_RGBA8_ETC2_EAC;
                    }
                }    // NOTE: Requires OpenGL ES 3.0 or OpenGL 4.3
                break;
            case RL_PIXELFORMAT_COMPRESSED_PVRT_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompPVRT()){
                        glInternalFormat = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
                    }
                } // NOTE: Requires PowerVR GPU
                break;
            case RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompPVRT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
                    }
                } // NOTE: Requires PowerVR GPU
                break;
            case RL_PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompASTC()){
                        glInternalFormat = GL_COMPRESSED_RGBA_ASTC_4x4_KHR;
                    }
                } // NOTE: Requires OpenGL ES 3.1 or OpenGL 4.3
                break;
            case RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompASTC()){
                        glInternalFormat = GL_COMPRESSED_RGBA_ASTC_8x8_KHR;
                    }
                }  // NOTE: Requires OpenGL ES 3.1 or OpenGL 4.3
                break;
            default:
                if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    throw new IllegalStateException("Unexpected value: " + format);
                }
                if (!GRAPHICS_API_OPENGL_11){
                    Tracelog(LOG_WARNING, "TEXTURE: Current format not supported (" + format + ")");
                }
                break;
        }
    }

    public static void rlUnloadTexture(int id){
        glDeleteTextures(id);
    }

    // Generate mipmap data for selected texture
    public static void rlGenTextureMipmaps(Texture2D texture){
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        // Check if texture is power-of-two (POT)
        boolean texIsPOT = ((texture.getWidth() > 0) && ((texture.getWidth() & (texture.getWidth() - 1)) == 0)) &&
                ((texture.getHeight() > 0) && ((texture.getHeight() & (texture.getHeight() - 1)) == 0));

        if (GRAPHICS_API_OPENGL_11){
            if (texIsPOT){
                // WARNING: Manual mipmap generation only works for RGBA 32bit textures!
                if (texture.getFormat() == RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8){
                    // Retrieve texture data from VRAM
                    byte[] texData = rlReadTexturePixels(texture.id, texture.width, texture.height, texture.format);

                    // NOTE: Texture data size is reallocated to fit mipmaps data
                    // NOTE: CPU mipmap generation only supports RGBA 32bit data
                    int mipmapCount = rlGenTextureMipmapsData(texData, texture.getWidth(), texture.getHeight());

                    int size = texture.getWidth() * texture.getHeight() * 4;
                    int offset = size;

                    int mipWidth = texture.getWidth() / 2;
                    int mipHeight = texture.getHeight() / 2;

                    // Load the mipmaps
                    for (int level = 1; level < mipmapCount; level++){
                        glTexImage2D(GL_TEXTURE_2D, level, GL_RGBA8, mipWidth, mipHeight, 0, GL_RGBA,
                                GL_UNSIGNED_BYTE, texData.length + offset);

                        size = mipWidth * mipHeight * 4;
                        offset += size;

                        mipWidth /= 2;
                        mipHeight /= 2;
                    }

                    texture.setMipmaps(mipmapCount + 1);
                    texData = null;
                    // Once mipmaps have been generated and data has been uploaded to GPU VRAM, we can discard RAM data

                    Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.getId() + "] Mipmaps generated manually on CPU side, total: " + texture.getMipmaps());
                }
                else{
                    Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.getId() + "] Failed to generate mipmaps for provided texture format");
                }
            }
        }
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if ((texIsPOT) || (rlglData.getExtSupported().isTexNPOT())){
                //glHint(GL_GENERATE_MIPMAP_HINT, GL_DONT_CARE);   // Hint for mipmaps generation algorithm: GL_FASTEST, GL_NICEST, GL_DONT_CARE
                glGenerateMipmap(GL_TEXTURE_2D);    // Generate mipmaps automatically

                texture.setMipmaps(1 + (int) Math.floor(Math.log(Math.max(texture.getWidth(), texture.getHeight())) / Math.log(2)));
                Tracelog(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Mipmaps generated automatically, total: " + texture.getMipmaps());
            }
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.getId() + "] Failed to generate mipmaps");
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Read texture pixel data
    public static byte[] rlReadTexturePixels(int id, int width, int height, int format){
        byte[] pixels = null;

        if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            glBindTexture(GL_TEXTURE_2D, id);

            // NOTE: Using texture.id, we can retrieve some texture info (but not on OpenGL ES 2.0)
            // Possible texture info: GL_TEXTURE_RED_SIZE, GL_TEXTURE_GREEN_SIZE, GL_TEXTURE_BLUE_SIZE, GL_TEXTURE_ALPHA_SIZE
            //int width, height, format;
            //glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH, &width);
            //glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT, &height);
            //glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT, &format);

            // NOTE: Each row written to or read from by OpenGL pixel operations like glGetTexImage are aligned to a 4 byte boundary by default, which may add some padding.
            // Use glPixelStorei to modify padding with the GL_[UN]PACK_ALIGNMENT setting.
            // GL_PACK_ALIGNMENT affects operations that read from OpenGL memory (glReadPixels, glGetTexImage, etc.)
            // GL_UNPACK_ALIGNMENT affects operations that write to OpenGL memory (glTexImage, etc.)
            glPixelStorei(GL_PACK_ALIGNMENT, 1);

            rlGetGlTextureFormats(format);
            int size = rlGetPixelDataSize(width, height, format);

            if ((glInternalFormat != -1) && (format < RL_PIXELFORMAT_COMPRESSED_DXT1_RGB)){
                pixels = new byte[size];
                ByteBuffer bb = ByteBuffer.allocateDirect(pixels.length);
                bb.put(pixels).flip();
                glGetTexImage(GL_TEXTURE_2D, 0, glFormat, glType, bb);
                for (int i = 0; i < pixels.length; i++){
                    pixels[i] = bb.get(i);
                }
            }
            else{
                Tracelog(LOG_WARNING, "TEXTURE: [ID " + id + "] Data retrieval not suported for " +
                        "pixel format (" + format + ")");
            }

            glBindTexture(GL_TEXTURE_2D, 0);
        }

        if (GRAPHICS_API_OPENGL_ES2){
            // glGetTexImage() is not available on OpenGL ES 2.0
            // Texture2D width and height are required on OpenGL ES 2.0. There is no way to get it from texture id.
            // Two possible Options:
            // 1 - Bind texture to color fbo attachment and glReadPixels()
            // 2 - Create an fbo, activate it, render quad with texture, glReadPixels()
            // We are using Option 1, just need to care for texture format on retrieval
            // NOTE: This behaviour could be conditioned by graphic driver...
            int fboId = rlLoadFramebuffer(width, width);

            // TODO: Create depth texture/renderbuffer for fbo?

            glBindFramebuffer(GL_FRAMEBUFFER, fboId);
            glBindTexture(GL_TEXTURE_2D, 0);

            // Attach our texture to FBO
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, id, 0);

            // We read data as RGBA because FBO texture is configured as RGBA, despite binding another texture format
            pixels = new byte[rlGetPixelDataSize(width, width,
                                                 RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8)];
            ByteBuffer bb = ByteBuffer.allocateDirect(pixels.length);
            bb.put(pixels).flip();
            GL11.glReadPixels(0, 0, width, width, GL_RGBA, GL_UNSIGNED_BYTE, bb);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            // Clean up temporal fbo
            rlUnloadFramebuffer(fboId);
        }

        return pixels;
    }

    public static short[] rlReadScreenPixels(int width, int height){
        short[] screenData = new short[width * height * 4];

        // NOTE 1: glReadPixels returns image flipped vertically -> (0,0) is the bottom left corner of the framebuffer
        // NOTE 2: We are getting alpha channel! Be careful, it can be transparent if not cleared properly!
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, screenData);

        // Flip image vertically!
        short[] imgData = new short[width * height * 4];

        for (int y = height - 1; y >= 0; y--){
            for (int x = 0; x < (width * 4); x++){
                imgData[((height - 1) - y) * width * 4 + x] = (byte) screenData[(y * width * 4) + x];  // Flip line

                // Set alpha component value to 255 (no trasparent image retrieval)
                // NOTE: Alpha value has already been applied to RGB in framebuffer, we don't need it!
                if (((x + 1) % 4) == 0){
                    imgData[((height - 1) - y) * width * 4 + x] = (byte) 255;
                }
            }
        }

        screenData = null;

        return imgData;     // NOTE: image data should be freed
    }

    // Framebuffer management (fbo)
    //-----------------------------------------------------------------------------------------
    // Load a framebuffer to be used for rendering
    // NOTE: No textures attached

    // Load a framebuffer to be used for rendering
    // NOTE: No textures attached
    public static int rlLoadFramebuffer(int width, int height){
        int fboId = 0;

        if ((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && RLGL_RENDER_TEXTURES_HINT){
            fboId = glGenFramebuffers();       // Create the framebuffer object
            glBindFramebuffer(GL_FRAMEBUFFER, 0);   // Unbind any framebuffer
        }

        return fboId;
    }

    // Attach color buffer texture to an fbo (unloads previous attachment)
    // NOTE: Attach type: 0-Color, 1-Depth renderbuffer, 2-Depth texture
    public static void rlFramebufferAttach(int fboId, int texId, int attachType, int texType){
        if ((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && RLGL_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, fboId);

            switch (attachType){
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL0:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL1:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL2:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL3:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL4:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL5:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL6:
                case rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL7:{
                    if (texType == RL_ATTACHMENT_TEXTURE2D){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType, GL_TEXTURE_2D, texId, 0);
                    }
                    else if (texType == RL_ATTACHMENT_RENDERBUFFER){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType, GL_RENDERBUFFER, texId);
                    }
                    else if (texType >= RL_ATTACHMENT_CUBEMAP_POSITIVE_X){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType, GL_TEXTURE_CUBE_MAP_POSITIVE_X + texType, texId, 0);
                    }

                }
                break;
                case rlFramebufferAttachType.RL_ATTACHMENT_DEPTH:{
                    if (texType == RL_ATTACHMENT_TEXTURE2D){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texId, 0);
                    }
                    else if (texType == RL_ATTACHMENT_RENDERBUFFER){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, texId);
                    }

                }
                break;
                case rlFramebufferAttachType.RL_ATTACHMENT_STENCIL:{
                    if (texType == RL_ATTACHMENT_TEXTURE2D){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_TEXTURE_2D, texId, 0);
                    }
                    else if (texType == RL_ATTACHMENT_RENDERBUFFER){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, texId);
                    }

                }
                break;
                default:
                    break;
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    // Verify render texture is complete
    public static boolean rlFramebufferComplete(int id){
        boolean result = false;

        if ((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && RLGL_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, id);

            int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

            if (status != GL_FRAMEBUFFER_COMPLETE){
                switch (status){
                    case GL_FRAMEBUFFER_UNSUPPORTED:
                        Tracelog(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer is unsupported");
                        break;
                    case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                        Tracelog(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has incomplete attachment");
                        break;

                    case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                        if (GRAPHICS_API_OPENGL_ES2){
                            Tracelog(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has incomplete dimensions");
                        }
                        break;
                    case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                        Tracelog(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has a missing attachment");
                        break;
                    default:
                        break;
                }
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            result = (status == GL_FRAMEBUFFER_COMPLETE);
        }

        return result;
    }

    // Unload framebuffer from GPU memory
    // NOTE: All attached textures/cubemaps/renderbuffers are also deleted
    public static void rlUnloadFramebuffer(int id){
        if ((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && RLGL_RENDER_TEXTURES_HINT){

            // Query depth attachment to automatically delete texture/renderbuffer
            int depthType, depthId;
            glBindFramebuffer(GL_FRAMEBUFFER, id);   // Bind framebuffer to query depth texture type
            depthType = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            depthId = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);

            int depthIdU = depthId;
            if (depthType == GL_RENDERBUFFER){
                glDeleteRenderbuffers(depthIdU);
                glDeleteTextures(depthIdU);
            }

            // NOTE: If a texture object is deleted while its image is attached to the *currently bound* framebuffer,
            // the texture image is automatically detached from the currently bound framebuffer.

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(id);

            Tracelog(LOG_INFO, "FBO: [ID " + id + "] Unloaded framebuffer from VRAM (GPU)");
        }
    }

    // Vertex data management
    //-----------------------------------------------------------------------------------------

    // Load a new attributes buffer
    public static int rlLoadVertexBuffer(float[] buffer, boolean dynamic){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferData(GL_ARRAY_BUFFER, buffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    public static int rlLoadVertexBuffer(byte[] buffer, boolean dynamic){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            ByteBuffer bb = ByteBuffer.allocateDirect(buffer.length);
            bb.put(buffer).flip();

            id = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferData(GL_ARRAY_BUFFER, bb, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    // Load a new attributes element buffer
    public static int rlLoadVertexBufferElement(float[] buffer, boolean dynamic){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    // Load a new attributes element buffer
    public static int rlLoadVertexBufferElement(short[] buffer, boolean dynamic){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    // Enable vertex buffer (VBO)
    public static void rlEnableVertexBuffer(int id) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ARRAY_BUFFER, id);
        }
    }

    // Disable vertex buffer (VBO)
    public static void rlDisableVertexBuffer() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    // Enable vertex buffer element (VBO Element)
    public static void rlEnableVertexBufferElement(int id) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
        }
    }

    // Disable vertex buffer element (VBO Element)
    public static void rlDisableVertexBufferElement() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    // Update vertex buffer with new data
    // NOTE: dataSize and offset must be provided in bytes
    public static void rlUpdateVertexBuffer(int id, byte[] data, int offset) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
            dataBuffer.put(data).flip();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferSubData(GL_ARRAY_BUFFER, offset, dataBuffer);
        }
    }

    // Update vertex buffer with new data
    // NOTE: dataSize and offset must be provided in bytes
    public static void rlUpdateVertexBuffer(int id, float[] data, int offset) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length*Float.BYTES).order(ByteOrder.nativeOrder());
            for (float datum : data) {
                dataBuffer.putFloat(datum);
            }
            dataBuffer.flip();

            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferSubData(GL_ARRAY_BUFFER, offset, dataBuffer);
        }
    }

    // Update vertex buffer elements with new data
    // NOTE: dataSize and offset must be provided in bytes
    public void rlUpdateVertexBufferElements(int id, byte[] data, int offset) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
            dataBuffer.put(data).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, dataBuffer);
        }
    }

    // Enable vertex array object (VAO)
    public static boolean rlEnableVertexArray(int vaoId) {
        boolean result = false;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getExtSupported().isVao()){
                glBindVertexArray(vaoId);
                result = true;
            }
        }
        return result;
    }

    // Disable vertex array object (VAO)
    public static void rlDisableVertexArray() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getExtSupported().isVao()){
                glBindVertexArray(0);
            }
        }
    }

    // Enable vertex attribute index
    public static void rlEnableVertexAttribute(int index) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glEnableVertexAttribArray(index);
        }
    }

    // Disable vertex attribute index
    public static void rlDisableVertexAttribute(int index) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDisableVertexAttribArray(index);
        }
    }

    // Draw vertex array
    public static void rlDrawVertexArray(int offset, int count) {
        glDrawArrays(GL_TRIANGLES, offset, count);
    }

    // Draw vetex array elements
    public static void rlDrawVertexArrayElements(int offset, int count, byte[] buffer) {
        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, buffer.length + offset);
    }

    // Draw vetex array elements
    public static void rlDrawVertexArrayElements(int offset, int count, float[] buffer) {
        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, buffer.length + offset);
    }

    // Draw vertex array instanced
    public static void rlDrawVertexArrayInstanced(int offset, int count, int instances) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDrawArraysInstanced(GL_TRIANGLES, 0, count, instances);
        }
    }

    // Draw vertex array elements instanced
    public static void rlDrawVertexArrayElementsInstanced(int offset, int count, int[] buffer, int instances) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDrawElementsInstanced(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, buffer.length + offset, instances);
        }
    }

    // Enable vertex state pointer
    public static void rlEnableStatePointer(int vertexAttribType, byte[] buffer){
        if (GRAPHICS_API_OPENGL_11){
            if (buffer != null) glEnableClientState(vertexAttribType);
            switch (vertexAttribType){
                case GL_VERTEX_ARRAY:
                    glVertexPointer(3, GL_FLOAT, 0, buffer.length);
                    break;
                case GL_TEXTURE_COORD_ARRAY:
                    glTexCoordPointer(2, GL_FLOAT, 0, buffer.length);
                    break;
                case GL_NORMAL_ARRAY:
                    if (buffer != null) glNormalPointer(GL_FLOAT, 0, buffer.length);
                    break;
                case GL_COLOR_ARRAY:
                    if (buffer != null) glColorPointer(4, GL_UNSIGNED_BYTE, 0, buffer.length);
                    break;
                //case GL_INDEX_ARRAY: if (buffer != NULL) glIndexPointer(GL_SHORT, 0, buffer); break; // Indexed colors
                default:
                    break;
            }
        }
    }

    // Enable vertex state pointer
    public static void rlEnableStatePointer(int vertexAttribType, float[] buffer){
        if (GRAPHICS_API_OPENGL_11){
            if (buffer != null) glEnableClientState(vertexAttribType);
            switch (vertexAttribType){
                case GL_VERTEX_ARRAY:
                    glVertexPointer(3, GL_FLOAT, 0, buffer.length);
                    break;
                case GL_TEXTURE_COORD_ARRAY:
                    glTexCoordPointer(2, GL_FLOAT, 0, buffer.length);
                    break;
                case GL_NORMAL_ARRAY:
                    if (buffer != null) glNormalPointer(GL_FLOAT, 0, buffer.length);
                    break;
                case GL_COLOR_ARRAY:
                    if (buffer != null) glColorPointer(4, GL_UNSIGNED_BYTE, 0, buffer.length);
                    break;
                //case GL_INDEX_ARRAY: if (buffer != NULL) glIndexPointer(GL_SHORT, 0, buffer); break; // Indexed colors
                default:
                    break;
            }
        }
    }

    // Disable vertex state pointer
    public static void rlDisableStatePointer(int vertexAttribType){
        if(GRAPHICS_API_OPENGL_11){
            glDisableClientState(vertexAttribType);
        }
    }

    // Load vertex array object (VAO)
    public static int rlLoadVertexArray() {
        int vaoId = 0;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            vaoId = glGenVertexArrays();
        }
        return vaoId;
    }

    // Set vertex attribute
    public static void rlSetVertexAttribute(int index, int compSize, int type, boolean normalized, int stride, long pointer) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glVertexAttribPointer(index, compSize, type, normalized, stride, pointer);
        }
    }

    // Set vertex attribute divisor
    public static void rlSetVertexAttributeDivisor(int index, int divisor) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glVertexAttribDivisor(index, divisor);
        }
    }

    // Unload vertex array object (VAO)
    public static void rlUnloadVertexArray(int vaoId) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getExtSupported().isVao())
            {
                glBindVertexArray(0);
                glDeleteVertexArrays(vaoId);
                Tracelog(LOG_INFO, "VAO: [ID " + vaoId + "] Unloaded vertex array data from VRAM (GPU)");
            }
        }
    }

    // Unload vertex buffer (VBO)
    public static void rlUnloadVertexBuffer(int vboId) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDeleteBuffers(vboId);
            //TRACELOG(LOG_INFO, "VBO: Unloaded vertex data from VRAM (GPU)");
        }
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Shaders Functions
    // NOTE: Those functions are exposed directly to the user in raylib.h
    //----------------------------------------------------------------------------------

    // Load shader from code strings
    // NOTE: If shader string is NULL, using default vertex/fragment shaders
    public int rlLoadShaderCode(String vsCode, String fsCode){
        int id = 0;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int vertexShaderId = 0;
            int fragmentShaderId = 0;

            // Compile vertex shader (if provided)
            if (vsCode != null) {
                vertexShaderId = rlCompileShader(vsCode, GL_VERTEX_SHADER);
            }

            // In case no vertex shader was provided or compilation failed, we use default vertex shader
            if (vertexShaderId == 0) {
                vertexShaderId = rlglData.getState().defaultVShaderId;
            }

            // Compile fragment shader (if provided)
            if (fsCode != null) {
                fragmentShaderId = rlCompileShader(fsCode, GL_FRAGMENT_SHADER);
            }

            // In case no fragment shader was provided or compilation failed, we use default fragment shader
            if (fragmentShaderId == 0) {
                fragmentShaderId = rlglData.getState().defaultFShaderId;
            }

            // In case vertex and fragment shader are the default ones, no need to recompile, we can just assign the default shader program id
            if ((vertexShaderId == rlglData.getState().defaultVShaderId) && (fragmentShaderId == rlglData.getState().defaultFShaderId)) {
                id = rlglData.getState().defaultShaderId;
            }
            else{
                // One of or both shader are new, we need to compile a new shader program
                id = rlLoadShaderProgram(vertexShaderId, fragmentShaderId);

                // We can detach and delete vertex/fragment shaders (if not default ones)
                // NOTE: We detach shader before deletion to make sure memory is freed
                if (vertexShaderId != rlglData.getState().defaultVShaderId){
                    // Detach shader before deletion to make sure memory is freed
                    glDetachShader(id, vertexShaderId);
                    glDeleteShader(vertexShaderId);
                }
                if (fragmentShaderId != rlglData.getState().defaultFShaderId){
                    // Detach shader before deletion to make sure memory is freed
                    glDetachShader(id, fragmentShaderId);
                    glDeleteShader(fragmentShaderId);
                }
                // In case shader program loading failed, we assign default shader
                if (id == 0){
                    Tracelog(LOG_WARNING, "SHADER: Failed to load custom shader code, using default shader");
                    id = rlglData.getState().defaultShaderId;
                }
            }

            /* Get available shader uniforms
            // NOTE: This information is useful for debug...
            // NOTE: glGetProgramiv() causes the JRE to crash...
            IntBuffer uniformCount = IntBuffer.allocate(64);

            glGetProgramiv(id, GL_ACTIVE_UNIFORMS, uniformCount);

            for (int i = 0; i < uniformCount.capacity(); i++){
                IntBuffer size = IntBuffer.allocate(256);
                IntBuffer type = IntBuffer.allocate(256);

                // Get the name of the uniforms
                glGetActiveUniform(id, i, 256, size, type);
                String name = glGetActiveUniformName(id, i);

                Tracelog(LOG_DEBUG, "SHADER: [ID " + id + "] Active uniform (" + name + ") set at location: " + glGetUniformLocation(id, name));
            }
             */
        }

        return id;
    }

    // Compile custom shader and return shader id
    static int rlCompileShader(String shaderStr, int type){
        int shader = glCreateShader(type);
        glShaderSource(shader, shaderStr);

        glCompileShader(shader);
        int success = glGetShaderi(shader, GL_COMPILE_STATUS);

        if (success == GL_FALSE){
            switch (type) {
                case GL_VERTEX_SHADER:
                    Tracelog(LOG_WARNING, "SHADER: [ID " + shader + "] Failed to compile vertex shader code");
                    break;
                case GL_FRAGMENT_SHADER:
                    Tracelog(LOG_WARNING, "SHADER: [ID " + shader + "] Failed to compile fragment shader code");
                    break;
                //case GL_GEOMETRY_SHADER:
                case GL_COMPUTE_SHADER:
                    Tracelog(LOG_WARNING, "SHADER: [ID " + shader + "] Failed to compile compute shader code");
                    break;
                default:
                    break;
            }

            int maxLength = 0;
            maxLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH);

            if (maxLength > 0){
                String log = "";

                log = glGetShaderInfoLog(shader, maxLength);
                Tracelog(LOG_WARNING, "SHADER: [ID " + shader + "] Compile error: " + log);
            }
        }
        else {
            switch (type) {
                case GL_VERTEX_SHADER:
                    Tracelog(LOG_INFO, "SHADER: [ID " + shader + "] Vertex shader compiled successfully");
                    break;
                case GL_FRAGMENT_SHADER:
                    Tracelog(LOG_INFO, "SHADER: [ID " + shader + "] Fragment shader compiled successfully");
                    break;
                //case GL_GEOMETRY_SHADER:
                case GL_COMPUTE_SHADER:
                    Tracelog(LOG_INFO, "SHADER: [ID " + shader + "] Compute shader compiled successfully");
                break;
                default:
                    break;
            }
        }

        return shader;
    }

    // Load custom shader strings and return program id
    static int rlLoadShaderProgram(int vShaderId, int fShaderId) {
        int program = 0;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int success;
            program = glCreateProgram();

            glAttachShader(program, vShaderId);
            glAttachShader(program, fShaderId);

            // NOTE: Default attribute shader locations must be binded before linking
            glBindAttribLocation(program, 0, RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION);
            glBindAttribLocation(program, 1, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
            glBindAttribLocation(program, 2, RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL);
            glBindAttribLocation(program, 3, RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR);
            glBindAttribLocation(program, 4, RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT);
            glBindAttribLocation(program, 5, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2);

            // NOTE: If some attrib name is not found on the shader, it locations becomes -1

            glLinkProgram(program);

            // NOTE: All uniform variables are intitialised to 0 when a program links

            success = glGetProgrami(program, GL_LINK_STATUS);

            if (success == GL_FALSE){
                Tracelog(LOG_WARNING, "SHADER: [ID " + program + "] Failed to link shader program");

                int maxLength = 0;
                maxLength = glGetProgrami(program, GL_INFO_LOG_LENGTH);

                if (maxLength > 0){
                    String log = glGetProgramInfoLog(program, maxLength);
                    Tracelog(LOG_WARNING, "SHADER: [ID " + program + "] Link error: " + log);
                    log = null;
                }

                glDeleteProgram(program);

                program = 0;
            }
            else{
                // Get the size of compiled shader program (not available on OpenGL ES 2.0)
                // NOTE: If GL_LINK_STATUS is GL_FALSE, program binary length is zero.
                //GLint binarySize = 0;
                //glGetProgramiv(id, GL_PROGRAM_BINARY_LENGTH, &binarySize);

                Tracelog(LOG_INFO, "SHADER: [ID " + program + "] Program shader loaded successfully");
            }
        }
        return program;
    }

    // Unload shader program
    public static void rlUnloadShaderProgram(int id) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDeleteProgram(id);

            Tracelog(LOG_INFO, "SHADER: [ID " + id + "] Unloaded shader program data from VRAM (GPU)");
        }
    }

    // Get shader location uniform
    public static int rlGetLocationUniform(int shaderId, String uniformName) {
        int location = -1;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            location = glGetUniformLocation(shaderId, uniformName);

            if (location == -1)
                Tracelog(LOG_WARNING, "SHADER: [ID " + shaderId + "] Failed to find shader uniform: " + uniformName);
            else
                Tracelog(LOG_INFO, "SHADER: [ID " + shaderId + "] Shader uniform (" + uniformName + ") set at location: " + location);
        }
        return location;
    }

    // Get shader attribute location
    public static int rlGetLocationAttrib(int shaderId, String attribName){
        int location = -1;
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            location = glGetAttribLocation(shaderId, attribName);

            if (location == -1){
                Tracelog(LOG_WARNING,
                                  "SHADER: [ID " + shaderId + "] Failed to find shader attribute: " + attribName);
            }
            else{
                Tracelog(LOG_INFO, "SHADER: [ID " + shaderId + "] Shader attribute (" + attribName + ") set at " +
                        "location: " + location);
            }
        }
        return location;
    }

    // Set shader value uniform
    public static void rlSetUniform(int locIndex, float[] value, int uniformType, int count) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){

            switch (uniformType){
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT:
                    glUniform1f(locIndex, value[0]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2:
                    glUniform2f(locIndex, value[0], value[1]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC3:
                    glUniform3f(locIndex, value[0], value[1], value[2]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC4:
                    glUniform4f(locIndex, value[0], value[1], value[2], value[3]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_INT:
                    glUniform1i(locIndex, (int) value[0]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_IVEC2:
                    glUniform2i(locIndex, (int) value[0], (int) value[1]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_IVEC3:
                    glUniform3i(locIndex, (int) value[0], (int) value[1], (int) value[2]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_IVEC4:
                    glUniform4i(locIndex, (int) value[0], (int) value[1], (int) value[2], (int) value[3]);
                    break;
                case rlShaderUniformDataType.RL_SHADER_UNIFORM_SAMPLER2D:
                    glUniform1i(locIndex, (int) value[0]);
                    break;
                default:
                    Tracelog(LOG_WARNING, "SHADER: Failed to set uniform value, data type not recognized");
            }
        }
    }

    // Set shader value attribute
    public static void rlSetVertexAttributeDefault(int locIndex, float[] value, int attribType, int count) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){

            switch (attribType){
                case rlShaderAttributeDataType.RL_SHADER_ATTRIB_FLOAT:
                    if (count == 1) glVertexAttrib1fv(locIndex, value);
                    break;
                case rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC2:
                    if (count == 2) glVertexAttrib2fv(locIndex, value);
                    break;
                case rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC3:
                    if (count == 3) glVertexAttrib3fv(locIndex, value);
                    break;
                case rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC4:
                    if (count == 4) glVertexAttrib4fv(locIndex, value);
                    break;
                default:
                    Tracelog(LOG_WARNING, "SHADER: Failed to set attrib default value, data type not recognized");
            }
        }
    }

    // Set shader value uniform matrix
    public static void rlSetUniformMatrix(int locIndex, Matrix mat) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUniformMatrix4fv(locIndex, false, MatrixToFloat(mat));
        }
    }

    // Set shader value uniform sampler
    public void rlSetUniformSampler(int locIndex, int textureId) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // Check if texture is already active
            for (int i = 0; i < RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS; i++){
                if (rlglData.getState().getActiveTextureId()[i] == textureId) {
                    return;
                }
            }

            // Register a new active texture for the internal batch system
            // NOTE: Default texture is always activated as GL_TEXTURE0
            for (int i = 0; i < RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS; i++){
                if (rlglData.getState().getActiveTextureId()[i] == 0){
                    glUniform1i(locIndex, 1 + i);              // Activate new texture unit
                    rlglData.getState().getActiveTextureId()[i] = textureId; // Save texture id for binding on drawing
                    break;
                }
            }
        }
    }

    // Set shader currently active (id and locations)
    public void rlSetShader(int id, int[] locs) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getState().currentShaderId != id){
                rlDrawRenderBatch(rlglData.getCurrentBatch());
                rlglData.getState().currentShaderId = id;
                rlglData.getState().currentShaderLocs = locs;
            }
        }
    }

    // Load compute shader program
    public int rlLoadComputeShaderProgram(int shaderId) {
        int program = 0;
        if(GRAPHICS_API_OPENGL_43){
            IntBuffer success = IntBuffer.allocate(1);

            program = glCreateProgram();
            glAttachShader(program, shaderId);
            glLinkProgram(program);

            // NOTE: All uniform variables are intitialised to 0 when a program links

            glGetProgramiv(program, GL_LINK_STATUS, success);

            if (success.get(0) == GL_FALSE){
                Tracelog(LOG_WARNING, "SHADER: [ID " + program + "] Failed to link compute shader program");

                IntBuffer maxLength = IntBuffer.allocate(1);
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, maxLength);

                if (maxLength.get(0) > 0){
                    int length = 0;
                    String log = glGetProgramInfoLog(program, maxLength.get(0));
                    Tracelog(LOG_WARNING, "SHADER: [ID " + program + "] Link error: " + log);
                }

                glDeleteProgram(program);

                program = 0;
            }
            else{
                // Get the size of compiled shader program (not available on OpenGL ES 2.0)
                // NOTE: If GL_LINK_STATUS is GL_FALSE, program binary length is zero.
                //GLint binarySize = 0;
                //glGetProgramiv(id, GL_PROGRAM_BINARY_LENGTH, &binarySize);

                Tracelog(LOG_INFO, "SHADER: [ID " + program + "] Compute shader program loaded successfully");
            }
        }

        return program;
    }

    // Dispatch compute shader (equivalent to *draw* for graphics pilepine)
    public void rlComputeShaderDispatch(int groupX, int groupY, int groupZ) {
        if (GRAPHICS_API_OPENGL_43){
            glDispatchCompute(groupX, groupY, groupZ);
        }
    }

    // Load shader storage buffer object (SSBO)
    public int rlLoadShaderBuffer(long size, byte[] data, int usageHint) {
        int ssbo = 0;

        if (GRAPHICS_API_OPENGL_43){
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
            dataBuffer.put(data).flip();

            ssbo = glGenBuffers();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
            glBufferData(GL_SHADER_STORAGE_BUFFER, dataBuffer, usageHint == 1 ? usageHint : RL_STREAM_COPY);
            glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R8UI, GL_RED_INTEGER, GL_UNSIGNED_BYTE, dataBuffer);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }

        return ssbo;
    }

    // Unload shader storage buffer object (SSBO)
    public void rlUnloadShaderBuffer(int ssboId) {
        if (GRAPHICS_API_OPENGL_43){
            glDeleteBuffers(ssboId);
        }
    }

    // Update SSBO buffer data
    public void rlUpdateShaderBufferElements(int id, byte[] data, long dataSize, long offset) {
        if(GRAPHICS_API_OPENGL_43) {
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
            dataBuffer.put(data).flip();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, offset, dataBuffer);
        }
    }

    // Get SSBO buffer size
    public long rlGetShaderBufferSize(int id) {
        long size = 0;

        if(GRAPHICS_API_OPENGL_43) {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
            size = glGetInteger64(GL_SHADER_STORAGE_BUFFER_SIZE);
        }

        return (size > 0)? size : 0;
    }

    // Read SSBO buffer data
    public void rlReadShaderBufferElements(int id, byte[] dest, long count,long offset) {
        if(GRAPHICS_API_OPENGL_43) {
            ByteBuffer destBuffer = ByteBuffer.allocateDirect(dest.length);
            destBuffer.put(dest).flip();

            glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
            glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, offset, destBuffer);
        }
    }

    // Bind SSBO buffer
    public void rlBindShaderBuffer(int id, int index) {
        if(GRAPHICS_API_OPENGL_43) {
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, index, id);
        }
    }

    // Copy SSBO buffer data
    public void rlCopyBuffersElements(int destId, int srcId, long destOffset, long srcOffset, long count) {
        if(GRAPHICS_API_OPENGL_43) {
            glBindBuffer(GL_COPY_READ_BUFFER, srcId);
            glBindBuffer(GL_COPY_WRITE_BUFFER, destId);
            glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, srcOffset, destOffset, count);
        }
    }

    // Bind image texture
    public void rlBindImageTexture(int id, int index, int format, boolean readonly) {
        if(GRAPHICS_API_OPENGL_43) {
            rlGetGlTextureFormats(format);
            glBindImageTexture(index, id, 0, false, 0, readonly ? GL_READ_ONLY : GL_READ_WRITE, glInternalFormat);
        }
    }

    // Matrix state management
    //-----------------------------------------------------------------------------------------

    // Get internal modelview matrix
    public static Matrix rlGetMatrixModelview() {
        Matrix matrix = MatrixIdentity();
        if(GRAPHICS_API_OPENGL_11){
            FloatBuffer mat = FloatBuffer.allocate(16);
            glGetFloatv(GL_MODELVIEW_MATRIX, mat);
            matrix.m0 = mat.get(0);
            matrix.m1 = mat.get(1);
            matrix.m2 = mat.get(2);
            matrix.m3 = mat.get(3);
            matrix.m4 = mat.get(4);
            matrix.m5 = mat.get(5);
            matrix.m6 = mat.get(6);
            matrix.m7 = mat.get(7);
            matrix.m8 = mat.get(8);
            matrix.m9 = mat.get(9);
            matrix.m10 = mat.get(10);
            matrix.m11 = mat.get(11);
            matrix.m12 = mat.get(12);
            matrix.m13 = mat.get(13);
            matrix.m14 = mat.get(14);
            matrix.m15 = mat.get(15);
        }
        else{
            matrix = rlglData.getState().modelview;
        }
        return matrix;
    }

    // Get internal projection matrix
    public static Matrix rlGetMatrixProjection() {
        if(GRAPHICS_API_OPENGL_11){
            FloatBuffer mat = FloatBuffer.allocate(16);
            glGetFloatv(GL_PROJECTION_MATRIX, mat);
            Matrix m = MatrixIdentity();
            m.m0 = mat.get(0);
            m.m1 = mat.get(1);
            m.m2 = mat.get(2);
            m.m3 = mat.get(3);
            m.m4 = mat.get(4);
            m.m5 = mat.get(5);
            m.m6 = mat.get(6);
            m.m7 = mat.get(7);
            m.m8 = mat.get(8);
            m.m9 = mat.get(9);
            m.m10 = mat.get(10);
            m.m11 = mat.get(11);
            m.m12 = mat.get(12);
            m.m13 = mat.get(13);
            m.m14 = mat.get(14);
            m.m15 = mat.get(15);
            return m;
        }
        else{
            return rlglData.getState().projection;
        }
    }
    // Get internal accumulated transform matrix
    public static Matrix rlGetMatrixTransform() {
        Matrix mat = MatrixIdentity();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // TODO: Consider possible transform matrices in the RLGL.State.stack
            // Is this the right order? or should we start with the first stored matrix instead of the last one?
            //Matrix matStackTransform = MatrixIdentity();
            //for (int i = RLGL.State.stackCounter; i > 0; i--) matStackTransform = MatrixMultiply(RLGL.State.stack[i], matStackTransform);
            mat = rlglData.getState().getTransform();
        }
        return mat;
    }

    // Get internal projection matrix for stereo render (selected eye)
    public static Matrix rlGetMatrixProjectionStereo(int eye) {
        Matrix mat = MatrixIdentity();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            mat = rlglData.getState().getProjectionStereo()[eye];
        }
        return mat;
    }

    // Get internal view offset matrix for stereo render (selected eye)
    public static Matrix rlGetMatrixViewOffsetStereo(int eye) {
        Matrix mat = MatrixIdentity();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            mat = rlglData.getState().getViewOffsetStereo()[eye];
        }
        return mat;
    }

    // Set a custom modelview matrix (replaces internal modelview matrix)
    public static void rlSetMatrixModelview(Matrix view){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setModelview(view);
        }
    }

    // Set a custom projection matrix (replaces internal projection matrix)
    public static void rlSetMatrixProjection(Matrix projection){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setProjection(projection);
        }
    }

    // Set eyes projection matrices for stereo rendering
    public static void rlSetMatrixProjectionStereo(Matrix right, Matrix left){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().getProjectionStereo()[0] = right;
            rlglData.getState().getProjectionStereo()[1] = left;
        }
    }

    // Set eyes view offsets matrices for stereo rendering
    public static void rlSetMatrixViewOffsetStereo(Matrix right, Matrix left){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().getViewOffsetStereo()[0] = right;
            rlglData.getState().getViewOffsetStereo()[1] = left;
        }
    }

    // Renders a quad in NDC
    public static void rlLoadDrawQuad() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int quadVAO = 0;
            int quadVBO = 0;

            float[] vertices = {
                    // Positions         Texcoords
                    -0.5f, 0.5f, 0.0f, -0.5f, 0.5f,
                    -0.5f, -0.5f, 0.0f, -0.5f, -0.5f,
                    0.5f, 0.5f, 0.0f, 0.5f, 1.5f,
                    0.5f, -0.5f, 0.0f, 0.5f, -0.5f,
            };

            // Gen VAO to contain VBO
            quadVAO = glGenVertexArrays();
            glBindVertexArray(quadVAO);

            // Gen and fill vertex buffer (VBO)
            quadVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

            // Bind vertex attributes (position, texcoords)
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0); //Positions
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES); //Texcoords

            // Draw quad
            glBindVertexArray(quadVAO);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            glBindVertexArray(0);

            // Delete buffers (VBO and VAO)
            glDeleteBuffers(quadVBO);
            glDeleteVertexArrays(quadVAO);
        }
    }

    // Renders a cube in NDC
    public static void rlLoadDrawCube(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int cubeVAO = 0;
            int cubeVBO = 0;

            float[] vertices = {
                    // Positions          Normals               Texcoords
                    -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                    -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                    1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                    -1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                    -1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    -1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    -1.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, -1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                    -1.0f, -1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                    -1.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                    -1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f
            };

            FloatBuffer vertFB = MemoryUtil.memAllocFloat(vertices.length);
            vertFB.put(vertices);
            vertFB.flip();

            // Gen VAO to contain VBO
            cubeVAO = glGenVertexArrays();
            glBindVertexArray(cubeVAO);

            // Gen and fill vertex buffer (VBO)
            cubeVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
            glBufferData(GL_ARRAY_BUFFER, vertFB, GL_STATIC_DRAW);

            // Bind vertex attributes (position, normals, texcoords)
            glBindVertexArray(cubeVAO);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 0); //Positions
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES); //Normals
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES); //Texcoords
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

            // Draw cube
            glBindVertexArray(cubeVAO);
            glDrawArrays(GL_TRIANGLES, 0, 36);
            glBindVertexArray(0);

            // Delete VBO and VAO
            glDeleteBuffers(cubeVBO);
            glDeleteVertexArrays(cubeVAO);
        }
    }

    // Get name string for pixel format
    public static String rlGetPixelFormatName(int format){
        switch (format) {
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAYSCALE: return "GRAYSCALE";          // 8 bit per pixel (no alpha)
            case RL_PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA: return "GRAY_ALPHA";        // 8*2 bpp (2 channels)
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G6B5: return "R5G6B5";                // 16 bpp
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8: return "R8G8B8";                // 24 bpp
            case RL_PIXELFORMAT_UNCOMPRESSED_R5G5B5A1: return "R5G5B5A1";            // 16 bpp (1 bit alpha)
            case RL_PIXELFORMAT_UNCOMPRESSED_R4G4B4A4: return "R4G4B4A4";            // 16 bpp (4 bit alpha)
            case RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8: return "R8G8B8A8";            // 32 bpp
            case RL_PIXELFORMAT_UNCOMPRESSED_R32: return "R32";                      // 32 bpp (1 channel - float)
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32: return "R32G32B32";          // 32*3 bpp (3 channels - float)
            case RL_PIXELFORMAT_UNCOMPRESSED_R32G32B32A32: return "R32G32B32A32";    // 32*4 bpp (4 channels - float)
            case RL_PIXELFORMAT_COMPRESSED_DXT1_RGB: return "DXT1_RGB";              // 4 bpp (no alpha)
            case RL_PIXELFORMAT_COMPRESSED_DXT1_RGBA: return "DXT1_RGBA";            // 4 bpp (1 bit alpha)
            case RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA: return "DXT3_RGBA";            // 8 bpp
            case RL_PIXELFORMAT_COMPRESSED_DXT5_RGBA: return "DXT5_RGBA";            // 8 bpp
            case RL_PIXELFORMAT_COMPRESSED_ETC1_RGB: return "ETC1_RGB";              // 4 bpp
            case RL_PIXELFORMAT_COMPRESSED_ETC2_RGB: return "ETC2_RGB";              // 4 bpp
            case RL_PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA: return "ETC2_RGBA";        // 8 bpp
            case RL_PIXELFORMAT_COMPRESSED_PVRT_RGB: return "PVRT_RGB";              // 4 bpp
            case RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA: return "PVRT_RGBA";            // 4 bpp
            case RL_PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA: return "ASTC_4x4_RGBA";    // 8 bpp
            case RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA: return "ASTC_8x8_RGBA";    // 2 bpp
            default: return "UNKNOWN";
        }
    }

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    // Load default shader (just vertex positioning and texture coloring)
    // NOTE: This shader program is used for internal buffers
    // NOTE: Loaded: rlglData.state.defaultShaderId, rlglData.state.defaultShaderLocs
    public static void rlLoadShaderDefault(){

        rlglData.getState().setDefaultShaderLocs(new int[RL_MAX_SHADER_LOCATIONS]);

        // NOTE: All locations must be reseted to -1 (no location)
        for (int i = 0; i < RL_MAX_SHADER_LOCATIONS; i++){
            rlglData.getState().defaultShaderLocs[i] = -1;
        }

        // Vertex shader directly defined, no external file required
        StringBuilder defaultVShaderCode = new StringBuilder();
        if (GRAPHICS_API_OPENGL_21){
            defaultVShaderCode.append("#version 120                       \n");
            defaultVShaderCode.append("attribute vec3 vertexPosition;     \n");
            defaultVShaderCode.append("attribute vec2 vertexTexCoord;     \n");
            defaultVShaderCode.append("attribute vec4 vertexColor;        \n");
            defaultVShaderCode.append("varying vec2 fragTexCoord;         \n");
            defaultVShaderCode.append("varying vec4 fragColor;            \n");
        }
        else if (GRAPHICS_API_OPENGL_33){
            defaultVShaderCode.append("#version 330                       \n");
            defaultVShaderCode.append("in vec3 vertexPosition;            \n");
            defaultVShaderCode.append("in vec2 vertexTexCoord;            \n");
            defaultVShaderCode.append("in vec4 vertexColor;               \n");
            defaultVShaderCode.append("out vec2 fragTexCoord;             \n");
            defaultVShaderCode.append("out vec4 fragColor;                \n");
        }
        if (GRAPHICS_API_OPENGL_ES2){
            defaultVShaderCode.append("#version 100                       \n");
            defaultVShaderCode.append("attribute vec3 vertexPosition;     \n");
            defaultVShaderCode.append("attribute vec2 vertexTexCoord;     \n");
            defaultVShaderCode.append("attribute vec4 vertexColor;        \n");
            defaultVShaderCode.append("varying vec2 fragTexCoord;         \n");
            defaultVShaderCode.append("varying vec4 fragColor;            \n");
        }
        defaultVShaderCode.append("uniform mat4 mvp;                  \n");
        defaultVShaderCode.append("void main()                        \n");
        defaultVShaderCode.append("{                                  \n");
        defaultVShaderCode.append("    fragTexCoord = vertexTexCoord; \n");
        defaultVShaderCode.append("    fragColor = vertexColor;       \n");
        defaultVShaderCode.append("    gl_Position = mvp*vec4(vertexPosition, 1.0); \n");
        defaultVShaderCode.append("}                                  \n");

        // Fragment shader directly defined, no external file required
        StringBuilder defaultFShaderCode = new StringBuilder();
        if (GRAPHICS_API_OPENGL_21){
            defaultFShaderCode.append("#version 120                       \n");
            defaultFShaderCode.append("varying vec2 fragTexCoord;         \n");
            defaultFShaderCode.append("varying vec4 fragColor;            \n");
            defaultFShaderCode.append("uniform sampler2D texture0;        \n");
            defaultFShaderCode.append("uniform vec4 colDiffuse;           \n");
            defaultFShaderCode.append("void main()                        \n");
            defaultFShaderCode.append("{                                  \n");
            defaultFShaderCode.append("    vec4 texelColor = texture2D(texture0, fragTexCoord); \n");
            defaultFShaderCode.append("    gl_FragColor = texelColor*colDiffuse*fragColor;      \n");
            defaultFShaderCode.append("}                                  \n");
        }
        else if (GRAPHICS_API_OPENGL_33){
            defaultFShaderCode.append("#version 330       \n");
            defaultFShaderCode.append("in vec2 fragTexCoord;              \n");
            defaultFShaderCode.append("in vec4 fragColor;                 \n");
            defaultFShaderCode.append("out vec4 finalColor;               \n");
            defaultFShaderCode.append("uniform sampler2D texture0;        \n");
            defaultFShaderCode.append("uniform vec4 colDiffuse;           \n");
            defaultFShaderCode.append("void main()                        \n");
            defaultFShaderCode.append("{                                  \n");
            defaultFShaderCode.append("    vec4 texelColor = texture(texture0, fragTexCoord);   \n");
            defaultFShaderCode.append("    finalColor = texelColor*colDiffuse*fragColor;        \n");
            defaultFShaderCode.append("}                                  \n");
        }
        if (GRAPHICS_API_OPENGL_ES2){
            defaultFShaderCode.append("#version 100                       \n");
            defaultFShaderCode.append("precision mediump float;           \n");     // Precision required for OpenGL ES2 (WebGL)
            defaultFShaderCode.append("varying vec2 fragTexCoord;         \n");
            defaultFShaderCode.append("varying vec4 fragColor;            \n");
            defaultFShaderCode.append("uniform sampler2D texture0;        \n");
            defaultFShaderCode.append("uniform vec4 colDiffuse;           \n");
            defaultFShaderCode.append("void main()                        \n");
            defaultFShaderCode.append("{                                  \n");
            defaultFShaderCode.append("    vec4 texelColor = texture2D(texture0, fragTexCoord); \n");
            defaultFShaderCode.append("    gl_FragColor = texelColor*colDiffuse*fragColor;      \n");
            defaultFShaderCode.append("}                                  \n");
        }

        // NOTE: Compiled vertex/fragment shaders are not deleted,
        // they are kept for re-use as default shaders in case some shader loading fails
        rlglData.getState().setDefaultVShaderId(rlCompileShader(String.valueOf(defaultVShaderCode), GL_VERTEX_SHADER));     // Compile default vertex shader
        rlglData.getState().setDefaultFShaderId(rlCompileShader(String.valueOf(defaultFShaderCode), GL_FRAGMENT_SHADER));   // Compile default fragment shader

        rlglData.getState().defaultShaderId = rlLoadShaderProgram(rlglData.getState().defaultVShaderId, rlglData.getState().defaultFShaderId);

        if (rlglData.getState().getDefaultShaderId() > 0){
            Tracelog(LOG_INFO, "SHADER: [ID " + rlglData.getState().defaultShaderId + "] Default shader loaded successfully");

            // Set default shader locations: attributes locations
            rlglData.getState().defaultShaderLocs[RL_SHADER_LOC_VERTEX_POSITION] = glGetAttribLocation(rlglData.getState().defaultShaderId, "vertexPosition");
            rlglData.getState().defaultShaderLocs[RL_SHADER_LOC_VERTEX_TEXCOORD01] = glGetAttribLocation(rlglData.getState().defaultShaderId, "vertexTexCoord");
            rlglData.getState().defaultShaderLocs[RL_SHADER_LOC_VERTEX_COLOR] = glGetAttribLocation(rlglData.getState().defaultShaderId, "vertexColor");

            // Set default shader locations: uniform locations
            rlglData.getState().defaultShaderLocs[RL_SHADER_LOC_MATRIX_MVP]  = glGetUniformLocation(rlglData.getState().defaultShaderId, "mvp");
            rlglData.getState().defaultShaderLocs[RL_SHADER_LOC_COLOR_DIFFUSE] = glGetUniformLocation(rlglData.getState().defaultShaderId, "colDiffuse");
            rlglData.getState().defaultShaderLocs[RL_SHADER_LOC_MAP_DIFFUSE] = glGetUniformLocation(rlglData.getState().defaultShaderId, "texture0");
        }
        else{
            Tracelog(LOG_WARNING, "SHADER: [ID " + rlglData.getState().getDefaultShaderId() + "] Failed to load default shader");
        }
    }

    // Unload default shader
    // NOTE: Unloads: rlglData.state.defaultShaderId, rlglData.state.defaultShaderLocs
    public static void rlUnloadShaderDefault(){
        glUseProgram(0);

        glDetachShader(rlglData.getState().getDefaultShaderId(), rlglData.getState().getDefaultFShaderId());
        glDetachShader(rlglData.getState().getDefaultShaderId(), rlglData.getState().getDefaultFShaderId());
        glDeleteShader(rlglData.getState().getDefaultFShaderId());
        glDeleteShader(rlglData.getState().getDefaultFShaderId());

        glDeleteProgram(rlglData.getState().getDefaultShaderId());

        rlglData.getState().setDefaultShaderLocs(null);
    }

    // Get compressed format official GL identifier name
    static String rlGetCompressedFormatName(int format){
        if (SUPPORT_GL_DETAILS_INFO){
            switch (format){
                // GL_EXT_texture_compression_s3tc
                case 0x83F0:
                    return "GL_COMPRESSED_RGB_S3TC_DXT1_EXT";
                case 0x83F1:
                    return "GL_COMPRESSED_RGBA_S3TC_DXT1_EXT";
                case 0x83F2:
                    return "GL_COMPRESSED_RGBA_S3TC_DXT3_EXT";
                case 0x83F3:
                    return "GL_COMPRESSED_RGBA_S3TC_DXT5_EXT";

                // GL_3DFX_texture_compression_FXT1
                case 0x86B0:
                    return "GL_COMPRESSED_RGB_FXT1_3DFX";
                case 0x86B1:
                    return "GL_COMPRESSED_RGBA_FXT1_3DFX";

                // GL_IMG_texture_compression_pvrtc
                case 0x8C00:
                    return "GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG";
                case 0x8C01:
                    return "GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG";
                case 0x8C02:
                    return "GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG";
                case 0x8C03:
                    return "GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG";

                // GL_OES_compressed_ETC1_RGB8_texture
                case 0x8D64:
                    return "GL_ETC1_RGB8_OES";

                // GL_ARB_texture_compression_rgtc
                case 0x8DBB:
                    return "GL_COMPRESSED_RED_RGTC1";
                case 0x8DBC:
                    return "GL_COMPRESSED_SIGNED_RED_RGTC1";
                case 0x8DBD:
                    return "GL_COMPRESSED_RG_RGTC2";
                case 0x8DBE:
                    return "GL_COMPRESSED_SIGNED_RG_RGTC2";

                // GL_ARB_texture_compression_bptc
                case 0x8E8C:
                    return "GL_COMPRESSED_RGBA_BPTC_UNORM_ARB";
                case 0x8E8D:
                    return "GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM_ARB";
                case 0x8E8E:
                    return "GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT_ARB";
                case 0x8E8F:
                    return "GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_ARB";

                // GL_ARB_ES3_compatibility
                case 0x9274:
                    return "GL_COMPRESSED_RGB8_ETC2";
                case 0x9275:
                    return "GL_COMPRESSED_SRGB8_ETC2";
                case 0x9276:
                    return "GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2";
                case 0x9277:
                    return "GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2";
                case 0x9278:
                    return "GL_COMPRESSED_RGBA8_ETC2_EAC";
                case 0x9279:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC";
                case 0x9270:
                    return "GL_COMPRESSED_R11_EAC";
                case 0x9271:
                    return "GL_COMPRESSED_SIGNED_R11_EAC";
                case 0x9272:
                    return "GL_COMPRESSED_RG11_EAC";
                case 0x9273:
                    return "GL_COMPRESSED_SIGNED_RG11_EAC";

                // GL_KHR_texture_compression_astc_hdr
                case 0x93B0:
                    return "GL_COMPRESSED_RGBA_ASTC_4x4_KHR";
                case 0x93B1:
                    return "GL_COMPRESSED_RGBA_ASTC_5x4_KHR";
                case 0x93B2:
                    return "GL_COMPRESSED_RGBA_ASTC_5x5_KHR";
                case 0x93B3:
                    return "GL_COMPRESSED_RGBA_ASTC_6x5_KHR";
                case 0x93B4:
                    return "GL_COMPRESSED_RGBA_ASTC_6x6_KHR";
                case 0x93B5:
                    return "GL_COMPRESSED_RGBA_ASTC_8x5_KHR";
                case 0x93B6:
                    return "GL_COMPRESSED_RGBA_ASTC_8x6_KHR";
                case 0x93B7:
                    return "GL_COMPRESSED_RGBA_ASTC_8x8_KHR";
                case 0x93B8:
                    return "GL_COMPRESSED_RGBA_ASTC_10x5_KHR";
                case 0x93B9:
                    return "GL_COMPRESSED_RGBA_ASTC_10x6_KHR";
                case 0x93BA:
                    return "GL_COMPRESSED_RGBA_ASTC_10x8_KHR";
                case 0x93BB:
                    return "GL_COMPRESSED_RGBA_ASTC_10x10_KHR";
                case 0x93BC:
                    return "GL_COMPRESSED_RGBA_ASTC_12x10_KHR";
                case 0x93BD:
                    return "GL_COMPRESSED_RGBA_ASTC_12x12_KHR";
                case 0x93D0:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR";
                case 0x93D1:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR";
                case 0x93D2:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR";
                case 0x93D3:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR";
                case 0x93D4:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR";
                case 0x93D5:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR";
                case 0x93D6:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR";
                case 0x93D7:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR";
                case 0x93D8:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR";
                case 0x93D9:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR";
                case 0x93DA:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR";
                case 0x93DB:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR";
                case 0x93DC:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR";
                case 0x93DD:
                    return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR";

                default:
                    return "GL_COMPRESSED_UNKNOWN";
            }
        }

        return "GL_COMPRESSED_UNKNOWN";
    }

    // Mipmaps data is generated after image data
    // NOTE: Only works with RGBA (4 bytes) data!
    static int rlGenTextureMipmapsData(byte[] data, int baseWidth, int baseHeight){
        int mipmapCount = 1;                // Required mipmap levels count (including base level)
        if(GRAPHICS_API_OPENGL_11){
        int width = baseWidth;
        int height = baseHeight;
        int size = baseWidth * baseHeight * 4;  // Size in bytes (will include mipmaps...), RGBA only

        // Count mipmap levels required
        while ((width != 1) && (height != 1)){
            width /= 2;
            height /= 2;

            Tracelog.Tracelog("TEXTURE: Next mipmap size: " + width + " x " + height);

            mipmapCount++;

            size += (width * height * 4);       // Add mipmap size (in bytes)
        }

        Tracelog.Tracelog("TEXTURE: Total mipmaps required: " + mipmapCount);
        Tracelog.Tracelog("TEXTURE: Total size of data required: " + size);

        byte[] temp = new byte[data.length];

        if (temp != null){
            data = temp;
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: Failed to re-allocate required mipmaps memory");
        }

        width = baseWidth;
        height = baseHeight;
        size = (width * height * 4);    // RGBA: 4 bytes

        // Generate mipmaps
        // NOTE: Every mipmap data is stored after data
        byte[] image = new byte[width * height];
        byte[] mipmap;
        int offset = 0;

        for (int i = 0; i < size; i += 4){
            image[i]     = data[i];
            image[i + 1] = data[i + 1];
            image[i + 2] = data[i + 2];
            image[i + 3] = data[i + 3];

        }

        Tracelog.Tracelog("TEXTURE: Mipmap base size (" + width + "x" + height + ")");

        for (int mip = 1; mip < mipmapCount; mip++){
            mipmap = rlGenNextMipmapData(image, width, height);

            offset += (width * height * 4); // Size of last mipmap

            width /= 2;
            height /= 2;
            size = (width * height * 4);    // Mipmap size to store after offset

            // Add mipmap to data
            for (int i = 0; i < size; i += 4){
                data[offset + i]     = mipmap[i];
                data[offset + i + 1] = mipmap[i + 1];
                data[offset + i + 2] = mipmap[i + 1];
                data[offset + i + 3] = mipmap[i + 1];
            }

            image = null;
            mipmap = null;
        }

        mipmap = null;       // free mipmap data

    }
        return mipmapCount;
    }

    // Manual mipmap generation (basic scaling algorithm)
    static byte[] rlGenNextMipmapData(byte[] srcData, int srcWidth, int srcHeight) {
        int x2, y2;
        byte[] prow = new byte[4];
        byte[] pcol = new byte[4];

        int width = srcWidth/2;
        int height = srcHeight/2;

         byte[] mipmap = new byte[width*height*4];

        // Scaling algorithm works perfectly (box-filter)
        for (int y = 0; y < height; y++) {
            y2 = 2*y;

            for (int x = 0; x < width; x++) {
                x2 = 2*x;

                prow[0] = (byte) ((srcData[(y2*srcWidth + x2)*4 + 0] + srcData[(y2*srcWidth + x2 + 1)*4 + 0])/2);
                prow[1] = (byte) ((srcData[(y2*srcWidth + x2)*4 + 1] + srcData[(y2*srcWidth + x2 + 1)*4 + 1])/2);
                prow[2] = (byte) ((srcData[(y2*srcWidth + x2)*4 + 2] + srcData[(y2*srcWidth + x2 + 1)*4 + 2])/2);
                prow[3] = (byte) ((srcData[(y2*srcWidth + x2)*4 + 3] + srcData[(y2*srcWidth + x2 + 1)*4 + 3])/2);

                pcol[0] = (byte) ((srcData[((y2 + 1)*srcWidth + x2)*4 + 0] + srcData[((y2 + 1)*srcWidth + x2 + 1)*4 + 0])/2);
                pcol[1] = (byte) ((srcData[((y2 + 1)*srcWidth + x2)*4 + 1] + srcData[((y2 + 1)*srcWidth + x2 + 1)*4 + 1])/2);
                pcol[2] = (byte) ((srcData[((y2 + 1)*srcWidth + x2)*4 + 2] + srcData[((y2 + 1)*srcWidth + x2 + 1)*4 + 2])/2);
                pcol[3] = (byte) ((srcData[((y2 + 1)*srcWidth + x2)*4 + 3] + srcData[((y2 + 1)*srcWidth + x2 + 1)*4 + 3])/2);

                mipmap[(y*width + x)*4 + 0] = (byte) ((prow[0] + pcol[0])/2);
                mipmap[(y*width + x)*4 + 1] = (byte) ((prow[1] + pcol[1])/2);
                mipmap[(y*width + x)*4 + 2] = (byte) ((prow[2] + pcol[2])/2);
                mipmap[(y*width + x)*4 + 3] = (byte) ((prow[3] + pcol[3])/2);
            }
        }

        Tracelog.Tracelog("TEXTURE: Mipmap generated successfully (" + width + "x" + height + ")");

        return mipmap;
    }

    public static int rlGetPixelDataSize(int width, int height, int format){
        int dataSize = 0;       // Size in bytes
        int bpp = 0;            // Bits per pixel

        switch (format){
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
        if ((width < 4) && (height < 4)){
            if ((format >= RL_PIXELFORMAT_COMPRESSED_DXT1_RGB) && (format < RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA)){
                dataSize = 8;
            }
            else if ((format >= RL_PIXELFORMAT_COMPRESSED_DXT3_RGBA) && (format < RL_PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA)){
                dataSize = 16;
            }
        }

        return dataSize;
    }
}