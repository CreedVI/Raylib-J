package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.extras.rLights;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.extras.rLights.LIGHT_DIRECTIONAL;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC3;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC4;
import static com.raylib.java.structs.Color.*;

public class MeshInstancing {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Mesh instancing
     *
     *   Example originally created with raylib 3.7, last time updated with raylib 4.2
     *
     *   Example contributed by @seanpringle and reviewed by Max (@moliad) and Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2020-2023 @seanpringle, Max (@moliad) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static final int MAX_INSTANCES = 10000;

    private static final int GLSL_VERSION = 330;
    // private static final int GLSL_VERSION = 110;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - mesh instancing");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(-125.0f, 125.0f, -125.0f);    // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);              // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);                  // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                        // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                     // Camera projection type

        // Define mesh to be instanced
        Mesh cube = rlj.models.GenMeshCube(1.0f, 1.0f, 1.0f);

        // Define transforms to be uploaded to GPU for instances
        Matrix[] transforms = new Matrix[MAX_INSTANCES];   // Pre-multiplied transformations passed to rlgl

        // Translate and rotate cubes randomly
        for (int i = 0; i < MAX_INSTANCES; i++) {
            Matrix translation = MatrixTranslate((float)rlj.core.GetRandomValue(-50, 50), (float)rlj.core.GetRandomValue(-50, 50), (float)rlj.core.GetRandomValue(-50, 50));
            Vector3 axis = Vector3Normalize(new Vector3((float)rlj.core.GetRandomValue(0, 360), (float)rlj.core.GetRandomValue(0, 360), (float)rlj.core.GetRandomValue(0, 360)));
            float angle = (float)rlj.core.GetRandomValue(0, 10)*DEG2RAD;
            Matrix rotation = MatrixRotate(axis, angle);

            transforms[i] = MatrixMultiply(rotation, translation);
        }

        // Load lighting shader
        Shader shader = rlj.core.LoadShader(rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/lighting_instancing.vs", GLSL_VERSION),
                                   rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/lighting.fs", GLSL_VERSION));
        // Get shader locations
        shader.locs[SHADER_LOC_MATRIX_MVP.GetLocation()] = rlj.core.GetShaderLocation(shader, "mvp");
        shader.locs[SHADER_LOC_VECTOR_VIEW.GetLocation()] = rlj.core.GetShaderLocation(shader, "viewPos");
        shader.locs[SHADER_LOC_MATRIX_MODEL.GetLocation()] = rlj.core.GetShaderLocationAttrib(shader, "instanceTransform");

        // Set shader value: ambient light level
        int ambientLoc = rlj.core.GetShaderLocation(shader, "ambient");
        rlj.core.SetShaderValue(shader, ambientLoc, new float[]{ 0.2f, 0.2f, 0.2f, 1.0f }, SHADER_UNIFORM_VEC4);

        // Create one light
        rLights.CreateLight(rlj, LIGHT_DIRECTIONAL, new Vector3(50.0f, 50.0f, 0.0f), Vector3Zero(), WHITE, shader);

        // NOTE: We are assigning the intancing shader to material.shader
        // to be used on mesh drawing with DrawMeshInstanced()
        Material matInstances = rlj.models.LoadMaterialDefault();
        matInstances.shader = shader;
        matInstances.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = RED;

        // Load default material (using raylib intenral default shader) for non-instanced mesh drawing
        // WARNING: Default shader enables vertex color attribute BUT GenMeshCube() does not generate vertex colors, so,
        // when drawing the color attribute is disabled and a default color value is provided as input for the vertex attribute
        Material matDefault = rlj.models.LoadMaterialDefault();
        matDefault.maps[MATERIAL_MAP_DIFFUSE.GetIndex()].color = BLUE;

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);

            // Update the light shader with the camera view position
            float[] cameraPos = { camera.position.x, camera.position.y, camera.position.z };
            rlj.core.SetShaderValue(shader, shader.locs[SHADER_LOC_VECTOR_VIEW.GetLocation()], cameraPos, SHADER_UNIFORM_VEC3);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // Draw cube mesh with default material (BLUE)
            rlj.models.DrawMesh(cube, matDefault, MatrixTranslate(-10.0f, 0.0f, 0.0f));

            // Draw meshes instanced using material containing instancing shader (RED + lighting),
            // transforms[] for the instances should be provided, they are dynamically
            // updated in GPU every frame, so we can animate the different mesh instances
            rlj.models.DrawMeshInstanced(cube, matInstances, transforms, MAX_INSTANCES);

            // Draw cube mesh with default material (BLUE)
            rlj.models.DrawMesh(cube, matDefault, MatrixTranslate(10.0f, 0.0f, 0.0f));

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------

        rlj.core.CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }

}
