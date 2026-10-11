package com.raylib.java.core.input;

import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;

public class Mouse{

    /**
     * Mouse position on screen
     */
    public Vector2 currentPosition;

    /**
     * Last mouse position on screen
     */
    public Vector2 previousPosition;

    /**
     * Mouse position when locked
     */
    public Vector2 lockedPosition;

    /**
     * Mouse offset
     */
    public Vector2 offset;

    /**
     * Mouse scaling
     */
    public Vector2 scale;

    /**
     * Tracks status of current mouse cursor
     */
    public MouseCursor cursor;

    /**
     * Track if mouse cursor is hidden
     */
    public boolean cursorHidden;

    /**
     * Track if the mouse cursor is inside the client area
     */
    public boolean cursorOnScreen;

    /**
     * Track if the mouse cursor is locked (i.e. disabled)
     */
    public boolean cursorLocked;

    /**
     * Registers current mouse button states
     */
    public int[] currentButtonState;

    /**
     * Registers previous mouse button states
     */
    public int[] previousButtonState;

    /**
     * Registers the current mouse wheel movement variation
     */
    public Vector2 currentWheelMove;

    /**
     * Registers the previous mouse wheel movement variation
     */
    public Vector2 previousWheelMove;


    /**
     * Mouse buttons
     */
    public enum MouseButton {
        /**
         * Mouse button left
         */
        MOUSE_BUTTON_LEFT(0),
        /**
         * Mouse button right
         */
        MOUSE_BUTTON_RIGHT(1),
        /**
         * Mouse button middle (pressed wheel)
         */
        MOUSE_BUTTON_MIDDLE(2),
        /**
         * Mouse button side (advanced mouse device)
         */
        MOUSE_BUTTON_SIDE(3),
        /**
         * Mouse button extra (advanced mouse device)
         */
        MOUSE_BUTTON_EXTRA(4),
        /**
         * Mouse button forward (advanced mouse device)
         */
        MOUSE_BUTTON_FORWARD(5),
        /**
         * Mouse button back (advanced mouse device)
         */
        MOUSE_BUTTON_BACK(6);

        private final int value;

        MouseButton(int value) {
            this.value = value;
        }

        public int GetValue() {
            return value;
        }
    }

    /**
     * Mouse cursor types
     */
    public enum MouseCursor {
        /**
         * Default pointer shape
         */
        MOUSE_CURSOR_DEFAULT(0),
        /**
         * Arrow shape
         */
        MOUSE_CURSOR_ARROW(1),
        /**
         * Text writing cursor shape
         */
        MOUSE_CURSOR_IBEAM(2),
        /**
         * Cross shape
         */
        MOUSE_CURSOR_CROSSHAIR(3),
        /**
         * Pointing hand cursor
         */
        MOUSE_CURSOR_POINTING_HAND(4),
        /**
         * Horizontal resize/move arrow shape
         */
        MOUSE_CURSOR_RESIZE_EW(5),
        /**
         * Vertical resize/move arrow shape
         */
        MOUSE_CURSOR_RESIZE_NS(6),
        /**
         * Top-left to bottom-right diagonal resize/move arrow shape
         */
        MOUSE_CURSOR_RESIZE_NWSE(7),
        /**
         * The top-right to bottom-left diagonal resize/move arrow shape
         */
        MOUSE_CURSOR_RESIZE_NESW(8),
        /**
         * The omni-directional resize/move cursor shape
         */
        MOUSE_CURSOR_RESIZE_ALL(9),
        /**
         * The operation-not-allowed shape
         */
        MOUSE_CURSOR_NOT_ALLOWED(10);

        private final int value;

        MouseCursor(int value) {
            this.value = value;
        }

        public int GetValue() {
            return value;
        }
    }

    public Mouse(){
        currentPosition = new Vector2();
        previousPosition = new Vector2();

        offset = new Vector2();
        scale = new Vector2();

        currentWheelMove = new Vector2();
        previousWheelMove = new Vector2();

        currentButtonState = new int[10];
        previousButtonState = new int[10];
    }

    public Vector2 getPreviousPosition(){
        return previousPosition;
    }

    public void setPreviousPosition(Vector2 previousPosition){
        this.previousPosition = previousPosition;
    }

    public Vector2 getCurrentPosition(){
        return currentPosition;
    }

    public void setCurrentPosition(Vector2 currentPosition){
        this.currentPosition = currentPosition;
    }

    public Vector2 getOffset(){
        return offset;
    }

    public void setOffset(Vector2 offset){
        this.offset = offset;
    }

    public Vector2 getScale(){
        return scale;
    }

    public void setScale(Vector2 scale){
        this.scale = scale;
    }

    public MouseCursor getCursor() {
        return cursor;
    }

    public void setCursor(MouseCursor cursor) {
        this.cursor = cursor;
    }

    public boolean isCursorHidden(){
        return cursorHidden;
    }

    public void setCursorHidden(boolean cursorHidden){
        this.cursorHidden = cursorHidden;
    }

    public boolean isCursorOnScreen(){
        return cursorOnScreen;
    }

    public void setCursorOnScreen(boolean cursorOnScreen){
        this.cursorOnScreen = cursorOnScreen;
    }

    public int[] getCurrentButtonState(){
        return currentButtonState;
    }

    public void setCurrentButtonState(int[] currentButtonState){
        this.currentButtonState = currentButtonState;
    }

    public int[] getPreviousButtonState(){
        return previousButtonState;
    }

    public void setPreviousButtonState(int[] previousButtonState){
        this.previousButtonState = previousButtonState;
    }

    public Vector2 getCurrentWheelMove(){
        return currentWheelMove;
    }

    public void setCurrentWheelMove(Vector2 currentWheelMove){
        this.currentWheelMove = currentWheelMove;
    }

    public Vector2 getPreviousWheelMove(){
        return previousWheelMove;
    }

    public void setPreviousWheelMove(Vector2 previousWheelMove){
        this.previousWheelMove = previousWheelMove;
    }
}
