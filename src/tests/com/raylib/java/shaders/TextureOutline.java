package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;

import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.*;
import static com.raylib.java.structs.Color.*;

public class TextureOutline {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Apply an shdrOutline to a texture
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   Example originally created with raylib 4.0, last time updated with raylib 4.0
     *
     *   Example contributed by Samuel Skiff (@GoldenThumbs) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2021-2023 Samuel SKiff (@GoldenThumbs) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static final int GLSL_VERSION = 330;
    // private static final int GLSL_VERSION = 110;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - Apply an outline to a texture");

        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/shaders/fudesumi.png");

        Shader shaderOutline = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/outline.fs", GLSL_VERSION));

        float outlineSize = 2.0f;
        float[] outlineColor = { 1.0f, 0.0f, 0.0f, 1.0f };     // Normalized RED color
        float[] textureSize = { (float)texture.width, (float)texture.height };

        // Get shader locations
        int outlineSizeLoc = rlj.core.GetShaderLocation(shaderOutline, "outlineSize");
        int outlineColorLoc = rlj.core.GetShaderLocation(shaderOutline, "outlineColor");
        int textureSizeLoc = rlj.core.GetShaderLocation(shaderOutline, "textureSize");

        // Set shader values (they can be changed later)
        rlj.core.SetShaderValue(shaderOutline, outlineSizeLoc, new float[] {outlineSize}, RL_SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shaderOutline, outlineColorLoc, outlineColor, RL_SHADER_UNIFORM_VEC4);
        rlj.core.SetShaderValue(shaderOutline, textureSizeLoc, textureSize, RL_SHADER_UNIFORM_VEC2);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            outlineSize += rlj.core.GetMouseWheelMove();
            if (outlineSize < 1.0f) {
                outlineSize = 1.0f;
            }

            rlj.core.SetShaderValue(shaderOutline, outlineSizeLoc, new float[]{outlineSize}, RL_SHADER_UNIFORM_FLOAT);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(RAYWHITE);
            rlj.core.BeginShaderMode(shaderOutline);

            rlj.textures.DrawTexture(texture, rlj.core.GetScreenWidth()/2 - texture.width/2, -30, WHITE);

            rlj.core.EndShaderMode();

            rlj.text.DrawText("Shader-based\ntexture\noutline", 10, 10, 20, GRAY);
            rlj.text.DrawText(rlj.text.TextFormat("Outline size: %d px", (int)outlineSize), 10, 120, 20, MAROON);
            rlj.text.DrawFPS(710, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);
        rlj.core.UnloadShader(shaderOutline);

        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
