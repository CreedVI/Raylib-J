package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.input.Mouse;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.RenderTexture;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.*;

public class JuliaSet{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - julia sets
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
     *   Copyright (c) 2019 eggmund (@eggmund)
     *
     ********************************************************************************************/

    // A few good julia sets
    final static float[][] pointsOfInterest = {
            {-0.348827f, 0.607167f},
            {-0.786268f, 0.169728f},
            {-0.8f, 0.156f},
            {0.285f, 0.0f},
            {-0.835f, -0.2321f},
            {-0.70176f, -0.3842f}
    };

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        //SetConfigFlags(FLAG_WINDOW_HIGHDPI);
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - julia sets");

        // Load julia set shader
        // NOTE: Defining 0 (NULL) for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, "resources/shaders/glsl330/julia_set.fs");

        // Create a RenderTexture2D to be used for render to texture
        RenderTexture target = rlj.textures.LoadRenderTexture(rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight());

        // c constant to use in z^2 + c
        float[] c = {pointsOfInterest[0][0], pointsOfInterest[0][1]};

        // Offset and zoom to draw the julia set at. (centered on screen and default size)
        float[] offset = {-(float) rlj.core.GetScreenWidth() / 2, -(float) rlj.core.GetScreenHeight() / 2};

        float zoom = 1.0f;

        Vector2 offsetSpeed = new Vector2(0.0f, 0.0f);

        // Get variable (uniform) locations on the shader to connect with the program
        // NOTE: If uniform variable could not be found in the shader, function returns -1
        int cLoc = rlj.core.GetShaderLocation(shader, "c");
        int zoomLoc = rlj.core.GetShaderLocation(shader, "zoom");
        int offsetLoc = rlj.core.GetShaderLocation(shader, "offset");

        // Tell the shader what the screen dimensions, zoom, offset and c are
        float[] screenDims = {(float) rlj.core.GetScreenWidth(), (float) rlj.core.GetScreenHeight()};
        rlj.core.SetShaderValue(shader, rlj.core.GetShaderLocation(shader, "screenDims"), screenDims, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);

        rlj.core.SetShaderValue(shader, cLoc, c, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(shader, zoomLoc, new float[]{zoom}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);
        rlj.core.SetShaderValue(shader, offsetLoc, offset, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);

        int incrementSpeed = 0;         // Multiplier of speed to change c value
        boolean showControls = true;       // Show controls
        boolean pause = false;             // Pause animation

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Press [1 - 6] to reset c to a point of interest
            if (rlj.core.IsKeyPressed(KEY_ONE) ||
                    rlj.core.IsKeyPressed(KEY_TWO) ||
                    rlj.core.IsKeyPressed(KEY_THREE) ||
                    rlj.core.IsKeyPressed(KEY_FOUR) ||
                    rlj.core.IsKeyPressed(KEY_FIVE) ||
                    rlj.core.IsKeyPressed(KEY_SIX)){
                if (rlj.core.IsKeyPressed(KEY_ONE)){
                    c[0] = pointsOfInterest[0][0];
                    c[1] = pointsOfInterest[0][1];
                }
                else if (rlj.core.IsKeyPressed(KEY_TWO)){
                    c[0] = pointsOfInterest[1][0];
                    c[1] = pointsOfInterest[1][1];
                }
                else if (rlj.core.IsKeyPressed(KEY_THREE)){
                    c[0] = pointsOfInterest[2][0];
                    c[1] = pointsOfInterest[2][1];
                }
                else if (rlj.core.IsKeyPressed(KEY_FOUR)){
                    c[0] = pointsOfInterest[3][0];
                    c[1] = pointsOfInterest[3][1];
                }
                else if (rlj.core.IsKeyPressed(KEY_FIVE)){
                    c[0] = pointsOfInterest[4][0];
                    c[1] = pointsOfInterest[4][1];
                }
                else if (rlj.core.IsKeyPressed(KEY_SIX)){
                    c[0] = pointsOfInterest[5][0];
                    c[1] = pointsOfInterest[5][1];
                }

                rlj.core.SetShaderValue(shader, cLoc, c, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);
            }

            if (rlj.core.IsKeyPressed(KEY_SPACE)) pause = !pause;                 // Pause animation (c change)
            if (rlj.core.IsKeyPressed(KEY_F1)) showControls = !showControls;  // Toggle whether or not to show controls

            if (!pause){
                if (rlj.core.IsKeyPressed(KEY_RIGHT)){
                    incrementSpeed++;
                }
                else if (rlj.core.IsKeyPressed(KEY_LEFT)) incrementSpeed--;

                // TODO.txt: The idea is to zoom and move around with mouse
                // Probably offset movement should be proportional to zoom level
                if (rlj.core.IsMouseButtonDown(Mouse.MouseButton.MOUSE_BUTTON_LEFT) || rlj.core.IsMouseButtonDown(Mouse.MouseButton.MOUSE_BUTTON_RIGHT)){
                    if (rlj.core.IsMouseButtonDown(Mouse.MouseButton.MOUSE_BUTTON_LEFT)) zoom += zoom * 0.003f;
                    if (rlj.core.IsMouseButtonDown(Mouse.MouseButton.MOUSE_BUTTON_RIGHT)) zoom -= zoom * 0.003f;

                    Vector2 mousePos = rlj.core.GetMousePosition();

                    offsetSpeed.x = mousePos.x - (float) screenWidth / 2;
                    offsetSpeed.y = mousePos.y - (float) screenHeight / 2;

                    // Slowly move camera to targetOffset
                    offset[0] += rlj.core.GetFrameTime() * offsetSpeed.x * 0.8f;
                    offset[1] += rlj.core.GetFrameTime() * offsetSpeed.y * 0.8f;
                }
                else{
                    offsetSpeed = new Vector2(0.0f, 0.0f);
                }

                rlj.core.SetShaderValue(shader, zoomLoc, new float[]{zoom}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);
                rlj.core.SetShaderValue(shader, offsetLoc, offset, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);

                // Increment c value with time
                float amount = rlj.core.GetFrameTime() * incrementSpeed * 0.0005f;
                c[0] += amount;
                c[1] += amount;

                rlj.core.SetShaderValue(shader, cLoc, c, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.BLACK);         // Clear the screen of the previous frame.

            // Using a render texture to draw Julia set
            rlj.core.BeginTextureMode(target);       // Enable drawing to texture
            rlj.core.ClearBackground(Color.BLACK);     // Clear the render texture

            // Draw a rectangle in shader mode to be used as shader canvas
            // NOTE: Rectangle uses font white character texture coordinates,
            // so shader can not be applied here directly because input vertexTexCoord
            // do not represent full screen coordinates (space where want to apply shader)
            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight(), Color.BLACK);
            rlj.core.EndTextureMode();

            // Draw the saved texture and rendered julia set with shader
            // NOTE: We do not invert texture on Y, already considered inside shader
            rlj.core.BeginShaderMode(shader);
            // WARNING: If FLAG_WINDOW_HIGHDPI is enabled, HighDPI monitor scaling should be considered
            // when rendering the RenderTexture2D to fit in the HighDPI scaled Window
            rlj.textures.DrawTextureEx(target.texture, new Vector2(0.0f, 0.0f), 0.0f, 1.0f, Color.WHITE);
            rlj.core.EndShaderMode();

            if (showControls){
                rlj.text.DrawText("Press Mouse buttons right/left to zoom in/out and move", 10, 15, 10, Color.RAYWHITE);
                rlj.text.DrawText("Press KEY_F1 to toggle these controls", 10, 30, 10, Color.RAYWHITE);
                rlj.text.DrawText("Press KEYS [1 - 6] to change point of interest", 10, 45, 10, Color.RAYWHITE);
                rlj.text.DrawText("Press KEY_LEFT | KEY_RIGHT to change speed", 10, 60, 10, Color.RAYWHITE);
                rlj.text.DrawText("Press KEY_SPACE to pause movement animation", 10, 75, 10, Color.RAYWHITE);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);           // Unload shader
        rlj.textures.UnloadRenderTexture(target);    // Unload render texture
        //--------------------------------------------------------------------------------------

    }
}
