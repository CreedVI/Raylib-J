package com.raylib.java.rlgl.data;

public class ExtSupported{
    
    public boolean vao;                           // VAO support (OpenGL ES2 could not support VAO extension) (GL_ARB_vertex_array_object)
    public boolean instancing;                    // Instancing supported (GL_ANGLE_instanced_arrays, GL_EXT_draw_instanced + GL_EXT_instanced_arrays)
    public boolean texNPOT;                       // NPOT textures full support (GL_ARB_texture_non_power_of_two, GL_OES_texture_npot)
    public boolean texDepth;                      // Depth textures supported (GL_ARB_depth_texture, GL_WEBGL_depth_texture, GL_OES_depth_texture)
    public boolean texFloat32;                    // float textures support (32 bit per channel) (GL_OES_texture_float)
    public boolean texCompDXT;                    // DDS texture compression support (GL_EXT_texture_compression_s3tc, GL_WEBGL_compressed_texture_s3tc, GL_WEBKIT_WEBGL_compressed_texture_s3tc)
    public boolean texCompETC1;                   // ETC1 texture compression support (GL_OES_compressed_ETC1_RGB8_texture, GL_WEBGL_compressed_texture_etc1)
    public boolean texCompETC2;                   // ETC2/EAC texture compression support (GL_ARB_ES3_compatibility)
    public boolean texCompPVRT;                   // PVR texture compression support (GL_IMG_texture_compression_pvrtc)
    public boolean texCompASTC;                   // ASTC texture compression support (GL_KHR_texture_compression_astc_hdr, GL_KHR_texture_compression_astc_ldr)
    public boolean texMirrorClamp;                // Clamp mirror wrap mode supported (GL_EXT_texture_mirror_clamp)
    public boolean texAnisoFilter;                // Anisotropic texture filtering support (GL_EXT_texture_filter_anisotropic)
    public boolean computeShader;                 // Compute shaders support (GL_ARB_compute_shader)
    public boolean ssbo;                          // Shader storage buffer object support (GL_ARB_shader_storage_buffer_object)

    public float maxAnisotropyLevel;           // Maximum anisotropy level supported (minimum is 2.0f)
    public int maxDepthBits;                   // Maximum bits for depth component

    public boolean isVao() {
        return vao;
    }

    public void setVao(boolean vao) {
        this.vao = vao;
    }

    public boolean isInstancing() {
        return instancing;
    }

    public void setInstancing(boolean instancing) {
        this.instancing = instancing;
    }

    public boolean isTexNPOT() {
        return texNPOT;
    }

    public void setTexNPOT(boolean texNPOT) {
        this.texNPOT = texNPOT;
    }

    public boolean isTexDepth() {
        return texDepth;
    }

    public void setTexDepth(boolean texDepth) {
        this.texDepth = texDepth;
    }

    public boolean isTexFloat32() {
        return texFloat32;
    }

    public void setTexFloat32(boolean texFloat32) {
        this.texFloat32 = texFloat32;
    }

    public boolean isTexCompDXT() {
        return texCompDXT;
    }

    public void setTexCompDXT(boolean texCompDXT) {
        this.texCompDXT = texCompDXT;
    }

    public boolean isTexCompETC1() {
        return texCompETC1;
    }

    public void setTexCompETC1(boolean texCompETC1) {
        this.texCompETC1 = texCompETC1;
    }

    public boolean isTexCompETC2() {
        return texCompETC2;
    }

    public void setTexCompETC2(boolean texCompETC2) {
        this.texCompETC2 = texCompETC2;
    }

    public boolean isTexCompPVRT(){
        return texCompPVRT;
    }

    public void setTexCompPVRT(boolean texCompPVRT){
        this.texCompPVRT = texCompPVRT;
    }

    public boolean isTexCompASTC(){
        return texCompASTC;
    }

    public void setTexCompASTC(boolean texCompASTC){
        this.texCompASTC = texCompASTC;
    }

    public boolean isTexMirrorClamp(){
        return texMirrorClamp;
    }

    public void setTexMirrorClamp(boolean texMirrorClamp){
        this.texMirrorClamp = texMirrorClamp;
    }

    public boolean isTexAnisoFilter(){
        return texAnisoFilter;
    }

    public void setTexAnisoFilter(boolean texAnisoFilter){
        this.texAnisoFilter = texAnisoFilter;
    }

    public boolean isComputeShader(){
        return computeShader;
    }

    public void setComputeShader(boolean computeShader){
        this.computeShader = computeShader;
    }

    public boolean isSsbo(){
        return ssbo;
    }

    public void setSsbo(boolean ssbo){
        this.ssbo = ssbo;
    }

    public float getMaxAnisotropyLevel(){
        return maxAnisotropyLevel;
    }

    public void setMaxAnisotropyLevel(float maxAnisotropyLevel){
        this.maxAnisotropyLevel = maxAnisotropyLevel;
    }

    public int getMaxDepthBits(){
        return maxDepthBits;
    }

    public void setMaxDepthBits(int maxDepthBits){
        this.maxDepthBits = maxDepthBits;
    }
    
}