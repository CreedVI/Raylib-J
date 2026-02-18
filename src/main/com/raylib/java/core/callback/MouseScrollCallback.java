package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import com.raylib.java.structs.Vector2;
import org.lwjgl.glfw.GLFWScrollCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class MouseScrollCallback extends GLFWScrollCallback {

    private final rCore context;
    public MouseScrollCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, double xoffset, double yoffset){
        TRACELOG(LOG_DEBUG, "Scroll Callback Triggered");
        context.input.mouse.setCurrentWheelMove(new Vector2((float) xoffset, (float) yoffset));
    }
}