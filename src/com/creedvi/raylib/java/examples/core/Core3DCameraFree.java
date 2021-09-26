package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.camera.Camera;
import com.creedvi.raylib.java.rlj.core.camera.Camera3D;
import com.creedvi.raylib.java.rlj.raymath.Vector3;
import com.creedvi.raylib.java.rlj.textures.Textures;

import static com.creedvi.raylib.java.rlj.core.camera.Camera.CameraMode.CAMERA_FREE;
import static com.creedvi.raylib.java.rlj.core.camera.Camera.CameraProjection.CAMERA_PERSPECTIVE;

public class Core3DCameraFree{

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - 3d camera free");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D();
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                   // Camera mode type

        Vector3 cubePosition = new Vector3(0.0f, 0.0f, 0.0f);

        Camera.SetCameraMode(camera, CAMERA_FREE); // Set a free camera mode

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            Camera.UpdateCamera(camera);          // Update camera

            if (rlj.core.IsKeyDown('Z'))
                camera.target = new Vector3(0.0f, 0.0f, 0.0f);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawCube(cubePosition, 2.0f, 2.0f, 2.0f, Color.RED);
            rlj.models.DrawCubeWires(cubePosition, 2.0f, 2.0f, 2.0f, Color.MAROON);

            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle(10, 10, 320, 133, Textures.Fade(Color.SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines( 10, 10, 320, 133, Color.BLUE);

            rlj.text.DrawText("Free camera default controls:", 20, 20, 10, Color.BLACK);
            rlj.text.DrawText("- Mouse Wheel to Zoom in-out", 40, 40, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Mouse Wheel Pressed to Pan", 40, 60, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Alt + Mouse Wheel Pressed to Rotate", 40, 80, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Alt + Ctrl + Mouse Wheel Pressed for Smooth Zoom", 40, 100, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Z to zoom to (0, 0, 0)", 40, 120, 10, Color.DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

    }

}
