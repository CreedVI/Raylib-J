package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.*;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FIRST_PERSON;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;

public class VRSimulator{

    /*******************************************************************************************
     *
     *   raylib-j [core] example - VR Simulator (Oculus Rift CV1 parameters)
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;
        Vector3 cubePosition = new Vector3();

        // NOTE: screenWidth/screenHeight should match VR device aspect ratio
        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - vr simulator");

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
        Shader distortion = rlj.core.LoadShader(null, "main/resources/distortion330.fs");

        // Update distortion shader with lens and distortion-scale parameters
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "leftLensCenter"),
                config.leftLensCenter, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "rightLensCenter"),
                config.rightLensCenter, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "leftScreenCenter"),
                config.leftScreenCenter, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "rightScreenCenter"),
                config.rightScreenCenter, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);

        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "scale"),
                config.scale, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "scaleIn"),
                config.scaleIn, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC2);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "deviceWarpParam"),
                device.lensDistortionValues, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC4);
        rlj.core.SetShaderValue(distortion, rlj.core.GetShaderLocation(distortion, "chromaAbParam"),
                device.chromaAbCorrection, RLGL.rlShaderUniformDataType.SHADER_UNIFORM_VEC4);

        // Initialize framebuffer for stereo rendering
        // NOTE: Screen size should match HMD aspect ratio
        RenderTexture target = rlj.textures.LoadRenderTexture(rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight());

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(5.0f, 2.0f, 5.0f);    // Camera position
        camera.target = new Vector3(0.0f, 2.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector
        camera.fovy = 60.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Cam()era type

        rlj.core.SetTargetFPS(90);                   // Set our game to run at 90 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_FIRST_PERSON);          // Update camera (simulator mode)
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginTextureMode(target);
            rlj.core.ClearBackground(Color.RAYWHITE);
            rlj.core.BeginVrStereoMode(config);
            rlj.core.BeginMode3D(camera);

            rlj.models.DrawCube(cubePosition, 2.0f, 2.0f, 2.0f, Color.RED);
            rlj.models.DrawCubeWires(cubePosition, 2.0f, 2.0f, 2.0f, Color.MAROON);
            rlj.models.DrawGrid(40, 1.0f);

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
