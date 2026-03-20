package com.raylib.java.core;

import com.raylib.java.Raylib;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.structs.Color.*;

public class WindowShouldClose {


    /*******************************************************************************************
     *
     *   raylib-j [core] example - Window Test
     *
     *   This example has been created using raylib-j (Version 0.5.5)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        boolean exitWindowRequested = false;
        boolean exitWindow = false;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");
        rlj.core.SetExitKey(KEY_NULL);
        rlj.core.SetTargetFPS(60);

        while(!exitWindow){
            // Update
            //----------------------------------------------------------------------------------
            // Detect if X-button or KEY_ESCAPE have been pressed to close window
            if (rlj.core.WindowShouldClose() || rlj.core.IsKeyPressed(KEY_ESCAPE)) {
                exitWindowRequested = true;
            }

            if (exitWindowRequested) {
                // A request for close window has been issued, we can save data before closing
                // or just show a message asking for confirmation

                if (rlj.core.IsKeyPressed(KEY_Y)) {
                    exitWindow = true;
                }
                else if (rlj.core.IsKeyPressed(KEY_N)) {
                    exitWindowRequested = false;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            if (exitWindowRequested) {
                rlj.shapes.DrawRectangle(0, 100, SCREEN_WIDTH, 200, BLACK);
                rlj.text.DrawText("Are you sure you want to exit program? [Y/N]", 40, 180, 30, WHITE);
            }
            else {
                rlj.text.DrawText("Try to close the window to get confirmation message!", 120, 200, 20, LIGHTGRAY);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }

}
