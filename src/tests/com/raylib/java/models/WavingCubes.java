package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.RAYWHITE;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class WavingCubes {

    /*******************************************************************************************
     *
     *   raylib [models] example - Waving cubes
     *
     *   Example originally created with raylib 2.5, last time updated with raylib 3.7
     *
     *   Example contributed by Codecat (@codecat) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2019-2023 Codecat (@codecat) and Ramon Santamaria (@raysan5)
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

        Raylib rlj =  new Raylib(screenWidth, screenHeight, "raylib [models] example - waving cubes");

        // Initialize the camera
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(30.0f, 20.0f, 30.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 70.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Specify the amount of blocks in each direction
        int numBlocks = 15;

        rlj.core.SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            double time = rlj.core.GetTime();

            // Calculate time scale for cube position and size
            float scale = (2.0f + (float)sin(time))*0.7f;

            // Move camera around the scene
            double cameraTime = time*0.3;
            camera.position.x = (float)cos(cameraTime)*40.0f;
            camera.position.z = (float)sin(cameraTime)*40.0f;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawGrid(10, 5.0f);

            for (int x = 0; x < numBlocks; x++) {
                for (int y = 0; y < numBlocks; y++) {
                    for (int z = 0; z < numBlocks; z++) {
                        // Scale of the blocks depends on x/y/z positions
                        float blockScale = (x + y + z)/30.0f;

                        // Scatter makes the waving effect by adding blockScale over time
                        float scatter = (float) sin(blockScale*20.0f + (float)(time*4.0f));

                        // Calculate the cube position
                        Vector3 cubePos = new Vector3(
                                (float)(x - numBlocks/2)*(scale*3.0f) + scatter,
                                (float)(y - numBlocks/2)*(scale*2.0f) + scatter,
                                (float)(z - numBlocks/2)*(scale*3.0f) + scatter
                        );

                        // Pick a color with a hue depending on cube position for the rainbow color effect
                        Color cubeColor = rlj.textures.ColorFromHSV((float)(((x + y + z)*18)%360), 0.75f, 0.9f);

                        // Calculate cube size
                        float cubeSize = (2.4f - scale)*blockScale;

                        // And finally, draw the cube!
                        rlj.models.DrawCube(cubePos, cubeSize, cubeSize, cubeSize, cubeColor);
                    }
                }
            }

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
