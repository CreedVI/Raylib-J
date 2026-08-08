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
    public final GesturesData gesturesData;

    public rGestures(Raylib context) {
        this.context = context;
        this.gesturesData = new GesturesData();
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    // Enable only desired gestures to be detected
    void SetGesturesEnabled(int flags) {
        gesturesData.enabledFlags = flags;
    }

    // Check if a gesture have been detected
    boolean IsGestureDetected(int gesture) {
        return (gesturesData.enabledFlags & gesturesData.current.getFlag()) == gesture;
    }

    // Process gesture event and translate it into gestures
    void ProcessGestureEvent(GestureEvent event) {
        // Reset required variables
        gesturesData.touch.pointCount = event.pointCount;      // Required on UpdateGestures()

        if (gesturesData.touch.pointCount == 1)     // One touch point
        {
            if (event.touchAction == TOUCH_ACTION_DOWN) {
                gesturesData.touch.tapCounter++;    // Tap counter

                // Detect GESTURE_DOUBLE_TAP
                if ((gesturesData.current == GESTURE_NONE) && (gesturesData.touch.tapCounter >= 2) && ((rgGetCurrentTime() - gesturesData.touch.eventTime) < TAP_TIMEOUT) && (rgVector2Distance(gesturesData.touch.downPositionA, event.position[0]) < DOUBLETAP_RANGE)) {
                    gesturesData.current = GESTURE_DOUBLETAP;
                    gesturesData.touch.tapCounter = 0;
                }
                else    // Detect GESTURE_TAP
                {
                    gesturesData.touch.tapCounter = 1;
                    gesturesData.current = GESTURE_TAP;
                }

                gesturesData.touch.downPositionA = event.position[0];
                gesturesData.touch.downDragPosition = event.position[0];

                gesturesData.touch.upPosition = gesturesData.touch.downPositionA;
                gesturesData.touch.eventTime = rgGetCurrentTime();

                gesturesData.swipe.startTime = rgGetCurrentTime();

                gesturesData.drag.vector = new Vector2();
            }
            else if (event.touchAction == TOUCH_ACTION_UP) {
                // A swipe can happen while the current gesture is drag, but (especially for web) also hold, so set upPosition for both cases
                if (gesturesData.current == GESTURE_DRAG || gesturesData.current == GESTURE_HOLD) {
                    gesturesData.touch.upPosition = event.position[0];
                }

                // NOTE: gestures.drag.intensity is dependent on the resolution of the screen
                gesturesData.drag.distance = rgVector2Distance(gesturesData.touch.downPositionA, gesturesData.touch.upPosition);
                gesturesData.drag.intensity = gesturesData.drag.distance / (float) ((rgGetCurrentTime() - gesturesData.swipe.startTime));

                // Detect GESTURE_SWIPE
                if ((gesturesData.drag.intensity > FORCE_TO_SWIPE) && (gesturesData.current != GESTURE_DRAG)) {
                    // NOTE: Angle should be inverted in Y
                    gesturesData.drag.angle = 360.0f - rgVector2Angle(gesturesData.touch.downPositionA, gesturesData.touch.upPosition);

                    if ((gesturesData.drag.angle < 30) || (gesturesData.drag.angle > 330)) {
                        gesturesData.current = GESTURE_SWIPE_RIGHT;          // Right
                    }
                    else if ((gesturesData.drag.angle >= 30) && (gesturesData.drag.angle <= 150)) {
                        gesturesData.current = GESTURE_SWIPE_UP;      // Up
                    }
                    else if ((gesturesData.drag.angle > 150) && (gesturesData.drag.angle < 210)) {
                        gesturesData.current = GESTURE_SWIPE_LEFT;     // Left
                    }
                    else if ((gesturesData.drag.angle >= 210) && (gesturesData.drag.angle <= 330)) {
                        gesturesData.current = GESTURE_SWIPE_DOWN;   // Down
                    }
                    else {
                        gesturesData.current = GESTURE_NONE;
                    }
                }
                else {
                    gesturesData.drag.distance = 0.0f;
                    gesturesData.drag.intensity = 0.0f;
                    gesturesData.drag.angle = 0.0f;

                    gesturesData.current = GESTURE_NONE;
                }

                gesturesData.touch.downDragPosition = new Vector2();
                gesturesData.touch.pointCount = 0;
            }
            else if (event.touchAction == TOUCH_ACTION_MOVE) {
                gesturesData.touch.moveDownPositionA = event.position[0];

                if (gesturesData.current == GESTURE_HOLD) {
                    if (gesturesData.hold.resetRequired) {
                        gesturesData.touch.downPositionA = event.position[0];
                    }

                    gesturesData.hold.resetRequired = false;

                    // Detect GESTURE_DRAG
                    if ((rgGetCurrentTime() - gesturesData.touch.eventTime) > DRAG_TIMEOUT) {
                        gesturesData.touch.eventTime = rgGetCurrentTime();
                        gesturesData.current = GESTURE_DRAG;
                    }
                }

                gesturesData.drag.vector.x = gesturesData.touch.moveDownPositionA.x - gesturesData.touch.downDragPosition.x;
                gesturesData.drag.vector.y = gesturesData.touch.moveDownPositionA.y - gesturesData.touch.downDragPosition.y;
            }
        }
        else if (gesturesData.touch.pointCount == 2)    // Two touch points
        {
            if (event.touchAction == TOUCH_ACTION_DOWN) {
                gesturesData.touch.downPositionA = event.position[0];
                gesturesData.touch.downPositionB = event.position[1];

                gesturesData.touch.previousPositionA = gesturesData.touch.downPositionA;
                gesturesData.touch.previousPositionB = gesturesData.touch.downPositionB;

                //gestures.pinch.distance = rgVector2Distance(gestures.touch.downPositionA, gestures.touch.downPositionB);

                gesturesData.pinch.vector.x = gesturesData.touch.downPositionB.x - gesturesData.touch.downPositionA.x;
                gesturesData.pinch.vector.y = gesturesData.touch.downPositionB.y - gesturesData.touch.downPositionA.y;

                gesturesData.current = GESTURE_HOLD;
                gesturesData.hold.timeDuration = rgGetCurrentTime();
            }
            else if (event.touchAction == TOUCH_ACTION_MOVE) {
                gesturesData.pinch.distance = rgVector2Distance(gesturesData.touch.moveDownPositionA, gesturesData.touch.moveDownPositionB);

                gesturesData.touch.moveDownPositionA = event.position[0];
                gesturesData.touch.moveDownPositionB = event.position[1];

                gesturesData.pinch.vector.x = gesturesData.touch.moveDownPositionB.x - gesturesData.touch.moveDownPositionA.x;
                gesturesData.pinch.vector.y = gesturesData.touch.moveDownPositionB.y - gesturesData.touch.moveDownPositionA.y;

                if ((rgVector2Distance(gesturesData.touch.previousPositionA, gesturesData.touch.moveDownPositionA) >= MINIMUM_PINCH) || (rgVector2Distance(gesturesData.touch.previousPositionB, gesturesData.touch.moveDownPositionB) >= MINIMUM_PINCH)) {
                    if (rgVector2Distance(gesturesData.touch.previousPositionA, gesturesData.touch.previousPositionB) > rgVector2Distance(gesturesData.touch.moveDownPositionA, gesturesData.touch.moveDownPositionB)) {
                        gesturesData.current = GESTURE_PINCH_IN;
                    }
                    else {
                        gesturesData.current = GESTURE_PINCH_OUT;
                    }
                }
                else {
                    gesturesData.current = GESTURE_HOLD;
                    gesturesData.hold.timeDuration = rgGetCurrentTime();
                }

                // NOTE: Angle should be inverted in Y
                gesturesData.pinch.angle = 360.0f - rgVector2Angle(gesturesData.touch.moveDownPositionA, gesturesData.touch.moveDownPositionB);
            }
            else if (event.touchAction == TOUCH_ACTION_UP) {
                gesturesData.pinch.distance = 0.0f;
                gesturesData.pinch.angle = 0.0f;
                gesturesData.pinch.vector = new Vector2();
                gesturesData.touch.pointCount = 0;

                gesturesData.current = GESTURE_NONE;
            }
        }
        else if (gesturesData.touch.pointCount > 2)     // More than two touch points
        {
            // TODO: Process gesture events for more than two points
        }
    }

    // Update gestures detected (must be called every frame)
    void UpdateGestures() {
        // NOTE: Gestures are processed through system callbacks on touch events

        // Detect GESTURE_HOLD
        if (((gesturesData.current == GESTURE_TAP) || (gesturesData.current == GESTURE_DOUBLETAP)) && (gesturesData.touch.pointCount < 2)) {
            gesturesData.current = GESTURE_HOLD;
            gesturesData.hold.timeDuration = rgGetCurrentTime();
        }

        // Detect GESTURE_NONE
        if ((gesturesData.current == GESTURE_SWIPE_RIGHT) || (gesturesData.current == GESTURE_SWIPE_UP) || (gesturesData.current == GESTURE_SWIPE_LEFT) || (gesturesData.current == GESTURE_SWIPE_DOWN)) {
            gesturesData.current = GESTURE_NONE;
        }
    }

    // Get latest detected gesture
    int GetGestureDetected() {
        // Get current gesture only if enabled
        return (gesturesData.enabledFlags & gesturesData.current.getFlag());
    }

    // hold time measured in seconds
    float GetGestureHoldDuration() {
        // NOTE: time is calculated on current gesture HOLD

        double time = 0.0;

        if (gesturesData.current == GESTURE_HOLD) {
            time = rgGetCurrentTime() - gesturesData.hold.timeDuration;
        }

        return (float) time;
    }

    // Get drag vector (between initial touch point to current)
    Vector2 GetGestureDragVector() {
        // NOTE: drag vector is calculated on one touch points TOUCH_ACTION_MOVE

        return gesturesData.drag.vector;
    }

    // Get drag angle
    // NOTE: Angle in degrees, horizontal-right is 0, counterclockwise
    float GetGestureDragAngle() {
        // NOTE: drag angle is calculated on one touch points TOUCH_ACTION_UP

        return gesturesData.drag.angle;
    }

    // Get distance between two pinch points
    Vector2 GetGesturePinchVector() {
        // NOTE: pinch distance is calculated on two touch points TOUCH_ACTION_MOVE

        return gesturesData.pinch.vector;
    }

    // Get angle between two pinch points
    // NOTE: Angle in degrees, horizontal-right is 0, counterclockwise
    float GetGesturePinchAngle() {
        // NOTE: pinch angle is calculated on two touch points TOUCH_ACTION_MOVE

        return gesturesData.pinch.angle;
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
