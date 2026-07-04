package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.structs.Color.*;

public class ModelLoading {

    /*******************************************************************************************
     *
     *   raylib [models] example - Models loading
     *
     *   NOTE: raylib supports multiple models file formats:
     *
     *     - OBJ  > Text file format. Must include vertex position-texcoords-normals information,
     *              if files references some .mtl materials file, it will be loaded (or try to).
     *     - GLTF > Text/binary file format. Includes lot of information and it could
     *              also reference external files, raylib will try loading mesh and materials data.
     *     - IQM  > Binary file format. Includes mesh vertex data but also animation data,
     *              raylib can load .iqm animations.
     *     - VOX  > Binary file format. MagikaVoxel mesh format:
     *              https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt
     *     - M3D  > Binary file format. Model 3D format:
     *              https://bztsrc.gitlab.io/model3d
     *
     *   Example originally created with raylib 2.0, last time updated with raylib 4.2
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2014-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - models loading");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(50.0f, 50.0f, 50.0f); // Camera position
        camera.target = new Vector3(0.0f, 10.0f, 0.0f);     // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                   // Camera mode type

        Model model = rlj.models.LoadModel("src/tests/resources/models/models/obj/castle.obj");                 // Load model
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/models/models/obj/castle_diffuse.png"); // Load model texture
        model.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;            // Set map diffuse texture

        Vector3 position = new Vector3();                    // Set model position

        BoundingBox bounds = rlj.models.GetMeshBoundingBox(model.meshes[0]);   // Set model bounds

        // NOTE: bounds are calculated from the original size of the model,
        // if model is scaled on drawing, bounds must be also scaled

        boolean selected = false;          // Selected object flag

        rlj.core.DisableCursor();                // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);

            // Load new models/textures on drag&drop
            if (rlj.core.IsFileDropped()) {
                FilePathList droppedFiles = rlj.core.LoadDroppedFiles();

                // Only support one file dropped
                if (droppedFiles.count == 1) {
                    if (rlj.core.IsFileExtension(droppedFiles.paths[0], ".obj") ||
                            rlj.core.IsFileExtension(droppedFiles.paths[0], ".gltf") ||
                            rlj.core.IsFileExtension(droppedFiles.paths[0], ".glb") ||
                            rlj.core.IsFileExtension(droppedFiles.paths[0], ".vox") ||
                            rlj.core.IsFileExtension(droppedFiles.paths[0], ".iqm") ||
                            rlj.core.IsFileExtension(droppedFiles.paths[0], ".m3d"))       // Model file formats supported
                    {
                        rlj.models.UnloadModel(model);                         // Unload previous model
                        model = rlj.models.LoadModel(droppedFiles.paths[0]);   // Load new model
                        model.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture; // Set current map diffuse texture

                        bounds = rlj.models.GetMeshBoundingBox(model.meshes[0]);

                        // TODO: Move camera position from target enough distance to visualize model properly
                    }
                    // Texture file formats supported
                    else if (rlj.core.IsFileExtension(droppedFiles.paths[0], ".png")) {
                        // Unload current model texture and load new one
                        rlj.textures.UnloadTexture(texture);
                        texture = rlj.textures.LoadTexture(droppedFiles.paths[0]);
                        model.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;
                    }
                }

                rlj.core.UnloadDroppedFiles(droppedFiles);    // Unload filepaths from memory
            }

            // Select model on mouse click
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                // Check collision between ray and box
                if (rlj.models.GetRayCollisionBox(rlj.core.GetMouseRay(rlj.core.GetMousePosition(), camera), bounds).hit) {
                    selected = !selected;
                }
                else {
                    selected = false;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(model, position, 1.0f, WHITE);        // Draw 3d model with texture

            rlj.models.DrawGrid(20, 10.0f);         // Draw a grid

            if (selected) {
                rlj.models.DrawBoundingBox(bounds, GREEN);   // Draw selection box
            }

            rlj.core.EndMode3D();

            rlj.text.DrawText("Drag & drop model to load mesh/texture.", 10, rlj.core.GetScreenHeight() - 20, 10, DARKGRAY);
            if (selected) {
                rlj.text.DrawText("MODEL SELECTED", rlj.core.GetScreenWidth() - 110, 10, 10, GREEN);
            }

            rlj.text.DrawText("(c) Castle 3D model by Alberto Cano", screenWidth - 200, screenHeight - 20, 10, GRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texture);     // Unload texture
        rlj.models.UnloadModel(model);         // Unload model

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
