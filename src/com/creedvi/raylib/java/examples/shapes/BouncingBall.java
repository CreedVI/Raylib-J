package com.creedvi.raylib.java.examples.shapes;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KeyboardKey.KEY_SPACE;

public class BouncingBall{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Bouncing Ball
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

    public static void main(String[] args){
        // Initialization
        //---------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;
        Raylib rlj = new Raylib();

        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shapes] example - bouncing ball");

        Vector2 ballPosition = new Vector2((float) rlj.core.GetScreenWidth() / 2,
                (float) rlj.core.GetScreenHeight() / 2);
        Vector2 ballSpeed = new Vector2(5.0f, 4.0f);
        int ballRadius = 20;

        boolean pause = false;
        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //----------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()){    // Detect window close button or ESC key

            // Update
            //-----------------------------------------------------
            //TODO: FIX
            if (rlj.core.IsKeyPressed(KEY_SPACE)){
                pause = !pause;
            }

            if (!pause){
                ballPosition.x += ballSpeed.x;
                ballPosition.y += ballSpeed.y;

                // Check walls collision for bouncing
                if ((ballPosition.x >= (rlj.core.GetScreenWidth() - ballRadius)) || (ballPosition.x <= ballRadius)){
                    ballSpeed.x *= -1.0f;
                }
                if ((ballPosition.y >= (rlj.core.GetScreenHeight() - ballRadius)) || (ballPosition.y <= ballRadius)){
                    ballSpeed.y *= -1.0f;
                }
            }
            else{
                framesCounter++;
            }
            //-----------------------------------------------------

            // Draw
            //-----------------------------------------------------

            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawCircleV(ballPosition, ballRadius, Color.MAROON);
            rlj.text.DrawText("PRESS SPACE to PAUSE BALL MOVEMENT", 10, rlj.core.GetScreenHeight() - 25, 20,
                    Color.LIGHTGRAY);

            // On pause, we draw a blinking message
            if (pause && ((framesCounter / 30) % 2) == 0){
                rlj.text.DrawText("PAUSED", 350, 200, 30, Color.GRAY);
            }

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //-----------------------------------------------------
        }

    }

}
