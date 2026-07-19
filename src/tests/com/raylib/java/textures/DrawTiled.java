package com.raylib.java.textures;

import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;

public class DrawTiled{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Draw part of the texture tiled
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2020 Vlad Adrian (@demizdor)
     *
     ********************************************************************************************/

    static Raylib rlj;

    final static int OPT_WIDTH = 220,       // Max width for the options container
        MARGIN_SIZE = 8,       // Size for the margins
        COLOR_SIZE = 16;       // Size of the color select buttons

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib();
        rlj.core.SetConfigFlags(Config.ConfigFlag.FLAG_WINDOW_RESIZABLE); // Make the window resizable
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib-j [textures] example - Draw part of a texture tiled");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Texture2D texPattern = rlj.textures.LoadTexture("src/tests/resources/textures/patterns.png");
        rlj.textures.SetTextureFilter(texPattern, RLGL.rlTextureFilterMode.TEXTURE_FILTER_TRILINEAR); // Makes the texture smoother when upscaled

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
        Color[] colors = { Color.BLACK, Color.MAROON, Color.ORANGE, Color.BLUE, Color.PURPLE, Color.BEIGE, Color.LIME, Color.RED, Color.DARKGRAY, Color.SKYBLUE };
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
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT))
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
            rlj.core.ClearBackground(Color.RAYWHITE);

            // Draw the tiled area
            DrawTextureTiled(texPattern, recPattern[activePattern], new Rectangle((float)OPT_WIDTH+MARGIN_SIZE,
                    (float)MARGIN_SIZE, screenWidth - OPT_WIDTH - 2.0f*MARGIN_SIZE, screenHeight - 2.0f*MARGIN_SIZE),
                    new Vector2(0.0f, 0.0f), rotation, scale, colors[activeCol]);

            // Draw options
            rlj.shapes.DrawRectangle(MARGIN_SIZE, MARGIN_SIZE, OPT_WIDTH - MARGIN_SIZE, screenHeight - 2*MARGIN_SIZE,
                    rlj.textures.ColorAlpha(Color.LIGHTGRAY, 0.5f));

            rlj.text.DrawText("Select Pattern", 2 + MARGIN_SIZE, 30 + MARGIN_SIZE, 10, Color.BLACK);
            rlj.textures.DrawTexture(texPattern, 2 + MARGIN_SIZE, 40 + MARGIN_SIZE, Color.BLACK);
            rlj.shapes.DrawRectangle(2 + MARGIN_SIZE + (int)recPattern[activePattern].x,
                    40 + MARGIN_SIZE + (int)recPattern[activePattern].y, (int)recPattern[activePattern].width, (int)recPattern[activePattern].height,
                    rlj.textures.ColorAlpha(Color.DARKBLUE, 0.3f));

            rlj.text.DrawText("Select Color", 2+MARGIN_SIZE, 10+256+MARGIN_SIZE, 10, Color.BLACK);
            for (int i = 0; i < MAX_COLORS; i++)
            {
                rlj.shapes.DrawRectangleRec(colorRec[i], colors[i]);
                if (activeCol == i) {
                    rlj.shapes.DrawRectangleLinesEx(colorRec[i], 3, rlj.textures.ColorAlpha(Color.WHITE, 0.5f));
                }
            }

            rlj.text.DrawText("Scale (UP/DOWN to change)", 2 + MARGIN_SIZE, 80 + 256 + MARGIN_SIZE, 10, Color.BLACK);
            rlj.text.DrawText(scale + "x", 2 + MARGIN_SIZE, 92 + 256 + MARGIN_SIZE, 20, Color.BLACK);

            rlj.text.DrawText("Rotation (LEFT/RIGHT to change)", 2 + MARGIN_SIZE, 122 + 256 + MARGIN_SIZE, 10, Color.BLACK);
            rlj.text.DrawText(rotation + " degrees", 2 + MARGIN_SIZE, 134 + 256 + MARGIN_SIZE, 20, Color.BLACK);

            rlj.text.DrawText("Press [SPACE] to reset", 2 + MARGIN_SIZE, 164 + 256 + MARGIN_SIZE, 10, Color.DARKBLUE);

            // Draw FPS
            rlj.text.DrawText(rlj.core.GetFPS() + " FPS", 2 + MARGIN_SIZE, 2 + MARGIN_SIZE, 20, Color.BLACK);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texPattern);        // Unload texture
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------

    }

    // Draw part of a texture (defined by a rectangle) with rotation and scale tiled into dest.
    // NOTE: For tilling a whole texture DrawTextureQuad() is better
    public static void DrawTextureTiled(Texture2D texture, Rectangle source, Rectangle dest, Vector2 origin, float rotation, float scale, Color tint) {
        if ((texture.id <= 0) || (scale <= 0.0f)) return;  // Wanna see a infinite loop?!...just delete this line!

        int tileWidth = (int) (source.width * scale), tileHeight = (int) (source.height * scale);
        if ((dest.width < tileWidth) && (dest.height < tileHeight)) {
            // Can fit only one tile
            rlj.textures.DrawTexturePro(texture,
                           new Rectangle(source.x, source.y, (dest.width / tileWidth) * source.width, (dest.height / tileHeight) * source.height),
                           new Rectangle(dest.x, dest.y, dest.width, dest.height), origin, rotation, tint);
        }
        else if (dest.width <= tileWidth) {
            // Tiled vertically (one column)
            int dy = 0;
            for (; dy + tileHeight < dest.height; dy += tileHeight) {
                rlj.textures.DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, (dest.width / tileWidth) * source.width, source.height),
                               new Rectangle(dest.x, dest.y + dy, dest.width, (float) tileHeight), origin, rotation, tint);
            }

            // Fit last tile
            if (dy < dest.height) {
                rlj.textures.DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, (dest.width / tileWidth) * source.width, ((dest.height - dy) / tileHeight) * source.height),
                               new Rectangle(dest.x, dest.y + dy, dest.width, dest.height - dy), origin, rotation, tint);
            }
        }
        else if (dest.height <= tileHeight) {
            // Tiled horizontally (one row)
            int dx = 0;
            for (; dx + tileWidth < dest.width; dx += tileWidth) {
                rlj.textures.DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, source.width, (dest.height / tileHeight) * source.height),
                               new Rectangle(dest.x + dx, dest.y, (float) tileWidth, dest.height), origin, rotation, tint);
            }

            // Fit last tile
            if (dx < dest.width) {
                rlj.textures.DrawTexturePro(texture,
                               new Rectangle(source.x, source.y, ((dest.width - dx) / tileWidth) * source.width,
                                             (dest.height / tileHeight) * source.height),
                               new Rectangle(dest.x + dx, dest.y, dest.width - dx, dest.height), origin, rotation, tint);
            }
        }
        else{
            // Tiled both horizontally and vertically (rows and columns)
            int dx = 0;
            for (; dx + tileWidth < dest.width; dx += tileWidth) {
                int dy = 0;
                for (; dy + tileHeight < dest.height; dy += tileHeight) {
                    rlj.textures.DrawTexturePro(texture, source,
                                   new Rectangle(dest.x + dx, dest.y + dy, (float) tileWidth, (float) tileHeight),
                                   origin, rotation, tint);
                }

                if (dy < dest.height) {
                    rlj.textures.DrawTexturePro(texture,
                                   new Rectangle(source.x, source.y, source.width, ((dest.height - dy) / tileHeight) * source.height),
                                   new Rectangle(dest.x + dx, dest.y + dy, (float) tileWidth, dest.height - dy),
                                   origin, rotation, tint);
                }
            }

            // Fit last column of tiles
            if (dx < dest.width) {
                int dy = 0;
                for (; dy + tileHeight < dest.height; dy += tileHeight) {
                    rlj.textures.DrawTexturePro(texture,
                                   new Rectangle(source.x, source.y, ((dest.width - dx) / tileWidth) * source.width, source.height),
                                   new Rectangle(dest.x + dx, dest.y + dy, dest.width - dx, (float) tileHeight),
                                   origin, rotation, tint);
                }

                // Draw final tile in the bottom right corner
                if (dy < dest.height) {
                    rlj.textures.DrawTexturePro(texture,
                                   new Rectangle(source.x, source.y, ((dest.width - dx) / tileWidth) * source.width,
                                                 ((dest.height - dy) / tileHeight) * source.height),
                                   new Rectangle(dest.x + dx, dest.y + dy, dest.width - dx, dest.height - dy),
                                   origin, rotation, tint);
                }
            }
        }
    }


}
