package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.input.Keyboard;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

public class TextureRectangle{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Texture loading and drawing a part defined by a rectangle
     *
     *   This example has been created using raylib 1.3 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2014 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/


    final static int MAX_FRAME_SPEED = 15;
    final static int MIN_FRAME_SPEED = 1;

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [texture] example - texture rectangle");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Texture2D scarfy = rlj.textures.LoadTexture("resources/scarfy.png");        // Texture loading

        Vector2 position = new Vector2(350.0f, 280.0f);
        Rectangle frameRec = new Rectangle(0.0f, 0.0f, (float)scarfy.width/6, (float)scarfy.height);
        int currentFrame = 0;

        int framesCounter = 0;
        int framesSpeed = 8;            // Number of spritesheet frames shown by second

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            framesCounter++;

            if (framesCounter >= (60/framesSpeed))
            {
                framesCounter = 0;
                currentFrame++;

                if (currentFrame > 5) currentFrame = 0;

                frameRec.x = (float)currentFrame*(float)scarfy.width/6;
            }

            if (rlj.core.IsKeyPressed(Keyboard.KEY_RIGHT)) framesSpeed++;
            else if (rlj.core.IsKeyPressed(Keyboard.KEY_LEFT)) framesSpeed--;

            if (framesSpeed > MAX_FRAME_SPEED) framesSpeed = MAX_FRAME_SPEED;
            else if (framesSpeed < MIN_FRAME_SPEED) framesSpeed = MIN_FRAME_SPEED;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.textures.DrawTexture(scarfy, 15, 40, Color.WHITE);
            rlj.shapes.DrawRectangleLines(15, 40, scarfy.width, scarfy.height, Color.LIME);
            rlj.shapes.DrawRectangleLines(15 + (int)frameRec.x, 40 + (int)frameRec.y, (int)frameRec.width,
                    (int)frameRec.height, Color.RED);

            rlj.text.DrawText("FRAME SPEED: ", 165, 210, 10, Color.DARKGRAY);
            rlj.text.DrawText(framesSpeed + " FPS", 575, 210, 10, Color.DARKGRAY);
            rlj.text.DrawText("PRESS RIGHT/LEFT KEYS to CHANGE SPEED!", 290, 240, 10, Color.DARKGRAY);

            for (int i = 0; i < MAX_FRAME_SPEED; i++)
            {
                if (i < framesSpeed){
                    rlj.shapes.DrawRectangle(250 + 21*i, 205, 20, 20, Color.RED);
                }
                rlj.shapes.DrawRectangleLines(250 + 21*i, 205, 20, 20, Color.MAROON);
            }

            rlj.textures.DrawTextureRec(scarfy, frameRec, position, Color.WHITE);  // Draw part of the texture

            rlj.text.DrawText("(c) Scarfy sprite by Eiden Marsal", screenWidth - 200, screenHeight - 20, 10,
                    Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(scarfy);       // Texture unloading
        //--------------------------------------------------------------------------------------

    }
}
