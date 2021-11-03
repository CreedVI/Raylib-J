package com.raylib.java;

import com.raylib.java.core.Color;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.rlgl.shader.Shader;
import com.raylib.java.text.Font;
import com.raylib.java.text.Text;
import com.raylib.java.textures.Image;
import com.raylib.java.utils.FileIO;

import java.io.IOException;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.rlgl.RLGL.TextureFilterMode.TEXTURE_FILTER_BILINEAR;

public class main{

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
            fileData = FileIO.LoadFileData("resources/anonymous_pro_bold.ttf");
        } catch (IOException e){
            e.printStackTrace();
        }

        // Default font generation from TTF font
        Font fontDefault = new Font();
        fontDefault.baseSize = 16;
        fontDefault.charsCount = 95;
        fontDefault.charsPadding = 4;

        // Loading font data from memory data
        // Parameters > font size: 16, no chars array provided (0), chars count: 95 (autogenerate chars array)
        fontDefault.chars = rlj.text.LoadFontData(fileData, fileSize, 16, null, 95, Text.FontType.FONT_DEFAULT);
        // Parameters > chars count: 95, font size: 16, chars padding in image: 4 px, pack method: 0 (default)
        Image atlas = rlj.text.GenImageFontAtlas(fontDefault, 0);
        fontDefault.texture = rlj.textures.LoadTextureFromImage(atlas);
        rlj.textures.UnloadImage(atlas);

        // SDF font generation from TTF font
        Font fontSDF = new Font();
        fontSDF.baseSize = 16;
        fontSDF.charsCount = 95;
        fontSDF.charsPadding = 0;
        // Parameters > font size: 16, no chars array provided (0), chars count: 0 (defaults to 95)
        fontSDF.chars = rlj.text.LoadFontData(fileData, fileSize, 16, null, 0, Text.FontType.FONT_SDF);
        // Parameters > chars count: 95, font size: 16, chars padding in image: 0 px, pack method: 1 (Skyline algorythm)
        atlas = rlj.text.GenImageFontAtlas(fontSDF, 1);
        fontSDF.texture = rlj.textures.LoadTextureFromImage(atlas);
        rlj.textures.UnloadImage(atlas);


        FileIO.UnloadFileData(fileData);      // Free memory from loaded file

        // Load SDF required shader (we use default vertex shader)
        Shader shader = rlj.core.LoadShader(null, "resources/shaders/glsl330/sdf.fs");
        rlj.textures.SetTextureFilter(fontSDF.texture, TEXTURE_FILTER_BILINEAR);    // Required for SDF font

        Vector2 fontPosition = new Vector2(40, screenHeight/2.0f - 50);
        Vector2 textSize = new Vector2();
        float fontSize = 16.0f;
        int currentFont = 0;            // 0 - fontDefault, 1 - fontSDF

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            fontSize += rlj.core.GetMouseWheelMove()*8.0f;

            if (fontSize < 6) fontSize = 6;

            if (rlj.core.IsKeyDown(KEY_SPACE)) currentFont = 1;
            else currentFont = 0;

            if (currentFont == 0) textSize = rlj.text.MeasureTextEx(fontDefault, msg, fontSize, 0);
            //else textSize = rlj.text.MeasureTextEx(fontSDF, msg, fontSize, 0);

            fontPosition.x = rlj.core.GetScreenWidth()/2 - textSize.x/2;
            fontPosition.y = rlj.core.GetScreenHeight()/2 - textSize.y/2 + 80;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (currentFont == 1)
            {
                // NOTE: SDF fonts require a custom SDf shader to compute fragment color
                rlj.core.BeginShaderMode(shader);    // Activate SDF font shader
                rlj.text.DrawTextEx(fontSDF, msg, fontPosition, fontSize, 0, Color.BLACK);
                rlj.core.EndShaderMode();            // Activate our default shader for next drawings

                rlj.textures.DrawTexture(fontSDF.texture, 10, 10, Color.BLACK);
            }
            else
            {
                rlj.text.DrawTextEx(fontDefault, msg, fontPosition, fontSize, 0, Color.BLACK);
                rlj.textures.DrawTexture(fontDefault.texture, 10, 10, Color.BLACK);
            }

            if (currentFont == 1) rlj.text.DrawText("SDF!", 320, 20, 80, Color.RED);
            else rlj.text.DrawText("default font", 315, 40, 30, Color.GRAY);

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
        //--------------------------------------------------------------------------------------
    }

}