package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

public class ImageLoading{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Image loading and texture creation
     *
     *   NOTE: Images are loaded in CPU memory (RAM); textures are loaded in GPU memory (VRAM)
     *
     *   This example has been created using raylib 1.3 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2015 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/


    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - image loading");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)

        Image image = rlj.textures.LoadImage("resources/raylib-j_logo.png"); // Loaded in CPU memory (RAM)
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
    }

}
