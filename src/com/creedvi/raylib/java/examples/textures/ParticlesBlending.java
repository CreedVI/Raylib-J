package com.creedvi.raylib.java.examples.textures;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.textures.Texture2D;
import com.creedvi.raylib.java.rlj.textures.Textures;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KEY_SPACE;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.BlendMode.BLEND_ADDITIVE;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.BlendMode.BLEND_ALPHA;

public class ParticlesBlending{

    static class Particle{
        Vector2 position;
        Color color;
        float alpha;
        float size;
        float rotation;
        boolean active;        // NOTE: Use it to activate/deactive particle
    }

    final static int MAX_PARTICLES = 200;

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [textures] example - particles blending");

        // Particles pool, reuse them!
        Particle[] mouseTail = new Particle[MAX_PARTICLES];

        // Initialize particles
        for (int i = 0; i < MAX_PARTICLES; i++)
        {
            mouseTail[i] = new Particle();
            mouseTail[i].position = new Vector2();
            mouseTail[i].color = new Color(Core.GetRandomValue(0, 255), Core.GetRandomValue(0, 255),
                    Core.GetRandomValue(0, 255), 255);
            mouseTail[i].alpha = 1.0f;
            mouseTail[i].size = (float)Core.GetRandomValue(1, 30)/20.0f;
            mouseTail[i].rotation = (float)Core.GetRandomValue(0, 360);
            mouseTail[i].active = false;
        }

        float gravity = 3.0f;

        Texture2D smoke = rlj.textures.LoadTexture("resources/spark_flame.png");

        int blending = BLEND_ALPHA;

        rlj.core.SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------

            // Activate one particle every frame and Update active particles
            // NOTE: Particles initial position should be mouse position when activated
            // NOTE: Particles fall down with gravity and rotation... and disappear after 2 seconds (alpha = 0)
            // NOTE: When a particle disappears, active = false and it can be reused.
            for (int i = 0; i < MAX_PARTICLES; i++)
            {
                if (!mouseTail[i].active)
                {
                    mouseTail[i].active = true;
                    mouseTail[i].alpha = 1.0f;
                    mouseTail[i].position = rlj.core.GetMousePosition();
                    i = MAX_PARTICLES;
                }
            }

            for (int i = 0; i < MAX_PARTICLES; i++)
            {
                if (mouseTail[i].active)
                {
                    mouseTail[i].position.y += gravity/2;
                    mouseTail[i].alpha -= 0.005f;

                    if (mouseTail[i].alpha <= 0.0f) mouseTail[i].active = false;

                    mouseTail[i].rotation += 2.0f;
                }
            }

            if (rlj.core.IsKeyPressed(KEY_SPACE))
            {
                if (blending == BLEND_ALPHA){
                    blending = BLEND_ADDITIVE;
                }
                else{
                    blending = BLEND_ALPHA;
                }
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.DARKGRAY);

            rlj.core.BeginBlendMode(blending);

            // Draw active particles
            for (int i = 0; i < MAX_PARTICLES; i++)
            {
                if (mouseTail[i].active){
                    rlj.textures.DrawTexturePro(smoke,
                            new Rectangle(0.0f, 0.0f, smoke.width, smoke.height),
                            new Rectangle(mouseTail[i].position.x, mouseTail[i].position.y,
                                    smoke.width*mouseTail[i].size, smoke.height*mouseTail[i].size),
                            new Vector2((smoke.width*mouseTail[i].size/2.0f), smoke.height*mouseTail[i].size/2.0f),
                            mouseTail[i].rotation, Textures.Fade(mouseTail[i].color, mouseTail[i].alpha));
                }
            }

            rlj.core.EndBlendMode();

            rlj.text.DrawText("PRESS SPACE to CHANGE BLENDING MODE", 180, 20, 20, Color.BLACK);

            if (blending == BLEND_ALPHA){
                rlj.text.DrawText("ALPHA BLENDING", 290, screenHeight - 40, 20, Color.BLACK);
            }
            else{
                rlj.text.DrawText("ADDITIVE BLENDING", 280, screenHeight - 40, 20, Color.RAYWHITE);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(smoke);
        //--------------------------------------------------------------------------------------

    }

}
