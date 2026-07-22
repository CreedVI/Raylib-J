package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Model;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.structs.Color.*;

public class ModelShader {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Model shader
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3), to test this example
     *         on OpenGL ES 2.0 platforms (Android, Raspberry Pi, HTML5), use #version 100 shaders
     *         raylib comes with shaders ready for both versions, check raylib/shaders install folder
     *
     *   Example originally created with raylib 1.3, last time updated with raylib 3.7
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2014-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static final int GLSL_VERSION = 330;
    // private static final int GLSL_VERSION = 110;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;
        Raylib rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT);      // Enable Multi Sampling Anti Aliasing 4x (if available)

        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shaders] example - model shader");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(4.0f, 4.0f, 4.0f);    // Camera position
        camera.target = new Vector3(0.0f, 1.0f, -1.0f);     // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Model model = rlj.models.LoadModel("src/tests/resources/shaders/models/watermill.obj");                   // Load OBJ model
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/shaders/models/watermill_diffuse.png");   // Load model texture

        // Load shader for model
        // NOTE: Defining null for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/grayscale.fs", GLSL_VERSION));

        model.materials[0].shader = shader;                     // Set shader effect to 3d model
        model.materials[0].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = texture; // Bind texture to model

        Vector3 position = new Vector3();    // Set model position

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window
        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(RAYWHITE);
            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(model, position, 0.2f, WHITE);   // Draw 3d model with texture
            rlj.models.DrawGrid(10, 1.0f);     // Draw a grid

            rlj.core.EndMode3D();

            rlj.text.DrawText("(c) Watermill 3D model by Alberto Cano", screenWidth - 210, screenHeight - 20, 10, GRAY);
            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);       // Unload shader
        rlj.textures.UnloadTexture(texture);     // Unload texture
        rlj.models.UnloadModel(model);         // Unload model

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
