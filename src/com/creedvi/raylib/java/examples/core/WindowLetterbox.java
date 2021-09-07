package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Config;
import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.rlgl.RLGL;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.RenderTexture;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_SPACE;

public class WindowLetterbox{

    /*******************************************************************************************
     *
     *   raylib [core] example - window scale letterbox (and virtual mouse)
     *
     *   This example has been created using raylib 2.5 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Anata (@anatagawa) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2019 Anata (@anatagawa) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/


    // Clamp Vector2 value with min and max and return a new vector2
    // NOTE: Required for virtual mouse, to clamp inside virtual game size
    static Vector2 ClampValue(Vector2 value, Vector2 min, Vector2 max)
    {
        Vector2 result = value;
        result.x = Math.min(result.x, max.x);
        result.x = Math.max(result.x, min.x);
        result.y = Math.min(result.y, max.y);
        result.y = Math.max(result.y, min.y);
        return result;
    }

    public static void main(String[] args){

        int windowWidth = 800;
        int windowHeight = 450;

        Raylib rlj = new Raylib();
        // Enable config flags for resizable window and vertical synchro
        rlj.core.SetConfigFlags(Config.ConfigFlag.FLAG_WINDOW_RESIZABLE | Config.ConfigFlag.FLAG_VSYNC_HINT);
        rlj.core.InitWindow(windowWidth, windowHeight, "raylib [core] example - window scale letterbox");
        rlj.core.SetWindowMinSize(320, 240);

        int gameScreenWidth = 640;
        int gameScreenHeight = 480;

        // Render texture initialization, used to hold the rendering result so we can easily resize it
        RenderTexture target = rlj.textures.LoadRenderTexture(gameScreenWidth, gameScreenHeight);
        rlj.textures.SetTextureFilter(target.texture, RLGL.TextureFilterMode.TEXTURE_FILTER_BILINEAR);  // Texture scale
        // filter to use

        Color[] colors = new Color[10];
        for (int i = 0; i < 10; i++)
            colors[i] = new Color(Core.GetRandomValue(100, 250), Core.GetRandomValue(50, 150),
                Core.GetRandomValue(10, 100), 255 );

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // Compute required framebuffer scaling
            float scale = Math.min((float)rlj.core.GetScreenWidth()/gameScreenWidth,
                    (float)rlj.core.GetScreenHeight()/gameScreenHeight);

            if (rlj.core.IsKeyPressed(KEY_SPACE))
            {
                // Recalculate random colors for the bars
                for (int i = 0; i < 10; i++)
                    colors[i] = new Color(Core.GetRandomValue(100, 250), Core.GetRandomValue(50, 150),
                            Core.GetRandomValue(10, 100), 255);
            }

            // Update virtual mouse (clamped mouse value behind game screen)
            Vector2 mouse = rlj.core.GetMousePosition();
            Vector2 virtualMouse = new Vector2();
            virtualMouse.x = (mouse.x - (rlj.core.GetScreenWidth() - (gameScreenWidth*scale))*0.5f)/scale;
            virtualMouse.y = (mouse.y - (rlj.core.GetScreenHeight() - (gameScreenHeight*scale))*0.5f)/scale;
            virtualMouse = ClampValue(virtualMouse, new Vector2(), new Vector2((float)gameScreenWidth, (float)gameScreenHeight));

            // Apply the same transformation as the virtual mouse to the real mouse (i.e. to work with raygui)
            //SetMouseOffset(-(GetScreenWidth() - (gameScreenWidth*scale))*0.5f, -(GetScreenHeight() - (gameScreenHeight*scale))*0.5f);
            //SetMouseScale(1/scale, 1/scale);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.BLACK);

            // Draw everything in the render texture, note this will not be rendered on screen, yet
            rlj.core.BeginTextureMode(target);

            rlj.core.ClearBackground(Color.RAYWHITE);         // Clear render texture background color

            for(int i = 0; i < 10; i++)
                rlj.shapes.DrawRectangle(0, (gameScreenHeight/10)*i, gameScreenWidth, gameScreenHeight/10, colors[i]);

            rlj.text.DrawText("If executed inside a window,\nyou can resize the window,\nand see the screen scaling!",
                    10, 25, 20, Color.WHITE);

            rlj.text.DrawText("Default Mouse: ["+(int)mouse.x+", "+(int)mouse.y+"]", 350, 25, 20, Color.GREEN);
            rlj.text.DrawText("Virtual Mouse: ["+(int)virtualMouse.x+ ", "+(int)virtualMouse.y+"]", 350, 55,
                    20, Color.YELLOW);

            rlj.core.EndTextureMode();

            // Draw RenderTexture2D to window, properly scaled
            rlj.textures.DrawTexturePro(target.texture, new Rectangle(0.0f, 0.0f, (float)target.texture.width, (float)-target.texture.height),
                    new Rectangle((rlj.core.GetScreenWidth() - ((float)gameScreenWidth*scale))*0.5f, (rlj.core.GetScreenHeight() - ((float)gameScreenHeight*scale))*0.5f,
                            (float)gameScreenWidth*scale, (float)gameScreenHeight*scale), new Vector2(), 0.0f, Color.WHITE);

            rlj.core.EndDrawing();
            //--------------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadRenderTexture(target);    // Unload render texture
        //--------------------------------------------------------------------------------------
    }

}
