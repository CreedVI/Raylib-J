package com.creedvi.raylib.java.rlj.core;

import org.lwjgl.glfw.*;

import static com.creedvi.raylib.java.rlj.Config.ConfigFlag.FLAG_WINDOW_MINIMIZED;
import static com.creedvi.raylib.java.rlj.Config.ConfigFlag.FLAG_WINDOW_UNFOCUSED;
import static com.creedvi.raylib.java.rlj.Config.MAX_FILEPATH_LENGTH;
import static com.creedvi.raylib.java.rlj.Config.MAX_KEY_PRESSED_QUEUE;
import static com.creedvi.raylib.java.rlj.core.Core.getWindow;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_DEBUG;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_WARNING;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Callbacks{

    static class ErrorCallback extends GLFWErrorCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(int error, long description){
            Tracelog(LOG_WARNING, "GLFW: Error: " + error + " Description: " + description);
        }

        @Override
        public void close(){

        }
    }

    static class WindowSizeCallback extends GLFWWindowSizeCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, int width, int height){
            Tracelog(LOG_DEBUG, "Window Size Callback Triggered");
            Core.SetupViewport(width, height);    // Reset viewport and projection matrix for new size
            // Set current screen size
            Window w = getWindow();
            w.getScreen().setWidth(width);
            w.getScreen().setHeight(height);
            w.getCurrentFbo().setWidth(width);
            w.getCurrentFbo().setHeight(height);

            // NOTE: Postprocessing texture is not scaled to new size
            w.setResizedLastFrame(true);
        }

        @Override
        public void close(){

        }
    }

    static class WindowIconifyCallback extends GLFWWindowIconifyCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

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

        @Override
        public void close(){

        }
    }

    static class WindowMaximizeCallback extends GLFWWindowMaximizeCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, boolean maximized){

        }

        @Override
        public void close(){

        }
    }

    static class WindowFocusCallback extends GLFWWindowFocusCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

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

        @Override
        public void close(){

        }
    }

    static class KeyCallback extends GLFWKeyCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, int key, int scancode, int action, int mods){
            Tracelog(LOG_DEBUG, "Key Callback Triggered");
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ){
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            else {
                // WARNING: GLFW could return GLFW_REPEAT, we need to consider it as 1
                // to work properly with our implementation (IsKeyDown/IsKeyUp checks)
                if (action == GLFW_RELEASE){
                    Core.getInput().keyboard.getCurrentKeyState()[key] = 0;
                }
                else{
                    Core.getInput().keyboard.getCurrentKeyState()[key] = 1;
                }

                // Check if there is space available in the key queue
                if ((Core.getInput().keyboard.getCharPressedQueueCount() < MAX_KEY_PRESSED_QUEUE) && (action == GLFW_RELEASE)) {
                    // Add character to the queue
                    Core.getInput().keyboard.getKeyPressedQueue()[Core.getInput().keyboard.getKeyPressedQueueCount()] = key;
                    Core.getInput().keyboard.setKeyPressedQueueCount(Core.getInput().keyboard.getKeyPressedQueueCount() + 1);
                }
            }
        }

        @Override
        public void close(){

        }
    }

    static class CharCallback extends GLFWCharCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

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

        @Override
        public void close(){

        }
    }

    static class MouseButtonCallback extends GLFWMouseButtonCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, int button, int action, int mods){
            Tracelog(LOG_DEBUG, "Mouse Button Callback Triggered");
            char[] tmp = Core.getInput().mouse.getCurrentButtonState();
            tmp[button] = (char) action;
            Core.getInput().mouse.setCurrentButtonState(tmp);
        }

        @Override
        public void close(){

        }
    }

    static class MouseCursorPosCallback extends GLFWCursorPosCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, double xpos, double ypos){
            Tracelog(LOG_DEBUG, "Cursor Position Callback Triggered");
            Core.getInput().mouse.getPosition().setX((float) xpos);
            Core.getInput().mouse.getPosition().setY((float) ypos);
        }

        @Override
        public void close(){

        }
    }

    static class MouseScrollCallback extends GLFWScrollCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, double xoffset, double yoffset){
            Tracelog(LOG_DEBUG, "Scroll Callback Triggered");
            Core.getInput().mouse.setCurrentWheelMove((float) yoffset);
        }

        @Override
        public void close(){

        }
    }

    static class CursorEnterCallback extends GLFWCursorEnterCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

        @Override
        public void invoke(long window, boolean entered){
            Tracelog(LOG_DEBUG, "Cursor Enter Callback Triggered");
            Core.getInput().mouse.setCursorOnScreen(entered);
        }

        @Override
        public void close(){

        }
    }

    static class WindowDropCallback extends GLFWDropCallback{

        @Override
        public String getSignature(){
            return null;
        }

        @Override
        public void callback(long args){

        }

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

        @Override
        public void close(){

        }
    }
}