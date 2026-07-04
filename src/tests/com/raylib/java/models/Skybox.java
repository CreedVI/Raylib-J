package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.*;

import static com.raylib.java.Config.RL_CULL_DISTANCE_FAR;
import static com.raylib.java.Config.RL_CULL_DISTANCE_NEAR;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_INFO;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_CUBEMAP;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.RL_ATTACHMENT_CUBEMAP_POSITIVE_X;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.RL_ATTACHMENT_RENDERBUFFER;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL0;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.RL_ATTACHMENT_DEPTH;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.RL_SHADER_LOC_MATRIX_PROJECTION;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.RL_SHADER_LOC_MATRIX_VIEW;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_INT;
import static com.raylib.java.structs.Color.*;
import static com.raylib.java.textures.rTextures.CubemapLayoutType.CUBEMAP_AUTO_DETECT;

public class Skybox {

    /*******************************************************************************************
     *
     *   raylib [models] example - Skybox loading and drawing
     *
     *   Example originally created with raylib 1.8, last time updated with raylib 4.0
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2017-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static Raylib rlj;

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

        rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - skybox loading and drawing");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(1.0f, 1.0f, 1.0f);    // Camera position
        camera.target = new Vector3(4.0f, 1.0f, 4.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        // Load skybox model
        Mesh cube = rlj.models.GenMeshCube(1.0f, 1.0f, 1.0f);
        Model skybox = rlj.models.LoadModelFromMesh(cube);

        boolean useHDR = true;

        // Load skybox shader and set required locations
        // NOTE: Some locations are automatically set at shader loading
        skybox.materials[0].shader = rlj.core.LoadShader(
                rlj.text.TextFormat("src/tests/resources/models/shaders/glsl%d/skybox.vs", GLSL_VERSION),
                rlj.text.TextFormat("src/tests/resources/models/shaders/glsl%d/skybox.fs", GLSL_VERSION)
        );

        rlj.core.SetShaderValue(skybox.materials[0].shader, rlj.core.GetShaderLocation(skybox.materials[0].shader, "environmentMap"), new float[]{ MATERIAL_MAP_CUBEMAP }, RL_SHADER_UNIFORM_INT);
        rlj.core.SetShaderValue(skybox.materials[0].shader, rlj.core.GetShaderLocation(skybox.materials[0].shader, "doGamma"), new float[] { useHDR ? 1 : 0 }, RL_SHADER_UNIFORM_INT);
        rlj.core.SetShaderValue(skybox.materials[0].shader, rlj.core.GetShaderLocation(skybox.materials[0].shader, "vflipped"), new float[]{ useHDR ? 1 : 0 }, RL_SHADER_UNIFORM_INT);

        // Load cubemap shader and setup required shader locations
        Shader shdrCubemap = rlj.core.LoadShader(
                rlj.text.TextFormat("src/tests/resources/models/shaders/glsl%d/cubemap.vs", GLSL_VERSION),
                rlj.text.TextFormat("src/tests/resources/models/shaders/glsl%d/cubemap.fs", GLSL_VERSION)
        );

        rlj.core.SetShaderValue(shdrCubemap, rlj.core.GetShaderLocation(shdrCubemap, "equirectangularMap"), new float[]{ 0 }, RL_SHADER_UNIFORM_INT);

        String skyboxFileName = new String();

        Texture2D panorama;

        if (useHDR) {
            skyboxFileName = "src/tests/resources/models/dresden_square_2k.hdr";

            // Load HDR panorama (sphere) texture
            panorama = rlj.textures.LoadTexture(skyboxFileName);

            // Generate cubemap (texture with 6 quads-cube-mapping) from panorama HDR texture
            // NOTE 1: New texture is generated rendering to texture, shader calculates the sphere->cube coordinates mapping
            // NOTE 2: It seems on some Android devices WebGL, fbo does not properly support a FLOAT-based attachment,
            // despite texture can be successfully created.. so using PIXELFORMAT_UNCOMPRESSED_R8G8B8A8 instead of PIXELFORMAT_UNCOMPRESSED_R32G32B32A32
            skybox.materials[0].maps[MATERIAL_MAP_CUBEMAP].texture = GenTextureCubemap(shdrCubemap, panorama, 1024, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);

            //UnloadTexture(panorama);    // Texture not required anymore, cubemap already generated
        }
        else {
            Image img = rlj.textures.LoadImage("src/tests/resources/models/skybox.png");
            skybox.materials[0].maps[MATERIAL_MAP_CUBEMAP].texture = rlj.textures.LoadTextureCubemap(img, CUBEMAP_AUTO_DETECT);    // CUBEMAP_LAYOUT_PANORAMA
            rlj.textures.UnloadImage(img);
        }

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);

            // Load new cubemap texture on drag&drop
            if (rlj.core.IsFileDropped()) {
                FilePathList droppedFiles = rlj.core.LoadDroppedFiles();

                if (droppedFiles.count == 1) {         // Only support one file dropped
                    if (rlj.core.IsFileExtension(droppedFiles.paths[0], ".png;.jpg;.hdr;.bmp;.tga")) {
                        // Unload current cubemap texture and load new one
                        rlj.textures.UnloadTexture(skybox.materials[0].maps[MATERIAL_MAP_CUBEMAP].texture);
                        if (useHDR) {
                            panorama = rlj.textures.LoadTexture(droppedFiles.paths[0]);

                            // Generate cubemap from panorama texture
                            skybox.materials[0].maps[MATERIAL_MAP_CUBEMAP].texture = GenTextureCubemap(shdrCubemap, panorama, 1024, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
                            rlj.textures.UnloadTexture(panorama);
                        }
                        else {
                            Image img = rlj.textures.LoadImage(droppedFiles.paths[0]);
                            skybox.materials[0].maps[MATERIAL_MAP_CUBEMAP].texture = rlj.textures.LoadTextureCubemap(img, CUBEMAP_AUTO_DETECT);
                            rlj.textures.UnloadImage(img);
                        }

                        skyboxFileName = droppedFiles.paths[0];
                    }
                }

                rlj.core.UnloadDroppedFiles(droppedFiles);    // Unload filepaths from memory
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // We are inside the cube, we need to disable backface culling!
            rlj.rlgl.rlDisableBackfaceCulling();
            rlj.rlgl.rlDisableDepthMask();
            rlj.models.DrawModel(skybox, new Vector3(), 1.0f, WHITE);
            rlj.rlgl.rlEnableBackfaceCulling();
            rlj.rlgl.rlEnableDepthMask();

            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            //DrawTextureEx(panorama, (Vector2){ 0, 0 }, 0.0f, 0.5f, WHITE);

            if (useHDR) {
                rlj.text.DrawText(rlj.text.TextFormat("Panorama image from hdrihaven.com: %s", rlj.core.GetFileName(skyboxFileName)), 10, rlj.core.GetScreenHeight() - 20, 10, BLACK);
            }
            else {
                rlj.text.DrawText(rlj.text.TextFormat(": %s", rlj.core.GetFileName(skyboxFileName)), 10, rlj.core.GetScreenHeight() - 20, 10, BLACK);
            }

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadShader(skybox.materials[0].shader);
        rlj.textures.UnloadTexture(skybox.materials[0].maps[MATERIAL_MAP_CUBEMAP].texture);

        rlj.models.UnloadModel(skybox);        // Unload skybox model

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

    // Generate cubemap texture from HDR texture
    private static Texture2D GenTextureCubemap(Shader shader, Texture2D panorama, int size, RLGL.rlPixelFormat format) {
        Texture2D cubemap = new Texture2D();

        rlj.rlgl.rlDisableBackfaceCulling();     // Disable backface culling to render inside the cube

        // STEP 1: Setup framebuffer
        //------------------------------------------------------------------------------------------
        int rbo = rlj.rlgl.rlLoadTextureDepth(size, size, true);
        cubemap.id = rlj.rlgl.rlLoadTextureCubemap(null, size, format);

        int fbo = rlj.rlgl.rlLoadFramebuffer(size, size);
        rlj.rlgl.rlFramebufferAttach(fbo, rbo, RL_ATTACHMENT_DEPTH, RL_ATTACHMENT_RENDERBUFFER);
        rlj.rlgl.rlFramebufferAttach(fbo, cubemap.id, RL_ATTACHMENT_COLOR_CHANNEL0, RL_ATTACHMENT_CUBEMAP_POSITIVE_X);

        // Check if framebuffer is complete with attachments (valid)
        if (rlj.rlgl.rlFramebufferComplete(fbo)) {
            rlj.traceLog.TRACELOG(LOG_INFO, "FBO: [ID %d] Framebuffer object created successfully", fbo);
        }
        //------------------------------------------------------------------------------------------

        // STEP 2: Draw to framebuffer
        //------------------------------------------------------------------------------------------
        // NOTE: Shader is used to convert HDR equirectangular environment map to cubemap equivalent (6 faces)
        rlj.rlgl.rlEnableShader(shader.id);

        // Define projection matrix and send it to shader
        Matrix matFboProjection = MatrixPerspective(90.0*DEG2RAD, 1.0, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        rlj.rlgl.rlSetUniformMatrix(shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION.GetLocation()], matFboProjection);

        // Define view matrix for every side of the cubemap
        Matrix fboViews[] = {
            MatrixLookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(  1.0f,  0.0f,  0.0f), new Vector3(0.0f, -1.0f,  0.0f )),
            MatrixLookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3( -1.0f,  0.0f,  0.0f), new Vector3(0.0f, -1.0f,  0.0f )),
            MatrixLookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(  0.0f,  1.0f,  0.0f), new Vector3(0.0f,  0.0f,  1.0f )),
            MatrixLookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(  0.0f, -1.0f,  0.0f), new Vector3(0.0f,  0.0f, -1.0f )),
            MatrixLookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(  0.0f,  0.0f,  1.0f), new Vector3(0.0f, -1.0f,  0.0f )),
            MatrixLookAt(new Vector3(0.0f, 0.0f, 0.0f), new Vector3(  0.0f,  0.0f, -1.0f), new Vector3(0.0f, -1.0f,  0.0f ))
        };

        rlj.rlgl.rlViewport(0, 0, size, size);   // Set viewport to current fbo dimensions

        // Activate and enable texture for drawing to cubemap faces
        rlj.rlgl.rlActiveTextureSlot(0);
        rlj.rlgl.rlEnableTexture(panorama.id);

        for (int i = 0; i < 6; i++) {
            // Set the view matrix for the current cube face
            rlj.rlgl.rlSetUniformMatrix(shader.locs[RL_SHADER_LOC_MATRIX_VIEW.GetLocation()], fboViews[i]);

            // Select the current cubemap face attachment for the fbo
            // WARNING: This function by default enables->attach->disables fbo!!!
            rlj.rlgl.rlFramebufferAttach(fbo, cubemap.id, RL_ATTACHMENT_COLOR_CHANNEL0, RLGL.rlFramebufferAttachTextureType.values()[i]);
            rlj.rlgl.rlEnableFramebuffer(fbo);

            // Load and draw a cube, it uses the current enabled texture
            rlj.rlgl.rlClearScreenBuffers();
            rlj.rlgl.rlLoadDrawCube();

            // ALTERNATIVE: Try to use internal batch system to draw the cube instead of rlLoadDrawCube
            // for some reason this method does not work, maybe due to cube triangles definition? normals pointing out?
            // TODO: Investigate this issue...
            //rlSetTexture(panorama.id); // WARNING: It must be called after enabling current framebuffer if using internal batch system!
            //rlClearScreenBuffers();
            //DrawCubeV(Vector3Zero(), Vector3One(), WHITE);
            //rlDrawRenderBatchActive();
        }
        //------------------------------------------------------------------------------------------

        // STEP 3: Unload framebuffer and reset state
        //------------------------------------------------------------------------------------------
        rlj.rlgl.rlDisableShader();          // Unbind shader
        rlj.rlgl.rlDisableTexture();         // Unbind texture
        rlj.rlgl.rlDisableFramebuffer();     // Unbind framebuffer
        rlj.rlgl.rlUnloadFramebuffer(fbo);   // Unload framebuffer (and automatically attached depth texture/renderbuffer)

        // Reset viewport dimensions to default
        rlj.rlgl.rlViewport(0, 0, rlj.rlgl.rlGetFramebufferWidth(), rlj.rlgl.rlGetFramebufferHeight());
        rlj.rlgl.rlEnableBackfaceCulling();
        //------------------------------------------------------------------------------------------

        cubemap.width = size;
        cubemap.height = size;
        cubemap.mipmaps = 1;
        cubemap.format = format;

        return cubemap;
    }

}
