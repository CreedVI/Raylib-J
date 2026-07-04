package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_ORTHOGRAPHIC;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.*;

public class OrthographicProjection {
    /*******************************************************************************************
     *
     *   raylib [models] example - Show the difference between perspective and orthographic projection
     *
     *   This program is heavily based on the geometric objects example
     *
     *   This example has been created using raylib 2.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Max Danielsson (@autious) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2018 Max Danielsson (@autious) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    final static float FOVY_PERSPECTIVE   = 45.0f;
    final static float WIDTH_ORTHOGRAPHIC = 10.0f;

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - geometric shapes");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj, new Vector3(0.0f, 10.0f, 10.0f ), new Vector3(0.0f, 0.0f, 0.0f ), new Vector3(0.0f, 1.0f, 0.0f ), FOVY_PERSPECTIVE, CAMERA_PERSPECTIVE);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {    // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_SPACE)) {
                if (camera.projection == CAMERA_PERSPECTIVE) {
                    camera.fovy = WIDTH_ORTHOGRAPHIC;
                    camera.projection = CAMERA_ORTHOGRAPHIC;
                }
                else {
                    camera.fovy = FOVY_PERSPECTIVE;
                    camera.projection = CAMERA_PERSPECTIVE;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawCube(new Vector3(-4.0f, 0.0f, 2.0f), 2.0f, 5.0f, 2.0f, RED);
            rlj.models.DrawCubeWires(new Vector3(-4.0f, 0.0f, 2.0f), 2.0f, 5.0f, 2.0f, GOLD);
            rlj.models.DrawCubeWires(new Vector3(-4.0f, 0.0f, -2.0f), 3.0f, 6.0f, 2.0f, MAROON);

            rlj.models.DrawSphere(new Vector3(-1.0f, 0.0f, -2.0f), 1.0f, GREEN);
            rlj.models.DrawSphereWires(new Vector3(1.0f, 0.0f, 2.0f), 2.0f, 16, 16, LIME);

            rlj.models.DrawCylinder(new Vector3(4.0f, 0.0f, -2.0f), 1.0f, 2.0f, 3.0f, 4, SKYBLUE);
            rlj.models.DrawCylinderWires(new Vector3(4.0f, 0.0f, -2.0f), 1.0f, 2.0f, 3.0f, 4, DARKBLUE);
            rlj.models.DrawCylinderWires(new Vector3(4.5f, -1.0f, 2.0f), 1.0f, 1.0f, 2.0f, 6, BROWN);

            rlj.models.DrawCylinder(new Vector3(1.0f, 0.0f, -4.0f), 0.0f, 1.5f, 3.0f, 8, GOLD);
            rlj.models.DrawCylinderWires(new Vector3(1.0f, 0.0f, -4.0f), 0.0f, 1.5f, 3.0f, 8, PINK);

            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            rlj.core.EndMode3D();

            rlj.text.DrawText("Press Spacebar to switch camera type", 10, rlj.core.GetScreenHeight() - 30, 20, DARKGRAY);

            if (camera.projection == CAMERA_ORTHOGRAPHIC) {
                rlj.text.DrawText("ORTHOGRAPHIC", 10, 40, 20, BLACK);
            }
            else if (camera.projection == CAMERA_PERSPECTIVE) {
                rlj.text.DrawText("PERSPECTIVE", 10, 40, 20, BLACK);
            }

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
