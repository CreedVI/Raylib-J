package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Texture2D;

import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.structs.Color.*;

public class GifPlayer {

    private static final int MAX_FRAME_DELAY = 20;
    private static final int MIN_FRAME_DELAY = 1;

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [textures] example -- GIF Player");

        // Load all GIF animation frames into a single Image
        // NOTE: GIF data is always loaded as RGBA (32bit) by default
        // NOTE: Frames are just appended one after another in image.data memory
        Image imScarfyAnim = rlj.textures.LoadImageAnim("src/tests/resources/textures/scarfy_run.gif");
        int frameDataSize = imScarfyAnim.width*imScarfyAnim.height*4;
        int animFrames = imScarfyAnim.data.capacity()/frameDataSize;

        // Load texture from image
        // NOTE: We will update this texture when required with next frame data
        // WARNING: It's not recommended to use this technique for sprites animation,
        // use spritesheets instead, like illustrated in textures_sprite_anim example
        Texture2D texScarfyAnim = rlj.textures.LoadTextureFromImage(imScarfyAnim);

        int nextFrameDataOffset = 0;  // Current byte offset to next frame in image.data

        int currentAnimFrame = 0;       // Current animation frame to load and draw
        int frameDelay = 8;             // Frame delay to switch between animation frames
        int frameCounter = 0;           // General frames counter

        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            // Update
            //----------------------------------------------------------------------------------
            frameCounter++;
            if (frameCounter >= frameDelay) {
                // Move to next frame
                // NOTE: If final frame is reached we return to first frame
                currentAnimFrame++;
                if (currentAnimFrame >= animFrames) {
                    currentAnimFrame = 0;
                }

                // Get memory offset position for next frame data in image.data
                nextFrameDataOffset = frameDataSize*currentAnimFrame;

                byte[] animData = new byte[frameDataSize];
                System.arraycopy(imScarfyAnim.getData(), nextFrameDataOffset, animData, 0, frameDataSize);

                // Update GPU texture data with next frame image data
                // WARNING: Data size (frame size) and pixel format must match already created texture
                rlj.textures.UpdateTexture(texScarfyAnim,animData);

                frameCounter = 0;
            }

            // Control frames delay
            if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
                frameDelay++;
            }
            else if (rlj.core.IsKeyPressed(KEY_LEFT)) {
                frameDelay--;
            }

            if (frameDelay > MAX_FRAME_DELAY) {
                frameDelay = MAX_FRAME_DELAY;
            }
            else if (frameDelay < MIN_FRAME_DELAY) {
                frameDelay = MIN_FRAME_DELAY;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.text.DrawText(rlj.text.TextFormat("TOTAL GIF FRAMES:  %02d", animFrames), 50, 30, 20, LIGHTGRAY);
            rlj.text.DrawText(rlj.text.TextFormat("CURRENT FRAME: %02d", currentAnimFrame), 50, 60, 20, GRAY);
            rlj.text.DrawText(rlj.text.TextFormat("CURRENT FRAME IMAGE.DATA OFFSET: %02d", nextFrameDataOffset), 50, 90, 20, GRAY);

            rlj.text.DrawText("FRAMES DELAY: ", 100, 305, 10, DARKGRAY);
            rlj.text.DrawText(rlj.text.TextFormat("%02d frames", frameDelay), 620, 305, 10, DARKGRAY);
            rlj.text.DrawText("PRESS RIGHT/LEFT KEYS to CHANGE SPEED!", 290, 350, 10, DARKGRAY);

            for (int i = 0; i < MAX_FRAME_DELAY; i++)
            {
                if (i < frameDelay) {
                    rlj.shapes.DrawRectangle(190 + 21*i, 300, 20, 20, RED);
                }
                rlj.shapes.DrawRectangleLines(190 + 21*i, 300, 20, 20, MAROON);
            }

            rlj.textures.DrawTexture(texScarfyAnim, rlj.core.GetScreenWidth()/2 - texScarfyAnim.width/2, 140, WHITE);

            rlj.text.DrawText("(c) Scarfy sprite by Eiden Marsal", SCREEN_WIDTH - 200, SCREEN_HEIGHT - 20, 10, GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        rlj.core.CloseWindow();
    }

}
