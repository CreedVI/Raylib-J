package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

public class BackgroundScrolling{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Background scrolling
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - background scrolling");

        // NOTE: Be careful, background width must be equal or bigger than screen width
        // if not, texture should be draw more than two times for scrolling effect
        Texture2D background = rlj.textures.LoadTexture("src/tests/resources/textures/cyberpunk_street_background.png");
        Texture2D midground = rlj.textures.LoadTexture("src/tests/resources/textures/cyberpunk_street_midground.png");
        Texture2D foreground = rlj.textures.LoadTexture("src/tests/resources/textures/cyberpunk_street_foreground.png");

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