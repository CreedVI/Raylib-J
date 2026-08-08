package com.raylib.java.gestures;

public class GesturesData {

    /**
     * Current detected gesture
     */
    public GestureEvent.Gesture current;

    /**
     * Enabled gestures flags
     */
    int enabledFlags;

    Touch touch;
    Hold hold;
    Drag drag;
    Swipe swipe;
    Pinch pinch;

    public GesturesData() {
        this.current = GestureEvent.Gesture.GESTURE_NONE;
        this.enabledFlags = 0;
        this.touch = new Touch();
        this.hold = new Hold();
        this.drag = new Drag();
        this.swipe = new Swipe();
        this.pinch = new Pinch();
    }

}
