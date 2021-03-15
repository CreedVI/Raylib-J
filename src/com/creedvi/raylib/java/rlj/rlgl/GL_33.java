package com.creedvi.raylib.java.rlj.rlgl;

import com.creedvi.raylib.java.rlj.raymath.Matrix;
import com.creedvi.raylib.java.rlj.raymath.Vector3;

import static com.creedvi.raylib.java.rlj.raymath.RayMath.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_ERROR;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;

public class GL_33{

    static void rlBegin(int mode){
        // Draw mode can be RL_LINES, RL_TRIANGLES and RL_QUADS
        // NOTE: In all three cases, vertex are accumulated over default internal vertex buffer
        if (RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].mode != mode){
            if (RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount > 0){
                // Make sure current RLGL.getRlgl().getCurrentBatch().draws[i].vertexCount is aligned a multiple of 4,
                // that way, following QUADS drawing will keep aligned with index processing
                // It implies adding some extra alignment vertex at the end of the draw,
                // those vertex are not processed but they are considered as an additional offset
                // for the next set of vertex to be drawn
                if (RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].mode == RL_LINES){
                    RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment =
                            ((RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount < 4) ?
                                    RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount :
                                    RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount % 4);
                }
                else if (RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].mode == RL_TRIANGLES){
                    RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment =
                            ((RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount < 4) ?
                                    1 : (4 - (RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount % 4)));
                }

                else{
                    RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment = 0;
                }

                if (RLGL.rlCheckBufferLimit(RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment)){
                    RLGL.DrawRenderBatch(RLGL.getRlglData().getCurrentBatch());
                }
                else{
                    RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().getCurrentBuffer()].vCounter += RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment;
                    RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().getCurrentBuffer()].cCounter += RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment;
                    RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().getCurrentBuffer()].tcCounter += RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexAlignment;

                    RLGL.getRlglData().getCurrentBatch().drawsCounter++;
                }
            }

            if (RLGL.getRlglData().getCurrentBatch().drawsCounter >= DEFAULT_BATCH_DRAWCALLS){
                RLGL.DrawRenderBatch(RLGL.getRlglData().getCurrentBatch());
            }

            RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].mode = mode;
            RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount = 0;
            RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].textureId =
                    RLGL.getRlglData().getState().getDefaultTextureId();
        }
    }

    // Finish vertex providing
    static void rlEnd(){
        // Make sure vertexCount is the same for vertices, texcoords, colors and normals
        // NOTE: In OpenGL 1.1, one glColor call can be made for all the subsequent glVertex calls
        // Make sure colors count match vertex count
        if (RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter != RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter){
            int addColors = RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter - RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter;

            for (int i = 0; i < addColors; i++){
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter] = RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter - 4];
                //currentBatch.vertexBuffer[0].colors[4 * 1] = currentBatch.vertexBuffer[0].colors[4 * 1 - 4]
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter + 1] = RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter - 3];
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter + 2] = RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter - 2];
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter + 3] = RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].colors[4 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter - 1];
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].cCounter++;
            }
        }

        // Make sure texcoords count match vertex count
        if (RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter != RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].tcCounter){
            int addTexCoords = RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter - RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].tcCounter;

            for (int i = 0; i < addTexCoords; i++){
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].texcoords[2 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].tcCounter] = 0.0f;
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].texcoords[2 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].tcCounter + 1] = 0.0f;
                RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].tcCounter++;
            }
        }

        // TODO: Make sure normals count match vertex count... if normals support is added in a future... :P

        // NOTE: Depth increment is dependant on rlOrtho(): z-near and z-far values,
        // as well as depth buffer bit-depth (16bit or 24bit or 32bit)
        // Correct increment formula would be: depthInc = (zfar - znear)/pow(2, bits)
        RLGL.getRlglData().getCurrentBatch().currentDepth += (1.0f / 20000.0f);

        // Verify internal buffers limits
        // NOTE: This check is combined with usage of rlCheckBufferLimit()
        if ((RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter) >= (RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].elementsCount * 4 - 4)){
            // WARNING: If we are between rlPushMatrix() and rlPopMatrix() and we need to force a DrawRenderBatch(),
            // we need to call rlPopMatrix() before to recover *RLGL.getRlglData().getState().currentMatrix (RLGL.getRlglData().getState().modelview) for the next forced draw call!
            // If we have multiple matrix pushed, it will require "RLGL.getRlglData().getState().stackCounter" pops before launching the draw
            for (int i = RLGL.getRlglData().getState().getStackCounter(); i >= 0; i--) rlPopMatrix();
            DrawRenderBatch(RLGL.getRlglData().getCurrentBatch());
        }
    }

    // Choose the current matrix to be transformed
    static void rlMatrixMode(int mode){
        if (mode == RL_PROJECTION){
            RLGL.getRlglData().getState().setCurrentMatrix(RLGL.getRlglData().getState().getProjection());
        }
        else if (mode == RL_MODELVIEW){
            RLGL.getRlglData().getState().setCurrentMatrix(RLGL.getRlglData().getState().getModelview());
            //else if (mode == RL_TEXTURE) // Not supported
        }
        RLGL.getRlglData().getState().setCurrentMatrixMode(mode);
    }

    // Push the current matrix into RLGL.getRlgl().getState().stack
    static void rlPushMatrix(){
        if (RLGL.getRlglData().getState().getStackCounter() >= MAX_MATRIX_STACK_SIZE){
            Tracelog(LOG_ERROR, "RLGL: Matrix stack overflow (MAX_MATRIX_STACK_SIZE)");
        }

        if (RLGL.getRlglData().getState().getCurrentMatrixMode() == RL_MODELVIEW){
            RLGL.getRlglData().getState().setTransformRequired(true);
            RLGL.getRlglData().getState().setCurrentMatrix(RLGL.getRlglData().getState().getTransform());
        }

        RLGL.getRlglData().getState().getStack()[RLGL.getRlglData().getState().getStackCounter()] =
                RLGL.getRlglData().getState().getCurrentMatrix();
        RLGL.getRlglData().getState().setStackCounter(RLGL.getRlglData().getState().getStackCounter() + 1);
    }

    // Pop lattest inserted matrix from RLGL.getRlgl().getState().stack
    static void rlPopMatrix(){
        if (RLGL.getRlglData().getState().getStackCounter() > 0){
            Matrix mat = RLGL.getRlglData().getState().getStack()[RLGL.getRlglData().getState().getStackCounter() - 1];
            RLGL.getRlglData().getState().setCurrentMatrix(mat);
            RLGL.getRlglData().getState().setStackCounter(RLGL.getRlglData().getState().getStackCounter() - 1);
        }

        if ((RLGL.getRlglData().getState().getStackCounter() == 0) && (RLGL.getRlglData().getState().getCurrentMatrixMode() == RL_MODELVIEW)){
            RLGL.getRlglData().getState().setCurrentMatrix(RLGL.getRlglData().getState().getModelview());
            RLGL.getRlglData().getState().setTransformRequired(false);
        }
    }

    // Reset current matrix to identity matrix
    static void rlLoadIdentity(){
        RLGL.getRlglData().getState().setCurrentMatrix(MatrixIdentity());
    }

    // Multiply the current matrix by a translation matrix
    static void rlTranslatef(float x, float y, float z){
        Matrix matTranslation = MatrixTranslate(x, y, z);

        // NOTE: We transpose matrix with multiplication order
        RLGL.getRlglData().getState().setCurrentMatrix(MatrixMultiply(matTranslation,
                RLGL.getRlglData().getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a rotation matrix
    static void rlRotatef(float angleDeg, float x, float y, float z){
        Matrix matRotation = MatrixIdentity();

        Vector3 axis = new Vector3(x, y, z);
        matRotation = MatrixRotate(Vector3Normalize(axis), angleDeg * DEG2RAD);

        // NOTE: We transpose matrix with multiplication order
        RLGL.getRlglData().getState().setCurrentMatrix(MatrixMultiply(matRotation,
                RLGL.getRlglData().getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by a scaling matrix
    static void rlScalef(float x, float y, float z){
        Matrix matScale = MatrixScale(x, y, z);

        // NOTE: We transpose matrix with multiplication order
        RLGL.getRlglData().getState().setCurrentMatrix(MatrixMultiply(matScale,
                RLGL.getRlglData().getState().getCurrentMatrix()));
    }

    // Multiply the current matrix by another matrix
    static void rlMultMatrixf(float[] matf){
        // Matrix creation from array
        Matrix mat = new Matrix(matf[0], matf[4], matf[8], matf[12],
                matf[1], matf[5], matf[9], matf[13],
                matf[2], matf[6], matf[10], matf[14],
                matf[3], matf[7], matf[11], matf[15]);

        RLGL.getRlglData().getState().setCurrentMatrix(MatrixMultiply(RLGL.getRlglData().getState().getCurrentMatrix(),
                mat));
    }

    // Multiply the current matrix by a perspective matrix generated by parameters
    static void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar){
        Matrix matPerps = MatrixFrustum(left, right, bottom, top, znear, zfar);

        RLGL.getRlglData().getState().setCurrentMatrix(MatrixMultiply(RLGL.getRlglData().getState().getCurrentMatrix(),
                matPerps));
    }

    // Multiply the current matrix by an orthographic matrix generated by parameters
    static void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar){
        // NOTE: If left-right and top-botton values are equal it could create
        // a division by zero on MatrixOrtho(), response to it is platform/compiler dependant
        Matrix matOrtho = MatrixOrtho(left, right, bottom, top, znear, zfar);

        RLGL.getRlglData().getState().setCurrentMatrix(MatrixMultiply(RLGL.getRlglData().getState().getCurrentMatrix(),
                matOrtho));
    }

    static void rlVertex3f(float x, float y, float z){
        Vector3 vec = new Vector3(x, y, z);

        // Transform provided vector if required
        if (RLGL.getRlglData().getState().isTransformRequired()){
            vec = Vector3Transform(vec, RLGL.getRlglData().getState().getTransform());
        }

        // Verify that current vertex buffer elements limit has not been reached
        if (RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter < (RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].elementsCount * 4)){
            RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vertices[3 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter] = vec.getX();
            RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vertices[3 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter + 1] = vec.getY();
            RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vertices[3 * RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter + 2] = vec.getZ();
            RLGL.getRlglData().getCurrentBatch().vertexBuffer[RLGL.getRlglData().getCurrentBatch().currentBuffer].vCounter++;

            RLGL.getRlglData().getCurrentBatch().draws[RLGL.getRlglData().getCurrentBatch().drawsCounter - 1].vertexCount++;
        }
        else{
            Tracelog(LOG_ERROR, "RLGL: Batch elements overflow");
        }
    }

    // Define one vertex (position)
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
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].texcoords[2 * rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].tcCounter] = x;
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].texcoords[2 * rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].tcCounter + 1] = y;
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].tcCounter++;
    }

    // Define one vertex (normal)
    // NOTE: Normals limited to TRIANGLES only?
    static void rlNormal3f(float x, float y, float z){
        // TODO: Normals usage...
    }

    // Define one vertex (color)
    static void rlColor4ub(int x, int y, int z, int w){
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4 * rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].cCounter] = x;
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4 * rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].cCounter + 1] = y;
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4 * rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].cCounter + 2] = z;
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].colors[4 * rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].cCounter + 3] = w;
        rlglData.getCurrentBatch().vertexBuffer[rlglData.getCurrentBatch().currentBuffer].cCounter++;
    }

    // Define one vertex (color)
    static void rlColor4f(float r, float g, float b, float a){
        rlColor4ub((byte) (r * 255), (byte) (g * 255), (byte) (b * 255), (byte) (a * 255));
    }

    // Define one vertex (color)
    static void rlColor3f(float x, float y, float z){
        rlColor4ub((byte) (x * 255), (byte) (y * 255), (byte) (z * 255), (byte) 255);
    }

}
