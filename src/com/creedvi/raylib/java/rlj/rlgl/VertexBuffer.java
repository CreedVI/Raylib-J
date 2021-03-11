package com.creedvi.raylib.java.rlj.rlgl;

public class VertexBuffer{

    int elementsCount;          // Number of elements in the buffer (QUADS)
    int vCounter;               // Vertex position counter to process (and draw) from full buffer
    int tcCounter;              // Vertex texcoord counter to process (and draw) from full buffer
    int cCounter;               // Vertex color counter to process (and draw) from full buffer
    float[] vertices;            // Vertex position (XYZ - 3 components per vertex) (shader-location = 0)
    float[] texcoords;           // Vertex texture coordinates (UV - 2 components per vertex) (shader-location = 1)
    int[] colors;              // Vertex colors (RGBA - 4 components per vertex) (shader-location = 3)
    int[] indices_GL11;      // Vertex indices (in case vertex data comes indexed) (6 indices per quad)
    short[] indices_ES20;    // Vertex indices (in case vertex data comes indexed) (6 indices per quad)
    int vaoId;         // OpenGL Vertex Array Object id
    int[] vboId;      // OpenGL Vertex Buffer Objects id (4 types of vertex data)

    public VertexBuffer(){
        vertices = new float[3];
        texcoords = new float[2];
        colors = new int[64];
        indices_GL11 = new int[6];
        indices_ES20 = new short[6];
        vboId = new int[4];
    }

    public int getElementsCount(){
        return elementsCount;
    }

    public void setElementsCount(int elementsCount){
        this.elementsCount = elementsCount;
    }

    public int getvCounter(){
        return vCounter;
    }

    public void setvCounter(int vCounter){
        this.vCounter = vCounter;
    }

    public int getTcCounter(){
        return tcCounter;
    }

    public void setTcCounter(int tcCounter){
        this.tcCounter = tcCounter;
    }

    public int getcCounter(){
        return cCounter;
    }

    public void setcCounter(int cCounter){
        this.cCounter = cCounter;
    }

    public float[] getVertices(){
        return vertices;
    }

    public void setVertices(float[] vertices){
        this.vertices = vertices;
    }

    public float[] getTexcoords(){
        return texcoords;
    }

    public void setTexcoords(float[] texcoords){
        this.texcoords = texcoords;
    }

    public int[] getColors(){
        return colors;
    }

    public void setColors(int[] colors){
        this.colors = colors;
    }

    public int[] getIndices_GL11(){
        return indices_GL11;
    }

    public void setIndices_GL11(int[] indices_GL11){
        this.indices_GL11 = indices_GL11;
    }

    public short[] getIndices_ES20(){
        return indices_ES20;
    }

    public void setIndices_ES20(short[] indices_ES20){
        this.indices_ES20 = indices_ES20;
    }

    public int getVaoId(){
        return vaoId;
    }

    public void setVaoId(int vaoId){
        this.vaoId = vaoId;
    }

    public int[] getVboId(){
        return vboId;
    }

    public void setVboId(int[] vboId){
        this.vboId = vboId;
    }
}
