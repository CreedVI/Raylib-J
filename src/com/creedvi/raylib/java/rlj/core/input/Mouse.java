package com.creedvi.raylib.java.rlj.core.input;

import com.creedvi.raylib.java.rlj.raymath.Vector2;

public class Mouse{

    Vector2 position;               // Mouse position on screen
    Vector2 offset;                 // Mouse offset
    Vector2 scale;                  // Mouse scaling

    long cursor;                     // Tracks current mouse cursor
    boolean cursorHidden;              // Track if cursor is hidden
    boolean cursorOnScreen;            // Tracks if cursor is inside client area

    int[] currentButtonState;     // Registers current mouse button state
    int[] previousButtonState;    // Registers previous mouse button state
    float currentWheelMove;         // Registers current mouse wheel variation
    float previousWheelMove;        // Registers previous mouse wheel variation


    // Mouse buttons
    enum MouseButton {
        MOUSE_LEFT_BUTTON  (0),
        MOUSE_RIGHT_BUTTON (1),
        MOUSE_MIDDLE_BUTTON(2);

        MouseButton(int i){
        }
    }

    // Mouse cursor types
    public enum MouseCursor{
        MOUSE_CURSOR_DEFAULT(0),
        MOUSE_CURSOR_ARROW(1),
        MOUSE_CURSOR_IBEAM(2),
        MOUSE_CURSOR_CROSSHAIR(3),
        MOUSE_CURSOR_POINTING_HAND(4),
        MOUSE_CURSOR_RESIZE_EW(5),     // The horizontal resize/move arrow shape
        MOUSE_CURSOR_RESIZE_NS(6),     // The vertical resize/move arrow shape
        MOUSE_CURSOR_RESIZE_NWSE(7),     // The top-left to bottom-right diagonal resize/move arrow shape
        MOUSE_CURSOR_RESIZE_NESW(8),     // The top-right to bottom-left diagonal resize/move arrow shape
        MOUSE_CURSOR_RESIZE_ALL(9),     // The omni-directional resize/move cursor shape
        MOUSE_CURSOR_NOT_ALLOWED(10);     // The operation-not-allowed shape

        int mouseCursorInt;

        MouseCursor(int i){
            mouseCursorInt = i;
        }

        public int getMouseCursorInt(){
            return mouseCursorInt;
        }
    }

    public Mouse(){
        position = new Vector2();
        offset = new Vector2();
        scale = new Vector2();

        currentButtonState = new int[3];
        previousButtonState = new int[3];
    }

    public Vector2 getPosition(){
        return position;
    }

    public void setPosition(Vector2 position){
        this.position = position;
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

    public float getCurrentWheelMove(){
        return currentWheelMove;
    }

    public void setCurrentWheelMove(float currentWheelMove){
        this.currentWheelMove = currentWheelMove;
    }

    public float getPreviousWheelMove(){
        return previousWheelMove;
    }

    public void setPreviousWheelMove(float previousWheelMove){
        this.previousWheelMove = previousWheelMove;
    }

    public int getMouseCursor(MouseCursor mouseCursor){
        return mouseCursor.getMouseCursorInt();
    }
}
