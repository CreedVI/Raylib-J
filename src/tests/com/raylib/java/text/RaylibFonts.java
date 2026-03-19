package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Vector2;

public class RaylibFonts{

    /*******************************************************************************************
     *
     *   raylib [text] example - raylib font loading and usage
     *
     *   NOTE: raylib is distributed with some free to use fonts (even for commercial pourposes!)
     *         To view details and credits for those fonts, check raylib license file
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    final static int MAX_FONTS = 8;

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - raylib fonts");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Font[] fonts = new Font[MAX_FONTS];

        fonts[0] = rlj.text.LoadFont("src/tests/resources/text/fonts/alagard.png");
        fonts[1] = rlj.text.LoadFont("src/tests/resources/text/fonts/pixelplay.png");
        fonts[2] = rlj.text.LoadFont("src/tests/resources/text/fonts/mecha.png");
        fonts[3] = rlj.text.LoadFont("src/tests/resources/text/fonts/setback.png");
        fonts[4] = rlj.text.LoadFont("src/tests/resources/text/fonts/romulus.png");
        fonts[5] = rlj.text.LoadFont("src/tests/resources/text/fonts/pixantiqua.png");
        fonts[6] = rlj.text.LoadFont("src/tests/resources/text/fonts/alpha_beta.png");
        fonts[7] = rlj.text.LoadFont("src/tests/resources/text/fonts/jupiter_crash.png");

        String[] messages = {
            "ALAGARD FONT designed by Hewett Tsoi",
            "PIXELPLAY FONT designed by Aleksander Shevchuk",
            "MECHA FONT designed by Captain Falcon",
            "SETBACK FONT designed by Brian Kent (AEnigma)",
            "ROMULUS FONT designed by Hewett Tsoi",
            "PIXANTIQUA FONT designed by Gerhard Grossmann",
            "ALPHA_BETA FONT designed by Brian Kent (AEnigma)",
            "JUPITER_CRASH FONT designed by Brian Kent (AEnigma)"
        };

        int[] spacings = { 2, 4, 8, 4, 3, 4, 4, 1 };

        Vector2[] positions = new Vector2[MAX_FONTS];

        for (int i = 0; i < MAX_FONTS; i++) {
            positions[i] = new Vector2();
            positions[i].x = screenWidth/2.0f - rlj.text.MeasureTextEx(fonts[i], messages[i], fonts[i].baseSize*2.0f, (float)spacings[i]).x/2.0f;
            positions[i].y = 60.0f + fonts[i].baseSize + 45.0f*i;
        }

        // Small Y position corrections
        positions[3].y += 8;
        positions[4].y += 2;
        positions[7].y -= 8;

        Color[] colors = { Color.MAROON, Color.ORANGE, Color.DARKGREEN, Color.DARKBLUE, Color.DARKPURPLE, Color.LIME, Color.GOLD, Color.RED };

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // TODO.txt: Update your variables here
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("free fonts included with raylib", 250, 20, 20, Color.DARKGRAY);
            rlj.shapes.DrawLine(220, 50, 590, 50, Color.DARKGRAY);

            for (int i = 0; i < MAX_FONTS; i++) {
                rlj.text.DrawTextEx(fonts[i], messages[i], positions[i], fonts[i].baseSize*2.0f, (float)spacings[i], colors[i]);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------

        // Fonts unloading
        for (int i = 0; i < MAX_FONTS; i++){
            rlj.text.UnloadFont(fonts[i]);
        }

        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
