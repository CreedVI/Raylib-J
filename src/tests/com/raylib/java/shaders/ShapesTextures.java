package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

public class ShapesTextures{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Apply a shader to some shape or texture
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3), to test this example
     *         on OpenGL ES 2.0 platforms (Android, Raspberry Pi, HTML5), use #version 100 shaders
     *         raylib comes with shaders ready for both versions, check raylib/shaders install folder
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - shapes and texture shaders");

        Texture2D fudesumi = rlj.textures.LoadTexture("src/tests/resources/shaders/fudesumi.png");

        // Load shader to be used on some parts drawing
        // NOTE 1: Using GLSL 330 shader version, on OpenGL ES 2.0 use GLSL 100 shader version
        // NOTE 2: Defining 0 (NULL) for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, "src/tests/resources/shaders/shaders/glsl330/grayscale.fs");

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

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

            // Start drawing with default shader

            rlj.text.DrawText("USING DEFAULT SHADER", 20, 40, 10, Color.RED);

            rlj.shapes.DrawCircle(80, 120, 35, Color.DARKBLUE);
            rlj.shapes.DrawCircleGradient(new Vector2(80, 220), 60, Color.GREEN, Color.SKYBLUE);
            rlj.shapes.DrawCircleLines(80, 340, 80, Color.DARKBLUE);


            // Activate our custom shader to be applied on next shapes/textures drawings
            rlj.core.BeginShaderMode(shader);

            rlj.text.DrawText("USING CUSTOM SHADER", 190, 40, 10, Color.RED);

            rlj.shapes.DrawRectangle(250 - 60, 90, 120, 60, Color.RED);
            rlj.shapes.DrawRectangleGradientH(250 - 90, 170, 180, 130, Color.MAROON, Color.GOLD);
            rlj.shapes.DrawRectangleLines(250 - 40, 320, 80, 60, Color.ORANGE);

            // Activate our default shader for next drawings
            rlj.core.EndShaderMode();

            rlj.text.DrawText("USING DEFAULT SHADER", 370, 40, 10, Color.RED);

            rlj.shapes.DrawTriangle(new Vector2(430, 80),
                         new Vector2(430 - 60, 150),
                         new Vector2(430 + 60, 150), Color.VIOLET);

            rlj.shapes.DrawTriangleLines(new Vector2(430, 160),
                              new Vector2(430 - 20, 230),
                              new Vector2(430 + 20, 230), Color.DARKBLUE);

            rlj.shapes.DrawPoly(new Vector2(430, 320), 6, 80, 0, Color.BROWN);

            // Activate our custom shader to be applied on next shapes/textures drawings
            rlj.core.BeginShaderMode(shader);

            rlj.textures.DrawTexture(fudesumi, 500, -30, Color.WHITE);    // Using custom shader

            // Activate our default shader for next drawings
            rlj.core.EndShaderMode();

            rlj.text.DrawText("(c) Fudesumi sprite by Eiden Marsal", 380, screenHeight - 20, 10, Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);       // Unload shader
        rlj.textures.UnloadTexture(fudesumi);    // Unload texture
        //--------------------------------------------------------------------------------------
    }

}
