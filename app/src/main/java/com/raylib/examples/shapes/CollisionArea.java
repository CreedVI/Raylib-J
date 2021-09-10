package com.creedvi.raylib.java.examples.shapes;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;

import static com.creedvi.raylib.java.rlj.core.Core.GetScreenHeight;
import static com.creedvi.raylib.java.rlj.core.Core.GetScreenWidth;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_SPACE;

public class CollisionArea{

    /*******************************************************************************************
     *
     *   raylib-j [shapes] example - Collision Area
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

    public static void main(String[] args){
        // Initialization
        //---------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;
        Raylib rlj = new Raylib(800, 450, "raylib [shapes] example - collision area");

        // Box A: Moving box
        Rectangle boxA = new Rectangle(10, GetScreenHeight()/2 - 50, 200, 100);
        int boxASpeedX = 4;

        // Box B: Mouse moved box
        Rectangle boxB = new Rectangle(GetScreenWidth()/2 - 30, GetScreenHeight()/2 - 30, 60, 60);

        Rectangle boxCollision = new Rectangle(); // Collision rectangle

        int screenUpperLimit = 40;      // Top menu limits

        boolean pause = false;             // Movement pause
        boolean collision = false;         // Collision detection

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //----------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //-----------------------------------------------------
            // Move box if not paused
            if (!pause) boxA.x += boxASpeedX;

            // Bounce box on x screen limits
            if (((boxA.x + boxA.width) >= GetScreenWidth()) || (boxA.x <= 0)) boxASpeedX *= -1;

            // Update player-controlled-box (box02)
            boxB.x = rlj.core.GetMouseX() - boxB.width/2;
            boxB.y = rlj.core.GetMouseY() - boxB.height/2;

            // Make sure Box B does not go out of move area limits
            if ((boxB.x + boxB.width) >= GetScreenWidth()) boxB.x = GetScreenWidth() - boxB.width;
            else if (boxB.x <= 0) boxB.x = 0;

            if ((boxB.y + boxB.height) >= GetScreenHeight()) boxB.y = GetScreenHeight() - boxB.height;
            else if (boxB.y <= screenUpperLimit) boxB.y = screenUpperLimit;

            // Check boxes collision
            collision = rlj.shapes.CheckCollisionRecs(boxA, boxB);

            // Get collision rectangle (only on collision)
            if (collision) boxCollision = rlj.shapes.GetCollisionRec(boxA, boxB);

            // Pause Box A movement
            if (rlj.core.IsKeyPressed(KEY_SPACE)) pause = !pause;
            //-----------------------------------------------------

            // Draw
            //-----------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.shapes.DrawRectangle(0, 0, screenWidth, screenUpperLimit, collision? Color.RED : Color.BLACK);

            rlj.shapes.DrawRectangleRec(boxA, Color.GOLD);
            rlj.shapes.DrawRectangleRec(boxB, Color.BLUE);

            if (collision)
            {
                // Draw collision area
                rlj.shapes.DrawRectangleRec(boxCollision, Color.LIME);

                // Draw collision message
                rlj.text.DrawText("COLLISION!", GetScreenWidth()/2 - rlj.text.MeasureText("COLLISION!", 20)/2,
                        screenUpperLimit/2 - 10, 20, Color.BLACK);

                // Draw collision area
                rlj.text.DrawText(("Collision Area: " + (int)boxCollision.width*(int)boxCollision.height),
                        GetScreenWidth()/2 - 100, screenUpperLimit + 10, 20, Color.BLACK);
            }

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //-----------------------------------------------------
        }
    }

}
