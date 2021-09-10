package com.raylib.examples.shapes;

import com.raylib.Raylib;
import com.raylib.core.Color;
import com.raylib.core.Core;
import com.raylib.raymath.Vector2;
import com.raylib.textures.Textures;

public class DrawCircleSector{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Draw Circle Sector
     *
     *   This example has been created using raylib-j (Version 0.1)
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
        //-------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - draw circle sector");

        Vector2 center = new Vector2((Core.GetScreenWidth() - 300)/2, Core.GetScreenHeight()/2);

        float outerRadius = 180.0f;
        int startAngle = 0;
        int endAngle = 180;
        int segments = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()){   // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            // NOTE: All variables update happens inside GUI control functions
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawLine(500, 0, 500, Core.GetScreenHeight(), Textures.Fade(Color.LIGHTGRAY, 0.6f));
            rlj.shapes.DrawRectangle(500, 0, Core.GetScreenWidth() - 500, Core.GetScreenHeight(),
                    Textures.Fade(Color.LIGHTGRAY, 0.3f));

            rlj.shapes.DrawCircleSector(center, outerRadius, startAngle, endAngle, segments, Textures.Fade(Color.MAROON,
                    0.3f));
            rlj.shapes.DrawCircleSectorLines(center, outerRadius, startAngle, endAngle, segments,
                    Textures.Fade(Color.MAROON, 0.6f));

            // Draw GUI controls
            //TODO: RGUI
            /*------------------------------------------------------------------------------
            startAngle = GuiSliderBar((Rectangle){ 600, 40, 120, 20}, "StartAngle", NULL, startAngle, 0, 720);
            endAngle = GuiSliderBar((Rectangle){ 600, 70, 120, 20}, "EndAngle", NULL, endAngle, 0, 720);

            outerRadius = GuiSliderBar((Rectangle){ 600, 140, 120, 20}, "Radius", NULL, outerRadius, 0, 200);
            segments = GuiSliderBar((Rectangle){ 600, 170, 120, 20}, "Segments", NULL, segments, 0, 100);
            */

            rlj.text.DrawText("MODE: " + ((segments >= 4)? "MANUAL" : "AUTO"), 600, 200, 10,
                    (segments >= 4)? Color.MAROON : Color.DARKGRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
