package com.raylib.examples.core;

import com.raylib.Raylib;
import com.raylib.core.Color;

/*******************************************************************************************
 *
 *   raylib-j [core] example - Window Test
 *
 *   This example has been created using raylib-j (Version 0.1)
 *   Ported by CreedVI
 *   https://github.com/creedvi/raylib-j
 *
 *   raylib is licensed under an unmodified zlib/libpng license
 *   Original example written and copyright by Ramon Santamaria (@raysan5)
 *   https://github.com/raysan5
 *
 *
 ********************************************************************************************/

public class WindowTest{

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.RAYWHITE);
            rlj.text.DrawText("Congrats! You created your first window!", 190, 200, 20, Color.LIGHTGRAY);
            rlj.core.EndDrawing();
        }
    }
}