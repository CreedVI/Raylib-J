package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWDropCallback;

import static com.raylib.java.Config.MAX_FILEPATH_LENGTH;
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
        context.ClearDroppedFiles();
        String[] paths = new String[(int) names];

        context.getWindow().setDropFilePaths(new String[count]);

        for (int j = 0; j < count; j++){
            context.getWindow().getDropFilePaths()[count] = String.valueOf(MAX_FILEPATH_LENGTH);
            paths[j] = String.valueOf(context.getWindow().getDropFilePaths()[j]);
        }

        context.getWindow().setDropFilesCount(count);
        context.getWindow().setDropFilePaths(paths);
    }
}