package com.raylib.java.rlgl;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Matrix;

import static com.raylib.java.Config.RL_DEFAULT_BATCH_DRAWCALLS;
import static com.raylib.java.Config.RL_MAX_MATRIX_STACK_SIZE;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.*;

public class GL_33 {

    private final Raylib context;

    public GL_33(Raylib context) {
        this.context = context;
    }

    void rlBegin(int mode){
        // Draw mode can be RL_LINES, RL_TRIANGLES and RL_QUADS
        // NOTE: In all three cases, vertex are accumulated over default internal vertex buffer
        if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode != mode) {
            if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount > 0) {
                // Make sure current rlglData.getCurrentBatch().draws[i].vertexCount is aligned a multiple of 4,
                // that way, following QUADS drawing will keep aligned with index processing
                // It implies adding some extra alignment vertex at the end of the draw,
                // those vertex are not processed but they are considered as an additional offset
                // for the next set of vertex to be drawn
                if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_LINES) {
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount < 4)? rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount : rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%4);
                }
                else if (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_TRIANGLES) {
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount < 4)? 1 : (4 - (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%4)));
                }
                else {
                    rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment = 0;
                }

                if (!context.rlgl.rlCheckRenderBatchLimit(rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment)) {
                    rlglData.getState().vertexCounter += rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexAlignment;
                    rlglData.getCurrentBatch().drawCounter++;
                }
            }

            if (rlglData.getCurrentBatch().drawCounter >= RL_DEFAULT_BATCH_DRAWCALLS) {
                context.rlgl.rlDrawRenderBatch(rlglData.getCurrentBatch());
            }

            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode = mode;
            rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].textureId = rlglData.getState().currentTextureId;
            rlglData.getState().currentTextureId = rlglData.getState().defaultTextureId;
        }
    }

    // Finish vertex providing
    void rlEnd() {
        // NOTE: Depth increment is dependent on rlOrtho(): z-near and z-far values,
        // as well as depth buffer bit-depth (16bit or 24bit or 32bit)
        // Correct increment formula would be: depthInc = (zfar - znear)/pow(2, bits)
        rlglData.getCurrentBatch().currentDepth += (1.0f/20000.0f);
    }

    // Define one vertex (position)
    // NOTE: Vertex position data is the basic information required for drawing
    void rlVertex3f(float x, float y, float z) {
        float tx = x;
        float ty = y;
        float tz = z;

        // Transform provided vector if required
        if (rlglData.getState().transformRequired) {
            tx = rlglData.getState().getTransform().m0*x + rlglData.getState().getTransform().m4*y + rlglData.getState().getTransform().m8*z + rlglData.getState().getTransform().m12;
            ty = rlglData.getState().getTransform().m1*x + rlglData.getState().getTransform().m5*y + rlglData.getState().getTransform().m9*z + rlglData.getState().getTransform().m13;
            tz = rlglData.getState().getTransform().m2*x + rlglData.getState().getTransform().m6*y + rlglData.getState().getTransform().m10*z + rlglData.getState().getTransform().m14;
        }

        // WARNING: Be careful with primitives breaking when launching a new batch!
        // RL_LINES comes in pairs, RL_TRIANGLES come in groups of 3 vertices and RL_QUADS come in groups of 4 vertices
        // Checking current draw.mode when a new vertex is required and finish the batch only if the draw.mode draw.vertexCount is %2, %3 or %4
        if (rlglData.getState().vertexCounter > (rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].elementCount*4 - 4)) {
            if ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_LINES) && (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%2 == 0)) {
                // Reached the maximum number of vertices for RL_LINES drawing
                // Launch a draw call but keep current state for next vertices coming
                // NOTE: Adding +1 vertex to the check for some safety
                context.rlgl.rlCheckRenderBatchLimit(2 + 1);
            }
            else if ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_TRIANGLES) && (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%3 == 0)) {
                context.rlgl.rlCheckRenderBatchLimit(3 + 1);
            }
            else if ((rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].mode == RL_QUADS) && (rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount%4 == 0)) {
                context.rlgl.rlCheckRenderBatchLimit(4 + 1);
            }
        }

        // Add vertices
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].vertices[3*rlglData.getState().vertexCounter] = tx;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].vertices[3*rlglData.getState().vertexCounter + 1] = ty;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].vertices[3*rlglData.getState().vertexCounter + 2] = tz;

        // Add current texcoord
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].texcoords[2*rlglData.getState().vertexCounter] = rlglData.getState().getTexcoordx();
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].texcoords[2*rlglData.getState().vertexCounter + 1] = rlglData.getState().getTexcoordy();

        // Add current normal
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].normals[3*rlglData.getState().vertexCounter] = rlglData.getState().normalx;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].normals[3*rlglData.getState().vertexCounter + 1] = rlglData.getState().normaly;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].normals[3*rlglData.getState().vertexCounter + 2] = rlglData.getState().normalz;

        // Add current color
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter] = rlglData.getState().colorr;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter + 1] = rlglData.getState().colorg;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter + 2] = rlglData.getState().colorb;
        rlglData.getCurrentBatch().getVertexBuffer()[rlglData.getCurrentBatch().currentBuffer].colors[4*rlglData.getState().vertexCounter + 3] = rlglData.getState().colora;

        rlglData.getState().vertexCounter++;
        rlglData.getCurrentBatch().draws[rlglData.getCurrentBatch().drawCounter - 1].vertexCount++;
    }

    // Define one vertex (position)
    void rlVertex2f(float x, float y) {
        rlVertex3f(x, y, rlglData.getCurrentBatch().currentDepth);
    }

    // Define one vertex (position)
    void rlVertex2i(int x, int y) {
        rlVertex3f((float)x, (float)y, rlglData.getCurrentBatch().currentDepth);
    }

    // Define one vertex (texture coordinate)
    // NOTE: Texture coordinates are limited to QUADS only
    void rlTexCoord2f(float x, float y) {
        rlglData.getState().setTexcoordx(x);
        rlglData.getState().setTexcoordy(y);
    }

    // Define one vertex (normal)
    // NOTE: Normals limited to TRIANGLES only?
    void rlNormal3f(float x, float y, float z) {
        float normalx = x;
        float normaly = y;
        float normalz = z;

        if (rlglData.getState().transformRequired) {
            normalx = rlglData.getState().getTransform().m0*x + rlglData.getState().getTransform().m4*y + rlglData.getState().getTransform().m8*z;
            normaly = rlglData.getState().getTransform().m1*x + rlglData.getState().getTransform().m5*y + rlglData.getState().getTransform().m9*z;
            normalz = rlglData.getState().getTransform().m2*x + rlglData.getState().getTransform().m6*y + rlglData.getState().getTransform().m10*z;
        }

        // NOTE: Default behavior assumes the normal vector is in the correct space for what the shader expects,
        // it could be not normalized to 0.0f..1.0f, magnitude can be used for some effects
        /*
        // WARNING: Vector normalization if required
        float length = sqrtf(normalx*normalx + normaly*normaly + normalz*normalz);
        if (length != 0.0f) {
            float ilength = 1.0f/length;
            normalx *= ilength;
            normaly *= ilength;
            normalz *= ilength;
        }
        */
        rlglData.getState().normalx = normalx;
        rlglData.getState().normaly = normaly;
        rlglData.getState().normalz = normalz;
    }

    // Define one vertex (color)
    void rlColor4ub(byte x, byte y, byte z, byte w) {
        rlglData.getState().colorr = x;
        rlglData.getState().colorg = y;
        rlglData.getState().colorb = z;
        rlglData.getState().colora = w;
    }

    // Define one vertex (color)
    void rlColor4f(float r, float g, float b, float a) {
        rlColor4ub((byte)(r*255), (byte)(g*255), (byte)(b*255), (byte)(a*255));
    }

    // Define one vertex (color)
    void rlColor3f(float x, float y, float z) {
        rlColor4ub((byte)(x*255), (byte)(y*255), (byte)(z*255), (byte)255);
    }

    void rlMatrixMode(int mode) {
        rlglData.getState().setCurrentMatrixMode(mode);
    }

    // Push the current matrix into rlglData.getState().stack
    void rlPushMatrix() {
        if (rlglData.getState().stackCounter >= RL_MAX_MATRIX_STACK_SIZE) {
            System.out.println("ERROR: RLGL: Matrix stack overflow (RL_MAX_MATRIX_STACK_SIZE)");
            return;
        }

        rlglData.getState().stack[rlglData.getState().stackCounter] = rlglData.getState().getCurrentMatrix().clone();
        rlglData.getState().stackCounter++;

        if (rlglData.getState().currentMatrixMode == RL_MODELVIEW) {
            rlglData.getState().transformRequired = true;
            rlglData.getState().setCurrentMatrix(rlglData.getState().getTransform());
        }
    }

    // Pop latest inserted matrix from rlglData.getState().stack
    void rlPopMatrix() {
        if (rlglData.getState().stackCounter > 0) {
            Matrix mat = rlglData.getState().getStack()[rlglData.getState().getStackCounter() - 1];
            rlglData.getState().setCurrentMatrix(mat);
            rlglData.getState().stackCounter--;
        }

        if ((rlglData.getState().stackCounter == 0) && (rlglData.getState().currentMatrixMode == RL_MODELVIEW)) {
            rlglData.getState().setCurrentMatrix(rlglData.getState().getModelview());
            rlglData.getState().transformRequired = false;
        }
    }

    // Reset current matrix to identity matrix
    void rlLoadIdentity() {
        rlglData.getState().setCurrentMatrix(MatrixIdentity());
    }

    // Multiply the current matrix by a translation matrix
    void rlTranslatef(float x, float y, float z) {
        Matrix matTranslation = MatrixIdentity();

        // Set translation component of matrix
        matTranslation.m12 = x;
        matTranslation.m13 = y;
        matTranslation.m14 = z;

        // NOTE: Transposing matrix by multiplication order
        rlglData.getState().setCurrentMatrix(MatrixMultiply(matTranslation, rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a rotation matrix
    // NOTE: The provided angle must be in degrees
    void rlRotatef(float angle, float x, float y, float z) {
        Matrix matRotation = MatrixIdentity();

        // Axis vector (x, y, z) normalization
        float lengthSquared = x*x + y*y + z*z;
        if ((lengthSquared != 1.0f) && (lengthSquared != 0.0f))
        {
            float inverseLength = (float) (1.0f/Math.sqrt(lengthSquared));
            x *= inverseLength;
            y *= inverseLength;
            z *= inverseLength;
        }

        // Rotation matrix generation
        float sinres = (float) Math.sin(DEG2RAD*angle);
        float cosres = (float) Math.cos(DEG2RAD*angle);
        float t = 1.0f - cosres;

        matRotation.m0 = x*x*t + cosres;
        matRotation.m1 = y*x*t + z*sinres;
        matRotation.m2 = z*x*t - y*sinres;
        matRotation.m3 = 0.0f;

        matRotation.m4 = x*y*t - z*sinres;
        matRotation.m5 = y*y*t + cosres;
        matRotation.m6 = z*y*t + x*sinres;
        matRotation.m7 = 0.0f;

        matRotation.m8 = x*z*t + y*sinres;
        matRotation.m9 = y*z*t - x*sinres;
        matRotation.m10 = z*z*t + cosres;
        matRotation.m11 = 0.0f;

        matRotation.m12 = 0.0f;
        matRotation.m13 = 0.0f;
        matRotation.m14 = 0.0f;
        matRotation.m15 = 1.0f;

        // NOTE: Transposing matrix by multiplication order
        rlglData.getState().setCurrentMatrix(MatrixMultiply(matRotation, rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a scaling matrix
    void rlScalef(float x, float y, float z) {
        Matrix matScale = MatrixIdentity();

        // Set scale component of matrix
        matScale.m0 = x;
        matScale.m5 = y;
        matScale.m10 = z;

        // NOTE: Transposing matrix by multiplication order
        rlglData.getState().setCurrentMatrix(MatrixMultiply(matScale, rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by another matrix
    void rlMultMatrixf(float[] matf) {
        // Matrix creation from array
        // Conversion from column-major to row-major memory order
        Matrix mat = new Matrix(
                matf[0], matf[4], matf[8], matf[12],
                matf[1], matf[5], matf[9], matf[13],
                matf[2], matf[6], matf[10], matf[14],
                matf[3], matf[7], matf[11], matf[15]
        );

        rlglData.getState().setCurrentMatrix(MatrixMultiply(mat, rlglData.getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a perspective matrix generated by parameters
    void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar) {
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
    void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar) {
        // NOTE: If left-right and top-botton values are equal it could create a division by zero,
        // response to it is platform/compiler dependent
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
}