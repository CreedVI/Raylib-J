package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;

public class Heightmap {

    /*******************************************************************************************
     *
     *   raylib [models] example - Heightmap loading and drawing
     *
     *   This example has been created using raylib 1.8 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2015 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - heightmap loading and drawing");

        // Define our custom camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj, new Vector3(18.0f, 18.0f, 18.0f), new Vector3(0.0f, 0.0f, 0.0f ), new Vector3(0.0f, 1.0f, 0.0f), 45.0f, CAMERA_PERSPECTIVE);

        Image image = rlj.textures.LoadImage("resources/heightmap.png");             // Load heightmap image (RAM)
        Texture2D texture = rlj.textures.LoadTextureFromImage(image);                // Convert image to texture (VRAM)

        Mesh mesh = rlj.models.GenMeshHeightmap(image, new Vector3(16, 8, 16));    // Generate heightmap mesh (RAM and VRAM)
        Model model = rlj.models.LoadModelFromMesh(mesh);                          // Load model from generated mesh

        model.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;         // Set map diffuse texture
        Vector3 mapPosition = new Vector3(-8.0f, 0.0f, -8.0f);                   // Define model position

        rlj.textures.UnloadImage(image);                     // Unload heightmap image from RAM, already uploaded to VRAM

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

            rlj.models.DrawModel(model, mapPosition, 1.0f, Color.WHITE);

            rlj.models.DrawGrid(20, 1.0f);

            rlj.core.EndMode3D();

            rlj.textures.DrawTexture(texture, screenWidth - texture.width - 20, 20, Color.WHITE);
            rlj.shapes.DrawRectangleLines(screenWidth - texture.width - 20, 20, texture.width, texture.height, Color.GREEN);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);     // Unload texture
        rlj.models.UnloadModel(model);         // Unload model
        //--------------------------------------------------------------------------------------
    }

}
