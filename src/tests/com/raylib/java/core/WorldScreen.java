package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_THIRD_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.*;

public class WorldScreen {

    // TODO: Fix

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- World Screen");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3();      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Vector3 cubePosition = new Vector3();
        Vector2 cubeScreenPosition = new Vector2();

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {        // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_THIRD_PERSON);

            // Calculate cube screen space position (with a little offset to be in top)
            // cubeScreenPosition = rlj.core.GetWorldToScreen(new Vector3(cubePosition.x, cubePosition.y + 2.5f, cubePosition.z), camera);
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

            // rlj.text.DrawText("Enemy: 100 / 100", (int)cubeScreenPosition.x - rlj.text.MeasureText("Enemy: 100/100", 20)/2, (int)cubeScreenPosition.y, 20, BLACK);

            rlj.text.DrawText(rlj.text.TextFormat("Cube position in screen space coordinates: [%d, %d]", (int)cubeScreenPosition.x, (int)cubeScreenPosition.y), 10, 10, 20, LIME);
            rlj.text.DrawText("Text 2d should be always on top of the cube", 10, 40, 20, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }

}
