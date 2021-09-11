package com.creedvi.raylib.java.rlj.core.input;

public class Gamepad{

    int lastButtonPressed;          // Register last gamepad button pressed
    int axisCount;                  // Register number of available gamepad axis
    boolean[] ready;// Flag to know if gamepad is ready
    float[][] axisState;// Gamepad axis state
    byte[][] currentState;// Current gamepad buttons state
    byte[][] previousState;// Previous gamepad buttons state

    public static class GamepadNumber {
        public static final int GAMEPAD_PLAYER1 = 0,
        GAMEPAD_PLAYER2 = 1,
        GAMEPAD_PLAYER3 = 2,
        GAMEPAD_PLAYER4 = 3;
    }

    public static class GamepadButton {
        // This is here just for error checking
        public static final int 
        GAMEPAD_BUTTON_UNKNOWN = 0,

        // This is normally a DPAD
        GAMEPAD_BUTTON_LEFT_FACE_UP = 1,
        GAMEPAD_BUTTON_LEFT_FACE_RIGHT = 2,
        GAMEPAD_BUTTON_LEFT_FACE_DOWN = 3,
        GAMEPAD_BUTTON_LEFT_FACE_LEFT = 4,

        // This normally corresponds with PlayStation and Xbox controllers
        // XBOX: [Y,X,A,B]
        // PS3: [Triangle,Square,Cross,Circle]
        // No support for 6 button controllers though..
        GAMEPAD_BUTTON_RIGHT_FACE_UP = 5,
        GAMEPAD_BUTTON_RIGHT_FACE_RIGHT = 6,
        GAMEPAD_BUTTON_RIGHT_FACE_DOWN = 7,
        GAMEPAD_BUTTON_RIGHT_FACE_LEFT = 8,

        // Triggers
        GAMEPAD_BUTTON_LEFT_TRIGGER_1 = 9,
        GAMEPAD_BUTTON_LEFT_TRIGGER_2 = 10,
        GAMEPAD_BUTTON_RIGHT_TRIGGER_1 = 11,
        GAMEPAD_BUTTON_RIGHT_TRIGGER_2 = 12,

        // These are buttons in the center of the gamepad
        GAMEPAD_BUTTON_MIDDLE_LEFT = 13,     // PS3 Select
        GAMEPAD_BUTTON_MIDDLE = 14,          // PS Button/XBOX Button
        GAMEPAD_BUTTON_MIDDLE_RIGHT = 15,    // PS3 Start

        // These are the joystick press in buttons
        GAMEPAD_BUTTON_LEFT_THUMB = 16,
        GAMEPAD_BUTTON_RIGHT_THUMB = 17;
    }

    public static class GamepadAxis {
        // Left stick
        public static final int GAMEPAD_AXIS_LEFT_X = 0,
        GAMEPAD_AXIS_LEFT_Y = 1,
        // Right stick
        GAMEPAD_AXIS_RIGHT_X = 2,
        GAMEPAD_AXIS_RIGHT_Y = 3,
        // Pressure levels for the back triggers
        GAMEPAD_AXIS_LEFT_TRIGGER = 4,      // [1..-1] (pressure-level)
        GAMEPAD_AXIS_RIGHT_TRIGGER = 5;      // [1..-1] (pressure-level)
    }

    public Gamepad(){
        ready = new boolean[4];
        axisState = new float[4][6];
        currentState = new byte[4][18];
        previousState = new byte[4][18];
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

    public byte[][] getCurrentState(){
        return currentState;
    }

    public void setCurrentState(byte[][] currentState){
        this.currentState = currentState;
    }

    public byte[][] getPreviousState(){
        return previousState;
    }

    public void setPreviousState(byte[][] previousState){
        this.previousState = previousState;
    }
}
