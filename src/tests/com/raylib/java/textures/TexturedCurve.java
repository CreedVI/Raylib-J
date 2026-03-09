package com.raylib.java.textures;

import com.raylib.java.Raylib;
import com.raylib.java.core.input.Mouse;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.Config.ConfigFlag.FLAG_VSYNC_HINT;
import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.RL_QUADS;
import static com.raylib.java.rlgl.RLGL.rlTextureFilterMode.RL_TEXTURE_FILTER_BILINEAR;
import static com.raylib.java.structs.Color.*;

public class TexturedCurve {


    //----------------------------------------------------------------------------------
    // Global Variables Definition
    //----------------------------------------------------------------------------------
    static Texture2D texRoad;

    static boolean showCurve = false;

    static float curveWidth = 50;
    static int curveSegments = 24;

    static Vector2 curveStartPosition;
    static Vector2 curveStartPositionTangent;

    static Vector2 curveEndPosition;
    static Vector2 curveEndPositionTangent;

    static Vector2 curveSelectedPoint;

    static Raylib rlj;

    //------------------------------------------------------------------------------------
    // Program main entry point
    //------------------------------------------------------------------------------------
    public static void main(String[] args) {
        
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib();
        rlj.core.SetConfigFlags(FLAG_VSYNC_HINT | FLAG_MSAA_4X_HINT);
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [textures] examples - textured curve");

        // Load the road texture
        texRoad = rlj.textures.LoadTexture("src/tests/resources/textures/road.png");
        rlj.textures.SetTextureFilter(texRoad, RL_TEXTURE_FILTER_BILINEAR);

        // Setup the curve
        curveStartPosition = new Vector2(80, 100);
        curveStartPositionTangent = new Vector2(100, 300);

        curveEndPosition = new Vector2(700, 350);
        curveEndPositionTangent = new Vector2(600, 100);

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            UpdateCurve();
            UpdateOptions();

            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            DrawTexturedCurve();
            DrawCurve();

            rlj.text.DrawText("Drag points to move curve, press SPACE to show/hide base curve", 10, 10, 10, DARKGRAY);
            rlj.text.DrawText(rlj.text.TextFormat("Curve width: %2.0f (Use + and - to adjust)", curveWidth), 10, 30, 10, DARKGRAY);
            rlj.text.DrawText(rlj.text.TextFormat("Curve segments: %d (Use LEFT and RIGHT to adjust)", curveSegments), 10, 50, 10, DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texRoad);

        rlj.core.CloseWindow();              // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------
    static void DrawCurve() {
        if (showCurve) {
            rlj.shapes.DrawLineBezierCubic(curveStartPosition, curveEndPosition, curveStartPositionTangent, curveEndPositionTangent, 2, BLUE);
        }

        // Draw the various control points and highlight where the mouse is
        rlj.shapes.DrawLineV(curveStartPosition, curveStartPositionTangent, SKYBLUE);
        rlj.shapes.DrawLineV(curveEndPosition, curveEndPositionTangent, PURPLE);
        Vector2 mouse = rlj.core.GetMousePosition();

        if (rlj.shapes.CheckCollisionPointCircle(mouse, curveStartPosition, 6)) {
            rlj.shapes.DrawCircleV(curveStartPosition, 7, YELLOW);
        }
        rlj.shapes.DrawCircleV(curveStartPosition, 5, RED);

        if (rlj.shapes.CheckCollisionPointCircle(mouse, curveStartPositionTangent, 6)) {
            rlj.shapes.DrawCircleV(curveStartPositionTangent, 7, YELLOW);
        }
        rlj.shapes.DrawCircleV(curveStartPositionTangent, 5, MAROON);

        if (rlj.shapes.CheckCollisionPointCircle(mouse, curveEndPosition, 6)) {
            rlj.shapes.DrawCircleV(curveEndPosition, 7, YELLOW);
        }
        rlj.shapes.DrawCircleV(curveEndPosition, 5, GREEN);

        if (rlj.shapes.CheckCollisionPointCircle(mouse, curveEndPositionTangent, 6)) {
            rlj.shapes.DrawCircleV(curveEndPositionTangent, 7, YELLOW);
        }
        rlj.shapes.DrawCircleV(curveEndPositionTangent, 5, DARKGREEN);
    }

    static void UpdateCurve() {
        // If the mouse is not down, we are not editing the curve so clear the selection
        if (!rlj.core.IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
            curveSelectedPoint = null;
            return;
        }

        // If a point was selected, move it
        if (curveSelectedPoint != null) {
            curveSelectedPoint = Vector2Add(curveSelectedPoint, rlj.core.GetMouseDelta());
            return;
        }

        // The mouse is down, and nothing was selected, so see if anything was picked
        Vector2 mouse = rlj.core.GetMousePosition();

        if (rlj.shapes.CheckCollisionPointCircle(mouse, curveStartPosition, 6)) {
            curveSelectedPoint = curveStartPosition;
        }
        else if (rlj.shapes.CheckCollisionPointCircle(mouse, curveStartPositionTangent, 6)) {
            curveSelectedPoint = curveStartPositionTangent;
        }
        else if (rlj.shapes.CheckCollisionPointCircle(mouse, curveEndPosition, 6)) {
            curveSelectedPoint = curveEndPosition;
        }
        else if (rlj.shapes.CheckCollisionPointCircle(mouse, curveEndPositionTangent, 6)) {
            curveSelectedPoint = curveEndPositionTangent;
        }
    }

    static void DrawTexturedCurve() {
        float step = 1.0f/curveSegments;

        Vector2 previous = curveStartPosition;
        Vector2 previousTangent = new Vector2();
        float previousV = 0;

        // We can't compute a tangent for the first point, so we need to reuse the tangent from the first segment
        boolean tangentSet = false;

        Vector2 current = new Vector2();
        float t = 0.0f;

        for (int i = 1; i <= curveSegments; i++) {
            // Segment the curve
            t = step*i;
            float a = (float) Math.pow(1 - t, 3);
            float b = (float) (3*Math.pow(1 - t, 2)*t);
            float c = (float) (3*(1 - t)*Math.pow(t, 2));
            float d = (float) Math.pow(t, 3);

            // Compute the endpoint for this segment
            current.y = a*curveStartPosition.y + b*curveStartPositionTangent.y + c*curveEndPositionTangent.y + d*curveEndPosition.y;
            current.x = a*curveStartPosition.x + b*curveStartPositionTangent.x + c*curveEndPositionTangent.x + d*curveEndPosition.x;

            // Vector from previous to current
            Vector2 delta = new Vector2(current.x - previous.x, current.y - previous.y);

            // The right hand normal to the delta vector
            Vector2 normal = Vector2Normalize(new Vector2(-delta.y, delta.x));

            // The v texture coordinate of the segment (add up the length of all the segments so far)
            float v = previousV + Vector2Length(delta);

            // Make sure the start point has a normal
            if (!tangentSet) {
                previousTangent = normal;
                tangentSet = true;
            }

            // Extend out the normals from the previous and current points to get the quad for this segment
            Vector2 prevPosNormal = Vector2Add(previous, Vector2Scale(previousTangent, curveWidth));
            Vector2 prevNegNormal = Vector2Add(previous, Vector2Scale(previousTangent, -curveWidth));

            Vector2 currentPosNormal = Vector2Add(current, Vector2Scale(normal, curveWidth));
            Vector2 currentNegNormal = Vector2Add(current, Vector2Scale(normal, -curveWidth));

            // Draw the segment as a quad
            rlj.rlgl.rlSetTexture(texRoad.id);
            rlj.rlgl.rlBegin(RL_QUADS);

            rlj.rlgl.rlColor4ub(255,255,255,255);
            rlj.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);

            rlj.rlgl.rlTexCoord2f(0, previousV);
            rlj.rlgl.rlVertex2f(prevNegNormal.x, prevNegNormal.y);

            rlj.rlgl.rlTexCoord2f(1, previousV);
            rlj.rlgl.rlVertex2f(prevPosNormal.x, prevPosNormal.y);

            rlj.rlgl.rlTexCoord2f(1, v);
            rlj.rlgl.rlVertex2f(currentPosNormal.x, currentPosNormal.y);

            rlj.rlgl.rlTexCoord2f(0, v);
            rlj.rlgl.rlVertex2f(currentNegNormal.x, currentNegNormal.y);

            rlj.rlgl.rlEnd();

            // The current step is the start of the next step
            previous = current;
            previousTangent = normal;
            previousV = v;
        }
    }

    static void UpdateOptions() {
        if (rlj.core.IsKeyPressed(KEY_SPACE)) {
            showCurve = !showCurve;
        }

        // Update with
        if (rlj.core.IsKeyPressed(KEY_EQUAL)) {
            curveWidth += 2;
        }
        if (rlj.core.IsKeyPressed(KEY_MINUS)) {
            curveWidth -= 2;
        }

        if (curveWidth < 2) {
            curveWidth = 2;
        }

        // Update segments
        if (rlj.core.IsKeyPressed(KEY_LEFT)) {
            curveSegments -= 2;
        }
        if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
            curveSegments += 2;
        }

        if (curveSegments < 2) {
            curveSegments = 2;
        }
    }

}
