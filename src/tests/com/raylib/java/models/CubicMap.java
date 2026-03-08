package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;

public class CubicMap {

    /*******************************************************************************************
     *
     *   Raylib-J [models] example - Cubicmap loading and drawing
     *
     *   This example has been created using Raylib-J 0.5 (www.raylib.com)
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - cubesmap loading and drawing");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(16.0f, 14.0f, 16.0f);
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);
        camera.up =  new Vector3(0.0f, 1.0f, 0.0f);
        camera.fovy = 45.0f;
        camera.projection = Camera3D.CameraProjection.CAMERA_PERSPECTIVE;

        Image image = rlj.textures.LoadImage("resources/cubicmap.png");      // Load cubicmap image (RAM)
        Texture2D cubicmap = rlj.textures.LoadTextureFromImage(image);       // Convert image to texture to display (VRAM)

        Mesh mesh = rlj.models.GenMeshCubicmap(image, new Vector3(1.0f, 1.0f, 1.0f));
        Model model = rlj.models.LoadModelFromMesh(mesh);

        // NOTE: By default each cube is mapped to one part of texture atlas
        Texture2D texture = rlj.textures.LoadTexture("resources/cubicmap_atlas.png");    // Load map texture
        model.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;             // Set map diffuse texture

        Vector3 mapPosition = new Vector3(-16.0f, 0.0f, -8.0f);          // Set model position

        rlj.textures.UnloadImage(image);     // Unload cubesmap image from RAM, already uploaded to VRAM

        rlj.core.SetTargetFPS(60);                       // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {          // Detect window close button or ESC key
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

            rlj.core.EndMode3D();

            rlj.textures.DrawTextureEx(cubicmap, new Vector2(screenWidth - cubicmap.width*4.0f - 20, 20.0f), 0.0f, 4.0f, Color.WHITE);
            rlj.shapes.DrawRectangleLines(screenWidth - cubicmap.width*4 - 20, 20, cubicmap.width*4, cubicmap.height*4, Color.GREEN);

            rlj.text.DrawText("cubicmap image used to", 658, 90, 10, Color.GRAY);
            rlj.text.DrawText("generate map 3d model", 658, 104, 10, Color.GRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(cubicmap);    // Unload cubicmap texture
        rlj.textures.UnloadTexture(texture);     // Unload map texture
        rlj.models.UnloadModel(model);         // Unload map model
        //--------------------------------------------------------------------------------------
    }

}
