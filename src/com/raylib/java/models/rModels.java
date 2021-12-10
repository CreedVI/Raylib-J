package com.raylib.java.models;

import com.raylib.java.core.Color;
import com.raylib.java.core.ray.Ray;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;
import com.raylib.java.rlgl.GL_33;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.textures.Texture2D;

public class rModels{

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    // Draw a line in 3D world space
    public void DrawLine3D(Vector3 startPos, Vector3 endPos, Color color){
        // WARNING: Be careful with internal buffer vertex alignment
        // when using RL_LINES or RL_TRIANGLES, data is aligned to fit
        // lines-triangles-quads in the same indexed buffers!!!
        RLGL.rlCheckRenderBatchLimit(8);

        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        GL_33.rlVertex3f(startPos.x, startPos.y, startPos.z);
        GL_33.rlVertex3f(endPos.x, endPos.y, endPos.z);
        RLGL.rlEnd();
    }

    // Draw a point in 3D space, actually a small line
    public void DrawPoint3D(Vector3 position, Color color){
        RLGL.rlCheckRenderBatchLimit(8);

        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(position.x, position.y, position.z);
        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        GL_33.rlVertex3f(0.0f, 0.0f, 0.0f);
        GL_33.rlVertex3f(0.0f, 0.0f, 0.1f);
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a circle in 3D world space
    public void DrawCircle3D(Vector3 center, float radius, Vector3 rotationAxis, float rotationAngle, Color color){
        RLGL.rlCheckRenderBatchLimit(2 * 36);

        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(center.x, center.y, center.z);
        RLGL.rlRotatef(rotationAngle, rotationAxis.x, rotationAxis.y, rotationAxis.z);

        RLGL.rlBegin(RLGL.RL_LINES);
        for (int i = 0; i < 360; i += 10){
            RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radius, (float) Math.cos(Raymath.DEG2RAD * i) * radius, 0.0f);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 10)) * radius, (float) Math.cos(Raymath.DEG2RAD * (i + 10)) * radius, 0.0f);
        }
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a color-filled triangle (vertex in counter-clockwise order!)
    public void DrawTriangle3D(Vector3 v1, Vector3 v2, Vector3 v3, Color color){
        RLGL.rlCheckRenderBatchLimit(3);

        RLGL.rlBegin(RLGL.RL_TRIANGLES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        GL_33.rlVertex3f(v1.x, v1.y, v1.z);
        GL_33.rlVertex3f(v2.x, v2.y, v2.z);
        GL_33.rlVertex3f(v3.x, v3.y, v3.z);
        RLGL.rlEnd();
    }

    // Draw a triangle strip defined by points
    public void DrawTriangleStrip3D(Vector3[] points, int pointsCount, Color color){
        if (pointsCount >= 3){
            RLGL.rlCheckRenderBatchLimit(3 * (pointsCount - 2));

            RLGL.rlBegin(RLGL.RL_TRIANGLES);
            RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

            for (int i = 2; i < pointsCount; i++){
                if ((i % 2) == 0){
                    GL_33.rlVertex3f(points[i].x, points[i].y, points[i].z);
                    GL_33.rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
                    GL_33.rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                }
                else{
                    GL_33.rlVertex3f(points[i].x, points[i].y, points[i].z);
                    GL_33.rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                    GL_33.rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
                }
            }
            RLGL.rlEnd();
        }
    }

    // Draw cube
    // NOTE: Cube position is the center position
    public void DrawCube(Vector3 position, float width, float height, float length, Color color){
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        RLGL.rlCheckRenderBatchLimit(36);

        RLGL.rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> rotate -> translate)
        RLGL.rlTranslatef(position.x, position.y, position.z);
        //rlRotatef(45, 0, 1, 0);
        //rlScalef(1.0f, 1.0f, 1.0f);   // NOTE: Vertices are directly scaled on definition

        RLGL.rlBegin(RLGL.RL_TRIANGLES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        // Front face
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Back face
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Top face
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        // Bottom face
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left

        // Right face
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        // Left face
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right

        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw cube (Vector version)
    public void DrawCubeV(Vector3 position, Vector3 size, Color color){
        DrawCube(position, size.x, size.y, size.z, color);
    }

    // Draw cube wires
    public void DrawCubeWires(Vector3 position, float width, float height, float length, Color color){
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        RLGL.rlCheckRenderBatchLimit(36);

        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(position.x, position.y, position.z);

        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        // Front Face -----------------------------------------------------
        // Bottom Line
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Left Line
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right

        // Top Line
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        // Right Line
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        // Back Face ------------------------------------------------------
        // Bottom Line
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        // Left Line
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right

        // Top Line
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Right Line
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left

        // Top Face -------------------------------------------------------
        // Left Line
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Front
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Back

        // Right Line
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Front
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Back

        // Bottom Face  ---------------------------------------------------
        // Left Line
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Top Left Front
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left Back

        // Right Line
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Top Right Front
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right Back
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw cube wires (vector version)
    public void DrawCubeWiresV(Vector3 position, Vector3 size, Color color){
        DrawCubeWires(position, size.x, size.y, size.z, color);
    }

    // Draw cube
    // NOTE: Cube position is the center position
    public void DrawCubeTexture(Texture2D texture, Vector3 position, float width, float height, float length, Color color){
        float x = position.x;
        float y = position.y;
        float z = position.z;

        RLGL.rlCheckRenderBatchLimit(36);

        RLGL.rlSetTexture(texture.id);

        //rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> rotate -> translate)
        //rlTranslatef(2.0f, 0.0f, 0.0f);
        //rlRotatef(45, 0, 1, 0);
        //rlScalef(2.0f, 2.0f, 2.0f);

        RLGL.rlBegin(RLGL.RL_QUADS);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        // Front Face
        RLGL.rlNormal3f(0.0f, 0.0f, 1.0f);                  // Normal Pointing Towards Viewer
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Of The Texture and Quad
        // Back Face
        RLGL.rlNormal3f(0.0f, 0.0f, -1.0f);                  // Normal Pointing Away From Viewer
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Left Of The Texture and Quad
        // Top Face
        RLGL.rlNormal3f(0.0f, 1.0f, 0.0f);                  // Normal Pointing Up
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        // Bottom Face
        RLGL.rlNormal3f(0.0f, -1.0f, 0.0f);                  // Normal Pointing Down
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        // Right face
        RLGL.rlNormal3f(1.0f, 0.0f, 0.0f);                  // Normal Pointing Right
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        GL_33.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        GL_33.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        // Left Face
        RLGL.rlNormal3f(-1.0f, 0.0f, 0.0f);                  // Normal Pointing Left
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        GL_33.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        GL_33.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlEnd();
        //rlPopMatrix();

        RLGL.rlSetTexture(0);
    }

    // Draw sphere
    public void DrawSphere(Vector3 centerPos, float radius, Color color){
        DrawSphereEx(centerPos, radius, 16, 16, color);
    }

    // Draw sphere with extended parameters
    public void DrawSphereEx(Vector3 centerPos, float radius, int rings, int slices, Color color){
        int numVertex = (rings + 2) * slices * 6;
        RLGL.rlCheckRenderBatchLimit(numVertex);

        RLGL.rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> translate)
        RLGL.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        RLGL.rlScalef(radius, radius, radius);

        RLGL.rlBegin(RLGL.RL_TRIANGLES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < (rings + 2); i++){
            for (int j = 0; j < slices; j++){
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));

                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
            }
        }
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw sphere wires
    public void DrawSphereWires(Vector3 centerPos, float radius, int rings, int slices, Color color){
        int numVertex = (rings + 2) * slices * 6;
        RLGL.rlCheckRenderBatchLimit(numVertex);

        RLGL.rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> translate)
        RLGL.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        RLGL.rlScalef(radius, radius, radius);

        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < (rings + 2); i++){
            for (int j = 0; j < slices; j++){
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));

                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));

                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                GL_33.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
            }
        }
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a cylinder
    // NOTE: It could be also used for pyramid and cone
    public void DrawCylinder(Vector3 position, float radiusTop, float radiusBottom, float height, int sides, Color color){
        if (sides < 3) sides = 3;

        int numVertex = sides * 6;
        RLGL.rlCheckRenderBatchLimit(numVertex);

        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(position.x, position.y, position.z);

        RLGL.rlBegin(RLGL.RL_TRIANGLES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        if (radiusTop > 0){
            // Draw Body -------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom); //Bottom Left
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom); //Bottom Right
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop); //Top Right

                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop); //Top Left
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom); //Bottom Left
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop); //Top Right
            }

            // Draw Cap --------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                GL_33.rlVertex3f(0, height, 0);
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop);
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop);
            }
        }
        else{
            // Draw Cone -------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                GL_33.rlVertex3f(0, height, 0);
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
                GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);
            }
        }

        // Draw Base -----------------------------------------------------------------------------------------
        for (int i = 0; i < 360; i += 360 / sides){
            GL_33.rlVertex3f(0, 0, 0);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
        }
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a wired cylinder
    // NOTE: It could be also used for pyramid and cone
    public void DrawCylinderWires(Vector3 position, float radiusTop, float radiusBottom, float height, int sides, Color color){
        if (sides < 3) sides = 3;

        int numVertex = sides * 8;
        RLGL.rlCheckRenderBatchLimit(numVertex);

        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(position.x, position.y, position.z);

        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < 360; i += 360 / sides){
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);

            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop);

            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop);

            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop);
            GL_33.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
        }
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a plane
    public void DrawPlane(Vector3 centerPos, Vector2 size, Color color){
        RLGL.rlCheckRenderBatchLimit(4);

        // NOTE: Plane is always created on XZ ground
        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        RLGL.rlScalef(size.x, 1.0f, size.y);

        RLGL.rlBegin(RLGL.RL_QUADS);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        RLGL.rlNormal3f(0.0f, 1.0f, 0.0f);

        GL_33.rlVertex3f(-0.5f, 0.0f, -0.5f);
        GL_33.rlVertex3f(-0.5f, 0.0f, 0.5f);
        GL_33.rlVertex3f(0.5f, 0.0f, 0.5f);
        GL_33.rlVertex3f(0.5f, 0.0f, -0.5f);
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a ray line
    public void DrawRay(Ray ray, Color color){
        float scale = 10000;

        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        GL_33.rlVertex3f(ray.position.x, ray.position.y, ray.position.z);
        GL_33.rlVertex3f(ray.position.x + ray.direction.x * scale, ray.position.y + ray.direction.y * scale, ray.position.z + ray.direction.z * scale);
        RLGL.rlEnd();
    }

    // Draw a grid centered at (0, 0, 0)
    public void DrawGrid(int slices, float spacing){
        int halfSlices = slices / 2;

        RLGL.rlCheckRenderBatchLimit((slices + 2) * 4);

        RLGL.rlBegin(RLGL.RL_LINES);
        for (int i = -halfSlices; i <= halfSlices; i++){
            if (i == 0){
                GL_33.rlColor3f(0.5f, 0.5f, 0.5f);
                GL_33.rlColor3f(0.5f, 0.5f, 0.5f);
                GL_33.rlColor3f(0.5f, 0.5f, 0.5f);
                GL_33.rlColor3f(0.5f, 0.5f, 0.5f);
            }
            else{
                GL_33.rlColor3f(0.75f, 0.75f, 0.75f);
                GL_33.rlColor3f(0.75f, 0.75f, 0.75f);
                GL_33.rlColor3f(0.75f, 0.75f, 0.75f);
                GL_33.rlColor3f(0.75f, 0.75f, 0.75f);
            }

            GL_33.rlVertex3f((float) i * spacing, 0.0f, (float) -halfSlices * spacing);
            GL_33.rlVertex3f((float) i * spacing, 0.0f, (float) halfSlices * spacing);

            GL_33.rlVertex3f((float) -halfSlices * spacing, 0.0f, (float) i * spacing);
            GL_33.rlVertex3f((float) halfSlices * spacing, 0.0f, (float) i * spacing);
        }
        RLGL.rlEnd();
    }

}
