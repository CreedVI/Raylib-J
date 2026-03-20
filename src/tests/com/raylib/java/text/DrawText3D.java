package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.Config.ConfigFlag.FLAG_VSYNC_HINT;
import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FREE;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_ORBITAL;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.rlgl.RLGL.RL_QUADS;
import static com.raylib.java.structs.Color.*;

public class DrawText3D {

    /*******************************************************************************************
     *
     *   raylib [text] example - Draw 3d
     *
     *   NOTE: Draw a 2D text in 3D space, each letter is drawn in a quad (or 2 quads if backface is set)
     *   where the texture coordinates of each quad map to the texture coordinates of the glyphs
     *   inside the font texture.
     *
     *   A more efficient approach, i believe, would be to render the text in a render texture and
     *   map that texture to a plane and render that, or maybe a shader but my method allows more
     *   flexibility...for example to change position of each letter individually to make something
     *   like a wavy text effect.
     *
     *   Special thanks to:
     *        @Nighten for the DrawTextStyle() code https://github.com/NightenDushi/Raylib_DrawTextStyle
     *        Chris Camacho (codifies - http://bedroomcoders.co.uk/) for the alpha discard shader
     *
     *   Example originally created with raylib 3.5, last time updated with raylib 4.0
     *
     *   Example contributed by Vlad Adrian (@demizdor) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
     *   BSD-like license that allows static linking with closed source software
     *
     *   Copyright (c) 2021-2023 Vlad Adrian (@demizdor)
     *
     ********************************************************************************************/

    final static float LETTER_BOUNDARY_SIZE = 0.25f;
    final static int TEXT_MAX_LAYERS = 32;
    final static Color LETTER_BOUNDRY_COLOR = Color.VIOLET;

    static boolean SHOW_LETTER_BOUNDARY = false;
    static boolean SHOW_TEXT_BOUNDARY = false;

    static Raylib rlj;
    
    public static class WaveTextConfig {
        public Vector3 waveRange;
        public Vector3 waveSpeed;
        public Vector3 waveOffset;

        public WaveTextConfig() {
            this.waveRange = new Vector3();
            this.waveSpeed = new Vector3();
            this.waveOffset = new Vector3();
        }
    }

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;
        
        rlj =  new Raylib();
        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT|FLAG_VSYNC_HINT);
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "raylib [text] example - draw 2D text in 3D");

        boolean spin = true;        // Spin the camera?
        boolean multicolor = false; // Multicolor mode

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(-10.0f, 15.0f, -10.0f);   // Camera position
        camera.target = new Vector3(0.0f, 0.0f, 0.0f);          // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);              // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                    // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                 // Camera projection type

        int camera_mode = CAMERA_ORBITAL;

        Vector3 cubePosition = new Vector3(0.0f, 1.0f, 0.0f);
        Vector3 cubeSize = new Vector3(2.0f, 2.0f, 2.0f);

        // Use the default font
        Font font = rlj.text.GetFontDefault();
        float fontSize = 8.0f;
        float fontSpacing = 0.5f;
        float lineSpacing = -1.0f;

        // Set the text (using markdown!)
        StringBuilder text = new StringBuilder("Hello ~~World~~ in 3D!");
        Vector3 tbox = new Vector3();
        int layers = 1;
        int quads = 0;
        float layerDistance = 0.01f;

        WaveTextConfig wcfg = new WaveTextConfig();
        wcfg.waveSpeed.x = wcfg.waveSpeed.y = 3.0f; wcfg.waveSpeed.z = 0.5f;
        wcfg.waveOffset.x = wcfg.waveOffset.y = wcfg.waveOffset.z = 0.35f;
        wcfg.waveRange.x = wcfg.waveRange.y = wcfg.waveRange.z = 0.45f;

        float time = 0.0f;

        // Setup a light and dark color
        Color light = MAROON;
        Color dark = RED;

        // Load the alpha discard shader
        Shader alphaDiscard = rlj.core.LoadShader(null, "src/tests/resources/text/shaders/glsl330/alpha_discard.fs");

        // Array filled with multiple random colors (when multicolor mode is set)
        Color[] multi = new Color[TEXT_MAX_LAYERS];

        rlj.core.DisableCursor();                    // Limit cursor to relative movement inside the window

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            camera.Update(camera_mode);

            // Handle font files dropped
            if (rlj.core.IsFileDropped()) {
                FilePathList droppedFiles = rlj.core.LoadDroppedFiles();

                // NOTE: We only support first ttf file dropped
                if (rlj.core.IsFileExtension(droppedFiles.paths[0], ".ttf")) {
                    rlj.text.UnloadFont(font);
                    font = rlj.text.LoadFontEx(droppedFiles.paths[0], (int)fontSize, null, 0);
                }
                else if (rlj.core.IsFileExtension(droppedFiles.paths[0], ".fnt")) {
                    rlj.text.UnloadFont(font);
                    font = rlj.text.LoadFont(droppedFiles.paths[0]);
                    fontSize = (float)font.baseSize;
                }

                rlj.core.UnloadDroppedFiles(droppedFiles);    // Unload filepaths from memory
            }

            // Handle Events
            if (rlj.core.IsKeyPressed(KEY_F1)) {
                SHOW_LETTER_BOUNDARY = !SHOW_LETTER_BOUNDARY;
            }
            if (rlj.core.IsKeyPressed(KEY_F2)) {
                SHOW_TEXT_BOUNDARY = !SHOW_TEXT_BOUNDARY;
            }
            if (rlj.core.IsKeyPressed(KEY_F3)) {
                // Handle camera change
                spin = !spin;
                // we need to reset the camera when changing modes
                camera = new Camera3D(rlj);
                camera.target = new Vector3(0.0f, 0.0f, 0.0f);          // Camera looking at point
                camera.up = new Vector3(0.0f, 1.0f, 0.0f);              // Camera up vector (rotation towards target)
                camera.fovy = 45.0f;                                    // Camera field-of-view Y
                camera.projection = CAMERA_PERSPECTIVE;                 // Camera mode type

                if (spin) {
                    camera.position = new Vector3(-10.0f, 15.0f, -10.0f);   // Camera position
                    camera_mode = CAMERA_ORBITAL;
                }
                else {
                    camera.position = new Vector3(10.0f, 10.0f, -10.0f);   // Camera position
                    camera_mode = CAMERA_FREE;
                }
            }

            // Handle clicking the cube
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                Ray ray = rlj.core.GetMouseRay(rlj.core.GetMousePosition(), camera);

                // Check collision between ray and box
                RayCollision collision = rlj.models.GetRayCollisionBox(
                        ray,
                        new BoundingBox(
                            new Vector3(cubePosition.x - cubeSize.x/2, cubePosition.y - cubeSize.y/2, cubePosition.z - cubeSize.z/2),
                            new Vector3(cubePosition.x + cubeSize.x/2, cubePosition.y + cubeSize.y/2, cubePosition.z + cubeSize.z/2)
                        )
                );

                if (collision.hit) {
                    // Generate new random colors
                    light = GenerateRandomColor(0.5f, 0.78f);
                    dark = GenerateRandomColor(0.4f, 0.58f);
                }
            }

            // Handle text layers changes
            if (rlj.core.IsKeyPressed(KEY_HOME)) {
                if (layers > 1){
                    --layers;
                }
            }
            else if (rlj.core.IsKeyPressed(KEY_END)) {
                if (layers < TEXT_MAX_LAYERS) {
                    ++layers;
                }
            }

            // Handle text changes
            if (rlj.core.IsKeyPressed(KEY_LEFT)) {
                fontSize -= 0.5f;
            }
            else if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
                fontSize += 0.5f;
            }
            else if (rlj.core.IsKeyPressed(KEY_UP)) {
                fontSpacing -= 0.1f;
            }
            else if (rlj.core.IsKeyPressed(KEY_DOWN)) {
                fontSpacing += 0.1f;
            }
            else if (rlj.core.IsKeyPressed(KEY_PAGE_UP)) {
                lineSpacing -= 0.1f;
            }
            else if (rlj.core.IsKeyPressed(KEY_PAGE_DOWN)) {
                lineSpacing += 0.1f;
            }
            else if (rlj.core.IsKeyDown(KEY_INSERT)) {
                layerDistance -= 0.001f;
            }
            else if (rlj.core.IsKeyDown(KEY_DELETE)) {
                layerDistance += 0.001f;
            }
            else if (rlj.core.IsKeyPressed(KEY_TAB)) {
                multicolor = !multicolor;   // Enable /disable multicolor mode

                if (multicolor) {
                    // Fill color array with random colors
                    for (int i = 0; i < TEXT_MAX_LAYERS; ++i) {
                        multi[i] = GenerateRandomColor(0.5f, 0.8f);
                        multi[i].a = rlj.core.GetRandomValue(0, 255);
                    }
                }
            }

            // Handle text input
            int ch = rlj.core.GetCharPressed();
            if (rlj.core.IsKeyPressed(KEY_BACKSPACE)) {
                // Remove last char
                if (!text.isEmpty()) {
                    text.deleteCharAt(text.length() - 1);
                }
            }
            else if (rlj.core.IsKeyPressed(KEY_ENTER)) {
                // handle newline
                text.append("\n");
            }
            else {
                // append only printable chars
                if (ch >= 32) {
                    text.append((char) ch);
                }
            }

            // Measure 3D text so we can center it
            tbox = MeasureTextWave3D(font, text.toString(), fontSize, fontSpacing, lineSpacing);

            quads = 0;                      // Reset quad counter
            time += rlj.core.GetFrameTime();         // Update timer needed by `DrawTextWave3D()`
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.core.BeginMode3D(camera);
            rlj.models.DrawCubeV(cubePosition, cubeSize, dark);
            rlj.models.DrawCubeWires(cubePosition, 2.1f, 2.1f, 2.1f, light);

            rlj.models.DrawGrid(10, 2.0f);

            // Use a shader to handle the depth buffer issue with transparent textures
            // NOTE: more info at https://bedroomcoders.co.uk/raylib-billboards-advanced-use/
            rlj.core.BeginShaderMode(alphaDiscard);

            // Draw the 3D text above the red cube
            rlj.rlgl.rlPushMatrix();
            rlj.rlgl.rlRotatef(90.0f, 1.0f, 0.0f, 0.0f);
            rlj.rlgl.rlRotatef(90.0f, 0.0f, 0.0f, -1.0f);

            for (int i = 0; i < layers; ++i) {
                Color clr = light;
                if (multicolor) {
                    clr = multi[i];
                }
                DrawTextWave3D(font, text.toString(), new Vector3(-tbox.x/2.0f, layerDistance*i, -4.5f), fontSize, fontSpacing, lineSpacing, true, wcfg, time, clr);
            }

            // Draw the text boundry if set
            if (SHOW_TEXT_BOUNDARY) {
                rlj.models.DrawCubeWiresV(new Vector3(0.0f, 0.0f, -4.5f + tbox.z/2), tbox, dark);
            }
            rlj.rlgl.rlPopMatrix();

            // Don't draw the letter boundries for the 3D text below
            boolean slb = SHOW_LETTER_BOUNDARY;
            SHOW_LETTER_BOUNDARY = false;

            // Draw 3D options (use default font)
            //-------------------------------------------------------------------------
            rlj.rlgl.rlPushMatrix();
            rlj.rlgl.rlRotatef(180.0f, 0.0f, 1.0f, 0.0f);
            String opt = rlj.text.TextFormat("< SIZE: %2.1f >", fontSize);
            quads += rlj.text.TextLength(opt);
            Vector3 m = MeasureText3D(rlj.text.GetFontDefault(), opt, 8.0f, 1.0f, 0.0f);
            Vector3 pos = new Vector3(-m.x/2.0f, 0.01f, 2.0f);
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 8.0f, 1.0f, 0.0f, false, BLUE);
            pos.z += 0.5f + m.z;

            opt = rlj.text.TextFormat("< SPACING: %2.1f >", fontSpacing);
            quads += rlj.text.TextLength(opt);
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 8.0f, 1.0f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 8.0f, 1.0f, 0.0f, false, BLUE);
            pos.z += 0.5f + m.z;

            opt = rlj.text.TextFormat("< LINE: %2.1f >", lineSpacing);
            quads += rlj.text.TextLength(opt);
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 8.0f, 1.0f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 8.0f, 1.0f, 0.0f, false, BLUE);
            pos.z += 1.0f + m.z;

            opt = rlj.text.TextFormat("< LBOX: %3s >", slb? "ON" : "OFF");
            quads += rlj.text.TextLength(opt);
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 8.0f, 1.0f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 8.0f, 1.0f, 0.0f, false, RED);
            pos.z += 0.5f + m.z;

            opt = rlj.text.TextFormat("< TBOX: %3s >", SHOW_TEXT_BOUNDARY ? "ON" : "OFF");
            quads += rlj.text.TextLength(opt);
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 8.0f, 1.0f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 8.0f, 1.0f, 0.0f, false, RED);
            pos.z += 0.5f + m.z;

            opt = rlj.text.TextFormat("< LAYER DISTANCE: %.3f >", layerDistance);
            quads += rlj.text.TextLength(opt);
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 8.0f, 1.0f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 8.0f, 1.0f, 0.0f, false, DARKPURPLE);
            rlj.rlgl.rlPopMatrix();
            //-------------------------------------------------------------------------

            // Draw 3D info text (use default font)
            //-------------------------------------------------------------------------
            opt = "All the text displayed here is in 3D";
            quads += 36;
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 10.0f, 0.5f, 0.0f);
            pos = new Vector3(-m.x/2.0f, 0.01f, 2.0f);
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 10.0f, 0.5f, 0.0f, false, DARKBLUE);
            pos.z += 1.5f + m.z;

            opt = "press [Left]/[Right] to change the font size";
            quads += 44;
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 6.0f, 0.5f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 6.0f, 0.5f, 0.0f, false, DARKBLUE);
            pos.z += 0.5f + m.z;

            opt = "press [Up]/[Down] to change the font spacing";
            quads += 44;
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 6.0f, 0.5f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 6.0f, 0.5f, 0.0f, false, DARKBLUE);
            pos.z += 0.5f + m.z;

            opt = "press [PgUp]/[PgDown] to change the line spacing";
            quads += 48;
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 6.0f, 0.5f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 6.0f, 0.5f, 0.0f, false, DARKBLUE);
            pos.z += 0.5f + m.z;

            opt = "press [F1] to toggle the letter boundry";
            quads += 39;
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 6.0f, 0.5f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 6.0f, 0.5f, 0.0f, false, DARKBLUE);
            pos.z += 0.5f + m.z;

            opt = "press [F2] to toggle the text boundry";
            quads += 37;
            m = MeasureText3D(rlj.text.GetFontDefault(), opt, 6.0f, 0.5f, 0.0f);
            pos.x = -m.x/2.0f;
            DrawText3D(rlj.text.GetFontDefault(), opt, pos, 6.0f, 0.5f, 0.0f, false, DARKBLUE);
            //-------------------------------------------------------------------------

            SHOW_LETTER_BOUNDARY = slb;
            rlj.core.EndShaderMode();

            rlj.core.EndMode3D();

            // Draw 2D info text & stats
            //-------------------------------------------------------------------------
            rlj.text.DrawText("Drag & drop a font file to change the font!\nType something, see what happens!\n\nPress [F3] to toggle the camera", 10, 35, 10, BLACK);

            quads += rlj.text.TextLength(text.toString())*2*layers;
            String tmp = rlj.text.TextFormat("%2d layer(s) | %s camera | %4d quads (%4d verts)", layers, spin? "ORBITAL" : "FREE", quads, quads*4);
            int width = rlj.text.MeasureText(tmp, 10);
            rlj.text.DrawText(tmp, SCREEN_WIDTH - 20 - width, 10, 10, DARKGREEN);

            tmp = "[Home]/[End] to add/remove 3D text layers";
            width = rlj.text.MeasureText(tmp, 10);
            rlj.text.DrawText(tmp, SCREEN_WIDTH - 20 - width, 25, 10, DARKGRAY);

            tmp = "[Insert]/[Delete] to increase/decrease distance between layers";
            width = rlj.text.MeasureText(tmp, 10);
            rlj.text.DrawText(tmp, SCREEN_WIDTH - 20 - width, 40, 10, DARKGRAY);

            tmp = "click the [CUBE] for a random color";
            width = rlj.text.MeasureText(tmp, 10);
            rlj.text.DrawText(tmp, SCREEN_WIDTH - 20 - width, 55, 10, DARKGRAY);

            tmp = "[Tab] to toggle multicolor mode";
            width = rlj.text.MeasureText(tmp, 10);
            rlj.text.DrawText(tmp, SCREEN_WIDTH - 20 - width, 70, 10, DARKGRAY);
            //-------------------------------------------------------------------------

            rlj.text.DrawFPS(10, 10);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.text.UnloadFont(font);
        rlj.core.CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

    //--------------------------------------------------------------------------------------
    // Module Functions Definitions
    //--------------------------------------------------------------------------------------
    // Draw codepoint at specified position in 3D space
    static void DrawTextCodepoint3D(Font font, int codepoint, Vector3 position, float fontSize, boolean backface, Color tint) {
        // Character index position in sprite font
        // NOTE: In case a codepoint is not available in the font, index returned points to '?'
        int index = rlj.text.GetGlyphIndex(font, codepoint);
        float scale = fontSize/(float)font.baseSize;

        // Character destination rectangle on screen
        // NOTE: We consider charsPadding on drawing
        position.x += (float)(font.glyphs[index].offsetX - font.glyphPadding)/(float)font.baseSize*scale;
        position.z += (float)(font.glyphs[index].offsetY - font.glyphPadding)/(float)font.baseSize*scale;

        // Character source rectangle from font texture atlas
        // NOTE: We consider chars padding when drawing, it could be required for outline/glow shader effects
        Rectangle srcRec = new Rectangle(font.recs[index].x - (float)font.glyphPadding, font.recs[index].y - (float)font.glyphPadding,
                font.recs[index].width + 2.0f*font.glyphPadding, font.recs[index].height + 2.0f*font.glyphPadding);

        float width = (float)(font.recs[index].width + 2.0f*font.glyphPadding)/(float)font.baseSize*scale;
        float height = (float)(font.recs[index].height + 2.0f*font.glyphPadding)/(float)font.baseSize*scale;

        if (font.texture.id > 0) {
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

            // normalized texture coordinates of the glyph inside the font texture (0.0f . 1.0f)
        float tx = srcRec.x/font.texture.width;
        float ty = srcRec.y/font.texture.height;
        float tw = (srcRec.x+srcRec.width)/font.texture.width;
        float th = (srcRec.y+srcRec.height)/font.texture.height;

        if (SHOW_LETTER_BOUNDARY) {
            rlj.models.DrawCubeWiresV(new Vector3(position.x + width/2, position.y, position.z + height/2), new Vector3(width, LETTER_BOUNDARY_SIZE, height), LETTER_BOUNDRY_COLOR);
        }

            rlj.rlgl.rlCheckRenderBatchLimit(4 + (backface ? 1 : 0));
            rlj.rlgl.rlSetTexture(font.texture.id);

            rlj.rlgl.rlPushMatrix();
            rlj.rlgl.rlTranslatef(position.x, position.y, position.z);

            rlj.rlgl.rlBegin(RL_QUADS);
            rlj.rlgl.rlColor4ub(tint.r, tint.g, tint.b, tint.a);

            // Front Face
            rlj.rlgl.rlNormal3f(0.0f, 1.0f, 0.0f);                                   // Normal Pointing Up
            rlj.rlgl.rlTexCoord2f(tx, ty); rlj.rlgl.rlVertex3f(x, y, z);              // Top Left Of The Texture and Quad
            rlj.rlgl.rlTexCoord2f(tx, th); rlj.rlgl.rlVertex3f(x, y,z + height);     // Bottom Left Of The Texture and Quad
            rlj.rlgl.rlTexCoord2f(tw, th); rlj.rlgl.rlVertex3f(x + width, y, z + height);     // Bottom Right Of The Texture and Quad
            rlj.rlgl.rlTexCoord2f(tw, ty); rlj.rlgl.rlVertex3f(x + width, y, z);              // Top Right Of The Texture and Quad

            if (backface) {
                // Back Face
                rlj.rlgl.rlNormal3f(0.0f, -1.0f, 0.0f);                              // Normal Pointing Down
                rlj.rlgl.rlTexCoord2f(tx, ty); rlj.rlgl.rlVertex3f(x, y, z);          // Top Right Of The Texture and Quad
                rlj.rlgl.rlTexCoord2f(tw, ty); rlj.rlgl.rlVertex3f(x + width, y, z);          // Top Left Of The Texture and Quad
                rlj.rlgl.rlTexCoord2f(tw, th); rlj.rlgl.rlVertex3f(x + width, y, z + height); // Bottom Left Of The Texture and Quad
                rlj.rlgl.rlTexCoord2f(tx, th); rlj.rlgl.rlVertex3f(x, y, z + height); // Bottom Right Of The Texture and Quad
            }
            rlj.rlgl.rlEnd();
            rlj.rlgl.rlPopMatrix();

            rlj.rlgl.rlSetTexture(0);
        }
    }

    // Draw a 2D text in 3D space
    static void DrawText3D(Font font, String text, Vector3 position, float fontSize, float fontSpacing, float lineSpacing, boolean backface, Color tint) {
        int length = rlj.text.TextLength(text);          // Total length in bytes of the text, scanned by codepoints in loop

        float textOffsetY = 0.0f;               // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;               // Offset X to next character to draw

        float scale = fontSize/(float)font.baseSize;

        for (int i = 0; i < length;) {
            // Get next codepoint from byte string and glyph index in font
            int codepoint = rlj.text.GetCodepointNext(text, i);
            int codepointByteCount = rlj.text.GetCodePointByteCount(codepoint);
            int index = rlj.text.GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f) {
                codepointByteCount = 1;
            }

            if (codepoint == '\n') {
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += scale + lineSpacing/(float)font.baseSize*scale;
                textOffsetX = 0.0f;
            }
            else {
                if ((codepoint != ' ') && (codepoint != '\t')) {
                    DrawTextCodepoint3D(font, codepoint, new Vector3(position.x + textOffsetX, position.y, position.z + textOffsetY), fontSize, backface, tint);
                }

                if (font.glyphs[index].advanceX == 0) {
                    textOffsetX += (font.recs[index].width + fontSpacing)/(float)font.baseSize*scale;
                }
                else {
                    textOffsetX += (font.glyphs[index].advanceX + fontSpacing)/(float)font.baseSize*scale;
                }
            }

            i += codepointByteCount;   // Move text bytes counter to next codepoint
        }
    }

    // Measure a text in 3D. For some reason `MeasureTextEx()` just doesn't seem to work so i had to use this instead.
    static Vector3 MeasureText3D(Font font, String text, float fontSize, float fontSpacing, float lineSpacing) {
        int len = rlj.text.TextLength(text);
        int tempLen = 0;                // Used to count longer text line num chars
        int lenCounter = 0;

        float tempTextWidth = 0.0f;     // Used to count longer text line width

        float scale = fontSize/(float)font.baseSize;
        float textHeight = scale;
        float textWidth = 0.0f;

        int letter = 0;                 // Current character
        int index = 0;                  // Index position in sprite font

        for (int i = 0; i < len; i++) {
            lenCounter++;

            int next = 0;
            letter = rlj.text.GetCodepointNext(text, i);
            next = rlj.text.GetCodePointByteCount(letter);
            index = rlj.text.GetGlyphIndex(font, letter);

            // NOTE: normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol so to not skip any we set next = 1
            if (letter == 0x3f) {
                next = 1;
            }
            i += next - 1;

            if (letter != '\n') {
                if (font.glyphs[index].advanceX != 0) {
                    textWidth += (font.glyphs[index].advanceX+fontSpacing)/(float)font.baseSize*scale;
                }
                else {
                    textWidth += (font.recs[index].width + font.glyphs[index].offsetX)/(float)font.baseSize*scale;
                }
            }
            else {
                if (tempTextWidth < textWidth) {
                    tempTextWidth = textWidth;
                }
                lenCounter = 0;
                textWidth = 0.0f;
                textHeight += scale + lineSpacing/(float)font.baseSize*scale;
            }

            if (tempLen < lenCounter) {
                tempLen = lenCounter;
            }
        }

        if (tempTextWidth < textWidth) {
            tempTextWidth = textWidth;
        }

        Vector3 vec = new Vector3();
        vec.x = tempTextWidth + ((tempLen - 1)*fontSpacing/(float)font.baseSize*scale); // Adds chars spacing to measure
        vec.y = 0.25f;
        vec.z = textHeight;

        return vec;
    }

    // Draw a 2D text in 3D space and wave the parts that start with `~~` and end with `~~`.
    // This is a modified version of the original code by @Nighten found here https://github.com/NightenDushi/Raylib_DrawTextStyle
    static void DrawTextWave3D(Font font, String text, Vector3 position, float fontSize, float fontSpacing, float lineSpacing, boolean backface, WaveTextConfig config, float time, Color tint) {
        int length = rlj.text.TextLength(text);          // Total length in bytes of the text, scanned by codepoints in loop

        float textOffsetY = 0.0f;               // Offset between lines (on line break '\n')
        float textOffsetX = 0.0f;               // Offset X to next character to draw

        float scale = fontSize/(float)font.baseSize;

        boolean wave = false;

        for (int i = 0, k = 0; i < length; ++k) {
            // Get next codepoint from byte string and glyph index in font
            int codepoint = rlj.text.GetCodepoint(text, i);
            int codepointByteCount = rlj.text.GetCodePointByteCount(codepoint);
            int index = rlj.text.GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f) {
                codepointByteCount = 1;
            }

            if (codepoint == '\n') {
                // NOTE: Fixed line spacing of 1.5 line-height
                // TODO: Support custom line spacing defined by user
                textOffsetY += scale + lineSpacing/(float)font.baseSize*scale;
                textOffsetX = 0.0f;
                k = 0;
            }
            else if (codepoint == '~') {
                int next = rlj.text.GetCodepoint(text, i+codepointByteCount);
                if (rlj.text.GetCodepoint(text, i+1) == '~') {
                    codepointByteCount += 1;
                    wave = !wave;
                }
            }
            else {
                if ((codepoint != ' ') && (codepoint != '\t')) {
                    Vector3 pos = new Vector3(position.x, position.y, position.z);
                    if (wave) {
                        // Apply the wave effect
                        pos.x += (float) (Math.sin(time*config.waveSpeed.x-k*config.waveOffset.x)*config.waveRange.x);
                        pos.y += (float) (Math.sin(time*config.waveSpeed.y-k*config.waveOffset.y)*config.waveRange.y);
                        pos.z += (float) (Math.sin(time*config.waveSpeed.z-k*config.waveOffset.z)*config.waveRange.z);
                    }

                    DrawTextCodepoint3D(font, codepoint, new Vector3(pos.x + textOffsetX, pos.y, pos.z + textOffsetY), fontSize, backface, tint);
                }

                if (font.glyphs[index].advanceX == 0) {
                    textOffsetX += (font.recs[index].width + fontSpacing)/(float)font.baseSize*scale;
                }
                else {
                    textOffsetX += (font.glyphs[index].advanceX + fontSpacing)/(float)font.baseSize*scale;
                }
            }

            i += codepointByteCount;   // Move text bytes counter to next codepoint
        }
    }

    // Measure a text in 3D ignoring the `~~` chars.
    static Vector3 MeasureTextWave3D(Font font, String text, float fontSize, float fontSpacing, float lineSpacing)
    {
        int len = rlj.text.TextLength(text);
        int tempLen = 0;                // Used to count longer text line num chars
        int lenCounter = 0;

        float tempTextWidth = 0.0f;     // Used to count longer text line width

        float scale = fontSize/(float)font.baseSize;
        float textHeight = scale;
        float textWidth = 0.0f;

        int letter = 0;                 // Current character
        int index = 0;                  // Index position in sprite font

        for (int i = 0; i < len; i++) {
            lenCounter++;

            letter = rlj.text.GetCodepoint(text, i);
            int next = rlj.text.GetCodePointByteCount(letter);
            index = rlj.text.GetGlyphIndex(font, letter);

            // NOTE: normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol so to not skip any we set next = 1
            if (letter == 0x3f) {
                next = 1;
            }
            i += next - 1;

            if (letter != '\n') {
                if (letter == '~' && rlj.text.GetCodepoint(text, i+1) == '~') {
                    i++;
                }
                else {
                    if (font.glyphs[index].advanceX != 0) {
                        textWidth += (font.glyphs[index].advanceX+fontSpacing)/(float)font.baseSize*scale;
                    }
                    else {
                        textWidth += (font.recs[index].width + font.glyphs[index].offsetX)/(float)font.baseSize*scale;
                    }
                }
            }
            else {
                if (tempTextWidth < textWidth) {
                    tempTextWidth = textWidth;
                }
                lenCounter = 0;
                textWidth = 0.0f;
                textHeight += scale + lineSpacing/(float)font.baseSize*scale;
            }

            if (tempLen < lenCounter) {
                tempLen = lenCounter;
            }
        }

        if (tempTextWidth < textWidth) {
            tempTextWidth = textWidth;
        }

        Vector3 vec = new Vector3();
        vec.x = tempTextWidth + ((tempLen - 1)*fontSpacing/(float)font.baseSize*scale); // Adds chars spacing to measure
        vec.y = 0.25f;
        vec.z = textHeight;

        return vec;
    }

    // Generates a nice color with a random hue
    static Color GenerateRandomColor(float s, float v) {
    float Phi = 0.618033988749895f; // Golden ratio conjugate
        float h = (float)rlj.core.GetRandomValue(0, 360);
        h = Math.floorMod((int) (h + h*Phi), 360);
        return rlj.textures.ColorFromHSV(h, s, v);
    }

}
