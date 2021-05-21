package com.creedvi.raylib.java.rlj.rlgl.data;

public class ExtSupported{
    boolean vao;                           // VAO support (OpenGL ES2 could not support VAO extension)
    boolean texNPOT;                       // NPOT textures full support
    boolean texDepth;                      // Depth textures supported
    boolean texFloat32;                    // float textures support (32 bit per channel)
    boolean texCompDXT;                    // DDS texture compression support
    boolean texCompETC1;                   // ETC1 texture compression support
    boolean texCompETC2;                   // ETC2/EAC texture compression support
    boolean texCompPVRT;                   // PVR texture compression support
    boolean texCompASTC;                   // ASTC texture compression support
    boolean texMirrorClamp;                // Clamp mirror wrap mode supported
    boolean texAnisoFilter;                // Anisotropic texture filtering support
    boolean debugMarker;                   // Debug marker support

    float maxAnisotropicLevel;          // Maximum anisotropy level supported (minimum is 2.0f)
    int maxDepthBits;                   // Maximum bits for depth component

    public ExtSupported(){

    }

    public boolean isVao(){
        return vao;
    }

    public void setVao(boolean vao){
        this.vao = vao;
    }

    public boolean isTexNPOT(){
        return texNPOT;
    }

    public void setTexNPOT(boolean texNPOT){
        this.texNPOT = texNPOT;
    }

    public boolean isTexDepth(){
        return texDepth;
    }

    public void setTexDepth(boolean texDepth){
        this.texDepth = texDepth;
    }

    public boolean isTexFloat32(){
        return texFloat32;
    }

    public void setTexFloat32(boolean texFloat32){
        this.texFloat32 = texFloat32;
    }

    public boolean isTexCompDXT(){
        return texCompDXT;
    }

    public void setTexCompDXT(boolean texCompDXT){
        this.texCompDXT = texCompDXT;
    }

    public boolean isTexCompETC1(){
        return texCompETC1;
    }

    public void setTexCompETC1(boolean texCompETC1){
        this.texCompETC1 = texCompETC1;
    }

    public boolean isTexCompETC2(){
        return texCompETC2;
    }

    public void setTexCompETC2(boolean texCompETC2){
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

    public boolean isDebugMarker(){
        return debugMarker;
    }

    public void setDebugMarker(boolean debugMarker){
        this.debugMarker = debugMarker;
    }

    public float getMaxAnisotropicLevel(){
        return maxAnisotropicLevel;
    }

    public void setMaxAnisotropicLevel(float maxAnisotropicLevel){
        this.maxAnisotropicLevel = maxAnisotropicLevel;
    }

    public int getMaxDepthBits(){
        return maxDepthBits;
    }

    public void setMaxDepthBits(int maxDepthBits){
        this.maxDepthBits = maxDepthBits;
    }
}