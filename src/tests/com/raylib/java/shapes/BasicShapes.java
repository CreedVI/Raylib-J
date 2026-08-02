package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.structs.Color.*;

public class BasicShapes{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Basic Shapes
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

    final static int SCREEN_WIDTH = 800;
    final static int SCREEN_HEIGHT = 450;

    public static void main(String[] args){
        Raylib rlj = new Raylib();
        // rlj.config.setSupportQuadsDrawMode(true);
        rlj.config.setSupportOpenGLDebug(true);

        float rotation = 0.0f;

        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "raylib [shapes] example - basic shapes drawing");
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            // Update
            //----------------------------------------------------------------------------------
            rotation += 0.2f;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.text.DrawText("some basic shapes available on raylib", 20, 20, 20, DARKGRAY);

            // Circle shapes and lines
            rlj.shapes.DrawCircle(SCREEN_WIDTH/5, 120, 35, DARKBLUE);
            rlj.shapes.DrawCircleGradient(new Vector2(SCREEN_WIDTH/5.0f, 220.0f ), 60, GREEN, SKYBLUE);
            rlj.shapes.DrawCircleLines(SCREEN_WIDTH/5, 340, 80, DARKBLUE);
            rlj.shapes.DrawEllipse(SCREEN_WIDTH/5, 120, 25, 20, YELLOW);
            rlj.shapes.DrawEllipseLines(SCREEN_WIDTH/5, 120, 30, 25, YELLOW);

            // Rectangle shapes and lines
            rlj.shapes.DrawRectangle(SCREEN_WIDTH/4*2 - 60, 100, 120, 60, RED);
            rlj.shapes.DrawRectangleGradientH(SCREEN_WIDTH/4*2 - 90, 170, 180, 130, MAROON, GOLD);
            rlj.shapes.DrawRectangleLines(SCREEN_WIDTH/4*2 - 40, 320, 80, 60, ORANGE);  // NOTE: Uses QUADS internally, not lines

            // Triangle shapes and lines
            rlj.shapes.DrawTriangle(
                    new Vector2(SCREEN_WIDTH/4.0f *3.0f, 80.0f ),
                    new Vector2(SCREEN_WIDTH/4.0f *3.0f - 60.0f, 150.0f ),
                    new Vector2(SCREEN_WIDTH/4.0f *3.0f + 60.0f, 150.0f ),
                    VIOLET
            );

            rlj.shapes.DrawTriangleLines(
                    new Vector2(SCREEN_WIDTH/4.0f*3.0f, 160.0f ),
                    new Vector2(SCREEN_WIDTH/4.0f*3.0f - 20.0f, 230.0f ),
                    new Vector2(SCREEN_WIDTH/4.0f*3.0f + 20.0f, 230.0f ),
                    DARKBLUE
            );

            // Polygon shapes and lines
            rlj.shapes.DrawPoly(new Vector2(SCREEN_WIDTH/4.0f*3, 330 ), 6, 80, rotation, BROWN);
            rlj.shapes.DrawPolyLines(new Vector2(SCREEN_WIDTH/4.0f*3, 330 ), 6, 90, rotation, BROWN);
            rlj.shapes.DrawPolyLinesEx(new Vector2(SCREEN_WIDTH/4.0f*3, 330 ), 6, 85, rotation, 6, BEIGE);

            // NOTE: We draw all LINES based shapes together to optimize internal drawing,
            // this way, all LINES are rendered in a single draw pass
            rlj.shapes.DrawLine(18, 42, SCREEN_WIDTH - 18, 42, BLACK);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }
}