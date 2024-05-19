package com.raylib.java.core.input;

import com.raylib.java.raymath.Vector2;

public class Mouse{

    public Vector2 currentPosition;               // Mouse position on screen
    public Vector2 previousPosition;               // last mouse position on screen
    public Vector2 offset;                 // Mouse offset
    public Vector2 scale;                  // Mouse scaling

    public long cursor;                     // Tracks current mouse cursor
    public boolean cursorHidden;              // Track if cursor is hidden
    public boolean cursorOnScreen;            // Tracks if cursor is inside client area

    public int[] currentButtonState;     // Registers current mouse button state
    public int[] previousButtonState;    // Registers previous mouse button state
    public Vector2 currentWheelMove;         // Registers current mouse wheel variation
    public Vector2 previousWheelMove;        // Registers previous mouse wheel variation


    // Mouse buttons
    public enum MouseButton {
                MOUSE_BUTTON_LEFT,       // Mouse button left
                MOUSE_BUTTON_RIGHT,       // Mouse button right
                MOUSE_BUTTON_MIDDLE,       // Mouse button middle (pressed wheel)
                MOUSE_BUTTON_SIDE,       // Mouse button side (advanced mouse device)
                MOUSE_BUTTON_EXTRA,       // Mouse button extra (advanced mouse device)
                MOUSE_BUTTON_FORWARD,       // Mouse button fordward (advanced mouse device)
                MOUSE_BUTTON_BACK;       // Mouse button back (advanced mouse device)

    }

    // Mouse cursor types
    public enum MouseCursor {
                MOUSE_CURSOR_DEFAULT,     // Default pointer shape
                MOUSE_CURSOR_ARROW,     // Arrow shape
                MOUSE_CURSOR_IBEAM,     // Text writing cursor shape
                MOUSE_CURSOR_CROSSHAIR,     // Cross shape
                MOUSE_CURSOR_POINTING_HAND,     // Pointing hand cursor
                MOUSE_CURSOR_RESIZE_EW,     // Horizontal resize/move arrow shape
                MOUSE_CURSOR_RESIZE_NS,     // Vertical resize/move arrow shape
                MOUSE_CURSOR_RESIZE_NWSE,     // Top-left to bottom-right diagonal resize/move arrow shape
                MOUSE_CURSOR_RESIZE_NESW,     // The top-right to bottom-left diagonal resize/move arrow shape
                MOUSE_CURSOR_RESIZE_ALL,     // The omni-directional resize/move cursor shape
                MOUSE_CURSOR_NOT_ALLOWED;     // The operation-not-allowed shape
    }

    public Mouse(){
        currentPosition = new Vector2();
        previousPosition = new Vector2();
        offset = new Vector2();
        scale = new Vector2();

        currentButtonState = new int[3];
        previousButtonState = new int[3];
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

    public long getCursor(){
        return cursor;
    }

    public void setCursor(long cursor){
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
