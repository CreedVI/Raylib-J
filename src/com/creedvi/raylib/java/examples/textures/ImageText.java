package com.creedvi.raylib.java.examples.textures;

public class ImageText{

    //TODO: Custom Font loading

    /*public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        InitWindow(screenWidth, screenHeight, "raylib [texture] example - image text drawing");

        Image parrots = LoadImage("resources/parrots.png"); // Load image in CPU memory (RAM)

        // TTF Font loading with custom generation parameters
        Font font = LoadFontEx("resources/KAISG.ttf", 64, 0, 0);

        // Draw over image using custom font
        ImageDrawTextEx(&parrots, font, "[Parrots font drawing]", (Vector2){ 20.0f, 20.0f }, (float)font.baseSize, 0.0f, RED);

        Texture2D texture = LoadTextureFromImage(parrots);  // Image converted to texture, uploaded to GPU memory (VRAM)
        UnloadImage(parrots);   // Once image has been converted to texture and uploaded to VRAM, it can be unloaded from RAM

        Vector2 position = { (float)(screenWidth/2 - texture.width/2), (float)(screenHeight/2 - texture.height/2 - 20) };

        bool showFont = false;

        SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (IsKeyDown(KEY_SPACE)) showFont = true;
            else showFont = false;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            BeginDrawing();

            ClearBackground(RAYWHITE);

            if (!showFont)
            {
                // Draw texture with text already drawn inside
                DrawTextureV(texture, position, WHITE);

                // Draw text directly using sprite font
                DrawTextEx(font, "[Parrots font drawing]", (Vector2){ position.x + 20,
                        position.y + 20 + 280 }, (float)font.baseSize, 0.0f, WHITE);
            }
            else DrawTexture(font.texture, screenWidth/2 - font.texture.width/2, 50, BLACK);

            DrawText("PRESS SPACE to SEE USED SPRITEFONT ", 290, 420, 10, DARKGRAY);

            EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        UnloadTexture(texture);     // Texture unloading

        UnloadFont(font);           // Unload custom spritefont
        //--------------------------------------------------------------------------------------
    }*/

}
