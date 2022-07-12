package com.raylib.java.models;

import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.core.ray.Ray;
import com.raylib.java.core.ray.RayCollision;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.textures.rTextures;
import com.raylib.java.utils.FileIO;
import com.raylib.java.utils.OBJLoader;

import java.io.IOException;

import static com.raylib.java.Config.COMPUTE_TANGENTS_METHOD_01;
import static com.raylib.java.Config.SUPPORT_MESH_GENERATION;
import static com.raylib.java.core.Color.WHITE;
import static com.raylib.java.models.rModels.MaterialMapIndex.*;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.raylib.java.rlgl.RLGL.rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC4;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_INT;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC4;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;

public class rModels{

    public class MaterialMapIndex {
        final static int
                MATERIAL_MAP_ALBEDO    = 0,     // Albedo material (same as: MATERIAL_MAP_DIFFUSE)
                MATERIAL_MAP_METALNESS = 1,     // Metalness material (same as: MATERIAL_MAP_SPECULAR)
                MATERIAL_MAP_NORMAL    = 2,     // Normal material
                MATERIAL_MAP_ROUGHNESS = 3,     // Roughness material
                MATERIAL_MAP_OCCLUSION = 4,     // Ambient occlusion material
                MATERIAL_MAP_EMISSION  = 5,     // Emission material
                MATERIAL_MAP_HEIGHT    = 6,     // Heightmap material
                MATERIAL_MAP_CUBEMAP   = 7,     // Cubemap material (NOTE: Uses GL_TEXTURE_CUBE_MAP)
                MATERIAL_MAP_IRRADIANCE= 8,     // Irradiance material (NOTE: Uses GL_TEXTURE_CUBE_MAP)
                MATERIAL_MAP_PREFILTER = 9,     // Prefilter material (NOTE: Uses GL_TEXTURE_CUBE_MAP)
                MATERIAL_MAP_BRDF      = 10;    // Brdf material

        public final static int MATERIAL_MAP_DIFFUSE = 0;
        final static int MATERIAL_MAP_SPECULAR = 1;
    }

    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    final static int MAX_MATERIAL_MAPS = 12;    // Maximum number of maps supported

    final static int MAX_MESH_VERTEX_BUFFERS = 7;    // Maximum vertex buffers (VBO) per mesh


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

    public Model LoadModel(String fileName) {

        Model model = new Model();

        if (rCore.IsFileExtension(fileName, ".obj")) {
            model = LoadOBJ(fileName);
        }

        // Make sure model transform is set to identity matrix!
        model.transform = Raymath.MatrixIdentity();

        if (model.meshCount == 0) {
            model.meshCount = 1;
            model.meshes = new Mesh[model.meshCount];
            if(SUPPORT_MESH_GENERATION) {
                Tracelog(LOG_WARNING, "MESH: ["+fileName+"] Failed to load mesh data, default to cube mesh");
                model.meshes[0] = GenMeshCube(1.0f, 1.0f, 1.0f);
            }
            else {
                Tracelog(LOG_WARNING, "MESH: ["+fileName+"] Failed to load mesh data");
            }
        }
        else {
            // Upload vertex data to GPU (static mesh)
            for (int i = 0; i < model.meshCount; i++) {
                UploadMesh(model.meshes[i], false);
            }
        }

        if (model.materialCount == 0) {
            Tracelog(LOG_WARNING, "MATERIAL: ["+fileName+"] Failed to load material data, default to white material");

            model.materialCount = 1;
            model.materials = new Material[model.materialCount];
            model.materials[0] = LoadMaterialDefault();

            if (model.meshMaterial == null) {
                model.meshMaterial = new int[model.meshCount];
            }
        }

        return model;
    }

    // Load model from generated mesh
    // WARNING: A shallow copy of mesh is generated, passed by value,
    // as long as struct contains pointers to data and some values, we get a copy
    // of mesh pointing to same data as original version... be careful!
    public Model LoadModelFromMesh(Mesh mesh) {
        Model model = new Model();

        model.transform = Raymath.MatrixIdentity();

        model.meshCount = 1;
        model.meshes = new Mesh[model.meshCount];
        model.meshes[0] = mesh;

        model.materialCount = 1;
        model.materials = new Material[model.materialCount];
        model.materials[0] = LoadMaterialDefault();

        model.meshMaterial = new int[model.meshCount];
        model.meshMaterial[0] = 0;  // First material index

        return model;
    }

    // Unload model (meshes/materials) from memory (RAM and/or VRAM)
    // NOTE: This function takes care of all model elements, for a detailed control
    // over them, use UnloadMesh() and UnloadMaterial()
    public void UnloadModel(Model model) {
        // Unload meshes
        for (int i = 0; i < model.meshCount; i++) {
            UnloadMesh(model.meshes[i]);
        }

        // Unload materials maps
        // NOTE: As the user could be sharing shaders and textures between models,
        // we don't unload the material but just free it's maps,
        // the user is responsible for freeing models shaders and textures
        for (int i = 0; i < model.materialCount; i++) {
            model.materials[i].maps = null;
        }

        // Unload arrays
        model.meshes = null;
        model.materials = null;
        model.meshMaterial = null;

        // Unload animation data
        model.bones = null;
        model.bindPose = null;

        Tracelog(LOG_INFO, "MODEL: Unloaded model (and meshes) from RAM and VRAM");
    }

    // Unload model (but not meshes) from memory (RAM and/or VRAM)
    public void UnloadModelKeepMeshes(Model model) {
        // Unload materials maps
        // NOTE: As the user could be sharing shaders and textures between models,
        // we don't unload the material but just free it's maps,
        // the user is responsible for freeing models shaders and textures
        for (int i = 0; i < model.materialCount; i++) {
            model.materials[i].maps = null;
        }

        // Unload arrays
        model.meshes = null;
        model.materials = null;
        model.meshMaterial = null;

        // Unload animation data
        model.bones = null;
        model.bindPose = null;

        Tracelog(LOG_INFO, "MODEL: Unloaded model (but not meshes) from RAM and VRAM");
    }

    // Compute model bounding box limits (considers all meshes)
    public BoundingBox GetModelBoundingBox(Model model) {
        BoundingBox bounds = new BoundingBox();

        if (model.meshCount > 0) {
            Vector3 temp = new Vector3();
            bounds = GetMeshBoundingBox(model.meshes[0]);

            for (int i = 1; i < model.meshCount; i++) {
                BoundingBox tempBounds = GetMeshBoundingBox(model.meshes[i]);

                temp.x = (bounds.min.x < tempBounds.min.x)? bounds.min.x : tempBounds.min.x;
                temp.y = (bounds.min.y < tempBounds.min.y)? bounds.min.y : tempBounds.min.y;
                temp.z = (bounds.min.z < tempBounds.min.z)? bounds.min.z : tempBounds.min.z;
                bounds.min = temp;

                temp.x = (bounds.max.x > tempBounds.max.x)? bounds.max.x : tempBounds.max.x;
                temp.y = (bounds.max.y > tempBounds.max.y)? bounds.max.y : tempBounds.max.y;
                temp.z = (bounds.max.z > tempBounds.max.z)? bounds.max.z : tempBounds.max.z;
                bounds.max = temp;
            }
        }

        return bounds;
    }


    // Upload vertex data into a VAO (if supported) and VBO
    void UploadMesh(Mesh mesh, boolean dynamic) {
        if (mesh.vaoId > 0) {
            // Check if mesh has already been loaded in GPU
            Tracelog(LOG_WARNING, "VAO: [ID "+mesh.vaoId+"] Trying to re-load an already loaded mesh");
            return;
        }

        mesh.vboId = new int[MAX_MESH_VERTEX_BUFFERS];

        mesh.vaoId = 0;        // Vertex Array Object
        mesh.vboId[0] = 0;     // Vertex buffer: positions
        mesh.vboId[1] = 0;     // Vertex buffer: texcoords
        mesh.vboId[2] = 0;     // Vertex buffer: normals
        mesh.vboId[3] = 0;     // Vertex buffer: colors
        mesh.vboId[4] = 0;     // Vertex buffer: tangents
        mesh.vboId[5] = 0;     // Vertex buffer: texcoords2
        mesh.vboId[6] = 0;     // Vertex buffer: indices

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            mesh.vaoId = RLGL.rlLoadVertexArray();
            rlEnableVertexArray(mesh.vaoId);

            // NOTE: Attributes must be uploaded considering default locations points

            // Enable vertex attributes: position (shader-location = 0)
            float[] vertices = mesh.animVertices != null ? mesh.animVertices : mesh.vertices;
            mesh.vboId[0] = rlLoadVertexBuffer(vertices, dynamic);
            rlSetVertexAttribute(0, 3, RL_FLOAT, false, 0, 0);
            rlEnableVertexAttribute(0);

            // Enable vertex attributes: texcoords (shader-location = 1)
            mesh.vboId[1] = rlLoadVertexBuffer(mesh.texcoords, dynamic);
            rlSetVertexAttribute(1, 2, RL_FLOAT, false, 0, 0);
            rlEnableVertexAttribute(1);

            if (mesh.normals != null) {
                // Enable vertex attributes: normals (shader-location = 2)
                float[] normals = mesh.animNormals != null ? mesh.animNormals : mesh.normals;
                mesh.vboId[2] = rlLoadVertexBuffer(normals, dynamic);
                rlSetVertexAttribute(2, 3, RL_FLOAT, false, 0, 0);
                rlEnableVertexAttribute(2);
            }
            else {
                // Default color vertex attribute set to WHITE
                float[] value ={1.0f, 1.0f, 1.0f} ;
                RLGL.rlSetVertexAttributeDefault(2, value, RLGL.rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC3, 3);
                RLGL.rlDisableVertexAttribute(2);
            }

            if (mesh.colors != null) {
                // Enable vertex attribute: color (shader-location = 3)
                float[] tmpColours = new float[mesh.colors.length];
                for (int i = 0; i < tmpColours.length; i++) {
                    tmpColours[i] = (float) mesh.colors[i]/255;
                }
                mesh.vboId[3] = rlLoadVertexBuffer(tmpColours, dynamic);
                rlSetVertexAttribute(3, 4, RL_UNSIGNED_BYTE, true, 0, 0);
                rlEnableVertexAttribute(3);
            } else {
                // Default color vertex attribute set to WHITE
                float[] value ={1.0f, 1.0f, 1.0f, 1.0f} ;
                rlSetVertexAttributeDefault(3, value, RL_SHADER_ATTRIB_VEC4, 4);
                rlDisableVertexAttribute(3);
            }

            if (mesh.tangents != null) {
                // Enable vertex attribute: tangent (shader-location = 4)
                mesh.vboId[4] = rlLoadVertexBuffer(mesh.tangents, dynamic);
                rlSetVertexAttribute(4, 4, RL_FLOAT, false, 0, 0);
                rlEnableVertexAttribute(4);
            } else {
                // Default tangents vertex attribute
                float[] value ={0.0f, 0.0f, 0.0f, 0.0f} ;
                rlSetVertexAttributeDefault(4, value, RL_SHADER_ATTRIB_VEC4, 4);
                rlDisableVertexAttribute(4);
            }

            if (mesh.texcoords2 != null) {
                // Enable vertex attribute: texcoord2 (shader-location = 5)
                mesh.vboId[5] = rlLoadVertexBuffer(mesh.texcoords2, dynamic);
                rlSetVertexAttribute(5, 2, RL_FLOAT, false, 0, 0);
                rlEnableVertexAttribute(5);
            } else {
                // Default texcoord2 vertex attribute
                float[] value ={0.0f, 0.0f} ;
                rlSetVertexAttributeDefault(5, value, RLGL.rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC2, 2);
                rlDisableVertexAttribute(5);
            }

            if (mesh.indices != null) {
                mesh.vboId[6] = RLGL.rlLoadVertexBufferElement(mesh.indices, dynamic);
            }

            if (mesh.vaoId > 0) {
                Tracelog(LOG_INFO, "VAO: [ID "+mesh.vaoId+"] Mesh uploaded successfully to VRAM (GPU)");
            }
            else {
                Tracelog(LOG_INFO, "VBO: Mesh uploaded successfully to VRAM (GPU)");
            }

            rlDisableVertexArray();
        }
    }

    // Update mesh vertex data in GPU for a specific buffer index
    public void UpdateMeshBuffer(Mesh mesh, int index, byte[] data, int dataSize, int offset) {
        RLGL.rlUpdateVertexBuffer(mesh.vboId[index], data, offset);
    }

    // Draw a 3d mesh with material and transform
    public void DrawMesh(Mesh mesh, Material material, Matrix transform) {
        if(GRAPHICS_API_OPENGL_11) {
            final int GL_VERTEX_ARRAY = 0x8074;
            final int GL_NORMAL_ARRAY = 0x8075;
            final int GL_COLOR_ARRAY = 0x8076;
            final int GL_TEXTURE_COORD_ARRAY = 0x8078;

            RLGL.rlEnableTexture(material.maps[MATERIAL_MAP_DIFFUSE].texture.id);

            RLGL.rlEnableStatePointer(GL_VERTEX_ARRAY, mesh.vertices);
            RLGL.rlEnableStatePointer(GL_TEXTURE_COORD_ARRAY, mesh.texcoords);
            RLGL.rlEnableStatePointer(GL_NORMAL_ARRAY, mesh.normals);
            RLGL.rlEnableStatePointer(GL_COLOR_ARRAY, mesh.colors);

            rlPushMatrix();
            rlMultMatrixf(MatrixToFloat(transform));
            rlColor4ub(material.maps[MATERIAL_MAP_DIFFUSE].color.r,
                    material.maps[MATERIAL_MAP_DIFFUSE].color.g,
                    material.maps[MATERIAL_MAP_DIFFUSE].color.b,
                    material.maps[MATERIAL_MAP_DIFFUSE].color.a);

            if (mesh.indices != null) {
                RLGL.rlDrawVertexArrayElements(0, mesh.triangleCount * 3, mesh.indices);
            }
            else {
                RLGL.rlDrawVertexArray(0, mesh.vertexCount);
            }
            rlPopMatrix();

            RLGL.rlDisableStatePointer(GL_VERTEX_ARRAY);
            RLGL.rlDisableStatePointer(GL_TEXTURE_COORD_ARRAY);
            RLGL.rlDisableStatePointer(GL_NORMAL_ARRAY);
            RLGL.rlDisableStatePointer(GL_COLOR_ARRAY);

            rlDisableTexture();
        }

        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            // Bind shader program
            rlEnableShader(material.shader.id);

            // Send required data to shader (matrices, values)
            //-----------------------------------------------------
            // Upload to shader material.colDiffuse
            if (material.shader.locs[RL_SHADER_LOC_COLOR_DIFFUSE] != -1) {
                float[] values ={
                    (float) material.maps[MATERIAL_MAP_DIFFUSE].color.r / 255.0f,
                            (float) material.maps[MATERIAL_MAP_DIFFUSE].color.g / 255.0f,
                            (float) material.maps[MATERIAL_MAP_DIFFUSE].color.b / 255.0f,
                            (float) material.maps[MATERIAL_MAP_DIFFUSE].color.a / 255.0f
                } ;

                rlSetUniform(material.shader.locs[RL_SHADER_LOC_COLOR_DIFFUSE], values, RL_SHADER_UNIFORM_VEC4, 1);
            }

            // Upload to shader material.colSpecular (if location available)
            if (material.shader.locs[RL_SHADER_LOC_COLOR_SPECULAR] != -1) {
                float[] values ={
                    (float) material.maps[RL_SHADER_LOC_COLOR_SPECULAR].color.r / 255.0f,
                            (float) material.maps[RL_SHADER_LOC_COLOR_SPECULAR].color.g / 255.0f,
                            (float) material.maps[RL_SHADER_LOC_COLOR_SPECULAR].color.b / 255.0f,
                            (float) material.maps[RL_SHADER_LOC_COLOR_SPECULAR].color.a / 255.0f
                } ;

                rlSetUniform(material.shader.locs[RL_SHADER_LOC_COLOR_SPECULAR], values, RL_SHADER_UNIFORM_VEC4, 1);
            }

            // Get a copy of current matrices to work with,
            // just in case stereo render is required and we need to modify them
            // NOTE: At this point the modelview matrix just contains the view matrix (camera)
            // That's because BeginMode3D() sets it and there is no model-drawing function
            // that modifies it, all use rlPushMatrix() and rlPopMatrix()
            Matrix matModel = Raymath.MatrixIdentity();
            Matrix matView = RLGL.rlGetMatrixModelview();
            Matrix matModelView = Raymath.MatrixIdentity();
            Matrix matProjection = RLGL.rlGetMatrixProjection();

            // Upload view and projection matrices (if locations available)
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_VIEW] != -1)
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_VIEW], matView);
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION] != -1)
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION], matProjection);

            // Model transformation matrix is send to shader uniform location: SHADER_LOC_MATRIX_MODEL
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL] != -1)
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL], transform);

            // Accumulate several model transformations:
            //    transform: model transformation provided (includes DrawModel() params combined with model.transform)
            //    rlGetMatrixTransform(): rlgl internal transform matrix due to push/pop matrix stack
            matModel = Raymath.MatrixMultiply(transform, RLGL.rlGetMatrixTransform());

            // Get model-view matrix
            matModelView = Raymath.MatrixMultiply(matModel, matView);

            // Upload model normal matrix (if locations available)
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_NORMAL] != -1) {
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_NORMAL], MatrixTranspose(MatrixInvert(matModel)));
            }
            //-----------------------------------------------------

            // Bind active texture maps (if available)
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id > 0) {
                    // Select current shader texture slot
                    rlActiveTextureSlot(i);

                    // Enable texture for active slot
                    if ((i == MATERIAL_MAP_IRRADIANCE) || (i == MATERIAL_MAP_PREFILTER) || (i == MATERIAL_MAP_CUBEMAP)) {
                        RLGL.rlEnableTextureCubemap(material.maps[i].texture.id);
                    }
                    else {
                        rlEnableTexture(material.maps[i].texture.id);
                    }

                    rlSetUniform(material.shader.locs[RL_SHADER_LOC_MAP_DIFFUSE + i], new float[]{i}, RL_SHADER_UNIFORM_INT, 1);
                }
            }

            // Try binding vertex array objects (VAO)
            // or use VBOs if not possible
            if (!rlEnableVertexArray(mesh.vaoId)) {
                // Bind mesh VBO data: vertex position (shader-location = 0)
                rlEnableVertexBuffer(mesh.vboId[0]);
                rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_POSITION], 3, RL_FLOAT, false, 0, 0);
                rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_POSITION]);

                // Bind mesh VBO data: vertex texcoords (shader-location = 1)
                rlEnableVertexBuffer(mesh.vboId[1]);
                rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD01], 2, RL_FLOAT, false, 0, 0);
                rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD01]);

                if (material.shader.locs[RL_SHADER_LOC_VERTEX_NORMAL] != -1) {
                    // Bind mesh VBO data: vertex normals (shader-location = 2)
                    rlEnableVertexBuffer(mesh.vboId[2]);
                    rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_NORMAL], 3, RL_FLOAT, false, 0, 0);
                    rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_NORMAL]);
                }

                // Bind mesh VBO data: vertex colors (shader-location = 3, if available)
                if (material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR] != -1) {
                    if (mesh.vboId[3] != 0) {
                        rlEnableVertexBuffer(mesh.vboId[3]);
                        rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR], 4, RL_UNSIGNED_BYTE, true, 0, 0);
                        rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR]);
                    } else {
                        // Set default value for unused attribute
                        // NOTE: Required when using default shader and no VAO support
                        float[] value ={1.0f, 1.0f, 1.0f, 1.0f} ;
                        rlSetVertexAttributeDefault(material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR], value, RL_SHADER_ATTRIB_VEC4, 4);
                        rlDisableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR]);
                    }
                }

                // Bind mesh VBO data: vertex tangents (shader-location = 4, if available)
                if (material.shader.locs[RL_SHADER_LOC_VERTEX_TANGENT] != -1) {
                    rlEnableVertexBuffer(mesh.vboId[4]);
                    rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TANGENT], 4, RL_FLOAT, false, 0, 0);
                    rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TANGENT]);
                }

                // Bind mesh VBO data: vertex texcoords2 (shader-location = 5, if available)
                if (material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD02] != -1) {
                    RLGL.rlEnableVertexBuffer(mesh.vboId[5]);
                    rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD02], 2, RL_FLOAT, false, 0, 0);
                    rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD02]);
                }

                if (mesh.indices != null) {
                    RLGL.rlEnableVertexBufferElement(mesh.vboId[6]);
                }
            }

            int eyeCount = 1;
            if (RLGL.rlIsStereoRendererEnabled()) {
                eyeCount = 2;
            }

            for (int eye = 0; eye < eyeCount; eye++) {
                // Calculate model-view-projection matrix (MVP)
                Matrix matModelViewProjection = Raymath.MatrixIdentity();
                if (eyeCount == 1) matModelViewProjection = Raymath.MatrixMultiply(matModelView, matProjection);
                else {
                    // Setup current eye viewport (half screen width)
                    rlViewport(eye * RLGL.rlGetFramebufferWidth() / 2, 0, rlGetFramebufferWidth() / 2, RLGL.rlGetFramebufferHeight());
                    matModelViewProjection = Raymath.MatrixMultiply(Raymath.MatrixMultiply(matModelView, RLGL.rlGetMatrixViewOffsetStereo(eye)), RLGL.rlGetMatrixProjectionStereo(eye));
                }

                // Send combined model-view-projection matrix to shader
                RLGL.rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_MVP], matModelViewProjection);

                // Draw mesh
                if (mesh.indices != null ) {
                    rlDrawVertexArrayElements(0, mesh.triangleCount * 3, new byte[0]);
                }
                else {
                    rlDrawVertexArray(0, mesh.vertexCount);
                }
            }

            // Unbind all binded texture maps
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                // Select current shader texture slot
                RLGL.rlActiveTextureSlot(i);

                // Disable texture for active slot
                if ((i == MATERIAL_MAP_IRRADIANCE) || (i == MaterialMapIndex.MATERIAL_MAP_PREFILTER) || (i == MaterialMapIndex.MATERIAL_MAP_CUBEMAP)) {
                    RLGL.rlDisableTextureCubemap();
                }
                else {
                    rlDisableTexture();
                }
            }

            // Disable all possible vertex array objects (or VBOs)
            rlDisableVertexArray();
            RLGL.rlDisableVertexBuffer();
            RLGL.rlDisableVertexBufferElement();

            // Disable shader program
            RLGL.rlDisableShader();

            // Restore rlgl internal modelview and projection matrices
            rlSetMatrixModelview(matView);
            rlSetMatrixProjection(matProjection);
        }
    }


    // Generated cuboid mesh
    public Mesh GenMeshCube(float width, float height, float length) {
        Mesh mesh = new Mesh();

        float vertices[] = {
                -width/2, -height/2, length/2,
                width/2, -height/2, length/2,
                width/2, height/2, length/2,
                -width/2, height/2, length/2,
                -width/2, -height/2, -length/2,
                -width/2, height/2, -length/2,
                width/2, height/2, -length/2,
                width/2, -height/2, -length/2,
                -width/2, height/2, -length/2,
                -width/2, height/2, length/2,
                width/2, height/2, length/2,
                width/2, height/2, -length/2,
                -width/2, -height/2, -length/2,
                width/2, -height/2, -length/2,
                width/2, -height/2, length/2,
                -width/2, -height/2, length/2,
                width/2, -height/2, -length/2,
                width/2, height/2, -length/2,
                width/2, height/2, length/2,
                width/2, -height/2, length/2,
                -width/2, -height/2, -length/2,
                -width/2, -height/2, length/2,
                -width/2, height/2, length/2,
                -width/2, height/2, -length/2
        };

        float texcoords[] = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
        };

        float normals[] = {
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f,-1.0f,
                0.0f, 0.0f,-1.0f,
                0.0f, 0.0f,-1.0f,
                0.0f, 0.0f,-1.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f,-1.0f, 0.0f,
                0.0f,-1.0f, 0.0f,
                0.0f,-1.0f, 0.0f,
                0.0f,-1.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f
        };

        mesh.vertices = vertices;
        mesh.texcoords = texcoords;
        mesh.normals = normals;
        mesh.indices = new float[36];

        int k = 0;

        // Indices can be initialized right now
        for (int i = 0; i < 36; i += 6) {
            mesh.indices[i] = 4*k;
            mesh.indices[i + 1] = 4*k + 1;
            mesh.indices[i + 2] = 4*k + 2;
            mesh.indices[i + 3] = 4*k;
            mesh.indices[i + 4] = 4*k + 2;
            mesh.indices[i + 5] = 4*k + 3;

            k++;
        }

        mesh.vertexCount = 24;
        mesh.triangleCount = 12;

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }


    // Unload mesh from memory (RAM and VRAM)
    public void UnloadMesh(Mesh mesh) {
        // Unload rlgl mesh vboId data
        RLGL.rlUnloadVertexArray(mesh.vaoId);

        if (mesh.vboId != null){
            for (int i = 0; i < MAX_MESH_VERTEX_BUFFERS; i++) {
                RLGL.rlUnloadVertexBuffer(mesh.vboId[i]);
            }
        }
        mesh = null;
    }


    // Compute mesh bounding box limits
    // NOTE: minVertex and maxVertex should be transformed by model transform matrix
    BoundingBox GetMeshBoundingBox(Mesh mesh) {
        // Get min and max vertex to construct bounds (AABB)
        Vector3 minVertex = new Vector3();
        Vector3 maxVertex = new Vector3();

        if (mesh.vertices != null) {
            minVertex = new Vector3(mesh.vertices[0], mesh.vertices[1], mesh.vertices[2]);
            maxVertex = new Vector3(mesh.vertices[0], mesh.vertices[1], mesh.vertices[2]);

            for (int i = 1; i < mesh.vertexCount; i++) {
                minVertex = Raymath.Vector3Min(minVertex, new Vector3(mesh.vertices[i*3], mesh.vertices[i*3 + 1], mesh.vertices[i*3 + 2]));
                maxVertex = Raymath.Vector3Max(maxVertex, new Vector3(mesh.vertices[i*3], mesh.vertices[i*3 + 1], mesh.vertices[i*3 + 2]));
            }
        }

        // Create the bounding box
        BoundingBox box = new BoundingBox();
        box.min = minVertex;
        box.max = maxVertex;

        return box;
    }

    // Compute mesh tangents
    // NOTE: To calculate mesh tangents and binormals we need mesh vertex positions and texture coordinates
    // Implementation base don: https://answers.unity.com/questions/7789/calculating-tangents-vector4.html
    public void GenMeshTangents(Mesh mesh) {
        if (mesh.tangents == null) {
            mesh.tangents = new float[mesh.vertexCount*4];
        }
        else {
            mesh.tangents = null;
            mesh.tangents = new float[mesh.vertexCount*4];
        }

        Vector3[] tan1 = new Vector3[mesh.vertexCount];
        Vector3[] tan2 = new Vector3[mesh.vertexCount];

        for (int i = 0; i < mesh.vertexCount; i += 3) {
            // Get triangle vertices
            Vector3 v1 = new Vector3(mesh.vertices[(i + 0)*3 + 0], mesh.vertices[(i + 0)*3 + 1], mesh.vertices[(i + 0)*3 + 2]);
            Vector3 v2 = new Vector3(mesh.vertices[(i + 1)*3 + 0], mesh.vertices[(i + 1)*3 + 1], mesh.vertices[(i + 1)*3 + 2]);
            Vector3 v3 = new Vector3(mesh.vertices[(i + 2)*3 + 0], mesh.vertices[(i + 2)*3 + 1], mesh.vertices[(i + 2)*3 + 2]);

            // Get triangle texcoords
            Vector2 uv1 = new Vector2(mesh.texcoords[(i + 0)*2 + 0], mesh.texcoords[(i + 0)*2 + 1]);
            Vector2 uv2 = new Vector2(mesh.texcoords[(i + 1)*2 + 0], mesh.texcoords[(i + 1)*2 + 1]);
            Vector2 uv3 = new Vector2(mesh.texcoords[(i + 2)*2 + 0], mesh.texcoords[(i + 2)*2 + 1]);

            float x1 = v2.x - v1.x;
            float y1 = v2.y - v1.y;
            float z1 = v2.z - v1.z;
            float x2 = v3.x - v1.x;
            float y2 = v3.y - v1.y;
            float z2 = v3.z - v1.z;

            float s1 = uv2.x - uv1.x;
            float t1 = uv2.y - uv1.y;
            float s2 = uv3.x - uv1.x;
            float t2 = uv3.y - uv1.y;

            float div = s1*t2 - s2*t1;
            float r = (div == 0.0f)? 0.0f : 1.0f/div;

            Vector3 sdir = new Vector3((t2*x1 - t1*x2)*r, (t2*y1 - t1*y2)*r, (t2*z1 - t1*z2)*r);
            Vector3 tdir = new Vector3((s1*x2 - s2*x1)*r, (s1*y2 - s2*y1)*r, (s1*z2 - s2*z1)*r);

            tan1[i + 0] = sdir;
            tan1[i + 1] = sdir;
            tan1[i + 2] = sdir;

            tan2[i + 0] = tdir;
            tan2[i + 1] = tdir;
            tan2[i + 2] = tdir;
        }

        // Compute tangents considering normals
        for (int i = 0; i < mesh.vertexCount; i++) {
            Vector3 normal = new Vector3(mesh.normals[i*3 + 0], mesh.normals[i*3 + 1], mesh.normals[i*3 + 2]);
            Vector3 tangent = tan1[i];

            // TODO: Review, not sure if tangent computation is right, just used reference proposed maths...
            if(COMPUTE_TANGENTS_METHOD_01) {
                Vector3 tmp = Raymath.Vector3Subtract(tangent, Raymath.Vector3Scale(normal, Vector3DotProduct(normal, tangent)));
                tmp = Raymath.Vector3Normalize(tmp);
                mesh.tangents[i * 4 + 0] = tmp.x;
                mesh.tangents[i * 4 + 1] = tmp.y;
                mesh.tangents[i * 4 + 2] = tmp.z;
                mesh.tangents[i * 4 + 3] = 1.0f;
            }
            else {
                Raymath.Vector3OrthoNormalize(normal, tangent);
                mesh.tangents[i * 4 + 0] = tangent.x;
                mesh.tangents[i * 4 + 1] = tangent.y;
                mesh.tangents[i * 4 + 2] = tangent.z;
                mesh.tangents[i * 4 + 3] = (Vector3DotProduct(Raymath.Vector3CrossProduct(normal, tangent), tan2[i]) < 0.0f) ? -1.0f : 1.0f;
            }
        }

        if (mesh.vboId != null) {
            if (mesh.vboId[RLGL.rlShaderLocationIndex.RL_SHADER_LOC_VERTEX_TANGENT] != 0) {
                // Upate existing vertex buffer
                RLGL.rlUpdateVertexBuffer(mesh.vboId[RLGL.rlShaderLocationIndex.RL_SHADER_LOC_VERTEX_TANGENT], mesh.tangents, mesh.vertexCount*4);
            }
            else {
                // Load a new tangent attributes buffer
                mesh.vboId[RLGL.rlShaderLocationIndex.RL_SHADER_LOC_VERTEX_TANGENT] = RLGL.rlLoadVertexBuffer(mesh.tangents, false);
            }

            RLGL.rlEnableVertexArray(mesh.vaoId);
            RLGL.rlSetVertexAttribute(4, 4, RLGL.RL_FLOAT, false, 0, 0);
            RLGL.rlEnableVertexAttribute(4);
            RLGL.rlDisableVertexArray();
        }

        Tracelog(LOG_INFO, "MESH: Tangents data computed and uploaded for provided mesh");
    }

    // Compute mesh binormals (aka bitangent)
    private void GenMeshBinormals(Mesh mesh) {
        for (int i = 0; i < mesh.vertexCount; i++) {
            //Vector3 normal = { mesh->normals[i*3 + 0], mesh->normals[i*3 + 1], mesh->normals[i*3 + 2] };
            //Vector3 tangent = { mesh->tangents[i*4 + 0], mesh->tangents[i*4 + 1], mesh->tangents[i*4 + 2] };
            //Vector3 binormal = Vector3Scale(Vector3CrossProduct(normal, tangent), mesh->tangents[i*4 + 3]);

            // TODO: Register computed binormal in mesh->binormal?
        }
    }

    // Draw a model (with texture if set)
    public void DrawModel(Model model, Vector3 position, float scale, Color tint) {
        Vector3 vScale = new Vector3(scale, scale, scale);
        Vector3 rotationAxis = new Vector3(0.0f, 1.0f, 0.0f);

        DrawModelEx(model, position, rotationAxis, 0.0f, vScale, tint);
    }

    // Draw a model with extended parameters
    public void DrawModelEx(Model model, Vector3 position, Vector3 rotationAxis, float rotationAngle, Vector3 scale, Color tint) {
        // Calculate transformation matrix from function parameters
        // Get transform matrix (rotation -> scale -> translation)
        Matrix matScale = Raymath.MatrixScale(scale.x, scale.y, scale.z);
        Matrix matRotation = Raymath.MatrixRotate(rotationAxis, rotationAngle*Raymath.DEG2RAD);
        Matrix matTranslation = Raymath.MatrixTranslate(position.x, position.y, position.z);

        Matrix matTransform = Raymath.MatrixMultiply(Raymath.MatrixMultiply(matScale, matRotation), matTranslation);

        // Combine model transformation matrix (model.transform) with matrix generated by function parameters (matTransform)
        model.transform = Raymath.MatrixMultiply(model.transform, matTransform);

        for (int i = 0; i < model.meshCount; i++) {
            Color color = model.materials[model.meshMaterial[i]].maps[MATERIAL_MAP_DIFFUSE].color;

            Color colorTint = WHITE;
            colorTint.r = (int) (((color.r/255.0f)*(tint.r/255.0f))*255.0f);
            colorTint.g = (int) (((color.g/255.0f)*(tint.g/255.0f))*255.0f);
            colorTint.b = (int) (((color.b/255.0f)*(tint.b/255.0f))*255.0f);
            colorTint.a = (int) (((color.a/255.0f)*(tint.a/255.0f))*255.0f);

            model.materials[model.meshMaterial[i]].maps[MATERIAL_MAP_DIFFUSE].color = colorTint;
            DrawMesh(model.meshes[i], model.materials[model.meshMaterial[i]], model.transform);
            model.materials[model.meshMaterial[i]].maps[MATERIAL_MAP_DIFFUSE].color = color;
        }
    }

    // Draw a model wires (with texture if set)
    public void DrawModelWires(Model model, Vector3 position, float scale, Color tint) {
        RLGL.rlEnableWireMode();

        DrawModel(model, position, scale, tint);

        RLGL.rlDisableWireMode();
    }

    // Draw a model wires (with texture if set) with extended parameters
    public void DrawModelWiresEx(Model model, Vector3 position, Vector3 rotationAxis, float rotationAngle, Vector3 scale, Color tint) {
        RLGL.rlEnableWireMode();

        DrawModelEx(model, position, rotationAxis, rotationAngle, scale, tint);

        RLGL.rlDisableWireMode();
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

    // Check collision between two spheres
    boolean CheckCollisionSpheres(Vector3 center1, float radius1, Vector3 center2, float radius2)
    {

        // Simple way to check for collision, just checking distance between two points
        // Unfortunately, sqrtf() is a costly operation, so we avoid it with following solution
        /*
        float dx = center1.x - center2.x;      // X distance between centers
        float dy = center1.y - center2.y;      // Y distance between centers
        float dz = center1.z - center2.z;      // Z distance between centers

        float distance = sqrtf(dx*dx + dy*dy + dz*dz);  // Distance between centers

        if (distance <= (radius1 + radius2)) collision = true;
        */

        // Check for distances squared to avoid sqrtf()

        return Vector3DotProduct(Vector3Subtract(center2, center1), Vector3Subtract(center2, center1)) <= (radius1 + radius2) * (radius1 + radius2);
    }

    // Check collision between two boxes
    // NOTE: Boxes are defined by two points minimum and maximum
    boolean CheckCollisionBoxes(BoundingBox box1, BoundingBox box2) {
        boolean collision = true;

        if ((box1.max.x >= box2.min.x) && (box1.min.x <= box2.max.x)) {
            if ((box1.max.y < box2.min.y) || (box1.min.y > box2.max.y)) {
                collision = false;
            }
            if ((box1.max.z < box2.min.z) || (box1.min.z > box2.max.z)) {
                collision = false;
            }
        }
        else {
            collision = false;
        }

        return collision;
    }

    // Check collision between box and sphere
    boolean CheckCollisionBoxSphere(BoundingBox box, Vector3 center, float radius) {
        boolean collision = false;

        float dmin = 0;

        if (center.x < box.min.x) dmin += Math.pow(center.x - box.min.x, 2);
        else if (center.x > box.max.x) dmin += Math.pow(center.x - box.max.x, 2);

        if (center.y < box.min.y) dmin += Math.pow(center.y - box.min.y, 2);
        else if (center.y > box.max.y) dmin += Math.pow(center.y - box.max.y, 2);

        if (center.z < box.min.z) dmin += Math.pow(center.z - box.min.z, 2);
        else if (center.z > box.max.z) dmin += Math.pow(center.z - box.max.z, 2);

        if (dmin <= (radius*radius)) collision = true;

        return collision;
    }

    // Get collision info between ray and sphere
    public RayCollision GetRayCollisionSphere(Ray ray, Vector3 center, float radius) {
        RayCollision collision = new RayCollision();

        Vector3 raySpherePos = Vector3Subtract(center, ray.position);
        float vector = Vector3DotProduct(raySpherePos, ray.direction);
        float distance = Vector3Length(raySpherePos);
        float d = radius*radius - (distance*distance - vector*vector);

        collision.hit = d >= 0.0f;

        // Check if ray origin is inside the sphere to calculate the correct collision point
        if (distance < radius) {
            collision.distance = (float) (vector + Math.sqrt(d));

            // Calculate collision point
            collision.point = Vector3Add(ray.position, Vector3Scale(ray.direction, collision.distance));

            // Calculate collision normal (pointing outwards)
            collision.normal = Vector3Negate(Vector3Normalize(Vector3Subtract(collision.point, center)));
        }
        else {
            collision.distance = (float) (vector - Math.sqrt(d));

            // Calculate collision point
            collision.point = Vector3Add(ray.position, Vector3Scale(ray.direction, collision.distance));

            // Calculate collision normal (pointing inwards)
            collision.normal = Vector3Normalize(Vector3Subtract(collision.point, center));
        }

        return collision;
    }

    // Get collision info between ray and box
    public RayCollision GetRayCollisionBox(Ray ray, BoundingBox box) {
        RayCollision collision = new RayCollision();

        // Note: If ray.position is inside the box, the distance is negative (as if the ray was reversed)
        // Reversing ray.direction will give use the correct result.
        boolean insideBox = (ray.position.x > box.min.x) && (ray.position.x < box.max.x) &&
                (ray.position.y > box.min.y) && (ray.position.y < box.max.y) &&
                (ray.position.z > box.min.z) && (ray.position.z < box.max.z);

        if (insideBox) ray.direction = Vector3Negate(ray.direction);

        float[] t = new float[11];

        t[8] = 1.0f/ray.direction.x;
        t[9] = 1.0f/ray.direction.y;
        t[10] = 1.0f/ray.direction.z;

        t[0] = (box.min.x - ray.position.x)*t[8];
        t[1] = (box.max.x - ray.position.x)*t[8];
        t[2] = (box.min.y - ray.position.y)*t[9];
        t[3] = (box.max.y - ray.position.y)*t[9];
        t[4] = (box.min.z - ray.position.z)*t[10];
        t[5] = (box.max.z - ray.position.z)*t[10];
        t[6] = (float)Math.max(Math.max(Math.min(t[0], t[1]), Math.min(t[2], t[3])), Math.min(t[4], t[5]));
        t[7] = (float)Math.min(Math.min(Math.max(t[0], t[1]), Math.max(t[2], t[3])), Math.max(t[4], t[5]));

        collision.hit = !((t[7] < 0) || (t[6] > t[7]));
        collision.distance = t[6];
        collision.point = Vector3Add(ray.position, Vector3Scale(ray.direction, collision.distance));

        // Get box center point
        collision.normal = Vector3Lerp(box.min, box.max, 0.5f);
        // Get vector center point->hit point
        collision.normal = Vector3Subtract(collision.point, collision.normal);
        // Scale vector to unit cube
        // NOTE: We use an additional .01 to fix numerical errors
        collision.normal = Vector3Scale(collision.normal, 2.01f);
        collision.normal = Vector3Divide(collision.normal, Vector3Subtract(box.max, box.min));
        // The relevant elemets of the vector are now slightly larger than 1.0f (or smaller than -1.0f)
        // and the others are somewhere between -1.0 and 1.0 casting to int is exactly our wanted normal!
        collision.normal.x = (float)((int)collision.normal.x);
        collision.normal.y = (float)((int)collision.normal.y);
        collision.normal.z = (float)((int)collision.normal.z);

        collision.normal = Vector3Normalize(collision.normal);

        if (insideBox) {
            // Reset ray.direction
            ray.direction = Vector3Negate(ray.direction);
            // Fix result
            collision.distance *= -1.0f;
            collision.normal = Vector3Negate(collision.normal);
        }

        return collision;
    }

    // Get collision info between ray and mesh
    public RayCollision GetRayCollisionMesh(Ray ray, Mesh mesh, Matrix transform) {
        RayCollision collision = new RayCollision();

        // Check if mesh vertex data on CPU for testing
        if (mesh.vertices != null) {
            int triangleCount = mesh.triangleCount;

            // Test against all triangles in mesh
            for (int i = 0; i < triangleCount; i++) {
                Vector3 a, b, c;
                Vector3[] vertdata = new Vector3[mesh.vertices.length];

                for(int j = 0, k = 0; j < vertdata.length; j+=3, k++) {
                    vertdata[k] = new Vector3(mesh.vertices[j], mesh.vertices[j + 1], mesh.vertices[j + 2]);
                }

                if (mesh.indices != null) {
                    a = vertdata[(int) mesh.indices[i*3 + 0]];
                    b = vertdata[(int) mesh.indices[i*3 + 1]];
                    c = vertdata[(int) mesh.indices[i*3 + 2]];
                }
                else {
                    a = vertdata[i*3 + 0];
                    b = vertdata[i*3 + 1];
                    c = vertdata[i*3 + 2];
                }

                a = Vector3Transform(a, transform);
                b = Vector3Transform(b, transform);
                c = Vector3Transform(c, transform);

                RayCollision triHitInfo = GetRayCollisionTriangle(ray, a, b, c);

                if (triHitInfo.hit) {
                    // Save the closest hit triangle
                    if ((!collision.hit) || (collision.distance > triHitInfo.distance)) collision = triHitInfo;
                }
            }
        }

        return collision;
    }

    // Get collision info between ray and model
    public RayCollision GetRayCollisionModel(Ray ray, Model model) {
        RayCollision collision = new RayCollision();

        for (int m = 0; m < model.meshCount; m++) {
            RayCollision meshHitInfo = GetRayCollisionMesh(ray, model.meshes[m], model.transform);

            if (meshHitInfo.hit) {
                // Save the closest hit mesh
                if ((!collision.hit) || (collision.distance > meshHitInfo.distance)) collision = meshHitInfo;
            }
        }

        return collision;
    }

    // Get collision info between ray and triangle
    // NOTE: The points are expected to be in counter-clockwise winding
    // NOTE: Based on https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
    public RayCollision GetRayCollisionTriangle(Ray ray, Vector3 p1, Vector3 p2, Vector3 p3) {
        float EPSILON = 0.000001f;        // A small number

        RayCollision collision = new RayCollision();
        Vector3 edge1 = new Vector3();
        Vector3 edge2 = new Vector3();
        Vector3 p, q, tv;
        float det, invDet, u, v, t;

        // Find vectors for two edges sharing V1
        edge1 = Vector3Subtract(p2, p1);
        edge2 = Vector3Subtract(p3, p1);

        // Begin calculating determinant - also used to calculate u parameter
        p = Vector3CrossProduct(ray.direction, edge2);

        // If determinant is near zero, ray lies in plane of triangle or ray is parallel to plane of triangle
        det = Vector3DotProduct(edge1, p);

        // Avoid culling!
        if ((det > -EPSILON) && (det < EPSILON)) {
            return collision;
        }

        invDet = 1.0f/det;

        // Calculate distance from V1 to ray origin
        tv = Vector3Subtract(ray.position, p1);

        // Calculate u parameter and test bound
        u = Vector3DotProduct(tv, p)*invDet;

        // The intersection lies outside of the triangle
        if ((u < 0.0f) || (u > 1.0f)) {
            return collision;
        }

        // Prepare to test v parameter
        q = Vector3CrossProduct(tv, edge1);

        // Calculate V parameter and test bound
        v = Vector3DotProduct(ray.direction, q)*invDet;

        // The intersection lies outside of the triangle
        if ((v < 0.0f) || ((u + v) > 1.0f)) {
            return collision;
        }

        t = Vector3DotProduct(edge2, q)*invDet;

        if (t > EPSILON) {
            // Ray hit, get hit point and normal
            collision.hit = true;
            collision.distance = t;
            collision.normal = Vector3Normalize(Vector3CrossProduct(edge1, edge2));
            collision.point = Vector3Add(ray.position, Vector3Scale(ray.direction, t));
        }

        return collision;
    }

    // Get collision info between ray and quad
    // NOTE: The points are expected to be in counter-clockwise winding
    public RayCollision GetRayCollisionQuad(Ray ray, Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p4) {
        RayCollision collision = new RayCollision();

        collision = GetRayCollisionTriangle(ray, p1, p2, p4);

        if (!collision.hit) {
            collision = GetRayCollisionTriangle(ray, p2, p3, p4);
        }

        return collision;
    }

    // Load default material (Supports: DIFFUSE, SPECULAR, NORMAL maps)
    public Material LoadMaterialDefault() {
        Material material = new Material();
        material.maps = new MaterialMap[MAX_MATERIAL_MAPS];
        for (int i = 0; i < material.maps.length; i++) {
            material.maps[i] = new MaterialMap();
        }

        // Using rlgl default shader
        material.shader.id = RLGL.rlGetShaderIdDefault();
        material.shader.locs = RLGL.rlGetShaderLocsDefault();

        // Using rlgl default texture (1x1 pixel, UNCOMPRESSED_R8G8B8A8, 1 mipmap)
        material.maps[MATERIAL_MAP_DIFFUSE].texture = new Texture2D(rlGetTextureIdDefault(), 1, 1, 1, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
        //material.maps[MATERIAL_MAP_NORMAL].texture;         // NOTE: By default, not set
        //material.maps[MATERIAL_MAP_SPECULAR].texture;       // NOTE: By default, not set

        material.maps[MATERIAL_MAP_DIFFUSE].color = WHITE;    // Diffuse color
        material.maps[MATERIAL_MAP_SPECULAR].color = WHITE;   // Specular color

        return material;
    }

    private Model LoadOBJ(String fileName) {

        Model model = new Model();

        try {
            String filetext = FileIO.LoadFileText(fileName);

            OBJLoader loader = new OBJLoader();
            boolean success = loader.ReadOBJ(filetext, true);

            if(success) {
                Tracelog(LOG_INFO, "MODEL: ["+fileName+"] OBJ data loaded successfully: "+ loader.objInfo.totalMaterials+" meshes/"+loader.objInfo.totalMaterials+" materials");
            }
            else {
                Tracelog(LOG_WARNING, "MODEL: ["+fileName+"] Failed to load OBJ data");
                return model;
            }

            model.meshCount = loader.objInfo.totalMaterials;

            // Init model materials array
            if(loader.objInfo.totalMaterials > 0) {
                model.materialCount = loader.objInfo.totalMaterials;
                model.materials = new Material[model.materialCount];
                Tracelog(LOG_INFO, "MODEL: model has " + model.materialCount + " material meshes.");
            }
            else {
                model.meshCount = 1;
                Tracelog(LOG_INFO, "MODEL: No materials, putting all meshes in a default material");
            }

            model.meshes = new Mesh[model.meshCount];
            model.meshMaterial = new int[model.meshCount];

            // Count the faces for each material
            int[] matFaces = new int[model.meshCount];

            // if no materials are present use all faces on one mesh
            if(loader.objInfo.totalMaterials > 0) {
                for(int fi = 0; fi < loader.objInfo.totalFaces; fi++) {
                    int idx = loader.objInfo.materialIds[fi];
                    matFaces[idx]++;
                }
            }
            else {
                matFaces[0] = loader.objInfo.totalFaces;
            }

            //--------------------------------------
            // Create the material meshes

            // Running counts/indexes for each material mesh as we are
            // building them at the same time
            int[] vCount = new int[model.meshCount];
            int[] vtCount = new int[model.meshCount];
            int[] vnCount = new int[model.meshCount];
            int[] faceCount = new int[model.meshCount];

            // Allocate each of the material meshes
            for (int mi = 0; mi < model.meshCount; mi++) {
                model.meshes[mi] = new Mesh();
                model.meshes[mi].vertexCount = matFaces[mi]*3;
                model.meshes[mi].triangleCount = matFaces[mi];
                model.meshes[mi].vertices = new float[model.meshes[mi].vertexCount*3];
                model.meshes[mi].texcoords = new float[model.meshes[mi].vertexCount*2];
                model.meshes[mi].normals = new float[model.meshes[mi].vertexCount*3];
                model.meshMaterial[mi] = mi;
            }

            // Scan through the combined sub meshes and pick out each material mesh
            for (int af = 0; af < loader.objInfo.totalFaces; af++) {
                int mm = loader.objInfo.materialIds[af];
                if (mm == -1) {
                    mm = 0;
                }

                // Get indices for the face
                OBJLoader.OBJVertexIndex idx0 = loader.objInfo.faces[3*af + 0];
                OBJLoader.OBJVertexIndex idx1 = loader.objInfo.faces[3*af + 1];
                OBJLoader.OBJVertexIndex idx2 = loader.objInfo.faces[3*af + 2];

                // Fill vertices buffer (float) using vertex index of the face
                // TODO: 11/07/2022 Eventually I'd like to be able to remove the - 1 from the following bits, but it works well enough for now...  
                for (int v = 0; v < 3; v++) { model.meshes[mm].vertices[vCount[mm] + v] = loader.objInfo.vertices[(idx0.vIndex - 1) * 3 + v]; } vCount[mm] +=3;
                for (int v = 0; v < 3; v++) { model.meshes[mm].vertices[vCount[mm] + v] = loader.objInfo.vertices[(idx1.vIndex - 1) * 3 + v]; } vCount[mm] +=3;
                for (int v = 0; v < 3; v++) { model.meshes[mm].vertices[vCount[mm] + v] = loader.objInfo.vertices[(idx2.vIndex - 1) * 3 + v]; } vCount[mm] +=3;

                //System.out.println(Arrays.toString(model.meshes[mm].vertices));

                if (loader.objInfo.totalTexcoords > 0) {
                    // Fill texcoords buffer (float) using vertex index of the face
                    // NOTE: Y-coordinate must be flipped upside-down to account for
                    // raylib's upside down textures...
                    model.meshes[mm].texcoords[vtCount[mm] + 0] = loader.objInfo.texcoords[(idx0.vtIndex - 1) * 2 + 0];
                    model.meshes[mm].texcoords[vtCount[mm] + 1] = 1.0f - loader.objInfo.texcoords[(idx0.vtIndex - 1) * 2 + 1];
                    vtCount[mm] += 2;
                    model.meshes[mm].texcoords[vtCount[mm] + 0] = loader.objInfo.texcoords[(idx1.vtIndex - 1) * 2 + 0];
                    model.meshes[mm].texcoords[vtCount[mm] + 1] = 1.0f - loader.objInfo.texcoords[(idx1.vtIndex - 1) * 2 + 1];
                    vtCount[mm] += 2;
                    model.meshes[mm].texcoords[vtCount[mm] + 0] = loader.objInfo.texcoords[(idx2.vtIndex - 1) * 2 + 0];
                    model.meshes[mm].texcoords[vtCount[mm] + 1] = 1.0f - loader.objInfo.texcoords[(idx2.vtIndex - 1) * 2 + 1];
                    vtCount[mm] += 2;

                }

                if (loader.objInfo.totalNormals > 0) {
                    // Fill normals buffer (float) using vertex index of the face
                    for (int v = 0; v < 3; v++) { model.meshes[mm].normals[vnCount[mm] + v] = loader.objInfo.normals[(idx0.vnIndex - 1) * 3 + v]; } vnCount[mm] += 3;
                    for (int v = 0; v < 3; v++) { model.meshes[mm].normals[vnCount[mm] + v] = loader.objInfo.normals[(idx1.vnIndex - 1) * 3 + v]; } vnCount[mm] += 3;
                    for (int v = 0; v < 3; v++) { model.meshes[mm].normals[vnCount[mm] + v] = loader.objInfo.normals[(idx2.vnIndex - 1) * 3 + v]; } vnCount[mm] += 3;
                }
            }

            // Init model materials
            for (int m = 0; m < loader.objInfo.totalMaterials; m++) {
                // Init material to default
                // NOTE: Uses default shader, which only supports MATERIAL_MAP_DIFFUSE
                model.materials[m] = LoadMaterialDefault();

                // Get default texture, in case no texture is defined
                // NOTE: rlgl default texture is a 1x1 pixel UNCOMPRESSED_R8G8B8A8
                model.materials[m].maps[MATERIAL_MAP_DIFFUSE].texture = new Texture2D(rlGetTextureIdDefault(), 1, 1, 1, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);

                if (loader.mtlInfo.materials[m].diffuse_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_DIFFUSE].texture = rTextures.LoadTexture(loader.mtlInfo.materials[m].diffuse_texname);  //char *diffuse_texname; // map_Kd
                }

                model.materials[m].maps[MATERIAL_MAP_DIFFUSE].color = new Color((int) (loader.mtlInfo.materials[m].diffuse[0]*255.0f), (int) (loader.mtlInfo.materials[m].diffuse[1]*255.0f), (int) (loader.mtlInfo.materials[m].diffuse[2]*255.0f), 255); //float diffuse[3];
                model.materials[m].maps[MATERIAL_MAP_DIFFUSE].value = 0.0f;

                if (loader.mtlInfo.materials[m].specular_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_SPECULAR].texture = rTextures.LoadTexture(loader.mtlInfo.materials[m].specular_texname);  //char *specular_texname; // map_Ks
                }

                model.materials[m].maps[MATERIAL_MAP_SPECULAR].color = new Color((int) (loader.mtlInfo.materials[m].specular[0]*255.0f), (int) (loader.mtlInfo.materials[m].specular[1]*255.0f), (int) (loader.mtlInfo.materials[m].specular[2]*255.0f), 255); //float specular[3];
                model.materials[m].maps[MATERIAL_MAP_SPECULAR].value = 0.0f;

                if (loader.mtlInfo.materials[m].bump_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_NORMAL].texture = rTextures.LoadTexture(loader.mtlInfo.materials[m].bump_texname);  //char *bump_texname; // map_bump, bump
                }

                model.materials[m].maps[MATERIAL_MAP_NORMAL].color = WHITE;
                model.materials[m].maps[MATERIAL_MAP_NORMAL].value = loader.mtlInfo.materials[m].shininess;

                model.materials[m].maps[MATERIAL_MAP_EMISSION].color = new Color((int) (loader.mtlInfo.materials[m].emission[0]*255.0f), (int) (loader.mtlInfo.materials[m].emission[1]*255.0f), (int) (loader.mtlInfo.materials[m].emission[2]*255.0f), 255); //float emission[3];

                if (loader.mtlInfo.materials[m].displacement_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_HEIGHT].texture = rTextures.LoadTexture(loader.mtlInfo.materials[m].displacement_texname);  //char *displacement_texname; // disp
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return model;
    }

}
