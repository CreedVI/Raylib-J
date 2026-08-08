package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWWindowPosCallback;

public class WindowPositionCallback extends GLFWWindowPosCallback {

    private final Raylib context;

    public WindowPositionCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int xpos, int ypos) {
        // Set current window position
        context.core.window.getPosition().x = xpos;
        context.core.window.getPosition().y = ypos;
    }

}
