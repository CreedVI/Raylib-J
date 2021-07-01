package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KeyboardKey.*;

/*******************************************************************************************
 *
 *   raylib-j [core] example - Keyboard input
 *
 *   This example has been created using raylib-j (Version 0.1)
 *   Ported by CreedVI
 *   https://github.com/creedvi/raylib-j
 *
 *   raylib is licensed under an unmodified zlib/libpng license
 *   Original example written and copyright by Ramon Santamaria (@raysan5)
 *   https://github.com/raysan5
 *
 ********************************************************************************************/

public class InputKeys{

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;
        Raylib rlj = new Raylib();

        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [core] example - keyboard input");

        Vector2 ballPosition = new Vector2((float) screenWidth / 2, (float) screenHeight / 2);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()){    // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyDown(KEY_RIGHT)){
                ballPosition.x += 2.0f;
            }
            if (rlj.core.IsKeyDown(KEY_LEFT)){
                ballPosition.x -= 2.0f;
            }
            if (rlj.core.IsKeyDown(KEY_UP)){
                ballPosition.y -= 2.0f;
            }
            if (rlj.core.IsKeyDown(KEY_DOWN)){
                ballPosition.y += 2.0f;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("move the ball with arrow keys", 10, 10, 20, Color.DARKGRAY);

            rlj.shapes.DrawCircleV(ballPosition, 50, Color.MAROON);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
