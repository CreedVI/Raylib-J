package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FREE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.*;

public class Core3DCameraFree {

    // TODO: fix free camera

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- 3D Camera: Free");
        
        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Vector3 cubePosition = new Vector3(0.0f, 0.0f, 0.0f);

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {        // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FREE);

            if (rlj.core.IsKeyDown('Z')) {
                camera.target = new Vector3(0.0f, 0.0f, 0.0f);
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawCube(cubePosition, 2.0f, 2.0f, 2.0f, RED);
            rlj.models.DrawCubeWires(cubePosition, 2.0f, 2.0f, 2.0f, MAROON);

            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle( 10, 10, 320, 133, rlj.textures.Fade(SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines( 10, 10, 320, 133, BLUE);

            rlj.text.DrawText("Free camera default controls:", 20, 20, 10, BLACK);
            rlj.text.DrawText("- Mouse Wheel to Zoom in-out", 40, 40, 10, DARKGRAY);
            rlj.text.DrawText("- Mouse Wheel Pressed to Pan", 40, 60, 10, DARKGRAY);
            rlj.text.DrawText("- Alt + Mouse Wheel Pressed to Rotate", 40, 80, 10, DARKGRAY);
            rlj.text.DrawText("- Alt + Ctrl + Mouse Wheel Pressed for Smooth Zoom", 40, 100, 10, DARKGRAY);
            rlj.text.DrawText("- Z to zoom to (0, 0, 0)", 40, 120, 10, DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }
    
}
