package com.raylib.java.models;

import com.raylib.java.core.Color;
import com.raylib.java.core.ray.Ray;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;

import static com.raylib.java.raymath.Raymath.Vector3DotProduct;

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
        RLGL.rlVertex3f(startPos.x, startPos.y, startPos.z);
        RLGL.rlVertex3f(endPos.x, endPos.y, endPos.z);
        RLGL.rlEnd();
    }

    // Draw a point in 3D space, actually a small line
    public void DrawPoint3D(Vector3 position, Color color){
        RLGL.rlCheckRenderBatchLimit(8);

        RLGL.rlPushMatrix();
        RLGL.rlTranslatef(position.x, position.y, position.z);
        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        RLGL.rlVertex3f(0.0f, 0.0f, 0.0f);
        RLGL.rlVertex3f(0.0f, 0.0f, 0.1f);
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

            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radius, (float) Math.cos(Raymath.DEG2RAD * i) * radius, 0.0f);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 10)) * radius, (float) Math.cos(Raymath.DEG2RAD * (i + 10)) * radius, 0.0f);
        }
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a color-filled triangle (vertex in counter-clockwise order!)
    public void DrawTriangle3D(Vector3 v1, Vector3 v2, Vector3 v3, Color color){
        RLGL.rlCheckRenderBatchLimit(3);

        RLGL.rlBegin(RLGL.RL_TRIANGLES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        RLGL.rlVertex3f(v1.x, v1.y, v1.z);
        RLGL.rlVertex3f(v2.x, v2.y, v2.z);
        RLGL.rlVertex3f(v3.x, v3.y, v3.z);
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
                    RLGL.rlVertex3f(points[i].x, points[i].y, points[i].z);
                    RLGL.rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
                    RLGL.rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                }
                else{
                    RLGL.rlVertex3f(points[i].x, points[i].y, points[i].z);
                    RLGL.rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                    RLGL.rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
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
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Back face
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Top face
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        // Bottom face
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left

        // Right face
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        // Left face
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right

        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
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
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Left Line
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right

        // Top Line
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        // Right Line
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        // Back Face ------------------------------------------------------
        // Bottom Line
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        // Left Line
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right

        // Top Line
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Right Line
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left

        // Top Face -------------------------------------------------------
        // Left Line
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Front
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Back

        // Right Line
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Front
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Back

        // Bottom Face  ---------------------------------------------------
        // Left Line
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Top Left Front
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left Back

        // Right Line
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Top Right Front
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right Back
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
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Of The Texture and Quad
        // Back Face
        RLGL.rlNormal3f(0.0f, 0.0f, -1.0f);                  // Normal Pointing Away From Viewer
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Left Of The Texture and Quad
        // Top Face
        RLGL.rlNormal3f(0.0f, 1.0f, 0.0f);                  // Normal Pointing Up
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        // Bottom Face
        RLGL.rlNormal3f(0.0f, -1.0f, 0.0f);                  // Normal Pointing Down
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        // Right face
        RLGL.rlNormal3f(1.0f, 0.0f, 0.0f);                  // Normal Pointing Right
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        RLGL.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        RLGL.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        // Left Face
        RLGL.rlNormal3f(-1.0f, 0.0f, 0.0f);                  // Normal Pointing Left
        RLGL.rlTexCoord2f(0.0f, 0.0f);
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 0.0f);
        RLGL.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        RLGL.rlTexCoord2f(1.0f, 1.0f);
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Right Of The Texture and Quad
        RLGL.rlTexCoord2f(0.0f, 1.0f);
        RLGL.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
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
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));

                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
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
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));

                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * ((j + 1) * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * ((j + 1) * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));

                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
                                 (float) Math.sin(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                 (float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(Raymath.DEG2RAD * (j * 360 / slices)));
                RLGL.rlVertex3f((float) Math.cos(Raymath.DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(Raymath.DEG2RAD * (j * 360 / slices)),
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
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom); //Bottom Left
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom); //Bottom Right
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop); //Top Right

                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop); //Top Left
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom); //Bottom Left
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop); //Top Right
            }

            // Draw Cap --------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                RLGL.rlVertex3f(0, height, 0);
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop);
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop);
            }
        }
        else{
            // Draw Cone -------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                RLGL.rlVertex3f(0, height, 0);
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
                RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);
            }
        }

        // Draw Base -----------------------------------------------------------------------------------------
        for (int i = 0; i < 360; i += 360 / sides){
            RLGL.rlVertex3f(0, 0, 0);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
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
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);

            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusBottom);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop);

            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * (i + 360 / sides)) * radiusTop);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop);

            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusTop, height, (float) Math.cos(Raymath.DEG2RAD * i) * radiusTop);
            RLGL.rlVertex3f((float) Math.sin(Raymath.DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(Raymath.DEG2RAD * i) * radiusBottom);
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

        RLGL.rlVertex3f(-0.5f, 0.0f, -0.5f);
        RLGL.rlVertex3f(-0.5f, 0.0f, 0.5f);
        RLGL.rlVertex3f(0.5f, 0.0f, 0.5f);
        RLGL.rlVertex3f(0.5f, 0.0f, -0.5f);
        RLGL.rlEnd();
        RLGL.rlPopMatrix();
    }

    // Draw a ray line
    public void DrawRay(Ray ray, Color color){
        float scale = 10000;

        RLGL.rlBegin(RLGL.RL_LINES);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);
        RLGL.rlColor4ub(color.r, color.g, color.b, color.a);

        RLGL.rlVertex3f(ray.position.x, ray.position.y, ray.position.z);
        RLGL.rlVertex3f(ray.position.x + ray.direction.x * scale, ray.position.y + ray.direction.y * scale, ray.position.z + ray.direction.z * scale);
        RLGL.rlEnd();
    }

    // Draw a grid centered at (0, 0, 0)
    public void DrawGrid(int slices, float spacing){
        int halfSlices = slices / 2;

        RLGL.rlCheckRenderBatchLimit((slices + 2) * 4);

        RLGL.rlBegin(RLGL.RL_LINES);
        for (int i = -halfSlices; i <= halfSlices; i++){
            if (i == 0){
                RLGL.rlColor3f(0.5f, 0.5f, 0.5f);
                RLGL.rlColor3f(0.5f, 0.5f, 0.5f);
                RLGL.rlColor3f(0.5f, 0.5f, 0.5f);
                RLGL.rlColor3f(0.5f, 0.5f, 0.5f);
            }
            else{
                RLGL.rlColor3f(0.75f, 0.75f, 0.75f);
                RLGL.rlColor3f(0.75f, 0.75f, 0.75f);
                RLGL.rlColor3f(0.75f, 0.75f, 0.75f);
                RLGL.rlColor3f(0.75f, 0.75f, 0.75f);
            }

            RLGL.rlVertex3f((float) i * spacing, 0.0f, (float) -halfSlices * spacing);
            RLGL.rlVertex3f((float) i * spacing, 0.0f, (float) halfSlices * spacing);

            RLGL.rlVertex3f((float) -halfSlices * spacing, 0.0f, (float) i * spacing);
            RLGL.rlVertex3f((float) halfSlices * spacing, 0.0f, (float) i * spacing);
        }
        RLGL.rlEnd();
    }


    // Draw a billboard
    public void DrawBillboard(Camera3D camera, Texture2D texture, Vector3 position, float size, Color tint) {
        Rectangle source = new Rectangle(0.0f, 0.0f, (float)texture.width, (float)texture.height);

        DrawBillboardRec(camera, texture, source, position, new Vector2(size, size), tint);
    }

    // Draw a billboard (part of a texture defined by a rectangle)
    public void DrawBillboardRec(Camera3D camera, Texture2D texture, Rectangle source, Vector3 position, Vector2 size, Color tint) {
        // NOTE: Billboard locked on axis-Y
        Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);

        DrawBillboardPro(camera, texture, source, position, up, size, Raymath.Vector2Zero(), 0.0f, tint);
    }

    public void DrawBillboardPro(Camera3D camera, Texture2D texture, Rectangle source, Vector3 position, Vector3 up, Vector2 size, Vector2 origin, float rotation, Color tint) {
        // NOTE: Billboard size will maintain source rectangle aspect ratio, size will represent billboard width
        Vector2 sizeRatio = new Vector2(size.y,size.x*source.height/source.width);

        Matrix matView = Raymath.MatrixLookAt(camera.position, camera.target, camera.up);

        Vector3 right = new Vector3(matView.m0, matView.m4, matView.m8);

        Vector3 rightScaled = Raymath.Vector3Scale(right, sizeRatio.x/2);
        Vector3 upScaled = Raymath.Vector3Scale(up, sizeRatio.y/2);

        Vector3 p1 = Raymath.Vector3Add(rightScaled, upScaled);
        Vector3 p2 = Raymath.Vector3Subtract(rightScaled, upScaled);

        Vector3 topLeft = Raymath.Vector3Scale(p2, -1);
        Vector3 topRight = p1;
        Vector3 bottomRight = p2;
        Vector3 bottomLeft = Raymath.Vector3Scale(p1, -1);

        if (rotation != 0.0f) {
            float sinRotation = (float) Math.sin(rotation*Raymath.DEG2RAD);
            float cosRotation = (float) Math.cos(rotation*Raymath.DEG2RAD);

            // NOTE: (-1, 1) is the range where origin.x, origin.y is inside the texture
            float rotateAboutX = sizeRatio.x*origin.x/2;
            float rotateAboutY = sizeRatio.y*origin.y/2;

            float xtvalue, ytvalue;
            float rotatedX, rotatedY;

            xtvalue = Vector3DotProduct(right, topLeft) - rotateAboutX; // Project points to x and y coordinates on the billboard plane
            ytvalue = Vector3DotProduct(up, topLeft) - rotateAboutY;
            rotatedX = xtvalue*cosRotation - ytvalue*sinRotation + rotateAboutX; // Rotate about the point origin
            rotatedY = xtvalue*sinRotation + ytvalue*cosRotation + rotateAboutY;
            topLeft = Raymath.Vector3Add(Raymath.Vector3Scale(up, rotatedY), Raymath.Vector3Scale(right, rotatedX)); // Translate back to cartesian coordinates

            xtvalue = Vector3DotProduct(right, topRight) - rotateAboutX;
            ytvalue = Vector3DotProduct(up, topRight) - rotateAboutY;
            rotatedX = xtvalue*cosRotation - ytvalue*sinRotation + rotateAboutX;
            rotatedY = xtvalue*sinRotation + ytvalue*cosRotation + rotateAboutY;
            topRight = Raymath.Vector3Add(Raymath.Vector3Scale(up, rotatedY), Raymath.Vector3Scale(right, rotatedX));

            xtvalue = Vector3DotProduct(right, bottomRight) - rotateAboutX;
            ytvalue = Vector3DotProduct(up, bottomRight) - rotateAboutY;
            rotatedX = xtvalue*cosRotation - ytvalue*sinRotation + rotateAboutX;
            rotatedY = xtvalue*sinRotation + ytvalue*cosRotation + rotateAboutY;
            bottomRight = Raymath.Vector3Add(Raymath.Vector3Scale(up, rotatedY), Raymath.Vector3Scale(right, rotatedX));

            xtvalue = Vector3DotProduct(right, bottomLeft)-rotateAboutX;
            ytvalue = Vector3DotProduct(up, bottomLeft)-rotateAboutY;
            rotatedX = xtvalue*cosRotation - ytvalue*sinRotation + rotateAboutX;
            rotatedY = xtvalue*sinRotation + ytvalue*cosRotation + rotateAboutY;
            bottomLeft = Raymath.Vector3Add(Raymath.Vector3Scale(up, rotatedY), Raymath.Vector3Scale(right, rotatedX));
        }

        // Translate points to the draw center (position)
        topLeft = Raymath.Vector3Add(topLeft, position);
        topRight = Raymath.Vector3Add(topRight, position);
        bottomRight = Raymath.Vector3Add(bottomRight, position);
        bottomLeft = Raymath.Vector3Add(bottomLeft, position);

        RLGL.rlCheckRenderBatchLimit(4);

        RLGL.rlSetTexture(texture.id);

        RLGL.rlBegin(RLGL.RL_QUADS);
        RLGL.rlColor4ub(tint.r, tint.g, tint.b, tint.a);

        // Bottom-left corner for texture and quad
        RLGL.rlTexCoord2f(source.x/texture.width, source.y/texture.height);
        RLGL.rlVertex3f(topLeft.x, topLeft.y, topLeft.z);

        // Top-left corner for texture and quad
        RLGL.rlTexCoord2f(source.x/texture.width, (source.y + source.height)/texture.height);
        RLGL.rlVertex3f(bottomLeft.x, bottomLeft.y, bottomLeft.z);

        // Top-right corner for texture and quad
        RLGL.rlTexCoord2f((source.x + source.width)/texture.width, (source.y + source.height)/texture.height);
        RLGL.rlVertex3f(bottomRight.x, bottomRight.y, bottomRight.z);

        // Bottom-right corner for texture and quad
        RLGL.rlTexCoord2f((source.x + source.width)/texture.width, source.y/texture.height);
        RLGL.rlVertex3f(topRight.x, topRight.y, topRight.z);
        RLGL.rlEnd();

        RLGL.rlSetTexture(0);
    }

    // Draw a bounding box with wires
    public void DrawBoundingBox(BoundingBox box, Color color) {
        Vector3 size = new Vector3();

        size.x = Math.abs(box.max.x - box.min.x);
        size.y = Math.abs(box.max.y - box.min.y);
        size.z = Math.abs(box.max.z - box.min.z);

        Vector3 center = new Vector3(box.min.x + size.x/2.0f, box.min.y + size.y/2.0f, box.min.z + size.z/2.0f);

        DrawCubeWires(center, size.x, size.y, size.z, color);
    }

}
