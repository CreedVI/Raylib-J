package com.creedvi.raylib.java.examples.text;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.text.Font;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_SPACE;

public class FontLoading{

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [text] example - font loading");

        // Define characters to draw
        // NOTE: raylib supports UTF-8 encoding, following list is actually codified as UTF8 internally
        String msg = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHI\nJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmn\nopqrstuvwxyz" +
            "{|}~¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓ\nÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷\nøùúûüýþÿ";

        // NOTE: Textures/Fonts MUST be loaded after Window initialization (OpenGL context is required)

        // BMFont (AngelCode) : Font data and image atlas have been generated using external program
        //Font fontBm = rlj.text.LoadFont("resources/pixantiqua.fnt");

        // TTF font : Font data and atlas are generated directly from TTF
        // NOTE: We define a font base size of 32 pixels tall and up-to 250 characters
        Font fontTtf = rlj.text.LoadFontEx("resources/pixantiqua.ttf", 32, null, 250);

        boolean useTtf = true;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyDown(KEY_SPACE)) useTtf = true;
            else //useTtf = false;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("Hold SPACE to use TTF generated font", 20, 20, 20, Color.LIGHTGRAY);

            if (!useTtf)
            {
                //TODO
                //rlj.text.DrawTextEx(fontBm, msg, (Vector2){ 20.0f, 100.0f }, (float)fontBm.baseSize, 2, MAROON);
                //rlj.text.DrawText("Using BMFont (Angelcode) imported", 20, GetScreenHeight() - 30, 20, GRAY);
            }
            else
            {
                rlj.text.DrawTextEx(fontTtf, msg, new Vector2(20, 100), (float)fontTtf.baseSize, 2, Color.LIME);
                rlj.text.DrawText("Using TTF font generated", 20, rlj.core.GetScreenHeight() - 30, 20, Color.GRAY);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        //UnloadFont(fontBm);     // AngelCode Font unloading
        rlj.text.UnloadFont(fontTtf);    // TTF Font unloading

    }

}
