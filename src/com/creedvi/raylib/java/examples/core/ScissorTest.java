package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_S;

public class ScissorTest{

    /*******************************************************************************************
     *
     *   raylib-j [module] example - Example Name
     *
     *   This example has been created using raylib-j (Version 0.2)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example contributed by Chris Dill (@MysteriousSpace) and reviewed by Ramon Santamaria (@raysan5)
     *   Copyright (c) 2019 Chris Dill (@MysteriousSpace)
     *
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - scissor test");

        Rectangle scissorArea = new Rectangle(0, 0, 300, 300);
        boolean scissorMode = true;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_S)) scissorMode = !scissorMode;

            // Centre the scissor area around the mouse position
            scissorArea.x = rlj.core.GetMouseX() - scissorArea.width/2;
            scissorArea.y = rlj.core.GetMouseY() - scissorArea.height/2;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (scissorMode){
                rlj.core.BeginScissorMode((int)scissorArea.x, (int)scissorArea.y, (int)scissorArea.width, (int)scissorArea.height);
            }

            // Draw full screen rectangle and some text
            // NOTE: Only part defined by scissor area will be rendered
            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight(), Color.RED);
            rlj.text.DrawText("Move the mouse around to reveal this text!", 190, 200, 20, Color.LIGHTGRAY);

            if (scissorMode){
                rlj.core.EndScissorMode();
            }

            rlj.shapes.DrawRectangleLinesEx(scissorArea, 1, Color.BLACK);
            rlj.text.DrawText("Press S to toggle scissor test", 10, 10, 20, Color.BLACK);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
