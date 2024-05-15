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
    public static class MouseButton {
        public static int
                MOUSE_BUTTON_LEFT    = 0,       // Mouse button left
                MOUSE_BUTTON_RIGHT   = 1,       // Mouse button right
                MOUSE_BUTTON_MIDDLE  = 2,       // Mouse button middle (pressed wheel)
                MOUSE_BUTTON_SIDE    = 3,       // Mouse button side (advanced mouse device)
                MOUSE_BUTTON_EXTRA   = 4,       // Mouse button extra (advanced mouse device)
                MOUSE_BUTTON_FORWARD = 5,       // Mouse button fordward (advanced mouse device)
                MOUSE_BUTTON_BACK    = 6;       // Mouse button back (advanced mouse device)

    }

    // Mouse cursor types
    public static class MouseCursor{
        public static int
                MOUSE_CURSOR_DEFAULT       = 0,     // Default pointer shape
                MOUSE_CURSOR_ARROW         = 1,     // Arrow shape
                MOUSE_CURSOR_IBEAM         = 2,     // Text writing cursor shape
                MOUSE_CURSOR_CROSSHAIR     = 3,     // Cross shape
                MOUSE_CURSOR_POINTING_HAND = 4,     // Pointing hand cursor
                MOUSE_CURSOR_RESIZE_EW     = 5,     // Horizontal resize/move arrow shape
                MOUSE_CURSOR_RESIZE_NS     = 6,     // Vertical resize/move arrow shape
                MOUSE_CURSOR_RESIZE_NWSE   = 7,     // Top-left to bottom-right diagonal resize/move arrow shape
                MOUSE_CURSOR_RESIZE_NESW   = 8,     // The top-right to bottom-left diagonal resize/move arrow shape
                MOUSE_CURSOR_RESIZE_ALL    = 9,     // The omni-directional resize/move cursor shape
                MOUSE_CURSOR_NOT_ALLOWED  = 10;     // The operation-not-allowed shape
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
