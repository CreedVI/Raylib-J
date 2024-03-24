package com.raylib.java.rlgl;

import com.raylib.java.raymath.Matrix;

public class rlDrawCall{

    int mode;                   // Drawing mode: LINES, TRIANGLES, QUADS
    int vertexCount;            // Number of vertex of the draw
    int vertexAlignment;        // Number of vertex required for index alignment (LINES, TRIANGLES)
    int vaoId;                  // Vertex array id to be used on the draw . Using RLGL.currentBatch.rlVertexBuffer.vaoId
    int shaderId;               // Shader id to be used on the draw . Using RLGL.currentShaderId
    int textureId;              // Texture id to be used on the draw . Use to create new draw call if changes

    Matrix projection;        // Projection matrix for this draw . Using RLGL.projection
    Matrix modelview;         // Modelview matrix for this draw . Using RLGL.modelview

    public rlDrawCall(){

    }

    public int getMode(){
        return mode;
    }

    public void setMode(int mode){
        this.mode = mode;
    }

    public int getVertexCount(){
        return vertexCount;
    }

    public void setVertexCount(int vertexCount){
        this.vertexCount = vertexCount;
    }

    public int getVertexAlignment(){
        return vertexAlignment;
    }

    public void setVertexAlignment(int vertexAlignment){
        this.vertexAlignment = vertexAlignment;
    }

    public int getVaoId(){
        return vaoId;
    }

    public void setVaoId(int vaoId){
        this.vaoId = vaoId;
    }

    public int getShaderId(){
        return shaderId;
    }

    public void setShaderId(int shaderId){
        this.shaderId = shaderId;
    }

    public int getTextureId(){
        return textureId;
    }

    public void setTextureId(int textureId){
        this.textureId = textureId;
    }

    public Matrix getProjection(){
        return projection;
    }

    public void setProjection(Matrix projection){
        this.projection = projection;
    }

    public Matrix getModelview(){
        return modelview;
    }

    public void setModelview(Matrix modelview){
        this.modelview = modelview;
    }
}
