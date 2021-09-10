package com.raylib.examples.core;

import com.raylib.Raylib;
import com.raylib.core.Color;
import com.raylib.raymath.Vector2;

import static com.raylib.core.input.Mouse.MouseButton.*;

public class InputMouse{

    /*******************************************************************************************
     *
     *   raylib [core] example - Mouse input
     *
     *   This example has been created using raylib 1.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2014 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - mouse input");

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

            if (rlj.core.IsMouseButtonPressed(MOUSE_LEFT_BUTTON)) ballColor = Color.MAROON;
            else if (rlj.core.IsMouseButtonPressed(MOUSE_MIDDLE_BUTTON)) ballColor = Color.LIME;
            else if (rlj.core.IsMouseButtonPressed(MOUSE_RIGHT_BUTTON)) ballColor = Color.DARKBLUE;
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