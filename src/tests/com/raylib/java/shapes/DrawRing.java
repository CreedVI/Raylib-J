package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Rectangle;

import static com.raylib.java.structs.Color.*;
import static com.raylib.java.structs.Color.DARKGRAY;
import static com.raylib.java.structs.Color.MAROON;

public class DrawRing {

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        float roundness = 0.2f;
        int width = 200;
        int height = 100;
        int segments = 0;
        int lineThick = 1;

        boolean drawRect = false;
        boolean drawRoundedRect = true;
        boolean drawRoundedLines = false;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            // Update
            //----------------------------------------------------------------------------------
            Rectangle rec = new Rectangle((rlj.core.GetScreenWidth() - width - 250)/2.0f, (rlj.core.GetScreenHeight() - height)/2.0f, (float)width, (float)height);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.shapes.DrawLine(560, 0, 560, rlj.core.GetScreenHeight(), rlj.textures.Fade(LIGHTGRAY, 0.6f));
            rlj.shapes.DrawRectangle(560, 0, rlj.core.GetScreenWidth() - 500, rlj.core.GetScreenHeight(), rlj.textures.Fade(LIGHTGRAY, 0.3f));

            if (drawRect) {
                rlj.shapes.DrawRectangleRec(rec, rlj.textures.Fade(GOLD, 0.6f));
            }
            if (drawRoundedRect) {
                rlj.shapes.DrawRectangleRounded(rec, roundness, segments, rlj.textures.Fade(MAROON, 0.2f));
            }
            if (drawRoundedLines) {
                rlj.shapes.DrawRectangleRoundedLinesEx(rec, roundness, segments, (float)lineThick, rlj.textures.Fade(MAROON, 0.4f));
            }

            // Draw GUI controls
            //------------------------------------------------------------------------------
            /* TODO: rGUI
            width = (int)GuiSliderBar((Rectangle){ 640, 40, 105, 20 }, "Width", NULL, (float)width, 0, (float)rlj.core.GetScreenWidth() - 300);
            height = (int)GuiSliderBar((Rectangle){ 640, 70, 105, 20 }, "Height", NULL, (float)height, 0, (float)rlj.core.GetScreenHeight() - 50);
            roundness = GuiSliderBar((Rectangle){ 640, 140, 105, 20 }, "Roundness", NULL, roundness, 0.0f, 1.0f);
            lineThick = (int)GuiSliderBar((Rectangle){ 640, 170, 105, 20 }, "Thickness", NULL, (float)lineThick, 0, 20);
            segments = (int)GuiSliderBar((Rectangle){ 640, 240, 105, 20}, "Segments", NULL, (float)segments, 0, 60);

            drawRoundedRect = GuiCheckBox((Rectangle){ 640, 320, 20, 20 }, "DrawRoundedRect", drawRoundedRect);
            drawRoundedLines = GuiCheckBox((Rectangle){ 640, 350, 20, 20 }, "DrawRoundedLines", drawRoundedLines);
            drawRect = GuiCheckBox((Rectangle){ 640, 380, 20, 20}, "DrawRect", drawRect);
             */
            //------------------------------------------------------------------------------

            rlj.text.DrawText(rlj.text.TextFormat("MODE: %s", (segments >= 4)? "MANUAL" : "AUTO"), 640, 280, 10, (segments >= 4)? MAROON : DARKGRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }

}
