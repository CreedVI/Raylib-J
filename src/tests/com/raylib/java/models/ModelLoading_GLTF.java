package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;
import com.raylib.java.structs.ModelAnimation;

import static com.raylib.java.core.input.Keyboard.KEY_DOWN;
import static com.raylib.java.core.input.Keyboard.KEY_UP;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_THIRD_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.*;

public class ModelLoading_GLTF {

    /*******************************************************************************************
     *
     *   raylib [models] example - loading gltf with animations
     *
     *   LIMITATIONS:
     *     - Only supports 1 armature per file, and skips loading it if there are multiple armatures
     *     - Only supports linear interpolation (default method in Blender when checked
     *       "Always Sample Animations" when exporting a GLTF file)
     *     - Only supports translation/rotation/scale animation channel.path,
     *       weights not considered (i.e. morph targets)
     *
     *   Example originally created with raylib 3.7, last time updated with raylib 4.2
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2020-2023 Ramon Santamaria (@raysan5)
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

        String modelFile = "src/tests/resources/models/models/gltf/girl.glb";

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - loading glTF");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(5.0f, 5.0f, 5.0f);    // Camera position
        camera.target = new Vector3(0.0f, 2.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Load glTF model
        Model model = rlj.models.LoadModel(modelFile);

        BoundingBox box = rlj.models.GetModelBoundingBox(model);

        // Load glTF model animations
        int animIndex = 0;
        int animCurrentFrame = 0;
        ModelAnimation[] modelAnimations = rlj.models.LoadModelAnimations(modelFile);

        Vector3 position = new Vector3();    // Set model position

        // rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_THIRD_PERSON);
            // Select current animation
            if (rlj.core.IsKeyPressed(KEY_UP)) {
                animIndex = (animIndex + 1) % modelAnimations.length;
            }
            else if (rlj.core.IsKeyPressed(KEY_DOWN)) {
                animIndex = (animIndex + modelAnimations.length - 1) % modelAnimations.length;
            }

            // Update model animation
            ModelAnimation anim = modelAnimations[animIndex];
            animCurrentFrame = (animCurrentFrame + 1) % anim.frameCount;
            rlj.models.UpdateModelAnimation(model, anim, animCurrentFrame);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(model, position, 1.0f, WHITE);    // Draw animated model
            rlj.models.DrawBoundingBox(box, DARKBLUE);

            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            rlj.text.DrawText("Use the UP/DOWN arrow keys to switch animation", 10, 10, 20, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.models.UnloadModel(model);         // Unload model and meshes/material

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
