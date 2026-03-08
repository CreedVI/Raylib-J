package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Texture2D;

public class TextureToImage{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Retrieve image data from texture: GetTextureData()
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
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - texture to image");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)

        Image image = rlj.textures.LoadImage("resources/raylib-j_logo.png");  // Load image data into CPU memory (RAM)
        Texture2D texture = rlj.textures.LoadTextureFromImage(image);       // Image converted to texture, GPU memory
        // (RAM -> VRAM)
        rlj.textures.UnloadImage(image); // Unload image data from CPU memory (RAM)

        image = rlj.textures.LoadImageFromTexture(texture); // Retrieve image data from GPU memory (VRAM -> RAM)
        rlj.textures.UnloadTexture(texture); // Unload texture from GPU memory (VRAM)

        texture = rlj.textures.LoadTextureFromImage(image); // Recreate texture from retrieved image data (RAM -> VRAM)
        rlj.textures.UnloadImage(image); // Unload retrieved image data from CPU memory (RAM)
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // TODO.txt: Update your variables here
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
        //--------------------------------------------------------------------------------------
    }
}