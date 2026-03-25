package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.RenderTexture;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.structs.Color.*;

public class SplitScreen {

    /*******************************************************************************************
     *
     *   raylib [core] example - split screen
     *
     *   Example originally created with raylib 3.7, last time updated with raylib 4.0
     *
     *   Example contributed by Jeffery Myers (@JeffM2501) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2021-2023 Jeffery Myers (@JeffM2501)
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - split screen");

        // Setup player 1 camera and screen
        Camera3D cameraPlayer1 = new Camera3D(rlj);
        cameraPlayer1.fovy = 45.0f;
        cameraPlayer1.up.y = 1.0f;
        cameraPlayer1.target.y = 1.0f;
        cameraPlayer1.position.z = -3.0f;
        cameraPlayer1.position.y = 1.0f;

        RenderTexture screenPlayer1 = rlj.textures.LoadRenderTexture(screenWidth/2, screenHeight);

        // Setup player two camera and screen
        Camera3D cameraPlayer2 = new Camera3D(rlj);
        cameraPlayer2.fovy = 45.0f;
        cameraPlayer2.up.y = 1.0f;
        cameraPlayer2.target.y = 3.0f;
        cameraPlayer2.position.x = -3.0f;
        cameraPlayer2.position.y = 3.0f;

        RenderTexture screenPlayer2 = rlj.textures.LoadRenderTexture(screenWidth / 2, screenHeight);

        // Grid data
        int count = 5;
        float spacing = 4;

        // Build a flipped rectangle the size of the split view to use for drawing later
        Rectangle splitScreenRect = new Rectangle(0.0f, 0.0f, (float)screenPlayer1.texture.width, (float)-screenPlayer1.texture.height);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // If anyone moves this frame, how far will they move based on the time since the last frame
            // this moves thigns at 10 world units per second, regardless of the actual FPS
            float offsetThisFrame = 10.0f * rlj.core.GetFrameTime();

            // Move Player1 forward and backwards (no turning)
            if (rlj.core.IsKeyDown(KEY_W)) {
                cameraPlayer1.position.z += offsetThisFrame;
                cameraPlayer1.target.z += offsetThisFrame;
            }
            else if (rlj.core.IsKeyDown(KEY_S)) {
                cameraPlayer1.position.z -= offsetThisFrame;
                cameraPlayer1.target.z -= offsetThisFrame;
            }

            // Move Player2 forward and backwards (no turning)
            if (rlj.core.IsKeyDown(KEY_UP)) {
                cameraPlayer2.position.x += offsetThisFrame;
                cameraPlayer2.target.x += offsetThisFrame;
            }
            else if (rlj.core.IsKeyDown(KEY_DOWN)) {
                cameraPlayer2.position.x -= offsetThisFrame;
                cameraPlayer2.target.x -= offsetThisFrame;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            // Draw Player1 view to the render texture
            rlj.core.BeginTextureMode(screenPlayer1);
            rlj.core.ClearBackground(SKYBLUE);
            rlj.core.BeginMode3D(cameraPlayer1);

            // Grid of cube trees on a plane to make a "world"
            rlj.models.DrawPlane(new Vector3(), new Vector2(50, 50), BEIGE); // Simple world plane

            for (float x = -count*spacing; x <= count*spacing; x += spacing) {
                for (float z = -count*spacing; z <= count*spacing; z += spacing) {
                    rlj.models.DrawCube(new Vector3(x, 1.5f, z), 1, 1, 1, LIME);
                    rlj.models.DrawCube(new Vector3(x, 0.5f, z), 0.25f, 1, 0.25f, BROWN);
                }
            }

            // Draw a cube at each player's position
            rlj.models.DrawCube(cameraPlayer1.position, 1, 1, 1, RED);
            rlj.models.DrawCube(cameraPlayer2.position, 1, 1, 1, BLUE);
            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth()/2, 40, rlj.textures.Fade(RAYWHITE, 0.8f));
            rlj.text.DrawText("PLAYER1 W/S to move", 10, 10, 20, RED);
            rlj.core.EndTextureMode();

            // Draw Player2 view to the render texture
            rlj.core.BeginTextureMode(screenPlayer2);
            rlj.core.ClearBackground(SKYBLUE);
            rlj.core.BeginMode3D(cameraPlayer2);

            // Grid of cube trees on a plane to make a "world"
            rlj.models.DrawPlane(new Vector3(), new Vector2(50, 50), BEIGE); // Simple world plane

            for (float x = -count*spacing; x <= count*spacing; x += spacing) {
                for (float z = -count*spacing; z <= count*spacing; z += spacing) {
                    rlj.models.DrawCube(new Vector3(x, 1.5f, z), 1, 1, 1, LIME);
                    rlj.models.DrawCube(new Vector3(x, 0.5f, z), 0.25f, 1, 0.25f, BROWN);
                }
            }

            // Draw a cube at each player's position
            rlj.models.DrawCube(cameraPlayer1.position, 1, 1, 1, RED);
            rlj.models.DrawCube(cameraPlayer2.position, 1, 1, 1, BLUE);
            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth()/2, 40, rlj.textures.Fade(RAYWHITE, 0.8f));
            rlj.text.DrawText("PLAYER2 UP/DOWN to move", 10, 10, 20, BLUE);
            rlj.core.EndTextureMode();

            // Draw both views render textures to the screen side by side
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(BLACK);
            rlj.textures.DrawTextureRec(screenPlayer1.texture, splitScreenRect, new Vector2(), WHITE);
            rlj.textures.DrawTextureRec(screenPlayer2.texture, splitScreenRect, new Vector2(screenWidth/2.0f, 0), WHITE);
            rlj.shapes.DrawRectangle(rlj.core.GetScreenWidth()/2 - 2, 0, 4, rlj.core.GetScreenHeight(), LIGHTGRAY);
            rlj.core.EndDrawing();
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadRenderTexture(screenPlayer1); // Unload render texture
        rlj.textures.UnloadRenderTexture(screenPlayer2); // Unload render texture

        rlj.core.CloseWindow();                      // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
