package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.*;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;

public class QuatConversion {

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Quat conversion
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2020-2021 Chris Camacho (@chriscamacho)
     *
     ********************************************************************************************/

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - quat conversions");

        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(0.0f, 10.0f, 10.0f);  // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                   // Camera mode type

        Mesh mesh = rlj.models.GenMeshCylinder(0.2f, 1.0f, 32);
        Model model = rlj.models.LoadModelFromMesh(mesh);

        // Some required variables
        Quaternion q1 = new Quaternion();

        Matrix m1 = new Matrix(),
                m2 = new Matrix(),
                m3 = new Matrix(),
                m4 = new Matrix();

        Vector3 v1 = new Vector3(),
                v2 = new Vector3();

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {    // Detect window close button or ESC key
            // Update
            //--------------------------------------------------------------------------------------
            if (!rlj.core.IsKeyDown(KEY_SPACE)) {
                v1.x += 0.01f;
                v1.y += 0.03f;
                v1.z += 0.05f;
            }

            if (v1.x > Math.PI*2) v1.x -= Math.PI*2;
            if (v1.y > Math.PI*2) v1.y -= Math.PI*2;
            if (v1.z > Math.PI*2) v1.z -= Math.PI*2;

            q1 = Raymath.QuaternionFromEuler(v1.x, v1.y, v1.z);
            m1 = Raymath.MatrixRotateZYX(v1);
            m2 = Raymath.QuaternionToMatrix(q1);

            q1 = Raymath.QuaternionFromMatrix(m1);
            m3 = Raymath.QuaternionToMatrix(q1);

            v2 = Raymath.QuaternionToEuler(q1);
            v2.x *= DEG2RAD;
            v2.y *= DEG2RAD;
            v2.z *= DEG2RAD;

            m4 = Raymath.MatrixRotateZYX(v2);
            //--------------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);


            model.transform = m1;
            rlj.models.DrawModel(model, new Vector3(-1, 0, 0), 1.0f, Color.RED);
            model.transform = m2;
            rlj.models.DrawModel(model, new Vector3(1, 0, 0), 1.0f, Color.RED);
            model.transform = m3;
            rlj.models.DrawModel(model, new Vector3(0, 0, 0 ), 1.0f, Color.RED);
            model.transform = m4;
            rlj.models.DrawModel(model, new Vector3(0, 0, -1), 1.0f, Color.RED);

            rlj.models.DrawGrid(10, 1.0f);

            rlj.core.EndMode3D();

            if (v2.x < 0) v2.x += Math.PI*2;
            if (v2.y < 0) v2.y += Math.PI*2;
            if (v2.z < 0) v2.z += Math.PI*2;

            Color cx,cy,cz;
            cx = cy = cz = Color.BLACK;
            if (v1.x == v2.x) cx = Color.GREEN;
            if (v1.y == v2.y) cy = Color.GREEN;
            if (v1.z == v2.z) cz = Color.GREEN;

            rlj.text.DrawText(String.valueOf(v1.x), 20, 20, 20, cx);
            rlj.text.DrawText(String.valueOf(v1.y), 20, 40, 20, cy);
            rlj.text.DrawText(String.valueOf(v1.z), 20, 60, 20, cz);

            rlj.text.DrawText(String.valueOf(v2.x), 200, 20, 20, cx);
            rlj.text.DrawText(String.valueOf(v2.y), 200, 40, 20, cy);
            rlj.text.DrawText(String.valueOf(v2.z), 200, 60, 20, cz);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

}
