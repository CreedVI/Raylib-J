package com.creedvi.raylib.java.examples.shapes;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.textures.Textures;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_R;

public class LogoAnim{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Raylib Logo Animation
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
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - raylib logo animation");

        int logoPositionX = screenWidth/2 - 128;
        int logoPositionY = screenHeight/2 - 128;

        int framesCounter = 0;
        int lettersCount = 0;

        int topSideRecWidth = 16;
        int leftSideRecHeight = 16;

        int bottomSideRecWidth = 16;
        int rightSideRecHeight = 16;

        int state = 0;                  // Tracking animation states (State Machine)
        float alpha = 1.0f;             // Useful for fading

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (state == 0)                 // State 0: Small box blinking
            {
                framesCounter++;

                if (framesCounter == 120)
                {
                    state = 1;
                    framesCounter = 0;      // Reset counter... will be used later...
                }
            }
            else if (state == 1)            // State 1: Top and left bars growing
            {
                topSideRecWidth += 4;
                leftSideRecHeight += 4;

                if (topSideRecWidth == 256) state = 2;
            }
            else if (state == 2)            // State 2: Bottom and right bars growing
            {
                bottomSideRecWidth += 4;
                rightSideRecHeight += 4;

                if (bottomSideRecWidth == 256) state = 3;
            }
            else if (state == 3)            // State 3: Letters appearing (one by one)
            {
                framesCounter++;

                if (framesCounter/12==1)       // Every 12 frames, one more letter!
                {
                    lettersCount++;
                    framesCounter = 0;
                }

                if (lettersCount >= 10)     // When all letters have appeared, just fade out everything
                {
                    alpha -= 0.02f;

                    if (alpha <= 0.0f)
                    {
                        alpha = 0.0f;
                        state = 4;
                    }
                }
            }
            else if (state == 4)            // State 4: Reset and Replay
            {
                if (rlj.core.IsKeyPressed(KEY_R))
                {
                    framesCounter = 0;
                    lettersCount = 0;

                    topSideRecWidth = 16;
                    leftSideRecHeight = 16;

                    bottomSideRecWidth = 16;
                    rightSideRecHeight = 16;

                    alpha = 1.0f;
                    state = 0;          // Return to State 0
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (state == 0)
            {
                if ((framesCounter/15)%2==0) rlj.shapes.DrawRectangle(logoPositionX, logoPositionY, 16, 16,
                        Color.BLACK);
            }
            else if (state == 1)
            {
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY, topSideRecWidth, 16, Color.BLACK);
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY, 16, leftSideRecHeight, Color.BLACK);
            }
            else if (state == 2)
            {
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY, topSideRecWidth, 16, Color.BLACK);
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY, 16, leftSideRecHeight, Color.BLACK);

                rlj.shapes.DrawRectangle(logoPositionX + 240, logoPositionY, 16, rightSideRecHeight, Color.BLACK);
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY + 240, bottomSideRecWidth, 16, Color.BLACK);
            }
            else if (state == 3)
            {
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY, topSideRecWidth, 16, Textures.Fade(Color.BLACK,
                        alpha));
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY + 16, 16, leftSideRecHeight - 32,
                        Textures.Fade(Color.BLACK, alpha));

                rlj.shapes.DrawRectangle(logoPositionX + 240, logoPositionY + 16, 16, rightSideRecHeight - 32,
                        Textures.Fade(Color.BLACK, alpha));
                rlj.shapes.DrawRectangle(logoPositionX, logoPositionY + 240, bottomSideRecWidth, 16,
                        Textures.Fade(Color.BLACK, alpha));

                rlj.shapes.DrawRectangle(screenWidth/2 - 112, screenHeight/2 - 112, 224, 224,
                        Textures.Fade(Color.RAYWHITE, alpha));

                rlj.text.DrawText(rlj.text.TextSubtext("raylib", 0, lettersCount), screenWidth/2 - 44,
                        screenHeight/2 + 48, 50, Textures.Fade(Color.BLACK, alpha));
            }
            else if (state == 4)
            {
                rlj.text.DrawText("[R] REPLAY", 340, 200, 20, Color.GRAY);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}