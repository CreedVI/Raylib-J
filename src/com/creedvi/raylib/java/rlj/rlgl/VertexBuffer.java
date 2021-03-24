package com.creedvi.raylib.java.rlj.rlgl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class VertexBuffer{

    int elementsCount;          // Number of elements in the buffer (QUADS)
    int vCounter;               // Vertex position counter to process (and draw) from full buffer
    int tcCounter;              // Vertex texcoord counter to process (and draw) from full buffer
    int cCounter;               // Vertex color counter to process (and draw) from full buffer
    FloatBuffer vertices;            // Vertex position (XYZ - 3 components per vertex) (shader-location = 0)
    FloatBuffer texcoords;           // Vertex texture coordinates (UV - 2 components per vertex) (shader-location = 1)
    IntBuffer colors;              // Vertex colors (RGBA - 4 components per vertex) (shader-location = 3)
    IntBuffer indices_GL11;      // Vertex indices (in case vertex data comes indexed) (6 indices per quad)
    ShortBuffer indices_ES20;    // Vertex indices (in case vertex data comes indexed) (6 indices per quad)
    int vaoId;         // OpenGL Vertex Array Object id
    IntBuffer vboId;      // OpenGL Vertex Buffer Objects id (4 types of vertex data)

    public VertexBuffer(){
        vertices = MemoryUtil.memAllocFloat(3);
        texcoords = MemoryUtil.memAllocFloat(2);
        colors = MemoryUtil.memAllocInt(64);
        indices_GL11 = MemoryUtil.memAllocInt(6);
        indices_ES20 = MemoryUtil.memAllocShort(6);
        vboId = MemoryUtil.memAllocInt(4);
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

    public FloatBuffer getVertices(){
        return vertices;
    }

    public void setVertices(FloatBuffer vertices){
        this.vertices = vertices;
    }

    public FloatBuffer getTexcoords(){
        return texcoords;
    }

    public void setTexcoords(FloatBuffer texcoords){
        this.texcoords = texcoords;
    }

    public IntBuffer getColors(){
        return colors;
    }

    public void setColors(IntBuffer colors){
        this.colors = colors;
    }

    public IntBuffer getIndices_GL11(){
        return indices_GL11;
    }

    public void setIndices_GL11(IntBuffer indices_GL11){
        this.indices_GL11 = indices_GL11;
    }

    public ShortBuffer getIndices_ES20(){
        return indices_ES20;
    }

    public void setIndices_ES20(ShortBuffer indices_ES20){
        this.indices_ES20 = indices_ES20;
    }

    public int getVaoId(){
        return vaoId;
    }

    public void setVaoId(int vaoId){
        this.vaoId = vaoId;
    }

    public IntBuffer getVboId(){
        return vboId;
    }

    public void setVboId(IntBuffer vboId){
        this.vboId = vboId;
    }
}
