package com.creedvi.raylib.java.examples.text;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.*;

public class WritingAnimation{

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [text] example - text writing anim");

        String message = "This sample illustrates a text writing\nanimation effect! Check it out! ;)";

        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyDown(KEY_SPACE)) framesCounter += 8;
            else framesCounter++;

            if (rlj.core.IsKeyPressed(KEY_ENTER)) framesCounter = 0;
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
    }
}
