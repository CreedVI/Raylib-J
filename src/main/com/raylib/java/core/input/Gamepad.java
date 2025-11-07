package com.raylib.java.core.input;

public class Gamepad{

    public int lastButtonPressed;          // Register last gamepad button pressed
    int axisCount;                  // Register number of available gamepad axis
    public boolean[] ready;// Flag to know if gamepad is ready
    public float[][] axisState;// Gamepad axis state
    public byte[][] currentButtonState;// Current gamepad buttons state
    public byte[][] previousButtonState;// Previous gamepad buttons state

    public static class GamepadNumber {
        public static final int
                GAMEPAD_PLAYER1 = 0,
                GAMEPAD_PLAYER2 = 1,
                GAMEPAD_PLAYER3 = 2,
                GAMEPAD_PLAYER4 = 3;
    }

    public static class GamepadButton {
        public final static int
                GAMEPAD_BUTTON_UNKNOWN           = 0,   // Unknown button, just for error checking
                GAMEPAD_BUTTON_LEFT_FACE_UP      = 1,   // Gamepad left DPAD up button
                GAMEPAD_BUTTON_LEFT_FACE_RIGHT   = 2,   // Gamepad left DPAD right button
                GAMEPAD_BUTTON_LEFT_FACE_DOWN    = 3,   // Gamepad left DPAD down button
                GAMEPAD_BUTTON_LEFT_FACE_LEFT    = 4,   // Gamepad left DPAD left button
                GAMEPAD_BUTTON_RIGHT_FACE_UP     = 5,   // Gamepad right button up (i.e. PS3: Triangle, Xbox: Y)
                GAMEPAD_BUTTON_RIGHT_FACE_RIGHT  = 6,   // Gamepad right button right (i.e. PS3: Square, Xbox: X)
                GAMEPAD_BUTTON_RIGHT_FACE_DOWN   = 7,   // Gamepad right button down (i.e. PS3: Cross, Xbox: A)
                GAMEPAD_BUTTON_RIGHT_FACE_LEFT   = 8,   // Gamepad right button left (i.e. PS3: Circle, Xbox: B)
                GAMEPAD_BUTTON_LEFT_TRIGGER_1    = 9,   // Gamepad top/back trigger left (first), it could be a trailing button
                GAMEPAD_BUTTON_LEFT_TRIGGER_2    = 10,  // Gamepad top/back trigger left (second), it could be a trailing button
                GAMEPAD_BUTTON_RIGHT_TRIGGER_1   = 11,  // Gamepad top/back trigger right (one), it could be a trailing button
                GAMEPAD_BUTTON_RIGHT_TRIGGER_2   = 12,  // Gamepad top/back trigger right (second), it could be a trailing button
                GAMEPAD_BUTTON_MIDDLE_LEFT       = 13,  // Gamepad center buttons, left one (i.e. PS3: Select)
                GAMEPAD_BUTTON_MIDDLE            = 14,  // Gamepad center buttons, middle one (i.e. PS3: PS, Xbox: XBOX)
                GAMEPAD_BUTTON_MIDDLE_RIGHT      = 15,  // Gamepad center buttons, right one (i.e. PS3: Start)
                GAMEPAD_BUTTON_LEFT_THUMB        = 16,  // Gamepad joystick pressed button left
                GAMEPAD_BUTTON_RIGHT_THUMB       = 17;  // Gamepad joystick pressed button right
    }

    public static class GamepadAxis {
        public final static int
                GAMEPAD_AXIS_LEFT_X        = 0,     // Gamepad left stick X axis
                GAMEPAD_AXIS_LEFT_Y        = 1,     // Gamepad left stick Y axis
                GAMEPAD_AXIS_RIGHT_X       = 2,     // Gamepad right stick X axis
                GAMEPAD_AXIS_RIGHT_Y       = 3,     // Gamepad right stick Y axis
                GAMEPAD_AXIS_LEFT_TRIGGER  = 4,     // Gamepad back trigger left, pressure level: [1..-1]
                GAMEPAD_AXIS_RIGHT_TRIGGER = 5;      // Gamepad back trigger right, pressure level: [1..-1]
    }

    public Gamepad(){
        ready = new boolean[4];
        axisState = new float[4][6];
        currentButtonState = new byte[4][18];
        previousButtonState = new byte[4][18];
    }

    public int getLastButtonPressed(){
        return lastButtonPressed;
    }

    public void setLastButtonPressed(int lastButtonPressed){
        this.lastButtonPressed = lastButtonPressed;
    }

    public int getAxisCount(){
        return axisCount;
    }

    public void setAxisCount(int axisCount){
        this.axisCount = axisCount;
    }

    public boolean[] getReady(){
        return ready;
    }

    public void setReady(boolean[] ready){
        this.ready = ready;
    }

    public float[][] getAxisState(){
        return axisState;
    }

    public void setAxisState(float[][] axisState){
        this.axisState = axisState;
    }

    public byte[][] getCurrentButtonState(){
        return currentButtonState;
    }

    public void setCurrentButtonState(byte[][] currentButtonState){
        this.currentButtonState = currentButtonState;
    }

    public byte[][] getPreviousButtonState(){
        return previousButtonState;
    }

    public void setPreviousButtonState(byte[][] previousButtonState){
        this.previousButtonState = previousButtonState;
    }
}
