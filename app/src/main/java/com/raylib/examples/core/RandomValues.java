package com.raylib.examples.core;

import com.raylib.Raylib;
import com.raylib.core.Color;
import com.raylib.core.Core;

public class RandomValues{

    /*******************************************************************************************
     *
     *   raylib [core] example - Generate random values
     *
     *   This example has been created using raylib 1.1 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2014 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - generate random values");

        int framesCounter = 0;          // Variable used to count frames

        int randValue = Core.GetRandomValue(-8, 5);   // Get a random integer number between -8 and 5 (both included)

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            framesCounter++;

            // Every two seconds (120 frames) a new random value is generated
            if (((framesCounter/120)%2) == 1)
            {
                randValue = Core.GetRandomValue(-8, 5);
                framesCounter = 0;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("Every 2 seconds a new random value is generated:", 130, 100, 20,
                    Color.MAROON);

            rlj.text.DrawText(String.valueOf(randValue), 360, 180, 80, Color.LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}