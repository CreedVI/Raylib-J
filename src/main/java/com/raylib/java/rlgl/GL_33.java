package com.raylib.java.rlgl;

import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Vector3;

import static com.raylib.java.Config.RL_DEFAULT_BATCH_DRAWCALLS;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_ERROR;

public class GL_33{

    static void rlBegin(int mode){
        // Draw mode can be RL_LINES, RL_TRIANGLES and RL_QUADS
        // NOTE: In all three cases, vertex are accumulated over default internal vertex buffer
        if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode != mode) {
            if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount > 0) {
                // Make sure current rlglData.getCurrentBatch().draws[i].vertexCount is aligned a multiple of 4,
                // that way, following QUADS drawing will keep aligned with index processing
                // It implies adding some extra alignment vertex at the end of the draw,
                // those vertex are not processed but they are considered as an additional offset
                // for the next set of vertex to be drawn
                if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_LINES){
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount < 4)? rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount : rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%4);
                }
                else if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_TRIANGLES) {
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount < 4)? 1 : (4 - (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%4)));
                }
                else{
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = 0;
                }

                if (!rlCheckRenderBatchLimit(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment)) {
                    rlglData.getState().vertexCounter += rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment;
                    rlglData.getCurrentBatch().drawCounter++;
                }
            }

            if (rlglData.getCurrentBatch().drawCounter >= RL_DEFAULT_BATCH_DRAWCALLS) {
                rlDrawRenderBatch(rlglData.getCurrentBatch());
            }

            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode = mode;
            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount = 0;
            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId = rlglData.getState().defaultTextureId;
        }
    }

    // Finish vertex providing
    static void rlEnd(){
        // NOTE: Depth increment is dependant on rlOrtho(): z-near and z-far values,
        // as well as depth buffer bit-depth (16bit or 24bit or 32bit)
        // Correct increment formula would be: depthInc = (zfar - znear)/pow(2, bits)
        rlglData.getCurrentBatch().currentDepth += (1.0f/20000.0f);

        // Verify internal buffers limits
        // NOTE: This check is combined with usage of rlCheckRenderBatchLimit()
        if (rlglData.getState().getVertexCounter() >= (rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].elementCount*4 - 4)) {
            // WARNING: If we are between rlPushMatrix() and rlPopMatrix() and we need to force a rlDrawRenderBatch(),
            // we need to call rlPopMatrix() before to recover *RLGL.State.currentMatrix (RLGL.State.modelview) for the next forced draw call!
            // If we have multiple matrix pushed, it will require "RLGL.State.stackCounter" pops before launching the draw
            for (int i = rlglData.getState().getStackCounter(); i >= 0; i--){
                rlPopMatrix();
            }
            rlDrawRenderBatch(rlglData.getCurrentBatch());
        }
    }

    // Choose the current matrix to be transformed
    static void rlMatrixMode(int mode){
        rlglData.getState().setCurrentMatrixMode(mode);

        if (mode == RL_PROJECTION){
            rlglData.getState().setCurrentMatrix(rlglData.getState().getProjection());
        }
        else if (mode == RL_MODELVIEW){
            rlglData.getState().setCurrentMatrix(rlglData.getState().getModelview());
        }
        //else if (mode == RL_TEXTURE) // Not supported
    }

    // Push the current matrix into RLGL.getRlgl().getState().stack
    static void rlPushMatrix(){
        if (rlglData.getState().getStackCounter() >= MAX_MATRIX_STACK_SIZE){
            Tracelog(LOG_ERROR, "RLGL: Matrix stack overflow (MAX_MATRIX_STACK_SIZE)");
        }

        if (rlglData.getState().getCurrentMatrixMode() == RL_MODELVIEW){
            rlglData.getState().setTransformRequired(true);
            rlglData.getState().setCurrentMatrixMode(RLJ_TRANSFORM);
            rlglData.getState().setCurrentMatrix(rlglData.getState().getTransform());
        }

        rlglData.getState().getStack()[rlglData.getState().getStackCounter()] =
                rlglData.getState().getCurrentMatrix();
        rlglData.getState().setStackCounter(rlglData.getState().getStackCounter() + 1);
    }

    // Pop lattest inserted matrix from RLGL.getRlgl().getState().stack
    static void rlPopMatrix(){
        if (rlglData.getState().getStackCounter() > 0){
            Matrix mat = rlglData.getState().getStack()[rlglData.getState().getStackCounter() - 1];
            rlglData.getState().setCurrentMatrix(mat);
            rlglData.getState().setStackCounter(rlglData.getState().getStackCounter() - 1);
        }

        if ((rlglData.getState().getStackCounter() == 0) && (rlglData.getState().getCurrentMatrixMode() == RL_MODELVIEW)){
            rlglData.getState().setCurrentMatrix(rlglData.getState().getModelview());
            rlglData.getState().setTransformRequired(false);
        }
    }

    // Reset current matrix to identity matrix
    static void rlLoadIdentity(){
        rlglData.getState().setCurrentMatrix(MatrixIdentity());
    }

    // Multiply the current matrix by a translation matrix
    static void rlTranslatef(float x, float y, float z){
        Matrix matTranslation = new Matrix(
                1.0f, 0.0f, 0.0f, x,
                0.0f, 1.0f, 0.0f, y,
                0.0f, 0.0f, 1.0f, z,
                0.0f, 0.0f, 0.0f, 1.0f
        );

        // NOTE: We transpose matrix with multiplication order
        rlglData.getState().setCurrentMatrix(MatrixMultiply(matTranslation,
                rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a rotation matrix
    static void rlRotatef(float angle, float x, float y, float z){
        Vector3 axis = new Vector3(x, y, z);
        Matrix matRotation = MatrixRotate(Vector3Normalize(axis), angle * DEG2RAD);

        // NOTE: We transpose matrix with multiplication order
        rlglData.getState().setCurrentMatrix(MatrixMultiply(matRotation,
                rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a scaling matrix
    static void rlScalef(float x, float y, float z){
        Matrix matScale = new Matrix(
                x, 0.0f, 0.0f, 0.0f,
                0.0f, y, 0.0f, 0.0f,
                0.0f, 0.0f, z, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        );

        // NOTE: We transpose matrix with multiplication order
        rlglData.getState().setCurrentMatrix(MatrixMultiply(matScale, rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by another matrix
    static void rlMultMatrixf(float[] matf){
        // Matrix creation from array
        Matrix mat = new Matrix(matf);

        rlglData.getState().setCurrentMatrix(MatrixMultiply(rlglData.getState().getCurrentMatrix(),
                mat));
    }

    // Multiply the current matrix by a perspective matrix generated by parameters
    static void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar){
        Matrix matFrustum = new Matrix();

        float rl = (float)(right - left);
        float tb = (float)(top - bottom);
        float fn = (float)(zfar - znear);

        matFrustum.m0 = ((float) znear*2.0f)/rl;
        matFrustum.m1 = 0.0f;
        matFrustum.m2 = 0.0f;
        matFrustum.m3 = 0.0f;

        matFrustum.m4 = 0.0f;
        matFrustum.m5 = ((float) znear*2.0f)/tb;
        matFrustum.m6 = 0.0f;
        matFrustum.m7 = 0.0f;

        matFrustum.m8 = ((float)right + (float)left)/rl;
        matFrustum.m9 = ((float)top + (float)bottom)/tb;
        matFrustum.m10 = -((float)zfar + (float)znear)/fn;
        matFrustum.m11 = -1.0f;

        matFrustum.m12 = 0.0f;
        matFrustum.m13 = 0.0f;
        matFrustum.m14 = -((float)zfar*(float)znear*2.0f)/fn;
        matFrustum.m15 = 0.0f;

        rlglData.getState().setCurrentMatrix(MatrixMultiply(rlglData.getState().getCurrentMatrix(), matFrustum));
    }

    // Multiply the current matrix by an orthographic matrix generated by parameters
    static void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar){
        // NOTE: If left-right and top-bottom values are equal it could create a division by zero, response to it is platform/compiler dependant
        Matrix matOrtho = new Matrix();

        float rl = (float)(right - left);
        float tb = (float)(top - bottom);
        float fn = (float)(zfar - znear);

        matOrtho.m0 = 2.0f/rl;
        matOrtho.m1 = 0.0f;
        matOrtho.m2 = 0.0f;
        matOrtho.m3 = 0.0f;
        matOrtho.m4 = 0.0f;
        matOrtho.m5 = 2.0f/tb;
        matOrtho.m6 = 0.0f;
        matOrtho.m7 = 0.0f;
        matOrtho.m8 = 0.0f;
        matOrtho.m9 = 0.0f;
        matOrtho.m10 = -2.0f/fn;
        matOrtho.m11 = 0.0f;
        matOrtho.m12 = -((float)left + (float)right)/rl;
        matOrtho.m13 = -((float)top + (float)bottom)/tb;
        matOrtho.m14 = -((float)zfar + (float)znear)/fn;
        matOrtho.m15 = 1.0f;

        rlglData.getState().setCurrentMatrix(MatrixMultiply(rlglData.getState().getCurrentMatrix(), matOrtho));
    }

    public static void rlVertex3f(float x, float y, float z){
        float tx = x;
        float ty = y;
        float tz = z;

        // Transform provided vector if required
        if (rlglData.getState().transformRequired) {
            tx = rlglData.getState().transform.m0*x + rlglData.getState().transform.m4*y + rlglData.getState().transform.m8*z + rlglData.getState().transform.m12;
            ty = rlglData.getState().transform.m1*x + rlglData.getState().transform.m5*y + rlglData.getState().transform.m9*z + rlglData.getState().transform.m13;
            tz = rlglData.getState().transform.m2*x + rlglData.getState().transform.m6*y + rlglData.getState().transform.m10*z + rlglData.getState().transform.m14;
        }

        // Verify that current vertex buffer elements limit has not been reached
        if (rlglData.getState().vertexCounter < (rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].elementCount*4)) {
            // Add vertices
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].vertices[3*rlglData.getState().vertexCounter] = tx;
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].vertices[3*rlglData.getState().vertexCounter + 1] = ty;
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].vertices[3*rlglData.getState().vertexCounter + 2] = tz;

            // Add current texcoord
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].texcoords[2*rlglData.getState().vertexCounter] = rlglData.getState().texcoordx;
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].texcoords[2*rlglData.getState().vertexCounter + 1] = rlglData.getState().texcoordy;

            // TODO: Add current normal
            // By default rlVertexBuffer type does not store normals

            // Add current color
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter] = rlglData.getState().colorr;
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter + 1] = rlglData.getState().colorg;
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter + 2] = rlglData.getState().colorb;
            rlglData.getCurrentBatch().rlVertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter + 3] = rlglData.getState().colora;

            rlglData.getState().vertexCounter++;

            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount++;
        }
        else{
            Tracelog(LOG_ERROR, "RLGL: Batch elements overflow");
        }
    }

    // Define one vertex (position)
    static void rlVertex2f(float x, float y){
        rlVertex3f(x, y, rlglData.getCurrentBatch().currentDepth);
    }

    // Define one vertex (position)
    static void rlVertex2i(int x, int y){
        rlVertex3f((float) x, (float) y, rlglData.getCurrentBatch().currentDepth);
    }

    // Define one vertex (texture coordinate)
    // NOTE: Texture coordinates are limited to QUADS only
    static void rlTexCoord2f(float x, float y){
        rlglData.getState().texcoordx = x;
        rlglData.getState().texcoordy = y;
    }

    // Define one vertex (normal)
    // NOTE: Normals limited to TRIANGLES only?
    static void rlNormal3f(float x, float y, float z){
        rlglData.getState().normalx = x;
        rlglData.getState().normaly = y;
        rlglData.getState().normalz = z;
    }

    // Define one vertex (color)
    static void rlColor4ub(byte x, byte y, byte z, byte w){
        rlglData.getState().colorr = x;
        rlglData.getState().colorg = y;
        rlglData.getState().colorb = z;
        rlglData.getState().colora = w;
    }

    // Define one vertex (color)
    static void rlColor4f(float r, float g, float b, float a){
        rlColor4ub((byte)(r * 255),  (byte)(g * 255),  (byte)(b * 255),  (byte)(a * 255));
    }

    // Define one vertex (color)
    public static void rlColor3f(float x, float y, float z){
        rlColor4ub((byte)(x * 255), (byte)(y * 255), (byte)(z * 255), (byte)255);
    }

}