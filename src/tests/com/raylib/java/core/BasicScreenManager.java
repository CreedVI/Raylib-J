package com.raylib.java.core;

import com.raylib.java.Raylib;

import static com.raylib.java.core.BasicScreenManager.GameScreen.*;
import static com.raylib.java.core.input.Keyboard.KEY_ENTER;
import static com.raylib.java.structs.Color.*;

public class BasicScreenManager {

    enum GameScreen {
        LOGO, TITLE, GAMEPLAY, ENDING;
    }

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- Basic Screen Manager");

        GameScreen currentScreen = LOGO;

        // TODO: Initialize all required variables and load all required data here!

        int framesCounter = 0;          // Useful to count frames

        rlj.core.SetTargetFPS(60);               // Set desired framerate (frames-per-second)
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {   // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            switch(currentScreen) {
                case LOGO: 
                    // TODO: Update LOGO screen variables here!

                    framesCounter++;    // Count frames

                    // Wait for 2 seconds (120 frames) before jumping to TITLE screen
                    if (framesCounter > 120) {
                        currentScreen = TITLE;
                    }
                    break;
                case TITLE:
                    // TODO: Update TITLE screen variables here!

                    // Press enter to change to GAMEPLAY screen
                    if (rlj.core.IsKeyPressed(KEY_ENTER)) {
                        currentScreen = GAMEPLAY;
                    }
                    break;
                case GAMEPLAY:
                    // TODO: Update GAMEPLAY screen variables here!

                    // Press enter to change to ENDING screen
                    if (rlj.core.IsKeyPressed(KEY_ENTER)) {
                        currentScreen = ENDING;
                    }
                    break;
                case ENDING:
                    // TODO: Update ENDING screen variables here!

                    // Press enter to return to TITLE screen
                    if (rlj.core.IsKeyPressed(KEY_ENTER)) {
                        currentScreen = TITLE;
                    }
                    break;
                default: 
                    break;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            switch(currentScreen) {
                case LOGO:
                    // TODO: Draw LOGO screen here!
                    rlj.text.DrawText("LOGO SCREEN", 20, 20, 40, LIGHTGRAY);
                    rlj.text.DrawText("WAIT for 2 SECONDS...", 290, 220, 20, GRAY);

                    break;
                case TITLE:
                    // TODO: Draw TITLE screen here!
                    rlj.shapes.DrawRectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, GREEN);
                    rlj.text.DrawText("TITLE SCREEN", 20, 20, 40, DARKGREEN);
                    rlj.text.DrawText("PRESS ENTER or TAP to JUMP to GAMEPLAY SCREEN", 120, 220, 20, DARKGREEN);

                    break;
                case GAMEPLAY:
                    // TODO: Draw GAMEPLAY screen here!
                    rlj.shapes.DrawRectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, PURPLE);
                    rlj.text.DrawText("GAMEPLAY SCREEN", 20, 20, 40, MAROON);
                    rlj.text.DrawText("PRESS ENTER or TAP to JUMP to ENDING SCREEN", 130, 220, 20, MAROON);

                    break;
                case ENDING:
                    // TODO: Draw ENDING screen here!
                    rlj.shapes.DrawRectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, BLUE);
                    rlj.text.DrawText("ENDING SCREEN", 20, 20, 40, DARKBLUE);
                    rlj.text.DrawText("PRESS ENTER or TAP to RETURN to TITLE SCREEN", 120, 220, 20, DARKBLUE);

                    break;
                default:
                    break;
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------

        // TODO: Unload all loaded data (textures, fonts, audio) here!

        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
