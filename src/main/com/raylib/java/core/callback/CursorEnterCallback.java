package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWCursorEnterCallback;

import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_DEBUG;

public class CursorEnterCallback extends GLFWCursorEnterCallback {

    private final Raylib context;
    public CursorEnterCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean entered){
        context.tracelog.TRACELOG(LOG_DEBUG, "Cursor Enter Callback Triggered");
        context.core.input.mouse.setCursorOnScreen(entered);
    }
}
