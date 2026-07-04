package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Texture2D;

public class ImageLoading{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Image loading and texture creation
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - image loading");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)

        Image image = rlj.textures.LoadImage("src/tests/resources/textures/raylib-j_logo.png"); // Loaded in CPU memory (RAM)
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

            rlj.text.DrawText("this IS a texture loaded from an image!", 300, 370, 10, Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);       // Texture unloading
        rlj.core.CloseWindow();
    }

}
