package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Vector2;

public class FontSpritefont{

    /*******************************************************************************************
     *
     *   raylib [text] example - Sprite font loading
     *
     *   Loaded sprite fonts have been generated following XNA SpriteFont conventions:
     *     - Characters must be ordered starting with character 32 (Space)
     *     - Every character must be contained within the same Rectangle height
     *     - Every character and every line must be separated the same distance
     *     - Rectangles must be defined by a MAGENTA color background
     *
     *   If following this constraints, a font can be provided just by an image,
     *   this is quite handy to avoid additional information files (like BMFonts use).
     *
     *   This example has been created using raylib 1.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2014 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - sprite font loading");

        String msg1 = "THIS IS A custom SPRITE FONT...";
        String msg2 = "...and this is ANOTHER CUSTOM font...";
        String msg3 = "...and a THIRD one! GREAT! :D";

        // NOTE: Textures/Fonts MUST be loaded after Window initialization (OpenGL context is required)
        Font font1 = rlj.text.LoadFont("resources/custom_mecha.png");          // Font loading
        Font font2 = rlj.text.LoadFont("resources/custom_alagard.png");        // Font loading
        Font font3 = rlj.text.LoadFont("resources/custom_jupiter_crash.png");  // Font loading

        Vector2 fontPosition1 =
                new Vector2(screenWidth/2.0f - rlj.text.MeasureTextEx(font1, msg1, (float)font1.baseSize, -3).x/2,
                screenHeight/2.0f - font1.baseSize/2.0f - 80.0f );

        Vector2 fontPosition2 = new Vector2(screenWidth/2.0f - rlj.text.MeasureTextEx(font2, msg2, (float)font2.baseSize, -2.0f).x/2.0f,
                screenHeight/2.0f - font2.baseSize/2.0f - 10.0f);

        Vector2 fontPosition3 = new Vector2(screenWidth/2.0f - rlj.text.MeasureTextEx(font3, msg3, (float)font3.baseSize, 2.0f).x/2.0f,
                screenHeight/2.0f - font3.baseSize/2.0f + 50.0f);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // TODO.txt: Update variables here...
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawTextEx(font1, msg1, fontPosition1, (float)font1.baseSize, -3, Color.WHITE);
            rlj.text.DrawTextEx(font2, msg2, fontPosition2, (float)font2.baseSize, -2, Color.WHITE);
            rlj.text.DrawTextEx(font3, msg3, fontPosition3, (float)font3.baseSize, 2, Color.WHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.text.UnloadFont(font1);      // Font unloading
        rlj.text.UnloadFont(font2);      // Font unloading
        rlj.text.UnloadFont(font3);      // Font unloading
        //--------------------------------------------------------------------------------------
    }

}
