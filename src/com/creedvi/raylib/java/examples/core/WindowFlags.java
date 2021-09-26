package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;

import static com.creedvi.raylib.java.rlj.Config.ConfigFlag.*;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.*;

public class WindowFlags{

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Window Flags
     *
     *   This example has been created using raylib-j (Version 0.2)
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
        int screenWidth = 800;
        int screenHeight = 450;

        // Possible window flags
        /*
        FLAG_VSYNC_HINT
        FLAG_FULLSCREEN_MODE    -> not working properly -> wrong scaling!
        FLAG_WINDOW_RESIZABLE
        FLAG_WINDOW_UNDECORATED
        FLAG_WINDOW_TRANSPARENT
        FLAG_WINDOW_HIDDEN
        FLAG_WINDOW_MINIMIZED   -> Not supported on window creation
        FLAG_WINDOW_MAXIMIZED   -> Not supported on window creation
        FLAG_WINDOW_UNFOCUSED
        FLAG_WINDOW_TOPMOST
        FLAG_WINDOW_HIGHDPI     -> errors after minimize-resize, fb size is recalculated
        FLAG_WINDOW_ALWAYS_RUN
        FLAG_MSAA_4X_HINT
        */

        // Set configuration flags for window creation
        //SetConfigFlags(FLAG_VSYNC_HINT | FLAG_MSAA_4X_HINT | FLAG_WINDOW_HIGHDPI);
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - window flags");

        Vector2 ballPosition = new Vector2(rlj.core.GetScreenWidth() / 2.0f, rlj.core.GetScreenHeight() / 2.0f);
        Vector2 ballSpeed = new Vector2(5.0f, 4.0f);
        float ballRadius = 20;

        int framesCounter = 0;

        //SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //----------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //-----------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_F)) rlj.core.ToggleFullscreen();  // modifies window size when scaling!

            if (rlj.core.IsKeyPressed(KEY_R)){
                if (rlj.core.IsWindowState(FLAG_WINDOW_RESIZABLE)){
                    rlj.core.ClearWindowState(FLAG_WINDOW_RESIZABLE);
                }
                else{
                    rlj.core.SetWindowState(FLAG_WINDOW_RESIZABLE);
                }
            }

            if (rlj.core.IsKeyPressed(KEY_D)){
                if (rlj.core.IsWindowState(FLAG_WINDOW_UNDECORATED)){
                    rlj.core.ClearWindowState(FLAG_WINDOW_UNDECORATED);
                }
                else{
                    rlj.core.SetWindowState(FLAG_WINDOW_UNDECORATED);
                }
            }

            if (rlj.core.IsKeyPressed(KEY_H)){
                if (!rlj.core.IsWindowState(FLAG_WINDOW_HIDDEN)) rlj.core.SetWindowState(FLAG_WINDOW_HIDDEN);

                framesCounter = 0;
            }

            if (rlj.core.IsWindowState(FLAG_WINDOW_HIDDEN)){
                framesCounter++;
                if (framesCounter >= 240) rlj.core.ClearWindowState(FLAG_WINDOW_HIDDEN); // Show window after 3 seconds
            }

            if (rlj.core.IsKeyPressed(KEY_N)){
                if (!rlj.core.IsWindowState(FLAG_WINDOW_MINIMIZED)) rlj.core.MinimizeWindow();

                framesCounter = 0;
            }

            if (rlj.core.IsWindowState(FLAG_WINDOW_MINIMIZED)){
                framesCounter++;
                if (framesCounter >= 240) rlj.core.RestoreWindow(); // Restore window after 3 seconds
            }

            if (rlj.core.IsKeyPressed(KEY_M)){
                // NOTE: Requires FLAG_WINDOW_RESIZABLE enabled!
                if (rlj.core.IsWindowState(FLAG_WINDOW_MAXIMIZED)){
                    rlj.core.RestoreWindow();
                }
                else{
                    rlj.core.MaximizeWindow();
                }
            }

            if (rlj.core.IsKeyPressed(KEY_U)){
                if (rlj.core.IsWindowState(FLAG_WINDOW_UNFOCUSED)){
                    rlj.core.ClearWindowState(FLAG_WINDOW_UNFOCUSED);
                }
                else{
                    rlj.core.SetWindowState(FLAG_WINDOW_UNFOCUSED);
                }
            }

            if (rlj.core.IsKeyPressed(KEY_T)){
                if (rlj.core.IsWindowState(FLAG_WINDOW_TOPMOST)){
                    rlj.core.ClearWindowState(FLAG_WINDOW_TOPMOST);
                }
                else{
                    rlj.core.SetWindowState(FLAG_WINDOW_TOPMOST);
                }
            }

            if (rlj.core.IsKeyPressed(KEY_A)){
                if (rlj.core.IsWindowState(FLAG_WINDOW_ALWAYS_RUN)){
                    rlj.core.ClearWindowState(FLAG_WINDOW_ALWAYS_RUN);
                }
                else{
                    rlj.core.SetWindowState(FLAG_WINDOW_ALWAYS_RUN);
                }
            }

            if (rlj.core.IsKeyPressed(KEY_V)){
                if (rlj.core.IsWindowState(FLAG_VSYNC_HINT)){
                    rlj.core.ClearWindowState(FLAG_VSYNC_HINT);
                }
                else{
                    rlj.core.SetWindowState(FLAG_VSYNC_HINT);
                }
            }

            // Bouncing ball logic
            ballPosition.x += ballSpeed.x;
            ballPosition.y += ballSpeed.y;
            if ((ballPosition.x >= (rlj.core.GetScreenWidth() - ballRadius)) || (ballPosition.x <= ballRadius)){
                ballSpeed.x *= -1.0f;
            }
            if ((ballPosition.y >= (rlj.core.GetScreenHeight() - ballRadius)) || (ballPosition.y <= ballRadius)){
                ballSpeed.y *= -1.0f;
            }
            //-----------------------------------------------------

            // Draw
            //-----------------------------------------------------
            rlj.core.BeginDrawing();

            if (rlj.core.IsWindowState(FLAG_WINDOW_TRANSPARENT)){
                rlj.core.ClearBackground(Color.BLANK);
            }
            else{
                rlj.core.ClearBackground(Color.RAYWHITE);
            }

            rlj.shapes.DrawCircleV(ballPosition, ballRadius, Color.MAROON);
            rlj.shapes.DrawRectangleLinesEx(new Rectangle(0, 0, rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight()),
                    4, Color.RAYWHITE);

            rlj.shapes.DrawCircleV(rlj.core.GetMousePosition(), 10, Color.DARKBLUE);

            rlj.text.DrawFPS(10, 10);

            rlj.text.DrawText("Screen Size: " + rlj.core.GetScreenWidth() + ", " + rlj.core.GetScreenHeight(),10,
                    40, 10, Color.GREEN);

            // Draw window state info
            rlj.text.DrawText("Following flags can be set after window creation:", 10, 60, 10, Color.GRAY);
            if (rlj.core.IsWindowState(FLAG_FULLSCREEN_MODE)){
                rlj.text.DrawText("[F] FLAG_FULLSCREEN_MODE: on", 10, 80, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[F] FLAG_FULLSCREEN_MODE: off", 10, 80, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_RESIZABLE)){
                rlj.text.DrawText("[R] FLAG_WINDOW_RESIZABLE: on", 10, 100, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[R] FLAG_WINDOW_RESIZABLE: off", 10, 100, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_UNDECORATED)){
                rlj.text.DrawText("[D] FLAG_WINDOW_UNDECORATED: on", 10, 120, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[D] FLAG_WINDOW_UNDECORATED: off", 10, 120, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_HIDDEN)){
                rlj.text.DrawText("[H] FLAG_WINDOW_HIDDEN: on", 10, 140, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[H] FLAG_WINDOW_HIDDEN: off", 10, 140, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_MINIMIZED)){
                rlj.text.DrawText("[N] FLAG_WINDOW_MINIMIZED: on", 10, 160, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[N] FLAG_WINDOW_MINIMIZED: off", 10, 160, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_MAXIMIZED)){
                rlj.text.DrawText("[M] FLAG_WINDOW_MAXIMIZED: on", 10, 180, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[M] FLAG_WINDOW_MAXIMIZED: off", 10, 180, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_UNFOCUSED)){
                rlj.text.DrawText("[G] FLAG_WINDOW_UNFOCUSED: on", 10, 200, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[U] FLAG_WINDOW_UNFOCUSED: off", 10, 200, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_TOPMOST)){
                rlj.text.DrawText("[T] FLAG_WINDOW_TOPMOST: on", 10, 220, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[T] FLAG_WINDOW_TOPMOST: off", 10, 220, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_ALWAYS_RUN)){
                rlj.text.DrawText("[A] FLAG_WINDOW_ALWAYS_RUN: on", 10, 240, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[A] FLAG_WINDOW_ALWAYS_RUN: off", 10, 240, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_VSYNC_HINT)){
                rlj.text.DrawText("[V] FLAG_VSYNC_HINT: on", 10, 260, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("[V] FLAG_VSYNC_HINT: off", 10, 260, 10, Color.MAROON);
            }

            rlj.text.DrawText("Following flags can only be set before window creation:", 10, 300, 10, Color.GRAY);
            if (rlj.core.IsWindowState(FLAG_WINDOW_HIGHDPI)){
                rlj.text.DrawText("FLAG_WINDOW_HIGHDPI: on", 10, 320, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("FLAG_WINDOW_HIGHDPI: off", 10, 320, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_WINDOW_TRANSPARENT)){
                rlj.text.DrawText("FLAG_WINDOW_TRANSPARENT: on", 10, 340, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("FLAG_WINDOW_TRANSPARENT: off", 10, 340, 10, Color.MAROON);
            }
            if (rlj.core.IsWindowState(FLAG_MSAA_4X_HINT)){
                rlj.text.DrawText("FLAG_MSAA_4X_HINT: on", 10, 360, 10, Color.LIME);
            }
            else{
                rlj.text.DrawText("FLAG_MSAA_4X_HINT: off", 10, 360, 10, Color.MAROON);
            }

            rlj.core.EndDrawing();
            //-----------------------------------------------------
        }
    }
}
