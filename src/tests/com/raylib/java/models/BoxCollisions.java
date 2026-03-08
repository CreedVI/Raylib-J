package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.BoundingBox;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;

public class BoxCollisions {

    /*******************************************************************************************
     *
     *   Raylib-J [models] example - Detect basic 3d collisions (box vs sphere vs box)
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - box collisions");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 10.0f, 10.0f);
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);
        camera.up =  new Vector3(0.0f, 1.0f, 0.0f);
        camera.fovy = 45.0f;
        camera.projection = Camera3D.CameraProjection.CAMERA_PERSPECTIVE;

        Vector3 playerPosition = new Vector3(0.0f, 1.0f, 2.0f);
        Vector3 playerSize = new Vector3(1.0f, 2.0f, 1.0f);
        Color playerColor = Color.GREEN;

        Vector3 enemyBoxPos = new Vector3(-4.0f, 1.0f, 0.0f);
        Vector3 enemyBoxSize = new Vector3(2.0f, 2.0f, 2.0f);

        Vector3 enemySpherePos = new Vector3(4.0f, 0.0f, 0.0f);
        float enemySphereSize = 1.5f;

        boolean collision;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {    // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------

            // Move player
            if      (rlj.core.IsKeyDown(KEY_RIGHT)) {
                playerPosition.x += 0.2f;
            }
            else if (rlj.core.IsKeyDown(KEY_LEFT)) {
                playerPosition.x -= 0.2f;
            }
            else if (rlj.core.IsKeyDown(KEY_DOWN)) {
                playerPosition.z += 0.2f;
            }
            else if (rlj.core.IsKeyDown(KEY_UP)) {
                playerPosition.z -= 0.2f;
            }

            collision = false;

            // Check collisions player vs enemy-box
            if (rlj.models.CheckCollisionBoxes(new BoundingBox(new Vector3(playerPosition.x - playerSize.x/2, playerPosition.y - playerSize.y/2, playerPosition.z - playerSize.z/2),
                                                               new Vector3(playerPosition.x + playerSize.x/2, playerPosition.y + playerSize.y/2, playerPosition.z + playerSize.z/2)),
                           new BoundingBox(new Vector3(enemyBoxPos.x - enemyBoxSize.x/2,enemyBoxPos.y - enemyBoxSize.y/2,enemyBoxPos.z - enemyBoxSize.z/2),
                           new Vector3(enemyBoxPos.x + enemyBoxSize.x/2,enemyBoxPos.y + enemyBoxSize.y/2,enemyBoxPos.z + enemyBoxSize.z/2)))
            ) {
                collision = true;
            }

            // Check collisions player vs enemy-sphere
            if (rlj.models.CheckCollisionBoxSphere(new BoundingBox(new Vector3(playerPosition.x - playerSize.x/2, playerPosition.y - playerSize.y/2, playerPosition.z - playerSize.z/2),
                            new Vector3(playerPosition.x + playerSize.x/2, playerPosition.y + playerSize.y/2, playerPosition.z + playerSize.z/2)),
                            enemySpherePos, enemySphereSize)
            ) {
                collision = true;
            }

            if (collision) {
                playerColor = Color.RED;
            }
            else {
                playerColor = Color.GREEN;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // Draw enemy-box
            rlj.models.DrawCube(enemyBoxPos, enemyBoxSize.x, enemyBoxSize.y, enemyBoxSize.z, Color.GRAY);
            rlj.models.DrawCubeWires(enemyBoxPos, enemyBoxSize.x, enemyBoxSize.y, enemyBoxSize.z, Color.DARKGRAY);

            // Draw enemy-sphere
            rlj.models.DrawSphere(enemySpherePos, enemySphereSize, Color.GRAY);
            rlj.models.DrawSphereWires(enemySpherePos, enemySphereSize, 16, 16, Color.DARKGRAY);

            // Draw player
            rlj.models.DrawCubeV(playerPosition, playerSize, playerColor);

            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            rlj.core.EndMode3D();

            rlj.text.DrawText("Move player with cursors to collide", 220, 40, 20, Color.GRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        // Nothing to do here...
        //--------------------------------------------------------------------------------------
    }

}
