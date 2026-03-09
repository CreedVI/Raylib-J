package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.raudioal.Sound;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.structs.Color.RAYWHITE;
import static com.raylib.java.structs.Color.WHITE;

public class SpriteExplosion {

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Window Test
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

    private static final int NUM_FRAMES_PER_LINE = 5;
    private static final int NUM_LINES = 5;

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");
        rlj.audio.InitAudioDevice();

        // Load explosion sound
        Sound fxBoom = rlj.audio.LoadSound("src/tests/resources/textures/boom.wav");

        // Load explosion texture
        Texture2D explosion = rlj.textures.LoadTexture("src/tests/resources/textures/explosion.png");

        // Init variables for animation
        float frameWidth = (float)(explosion.width/NUM_FRAMES_PER_LINE);   // Sprite one frame rectangle width
        float frameHeight = (float)(explosion.height/NUM_LINES);           // Sprite one frame rectangle height
        int currentFrame = 0;
        int currentLine = 0;

        Rectangle frameRec = new Rectangle(0, 0, frameWidth, frameHeight);
        Vector2 position = new Vector2();

        boolean active = false;
        int framesCounter = 0;

        rlj.core.SetTargetFPS(120);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------

            // Check for mouse button pressed and activate explosion (if not active)
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && !active) {
                position = rlj.core.GetMousePosition();
                active = true;

                position.x -= frameWidth/2.0f;
                position.y -= frameHeight/2.0f;

                rlj.audio.PlaySound(fxBoom);
            }

            // Compute explosion animation frames
            if (active) {
                framesCounter++;

                if (framesCounter > 2) {
                    currentFrame++;

                    if (currentFrame >= NUM_FRAMES_PER_LINE) {
                        currentFrame = 0;
                        currentLine++;

                        if (currentLine >= NUM_LINES) {
                            currentLine = 0;
                            active = false;
                        }
                    }

                    framesCounter = 0;
                }
            }

            frameRec.x = frameWidth*currentFrame;
            frameRec.y = frameHeight*currentLine;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            // Draw explosion required frame rectangle
            if (active) {
                rlj.textures.DrawTextureRec(explosion, frameRec, position, WHITE);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(explosion);   // Unload texture
        rlj.audio.UnloadSound(fxBoom);        // Unload sound

        rlj.audio.CloseAudioDevice();

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }
}
