package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Model;
import com.raylib.java.structs.ModelAnimation;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.*;

public class ModelLoading_M3D {

    /*******************************************************************************************
     *
     *   raylib [models] example - Load models M3D
     *
     *   Example originally created with raylib 4.5, last time updated with raylib 4.5
     *
     *   Example contributed by bzt (@bztsrc) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   NOTES:
     *     - Model3D (M3D) fileformat specs: https://gitlab.com/bztsrc/model3d
     *     - Bender M3D exported: https://gitlab.com/bztsrc/model3d/-/tree/master/blender
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2022-2023 bzt (@bztsrc)
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - M3D model loading");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(1.5f, 1.5f, 1.5f);    // Camera position
        camera.target = new Vector3(0.0f, 0.4f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Vector3 position = new Vector3();            // Set model position

        String modelFileName = "src/tests/resources/models/models/m3d/cesium_man.m3d";
        boolean drawMesh = true;
        boolean drawSkeleton = true;
        boolean animPlaying = false;   // Store anim state, what to draw

        // Load model
        Model model = rlj.models.LoadModel(modelFileName); // Load the bind-pose model mesh and basic data

        // Load animations
        int animsCount = 0;
        int animFrameCounter = 0, animId = 0;
        ModelAnimation[] anims = rlj.models.LoadModelAnimations(modelFileName); // Load skeletal animation data
        animsCount = anims.length;
        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);

            if (animsCount > 0) {
                // Play animation when spacebar is held down (or step one frame with N)
                if (rlj.core.IsKeyDown(KEY_SPACE) || rlj.core.IsKeyPressed(KEY_N)) {
                    animFrameCounter++;

                    if (animFrameCounter >= anims[animId].frameCount) {
                        animFrameCounter = 0;
                    }

                    rlj.models.UpdateModelAnimation(model, anims[animId], animFrameCounter);
                    animPlaying = true;
                }

                // Select animation by pressing A
                if (rlj.core.IsKeyPressed(KEY_COMMA)) {
                    animFrameCounter = 0;
                    animId++;

                    if (animId >= animsCount) {
                        animId = 0;
                    }
                    rlj.models.UpdateModelAnimation(model, anims[animId], 0);
                    animPlaying = true;
                }
            }

            // Toggle skeleton drawing
            if (rlj.core.IsKeyPressed(KEY_S)) {
                drawSkeleton = !drawSkeleton;
            }

            // Toggle mesh drawing
            if (rlj.core.IsKeyPressed(KEY_M)) {
                drawMesh = !drawMesh;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // Draw 3d model with texture
            if (drawMesh) {
                rlj.models.DrawModel(model, position, 1.0f, WHITE);
            }

            // Draw the animated skeleton
            if (drawSkeleton) {
                // Loop to (boneCount - 1) because the last one is a special "no bone" bone,
                // needed to workaround buggy models
                // without a -1, we would always draw a cube at the origin
                for (int i = 0; i < model.boneCount - 1; i++) {
                    // By default the model is loaded in bind-pose by LoadModel().
                    // But if UpdateModelAnimation() has been called at least once
                    // then the model is already in animation pose, so we need the animated skeleton
                    if (!animPlaying || animsCount == 0) {
                        // Display the bind-pose skeleton
                        rlj.models.DrawCube(model.bindPose[i].translation, 0.04f, 0.04f, 0.04f, RED);

                        if (model.bones[i].parent >= 0) {
                            rlj.models.DrawLine3D(model.bindPose[i].translation,
                                       model.bindPose[model.bones[i].parent].translation, RED);
                        }
                    }
                    else {
                        // Display the frame-pose skeleton
                        rlj.models.DrawCube(anims[animId].framePoses[animFrameCounter][i].translation, 0.05f, 0.05f, 0.05f, RED);

                        if (anims[animId].bones[i].parent >= 0) {
                            rlj.models.DrawLine3D(anims[animId].framePoses[animFrameCounter][i].translation,
                                       anims[animId].framePoses[animFrameCounter][anims[animId].bones[i].parent].translation, RED);
                        }
                    }
                }
            }

            rlj.models.DrawGrid(10, 1.0f);         // Draw a grid

            rlj.core.EndMode3D();

            rlj.text.DrawText("PRESS SPACE to PLAY MODEL ANIMATION", 10, rlj.core.GetScreenHeight() - 60, 10, MAROON);
            rlj.text.DrawText("PRESS , to CYCLE THROUGH ANIMATIONS", 10, rlj.core.GetScreenHeight() - 40, 10, DARKGRAY);
            rlj.text.DrawText("PRESS M to toggle MESH, S to toggle SKELETON DRAWING", 10, rlj.core.GetScreenHeight() - 20, 10, DARKGRAY);
            rlj.text.DrawText("(c) CesiumMan model by KhronosGroup", rlj.core.GetScreenWidth() - 210,rlj.core. GetScreenHeight() - 20, 10, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------

        // Unload model animations data
        rlj.models.UnloadModelAnimations(anims, animsCount);

        rlj.models.UnloadModel(model);         // Unload model

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
