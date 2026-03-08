package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector2;

public class FollowingEyes{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Following Eyes
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


    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - following eyes");

        Vector2 scleraLeftPosition = new Vector2(rlj.core.GetScreenWidth()/2.0f - 100, rlj.core.GetScreenHeight()/2.0f);
        Vector2 scleraRightPosition = new Vector2(rlj.core.GetScreenWidth()/2.0f + 100, rlj.core.GetScreenHeight()/2.0f);
        float scleraRadius = 80;

        Vector2 irisLeftPosition;
        Vector2 irisRightPosition;
        float irisRadius = 24;

        float angle;
        float dx, dy, dxx, dyy;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            irisLeftPosition = rlj.core.GetMousePosition();
            irisRightPosition = rlj.core.GetMousePosition();

            // Check not inside the left eye sclera
            if (!rlj.shapes.CheckCollisionPointCircle(irisLeftPosition, scleraLeftPosition, scleraRadius - 20))
            {
                dx = irisLeftPosition.x - scleraLeftPosition.x;
                dy = irisLeftPosition.y - scleraLeftPosition.y;

                angle = (float) Math.atan2(dy, dx);

                dxx = (float) ((scleraRadius - irisRadius)*Math.cos(angle));
                dyy = (float) ((scleraRadius - irisRadius)*Math.sin(angle));

                irisLeftPosition.x = scleraLeftPosition.x + dxx;
                irisLeftPosition.y = scleraLeftPosition.y + dyy;
            }

            // Check not inside the right eye sclera
            if (!rlj.shapes.CheckCollisionPointCircle(irisRightPosition, scleraRightPosition, scleraRadius - 20))
            {
                dx = irisRightPosition.x - scleraRightPosition.x;
                dy = irisRightPosition.y - scleraRightPosition.y;

                angle = (float) Math.atan2(dy, dx);

                dxx = (float) ((scleraRadius - irisRadius)*Math.cos(angle));
                dyy = (float) ((scleraRadius - irisRadius)*Math.sin(angle));

                irisRightPosition.x = scleraRightPosition.x + dxx;
                irisRightPosition.y = scleraRightPosition.y + dyy;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawCircleV(scleraLeftPosition, scleraRadius, Color.LIGHTGRAY);
            rlj.shapes.DrawCircleV(irisLeftPosition, irisRadius, Color.BROWN);
            rlj.shapes.DrawCircleV(irisLeftPosition, 10, Color.BLACK);

            rlj.shapes.DrawCircleV(scleraRightPosition, scleraRadius, Color.LIGHTGRAY);
            rlj.shapes.DrawCircleV(irisRightPosition, irisRadius, Color.DARKGREEN);
            rlj.shapes.DrawCircleV(irisRightPosition, 10, Color.BLACK);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}