package com.creedvi.raylib.java.examples.shapes;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;

public class BasicShapes{

    final static int SCREEN_WIDTH = 800;
    final static int SCREEN_HEIGHT = 450;

    public static void main(String[] args){
        Raylib rlj = new Raylib();

        rlj.core.initWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "raylib [shapes] example - basic shapes drawing");
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("some basic shapes available on raylib", 20, 20, 20, Color.DARKGRAY);

            rlj.shapes.DrawCircle(SCREEN_WIDTH/4, 120, 35, Color.DARKBLUE);

            rlj.shapes.DrawRectangle(SCREEN_WIDTH/4*2 - 60, 100, 120, 60, Color.RED);
            rlj.shapes.DrawRectangleLines(SCREEN_WIDTH/4*2 - 40, 320, 80, 60, Color.ORANGE);  // NOTE: Uses QUADS
            // internally,
            // not
            // lines
            rlj.shapes.DrawRectangleGradientH(SCREEN_WIDTH/4*2 - 90, 170, 180, 130, Color.MAROON, Color.GOLD);

            rlj.shapes.DrawTriangle(new Vector2(SCREEN_WIDTH/4*3, 80), new Vector2(SCREEN_WIDTH/4*3 - 60, 150), new Vector2(SCREEN_WIDTH/4*3 + 60, 150), Color.VIOLET);

            rlj.shapes.DrawPoly(new Vector2(SCREEN_WIDTH/4*3, 320), 6, 80, 0, Color.BROWN);

            rlj.shapes.DrawCircleGradient(SCREEN_WIDTH/4, 220, 60, Color.GREEN, Color.SKYBLUE);

            // NOTE: We draw all LINES based shapes together to optimize internal drawing,
            // this way, all LINES are rendered in a single draw pass
            rlj.shapes.DrawLine(18, 42, SCREEN_WIDTH - 18, 42, Color.BLACK);
            rlj.shapes.DrawCircleLines(SCREEN_WIDTH/4, 340, 80, Color.DARKBLUE);
            rlj.shapes.DrawTriangleLines(new Vector2(SCREEN_WIDTH/4*3, 160), new Vector2(SCREEN_WIDTH/4*3 - 20, 230), new Vector2(SCREEN_WIDTH/4*3 + 20, 230), Color.DARKBLUE);
            rlj.core.EndDrawing();
        }
    }
}