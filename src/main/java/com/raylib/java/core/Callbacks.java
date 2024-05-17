package com.raylib.java.core;

import com.raylib.java.Raylib;
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

    final ErrorCallback errorCallback;
    final WindowMaximizeCallback windowMaximizeCallback;
    final WindowSizeCallback windowSizeCallback;
    final WindowIconifyCallback windowIconifyCallback;
    final WindowFocusCallback windowFocusCallback;
    final WindowDropCallback windowDropCallback;
    final KeyCallback keyCallback;
    final CharCallback charCallback;
    final MouseButtonCallback mouseButtonCallback;
    final MouseCursorPosCallback mouseCursorPosCallback;
    final MouseScrollCallback mouseScrollCallback;
    final CursorEnterCallback cursorEnterCallback;


    Callbacks(rCore context) {
        this.context = context;

        errorCallback = new ErrorCallback();
        windowMaximizeCallback = new WindowMaximizeCallback(context);
        windowSizeCallback = new WindowSizeCallback(context);
        windowIconifyCallback = new WindowIconifyCallback(context);
        windowFocusCallback = new WindowFocusCallback(context);
        windowDropCallback = new WindowDropCallback(context);
        keyCallback = new KeyCallback(context);
        charCallback = new CharCallback(context);
        mouseButtonCallback = new MouseButtonCallback(context);
        mouseCursorPosCallback = new MouseCursorPosCallback(context);
        mouseScrollCallback = new MouseScrollCallback(context);
        cursorEnterCallback = new CursorEnterCallback(context);
    }

    class ErrorCallback extends GLFWErrorCallback{
        @Override
        public void invoke(int error, long description){
            Tracelog(LOG_WARNING, "GLFW: Error: " + error + " Description: " + description);
        }
    }

    class WindowSizeCallback extends GLFWWindowSizeCallback{

        private final rCore context;
        public WindowSizeCallback(rCore context) {
            this.context = context;
        }

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

    class WindowIconifyCallback extends GLFWWindowIconifyCallback{

        private final rCore context;
        public WindowIconifyCallback(rCore context) {
            this.context = context;
        }

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

    class WindowMaximizeCallback extends GLFWWindowMaximizeCallback{

        private final rCore context;
        public WindowMaximizeCallback(rCore context) {
            this.context = context;
        }

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

    class WindowFocusCallback extends GLFWWindowFocusCallback{

        private final rCore context;
        public WindowFocusCallback(rCore context) {
            this.context = context;
        }

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

    class KeyCallback extends GLFWKeyCallback{

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

    class CharCallback extends GLFWCharCallback{

        private final rCore context;
        public CharCallback(rCore context) {
            this.context = context;
        }

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

    class MouseButtonCallback extends GLFWMouseButtonCallback{

        private final rCore context;
        public MouseButtonCallback(rCore context) {
            this.context = context;
        }

        @Override
        public void invoke(long window, int button, int action, int mods){
            Tracelog(LOG_DEBUG, "Mouse Button Callback Triggered");
            // WARNING: GLFW could only return GLFW_PRESS (1) or GLFW_RELEASE (0) for now,
            // but future releases may add more actions (i.e. GLFW_REPEAT)
            context.getInput().mouse.getCurrentButtonState()[button] = action;
        }
    }

    class MouseCursorPosCallback extends GLFWCursorPosCallback{

        private final rCore context;
        public MouseCursorPosCallback(rCore context) {
            this.context = context;
        }

        @Override
        public void invoke(long window, double xpos, double ypos){
            Tracelog(LOG_DEBUG, "Cursor Position Callback Triggered");
            context.getInput().mouse.previousPosition.x = context.getInput().mouse.currentPosition.x;
            context.getInput().mouse.previousPosition.y = context.getInput().mouse.currentPosition.y;
            context.getInput().mouse.currentPosition.x = (float) xpos;
            context.getInput().mouse.currentPosition.y = (float) ypos;
        }
    }

    class MouseScrollCallback extends GLFWScrollCallback{

        private final rCore context;
        public MouseScrollCallback(rCore context) {
            this.context = context;
        }

        @Override
        public void invoke(long window, double xoffset, double yoffset){
            Tracelog(LOG_DEBUG, "Scroll Callback Triggered");
            context.getInput().mouse.setCurrentWheelMove(new Vector2((float) xoffset, (float) yoffset));
        }
    }

    class CursorEnterCallback extends GLFWCursorEnterCallback{

        private final rCore context;
        public CursorEnterCallback(rCore context) {
            this.context = context;
        }

        @Override
        public void invoke(long window, boolean entered){
            Tracelog(LOG_DEBUG, "Cursor Enter Callback Triggered");
            context.getInput().mouse.setCursorOnScreen(entered);
        }
    }

    class WindowDropCallback extends GLFWDropCallback{

        private final rCore context;
        public WindowDropCallback(rCore context) {
            this.context = context;
        }

        @Override
        public void invoke(long window, int count, long names){
            Tracelog(LOG_DEBUG, "Drop Callback Triggered");
            context.ClearDroppedFiles();
            String[] paths = new String[(int) names];

            context.getWindow().setDropFilePaths(new String[count]);

            for (int j = 0; j < count; j++){
                context.getWindow().getDropFilePaths()[count] = String.valueOf(MAX_FILEPATH_LENGTH);
                paths[j] = String.valueOf(context.getWindow().getDropFilePaths()[j]);
            }

            context.getWindow().setDropFilesCount(count);
        }
    }
}