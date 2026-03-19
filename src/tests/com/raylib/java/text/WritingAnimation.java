package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;

import static com.raylib.java.core.input.Keyboard.KEY_ENTER;
import static com.raylib.java.core.input.Keyboard.KEY_SPACE;

public class WritingAnimation {

    /*******************************************************************************************
     *
     *   raylib [text] example - Text writing animation
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

    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - text writing anim");

        String message = "This sample illustrates a text writing\nanimation effect! Check it out! ;)";

        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyDown(KEY_SPACE)) {
                framesCounter += 8;
            }
            else {
                framesCounter++;
            }

            if (rlj.core.IsKeyPressed(KEY_ENTER)) {
                framesCounter = 0;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText(rlj.text.TextSubtext(message, 0, framesCounter / 10), 210, 160,
                              20, Color.MAROON);

            rlj.text.DrawText("PRESS [ENTER] to RESTART!", 240, 260, 20, Color.LIGHTGRAY);
            rlj.text.DrawText("PRESS [SPACE] to SPEED UP!", 239, 300, 20, Color.LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }
}
