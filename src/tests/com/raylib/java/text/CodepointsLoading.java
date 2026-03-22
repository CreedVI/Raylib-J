package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Vector2;

import java.util.Arrays;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.rlgl.RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR;
import static com.raylib.java.structs.Color.*;

public class CodepointsLoading {

    /*******************************************************************************************
     *
     *   raylib [text] example - Codepoints loading
     *
     *   Example originally created with raylib 4.2, last time updated with raylib 2.5
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2022-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    // Text to be displayed, must be UTF-8 (save this code file as UTF-8)
    // NOTE: It can contain all the required text for the game,
    // this text will be scanned to get all the required codepoints
    static String text = "いろはにほへと　ちりぬるを\nわかよたれそ　つねならむ\nうゐのおくやま　けふこえて\nあさきゆめみし　ゑひもせす";

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - codepoints loading");

        // Get codepoints from text
        int[] codepoints = rlj.text.LoadCodepoints(text);
        int codepointCount = codepoints.length;

        // Removed duplicate codepoints to generate smaller font atlas
        int []codepointsNoDups = CodepointRemoveDuplicates(codepoints);
        int codepointsNoDupsCount = codepointsNoDups.length;
        rlj.text.UnloadCodepoints(codepoints);

        // Load font containing all the provided codepoint glyphs
        // A texture font atlas is automatically generated
        Font font = rlj.text.LoadFontEx("src/tests/resources/text/DotGothic16-Regular.ttf", 36, codepointsNoDups, codepointsNoDupsCount);

        // Set bilinear scale filter for better font scaling
        rlj.textures.SetTextureFilter(font.texture, RL_TEXTURE_FILTER_BILINEAR);

        // Free codepoints, atlas has already been generated
        rlj.text.UnloadCodepoints(codepointsNoDups);

        boolean showFontAtlas = false;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_SPACE)) {
                showFontAtlas = !showFontAtlas;
            }

            // Testing code: getting next and previous codepoints on provided text
            if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
                // Get next codepoint in string and move pointer
                // codepoint = rlj.text.GetCodepointNext(text.toCharArray());
                // ptr += codepointSize;
            }
            else if (rlj.core.IsKeyPressed(KEY_LEFT)) {
                // Get previous codepoint in string and move pointer
                // codepoint = rlj.text.GetCodepointPrevious(text.toCharArray());
                // ptr -= codepointSize;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), 70, BLACK);
            // rlj.text.DrawText(rlj.text.TextFormat("Total codepoints contained in provided text: %d", codepointCount), 10, 10, 20, GREEN);
            // rlj.text.DrawText(rlj.text.TextFormat("Total codepoints required for font atlas (duplicates excluded): %d", codepointsNoDupsCount), 10, 40, 20, GREEN);

            if (showFontAtlas) {
                // Draw generated font texture atlas containing provided codepoints
                rlj.textures.DrawTexture(font.texture, 150, 100, BLACK);
                rlj.shapes.DrawRectangleLines(150, 100, font.texture.width, font.texture.height, BLACK);
            }
            else {
                // Draw provided text with laoded font, containing all required codepoint glyphs
                rlj.text.DrawTextEx(font, text, new Vector2(160, 110), 48, 5, BLACK);
            }

            rlj.text.DrawText("Press SPACE to toggle font atlas view!", 10, rlj.core.GetScreenHeight() - 30, 20, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.text.UnloadFont(font);     // Unload font

        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

    // Remove codepoint duplicates if requested
    // WARNING: This process could be a bit slow if there text to process is very long
    static int[] CodepointRemoveDuplicates(int[] codepoints) {
        int codepointsNoDupsCount = codepoints.length;
        int[] codepointsNoDups = new int[codepoints.length];
        System.arraycopy(codepoints, 0, codepointsNoDups, 0, codepoints.length);

        // Remove duplicates
        for (int i = 0; i < codepointsNoDupsCount; i++) {
            for (int j = i + 1; j < codepointsNoDupsCount; j++) {
                if (codepointsNoDups[i] == codepointsNoDups[j]) {
                    for (int k = j; k + 1 < codepointsNoDupsCount; k++) {
                        codepointsNoDups[k] = codepointsNoDups[k + 1];
                    }

                    codepointsNoDupsCount--;
                    j--;
                }
            }
        }

        return Arrays.copyOf(codepointsNoDups, codepointsNoDupsCount);
    }

}
