package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.*;

import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_RIGHT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FREE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;

public class MeshPicking {

    /*******************************************************************************************
     *
     *   raylib [models] example - Mesh picking in 3d mode, ground plane, triangle, mesh
     *
     *   This example has been created using raylib 1.7 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Joel Davis (@joeld42) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2017 Joel Davis (@joeld42) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    static Raylib rlj;

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib(screenWidth, screenHeight, "raylib [models] example - mesh picking");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(20.0f, 20.0f, 20.0f); // Camera position
        camera.target = new Vector3(0.0f, 8.0f, 0.0f);      // Camera looking at point
        camera.up = new Vector3(0.0f, 1.6f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;             // Camera projection type

        Ray ray = new Ray();        // Picking ray

        Model tower = rlj.models.LoadModel("src/tests/resources/models/models/obj/turret.obj");                 // Load OBJ model
        Texture2D texture = rlj.textures.LoadTexture("src/tests/resources/models/models/obj/turret_diffuse.png"); // Load model texture
        tower.materials[0].maps[MATERIAL_MAP_DIFFUSE.GetIndex()].texture = texture;            // Set model diffuse texture

        Vector3 towerPos = new Vector3();                        // Set model position
        BoundingBox towerBBox = rlj.models.GetMeshBoundingBox(tower.meshes[0]);    // Get mesh bounding box

        // Ground quad
        Vector3 g0 = new Vector3(-50.0f, 0.0f, -50.0f);
        Vector3 g1 = new Vector3(-50.0f, 0.0f,  50.0f);
        Vector3 g2 = new Vector3( 50.0f, 0.0f,  50.0f);
        Vector3 g3 = new Vector3( 50.0f, 0.0f, -50.0f);

        // Test triangle
        Vector3 ta = new Vector3(-25.0f, 0.5f, 0.0f);
        Vector3 tb = new Vector3(-4.0f, 2.5f, 1.0f);
        Vector3 tc = new Vector3(-8.0f, 6.5f, 0.0f);

        Vector3 bary = new Vector3(0.0f, 0.0f, 0.0f);

        // Test sphere
        Vector3 sp = new Vector3(-30.0f, 5.0f, 5.0f);
        float sr = 4.0f;

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------
        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsCursorHidden()) {
                camera.Update(CAMERA_FREE); // Update camera
            }

            // Toggle camera controls
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_RIGHT))
            {
                if (rlj.core.IsCursorHidden()) {
                    rlj.core.EnableCursor();
                }
                else {
                    rlj.core.DisableCursor();
                }
            }

            // Display information about closest hit
            RayCollision collision = new RayCollision();
            String hitObjectName = "None";
            collision.distance = Float.MAX_VALUE;
            collision.hit = false;
            Color cursorColor = Color.WHITE;

            // Get ray and test against objects
            ray = rlj.core.GetMouseRay(rlj.core.GetMousePosition(), camera);

            // Check ray collision against ground quad
            RayCollision groundHitInfo = rlj.models.GetRayCollisionQuad(ray, g0, g1, g2, g3);

            if ((groundHitInfo.hit) && (groundHitInfo.distance < collision.distance))
            {
                collision = groundHitInfo;
                cursorColor = Color.GREEN;
                hitObjectName = "Ground";
            }

            // Check ray collision against test triangle
            RayCollision triHitInfo = rlj.models.GetRayCollisionTriangle(ray, ta, tb, tc);

            if ((triHitInfo.hit) && (triHitInfo.distance < collision.distance))
            {
                collision = triHitInfo;
                cursorColor = Color.PURPLE;
                hitObjectName = "Triangle";

                bary = Raymath.Vector3Barycenter(collision.point, ta, tb, tc);
            }

            // Check ray collision against test sphere
            RayCollision sphereHitInfo = rlj.models.GetRayCollisionSphere(ray, sp, sr);

            if ((sphereHitInfo.hit) && (sphereHitInfo.distance < collision.distance))
            {
                collision = sphereHitInfo;
                cursorColor = Color.ORANGE;
                hitObjectName = "Sphere";
            }

            // Check ray collision against bounding box first, before trying the full ray-mesh test
            RayCollision boxHitInfo = rlj.models.GetRayCollisionBox(ray, towerBBox);

            if ((boxHitInfo.hit) && (boxHitInfo.distance < collision.distance))
            {
                collision = boxHitInfo;
                cursorColor = Color.ORANGE;
                hitObjectName = "Box";

                // Check ray collision against model meshes
                RayCollision meshHitInfo = new RayCollision();
                for (int m = 0; m < tower.meshCount; m++)
                {
                    // NOTE: We consider the model.transform for the collision check but
                    // it can be checked against any transform Matrix, used when checking against same
                    // model drawn multiple times with multiple transforms
                    meshHitInfo = rlj.models.GetRayCollisionMesh(ray, tower.meshes[m], tower.transform);
                    if (meshHitInfo.hit)
                    {
                        // Save the closest hit mesh
                        if ((!collision.hit) || (collision.distance > meshHitInfo.distance)) collision = meshHitInfo;

                        break;  // Stop once one mesh collision is detected, the colliding mesh is m
                    }
                }

                if (meshHitInfo.hit)
                {
                    collision = meshHitInfo;
                    cursorColor = Color.ORANGE;
                    hitObjectName = "Mesh";
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.core.BeginMode3D(camera);

            // Draw the tower
            // WARNING: If scale is different than 1.0f,
            // not considered by GetRayCollisionModel()
            rlj.models.DrawModel(tower, towerPos, 1.0f, Color.WHITE);

            // Draw the test triangle
            rlj.models.DrawLine3D(ta, tb, Color.PURPLE);
            rlj.models.DrawLine3D(tb, tc, Color.PURPLE);
            rlj.models.DrawLine3D(tc, ta, Color.PURPLE);

            // Draw the test sphere
            rlj.models.DrawSphereWires(sp, sr, 8, 8, Color.PURPLE);

            // Draw the mesh bbox if we hit it
            if (boxHitInfo.hit) {
                rlj.models.DrawBoundingBox(towerBBox, Color.LIME);
            }

            // If we hit something, draw the cursor at the hit point
            if (collision.hit) {
                rlj.models.DrawCube(collision.point, 0.3f, 0.3f, 0.3f, cursorColor);
                rlj.models.DrawCubeWires(collision.point, 0.3f, 0.3f, 0.3f, Color.RED);

                Vector3 normalEnd = new Vector3();
                normalEnd.x = collision.point.x + collision.normal.x;
                normalEnd.y = collision.point.y + collision.normal.y;
                normalEnd.z = collision.point.z + collision.normal.z;

                rlj.models.DrawLine3D(collision.point, normalEnd, Color.RED);
            }

            rlj.models.DrawRay(ray, Color.MAROON);

            rlj.models.DrawGrid(10, 10.0f);

            rlj.core.EndMode3D();

            // Draw some debug GUI text
            rlj.text.DrawText(rlj.text.TextFormat("Hit Object: %s", hitObjectName), 10, 50, 10, Color.BLACK);

            if (collision.hit) {
                int ypos = 70;

                rlj.text.DrawText(rlj.text.TextFormat("Distance: %3.2f", collision.distance), 10, ypos, 10, Color.BLACK);

                rlj.text.DrawText("Hit Pos: " +
                                collision.point.x + ", " +
                                collision.point.y + ", " +
                                collision.point.z, 10, ypos + 15, 10, Color.BLACK);

                rlj.text.DrawText("Hit Norm: " +
                                collision.normal.x + ", " +
                                collision.normal.y + ", " +
                                collision.normal.z, 10, ypos + 30, 10, Color.BLACK);

                if (triHitInfo.hit && rlj.text.TextIsEqual(hitObjectName, "Triangle"))
                    rlj.text.DrawText("Barycenter:" +
                            bary.x + ", " +
                            bary.y + ", " +
                            bary.z, 10, ypos + 45, 10, Color.BLACK);
            }

            rlj.text.DrawText("Right click mouse to toggle camera controls", 10, 430, 10, Color.GRAY);

            rlj.text.DrawText("(c) Turret 3D model by Alberto Cano", screenWidth - 200, screenHeight - 20, 10, Color.GRAY);

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.models.UnloadModel(tower);
        rlj.textures.UnloadTexture(texture);

        rlj.core.CloseWindow();
    }

}
