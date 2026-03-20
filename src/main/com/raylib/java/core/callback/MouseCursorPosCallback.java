package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Vector2;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_DEBUG;

public class MouseCursorPosCallback extends GLFWCursorPosCallback {

    private final Raylib context;
    public MouseCursorPosCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, double xpos, double ypos){
        context.traceLog.TRACELOG(LOG_DEBUG, "Cursor Position Callback Triggered");
        context.core.input.mouse.currentPosition = new Vector2(xpos, ypos);
    }
}