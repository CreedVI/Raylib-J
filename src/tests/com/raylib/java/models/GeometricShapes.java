package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;

public class GeometricShapes {

    /*******************************************************************************************
     *
     *   raylib [models] example - Draw some basic geometric shapes (cube, sphere, cylinder...)
     *
     *   This example has been created using raylib 1.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2014 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/


    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - geometric shapes");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 10.0f, 10.0f);
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);
        camera.fovy = 45.0f;
        camera.projection = CAMERA_PERSPECTIVE;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {   // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            // TODO: Update your variables here
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawCube(new Vector3(-4.0f, 0.0f, 2.0f), 2.0f, 5.0f, 2.0f, Color.RED);
            rlj.models.DrawCubeWires(new Vector3(-4.0f, 0.0f, 2.0f), 2.0f, 5.0f, 2.0f, Color.GOLD);
            rlj.models.DrawCubeWires(new Vector3(-4.0f, 0.0f, -2.0f), 3.0f, 6.0f, 2.0f, Color.MAROON);

            rlj.models.DrawSphere(new Vector3(-1.0f, 0.0f, -2.0f), 1.0f, Color.GREEN);
            rlj.models.DrawSphereWires(new Vector3(1.0f, 0.0f, 2.0f), 2.0f, 16, 16, Color.LIME);

            rlj.models.DrawCylinder(new Vector3(4.0f, 0.0f, -2.0f), 1.0f, 2.0f, 3.0f, 4, Color.SKYBLUE);
            rlj.models.DrawCylinderWires(new Vector3(4.0f, 0.0f, -2.0f), 1.0f, 2.0f, 3.0f, 4, Color.DARKBLUE);
            rlj.models.DrawCylinderWires(new Vector3(4.5f, -1.0f, 2.0f), 1.0f, 1.0f, 2.0f, 6, Color.BROWN);

            rlj.models.DrawCylinder(new Vector3(1.0f, 0.0f, -4.0f), 0.0f, 1.5f, 3.0f, 8, Color.GOLD);
            rlj.models.DrawCylinderWires(new Vector3(1.0f, 0.0f, -4.0f), 0.0f, 1.5f, 3.0f, 8, Color.PINK);

            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
