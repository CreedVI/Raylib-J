package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;
import org.lwjgl.glfw.GLFWKeyCallback;

import static com.raylib.java.Config.*;
import static com.raylib.java.Config.SUPPORT_EVENTS_AUTOMATION;
import static com.raylib.java.utils.Tracelog.TRACELOG;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.glfw.GLFW.*;

public class KeyCallback extends GLFWKeyCallback {

    private final rCore context;
    public KeyCallback(rCore context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods){
        // WARNING: GLFW could return GLFW_REPEAT, we need to consider it as 1
        // to work properly with our implementation (IsKeyDown/IsKeyUp checks)
        context.getInput().keyboard.currentKeyState[key] = action != GLFW_RELEASE;

        // Check if there is space available in the key queue
        if ((context.getInput().keyboard.keyPressedQueueCount < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_PRESS)){
            // Add character to the queue
            context.getInput().keyboard.keyPressedQueue[context.getInput().keyboard.keyPressedQueueCount] = key;
            context.getInput().keyboard.keyPressedQueueCount++;
        }

        if ((context.getInput().keyboard.keyPressedQueueCount < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_REPEAT)){
            context.getInput().keyboard.keyRepeatInFrame[key] = true;
        }

        // Check the exit key to set close window
        if ((key == context.getInput().keyboard.exitKey) && (action == GLFW_PRESS)){
            glfwSetWindowShouldClose(context.getWindow().handle, true);
        }

        if(SUPPORT_SCREEN_CAPTURE){
            if ((key == GLFW_KEY_F12) && (action == GLFW_PRESS)){
                if(SUPPORT_GIF_RECORDING){
                    //TODO: GIF RECORDING
                }
                else {
                    context.TakeScreenshot("screenshot" + context.screenshotCounter + ".png");
                    context.screenshotCounter++;
                }
            }
        }

        if(SUPPORT_EVENTS_AUTOMATION){
            if ((key == GLFW_KEY_F11) && (action == GLFW_PRESS)){
                context.eventsRecording = !context.eventsRecording;

                // On finish recording, we export events into a file
                if (!context.eventsRecording){
                    context.ExportAutomationEvents("eventsrec.rep");
                }
            }
            else if ((key == GLFW_KEY_F9) && (action == GLFW_PRESS)){
                context.LoadAutomationEvents("eventsrec.rep");
                context.eventsPlaying = true;

                TRACELOG(LOG_WARNING, "eventsPlaying enabled!");
            }

        }

    }
}