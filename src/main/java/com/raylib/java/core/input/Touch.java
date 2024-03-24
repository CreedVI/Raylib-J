package com.raylib.java.core.input;

import com.raylib.java.raymath.Vector2;

import static com.raylib.java.Config.MAX_TOUCH_POINTS;

public class Touch{

    public int pointCount;                // Number of touch points active
    public int[] pointId;                 // Point identifiers
    public Vector2[] position;            // Touch position on screen
    public boolean[] currentTouchState;   // Registers current touch state
    public boolean[] previousTouchState;  // Registers previous touch state

    public Touch(){
        pointId = new int[MAX_TOUCH_POINTS];
        position = new Vector2[MAX_TOUCH_POINTS];
        currentTouchState = new boolean[MAX_TOUCH_POINTS];
        previousTouchState = new boolean[MAX_TOUCH_POINTS];
    }

}
