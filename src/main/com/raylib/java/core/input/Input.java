package com.raylib.java.core.input;

import static com.raylib.java.Config.MAX_GAMEPADS;

public class Input{

    /**
     * Track keyboard state
     */
    public Keyboard keyboard;
    /**
     * Track mouse state
     */
    public Mouse mouse;
    /**
     * Track state for multiple gamepads
     */
    public Gamepad[] gamepad;
    /**
     * Track state for touch inputs
     */
    public Touch touch;

    public Input(){
        keyboard = new Keyboard();
        mouse = new Mouse();
        gamepad = new Gamepad[MAX_GAMEPADS];
        for (int i = 0; i < gamepad.length; i++) {
            gamepad[i] = new Gamepad();
        }
        touch = new Touch();
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public void setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public Gamepad getGamepad(int index) {
        return gamepad[index];
    }

    public void setGamepad(int index, Gamepad gamepad) {
        this.gamepad[index] = gamepad;
    }

    public Touch getTouch() {
        return touch;
    }

    public void setTouch(Touch touch) {
        this.touch = touch;
    }
}
