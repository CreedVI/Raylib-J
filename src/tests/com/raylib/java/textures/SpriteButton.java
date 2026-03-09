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

public class SpriteButton {

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

    private static final int NUM_FRAMES = 3;

    public static void main(String[] args) {
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- basic window");

        rlj.audio.InitAudioDevice();      // Initialize audio device

        Sound fxButton = rlj.audio.LoadSound("src/tests/resources/textures/buttonfx.wav");   // Load button sound
        Texture2D button = rlj.textures.LoadTexture("src/tests/resources/textures/button.png"); // Load button texture

        // Define frame rectangle for drawing
        float frameHeight = (float)button.height/NUM_FRAMES;
        Rectangle sourceRec = new Rectangle(0, 0, (float)button.width, frameHeight);

        // Define button bounds on screen
        Rectangle btnBounds = new Rectangle(SCREEN_WIDTH/2.0f - button.width/2.0f, SCREEN_HEIGHT/2.0f - button.height/NUM_FRAMES/2.0f, (float)button.width, frameHeight);

        int btnState = 0;               // Button state: 0-NORMAL, 1-MOUSE_HOVER, 2-PRESSED
        boolean btnAction = false;         // Button action should be activated

        Vector2 mousePoint = new Vector2();

        rlj.core.SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            mousePoint = rlj.core.GetMousePosition();
            btnAction = false;

            // Check button state
            if (rlj.shapes.CheckCollisionPointRec(mousePoint, btnBounds)) {
                if (rlj.core.IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
                    btnState = 2;
                }
                else {
                    btnState = 1;
                }

                if (rlj.core.IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) {
                    btnAction = true;
                }
            }
            else {
                btnState = 0;
            }

            if (btnAction) {
                rlj.audio.PlaySound(fxButton);

                // TODO: Any desired action
            }

            // Calculate button frame rectangle to draw depending on button state
            sourceRec.y = btnState*frameHeight;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.textures.DrawTextureRec(button, sourceRec, new Vector2(btnBounds.x, btnBounds.y), WHITE); // Draw button frame

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(button);  // Unload button texture
        rlj.audio.UnloadSound(fxButton);  // Unload sound

        rlj.audio.CloseAudioDevice();     // Close audio device

        rlj.core.CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
