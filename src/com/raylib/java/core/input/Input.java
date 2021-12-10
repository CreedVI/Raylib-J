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



}
