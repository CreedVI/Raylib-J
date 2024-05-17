package com.raylib.java.shapes;

import com.raylib.java.Raylib;
import com.raylib.java.core.Color;
import com.raylib.java.core.ray.Ray;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.textures.Texture2D;
import org.jetbrains.annotations.Contract;

import static com.raylib.java.Config.SUPPORT_QUADS_DRAW_MODE;
import static com.raylib.java.raymath.Raymath.DEG2RAD;
import static com.raylib.java.raymath.Raymath.PI;
import static com.raylib.java.rlgl.RLGL.*;

public class rShapes{


    /**
     * Error rate to calculate how many segments we need to draw a smooth circle,
     * taken from <a href="https://stackoverflow.com/a/2244088">https://stackoverflow.com/a/2244088</a>
     */
    final static float SMOOTH_CIRCLE_ERROR_RATE = 0.5f;

    /**
     * Bezier line divisions
     */
    int BEZIER_LINE_DIVISIONS = 24;

    static Texture2D texShapes = new Texture2D(1, 1, 1, 1, 7);        // Texture used on rShapes drawing (usually a white pixel)
    static Rectangle texShapesRec = new Rectangle(0f, 0f, 1f, 1f);        // Texture source rectangle used on rShapes drawing

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
    public static void SetShapesTexture(Texture2D texture, Rectangle source) {
        texShapes = texture;
        texShapesRec = source;
    }

    /**
     * Draw a pixel
     *
     * @param posX  X coordinate of pixel
     * @param posY  Y coordinate of pixel
     * @param color Color to draw pixel
     */
    public void DrawPixel(int posX, int posY, Color color) {
        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex2i(posX, posY);
        rlVertex2i(posX + 1, posY + 1);
        rlEnd();
    }

    /**
     * Draw a pixel (Vector version)
     *
     * @param position X, Y position of pixel
     * @param color    Color to draw pixel
     */
    public void DrawPixelV(Vector2 position, Color color) {
        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex2f(position.x, position.y);
        rlVertex2f(position.x + 1.0f, position.y + 1.0f);
        rlEnd();
    }

    /**
     * Draw a line
     *
     * @param startPosX X position to begin drawing line
     * @param startPosY Y position to begin drawing line
     * @param endPosX   X position to end drawing line
     * @param endPosY   Y position to end drawing line
     * @param color     color to draw line
     */
    public void DrawLine(int startPosX, int startPosY, int endPosX, int endPosY, Color color) {
        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex2i(startPosX, startPosY);
        rlVertex2i(endPosX, endPosY);
        rlEnd();
    }

    /**
     * Draw a line  (Vector version)
     *
     * @param startPos X, Y position to begin drawing line
     * @param endPos   X, Y position to end drawing line
     * @param color    color to draw line
     */
    public void DrawLineV(Vector2 startPos, Vector2 endPos, Color color) {
        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex2f(startPos.x, startPos.y);
        rlVertex2f(endPos.x, endPos.y);
        rlEnd();
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

            DrawTriangleStrip(strip, 4, color);
        }
    }

    /**
     * Draw line using cubic-bezier curves in-out
     *
     * @param startPos X, Y position to begin drawing line
     * @param endPos   X, Y position to end drawing line
     * @param thick    thickness of the line
     * @param color    color to draw the line
     */
    public void DrawLineBezier(Vector2 startPos, Vector2 endPos, float thick, Color color) {
        Vector2 previous = startPos;
        Vector2 current = new Vector2();

        for (int i = 1; i <= BEZIER_LINE_DIVISIONS; i++) {
            // Cubic easing in-out
            // NOTE: Easing is calculated only for y position value
            current.y = EaseCubicInOut((float)i, startPos.y, endPos.y - startPos.y, (float)BEZIER_LINE_DIVISIONS);
            current.x = previous.x + (endPos.x - startPos.x)/ (float)BEZIER_LINE_DIVISIONS;

            DrawLineEx(previous, current, thick, color);

            previous = current;
        }
    }

    /**
     * Draw line using quadratic bezier curves with a control point
     *
     * @param startPos   X, Y position to begin drawing line
     * @param endPos     X, Y position to end drawing line
     * @param controlPos X, Y position of the control point
     * @param thick      thickness of the line
     * @param color      color to draw the line
     */
    public void DrawLineBezierQuad(Vector2 startPos, Vector2 endPos, Vector2 controlPos, float thick, Color color) {
        float step = 1.0f/BEZIER_LINE_DIVISIONS;
        
        Vector2 previous = startPos;
        Vector2 current = new Vector2();
        float t;

        for(int i = 0; i <= BEZIER_LINE_DIVISIONS; i++) {
            t = step * i;
            float a = (float) Math.pow(1 - t, 2);
            float b = 2 * (1 - t) * t;
            float c = (float) Math.pow(t, 2);

            // NOTE: The easing functions aren't suitable here because they don't take a control point
            current.setY(a * startPos.y + b * controlPos.y + c * endPos.y);
            current.setX(a * startPos.y + b * controlPos.x + c * endPos.x);

            DrawLineEx(previous, current, thick, color);

            previous = current;
        }
    }

    // Draw line using cubic bezier curves with 2 control points
    public void DrawLineBezierCubic(Vector2 startPos, Vector2 endPos, Vector2 startControlPos, Vector2 endControlPos,
                                   float thick, Color color) {
        float step = 1.0f/BEZIER_LINE_DIVISIONS;

        Vector2 previous = startPos;
        Vector2 current = new Vector2();
        float t;

        for (int i = 0; i <= BEZIER_LINE_DIVISIONS; i++) {
            t = step*i;
            float a = (float) Math.pow(1 - t, 3);
            float b = (float) (3*Math.pow(1 - t, 2)*t);
            float c = (float) (3*(1-t)*Math.pow(t, 2));
            float d = (float) Math.pow(t, 3);

            current.y = a*startPos.y + b*startControlPos.y + c*endControlPos.y + d*endPos.y;
            current.x = a*startPos.x + b*startControlPos.x + c*endControlPos.x + d*endPos.x;

            DrawLineEx(previous, current, thick, color);

            previous = current;
        }
    }


    /**
     * Draw lines sequence
     *
     * @param points      Array of X, Y points to draw lines
     * @param pointCount number of points in array
     * @param color       color to draw lines
     */
    public void DrawLineStrip(Vector2[] points, int pointCount, Color color) {
        if(pointCount >= 2) {
            rlCheckRenderBatchLimit(pointCount);

            rlBegin(RL_LINES);
            rlColor4ub(color.r, color.g, color.b, color.a);

            for(int i = 0; i < pointCount - 1; i++) {
                rlVertex2f(points[i].x, points[i].y);
                rlVertex2f(points[i + 1].x, points[i + 1].y);
            }
            rlEnd();
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
     * Draw a piece of a circle
     *
     * @param center     X, Y position of circle center
     * @param radius     length of circle radius
     * @param startAngle angle to begin drawing circle sector
     * @param endAngle   angle to end drawing circle sector
     * @param segments   number of segments
     * @param color      color to draw circle sector
     */
    public void DrawCircleSector(Vector2 center, float radius, float startAngle, float endAngle, int segments,
                                 Color color) {
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
            rlCheckRenderBatchLimit(4*segments/2);

            rlSetTexture(texShapes.getId());

            rlBegin(RL_QUADS);
            // NOTE: Every QUAD actually represents two segments
            for(int i = 0; i < segments / 2; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(center.x, center.y);

                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength * 2)) * radius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength * 2)) * radius);

                angle += (stepLength * 2);
            }

            // NOTE: In case number of segments is odd, we add one last piece to the cake
            if(segments % 2 == 1) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(center.x, center.y);

                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(center.x, center.y);
            }
            rlEnd();

            rlSetTexture(0);
        }
        else{
            rlCheckRenderBatchLimit(3*segments);

            rlBegin(RL_TRIANGLES);
            for(int i = 0; i < segments; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlVertex2f(center.x, center.y);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

                angle += stepLength;
            }
            rlEnd();
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

        rlCheckRenderBatchLimit(limit);

        rlBegin(RL_LINES);
        if(showCapLines) {
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(center.x, center.y);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);
        }

        for(int i = 0; i < segments; i++) {
            rlColor4ub(color.r, color.g, color.b, color.a);

            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

            angle += stepLength;
        }

        if(showCapLines) {
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(center.x, center.y);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);
        }
        rlEnd();
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
        rlCheckRenderBatchLimit(3*36);

        rlBegin(RL_TRIANGLES);
        for(int i = 0; i < 360; i += 10) {
            rlColor4ub(color1.r, color1.g, color1.b, color1.a);
            rlVertex2f((float) centerX, (float) centerY);
            rlColor4ub(color2.r, color2.g, color2.b, color2.a);
            rlVertex2f((float) centerX + (float) Math.sin(DEG2RAD * i) * radius, (float) centerY + (float) Math.cos(DEG2RAD * i) * radius);
            rlColor4ub(color2.r, color2.g, color2.b, color2.a);
            rlVertex2f((float) centerX + (float) Math.sin(DEG2RAD * (i + 10)) * radius, (float) centerY + (float) Math.cos(DEG2RAD * (i + 10)) * radius);
        }
        rlEnd();
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
     * Draw circle outline
     *
     * @param centerX X coordinate of circle center
     * @param centerY Y coordinate of circle center
     * @param radius  length of circle radius
     * @param color   color to draw circle outline
     */
    public void DrawCircleLines(int centerX, int centerY, float radius, Color color) {
        rlCheckRenderBatchLimit(2*36);

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);

        // NOTE: Circle outline is drawn pixel by pixel every degree (0 to 360)
        for(int i = 0; i < 360; i += 10) {
            rlVertex2f(centerX + (float) Math.sin(DEG2RAD * i) * radius, centerY + (float) Math.cos(DEG2RAD * i) * radius);
            rlVertex2f(centerX + (float) Math.sin(DEG2RAD * (i + 10)) * radius, centerY + (float) Math.cos(DEG2RAD * (i + 10)) * radius);
        }
        rlEnd();
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
        rlCheckRenderBatchLimit(3*36);

        rlBegin(RL_TRIANGLES);
        for(int i = 0; i < 360; i += 10) {
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f((float) centerX, (float) centerY);
            rlVertex2f((float) centerX + (float) Math.sin(DEG2RAD * i) * radiusH, (float) centerY + (float) Math.cos(DEG2RAD * i) * radiusV);
            rlVertex2f((float) centerX + (float) Math.sin(DEG2RAD * (i + 10)) * radiusH, (float) centerY + (float) Math.cos(DEG2RAD * (i + 10)) * radiusV);
        }
        rlEnd();
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
        rlCheckRenderBatchLimit(2*36);

        rlBegin(RL_LINES);
        for(int i = 0; i < 360; i += 10) {
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(centerX + (float) Math.sin(DEG2RAD * i) * radiusH, centerY + (float) Math.cos(DEG2RAD * i) * radiusV);
            rlVertex2f(centerX + (float) Math.sin(DEG2RAD * (i + 10)) * radiusH, centerY + (float) Math.cos(DEG2RAD * (i + 10)) * radiusV);
        }
        rlEnd();
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
            rlCheckRenderBatchLimit(4*segments);

            RLGL.rlSetTexture(texShapes.getId());

            rlBegin(RL_QUADS);
            for(int i = 0; i < segments; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);

                rlTexCoord2f(texShapesRec.x / texShapes.width,
                        (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius,
                        center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width,
                        (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius,
                        center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width,
                        texShapesRec.y / texShapes.height);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius,
                        center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                angle += stepLength;
            }
            rlEnd();

            rlSetTexture(0);
        }
        else{
            rlCheckRenderBatchLimit(6*segments);

            rlBegin(RL_TRIANGLES);
            for(int i = 0; i < segments; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

                angle += stepLength;
            }
            rlEnd();
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

        rlCheckRenderBatchLimit(limit);

        rlBegin(RL_LINES);
        if(showCapLines) {
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);
        }

        for(int i = 0; i < segments; i++) {
            rlColor4ub(color.r, color.g, color.b, color.a);

            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius,
                    center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius,
                    center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius,
                    center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius,
                    center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

            angle += stepLength;
        }

        if(showCapLines) {
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius,
                    center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
            rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius,
                    center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);
        }
        rlEnd();
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
    public static void DrawRectangleRec(Rectangle rec, Color color) {
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
    public static void DrawRectanglePro(Rectangle rec, Vector2 origin, float rotation, Color color) {
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
            rlCheckRenderBatchLimit(4);

            rlSetTexture(texShapes.getId());
            rlBegin(RL_QUADS);

            rlNormal3f(0.0f, 0.0f, 1.0f);
            rlColor4ub(color.r, color.g, color.b, color.a);

            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(topLeft.x, topLeft.y);

            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(bottomLeft.x, bottomLeft.y);

            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width,
                    (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(bottomRight.x, bottomRight.y);

            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(topRight.x, topRight.y);

            rlEnd();
            rlSetTexture(0);
        }
        else {
            rlCheckRenderBatchLimit(6);

            rlBegin(RL_TRIANGLES);

            rlColor4ub(color.r, color.g, color.b, color.a);

            rlVertex2f(topLeft.x, topLeft.y);
            rlVertex2f(bottomLeft.x, bottomLeft.y);
            rlVertex2f(topRight.x, topRight.y);

            rlVertex2f(topRight.x, topRight.y);
            rlVertex2f(bottomLeft.x, bottomLeft.y);
            rlVertex2f(bottomRight.x, bottomRight.y);

            rlEnd();
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
        rlCheckRenderBatchLimit(4);
        RLGL.rlSetTexture(texShapes.getId());

        rlBegin(RL_QUADS);
        rlNormal3f(0.0f, 0.0f, 1.0f);
        // NOTE: Default raylib font character 95 is a white square
        rlColor4ub(col1.r, col1.g, col1.b, col1.a);
        rlTexCoord2f(texShapesRec.x/texShapes.width, texShapesRec.y/texShapes.height);
        rlVertex2f(rec.x, rec.y);
        rlColor4ub(col2.r, col2.g, col2.b, col2.a);
        rlTexCoord2f(texShapesRec.x/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
        rlVertex2f(rec.x, rec.y + rec.height);
        rlColor4ub(col3.r, col3.g, col3.b, col3.a);
        rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, (texShapesRec.y + texShapesRec.height)/texShapes.height);
        rlVertex2f(rec.x + rec.width, rec.y + rec.height);
        rlColor4ub(col4.r, col4.g, col4.b, col4.a);
        rlTexCoord2f((texShapesRec.x + texShapesRec.width)/texShapes.width, texShapesRec.y/texShapes.height);
        rlVertex2f(rec.x + rec.width, rec.y);


        rlSetTexture(0);
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
            rlBegin(RL_LINES);
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2i(posX + 1, posY + 1);
            rlVertex2i(posX + width, posY + 1);

            rlVertex2i(posX + width, posY + 1);
            rlVertex2i(posX + width, posY + height);

            rlVertex2i(posX + width, posY + height);
            rlVertex2i(posX + 1, posY + height);

            rlVertex2i(posX + 1, posY + height);
            rlVertex2i(posX + 1, posY + 1);
            rlEnd();
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
                180.0f, 90.0f, 0.0f, 270.0f
        };

        if(SUPPORT_QUADS_DRAW_MODE) {
            rlCheckRenderBatchLimit(16*segments/2 + 5*4);

            RLGL.rlSetTexture(texShapes.getId());

            rlBegin(RL_QUADS);
            // Draw all of the 4 corners: [1] Upper Left Corner, [3] Upper Right Corner, [5] Lower Right Corner, [7] Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];
                // NOTE: Every QUAD actually represents two segments
                for(int i = 0; i < segments / 2; i++) {
                    rlColor4ub(color.r, color.g, color.b, color.a);
                    rlTexCoord2f(texShapesRec.x / texShapes.width,
                            texShapesRec.y / texShapes.height);
                    rlVertex2f(center.x, center.y);
                    rlTexCoord2f(texShapesRec.x / texShapes.width,
                            (texShapesRec.y + texShapesRec.height) / texShapes.height);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius,
                            center.y + (float) Math.cos(DEG2RAD * angle) * radius);
                    rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width,
                            (texShapesRec.y + texShapesRec.height) / texShapes.height);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius,
                            center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);
                    rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width,
                            texShapesRec.y / texShapes.height);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength * 2)) * radius,
                            center.y + (float) Math.cos(DEG2RAD * (angle + stepLength * 2)) * radius);
                    angle += (stepLength * 2);
                }
                // NOTE: In case number of segments is odd, we add one last piece to the cake
                if(segments % 2 == 1) {
                    rlColor4ub(color.r, color.g, color.b, color.a);
                    rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                    rlVertex2f(center.x, center.y);
                    rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius, center.y + (float) Math.cos(DEG2RAD * angle) * radius);
                    rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);
                    rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                    rlVertex2f(center.x, center.y);
                }
            }

            // [2] Upper Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[0].x, point[0].y);
            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[8].x, point[8].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[9].x, point[9].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[1].x, point[1].y);

            // [4] Right Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[2].x, point[2].y);
            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[9].x, point[9].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[10].x, point[10].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[3].x, point[3].y);

            // [6] Bottom Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[11].x, point[11].y);
            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[5].x, point[5].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[4].x, point[4].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[10].x, point[10].y);

            // [8] Left Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[7].x, point[7].y);
            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[6].x, point[6].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[11].x, point[11].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[8].x, point[8].y);

            // [9] Middle Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[8].x, point[8].y);
            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[11].x, point[11].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(point[10].x, point[10].y);
            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(point[9].x, point[9].y);

            rlEnd();
            rlSetTexture(0);
        }
        else{
            rlCheckRenderBatchLimit(12 * segments + 5 * 6);

            rlBegin(RL_TRIANGLES);
            // Draw all of the 4 corners: [1] Upper Left Corner, [3] Upper Right Corner, [5] Lower Right Corner, [7] Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];
                for(int i = 0; i < segments; i++) {
                    rlColor4ub(color.r, color.g, color.b, color.a);
                    rlVertex2f(center.x, center.y);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * radius,
                            center.y + (float) Math.cos(DEG2RAD * angle) * radius);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius,
                            center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);
                    angle += stepLength;
                }
            }

            // [2] Upper Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(point[0].x, point[0].y);
            rlVertex2f(point[8].x, point[8].y);
            rlVertex2f(point[9].x, point[9].y);
            rlVertex2f(point[1].x, point[1].y);
            rlVertex2f(point[0].x, point[0].y);
            rlVertex2f(point[9].x, point[9].y);

            // [4] Right Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(point[9].x, point[9].y);
            rlVertex2f(point[10].x, point[10].y);
            rlVertex2f(point[3].x, point[3].y);
            rlVertex2f(point[2].x, point[2].y);
            rlVertex2f(point[9].x, point[9].y);
            rlVertex2f(point[3].x, point[3].y);

            // [6] Bottom Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(point[11].x, point[11].y);
            rlVertex2f(point[5].x, point[5].y);
            rlVertex2f(point[4].x, point[4].y);
            rlVertex2f(point[10].x, point[10].y);
            rlVertex2f(point[11].x, point[11].y);
            rlVertex2f(point[4].x, point[4].y);

            // [8] Left Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(point[7].x, point[7].y);
            rlVertex2f(point[6].x, point[6].y);
            rlVertex2f(point[11].x, point[11].y);
            rlVertex2f(point[8].x, point[8].y);
            rlVertex2f(point[7].x, point[7].y);
            rlVertex2f(point[11].x, point[11].y);

            // [9] Middle Rectangle
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(point[8].x, point[8].y);
            rlVertex2f(point[11].x, point[11].y);
            rlVertex2f(point[10].x, point[10].y);
            rlVertex2f(point[9].x, point[9].y);
            rlVertex2f(point[8].x, point[8].y);
            rlVertex2f(point[10].x, point[10].y);
            rlEnd();
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

        float[] angles = {180.0f, 90.0f, 0.0f, 270.0f};

        if(lineThick > 1) {
            if(SUPPORT_QUADS_DRAW_MODE) {
                rlCheckRenderBatchLimit(4 * 4 * segments + 4 * 4);

                RLGL.rlSetTexture(texShapes.getId());

                rlBegin(RL_QUADS);
                // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
                for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
                {
                    float angle = angles[k];
                    Vector2 center = centers[k];
                    for(int i = 0; i < segments; i++) {
                        rlColor4ub(color.r, color.g, color.b, color.a);
                        rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);
                        rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                        rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);
                        rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                        angle += stepLength;
                    }
                }

                // Upper rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[0].x, point[0].y);
                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[8].x, point[8].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[9].x, point[9].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[1].x, point[1].y);

                // Right rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[2].x, point[2].y);
                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[10].x, point[10].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[11].x, point[11].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[3].x, point[3].y);

                // Lower rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[13].x, point[13].y);
                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[5].x, point[5].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[4].x, point[4].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[12].x, point[12].y);

                // Left rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[15].x, point[15].y);
                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[7].x, point[7].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(point[6].x, point[6].y);
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(point[14].x, point[14].y);

                rlEnd();
                rlSetTexture(0);
            }
            else{
                rlCheckRenderBatchLimit(4 * 6 * segments + 4 * 6);

                rlBegin(RL_TRIANGLES);

                // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
                for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
                {
                    float angle = angles[k];
                    Vector2 center = centers[k];

                    for(int i = 0; i < segments; i++) {
                        rlColor4ub(color.r, color.g, color.b, color.a);

                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * innerRadius);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                        rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

                        angle += stepLength;
                    }
                }

                // Upper rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlVertex2f(point[0].x, point[0].y);
                rlVertex2f(point[8].x, point[8].y);
                rlVertex2f(point[9].x, point[9].y);
                rlVertex2f(point[1].x, point[1].y);
                rlVertex2f(point[0].x, point[0].y);
                rlVertex2f(point[9].x, point[9].y);

                // Right rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlVertex2f(point[10].x, point[10].y);
                rlVertex2f(point[11].x, point[11].y);
                rlVertex2f(point[3].x, point[3].y);
                rlVertex2f(point[2].x, point[2].y);
                rlVertex2f(point[10].x, point[10].y);
                rlVertex2f(point[3].x, point[3].y);

                // Lower rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlVertex2f(point[13].x, point[13].y);
                rlVertex2f(point[5].x, point[5].y);
                rlVertex2f(point[4].x, point[4].y);
                rlVertex2f(point[12].x, point[12].y);
                rlVertex2f(point[13].x, point[13].y);
                rlVertex2f(point[4].x, point[4].y);

                // Left rectangle
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlVertex2f(point[7].x, point[7].y);
                rlVertex2f(point[6].x, point[6].y);
                rlVertex2f(point[14].x, point[14].y);
                rlVertex2f(point[15].x, point[15].y);
                rlVertex2f(point[7].x, point[7].y);
                rlVertex2f(point[14].x, point[14].y);
                rlEnd();
            }
        }
        else{
            // Use LINES to draw the outline
            rlCheckRenderBatchLimit(8 * segments + 4 * 2);

            rlBegin(RL_LINES);

            // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];

                for(int i = 0; i < segments; i++) {
                    rlColor4ub(color.r, color.g, color.b, color.a);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.y + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                    rlVertex2f(center.x + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.y + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);
                    angle += stepLength;
                }
            }

            // And now the remaining 4 lines
            for(int i = 0; i < 8; i += 2) {
                rlColor4ub(color.r, color.g, color.b, color.a);
                rlVertex2f(point[i].x, point[i].y);
                rlVertex2f(point[i + 1].x, point[i + 1].y);
            }

            rlEnd();
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
            rlCheckRenderBatchLimit(4);

            RLGL.rlSetTexture(texShapes.getId());

            rlBegin(RL_QUADS);
            rlColor4ub(color.r, color.g, color.b, color.a);

            rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(v1.x, v1.y);

            rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(v2.x, v2.y);

            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
            rlVertex2f(v2.x, v2.y);

            rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
            rlVertex2f(v3.x, v3.y);
            rlEnd();

            rlSetTexture(0);
        }
        else{
            rlCheckRenderBatchLimit(3);

            rlBegin(RL_TRIANGLES);
            rlColor4ub(color.r, color.g, color.b, color.a);
            rlVertex2f(v1.x, v1.y);
            rlVertex2f(v2.x, v2.y);
            rlVertex2f(v3.x, v3.y);
            rlEnd();
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
        rlCheckRenderBatchLimit(6);

        rlBegin(RL_LINES);
        rlColor4ub(color.r, color.g, color.b, color.a);
        rlVertex2f(v1.x, v1.y);
        rlVertex2f(v2.x, v2.y);

        rlVertex2f(v2.x, v2.y);
        rlVertex2f(v3.x, v3.y);

        rlVertex2f(v3.x, v3.y);
        rlVertex2f(v1.x, v1.y);
        rlEnd();
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
            rlCheckRenderBatchLimit((pointsCount - 2) * 4);

            RLGL.rlSetTexture(texShapes.getId());
            rlBegin(RL_QUADS);
            rlColor4ub(color.r, color.g, color.b, color.a);

            for(int i = 1; i < pointsCount - 1; i++) {
                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(points[0].x, points[0].y);

                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(points[i].x, points[i].y);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f(points[i + 1].x, points[i + 1].y);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(points[i + 1].x, points[i + 1].y);
            }
            rlEnd();
            rlSetTexture(0);
        }
    }

    /**
     * Draw a triangle strip defined by points
     * NOTE: Every new vertex connects with previous two
     *
     * @param points      Array of X, Y coordinates
     * @param pointCount number of points
     * @param color       color to draw strip
     */
    public void DrawTriangleStrip(Vector2[] points, int pointCount, Color color) {
        if(pointCount >= 3) {
            rlCheckRenderBatchLimit(3 * (pointCount - 2));

            rlBegin(RL_TRIANGLES);
            rlColor4ub(color.r, color.g, color.b, color.a);

            for(int i = 2; i < pointCount; i++) {
                if((i % 2) == 0) {
                    rlVertex2f(points[i].x, points[i].y);
                    rlVertex2f(points[i - 2].x, points[i - 2].y);
                    rlVertex2f(points[i - 1].x, points[i - 1].y);
                }
                else{
                    rlVertex2f(points[i].x, points[i].y);
                    rlVertex2f(points[i - 1].x, points[i - 1].y);
                    rlVertex2f(points[i - 2].x, points[i - 2].y);
                }
            }
            rlEnd();
        }
    }

    /**
     * Draw a regular polygon of n sides (vector version)
     *
     * @param center   X, Y coordinates of polygon center
     * @param sides    number of sides
     * @param radius   length of polygon radius
     * @param rotation degrees to rotate polygon
     * @param color    Color to draw polygon
     */
    public void DrawPoly(Vector2 center, int sides, float radius, float rotation, Color color) {
        if(sides < 3) {
            sides = 3;
        }
        float centralAngle = 0.0f;

        if(SUPPORT_QUADS_DRAW_MODE) {
            rlCheckRenderBatchLimit(4 * sides); //Each side is a quad
        }
        else {
            rlCheckRenderBatchLimit(3 * sides);
        }
        rlPushMatrix();
        rlTranslatef(center.x, center.y, 0.0f);
        rlRotatef(rotation, 0.0f, 0.0f, 1.0f);

        if(SUPPORT_QUADS_DRAW_MODE) {
            RLGL.rlSetTexture(texShapes.getId());

            rlBegin(RL_QUADS);
            for(int i = 0; i < sides; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f(0, 0);

                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                centralAngle += 360.0f / (float) sides;
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
            }
            rlEnd();
            rlSetTexture(0);
        }
        else{
            rlBegin(RL_TRIANGLES);
            for(int i = 0; i < sides; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlVertex2f(0, 0);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                centralAngle += 360.0f / (float) sides;
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
            }
            rlEnd();
        }
        rlPopMatrix();
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
        if(sides < 3) {
            sides = 3;
        }
        float centralAngle = 0.0f;

        rlCheckRenderBatchLimit(2 * sides);

        rlPushMatrix();
        rlTranslatef(center.x, center.y, 0.0f);
        rlRotatef(rotation, 0.0f, 0.0f, 1.0f);

        rlBegin(RL_LINES);
        for(int i = 0; i < sides; i++) {
            rlColor4ub(color.r, color.g, color.b, color.a);

            rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
            centralAngle += 360.0f / (float) sides;
            rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
        }
        rlEnd();
        rlPopMatrix();
    }

    public void DrawPolyLinesEx(Vector2 center, int sides, float radius, float rotation, float lineThick, Color color) {
        if (sides < 3) sides = 3;
        float centralAngle = 0.0f;
        float exteriorAngle = 360.0f/(float)sides;
        float innerRadius = radius - (lineThick*(float) Math.cos(DEG2RAD*exteriorAngle/2.0f));

        if(SUPPORT_QUADS_DRAW_MODE) {
            rlCheckRenderBatchLimit(4 * sides);
        }
        else{
            rlCheckRenderBatchLimit(6 * sides);
        }

        rlPushMatrix();
        rlTranslatef(center.x, center.y, 0.0f);
        rlRotatef(rotation, 0.0f, 0.0f, 1.0f);

        if(SUPPORT_QUADS_DRAW_MODE) {
            rlSetTexture(texShapes.id);

            rlBegin(RL_QUADS);
            for (int i = 0; i < sides; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);

                rlTexCoord2f(texShapesRec.x / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * innerRadius, (float) Math.cos(DEG2RAD * centralAngle) * innerRadius);

                rlTexCoord2f(texShapesRec.x / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                centralAngle += exteriorAngle;
                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, texShapesRec.y / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                rlTexCoord2f((texShapesRec.x + texShapesRec.width) / texShapes.width, (texShapesRec.y + texShapesRec.height) / texShapes.height);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * innerRadius, (float) Math.cos(DEG2RAD * centralAngle) * innerRadius);
            }
            rlEnd();
            rlSetTexture(0);
        }
        else{
            rlBegin(RL_TRIANGLES);
            for (int i = 0; i < sides; i++) {
                rlColor4ub(color.r, color.g, color.b, color.a);
                float nextAngle = centralAngle + exteriorAngle;

                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * innerRadius, (float) Math.cos(DEG2RAD * centralAngle) * innerRadius);
                rlVertex2f((float) Math.sin(DEG2RAD * nextAngle) * radius, (float) Math.cos(DEG2RAD * nextAngle) * radius);

                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * innerRadius, (float) Math.cos(DEG2RAD * centralAngle) * innerRadius);
                rlVertex2f((float) Math.sin(DEG2RAD * nextAngle) * radius, (float) Math.cos(DEG2RAD * nextAngle) * radius);
                rlVertex2f((float) Math.sin(DEG2RAD * nextAngle) * innerRadius, (float) Math.cos(DEG2RAD * nextAngle) * innerRadius);

                centralAngle = nextAngle;
            }
            rlEnd();
        }
        rlPopMatrix();
    }

    /**
     * Check if point is inside rectangle
     *
     * @param point X, Y coordinate
     * @param rec   area to check
     * @return Is point inside rectangle
     */
    public boolean CheckCollisionPointRec(Vector2 point, Rectangle rec) {
        boolean collision = false;

        if((point.x >= rec.x) && (point.x <= (rec.x + rec.width)) && (point.y >= rec.y) && (point.y <= (rec.y + rec.height))) {
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
    @Contract(mutates = "param5")
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
        Rectangle rec = new Rectangle();

        if(CheckCollisionRecs(rec1, rec2)) {
            float dxx = Math.abs(rec1.x - rec2.x);
            float dyy = Math.abs(rec1.y - rec2.y);

            if(rec1.x <= rec2.x) {
                if(rec1.y <= rec2.y) {
                    rec.setX(rec2.x);
                    rec.setY(rec2.y);
                    rec.setWidth(rec1.width - dxx);
                    rec.setHeight(rec1.height - dyy);
                }
                else{
                    rec.setX(rec2.x);
                    rec.setY(rec1.y);
                    rec.setWidth(rec1.width - dxx);
                    rec.setHeight(rec2.height - dyy);
                }
            }
            else{
                if(rec1.y <= rec2.y) {
                    rec.setX(rec1.x);
                    rec.setY(rec2.y);
                    rec.setWidth(rec2.width - dxx);
                    rec.setHeight(rec1.height - dyy);
                }
                else{
                    rec.setX(rec1.x);
                    rec.setY(rec1.y);
                    rec.setWidth(rec2.width - dxx);
                    rec.setHeight(rec2.height - dyy);
                }
            }

            if(rec1.width > rec2.width) {
                if(rec.width >= rec2.width) {
                    rec.setWidth(rec2.width);
                }
            }
            else{
                if(rec.width >= rec1.width) {
                    rec.setWidth(rec1.width);
                }
            }

            if(rec1.height > rec2.height) {
                if(rec.height >= rec2.height) {
                    rec.setHeight(rec2.height);
                }
            }
            else{
                if(rec.height >= rec1.height) {
                    rec.setHeight(rec1.height);
                }
            }
        }

        return rec;
    }

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    // Cubic easing in-out
    // NOTE: Used by DrawLineBezier() only
    private float EaseCubicInOut(float t, float b, float c, float d) {
        if ((t /= 0.5f*d) < 1) return 0.5f*c*t*t*t + b;

        t -= 2;

        return 0.5f*c*(t*t*t + 2.0f) + b;
    }
}