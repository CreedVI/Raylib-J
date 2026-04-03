package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.structs.*;

public class Eratosthenes{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Sieve of Eratosthenes
     *
     *   Sieve of Eratosthenes, the earliest known (ancient Greek) prime number sieve.
     *
     *   "Sift the twos and sift the threes,
     *    The Sieve of Eratosthenes.
     *    When the multiples sublime,
     *    the numbers that are left are prime."
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3).
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 ProfJski
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - Sieve of Eratosthenes");

        RenderTexture target = rlj.textures.LoadRenderTexture(screenWidth, screenHeight);

        // Load Eratosthenes shader
        // NOTE: Defining null for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, "src/tests/resources/shaders/shaders/glsl330/eratosthenes.fs");

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Nothing to do here, everything is happening in the shader
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginTextureMode(target);   // Enable drawing to texture
            rlj.core.ClearBackground(Color.BLACK); // Clear the render texture

            // Draw a rectangle in shader mode to be used as shader canvas
            // NOTE: Rectangle uses font white character texture coordinates,
            // so shader can not be applied here directly because input vertexTexCoord
            // do not represent full screen coordinates (space where want to apply shader)
            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight(), Color.BLACK);
            rlj.core.EndTextureMode();   // End drawing to texture (now we have a blank texture available for the shader)

            rlj.core.BeginShaderMode(shader);
            // NOTE: Render texture must be y-flipped due to default OpenGL coordinates (left-bottom)
            rlj.textures.DrawTextureRec(target.texture, new Rectangle(0, 0, (float)target.texture.width, (float)-target.texture.height),
                                        new Vector2(0.0f, 0.0f), Color.WHITE);
            rlj.core.EndShaderMode();

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);           // Unload shader
        rlj.textures.UnloadRenderTexture(target);    // Unload texture
        //--------------------------------------------------------------------------------------
    }

}
