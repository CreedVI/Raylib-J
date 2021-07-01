package com.creedvi.raylib.java.examples.shapes;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.textures.Textures;
import com.creedvi.raylib.java.rlj.utils.Easings;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KeyboardKey.KEY_ENTER;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KeyboardKey.KEY_R;

public class EasingsBall{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Easings Ball
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
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - easings ball anim");

        // Ball variable value to be animated with easings
        int ballPositionX = -100;
        int ballRadius = 20;
        float ballAlpha = 0.0f;

        int state = 0;
        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (state == 0)             // Move ball position X with easing
            {
                framesCounter++;
                ballPositionX = (int) Easings.EaseElasticOut(framesCounter, -100, screenWidth/2 + 100, 120);

                if (framesCounter >= 120)
                {
                    framesCounter = 0;
                    state = 1;
                }
            }
            else if (state == 1)        // Increase ball radius with easing
            {
                framesCounter++;
                ballRadius = (int) Easings.EaseElasticIn(framesCounter, 20, 500, 200);

                if (framesCounter >= 200)
                {
                    framesCounter = 0;
                    state = 2;
                }
            }
            else if (state == 2)        // Change ball alpha with easing (background color blending)
            {
                framesCounter++;
                ballAlpha = Easings.EaseCubicOut(framesCounter, 0.0f, 1.0f, 200);

                if (framesCounter >= 200)
                {
                    framesCounter = 0;
                    state = 3;
                }
            }
            else if (state == 3)        // Reset state to play again
            {
                if (rlj.core.IsKeyPressed(KEY_ENTER))
                {
                    // Reset required variables to play again
                    ballPositionX = -100;
                    ballRadius = 20;
                    ballAlpha = 0.0f;
                    state = 0;
                }
            }

            if (rlj.core.IsKeyPressed(KEY_R)) framesCounter = 0;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (state >= 2) rlj.shapes.DrawRectangle(0, 0, screenWidth, screenHeight, Color.GREEN);
            rlj.shapes.DrawCircle(ballPositionX, 200, ballRadius, Textures.Fade(Color.RED, 1.0f - ballAlpha));

            if (state == 3) rlj.text.DrawText("PRESS [ENTER] TO PLAY AGAIN!", 240, 200, 20, Color.BLACK);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
