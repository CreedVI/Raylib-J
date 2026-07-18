package com.raylib.java.shapes;

import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.structs.Color.*;

public class LinesBezier {

    /*******************************************************************************************
     *
     *   raylib [shapes] example - Cubic-bezier lines
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

    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Vector2 startPoint = new Vector2(30, 30);
        Vector2 endPoint = new Vector2((float)screenWidth - 30, (float)screenHeight - 30);
        boolean moveStartPoint = false;
        boolean moveEndPoint = false;
        
        Raylib rlj = new Raylib();
        rlj.core.SetConfigFlags(Config.ConfigFlag.FLAG_MSAA_4X_HINT);
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shapes] example - cubic-bezier lines");

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            Vector2 mouse = rlj.core.GetMousePosition();

            if (rlj.shapes.CheckCollisionPointCircle(mouse, startPoint, 10.0f) && rlj.core.IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
                moveStartPoint = true;
            }
            else if (rlj.shapes.CheckCollisionPointCircle(mouse, endPoint, 10.0f) && rlj.core.IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
                moveEndPoint = true;
            }

            if (moveStartPoint) {
                startPoint = mouse;
                if (rlj.core.IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) moveStartPoint = false;
            }

            if (moveEndPoint) {
                endPoint = mouse;
                if (rlj.core.IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) moveEndPoint = false;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.text.DrawText("MOVE START-END POINTS WITH MOUSE", 15, 20, 20, GRAY);

            // Draw line Cubic Bezier, in-out interpolation (easing), no control points
            rlj.shapes.DrawLineBezier(startPoint, endPoint, 4.0f, BLUE);

            // Draw start-end spline circles with some details
            rlj.shapes.DrawCircleV(startPoint, rlj.shapes.CheckCollisionPointCircle(mouse, startPoint, 10.0f)? 14.0f : 8.0f, moveStartPoint? RED : BLUE);
            rlj.shapes.DrawCircleV(endPoint, rlj.shapes.CheckCollisionPointCircle(mouse, endPoint, 10.0f)? 14.0f : 8.0f, moveEndPoint? RED : BLUE);


            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }
}