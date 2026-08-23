package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.*;

import java.io.IOException;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;

public class FontSDF{

    /*******************************************************************************************
     *
     *   raylib [text] example - TTF loading and usage
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


    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - SDF fonts");

        // NOTE: Textures/Fonts MUST be loaded after Window initialization (OpenGL context is required)

        String msg = "Signed Distance Fields";

        // Loading file to memory
        int fileSize = 0;
        byte[] fileData = null;
        try{
            fileData = rlj.files.LoadFileData("src/tests/resources/text/anonymous_pro_bold.ttf");
        } catch (IOException e){
            e.printStackTrace();
        }

        // Default font generation from TTF font
        Font fontDefault = new Font();
        fontDefault.baseSize = 16;
        fontDefault.glyphCount = 95;

        // Loading font data from memory data
        // Parameters > font size: 16, no chars array provided (0), chars count: 95 (autogenerate chars array)
        fontDefault.glyphs = rlj.text.LoadFontData(fileData, 16, null, 95, rText.FontType.FONT_DEFAULT);
        // Parameters > chars count: 95, font size: 16, chars padding in image: 4 px, pack method: 0 (default)
        Image atlas = rlj.text.GenImageFontAtlas(fontDefault, 0, 0);
        fontDefault.texture = rlj.textures.LoadTextureFromImage(atlas);
        rlj.textures.UnloadImage(atlas);

        // SDF font generation from TTF font
        Font fontSDF = new Font();
        fontSDF.baseSize = 16;
        fontSDF.glyphCount = 95;
        // Parameters > font size: 16, no chars array provided (0), chars count: 0 (defaults to 95)
        fontSDF.glyphs = rlj.text.LoadFontData(fileData, 16, null, 0, rText.FontType.FONT_SDF);
        // Parameters > chars count: 95, font size: 16, chars padding in image: 0 px, pack method: 1 (Skyline algorythm)
        atlas = rlj.text.GenImageFontAtlas(fontSDF, 0, 1);
        fontSDF.texture = rlj.textures.LoadTextureFromImage(atlas);
        rlj.textures.UnloadImage(atlas);

        // Load SDF required shader (we use default vertex shader)
        Shader shader = rlj.core.LoadShader(null, "src/tests/resources/text/shaders/glsl330/sdf.fs");
        rlj.textures.SetTextureFilter(fontSDF.texture, RLGL.rlTextureFilterMode.TEXTURE_FILTER_BILINEAR);    // Required for SDF font

        Vector2 fontPosition = new Vector2(40, screenHeight / 2.0f - 50);
        Vector2 textSize;
        float fontSize = 16.0f;
        int currentFont;            // 0 - fontDefault, 1 - fontSDF

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            fontSize += rlj.core.GetMouseWheelMove() * 8.0f;

            if (fontSize < 6) fontSize = 6;

            if (rlj.core.IsKeyDown(KEY_SPACE)){
                currentFont = 1;
            }
            else{
                currentFont = 0;
            }

            if (currentFont == 0){
                textSize = rlj.text.MeasureTextEx(fontDefault, msg, fontSize, 0);
            }
            else{
                textSize = rlj.text.MeasureTextEx(fontSDF, msg, fontSize, 0);
            }

            fontPosition.x = rlj.core.GetScreenWidth() / 2.0f - textSize.x / 2;
            fontPosition.y = rlj.core.GetScreenHeight() / 2.0f - textSize.y / 2 + 80;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (currentFont == 1){
                // NOTE: SDF fonts require a custom SDf shader to compute fragment color
                rlj.core.BeginShaderMode(shader);    // Activate SDF font shader
                rlj.text.DrawTextEx(fontSDF, msg, fontPosition, fontSize, 0, Color.BLACK);
                rlj.core.EndShaderMode();            // Activate our default shader for next drawings

                rlj.textures.DrawTexture(fontSDF.texture, 10, 10, Color.BLACK);
            }
            else{
                rlj.text.DrawTextEx(fontDefault, msg, fontPosition, fontSize, 0, Color.BLACK);
                rlj.textures.DrawTexture(fontDefault.texture, 10, 10, Color.BLACK);
            }

            if (currentFont == 1){
                rlj.text.DrawText("SDF!", 320, 20, 80, Color.RED);
            }
            else{
                rlj.text.DrawText("default font", 315, 40, 30, Color.GRAY);
            }

            rlj.text.DrawText("FONT SIZE: 16.0", rlj.core.GetScreenWidth() - 240, 20, 20, Color.DARKGRAY);
            rlj.text.DrawText("RENDER SIZE: " + fontSize, rlj.core.GetScreenWidth() - 240, 50, 20, Color.DARKGRAY);
            rlj.text.DrawText("Use MOUSE WHEEL to SCALE TEXT!", rlj.core.GetScreenWidth() - 240, 90, 10, Color.DARKGRAY);

            rlj.text.DrawText("HOLD SPACE to USE SDF FONT VERSION!", 340, rlj.core.GetScreenHeight() - 30, 20, Color.MAROON);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.text.UnloadFont(fontDefault);    // Default font unloading
        rlj.text.UnloadFont(fontSDF);        // SDF font unloading

        rlj.core.UnloadShader(shader);       // Unload SDF shader
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
