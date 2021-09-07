package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.camera.Camera;
import com.creedvi.raylib.java.rlj.core.camera.Camera3D;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.raymath.Vector3;
import com.creedvi.raylib.java.rlj.rlgl.shader.Shader;
import com.creedvi.raylib.java.rlj.rlgl.vr.VrDeviceInfo;
import com.creedvi.raylib.java.rlj.rlgl.vr.VrStereoConfig;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.RenderTexture;

import static com.creedvi.raylib.java.rlj.core.camera.Camera.CameraMode.CAMERA_FIRST_PERSON;
import static com.creedvi.raylib.java.rlj.core.camera.Camera.CameraProjection.CAMERA_PERSPECTIVE;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.ShaderUniformDataType.SHADER_UNIFORM_VEC2;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.ShaderUniformDataType.SHADER_UNIFORM_VEC4;

public class VRSimulator{

    /*******************************************************************************************
     *
     *   raylib [core] example - VR Simulator (Oculus Rift CV1 parameters)
     *
     *   This example has been created using raylib 3.7 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Copyright (c) 2017-2021 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        // NOTE: screenWidth/screenHeight should match VR device aspect ratio
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - vr simulator");

        // VR device parameters definition
        VrDeviceInfo device = new VrDeviceInfo();
                // Oculus Rift CV1 parameters for simulator
        device.hResolution = 2160;                 // Horizontal resolution in pixels
        device.vResolution = 1200;                 // Vertical resolution in pixels
        device.hScreenSize = 0.133793f;            // Horizontal size in meters
        device.vScreenSize = 0.0669f;              // Vertical size in meters
        device.vScreenCenter = 0.04678f;           // Screen center in meters
        device.eyeToScreenDistance = 0.041f;       // Distance between eye and display in meters
        device.lensSeparationDistance = 0.07f;     // Lens separation distance in meters
        device.interpupillaryDistance = 0.07f;     // IPD (distance between pupils) in meters

        // NOTE: CV1 uses fresnel-hybrid-asymmetric lenses with specific compute shaders
        // Following parameters are just an approximation to CV1 distortion stereo rendering
        device.lensDistortionValues[0] = 1.0f;     // Lens distortion constant parameter 0
        device.lensDistortionValues[1] = 0.22f;    // Lens distortion constant parameter 1
        device.lensDistortionValues[2] = 0.24f;    // Lens distortion constant parameter 2
        device.lensDistortionValues[3] = 0.0f;     // Lens distortion constant parameter 3
        device.chromaAbCorrection[0] = 0.996f;     // Chromatic aberration correction parameter 0
        device.chromaAbCorrection[1] = -0.004f;    // Chromatic aberration correction parameter 1
        device.chromaAbCorrection[2] = 1.014f;     // Chromatic aberration correction parameter 2
        device.chromaAbCorrection[3] = 0.0f;       // Chromatic aberration correction parameter 3

        // Load VR stereo config for VR device parameteres (Oculus Rift CV1 parameters)
        VrStereoConfig config = rlj.core.LoadVrStereoConfig(device);

        // Distortion shader (uses device lens distortion and chroma)
        Shader distortion = rlj.core.LoadShader(null, "resources/distortion330.fs");

        // Update distortion shader with lens and distortion-scale parameters
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "leftLensCenter"),
                config.leftLensCenter, SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "rightLensCenter"),
                config.rightLensCenter, SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "leftScreenCenter"),
                config.leftScreenCenter, SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "rightScreenCenter"),
                config.rightScreenCenter, SHADER_UNIFORM_VEC2);

        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "scale"),
                config.scale, SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "scaleIn"),
                config.scaleIn, SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "deviceWarpParam"),
                device.lensDistortionValues, SHADER_UNIFORM_VEC4);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "chromaAbParam"),
                device.chromaAbCorrection, SHADER_UNIFORM_VEC4);

        // Initialize framebuffer for stereo rendering
        // NOTE: Screen size should match HMD aspect ratio
        RenderTexture target = rlj.textures.LoadRenderTexture(rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight());

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D();
        camera.position = new Vector3(5.0f, 2.0f, 5.0f);    // Camera position
        camera.target = new Vector3(0.0f, 2.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector
        camera.fovy = 60.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Cam()era type

        Vector3 cubePosition = new Vector3(0.0f, 0.0f, 0.0f);

        Camera.SetCameraMode(camera, CAMERA_FIRST_PERSON);         // Set first person camera mode

        rlj.core.SetTargetFPS(90);                   // Set our game to run at 90 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            Camera.UpdateCamera(camera);          // Update camera (simulator mode)
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginTextureMode(target);
            rlj.core.ClearBackground(Color.RAYWHITE);
            rlj.core.BeginVrStereoMode(config);
            rlj.core.BeginMode3D(camera);

            //TODO: Module MODELS
            // rlj.models.DrawCube(cubePosition, 2.0f, 2.0f, 2.0f, Color.RED);
            // rlj.models.DrawCubeWires(cubePosition, 2.0f, 2.0f, 2.0f, Color.MAROON);
            // rlj.models.DrawGrid(40, 1.0f);

            rlj.core.EndMode3D();
            rlj.core.EndVrStereoMode();
            rlj.core.EndTextureMode();

            rlj.core.BeginShaderMode(distortion);
            rlj.textures.DrawTextureRec(target.texture, new Rectangle(0, 0, (float)target.texture.width,
                            (float)-target.texture.height),
                    new Vector2(0.0f, 0.0f), Color.WHITE);
            rlj.core.EndShaderMode();

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.UnloadVrStereoConfig(config);   // Unload stereo config

        rlj.textures.UnloadRenderTexture(target);    // Unload stereo render fbo
        rlj.core.UnloadShader(distortion);       // Unload distortion shader
        //--------------------------------------------------------------------------------------
    }

}
