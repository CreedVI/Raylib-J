package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.extras.rLights;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.*;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.extras.rLights.LIGHT_POINT;
import static com.raylib.java.extras.rLights.MAX_LIGHTS;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.RL_SHADER_LOC_MATRIX_MODEL;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.RL_SHADER_LOC_VECTOR_VIEW;

public class BasicLighting{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - basic lighting
     *
     *   NOTE: This example requires raylib OpenGL 3.3 or ES2 versions for shaders support,
     *         OpenGL 1.1 does not support shaders, recompile raylib to OpenGL 3.3 version.
     *
     *   NOTE: Shaders used in this example are #version 330 (OpenGL 3.3).
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019-2021 Chris Camacho (@codifies)
     *
     ********************************************************************************************/

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;
        Raylib rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT);  // Enable Multi Sampling Anti Aliasing 4x (if available)
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shaders] example - basic lighting");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(2.0f, 2.0f, 6.0f);    // Camera position
        camera.target = new Vector3(0.0f, 0.5f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                        // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                     // Camera mode type

        // Load models
        Model modelA = rlj.models.LoadModelFromMesh(rlj.models.GenMeshTorus(0.4f, 1.0f, 16, 32));
        Model modelB = rlj.models.LoadModelFromMesh(rlj.models.GenMeshCube(1.0f, 1.0f, 1.0f));
        Model modelC = rlj.models.LoadModelFromMesh(rlj.models.GenMeshSphere(0.5f, 32, 32));

        // Load models texture
        Texture2D texture = rlj.textures.LoadTexture("/src/tests/resources/shaders/texel_checker.png");

        // Assign texture to default model material
        modelA.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;
        modelB.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;
        modelC.materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = texture;

        Shader shader = rlj.core.LoadShader("/src/tests/resources/shaders/shaders/glsl330/base_lighting.vs",
                                            "/src/tests/resources/shaders/shaders/glsl330/lighting.fs");

        // Get some shader loactions
        shader.locs[RL_SHADER_LOC_MATRIX_MODEL.GetLocation()] = rlj.core.GetShaderLocation(shader, "matModel");
        shader.locs[RL_SHADER_LOC_VECTOR_VIEW.GetLocation()] = rlj.core.GetShaderLocation(shader, "viewPos");

        // ambient light level
        int ambientLoc = rlj.core.GetShaderLocation(shader, "ambient");
        rlj.core.SetShaderValue(shader, ambientLoc, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC4);

        float angle = 6.282f;

        // All models use the same shader
        modelA.materials[0].shader = shader;
        modelB.materials[0].shader = shader;
        modelC.materials[0].shader = shader;

        // Using 4 point lights, white, red, green and blue
        rLights.Light[] lights = new rLights.Light[MAX_LIGHTS];
        lights[0] = rLights.CreateLight(rlj, LIGHT_POINT, new Vector3(4, 2, 4), new Vector3(), Color.WHITE, shader);
        lights[1] = rLights.CreateLight(rlj, LIGHT_POINT, new Vector3(4, 2, 4), new Vector3(), Color.RED, shader);
        lights[2] = rLights.CreateLight(rlj, LIGHT_POINT, new Vector3(0, 4, 2), new Vector3(), Color.GREEN, shader);
        lights[3] = rLights.CreateLight(rlj, LIGHT_POINT, new Vector3(0, 4, 2), new Vector3(), Color.BLUE, shader);

        rlj.core.SetTargetFPS(60);                       // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())            // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_W)) { lights[0].enabled = !lights[0].enabled; }
            if (rlj.core.IsKeyPressed(KEY_R)) { lights[1].enabled = !lights[1].enabled; }
            if (rlj.core.IsKeyPressed(KEY_G)) { lights[2].enabled = !lights[2].enabled; }
            if (rlj.core.IsKeyPressed(KEY_B)) { lights[3].enabled = !lights[3].enabled; }

            camera.Update(CAMERA_ORBITAL);              // Update camera

            // Make the lights do differing orbits
            angle -= 0.02f;
            lights[0].position.x = (float) (Math.cos(angle)*4.0f);
            lights[0].position.z = (float) (Math.sin(angle)*4.0f);
            lights[1].position.x = (float) (Math.cos(-angle*0.6f)*4.0f);
            lights[1].position.z = (float) (Math.sin(-angle*0.6f)*4.0f);
            lights[2].position.y = (float) (Math.cos(angle*0.2f)*4.0f);
            lights[2].position.z = (float) (Math.sin(angle*0.2f)*4.0f);
            lights[3].position.y = (float) (Math.cos(-angle*0.35f)*4.0f);
            lights[3].position.z = (float) (Math.sin(-angle*0.35f)*4.0f);

            rLights.UpdateLightValues(rlj, shader, lights[0]);
            rLights.UpdateLightValues(rlj, shader, lights[1]);
            rLights.UpdateLightValues(rlj, shader, lights[2]);
            rLights.UpdateLightValues(rlj, shader, lights[3]);

            // Rotate the torus
            modelA.transform = MatrixMultiply(modelA.transform, MatrixRotateX(-0.025f));
            modelA.transform = MatrixMultiply(modelA.transform, MatrixRotateZ(0.012f));

            // Update the light shader with the camera view position
            float[] cameraPos = new float[]{ camera.position.x, camera.position.y, camera.position.z };
            rlj.core.SetShaderValue(shader, shader.locs[RL_SHADER_LOC_VECTOR_VIEW.GetLocation()], cameraPos, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC3);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // Draw the three models
            rlj.models.DrawModel(modelA, new Vector3(), 1.0f, Color.WHITE);
            rlj.models.DrawModel(modelB, new Vector3(-1.6f,0.0f,0.0f), 1.0f, Color.WHITE);
            rlj.models.DrawModel(modelC, new Vector3(1.6f,0.0f,0.0f), 1.0f, Color.WHITE);

            // Draw markers to show where the lights are
            if (lights[0].enabled) { rlj.models.DrawSphereEx(lights[0].position, 0.2f, 8, 8, Color.WHITE); }
            if (lights[1].enabled) { rlj.models.DrawSphereEx(lights[1].position, 0.2f, 8, 8, Color.RED); }
            if (lights[2].enabled) { rlj.models.DrawSphereEx(lights[2].position, 0.2f, 8, 8, Color.GREEN); }
            if (lights[3].enabled) { rlj.models.DrawSphereEx(lights[3].position, 0.2f, 8, 8, Color.BLUE); }

            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            rlj.text.DrawFPS(10, 10);

            rlj.text.DrawText("Use keys RGBW to toggle lights", 10, 30, 20, Color.DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        //
        rlj.models.UnloadModel(modelA);        // Unload the modelA
        rlj.models.UnloadModel(modelB);        // Unload the modelB
        rlj.models.UnloadModel(modelC);        // Unload the modelC

        rlj.textures.UnloadTexture(texture);     // Unload the texture
        rlj.core.UnloadShader(shader);       // Unload shader
        //--------------------------------------------------------------------------------------
    }
}
