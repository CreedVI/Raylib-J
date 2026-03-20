package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_MAXIMIZED;

public class WindowMaximizeCallback extends GLFWWindowMaximizeCallback {

    private final Raylib context;
    public WindowMaximizeCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean maximized){
        if (maximized){
            context.core.window.flags |= FLAG_WINDOW_MAXIMIZED;  // The window was maximized
        }
        else{
            context.core.window.flags &= ~FLAG_WINDOW_MAXIMIZED;           // The window was restored
        }
    }
}