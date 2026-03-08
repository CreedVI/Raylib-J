package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Shader;

public class PaletteSwitch{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Color palette switch
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3), to test this example
     *         on OpenGL ES 2.0 platforms (Android, Raspberry Pi, HTML5), use #version 100 shaders
     *         raylib comes with shaders ready for both versions, check raylib/shaders install folder
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 Marco Lizza (@MarcoLizza)
     *
     ********************************************************************************************/


    final static int MAX_PALETTES = 3;
    final static int COLORS_PER_PALETTE = 8;

    static float[][] palettes = {
            {   // 3-BIT RGB
                    0, 0, 0,
                    255, 0, 0,
                    0, 255, 0,
                    0, 0, 255,
                    0, 255, 255,
                    255, 0, 255,
                    255, 255, 0,
                    255, 255, 255,
            },
            {   // AMMO-8 (GameBoy-like)
                    4, 12, 6,
                    17, 35, 24,
                    30, 58, 41,
                    48, 93, 66,
                    77, 128, 97,
                    137, 162, 87,
                    190, 220, 127,
                    238, 255, 204,
            },
            {   // RKBV (2-strip film)
                    21, 25, 26,
                    138, 76, 88,
                    217, 98, 117,
                    230, 184, 193,
                    69, 107, 115,
                    75, 151, 166,
                    165, 189, 194,
                    255, 245, 247,
            }
    };

    static String[] paletteText = {
            "3-BIT RGB", "AMMO-8 (GameBoy-like)", "RKBV (2-strip film)"
    };

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - color palette switch");

        // Load shader to be used on some parts drawing
        // NOTE 1: Using GLSL 330 shader version, on OpenGL ES 2.0 use GLSL 100 shader version
        // NOTE 2: Defining 0 (NULL) for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, "resources/shaders/glsl330/palette_switch.fs");

        // Get variable (uniform) location on the shader to connect with the program
        // NOTE: If uniform variable could not be found in the shader, function returns -1
        int paletteLoc = rlj.core.GetShaderLocation(shader, "palette");

        int currentPalette = 0;
        int lineHeight = screenHeight / COLORS_PER_PALETTE;

        rlj.core.SetTargetFPS(60);                       // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())            // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(Keyboard.KEY_RIGHT)){
                currentPalette++;
            }
            else if (rlj.core.IsKeyPressed(Keyboard.KEY_LEFT)){
                currentPalette--;
            }

            if (currentPalette >= MAX_PALETTES){
                currentPalette = 0;
            }
            else if (currentPalette < 0){
                currentPalette = MAX_PALETTES - 1;
            }

            // Send new value to the shader to be used on drawing.
            // NOTE: We are sending RGB triplets w/o the alpha channel
            rlj.core.SetShaderValueV(shader, paletteLoc, palettes[currentPalette], RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_IVEC3);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginShaderMode(shader);

            for (int i = 0; i < COLORS_PER_PALETTE; i++){
                // Draw horizontal screen-wide rectangles with increasing "palette index"
                // The used palette index is encoded in the RGB components of the pixel
                rlj.shapes.DrawRectangle(0, lineHeight * i, rlj.core.GetScreenWidth(), lineHeight, new Color(i, i, i, 255));
            }

            rlj.core.EndShaderMode();

            rlj.text.DrawText("< >", 10, 10, 30, Color.DARKBLUE);
            rlj.text.DrawText("CURRENT PALETTE:", 60, 15, 20, Color.RAYWHITE);
            rlj.text.DrawText(paletteText[currentPalette], 300, 15, 20, Color.RED);

            rlj.text.DrawFPS(700, 15);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);       // Unload shader
        //--------------------------------------------------------------------------------------

    }

}
