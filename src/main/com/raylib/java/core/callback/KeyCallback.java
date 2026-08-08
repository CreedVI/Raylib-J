package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWKeyCallback;

import static com.raylib.java.Config.*;
import static com.raylib.java.Config.SUPPORT_AUTOMATION_EVENTS;
import static com.raylib.java.core.input.Keyboard.KEY_CAPS_LOCK;
import static com.raylib.java.core.input.Keyboard.KEY_NUM_LOCK;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static org.lwjgl.glfw.GLFW.*;

public class KeyCallback extends GLFWKeyCallback {

    private final Raylib context;
    public KeyCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods){

        if (key < 0) {
            return;    // Security check, macOS fn key generates -1
        }

        // WARNING: GLFW could return GLFW_REPEAT, it needs to be considered as 1
        // to work properly with our implementation (IsKeyDown/IsKeyUp checks)
        if (action == GLFW_RELEASE) {
            context.core.input.keyboard.currentKeyState[key] = false;
        }
        else if (action == GLFW_PRESS) {
            context.core.input.keyboard.currentKeyState[key] = true;
        }
        else if (action == GLFW_REPEAT) {
            context.core.input.keyboard.currentKeyState[key] = true;
        }

        // WARNING: Check if CAPS/NUM key modifiers are enabled and force down state for those keys
        if (((key == KEY_CAPS_LOCK) && ((mods & GLFW_MOD_CAPS_LOCK) > 0)) || ((key == KEY_NUM_LOCK) && ((mods & GLFW_MOD_NUM_LOCK) > 0))) {
            context.core.input.keyboard.currentKeyState[key] = true;
        }

        // Check if there is space available in the key queue
        if ((context.core.input.keyboard.keyPressedQueueCount < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_PRESS)) {
            // Add character to the queue
            context.core.input.keyboard.keyPressedQueue[context.core.input.keyboard.keyPressedQueueCount] = key;
            context.core.input.keyboard.keyPressedQueueCount++;
        }

        // Check the exit key to set close window
        if ((key == context.core.input.keyboard.exitKey) && (action == GLFW_PRESS)) {
            glfwSetWindowShouldClose(context.core.GetPlatform().GetWindowHandle(), true);
        }
    }
}