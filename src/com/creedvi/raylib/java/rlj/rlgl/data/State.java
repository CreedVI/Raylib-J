package com.creedvi.raylib.java.rlj.rlgl.data;

import com.creedvi.raylib.java.rlj.raymath.Matrix;
import com.creedvi.raylib.java.rlj.rlgl.shader.Shader;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;

public  class State{                    // Renderer state
    int currentMatrixMode;              // Current matrix mode
    Matrix modelview;                   // Default modelview matrix
    Matrix projection;                  // Default projection matrix
    Matrix transform;                   // Transform matrix to be used with rlTranslate, rlRotate, rlScale
    boolean transformRequired;          // Require transform matrix application to current draw-call vertex (if required)
    Matrix[] stack;                     // Matrix stack for push/pop
    int stackCounter;                   // Matrix stack counter

    Texture2D shapesTexture;            // Texture used on shapes drawing (usually a white pixel)
    Rectangle shapesTextureRec;         // Texture source rectangle used on shapes drawing
    int defaultTextureId;               // Default texture used on shapes/poly drawing (required by shader)
    int[] activeTextureId;              // Active texture ids to be enabled on batch drawing (0 active by default)
    int defaultVShaderId;               // Default vertex shader id (used by default shader program)
    int defaultFShaderId;               // Default fragment shader Id (used by default shader program)
    Shader defaultShader;               // Basic shader, support vertex color and diffuse texture
    Shader currentShader;               // Shader to be used on rendering (by default, defaultShader)

    int currentBlendMode;               // Blending mode active
    int glBlendSrcFactor;               // Blending source factor
    int glBlendDstFactor;               // Blending destination factor
    int glBlendEquation;                // Blending equation

    int framebufferWidth;               // Default framebuffer width
    int framebufferHeight;              // Default framebuffer height

    public State(){
        stack = new Matrix[MAX_MATRIX_STACK_SIZE];
        activeTextureId = new int[4];
    }

    public int getCurrentMatrixMode(){
        return currentMatrixMode;
    }

    public void setCurrentMatrixMode(int currentMatrixMode){
        this.currentMatrixMode = currentMatrixMode;
    }

    public Matrix getCurrentMatrix(){
        if(currentMatrixMode == RL_PROJECTION){
            return projection;
        }
        else {
            return modelview;
        }
    }

    public void setCurrentMatrix(Matrix currentMatrix){

        if(currentMatrixMode == RL_PROJECTION){
            projection = currentMatrix;
        }
        else if(currentMatrixMode == RL_MODELVIEW){
            modelview = currentMatrix;
        }
    }

    public void setCurrentMatrix(int mode, Matrix currentMatrix){

        if(mode == RL_PROJECTION){
            projection = currentMatrix;
        }
        else if(mode == RL_MODELVIEW){
            modelview = currentMatrix;
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

    public Texture2D getShapesTexture(){
        return shapesTexture;
    }

    public void setShapesTexture(Texture2D shapesTexture){
        this.shapesTexture = shapesTexture;
    }

    public Rectangle getShapesTextureRec(){
        return shapesTextureRec;
    }

    public void setShapesTextureRec(Rectangle shapesTextureRec){
        this.shapesTextureRec = shapesTextureRec;
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

    public Shader getDefaultShader(){
        return defaultShader;
    }

    public void setDefaultShader(Shader defaultShader){
        this.defaultShader = defaultShader;
    }

    public Shader getCurrentShader(){
        return currentShader;
    }

    public void setCurrentShader(Shader currentShader){
        this.currentShader = currentShader;
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
