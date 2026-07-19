package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.FilePathList;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.rlgl.RLGL.rlTextureFilterMode.*;
import static com.raylib.java.structs.Color.*;

public class FontFilters{

    /*******************************************************************************************
     *
     *   raylib [text] example - Font filters
     *
     *   After font loading, font texture atlas filter could be configured for a softer
     *   display of the font when scaling it to different sizes, that way, it's not required
     *   to generate multiple fonts at multiple sizes (as long as the scaling is not very different)
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
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - font filters");

        String msg = "Loaded Font";

        // NOTE: Textures/Fonts MUST be loaded after Window initialization (OpenGL context is required)

        // TTF Font loading with custom generation parameters
        Font font = rlj.text.LoadFontEx("src/tests/resources/textures/KAISG.ttf", 96, null, 0);

        // Generate mipmap levels to use trilinear filtering
        // NOTE: On 2D drawing it won't be noticeable, it looks like FILTER_BILINEAR
        rlj.textures.GenTextureMipmaps(font.texture);

        float fontSize = (float)font.baseSize;
        Vector2 fontPosition = new Vector2(40.0f, screenHeight/2.0f - 80.0f);
        Vector2 textSize = new Vector2(0.0f, 0.0f);

        // Setup texture scaling filter
        rlj.textures.SetTextureFilter(font.texture, TEXTURE_FILTER_POINT);
        int currentFontFilter = 0;      // TEXTURE_FILTER_POINT

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {    // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            fontSize += rlj.core.GetMouseWheelMove() * 4.0f;

            // Choose font texture filter method
            if (rlj.core.IsKeyPressed(KEY_ONE)) {
                rlj.textures.SetTextureFilter(font.texture, TEXTURE_FILTER_POINT);
                currentFontFilter = 0;
            }
            else if (rlj.core.IsKeyPressed(KEY_TWO)) {
                rlj.textures.SetTextureFilter(font.texture, TEXTURE_FILTER_BILINEAR);
                currentFontFilter = 1;
            }
            else if (rlj.core.IsKeyPressed(KEY_THREE)) {
                // NOTE: Trilinear filter won't be noticed on 2D drawing
                rlj.textures.SetTextureFilter(font.texture, TEXTURE_FILTER_TRILINEAR);
                currentFontFilter = 2;
            }

            textSize = rlj.text.MeasureTextEx(font, msg, fontSize, 0);

            if (rlj.core.IsKeyDown(KEY_LEFT)) {
                fontPosition.x -= 10;
            }
            else if (rlj.core.IsKeyDown(KEY_RIGHT)) {
                fontPosition.x += 10;
            }

            // Load a dropped TTF file dynamically (at current fontSize)
            if (rlj.core.IsFileDropped()) {
                FilePathList droppedFiles = rlj.core.LoadDroppedFiles();

                // NOTE: We only support first ttf file dropped
                if (rlj.core.IsFileExtension(droppedFiles.paths[0], ".ttf")) {
                    rlj.text.UnloadFont(font);
                    font = rlj.text.LoadFontEx(droppedFiles.paths[0], (int)fontSize, null, 0);
                    rlj.core.UnloadDroppedFiles(droppedFiles);
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.text.DrawText("Use mouse wheel to change font size", 20, 20, 10, GRAY);
            rlj.text.DrawText("Use KEY_RIGHT and KEY_LEFT to move text", 20, 40, 10, GRAY);
            rlj.text.DrawText("Use 1, 2, 3 to change texture filter", 20, 60, 10, GRAY);
            rlj.text.DrawText("Drop a new TTF font for dynamic loading", 20, 80, 10, DARKGRAY);

            rlj.text.DrawTextEx(font, msg, fontPosition, fontSize, 0, BLACK);

            rlj.shapes.DrawRectangleLines((int) fontPosition.x, (int) fontPosition.y, (int) textSize.x, (int) textSize.y, RED);

            rlj.shapes.DrawRectangle(0, screenHeight - 80, screenWidth, 80, LIGHTGRAY);
            rlj.text.DrawText("Font size: " + fontSize, 20, screenHeight - 50, 10, DARKGRAY);
            rlj.text.DrawText("Text size: [" + textSize.x + ", " + textSize.y + "]", 20, screenHeight - 30, 10, DARKGRAY);
            rlj.text.DrawText("CURRENT TEXTURE FILTER:", 250, 400, 20, GRAY);

            if (currentFontFilter == 0) {
                rlj.text.DrawText("POINT", 570, 400, 20, BLACK);
            }
            else if (currentFontFilter == 1) {
                rlj.text.DrawText("BILINEAR", 570, 400, 20, BLACK);
            }
            else if (currentFontFilter == 2) {
                rlj.text.DrawText("TRILINEAR", 570, 400, 20, BLACK);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadDroppedFiles();        // Clear internal buffers
        rlj.text.UnloadFont(font);           // Font unloading
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
