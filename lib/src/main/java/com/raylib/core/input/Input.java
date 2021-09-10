package com.raylib.core.input;

public class Input{

    public Keyboard keyboard;
    public Mouse mouse;
    public Gamepad gamepad;

    public Input(){
        keyboard = new Keyboard();
        mouse = new Mouse();
        gamepad = new Gamepad();
    }



}
