package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Vector2;
import org.lwjgl.glfw.GLFWScrollCallback;

import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_DEBUG;

public class MouseScrollCallback extends GLFWScrollCallback {

    private final Raylib context;
    public MouseScrollCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, double xoffset, double yoffset){
        context.traceLog.TRACELOG(LOG_DEBUG, "Scroll Callback Triggered");
        context.core.input.mouse.setCurrentWheelMove(new Vector2((float) xoffset, (float) yoffset));
    }
}