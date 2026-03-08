package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_ORTHOGRAPHIC;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.structs.Color.*;

public class Core3DCameraFirstPerson {

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        final int MAX_COLUMNS = 20;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- 3D Camera: First Person");


        // Define the camera to look into our 3d world (position, target, up vector)
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 2.0f, 4.0f);   // Camera position
        camera.target = new Vector3(0.0f, 2.0f, 0.0f);     // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);         // Camera up vector (rotation towards target)
        camera.fovy = 60.0f;                                         // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                      // Camera projection type

        int cameraMode = CAMERA_FIRST_PERSON;

        // Generates some random columns
        float[] heights = new float[MAX_COLUMNS];
        Vector3[] positions = new Vector3[MAX_COLUMNS];
        Color[] colors = new Color[MAX_COLUMNS];

        for (int i = 0; i < MAX_COLUMNS; i++) {
            heights[i] = (float) rlj.core.GetRandomValue(1, 12);
            positions[i] = new Vector3((float)rlj.core.GetRandomValue(-15, 15), heights[i]/2.0f, (float)rlj.core.GetRandomValue(-15, 15));
            colors[i] = new Color(rlj.core.GetRandomValue(20, 255), rlj.core.GetRandomValue(10, 55), 30, 255);
        }

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {        // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            // Switch camera mode
            if (rlj.core.IsKeyPressed(KEY_ONE)) {
                cameraMode = CAMERA_FREE;
                camera.up = new Vector3(0.0f, 1.0f, 0.0f); // Reset roll
            }

            if (rlj.core.IsKeyPressed(KEY_TWO)) {
                cameraMode = CAMERA_FIRST_PERSON;
                camera.up = new Vector3(0.0f, 1.0f, 0.0f); // Reset roll
            }

            if (rlj.core.IsKeyPressed(KEY_THREE)) {
                cameraMode = CAMERA_THIRD_PERSON;
                camera.up = new Vector3(0.0f, 1.0f, 0.0f); // Reset roll
            }

            if (rlj.core.IsKeyPressed(KEY_FOUR)) {
                cameraMode = CAMERA_ORBITAL;
                camera.up = new Vector3(0.0f, 1.0f, 0.0f); // Reset roll
            }

            // Switch camera projection
            if (rlj.core.IsKeyPressed(KEY_P)) {
                if (camera.projection == CAMERA_PERSPECTIVE) {
                    // Create isometric view
                    cameraMode = CAMERA_THIRD_PERSON;
                    // Note: The target distance is related to the render distance in the orthographic projection
                    camera.position = new Vector3(0.0f, 2.0f, -100.0f);
                    camera.target = new Vector3(0.0f, 2.0f, 0.0f);
                    camera.up = new Vector3(0.0f, 1.0f, 0.0f);
                    camera.projection = CAMERA_ORTHOGRAPHIC;
                    camera.fovy = 20.0f; // near plane width in CAMERA_ORTHOGRAPHIC
                    camera.Yaw(-135 * DEG2RAD, true);
                    camera.Pitch(-45 * DEG2RAD, true, true, false);
                }
                else if (camera.projection == CAMERA_ORTHOGRAPHIC) {
                    // Reset to default view
                    cameraMode = CAMERA_THIRD_PERSON;
                    camera.position = new Vector3(0.0f, 2.0f, 10.0f);
                    camera.target = new Vector3(0.0f, 2.0f, 0.0f);
                    camera.up = new Vector3(0.0f, 1.0f, 0.0f);
                    camera.projection = CAMERA_PERSPECTIVE;
                    camera.fovy = 60.0f;
                }
            }

            // Update camera computes movement internally depending on the camera mode
            // Some default standard keyboard/mouse inputs are hardcoded to simplify use
            // For advance camera controls, it's reecommended to compute camera movement manually
            camera.Update(cameraMode);                  // Update camera

            String cameraModeString = rlj.text.TextFormat("- Mode: %s", (cameraMode == CAMERA_FREE) ? "FREE" : (cameraMode == CAMERA_FIRST_PERSON) ? "FIRST_PERSON" : (cameraMode == CAMERA_THIRD_PERSON) ? "THIRD_PERSON" : (cameraMode == CAMERA_ORBITAL) ? "ORBITAL" : "CUSTOM");
            String cameraProjectionString = rlj.text.TextFormat("- Projection: %s", (camera.projection == CAMERA_PERSPECTIVE) ? "PERSPECTIVE" : (camera.projection == CAMERA_ORTHOGRAPHIC) ? "ORTHOGRAPHIC" : "CUSTOM");
            String cameraPositionString = String.format("- Position: (%06.3f, %06.3f, %06.3f)", camera.position.x, camera.position.y, camera.position.z);
            String cameraTargetString = String.format("- Target: (%06.3f, %06.3f, %06.3f)", camera.target.x, camera.target.y, camera.target.z);
            String cameraUpString = String.format("- Up: (%06.3f, %06.3f, %06.3f)", camera.up.x, camera.up.y, camera.up.z);

            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawPlane(new Vector3(0.0f, 0.0f, 0.0f), new Vector2(32.0f, 32.0f), LIGHTGRAY); // Draw ground
            rlj.models.DrawCube(new Vector3(-16.0f, 2.5f, 0.0f), 1.0f, 5.0f, 32.0f, BLUE);     // Draw a blue wall
            rlj.models.DrawCube(new Vector3(16.0f, 2.5f, 0.0f), 1.0f, 5.0f, 32.0f, LIME);      // Draw a green wall
            rlj.models.DrawCube(new Vector3(0.0f, 2.5f, 16.0f), 32.0f, 5.0f, 1.0f, GOLD);      // Draw a yellow wall

            // Draw some cubes around
            for (int i = 0; i < MAX_COLUMNS; i++) {
                rlj.models.DrawCube(positions[i], 2.0f, heights[i], 2.0f, colors[i]);
                rlj.models.DrawCubeWires(positions[i], 2.0f, heights[i], 2.0f, MAROON);
            }

            // Draw player cube
            if (cameraMode == CAMERA_THIRD_PERSON) {
                rlj.models.DrawCube(camera.target, 0.5f, 0.5f, 0.5f, PURPLE);
                rlj.models.DrawCubeWires(camera.target, 0.5f, 0.5f, 0.5f, DARKPURPLE);
            }

            rlj.core.EndMode3D();

            // Draw info boxes
            rlj.shapes.DrawRectangle(5, 5, 330, 100, rlj.textures.Fade(SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines(5, 5, 330, 100, BLUE);

            rlj.text.DrawText("Camera controls:", 15, 15, 10, BLACK);
            rlj.text.DrawText("- Move keys: W, A, S, D, Space, Left-Ctrl", 15, 30, 10, BLACK);
            rlj.text.DrawText("- Look around: arrow keys or mouse", 15, 45, 10, BLACK);
            rlj.text.DrawText("- Camera mode keys: 1, 2, 3, 4", 15, 60, 10, BLACK);
            rlj.text.DrawText("- Zoom keys: num-plus, num-minus or mouse scroll", 15, 75, 10, BLACK);
            rlj.text.DrawText("- Camera projection key: P", 15, 90, 10, BLACK);

            rlj.shapes.DrawRectangle(600, 5, 195, 100, rlj.textures.Fade(SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines(600, 5, 195, 100, BLUE);

            rlj.text.DrawText("Camera status:", 610, 15, 10, BLACK);
            rlj.text.DrawText(cameraModeString, 610, 30, 10, BLACK);
            rlj.text.DrawText(cameraProjectionString, 610, 45, 10, BLACK);
            rlj.text.DrawText(cameraPositionString, 610, 60, 10, BLACK);
            rlj.text.DrawText(cameraTargetString, 610, 75, 10, BLACK);
            rlj.text.DrawText(cameraUpString, 610, 90, 10, BLACK);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }
}
