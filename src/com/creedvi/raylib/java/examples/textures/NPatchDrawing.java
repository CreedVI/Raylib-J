package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.NPatchInfo;
import com.creedvi.raylib.java.rlj.textures.NPatchInfo.NPatchType;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

public class NPatchDrawing{

    /*******************************************************************************************
     *
     *   raylib [textures] example - N-patch drawing
     *
     *   NOTE: Images are loaded in CPU memory (RAM); textures are loaded in GPU memory (VRAM)
     *
     *   This example has been created using raylib 2.0 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Jorge A. Gomes (@overdev) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2018 Jorge A. Gomes (@overdev) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - N-patch drawing");

        // NOTE: Textures MUST be loaded after Window initialization (OpenGL context is required)
        Texture2D nPatchTexture = rlj.textures.LoadTexture("resources/ninepatch_button.png");

        Vector2 mousePosition = new Vector2();
        Vector2 origin = new Vector2();

        // Position and size of the n-patches
        Rectangle dstRec1 = new Rectangle(480.0f, 160.0f, 32.0f, 32.0f);
        Rectangle dstRec2 = new Rectangle(160.0f, 160.0f, 32.0f, 32.0f);
        Rectangle dstRecH = new Rectangle( 160.0f, 93.0f, 32.0f, 32.0f);
        Rectangle dstRecV = new Rectangle( 92.0f, 160.0f, 32.0f, 32.0f);

        // A 9-patch (NPATCH_NINE_PATCH) changes its sizes in both axis
        NPatchInfo ninePatchInfo1 = new NPatchInfo(new Rectangle( 0.0f, 0.0f, 64.0f, 64.0f ), 12, 40, 12, 12,
                NPatchType.NPATCH_NINE_PATCH);
        NPatchInfo ninePatchInfo2 = new NPatchInfo(new Rectangle(0.0f, 128.0f, 64.0f, 64.0f ), 16, 16, 16, 16,
                NPatchType.NPATCH_NINE_PATCH);

        // A horizontal 3-patch (NPATCH_THREE_PATCH_HORIZONTAL) changes its sizes along the x axis only
        NPatchInfo h3PatchInfo = new NPatchInfo(new Rectangle( 0.0f,  64.0f, 64.0f, 64.0f ), 8, 8, 8, 8,
                NPatchType.NPATCH_THREE_PATCH_HORIZONTAL);

        // A vertical 3-patch (NPATCH_THREE_PATCH_VERTICAL) changes its sizes along the y axis only
        NPatchInfo v3PatchInfo = new NPatchInfo(new Rectangle(0.0f, 192.0f, 64.0f, 64.0f), 6, 6, 6, 6,
                NPatchType.NPATCH_THREE_PATCH_VERTICAL);

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
            if (dstRec1.width < 1.0f) dstRec1.width = 1.0f;
            if (dstRec1.width > 300.0f) dstRec1.width = 300.0f;
            if (dstRec1.height < 1.0f) dstRec1.height = 1.0f;
            if (dstRec2.width < 1.0f) dstRec2.width = 1.0f;
            if (dstRec2.width > 300.0f) dstRec2.width = 300.0f;
            if (dstRec2.height < 1.0f) dstRec2.height = 1.0f;
            if (dstRecH.width < 1.0f) dstRecH.width = 1.0f;
            if (dstRecV.height < 1.0f) dstRecV.height = 1.0f;
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
        //--------------------------------------------------------------------------------------
    }

}
