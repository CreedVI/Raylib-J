package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Model;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.raymath.Raymath.MatrixRotateXYZ;
import static com.raylib.java.structs.Color.*;

public class YawPitchRoll {

    /*******************************************************************************************
     *
     *   raylib [models] example - Plane rotations (yaw, pitch, roll)
     *
     *   Example originally created with raylib 1.8, last time updated with raylib 4.0
     *
     *   Example contributed by Berni (@Berni8k) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2017-2023 Berni (@Berni8k) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        //SetConfigFlags(FLAG_MSAA_4X_HINT | FLAG_WINDOW_HIGHDPI);
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - plane rotations (yaw, pitch, roll)");

        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 50.0f, -120.0f);// Camera position perspective
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 30.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera type

        Model model = rlj.models.LoadModel("src/tests/resources/models/models/obj/plane.obj");                  // Load model
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/models/models/obj/plane_diffuse.png");  // Load model texture
        model.materials[0].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = texture;            // Set map diffuse texture

        float pitch = 0.0f;
        float roll = 0.0f;
        float yaw = 0.0f;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Plane pitch (x-axis) controls
            if (rlj.core.IsKeyDown(KEY_DOWN)) {
                pitch += 0.6f;
            }
            else if (rlj.core.IsKeyDown(KEY_UP)) {
                pitch -= 0.6f;
            }
            else {
                if (pitch > 0.3f) {
                    pitch -= 0.3f;
                }
                else if (pitch < -0.3f) {
                    pitch += 0.3f;
                }
            }

            // Plane yaw (y-axis) controls
            if (rlj.core.IsKeyDown(KEY_S)) {
                yaw -= 1.0f;
            }
            else if (rlj.core.IsKeyDown(KEY_A)) {
                yaw += 1.0f;
            }
            else {
                if (yaw > 0.0f) {
                    yaw -= 0.5f;
                }
                else if (yaw < 0.0f) {
                    yaw += 0.5f;
                }
            }

            // Plane roll (z-axis) controls
            if (rlj.core.IsKeyDown(KEY_LEFT)) {
                roll -= 1.0f;
            }
            else if (rlj.core.IsKeyDown(KEY_RIGHT)) {
                roll += 1.0f;
            }
            else {
                if (roll > 0.0f) {
                    roll -= 0.5f;
                }
                else if (roll < 0.0f) {
                    roll += 0.5f;
                }
            }

            // Tranformation matrix for rotations
            model.transform = MatrixRotateXYZ(new Vector3(DEG2RAD*pitch, DEG2RAD*yaw, DEG2RAD*roll));
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            // Draw 3D model (recomended to draw 3D always before 2D)
            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(model, new Vector3(0.0f, -8.0f, 0.0f), 1.0f, WHITE);   // Draw 3d model with texture
            rlj.models.DrawGrid(10, 10.0f);

            rlj.core.EndMode3D();

            // Draw controls info
            rlj.shapes.DrawRectangle(30, 370, 260, 70, rlj.textures.Fade(GREEN, 0.5f));
            rlj.shapes.DrawRectangleLines(30, 370, 260, 70, rlj.textures.Fade(DARKGREEN, 0.5f));
            rlj.text.DrawText("Pitch controlled with: KEY_UP / KEY_DOWN", 40, 380, 10, DARKGRAY);
            rlj.text.DrawText("Roll controlled with: KEY_LEFT / KEY_RIGHT", 40, 400, 10, DARKGRAY);
            rlj.text.DrawText("Yaw controlled with: KEY_A / KEY_S", 40, 420, 10, DARKGRAY);

            rlj.text.DrawText("(c) WWI Plane Model created by GiaHanLam", screenWidth - 240, screenHeight - 20, 10, DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.models.UnloadModel(model);     // Unload model data

        rlj.core.CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
