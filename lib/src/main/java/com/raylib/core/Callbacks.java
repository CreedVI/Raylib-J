package com.raylib.core;

import org.lwjgl.glfw.*;

import static com.raylib.Config.ConfigFlag.*;
import static com.raylib.Config.MAX_FILEPATH_LENGTH;
import static com.raylib.Config.MAX_KEY_PRESSED_QUEUE;
import static com.raylib.core.Core.getWindow;
import static com.raylib.utils.Tracelog.Tracelog;
import static com.raylib.utils.Tracelog.TracelogType.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Callbacks{

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
            Core.SetupViewport(width, height);    // Reset viewport and projection matrix for new size
            Core.getWindow().currentFbo.setWidth(width);
            Core.getWindow().currentFbo.setHeight(height);
            Core.getWindow().setResizedLastFrame(true);

            if(Core.IsWindowFullscreen()){
                return;
            }

            // Set current screen size
            Core.getWindow().screen.setWidth(width);
            Core.getWindow().screen.setHeight(height);
            // NOTE: Postprocessing texture is not scaled to new size
        }
    }

    static class WindowIconifyCallback extends GLFWWindowIconifyCallback{
        @Override
        public void invoke(long window, boolean iconified){
            Tracelog(LOG_DEBUG, "Iconify Callback Triggered");
            if (iconified){
                Core.getWindow().flags |= FLAG_WINDOW_MINIMIZED.getFlag();  // The window was iconified
            }
            else{
                Core.getWindow().flags &= ~FLAG_WINDOW_MINIMIZED.getFlag();           // The window was restored
            }
        }
    }

    static class WindowMaximizeCallback extends GLFWWindowMaximizeCallback{
        @Override
        public void invoke(long window, boolean maximized){
            if (maximized){
                Core.getWindow().flags |= FLAG_WINDOW_MAXIMIZED.getFlag();  // The window was maximized
            }
            else{
                Core.getWindow().flags &= ~FLAG_WINDOW_MAXIMIZED.getFlag();           // The window was restored
            }
        }
    }

    static class WindowFocusCallback extends GLFWWindowFocusCallback{
        @Override
        public void invoke(long window, boolean focused){
            Tracelog(LOG_DEBUG, "Focus Callback Triggered");
            if (focused){
                Core.getWindow().flags &= ~FLAG_WINDOW_UNFOCUSED.getFlag();   // The window was focused
            }
            else{
                Core.getWindow().flags |= FLAG_WINDOW_UNFOCUSED.getFlag();            // The window lost focus
            }
        }
    }

    static class KeyCallback extends GLFWKeyCallback{
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods){
            Tracelog(LOG_DEBUG, "Key Callback: KEY: " + key + "(" + Character.highSurrogate(key) + ") - SCANCODE:" +
                    scancode + " (STATE: " + action + ")");

            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ){
                getWindow().setReady(false);
                //glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            else {
                // WARNING: GLFW could return GLFW_REPEAT, we need to consider it as 1
                // to work properly with our implementation (IsKeyDown/IsKeyUp checks)
                if (action == GLFW_RELEASE){
                    Core.getInput().keyboard.getCurrentKeyState()[key] = false;
                }
                else{
                    Core.getInput().keyboard.getCurrentKeyState()[key] = true;
                }

                // Check if there is space available in the key queue
                if ((Core.getInput().keyboard.getCharPressedQueueCount() < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_PRESS)) {
                    // Add character to the queue
                    Core.getInput().keyboard.getKeyPressedQueue()[Core.getInput().keyboard.getKeyPressedQueueCount()] = key;
                    Core.getInput().keyboard.setKeyPressedQueueCount(Core.getInput().keyboard.getKeyPressedQueueCount() + 1);
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
            if (Core.getInput().keyboard.getCharPressedQueueCount() < MAX_KEY_PRESSED_QUEUE){
                // Add character to the queue
                Core.getInput().keyboard.getCharPressedQueue()[Core.getInput().keyboard.getCharPressedQueueCount()] =
                        codepoint;
                Core.getInput().keyboard.setCharPressedQueueCount(Core.getInput().keyboard.getKeyPressedQueueCount() + 1);
            }
        }
    }

    static class MouseButtonCallback extends GLFWMouseButtonCallback{
        @Override
        public void invoke(long window, int button, int action, int mods){
            Tracelog(LOG_DEBUG, "Mouse Button Callback Triggered");
            // WARNING: GLFW could only return GLFW_PRESS (1) or GLFW_RELEASE (0) for now,
            // but future releases may add more actions (i.e. GLFW_REPEAT)
            Core.getInput().mouse.getCurrentButtonState()[button] = action;
        }
    }

    static class MouseCursorPosCallback extends GLFWCursorPosCallback{
        @Override
        public void invoke(long window, double xpos, double ypos){
            Tracelog(LOG_DEBUG, "Cursor Position Callback Triggered");
            Core.getInput().mouse.getPosition().setX((float) xpos);
            Core.getInput().mouse.getPosition().setY((float) ypos);
        }
    }

    static class MouseScrollCallback extends GLFWScrollCallback{
        @Override
        public void invoke(long window, double xoffset, double yoffset){
            Tracelog(LOG_DEBUG, "Scroll Callback Triggered");
            Core.getInput().mouse.setCurrentWheelMove((float) yoffset);
        }
    }

    static class CursorEnterCallback extends GLFWCursorEnterCallback{
        @Override
        public void invoke(long window, boolean entered){
            Tracelog(LOG_DEBUG, "Cursor Enter Callback Triggered");
            Core.getInput().mouse.setCursorOnScreen(entered);
        }
    }

    static class WindowDropCallback extends GLFWDropCallback{
        @Override
        public void invoke(long window, int count, long names){
            Tracelog(LOG_DEBUG, "Drop Callback Triggered");
            Core.ClearDroppedFiles();
            String[] paths = new String[(int) names];

            Core.getWindow().setDropFilesPath(new String[count]);

            for (int j = 0; j < count; j++){
                Core.getWindow().getDropFilesPath()[count] = String.valueOf(MAX_FILEPATH_LENGTH);
                paths[j] = String.valueOf(getWindow().dropFilesPath[j]);
            }

            Core.getWindow().setDropFilesCount(count);
        }
    }
}