package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.core.input.Mouse.MouseButton.MOUSE_LEFT_BUTTON;

public class Bunnymark{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Bunnymark
     *
     *   This example has been created using raylib 1.6 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2014-2019 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/


     final static int MAX_BUNNIES = 50000;    // 50K bunnies limit

    // This is the maximum amount of elements (quads) per batch
    // NOTE: This value is defined in [rlgl] module and can be changed there
    final static int MAX_BATCH_ELEMENTS = 8192;

    static class Bunny {
        Vector2 position;
        Vector2 speed;
        Color color;

        public Bunny(){
            position = new Vector2();
            speed = new Vector2();
            color = new Color();
        }
    }

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - bunnymark");

        // Load bunny texture
        Texture2D texBunny = rlj.textures.LoadTexture("resources/wabbit_alpha.png");

        Bunny[] bunnies = new Bunny[MAX_BUNNIES];    // Bunnies array
        for (int i = 0; i < bunnies.length; i++){
            bunnies[i] = new Bunny(); //Init bunnies
        }

        int bunniesCount = 0;           // Bunnies counter

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsMouseButtonDown(MOUSE_LEFT_BUTTON))
            {
                // Create more bunnies
                for (int i = 0; i < 100; i++)
                {
                    if (bunniesCount < MAX_BUNNIES)
                    {
                        bunnies[bunniesCount].position = Core.GetMousePosition();
                        bunnies[bunniesCount].speed.x = (float) Core.GetRandomValue(-250, 250)/60.0f;
                        bunnies[bunniesCount].speed.y = (float) Core.GetRandomValue(-250, 250)/60.0f;
                        bunnies[bunniesCount].color = new Color(Core.GetRandomValue(50, 240),
                                Core.GetRandomValue(80, 240), Core.GetRandomValue(100, 240), 255);
                        bunniesCount++;
                    }
                }
            }

            // Update bunnies
            for (int i = 0; i < bunniesCount; i++)
            {
                bunnies[i].position.x += bunnies[i].speed.x;
                bunnies[i].position.y += bunnies[i].speed.y;

                if (((bunnies[i].position.x + texBunny.width/2) > rlj.core.GetScreenWidth()) ||
                        ((bunnies[i].position.x + texBunny.width/2) < 0)) bunnies[i].speed.x *= -1;
                if (((bunnies[i].position.y + texBunny.height/2) > rlj.core.GetScreenHeight()) ||
                        ((bunnies[i].position.y + texBunny.height/2 - 40) < 0)) bunnies[i].speed.y *= -1;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            for (int i = 0; i < bunniesCount; i++)
            {
                // NOTE: When internal batch buffer limit is reached (MAX_BATCH_ELEMENTS),
                // a draw call is launched and buffer starts being filled again;
                // before issuing a draw call, updated vertex data from internal CPU buffer is send to GPU...
                // Process of sending data is costly and it could happen that GPU data has not been completely
                // processed for drawing while new data is tried to be sent (updating current in-use buffers)
                // it could generates a stall and consequently a frame drop, limiting the number of drawn bunnies
                rlj.textures.DrawTexture(texBunny, (int)bunnies[i].position.x, (int)bunnies[i].position.y,
                        bunnies[i].color);
            }

            rlj.shapes.DrawRectangle(0, 0, screenWidth, 40, Color.BLACK);
            rlj.text.DrawText("bunnies: " + bunniesCount, 120, 10, 20, Color.GREEN);
            rlj.text.DrawText("batched draw calls: " + (1 + bunniesCount/MAX_BATCH_ELEMENTS), 320, 10, 20,
                    Color.MAROON);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texBunny);    // Unload bunny texture
        //--------------------------------------------------------------------------------------
    }
}
