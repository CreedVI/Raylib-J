package com.raylib.java.rlgl;

public class rlRenderBatch{

    int bufferCount;           // Number of vertex buffers (multi-buffering support)
    int currentBuffer;          // Current buffer tracking in case of multi-buffering
    rlVertexBuffer[] rlVertexBuffer; // Dynamic buffer(s) for vertex data

    rlDrawCall[] draws;            // Draw calls array, depends on textureId
    int drawCounter;           // Draw calls counter
    float currentDepth;         // Current depth value for next draw

    public rlRenderBatch() {
    }

    public int getBufferCount() {
        return bufferCount;
    }

    public void setBufferCount(int bufferCount) {
        this.bufferCount = bufferCount;
    }

    public int getCurrentBuffer() {
        return currentBuffer;
    }

    public void setCurrentBuffer(int currentBuffer) {
        this.currentBuffer = currentBuffer;
    }

    public rlVertexBuffer[] getVertexBuffer() {
        return rlVertexBuffer;
    }

    public void setVertexBuffer(rlVertexBuffer[] rlVertexBuffer) {
        this.rlVertexBuffer = rlVertexBuffer;
    }

    public rlDrawCall[] getDraws() {
        return draws;
    }

    public void setDraws(rlDrawCall[] draws) {
        this.draws = draws;
    }

    public int getDrawCounter() {
        return drawCounter;
    }

    public void setDrawCounter(int drawCounter) {
        this.drawCounter = drawCounter;
    }

    public float getCurrentDepth() {
        return currentDepth;
    }

    public void setCurrentDepth(float currentDepth) {
        this.currentDepth = currentDepth;
    }
}
