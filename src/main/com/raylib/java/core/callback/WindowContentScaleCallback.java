package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWWindowContentScaleCallback;

import static com.raylib.java.Config._GLFW_WAYLAND;
import static com.raylib.java.Config.__APPLE__;
import static com.raylib.java.raymath.Raymath.MatrixScale;

public class WindowContentScaleCallback extends GLFWWindowContentScaleCallback {

    private final Raylib context;

    public WindowContentScaleCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, float xscale, float yscale) {
        //TRACELOG(LOG_INFO, "GLFW3: Window content scale changed, scale: [%.2f,%.2f]", scalex, scaley);

        context.core.window.getRender().width = (int)((float) context.core.window.getScreen().width*xscale);
        context.core.window.getRender().height = (int)((float) context.core.window.getScreen().height*yscale);
        context.core.window.setCurrentFbo(context.core.window.getRender());

        // NOTE: On APPLE platforms system should manage window/input scaling and also framebuffer scaling
        // Framebuffer scaling is activated with: glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_TRUE);
        context.core.window.setScreenScale(MatrixScale(xscale, yscale, 1.0f));

        if (!__APPLE__ && !_GLFW_WAYLAND) {
            // On macOS and Linux-Wayland, mouse coords are already in logical space
            context.core.SetMouseScale(1.0f / xscale, 1.0f / yscale);
        }
    }
}
