package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWCharCallback;

import static com.raylib.java.Config.MAX_CHAR_PRESSED_QUEUE;
import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_DEBUG;

public class CharCallback extends GLFWCharCallback {

    private final rCore context;
    public CharCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int codepoint){
        TRACELOG(LOG_DEBUG, "Char Callback: KEY:"+codepoint+"("+ Character.highSurrogate(codepoint) +")");

        // NOTE: Registers any key down considering OS keyboard layout but
        // do not detects action events, those should be managed by user...
        // Ref: https://github.com/glfw/glfw/issues/668#issuecomment-166794907
        // Ref: https://www.glfw.org/docs/latest/input_guide.html#input_char

        // Check if there is space available in the queue
        if (context.getInput().keyboard.getCharPressedQueueCount() < MAX_CHAR_PRESSED_QUEUE){
            // Add character to the queue
            context.getInput().keyboard.getCharPressedQueue()[context.getInput().keyboard.getCharPressedQueueCount()] =
                    codepoint;
            context.getInput().keyboard.setCharPressedQueueCount(context.getInput().keyboard.getCharPressedQueueCount() + 1);
        }
    }
}