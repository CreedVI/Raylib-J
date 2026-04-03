package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;

import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;

public class MultiSample2D{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Multiple sample2D with default batch system
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
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib - multiple sample2D");

        Image imRed = rlj.textures.GenImageColor(800, 450, new Color(255, 0, 0, 255));
        Texture2D texRed = rlj.textures.LoadTextureFromImage(imRed);
        rlj.textures.UnloadImage(imRed);

        Image imBlue = rlj.textures.GenImageColor(800, 450, new Color(0, 0, 255, 255));
        Texture2D texBlue = rlj.textures.LoadTextureFromImage(imBlue);
        rlj.textures.UnloadImage(imBlue);

        Shader shader = rlj.core.LoadShader(null, "src/tests/resources/shaders/shaders/glsl330/color_mix.fs");

        // Get an additional sampler2D location to be enabled on drawing
        int texBlueLoc = rlj.core.GetShaderLocation(shader, "texture1");

        // Get shader uniform for divider
        int dividerLoc = rlj.core.GetShaderLocation(shader, "divider");
        float[] dividerValue = {0.5f};

        rlj.core.SetTargetFPS(60);                           // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())                // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyDown(KEY_RIGHT)) dividerValue[0] += 0.01f;
            else if (rlj.core.IsKeyDown(KEY_LEFT)) dividerValue[0] -= 0.01f;

            if (dividerValue[0] < 0.0f) dividerValue[0] = 0.0f;
            else if (dividerValue[0] > 1.0f) dividerValue[0] = 1.0f;

            rlj.core.SetShaderValue(shader, dividerLoc, dividerValue, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.WHITE);

            rlj.core.BeginShaderMode(shader);

            // WARNING: Additional samplers are enabled for all draw calls in the batch,
            // EndShaderMode() forces batch drawing and consequently resets active textures
            // to let other sampler2D to be activated on consequent drawings (if required)
            rlj.core.SetShaderValueTexture(shader, texBlueLoc, texBlue);

            // We are drawing texRed using default sampler2D texture0 but
            // an additional texture units is enabled for texBlue (sampler2D texture1)
            rlj.textures.DrawTexture(texRed, 0, 0, Color.WHITE);

            rlj.core.EndShaderMode();

            rlj.text.DrawText("Use KEY_LEFT/KEY_RIGHT to move texture mixing in shader!", 80,
                    rlj.core.GetScreenHeight() - 40, 20, Color.RAYWHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);       // Unload shader
        rlj.textures.UnloadTexture(texRed);      // Unload texture
        rlj.textures.UnloadTexture(texBlue);     // Unload texture
        //--------------------------------------------------------------------------------------

    }

}
