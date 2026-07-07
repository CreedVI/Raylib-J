package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Model;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.structs.Color.*;

public class ModelAnimation {

    /*******************************************************************************************
     *
     *   raylib [models] example - Load 3d model with animations and play them
     *
     *   Example originally created with raylib 2.5, last time updated with raylib 3.5
     *
     *   Example contributed by Culacant (@culacant) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2019-2023 Culacant (@culacant) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************
     *
     *   NOTE: To export a model from blender, make sure it is not posed, the vertices need to be
     *         in the same position as they would be in edit mode and the scale of your models is
     *         set to 0. Scaling can be done from the export menu.
     *
     ********************************************************************************************/

    // TODO: fix

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - model animation");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera mode type

        Model model = rlj.models.LoadModel("src/tests/resources/models/models/iqm/guy.iqm");                    // Load the animated model mesh and basic data
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/models/models/iqm/guytex.png");         // Load model texture and set material
        rlj.models.SetMaterialTexture(model.materials[0], MATERIAL_MAP_DIFFUSE, texture);     // Set model material map texture

        Vector3 position = new Vector3();            // Set model position

        // Load animation data
        com.raylib.java.structs.ModelAnimation[] anims = rlj.models.LoadModelAnimations("src/tests/resources/models/models/iqm/guyanim.iqm");
        int animsCount = anims.length;
        int animFrameCounter = 0;

        rlj.core.DisableCursor();                    // Catch cursor
        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second

        int vertex = 0;

        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);

            // Play animation when spacebar is held down
            if (rlj.core.IsKeyDown(KEY_SPACE)) {
                animFrameCounter++;
                rlj.models.UpdateModelAnimation(model, anims[0], animFrameCounter);
                if (animFrameCounter >= anims[0].frameCount) {
                    animFrameCounter = 0;
                }
            }
            if (rlj.core.IsKeyPressed(KEY_PAGE_UP)) {
                vertex += 3;
                if (vertex >= model.meshes[0].vertexCount - 3) {
                    vertex = 0;
                }
            }
            if (rlj.core.IsKeyPressed(KEY_PAGE_DOWN)) {
                vertex -= 3;
                if (vertex < 0) {
                    vertex = model.meshes[0].vertexCount - 3;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModelEx(model, position, new Vector3(1.0f, 0.0f, 0.0f), -90.0f, new Vector3(1.0f, 1.0f, 1.0f), WHITE);

            for (int i = 0; i < model.boneCount; i++) {
                rlj.models.DrawCube(anims[0].framePoses[animFrameCounter][i].translation, 0.2f, 0.2f, 0.2f, RED);
            }

            rlj.models.DrawGrid(10, 1.0f);         // Draw a grid 

            rlj.core.EndMode3D();

            rlj.text.DrawText("PRESS SPACE to PLAY MODEL ANIMATION", 10, 10, 20, MAROON);
            rlj.text.DrawText("(c) Guy IQM 3D model by @culacant", screenWidth - 200, screenHeight - 20, 10, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);                     // Unload texture
        rlj.models.UnloadModelAnimations(anims);   // Unload model animations data
        rlj.models.UnloadModel(model);                         // Unload model

        rlj.core.CloseWindow();                  // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }

}
