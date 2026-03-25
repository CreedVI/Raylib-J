package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera2D;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.RenderTexture;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.structs.Color.*;

public class PixelPerfectCamera {

    /*******************************************************************************************
     *
     *   raylib [core] example - smooth pixel-perfect camera
     *
     *   Example originally created with raylib 3.7, last time updated with raylib 4.0
     *
     *   Example contributed by Giancamillo Alessandroni (@NotManyIdeasDev) and
     *   reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2021-2023 Giancamillo Alessandroni (@NotManyIdeasDev) and Ramon Santamaria (@raysan5)
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

        int virtualScreenWidth = 160;
        int virtualScreenHeight = 90;

        float virtualRatio = (float)screenWidth/(float)virtualScreenWidth;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - smooth pixel-perfect camera");

        Camera2D worldSpaceCamera = new Camera2D();  // Game world camera
        worldSpaceCamera.zoom = 1.0f;

        Camera2D screenSpaceCamera = new Camera2D(); // Smoothing camera
        screenSpaceCamera.zoom = 1.0f;

        RenderTexture target = rlj.textures.LoadRenderTexture(virtualScreenWidth, virtualScreenHeight); // This is where we'll draw all our objects.

        Rectangle rec01 = new Rectangle(70.0f, 35.0f, 20.0f, 20.0f);
        Rectangle rec02 = new Rectangle(90.0f, 55.0f, 30.0f, 10.0f);
        Rectangle rec03 = new Rectangle(80.0f, 65.0f, 15.0f, 25.0f);

        // The target's height is flipped (in the source Rectangle), due to OpenGL reasons
        Rectangle sourceRec = new Rectangle(0.0f, 0.0f, (float)target.texture.width, -(float)target.texture.height);
        Rectangle destRec = new Rectangle(-virtualRatio, -virtualRatio, screenWidth + (virtualRatio*2), screenHeight + (virtualRatio*2));

        Vector2 origin = new Vector2();

        float rotation = 0.0f;

        float cameraX = 0.0f;
        float cameraY = 0.0f;

        rlj.core.SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {    // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            rotation += 60.0f * rlj.core.GetFrameTime();   // Rotate the rectangles, 60 degrees per second

            // Make the camera move to demonstrate the effect
            cameraX = (float) ((Math.sin(rlj.core.GetTime())*50.0f) - 10.0f);
            cameraY = (float) (Math.cos(rlj.core.GetTime())*30.0f);

            // Set the camera's target to the values computed above
            screenSpaceCamera.target = new Vector2(cameraX, cameraY);

            // Round worldSpace coordinates, keep decimals into screenSpace coordinates
            worldSpaceCamera.target.x = (int)screenSpaceCamera.target.x;
            screenSpaceCamera.target.x -= worldSpaceCamera.target.x;
            screenSpaceCamera.target.x *= virtualRatio;

            worldSpaceCamera.target.y = (int)screenSpaceCamera.target.y;
            screenSpaceCamera.target.y -= worldSpaceCamera.target.y;
            screenSpaceCamera.target.y *= virtualRatio;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginTextureMode(target);
            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode2D(worldSpaceCamera);
            rlj.shapes.DrawRectanglePro(rec01, origin, rotation, BLACK);
            rlj.shapes.DrawRectanglePro(rec02, origin, -rotation, RED);
            rlj.shapes.DrawRectanglePro(rec03, origin, rotation + 45.0f, BLUE);
            rlj.core.EndMode2D();
            rlj.core.EndTextureMode();

            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(RED);

            rlj.core.BeginMode2D(screenSpaceCamera);
            rlj.textures.DrawTexturePro(target.texture, sourceRec, destRec, origin, 0.0f, WHITE);
            rlj.core.EndMode2D();

            rlj.text.DrawText(rlj.text.TextFormat("Screen resolution: %dx%d", screenWidth, screenHeight), 10, 10, 20, DARKBLUE);
            rlj.text.DrawText(rlj.text.TextFormat("World resolution: %dx%d", virtualScreenWidth, virtualScreenHeight), 10, 40, 20, DARKGREEN);
            rlj.text.DrawFPS(rlj.core.GetScreenWidth() - 95, 10);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadRenderTexture(target);    // Unload render texture

        rlj.core.CloseWindow();                  // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }
    
}
