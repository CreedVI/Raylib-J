package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Texture2D;

import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;

public class ImageGeneration{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Procedural images generation
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2017-2021 Wilhem Barbier (@nounoursheureux)
     *
     ********************************************************************************************/


    final static int NUM_TEXTURES = 6;      // Currently we have 6 generation algorithms

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - procedural images generation");

        Image verticalGradient = rlj.textures.GenImageGradientV(screenWidth, screenHeight, Color.RED, Color.BLUE);
        Image horizontalGradient = rlj.textures.GenImageGradientH(screenWidth, screenHeight, Color.RED, Color.BLUE);
        Image radialGradient = rlj.textures.GenImageGradientRadial(screenWidth, screenHeight, 0.0f, Color.WHITE, Color.BLACK);
        Image checked = rlj.textures.GenImageChecked(screenWidth, screenHeight, 32, 32, Color.RED, Color.BLUE);
        Image whiteNoise = rlj.textures.GenImageWhiteNoise(screenWidth, screenHeight, 0.5f);
        Image cellular = rlj.textures.GenImageCellular(screenWidth, screenHeight, 32);

        Texture2D[] textures = new Texture2D[ NUM_TEXTURES];

        textures[0] = rlj.textures.LoadTextureFromImage(verticalGradient);
        textures[1] = rlj.textures.LoadTextureFromImage(horizontalGradient);
        textures[2] = rlj.textures.LoadTextureFromImage(radialGradient);
        textures[3] = rlj.textures.LoadTextureFromImage(checked);
        textures[4] = rlj.textures.LoadTextureFromImage(whiteNoise);
        textures[5] = rlj.textures.LoadTextureFromImage(cellular);

        // Unload image data (CPU RAM)
        rlj.textures.UnloadImage(verticalGradient);
        rlj.textures.UnloadImage(horizontalGradient);
        rlj.textures.UnloadImage(radialGradient);
        rlj.textures.UnloadImage(checked);
        rlj.textures.UnloadImage(whiteNoise);
        rlj.textures.UnloadImage(cellular);

        int currentTexture = 0;

        rlj.core.SetTargetFPS(60);
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()){
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT) || rlj.core.IsKeyPressed(KEY_RIGHT)){
                currentTexture = (currentTexture + 1) % NUM_TEXTURES; // Cycle between the textures
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.textures.DrawTexture(textures[currentTexture], 0, 0, Color.WHITE);

            rlj.shapes.DrawRectangle(30, 400, 325, 30, rlj.textures.Fade(Color.SKYBLUE, 0.5f));
            rlj.shapes.DrawRectangleLines(30, 400, 325, 30, rlj.textures.Fade(Color.WHITE, 0.5f));
            rlj.text.DrawText("MOUSE LEFT BUTTON to CYCLE PROCEDURAL TEXTURES", 40, 410, 10, Color.WHITE);

            switch (currentTexture) {
                case 0 -> rlj.text.DrawText("VERTICAL GRADIENT", 560, 10, 20, Color.RAYWHITE);
                case 1 -> rlj.text.DrawText("HORIZONTAL GRADIENT", 540, 10, 20, Color.RAYWHITE);
                case 2 -> rlj.text.DrawText("RADIAL GRADIENT", 580, 10, 20, Color.LIGHTGRAY);
                case 3 -> rlj.text.DrawText("CHECKED", 680, 10, 20, Color.RAYWHITE);
                case 4 -> rlj.text.DrawText("WHITE NOISE", 640, 10, 20, Color.RED);
                case 5 -> rlj.text.DrawText("CELLULAR", 670, 10, 20, Color.RAYWHITE);
                default -> {
                }
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------

        // Unload textures data (GPU VRAM)
        for (int i = 0; i < NUM_TEXTURES; i++){
            rlj.textures.UnloadTexture(textures[i]);
        }
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
