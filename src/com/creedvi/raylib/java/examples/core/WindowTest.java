package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;

public class WindowTest{

    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 600;

    public static void main(String[] args) {
        Raylib rlj = new Raylib();
        rlj.core.initWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.DARKPURPLE);
            //rlj.text.DrawText("Congrats! You created your first window!", 190, 200, 20, Color.LIGHTGRAY);
            rlj.core.EndDrawing();
        }
        rlj.core.CloseWindow();
    }
}