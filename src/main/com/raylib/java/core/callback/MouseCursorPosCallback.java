package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import com.raylib.java.structs.Vector2;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class MouseCursorPosCallback extends GLFWCursorPosCallback {

    private final rCore context;
    public MouseCursorPosCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, double xpos, double ypos){
        TRACELOG(LOG_DEBUG, "Cursor Position Callback Triggered");
        context.input.mouse.currentPosition = new Vector2(xpos, ypos);
    }
}