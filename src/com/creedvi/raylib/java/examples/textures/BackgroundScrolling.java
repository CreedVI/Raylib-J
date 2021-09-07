package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

public class BackgroundScrolling{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Background scrolling
     *
     *   This example has been created using raylib 2.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2019 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/


    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - background scrolling");

        // NOTE: Be careful, background width must be equal or bigger than screen width
        // if not, texture should be draw more than two times for scrolling effect
        Texture2D background = rlj.textures.LoadTexture("resources/cyberpunk_street_background.png");
        Texture2D midground = rlj.textures.LoadTexture("resources/cyberpunk_street_midground.png");
        Texture2D foreground = rlj.textures.LoadTexture("resources/cyberpunk_street_foreground.png");

        float scrollingBack = 0.0f;
        float scrollingMid = 0.0f;
        float scrollingFore = 0.0f;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            scrollingBack -= 0.1f;
            scrollingMid -= 0.5f;
            scrollingFore -= 1.0f;

            // NOTE: Texture is scaled twice its size, so it sould be considered on scrolling
            if (scrollingBack <= -background.width*2) scrollingBack = 0;
            if (scrollingMid <= -midground.width*2) scrollingMid = 0;
            if (scrollingFore <= -foreground.width*2) scrollingFore = 0;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(rlj.textures.GetColor(0x052c46ff));

            // Draw background image twice
            // NOTE: Texture is scaled twice its size
            rlj.textures.DrawTextureEx(background, new Vector2(scrollingBack, 20), 0.0f, 2.0f, Color.WHITE);
            rlj.textures.DrawTextureEx(background, new Vector2(background.width*2 + scrollingBack, 20), 0.0f, 2.0f,
                    Color.WHITE);

            // Draw midground image twice
            rlj.textures.DrawTextureEx(midground, new Vector2(scrollingMid, 20), 0.0f, 2.0f, Color.WHITE);
            rlj.textures.DrawTextureEx(midground, new Vector2(midground.width*2 + scrollingMid, 20), 0.0f, 2.0f,
                    Color.WHITE);

            // Draw foreground image twice
            rlj.textures.DrawTextureEx(foreground, new Vector2(scrollingFore, 70), 0.0f, 2.0f, Color.WHITE);
            rlj.textures.DrawTextureEx(foreground, new Vector2(foreground.width*2 + scrollingFore, 70), 0.0f, 2.0f,
                    Color.WHITE);

            rlj.text.DrawText("BACKGROUND SCROLLING & PARALLAX", 10, 10, 20, Color.RED);
            rlj.text.DrawText("(c) Cyberpunk Street Environment by Luis Zuno (@ansimuz)", screenWidth - 330,
                    screenHeight - 20, 10, Color.RAYWHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(background);  // Unload background texture
        rlj.textures.UnloadTexture(midground);   // Unload midground texture
        rlj.textures.UnloadTexture(foreground);  // Unload foreground texture
        //--------------------------------------------------------------------------------------
    }
}