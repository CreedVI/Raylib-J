package com.raylib.java.core;

import com.raylib.java.raymath.Vector2;
import org.lwjgl.glfw.*;

import static com.raylib.java.Config.*;
import static com.raylib.java.Config.ConfigFlag.*;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Callbacks{

    private static rCore context;

    Callbacks(rCore context) {
        this.context = context;
    }

    static class ErrorCallback extends GLFWErrorCallback{
        @Override
        public void invoke(int error, long description){
            Tracelog(LOG_WARNING, "GLFW: Error: " + error + " Description: " + description);
        }
    }

    static class WindowSizeCallback extends GLFWWindowSizeCallback{
        @Override
        public void invoke(long window, int width, int height){
            Tracelog(LOG_DEBUG, "Window Size Callback Triggered");
            context.SetupViewport(width, height);    // Reset viewport and projection matrix for new size
            context.getWindow().currentFbo.setWidth(width);
            context.getWindow().currentFbo.setHeight(height);
            context.getWindow().setResizedLastFrame(true);

            if(context.IsWindowFullscreen()){
                return;
            }

            // Set current screen size
            context.getWindow().screen.setWidth(width);
            context.getWindow().screen.setHeight(height);
            // NOTE: Postprocessing texture is not scaled to new size
        }
    }

    static class WindowIconifyCallback extends GLFWWindowIconifyCallback{
        @Override
        public void invoke(long window, boolean iconified){
            Tracelog(LOG_DEBUG, "Iconify Callback Triggered");
            if (iconified){
                context.getWindow().flags |= FLAG_WINDOW_MINIMIZED;  // The window was iconified
            }
            else{
                context.getWindow().flags &= ~FLAG_WINDOW_MINIMIZED;           // The window was restored
            }
        }
    }

    static class WindowMaximizeCallback extends GLFWWindowMaximizeCallback{
        @Override
        public void invoke(long window, boolean maximized){
            if (maximized){
                context.getWindow().flags |= FLAG_WINDOW_MAXIMIZED;  // The window was maximized
            }
            else{
                context.getWindow().flags &= ~FLAG_WINDOW_MAXIMIZED;           // The window was restored
            }
        }
    }

    static class WindowFocusCallback extends GLFWWindowFocusCallback{
        @Override
        public void invoke(long window, boolean focused){
            Tracelog(LOG_DEBUG, "Focus Callback Triggered");
            if (focused){
                context.getWindow().flags &= ~FLAG_WINDOW_UNFOCUSED;   // The window was focused
            }
            else{
                context.getWindow().flags |= FLAG_WINDOW_UNFOCUSED;            // The window lost focus
            }
        }
    }

    static class KeyCallback extends GLFWKeyCallback{
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

                    Tracelog(LOG_WARNING, "eventsPlaying enabled!");
                }

            }

        }
    }

    static class CharCallback extends GLFWCharCallback{
        @Override
        public void invoke(long window, int codepoint){
            Tracelog(LOG_DEBUG, "Char Callback: KEY:"+codepoint+"("+ Character.highSurrogate(codepoint) +")");

            // NOTE: Registers any key down considering OS keyboard layout but
            // do not detects action events, those should be managed by user...
            // Ref: https://github.com/glfw/glfw/issues/668#issuecomment-166794907
            // Ref: https://www.glfw.org/docs/latest/input_guide.html#input_char

            // Check if there is space available in the queue
            if (context.getInput().keyboard.getCharPressedQueueCount() < MAX_KEY_PRESSED_QUEUE){
                // Add character to the queue
                context.getInput().keyboard.getCharPressedQueue()[context.getInput().keyboard.getCharPressedQueueCount()] =
                        codepoint;
                context.getInput().keyboard.setCharPressedQueueCount(context.getInput().keyboard.getCharPressedQueueCount() + 1);
            }
        }
    }

    static class MouseButtonCallback extends GLFWMouseButtonCallback{
        @Override
        public void invoke(long window, int button, int action, int mods){
            Tracelog(LOG_DEBUG, "Mouse Button Callback Triggered");
            // WARNING: GLFW could only return GLFW_PRESS (1) or GLFW_RELEASE (0) for now,
            // but future releases may add more actions (i.e. GLFW_REPEAT)
            context.getInput().mouse.getCurrentButtonState()[button] = action;
        }
    }

    static class MouseCursorPosCallback extends GLFWCursorPosCallback{
        @Override
        public void invoke(long window, double xpos, double ypos){
            Tracelog(LOG_DEBUG, "Cursor Position Callback Triggered");
            context.getInput().mouse.previousPosition.x = context.getInput().mouse.currentPosition.x;
            context.getInput().mouse.previousPosition.y = context.getInput().mouse.currentPosition.y;
            context.getInput().mouse.currentPosition.x = (float) xpos;
            context.getInput().mouse.currentPosition.y = (float) ypos;
        }
    }

    static class MouseScrollCallback extends GLFWScrollCallback{
        @Override
        public void invoke(long window, double xoffset, double yoffset){
            Tracelog(LOG_DEBUG, "Scroll Callback Triggered");
            context.getInput().mouse.setCurrentWheelMove(new Vector2((float) xoffset, (float) yoffset));
        }
    }

    static class CursorEnterCallback extends GLFWCursorEnterCallback{
        @Override
        public void invoke(long window, boolean entered){
            Tracelog(LOG_DEBUG, "Cursor Enter Callback Triggered");
            context.getInput().mouse.setCursorOnScreen(entered);
        }
    }

    static class WindowDropCallback extends GLFWDropCallback{
        @Override
        public void invoke(long window, int count, long names){
            Tracelog(LOG_DEBUG, "Drop Callback Triggered");
            context.ClearDroppedFiles();
            String[] paths = new String[(int) names];

            context.getWindow().setDropFilesPath(new String[count]);

            for (int j = 0; j < count; j++){
                context.getWindow().getDropFilesPath()[count] = String.valueOf(MAX_FILEPATH_LENGTH);
                paths[j] = String.valueOf(context.getWindow().getDropFilesPath()[j]);
            }

            context.getWindow().setDropFilesCount(count);
        }
    }
}