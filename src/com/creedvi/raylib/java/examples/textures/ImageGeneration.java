package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_RIGHT;
import static com.creedvi.raylib.java.rlj.core.input.Mouse.MouseButton.MOUSE_LEFT_BUTTON;

public class ImageGeneration{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Procedural images generation
     *
     *   This example has been created using raylib 1.8 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2O17 Wilhem Barbier (@nounoursheureux)
     *
     ********************************************************************************************/


    final static int NUM_TEXTURES = 7;      // Currently we have 7 generation algorithms

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
        Image perlinNoise = rlj.textures.GenImagePerlinNoise(screenWidth, screenHeight, 50, 50, 4.0f);
        Image cellular = rlj.textures.GenImageCellular(screenWidth, screenHeight, 32);

        Texture2D[] textures = new Texture2D[ NUM_TEXTURES];

        textures[0] = rlj.textures.LoadTextureFromImage(verticalGradient);
        textures[1] = rlj.textures.LoadTextureFromImage(horizontalGradient);
        textures[2] = rlj.textures.LoadTextureFromImage(radialGradient);
        textures[3] = rlj.textures.LoadTextureFromImage(checked);
        textures[4] = rlj.textures.LoadTextureFromImage(whiteNoise);
        textures[5] = rlj.textures.LoadTextureFromImage(perlinNoise);
        textures[6] = rlj.textures.LoadTextureFromImage(cellular);

        // Unload image data (CPU RAM)
        rlj.textures.UnloadImage(verticalGradient);
        rlj.textures.UnloadImage(horizontalGradient);
        rlj.textures.UnloadImage(radialGradient);
        rlj.textures.UnloadImage(checked);
        rlj.textures.UnloadImage(whiteNoise);
        rlj.textures.UnloadImage(perlinNoise);
        rlj.textures.UnloadImage(cellular);

        int currentTexture = 0;

        rlj.core.SetTargetFPS(60);
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()){
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsMouseButtonPressed(MOUSE_LEFT_BUTTON) || rlj.core.IsKeyPressed(KEY_RIGHT)){
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

            switch (currentTexture){
                case 0:
                    rlj.text.DrawText("VERTICAL GRADIENT", 560, 10, 20, Color.RAYWHITE);
                    break;
                case 1:
                    rlj.text.DrawText("HORIZONTAL GRADIENT", 540, 10, 20, Color.RAYWHITE);
                    break;
                case 2:
                    rlj.text.DrawText("RADIAL GRADIENT", 580, 10, 20, Color.LIGHTGRAY);
                    break;
                case 3:
                    rlj.text.DrawText("CHECKED", 680, 10, 20, Color.RAYWHITE);
                    break;
                case 4:
                    rlj.text.DrawText("WHITE NOISE", 640, 10, 20, Color.RED);
                    break;
                case 5:
                    rlj.text.DrawText("PERLIN NOISE", 630, 10, 20, Color.RAYWHITE);
                    break;
                case 6:
                    rlj.text.DrawText("CELLULAR", 670, 10, 20, Color.RAYWHITE);
                    break;
                default:
                    break;
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

        //--------------------------------------------------------------------------------------
    }

}
