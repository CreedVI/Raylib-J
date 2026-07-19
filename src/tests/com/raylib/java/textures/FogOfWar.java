package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.RenderTexture;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.rlgl.RLGL.rlTextureFilterMode.TEXTURE_FILTER_BILINEAR;
import static com.raylib.java.structs.Color.*;

public class FogOfWar {

    /*******************************************************************************************
     *
     *   raylib [textures] example - Fog of war
     *
     *   Example originally created with raylib 4.2, last time updated with raylib 4.2
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2018-2023 Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    private static final int MAP_TILE_SIZE = 32;         // Tiles size 32x32 pixels
    private static final int PLAYER_SIZE = 16;         // Player size
    private static final int PLAYER_TILE_VISIBILITY = 2;   // Player can see 2 tiles around its position

    // Map data type
    static class Map {
        public int tilesX;            // Number of tiles in X axis
        public int tilesY;            // Number of tiles in Y axis
        public byte[] tileIds;         // Tile ids (tilesX*tilesY), defines type of tile to draw
        public byte[] tileFog;         // Tile fog state (tilesX*tilesY), defines if a tile has fog or half-fog
    }

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - fog of war");

        Map map = new Map();
        map.tilesX = 25;
        map.tilesY = 15;

        // NOTE: We can have up to 256 values for tile ids and for tile fog state,
        // probably we don't need that many values for fog state, it can be optimized
        // to use only 2 bits per fog state (reducing size by 4) but logic will be a bit more complex
        map.tileIds = new byte[map.tilesX * map.tilesY];
        map.tileFog = new byte[map.tilesX * map.tilesY];

        // Load map tiles (generating 2 random tile ids for testing)
        // NOTE: Map tile ids should be probably loaded from an external map file
        for (int i = 0; i < map.tilesY * map.tilesX; i++) {
            map.tileIds[i] = (byte) rlj.core.GetRandomValue(0, 1);
        }

        // Player position on the screen (pixel coordinates, not tile coordinates)
        Vector2 playerPosition = new Vector2(180, 130);
        int playerTileX = 0;
        int playerTileY = 0;

        // Render texture to render fog of war
        // NOTE: To get an automatic smooth-fog effect we use a render texture to render fog
        // at a smaller size (one pixel per tile) and scale it on drawing with bilinear filtering
        RenderTexture fogOfWar = rlj.textures.LoadRenderTexture(map.tilesX, map.tilesY);
        rlj.textures.SetTextureFilter(fogOfWar.texture, TEXTURE_FILTER_BILINEAR);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Move player around
            if (rlj.core.IsKeyDown(KEY_RIGHT)) {
                playerPosition.x += 5;
            }
            if (rlj.core.IsKeyDown(KEY_LEFT)) {
                playerPosition.x -= 5;
            }
            if (rlj.core.IsKeyDown(KEY_DOWN)) {
                playerPosition.y += 5;
            }
            if (rlj.core.IsKeyDown(KEY_UP)) {
                playerPosition.y -= 5;
            }

            // Check player position to avoid moving outside tilemap limits
            if (playerPosition.x < 0) {
                playerPosition.x = 0;
            }
            else if ((playerPosition.x + PLAYER_SIZE) > (map.tilesX * MAP_TILE_SIZE)) {
                playerPosition.x = (float) map.tilesX * MAP_TILE_SIZE - PLAYER_SIZE;
            }

            if (playerPosition.y < 0) {
                playerPosition.y = 0;
            }
            else if ((playerPosition.y + PLAYER_SIZE) > (map.tilesY * MAP_TILE_SIZE)) {
                playerPosition.y = (float) map.tilesY * MAP_TILE_SIZE - PLAYER_SIZE;
            }

            // Previous visited tiles are set to partial fog
            for (int i = 0; i < map.tilesX * map.tilesY; i++){
                if (map.tileFog[i] == 1) {
                    map.tileFog[i] = 2;
                }
            }

            // Get current tile position from player pixel position
            playerTileX = (int) ((playerPosition.x + MAP_TILE_SIZE / 2) / MAP_TILE_SIZE);
            playerTileY = (int) ((playerPosition.y + MAP_TILE_SIZE / 2) / MAP_TILE_SIZE);

            // Check visibility and update fog
            // NOTE: We check tilemap limits to avoid processing tiles out-of-array-bounds (it could crash program)
            for (int y = (playerTileY - PLAYER_TILE_VISIBILITY); y < (playerTileY + PLAYER_TILE_VISIBILITY); y++) {
                for (int x = (playerTileX - PLAYER_TILE_VISIBILITY); x < (playerTileX + PLAYER_TILE_VISIBILITY); x++) {
                    if ((x >= 0) && (x < map.tilesX) && (y >= 0) && (y < map.tilesY)) {
                        map.tileFog[y * map.tilesX + x] = 1;
                    }
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            // Draw fog of war to a small render texture for automatic smoothing on scaling
            rlj.core.BeginTextureMode(fogOfWar);
            rlj.core.ClearBackground(BLANK);
            for (int y = 0; y < map.tilesY; y++) {
                for (int x = 0; x < map.tilesX; x++) {
                    if (map.tileFog[y * map.tilesX + x] == 0) {
                        rlj.shapes.DrawRectangle(x, y, 1, 1, BLACK);
                    }
                    else if (map.tileFog[y * map.tilesX + x] == 2) {
                        rlj.shapes.DrawRectangle(x, y, 1, 1, rlj.textures.Fade(BLACK, 0.8f));
                    }
                }
            }
            rlj.core.EndTextureMode();

            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            for (int y = 0; y < map.tilesY; y++) {
                for (int x = 0; x < map.tilesX; x++) {
                    // Draw tiles from id (and tile borders)
                    rlj.shapes.DrawRectangle(x * MAP_TILE_SIZE, y * MAP_TILE_SIZE, MAP_TILE_SIZE, MAP_TILE_SIZE,
                                  (map.tileIds[y * map.tilesX + x] == 0) ? BLUE : rlj.textures.Fade(BLUE, 0.9f));
                    rlj.shapes.DrawRectangleLines(x * MAP_TILE_SIZE, y * MAP_TILE_SIZE, MAP_TILE_SIZE, MAP_TILE_SIZE, rlj.textures.Fade(DARKBLUE, 0.5f));
                }
            }

            // Draw player
            rlj.shapes.DrawRectangleV(playerPosition, new Vector2(PLAYER_SIZE, PLAYER_SIZE), RED);


            // Draw fog of war (scaled to full map, bilinear filtering)
            rlj.textures.DrawTexturePro(fogOfWar.texture, new Rectangle(0, 0, (float) fogOfWar.texture.width, (float) -fogOfWar.texture.height),
                           new Rectangle(0, 0, (float) map.tilesX * MAP_TILE_SIZE, (float) map.tilesY * MAP_TILE_SIZE),
                           new Vector2(), 0.0f, WHITE);

            // Draw player current tile
            rlj.text.DrawText(rlj.text.TextFormat("Current tile: [%d,%d]", playerTileX, playerTileY), 10, 10, 20, LIME);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadRenderTexture(fogOfWar);  // Unload render texture

        rlj.core.CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }
}
