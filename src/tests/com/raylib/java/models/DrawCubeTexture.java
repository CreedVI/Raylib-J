package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.rlgl.RLGL.RL_QUADS;
import static com.raylib.java.structs.Color.RAYWHITE;
import static com.raylib.java.structs.Color.WHITE;

public class DrawCubeTexture {
    /*******************************************************************************************
     *
     *   raylib [models] example - Draw textured cube
     *
     *   Example originally created with raylib 4.5, last time updated with raylib 4.5
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2022-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static Raylib rlj;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - draw cube texture");

        // Define the camera to look into our 3d worlj.rlgl.rld
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 10.0f, 10.0f);
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);
        camera.fovy = 45.0f;
        camera.projection = CAMERA_PERSPECTIVE;

        // Load texture to be applied to the cubes sides
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/models/cubicmap_atlas.png");

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // TODO: Update your variables here
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // Draw cube with an applied texture
            DrawCubeTexture(texture, new Vector3(-2.0f, 2.0f, 0.0f), 2.0f, 4.0f, 2.0f, WHITE);

            // Draw cube with an applied texture, but only a defined rectangle piece of the texture
            DrawCubeTextureRec(texture, new Rectangle(0, texture.height/2, texture.width/2, texture.height/2),
                               new Vector3(2.0f, 1.0f, 0.0f), 2.0f, 2.0f, 2.0f, WHITE);

            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture); // Unload texture

        rlj.core.CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }

    //------------------------------------------------------------------------------------
    // Custom Functions Definition
    //------------------------------------------------------------------------------------
    // Draw cube textured
    // NOTE: Cube position is the center position
    private static void DrawCubeTexture(Texture2D texture, Vector3 position, float width, float height, float length, Color color) {
        float x = position.x;
        float y = position.y;
        float z = position.z;

        // Set desired texture to be enabled while drawing following vertex data
        rlj.rlgl.rlSetTexture(texture.id);

        // Vertex data transformation can be defined with the commented lines,
        // but in this example we calculate the transformed vertex data directly when calling rlj.rlgl.rlVertex3f()
        //rlj.rlgl.rlPushMatrix();
        // NOTE: Transformation is applied in inverse order (scale -> rotate -> translate)
        //rlj.rlgl.rlTranslatef(2.0f, 0.0f, 0.0f);
        //rlj.rlgl.rlRotatef(45, 0, 1, 0);
        //rlj.rlgl.rlScalef(2.0f, 2.0f, 2.0f);

        rlj.rlgl.rlBegin(RL_QUADS);
        rlj.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        // Front Face
        rlj.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);       // Normal Pointing Towards Viewer
        rlj.rlgl.rlTexCoord2f(0.0f, 0.0f); rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z + length/2);  // Bottom Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 0.0f); rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z + length/2);  // Bottom Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 1.0f); rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z + length/2);  // Top Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 1.0f); rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z + length/2);  // Top Left Of The Texture and Quad
        // Back Face
        rlj.rlgl.rlNormal3f(0.0f, 0.0f, - 1.0f);     // Normal Pointing Away From Viewer
        rlj.rlgl.rlTexCoord2f(1.0f, 0.0f); rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z - length/2);  // Bottom Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 1.0f); rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z - length/2);  // Top Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 1.0f); rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z - length/2);  // Top Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 0.0f); rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z - length/2);  // Bottom Left Of The Texture and Quad
        // Top Face
        rlj.rlgl.rlNormal3f(0.0f, 1.0f, 0.0f);       // Normal Pointing Up
        rlj.rlgl.rlTexCoord2f(0.0f, 1.0f); rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z - length/2);  // Top Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 0.0f); rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z + length/2);  // Bottom Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 0.0f); rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z + length/2);  // Bottom Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 1.0f); rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z - length/2);  // Top Right Of The Texture and Quad
        // Bottom Face
        rlj.rlgl.rlNormal3f(0.0f, - 1.0f, 0.0f);     // Normal Pointing Down
        rlj.rlgl.rlTexCoord2f(1.0f, 1.0f); rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z - length/2);  // Top Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 1.0f); rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z - length/2);  // Top Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 0.0f); rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z + length/2);  // Bottom Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 0.0f); rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z + length/2);  // Bottom Right Of The Texture and Quad
        // Right face
        rlj.rlgl.rlNormal3f(1.0f, 0.0f, 0.0f);       // Normal Pointing Right
        rlj.rlgl.rlTexCoord2f(1.0f, 0.0f); rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z - length/2);  // Bottom Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 1.0f); rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z - length/2);  // Top Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 1.0f); rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z + length/2);  // Top Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 0.0f); rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z + length/2);  // Bottom Left Of The Texture and Quad
        // Left Face
        rlj.rlgl.rlNormal3f( - 1.0f, 0.0f, 0.0f);    // Normal Pointing Left
        rlj.rlgl.rlTexCoord2f(0.0f, 0.0f); rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z - length/2);  // Bottom Left Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 0.0f); rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z + length/2);  // Bottom Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(1.0f, 1.0f); rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z + length/2);  // Top Right Of The Texture and Quad
        rlj.rlgl.rlTexCoord2f(0.0f, 1.0f); rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z - length/2);  // Top Left Of The Texture and Quad
        rlj.rlgl.rlEnd();
        //rlj.rlgl.rlPopMatrix();

        rlj.rlgl.rlSetTexture(0);
    }

    // Draw cube with texture piece applied to all faces
    private static void DrawCubeTextureRec(Texture2D texture, Rectangle source, Vector3 position, float width, float height, float length, Color color) {
        float x = position.x;
        float y = position.y;
        float z = position.z;
        float texWidth = (float)texture.width;
        float texHeight = (float)texture.height;

        // Set desired texture to be enabled while drawing following vertex data
        rlj.rlgl.rlSetTexture(texture.id);

        // We calculate the normalized texture coordinates for the desired texture-source-rectangle
        // It means converting from (tex.width, tex.height) coordinates to [0.0f, 1.0f] equivalent
        rlj.rlgl.rlBegin(RL_QUADS);
        rlj.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        // Front face
        rlj.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z + length/2);

        // Back face
        rlj.rlgl.rlNormal3f(0.0f, 0.0f, - 1.0f);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z - length/2);

        // Top face
        rlj.rlgl.rlNormal3f(0.0f, 1.0f, 0.0f);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z - length/2);

        // Bottom face
        rlj.rlgl.rlNormal3f(0.0f, - 1.0f, 0.0f);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z + length/2);

        // Right face
        rlj.rlgl.rlNormal3f(1.0f, 0.0f, 0.0f);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y + height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x + width/2, y - height/2, z + length/2);

        // Left face
        rlj.rlgl.rlNormal3f( - 1.0f, 0.0f, 0.0f);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z - length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, (source.y + source.height)/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y - height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f((source.x + source.width)/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z + length/2);
        rlj.rlgl.rlTexCoord2f(source.x/texWidth, source.y/texHeight);
        rlj.rlgl.rlVertex3f(x - width/2, y + height/2, z - length/2);

        rlj.rlgl.rlEnd();

        rlj.rlgl.rlSetTexture(0);
    }

}
