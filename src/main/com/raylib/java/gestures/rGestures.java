package com.raylib.java.gestures;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.gestures.GestureEvent.Gesture.*;
import static com.raylib.java.gestures.GestureEvent.TouchAction.*;

public class rGestures {


    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    public static final float FORCE_TO_SWIPE = 0.2f;           // swipe force, measured in normalized screen units/time
    public static final float MINIMUM_DRAG = 0.015f;         // drag minimum force, measured in normalized screen units (0.0f to 1.0f)
    public static final float DRAG_TIMEOUT = 0.3f;       // drag minimum time for web, measured in seconds
    public static final float MINIMUM_PINCH = 0.005f;         // pinch minimum force, measured in normalized screen units (0.0f to 1.0f)
    public static final float TAP_TIMEOUT = 0.3f;       // Tap minimum time, measured in seconds
    public static final float PINCH_TIMEOUT = 0.3f;       // pinch minimum time, measured in seconds
    public static final float DOUBLETAP_RANGE = 0.03f;          // DoubleTap range, measured in normalized screen units (0.0f to 1.0f)


    //----------------------------------------------------------------------------------
    // Global Variables Definition
    //----------------------------------------------------------------------------------

    private final Raylib context;
    final GesturesData gestures;

    public rGestures(Raylib context) {
        this.context = context;
        this.gestures = new GesturesData();
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    // Enable only desired gestures to be detected
    void SetGesturesEnabled(int flags) {
        gestures.enabledFlags = flags;
    }

    // Check if a gesture have been detected
    boolean IsGestureDetected(int gesture) {
        return (gestures.enabledFlags & gestures.current.getFlag()) == gesture;
    }

    // Process gesture event and translate it into gestures
    void ProcessGestureEvent(GestureEvent event) {
        // Reset required variables
        gestures.touch.pointCount = event.pointCount;      // Required on UpdateGestures()

        if (gestures.touch.pointCount == 1)     // One touch point
        {
            if (event.touchAction == TOUCH_ACTION_DOWN) {
                gestures.touch.tapCounter++;    // Tap counter

                // Detect GESTURE_DOUBLE_TAP
                if ((gestures.current == GESTURE_NONE) && (gestures.touch.tapCounter >= 2) && ((rgGetCurrentTime() - gestures.touch.eventTime) < TAP_TIMEOUT) && (rgVector2Distance(gestures.touch.downPositionA, event.position[0]) < DOUBLETAP_RANGE)) {
                    gestures.current = GESTURE_DOUBLETAP;
                    gestures.touch.tapCounter = 0;
                }
                else    // Detect GESTURE_TAP
                {
                    gestures.touch.tapCounter = 1;
                    gestures.current = GESTURE_TAP;
                }

                gestures.touch.downPositionA = event.position[0];
                gestures.touch.downDragPosition = event.position[0];

                gestures.touch.upPosition = gestures.touch.downPositionA;
                gestures.touch.eventTime = rgGetCurrentTime();

                gestures.swipe.startTime = rgGetCurrentTime();

                gestures.drag.vector = new Vector2();
            }
            else if (event.touchAction == TOUCH_ACTION_UP) {
                // A swipe can happen while the current gesture is drag, but (especially for web) also hold, so set upPosition for both cases
                if (gestures.current == GESTURE_DRAG || gestures.current == GESTURE_HOLD) {
                    gestures.touch.upPosition = event.position[0];
                }

                // NOTE: gestures.drag.intensity is dependent on the resolution of the screen
                gestures.drag.distance = rgVector2Distance(gestures.touch.downPositionA, gestures.touch.upPosition);
                gestures.drag.intensity = gestures.drag.distance / (float) ((rgGetCurrentTime() - gestures.swipe.startTime));

                // Detect GESTURE_SWIPE
                if ((gestures.drag.intensity > FORCE_TO_SWIPE) && (gestures.current != GESTURE_DRAG)) {
                    // NOTE: Angle should be inverted in Y
                    gestures.drag.angle = 360.0f - rgVector2Angle(gestures.touch.downPositionA, gestures.touch.upPosition);

                    if ((gestures.drag.angle < 30) || (gestures.drag.angle > 330)) {
                        gestures.current = GESTURE_SWIPE_RIGHT;          // Right
                    }
                    else if ((gestures.drag.angle >= 30) && (gestures.drag.angle <= 150)) {
                        gestures.current = GESTURE_SWIPE_UP;      // Up
                    }
                    else if ((gestures.drag.angle > 150) && (gestures.drag.angle < 210)) {
                        gestures.current = GESTURE_SWIPE_LEFT;     // Left
                    }
                    else if ((gestures.drag.angle >= 210) && (gestures.drag.angle <= 330)) {
                        gestures.current = GESTURE_SWIPE_DOWN;   // Down
                    }
                    else {
                        gestures.current = GESTURE_NONE;
                    }
                }
                else {
                    gestures.drag.distance = 0.0f;
                    gestures.drag.intensity = 0.0f;
                    gestures.drag.angle = 0.0f;

                    gestures.current = GESTURE_NONE;
                }

                gestures.touch.downDragPosition = new Vector2();
                gestures.touch.pointCount = 0;
            }
            else if (event.touchAction == TOUCH_ACTION_MOVE) {
                gestures.touch.moveDownPositionA = event.position[0];

                if (gestures.current == GESTURE_HOLD) {
                    if (gestures.hold.resetRequired) {
                        gestures.touch.downPositionA = event.position[0];
                    }

                    gestures.hold.resetRequired = false;

                    // Detect GESTURE_DRAG
                    if ((rgGetCurrentTime() - gestures.touch.eventTime) > DRAG_TIMEOUT) {
                        gestures.touch.eventTime = rgGetCurrentTime();
                        gestures.current = GESTURE_DRAG;
                    }
                }

                gestures.drag.vector.x = gestures.touch.moveDownPositionA.x - gestures.touch.downDragPosition.x;
                gestures.drag.vector.y = gestures.touch.moveDownPositionA.y - gestures.touch.downDragPosition.y;
            }
        }
        else if (gestures.touch.pointCount == 2)    // Two touch points
        {
            if (event.touchAction == TOUCH_ACTION_DOWN) {
                gestures.touch.downPositionA = event.position[0];
                gestures.touch.downPositionB = event.position[1];

                gestures.touch.previousPositionA = gestures.touch.downPositionA;
                gestures.touch.previousPositionB = gestures.touch.downPositionB;

                //gestures.pinch.distance = rgVector2Distance(gestures.touch.downPositionA, gestures.touch.downPositionB);

                gestures.pinch.vector.x = gestures.touch.downPositionB.x - gestures.touch.downPositionA.x;
                gestures.pinch.vector.y = gestures.touch.downPositionB.y - gestures.touch.downPositionA.y;

                gestures.current = GESTURE_HOLD;
                gestures.hold.timeDuration = rgGetCurrentTime();
            }
            else if (event.touchAction == TOUCH_ACTION_MOVE) {
                gestures.pinch.distance = rgVector2Distance(gestures.touch.moveDownPositionA, gestures.touch.moveDownPositionB);

                gestures.touch.moveDownPositionA = event.position[0];
                gestures.touch.moveDownPositionB = event.position[1];

                gestures.pinch.vector.x = gestures.touch.moveDownPositionB.x - gestures.touch.moveDownPositionA.x;
                gestures.pinch.vector.y = gestures.touch.moveDownPositionB.y - gestures.touch.moveDownPositionA.y;

                if ((rgVector2Distance(gestures.touch.previousPositionA, gestures.touch.moveDownPositionA) >= MINIMUM_PINCH) || (rgVector2Distance(gestures.touch.previousPositionB, gestures.touch.moveDownPositionB) >= MINIMUM_PINCH)) {
                    if (rgVector2Distance(gestures.touch.previousPositionA, gestures.touch.previousPositionB) > rgVector2Distance(gestures.touch.moveDownPositionA, gestures.touch.moveDownPositionB)) {
                        gestures.current = GESTURE_PINCH_IN;
                    }
                    else {
                        gestures.current = GESTURE_PINCH_OUT;
                    }
                }
                else {
                    gestures.current = GESTURE_HOLD;
                    gestures.hold.timeDuration = rgGetCurrentTime();
                }

                // NOTE: Angle should be inverted in Y
                gestures.pinch.angle = 360.0f - rgVector2Angle(gestures.touch.moveDownPositionA, gestures.touch.moveDownPositionB);
            }
            else if (event.touchAction == TOUCH_ACTION_UP) {
                gestures.pinch.distance = 0.0f;
                gestures.pinch.angle = 0.0f;
                gestures.pinch.vector = new Vector2();
                gestures.touch.pointCount = 0;

                gestures.current = GESTURE_NONE;
            }
        }
        else if (gestures.touch.pointCount > 2)     // More than two touch points
        {
            // TODO: Process gesture events for more than two points
        }
    }

    // Update gestures detected (must be called every frame)
    void UpdateGestures() {
        // NOTE: Gestures are processed through system callbacks on touch events

        // Detect GESTURE_HOLD
        if (((gestures.current == GESTURE_TAP) || (gestures.current == GESTURE_DOUBLETAP)) && (gestures.touch.pointCount < 2)) {
            gestures.current = GESTURE_HOLD;
            gestures.hold.timeDuration = rgGetCurrentTime();
        }

        // Detect GESTURE_NONE
        if ((gestures.current == GESTURE_SWIPE_RIGHT) || (gestures.current == GESTURE_SWIPE_UP) || (gestures.current == GESTURE_SWIPE_LEFT) || (gestures.current == GESTURE_SWIPE_DOWN)) {
            gestures.current = GESTURE_NONE;
        }
    }

    // Get latest detected gesture
    int GetGestureDetected() {
        // Get current gesture only if enabled
        return (gestures.enabledFlags & gestures.current.getFlag());
    }

    // hold time measured in seconds
    float GetGestureHoldDuration() {
        // NOTE: time is calculated on current gesture HOLD

        double time = 0.0;

        if (gestures.current == GESTURE_HOLD) {
            time = rgGetCurrentTime() - gestures.hold.timeDuration;
        }

        return (float) time;
    }

    // Get drag vector (between initial touch point to current)
    Vector2 GetGestureDragVector() {
        // NOTE: drag vector is calculated on one touch points TOUCH_ACTION_MOVE

        return gestures.drag.vector;
    }

    // Get drag angle
    // NOTE: Angle in degrees, horizontal-right is 0, counterclockwise
    float GetGestureDragAngle() {
        // NOTE: drag angle is calculated on one touch points TOUCH_ACTION_UP

        return gestures.drag.angle;
    }

    // Get distance between two pinch points
    Vector2 GetGesturePinchVector() {
        // NOTE: pinch distance is calculated on two touch points TOUCH_ACTION_MOVE

        return gestures.pinch.vector;
    }

    // Get angle between two pinch points
    // NOTE: Angle in degrees, horizontal-right is 0, counterclockwise
    float GetGesturePinchAngle() {
        // NOTE: pinch angle is calculated on two touch points TOUCH_ACTION_MOVE

        return gestures.pinch.angle;
    }

    //----------------------------------------------------------------------------------
    // Module Internal Functions Definition
    //----------------------------------------------------------------------------------

    // Get angle from two-points vector with X-axis
    private float rgVector2Angle(Vector2 v1, Vector2 v2) {
        float angle = (float) (Math.atan2(v2.y - v1.y, v2.x - v1.x) * (180.0f / Math.PI));

        if (angle < 0) {
            angle += 360.0f;
        }

        return angle;
    }

    // Calculate distance between two Vector2
    private float rgVector2Distance(Vector2 v1, Vector2 v2) {
        float result;

        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;

        result = (float) Math.sqrt(dx * dx + dy * dy);

        return result;
    }

    private double rgGetCurrentTime() {
        return System.currentTimeMillis();
    }
}
