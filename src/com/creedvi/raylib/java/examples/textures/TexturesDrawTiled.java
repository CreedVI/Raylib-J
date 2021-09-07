package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Config;
import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.core.Color.*;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.*;
import static com.creedvi.raylib.java.rlj.core.input.Mouse.MouseButton.MOUSE_LEFT_BUTTON;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.TextureFilterMode.TEXTURE_FILTER_TRILINEAR;

public class TexturesDrawTiled{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Draw part of the texture tiled
     *
     *   This example has been created using raylib 3.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2020 Vlad Adrian (@demizdor) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    final static int OPT_WIDTH = 220,       // Max width for the options container
        MARGIN_SIZE = 8,       // Size for the margins
        COLOR_SIZE = 16;       // Size of the color select buttons

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib();
        rlj.core.SetConfigFlags(Config.ConfigFlag.FLAG_WINDOW_RESIZABLE); // Make the window resizable
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [textures] example - Draw part of a texture tiled");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Texture2D texPattern = rlj.textures.LoadTexture("resources/patterns.png");
        rlj.textures.SetTextureFilter(texPattern, TEXTURE_FILTER_TRILINEAR); // Makes the texture smoother when upscaled

        // Coordinates for all patterns inside the texture
         Rectangle[] recPattern = {
            new Rectangle(3, 3, 66, 66 ),
            new Rectangle( 75, 3, 100, 100 ),
            new Rectangle( 3, 75, 66, 66 ),
            new Rectangle( 7, 156, 50, 50 ),
            new Rectangle( 85, 106, 90, 45 ),
            new Rectangle( 75, 154, 100, 60)
        };

        // Setup colors
        Color[] colors = { BLACK, MAROON, ORANGE, BLUE, PURPLE, BEIGE, LIME, RED, DARKGRAY, SKYBLUE };
        final int MAX_COLORS = colors.length;
        Rectangle[] colorRec = new Rectangle[MAX_COLORS];
        for (int i = 0; i < MAX_COLORS; i++){
            colorRec[i] = new Rectangle();
        }

        // Calculate rectangle for each color
        for (int i = 0, x = 0, y = 0; i < MAX_COLORS; i++)
        {
            colorRec[i].x = 2.0f + MARGIN_SIZE + x;
            colorRec[i].y = 22.0f + 256.0f + MARGIN_SIZE + y;
            colorRec[i].width = COLOR_SIZE*2.0f;
            colorRec[i].height = (float)COLOR_SIZE;

            if (i == (MAX_COLORS/2 - 1))
            {
                x = 0;
                y += COLOR_SIZE + MARGIN_SIZE;
            }
            else x += (COLOR_SIZE*2 + MARGIN_SIZE);
        }

        int activePattern = 0, activeCol = 0;
        float scale = 1.0f, rotation = 0.0f;

        rlj.core.SetTargetFPS(60);
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            screenWidth = rlj.core.GetScreenWidth();
            screenHeight = rlj.core.GetScreenHeight();

            // Handle mouse
            if (rlj.core.IsMouseButtonPressed(MOUSE_LEFT_BUTTON))
            {
                Vector2 mouse = rlj.core.GetMousePosition();

                // Check which pattern was clicked and set it as the active pattern
                for (int i = 0; i < recPattern.length; i++)
                {
                    if (rlj.shapes.CheckCollisionPointRec(mouse, new Rectangle( 2 + MARGIN_SIZE + recPattern[i].x,
                            40 + MARGIN_SIZE + recPattern[i].y, recPattern[i].width, recPattern[i].height)))
                    {
                        activePattern = i;
                        break;
                    }
                }

                // Check to see which color was clicked and set it as the active color
                for (int i = 0; i < MAX_COLORS; ++i)
                {
                    if (rlj.shapes.CheckCollisionPointRec(mouse, colorRec[i]))
                    {
                        activeCol = i;
                        break;
                    }
                }
            }

            // Handle keys

            // Change scale
            if (rlj.core.IsKeyPressed(KEY_UP)) scale += 0.25f;
            if (rlj.core.IsKeyPressed(KEY_DOWN)) scale -= 0.25f;
            if (scale > 10.0f) scale = 10.0f;
            else if ( scale <= 0.0f) scale = 0.25f;

            // Change rotation
            if (rlj.core.IsKeyPressed(KEY_LEFT)) rotation -= 25.0f;
            if (rlj.core.IsKeyPressed(KEY_RIGHT)) rotation += 25.0f;

            // Reset
            if (rlj.core.IsKeyPressed(KEY_SPACE)) { rotation = 0.0f; scale = 1.0f; }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(RAYWHITE);

            // Draw the tiled area
            rlj.textures.DrawTextureTiled(texPattern, recPattern[activePattern], new Rectangle((float)OPT_WIDTH+MARGIN_SIZE,
                    (float)MARGIN_SIZE, screenWidth - OPT_WIDTH - 2.0f*MARGIN_SIZE, screenHeight - 2.0f*MARGIN_SIZE),
                    new Vector2(0.0f, 0.0f), rotation, scale, colors[activeCol]);

            // Draw options
            rlj.shapes.DrawRectangle(MARGIN_SIZE, MARGIN_SIZE, OPT_WIDTH - MARGIN_SIZE, screenHeight - 2*MARGIN_SIZE,
                    rlj.textures.ColorAlpha(LIGHTGRAY, 0.5f));

            rlj.text.DrawText("Select Pattern", 2 + MARGIN_SIZE, 30 + MARGIN_SIZE, 10, BLACK);
            rlj.textures.DrawTexture(texPattern, 2 + MARGIN_SIZE, 40 + MARGIN_SIZE, BLACK);
            rlj.shapes.DrawRectangle(2 + MARGIN_SIZE + (int)recPattern[activePattern].x,
                    40 + MARGIN_SIZE + (int)recPattern[activePattern].y, (int)recPattern[activePattern].width, (int)recPattern[activePattern].height,
                    rlj.textures.ColorAlpha(DARKBLUE, 0.3f));

            rlj.text.DrawText("Select Color", 2+MARGIN_SIZE, 10+256+MARGIN_SIZE, 10, BLACK);
            for (int i = 0; i < MAX_COLORS; i++)
            {
                rlj.shapes.DrawRectangleRec(colorRec[i], colors[i]);
                if (activeCol == i) rlj.shapes.DrawRectangleLinesEx(colorRec[i], 3, rlj.textures.ColorAlpha(WHITE,
                        0.5f));
            }

            rlj.text.DrawText("Scale (UP/DOWN to change)", 2 + MARGIN_SIZE, 80 + 256 + MARGIN_SIZE, 10, BLACK);
            rlj.text.DrawText(scale + "x", 2 + MARGIN_SIZE, 92 + 256 + MARGIN_SIZE, 20, BLACK);

            rlj.text.DrawText("Rotation (LEFT/RIGHT to change)", 2 + MARGIN_SIZE, 122 + 256 + MARGIN_SIZE, 10, BLACK);
            rlj.text.DrawText(rotation + " degrees", 2 + MARGIN_SIZE, 134 + 256 + MARGIN_SIZE, 20,
                    BLACK);

            rlj.text.DrawText("Press [SPACE] to reset", 2 + MARGIN_SIZE, 164 + 256 + MARGIN_SIZE, 10, DARKBLUE);

            // Draw FPS
            rlj.text.DrawText(rlj.core.GetFPS() + " FPS", 2 + MARGIN_SIZE, 2 + MARGIN_SIZE, 20, BLACK);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texPattern);        // Unload texture
        //--------------------------------------------------------------------------------------

    }


}
