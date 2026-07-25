package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWCharCallback;

import static com.raylib.java.Config.MAX_CHAR_PRESSED_QUEUE;

public class CharCallback extends GLFWCharCallback {

    private final Raylib context;
    public CharCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int codepoint){
        // System.out.println("Char Callback: KEY: " + codepoint + "(" + (char) (codepoint) +")");

        // NOTE: Registers any key down considering OS keyboard layout but
        // do not detects action events, those should be managed by user...
        // Ref: https://github.com/glfw/glfw/issues/668#issuecomment-166794907
        // Ref: https://www.glfw.org/docs/latest/input_guide.html#input_char

        // Check if there is space available in the queue
        if (context.core.input.keyboard.getCharPressedQueueCount() < MAX_CHAR_PRESSED_QUEUE){
            // Add character to the queue
            context.core.input.keyboard.getCharPressedQueue()[context.core.input.keyboard.getCharPressedQueueCount()] = codepoint;
            context.core.input.keyboard.setCharPressedQueueCount(context.core.input.keyboard.getCharPressedQueueCount() + 1);
        }
    }
}