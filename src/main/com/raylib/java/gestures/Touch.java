package com.raylib.java.gestures;

import com.raylib.java.structs.Vector2;

public class Touch {

    /**
     * Touch id for first touch point
     */
    int firstId;

    /**
     * Touch points counter
     */
    int pointCount;

    /**
     * Time stamp when an event happened
     */
    double eventTime;

    /**
     * Touch up position
     */
    Vector2 upPosition;

    /**
     * First touch down position
     */
    Vector2 downPositionA;

    /**
     * Second touch down position
     */
    Vector2 downPositionB;

    /**
     * Touch drag position
     */
    Vector2 downDragPosition;

    /**
     * First touch down position on move
     */
    Vector2 moveDownPositionA;

    /**
     * Second touch down position on move
     */
    Vector2 moveDownPositionB;

    /**
     * Previous position A to compare for pinch gestures
     */
    Vector2 previousPositionA;

    /**
     * Previous position B to compare for pinch gestures
     */
    Vector2 previousPositionB;

    /**
     * TAP counter (one tap implies TOUCH_ACTION_DOWN and TOUCH_ACTION_UP actions)
     */
    int tapCounter;

    public Touch() {
        this.firstId = 0;
        this.pointCount = 0;
        this.eventTime = 0;
        this.upPosition = new Vector2();
        this.downPositionA = new Vector2();
        this.downPositionB = new Vector2();
        this.downDragPosition = new Vector2();
        this.moveDownPositionA = new Vector2();
        this.moveDownPositionB = new Vector2();
        this.previousPositionA = new Vector2();
        this.previousPositionB = new Vector2();
        this.tapCounter = 0;
    }
}
