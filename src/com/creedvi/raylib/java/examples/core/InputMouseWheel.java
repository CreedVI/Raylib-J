package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;

public class InputMouseWheel{

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - input mouse wheel");

        int boxPositionY = screenHeight/2 - 40;
        int scrollSpeed = 4;            // Scrolling speed in pixels

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            boxPositionY -= (rlj.core.GetMouseWheelMove()*scrollSpeed);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawRectangle(screenWidth/2 - 40, boxPositionY, 80, 80, Color.MAROON);

            rlj.text.DrawText("Use mouse wheel to move the cube up and down!", 10, 10, 20, Color.GRAY);
            rlj.text.DrawText("Box position Y: " + boxPositionY, 10, 40, 20, Color.LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        //--------------------------------------------------------------------------------------

    }

}
