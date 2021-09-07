package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.rlgl.RLGL;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_SPACE;

public class BlendModes{

    /*******************************************************************************************
     *
     *   raylib [textures] example - blend modes
     *
     *   NOTE: Images are loaded in CPU memory (RAM); textures are loaded in GPU memory (VRAM)
     *
     *   This example has been created using raylib 3.5 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Karlo Licudine (@accidentalrebel) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2020 Karlo Licudine (@accidentalrebel)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - blend modes");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Image bgImage = rlj.textures.LoadImage("resources/cyberpunk_street_background.png"); // Loaded in CPU
        // memory (RAM)
        Texture2D bgTexture = rlj.textures.LoadTextureFromImage(bgImage); // Image converted to texture, GPU memory
        // (VRAM)

        Image fgImage = rlj.textures.LoadImage("resources/cyberpunk_street_foreground.png"); // Loaded in CPU
        // memory (RAM)
        Texture2D fgTexture = rlj.textures.LoadTextureFromImage(fgImage); // Image converted to texture, GPU memory
        // (VRAM)

        // Once image has been converted to texture and uploaded to VRAM, it can be unloaded from RAM
        rlj.textures.UnloadImage(bgImage);
        rlj.textures.UnloadImage(fgImage);

        final int blendCountMax = 4;
        int blendMode = RLGL.BlendMode.BLEND_ALPHA;

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_SPACE))
            {
                if (blendMode >= (blendCountMax - 1)) blendMode = 0;
                else blendMode++;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.textures.DrawTexture(bgTexture, screenWidth/2 - bgTexture.width/2,
                    screenHeight/2 - bgTexture.height/2, Color.WHITE);

            // Apply the blend mode and then draw the foreground texture
            rlj.core.BeginBlendMode(blendMode);
            rlj.textures.DrawTexture(fgTexture, screenWidth/2 - fgTexture.width/2, screenHeight/2 - fgTexture.height/2,
                    Color.WHITE);
            rlj.core.EndBlendMode();

            // Draw the texts
            rlj.text.DrawText("Press SPACE to change blend modes.", 310, 350, 10, Color.GRAY);

            switch (blendMode)
            {
                case RLGL.BlendMode.BLEND_ALPHA:
                    rlj.text.DrawText("Current: BLEND_ALPHA", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                    break;
                case RLGL.BlendMode.BLEND_ADDITIVE:
                    rlj.text.DrawText("Current: BLEND_ADDITIVE", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                    break;
                case RLGL.BlendMode.BLEND_MULTIPLIED:
                    rlj.text.DrawText("Current: BLEND_MULTIPLIED", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                    break;
                case RLGL.BlendMode.BLEND_ADD_COLORS:
                    rlj.text.DrawText("Current: BLEND_ADD_COLORS", (screenWidth / 2) - 60, 370, 10, Color.GRAY);
                    break;
                default:
                    break;
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
        //--------------------------------------------------------------------------------------
    }
}
