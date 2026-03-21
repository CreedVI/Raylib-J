package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FREE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;

public class FirstPersonMaze {

    /*******************************************************************************************
     *
     *   raylib [models] example - first person maze
     *
     *   This example has been created using raylib 2.5 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2019 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - first person maze");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj, new Vector3(0.2f, 0.4f, 0.2f), new Vector3(0.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f), 45.0f, CAMERA_PERSPECTIVE);

        Image imMap = rlj.textures.LoadImage("src/tests/resources/models/cubicmap.png");      // Load cubicmap image (RAM)
        Texture2D cubicmap = rlj.textures.LoadTextureFromImage(imMap);       // Convert image to texture to display (VRAM)
        Mesh mesh = rlj.models.GenMeshCubicmap(imMap, new Vector3(1.0f, 1.0f, 1.0f));
        Model model = rlj.models.LoadModelFromMesh(mesh);

        // NOTE: By default each cube is mapped to one part of texture atlas
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/models/cubicmap_atlas.png");    // Load map texture
        model.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;             // Set map diffuse texture

        // Get map image data to be used for collision detection
        byte[] mapPixels = rlj.textures.LoadImageColors(imMap);
        rlj.textures.UnloadImage(imMap);             // Unload image from RAM

        Vector3 mapPosition = new Vector3(-16.0f, 0.0f, -8.0f);  // Set model position

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {   // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            Vector3 oldCamPos = camera.position;    // Store old camera position

            camera.Update(CAMERA_FREE);      // Update camera

            // Check player collision (we simplify to 2D collision detection)
            Vector2 playerPos = new Vector2(camera.position.x, camera.position.z);
            float playerRadius = 0.1f;  // Collision radius (player is modelled as a cilinder for collision)

            int playerCellX = (int)(playerPos.x - mapPosition.x + 0.5f);
            int playerCellY = (int)(playerPos.y - mapPosition.z + 0.5f);

            // Out-of-limits security check
            if (playerCellX < 0) playerCellX = 0;
            else if (playerCellX >= cubicmap.width) playerCellX = cubicmap.width - 1;

            if (playerCellY < 0) playerCellY = 0;
            else if (playerCellY >= cubicmap.height) playerCellY = cubicmap.height - 1;

            // Check map collisions using image data and player position
            // TODO: Improvement: Just check player surrounding cells for collision
            for (int y = 0; y < cubicmap.height; y++) {
                for (int x = 0; x < cubicmap.width; x++) {
                    if ((mapPixels[y*cubicmap.width + x] == 255) &&       // Collision: white pixel, only check R channel
                            (rlj.shapes.CheckCollisionCircleRec(playerPos, playerRadius,
                                                     new Rectangle(mapPosition.x - 0.5f + x*1.0f, mapPosition.z - 0.5f + y*1.0f, 1.0f, 1.0f))))
                    {
                        // Collision detected, reset camera position
                        camera.position = oldCamPos;
                    }
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);
            rlj.models.DrawModel(model, mapPosition, 1.0f, Color.WHITE);                     // Draw maze map
            rlj.core.EndMode3D();

            rlj.textures.DrawTextureEx(cubicmap, new Vector2(rlj.core.GetScreenWidth() - cubicmap.width*4.0f - 20, 20.0f), 0.0f, 4.0f, Color.WHITE);
            rlj.shapes.DrawRectangleLines(rlj.core.GetScreenWidth() - cubicmap.width*4 - 20, 20, cubicmap.width*4, cubicmap.height*4, Color.GREEN);

            // Draw player position radar
            rlj.shapes.DrawRectangle(rlj.core.GetScreenWidth() - cubicmap.width*4 - 20 + playerCellX*4, 20 + playerCellY*4, 4, 4, Color.RED);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(cubicmap);        // Unload cubicmap texture
        rlj.textures.UnloadTexture(texture);         // Unload map texture
        rlj.models.UnloadModel(model);             // Unload map model
        //--------------------------------------------------------------------------------------

    }

}
