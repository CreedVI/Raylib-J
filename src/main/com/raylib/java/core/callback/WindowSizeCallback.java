package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class WindowSizeCallback extends GLFWWindowSizeCallback {

    private final Raylib context;
    public WindowSizeCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int width, int height){
        // System.out.println("Window Size Callback Triggered");
        context.core.SetupViewport(width, height);    // Reset viewport and projection matrix for new size
        context.core.window.getCurrentFbo().setWidth(width);
        context.core.window.getCurrentFbo().setHeight(height);
        context.core.window.setResizedLastFrame(true);

        if(context.core.IsWindowFullscreen()){
            return;
        }

        // Set current screen size
        context.core.window.getScreen().setWidth(width);
        context.core.window.getScreen().setHeight(height);
        // NOTE: Postprocessing texture is not scaled to new size
    }
}