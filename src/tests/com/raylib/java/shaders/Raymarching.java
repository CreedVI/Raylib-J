package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_RESIZABLE;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.*;
import static com.raylib.java.structs.Color.*;

public class Raymarching {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Raymarching shapes generation
     *
     *   NOTE: This example requires raylib OpenGL 3.3 for shaders support and only #version 330
     *         is currently supported. OpenGL ES 2.0 platforms are not supported at the moment.
     *
     *   Example originally created with raylib 2.0, last time updated with raylib 4.2
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2018-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

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

        Raylib rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shaders] example - raymarching shapes");

        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(2.5f, 2.5f, 3.0f);    // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.7f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 65.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Load raymarching shader
        // NOTE: Defining 0 (NULL) for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/raymarching.fs", GLSL_VERSION));

        // Get shader locations for required uniforms
        int viewEyeLoc = rlj.core.GetShaderLocation(shader, "viewEye");
        int viewCenterLoc = rlj.core.GetShaderLocation(shader, "viewCenter");
        int runTimeLoc = rlj.core.GetShaderLocation(shader, "runTime");
        int resolutionLoc = rlj.core.GetShaderLocation(shader, "resolution");

        float[] resolution = { (float)screenWidth, (float)screenHeight };
        rlj.core.SetShaderValue(shader, resolutionLoc, resolution, RL_SHADER_UNIFORM_VEC2);

        float runTime = 0.0f;

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window
        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);

            float[] cameraPos = { camera.position.x, camera.position.y, camera.position.z };
            float[] cameraTarget = { camera.target.x, camera.target.y, camera.target.z };

            float deltaTime = rlj.core.GetFrameTime();
            runTime += deltaTime;

            // Set shader required uniform values
            rlj.core.SetShaderValue(shader, viewEyeLoc, cameraPos, RL_SHADER_UNIFORM_VEC3);
            rlj.core.SetShaderValue(shader, viewCenterLoc, cameraTarget, RL_SHADER_UNIFORM_VEC3);
            rlj.core.SetShaderValue(shader, runTimeLoc, new float[] {runTime}, RL_SHADER_UNIFORM_FLOAT);

            // Check if screen is resized
            if (rlj.core.IsWindowResized()) {
                resolution = new float[]{(float) rlj.core.GetScreenWidth(), (float) rlj.core.GetScreenHeight()};
                rlj.core.SetShaderValue(shader, resolutionLoc, resolution, RL_SHADER_UNIFORM_VEC2);
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            // We only draw a white full-screen rectangle,
            // frame is generated in shader using raymarching
            rlj.core.BeginShaderMode(shader);
            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight(), WHITE);
            rlj.core.EndShaderMode();

            rlj.text.DrawText("(c) Raymarching shader by Iñigo Quilez. MIT License.", rlj.core.GetScreenWidth() - 280, rlj.core.GetScreenHeight() - 20, 10, BLACK);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);           // Unload shader

        rlj.core.CloseWindow();                  // Close window and OpenGL context
        //--------------------------------------------------------------------------------------

    }

}
