package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Raymath;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

public class TexturesPoly{

    final static int MAX_POINTS = 11;

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Vector2[] texcoords = {
            new Vector2( 0.75f, 0.0f ),
            new Vector2( 0.25f, 0.0f ),
            new Vector2( 0.0f, 0.5f ),
            new Vector2( 0.0f, 0.75f ),
            new Vector2( 0.25f, 1.0f),
            new Vector2( 0.375f, 0.875f),
            new Vector2( 0.625f, 0.875f),
            new Vector2( 0.75f, 1.0f),
            new Vector2( 1.0f, 0.75f),
            new Vector2( 1.0f, 0.5f),
            new Vector2( 0.75f, 0.0f)  // Close the poly
        };

        Vector2[] points = new Vector2[MAX_POINTS];

        // Create the poly coords from the UV's
        // you don't have to do this you can specify
        // them however you want
        for (int i = 0; i < MAX_POINTS; i++)
        {
            points[i] = new Vector2((texcoords[i].x - 0.5f)*256.0f, (texcoords[i].y - 0.5f)*256.0f);
        }

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - textured polygon");

        Texture2D texture = rlj.textures.LoadTexture("resources/cat.png");

        float ang = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            ang++;

            Vector2[] positions = new Vector2[MAX_POINTS];

            for (int i = 0; i < MAX_POINTS; i++){
                positions[i] = Raymath.Vector2Rotate(points[i], ang);
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("textured polygon", 20, 20, 20, Color.DARKGRAY);

            rlj.textures.DrawTexturePoly(texture, new Vector2( screenWidth/2, screenHeight/2 ),
                    positions, texcoords, MAX_POINTS, Color.WHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture); // Unload texture
        //--------------------------------------------------------------------------------------

    }

}
