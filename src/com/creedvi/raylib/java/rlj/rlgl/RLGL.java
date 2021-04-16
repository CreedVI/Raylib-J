package com.creedvi.raylib.java.rlj.rlgl;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Matrix;
import com.creedvi.raylib.java.rlj.rlgl.data.rlglData;
import com.creedvi.raylib.java.rlj.rlgl.shader.Shader;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Texture2D;
import com.creedvi.raylib.java.rlj.utils.Files;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static com.creedvi.raylib.java.rlj.Config.*;
import static com.creedvi.raylib.java.rlj.raymath.RayMath.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.BlendMode.BLEND_ALPHA;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.FramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL0;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.FramebufferAttachType.RL_ATTACHMENT_DEPTH;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.FramebufferTexType.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.GlVersion.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.ShaderLocationIndex.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogS;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.opengl.EXTDebugMarker.glInsertEventMarkerEXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_SWIZZLE_RGBA;
import static org.lwjgl.opengl.GL41.GL_RGB565;
import static org.lwjgl.opengles.GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
import static org.lwjgl.opengles.OESDepth24.GL_DEPTH_COMPONENT24_OES;
import static org.lwjgl.opengles.OESDepth32.GL_DEPTH_COMPONENT32_OES;

public class RLGL{

    //*********************
    //GL API VERSION
    //*********************
    private static boolean GRAPHICS_API_OPENGL_11 = false;
    private static boolean GRAPHICS_API_OPENGL_21 = false;
    private static boolean GRAPHICS_API_OPENGL_33 = true;
    private static final boolean GRAPHICS_API_OPENGL_ES2 = false;
    private static final boolean SUPPORT_RENDER_TEXTURES_HINT = true;

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
    public static final int RL_TEXTURE_ANISOTROPIC_FILTER = 0x3000;      // Anisotropic filter (custom identifier)

    public static final int RL_TEXTURE_FILTER_NEAREST = 0x2600;     // GL_NEAREST
    public static final int RL_TEXTURE_FILTER_LINEAR = 0x2601;     // GL_LINEAR
    public static final int RL_TEXTURE_FILTER_MIP_NEAREST = 0x2700;     // GL_NEAREST_MIPMAP_NEAREST
    public static final int RL_TEXTURE_FILTER_NEAREST_MIP_LINEAR = 0x2702;     // GL_NEAREST_MIPMAP_LINEAR
    public static final int RL_TEXTURE_FILTER_LINEAR_MIP_NEAREST = 0x2701;     // GL_LINEAR_MIPMAP_NEAREST
    public static final int RL_TEXTURE_FILTER_MIP_LINEAR = 0x2703;      // GL_LINEAR_MIPMAP_LINEAR

    public static final int RL_TEXTURE_WRAP_REPEAT = 0x2901;      // GL_REPEAT
    public static final int RL_TEXTURE_WRAP_CLAMP = 0x812F;      // GL_CLAMP_TO_EDGE
    public static final int RL_TEXTURE_WRAP_MIRROR_REPEAT = 0x8370;      // GL_MIRRORED_REPEAT
    public static final int RL_TEXTURE_WRAP_MIRROR_CLAMP = 0x8742;      // GL_MIRROR_CLAMP_EXT

    // Matrix modes (equivalent to OpenGL)=
    public static final int RL_MODELVIEW = 0x1700;      // GL_MODELVIEW
    public static final int RL_PROJECTION = 0x1701;      // GL_PROJECTION
    public static final int RL_TEXTURE = 0x1702;      // GL_TEXTURE

    // Primitive assembly draw modes
    public static final int RL_LINES = 0x0001;      // GL_LINES
    public static final int RL_TRIANGLES = 0x0004;      // GL_TRIANGLES
    public static final int RL_QUADS = 0x0007;      // GL_QUADS

    static int glInternalFormat = 0, glFormat = 0, glType = 0;

    static rlglData rlglData;

    /**
     * Version of OpenGL being used by Raylib-J
     */
    public enum GlVersion{
        NOGL(0),
        OPENGL_11(1),
        OPENGL_21(2),
        OPENGL_33(3),
        OPENGL_ES_20(4);

        int glType;

        GlVersion(int i){
            glType = i;
        }

        public int getGlType(){
            return glType;
        }
    }

    public enum FramebufferAttachType{
        RL_ATTACHMENT_COLOR_CHANNEL0(0),
        RL_ATTACHMENT_COLOR_CHANNEL1(1),
        RL_ATTACHMENT_COLOR_CHANNEL2(2),
        RL_ATTACHMENT_COLOR_CHANNEL3(3),
        RL_ATTACHMENT_COLOR_CHANNEL4(4),
        RL_ATTACHMENT_COLOR_CHANNEL5(5),
        RL_ATTACHMENT_COLOR_CHANNEL6(6),
        RL_ATTACHMENT_COLOR_CHANNEL7(7),
        RL_ATTACHMENT_DEPTH(100),
        RL_ATTACHMENT_STENCIL(200),
        ;
        private final int rlAttachInt;

        FramebufferAttachType(int i){
            rlAttachInt = i;
        }

        final int getRlAttachInt(){
            return rlAttachInt;
        }

        public static FramebufferAttachType getByInt(int index){
            for(FramebufferAttachType p : values()){
                if(p.rlAttachInt == index){
                    return p;
                }
            }
            return RL_ATTACHMENT_COLOR_CHANNEL0;
        }
    }

    public enum FramebufferTexType{
        RL_ATTACHMENT_CUBEMAP_POSITIVE_X(0),
        RL_ATTACHMENT_CUBEMAP_NEGATIVE_X(1),
        RL_ATTACHMENT_CUBEMAP_POSITIVE_Y(2),
        RL_ATTACHMENT_CUBEMAP_NEGATIVE_Y(3),
        RL_ATTACHMENT_CUBEMAP_POSITIVE_Z(4),
        RL_ATTACHMENT_CUBEMAP_NEGATIVE_Z(5),
        RL_ATTACHMENT_TEXTURE2D(100),
        RL_ATTACHMENT_RENDERBUFFER(200),
        ;

        private final int texInt;

        FramebufferTexType(int i){
            texInt = i;
        }

        int getTexInt(){
            return texInt;
        }
    }

    /**
     * Texture formats (support depends on OpenGL version)
     */
    public enum PixelFormat{
        PLACEHOLDER(0),
        UNCOMPRESSED_GRAYSCALE(1),     // 8 bit per pixel (no alpha)
        UNCOMPRESSED_GRAY_ALPHA(2),
        UNCOMPRESSED_R5G6B5(3),            // 16 bpp
        UNCOMPRESSED_R8G8B8(4),            // 24 bpp
        UNCOMPRESSED_R5G5B5A1(5),          // 16 bpp (1 bit alpha)
        UNCOMPRESSED_R4G4B4A4(6),         // 16 bpp (4 bit alpha)
        UNCOMPRESSED_R8G8B8A8(7),          // 32 bpp
        UNCOMPRESSED_R32(8),               // 32 bpp (1 channel - float)
        UNCOMPRESSED_R32G32B32(9),         // 32*3 bpp (3 channels - float)
        UNCOMPRESSED_R32G32B32A32(10),      // 32*4 bpp (4 channels - float)
        COMPRESSED_DXT1_RGB(11),            // 4 bpp (no alpha)
        COMPRESSED_DXT1_RGBA(12),           // 4 bpp (1 bit alpha)
        COMPRESSED_DXT3_RGBA(13),           // 8 bpp
        COMPRESSED_DXT5_RGBA(14),           // 8 bpp
        COMPRESSED_ETC1_RGB(15),            // 4 bpp
        COMPRESSED_ETC2_RGB(16),            // 4 bpp
        COMPRESSED_ETC2_EAC_RGBA(17),       // 8 bpp
        COMPRESSED_PVRT_RGB(18),            // 4 bpp
        COMPRESSED_PVRT_RGBA(19),           // 4 bpp
        COMPRESSED_ASTC_4x4_RGBA(21),       // 8 bpp
        COMPRESSED_ASTC_8x8_RGBA(22)        // 2 bpp
        ;

        private final int pixForInt;

        PixelFormat(int i){
            pixForInt = i;
        }

        public final int getPixForInt(){
            return pixForInt;
        }

        public static PixelFormat getByInt(int index){
            for(PixelFormat p : values()){
                if(p.pixForInt == index){
                    return p;
                }
            }
            return PLACEHOLDER;
        }
    }

    // Texture parameters: filter mode
    // NOTE 1: Filtering considers mipmaps if available in the texture
    // NOTE 2: Filter is accordingly set for minification and magnification
    public enum TextureFilterMode{
        FILTER_POINT(0),               // No filter, just pixel aproximation
        FILTER_BILINEAR(1),                // Linear filtering
        FILTER_TRILINEAR(2),               // Trilinear filtering (linear with mipmaps)
        FILTER_ANISOTROPIC_4X(3),          // Anisotropic filtering 4x
        FILTER_ANISOTROPIC_8X(4),          // Anisotropic filtering 8x
        FILTER_ANISOTROPIC_16X(5),
        ;         // Anisotropic filtering 16x

        private final int textureFilterInt;

        TextureFilterMode(int i){
            textureFilterInt = i;
        }

        public int getTextureFilterInt(){
            return textureFilterInt;
        }
    }

    // Color blending modes (pre-defined)
    public enum BlendMode{
        BLEND_ALPHA(0),                // Blend textures considering alpha (default)
        BLEND_ADDITIVE(1),                 // Blend textures adding colors
        BLEND_MULTIPLIED(2),               // Blend textures multiplying colors
        BLEND_ADD_COLORS(3),               // Blend textures adding colors (alternative)
        BLEND_SUBTRACT_COLORS(4),          // Blend textures subtracting colors (alternative)
        BLEND_CUSTOM(5)                    // Belnd textures using custom src/dst factors (use SetBlendModeCustom())
        ;

        private final int blendInt;

        BlendMode(int i){
            blendInt = i;
        }

        int getBlendInt(){
            return blendInt;
        }

        public static BlendMode getByInt(int index){
            for(BlendMode p : values()){
                if(p.blendInt == index){
                    return p;
                }
            }
            return BLEND_ALPHA;
        }
    }

    // Shader location point type
    public enum ShaderLocationIndex{
        LOC_VERTEX_POSITION(0),
        LOC_VERTEX_TEXCOORD01(1),
        LOC_VERTEX_TEXCOORD02(2),
        LOC_VERTEX_NORMAL(3),
        LOC_VERTEX_TANGENT(4),
        LOC_VERTEX_COLOR(5),
        LOC_MATRIX_MVP(6),
        LOC_MATRIX_MODEL(7),
        LOC_MATRIX_VIEW(8),
        LOC_MATRIX_PROJECTION(9),
        LOC_VECTOR_VIEW(10),
        LOC_COLOR_DIFFUSE(11),
        LOC_COLOR_SPECULAR(12),
        LOC_COLOR_AMBIENT(13),
        LOC_MAP_ALBEDO(14),          // LOC_MAP_DIFFUSE
        LOC_MAP_DIFFUSE(14),
        LOC_MAP_METALNESS(15),       // LOC_MAP_SPECULAR
        LOC_MAP_SPECULAR(15),
        LOC_MAP_NORMAL(16),
        LOC_MAP_ROUGHNESS(17),
        LOC_MAP_OCCLUSION(18),
        LOC_MAP_EMISSION(19),
        LOC_MAP_HEIGHT(20),
        LOC_MAP_CUBEMAP(21),
        LOC_MAP_IRRADIANCE(22),
        LOC_MAP_PREFILTER(23),
        LOC_MAP_BRDF(24);


        int ShaderLocationInt;

        ShaderLocationIndex(int i){
            ShaderLocationInt = i;
        }

        public int getShaderLocationInt(){
            return ShaderLocationInt;
        }
    }

    // Shader uniform data types
    public enum ShaderUniformDataType{
        UNIFORM_FLOAT(0),
        UNIFORM_VEC2(1),
        UNIFORM_VEC3(2),
        UNIFORM_VEC4(3),
        UNIFORM_INT(4),
        UNIFORM_IVEC2(5),
        UNIFORM_IVEC3(6),
        UNIFORM_IVEC4(7),
        UNIFORM_SAMPLER2D(8);

        int ShaderUniformDataInt;

        ShaderUniformDataType(int i){
            ShaderUniformDataInt = i;
        }

        public int getShaderUniformDataInt(){
            return ShaderUniformDataInt;
        }

        public static ShaderUniformDataType getByInt(int index){
            for(ShaderUniformDataType dt : values()){
                if(dt.getShaderUniformDataInt() == index){
                    return dt;
                }
            }
            return null;
        }
    }

    // Material map type
    public enum MaterialMapType{
        MAP_ALBEDO(0),       // MAP_DIFFUSE
        MAP_METALNESS(1),       // MAP_SPECULAR
        MAP_NORMAL(2),
        MAP_ROUGHNESS(3),
        MAP_OCCLUSION(4),
        MAP_EMISSION(5),
        MAP_HEIGHT(6),
        MAP_CUBEMAP(7),             // NOTE: Uses GL_TEXTURE_CUBE_MAP
        MAP_IRRADIANCE(8),          // NOTE: Uses GL_TEXTURE_CUBE_MAP
        MAP_PREFILTER(9),           // NOTE: Uses GL_TEXTURE_CUBE_MAP
        MAP_BRDF(10);

        int MaterialMapInt;

        MaterialMapType(int i){
            MaterialMapInt = i;
        }

        public int getMaterialMapInt(){
            return MaterialMapInt;
        }
    }

    public RLGL(){
        rlglData = new rlglData();
    }

    public static rlglData getRlglData(){
        return rlglData;
    }

    public static void rlMatrixMode(int mode){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlMatrixMode(mode);
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlMatrixMode(mode);
        }
    }

    public static void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlFrustum(left, right, bottom, top, znear, zfar);
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlFrustum(left, right, bottom, top, znear, zfar);
        }
    }

    public static void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlOrtho(left, right, bottom, top, znear, zfar);
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlOrtho(left, right, bottom, top, znear, zfar);
        }
    }

    public static void rlPushMatrix(){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlPushMatrix();
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlPushMatrix();
        }
    }

    public static void rlPopMatrix(){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlPopMatrix();
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlPopMatrix();
        }
    }

    public static void rlLoadIdentity(){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlLoadIdentity();
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlLoadIdentity();
        }
    }

    public static void rlTranslatef(float x, float y, float z){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlTranslatef(x, y, z);
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlTranslatef(x, y, z);
        }
    }

    public static void rlRotatef(float angleDeg, float x, float y, float z){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlRotatef(angleDeg, x, y, z);
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlRotatef(angleDeg, x, y, z);
        }
    }

    public static void rlScalef(float x, float y, float z){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlScalef(x, y, z);

        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlScalef(x, y, z);
        }
    }

    public static void rlMultMatrixf(float[] matf){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlMultMatrixf(matf);
        }
        else if(GRAPHICS_API_OPENGL_11){
            GL_11.rlMultMatrixf(matf);
        }

    }

    // Set the viewport area (transformation from normalized device coordinates to window coordinates)
    public static void rlViewport(int x, int y, int width, int height){
        glViewport(x, y, width, height);
    }

    public static void rlBegin(int mode){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlBegin(mode);
        }
        else{
            GL_11.rlBegin(mode);
        }
    }

    public static void rlEnd(){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlEnd();
        }
        else{
            GL_11.rlEnd();
        }
    }

    // Define one vertex (position)
    // NOTE: Vertex position data is the basic information required for drawing
    void rlVertex3f(float x, float y, float z){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlVertex3f(x, y, z);
        }
        else{
            GL_11.rlVertex3f(x, y, z);
        }
    }

    // Define one vertex (position)
    public static void rlVertex2f(float x, float y){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlVertex2f(x, y);
        }
        else{
            GL_11.rlVertex2f(x, y);
        }
    }

    // Define one vertex (position)
    public static void rlVertex2i(int x, int y){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlVertex2i(x, y);
        }
        else{
            GL_11.rlVertex2i(x, y);
        }
    }

    // Define one vertex (texture coordinate)
    // NOTE: Texture coordinates are limited to QUADS only
    public static void rlTexCoord2f(float x, float y){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlTexCoord2f(x, y);
        }
        else{
            GL_11.rlTexCoord2f(x, y);
        }
    }

    // Define one vertex (normal)
    // NOTE: Normals limited to TRIANGLES only?
    public static void rlNormal3f(float x, float y, float z){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlNormal3f(x, y, z);
        }
        else{
            GL_11.rlNormal3f(x, y, z);
        }
    }

    // Define one vertex (color)
    public static void rlColor4ub(int x, int y, int z, int w){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlColor4ub(x, y, z, w);
        }
        else{
            GL_11.rlColor4ub(x, y, z, w);
        }
    }

    // Define one vertex (color)
    void rlColor4f(float r, float g, float b, float a){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlColor4f(r, g, b, a);
        }
        else{
            GL_11.rlColor4f(r, g, b, a);
        }
    }

    // Define one vertex (color)
    void rlColor3f(float x, float y, float z){
        if(GRAPHICS_API_OPENGL_33){
            GL_33.rlColor3f(x, y, z);
        }
        else{
            GL_11.rlColor3f(x, y, z);
        }
    }

    public static void rlEnableTexture(int id){
        if(GRAPHICS_API_OPENGL_11){
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, id);
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].textureId != id){
                if(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount > 0){
                    // Make sure current rlglData.getCurrentBatch().draws[i].vertexCount is aligned a multiple of 4,
                    // that way, following QUADS drawing will keep aligned with index processing
                    // It implies adding some extra alignment vertex at the end of the draw,
                    // those vertex are not processed but they are considered as an additional offset
                    // for the next set of vertex to be drawn
                    if(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].mode == RL_LINES){
                        rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount < 4) ? rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount : rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount % 4);
                    }
                    else if(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].mode == RL_TRIANGLES){
                        rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount < 4) ? 1 : (4 - (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount % 4)));
                    }

                    else{
                        rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment = 0;
                    }

                    if(rlCheckBufferLimit(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment)){
                        DrawRenderBatch(rlglData.getCurrentBatch());
                    }
                    else{
                        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].vCounter += rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment;
                        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].cCounter += rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment;
                        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].tcCounter += rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexAlignment;

                        rlglData.getCurrentBatch().drawsCounter++;
                    }
                }

                if(rlglData.getCurrentBatch().drawsCounter >= DEFAULT_BATCH_DRAWCALLS){
                    DrawRenderBatch(rlglData.getCurrentBatch());
                }

                rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].textureId = id;
                rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawsCounter - 1].vertexCount = 0;
            }
        }
    }

    public static void rlDisableTexture(){
        if(GRAPHICS_API_OPENGL_11){
            glDisable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        else{
            // NOTE: If quads batch limit is reached,
            // we force a draw call and next batch starts
            if(rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].vCounter >=
                    (rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].elementsCount * 4)){
                DrawRenderBatch(rlglData.getCurrentBatch());
            }
        }
    }

    // Set texture parameters (wrap mode/filter mode)
    public static void rlTextureParameters(int id, int param, int value){
        glBindTexture(GL_TEXTURE_2D, id);

        switch(param){
            case RL_TEXTURE_WRAP_S:
            case RL_TEXTURE_WRAP_T:{
                if(value == RL_TEXTURE_WRAP_MIRROR_CLAMP){
                    if(GRAPHICS_API_OPENGL_11){
                        if(rlglData.getExtSupported().isTexMirrorClamp()){
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
            case RL_TEXTURE_ANISOTROPIC_FILTER:{
                if(GRAPHICS_API_OPENGL_11){
                    if(value <= rlglData.getExtSupported().getMaxAnisotropicLevel()){
                        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) value);
                    }
                    else if(rlglData.getExtSupported().getMaxAnisotropicLevel() > 0.0f){
                        Tracelog(LOG_WARNING, "GL: Maximum anisotropic filter level supported is " + id + "X" +
                                rlglData.getExtSupported().getMaxAnisotropicLevel());
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
    void rlEnableShader(int id){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(id);
        }
    }

    // Disable shader program usage
    void rlDisableShader(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(0);
        }
    }

    // Enable rendering to texture (fbo)
    public static void rlEnableFramebuffer(int id){
        if((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && SUPPORT_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, id);
        }
    }

    // Disable rendering to texture
    public static void rlDisableFramebuffer(){
        if((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && SUPPORT_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
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
    void rlEnableWireMode(){
        if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
    }

    // Disable wire mode
    void rlDisableWireMode(){
        if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            // NOTE: glPolygonMode() not available on OpenGL ES
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    // Set the line drawing width
    void rlSetLineWidth(float width){
        glLineWidth(width);
    }

    // Get the line drawing width
    float rlGetLineWidth(){
        return glGetFloat(GL_LINE_WIDTH);
    }

    // Enable line aliasing
    void rlEnableSmoothLines(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_11){
            glEnable(GL_LINE_SMOOTH);
        }
    }

    // Disable line aliasing
    void rlDisableSmoothLines(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_11){
            glDisable(GL_LINE_SMOOTH);
        }
    }

    // Unload framebuffer from GPU memory
    // NOTE: All attached textures/cubemaps/renderbuffers are also deleted
    public static void rlUnloadFramebuffer(int id){
        if((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && SUPPORT_RENDER_TEXTURES_HINT){

            // Query depth attachment to automatically delete texture/renderbuffer
            int depthType, depthId;
            glBindFramebuffer(GL_FRAMEBUFFER, id);   // Bind framebuffer to query depth texture type
            depthType = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            depthId = glGetFramebufferAttachmentParameteri(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                    GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);

            int depthIdU = depthId;
            if(depthType == GL_RENDERBUFFER){
                glDeleteRenderbuffers(depthIdU);
            }
            else if(depthType == GL_RENDERBUFFER){
                glDeleteTextures(depthIdU);
            }

            // NOTE: If a texture object is deleted while its image is attached to the *currently bound* framebuffer,
            // the texture image is automatically detached from the currently bound framebuffer.

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(id);

            Tracelog(LOG_INFO, "FBO: [ID " + id + "] Unloaded framebuffer from VRAM (GPU)");
        }
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

    // Update GPU buffer with new data
    public void rlUpdateBuffer(int bufferId, int[] data, int dataSize){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glBindBuffer(GL_ARRAY_BUFFER, bufferId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, data);
        }
    }

    public static void rlglInit(int width, int height){
        // Check OpenGL information and capabilities
        //------------------------------------------------------------------------------
        // Print current OpenGL and GLSL version
        Tracelog(LOG_INFO, "GL: OpenGL device information:");
        Tracelog(LOG_INFO, "    > Vendor: " + glGetString(GL_VENDOR));
        Tracelog(LOG_INFO, "    > Renderer: " + glGetString(GL_RENDERER));
        Tracelog(LOG_INFO, "    > Version: " + glGetString(GL_VERSION));
        Tracelog(LOG_INFO, "    > GLSL: " + glGetString(GL_SHADING_LANGUAGE_VERSION));

        String glVersion = glGetString(GL_VERSION);
        float glVersionF = Float.parseFloat(glVersion.substring(0, 3));

        if(glVersionF >= 3.3f){
            GRAPHICS_API_OPENGL_33 = true;
            GRAPHICS_API_OPENGL_21 = false;
            GRAPHICS_API_OPENGL_11 = false;

        }
        else if(glVersionF < 3.3f && glVersionF >= 2.1f){
            GRAPHICS_API_OPENGL_33 = false;
            GRAPHICS_API_OPENGL_21 = true;
            GRAPHICS_API_OPENGL_11 = false;
        }
        else if(glVersionF < 2.1f && glVersionF >= 1.1f){
            GRAPHICS_API_OPENGL_33 = false;
            GRAPHICS_API_OPENGL_21 = false;
            GRAPHICS_API_OPENGL_11 = true;
        }

        // TODO: Automatize extensions loading using rlLoadExtensions() and GLAD
        // Actually, when rlglInit() is called in InitWindow() in core.c,
        // OpenGL context has already been created and required extensions loaded
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int numExt = 0;
            String[] extList = new String[numExt];

            if(GRAPHICS_API_OPENGL_33 && !GRAPHICS_API_OPENGL_21){
                // NOTE: On OpenGL 3.3 VAO and NPOT are supported by default
                rlglData.getExtSupported().setVao(true);

                // Multiple texture extensions supported by default
                rlglData.getExtSupported().setTexNPOT(true);
                rlglData.getExtSupported().setTexFloat32(true);
                rlglData.getExtSupported().setTexDepth(true);

                // We get a list of available extensions and we check for some of them (compressed textures)
                // NOTE: We don't need to check again supported extensions but we do (GLAD already dealt with that)
                numExt = glGetInteger(GL_NUM_EXTENSIONS);

                // Allocate numExt strings pointers
                extList = new String[numExt];

                // Get extensions strings
                for(int i = 0; i < numExt; i++){
                    extList[i] = glGetStringi(GL_EXTENSIONS, i);
                }
            }

            if(GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_21){
                // Allocate 512 strings pointers (2 KB)

                // NOTE: We have to duplicate string because glGetString() returns a const string
                String extensions = glGetString(GL_EXTENSIONS);
                extensions = extensions != null ? extensions.replace(' ', '\0') : null;

                for(int i = 0; i < extensions.length(); i++){
                    extList[i] = String.valueOf(extensions.charAt(i));
                }

                // NOTE: Duplicated string (extensionsDup) must be deallocated
            }

            Tracelog(LOG_INFO, "GL: Supported extensions count: " + numExt);

            // Show supported extensions
            //for (int i = 0; i < numExt; i++)  Tracelog(LOG_INFO, "Supported extension: %s", extList[i]);

            // Check required extensions
            for(int i = 0; i < numExt; i++){
                if(GRAPHICS_API_OPENGL_ES2){
                    // Check VAO support
                    // NOTE: Only check on OpenGL ES, OpenGL 3.3 has VAO support as core feature
                    /*if (Arrays.toString(extList).equals("GL_OES_vertex_array_object")){
                        // The extension is supported by our hardware and driver, try to get related functions pointers
                        // NOTE: emscripten does not support VAOs natively, it uses emulation and it reduces overall performance...
                        glGenVertexArrays() = (PFNGLGENVERTEXARRAYSOESPROC) eglGetProcAddress("glGenVertexArraysOES");

                        glBindVertexArray((PFNGLBINDVERTEXARRAYOESPROC) eglGetProcAddress("glBindVertexArrayOES"));
                        glDeleteVertexArrays((PFNGLDELETEVERTEXARRAYSOESPROC) eglGetProcAddress("glDeleteVertexArraysOES"));
                        //glIsVertexArray = (PFNGLISVERTEXARRAYOESPROC)eglGetProcAddress("glIsVertexArrayOES");     // NOTE: Fails in WebGL, omitted

                        if ((glGenVertexArrays() != 0) && (glBindVertexArray() != 0) && (glDeleteVertexArrays() != 0)){
                            rlgl.getExtSupported().setVao(true);
                        }
                    }*/

                    // Check NPOT textures support
                    // NOTE: Only check on OpenGL ES, OpenGL 3.3 has NPOT textures full support as core feature
                    if(Arrays.toString(extList).equals("GL_OES_texture_npot")){
                        rlglData.getExtSupported().setTexNPOT(true);
                    }

                    // Check texture float support
                    if(Arrays.toString(extList).equals("GL_OES_texture_float")){
                        rlglData.getExtSupported().setTexFloat32(true);
                    }

                    // Check depth texture support
                    if(Arrays.toString(extList).equals("GL_OES_depth_texture") || Arrays.toString(extList).equals(
                            "GL_WEBGL_depth_texture")){
                        rlglData.getExtSupported().setTexDepth(true);
                    }

                    if(Arrays.toString(extList).equals("GL_OES_depth24")){
                        rlglData.getExtSupported().setMaxDepthBits(24);
                    }
                    if(Arrays.toString(extList).equals("GL_OES_depth32")){
                        rlglData.getExtSupported().setMaxDepthBits(32);
                    }
                }
                // DDS texture compression support
                if(Arrays.toString(extList).equals("GL_EXT_texture_compression_s3tc") ||
                        Arrays.toString(extList).equals("GL_WEBGL_compressed_texture_s3tc") ||
                        Arrays.toString(extList).equals("GL_WEBKIT_WEBGL_compressed_texture_s3tc")){
                    rlglData.getExtSupported().setTexCompDXT(true);
                }

                // ETC1 texture compression support
                if(Arrays.toString(extList).equals("GL_OES_compressed_ETC1_RGB8_texture") ||
                        Arrays.toString(extList).equals("GL_WEBGL_compressed_texture_etc1")){
                    rlglData.getExtSupported().setTexCompETC1(true);
                }

                // ETC2/EAC texture compression support
                if(Arrays.toString(extList).equals("GL_ARB_ES3_compatibility")){
                    rlglData.getExtSupported().setTexCompETC2(true);
                }

                // PVR texture compression support
                if(Arrays.toString(extList).equals("GL_IMG_texture_compression_pvrtc")){
                    rlglData.getExtSupported().setTexCompPVRT(true);
                }

                // ASTC texture compression support
                if(Arrays.toString(extList).equals("GL_KHR_texture_compression_astc_hdr")){
                    rlglData.getExtSupported().setTexCompASTC(true);
                }
                // Anisotropic texture filter support
                if(Arrays.toString(extList).equals("GL_EXT_texture_filter_anisotropic")){
                    rlglData.getExtSupported().setTexAnisoFilter(true);
                    glGetFloatv(0x84FF, new float[]{rlglData.getExtSupported().getMaxAnisotropicLevel()});
                    // GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
                }

                // Clamp mirror wrap mode supported
                if(Arrays.toString(extList).equals("GL_EXT_texture_mirror_clamp")){
                    rlglData.getExtSupported().setTexMirrorClamp(true);
                }

                // Debug marker support
                if(Arrays.toString(extList).equals("GL_EXT_debug_marker")){
                    rlglData.getExtSupported().setDebugMarker(true);
                }
            }

            // Free extensions pointers
            extList = null;

            if(GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_21){
                //extensionsDup = null;    // Duplicated string must be deallocated
            }

            if(GRAPHICS_API_OPENGL_ES2){
                if(rlglData.getExtSupported().isVao()){
                    Tracelog(LOG_INFO, "GL: VAO extension detected, VAO functions initialized successfully");
                }
                else{
                    Tracelog(LOG_WARNING, "GL: VAO extension not found, VAO usage not supported");
                }

                if(rlglData.getExtSupported().isTexNPOT()){
                    Tracelog(LOG_INFO, "GL: NPOT textures extension detected, full NPOT textures supported");
                }
                else{
                    Tracelog(LOG_WARNING, "GL: NPOT textures extension not found, limited NPOT support (no-mipmaps, no-repeat)");
                }
            }

            if(rlglData.getExtSupported().isTexCompDXT()){
                Tracelog(LOG_INFO, "GL: DXT compressed textures supported");
            }
            if(rlglData.getExtSupported().isTexAnisoFilter()){
                Tracelog(LOG_INFO, "GL: ETC1 compressed textures supported");
            }
            if(rlglData.getExtSupported().isTexCompETC2()){
                Tracelog(LOG_INFO, "GL: ETC2/EAC compressed textures supported");
            }
            if(rlglData.getExtSupported().isTexCompPVRT()){
                Tracelog(LOG_INFO, "GL: PVRT compressed textures supported");
            }
            if(rlglData.getExtSupported().isTexCompASTC()){
                Tracelog(LOG_INFO, "GL: ASTC compressed textures supported");
            }

            if(rlglData.getExtSupported().isTexAnisoFilter()){
                Tracelog(LOG_INFO,
                        "GL: Anisotropic textures filtering supported (max: " +
                                rlglData.getExtSupported().getMaxAnisotropicLevel() + ")");
            }
            if(rlglData.getExtSupported().isTexMirrorClamp()){
                Tracelog(LOG_INFO, "GL: Mirror clamp wrap texture mode supported");
            }

            if(rlglData.getExtSupported().isDebugMarker()){
                Tracelog(LOG_INFO, "GL: Debug Marker supported");
            }

            // Initialize buffers, default shaders and default textures
            //----------------------------------------------------------
            // Init default white texture
            int[] pixels = new int[]{255,255,255,255};
            rlglData.getState().setDefaultTextureId(rlLoadTexture(pixels, 1, 1, UNCOMPRESSED_R8G8B8A8.getPixForInt(), 1));

            if(rlglData.getState().getDefaultTextureId() != 0){
                Tracelog(LOG_INFO, "TEXTURE: [ID " + rlglData.getState().getDefaultTextureId()
                        + "] Default texture loaded successfully");
            }
            else{
                Tracelog(LOG_WARNING, "TEXTURE: Failed to load default texture");
            }

            // Init default Shader (customized for GL 3.3 and ES2)
            rlglData.getState().setDefaultShader(LoadShaderDefault());
            rlglData.getState().setCurrentShader(rlglData.getState().getDefaultShader());

            // Init default vertex arrays buffers
            rlglData.setDefaultBatch(LoadRenderBatch(DEFAULT_BATCH_BUFFERS, DEFAULT_BATCH_BUFFER_ELEMENTS));
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

        }// GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2

        // Initialize OpenGL default states
        //----------------------------------------------------------
        // Init state: Depth test
        glDepthFunc(GL_LEQUAL);                                 // Type of depth testing to apply
        glDisable(GL_DEPTH_TEST);                               // Disable depth testing for 2D (only used for 3D)

        // Init state: Blending mode
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);      // Color blending function (how colors are mixed)
        glEnable(GL_BLEND);                                     // Enable color blending (required to work with transparencies)

        // Init state: Culling
        // NOTE: All shapes/models triangles are drawn CCW
        glCullFace(GL_BACK);                                    // Cull the back face (default)
        glFrontFace(GL_CCW);                                    // Front face are defined counter clockwise (default)
        glEnable(GL_CULL_FACE);                                 // Enable backface culling

        // Init state: Cubemap seamless
        if(GRAPHICS_API_OPENGL_33){
            glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);                 // Seamless cubemaps (not supported on OpenGL ES 2.0)
        }

        if(GRAPHICS_API_OPENGL_11){
            // Init state: Color hints (deprecated in OpenGL 3.0+)
            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);      // Improve quality of color and texture coordinate interpolation
            glShadeModel(GL_SMOOTH);                                // Smooth shading between vertex (vertex colors interpolation)
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // Store screen size into global variables
            rlglData.getState().setFramebufferWidth(width);
            rlglData.getState().setFramebufferHeight(height);

            // Init texture and rectangle used on basic shapes drawing
            rlglData.getState().setShapesTexture(GetTextureDefault());
            rlglData.getState().setShapesTextureRec(new Rectangle(0.0f, 0.0f, 1.0f, 1.0f));

            Tracelog(LOG_INFO, "RLGL: Default state initialized successfully");
        }

        // Init state: Color/Depth buffers clear
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);                   // Set clear color (black)

        glClearDepth(1.0f);                                     // Set clear depth value (default)

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // Clear color and depth buffers (depth buffer required for 3D)

    }

    // Vertex Buffer Object deinitialization (memory free)
    public static void rlglClose(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            UnloadRenderBatch(rlglData.getDefaultBatch());

            UnloadShaderDefault();          // Unload default shader
            glDeleteTextures(rlglData.getState().getDefaultTextureId()); // Unload default texture

            Tracelog(LOG_INFO, "TEXTURE: [ID " + rlglData.getState().getDefaultTextureId() + "] Unloaded default texture " +
                    "data from VRAM (GPU)");
        }
    }

    // Update and draw internal buffers
    public static void rlglDraw(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            DrawRenderBatch(rlglData.getCurrentBatch());    // NOTE: Stereo rendering is checked inside
        }
    }

    //rlCheckErrors

    public static int rlGetVersion(){
        GlVersion version = null;

        if(GRAPHICS_API_OPENGL_11){
            version = OPENGL_11;
        }
        else if(GRAPHICS_API_OPENGL_21){
            if(__APPLE__){
                version = OPENGL_33;           // NOTE: Force OpenGL 3.3 on OSX
            }
            else{
                version = OPENGL_21;
            }
        }
        else if(GRAPHICS_API_OPENGL_33){
            version = OPENGL_33;
        }
        else if(GRAPHICS_API_OPENGL_ES2){
            version = OPENGL_ES_20;
        }
        assert version != null;
        return version.getGlType();
    }

    public static boolean rlCheckBufferLimit(int vCount){
        boolean overflow = false;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if((rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().getCurrentBuffer()].getvCounter() + vCount) >=
                    (rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].getElementsCount() * 4)){
                overflow = true;
            }
        }
        return overflow;
    }

    // Set debug marker
    void rlSetDebugMarker(String text){
        if(GRAPHICS_API_OPENGL_33){
            if(rlglData.getExtSupported().isDebugMarker()){
                glInsertEventMarkerEXT(text);
            }
        }
    }

    // Set blending mode factor and equation
    void rlSetBlendMode(int glSrcFactor, int glDstFactor, int glEquation){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setGlBlendSrcFactor(glSrcFactor);
            rlglData.getState().setGlBlendDstFactor(glDstFactor);
            rlglData.getState().setGlBlendEquation(glEquation);
        }
    }

    //TODO: ??the fuck is GLAD??
    public static void rlLoadExtensions(){
        /*if (GRAPHICS_API_OPENGL_33){
            // NOTE: glad is generated and contains only required OpenGL 3.3 Core extensions (and lower versions)
            if (!__APPLE__){
                if (!gladLoadGLLoader((GLADloadproc) loader)){
                    Tracelog(LOG_WARNING, "GLAD: Cannot load OpenGL extensions");
                }
                else{
                    Tracelog(LOG_INFO, "GLAD: OpenGL extensions loaded successfully");
                }

                if (GRAPHICS_API_OPENGL_21){
                    if (GLAD_GL_VERSION_2_1) Tracelog(LOG_INFO, "GL: OpenGL 2.1 profile supported");
                }
                else if (GRAPHICS_API_OPENGL_33){
                    if (GLAD_GL_VERSION_3_3){
                        Tracelog(LOG_INFO, "GL: OpenGL 3.3 Core profile supported");
                    }
                    else{
                        Tracelog(LOG_ERROR, "GL: OpenGL 3.3 Core profile not supported");
                    }
                }
            }

            // With GLAD, we can check if an extension is supported using the GLAD_GL_xxx booleans
            //if (GLAD_GL_ARB_vertex_array_object) // Use GL_ARB_vertex_array_object
        }*/
    }

    // Convert image data to OpenGL texture (returns OpenGL valid Id)
    public static int rlLoadTexture(int[] data, int width, int height, int format, int mipmapCount){
        glBindTexture(GL_TEXTURE_2D, 0);    // Free any old binding

        int id = 0;

        // Check texture format support by OpenGL 1.1 (compressed textures not supported)
        if(GRAPHICS_API_OPENGL_11){
            if(format >= COMPRESSED_DXT1_RGB.pixForInt){
                Tracelog(LOG_WARNING, "GL: OpenGL 1.1 does not support GPU compressed texture formats");
                return id;
            }
        }
        else{
            if((!rlglData.getExtSupported().isTexCompDXT()) && ((format == COMPRESSED_DXT1_RGB.pixForInt) ||
                    (format == COMPRESSED_DXT1_RGBA.pixForInt) || (format == COMPRESSED_DXT3_RGBA.pixForInt) ||
                    (format == COMPRESSED_DXT5_RGBA.pixForInt))){
                Tracelog(LOG_WARNING, "GL: DXT compressed texture format not supported");
                return id;
            }
            if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
                if((!rlglData.getExtSupported().isTexCompETC1()) && (format == COMPRESSED_ETC1_RGB.pixForInt)){
                    Tracelog(LOG_WARNING, "GL: ETC1 compressed texture format not supported");
                    return id;
                }

                if((!rlglData.getExtSupported().isTexCompETC2()) && ((format == COMPRESSED_ETC2_RGB.pixForInt) ||
                        (format == COMPRESSED_ETC2_EAC_RGBA.pixForInt))){
                    Tracelog(LOG_WARNING, "GL: ETC2 compressed texture format not supported");
                    return id;
                }

                if((!rlglData.getExtSupported().isTexCompPVRT()) && ((format == COMPRESSED_PVRT_RGB.pixForInt) ||
                        (format == COMPRESSED_PVRT_RGBA.pixForInt))){
                    Tracelog(LOG_WARNING, "GL: PVRT compressed texture format not supported");
                    return id;
                }

                if((!rlglData.getExtSupported().isTexCompASTC()) && ((format == COMPRESSED_ASTC_4x4_RGBA.pixForInt) ||
                        (format == COMPRESSED_ASTC_8x8_RGBA.pixForInt))){
                    Tracelog(LOG_WARNING, "GL: ASTC compressed texture format not supported");
                    return id;
                }
            }
        }      // GRAPHICS_API_OPENGL_11

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        id = glGenTextures();              // Generate texture id

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
           // glActiveTexture(GL_TEXTURE0);     // If not defined, using GL_TEXTURE0 by default (shader texture)
        }

        glBindTexture(GL_TEXTURE_2D, id);

        int mipWidth = width;
        int mipHeight = height;
        int mipOffset = 0;          // Mipmap data offset

        // Load the different mipmap levels
        for(int i = 0; i < mipmapCount; i++){
            int mipSize = GetPixelDataSize(mipWidth, mipHeight, format);

            int glInternalFormat = 0, glFormat = 0, glType = 0;
            rlGetGlTextureFormats(format, glInternalFormat, glFormat, glType);

            TracelogS("TEXTURE: Load mipmap level " + i + " (" + mipWidth + " x " + mipHeight + "), size: " +
                    mipSize + ", offset: " + mipOffset);

            if(glInternalFormat != -1){
                if(format < COMPRESSED_DXT1_RGB.pixForInt){
                    glTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, glFormat, glType,
                            data.length + mipOffset);
                }
                else{
                    if(!GRAPHICS_API_OPENGL_11){
                        glCompressedTexImage2D(GL_TEXTURE_2D, i, glInternalFormat, mipWidth, mipHeight, 0, mipSize,
                                data.length + mipOffset);
                    }
                }

                int[] swizzleMask = new int[4];
                if(GRAPHICS_API_OPENGL_33){
                    if(format == UNCOMPRESSED_GRAYSCALE.pixForInt){
                        swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ONE};
                        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                    }
                    else if(format == UNCOMPRESSED_GRAY_ALPHA.pixForInt){
                        if(GRAPHICS_API_OPENGL_21){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ALPHA};
                        }
                    }
                    if(GRAPHICS_API_OPENGL_33){
                        swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_GREEN};
                    }
                }
                glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
            }

            mipWidth /= 2;
            mipHeight /= 2;
            mipOffset += mipSize;

            // Security check for NPOT textures
            if(mipWidth < 1){
                mipWidth = 1;
            }
            if(mipHeight < 1){
                mipHeight = 1;
            }
        }


        // Texture parameters configuration
        // NOTE: glTexParameteri does NOT affect texture uploading, just the way it's used
        if(GRAPHICS_API_OPENGL_ES2){
            // NOTE: OpenGL ES 2.0 with no GL_OES_texture_npot support (i.e. WebGL) has limited NPOT support, so CLAMP_TO_EDGE must be used
            if(rlglData.getExtSupported().isTexNPOT()){
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

        if(GRAPHICS_API_OPENGL_33){
            if(mipmapCount > 1){
                // Activate Trilinear filtering if mipmaps are available
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            }
        }

        // At this point we have the texture loaded in GPU and texture parameters configured

        // NOTE: If mipmaps were not in data, they are not generated automatically

        // Unbind current texture
        glBindTexture(GL_TEXTURE_2D, 0);

        if(id > 0){
            Tracelog(LOG_INFO, "TEXTURE: [ID " + id + "] Texture created successfully (" + width + "x" + height +
                    " - " + mipmapCount + " mipmaps)");
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: Failed to load texture");
        }

        return id;
    }

    // Load depth texture/renderbuffer (to be attached to fbo)
    // WARNING: OpenGL ES 2.0 requires GL_OES_depth_texture/WEBGL_depth_texture extensions
    int rlLoadTextureDepth(int width, int height, boolean useRenderBuffer){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // In case depth textures not supported, we force renderbuffer usage
            if(!rlglData.getExtSupported().isTexDepth()){
                useRenderBuffer = true;
            }

            // NOTE: We let the implementation to choose the best bit-depth
            // Possible formats: GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT32 and GL_DEPTH_COMPONENT32F
            int glInternalFormat = GL_DEPTH_COMPONENT;

            if(GRAPHICS_API_OPENGL_ES2){
                if(rlglData.getExtSupported().getMaxDepthBits() == 32){
                    glInternalFormat = GL_DEPTH_COMPONENT32_OES;
                }
                else if(rlglData.getExtSupported().getMaxDepthBits() == 24){
                    glInternalFormat = GL_DEPTH_COMPONENT24_OES;
                }
                else{
                    glInternalFormat = GL_DEPTH_COMPONENT16;
                }
            }

            if(!useRenderBuffer && rlglData.getExtSupported().isTexDepth()){
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
    int rlLoadTextureCubemap(int[] data, int size, int format){
        int id = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int dataSize = GetPixelDataSize(size, size, format);

            id = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, id);

            int glInternalFormat = 0;
            int glFormat = 0;
            int glType = 0;
            rlGetGlTextureFormats(format, glInternalFormat, glFormat, glType);

            if(glInternalFormat != -1){
                // Load cubemap faces
                for(int i = 0; i < 6; i++){
                    if(data == null){
                        if(format < COMPRESSED_DXT1_RGB.getPixForInt()){
                            if(format == UNCOMPRESSED_R32G32B32.getPixForInt()){
                                // Instead of using a sized internal texture format (GL_RGB16F, GL_RGB32F), we let the driver to choose the better format for us (GL_RGB)
                                if(rlglData.getExtSupported().isTexFloat32()){
                                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, size, size,
                                            0, GL_RGB, GL_FLOAT, (int[]) null);
                                }
                                else{
                                    Tracelog(LOG_WARNING, "TEXTURES: Cubemap requested format not supported");
                                }
                            }
                            else if((format == UNCOMPRESSED_R32.getPixForInt()) || (format == UNCOMPRESSED_R32G32B32A32.getPixForInt())){
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
                        if(format < COMPRESSED_DXT1_RGB.getPixForInt()){
                            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, size, size, 0,
                                    glFormat, glType, data[i] * dataSize);
                        }
                        else{
                            glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, size,
                                    size, 0, dataSize, (long) data[i] * dataSize);
                        }
                    }
                    int[] swizzleMask = new int[16];

                    if(GRAPHICS_API_OPENGL_33){
                        if(format == UNCOMPRESSED_GRAYSCALE.getPixForInt()){
                            swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ONE};
                            glTexParameteriv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask);
                        }
                        else if(format == UNCOMPRESSED_GRAY_ALPHA.getPixForInt()){
                            if(GRAPHICS_API_OPENGL_21){
                                swizzleMask = new int[]{GL_RED, GL_RED, GL_RED, GL_ALPHA};
                            }
                            else if(GRAPHICS_API_OPENGL_33){
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
            if(GRAPHICS_API_OPENGL_33){
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);  // Flag not supported on OpenGL ES 2.0
            }

            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        }

        if(id > 0){
            Tracelog(LOG_INFO, "TEXTURE: [ID " + id + "] Cubemap texture created successfully (" + size + "x" + size + ")");
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: Failed to load cubemap texture");
        }

        return id;
    }

    public static void rlUpdateTexture(int id, int offsetX, int offsetY, int width, int height, int format, int[] data){
        glBindTexture(GL_TEXTURE_2D, id);

        int glInternalFormat = 0, glFormat = 0, glType = 0;
        rlGetGlTextureFormats(format, glInternalFormat, glFormat, glType);

        if((glInternalFormat != -1) && (format < COMPRESSED_DXT1_RGB.getPixForInt())){
            glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, width, height, glFormat, glType, data);
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: [ID " + id + "] Failed to update for current texture format (" + format + ")");
        }
    }

    // Get OpenGL internal formats and data type from raylib PixelFormat
    static void rlGetGlTextureFormats(int format, int glInternalFormat, int glFormat, int glType){
        glInternalFormat = -1;
        glFormat = -1;
        glType = -1;

        PixelFormat pixForm = PixelFormat.getByInt(format);

        switch(pixForm){
            case UNCOMPRESSED_GRAYSCALE -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_LUMINANCE;
                    glFormat = GL_LUMINANCE;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_R8;
                    glFormat = GL_RED;
                    glType = GL_UNSIGNED_BYTE;
                }
            }
            case UNCOMPRESSED_GRAY_ALPHA -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_LUMINANCE_ALPHA;
                    glFormat = GL_LUMINANCE_ALPHA;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RG8;
                    glFormat = GL_RG;
                    glType = GL_UNSIGNED_BYTE;
                }
            }
            case UNCOMPRESSED_R5G6B5 -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGB;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_SHORT_5_6_5;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGB565;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_SHORT_5_6_5;
                }
            }
            case UNCOMPRESSED_R8G8B8 -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGB;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGB8;
                    glFormat = GL_RGB;
                    glType = GL_UNSIGNED_BYTE;
                }
            }
            case UNCOMPRESSED_R5G5B5A1 -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGBA;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_5_5_5_1;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGB5_A1;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_5_5_5_1;
                }
            }
            case UNCOMPRESSED_R4G4B4A4 -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGBA;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_4_4_4_4;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGBA4;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_SHORT_4_4_4_4;
                }
            }
            case UNCOMPRESSED_R8G8B8A8 -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    glInternalFormat = GL_RGBA;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_BYTE;
                }
                else if(GRAPHICS_API_OPENGL_33){
                    glInternalFormat = GL_RGBA8;
                    glFormat = GL_RGBA;
                    glType = GL_UNSIGNED_BYTE;
                }
            }
            case UNCOMPRESSED_R32 -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_LUMINANCE;
                        glFormat = GL_LUMINANCE;
                        glType = GL_FLOAT;
                    }
                }
                if(GRAPHICS_API_OPENGL_33){
                    if(rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_R32F;
                        glFormat = GL_RED;
                        glType = GL_FLOAT;
                    }
                } // NOTE: Requires extension OES_texture_float
            }
            case UNCOMPRESSED_R32G32B32 -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGB;
                        glFormat = GL_RGB;
                        glType = GL_FLOAT;
                    }
                }
                if(GRAPHICS_API_OPENGL_33){
                    if(rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGB32F;
                        glFormat = GL_RGB;
                        glType = GL_FLOAT;
                    }
                }    // NOTE: Requires extension OES_texture_float
            }
            case UNCOMPRESSED_R32G32B32A32 -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGBA;
                        glFormat = GL_RGBA;
                        glType = GL_FLOAT;
                    }
                }
                if(GRAPHICS_API_OPENGL_33){
                    if(rlglData.getExtSupported().isTexFloat32()){
                        glInternalFormat = GL_RGBA32F;
                    }
                    glFormat = GL_RGBA;
                    glType = GL_FLOAT;
                } // NOTE: Requires extension OES_texture_float
            }
            case COMPRESSED_DXT1_RGB -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                    }
                }
            }
            case COMPRESSED_DXT1_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
                    }
                }
            }
            case COMPRESSED_DXT3_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                    }
                }
            }
            case COMPRESSED_DXT5_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompDXT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                    }
                }
            }
            case COMPRESSED_ETC1_RGB -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompETC1()){
                        glInternalFormat = GL_ETC1_RGB8_OES;
                    }
                }  // NOTE: Requires OpenGL ES 2.0 or OpenGL 4.3
            }
            case COMPRESSED_ETC2_RGB -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompETC2()){
                        glInternalFormat = GL_COMPRESSED_RGB8_ETC2;
                    }
                }      // NOTE: Requires OpenGL ES 3.0 or OpenGL 4.3
            }
            case COMPRESSED_ETC2_EAC_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompETC2()){
                        glInternalFormat = GL_COMPRESSED_RGBA8_ETC2_EAC;
                    }
                }    // NOTE: Requires OpenGL ES 3.0 or OpenGL 4.3
            }
            case COMPRESSED_PVRT_RGB -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompPVRT()){
                        glInternalFormat = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
                    }
                } // NOTE: Requires PowerVR GPU
            }
            case COMPRESSED_PVRT_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompPVRT()){
                        glInternalFormat = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
                    }
                } // NOTE: Requires PowerVR GPU
            }
            case COMPRESSED_ASTC_4x4_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompASTC()){
                        glInternalFormat = GL_COMPRESSED_RGBA_ASTC_4x4_KHR;
                    }
                } // NOTE: Requires OpenGL ES 3.1 or OpenGL 4.3
            }
            case COMPRESSED_ASTC_8x8_RGBA -> {
                if(!GRAPHICS_API_OPENGL_11){
                    if(rlglData.getExtSupported().isTexCompASTC()){
                        glInternalFormat = GL_COMPRESSED_RGBA_ASTC_8x8_KHR;
                    }
                }  // NOTE: Requires OpenGL ES 3.1 or OpenGL 4.3
            }
            default -> {
                if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_21 || GRAPHICS_API_OPENGL_ES2){
                    throw new IllegalStateException("Unexpected value: " + format);
                }
                if(!GRAPHICS_API_OPENGL_11){
                    Tracelog(LOG_WARNING, "TEXTURE: Current format not supported (" + pixForm + ")");
                }

            }
        }

        RLGL.glInternalFormat = glInternalFormat;
        RLGL.glFormat = glFormat;
        RLGL.glType = glType;
    }

    public static void rlUnloadTexture(int id){
        glDeleteTextures(id);
    }

    // Load a framebuffer to be used for rendering
    // NOTE: No textures attached
    public static int rlLoadFramebuffer(int width, int height){
        int fboId = 0;

        if((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && SUPPORT_RENDER_TEXTURES_HINT){
            fboId = glGenFramebuffers();       // Create the framebuffer object
            glBindFramebuffer(GL_FRAMEBUFFER, 0);   // Unbind any framebuffer
        }

        return fboId;
    }

    // Attach color buffer texture to an fbo (unloads previous attachment)
    // NOTE: Attach type: 0-Color, 1-Depth renderbuffer, 2-Depth texture
    void rlFramebufferAttach(int fboId, int texId, int attachType, int texType){
        if((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && SUPPORT_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, fboId);

            FramebufferAttachType tmp = FramebufferAttachType.getByInt(attachType);

            switch(tmp){
                case RL_ATTACHMENT_COLOR_CHANNEL0:
                case RL_ATTACHMENT_COLOR_CHANNEL1:
                case RL_ATTACHMENT_COLOR_CHANNEL2:
                case RL_ATTACHMENT_COLOR_CHANNEL3:
                case RL_ATTACHMENT_COLOR_CHANNEL4:
                case RL_ATTACHMENT_COLOR_CHANNEL5:
                case RL_ATTACHMENT_COLOR_CHANNEL6:
                case RL_ATTACHMENT_COLOR_CHANNEL7:{
                    if(texType == RL_ATTACHMENT_TEXTURE2D.getTexInt()){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType, GL_TEXTURE_2D, texId, 0);
                    }
                    else if(texType == RL_ATTACHMENT_RENDERBUFFER.getTexInt()){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType, GL_RENDERBUFFER, texId);
                    }
                    else if(texType >= RL_ATTACHMENT_CUBEMAP_POSITIVE_X.getTexInt()){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + attachType, GL_TEXTURE_CUBE_MAP_POSITIVE_X + texType, texId, 0);
                    }

                }
                break;
                case RL_ATTACHMENT_DEPTH:{
                    if(texType == RL_ATTACHMENT_TEXTURE2D.getTexInt()){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texId, 0);
                    }
                    else if(texType == RL_ATTACHMENT_RENDERBUFFER.getTexInt()){
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, texId);
                    }

                }
                break;
                case RL_ATTACHMENT_STENCIL:{
                    if(texType == RL_ATTACHMENT_TEXTURE2D.getTexInt()){
                        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_TEXTURE_2D, texId, 0);
                    }
                    else if(texType == RL_ATTACHMENT_RENDERBUFFER.getTexInt()){
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
    boolean rlFramebufferComplete(int id){
        boolean result = false;

        if((GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) && SUPPORT_RENDER_TEXTURES_HINT){
            glBindFramebuffer(GL_FRAMEBUFFER, id);

            int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

            if(status != GL_FRAMEBUFFER_COMPLETE){
                switch(status){
                    case GL_FRAMEBUFFER_UNSUPPORTED:
                        Tracelog(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer is unsupported");
                        break;
                    case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                        Tracelog(LOG_WARNING, "FBO: [ID " + id + "] Framebuffer has incomplete attachment");
                        break;

                    case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                        if(GRAPHICS_API_OPENGL_ES2){
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

    // Generate mipmap data for selected texture
    public static void rlGenerateMipmaps(Texture2D texture){
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        // Check if texture is power-of-two (POT)
        boolean texIsPOT = false;

        if(((texture.getWidth() > 0) && ((texture.getWidth() & (texture.getWidth() - 1)) == 0)) &&
                ((texture.getHeight() > 0) && ((texture.getHeight() & (texture.getHeight() - 1)) == 0))){
            texIsPOT = true;
        }

        if(GRAPHICS_API_OPENGL_11){
            if(texIsPOT){
                // WARNING: Manual mipmap generation only works for RGBA 32bit textures!
                if(texture.getFormat() == UNCOMPRESSED_R8G8B8A8.pixForInt){
                    // Retrieve texture data from VRAM
                    int[] texData = rlReadTexturePixels(texture);

                    // NOTE: Texture data size is reallocated to fit mipmaps data
                    // NOTE: CPU mipmap generation only supports RGBA 32bit data
                    int mipmapCount = GenerateMipmaps(texData, texture.getWidth(), texture.getHeight());

                    int size = texture.getWidth() * texture.getHeight() * 4;
                    int offset = size;

                    int mipWidth = texture.getWidth() / 2;
                    int mipHeight = texture.getHeight() / 2;

                    // Load the mipmaps
                    for(int level = 1; level < mipmapCount; level++){
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
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if((texIsPOT) || (rlglData.getExtSupported().isTexNPOT())){
                //glHint(GL_GENERATE_MIPMAP_HINT, GL_DONT_CARE);   // Hint for mipmaps generation algorythm: GL_FASTEST, GL_NICEST, GL_DONT_CARE
                glGenerateMipmap(GL_TEXTURE_2D);    // Generate mipmaps automatically

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);   // Activate Trilinear filtering for mipmaps

                //#define MIN (a, b)(((a) < (b)) ? (a) : (b))
                //#define MAX (a, b)(((a) > (b)) ? (a) : (b))

                texture.setMipmaps(1 + (int) Math.floor(Math.log(Math.max(texture.getWidth(), texture.getHeight())) / Math.log(2)));
                Tracelog(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Mipmaps generated automatically, total: " + texture.getMipmaps());
            }
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.getId() + "] Failed to generate mipmaps");
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // TODO: 3/11/21
    //rlLoadMesh

    //rlLoadAttribBuffer

    //rlUpdateMesh

    //rlUpdateMeshAt

    //rlDrawMesh

    //rlDrawMeshInstanced

    //rlUnloadMesh

    public static int[] rlReadScreenPixels(int width, int height){
        int[] screenData = new int[width * height * 4];

        // NOTE 1: glReadPixels returns image flipped vertically -> (0,0) is the bottom left corner of the framebuffer
        // NOTE 2: We are getting alpha channel! Be careful, it can be transparent if not cleared properly!
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, screenData);

        // Flip image vertically!
        int[] imgData = new int[width * height * 4];

        for(int y = height - 1; y >= 0; y--){
            for(int x = 0; x < (width * 4); x++){
                imgData[((height - 1) - y) * width * 4 + x] = screenData[(y * width * 4) + x];  // Flip line

                // Set alpha component value to 255 (no trasparent image retrieval)
                // NOTE: Alpha value has already been applied to RGB in framebuffer, we don't need it!
                if(((x + 1) % 4) == 0){
                    imgData[((height - 1) - y) * width * 4 + x] = 255;
                }
            }
        }

        screenData = null;

        return imgData;     // NOTE: image data should be freed
    }

    // Read texture pixel data
    public static int[] rlReadTexturePixels(Texture2D texture){
        int[] pixels = null;

        if(GRAPHICS_API_OPENGL_11 || GRAPHICS_API_OPENGL_33){
            glBindTexture(GL_TEXTURE_2D, texture.getId());

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

            int glInternalFormat = 0, glFormat = 0, glType = 0;
            rlGetGlTextureFormats(texture.getFormat(), glInternalFormat, glFormat, glType);
            int size = GetPixelDataSize(texture.getWidth(), texture.getHeight(), texture.getFormat());

            if((glInternalFormat != -1) && (texture.getFormat() < COMPRESSED_DXT1_RGB.getPixForInt())){
                pixels = new int[size];
                glGetTexImage(GL_TEXTURE_2D, 0, glFormat, glType, pixels);
            }
            else{
                Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.getId() + "] Data retrieval not suported for " +
                        "pixel format (" + texture.getFormat() + ")");
            }

            glBindTexture(GL_TEXTURE_2D, 0);
        }

        if(GRAPHICS_API_OPENGL_ES2){
            // glGetTexImage() is not available on OpenGL ES 2.0
            // Texture2D width and height are required on OpenGL ES 2.0. There is no way to get it from texture id.
            // Two possible Options:
            // 1 - Bind texture to color fbo attachment and glReadPixels()
            // 2 - Create an fbo, activate it, render quad with texture, glReadPixels()
            // We are using Option 1, just need to care for texture format on retrieval
            // NOTE: This behaviour could be conditioned by graphic driver...
            int fboId = rlLoadFramebuffer(texture.getWidth(), texture.getHeight());

            // TODO: Create depth texture/renderbuffer for fbo?

            glBindFramebuffer(GL_FRAMEBUFFER, fboId);
            glBindTexture(GL_TEXTURE_2D, 0);

            // Attach our texture to FBO
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getId(), 0);

            // We read data as RGBA because FBO texture is configured as RGBA, despite binding another texture format
            pixels = new int[GetPixelDataSize(texture.getWidth(), texture.getHeight(),
                    UNCOMPRESSED_R8G8B8A8.getPixForInt())];
            glReadPixels(0, 0, texture.getWidth(), texture.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, pixels);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            // Clean up temporal fbo
            rlUnloadFramebuffer(fboId);
        }

        return pixels;
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Shaders Functions
    // NOTE: Those functions are exposed directly to the user in raylib.h
    //----------------------------------------------------------------------------------

    // Get default internal texture (white texture)
    static Texture2D GetTextureDefault(){
        Texture2D texture = new Texture2D();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            texture.setId(rlglData.getState().getDefaultTextureId());
            texture.setWidth(1);
            texture.setHeight(1);
            texture.setMipmaps(1);
            texture.setFormat(UNCOMPRESSED_R8G8B8A8.getPixForInt());
        }
        return texture;
    }

    // Get texture to draw shapes (RAII)
    public static Texture2D GetShapesTexture(){
        if(GRAPHICS_API_OPENGL_11){
            return new Texture2D();
        }
        else{
            return rlglData.getState().getShapesTexture();
        }
    }

    // Get texture rectangle to draw shapes
    public static Rectangle GetShapesTextureRec(){
        if(GRAPHICS_API_OPENGL_11){
            return new Rectangle();
        }
        else{
            return rlglData.getState().getShapesTextureRec();
        }
    }

    // Define default texture used to draw shapes
    public static void SetShapesTexture(Texture2D texture, Rectangle source){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setShapesTexture(texture);
            rlglData.getState().setShapesTextureRec(source);
        }
    }

    // Get default shader
    Shader GetShaderDefault(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            return rlglData.getState().getDefaultShader();
        }
        else{
            Shader shader = new Shader();
            return shader;
        }
    }

    // Load shader from files and bind default locations
    // NOTE: If shader string is NULL, using default vertex/fragment shaders
    Shader LoadShader(String vsFileName, String fsFileName){
        Shader shader = new Shader();

        // NOTE: Shader.locs is allocated by LoadShaderCode()

        String vShaderStr = "";
        String fShaderStr = "";
        try{
            if(vsFileName != null){
                vShaderStr = Files.LoadFileText(vsFileName);
            }
            if(fsFileName != null){
                fShaderStr = Files.LoadFileText(fsFileName);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        shader = LoadShaderCode(vShaderStr, fShaderStr);

        if(vShaderStr != null){
            vShaderStr = null;
        }
        if(fShaderStr != null){
            fShaderStr = null;
        }

        return shader;
    }

    // Load shader from code strings
    // NOTE: If shader string is NULL, using default vertex/fragment shaders
    Shader LoadShaderCode(String vsCode, String fsCode){
        Shader shader = new Shader();
        shader.setLocs(new int[MAX_SHADER_LOCATIONS]);

        // NOTE: All locations must be reseted to -1 (no location)
        for(int i = 0; i < MAX_SHADER_LOCATIONS; i++){
            shader.locs[i] = -1;
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int vertexShaderId = rlglData.getState().getDefaultVShaderId();
            int fragmentShaderId = rlglData.getState().getDefaultFShaderId();

            if(vsCode != null){
                vertexShaderId = CompileShader(vsCode, GL_VERTEX_SHADER);
            }
            if(fsCode != null){
                fragmentShaderId = CompileShader(fsCode, GL_FRAGMENT_SHADER);
            }

            if((vertexShaderId == rlglData.getState().getDefaultVShaderId()) &&
                    (fragmentShaderId == rlglData.getState().getDefaultFShaderId())){
                shader = rlglData.getState().getDefaultShader();
            }
            else{
                shader.setId(LoadShaderProgram(vertexShaderId, fragmentShaderId));

                if(vertexShaderId != rlglData.getState().getDefaultVShaderId()){
                    // Detach shader before deletion to make sure memory is freed
                    glDetachShader(shader.getId(), vertexShaderId);
                    glDeleteShader(vertexShaderId);
                }
                if(fragmentShaderId != rlglData.getState().getDefaultFShaderId()){
                    // Detach shader before deletion to make sure memory is freed
                    glDetachShader(shader.getId(), fragmentShaderId);
                    glDeleteShader(fragmentShaderId);
                }

                if(shader.getId() == 0){
                    Tracelog(LOG_WARNING, "SHADER: Failed to load custom shader code");
                    shader = rlglData.getState().getDefaultShader();
                }

                // After shader loading, we TRY to set default location names
                if(shader.getId() > 0){
                    SetShaderDefaultLocations(shader);
                }
            }

            // Get available shader uniforms
            // NOTE: This information is useful for debug...
            int uniformCount = glGetProgrami(shader.getId(), GL_ACTIVE_UNIFORMS);

            for(int i = 0; i < uniformCount; i++){
                int namelen = -1;
                int num = -1;
                char[] name = new char[256]; // Assume no variable names longer than 256
                int type = GL_ZERO;

                // Get the name of the uniforms
                String sName = glGetActiveUniform(shader.getId(), i, name.length - 1, IntBuffer.allocate(namelen),
                        IntBuffer.allocate(type));

                name[namelen] = 0;

                TracelogS("SHADER: [ID " + shader.getId() + "] Active uniform (" + Arrays.toString(name) + ") set at location: " +
                        glGetUniformLocation(shader.getId(), Arrays.toString(name)));
            }
        }

        return shader;
    }

    // Unload shader from GPU memory (VRAM)
    void UnloadShader(Shader shader){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if(shader.getId() != rlglData.getState().getDefaultShader().getId()){
                glDeleteProgram(shader.getId());
                shader.setLocs(null);

                Tracelog(LOG_INFO, "SHADER: [ID " + shader.getId() + "] Unloaded shader program data from VRAM (GPU)");
            }
        }
    }

    // Begin custom shader mode
    void BeginShaderMode(Shader shader){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if(rlglData.getState().getCurrentShader().getId() != shader.getId()){
                DrawRenderBatch(rlglData.getCurrentBatch());
                rlglData.getState().setCurrentShader(shader);
            }
        }
    }

    // End custom shader mode (returns to default shader)
    void EndShaderMode(){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            BeginShaderMode(rlglData.getState().getDefaultShader());
        }
    }

    // Get shader uniform location
    int GetShaderLocation(Shader shader, String uniformName){
        int location = -1;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            location = glGetUniformLocation(shader.getId(), uniformName);

            if(location == -1){
                Tracelog(LOG_WARNING, "SHADER: [ID " + shader.getId() + "] Failed to find shader uniform: " +
                        uniformName);
            }
            else{
                Tracelog(LOG_INFO, "SHADER: [ID " + shader.getId() + "] Shader uniform (" + location + ") set at " +
                        "location: " + location);
            }
        }
        return location;
    }

    // Get shader attribute location
    int GetShaderLocationAttrib(Shader shader, String attribName){
        int location = -1;
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            location = glGetAttribLocation(shader.getId(), attribName);

            if(location == -1){
                Tracelog(LOG_WARNING,
                        "SHADER: [ID " + shader.getId() + "] Failed to find shader attribute: " + attribName);
            }
            else{
                Tracelog(LOG_INFO, "SHADER: [ID " + shader.getId() + "] Shader attribute (" + attribName + ") set at " +
                        "location: " + location);
            }
        }
        return location;
    }

    // Set shader uniform value
    void SetShaderValue(Shader shader, int uniformLoc, int[] value, int uniformType)
    {
        SetShaderValueV(shader, uniformLoc, value, uniformType);
    }

    // Set shader uniform value vector
    void SetShaderValueV(Shader shader, int uniformLoc, int[] value, int uniformType){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(shader.getId());

            ShaderUniformDataType tmp = ShaderUniformDataType.getByInt(uniformType);

            switch(tmp){
                case UNIFORM_FLOAT:
                    glUniform1f(uniformLoc, value[0]);
                    break;
                case UNIFORM_VEC2:
                    glUniform2f(uniformLoc, value[0], value[1]);
                    break;
                case UNIFORM_VEC3:
                    glUniform3f(uniformLoc, value[0], value[1], value[2]);
                    break;
                case UNIFORM_VEC4:
                    glUniform4f(uniformLoc, value[0], value[1], value[2], value[3]);
                    break;
                case UNIFORM_INT:
                    glUniform1i(uniformLoc, value[0]);
                    break;
                case UNIFORM_IVEC2:
                    glUniform2i(uniformLoc, value[0], value[1]);
                    break;
                case UNIFORM_IVEC3:
                    glUniform3i(uniformLoc, value[0],value[1],value[2]);
                    break;
                case UNIFORM_IVEC4:
                    glUniform4i(uniformLoc, value[0], value[1], value[2], value[3]);
                    break;
                case UNIFORM_SAMPLER2D:
                    glUniform1i(uniformLoc, value[0]);
                    break;
                default:
                    Tracelog(LOG_WARNING, "SHADER: [ID " + shader.getId() + "] Failed to set uniform, data type not recognized");
            }

            //glUseProgram(0);      // Avoid reseting current shader program, in case other uniforms are set
        }
    }

    void SetShaderValueMatrix(Shader shader, int uniformLoc, Matrix mat){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(shader.getId());

            glUniformMatrix4fv(uniformLoc, false, MatrixToFloat(mat));

            //glUseProgram(0);
        }
    }

    // Set shader uniform value for texture
    void SetShaderValueTexture(Shader shader, int uniformLoc, Texture2D texture){
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            glUseProgram(shader.getId());

            // Check if texture is already active
            for (int i = 0; i < MAX_BATCH_ACTIVE_TEXTURES; i++){
                if (rlglData.getState().getActiveTextureId()[i] == texture.getId()){
                    return;
                }
            }

            // Register a new active texture for the internal batch system
            // NOTE: Default texture is always activated as GL_TEXTURE0
            for (int i = 0; i < MAX_BATCH_ACTIVE_TEXTURES; i++){
                if (rlglData.getState().getActiveTextureId()[i] == 0){
                    glUniform1i(uniformLoc, 1 + i);             // Activate new texture unit
                    rlglData.getState().getActiveTextureId()[i] = texture.getId(); // Save texture id for binding on
                    // drawing
                    break;
                }
            }

            //glUseProgram(0);
        }
    }

    // Set a custom projection matrix (replaces internal projection matrix)
    static void SetMatrixProjection(Matrix projection){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setProjection(projection);
        }
    }

    // Return internal projection matrix
    Matrix GetMatrixProjection(){
        if(GRAPHICS_API_OPENGL_11){
            float[] mat = new float[16];
            glGetFloatv(GL_PROJECTION_MATRIX, mat);

            return new Matrix(mat);
        }
        else{
            return rlglData.getState().getProjection();
        }
    }

    // Set a custom modelview matrix (replaces internal modelview matrix)
    static void SetMatrixModelview(Matrix view){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            rlglData.getState().setModelview(view);
        }
    }

    // Return internal modelview matrix
    Matrix GetMatrixModelview(){
        Matrix matrix;
        if(GRAPHICS_API_OPENGL_11){
            float[] mat = new float[16];
            glGetFloatv(GL_MODELVIEW_MATRIX, mat);
            matrix = new Matrix(mat);

        }
        else{
            matrix = rlglData.getState().getModelview();
        }
        return matrix;
    }

    //TODO: 3/11/21 - CUBEMAP
    //GenTextureCubeMap

    //GenTextureIrradience

    //GenTexturePrefilter

    // Generate BRDF texture using cubemap data
    // TODO: Review implementation: https://github.com/HectorMF/BRDFGenerator
    Texture2D GenTextureBRDF(Shader shader, int size){
        Texture2D brdf = new Texture2D();
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            // STEP 1: Setup framebuffer
            //------------------------------------------------------------------------------------------
            int rbo = rlLoadTextureDepth(size, size, true);
            brdf.setId(rlLoadTexture(null, size, size, UNCOMPRESSED_R32G32B32.getPixForInt(), 1));

            int fbo = rlLoadFramebuffer(size, size);
            rlFramebufferAttach(fbo, rbo, RL_ATTACHMENT_DEPTH.getRlAttachInt(), RL_ATTACHMENT_RENDERBUFFER.getTexInt());
            rlFramebufferAttach(fbo, brdf.getId(), RL_ATTACHMENT_COLOR_CHANNEL0.getRlAttachInt(), RL_ATTACHMENT_TEXTURE2D.getTexInt());
            //------------------------------------------------------------------------------------------

            // STEP 2: Draw to framebuffer
            //------------------------------------------------------------------------------------------
            // NOTE: Render BRDF LUT into a quad using FBO

            rlEnableShader(shader.getId());

            rlViewport(0, 0, size, size);

            rlEnableFramebuffer(fbo);
            rlClearScreenBuffers();
            GenDrawQuad();
            //------------------------------------------------------------------------------------------

            // STEP 3: Unload framebuffer and reset state
            //------------------------------------------------------------------------------------------
            rlDisableShader();          // Unbind shader
            rlDisableTexture();         // Unbind texture
            rlDisableFramebuffer();     // Unbind framebuffer
            rlUnloadFramebuffer(fbo);   // Unload framebuffer (and automatically attached depth texture/renderbuffer)

            // Reset viewport dimensions to default
            rlViewport(0, 0, rlglData.getState().getFramebufferWidth(), rlglData.getState().getFramebufferHeight());
            //------------------------------------------------------------------------------------------

            brdf.setWidth(size);
            brdf.setHeight(size);
            brdf.setMipmaps(1);
            brdf.setFormat(UNCOMPRESSED_R32G32B32.getPixForInt());
        }
        return brdf;
    }

    // Begin blending mode (alpha, additive, multiplied)
    // NOTE: Only 3 blending modes supported, default blend mode is alpha
    void BeginBlendMode(int mode){
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            if(rlglData.getState().getCurrentBlendMode() != mode){
                DrawRenderBatch(rlglData.getCurrentBatch());

                BlendMode tmp = BlendMode.getByInt(mode);

                switch(tmp){
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
                    case BLEND_CUSTOM:
                        glBlendFunc(rlglData.getState().getGlBlendSrcFactor(), rlglData.getState().getGlBlendDstFactor());
                        glBlendEquation(rlglData.getState().getGlBlendEquation());
                        break;
                    default:
                        break;
                }

                rlglData.getState().setCurrentBlendMode(mode);
            }
        }
    }

    // End blending mode (reset to default: alpha blending)
    void EndBlendMode(){
        BeginBlendMode(BLEND_ALPHA.blendInt);
    }

    //SUPPORT_VR_SIMULATOR
    // TODO: 3/11/21 - VR
    //InitVrSimulator

    //CloseVrSimulator

    //SetVrConfiguration

    //IsVrSimulatorReady

    //ToggleVrMode

    //BeginVrDrawing

    //EndVrDrawing
    //END SUPPORT_VR_SIMULATOR

    // Compile custom shader and return shader id
    static int CompileShader(String shaderStr, int type){
        int shader = glCreateShader(type);
        glShaderSource(shader, shaderStr);

        int success;
        glCompileShader(shader);
        success = glGetShaderi(shader, GL_COMPILE_STATUS);

        if(success == GL_FALSE){
            Tracelog(LOG_WARNING, "SHADER: [ID " + shader + "] Failed to compile shader code");

            int maxLength = 0;
            maxLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH);

            if(maxLength > 0){
                String log = "";

                log = glGetShaderInfoLog(shader, maxLength);
                Tracelog(LOG_WARNING, "SHADER: [ID " + shader + "] Compile error: " + log);
            }
        }
        else{
            Tracelog(LOG_INFO, "SHADER: [ID " + shader + "] Compiled successfully");
        }

        return shader;
    }

    // Load custom shader strings and return program id
    static int LoadShaderProgram(int vShaderId, int fShaderId){
        int program = 0;

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2){
            int success;
            program = glCreateProgram();

            glAttachShader(program, vShaderId);
            glAttachShader(program, fShaderId);

            // NOTE: Default attribute shader locations must be binded before linking
            glBindAttribLocation(program, 0, DEFAULT_SHADER_ATTRIB_NAME_POSITION);
            glBindAttribLocation(program, 1, DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
            glBindAttribLocation(program, 2, DEFAULT_SHADER_ATTRIB_NAME_NORMAL);
            glBindAttribLocation(program, 3, DEFAULT_SHADER_ATTRIB_NAME_COLOR);
            glBindAttribLocation(program, 4, DEFAULT_SHADER_ATTRIB_NAME_TANGENT);
            glBindAttribLocation(program, 5, DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2);

            // NOTE: If some attrib name is no found on the shader, it locations becomes -1

            glLinkProgram(program);

            // NOTE: All uniform variables are intitialised to 0 when a program links

            success = glGetProgrami(program, GL_LINK_STATUS);

            if(success == GL_FALSE){
                Tracelog(LOG_WARNING, "SHADER: [ID " + program + "] Failed to link shader program");

                int maxLength = 0;
                maxLength = glGetProgrami(program, GL_INFO_LOG_LENGTH);

                if(maxLength > 0){
                    String log = glGetProgramInfoLog(program, maxLength);
                    Tracelog(LOG_WARNING, "SHADER: [ID " + program + "] Link error: " + log);
                    log = null;
                }

                glDeleteProgram(program);

                program = 0;
            }
            else{
                Tracelog(LOG_INFO, "SHADER: [ID " + program + "] Program loaded successfully");
            }
        }
        return program;
    }

    // Load default shader (just vertex positioning and texture coloring)
    // NOTE: This shader program is used for internal buffers
    static Shader LoadShaderDefault(){
        Shader shader = new Shader();
        shader.setLocs(new int[MAX_SHADER_LOCATIONS]);

        // NOTE: All locations must be reseted to -1 (no location)
        for(int i = 0; i < MAX_SHADER_LOCATIONS; i++){
            shader.locs[i] = -1;
        }

        // Vertex shader directly defined, no external file required
        StringBuilder defaultVShaderStr = new StringBuilder();
        if(GRAPHICS_API_OPENGL_21){
            defaultVShaderStr.append("#version 120                       \n");
        }
        else if(GRAPHICS_API_OPENGL_ES2){
            defaultVShaderStr.append("#version 100                       \n");
        }
        if(GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_21){
            defaultVShaderStr.append("attribute vec3 vertexPosition;     \n");
            defaultVShaderStr.append("attribute vec2 vertexTexCoord;     \n");
            defaultVShaderStr.append("attribute vec4 vertexColor;        \n");
            defaultVShaderStr.append("varying vec2 fragTexCoord;         \n");
            defaultVShaderStr.append("varying vec4 fragColor;            \n");
        }
        else if(GRAPHICS_API_OPENGL_33){
            defaultVShaderStr.append("#version 330                       \n");
            defaultVShaderStr.append("in vec3 vertexPosition;            \n");
            defaultVShaderStr.append("in vec2 vertexTexCoord;            \n");
            defaultVShaderStr.append("in vec4 vertexColor;               \n");
            defaultVShaderStr.append("out vec2 fragTexCoord;             \n");
            defaultVShaderStr.append("out vec4 fragColor;                \n");
        }
        defaultVShaderStr.append("uniform mat4 mvp;                  \n");
        defaultVShaderStr.append("void main()                        \n");
        defaultVShaderStr.append("{                                  \n");
        defaultVShaderStr.append("    fragTexCoord = vertexTexCoord; \n");
        defaultVShaderStr.append("    fragColor = vertexColor;       \n");
        defaultVShaderStr.append("    gl_Position = mvp*vec4(vertexPosition, 1.0); \n");
        defaultVShaderStr.append("}                                  \n");

        // Fragment shader directly defined, no external file required
        StringBuilder defaultFShaderStr = new StringBuilder();
        if(GRAPHICS_API_OPENGL_21){
            defaultFShaderStr.append("#version 120                       \n");
        }
        else if(GRAPHICS_API_OPENGL_ES2){
            defaultFShaderStr.append("#version 100                       \n");
            defaultFShaderStr.append("precision mediump float;           \n");     // precision required for OpenGL
            // ES2 (WebGL)
        }
        if(GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_21){
            defaultFShaderStr.append("varying vec2 fragTexCoord;         \n");
            defaultFShaderStr.append("varying vec4 fragColor;            \n");
        }
        else if(GRAPHICS_API_OPENGL_33){
            defaultFShaderStr.append("#version 330       \n");
            defaultFShaderStr.append("in vec2 fragTexCoord;              \n");
            defaultFShaderStr.append("in vec4 fragColor;                 \n");
            defaultFShaderStr.append("out vec4 finalColor;               \n");
        }
        defaultFShaderStr.append("uniform sampler2D texture0;        \n");
        defaultFShaderStr.append("uniform vec4 colDiffuse;           \n");
        defaultFShaderStr.append("void main()                        \n");
        defaultFShaderStr.append("{                                  \n");
        if(GRAPHICS_API_OPENGL_ES2 || GRAPHICS_API_OPENGL_21){
            defaultFShaderStr.append("    vec4 texelColor = texture2D(texture0, fragTexCoord); \n"); // NOTE: texture2D() is deprecated on OpenGL 3
            // .3 and ES 3.0
            defaultFShaderStr.append("    gl_FragColor = texelColor*colDiffuse*fragColor;      \n");
        }
        else if(GRAPHICS_API_OPENGL_33){
            defaultFShaderStr.append("    vec4 texelColor = texture(texture0, fragTexCoord);   \n");
            defaultFShaderStr.append("    finalColor = texelColor*colDiffuse*fragColor;        \n");
        }
        defaultFShaderStr.append("}                                  \n");

        // NOTE: Compiled vertex/fragment shaders are kept for re-use
        rlglData.getState().setDefaultVShaderId(CompileShader(String.valueOf(defaultVShaderStr), GL_VERTEX_SHADER));     // Compile
        // default vertex shader
        rlglData.getState().setDefaultFShaderId(CompileShader(String.valueOf(defaultFShaderStr), GL_FRAGMENT_SHADER));   // Compile
        // default fragment shader

        shader.setId(LoadShaderProgram(rlglData.getState().getDefaultVShaderId(), rlglData.getState().getDefaultFShaderId()));

        if(shader.getId() > 0){
            Tracelog(LOG_INFO, "SHADER: [ID " + shader.getId() + "] Default shader loaded successfully");

            // Set default shader locations: attributes locations
            shader.locs[LOC_VERTEX_POSITION.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                    "vertexPosition");
            shader.locs[LOC_VERTEX_TEXCOORD01.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                    "vertexTexCoord");
            shader.locs[LOC_VERTEX_COLOR.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                    "vertexColor");

            // Set default shader locations: uniform locations
            shader.locs[LOC_MATRIX_MVP.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "mvp");
            shader.locs[LOC_COLOR_DIFFUSE.getShaderLocationInt()] = glGetUniformLocation(shader.getId(),
                    "colDiffuse");
            shader.locs[LOC_MAP_DIFFUSE.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "texture0");

            // NOTE: We could also use below function but in case DEFAULT_ATTRIB_* points are
            // changed for external custom shaders, we just use direct bindings above
            //SetShaderDefaultLocations(&shader);
        }
        else{
            Tracelog(LOG_WARNING, "SHADER: [ID " + shader.getId() + "] Failed to load default shader");
        }

        return shader;
    }

    // Get location handlers to for shader attributes and uniforms
    // NOTE: If any location is not found, loc point becomes -1
    static void SetShaderDefaultLocations(Shader shader){
        // NOTE: Default shader attrib locations have been fixed before linking:
        //          vertex position location    = 0
        //          vertex texcoord location    = 1
        //          vertex normal location      = 2
        //          vertex color location       = 3
        //          vertex tangent location     = 4
        //          vertex texcoord2 location   = 5

        // Get handles to GLSL input attibute locations
        shader.locs[LOC_VERTEX_POSITION.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                DEFAULT_SHADER_ATTRIB_NAME_POSITION);
        shader.locs[LOC_VERTEX_TEXCOORD01.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
        shader.locs[LOC_VERTEX_TEXCOORD02.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2);
        shader.locs[LOC_VERTEX_NORMAL.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                DEFAULT_SHADER_ATTRIB_NAME_NORMAL);
        shader.locs[LOC_VERTEX_TANGENT.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                DEFAULT_SHADER_ATTRIB_NAME_TANGENT);
        shader.locs[LOC_VERTEX_COLOR.getShaderLocationInt()] = glGetAttribLocation(shader.getId(),
                DEFAULT_SHADER_ATTRIB_NAME_COLOR);

        // Get handles to GLSL uniform locations (vertex shader)
        shader.locs[LOC_MATRIX_MVP.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "mvp");
        shader.locs[LOC_MATRIX_PROJECTION.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "projection");
        shader.locs[LOC_MATRIX_VIEW.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "view");

        // Get handles to GLSL uniform locations (fragment shader)
        shader.locs[LOC_COLOR_DIFFUSE.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "colDiffuse");
        shader.locs[LOC_MAP_DIFFUSE.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "texture0");
        shader.locs[LOC_MAP_SPECULAR.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "texture1");
        shader.locs[LOC_MAP_NORMAL.getShaderLocationInt()] = glGetUniformLocation(shader.getId(), "texture2");
    }


    //Unload default shader
    static void UnloadShaderDefault(){
        glUseProgram(0);

        glDetachShader(rlglData.getState().getDefaultShader().getId(), rlglData.getState().getDefaultFShaderId());
        glDetachShader(rlglData.getState().getDefaultShader().getId(), rlglData.getState().getDefaultFShaderId());
        glDeleteShader(rlglData.getState().getDefaultFShaderId());
        glDeleteShader(rlglData.getState().getDefaultFShaderId());

        glDeleteProgram(rlglData.getState().getDefaultShader().getId());

        rlglData.getState().getDefaultShader().setLocs(null);
    }

    //Load render batch
    static RenderBatch LoadRenderBatch(int numBuffers, int bufferElements){
        RenderBatch batch = new RenderBatch();

        // Initialize CPU (RAM) vertex buffers (position, texcoord, color data and indexes)
        //--------------------------------------------------------------------------------------------
        batch.vertexBuffer = new VertexBuffer[numBuffers];

        for(int i = 0; i < batch.vertexBuffer.length; i++){
            batch.vertexBuffer[i] = new VertexBuffer();
        }

        for(int i = 0; i < numBuffers; i++){
            batch.vertexBuffer[i].elementsCount = bufferElements;

            batch.vertexBuffer[i].setVertices(new float[bufferElements * 3 * Float.BYTES]);
            //batch.vertexBuffer[i].vertices.flip();
            // 3 float by vertex, 4 vertex by quad
            batch.vertexBuffer[i].setTexcoords(new float[bufferElements * 2 * Float.BYTES]);
            //batch.vertexBuffer[i].texcoords.flip();
            // 2 float by texcoord, 4 texcoord by quad
            batch.vertexBuffer[i].setColors(new float[bufferElements * 4 * Integer.BYTES]);
            //batch.vertexBuffer[i].colors.flip();
            // 4 float by color, 4 colors by quad
            if(GRAPHICS_API_OPENGL_33){
                //batch.vertexBuffer[i].setIndices_GL11() = (unsigned int *)RL_MALLOC(bufferElements * 6 * sizeof
                // (unsigned int));
                batch.getVertexBuffer()[i].setIndices_GL11(new int[bufferElements * 6 * Integer.BYTES]);
            }// 6 int by quad (indices)
            else if(GRAPHICS_API_OPENGL_ES2){
                batch.vertexBuffer[i].setIndices_ES20(new short[bufferElements * 6 * Short.SIZE]);
                // 6 int by quad (indices)
            }

            for (int j = 0; j < (3*4*bufferElements); j++) {
                batch.vertexBuffer[i].vertices[j] = 0.0f;
            }
            for (int j = 0; j < (2*4*bufferElements); j++) {
                batch.vertexBuffer[i].texcoords[j] = 0.0f;
            }
            for (int j = 0; j < (4*4*bufferElements); j++) {
                batch.vertexBuffer[i].colors[j] = 0;
            }

            int k = 0;

            if(GRAPHICS_API_OPENGL_33){
                // Indices can be initialized right now
                for(int j = 0; j < (6 * bufferElements); j += 6){
                    batch.vertexBuffer[i].getIndices_GL11()[j] = 4 * k;
                    batch.vertexBuffer[i].getIndices_GL11()[j + 1] = 4 * k + 1;
                    batch.vertexBuffer[i].getIndices_GL11()[j + 2] = 4 * k + 2;
                    batch.vertexBuffer[i].getIndices_GL11()[j + 3] = 4 * k;
                    batch.vertexBuffer[i].getIndices_GL11()[j + 4] = 4 * k + 2;
                    batch.vertexBuffer[i].getIndices_GL11()[j + 5] = 4 * k + 3;

                    k++;
                }
            }

            if(GRAPHICS_API_OPENGL_ES2){
                // Indices can be initialized right now
                for(int j = 0; j < (6 * bufferElements); j += 6){
                    batch.vertexBuffer[i].getIndices_ES20()[j] = (short) (4 * k);
                    batch.vertexBuffer[i].getIndices_ES20()[j + 1] = (short) (4 * k + 1);
                    batch.vertexBuffer[i].getIndices_ES20()[j + 2] = (short) (4 * k + 2);
                    batch.vertexBuffer[i].getIndices_ES20()[j + 3] = (short) (4 * k);
                    batch.vertexBuffer[i].getIndices_ES20()[j + 4] = (short) (4 * k + 2);
                    batch.vertexBuffer[i].getIndices_ES20()[j + 5] = (short) (4 * k + 3);

                    k++;
                }
            }

            batch.vertexBuffer[i].vCounter = 0;
            batch.vertexBuffer[i].tcCounter = 0;
            batch.vertexBuffer[i].cCounter = 0;
        }

        Tracelog(LOG_INFO, "RLGL: Internal vertex buffers initialized successfully in RAM (CPU)");
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
            glEnableVertexAttribArray(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_POSITION.getShaderLocationInt()]);
            glVertexAttribPointer(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_POSITION.getShaderLocationInt()],
                    3, GL_FLOAT, false, 0, 0);

            // Vertex texcoord buffer (shader-location = 1)
            batch.vertexBuffer[i].vboId[1] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[1]);
            glBufferData(GL_ARRAY_BUFFER, batch.vertexBuffer[i].texcoords, GL_DYNAMIC_DRAW);
            glEnableVertexAttribArray(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_TEXCOORD01.getShaderLocationInt()]);
            glVertexAttribPointer(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_TEXCOORD01.getShaderLocationInt()],
                    2, GL_FLOAT, false, 0, 0);

            // Vertex color buffer (shader-location = 3)
            batch.vertexBuffer[i].vboId[2] = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[2]);
            glBufferData(GL_ARRAY_BUFFER, batch.vertexBuffer[i].colors, GL_DYNAMIC_DRAW);
            glEnableVertexAttribArray(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_COLOR.getShaderLocationInt()]);
            glVertexAttribPointer(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_COLOR.getShaderLocationInt()],
                    4, GL_FLOAT, false, 0, 0);


            // Fill index buffer
            batch.vertexBuffer[i].vboId[3] = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[i].vboId[3]);
            if(GRAPHICS_API_OPENGL_33){
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[i].getIndices_GL11(), GL_STATIC_DRAW);
            }
            else if(GRAPHICS_API_OPENGL_ES2){
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[i].getIndices_ES20(), GL_STATIC_DRAW);
            }
        }

        Tracelog(LOG_INFO, "RLGL: Render batch vertex buffers loaded successfully");

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Unbind the current VAO
        if(rlglData.getExtSupported().isVao()){
            glBindVertexArray(0);
        }


        //--------------------------------------------------------------------------------------------
        // Init draw calls tracking system
        //--------------------------------------------------------------------------------------------
        //batch.draws = (DrawCall *)RL_MALLOC(DEFAULT_BATCH_DRAWCALLS*sizeof(DrawCall));
        batch.draws = new DrawCall[DEFAULT_BATCH_DRAWCALLS];
        for(int i = 0; i < batch.draws.length; i++){
            batch.draws[i] = new DrawCall();
        }
        for(int i = 0; i < DEFAULT_BATCH_DRAWCALLS; i++){
            batch.draws[i].mode = RL_QUADS;
            batch.draws[i].vertexCount = 0;
            batch.draws[i].vertexAlignment = 0;
            //batch.draws[i].vaoId = 0;
            //batch.draws[i].shaderId = 0;
            batch.draws[i].textureId = rlglData.getState().getDefaultTextureId();
            //batch.draws[i].rlgl.State.projection = MatrixIdentity();
            //batch.draws[i].rlgl.State.modelview = MatrixIdentity();
        }

        batch.buffersCount = numBuffers;    // Record buffer count
        batch.drawsCounter = 1;             // Reset draws counter
        batch.currentDepth = -1.0f;         // Reset depth value
        //--------------------------------------------------------------------------------------------

        return batch;
    }

    //Draw render batch
    // NOTE: We require a pointer to reset batch and increase current buffer (multi-buffer)
    static void DrawRenderBatch(RenderBatch batch){
        // Update batch vertex buffers
        //------------------------------------------------------------------------------------------------------------
        // NOTE: If there is not vertex data, buffers doesn't need to be updated (vertexCount > 0)
        // TODO: If no data changed on the CPU arrays --> No need to re-update GPU arrays (change flag required)
        if(batch.getVertexBuffer()[batch.currentBuffer].vCounter > 0){
            // Activate elements VAO
            if(rlglData.getExtSupported().isVao()){
                glBindVertexArray(batch.vertexBuffer[batch.currentBuffer].vaoId);
            }

            // Vertex positions buffer
            glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.getCurrentBuffer()].vboId[0]);
            glBufferSubData(GL_ARRAY_BUFFER, 0, batch.vertexBuffer[batch.getCurrentBuffer()].vertices);
            //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*3*4*batch->vertexBuffer[batch->currentBuffer].elementsCount,
            // batch->vertexBuffer[batch->currentBuffer].vertices, GL_DYNAMIC_DRAW);  // Update all buffer

            // Texture coordinates buffer
            glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.getCurrentBuffer()].vboId[1]);
            glBufferSubData(GL_ARRAY_BUFFER, 0, batch.vertexBuffer[batch.getCurrentBuffer()].texcoords);
            //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*2*4*batch->vertexBuffer[batch->currentBuffer].elementsCount,
            // batch->vertexBuffer[batch->currentBuffer].texcoords, GL_DYNAMIC_DRAW); // Update all buffer

            // Colors buffer
            glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.getCurrentBuffer()].vboId[2]);
            glBufferSubData(GL_ARRAY_BUFFER, 0, batch.vertexBuffer[batch.getCurrentBuffer()].colors);
            //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*4*4*batch->vertexBuffer[batch->currentBuffer].elementsCount,
            // batch->vertexBuffer[batch->currentBuffer].colors, GL_DYNAMIC_DRAW);    // Update all buffer

            // NOTE: glMapBuffer() causes sync issue.
            // If GPU is working with this buffer, glMapBuffer() will wait(stall) until GPU to finish its job.
            // To avoid waiting (idle), you can call first glBufferData() with null pointer before glMapBuffer().
            // If you do that, the previous data in PBO will be discarded and glMapBuffer() returns a new
            // allocated pointer immediately even if GPU is still working with the previous data.

            // Another option: map the buffer object into client's memory
            // Probably this code could be moved somewhere else...
            // batch.vertexBuffer[batch.currentBuffer].vertices = (float *)glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
            // if (batch.vertexBuffer[batch.currentBuffer].vertices)
            // {
            // Update vertex data
            // }
            // glUnmapBuffer(GL_ARRAY_BUFFER);

            // Unbind the current VAO
            if(rlglData.getExtSupported().isVao()){
                glBindVertexArray(0);
            }
        }

        //------------------------------------------------------------------------------------------------------------
        // Draw batch vertex buffers (considering VR stereo if required)
        //------------------------------------------------------------------------------------------------------------
        Matrix matProjection = rlglData.getState().getProjection();
        Matrix matModelView = rlglData.getState().getModelview();

        //System.out.println("\n" + Arrays.toString(MatrixToFloat(matProjection)));
        //System.out.println(Arrays.toString(MatrixToFloat(matModelView)) + "\n");

        int eyesCount = (rlglData.getVr().isStereoRender()) ? 2 : 1;


        for(int eye = 0; eye < eyesCount; eye++){
            if(SUPPORT_VR_SIMULATOR){
                if(eyesCount == 2){
                    SetStereoView(eye, matProjection, matModelView);
                }
            }
            // Draw buffers
            if(batch.vertexBuffer[batch.currentBuffer].vCounter > 0){
                // Set current shader and upload current MVP matrix
                glUseProgram(rlglData.getState().getCurrentShader().getId());

                // Create modelview-projection matrix and upload to shader
                Matrix matMVP = MatrixMultiply(rlglData.getState().getModelview(), rlglData.getState().getProjection());
                glUniformMatrix4fv(rlglData.getState().getCurrentShader().locs[LOC_MATRIX_MVP.getShaderLocationInt()],
                        false, MatrixToFloat(matMVP));

                System.out.println("\n" + Arrays.toString(MatrixToFloat(matProjection)));
                System.out.println(Arrays.toString(MatrixToFloat(matModelView)));
                System.out.println( Arrays.toString(MatrixToFloat(matMVP))+ "\n");

                if(rlglData.getExtSupported().isVao()){
                    glBindVertexArray(batch.vertexBuffer[batch.currentBuffer].vaoId);
                }
                else{
                    // Bind vertex attrib: position (shader-location = 0)
                    glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[0]);
                    glVertexAttribPointer(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_POSITION.getShaderLocationInt()],
                            3, GL_FLOAT, false, 0, 0);
                    glEnableVertexAttribArray(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_POSITION.getShaderLocationInt()]);

                    // Bind vertex attrib: texcoord (shader-location = 1)
                    glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[1]);
                    glVertexAttribPointer(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_TEXCOORD01.getShaderLocationInt()],
                            2, GL_FLOAT, false, 0, 0);
                    glEnableVertexAttribArray(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_TEXCOORD01.getShaderLocationInt()]);

                    // Bind vertex attrib: color (shader-location = 3)
                    glBindBuffer(GL_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[2]);
                    glVertexAttribPointer(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_COLOR.getShaderLocationInt()],
                            4, GL_FLOAT, false, 0, 0);
                    glEnableVertexAttribArray(rlglData.getState().getCurrentShader().locs[LOC_VERTEX_COLOR.getShaderLocationInt()]);

                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batch.vertexBuffer[batch.currentBuffer].vboId[3]);
                }

                // Setup some default shader values
                glUniform4f(rlglData.getState().getCurrentShader().locs[LOC_COLOR_DIFFUSE.getShaderLocationInt()], 1.0f, 1.0f, 1.0f, 1.0f);
                glUniform1i(rlglData.getState().getCurrentShader().locs[LOC_MAP_DIFFUSE.getShaderLocationInt()], 0);
                // Active default sampler2D: texture0

                // Activate additional sampler textures
                // Those additional textures will be common for all draw calls of the batch
                for(int i = 0; i < MAX_BATCH_ACTIVE_TEXTURES; i++){
                    if(rlglData.getState().getActiveTextureId()[i] > 0){
                        glActiveTexture(GL_TEXTURE0 + 1 + i);
                        glBindTexture(GL_TEXTURE_2D, rlglData.getState().getActiveTextureId()[i]);
                    }
                }

                // Activate default sampler2D texture0 (one texture is always active for default batch shader)
                // NOTE: Batch system accumulates calls by texture0 changes, additional textures are enabled for all the draw calls
                glActiveTexture(GL_TEXTURE0);

                for(int i = 0, vertexOffset = 0; i < batch.drawsCounter; i++){
                    // Bind current draw call texture, activated as GL_TEXTURE0 and binded to sampler2D texture0 by default
                    glBindTexture(GL_TEXTURE_2D, batch.draws[i].textureId);

                    if((batch.draws[i].mode == RL_LINES) || (batch.draws[i].mode == RL_TRIANGLES)){
                        glDrawArrays(batch.draws[i].mode, vertexOffset, batch.draws[i].vertexCount);
                    }
                    else{
                        if(GRAPHICS_API_OPENGL_33){
                            // We need to define the number of indices to be processed: quadsCount*6
                            // NOTE: The final parameter tells the GPU the offset in bytes from the
                            // start of the index buffer to the location of the first index to process
                            //glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_INT, (vertexOffset / 4 * 6L * (Integer.BYTES)));
                            glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_INT, 0);
                            //glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount, GL_UNSIGNED_INT, 0);
                        }
                        else if(GRAPHICS_API_OPENGL_ES2){
                            glDrawElements(GL_TRIANGLES, batch.draws[i].vertexCount / 4 * 6, GL_UNSIGNED_SHORT, 0);
                        }
                    }

                    vertexOffset += (batch.draws[i].vertexCount + batch.draws[i].vertexAlignment);
                }

                if(!rlglData.getExtSupported().isVao()){
                    glBindBuffer(GL_ARRAY_BUFFER, 0);
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                }

                glBindTexture(GL_TEXTURE_2D, 0);    // Unbind textures
            }

            if(rlglData.getExtSupported().isVao()){
                glBindVertexArray(0); // Unbind VAO
            }

            glUseProgram(0);    // Unbind shader program
        }
        //------------------------------------------------------------------------------------------------------------
        // Reset batch buffers
        //------------------------------------------------------------------------------------------------------------
        // Reset vertex counters for next frame
        batch.vertexBuffer[batch.currentBuffer].vCounter = 0;
        batch.vertexBuffer[batch.currentBuffer].tcCounter = 0;
        batch.vertexBuffer[batch.currentBuffer].cCounter = 0;

        // Reset depth for next draw
        batch.currentDepth = -1.0f;

        // Restore projection/modelview matrices
        rlglData.getState().setProjection(matProjection);
        rlglData.getState().setModelview(matModelView);

        // Reset rlgl.currentBatch.draws array
        for(int i = 0; i < DEFAULT_BATCH_DRAWCALLS; i++){
            batch.draws[i].mode = RL_QUADS;
            batch.draws[i].vertexCount = 0;
            batch.draws[i].textureId = rlglData.getState().getDefaultTextureId();
        }

        // Reset active texture units for next batch
        for(int i = 0; i < MAX_BATCH_ACTIVE_TEXTURES; i++){
            rlglData.getState().getActiveTextureId()[i] = 0;
        }

        // Reset draws counter to one draw for the batch
        batch.drawsCounter = 1;
        //------------------------------------------------------------------------------------------------------------

        // Change to next buffer in the list (in case of multi-buffering)
        batch.currentBuffer++;
        if(batch.currentBuffer >= batch.buffersCount){
            batch.currentBuffer = 0;
        }
    }

    // Unload default internal buffers vertex data from CPU and GPU
    static void UnloadRenderBatch(RenderBatch batch){
        // Unbind everything
        if(rlglData.getExtSupported().isVao()){
            glBindVertexArray(0);
        }
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Unload all vertex buffers data
        for(int i = 0; i < batch.buffersCount; i++){
            // Delete VBOs from GPU (VRAM)
            glDeleteBuffers(batch.vertexBuffer[i].vboId[0]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[1]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[2]);
            glDeleteBuffers(batch.vertexBuffer[i].vboId[3]);

            // Delete VAOs from GPU (VRAM)
            if(rlglData.getExtSupported().isVao()){
                glDeleteVertexArrays(batch.vertexBuffer[i].vaoId);
            }
        }

        // Unload arrays
        batch.setVertexBuffer(null);
        batch.setDraws(null);
    }

    // Set the active render batch for rlgl
    static void SetRenderBatchActive(RenderBatch batch){
        DrawRenderBatch(rlglData.getCurrentBatch());
        rlglData.setCurrentBatch(batch);
    }

    // Set default render batch for rlgl
    static void SetRenderBatchDefault(){
        DrawRenderBatch(rlglData.getCurrentBatch());
        rlglData.setCurrentBatch(rlglData.getDefaultBatch());
    }

    // Renders a 1x1 XY quad in NDC
    public static void GenDrawQuad() {
        int quadVAO = 0;
        int quadVBO = 0;

        float vertices[] = {
                // Positions         Texcoords
                -0.5f,  0.5f, 0.0f,  -0.5f, 0.5f,
                -0.5f, -0.5f, 0.0f,  -0.5f,-0.5f,
                 0.5f,  0.5f, 0.0f,   0.5f, 1.5f,
                 0.5f, -0.5f, 0.0f,   0.5f,-0.5f,
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
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5*Float.BYTES,0); //Positions
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5*Float.BYTES,3*Float.BYTES); //Texcoords

        // Draw quad
        glBindVertexArray(quadVAO);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);

        // Delete buffers (VBO and VAO)
        glDeleteBuffers(quadVBO);
        glDeleteVertexArrays(quadVAO);
    }

    public static void GenDrawQuadAlt() {
        int quadVAO;
        int[] quadVBO = {0, 0, 0, 0};
        Shader shaderProgram = rlglData.getState().getCurrentShader();

        float[] vertices = {
                -0.5f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f,  0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };

        float[] texcoords = {
                0.5f, -0.5f,
                -0.5f, -0.5f,
                0.5f,  0.5f,
                0.5f, -0.5f
        };

        float[] colour = {
                1, 0.4274509f, 0.6079216f, 1,
                1, 0.4274509f, 0.6079216f, 1,
                1, 0.4274509f, 0.6079216f, 1,
                1, 0.4274509f, 0.6079216f, 1
        };

        int[] indices = {
                0, 1, 2,
                0, 2, 3
        };

        // Gen VAO to contain VBO
        quadVAO = glGenVertexArrays();
        glBindVertexArray(quadVAO);

        // Gen and fill vertex buffer (VBO)
        quadVBO[0] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0,0); //Positions
        glEnableVertexAttribArray(0);

        quadVBO[1] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO[1]);
        glBufferData(GL_ARRAY_BUFFER, texcoords, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0,0); //Texcoords
        glEnableVertexAttribArray(1);

        quadVBO[2] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO[2]);
        glBufferData(GL_ARRAY_BUFFER, colour, GL_STATIC_DRAW);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0); //Colours
        glEnableVertexAttribArray(3);

        quadVBO[3] = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO[3]);
        glBufferData(GL_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Draw quad
        glUseProgram(shaderProgram.getId());
        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertices.length/3);
        glBindVertexArray(0);
        glUseProgram(0);

        // Delete buffers (VBO and VAO)
        glDeleteBuffers(quadVBO);
        glDeleteVertexArrays(quadVAO);
    }


    // Renders a 1x1 3D cube in NDC
    public static void GenDrawCube() {
        int cubeVAO = 0;
        int cubeVBO = 0;

        float vertices[] = {
                // Positions          Normals               Texcoords
                -1.0f, -1.0f, -1.0f,   0.0f,  0.0f, -1.0f,   0.0f, 0.0f,
                1.0f,  1.0f, -1.0f,   0.0f,  0.0f, -1.0f,   1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,   0.0f,  0.0f, -1.0f,   1.0f, 0.0f,
                1.0f,  1.0f, -1.0f,   0.0f,  0.0f, -1.0f,   1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,   0.0f,  0.0f, -1.0f,   0.0f, 0.0f,
                -1.0f,  1.0f, -1.0f,   0.0f,  0.0f, -1.0f,   0.0f, 1.0f,
                -1.0f, -1.0f,  1.0f,   0.0f,  0.0f,  1.0f,   0.0f, 0.0f,
                1.0f, -1.0f,  1.0f,   0.0f,  0.0f,  1.0f,   1.0f, 0.0f,
                1.0f,  1.0f,  1.0f,   0.0f,  0.0f,  1.0f,   1.0f, 1.0f,
                1.0f,  1.0f,  1.0f,   0.0f,  0.0f,  1.0f,   1.0f, 1.0f,
                -1.0f,  1.0f,  1.0f,   0.0f,  0.0f,  1.0f,   0.0f, 1.0f,
                -1.0f, -1.0f,  1.0f,   0.0f,  0.0f,  1.0f,   0.0f, 0.0f,
                -1.0f,  1.0f,  1.0f,  -1.0f,  0.0f,  0.0f,   1.0f, 0.0f,
                -1.0f,  1.0f, -1.0f,  -1.0f,  0.0f,  0.0f,   1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,  -1.0f,  0.0f,  0.0f,   0.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,  -1.0f,  0.0f,  0.0f,   0.0f, 1.0f,
                -1.0f, -1.0f,  1.0f,  -1.0f,  0.0f,  0.0f,   0.0f, 0.0f,
                -1.0f,  1.0f,  1.0f,  -1.0f,  0.0f,  0.0f,   1.0f, 0.0f,
                1.0f,  1.0f,  1.0f,   1.0f,  0.0f,  0.0f,   1.0f, 0.0f,
                1.0f, -1.0f, -1.0f,   1.0f,  0.0f,  0.0f,   0.0f, 1.0f,
                1.0f,  1.0f, -1.0f,   1.0f,  0.0f,  0.0f,   1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,   1.0f,  0.0f,  0.0f,   0.0f, 1.0f,
                1.0f,  1.0f,  1.0f,   1.0f,  0.0f,  0.0f,   1.0f, 0.0f,
                1.0f, -1.0f,  1.0f,   1.0f,  0.0f,  0.0f,   0.0f, 0.0f,
                -1.0f, -1.0f, -1.0f,   0.0f, -1.0f,  0.0f,   0.0f, 1.0f,
                1.0f, -1.0f, -1.0f,   0.0f, -1.0f,  0.0f,   1.0f, 1.0f,
                1.0f, -1.0f,  1.0f,   0.0f, -1.0f,  0.0f,   1.0f, 0.0f,
                1.0f, -1.0f,  1.0f,   0.0f, -1.0f,  0.0f,   1.0f, 0.0f,
                -1.0f, -1.0f,  1.0f,   0.0f, -1.0f,  0.0f,   0.0f, 0.0f,
                -1.0f, -1.0f, -1.0f,   0.0f, -1.0f,  0.0f,   0.0f, 1.0f,
                -1.0f,  1.0f, -1.0f,   0.0f,  1.0f,  0.0f,   0.0f, 1.0f,
                1.0f,  1.0f,  1.0f,   0.0f,  1.0f,  0.0f,   1.0f, 0.0f,
                1.0f,  1.0f, -1.0f,   0.0f,  1.0f,  0.0f,   1.0f, 1.0f,
                1.0f,  1.0f,  1.0f,   0.0f,  1.0f,  0.0f,   1.0f, 0.0f,
                -1.0f,  1.0f, -1.0f,   0.0f,  1.0f,  0.0f,   0.0f, 1.0f,
                -1.0f,  1.0f,  1.0f,   0.0f,  1.0f,  0.0f,   0.0f, 0.0f
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
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8*Float.BYTES,0); //Positions
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8*Float.BYTES,3*Float.BYTES); //Normals
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8*Float.BYTES,6*Float.BYTES); //Texcoords
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


    //SUPPORT_VR_SIMULATOR
    // Set internal projection and modelview matrix depending on eyes tracking data
    static void SetStereoView(int eye, Matrix matProjection, Matrix matModelView){
        Matrix eyeProjection = matProjection;
        Matrix eyeModelView = matModelView;

        // Setup viewport and projection/modelview matrices using tracking data
        rlViewport(eye * rlglData.getState().getFramebufferWidth() / 2, 0, rlglData.getState().getFramebufferWidth() / 2,
                rlglData.getState().getFramebufferHeight());

        // Apply view offset to modelview matrix
        eyeModelView = MatrixMultiply(matModelView, rlglData.getVr().getConfig().getEyesViewOffset()[eye]);

        // Set current eye projection matrix
        eyeProjection = rlglData.getVr().getConfig().getEyesProjection()[eye];

        SetMatrixModelview(eyeModelView);
        SetMatrixProjection(eyeProjection);
    }

    //////////////////
    //*****GL11*****//
    //////////////////

    // Mipmaps data is generated after image data
    // NOTE: Only works with RGBA (4 bytes) data!
    static int GenerateMipmaps(int[] data, int baseWidth, int baseHeight){
        int mipmapCount = 1;                // Required mipmap levels count (including base level)
        int width = baseWidth;
        int height = baseHeight;
        int size = baseWidth * baseHeight * 4;  // Size in bytes (will include mipmaps...), RGBA only

        // Count mipmap levels required
        while((width != 1) && (height != 1)){
            width /= 2;
            height /= 2;

            TracelogS("TEXTURE: Next mipmap size: " + width + " x " + height);

            mipmapCount++;

            size += (width * height * 4);       // Add mipmap size (in bytes)
        }

        TracelogS("TEXTURE: Total mipmaps required: " + mipmapCount);
        TracelogS("TEXTURE: Total size of data required: " + size);

        int[] temp = new int[data.length];

        if(temp != null){
            data = temp;
        }
        else{
            Tracelog(LOG_WARNING, "TEXTURE: Failed to re-allocate required mipmaps memory");
        }

        width = baseWidth;
        height = baseHeight;
        size = (width * height * 4);

        // Generate mipmaps
        // NOTE: Every mipmap data is stored after data
        Color[] image = new Color[width * height];
        Color[] mipmap = null;
        int offset = 0;
        int j = 0;

        for(int i = 0; i < size; i += 4){
            image[j].setR((byte) data[i]);
            image[j].setG((byte) data[i + 1]);
            image[j].setB((byte) data[i + 2]);
            image[j].setA((byte) data[i + 3]);

            j++;
        }

        TracelogS("TEXTURE: Mipmap base size (" + width + "x" + height + ")");

        for(int mip = 1; mip < mipmapCount; mip++){
            mipmap = GenNextMipmap(image, width, height);

            offset += (width * height * 4); // Size of last mipmap
            j = 0;

            width /= 2;
            height /= 2;
            size = (width * height * 4);    // Mipmap size to store after offset

            // Add mipmap to data
            for(int i = 0; i < size; i += 4){
                data[offset + i] = mipmap[j].getR();
                data[offset + i + 1] = mipmap[j].getG();
                data[offset + i + 2] = mipmap[j].getB();
                data[offset + i + 3] = mipmap[j].getA();
                j++;
            }

            image = null;
            mipmap = null;
        }

        mipmap = null;       // free mipmap data

        return mipmapCount;
    }

    // Manual mipmap generation (basic scaling algorithm)
    static Color[] GenNextMipmap(Color[] srcData, int srcWidth, int srcHeight){
        int x2, y2;
        Color prow = new Color();
        Color pcol = new Color();

        int width = srcWidth / 2;
        int height = srcHeight / 2;

        Color[] mipmap = new Color[width * height];

        // Scaling algorithm works perfectly (box-filter)
        for(int y = 0; y < height; y++){
            y2 = 2 * y;

            for(int x = 0; x < width; x++){
                x2 = 2 * x;

                prow.setR(((srcData[y2 * srcWidth + x2].getR() + srcData[y2 * srcWidth + x2 + 1].getR()) / 2));
                prow.setG(((srcData[y2 * srcWidth + x2].getG() + srcData[y2 * srcWidth + x2 + 1].getG()) / 2));
                prow.setB(((srcData[y2 * srcWidth + x2].getB() + srcData[y2 * srcWidth + x2 + 1].getB()) / 2));
                prow.setA(((srcData[y2 * srcWidth + x2].getA() + srcData[y2 * srcWidth + x2 + 1].getA()) / 2));

                pcol.setR(((srcData[(y2 + 1) * srcWidth + x2].getR() + srcData[(y2 + 1) * srcWidth + x2 + 1].getR()) / 2));
                pcol.setG(((srcData[(y2 + 1) * srcWidth + x2].getG() + srcData[(y2 + 1) * srcWidth + x2 + 1].getG()) / 2));
                pcol.setB(((srcData[(y2 + 1) * srcWidth + x2].getB() + srcData[(y2 + 1) * srcWidth + x2 + 1].getB()) / 2));
                pcol.setA(((srcData[(y2 + 1) * srcWidth + x2].getA() + srcData[(y2 + 1) * srcWidth + x2 + 1].getA()) / 2));

                mipmap[y * width + x].setR((prow.getR() + pcol.getR()) / 2);
                mipmap[y * width + x].setG((prow.getG() + pcol.getG()) / 2);
                mipmap[y * width + x].setB((prow.getB() + pcol.getB()) / 2);
                mipmap[y * width + x].setA((prow.getA() + pcol.getA()) / 2);
            }
        }

        TracelogS("TEXTURE: Mipmap generated successfully (" + width + "x" + height + ")");

        return mipmap;
    }

    //LoadFileText
    //NOTE: SEE Utils/Files.LoadFileText(String fileName)

    public static int GetPixelDataSize(int width, int height, int format){
        int dataSize = 0;       // Size in bytes
        int bpp = 0;            // Bits per pixel

        PixelFormat pixForm = PixelFormat.getByInt(format);

        switch(pixForm){
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
        if((width < 4) && (height < 4)){
            if((format >= COMPRESSED_DXT1_RGB.getPixForInt()) && (format < COMPRESSED_DXT3_RGBA.getPixForInt())){
                dataSize = 8;
            }
            else if((format >= COMPRESSED_DXT3_RGBA.getPixForInt()) && (format < COMPRESSED_ASTC_8x8_RGBA.getPixForInt())){
                dataSize = 16;
            }
        }

        return dataSize;
    }

}