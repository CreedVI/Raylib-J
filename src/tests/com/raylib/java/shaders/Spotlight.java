package com.raylib.java.shaders;

import com.raylib.java.Raylib;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Shader;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

public class Spotlight{

    /*******************************************************************************************
     *
     *   raylib [shaders] example - Simple shader mask
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 Chris Camacho (@chriscamacho)
     *
     ********************************************************************************************
     *
     *   The shader makes alpha holes in the forground to give the apearance of a top
     *   down look at a spotlight casting a pool of light...
     *
     *   The right hand side of the screen there is just enough light to see whats
     *   going on without the spot light, great for a stealth type game where you
     *   have to avoid the spotlights.
     *
     *   The left hand side of the screen is in pitch dark except for where the spotlights are.
     *
     *   Although this example doesn't scale like the letterbox example, you could integrate
     *   the two techniques, but by scaling the actual colour of the render texture rather
     *   than using alpha as a mask.
     *
     ********************************************************************************************/

    final static int MAX_SPOTS = 3;        // NOTE: It must be the same as define in shader
    final static int MAX_STARS = 400;

    static Raylib rlj;

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib(screenWidth, screenHeight, "raylib - shader spotlight");
        rlj.core.HideCursor();

        Texture2D texRay = rlj.textures.LoadTexture("src/tests/resources/shaders/raysan.png");

        Star[] stars = new Star[MAX_STARS];

        for (int n = 0; n < MAX_STARS; n++){
            stars[n] = new Star();
            ResetStar(stars[n]);
        }

        // Progress all the stars on, so they don't all start in the centre
        for (int m = 0; m < screenWidth / 2.0; m++){
            for (int n = 0; n < MAX_STARS; n++) UpdateStar(stars[n]);
        }

        int frameCounter = 0;

        // Use default vert shader
        Shader shdrSpot = rlj.core.LoadShader(null, "src/tests/resources/shaders/shaders/glsl330/spotlight.fs");

        // Get the locations of spots in the shader
        Spot[] spots = new Spot[MAX_SPOTS];

        for (int i = 0; i < MAX_SPOTS; i++){
            spots[i] = new Spot();
            String posName = "spots[" + i + "].pos";
            String innerName = "spots[" + i + "].inner";
            String radiusName = "spots[" + i + "].radius";

            spots[i].posLoc = rlj.core.GetShaderLocation(shdrSpot, posName);
            spots[i].innerLoc = rlj.core.GetShaderLocation(shdrSpot, innerName);
            spots[i].radiusLoc = rlj.core.GetShaderLocation(shdrSpot, radiusName);

        }

        // Tell the shader how wide the screen is so we can have
        // a pitch black half and a dimly lit half.
        int wLoc = rlj.core.GetShaderLocation(shdrSpot, "screenWidth");
        float sw = (float) rlj.core.GetScreenWidth();
        rlj.core.SetShaderValue(shdrSpot, wLoc, new float[]{sw}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);

        // Randomize the locations and velocities of the spotlights
        // and initialize the shader locations
        for (int i = 0; i < MAX_SPOTS; i++){
            spots[i].pos.x = rlj.core.GetRandomValue(64, screenWidth - 64);
            spots[i].pos.y = rlj.core.GetRandomValue(64, screenHeight - 64);
            spots[i].vel = new Vector2(0, 0);

            while ((Math.abs(spots[i].vel.x) + Math.abs(spots[i].vel.y)) < 2){
                spots[i].vel.x = rlj.core.GetRandomValue(-400, 40) / 10.0f;
                spots[i].vel.y = rlj.core.GetRandomValue(-400, 40) / 10.0f;
            }

            spots[i].inner = 28.0f * (i + 1);
            spots[i].radius = 48.0f * (i + 1);

            rlj.core.SetShaderValue(shdrSpot, spots[i].posLoc, new float[]{spots[i].pos.x, spots[i].pos.y}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);
            rlj.core.SetShaderValue(shdrSpot, spots[i].innerLoc, new float[]{spots[i].inner}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);
            rlj.core.SetShaderValue(shdrSpot, spots[i].radiusLoc, new float[]{spots[i].radius}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_FLOAT);
        }

        rlj.core.SetTargetFPS(60);               // Set  to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            frameCounter++;

            // Move the stars, resetting them if the go offscreen
            for (int n = 0; n < MAX_STARS; n++){
                UpdateStar(stars[n]);
            }

            // Update the spots, send them to the shader
            for (int i = 0; i < MAX_SPOTS; i++){
                if (i == 0){
                    Vector2 mp = rlj.core.GetMousePosition();
                    spots[i].pos.x = mp.x;
                    spots[i].pos.y = screenHeight - mp.y;
                }
                else{
                    spots[i].pos.x += spots[i].vel.x;
                    spots[i].pos.y += spots[i].vel.y;

                    if (spots[i].pos.x < 64) spots[i].vel.x = -spots[i].vel.x;
                    if (spots[i].pos.x > (screenWidth - 64)) spots[i].vel.x = -spots[i].vel.x;
                    if (spots[i].pos.y < 64) spots[i].vel.y = -spots[i].vel.y;
                    if (spots[i].pos.y > (screenHeight - 64)) spots[i].vel.y = -spots[i].vel.y;
                }

                rlj.core.SetShaderValue(shdrSpot, spots[i].posLoc, new float[]{spots[i].pos.x, spots[i].pos.y}, RLGL.rlShaderUniformDataType.RL_SHADER_UNIFORM_VEC2);
            }

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.DARKBLUE);

            // Draw stars and bobs
            for (int n = 0; n < MAX_STARS; n++){
                // Single pixel is just too small these days!
                rlj.shapes.DrawRectangle((int) stars[n].pos.x, (int) stars[n].pos.y, 2, 2, Color.WHITE);
            }

            for (int i = 0; i < 16; i++){
                rlj.textures.DrawTexture(texRay,
                                         (int) ((screenWidth / 2.0f) + Math.cos((frameCounter + i * 8) / 51.45f) * (screenWidth / 2.2f) - 32),
                                         (int) ((screenHeight / 2.0f) + Math.sin((frameCounter + i * 8) / 17.87f) * (screenHeight / 4.2f)), Color.WHITE);
            }

            // Draw spot lights
            rlj.core.BeginShaderMode(shdrSpot);
            // Instead of a blank rectangle you could render here
            // a render texture of the full screen used to do screen
            // scaling (slight adjustment to shader would be required
            // to actually pay attention to the colour!)
            rlj.shapes.DrawRectangle(0, 0, screenWidth, screenHeight, Color.WHITE);
            rlj.core.EndShaderMode();

            rlj.text.DrawFPS(10, 10);

            rlj.text.DrawText("Move the mouse!", 10, 30, 20, Color.GREEN);
            rlj.text.DrawText("Pitch Black", (int) (screenWidth * 0.2f), screenHeight / 2, 20, Color.GREEN);
            rlj.text.DrawText("Dark", (int) (screenWidth * .66f), screenHeight / 2, 20, Color.GREEN);


            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texRay);
        rlj.core.UnloadShader(shdrSpot);
        //--------------------------------------------------------------------------------------
    }

    static void ResetStar(Star s){
        s.pos = new Vector2(rlj.core.GetScreenWidth() / 2.0f, rlj.core.GetScreenHeight() / 2.0f);

        do{
            s.vel.x = (float) rlj.core.GetRandomValue(-1000, 1000) / 100.0f;
            s.vel.y = (float) rlj.core.GetRandomValue(-1000, 1000) / 100.0f;

        } while (!(Math.abs(s.vel.x) + Math.abs(s.vel.y) > 1));

        s.pos = Raymath.Vector2Add(s.pos, Raymath.Vector2Multiply(s.vel, new Vector2(8.0f, 8.0f)));
    }

    static void UpdateStar(Star s){
        s.pos = Raymath.Vector2Add(s.pos, s.vel);

        if ((s.pos.x < 0) || (s.pos.x > rlj.core.GetScreenWidth()) ||
                (s.pos.y < 0) || (s.pos.y > rlj.core.GetScreenHeight())){
            ResetStar(s);
        }
    }

    // Spot data
    static class Spot{
        Vector2 pos;
        Vector2 vel;
        float inner;
        float radius;

        // Shader locations
        int posLoc;
        int innerLoc;
        int radiusLoc;

        public Spot(){
            pos = new Vector2();
            vel = new Vector2();
        }

    }

    // Stars in the star field have a position and velocity
    static class Star{
        Vector2 pos;
        Vector2 vel;

        public Star(){
            pos = new Vector2();
            vel = new Vector2();
        }
    }


}
