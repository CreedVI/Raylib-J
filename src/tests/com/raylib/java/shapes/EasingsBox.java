package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.extras.Easings;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;

public class EasingsBox{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Easings Box
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
        final int screenWidth = 800;
        final int screenHeight = 450;
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - easings box anim");

        // Box variables to be animated with easings
        Rectangle rec = new Rectangle((float)rlj.core.GetScreenWidth()/2, -100, 100, 100);
        float rotation = 0.0f;
        float alpha = 1.0f;

        int state = 0;
        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            switch (state) {
                case 0 ->     // Move box down to center of screen
                {
                    framesCounter++;

                    // NOTE: Remember that 3rd parameter of easing function refers to
                    // desired value variation, do not confuse it with expected final value!
                    rec.y = Easings.EaseElasticOut(framesCounter, -100, (float) rlj.core.GetScreenHeight() / 2 + 100, 120);

                    if (framesCounter >= 120) {
                        framesCounter = 0;
                        state = 1;
                    }
                }
                case 1 ->     // Scale box to an horizontal bar
                {
                    framesCounter++;
                    rec.height = Easings.EaseBounceOut(framesCounter, 100, -90, 120);
                    rec.width = Easings.EaseBounceOut(framesCounter, 100, rlj.core.GetScreenWidth(), 120);

                    if (framesCounter >= 120) {
                        framesCounter = 0;
                        state = 2;
                    }
                }
                case 2 ->     // Rotate horizontal bar rectangle
                {
                    framesCounter++;
                    rotation = Easings.EaseQuadOut(framesCounter, 0.0f, 270.0f, 240);

                    if (framesCounter >= 240) {
                        framesCounter = 0;
                        state = 3;
                    }
                }
                case 3 ->     // Increase bar size to fill all screen
                {
                    framesCounter++;
                    rec.height = Easings.EaseCircOut(framesCounter, 10, rlj.core.GetScreenWidth(), 120);

                    if (framesCounter >= 120) {
                        framesCounter = 0;
                        state = 4;
                    }
                }
                case 4 ->     // Fade out animation
                {
                    framesCounter++;
                    alpha = Easings.EaseSineOut(framesCounter, 1.0f, -1.0f, 160);

                    if (framesCounter >= 160) {
                        framesCounter = 0;
                        state = 5;
                    }
                }
                default -> {
                }
            }

            // Reset animation at any moment
            if (rlj.core.IsKeyPressed(KEY_SPACE))
            {
                rec = new Rectangle((float)rlj.core.GetScreenWidth()/2, -100, 100, 100);
                rotation = 0.0f;
                alpha = 1.0f;
                state = 0;
                framesCounter = 0;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawRectanglePro(rec,
                                        new Vector2(rec.width/2, rec.height/2),
                                        rotation,
                                        rlj.textures.Fade(Color.BLACK, alpha));

            rlj.text.DrawText("PRESS [SPACE] TO RESET BOX ANIMATION!", 10, rlj.core.GetScreenHeight() - 25, 20,
                    Color.LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
