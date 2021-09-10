package com.raylib.examples.core;

import com.raylib.Raylib;
import com.raylib.core.Color;
import com.raylib.core.Core;
import com.raylib.core.camera.Camera;
import com.raylib.core.camera.Camera3D;
import com.raylib.raymath.Vector3;
import com.raylib.textures.Textures;

import static com.raylib.core.camera.Camera.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.core.camera.Camera.CameraProjection.CAMERA_PERSPECTIVE;

public class Core3dCameraFirstPerson{

    /*******************************************************************************************
     *
     *   raylib [core] example - 3d camera first person
     *
     *   This example has been created using raylib 1.3 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2015 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    //TODO: stuff not rendering

    final static int MAX_COLUMNS = 20;

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - 3d camera first person");

        // Define the camera to look into our 3d world (position, target, up vector)
        Camera3D camera = new Camera3D();
        camera.position = new Vector3(4.0f, 2.0f, 4.0f);
        camera.target = new Vector3( 0.0f, 1.8f, 0.0f);
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);
        camera.fovy = 60.0f;
        camera.projection = CAMERA_PERSPECTIVE;

        // Generates some random columns
        float[] heights = new float[MAX_COLUMNS];
        Vector3[] positions = new Vector3[MAX_COLUMNS];
        Color[] colors = new Color[MAX_COLUMNS];

        for (int i = 0; i < MAX_COLUMNS; i++) {
            heights[i] = (float) Core.GetRandomValue(1, 12);
            positions[i] = new Vector3(Core.GetRandomValue(-15, 15), heights[i]/2, Core.GetRandomValue(-15, 15));
            colors[i] = new Color(Core.GetRandomValue(20, 255), Core.GetRandomValue(10, 55), 30, 255);
        }

        Camera.SetCameraMode(camera, CAMERA_FIRST_PERSON); // Set a first person camera mode

        rlj.core.SetTargetFPS(60);                           // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())                // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            Camera.UpdateCamera(camera);                  // Update camera
            System.out.println(camera.position.x +", "+ camera.position.y +", "+ camera.position.z);
            System.out.println(camera.target.x +", "+ camera.target.y +", "+ camera.target.z);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            /*TODO - MESHES
            Models.DrawPlane(new Vector3(0.0f, 0.0f, 0.0f), new Vector2(32.0f, 32.0f), Color.LIGHTGRAY); // Draw ground
            DrawCube((Vector3){ -16.0f, 2.5f, 0.0f }, 1.0f, 5.0f, 32.0f, BLUE);     // Draw a blue wall
            DrawCube((Vector3){ 16.0f, 2.5f, 0.0f }, 1.0f, 5.0f, 32.0f, LIME);      // Draw a green wall
            DrawCube((Vector3){ 0.0f, 2.5f, 16.0f }, 32.0f, 5.0f, 1.0f, GOLD);      // Draw a yellow wall

            // Draw some cubes around
            for (int i = 0; i < MAX_COLUMNS; i++)
            {
                DrawCube(positions[i], 2.0f, heights[i], 2.0f, colors[i]);
                DrawCubeWires(positions[i], 2.0f, heights[i], 2.0f, MAROON);
            }
            */

            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle( 10, 10, 220, 70, Textures.Fade(Color.SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines( 10, 10, 220, 70, Color.BLUE);

            rlj.text.DrawText("First person camera default controls:", 20, 20, 10, Color.BLACK);
            rlj.text.DrawText("- Move with keys: W, A, S, D", 40, 40, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Mouse move to look around", 40, 60, 10, Color.DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}