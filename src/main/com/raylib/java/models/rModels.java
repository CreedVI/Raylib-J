package com.raylib.java.models;

import com.creedvi.utils.m3dj.M3DJ;
import com.creedvi.utils.m3dj.model.M3DJ_Model;
import com.creedvi.utils.m3dj.model.chunks.M3DJ_Bone;
import com.creedvi.utils.m3dj.model.chunks.M3DJ_Property;
import com.raylib.java.Raylib;
import com.raylib.java.structs.*;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.iqm.*;
import com.raylib.java.utils.OBJLoader;
import com.raylib.java.utils.VoxLoader;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.jetbrains.annotations.Contract;
import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import static com.creedvi.utils.m3dj.M3DJ.M3D_UNDEF;
import static com.raylib.java.Config.*;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_GRAYSCALE;
import static com.raylib.java.rlgl.RLGL.rlShaderAttributeDataType.SHADER_ATTRIB_VEC2;
import static com.raylib.java.rlgl.RLGL.rlShaderAttributeDataType.SHADER_ATTRIB_VEC4;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.SHADER_UNIFORM_INT;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC4;
import static com.raylib.java.structs.Color.BLACK;
import static com.raylib.java.structs.Color.WHITE;
import static com.raylib.java.models.rModels.MaterialMapIndex.*;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.*;

/**
 rmodels - Basic functions to draw 3d shapes and load and draw 3d models <br/>
 &emsp;CONFIGURATION: <br/>
 &emsp;&emsp;#define SUPPORT_MODULE_RMODELS      1 <br/>
 &emsp;&emsp;&emsp;rmodels module is included in the build <br/>
 <br/>
 &emsp;&emsp;&emsp;#define SUPPORT_FILEFORMAT_OBJ      1 <br/>
 &emsp;&emsp;&emsp;SUPPORT_FILEFORMAT_MTL      1 <br/>
 &emsp;&emsp;&emsp;SUPPORT_FILEFORMAT_IQM      1 <br/>
 &emsp;&emsp;&emsp;SUPPORT_FILEFORMAT_GLTF     1 <br/>
 &emsp;&emsp;&emsp;SUPPORT_FILEFORMAT_GLTF_WRITE   0 <br/>
 &emsp;&emsp;&emsp;SUPPORT_FILEFORMAT_VOX      1 <br/>
 &emsp;&emsp;&emsp;SUPPORT_FILEFORMAT_M3D      1 <br/>
 &emsp;&emsp;&emsp;&emsp;Selected desired fileformats to be supported for model data loading <br/>
 <br/>
 &emsp;&emsp;&emsp;#define SUPPORT_MESH_GENERATION     1 <br/>
 &emsp;&emsp;&emsp;Support procedural mesh generation functions, uses external par_shapes.h library <br/>
 &emsp;&emsp;&emsp;NOTE: Some generated meshes DO NOT include generated texture coordinates <br/>
 <br/>
 <br/>
 &emsp;LICENSE: zlib/libpng
 <br/>
 &emsp;Copyright (c) 2013-2026 Ramon Santamaria (@raysan5) <br/>
 <br/>
 &emsp;This software is provided "as-is", without any express or implied warranty. In no event <br/>
 &emsp;will the authors be held liable for any damages arising from the use of this software. <br/>
 <br/>
 &emsp;Permission is granted to anyone to use this software for any purpose, including commercial <br/>
 &emsp;applications, and to alter it and redistribute it freely, subject to the following restrictions: <br/>
 <br/>
 &emsp;1. The origin of this software must not be misrepresented; you must not claim that you <br/>
 &emsp;wrote the original software. If you use this software in a product, an acknowledgment <br/>
 &emsp;in the product documentation would be appreciated but is not required. <br/>
 <br/>
 &emsp;2. Altered source versions must be plainly marked as such, and must not be misrepresented <br/>
 &emsp;as being the original software. <br/>
 <br/>
 &emsp;3. This notice may not be removed or altered from any source distribution. <br/>
 <br/>
 */
public class rModels {

    /**
     * Defines Material Map indices for the following material types:
     * <ol start="0">
     *     <li>Albedo/Diffuse</li>
     *     <li>Metalness/Specular</li>
     *     <li>Normal</li>
     *     <li>Roughness</li>
     *     <li>Occlusion</li>
     *     <li>Emission</li>
     *     <li>Height</li>
     *     <li>Cubemap</li>
     *     <li>Irradiance</li>
     *     <li>Prefilter</li>
     *     <li>BRDF</li>
     * </ol>
     */
    public enum MaterialMapIndex {

        MATERIAL_MAP_ALBEDO(0),     // Albedo material (same as: MATERIAL_MAP_DIFFUSE)
        MATERIAL_MAP_DIFFUSE(0),
        MATERIAL_MAP_METALNESS(1),     // Metalness material (same as: MATERIAL_MAP_SPECULAR),
        MATERIAL_MAP_SPECULAR(1),
        MATERIAL_MAP_NORMAL(2),     // Normal material
        MATERIAL_MAP_ROUGHNESS(3),     // Roughness material
        MATERIAL_MAP_OCCLUSION(4),     // Ambient occlusion material
        MATERIAL_MAP_EMISSION(5),     // Emission material
        MATERIAL_MAP_HEIGHT(6),     // Heightmap material
        MATERIAL_MAP_CUBEMAP(7),     // Cubemap material (NOTE: Uses GL_TEXTURE_CUBE_MAP)
        MATERIAL_MAP_IRRADIANCE(8),     // Irradiance material (NOTE: Uses GL_TEXTURE_CUBE_MAP)
        MATERIAL_MAP_PREFILTER(9),     // Prefilter material (NOTE: Uses GL_TEXTURE_CUBE_MAP)
        MATERIAL_MAP_BRDF(10);    // Brdf material

        private final int index;

        MaterialMapIndex(int index) {
            this.index = index;
        }

        public int GetIndex() {
            return index;
        }
    }

    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    final private int MAX_MATERIAL_MAPS = 12;    // Maximum number of maps supported

    // NOTE: Two additional vertex buffers required to store bone indices and bone weights
    // WARNING: Some GPUs could not support more than 8 VBOs
    final private int MAX_MESH_VERTEX_BUFFERS;      // Maximum vertex buffers (VBO) per mesh

    final private int GLTF_ANIMDELAY = 17;    // Animation frames delay, (~1000 ms/60 FPS = 16.666666* ms)
    final private int M3D_ANIMDELAY = 17;   // Animation frames delay, (~1000 ms/60 FPS = 16.666666* ms)

    final private Raylib context;

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    /**
     * Initialise rModels
     *
     * @param context context for Raylib-J
     */
    public rModels(Raylib context) {
        this.context = context;
        if (RL_SUPPORT_GPU_SKINNING) {
            MAX_MESH_VERTEX_BUFFERS = 9;
        }
        else {
            MAX_MESH_VERTEX_BUFFERS = 7;
        }
    }

    /**
     * Convert a color to a normalised greyscale value
     *
     * @param c Color to convert
     * @return Normalised greyscale value
     */
    private float GRAY_VALUE(Color c) {
        return (c.r + c.g + c.b) / 3f;
    }

    /**
     * Check if two colors are equal
     *
     * @param col1 First color to evaluate
     * @param col2 Second color to evaluate
     * @return {@code true} if colors have equal RGBA components
     */
    private static boolean COLOR_EQUAL(Color col1, Color col2) {
        return ((col1.r == col2.r) && (col1.g == col2.g) && (col1.b == col2.b) && (col1.a == col2.a));
    }

    /**
     * Draw a line in 3D world space <br/>
     * WARNING: OpenGL ES 2.0 does not support point mode drawing
     *
     * @param startPos Staring point of the line
     * @param endPos   Ending point of the line
     * @param color    Color to draw line
     */
    public void DrawLine3D(Vector3 startPos, Vector3 endPos, Color color) {
        // WARNING: Be careful with internal buffer vertex alignment
        // when using RL_LINES or RL_TRIANGLES, data is aligned to fit
        // lines-triangles-quads in the same indexed buffers!!!

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlVertex3f(startPos.x, startPos.y, startPos.z);
        context.rlgl.rlVertex3f(endPos.x, endPos.y, endPos.z);
        context.rlgl.rlEnd();
    }

    /**
     * Draw a point in 3D space, actually a small line <br/>
     * WARNING: OpenGL ES 2.0 does not support point mode drawing
     *
     * @param position Position of point in 3D space
     * @param color    Color to draw the point
     */
    public void DrawPoint3D(Vector3 position, Color color) {
        context.rlgl.rlPushMatrix();
        context.rlgl.rlTranslatef(position.x, position.y, position.z);
        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlVertex3f(0.0f, 0.0f, 0.0f);
        context.rlgl.rlVertex3f(0.0f, 0.0f, 0.1f);
        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a circle in 3D world space
     *
     * @param center        Center position of the circle
     * @param radius        Length of the circle radius
     * @param rotationAxis  Axis of rotation for the circle
     * @param rotationAngle Angle of rotation for the circle
     * @param color         Color to draw the circle
     */
    public void DrawCircle3D(Vector3 center, float radius, Vector3 rotationAxis, float rotationAngle, Color color) {
        context.rlgl.rlPushMatrix();
        context.rlgl.rlTranslatef(center.x, center.y, center.z);
        context.rlgl.rlRotatef(rotationAngle, rotationAxis.x, rotationAxis.y, rotationAxis.z);

        context.rlgl.rlBegin(RL_LINES);
        for (int i = 0; i < 360; i += 10) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i) * radius, (float) Math.cos(DEG2RAD * i) * radius, 0.0f);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 10)) * radius, (float) Math.cos(DEG2RAD * (i + 10)) * radius, 0.0f);
        }
        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a color-filled triangle. <br/>
     * CAUTION: Vertices must be in counter-clockwise order!
     *
     * @param v1    First vertex of the triangle
     * @param v2    Second vertex of the triangle
     * @param v3    Third vertex of the triangle
     * @param color Color to draw the triangle
     */
    public void DrawTriangle3D(Vector3 v1, Vector3 v2, Vector3 v3, Color color) {
        context.rlgl.rlBegin(RL_TRIANGLES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlVertex3f(v1.x, v1.y, v1.z);
        context.rlgl.rlVertex3f(v2.x, v2.y, v2.z);
        context.rlgl.rlVertex3f(v3.x, v3.y, v3.z);
        context.rlgl.rlEnd();
    }

    /**
     * Draw a triangle strip defined by points
     *
     * @param points {@code Vector3[]} of vertices. Must have a length greater than three.
     * @param color  Color to use when drawing the triangles
     */
    public void DrawTriangleStrip3D(Vector3[] points, Color color) {
        if (points.length < 3) {
            return; // Security check
        }

        context.rlgl.rlBegin(RL_TRIANGLES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 2; i < points.length; i++) {
            if ((i % 2) == 0) {
                context.rlgl.rlVertex3f(points[i].x, points[i].y, points[i].z);
                context.rlgl.rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
                context.rlgl.rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
            }
            else {
                context.rlgl.rlVertex3f(points[i].x, points[i].y, points[i].z);
                context.rlgl.rlVertex3f(points[i - 1].x, points[i - 1].y, points[i - 1].z);
                context.rlgl.rlVertex3f(points[i - 2].x, points[i - 2].y, points[i - 2].z);
            }
        }
        context.rlgl.rlEnd();
    }

    // Draw cube
    // NOTE: Cube position is the center position

    /**
     * Draw a cube in 3D space
     *
     * @param position Center position of the cube in 3D space
     * @param width    Width of the cube
     * @param height   Height of the cube
     * @param length   Length of the cube
     * @param color    Color to draw the cube
     */
    public void DrawCube(Vector3 position, float width, float height, float length, Color color) {
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        context.rlgl.rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> rotate -> translate)
        context.rlgl.rlTranslatef(position.x, position.y, position.z);
        //context.rlgl.rlRotatef(45, 0, 1, 0);
        //context.rlgl.rlScalef(1.0f, 1.0f, 1.0f);   // NOTE: Vertices are directly scaled on definition

        context.rlgl.rlBegin(RL_TRIANGLES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        // Front face
        context.rlgl.rlNormal3f(0f, 0f, 1f);
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Back face
        context.rlgl.rlNormal3f(0f, 0f, -1f);
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Top face
        context.rlgl.rlNormal3f(0f, 1f, 0f);
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Bottom Right

        // Bottom face
        context.rlgl.rlNormal3f(0f, -1f, 0f);
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left

        // Right face
        context.rlgl.rlNormal3f(1f, 0f, 0f);
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Left

        // Left face
        context.rlgl.rlNormal3f(-1f, 0f, 0f);
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Right

        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Right
        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a cube in 3D space
     *
     * @param position Center position of the cube in 3D space
     * @param size     {@code Vector3} of measurements for the cube as (Width, Height, Length)
     * @param color    Color to draw the cube
     */
    public void DrawCubeV(Vector3 position, Vector3 size, Color color) {
        DrawCube(position, size.x, size.y, size.z, color);
    }

    /**
     * Draw cube wires
     *
     * @param position Center position of the cube in 3D space
     * @param width    Width of the cube
     * @param height   Height of the cube
     * @param length   Length of the cube
     * @param color    Color to draw the cube
     */
    public void DrawCubeWires(Vector3 position, float width, float height, float length, Color color) {
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        context.rlgl.rlPushMatrix();
        context.rlgl.rlTranslatef(position.x, position.y, position.z);

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        // Front Face -----------------------------------------------------
        // Bottom Line
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right

        // Left Line
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right

        // Top Line
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left

        // Right Line
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Bottom Left

        // Back Face ------------------------------------------------------
        // Bottom Line
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right

        // Left Line
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Bottom Right
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right

        // Top Line
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left

        // Right Line
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Bottom Left

        // Top Face -------------------------------------------------------
        // Left Line
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z + length / 2);  // Top Left Front
        context.rlgl.rlVertex3f(x - width / 2, y + height / 2, z - length / 2);  // Top Left Back

        // Right Line
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z + length / 2);  // Top Right Front
        context.rlgl.rlVertex3f(x + width / 2, y + height / 2, z - length / 2);  // Top Right Back

        // Bottom Face  ---------------------------------------------------
        // Left Line
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z + length / 2);  // Top Left Front
        context.rlgl.rlVertex3f(x - width / 2, y - height / 2, z - length / 2);  // Top Left Back

        // Right Line
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z + length / 2);  // Top Right Front
        context.rlgl.rlVertex3f(x + width / 2, y - height / 2, z - length / 2);  // Top Right Back
        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw cube wires
     *
     * @param position Center position of the cube in 3D space
     * @param size     {@code Vector3} of measurements for the cube as (Width, Height, Length)
     * @param color    Color to draw the cube
     */
    public void DrawCubeWiresV(Vector3 position, Vector3 size, Color color) {
        DrawCubeWires(position, size.x, size.y, size.z, color);
    }

    /**
     * Draw sphere
     *
     * @param centerPos Center position of the cube in 3D space
     * @param radius    Length of the sphere's radius
     * @param color     Color to draw the cube
     */
    public void DrawSphere(Vector3 centerPos, float radius, Color color) {
        DrawSphereEx(centerPos, radius, 16, 16, color);
    }

    /**
     * Draw sphere with extended parameters
     *
     * @param centerPos Center position of the cube in 3D space
     * @param radius    Length of the sphere's radius
     * @param rings     Number of horizontal segments to use when rendering the sphere
     * @param slices    Number of vertical segments to use when rendering the sphere
     * @param color     Color to draw the sphere
     */
    public void DrawSphereEx(Vector3 centerPos, float radius, int rings, int slices, Color color) {
        if (false) {
            // Basic implementation, do not use it!
            // For a sphere with 16 rings and 16 slices it requires 8640 cos()/sin() function calls!
            // New optimized version below only requires 4 cos()/sin() calls

            int numVertex = (rings + 2) * slices * 6;

            context.rlgl.rlPushMatrix();
            // NOTE: Transformation is applied in inverse order (scale -> translate)
            context.rlgl.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
            context.rlgl.rlScalef(radius, radius, radius);

            context.rlgl.rlBegin(RL_TRIANGLES);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            for (int i = 0; i < (rings + 2); i++) {
                for (int j = 0; j < slices; j++) {
                    context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                            (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                            (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                    context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                                            (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                            (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                    context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                            (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                            (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));

                    context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                            (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                            (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                    context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                                            (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i))),
                                            (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                    context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                                            (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                            (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                }
            }

            context.rlgl.rlEnd();
            context.rlgl.rlPopMatrix();
        }
        else {
            context.rlgl.rlPushMatrix();
            // NOTE: Transformation is applied in inverse order (scale -> translate)
            context.rlgl.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
            context.rlgl.rlScalef(radius, radius, radius);

            context.rlgl.rlBegin(RL_TRIANGLES);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            float ringangle = DEG2RAD * (180.0f / (rings + 1)); // Angle between latitudinal parallels
            float sliceangle = DEG2RAD * (360.0f / slices); // Angle between longitudinal meridians

            float cosring = (float) Math.cos(ringangle);
            float sinring = (float) Math.sin(ringangle);
            float cosslice = (float) Math.cos(sliceangle);
            float sinslice = (float) Math.sin(sliceangle);

            Vector3[] vertices = new Vector3[4]; // Required to store face vertices
            vertices[0] = new Vector3();
            vertices[1] = new Vector3();
            vertices[2] = new Vector3(0, 1, 0);
            vertices[3] = new Vector3(sinring, cosring, 0);

            for (int i = 0; i < rings + 1; i++) {
                for (int j = 0; j < slices; j++) {
                    vertices[0] = vertices[2]; // Rotate around y axis to set up vertices for next face
                    vertices[1] = vertices[3];
                    vertices[2] = new Vector3(cosslice*vertices[2].x - sinslice*vertices[2].z, vertices[2].y, sinslice*vertices[2].x + cosslice*vertices[2].z); // Rotation matrix around y axis
                    vertices[3] = new Vector3(cosslice*vertices[3].x - sinslice*vertices[3].z, vertices[3].y, sinslice*vertices[3].x + cosslice*vertices[3].z);

                    context.rlgl.rlNormal3f(vertices[0].x, vertices[0].y, vertices[0].z);
                    context.rlgl.rlVertex3f(vertices[0].x, vertices[0].y, vertices[0].z);
                    context.rlgl.rlNormal3f(vertices[3].x, vertices[3].y, vertices[3].z);
                    context.rlgl.rlVertex3f(vertices[3].x, vertices[3].y, vertices[3].z);
                    context.rlgl.rlNormal3f(vertices[1].x, vertices[1].y, vertices[1].z);
                    context.rlgl.rlVertex3f(vertices[1].x, vertices[1].y, vertices[1].z);

                    context.rlgl.rlNormal3f(vertices[0].x, vertices[0].y, vertices[0].z);
                    context.rlgl.rlVertex3f(vertices[0].x, vertices[0].y, vertices[0].z);
                    context.rlgl.rlNormal3f(vertices[2].x, vertices[2].y, vertices[2].z);
                    context.rlgl.rlVertex3f(vertices[2].x, vertices[2].y, vertices[2].z);
                    context.rlgl.rlNormal3f(vertices[3].x, vertices[3].y, vertices[3].z);
                    context.rlgl.rlVertex3f(vertices[3].x, vertices[3].y, vertices[3].z);
                }

                vertices[2] = vertices[3]; // Rotate around z axis to set up  starting vertices for next ring
                vertices[3] = new Vector3(cosring * vertices[3].x + sinring * vertices[3].y, -sinring * vertices[3].x + cosring * vertices[3].y, vertices[3].z); // Rotation matrix around z axis
            }
            context.rlgl.rlEnd();
            context.rlgl.rlPopMatrix();
        }
    }

    /**
     * Draw sphere wires
     *
     * @param centerPos Center position of the cube in 3D space
     * @param radius    Length of the sphere's radius
     * @param rings     Number of horizontal segments to use when rendering the sphere
     * @param slices    Number of vertical segments to use when rendering the sphere
     * @param color     Color to draw the sphere
     */
    public void DrawSphereWires(Vector3 centerPos, float radius, int rings, int slices, Color color) {
        int numVertex = (rings + 2) * slices * 6;

        context.rlgl.rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> translate)
        context.rlgl.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        context.rlgl.rlScalef(radius, radius, radius);

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < (rings + 2); i++) {
            for (int j = 0; j < slices; j++) {
                context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));

                context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * ((j + 1) * 360 / slices)),
                                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * ((j + 1) * 360 / slices)));
                context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));

                context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))),
                                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * (i + 1))) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
                context.rlgl.rlVertex3f((float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.sin(DEG2RAD * (j * 360 / slices)),
                                        (float) Math.sin(DEG2RAD * (270 + (180 / (rings + 1)) * i)),
                                        (float) Math.cos(DEG2RAD * (270 + (180 / (rings + 1)) * i)) * (float) Math.cos(DEG2RAD * (j * 360 / slices)));
            }
        }

        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a cylinder <br/>
     * NOTE: This function may also be used to draw pyramids and cones
     *
     * @param position     Center position of the cylinder in 3D space
     * @param radiusTop    Length of the radius for the top face
     * @param radiusBottom Length of the radius for the bottom face
     * @param height       Height of the cylinder
     * @param sides        Number of sides to use when rendering the cylinder
     * @param color        Color to draw the cylinder
     */
    public void DrawCylinder(Vector3 position, float radiusTop, float radiusBottom, float height, int sides, Color color) {
        if (sides < 3) {
            sides = 3;
        }

        float angleStep = 360.0f / sides;

        context.rlgl.rlPushMatrix();
        context.rlgl.rlTranslatef(position.x, position.y, position.z);

        context.rlgl.rlBegin(RL_TRIANGLES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        if (radiusTop > 0) {
            // Draw Body -------------------------------------------------------------------------------------
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i * angleStep) * radiusBottom); //Bottom Left
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusBottom); //Bottom Right
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusTop); //Top Right

                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * i * angleStep) * radiusTop); //Top Left
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i * angleStep) * radiusBottom); //Bottom Left
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusTop); //Top Right
            }

            // Draw Cap --------------------------------------------------------------------------------------
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlVertex3f(0, height, 0);
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * i * angleStep) * radiusTop);
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusTop);
            }
        }
        else {
            // Draw Cone -------------------------------------------------------------------------------------
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlVertex3f(0, height, 0);
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i * angleStep) * radiusBottom);
                context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusBottom);
            }
        }

        // Draw Base -----------------------------------------------------------------------------------------
        for (int i = 0; i < sides; i++) {
            context.rlgl.rlVertex3f(0, 0, 0);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusBottom);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i * angleStep) * radiusBottom);
        }

        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a cylinder <br/>
     * NOTE: This function may also be used to draw pyramids and cones
     *
     * @param startPos    Position of the top face in 3D space
     * @param endPos      Position of the bottom face in 3D space
     * @param startRadius Length of the top radius
     * @param endRadius   Length of the bottom radius
     * @param sides       Number of sides to use when rendering the cylinder
     * @param color       Color to use when drawing the cylinder
     */
    public void DrawCylinderEx(Vector3 startPos, Vector3 endPos, float startRadius, float endRadius, int sides, Color color) {
        if (sides < 3) {
            sides = 3;
        }

        Vector3 direction = new Vector3(endPos.x - startPos.x, endPos.y - startPos.y, endPos.z - startPos.z);
        if ((direction.x == 0) && (direction.y == 0) && (direction.z == 0)) {
            return; // Security check
        }

        // Construct a basis of the base and the top face:
        Vector3 b1 = Vector3Normalize(Vector3Perpendicular(direction));
        Vector3 b2 = Vector3Normalize(Vector3CrossProduct(b1, direction));

        float baseAngle = (2.0f * PI) / sides;

        context.rlgl.rlBegin(RL_TRIANGLES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < sides; i++) {
            // Compute the four vertices
            float s1 = (float) Math.sin(baseAngle * (i + 0)) * startRadius;
            float c1 = (float) Math.cos(baseAngle * (i + 0)) * startRadius;
            Vector3 w1 = new Vector3(startPos.x + s1 * b1.x + c1 * b2.x, startPos.y + s1 * b1.y + c1 * b2.y, startPos.z + s1 * b1.z + c1 * b2.z);

            float s2 = (float) Math.sin(baseAngle * (i + 1)) * startRadius;
            float c2 = (float) Math.cos(baseAngle * (i + 1)) * startRadius;
            Vector3 w2 = new Vector3(startPos.x + s2 * b1.x + c2 * b2.x, startPos.y + s2 * b1.y + c2 * b2.y, startPos.z + s2 * b1.z + c2 * b2.z);

            float s3 = (float) Math.sin(baseAngle * (i + 0)) * endRadius;
            float c3 = (float) Math.cos(baseAngle * (i + 0)) * endRadius;
            Vector3 w3 = new Vector3(endPos.x + s3 * b1.x + c3 * b2.x, endPos.y + s3 * b1.y + c3 * b2.y, endPos.z + s3 * b1.z + c3 * b2.z);

            float s4 = (float) Math.sin(baseAngle * (i + 1)) * endRadius;
            float c4 = (float) Math.cos(baseAngle * (i + 1)) * endRadius;
            Vector3 w4 = new Vector3(endPos.x + s4 * b1.x + c4 * b2.x, endPos.y + s4 * b1.y + c4 * b2.y, endPos.z + s4 * b1.z + c4 * b2.z);

            if (startRadius > 0) {
                context.rlgl.rlVertex3f(startPos.x, startPos.y, startPos.z); // |
                context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);                   // T0
                context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);                   // |
            }
            //          w2 x.-----------x startPos
            context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);                       // |           |\'.  T0    /
            context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);                       // T1          | \ '.     /
            context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);                       // |           |T \  '.  /
            //             | 2 \ T 'x w1
            context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);                       // |        w4 x.---\-1-|---x endPos
            context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);                       // T2            '.  \  |T3/
            context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);                       // |               '. \ | /
            //                   '.\|/
            if (endRadius > 0) {                                             //                     'x w3
                context.rlgl.rlVertex3f(endPos.x, endPos.y, endPos.z);       // |
                context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);                   // T3
                context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);                   // |
            }                                                                //
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw a wired cylinder
     * NOTE: This function may also be used to draw pyramids and cones
     *
     * @param position     Center position of the cylinder in 3D space
     * @param radiusTop    Length of the radius for the top face
     * @param radiusBottom Length of the radius for the bottom face
     * @param height       Height of the cylinder
     * @param sides        Number of sides to use when rendering the cylinder
     * @param color        Color to draw the cylinder
     */
    public void DrawCylinderWires(Vector3 position, float radiusTop, float radiusBottom, float height, int sides, Color color) {
        if (sides < 3) {
            sides = 3;
        }

        float angleStep = 360.0f / sides;

        int numVertex = sides * 8;

        context.rlgl.rlPushMatrix();
        context.rlgl.rlTranslatef(position.x, position.y, position.z);

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < sides; i++) {
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i * angleStep) * radiusBottom);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusBottom);

            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusBottom);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusTop);

            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * (i + 1) * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * (i + 1) * angleStep) * radiusTop);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * i * angleStep) * radiusTop);

            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusTop, height, (float) Math.cos(DEG2RAD * i * angleStep) * radiusTop);
            context.rlgl.rlVertex3f((float) Math.sin(DEG2RAD * i * angleStep) * radiusBottom, 0, (float) Math.cos(DEG2RAD * i * angleStep) * radiusBottom);
        }

        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a wired cylinder <br/>
     * NOTE: This function may also be used to draw pyramids and cones
     *
     * @param startPos    Position of the top face in 3D space
     * @param endPos      Position of the bottom face in 3D space
     * @param startRadius Length of the top radius
     * @param endRadius   Length of the bottom radius
     * @param sides Number of sides to use when rendering the cylinder
     * @param color Color to draw the cylinder
     */
    public void DrawCylinderWiresEx(Vector3 startPos, Vector3 endPos, float startRadius, float endRadius, int sides, Color color) {
        if (sides < 3) {
            sides = 3;
        }

        Vector3 direction = new Vector3(endPos.x - startPos.x, endPos.y - startPos.y, endPos.z - startPos.z);
        if ((direction.x == 0) && (direction.y == 0) && (direction.z == 0)) {
            return; // Security check
        }

        // Construct a basis of the base and the top face:
        Vector3 b1 = Vector3Normalize(Vector3Perpendicular(direction));
        Vector3 b2 = Vector3Normalize(Vector3CrossProduct(b1, direction));

        float baseAngle = (2.0f * PI) / sides;

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < sides; i++) {
            // Compute the four vertices
            float s1 = (float) Math.sin(baseAngle * (i + 0)) * startRadius;
            float c1 = (float) Math.cos(baseAngle * (i + 0)) * startRadius;
            Vector3 w1 = new Vector3(startPos.x + s1 * b1.x + c1 * b2.x, startPos.y + s1 * b1.y + c1 * b2.y, startPos.z + s1 * b1.z + c1 * b2.z);

            float s2 = (float) Math.sin(baseAngle * (i + 1)) * startRadius;
            float c2 = (float) Math.cos(baseAngle * (i + 1)) * startRadius;
            Vector3 w2 = new Vector3(startPos.x + s2 * b1.x + c2 * b2.x, startPos.y + s2 * b1.y + c2 * b2.y, startPos.z + s2 * b1.z + c2 * b2.z);

            float s3 = (float) Math.sin(baseAngle * (i + 0)) * endRadius;
            float c3 = (float) Math.cos(baseAngle * (i + 0)) * endRadius;
            Vector3 w3 = new Vector3(endPos.x + s3 * b1.x + c3 * b2.x, endPos.y + s3 * b1.y + c3 * b2.y, endPos.z + s3 * b1.z + c3 * b2.z);

            float s4 = (float) Math.sin(baseAngle * (i + 1)) * endRadius;
            float c4 = (float) Math.cos(baseAngle * (i + 1)) * endRadius;
            Vector3 w4 = new Vector3(endPos.x + s4 * b1.x + c4 * b2.x, endPos.y + s4 * b1.y + c4 * b2.y, endPos.z + s4 * b1.z + c4 * b2.z);

            context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
            context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);

            context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
            context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);

            context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);
            context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw a capsule
     *
     * @param startPos Center position of the top endcap
     * @param endPos Center position of the bottom endcap
     * @param radius Length of the capsule's radius
     * @param rings     Number of horizontal segments to use when rendering the sphere
     * @param slices    Number of vertical segments to use when rendering the sphere
     * @param color     Color to draw the sphere
     */
    void DrawCapsule(Vector3 startPos, Vector3 endPos, float radius, int slices, int rings, Color color) {
        if (slices < 3) {
            slices = 3;
        }

        Vector3 direction = new Vector3(endPos.x - startPos.x, endPos.y - startPos.y, endPos.z - startPos.z);

        // draw a sphere if start and end points are the same
        boolean sphereCase = (direction.x == 0) && (direction.y == 0) && (direction.z == 0);
        if (sphereCase) {
            direction = new Vector3(0.0f, 1.0f, 0.0f);
        }

        // Construct a basis of the base and the caps:
        Vector3 b0 = Vector3Normalize(direction);
        Vector3 b1 = Vector3Normalize(Vector3Perpendicular(direction));
        Vector3 b2 = Vector3Normalize(Vector3CrossProduct(b1, direction));
        Vector3 capCenter = endPos;

        float baseSliceAngle = (2.0f * PI) / slices;
        float baseRingAngle = PI * 0.5f / rings;

        context.rlgl.rlBegin(RL_TRIANGLES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        // render both caps
        for (int c = 0; c < 2; c++) {
            for (int i = 0; i < rings; i++) {
                for (int j = 0; j < slices; j++) {
                    // Building up the rings from capCenter in the direction of the 'direction' vector computed earlier

                    // Compute the four vertices
                    float ringSin1 = (float) (Math.sin(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i)));
                    float ringCos1 = (float) (Math.cos(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i)));
                    Vector3 w1 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i)) * b0.x + ringSin1 * b1.x + ringCos1 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i)) * b0.y + ringSin1 * b1.y + ringCos1 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i)) * b0.z + ringSin1 * b1.z + ringCos1 * b2.z) * radius)
                    );

                    float ringSin2 = (float) (Math.sin(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i)));
                    float ringCos2 = (float) (Math.cos(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i)));
                    Vector3 w2 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i)) * b0.x + ringSin2 * b1.x + ringCos2 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i)) * b0.y + ringSin2 * b1.y + ringCos2 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i)) * b0.z + ringSin2 * b1.z + ringCos2 * b2.z) * radius)
                    );

                    float ringSin3 = (float) (Math.sin(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i + 1)));
                    float ringCos3 = (float) (Math.cos(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i + 1)));
                    Vector3 w3 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i + 1)) * b0.x + ringSin3 * b1.x + ringCos3 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i + 1)) * b0.y + ringSin3 * b1.y + ringCos3 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i + 1)) * b0.z + ringSin3 * b1.z + ringCos3 * b2.z) * radius)
                    );

                    float ringSin4 = (float) (Math.sin(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i + 1)));
                    float ringCos4 = (float) (Math.cos(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i + 1)));
                    Vector3 w4 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i + 1)) * b0.x + ringSin4 * b1.x + ringCos4 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i + 1)) * b0.y + ringSin4 * b1.y + ringCos4 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i + 1)) * b0.z + ringSin4 * b1.z + ringCos4 * b2.z) * radius)
                    );

                    // Make sure cap triangle normals are facing outwards
                    if (c == 0) {
                        context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
                        context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                        context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);

                        context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                        context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);
                        context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);
                    }
                    else {
                        context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
                        context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);
                        context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);

                        context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                        context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);
                        context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);
                    }
                }
            }
            capCenter = startPos;
            b0 = Vector3Scale(b0, -1.0f);
        }

        // render middle
        if (!sphereCase) {
            for (int j = 0; j < slices; j++) {
                // compute the four vertices
                float ringSin1 = (float) (Math.sin(baseSliceAngle * (j)) * radius);
                float ringCos1 = (float) (Math.cos(baseSliceAngle * (j)) * radius);
                Vector3 w1 = new Vector3(
                        startPos.x + ringSin1 * b1.x + ringCos1 * b2.x,
                        startPos.y + ringSin1 * b1.y + ringCos1 * b2.y,
                        startPos.z + ringSin1 * b1.z + ringCos1 * b2.z
                );

                float ringSin2 = (float) (Math.sin(baseSliceAngle * (j + 1)) * radius);
                float ringCos2 = (float) (Math.cos(baseSliceAngle * (j + 1)) * radius);
                Vector3 w2 = new Vector3(
                        startPos.x + ringSin2 * b1.x + ringCos2 * b2.x,
                        startPos.y + ringSin2 * b1.y + ringCos2 * b2.y,
                        startPos.z + ringSin2 * b1.z + ringCos2 * b2.z
                );

                float ringSin3 = (float) (Math.sin(baseSliceAngle * (j)) * radius);
                float ringCos3 = (float) (Math.cos(baseSliceAngle * (j)) * radius);
                Vector3 w3 = new Vector3(
                        endPos.x + ringSin3 * b1.x + ringCos3 * b2.x,
                        endPos.y + ringSin3 * b1.y + ringCos3 * b2.y,
                        endPos.z + ringSin3 * b1.z + ringCos3 * b2.z
                );

                float ringSin4 = (float) (Math.sin(baseSliceAngle * (j + 1)) * radius);
                float ringCos4 = (float) (Math.cos(baseSliceAngle * (j + 1)) * radius);
                Vector3 w4 = new Vector3(
                        endPos.x + ringSin4 * b1.x + ringCos4 * b2.x,
                        endPos.y + ringSin4 * b1.y + ringCos4 * b2.y,
                        endPos.z + ringSin4 * b1.z + ringCos4 * b2.z
                );
                //          w2 x.-----------x startPos
                context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);                         // |           |\'.  T0    /
                context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);                         // T1          | \ '.     /
                context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);                         // |           |T \  '.  /
                //             | 2 \ T 'x w1
                context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);                         // |        w4 x.---\-1-|---x endPos
                context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);                         // T2            '.  \  |T3/
                context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);                         // |               '. \ | /
                //                   '.\|/
                //                   'x w3
            }
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw capsule wires
     *
     * @param startPos Center position of the top endcap
     * @param endPos Center position of the bottom endcap
     * @param radius Length of the capsule's radius
     * @param rings     Number of horizontal segments to use when rendering the sphere
     * @param slices    Number of vertical segments to use when rendering the sphere
     * @param color     Color to draw the sphere
     */
    void DrawCapsuleWires(Vector3 startPos, Vector3 endPos, float radius, int slices, int rings, Color color) {
        if (slices < 3) {
            slices = 3;
        }

        Vector3 direction = new Vector3(endPos.x - startPos.x, endPos.y - startPos.y, endPos.z - startPos.z);

        // draw a sphere if start and end points are the same
        boolean sphereCase = (direction.x == 0) && (direction.y == 0) && (direction.z == 0);
        if (sphereCase) {
            direction = new Vector3(0.0f, 1.0f, 0.0f);
        }

        // Construct a basis of the base and the caps:
        Vector3 b0 = Vector3Normalize(direction);
        Vector3 b1 = Vector3Normalize(Vector3Perpendicular(direction));
        Vector3 b2 = Vector3Normalize(Vector3CrossProduct(b1, direction));
        Vector3 capCenter = endPos;

        float baseSliceAngle = (2.0f * PI) / slices;
        float baseRingAngle = PI * 0.5f / rings;

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        // render both caps
        for (int c = 0; c < 2; c++) {
            for (int i = 0; i < rings; i++) {
                for (int j = 0; j < slices; j++) {
                    // Building up the rings from capCenter in the direction of the 'direction' vector computed earlier

                    // Compute the four vertices
                    float ringSin1 = (float) (Math.sin(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i)));
                    float ringCos1 = (float) (Math.cos(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i)));
                    Vector3 w1 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i)) * b0.x + ringSin1 * b1.x + ringCos1 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i)) * b0.y + ringSin1 * b1.y + ringCos1 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i)) * b0.z + ringSin1 * b1.z + ringCos1 * b2.z) * radius)
                    );

                    float ringSin2 = (float) (Math.sin(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i)));
                    float ringCos2 = (float) (Math.cos(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i)));
                    Vector3 w2 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i)) * b0.x + ringSin2 * b1.x + ringCos2 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i)) * b0.y + ringSin2 * b1.y + ringCos2 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i)) * b0.z + ringSin2 * b1.z + ringCos2 * b2.z) * radius)
                    );

                    float ringSin3 = (float) (Math.sin(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i + 1)));
                    float ringCos3 = (float) (Math.cos(baseSliceAngle * (j)) * Math.cos(baseRingAngle * (i + 1)));
                    Vector3 w3 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i + 1)) * b0.x + ringSin3 * b1.x + ringCos3 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i + 1)) * b0.y + ringSin3 * b1.y + ringCos3 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i + 1)) * b0.z + ringSin3 * b1.z + ringCos3 * b2.z) * radius)
                    );

                    float ringSin4 = (float) (Math.sin(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i + 1)));
                    float ringCos4 = (float) (Math.cos(baseSliceAngle * (j + 1)) * Math.cos(baseRingAngle * (i + 1)));
                    Vector3 w4 = new Vector3(
                            (capCenter.x + (Math.sin(baseRingAngle * (i + 1)) * b0.x + ringSin4 * b1.x + ringCos4 * b2.x) * radius),
                            (capCenter.y + (Math.sin(baseRingAngle * (i + 1)) * b0.y + ringSin4 * b1.y + ringCos4 * b2.y) * radius),
                            (capCenter.z + (Math.sin(baseRingAngle * (i + 1)) * b0.z + ringSin4 * b1.z + ringCos4 * b2.z) * radius)
                    );

                    context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
                    context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);

                    context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                    context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);

                    context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
                    context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);

                    context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                    context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);

                    context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);
                    context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);
                }
            }
            capCenter = startPos;
            b0 = Vector3Scale(b0, -1.0f);
        }

        // render middle
        if (!sphereCase) {
            for (int j = 0; j < slices; j++) {
                // compute the four vertices
                float ringSin1 = (float) (Math.sin(baseSliceAngle * (j)) * radius);
                float ringCos1 = (float) (Math.cos(baseSliceAngle * (j)) * radius);
                Vector3 w1 = new Vector3(
                        startPos.x + ringSin1 * b1.x + ringCos1 * b2.x,
                        startPos.y + ringSin1 * b1.y + ringCos1 * b2.y,
                        startPos.z + ringSin1 * b1.z + ringCos1 * b2.z
                );

                float ringSin2 = (float) (Math.sin(baseSliceAngle * (j + 1)) * radius);
                float ringCos2 = (float) (Math.cos(baseSliceAngle * (j + 1)) * radius);
                Vector3 w2 = new Vector3(
                        startPos.x + ringSin2 * b1.x + ringCos2 * b2.x,
                        startPos.y + ringSin2 * b1.y + ringCos2 * b2.y,
                        startPos.z + ringSin2 * b1.z + ringCos2 * b2.z
                );

                float ringSin3 = (float) (Math.sin(baseSliceAngle * (j)) * radius);
                float ringCos3 = (float) (Math.cos(baseSliceAngle * (j)) * radius);
                Vector3 w3 = new Vector3(
                        endPos.x + ringSin3 * b1.x + ringCos3 * b2.x,
                        endPos.y + ringSin3 * b1.y + ringCos3 * b2.y,
                        endPos.z + ringSin3 * b1.z + ringCos3 * b2.z
                );

                float ringSin4 = (float) (Math.sin(baseSliceAngle * (j + 1)) * radius);
                float ringCos4 = (float) (Math.cos(baseSliceAngle * (j + 1)) * radius);
                Vector3 w4 = new Vector3(
                        endPos.x + ringSin4 * b1.x + ringCos4 * b2.x,
                        endPos.y + ringSin4 * b1.y + ringCos4 * b2.y,
                        endPos.z + ringSin4 * b1.z + ringCos4 * b2.z
                );

                context.rlgl.rlVertex3f(w1.x, w1.y, w1.z);
                context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);

                context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                context.rlgl.rlVertex3f(w4.x, w4.y, w4.z);

                context.rlgl.rlVertex3f(w2.x, w2.y, w2.z);
                context.rlgl.rlVertex3f(w3.x, w3.y, w3.z);
            }
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw a plane
     *
     * @param centerPos Center position of the plane in 3D space
     * @param size Size of the plane
     * @param color Color to draw the plane
     */
    public void DrawPlane(Vector3 centerPos, Vector2 size, Color color) {

        // NOTE: Plane is always created on XZ ground
        context.rlgl.rlPushMatrix();
        context.rlgl.rlTranslatef(centerPos.x, centerPos.y, centerPos.z);
        context.rlgl.rlScalef(size.x, 1.0f, size.y);

        context.rlgl.rlBegin(RL_QUADS);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlNormal3f(0.0f, 1.0f, 0.0f);

        context.rlgl.rlVertex3f(-0.5f, 0.0f, -0.5f);
        context.rlgl.rlVertex3f(-0.5f, 0.0f, 0.5f);
        context.rlgl.rlVertex3f(0.5f, 0.0f, 0.5f);
        context.rlgl.rlVertex3f(0.5f, 0.0f, -0.5f);
        context.rlgl.rlEnd();
        context.rlgl.rlPopMatrix();
    }

    /**
     * Draw a ray line
     *
     * @param ray {@code Ray} to draw
     * @param color Color to draw the ray
     */
    public void DrawRay(Ray ray, Color color) {
        float scale = 10000;

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        context.rlgl.rlVertex3f(ray.position.x, ray.position.y, ray.position.z);
        context.rlgl.rlVertex3f(ray.position.x + ray.direction.x * scale, ray.position.y + ray.direction.y * scale, ray.position.z + ray.direction.z * scale);
        context.rlgl.rlEnd();
    }

    /**
     * Draw a grid centered at the origin (0, 0, 0)
     *
     * @param slices
     * @param spacing
     */
    public void DrawGrid(int slices, float spacing) {
        int halfSlices = slices / 2;

        context.rlgl.rlBegin(RL_LINES);
        for (int i = -halfSlices; i <= halfSlices; i++) {
            if (i == 0) {
                context.rlgl.rlColor3f(0.5f, 0.5f, 0.5f);
            }
            else {
                context.rlgl.rlColor3f(0.75f, 0.75f, 0.75f);
            }

            context.rlgl.rlVertex3f((float) i * spacing, 0.0f, (float) -halfSlices * spacing);
            context.rlgl.rlVertex3f((float) i * spacing, 0.0f, (float) halfSlices * spacing);

            context.rlgl.rlVertex3f((float) -halfSlices * spacing, 0.0f, (float) i * spacing);
            context.rlgl.rlVertex3f((float) halfSlices * spacing, 0.0f, (float) i * spacing);
        }
        context.rlgl.rlEnd();
    }

    /**
     * Load model from files (mesh and material)
     *
     * @param fileName Filepath to model
     * @return Model defined by file
     */
    public Model LoadModel(String fileName) {

        Model model = new Model();

        if (SUPPORT_FILEFORMAT_OBJ && context.files.IsFileExtension(fileName, ".obj")) {
            model = LoadOBJ(fileName);
        }
        else if (SUPPORT_FILEFORMAT_IQM && context.files.IsFileExtension(fileName, ".iqm")) {
            model = LoadIQM(fileName);
        }
        else if (SUPPORT_FILEFORMAT_GLTF && ((context.files.IsFileExtension(fileName, ".gltf") || context.files.IsFileExtension(fileName, ".glb")))) {
            model = LoadGLTF(fileName);
        }
        else if (SUPPORT_FILEFORMAT_VOX && context.files.IsFileExtension(fileName, ".vox")) {
            model = LoadVOX(fileName);
        }
        else if (SUPPORT_FILEFORMAT_M3D && (context.files.IsFileExtension(fileName, ".m3d") || context.files.IsFileExtension(fileName, ".a3d"))) {
            model = LoadM3D(fileName);
        }

        // Make sure model transform is set to identity matrix!
        model.transform = MatrixIdentity();

        if ((model.meshCount != 0) && (model.meshes != null)) {
            // Upload vertex data to GPU (static meshes)
            for (int i = 0; i < model.meshCount; i++) {
                UploadMesh(model.meshes[i], false);
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: [%s] Failed to load model mesh(es) data", fileName);
        }

        if (model.materialCount == 0) {
            context.tracelog.TRACELOG(LOG_WARNING, "MATERIAL: [%s] Failed to load model material data, default to white material", fileName);

            model.materialCount = 1;
            model.materials = new Material[model.materialCount];
            model.materials[0] = LoadMaterialDefault();

            if (model.meshMaterial == null) {
                model.meshMaterial = new int[model.meshCount];
                Arrays.fill(model.meshMaterial, 0);
            }
        }

        return model;
    }

    /**
     * Load a model from a generated mesh
     *
     * @param mesh Mesh to use for model generation
     * @return {@code Model} defined by the mesh
     */
    public Model LoadModelFromMesh(Mesh mesh) {
        Model model = new Model();

        model.transform = MatrixIdentity();

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

    /**
     * Check if a model is valid (loaded in GPU, VAO/VBOs)
     *
     * @param model Model to evaluate
     * @return {@code true} if {@code model} is valid
     */
    public boolean IsModelValid(Model model) {
        boolean result = false;

        if ((model.meshes != null) &&           // Validate model contains some mesh
                (model.materials != null) &&        // Validate model contains some material (at least default one)
                (model.meshMaterial != null) &&     // Validate mesh-material linkage
                (model.meshCount > 0) &&            // Validate mesh count
                (model.materialCount > 0)) {
            result = true; // Validate material count
        }

        // NOTE: Many elements could be validated from a model, including every model mesh VAO/VBOs
        // but some VBOs could not be used, it depends on Mesh vertex data
        for (int i = 0; i < model.meshCount; i++) {
            if ((model.meshes[i].vertices != null) && (model.meshes[i].vboId[0] == 0)) {
                result = false;
                break;
            }  // Vertex position buffer not uploaded to GPU
            if ((model.meshes[i].texcoords != null) && (model.meshes[i].vboId[1] == 0)) {
                result = false;
                break;
            }  // Vertex textcoords buffer not uploaded to GPU
            if ((model.meshes[i].normals != null) && (model.meshes[i].vboId[2] == 0)) {
                result = false;
                break;
            }  // Vertex normals buffer not uploaded to GPU
            if ((model.meshes[i].colors != null) && (model.meshes[i].vboId[3] == 0)) {
                result = false;
                break;
            }  // Vertex colors buffer not uploaded to GPU
            if ((model.meshes[i].tangents != null) && (model.meshes[i].vboId[4] == 0)) {
                result = false;
                break;
            }  // Vertex tangents buffer not uploaded to GPU
            if ((model.meshes[i].texcoords2 != null) && (model.meshes[i].vboId[5] == 0)) {
                result = false;
                break;
            }  // Vertex texcoords2 buffer not uploaded to GPU
            if ((model.meshes[i].indices != null) && (model.meshes[i].vboId[6] == 0)) {
                result = false;
                break;
            }  // Vertex indices buffer not uploaded to GPU
            if ((model.meshes[i].boneIndices != null) && (model.meshes[i].vboId[7] == 0)) {
                result = false;
                break;
            }  // Vertex boneIds buffer not uploaded to GPU
            if ((model.meshes[i].boneWeights != null) && (model.meshes[i].vboId[8] == 0)) {
                result = false;
                break;
            }  // Vertex boneWeights buffer not uploaded to GPU

            // NOTE: Some OpenGL versions do not support VAO, so avoid below check
            //if (model.meshes[i].vaoId == 0) { result = false; break }
        }

        return result;
    }

    /**
     * Unload model (meshes/materials) from memory (RAM and/or VRAM) <br/>
     * NOTE: This function takes care of all model elements, for detailed control over them, use UnloadMesh() and UnloadMaterial()
     *
     * @param model Model to unload
     * @see rModels#UnloadMesh(Mesh)
     * @see rModels#UnloadMaterial(Material)
     */
    @Contract(mutates = "param")
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
        model.skeleton.bones = null;
        model.skeleton.bindPose = null;

        context.tracelog.TRACELOG(LOG_INFO, "MODEL: Unloaded model (and meshes) from RAM and VRAM");
    }

    /**
     * Compute model bounding box limits (considers all meshes)
     *
     * @param model Model to evaluate
     * @return {@code BoundingBox} defined by {@code model}
     */
    public BoundingBox GetModelBoundingBox(Model model) {
        BoundingBox bounds = new BoundingBox();

        if (model.meshCount > 0) {
            Vector3 temp = new Vector3();
            bounds = GetMeshBoundingBox(model.meshes[0]);

            for (int i = 1; i < model.meshCount; i++) {
                BoundingBox tempBounds = GetMeshBoundingBox(model.meshes[i]);

                temp.x = Math.min(bounds.min.x, tempBounds.min.x);
                temp.y = Math.min(bounds.min.y, tempBounds.min.y);
                temp.z = Math.min(bounds.min.z, tempBounds.min.z);
                bounds.min = new Vector3(temp.x, temp.y, temp.z);

                temp.x = Math.max(bounds.max.x, tempBounds.max.x);
                temp.y = Math.max(bounds.max.y, tempBounds.max.y);
                temp.z = Math.max(bounds.max.z, tempBounds.max.z);
                bounds.max = new Vector3(temp.x, temp.y, temp.z);
            }
        }

        // Apply model.transform to bounding box
        // WARNING: Current BoundingBox structure design does not support rotation transformations,
        // in those cases is up to the user to calculate the proper box bounds (8 vertices transformed)
        bounds.min = Vector3Transform(bounds.min, model.transform);
        bounds.max = Vector3Transform(bounds.max, model.transform);

        return bounds;
    }

    /**
     * Upload vertex data into a VAO (if supported) and VBO
     *
     * @param mesh {@code Mesh} to upload
     * @param dynamic {@code true} if {@code mesh} contains dynamic vertex buffers
     */
    public void UploadMesh(Mesh mesh, boolean dynamic) {
        if (mesh.vaoId > 0) {
            // Check if mesh has already been loaded in GPU
            context.tracelog.TRACELOG(LOG_WARNING, "VAO: [ID " + mesh.vaoId + "] Trying to re-load an already loaded mesh");
            return;
        }

        mesh.vboId = new int[MAX_MESH_VERTEX_BUFFERS];

        mesh.vaoId = 0;        // Vertex Array Object
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION] = 0;     // Vertex buffer: positions
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD] = 0;     // Vertex buffer: texcoords
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL] = 0;     // Vertex buffer: normals
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR] = 0;     // Vertex buffer: colors
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT] = 0;     // Vertex buffer: tangents
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2] = 0;     // Vertex buffer: texcoords2
        mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_INDICES] = 0;     // Vertex buffer: indices

        if (RL_SUPPORT_GPU_SKINNING) {
            mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES] = 0;      // Vertex buffer: boneIndices
            mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS] = 0;  // Vertex buffer: boneWeights
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            mesh.vaoId = context.rlgl.rlLoadVertexArray();
            context.rlgl.rlEnableVertexArray(mesh.vaoId);

            // NOTE: Attributes must be uploaded considering default locations points

            // Enable vertex attributes: position (shader-location = 0)
            float[] vertices = (mesh.animVertices != null) ? mesh.animVertices : mesh.vertices;
            mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION] = context.rlgl.rlLoadVertexBuffer(vertices, dynamic);
            context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION, 3, RL_FLOAT, false, 0, 0);
            context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION);

            // Enable vertex attributes: texcoords (shader-location = 1)
            if (mesh.texcoords != null) {
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD] = context.rlgl.rlLoadVertexBuffer(mesh.texcoords, dynamic);
                context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD, 2, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD);
            }
            else {
                float[] value = {0.0f, 0.0f};

                context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD, value, SHADER_ATTRIB_VEC2, 2);
                context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD);
            }

            if (mesh.normals != null) {
                // Enable vertex attributes: normals (shader-location = 2)
                float[] normals = (mesh.animNormals != null) ? mesh.animNormals : mesh.normals;
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL] = context.rlgl.rlLoadVertexBuffer(normals, dynamic);
                context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL, 3, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL);
            }
            else {
                // Default vertex attribute: normal
                // WARNING: Default value provided to shader if location available
                float[] value = {0.0f, 0.0f, 0.0f};
                context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL, value, rlShaderAttributeDataType.SHADER_ATTRIB_VEC3, 3);
                context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL);
            }

            if (mesh.colors != null) {
                // Enable vertex attribute: color (shader-location = 3)
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR] = context.rlgl.rlLoadVertexBuffer(mesh.colors, dynamic);
                context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR, 4, RL_UNSIGNED_BYTE, true, 0, 0);
                context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR);
            }
            else {
                // Default vertex attribute: color
                // WARNING: Default value provided to shader if location available
                float[] value = {1.0f, 1.0f, 1.0f, 1.0f}; // WHITE
                context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR, value, SHADER_ATTRIB_VEC4, 4);
                context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR);
            }

            if (mesh.tangents != null) {
                // Enable vertex attribute: tangent (shader-location = 4)
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT] = context.rlgl.rlLoadVertexBuffer(mesh.tangents, dynamic);
                context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT, 4, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT);
            }
            else {
                // Default tangents vertex attribute
                // WARNING: Default value provided to shader if location available
                float[] value = {1.0f, 0.0f, 0.0f, 1.0f};
                context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT, value, SHADER_ATTRIB_VEC4, 4);
                context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT);
            }

            if (mesh.texcoords2 != null) {
                // Enable vertex attribute: texcoord2 (shader-location = 5)
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2] = context.rlgl.rlLoadVertexBuffer(mesh.texcoords2, dynamic);
                context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2, 2, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2);
            }
            else {
                // Default vertex attribute: texcoord2
                // WARNING: Default value provided to shader if location available
                float[] value = {0.0f, 0.0f};
                context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2, value, SHADER_ATTRIB_VEC2, 2);
                context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2);
            }

            if (RL_SUPPORT_GPU_SKINNING) {
                if (mesh.boneIndices != null) {
                    // Enable vertex attribute: boneIndices (shader-location = 7)
                    mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES] = context.rlgl.rlLoadVertexBuffer(mesh.boneIndices, dynamic);
                    context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES, 4, RL_UNSIGNED_BYTE, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES);
                }
                else {
                    // Default vertex attribute: boneIds
                    // WARNING: Default value provided to shader if location available
                    float[] value ={
                        0.0f, 0.0f, 0.0f, 0.0f
                    } ;
                    context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES, value, SHADER_ATTRIB_VEC4, 4);
                    context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES);
                }

                if (mesh.boneWeights != null) {
                    // Enable vertex attribute: boneWeights (shader-location = 8)
                    mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS] = context.rlgl.rlLoadVertexBuffer(mesh.boneWeights, dynamic);
                    context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS, 4, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS);
                }
                else {
                    // Default vertex attribute: boneWeights
                    // WARNING: Default value provided to shader if location available
                    float[] value ={
                        0.0f, 0.0f, 0.0f, 0.0f
                    } ;
                    context.rlgl.rlSetVertexAttributeDefault(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS, value, SHADER_ATTRIB_VEC4, 2);
                    context.rlgl.rlDisableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS);
                }
            }

            if (mesh.indices != null) {
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_INDICES] = context.rlgl.rlLoadVertexBufferElement(mesh.indices, dynamic);
            }

            if (mesh.indicesS != null) {
                mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_INDICES] = context.rlgl.rlLoadVertexBufferElement(mesh.indicesS, dynamic);
            }

            if (mesh.vaoId > 0) {
                context.tracelog.TRACELOG(LOG_INFO, "VAO: [ID " + mesh.vaoId + "] Mesh uploaded successfully to VRAM (GPU)");
            }
            else {
                context.tracelog.TRACELOG(LOG_INFO, "VBO: Mesh uploaded successfully to VRAM (GPU)");
            }

            context.rlgl.rlDisableVertexArray();
        }
    }

    /**
     * Update mesh vertex data in GPU for a specific buffer index
     *
     * @param mesh Mesh to update
     * @param index VBO to update
     * @param data data to upload to the VBO
     * @param offset Offset in {@code data} to upload
     */
    public void UpdateMeshBuffer(Mesh mesh, int index, byte[] data, int offset) {
        context.rlgl.rlUpdateVertexBuffer(mesh.vboId[index], data, offset);
    }

    /**
     * Draw a 3d mesh with material and transform
     *
     * @param mesh Mesh to draw
     * @param material Material to use when drawing {@code mesh}
     * @param transform Transform matrix to use when rendering {@code mesh}
     */
    public void DrawMesh(Mesh mesh, Material material, Matrix transform) {
        if (GRAPHICS_API_OPENGL_11) {
            final int GL_VERTEX_ARRAY = 0x8074;
            final int GL_NORMAL_ARRAY = 0x8075;
            final int GL_COLOR_ARRAY = 0x8076;
            final int GL_TEXTURE_COORD_ARRAY = 0x8078;

            if ((mesh.texcoords != null) && (material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture.id > 0)) {
                context.rlgl.rlEnableTexture(material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture.id);
            }

            if (mesh.animVertices != null) {
                context.rlgl.rlEnableStatePointer(GL_VERTEX_ARRAY, mesh.animVertices);
            }
            else {
                context.rlgl.rlEnableStatePointer(GL_VERTEX_ARRAY, mesh.vertices);
            }

            if (mesh.texcoords != null) {
                context.rlgl.rlEnableStatePointer(GL_TEXTURE_COORD_ARRAY, mesh.texcoords);
            }

            if (mesh.animNormals != null) {
                context.rlgl.rlEnableStatePointer(GL_NORMAL_ARRAY, mesh.animNormals);
            }
            else if (mesh.normals != null) {
                context.rlgl.rlEnableStatePointer(GL_NORMAL_ARRAY, mesh.normals);
            }

            if (mesh.colors != null) {
                context.rlgl.rlEnableStatePointer(GL_COLOR_ARRAY, mesh.colors);
            }

            context.rlgl.rlPushMatrix();
            context.rlgl.rlMultMatrixf(MatrixToFloat(transform));
            context.rlgl.rlColor4ub(
                    material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.r,
                    material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.g,
                    material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.b,
                    material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.a
            );

            if (mesh.indices != null) {
                context.rlgl.rlDrawVertexArrayElements(0, mesh.triangleCount * 3, mesh.indices);
            }
            else {
                context.rlgl.rlDrawVertexArray(0, mesh.vertexCount);
            }
            context.rlgl.rlPopMatrix();

            context.rlgl.rlDisableStatePointer(GL_VERTEX_ARRAY);
            context.rlgl.rlDisableStatePointer(GL_TEXTURE_COORD_ARRAY);
            context.rlgl.rlDisableStatePointer(GL_NORMAL_ARRAY);
            context.rlgl.rlDisableStatePointer(GL_COLOR_ARRAY);

            context.rlgl.rlDisableTexture();
        }

        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            // Bind shader program
            context.rlgl.rlEnableShader(material.shader.id);

            if (material.shader.locs == null) {
                return;
            }

            // Send required data to shader (matrices, values)
            //-----------------------------------------------------
            // Upload to shader material.colDiffuse
            if (material.shader.locs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()] != -1) {
                float[] values = {
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.r / 255.0f,
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.g / 255.0f,
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.b / 255.0f,
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.a / 255.0f
                };

                context.rlgl.rlSetUniform(material.shader.locs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()], values, SHADER_UNIFORM_VEC4);
            }

            // Upload to shader material.colSpecular (if location available)
            if (material.shader.locs[SHADER_LOC_COLOR_SPECULAR.GetLocation()] != -1) {
                float[] values = {
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.r / 255.0f,
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.g / 255.0f,
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.b / 255.0f,
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.a / 255.0f
                };

                context.rlgl.rlSetUniform(material.shader.locs[SHADER_LOC_COLOR_SPECULAR.GetLocation()], values, SHADER_UNIFORM_VEC4);
            }

            // Get a copy of current matrices to work with,
            // in case stereo render is required, and they need to be modified
            // NOTE: At this point the modelview matrix contains the view matrix (camera)
            // That's because BeginMode3D() sets it and there is no model-drawing function
            // that modifies it, all use rlPushMatrix() and rlPopMatrix()
            Matrix matModel = MatrixIdentity();
            Matrix matView = context.rlgl.rlGetMatrixModelview();
            Matrix matModelView = MatrixIdentity();
            Matrix matProjection = context.rlgl.rlGetMatrixProjection();

            // Upload view and projection matrices (if locations available)
            if (material.shader.locs[SHADER_LOC_MATRIX_VIEW.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_VIEW.GetLocation()], matView);
            }
            if (material.shader.locs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()], matProjection);
            }

            // Accumulate several model transformations:
            //    transform: model transformation provided (includes DrawModel() params combined with model.transform)
            //    context.rlgl.rlGetMatrixTransform(): rlgl internal transform matrix due to push/pop matrix stack
            matModel = MatrixMultiply(transform, context.rlgl.rlGetMatrixTransform());

            // Model transformation matrix is sent to shader uniform location: SHADER_LOC_MATRIX_MODEL
            if (material.shader.locs[SHADER_LOC_MATRIX_MODEL.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_MODEL.GetLocation()], matModel);
            }

            // Get model-view matrix
            matModelView = MatrixMultiply(matModel, matView);

            // Upload model normal matrix (if locations available)
            if (material.shader.locs[SHADER_LOC_MATRIX_NORMAL.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_NORMAL.GetLocation()], MatrixTranspose(MatrixInvert(matModel)));
            }
            //-----------------------------------------------------

            // Bind active texture maps (if available)
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id > 0) {
                    // Select current shader texture slot
                    context.rlgl.rlActiveTextureSlot(i);

                    // Enable texture for active slot
                    if ((i == MATERIAL_MAP_IRRADIANCE.GetIndex()) || (i == MATERIAL_MAP_PREFILTER.GetIndex()) || (i == MATERIAL_MAP_CUBEMAP.GetIndex())) {
                        context.rlgl.rlEnableTextureCubemap(material.maps[i].texture.id);
                    }
                    else {
                        context.rlgl.rlEnableTexture(material.maps[i].texture.id);
                    }

                    context.rlgl.rlSetUniform(material.shader.locs[SHADER_LOC_MAP_DIFFUSE.GetLocation() + i], new float[]{i}, SHADER_UNIFORM_INT);
                }
            }

            // Try binding vertex array objects (VAO) or use VBOs if not possible
            // WARNING: UploadMesh() enables all vertex attributes available in mesh and sets default attribute values
            // for shader expected vertex attributes that are not provided by the mesh (i.e. colors)
            // This could be a dangerous approach because different meshes with different shaders can enable/disable some attributes
            if (!context.rlgl.rlEnableVertexArray(mesh.vaoId)) {
                // Bind mesh VBO data: vertex position (shader-location = 0)
                context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION]);
                context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_POSITION.GetLocation()], 3, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_POSITION.GetLocation()]);

                // Bind mesh VBO data: vertex texcoords (shader-location = 1)
                context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD]);
                context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()], 2, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()]);

                if (material.shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()] != -1) {
                    // Bind mesh VBO data: vertex normals (shader-location = 2)
                    context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL]);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()], 3, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()]);
                }

                // Bind mesh VBO data: vertex colors (shader-location = 3, if available)
                if (material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()] != -1) {
                    if (mesh.vboId[3] != 0) {
                        context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR]);
                        context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()], 4, RL_UNSIGNED_BYTE, true, 0, 0);
                        context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);
                    }
                    else {
                        // Set default value for defined vertex attribute in shader but not provided by mesh
                        // WARNING: It could result in GPU undefined behaviour
                        float[] value = {1.0f, 1.0f, 1.0f, 1.0f};
                        context.rlgl.rlSetVertexAttributeDefault(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()], value, SHADER_ATTRIB_VEC4, 4);
                        context.rlgl.rlDisableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);
                    }
                }

                // Bind mesh VBO data: vertex tangents (shader-location = 4, if available)
                if (material.shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()] != -1) {
                    context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT]);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()], 4, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()]);
                }

                // Bind mesh VBO data: vertex texcoords2 (shader-location = 5, if available)
                if (material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()] != -1) {
                    context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2]);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()], 2, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()]);
                }

                if (RL_SUPPORT_GPU_SKINNING) {
                    // Bind mesh VBO data: vertex bone ids (shader-location = 6, if available)
                    if (material.shader.locs[SHADER_LOC_VERTEX_BONEIDS.GetLocation()] != -1) {
                        context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES]);
                        context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEIDS.GetLocation()], 4, RL_UNSIGNED_BYTE, false, 0, 0);
                        context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEIDS.GetLocation()]);
                    }

                    // Bind mesh VBO data: vertex bone weights (shader-location = 7, if available)
                    if (material.shader.locs[SHADER_LOC_VERTEX_BONEWEIGHTS.GetLocation()] != -1) {
                        context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS]);
                        context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEWEIGHTS.GetLocation()], 4, RL_FLOAT, false, 0, 0);
                        context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEWEIGHTS.GetLocation()]);
                    }
                }

                if (mesh.indices != null || mesh.indicesS != null) {
                    context.rlgl.rlEnableVertexBufferElement(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_INDICES]);
                }
            }

            int eyeCount = 1;
            if (context.rlgl.rlIsStereoRendererEnabled()) {
                eyeCount = 2;
            }

            for (int eye = 0; eye < eyeCount; eye++) {
                // Calculate model-view-projection matrix (MVP)
                Matrix matModelViewProjection = MatrixIdentity();
                if (eyeCount == 1) {
                    matModelViewProjection = MatrixMultiply(matModelView, matProjection);
                }
                else {
                    // Setup current eye viewport (half screen width)
                    context.rlgl.rlViewport(eye * context.rlgl.rlGetFramebufferWidth() / 2, 0, context.rlgl.rlGetFramebufferWidth() / 2, context.rlgl.rlGetFramebufferHeight());
                    matModelViewProjection = MatrixMultiply(MatrixMultiply(matModelView, context.rlgl.rlGetMatrixViewOffsetStereo(eye)), context.rlgl.rlGetMatrixProjectionStereo(eye));
                }

                // Send combined model-view-projection matrix to shader
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_MVP.GetLocation()], matModelViewProjection);

                // Draw mesh
                if (mesh.indices != null || mesh.indicesS != null) {
                    context.rlgl.rlDrawVertexArrayElements(0, mesh.triangleCount * 3, new float[0]);
                }
                else {
                    context.rlgl.rlDrawVertexArray(0, mesh.vertexCount);
                }
            }

            // Unbind all bound texture maps
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                // Select current shader texture slot
                context.rlgl.rlActiveTextureSlot(i);

                // Disable texture for active slot
                if ((i == MATERIAL_MAP_IRRADIANCE.GetIndex()) || (i == MATERIAL_MAP_PREFILTER.GetIndex()) || (i == MATERIAL_MAP_CUBEMAP.GetIndex())) {
                    context.rlgl.rlDisableTextureCubemap();
                }
                else {
                    context.rlgl.rlDisableTexture();
                }
            }

            // Disable all possible vertex array objects (or VBOs)
            context.rlgl.rlDisableVertexArray();
            context.rlgl.rlDisableVertexBuffer();
            context.rlgl.rlDisableVertexBufferElement();

            // Disable shader program
            context.rlgl.rlDisableShader();

            // Restore rlgl internal modelview and projection matrices
            context.rlgl.rlSetMatrixModelview(matView);
            context.rlgl.rlSetMatrixProjection(matProjection);
        }
    }

    /**
     * Draw multiple mesh instances with material and different transforms
     *
     * @param mesh Mesh to draw
     * @param material Material to use when drawing {@code mesh}
     * @param transforms {@code Matrix[]} of transform matrices to use for rendering
     * @param instances Number of instances of {@code mesh} to draw
     */
    public void DrawMeshInstanced(Mesh mesh, Material material, Matrix[] transforms, int instances) {
        if (GRAPHICS_API_OPENGL_33 || GRAPHICS_API_OPENGL_ES2) {
            // Instancing required variables
            float[] instanceTransform = null;
            int instancesVboId = 0;

            // Bind shader program
            context.rlgl.rlEnableShader(material.shader.id);

            // Send required data to shader (matrices, values)
            //-----------------------------------------------------
            // Upload to shader material.colDiffuse
            if (material.shader.locs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()] != -1) {
                float[] values = {
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.r / 255.0f,
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.g / 255.0f,
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.b / 255.0f,
                        (float) material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color.a / 255.0f
                };

                context.rlgl.rlSetUniform(material.shader.locs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()], values, SHADER_UNIFORM_VEC4);
            }

            // Upload to shader material.colSpecular (if location available)
            if (material.shader.locs[SHADER_LOC_COLOR_SPECULAR.GetLocation()] != -1) {
                float[] values = {
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.r / 255.0f,
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.g / 255.0f,
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.b / 255.0f,
                        (float) material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color.a / 255.0f
                };

                context.rlgl.rlSetUniform(material.shader.locs[SHADER_LOC_COLOR_SPECULAR.GetLocation()], values, SHADER_UNIFORM_VEC4);
            }

            // Get a copy of current matrices to work with,
            // in case stereo render is required, and they need to be modified
            // NOTE: At this point the modelview matrix contains the view matrix (camera)
            // That's because BeginMode3D() sets it and there is no model-drawing function
            // that modifies it, all use context.rlgl.rlPushMatrix() and context.rlgl.rlPopMatrix()
            Matrix matModel = MatrixIdentity();
            Matrix matView = context.rlgl.rlGetMatrixModelview();
            Matrix matModelView = MatrixIdentity();
            Matrix matProjection = context.rlgl.rlGetMatrixProjection();

            // Upload view and projection matrices (if locations available)
            if (material.shader.locs[SHADER_LOC_MATRIX_VIEW.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_VIEW.GetLocation()], matView);
            }
            if (material.shader.locs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()], matProjection);
            }

            // Create instances buffer
            instanceTransform = new float[instances * 16];

            // Fill buffer with instances transformations as float16 arrays
            for (int i = 0; i < instances; i++) {
                Float16 tmp = MatrixToFloatV(transforms[i]);
                System.arraycopy(tmp.v, 0, instanceTransform, i * 16, tmp.v.length);
            }

            // Enable mesh VAO to attach new buffer
            context.rlgl.rlEnableVertexArray(mesh.vaoId);

            // This could alternatively use a static VBO and either glMapBuffer() or glBufferSubData()
            // It isn't clear which would be reliably faster in all cases and on all platforms,
            // anecdotally glMapBuffer() seems quite slow (syncs) while glBufferSubData() seems
            // no faster, since all the transform matrices are transferred anyway
            instancesVboId = context.rlgl.rlLoadVertexBuffer(instanceTransform, false);

            // Instances transformation matrices are sent to shader attribute location: SHADER_LOC_VERTEX_INSTANCETRANSFORM
            if (material.shader.locs[SHADER_LOC_VERTEX_INSTANCETRANSFORM.GetLocation()] != -1) {
                for (int i = 0; i < 4; i++) {
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_INSTANCETRANSFORM.GetLocation()] + i);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_INSTANCETRANSFORM.GetLocation()] + i, 4, RL_FLOAT, false, 16*Float.BYTES, i*4*Float.BYTES);
                    context.rlgl.rlSetVertexAttributeDivisor(material.shader.locs[SHADER_LOC_VERTEX_INSTANCETRANSFORM.GetLocation()] + i, 1);
                }
            }

            context.rlgl.rlDisableVertexBuffer();
            context.rlgl.rlDisableVertexArray();

            // Accumulate internal matrix transform (push/pop) and view matrix
            // NOTE: In this case, model instance transformation must be computed in the shader
            matModelView = MatrixMultiply(context.rlgl.rlGetMatrixTransform(), matView);

            // Upload model normal matrix (if locations available)
            if (material.shader.locs[SHADER_LOC_MATRIX_NORMAL.GetLocation()] != -1) {
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_NORMAL.GetLocation()], MatrixTranspose(MatrixInvert(matModel)));
            }
            //-----------------------------------------------------

            // Bind active texture maps (if available)
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id > 0) {
                    // Select current shader texture slot
                    context.rlgl.rlActiveTextureSlot(i);

                    // Enable texture for active slot
                    if ((i == MATERIAL_MAP_IRRADIANCE.GetIndex()) || (i == MATERIAL_MAP_PREFILTER.GetIndex()) || (i == MATERIAL_MAP_CUBEMAP.GetIndex())) {
                        context.rlgl.rlEnableTextureCubemap(material.maps[i].texture.id);
                    }
                    else {
                        context.rlgl.rlEnableTexture(material.maps[i].texture.id);
                    }

                    context.rlgl.rlSetUniform(material.shader.locs[SHADER_LOC_MAP_DIFFUSE.GetLocation() + i], new float[]{i}, SHADER_UNIFORM_INT);
                }
            }

            // Try binding vertex array objects (VAO)
            // or use VBOs if not possible
            if (!context.rlgl.rlEnableVertexArray(mesh.vaoId)) {
                // Bind mesh VBO data: vertex position (shader-location = 0)
                context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_POSITION]);
                context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_POSITION.GetLocation()], 3, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_POSITION.GetLocation()]);

                // Bind mesh VBO data: vertex texcoords (shader-location = 1)
                context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD]);
                context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()], 2, RL_FLOAT, false, 0, 0);
                context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()]);

                if (material.shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()] != -1) {
                    // Bind mesh VBO data: vertex normals (shader-location = 2)
                    context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_NORMAL]);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()], 3, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()]);
                }

                // Bind mesh VBO data: vertex colors (shader-location = 3, if available)
                if (material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()] != -1) {
                    if (mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR] != 0) {
                        context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_COLOR]);
                        context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()], 4, RL_UNSIGNED_BYTE, true, 0, 0);
                        context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);
                    }
                    else {
                        // Set default value for unused attribute
                        // NOTE: Required when using default shader and no VAO support
                        float[] value = {
                                1.0f, 1.0f, 1.0f, 1.0f
                        };
                        context.rlgl.rlSetVertexAttributeDefault(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()], value, SHADER_ATTRIB_VEC4, 4);
                        context.rlgl.rlDisableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);
                    }
                }

                // Bind mesh VBO data: vertex tangents (shader-location = 4, if available)
                if (material.shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()] != -1) {
                    context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT]);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()], 4, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()]);
                }

                // Bind mesh VBO data: vertex texcoords2 (shader-location = 5, if available)
                if (material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()] != -1) {
                    context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_TEXCOORD2]);
                    context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()], 2, RL_FLOAT, false, 0, 0);
                    context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()]);
                }

                if (RL_SUPPORT_GPU_SKINNING) {
                    // Bind mesh VBO data: vertex bone ids (shader-location = 6, if available)
                    if (material.shader.locs[SHADER_LOC_VERTEX_BONEIDS.GetLocation()] != -1) {
                        context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEINDICES]);
                        context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEIDS.GetLocation()], 4, RL_UNSIGNED_BYTE, false, 0, 0);
                        context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEIDS.GetLocation()]);
                    }

                    // Bind mesh VBO data: vertex bone weights (shader-location = 7, if available)
                    if (material.shader.locs[SHADER_LOC_VERTEX_BONEWEIGHTS.GetLocation()] != -1) {
                        context.rlgl.rlEnableVertexBuffer(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_BONEWEIGHTS]);
                        context.rlgl.rlSetVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEWEIGHTS.GetLocation()], 4, RL_FLOAT, false, 0, 0);
                        context.rlgl.rlEnableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_BONEWEIGHTS.GetLocation()]);
                    }
                }

                if (mesh.indices != null) {
                    context.rlgl.rlEnableVertexBufferElement(mesh.vboId[RL_DEFAULT_SHADER_ATTRIB_LOCATION_INDICES]);
                }
            }

            // WARNING: Disable vertex attribute color input if mesh can not provide that data (despite location being enabled in shader)
            if (mesh.vboId[3] == 0) {
                context.rlgl.rlDisableVertexAttribute(material.shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()]);
            }

            int eyeCount = 1;
            if (context.rlgl.rlIsStereoRendererEnabled()) {
                eyeCount = 2;
            }

            for (int eye = 0; eye < eyeCount; eye++) {
                // Calculate model-view-projection matrix (MVP)
                Matrix matModelViewProjection = MatrixIdentity();
                if (eyeCount == 1) {
                    matModelViewProjection = MatrixMultiply(matModelView, matProjection);
                }
                else {
                    // Setup current eye viewport (half screen width)
                    context.rlgl.rlViewport(eye * context.rlgl.rlGetFramebufferWidth() / 2, 0, context.rlgl.rlGetFramebufferWidth() / 2, context.rlgl.rlGetFramebufferHeight());
                    matModelViewProjection = MatrixMultiply(MatrixMultiply(matModelView, context.rlgl.rlGetMatrixViewOffsetStereo(eye)), context.rlgl.rlGetMatrixProjectionStereo(eye));
                }

                // Send combined model-view-projection matrix to shader
                context.rlgl.rlSetUniformMatrix(material.shader.locs[SHADER_LOC_MATRIX_MVP.GetLocation()], matModelViewProjection);

                // Draw mesh instanced
                if (mesh.indices != null) {
                    context.rlgl.rlDrawVertexArrayElementsInstanced(0, mesh.triangleCount * 3, null, instances);
                }
                else {
                    context.rlgl.rlDrawVertexArrayInstanced(0, mesh.vertexCount, instances);
                }
            }

            // Unbind all binded texture maps
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id > 0) {
                    // Select current shader texture slot
                    context.rlgl.rlActiveTextureSlot(i);

                    // Disable texture for active slot
                    if ((i == MATERIAL_MAP_IRRADIANCE.GetIndex()) ||
                            (i == MATERIAL_MAP_PREFILTER.GetIndex()) ||
                            (i == MATERIAL_MAP_CUBEMAP.GetIndex())) {
                        context.rlgl.rlDisableTextureCubemap();
                    }
                    else {
                        context.rlgl.rlDisableTexture();
                    }
                }
            }

            // Disable all possible vertex array objects (or VBOs)
            context.rlgl.rlDisableVertexArray();
            context.rlgl.rlDisableVertexBuffer();
            context.rlgl.rlDisableVertexBufferElement();

            // Disable shader program
            context.rlgl.rlDisableShader();

            // Remove instance transforms buffer
            context.rlgl.rlUnloadVertexBuffer(instancesVboId);
        }
    }

    /**
     * Unload mesh from memory (RAM and VRAM)
     *
     * @param mesh Mesh to unload from memory
     */
    @Contract(mutates = "param")
    public void UnloadMesh(Mesh mesh) {
        // Unload rlgl mesh vboId data
        context.rlgl.rlUnloadVertexArray(mesh.vaoId);

        if (mesh.vboId != null) {
            for (int i = 0; i < MAX_MESH_VERTEX_BUFFERS; i++) {
                context.rlgl.rlUnloadVertexBuffer(mesh.vboId[i]);
            }
        }

        // Unload mesh vertex buffers
        mesh.vertices = null;
        mesh.texcoords = null;
        mesh.normals = null;
        mesh.colors = null;
        mesh.tangents = null;
        mesh.texcoords2 = null;
        mesh.indices = null;
        mesh.indicesS = null;

        // Unload mesh skin animation data
        mesh.boneWeights = null;
        mesh.boneIndices = null;

        // Unload mesh runtime CPU skinning data
        mesh.animVertices = null;
        mesh.animNormals = null;
    }

    /**
     * Export mesh data to file
     *
     * @param mesh Mesh to export
     * @param fileName Location to save file
     * @return {@code true} if file operation is successful
     */
    public boolean ExportMesh(Mesh mesh, String fileName) {
        boolean result = false;

        if (context.files.IsFileExtension(fileName, ".obj")) {
            StringBuilder txtData = new StringBuilder();

            txtData.append("# //////////////////////////////////////////////////////////////////////////////////\n");
            txtData.append("# //                                                                              //\n");
            txtData.append("# // rMeshOBJ exporter v1.0 - Mesh exported as triangle faces and not optimized   //\n");
            txtData.append("# //                                                                              //\n");
            txtData.append("# // more info and bugs-report:  github.com/raysan5/raylib                        //\n");
            txtData.append("# // feedback and support:       ray[at]raylib.com                                //\n");
            txtData.append("# //                                                                              //\n");
            txtData.append("# // Copyright (c) 2018-2026 Ramon Santamaria (@raysan5)                          //\n");
            txtData.append("# //                                                                              //\n");
            txtData.append("# //////////////////////////////////////////////////////////////////////////////////\n\n");
            txtData.append("# Vertex Count:     " + mesh.vertexCount + "\n");
            txtData.append("# Triangle Count:   " + mesh.triangleCount + "\n\n");

            txtData.append("g mesh\n");

            for (int i = 0, v = 0; i < mesh.vertexCount; i++, v += 3) {
                txtData.append("v " + String.format(String.valueOf(mesh.vertices[v]), "%.6f") + String.format(String.valueOf(mesh.vertices[v + 1]), "%.6f") + String.format(String.valueOf(mesh.vertices[v + 2]), "%.6f"));
            }

            for (int i = 0, v = 0; i < mesh.vertexCount; i++, v += 2) {
                txtData.append("vt " + String.format(String.valueOf(mesh.texcoords[v]), "%.6f") + String.format(String.valueOf(mesh.texcoords[v + 1]), "%.6f"));

            }

            for (int i = 0, v = 0; i < mesh.vertexCount; i++, v += 3) {
                txtData.append("v " + String.format(String.valueOf(mesh.normals[v]), "%.6f") + String.format(String.valueOf(mesh.normals[v + 1]), "%.6f") + String.format(String.valueOf(mesh.normals[v + 2]), "%.6f"));
            }

            if (mesh.indices != null) {
                for (int i = 0, v = 0; i < mesh.triangleCount; i++, v += 3) {
                    txtData.append("f " +
                                           mesh.indices[v] + 1 + "/" + mesh.indices[v] + 1 + "/" + mesh.indices[v] + 1 + " " +
                                           mesh.indices[v + 1] + 1 + "/" + mesh.indices[v + 1] + 1 + "/" + mesh.indices[v + 1] + 1 + " " +
                                           mesh.indices[v + 2] + 1 + "/" + mesh.indices[v + 2] + 1 + "/" + mesh.indices[v + 2] + 1 + "\n");
                }
            }
            else {
                for (int i = 0, v = 1; i < mesh.triangleCount; i++, v += 3) {
                    txtData.append("f " + v + "/" + v + "/" + v + " " + (v + 1) + "/" + (v + 1) + "/" + (v + 1) + " " + (v + 2) + "/" + (v + 2) + "/" + (v + 2) + "\n");
                }
            }

            txtData.append("\n");

            // NOTE: Text data length exported is determined by '\0' (null) character
            try {
                result = context.files.SaveFileText(fileName, txtData.toString());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (context.files.IsFileExtension(fileName, ".gltf") || context.files.IsFileExtension(fileName, ".glb")) {
            // TODO: Implement gltf/glb support
        }
        else if (context.files.IsFileExtension(fileName, ".raw")) {
            // TODO: Support additional file formats to export mesh vertex data
        }

        return result;
    }

    /**
     * Export mesh as code file (.h) defining multiple arrays of vertex attributes
     *
     * @param mesh Mesh to export
     * @param fileName Location to save file
     * @return {@code true} if file operation is successful
     */
    public boolean ExportMeshAsCode(Mesh mesh, String fileName) {
        boolean result = false;

        int TEXT_BYTES_PER_LINE = 20;

        // NOTE: Text data buffer size is fixed to 64MB
        StringBuilder txtData = new StringBuilder();

        txtData.append("////////////////////////////////////////////////////////////////////////////////////////\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// MeshAsCode exporter v1.0 - Mesh vertex data exported as arrays                     //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// more info and bugs-report:  github.com/raysan5/raylib                              //\n");
        txtData.append("// feedback and support:       ray[at]raylib.com                                      //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("// Copyright (c) 2023 Ramon Santamaria (@raysan5)                                     //\n");
        txtData.append("//                                                                                    //\n");
        txtData.append("////////////////////////////////////////////////////////////////////////////////////////\n\n");

        // Get file name from path and convert variable name to uppercase
        String varFileName = context.files.GetFileNameWithoutExt(fileName).toUpperCase(); // NOTE: Using function provided by [rcore] module

        // Add image information
        txtData.append("// Mesh basic information\n");
        txtData.append(String.format("#define %s_VERTEX_COUNT    %d\n", varFileName, mesh.vertexCount));
        txtData.append(String.format("#define %s_TRIANGLE_COUNT   %d\n\n", varFileName, mesh.triangleCount));

        // Define vertex attributes data as separate arrays
        //-----------------------------------------------------------------------------------------
        if (mesh.vertices != null) {
            // Vertex position (XYZ - 3 components per vertex - float)
            txtData.append(String.format("static float %s_VERTEX_DATA[%d] = { ", varFileName, mesh.vertexCount*3));
            for (int i = 0; i < mesh.vertexCount*3 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "%.3ff,\n" : "%.3ff, "), mesh.vertices[i]));
            }
            txtData.append(String.format("%.3ff };\n\n", mesh.vertices[mesh.vertexCount*3 - 1]));
        }

        if (mesh.texcoords != null) {
            // Vertex texture coordinates (UV - 2 components per vertex - float)
            txtData.append(String.format("static float %s_TEXCOORD_DATA[%d] = { ", varFileName, mesh.vertexCount*2));
            for (int i = 0; i < mesh.vertexCount*2 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "%.3ff,\n" : "%.3ff, "), mesh.texcoords[i]));
            }
            txtData.append(String.format("%.3ff };\n\n", mesh.texcoords[mesh.vertexCount*2 - 1]));
        }

        if (mesh.texcoords2 != null) {
            // Vertex texture coordinates (UV - 2 components per vertex - float)
            txtData.append(String.format("static float %s_TEXCOORD2_DATA[%d] = { ", varFileName, mesh.vertexCount*2));
            for (int i = 0; i < mesh.vertexCount*2 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "%.3ff,\n" : "%.3ff, "), mesh.texcoords2[i]));
            }
            txtData.append(String.format("%.3ff };\n\n", mesh.texcoords2[mesh.vertexCount*2 - 1]));
        }

        if (mesh.normals != null) {
            // Vertex normals (XYZ - 3 components per vertex - float)
            txtData.append(String.format("static float %s_NORMAL_DATA[%d] = { ", varFileName, mesh.vertexCount*3));
            for (int i = 0; i < mesh.vertexCount*3 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "%.3ff,\n" : "%.3ff, "), mesh.normals[i]));
            }
            txtData.append(String.format("%.3ff };\n\n", mesh.normals[mesh.vertexCount*3 - 1]));
        }

        if (mesh.tangents != null) {
            // Vertex tangents (XYZW - 4 components per vertex - float)
            txtData.append(String.format("static float %s_TANGENT_DATA[%d] = { ", varFileName, mesh.vertexCount*4));
            for (int i = 0; i < mesh.vertexCount*4 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "%.3ff,\n" : "%.3ff, "), mesh.tangents[i]));
            }
            txtData.append(String.format("%.3ff };\n\n", mesh.tangents[mesh.vertexCount*4 - 1]));
        }

        if (mesh.colors != null) {
            // Vertex colors (RGBA - 4 components per vertex - unsigned char)
            txtData.append(String.format("static unsigned char %s_COLOR_DATA[%d] = { ", varFileName, mesh.vertexCount*4));
            for (int i = 0; i < mesh.vertexCount*4 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "0x%x,\n" : "0x%x, "), mesh.colors[i]));
            }
            txtData.append(String.format("0x%x };\n\n", mesh.colors[mesh.vertexCount*4 - 1]));
        }

        if (mesh.indices != null) {
            // Vertex indices (3 index per triangle - unsigned short)
            txtData.append(String.format("static unsigned short %s_INDEX_DATA[%d] = { ", varFileName, mesh.triangleCount*3));
            for (int i = 0; i < mesh.triangleCount*3 - 1; i++) {
                txtData.append(String.format(((i%TEXT_BYTES_PER_LINE == 0)? "%d,\n" : "%d, "), mesh.indices[i]));
            }
            txtData.append(String.format("%d };\n", (int) mesh.indices[mesh.triangleCount*3 - 1]));
        }
        //-----------------------------------------------------------------------------------------

        // NOTE: Text data size exported is determined by '\0' (null) character
        try {
            result = context.files.SaveFileText(fileName, txtData.toString());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        // if (result) context.tracelog.TRACELOG(LOG_INFO, "FILEIO: [%s] Image as code exported successfully", fileName);
        // else context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: [%s] Failed to export image as code", fileName);

        return result;
    }

    /**
     * Process OBJ Materials
     * @param loader {@code OBJLoader} used to load the OBJ file
     * @param path Working path of the OBJ file
     * @return Array of {Material}s defined by the OBJ file
     * @see OBJLoader
     */
    private Material[] ProcessMaterialsOBJ(OBJLoader loader, String path) {
        Material[] materials = new Material[loader.objInfo.totalMaterials];
        context.tracelog.TRACELOG(LOG_INFO, path);
        // Init model materials
        for (int m = 0; m < loader.objInfo.totalMaterials; m++) {
            // Init material to default
            // NOTE: Uses default shader, which only supports MATERIAL_MAP_DIFFUSE
            materials[m] = LoadMaterialDefault();

            // Get default texture, in case no texture is defined
            // NOTE: rlgl default texture is a 1x1 pixel UNCOMPRESSED_R8G8B8A8
            materials[m].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = new Texture2D(context.rlgl.rlGetTextureIdDefault(), 1, 1, 1, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);

            if (loader.mtlInfo.materials[m].diffuse_texname != null) {
                materials[m].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = context.textures.LoadTexture(path + loader.mtlInfo.materials[m].diffuse_texname);  //char *diffuse_texname; // map_Kd
            }
            else {
                materials[m].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = new Color(
                        (int) (loader.mtlInfo.materials[m].diffuse[0] * 255.0f),
                        (int) (loader.mtlInfo.materials[m].diffuse[1] * 255.0f),
                        (int) (loader.mtlInfo.materials[m].diffuse[2] * 255.0f),
                        255
                );
            }
            materials[m].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].value = 0f;

            if (loader.mtlInfo.materials[m].specular_texname != null) {
                materials[m].maps[MATERIAL_MAP_SPECULAR.GetIndex()].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].specular_texname);  //char *specular_texname; // map_Ks
            }
            materials[m].maps[MATERIAL_MAP_SPECULAR.GetIndex()].color = new Color(
                    (int) (loader.mtlInfo.materials[m].specular[0] * 255.0f),
                    (int) (loader.mtlInfo.materials[m].specular[1] * 255.0f),
                    (int) (loader.mtlInfo.materials[m].specular[2] * 255.0f),
                    255
            );
            materials[m].maps[MATERIAL_MAP_SPECULAR.GetIndex()].value = 0.0f;

            if (loader.mtlInfo.materials[m].bump_texname != null) {
                materials[m].maps[MATERIAL_MAP_NORMAL.GetIndex()].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].bump_texname);  //char *bump_texname; // map_bump, bump
            }
            materials[m].maps[MATERIAL_MAP_NORMAL.GetIndex()].color = WHITE;
            materials[m].maps[MATERIAL_MAP_NORMAL.GetIndex()].value = loader.mtlInfo.materials[m].shininess;

            materials[m].maps[MATERIAL_MAP_EMISSION.GetIndex()].color = new Color(
                    (int) (loader.mtlInfo.materials[m].emission[0] * 255.0f),
                    (int) (loader.mtlInfo.materials[m].emission[1] * 255.0f),
                    (int) (loader.mtlInfo.materials[m].emission[2] * 255.0f),
                    255
            );

            if (loader.mtlInfo.materials[m].displacement_texname != null) {
                materials[m].maps[MATERIAL_MAP_HEIGHT.GetIndex()].texture = context.textures.LoadTexture(loader.mtlInfo.materials[m].displacement_texname);  //char *displacement_texname; // disp
            }

        }

        return materials;
    }

    /**
     * Load materials from model file
     *
     * @param fileName Material file to load
     * @return Array of {Material}s defined by the material file
     */
    public Material[] LoadMaterials(String fileName) {
        Material[] materials = null;
        int count = 0;

        // TODO: Support IQM and GLTF for materials parsing

        if (SUPPORT_FILEFORMAT_MTL) {
            if (context.files.IsFileExtension(fileName, ".mtl")) {
                OBJLoader loader = new OBJLoader();
                String fileText;
                try {
                    fileText = context.files.LoadFileText(fileName);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                boolean result = loader.ReadMTL(context, fileText);
                if (!result) {
                    context.tracelog.TRACELOG(LOG_WARNING, "MATERIAL: [" + fileName + "] Failed to parse materials file");
                }

                String path = fileName.substring(fileName.lastIndexOf("/") + 1);
                materials = ProcessMaterialsOBJ(loader, path);
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to load material file");
        }

        return materials;
    }

    /**
     * Load default material (Supports: DIFFUSE, SPECULAR, NORMAL maps)
     *
     * @return Default {@code Material}
     */
    public Material LoadMaterialDefault() {
        Material material = new Material();
        material.maps = new MaterialMap[MAX_MATERIAL_MAPS];
        for (int i = 0; i < material.maps.length; i++) {
            material.maps[i] = new MaterialMap();
        }

        // Using rlgl default shader
        material.shader.id = context.rlgl.rlGetShaderIdDefault();
        material.shader.locs = context.rlgl.rlGetShaderLocsDefault();

        // Using rlgl default texture (1x1 pixel, UNCOMPRESSED_R8G8B8A8, 1 mipmap)
        material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = new Texture2D(context.rlgl.rlGetTextureIdDefault(), 1, 1, 1, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
        //material.maps[MATERIAL_MAP_NORMAL].texture;         // NOTE: By default, not set
        //material.maps[MATERIAL_MAP_SPECULAR].texture;       // NOTE: By default, not set

        material.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = WHITE;    // Diffuse color
        material.maps[MATERIAL_MAP_SPECULAR.GetIndex()].color = WHITE;   // Specular color

        return material;
    }

    /**
     * Check if a material is valid (map textures loaded in GPU)
     *
     * @param material Material to evaluate
     * @return {@code true} if {@code material} is valid
     */
    public boolean IsMaterialValid(Material material) {
        boolean result = false;

        if ((material.maps != null) &&      // Validate material contain some map
                (material.shader.id > 0)) { // Validate material shader is valid
            result = true;
        }

        // NOTE: Checking if available maps contain loaded textures does not determine if
        // a material is valid, there can be maps without a texture assigned, only properties

        return result;
    }

    /**
     * Unload material from memory
     *
     * @param material {@code Material} to unload
     */
    @Contract(mutates = "param")
    public void UnloadMaterial(Material material) {
        // Unload material shader (avoid unloading default shader, managed by raylib)
        if (material.shader.id != context.rlgl.rlGetShaderIdDefault()) {
            context.core.UnloadShader(material.shader);
        }

        // Unload loaded texture maps (avoid unloading default texture, managed by raylib)
        if (material.maps != null) {
            for (int i = 0; i < MAX_MATERIAL_MAPS; i++) {
                if (material.maps[i].texture.id != context.rlgl.rlGetTextureIdDefault()) {
                    context.rlgl.rlUnloadTexture(material.maps[i].texture.id);
                }
            }
        }

        material.maps = null;
    }

    /**
     * Set texture for a material map type (MATERIAL_MAP_DIFFUSE, MATERIAL_MAP_SPECULAR...)
     *
     * @param material Material to map to
     * @param mapType Material Map type
     * @param texture Texture to map
     * @see MaterialMapIndex
     */
    @Contract(mutates = "param1")
    public void SetMaterialTexture(Material material, MaterialMapIndex mapType, Texture2D texture) {
        material.maps[mapType.GetIndex()].texture = texture;
    }

    /**
     * Set the material for a mesh
     *
     * @param model Model to modify the material of
     * @param meshId Mesh to modify
     * @param materialId Material to use for {@code meshId}
     */
    @Contract(mutates = "param1")
    public void SetModelMeshMaterial(Model model, int meshId, int materialId) {
        if (meshId >= model.meshCount) {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Id greater than mesh count");
        }
        else if (materialId >= model.materialCount) {
            context.tracelog.TRACELOG(LOG_WARNING, "MATERIAL: Id greater than material count");
        }
        else {
            model.meshMaterial[meshId] = materialId;
        }
    }

    /**
     * Load model animations from file
     * @param fileName File to load
     * @return Array of {ModelAnimations} defined by the animation file
     */
    public ModelAnimation[] LoadModelAnimations(String fileName) {
        ModelAnimation[] animations = null;

        if (SUPPORT_FILEFORMAT_IQM) {
            if (context.files.IsFileExtension(fileName, ".iqm")) {
                animations = LoadModelAnimationsIQM(fileName);
            }
        }
        if (SUPPORT_FILEFORMAT_M3D) {
            if (context.files.IsFileExtension(fileName, ".m3d")) {
                animations = LoadModelAnimationsM3D(fileName);
            }
        }
        if (SUPPORT_FILEFORMAT_GLTF) {
            if (context.files.IsFileExtension(fileName, ".gltf;.glb")) {
                animations = LoadModelAnimationsGLTF(fileName);
            }
        }

        return animations;
    }

    // Update model animated vertex data (positions and normals) for a given frame
    // NOTE: Updated data is uploaded to GPU

    /**
     * Update model animation data (vertex buffers / bone matrices) for a specific pose <br/>
     * NOTE 1: Request frame could be fractional, using a lerp interpolation between two frames <br/>
     * NOTE 2: Updated vertex animation data is uploaded to GPU in case of CPU skinning, for GPU skinning, bone matrices are uploaded to shader on DrawModelEx()
     *
     * @param model Model to update
     * @param anim Animation to use for transformation
     * @param frame Frame of the animation
     */
    public void UpdateModelAnimation(Model model, ModelAnimation anim, int frame) {
        if (model.boneMatrices == null) {
            return;
        }

        //UpdateModelAnimationEx(model, anim, frame, anim, frame, 0.0f);

        // Update model animated bones transform matrices for a given frame
        if ((anim.keyframeCount > 0) && (model.skeleton.bones != null) && (anim.keyframePoses != null)) {
            // Get frame and blending from frame factor required
            int currentFrame = (int)frame;
            int nextFrame = currentFrame + 1;
            float blend = frame - currentFrame;
            blend = Clamp(blend, 0.0f, 1.0f);
            if (currentFrame >= anim.keyframeCount) currentFrame = currentFrame%anim.keyframeCount;
            if (nextFrame >= anim.keyframeCount) nextFrame = nextFrame%anim.keyframeCount;

            Matrix bindPoseMatrix = new Matrix();
            Matrix currentPoseMatrix = new Matrix();

            // Update all bones and bone matrices of model
            for (int boneIndex = 0; boneIndex < model.skeleton.boneCount; boneIndex++) {
                // Compute interpolated pose between current and next frame
                // NOTE: Storing animation frame data in model.currentPose
                model.currentPose[boneIndex].translation = Vector3Lerp(
                        anim.keyframePoses[currentFrame][boneIndex].translation,
                        anim.keyframePoses[nextFrame][boneIndex].translation, blend);
                model.currentPose[boneIndex].rotation = QuaternionSlerp(
                        anim.keyframePoses[currentFrame][boneIndex].rotation,
                        anim.keyframePoses[nextFrame][boneIndex].rotation, blend);
                model.currentPose[boneIndex].scale = Vector3Lerp(
                        anim.keyframePoses[currentFrame][boneIndex].scale,
                        anim.keyframePoses[nextFrame][boneIndex].scale, blend);

                // Compute runtime bone matrix from model current pose
                //-----------------------------------------------------------------------------------
                Transform bindPoseTransform = model.skeleton.bindPose[boneIndex].clone();
                bindPoseMatrix = MatrixMultiply(
                        MatrixMultiply(MatrixScale(bindPoseTransform.scale.x, bindPoseTransform.scale.y, bindPoseTransform.scale.z),
                                       QuaternionToMatrix(bindPoseTransform.rotation)),
                        MatrixTranslate(bindPoseTransform.translation.x, bindPoseTransform.translation.y, bindPoseTransform.translation.z));

                Transform currentPoseTransform = model.currentPose[boneIndex].clone();
                currentPoseMatrix = MatrixMultiply(
                        MatrixMultiply(MatrixScale(currentPoseTransform.scale.x, currentPoseTransform.scale.y, currentPoseTransform.scale.z),
                                       QuaternionToMatrix(currentPoseTransform.rotation)),
                        MatrixTranslate(currentPoseTransform.translation.x, currentPoseTransform.translation.y, currentPoseTransform.translation.z));

                model.boneMatrices[boneIndex] = MatrixMultiply(MatrixInvert(bindPoseMatrix), currentPoseMatrix);
                //-----------------------------------------------------------------------------------
            }

            // CPU skinning, updates CPU buffers and uploads them to GPU
            // NOTE: On GPU skinning not supported, use CPU skinning
            UpdateModelAnimationVertexBuffers(model);
        }
    }

    /**
     * Update model animation data (vertex buffers / bone matrices) for a specific pose, defined by two different animations at specific frames blended together ,br/>
     * NOTE 1: Request frames could be fractional, using a lerp interpolation between two frames <br/>
     * NOTE 2: Updated vertex animation data is uploaded to GPU in case of CPU skinning, for GPU skinning, bone matrices are uploaded to shader on DrawModelEx()
     *
     * @param model
     * @param animA
     * @param frameA
     * @param animB
     * @param frameB
     * @param blend
     */
    public void UpdateModelAnimationEx(Model model, ModelAnimation animA, float frameA, ModelAnimation animB, float frameB, float blend) {
        if (model.boneMatrices == null) return;

        if ((animA.keyframeCount > 0) && (animA.keyframePoses != null) &&
                (animB.keyframeCount > 0) && (animB.keyframePoses != null) &&
                (blend >= 0.0f) && (blend <= 1.0f)) {
            // Inter-frame interpolation values for first animation
            int currentFrameA = (int)frameA%animA.keyframeCount;
            int nextFrameA = currentFrameA + 1;
            float blendA = frameA - currentFrameA;
            blendA = Clamp(blendA, 0.0f, 1.0f);
            if (currentFrameA >= animA.keyframeCount) currentFrameA = currentFrameA%animA.keyframeCount;
            if (nextFrameA >= animA.keyframeCount) nextFrameA = nextFrameA%animA.keyframeCount;

            // Inter-frame interpolation values for second animation
            int currentFrameB = (int)frameB%animB.keyframeCount;
            int nextFrameB = currentFrameB + 1;
            float blendB = frameB - currentFrameB;
            blendB = Clamp(blendB, 0.0f, 1.0f);
            if (currentFrameB >= animB.keyframeCount) currentFrameB = currentFrameB%animB.keyframeCount;
            if (nextFrameB >= animB.keyframeCount) nextFrameB = nextFrameB%animB.keyframeCount;

            Matrix bindPoseMatrix = new Matrix();
            Matrix currentPoseMatrix = new Matrix();

            for (int boneIndex = 0; boneIndex < model.skeleton.boneCount; boneIndex++) {
                // Get frame-interpolation for first animation
                Vector3 frameATranslation = Vector3Lerp(
                        animA.keyframePoses[currentFrameA][boneIndex].translation,
                        animA.keyframePoses[nextFrameA][boneIndex].translation, blendA);
                Quaternion frameARotation = QuaternionSlerp(
                        animA.keyframePoses[currentFrameA][boneIndex].rotation,
                        animA.keyframePoses[nextFrameA][boneIndex].rotation, blendA);
                Vector3 frameAScale = Vector3Lerp(
                        animA.keyframePoses[currentFrameA][boneIndex].scale,
                        animA.keyframePoses[nextFrameA][boneIndex].scale, blendA);

                // Get frame-interpolation for second animation
                Vector3 frameBTranslation = Vector3Lerp(
                        animB.keyframePoses[currentFrameB][boneIndex].translation,
                        animB.keyframePoses[nextFrameB][boneIndex].translation, blendB);
                Quaternion frameBRotation = QuaternionSlerp(
                        animB.keyframePoses[currentFrameB][boneIndex].rotation,
                        animB.keyframePoses[nextFrameB][boneIndex].rotation, blendB);
                Vector3 frameBScale = Vector3Lerp(
                        animB.keyframePoses[currentFrameB][boneIndex].scale,
                        animB.keyframePoses[nextFrameB][boneIndex].scale, blendB);

                // Compute interpolated pose between both animations frames
                // NOTE: Storing animation frame data in model.currentPose
                model.currentPose[boneIndex].translation = Vector3Lerp(frameATranslation, frameBTranslation, blend);
                model.currentPose[boneIndex].rotation = QuaternionSlerp(frameARotation, frameBRotation, blend);
                model.currentPose[boneIndex].scale = Vector3Lerp(frameAScale, frameBScale, blend);

                // Compute runtime bone matrix from model current pose
                //-----------------------------------------------------------------------------------
                Transform bindPoseTransform = model.skeleton.bindPose[boneIndex].clone();
                bindPoseMatrix = MatrixMultiply(
                        MatrixMultiply(MatrixScale(bindPoseTransform.scale.x, bindPoseTransform.scale.y, bindPoseTransform.scale.z),
                                       QuaternionToMatrix(bindPoseTransform.rotation)),
                        MatrixTranslate(bindPoseTransform.translation.x, bindPoseTransform.translation.y, bindPoseTransform.translation.z));

                Transform currentPoseTransform = model.currentPose[boneIndex].clone();
                currentPoseMatrix = MatrixMultiply(
                        MatrixMultiply(MatrixScale(currentPoseTransform.scale.x, currentPoseTransform.scale.y, currentPoseTransform.scale.z),
                                       QuaternionToMatrix(currentPoseTransform.rotation)),
                        MatrixTranslate(currentPoseTransform.translation.x, currentPoseTransform.translation.y, currentPoseTransform.translation.z));

                model.boneMatrices[boneIndex] = MatrixMultiply(MatrixInvert(bindPoseMatrix), currentPoseMatrix);
                //-----------------------------------------------------------------------------------

                /*
                Vector3 outATranslation = animA.keyframePoses[currentFrameA][boneIndex].translation;
                Quaternion outARotation = animA.keyframePoses[currentFrameA][boneIndex].rotation;
                Vector3 outAScale = animA.keyframePoses[currentFrameA][boneIndex].scale;
    
                Vector3 outBTranslation = animB.keyframePoses[currentFrameB][boneIndex].translation;
                Quaternion outBRotation = animB.keyframePoses[currentFrameB][boneIndex].rotation;
                Vector3 outBScale = animB.keyframePoses[currentFrameB][boneIndex].scale;
    
                // Invert bind pose transformation
                Vector3 invBindTranslation = Vector3RotateByQuaternion(
                    Vector3Negate(model.skeleton.bindPose[boneIndex].translation),
                    QuaternionInvert(model.skeleton.bindPose[boneIndex].rotation));
                Quaternion invBindRotation = QuaternionInvert(model.skeleton.bindPose[boneIndex].rotation);
                Vector3 invBindScale = Vector3Divide((Vector3){ 1.0f, 1.0f, 1.0f }, model.skeleton.bindPose[boneIndex].scale);
    
                Vector3 boneTranslation = Vector3Add(Vector3RotateByQuaternion(
                    Vector3Multiply(model.currentPose[boneIndex].scale, invBindTranslation),
                        model.currentPose[boneIndex].rotation),
                        model.currentPose[boneIndex].translation);
                Quaternion boneRotation = QuaternionMultiply(model.currentPose[boneIndex].rotation, invBindRotation);
                Vector3 boneScale = Vector3Multiply(model.currentPose[boneIndex].scale, invBindScale);
    
                model.boneMatrices[boneIndex] = MatrixMultiply(
                    MatrixMultiply(QuaternionToMatrix(boneRotation),
                        MatrixTranslate(boneTranslation.x, boneTranslation.y, boneTranslation.z)),
                    MatrixScale(boneScale.x, boneScale.y, boneScale.z));
                */
            }

            // CPU skinning, updates CPU buffers and uploads them to GPU (if available)
            // NOTE: Fallback in case GPU skinning is not supported or enabled
            UpdateModelAnimationVertexBuffers(model);
        }
    }

    /**
     * Update model vertex animation buffers (positions and normals) <br/>
     * NOTE: Required for CPU skinning, uploads animated vertex buffers to GPU
     * @param model
     */
    private void UpdateModelAnimationVertexBuffers(Model model) {
        for (int m = 0; m < model.meshCount; m++) {
            Mesh mesh = model.meshes[m];
            Vector3 animVertex;
            Vector3 animNormal;
            int vertexValuesCount = mesh.vertexCount*3;

            int boneIndex = 0;
            int boneCounter = 0;
            float boneWeight = 0.0f;
            boolean bufferUpdateRequired = false; // Flag to check when anim vertex information is updated

            // Skip if missing bone data or missing anim buffers initialization
            if ((mesh.boneWeights == null) || (mesh.boneIndices == null) || (mesh.animVertices == null) || (mesh.animNormals == null)) {
                continue;
            }

            for (int vCounter = 0; vCounter < vertexValuesCount; vCounter += 3) {
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
                    boneIndex = mesh.boneIndices[boneCounter];

                    // Early stop when no transformation will be applied
                    if (boneWeight == 0.0f) continue;
                    animVertex = new Vector3(mesh.vertices[vCounter], mesh.vertices[vCounter + 1], mesh.vertices[vCounter + 2]);
                    animVertex = Vector3Transform(animVertex, model.boneMatrices[boneIndex]);
                    mesh.animVertices[vCounter] += animVertex.x*boneWeight;
                    mesh.animVertices[vCounter + 1] += animVertex.y*boneWeight;
                    mesh.animVertices[vCounter + 2] += animVertex.z*boneWeight;
                    bufferUpdateRequired = true;

                    // Normals processing
                    // NOTE: Using meshes.baseNormals (default normal) to calculate meshes.normals (animated normals)
                    if ((mesh.normals != null) && (mesh.animNormals != null )) {
                        animNormal = new Vector3(mesh.normals[vCounter], mesh.normals[vCounter + 1], mesh.normals[vCounter + 2]);
                        animNormal = Vector3Transform(animNormal, MatrixTranspose(MatrixInvert(model.boneMatrices[boneIndex])));
                        mesh.animNormals[vCounter] += animNormal.x*boneWeight;
                        mesh.animNormals[vCounter + 1] += animNormal.y*boneWeight;
                        mesh.animNormals[vCounter + 2] += animNormal.z*boneWeight;
                    }
                }
            }

            if (bufferUpdateRequired) {
                // Update GPU vertex buffers with updated data (position + normals)
                context.rlgl.rlUpdateVertexBuffer(mesh.vboId[SHADER_LOC_VERTEX_POSITION.GetLocation()], mesh.animVertices, mesh.vertexCount*3*Float.BYTES);
                if (mesh.normals != null) {
                    context.rlgl.rlUpdateVertexBuffer(mesh.vboId[SHADER_LOC_VERTEX_NORMAL.GetLocation()], mesh.animNormals, mesh.vertexCount*3*Float.BYTES);
                }
            }
        }
    }

    /**
     * Unload animation array data
     * @param animations Animation data to unload
     */
    @Contract(mutates = "param")
    public void UnloadModelAnimations(ModelAnimation[] animations) {
        for (ModelAnimation animation : animations) {
            for (int i = 0; i < animation.keyframeCount; i++) {
                animation.keyframePoses[i] = null;
            }

            animation.keyframePoses = null;
        }
        animations = null;
    }

    /**
     * Check model animation skeleton match <br/>
     * NOTE: Only number of bones and parent connections are checked
     *
     * @param model
     * @param anim
     * @return
     */
    public boolean IsModelAnimationValid(Model model, ModelAnimation anim) {
        boolean result = true;

        if (model.skeleton.boneCount != anim.boneCount) {
            result = false;
        }

        return result;
    }

    // MESH GENERATION

    /**
     * Generate polygonal mesh
     *
     * @param sides Number of sides on the polygon
     * @param radius Distance from polygon center to the side
     * @return Mesh for a polygon defined by the parameters
     */
    public Mesh GenMeshPoly(int sides, float radius) {
        Mesh mesh = new Mesh();

        if (sides < 3) {
            return mesh; // Security check
        }

        int vertexCount = sides * 3;

        // Vertices definition
        Vector3[] vertices = new Vector3[vertexCount];

        float d = 0.0f, dStep = 360.0f / sides;
        for (int v = 0; v < vertexCount - 2; v += 3) {
            vertices[v] = new Vector3(0.0f, 0.0f, 0.0f);
            vertices[v + 1] = new Vector3((float) Math.sin(DEG2RAD * d) * radius, 0.0f, (float) Math.cos(DEG2RAD * d) * radius);
            vertices[v + 2] = new Vector3((float) Math.sin(DEG2RAD * (d + dStep)) * radius, 0.0f, (float) Math.cos(DEG2RAD * (d + dStep)) * radius);
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
            mesh.vertices[3 * i] = vertices[i].x;
            mesh.vertices[3 * i + 1] = vertices[i].y;
            mesh.vertices[3 * i + 2] = vertices[i].z;
        }

        // Mesh texcoords array
        for (int i = 0; i < mesh.vertexCount; i++) {
            mesh.texcoords[2 * i] = texcoords[i].x;
            mesh.texcoords[2 * i + 1] = texcoords[i].y;
        }

        // Mesh normals array
        for (int i = 0; i < mesh.vertexCount; i++) {
            mesh.normals[3 * i] = normals[i].x;
            mesh.normals[3 * i + 1] = normals[i].y;
            mesh.normals[3 * i + 2] = normals[i].z;
        }

        vertices = null;
        normals = null;
        texcoords = null;

        // Upload vertex data to GPU (static mesh)
        // NOTE: mesh.vboId array is allocated inside UploadMesh()
        UploadMesh(mesh, false);

        return mesh;
    }

    /**
     * Generate plane mesh (with subdivisions)
     *
     * @param width Width of the plane
     * @param length Length of the plane
     * @param resX Width of the plane subdivisions
     * @param resZ Length of the plane subdivisions
     * @return Mesh of a plane defined by the parameters
     */
    public Mesh GenMeshPlane(float width, float length, int resX, int resZ) {
        Mesh mesh = new Mesh();

        if (SUPPORT_CUSTOM_MESH_GEN_PLANE) {
            resX++;
            resZ++;

            // Vertices definition
            int vertexCount = resX * resZ; // vertices get reused for the faces

            Vector3[] vertices = new Vector3[vertexCount];
            for (int z = 0; z < resZ; z++) {
                // [-length/2, length/2]
                float zPos = ((float) z / (resZ - 1) - 0.5f) * length;
                for (int x = 0; x < resX; x++) {
                    // [-width/2, width/2]
                    float xPos = ((float) x / (resX - 1) - 0.5f) * width;
                    vertices[x + z * resX] = new Vector3(xPos, 0.0f, zPos);
                }
            }

            // Normals definition
            Vector3[] normals = new Vector3[vertexCount];
            for (int n = 0; n < vertexCount; n++) {
                normals[n] = new Vector3(0.0f, 1.0f, 0.0f);   // Vector3.up;
            }

            // TexCoords definition
            Vector2[] texcoords = new Vector2[vertexCount];
            for (int v = 0; v < resZ; v++) {
                for (int u = 0; u < resX; u++) {
                    texcoords[u + v * resX] = new Vector2((float) u / (resX - 1), (float) v / (resZ - 1));
                }
            }

            // Triangles definition (indices)
            int numFaces = (resX - 1) * (resZ - 1);
            int[] triangles = new int[numFaces * 6];
            int t = 0;
            for (int face = 0; face < numFaces; face++) {
                // Retrieve lower left corner from face ind
                int i = face + face / (resX - 1);

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
            for (int i = 0; i < mesh.vertexCount; i++) {
                mesh.vertices[3 * i] = vertices[i].x;
                mesh.vertices[3 * i + 1] = vertices[i].y;
                mesh.vertices[3 * i + 2] = vertices[i].z;
            }

            // Mesh texcoords array
            for (int i = 0; i < mesh.vertexCount; i++) {
                mesh.texcoords[2 * i] = texcoords[i].x;
                mesh.texcoords[2 * i + 1] = texcoords[i].y;
            }

            // Mesh normals array
            for (int i = 0; i < mesh.vertexCount; i++) {
                mesh.normals[3 * i] = normals[i].x;
                mesh.normals[3 * i + 1] = normals[i].y;
                mesh.normals[3 * i + 2] = normals[i].z;
            }

            // Mesh indices array initialization
            for (int i = 0; i < mesh.triangleCount * 3; i++) {
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

            FloatBuffer points = plane.points(plane.ntriangles() * 3 * 3);
            FloatBuffer normals = plane.normals(plane.ntriangles() * 3 * 3);
            FloatBuffer tcoords = plane.tcoords(plane.ntriangles() * 3 * 2);
            IntBuffer triangles = plane.triangles(plane.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(plane);
        }

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }

    /**
     * Generated cuboid mesh
     *
     * @param width Width of the cuboid
     * @param height Height of the cuboid
     * @param length Length of the cuboid
     * @return Cuboid mesh defined by the parameters
     */
    public Mesh GenMeshCube(float width, float height, float length) {
        Mesh mesh = new Mesh();

        if (SUPPORT_CUSTOM_MESH_GEN_CUBE) {
            mesh.vertices = new float[]{
                    -width / 2, -height / 2, length / 2,
                    width / 2, -height / 2, length / 2,
                    width / 2, height / 2, length / 2,
                    -width / 2, height / 2, length / 2,
                    -width / 2, -height / 2, -length / 2,
                    -width / 2, height / 2, -length / 2,
                    width / 2, height / 2, -length / 2,
                    width / 2, -height / 2, -length / 2,
                    -width / 2, height / 2, -length / 2,
                    -width / 2, height / 2, length / 2,
                    width / 2, height / 2, length / 2,
                    width / 2, height / 2, -length / 2,
                    -width / 2, -height / 2, -length / 2,
                    width / 2, -height / 2, -length / 2,
                    width / 2, -height / 2, length / 2,
                    -width / 2, -height / 2, length / 2,
                    width / 2, -height / 2, -length / 2,
                    width / 2, height / 2, -length / 2,
                    width / 2, height / 2, length / 2,
                    width / 2, -height / 2, length / 2,
                    -width / 2, -height / 2, -length / 2,
                    -width / 2, -height / 2, length / 2,
                    -width / 2, height / 2, length / 2,
                    -width / 2, height / 2, -length / 2
            };

            mesh.texcoords = new float[]{
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

            mesh.normals = new float[]{
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
            for (int i = 0, k = 0; i < 36; i += 6) {
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
            ParShapes.par_shapes_translate(cube, -width / 2, 0.0f, -length / 2);
            ParShapes.par_shapes_compute_normals(cube);

            mesh.vertices = new float[cube.ntriangles() * 3 * 3];
            mesh.texcoords = new float[cube.ntriangles() * 3 * 2];
            mesh.normals = new float[cube.ntriangles() * 3 * 3];

            mesh.vertexCount = cube.ntriangles() * 3;
            mesh.triangleCount = cube.ntriangles();

            FloatBuffer points = cube.points(cube.ntriangles() * 3 * 3);
            FloatBuffer normals = cube.normals(cube.ntriangles() * 3 * 3);
            FloatBuffer tcoords = FloatBuffer.allocate(cube.npoints() * 2);
            for (int i = 0; i < tcoords.limit(); i++) {
                tcoords.put(0.0f);
            }
            IntBuffer triangles = cube.triangles(cube.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(cube);
        }

        // Upload vertex data to GPU (static mesh)
        UploadMesh(mesh, false);

        return mesh;
    }

    /**
     * Generate sphere mesh (standard sphere)
     *
     * @param radius Radius of the sphere
     * @param rings Horizontal divisions of the sphere (Parallels)
     * @param slices Vertical divisions of the sphere (Meridians)
     * @return Sphere mesh defined by the parameters
     */
    public Mesh GenMeshSphere(float radius, int rings, int slices) {
        Mesh mesh = new Mesh();

        if ((rings >= 3) && (slices >= 3)) {
            ParShapes.par_shapes_set_epsilon_degenerate_sphere(0.0f);
            ParShapesMesh sphere = ParShapes.par_shapes_create_parametric_sphere(slices, rings);
            ParShapes.par_shapes_scale(sphere, radius, radius, radius);
            // NOTE: Soft normals are computed internally

            mesh.vertices = new float[sphere.ntriangles() * 3 * 3];
            mesh.texcoords = new float[sphere.ntriangles() * 3 * 2];
            mesh.normals = new float[sphere.ntriangles() * 3 * 3];

            mesh.vertexCount = sphere.ntriangles() * 3;
            mesh.triangleCount = sphere.ntriangles();

            FloatBuffer points = sphere.points(sphere.ntriangles() * 3 * 3);
            FloatBuffer normals = sphere.normals(sphere.ntriangles() * 3 * 3);
            FloatBuffer tcoords = sphere.tcoords(sphere.ntriangles() * 3 * 2);
            IntBuffer triangles = sphere.triangles(sphere.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(sphere);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to generate mesh: sphere");
        }

        return mesh;
    }

    /**
     * Generate hemi-sphere mesh (half sphere, no bottom cap)
     *
     * @param radius Radius of the sphere
     * @param rings Horizontal divisions of the sphere (Parallels)
     * @param slices Vertical divisions of the sphere (Meridians)
     * @return Hemisphere mesh defined by the parameters
     */
    public Mesh GenMeshHemiSphere(float radius, int rings, int slices) {
        Mesh mesh = new Mesh();

        if ((rings >= 3) && (slices >= 3)) {
            if (radius < 0.0f) {
                radius = 0.0f;
            }

            ParShapesMesh sphere = ParShapes.par_shapes_create_hemisphere(slices, rings);
            ParShapes.par_shapes_scale(sphere, radius, radius, radius);
            // NOTE: Soft normals are computed internally

            mesh.vertices = new float[sphere.ntriangles() * 3 * 3];
            mesh.texcoords = new float[sphere.ntriangles() * 3 * 2];
            mesh.normals = new float[sphere.ntriangles() * 3 * 3];

            mesh.vertexCount = sphere.ntriangles() * 3;
            mesh.triangleCount = sphere.ntriangles();

            FloatBuffer points = sphere.points(sphere.ntriangles() * 3 * 3);
            FloatBuffer normals = sphere.normals(sphere.ntriangles() * 3 * 3);
            FloatBuffer tcoords = sphere.tcoords(sphere.ntriangles() * 3 * 2);
            IntBuffer triangles = sphere.triangles(sphere.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(sphere);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to generate mesh: hemisphere");
        }

        return mesh;
    }

    /**
     * Generate cylinder mesh
     *
     * @param radius Distance from the center of the cylinder to the wall
     * @param height Height of the cylinder
     * @param slices Vertical divisions of the cylinder (Meridians)
     * @return Hemisphere mesh defined by the parameters
     */
    public Mesh GenMeshCylinder(float radius, float height, int slices) {
        Mesh mesh = new Mesh();

        if (slices >= 3) {
            // Instance a cylinder that sits on the Z=0 plane using the given tessellation
            // levels across the UV domain.  Think of "slices" like a number of pizza
            // slices, and "stacks" like a number of stacked rings
            // Height and radius are both 1.0, but they can easily be changed with par_shapes_scale
            ParShapesMesh cylinder = ParShapes.par_shapes_create_cylinder(slices, 8);
            ParShapes.par_shapes_scale(cylinder, radius, radius, height);
            ParShapes.par_shapes_rotate(cylinder, -PI / 2.0f, new float[]{1, 0, 0});

            // Generate an orientable disk shape (top cap)
            ParShapesMesh capTop = ParShapes.par_shapes_create_disk(radius, slices, new float[]{0, 0, 0}, new float[]{0, 0, 1});
            // TODO: 7/19/22 tcoords?
            ParShapes.par_shapes_rotate(capTop, -PI / 2.0f, new float[]{1, 0, 0});
            ParShapes.par_shapes_rotate(capTop, 90 * DEG2RAD, new float[]{0, 1, 0});
            ParShapes.par_shapes_translate(capTop, 0, height, 0);

            // Generate an orientable disk shape (bottom cap)
            ParShapesMesh capBottom = ParShapes.par_shapes_create_disk(radius, slices, new float[]{0, 0, 0}, new float[]{0, 0, -1});
            // TODO: 7/19/22 tcoords?
            ParShapes.par_shapes_rotate(capBottom, PI / 2.0f, new float[]{1, 0, 0});
            ParShapes.par_shapes_rotate(capBottom, -90 * DEG2RAD, new float[]{0, 1, 0});

            ParShapes.par_shapes_merge_and_free(cylinder, capTop);
            ParShapes.par_shapes_merge_and_free(cylinder, capBottom);

            mesh.vertices = new float[cylinder.ntriangles() * 3 * 3];
            mesh.texcoords = new float[cylinder.ntriangles() * 3 * 2];
            mesh.normals = new float[cylinder.ntriangles() * 3 * 3];

            mesh.vertexCount = cylinder.ntriangles() * 3;
            mesh.triangleCount = cylinder.ntriangles();

            FloatBuffer points = cylinder.points(cylinder.ntriangles() * 3 * 3);
            FloatBuffer normals = cylinder.normals(cylinder.ntriangles() * 3 * 3);
            FloatBuffer tcoords = cylinder.tcoords(cylinder.ntriangles() * 3 * 2);
            IntBuffer triangles = cylinder.triangles(cylinder.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(cylinder);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to generate mesh: cylinder");
        }

        return mesh;
    }

    /**
     * Generate cone/pyramid mesh
     *
     * @param radius Radius of the bottom face
     * @param height Height of the cone
     * @param slices Vertical divisions of the cylinder (Meridians)
     * @return conical mesh defined by the parameters
     */
    public Mesh GenMeshCone(float radius, float height, int slices) {
        Mesh mesh = new Mesh();

        if (slices >= 3) {
            // Instance a cone that sits on the Z=0 plane using the given tessellation
            // levels across the UV domain.  Think of "slices" like a number of pizza
            // slices, and "stacks" like a number of stacked rings
            // Height and radius are both 1.0, but they can easily be changed with par_shapes_scale
            ParShapesMesh cone = ParShapes.par_shapes_create_cone(slices, 8);
            ParShapes.par_shapes_scale(cone, radius, radius, height);
            ParShapes.par_shapes_rotate(cone, -PI / 2.0f, new float[]{1, 0, 0});
            ParShapes.par_shapes_rotate(cone, PI / 2.0f, new float[]{0, 1, 0});

            // Generate an orientable disk shape (bottom cap)
            ParShapesMesh capBottom = ParShapes.par_shapes_create_disk(radius, slices, new float[]{0, 0, 0}, new float[]{0, 0, -1});
            ParShapes.par_shapes_rotate(capBottom, PI / 2.0f, new float[]{1, 0, 0});

            ParShapes.par_shapes_merge_and_free(cone, capBottom);

            mesh.vertices = new float[cone.ntriangles() * 3 * 3];
            mesh.texcoords = new float[cone.ntriangles() * 3 * 2];
            mesh.normals = new float[cone.ntriangles() * 3 * 3];

            mesh.vertexCount = cone.ntriangles() * 3;
            mesh.triangleCount = cone.ntriangles();

            FloatBuffer points = cone.points(cone.ntriangles() * 3 * 3);
            FloatBuffer normals = cone.normals(cone.ntriangles() * 3 * 3);
            FloatBuffer tcoords = cone.tcoords(cone.ntriangles() * 3 * 2);
            IntBuffer triangles = cone.triangles(cone.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(cone);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to generate mesh: cone");
        }

        return mesh;
    }

    /**
     * Generate torus mesh
     *
     * @param radius Radius of the torus
     * @param size Thickness of the torus
     * @param radSeg Radial segments
     * @param sides Vertical divisions of the torus (Meridians)
     * @return Torus mesh defined by the parameters
     */
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
            ParShapes.par_shapes_scale(torus, size / 2, size / 2, size / 2);

            mesh.vertices = new float[torus.ntriangles() * 3 * 3];
            mesh.texcoords = new float[torus.ntriangles() * 3 * 2];
            mesh.normals = new float[torus.ntriangles() * 3 * 3];

            mesh.vertexCount = torus.ntriangles() * 3;
            mesh.triangleCount = torus.ntriangles();

            FloatBuffer points = torus.points(torus.ntriangles() * 3 * 3);
            FloatBuffer normals = torus.normals(torus.ntriangles() * 3 * 3);
            FloatBuffer tcoords = torus.tcoords(torus.ntriangles() * 3 * 2);
            IntBuffer triangles = torus.triangles(torus.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(torus);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to generate mesh: torus");
        }

        return mesh;
    }

    /**
     * Generate trefoil knot mesh
     *
     * @param radius Radius of the knot
     * @param size Thickness of the knot
     * @param radSeg Radial segments
     * @param sides Vertical divisions of the knot (Meridians)
     * @return Trefoil knot mesh defined by the parameters
     */
    public Mesh GenMeshKnot(float radius, float size, int radSeg, int sides) {
        Mesh mesh = new Mesh();

        if ((sides >= 3) && (radSeg >= 3)) {
            if (radius > 3.0f) {
                radius = 3.0f;
            }
            else if (radius < 0.5f) {
                radius = 0.5f;
            }

            ParShapesMesh knot = ParShapes.par_shapes_create_trefoil_knot(radSeg, sides, radius);
            ParShapes.par_shapes_scale(knot, size, size, size);

            mesh.vertices = new float[knot.ntriangles() * 3 * 3];
            mesh.texcoords = new float[knot.ntriangles() * 3 * 2];
            mesh.normals = new float[knot.ntriangles() * 3 * 3];

            mesh.vertexCount = knot.ntriangles() * 3;
            mesh.triangleCount = knot.ntriangles();

            FloatBuffer points = knot.points(knot.ntriangles() * 3 * 3);
            FloatBuffer normals = knot.normals(knot.ntriangles() * 3 * 3);
            FloatBuffer tcoords = knot.tcoords(knot.ntriangles() * 3 * 2);
            IntBuffer triangles = knot.triangles(knot.ntriangles() * 3 * 3);

            for (int k = 0; k < mesh.vertexCount; k++) {
                mesh.vertices[k * 3] = points.get(triangles.get(k) * 3);
                mesh.vertices[k * 3 + 1] = points.get(triangles.get(k) * 3 + 1);
                mesh.vertices[k * 3 + 2] = points.get(triangles.get(k) * 3 + 2);

                mesh.normals[k * 3] = normals.get(triangles.get(k) * 3);
                mesh.normals[k * 3 + 1] = normals.get(triangles.get(k) * 3 + 1);
                mesh.normals[k * 3 + 2] = normals.get(triangles.get(k) * 3 + 2);

                mesh.texcoords[k * 2] = tcoords.get(triangles.get(k) * 2);
                mesh.texcoords[k * 2 + 1] = tcoords.get(triangles.get(k) * 2 + 1);
            }

            ParShapes.par_shapes_free_mesh(knot);

            // Upload vertex data to GPU (static mesh)
            UploadMesh(mesh, false);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to generate mesh: knot");
        }

        return mesh;
    }

    /**
     * Generate a mesh from heightmap <br/>
     * NOTE: Vertex data is uploaded to GPU
     *
     * @param heightmap {@code Image} containing heightmap data
     * @param size {@code Vector3} containing the desired (Width, Height, Depth) of the mesh
     * @return Heightmap defined by the image and size
     */
    public Mesh GenMeshHeightmap(Image heightmap, Vector3 size) {
        Mesh mesh = new Mesh();

        int mapX = heightmap.width;
        int mapZ = heightmap.height;

        Color[] pixels = Color.FromPixels(context.textures.LoadImageColors(heightmap));

        // NOTE: One vertex per pixel
        mesh.triangleCount = (mapX - 1) * (mapZ - 1) * 2;    // One quad every four pixels

        mesh.vertexCount = mesh.triangleCount * 3;

        mesh.vertices = new float[mesh.vertexCount * 3];
        mesh.normals = new float[mesh.vertexCount * 3];
        mesh.texcoords = new float[mesh.vertexCount * 2];
        mesh.colors = null;

        int vCounter = 0;       // Used to count vertices float by float
        int tcCounter = 0;      // Used to count texcoords float by float
        int nCounter = 0;       // Used to count normals float by float

        Vector3 scaleFactor = new Vector3(size.x / (mapX - 1), size.y / 255.0f, size.z / (mapZ - 1));

        Vector3 vA = new Vector3();
        Vector3 vB = new Vector3();
        Vector3 vC = new Vector3();
        Vector3 vN = new Vector3();

        for (int z = 0; z < mapZ - 1; z++) {
            for (int x = 0; x < mapX - 1; x++) {
                // Fill vertices array with data
                //----------------------------------------------------------

                // One triangle - 3 vertex
                mesh.vertices[vCounter] = (float) x * scaleFactor.x;
                mesh.vertices[vCounter + 1] = GRAY_VALUE(pixels[x + z * mapX]) * scaleFactor.y;
                mesh.vertices[vCounter + 2] = (float) z * scaleFactor.z;

                mesh.vertices[vCounter + 3] = (float) x * scaleFactor.x;
                mesh.vertices[vCounter + 4] = GRAY_VALUE(pixels[x + (z + 1) * mapX]) * scaleFactor.y;
                mesh.vertices[vCounter + 5] = (float) (z + 1) * scaleFactor.z;

                mesh.vertices[vCounter + 6] = (float) (x + 1) * scaleFactor.x;
                mesh.vertices[vCounter + 7] = GRAY_VALUE(pixels[(x + 1) + z * mapX]) * scaleFactor.y;
                mesh.vertices[vCounter + 8] = (float) z * scaleFactor.z;

                // Another triangle - 3 vertex
                mesh.vertices[vCounter + 9] = mesh.vertices[vCounter + 6];
                mesh.vertices[vCounter + 10] = mesh.vertices[vCounter + 7];
                mesh.vertices[vCounter + 11] = mesh.vertices[vCounter + 8];

                mesh.vertices[vCounter + 12] = mesh.vertices[vCounter + 3];
                mesh.vertices[vCounter + 13] = mesh.vertices[vCounter + 4];
                mesh.vertices[vCounter + 14] = mesh.vertices[vCounter + 5];

                mesh.vertices[vCounter + 15] = (float) (x + 1) * scaleFactor.x;
                mesh.vertices[vCounter + 16] = GRAY_VALUE(pixels[(x + 1) + (z + 1) * mapX]) * scaleFactor.y;
                mesh.vertices[vCounter + 17] = (float) (z + 1) * scaleFactor.z;
                vCounter += 18;     // 6 vertex, 18 floats

                // Fill texcoords array with data
                //--------------------------------------------------------------
                mesh.texcoords[tcCounter] = (float) x / (mapX - 1);
                mesh.texcoords[tcCounter + 1] = (float) z / (mapZ - 1);

                mesh.texcoords[tcCounter + 2] = (float) x / (mapX - 1);
                mesh.texcoords[tcCounter + 3] = (float) (z + 1) / (mapZ - 1);

                mesh.texcoords[tcCounter + 4] = (float) (x + 1) / (mapX - 1);
                mesh.texcoords[tcCounter + 5] = (float) z / (mapZ - 1);

                mesh.texcoords[tcCounter + 6] = mesh.texcoords[tcCounter + 4];
                mesh.texcoords[tcCounter + 7] = mesh.texcoords[tcCounter + 5];

                mesh.texcoords[tcCounter + 8] = mesh.texcoords[tcCounter + 2];
                mesh.texcoords[tcCounter + 9] = mesh.texcoords[tcCounter + 3];

                mesh.texcoords[tcCounter + 10] = (float) (x + 1) / (mapX - 1);
                mesh.texcoords[tcCounter + 11] = (float) (z + 1) / (mapZ - 1);
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

    /**
     * Generate a cube mesh from pixel data <br/>
     * NOTE: Vertex data is uploaded to the GPU
     * @param cubicmap
     * @param cubeSize
     * @return
     */
    public Mesh GenMeshCubicmap(Image cubicmap, Vector3 cubeSize) {
        Mesh mesh = new Mesh();

        Color[] pixels = Color.FromPixels(context.textures.LoadImageColors(cubicmap));

        // NOTE: Max possible number of triangles numCubes*(12 triangles by cube)
        int maxTriangles = cubicmap.width * cubicmap.height * 12;

        int vCounter = 0;       // Used to count vertices
        int tcCounter = 0;      // Used to count texcoords
        int nCounter = 0;       // Used to count normals

        float w = cubeSize.x;
        float h = cubeSize.z;
        float h2 = cubeSize.y;

        Vector3[] mapVertices = new Vector3[maxTriangles * 3];
        Vector2[] mapTexcoords = new Vector2[maxTriangles * 3];
        Vector3[] mapNormals = new Vector3[maxTriangles * 3];

        // Define the 6 normals of the cube, combined accoridingly later
        Vector3 n1 = new Vector3(1.0f, 0.0f, 0.0f);
        Vector3 n2 = new Vector3(-1.0f, 0.0f, 0.0f);
        Vector3 n3 = new Vector3(0.0f, 1.0f, 0.0f);
        Vector3 n4 = new Vector3(0.0f, -1.0f, 0.0f);
        Vector3 n5 = new Vector3(0.0f, 0.0f, -1.0f);
        Vector3 n6 = new Vector3(0.0f, 0.0f, 1.0f);

        // NOTE: Using texture rectangles to define different
        // textures for top-bottom-front-back-right-left (6)

        Rectangle rightTexUV = new Rectangle(0.0f, 0.0f, 0.5f, 0.5f);
        Rectangle leftTexUV = new Rectangle(0.5f, 0.0f, 0.5f, 0.5f);
        Rectangle frontTexUV = new Rectangle(0.0f, 0.0f, 0.5f, 0.5f);
        Rectangle backTexUV = new Rectangle(0.5f, 0.0f, 0.5f, 0.5f);
        Rectangle topTexUV = new Rectangle(0.0f, 0.5f, 0.5f, 0.5f);
        Rectangle bottomTexUV = new Rectangle(0.5f, 0.5f, 0.5f, 0.5f);

        for (int z = 0; z < cubicmap.height; ++z) {
            for (int x = 0; x < cubicmap.width; x++) {
                // Define the 8 vertex of the cube, to be combined accordingly later...
                Vector3 v1 = new Vector3(w * (x - 0.5f), h2, h * (z - 0.5f));
                Vector3 v2 = new Vector3(w * (x - 0.5f), h2, h * (z + 0.5f));
                Vector3 v3 = new Vector3(w * (x + 0.5f), h2, h * (z + 0.5f));
                Vector3 v4 = new Vector3(w * (x + 0.5f), h2, h * (z - 0.5f));
                Vector3 v5 = new Vector3(w * (x + 0.5f), 0, h * (z - 0.5f));
                Vector3 v6 = new Vector3(w * (x - 0.5f), 0, h * (z - 0.5f));
                Vector3 v7 = new Vector3(w * (x - 0.5f), 0, h * (z + 0.5f));
                Vector3 v8 = new Vector3(w * (x + 0.5f), 0, h * (z + 0.5f));

                // Check pixel color to be WHITE -> draw full cube
                if (COLOR_EQUAL(pixels[z * cubicmap.width + x], WHITE)) {
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
                    if (((z < cubicmap.height - 1) && COLOR_EQUAL(pixels[(z + 1) * cubicmap.width + x], BLACK)) || (z == cubicmap.height - 1)) {
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
                    if (((z > 0) && COLOR_EQUAL(pixels[(z - 1) * cubicmap.width + x], BLACK)) || (z == 0)) {
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
                    if (((x < cubicmap.width - 1) && COLOR_EQUAL(pixels[z * cubicmap.width + (x + 1)], BLACK)) || (x == cubicmap.width - 1)) {
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
                    if (((x > 0) && COLOR_EQUAL(pixels[z * cubicmap.width + (x - 1)], BLACK)) || (x == 0)) {
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
                // Check pixel color to be BLACK, in that case only draw floor and roof
                else if (COLOR_EQUAL(pixels[z * cubicmap.width + x], BLACK)) {
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
        mesh.triangleCount = vCounter / 3;

        mesh.vertices = new float[mesh.vertexCount * 3];
        mesh.normals = new float[mesh.vertexCount * 3];
        mesh.texcoords = new float[mesh.vertexCount * 2];
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

    /**
     * Compute mesh bounding box limits
     * NOTE: resulting {@code BoundingBox} should be transformed by the model's transformation matrix
     *
     * @param mesh Mesh to evaluate
     * @return {@code BoudingBox} defined by mesh
     */
    public BoundingBox GetMeshBoundingBox(Mesh mesh) {
        // Get min and max vertex to construct bounds (AABB)
        Vector3 minVertex = new Vector3();
        Vector3 maxVertex = new Vector3();

        if (mesh.vertices != null) {
            minVertex = new Vector3(mesh.vertices[0], mesh.vertices[1], mesh.vertices[2]);
            maxVertex = new Vector3(mesh.vertices[0], mesh.vertices[1], mesh.vertices[2]);

            for (int i = 1; i < mesh.vertexCount; i++) {
                minVertex = Vector3Min(minVertex, new Vector3(mesh.vertices[i * 3], mesh.vertices[i * 3 + 1], mesh.vertices[i * 3 + 2]));
                maxVertex = Vector3Max(maxVertex, new Vector3(mesh.vertices[i * 3], mesh.vertices[i * 3 + 1], mesh.vertices[i * 3 + 2]));
            }
        }

        // Create the bounding box
        return new BoundingBox(minVertex, maxVertex);
    }

    /**
     * Compute mesh tangents
     * NOTE: To calculate mesh tangents and binormals, we need mesh vertex positions and texture coordinates
     * @param mesh
     * @link <a href="https://answers.unity.com/questions/7789/calculating-tangents-vector4.html">Implementation based on</a>
     */
    @Contract(mutates = "param")
    public void GenMeshTangents(Mesh mesh) {
        // Check if input mesh data is useful
        if ((mesh == null) || (mesh.vertices == null) || (mesh.texcoords == null) || (mesh.normals == null)) {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Tangents generation requires vertices, texcoords and normals vertex attribute data");
            return;
        }

        // Allocate or reallocate tangents data
        if (mesh.tangents == null) {
            mesh.tangents = new float[mesh.vertexCount*4];
        }
        else {
            mesh.tangents = null;
            mesh.tangents = new float[mesh.vertexCount*4];
        }

        // Allocate temporary arrays for tangents calculation
        Vector3[] tan1 = new Vector3[mesh.vertexCount];
        for (int i = 0; i < mesh.vertexCount; i++) {
            tan1[i] = new Vector3();
        }
        Vector3[] tan2 = new Vector3[mesh.vertexCount];
        for (int i = 0; i < mesh.vertexCount; i++) {
            tan2[i] = new Vector3();
        }

        if (tan1 == null || tan2 == null) {
            context.tracelog.TRACELOG(LOG_WARNING, "MESH: Failed to allocate temporary memory for tangent calculation");
            return;
        }

        // Process all triangles of the mesh
        // 'triangleCount' must be always valid
        for (int t = 0; t < mesh.triangleCount; t++) {
            // Get triangle vertex indices
            int i0 = 0, i1 = 0, i2 = 0;

            if (mesh.indices != null) {
                // Use indices if available
                i0 = (int) mesh.indices[t*3 + 0];
                i1 = (int) mesh.indices[t*3 + 1];
                i2 = (int) mesh.indices[t*3 + 2];
            }
            else {
                // Sequential access for non-indexed mesh
                i0 = t*3 + 0;
                i1 = t*3 + 1;
                i2 = t*3 + 2;
            }

            // Get triangle vertices position
            Vector3 v1 = new Vector3(mesh.vertices[i0*3 + 0], mesh.vertices[i0*3 + 1], mesh.vertices[i0*3 + 2]);
            Vector3 v2 = new Vector3(mesh.vertices[i1*3 + 0], mesh.vertices[i1*3 + 1], mesh.vertices[i1*3 + 2]);
            Vector3 v3 = new Vector3(mesh.vertices[i2*3 + 0], mesh.vertices[i2*3 + 1], mesh.vertices[i2*3 + 2]);

            // Get triangle texcoords
            Vector2 uv1 = new Vector2(mesh.texcoords[i0*2 + 0], mesh.texcoords[i0*2 + 1]);
            Vector2 uv2 = new Vector2(mesh.texcoords[i1*2 + 0], mesh.texcoords[i1*2 + 1]);
            Vector2 uv3 = new Vector2(mesh.texcoords[i2*2 + 0], mesh.texcoords[i2*2 + 1]);

            // Calculate triangle edges
            float x1 = v2.x - v1.x;
            float y1 = v2.y - v1.y;
            float z1 = v2.z - v1.z;
            float x2 = v3.x - v1.x;
            float y2 = v3.y - v1.y;
            float z2 = v3.z - v1.z;

            // Calculate texture coordinate differences
            float s1 = uv2.x - uv1.x;
            float t1 = uv2.y - uv1.y;
            float s2 = uv3.x - uv1.x;
            float t2 = uv3.y - uv1.y;

            // Calculate denominator and check for degenerate UV
            float div = s1*t2 - s2*t1;
            float r = (Math.abs(div) < 0.0001f)? 0.0f : 1.0f/div;

            // Calculate tangent and bitangent directions
            Vector3 sdir = new Vector3((t2*x1 - t1*x2)*r, (t2*y1 - t1*y2)*r, (t2*z1 - t1*z2)*r);
            Vector3 tdir = new Vector3((s1*x2 - s2*x1)*r, (s1*y2 - s2*y1)*r, (s1*z2 - s2*z1)*r);

            // Accumulate tangents and bitangents for each vertex of the triangle
            tan1[i0] = Vector3Add(tan1[i0], sdir);
            tan1[i1] = Vector3Add(tan1[i1], sdir);
            tan1[i2] = Vector3Add(tan1[i2], sdir);

            tan2[i0] = Vector3Add(tan2[i0], tdir);
            tan2[i1] = Vector3Add(tan2[i1], tdir);
            tan2[i2] = Vector3Add(tan2[i2], tdir);
        }

        // Calculate final tangents for each vertex
        for (int i = 0; i < mesh.vertexCount; i++) {
            Vector3 normal = new Vector3(mesh.normals[i*3 + 0], mesh.normals[i*3 + 1], mesh.normals[i*3 + 2]);
            Vector3 tangent = tan1[i];

            // Handle zero tangent (can happen with degenerate UVs)
            if (Vector3Length(tangent) < 0.0001f) {
                // Create a tangent perpendicular to the normal
                if (Math.abs(normal.z) > 0.707f) tangent = new Vector3(1.0f, 0.0f, 0.0f);
                else tangent = Vector3Normalize(new Vector3(-normal.y, normal.x, 0.0f ));

                mesh.tangents[i*4 + 0] = tangent.x;
                mesh.tangents[i*4 + 1] = tangent.y;
                mesh.tangents[i*4 + 2] = tangent.z;
                mesh.tangents[i*4 + 3] = 1.0f;
                continue;
            }

            // Gram-Schmidt orthogonalization to make tangent orthogonal to normal
            // T_prime = T - N*dot(N, T)
            Vector3 orthogonalized = Vector3Subtract(tangent, Vector3Scale(normal, Vector3DotProduct(normal, tangent)));

            // Handle cases where orthogonalized vector is too small
            if (Vector3Length(orthogonalized) < 0.0001f) {
                // Create a tangent perpendicular to the normal
                if (Math.abs(normal.z) > 0.707f) orthogonalized = new Vector3(1.0f, 0.0f, 0.0f);
                else orthogonalized = Vector3Normalize(new Vector3(-normal.y, normal.x, 0.0f));
            }
            else {
                // Normalize the orthogonalized tangent
                orthogonalized = Vector3Normalize(orthogonalized);
            }

            // Store the calculated tangent
            mesh.tangents[i*4 + 0] = orthogonalized.x;
            mesh.tangents[i*4 + 1] = orthogonalized.y;
            mesh.tangents[i*4 + 2] = orthogonalized.z;

            // Calculate the handedness (w component)
            mesh.tangents[i*4 + 3] = (Vector3DotProduct(Vector3CrossProduct(normal, orthogonalized), tan2[i]) < 0.0f)? -1.0f : 1.0f;
        }

        // Free temporary arrays
        tan1 = null;
        tan2 = null;

        // Update vertex buffers if available
        if (mesh.vboId != null) {
            if (mesh.vboId[SHADER_LOC_VERTEX_TANGENT.GetLocation()] != 0) {
                // Update existing tangent vertex buffer
                context.rlgl.rlUpdateVertexBuffer(mesh.vboId[SHADER_LOC_VERTEX_TANGENT.GetLocation()], mesh.tangents, mesh.vertexCount*4*Float.BYTES);
            }
            else {
                // Create new tangent vertex buffer
                mesh.vboId[SHADER_LOC_VERTEX_TANGENT.GetLocation()] = context.rlgl.rlLoadVertexBuffer(mesh.tangents, false);
            }

            // Set up vertex attributes for shader
            context.rlgl.rlEnableVertexArray(mesh.vaoId);
            context.rlgl.rlSetVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT, 4, RL_FLOAT, false, 0, 0);
            context.rlgl.rlEnableVertexAttribute(RL_DEFAULT_SHADER_ATTRIB_LOCATION_TANGENT);
            context.rlgl.rlDisableVertexArray();
        }

        context.tracelog.TRACELOG(LOG_INFO, "MESH: Tangents data computed and uploaded for provided mesh");
    }

    /**
     * Draw a model (with texture if set)
     *
     * @param model Model to draw
     * @param position Position to draw model at
     * @param scale Amount to scale model by
     * @param tint Tint to apply when drawing model
     */
    public void DrawModel(Model model, Vector3 position, float scale, Color tint) {
        Vector3 vScale = new Vector3(scale, scale, scale);
        Vector3 rotationAxis = new Vector3(0.0f, 1.0f, 0.0f);

        DrawModelEx(model, position, rotationAxis, 0.0f, vScale, tint);
    }

    /**
     * Draw a model with extended parameters
     *
     * @param model Model to draw
     * @param position Position to draw model at
     * @param rotationAxis Axis about which to rotate model
     * @param rotationAngle Amount to rotate model
     * @param scale Amount to scale model on each axis by
     * @param tint Tint to apply when drawing model
     */
    public void DrawModelEx(Model model, Vector3 position, Vector3 rotationAxis, float rotationAngle, Vector3 scale, Color tint) {
        // Calculate transformation matrix from function parameters
        // Get transform matrix (rotation -> scale -> translation)
        Matrix matScale = MatrixScale(scale.x, scale.y, scale.z);
        Matrix matRotation = MatrixRotate(rotationAxis, rotationAngle * DEG2RAD);
        Matrix matTranslation = MatrixTranslate(position.x, position.y, position.z);

        Matrix matTransform = MatrixMultiply(MatrixMultiply(matScale, matRotation), matTranslation);
        // Combine model transformation matrix (model.transform) with matrix generated by function parameters (matTransform)
        Matrix transform = MatrixMultiply(model.transform, matTransform);

        for (int i = 0; i < model.meshCount; i++) {
            Material mat = model.materials[model.meshMaterial[i]];
            Color colDiffuse = mat.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color;

            // Applying color tint directly to material diffuse map,
            // because is comes as an input paramter to the function
            Color colTinted = new Color();
            colTinted.r = ((colDiffuse.r * tint.r)/255);
            colTinted.g = ((colDiffuse.g * tint.g)/255);
            colTinted.b = ((colDiffuse.b * tint.b)/255);
            colTinted.a = ((colDiffuse.a * tint.a)/255);

            mat.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = colTinted;

            // Upload runtime bone transforms matrices, to compute skinning on the shader (GPU-skinning)
            // NOTE: Required location must be found and Mesh bones indices and weights must be also uploaded to shader
            if ((mat.shader.locs != null) && (mat.shader.locs[SHADER_LOC_MATRIX_BONETRANSFORMS.GetLocation()] != -1) && (model.boneMatrices != null)) {
                context.rlgl.rlEnableShader(mat.shader.id); // Enable shader to set bone transform matrices
                context.rlgl.rlSetUniformMatrices(mat.shader.locs[SHADER_LOC_MATRIX_BONETRANSFORMS.GetLocation()], model.boneMatrices);
            }

            DrawMesh(model.meshes[i], mat, model.transform);

            // Restore material diffuse map color (before tint applied)
            mat.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = colDiffuse;
        }
    }

    /**
     * Draw a model wires (with texture if set)
     *
     * @param model Model to draw
     * @param position Position to draw model at
     * @param scale Amount to scale model by
     * @param tint Tint to apply when drawing model
     */
    public void DrawModelWires(Model model, Vector3 position, float scale, Color tint) {
        context.rlgl.rlEnableWireMode();

        DrawModel(model, position, scale, tint);

        context.rlgl.rlDisableWireMode();
    }

    /**
     * Draw a model wires (with texture if set) with extended parameters
     *
     * @param model Model to draw
     * @param position Position to draw model at
     * @param rotationAxis Axis about which to rotate model
     * @param rotationAngle Amount to rotate model
     * @param scale Amount to scale model on each axis by
     * @param tint Tint to apply when drawing model
     */
    public void DrawModelWiresEx(Model model, Vector3 position, Vector3 rotationAxis, float rotationAngle, Vector3 scale, Color tint) {
        context.rlgl.rlEnableWireMode();

        DrawModelEx(model, position, rotationAxis, rotationAngle, scale, tint);

        context.rlgl.rlDisableWireMode();
    }

    /**
     * Draw a billboard
     *
     * @param camera Camera to use when drawing billboard
     * @param texture Texture to draw as a billboard
     * @param position Position in the world to draw the billboard
     * @param scale Scaling factor used when drawing the billoard
     * @param tint Color tint to apply when drawing billboard
     */
    public void DrawBillboard(Camera3D camera, Texture2D texture, Vector3 position, float scale, Color tint) {
        Rectangle source = new Rectangle(0.0f, 0.0f, (float) texture.width, (float) texture.height);

        DrawBillboardRec(camera, texture, source, position, new Vector2(scale * Math.abs(source.width / source.height), scale), tint);
    }

    /**
     * Draw a billboard from part of a texture defined by a rectangle
     *
     * @param camera Camera to use when drawing billboard
     * @param texture Texture to draw as a billboard
     * @param source Region of the texture to render
     * @param position Position in the world to draw the billboard
     * @param size (Width, Height) pair defining billboard size
     * @param tint Color tint to apply when drawing billboard
     */
    public void DrawBillboardRec(Camera3D camera, Texture2D texture, Rectangle source, Vector3 position, Vector2 size, Color tint) {
        // NOTE: Billboard locked on axis-Y
        Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);

        DrawBillboardPro(camera, texture, source, position, up, size, Vector2Scale(size, 0.5f), 0.0f, tint);
    }

    /**
     * Draw a billboard with additional parameters
     *
     * @param camera Camera to use when drawing billboard
     * @param texture Texture to draw as a billboard
     * @param source Region of the texture to render
     * @param position Position in the world to draw the billboard
     * @param up Upwards pointing vector of the billboard
     * @param size (Width, Height) pair defining billboard size
     * @param origin Centerpoint within the billboard
     * @param rotation Rotation applied to the billboard about the up vector, in degrees
     * @param tint Color tint to apply when drawing billboard
     */
    public void DrawBillboardPro(Camera3D camera, Texture2D texture, Rectangle source, Vector3 position, Vector3 up, Vector2 size, Vector2 origin, float rotation, Color tint) {
        // Compute the up vector and the right vector
        Matrix matView = MatrixLookAt(camera.position, camera.target, camera.up);
        Vector3 right = new Vector3(matView.m0, matView.m4, matView.m8);
        right = Vector3Scale(right, size.x);
        up = Vector3Scale(up, size.y);

        // Flip the content of the billboard while maintaining the counterclockwise edge rendering order
        if (size.x < 0.0f) {
            source.x -= size.x;
            source.width *= -1.0;
            right = Vector3Negate(right);
            origin.x *= -1.0f;
        }
        if (size.y < 0.0f) {
            source.y -= size.y;
            source.height *= -1.0;
            up = Vector3Negate(up);
            origin.y *= -1.0f;
        }

        // Draw the texture region described by source on the following rectangle in 3D space:
        //
        //                size.x          <--.
        //  3 ^---------------------------+ 2 \ rotation
        //    |                           |   /
        //    |                           |
        //    |   origin.x   position     |
        // up |..............             | size.y
        //    |             .             |
        //    |             . origin.y    |
        //    |             .             |
        //  0 +---------------------------> 1
        //                right
        Vector3 forward = new Vector3();
        if (rotation != 0.0) {
            forward = Vector3CrossProduct(right, up);
        }

        Vector3 origin3D = Vector3Add(Vector3Scale(Vector3Normalize(right), origin.x), Vector3Scale(Vector3Normalize(up), origin.y));

        Vector3[] points = new Vector3[4];
        points[0] = Vector3Zero();
        points[1] = right;
        points[2] = Vector3Add(up, right);
        points[3] = up;

        for (int i = 0; i < 4; i++) {
            points[i] = Vector3Subtract(points[i], origin3D);
            if (rotation != 0.0) {
                points[i] = Vector3RotateByAxisAngle(points[i], forward, rotation * DEG2RAD);
            }
            points[i] = Vector3Add(points[i], position);
        }

        Vector2 texcoords[] = new Vector2[4];
        texcoords[0] = new Vector2((float) source.x / texture.width, (float) (source.y + source.height) / texture.height);
        texcoords[1] = new Vector2((float) (source.x + source.width) / texture.width, (float) (source.y + source.height) / texture.height);
        texcoords[2] = new Vector2((float) (source.x + source.width) / texture.width, (float) source.y / texture.height);
        texcoords[3] = new Vector2((float) source.x / texture.width, (float) source.y / texture.height);

        context.rlgl.rlSetTexture(texture.id);
        context.rlgl.rlBegin(RL_QUADS);

        context.rlgl.rlColor4ub(tint.r, tint.g, tint.b, tint.a);
        for (int i = 0; i < 4; i++) {
            context.rlgl.rlTexCoord2f(texcoords[i].x, texcoords[i].y);
            context.rlgl.rlVertex3f(points[i].x, points[i].y, points[i].z);
        }

        context.rlgl.rlEnd();
        context.rlgl.rlSetTexture(0);
    }

    /**
     * Draw a bounding box with wires
     *
     * @param box BoundingBox to draw
     * @param color Color to draw wires
     */
    public void DrawBoundingBox(BoundingBox box, Color color) {
        Vector3 size = new Vector3();

        size.x = Math.abs(box.max.x - box.min.x);
        size.y = Math.abs(box.max.y - box.min.y);
        size.z = Math.abs(box.max.z - box.min.z);

        Vector3 center = new Vector3(box.min.x + size.x / 2.0f, box.min.y + size.y / 2.0f, box.min.z + size.z / 2.0f);

        DrawCubeWires(center, size.x, size.y, size.z, color);
    }

    /**
     * Check collision between two spheres
     *
     * @param center1 Center point of sphere 1
     * @param radius1 Radius of sphere 1
     * @param center2 Center point of sphere 2
     * @param radius2 Radius of sphere 2
     * @return {@code true} if the two spheres overlap
     */
    public boolean CheckCollisionSpheres(Vector3 center1, float radius1, Vector3 center2, float radius2) {
        boolean collision = false;

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
        float radSum = radius1 + radius2;

        if (Vector3DistanceSqr(center1, center2) <= (radSum*radSum)) {
            collision = true;
        }

        return collision;
    }

    /**
     * Check collision between two boxes
     *
     * @param box1 First {@code BoundingBox} to evaluate
     * @param box2 Second {@code BoundingBox} to evaluate
     * @return {@code true} if the {@code BoundingBox}es overlap
     */
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

    /**
     * Check collision between box and sphere
     *
     * @param box {@code BoundingBox} to evaluate
     * @param center Center point of the sphere
     * @param radius Radius of the sphere
     * @return {@code true} if there is overlap between the two shapes
     */
    public boolean CheckCollisionBoxSphere(BoundingBox box, Vector3 center, float radius) {
        boolean collision = false;

        Vector3 closestPoint = new Vector3(
                Clamp(center.x, box.min.x, box.max.x),
                Clamp(center.y, box.min.y, box.max.y),
                Clamp(center.z, box.min.z, box.max.z)
        );

        if (Vector3DistanceSqr(center, closestPoint) <= (radius*radius)) {
            collision = true;
        }

        return collision;
    }

    /**
     * Get Collision info between ray and sphere
     *
     * @param ray {@code Ray} to evaluate
     * @param center Center point of the sphere
     * @param radius Radius of the sphere
     * @return {@code RayCollision} object containing collision information
     * @see RayCollision
     */
    public RayCollision GetRayCollisionSphere(Ray ray, Vector3 center, float radius) {
        RayCollision collision = new RayCollision();

        Vector3 raySpherePos = Vector3Subtract(center, ray.position);
        float vector = Vector3DotProduct(raySpherePos, ray.direction);
        float distance = Vector3Length(raySpherePos);
        float d = radius * radius - (distance * distance - vector * vector);

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

    /**
     * Get collision info between a Ray and a BoundingBox
     *
     * @param ray {@code Ray} to evaluate
     * @param box {@code BoundingBox} to evaluate
     * @return {@code RayCollision} object containing collision information
     * @see RayCollision
     */
    public RayCollision GetRayCollisionBox(Ray ray, BoundingBox box) {
        RayCollision collision = new RayCollision();

        // NOTE: If ray.position is inside the box, the distance is negative (as if the ray was reversed)
        // Reversing ray.direction will give use the correct result
        boolean insideBox = (ray.position.x > box.min.x) && (ray.position.x < box.max.x) &&
                (ray.position.y > box.min.y) && (ray.position.y < box.max.y) &&
                (ray.position.z > box.min.z) && (ray.position.z < box.max.z);

        if (insideBox) {
            ray.direction = Vector3Negate(ray.direction);
        }

        float[] t = new float[11];

        t[8] = 1.0f / ray.direction.x;
        t[9] = 1.0f / ray.direction.y;
        t[10] = 1.0f / ray.direction.z;

        t[0] = (box.min.x - ray.position.x) * t[8];
        t[1] = (box.max.x - ray.position.x) * t[8];
        t[2] = (box.min.y - ray.position.y) * t[9];
        t[3] = (box.max.y - ray.position.y) * t[9];
        t[4] = (box.min.z - ray.position.z) * t[10];
        t[5] = (box.max.z - ray.position.z) * t[10];
        t[6] = Math.max(Math.max(Math.min(t[0], t[1]), Math.min(t[2], t[3])), Math.min(t[4], t[5]));
        t[7] = Math.min(Math.min(Math.max(t[0], t[1]), Math.max(t[2], t[3])), Math.max(t[4], t[5]));

        collision.hit = !((t[7] < 0) || (t[6] > t[7]));
        collision.distance = t[6];
        collision.point = Vector3Add(ray.position, Vector3Scale(ray.direction, collision.distance));

        // Get box center point
        collision.normal = Vector3Lerp(box.min, box.max, 0.5f);
        // Get vector center point->hit point
        collision.normal = Vector3Subtract(collision.point, collision.normal);
        // Scale vector to unit cube
        // NOTE: Use an additional .01 to fix numerical errors
        collision.normal = Vector3Scale(collision.normal, 2.01f);
        collision.normal = Vector3Divide(collision.normal, Vector3Subtract(box.max, box.min));
        // The relevant elemets of the vector are now slightly larger than 1.0f (or smaller than -1.0f)
        // and the others are somewhere between -1.0 and 1.0 casting to int is exactly our wanted normal!
        collision.normal.x = (float) ((int) collision.normal.x);
        collision.normal.y = (float) ((int) collision.normal.y);
        collision.normal.z = (float) ((int) collision.normal.z);

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

    /**
     * Get collision info between ray and mesh
     *
     * @param ray {@code Ray} to evaluate
     * @param mesh {@code Mesh} to evaluate
     * @param transform Transformation matrix to be used against the {@code mesh}
     * @return {@code RayCollision} object containing collision information
     * @see RayCollision
     */
    public RayCollision GetRayCollisionMesh(Ray ray, Mesh mesh, Matrix transform) {
        RayCollision collision = new RayCollision();

        // Check if mesh vertex data on CPU for testing
        if (mesh.vertices != null) {
            int triangleCount = mesh.triangleCount;

            // Test against all triangles in mesh
            for (int i = 0; i < triangleCount; i++) {
                Vector3 a, b, c;
                Vector3[] vertdata = new Vector3[mesh.vertices.length];

                for (int j = 0, k = 0; j < vertdata.length; j += 3, k++) {
                    vertdata[k] = new Vector3(mesh.vertices[j], mesh.vertices[j + 1], mesh.vertices[j + 2]);
                }

                if (mesh.indices != null) {
                    a = vertdata[(int) mesh.indices[i * 3]];
                    b = vertdata[(int) mesh.indices[i * 3 + 1]];
                    c = vertdata[(int) mesh.indices[i * 3 + 2]];
                }
                else {
                    a = vertdata[i * 3];
                    b = vertdata[i * 3 + 1];
                    c = vertdata[i * 3 + 2];
                }

                a = Vector3Transform(a, transform);
                b = Vector3Transform(b, transform);
                c = Vector3Transform(c, transform);

                RayCollision triHitInfo = GetRayCollisionTriangle(ray, a, b, c);

                if (triHitInfo.hit) {
                    // Save the closest hit triangle
                    if ((!collision.hit) || (collision.distance > triHitInfo.distance)) {
                        collision = triHitInfo;
                    }
                }
            }
        }

        return collision;
    }

    /**
     * Get collision info between ray and triangle <br/>
     * NOTE: The points are expected to be given in counter-clockwise winding
     *
     * @param ray {@code Ray} to evaluate
     * @param p1 First vertex of the triangle
     * @param p2 Second vertex of the triangle
     * @param p3 Third vertex of the triangle
     * @return {@code RayCollision} object containing collision information
     * @see RayCollision
     * @link <a href="https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm">Based on Trumbore Intersection Algorithm</a>
     */
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

        invDet = 1.0f / det;

        // Calculate distance from V1 to ray origin
        tv = Vector3Subtract(ray.position, p1);

        // Calculate u parameter and test bound
        u = Vector3DotProduct(tv, p) * invDet;

        // The intersection lies outside of the triangle
        if ((u < 0.0f) || (u > 1.0f)) {
            return collision;
        }

        // Prepare to test v parameter
        q = Vector3CrossProduct(tv, edge1);

        // Calculate V parameter and test bound
        v = Vector3DotProduct(ray.direction, q) * invDet;

        // The intersection lies outside of the triangle
        if ((v < 0.0f) || ((u + v) > 1.0f)) {
            return collision;
        }

        t = Vector3DotProduct(edge2, q) * invDet;

        if (t > EPSILON) {
            // Ray hit, get hit point and normal
            collision.hit = true;
            collision.distance = t;
            collision.normal = Vector3Normalize(Vector3CrossProduct(edge1, edge2));
            collision.point = Vector3Add(ray.position, Vector3Scale(ray.direction, t));
        }

        return collision;
    }

    /**
     * Get collision info between ray and quad <br/>
     * NOTE: The points are expected to be given in counter-clockwise winding
     *
     * @param ray {@code Ray} to evaluate
     * @param p1 First vertex of the quad
     * @param p2 Second vertex of the quad
     * @param p3 Third vertex of the quad
     * @param p4 Fourth vertex of the quad
     * @return {@code RayCollision} object containing collision information
     * @see RayCollision
     */
    public RayCollision GetRayCollisionQuad(Ray ray, Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p4) {
        RayCollision collision = new RayCollision();

        collision = GetRayCollisionTriangle(ray, p1, p2, p4);

        if (!collision.hit) {
            collision = GetRayCollisionTriangle(ray, p2, p3, p4);
        }

        return collision;
    }

    //----------------------------------------------------------------------------------
    // Module Internal Functions Definition
    //----------------------------------------------------------------------------------

    // Build pose from parent joints
    // NOTE: Required for animations loading (required by IQM and GlTF)

    /**
     * Build pose from parent joints <br/>
     * Required for animations loading
     *
     * @param bones
     * @param transforms
     * @return
     */
    private Transform[] BuildPoseFromParentJoints(BoneInfo[] bones, Transform[] transforms) {
        Transform[] result = transforms.clone();

        for (int i = 0; i < bones.length; i++) {
            if (bones[i].parent >= 0) {
                if (bones[i].parent > i) {
                    context.tracelog.TRACELOG(LOG_WARNING, "Skipping bone not topologically sorted: Bone %i has parent %i", i, bones[i].parent);
                    continue;
                }
                result[i].rotation = QuaternionMultiply(result[bones[i].parent].rotation, result[i].rotation);
                result[i].scale = Vector3Multiply(result[i].scale, result[bones[i].parent].scale);
                result[i].translation = Vector3Multiply(result[i].translation, result[bones[i].parent].scale);
                result[i].translation = Vector3RotateByQuaternion(result[i].translation, result[bones[i].parent].rotation);
                result[i].translation = Vector3Add(result[i].translation, result[bones[i].parent].translation);
            }
        }

        return result;
    }

    /**
     * Load OBJ mesh data
     *
     * @param fileName Filepath of OBJ file
     * @return {@code Model} as defined by the OBJ file
     */
    private Model LoadOBJ(String fileName) {
        Model model = new Model();

        try {
            OBJLoader loader = new OBJLoader();
            boolean success = loader.ReadOBJ(context, fileName, true);

            if (success) {
                context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] OBJ data loaded successfully: " + loader.objInfo.shapes.length + " meshes/" + loader.objInfo.totalMaterials + " materials");
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load OBJ data");
                return model;
            }

            model.meshCount = loader.objInfo.totalMaterials;

            // Init model materials array
            if (loader.objInfo.totalMaterials > 0) {
                model.materialCount = loader.objInfo.totalMaterials;
                model.materials = new Material[model.materialCount];
                context.tracelog.TRACELOG(LOG_INFO, "MODEL: model has " + model.materialCount + " material meshes.");
            }
            else {
                model.meshCount = 1;
                context.tracelog.TRACELOG(LOG_INFO, "MODEL: No materials, putting all meshes in a default material");
            }

            model.meshes = new Mesh[model.meshCount];
            model.meshMaterial = new int[model.meshCount];

            // Count the faces for each material
            int[] matFaces = new int[model.meshCount];

            // if no materials are present use all faces on one mesh
            if (loader.objInfo.totalMaterials > 0) {
                for (int fi = 0; fi < loader.objInfo.totalFaces; fi++) {
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
                model.meshes[mi].vertexCount = matFaces[mi] * 3;
                model.meshes[mi].triangleCount = matFaces[mi];
                model.meshes[mi].vertices = new float[model.meshes[mi].vertexCount * 3];
                model.meshes[mi].texcoords = new float[model.meshes[mi].vertexCount * 2];
                model.meshes[mi].normals = new float[model.meshes[mi].vertexCount * 3];
                model.meshMaterial[mi] = mi;
            }

            // Scan through the combined sub meshes and pick out each material mesh
            for (int af = 0; af < loader.objInfo.totalFaces; af++) {
                int mm = loader.objInfo.materialIds[af];
                if (mm == -1) {
                    mm = 0;
                }

                // Get indices for the face
                OBJLoader.OBJVertexIndex idx0 = loader.objInfo.faces[3 * af + 0];
                OBJLoader.OBJVertexIndex idx1 = loader.objInfo.faces[3 * af + 1];
                OBJLoader.OBJVertexIndex idx2 = loader.objInfo.faces[3 * af + 2];

                // Fill vertices buffer (float) using vertex index of the face
                // TODO: 11/07/2022 Eventually I'd like to be able to remove the - 1 from the following bits, but it works well enough for now...
                for (int v = 0; v < 3; v++) {
                    model.meshes[mm].vertices[vCount[mm] + v] = loader.objInfo.vertices[(idx0.vIndex - 1) * 3 + v];
                }
                vCount[mm] += 3;
                for (int v = 0; v < 3; v++) {
                    model.meshes[mm].vertices[vCount[mm] + v] = loader.objInfo.vertices[(idx1.vIndex - 1) * 3 + v];
                }
                vCount[mm] += 3;
                for (int v = 0; v < 3; v++) {
                    model.meshes[mm].vertices[vCount[mm] + v] = loader.objInfo.vertices[(idx2.vIndex - 1) * 3 + v];
                }
                vCount[mm] += 3;

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
                    for (int v = 0; v < 3; v++) {
                        model.meshes[mm].normals[vnCount[mm] + v] = loader.objInfo.normals[(idx0.vnIndex - 1) * 3 + v];
                    }
                    vnCount[mm] += 3;
                    for (int v = 0; v < 3; v++) {
                        model.meshes[mm].normals[vnCount[mm] + v] = loader.objInfo.normals[(idx1.vnIndex - 1) * 3 + v];
                    }
                    vnCount[mm] += 3;
                    for (int v = 0; v < 3; v++) {
                        model.meshes[mm].normals[vnCount[mm] + v] = loader.objInfo.normals[(idx2.vnIndex - 1) * 3 + v];
                    }
                    vnCount[mm] += 3;
                }
            }

            String path = fileName.substring(0, fileName.lastIndexOf("/") + 1);
            model.materials = ProcessMaterialsOBJ(loader, path);

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    /**
     * Convert array of four bytes to int <br/>
     * If an array with a length of less than four is passed, {@code -1} <br/>
     * If an array with greater than four is passed, only [0...3] are considered.
     *
     * @param bytes Byte array of at least four bytes
     * @return Integer value defined by bytes [0...3] of the byte array
     */
    private static int IQM_toInt(byte[] bytes) {
        if (bytes.length < Integer.BYTES) {
            return -1;
        }
        else {
            return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getInt();
        }
    }

    /**
     * Convert array of four bytes to float <br/>
     * If an array with a length of less than four is passed, {@code -1} <br/>
     * If an array with greater than four is passed, only [0...3] are considered.
     *
     * @param bytes Byte array of at least four bytes
     * @return Float value defined by bytes [0...3] of the byte array
     */
    private static float IQM_toFloat(byte[] bytes) {
        if (bytes.length < Float.BYTES) {
            return -1;
        }
        else {
            return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getFloat();
        }
    }

    /**
     * Convert array of two bytes to short <br/>
     * If an array with a length of less than two is passed, {@code -1} <br/>
     * If an array with greater than two is passed, only [0...1] are considered.
     *
     * @param bytes Byte array of at least two bytes
     * @return Short value defined by bytes [0...1] of the byte array
     */
    private static short IQM_toShort(byte[] bytes) {
        if (bytes.length < Short.BYTES) {
            return -1;
        }
        else {
            return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getShort();
        }
    }

    /**
     * Load IQM mesh data
     *
     * @param fileName Location of the IQM file
     * @return {@code Model} as defined by the IQM file
     */
    public Model LoadIQM(String fileName) {
        Model model = new Model();

        try (ByteArrayInputStream fileData = new ByteArrayInputStream(context.files.LoadFileData(fileName))) {

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
            byte[] intBuffer = new byte[Integer.BYTES];
            byte[] floatBuffer = new byte[Float.BYTES];
            byte[] tmpMagic = new byte[header.magic.length];

            // Read IQM Header
            fileDataPtr += fileData.read(tmpMagic);
            for (int i = 0; i < header.magic.length; i++) {
                header.magic[i] = (char) tmpMagic[i];
            }
            fileDataPtr += fileData.read(intBuffer);
            header.version = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.filesize = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.flags = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_text = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_text = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_meshes = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_meshes = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_vertexarrays = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_vertexes = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_vertexarrays = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_triangles = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_triangles = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_adjacency = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_joints = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_joints = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_poses = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_poses = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_anims = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_anims = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_frames = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_framechannels = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_frames = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_bounds = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_comment = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_comment = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.num_extensions = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            header.ofs_extensions = IQM_toInt(intBuffer);

            if (!IQM_MAGIC.equals(String.valueOf(header.magic))) {
                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] IQM file is not a valid model");
            }
            if (IQM_VERSION != header.version) {
                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] IQM file version not supported (" + header.version + ")");
            }

            imesh = new IQMMesh[header.num_meshes];
            int off = 0;
            for (int i = 0; i < imesh.length; i++) {
                imesh[i] = new IQMMesh();
                fileData.reset();
                fileData.skip(header.ofs_meshes + off);
                fileData.read(intBuffer);
                imesh[i].name = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                imesh[i].material = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                imesh[i].first_vertex = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                imesh[i].num_vertexes = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                imesh[i].first_triangle = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                imesh[i].num_triangles = IQM_toInt(intBuffer);
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
                fileData.skip(header.ofs_text + imesh[i].material);
                fileData.read(tmpMaterial);
                for (int j = 0; j < MESH_NAME_LENGTH; j++) {
                    material[j] = (char) tmpMaterial[j];
                }

                model.meshes[i] = new Mesh();
                model.materials[i] = LoadMaterialDefault();

                context.tracelog.TRACELOG(LOG_DEBUG, "MODEL: [" + fileName + "] mesh name (" + String.valueOf(name) + "), material (" + String.valueOf(material) + ")");

                model.meshes[i].vertexCount = imesh[i].num_vertexes;

                model.meshes[i].vertices = new float[model.meshes[i].vertexCount * 3];
                model.meshes[i].normals = new float[model.meshes[i].vertexCount * 3];
                model.meshes[i].texcoords = new float[model.meshes[i].vertexCount * 2];

                model.meshes[i].boneIndices = new byte[model.meshes[i].vertexCount * 4];
                model.meshes[i].boneWeights = new float[model.meshes[i].vertexCount * 4];

                model.meshes[i].triangleCount = imesh[i].num_triangles;
                model.meshes[i].indicesS = new short[model.meshes[i].triangleCount * 3];

                // Animated verted data, what we actually process for rendering
                // NOTE: Animated vertex should be re-uploaded to GPU (if not using GPU skinning)
                model.meshes[i].animVertices = new float[model.meshes[i].vertexCount * 3];
                model.meshes[i].animNormals = new float[model.meshes[i].vertexCount * 3];
            }

            // Triangles data processing
            tri = new IQMTriangle[header.num_triangles];
            fileData.reset();
            fileData.skip(header.ofs_triangles);
            for (int i = 0; i < tri.length; i++) {
                tri[i] = new IQMTriangle();
                for (int j = 0; j < tri[i].vertex.length; j++) {
                    fileData.read(intBuffer);
                    tri[i].vertex[j] = IQM_toInt(intBuffer);
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
                fileData.read(intBuffer);
                va[i].type = IQMVertexDataType.values()[IQM_toInt(intBuffer)];
                fileData.read(intBuffer);
                va[i].flags = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                va[i].format = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                va[i].size = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                va[i].offset = IQM_toInt(intBuffer);
            }

            for (int i = 0; i < header.num_vertexarrays; i++) {
                switch (va[i].type) {
                    case IQM_POSITION: {
                        vertex = new float[header.num_vertexes * 3];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        for (int j = 0; j < vertex.length; j++) {
                            fileData.read(floatBuffer);
                            vertex[j] = IQM_toFloat(floatBuffer);
                        }

                        for (int m = 0; m < header.num_meshes; m++) {
                            int vCounter = 0;
                            for (int j = imesh[m].first_vertex * 3; j < (imesh[m].first_vertex + imesh[m].num_vertexes) * 3; j++) {
                                model.meshes[m].vertices[vCounter] = vertex[j];
                                model.meshes[m].animVertices[vCounter] = vertex[j];
                                vCounter++;
                            }
                        }
                    }
                    break;
                    case IQM_TEXCOORD: {
                        text = new float[header.num_vertexes * 2];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        for (int j = 0; j < text.length; j++) {
                            fileData.read(floatBuffer);
                            text[j] = IQM_toFloat(floatBuffer);
                        }

                        for (int m = 0; m < header.num_meshes; m++) {
                            int vCounter = 0;

                            for (int j = imesh[m].first_vertex * 2; j < (imesh[m].first_vertex + imesh[m].num_vertexes) * 2; j++) {
                                model.meshes[m].texcoords[vCounter] = text[j];
                                vCounter++;
                            }
                        }
                    }
                    break;
                    case IQM_NORMAL: {
                        normal = new float[header.num_vertexes * 3];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        for (int j = 0; j < normal.length; j++) {
                            fileData.read(floatBuffer);
                            normal[j] = IQM_toFloat(floatBuffer);
                        }

                        for (int m = 0; m < header.num_meshes; m++) {
                            int vCounter = 0;
                            for (int j = imesh[m].first_vertex * 3; j < (imesh[m].first_vertex + imesh[m].num_vertexes) * 3; j++) {
                                model.meshes[m].normals[vCounter] = normal[j];
                                model.meshes[m].animNormals[vCounter] = normal[j];
                                vCounter++;
                            }
                        }
                    }
                    break;
                    case IQM_BLENDINDEXES: {
                        blendi = new byte[header.num_vertexes * 4];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        fileData.read(blendi);

                        for (int m = 0; m < header.num_meshes; m++) {
                            int boneCounter = 0;
                            for (int j = imesh[m].first_vertex * 4; j < (imesh[m].first_vertex + imesh[m].num_vertexes) * 4; j++) {
                                model.meshes[m].boneIndices[boneCounter] = blendi[j];
                                boneCounter++;
                            }
                        }
                    }
                    break;
                    case IQM_BLENDWEIGHTS: {
                        blendw = new byte[header.num_vertexes * 4];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        fileData.read(blendw);

                        for (int m = 0; m < header.num_meshes; m++) {
                            int boneCounter = 0;
                            for (int j = imesh[m].first_vertex * 4; j < (imesh[m].first_vertex + imesh[m].num_vertexes) * 4; j++) {
                                model.meshes[m].boneWeights[boneCounter] = Byte.toUnsignedInt(blendw[j]) / 255.0f;
                                boneCounter++;
                            }
                        }
                    }
                    break;
                    case IQM_COLOR: {
                        color = new byte[header.num_vertexes * 4];
                        fileData.reset();
                        fileData.skip(va[i].offset);
                        fileData.read(color);

                        for (int m = 0; m < header.num_meshes; m++) {
                            model.meshes[m].colors = new byte[model.meshes[m].vertexCount * 4];

                            int vCounter = 0;
                            for (int j = imesh[m].first_vertex * 4; j < (imesh[m].first_vertex + imesh[m].num_vertexes) * 4; j++) {
                                model.meshes[m].colors[vCounter] = color[j];
                                vCounter++;
                            }
                        }
                    }
                    break;
                    case IQM_CUSTOM: {
                    }
                    break;
                }
            }

            // Bones (joints) data processing
            joint = new IQMJoint[header.num_joints];
            fileData.reset();
            fileData.skip(header.ofs_joints);
            for (int i = 0; i < joint.length; i++) {
                joint[i] = new IQMJoint();
                fileData.read(intBuffer);
                joint[i].name = IQM_toInt(intBuffer);
                fileData.read(intBuffer);
                joint[i].parent = IQM_toInt(intBuffer);
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

            model.skeleton.boneCount = header.num_joints;
            model.skeleton.bones = new BoneInfo[header.num_joints];
            model.skeleton.bindPose = new Transform[header.num_joints];

            for (int i = 0; i < header.num_joints; i++) {
                // Bones
                model.skeleton.bones[i] = new BoneInfo();
                model.skeleton.bones[i].parent = joint[i].parent;
                byte[] tmp = new byte[BONE_NAME_LENGTH];
                char[] bonetmp = new char[BONE_NAME_LENGTH];
                fileData.reset();
                fileData.skip(header.ofs_text + joint[i].name);
                fileData.read(tmp);
                for (int j = 0; j < BONE_NAME_LENGTH; j++) {
                    bonetmp[j] = (char) tmp[j];
                }
                model.skeleton.bones[i].name = bonetmp.toString();

                // Bind pose (base pose)
                model.skeleton.bindPose[i] = new Transform();
                model.skeleton.bindPose[i].translation.x = joint[i].translate[0];
                model.skeleton.bindPose[i].translation.y = joint[i].translate[1];
                model.skeleton.bindPose[i].translation.z = joint[i].translate[2];

                model.skeleton.bindPose[i].rotation.x = joint[i].rotate[0];
                model.skeleton.bindPose[i].rotation.y = joint[i].rotate[1];
                model.skeleton.bindPose[i].rotation.z = joint[i].rotate[2];
                model.skeleton.bindPose[i].rotation.w = joint[i].rotate[3];

                model.skeleton.bindPose[i].scale.x = joint[i].scale[0];
                model.skeleton.bindPose[i].scale.y = joint[i].scale[1];
                model.skeleton.bindPose[i].scale.z = joint[i].scale[2];
            }

            model.skeleton.bindPose = BuildPoseFromParentJoints(model.skeleton.bones, model.skeleton.bindPose);

            for (int i = 0; i < model.meshCount; i++) {
                model.meshes[i].boneCount = model.skeleton.boneCount;
                model.meshes[i].boneMatrices = new Matrix[model.meshes[i].boneCount];
                for (int j = 0; j < model.meshes[i].boneMatrices.length; j++) {
                    model.meshes[i].boneMatrices[j] = MatrixIdentity();
                }
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    /**
     * Load IQM animation data
     * @param fileName Location of the IQM file
     * @return Array of {@code ModelAnimation}s as defined by the IQM file
     */
    public ModelAnimation[] LoadModelAnimationsIQM(String fileName) {
        final String IQM_MAGIC = "INTERQUAKEMODEL\0"; //IQM file magic number
        final int IQM_VERSION = 2;                    //only IQM version 2 supported

        int fileSize = 0;
        int fileDataPtr = 0;
        int animCount;

        ModelAnimation[] animations;

        try (ByteArrayInputStream fileData = new ByteArrayInputStream(context.files.LoadFileData(fileName))) {
            fileSize = fileData.available();

            IQMHeader iqmHeader = new IQMHeader();

            byte[] intBuffer = new byte[Integer.BYTES];
            byte[] floatBuffer = new byte[Float.BYTES];
            byte[] shortbuffer = new byte[Short.BYTES];
            byte[] tmpMagic = new byte[iqmHeader.magic.length];

            // Read IQM Header
            fileDataPtr += fileData.read(tmpMagic);
            for (int i = 0; i < iqmHeader.magic.length; i++) {
                iqmHeader.magic[i] = (char) tmpMagic[i];
            }
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.version = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.filesize = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.flags = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_text = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_text = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_meshes = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_meshes = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_vertexarrays = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_vertexes = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_vertexarrays = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_triangles = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_triangles = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_adjacency = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_joints = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_joints = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_poses = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_poses = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_anims = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_anims = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_frames = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_framechannels = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_frames = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_bounds = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_comment = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_comment = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.num_extensions = IQM_toInt(intBuffer);
            fileDataPtr += fileData.read(intBuffer);
            iqmHeader.ofs_extensions = IQM_toInt(intBuffer);

            if (!IQM_MAGIC.equals(String.valueOf(iqmHeader.magic))) {
                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] IQM file is not a valid model");
            }
            if (IQM_VERSION != iqmHeader.version) {
                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] IQM file version not supported (" + iqmHeader.version + ")");
            }

            // Get bones data
            IQMPose[] poses = new IQMPose[iqmHeader.num_poses];
            fileData.reset();
            fileData.skip(iqmHeader.ofs_poses);
            fileDataPtr = iqmHeader.ofs_poses;
            for (int i = 0; i < poses.length; i++) {
                poses[i] = new IQMPose();
                fileDataPtr += fileData.read(intBuffer);
                poses[i].parent = IQM_toInt(intBuffer);
                fileDataPtr += fileData.read(intBuffer);
                poses[i].mask = IQM_toInt(intBuffer);
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
                fileDataPtr += fileData.read(intBuffer);
                anim[i].name = IQM_toInt(intBuffer);
                fileDataPtr += fileData.read(intBuffer);
                anim[i].first_frame = IQM_toInt(intBuffer);
                fileDataPtr += fileData.read(intBuffer);
                anim[i].num_frames = IQM_toInt(intBuffer);
                fileDataPtr += fileData.read(floatBuffer);
                anim[i].framerate = IQM_toFloat(floatBuffer);
                fileDataPtr += fileData.read(intBuffer);
                anim[i].flags = IQM_toInt(intBuffer);
            }

            animations = new ModelAnimation[iqmHeader.num_anims];

            // frameposes
            int[] framedata = new int[iqmHeader.num_frames * iqmHeader.num_framechannels];
            fileData.reset();
            fileData.skip(iqmHeader.ofs_frames);
            fileDataPtr = iqmHeader.ofs_frames;

            for (int i = 0; i < framedata.length; i++) {
                fileDataPtr += fileData.read(shortbuffer);
                framedata[i] = Short.toUnsignedInt(IQM_toShort(shortbuffer));
            }

            for (int a = 0; a < iqmHeader.num_anims; a++) {
                animations[a] = new ModelAnimation();
                animations[a].boneCount = iqmHeader.num_poses;
                BoneInfo[] bones = new BoneInfo[iqmHeader.num_poses];
                
                animations[a].keyframeCount = anim[a].num_frames;
                animations[a].keyframePoses = new Transform[anim[a].num_frames][];
                // animations[a].framerate = anim.framerate;     // TODO: Use framerate?

                for (int j = 0; j < iqmHeader.num_poses; j++) {
                    bones[j] = new BoneInfo();
                    bones[j].name = "ANIMJOINTNAME";
                    bones[j].parent = poses[j].parent;
                }

                for (int j = 0; j < anim[a].num_frames; j++) {
                    animations[a].keyframePoses[j] = new Transform[iqmHeader.num_poses];
                    for (int i = 0; i < iqmHeader.num_poses; i++) {
                        animations[a].keyframePoses[j][i] = new Transform();
                    }
                }

                int dcounter = anim[a].first_frame * iqmHeader.num_framechannels;

                for (int frame = 0; frame < anim[a].num_frames; frame++) {
                    for (int i = 0; i < iqmHeader.num_poses; i++) {
                        animations[a].keyframePoses[frame][i].translation.x = poses[i].channeloffset[0];

                        if ((poses[i].mask & 0x01) == 1) {
                            animations[a].keyframePoses[frame][i].translation.x += framedata[dcounter] * poses[i].channelscale[0];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].translation.y = poses[i].channeloffset[1];

                        if ((poses[i].mask & 0x02) == 2) {
                            animations[a].keyframePoses[frame][i].translation.y += framedata[dcounter] * poses[i].channelscale[1];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].translation.z = poses[i].channeloffset[2];

                        if ((poses[i].mask & 0x04) == 4) {
                            animations[a].keyframePoses[frame][i].translation.z += framedata[dcounter] * poses[i].channelscale[2];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].rotation.x = poses[i].channeloffset[3];

                        if ((poses[i].mask & 0x08) == 8) {
                            animations[a].keyframePoses[frame][i].rotation.x += framedata[dcounter] * poses[i].channelscale[3];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].rotation.y = poses[i].channeloffset[4];

                        if ((poses[i].mask & 0x10) == 16) {
                            animations[a].keyframePoses[frame][i].rotation.y += framedata[dcounter] * poses[i].channelscale[4];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].rotation.z = poses[i].channeloffset[5];

                        if ((poses[i].mask & 0x20) == 32) {
                            animations[a].keyframePoses[frame][i].rotation.z += framedata[dcounter] * poses[i].channelscale[5];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].rotation.w = poses[i].channeloffset[6];

                        if ((poses[i].mask & 0x40) == 64) {
                            animations[a].keyframePoses[frame][i].rotation.w += framedata[dcounter] * poses[i].channelscale[6];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].scale.x = poses[i].channeloffset[7];

                        if ((poses[i].mask & 0x80) == 128) {
                            animations[a].keyframePoses[frame][i].scale.x += framedata[dcounter] * poses[i].channelscale[7];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].scale.y = poses[i].channeloffset[8];

                        if ((poses[i].mask & 0x100) == 256) {
                            animations[a].keyframePoses[frame][i].scale.y += framedata[dcounter] * poses[i].channelscale[8];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].scale.z = poses[i].channeloffset[9];

                        if ((poses[i].mask & 0x200) == 512) {
                            animations[a].keyframePoses[frame][i].scale.z += framedata[dcounter] * poses[i].channelscale[9];
                            dcounter++;
                        }

                        animations[a].keyframePoses[frame][i].rotation = QuaternionNormalize(animations[a].keyframePoses[frame][i].rotation);
                    }
                }

                // Build frameposes
                for (int frame = 0; frame < anim[a].num_frames; frame++) {
                    for (int i = 0; i < animations[a].boneCount; i++) {
                        if (bones[i].parent >= 0) {
                            animations[a].keyframePoses[frame][i].rotation = QuaternionMultiply(animations[a].keyframePoses[frame][bones[i].parent].rotation, animations[a].keyframePoses[frame][i].rotation);
                            animations[a].keyframePoses[frame][i].translation = Vector3RotateByQuaternion(animations[a].keyframePoses[frame][i].translation, animations[a].keyframePoses[frame][bones[i].parent].rotation);
                            animations[a].keyframePoses[frame][i].translation = Vector3Add(animations[a].keyframePoses[frame][i].translation, animations[a].keyframePoses[frame][bones[i].parent].translation);
                            animations[a].keyframePoses[frame][i].scale = Vector3Multiply(animations[a].keyframePoses[frame][i].scale, animations[a].keyframePoses[frame][bones[i].parent].scale);
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

    /**
     *
     * @param img
     * @param bufferView
     * @param texPath
     * @return
     */
    private Image LoadImageFromCgltfImage(ImageModel img, BufferViewModel bufferView, String texPath) {
        Image image = new Image();

        if (img.getUri() != null) {
            if (img.getUri().length() > 5 && img.getUri().startsWith("data:")) {
                int i = 0;
                while ((img.getUri().charAt(i) != ',') && (i != img.getUri().length())) {
                    i++;
                }

                if (i == img.getUri().length()) {
                    context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: glTF data URI is not a valid image");
                }
                else {
                    String uri = img.getUri();
                    String encoded = uri.substring(uri.lastIndexOf(",") + 1);
                    byte[] data = Base64.getDecoder().decode(encoded);

                    if (data != null) {
                        image = context.textures.LoadImageFromMemory(".png", data);
                    }

                }
            }
            else {
                image = context.textures.LoadImage(texPath + "/" + img.getUri());
            }
        }
        else if (bufferView.getBufferViewData() != null) {
            byte[] data = new byte[bufferView.getByteLength()];
            bufferView.getBufferViewData().get(data);

            if (img.getMimeType().equals("image\\/png") || img.getMimeType().equals("image/png")) {
                image = context.textures.LoadImageFromMemory(".png", data);
            }
            else if (img.getMimeType().equals("image\\/jpeg") || img.getMimeType().equals("image/jpeg")) {
                image = context.textures.LoadImageFromMemory(".jpg", data);
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: glTF image data MIME type not recognized (" + texPath + "/" + img.getMimeType() + ")");
            }
        }

        return image;
    }

    // Load bone info from GLTF skin data
    private BoneInfo[] LoadBoneInfoGLTF(SkinModel skin) {
        int boneCount = skin.getJoints().size();
        BoneInfo[] bones = new BoneInfo[boneCount];

        for (int i = 0; i < boneCount; i++) {
            NodeModel node = skin.getJoints().get(i);
            bones[i] = new BoneInfo();
            bones[i].name = node.getName();

            // Find parent bone index
            int parentIndex = -1;

            for (int j = 0; j < boneCount; j++) {
                if (node.getParent().equals(skin.getJoints().get(j))) {
                    parentIndex = j;
                    break;
                }
            }

            bones[i].parent = parentIndex;
        }

        int i = 0;
        for (BoneInfo bone : bones) {
            System.out.println(i + ": " + bone.name + ", " + bone.parent);
            i++;
        }

        return bones;
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

        GltfModel gltf;

        try {
            gltf = new GltfModelReader().read(Paths.get(fileName));
        }
        catch (IOException e) {
            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load glTF data");
            return model;
        }

        if (context.files.GetFileExtension(fileName).equalsIgnoreCase(".glb")) {
            context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Model basic data (glb) loaded successfully");
        }
        else if (context.files.GetFileExtension(fileName).equalsIgnoreCase(".gltf")) {
            context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Model basic data (glTF) loaded successfully");
        }

        context.tracelog.TRACELOG(LOG_INFO, "    > Meshes count: " + gltf.getMeshModels().size());
        context.tracelog.TRACELOG(LOG_INFO, "    > Materials count: " + gltf.getMaterialModels().size() + " (+1 default)");
        context.tracelog.TRACELOG(LOG_DEBUG, "    > Buffers count: " + gltf.getBufferModels().size());
        context.tracelog.TRACELOG(LOG_DEBUG, "    > Images count: " + gltf.getImageModels().size());
        context.tracelog.TRACELOG(LOG_DEBUG, "    > Textures count: " + gltf.getTextureModels().size());

        int primitivesCount = 0;
        // NOTE: We will load every primitive in the glTF as a separate raylib mesh
        for (int i = 0; i < gltf.getMeshModels().size(); i++) {
            primitivesCount += gltf.getMeshModels().get(i).getMeshPrimitiveModels().size();
        }
        context.tracelog.TRACELOG(LOG_DEBUG, "    > Primitives (triangles only) count based on hierarchy : " + primitivesCount);

        // Load our model data: meshes and materials
        model.meshCount = primitivesCount;
        model.meshes = new Mesh[model.meshCount];
        for (int i = 0; i < model.meshCount; i++) {
            model.meshes[i] = new Mesh();
        }

        // NOTE: We keep an extra slot for default material, in case some mesh requires it
        model.materialCount = gltf.getMaterialModels().size() + 1;
        model.materials = new Material[model.materialCount];
        model.materials[0] = LoadMaterialDefault();

        // Load mesh-material indices, by default all meshes are mapped to material index: 0
        model.meshMaterial = new int[model.meshCount];

        // Load materials data
        //----------------------------------------------------------------------------------------------------
        for (int i = 0, j = 1; i < gltf.getMaterialModels().size(); i++, j++) {
            model.materials[j] = LoadMaterialDefault();
            String texPath = context.files.GetDirectoryPath(fileName);
            MaterialModelV2 material = (MaterialModelV2) gltf.getMaterialModels().get(i);

            // Check glTF material flow: PBR metallic/roughness flow
            // NOTE: Alternatively, materials can follow PBR specular/glossiness flow
            if (material != null) {
                // Load base color texture (albedo)
                if (material.getBaseColorTexture() != null) {
                    Image imAlbedo = LoadImageFromCgltfImage(material.getBaseColorTexture().getImageModel(), material.getBaseColorTexture().getImageModel().getBufferViewModel(), texPath);
                    if (imAlbedo.getData() != null) {
                        model.materials[j].maps[MATERIAL_MAP_ALBEDO.GetIndex()].texture = context.textures.LoadTextureFromImage(imAlbedo);
                    }
                }
                //Load base colour factor (tint)
                model.materials[j].maps[MATERIAL_MAP_ALBEDO.GetIndex()].color = new Color();
                model.materials[j].maps[MATERIAL_MAP_ALBEDO.GetIndex()].color.r = (int) (material.getBaseColorFactor()[0] * 255);
                model.materials[j].maps[MATERIAL_MAP_ALBEDO.GetIndex()].color.g = (int) (material.getBaseColorFactor()[1] * 255);
                model.materials[j].maps[MATERIAL_MAP_ALBEDO.GetIndex()].color.b = (int) (material.getBaseColorFactor()[2] * 255);
                model.materials[j].maps[MATERIAL_MAP_ALBEDO.GetIndex()].color.a = (int) (material.getBaseColorFactor()[3] * 255);

                //Load metallic/roughness texture
                if (material.getMetallicRoughnessTexture() != null) {
                    Image imMetallicRoughness = LoadImageFromCgltfImage(material.getMetallicRoughnessTexture().getImageModel(), material.getMetallicRoughnessTexture().getImageModel().getBufferViewModel(), texPath);
                    if (imMetallicRoughness.getData() != null) {
                        model.materials[j].maps[MATERIAL_MAP_ROUGHNESS.GetIndex()].texture = context.textures.LoadTextureFromImage(imMetallicRoughness);
                    }

                    // Load metallic/roughness material properties
                    model.materials[j].maps[MATERIAL_MAP_ROUGHNESS.GetIndex()].value = material.getRoughnessFactor();
                    model.materials[j].maps[MATERIAL_MAP_METALNESS.GetIndex()].value = material.getMetallicFactor();
                }

                //Load normal texture
                if (material.getNormalTexture() != null) {
                    Image imNormal = LoadImageFromCgltfImage(material.getNormalTexture().getImageModel(), material.getNormalTexture().getImageModel().getBufferViewModel(), texPath);
                    if (imNormal.getData() != null) {
                        model.materials[j].maps[MATERIAL_MAP_NORMAL.GetIndex()].texture = context.textures.LoadTextureFromImage(imNormal);
                    }
                }

                //Load ambient occlusion texture
                if (material.getOcclusionTexture() != null) {
                    Image imOcclusion = LoadImageFromCgltfImage(material.getOcclusionTexture().getImageModel(), material.getOcclusionTexture().getImageModel().getBufferViewModel(), texPath);
                    if (imOcclusion.getData() != null) {
                        model.materials[j].maps[MATERIAL_MAP_OCCLUSION.GetIndex()].texture = context.textures.LoadTextureFromImage(imOcclusion);
                    }
                }

                //Load emission texture
                if (material.getEmissiveTexture() != null) {
                    Image imEmissive = LoadImageFromCgltfImage(material.getEmissiveTexture().getImageModel(), material.getEmissiveTexture().getImageModel().getBufferViewModel(), texPath);
                    if (imEmissive.getData() != null) {
                        model.materials[j].maps[MATERIAL_MAP_EMISSION.GetIndex()].texture = context.textures.LoadTextureFromImage(imEmissive);
                    }

                    //Load base colour factor (tint)
                    model.materials[j].maps[MATERIAL_MAP_EMISSION.GetIndex()].color.r = (int) (material.getEmissiveFactor()[0] * 255);
                    model.materials[j].maps[MATERIAL_MAP_EMISSION.GetIndex()].color.g = (int) (material.getEmissiveFactor()[1] * 255);
                    model.materials[j].maps[MATERIAL_MAP_EMISSION.GetIndex()].color.b = (int) (material.getEmissiveFactor()[2] * 255);
                    model.materials[j].maps[MATERIAL_MAP_EMISSION.GetIndex()].color.a = 255;
                }
            }
        }

        // Other possible materials not supported by raylib pipeline:
        // has_clearcoat, has_transmission, has_volume, has_ior, has specular, has_sheen

        // Visit each node in the hierarchy and process any mesh linked from it.
        // Each primitive within a glTF node becomes a Raylib Mesh.
        // The local-to-world transform of each node is used to transform the
        // points/normals/tangents of the created Mesh(es).
        // Any glTF mesh linked from more than one Node (i.e. instancing)
        // is turned into multiple Mesh's, as each Node will have its own
        // transform applied.
        // NOTE: The code below disregards the scenes defined in the file, all nodes are used.
        //----------------------------------------------------------------------------------------------------
        for (int i = 0, meshIndex = 0; i < gltf.getNodeModels().size(); i++) {
            // NOTE: meshIndex accumulates primitives
            NodeModel node = gltf.getNodeModels().get(i);
            if (node.getMeshModels().isEmpty()) {
                continue;
            }
            MeshModel mesh = node.getMeshModels().get(0);

            float[] worldTransform = new float[16];
            node.computeLocalTransform(worldTransform);
            Matrix worldMatrix = new Matrix(
                    worldTransform[0], worldTransform[4], worldTransform[8], worldTransform[12],
                    worldTransform[1], worldTransform[5], worldTransform[9], worldTransform[13],
                    worldTransform[2], worldTransform[6], worldTransform[10], worldTransform[14],
                    worldTransform[3], worldTransform[7], worldTransform[11], worldTransform[15]
            );
            Matrix worldMatrixNormals = MatrixTranspose(MatrixInvert(worldMatrix));

            for (int p = 0; p < mesh.getMeshPrimitiveModels().size(); p++) {
                // NOTE: We only support primitives defined by triangles
                // Other alternatives: points, lines, line_strip, triangle_strip
                if (mesh.getMeshPrimitiveModels().get(p).getMode() != RL_TRIANGLES) {
                    continue;
                }

                // NOTE: Attributes data could be provided in several data formats (8, 8u, 16u, 32...),
                // Only some formats for each attribute type are supported, read info at the top of this function!

                for (int j = 0; j < mesh.getMeshPrimitiveModels().get(p).getAttributes().size(); j++) {
                    // Check the different attributes for every primitive

                    // POSITION
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("POSITION")) {
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("POSITION");

                        // WARNING: SPECS: POSITION accessor MUST have its min and max properties defined.

                        if ((attribute.getComponentDataType() == float.class) && (attribute.getElementType() == ElementType.VEC3)) {
                            // Init raylib mesh vertices to copy glTF attribute data
                            model.meshes[meshIndex].vertexCount = attribute.getCount();
                            model.meshes[meshIndex].vertices = new float[attribute.getCount() * 3];

                            // Load 3 components of float data type into mesh.vertices
                            attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(model.meshes[meshIndex].vertices);

                            // Transform the vertices
                            for (int k = 0; k < attribute.getCount(); k++) {
                                Vector3 vt = Vector3Transform(
                                        new Vector3(
                                                model.meshes[meshIndex].vertices[3 * k],
                                                model.meshes[meshIndex].vertices[3 * k + 1],
                                                model.meshes[meshIndex].vertices[3 * k + 2]
                                        ),
                                        worldMatrix
                                );
                                model.meshes[meshIndex].vertices[3 * k] = vt.x;
                                model.meshes[meshIndex].vertices[3 * k + 1] = vt.y;
                                model.meshes[meshIndex].vertices[3 * k + 2] = vt.z;
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Vertices attribute data format not supported, use vec3 float");
                        }
                    }
                    // NORMAL
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("NORMAL")) {
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("NORMAL");

                        if ((attribute.getComponentDataType() == float.class) && (attribute.getElementType() == ElementType.VEC3)) {
                            // Init raylib mesh normals to copy glTF attribute data
                            model.meshes[meshIndex].normals = new float[attribute.getCount() * 3];

                            // Load 3 components of float data type into mesh.normals
                            attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(model.meshes[meshIndex].normals);

                            for (int k = 0; k < attribute.getCount(); k++) {
                                Vector3 nt = Vector3Transform(
                                        new Vector3(
                                                model.meshes[meshIndex].normals[3 * k],
                                                model.meshes[meshIndex].normals[3 * k + 1],
                                                model.meshes[meshIndex].normals[3 * k + 2]
                                        ),
                                        worldMatrixNormals
                                );
                                model.meshes[meshIndex].normals[3 * k] = nt.x;
                                model.meshes[meshIndex].normals[3 * k + 1] = nt.y;
                                model.meshes[meshIndex].normals[3 * k + 2] = nt.z;
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Normal attribute data format not supported, use vec3 float");
                        }
                    }
                    // TANGENT
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("TANGENT")) {
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("TANGENT");

                        if ((attribute.getComponentDataType() == float.class) && (attribute.getElementType() == ElementType.VEC4)) {
                            // Init raylib mesh tangent to copy glTF attribute data
                            model.meshes[meshIndex].tangents = new float[attribute.getCount() * 4];

                            // Load 4 components of float data type into mesh.tangents
                            attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(model.meshes[meshIndex].tangents);

                            for (int k = 0; k < attribute.getCount(); k++) {
                                Vector3 tt = Vector3Transform(
                                        new Vector3(
                                                model.meshes[meshIndex].tangents[3 * k],
                                                model.meshes[meshIndex].tangents[3 * k + 1],
                                                model.meshes[meshIndex].tangents[3 * k + 2]
                                        ),
                                        worldMatrix
                                );
                                model.meshes[meshIndex].tangents[3 * k] = tt.x;
                                model.meshes[meshIndex].tangents[3 * k + 1] = tt.y;
                                model.meshes[meshIndex].tangents[3 * k + 2] = tt.z;
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Tangent attribute data format not supported, use vec4 float");
                        }
                    }
                    // TEXCOORD_n, vec2, float/byte/short
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("TEXCOORD_0") || mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("TEXCOORD_1")) {
                        float[] texcoords = new float[0];
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("TEXCOORD_0");
                        if (attribute == null) {
                            attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("TEXCOORD_1");
                        }

                        if (attribute.getElementType() == ElementType.VEC2) {
                            if (attribute.getComponentDataType() == float.class) {
                                // Init raylib mesh texcoords to copy glTF attribute data
                                texcoords = new float[attribute.getCount() * 2];

                                // Load 2 components of float data type into mesh.texcoords
                                attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(texcoords);
                            }
                            else if (attribute.getComponentDataType() == byte.class) {
                                // Init raylib mesh texcoords to copy glTF attribute data
                                texcoords = new float[attribute.getCount() * 2];

                                // Load data into a temp buffer to be converted to raylib data type
                                byte[] tmp = new byte[attribute.getCount() * 2];
                                attribute.getBufferViewModel().getBufferViewData().get(tmp);

                                // Convert data to raylib texcoord data type (float)
                                for (int t = 0; t < attribute.getCount() * 2; t++) {
                                    texcoords[t] = (float) tmp[t] / 255;
                                }
                            }
                            else if (attribute.getComponentDataType() == short.class) {
                                // Init raylib mesh texcoords to copy glTF attribute data
                                texcoords = new float[attribute.getCount() * 2];

                                // Load data into a temp buffer to be converted to raylib data type
                                short[] tmp = new short[attribute.getCount() * 2];
                                attribute.getBufferViewModel().getBufferViewData().asShortBuffer().get(tmp);

                                // Convert data to raylib texcoord data type (float)
                                for (int t = 0; t < attribute.getCount() * 2; t++) {
                                    texcoords[t] = (float) tmp[t] / 65535;
                                }
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Texcoords attribute data format not supported");
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Texcoords attribute data format not supported, use vec2 float");
                        }

                        int index = mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("TEXCOORD_0") ? 0 :
                                mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("TEXCOORD_1") ? 1 : -1;
                        if (index == 0) {
                            model.meshes[meshIndex].texcoords = texcoords;
                        }
                        else if (index == 1) {
                            model.meshes[meshIndex].texcoords2 = texcoords;
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] No more than 2 texture coordinates attributes supported");
                        }
                    }
                    // COLOR_0
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().get("COLOR_0") != null) {
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("COLOR_0");

                        // WARNING: SPECS: All components of each COLOR_n accessor element MUST be clamped to [0.0, 1.0] range.

                        // RGB
                        if (attribute.getElementType() == ElementType.VEC3) {
                            if (attribute.getComponentDataType() == byte.class) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.getCount() * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                byte[] tmp = new byte[attribute.getCount() * 3];
                                attribute.getBufferViewModel().getBufferViewData().get(tmp);

                                //Convert data to raylib color data type (4 bytes)
                                for (int c = 0, k = 0; c < (attribute.getCount() * 4) - 3; c += 4, k += 3) {
                                    model.meshes[meshIndex].colors[c] = tmp[k];
                                    model.meshes[meshIndex].colors[c + 1] = tmp[k + 1];
                                    model.meshes[meshIndex].colors[c + 2] = tmp[k + 2];
                                    model.meshes[meshIndex].colors[c + 3] = (byte) 255;
                                }
                            }
                            else if (attribute.getComponentDataType() == short.class) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.getCount() * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                short[] tmp = new short[attribute.getCount() * 3];
                                attribute.getBufferViewModel().getBufferViewData().asShortBuffer().get(tmp);

                                //Convert data to raylib color data type (4 bytes)
                                for (int c = 0, k = 0; c < (attribute.getCount() * 4) - 3; c += 4, k += 3) {
                                    model.meshes[meshIndex].colors[c] = (byte) (((float) tmp[k] / 65535.0f) * 255.0f);
                                    model.meshes[meshIndex].colors[c + 1] = (byte) (((float) tmp[k + 1] / 65535.0f) * 255.0f);
                                    model.meshes[meshIndex].colors[c + 2] = (byte) (((float) tmp[k + 2] / 65535.0f) * 255.0f);
                                    model.meshes[meshIndex].colors[c + 3] = (byte) 255;
                                }
                            }
                            else if (attribute.getComponentDataType() == float.class) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.getCount() * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                float[] tmp = new float[attribute.getCount() * 3];
                                attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(tmp);

                                //Convert data to raylib color data type (4 bytes)
                                for (int c = 0, k = 0; c < (attribute.getCount() * 4) - 3; c += 4, k += 3) {
                                    model.meshes[meshIndex].colors[c] = (byte) (tmp[k] * 255);
                                    model.meshes[meshIndex].colors[c + 1] = (byte) (tmp[k + 1] * 255);
                                    model.meshes[meshIndex].colors[c + 2] = (byte) (tmp[k + 2] * 255);
                                    model.meshes[meshIndex].colors[c + 3] = (byte) 255;
                                }
                            }
                        }
                        // RGBA
                        else if (attribute.getElementType() == ElementType.VEC4) {
                            if (attribute.getComponentDataType() == byte.class) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.getCount() * 4];

                                // Load 4 components of unsigned char data type into mesh.colors
                                attribute.getBufferViewModel().getBufferViewData().get(model.meshes[meshIndex].colors);
                            }
                            else if (attribute.getComponentDataType() == short.class) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.getCount() * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                short[] tmp = new short[attribute.getCount() * 4];
                                attribute.getBufferViewModel().getBufferViewData().asShortBuffer().get(tmp);

                                //Convert data to raylib color data type (4 bytes)
                                for (int c = 0; c < (attribute.getCount() * 4); c++) {
                                    model.meshes[meshIndex].colors[c] = (byte) (((float) tmp[c] / 65535.0f) * 255.0f);
                                }
                            }
                            else if (attribute.getComponentDataType() == float.class) {
                                // Init raylib mesh color to copy glTF attribute data
                                model.meshes[meshIndex].colors = new byte[attribute.getCount() * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                float[] tmp = new float[attribute.getCount() * 4];
                                attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(tmp);

                                //Convert data to raylib color data type (4 bytes)
                                for (int c = 0, k = 0; c < (attribute.getCount() * 4); c++) {
                                    model.meshes[meshIndex].colors[c] = (byte) (tmp[k] * 255);
                                }
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Color attribute data format not supported");
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Color attribute data format not supported");
                        }
                    }
                }

                // Load primitive indices data (if provided)
                if (mesh.getMeshPrimitiveModels().get(p).getIndices() != null) {
                    AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getIndices();

                    model.meshes[meshIndex].triangleCount = attribute.getCount() / 3;

                    if (attribute.getComponentDataType() == short.class) {
                        // Init raylib mesh indices to copy glTF attribute data
                        model.meshes[meshIndex].indicesS = new short[attribute.getCount()];

                        // Load unsigned short data type into mesh.indices
                        attribute.getBufferViewModel().getBufferViewData().asShortBuffer().get(model.meshes[meshIndex].indicesS);
                    }
                    else if (attribute.getComponentDataType() == byte.class) {
                        // Init raylib mesh indices to copy glTF attribute data
                        model.meshes[meshIndex].indicesS = new short[attribute.getCount()];

                        // Load data into a temp buffer to be converted to raylib data type
                        byte[] temp = new byte[attribute.getCount()];

                        attribute.getBufferViewModel().getBufferViewData().get(temp);

                        // Convert data to raylib indices data type (unsigned short)
                        for (int d = 0; d < attribute.getCount(); d++) {
                            model.meshes[meshIndex].indicesS[d] = temp[d];
                        }
                    }
                    else if (attribute.getComponentDataType() == int.class) {
                        // Init raylib mesh indices to copy glTF attribute data
                        model.meshes[meshIndex].indicesS = new short[attribute.getCount()];

                        // Load data into a temp buffer to be converted to raylib data type
                        int[] temp = new int[attribute.getCount()];

                        attribute.getBufferViewModel().getBufferViewData().asIntBuffer().get(temp);

                        // Convert data to raylib indices data type (unsigned short)
                        for (int d = 0; d < attribute.getCount(); d++) {
                            model.meshes[meshIndex].indicesS[d] = (short) temp[d];
                        }

                        context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Indices data converted from u32 to u16, possible loss of data");
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Indices data format not supported, use u16");
                    }
                }
                else {
                    model.meshes[meshIndex].triangleCount = model.meshes[meshIndex].vertexCount / 3;    // Unindexed mesh
                }

                // Assign to the primitive mesh the corresponding material index
                // NOTE: If no material defined, mesh uses the already assigned default material (index: 0)
                for (int m = 0; m < gltf.getMaterialModels().size(); m++) {
                    // The primitive actually keeps the pointer to the corresponding material,
                    // raylib instead assigns to the mesh the by its index, as loaded in model.materials array
                    // To get the index, we check if material pointers match and we assign the corresponding index,
                    // skipping index 0, the default material
                    if (gltf.getMaterialModels().get(m) == mesh.getMeshPrimitiveModels().get(p).getMaterialModel()) {
                        model.meshMaterial[meshIndex] = m + 1;
                        break;
                    }
                }

                meshIndex++;       // Move to next mesh
            }
        }

        // Load glTF meshes animation data
        // REF: https://www.khronos.org/registry/glTF/specs/2.0/glTF-2.0.html#skins
        // REF: https://www.khronos.org/registry/glTF/specs/2.0/glTF-2.0.html#skinned-mesh-attributes
        //
        // LIMITATIONS:
        //  - Only supports 1 armature per file, and skips loading it if there are multiple armatures
        //  - Only supports linear interpolation (default method in Blender when checked "Always Sample Animations" when exporting a GLTF file)
        //  - Only supports translation/rotation/scale animation channel.path, weights not considered (i.e. morph targets)
        //----------------------------------------------------------------------------------------------------
        if (!gltf.getSkinModels().isEmpty()) {
            SkinModel skin = gltf.getSkinModels().get(0);
            model.skeleton.bones = LoadBoneInfoGLTF(skin);
            model.skeleton.boneCount = model.skeleton.bones.length;
            model.skeleton.bindPose = new Transform[model.skeleton.boneCount];

            for (int i = 0; i < model.skeleton.boneCount; i++) {
                model.skeleton.bindPose[i] = new Transform();
                float[] worldTransform = new float[16];
                skin.getJoints().get(i).computeGlobalTransform(worldTransform);
                Matrix worldMatrix = new Matrix(
                        worldTransform[0], worldTransform[4], worldTransform[8], worldTransform[12],
                        worldTransform[1], worldTransform[5], worldTransform[9], worldTransform[13],
                        worldTransform[2], worldTransform[6], worldTransform[10], worldTransform[14],
                        worldTransform[3], worldTransform[7], worldTransform[11], worldTransform[15]
                );
                MatrixDecompose(worldMatrix, model.skeleton.bindPose[i].translation, model.skeleton.bindPose[i].rotation, model.skeleton.bindPose[i].scale);
            }
        }

        if (gltf.getSkinModels().size() > 1) {
            context.tracelog.TRACELOG(LOG_ERROR, "MODEL: [" + fileName + "] can only load one skin (armature) per model, but gltf skins_count == " + gltf.getSkinModels().size());
        }

        for (int i = 0, meshIndex = 0; i < gltf.getMeshModels().size(); i++) {
            MeshModel mesh = gltf.getMeshModels().get(i);
            NodeModel node = gltf.getNodeModels().get(i);

            for (int p = 0; p < gltf.getMeshModels().get(i).getMeshPrimitiveModels().size(); p++) {
                boolean hasJoints = false;

                // NOTE: We only support primitives defined by triangles
                if (mesh.getMeshPrimitiveModels().get(p).getMode() != RL_TRIANGLES) {
                    continue;
                }

                for (int j = 0; j < mesh.getMeshPrimitiveModels().get(p).getAttributes().size(); j++) {
                    // NOTE: JOINTS_1 + WEIGHT_1 will be used for +4 joints influencing a vertex -> Not supported by raylib
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("JOINTS_0")) {
                        hasJoints = true;
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("JOINTS_0");

                        // JOINTS_n (vec4: 4 bones max per vertex / u8, u16)
                        // SPECS: https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#meshes-overview

                        // WARNING: raylib only supports model.meshes[].boneIds as u8 (unsigned char),
                        // if data is provided in any other format, it is converted to supported format but
                        // it could imply data loss (a warning message is issued in that case)

                        if (attribute.getElementType() == ElementType.VEC4) {
                            if (attribute.getComponentDataType() == byte.class) {
                                // Init raylib mesh bone ids to copy glTF attribute data
                                model.meshes[meshIndex].boneIndices = new byte[model.meshes[meshIndex].vertexCount * 4];

                                //Load attribute: vec4, byte
                                BufferViewModel bufferView = attribute.getBufferViewModel();
                                bufferView.getBufferViewData().get(model.meshes[meshIndex].boneIndices);
                            }
                            else if (attribute.getComponentDataType() == short.class) {
                                // Init raylib mesh bone ids to copy glTF attribute data
                                model.meshes[meshIndex].boneIndices = new byte[model.meshes[meshIndex].vertexCount * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                short[] tmp = new short[model.meshes[meshIndex].vertexCount * 4];
                                attribute.getBufferViewModel().getBufferViewData().asShortBuffer().get(tmp);

                                // Convert data to raylib data type
                                boolean boneIdOverflowWarning = false;
                                for (int b = 0; b < model.meshes[meshIndex].vertexCount * 4; b++) {
                                    if ((tmp[b] > 255) && !boneIdOverflowWarning) {
                                        context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Joint attribute data format (short) overflow");
                                        boneIdOverflowWarning = true;
                                    }

                                    // Cast to byte, regardless of overflow
                                    model.meshes[meshIndex].boneIndices[b] = (byte) tmp[b];
                                }
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Joint attribute data format not supported");
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Joint attribute data format not supported");
                        }
                    }
                    if (mesh.getMeshPrimitiveModels().get(p).getAttributes().containsKey("WEIGHTS_0")) {
                        // WEIGHTS_n (vec4 / u8, u16, f32)
                        AccessorModel attribute = mesh.getMeshPrimitiveModels().get(p).getAttributes().get("WEIGHTS_0");

                        if (attribute.getElementType() == ElementType.VEC4) {
                            if (attribute.getComponentDataType() == byte.class) {
                                // Init raylib bone weigths to copy glTF attribute data
                                model.meshes[meshIndex].boneWeights = new float[model.meshes[meshIndex].vertexCount * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                byte[] temp = new byte[model.meshes[meshIndex].vertexCount * 4];
                                attribute.getBufferViewModel().getBufferViewData().get(temp);

                                // Convert data to raylib bone weight data type
                                for (int b = 0; b < model.meshes[meshIndex].vertexCount * 4; b++) {
                                    model.meshes[meshIndex].boneWeights[b] = (float) temp[b] / 255;
                                }
                            }
                            else if (attribute.getComponentDataType() == short.class) {
                                // Init raylib bone weights to copy glTF attribute data
                                model.meshes[meshIndex].boneWeights = new float[model.meshes[meshIndex].vertexCount * 4];

                                // Load data into a temp buffer to be converted to raylib data type
                                short[] temp = new short[model.meshes[meshIndex].vertexCount * 4];
                                attribute.getBufferViewModel().getBufferViewData().asShortBuffer().get(temp);

                                // Convert data to raylib bone weight data type
                                for (int b = 0; b < model.meshes[meshIndex].vertexCount * 4; b++) {
                                    model.meshes[meshIndex].boneWeights[b] = (float) temp[b] / 65535;
                                }
                            }
                            else if (attribute.getComponentDataType() == float.class) {
                                // Init raylib mesh bone weight to copy glTF attribute data
                                model.meshes[meshIndex].boneWeights = new float[model.meshes[meshIndex].vertexCount * 4];

                                // Load 4 components of float data type into mesh.boneWeights
                                // for cgltf_attribute_type_weights we have:
                                //   - data.meshes[0] (256 vertices)
                                //   - 256 values, provided as cgltf_type_vec4 of float (4 byte per joint, stride 16)
                                attribute.getBufferViewModel().getBufferViewData().asFloatBuffer().get(model.meshes[meshIndex].boneWeights);
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Joint weight attribute data format not supported, use vec4 float");
                            }
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Joint weight attribute data format not supported, use vec4 float");
                        }
                    }
                }

                // Check if we are animated, and the mesh was not given any bone assignments, but is the child of a bone node
                // in this case we need to fully attach all the verts to the parent bone so it will animate with the bone
                if (!gltf.getSkinModels().isEmpty() && !hasJoints && node.getParent() != null && !node.getParent().getMeshModels().isEmpty()) {
                    int parentBoneId = -1;
                    for (int joint = 0; joint < model.skeleton.boneCount; joint++) {
                        if (gltf.getSkinModels().get(0).getJoints().get(joint) == node.getParent()) {
                            parentBoneId = joint;
                            break;
                        }
                    }

                    if (parentBoneId >= 0) {
                        model.meshes[meshIndex].boneIndices = new byte[model.meshes[meshIndex].vertexCount * 4];
                        model.meshes[meshIndex].boneWeights = new float[model.meshes[meshIndex].vertexCount * 4];

                        for (int vertexIndex = 0; vertexIndex < model.meshes[meshIndex].vertexCount * 4; vertexIndex += 4) {
                            model.meshes[meshIndex].boneIndices[vertexIndex] = (byte) parentBoneId;
                            model.meshes[meshIndex].boneWeights[vertexIndex] = 1.0f;
                        }
                    }
                }

                // Animated vertex data
                model.meshes[meshIndex].animVertices = new float[model.meshes[meshIndex].vertexCount * 3];
                System.arraycopy(model.meshes[meshIndex].vertices, 0, model.meshes[meshIndex].animVertices, 0, model.meshes[meshIndex].vertexCount * 3);
                model.meshes[meshIndex].animNormals = new float[model.meshes[meshIndex].vertexCount * 3];
                if (model.meshes[meshIndex].normals != null) {
                    System.arraycopy(model.meshes[meshIndex].normals, 0, model.meshes[meshIndex].animNormals, 0, model.meshes[meshIndex].vertexCount * 3);
                }

                // Bone Transform Matrices
                model.meshes[meshIndex].boneCount = model.skeleton.boneCount;
                model.meshes[meshIndex].boneMatrices = new Matrix[model.meshes[meshIndex].boneCount];

                for (int j = 0; j < model.meshes[meshIndex].boneCount; j++) {
                    model.meshes[meshIndex].boneMatrices[j] = MatrixIdentity();
                }
                meshIndex++;       // Move to next mesh
            }
        }

        return model;
    }

    /**
     * Get interpolated pose for bone sampler at a specific time. Returns true on success
     * @param interpolationType
     * @param input
     * @param output
     * @param time
     * @return
     */
    private float[] GetPoseAtTimeGLTF(AnimationModel.Interpolation interpolationType, AccessorModel input, AccessorModel output, float time) {
        float[] pose = new float[0];

        // Input and output should have the same count
        float tStart = 0;
        float tEnd = 0;
        int keyframe = 0;       // Defaults to first pose

        FloatBuffer inputData = input.getBufferViewModel().getBufferViewData().asFloatBuffer();
        FloatBuffer outputData = output.getBufferViewModel().getBufferViewData().asFloatBuffer();

        for (int i = 0; i < input.getCount() - 1; i++) {
            tStart = inputData.get(i);
            tEnd = inputData.get(i + 1);

            if ((tStart <= time) && (time < tEnd)) {
                keyframe = i;
                break;
            }
        }

        if (tStart == tEnd) {
            return null;
        }

        float duration = Math.max(tEnd - tStart, EPSILON);
        float t = (time - tStart) / duration;
        t = Math.max(t, 0.0f);
        t = Math.min(t, 1.0f);

        if (output.getComponentDataType() != float.class) {
            return null;
        }

        int advance = output.getPaddedElementSizeInBytes() / Float.BYTES;
        if (output.getElementType() == ElementType.VEC3) {
            Vector3 v1, v2, r;
            switch (interpolationType) {
                case STEP:
                    v1 = new Vector3(outputData.get(keyframe * advance), outputData.get(keyframe * advance + 1), outputData.get(keyframe * advance + 2));
                    pose = new float[]{v1.x, v1.y, v1.z};
                    break;
                case LINEAR:
                    v1 = new Vector3(outputData.get(keyframe * advance), outputData.get(keyframe * advance + 1), outputData.get(keyframe * advance + 2));
                    v2 = new Vector3(outputData.get(keyframe * advance + advance), outputData.get(keyframe * advance + advance + 1), outputData.get(keyframe * advance + advance + 2));

                    r = Vector3Lerp(v1, v2, t);
                    pose = new float[]{r.x, r.y, r.z};
                    break;
                case CUBICSPLINE:
                    break;
            }
        }
        else if (output.getElementType() == ElementType.VEC4) {
            Quaternion q1, q2, r;
            switch (interpolationType) {
                case STEP:
                    q1 = new Quaternion(outputData.get(keyframe), outputData.get(keyframe + 1), outputData.get(keyframe + 2), outputData.get(keyframe + 3));
                    pose = new float[]{q1.x, q1.y, q1.z, q1.w};
                    break;
                case LINEAR:
                    q1 = new Quaternion(outputData.get(keyframe * advance), outputData.get(keyframe * advance + 1), outputData.get(keyframe * advance + 2), outputData.get(keyframe * advance + 3));
                    q2 = new Quaternion(outputData.get(keyframe * advance + advance), outputData.get(keyframe * advance + advance + 1), outputData.get(keyframe * advance + advance + 2), outputData.get(keyframe * advance + advance + 3));

                    // Only v4 is for rotations, so we know it's a quat
                    r = QuaternionSlerp(q1, q2, t);
                    pose = new float[]{r.x, r.y, r.z, r.w};
                    break;
                case CUBICSPLINE:
                    break;
            }
        }

        return pose;
    }

    private static class Channels {
        AnimationModel.Channel translate;
        AnimationModel.Channel rotate;
        AnimationModel.Channel scale;
        AnimationModel.Interpolation interpolationType;

        Channels() {
        }
    }

    /**
     *
     * @param fileName
     * @return
     */
    private ModelAnimation[] LoadModelAnimationsGLTF(String fileName) {
        ModelAnimation[] animations = null;

        // glTF data loading
        GltfModel gltf;

        try {
            gltf = new GltfModelReader().read(Paths.get(fileName));
        }
        catch (IOException e) {
            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load glTF data");
            context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Failed to load animation buffers");

            return animations;
        }

        if (!gltf.getSkinModels().isEmpty()) {
            SkinModel skin = gltf.getSkinModels().get(0);
            animations = new ModelAnimation[gltf.getAnimationModels().size()];

            for (int i = 0; i < gltf.getAnimationModels().size(); i++) {
                animations[i] = new ModelAnimation();
                BoneInfo[] bones = LoadBoneInfoGLTF(skin);
                animations[i].boneCount = bones.length;

                AnimationModel animData = gltf.getAnimationModels().get(i);
                Channels[] boneChannels = new Channels[animations[i].boneCount];
                for (int j = 0; j < boneChannels.length; j++) {
                    boneChannels[j] = new Channels();
                }

                float animDuration = 0.0f;

                for (int j = 0; j < animData.getChannels().size(); j++) {
                    AnimationModel.Channel channel = animData.getChannels().get(j);
                    int boneIndex = -1;

                    for (int k = 0; k < skin.getJoints().size(); k++) {
                        if (skin.getJoints().get(k).equals(channel.getNodeModel())) {
                            boneIndex = k;
                            break;
                        }
                    }

                    if (boneIndex == -1) {
                        // Animation channel for a node not in the armature
                        continue;
                    }

                    boneChannels[boneIndex].interpolationType = animData.getChannels().get(j).getSampler().getInterpolation();

                    if (channel.getSampler().getInterpolation() == AnimationModel.Interpolation.LINEAR) {
                        if (channel.getPath().equalsIgnoreCase("TRANSLATION")) {
                            boneChannels[boneIndex].translate = channel;
                        }
                        else if (channel.getPath().equalsIgnoreCase("ROTATION")) {
                            boneChannels[boneIndex].rotate = channel;
                        }
                        else if (channel.getPath().equalsIgnoreCase("SCALE")) {
                            boneChannels[boneIndex].scale = channel;
                        }
                        else {
                            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Unsupported target_path on channel " + j + "'s sampler for animation " + i + ". Skipping.");
                        }
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Only linear interpolation curves are supported for GLTF animation.");
                    }

                    AccessorModel inputAccessor = channel.getSampler().getInput();
                    float t = inputAccessor.getBufferViewModel().getBufferViewData().getFloat(inputAccessor.getBufferViewModel().getByteLength() - Float.BYTES);

                    if (t <= 0f) {
                        context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load input time");
                        continue;
                    }

                    animDuration = Math.max(t, animDuration);
                }

                if (animData.getName() != null) {
                    animations[i].name = animData.getName();
                }

                animations[i].keyframeCount = (int) (((animDuration * 1000.0f) / GLTF_ANIMDELAY) + 1);
                animations[i].keyframePoses = new Transform[animations[i].keyframeCount][];

                for (int j = 0; j < animations[i].keyframeCount; j++) {
                    animations[i].keyframePoses[j] = new Transform[animations[i].boneCount];
                    float time = ((float) j * GLTF_ANIMDELAY) / 1000.0f;

                    for (int k = 0; k < animations[i].boneCount; k++) {
                        Vector3 translation = new Vector3(0, 0, 0);
                        Quaternion rotation = new Quaternion(0, 0, 0, 1);
                        Vector3 scale = new Vector3(1, 1, 1);

                        if (skin.getJoints().get(k).getTranslation() != null) {
                            translation = new Vector3(skin.getJoints().get(k).getTranslation()[0], skin.getJoints().get(k).getTranslation()[1], skin.getJoints().get(k).getTranslation()[2]);
                            float[] tTranslate = GetPoseAtTimeGLTF(boneChannels[k].interpolationType, boneChannels[k].translate.getSampler().getInput(), boneChannels[k].translate.getSampler().getOutput(), time);
                            if (tTranslate != null) {
                                translation = new Vector3(tTranslate[0], tTranslate[1], tTranslate[2]);
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Failed to load translate pose data for bone " + bones[k].name);
                            }
                        }

                        if (skin.getJoints().get(k).getRotation() != null) {
                            rotation = new Quaternion(skin.getJoints().get(k).getRotation()[0], skin.getJoints().get(k).getRotation()[1], skin.getJoints().get(k).getRotation()[2], skin.getJoints().get(k).getRotation()[3]);

                            float[] tRotate = GetPoseAtTimeGLTF(boneChannels[k].interpolationType, boneChannels[k].rotate.getSampler().getInput(), boneChannels[k].rotate.getSampler().getOutput(), time);
                            if (tRotate != null) {
                                rotation = new Quaternion(tRotate[0], tRotate[1], tRotate[2], tRotate[3]);
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Failed to load rotate pose data for bone " + bones[k].name);
                            }
                        }

                        if (skin.getJoints().get(k).getScale() != null) {
                            scale = new Vector3(skin.getJoints().get(k).getScale()[0], skin.getJoints().get(k).getScale()[1], skin.getJoints().get(k).getScale()[2]);

                            float[] tScale = GetPoseAtTimeGLTF(boneChannels[k].interpolationType, boneChannels[k].scale.getSampler().getInput(), boneChannels[k].scale.getSampler().getOutput(), time);
                            if (tScale != null) {
                                scale = new Vector3(tScale[0], tScale[1], tScale[2]);
                            }
                            else {
                                context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Failed to load scale pose data for bone " + bones[k].name);
                            }
                        }

                        animations[i].keyframePoses[j][k] = new Transform(translation, rotation, scale);
                    }

                    animations[i].keyframePoses[j] = BuildPoseFromParentJoints(bones, animations[i].keyframePoses[j]);
                }

                context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] Loaded animation: " + animData.getName() + " (" + animations[i].keyframeCount + " frames, " + animDuration + "s)");
            }
        }

        if (gltf.getSkinModels().size() > 1) {
            context.tracelog.TRACELOG(LOG_ERROR, "MODEL: [" + fileName + "] expected exactly one skin to load animation data from, but found " + gltf.getSkinModels().size());
        }

        return animations;
    }

    /**
     * Load VOX (MagicaVoxel) mesh data
     *
     * @param fileName
     * @return
     */
    private Model LoadVOX(String fileName) {
        Model model = new Model();

        int nbvertices = 0;
        int meshescount = 0;
        byte[] fileData = null;

        // Read vox file into buffer
        try {
            fileData = context.files.LoadFileData(fileName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (fileData == null) {
            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load VOX file");
            return model;
        }

        // Read and build voxarray description
        VoxLoader voxLoader = new VoxLoader();
        int ret = voxLoader.Vox_LoadFromMemory(fileData);
        VoxLoader.VoxArray3D voxarray = voxLoader.pvoxArray;

        if (ret != VoxLoader.VOX_SUCCESS) {
            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + fileName + "] Failed to load VOX data");
            return model;
        }
        else {
            // Success: Compute meshes count
            nbvertices = voxarray.vertices.used;
            meshescount = 1 + (nbvertices / 65536);

            context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + fileName + "] VOX data loaded successfully : " + nbvertices + " vertices/ " + meshescount + " meshes");
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
        System.arraycopy(voxarray.vertices.array, 0, pvertices, 0, pvertices.length);
        VoxLoader.VoxColor[] pcolors = new VoxLoader.VoxColor[voxarray.colors.array.length];
        System.arraycopy(voxarray.colors.array, 0, pcolors, 0, pvertices.length);

        short[] pindices = new short[voxarray.indices.array.length];    // 5461*6*6 = 196596 indices max per mesh
        System.arraycopy(voxarray.indices.array, 0, pindices, 0, pindices.length);

        int pverticesPointer = 0, pcolorsPointer = 0, pindicesPointer = 0;

        int size = 0;

        for (int i = 0; i < meshescount; i++) {
            Mesh pmesh = new Mesh();

            // Copy vertices
            pmesh.vertexCount = Math.min(verticesMax, verticesRemain);

            size = pmesh.vertexCount * 3;
            pmesh.vertices = new float[size];
            for (int j = 0, k = pverticesPointer; j < pmesh.vertices.length; j += 3, k++) {
                pmesh.vertices[j] = pvertices[k].x;
                pmesh.vertices[j + 1] = pvertices[k].y;
                pmesh.vertices[j + 2] = pvertices[k].z;
            }

            pmesh.texcoords = new float[(size / 3) * 2];
            Arrays.fill(pmesh.texcoords, 0f);

            // Copy indices
            // TODO: Compute globals indices array
            size = voxarray.indices.used;
            pmesh.indicesS = new short[size];
            System.arraycopy(pindices, 0, pmesh.indicesS, 0, pmesh.indicesS.length);

            pmesh.triangleCount = (pmesh.vertexCount / 4) * 2;

            // Copy colors
            size = pmesh.vertexCount * 4;
            pmesh.colors = new byte[size];
            for (int j = 0, k = pcolorsPointer; j < pmesh.colors.length; j += 4, k++) {
                pmesh.colors[j] = pcolors[k].r;
                pmesh.colors[j + 1] = pcolors[k].g;
                pmesh.colors[j + 2] = pcolors[k].b;
                pmesh.colors[j + 3] = pcolors[k].a;
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

        return model;
    }

    /**
     *
     * @param filePath
     * @return
     */
    private Model LoadM3D(String filePath) {
        M3DJ parser = new M3DJ();
        M3DJ_Model m3dj;

        try {
            m3dj = parser.LoadFile(filePath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        Model model = new Model();
        int i, j, k, l, n, mi = -2, vcolor;

        // no faces? this is probably just a material library
        if (m3dj.faces.isEmpty()) {
            return model;
        }

        if (!m3dj.materials.isEmpty()) {
            model.meshCount = model.materialCount = m3dj.materials.size();
            context.tracelog.TRACELOG(LOG_INFO, "MODEL: model has " + model.materialCount + " material meshes");
        }
        else {
            model.meshCount = 1;
            model.materialCount = 0;
            context.tracelog.TRACELOG(LOG_INFO, "MODEL: No materials, putting all meshes in a default material");
        }

        // We always need a default material, so we add +1
        model.materialCount++;

        // Faces must be in non-decreasing materialid order. Verify that quickly, sorting them otherwise
        // WARNING: Sorting is not needed, valid M3D model files should already be sorted
        // Just keeping the sorting function for reference (Check PR #3363 #3385)
        /*
        for (i = 1; i < m3d->numface; i++)
        {
            if (m3d->face[i-1].materialid <= m3d->face[i].materialid) continue;

            // face[i-1] > face[i].  slide face[i] lower
            m3df_t slider = m3d->face[i];
            j = i-1;

            do
            {   // face[j] > slider, face[j+1] is svailable vacant gap
                m3d->face[j+1] = m3d->face[j];
                j = j-1;
            }
            while (j >= 0 && m3d->face[j].materialid > slider.materialid);

            m3d->face[j+1] = slider;
        }
        */

        model.meshes = new Mesh[model.meshCount];
        for (int m = 0; m < model.meshes.length; m++) {
            model.meshes[m] = new Mesh();
        }

        model.meshMaterial = new int[model.meshCount];

        model.materials = new Material[model.materialCount + 1];
        for (int m = 0; m < model.materials.length; m++) {
            model.materials[m] = new Material();
        }

        // Map no material to index 0 with default shader, everything else materialId + 1
        model.materials[0] = context.models.LoadMaterialDefault();

        for (i = l = 0, k = -1; i < m3dj.faces.size(); i++, l++) {
            // Materials are grouped together
            if (mi != m3dj.faces.get(i).materialId) {
                // There should be only one material switch per material kind,
                // but be bulletproof for non-optimal model files
                if (k + 1 >= model.meshCount) {
                    model.meshCount++;

                    // Create a second buffer for mesh re-allocation
                    Mesh[] tempMeshes = new Mesh[model.meshCount];
                    System.arraycopy(model.meshes, 0, tempMeshes, 0, model.meshCount - 1);
                    model.meshes = tempMeshes;
                    for (int m = 0; m < model.meshes.length; m++) {
                        model.meshes[m] = new Mesh();
                    }

                    // Create a second buffer for material re-allocation
                    int[] tempMeshMaterial = new int[model.meshCount];
                    System.arraycopy(model.meshMaterial, 0, tempMeshMaterial, 0, model.meshCount - 1);
                    model.meshMaterial = tempMeshMaterial;
                }

                k++;
                mi = m3dj.faces.get(i).materialId;

                // Only allocate colors VertexBuffer if there's a color vertices in the model for this material batch
                // if all colors are fully transparent black for all vertices of this material, then we assume no vertices colors
                for (j = i, l = vcolor = 0; (j < m3dj.faces.size()) && (mi == m3dj.faces.get(j).materialId); j++, l++) {
                    if (
                            m3dj.vertices.get(m3dj.faces.get(j).vertices[0]).colorIndex != M3D_UNDEF ||
                                    m3dj.vertices.get(m3dj.faces.get(j).vertices[1]).colorIndex != M3D_UNDEF ||
                                    m3dj.vertices.get(m3dj.faces.get(j).vertices[2]).colorIndex != M3D_UNDEF
                    ) {
                        vcolor = 1;
                    }
                }

                model.meshes[k].vertexCount = l * 3;
                model.meshes[k].triangleCount = l;
                model.meshes[k].vertices = new float[model.meshes[k].vertexCount * 3];
                model.meshes[k].texcoords = new float[model.meshes[k].vertexCount * 2];
                model.meshes[k].normals = new float[model.meshes[k].vertexCount * 3];

                // If no map is provided, or we have colors defined, we allocate storage for vertices colors
                // M3D specs only consider vertices colors if no material is provided, however raylib uses both and mixes the colors
                if ((mi == M3D_UNDEF) || vcolor != 0) {
                    model.meshes[k].colors = new byte[model.meshes[k].vertexCount * 4];
                }

                // If no map is provided, and we allocated vertices colors, set them to white
                if ((mi == M3D_UNDEF) && (model.meshes[k].colors != null)) {
                    Arrays.fill(model.meshes[k].colors, (byte) 255);
                }

                if (!m3dj.bones.isEmpty() && !m3dj.skins.isEmpty()) {
                    model.meshes[k].boneIndices = new byte[model.meshes[k].vertexCount * 4];
                    model.meshes[k].boneWeights = new float[model.meshes[k].vertexCount * 4];
                    model.meshes[k].animVertices = new float[model.meshes[k].vertexCount * 3];
                    model.meshes[k].animNormals = new float[model.meshes[k].vertexCount * 3];
                }

                model.meshMaterial[k] = mi + 1;
                l = 0;
            }

            // Process meshes per material, add triangles
            model.meshes[k].vertices[l * 9 + 0] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).x * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 1] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).y * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 2] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).z * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 3] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).x * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 4] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).y * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 5] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).z * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 6] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).x * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 7] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).y * m3dj.header.scale);
            model.meshes[k].vertices[l * 9 + 8] = (float) (m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).z * m3dj.header.scale);

            // Without vertices color (full transparency), we use the default color
            if (model.meshes[k].colors != null && !m3dj.colors.isEmpty()) {
                if (m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).colorIndex).a > 0) {
                    model.meshes[k].colors[l * 12 + 0] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).colorIndex).a;
                    model.meshes[k].colors[l * 12 + 1] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).colorIndex).b;
                    model.meshes[k].colors[l * 12 + 2] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).colorIndex).g;
                    model.meshes[k].colors[l * 12 + 3] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[0]).colorIndex).r;
                }
                if (m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).colorIndex).a > 0) {
                    model.meshes[k].colors[l * 12 + 4] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).colorIndex).a;
                    model.meshes[k].colors[l * 12 + 5] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).colorIndex).b;
                    model.meshes[k].colors[l * 12 + 6] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).colorIndex).g;
                    model.meshes[k].colors[l * 12 + 7] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[1]).colorIndex).r;
                }
                if (m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).colorIndex).a > 0) {
                    model.meshes[k].colors[l * 12 + 8] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).colorIndex).a;
                    model.meshes[k].colors[l * 12 + 9] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).colorIndex).b;
                    model.meshes[k].colors[l * 12 + 10] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).colorIndex).g;
                    model.meshes[k].colors[l * 12 + 11] = (byte) m3dj.colors.get(m3dj.vertices.get(m3dj.faces.get(i).vertices[2]).colorIndex).r;
                }
            }

            if (m3dj.faces.get(i).texCoords[0] != M3D_UNDEF) {
                model.meshes[k].texcoords[l * 6 + 0] = (float) m3dj.textureMap.get(m3dj.faces.get(i).texCoords[0]).u;
                model.meshes[k].texcoords[l * 6 + 1] = (float) (1.0f - m3dj.textureMap.get(m3dj.faces.get(i).texCoords[0]).v);
                model.meshes[k].texcoords[l * 6 + 2] = (float) m3dj.textureMap.get(m3dj.faces.get(i).texCoords[1]).u;
                model.meshes[k].texcoords[l * 6 + 3] = (float) (1.0f - m3dj.textureMap.get(m3dj.faces.get(i).texCoords[1]).v);
                model.meshes[k].texcoords[l * 6 + 4] = (float) m3dj.textureMap.get(m3dj.faces.get(i).texCoords[2]).u;
                model.meshes[k].texcoords[l * 6 + 5] = (float) (1.0f - m3dj.textureMap.get(m3dj.faces.get(i).texCoords[2]).v);
            }

            if (m3dj.faces.get(i).normals[0] != M3D_UNDEF) {
                model.meshes[k].normals[l * 9 + 0] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[0]).x;
                model.meshes[k].normals[l * 9 + 1] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[0]).y;
                model.meshes[k].normals[l * 9 + 2] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[0]).z;
                model.meshes[k].normals[l * 9 + 3] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[1]).x;
                model.meshes[k].normals[l * 9 + 4] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[1]).y;
                model.meshes[k].normals[l * 9 + 5] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[1]).z;
                model.meshes[k].normals[l * 9 + 6] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[2]).x;
                model.meshes[k].normals[l * 9 + 7] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[2]).y;
                model.meshes[k].normals[l * 9 + 8] = (float) m3dj.vertices.get(m3dj.faces.get(i).normals[2]).z;
            }

            // Add skin (vertices / bone weight pairs)
            if (!m3dj.bones.isEmpty() && !m3dj.skins.isEmpty()) {
                for (n = 0; n < 3; n++) {
                    int skinId = m3dj.vertices.get(m3dj.faces.get(i).vertices[n]).skinIndex;

                    // Check if there is a skin for this mesh, should be, just failsafe
                    if ((skinId != M3D_UNDEF) && (skinId < m3dj.skins.size())) {
                        for (j = 0; j < 4; j++) {
                            model.meshes[k].boneIndices[l * 12 + n * 4 + j] = (byte) m3dj.skins.get(skinId).boneIds[j];
                            model.meshes[k].boneWeights[l * 12 + n * 4 + j] = m3dj.skins.get(skinId).weights[j];
                        }
                    }
                    else {
                        // raylib does not handle boneless meshes with skeletal animations, so
                        // we put all vertices without a bone into a special "no bone" bone
                        model.meshes[k].boneIndices[l * 12 + n * 4] = (byte) m3dj.bones.size();
                        model.meshes[k].boneWeights[l * 12 + n * 4] = 1.0f;
                    }
                }
            }
        }

        // Load materials
        for (i = 0; i < m3dj.materials.size(); i++) {
            model.materials[i + 1] = context.models.LoadMaterialDefault();

            for (j = 0; j < m3dj.materials.get(i).properties.size(); j++) {
                M3DJ_Property property = m3dj.materials.get(i).properties.get(j);

                if (property.key.equals("m3dp_Kd")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = context.textures.GetColor((int) property.GetPropertyValue());
                    model.materials[i + 1].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].value = 0.0f;
                }
                else if (property.key.equals("m3dp_Ks")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_SPECULAR.GetIndex()].color = context.textures.GetColor((int) property.GetPropertyValue());
                }
                else if (property.key.equals("m3dp_Ns")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_SPECULAR.GetIndex()].value = (int) property.GetPropertyValue();
                }
                else if (property.key.equals("m3dp_Ke")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_EMISSION.GetIndex()].color = context.textures.GetColor((int) property.GetPropertyValue());
                    model.materials[i + 1].maps[MATERIAL_MAP_EMISSION.GetIndex()].value = 0.0f;
                }
                else if (property.key.equals("m3dp_Pm")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_METALNESS.GetIndex()].value = (float) property.GetPropertyValue();
                }
                else if (property.key.equals("m3dp_Pr")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_ROUGHNESS.GetIndex()].value = (float) property.GetPropertyValue();
                }
                else if (property.key.equals("m3dp_Ps")) {
                    model.materials[i + 1].maps[MATERIAL_MAP_NORMAL.GetIndex()].color = WHITE;
                    model.materials[i + 1].maps[MATERIAL_MAP_NORMAL.GetIndex()].value = (float) property.GetPropertyValue();
                }
                else if (property.id >= 128) {
                    Image image = new Image();
                    image.data = m3dj.textures.get((int) property.GetPropertyValue()).textureData;
                    image.width = m3dj.textures.get((int) property.GetPropertyValue()).width;
                    image.height = m3dj.textures.get((int) property.GetPropertyValue()).height;
                    image.mipmaps = 1;
                    image.format =
                            (m3dj.textures.get((int) property.GetPropertyValue()).format == 4) ? PIXELFORMAT_UNCOMPRESSED_R8G8B8A8 :
                                    ((m3dj.textures.get((int) property.GetPropertyValue()).format == 3) ? PIXELFORMAT_UNCOMPRESSED_R8G8B8 :
                                            ((m3dj.textures.get((int) property.GetPropertyValue()).format == 2) ? PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA : PIXELFORMAT_UNCOMPRESSED_GRAYSCALE));

                    if (property.key.equals("m3dp_map_Kd")) {
                        model.materials[i + 1].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = context.textures.LoadTextureFromImage(image);
                    }
                    else if (property.key.equals("m3dp_map_Ks")) {
                        model.materials[i + 1].maps[MATERIAL_MAP_SPECULAR.GetIndex()].texture = context.textures.LoadTextureFromImage(image);
                    }
                    else if (property.key.equals("m3dp_map_Ke")) {
                        model.materials[i + 1].maps[MATERIAL_MAP_EMISSION.GetIndex()].texture = context.textures.LoadTextureFromImage(image);
                    }
                    else if (property.key.equals("m3dp_map_Km")) {
                        model.materials[i + 1].maps[MATERIAL_MAP_NORMAL.GetIndex()].texture = context.textures.LoadTextureFromImage(image);
                    }
                    else if (property.key.equals("m3dp_map_Ka")) {
                        model.materials[i + 1].maps[MATERIAL_MAP_OCCLUSION.GetIndex()].texture = context.textures.LoadTextureFromImage(image);
                    }
                    else if (property.key.equals("m3dp_map_Pm")) {
                        model.materials[i + 1].maps[MATERIAL_MAP_ROUGHNESS.GetIndex()].texture = context.textures.LoadTextureFromImage(image);
                    }
                }
            }
        }

        // Load bones
        if (!m3dj.bones.isEmpty()) {
            model.skeleton.boneCount = m3dj.bones.size() + 1;
            model.skeleton.bones = new BoneInfo[model.skeleton.boneCount];
            model.skeleton.bindPose = new Transform[model.skeleton.boneCount];

            for (i = 0; i < m3dj.bones.size(); i++) {
                model.skeleton.bones[i] = new BoneInfo(m3dj.bones.get(i).name, m3dj.bones.get(i).parentIndex);
                model.skeleton.bindPose[i] = new Transform();
                model.skeleton.bindPose[i].translation.x = (float) (m3dj.vertices.get(m3dj.bones.get(i).position).x * m3dj.header.scale);
                model.skeleton.bindPose[i].translation.y = (float) (m3dj.vertices.get(m3dj.bones.get(i).position).y * m3dj.header.scale);
                model.skeleton.bindPose[i].translation.z = (float) (m3dj.vertices.get(m3dj.bones.get(i).position).z * m3dj.header.scale);
                model.skeleton.bindPose[i].rotation.x = (float) m3dj.vertices.get(m3dj.bones.get(i).orientation).x;
                model.skeleton.bindPose[i].rotation.y = (float) m3dj.vertices.get(m3dj.bones.get(i).orientation).y;
                model.skeleton.bindPose[i].rotation.z = (float) m3dj.vertices.get(m3dj.bones.get(i).orientation).z;
                model.skeleton.bindPose[i].rotation.w = (float) m3dj.vertices.get(m3dj.bones.get(i).orientation).w;

                // TODO: If the orientation quaternion is not normalized, then that's encoding scaling
                model.skeleton.bindPose[i].rotation = QuaternionNormalize(model.skeleton.bindPose[i].rotation);
                model.skeleton.bindPose[i].scale.x = model.skeleton.bindPose[i].scale.y = model.skeleton.bindPose[i].scale.z = 1.0f;

                // Child bones are stored in parent bone relative space, convert that into model space
                if (model.skeleton.bones[i].parent >= 0) {
                    model.skeleton.bindPose[i].rotation = QuaternionMultiply(model.skeleton.bindPose[model.skeleton.bones[i].parent].rotation, model.skeleton.bindPose[i].rotation);
                    model.skeleton.bindPose[i].translation = Vector3RotateByQuaternion(model.skeleton.bindPose[i].translation, model.skeleton.bindPose[model.skeleton.bones[i].parent].rotation);
                    model.skeleton.bindPose[i].translation = Vector3Add(model.skeleton.bindPose[i].translation, model.skeleton.bindPose[model.skeleton.bones[i].parent].translation);
                    model.skeleton.bindPose[i].scale = Vector3Multiply(model.skeleton.bindPose[i].scale, model.skeleton.bindPose[model.skeleton.bones[i].parent].scale);
                }
            }

            // Add a special "no bone" bone
            model.skeleton.bones[i] = new BoneInfo("NO BONE", -1);
            model.skeleton.bindPose[i] = new Transform();
            model.skeleton.bindPose[i].translation.x = 0.0f;
            model.skeleton.bindPose[i].translation.y = 0.0f;
            model.skeleton.bindPose[i].translation.z = 0.0f;
            model.skeleton.bindPose[i].rotation.x = 0.0f;
            model.skeleton.bindPose[i].rotation.y = 0.0f;
            model.skeleton.bindPose[i].rotation.z = 0.0f;
            model.skeleton.bindPose[i].rotation.w = 1.0f;
            model.skeleton.bindPose[i].scale.x = model.skeleton.bindPose[i].scale.y = model.skeleton.bindPose[i].scale.z = 1.0f;
        }

        // Load bone-pose default mesh into animations[a] vertices. These will be updated when UpdateModelAnimation gets
        // called, but not before, however DrawMesh uses these if they exist (so not good if they are left empty)
        if (!m3dj.bones.isEmpty() && !m3dj.skins.isEmpty()) {
            for (i = 0; i < model.meshCount; i++) {
                System.arraycopy(model.meshes[i].vertices, 0, model.meshes[i].animVertices, 0, model.meshes[i].vertexCount * 3);
                System.arraycopy(model.meshes[i].normals, 0, model.meshes[i].animNormals, 0, model.meshes[i].vertexCount * 3);

                model.meshes[i].boneCount = model.skeleton.boneCount;
                model.meshes[i].boneMatrices = new Matrix[model.meshes[i].boneCount];
                for (int bm = 0; bm < model.meshes[i].boneMatrices.length; bm++) {
                    model.meshes[i].boneMatrices[bm] = MatrixIdentity();
                }
            }
        }

        return model;
    }

    /**
     *
     * @param filePath
     * @return
     */
    private ModelAnimation[] LoadModelAnimationsM3D(String filePath) {
        M3DJ parser = new M3DJ();
        M3DJ_Model m3dj;
        int i, j;

        try {
            m3dj = parser.LoadFile(filePath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (m3dj == null) {
            context.tracelog.TRACELOG(LOG_WARNING, "MODEL: [" + filePath + "] Failed to load M3D.");
            return null;
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + filePath + "] Loaded successfully: " +
                    m3dj.actions.size() + " animations[a](s), " + m3dj.bones.size() + " bones, " + m3dj.skins.size() + " skins.");
        }

        if (m3dj.actions.isEmpty() || m3dj.bones.isEmpty() || m3dj.skins.isEmpty()) {
            return null;
        }

        ModelAnimation[] animations = new ModelAnimation[m3dj.actions.size()];

        for (int a = 0; a < m3dj.actions.size(); a++) {
            animations[a] = new ModelAnimation();

            animations[a].keyframeCount = m3dj.actions.get(a).animationLength / M3D_ANIMDELAY;
            animations[a].boneCount = m3dj.bones.size() + 1;
            BoneInfo[] bones = new BoneInfo[animations[a].boneCount];
            animations[a].keyframePoses = new Transform[animations[a].keyframeCount][];

            context.tracelog.TRACELOG(LOG_INFO, "MODEL: [" + filePath + "] animations[a] #" + a + ": " + m3dj.actions.get(a).animationLength +
                    " msec, " + animations[a].keyframeCount + " frames.");

            for (i = 0; i < m3dj.bones.size(); i++) {
                bones[i] = new BoneInfo(m3dj.bones.get(i).name, m3dj.bones.get(i).parentIndex);
            }

            // A special, never transformed "no bone" bone, used for boneless vertices
            bones[i] = new BoneInfo("NO BONE", -1);

            // M3D stores frames at arbitrary intervals with sparse skeletons. We need full skeletons at
            // regular intervals, so let the M3D SDK do the heavy lifting and calculate interpolated bones
            for (i = 0; i < animations[a].keyframeCount; i++) {
                animations[a].keyframePoses[i] = new Transform[m3dj.bones.size() + 1];

                M3DJ_Bone[] pose = parser.Pose(m3dj, a, i * M3D_ANIMDELAY);

                if (pose != null) {
                    for (j = 0; j < m3dj.bones.size(); j++) {
                        animations[a].keyframePoses[i][j] = new Transform();
                        animations[a].keyframePoses[i][j].translation.x = (float) (m3dj.vertices.get(pose[j].position).x * m3dj.header.scale);
                        animations[a].keyframePoses[i][j].translation.y = (float) (m3dj.vertices.get(pose[j].position).y * m3dj.header.scale);
                        animations[a].keyframePoses[i][j].translation.z = (float) (m3dj.vertices.get(pose[j].position).z * m3dj.header.scale);
                        animations[a].keyframePoses[i][j].rotation.x = (float) m3dj.vertices.get(pose[j].orientation).x;
                        animations[a].keyframePoses[i][j].rotation.y = (float) m3dj.vertices.get(pose[j].orientation).y;
                        animations[a].keyframePoses[i][j].rotation.z = (float) m3dj.vertices.get(pose[j].orientation).z;
                        animations[a].keyframePoses[i][j].rotation.w = (float) m3dj.vertices.get(pose[j].orientation).w;
                        animations[a].keyframePoses[i][j].rotation = QuaternionNormalize(animations[a].keyframePoses[i][j].rotation);
                        animations[a].keyframePoses[i][j].scale.x = 1.0f;
                        animations[a].keyframePoses[i][j].scale.y = 1.0f;
                        animations[a].keyframePoses[i][j].scale.z = 1.0f;

                        // Child bones are stored in parent bone relative space, convert that into model space
                        if (bones[j].parent >= 0) {
                            animations[a].keyframePoses[i][j].rotation = QuaternionMultiply(animations[a].keyframePoses[i][bones[j].parent].rotation, animations[a].keyframePoses[i][j].rotation);
                            animations[a].keyframePoses[i][j].translation = Vector3RotateByQuaternion(animations[a].keyframePoses[i][j].translation, animations[a].keyframePoses[i][bones[j].parent].rotation);
                            animations[a].keyframePoses[i][j].translation = Vector3Add(animations[a].keyframePoses[i][j].translation, animations[a].keyframePoses[i][bones[j].parent].translation);
                            animations[a].keyframePoses[i][j].scale = Vector3Multiply(animations[a].keyframePoses[i][j].scale, animations[a].keyframePoses[i][bones[j].parent].scale);
                        }
                    }

                    // Default transform for the "no bone" bone
                    animations[a].keyframePoses[i][j] = new Transform();
                    animations[a].keyframePoses[i][j].translation.x = 0.0f;
                    animations[a].keyframePoses[i][j].translation.y = 0.0f;
                    animations[a].keyframePoses[i][j].translation.z = 0.0f;
                    animations[a].keyframePoses[i][j].rotation.x = 0.0f;
                    animations[a].keyframePoses[i][j].rotation.y = 0.0f;
                    animations[a].keyframePoses[i][j].rotation.z = 0.0f;
                    animations[a].keyframePoses[i][j].rotation.w = 1.0f;
                    animations[a].keyframePoses[i][j].scale.x = 1.0f;
                    animations[a].keyframePoses[i][j].scale.y = 1.0f;
                    animations[a].keyframePoses[i][j].scale.z = 1.0f;
                }
            }
        }

        return animations;
    }

}
