/*
package com.raylib.java.shapes;

import com.raylib.java.Raygui;
import com.raylib.java.Raylib;
import com.raylib.java.elements.CheckBox;
import com.raylib.java.elements.Slider;
import com.raylib.java.structs.Rectangle;

import static com.raylib.java.structs.Color.*;

public class DrawRectangleRounded {

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
        Raygui rgui = new Raygui(rlj);

        Slider widthSlider = new Slider(new Rectangle(640, 40, 105, 20), "Width", null, (float)width, 0, (float)rlj.core.GetScreenWidth() - 300);
        Slider heightSlider = new Slider(new Rectangle(640, 70, 105, 20), "Height", null, (float)height, 0, (float)rlj.core.GetScreenHeight() - 50);
        Slider roundnessSlider = new Slider(new Rectangle(640, 140, 105, 20), "Roundness", null, roundness, 0.0f, 1.0f);
        Slider thicknessSlider = new Slider(new Rectangle(640, 170, 105, 20), "Thickness", null, (float)lineThick, 0, 20);
        Slider segmentsSlider = new Slider(new Rectangle(640, 240, 105, 20), "Segments", null, (float)segments, 0, 60);

        CheckBox drawRectCheck = new CheckBox(new Rectangle(640, 380, 20, 20), "DrawRect", drawRect);
        CheckBox drawRoundedRectCheck = new CheckBox(new Rectangle(640, 320, 20, 20), "DrawRoundedRect", drawRoundedRect);
        CheckBox drawRoundedLinesCheck = new CheckBox(new Rectangle(640, 350, 20, 20), "DrawRoundedLines", drawRoundedLines);

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
                rlj.shapes.DrawRectangleRoundedLines(rec,roundness, segments, (float)lineThick, rlj.textures.Fade(MAROON, 0.4f));
            }

            // Draw GUI controls
            //------------------------------------------------------------------------------
            width = (int) rgui.GuiSliderBar(widthSlider);
            height = (int) rgui.GuiSliderBar(heightSlider);
            roundness = rgui.GuiSliderBar(roundnessSlider);
            lineThick = (int) rgui.GuiSliderBar(thicknessSlider);
            segments = (int) rgui.GuiSliderBar(segmentsSlider);

            drawRoundedRect = rgui.GuiCheckBox(drawRoundedRectCheck);
            drawRoundedLines = rgui.GuiCheckBox(drawRoundedLinesCheck);
            drawRect = rgui.GuiCheckBox(drawRectCheck);
            //------------------------------------------------------------------------------

            rlj.text.DrawText(rlj.text.TextFormat("MODE: %s", (segments >= 4)? "MANUAL" : "AUTO"), 640, 280, 10, (segments >= 4)? MAROON : DARKGRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }

}
*/