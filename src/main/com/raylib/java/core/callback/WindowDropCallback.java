package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWDropCallback;

import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class WindowDropCallback extends GLFWDropCallback {

    private final rCore context;
    public WindowDropCallback(rCore context) {
        this.context = context;

    }

    @Override
    public void invoke(long window, int count, long names){
        TRACELOG(LOG_DEBUG, "Drop Callback Triggered");
        context.UnloadDroppedFiles();
        String[] paths = new String[(int) count];

        for (int j = 0; j < count; j++){
            paths[j] = getName(names, j);
        }

        context.window.setDropFilePaths(paths);
        context.window.setDropFilesCount(paths.length);
    }
}