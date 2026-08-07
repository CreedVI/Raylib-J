package com.raylib.java.gestures;

import com.raylib.java.structs.Vector2;

import static com.raylib.java.Config.MAX_TOUCH_POINTS;

public class GestureEvent {

    /**
     * Gestures Type
     */
    public enum Gesture {
        GESTURE_NONE(0),
        GESTURE_TAP(1),
        GESTURE_DOUBLETAP(2),
        GESTURE_HOLD(4),
        GESTURE_DRAG(8),
        GESTURE_SWIPE_RIGHT(16),
        GESTURE_SWIPE_LEFT(32),
        GESTURE_SWIPE_UP(64),
        GESTURE_SWIPE_DOWN(128),
        GESTURE_PINCH_IN(256),
        GESTURE_PINCH_OUT(512);

        private final int flag;

        Gesture(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }
    }

    /**
     * Touch action type
     */
    public enum TouchAction {
        TOUCH_ACTION_UP,
        TOUCH_ACTION_DOWN,
        TOUCH_ACTION_MOVE,
        TOUCH_ACTION_CANCEL
    }

    TouchAction touchAction;
    int pointCount;
    int[] pointId;
    Vector2[] position;

    public GestureEvent() {
        this.touchAction = TouchAction.TOUCH_ACTION_UP;
        this.pointCount = 0;
        this.pointId = new int[MAX_TOUCH_POINTS];
        this.position = new Vector2[MAX_TOUCH_POINTS];
    }

}
