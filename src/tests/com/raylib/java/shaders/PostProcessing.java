package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;
import static com.raylib.java.structs.Color.*;

public class PostProcessing {

        /*******************************************************************************************
         *
         *   raylib [shaders] example - Apply a postprocessing shader to a scene
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

        private static final int
            FX_GRAYSCALE = 0,
            FX_POSTERIZATION = 1,
            FX_DREAM_VISION = 2,
            FX_PIXELIZER = 3,
            FX_CROSS_HATCHING = 4,
            FX_CROSS_STITCHING = 5,
            FX_PREDATOR_VIEW = 6,
            FX_SCANLINES = 7,
            FX_FISHEYE = 8,
            FX_SOBEL = 9,
            FX_BLOOM = 10,
            FX_BLUR = 11;

        private static String[] postproShaderText = {
                "GRAYSCALE",
                "POSTERIZATION",
                "DREAM_VISION",
                "PIXELIZER",
                "CROSS_HATCHING",
                "CROSS_STITCHING",
                "PREDATOR_VIEW",
                "SCANLINES",
                "FISHEYE",
                "SOBEL",
                "BLOOM",
                "BLUR",
        };

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

                rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shaders] example - postprocessing shader");

                // Define the camera to look into our 3d world
                Camera3D camera = new Camera3D(rlj);
                camera.position = new Vector3(2.0f, 3.0f, 2.0f);    // Camera position
                camera.target = new Vector3(0.0f, 1.0f, 0.0f);      // Camera looking at point
                camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
                camera.fovy = 45.0f;                                // Camera field-of-view Y
                camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

                Model model = rlj.models.LoadModel("src/tests/resources/shaders/models/church.obj");                 // Load OBJ model
                Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/shaders/models/church_diffuse.png"); // Load model texture (diffuse map)
                model.materials[0].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = texture;        // Set model diffuse texture

                Vector3 position = new Vector3();            // Set model position

                // Load all postpro shaders
                // NOTE 1: All postpro shader use the base vertex shader (DEFAULT_VERTEX_SHADER)
                // NOTE 2: We load the correct shader depending on GLSL version
                Shader[] shaders = new Shader[12];

                // NOTE: Defining null for vertex shader forces usage of internal default vertex shader
                shaders[FX_GRAYSCALE] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/grayscale.fs", GLSL_VERSION));
                shaders[FX_POSTERIZATION] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/posterization.fs", GLSL_VERSION));
                shaders[FX_DREAM_VISION] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/dream_vision.fs", GLSL_VERSION));
                shaders[FX_PIXELIZER] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/pixelizer.fs", GLSL_VERSION));
                shaders[FX_CROSS_HATCHING] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/cross_hatching.fs", GLSL_VERSION));
                shaders[FX_CROSS_STITCHING] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/cross_stitching.fs", GLSL_VERSION));
                shaders[FX_PREDATOR_VIEW] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/predator.fs", GLSL_VERSION));
                shaders[FX_SCANLINES] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/scanlines.fs", GLSL_VERSION));
                shaders[FX_FISHEYE] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/fisheye.fs", GLSL_VERSION));
                shaders[FX_SOBEL] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/sobel.fs", GLSL_VERSION));
                shaders[FX_BLOOM] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/bloom.fs", GLSL_VERSION));
                shaders[FX_BLUR] = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/blur.fs", GLSL_VERSION));

                int currentShader = FX_GRAYSCALE;

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

                        if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
                                currentShader++;
                        }
                        else if (rlj.core.IsKeyPressed(KEY_LEFT)) {
                                currentShader--;
                        }

                        if (currentShader >= shaders.length) {
                                currentShader = 0;
                        }
                        else if (currentShader < 0) {
                                currentShader = shaders.length - 1;
                        }
                        //----------------------------------------------------------------------------------

                        // Draw
                        //----------------------------------------------------------------------------------
                        rlj.core.BeginTextureMode(target);       // Enable drawing to texture
                        rlj.core.ClearBackground(RAYWHITE);  // Clear texture background

                        rlj.core.BeginMode3D(camera);        // Begin 3d mode drawing
                        rlj.models.DrawModel(model, position, 0.1f, WHITE);   // Draw 3d model with texture
                        rlj.models.DrawGrid(10, 1.0f);     // Draw a grid
                        rlj.core.EndMode3D();                // End 3d mode drawing, returns to orthographic 2d mode
                        rlj.core.EndTextureMode();               // End drawing to texture (now we have a texture available for next passes)

                        rlj.core.BeginDrawing();
                        rlj.core.ClearBackground(RAYWHITE);  // Clear screen background

                        // Render generated texture using selected postprocessing shader
                        rlj.core.BeginShaderMode(shaders[currentShader]);
                        // NOTE: Render texture must be y-flipped due to default OpenGL coordinates (left-bottom)
                        rlj.textures.DrawTextureRec(target.texture, new Rectangle(0, 0, (float)target.texture.width, (float)-target.texture.height), new Vector2(), WHITE);
                        rlj.core.EndShaderMode();

                        // Draw 2d shapes and text over drawn texture
                        rlj.shapes.DrawRectangle(0, 9, 580, 30, rlj.textures.Fade(LIGHTGRAY, 0.7f));

                        rlj.text.DrawText("(c) Church 3D model by Alberto Cano", screenWidth - 200, screenHeight - 20, 10, GRAY);
                        rlj.text.DrawText("CURRENT POSTPRO SHADER:", 10, 15, 20, BLACK);
                        rlj.text.DrawText(postproShaderText[currentShader], 330, 15, 20, RED);
                        rlj.text.DrawText("< >", 540, 10, 30, DARKBLUE);
                        rlj.text.DrawFPS(700, 15);
                        rlj.core.EndDrawing();
                        //----------------------------------------------------------------------------------
                }

                // De-Initialization
                //--------------------------------------------------------------------------------------
                // Unload all postpro shaders
                for (int i = 0; i < shaders.length; i++) {
                        rlj.core.UnloadShader(shaders[i]);
                }

                rlj.textures.UnloadTexture(texture);         // Unload texture
                rlj.models.UnloadModel(model);             // Unload model
                rlj.textures.UnloadRenderTexture(target);    // Unload render texture

                rlj.core.CloseWindow();                  // Close window and OpenGL context
                //--------------------------------------------------------------------------------------
        }



}
