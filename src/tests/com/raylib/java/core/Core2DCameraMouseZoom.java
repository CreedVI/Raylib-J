package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera2D;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_RIGHT;

public class Core2DCameraMouseZoom {

    static Raylib rlj;
    static Camera2D camera;

    final static int SCREEN_WIDTH = 800;
    final static int SCREEN_HEIGHT = 450;

    public static void main(String[] args) {

        rlj = new Raylib(SCREEN_WIDTH, SCREEN_HEIGHT, null);

        camera = new Camera2D();
        camera.zoom = 1.0f;
        camera.target = new Vector2();
        camera.rotation = 0f;
        camera.offset = new Vector2();

        rlj.core.SetTargetFPS(60);

        while (!rlj.core.WindowShouldClose()) {
            Update();
            Render();
        }

    }

    private static void Update() {
        // Translate based on mouse right click
        if (rlj.core.IsMouseButtonDown(MOUSE_BUTTON_RIGHT)) {
            Vector2 delta = rlj.core.GetMouseDelta();
            delta = Raymath.Vector2Scale(delta, -1.0f/camera.zoom);
            System.out.println(delta.x + ", " + delta.y);
            camera.target = Raymath.Vector2Add(camera.target, delta);
        }

        // Zoom based on mouse wheel
        float wheel = rlj.core.GetMouseWheelMove();
        if (wheel != 0) {
            // Get the world point that is under the mouse
            Vector2 mouseWorldPos = rlj.core.GetScreenToWorld2D(rlj.core.GetMousePosition(), camera);

            // Set the offset to where the mouse is
            camera.offset = rlj.core.GetMousePosition();

            // Set the target to match, so that the camera maps the world space point
            // under the cursor to the screen space point under the cursor at any zoom
            camera.target = mouseWorldPos;

            // Zoom increment
            float zoomIncrement = 0.125f;

            camera.zoom += (wheel*zoomIncrement);
            if (camera.zoom < zoomIncrement) {
                camera.zoom = zoomIncrement;
            }
        }

    }

    private static void Render(){
        rlj.core.BeginDrawing();
        rlj.core.ClearBackground(Color.BLACK);

        rlj.core.BeginMode2D(camera);

        // Draw the 3d grid, rotated 90 degrees and centered around 0,0
        // just so we have something in the XY plane
        rlj.rlgl.rlPushMatrix();
        rlj.rlgl.rlTranslatef(0, 25*50, 0);
        rlj.rlgl.rlRotatef(90, 1, 0, 0);
        rlj.models.DrawGrid(100, 50);
        rlj.rlgl.rlPopMatrix();

        // Draw a reference circle
        rlj.shapes.DrawCircle(100, 100, 50, Color.YELLOW);

        rlj.core.EndMode2D();

        rlj.text.DrawText("Mouse right button drag to move, mouse wheel to zoom", 10, 10, 20, Color.WHITE);

        rlj.core.EndDrawing();
    }

}
