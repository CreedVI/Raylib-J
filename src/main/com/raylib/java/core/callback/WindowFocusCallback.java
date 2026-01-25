package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWWindowFocusCallback;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_UNFOCUSED;
import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class WindowFocusCallback extends GLFWWindowFocusCallback {

    private final rCore context;
    public WindowFocusCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean focused){
        TRACELOG(LOG_DEBUG, "Focus Callback Triggered");
        if (focused){
            context.getWindow().flags &= ~FLAG_WINDOW_UNFOCUSED;   // The window was focused
        }
        else{
            context.getWindow().flags |= FLAG_WINDOW_UNFOCUSED;            // The window lost focus
        }
    }
}