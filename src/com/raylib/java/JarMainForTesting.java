package com.raylib.java;

import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Vector3;
import com.raylib.java.textures.rTextures;

import static com.raylib.java.core.Color.*;
import static com.raylib.java.core.input.Keyboard.KEY_L;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FREE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;

public class JarMainForTesting{

    static Raylib rlj;

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 1920;
        int screenHeight = 1080;

        rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - 3d camera free");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D();
        camera.position = new Vector3(10.0f, 10.0f, 10.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera mode type

        Vector3 cubePosition = new Vector3(0.0f, 0.0f, 0.0f);

        camera.SetCameraMode(camera, CAMERA_FREE); // Set a free camera mode

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.UpdateCamera();          // Update camera
            if (rlj.core.IsKeyPressed(KEY_L)) {
                System.out.println("----------CAMERA----------");
                System.out.println("Target x :: " + camera.target.x);
                System.out.println("Target y :: " + camera.target.y);
                System.out.println("Target z :: " + camera.target.z);
                System.out.println("Position x :: " + camera.position.x);
                System.out.println("Position y :: " + camera.position.y);
                System.out.println("Position z :: " + camera.position.z);
                System.out.println("----------END----------");
            }


            if (rlj.core.IsKeyDown('Z')) camera.target = new Vector3(0.0f, 0.0f, 0.0f);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            //rlj.models.DrawCube(new Vector3(), 2, 2, 2, RED);
            rlj.models.DrawCubeWires(new Vector3(), 2, 2, 2, MAROON);
            //rlj.models.DrawSphere(new Vector3(), 10, BLUE);

            //rlj.models.DrawGrid(10,1);

            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle( 10, 10, 320, 133, rTextures.Fade(SKYBLUE, 0.5f));
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
        //--------------------------------------------------------------------------------------
    }

}
