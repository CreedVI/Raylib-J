package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;

public class Billboard {

    /*******************************************************************************************
     *
     *   Raylib-J [models] example - Drawing billboards
     *
     *   This example has been created using Raylib-J 0.5
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - drawing billboards");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(5.0f, 4.0f, 5.0f);
        camera.target = new Vector3(0.0f, 2.0f, 0.0f);
        camera.up =  new Vector3(0.0f, 1.0f, 0.0f);
        camera.fovy = 45.0f;
        camera.projection = CAMERA_PERSPECTIVE;

        Texture2D bill = rlj.textures.LoadTexture("resources/billboard.png");     // Our texture billboard
        Vector3 billPosition = new Vector3(0.0f, 2.0f, 0.0f);                 // Position where draw billboard

        rlj.core.SetTargetFPS(60);                       // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {           // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);              // Update camera
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            rlj.models.DrawBillboard(camera, bill, billPosition, 2.0f, Color.WHITE);

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(bill);        // Unload texture
        //--------------------------------------------------------------------------------------
    }

}
