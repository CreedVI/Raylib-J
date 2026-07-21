package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Texture2D;

public class SvgLoading {

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - SVG loading");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)

        Image image = rlj.textures.LoadImageSvg("src/tests/resources/textures/cheese.svg", 300, 300); // Loaded in CPU memory (RAM)
        // Image image = rlj.textures.LoadImageSvg("src/tests/resources/textures/cheese-wedge.svg", 300, 300); // Loaded in CPU memory (RAM)
        Texture2D texture = rlj.textures.LoadTextureFromImage(image); // Image converted to texture, GPU memory (VRAM)

        rlj.textures.UnloadImage(image);   // Once image has been converted to texture and uploaded to VRAM, it can be
        // unloaded from RAM
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {   // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            // TODO: Update your variables here
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.textures.DrawTexture(texture, screenWidth/2 - texture.width/2, screenHeight/2 - texture.height/2,
                                     Color.WHITE);

            rlj.text.DrawText("This is a texture loaded from an SVG!", 200, 380, 20, Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);       // Texture unloading
        rlj.core.CloseWindow();
    }

}
