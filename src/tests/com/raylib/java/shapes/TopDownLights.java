package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.structs.*;

import static com.raylib.java.core.input.Keyboard.KEY_F1;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_RIGHT;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlBlendMode.BLEND_ALPHA;
import static com.raylib.java.rlgl.RLGL.rlBlendMode.BLEND_CUSTOM;
import static com.raylib.java.structs.Color.*;

public class TopDownLights {

    /*******************************************************************************************
     *
     *   raylib [shapes] example - top down lights
     *
     *   Example originally created with raylib 4.2, last time updated with raylib 4.2
     *
     *   Example contributed by Vlad Adrian (@demizdor) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2022-2023 Jeffery Myers (@JeffM2501)
     *
     ********************************************************************************************/


    // Custom Blend Modes
    static final int RLGL_SRC_ALPHA =  0x0302;
    static final int RLGL_MIN =  0x8007;
    static final int RLGL_MAX =  0x8008;

    static final int MAX_BOXES = 20;
    static final int MAX_SHADOWS = MAX_BOXES*3;         // MAX_BOXES *3. Each box can cast up to two shadow volumes for the edges it is away from, and one for the box itself
    static final int MAX_LIGHTS = 16;

    // Shadow geometry type
    static class ShadowGeometry {
        Vector2[] vertices;

        public ShadowGeometry() {
            vertices = new Vector2[4];
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = new Vector2();
            }
        }
    }

    // Light info type
    static class LightInfo {
        boolean active;                // Is this light slot active?
        boolean dirty;                 // Does this light need to be updated?
        boolean valid;                 // Is this light in a valid position?

        Vector2 position;           // Light position
        RenderTexture mask;         // Alpha mask for the light
        float outerRadius;          // The distance the light touches
        Rectangle bounds;           // A cached rectangle of the light bounds to help with culling

        ShadowGeometry[] shadows;
        int shadowCount;

        public LightInfo() {
            position = new Vector2();
            mask = new RenderTexture();
            bounds = new Rectangle();
            shadows = new ShadowGeometry[MAX_SHADOWS];
            for (int i = 0; i < MAX_SHADOWS; i++) {
                shadows[i] = new ShadowGeometry();
            }
            shadowCount = shadows.length;
        }
    }

    static LightInfo[] lights;
    static Raylib rlj;

    // Move a light and mark it as dirty so that we update it's mask next frame
    static void MoveLight(int slot, float x, float y) {
        lights[slot].dirty = true;
        lights[slot].position.x = x;
        lights[slot].position.y = y;

        // update the cached bounds
        lights[slot].bounds.x = x - lights[slot].outerRadius;
        lights[slot].bounds.y = y - lights[slot].outerRadius;
    }

    // Compute a shadow volume for the edge
    // It takes the edge and projects it back by the light radius and turns it into a quad
    static void ComputeShadowVolumeForEdge(int slot, Vector2 sp, Vector2 ep) {
        if (lights[slot].shadowCount >= MAX_SHADOWS) {
            return;
        }

        float extension = lights[slot].outerRadius*2;

        Vector2 spVector = Vector2Normalize(Vector2Subtract(sp, lights[slot].position));
        Vector2 spProjection = Vector2Add(sp, Vector2Scale(spVector, extension));

        Vector2 epVector = Vector2Normalize(Vector2Subtract(ep, lights[slot].position));
        Vector2 epProjection = Vector2Add(ep, Vector2Scale(epVector, extension));

        lights[slot].shadows[lights[slot].shadowCount].vertices[0] = sp;
        lights[slot].shadows[lights[slot].shadowCount].vertices[1] = ep;
        lights[slot].shadows[lights[slot].shadowCount].vertices[2] = epProjection;
        lights[slot].shadows[lights[slot].shadowCount].vertices[3] = spProjection;

        lights[slot].shadowCount++;
    }

    // Draw the light and shadows to the mask for a light
    static void DrawLightMask(int slot) {
        // Use the light mask
        rlj.core.BeginTextureMode(lights[slot].mask);

        rlj.core.ClearBackground(WHITE);

        // Force the blend mode to only set the alpha of the destination
        rlj.rlgl.rlSetBlendFactors(RLGL_SRC_ALPHA, RLGL_SRC_ALPHA, RLGL_MIN);
        rlj.rlgl.rlSetBlendMode(BLEND_CUSTOM);

        // If we are valid, then draw the light radius to the alpha mask
        if (lights[slot].valid) {
            rlj.shapes.DrawCircleGradient((int)lights[slot].position.x, (int)lights[slot].position.y, lights[slot].outerRadius, rlj.textures.ColorAlpha(WHITE, 0), WHITE);
        }

        rlj.rlgl.rlDrawRenderBatchActive();

        // Cut out the shadows from the light radius by forcing the alpha to maximum
        rlj.rlgl.rlSetBlendMode(BLEND_ALPHA);
        rlj.rlgl.rlSetBlendFactors(RLGL_SRC_ALPHA, RLGL_SRC_ALPHA, RLGL_MAX);
        rlj.rlgl.rlSetBlendMode(BLEND_CUSTOM);

        // Draw the shadows to the alpha mask
        for (int i = 0; i < lights[slot].shadowCount; i++) {
            rlj.shapes.DrawTriangleFan(lights[slot].shadows[i].vertices, 4, WHITE);
        }

        rlj.rlgl.rlDrawRenderBatchActive();

        // Go back to normal blend mode
        rlj.rlgl.rlSetBlendMode(BLEND_ALPHA);

        rlj.core.EndTextureMode();
    }

    // Setup a light
    static void SetupLight(int slot, float x, float y, float radius) {
        lights[slot].active = true;
        lights[slot].valid = false;  // The light must prove it is valid
        lights[slot].mask = rlj.textures.LoadRenderTexture(rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight());
        lights[slot].outerRadius = radius;

        lights[slot].bounds.width = radius * 2;
        lights[slot].bounds.height = radius * 2;

        MoveLight(slot, x, y);

        // Force the render texture to have something in it
        DrawLightMask(slot);
    }

    // See if a light needs to update it's mask
    static boolean UpdateLight(int slot, Rectangle[] boxes, int count) {
        if (!lights[slot].active || !lights[slot].dirty) {
            return false;
        }

        lights[slot].dirty = false;
        lights[slot].shadowCount = 0;
        lights[slot].valid = false;

        for (int i = 0; i < count; i++) {
            // Are we in a box? if so we are not valid
            if (rlj.shapes.CheckCollisionPointRec(lights[slot].position, boxes[i])) {
                return false;
            }

            // If this box is outside our bounds, we can skip it
            if (!rlj.shapes.CheckCollisionRecs(lights[slot].bounds, boxes[i])) {
                continue;
            }

            // Check the edges that are on the same side we are, and cast shadow volumes out from them

            // Top
            Vector2 sp = new Vector2(boxes[i].x, boxes[i].y);
            Vector2 ep = new Vector2(boxes[i].x + boxes[i].width, boxes[i].y);

            if (lights[slot].position.y > ep.y) ComputeShadowVolumeForEdge(slot, sp, ep);

            // Right
            sp = ep;
            ep.y += boxes[i].height;
            if (lights[slot].position.x < ep.x) ComputeShadowVolumeForEdge(slot, sp, ep);

            // Bottom
            sp = ep;
            ep.x -= boxes[i].width;
            if (lights[slot].position.y < ep.y) ComputeShadowVolumeForEdge(slot, sp, ep);

            // Left
            sp = ep;
            ep.y -= boxes[i].height;
            if (lights[slot].position.x > ep.x) ComputeShadowVolumeForEdge(slot, sp, ep);

            // The box itself
            lights[slot].shadows[lights[slot].shadowCount].vertices[0] = new Vector2(boxes[i].x, boxes[i].y);
            lights[slot].shadows[lights[slot].shadowCount].vertices[1] = new Vector2(boxes[i].x, boxes[i].y + boxes[i].height);
            lights[slot].shadows[lights[slot].shadowCount].vertices[2] = new Vector2(boxes[i].x + boxes[i].width, boxes[i].y + boxes[i].height);
            lights[slot].shadows[lights[slot].shadowCount].vertices[3] = new Vector2(boxes[i].x + boxes[i].width, boxes[i].y);
            lights[slot].shadowCount++;
        }

        lights[slot].valid = true;

        DrawLightMask(slot);

        return true;
    }

    // Set up some boxes
    static Rectangle[] SetupBoxes() {
        Rectangle[] boxes = new Rectangle[MAX_BOXES];

        boxes[0] = new Rectangle(150,80, 40, 40);
        boxes[1] = new Rectangle(1200, 700, 40, 40);
        boxes[2] = new Rectangle(200, 600, 40, 40);
        boxes[3] = new Rectangle(1000, 50, 40, 40);
        boxes[4] = new Rectangle(500, 350, 40, 40);

        for (int i = 5; i < MAX_BOXES; i++) {
            boxes[i] = new Rectangle(
                    (float)rlj.core.GetRandomValue(0,rlj.core.GetScreenWidth()),
                    (float)rlj.core.GetRandomValue(0,rlj.core.GetScreenHeight()),
                    (float)rlj.core.GetRandomValue(10,100),
                    (float)rlj.core.GetRandomValue(10,100)
            );
        }

        return boxes;
    }

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib();
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [shapes] example - top down lights");

        // Initialize our 'world' of boxes
        int boxCount = 0;
        Rectangle[] boxes = SetupBoxes();

        // Create a checkerboard ground texture
        Image img = rlj.textures.GenImageChecked(64, 64, 32, 32, DARKBROWN, DARKGRAY);
        Texture2D backgroundTexture = rlj.textures.LoadTextureFromImage(img);
        rlj.textures.UnloadImage(img);

        // Create a global light mask to hold all the blended lights
        RenderTexture lightMask = rlj.textures.LoadRenderTexture(rlj.core.GetScreenWidth(), rlj.core.GetScreenHeight());

        // Setup initial light
        lights = new LightInfo[MAX_LIGHTS];
        for (int i = 0; i < MAX_LIGHTS; i++) {
            lights[i] = new LightInfo();
        }
        SetupLight(0, 600, 400, 300);
        int nextLight = 1;

        boolean showLines = false;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Drag light 0
            if (rlj.core.IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
                MoveLight(0, rlj.core.GetMousePosition().x, rlj.core.GetMousePosition().y);
            }

            // Make a new light
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_RIGHT) && (nextLight < MAX_LIGHTS)) {
                SetupLight(nextLight, rlj.core.GetMousePosition().x, rlj.core.GetMousePosition().y, 200);
                nextLight++;
            }

            // Toggle debug info
            if (rlj.core.IsKeyPressed(KEY_F1)) {
                showLines = !showLines;
            }

            // Update the lights and keep track if any were dirty so we know if we need to update the master light mask
            boolean dirtyLights = false;
            for (int i = 0; i < MAX_LIGHTS; i++) {
                if (UpdateLight(i, boxes, boxCount)) dirtyLights = true;
            }

            // Update the light mask
            if (dirtyLights) {
                // Build up the light mask
                rlj.core.BeginTextureMode(lightMask);

                rlj.core.ClearBackground(BLACK);

                // Force the blend mode to only set the alpha of the destination
                rlj.rlgl.rlSetBlendFactors(RLGL_SRC_ALPHA, RLGL_SRC_ALPHA, RLGL_MIN);
                rlj.rlgl.rlSetBlendMode(BLEND_CUSTOM);

                // Merge in all the light masks
                for (int i = 0; i < MAX_LIGHTS; i++) {
                    if (lights[i].active) {
                        rlj.textures.DrawTextureRec(lights[i].mask.texture, new Rectangle(0, 0, (float)rlj.core.GetScreenWidth(), -(float)rlj.core.GetScreenHeight()), Vector2Zero(), WHITE);
                    }
                }

                rlj.rlgl.rlDrawRenderBatchActive();

                // Go back to normal blend
                rlj.rlgl.rlSetBlendMode(BLEND_ALPHA);
                rlj.core.EndTextureMode();
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(BLACK);

            // Draw the tile background
            rlj.textures.DrawTextureRec(backgroundTexture, new Rectangle(0, 0, (float)rlj.core.GetScreenWidth(), -(float)rlj.core.GetScreenHeight()), Vector2Zero(), WHITE);

            // Overlay the shadows from all the lights
            rlj.textures.DrawTextureRec(lightMask.texture, new Rectangle(0, 0, (float)rlj.core.GetScreenWidth(), -(float)rlj.core.GetScreenHeight()), Vector2Zero(), rlj.textures.ColorAlpha(WHITE, showLines? 0.75f : 1.0f));

            // Draw the lights
            for (int i = 0; i < MAX_LIGHTS; i++) {
                if (lights[i].active) {
                    rlj.shapes.DrawCircle((int)lights[i].position.x, (int)lights[i].position.y, 10, (i == 0)? YELLOW : WHITE);
                }
            }

            if (showLines) {
                for (int s = 0; s < lights[0].shadowCount; s++) {
                    rlj.shapes.DrawTriangleFan(lights[0].shadows[s].vertices, 4, DARKPURPLE);
                }

                for (int b = 0; b < boxCount; b++) {
                    if (rlj.shapes.CheckCollisionRecs(boxes[b],lights[0].bounds)) {
                        rlj.shapes.DrawRectangleRec(boxes[b], PURPLE);
                    }

                    rlj.shapes.DrawRectangleLines((int)boxes[b].x, (int)boxes[b].y, (int)boxes[b].width, (int)boxes[b].height, DARKBLUE);
                }

                rlj.text.DrawText("(F1) Hide Shadow Volumes", 10, 50, 10, GREEN);
            }
            else {
                rlj.text.DrawText("(F1) Show Shadow Volumes", 10, 50, 10, GREEN);
            }

            rlj.text.DrawFPS(screenWidth - 80, 10);
            rlj.text.DrawText("Drag to move light #1", 10, 10, 10, DARKGREEN);
            rlj.text.DrawText("Right click to add new light", 10, 30, 10, DARKGREEN);
            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(backgroundTexture);
        rlj.textures.UnloadRenderTexture(lightMask);
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (lights[i].active) {
                rlj.textures.UnloadRenderTexture(lights[i].mask);
            }
        }

        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

}
