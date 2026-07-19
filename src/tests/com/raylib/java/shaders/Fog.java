package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.extras.rLights;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Model;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.core.input.Keyboard.KEY_DOWN;
import static com.raylib.java.core.input.Keyboard.KEY_UP;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.SHADER_LOC_MATRIX_MODEL;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.SHADER_LOC_VECTOR_VIEW;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.*;
import static com.raylib.java.structs.Color.*;

public class Fog {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - fog
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3).
     *
     *   Example originally created with raylib 2.5, last time updated with raylib 3.7
     *
     *   Example contributed by Chris Camacho (@chriscamacho) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2019-2023 Chris Camacho (@chriscamacho) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    static final int GLSL_VERSION = 330;
    // static final int GLSL_VERSION = 110;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT);  // Enable Multi Sampling Anti Aliasing 4x (if available)
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shaders] example - fog");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(2.0f, 2.0f, 6.0f);    // Camera position
        camera.target = new Vector3(0.0f, 0.5f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Load models and texture
        Model modelA = rlj.models.LoadModelFromMesh(rlj.models.GenMeshTorus(0.4f, 1.0f, 16, 32));
        Model modelB = rlj.models.LoadModelFromMesh(rlj.models.GenMeshCube(1.0f, 1.0f, 1.0f));
        Model modelC = rlj.models.LoadModelFromMesh(rlj.models.GenMeshSphere(0.5f, 32, 32));
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/shaders/texel_checker.png");

        // Assign texture to default model material
        modelA.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;
        modelB.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;
        modelC.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;

        // Load shader and set up some uniforms
        Shader shader = rlj.core.LoadShader(rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/lighting.vs", GLSL_VERSION),
                                            rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/fog.fs", GLSL_VERSION));
        shader.locs[SHADER_LOC_MATRIX_MODEL.GetLocation()] = rlj.core.GetShaderLocation(shader, "matModel");
        shader.locs[SHADER_LOC_VECTOR_VIEW.GetLocation()] = rlj.core.GetShaderLocation(shader, "viewPos");

        // Ambient light level
        int ambientLoc = rlj.core.GetShaderLocation(shader, "ambient");
        rlj.core.SetShaderValue(shader, ambientLoc, new float[]{ 0.2f, 0.2f, 0.2f, 1.0f }, SHADER_UNIFORM_VEC4);

        float fogDensity = 0.15f;
        int fogDensityLoc = rlj.core.GetShaderLocation(shader, "fogDensity");
        rlj.core.SetShaderValue(shader, fogDensityLoc, new float[]{fogDensity}, SHADER_UNIFORM_FLOAT);

        // NOTE: All models share the same shader
        modelA.materials[0].shader = shader;
        modelB.materials[0].shader = shader;
        modelC.materials[0].shader = shader;

        // Using just 1 point lights
        rLights.CreateLight(rlj, rLights.LIGHT_POINT, new Vector3(0, 2, 6), Raymath.Vector3Zero(), WHITE, shader);

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);

            if (rlj.core.IsKeyDown(KEY_UP)) {
                fogDensity += 0.001f;
                if (fogDensity > 1.0f) fogDensity = 1.0f;
            }

            if (rlj.core.IsKeyDown(KEY_DOWN)) {
                fogDensity -= 0.001f;
                if (fogDensity < 0.0f) fogDensity = 0.0f;
            }

            rlj.core.SetShaderValue(shader, fogDensityLoc, new float[]{fogDensity}, SHADER_UNIFORM_FLOAT);

            // Rotate the torus
            modelA.transform = Raymath.MatrixMultiply(modelA.transform, Raymath.MatrixRotateX(-0.025f));
            modelA.transform = Raymath.MatrixMultiply(modelA.transform, Raymath.MatrixRotateZ(0.012f));

            // Update the light shader with the camera view position
            rlj.core.SetShaderValue(shader, shader.locs[SHADER_LOC_VECTOR_VIEW.GetLocation()], new float[]{camera.position.x, 0, 0}, SHADER_UNIFORM_VEC3);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(GRAY);

            rlj.core.BeginMode3D(camera);

            // Draw the three models
            rlj.models.DrawModel(modelA, Raymath.Vector3Zero(), 1.0f, WHITE);
            rlj.models.DrawModel(modelB, new Vector3(-2.6f, 0, 0), 1.0f, WHITE);
            rlj.models.DrawModel(modelC, new Vector3(2.6f, 0, 0), 1.0f, WHITE);

            for (int i = -20; i < 20; i += 2) {
                rlj.models.DrawModel(modelA, new Vector3(i, 0, 2), 1.0f, WHITE);
            }

            rlj.core.EndMode3D();

            rlj.text.DrawText(rlj.text.TextFormat("Use KEY_UP/KEY_DOWN to change fog density [%.2f]", fogDensity), 10, 10, 20, RAYWHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.models.UnloadModel(modelA);        // Unload the model A
        rlj.models.UnloadModel(modelB);        // Unload the model B
        rlj.models.UnloadModel(modelC);        // Unload the model C
        rlj.textures.UnloadTexture(texture);     // Unload the texture
        rlj.core.UnloadShader(shader);       // Unload shader

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
