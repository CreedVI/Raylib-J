package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.structs.Color.*;

public class SpriteAnimation {

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Sprite Animation
     *
     *   This example has been created using raylib-j (Version 0.5.5)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    private static final int MAX_FRAME_SPEED = 15;
    private static final int MIN_FRAME_SPEED = 1;

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Texture2D scarfy = rlj.textures.LoadTexture("src/tests/resources/textures/scarfy.png");        // Texture loading

        Vector2 position = new Vector2(350.0f, 280.0f);
        Rectangle frameRec = new Rectangle(0.0f, 0.0f, (float)scarfy.width/6, (float)scarfy.height);
        int currentFrame = 0;

        int framesCounter = 0;
        int framesSpeed = 8;            // Number of spritesheet frames shown by second

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            framesCounter++;

            if (framesCounter >= (60/framesSpeed)) {
                framesCounter = 0;
                currentFrame++;

                if (currentFrame > 5) {
                    currentFrame = 0;
                }

                frameRec.x = (float)currentFrame*(float)scarfy.width/6;
            }

            // Control frames speed
            if (rlj.core.IsKeyPressed(KEY_RIGHT)) framesSpeed++;
            else if (rlj.core.IsKeyPressed(KEY_LEFT)) framesSpeed--;

            if (framesSpeed > MAX_FRAME_SPEED) {
                framesSpeed = MAX_FRAME_SPEED;
            }
            else if (framesSpeed < MIN_FRAME_SPEED) {
                framesSpeed = MIN_FRAME_SPEED;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.textures.DrawTexture(scarfy, 15, 40, WHITE);
            rlj.shapes.DrawRectangleLines(15, 40, scarfy.width, scarfy.height, LIME);
            rlj.shapes.DrawRectangleLines(15 + (int)frameRec.x, 40 + (int)frameRec.y, (int)frameRec.width, (int)frameRec.height, RED);

            rlj.text.DrawText("FRAME SPEED: ", 165, 210, 10, DARKGRAY);
            rlj.text.DrawText(rlj.text.TextFormat("%02d FPS", framesSpeed), 575, 210, 10, DARKGRAY);
            rlj.text.DrawText("PRESS RIGHT/LEFT KEYS to CHANGE SPEED!", 290, 240, 10, DARKGRAY);

            for (int i = 0; i < MAX_FRAME_SPEED; i++) {
                if (i < framesSpeed) {
                    rlj.shapes.DrawRectangle(250 + 21*i, 205, 20, 20, RED);
                }
                rlj.shapes.DrawRectangleLines(250 + 21*i, 205, 20, 20, MAROON);
            }

            rlj.textures.DrawTextureRec(scarfy, frameRec, position, WHITE);  // Draw part of the texture

            rlj.text.DrawText("(c) Scarfy sprite by Eiden Marsal", SCREEN_WIDTH - 200, SCREEN_HEIGHT - 20, 10, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(scarfy);       // Texture unloading

        rlj.core.CloseWindow();                // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
