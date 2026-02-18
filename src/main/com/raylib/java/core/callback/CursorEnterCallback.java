package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWCursorEnterCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class CursorEnterCallback extends GLFWCursorEnterCallback {

    private final rCore context;
    public CursorEnterCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean entered){
        TRACELOG(LOG_DEBUG, "Cursor Enter Callback Triggered");
        context.input.mouse.setCursorOnScreen(entered);
    }
}
