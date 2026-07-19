package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;

public class TextureWaves{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Texture Waves
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
     *   Copyright (c) 2019 Anata (@anatagawa)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - texture waves");

        // Load texture texture to apply shaders
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/shaders/space.png");

        // Load shader and setup location points and values
        Shader shader = rlj.core.LoadShader(null, "src/tests/resources/shaders/shaders/glsl330/wave.fs");

        int secondsLoc = rlj.core.GetShaderLocation(shader, "secondes");
        int freqXLoc = rlj.core.GetShaderLocation(shader, "freqX");
        int freqYLoc = rlj.core.GetShaderLocation(shader, "freqY");
        int ampXLoc = rlj.core.GetShaderLocation(shader, "ampX");
        int ampYLoc = rlj.core.GetShaderLocation(shader, "ampY");
        int speedXLoc = rlj.core.GetShaderLocation(shader, "speedX");
        int speedYLoc = rlj.core.GetShaderLocation(shader, "speedY");

        // Shader uniform values that can be updated at any time
        float[] freqX = {25.0f};
        float[] freqY = {25.0f};
        float[] ampX = {5.0f};
        float[] ampY = {5.0f};
        float[] speedX = {8.0f};
        float[] speedY = {8.0f};

        float[] screenSize = { (float) rlj.core.GetScreenWidth(), (float) rlj.core.GetScreenHeight() };
        rlj.core.SetShaderValue(shader, rlj.core.GetShaderLocation(shader, "size"), screenSize, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(shader, freqXLoc, freqX, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shader, freqYLoc, freqY, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shader, ampXLoc, ampX, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shader, ampYLoc, ampY, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shader, speedXLoc, speedX, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shader, speedYLoc, speedY, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);

        float seconds = 0.0f;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        // -------------------------------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            seconds += rlj.core.GetFrameTime();

            rlj.core.SetShaderValue(shader, secondsLoc, new float[]{seconds}, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_FLOAT);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginShaderMode(shader);

            rlj.textures.DrawTexture(texture, 0, 0, Color.WHITE);
            rlj.textures.DrawTexture(texture, texture.width, 0, Color.WHITE);

            rlj.core.EndShaderMode();

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);         // Unload shader
        rlj.textures.UnloadTexture(texture);       // Unload texture
        //--------------------------------------------------------------------------------------
    }

}
