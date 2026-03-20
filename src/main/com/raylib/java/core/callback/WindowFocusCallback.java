package com.raylib.java.core.callback;


import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWWindowFocusCallback;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_UNFOCUSED;

public class WindowFocusCallback extends GLFWWindowFocusCallback {

    private final Raylib context;
    public WindowFocusCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean focused){
        // System.out.println("Focus Callback Triggered");
        if (focused){
            context.core.window.flags &= ~FLAG_WINDOW_UNFOCUSED;   // The window was focused
        }
        else{
            context.core.window.flags |= FLAG_WINDOW_UNFOCUSED;            // The window lost focus
        }
    }
}