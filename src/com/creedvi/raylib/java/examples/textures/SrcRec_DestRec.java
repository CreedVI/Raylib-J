package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

public class SrcRec_DestRec{

    /*******************************************************************************************
     *
     *   raylib [textures] example - Texture source and destination rectangles
     *
     *   This example has been created using raylib 1.3 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2015 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] examples - texture source and destination " +
                "rectangles");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)

        Texture2D scarfy = rlj.textures.LoadTexture("resources/scarfy.png");        // Texture loading

        int frameWidth = scarfy.width/6;
        int frameHeight = scarfy.height;

        // Source rectangle (part of the texture to use for drawing)
        Rectangle sourceRec = new Rectangle(0.0f, 0.0f, (float)frameWidth, (float)frameHeight);

        // Destination rectangle (screen rectangle where drawing part of texture)
        Rectangle destRec = new Rectangle(screenWidth/2.0f, screenHeight/2.0f, frameWidth*2.0f, frameHeight*2.0f);

        // Origin of the texture (rotation/scale point), it's relative to destination rectangle size
        Vector2 origin = new Vector2((float)frameWidth, (float)frameHeight);

        int rotation = 0;

        rlj.core.SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            rotation++;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            // NOTE: Using DrawTexturePro() we can easily rotate and scale the part of the texture we draw
            // sourceRec defines the part of the texture we use for drawing
            // destRec defines the rectangle where our texture part will fit (scaling it to fit)
            // origin defines the point of the texture used as reference for rotation and scaling
            // rotation defines the texture rotation (using origin as rotation point)
            rlj.textures.DrawTexturePro(scarfy, sourceRec, destRec, origin, (float)rotation, Color.WHITE);

            rlj.shapes.DrawLine((int)destRec.x, 0, (int)destRec.x, screenHeight, Color.GRAY);
            rlj.shapes.DrawLine(0, (int)destRec.y, screenWidth, (int)destRec.y, Color.GRAY);

            rlj.text.DrawText("(c) Scarfy sprite by Eiden Marsal", screenWidth - 200, screenHeight - 20, 10,
                    Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(scarfy);        // Texture unloading
        //--------------------------------------------------------------------------------------
    }

}
