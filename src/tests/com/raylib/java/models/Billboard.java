package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.raymath.Raymath.Vector3Distance;
import static com.raylib.java.structs.Color.WHITE;

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

        Texture2D bill = rlj.textures.LoadTexture("src/tests/resources/models/billboard.png");     // Our billboard texture
        Vector3 billPositionStatic = new Vector3(0.0f, 2.0f, 0.0f);                 // Position of static billboard
        Vector3 billPositionRotating = new Vector3(1.0f, 2.0f, 1.0f);               // Position of rotating billboard

        // Entire billboard texture, source is used to take a segment from a larger texture.
        Rectangle source = new Rectangle(0.0f, 0.0f, (float)bill.width, (float)bill.height);

        // NOTE: Billboard locked on axis-Y
        Vector3 billUp = new Vector3(0.0f, 1.0f, 0.0f);

        // Rotate around origin
        // Here we choose to rotate around the image center
        // NOTE: (-1, 1) is the range where origin.x, origin.y is inside the texture
        Vector2 rotateOrigin = new Vector2();

        // Distance is needed for the correct billboard draw order
        // Larger distance (further away from the camera) should be drawn prior to smaller distance.
        float distanceStatic;
        float distanceRotating;
        float rotation = 0.0f;

        rlj.core.SetTargetFPS(60);                       // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {           // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);              // Update camera

            rotation += 0.4f;
            distanceStatic = Vector3Distance(camera.position, billPositionStatic);
            distanceRotating = Vector3Distance(camera.position, billPositionRotating);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            // Draw order matters!
            if (distanceStatic > distanceRotating) {
                rlj.models.DrawBillboard(camera, bill, billPositionStatic, 2.0f, WHITE);
                rlj.models.DrawBillboardPro(camera, bill, source, billPositionRotating, billUp, new Vector2(1.0f, 1.0f), rotateOrigin, rotation, WHITE);
            }
            else {
                rlj.models.DrawBillboardPro(camera, bill, source, billPositionRotating, billUp, new Vector2(1.0f, 1.0f), rotateOrigin, rotation, WHITE);
                rlj.models.DrawBillboard(camera, bill, billPositionStatic, 2.0f, WHITE);
            }

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(bill);        // Unload texture

        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
