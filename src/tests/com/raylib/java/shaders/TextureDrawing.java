package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;

public class TextureDrawing{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Texture drawing
     *
     *   This example illustrates how to draw on a blank texture using a shader
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 Michał Ciesielski
     *
     ********************************************************************************************/


    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - texture drawing");

        Image imBlank = rlj.textures.GenImageColor(1024, 1024, Color.BLANK);
        Texture2D texture = rlj.textures.LoadTextureFromImage(imBlank);  // Load blank texture to fill on shader
        rlj.textures.UnloadImage(imBlank);

        // NOTE: Using GLSL 330 shader version, on OpenGL ES 2.0 use GLSL 100 shader version
        Shader shader = rlj.core.LoadShader(null, "src/tests/resources/shaders/shaders/glsl330/cubes_panning.fs");

        float time = 0.0f;
        int timeLoc = rlj.core.GetShaderLocation(shader, "uTime");
        rlj.core.SetShaderValue(shader, timeLoc, new float[]{time}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        // -------------------------------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            time = (float) rlj.core.GetTime();
            rlj.core.SetShaderValue(shader, timeLoc, new float[]{time}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginShaderMode(shader);    // Enable our custom shader for next shapes/textures drawings
            rlj.textures.DrawTexture(texture, 0, 0, Color.WHITE);  // Drawing BLANK texture, all magic happens on shader
            rlj.core.EndShaderMode();            // Disable our custom shader, return to default shader

            rlj.text.DrawText("BACKGROUND is PAINTED and ANIMATED on SHADER!", 10, 10, 20, Color.MAROON);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);
        //--------------------------------------------------------------------------------------
    }

}
