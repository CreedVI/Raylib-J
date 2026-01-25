package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class WindowSizeCallback extends GLFWWindowSizeCallback {

    private final rCore context;
    public WindowSizeCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int width, int height){
        TRACELOG(LOG_DEBUG, "Window Size Callback Triggered");
        context.SetupViewport(width, height);    // Reset viewport and projection matrix for new size
        context.getWindow().getCurrentFbo().setWidth(width);
        context.getWindow().getCurrentFbo().setHeight(height);
        context.getWindow().setResizedLastFrame(true);

        if(context.IsWindowFullscreen()){
            return;
        }

        // Set current screen size
        context.getWindow().getScreen().setWidth(width);
        context.getWindow().getScreen().setHeight(height);
        // NOTE: Postprocessing texture is not scaled to new size
    }
}