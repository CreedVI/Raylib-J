package com.raylib.java.rlgl;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Matrix;
import com.raylib.java.rlgl.data.rlglData;
import com.raylib.java.structs.Texture2D;
import org.lwjgl.system.MemoryUtil;

import java.nio.*;
import java.util.Arrays;

import static com.raylib.java.Config.*;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.*;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlBlendMode.*;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.*;
import static com.raylib.java.rlgl.RLGL.rlGlVersion.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_SWIZZLE_RGBA;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengles.EXTDrawBuffers.glDrawBuffersEXT;
import static org.lwjgl.opengles.GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
import static org.lwjgl.opengles.OESDepth24.GL_DEPTH_COMPONENT24_OES;
import static org.lwjgl.opengles.OESDepth32.GL_DEPTH_COMPONENT32_OES;

public class RLGL {

    /**********************************************************************************************
     *
     *   rlgl v6.0 - A multi-OpenGL abstraction layer with an immediate-mode style API
     *
     *   DESCRIPTION:
     *       An abstraction layer for multiple OpenGL versions (1.1, 2.1, 3.3 Core, 4.3 Core, ES 2.0, ES 3.0)
     *       that provides a pseudo-OpenGL 1.1 immediate-mode style API (rlVertex, rlTranslate, rlRotate...)
     *
     *   ADDITIONAL NOTES:
     *       When choosing an OpenGL backend different than OpenGL 1.1, some internal buffers are
     *       initialized on rlglInit() to accumulate vertex data
     *
     *       When an internal state change is required all the stored vertex data is rendered in a batch,
     *       additionally, rlDrawRenderBatchActive() could be called to force flushing of the batch
     *
     *       Some resources are also loaded for convenience, here the complete list:
     *          - Default batch (RLGL.defaultBatch): RenderBatch system to accumulate vertex data
     *          - Default texture (RLGL.defaultTextureId): 1x1 white pixel R8G8B8A8
     *          - Default shader (RLGL.State.defaultShaderId, RLGL.State.defaultShaderLocs)
     *
     *       Internal buffer (and resources) must be manually unloaded calling rlglClose()
     *
     *   CONFIGURATION:
     *       #define GRAPHICS_API_OPENGL_SOFTWARE
     *       #define GRAPHICS_API_OPENGL_11
     *       #define GRAPHICS_API_OPENGL_21
     *       #define GRAPHICS_API_OPENGL_33
     *       #define GRAPHICS_API_OPENGL_43
     *       #define GRAPHICS_API_OPENGL_ES2
     *       #define GRAPHICS_API_OPENGL_ES3
     *           Use selected OpenGL graphics backend, should be supported by platform
     *           Those preprocessor defines are only used on the rlgl module, if OpenGL version is
     *           required by any other module, use rlGetVersion() to check it
     *
     *       #define RLGL_IMPLEMENTATION
     *           Generates the implementation of the library into the included file
     *           If not defined, the library is in header only mode and can be included in other headers
     *           or source files without problems. But only ONE file should hold the implementation
     *
     *       #if RLGL_SHOW_GL_DETAILS_INFO
     *           Show OpenGL extensions and capabilities detailed logs on init
     *
     *       #if RLGL_ENABLE_OPENGL_DEBUG_CONTEXT
     *           Enable debug context (only available on OpenGL 4.3)
     *
     *       rlgl capabilities could be customized defining some internal
     *       values before library inclusion (default values listed):
     *
     *       #define RL_DEFAULT_BATCH_BUFFER_ELEMENTS   8192    // Default internal render batch elements limits
     *       #define RL_DEFAULT_BATCH_BUFFERS              1    // Default number of batch buffers (multi-buffering)
     *       #define RL_DEFAULT_BATCH_DRAWCALLS          256    // Default number of batch draw calls (by state changes: mode, texture)
     *       #define RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS    4    // Maximum number of texture units that can be activated on batch drawing (SetShaderValueTexture())
     *
     *       #define RL_MAX_MATRIX_STACK_SIZE             32    // Maximum size of internal Matrix stack
     *       #define RL_MAX_SHADER_LOCATIONS              32    // Maximum number of shader locations supported
     *       #define RL_CULL_DISTANCE_NEAR              0.05    // Default projection matrix near cull distance
     *       #define RL_CULL_DISTANCE_FAR             4000.0    // Default projection matrix far cull distance
     *
     *       When loading a shader, the following vertex attributes and uniform location names are tried to be set automatically:
     *       WARNING: Pre-defined names can not be changed, they are used by default shaders and all raylib examples shaders, they are just listed here for reference
     *
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION     "vertexPosition"    // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD     "vertexTexCoord"    // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL       "vertexNormal"      // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR        "vertexColor"       // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT      "vertexTangent"     // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2    "vertexTexCoord2"   // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_BONEINDICES  "vertexBoneIndices" // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES
     *       #define RL_DEFAULT_SHADER_ATTRIB_NAME_BONEWEIGHTS  "vertexBoneWeights" // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS
     *
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_MVP         "mvp"               // model-view-projection matrix
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_VIEW        "matView"           // view matrix
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_PROJECTION  "matProjection"     // projection matrix
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_MODEL       "matModel"          // model matrix
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_NORMAL      "matNormal"         // normal matrix (transpose(inverse(matModelView)))
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_COLOR       "colDiffuse"        // color diffuse (base tint color, multiplied by texture color)
     *       #define RL_DEFAULT_SHADER_UNIFORM_NAME_BONEMATRICES "boneMatrices"     // bone matrices
     *
     *       #define RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE0  "texture0"          // texture0 (texture slot active 0)
     *       #define RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE1  "texture1"          // texture1 (texture slot active 1)
     *       #define RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE2  "texture2"          // texture2 (texture slot active 2)
     *
     *   DEPENDENCIES:
     *      - OpenGL libraries (depending on platform and OpenGL version selected)
     *      - GLAD OpenGL extensions loading library (only for OpenGL 3.3 Core, 4.3 Core)
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2014-2026 Ramon Santamaria (@raysan5)
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

    //*********************
    //GL API VERSION
    //*********************
    public static boolean GRAPHICS_API_OPENGL_11 = false;
    private static boolean GRAPHICS_API_OPENGL_21 = false;
    public static boolean GRAPHICS_API_OPENGL_33 = true;
    public static boolean GRAPHICS_API_OPENGL_43 = false;
    public static boolean GRAPHICS_API_OPENGL_ES2 = false;
    public static boolean GRAPHICS_API_OPENGL_ES3 = false;
    public static boolean GRAPHICS_API_OPENGL_SOFTWARE = false;
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
    static final float RL_CULL_DISTANCE_NEAR = 0.05f;
    // Default near cull distance

    static final float RL_CULL_DISTANCE_FAR = 4000.0f;
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
    public static final int RL_TEXTURE_MIPMAP_BIAS_RATIO = 0x4000; // Texture mipmap bias, percentage ratio (custom identifier)

    public static final int RL_TEXTURE_WRAP_REPEAT = 0x2901;      // GL_REPEAT
    public static final int RL_TEXTURE_WRAP_CLAMP = 0x812F;      // GL_CLAMP_TO_EDGE
    public static final int RL_TEXTURE_WRAP_MIRROR_REPEAT = 0x8370;      // GL_MIRRORED_REPEAT
    public static final int RL_TEXTURE_WRAP_MIRROR_CLAMP = 0x8742;      // GL_MIRROR_CLAMP_EXT

    // Matrix modes (equivalent to OpenGL)=
    public static final int RL_MODELVIEW = 0x1700;      // GL_MODELVIEW
    public static final int RL_PROJECTION = 0x1701;      // GL_PROJECTION
    public static final int RL_TEXTURE = 0x1702;      // GL_TEXTURE

    public static final int RL_TRANSFORM = 0x1703;

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

    // GL blending factors
    public static final int RL_ZERO = 0;           // GL_ZERO
    public static final int RL_ONE = 1;           // GL_ONE
    public static final int RL_SRC_COLOR = 0x0300;      // GL_SRC_COLOR
    public static final int RL_ONE_MINUS_SRC_COLOR = 0x0301;      // GL_ONE_MINUS_SRC_COLOR
    public static final int RL_SRC_ALPHA = 0x0302;      // GL_SRC_ALPHA
    public static final int RL_ONE_MINUS_SRC_ALPHA = 0x0303;      // GL_ONE_MINUS_SRC_ALPHA
    public static final int RL_DST_ALPHA = 0x0304;      // GL_DST_ALPHA
    public static final int RL_ONE_MINUS_DST_ALPHA = 0x0305;      // GL_ONE_MINUS_DST_ALPHA
    public static final int RL_DST_COLOR = 0x0306;      // GL_DST_COLOR
    public static final int RL_ONE_MINUS_DST_COLOR = 0x0307;      // GL_ONE_MINUS_DST_COLOR
    public static final int RL_SRC_ALPHA_SATURATE = 0x0308;      // GL_SRC_ALPHA_SATURATE
    public static final int RL_CONSTANT_COLOR = 0x8001;      // GL_CONSTANT_COLOR
    public static final int RL_ONE_MINUS_CONSTANT_COLOR = 0x8002;      // GL_ONE_MINUS_CONSTANT_COLOR
    public static final int RL_CONSTANT_ALPHA = 0x8003;      // GL_CONSTANT_ALPHA
    public static final int RL_ONE_MINUS_CONSTANT_ALPHA = 0x8004;      // GL_ONE_MINUS_CONSTANT_ALPHA

    // GL blending functions/equations
    public static final int RL_FUNC_ADD = 0x8006;      // GL_FUNC_ADD
    public static final int RL_MIN = 0x8007;      // GL_MIN
    public static final int RL_MAX = 0x8008;      // GL_MAX
    public static final int RL_FUNC_SUBTRACT = 0x800A;      // GL_FUNC_SUBTRACT
    public static final int RL_FUNC_REVERSE_SUBTRACT = 0x800B;      // GL_FUNC_REVERSE_SUBTRACT
    public static final int RL_BLEND_EQUATION = 0x8009;      // GL_BLEND_EQUATION
    public static final int RL_BLEND_EQUATION_RGB = 0x8009;      // GL_BLEND_EQUATION_RGB   // (Same as BLEND_EQUATION)
    public static final int RL_BLEND_EQUATION_ALPHA = 0x883D;      // GL_BLEND_EQUATION_ALPHA
    public static final int RL_BLEND_DST_RGB = 0x80C8;      // GL_BLEND_DST_RGB
    public static final int RL_BLEND_SRC_RGB = 0x80C9;      // GL_BLEND_SRC_RGB
    public static final int RL_BLEND_DST_ALPHA = 0x80CA;      // GL_BLEND_DST_ALPHA
    public static final int RL_BLEND_SRC_ALPHA = 0x80CB;      // GL_BLEND_SRC_ALPHA
    public static final int RL_BLEND_COLOR = 0x8005;      // GL_BLEND_COLOR

    public static final int RL_READ_FRAMEBUFFER = 0x8CA8;      // GL_READ_FRAMEBUFFER
    public static final int RL_DRAW_FRAMEBUFFER = 0x8CA9;      // GL_DRAW_FRAMEBUFFER

    // Default shader vertex attribute locations
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION = 0;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD = 1;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL = 2;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR = 3;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT = 4;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2 = 5;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_INDICES = 6;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES = 7;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS = 8;
    public static final int RL_DEFAULT_SHADER_ATTRIB_LOCATION_INSTANCETRANSFORM = 9;

    // Default shader vertex attribute names to set location points
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION = "vertexPosition";    // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD = "vertexTexCoord";    // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL = "vertexNormal";      // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR = "vertexColor";       // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT = "vertexTangent";     // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2 = "vertexTexCoord2";   // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_BONEINDICES = "vertexBoneIndices"; // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_BONEWEIGHTS = "vertexBoneWeights"; // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_INSTANCETRANSFORM = "instanceTransform"; // Bound by default to shader location: RL_DEFAULT_SHADER_ATTRIB_LOCATION_INSTANCETRANSFORM

    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_MVP = "mvp";               // model-view-projection matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_VIEW = "matView";           // view matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_PROJECTION = "matProjection";     // projection matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_MODEL = "matModel";          // model matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_NORMAL = "matNormal";         // normal matrix (transpose(inverse(matModelView))
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_COLOR = "colDiffuse";        // color diffuse (base tint color, multiplied by texture color)
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_BONEMATRICES = "boneMatrices";      // bone matrices (required for GPU skinning)

    public static final String RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE0 = "texture0";          // texture0 (texture slot active 0)
    public static final String RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE1 = "texture1";          // texture1 (texture slot active 1)
    public static final String RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE2 = "texture2";          // texture2 (texture slot active 2)
            
    
    private static int glInternalFormat = 0, glFormat = 0, glType = 0;

    protected static rlglData rlglData;
    private static double rlCullDistanceNear = RL_CULL_DISTANCE_NEAR;
    private static double rlCullDistanceFar = RL_CULL_DISTANCE_FAR;

    /**
     * Version of OpenGL being used by Raylib-J
     */
    public enum rlGlVersion {
        OPENGL_SOFTWARE(0), // Software rendering
        OPENGL_11(1),       // OpenGL 1.1
        OPENGL_21(2),       // OpenGL 2.1 (GLSL 120)
        OPENGL_33(3),       // OpenGL 3.3 (GLSL 330)
        OPENGL_43(4),       // OpenGL 4.3 (using GLSL 330)
        OPENGL_ES_20(5),    // OpenGL ES 2.0 (GLSL 120)
        OPENGL_ES_30(6);    // OpenGL ES 3.0 (GLSL 300 es)
        
        private final int version;
        
        rlGlVersion(int value) {
            this.version = value;
        }

        public int GetVersion() {
            return version;
        }
    }

    /**
     * Texture formats (support depends on OpenGL version)
     */
    public enum rlPixelFormat {
        PIXELFORMAT_UNCOMPRESSED_GRAYSCALE(1),              // 8 bit per pixel (no alpha)
        PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA(2),             // 8*2 bpp (2 channels)
        PIXELFORMAT_UNCOMPRESSED_R5G6B5(3),                 // 16 bpp
        PIXELFORMAT_UNCOMPRESSED_R8G8B8(4),                 // 24 bpp
        PIXELFORMAT_UNCOMPRESSED_R5G5B5A1(5),               // 16 bpp (1 bit alpha)
        PIXELFORMAT_UNCOMPRESSED_R4G4B4A4(6),               // 16 bpp (4 bit alpha)
        PIXELFORMAT_UNCOMPRESSED_R8G8B8A8(7),               // 32 bpp
        PIXELFORMAT_UNCOMPRESSED_R32(8),                    // 32 bpp (1 channel - float)
        PIXELFORMAT_UNCOMPRESSED_R32G32B32(9),              // 32*3 bpp (3 channels - float)
        PIXELFORMAT_UNCOMPRESSED_R32G32B32A32(10),          // 32*4 bpp (4 channels - float)
        PIXELFORMAT_UNCOMPRESSED_R16(11),                // 16 bpp (1 channel - half float)
        PIXELFORMAT_UNCOMPRESSED_R16G16B16(12),          // 16*3 bpp (3 channels - half float)
        PIXELFORMAT_UNCOMPRESSED_R16G16B16A16(13),       // 16*4 bpp (4 channels - half float)
        PIXELFORMAT_COMPRESSED_DXT1_RGB(14),                // 4 bpp (no alpha)
        PIXELFORMAT_COMPRESSED_DXT1_RGBA(15),               // 4 bpp (1 bit alpha)
        PIXELFORMAT_COMPRESSED_DXT3_RGBA(16),               // 8 bpp
        PIXELFORMAT_COMPRESSED_DXT5_RGBA(17),               // 8 bpp
        PIXELFORMAT_COMPRESSED_ETC1_RGB(18),                // 4 bpp
        PIXELFORMAT_COMPRESSED_ETC2_RGB(19),                // 4 bpp
        PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA(20),           // 8 bpp
        PIXELFORMAT_COMPRESSED_PVRT_RGB(21),                // 4 bpp
        PIXELFORMAT_COMPRESSED_PVRT_RGBA(22),               // 4 bpp
        PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA(23),           // 8 bpp
        PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA(24);           // 2 bpp

        private final int format;

        rlPixelFormat(int value) {
            this.format = value;
        }

        public int GetFormat() {
            return format;
        }
    }

    // Texture parameters: filter mode
    // NOTE 1: Filtering considers mipmaps if available in the texture
    // NOTE 2: Filter is accordingly set for minification and magnification
    public enum rlTextureFilterMode {
        TEXTURE_FILTER_POINT(0),                   // No filter, just pixel approximation
        TEXTURE_FILTER_BILINEAR(1),                // Linear filtering
        TEXTURE_FILTER_TRILINEAR(2),               // Trilinear filtering  = linear with mipmaps)
        TEXTURE_FILTER_ANISOTROPIC_4X(3),          // Anisotropic filtering 4x
        TEXTURE_FILTER_ANISOTROPIC_8X(4),          // Anisotropic filtering 8x
        TEXTURE_FILTER_ANISOTROPIC_16X(5);         // Anisotropic filtering 16x
        
        private final int mode;

        rlTextureFilterMode(int value) {
            this.mode = value;
        }

        public int GetMode() {
            return mode;
        }
    }

    // Color blending modes (pre-defined)
    public enum rlBlendMode {
        BLEND_ALPHA(0),                    // Blend textures considering alpha  = default)
        BLEND_ADDITIVE(1),                 // Blend textures adding colors
        BLEND_MULTIPLIED(2),               // Blend textures multiplying colors
        BLEND_ADD_COLORS(3),               // Blend textures adding colors (alternative)
        BLEND_SUBTRACT_COLORS(4),          // Blend textures subtracting colors (alternative)
        BLEND_ALPHA_PREMULTIPLY(5),        // Blend premultiplied textures considering alpha
        BLEND_CUSTOM(6),                   // Blend textures using custom src/dst factors (use SetBlendModeCustom())
        BLEND_CUSTOM_SEPARATE(7);          // Blend textures using custom src/dst factors (use rlSetBlendFactorsSeparate())

        private final int mode;

        rlBlendMode(int value) {
            this.mode = value;
        }

        public int GetMode() {
            return mode;
        }
    }

    // Shader location point type
    public enum rlShaderLocationIndex {
        SHADER_LOC_VERTEX_POSITION(0), // Shader location: vertex attribute: position
        SHADER_LOC_VERTEX_TEXCOORD01(1), // Shader location: vertex attribute: texcoord01
        SHADER_LOC_VERTEX_TEXCOORD02(2), // Shader location: vertex attribute: texcoord02
        SHADER_LOC_VERTEX_NORMAL(3), // Shader location: vertex attribute: normal
        SHADER_LOC_VERTEX_TANGENT(4), // Shader location: vertex attribute: tangent
        SHADER_LOC_VERTEX_COLOR(5), // Shader location: vertex attribute: color
        SHADER_LOC_MATRIX_MVP(6), // Shader location: matrix uniform: model-view-projection
        SHADER_LOC_MATRIX_VIEW(7), // Shader location: matrix uniform: view (camera transform)
        SHADER_LOC_MATRIX_PROJECTION(8), // Shader location: matrix uniform: projection
        SHADER_LOC_MATRIX_MODEL(9), // Shader location: matrix uniform: model (transform)
        SHADER_LOC_MATRIX_NORMAL(10), // Shader location: matrix uniform: normal
        SHADER_LOC_VECTOR_VIEW(11), // Shader location: vector uniform: view
        SHADER_LOC_COLOR_DIFFUSE(12), // Shader location: vector uniform: diffuse color
        SHADER_LOC_COLOR_SPECULAR(13), // Shader location: vector uniform: specular color
        SHADER_LOC_COLOR_AMBIENT(14), // Shader location: vector uniform: ambient color
        SHADER_LOC_MAP_ALBEDO(15), // Shader location: sampler2d texture: albedo (same as: SHADER_LOC_MAP_DIFFUSE)
        SHADER_LOC_MAP_DIFFUSE(15),
        SHADER_LOC_MAP_METALNESS(16), // Shader location: sampler2d texture: metalness (same as: SHADER_LOC_MAP_SPECULAR)
        SHADER_LOC_MAP_SPECULAR(16),
        SHADER_LOC_MAP_NORMAL(17), // Shader location: sampler2d texture: normal
        SHADER_LOC_MAP_ROUGHNESS(18), // Shader location: sampler2d texture: roughness
        SHADER_LOC_MAP_OCCLUSION(19), // Shader location: sampler2d texture: occlusion
        SHADER_LOC_MAP_EMISSION(20), // Shader location: sampler2d texture: emission
        SHADER_LOC_MAP_HEIGHT(21), // Shader location: sampler2d texture: height
        SHADER_LOC_MAP_CUBEMAP(22), // Shader location: samplerCube texture: cubemap
        SHADER_LOC_MAP_IRRADIANCE(23), // Shader location: samplerCube texture: irradiance
        SHADER_LOC_MAP_PREFILTER(24), // Shader location: samplerCube texture: prefilter
        SHADER_LOC_MAP_BRDF(25); // Shader location: sampler2d texture: brdf
        
        private final int location;

        rlShaderLocationIndex(int value) {
            this.location = value;
        }

        public int GetLocation() {
            return location;
        }
    }

    // Shader uniform data types
    public enum rlShaderUniformDataType {
        SHADER_UNIFORM_FLOAT(0), // Shader uniform type: float
        SHADER_UNIFORM_VEC2(1), // Shader uniform type: vec2 (2 float)
        SHADER_UNIFORM_VEC3(2), // Shader uniform type: vec3 (3 float)
        SHADER_UNIFORM_VEC4(3), // Shader uniform type: vec4 (4 float)
        SHADER_UNIFORM_INT(4), // Shader uniform type: int
        SHADER_UNIFORM_IVEC2(5), // Shader uniform type: ivec2 (2 int)
        SHADER_UNIFORM_IVEC3(6), // Shader uniform type: ivec3 (3 int)
        SHADER_UNIFORM_IVEC4(7), // Shader uniform type: ivec4 (4 int)
        SHADER_UNIFORM_UINT(8), // Shader uniform type: unsigned int
        SHADER_UNIFORM_UIVEC2(9), // Shader uniform type: uivec2 (2 unsigned int)
        SHADER_UNIFORM_UIVEC3(10), // Shader uniform type: uivec3 (3 unsigned int)
        SHADER_UNIFORM_UIVEC4(11), // Shader uniform type: uivec4 (4 unsigned int)
        SHADER_UNIFORM_SAMPLER2D(12); // Shader uniform type: sampler2d

        private final int type;

        rlShaderUniformDataType(int value) {
            this.type = value;
        }

        public int GetType() {
            return type;
        }
    }

    public enum rlShaderAttributeDataType {
        SHADER_ATTRIB_FLOAT(0),        // Shader attribute type: float
        SHADER_ATTRIB_VEC2(1),        // Shader attribute type: vec2 (2 float)
        SHADER_ATTRIB_VEC3(2),        // Shader attribute type: vec3 (3 float)
        SHADER_ATTRIB_VEC4(3);        // Shader attribute type: vec4 (4 float)

        private final int type;

        rlShaderAttributeDataType(int value) {
            this.type = value;
        }

        public int GetType() {
            return type;
        }
    }

    public enum rlFramebufferAttachType{
        ATTACHMENT_COLOR_CHANNEL0(0),
        ATTACHMENT_COLOR_CHANNEL1(1),
        ATTACHMENT_COLOR_CHANNEL2(2),
        ATTACHMENT_COLOR_CHANNEL3(3),
        ATTACHMENT_COLOR_CHANNEL4(4),
        ATTACHMENT_COLOR_CHANNEL5(5),
        ATTACHMENT_COLOR_CHANNEL6(6),
        ATTACHMENT_COLOR_CHANNEL7(7),
        ATTACHMENT_DEPTH(100),
        ATTACHMENT_STENCIL(200);

        private final int type;

        rlFramebufferAttachType(int value) {
            this.type = value;
        }

        public int GetType() {
            return type;
        }
    }

    public enum rlFramebufferAttachTextureType{
        ATTACHMENT_CUBEMAP_POSITIVE_X(0),
        ATTACHMENT_CUBEMAP_NEGATIVE_X(1),
        ATTACHMENT_CUBEMAP_POSITIVE_Y(2),
        ATTACHMENT_CUBEMAP_NEGATIVE_Y(3),
        ATTACHMENT_CUBEMAP_POSITIVE_Z(4),
        ATTACHMENT_CUBEMAP_NEGATIVE_Z(5),
        ATTACHMENT_TEXTURE2D(100),
        ATTACHMENT_RENDERBUFFER(200);

        private final int type;

        rlFramebufferAttachTextureType(int value) {
            this.type = value;
        }

        public int GetType() {
            return type;
        }
    }

    public enum rlCullMode {
        RL_FRONT,
        RL_BACK
    }

    private final Raylib context;
    private final GL_33 gl33;
    private final GL_11 gl11;

    private boolean isGpuReady;

    public RLGL(Raylib context) {
        this.context = context;
        rlglData = new rlglData();
        gl33 = new GL_33(context);
        gl11 = new GL_11(context);
    }

    public void rlMatrixMode(int mode){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlMatrixMode(mode);
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlMatrixMode(mode);
        }
    }

    public void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlFrustum(left, right, bottom, top, znear, zfar);
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlFrustum(left, right, bottom, top, znear, zfar);
        }
    }

    public void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlOrtho(left, right, bottom, top, znear, zfar);
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlOrtho(left, right, bottom, top, znear, zfar);
        }
    }

    public void rlPushMatrix(){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlPushMatrix();
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlPushMatrix();
        }
    }

    public void rlPopMatrix(){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlPopMatrix();
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlPopMatrix();
        }
    }

    public void rlLoadIdentity(){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlLoadIdentity();
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlLoadIdentity();
        }
    }

    public void rlTranslatef(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlTranslatef(x, y, z);
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlTranslatef(x, y, z);
        }
    }

    public void rlRotatef(float angle, float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlRotatef(angle, x, y, z);
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlRotatef(angle, x, y, z);
        }
    }

    public void rlScalef(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlScalef(x, y, z);

        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlScalef(x, y, z);
        }
    }

    public void rlMultMatrixf(float[] matf){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlMultMatrixf(matf);
        }
        else if (GRAPHICS_API_OPENGL_11){
            gl11.rlMultMatrixf(matf);
        }

    }

    // Set the viewport area (transformation from normalized device coordinates to window coordinates)
    // NOTE: We store current viewport dimensions
    public void rlViewport(int x, int y, int width, int height){
        glViewport(x, y, width, height);
    }

    // Set clip planes distances
    public void rlSetClipPlanes(double nearPlane, double farPlane) {
        rlCullDistanceNear = nearPlane;
        rlCullDistanceFar = farPlane;
    }

    // Get cull plane distance near
    public double rlGetCullDistanceNear() {
        return rlCullDistanceNear;
    }

    // Get cull plane distance far
    public double rlGetCullDistanceFar() {
        return rlCullDistanceFar;
    }

    public void rlBegin(int mode){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlBegin(mode);
        }
        else{
            gl11.rlBegin(mode);
        }
    }

    public void rlEnd(){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlEnd();
        }
        else{
            gl11.rlEnd();
        }
    }

    // Define one vertex (position)
    // NOTE: Vertex position data is the basic information required for drawing
    public void rlVertex3f(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlVertex3f(x, y, z);
        }
        else{
            gl11.rlVertex3f(x, y, z);
        }
    }

    // Define one vertex (position)
    public void rlVertex2f(float x, float y){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlVertex2f(x, y);
        }
        else{
            gl11.rlVertex2f(x, y);
        }
    }

    // Define one vertex (position)
    public void rlVertex2i(int x, int y){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlVertex2i(x, y);
        }
        else{
            gl11.rlVertex2i(x, y);
        }
    }

    // Define one vertex (texture coordinate)
    // NOTE: Texture coordinates are limited to QUADS only
    public void rlTexCoord2f(float x, float y){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlTexCoord2f(x, y);
        }
        else{
            gl11.rlTexCoord2f(x, y);
        }
    }

    // Define one vertex (normal)
    // NOTE: Normals limited to TRIANGLES only?
    public void rlNormal3f(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlNormal3f(x, y, z);
        }
        else{
            gl11.rlNormal3f(x, y, z);
        }
    }

    // Define one vertex (color)
    public void rlColor4ub(int x, int y, int z, int w){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlColor4ub((byte)x, (byte)y, (byte)z, (byte)w);
        }
        else{
            gl11.rlColor4ub(x, y, z, w);
        }
    }

    // Define one vertex (color)
    void rlColor4f(float r, float g, float b, float a){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlColor4f(r, g, b, a);
        }
        else{
            gl11.rlColor4f(r, g, b, a);
        }
    }

    // Define one vertex (color)
    public void rlColor3f(float x, float y, float z){
        if (GRAPHICS_API_OPENGL_33){
            gl33.rlColor3f(x, y, z);
        }
        else{
            gl11.rlColor3f(x, y, z);
        }
    }

    public void rlSetTexture(int id){
        if (id == 0){
            if (GRAPHICS_API_OPENGL_11){
                rlDisableTexture();
            }
            else{
                // NOTE: If quads batch limit is reached, force a draw call and next batch starts
                if (rlglData.getState().vertexCounter >= rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].elementCount*4) {
                    rlDrawRenderBatch(rlglData.getCurrentBatch());
                }
                rlglData.getState().currentTextureId = rlglData.getState().defaultTextureId;
            }
        }
        else{
            if (GRAPHICS_API_OPENGL_11){
                rlSetTexture(id);
            }
            else{
                rlglData.getState().currentTextureId = id;
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
                            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode = rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 2].mode;
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
    public void rlActiveTextureSlot(int slot) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2)
            glActiveTexture(GL_TEXTURE0 + slot);
    }

    // Enable texture
    public void rlEnableTexture(int id) {
        if(GRAPHICS_API_OPENGL_11){
            glEnable(GL_TEXTURE_2D);
        }

        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void rlDisableTexture(){
        if (GRAPHICS_API_OPENGL_11){
            glDisable(GL_TEXTURE_2D);
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Enable texture cubemap
    public void rlEnableTextureCubemap(int id){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glEnable(GL_TEXTURE_CUBE_MAP);   // rCore in OpenGL 1.4
            glBindTexture(GL_TEXTURE_CUBE_MAP, id);
        }
    }

    // Disable texture cubemap
    public void rlDisableTextureCubemap(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDisable(GL_TEXTURE_CUBE_MAP);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        }
    }

    // Set texture parameters (wrap mode/filter mode)
    public void rlTextureParameters(int id, int param, int value){
        glBindTexture(GL_TEXTURE_2D, id);

        switch (param){
            case RL_TEXTURE_WRAP_S:
            case RL_TEXTURE_WRAP_T:{
                if (value == RL_TEXTURE_WRAP_MIRROR_CLAMP){
                    if (GRAPHICS_API_OPENGL_11){
                        if (rlglData.getExtSupported().isTexMirrorClamp()){
                            glTexParameteri(GL_TEXTURE_2D, param, value);
                        }
                        else{
                            context.tracelog.TRACELOG(LOG_WARNING, "GL: Clamp mirror wrap mode not supported (GL_MIRROR_CLAMP_EXT)");
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
                if (!GRAPHICS_API_OPENGL_11){
                    // Reset anisotropy filter, in case it was set
                    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 1.0f);
                    if (value <= rlglData.getExtSupported().getMaxAnisotropyLevel()){
                        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else if (rlglData.getExtSupported().getMaxAnisotropyLevel() > 0.0f){
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Maximum anisotropic filter level supported is " +
                                rlglData.getExtSupported().getMaxAnisotropyLevel());
                        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else{
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Anisotropic filtering not supported");
                    }
                }
            }
            break;
            default:
                break;
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void rlCubemapParameters(int id, int param, int value) {
        glBindTexture(GL_TEXTURE_CUBE_MAP, id);

        if(!GRAPHICS_API_OPENGL_11) {
            // Reset anisotropy filter, in case it was set
            glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_ANISOTROPY_EXT, 1.0f);
        }

        switch (param)
        {
            case RL_TEXTURE_WRAP_S:
            case RL_TEXTURE_WRAP_T:
                if (value == RL_TEXTURE_WRAP_MIRROR_CLAMP) {
                    if(!GRAPHICS_API_OPENGL_11) {
                        if (rlglData.getExtSupported().texMirrorClamp) {
                            glTexParameteri(GL_TEXTURE_CUBE_MAP, param, value);
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "GL: Clamp mirror wrap mode not supported (GL_MIRROR_CLAMP_EXT)");
                        }
                    }
                }
                else {
                    glTexParameteri(GL_TEXTURE_CUBE_MAP, param, value);
                }
                break;
            case RL_TEXTURE_MAG_FILTER:
            case RL_TEXTURE_MIN_FILTER:
                glTexParameteri(GL_TEXTURE_CUBE_MAP, param, value);
                break;
            case RL_TEXTURE_FILTER_ANISOTROPIC:
                if(!GRAPHICS_API_OPENGL_11) {
                    if (value <= rlglData.getExtSupported().maxAnisotropyLevel) {
                        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else if (rlglData.getExtSupported().maxAnisotropyLevel > 0.0f) {
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Maximum anisotropic filter level supported is " + rlglData.getExtSupported().maxAnisotropyLevel);
                        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Anisotropic filtering not supported");
                    }
                }
                break;
            case RL_TEXTURE_MIPMAP_BIAS_RATIO:
                if(GRAPHICS_API_OPENGL_33) {
                    glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, value / 100.0f);
                }
                break;
            default:
                break;
        }

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    // Enable shader program usage
    public void rlEnableShader(int id){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(id);
        }
    }

    // Disable shader program usage
    public void rlDisableShader(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(0);
        }
    }

    // Enable rendering to texture (fbo)
    public void rlEnableFramebuffer(int id){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            glBindFramebuffer(GL_FRAMEBUFFER, id);
        }
    }

    // return the active render texture (fbo)
    public int rlGetActiveFramebuffer() {
        int fboId = 0;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE) {
            fboId = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        }

        return fboId;
    }

    // Disable rendering to texture
    public void rlDisableFramebuffer(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    // Blit active framebuffer to main framebuffer
    public void rlBlitFramebuffer(int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight, int bufferMask) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            glBlitFramebuffer(srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight, bufferMask, GL_NEAREST);
        }
    }

    // Bind framebuffer object (fbo)
    public void rlBindFramebuffer(int target, int framebuffer) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE) {
            glBindFramebuffer(target, framebuffer);
        }
    }

    // Activate multiple draw color buffers
    // NOTE: One color buffer is always active by default
    public void rlActiveDrawBuffers(int count){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES3 || GRAPHICS_API_OPENGL_SOFTWARE){
            // NOTE: Maximum number of draw buffers supported is implementation dependant,
            // it can be queried with glGet*() but it must be at least 8
            //GLint maxDrawBuffers = 0;
            //glGetIntegerv(GL_MAX_DRAW_BUFFERS, &maxDrawBuffers);

            if (count > 0){
                if (count > 8){
                    context.tracelog.TRACELOG(LOG_WARNING, "GL: Max color buffers limited to 8");
                }
                else{
                    if (GRAPHICS_API_OPENGL_ES3) {
                        int[] buffers = {
                                GL_COLOR_ATTACHMENT0_EXT,
                                GL_COLOR_ATTACHMENT1_EXT,
                                GL_COLOR_ATTACHMENT2_EXT,
                                GL_COLOR_ATTACHMENT3_EXT,
                                GL_COLOR_ATTACHMENT4_EXT,
                                GL_COLOR_ATTACHMENT5_EXT,
                                GL_COLOR_ATTACHMENT6_EXT,
                                GL_COLOR_ATTACHMENT7_EXT,
                        };
                        glDrawBuffersEXT(buffers);
                    }
                    else {
                        int[] buffers = {
                                GL_COLOR_ATTACHMENT0,
                                GL_COLOR_ATTACHMENT1,
                                GL_COLOR_ATTACHMENT2,
                                GL_COLOR_ATTACHMENT3,
                                GL_COLOR_ATTACHMENT4,
                                GL_COLOR_ATTACHMENT5,
                                GL_COLOR_ATTACHMENT6,
                                GL_COLOR_ATTACHMENT7,
                        };
                        glDrawBuffers(buffers);
                    }
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "GL: One color buffer active by default");
            }
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
    public void rlEnableDepthMask(){
        glDepthMask(true);
    }

    // Disable depth write
    public void rlDisableDepthMask(){
        glDepthMask(false);
    }

    // Enable backface culling
    public void rlEnableBackfaceCulling(){
        glEnable(GL_CULL_FACE);
    }

    // Disable backface culling
    public void rlDisableBackfaceCulling(){
        glDisable(GL_CULL_FACE);
    }

    // Set color mask active for screen read/draw
    public void rlColorMask(boolean r, boolean g, boolean b, boolean a) {
        glColorMask(r, g, b, a);
    }

    public void rlCullFace(rlCullMode mode) {
        switch (mode) {
            case RL_BACK:
                glCullFace(GL_BACK);
                break;
            case RL_FRONT:
                glCullFace(GL_FRONT);
                break;
        }
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
    public void rlEnableWireMode(){
        if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
    }

    // Disable wire mode
    public void rlDisableWireMode(){
        if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    // Enable point mode
    public void rlEnablePointMode() {
        if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33) {
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
            glEnable(GL_PROGRAM_POINT_SIZE);
        }
    }

    // Disable point mode
    public void rlDisablePointMode() {
        if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33) {
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

    // Set the point drawing size
    public void rlSetPointSize(float size) {
        if (GRAPHICS_API_OPENGL_11){
            glPointSize(size);
        }
    }

    // Get the point drawing size
    public float rlGetPointSize() {
        float size = 1;

        if(GRAPHICS_API_OPENGL_11) {
            size = glGetFloat(GL_POINT_SIZE);
        }

        return size;
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

    public boolean rlIsStereoRendererEnabled(){
        return rlglData.getState().isStereoRender();
    }

    public void rlClearColor(int r, int g, int b, int a){
        float cr = (float) r / 255;
        float cg = (float) g / 255;
        float cb = (float) b / 255;
        float ca = (float) a / 255;

        glClearColor(cr, cg, cb, ca);
    }

    public void rlClearScreenBuffers(){
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
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_INVALID_ENUM");
                        break;
                    case 0x0501:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_INVALID_VALUE");
                        break;
                    case 0x0502:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_INVALID_OPERATION");
                        break;
                    case 0x0503:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_STACK_OVERFLOW");
                        break;
                    case 0x0504:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_STACK_UNDERFLOW");
                        break;
                    case 0x0505:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_OUT_OF_MEMORY");
                        break;
                    case 0x0506:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: GL_INVALID_FRAMEBUFFER_OPERATION");
                        break;
                    default:
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: Error detected: Unknown error code: " + err);
                        break;
                }
            }
        }
    }

    // Set blend mode
    public void rlSetBlendMode(rlBlendMode mode){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getState().getCurrentBlendMode() != mode.GetMode() ||
                    ((mode == BLEND_CUSTOM || mode == BLEND_CUSTOM_SEPARATE) &&
                    rlglData.getState().glCustomBlendModeModified))
            {
                rlDrawRenderBatch(rlglData.getCurrentBatch());

                switch (mode){
                    case BLEND_ALPHA:
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case BLEND_ADDITIVE:
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case BLEND_MULTIPLIED:
                        glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case BLEND_ADD_COLORS:
                        glBlendFunc(GL_ONE, GL_ONE);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case BLEND_SUBTRACT_COLORS:
                        glBlendFunc(GL_ONE, GL_ONE);
                        glBlendEquation(GL_FUNC_SUBTRACT);
                        break;
                    case BLEND_ALPHA_PREMULTIPLY:
                        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                        glBlendEquation(GL_FUNC_ADD);
                        break;
                    case BLEND_CUSTOM:
                        // NOTE: Using GL blend src/dst factors and GL equation configured with rlSetBlendFactors()
                        glBlendFunc(rlglData.getState().glBlendSrcFactor, rlglData.getState().glBlendDstFactor);
                        glBlendEquation(rlglData.getState().glBlendEquation);
                        break;
                    case BLEND_CUSTOM_SEPARATE:
                        // NOTE: Using GL blend src/dst factors and GL equation configured with rlSetBlendFactorsSeparate()
                        glBlendFuncSeparate(rlglData.getState().glBlendSrcFactorRGB, rlglData.getState().glBlendDestFactorRGB, rlglData.getState().glBlendSrcFactorAlpha, rlglData.getState().glBlendDestFactorAlpha);
                        glBlendEquationSeparate(rlglData.getState().glBlendEquationRGB, rlglData.getState().glBlendEquationAlpha);
                        break;
                    default:
                        break;
                }

                rlglData.getState().setCurrentBlendMode(mode.mode);
            }
        }
    }

    // Set blending mode factor and equation used by glBlendFuncSeparate and glBlendEquationSeparate
    void rlSetBlendFactorsSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha, int modeRGB, int modeAlpha) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getState().glBlendSrcFactorRGB == srcRGB
                    && rlglData.getState().glBlendDestFactorRGB == dstRGB
                    && rlglData.getState().glBlendSrcFactorAlpha == srcAlpha
                    && rlglData.getState().glBlendDestFactorAlpha == dstAlpha
                    && rlglData.getState().glBlendEquationRGB == modeRGB
                    && rlglData.getState().glBlendEquationAlpha == modeAlpha) {
                return;
            }
            else {
                rlglData.getState().glBlendSrcFactorRGB = srcRGB;
                rlglData.getState().glBlendDestFactorRGB = dstRGB;
                rlglData.getState().glBlendSrcFactorAlpha = srcAlpha;
                rlglData.getState().glBlendDestFactorAlpha = dstAlpha;
                rlglData.getState().glBlendEquationRGB = modeRGB;
                rlglData.getState().glBlendEquationAlpha = modeAlpha;
                rlglData.getState().glCustomBlendModeModified = true;
            }
        }
    }

    // Set blending mode factor and equation
    public void rlSetBlendFactors(int glSrcFactor, int glDstFactor, int glEquation){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getState().glBlendSrcFactor == glSrcFactor
                    && rlglData.getState().glBlendDstFactor == glDstFactor
                    && rlglData.getState().glBlendEquation == glEquation) {
                return;
            }
            else {
                rlglData.getState().setGlBlendSrcFactor(glSrcFactor);
                rlglData.getState().setGlBlendDstFactor(glDstFactor);
                rlglData.getState().setGlBlendEquation(glEquation);
                rlglData.getState().glCustomBlendModeModified = true;
            }
        }
    }

    public void rlglInit(int width, int height) {
        isGpuReady = true;

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
            byte[] pixels = {
                    (byte) 255, (byte) 255, (byte) 255, (byte) 255
            };

            rlglData.getState().setDefaultTextureId(rlLoadTexture(pixels, 1, 1, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1));
            rlglData.getState().setCurrentTextureId(rlglData.getState().getDefaultTextureId());

            if(rlglData.getState().getDefaultTextureId() != 0) {
                context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + rlglData.getState().getDefaultTextureId() + "] Default texture loaded successfully");
            }
            else{
                context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: Failed to load default texture");
            }

            // Init default Shader (customized for GL 3.3 and ES2)
            // Loaded: RLGL.State.defaultShaderId + RLGL.State.defaultShaderLocs
            rlLoadShaderDefault();
            rlglData.getState().currentShaderId = rlglData.getState().defaultShaderId;
            rlglData.getState().currentShaderLocs = rlglData.getState().defaultShaderLocs;

            // Init default vertex arrays buffers
            // Simulate that the default shader has the location RL_SHADER_LOC_VERTEX_NORMAL to bind the normal buffer for the default render batch
            rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_NORMAL.GetLocation()] = RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL;

            rlglData.setDefaultBatch(rlLoadRenderBatch(DEFAULT_BATCH_BUFFERS, RL_DEFAULT_BATCH_BUFFER_ELEMENTS));
            rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_NORMAL.GetLocation()] = -1;
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

        if (GRAPHICS_API_OPENGL_SOFTWARE) {
            // Initialize software renderer backend
            // int result = swInit(width, height);
            int result = 0;
            if (result == 0) {
                context.tracelog.TRACELOG(LOG_ERROR, "RLSW: Software renderer initialization failed!");
                System.exit(-1);
            }
        }

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

        if (GRAPHICS_API_OPENGL_11){
            // Init state: Color hints (deprecated in OpenGL 3.0+)
            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);      // Improve quality of color and texture coordinate interpolation
            glShadeModel(GL_SMOOTH);                                // Smooth shading between vertex (vertex colors interpolation)
        }

        // Init state: Cubemap seamless
        if (GRAPHICS_API_OPENGL_33){
            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);                 // Seamless cubemaps (not supported on OpenGL ES 2.0)
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // Store screen size into global variables
            rlglData.getState().setFramebufferWidth(width);
            rlglData.getState().setFramebufferHeight(height);
        }

        // Init state: custom blend factor and equation modification flag
        rlglData.getState().glCustomBlendModeModified = false;

        // Init state: Color/Depth buffers clear
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Set clear color (black)
        glClearDepth(1.0f);                                           // Set clear depth value (default)
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // Clear color and depth buffers (depth buffer required for 3D)

        context.tracelog.TRACELOG(LOG_INFO, "RLGL: Default OpenGL state initialized successfully");
    }

    // Vertex Buffer Object deinitialization (memory free)
    public void rlglClose(){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            UnloadRenderBatch(rlglData.getDefaultBatch());

            rlUnloadShaderDefault(); // Unload default shader
            glDeleteTextures(rlglData.getState().getDefaultTextureId()); // Unload default texture

            context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + rlglData.getState().getDefaultTextureId() + "] Default texture unloaded successfully");
        }

        if (GRAPHICS_API_OPENGL_SOFTWARE) {
            // swClose();
        }

        isGpuReady = false;
    }

    // Load OpenGL extensions
    // NOTE: External loader function could be passed as a pointer
    public void rlLoadExtensions(){
        if(GRAPHICS_API_OPENGL_33) {     // Also defined for GRAPHICS_API_OPENGL_21
            // NOTE: glad is generated and contains only required OpenGL 3.3 rCore extensions (and lower versions)
            if(!__APPLE__) {
                //if (!gladLoadGLLoader((GLADloadproc) loader))
                //    logger(LOG_WARNING, "GLAD: Cannot load OpenGL extensions");
                //else
                //logger(LOG_INFO, "GLAD: OpenGL extensions loaded successfully");
            }

            // Get number of supported extensions
            int numExt = glGetInteger(GL_NUM_EXTENSIONS);
            context.tracelog.TRACELOG(LOG_INFO, "GL: Supported extensions count: " + numExt);

            if (SUPPORT_GL_DETAILS_INFO){
                // Get supported extensions list
                // WARNING: glGetStringi() not available on OpenGL 2.1
                String[] extList = new String[numExt];
                context.tracelog.TRACELOG(LOG_INFO, "GL: OpenGL extensions:");
                for (int i = 0; i < numExt; i++){
                    extList[i] = glGetStringi(GL_EXTENSIONS, i);
                    context.tracelog.TRACELOG(LOG_INFO, "    " + extList[i]);
                }
                extList = null;       // Free extensions pointers
            }

            // Register supported extensions flags
            // OpenGL 3.3 extensions supported by default (core)
            rlglData.getExtSupported().vao = true;
            rlglData.getExtSupported().instancing = true;
            rlglData.getExtSupported().texNPOT = true;
            rlglData.getExtSupported().texFloat32 = true;
            rlglData.getExtSupported().texFloat16 = true;
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

        if (GRAPHICS_API_OPENGL_ES3) {
            // Register supported extensions flags
            // OpenGL ES 3.0 extensions supported by default (or it should be)
            rlglData.getExtSupported().vao = true;
            rlglData.getExtSupported().instancing = true;
            rlglData.getExtSupported().texNPOT = true;
            rlglData.getExtSupported().texFloat32 = true;
            rlglData.getExtSupported().texFloat16 = true;
            rlglData.getExtSupported().texDepth = true;
            rlglData.getExtSupported().texDepthWebGL = true;
            rlglData.getExtSupported().maxDepthBits = 24;
            rlglData.getExtSupported().texAnisoFilter = true;
            rlglData.getExtSupported().texMirrorClamp = true;
            // TODO: Check for additional OpenGL ES 3.0 supported extensions:
            //rlglData.getExtSupported().texCompDXT = true;
            //rlglData.getExtSupported().texCompETC1 = true;
            //rlglData.getExtSupported().texCompETC2 = true;
            //rlglData.getExtSupported().texCompPVRT = true;
            //rlglData.getExtSupported().texCompASTC = true;
            //rlglData.getExtSupported().maxAnisotropyLevel = true;
            //rlglData.getExtSupported().computeShader = true;
            //rlglData.getExtSupported().ssbo = true;
        }
        else if (GRAPHICS_API_OPENGL_ES2){
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

            context.tracelog.TRACELOG(LOG_INFO, "GL: Supported extensions count: " + numExt);

            if (SUPPORT_GL_DETAILS_INFO){
                context.tracelog.TRACELOG(LOG_INFO, "GL: OpenGL extensions:");
                for (int i = 0; i < numExt; i++) context.tracelog.TRACELOG(LOG_INFO, "    " + extList[i]);
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
                if (extList[i].equals("GL_OES_texture_npot")) {
                    rlglData.getExtSupported().setTexNPOT(true);
                }

                // Check texture float support
                if (extList[i].equals("GL_OES_texture_float")) {
                    rlglData.getExtSupported().setTexFloat32(true);
                }
                if (extList[i].equals("GL_OES_texture_half_float")) {
                    rlglData.getExtSupported().setTexFloat16(true);
                }

                // Check depth texture support
                if (extList[i].equals("GL_OES_depth_texture")) {
                    rlglData.getExtSupported().setTexDepth(true);
                }
                if (extList[i].equals("GL_WEBGL_depth_texture")) {
                    rlglData.getExtSupported().setTexDepthWebGL(true); // WebGL requires unsized internal format
                }
                if (rlglData.getExtSupported().texDepthWebGL) {
                    rlglData.getExtSupported().setTexDepth(true);
                }

                if (extList[i].equals("GL_OES_depth24")) {
                    rlglData.getExtSupported().maxDepthBits = 24;   // Not available on WebGL
                }
                if (extList[i].equals("GL_OES_depth32")) {
                    rlglData.getExtSupported().maxDepthBits = 32;   // Not available on WebGL
                }

                if (extList[i].equals("GL_OES_depth24")) {
                    rlglData.getExtSupported().setMaxDepthBits(24);
                }
                if (extList[i].equals("GL_OES_depth32")) {
                    rlglData.getExtSupported().setMaxDepthBits(32);
                }

                // Check texture compression support: DXT
                if (extList[i].equals("GL_EXT_texture_compression_s3tc") ||
                        extList[i].equals("GL_WEBGL_compressed_texture_s3tc")  ||
                        extList[i].equals("GL_WEBKIT_WEBGL_compressed_texture_s3tc")) {

                    rlglData.getExtSupported().setTexCompDXT(true);
                }

                // Check texture compression support: ETC1
                if (extList[i].equals("GL_OES_compressed_ETC1_RGB8_texture") ||
                        extList[i].equals("GL_WEBGL_compressed_texture_etc1")) {
                    rlglData.getExtSupported().setTexCompETC1(true);
                }

                // Check texture compression support: ETC2/EAC
                if (extList[i].equals("GL_ARB_ES3_compatibility")) {
                    rlglData.getExtSupported().setTexCompETC2(true);
                }

                // Check texture compression support: PVR
                if (extList[i].equals("GL_IMG_texture_compression_pvrtc")) {
                    rlglData.getExtSupported().setTexCompPVRT(true);
                }

                // Check texture compression support: ASTC
                if (extList[i].equals("GL_KHR_texture_compression_astc_hdr")) {
                    rlglData.getExtSupported().setTexCompASTC(true);
                }

                // Check anisotropic texture filter support
                if (extList[i].equals("GL_EXT_texture_filter_anisotropic")) {
                    rlglData.getExtSupported().setTexAnisoFilter(true);
                }

                // Check clamp mirror wrap mode support
                if (extList[i].equals("GL_EXT_texture_mirror_clamp")) {
                    rlglData.getExtSupported().setTexMirrorClamp(true);
                }
            }
        }  // GRAPHICS_API_OPENGL_ES2

        // Check OpenGL information and capabilities
        //------------------------------------------------------------------------------
        // Show current OpenGL and GLSL version
        context.tracelog.TRACELOG(LOG_INFO, "GL: OpenGL device information:");
        context.tracelog.TRACELOG(LOG_INFO, "    > Vendor:   " + glGetString(GL_VENDOR));
        context.tracelog.TRACELOG(LOG_INFO, "    > Renderer: " + glGetString(GL_RENDERER));
        context.tracelog.TRACELOG(LOG_INFO, "    > Version:  " + glGetString(GL_VERSION));
        context.tracelog.TRACELOG(LOG_INFO, "    > GLSL:     " +  glGetString(GL_SHADING_LANGUAGE_VERSION));

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // NOTE: Anisotropy levels capability is an extension
            int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;

            rlglData.getExtSupported().setMaxAnisotropyLevel(glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

            if (SUPPORT_GL_DETAILS_INFO){
                // Show some OpenGL GPU capabilities
                context.tracelog.TRACELOG(LOG_INFO, "GL: OpenGL capabilities:");
                int capability = 0;
                capability = glGetInteger(GL_MAX_TEXTURE_SIZE);
                context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_TEXTURE_SIZE: " + capability);
                capability = glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE);
                context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_CUBE_MAP_TEXTURE_SIZE: " + capability);
                capability = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS);
                context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_TEXTURE_IMAGE_UNITS: " + capability);
                capability = glGetInteger(GL_MAX_VERTEX_ATTRIBS);
                context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_VERTEX_ATTRIBS: " + capability);

                if (!GRAPHICS_API_OPENGL_ES2){
                    capability = glGetInteger(GL_MAX_UNIFORM_BLOCK_SIZE);
                    context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_UNIFORM_BLOCK_SIZE: " + capability);
                    capability = glGetInteger(GL_MAX_DRAW_BUFFERS);
                    context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_DRAW_BUFFERS: " + capability);
                    if (rlglData.getExtSupported().isTexAnisoFilter())
                        context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_TEXTURE_MAX_ANISOTROPY: " + rlglData.getExtSupported().getMaxAnisotropyLevel());
                }
                capability = glGetInteger(GL_NUM_COMPRESSED_TEXTURE_FORMATS);
                context.tracelog.TRACELOG(LOG_INFO, "    GL_NUM_COMPRESSED_TEXTURE_FORMATS: " + capability);
                int[] format = new int[32];
                glGetInteger(GL_COMPRESSED_TEXTURE_FORMATS);
                for (int i = 0; i < capability; i++) {
                    context.tracelog.TRACELOG(LOG_INFO, "        " + rlGetCompressedFormatName(format[i]));
                }

                if (GRAPHICS_API_OPENGL_43) {
                    IntBuffer capabilityIB = IntBuffer.allocate(1);
                    glGetIntegerv(GL_MAX_VERTEX_ATTRIB_BINDINGS, capabilityIB);
                    context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_VERTEX_ATTRIB_BINDINGS: " + capabilityIB.get());
                    glGetIntegerv(GL_MAX_UNIFORM_LOCATIONS, capabilityIB);
                    context.tracelog.TRACELOG(LOG_INFO, "    GL_MAX_UNIFORM_LOCATIONS: " +  capabilityIB.get());
                }
            }
            else{   // SUPPORT_GL_DETAILS_INFO
                // Show some basic info about GL supported features
                if (GRAPHICS_API_OPENGL_ES2){
                    if (rlglData.getExtSupported().isVao()) {
                        context.tracelog.TRACELOG(LOG_INFO, "GL: VAO extension detected, VAO functions loaded successfully");
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: VAO extension not found, VAO not supported");
                    }

                    if (rlglData.getExtSupported().isTexNPOT()) {
                        context.tracelog.TRACELOG(LOG_INFO, "GL: NPOT textures extension detected, full NPOT textures supported");
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "GL: NPOT textures extension not found, limited NPOT support (no-mipmaps, no-repeat)");
                    }
                }
                if (rlglData.getExtSupported().isTexCompDXT()) {
                    context.tracelog.TRACELOG(LOG_INFO, "GL: DXT compressed textures supported");
                }

                if (rlglData.getExtSupported().isTexCompETC1()) {
                    context.tracelog.TRACELOG(LOG_INFO, "GL: ETC1 compressed textures supported");
                }

                if (rlglData.getExtSupported().isTexCompETC2()) {
                    context.tracelog.TRACELOG(LOG_INFO, "GL: ETC2/EAC compressed textures supported");
                }

                if (rlglData.getExtSupported().isTexCompPVRT()) {
                    context.tracelog.TRACELOG(LOG_INFO, "GL: PVRT compressed textures supported");
                }

                if (rlglData.getExtSupported().isTexCompASTC()) {
                    context.tracelog.TRACELOG(LOG_INFO, "GL: ASTC compressed textures supported");
                }
            }  // SUPPORT_GL_DETAILS_INFO
        }  // GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2
    }

    // rlGetProcAddress

    public rlGlVersion rlGetVersion(){
        rlGlVersion version = null;

        if (GRAPHICS_API_OPENGL_SOFTWARE) {
            version = OPENGL_SOFTWARE;
        }
        else if (GRAPHICS_API_OPENGL_11){
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
        else if (GRAPHICS_API_OPENGL_43) {
            version = OPENGL_43;
        }
        else if (GRAPHICS_API_OPENGL_33){
            version = OPENGL_33;
        }
        else if (GRAPHICS_API_OPENGL_ES3) {
            version = OPENGL_ES_30;
        }
        else if (GRAPHICS_API_OPENGL_ES2){
            version = OPENGL_ES_20;
        }
        
        return version;
    }

    // Set current framebuffer width
    public void rlSetFramebufferWidth(int width) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            rlglData.getState().framebufferWidth = width;
        }
    }

    // Set current framebuffer height
    public void rlSetFramebufferHeight(int height) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            rlglData.getState().framebufferHeight = height;
        }
    }

    // Get current framebuffer width
    public int rlGetFramebufferWidth(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            return rlglData.getState().getFramebufferWidth();
        }
        else{
            return 0;
        }
    }

    // Get current framebuffer height
    public int rlGetFramebufferHeight(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            return rlglData.getState().getFramebufferHeight();
        }
        else{
            return 0;
        }
    }

    // Get default internal texture (white texture)
    // NOTE: Default texture is a 1x1 pixel UNCOMPRESSED_R8G8B8A8
    public int rlGetTextureIdDefault() {
        int id = 0;
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = rlglData.getState().defaultTextureId;
        }
        return id;
    }

    // Get default shader id
    public int rlGetShaderIdDefault() {
        int id = 0;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = rlglData.getState().defaultShaderId;
        }
        return id;
    }

    // Get default shader locs
    public int[] rlGetShaderLocsDefault() {
        int[] locs = null;
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            locs = rlglData.getState().defaultShaderLocs;
        }
        return locs;
    }


    //Load render batch
    public rlRenderBatch rlLoadRenderBatch(int numBuffers, int bufferElements){
        rlRenderBatch batch = new rlRenderBatch();

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return batch;
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // Initialize CPU (RAM) vertex buffers (position, texcoord, color data and indexes)
            //--------------------------------------------------------------------------------------------
            batch.vertexBuffer = new rlVertexBuffer[numBuffers];

            for(int i = 0; i < batch.vertexBuffer.length; i++){
                batch.vertexBuffer[i] = new rlVertexBuffer();
            }

            for(int i = 0; i < numBuffers; i++){
                batch.vertexBuffer[i].elementCount = bufferElements;

                batch.vertexBuffer[i].setVertices(new float[bufferElements * 3 * 4]); // 3 float by vertex, 4 vertex by quad
                batch.vertexBuffer[i].setTexcoords(new float[bufferElements * 2 * 4]); // 2 float by texcoord, 4 texcoord by quad
                batch.vertexBuffer[i].setNormals(new float[bufferElements * 3 * 4]); // 3 float by vertex, 4 vertex by quad
                batch.vertexBuffer[i].setColors(new byte[bufferElements * 4 * 4]); // 4 float by color, 4 colors by quad

                if(GRAPHICS_API_OPENGL_33){
                    batch.getVertexBuffer()[i].setIndices_GL11(new int[bufferElements * 6]); // 6 int by quad (indices)
                }
                else if(GRAPHICS_API_OPENGL_ES2){
                    batch.vertexBuffer[i].setIndices_ES20(new short[bufferElements * 6]); // 6 int by quad (indices)
                }

                Arrays.fill(batch.vertexBuffer[i].vertices, 0.0f);
                Arrays.fill(batch.vertexBuffer[i].texcoords, 0.0f);
                Arrays.fill(batch.vertexBuffer[i].normals, 0.0f);
                Arrays.fill(batch.vertexBuffer[i].colors, (byte) 0);

                int k = 0;

                if(GRAPHICS_API_OPENGL_33){
                    // Indices can be initialized right now
                    for(int j = 0; j < (bufferElements * 6); j += 6) {
                        batch.vertexBuffer[i].indices_GL11[j] = 4 * k;
                        batch.vertexBuffer[i].indices_GL11[j + 1] = 4 * k + 1;
                        batch.vertexBuffer[i].indices_GL11[j + 2] = 4 * k + 2;
                        batch.vertexBuffer[i].indices_GL11[j + 3] = 4 * k;
                        batch.vertexBuffer[i].indices_GL11[j + 4] = 4 * k + 2;
                        batch.vertexBuffer[i].indices_GL11[j + 5] = 4 * k + 3;

                        k++;
                    }
                }

                if(GRAPHICS_API_OPENGL_ES2){
                    // Indices can be initialized right now
                    for(int j = 0; j < (bufferElements * 6); j += 6){
                        batch.vertexBuffer[i].getIndices_ES20()[j] = (short) (4 * k);
                        batch.vertexBuffer[i].getIndices_ES20()[j + 1] = (short) (4 * k + 1);
                        batch.vertexBuffer[i].getIndices_ES20()[j + 2] = (short) (4 * k + 2);
                        batch.vertexBuffer[i].getIndices_ES20()[j + 3] = (short) (4 * k);
                        batch.vertexBuffer[i].getIndices_ES20()[j + 4] = (short) (4 * k + 2);
                        batch.vertexBuffer[i].getIndices_ES20()[j + 5] = (short) (4 * k + 3);

                        k++;
                    }
                }

                rlglData.getState().vertexCounter = 0;
            }

            context.tracelog.TRACELOG(LOG_INFO, "RLGL: Internal vertex buffers initialized successfully in RAM (CPU)");
            //--------------------------------------------------------------------------------------------
            // Upload to GPU (VRAM) vertex data and initialize VAOs/VBOs
            //--------------------------------------------------------------------------------------------

            for(int i = 0; i < numBuffers; i++){
                if(rlglData.getExtSupported().isVao()){
                    // Initialize Quads VAO
                    batch.vertexBuffer[i].setVaoId(glGenVertexArrays());
                    glBindVertexArray(batch.vertexBuffer[i].getVaoId());
                }

                // Quads - Vertex buffers binding and attributes enable
                // Vertex position buffer (shader-location = 0)
                batch.vertexBuffer[i].vboId[0] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[0]);
                glBufferData(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vertices, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_POSITION.GetLocation()]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_POSITION.GetLocation()], 3, GL_FLOAT, false, 0, 0);

                // Vertex texcoord buffer (shader-location = 1)
                batch.vertexBuffer[i].vboId[1] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[1]);
                glBufferData(GL_ARRAY_BUFFER, batch.vertexBuffer[i].texcoords, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()],2, GL_FLOAT, false, 0, 0);

                // Vertex normal buffer (shader-location = 2)
                batch.vertexBuffer[i].vboId[2] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[2]);
                glBufferData(GL_ARRAY_BUFFER, batch.vertexBuffer[i].normals, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_NORMAL.GetLocation()]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_NORMAL.GetLocation()], 3, GL_FLOAT, false, 0, 0);

                // Vertex color buffer (shader-location = 3)
                ByteBuffer colours = ByteBuffer.allocateDirect(batch.vertexBuffer[i].colors.length);
                colours.put(batch.vertexBuffer[i].colors).flip();
                batch.vertexBuffer[i].vboId[3] = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[3]);
                glBufferData(GL_ARRAY_BUFFER, colours, GL_DYNAMIC_DRAW);
                glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);
                glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_COLOR.GetLocation()], 4, GL_UNSIGNED_BYTE, true, 0, 0);

                // Fill index buffer
                batch.vertexBuffer[i].vboId[4] = glGenBuffers();
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[4]);
                if(GRAPHICS_API_OPENGL_33){
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[i].getIndices_GL11(), GL_STATIC_DRAW);
                }
                else if(GRAPHICS_API_OPENGL_ES2){
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[i].getIndices_ES20(), GL_STATIC_DRAW);
                }
            }

            context.tracelog.TRACELOG(LOG_INFO, "RLGL: Render batch vertex buffers loaded successfully in VRAM (GPU)");

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
    public void UnloadRenderBatch(rlRenderBatch batch){
        // Unbind everything
        if (rlglData.getExtSupported().isVao()){
            glBindVertexArray(0);
        }
        glDisableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION);
        glDisableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD);
        glDisableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL);
        glDisableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Unload all vertex buffers data
        for (int i = 0; i < batch.bufferCount; i++){
            // Delete VBOs from GPU (VRAM)
            glDeleteBuffers(batch.vertexBuffer[i].vboId[0]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[1]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[2]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[3]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[4]);

            // Delete VAOs from GPU (VRAM)
            if (rlglData.getExtSupported().isVao()){
                glDeleteVertexArrays(batch.vertexBuffer[i].vaoId);
            }
        }

        // Unload arrays
        batch.setVertexBuffer(null);
        batch.setDraws(null);
    }

    // Draw render batch
    // NOTE: Batch is reset and current buffer is updated (for multi-buffer config)
    public void rlDrawRenderBatch(rlRenderBatch batch){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            // Update batch vertex buffers
            //------------------------------------------------------------------------------------------------------------
            // NOTE: If there is not vertex data, buffers doesn't need to be updated (vertexCount > 0)
            if (rlglData.getState().vertexCounter > 0) {
                // Activate elements VAO
                if (rlglData.getExtSupported().vao) {
                    glBindVertexArray(batch.vertexBuffer[batch.currentBuffer].vaoId);
                }

                // TODO: If no data changed on the CPU arrays there is no need to re-upload data to GPU,
                // a flag can be used to detect changes but it would imply keeping a copy buffer and memcmp() both, does it worth it?

                // Vertex positions buffer
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[0]);
                glBufferSubData(GL_ARRAY_BUFFER, 0, batch.vertexBuffer[batch.currentBuffer].vertices);
                //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*3*4*batch.vertexBuffer[batch.currentBuffer].elementCount, batch.vertexBuffer[batch.currentBuffer].vertices, GL_DYNAMIC_DRAW);  // Update all buffer

                // Texture coordinates buffer
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[1]);
                glBufferSubData(GL_ARRAY_BUFFER, 0, batch.vertexBuffer[batch.currentBuffer].texcoords);
                //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*2*4*batch.vertexBuffer[batch.currentBuffer].elementCount, batch.vertexBuffer[batch.currentBuffer].texcoords, GL_DYNAMIC_DRAW); // Update all buffer

                // Normals buffer
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[2]);
                glBufferSubData(GL_ARRAY_BUFFER, 0, batch.vertexBuffer[batch.currentBuffer].normals);
                //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*3*4*batch.vertexBuffer[batch.currentBuffer].elementCount, batch.vertexBuffer[batch.currentBuffer].normals, GL_DYNAMIC_DRAW); // Update all buffer

                // Colors buffer
                ByteBuffer colourBuffer = ByteBuffer.allocateDirect(batch.vertexBuffer[batch.currentBuffer].colors.length);
                colourBuffer.put(batch.vertexBuffer[batch.currentBuffer].colors).flip();
                glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[3]);
                glBufferSubData(GL_ARRAY_BUFFER, 0, colourBuffer);
                //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*4*4*batch.vertexBuffer[batch.currentBuffer].elementCount, batch.vertexBuffer[batch.currentBuffer].colors, GL_DYNAMIC_DRAW);    // Update all buffer

                // NOTE: glMapBuffer() causes sync issue
                // If GPU is working with this buffer, glMapBuffer() will wait(stall) until GPU to finish its job
                // To avoid waiting (idle), glBufferData() can bee called first with NULL pointer before glMapBuffer()
                // Doing that, the previous data in PBO will be discarded and glMapBuffer() returns a new
                // allocated pointer immediately even if GPU is still working with the previous data

                // Another option: map the buffer object into client's memory
                //batch.vertexBuffer[batch.currentBuffer].vertices = (float *)glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
                //if (batch.vertexBuffer[batch.currentBuffer].vertices)
                //{
                //    Update vertex data
                //}
                //glUnmapBuffer(GL_ARRAY_BUFFER);

                // Unbind the current VAO
                if (rlglData.getExtSupported().vao) {
                    glBindVertexArray(0);
                }
            }
            //------------------------------------------------------------------------------------------------------------

            // Draw batch vertex buffers (considering VR stereo if required)
            //------------------------------------------------------------------------------------------------------------
            Matrix matProjection = rlglData.getState().getProjection().clone();
            Matrix matModelView = rlglData.getState().getModelview().clone();

            int eyeCount = rlglData.getState().isStereoRender() ? 2 : 1;

            for (int eye = 0; eye < eyeCount; eye++) {
                if (eyeCount == 2) {
                    // Setup current eye viewport (half screen width)
                    rlViewport(eye*rlglData.getState().framebufferWidth/2, 0, rlglData.getState().framebufferWidth/2, rlglData.getState().framebufferHeight);

                    // Set current eye view offset to modelview matrix
                    rlSetMatrixModelview(MatrixMultiply(matModelView, rlglData.getState().viewOffsetStereo[eye]));
                    // Set current eye projection matrix
                    rlSetMatrixProjection(rlglData.getState().projectionStereo[eye]);
                }

                // Draw buffers
                if (rlglData.getState().vertexCounter > 0) {
                    // Set current shader and upload current MVP matrix
                    glUseProgram(rlglData.getState().currentShaderId);

                    // Create modelview-projection matrix and upload to shader
                    Matrix matMVP = MatrixMultiply(rlglData.getState().getModelview(), rlglData.getState().getProjection());
                    glUniformMatrix4fv(rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_MVP.GetLocation()], false, MatrixToFloat(matMVP));

                    if (rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()] != -1) {
                        glUniformMatrix4fv(rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()], false, MatrixToFloat(rlglData.getState().getProjection()));
                    }

                    // WARNING: For the following setup of the view, model, and normal matrices, it is expected that transformations and rendering occur between rlPushMatrix() and rlPopMatrix()

                    if (rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_VIEW.GetLocation()] != -1) {
                        glUniformMatrix4fv(rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_VIEW.GetLocation()], false, MatrixToFloat(rlglData.getState().getModelview()));
                    }

                    if (rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_MODEL.GetLocation()] != -1) {
                        glUniformMatrix4fv(rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_MODEL.GetLocation()], false, MatrixToFloat(rlglData.getState().getTransform()));
                    }

                    if (rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_NORMAL.GetLocation()] != -1) {
                        glUniformMatrix4fv(rlglData.getState().currentShaderLocs[SHADER_LOC_MATRIX_NORMAL.GetLocation()], false, MatrixToFloat(MatrixTranspose(MatrixInvert(rlglData.getState().getTransform()))));
                    }

                    if (rlglData.getExtSupported().isVao()) {
                        glBindVertexArray(batch.vertexBuffer[batch.currentBuffer].vaoId);
                    }
                    else {
                        // Bind vertex attrib: position (shader-location = 0)
                        glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[0]);
                        glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_POSITION.GetLocation()], 3, GL_FLOAT, false, 0, 0);
                        glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_POSITION.GetLocation()]);

                        // Bind vertex attrib: texcoord (shader-location = 1)
                        glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[1]);
                        glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()], 2, GL_FLOAT, false, 0, 0);
                        glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()]);

                        // Bind vertex attrib: normal (shader-location = 2)
                        glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[2]);
                        glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_NORMAL.GetLocation()], 3, GL_FLOAT, false, 0, 0);
                        glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_NORMAL.GetLocation()]);

                        // Bind vertex attrib: color (shader-location = 3)
                        glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[3]);
                        glVertexAttribPointer(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_COLOR.GetLocation()], 4, GL_UNSIGNED_BYTE, true, 0, 0);
                        glEnableVertexAttribArray(rlglData.getState().currentShaderLocs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);

                        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[4]);
                    }

                    // Setup some default shader values
                    glUniform4f(rlglData.getState().currentShaderLocs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()], 1.0f, 1.0f, 1.0f, 1.0f);
                    glUniform1i(rlglData.getState().currentShaderLocs[SHADER_LOC_MAP_DIFFUSE.GetLocation()], 0);  // Active default sampler2D: texture0

                    // Activate additional sampler textures
                    // Those additional textures will be common for all draw calls of the batch
                    for (int i = 0; i < RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS; i++) {
                        if (rlglData.getState().activeTextureId[i] > 0) {
                            glActiveTexture(GL_TEXTURE0 + 1 + i);
                            glBindTexture(GL_TEXTURE_2D, rlglData.getState().activeTextureId[i]);
                        }
                    }

                    // Activate default sampler2D texture0 (one texture is always active for default batch shader)
                    // NOTE: Batch system accumulates calls by texture0 changes, additional textures are enabled for all the draw calls
                    glActiveTexture(GL_TEXTURE0);

                    for (int i = 0, vertexOffset = 0; i < batch.drawCounter; i++) {
                        // Bind current draw call texture, activated as GL_TEXTURE0 and bound to sampler2D texture0 by default
                        glBindTexture(GL_TEXTURE_2D, batch.draws[i].textureId);

                        if ((batch.draws[i].mode == RL_LINES) || (batch.draws[i].mode == RL_TRIANGLES)) {
                            glDrawArrays(batch.draws[i].mode, vertexOffset, batch.draws[i].vertexCount);
                        }
                        else {
                            if (GRAPHICS_API_OPENGL_33) {
                                // The number of indices to be processed needs to be defined: elementCount*6
                                // NOTE: The final parameter tells the GPU the offset in bytes from the
                                // start of the index buffer to the location of the first index to process
                                glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_INT, 0);
                            }
                            if (GRAPHICS_API_OPENGL_ES2) {
                                glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_SHORT, 0);
                            }
                        }

                        vertexOffset += (batch.draws[i].vertexCount + batch.draws[i].vertexAlignment);
                    }

                    if (!rlglData.getExtSupported().vao) {
                        glBindBuffer(GL_ARRAY_BUFFER, 0);
                        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                    }

                    glBindTexture(GL_TEXTURE_2D, 0);    // Unbind textures
                }

                if (rlglData.getExtSupported().vao) {
                    glBindVertexArray(0); // Unbind VAO
                }

                glUseProgram(0);    // Unbind shader program
            }

            // Restore viewport to default measures
            if (eyeCount == 2) {
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

            // Reset RLGL.currentBatch.draws array
            for (int i = 0; i < RL_DEFAULT_BATCH_DRAWCALLS; i++) {
                batch.draws[i].mode = RL_QUADS;
                batch.draws[i].vertexCount = 0;
                batch.draws[i].textureId = rlglData.getState().defaultTextureId;
            }

            // Reset active texture units for next batch
            Arrays.fill(rlglData.getState().activeTextureId, 0);

            // Reset draws counter to one draw for the batch
            batch.drawCounter = 1;
            //------------------------------------------------------------------------------------------------------------

            // Change to next buffer in the list (in case of multi-buffering)
            batch.currentBuffer++;
            if (batch.currentBuffer >= batch.bufferCount) {
                batch.currentBuffer = 0;
            }
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

    // Check internal buffer overflow for a given number of vertex and force a rlRenderBatch draw call if required
    public boolean rlCheckRenderBatchLimit(int vCount){
        boolean overflow = false;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if((rlglData.getState().vertexCounter + vCount) >= (rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].elementCount * 4)) {
                 int currentMode = rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode;
                 int currentTexture = rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId;

                 overflow = true;
                 rlDrawRenderBatch(rlglData.getCurrentBatch());

                // Restore state of last batch so new vertices can be added
                rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode = currentMode;
                rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId = currentTexture;
            }
        }

        return overflow;
    }


    // Convert image data to OpenGL texture (returns OpenGL valid Id)
    public int rlLoadTexture(byte[] data, int width, int height, rlPixelFormat format, int mipmapCount){
        int id = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return id;
        }

        glBindTexture(GL_TEXTURE_2D, 0);    // Free any old binding

        // Check texture format support by OpenGL 1.1 (compressed textures not supported)
        if (GRAPHICS_API_OPENGL_11){
            if (format.GetFormat() >= PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()){
                // TODO: Support texture data decompression
                context.tracelog.TRACELOG(LOG_WARNING, "GL: OpenGL 1.1 does not support GPU compressed texture formats");
                return id;
            }
        }
        else{
            if (
                    (!rlglData.getExtSupported().isTexCompDXT()) && ((format == PIXELFORMAT_COMPRESSED_DXT1_RGB) ||
                    (format == PIXELFORMAT_COMPRESSED_DXT1_RGBA) || (format == PIXELFORMAT_COMPRESSED_DXT3_RGBA) ||
                    (format == PIXELFORMAT_COMPRESSED_DXT5_RGBA))
            ){
                context.tracelog.TRACELOG(LOG_WARNING, "GL: DXT compressed texture format not supported");
                return id;
            }
            if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
                if ((!rlglData.getExtSupported().isTexCompETC1()) && (format == PIXELFORMAT_COMPRESSED_ETC1_RGB)){
                    context.tracelog.TRACELOG(LOG_WARNING, "GL: ETC1 compressed texture format not supported");
                    return id;
                }

                if ((!rlglData.getExtSupported().isTexCompETC2()) && ((format == PIXELFORMAT_COMPRESSED_ETC2_RGB) ||
                        (format == PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA))){
                    context.tracelog.TRACELOG(LOG_WARNING, "GL: ETC2 compressed texture format not supported");
                    return id;
                }

                if ((!rlglData.getExtSupported().isTexCompPVRT()) && ((format == PIXELFORMAT_COMPRESSED_PVRT_RGB) ||
                        (format == PIXELFORMAT_COMPRESSED_PVRT_RGBA))){
                    context.tracelog.TRACELOG(LOG_WARNING, "GL: PVRT compressed texture format not supported");
                    return id;
                }

                if ((!rlglData.getExtSupported().isTexCompASTC()) && ((format == PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA) ||
                        (format == PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA))){
                    context.tracelog.TRACELOG(LOG_WARNING, "GL: ASTC compressed texture format not supported");
                    return id;
                }
            }
        }      // GRAPHICS_API_OPENGL_11

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        id = glGenTextures(); // Generate texture id

        glBindTexture(GL_TEXTURE_2D, id);

        int mipWidth = width;
        int mipHeight = height;
        int mipOffset = 0;          // Mipmap data offset

        // Load the different mipmap levels
        for (int i = 0; i < mipmapCount; i++) {
            int mipSize = rlGetPixelDataSize(mipWidth, mipHeight, format);

            //using globals here to get around pointers
            rlGetGlTextureFormats(format);

            context.tracelog.TRACELOG(LOG_DEBUG, "TEXTURE: Load mipmap level " + i + " (" + mipWidth + " x " + mipHeight + "), size: " + mipSize + ", offset: " + mipOffset);

            if (glInternalFormat != 0) {
                byte[] buffer = new byte[mipSize];
                System.arraycopy(data, mipOffset, buffer, 0, mipSize);
                ByteBuffer dataBuffer = ByteBuffer.allocateDirect(mipSize);
                dataBuffer.put(buffer);
                dataBuffer.flip();

                if (format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()){
                    // Todo: HDR breaks here
                    glTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, glFormat, glType, dataBuffer);
                }
                else{
                    if (!GRAPHICS_API_OPENGL_11){
                        glCompressedTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, dataBuffer);
                    }
                }

                int[] swizzleMask = new int[4];
                if (GRAPHICS_API_OPENGL_33){
                    if (format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE){
                        swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ONE};
                        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                    }
                    else if (format == PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA){
                        if (GRAPHICS_API_OPENGL_21){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ALPHA};
                        }
                        else {
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_GREEN};
                        }
                        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                    }
                }
            }

            mipWidth /= 2;
            mipHeight /= 2;
            mipOffset += mipSize; // Increment offset position to next mipmap

            // Security check for NPOT textures
            if (mipWidth < 1){
                mipWidth = 1;
            }
            if (mipHeight < 1){
                mipHeight = 1;
            }
        }


        // Texture parameters configuration
        // NOTE: glTexParameteri does NOT affect texture uploading
        if (GRAPHICS_API_OPENGL_ES2) {
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
                // Activate trilinear filtering if mipmaps are available
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            }
            // Define the maximum number of mipmap levels to be used, 0 is base texture size
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmapCount - 1);

            // Check if the loaded texture with mipmaps is complete,
            // uncomplete textures will draw in black if mipmap filtering is required
            //GLint complete = 0;
            //glGetTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_IMMUTABLE_FORMAT, &complete);
        }

        // At this point texture is loaded in GPU and texture parameters configured

        // NOTE: If mipmaps were not in data, they are not generated automatically

        // Unbind current texture
        glBindTexture(GL_TEXTURE_2D, 0);

        if (id > 0){
            context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + id + "] Texture loaded successfully (" + width + "x" + height +
                    " | " + rlGetPixelFormatName(format) + " | " + mipmapCount + " mipmaps)");
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: Failed to load texture");
        }

        return id;
    }

    // Load depth texture/renderbuffer (to be attached to fbo)
    // WARNING: OpenGL ES 2.0 requires GL_OES_depth_texture and WebGL requires WEBGL_depth_texture extensions
    public int rlLoadTextureDepth(int width, int height, boolean useRenderBuffer){
        int id = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return id;
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // In case depth textures not supported, were force renderbuffer usage
            if (!rlglData.getExtSupported().isTexDepth()){
                useRenderBuffer = true;
            }

            // NOTE: Letting the implementation to choose the best bit-depth
            // Possible formats: GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT32 and GL_DEPTH_COMPONENT32F
            int glInternalFormat = GL_DEPTH_COMPONENT;

            if (GRAPHICS_API_OPENGL_ES2) {
                if(!rlglData.getExtSupported().texDepthWebGL) {
                    if (rlglData.getExtSupported().getMaxDepthBits() == 32) {
                        glInternalFormat = GL_DEPTH_COMPONENT32_OES;
                    }
                    else if (rlglData.getExtSupported().getMaxDepthBits() == 24) {
                        glInternalFormat = GL_DEPTH_COMPONENT24_OES;
                    }
                    else {
                        glInternalFormat = GL_DEPTH_COMPONENT16;
                    }
                }
            }

            if (GRAPHICS_API_OPENGL_ES3) {
                // NOTE: This sized internal format should also work for WebGL 2.0
                // WARNING: Specification only allows GL_DEPTH_COMPONENT32F for GL_FLOAT type
                // REF: https://registry.khronos.org/OpenGL-Refpages/es3.0/html/glTexImage2D.xhtml
                if (rlglData.getExtSupported().maxDepthBits == 24) {
                    glInternalFormat = GL_DEPTH_COMPONENT24;
                }
                else {
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

                context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: Depth texture loaded successfully");
            }
            else{
                // Create the renderbuffer that will serve as the depth attachment for the framebuffer
                // NOTE: A renderbuffer is simpler than a texture and could offer better performance on embedded devices
                id = glGenRenderbuffers();
                glBindRenderbuffer(GL_RENDERBUFFER, id);
                glRenderbufferStorage(GL_RENDERBUFFER, glInternalFormat, width, height);

                glBindRenderbuffer(GL_RENDERBUFFER, 0);
                int logTmp = rlglData.getExtSupported().getMaxDepthBits() >= 24 ? rlglData.getExtSupported().getMaxDepthBits() : 16;
                context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + id + "] Depth renderbuffer loaded successfully (" + logTmp + " bits)");

            }
        }
        else if (GRAPHICS_API_OPENGL_SOFTWARE) {
            // NOTE: Renderbuffers are the same type of object as textures in rlsw
            // WARNING: Ensure that the depth format is the one specified at rlsw compilation
            id = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, id);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, width, height);
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
        }

        return id;
    }

    // Load texture cubemap
    // NOTE: Cubemap data is expected to be 6 images in a single data array (one after the other),
    // expected the following convention: +X, -X, +Y, -Y, +Z, -Z
    public int rlLoadTextureCubemap(byte[] data, int size, rlPixelFormat format, int mipmapCount) {
        int id = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return id;
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            int mipSize = size;

            int dataSize = rlGetPixelDataSize(size, size, format);

            id = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, id);

            rlGetGlTextureFormats(format);

            if (glInternalFormat != 0) {
                // Load cubemap faces/mipmaps
                for (int i = 0; i < 6 * mipmapCount; i++) {
                    int mipmapLevel = i / 6;
                    int face = i % 6;

                    if (data == null){
                        if (format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()){
                            if (
                                    (format == PIXELFORMAT_UNCOMPRESSED_R32) ||
                                    (format == PIXELFORMAT_UNCOMPRESSED_R32G32B32A32) ||
                                    (format == PIXELFORMAT_UNCOMPRESSED_R16) ||
                                    (format == PIXELFORMAT_UNCOMPRESSED_R16G16B16A16)
                            ) {
                                context.tracelog.TRACELOG(LOG_WARNING, "TEXTURES: Cubemap requested format not supported");
                            }
                            else {
                                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, mipmapLevel, glInternalFormat, mipSize, mipSize, 0, glFormat, glType, (ByteBuffer) null);
                            }

                        }
                        else{
                            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURES: Empty cubemap creation does not support compressed format");
                        }
                    }
                    else{
                        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
                        dataBuffer.put(data);

                        if (format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat()) {
                            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, mipmapLevel, glInternalFormat, mipSize, mipSize, 0, glFormat, glType, dataBuffer);
                        }
                        else{
                            glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, mipSize, mipSize, 0, dataBuffer);
                        }
                    }
                    int[] swizzleMask = new int[16];

                    if (GRAPHICS_API_OPENGL_33){
                        if (format == PIXELFORMAT_UNCOMPRESSED_GRAYSCALE){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ONE};
                            glTexParameteriv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                        }
                        else if (format == PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA){
                            if (GRAPHICS_API_OPENGL_21){
                                swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ALPHA};
                            }
                            else if (GRAPHICS_API_OPENGL_33){
                                swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_GREEN};
                            }
                            glTexParameteriv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                        }
                    }

                    if (face == 5) {
                        mipSize /= 2;
                        // Security check for NPOT textures
                        if (mipSize < 1) {
                            mipSize = 1;
                        }

                        dataSize = rlGetPixelDataSize(mipSize, mipSize, format);
                    }
                }
            }

            // Set cubemap texture sampling parameters
            if (mipmapCount > 1) {
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            }
            else {
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            }

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
            context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + id + "] Cubemap texture created successfully (" + size + "x" + size + ")");
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: Failed to load cubemap texture");
        }

        return id;
    }

    // Update already loaded texture in GPU with new data
    // WARNING: Not possible to know safely if internal texture format is the expected one...
    public void rlUpdateTexture(int id, int offsetX, int offsetY, int width, int height, rlPixelFormat format, byte[] data){
        glBindTexture(GL_TEXTURE_2D, id);

        rlGetGlTextureFormats(format);

        if ((glInternalFormat != 0) && (format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat())){
            ByteBuffer bb = ByteBuffer.allocateDirect(data.length);
            bb.put(data);
            bb.flip();
            glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, width, height, glFormat, glType, bb);
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: [ID " + id + "] Failed to update for current texture format (" + format + ")");
        }
    }

    // Get OpenGL internal formats and data type from raylib rlPixelFormat
    private void rlGetGlTextureFormats(rlPixelFormat format){
        glInternalFormat = -1;
        glFormat = -1;
        glType = -1;

        switch (format){
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE:
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
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA:
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
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5:
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
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8:
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
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1:
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
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4:
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
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8:
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
            case PIXELFORMAT_UNCOMPRESSED_R32:
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
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32:
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
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32:
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
            case PIXELFORMAT_UNCOMPRESSED_R16:
                if (rlglData.getExtSupported().isTexFloat16()){
                    glInternalFormat = GL_R16F;
                }
                glFormat = GL_RED;
                glType = GL_HALF_FLOAT;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R16G16B16:
                if (rlglData.getExtSupported().isTexFloat16()){
                    glInternalFormat = GL_RGB16F;
                }
                glFormat = GL_RGB;
                glType = GL_HALF_FLOAT;
                break;
            case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16:
                if (rlglData.getExtSupported().isTexFloat16()){
                    glInternalFormat = GL_RGBA16F;
                }
                glFormat = GL_RGBA;
                glType = GL_HALF_FLOAT;
                break;
            case PIXELFORMAT_COMPRESSED_DXT1_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                    }
                }
                break;
            case PIXELFORMAT_COMPRESSED_DXT1_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
                    }
                }
                break;
            case PIXELFORMAT_COMPRESSED_DXT3_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                    }
                }
                break;
            case PIXELFORMAT_COMPRESSED_DXT5_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                    }
                }
                break;
            case PIXELFORMAT_COMPRESSED_ETC1_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompETC1()){
                        glInternalFormat = GL_ETC1_RGB8_OES;
                    }
                }  // NOTE: Requires OpenGL ES 2.0 or OpenGL 4.3
                break;
            case PIXELFORMAT_COMPRESSED_ETC2_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompETC2()){
                        glInternalFormat = GL_COMPRESSED_RGB8_ETC2;
                    }
                }      // NOTE: Requires OpenGL ES 3.0 or OpenGL 4.3
                break;
            case PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompETC2()){
                        glInternalFormat = GL_COMPRESSED_RGBA8_ETC2_EAC;
                    }
                }    // NOTE: Requires OpenGL ES 3.0 or OpenGL 4.3
                break;
            case PIXELFORMAT_COMPRESSED_PVRT_RGB:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompPVRT()){
                        glInternalFormat = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
                    }
                } // NOTE: Requires PowerVR GPU
                break;
            case PIXELFORMAT_COMPRESSED_PVRT_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompPVRT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
                    }
                } // NOTE: Requires PowerVR GPU
                break;
            case PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA:
                if (!GRAPHICS_API_OPENGL_11){
                    if (rlglData.getExtSupported().isTexCompASTC()){
                        glInternalFormat = GL_COMPRESSED_RGBA_ASTC_4x4_KHR;
                    }
                } // NOTE: Requires OpenGL ES 3.1 or OpenGL 4.3
                break;
            case PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA:
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
                    context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: Current format not supported (" + format + ")");
                }
                break;
        }
    }

    public void rlUnloadTexture(int id){
        glDeleteTextures(id);
    }

    // Generate mipmap data for selected texture
    public void rlGenTextureMipmaps(Texture2D texture) {
        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return;
        }

        glBindTexture(GL_TEXTURE_2D, texture.getId());

        // Check if texture is power-of-two (POT)
        boolean texIsPOT = ((texture.getWidth() > 0) && ((texture.getWidth() & (texture.getWidth() - 1)) == 0)) &&
                ((texture.getHeight() > 0) && ((texture.getHeight() & (texture.getHeight() - 1)) == 0));

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if ((texIsPOT) || (rlglData.getExtSupported().isTexNPOT())){
                //glHint(GL_GENERATE_MIPMAP_HINT, GL_DONT_CARE);   // Hint for mipmaps generation algorithm: GL_FASTEST, GL_NICEST, GL_DONT_CARE
                glGenerateMipmap(GL_TEXTURE_2D);    // Generate mipmaps automatically

                texture.setMipmaps(1 + (int) Math.floor(Math.log(Math.max(texture.getWidth(), texture.getHeight())) / Math.log(2)));
                context.tracelog.TRACELOG(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Mipmaps generated automatically, total: " + texture.getMipmaps());
            }
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: [ID " + texture.getId() + "] Failed to generate mipmaps");
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Read texture pixel data
    public byte[] rlReadTexturePixels(int id, int width, int height, rlPixelFormat format){
        byte[] pixels = null;

        if (GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            glBindTexture(GL_TEXTURE_2D, id);

            // NOTE: Using texture id, some texture info can be retrieved (but not on OpenGL ES 2.0)
            // Possible texture info: GL_TEXTURE_RED_SIZE, GL_TEXTURE_GREEN_SIZE, GL_TEXTURE_BLUE_SIZE, GL_TEXTURE_ALPHA_SIZE
            //int width, height, format;
            //glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH, &width);
            //glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT, &height);
            //glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT, &format);

            // NOTE: Each row written to or read from by OpenGL pixel operations like glGetTexImage are aligned to a 4 byte boundary by default, which may add some padding
            // Use glPixelStorei to modify padding with the GL_[UN]PACK_ALIGNMENT setting
            // GL_PACK_ALIGNMENT affects operations that read from OpenGL memory (glReadPixels, glGetTexImage, etc.)
            // GL_UNPACK_ALIGNMENT affects operations that write to OpenGL memory (glTexImage, etc.)
            glPixelStorei(GL_PACK_ALIGNMENT, 1);

            rlGetGlTextureFormats(format);
            int size = rlGetPixelDataSize(width, height, format);

            if ((glInternalFormat != 0) && (format.GetFormat() < PIXELFORMAT_COMPRESSED_DXT1_RGB.GetFormat())){
                pixels = new byte[size];
                ByteBuffer bb = ByteBuffer.allocateDirect(pixels.length);
                bb.put(pixels).flip();
                glGetTexImage(GL_TEXTURE_2D, 0, glFormat, glType, bb);
                for (int i = 0; i < pixels.length; i++){
                    pixels[i] = bb.get(i);
                }
            }
            else{
                context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: [ID " + id + "] Data retrieval not suported for " +
                        "pixel format (" + format + ")");
            }

            glBindTexture(GL_TEXTURE_2D, 0);
        }

        if (GRAPHICS_API_OPENGL_ES2){
            // glGetTexImage() is not available on OpenGL ES 2.0
            // Texture width and height are required on OpenGL ES 2.0, there is no way to get it from texture id
            // Two possible Options:
            // 1 - Bind texture to color fbo attachment and glReadPixels()
            // 2 - Create an fbo, activate it, render quad with texture, glReadPixels()
            // Using Option 1, care for texture format on retrieval
            // NOTE: This behaviour could be conditioned by graphic driver...
            int fboId = rlLoadFramebuffer();

            glBindFramebuffer(GL_FRAMEBUFFER, fboId);
            glBindTexture(GL_TEXTURE_2D, 0);

            // Attach our texture to FBO
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, id, 0);

            // Reading data as RGBA because FBO texture is configured as RGBA, despite binding another texture format
            ByteBuffer pixelsBuffers = ByteBuffer.allocateDirect(rlGetPixelDataSize(width, height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8));
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixelsBuffers);


            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            // Clean up temporal fbo
            rlUnloadFramebuffer(fboId);
        }

        return pixels;
    }

    // Copy framebuffer pixel data to internal buffer
    void rlCopyFramebuffer(int x, int y, int width, int height, rlPixelFormat format, byte[] pixels) {
        if (GRAPHICS_API_OPENGL_SOFTWARE){
            rlGetGlTextureFormats(format); // Get OpenGL texture format
            // swReadPixels(x, y, width, height, glFormat, glType, pixels);
        }
    }

    // Resize internal framebuffer
    void rlResizeFramebuffer(int width, int height) {
        if (GRAPHICS_API_OPENGL_SOFTWARE){
            // swResize(width, height);
        }
    }

    // Read screen pixel data (color buffer)
    public byte[] rlReadScreenPixels(int width, int height){
         ByteBuffer imgDataBuffer = ByteBuffer.allocateDirect(width * height * 4);
         byte[] imgData = new byte[width * height * 4];

        // NOTE: glReadPixels() returns image flipped vertically -> (0,0) is the bottom left corner of the framebuffer
        // WARNING: Getting alpha channel! Be careful, it can be transparent if not cleared properly!
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, imgDataBuffer);

        // Flip image vertically
        // NOTE: Alpha value has already been applied to RGB in framebuffer, not needed anymore
        for (int y = height - 1; y >= height/2; y--) {
            for (int x = 0; x < (width*4); x += 4) {
                int s = ((height - 1) - y)*width*4 + x;
                int e = y * width * 4 + x;

                byte r = imgDataBuffer.get(s);
                byte g = imgDataBuffer.get(s+1);
                byte b = imgDataBuffer.get(s+2);

                imgData[s] = imgDataBuffer.get(e);
                imgData[s+1] = imgDataBuffer.get(e+1);
                imgData[s+2] = imgDataBuffer.get(e+2);
                imgData[s+3] = (byte) 255; // Set alpha component value to 255 (no trasparent image retrieval)

                imgData[e] = r;
                imgData[e+1] = g;
                imgData[e+2] = b;
                imgData[e+3] = (byte) 255; // Ditto
            }
        }

        return imgData;     // NOTE: image data should be freed
    }

    // Framebuffer management (fbo)
    //-----------------------------------------------------------------------------------------
    // Load a framebuffer to be used for rendering
    // NOTE: No textures attached

    // Load a framebuffer to be used for rendering
    // NOTE: No textures attached
    public int rlLoadFramebuffer(){
        int fboId = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return fboId;
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            fboId = glGenFramebuffers();       // Create the framebuffer object
            glBindFramebuffer(GL_FRAMEBUFFER, 0);   // Unbind any framebuffer
        }

        return fboId;
    }

    // Attach color buffer texture to a framebuffer object (unloads previous attachment)
    // NOTE: Attach type: 0-Color, 1-Depth renderbuffer, 2-Depth texture
    public void rlFramebufferAttach(int id, int texId, rlFramebufferAttachType attachType, rlFramebufferAttachTextureType texType){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            glBindFramebuffer(GL_FRAMEBUFFER, id);

            switch (attachType){
                case ATTACHMENT_COLOR_CHANNEL0:
                case ATTACHMENT_COLOR_CHANNEL1:
                case ATTACHMENT_COLOR_CHANNEL2:
                case ATTACHMENT_COLOR_CHANNEL3:
                case ATTACHMENT_COLOR_CHANNEL4:
                case ATTACHMENT_COLOR_CHANNEL5:
                case ATTACHMENT_COLOR_CHANNEL6:
                case ATTACHMENT_COLOR_CHANNEL7:{
                    if (texType == ATTACHMENT_TEXTURE2D){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType.GetType(), GL_TEXTURE_2D, texId, 0);
                    }
                    else if (texType == ATTACHMENT_RENDERBUFFER){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType.GetType(), GL_RENDERBUFFER, texId);
                    }
                    else if (texType.GetType() >= ATTACHMENT_CUBEMAP_POSITIVE_X.GetType()){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType.GetType(), GL_TEXTURE_CUBE_MAP_POSITIVE_X + texType.GetType(), texId, 0);
                    }

                }
                break;
                case ATTACHMENT_DEPTH:{
                    if (texType == ATTACHMENT_TEXTURE2D){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texId, 0);
                    }
                    else if (texType == ATTACHMENT_RENDERBUFFER){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, texId);
                    }

                }
                break;
                case ATTACHMENT_STENCIL:{
                    if (texType == ATTACHMENT_TEXTURE2D){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_TEXTURE_2D, texId, 0);
                    }
                    else if (texType == ATTACHMENT_RENDERBUFFER){
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
    public boolean rlFramebufferComplete(int id){
        boolean result = false;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            glBindFramebuffer(GL_FRAMEBUFFER, id);

            int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

            if (status != GL_FRAMEBUFFER_COMPLETE){
                switch (status){
                    case GL_FRAMEBUFFER_UNSUPPORTED:
                        context.tracelog.TRACELOG(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer is unsupported");
                        break;
                    case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                        context.tracelog.TRACELOG(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has incomplete attachment");
                        break;

                    case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                        if (GRAPHICS_API_OPENGL_ES2){
                            context.tracelog.TRACELOG(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has incomplete dimensions");
                        }
                        break;
                    case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                        context.tracelog.TRACELOG(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has a missing attachment");
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
    public void rlUnloadFramebuffer(int id){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_SOFTWARE){
            // Query depth attachment to automatically delete texture/renderbuffer
            int depthType;
            glBindFramebuffer(GL_FRAMEBUFFER, id);   // Bind framebuffer to query depth texture type
            depthType = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            // TODO: Review warning retrieving object name in WebGL
            // WARNING: WebGL: INVALID_ENUM: getFramebufferAttachmentParameter: invalid parameter name
            // https://registry.khronos.org/webgl/specs/latest/1.0/
            int depthId = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);

            if (depthType == GL_RENDERBUFFER){
                glDeleteRenderbuffers(depthId);
            }
            else if (depthType == GL_TEXTURE) {
                glDeleteTextures(depthId);
            }

            // NOTE: If a texture object is deleted while its image is attached to the *currently bound* framebuffer,
            // the texture image is automatically detached from the currently bound framebuffer

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(id);

            context.tracelog.TRACELOG(LOG_INFO, "FBO: [ID " + id + "] Unloaded framebuffer from VRAM (GPU)");
        }
    }

    // Vertex data management
    //-----------------------------------------------------------------------------------------

    // Load a new attributes buffer
    public int rlLoadVertexBuffer(float[] buffer, boolean dynamic){
        int id = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return id;
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferData(GL_ARRAY_BUFFER, buffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    public int rlLoadVertexBuffer(byte[] buffer, boolean dynamic){
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
    public int rlLoadVertexBufferElement(float[] buffer, boolean dynamic){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    // Load a new attributes element buffer
    public int rlLoadVertexBufferElement(short[] buffer, boolean dynamic){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            id = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
        }

        return id;
    }

    // Enable vertex buffer (VBO)
    public void rlEnableVertexBuffer(int id) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ARRAY_BUFFER, id);
        }
    }

    // Disable vertex buffer (VBO)
    public void rlDisableVertexBuffer() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    // Enable vertex buffer element (VBO Element)
    public void rlEnableVertexBufferElement(int id) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
        }
    }

    // Disable vertex buffer element (VBO Element)
    public void rlDisableVertexBufferElement() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    // Update vertex buffer with new data
    // NOTE: dataSize and offset must be provided in bytes
    public void rlUpdateVertexBuffer(int id, byte[] data, int offset) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
            dataBuffer.put(data).flip();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferSubData(GL_ARRAY_BUFFER, offset, dataBuffer);
        }
    }

    // Update vertex buffer with new data
    // NOTE: dataSize and offset must be provided in bytes
    public void rlUpdateVertexBuffer(int id, float[] data, int offset) {
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
    public boolean rlEnableVertexArray(int vaoId) {
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
    public void rlDisableVertexArray() {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getExtSupported().isVao()){
                glBindVertexArray(0);
            }
        }
    }

    // Enable vertex attribute index
    public void rlEnableVertexAttribute(int index) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glEnableVertexAttribArray(index);
        }
    }

    // Disable vertex attribute index
    public void rlDisableVertexAttribute(int index) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDisableVertexAttribArray(index);
        }
    }

    // Draw vertex array
    public void rlDrawVertexArray(int offset, int count) {
        glDrawArrays(GL_TRIANGLES, offset, count);
    }

    // Draw vetex array elements
    public void rlDrawVertexArrayElements(int offset, int count, byte[] buffer) {
        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, buffer.length + offset);
    }

    // Draw vetex array elements
    public void rlDrawVertexArrayElements(int offset, int count, float[] buffer) {
        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, buffer.length + offset);
    }

    // Draw vertex array instanced
    public void rlDrawVertexArrayInstanced(int offset, int count, int instances) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDrawArraysInstanced(GL_TRIANGLES, offset, count, instances);
        }
    }

    // Draw vertex array elements instanced
    public void rlDrawVertexArrayElementsInstanced(int offset, int count, int[] buffer, int instances) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDrawElementsInstanced(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, buffer.length + offset, instances);
        }
    }

    // Enable vertex state pointer
    public void rlEnableStatePointer(int vertexAttribType, byte[] buffer){
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
    public void rlEnableStatePointer(int vertexAttribType, float[] buffer){
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
    public void rlDisableStatePointer(int vertexAttribType){
        if(GRAPHICS_API_OPENGL_11){
            glDisableClientState(vertexAttribType);
        }
    }

    // Load vertex array object (VAO)
    public int rlLoadVertexArray() {
        int vaoId = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return vaoId;
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            vaoId = glGenVertexArrays();
        }

        return vaoId;
    }

    // Set vertex attribute
    public void rlSetVertexAttribute(int index, int compSize, int type, boolean normalized, int stride, int offset) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){    // NOTE: Data type could be: GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, GL_INT, GL_UNSIGNED_INT
            // Additional types (depends on OpenGL version or extensions):
            //  - GL_HALF_FLOAT, GL_FLOAT, GL_DOUBLE, GL_FIXED,
            //  - GL_INT_2_10_10_10_REV, GL_UNSIGNED_INT_2_10_10_10_REV, GL_UNSIGNED_INT_10F_11F_11F_REV

            long offsetNative = offset;
            glVertexAttribPointer(index, compSize, type, normalized, stride, offsetNative);
        }
    }

    // Set vertex attribute divisor
    public void rlSetVertexAttributeDivisor(int index, int divisor) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glVertexAttribDivisor(index, divisor);
        }
    }

    // Unload vertex array object (VAO)
    public void rlUnloadVertexArray(int vaoId) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if (rlglData.getExtSupported().isVao())
            {
                glBindVertexArray(0);
                glDeleteVertexArrays(vaoId);
                context.tracelog.TRACELOG(LOG_INFO, "VAO: [ID " + vaoId + "] Unloaded vertex array data from VRAM (GPU)");
            }
        }
    }

    // Unload vertex buffer (VBO)
    public void rlUnloadVertexBuffer(int vboId) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDeleteBuffers(vboId);
            //logger(LOG_INFO, "VBO: Unloaded vertex data from VRAM (GPU)");
        }
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Shaders Functions
    //----------------------------------------------------------------------------------

    // Load (compile) shader and return shader id
    public int rlLoadShader(String code, int type) {
        int shaderId = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            shaderId = glCreateShader(type);
            glShaderSource(shaderId, code);

            glCompileShader(shaderId);
            int success = glGetShaderi(shaderId, GL_COMPILE_STATUS);

            if (success == GL_FALSE) {
                switch (type) {
                    case GL_VERTEX_SHADER:
                        context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Failed to compile vertex shader code", shaderId);
                        break;
                    case GL_FRAGMENT_SHADER:
                        context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Failed to compile fragment shader code", shaderId);
                        break;
                    //case GL_GEOMETRY_SHADER:
                    case GL_COMPUTE_SHADER:
                        if(GRAPHICS_API_OPENGL_43) {
                            context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Failed to compile compute shader code", shaderId);
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "SHADER: Compute shaders not enabled. Define GRAPHICS_API_OPENGL_43", shaderId);
                        }
                        break;
                    default:
                        break;
                }

                int maxLength = glGetShaderi(shaderId, GL_INFO_LOG_LENGTH);

                if (maxLength > 0) {
                    String log = glGetShaderInfoLog(shaderId, maxLength);
                    context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Compile error: %s", shaderId, log);
                }

                // Unload object allocated by glCreateShader(),
                // despite failing in the compilation process
                glDeleteShader(shaderId);
                shaderId = 0;
            }
            else {
                switch (type) {
                    case GL_VERTEX_SHADER:
                        context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID %d] Vertex shader compiled successfully", shaderId);
                        break;
                    case GL_FRAGMENT_SHADER:
                        context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID %d] Fragment shader compiled successfully", shaderId);
                        break;
                    //case GL_GEOMETRY_SHADER:
                    case GL_COMPUTE_SHADER:
                        if (GRAPHICS_API_OPENGL_43) {
                            context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID %d] Compute shader compiled successfully", shaderId);
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "SHADER: Compute shaders not enabled. Define GRAPHICS_API_OPENGL_43", shaderId);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return shaderId;
    }

    // Load shader program from code strings
    // NOTE: If shader string is NULL, using default vertex/fragment shaders
    public int rlLoadShaderProgram(String vsCode, String fsCode) {
        int id = 0; // Shader program id
        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return id;
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            int vertexShaderId = 0;
            int fragmentShaderId = 0;

            // Compile vertex shader (if provided)
            // NOTE: If not vertex shader is provided, use default one
            if (vsCode != null) {
                vertexShaderId = rlLoadShader(vsCode, GL_VERTEX_SHADER);
            }
            else {
                vertexShaderId = rlglData.getState().defaultVShaderId;
            }

            // Compile fragment shader (if provided)
            // NOTE: If not vertex shader is provided, use default one
            if (fsCode != null) {
                fragmentShaderId = rlLoadShader(fsCode, GL_FRAGMENT_SHADER);
            }
            else {
                fragmentShaderId = rlglData.getState().defaultFShaderId;
            }

            // In case vertex and fragment shader are the default ones, no need to recompile, assign the default shader program id
            if ((vertexShaderId == rlglData.getState().defaultVShaderId) && (fragmentShaderId == rlglData.getState().defaultFShaderId)) {
                id = rlglData.getState().defaultShaderId;
            }
            else if ((vertexShaderId > 0) && (fragmentShaderId > 0)) {
                // One of or both shader are new, a new shader program needs to be compiled
                id = rlLoadShaderProgramEx(vertexShaderId, fragmentShaderId);

                // Detaching and deleting vertex/fragment shaders (if not default ones)
                // WARNING: Detach shader before deletion to make sure memory is freed
                if (vertexShaderId != rlglData.getState().defaultVShaderId) {
                    // WARNING: Shader program linkage could fail and returned id is 0
                    if (id > 0) glDetachShader(id, vertexShaderId);
                    glDeleteShader(vertexShaderId);
                }
                if (fragmentShaderId != rlglData.getState().defaultFShaderId) {
                    // WARNING: Shader program linkage could fail and returned id is 0
                    if (id > 0) glDetachShader(id, fragmentShaderId);
                    glDeleteShader(fragmentShaderId);
                }

                // In case shader program loading failed, assign default shader
                if (id == 0) {
                    // In case shader loading fails, reassigning default shader
                    context.tracelog.TRACELOG(LOG_WARNING, "SHADER: Failed to load custom shader code, using default shader");
                    id = rlglData.getState().defaultShaderId;
                }
                /*
                else
                {
                    // Get available shader uniforms
                    // NOTE: This information is useful for debug...
                    int uniformCount = -1;
                    glGetProgramiv(id, GL_ACTIVE_UNIFORMS, &uniformCount);

                    for (int i = 0; i < uniformCount; i++)
                    {
                        int namelen = -1;
                        int num = -1;
                        char name[256] = { 0 };     // Assume no variable names longer than 256
                        GLenum type = GL_ZERO;

                        // Get the name of the uniforms
                        glGetActiveUniform(id, i, sizeof(name) - 1, &namelen, &num, &type, name);

                        name[namelen] = 0;
                        TRACELOG(RL_LOG_DEBUG, "SHADER: [ID %d] Active uniform (%s) set at location: %d", id, name, glGetUniformLocation(id, name));
                    }
                }
                */
            }
        }

        return id;
    }

    // Load shader program from already loaded shader ids
    public int rlLoadShaderProgramEx(int vsId, int fsId) {
        int programId = 0;

        if (!isGpuReady) {
            context.tracelog.TRACELOG(LOG_WARNING, "GL: GPU is not ready to load data, trying to load before InitWindow()?");
            return programId;
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            int success = 0;
            programId = glCreateProgram();

            glAttachShader(programId, vsId);
            glAttachShader(programId, fsId);

            // Default attribute shader locations must be bound before linking
            // NOTE: There is no problem with binding a generic attribute index to an attribute variable name
            // that is never used; if some attrib name is no found on the shader, it locations becomes -1
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION, RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL, RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR, RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT, RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2);

            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES, RL_DEFAULT_SHADER_ATTRIB_NAME_BONEINDICES);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS, RL_DEFAULT_SHADER_ATTRIB_NAME_BONEWEIGHTS);
            glBindAttribLocation(programId, RL_DEFAULT_SHADER_ATTRIB_LOCATION_INSTANCETRANSFORM, RL_DEFAULT_SHADER_ATTRIB_NAME_INSTANCETRANSFORM);

            glLinkProgram(programId);

            // NOTE: All uniform variables are initialized to 0 when a program links

            success = glGetProgrami(programId, GL_LINK_STATUS);

            if (success == GL_FALSE) {
                context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Failed to link shader program", programId);

                int maxLength = glGetProgrami(programId, GL_INFO_LOG_LENGTH);

                if (maxLength > 0) {
                    String log = glGetProgramInfoLog(programId, maxLength);
                    context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Link error: %s", programId, log);
                }

                glDeleteProgram(programId);

                programId = 0;
            }
            else {
                // Get the size of compiled shader program (not available on OpenGL ES 2.0)
                // NOTE: If GL_LINK_STATUS is GL_FALSE, program binary length is zero
                //GLint binarySize = 0;
                //glGetProgramiv(id, GL_PROGRAM_BINARY_LENGTH, &binarySize);

                context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID %d] Program shader loaded successfully", programId);
            }
        }
        return programId;
    }

    // Load compute shader program
    public int rlLoadShaderProgramCompute(int csId) {
        int programId = 0;

        if (GRAPHICS_API_OPENGL_43) {
            int success = 0;
            programId = glCreateProgram();

            glAttachShader(programId, csId);

            glLinkProgram(programId);

            // NOTE: All uniform variables are intitialised to 0 when a program links
            
            success = glGetProgrami(programId, GL_LINK_STATUS);

            if (success == GL_FALSE) {
                context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Failed to link compute shader program", programId);

                IntBuffer maxLengthBuffer = IntBuffer.allocate(1);
                glGetProgramiv(programId, GL_INFO_LOG_LENGTH, maxLengthBuffer);
                int maxLength = maxLengthBuffer.get();

                if (maxLength > 0) {
                    String log = glGetProgramInfoLog(programId, maxLength);
                    context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID %d] Link error: %s", programId, log);
                }

                glDeleteProgram(programId);

                programId = 0;
            }
            else {
                // Get the size of compiled shader program (not available on OpenGL ES 2.0)
                // NOTE: If GL_LINK_STATUS is GL_FALSE, program binary length is zero
                //GLint binarySize = 0;
                //glGetProgramiv(id, GL_PROGRAM_BINARY_LENGTH, &binarySize);

                context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID %d] Compute shader program loaded successfully", programId);
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "SHADER: Compute shaders not supported, enable GRAPHICS_API_OPENGL_43");
        }

        return programId;
    }

    public void rlUnloadShader(int id) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDeleteShader(id);

            context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID " + id + "] Unloaded shader data from VRAM (GPU)");
        }
    }

    // Unload shader program
    public void rlUnloadShaderProgram(int id) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glDeleteProgram(id);

            context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID " + id + "] Unloaded shader program data from VRAM (GPU)");
        }
    }

    // Get shader location uniform
    // NOTE: First parameter refers to shader program id
    public int rlGetLocationUniform(int id, String uniformName) {
        int location = -1;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            location = glGetUniformLocation(id, uniformName);

            if (location == -1) {
                // context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID " + id + "] Failed to find shader uniform: " + uniformName);
            }
            else {
                // context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID " + id + "] Shader uniform (" + uniformName + ") set at location: " + location);
            }
        }
        return location;
    }

    // Get shader attribute location
    // NOTE: First parameter refers to shader program id
    public int rlGetLocationAttrib(int id, String attribName){
        int location = -1;
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            location = glGetAttribLocation(id, attribName);

            if (location == -1){
                // context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID " + id + "] Failed to find shader attribute: " + attribName);
            }
            else{
                // context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID " + id + "] Shader attribute (" + attribName + ") set at " + "location: " + location);
            }
        }
        return location;
    }

    // Set shader value uniform
    public void rlSetUniform(int locIndex, float[] value, rlShaderUniformDataType uniformType) {
        ByteBuffer buffer;

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            switch (uniformType){
                case SHADER_UNIFORM_FLOAT:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putFloat(v);
                    }
                    buffer.flip();
                    glUniform1fv(locIndex, buffer.asFloatBuffer());
                    break;
                case SHADER_UNIFORM_VEC2:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putFloat(v);
                    }
                    buffer.flip();
                    glUniform2fv(locIndex, buffer.asFloatBuffer());
                    break;
                case SHADER_UNIFORM_VEC3:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putFloat(v);
                    }
                    buffer.flip();
                    glUniform3fv(locIndex, buffer.asFloatBuffer());
                    break;
                case SHADER_UNIFORM_VEC4:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putFloat(v);
                    }
                    buffer.flip();
                    glUniform4fv(locIndex, buffer.asFloatBuffer());
                    break;
                case SHADER_UNIFORM_INT, SHADER_UNIFORM_SAMPLER2D:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putInt((int) v);
                    }
                    buffer.flip();
                    glUniform1iv(locIndex, buffer.asIntBuffer());
                    break;
                case SHADER_UNIFORM_IVEC2:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putInt((int) v);
                    }
                    buffer.flip();
                    glUniform2iv(locIndex, buffer.asIntBuffer());
                    break;
                case SHADER_UNIFORM_IVEC3:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putInt((int) v);
                    }
                    buffer.flip();
                    glUniform3iv(locIndex, buffer.asIntBuffer());
                    break;
                case SHADER_UNIFORM_IVEC4:
                    buffer = ByteBuffer.allocateDirect(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                    for (float v : value) {
                        buffer.putInt((int) v);
                    }
                    buffer.flip();
                    glUniform4iv(locIndex, buffer.asIntBuffer());
                    break;
                default:
                    context.tracelog.TRACELOG(LOG_WARNING, "SHADER: Failed to set uniform value, data type not recognized");
            }
        }
    }

    // Set shader value attribute
    public void rlSetVertexAttributeDefault(int locIndex, float[] value, rlShaderAttributeDataType attribType, int count) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){

            switch (attribType){
                case SHADER_ATTRIB_FLOAT:
                    if (count == 1) glVertexAttrib1fv(locIndex, value);
                    break;
                case SHADER_ATTRIB_VEC2:
                    if (count == 2) glVertexAttrib2fv(locIndex, value);
                    break;
                case SHADER_ATTRIB_VEC3:
                    if (count == 3) glVertexAttrib3fv(locIndex, value);
                    break;
                case SHADER_ATTRIB_VEC4:
                    if (count == 4) glVertexAttrib4fv(locIndex, value);
                    break;
                default:
                    context.tracelog.TRACELOG(LOG_WARNING, "SHADER: Failed to set attrib default value, data type not recognized");
            }
        }
    }

    // Set shader value uniform matrix
    public void rlSetUniformMatrix(int locIndex, Matrix mat) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUniformMatrix4fv(locIndex, false, MatrixToFloat(mat));
        }
    }

    // Set shader value uniform matrix
    public void rlSetUniformMatrices(int locIndex, Matrix[] matrices) {
        FloatBuffer matBuffer = FloatBuffer.allocate(matrices.length * 16);
        for (Matrix matrix : matrices) {
            matBuffer.put(MatrixToFloat(matrix));
        }

        if (GRAPHICS_API_OPENGL_33) {
            glUniformMatrix4fv(locIndex, true, matBuffer);
        }
        else if (GRAPHICS_API_OPENGL_ES2) {
            // WARNING: WebGL does not support Matrix transpose ("true" parameter)
            // REF: https://developer.mozilla.org/en-US/docs/Web/API/WebGLRenderingContext/uniformMatrix
            glUniformMatrix4fv(locIndex, false, matBuffer);
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

    // Dispatch compute shader (equivalent to *draw* for graphics pilepine)
    public void rlComputeShaderDispatch(int groupX, int groupY, int groupZ) {
        if (GRAPHICS_API_OPENGL_43){
            glDispatchCompute(groupX, groupY, groupZ);
        }
    }

    // Load shader storage buffer object (SSBO)
    public int rlLoadShaderBuffer(byte[] data, int usageHint) {
        int ssbo = 0;

        if (GRAPHICS_API_OPENGL_43){
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
            dataBuffer.put(data).flip();

            ssbo = glGenBuffers();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
            glBufferData(GL_SHADER_STORAGE_BUFFER, dataBuffer, usageHint == 1 ? usageHint : RL_STREAM_COPY);
            if (data == null) {
                glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R8UI, GL_RED_INTEGER, GL_UNSIGNED_BYTE, (ByteBuffer) null);
            }
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "SSBO: SSBO not enabled. Define GRAPHICS_API_OPENGL_43");
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
    public int rlGetShaderBufferSize(int id) {
        int result = 0;

        if(GRAPHICS_API_OPENGL_43) {
            int size = 0;
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
            size = glGetBufferParameteri(GL_SHADER_STORAGE_BUFFER, GL_BUFFER_SIZE);
            if (size > 0) {
                result = size;
            }
        }

        return result;
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
    public void rlBindImageTexture(int id, int index, rlPixelFormat format, boolean readonly) {
        if(GRAPHICS_API_OPENGL_43) {
            rlGetGlTextureFormats(format);
            glBindImageTexture(index, id, 0, false, 0, readonly ? GL_READ_ONLY : GL_READ_WRITE, glInternalFormat);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: Image texture binding not enabled. Define GRAPHICS_API_OPENGL_43");
        }
    }

    // Matrix state management
    //-----------------------------------------------------------------------------------------

    // Get internal modelview matrix
    public Matrix rlGetMatrixModelview() {
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
            matrix = rlglData.getState().getModelview();
        }
        return matrix;
    }

    // Get internal projection matrix
    public Matrix rlGetMatrixProjection() {
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
            return rlglData.getState().getProjection();
        }
    }
    // Get internal accumulated transform matrix
    public Matrix rlGetMatrixTransform() {
        Matrix mat = MatrixIdentity();

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // TODO: Consider possible transform matrices in the RLGL.State.stack
            // Matrix matStackTransform = rlMatrixIdentity();
            // for (int i = RLGL.State.stackCounter; i > 0; i--) matStackTransform = MatrixMultiply(RLGL.State.stack[i], matStackTransform);

            mat = rlglData.getState().getTransform();
        }

        return mat;
    }

    // Get internal projection matrix for stereo render (selected eye)
    public Matrix rlGetMatrixProjectionStereo(int eye) {
        Matrix mat = MatrixIdentity();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            mat = rlglData.getState().getProjectionStereo()[eye];
        }
        return mat;
    }

    // Get internal view offset matrix for stereo render (selected eye)
    public Matrix rlGetMatrixViewOffsetStereo(int eye) {
        Matrix mat = MatrixIdentity();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            mat = rlglData.getState().getViewOffsetStereo()[eye];
        }
        return mat;
    }

    // Set a custom modelview matrix (replaces internal modelview matrix)
    public void rlSetMatrixModelview(Matrix view){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setModelview(view);
        }
    }

    // Set a custom projection matrix (replaces internal projection matrix)
    public void rlSetMatrixProjection(Matrix projection){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setProjection(projection);
        }
    }

    // Set eyes projection matrices for stereo rendering
    public void rlSetMatrixProjectionStereo(Matrix right, Matrix left){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().getProjectionStereo()[0] = right;
            rlglData.getState().getProjectionStereo()[1] = left;
        }
    }

    // Set eyes view offsets matrices for stereo rendering
    public void rlSetMatrixViewOffsetStereo(Matrix right, Matrix left){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().getViewOffsetStereo()[0] = right;
            rlglData.getState().getViewOffsetStereo()[1] = left;
        }
    }

    // Renders a quad in NDC
    public void rlLoadDrawQuad() {
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
            glEnableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION);
            glVertexAttribPointer(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION, 3, GL_FLOAT, false, 5 * Float.BYTES, 0); //Positions
            glEnableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD);
            glVertexAttribPointer(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES); //Texcoords

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
    public void rlLoadDrawCube(){
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
            glEnableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION);
            glVertexAttribPointer(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION, 3, GL_FLOAT, false, 8 * Float.BYTES, 0); //Positions
            glEnableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL);
            glVertexAttribPointer(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES); // Normals
            glEnableVertexAttribArray(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD);
            glVertexAttribPointer(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD, 2, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES); // Texcoords
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
    public String rlGetPixelFormatName(rlPixelFormat format){
        switch (format) {
            case PIXELFORMAT_UNCOMPRESSED_GRAYSCALE: return "GRAYSCALE";          // 8 bit per pixel (no alpha)
            case PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA: return "GRAY_ALPHA";        // 8*2 bpp (2 channels)
            case PIXELFORMAT_UNCOMPRESSED_R5G6B5: return "R5G6B5";                // 16 bpp
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8: return "R8G8B8";                // 24 bpp
            case PIXELFORMAT_UNCOMPRESSED_R5G5B5A1: return "R5G5B5A1";            // 16 bpp (1 bit alpha)
            case PIXELFORMAT_UNCOMPRESSED_R4G4B4A4: return "R4G4B4A4";            // 16 bpp (4 bit alpha)
            case PIXELFORMAT_UNCOMPRESSED_R8G8B8A8: return "R8G8B8A8";            // 32 bpp
            case PIXELFORMAT_UNCOMPRESSED_R32: return "R32";                      // 32 bpp (1 channel - float)
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32: return "R32G32B32";          // 32*3 bpp (3 channels - float)
            case PIXELFORMAT_UNCOMPRESSED_R32G32B32A32: return "R32G32B32A32";    // 32*4 bpp (4 channels - float)
            case PIXELFORMAT_UNCOMPRESSED_R16: return "R16";                      // 16 bpp (1 channel - half float)
            case PIXELFORMAT_UNCOMPRESSED_R16G16B16: return "R16G16B16";          // 16*3 bpp (3 channels - half float)
            case PIXELFORMAT_UNCOMPRESSED_R16G16B16A16: return "R16G16B16A16";    // 16*4 bpp (4 channels - half float)
            case PIXELFORMAT_COMPRESSED_DXT1_RGB: return "DXT1_RGB";              // 4 bpp (no alpha)
            case PIXELFORMAT_COMPRESSED_DXT1_RGBA: return "DXT1_RGBA";            // 4 bpp (1 bit alpha)
            case PIXELFORMAT_COMPRESSED_DXT3_RGBA: return "DXT3_RGBA";            // 8 bpp
            case PIXELFORMAT_COMPRESSED_DXT5_RGBA: return "DXT5_RGBA";            // 8 bpp
            case PIXELFORMAT_COMPRESSED_ETC1_RGB: return "ETC1_RGB";              // 4 bpp
            case PIXELFORMAT_COMPRESSED_ETC2_RGB: return "ETC2_RGB";              // 4 bpp
            case PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA: return "ETC2_RGBA";        // 8 bpp
            case PIXELFORMAT_COMPRESSED_PVRT_RGB: return "PVRT_RGB";              // 4 bpp
            case PIXELFORMAT_COMPRESSED_PVRT_RGBA: return "PVRT_RGBA";            // 4 bpp
            case PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA: return "ASTC_4x4_RGBA";    // 8 bpp
            case PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA: return "ASTC_8x8_RGBA";    // 2 bpp
            default: return "UNKNOWN";
        }
    }

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    // Load default shader (just vertex positioning and texture coloring)
    // NOTE: This shader program is used for internal buffers
    // NOTE: Loaded: rlglData.state.defaultShaderId, rlglData.state.defaultShaderLocs
    public void rlLoadShaderDefault(){
        rlglData.getState().setDefaultShaderLocs(new int[RL_MAX_SHADER_LOCATIONS]);

        // NOTE: All locations must be reset to -1 (no location)
        Arrays.fill(rlglData.getState().defaultShaderLocs, -1);

        // Vertex shader directly defined, no external file required
        StringBuilder defaultVShaderCode = new StringBuilder();
        if (GRAPHICS_API_OPENGL_21) {
            defaultVShaderCode.append("#version 120                       \n");
            defaultVShaderCode.append("attribute vec3 vertexPosition;     \n");
            defaultVShaderCode.append("attribute vec2 vertexTexCoord;     \n");
            defaultVShaderCode.append("attribute vec4 vertexColor;        \n");
            defaultVShaderCode.append("varying vec2 fragTexCoord;         \n");
            defaultVShaderCode.append("varying vec4 fragColor;            \n");
        }
        else if (GRAPHICS_API_OPENGL_33) {
            defaultVShaderCode.append("#version 330                       \n");
            defaultVShaderCode.append("in vec3 vertexPosition;            \n");
            defaultVShaderCode.append("in vec2 vertexTexCoord;            \n");
            defaultVShaderCode.append("in vec4 vertexColor;               \n");
            defaultVShaderCode.append("out vec2 fragTexCoord;             \n");
            defaultVShaderCode.append("out vec4 fragColor;                \n");
        }
        if (GRAPHICS_API_OPENGL_ES3) {
            defaultVShaderCode.append("#version 300 es                    \n");
            defaultVShaderCode.append("precision mediump float            \n"); // Precision required for OpenGL ES3 (WebGL 2) (on some browsers)
            defaultVShaderCode.append("attribute vec3 vertexPosition;     \n");
            defaultVShaderCode.append("attribute vec2 vertexTexCoord;     \n");
            defaultVShaderCode.append("attribute vec4 vertexColor;        \n");
            defaultVShaderCode.append("varying vec2 fragTexCoord;         \n");
            defaultVShaderCode.append("varying vec4 fragColor;            \n");
        }
        else if (GRAPHICS_API_OPENGL_ES2) {
            defaultVShaderCode.append("#version 100                       \n");
            defaultVShaderCode.append("precision mediump float            \n"); // Precision required for OpenGL ES2 (WebGL) (on some browsers)
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
        if (GRAPHICS_API_OPENGL_ES3) {
            defaultFShaderCode.append("#version 300 es                       \n");
            defaultFShaderCode.append("precision mediump float;           \n");     // Precision required for OpenGL ES3 (WebGL 2)
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
        else if (GRAPHICS_API_OPENGL_ES2){
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
        rlglData.getState().setDefaultVShaderId(rlLoadShader(String.valueOf(defaultVShaderCode), GL_VERTEX_SHADER));     // Compile default vertex shader
        rlglData.getState().setDefaultFShaderId(rlLoadShader(String.valueOf(defaultFShaderCode), GL_FRAGMENT_SHADER));   // Compile default fragment shader

        rlglData.getState().defaultShaderId = rlLoadShaderProgramEx(rlglData.getState().defaultVShaderId, rlglData.getState().defaultFShaderId);

        if (rlglData.getState().getDefaultShaderId() > 0){
            context.tracelog.TRACELOG(LOG_INFO, "SHADER: [ID " + rlglData.getState().defaultShaderId + "] Default shader loaded successfully");

            // Set default shader locations: attributes locations
            rlglData.getState().defaultShaderLocs[SHADER_LOC_VERTEX_POSITION.GetLocation()] = glGetAttribLocation(rlglData.getState().defaultShaderId, RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION);
            rlglData.getState().defaultShaderLocs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()] = glGetAttribLocation(rlglData.getState().defaultShaderId, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
            rlglData.getState().defaultShaderLocs[SHADER_LOC_VERTEX_COLOR.GetLocation()] = glGetAttribLocation(rlglData.getState().defaultShaderId, RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR);

            // Set default shader locations: uniform locations
            rlglData.getState().defaultShaderLocs[SHADER_LOC_MATRIX_MVP.GetLocation()]  = glGetUniformLocation(rlglData.getState().defaultShaderId, RL_DEFAULT_SHADER_UNIFORM_NAME_MVP);
            rlglData.getState().defaultShaderLocs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()] = glGetUniformLocation(rlglData.getState().defaultShaderId, RL_DEFAULT_SHADER_UNIFORM_NAME_COLOR);
            rlglData.getState().defaultShaderLocs[SHADER_LOC_MAP_DIFFUSE.GetLocation()] = glGetUniformLocation(rlglData.getState().defaultShaderId, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE0);
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "SHADER: [ID " + rlglData.getState().getDefaultShaderId() + "] Failed to load default shader");
        }
    }

    // Unload default shader
    // NOTE: Unloads: rlglData.state.defaultShaderId, rlglData.state.defaultShaderLocs
    public void rlUnloadShaderDefault(){
        glUseProgram(0);

        glDetachShader(rlglData.getState().getDefaultShaderId(), rlglData.getState().getDefaultFShaderId());
        glDetachShader(rlglData.getState().getDefaultShaderId(), rlglData.getState().getDefaultVShaderId());
        glDeleteShader(rlglData.getState().getDefaultFShaderId());
        glDeleteShader(rlglData.getState().getDefaultVShaderId());

        glDeleteProgram(rlglData.getState().getDefaultShaderId());

        rlglData.getState().setDefaultShaderLocs(null);
    }

    // Get compressed format official GL identifier name
    public String rlGetCompressedFormatName(int format){
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
    public int rlGenTextureMipmapsData(byte[] data, int baseWidth, int baseHeight){
        int mipmapCount = 1;                // Required mipmap levels count (including base level)
        if(GRAPHICS_API_OPENGL_11){
        int width = baseWidth;
        int height = baseHeight;
        int size = baseWidth * baseHeight * 4;  // Size in bytes (will include mipmaps...), RGBA only

        // Count mipmap levels required
        while ((width != 1) && (height != 1)){
            width /= 2;
            height /= 2;

            context.tracelog.TRACELOG(null, "TEXTURE: Next mipmap size: " + width + " x " + height);

            mipmapCount++;

            size += (width * height * 4);       // Add mipmap size (in bytes)
        }

        context.tracelog.TRACELOG(null, "TEXTURE: Total mipmaps required: " + mipmapCount);
        context.tracelog.TRACELOG(null, "TEXTURE: Total size of data required: " + size);

        byte[] temp = new byte[data.length];

        if (temp != null){
            data = temp;
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "TEXTURE: Failed to re-allocate required mipmaps memory");
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

        context.tracelog.TRACELOG(null, "TEXTURE: Mipmap base size (" + width + "x" + height + ")");

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
    public byte[] rlGenNextMipmapData(byte[] srcData, int srcWidth, int srcHeight) {
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

        context.tracelog.TRACELOG(null, "TEXTURE: Mipmap generated successfully (" + width + "x" + height + ")");

        return mipmap;
    }

    public int rlGetPixelDataSize(int width, int height, rlPixelFormat format){
        int dataSize = 0;       // Size in bytes
        int bpp = 0;            // Bits per pixel
        int blockWidth, blockHeight;

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
                // 8 bytes per each 4x4 block
                blockWidth = (width + 3)/4;
                blockHeight = (height + 3)/4;
                dataSize = blockWidth*blockHeight*8;
                break;
            case PIXELFORMAT_COMPRESSED_DXT3_RGBA:
            case PIXELFORMAT_COMPRESSED_DXT5_RGBA:
            case PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA:
            case PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA:
                // 16 bytes per each 4x4 block
                blockWidth = (width + 3)/4;
                blockHeight = (height + 3)/4;
                dataSize = blockWidth * blockHeight * 16;
                break;
            case PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA:
                blockWidth = (width + 3)/4;
                blockHeight = (height + 3)/4;
                dataSize = blockWidth*blockHeight*8;
                break;
            default:
                break;
        }

        // Compute dataSize for uncompressed texture data (no blocks)
        if ((format.GetFormat() >= PIXELFORMAT_UNCOMPRESSED_GRAYSCALE.GetFormat()) && (format.GetFormat() <= PIXELFORMAT_UNCOMPRESSED_R16G16B16A16.GetFormat())) {
            double bytesPerPixel = (double)bpp/8.0;
            dataSize = (int)(bytesPerPixel*width*height); // Total data size in bytes
        }

        if (dataSize == 0) {
            context.tracelog.TRACELOG(LOG_WARNING, "Requested image size is larger than 2GB, it can not be allocated");
        }

        return dataSize;
    }
}