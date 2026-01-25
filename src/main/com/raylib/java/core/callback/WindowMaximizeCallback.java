package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_MAXIMIZED;

public class WindowMaximizeCallback extends GLFWWindowMaximizeCallback {

    private final rCore context;
    public WindowMaximizeCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean maximized){
        if (maximized){
            context.getWindow().flags |= FLAG_WINDOW_MAXIMIZED;  // The window was maximized
        }
        else{
            context.getWindow().flags &= ~FLAG_WINDOW_MAXIMIZED;           // The window was restored
        }
    }
}