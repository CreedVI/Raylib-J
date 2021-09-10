package com.creedvi.raylib.java.examples.shapes;

import com.creedvi.raylib.java.rlj.Config;
import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.core.input.Mouse.MouseButton;
import com.creedvi.raylib.java.rlj.raymath.Vector2;

public class LinesBezier{

    //TODO:Something's fucky wucky

    /*******************************************************************************************
     *
     *   raylib [shapes] example - Cubic-bezier lines
     *
     *   This example has been created using raylib 1.7 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2017 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - cubic-bezier lines");
        Core.SetConfigFlags(Config.ConfigFlag.FLAG_MSAA_4X_HINT);

        Vector2 start = new Vector2();
        Vector2 end = new Vector2(screenWidth, screenHeight);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsMouseButtonDown(MouseButton.MOUSE_LEFT_BUTTON)) start = rlj.core.GetMousePosition();
            else if (rlj.core.IsMouseButtonDown(MouseButton.MOUSE_RIGHT_BUTTON)) end = rlj.core.GetMousePosition();
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("USE MOUSE LEFT-RIGHT CLICK to DEFINE LINE START and END POINTS", 15, 20, 20, Color.GRAY);

            rlj.shapes.DrawLineBezier(start, end, 2.0f, Color.RED);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}