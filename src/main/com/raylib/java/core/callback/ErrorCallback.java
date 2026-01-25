package com.raylib.java.core.callback;

import org.lwjgl.glfw.GLFWErrorCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;

public class ErrorCallback extends GLFWErrorCallback {
    @Override
    public void invoke(int error, long description){
        TRACELOG(LOG_WARNING, "GLFW: Error: " + error + " Description: " + description);
    }
}
