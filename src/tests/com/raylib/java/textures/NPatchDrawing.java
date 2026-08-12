package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.*;

public class NPatchDrawing {

    /*******************************************************************************************
     *
     *   raylib-j [textures] example - N-patch drawing
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2018 Jorge A. Gomes (@overdev)
     *
     ********************************************************************************************/

    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [textures] example - N-patch drawing");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Texture2D nPatchTexture = rlj.textures.LoadTexture("src/tests/resources/textures/ninepatch_button.png");

        Vector2 mousePosition;
        Vector2 origin = new Vector2();

        // Position and size of the n-patches
        Rectangle dstRec1 = new Rectangle(480.0f, 160.0f, 32.0f, 32.0f);
        Rectangle dstRec2 = new Rectangle(160.0f, 160.0f, 32.0f, 32.0f);
        Rectangle dstRecH = new Rectangle(160.0f, 93.0f, 32.0f, 32.0f);
        Rectangle dstRecV = new Rectangle(92.0f, 160.0f, 32.0f, 32.0f);

        // A 9-patch (NPATCH_NINE_PATCH) changes its sizes in both axis
        NPatchInfo ninePatchInfo1 = new NPatchInfo(new Rectangle(0.0f, 0.0f, 64.0f, 64.0f), 12, 40, 12, 12, NPatchInfo.NPatchType.NPATCH_NINE_PATCH);
        NPatchInfo ninePatchInfo2 = new NPatchInfo(new Rectangle(0.0f, 128.0f, 64.0f, 64.0f), 16, 16, 16, 16, NPatchInfo.NPatchType.NPATCH_NINE_PATCH);

        // A horizontal 3-patch (NPATCH_THREE_PATCH_HORIZONTAL) changes its sizes along the x axis only
        NPatchInfo h3PatchInfo = new NPatchInfo(new Rectangle(0.0f, 64.0f, 64.0f, 64.0f), 8, 8, 8, 8, NPatchInfo.NPatchType.NPATCH_THREE_PATCH_HORIZONTAL);

        // A vertical 3-patch (NPATCH_THREE_PATCH_VERTICAL) changes its sizes along the y axis only
        NPatchInfo v3PatchInfo = new NPatchInfo(new Rectangle(0.0f, 192.0f, 64.0f, 64.0f), 6, 6, 6, 6, NPatchInfo.NPatchType.NPATCH_THREE_PATCH_VERTICAL);

        rlj.core.SetTargetFPS(60);
        //---------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            mousePosition = rlj.core.GetMousePosition();

            // Resize the n-patches based on mouse position
            dstRec1.width = mousePosition.x - dstRec1.x;
            dstRec1.height = mousePosition.y - dstRec1.y;
            dstRec2.width = mousePosition.x - dstRec2.x;
            dstRec2.height = mousePosition.y - dstRec2.y;
            dstRecH.width = mousePosition.x - dstRecH.x;
            dstRecV.height = mousePosition.y - dstRecV.y;

            // Set a minimum width and/or height
            if (dstRec1.width < 1.0f) {
                dstRec1.width = 1.0f;
            }
            if (dstRec1.width > 300.0f) {
                dstRec1.width = 300.0f;
            }
            if (dstRec1.height < 1.0f) {
                dstRec1.height = 1.0f;
            }
            if (dstRec2.width < 1.0f) {
                dstRec2.width = 1.0f;
            }
            if (dstRec2.width > 300.0f) {
                dstRec2.width = 300.0f;
            }
            if (dstRec2.height < 1.0f) {
                dstRec2.height = 1.0f;
            }
            if (dstRecH.width < 1.0f) {
                dstRecH.width = 1.0f;
            }
            if (dstRecV.height < 1.0f) {
                dstRecV.height = 1.0f;
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            // Draw the n-patches
            rlj.textures.DrawTextureNPatch(nPatchTexture, ninePatchInfo2, dstRec2, origin, 0.0f, Color.WHITE);
            rlj.textures.DrawTextureNPatch(nPatchTexture, ninePatchInfo1, dstRec1, origin, 0.0f, Color.WHITE);
            rlj.textures.DrawTextureNPatch(nPatchTexture, h3PatchInfo, dstRecH, origin, 0.0f, Color.WHITE);
            rlj.textures.DrawTextureNPatch(nPatchTexture, v3PatchInfo, dstRecV, origin, 0.0f, Color.WHITE);

            // Draw the source texture
            rlj.shapes.DrawRectangleLines(5, 88, 74, 266, Color.BLUE);
            rlj.textures.DrawTexture(nPatchTexture, 10, 93, Color.WHITE);
            rlj.text.DrawText("TEXTURE", 15, 360, 10, Color.DARKGRAY);

            rlj.text.DrawText("Move the mouse to stretch or shrink the n-patches", 10, 20, 20, Color.DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(nPatchTexture);       // Texture unloading
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

}
