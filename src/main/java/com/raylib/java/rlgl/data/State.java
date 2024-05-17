package com.raylib.java.rlgl.data;

import com.raylib.java.raymath.Matrix;

import static com.raylib.java.rlgl.RLGL.*;

public  class State{                    // Renderer state

    public int vertexCounter;                  // Current active render batch vertex counter (generic, used for all batches)
    public float texcoordx, texcoordy;         // Current active texture coordinate (added on glVertex*())
    public float normalx, normaly, normalz;    // Current active normal (added on glVertex*())
    public byte colorr, colorg, colorb, colora; // Current active color (added on glVertex*())

    public int currentMatrixMode;              // Current matrix mode

    public Matrix currentMatrix;
    public Matrix modelview;                   // Default modelview matrix
    public Matrix projection;                  // Default projection matrix
    public Matrix transform;                   // Transform matrix to be used with rlTranslate, rlRotate, rlScale
    public boolean transformRequired;          // Require transform matrix application to current draw-call vertex (if required)
    public Matrix[] stack;                     // Matrix stack for push/pop
    public int stackCounter;                   // Matrix stack counter

    public int defaultTextureId;      // Default texture used on shapes/poly drawing (required by shader)
    public int[] activeTextureId;     // Active texture ids to be enabled on batch drawing (0 active by default)
    public int defaultVShaderId;      // Default vertex shader id (used by default shader program)
    public int defaultFShaderId;      // Default fragment shader id (used by default shader program)
    public int defaultShaderId;       // Default shader program id, supports vertex color and diffuse texture
    public int[] defaultShaderLocs;     // Default shader locations pointer to be used on rendering
    public int currentShaderId;       // Current shader id to be used on rendering (by default, defaultShaderId)
    public int[] currentShaderLocs;     // Current shader locations pointer to be used on rendering (by default, defaultShaderLocs)

    public boolean stereoRender;              // Stereo rendering flag
    public Matrix[] projectionStereo;         // VR stereo rendering eyes projection matrices
    public Matrix[] viewOffsetStereo;         // VR stereo rendering eyes view offset matrices

    public int currentBlendMode;               // Blending mode active
    public int glBlendSrcFactor;               // Blending source factor
    public int glBlendDstFactor;               // Blending destination factor
    public int glBlendEquation;                // Blending equation

    public int framebufferWidth;               // Current framebuffer width
    public int framebufferHeight;              // Current framebuffer height

    public State(){
        stack = new Matrix[MAX_MATRIX_STACK_SIZE];
        activeTextureId = new int[MAX_BATCH_ACTIVE_TEXTURES];
        projectionStereo = new Matrix[2];
        viewOffsetStereo = new Matrix[2];
        currentMatrix = new Matrix();
        modelview = new Matrix();
        projection = new Matrix();
    }

    public Matrix getCurrentMatrix(){
        return currentMatrix;
    }

    public void setCurrentMatrix(Matrix currentMatrix){
        this.currentMatrix = currentMatrix;

        if (currentMatrixMode == RL_MODELVIEW) {
            modelview = this.currentMatrix;
        }
        else if (currentMatrixMode == RL_PROJECTION) {
            projection = this.currentMatrix;
        }
        else if (currentMatrixMode == RLJ_TRANSFORM) {
            transform = this.currentMatrix;
        }
    }

    public int getVertexCounter(){
        return vertexCounter;
    }

    public void setVertexCounter(int vertexCounter){
        this.vertexCounter = vertexCounter;
    }

    public float getTexcoordx(){
        return texcoordx;
    }

    public void setTexcoordx(float texcoordx){
        this.texcoordx = texcoordx;
    }

    public float getTexcoordy(){
        return texcoordy;
    }

    public void setTexcoordy(float texcoordy){
        this.texcoordy = texcoordy;
    }

    public float getNormalx(){
        return normalx;
    }

    public void setNormalx(float normalx){
        this.normalx = normalx;
    }

    public float getNormaly(){
        return normaly;
    }

    public void setNormaly(float normaly){
        this.normaly = normaly;
    }

    public float getNormalz(){
        return normalz;
    }

    public void setNormalz(float normalz){
        this.normalz = normalz;
    }

    public int getColorr(){
        return colorr;
    }

    public void setColorr(byte colorr){
        this.colorr = colorr;
    }

    public int getColorg(){
        return colorg;
    }

    public void setColorg(byte colorg){
        this.colorg = colorg;
    }

    public int getColorb(){
        return colorb;
    }

    public void setColorb(byte colorb){
        this.colorb = colorb;
    }

    public int getColora(){
        return colora;
    }

    public void setColora(byte colora){
        this.colora = colora;
    }

    public int getCurrentMatrixMode(){
        return currentMatrixMode;
    }

    public void setCurrentMatrixMode(int currentMatrixMode){
        if (this.currentMatrixMode == RL_MODELVIEW) {
            modelview = currentMatrix;
        }
        else if (this.currentMatrixMode == RL_PROJECTION) {
            projection = currentMatrix;
        }
        else if (this.currentMatrixMode == RLJ_TRANSFORM) {
            transform = currentMatrix;
        }

        this.currentMatrixMode = currentMatrixMode;

        if (currentMatrixMode == RL_MODELVIEW) {
            this.currentMatrix = modelview;
        }
        else if (currentMatrixMode == RL_PROJECTION) {
            this.currentMatrix = projection;
        }
        else if (currentMatrixMode == RLJ_TRANSFORM) {
            this.currentMatrix = transform;
        }
    }

    public Matrix getModelview(){
        return modelview;
    }

    public void setModelview(Matrix modelview){
        this.modelview = modelview;
    }

    public Matrix getProjection(){
        return projection;
    }

    public void setProjection(Matrix projection){
        this.projection = projection;
    }

    public Matrix getTransform(){
        return transform;
    }

    public void setTransform(Matrix transform){
        this.transform = transform;
    }

    public boolean isTransformRequired(){
        return transformRequired;
    }

    public void setTransformRequired(boolean transformRequired){
        this.transformRequired = transformRequired;
    }

    public Matrix[] getStack(){
        return stack;
    }

    public void setStack(Matrix[] stack){
        this.stack = stack;
    }

    public int getStackCounter(){
        return stackCounter;
    }

    public void setStackCounter(int stackCounter){
        this.stackCounter = stackCounter;
    }

    public int getDefaultTextureId(){
        return defaultTextureId;
    }

    public void setDefaultTextureId(int defaultTextureId){
        this.defaultTextureId = defaultTextureId;
    }

    public int[] getActiveTextureId(){
        return activeTextureId;
    }

    public void setActiveTextureId(int[] activeTextureId){
        this.activeTextureId = activeTextureId;
    }

    public int getDefaultVShaderId(){
        return defaultVShaderId;
    }

    public void setDefaultVShaderId(int defaultVShaderId){
        this.defaultVShaderId = defaultVShaderId;
    }

    public int getDefaultFShaderId(){
        return defaultFShaderId;
    }

    public void setDefaultFShaderId(int defaultFShaderId){
        this.defaultFShaderId = defaultFShaderId;
    }

    public int getDefaultShaderId(){
        return defaultShaderId;
    }

    public void setDefaultShaderId(int defaultShaderId){
        this.defaultShaderId = defaultShaderId;
    }

    public int[] getDefaultShaderLocs(){
        return defaultShaderLocs;
    }

    public void setDefaultShaderLocs(int[] defaultShaderLocs){
        this.defaultShaderLocs = defaultShaderLocs;
    }

    public int getCurrentShaderId(){
        return currentShaderId;
    }

    public void setCurrentShaderId(int currentShaderId){
        this.currentShaderId = currentShaderId;
    }

    public int[] getCurrentShaderLocs(){
        return currentShaderLocs;
    }

    public void setCurrentShaderLocs(int[] currentShaderLocs){
        this.currentShaderLocs = currentShaderLocs;
    }

    public boolean isStereoRender(){
        return stereoRender;
    }

    public void setStereoRender(boolean stereoRender){
        this.stereoRender = stereoRender;
    }

    public Matrix[] getProjectionStereo(){
        return projectionStereo;
    }

    public void setProjectionStereo(Matrix[] projectionStereo){
        this.projectionStereo = projectionStereo;
    }

    public Matrix[] getViewOffsetStereo(){
        return viewOffsetStereo;
    }

    public void setViewOffsetStereo(Matrix[] viewOffsetStereo){
        this.viewOffsetStereo = viewOffsetStereo;
    }

    public int getCurrentBlendMode(){
        return currentBlendMode;
    }

    public void setCurrentBlendMode(int currentBlendMode){
        this.currentBlendMode = currentBlendMode;
    }

    public int getGlBlendSrcFactor(){
        return glBlendSrcFactor;
    }

    public void setGlBlendSrcFactor(int glBlendSrcFactor){
        this.glBlendSrcFactor = glBlendSrcFactor;
    }

    public int getGlBlendDstFactor(){
        return glBlendDstFactor;
    }

    public void setGlBlendDstFactor(int glBlendDstFactor){
        this.glBlendDstFactor = glBlendDstFactor;
    }

    public int getGlBlendEquation(){
        return glBlendEquation;
    }

    public void setGlBlendEquation(int glBlendEquation){
        this.glBlendEquation = glBlendEquation;
    }

    public int getFramebufferWidth(){
        return framebufferWidth;
    }

    public void setFramebufferWidth(int framebufferWidth){
        this.framebufferWidth = framebufferWidth;
    }

    public int getFramebufferHeight(){
        return framebufferHeight;
    }

    public void setFramebufferHeight(int framebufferHeight){
        this.framebufferHeight = framebufferHeight;
    }
}
