package com.raylib.examples.shapes;

import com.raylib.Raylib;
import com.raylib.core.Color;
import com.raylib.raymath.Vector2;
import com.raylib.shapes.Rectangle;
import com.raylib.utils.Easings;

import static com.raylib.core.input.Keyboard.*;

public class EasingsRectArray{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Easings Rectangle Array
     *
     *   This example has been created using raylib-j (Version 0.1)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

     static final int RECS_WIDTH = 50;
     static final int RECS_HEIGHT = 50;
     static final int MAX_RECS_X = 800/RECS_WIDTH;
     static final int MAX_RECS_Y = 450/RECS_HEIGHT;
     static final int PLAY_TIME_IN_FRAMES = 240;                 // At 60 fps = 4 seconds

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shapes] example - easings rectangle array");

        Rectangle[] recs = new Rectangle[(int) (MAX_RECS_X * MAX_RECS_Y)];

        for (int y = 0; y < MAX_RECS_Y; y++)
        {
            for (int x = 0; x < MAX_RECS_X; x++)
            {
                recs[y*MAX_RECS_X + x] = new Rectangle();
                recs[y*MAX_RECS_X + x].x = RECS_WIDTH/2 + RECS_WIDTH*x;
                recs[y*MAX_RECS_X + x].y = RECS_HEIGHT/2 + RECS_HEIGHT*y;
                recs[y*MAX_RECS_X + x].width = RECS_WIDTH;
                recs[y*MAX_RECS_X + x].height = RECS_HEIGHT;
            }
        }

        float rotation = 0.0f;
        int framesCounter = 0;
        int state = 0;                  // Rectangles animation state: 0-Playing, 1-Finished

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (state == 0)
            {
                framesCounter++;

                for (int i = 0; i < MAX_RECS_X*MAX_RECS_Y; i++)
                {
                    recs[i].height = Easings.EaseCircOut(framesCounter, RECS_HEIGHT, -RECS_HEIGHT, PLAY_TIME_IN_FRAMES);
                    recs[i].width = Easings.EaseCircOut(framesCounter, RECS_WIDTH, -RECS_WIDTH, PLAY_TIME_IN_FRAMES);

                    if (recs[i].height < 0) recs[i].height = 0;
                    if (recs[i].width < 0) recs[i].width = 0;

                    if ((recs[i].height == 0) && (recs[i].width == 0)) state = 1;   // Finish playing

                    rotation = Easings.EaseLinearIn(framesCounter, 0.0f, 360.0f, PLAY_TIME_IN_FRAMES);
                }
            }
            else if ((state == 1) && rlj.core.IsKeyPressed(KEY_SPACE))
            {
                // When animation has finished, press space to restart
                framesCounter = 0;

                for (int i = 0; i < MAX_RECS_X*MAX_RECS_Y; i++)
                {
                    recs[i].height = RECS_HEIGHT;
                    recs[i].width = RECS_WIDTH;
                }

                state = 0;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (state == 0)
            {
                for (int i = 0; i < MAX_RECS_X*MAX_RECS_Y; i++)
                {
                    rlj.shapes.DrawRectanglePro(recs[i], new Vector2(recs[i].width/2, recs[i].height/2), rotation,
                    Color.RED);
                }
            }
            else if (state == 1) rlj.text.DrawText("PRESS [SPACE] TO PLAY AGAIN!", 240, 200, 20, Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
