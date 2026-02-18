package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class MouseButtonCallback extends GLFWMouseButtonCallback {

    private final rCore context;
    public MouseButtonCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int button, int action, int mods){
        TRACELOG(LOG_DEBUG, "Mouse Button Callback Triggered");
        // WARNING: GLFW could only return GLFW_PRESS (1) or GLFW_RELEASE (0) for now,
        // but future releases may add more actions (i.e. GLFW_REPEAT)
        context.input.mouse.getCurrentButtonState()[button] = action;
    }
}