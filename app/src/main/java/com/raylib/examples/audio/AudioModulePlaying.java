package com.raylib.examples.audio;

import com.raylib.audio.*;
import com.raylib.*;
import com.raylib.core.Color;
import com.raylib.core.input.Keyboard;
import com.raylib.raymath.Vector2;

class AudioModulePlaying {
    private final int MAX_CIRCLES = 64;
    int screenWidth = 800;
    int screenHeight = 450;
    Raylib raylib = new Raylib();

    Color[] colors = { Color.ORANGE, Color.RED, Color.LIME, Color.BLUE, Color.VIOLET, Color.BROWN, Color.LIGHTGRAY,
            Color.PINK, Color.YELLOW, Color.GREEN, Color.SKYBLUE, Color.PURPLE, Color.BEIGE };

    CircleWave[] circles = new CircleWave[MAX_CIRCLES];

    public static void main(String[] args) {
        AudioModulePlaying audio = new AudioModulePlaying();

    }

    public AudioModulePlaying() {
        // SetConfigFlags(FLAG_MSAA_4X_HINT);
        raylib.core.InitWindow(screenWidth, screenHeight, "raylib [audio] example - module playing (streaming)");
        raylib.audio.InitAudioDevice();
        for (int i = MAX_CIRCLES - 1; i >= 0; i--) {
            circles[i].alpha = 0.0f;
            circles[i].radius = (float) raylib.core.GetRandomValue(10, 40);
            circles[i].position.x = (float) raylib.core.GetRandomValue((int) circles[i].radius,
                    (int) (screenWidth - circles[i].radius));
            circles[i].position.y = (float) raylib.core.GetRandomValue((int) circles[i].radius,
                    (int) (screenHeight - circles[i].radius));
            circles[i].speed = (float) raylib.core.GetRandomValue(1, 100) / 2000.0f;
            circles[i].color = colors[raylib.core.GetRandomValue(0, 13)];
        }

        Music music = raylib.audio.LoadMusicStream("resources/mini1111.xm");

        music.looping = false;
        float pitch = 1.0f;

        raylib.audio.PlayMusicStream(music);

        float timePlayed = 0.0f;
        boolean pause = false;

        raylib.core.SetTargetFPS(60);

        while (!raylib.core.WindowShouldClose()) {
            raylib.audio.UpdateMusicStream(music);

            if (raylib.core.IsKeyPressed(Keyboard.KEY_SPACE)) {
                raylib.audio.StopMusicStream(music);
                raylib.audio.PlayMusicStream(music);
            }

            if (raylib.core.IsKeyPressed(Keyboard.KEY_P)) {
                pause = !pause;

                if (pause) {
                    raylib.audio.PauseMusicStream(music);
                } else {
                    raylib.audio.ResumeMusicStream(music);
                }
            }

            raylib.audio.SetMusicPitch(music, pitch);

            timePlayed = raylib.audio.GetMusicTimePlayed(music) / raylib.audio.GetMusicTimeLength(music)
                    * (screenWidth - 40);

            // Color circles animation
            for (int i = MAX_CIRCLES - 1; (i >= 0) && !pause; i--) {
                circles[i].alpha += circles[i].speed;
                circles[i].radius += circles[i].speed * 10.0f;

                if (circles[i].alpha > 1.0f)
                    circles[i].speed *= -1;

                if (circles[i].alpha <= 0.0f) {
                    circles[i].alpha = 0.0f;
                    circles[i].radius = (float) raylib.core.GetRandomValue(10, 40);
                    circles[i].position.x = (float) raylib.core.GetRandomValue((int) circles[i].radius,
                            (int) (screenWidth - circles[i].radius));
                    circles[i].position.y = (float) raylib.core.GetRandomValue((int) circles[i].radius,
                            (int) (screenHeight - circles[i].radius));
                    circles[i].color = colors[raylib.core.GetRandomValue(0, 13)];
                    circles[i].speed = (float) raylib.core.GetRandomValue(1, 100) / 2000.0f;
                }
            }

            // ----------------------------------------------------------------------------------

            // Draw
            // ----------------------------------------------------------------------------------
            raylib.core.BeginDrawing();

            raylib.core.ClearBackground(Color.RAYWHITE);

            for (int i = MAX_CIRCLES - 1; i >= 0; i--) {
                raylib.shapes.DrawCircleV(circles[i].position, circles[i].radius,
                        raylib.textures.Fade(circles[i].color, circles[i].alpha));
            }

            // Draw time bar
            raylib.shapes.DrawRectangle(20, screenHeight - 20 - 12, screenWidth - 40, 12, Color.LIGHTGRAY);
            raylib.shapes.DrawRectangle(20, screenHeight - 20 - 12, (int) timePlayed, 12, Color.MAROON);
            raylib.shapes.DrawRectangleLines(20, screenHeight - 20 - 12, screenWidth - 40, 12, Color.GRAY);

            raylib.core.EndDrawing();
            // ----------------------------------------------------------------------------------
        }
        // De-Initialization
        // --------------------------------------------------------------------------------------
        raylib.audio.UnloadMusicStream(music); // Unload music stream buffers from RAM

        raylib.audio.CloseAudioDevice(); // Close audio device (music streaming is automatically stopped)

        raylib.core.CloseWindow(); // Close window and OpenGL context
        // --------------------------------------------------------------------------------------

    }

    class CircleWave {
        Vector2 position;
        float radius;
        float alpha;
        float speed;
        Color color;
    }
}
