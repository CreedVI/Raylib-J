package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Mouse.MouseButton.*;

public class InputMouse{

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Mouse input
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - mouse input");

        Vector2 ballPosition;
        Color ballColor = Color.DARKBLUE;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            ballPosition = rlj.core.GetMousePosition();

            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) ballColor = Color.MAROON;
            else if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_MIDDLE)) ballColor = Color.LIME;
            else if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) ballColor = Color.DARKBLUE;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawCircleV(ballPosition, 40, ballColor);

            rlj.text.DrawText("move ball with mouse and click mouse button to change color", 10, 10, 20,
                    Color.DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}