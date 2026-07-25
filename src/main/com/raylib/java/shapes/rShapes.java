package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.structs.*;
import org.jetbrains.annotations.Contract;

import static com.raylib.java.Config.SUPPORT_QUADS_DRAW_MODE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.raymath.Raymath.PI;
import static com.raylib.java.rlgl.RLGL.*;

public class rShapes {

    /**********************************************************************************************
     *
     *   rshapes - Basic functions to draw 2d shapes and check collisions
     *
     *   ADDITIONAL NOTES:
     *       Shapes can be draw using 3 types of primitives: LINES, TRIANGLES and QUADS.
     *       Some functions implement two drawing options: TRIANGLES and QUADS, by default TRIANGLES
     *       are used but QUADS implementation can be selected with SUPPORT_QUADS_DRAW_MODE define
     *
     *       Some functions define texture coordinates (rlTexCoord2f()) for the shapes and use a
     *       user-provided texture with SetShapesTexture(), the pourpouse of this implementation
     *       is allowing to reduce draw calls when combined with a texture-atlas.
     *
     *       By default, raylib sets the default texture and rectangle at InitWindow()[rcore] to one
     *       white character of default font [rtext], this way, raylib text and shapes can be draw with
     *       a single draw call and it also allows users to configure it the same way with their own fonts.
     *
     *   CONFIGURATION:
     *       #define SUPPORT_MODULE_RSHAPES
     *           rshapes module is included in the build
     *
     *       #define SUPPORT_QUADS_DRAW_MODE
     *           Use QUADS instead of TRIANGLES for drawing when possible. Lines-based shapes still use LINES
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2013-2023 Ramon Santamaria (@raysan5)
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     *******************

    /**
     * Error rate to calculate how many segments we need to draw a smooth circle,
     * taken from <a href="https://stackoverflow.com/a/2244088">https://stackoverflow.com/a/2244088</a>
     */
    final static float SMOOTH_CIRCLE_ERROR_RATE = 0.5f;

    /**
     * Bezier line divisions
     */
    int SPLINE_SEGMENT_DIVISIONS = 24;

    private Texture2D texShapes = new Texture2D(1, 1, 1, 1, rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8); // Texture used on rShapes drawing (white pixel loaded by rlgl)
    private Rectangle texShapesRec = new Rectangle(0f, 0f, 1f, 1f);        // Texture source rectangle used on rShapes drawing

    final private Raylib context;

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    public rShapes(Raylib context) {
        this.context = context;
    }

    /** Set texture and rectangle to be used on rShapes drawing
     * NOTE: It can be useful when using basic rShapes and one single font,
     * defining a font char white rectangle would allow drawing everything in a single draw call
     *
     * @param texture New default shape texture
     * @param source Defined area of default texture
     */
    public void SetShapesTexture(Texture2D texture, Rectangle source) {
        // Reset texture to default pixel if required
        // WARNING: Shapes texture should be probably better validated,
        // it can break the rendering of all shapes if misused
        if ((texture.id == 0) || (source.width == 0) || (source.height == 0)) {
            texShapes = new Texture2D(1, 1, 1, 1, rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
            texShapesRec = new Rectangle(0.0f, 0.0f, 1.0f, 1.0f);
        }
        else {
            texShapes = texture;
            texShapesRec = source;
        }
    }

    /**
     * Draw a pixel
     *
     * @param posX  X coordinate of pixel
     * @param posY  Y coordinate of pixel
     * @param color Color to draw pixel
     */
    public void DrawPixel(int posX, int posY, Color color) {
        DrawPixelV(new Vector2(posX, posY), color);
    }

    /**
     * Draw a pixel (Vector version)
     *
     * @param position X, Y position of pixel
     * @param color    Color to draw pixel
     */
    public void DrawPixelV(Vector2 position, Color color) {
        if (SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlSetTexture(texShapes.id);

            context.rlgl.rlBegin(RL_QUADS);

            context.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
            context.rlgl.rlVertex2f(position.x, position.y);

            context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
            context.rlgl.rlVertex2f(position.x, position.y + 1);

            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
            context.rlgl.rlVertex2f(position.x + 1, position.y + 1);

            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
            context.rlgl.rlVertex2f(position.x + 1, position.y);

            context.rlgl.rlEnd();

            context.rlgl.rlSetTexture(0);
        }
        else {
            context.rlgl.rlBegin(RL_TRIANGLES);

            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlVertex2f(position.x, position.y);
            context.rlgl.rlVertex2f(position.x, position.y + 1);
            context.rlgl.rlVertex2f(position.x + 1, position.y);

            context.rlgl.rlVertex2f(position.x + 1, position.y);
            context.rlgl.rlVertex2f(position.x, position.y + 1);
            context.rlgl.rlVertex2f(position.x + 1, position.y + 1);

            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a line using gl lines
     *
     * @param startPosX X position to begin drawing line
     * @param startPosY Y position to begin drawing line
     * @param endPosX   X position to end drawing line
     * @param endPosY   Y position to end drawing line
     * @param color     color to draw line
     */
    public void DrawLine(int startPosX, int startPosY, int endPosX, int endPosY, Color color) {
        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlVertex2i(startPosX, startPosY);
        context.rlgl.rlVertex2i(endPosX, endPosY);
        context.rlgl.rlEnd();
    }

    /**
     * Draw a line using gl lines
     *
     * @param startPos X, Y position to begin drawing line
     * @param endPos   X, Y position to end drawing line
     * @param color    color to draw line
     */
    public void DrawLineV(Vector2 startPos, Vector2 endPos, Color color) {
        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlVertex2f(startPos.x, startPos.y);
        context.rlgl.rlVertex2f(endPos.x, endPos.y);
        context.rlgl.rlEnd();
    }

    /**
     * Draw lines sequence (using gl lines)
     * @param points Array of X, Y points to draw lines
     * @param color color to draw lines
     */
    public void DrawLineStrip(Vector2[] points, Color color) {
        if (points.length >= 2) {
            context.rlgl.rlBegin(RL_LINES);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            for (int i = 0; i < points.length - 1; i++) {
                context.rlgl.rlVertex2f(points[i].x, points[i].y);
                context.rlgl.rlVertex2f(points[i + 1].x, points[i + 1].y);
            }
            context.rlgl.rlEnd();
        }
    }

    /**
     *  Draw line using cubic-bezier spline, in-out interpolation, no control points
     * @param startPos X, Y position to begin drawing line
     * @param endPos X, Y position to end drawing line
     * @param thick thickness of the line
     * @param color color to draw the line
     */
    public void DrawLineBezier(Vector2 startPos, Vector2 endPos, float thick, Color color) {
        Vector2 previous = new Vector2(startPos.x, startPos.y);
        Vector2 current = new Vector2();

        Vector2[] points = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector2();
        }

        for (int i = 1; i <= SPLINE_SEGMENT_DIVISIONS; i++) {
            // Cubic easing in-out
            // NOTE: Easing is calculated only for y position value
            current.y = EaseCubicInOut(i, startPos.y, endPos.y - startPos.y, SPLINE_SEGMENT_DIVISIONS);
            current.x = previous.x + (endPos.x - startPos.x)/SPLINE_SEGMENT_DIVISIONS;

            float dy = current.y - previous.y;
            float dx = current.x - previous.x;
            float size = (float) (0.5f*thick/Math.sqrt(dx*dx+dy*dy));

            if (i == 1) {
                points[0].x = previous.x + dy*size;
                points[0].y = previous.y - dx*size;
                points[1].x = previous.x - dy*size;
                points[1].y = previous.y + dx*size;
            }

            points[2*i + 1].x = current.x - dy*size;
            points[2*i + 1].y = current.y + dx*size;
            points[2*i].x = current.x + dy*size;
            points[2*i].y = current.y - dx*size;

            previous.x = current.x;
            previous.y = current.y;
        }

        DrawTriangleStrip(points, color);
    }



    /**
     * Draw a line defining thickness
     *
     * @param startPos X, Y position to begin drawing line
     * @param endPos   X, Y position to end drawing line
     * @param thick    thickness of the line
     * @param color    color to draw the line
     */
    public void DrawLineEx(Vector2 startPos, Vector2 endPos, float thick, Color color) {
        Vector2 delta = new Vector2(endPos.x - startPos.x, endPos.y - startPos.y);
        float length = (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y);

        if((length > 0) && (thick > 0)) {
            float scale = thick / (2 * length);

            Vector2 radius = new Vector2(-scale * delta.y, scale * delta.x);
            Vector2[] strip = new Vector2[]{
                    new Vector2(startPos.x - radius.x, startPos.y - radius.y),
                    new Vector2(startPos.x + radius.x, startPos.y + radius.y),
                    new Vector2(endPos.x - radius.x, endPos.y - radius.y),
                    new Vector2(endPos.x + radius.x, endPos.y + radius.y)
            };

            DrawTriangleStrip(strip, color);
        }
    }

    /**
     * Draw a color-filled circle
     *
     * @param centerX X coordinate of circle center
     * @param centerY Y coordinate of circle center
     * @param radius  length of circle radius
     * @param color   color to fill circle
     */
    public void DrawCircle(int centerX, int centerY, float radius, Color color) {
        DrawCircleV(new Vector2((float) centerX, (float) centerY), radius, color);
    }

    /**
     * Draw a color-filled circle (Vector version)
     * NOTE: On OpenGL 3.3 and ES2 we use QUADS to avoid drawing order issues (view rlglDraw)
     *
     * @param center X, Y position of circle center
     * @param radius length of circle radius
     * @param color  color to draw circle
     */
    public void DrawCircleV(Vector2 center, float radius, Color color) {
        DrawCircleSector(center, radius, 0, 360, 36, color);
    }

    /**
     * Draw a piece of a circle
     *
     * @param center     X, Y position of circle center
     * @param radius     length of circle radius
     * @param startAngle angle to begin drawing circle sector
     * @param endAngle   angle to end drawing circle sector
     * @param segments   number of segments
     * @param color      color to draw circle sector
     */
    public void DrawCircleSector(Vector2 center, float radius, float startAngle, float endAngle, int segments, Color color) {
        if(radius <= 0.0f) {
            radius = 0.1f;  // A public void div by zero
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle) {
            // Swap values
            float tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        int minSegments = (int)Math.ceil((endAngle - startAngle)/90);

        if(segments < minSegments) {
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0) {
                segments = minSegments;
            }
        }

        float stepLength = (endAngle - startAngle) / (float) segments;
        float angle = startAngle;

        if(SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlCheckRenderBatchLimit(4*segments/2);

            context.rlgl.rlSetTexture(texShapes.getId());

            context.rlgl.rlBegin(RL_QUADS);
            // NOTE: Every QUAD actually represents two segments
            for(int i = 0; i < segments / 2; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
                context.rlgl.rlVertex2f(center.x, center.y);

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength*2.0f))*radius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength*2.0f))*radius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*radius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*radius));

                context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*radius), (float) (center.y + Math.sin(DEG2RAD*angle)*radius));

                angle += (stepLength*2.0f);
            }

            // NOTE: In case number of segments is odd, we add one last piece to the cake
            if(segments % 2 == 1) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
                context.rlgl.rlVertex2f(center.x, center.y);

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*radius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*radius));

                context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*radius), (float) (center.y + Math.sin(DEG2RAD*angle)*radius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
                context.rlgl.rlVertex2f(center.x, center.y);
            }
            context.rlgl.rlEnd();

            context.rlgl.rlSetTexture(0);
        }
        else{
            context.rlgl.rlCheckRenderBatchLimit(3*segments);

            context.rlgl.rlBegin(RL_TRIANGLES);
            for(int i = 0; i < segments; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                context.rlgl.rlVertex2f(center.x, center.y);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*radius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*radius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*radius), (float) (center.y + Math.sin(DEG2RAD*angle)*radius));

                angle += stepLength;
            }
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a piece of a circle outlines
     *
     * @param center     X, Y coordinate of circle center
     * @param radius     length of circle radius
     * @param startAngle angle to begin drawing circle sector
     * @param endAngle   angle to end drawing circle sector
     * @param segments   number of segments
     * @param color      color to draw circle sector
     */
    public void DrawCircleSectorLines(Vector2 center, float radius, float startAngle, float endAngle, int segments,
                                      Color color) {
        if(radius <= 0.0f) {
            radius = 0.1f;  // Avoid div by zero issue
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle) {
            // Swap values
            float tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        int minSegments = (int)Math.ceil((endAngle - startAngle)/90);

        if(segments < minSegments) {
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0) {
                segments = minSegments;
            }
        }

        float stepLength = (endAngle - startAngle) / (float) segments;
        float angle = startAngle;

        // Hide the cap lines when the circle is full
        boolean showCapLines = true;
        int limit = 2 * (segments + 2);
        if((endAngle - startAngle) % 360 == 0) {
            limit = 2 * segments;
            showCapLines = false;
        }

        context.rlgl.rlCheckRenderBatchLimit(limit);

        context.rlgl.rlBegin(RL_LINES);
        if(showCapLines) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(center.x, center.y);
            context.rlgl.rlVertex2f(center.x + (float) Math.cos(DEG2RAD * angle) * radius, center.y + (float) Math.sin(DEG2RAD * angle) * radius);
        }

        for(int i = 0; i < segments; i++) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlVertex2f(center.x + (float) Math.cos(DEG2RAD * angle) * radius, center.y + (float) Math.sin(DEG2RAD * angle) * radius);
            context.rlgl.rlVertex2f(center.x + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius, center.y + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius);

            angle += stepLength;
        }

        if(showCapLines) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(center.x, center.y);
            context.rlgl.rlVertex2f(center.x + (float) Math.cos(DEG2RAD * angle) * radius, center.y + (float) Math.sin(DEG2RAD * angle) * radius);
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw a gradient-filled circle
     * NOTE: Gradient goes from center (color1) to border (color2)
     *
     * @param centerX X coordinate of circle center
     * @param centerY Y coordinate of circle center
     * @param radius  length of circle radius
     * @param color1  color at the beginning of the gradient
     * @param color2  color at the end of the gradient
     */
    public void DrawCircleGradient(int centerX, int centerY, float radius, Color color1, Color color2) {
        context.rlgl.rlCheckRenderBatchLimit(3*36);

        context.rlgl.rlBegin(RL_TRIANGLES);
        for(int i = 0; i < 360; i += 10) {
            context.rlgl.rlColor4ub(color1.r, color1.g, color1.b, color1.a);
            context.rlgl.rlVertex2f((float) centerX, (float) centerY);
            context.rlgl.rlColor4ub(color2.r, color2.g, color2.b, color2.a);
            context.rlgl.rlVertex2f((float) (centerX + Math.cos(DEG2RAD*(i + 10))*radius), (float) (centerY + Math.sin(DEG2RAD*(i + 10))*radius));
            context.rlgl.rlColor4ub(color2.r, color2.g, color2.b, color2.a);
            context.rlgl.rlVertex2f((float) (centerX + Math.cos(DEG2RAD*i)*radius), (float) (centerY + Math.sin(DEG2RAD*i)*radius));
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw circle outline
     *
     * @param centerX X coordinate of circle center
     * @param centerY Y coordinate of circle center
     * @param radius  length of circle radius
     * @param color   color to draw circle outline
     */
    public void DrawCircleLines(int centerX, int centerY, float radius, Color color) {
        DrawCircleLinesV(new Vector2(centerX, centerY), radius, color);
    }

    /**
     * Draw circle outline
     *
     * @param center X, Y coordinate of circle center
     * @param radius  length of circle radius
     * @param color   color to draw circle outline
     */
    public void DrawCircleLinesV(Vector2 center, float radius, Color color) {
        context.rlgl.rlCheckRenderBatchLimit(2*36);

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

        // NOTE: Circle outline is drawn pixel by pixel every degree (0 to 360)
        for(int i = 0; i < 360; i += 10) {
            context.rlgl.rlVertex2f(center.x + (float) Math.cos(DEG2RAD * i) * radius, center.y + (float) Math.sin(DEG2RAD * i) * radius);
            context.rlgl.rlVertex2f(center.x + (float) Math.cos(DEG2RAD * (i + 10)) * radius, center.y + (float) Math.sin(DEG2RAD * (i + 10)) * radius);
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw ellipse
     *
     * @param centerX X coordinate of ellipse center
     * @param centerY Y coordinate of ellipse center
     * @param radiusH length of horizontal radius
     * @param radiusV length of vertical radius
     * @param color   color to draw ellipse
     */
    public void DrawEllipse(int centerX, int centerY, float radiusH, float radiusV, Color color) {
        context.rlgl.rlCheckRenderBatchLimit(3*36);

        context.rlgl.rlBegin(RL_TRIANGLES);
        for(int i = 0; i < 360; i += 10) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f((float) centerX, (float) centerY);
            context.rlgl.rlVertex2f((float) (centerX + Math.cos(DEG2RAD*(i + 10))*radiusH), (float) (centerY + Math.sin(DEG2RAD*(i + 10))*radiusV));
            context.rlgl.rlVertex2f((float) (centerX + Math.cos(DEG2RAD*i)*radiusH), (float) (centerY + Math.sin(DEG2RAD*i)*radiusV));
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw ellipse outline
     *
     * @param centerX X coordinate of ellipse center
     * @param centerY Y coordinate of ellipse center
     * @param radiusH length of horizontal radius
     * @param radiusV length of vertical radius
     * @param color   color to draw ellipse
     */
    public void DrawEllipseLines(int centerX, int centerY, float radiusH, float radiusV, Color color) {
        context.rlgl.rlCheckRenderBatchLimit(2*36);

        context.rlgl.rlBegin(RL_LINES);
        for(int i = 0; i < 360; i += 10) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f((float) (centerX + Math.cos(DEG2RAD*(i + 10))*radiusH), (float) (centerY + Math.sin(DEG2RAD*(i + 10))*radiusV));
            context.rlgl.rlVertex2f((float) (centerX + Math.cos(DEG2RAD*i)*radiusH), (float) (centerY + Math.sin(DEG2RAD*i)*radiusV));
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw ring
     *
     * @param center      X, Y coordinate for ring center
     * @param innerRadius length of inner radius
     * @param outerRadius length of outer radius
     * @param startAngle  angle to begin drawing
     * @param endAngle    angle to stop drawing
     * @param segments    number of segments
     * @param color       color to draw ring
     */
    public void DrawRing(Vector2 center, float innerRadius, float outerRadius, float startAngle, float endAngle,
                         int segments, Color color) {
        if(startAngle == endAngle) {
            return;
        }

        // Function expects (outerRadius > innerRadius)
        if(outerRadius < innerRadius) {
            float tmp = outerRadius;
            outerRadius = innerRadius;
            innerRadius = tmp;

            if(outerRadius <= 0.0f) {
                outerRadius = 0.1f;
            }
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle) {
            // Swap values
            float tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        int minSegments = (int)Math.ceil((endAngle - startAngle)/90);

        if(segments < minSegments) {
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / outerRadius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0) {
                segments = minSegments;
            }
        }

        // Not a ring
        if(innerRadius <= 0.0f) {
            DrawCircleSector(center, outerRadius, startAngle, endAngle, segments, color);
            return;
        }

        float stepLength = (endAngle - startAngle) / (float) segments;
        float angle = startAngle;

        if(SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlCheckRenderBatchLimit(4*segments);

            context.rlgl.rlSetTexture(texShapes.getId());

            context.rlgl.rlBegin(RL_QUADS);
            for(int i = 0; i < segments; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x +  Math.cos(DEG2RAD * angle) * outerRadius), (float) (center.y +  Math.sin(DEG2RAD * angle) * outerRadius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x +  Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius), (float) (center.y +  Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x +  Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius), (float) (center.y +  Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*outerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*outerRadius));

                angle += stepLength;
            }
            context.rlgl.rlEnd();

            context.rlgl.rlSetTexture(0);
        }
        else{
            context.rlgl.rlCheckRenderBatchLimit(6*segments);

            context.rlgl.rlBegin(RL_TRIANGLES);
            for(int i = 0; i < segments; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*innerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*innerRadius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*innerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*innerRadius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*outerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*outerRadius));

                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*innerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*innerRadius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*outerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*outerRadius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*outerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*outerRadius));
                angle += stepLength;
            }
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw ring outline
     *
     * @param center      X, Y coordinate for ring center
     * @param innerRadius length of inner radius
     * @param outerRadius length of outer radius
     * @param startAngle  angle to begin drawing
     * @param endAngle    angle to stop drawing
     * @param segments    number of segments
     * @param color       color to draw ring
     */
    public void DrawRingLines(Vector2 center, float innerRadius, float outerRadius, float startAngle, float endAngle,
                              int segments, Color color) {
        if(startAngle == endAngle) {
            return;
        }

        // Function expects (outerRadius > innerRadius)
        if(outerRadius < innerRadius) {
            float tmp = outerRadius;
            outerRadius = innerRadius;
            innerRadius = tmp;

            if(outerRadius <= 0.0f) {
                outerRadius = 0.1f;
            }
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle) {
            // Swap values
            float tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        int minSegments = (int)Math.ceil((endAngle - startAngle)/90);

        if(segments < minSegments) {
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / outerRadius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0) {
                segments = minSegments;
            }
        }

        if(innerRadius <= 0.0f) {
            DrawCircleSectorLines(center, outerRadius, startAngle, endAngle, segments, color);
            return;
        }

        float stepLength = (endAngle - startAngle) / (float) segments;
        float angle = startAngle;

        boolean showCapLines = true;
        int limit = 4 * (segments + 1);
        if((endAngle - startAngle) % 360 == 0) {
            limit = 4 * segments;
            showCapLines = false;
        }

        context.rlgl.rlCheckRenderBatchLimit(limit);

        context.rlgl.rlBegin(RL_LINES);
        if(showCapLines) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * outerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * outerRadius));
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * innerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * innerRadius));
        }

        for(int i = 0; i < segments; i++) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * outerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * outerRadius));
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius), (float) (center.y + Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius));

            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * innerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * innerRadius));
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius), (float) (center.y + Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius));

            angle += stepLength;
        }

        if(showCapLines) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * outerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * outerRadius));
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * innerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * innerRadius));
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw a color-filled rectangle
     *
     * @param posX   X coordinate of the rectangle (top left)
     * @param posY   Y coordinate of the rectangle (top left)
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param color  color to draw rectangle
     */
    public void DrawRectangle(int posX, int posY, int width, int height, Color color) {
        DrawRectangleV(new Vector2((float) posX, (float) posY), new Vector2((float) width, (float) height), color);
    }

    /**
     * Draw a color-filled rectangle (Vector version)
     * NOTE: On OpenGL 3.3 and ES2 we use QUADS to avoid drawing order issues (view rlglDraw)
     *
     * @param position X, Y coordinate pair
     * @param size     Width, Height pair
     * @param color    color to draw rectangle
     */
    public void DrawRectangleV(Vector2 position, Vector2 size, Color color) {
        DrawRectanglePro(new Rectangle(position.x, position.y, size.x, size.y),
                new Vector2(0.0f, 0.0f), 0.0f, color);
    }

    /**
     * Draw a color-filled rectangle
     *
     * @param rec   rectangle shape to draw
     * @param color color to draw rectangle
     */
    public void DrawRectangleRec(Rectangle rec, Color color) {
        DrawRectanglePro(rec, new Vector2(0.0f, 0.0f), 0.0f, color);
    }

    /**
     * Draw a color-filled rectangle with pro parameters
     *
     * @param rec      rectangle shape to draw
     * @param origin   X, Y coordinate
     * @param rotation degrees to rotate rectangle
     * @param color    color to draw rectangle
     */
    public void DrawRectanglePro(Rectangle rec, Vector2 origin, float rotation, Color color) {
        Vector2 topLeft = new Vector2();
        Vector2 topRight = new Vector2();
        Vector2 bottomLeft = new Vector2();
        Vector2 bottomRight = new Vector2();

        // Only calculate rotation if needed
        if (rotation == 0.0f) {
            float x = rec.x - origin.x;
            float y = rec.y - origin.y;
            topLeft = new Vector2(x, y);
            topRight = new Vector2(x + rec.width, y);
            bottomLeft = new Vector2(x, y + rec.height);
            bottomRight = new Vector2(x + rec.width, y + rec.height);
        }
        else {
            float sinRotation = (float) Math.sin(rotation*DEG2RAD);
            float cosRotation = (float) Math.cos(rotation*DEG2RAD);
            float x = rec.x;
            float y = rec.y;
            float dx = -origin.x;
            float dy = -origin.y;

            topLeft.x = x + dx*cosRotation - dy*sinRotation;
            topLeft.y = y + dx*sinRotation + dy*cosRotation;

            topRight.x = x + (dx + rec.width)*cosRotation - dy*sinRotation;
            topRight.y = y + (dx + rec.width)*sinRotation + dy*cosRotation;

            bottomLeft.x = x + dx*cosRotation - (dy + rec.height)*sinRotation;
            bottomLeft.y = y + dx*sinRotation + (dy + rec.height)*cosRotation;

            bottomRight.x = x + (dx + rec.width)*cosRotation - (dy + rec.height)*sinRotation;
            bottomRight.y = y + (dx + rec.width)*sinRotation + (dy + rec.height)*cosRotation;
        }

        if (SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlCheckRenderBatchLimit(4);

            context.rlgl.rlSetTexture(texShapes.getId());
            context.rlgl.rlBegin(RL_QUADS);

            context.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(topLeft.x, topLeft.y);

            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(bottomLeft.x, bottomLeft.y);

            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width,
                    (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(bottomRight.x, bottomRight.y);

            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(topRight.x, topRight.y);

            context.rlgl.rlEnd();
            context.rlgl.rlSetTexture(0);
        }
        else {
            context.rlgl.rlCheckRenderBatchLimit(6);

            context.rlgl.rlBegin(RL_TRIANGLES);

            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlVertex2f(topLeft.x, topLeft.y);
            context.rlgl.rlVertex2f(bottomLeft.x, bottomLeft.y);
            context.rlgl.rlVertex2f(topRight.x, topRight.y);

            context.rlgl.rlVertex2f(topRight.x, topRight.y);
            context.rlgl.rlVertex2f(bottomLeft.x, bottomLeft.y);
            context.rlgl.rlVertex2f(bottomRight.x, bottomRight.y);

            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a vertical-gradient-filled rectangle
     *
     * @param posX   X coordinate position
     * @param posY   Y coordinate position
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param color1 color at the beginning of the gradient (bottom)
     * @param color2 color at the end of the gradient (top)
     */
    public void DrawRectangleGradientV(int posX, int posY, int width, int height, Color color1, Color color2) {
        DrawRectangleGradientEx(new Rectangle((float) posX, (float) posY, (float) width, (float) height), color1, color2, color2, color1);
    }

    /**
     * Draw a vertical-gradient-filled rectangle
     *
     * @param posX   X coordinate position
     * @param posY   Y coordinate position
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param color1 color at the beginning of the gradient (left)
     * @param color2 color at the end of the gradient (right)
     */
    public void DrawRectangleGradientH(int posX, int posY, int width, int height, Color color1, Color color2) {
        DrawRectangleGradientEx(new Rectangle((float) posX, (float) posY, (float) width, (float) height), color1, color1, color2, color2);
    }

    // Draw a gradient-filled rectangle
    // NOTE: Colors refer to corners, starting at top-lef corner and counter-clockwise
    public void DrawRectangleGradientEx(Rectangle rec, Color col1, Color col2, Color col3, Color col4) {
        context.rlgl.rlCheckRenderBatchLimit(4);
        context.rlgl.rlSetTexture(texShapes.getId());

        context.rlgl.rlBegin(RL_QUADS);
        context.rlgl.rlNormal3f(0.0f, 0.0f, 1.0f);
        // NOTE: Default raylib font character 95 is a white square
        context.rlgl.rlColor4ub(col1.r, col1.g, col1.b, col1.a);
        context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
        context.rlgl.rlVertex2f(rec.x, rec.y);
        context.rlgl.rlColor4ub(col2.r, col2.g, col2.b, col2.a);
        context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
        context.rlgl.rlVertex2f(rec.x, rec.y + rec.height);
        context.rlgl.rlColor4ub(col3.r, col3.g, col3.b, col3.a);
        context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
        context.rlgl.rlVertex2f(rec.x + rec.width, rec.y + rec.height);
        context.rlgl.rlColor4ub(col4.r, col4.g, col4.b, col4.a);
        context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
        context.rlgl.rlVertex2f(rec.x + rec.width, rec.y);


        context.rlgl.rlSetTexture(0);
    }

    /**
     * Draw rectangle outline
     * NOTE: On OpenGL 3.3 and ES2 we use QUADS to avoid drawing order issues (view rlglDraw)
     *
     * @param posX   X coordinate of rectangle position
     * @param posY   Y coordinate of rectangle position
     * @param width  Width of rectangle
     * @param height Height of rectangle
     * @param color  Color to draw rectangle
     */
    public void DrawRectangleLines(int posX, int posY, int width, int height, Color color) {
        if(SUPPORT_QUADS_DRAW_MODE) {
            DrawRectangle(posX, posY, width, 1, color);
            DrawRectangle(posX + width - 1, posY + 1, 1, height - 2, color);
            DrawRectangle(posX, posY + height - 1, width, 1, color);
            DrawRectangle(posX, posY + 1, 1, height - 2, color);
        }
        else{
            context.rlgl.rlBegin(RL_LINES);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2i(posX + 1, posY + 1);
            context.rlgl.rlVertex2i(posX + width, posY + 1);

            context.rlgl.rlVertex2i(posX + width, posY + 1);
            context.rlgl.rlVertex2i(posX + width, posY + height);

            context.rlgl.rlVertex2i(posX + width, posY + height);
            context.rlgl.rlVertex2i(posX + 1, posY + height);

            context.rlgl.rlVertex2i(posX + 1, posY + height);
            context.rlgl.rlVertex2i(posX + 1, posY + 1);
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw rectangle outline with extended parameters
     *
     * @param rec       Rectangle to draw
     * @param lineThick Thickness of rectangle lines
     * @param color     Color to draw rectangle
     */
    public void DrawRectangleLinesEx(Rectangle rec, float lineThick, Color color) {
        if(lineThick > rec.width || lineThick > rec.height) {
            if(rec.width > rec.height) {
                lineThick = rec.height / 2;
            }
            else if(rec.width < rec.height) {
                lineThick = rec.width / 2;
            }
        }

        // When rec = { x, y, 8.0f, 6.0f } and lineThick = 2, the following
        // four rectangles are drawn ([T]op, [B]ottom, [L]eft, [R]ight):
        //
        //   TTTTTTTT
        //   TTTTTTTT
        //   LL    RR
        //   LL    RR
        //   BBBBBBBB
        //   BBBBBBBB
        //
        Rectangle top = new Rectangle(rec.x, rec.y, rec.width, lineThick);
        Rectangle bottom = new Rectangle(rec.x, rec.y - lineThick + rec.height, rec.width, lineThick);
        Rectangle left = new Rectangle(rec.x, rec.y + lineThick, lineThick, rec.height - lineThick*2.0f);
        Rectangle right = new Rectangle(rec.x - lineThick + rec.width, rec.y + lineThick, lineThick, rec.height - lineThick*2.0f);

        DrawRectangleRec(top, color);
        DrawRectangleRec(bottom, color);
        DrawRectangleRec(left, color);
        DrawRectangleRec(right, color);
    }

    /**
     * Draw rectangle with rounded edges
     *
     * @param rec       Rectangle to draw
     * @param roundness degree to round corners
     * @param segments  number of segments
     * @param color     color to draw rectangle
     */
    public void DrawRectangleRounded(Rectangle rec, float roundness, int segments, Color color) {
        // Not a rounded rectangle
        if((roundness <= 0.0f) || (rec.width < 1) || (rec.height < 1)) {
            DrawRectangleRec(rec, color);
            return;
        }

        if(roundness >= 1.0f) {
            roundness = 1.0f;
        }

        // Calculate corner radius
        float radius = (rec.width > rec.height) ? (rec.height * roundness) / 2 : (rec.width * roundness) / 2;
        if(radius <= 0.0f) {
            return;
        }

        // Calculate number of segments to use for the corners
        if(segments < 4) {
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) (Math.ceil(2 * PI / th) / 4.0f);
            if(segments <= 0) {
                segments = 4;
            }
        }

        float stepLength = 90.0f / (float) segments;

        /*
            Quick sketch to make sense of all of this,
            there are 9 parts to draw, also mark the 12 points we'll use

                  P0____________________P1
                  /|                    |\
                 /1|          2         |3\
             P7 /__|____________________|__\ P2
               |   |P8                P9|   |
               | 8 |          9         | 4 |
               | __|____________________|__ |
             P6 \  |P11              P10|  / P3
                 \7|          6         |5/
                  \|____________________|/
                  P5                    P4
        */

        // coordinates of the 12 points that define the rounded rect (the idea here is to make things easier)
        Vector2[] point = new Vector2[]{
                new Vector2(rec.x + radius, rec.y),
                new Vector2((rec.x + rec.width) - radius, rec.y),
                new Vector2(rec.x + rec.width, rec.y + radius),
                new Vector2(rec.x + rec.width, (rec.y + rec.height) - radius),
                new Vector2((rec.x + rec.width) - radius, rec.y + rec.height),
                new Vector2(rec.x + radius, rec.y + rec.height),
                new Vector2(rec.x, (rec.y + rec.height) - radius),
                new Vector2(rec.x, rec.y + radius),
                new Vector2(rec.x + radius, rec.y + radius),
                new Vector2((rec.x + rec.width) - radius, rec.y + radius),
                new Vector2((rec.x + rec.width) - radius, (rec.y + rec.height) - radius),
                new Vector2(rec.x + radius, (rec.y + rec.height) - radius)
        };

        Vector2[] centers = {
                point[8], point[9], point[10], point[11]
        };
        float[] angles = {
                180.0f, 270.0f, 0.0f, 90.0f
        };

        if(SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlCheckRenderBatchLimit(16*segments/2 + 5*4);

            context.rlgl.rlSetTexture(texShapes.getId());

            context.rlgl.rlBegin(RL_QUADS);
            // Draw all of the 4 corners: [1] Upper Left Corner, [3] Upper Right Corner, [5] Lower Right Corner, [7] Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];
                // NOTE: Every QUAD actually represents two segments
                for(int i = 0; i < segments / 2; i++) {
                    context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                    context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                    context.rlgl.rlVertex2f(center.x, center.y);

                    context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * radius), (float) (center.y + Math.sin(DEG2RAD * angle) * radius));

                    context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * (angle + stepLength)) * radius), (float) (center.y + Math.sin(DEG2RAD * (angle + stepLength)) * radius));

                    context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * (angle + stepLength * 2)) * radius), (float) (center.y + Math.sin(DEG2RAD * (angle + stepLength * 2)) * radius));

                    angle += (stepLength * 2);
                }
                // NOTE: In case number of segments is odd, we add one last piece to the cake
                if(segments % 2 == 1) {
                    context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                    context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
                    context.rlgl.rlVertex2f(center.x, center.y);

                    context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*radius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*radius));

                    context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*radius), (float) (center.y + Math.sin(DEG2RAD*angle)*radius));

                    context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
                    context.rlgl.rlVertex2f(center.x, center.y);
                }
            }

            // [2] Upper Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[0].x, point[0].y);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[1].x, point[1].y);

            // [4] Right Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[2].x, point[2].y);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[3].x, point[3].y);

            // [6] Bottom Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[5].x, point[5].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[4].x, point[4].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);

            // [8] Left Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[7].x, point[7].y);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[6].x, point[6].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);

            // [9] Middle Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);
            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);
            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);

            context.rlgl.rlEnd();
            context.rlgl.rlSetTexture(0);
        }
        else{
            context.rlgl.rlCheckRenderBatchLimit(12 * segments + 5 * 6);

            context.rlgl.rlBegin(RL_TRIANGLES);
            // Draw all of the 4 corners: [1] Upper Left Corner, [3] Upper Right Corner, [5] Lower Right Corner, [7] Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];
                for(int i = 0; i < segments; i++) {
                    context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                    context.rlgl.rlVertex2f(center.x, center.y);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*radius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*radius));
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*radius), (float) (center.y + Math.sin(DEG2RAD*angle)*radius));
                    angle += stepLength;
                }
            }

            // [2] Upper Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(point[0].x, point[0].y);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);
            context.rlgl.rlVertex2f(point[1].x, point[1].y);
            context.rlgl.rlVertex2f(point[0].x, point[0].y);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);

            // [4] Right Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);
            context.rlgl.rlVertex2f(point[3].x, point[3].y);
            context.rlgl.rlVertex2f(point[2].x, point[2].y);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);
            context.rlgl.rlVertex2f(point[3].x, point[3].y);

            // [6] Bottom Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlVertex2f(point[5].x, point[5].y);
            context.rlgl.rlVertex2f(point[4].x, point[4].y);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlVertex2f(point[4].x, point[4].y);

            // [8] Left Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(point[7].x, point[7].y);
            context.rlgl.rlVertex2f(point[6].x, point[6].y);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);
            context.rlgl.rlVertex2f(point[7].x, point[7].y);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);

            // [9] Middle Rectangle
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);
            context.rlgl.rlVertex2f(point[11].x, point[11].y);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);
            context.rlgl.rlVertex2f(point[9].x, point[9].y);
            context.rlgl.rlVertex2f(point[8].x, point[8].y);
            context.rlgl.rlVertex2f(point[10].x, point[10].y);
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw rectangle outline with rounded edges
     *
     * @param rec       Rectangle to draw
     * @param roundness degree to round corners
     * @param segments  number of segments
     * @param lineThick thickness of lines
     * @param color     color to draw rectangle
     */
    public void DrawRectangleRoundedLines(Rectangle rec, float roundness, int segments, float lineThick, Color color) {
        if(lineThick < 0) {
            lineThick = 0;
        }

        // Not a rounded rectangle
        if(roundness <= 0.0f) {
            DrawRectangleLinesEx(new Rectangle(rec.x - lineThick, rec.y - lineThick, rec.width + 2 * lineThick, rec.height + 2 * lineThick), lineThick, color);
            return;
        }

        if(roundness >= 1.0f) {
            roundness = 1.0f;
        }

        // Calculate corner radius
        float radius = (rec.width > rec.height) ? (rec.height * roundness) / 2 : (rec.width * roundness) / 2;
        if(radius <= 0.0f) {
            return;
        }

        // Calculate number of segments to use for the corners
        if(segments < 4) {
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) (Math.ceil(2 * PI / th) / 2.0f);
            if(segments <= 0) {
                segments = 4;
            }
        }

        float stepLength = 90.0f / (float) segments;
        float outerRadius = radius + lineThick, innerRadius = radius;

        /*
            Quick sketch to make sense of all of this,
            marks the 16 + 4(corner centers P16-19) points we'll use

                   P0 ================== P1
                  // P8                P9 \\
                 //                        \\
             P7 // P15                  P10 \\ P2
               ||   *P16             P17*    ||
               ||                            ||
               || P14                   P11  ||
             P6 \\  *P19             P18*   // P3
                 \\                        //
                  \\ P13              P12 //
                   P5 ================== P4
        */
        Vector2[] point = {
                new Vector2(rec.x + innerRadius, rec.y - lineThick),
                new Vector2((rec.x + rec.width) - innerRadius, rec.y - lineThick),
                new Vector2(rec.x + rec.width + lineThick, rec.y + innerRadius), // PO, P1, P2
                new Vector2(rec.x + rec.width + lineThick, (rec.y + rec.height) - innerRadius),
                new Vector2((rec.x + rec.width) - innerRadius, rec.y + rec.height + lineThick), // P3, P4
                new Vector2(rec.x + innerRadius, rec.y + rec.height + lineThick),
                new Vector2(rec.x - lineThick, (rec.y + rec.height) - innerRadius),
                new Vector2(rec.x - lineThick, rec.y + innerRadius), // P5, P6, P7
                new Vector2(rec.x + innerRadius, rec.y),
                new Vector2((rec.x + rec.width) - innerRadius, rec.y), // P8, P9
                new Vector2(rec.x + rec.width, rec.y + innerRadius),
                new Vector2(rec.x + rec.width, (rec.y + rec.height) - innerRadius), // P10, P11
                new Vector2((rec.x + rec.width) - innerRadius, rec.y + rec.height),
                new Vector2(rec.x + innerRadius, rec.y + rec.height), // P12, P13
                new Vector2(rec.x, (rec.y + rec.height) - innerRadius),
                new Vector2(rec.x, rec.y + innerRadius) // P14, P15
        };

        Vector2[] centers = {
                new Vector2(rec.x + innerRadius, rec.y + innerRadius),
                new Vector2((rec.x + rec.width) - innerRadius, rec.y + innerRadius), // P16, P17
                new Vector2(rec.x + rec.width - innerRadius, (rec.y + rec.height) - innerRadius),
                new Vector2(rec.x + innerRadius, (rec.y + rec.height) - innerRadius) // P18, P19
        };

        float[] angles = {180.0f, 270.0f, 0.0f, 90.0f};

        if(lineThick > 1) {
            if(SUPPORT_QUADS_DRAW_MODE) {
                context.rlgl.rlCheckRenderBatchLimit(4 * 4 * segments + 4 * 4);

                context.rlgl.rlSetTexture(texShapes.getId());

                context.rlgl.rlBegin(RL_QUADS);
                // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
                for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
                {
                    float angle = angles[k];
                    Vector2 center = centers[k];
                    for(int i = 0; i < segments; i++) {
                        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                        context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*innerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*innerRadius));

                        context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*innerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*innerRadius));

                        context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*outerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*outerRadius));

                        context.rlgl.rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*outerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*outerRadius));

                        angle += stepLength;
                    }
                }

                // Upper rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[0].x, point[0].y);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[8].x, point[8].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[9].x, point[9].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[1].x, point[1].y);

                // Right rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[2].x, point[2].y);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[10].x, point[10].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[11].x, point[11].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[3].x, point[3].y);

                // Lower rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[13].x, point[13].y);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[5].x, point[5].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[4].x, point[4].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[12].x, point[12].y);

                // Left rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[15].x, point[15].y);
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[7].x, point[7].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(point[6].x, point[6].y);
                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(point[14].x, point[14].y);

                context.rlgl.rlEnd();
                context.rlgl.rlSetTexture(0);
            }
            else{
                context.rlgl.rlCheckRenderBatchLimit(4 * 6 * segments + 4 * 6);

                context.rlgl.rlBegin(RL_TRIANGLES);

                // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
                for(int k = 0; k < 4; ++k) {
                    float angle = angles[k];
                    Vector2 center = centers[k];

                    for(int i = 0; i < segments; i++) {
                        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*innerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*innerRadius));
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*innerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*innerRadius));
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*outerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*outerRadius));

                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*innerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*innerRadius));
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*(angle + stepLength))*outerRadius), (float) (center.y + Math.sin(DEG2RAD*(angle + stepLength))*outerRadius));
                        context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD*angle)*outerRadius), (float) (center.y + Math.sin(DEG2RAD*angle)*outerRadius));
                        angle += stepLength;
                    }
                }

                // Upper rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlVertex2f(point[0].x, point[0].y);
                context.rlgl.rlVertex2f(point[8].x, point[8].y);
                context.rlgl.rlVertex2f(point[9].x, point[9].y);
                context.rlgl.rlVertex2f(point[1].x, point[1].y);
                context.rlgl.rlVertex2f(point[0].x, point[0].y);
                context.rlgl.rlVertex2f(point[9].x, point[9].y);

                // Right rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlVertex2f(point[10].x, point[10].y);
                context.rlgl.rlVertex2f(point[11].x, point[11].y);
                context.rlgl.rlVertex2f(point[3].x, point[3].y);
                context.rlgl.rlVertex2f(point[2].x, point[2].y);
                context.rlgl.rlVertex2f(point[10].x, point[10].y);
                context.rlgl.rlVertex2f(point[3].x, point[3].y);

                // Lower rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlVertex2f(point[13].x, point[13].y);
                context.rlgl.rlVertex2f(point[5].x, point[5].y);
                context.rlgl.rlVertex2f(point[4].x, point[4].y);
                context.rlgl.rlVertex2f(point[12].x, point[12].y);
                context.rlgl.rlVertex2f(point[13].x, point[13].y);
                context.rlgl.rlVertex2f(point[4].x, point[4].y);

                // Left rectangle
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlVertex2f(point[7].x, point[7].y);
                context.rlgl.rlVertex2f(point[6].x, point[6].y);
                context.rlgl.rlVertex2f(point[14].x, point[14].y);
                context.rlgl.rlVertex2f(point[15].x, point[15].y);
                context.rlgl.rlVertex2f(point[7].x, point[7].y);
                context.rlgl.rlVertex2f(point[14].x, point[14].y);
                context.rlgl.rlEnd();
            }
        }
        else{
            // Use LINES to draw the outline
            context.rlgl.rlCheckRenderBatchLimit(8 * segments + 4 * 2);

            context.rlgl.rlBegin(RL_LINES);

            // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];

                for(int i = 0; i < segments; i++) {
                    context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * angle) * outerRadius), (float) (center.y + Math.sin(DEG2RAD * angle) * outerRadius));
                    context.rlgl.rlVertex2f((float) (center.x + Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius), (float) (center.y + Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius));
                    angle += stepLength;
                }
            }

            // And now the remaining 4 lines
            for(int i = 0; i < 8; i += 2) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                context.rlgl.rlVertex2f(point[i].x, point[i].y);
                context.rlgl.rlVertex2f(point[i + 1].x, point[i + 1].y);
            }

            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a triangle
     * NOTE: Vertex must be provided in counter-clockwise order
     *
     * @param v1    X,Y coordinate (top vertex)
     * @param v2    X,Y coordinate (left vertex)
     * @param v3    X,Y coordinate (right vertex)
     * @param color color to draw triangle
     */
    public void DrawTriangle(Vector2 v1, Vector2 v2, Vector2 v3, Color color) {

        if(SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlCheckRenderBatchLimit(4);

            context.rlgl.rlSetTexture(texShapes.getId());

            context.rlgl.rlBegin(RL_QUADS);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(v1.x, v1.y);

            context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(v2.x, v2.y);

            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            context.rlgl.rlVertex2f(v2.x, v2.y);

            context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            context.rlgl.rlVertex2f(v3.x, v3.y);
            context.rlgl.rlEnd();

            context.rlgl.rlSetTexture(0);
        }
        else{
            context.rlgl.rlCheckRenderBatchLimit(3);

            context.rlgl.rlBegin(RL_TRIANGLES);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
            context.rlgl.rlVertex2f(v1.x, v1.y);
            context.rlgl.rlVertex2f(v2.x, v2.y);
            context.rlgl.rlVertex2f(v3.x, v3.y);
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a triangle using lines
     * NOTE: Vertex must be provided in counter-clockwise order
     *
     * @param v1    X,Y coordinate (top vertex)
     * @param v2    X,Y coordinate (left vertex)
     * @param v3    X,Y coordinate (right vertex)
     * @param color color to draw triangle
     */
    public void DrawTriangleLines(Vector2 v1, Vector2 v2, Vector2 v3, Color color) {
        context.rlgl.rlCheckRenderBatchLimit(6);

        context.rlgl.rlBegin(RL_LINES);
        context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
        context.rlgl.rlVertex2f(v1.x, v1.y);
        context.rlgl.rlVertex2f(v2.x, v2.y);

        context.rlgl.rlVertex2f(v2.x, v2.y);
        context.rlgl.rlVertex2f(v3.x, v3.y);

        context.rlgl.rlVertex2f(v3.x, v3.y);
        context.rlgl.rlVertex2f(v1.x, v1.y);
        context.rlgl.rlEnd();
    }

    /**
     * Draw a triangle fan defined by points
     * NOTE: First vertex provided is the center, shared by all triangles
     * By default, following vertex should be provided in counter-clockwise order
     *
     * @param points      Array of X, Y coordinates
     * @param pointsCount number of points
     * @param color       color to draw fan
     */
    public void DrawTriangleFan(Vector2[] points, int pointsCount, Color color) {
        if(pointsCount >= 3) {
            context.rlgl.rlCheckRenderBatchLimit((pointsCount - 2) * 4);

            context.rlgl.rlSetTexture(texShapes.getId());
            context.rlgl.rlBegin(RL_QUADS);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            for(int i = 1; i < pointsCount - 1; i++) {
                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(points[0].x, points[0].y);

                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(points[i].x, points[i].y);

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f(points[i + 1].x, points[i + 1].y);

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(points[i + 1].x, points[i + 1].y);
            }
            context.rlgl.rlEnd();
            context.rlgl.rlSetTexture(0);
        }
    }

    /**
     * Draw a triangle strip defined by points
     * NOTE: Every new vertex connects with previous two
     *
     * @param points      Array of X, Y coordinates
     * @param color       color to draw strip
     */
    public void DrawTriangleStrip(Vector2[] points, Color color) {
        if (points.length >= 3) {
            context.rlgl.rlBegin(RL_TRIANGLES);
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            for (int i = 2; i < points.length; i++) {
                if ((i%2) == 0) {
                    context.rlgl.rlVertex2f(points[i].x, points[i].y);
                    context.rlgl.rlVertex2f(points[i - 2].x, points[i - 2].y);
                    context.rlgl.rlVertex2f(points[i - 1].x, points[i - 1].y);
                }
                else {
                    context.rlgl.rlVertex2f(points[i].x, points[i].y);
                    context.rlgl.rlVertex2f(points[i - 1].x, points[i - 1].y);
                    context.rlgl.rlVertex2f(points[i - 2].x, points[i - 2].y);
                }
            }
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a regular polygon of n sides
     *
     * @param center   X, Y coordinates of polygon center
     * @param sides    number of sides
     * @param radius   length of polygon radius
     * @param rotation degrees to rotate polygon
     * @param color    Color to draw polygon
     */
    public void DrawPoly(Vector2 center, int sides, float radius, float rotation, Color color) {
        if (sides < 3) {
            sides = 3;
        }
        float centralAngle = rotation*DEG2RAD;
        float angleStep = 360.0f/(float)sides*DEG2RAD;

        if(SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlSetTexture(texShapes.id);

            context.rlgl.rlBegin(RL_QUADS);
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                float nextAngle = centralAngle + angleStep;

                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f(center.x, center.y);

                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * radius), (float) (center.y + Math.sin(centralAngle) * radius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(nextAngle) * radius), (float) (center.y + Math.sin(nextAngle) * radius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * radius), (float) (center.y + Math.sin(centralAngle) * radius));

                centralAngle = nextAngle;
            }
            context.rlgl.rlEnd();
            context.rlgl.rlSetTexture(0);
        }
        else {
            context.rlgl.rlBegin(RL_TRIANGLES);
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

                context.rlgl.rlVertex2f(center.x, center.y);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle + angleStep) * radius), (float) (center.y + Math.sin(centralAngle + angleStep) * radius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * radius), (float) (center.y + Math.sin(centralAngle) * radius));

                centralAngle += angleStep;
            }
            context.rlgl.rlEnd();
        }
    }

    /**
     * Draw a polygon outline of n sides
     *
     * @param center   X, Y coordinates of polygon center
     * @param sides    number of sides
     * @param radius   length of polygon radius
     * @param rotation degrees to rotate polygon
     * @param color    Color to draw polygon
     */
    public void DrawPolyLines(Vector2 center, int sides, float radius, float rotation, Color color) {
        if (sides < 3) {
            sides = 3;
        }
        float centralAngle = rotation*DEG2RAD;
        float angleStep = 360.0f/(float)sides*DEG2RAD;

        context.rlgl.rlBegin(RL_LINES);
        for (int i = 0; i < sides; i++) {
            context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);

            context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle)*radius), (float) (center.y + Math.sin(centralAngle)*radius));
            context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle + angleStep)*radius), (float) (center.y + Math.sin(centralAngle + angleStep)*radius));

            centralAngle += angleStep;
        }
        context.rlgl.rlEnd();
    }

    /**
     * Draw a polygon outline of n sides
     *
     * @param center   X, Y coordinates of polygon center
     * @param sides    number of sides
     * @param radius   length of polygon radius
     * @param rotation degrees to rotate polygon
     * @param lineThick thickness of lines in pixels
     * @param color    Color to draw polygon
     */
    public void DrawPolyLinesEx(Vector2 center, int sides, float radius, float rotation, float lineThick, Color color) {
        if (sides < 3) sides = 3;
        float centralAngle = rotation*DEG2RAD;
        float exteriorAngle = 360.0f/(float)sides*DEG2RAD;
        float innerRadius = (float) (radius - (lineThick*Math.cos(DEG2RAD*exteriorAngle/2.0f)));

        if (SUPPORT_QUADS_DRAW_MODE) {
            context.rlgl.rlSetTexture(texShapes.id);

            context.rlgl.rlBegin(RL_QUADS);
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                float nextAngle = centralAngle + exteriorAngle;

                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * radius), (float) (center.y + Math.sin(centralAngle) * radius));

                context.rlgl.rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * innerRadius), (float) (center.y + Math.sin(centralAngle) * innerRadius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(nextAngle) * innerRadius), (float) (center.y + Math.sin(nextAngle) * innerRadius));

                context.rlgl.rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(nextAngle) * radius), (float) (center.y + Math.sin(nextAngle) * radius));

                centralAngle = nextAngle;
            }
            context.rlgl.rlEnd();
            context.rlgl.rlSetTexture(0);
        }
        else {
            context.rlgl.rlBegin(RL_TRIANGLES);
            for (int i = 0; i < sides; i++) {
                context.rlgl.rlColor4ub(color.r, color.g, color.b, color.a);
                float nextAngle = centralAngle + exteriorAngle;

                context.rlgl.rlVertex2f((float) (center.x + Math.cos(nextAngle) * radius), (float) (center.y + Math.sin(nextAngle) * radius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * radius), (float) (center.y + Math.sin(centralAngle) * radius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * innerRadius), (float) (center.y + Math.sin(centralAngle) * innerRadius));

                context.rlgl.rlVertex2f((float) (center.x + Math.cos(centralAngle) * innerRadius), (float) (center.y + Math.sin(centralAngle) * innerRadius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(nextAngle) * innerRadius), (float) (center.y + Math.sin(nextAngle) * innerRadius));
                context.rlgl.rlVertex2f((float) (center.x + Math.cos(nextAngle) * radius), (float) (center.y + Math.sin(nextAngle) * radius));

                centralAngle = nextAngle;
            }
            context.rlgl.rlEnd();
        }
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Splines functions
    //----------------------------------------------------------------------------------

    /**
     * Draw spline: linear, minimum 2 points
     * @param points Array of X, Y points
     * @param thick spline thickness in pixels
     * @param color spline color
     */
    public void DrawSplineLinear(Vector2[] points, float thick, Color color) {
        Vector2 delta = new Vector2();
        float length = 0.0f;
        float scale = 0.0f;

        for (int i = 0; i < points.length - 1; i++) {
            delta = new Vector2(points[i + 1].x - points[i].x, points[i + 1].y - points[i].y);
            length = (float) Math.sqrt(delta.x*delta.x + delta.y*delta.y);

            if (length > 0) scale = thick/(2*length);

            Vector2 radius = new Vector2(-scale*delta.y, scale*delta.x);
            Vector2[] strip = {
                new Vector2(points[i].x - radius.x, points[i].y - radius.y),
                new Vector2(points[i].x + radius.x, points[i].y + radius.y),
                new Vector2(points[i + 1].x - radius.x, points[i + 1].y - radius.y),
                new Vector2(points[i + 1].x + radius.x, points[i + 1].y + radius.y)
            };

            DrawTriangleStrip(strip, color);
        }
    }

    /**
     * Draw spline: B-Spline, minimum 4 points
     * @param points
     * @param thick
     * @param color
     */
    public void DrawSplineBasis(Vector2[] points, float thick, Color color) {
        if (points.length < 4) {
            return;
        }

        float[] a = new float[4];
        float[] b = new float[4];
        float dy = 0.0f;
        float dx = 0.0f;
        float size = 0.0f;

        Vector2 currentPoint = new Vector2();
        Vector2 nextPoint = new Vector2();
        Vector2[] vertices = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vector2();
        }

        for (int i = 0; i < (points.length - 3); i++) {
            float t = 0.0f;
            Vector2 p1 = points[i], p2 = points[i + 1], p3 = points[i + 2], p4 = points[i + 3];

            a[0] = (-p1.x + 3.0f*p2.x - 3.0f*p3.x + p4.x)/6.0f;
            a[1] = (3.0f*p1.x - 6.0f*p2.x + 3.0f*p3.x)/6.0f;
            a[2] = (-3.0f*p1.x + 3.0f*p3.x)/6.0f;
            a[3] = (p1.x + 4.0f*p2.x + p3.x)/6.0f;

            b[0] = (-p1.y + 3.0f*p2.y - 3.0f*p3.y + p4.y)/6.0f;
            b[1] = (3.0f*p1.y - 6.0f*p2.y + 3.0f*p3.y)/6.0f;
            b[2] = (-3.0f*p1.y + 3.0f*p3.y)/6.0f;
            b[3] = (p1.y + 4.0f*p2.y + p3.y)/6.0f;

            currentPoint.x = a[3];
            currentPoint.y = b[3];

            if (i == 0) {
                DrawCircleV(currentPoint, thick/2.0f, color);   // Draw init line circle-cap
            }

            if (i > 0) {
                vertices[0].x = currentPoint.x + dy*size;
                vertices[0].y = currentPoint.y - dx*size;
                vertices[1].x = currentPoint.x - dy*size;
                vertices[1].y = currentPoint.y + dx*size;
            }

            for (int j = 1; j <= SPLINE_SEGMENT_DIVISIONS; j++) {
                t = ((float)j)/((float)SPLINE_SEGMENT_DIVISIONS);

                nextPoint.x = a[3] + t*(a[2] + t*(a[1] + t*a[0]));
                nextPoint.y = b[3] + t*(b[2] + t*(b[1] + t*b[0]));

                dy = nextPoint.y - currentPoint.y;
                dx = nextPoint.x - currentPoint.x;
                size = (float) (0.5f*thick/Math.sqrt(dx*dx+dy*dy));

                if ((i == 0) && (j == 1)) {
                    vertices[0].x = currentPoint.x + dy*size;
                    vertices[0].y = currentPoint.y - dx*size;
                    vertices[1].x = currentPoint.x - dy*size;
                    vertices[1].y = currentPoint.y + dx*size;
                }

                vertices[2*j + 1].x = nextPoint.x - dy*size;
                vertices[2*j + 1].y = nextPoint.y + dx*size;
                vertices[2*j].x = nextPoint.x + dy*size;
                vertices[2*j].y = nextPoint.y - dx*size;

                currentPoint = nextPoint;
            }

            DrawTriangleStrip(vertices, color);
        }

        DrawCircleV(currentPoint, thick/2.0f, color);   // Draw end line circle-cap
    }

    /**
     * Draw spline: Catmull-Rom, minimum 4 points
     * @param points
     * @param thick
     * @param color
     */
    public void DrawSplineCatmullRom(Vector2[] points, float thick, Color color) {
        if (points.length < 4) {
            return;
        }

        float dy = 0.0f;
        float dx = 0.0f;
        float size = 0.0f;

        Vector2 currentPoint = points[1];
        Vector2 nextPoint = new Vector2();
        Vector2[] vertices = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vector2();
        }
        
        DrawCircleV(currentPoint, thick/2.0f, color);   // Draw init line circle-cap

        for (int i = 0; i < (points.length - 3); i++) {
            float t = 0.0f;
            Vector2 p1 = points[i], p2 = points[i + 1], p3 = points[i + 2], p4 = points[i + 3];

            if (i > 0) {
                vertices[0].x = currentPoint.x + dy*size;
                vertices[0].y = currentPoint.y - dx*size;
                vertices[1].x = currentPoint.x - dy*size;
                vertices[1].y = currentPoint.y + dx*size;
            }

            for (int j = 1; j <= SPLINE_SEGMENT_DIVISIONS; j++) {
                t = ((float)j)/((float)SPLINE_SEGMENT_DIVISIONS);

                float q0 = (-1.0f*t*t*t) + (2.0f*t*t) + (-1.0f*t);
                float q1 = (3.0f*t*t*t) + (-5.0f*t*t) + 2.0f;
                float q2 = (-3.0f*t*t*t) + (4.0f*t*t) + t;
                float q3 = t*t*t - t*t;

                nextPoint.x = 0.5f*((p1.x*q0) + (p2.x*q1) + (p3.x*q2) + (p4.x*q3));
                nextPoint.y = 0.5f*((p1.y*q0) + (p2.y*q1) + (p3.y*q2) + (p4.y*q3));

                dy = nextPoint.y - currentPoint.y;
                dx = nextPoint.x - currentPoint.x;
                size = (float) ((0.5f*thick)/Math.sqrt(dx*dx + dy*dy));

                if ((i == 0) && (j == 1)) {
                    vertices[0].x = currentPoint.x + dy*size;
                    vertices[0].y = currentPoint.y - dx*size;
                    vertices[1].x = currentPoint.x - dy*size;
                    vertices[1].y = currentPoint.y + dx*size;
                }

                vertices[2*j + 1].x = nextPoint.x - dy*size;
                vertices[2*j + 1].y = nextPoint.y + dx*size;
                vertices[2*j].x = nextPoint.x + dy*size;
                vertices[2*j].y = nextPoint.y - dx*size;

                currentPoint = nextPoint;
            }

            DrawTriangleStrip(vertices, color);
        }

        DrawCircleV(currentPoint, thick/2.0f, color);   // Draw end line circle-cap
    }

    /**
     * Draw spline: Quadratic Bezier, minimum 3 points (1 control point): [p1, c2, p3, c4...]
     * @param points
     * @param thick
     * @param color
     */
    public void DrawSplineBezierQuadratic(Vector2[] points, float thick, Color color) {
        if (points.length < 3) return;

        for (int i = 0; i < points.length - 2; i++) {
            DrawSplineSegmentBezierQuadratic(points[i], points[i + 1], points[i + 2], thick, color);
        }
    }

    /**
     * Draw spline: Cubic Bezier, minimum 4 points (2 control points): [p1, c2, c3, p4, c5, c6...]
     * @param points
     * @param thick
     * @param color
     */
    public void DrawSplineBezierCubic(Vector2[] points, float thick, Color color) {
        if (points.length < 4) {
            return;
        }

        for (int i = 0; i < points.length - 3; i++) {
            DrawSplineSegmentBezierCubic(points[i], points[i + 1], points[i + 2], points[i + 3], thick, color);
        }
    }

    /**
     * Draw spline segment: Linear, 2 points
     * @param p1
     * @param p2
     * @param thick
     * @param color
     */
    public void DrawSplineSegmentLinear(Vector2 p1, Vector2 p2, float thick, Color color) {
        // NOTE: For the linear spline we don't use subdivisions, just a single quad

        Vector2 delta = new Vector2(p2.x - p1.x, p2.y - p1.y);
        float length = (float) Math.sqrt(delta.x*delta.x + delta.y*delta.y);

        if ((length > 0) && (thick > 0)) {
            float scale = thick/(2*length);

            Vector2 radius = new Vector2(-scale*delta.y, scale*delta.x);
            Vector2[] strip = {
                new Vector2(p1.x - radius.x, p1.y - radius.y),
                new Vector2(p1.x + radius.x, p1.y + radius.y),
                new Vector2(p2.x - radius.x, p2.y - radius.y),
                new Vector2(p2.x + radius.x, p2.y + radius.y)
            };

            DrawTriangleStrip(strip, color);
        }
    }

    /**
     * Draw spline segment: B-Spline, 4 points
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param thick
     * @param color
     */
    public void DrawSplineSegmentBasis(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, float thick, Color color) {
        float step = 1.0f/SPLINE_SEGMENT_DIVISIONS;

        Vector2 currentPoint = new Vector2();
        Vector2 nextPoint = new Vector2();
        float t = 0.0f;

        Vector2[] points = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector2();
        }

        float[] a = new float[4];
        float[] b = new float[4];

        a[0] = (-p1.x + 3*p2.x - 3*p3.x + p4.x)/6.0f;
        a[1] = (3*p1.x - 6*p2.x + 3*p3.x)/6.0f;
        a[2] = (-3*p1.x + 3*p3.x)/6.0f;
        a[3] = (p1.x + 4*p2.x + p3.x)/6.0f;

        b[0] = (-p1.y + 3*p2.y - 3*p3.y + p4.y)/6.0f;
        b[1] = (3*p1.y - 6*p2.y + 3*p3.y)/6.0f;
        b[2] = (-3*p1.y + 3*p3.y)/6.0f;
        b[3] = (p1.y + 4*p2.y + p3.y)/6.0f;

        currentPoint.x = a[3];
        currentPoint.y = b[3];

        for (int i = 0; i <= SPLINE_SEGMENT_DIVISIONS; i++) {
            t = step*(float)i;

            nextPoint.x = a[3] + t*(a[2] + t*(a[1] + t*a[0]));
            nextPoint.y = b[3] + t*(b[2] + t*(b[1] + t*b[0]));

            float dy = nextPoint.y - currentPoint.y;
            float dx = nextPoint.x - currentPoint.x;
            float size = (float) ((0.5f*thick)/Math.sqrt(dx*dx + dy*dy));

            if (i == 1) {
                points[0].x = currentPoint.x + dy*size;
                points[0].y = currentPoint.y - dx*size;
                points[1].x = currentPoint.x - dy*size;
                points[1].y = currentPoint.y + dx*size;
            }

            points[2*i + 1].x = nextPoint.x - dy*size;
            points[2*i + 1].y = nextPoint.y + dx*size;
            points[2*i].x = nextPoint.x + dy*size;
            points[2*i].y = nextPoint.y - dx*size;

            currentPoint = nextPoint;
        }

        DrawTriangleStrip(points, color);
    }

    /**
     * Draw spline segment: Catmull-Rom, 4 points
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param thick
     * @param color
     */
    public void DrawSplineSegmentCatmullRom(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, float thick, Color color) {
        float step = 1.0f/SPLINE_SEGMENT_DIVISIONS;

        Vector2 currentPoint = p1;
        Vector2 nextPoint = new Vector2();
        float t = 0.0f;

        Vector2[] points = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];

        for (int i = 0; i <= SPLINE_SEGMENT_DIVISIONS; i++) {
            t = step*(float)i;

            float q0 = (-1*t*t*t) + (2*t*t) + (-1*t);
            float q1 = (3*t*t*t) + (-5*t*t) + 2;
            float q2 = (-3*t*t*t) + (4*t*t) + t;
            float q3 = t*t*t - t*t;

            nextPoint.x = 0.5f*((p1.x*q0) + (p2.x*q1) + (p3.x*q2) + (p4.x*q3));
            nextPoint.y = 0.5f*((p1.y*q0) + (p2.y*q1) + (p3.y*q2) + (p4.y*q3));

            float dy = nextPoint.y - currentPoint.y;
            float dx = nextPoint.x - currentPoint.x;
            float size = (float) ((0.5f*thick)/Math.sqrt(dx*dx + dy*dy));

            if (i == 1) {
                points[0].x = currentPoint.x + dy*size;
                points[0].y = currentPoint.y - dx*size;
                points[1].x = currentPoint.x - dy*size;
                points[1].y = currentPoint.y + dx*size;
            }

            points[2*i + 1].x = nextPoint.x - dy*size;
            points[2*i + 1].y = nextPoint.y + dx*size;
            points[2*i].x = nextPoint.x + dy*size;
            points[2*i].y = nextPoint.y - dx*size;

            currentPoint = nextPoint;
        }

        DrawTriangleStrip(points, color);
    }

    /**
     * Draw spline segment: Quadratic Bezier, 2 points, 1 control point
     * @param p1
     * @param c2
     * @param p3
     * @param thick
     * @param color
     */
    public void DrawSplineSegmentBezierQuadratic(Vector2 p1, Vector2 c2, Vector2 p3, float thick, Color color) {
        float step = 1.0f/SPLINE_SEGMENT_DIVISIONS;

        Vector2 previous = p1;
        Vector2 current = new Vector2();
        float t = 0.0f;

        Vector2[] points = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];

        for (int i = 1; i <= SPLINE_SEGMENT_DIVISIONS; i++) {
            t = step*(float)i;

            float a = (float) Math.pow(1.0f - t, 2);
            float b = 2.0f*(1.0f - t)*t;
            float c = (float) Math.pow(t, 2);

            // NOTE: The easing functions aren't suitable here because they don't take a control point
            current.y = a*p1.y + b*c2.y + c*p3.y;
            current.x = a*p1.x + b*c2.x + c*p3.x;

            float dy = current.y - previous.y;
            float dx = current.x - previous.x;
            float size = (float) (0.5f*thick/Math.sqrt(dx*dx+dy*dy));

            if (i == 1) {
                points[0].x = previous.x + dy*size;
                points[0].y = previous.y - dx*size;
                points[1].x = previous.x - dy*size;
                points[1].y = previous.y + dx*size;
            }

            points[2*i + 1].x = current.x - dy*size;
            points[2*i + 1].y = current.y + dx*size;
            points[2*i].x = current.x + dy*size;
            points[2*i].y = current.y - dx*size;

            previous = current;
        }

        DrawTriangleStrip(points, color);
    }

    /**
     * Draw spline segment: Cubic Bezier, 2 points, 2 control points
     * @param p1
     * @param c2
     * @param c3
     * @param p4
     * @param thick
     * @param color
     */
    public void DrawSplineSegmentBezierCubic(Vector2 p1, Vector2 c2, Vector2 c3, Vector2 p4, float thick, Color color) {
        float step = 1.0f/SPLINE_SEGMENT_DIVISIONS;

        Vector2 previous = p1;
        Vector2 current = new Vector2();
        float t = 0.0f;

        Vector2[] points = new Vector2[2*SPLINE_SEGMENT_DIVISIONS + 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector2();
        }

        for (int i = 1; i <= SPLINE_SEGMENT_DIVISIONS; i++) {
            t = step*(float)i;

            float a = (float) Math.pow(1.0f - t, 3);
            float b = (float) (3.0f*Math.pow(1.0f - t, 2)*t);
            float c = (float) (3.0f*(1.0f - t)*Math.pow(t, 2));
            float d = (float) Math.pow(t, 3);

            current.y = a*p1.y + b*c2.y + c*c3.y + d*p4.y;
            current.x = a*p1.x + b*c2.x + c*c3.x + d*p4.x;

            float dy = current.y - previous.y;
            float dx = current.x - previous.x;
            float size = (float) (0.5f*thick/Math.sqrt(dx*dx+dy*dy));

            if (i == 1) {
                points[0].x = previous.x + dy*size;
                points[0].y = previous.y - dx*size;
                points[1].x = previous.x - dy*size;
                points[1].y = previous.y + dx*size;
            }

            points[2*i + 1].x = current.x - dy*size;
            points[2*i + 1].y = current.y + dx*size;
            points[2*i].x = current.x + dy*size;
            points[2*i].y = current.y - dx*size;

            previous = current;
        }

        DrawTriangleStrip(points, color);
    }

    /**
     * Get spline point for a given t [0.0f .. 1.0f], Linear
     * @param startPos
     * @param endPos
     * @param t
     * @return
     */
    public Vector2 GetSplinePointLinear(Vector2 startPos, Vector2 endPos, float t) {
        Vector2 point = new Vector2();

        point.x = startPos.x*(1.0f - t) + endPos.x*t;
        point.y = startPos.y*(1.0f - t) + endPos.y*t;

        return point;
    }

    /**
     * Get spline point for a given t [0.0f .. 1.0f], B-Spline
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param t
     * @return
     */
    public Vector2 GetSplinePointBasis(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, float t) {
        Vector2 point = new Vector2();

        float[] a = new float[4];
        float[] b = new float[4];

        a[0] = (-p1.x + 3*p2.x - 3*p3.x + p4.x)/6.0f;
        a[1] = (3*p1.x - 6*p2.x + 3*p3.x)/6.0f;
        a[2] = (-3*p1.x + 3*p3.x)/6.0f;
        a[3] = (p1.x + 4*p2.x + p3.x)/6.0f;

        b[0] = (-p1.y + 3*p2.y - 3*p3.y + p4.y)/6.0f;
        b[1] = (3*p1.y - 6*p2.y + 3*p3.y)/6.0f;
        b[2] = (-3*p1.y + 3*p3.y)/6.0f;
        b[3] = (p1.y + 4*p2.y + p3.y)/6.0f;

        point.x = a[3] + t*(a[2] + t*(a[1] + t*a[0]));
        point.y = b[3] + t*(b[2] + t*(b[1] + t*b[0]));

        return point;
    }

    /**
     * Get spline point for a given t [0.0f .. 1.0f], Catmull-Rom
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param t
     * @return
     */
    public Vector2 GetSplinePointCatmullRom(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, float t) {
        Vector2 point = new Vector2();

        float q0 = (-1*t*t*t) + (2*t*t) + (-1*t);
        float q1 = (3*t*t*t) + (-5*t*t) + 2;
        float q2 = (-3*t*t*t) + (4*t*t) + t;
        float q3 = t*t*t - t*t;

        point.x = 0.5f*((p1.x*q0) + (p2.x*q1) + (p3.x*q2) + (p4.x*q3));
        point.y = 0.5f*((p1.y*q0) + (p2.y*q1) + (p3.y*q2) + (p4.y*q3));

        return point;
    }

    /**
     * Get spline point for a given t [0.0f .. 1.0f], Quadratic Bezier
     * @param startPos
     * @param controlPos
     * @param endPos
     * @param t
     * @return
     */
    Vector2 GetSplinePointBezierQuad(Vector2 startPos, Vector2 controlPos, Vector2 endPos, float t) {
        Vector2 point = new Vector2();

        float a = (float) Math.pow(1.0f - t, 2);
        float b = 2.0f*(1.0f - t)*t;
        float c = (float) Math.pow(t, 2);

        point.y = a*startPos.y + b*controlPos.y + c*endPos.y;
        point.x = a*startPos.x + b*controlPos.x + c*endPos.x;

        return point;
    }

    /**
     * Get spline point for a given t [0.0f .. 1.0f], Cubic Bezier
     * @param startPos
     * @param startControlPos
     * @param endControlPos
     * @param endPos
     * @param t
     * @return
     */
    Vector2 GetSplinePointBezierCubic(Vector2 startPos, Vector2 startControlPos, Vector2 endControlPos, Vector2 endPos, float t)
    {
        Vector2 point = new Vector2();

        float a = (float) Math.pow(1.0f - t, 3);
        float b = (float) (3.0f*Math.pow(1.0f - t, 2)*t);
        float c = (float) (3.0f*(1.0f - t)*Math.pow(t, 2));
        float d = (float) Math.pow(t, 3);

        point.y = a*startPos.y + b*startControlPos.y + c*endControlPos.y + d*endPos.y;
        point.x = a*startPos.x + b*startControlPos.x + c*endControlPos.x + d*endPos.x;

        return point;
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Collision Detection functions
    //----------------------------------------------------------------------------------

    /**
     * Check if point is inside rectangle
     *
     * @param point X, Y coordinate
     * @param rec   area to check
     * @return Is point inside rectangle
     */
    public boolean CheckCollisionPointRec(Vector2 point, Rectangle rec) {
        boolean collision = false;

        if ((point.x >= rec.x) && (point.x < (rec.x + rec.width)) && (point.y >= rec.y) && (point.y < (rec.y + rec.height))) {
            collision = true;
        }


        return collision;
    }

    /**
     * Check if point is inside circle
     *
     * @param point  X, Y coordinate
     * @param center X, Y coordinate of circle center
     * @param radius length of the radius
     * @return Is point inside circle
     */
    public boolean CheckCollisionPointCircle(Vector2 point, Vector2 center, float radius) {
        boolean collision = false;

        collision = CheckCollisionCircles(point, 0, center, radius);

        return collision;
    }

    /**
     * Check if point is inside a triangle defined by three points (p1, p2, p3)
     *
     * @param point X, Y coordinate to check location
     * @param p1    X, Y coordinate of triangle
     * @param p2    X, Y coordinate of triangle
     * @param p3    X, Y coordinate of triangle
     * @return Is point inside of triangle
     */
    public boolean CheckCollisionPointTriangle(Vector2 point, Vector2 p1, Vector2 p2, Vector2 p3) {
        boolean collision = false;

        float alpha = ((p2.y - p3.y)*(point.x - p3.x) + (p3.x - p2.x)*(point.y - p3.y)) /
                ((p2.y - p3.y)*(p1.x - p3.x) + (p3.x - p2.x)*(p1.y - p3.y));

        float beta = ((p3.y - p1.y)*(point.x - p3.x) + (p1.x - p3.x)*(point.y - p3.y)) /
                ((p2.y - p3.y)*(p1.x - p3.x) + (p3.x - p2.x)*(p1.y - p3.y));

        float gamma = 1.0f - alpha - beta;

        if ((alpha > 0) && (beta > 0) && (gamma > 0)) collision = true;

        return collision;
    }

    /**
     * Check if point is within a polygon described by an array of vertices
     * @param point XY point to check
     * @param points Array of XY points to describe the polygon
     * @return If point is within polygon
     */
    public boolean CheckCollisionPointPolly(Vector2 point, Vector2[] points) {
        boolean collision = false;

        if (points.length > 2) {
            for (int i = 0; i < points.length; i++) {
                Vector2 vc = points[i];
                Vector2 vn = points[i + 1];

                if ((((vc.y >= point.y) && (vn.y < point.y)) || ((vc.y < point.y) && (vn.y >= point.y))) &&
                        (point.x < ((vn.x - vc.x)*(point.y - vc.y)/(vn.y - vc.y) + vc.x))) {
                    collision = !collision;
                }
            }
        }

        return collision;
    }

    /**
     * Check collision between two rectangles
     *
     * @param rec1 Rectangle to check
     * @param rec2 Rectangle to check
     * @return Are rectangles colliding
     */
    public boolean CheckCollisionRecs(Rectangle rec1, Rectangle rec2) {
        boolean collision = false;

        if((rec1.x < (rec2.x + rec2.width) && (rec1.x + rec1.width) > rec2.x) &&
                (rec1.y < (rec2.y + rec2.height) && (rec1.y + rec1.height) > rec2.y)) {
            collision = true;
        }

        return collision;
    }

    /**
     * Check collision between two circles
     *
     * @param center1 X, Y Position of circle 1 center
     * @param radius1 Length of radius for circle 1
     * @param center2 x, Y Position of circle 2 center
     * @param radius2 Length of radius for circle 2
     * @return Are circles colliding
     */
    public boolean CheckCollisionCircles(Vector2 center1, float radius1, Vector2 center2, float radius2) {
        boolean collision = false;

        float dx = center2.x - center1.x;      // X distance between centers
        float dy = center2.y - center1.y;      // Y distance between centers

        float distance = (float) Math.sqrt(dx * dx + dy * dy); // Distance between centers

        if(distance <= (radius1 + radius2)) {
            collision = true;
        }

        return collision;
    }

    /**
     * Check collision between circle and rectangle
     * NOTE: Reviewed version to take into account corner limit case
     *
     * @param center X, Y coordinate of circle center
     * @param radius Length of circle radius
     * @param rec    Rectangle to check
     * @return Are circle and rectangle colliding
     */
    public boolean CheckCollisionCircleRec(Vector2 center, float radius, Rectangle rec) {
        boolean collision = false;

        int recCenterX = (int) (rec.x + rec.width / 2.0f);
        int recCenterY = (int) (rec.y + rec.height / 2.0f);

        float dx = Math.abs(center.x - (float) recCenterX);
        float dy = Math.abs(center.y - (float) recCenterY);

        if(dx > (rec.width / 2.0f + radius)) {
            return false;
        }
        if(dy > (rec.height / 2.0f + radius)) {
            return false;
        }

        if(dx <= (rec.width / 2.0f)) {
            return true;
        }
        if(dy <= (rec.height / 2.0f)) {
            return true;
        }

        float cornerDistanceSq = (dx - rec.width / 2.0f) * (dx - rec.width / 2.0f) +
                (dy - rec.height / 2.0f) * (dy - rec.height / 2.0f);

        collision = (cornerDistanceSq <= (radius * radius));

        return collision;
    }

    /**
     * Check the collision between two lines defined by two points each
     *
     * @param startPos1 X, Y coordinate for initial endpoint of line 1
     * @param endPos1   X, Y coordinate for final endpoint of line 1
     * @param startPos2 X, Y coordinate for initial endpoint of line 2
     * @param endPos2   X, Y coordinate for final endpoint of line 2
     * @param collisionPoint X, Y coordinate for location of collision
     * @return true if lines collide
     */
    @Contract(mutates = "collisionPoint")
    public boolean CheckCollisionLines(Vector2 startPos1, Vector2 endPos1, Vector2 startPos2, Vector2 endPos2, Vector2 collisionPoint) {
        boolean collision = false;

        float div = (endPos2.y - startPos2.y)*(endPos1.x - startPos1.x) - (endPos2.x - startPos2.x)*(endPos1.y - startPos1.y);

        if (Math.abs(div) >= Float.MIN_VALUE) {
            collision = true;

            float xi = ((startPos2.x - endPos2.x)*(startPos1.x*endPos1.y - startPos1.y*endPos1.x) - (startPos1.x - endPos1.x)*(startPos2.x*endPos2.y - startPos2.y*endPos2.x))/div;
            float yi = ((startPos2.y - endPos2.y)*(startPos1.x*endPos1.y - startPos1.y*endPos1.x) - (startPos1.y - endPos1.y)*(startPos2.x*endPos2.y - startPos2.y*endPos2.x))/div;

            if (((Math.abs(startPos1.x - endPos1.x) > Float.MIN_VALUE) && (xi < Math.min(startPos1.x, endPos1.x) || (xi > Math.max(startPos1.x, endPos1.x)))) ||
                    ((Math.abs(startPos2.x - endPos2.x) > Float.MIN_VALUE) && (xi < Math.min(startPos2.x, endPos2.x) || (xi > Math.max(startPos2.x, endPos2.x)))) ||
                    ((Math.abs(startPos1.y - endPos1.y) > Float.MIN_VALUE) && (yi < Math.min(startPos1.y, endPos1.y) || (yi > Math.max(startPos1.y, endPos1.y)))) ||
                    ((Math.abs(startPos2.y - endPos2.y) > Float.MIN_VALUE) && (yi < Math.min(startPos2.y, endPos2.y) || (yi > Math.max(startPos2.y, endPos2.y))))) {
                collision = false;
            }

            if (collision && (collisionPoint != null)) {
                collisionPoint.x = xi;
                collisionPoint.y = yi;
            }
        }

        return collision;
    }

    // Check if point belongs to line created between two points [p1] and [p2] with defined margin in pixels [threshold]
    boolean CheckCollisionPointLine(Vector2 point, Vector2 p1, Vector2 p2, int threshold) {
        boolean collision = false;
        float dxc = point.x - p1.x;
        float dyc = point.y - p1.y;
        float dxl = p2.x - p1.x;
        float dyl = p2.y - p1.y;
        float cross = dxc*dyl - dyc*dxl;

        if (Math.abs(cross) < (threshold*Math.max(Math.abs(dxl), Math.abs(dyl)))) {
            if (Math.abs(dxl) >= Math.abs(dyl))
                collision = (dxl > 0)? ((p1.x <= point.x) && (point.x <= p2.x)) : ((p2.x <= point.x) && (point.x <= p1.x));
            else
                collision = (dyl > 0)? ((p1.y <= point.y) && (point.y <= p2.y)) : ((p2.y <= point.y) && (point.y <= p1.y));
        }

        return collision;
    }

    // Get collision rectangle for two rectangles collision
    public Rectangle GetCollisionRec(Rectangle rec1, Rectangle rec2) {
        Rectangle overlap = new Rectangle();

        float left = Math.max(rec1.x, rec2.x);
        float right1 = rec1.x + rec1.width;
        float right2 = rec2.x + rec2.width;
        float right = Math.min(right1, right2);
        float top = Math.max(rec1.y, rec2.y);
        float bottom1 = rec1.y + rec1.height;
        float bottom2 = rec2.y + rec2.height;
        float bottom = Math.min(bottom1, bottom2);

        if ((left < right) && (top < bottom)) {
            overlap.x = left;
            overlap.y = top;
            overlap.width = right - left;
            overlap.height = bottom - top;
        }

        return overlap;
    }

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    // Cubic easing in-out
    // NOTE: Used by DrawLineBezier() only
    private float EaseCubicInOut(float t, float b, float c, float d) {
        float result = 0.0f;

        if ((t /= 0.5f*d) < 1) {
            result = 0.5f*c*t*t*t + b;
        }
        else {
            t -= 2;
            result = 0.5f*c*(t*t*t + 2.0f) + b;
        }

        return result;
    }
}