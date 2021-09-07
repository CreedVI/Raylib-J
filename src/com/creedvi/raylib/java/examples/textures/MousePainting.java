package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.RenderTexture;
import com.creedvi.raylib.java.rlj.textures.Textures;

import static com.creedvi.raylib.java.rlj.core.Color.*;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.*;
import static com.creedvi.raylib.java.rlj.core.input.Mouse.MouseButton.MOUSE_LEFT_BUTTON;
import static com.creedvi.raylib.java.rlj.core.input.Mouse.MouseButton.MOUSE_RIGHT_BUTTON;

public class MousePainting{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Mouse painting
     *
     *   This example has been created using raylib 2.5 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Chris Dill (@MysteriousSpace) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2019 Chris Dill (@MysteriousSpace) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

     final static int MAX_COLORS_COUNT = 23;          // Number of colors available

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - mouse painting");

        // Colours to choose from
        Color colors[] = new Color[]{
            RAYWHITE, YELLOW, GOLD, ORANGE, PINK, RED, MAROON, GREEN, LIME, DARKGREEN,
            SKYBLUE, BLUE, DARKBLUE, PURPLE, VIOLET, DARKPURPLE, BEIGE, BROWN, DARKBROWN,
            LIGHTGRAY, GRAY, DARKGRAY, BLACK
        };

        // Define colorsRecs data (for every rectangle)
        Rectangle colorsRecs[] = new Rectangle[MAX_COLORS_COUNT];

        for (int i = 0; i < MAX_COLORS_COUNT; i++)
        {
            colorsRecs[i] = new Rectangle();
            colorsRecs[i].x = 10 + 30.0f*i + 2*i;
            colorsRecs[i].y = 10;
            colorsRecs[i].width = 30;
            colorsRecs[i].height = 30;
        }

        int colorSelected = 0;
        int colorSelectedPrev = colorSelected;
        int colorMouseHover = 0;
        float brushSize = 20.0f;
        boolean mouseWasPressed = false;

        Rectangle btnSaveRec = new Rectangle(750, 10, 40, 30);
        boolean btnSaveMouseHover = false;
        boolean showSaveMessage = false;
        int saveMessageCounter = 0;

        // Create a RenderTexture2D to use as a canvas
        RenderTexture target = rlj.textures.LoadRenderTexture(screenWidth, screenHeight);

        // Clear render texture before entering the game loop
        rlj.core.BeginTextureMode(target);
        rlj.core.ClearBackground(colors[0]);
        rlj.core.EndTextureMode();

        rlj.core.SetTargetFPS(120);              // Set our game to run at 120 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            Vector2 mousePos = rlj.core.GetMousePosition();

            // Move between colors with keys
            if (rlj.core.IsKeyPressed(KEY_RIGHT)) colorSelected++;
            else if (rlj.core.IsKeyPressed(KEY_LEFT)) colorSelected--;

            if (colorSelected >= MAX_COLORS_COUNT) colorSelected = MAX_COLORS_COUNT - 1;
            else if (colorSelected < 0) colorSelected = 0;

            // Choose color with mouse
            for (int i = 0; i < MAX_COLORS_COUNT; i++)
            {
                if (rlj.shapes.CheckCollisionPointRec(mousePos, colorsRecs[i]))
                {
                    colorMouseHover = i;
                    break;
                }
                else colorMouseHover = -1;
            }

            if ((colorMouseHover >= 0) && rlj.core.IsMouseButtonPressed(MOUSE_LEFT_BUTTON))
            {
                colorSelected = colorMouseHover;
                colorSelectedPrev = colorSelected;
            }

            // Change brush size
            brushSize += rlj.core.GetMouseWheelMove()*5;
            if (brushSize < 2) brushSize = 2;
            if (brushSize > 50) brushSize = 50;

            if (rlj.core.IsKeyPressed(KEY_C))
            {
                // Clear render texture to clear color
                rlj.core.BeginTextureMode(target);
                rlj.core.ClearBackground(colors[0]);
                rlj.core.EndTextureMode();
            }

            if (rlj.core.IsMouseButtonDown(MOUSE_LEFT_BUTTON))
            {
                // Paint circle into render texture
                // NOTE: To avoid discontinuous circles, we could store
                // previous-next mouse points and just draw a line using brush size
                rlj.core.BeginTextureMode(target);
                if (mousePos.y > 50){
                    rlj.shapes.DrawCircle((int)mousePos.x, (int)mousePos.y, brushSize, colors[colorSelected]);
                }
                rlj.core.EndTextureMode();
            }

            if (rlj.core.IsMouseButtonDown(MOUSE_RIGHT_BUTTON))
            {
                if (!mouseWasPressed)
                {
                    colorSelectedPrev = colorSelected;
                    colorSelected = 0;
                }

                mouseWasPressed = true;

                // Erase circle from render texture
                rlj.core.BeginTextureMode(target);
                if (mousePos.y > 50){
                    rlj.shapes.DrawCircle((int)mousePos.x, (int)mousePos.y, brushSize, colors[0]);
                }
                rlj.core.EndTextureMode();
            }
            else if (rlj.core.IsMouseButtonReleased(MOUSE_RIGHT_BUTTON) && mouseWasPressed)
            {
                colorSelected = colorSelectedPrev;
                mouseWasPressed = false;
            }

            // Check mouse hover save button
            if (rlj.shapes.CheckCollisionPointRec(mousePos, btnSaveRec)) btnSaveMouseHover = true;
            else btnSaveMouseHover = false;

            // Image saving logic
            // NOTE: Saving painted texture to a default named image
            if ((btnSaveMouseHover && rlj.core.IsMouseButtonReleased(MOUSE_LEFT_BUTTON)) || rlj.core.IsKeyPressed(KEY_S))
            {
                Image image = Textures.GetTextureData(target.texture);
                rlj.textures.ImageFlipVertical(image);
                rlj.textures.ExportImage(image, "my_amazing_texture_painting.png");
                rlj.textures.UnloadImage(image);
                showSaveMessage = true;
            }

            if (showSaveMessage)
            {
                // On saving, show a full screen message for 2 seconds
                saveMessageCounter++;
                if (saveMessageCounter > 240)
                {
                    showSaveMessage = false;
                    saveMessageCounter = 0;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            // NOTE: Render texture must be y-flipped due to default OpenGL coordinates (left-bottom)
            rlj.textures.DrawTextureRec(target.texture, new Rectangle(0, 0, (float)target.texture.width,
                    (float)-target.texture.height), new Vector2(), WHITE);

            // Draw drawing circle for reference
            if (mousePos.y > 50)
            {
                if (rlj.core.IsMouseButtonDown(MOUSE_RIGHT_BUTTON)){
                    rlj.shapes.DrawCircleLines((int)mousePos.x, (int)mousePos.y, brushSize, GRAY);
                }
                else{
                    rlj.shapes.DrawCircle(rlj.core.GetMouseX(), rlj.core.GetMouseY(), brushSize, colors[colorSelected]);
                }
            }

            // Draw top panel
            rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), 50, RAYWHITE);
            rlj.shapes.DrawLine(0, 50, rlj.core.GetScreenWidth(), 50, LIGHTGRAY);

            // Draw color selection rectangles
            for (int i = 0; i < MAX_COLORS_COUNT; i++){
                rlj.shapes.DrawRectangleRec(colorsRecs[i], colors[i]);
            }
            rlj.shapes.DrawRectangleLines(10, 10, 30, 30, LIGHTGRAY);

            if (colorMouseHover >= 0){
                rlj.shapes.DrawRectangleRec(colorsRecs[colorMouseHover], Textures.Fade(WHITE, 0.6f));
            }

            rlj.shapes.DrawRectangleLinesEx(new Rectangle(colorsRecs[colorSelected].x - 2,
                    colorsRecs[colorSelected].y - 2, colorsRecs[colorSelected].width + 4,
                    colorsRecs[colorSelected].height + 4), 2, BLACK);

            // Draw save image button
            rlj.shapes.DrawRectangleLinesEx(btnSaveRec, 2, btnSaveMouseHover ? RED : BLACK);
            rlj.text.DrawText("SAVE!", 755, 20, 10, btnSaveMouseHover ? RED : BLACK);

            // Draw save image message
            if (showSaveMessage)
            {
                rlj.shapes.DrawRectangle(0, 0, rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight(),
                        Textures.Fade(RAYWHITE, 0.8f));
                rlj.shapes.DrawRectangle(0, 150, rlj.core.GetScreenWidth(), 80, BLACK);
                rlj.text.DrawText("IMAGE SAVED:  my_amazing_texture_painting.png", 150, 180, 20, RAYWHITE);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadRenderTexture(target);    // Unload render texture
        //--------------------------------------------------------------------------------------
    }

}
