package com.creedvi.raylib.java.rlj.shapes;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_QUADS_DRAW_MODE;
import static com.creedvi.raylib.java.rlj.raymath.RayMath.DEG2RAD;
import static com.creedvi.raylib.java.rlj.raymath.RayMath.PI;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;
import static com.creedvi.raylib.java.rlj.utils.Easings.EaseCubicInOut;

public class Shapes{


    /**
     * Error rate to calculate how many segments we need to draw a smooth circle,
     * taken from https://stackoverflow.com/a/2244088
     */
    final static float SMOOTH_CIRCLE_ERROR_RATE = 0.5f;

    /**
     * Bezier line divisions
     */
    int BEZIER_LINE_DIVISIONS = 24;

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    /**
     * Draw a pixel
     *
     * @param posX  X coordinate of pixel
     * @param posY  Y coordinate of pixel
     * @param color Color to draw pixel
     */
    public void DrawPixel(int posX, int posY, Color color){
        rlBegin(RL_LINES);
        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
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
    public void DrawPixelV(Vector2 position, Color color){
        rlBegin(RL_LINES);
        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
        rlVertex2f(position.getX(), position.getY());
        rlVertex2f(position.getX() + 1.0f, position.getY() + 1.0f);
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
    public void DrawLine(int startPosX, int startPosY, int endPosX, int endPosY, Color color){
        rlBegin(RL_LINES);
        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
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
    public void DrawLineV(Vector2 startPos, Vector2 endPos, Color color){
        rlBegin(RL_LINES);
        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
        rlVertex2f(startPos.getX(), startPos.getY());
        rlVertex2f(endPos.getX(), endPos.getY());
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
    public void DrawLineEx(Vector2 startPos, Vector2 endPos, float thick, Color color){
        Vector2 delta = new Vector2(endPos.getX() - startPos.getX(), endPos.getY() - startPos.getY());
        float length = (float) Math.sqrt(delta.getX() * delta.getX() + delta.getY() * delta.getY());

        if(length > 0 && thick > 0){
            float scale = thick / (2 * length);
            Vector2 radius = new Vector2(-scale * delta.getY(), scale * delta.getX());
            Vector2[] strip = new Vector2[]{
                    new Vector2(startPos.getX() - radius.getX(), startPos.getY() - radius.getY()),
                    new Vector2(startPos.getX() + radius.getX(), startPos.getY() + radius.getY()),
                    new Vector2(endPos.getX() - radius.getX(), endPos.getY() - radius.getY()),
                    new Vector2(endPos.getX() + radius.getX(), endPos.getY() + radius.getY())
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
    public void DrawLineBezier(Vector2 startPos, Vector2 endPos, float thick, Color color){
        Vector2 previous = startPos;
        Vector2 current = new Vector2();

        for(int i = 1; i <= BEZIER_LINE_DIVISIONS; i++){
            // Cubic easing in-out
            // NOTE: Easing is calculated only for y position value
            current.setY(EaseCubicInOut((float) i, startPos.getY(), endPos.getY() - startPos.getY(), (float) BEZIER_LINE_DIVISIONS));
            current.setX(previous.getX() + (endPos.getX() - startPos.getX()) / (float) BEZIER_LINE_DIVISIONS);

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
    public void DrawLineBezierQuad(Vector2 startPos, Vector2 endPos, Vector2 controlPos, float thick, Color color){
        float step = 1.0f / BEZIER_LINE_DIVISIONS;

        Vector2 previous = startPos;
        Vector2 current = new Vector2();
        float t = 0.0f;

        for(int i = 0; i <= BEZIER_LINE_DIVISIONS; i++){
            t = step * i;
            float a = (float) Math.pow(1 - t, 2);
            float b = 2 * (1 - t) * t;
            float c = (float) Math.pow(t, 2);

            // NOTE: The easing functions aren't suitable here because they don't take a control point
            current.setY(a * startPos.getY() + b * controlPos.getY() + c * endPos.getY());
            current.setX(a * startPos.getY() + b * controlPos.getX() + c * endPos.getX());

            DrawLineEx(previous, current, thick, color);

            previous = current;
        }
    }

    /**
     * Draw lines sequence
     *
     * @param points      Array of X, Y points to draw lines
     * @param pointsCount number of points in array
     * @param color       color to draw lines
     */
    //TODO: replace pointsCount with points.length?
    public void DrawLineStrip(Vector2[] points, int pointsCount, Color color){
        if(pointsCount >= 2){
            if(rlCheckBufferLimit(pointsCount)){
                rlglDraw();
            }

            rlBegin(RL_LINES);
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            for(int i = 0; i < pointsCount - 1; i++){
                rlVertex2f(points[i].getX(), points[i].getY());
                rlVertex2f(points[i + 1].getX(), points[i + 1].getY());
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
    public void DrawCircle(int centerX, int centerY, float radius, Color color){
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
    public void DrawCircleSector(Vector2 center, float radius, int startAngle, int endAngle, int segments, Color color){
        if(radius <= 0.0f){
            radius = 0.1f;  // Apublic void div by zero
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle){
            // Swap values
            int tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        if(segments < 4){
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0){
                segments = 4;
            }
        }

        float stepLength = (float) (endAngle - startAngle) / (float) segments;
        float angle = (float) startAngle;

        if(SUPPORT_QUADS_DRAW_MODE){
            if(rlCheckBufferLimit(4 * segments / 2)){
                rlglDraw();
            }

            rlEnableTexture(GetShapesTexture().getId());

            rlBegin(RL_QUADS);
            // NOTE: Every QUAD actually represents two segments
            for(int i = 0; i < segments / 2; i++){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(center.getX(), center.getY());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength * 2)) * radius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength * 2)) * radius);

                angle += (stepLength * 2);
            }

            // NOTE: In case number of segments is odd, we add one last piece to the cake
            if(segments % 2 == 1){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(center.getX(), center.getY());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(center.getX(), center.getY());
            }
            rlEnd();

            rlDisableTexture();
        }
        else{
            if(rlCheckBufferLimit(3 * segments)){
                rlglDraw();
            }

            rlBegin(RL_TRIANGLES);
            for(int i = 0; i < segments; i++){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlVertex2f(center.getX(), center.getY());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

                angle += stepLength;
            }
            rlEnd();
        }
    }

    /**
     * Draw a piece of a circle (lines)
     *
     * @param center     X, Y coordinate of circle center
     * @param radius     length of circle radius
     * @param startAngle angle to begin drawing circle sector
     * @param endAngle   angle to end drawing circle sector
     * @param segments   number of segments
     * @param color      color to draw circle sector
     */
    public void DrawCircleSectorLines(Vector2 center, float radius, int startAngle, int endAngle, int segments, Color color){
        if(radius <= 0.0f){
            radius = 0.1f;  // Avoid div by zero issue
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle){
            // Swap values
            int tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        if(segments < 4){
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0){
                segments = 4;
            }
        }

        float stepLength = (float) (endAngle - startAngle) / (float) segments;
        float angle = (float) startAngle;

        // Hide the cap lines when the circle is full
        boolean showCapLines = true;
        int limit = 2 * (segments + 2);
        if((endAngle - startAngle) % 360 == 0){
            limit = 2 * segments;
            showCapLines = false;
        }

        if(rlCheckBufferLimit(limit)){
            rlglDraw();
        }

        rlBegin(RL_LINES);
        if(showCapLines){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(center.getX(), center.getY());
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
        }

        for(int i = 0; i < segments; i++){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);

            angle += stepLength;
        }

        if(showCapLines){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(center.getX(), center.getY());
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
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
    public void DrawCircleGradient(int centerX, int centerY, float radius, Color color1, Color color2){
        if(rlCheckBufferLimit(3 * 36)){
            rlglDraw();
        }

        rlBegin(RL_TRIANGLES);
        for(int i = 0; i < 360; i += 10){
            rlColor4ub(color1.getR(), color1.getG(), color1.getB(), color1.getA());
            rlVertex2f((float) centerX, (float) centerY);
            rlColor4ub(color2.getR(), color2.getG(), color2.getB(), color2.getA());
            rlVertex2f((float) centerX + (float) Math.sin(DEG2RAD * i) * radius, (float) centerY + (float) Math.cos(DEG2RAD * i) * radius);
            rlColor4ub(color2.getR(), color2.getG(), color2.getB(), color2.getA());
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
    public void DrawCircleV(Vector2 center, float radius, Color color){
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
    public void DrawCircleLines(int centerX, int centerY, float radius, Color color){
        if(rlCheckBufferLimit(2 * 36)){
            rlglDraw();
        }

        rlBegin(RL_LINES);
        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

        // NOTE: Circle outline is drawn pixel by pixel every degree (0 to 360)
        for(int i = 0; i < 360; i += 10){
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
    public void DrawEllipse(int centerX, int centerY, float radiusH, float radiusV, Color color){
        if(rlCheckBufferLimit(3 * 36)){
            rlglDraw();
        }

        rlBegin(RL_TRIANGLES);
        for(int i = 0; i < 360; i += 10){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
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
    public void DrawEllipseLines(int centerX, int centerY, float radiusH, float radiusV, Color color){
        if(rlCheckBufferLimit(2 * 36)){
            rlglDraw();
        }

        rlBegin(RL_LINES);
        for(int i = 0; i < 360; i += 10){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
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
    public void DrawRing(Vector2 center, float innerRadius, float outerRadius, int startAngle, int endAngle, int segments, Color color){
        if(startAngle == endAngle){
            return;
        }

        // Function expects (outerRadius > innerRadius)
        if(outerRadius < innerRadius){
            float tmp = outerRadius;
            outerRadius = innerRadius;
            innerRadius = tmp;

            if(outerRadius <= 0.0f){
                outerRadius = 0.1f;
            }
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle){
            // Swap values
            int tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        if(segments < 4){
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / outerRadius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0){
                segments = 4;
            }
        }

        // Not a ring
        if(innerRadius <= 0.0f){
            DrawCircleSector(center, outerRadius, startAngle, endAngle, segments, color);
            return;
        }

        float stepLength = (float) (endAngle - startAngle) / (float) segments;
        float angle = (float) startAngle;

        if(SUPPORT_QUADS_DRAW_MODE){
            if(rlCheckBufferLimit(4 * segments)){
                rlglDraw();
            }

            rlEnableTexture(GetShapesTexture().getId());

            rlBegin(RL_QUADS);
            for(int i = 0; i < segments; i++){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(),
                        (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius,
                        center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(),
                        (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius,
                        center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(),
                        GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius,
                        center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                angle += stepLength;
            }
            rlEnd();

            rlDisableTexture();
        }
        else{
            if(rlCheckBufferLimit(6 * segments)){
                rlglDraw();
            }

            rlBegin(RL_TRIANGLES);
            for(int i = 0; i < segments; i++){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

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
    public void DrawRingLines(Vector2 center, float innerRadius, float outerRadius, int startAngle, int endAngle, int segments, Color color){
        if(startAngle == endAngle){
            return;
        }

        // Function expects (outerRadius > innerRadius)
        if(outerRadius < innerRadius){
            float tmp = outerRadius;
            outerRadius = innerRadius;
            innerRadius = tmp;

            if(outerRadius <= 0.0f){
                outerRadius = 0.1f;
            }
        }

        // Function expects (endAngle > startAngle)
        if(endAngle < startAngle){
            // Swap values
            int tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }

        if(segments < 4){
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / outerRadius, 2) - 1);
            segments = (int) ((endAngle - startAngle) * Math.ceil(2 * PI / th) / 360);

            if(segments <= 0){
                segments = 4;
            }
        }

        if(innerRadius <= 0.0f){
            DrawCircleSectorLines(center, outerRadius, startAngle, endAngle, segments, color);
            return;
        }

        float stepLength = (float) (endAngle - startAngle) / (float) segments;
        float angle = (float) startAngle;

        boolean showCapLines = true;
        int limit = 4 * (segments + 1);
        if((endAngle - startAngle) % 360 == 0){
            limit = 4 * segments;
            showCapLines = false;
        }

        if(rlCheckBufferLimit(limit)){
            rlglDraw();
        }

        rlBegin(RL_LINES);
        if(showCapLines){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);
        }

        for(int i = 0; i < segments; i++){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius,
                    center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius,
                    center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius,
                    center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius,
                    center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

            angle += stepLength;
        }

        if(showCapLines){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius,
                    center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
            rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius,
                    center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);
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
    public void DrawRectangle(int posX, int posY, int width, int height, Color color){
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
    public void DrawRectangleV(Vector2 position, Vector2 size, Color color){
        DrawRectanglePro(new Rectangle(position.getX(), position.getY(), size.getX(), size.getY()),
                new Vector2(0.0f, 0.0f), 0.0f, color);
    }

    /**
     * Draw a color-filled rectangle
     *
     * @param rec   rectangle shape to draw
     * @param color color to draw rectangle
     */
    public void DrawRectangleRec(Rectangle rec, Color color){
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
    public void DrawRectanglePro(Rectangle rec, Vector2 origin, float rotation, Color color){
        if(rlCheckBufferLimit(4)){
            rlglDraw();
        }

        rlEnableTexture(GetShapesTexture().getId());

        rlPushMatrix();
            rlTranslatef(rec.x, rec.y, 0.0f);
            rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
            rlTranslatef(-origin.getX(), -origin.getY(), 0.0f);

            rlBegin(RL_QUADS);
                rlNormal3f(0.0f, 0.0f, 1.0f);
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(0.0f, 0.0f);

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(0.0f, rec.getHeight());

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(rec.getWidth(), rec.getHeight());

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(rec.getWidth(), 0.0f);
            rlEnd();
        rlPopMatrix();

        rlDisableTexture();
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
    public void DrawRectangleGradientV(int posX, int posY, int width, int height, Color color1, Color color2){
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
    public void DrawRectangleGradientH(int posX, int posY, int width, int height, Color color1, Color color2){
        DrawRectangleGradientEx(new Rectangle((float) posX, (float) posY, (float) width, (float) height), color1, color1, color2, color2);
    }

    // Draw a gradient-filled rectangle
    // NOTE: Colors refer to corners, starting at top-lef corner and counter-clockwise
    public void DrawRectangleGradientEx(Rectangle rec, Color col1, Color col2, Color col3, Color col4){
        rlEnableTexture(GetShapesTexture().getId());

        rlPushMatrix();
        rlBegin(RL_QUADS);
        rlNormal3f(0.0f, 0.0f, 1.0f);

        // NOTE: Default raylib font character 95 is a white square
        rlColor4ub(col1.getR(), col1.getG(), col1.getB(), col1.getA());
        rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(),
                GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
        rlVertex2f(rec.getX(), rec.getY());

        rlColor4ub(col2.getR(), col2.getG(), col2.getB(), col2.getA());
        rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(),
                (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
        rlVertex2f(rec.getX(), rec.getY() + rec.getHeight());

        rlColor4ub(col3.getR(), col3.getG(), col3.getB(), col3.getA());
        rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(),
                (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
        rlVertex2f(rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight());

        rlColor4ub(col4.getR(), col4.getG(), col4.getB(), col4.getA());
        rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(),
                GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
        rlVertex2f(rec.getX() + rec.getWidth(), rec.getY());
        rlEnd();
        rlPopMatrix();

        rlDisableTexture();
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
    public void DrawRectangleLines(int posX, int posY, int width, int height, Color color){
        if(SUPPORT_QUADS_DRAW_MODE){
            DrawRectangle(posX, posY, width, 1, color);
            DrawRectangle(posX + width - 1, posY + 1, 1, height - 2, color);
            DrawRectangle(posX, posY + height - 1, width, 1, color);
            DrawRectangle(posX, posY + 1, 1, height - 2, color);
        }
        else{
            rlBegin(RL_LINES);
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
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
    public void DrawRectangleLinesEx(Rectangle rec, int lineThick, Color color){
        if(lineThick > rec.getWidth() || lineThick > rec.getHeight()){
            if(rec.getWidth() > rec.getHeight()){
                lineThick = (int) rec.getHeight() / 2;
            }
            else if(rec.getWidth() < rec.getHeight()){
                lineThick = (int) rec.getWidth() / 2;
            }
        }

        DrawRectangle((int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), lineThick, color);
        DrawRectangle((int) (rec.getX() - lineThick + rec.getWidth()), (int) (rec.getY() + lineThick), lineThick, (int) (rec.getHeight() - lineThick * 2.0f), color);
        DrawRectangle((int) rec.getX(), (int) (rec.getY() + rec.getHeight() - lineThick), (int) rec.getWidth(), lineThick, color);
        DrawRectangle((int) rec.getX(), (int) (rec.getY() + lineThick), lineThick, (int) (rec.getHeight() - lineThick * 2), color);
    }

    /**
     * Draw rectangle with rounded edges
     *
     * @param rec       Rectangle to draw
     * @param roundness degree to round corners
     * @param segments  number of segments
     * @param color     color to draw rectangle
     */
    public void DrawRectangleRounded(Rectangle rec, float roundness, int segments, Color color){
        // Not a rounded rectangle
        if((roundness <= 0.0f) || (rec.getWidth() < 1) || (rec.getHeight() < 1)){
            DrawRectangleRec(rec, color);
            return;
        }

        if(roundness >= 1.0f){
            roundness = 1.0f;
        }

        // Calculate corner radius
        float radius = (rec.getWidth() > rec.getHeight()) ? (rec.getHeight() * roundness) / 2 : (rec.getWidth() * roundness) / 2;
        if(radius <= 0.0f){
            return;
        }

        // Calculate number of segments to use for the corners
        if(segments < 4){
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) (Math.ceil(2 * PI / th) / 4.0f);
            if(segments <= 0){
                segments = 4;
            }
        }

        float stepLength = 90.0f / (float) segments;

        /*  Quick sketch to make sense of all of this (there are 9 parts to draw, also mark the 12 points we'll use below)
         *  Not my best attempt at ASCII art, just pretend it's rounded rectangle :)
         *     P0                    P1
         *       ____________________
         *     /|                    |\
         *    /1|          2         |3\
         *P7 /__|____________________|__\ P2
         *  |   |P8                P9|   |
         *  | 8 |          9         | 4 |
         *  | __|____________________|__ |
         *P6 \  |P11              P10|  / P3
         *    \7|          6         |5/
         *     \|____________________|/
         *     P5                    P4
         */

        // coordinates of the 12 points that define the rounded rect (the idea here is to make things easier)
        Vector2[] point = new Vector2[]{
                new Vector2(rec.getX() + radius, rec.getY()),
                new Vector2((rec.getX() + rec.getWidth()) - radius, rec.getY()),
                new Vector2(rec.getX() + rec.getWidth(), rec.getY() + radius),
                new Vector2(rec.getX() + rec.getWidth(), (rec.getY() + rec.getHeight()) - radius),
                new Vector2((rec.getX() + rec.getWidth()) - radius, rec.getY() + rec.getHeight()),
                new Vector2(rec.getX() + radius, rec.getY() + rec.getHeight()),
                new Vector2(rec.getX(), (rec.getY() + rec.getHeight()) - radius),
                new Vector2(rec.getX(), rec.getY() + radius),
                new Vector2(rec.getX() + radius, rec.getY() + radius),
                new Vector2((rec.getX() + rec.getWidth()) - radius, rec.getY() + radius),
                new Vector2((rec.getX() + rec.getWidth()) - radius, (rec.getY() + rec.getHeight()) - radius),
                new Vector2(rec.getX() + radius, (rec.getY() + rec.getHeight()) - radius)
        };

        Vector2[] centers = {
                point[8], point[9], point[10], point[11]
        };
        float[] angles = {
                180.0f, 90.0f, 0.0f, 270.0f
        };

        if(SUPPORT_QUADS_DRAW_MODE){
            if(rlCheckBufferLimit(16 * segments / 2 + 5 * 4)){
                rlglDraw();
            }

            rlEnableTexture(GetShapesTexture().getId());

            rlBegin(RL_QUADS);
            // Draw all of the 4 corners: [1] Upper Left Corner, [3] Upper Right Corner, [5] Lower Right Corner, [7] Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];
                // NOTE: Every QUAD actually represents two segments
                for(int i = 0; i < segments / 2; i++){
                    rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                    rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(),
                            GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX(), center.getY());
                    rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(),
                            (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius,
                            center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
                    rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(),
                            (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius,
                            center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);
                    rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(),
                            GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength * 2)) * radius,
                            center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength * 2)) * radius);
                    angle += (stepLength * 2);
                }
                // NOTE: In case number of segments is odd, we add one last piece to the cake
                if(segments % 2 == 1){
                    rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                    rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX(), center.getY());
                    rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius, center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
                    rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);
                    rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                    rlVertex2f(center.getX(), center.getY());
                }
            }

            // [2] Upper Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[0].getX(), point[0].getY());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[8].getX(), point[8].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[9].getX(), point[9].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[1].getX(), point[1].getY());

            // [4] Right Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[2].getX(), point[2].getY());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[9].getX(), point[9].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[10].getX(), point[10].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[3].getX(), point[3].getY());

            // [6] Bottom Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[5].getX(), point[5].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[4].getX(), point[4].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[10].getX(), point[10].getY());

            // [8] Left Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[7].getX(), point[7].getY());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[6].getX(), point[6].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[8].getX(), point[8].getY());

            // [9] Middle Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[8].getX(), point[8].getY());
            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(point[10].getX(), point[10].getY());
            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(point[9].getX(), point[9].getY());

            rlEnd();
            rlDisableTexture();
        }
        else{
            if(rlCheckBufferLimit(12 * segments + 5 * 6)){
                rlglDraw(); // 4 corners with 3 vertices per segment + 5 rectangles with 6 vertices each
            }

            rlBegin(RL_TRIANGLES);
            // Draw all of the 4 corners: [1] Upper Left Corner, [3] Upper Right Corner, [5] Lower Right Corner, [7] Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];
                for(int i = 0; i < segments; i++){
                    rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                    rlVertex2f(center.getX(), center.getY());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * radius,
                            center.getY() + (float) Math.cos(DEG2RAD * angle) * radius);
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * radius,
                            center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * radius);
                    angle += stepLength;
                }
            }

            // [2] Upper Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(point[0].getX(), point[0].getY());
            rlVertex2f(point[8].getX(), point[8].getY());
            rlVertex2f(point[9].getX(), point[9].getY());
            rlVertex2f(point[1].getX(), point[1].getY());
            rlVertex2f(point[0].getX(), point[0].getY());
            rlVertex2f(point[9].getX(), point[9].getY());

            // [4] Right Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(point[9].getX(), point[9].getY());
            rlVertex2f(point[10].getX(), point[10].getY());
            rlVertex2f(point[3].getX(), point[3].getY());
            rlVertex2f(point[2].getX(), point[2].getY());
            rlVertex2f(point[9].getX(), point[9].getY());
            rlVertex2f(point[3].getX(), point[3].getY());

            // [6] Bottom Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlVertex2f(point[5].getX(), point[5].getY());
            rlVertex2f(point[4].getX(), point[4].getY());
            rlVertex2f(point[10].getX(), point[10].getY());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlVertex2f(point[4].getX(), point[4].getY());

            // [8] Left Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(point[7].getX(), point[7].getY());
            rlVertex2f(point[6].getX(), point[6].getY());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlVertex2f(point[8].getX(), point[8].getY());
            rlVertex2f(point[7].getX(), point[7].getY());
            rlVertex2f(point[11].getX(), point[11].getY());

            // [9] Middle Rectangle
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(point[8].getX(), point[8].getY());
            rlVertex2f(point[11].getX(), point[11].getY());
            rlVertex2f(point[10].getX(), point[10].getY());
            rlVertex2f(point[9].getX(), point[9].getY());
            rlVertex2f(point[8].getX(), point[8].getY());
            rlVertex2f(point[10].getX(), point[10].getY());
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
    public void DrawRectangleRoundedLines(Rectangle rec, float roundness, int segments, int lineThick, Color color){
        if(lineThick < 0){
            lineThick = 0;
        }

        // Not a rounded rectangle
        if(roundness <= 0.0f){
            DrawRectangleLinesEx(new Rectangle(rec.getX() - lineThick, rec.getY() - lineThick, rec.getWidth() + 2 * lineThick, rec.getHeight() + 2 * lineThick), lineThick, color);
            return;
        }

        if(roundness >= 1.0f){
            roundness = 1.0f;
        }

        // Calculate corner radius
        float radius = (rec.getWidth() > rec.getHeight()) ? (rec.getHeight() * roundness) / 2 : (rec.getWidth() * roundness) / 2;
        if(radius <= 0.0f){
            return;
        }

        // Calculate number of segments to use for the corners
        if(segments < 4){
            // Calculate the maximum angle between segments based on the error rate (usually 0.5f)
            float th = (float) Math.acos(2 * Math.pow(1 - SMOOTH_CIRCLE_ERROR_RATE / radius, 2) - 1);
            segments = (int) (Math.ceil(2 * PI / th) / 2.0f);
            if(segments <= 0){
                segments = 4;
            }
        }

        float stepLength = 90.0f / (float) segments;
        float outerRadius = radius + (float) lineThick, innerRadius = radius;

        /*  Quick sketch to make sense of all of this (mark the 16 + 4(corner centers P16-19) points we'll use below)
         *  Not my best attempt at ASCII art, just preted it's rounded rectangle :)
         *     P0                     P1
         *        ====================
         *     // P8                P9 \\
         *    //                        \\
         *P7 // P15                  P10 \\ P2
         *  ||   *P16             P17*    ||
         *  ||                            ||
         *  || P14                   P11  ||
         *P6 \\  *P19             P18*   // P3
         *    \\                        //
         *     \\ P13              P12 //
         *        ====================
         *     P5                     P4
         */
        Vector2[] point = {
                new Vector2(rec.getX() + innerRadius, rec.getY() - lineThick),
                new Vector2((rec.getX() + rec.getWidth()) - innerRadius, rec.getY() - lineThick),
                new Vector2(rec.getX() + rec.getWidth() + lineThick, rec.getY() + innerRadius), // PO, P1, P2
                new Vector2(rec.getX() + rec.getWidth() + lineThick, (rec.getY() + rec.getHeight()) - innerRadius),
                new Vector2((rec.getX() + rec.getWidth()) - innerRadius, rec.getY() + rec.getHeight() + lineThick), // P3, P4
                new Vector2(rec.getX() + innerRadius, rec.getY() + rec.getHeight() + lineThick),
                new Vector2(rec.getX() - lineThick, (rec.getY() + rec.getHeight()) - innerRadius),
                new Vector2(rec.getX() - lineThick, rec.getY() + innerRadius), // P5, P6, P7
                new Vector2(rec.getX() + innerRadius, rec.getY()),
                new Vector2((rec.getX() + rec.getWidth()) - innerRadius, rec.getY()), // P8, P9
                new Vector2(rec.getX() + rec.getWidth(), rec.getY() + innerRadius),
                new Vector2(rec.getX() + rec.getWidth(), (rec.getY() + rec.getHeight()) - innerRadius), // P10, P11
                new Vector2((rec.getX() + rec.getWidth()) - innerRadius, rec.getY() + rec.getHeight()),
                new Vector2(rec.getX() + innerRadius, rec.getY() + rec.getHeight()), // P12, P13
                new Vector2(rec.getX(), (rec.getY() + rec.getHeight()) - innerRadius),
                new Vector2(rec.getX(), rec.getY() + innerRadius) // P14, P15
        };

        Vector2[] centers = {
                new Vector2(rec.getX() + innerRadius, rec.getY() + innerRadius),
                new Vector2((rec.getX() + rec.getWidth()) - innerRadius, rec.getY() + innerRadius), // P16, P17
                new Vector2(rec.getX() + rec.getWidth() - innerRadius, (rec.getY() + rec.getHeight()) - innerRadius),
                new Vector2(rec.getX() + innerRadius, (rec.getY() + rec.getHeight()) - innerRadius) // P18, P19
        };

        float[] angles = {180.0f, 90.0f, 0.0f, 270.0f};

        if(lineThick > 1){
            if(SUPPORT_QUADS_DRAW_MODE){
                if(rlCheckBufferLimit(4 * 4 * segments + 4 * 4)){
                    rlglDraw(); // 4 corners with 4 vertices for each segment + 4 rectangles with 4 vertices each
                }

                rlEnableTexture(GetShapesTexture().getId());

                rlBegin(RL_QUADS);
                // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
                for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
                {
                    float angle = angles[k];
                    Vector2 center = centers[k];
                    for(int i = 0; i < segments; i++){
                        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                        rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);
                        rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                        rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);
                        rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                        angle += stepLength;
                    }
                }

                // Upper rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[0].getX(), point[0].getY());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[8].getX(), point[8].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[9].getX(), point[9].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[1].getX(), point[1].getY());

                // Right rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[2].getX(), point[2].getY());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[10].getX(), point[10].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[11].getX(), point[11].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[3].getX(), point[3].getY());

                // Lower rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[13].getX(), point[13].getY());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[5].getX(), point[5].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[4].getX(), point[4].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[12].getX(), point[12].getY());

                // Left rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[15].getX(), point[15].getY());
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[7].getX(), point[7].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(point[6].getX(), point[6].getY());
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(point[14].getX(), point[14].getY());

                rlEnd();
                rlDisableTexture();
            }
            else{
                if(rlCheckBufferLimit(4 * 6 * segments + 4 * 6)){
                    rlglDraw(); // 4 corners with 6(2*3) vertices for each segment + 4 rectangles with 6 vertices each
                }

                rlBegin(RL_TRIANGLES);

                // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
                for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
                {
                    float angle = angles[k];
                    Vector2 center = centers[k];

                    for(int i = 0; i < segments; i++){
                        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * innerRadius);
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);

                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * innerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * innerRadius);
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                        rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);

                        angle += stepLength;
                    }
                }

                // Upper rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlVertex2f(point[0].getX(), point[0].getY());
                rlVertex2f(point[8].getX(), point[8].getY());
                rlVertex2f(point[9].getX(), point[9].getY());
                rlVertex2f(point[1].getX(), point[1].getY());
                rlVertex2f(point[0].getX(), point[0].getY());
                rlVertex2f(point[9].getX(), point[9].getY());

                // Right rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlVertex2f(point[10].getX(), point[10].getY());
                rlVertex2f(point[11].getX(), point[11].getY());
                rlVertex2f(point[3].getX(), point[3].getY());
                rlVertex2f(point[2].getX(), point[2].getY());
                rlVertex2f(point[10].getX(), point[10].getY());
                rlVertex2f(point[3].getX(), point[3].getY());

                // Lower rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlVertex2f(point[13].getX(), point[13].getY());
                rlVertex2f(point[5].getX(), point[5].getY());
                rlVertex2f(point[4].getX(), point[4].getY());
                rlVertex2f(point[12].getX(), point[12].getY());
                rlVertex2f(point[13].getX(), point[13].getY());
                rlVertex2f(point[4].getX(), point[4].getY());

                // Left rectangle
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlVertex2f(point[7].getX(), point[7].getY());
                rlVertex2f(point[6].getX(), point[6].getY());
                rlVertex2f(point[14].getX(), point[14].getY());
                rlVertex2f(point[15].getX(), point[15].getY());
                rlVertex2f(point[7].getX(), point[7].getY());
                rlVertex2f(point[14].getX(), point[14].getY());
                rlEnd();
            }
        }
        else{
            // Use LINES to draw the outline
            if(rlCheckBufferLimit(8 * segments + 4 * 2)){
                rlglDraw(); // 4 corners with 2 vertices for each segment + 4 rectangles with 2 vertices each
            }

            rlBegin(RL_LINES);

            // Draw all of the 4 corners first: Upper Left Corner, Upper Right Corner, Lower Right Corner, Lower Left Corner
            for(int k = 0; k < 4; ++k) // Hope the compiler is smart enough to unroll this loop
            {
                float angle = angles[k];
                Vector2 center = centers[k];

                for(int i = 0; i < segments; i++){
                    rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * angle) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * angle) * outerRadius);
                    rlVertex2f(center.getX() + (float) Math.sin(DEG2RAD * (angle + stepLength)) * outerRadius, center.getY() + (float) Math.cos(DEG2RAD * (angle + stepLength)) * outerRadius);
                    angle += stepLength;
                }
            }

            // And now the remaining 4 lines
            for(int i = 0; i < 8; i += 2){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
                rlVertex2f(point[i].getX(), point[i].getY());
                rlVertex2f(point[i + 1].getX(), point[i + 1].getY());
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
    public void DrawTriangle(Vector2 v1, Vector2 v2, Vector2 v3, Color color){
        if(rlCheckBufferLimit(4)){
            rlglDraw();
        }

        if(SUPPORT_QUADS_DRAW_MODE){
            rlEnableTexture(GetShapesTexture().getId());

            rlBegin(RL_QUADS);
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(v1.getX(), v1.getY());

            rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(v2.getX(), v2.getY());

            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
            rlVertex2f(v2.getX(), v2.getY());

            rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
            rlVertex2f(v3.getX(), v3.getY());
            rlEnd();

            rlDisableTexture();
        }
        else{
            rlBegin(RL_TRIANGLES);
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
            rlVertex2f(v1.getX(), v1.getY());
            rlVertex2f(v2.getX(), v2.getY());
            rlVertex2f(v3.getX(), v3.getY());
            rlEnd();
        }
    }

    /**
     * Draw a trianlge using lines
     * NOTE: Vertex must be provided in counter-clockwise order
     *
     * @param v1    X,Y coordinate (top vertex)
     * @param v2    X,Y coordinate (left vertex)
     * @param v3    X,Y coordinate (right vertex)
     * @param color color to draw triangle
     */
    public void DrawTriangleLines(Vector2 v1, Vector2 v2, Vector2 v3, Color color){
        if(rlCheckBufferLimit(6)){
            rlglDraw();
        }

        rlBegin(RL_LINES);
        rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
        rlVertex2f(v1.getX(), v1.getY());
        rlVertex2f(v2.getX(), v2.getY());

        rlVertex2f(v2.getX(), v2.getY());
        rlVertex2f(v3.getX(), v3.getY());

        rlVertex2f(v3.getX(), v3.getY());
        rlVertex2f(v1.getX(), v1.getY());
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
    public void DrawTriangleFan(Vector2[] points, int pointsCount, Color color){
        if(pointsCount >= 3){
            if(rlCheckBufferLimit((pointsCount - 2) * 4)){
                rlglDraw();
            }

            rlEnableTexture(GetShapesTexture().getId());
            rlBegin(RL_QUADS);
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            for(int i = 1; i < pointsCount - 1; i++){
                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(points[0].getX(), points[0].getY());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(points[i].getX(), points[i].getY());

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f(points[i + 1].getX(), points[i + 1].getY());

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(points[i + 1].getX(), points[i + 1].getY());
            }
            rlEnd();
            rlDisableTexture();
        }
    }

    /**
     * Draw a triangle strip defined by points
     * NOTE: Every new vertex connects with previous two
     *
     * @param points      Array of X, Y coordinates
     * @param pointsCount number of points
     * @param color       color to draw strip
     */
    public void DrawTriangleStrip(Vector2[] points, int pointsCount, Color color){
        if(pointsCount >= 3){
            if(rlCheckBufferLimit(3 * (pointsCount - 2))){
                rlglDraw();
            }

            rlBegin(RL_TRIANGLES);
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            for(int i = 2; i < pointsCount; i++){
                if((i % 2) == 0){
                    rlVertex2f(points[i].getX(), points[i].getY());
                    rlVertex2f(points[i - 2].getX(), points[i - 2].getY());
                    rlVertex2f(points[i - 1].getX(), points[i - 1].getY());
                }
                else{
                    rlVertex2f(points[i].getX(), points[i].getY());
                    rlVertex2f(points[i - 1].getX(), points[i - 1].getY());
                    rlVertex2f(points[i - 2].getX(), points[i - 2].getY());
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
    public void DrawPoly(Vector2 center, int sides, float radius, float rotation, Color color){
        if(sides < 3){
            sides = 3;
        }
        float centralAngle = 0.0f;

        if(rlCheckBufferLimit(4 * (360 / sides))){
            rlglDraw();
        }

        rlPushMatrix();
        rlTranslatef(center.getX(), center.getY(), 0.0f);
        rlRotatef(rotation, 0.0f, 0.0f, 1.0f);

        if(SUPPORT_QUADS_DRAW_MODE){
            rlEnableTexture(GetShapesTexture().getId());

            rlBegin(RL_QUADS);
            for(int i = 0; i < sides; i++){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f(0, 0);

                rlTexCoord2f(GetShapesTextureRec().getX() / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), (GetShapesTextureRec().getY() + GetShapesTextureRec().getHeight()) / GetShapesTexture().getHeight());
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);

                centralAngle += 360.0f / (float) sides;
                rlTexCoord2f((GetShapesTextureRec().getX() + GetShapesTextureRec().getWidth()) / GetShapesTexture().getWidth(), GetShapesTextureRec().getY() / GetShapesTexture().getHeight());
                rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
            }
            rlEnd();
            rlDisableTexture();
        }
        else{
            rlBegin(RL_TRIANGLES);
            for(int i = 0; i < sides; i++){
                rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

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
    public void DrawPolyLines(Vector2 center, int sides, float radius, float rotation, Color color){
        if(sides < 3){
            sides = 3;
        }
        float centralAngle = 0.0f;

        if(rlCheckBufferLimit(3 * (360 / sides))){
            rlglDraw();
        }

        rlPushMatrix();
        rlTranslatef(center.getX(), center.getY(), 0.0f);
        rlRotatef(rotation, 0.0f, 0.0f, 1.0f);

        rlBegin(RL_LINES);
        for(int i = 0; i < sides; i++){
            rlColor4ub(color.getR(), color.getG(), color.getB(), color.getA());

            rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
            centralAngle += 360.0f / (float) sides;
            rlVertex2f((float) Math.sin(DEG2RAD * centralAngle) * radius, (float) Math.cos(DEG2RAD * centralAngle) * radius);
        }
        rlEnd();
        rlPopMatrix();
    }

    /**
     * Check if point is inside rectangle
     *
     * @param point X, Y coordinate
     * @param rec   area to check
     * @return Is point inside rectangle
     */
    public boolean CheckCollisionPointRec(Vector2 point, Rectangle rec){
        boolean collision = false;

        if((point.getX() >= rec.getX()) && (point.getX() <= (rec.getX() + rec.getWidth())) && (point.getY() >= rec.getY()) && (point.getY() <= (rec.getY() + rec.getHeight()))){
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
    public boolean CheckCollisionPointCircle(Vector2 point, Vector2 center, float radius){
        return CheckCollisionCircles(point, 0, center, radius);
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
    public boolean CheckCollisionPointTriangle(Vector2 point, Vector2 p1, Vector2 p2, Vector2 p3){
        boolean collision = false;

        float alpha = ((p2.getY() - p3.getY()) * (point.getX() - p3.getX()) + (p3.getX() - p2.getX()) * (point.getY() - p3.getY())) /
                ((p2.getY() - p3.getY()) * (p1.getX() - p3.getX()) + (p3.getX() - p2.getX()) * (p1.getY() - p3.getY()));

        float beta = ((p3.getY() - p1.getY()) * (point.getX() - p3.getX()) + (p1.getX() - p3.getX()) * (point.getY() - p3.getY())) /
                ((p2.getY() - p3.getY()) * (p1.getX() - p3.getX()) + (p3.getX() - p2.getX()) * (p1.getY() - p3.getY()));

        float gamma = 1.0f - alpha - beta;

        if((alpha > 0) && (beta > 0) & (gamma > 0)){
            collision = true;
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
    public boolean CheckCollisionRecs(Rectangle rec1, Rectangle rec2){
        boolean collision = false;

        if((rec1.getX() < (rec2.getX() + rec2.getWidth()) && (rec1.getX() + rec1.getWidth()) > rec2.getX()) &&
                (rec1.getY() < (rec2.getY() + rec2.getHeight()) && (rec1.getY() + rec1.getHeight()) > rec2.getY())){
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
    public boolean CheckCollisionCircles(Vector2 center1, float radius1, Vector2 center2, float radius2){
        boolean collision = false;

        float dx = center2.getX() - center1.getX();      // X distance between centers
        float dy = center2.getY() - center1.getY();      // Y distance between centers

        float distance = (float) Math.sqrt(dx * dx + dy * dy); // Distance between centers

        if(distance <= (radius1 + radius2)){
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
    public boolean CheckCollisionCircleRec(Vector2 center, float radius, Rectangle rec){
        int recCenterX = (int) (rec.getX() + rec.getWidth() / 2.0f);
        int recCenterY = (int) (rec.getY() + rec.getHeight() / 2.0f);

        float dx = Math.abs(center.getX() - (float) recCenterX);
        float dy = Math.abs(center.getY() - (float) recCenterY);

        if(dx > (rec.getWidth() / 2.0f + radius)){
            return false;
        }
        if(dy > (rec.getHeight() / 2.0f + radius)){
            return false;
        }

        if(dx <= (rec.getWidth() / 2.0f)){
            return true;
        }
        if(dy <= (rec.getHeight() / 2.0f)){
            return true;
        }

        float cornerDistanceSq = (dx - rec.getWidth() / 2.0f) * (dx - rec.getWidth() / 2.0f) +
                (dy - rec.getHeight() / 2.0f) * (dy - rec.getHeight() / 2.0f);

        return (cornerDistanceSq <= (radius * radius));
    }

    /**
     * Check the collision between two lines defined by two points each
     *
     * @param startPos1 X, Y coordinate for initial endpoint of line 1
     * @param endPos1   X, Y coordinate for final endpoint of line 1
     * @param startPos2 X, Y coordinate for initial endpoint of line 2
     * @param endPos2   X, Y coordinate for final endpoint of line 2
     * @return if lines intersect
     */
    public boolean CheckCollisionLines(Vector2 startPos1, Vector2 endPos1, Vector2 startPos2, Vector2 endPos2){
        float div = (endPos2.getY() - startPos2.getY()) * (endPos1.getX() - startPos1.getX()) - (endPos2.getX() - startPos2.getX()) * (endPos1.getY() - startPos1.getY());

        if(div == 0.0f){
            return false;      // WARNING: This check could not work due to float precision rounding issues...
        }

        float xi = ((startPos2.getX() - endPos2.getX()) * (startPos1.getX() * endPos1.getY() - startPos1.getY() * endPos1.getX()) - (startPos1.getX() - endPos1.getX()) * (startPos2.getX() * endPos2.getY() - startPos2.getY() * endPos2.getX())) / div;
        float yi = ((startPos2.getY() - endPos2.getY()) * (startPos1.getX() * endPos1.getY() - startPos1.getY() * endPos1.getX()) - (startPos1.getY() - endPos1.getY()) * (startPos2.getX() * endPos2.getY() - startPos2.getY() * endPos2.getX())) / div;

        if(xi < Math.min(startPos1.getX(), endPos1.getX()) || xi > Math.max(startPos1.getX(), endPos1.getX())){
            return false;
        }
        if(xi < Math.min(startPos2.getX(), endPos2.getX()) || xi > Math.max(startPos2.getX(), endPos2.getX())){
            return false;
        }
        if(yi < Math.min(startPos1.getY(), endPos1.getY()) || yi > Math.max(startPos1.getY(), endPos1.getY())){
            return false;
        }
        if(yi < Math.min(startPos2.getY(), endPos2.getY()) || yi > Math.max(startPos2.getY(), endPos2.getY())){
            return false;
        }

        return true;
    }

    /**
     * Check the collision between two lines defined by two points each, returns collision point by reference
     *
     * @param startPos1 X, Y coordinate for initial endpoint of line 1
     * @param endPos1   X, Y coordinate for final endpoint of line 1
     * @param startPos2 X, Y coordinate for initial endpoint of line 2
     * @param endPos2   X, Y coordinate for final endpoint of line 2
     * @return X, Y coordinate of intersection
     */
    public Vector2 CheckCollisionLinesV(Vector2 startPos1, Vector2 endPos1, Vector2 startPos2, Vector2 endPos2){
        float div = (endPos2.getY() - startPos2.getY()) * (endPos1.getX() - startPos1.getX()) - (endPos2.getX() - startPos2.getX()) * (endPos1.getY() - startPos1.getY());

        if(div == 0.0f){
            return null;      // WARNING: This check could not work due to float precision rounding issues...
        }

        float xi = ((startPos2.getX() - endPos2.getX()) * (startPos1.getX() * endPos1.getY() - startPos1.getY() * endPos1.getX()) - (startPos1.getX() - endPos1.getX()) * (startPos2.getX() * endPos2.getY() - startPos2.getY() * endPos2.getX())) / div;
        float yi = ((startPos2.getY() - endPos2.getY()) * (startPos1.getX() * endPos1.getY() - startPos1.getY() * endPos1.getX()) - (startPos1.getY() - endPos1.getY()) * (startPos2.getX() * endPos2.getY() - startPos2.getY() * endPos2.getX())) / div;

        if(xi < Math.min(startPos1.getX(), endPos1.getX()) || xi > Math.max(startPos1.getX(), endPos1.getX())){
            return null;
        }
        if(xi < Math.min(startPos2.getX(), endPos2.getX()) || xi > Math.max(startPos2.getX(), endPos2.getX())){
            return null;
        }
        if(yi < Math.min(startPos1.getY(), endPos1.getY()) || yi > Math.max(startPos1.getY(), endPos1.getY())){
            return null;
        }
        if(yi < Math.min(startPos2.getY(), endPos2.getY()) || yi > Math.max(startPos2.getY(), endPos2.getY())){
            return null;
        }

        return new Vector2(xi, yi);
    }

    // Get collision rectangle for two rectangles collision
    public Rectangle GetCollisionRec(Rectangle rec1, Rectangle rec2){
        Rectangle rec = new Rectangle();

        if(CheckCollisionRecs(rec1, rec2)){
            float dxx = Math.abs(rec1.getX() - rec2.getX());
            float dyy = Math.abs(rec1.getY() - rec2.getY());

            if(rec1.getX() <= rec2.getX()){
                if(rec1.getY() <= rec2.getY()){
                    rec.setX(rec2.getX());
                    rec.setY(rec2.getY());
                    rec.setWidth(rec1.getWidth() - dxx);
                    rec.setHeight(rec1.getHeight() - dyy);
                }
                else{
                    rec.setX(rec2.getX());
                    rec.setY(rec1.getY());
                    rec.setWidth(rec1.getWidth() - dxx);
                    rec.setHeight(rec2.getHeight() - dyy);
                }
            }
            else{
                if(rec1.getY() <= rec2.getY()){
                    rec.setX(rec1.getX());
                    rec.setY(rec2.getY());
                    rec.setWidth(rec2.getWidth() - dxx);
                    rec.setHeight(rec1.getHeight() - dyy);
                }
                else{
                    rec.setX(rec1.getX());
                    rec.setY(rec1.getY());
                    rec.setWidth(rec2.getWidth() - dxx);
                    rec.setHeight(rec2.getHeight() - dyy);
                }
            }

            if(rec1.getWidth() > rec2.getWidth()){
                if(rec.getWidth() >= rec2.getWidth()){
                    rec.setWidth(rec2.getWidth());
                }
            }
            else{
                if(rec.getWidth() >= rec1.getWidth()){
                    rec.setWidth(rec1.getWidth());
                }
            }

            if(rec1.getHeight() > rec2.getHeight()){
                if(rec.getHeight() >= rec2.getHeight()){
                    rec.setHeight(rec2.getHeight());
                }
            }
            else{
                if(rec.getHeight() >= rec1.getHeight()){
                    rec.setHeight(rec1.getHeight());
                }
            }
        }

        return rec;
    }
}