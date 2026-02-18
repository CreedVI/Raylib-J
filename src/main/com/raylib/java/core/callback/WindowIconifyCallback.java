package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;

import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_MINIMIZED;
import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class WindowIconifyCallback extends GLFWWindowIconifyCallback {

    private final rCore context;
    public WindowIconifyCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, boolean iconified){
        TRACELOG(LOG_DEBUG, "Iconify Callback Triggered");
        if (iconified){
            context.window.flags |= FLAG_WINDOW_MINIMIZED;  // The window was iconified
        }
        else{
            context.window.flags &= ~FLAG_WINDOW_MINIMIZED;           // The window was restored
        }
    }
}