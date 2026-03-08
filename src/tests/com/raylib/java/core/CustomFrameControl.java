package com.raylib.java.core;

import com.raylib.java.Raylib;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.structs.Color.*;

public class CustomFrameControl {

    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- Custom Frame Control");

        // Custom timming variables
        double previousTime = rlj.core.GetTime();    // Previous time measure
        double currentTime = 0.0;           // Current time measure
        double updateDrawTime = 0.0;        // Update + Draw time
        double waitTime = 0.0;              // Wait time (if target fps required)
        float deltaTime = 0.0f;             // Frame time (Update + Draw + Wait time)

        float timeCounter = 0.0f;           // Accumulative time counter (seconds)
        float position = 0.0f;              // Circle position
        boolean pause = false;                 // Pause control flag

        int targetFPS = 60;                 // Our initial target fps
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {        // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            rlj.core.PollInputEvents();              // Poll input events (SUPPORT_CUSTOM_FRAME_CONTROL)

            if (rlj.core.IsKeyPressed(KEY_SPACE)) {
                pause = !pause;
            }

            if (rlj.core.IsKeyPressed(KEY_UP)) {
                targetFPS += 20;
            }
            else if (rlj.core.IsKeyPressed(KEY_DOWN)) {
                targetFPS -= 20;
            }

            if (targetFPS < 0) {
                targetFPS = 0;
            }

            if (!pause) {
                position += 200*deltaTime;  // We move at 200 pixels per second
                if (position >= rlj.core.GetScreenWidth()) {
                    position = 0;
                }
                timeCounter += deltaTime;   // We count time (seconds)
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            for (int i = 0; i < rlj.core.GetScreenWidth()/200; i++) {
                rlj.shapes.DrawRectangle(200*i, 0, 1, rlj.core.GetScreenHeight(), SKYBLUE);
            }

            rlj.shapes.DrawCircle((int)position, rlj.core.GetScreenHeight()/2 - 25, 50, RED);

            rlj.text.DrawText(rlj.text.TextFormat("%03.0f ms", timeCounter*1000.0f), (int)position - 40, rlj.core.GetScreenHeight()/2 - 100, 20, MAROON);
            rlj.text.DrawText(rlj.text.TextFormat("PosX: %03.0f", position), (int)position - 50, rlj.core.GetScreenHeight()/2 + 40, 20, BLACK);

            rlj.text.DrawText("Circle is moving at a constant 200 pixels/sec,\nindependently of the frame rate.", 10, 10, 20, DARKGRAY);
            rlj.text.DrawText("PRESS SPACE to PAUSE MOVEMENT", 10, rlj.core.GetScreenHeight() - 60, 20, GRAY);
            rlj.text.DrawText("PRESS UP | DOWN to CHANGE TARGET FPS", 10, rlj.core.GetScreenHeight() - 30, 20, GRAY);
            rlj.text.DrawText(rlj.text.TextFormat("TARGET FPS: %d", targetFPS), rlj.core.GetScreenWidth() - 220, 10, 20, LIME);
            rlj.text.DrawText(rlj.text.TextFormat("CURRENT FPS: %d", (int)(1.0f/deltaTime)), rlj.core.GetScreenWidth() - 220, 40, 20, GREEN);

            rlj.core.EndDrawing();

            // NOTE: In case raylib is configured to SUPPORT_CUSTOM_FRAME_CONTROL,
            // Events polling, screen buffer swap and frame time control must be managed by the user

            rlj.core.SwapScreenBuffer();         // Flip the back buffer to screen (front buffer)

            currentTime = rlj.core.GetTime();
            updateDrawTime = currentTime - previousTime;

            if (targetFPS > 0) {          // We want a fixed frame rate
                waitTime = (1.0f/(float)targetFPS) - updateDrawTime;

                if (waitTime > 0.0) {
                    rlj.core.WaitTime((float)waitTime);
                    currentTime = rlj.core.GetTime();
                    deltaTime = (float)(currentTime - previousTime);
                }
            }
            else {
                deltaTime = (float)updateDrawTime;    // Framerate could be variable
            }

            previousTime = currentTime;
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------

    }

}
