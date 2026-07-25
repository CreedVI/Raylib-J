package com.raylib.java.core.input;

public class Input{

    public Keyboard keyboard;
    public Mouse mouse;
    public Gamepad gamepad;
    public Touch touch;

    public Input(){
        keyboard = new Keyboard();
        mouse = new Mouse();
        gamepad = new Gamepad();
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

    public Gamepad getGamepad() {
        return gamepad;
    }

    public void setGamepad(Gamepad gamepad) {
        this.gamepad = gamepad;
    }

    public Touch getTouch() {
        return touch;
    }

    public void setTouch(Touch touch) {
        this.touch = touch;
    }
}
