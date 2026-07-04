package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.rlgl.RLGL.RL_TRIANGLES;
import static com.raylib.java.structs.Color.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class RLGL_SolarSystem {

    /*******************************************************************************************
     *
     *   raylib [models] example - rlgl module usage with push/pop matrix transformations
     *
     *   NOTE: This example uses [rlgl] module functionality (pseudo-OpenGL 1.1 style coding)
     *
     *   Example originally created with raylib 2.5, last time updated with raylib 4.0
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2018-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static Raylib rlj;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        float sunRadius = 4.0f;
        float earthRadius = 0.6f;
        float earthOrbitRadius = 8.0f;
        float moonRadius = 0.16f;
        float moonOrbitRadius = 1.5f;

        rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - rlgl module usage with push/pop matrix transformations");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(16.0f, 16.0f, 16.0f); // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        float rotationSpeed = 0.2f;         // General system rotation speed

        float earthRotation = 0.0f;         // Rotation of earth around itself (days) in degrees
        float earthOrbitRotation = 0.0f;    // Rotation of earth around the Sun (years) in degrees
        float moonRotation = 0.0f;          // Rotation of moon around itself
        float moonOrbitRotation = 0.0f;     // Rotation of moon around earth in degrees

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(CAMERA_ORBITAL);

            earthRotation += (5.0f*rotationSpeed);
            earthOrbitRotation += (365/360.0f*(5.0f*rotationSpeed)*rotationSpeed);
            moonRotation += (2.0f*rotationSpeed);
            moonOrbitRotation += (8.0f*rotationSpeed);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);

            rlj.rlgl.rlPushMatrix();
            rlj.rlgl.rlScalef(sunRadius, sunRadius, sunRadius);          // Scale Sun
            DrawSphereBasic(GOLD);                              // Draw the Sun
            rlj.rlgl.rlPopMatrix();

            rlj.rlgl.rlPushMatrix();
            rlj.rlgl.rlRotatef(earthOrbitRotation, 0.0f, 1.0f, 0.0f);    // Rotation for Earth orbit around Sun
            rlj.rlgl.rlTranslatef(earthOrbitRadius, 0.0f, 0.0f);         // Translation for Earth orbit

            rlj.rlgl.rlPushMatrix();
            rlj.rlgl.rlRotatef(earthRotation, 0.25f, 1.0f, 0.0f);       // Rotation for Earth itself
            rlj.rlgl.rlScalef(earthRadius, earthRadius, earthRadius);// Scale Earth

            DrawSphereBasic(BLUE);                          // Draw the Earth
            rlj.rlgl.rlPopMatrix();

            rlj.rlgl.rlRotatef(moonOrbitRotation, 0.0f, 1.0f, 0.0f);     // Rotation for Moon orbit around Earth
            rlj.rlgl.rlTranslatef(moonOrbitRadius, 0.0f, 0.0f);          // Translation for Moon orbit
            rlj.rlgl.rlRotatef(moonRotation, 0.0f, 1.0f, 0.0f);          // Rotation for Moon itself
            rlj.rlgl.rlScalef(moonRadius, moonRadius, moonRadius);       // Scale Moon

            DrawSphereBasic(LIGHTGRAY);                         // Draw the Moon
            rlj.rlgl.rlPopMatrix();

            // Some reference elements (not affected by previous matrix transformations)
            rlj.models.DrawCircle3D(new Vector3(), earthOrbitRadius, new Vector3(1, 0, 0), 90.0f, rlj.textures.Fade(RED, 0.5f));
            rlj.models.DrawGrid(20, 1.0f);

            rlj.core.EndMode3D();

            rlj.text.DrawText("EARTH ORBITING AROUND THE SUN!", 400, 10, 20, MAROON);
            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

    //--------------------------------------------------------------------------------------------
    // Module Functions Definitions (local)
    //--------------------------------------------------------------------------------------------

    // Draw sphere without any matrix transformation
    // NOTE: Sphere is drawn in world position ( 0, 0, 0 ) with radius 1.0f
    private static void DrawSphereBasic(Color color) {
        int rings = 16;
        int slices = 16;

        // Make sure there is enough space in the internal render batch
        // buffer to store all required vertex, batch is reseted if required
        rlj.rlgl.rlCheckRenderBatchLimit((rings + 2)*slices*6);

        rlj.rlgl.rlBegin(RL_TRIANGLES);
        rlj.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        for (int i = 0; i < (rings + 2); i++) {
            for (int j = 0; j < slices; j++) {
                rlj.rlgl.rlVertex3f((float) (cos(DEG2RAD*(270+(180/(rings + 1))*i))*sin(DEG2RAD*(j*360/slices))),
                                    (float) sin(DEG2RAD*(270+(180/(rings + 1))*i)),
                                    (float) (cos(DEG2RAD*(270+(180/(rings + 1))*i))*cos(DEG2RAD*(j*360/slices))));
                rlj.rlgl.rlVertex3f((float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i+1)))*sin(DEG2RAD*((j+1)*360/slices))),
                                    (float) sin(DEG2RAD*(270+(180/(rings + 1))*(i+1))),
                                    (float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i+1)))*cos(DEG2RAD*((j+1)*360/slices))));
                rlj.rlgl.rlVertex3f((float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i+1)))*sin(DEG2RAD*(j*360/slices))),
                                    (float) sin(DEG2RAD*(270+(180/(rings + 1))*(i+1))),
                                    (float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i+1)))*cos(DEG2RAD*(j*360/slices))));

                rlj.rlgl.rlVertex3f((float) (cos(DEG2RAD*(270+(180/(rings + 1))*i))*sin(DEG2RAD*(j*360/slices))),
                                    (float) sin(DEG2RAD*(270+(180/(rings + 1))*i)),
                                    (float) (cos(DEG2RAD*(270+(180/(rings + 1))*i))*cos(DEG2RAD*(j*360/slices))));
                rlj.rlgl.rlVertex3f((float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i)))*sin(DEG2RAD*((j+1)*360/slices))),
                                    (float) sin(DEG2RAD*(270+(180/(rings + 1))*(i))),
                                    (float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i)))*cos(DEG2RAD*((j+1)*360/slices))));
                rlj.rlgl.rlVertex3f((float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i+1)))*sin(DEG2RAD*((j+1)*360/slices))),
                                    (float) sin(DEG2RAD*(270+(180/(rings + 1))*(i+1))),
                                    (float) (cos(DEG2RAD*(270+(180/(rings + 1))*(i+1)))*cos(DEG2RAD*((j+1)*360/slices))));
            }
        }
        rlj.rlgl.rlEnd();
    }
}