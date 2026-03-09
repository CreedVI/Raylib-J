package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.rlgl.RLGL.rlBlendMode;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Texture2D;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.rlgl.RLGL.rlBlendMode.*;

public class BlendModes{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - blend modes
     *
     *   NOTE: Images are loaded in CPU memory (RAM); textures are loaded in GPU memory (VRAM)
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2020 Karlo Licudine (@accidentalrebel)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - blend modes");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Image bgImage = rlj.textures.LoadImage("src/tests/resources/textures/cyberpunk_street_background.png"); // Loaded in CPU
        // memory (RAM)
        Texture2D bgTexture = rlj.textures.LoadTextureFromImage(bgImage); // Image converted to texture, GPU memory
        // (VRAM)

        Image fgImage = rlj.textures.LoadImage("src/tests/resources/textures/cyberpunk_street_foreground.png"); // Loaded in CPU
        // memory (RAM)
        Texture2D fgTexture = rlj.textures.LoadTextureFromImage(fgImage); // Image converted to texture, GPU memory
        // (VRAM)

        // Once image has been converted to texture and uploaded to VRAM, it can be unloaded from RAM
        rlj.textures.UnloadImage(bgImage);
        rlj.textures.UnloadImage(fgImage);

        final int blendCountMax = 4;
        int blendMode = RL_BLEND_ALPHA.GetMode();

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_SPACE)) {
                if (blendMode >= (blendCountMax - 1)) {
                    blendMode = 0;
                }
                else {
                    blendMode++;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.textures.DrawTexture(bgTexture, screenWidth/2 - bgTexture.width/2,
                    screenHeight/2 - bgTexture.height/2, Color.WHITE);

            // Apply the blend mode and then draw the foreground texture
            rlj.core.BeginBlendMode(GetBlendModeFromInt(blendMode));
            rlj.textures.DrawTexture(fgTexture, screenWidth/2 - fgTexture.width/2, screenHeight/2 - fgTexture.height/2,
                    Color.WHITE);
            rlj.core.EndBlendMode();

            // Draw the texts
            rlj.text.DrawText("Press SPACE to change blend modes.", 310, 350, 10, Color.GRAY);

            switch (GetBlendModeFromInt(blendMode)) {
                case RL_BLEND_ALPHA ->
                        rlj.text.DrawText("Current: BLEND_ALPHA", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                case RL_BLEND_ADDITIVE ->
                        rlj.text.DrawText("Current: BLEND_ADDITIVE", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                case RL_BLEND_MULTIPLIED ->
                        rlj.text.DrawText("Current: BLEND_MULTIPLIED", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                case RL_BLEND_ADD_COLORS ->
                        rlj.text.DrawText("Current: BLEND_ADD_COLORS", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                default -> {
                }
            }

            rlj.text.DrawText("(c) Cyberpunk Street Environment by Luis Zuno (@ansimuz)", screenWidth - 330,
                    screenHeight - 20, 10, Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(fgTexture); // Unload foreground texture
        rlj.textures.UnloadTexture(bgTexture); // Unload background texture
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

    private static rlBlendMode GetBlendModeFromInt(int mode) {
        return switch (mode) {
            case 1 -> RL_BLEND_ADDITIVE;
            case 2 -> RL_BLEND_MULTIPLIED;
            case 3 -> RL_BLEND_ADD_COLORS;
            case 4 -> RL_BLEND_SUBTRACT_COLORS;
            case 5 -> RL_BLEND_ALPHA_PREMULTIPLY;
            case 6 -> RL_BLEND_CUSTOM;
            case 7 -> RL_BLEND_CUSTOM_SEPARATE;
            default -> RL_BLEND_ALPHA;
        };
    }
}
