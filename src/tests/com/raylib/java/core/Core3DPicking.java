package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.BoundingBox;
import com.raylib.java.structs.Ray;
import com.raylib.java.structs.RayCollision;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_RIGHT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.structs.Color.*;

public class Core3DPicking {

    // TODO: picking in first person seems off centre after moving mouse

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- 3D Picking");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Vector3 cubePosition = new Vector3(0.0f, 1.0f, 0.0f);
        Vector3 cubeSize = new Vector3(2.0f, 2.0f, 2.0f);

        Ray ray = new Ray();                    // Picking line ray
        RayCollision collision = new RayCollision();     // Ray collision hit info

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {       // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsCursorHidden()) {
                camera.Update(CAMERA_FIRST_PERSON);
            }
            
            // Toggle camera controls
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
                if (rlj.core.IsCursorHidden()) {
                    rlj.core.EnableCursor();
                }
                else {
                    rlj.core.DisableCursor();
                }
            }

            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                if (!collision.hit) {
                    ray = rlj.core.GetMouseRay(rlj.core.GetMousePosition(), camera);

                    // Check collision between ray and box
                    collision = rlj.models.GetRayCollisionBox(
                            ray,
                            new BoundingBox(
                                    new Vector3(cubePosition.x - cubeSize.x/2, cubePosition.y - cubeSize.y/2, cubePosition.z - cubeSize.z/2),
                                    new Vector3(cubePosition.x + cubeSize.x/2, cubePosition.y + cubeSize.y/2, cubePosition.z + cubeSize.z/2)
                            )
                    );
                }
                else{
                    collision.hit = false;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            if (collision.hit) {
                rlj.models.DrawCube(cubePosition, cubeSize.x, cubeSize.y, cubeSize.z, RED);
                rlj.models.DrawCubeWires(cubePosition, cubeSize.x, cubeSize.y, cubeSize.z, MAROON);

                rlj.models.DrawCubeWires(cubePosition, cubeSize.x + 0.2f, cubeSize.y + 0.2f, cubeSize.z + 0.2f, GREEN);
            }
            else {
                rlj.models.DrawCube(cubePosition, cubeSize.x, cubeSize.y, cubeSize.z, GRAY);
                rlj.models.DrawCubeWires(cubePosition, cubeSize.x, cubeSize.y, cubeSize.z, DARKGRAY);
            }

            rlj.models.DrawRay(ray, MAROON);
            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            rlj.text.DrawText("Try clicking on the box with your mouse!", 240, 10, 20, DARKGRAY);

            if (collision.hit) {
                rlj.text.DrawText("BOX SELECTED", (SCREEN_WIDTH - rlj.text.MeasureText("BOX SELECTED", 30)) / 2, (int)(SCREEN_HEIGHT * 0.1f), 30, GREEN);
            }

            rlj.text.DrawText("Right click mouse to toggle camera controls", 10, 430, 10, GRAY);

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
