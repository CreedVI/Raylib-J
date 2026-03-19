package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;

import static com.raylib.java.core.input.Keyboard.KEY_BACKSPACE;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_DEFAULT;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_IBEAM;

public class TextInputBox{

    /*******************************************************************************************
     *
     *   raylib [text] example - Input box
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

    final static int MAX_INPUT_CHARS = 9;
    static Raylib rlj;

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - input box");

        StringBuilder name = new StringBuilder();
        int letterCount = 0;

        Rectangle textBox = new Rectangle(screenWidth / 2.0f - 100, 180, 225, 50);
        boolean mouseOnText;

        int framesCounter = 0;

        rlj.core.SetTargetFPS(10);               // Set our game to run at 10 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {    // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            mouseOnText = rlj.shapes.CheckCollisionPointRec(rlj.core.GetMousePosition(), textBox);

            if (mouseOnText) {
                // Set the window's cursor to the I-Beam
                rlj.core.SetMouseCursor(MOUSE_CURSOR_IBEAM);

                // Get char pressed (unicode character) on the queue
                int key = rlj.core.GetCharPressed();

                // Check if more characters have been pressed on the same frame
                while (key > 0) {
                    // NOTE: Only allow keys in range [32..125]
                    if ((key >= 32) && (key <= 125) && (letterCount < MAX_INPUT_CHARS)) {
                        name.append((char) key);
                        letterCount++;
                    }

                    key = rlj.core.GetCharPressed();  // Check next character in the queue
                }

                if (rlj.core.IsKeyPressed(KEY_BACKSPACE)) {
                    letterCount--;
                    if (letterCount < 0) {
                        letterCount = 0;
                    }
                    if (!name.toString().equals("")) {
                        name.deleteCharAt(letterCount);
                    }
                }
            }
            else {
                rlj.core.SetMouseCursor(MOUSE_CURSOR_DEFAULT);
            }

            if (mouseOnText) {
                framesCounter++;
            }
            else {
                framesCounter = 0;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawFPS(10, 10);

            rlj.text.DrawText("PLACE MOUSE OVER INPUT BOX!", 240, 140, 20, Color.GRAY);

            rlj.shapes.DrawRectangleRec(textBox, Color.LIGHTGRAY);
            if (mouseOnText) {
                rlj.shapes.DrawRectangleLines((int) textBox.x, (int) textBox.y, (int) textBox.width, (int) textBox.height, Color.RED);
            }
            else {
                rlj.shapes.DrawRectangleLines((int) textBox.x, (int) textBox.y, (int) textBox.width, (int) textBox.height, Color.DARKGRAY);
            }

            rlj.text.DrawText(name.toString(), (int) textBox.x + 5, (int) textBox.y + 8, 40, Color.MAROON);

            rlj.text.DrawText("INPUT CHARS: " + letterCount + "/" + MAX_INPUT_CHARS, 315, 250, 20,
                    Color.DARKGRAY);

            if (mouseOnText) {
                if (letterCount < MAX_INPUT_CHARS) {
                    // Draw blinking underscore char
                    if (((framesCounter / 20) % 2) == 0) {
                        rlj.text.DrawText("_",
                                (int) textBox.x + 8 + rlj.text.MeasureText(name.toString(), 40),
                                (int) textBox.y + 12, 40, Color.MAROON);
                    }
                }
                else {
                    rlj.text.DrawText("Press BACKSPACE to delete chars...", 230, 300, 20, Color.GRAY);
                }
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }

}
