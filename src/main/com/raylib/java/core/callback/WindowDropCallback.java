package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWDropCallback;

import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_DEBUG;

public class WindowDropCallback extends GLFWDropCallback {

    private final Raylib context;
    public WindowDropCallback(Raylib context) {
        this.context = context;

    }

    @Override
    public void invoke(long window, int count, long names){
        context.logger.TRACELOG(LOG_DEBUG, "Drop Callback Triggered");
        context.core.UnloadDroppedFiles();
        String[] paths = new String[(int) count];

        for (int j = 0; j < count; j++){
            paths[j] = getName(names, j);
        }

        context.core.window.setDropFilePaths(paths);
        context.core.window.setDropFilesCount(paths.length);
    }
}