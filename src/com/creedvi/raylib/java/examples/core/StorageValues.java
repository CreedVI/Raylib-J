package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.core.input.Keyboard;

import static com.creedvi.raylib.java.rlj.core.Color.*;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_ENTER;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_SPACE;

public class StorageValues{

    //TODO: Core.SaveStorageValue, Core.LoadStorageValue

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Storage Values
     *
     *   This example has been created using raylib-j (Version 0.2)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    static class StorageData{
        final static int
            STORAGE_POSITION_SCORE = 0,
            STORAGE_POSITION_HISCORE = 1;
    }

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - storage save/load values");

        int score = 0;
        int hiscore = 0;
        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(Keyboard.KEY_R)){
                score = Core.GetRandomValue(1000, 2000);
                hiscore = Core.GetRandomValue(2000, 4000);
            }

            if (rlj.core.IsKeyPressed(KEY_ENTER)){
                //rlj.core.SaveStorageValue(STORAGE_POSITION_SCORE, score);
                //rlj.core.SaveStorageValue(STORAGE_POSITION_HISCORE, hiscore);
            }
            else if (rlj.core.IsKeyPressed(KEY_SPACE)){
                // NOTE: If requested position could not be found, value 0 is returned
                //score = rlj.core.LoadStorageValue(STORAGE_POSITION_SCORE);
                //hiscore = rlj.core.LoadStorageValue(STORAGE_POSITION_HISCORE);
            }

            framesCounter++;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.text.DrawText("SCORE: " + score, 280, 130, 40, MAROON);
            rlj.text.DrawText("HI-SCORE: " + hiscore, 210, 200, 50, BLACK);

            rlj.text.DrawText("frames: " + framesCounter, 10, 10, 20, LIME);

            rlj.text.DrawText("Press R to generate random numbers", 220, 40, 20, LIGHTGRAY);
            rlj.text.DrawText("Press ENTER to SAVE values", 250, 310, 20, LIGHTGRAY);
            rlj.text.DrawText("Press SPACE to LOAD values", 252, 350, 20, LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        //TODO: Unload values here
        //--------------------------------------------------------------------------------------

    }

}
