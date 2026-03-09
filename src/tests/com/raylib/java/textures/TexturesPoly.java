package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.rlgl.RLGL.RL_QUADS;

public class TexturesPoly{

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - Textured polygon
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

    final static int MAX_POINTS = 11;
    static Raylib rlj;

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Vector2[] texcoords = {
            new Vector2(0.75f, 0.0f ),
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
        for (int i = 0; i < MAX_POINTS; i++) {
            points[i] = new Vector2((texcoords[i].x - 0.5f)*256.0f, (texcoords[i].y - 0.5f)*256.0f);
        }

        rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - textured polygon");

        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/textures/cat.png");

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

            DrawTexturePoly(texture, new Vector2( screenWidth/2.0f, screenHeight/2.0f ),
                    positions, texcoords, MAX_POINTS, Color.WHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture); // Unload texture
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------

    }

    // Draw textured polygon, defined by vertex and texturecoordinates
    // NOTE: Polygon center must have straight line path to all points
    // without crossing perimeter, points must be in anticlockwise order
    public static void DrawTexturePoly(Texture2D texture, Vector2 center, Vector2[] points, Vector2[] texcoords, int pointsCount,
                                       Color tint) {
        rlj.rlgl.rlCheckRenderBatchLimit((pointsCount - 1) * 4);

        rlj.rlgl.rlSetTexture(texture.id);

        // Texturing is only supported on QUADs
        rlj.rlgl.rlBegin(RL_QUADS);

        rlj.rlgl.rlColor4ub(tint.r, tint.g, tint.b, tint.a);

        for (int i = 0; i < pointsCount - 1; i++) {
            rlj.rlgl.rlTexCoord2f(0.5f, 0.5f);
            rlj.rlgl.rlVertex2f(center.x, center.y);

            rlj.rlgl.rlTexCoord2f(texcoords[i].x, texcoords[i].y);
            rlj.rlgl.rlVertex2f(points[i].x + center.x, points[i].y + center.y);

            rlj.rlgl.rlTexCoord2f(texcoords[i + 1].x, texcoords[i + 1].y);
            rlj.rlgl.rlVertex2f(points[i + 1].x + center.x, points[i + 1].y + center.y);

            rlj.rlgl.rlTexCoord2f(texcoords[i + 1].x, texcoords[i + 1].y);
            rlj.rlgl.rlVertex2f(points[i + 1].x + center.x, points[i + 1].y + center.y);
        }
        rlj.rlgl.rlEnd();

        rlj.rlgl.rlSetTexture(0);
    }

}
