package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.*;

import static com.raylib.java.structs.Color.*;

public class ImageDrawing{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Image processing
     *
     *   NOTE: Images are loaded in CPU memory (RAM); textures are loaded in GPU memory (VRAM)
     *
     *   This example has been created using raylib 3.5 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2016 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - image processing");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)

        Image cat = rlj.textures.LoadImage("src/tests/resources/textures/cat.png"); // Load image in CPU memory (RAM)
        cat = rlj.textures.ImageCrop(cat, new Rectangle(100, 10, 280, 380));   // Crop an image piece
        cat = rlj.textures.ImageFlipHorizontal(cat);                                         // Flip cropped image horizontally
        cat = rlj.textures.ImageResize(cat, 150, 200);                  // Resize flipped-cropped image

        Image parrots = rlj.textures.LoadImage("src/tests/resources/textures/parrots.png");     // Load image in CPU memory (RAM)

        // Draw one image over the other with a scaling of 1.5f
        parrots = rlj.textures.ImageDraw(parrots, cat, new Rectangle(0, 0, (float)cat.width, (float)cat.height), new Rectangle(30, 40, cat.width*1.5f, cat.height*1.5f), WHITE);
        parrots = rlj.textures.ImageCrop(parrots, new Rectangle(0, 50, (float)parrots.width, (float)parrots.height - 100 )); // Crop resulting image

        // Draw on the image with a few image draw methods
        parrots = rlj.textures.ImageDrawPixel(parrots, 10, 10, RAYWHITE);
        parrots = rlj.textures.ImageDrawCircleLines(parrots, 10, 10, 5, RAYWHITE);
        parrots = rlj.textures.ImageDrawRectangle(parrots, 5, 20, 10, 10, RAYWHITE);

        rlj.textures.UnloadImage(cat);       // Unload image from RAM

        // Load custom font for frawing on image
        Font font = rlj.text.LoadFont("resources/custom_jupiter_crash.png");

        // Draw over image using custom font
        parrots = rlj.textures.ImageDrawTextEx(parrots, font, "PARROTS & CAT", new Vector2(300, 230 ), (float)font.baseSize, -2, WHITE);

        rlj.text.UnloadFont(font);       // Unload custom font (already drawn used on image)

        Texture2D texture = rlj.textures.LoadTextureFromImage(parrots);      // Image converted to texture, uploaded to GPU memory (VRAM)
        rlj.textures.UnloadImage(parrots);   // Once image has been converted to texture and uploaded to VRAM, it can be unloaded from RAM

        rlj.core.SetTargetFPS(60);
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // TODO: Update your variables here
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.textures.DrawTexture(texture, screenWidth/2 - texture.width/2, screenHeight/2 - texture.height/2 - 40, WHITE);
            rlj.shapes.DrawRectangleLines(screenWidth/2 - texture.width/2, screenHeight/2 - texture.height/2 - 40, texture.width, texture.height, DARKGRAY);

            rlj.text.DrawText("We are drawing only one texture from various images composed!", 240, 350, 10, DARKGRAY);
            rlj.text.DrawText("Source images have been cropped, scaled, flipped and copied one over the other.", 190, 370, 10, DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);       // Texture unloading

        rlj.core.CloseWindow();                // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }

}

