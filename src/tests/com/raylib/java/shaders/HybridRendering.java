package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import java.nio.ByteBuffer;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_INFO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachTextureType.RL_ATTACHMENT_TEXTURE2D;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.RL_ATTACHMENT_COLOR_CHANNEL0;
import static com.raylib.java.rlgl.RLGL.rlFramebufferAttachType.RL_ATTACHMENT_DEPTH;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2;
import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC3;
import static com.raylib.java.structs.Color.*;

public class HybridRendering {

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Hybrid Rendering
     *
     *   Example originally created with raylib 4.2, last time updated with raylib 4.2
     *
     *   Example contributed by Buğra Alptekin Sarı (@BugraAlptekinSari) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2022-2023 Buğra Alptekin Sarı (@BugraAlptekinSari)
     *
     ********************************************************************************************/

    private static final int GLSL_VERSION = 330;
    // private static final int GLSL_VERSION = 110;

    private static class RayLocs {
        public int camPos;
        public int camDir;
        public int screenCenter;
    }

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [shaders] example - write depth buffer");

        // This Shader calculates pixel depth and color using raymarch
        Shader shdrRaymarch = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/hybrid_raymarch.fs", GLSL_VERSION));

        // This Shader is a standard rasterization fragment shader with the addition of depth writing
        // You are required to write depth for all shaders if one shader does it
        Shader shdrRaster = rlj.core.LoadShader(null, rlj.text.TextFormat("src/tests/resources/shaders/shaders/glsl%d/hybrid_raster.fs", GLSL_VERSION));

        // Declare Struct used to store camera locs.
        RayLocs marchLocs = new RayLocs();

        // Fill the struct with shader locs.
        marchLocs.camPos = rlj.core.GetShaderLocation(shdrRaymarch, "camPos");
        marchLocs.camDir = rlj.core.GetShaderLocation(shdrRaymarch, "camDir");
        marchLocs.screenCenter = rlj.core.GetShaderLocation(shdrRaymarch, "screenCenter");

        // Transfer screenCenter position to shader. Which is used to calculate ray direction.
        Vector2 screenCenter = new Vector2(screenWidth/2.0, screenHeight/2.0);
        rlj.core.SetShaderValue(shdrRaymarch, marchLocs.screenCenter, new float[]{screenCenter.x, screenCenter.y}, RL_SHADER_UNIFORM_VEC2);

        // Use Customized function to create writable depth texture buffer
        RenderTexture target = LoadRenderTextureDepthTex(rlj, screenWidth, screenHeight);

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.5f, 1.0f, 1.5f);    // Camera position
        camera.target = new Vector3(0.0f, 0.5f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;              // Camera projection type


        // Camera FOV is pre-calculated in the camera Distance.
        double camDist = 1.0/(Math.tan(camera.fovy*0.5*DEG2RAD));

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);

            // Update Camera Postion in the ray march shader.
            rlj.core.SetShaderValue(shdrRaymarch, marchLocs.camPos, new float[]{camera.position.x, camera.position.y, camera.position.z}, RL_SHADER_UNIFORM_VEC3);

            // Update Camera Looking Vector. Vector length determines FOV.
            Vector3 camDir = Vector3Scale(Vector3Normalize( Vector3Subtract(camera.target, camera.position)) , (float) camDist);
            rlj.core.SetShaderValue(shdrRaymarch, marchLocs.camDir, new float[]{camDir.x, camDir.y, camDir.z}, RL_SHADER_UNIFORM_VEC3);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            // Draw into our custom render texture (framebuffer)
            rlj.core.BeginTextureMode(target);
            rlj.core.ClearBackground(WHITE);

            // Raymarch Scene
            rlj.rlgl.rlEnableDepthTest(); //Manually enable Depth Test to handle multiple rendering methods.
            rlj.core.BeginShaderMode(shdrRaymarch);
            rlj.shapes.DrawRectangleRec(new Rectangle(0, 0, screenWidth, screenHeight),WHITE);
            rlj.core.EndShaderMode();

            // Raserize Scene
            rlj.core.BeginMode3D(camera);
            rlj.core.BeginShaderMode(shdrRaster);
            rlj.models.DrawCubeWiresV(new Vector3(0.0f, 0.5f, 1.0f ), new Vector3(1.0f, 1.0f, 1.0f ), RED);
            rlj.models.DrawCubeV(new Vector3(0.0f, 0.5f, 1.0f ), new Vector3(1.0f, 1.0f, 1.0f ), PURPLE);
            rlj.models.DrawCubeWiresV(new Vector3(0.0f, 0.5f, -1.0f ), new Vector3(1.0f, 1.0f, 1.0f ), DARKGREEN);
            rlj.models.DrawCubeV(new Vector3(0.0f, 0.5f, -1.0f ), new Vector3(1.0f, 1.0f, 1.0f ), YELLOW);
            rlj.models.DrawGrid(10, 1.0f);
            rlj.core.EndShaderMode();
            rlj.core.EndMode3D();
            rlj.core.EndTextureMode();

            // Draw into screen our custom render texture
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(RAYWHITE);

            rlj.textures.DrawTextureRec(target.texture, new Rectangle(0, 0, screenWidth, -screenHeight ), new Vector2(0, 0 ), WHITE);
            rlj.text.DrawFPS(10, 10);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        UnloadRenderTextureDepthTex(rlj, target);
        rlj.core.UnloadShader(shdrRaymarch);
        rlj.core.UnloadShader(shdrRaster);

        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

    //------------------------------------------------------------------------------------
    // Define custom functions required for the example
    //------------------------------------------------------------------------------------
    // Load custom render texture, create a writable depth texture buffer
    private static RenderTexture LoadRenderTextureDepthTex(Raylib context, int width, int height) {
        RenderTexture target = new RenderTexture();

        target.id = context.rlgl.rlLoadFramebuffer(width, height);   // Load an empty framebuffer

        if (target.id > 0) {
            context.rlgl.rlEnableFramebuffer(target.id);

            // Create color texture (default to RGBA)
            target.texture.id = context.rlgl.rlLoadTexture(ByteBuffer.allocateDirect(width * height), width, height, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);
            target.texture.width = width;
            target.texture.height = height;
            target.texture.format = RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
            target.texture.mipmaps = 1;

            // Create depth texture buffer (instead of raylib default renderbuffer)
            target.depth.id = context.rlgl.rlLoadTextureDepth(width, height, false);
            target.depth.width = width;
            target.depth.height = height;
            target.depth.format = RL_PIXELFORMAT_COMPRESSED_PVRT_RGBA;       //DEPTH_COMPONENT_24BIT?
            target.depth.mipmaps = 1;

            // Attach color texture and depth texture to FBO
            context.rlgl.rlFramebufferAttach(target.id, target.texture.id, RL_ATTACHMENT_COLOR_CHANNEL0, RL_ATTACHMENT_TEXTURE2D);
            context.rlgl.rlFramebufferAttach(target.id, target.depth.id, RL_ATTACHMENT_DEPTH, RL_ATTACHMENT_TEXTURE2D);

            // Check if fbo is complete with attachments (valid)
            if (context.rlgl.rlFramebufferComplete(target.id)) {
                context.traceLog.TRACELOG(LOG_INFO, "FBO: [ID %d] Framebuffer object created successfully", target.id);
            }

            context.rlgl.rlDisableFramebuffer();
        }
        else {
            context.traceLog.TRACELOG(LOG_WARNING, "FBO: Framebuffer object can not be created");
        }

        return target;
    }

    // Unload render texture from GPU memory (VRAM)
    private static void UnloadRenderTextureDepthTex(Raylib context, RenderTexture target) {
        if (target.id > 0) {
            // Color texture attached to FBO is deleted
            context.rlgl.rlUnloadTexture(target.texture.id);
            context.rlgl.rlUnloadTexture(target.depth.id);

            // NOTE: Depth texture is automatically
            // queried and deleted before deleting framebuffer
            context.rlgl.rlUnloadFramebuffer(target.id);
        }
    }

}
