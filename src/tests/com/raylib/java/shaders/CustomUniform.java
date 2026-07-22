package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2;
import static com.raylib.java.structs.Color.*;

public class CustomUniform {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Apply a postprocessing shader and connect a custom uniform variable
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3), to test this example
     *         on OpenGL ES 2.0 platforms (Android, Raspberry Pi, HTML5), use #version 100 shaders
     *         raylib comes with shaders ready for both versions, check raylib/shaders install folder
     *
     *   Example originally created with raylib 1.3, last time updated with raylib 4.0
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2015-2023 Ramon Santamaria (@raysan5)
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

        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shaders] example - custom uniform variable");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(8, 8, 8);    // Camera position
        camera.target = new Vector3(0, 1.5f, 0);      // Camera looking at point
        camera.up = new Vector3(0, 1, 0);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Model model = rlj.models.LoadModel("src/tests/resources/shaders/models/barracks.obj");                   // Load OBJ model
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/shaders/models/barracks_diffuse.png");   // Load model texture (diffuse map)
        model.materials[0].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = texture;                     // Set model diffuse texture

        Vector3 position = new Vector3();                                    // Set model position

        // Load postprocessing shader
        // NOTE: Defining null for vertex shader forces usage of internal default vertex shader
        Shader shader = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/swirl.fs", GLSL_VERSION));

        // Get variable (uniform) location on the shader to connect with the program
        // NOTE: If uniform variable could not be found in the shader, function returns -1
        int swirlCenterLoc = rlj.core.GetShaderLocation(shader, "center");

        float[] swirlCenter = { (float)screenWidth/2, (float)screenHeight/2 };

        // Create a RenderTexture2D to be used for render to texture
        RenderTexture target = rlj.textures.LoadRenderTexture(screenWidth, screenHeight);

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);

            Vector2 mousePosition = rlj.core.GetMousePosition();

            swirlCenter[0] = mousePosition.x;
            swirlCenter[1] = screenHeight - mousePosition.y;

            // Send new value to the shader to be used on drawing
            rlj.core.SetShaderValue(shader, swirlCenterLoc, swirlCenter, SHADER_UNIFORM_VEC2);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginTextureMode(target);       // Enable drawing to texture
            rlj.core.ClearBackground(RAYWHITE);  // Clear texture background

            rlj.core.BeginMode3D(camera);        // Begin 3d mode drawing
            rlj.models.DrawModel(model, position, 0.5f, WHITE);   // Draw 3d model with texture
            rlj.models.DrawGrid(10, 1.0f);     // Draw a grid
            rlj.core.EndMode3D();                // End 3d mode drawing, returns to orthographic 2d mode

            rlj.text.DrawText("TEXT DRAWN IN RENDER TEXTURE", 200, 10, 30, RED);
            rlj.core.EndTextureMode();               // End drawing to texture (now we have a texture available for next passes)

            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(RAYWHITE);  // Clear screen background

            // Enable shader using the custom uniform
            rlj.core.BeginShaderMode(shader);
            // NOTE: Render texture must be y-flipped due to default OpenGL coordinates (left-bottom)
            rlj.textures.DrawTextureRec(target.texture, new Rectangle(0, 0, (float)target.texture.width, (float)-target.texture.height), new Vector2(), WHITE);
            rlj.core.EndShaderMode();

            // Draw some 2d text over drawn texture
            rlj.text.DrawText("(c) Barracks 3D model by Alberto Cano", screenWidth - 220, screenHeight - 20, 10, GRAY);
            rlj.text.DrawFPS(10, 10);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(shader);               // Unload shader
        rlj.textures.UnloadTexture(texture);             // Unload texture
        rlj.models.UnloadModel(model);                 // Unload model
        rlj.textures.UnloadRenderTexture(target);        // Unload render texture

        rlj.core.CloseWindow();                      // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
