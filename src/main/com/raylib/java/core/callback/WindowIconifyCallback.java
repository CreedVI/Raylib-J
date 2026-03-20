package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_MINIMIZED;

public class WindowIconifyCallback extends GLFWWindowIconifyCallback {

    private final Raylib context;
    public WindowIconifyCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean iconified){
        // System.out.println("Iconify Callback Triggered");
        if (iconified){
            context.core.window.flags |= FLAG_WINDOW_MINIMIZED;  // The window was iconified
        }
        else{
            context.core.window.flags &= ~FLAG_WINDOW_MINIMIZED;           // The window was restored
        }
    }
}