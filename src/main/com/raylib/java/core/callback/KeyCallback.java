package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import com.raylib.java.core.tracelog.TraceLog;
import org.lwjgl.glfw.GLFWKeyCallback;

import static com.raylib.java.Config.*;
import static com.raylib.java.Config.SUPPORT_EVENTS_AUTOMATION;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static org.lwjgl.glfw.GLFW.*;

public class KeyCallback extends GLFWKeyCallback {

    private final Raylib context;
    public KeyCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods){
        // WARNING: GLFW could return GLFW_REPEAT, we need to consider it as 1
        // to work properly with our implementation (IsKeyDown/IsKeyUp checks)
        context.core.input.keyboard.currentKeyState[key] = action != GLFW_RELEASE;

        // Check if there is space available in the key queue
        if ((context.core.input.keyboard.keyPressedQueueCount < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_PRESS)){
            // Add character to the queue
            context.core.input.keyboard.keyPressedQueue[context.core.input.keyboard.keyPressedQueueCount] = key;
            context.core.input.keyboard.keyPressedQueueCount++;
        }

        if ((context.core.input.keyboard.keyPressedQueueCount < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_REPEAT)){
            context.core.input.keyboard.keyRepeatInFrame[key] = true;
        }

        // Check the exit key to set close window
        if ((key == context.core.input.keyboard.exitKey) && (action == GLFW_PRESS)){
            glfwSetWindowShouldClose(context.core.window.handle, true);
        }

        if(SUPPORT_SCREEN_CAPTURE){
            if ((key == GLFW_KEY_F12) && (action == GLFW_PRESS)){
                if(SUPPORT_GIF_RECORDING){
                    //TODO: GIF RECORDING
                }
                else {
                    context.core.TakeScreenshot("screenshot" + context.core.screenshotCounter + ".png");
                    context.core.screenshotCounter++;
                }
            }
        }

        if(SUPPORT_EVENTS_AUTOMATION){
            if ((key == GLFW_KEY_F11) && (action == GLFW_PRESS)){
                context.core.eventsRecording = !context.core.eventsRecording;

                // On finish recording, we export events into a file
                if (!context.core.eventsRecording){
                    context.core.ExportAutomationEvents("eventsrec.rep");
                }
            }
            else if ((key == GLFW_KEY_F9) && (action == GLFW_PRESS)){
                context.core.LoadAutomationEvents("eventsrec.rep");
                context.core.eventsPlaying = true;

                context.logger.TRACELOG(LOG_WARNING, "eventsPlaying enabled!");
            }

        }

    }
}