package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_EMISSION;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.RL_SHADER_LOC_MAP_EMISSION;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_INT;
import static com.raylib.java.structs.Color.*;

public class SimpleMask {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Simple shader mask
     *
     *   Example originally created with raylib 2.5, last time updated with raylib 3.7
     *
     *   Example contributed by Chris Camacho (@chriscamacho) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2019-2023 Chris Camacho (@chriscamacho) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************
     *
     *   After a model is loaded it has a default material, this material can be
     *   modified in place rather than creating one from scratch...
     *   While all of the maps have particular names, they can be used for any purpose
     *   except for three maps that are applied as cubic maps (see below)
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

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - simple shader mask");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 1.0f, 2.0f);    // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Define our three models to show the shader on
        Mesh torus = rlj.models.GenMeshTorus(0.3f, 1, 16, 32);
        Model model1 = rlj.models.LoadModelFromMesh(torus);

        Mesh cube = rlj.models.GenMeshCube(0.8f,0.8f,0.8f);
        Model model2 = rlj.models.LoadModelFromMesh(cube);

        // Generate model to be shaded just to see the gaps in the other two
        Mesh sphere = rlj.models.GenMeshSphere(1, 16, 16);
        Model model3 = rlj.models.LoadModelFromMesh(sphere);

        // Load the shader
        Shader shader = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/mask.fs", GLSL_VERSION));

        // Load and apply the diffuse texture (colour map)
        Texture2D texDiffuse = rlj.textures.LoadTexture("src/tests/resources/shaders/plasma.png");
        model1.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texDiffuse;
        model2.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texDiffuse;

        // Using MATERIAL_MAP_EMISSION as a spare slot to use for 2nd texture
        // NOTE: Don't use MATERIAL_MAP_IRRADIANCE, MATERIAL_MAP_PREFILTER or  MATERIAL_MAP_CUBEMAP as they are bound as cube maps
        Texture2D texMask = rlj.textures.LoadTexture("src/tests/resources/shaders/mask.png");
        model1.materials[0].maps[MATERIAL_MAP_EMISSION].texture = texMask;
        model2.materials[0].maps[MATERIAL_MAP_EMISSION].texture = texMask;
        shader.locs[RL_SHADER_LOC_MAP_EMISSION.GetLocation()] = rlj.core.GetShaderLocation(shader, "mask");

        // Frame is incremented each frame to animate the shader
        int shaderFrame = rlj.core.GetShaderLocation(shader, "frame");

        // Apply the shader to the two models
        model1.materials[0].shader = shader;
        model2.materials[0].shader = shader;

        int framesCounter = 0;
        Vector3 rotation = new Vector3();           // Model rotation angles

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window
        rlj.core.SetTargetFPS(60);                   // Set  to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);

            framesCounter++;
            rotation.x += 0.01f;
            rotation.y += 0.005f;
            rotation.z -= 0.0025f;

            // Send frames counter to shader for animation
            rlj.core.SetShaderValue(shader, shaderFrame, new float[] {framesCounter}, RL_SHADER_UNIFORM_INT);

            // Rotate one of the models
            model1.transform = Raymath.MatrixRotateXYZ(rotation);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(DARKBLUE);

            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(model1, new Vector3(0.5f, 0.0f, 0.0f), 1, WHITE);
            rlj.models.DrawModelEx(model2, new Vector3(-0.5f, 0.0f, 0.0f), new Vector3(1.0f, 1.0f, 0.0f), 50, new Vector3(1.0f, 1.0f, 1.0f), WHITE);
            rlj.models.DrawModel(model3,new Vector3(0.0f, 0.0f, -1.5f), 1, WHITE);
            rlj.models.DrawGrid(10, 1.0f);        // Draw a grid

            rlj.core.EndMode3D();

            rlj.shapes.DrawRectangle(16, 698, rlj.text.MeasureText(rlj.text.TextFormat("Frame: %d", framesCounter), 20) + 8, 42, BLUE);
            rlj.text.DrawText(rlj.text.TextFormat("Frame: %d", framesCounter), 20, 700, 20, WHITE);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.models.UnloadModel(model1);
        rlj.models.UnloadModel(model2);
        rlj.models.UnloadModel(model3);

        rlj.textures.UnloadTexture(texDiffuse);  // Unload default diffuse texture
        rlj.textures.UnloadTexture(texMask);     // Unload texture mask

        rlj.core.UnloadShader(shader);       // Unload shader

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
