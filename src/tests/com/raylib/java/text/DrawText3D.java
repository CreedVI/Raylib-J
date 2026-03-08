package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;

public class DrawText3D {

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
