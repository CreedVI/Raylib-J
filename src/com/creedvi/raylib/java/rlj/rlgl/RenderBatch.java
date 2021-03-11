package com.creedvi.raylib.java.rlj.rlgl;

public class RenderBatch{

    int buffersCount;           // Number of vertex buffers (multi-buffering support)
    int currentBuffer;          // Current buffer tracking in case of multi-buffering
    VertexBuffer[] vertexBuffer; // Dynamic buffer(s) for vertex data

    DrawCall[] draws;            // Draw calls array, depends on textureId
    int drawsCounter;           // Draw calls counter
    float currentDepth;         // Current depth value for next draw

    public RenderBatch(){
    }

    public int getBuffersCount(){
        return buffersCount;
    }

    public void setBuffersCount(int buffersCount){
        this.buffersCount = buffersCount;
    }

    public int getCurrentBuffer(){
        return currentBuffer;
    }

    public void setCurrentBuffer(int currentBuffer){
        this.currentBuffer = currentBuffer;
    }

    public VertexBuffer[] getVertexBuffer(){
        return vertexBuffer;
    }

    public void setVertexBuffer(VertexBuffer[] vertexBuffer){
        this.vertexBuffer = vertexBuffer;
    }

    public DrawCall[] getDraws(){
        return draws;
    }

    public void setDraws(DrawCall[] draws){
        this.draws = draws;
    }

    public int getDrawsCounter(){
        return drawsCounter;
    }

    public void setDrawsCounter(int drawsCounter){
        this.drawsCounter = drawsCounter;
    }

    public float getCurrentDepth(){
        return currentDepth;
    }

    public void setCurrentDepth(float currentDepth){
        this.currentDepth = currentDepth;
    }
}
