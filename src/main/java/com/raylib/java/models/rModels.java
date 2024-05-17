package com.raylib.java.models;

import com.creedvi.utils.gltfj.gltf.*;
import com.creedvi.utils.gltfj.gltf.mesh.gltfj_Primitive;
import com.creedvi.utils.gltfj.gltfj;
import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.rCore;
import com.raylib.java.core.ray.Ray;
import com.raylib.java.core.ray.RayCollision;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.models.iqm.*;
import com.raylib.java.raymath.*;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Image;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.textures.rTextures;
import com.raylib.java.utils.FileIO;
import com.raylib.java.utils.OBJLoader;
import com.raylib.java.utils.VoxLoader;
import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Base64;

import static com.raylib.java.Config.*;
import static com.raylib.java.core.Color.BLACK;
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
import static com.raylib.java.utils.Tracelog.TracelogType.*;

public class rModels{

    public static class MaterialMapIndex {
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

    final private Raylib context;

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    public rModels(Raylib context) {
        this.context = context;
    }

    private float GRAY_VALUE(Color c) {
        return (float)((c.r+c.g+c.b)/3f);
    }

    private static boolean COLOR_EQUAL(Color col1, Color col2) {
        return ((col1.r == col2.r)&&(col1.g == col2.g)&&(col1.b == col2.b)&&(col1.a == col2.a));
    }

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
        RLGL.rlCheckRenderBatchLimit(8);

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


        if (SUPPORT_FILEFORMAT_OBJ && rCore.IsFileExtension(fileName, ".obj")) {
            model = LoadOBJ(fileName);
        }
        else if (SUPPORT_FILEFORMAT_IQM && rCore.IsFileExtension(fileName, ".iqm")) {
            model = LoadIQM(fileName);
        }
        else if (SUPPORT_FILEFORMAT_GLTF && ((rCore.IsFileExtension(fileName, ".gltf") || rCore.IsFileExtension(fileName, ".glb"))))  {
            model = LoadGLTF(fileName);
        }
        else if(SUPPORT_FILEFORMAT_VOX && rCore.IsFileExtension(fileName, ".vox")) {
            model = LoadVOX(fileName);
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
        // we don't unload the material but just free its maps,
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
        // we don't unload the material but just free its maps,
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
    public void UploadMesh(Mesh mesh, boolean dynamic) {
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
                mesh.vboId[3] = rlLoadVertexBuffer(mesh.colors, dynamic);
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
                float[] value = {0.0f, 0.0f} ;
                rlSetVertexAttributeDefault(5, value, RLGL.rlShaderAttributeDataType.RL_SHADER_ATTRIB_VEC2, 2);
                rlDisableVertexAttribute(5);
            }

            if (mesh.indices != null) {
                mesh.vboId[6] = RLGL.rlLoadVertexBufferElement(mesh.indices, dynamic);
            }

            if (mesh.indicesS != null) {
                mesh.vboId[6] = RLGL.rlLoadVertexBufferElement(mesh.indicesS, dynamic);
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
    public void UpdateMeshBuffer(Mesh mesh, int index, byte[] data, int offset) {
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
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_VIEW] != -1) {
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_VIEW], matView);
            }
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION] != -1) {
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION], matProjection);
            }

            // Model transformation matrix is sent to shader uniform location: SHADER_LOC_MATRIX_MODEL
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL] != -1) {
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL], transform);
            }

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

            // Try binding vertex array objects (VAO) or use VBOs if not possible
            // WARNING: UploadMesh() enables all vertex attributes available in mesh and sets default attribute values
            // for shader expected vertex attributes that are not provided by the mesh (i.e. colors)
            // This could be a dangerous approach because different meshes with different shaders can enable/disable some attributes
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
                    }
                    else {
                        // Set default value for defined vertex attribute in shader but not provided by mesh
                        // WARNING: It could result in GPU undefined behaviour
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

                if (mesh.indices != null || mesh.indicesS != null) {
                    RLGL.rlEnableVertexBufferElement(mesh.vboId[6]);
                }
            }

            // WARNING: Disable vertex attribute color input if mesh can not provide that data (despite location being enabled in shader)
            if (mesh.vboId[3] == 0) rlDisableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR]);

            int eyeCount = 1;
            if (RLGL.rlIsStereoRendererEnabled()) {
                eyeCount = 2;
            }

            for (int eye = 0; eye < eyeCount; eye++) {
                // Calculate model-view-projection matrix (MVP)
                Matrix matModelViewProjection = Raymath.MatrixIdentity();
                if (eyeCount == 1) {
                    matModelViewProjection = Raymath.MatrixMultiply(matModelView, matProjection);
                }
                else {
                    // Setup current eye viewport (half screen width)
                    rlViewport(eye * RLGL.rlGetFramebufferWidth() / 2, 0, rlGetFramebufferWidth() / 2, RLGL.rlGetFramebufferHeight());
                    matModelViewProjection = Raymath.MatrixMultiply(Raymath.MatrixMultiply(matModelView, RLGL.rlGetMatrixViewOffsetStereo(eye)), RLGL.rlGetMatrixProjectionStereo(eye));
                }

                // Send combined model-view-projection matrix to shader
                RLGL.rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_MVP], matModelViewProjection);

                // Draw mesh
                if (mesh.indices != null || mesh.indicesS != null) {
                    rlDrawVertexArrayElements(0, mesh.triangleCount * 3, new float[0]);
                }
                else {
                    rlDrawVertexArray(0, mesh.vertexCount);
                }
            }

            // Unbind all bound texture maps
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

    // Draw multiple mesh instances with material and different transforms
    public void DrawMeshInstanced(Mesh mesh, Material material, Matrix[] transforms, int instances) {
        if(GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            // Instancing required variables
            float[] instanceTransforms = null;
            int instancesVboId = 0;

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
                float[] values = {
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
            Matrix matModel = MatrixIdentity();
            Matrix matView = rlGetMatrixModelview();
            Matrix matModelView = MatrixIdentity();
            Matrix matProjection = rlGetMatrixProjection();

            // Upload view and projection matrices (if locations available)
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_VIEW] != -1)
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_VIEW], matView);
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION] != -1)
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION], matProjection);

            // Create instances buffer
            instanceTransforms = new float[instances*16];

            // Fill buffer with instances transformations as float16 arrays
            for (int i = 0; i < instances; i++) {
                Float16 tmp = MatrixToFloatV(transforms[i]);
                for (int j = 0; j < tmp.v.length; j++) {
                    instanceTransforms[i*16 + j] = tmp.v[j];
                }
            }

            // Enable mesh VAO to attach new buffer
            rlEnableVertexArray(mesh.vaoId);

            // This could alternatively use a static VBO and either glMapBuffer() or glBufferSubData().
            // It isn't clear which would be reliably faster in all cases and on all platforms,
            // anecdotally glMapBuffer() seems very slow (syncs) while glBufferSubData() seems
            // no faster, since we're transferring all the transform matrices anyway
            instancesVboId = rlLoadVertexBuffer(instanceTransforms, false);

            // Instances transformation matrices are send to shader attribute location: SHADER_LOC_MATRIX_MODEL
            for (int i = 0; i < 4; i++) {
                rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL] + i);
                rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL] + i, 4, RL_FLOAT, false, 16, i);
                rlSetVertexAttributeDivisor(material.shader.locs[RL_SHADER_LOC_MATRIX_MODEL] + i, 1);
            }

            rlDisableVertexBuffer();
            rlDisableVertexArray();

            // Accumulate internal matrix transform (push/pop) and view matrix
            // NOTE: In this case, model instance transformation must be computed in the shader
            matModelView = MatrixMultiply(rlGetMatrixTransform(), matView);

            // Upload model normal matrix (if locations available)
            if (material.shader.locs[RL_SHADER_LOC_MATRIX_NORMAL] != -1)
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_NORMAL], MatrixTranspose(MatrixInvert(matModel)));
            //-----------------------------------------------------

            // Bind active texture maps (if available)
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id > 0) {
                    // Select current shader texture slot
                    rlActiveTextureSlot(i);

                    // Enable texture for active slot
                    if ((i == MATERIAL_MAP_IRRADIANCE) || (i == MATERIAL_MAP_PREFILTER) || (i == MATERIAL_MAP_CUBEMAP)) {
                        rlEnableTextureCubemap(material.maps[i].texture.id);
                    }
                    else {
                        rlEnableTexture(material.maps[i].texture.id);
                    }

                    rlSetUniform(material.shader.locs[RL_SHADER_LOC_MAP_DIFFUSE + i],  new float[]{i}, RL_SHADER_UNIFORM_INT, 1);
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
                    }
                    else {
                        // Set default value for unused attribute
                        // NOTE: Required when using default shader and no VAO support
                        float[] value ={
                            1.0f, 1.0f, 1.0f, 1.0f
                        };
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
                    rlEnableVertexBuffer(mesh.vboId[5]);
                    rlSetVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD02], 2, RL_FLOAT, false, 0, 0);
                    rlEnableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD02]);
                }

                if (mesh.indices != null) {
                    rlEnableVertexBufferElement(mesh.vboId[6]);
                }
            }

            // WARNING: Disable vertex attribute color input if mesh can not provide that data (despite location being enabled in shader)
            if (mesh.vboId[3] == 0) {
                rlDisableVertexAttribute(material.shader.locs[RL_SHADER_LOC_VERTEX_COLOR]);
            }

            int eyeCount = 1;
            if (RLGL.rlIsStereoRendererEnabled()) {
                eyeCount = 2;
            }

            for (int eye = 0; eye < eyeCount; eye++) {
                // Calculate model-view-projection matrix (MVP)
                Matrix matModelViewProjection = MatrixIdentity();
                if (eyeCount == 1) matModelViewProjection = MatrixMultiply(matModelView, matProjection);
                else {
                    // Setup current eye viewport (half screen width)
                    rlViewport(eye * rlGetFramebufferWidth() / 2, 0, rlGetFramebufferWidth() / 2, rlGetFramebufferHeight());
                    matModelViewProjection = MatrixMultiply(MatrixMultiply(matModelView, rlGetMatrixViewOffsetStereo(eye)), rlGetMatrixProjectionStereo(eye));
                }

                // Send combined model-view-projection matrix to shader
                rlSetUniformMatrix(material.shader.locs[RL_SHADER_LOC_MATRIX_MVP], matModelViewProjection);

                // Draw mesh instanced
                if (mesh.indices != null){
                    rlDrawVertexArrayElementsInstanced(0, mesh.triangleCount * 3, null, instances);
                }
                else {
                    rlDrawVertexArrayInstanced(0, mesh.vertexCount, instances);
                }
            }

            // Unbind all binded texture maps
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id > 0) {
                    // Select current shader texture slot
                    rlActiveTextureSlot(i);

                    // Disable texture for active slot
                    if ((i == MATERIAL_MAP_IRRADIANCE) ||
                            (i == MATERIAL_MAP_PREFILTER) ||
                            (i == MATERIAL_MAP_CUBEMAP)) {
                        rlDisableTextureCubemap();
                    }
                    else {
                        rlDisableTexture();
                    }
                }
            }

            // Disable all possible vertex array objects (or VBOs)
            rlDisableVertexArray();
            rlDisableVertexBuffer();
            rlDisableVertexBufferElement();

            // Disable shader program
            rlDisableShader();

            // Remove instance transforms buffer
            rlUnloadVertexBuffer(instancesVboId);
            instanceTransforms = null;
        }
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

    // Export mesh data to file
    public boolean ExportMesh(Mesh mesh, String fileName) {
        boolean success = false;

        if (rCore.IsFileExtension(fileName, ".obj")) {
            // Estimated data size, it should be enough...
            int dataSize = mesh.vertexCount * ("v 0000.00f 0000.00f 0000.00f").length() +
                    (mesh.vertexCount * ("vt 0.000f 0.00f").length()) +
                    (mesh.vertexCount * ("vn 0.000f 0.00f 0.00f").length()) +
                    (mesh.triangleCount * ("f 00000/00000/00000 00000/00000/00000 00000/00000/00000").length());

            // NOTE: Text data buffer size is estimated considering mesh data size
            StringBuilder txtData = new StringBuilder();

            int byteCount = 0;
            txtData.append( "# //////////////////////////////////////////////////////////////////////////////////\n");
            txtData.append( "# //                                                                              //\n");
            txtData.append( "# // rMeshOBJ exporter v1.0 - Mesh exported as triangle faces and not optimized   //\n");
            txtData.append( "# //                                                                              //\n");
            txtData.append( "# // more info and bugs-report:  github.com/raysan5/raylib                        //\n");
            txtData.append( "# // feedback and support:       ray[at]raylib.com                                //\n");
            txtData.append( "# //                                                                              //\n");
            txtData.append( "# // Copyright (c) 2018-2022 Ramon Santamaria (@raysan5)                          //\n");
            txtData.append( "# //                                                                              //\n");
            txtData.append( "# //////////////////////////////////////////////////////////////////////////////////\n\n");
            txtData.append( "# Vertex Count:     " + mesh.vertexCount + "\n");
            txtData.append( "# Triangle Count:   " + mesh.triangleCount + "\n\n");

            txtData.append( "g mesh\n");

            for (int i = 0, v = 0; i < mesh.vertexCount; i++, v += 3) {
                txtData.append("v " + String.format(String.valueOf(mesh.vertices[v]), "%.2f") + String.format(String.valueOf(mesh.vertices[v+1]), "%.2f") + String.format(String.valueOf(mesh.vertices[v+2]), "%.2f"));
            }

            for (int i = 0, v = 0; i < mesh.vertexCount; i++, v += 2) {
                txtData.append("vt " + String.format(String.valueOf(mesh.texcoords[v]), "%.2f") + String.format(String.valueOf(mesh.texcoords[v+1]), "%.2f"));

            }

            for (int i = 0, v = 0; i < mesh.vertexCount; i++, v += 3) {
                txtData.append("v " + String.format(String.valueOf(mesh.normals[v]), "%.2f") + String.format(String.valueOf(mesh.normals[v+1]), "%.2f") + String.format(String.valueOf(mesh.normals[v+2]), "%.2f"));
            }

            if (mesh.indices != null) {
                for (int i = 0, v = 0; i < mesh.triangleCount; i++, v += 3) {
                    txtData.append( "f " +
                            mesh.indices[v] + 1 +"/"+ mesh.indices[v] + 1+"/"+ mesh.indices[v] + 1 +" "+
                            mesh.indices[v + 1] + 1+"/"+ mesh.indices[v + 1] + 1+"/"+ mesh.indices[v + 1] + 1 +" "+
                            mesh.indices[v + 2] + 1+"/"+ mesh.indices[v + 2] + 1+"/"+mesh.indices[v + 2] + 1 + "\n");
                }
            }
            else {
                for (int i = 0, v = 1; i < mesh.triangleCount; i++, v += 3) {
                    txtData.append( "f " + v+"/"+v+"/"+v+" "+ (v + 1)+"/"+(v + 1)+"/"+(v + 1)+" "+(v + 2)+"/"+(v + 2)+"/"+(v + 2)+"\n");
                }
            }

            txtData.append( "\n");

            // NOTE: Text data length exported is determined by '\0' (NULL) character
            try {
                success = FileIO.SaveFileText(fileName, txtData.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (rCore.IsFileExtension(fileName, ".raw")) {
            // TODO: Support additional file formats to export mesh vertex data
        }

        return success;
    }


    // Load materials from model file
    public Material[] LoadMaterials(String fileName) {
        Material[] materials = null;
        int count = 0;

        // TODO: Support IQM and GLTF for materials parsing

        if(SUPPORT_FILEFORMAT_MTL) {
            if (rCore.IsFileExtension(fileName, ".mtl")) {
                OBJLoader loader = new OBJLoader();
                String fileText;
                try {
                    fileText = FileIO.LoadFileText(fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                boolean result = loader.ReadMTL(fileText);
                if (result != true) {
                    Tracelog(LOG_WARNING, "MATERIAL: [" + fileName + "] Failed to parse materials file");
                }

                // TODO: Process materials to return
            }
        }
        else {
            Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to load material file");
        }

        // Set materials shader to default (DIFFUSE, SPECULAR, NORMAL)
        if (materials != null) {
            for (int i = 0; i < count; i++) {
                materials[i].shader.id = rlGetShaderIdDefault();
                materials[i].shader.locs = rlGetShaderLocsDefault();
            }
        }

        return materials;
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

    // Unload material from memory
    public void UnloadMaterial(Material material) {
        // Unload material shader (avoid unloading default shader, managed by raylib)
        if (material.shader.id != rlGetShaderIdDefault()) {
            rCore.UnloadShader(material.shader);
        }

        // Unload loaded texture maps (avoid unloading default texture, managed by raylib)
        if (material.maps != null) {
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id != rlGetTextureIdDefault()) {
                    rlUnloadTexture(material.maps[i].texture.id);
                }
            }
        }

        material.maps = null;
    }

    // Set texture for a material map type (MATERIAL_MAP_DIFFUSE, MATERIAL_MAP_SPECULAR...)
    // NOTE: Previous texture should be manually unloaded
    public void SetMaterialTexture(Material material, int mapType, Texture2D texture) {
        material.maps[mapType].texture = texture;
    }

    // Set the material for a mesh
    public void SetModelMeshMaterial(Model model, int meshId, int materialId) {
        if (meshId >= model.meshCount) {
            Tracelog(LOG_WARNING, "MESH: Id greater than mesh count");
        }
        else if (materialId >= model.materialCount) {
            Tracelog(LOG_WARNING, "MATERIAL: Id greater than material count");
        }
        else {
            model.meshMaterial[meshId] = materialId;
        }
    }

    // Load model animations from file
    public ModelAnimation[] LoadModelAnimations(String fileName) {
        ModelAnimation[] animations = null;

        if(SUPPORT_FILEFORMAT_IQM) {
            if (rCore.IsFileExtension(fileName, ".iqm")) {
                animations = LoadModelAnimationsIQM(fileName);
            }
        }
        if(SUPPORT_FILEFORMAT_GLTF) {
            //if (IsFileExtension(fileName, ".gltf;.glb")) animations = LoadModelAnimationGLTF(fileName, animCount);
        }

        return animations;
    }

    // Update model animated vertex data (positions and normals) for a given frame
    // NOTE: Updated data is uploaded to GPU
    public void UpdateModelAnimation(Model model, ModelAnimation anim, int frame) {
        if ((anim.frameCount > 0) && (anim.bones != null) && (anim.framePoses != null)) {
            if (frame >= anim.frameCount) {
                frame = frame%anim.frameCount;
            }

            for (int m = 0; m < model.meshCount; m++) {
                Mesh mesh = model.meshes[m];
                if (mesh.boneIds == null || mesh.boneWeights == null) {
                    Tracelog(LOG_WARNING, "MODEL: UpdateModelAnimation Mesh " + m + " has no connection to bones");
                    continue;
                }

                boolean updated = false; // set to true when anim vertex information is updated
                Vector3 animVertex = new Vector3();
                Vector3 animNormal = new Vector3();

                Vector3 inTranslation = new Vector3();
                Quaternion inRotation = new Quaternion();
                // Vector3 inScale = new Vector3();

                Vector3 outTranslation = new Vector3();
                Quaternion outRotation = new Quaternion();
                Vector3 outScale = new Vector3();

                int boneId = 0;
                int boneCounter = 0;
                float boneWeight = 0.0f;

                int vValues = mesh.vertexCount*3;
                for (int vCounter = 0; vCounter < vValues; vCounter+=3) {
                    mesh.animVertices[vCounter] = 0;
                    mesh.animVertices[vCounter + 1] = 0;
                    mesh.animVertices[vCounter + 2] = 0;

                    if (mesh.animNormals != null) {
                        mesh.animNormals[vCounter] = 0;
                        mesh.animNormals[vCounter + 1] = 0;
                        mesh.animNormals[vCounter + 2] = 0;
                    }

                    // Iterates over 4 bones per vertex
                    for (int j = 0; j < 4; j++, boneCounter++) {
                        boneWeight = mesh.boneWeights[boneCounter];
                        // early stop when no transformation will be applied
                        if (boneWeight == 0.0f) {
                            continue;
                        }
                        boneId = mesh.boneIds[boneCounter];
                        //int boneIdParent = model.bones[boneId].parent;
                        inTranslation = model.bindPose[boneId].translation;
                        inRotation = model.bindPose[boneId].rotation;
                        // inScale = model.bindPose[boneId].scale;
                        outTranslation = anim.framePoses[frame][boneId].translation;
                        outRotation = anim.framePoses[frame][boneId].rotation;
                        outScale = anim.framePoses[frame][boneId].scale;

                        // Vertices processing
                        // NOTE: We use meshes.vertices (default vertex position) to calculate meshes.animVertices (animated vertex position)
                        animVertex = new Vector3(mesh.vertices[vCounter], mesh.vertices[vCounter + 1], mesh.vertices[vCounter + 2]);
                        animVertex = Vector3Multiply(animVertex, outScale);
                        animVertex = Vector3Subtract(animVertex, inTranslation);
                        animVertex = Vector3RotateByQuaternion(animVertex, QuaternionMultiply(outRotation, QuaternionInvert(inRotation)));
                        animVertex = Vector3Add(animVertex, outTranslation);
                        // animVertex = Vector3Transform(animVertex, model.transform);
                        mesh.animVertices[vCounter] += animVertex.x*boneWeight;
                        mesh.animVertices[vCounter + 1] += animVertex.y*boneWeight;
                        mesh.animVertices[vCounter + 2] += animVertex.z*boneWeight;
                        updated = true;

                        // Normals processing
                        // NOTE: We use meshes.baseNormals (default normal) to calculate meshes.normals (animated normals)
                        if (mesh.normals != null) {
                            animNormal = new Vector3(mesh.normals[vCounter], mesh.normals[vCounter + 1], mesh.normals[vCounter + 2]);
                            animNormal = Vector3RotateByQuaternion(animNormal, QuaternionMultiply(outRotation, QuaternionInvert(inRotation)));
                            mesh.animNormals[vCounter] += animNormal.x*boneWeight;
                            mesh.animNormals[vCounter + 1] += animNormal.y*boneWeight;
                            mesh.animNormals[vCounter + 2] += animNormal.z*boneWeight;
                        }
                    }
                }

                // Upload new vertex data to GPU for model drawing
                // Only update data when values changed.
                if (updated){
                    rlUpdateVertexBuffer(mesh.vboId[0], mesh.animVertices, 0);    // Update vertex position
                    rlUpdateVertexBuffer(mesh.vboId[2], mesh.animNormals, 0);     // Update vertex normals
                }
            }
        }
    }

    // Unload animation array data
    public void UnloadModelAnimations(ModelAnimation[] animations, int count) {
        for (int i = 0; i < count; i++) {
            UnloadModelAnimation(animations[i]);
        }
        animations = null;
    }

    // Unload animation data
    public void UnloadModelAnimation(ModelAnimation anim) {
        for (int i = 0; i < anim.frameCount; i++) {
            anim.framePoses[i] = null;
        }

        anim.bones = null;
        anim.framePoses = null;
    }

    // Check model animation skeleton match
    // NOTE: Only number of bones and parent connections are checked
    boolean IsModelAnimationValid(Model model, ModelAnimation anim) {
        boolean result = true;

        if (model.boneCount != anim.boneCount) {
            result = false;
        }
        else {
            for (int i = 0; i < model.boneCount; i++) {
                if (model.bones[i].parent != anim.bones[i].parent) {
                    result = false; break;
                }
            }
        }

        return result;
    }

    // MESH GENERATION

    // Generate polygonal mesh
    public Mesh GenMeshPoly(int sides, float radius) {
        Mesh mesh = new Mesh();

        if (sides < 3) {
            return mesh;
        }

        int vertexCount = sides*3;

        // Vertices definition
        Vector3[] vertices = new Vector3[vertexCount];

        float d = 0.0f, dStep = 360.0f/sides;
        for (int v = 0; v < vertexCount; v += 3) {
            vertices[v] = new Vector3(0.0f, 0.0f, 0.0f);
            vertices[v + 1] = new Vector3((float)Math.sin(DEG2RAD*d)*radius, 0.0f, (float)Math.cos(DEG2RAD*d)*radius);
            vertices[v + 2] = new Vector3((float)Math.sin(DEG2RAD*(d+dStep))*radius, 0.0f, (float)Math.cos(DEG2RAD*(d+dStep))*radius);
            d += dStep;
        }

        // Normals definition
        Vector3[] normals = new Vector3[vertexCount];
        for (int n = 0; n < vertexCount; n++) {
            normals[n] = new Vector3(0.0f, 1.0f, 0.0f);   // Vector3.up;
        }

        // TexCoords definition
        Vector2[] texcoords = new Vector2[vertexCount];
        for (int n = 0; n < vertexCount; n++) {
            texcoords[n] = new Vector2(0.0f, 0.0f);
        }

        mesh.vertexCount = vertexCount;
        mesh.triangleCount = sides;
        mesh.vertices = new float[mesh.vertexCount * 3];
        mesh.texcoords = new float[mesh.vertexCount * 2];
        mesh.normals = new float[mesh.vertexCount * 3];

        // Mesh vertices position array
        for (int i = 0; i < mesh.vertexCount; i++) {
            mesh.vertices[3*i] = vertices[i].x;
            mesh.vertices[3*i + 1] = vertices[i].y;
            mesh.vertices[3*i + 2] = vertices[i].z;
        }

        // Mesh texcoords array
        for (int i = 0; i < mesh.vertexCount; i++) {
            mesh.texcoords[2*i] = texcoords[i].x;
            mesh.texcoords[2*i + 1] = texcoords[i].y;
        }

        // Mesh normals array
        for (int i = 0; i < mesh.vertexCount; i++) {
            mesh.normals[3*i] = normals[i].x;
            mesh.normals[3*i + 1] = normals[i].y;
            mesh.normals[3*i + 2] = normals[i].z;
        }

        vertices = null;
        normals = null;
        texcoords = null;

        // Upload vertex data to GPU (static mesh)
        // NOTE: mesh.vboId array is allocated inside UploadMesh()
        UploadMesh(mesh, false);

        return mesh;
    }

    // Generate plane mesh (with subdivisions)
    public Mesh GenMeshPlane(float width, float length, int resX, int resZ) {
        Mesh mesh = new Mesh();

        if(SUPPORT_CUSTOM_MESH_GEN_PLANE) {
            //TODO: Fix this.

            resX++;
            resZ++;

            // Vertices definition
            int vertexCount = resX * resZ; // vertices get reused for the faces

            Vector3[] vertices = new Vector3[vertexCount];
            for(int z = 0; z < resZ; z++) {
                // [-length/2, length/2]
                float zPos = ((float) z / (resZ - 1) - 0.5f) * length;
                for(int x = 0; x < resX; x++) {
                    // [-width/2, width/2]
                    float xPos = ((float) x / (resX - 1) - 0.5f) * width;
                    vertices[x + z * resX] = new Vector3(xPos, 0.0f, zPos);
                }
            }

            // Normals definition
            Vector3[] normals = new Vector3[vertexCount];
            for(int n = 0; n < vertexCount; n++) {
                normals[n] = new Vector3(0.0f, 1.0f, 0.0f);   // Vector3.up;
            }

            // TexCoords definition
            Vector2[] texcoords = new Vector2[vertexCount];
            for(int v = 0; v < resZ; v++) {
                for(int u = 0; u < resX; u++) {
                    texcoords[u + v * resX] = new Vector2((float) u / (resX - 1), (float) v / (resZ - 1));
                }
            }

            // Triangles definition (indices)
            int numFaces = (resX - 1) * (resZ - 1);
            int[] triangles = new int[numFaces * 6];
            int t = 0;
            for(int face = 0; face < numFaces; face++) {
                // Retrieve lower left corner from face ind
                int i = face % (resX - 1) + (face / (resZ - 1) * resX);

                triangles[t++] = i + resX;
                triangles[t++] = i + 1;
                triangles[t++] = i;

                triangles[t++] = i + resX;
                triangles[t++] = i + resX + 1;
                triangles[t++] = i + 1;
            }

            mesh.vertexCount = vertexCount;
            mesh.triangleCount = numFaces * 2;
            mesh.vertices = new float[mesh.vertexCount * 3];
            mesh.texcoords = new float[mesh.vertexCount * 2];
            mesh.normals = new float[mesh.vertexCount * 3];
            mesh.indicesS = new short[mesh.triangleCount * 3];

            // Mesh vertices position array
            for(int i = 0; i < mesh.vertexCount; i++) {
                mesh.vertices[3 * i] = vertices[i].x;
                mesh.vertices[3 * i + 1] = vertices[i].y;
                mesh.vertices[3 * i + 2] = vertices[i].z;
            }

            // Mesh texcoords array
            for(int i = 0; i < mesh.vertexCount; i++) {
                mesh.texcoords[2 * i] = texcoords[i].x;
                mesh.texcoords[2 * i + 1] = texcoords[i].y;
            }

            // Mesh normals array
            for(int i = 0; i < mesh.vertexCount; i++) {
                mesh.normals[3 * i] = normals[i].x;
                mesh.normals[3 * i + 1] = normals[i].y;
                mesh.normals[3 * i + 2] = normals[i].z;
            }

            // Mesh indices array initialization
            for(int i = 0; i < mesh.triangleCount * 3; i++) {
                mesh.indicesS[i] = (short) triangles[i];
            }
        }
        else {       // Use par_shapes library to generate plane mesh

            ParShapesMesh plane = ParShapes.par_shapes_create_plane(resX, resZ);   // No normals/texcoords generated!!!
            ParShapes.par_shapes_scale(plane, width, length, 1.0f);
            ParShapes.par_shapes_rotate(plane, -PI / 2.0f, new float[]{1, 0, 0});
            ParShapes.par_shapes_translate(plane, -width / 2, 0.0f, length / 2);

            mesh.vertices = new float[plane.ntriangles() * 3 * 3];
            mesh.texcoords = new float[plane.ntriangles() * 3 * 2];
            mesh.normals = new float[plane.ntriangles() * 3 * 3];

            mesh.vertexCount = plane.ntriangles() * 3;
            mesh.triangleCount = plane.ntriangles();

            FloatBuffer points = plane.points(plane.ntriangles()*3*3);
            FloatBuffer normals = plane.normals(plane.ntriangles()*3*3);
            FloatBuffer tcoords = plane.tcoords(plane.ntriangles()*3*2);
            IntBuffer triangles = plane.triangles(plane.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(plane);
        }

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }

    // Generated cuboid mesh
    public Mesh GenMeshCube(float width, float height, float length) {
        Mesh mesh = new Mesh();

        if(SUPPORT_CUSTOM_MESH_GEN_CUBE) {
            //TODO: Fix this.
            mesh.vertices = new float[] {
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

            mesh.texcoords = new float[] {
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

            mesh.normals = new float[] {
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f
            };

            mesh.indicesS = new short[36];

            // Indices can be initialized right now
            for(int i = 0, k = 0; i < 36; i += 6) {
                mesh.indicesS[i] = (short) (4 * k);
                mesh.indicesS[i + 1] = (short) (4 * k + 1);
                mesh.indicesS[i + 2] = (short) (4 * k + 2);
                mesh.indicesS[i + 3] = (short) (4 * k);
                mesh.indicesS[i + 4] = (short) (4 * k + 2);
                mesh.indicesS[i + 5] = (short) (4 * k + 3);

                k++;
            }

            mesh.vertexCount = 24;
            mesh.triangleCount = 12;
        }
        else {
            ParShapesMesh cube = ParShapes.par_shapes_create_cube();
            ParShapes.par_shapes_scale(cube, width, height, length);
            ParShapes.par_shapes_translate(cube, -width/2, 0.0f, -length/2);
            ParShapes.par_shapes_compute_normals(cube);

            mesh.vertices = new float[cube.ntriangles()*3*3];
            mesh.texcoords = new float[cube.ntriangles()*3*2];
            mesh.normals = new float[cube.ntriangles()*3*3];

            mesh.vertexCount = cube.ntriangles()*3;
            mesh.triangleCount = cube.ntriangles();

            FloatBuffer points = cube.points(cube.ntriangles()*3*3);
            FloatBuffer normals = cube.normals(cube.ntriangles()*3*3);
            FloatBuffer tcoords = FloatBuffer.allocate(cube.npoints()*2);
            for(int i = 0; i < tcoords.limit(); i++) { tcoords.put(0.0f); }
            IntBuffer triangles = cube.triangles(cube.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(cube);
        }

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }

    // Generate sphere mesh (standard sphere)
    public Mesh GenMeshSphere(float radius, int rings, int slices) {
        Mesh mesh = new Mesh();

        if ((rings >= 3) && (slices >= 3)) {
            ParShapesMesh sphere = ParShapes.par_shapes_create_parametric_sphere(slices, rings);
            ParShapes.par_shapes_scale(sphere, radius, radius, radius);
            // NOTE: Soft normals are computed internally

            mesh.vertices = new float[sphere.ntriangles()*3*3];
            mesh.texcoords = new float[sphere.ntriangles()*3*2];
            mesh.normals = new float[sphere.ntriangles()*3*3];

            mesh.vertexCount = sphere.ntriangles()*3;
            mesh.triangleCount = sphere.ntriangles();

            FloatBuffer points = sphere.points(sphere.ntriangles()*3*3);
            FloatBuffer normals = sphere.normals(sphere.ntriangles()*3*3);
            FloatBuffer tcoords = sphere.tcoords(sphere.ntriangles()*3*2);
            IntBuffer triangles = sphere.triangles(sphere.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(sphere);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            Tracelog(LOG_WARNING, "MESH: Failed to generate mesh: sphere");
        }

        return mesh;
    }

    // Generate hemi-sphere mesh (half sphere, no bottom cap)
    public Mesh GenMeshHemiSphere(float radius, int rings, int slices) {
        Mesh mesh = new Mesh();

        if ((rings >= 3) && (slices >= 3)) {
            if (radius < 0.0f) {
                radius = 0.0f;
            }

            ParShapesMesh sphere = ParShapes.par_shapes_create_hemisphere(slices, rings);
            ParShapes.par_shapes_scale(sphere, radius, radius, radius);
            // NOTE: Soft normals are computed internally

            mesh.vertices = new float[sphere.ntriangles()*3*3];
            mesh.texcoords = new float[sphere.ntriangles()*3*2];
            mesh.normals = new float[sphere.ntriangles()*3*3];

            mesh.vertexCount = sphere.ntriangles()*3;
            mesh.triangleCount = sphere.ntriangles();

            FloatBuffer points = sphere.points(sphere.ntriangles()*3*3);
            FloatBuffer normals = sphere.normals(sphere.ntriangles()*3*3);
            FloatBuffer tcoords = sphere.tcoords(sphere.ntriangles()*3*2);
            IntBuffer triangles = sphere.triangles(sphere.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(sphere);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            Tracelog(LOG_WARNING, "MESH: Failed to generate mesh: hemisphere");
        }

        return mesh;
    }

    // Generate cylinder mesh
    public Mesh GenMeshCylinder(float radius, float height, int slices) {
        Mesh mesh = new Mesh();

        if (slices >= 3) {
            // Instance a cylinder that sits on the Z=0 plane using the given tessellation
            // levels across the UV domain.  Think of "slices" like a number of pizza
            // slices, and "stacks" like a number of stacked rings.
            // Height and radius are both 1.0, but they can easily be changed with par_shapes_scale
            ParShapesMesh cylinder = ParShapes.par_shapes_create_cylinder(slices, 8);
            ParShapes.par_shapes_scale(cylinder, radius, radius, height);
            ParShapes.par_shapes_rotate(cylinder, -PI/2.0f, new float[]{ 1, 0, 0 });

            // Generate an orientable disk shape (top cap)
            ParShapesMesh capTop = ParShapes.par_shapes_create_disk(radius, slices, new float[]{ 0, 0, 0 }, new float[]{ 0, 0, 1 });
            // TODO: 7/19/22 tcoords?
            ParShapes.par_shapes_rotate(capTop, -PI/2.0f, new float[]{ 1, 0, 0 });
            ParShapes.par_shapes_rotate(capTop, 90*DEG2RAD, new float[]{ 0, 1, 0 });
            ParShapes.par_shapes_translate(capTop, 0, height, 0);

            // Generate an orientable disk shape (bottom cap)
            ParShapesMesh capBottom = ParShapes.par_shapes_create_disk(radius, slices, new float[]{ 0, 0, 0 }, new float[]{ 0, 0, -1 });
            // TODO: 7/19/22 tcoords?
            ParShapes.par_shapes_rotate(capBottom, PI/2.0f, new float[]{ 1, 0, 0 });
            ParShapes.par_shapes_rotate(capBottom, -90*DEG2RAD, new float[]{ 0, 1, 0 });

            ParShapes.par_shapes_merge_and_free(cylinder, capTop);
            ParShapes.par_shapes_merge_and_free(cylinder, capBottom);

            mesh.vertices = new float[cylinder.ntriangles()*3*3];
            mesh.texcoords = new float[cylinder.ntriangles()*3*2];
            mesh.normals = new float[cylinder.ntriangles()*3*3];

            mesh.vertexCount = cylinder.ntriangles()*3;
            mesh.triangleCount = cylinder.ntriangles();

            FloatBuffer points = cylinder.points(cylinder.ntriangles()*3*3);
            FloatBuffer normals = cylinder.normals(cylinder.ntriangles()*3*3);
            FloatBuffer tcoords = cylinder.tcoords(cylinder.ntriangles()*3*2);
            IntBuffer triangles = cylinder.triangles(cylinder.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(cylinder);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            Tracelog(LOG_WARNING, "MESH: Failed to generate mesh: cylinder");
        }

        return mesh;
    }

    // Generate cone/pyramid mesh
    public Mesh GenMeshCone(float radius, float height, int slices) {
        Mesh mesh = new Mesh();

        if (slices >= 3) {
            // Instance a cone that sits on the Z=0 plane using the given tessellation
            // levels across the UV domain.  Think of "slices" like a number of pizza
            // slices, and "stacks" like a number of stacked rings.
            // Height and radius are both 1.0, but they can easily be changed with par_shapes_scale
            ParShapesMesh cone = ParShapes.par_shapes_create_cone(slices, 8);
            ParShapes.par_shapes_scale(cone, radius, radius, height);
            ParShapes.par_shapes_rotate(cone, -PI/2.0f, new float[]{ 1, 0, 0 });
            ParShapes.par_shapes_rotate(cone, PI/2.0f, new float[]{ 0, 1, 0 });

            // Generate an orientable disk shape (bottom cap)
            ParShapesMesh capBottom = ParShapes.par_shapes_create_disk(radius, slices, new float[]{ 0, 0, 0 }, new float[]{ 0, 0, -1 });
            ParShapes.par_shapes_rotate(capBottom, PI/2.0f, new float[]{ 1, 0, 0 });

            ParShapes.par_shapes_merge_and_free(cone, capBottom);

            mesh.vertices = new float[cone.ntriangles()*3*3];
            mesh.texcoords = new float[cone.ntriangles()*3*2];
            mesh.normals = new float[cone.ntriangles()*3*3];

            mesh.vertexCount = cone.ntriangles()*3;
            mesh.triangleCount = cone.ntriangles();

            FloatBuffer points = cone.points(cone.ntriangles()*3*3);
            FloatBuffer normals = cone.normals(cone.ntriangles()*3*3);
            FloatBuffer tcoords = cone.tcoords(cone.ntriangles()*3*2);
            IntBuffer triangles = cone.triangles(cone.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(cone);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            Tracelog(LOG_WARNING, "MESH: Failed to generate mesh: cone");
        }

        return mesh;
    }

    // Generate torus mesh
    public Mesh GenMeshTorus(float radius, float size, int radSeg, int sides) {
        Mesh mesh = new Mesh();

        if ((sides >= 3) && (radSeg >= 3)) {
            if (radius > 1.0f) {
                radius = 1.0f;
            }
            else if (radius < 0.1f) {
                radius = 0.1f;
            }

            // Create a donut that sits on the Z=0 plane with the specified inner radius
            // The outer{ 0 } radius can be controlled with par_shapes_scale
            ParShapesMesh torus = ParShapes.par_shapes_create_torus(radSeg, sides, radius);
            ParShapes.par_shapes_scale(torus, size/2, size/2, size/2);

            mesh.vertices = new float[torus.ntriangles()*3*3];
            mesh.texcoords = new float[torus.ntriangles()*3*2];
            mesh.normals = new float[torus.ntriangles()*3*3];

            mesh.vertexCount = torus.ntriangles()*3;
            mesh.triangleCount = torus.ntriangles();

            FloatBuffer points = torus.points(torus.ntriangles()*3*3);
            FloatBuffer normals = torus.normals(torus.ntriangles()*3*3);
            FloatBuffer tcoords = torus.tcoords(torus.ntriangles()*3*2);
            IntBuffer triangles = torus.triangles(torus.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(torus);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            Tracelog(LOG_WARNING, "MESH: Failed to generate mesh: torus");
        }

        return mesh;
    }

    // Generate trefoil knot mesh
    public Mesh GenMeshKnot(float radius, float size, int radSeg, int sides) {
        Mesh mesh = new Mesh();

        if((sides >= 3) && (radSeg >= 3)) {
            if(radius > 3.0f) {
                radius = 3.0f;
            }
            else if(radius < 0.5f) {
                radius = 0.5f;
            }

            ParShapesMesh knot = ParShapes.par_shapes_create_trefoil_knot(radSeg, sides, radius);
            ParShapes.par_shapes_scale(knot, size, size, size);

            mesh.vertices = new float[knot.ntriangles() * 3 * 3];
            mesh.texcoords = new float[knot.ntriangles() * 3 * 2];
            mesh.normals = new float[knot.ntriangles() * 3 * 3];

            mesh.vertexCount = knot.ntriangles() * 3;
            mesh.triangleCount = knot.ntriangles();

            FloatBuffer points = knot.points(knot.ntriangles()*3*3);
            FloatBuffer normals = knot.normals(knot.ntriangles()*3*3);
            FloatBuffer tcoords = knot.tcoords(knot.ntriangles()*3*2);
            IntBuffer triangles = knot.triangles(knot.ntriangles()*3*3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k*3] = points.get(triangles.get(k)*3);
                mesh.vertices[k*3 + 1] = points.get(triangles.get(k)*3 + 1);
                mesh.vertices[k*3 + 2] = points.get(triangles.get(k)*3 + 2);

                mesh.normals[k*3] = normals.get(triangles.get(k)*3);
                mesh.normals[k*3 + 1] = normals.get(triangles.get(k)*3 + 1);
                mesh.normals[k*3 + 2] = normals.get(triangles.get(k)*3 + 2);

                mesh.texcoords[k*2] = tcoords.get(triangles.get(k)*2);
                mesh.texcoords[k*2 + 1] = tcoords.get(triangles.get(k)*2 + 1);
            }

            ParShapes.par_shapes_free_mesh(knot);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            Tracelog(LOG_WARNING, "MESH: Failed to generate mesh: knot");
        }

        return mesh;
    }

    // Generate a mesh from heightmap
    // NOTE: Vertex data is uploaded to GPU
    public Mesh GenMeshHeightmap(Image heightmap, Vector3 size) {
        Mesh mesh = new Mesh();

        int mapX = heightmap.width;
        int mapZ = heightmap.height;

        Color[] pixels = context.textures.LoadImageColors(heightmap);

        // NOTE: One vertex per pixel
        mesh.triangleCount = (mapX-1)*(mapZ-1)*2;    // One quad every four pixels

        mesh.vertexCount = mesh.triangleCount*3;

        mesh.vertices = new float[mesh.vertexCount*3];
        mesh.normals = new float[mesh.vertexCount*3];
        mesh.texcoords = new float[mesh.vertexCount*2];
        mesh.colors = null;

        int vCounter = 0;       // Used to count vertices float by float
        int tcCounter = 0;      // Used to count texcoords float by float
        int nCounter = 0;       // Used to count normals float by float

        Vector3 scaleFactor = new Vector3(size.x/mapX, size.y/255.0f, size.z/mapZ);

        Vector3 vA = new Vector3();
        Vector3 vB = new Vector3();
        Vector3 vC = new Vector3();
        Vector3 vN = new Vector3();

        for (int z = 0; z < mapZ-1; z++) {
            for (int x = 0; x < mapX-1; x++) {
                // Fill vertices array with data
                //----------------------------------------------------------

                // one triangle - 3 vertex
                mesh.vertices[vCounter] = (float)x*scaleFactor.x;
                mesh.vertices[vCounter + 1] = GRAY_VALUE(pixels[x + z*mapX])*scaleFactor.y;
                mesh.vertices[vCounter + 2] = (float)z*scaleFactor.z;

                mesh.vertices[vCounter + 3] = (float)x*scaleFactor.x;
                mesh.vertices[vCounter + 4] = GRAY_VALUE(pixels[x + (z + 1)*mapX])*scaleFactor.y;
                mesh.vertices[vCounter + 5] = (float)(z + 1)*scaleFactor.z;

                mesh.vertices[vCounter + 6] = (float)(x + 1)*scaleFactor.x;
                mesh.vertices[vCounter + 7] = GRAY_VALUE(pixels[(x + 1) + z*mapX])*scaleFactor.y;
                mesh.vertices[vCounter + 8] = (float)z*scaleFactor.z;

                // another triangle - 3 vertex
                mesh.vertices[vCounter + 9] = mesh.vertices[vCounter + 6];
                mesh.vertices[vCounter + 10] = mesh.vertices[vCounter + 7];
                mesh.vertices[vCounter + 11] = mesh.vertices[vCounter + 8];

                mesh.vertices[vCounter + 12] = mesh.vertices[vCounter + 3];
                mesh.vertices[vCounter + 13] = mesh.vertices[vCounter + 4];
                mesh.vertices[vCounter + 14] = mesh.vertices[vCounter + 5];

                mesh.vertices[vCounter + 15] = (float)(x + 1)*scaleFactor.x;
                mesh.vertices[vCounter + 16] = GRAY_VALUE(pixels[(x + 1) + (z + 1)*mapX])*scaleFactor.y;
                mesh.vertices[vCounter + 17] = (float)(z + 1)*scaleFactor.z;
                vCounter += 18;     // 6 vertex, 18 floats

                // Fill texcoords array with data
                //--------------------------------------------------------------
                mesh.texcoords[tcCounter] = (float)x/(mapX - 1);
                mesh.texcoords[tcCounter + 1] = (float)z/(mapZ - 1);

                mesh.texcoords[tcCounter + 2] = (float)x/(mapX - 1);
                mesh.texcoords[tcCounter + 3] = (float)(z + 1)/(mapZ - 1);

                mesh.texcoords[tcCounter + 4] = (float)(x + 1)/(mapX - 1);
                mesh.texcoords[tcCounter + 5] = (float)z/(mapZ - 1);

                mesh.texcoords[tcCounter + 6] = mesh.texcoords[tcCounter + 4];
                mesh.texcoords[tcCounter + 7] = mesh.texcoords[tcCounter + 5];

                mesh.texcoords[tcCounter + 8] = mesh.texcoords[tcCounter + 2];
                mesh.texcoords[tcCounter + 9] = mesh.texcoords[tcCounter + 3];

                mesh.texcoords[tcCounter + 10] = (float)(x + 1)/(mapX - 1);
                mesh.texcoords[tcCounter + 11] = (float)(z + 1)/(mapZ - 1);
                tcCounter += 12;    // 6 texcoords, 12 floats

                // Fill normals array with data
                //--------------------------------------------------------------
                for (int i = 0; i < 18; i += 9) {
                    vA.x = mesh.vertices[nCounter + i];
                    vA.y = mesh.vertices[nCounter + i + 1];
                    vA.z = mesh.vertices[nCounter + i + 2];

                    vB.x = mesh.vertices[nCounter + i + 3];
                    vB.y = mesh.vertices[nCounter + i + 4];
                    vB.z = mesh.vertices[nCounter + i + 5];

                    vC.x = mesh.vertices[nCounter + i + 6];
                    vC.y = mesh.vertices[nCounter + i + 7];
                    vC.z = mesh.vertices[nCounter + i + 8];

                    vN = Vector3Normalize(Vector3CrossProduct(Vector3Subtract(vB, vA), Vector3Subtract(vC, vA)));

                    mesh.normals[nCounter + i] = vN.x;
                    mesh.normals[nCounter + i + 1] = vN.y;
                    mesh.normals[nCounter + i + 2] = vN.z;

                    mesh.normals[nCounter + i + 3] = vN.x;
                    mesh.normals[nCounter + i + 4] = vN.y;
                    mesh.normals[nCounter + i + 5] = vN.z;

                    mesh.normals[nCounter + i + 6] = vN.x;
                    mesh.normals[nCounter + i + 7] = vN.y;
                    mesh.normals[nCounter + i + 8] = vN.z;
                }

                nCounter += 18;     // 6 vertex, 18 floats
            }
        }

        context.textures.UnloadImageColors(pixels);  // Unload pixels color data

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }

    // Generate a cubes mesh from pixel data
    // NOTE: Vertex data is uploaded to GPU
    public Mesh GenMeshCubicmap(Image cubicmap, Vector3 cubeSize) {
        Mesh mesh = new Mesh();

        Color[] pixels = context.textures.LoadImageColors(cubicmap);

        int mapWidth = cubicmap.width;
        int mapHeight = cubicmap.height;

        // NOTE: Max possible number of triangles numCubes*(12 triangles by cube)
        int maxTriangles = cubicmap.width*cubicmap.height*12;

        int vCounter = 0;       // Used to count vertices
        int tcCounter = 0;      // Used to count texcoords
        int nCounter = 0;       // Used to count normals

        float w = cubeSize.x;
        float h = cubeSize.z;
        float h2 = cubeSize.y;

        Vector3[] mapVertices = new Vector3[maxTriangles*3];
        Vector2[] mapTexcoords = new Vector2[maxTriangles*3];
        Vector3[] mapNormals = new Vector3[maxTriangles*3];

        // Define the 6 normals of the cube, we will combine them accordingly later...
        Vector3 n1 = new Vector3(1.0f, 0.0f, 0.0f);
        Vector3 n2 = new Vector3(-1.0f, 0.0f, 0.0f);
        Vector3 n3 = new Vector3(0.0f, 1.0f, 0.0f);
        Vector3 n4 = new Vector3(0.0f, -1.0f, 0.0f);
        Vector3 n5 = new Vector3(0.0f, 0.0f, -1.0f);
        Vector3 n6 = new Vector3(0.0f, 0.0f, 1.0f);

        Rectangle rightTexUV = new Rectangle(0.0f, 0.0f, 0.5f, 0.5f);
        Rectangle leftTexUV = new Rectangle(0.5f, 0.0f, 0.5f, 0.5f);
        Rectangle frontTexUV = new Rectangle(0.0f, 0.0f, 0.5f, 0.5f);
        Rectangle backTexUV = new Rectangle(0.5f, 0.0f, 0.5f, 0.5f);
        Rectangle topTexUV = new Rectangle(0.0f, 0.5f, 0.5f, 0.5f);
        Rectangle bottomTexUV = new Rectangle(0.5f, 0.5f, 0.5f, 0.5f);

        for (int z = 0; z < mapHeight; ++z) {
            for (int x = 0; x < mapWidth; ++x) {
                // Define the 8 vertex of the cube, we will combine them accordingly later...
                Vector3 v1 = new Vector3(w*(x - 0.5f), h2, h*(z - 0.5f));
                Vector3 v2 = new Vector3(w*(x - 0.5f), h2, h*(z + 0.5f));
                Vector3 v3 = new Vector3(w*(x + 0.5f), h2, h*(z + 0.5f));
                Vector3 v4 = new Vector3(w*(x + 0.5f), h2, h*(z - 0.5f));
                Vector3 v5 = new Vector3(w*(x + 0.5f), 0, h*(z - 0.5f));
                Vector3 v6 = new Vector3(w*(x - 0.5f), 0, h*(z - 0.5f));
                Vector3 v7 = new Vector3(w*(x - 0.5f), 0, h*(z + 0.5f));
                Vector3 v8 = new Vector3(w*(x + 0.5f), 0, h*(z + 0.5f));

                // We check pixel color to be WHITE -> draw full cube
                if (COLOR_EQUAL(pixels[z*cubicmap.width + x], WHITE)) {
                    // Define triangles and checking collateral cubes
                    //------------------------------------------------

                    // Define top triangles (2 tris, 6 vertex --> v1-v2-v3, v1-v3-v4)
                    // WARNING: Not required for a WHITE cubes, created to allow seeing the map from outside
                    mapVertices[vCounter] = v1;
                    mapVertices[vCounter + 1] = v2;
                    mapVertices[vCounter + 2] = v3;
                    mapVertices[vCounter + 3] = v1;
                    mapVertices[vCounter + 4] = v3;
                    mapVertices[vCounter + 5] = v4;
                    vCounter += 6;

                    mapNormals[nCounter] = n3;
                    mapNormals[nCounter + 1] = n3;
                    mapNormals[nCounter + 2] = n3;
                    mapNormals[nCounter + 3] = n3;
                    mapNormals[nCounter + 4] = n3;
                    mapNormals[nCounter + 5] = n3;
                    nCounter += 6;

                    mapTexcoords[tcCounter] = new Vector2(topTexUV.x, topTexUV.y);
                    mapTexcoords[tcCounter + 1] = new Vector2(topTexUV.x, topTexUV.y + topTexUV.height);
                    mapTexcoords[tcCounter + 2] = new Vector2(topTexUV.x + topTexUV.width, topTexUV.y + topTexUV.height);
                    mapTexcoords[tcCounter + 3] = new Vector2(topTexUV.x, topTexUV.y);
                    mapTexcoords[tcCounter + 4] = new Vector2(topTexUV.x + topTexUV.width, topTexUV.y + topTexUV.height);
                    mapTexcoords[tcCounter + 5] = new Vector2(topTexUV.x + topTexUV.width, topTexUV.y);
                    tcCounter += 6;

                    // Define bottom triangles (2 tris, 6 vertex --> v6-v8-v7, v6-v5-v8)
                    mapVertices[vCounter] = v6;
                    mapVertices[vCounter + 1] = v8;
                    mapVertices[vCounter + 2] = v7;
                    mapVertices[vCounter + 3] = v6;
                    mapVertices[vCounter + 4] = v5;
                    mapVertices[vCounter + 5] = v8;
                    vCounter += 6;

                    mapNormals[nCounter] = n4;
                    mapNormals[nCounter + 1] = n4;
                    mapNormals[nCounter + 2] = n4;
                    mapNormals[nCounter + 3] = n4;
                    mapNormals[nCounter + 4] = n4;
                    mapNormals[nCounter + 5] = n4;
                    nCounter += 6;

                    mapTexcoords[tcCounter] = new Vector2(bottomTexUV.x + bottomTexUV.width, bottomTexUV.y);
                    mapTexcoords[tcCounter + 1] = new Vector2(bottomTexUV.x, bottomTexUV.y + bottomTexUV.height);
                    mapTexcoords[tcCounter + 2] = new Vector2(bottomTexUV.x + bottomTexUV.width, bottomTexUV.y + bottomTexUV.height);
                    mapTexcoords[tcCounter + 3] = new Vector2(bottomTexUV.x + bottomTexUV.width, bottomTexUV.y);
                    mapTexcoords[tcCounter + 4] = new Vector2(bottomTexUV.x, bottomTexUV.y);
                    mapTexcoords[tcCounter + 5] = new Vector2(bottomTexUV.x, bottomTexUV.y + bottomTexUV.height);
                    tcCounter += 6;

                    // Checking cube on bottom of current cube
                    if (((z < cubicmap.height - 1) && COLOR_EQUAL(pixels[(z + 1)*cubicmap.width + x], BLACK)) || (z == cubicmap.height - 1)) {
                        // Define front triangles (2 tris, 6 vertex) --> v2 v7 v3, v3 v7 v8
                        // NOTE: Collateral occluded faces are not generated
                        mapVertices[vCounter] = v2;
                        mapVertices[vCounter + 1] = v7;
                        mapVertices[vCounter + 2] = v3;
                        mapVertices[vCounter + 3] = v3;
                        mapVertices[vCounter + 4] = v7;
                        mapVertices[vCounter + 5] = v8;
                        vCounter += 6;

                        mapNormals[nCounter] = n6;
                        mapNormals[nCounter + 1] = n6;
                        mapNormals[nCounter + 2] = n6;
                        mapNormals[nCounter + 3] = n6;
                        mapNormals[nCounter + 4] = n6;
                        mapNormals[nCounter + 5] = n6;
                        nCounter += 6;

                        mapTexcoords[tcCounter] = new Vector2(frontTexUV.x, frontTexUV.y);
                        mapTexcoords[tcCounter + 1] = new Vector2(frontTexUV.x, frontTexUV.y + frontTexUV.height);
                        mapTexcoords[tcCounter + 2] = new Vector2(frontTexUV.x + frontTexUV.width, frontTexUV.y);
                        mapTexcoords[tcCounter + 3] = new Vector2(frontTexUV.x + frontTexUV.width, frontTexUV.y);
                        mapTexcoords[tcCounter + 4] = new Vector2(frontTexUV.x, frontTexUV.y + frontTexUV.height);
                        mapTexcoords[tcCounter + 5] = new Vector2(frontTexUV.x + frontTexUV.width, frontTexUV.y + frontTexUV.height);
                        tcCounter += 6;
                    }

                    // Checking cube on top of current cube
                    if (((z > 0) && COLOR_EQUAL(pixels[(z - 1)*cubicmap.width + x], BLACK)) || (z == 0)) {
                        // Define back triangles (2 tris, 6 vertex) --> v1 v5 v6, v1 v4 v5
                        // NOTE: Collateral occluded faces are not generated
                        mapVertices[vCounter] = v1;
                        mapVertices[vCounter + 1] = v5;
                        mapVertices[vCounter + 2] = v6;
                        mapVertices[vCounter + 3] = v1;
                        mapVertices[vCounter + 4] = v4;
                        mapVertices[vCounter + 5] = v5;
                        vCounter += 6;

                        mapNormals[nCounter] = n5;
                        mapNormals[nCounter + 1] = n5;
                        mapNormals[nCounter + 2] = n5;
                        mapNormals[nCounter + 3] = n5;
                        mapNormals[nCounter + 4] = n5;
                        mapNormals[nCounter + 5] = n5;
                        nCounter += 6;

                        mapTexcoords[tcCounter] = new Vector2(backTexUV.x + backTexUV.width, backTexUV.y);
                        mapTexcoords[tcCounter + 1] = new Vector2(backTexUV.x, backTexUV.y + backTexUV.height);
                        mapTexcoords[tcCounter + 2] = new Vector2(backTexUV.x + backTexUV.width, backTexUV.y + backTexUV.height);
                        mapTexcoords[tcCounter + 3] = new Vector2(backTexUV.x + backTexUV.width, backTexUV.y);
                        mapTexcoords[tcCounter + 4] = new Vector2(backTexUV.x, backTexUV.y);
                        mapTexcoords[tcCounter + 5] = new Vector2(backTexUV.x, backTexUV.y + backTexUV.height);
                        tcCounter += 6;
                    }

                    // Checking cube on right of current cube
                    if (((x < cubicmap.width - 1) && COLOR_EQUAL(pixels[z*cubicmap.width + (x + 1)], BLACK)) || (x == cubicmap.width - 1)) {
                        // Define right triangles (2 tris, 6 vertex) --> v3 v8 v4, v4 v8 v5
                        // NOTE: Collateral occluded faces are not generated
                        mapVertices[vCounter] = v3;
                        mapVertices[vCounter + 1] = v8;
                        mapVertices[vCounter + 2] = v4;
                        mapVertices[vCounter + 3] = v4;
                        mapVertices[vCounter + 4] = v8;
                        mapVertices[vCounter + 5] = v5;
                        vCounter += 6;

                        mapNormals[nCounter] = n1;
                        mapNormals[nCounter + 1] = n1;
                        mapNormals[nCounter + 2] = n1;
                        mapNormals[nCounter + 3] = n1;
                        mapNormals[nCounter + 4] = n1;
                        mapNormals[nCounter + 5] = n1;
                        nCounter += 6;

                        mapTexcoords[tcCounter] = new Vector2(rightTexUV.x, rightTexUV.y);
                        mapTexcoords[tcCounter + 1] = new Vector2(rightTexUV.x, rightTexUV.y + rightTexUV.height);
                        mapTexcoords[tcCounter + 2] = new Vector2(rightTexUV.x + rightTexUV.width, rightTexUV.y);
                        mapTexcoords[tcCounter + 3] = new Vector2(rightTexUV.x + rightTexUV.width, rightTexUV.y);
                        mapTexcoords[tcCounter + 4] = new Vector2(rightTexUV.x, rightTexUV.y + rightTexUV.height);
                        mapTexcoords[tcCounter + 5] = new Vector2(rightTexUV.x + rightTexUV.width, rightTexUV.y + rightTexUV.height);
                        tcCounter += 6;
                    }

                    // Checking cube on left of current cube
                    if (((x > 0) && COLOR_EQUAL(pixels[z*cubicmap.width + (x - 1)], BLACK)) || (x == 0)) {
                        // Define left triangles (2 tris, 6 vertex) --> v1 v7 v2, v1 v6 v7
                        // NOTE: Collateral occluded faces are not generated
                        mapVertices[vCounter] = v1;
                        mapVertices[vCounter + 1] = v7;
                        mapVertices[vCounter + 2] = v2;
                        mapVertices[vCounter + 3] = v1;
                        mapVertices[vCounter + 4] = v6;
                        mapVertices[vCounter + 5] = v7;
                        vCounter += 6;

                        mapNormals[nCounter] = n2;
                        mapNormals[nCounter + 1] = n2;
                        mapNormals[nCounter + 2] = n2;
                        mapNormals[nCounter + 3] = n2;
                        mapNormals[nCounter + 4] = n2;
                        mapNormals[nCounter + 5] = n2;
                        nCounter += 6;

                        mapTexcoords[tcCounter] = new Vector2(leftTexUV.x, leftTexUV.y);
                        mapTexcoords[tcCounter + 1] = new Vector2(leftTexUV.x + leftTexUV.width, leftTexUV.y + leftTexUV.height);
                        mapTexcoords[tcCounter + 2] = new Vector2(leftTexUV.x + leftTexUV.width, leftTexUV.y);
                        mapTexcoords[tcCounter + 3] = new Vector2(leftTexUV.x, leftTexUV.y);
                        mapTexcoords[tcCounter + 4] = new Vector2(leftTexUV.x, leftTexUV.y + leftTexUV.height);
                        mapTexcoords[tcCounter + 5] = new Vector2(leftTexUV.x + leftTexUV.width, leftTexUV.y + leftTexUV.height);
                        tcCounter += 6;
                    }
                }
                // We check pixel color to be BLACK, we will only draw floor and roof
                else if (COLOR_EQUAL(pixels[z*cubicmap.width + x], BLACK)) {
                    // Define top triangles (2 tris, 6 vertex --> v1-v2-v3, v1-v3-v4)
                    mapVertices[vCounter] = v1;
                    mapVertices[vCounter + 1] = v3;
                    mapVertices[vCounter + 2] = v2;
                    mapVertices[vCounter + 3] = v1;
                    mapVertices[vCounter + 4] = v4;
                    mapVertices[vCounter + 5] = v3;
                    vCounter += 6;

                    mapNormals[nCounter] = n4;
                    mapNormals[nCounter + 1] = n4;
                    mapNormals[nCounter + 2] = n4;
                    mapNormals[nCounter + 3] = n4;
                    mapNormals[nCounter + 4] = n4;
                    mapNormals[nCounter + 5] = n4;
                    nCounter += 6;

                    mapTexcoords[tcCounter] = new Vector2(topTexUV.x, topTexUV.y);
                    mapTexcoords[tcCounter + 2] = new Vector2(topTexUV.x, topTexUV.y + topTexUV.height);
                    mapTexcoords[tcCounter + 1] = new Vector2(topTexUV.x + topTexUV.width, topTexUV.y + topTexUV.height);
                    mapTexcoords[tcCounter + 3] = new Vector2(topTexUV.x, topTexUV.y);
                    mapTexcoords[tcCounter + 4] = new Vector2(topTexUV.x + topTexUV.width, topTexUV.y);
                    mapTexcoords[tcCounter + 5] = new Vector2(topTexUV.x + topTexUV.width, topTexUV.y + topTexUV.height);
                    tcCounter += 6;

                    // Define bottom triangles (2 tris, 6 vertex --> v6-v8-v7, v6-v5-v8)
                    mapVertices[vCounter] = v6;
                    mapVertices[vCounter + 1] = v7;
                    mapVertices[vCounter + 2] = v8;
                    mapVertices[vCounter + 3] = v6;
                    mapVertices[vCounter + 4] = v8;
                    mapVertices[vCounter + 5] = v5;
                    vCounter += 6;

                    mapNormals[nCounter] = n3;
                    mapNormals[nCounter + 1] = n3;
                    mapNormals[nCounter + 2] = n3;
                    mapNormals[nCounter + 3] = n3;
                    mapNormals[nCounter + 4] = n3;
                    mapNormals[nCounter + 5] = n3;
                    nCounter += 6;

                    mapTexcoords[tcCounter] = new Vector2(bottomTexUV.x + bottomTexUV.width, bottomTexUV.y);
                    mapTexcoords[tcCounter + 1] = new Vector2(bottomTexUV.x + bottomTexUV.width, bottomTexUV.y + bottomTexUV.height);
                    mapTexcoords[tcCounter + 2] = new Vector2(bottomTexUV.x, bottomTexUV.y + bottomTexUV.height);
                    mapTexcoords[tcCounter + 3] = new Vector2(bottomTexUV.x + bottomTexUV.width, bottomTexUV.y);
                    mapTexcoords[tcCounter + 4] = new Vector2(bottomTexUV.x, bottomTexUV.y + bottomTexUV.height);
                    mapTexcoords[tcCounter + 5] = new Vector2(bottomTexUV.x, bottomTexUV.y);
                    tcCounter += 6;
                }
            }
        }

        // Move data from mapVertices temp arrays to vertices float array
        mesh.vertexCount = vCounter;
        mesh.triangleCount = vCounter/3;

        mesh.vertices = new float[mesh.vertexCount*3];
        mesh.normals = new float[mesh.vertexCount*3];
        mesh.texcoords = new float[mesh.vertexCount*2];
        mesh.colors = null;

        int fCounter = 0;

        // Move vertices data
        for (int i = 0; i < vCounter; i++) {
            mesh.vertices[fCounter] = mapVertices[i].x;
            mesh.vertices[fCounter + 1] = mapVertices[i].y;
            mesh.vertices[fCounter + 2] = mapVertices[i].z;
            fCounter += 3;
        }

        fCounter = 0;

        // Move normals data
        for (int i = 0; i < nCounter; i++) {
            mesh.normals[fCounter] = mapNormals[i].x;
            mesh.normals[fCounter + 1] = mapNormals[i].y;
            mesh.normals[fCounter + 2] = mapNormals[i].z;
            fCounter += 3;
        }

        fCounter = 0;

        // Move texcoords data
        for (int i = 0; i < tcCounter; i++) {
            mesh.texcoords[fCounter] = mapTexcoords[i].x;
            mesh.texcoords[fCounter + 1] = mapTexcoords[i].y;
            fCounter += 2;
        }

        mapVertices = null;
        mapNormals = null;
        mapTexcoords = null;

        context.textures.UnloadImageColors(pixels);   // Unload pixels color data

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }

    // END MESH GENERATION

    // Compute mesh bounding box limits
    // NOTE: minVertex and maxVertex should be transformed by model transform matrix
    public BoundingBox GetMeshBoundingBox(Mesh mesh) {
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
        Matrix modTransform = model.transform;
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

        model.transform = modTransform;
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
        Vector2 sizeRatio = new Vector2(size.x*source.width/source.height, size.y );

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

        RLGL.rlCheckRenderBatchLimit(8);

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
    public boolean CheckCollisionSpheres(Vector3 center1, float radius1, Vector3 center2, float radius2) {

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
    public boolean CheckCollisionBoxes(BoundingBox box1, BoundingBox box2) {
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
    public boolean CheckCollisionBoxSphere(BoundingBox box, Vector3 center, float radius) {
        boolean collision = false;

        float dmin = 0;

        if (center.x < box.min.x) {
            dmin += Math.pow(center.x - box.min.x, 2);
        }
        else if (center.x > box.max.x) {
            dmin += Math.pow(center.x - box.max.x, 2);
        }

        if (center.y < box.min.y) {
            dmin += Math.pow(center.y - box.min.y, 2);
        }
        else if (center.y > box.max.y) {
            dmin += Math.pow(center.y - box.max.y, 2);
        }

        if (center.z < box.min.z) {
            dmin += Math.pow(center.z - box.min.z, 2);
        }
        else if (center.z > box.max.z) {
            dmin += Math.pow(center.z - box.max.z, 2);
        }

        if (dmin <= (radius*radius)) {
            collision = true;
        }

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

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------


    private Model LoadOBJ(String fileName) {

        Model model = new Model();

        try {
            String filetext = FileIO.LoadFileText(fileName);

            OBJLoader loader = new OBJLoader();
            boolean success = loader.ReadOBJ(filetext, true);

            if(success) {
                Tracelog(LOG_INFO, "MODEL: ["+fileName+"] OBJ data loaded successfully: "+ loader.objInfo.shapes.length+" meshes/"+loader.objInfo.totalMaterials+" materials");
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
                    model.materials[m].maps[MATERIAL_MAP_DIFFUSE].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].diffuse_texname);  //char *diffuse_texname; // map_Kd
                }

                model.materials[m].maps[MATERIAL_MAP_DIFFUSE].color = new Color((int) (loader.mtlInfo.materials[m].diffuse[0]*255.0f), (int) (loader.mtlInfo.materials[m].diffuse[1]*255.0f), (int) (loader.mtlInfo.materials[m].diffuse[2]*255.0f), 255); //float diffuse[3];
                model.materials[m].maps[MATERIAL_MAP_DIFFUSE].value = 0.0f;

                if (loader.mtlInfo.materials[m].specular_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_SPECULAR].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].specular_texname);  //char *specular_texname; // map_Ks
                }

                model.materials[m].maps[MATERIAL_MAP_SPECULAR].color = new Color((int) (loader.mtlInfo.materials[m].specular[0]*255.0f), (int) (loader.mtlInfo.materials[m].specular[1]*255.0f), (int) (loader.mtlInfo.materials[m].specular[2]*255.0f), 255); //float specular[3];
                model.materials[m].maps[MATERIAL_MAP_SPECULAR].value = 0.0f;

                if (loader.mtlInfo.materials[m].bump_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_NORMAL].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].bump_texname);  //char *bump_texname; // map_bump, bump
                }

                model.materials[m].maps[MATERIAL_MAP_NORMAL].color = WHITE;
                model.materials[m].maps[MATERIAL_MAP_NORMAL].value = loader.mtlInfo.materials[m].shininess;

                model.materials[m].maps[MATERIAL_MAP_EMISSION].color = new Color((int) (loader.mtlInfo.materials[m].emission[0]*255.0f), (int) (loader.mtlInfo.materials[m].emission[1]*255.0f), (int) (loader.mtlInfo.materials[m].emission[2]*255.0f), 255); //float emission[3];

                if (loader.mtlInfo.materials[m].displacement_texname != null) {
                    model.materials[m].maps[MATERIAL_MAP_HEIGHT].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].displacement_texname);  //char *displacement_texname; // disp
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return model;
    }

    // Convert array of four bytes to int
    private static int IQM_toInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getInt();
    }

    private static float IQM_toFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getFloat();
    }

    private static short IQM_toShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getShort();
    }

    public Model LoadIQM(String fileName) {
        Model model = new Model();

        try (ByteArrayInputStream fileData = new ByteArrayInputStream(FileIO.LoadFileData(fileName))){

            int dataSize = fileData.available();

            final String IQM_MAGIC = "INTERQUAKEMODEL\0";
            final int IQM_VERSION = 2;

            final int BONE_NAME_LENGTH = 32;
            final int MESH_NAME_LENGTH = 32;
            final int MATERIAL_NAME_LENGTH = 32;

            int fileDataPtr = 0;

            IQMMesh[] imesh;
            IQMTriangle[] tri;
            IQMVertexArray[] va;
            IQMJoint[] joint;
            IQMHeader header = new IQMHeader();

            float[] vertex;
            float[] normal;
            float[] text;
            byte[] blendi;
            byte[] blendw;
            byte[] color;
            byte[] intbuffer = new byte[Integer.BYTES];
            byte[] floatBuffer = new byte[Float.BYTES];
            byte[] tmpMagic = new byte[header.magic.length];

            // Read IQM Header
            fileDataPtr += fileData.read(tmpMagic);
            for (int i = 0; i < header.magic.length; i++) {
                header.magic[i] = (char) tmpMagic[i];
            }
            fileDataPtr += fileData.read(intbuffer);
            header.version = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.filesize = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.flags = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_text = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_text = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_meshes = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_meshes = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_vertexarrays = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_vertexes = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_vertexarrays = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_triangles = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_triangles = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_adjacency = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_joints = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_joints = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_poses = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_poses = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_anims = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_anims = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_frames = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_framechannels = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_frames = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_bounds = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_comment = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_comment = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.num_extensions = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            header.ofs_extensions = IQM_toInt(intbuffer);

            if (!IQM_MAGIC.equals(String.valueOf(header.magic))) {
                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] IQM file is not a valid model");
            }
            if (IQM_VERSION != header.version) {
                Tracelog(LOG_WARNING, "MODEL: ["+fileName+"] IQM file version not supported ("+header.version+")");
            }

            imesh = new IQMMesh[header.num_meshes];
            int off = 0;
            for (int i = 0; i < imesh.length; i++) {
                imesh[i] = new IQMMesh();
                fileData.reset();
                fileData.skip( header.ofs_meshes + off);
                fileData.read(intbuffer);
                imesh[i].name = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                imesh[i].material = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                imesh[i].first_vertex = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                imesh[i].num_vertexes = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                imesh[i].first_triangle = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                imesh[i].num_triangles = IQM_toInt(intbuffer);
                off += 24;
            }
            fileData.reset();
            fileData.skip(fileDataPtr);

            model.meshCount = header.num_meshes;
            model.meshes = new Mesh[model.meshCount];

            model.materialCount = model.meshCount;
            model.materials = new Material[model.materialCount];

            char[] name = new char[MESH_NAME_LENGTH];
            byte[] tmpName = new byte[MESH_NAME_LENGTH];
            char[] material = new char[MATERIAL_NAME_LENGTH];
            byte[] tmpMaterial = new byte[MATERIAL_NAME_LENGTH];

            for (int i = 0; i < model.meshCount; i++) {
                // Copy mesh name
                fileData.reset();
                fileData.skip(header.ofs_text + imesh[i].name);
                fileData.read(tmpName);
                for (int j = 0; j < MESH_NAME_LENGTH; j++) {
                    name[j] = (char) tmpName[j];
                }

                // Copy mesh material
                fileData.reset();
                fileData.skip( header.ofs_text + imesh[i].material);
                fileData.read(tmpMaterial);
                for (int j = 0; j < MESH_NAME_LENGTH; j++) {
                    material[j] = (char) tmpMaterial[j];
                }

                model.meshes[i] = new Mesh();
                model.materials[i] = LoadMaterialDefault();

                Tracelog(LOG_DEBUG, "MODEL: [" + fileName + "] mesh name (" + String.valueOf(name) + "), material (" + String.valueOf(material) + ")");

                model.meshes[i].vertexCount = imesh[i].num_vertexes;

                model.meshes[i].vertices = new float[model.meshes[i].vertexCount*3];
                model.meshes[i].normals = new float[model.meshes[i].vertexCount*3];
                model.meshes[i].texcoords = new float[model.meshes[i].vertexCount*2];

                model.meshes[i].boneIds = new byte[model.meshes[i].vertexCount*4];
                model.meshes[i].boneWeights = new float[model.meshes[i].vertexCount*4];

                model.meshes[i].triangleCount = imesh[i].num_triangles;
                model.meshes[i].indicesS = new short[model.meshes[i].triangleCount*3];

                // Animated verted data, what we actually process for rendering
                // NOTE: Animated vertex should be re-uploaded to GPU (if not using GPU skinning)
                model.meshes[i].animVertices = new float[model.meshes[i].vertexCount*3];
                model.meshes[i].animNormals = new float[model.meshes[i].vertexCount*3];
            }

            // Triangles data processing
            tri = new IQMTriangle[header.num_triangles];
            fileData.reset();
            fileData.skip(header.ofs_triangles);
            for (int i = 0; i < tri.length; i++) {
                tri[i] = new IQMTriangle();
                for (int j = 0; j < tri[i].vertex.length; j++) {
                    fileData.read(intbuffer);
                    tri[i].vertex[j] = IQM_toInt(intbuffer);
                }
            }

            for (int m = 0; m < model.meshCount; m++) {
                int tcounter = 0;

                for (int i = imesh[m].first_triangle; i < (imesh[m].first_triangle + imesh[m].num_triangles); i++) {
                    // IQM triangles indexes are stored in counter-clockwise, but raylib processes the index in linear order,
                    // expecting they point to the counter-clockwise vertex triangle, so we need to reverse triangle indexes
                    // NOTE: raylib renders vertex data in counter-clockwise order (standard convention) by default
                    model.meshes[m].indicesS[tcounter + 2] = (short) (tri[i].vertex[0] - imesh[m].first_vertex);
                    model.meshes[m].indicesS[tcounter + 1] = (short) (tri[i].vertex[1] - imesh[m].first_vertex);
                    model.meshes[m].indicesS[tcounter] = (short) (tri[i].vertex[2] - imesh[m].first_vertex);
                    tcounter += 3;
                }
            }

            // Vertex arrays data processing
            va = new IQMVertexArray[header.num_vertexarrays];
            fileData.reset();
            fileData.skip(header.ofs_vertexarrays);
            for (int i = 0; i < va.length; i++) {
                va[i] = new IQMVertexArray();
                fileData.read(intbuffer);
                va[i].type = IQMVertexDataType.values()[IQM_toInt(intbuffer)];
                fileData.read(intbuffer);
                va[i].flags = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                va[i].format = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                va[i].size = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                va[i].offset = IQM_toInt(intbuffer);
            }
            
            for (int i = 0; i < header.num_vertexarrays; i++) {
                switch (va[i].type) {
                    case IQM_POSITION: {
                        vertex = new float[header.num_vertexes*3];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        for (int j = 0; j < vertex.length; j++) {
                            fileData.read(floatBuffer);
                            vertex[j] = IQM_toFloat(floatBuffer);
                        }

                        for (int m = 0; m < header.num_meshes; m++) {
                            int vCounter = 0;
                            for (int j = imesh[m].first_vertex*3; j < (imesh[m].first_vertex + imesh[m].num_vertexes)*3; j++) {
                                model.meshes[m].vertices[vCounter] = vertex[j];
                                model.meshes[m].animVertices[vCounter] = vertex[j];
                                vCounter++;
                            }
                        }
                    } break;
                    case IQM_TEXCOORD: {
                        text = new float[header.num_vertexes*2];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        for (int j = 0; j < text.length; j++) {
                            fileData.read(floatBuffer);
                            text[j] = IQM_toFloat(floatBuffer);
                        }

                        for (int m = 0; m < header.num_meshes; m++) {
                            int vCounter = 0;

                            for (int j = imesh[m].first_vertex*2; j < (imesh[m].first_vertex + imesh[m].num_vertexes)*2; j++){
                                model.meshes[m].texcoords[vCounter] = text[j];
                                vCounter++;
                            }
                        }
                    } break;
                    case IQM_NORMAL: {
                        normal = new float[header.num_vertexes*3];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        for (int j = 0; j < normal.length; j++) {
                            fileData.read(floatBuffer);
                            normal[j] = IQM_toFloat(floatBuffer);
                        }

                        for (int m = 0; m < header.num_meshes; m++) {
                            int vCounter = 0;
                            for (int j = imesh[m].first_vertex*3; j < (imesh[m].first_vertex + imesh[m].num_vertexes)*3; j++){
                                model.meshes[m].normals[vCounter] = normal[j];
                                model.meshes[m].animNormals[vCounter] = normal[j];
                                vCounter++;
                            }
                        }
                    } break;
                    case IQM_BLENDINDEXES: {
                        blendi = new byte[header.num_vertexes*4];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        fileData.read(blendi);

                        for (int m = 0; m < header.num_meshes; m++) {
                            int boneCounter = 0;
                            for (int j = imesh[m].first_vertex*4; j < (imesh[m].first_vertex + imesh[m].num_vertexes)*4; j++) {
                                model.meshes[m].boneIds[boneCounter] = blendi[j];
                                boneCounter++;
                            }
                        }
                    } break;
                    case IQM_BLENDWEIGHTS: {
                        blendw = new byte[header.num_vertexes*4];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        fileData.read(blendw);

                        for (int m = 0; m < header.num_meshes; m++) {
                            int boneCounter = 0;
                            for (int j = imesh[m].first_vertex*4; j < (imesh[m].first_vertex + imesh[m].num_vertexes)*4; j++) {
                                model.meshes[m].boneWeights[boneCounter] = Byte.toUnsignedInt(blendw[j])/255.0f;
                                boneCounter++;
                            }
                        }
                    } break;
                    case IQM_COLOR: {
                        color = new byte[header.num_vertexes*4];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        fileData.read(color);

                        for (int m = 0; m < header.num_meshes; m++) {
                            model.meshes[m].colors = new byte[model.meshes[m].vertexCount*4];

                            int vCounter = 0;
                            for (int j = imesh[m].first_vertex*4; j < (imesh[m].first_vertex + imesh[m].num_vertexes)*4; j++) {
                                model.meshes[m].colors[vCounter] = color[j];
                                vCounter++;
                            }
                        }
                    } break;
                    case IQM_CUSTOM: {
                    } break;
                }
            }

            // Bones (joints) data processing
            joint = new IQMJoint[header.num_joints];
            fileData.reset();
            fileData.skip(header.ofs_joints);
            for (int i = 0; i < joint.length; i++) {
                joint[i] = new IQMJoint();
                fileData.read(intbuffer);
                joint[i].name = IQM_toInt(intbuffer);
                fileData.read(intbuffer);
                joint[i].parent = IQM_toInt(intbuffer);
                for (int j = 0; j < joint[i].translate.length; j++) {
                    fileData.read(floatBuffer);
                    joint[i].translate[j] = IQM_toFloat(floatBuffer);
                }
                for (int j = 0; j < joint[i].rotate.length; j++) {
                    fileData.read(floatBuffer);
                    joint[i].rotate[j] = IQM_toFloat(floatBuffer);
                }
                for (int j = 0; j < joint[i].scale.length; j++) {
                    fileData.read(floatBuffer);
                    joint[i].scale[j] = IQM_toFloat(floatBuffer);
                }
            }

            model.boneCount = header.num_joints;
            model.bones = new BoneInfo[header.num_joints];
            model.bindPose = new Transform[header.num_joints];

            for (int i = 0; i < header.num_joints; i++) {
                // Bones
                model.bones[i] = new BoneInfo();
                model.bones[i].parent = joint[i].parent;
                byte[] tmp = new byte[BONE_NAME_LENGTH];
                char[] bonetmp = new char[BONE_NAME_LENGTH];
                fileData.reset();
                fileData.skip(header.ofs_text + joint[i].name);
                fileData.read(tmp);
                for (int j = 0; j < BONE_NAME_LENGTH; j++) {
                    bonetmp[j] = (char) tmp[j];
                }
                model.bones[i].name = bonetmp.toString();

                // Bind pose (base pose)
                model.bindPose[i] = new Transform();
                model.bindPose[i].translation.x = joint[i].translate[0];
                model.bindPose[i].translation.y = joint[i].translate[1];
                model.bindPose[i].translation.z = joint[i].translate[2];

                model.bindPose[i].rotation.x = joint[i].rotate[0];
                model.bindPose[i].rotation.y = joint[i].rotate[1];
                model.bindPose[i].rotation.z = joint[i].rotate[2];
                model.bindPose[i].rotation.w = joint[i].rotate[3];

                model.bindPose[i].scale.x = joint[i].scale[0];
                model.bindPose[i].scale.y = joint[i].scale[1];
                model.bindPose[i].scale.z = joint[i].scale[2];
            }

            // Build bind pose from parent joints
            for (int i = 0; i < model.boneCount; i++) {
                if (model.bones[i].parent >= 0) {
                    model.bindPose[i].rotation = QuaternionMultiply(model.bindPose[model.bones[i].parent].rotation, model.bindPose[i].rotation);
                    model.bindPose[i].translation = Vector3RotateByQuaternion(model.bindPose[i].translation, model.bindPose[model.bones[i].parent].rotation);
                    model.bindPose[i].translation = Vector3Add(model.bindPose[i].translation, model.bindPose[model.bones[i].parent].translation);
                    model.bindPose[i].scale = Vector3Multiply(model.bindPose[i].scale, model.bindPose[model.bones[i].parent].scale);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    public ModelAnimation[] LoadModelAnimationsIQM(String fileName) {
        final String IQM_MAGIC = "INTERQUAKEMODEL\0"; //IQM file magic number
        final int IQM_VERSION = 2;                    //only IQM version 2 supported

        int fileSize = 0;
        int fileDataPtr = 0;
        int animCount;

        ModelAnimation[] animations;

        try (ByteArrayInputStream fileData = new ByteArrayInputStream(FileIO.LoadFileData(fileName))) {
            fileSize = fileData.available();

            IQMHeader iqmHeader = new IQMHeader();
            
            byte[] intbuffer = new byte[Integer.BYTES];
            byte[] floatBuffer = new byte[Float.BYTES];
            byte[] shortbuffer = new byte[Short.BYTES];
            byte[] tmpMagic = new byte[iqmHeader.magic.length];

            // Read IQM Header
            fileDataPtr += fileData.read(tmpMagic);
            for (int i = 0; i < iqmHeader.magic.length; i++) {
                iqmHeader.magic[i] = (char) tmpMagic[i];
            }
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.version = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.filesize = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.flags = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_text = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_text = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_meshes = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_meshes = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_vertexarrays = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_vertexes = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_vertexarrays = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_triangles = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_triangles = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_adjacency = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_joints = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_joints = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_poses = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_poses = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_anims = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_anims = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_frames = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_framechannels = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_frames = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_bounds = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_comment = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_comment = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.num_extensions = IQM_toInt(intbuffer);
            fileDataPtr += fileData.read(intbuffer);
            iqmHeader.ofs_extensions = IQM_toInt(intbuffer);

            if (!IQM_MAGIC.equals(String.valueOf(iqmHeader.magic))) {
                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] IQM file is not a valid model");
            }
            if (IQM_VERSION != iqmHeader.version) {
                Tracelog(LOG_WARNING, "MODEL: ["+fileName+"] IQM file version not supported ("+iqmHeader.version+")");
            }

            // Get bones data
            IQMPose[] poses = new IQMPose[iqmHeader.num_poses];
            fileData.reset();
            fileData.skip(iqmHeader.ofs_poses);
            fileDataPtr = iqmHeader.ofs_poses;
            for (int i = 0; i < poses.length; i++) {
                poses[i] = new IQMPose();
                fileDataPtr += fileData.read(intbuffer);
                poses[i].parent = IQM_toInt(intbuffer);
                fileDataPtr += fileData.read(intbuffer);
                poses[i].mask = IQM_toInt(intbuffer);
                for (int j = 0; j < poses[i].channeloffset.length; j++) {
                    fileDataPtr += fileData.read(floatBuffer);
                    poses[i].channeloffset[j] = IQM_toFloat(floatBuffer);
                }
                for (int j = 0; j < poses[i].channelscale.length; j++) {
                    fileDataPtr += fileData.read(floatBuffer);
                    poses[i].channelscale[j] = IQM_toFloat(floatBuffer);
                }

            }


            // Get animations data
            animCount = iqmHeader.num_anims;
            IQMAnim[] anim = new IQMAnim[animCount];
            fileData.reset();
            fileData.skip(iqmHeader.ofs_anims);
            fileDataPtr = iqmHeader.ofs_anims;
            for (int i = 0; i < anim.length; i++) {
                anim[i] = new IQMAnim();
                fileDataPtr += fileData.read(intbuffer);
                anim[i].name = IQM_toInt(intbuffer);
                fileDataPtr += fileData.read(intbuffer);
                anim[i].first_frame = IQM_toInt(intbuffer);
                fileDataPtr += fileData.read(intbuffer);
                anim[i].num_frames = IQM_toInt(intbuffer);
                fileDataPtr += fileData.read(floatBuffer);
                anim[i].framerate = IQM_toFloat(floatBuffer);
                fileDataPtr += fileData.read(intbuffer);
                anim[i].flags = IQM_toInt(intbuffer);
            }

            animations = new ModelAnimation[iqmHeader.num_anims];

            // frameposes
            int[] framedata = new int[iqmHeader.num_frames* iqmHeader.num_framechannels];
            fileData.reset();
            fileData.skip(iqmHeader.ofs_frames);
            fileDataPtr = iqmHeader.ofs_frames;

            for (int i = 0; i < framedata.length; i++) {
                fileDataPtr += fileData.read(shortbuffer);
                framedata[i] = Short.toUnsignedInt(IQM_toShort(shortbuffer));
            }

            for (int a = 0; a < iqmHeader.num_anims; a++) {
                animations[a] = new ModelAnimation();
                animations[a].frameCount = anim[a].num_frames;
                animations[a].boneCount = iqmHeader.num_poses;
                animations[a].bones = new BoneInfo[iqmHeader.num_poses];
                animations[a].framePoses = new Transform[anim[a].num_frames][];
                // animations[a].framerate = anim.framerate;     // TODO: Use framerate?

                for (int j = 0; j < iqmHeader.num_poses; j++) {
                    animations[a].bones[j] = new BoneInfo();
                    animations[a].bones[j].name = "ANIMJOINTNAME";
                    animations[a].bones[j].parent = poses[j].parent;
                }

                for (int j = 0; j < anim[a].num_frames; j++) {
                    animations[a].framePoses[j] = new Transform[iqmHeader.num_poses];
                    for (int i = 0; i < iqmHeader.num_poses; i++) {
                        animations[a].framePoses[j][i] = new Transform();
                    }
                }

                int dcounter = anim[a].first_frame*iqmHeader.num_framechannels;

                for (int frame = 0; frame < anim[a].num_frames; frame++){
                    for (int i = 0; i < iqmHeader.num_poses; i++){
                        animations[a].framePoses[frame][i].translation.x = poses[i].channeloffset[0];

                        if ((poses[i].mask & 0x01) == 1) {
                            animations[a].framePoses[frame][i].translation.x += framedata[dcounter]*poses[i].channelscale[0];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].translation.y = poses[i].channeloffset[1];

                        if ((poses[i].mask & 0x02) == 2) {
                            animations[a].framePoses[frame][i].translation.y += framedata[dcounter]*poses[i].channelscale[1];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].translation.z = poses[i].channeloffset[2];

                        if ((poses[i].mask & 0x04) == 4) {
                            animations[a].framePoses[frame][i].translation.z += framedata[dcounter]*poses[i].channelscale[2];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].rotation.x = poses[i].channeloffset[3];

                        if ((poses[i].mask & 0x08) == 8) {
                            animations[a].framePoses[frame][i].rotation.x += framedata[dcounter]*poses[i].channelscale[3];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].rotation.y = poses[i].channeloffset[4];

                        if ((poses[i].mask & 0x10) == 16) {
                            animations[a].framePoses[frame][i].rotation.y += framedata[dcounter]*poses[i].channelscale[4];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].rotation.z = poses[i].channeloffset[5];

                        if ((poses[i].mask & 0x20) == 32) {
                            animations[a].framePoses[frame][i].rotation.z += framedata[dcounter]*poses[i].channelscale[5];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].rotation.w = poses[i].channeloffset[6];

                        if ((poses[i].mask & 0x40) == 64) {
                            animations[a].framePoses[frame][i].rotation.w += framedata[dcounter]*poses[i].channelscale[6];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].scale.x = poses[i].channeloffset[7];

                        if ((poses[i].mask & 0x80) == 128) {
                            animations[a].framePoses[frame][i].scale.x += framedata[dcounter]*poses[i].channelscale[7];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].scale.y = poses[i].channeloffset[8];

                        if ((poses[i].mask & 0x100) == 256) {
                            animations[a].framePoses[frame][i].scale.y += framedata[dcounter]*poses[i].channelscale[8];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].scale.z = poses[i].channeloffset[9];

                        if ((poses[i].mask & 0x200) == 512) {
                            animations[a].framePoses[frame][i].scale.z += framedata[dcounter]*poses[i].channelscale[9];
                            dcounter++;
                        }

                        animations[a].framePoses[frame][i].rotation = QuaternionNormalize(animations[a].framePoses[frame][i].rotation);
                    }
                }

                // Build frameposes
                for (int frame = 0; frame < anim[a].num_frames; frame++) {
                    for (int i = 0; i < animations[a].boneCount; i++) {
                        if (animations[a].bones[i].parent >= 0) {
                            animations[a].framePoses[frame][i].rotation = QuaternionMultiply(animations[a].framePoses[frame][animations[a].bones[i].parent].rotation, animations[a].framePoses[frame][i].rotation);
                            animations[a].framePoses[frame][i].translation = Vector3RotateByQuaternion(animations[a].framePoses[frame][i].translation, animations[a].framePoses[frame][animations[a].bones[i].parent].rotation);
                            animations[a].framePoses[frame][i].translation = Vector3Add(animations[a].framePoses[frame][i].translation, animations[a].framePoses[frame][animations[a].bones[i].parent].translation);
                            animations[a].framePoses[frame][i].scale = Vector3Multiply(animations[a].framePoses[frame][i].scale, animations[a].framePoses[frame][animations[a].bones[i].parent].scale);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return animations;
    }

    private Image LoadImageFromCgltfImage(gltfj_glTF gltf, int img, int bv, String texPath) {
        Image image = new Image();
        gltfj_Image gltfjImage = gltf.images.get(img);
        gltfj_BufferView bufferView = gltf.bufferViews.get(bv);
        gltfj_Buffer imgBuffer = gltf.buffers.get(bufferView.buffer);

        if (gltfjImage.uri != null) {
            if (gltfjImage.uri.length() > 5 && gltfjImage.uri.startsWith("data:")) {
                int i = 0;
                while ((gltfjImage.uri.charAt(i) != ',') && (i != gltfjImage.uri.length())) {
                    i++;
                }

                if (i == gltfjImage.uri.length()) {
                    Tracelog(LOG_WARNING, "IMAGE: glTF data URI is not a valid image");
                }
                else {
                    int b64Size = gltfjImage.uri.length() + i + 1;
                    int outSize = 3*(b64Size/4);

                    String uri = gltfjImage.uri;
                    String encoded = uri.substring(uri.lastIndexOf(",") + 1);
                    byte[] data = Base64.getDecoder().decode(encoded);

                    if (data != null) {
                        image = context.textures.LoadImageFromMemory(".png", data, outSize);
                    }

                }
            }
            else {
                image = context.textures.LoadImage(texPath + "/" + gltfjImage.uri);
            }
        }
        else if (imgBuffer.data != null) {
            byte[] data = new byte[bufferView.size];
            int offset = bufferView.offset;
            int stride = (bufferView.stride > 0) ? bufferView.stride : 1;

            for (int i = 0; i < bufferView.size; i++) {
                data[i] = imgBuffer.data[offset];
                offset += stride;
            }

            if (gltfjImage.mimeType.equals("image\\/png") || gltfjImage.mimeType.equals("image/png")) {
                image = context.textures.LoadImageFromMemory(".png", data, bufferView.size);
            }
            else if (gltfjImage.mimeType.equals("image\\/jpeg") || gltfjImage.mimeType.equals("image/jpeg")) {
                image = context.textures.LoadImageFromMemory(".jpg", data, bufferView.size);
            }
            else {
                Tracelog(LOG_WARNING, "MODEL: glTF image data MIME type not recognized (" + texPath + "/" + gltfjImage.mimeType + ")");
            }
        }

        return image;
    }

    // Load glTF file into model struct, .gltf and .glb supported
    private Model LoadGLTF(String fileName) {

        /*********************************************************************************************

         Function written by Wilhem Barbier(@wbrbr), with modifications by Tyler Bezera(@gamerfiend)
         Reviewed by Ramon Santamaria (@raysan5)

         FEATURES:
         - Supports .gltf and .glb files
         - Supports embedded (base64) or external textures
         - Supports PBR metallic/roughness flow, loads material textures, values and colors
         PBR specular/glossiness flow and extended texture flows not supported
         - Supports multiple meshes per model (every primitive is loaded as a separate mesh)

         RESTRICTIONS:
         - Only triangle meshes supported
         - Vertex attibute types and formats supported:
         > Vertices (position): vec3: float
         > Normals: vec3: float
         > Texcoords: vec2: float
         > Colors: vec4: u8, u16, f32 (normalized)
         > Indices: u16, u32 (truncated to u16)
         - Node hierarchies or transforms not supported

         ***********************************************************************************************/

        Model model = new Model();
        int dataSize;

        gltfj_glTF gltf = gltfj.Read(fileName);

        if (gltf.result == gltfj_glTF.ResultType.SUCCESS) {
            if (gltf.fileType == gltfj_glTF.FileType.GLB) {
                Tracelog(LOG_INFO, "MODEL: [" + fileName + "] Model basic data (glb) loaded successfully");
            }
            else if (gltf.fileType == gltfj_glTF.FileType.GLTF) {
                Tracelog(LOG_INFO, "MODEL: [" + fileName + "] Model basic data (glTF) loaded successfully");
            }
            else {
                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Model format not recognized");
                return model;
            }

            Tracelog(LOG_INFO, "    > Meshes count: " + gltf.meshCount);
            Tracelog(LOG_INFO, "    > Materials count: " + gltf.materialCount + " (+1 default)");
            Tracelog(LOG_DEBUG, "    > Buffers count: " + gltf.bufferCount);
            Tracelog(LOG_DEBUG, "    > Images count: " + gltf.imageCount);
            Tracelog(LOG_DEBUG, "    > Textures count: " + gltf.textureCount);

            int primitivesCount = 0;
            // NOTE: We will load every primitive in the glTF as a separate raylib mesh
            for (int i = 0; i < gltf.meshCount; i++) {
                primitivesCount += gltf.meshes.get(i).primitivesCount;
            }

            // Load our model data: meshes and materials
            model.meshCount = primitivesCount;
            model.meshes = new Mesh[model.meshCount];
            for (int i = 0; i < model.meshCount; i++) {
                model.meshes[i] = new Mesh();
            }

            // NOTE: We keep an extra slot for default material, in case some mesh requires it
            model.materialCount = gltf.materialCount + 1;
            model.materials = new Material[model.materialCount];
            model.materials[0] = LoadMaterialDefault();

            // Load mesh-material indices, by default all meshes are mapped to material index: 0
            model.meshMaterial = new int[model.meshCount];

            // Load materials data
            //----------------------------------------------------------------------------------------------------
            for (int i = 0, j = 1; i < gltf.materialCount; i++, j++) {
                model.materials[j] = LoadMaterialDefault();
                String texPath = rCore.GetDirectoryPath(fileName);

                // Check glTF material flow: PBR metallic/roughness flow
                // NOTE: Alternatively, materials can follow PBR specular/glossiness flow
                if (gltf.materials.get(i).hasMetallicRoughness) {

                    // Load base color texture (albedo)
                    if (gltf.materials.get(i).metallicRoughness.baseColorTexture.texture >= 0) {
                        Image imAlbedo = LoadImageFromCgltfImage(gltf, gltf.textures.get(gltf.materials.get(i).metallicRoughness.baseColorTexture.texture).image,
                                gltf.images.get(gltf.materials.get(i).metallicRoughness.baseColorTexture.texture).bufferView,  texPath);
                        if (imAlbedo.getData() != null) {
                            model.materials[j].maps[MATERIAL_MAP_ALBEDO].texture = context.textures.LoadTextureFromImage(imAlbedo);
                        }

                        //Load base colour factor (tint)
                        Color c = new Color();
                        c.r = (int) (gltf.materials.get(i).metallicRoughness.baseColorFactor[0]*255);
                        c.g = (int) (gltf.materials.get(i).metallicRoughness.baseColorFactor[1]*255);
                        c.b = (int) (gltf.materials.get(i).metallicRoughness.baseColorFactor[2]*255);
                        c.a = (int) (gltf.materials.get(i).metallicRoughness.baseColorFactor[3]*255);
                        model.materials[j].maps[MATERIAL_MAP_ALBEDO].color = c;
                    }

                    //Load metallic/roughness texture
                    if (gltf.materials.get(i).metallicRoughness.metallicRoughnessTexture.texture > 0) {
                        Image imMetallicRoughness = LoadImageFromCgltfImage(gltf, gltf.textures.get(gltf.materials.get(i).metallicRoughness.metallicRoughnessTexture.texture).image,
                                gltf.images.get(gltf.materials.get(i).metallicRoughness.metallicRoughnessTexture.texture).bufferView,  texPath);
                        if (imMetallicRoughness.getData() != null) {
                            model.materials[j].maps[MATERIAL_MAP_ROUGHNESS].texture = context.textures.LoadTextureFromImage(imMetallicRoughness);
                        }

                        // Load metallic/roughness material properties
                        float roughness = (float) gltf.materials.get(i).metallicRoughness.roughnessFactor;
                        model.materials[j].maps[MATERIAL_MAP_ROUGHNESS].value = roughness;

                        float metallic = (float) gltf.materials.get(i).metallicRoughness.metallicFactor;
                        model.materials[j].maps[MATERIAL_MAP_METALNESS].value = metallic;
                    }

                    //Load normal texture
                    if (gltf.materials.get(i).normalTexture.texture > 0) {
                        Image imNormal = LoadImageFromCgltfImage(gltf, gltf.textures.get(gltf.materials.get(i).normalTexture.texture).image,
                                gltf.images.get(gltf.materials.get(i).normalTexture.texture).bufferView,  texPath);
                        if (imNormal.getData() != null) {
                            model.materials[j].maps[MATERIAL_MAP_NORMAL].texture = context.textures.LoadTextureFromImage(imNormal);
                        }
                    }

                    //Load ambient occlusion texture
                    if (gltf.materials.get(i).occlusionTexture.texture > 0) {
                        Image imOcclusion = LoadImageFromCgltfImage(gltf, gltf.textures.get(gltf.materials.get(i).occlusionTexture.texture).image,
                                gltf.images.get(gltf.materials.get(i).occlusionTexture.texture).bufferView,  texPath);
                        if (imOcclusion.getData() != null) {
                            model.materials[j].maps[MATERIAL_MAP_OCCLUSION].texture = context.textures.LoadTextureFromImage(imOcclusion);
                        }
                    }

                    if (gltf.materials.get(i).emissiveTexture.texture > 0) {
                        Image imEmissive = LoadImageFromCgltfImage(gltf, gltf.textures.get(gltf.materials.get(i).emissiveTexture.texture).image,
                                gltf.images.get(gltf.materials.get(i).emissiveTexture.texture).bufferView,  texPath);
                        if (imEmissive.getData() != null) {
                            model.materials[j].maps[MATERIAL_MAP_EMISSION].texture = context.textures.LoadTextureFromImage(imEmissive);
                        }

                        //Load base colour factor (tint)
                        model.materials[j].maps[MATERIAL_MAP_EMISSION].color.r = (int) (gltf.materials.get(i).emissiveFactor[0]*255);
                        model.materials[j].maps[MATERIAL_MAP_EMISSION].color.g = (int) (gltf.materials.get(i).emissiveFactor[1]*255);
                        model.materials[j].maps[MATERIAL_MAP_EMISSION].color.b = (int) (gltf.materials.get(i).emissiveFactor[2]*255);
                        model.materials[j].maps[MATERIAL_MAP_EMISSION].color.a = (int) (gltf.materials.get(i).emissiveFactor[3]*255);
                    }
                }

                // Other possible materials not supported by raylib pipeline:
                // has_clearcoat, has_transmission, has_volume, has_ior, has specular, has_sheen
            }

            // Load meshes data
            //----------------------------------------------------------------------------------------------------
            for (int i = 0, meshIndex = 0; i < gltf.meshCount; i++) {
                // NOTE: meshIndex accumulates primitives

                for (int p = 0; p < gltf.meshes.get(i).primitivesCount; p++) {
                    // NOTE: We only support primitives defined by triangles
                    // Other alternatives: points, lines, line_strip, triangle_strip
                    if (gltf.meshes.get(i).primitives.get(p).type != gltfj_Primitive.PrimitiveType.TRIANGLES) {
                        continue;
                    }

                    // NOTE: Attributes data could be provided in several data formats (8, 8u, 16u, 32...),
                    // Only some formats for each attribute type are supported, read info at the top of this function!

                    for (int j = 0; j < gltf.meshes.get(i).primitives.get(p).attributesCount; j++) {
                        // Check the different attributes for every primitive
                        //int attribDataPtr = gltf.meshes.get(i).primitives.get(p).attributes.get(j).index;
                        // POSITION
                        if (gltf.meshes.get(i).primitives.get(p).attributes.get(j).type == gltfj_Attribute.AttributeType.POSITION) {
                            gltfj_Accessor attribute = gltf.accessors.get(gltf.meshes.get(i).primitives.get(p).attributes.get(j).index);

                            // WARNING: SPECS: POSITION accessor MUST have its min and max properties defined.

                            if ((attribute.componentType == gltfj_Accessor.AccessorDataType.FLOAT) && (attribute.type == gltfj_Accessor.AccessorType.VEC3)) {
                                // Init raylib mesh vertices to copy glTF attribute data
                                model.meshes[meshIndex].vertexCount = attribute.count;
                                model.meshes[meshIndex].vertices = new float[attribute.count*3];

                                // Load 3 components of float data type into mesh.vertices
                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                                byte[] fBuffer = new byte[Float.BYTES];

                                for (int v = 0, vo = 0; v < model.meshes[meshIndex].vertices.length; v++, vo+=Float.BYTES) {
                                    for (int f = 0; f < fBuffer.length; f++) {
                                        fBuffer[f] = buffer.data[bufferView.offset + attribute.byteOffset + vo + f];
                                    }
                                    model.meshes[meshIndex].vertices[v] = glFT_ByteArrayToFloat(fBuffer);
                                }
                            }
                            else {
                                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Vertices attribute data format not supported, use vec3 float");
                            }
                        }
                        // NORMAL
                        else if (gltf.meshes.get(i).primitives.get(p).attributes.get(j).type == gltfj_Attribute.AttributeType.NORMAL) {
                            gltfj_Accessor attribute = gltf.accessors.get(gltf.meshes.get(i).primitives.get(p).attributes.get(j).index);

                            if ((attribute.componentType == gltfj_Accessor.AccessorDataType.FLOAT) && (attribute.type == gltfj_Accessor.AccessorType.VEC3)) {
                                // Init raylib mesh normals to copy glTF attribute data
                                model.meshes[meshIndex].normals = new float[attribute.count*3];

                                // Load 3 components of float data type into mesh.normals
                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                                byte[] fBuffer = new byte[Float.BYTES];

                                for (int n = 0, no = 0; n < model.meshes[meshIndex].normals.length; n++, no+=Float.BYTES) {
                                    for (int f = 0; f < fBuffer.length; f++) {
                                        fBuffer[f] = buffer.data[bufferView.offset + attribute.byteOffset + no + f];
                                    }
                                    model.meshes[meshIndex].normals[n] = glFT_ByteArrayToFloat(fBuffer);
                                }
                            }
                            else {
                                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Normal attribute data format not supported, use vec3 float");
                            }
                        }
                        // TANGENT
                        else if (gltf.meshes.get(i).primitives.get(p).attributes.get(j).type == gltfj_Attribute.AttributeType.TANGENT) {
                            gltfj_Accessor attribute = gltf.accessors.get(gltf.meshes.get(i).primitives.get(p).attributes.get(j).index);

                            if ((attribute.componentType == gltfj_Accessor.AccessorDataType.FLOAT) && (attribute.type == gltfj_Accessor.AccessorType.VEC4)) {
                                // Init raylib mesh tangent to copy glTF attribute data
                                model.meshes[meshIndex].tangents = new float[attribute.count*4];

                                // Load 4 components of float data type into mesh.tangents
                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                                byte[] fBuffer = new byte[Float.BYTES];

                                for (int t = 0, to = 0; t < model.meshes[meshIndex].tangents.length; t++, to += Float.BYTES) {
                                    for (int f = 0; f < fBuffer.length; f++) {
                                        fBuffer[f] = buffer.data[bufferView.offset + attribute.byteOffset + to + f];
                                    }
                                    model.meshes[meshIndex].tangents[t] = glFT_ByteArrayToFloat(fBuffer);
                                }
                            }
                            else {
                                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Tangent attribute data format not supported, use vec4 float");
                            }
                        }
                        // TEXCOORD_0
                        else if (gltf.meshes.get(i).primitives.get(p).attributes.get(j).type == gltfj_Attribute.AttributeType.TEXCOORD) {
                            // TODO: Support additional texture coordinates: TEXCOORD_1 -> mesh.texcoords2

                            gltfj_Accessor attribute = gltf.accessors.get(gltf.meshes.get(i).primitives.get(p).attributes.get(j).index);

                            if ((attribute.componentType == gltfj_Accessor.AccessorDataType.FLOAT) && (attribute.type == gltfj_Accessor.AccessorType.VEC2)) {
                                // Init raylib mesh texcoords to copy glTF attribute data
                                model.meshes[meshIndex].texcoords = new float[attribute.count*2];

                                // Load 2 components of float data type into mesh.texcoords
                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                                byte[] fBuffer = new byte[Float.BYTES];

                                for (int t = 0, to = 0; t < model.meshes[meshIndex].texcoords.length; t++, to += Float.BYTES) {
                                    for (int f = 0; f < fBuffer.length; f++) {
                                        fBuffer[f] = buffer.data[bufferView.offset + attribute.byteOffset + to + f];
                                    }
                                    model.meshes[meshIndex].texcoords[t] = glFT_ByteArrayToFloat(fBuffer);
                                }
                            }
                            else {
                                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Texcoords attribute data format not supported, use vec2 float");
                            }
                        }
                        // COLOR_0
                        else if (gltf.meshes.get(i).primitives.get(p).attributes.get(j).type == gltfj_Attribute.AttributeType.COLOR) {
                            gltfj_Accessor attribute = gltf.accessors.get(gltf.meshes.get(i).primitives.get(p).attributes.get(j).index);

                            // WARNING: SPECS: All components of each COLOR_n accessor element MUST be clamped to [0.0, 1.0] range.

                            if ((attribute.componentType == gltfj_Accessor.AccessorDataType.UNSIGNED_BYTE) && (attribute.type == gltfj_Accessor.AccessorType.VEC4)) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.count*4];

                                // Load 4 components of unsigned char data type into mesh.colors
                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);

                                for (int c = 0; c < model.meshes[meshIndex].vertices.length; c++) {
                                    model.meshes[meshIndex].colors[c] = buffer.data[bufferView.offset + attribute.byteOffset + c];
                                }

                            }
                            else if ((attribute.componentType == gltfj_Accessor.AccessorDataType.UNSIGNED_SHORT) && (attribute.type == gltfj_Accessor.AccessorType.VEC4)) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.count*4];

                                // Load data into a temp buffer to be converted to raylib data type
                                short[] temp = new short[attribute.count*4];

                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                                byte[] sBuffer = new byte[Short.BYTES];

                                for (int c = 0, co = 0; c < temp.length; c++, co += Short.BYTES) {
                                    for (int f = 0; f < sBuffer.length; f++) {
                                        sBuffer[f] = buffer.data[bufferView.offset + attribute.byteOffset + co + f];
                                    }
                                    temp[c] = glFT_ByteArrayToShort(sBuffer);
                                }

                                // Convert data to raylib color data type (4 bytes)
                                for (int c = 0; c < attribute.count * 4; c++) {
                                    model.meshes[meshIndex].colors[c] = (byte)(temp[c]);

                                }

                            }
                            else if ((attribute.componentType == gltfj_Accessor.AccessorDataType.FLOAT) && (attribute.type == gltfj_Accessor.AccessorType.VEC4)) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.count * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                float[] temp = new float[attribute.count*4];

                                gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                                gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                                byte[] fBuffer = new byte[Float.BYTES];

                                for (int c = 0, co = 0; c < temp.length; c++, co+=Float.BYTES) {
                                    for (int f = 0; f < fBuffer.length; f++) {
                                        fBuffer[f] = buffer.data[bufferView.offset + attribute.byteOffset + co + f];
                                    }
                                    temp[c] = glFT_ByteArrayToFloat(fBuffer);
                                }

                                // Convert data to raylib color data type (4 bytes), we expect the color data normalized
                                for (int c = 0; c < attribute.count * 4; c++) {
                                    model.meshes[meshIndex].colors[c] = (byte)(temp[c] * 255.0f);
                                }

                            }
                            else {
                                Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Color attribute data format not supported");
                            }
                        }
                    }

                    // Load primitive indices data (if provided)
                    if (gltf.meshes.get(i).primitives.get(p).indices != -1) {
                        gltfj_Accessor attribute = gltf.accessors.get(gltf.meshes.get(i).primitives.get(p).indices);

                        model.meshes[meshIndex].triangleCount = attribute.count/3;

                        if (attribute.componentType == gltfj_Accessor.AccessorDataType.UNSIGNED_SHORT) {
                            // Init raylib mesh indices to copy glTF attribute data
                            model.meshes[meshIndex].indicesS = new short[attribute.count];

                            // Load unsigned short data type into mesh.indices
                            gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                            gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                            byte[] sBuffer = new byte[Short.BYTES];

                            for (int is = 0, iso = 0; is < model.meshes[meshIndex].indicesS.length; is++, iso += Short.BYTES) {
                                for (int s = 0; s < sBuffer.length; s++) {
                                    sBuffer[s] = buffer.data[bufferView.offset + attribute.byteOffset + iso + s];
                                }
                                model.meshes[meshIndex].indicesS[is] = glFT_ByteArrayToShort(sBuffer);
                            }

                        }
                        else if (attribute.componentType == gltfj_Accessor.AccessorDataType.UNSIGNED_INT) {
                            // Init raylib mesh indices to copy glTF attribute data
                            model.meshes[meshIndex].indicesS = new short[attribute.count];

                            // Load data into a temp buffer to be converted to raylib data type
                            int[] temp = new int[attribute.count];

                            gltfj_BufferView bufferView= gltf.bufferViews.get(attribute.bufferView);
                            gltfj_Buffer buffer = gltf.buffers.get(bufferView.buffer);
                            byte[] iBuffer = new byte[Integer.BYTES];

                            for (int in = 0, ino = 0; in < temp.length; in++, ino += Integer.BYTES) {
                                for (int ib = 0; ib < iBuffer.length; ib++) {
                                    iBuffer[ib] = buffer.data[bufferView.offset + attribute.byteOffset + ino + ib];
                                }
                                temp[in] = glFT_ByteArrayToInt(iBuffer);
                            }
                            // Convert data to raylib indices data type (unsigned short)
                            for (int d = 0; d < attribute.count; d++) {
                                model.meshes[meshIndex].indicesS[d] = (short)temp[d];
                            }

                            Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Indices data converted from u32 to u16, possible loss of data");

                        }
                        else {
                            Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Indices data format not supported, use u16");
                        }
                    }
                    else {
                        model.meshes[meshIndex].triangleCount = model.meshes[meshIndex].vertexCount/3;    // Unindexed mesh
                    }

                    // Assign to the primitive mesh the corresponding material index
                    // NOTE: If no material defined, mesh uses the already assigned default material (index: 0)
                    for (int m = 0; m < gltf.materialCount; m++) {
                        // The primitive actually keeps the pointer to the corresponding material,
                        // raylib instead assigns to the mesh the by its index, as loaded in model.materials array
                        // To get the index, we check if material pointers match and we assign the corresponding index,
                        // skipping index 0, the default material
                        if (gltf.materials.get(m) == gltf.materials.get(gltf.meshes.get(i).primitives.get(p).material)){
                            model.meshMaterial[meshIndex] = m + 1;
                            break;
                        }
                    }

                    meshIndex++;       // Move to next mesh
                }
            }

            //todo: Load glTF meshes animation data

        }
        else {
            Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] failed to load glTF data");
        }

        return model;
    }

    // Load VOX (MagicaVoxel) mesh data
    private Model LoadVOX(String fileName) {
        Model model = new Model();

        int nbvertices = 0;
        int meshescount = 0;
        byte[] fileData = null;

        // Read vox file into buffer
        try {
            fileData = FileIO.LoadFileData(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (fileData == null) {
            Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load VOX file");
            return model;
        }

        // Read and build voxarray description
        VoxLoader voxLoader = new VoxLoader();
        int ret = voxLoader.Vox_LoadFromMemory(fileData);
        VoxLoader.VoxArray3D voxarray = voxLoader.pvoxArray;

        if (ret != VoxLoader.VOX_SUCCESS) {
            // Error
            FileIO.UnloadFileData(fileData);

            Tracelog(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load VOX data");
            return model;
        }
        else {
            // Success: Compute meshes count
            nbvertices = voxarray.vertices.used;
            meshescount = 1 + (nbvertices/65536);

            Tracelog(LOG_INFO, "MODEL: [" + fileName + "] VOX data loaded successfully : " + nbvertices + " vertices/ " + meshescount + " meshes");
        }

        // Build models from meshes
        model.transform = MatrixIdentity();

        model.meshCount = meshescount;
        model.meshes = new Mesh[model.meshCount];
        for (int i = 0; i < model.meshCount; i++) {
            model.meshes[i] = new Mesh();
        }

        model.meshMaterial = new int[model.meshCount];

        model.materialCount = 1;
        model.materials = new Material[model.materialCount];
        model.materials[0] = LoadMaterialDefault();

        // Init model meshes
        int verticesRemain = voxarray.vertices.used;
        int verticesMax = 65532; // 5461 voxels x 12 vertices per voxel -> 65532 (must be inf 65536)

        // 6*4 = 12 vertices per voxel
        VoxLoader.VoxVector3[] pvertices = new VoxLoader.VoxVector3[voxarray.vertices.array.length];
        System.arraycopy(voxarray.vertices.array, 0, pvertices, 0 , pvertices.length);
        VoxLoader.VoxColor[] pcolors = new VoxLoader.VoxColor[voxarray.colors.array.length];
        System.arraycopy(voxarray.colors.array, 0, pcolors, 0 , pvertices.length);

        short[] pindices = new short[voxarray.indices.array.length];    // 5461*6*6 = 196596 indices max per mesh
        System.arraycopy(voxarray.indices.array, 0, pindices, 0, pindices.length);

        int pverticesPointer = 0, pcolorsPointer = 0, pindicesPointer = 0;

        int size = 0;

        for (int i = 0; i < meshescount; i++) {
            Mesh pmesh = new Mesh();

            // Copy vertices
            pmesh.vertexCount = Math.min(verticesMax, verticesRemain);

            size = pmesh.vertexCount*3;
            pmesh.vertices = new float[size];
            for (int j = 0, k = pverticesPointer; j < pmesh.vertices.length; j+=3, k++) {
                pmesh.vertices[j] = pvertices[k].x;
                pmesh.vertices[j+1] = pvertices[k].y;
                pmesh.vertices[j+2] = pvertices[k].z;
            }

            pmesh.texcoords = new float[(size/3)*2];
            Arrays.fill(pmesh.texcoords, 0f);

            // Copy indices
            // TODO: Compute globals indices array
            size = voxarray.indices.used;
            pmesh.indicesS = new short[size];
            for (int j = 0; j < pmesh.indicesS.length; j++) {
                pmesh.indicesS[j] = pindices[j];
            }

            pmesh.triangleCount = (pmesh.vertexCount/4)*2;

            // Copy colors
            size = pmesh.vertexCount*4;
            pmesh.colors = new byte[size];
            for (int j = 0, k = pcolorsPointer; j < pmesh.colors.length; j+=4, k++) {
                pmesh.colors[j]   = pcolors[k].r;
                pmesh.colors[j+1] = pcolors[k].g;
                pmesh.colors[j+2] = pcolors[k].b;
                pmesh.colors[j+3] = pcolors[k].a;
            }

            // First material index
            model.meshMaterial[i] = 0;

            verticesRemain -= verticesMax;
            pverticesPointer += verticesMax;
            pcolorsPointer += verticesMax;

            model.meshes[i] = pmesh;
        }

        // Free buffers
        VoxLoader.Vox_FreeArrays(voxarray);
        FileIO.UnloadFileData(fileData);

        return model;
    }

    private float glFT_ByteArrayToFloat(byte[] floatBuffer) { return ByteBuffer.wrap(floatBuffer).order(ByteOrder.LITTLE_ENDIAN).getFloat(); }

    private short glFT_ByteArrayToShort(byte[] shortBuffer) { return ByteBuffer.wrap(shortBuffer).order(ByteOrder.LITTLE_ENDIAN).getShort(); }

    private int glFT_ByteArrayToInt(byte[] intBuffer) { return ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt(); }
}
