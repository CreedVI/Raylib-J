package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.BoundingBox;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Model;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static com.raylib.java.raymath.Raymath.MatrixTranslate;
import static com.raylib.java.structs.Color.*;

public class ModelLoading_VOX {

    /*******************************************************************************************
     *
     *   raylib [models] example - Load models vox (MagicaVoxel)
     *
     *   Example originally created with raylib 4.0, last time updated with raylib 4.0
     *
     *   Example contributed by Johann Nadalutti (@procfxgen) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2021-2023 Johann Nadalutti (@procfxgen) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static final int MAX_VOX_FILES = 3;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        String[] voxFileNames = {
                "src/tests/resources/models/models/vox/chr_knight.vox",
                "src/tests/resources/models/models/vox/chr_sword.vox",
                "src/tests/resources/models/models/vox/monu9.vox"
        };

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - magicavoxel loading");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Load MagicaVoxel files
        Model[] models = new Model[MAX_VOX_FILES];

        for (int i = 0; i < MAX_VOX_FILES; i++) {
            // Load VOX file and measure time
            double t0 = rlj.core.GetTime()*1000.0;
            models[i] = rlj.models.LoadModel(voxFileNames[i]);
            double t1 = rlj.core.GetTime()*1000.0;

            rlj.traceLog.TRACELOG(LOG_WARNING, rlj.text.TextFormat("[%s] File loaded in %.3f ms", voxFileNames[i], t1 - t0));

            // Compute model translation matrix to center model on draw position (0, 0 , 0)
            BoundingBox bb = rlj.models.GetModelBoundingBox(models[i]);
            Vector3 center = new Vector3();
            center.x = bb.min.x  + (((bb.max.x - bb.min.x)/2));
            center.z = bb.min.z  + (((bb.max.z - bb.min.z)/2));

            Matrix matTranslate = MatrixTranslate(-center.x, 0, -center.z);
            models[i].transform = matTranslate;
        }

        int currentModel = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);

            // Cycle between models on mouse click
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentModel = (currentModel + 1)%MAX_VOX_FILES;
            }

            // Cycle between models on key pressed
            if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
                currentModel++;
                if (currentModel >= MAX_VOX_FILES) {
                    currentModel = 0;
                }
            }
            else if (rlj.core.IsKeyPressed(KEY_LEFT)) {
                currentModel--;
                if (currentModel < 0) {
                    currentModel = MAX_VOX_FILES - 1;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            // Draw 3D model
            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(models[currentModel], new Vector3(), 1.0f, WHITE);
            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            // Display info
            rlj.shapes.DrawRectangle(10, 400, 310, 30, rlj.textures.Fade(SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines(10, 400, 310, 30, rlj.textures.Fade(DARKBLUE, 0.5f));
            rlj.text.DrawText("MOUSE LEFT BUTTON to CYCLE VOX MODELS", 40, 410, 10, BLUE);
            rlj.text.DrawText(rlj.text.TextFormat("File: %s", rlj.core.GetFileName(voxFileNames[currentModel])), 10, 10, 20, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        // Unload models data (GPU VRAM)
        for (int i = 0; i < MAX_VOX_FILES; i++) {
            rlj.models.UnloadModel(models[i]);
        }

        rlj.core.CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }
}
