package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.structs.Color.*;

public class RectangleBounds {

    /*******************************************************************************************
     *
     *   raylib [text] example - Draw text inside a rectangle
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2018 Blad Adrian (@demizdor)
     *
     ********************************************************************************************/

    public static Raylib rlj;

    //TODO: Fix

    public static void main(String[] args) {

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib(screenWidth, screenHeight, "raylib [text] example - draw text inside a rectangle");

        String text = """
                Text cannot escape\tthis container\t...word wrap also works when active so here's a long text for testing.

                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nec ullamcorper sit amet risus nullam eget felis eget.
            """;

        boolean resizing = false;
        boolean wordWrap = true;

        Rectangle container = new Rectangle(25.0f, 25.0f, screenWidth - 50.0f, screenHeight - 250.0f);
        Rectangle resizer = new Rectangle(container.x + container.width - 17, container.y + container.height - 17, 14, 14);

        // Minimum width and heigh for the container rectangle
        float minWidth = 60;
        float minHeight = 60;
        float maxWidth = screenWidth - 50.0f;
        float maxHeight = screenHeight - 160.0f;

        Vector2 lastMouse = new Vector2(); // Stores last mouse coordinates
        Color borderColor = MAROON;         // Container border color
        Font font = rlj.text.GetFontDefault();       // Get default system font

        rlj.core.SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {        // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_SPACE)) {
                wordWrap = !wordWrap;
            }

            Vector2 mouse = rlj.core.GetMousePosition();

            // Check if the mouse is inside the container and toggle border color
            if (rlj.shapes.CheckCollisionPointRec(mouse, container)) {
                borderColor = rlj.textures.Fade(MAROON, 0.4f);
            }
            else if (!resizing) {
                borderColor = MAROON;
            }

            // Container resizing logic
            if (resizing) {
                if (rlj.core.IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) {
                    resizing = false;
                }

                float width = container.width + (mouse.x - lastMouse.x);
                container.width = (width > minWidth) ? Math.min(width, maxWidth) : minWidth;

                float height = container.height + (mouse.y - lastMouse.y);
                container.height = (height > minHeight) ? Math.min(height, maxHeight) : minHeight;
            }
            else {
                // Check if we're resizing
                if (rlj.core.IsMouseButtonDown(MOUSE_BUTTON_LEFT) && rlj.shapes.CheckCollisionPointRec(mouse, resizer)) {
                    resizing = true;
                }
            }

            // Move resizer rectangle properly
            resizer.x = container.x + container.width - 17;
            resizer.y = container.y + container.height - 17;

            lastMouse = mouse; // Update mouse
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.shapes.DrawRectangleLinesEx(container, 3, borderColor); // Draw container border

            // Draw text in container (add some padding)
            DrawTextBoxed(font, text, new Rectangle(container.x + 4, container.y + 4, container.width - 4, container.height - 4), 20.0f, 2.0f, wordWrap, GRAY);

            rlj.shapes.DrawRectangleRec(resizer, borderColor);         // Draw the resize box

            // Draw bottom info
            rlj.shapes.DrawRectangle(0, screenHeight - 54, screenWidth, 54, GRAY);
            rlj.shapes.DrawRectangleRec(new Rectangle(382.0f, screenHeight - 34.0f, 12.0f, 12.0f), MAROON);

            rlj.text.DrawText("Word Wrap: ", 313, screenHeight - 115, 20, BLACK);
            if (wordWrap) rlj.text.DrawText("ON", 447, screenHeight - 115, 20, RED);
            else rlj.text.DrawText("OFF", 447, screenHeight - 115, 20, BLACK);

            rlj.text.DrawText("Press [SPACE] to toggle word wrap", 218, screenHeight - 86, 20, GRAY);

            rlj.text.DrawText("Click hold & drag the    to resize the container", 155, screenHeight - 38, 20, RAYWHITE);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

    //--------------------------------------------------------------------------------------
    // Module functions definition
    //--------------------------------------------------------------------------------------

    // Draw text using font inside rectangle limits
    public static void DrawTextBoxed(Font font, String text, Rectangle rec, float fontSize, float spacing, boolean wordWarp, Color tint) {
        DrawTextBoxedSelectable(font, text, rec, fontSize, spacing, wordWarp, tint, 0, 0, WHITE, WHITE);
    }

    public static final int MEASURE_STATE = 0;
    public static final int DRAW_STATE = 1;

    // Draw text using font inside rectangle limits with support for text selection
    public static void DrawTextBoxedSelectable(Font font, String text, Rectangle rec, float fontSize, float spacing, boolean wordWrap, Color tint, int selectStart, int selectLength, Color selectTint, Color selectBackTint) {
        int length = rlj.text.TextLength(text);  // Total length in bytes of the text, scanned by codepoints in loop

        float textOffsetY = 0;       // Offset between lines (on line break '\n')
        float textOffsetX = 0;       // Offset X to next character to draw

        float scaleFactor = fontSize / (float) font.baseSize;     // Character rectangle scaling factor

        // Word/character wrapping mechanism variables
        int state = wordWrap ? MEASURE_STATE : DRAW_STATE;

        int startLine = -1;         // Index where to begin drawing (where a line begins)
        int endLine = -1;           // Index where to stop drawing (where a line ends)
        int lastk = -1;             // Holds last value of the character position

        for(int i = 0, k = 0; i < length; i++, k++) {
            // Get next codepoint from byte string and glyph index in font
            int codepoint = rlj.text.GetCodepointNext(text, i);
            int codepointByteCount = rlj.text.GetCodePointByteCount(codepoint);
            int index = rlj.text.GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f) {
                codepointByteCount = 1;
            }
            i += (codepointByteCount - 1);

            float glyphWidth = 0;
            if (codepoint != '\n') {
                glyphWidth = (font.glyphs[index].advanceX == 0) ? font.recs[index].width*scaleFactor : font.glyphs[index].advanceX*scaleFactor;

                if (i + 1 < length) {
                    glyphWidth = glyphWidth + spacing;
                }
            }

            // NOTE: When wordWrap is ON we first measure how much of the text we can draw before going outside of the rec container
            // We store this info in startLine and endLine, then we change states, draw the text between those two variables
            // and change states again and again recursively until the end of the text (or until we get outside of the container).
            // When wordWrap is OFF we don't need the measure state so we go to the drawing state immediately
            // and begin drawing on the next line before we can get outside the container.
            if (state == MEASURE_STATE) {

                // TODO: There are multiple types of spaces in UNICODE, maybe it's a good idea to add support for more
                // Ref: http://jkorpela.fi/chars/spaces.html
                if ((codepoint == ' ') || (codepoint == '\t') || (codepoint == '\n')) endLine = i;
                if ((textOffsetX + glyphWidth) > rec.width) {
                    endLine = (endLine < 1)? i : endLine;
                    if (i == endLine) endLine -= codepointByteCount;
                    if ((startLine + codepointByteCount) == endLine) endLine = (i - codepointByteCount);

                    state = DRAW_STATE;
                }
                else if ((i + 1) == length) {
                    endLine = i;
                    state = DRAW_STATE;
                }
                else if (codepoint == '\n') state = DRAW_STATE;

                if (state == DRAW_STATE) {
                    textOffsetX = 0;
                    i = startLine;
                    glyphWidth = 0;

                    // Save character position when we switch states
                    int tmp = lastk;
                    lastk = k - 1;
                    k = tmp;
                }
            }
            else {
                if (codepoint == '\n') {
                    if (!wordWrap) {
                        textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                        textOffsetX = 0;
                    }
                }
                else {
                    if (!wordWrap && ((textOffsetX + glyphWidth) > rec.width)) {
                        textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                        textOffsetX = 0;
                    }

                    // When text overflows rectangle height limit, just stop drawing
                    if ((textOffsetY + font.baseSize*scaleFactor) > rec.height) {
                        break;
                    }

                    // Draw selection background
                    boolean isGlyphSelected = false;
                    if ((selectStart >= 0) && (k >= selectStart) && (k < (selectStart + selectLength))) {
                        rlj.shapes.DrawRectangleRec(new Rectangle(rec.x + textOffsetX - 1, rec.y + textOffsetY, glyphWidth, (float)font.baseSize*scaleFactor), selectBackTint);
                        isGlyphSelected = true;
                    }

                    // Draw current character glyph
                    if ((codepoint != ' ') && (codepoint != '\t')) {
                        rlj.text.DrawTextCodepoint(font, codepoint, new Vector2(rec.x + textOffsetX, rec.y + textOffsetY), fontSize, isGlyphSelected? selectTint : tint);
                    }
                }

                if (wordWrap && (i == endLine)) {
                    textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                    textOffsetX = 0;
                    startLine = endLine;
                    endLine = -1;
                    glyphWidth = 0;
                    selectStart += lastk - k;
                    k = lastk;

                    state = MEASURE_STATE;
                }
            }

            textOffsetX += glyphWidth;
        }
    }

}