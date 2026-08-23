package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Vector2;

public class FontBDF {

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
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- BDF fonts");
        rlj.core.SetTargetFPS(60);

        Font bdf = rlj.text.LoadFont("src/tests/resources/text/ic8x16u.bdf");

        while (!rlj.core.WindowShouldClose()) {
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.RAYWHITE);
            rlj.text.DrawTextEx(bdf, "Congrats! You created your first window!", new Vector2(190, 200), 60, 1, Color.LIGHTGRAY);
            rlj.core.EndDrawing();
        }

        rlj.core.CloseWindow();
    }


}
