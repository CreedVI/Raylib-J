package com.creedvi.raylib.java.rlj.models;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.ray.Ray;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.raymath.Vector3;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.raymath.Raymath.DEG2RAD;
import static com.creedvi.raylib.java.rlj.rlgl.GL_33.rlColor3f;
import static com.creedvi.raylib.java.rlj.rlgl.GL_33.rlVertex3f;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;

public class Models{

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    // Draw a line in 3D world space
    public void DrawLine3D(Vector3 startPos, Vector3 endPos, Color color){
        // WARNING: Be careful with internal buffer vertex alignment
        // when using RL_LINES or RL_TRIANGLES, data is aligned to fit
        // lines-triangles-quads in the same indexed buffers!!!
        rlCheckRenderBatchLimit(8);

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex3f(startPos.x, startPos.y, startPos.z);
        rlVertex3f(endPos.x, endPos.y, endPos.z);
        rlEnd();
    }

    // Draw a point in 3D space, actually a small line
    public void DrawPoint3D(Vector3 position, Color color){
        rlCheckRenderBatchLimit(8);

        rlPushMatrix();
        rlTranslatef(position.x, position.y, position.z);
        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex3f(0.0f, 0.0f, 0.0f);
        rlVertex3f(0.0f, 0.0f, 0.1f);
        rlEnd();
        rlPopMatrix();
    }

    // Draw a circle in 3D world space
    public void DrawCircle3D(Vector3 center, float radius, Vector3 rotationAxis, float rotationAngle, Color color){
        rlCheckRenderBatchLimit(2 * 36);

        rlPushMatrix();
        rlTranslatef(center.x, center.y, center.z);
        rlRotatef(rotationAngle, rotationAxis.x, rotationAxis.y, rotationAxis.z);

        rlBegin(RL_LINES);
        for (int i = 0; i < 360; i += 10){
            rlColor4ub(color.r, color.g, color.b, color.a);

            rlVertex3f((float) Math.sin(DEG2RAD * i) * radius, (float) Math.cos(DEG2RAD * i) * radius, 0.0f);
            rlVertex3f((float) Math.sin(DEG2RAD * (i + 10)) * radius, (float) Math.cos(DEG2RAD * (i + 10)) * radius, 0.0f);
        }
        rlEnd();
        rlPopMatrix();
    }

    // Draw a color-filled triangle (vertex in counter-clockwise order!)
    public void DrawTriangle3D(Vector3 v1, Vector3 v2, Vector3 v3, Color color){
        rlCheckRenderBatchLimit(3);

        rlBegin(RL_TRIANGLES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex3f(v1.x, v1.y, v1.z);
        rlVertex3f(v2.x, v2.y, v2.z);
        rlVertex3f(v3.x, v3.y, v3.z);
        rlEnd();
    }

    // Draw a triangle strip defined by points
    public void DrawTriangleStrip3D(Vector3[] points, int pointsCount, Color color){
        if (pointsCount >= 3){
            rlCheckRenderBatchLimit(3 * (pointsCount - 2));

            rlBegin(RL_TRIANGLES);
            rlColor4ub(color.r, color.g, color.b, color.a);

            for (int i = 2; i < pointsCount; i++){
                if ((i % 2) == 0){
                    rlVertex3f(points[i].x, points[i].y, points[i].z);
                    rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
                    rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                }
                else{
                    rlVertex3f(points[i].x, points[i].y, points[i].z);
                    rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                    rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
                }
            }
            rlEnd();
        }
    }

    // Draw cube
    // NOTE: Cube position is the center position
    public void DrawCube(Vector3 position, float width, float height, float length, Color color){
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        rlCheckRenderBatchLimit(36);

        rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> rotate -> translate)
        rlTranslatef(position.x, position.y, position.z);
        //rlRotatef(45, 0, 1, 0);
        //rlScalef(1.0f, 1.0f, 1.0f);   // NOTE: Vertices are directly scaled on definition

        rlBegin(RL_TRIANGLES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        // Front face
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Back face
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Top face
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        // Bottom face
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left

        // Right face
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        // Left face
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right

        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        rlEnd();
        rlPopMatrix();
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

        rlCheckRenderBatchLimit(36);

        rlPushMatrix();
        rlTranslatef(position.x, position.y, position.z);

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        // Front Face -----------------------------------------------------
        // Bottom Line
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Left Line
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right

        // Top Line
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        // Right Line
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        // Back Face ------------------------------------------------------
        // Bottom Line
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        // Left Line
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right

        // Top Line
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Right Line
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left

        // Top Face -------------------------------------------------------
        // Left Line
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Front
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Back

        // Right Line
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Front
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Back

        // Bottom Face  ---------------------------------------------------
        // Left Line
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Top Left Front
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left Back

        // Right Line
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Top Right Front
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right Back
        rlEnd();
        rlPopMatrix();
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

        rlCheckRenderBatchLimit(36);

        rlSetTexture(texture.id);

        //rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> rotate -> translate)
        //rlTranslatef(2.0f, 0.0f, 0.0f);
        //rlRotatef(45, 0, 1, 0);
        //rlScalef(2.0f, 2.0f, 2.0f);

        rlBegin(RL_QUADS);
        rlColor4ub(color.r, color.g, color.b, color.a);
        // Front Face
        rlNormal3f(0.0f, 0.0f, 1.0f);                  // Normal Pointing Towards Viewer
        rlTexCoord2f(0.0f, 0.0f);
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        rlTexCoord2f(1.0f, 0.0f);
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        rlTexCoord2f(1.0f, 1.0f);
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Of The Texture and Quad
        rlTexCoord2f(0.0f, 1.0f);
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Of The Texture and Quad
        // Back Face
        rlNormal3f(0.0f, 0.0f, -1.0f);                  // Normal Pointing Away From Viewer
        rlTexCoord2f(1.0f, 0.0f);
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right Of The Texture and Quad
        rlTexCoord2f(1.0f, 1.0f);
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        rlTexCoord2f(0.0f, 1.0f);
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        rlTexCoord2f(0.0f, 0.0f);
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Left Of The Texture and Quad
        // Top Face
        rlNormal3f(0.0f, 1.0f, 0.0f);                  // Normal Pointing Up
        rlTexCoord2f(0.0f, 1.0f);
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        rlTexCoord2f(0.0f, 0.0f);
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        rlTexCoord2f(1.0f, 0.0f);
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        rlTexCoord2f(1.0f, 1.0f);
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        // Bottom Face
        rlNormal3f(0.0f, -1.0f, 0.0f);                  // Normal Pointing Down
        rlTexCoord2f(1.0f, 1.0f);
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        rlTexCoord2f(0.0f, 1.0f);
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        rlTexCoord2f(0.0f, 0.0f);
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        rlTexCoord2f(1.0f, 0.0f);
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        // Right face
        rlNormal3f(1.0f, 0.0f, 0.0f);                  // Normal Pointing Right
        rlTexCoord2f(1.0f, 0.0f);
        rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right Of The Texture and Quad
        rlTexCoord2f(1.0f, 1.0f);
        rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Of The Texture and Quad
        rlTexCoord2f(0.0f, 1.0f);
        rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left Of The Texture and Quad
        rlTexCoord2f(0.0f, 0.0f);
        rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left Of The Texture and Quad
        // Left Face
        rlNormal3f(-1.0f, 0.0f, 0.0f);                  // Normal Pointing Left
        rlTexCoord2f(0.0f, 0.0f);
        rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left Of The Texture and Quad
        rlTexCoord2f(1.0f, 0.0f);
        rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Right Of The Texture and Quad
        rlTexCoord2f(1.0f, 1.0f);
        rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Right Of The Texture and Quad
        rlTexCoord2f(0.0f, 1.0f);
        rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Of The Texture and Quad
        rlEnd();
        //rlPopMatrix();

        rlSetTexture(0);
    }

    // Draw sphere
    public void DrawSphere(Vector3 centerPos, float radius, Color color){
        DrawSphereEx(centerPos, radius, 16, 16, color);
    }

    // Draw sphere with extended parameters
    public void DrawSphereEx(Vector3 centerPos, float radius, int rings, int slices, Color color){
        int numVertex = (rings + 2) * slices * 6;
        rlCheckRenderBatchLimit(numVertex);

        rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> translate)
        rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        rlScalef(radius, radius, radius);

        rlBegin(RL_TRIANGLES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < (rings + 2); i++){
            for (int j = 0; j < slices; j++){
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));

                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
            }
        }
        rlEnd();
        rlPopMatrix();
    }

    // Draw sphere wires
    public void DrawSphereWires(Vector3 centerPos, float radius, int rings, int slices, Color color){
        int numVertex = (rings + 2) * slices * 6;
        rlCheckRenderBatchLimit(numVertex);

        rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> translate)
        rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        rlScalef(radius, radius, radius);

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < (rings + 2); i++){
            for (int j = 0; j < slices; j++){
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));

                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));

                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
            }
        }
        rlEnd();
        rlPopMatrix();
    }

    // Draw a cylinder
    // NOTE: It could be also used for pyramid and cone
    public void DrawCylinder(Vector3 position, float radiusTop, float radiusBottom, float height, int sides, Color color){
        if (sides < 3) sides = 3;

        int numVertex = sides * 6;
        rlCheckRenderBatchLimit(numVertex);

        rlPushMatrix();
        rlTranslatef(position.x, position.y, position.z);

        rlBegin(RL_TRIANGLES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        if (radiusTop > 0){
            // Draw Body -------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i) * radiusBottom); //Bottom Left
                rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusBottom); //Bottom Right
                rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusTop); //Top Right

                rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusTop, height, (float) Math.cos(DEG2RAD * i) * radiusTop); //Top Left
                rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i) * radiusBottom); //Bottom Left
                rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusTop); //Top Right
            }

            // Draw Cap --------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                rlVertex3f(0, height, 0);
                rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusTop, height, (float) Math.cos(DEG2RAD * i) * radiusTop);
                rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusTop);
            }
        }
        else{
            // Draw Cone -------------------------------------------------------------------------------------
            for (int i = 0; i < 360; i += 360 / sides){
                rlVertex3f(0, height, 0);
                rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i) * radiusBottom);
                rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusBottom);
            }
        }

        // Draw Base -----------------------------------------------------------------------------------------
        for (int i = 0; i < 360; i += 360 / sides){
            rlVertex3f(0, 0, 0);
            rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusBottom);
            rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i) * radiusBottom);
        }
        rlEnd();
        rlPopMatrix();
    }

    // Draw a wired cylinder
    // NOTE: It could be also used for pyramid and cone
    public void DrawCylinderWires(Vector3 position, float radiusTop, float radiusBottom, float height, int sides, Color color){
        if (sides < 3) sides = 3;

        int numVertex = sides * 8;
        rlCheckRenderBatchLimit(numVertex);

        rlPushMatrix();
        rlTranslatef(position.x, position.y, position.z);

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < 360; i += 360 / sides){
            rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i) * radiusBottom);
            rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusBottom);

            rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusBottom);
            rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusTop);

            rlVertex3f((float) Math.sin(DEG2RAD * (i + 360 / sides)) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 360 / sides)) * radiusTop);
            rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusTop, height, (float) Math.cos(DEG2RAD * i) * radiusTop);

            rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusTop, height, (float) Math.cos(DEG2RAD * i) * radiusTop);
            rlVertex3f((float) Math.sin(DEG2RAD * i) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i) * radiusBottom);
        }
        rlEnd();
        rlPopMatrix();
    }

    // Draw a plane
    public void DrawPlane(Vector3 centerPos, Vector2 size, Color color){
        rlCheckRenderBatchLimit(4);

        // NOTE: Plane is always created on XZ ground
        rlPushMatrix();
        rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        rlScalef(size.x, 1.0f, size.y);

        rlBegin(RL_QUADS);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlNormal3f(0.0f, 1.0f, 0.0f);

        rlVertex3f(-0.5f, 0.0f, -0.5f);
        rlVertex3f(-0.5f, 0.0f, 0.5f);
        rlVertex3f(0.5f, 0.0f, 0.5f);
        rlVertex3f(0.5f, 0.0f, -0.5f);
        rlEnd();
        rlPopMatrix();
    }

    // Draw a ray line
    public void DrawRay(Ray ray, Color color){
        float scale = 10000;

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlColor4ub(color.r, color.g, color.b, color.a);

        rlVertex3f(ray.position.x, ray.position.y, ray.position.z);
        rlVertex3f(ray.position.x + ray.direction.x * scale, ray.position.y + ray.direction.y * scale, ray.position.z + ray.direction.z * scale);
        rlEnd();
    }

    // Draw a grid centered at (0, 0, 0)
    public void DrawGrid(int slices, float spacing){
        int halfSlices = slices / 2;

        rlCheckRenderBatchLimit((slices + 2) * 4);

        rlBegin(RL_LINES);
        for (int i = -halfSlices; i <= halfSlices; i++){
            if (i == 0){
                rlColor3f(0.5f, 0.5f, 0.5f);
                rlColor3f(0.5f, 0.5f, 0.5f);
                rlColor3f(0.5f, 0.5f, 0.5f);
                rlColor3f(0.5f, 0.5f, 0.5f);
            }
            else{
                rlColor3f(0.75f, 0.75f, 0.75f);
                rlColor3f(0.75f, 0.75f, 0.75f);
                rlColor3f(0.75f, 0.75f, 0.75f);
                rlColor3f(0.75f, 0.75f, 0.75f);
            }

            rlVertex3f((float) i * spacing, 0.0f, (float) -halfSlices * spacing);
            rlVertex3f((float) i * spacing, 0.0f, (float) halfSlices * spacing);

            rlVertex3f((float) -halfSlices * spacing, 0.0f, (float) i * spacing);
            rlVertex3f((float) halfSlices * spacing, 0.0f, (float) i * spacing);
        }
        rlEnd();
    }

}
