package com.raylib.java.core.platforms;

import com.raylib.java.Raylib;
import com.raylib.java.core.Window;
import com.raylib.java.core.callback.Callbacks;
import com.raylib.java.core.input.*;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Vector2;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static com.raylib.java.Config.*;
import static com.raylib.java.Config.ConfigFlag.*;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.GAMEPAD_AXIS_LEFT_TRIGGER;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.GAMEPAD_AXIS_RIGHT_TRIGGER;
import static com.raylib.java.core.input.Gamepad.GamepadButton.*;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_DEFAULT;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.*;
import static com.raylib.java.raymath.Raymath.MatrixScale;
import static com.raylib.java.rlgl.RLGL.rlGlVersion.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Desktop implements Platform {

    /**********************************************************************************************
     *
     *   rcore_desktop - Functions to manage window, graphics device and inputs
     *
     *   PLATFORM: DESKTOP: GLFW
     *       - Windows (Win32, Win64)
     *       - Linux (X11/Wayland desktop mode)
     *       - FreeBSD, OpenBSD, NetBSD, DragonFly (X11 desktop)
     *       - OSX/macOS (x64, arm64)
     *
     *   LIMITATIONS:
     *       - Limitation 01
     *       - Limitation 02
     *
     *   POSSIBLE IMPROVEMENTS:
     *       - Improvement 01
     *       - Improvement 02
     *
     *   ADDITIONAL NOTES:
     *       - TRACELOG() function is located in raylib [utils] module
     *
     *   CONFIGURATION:
     *       #define RCORE_PLATFORM_CUSTOM_FLAG
     *           Custom flag for rcore on target platform -not used-
     *
     *   DEPENDENCIES:
     *       - rglfw: Manage graphic device, OpenGL context and inputs (Windows, Linux, OSX, FreeBSD...)
     *       - gestures: Gestures system for touch-ready devices (or simulated from mouse inputs)
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2013-2023 Ramon Santamaria (@raysan5) and contributors
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     **********************************************************************************************/


    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    // TODO: HACK: Added flag if not provided by GLFW when using external library
    // Latest GLFW release (GLFW 3.3.8) does not implement this flag, it was added for 3.4.0-dev
    final int GLFW_MOUSE_PASSTHROUGH = 0x0002000D;
    
    //----------------------------------------------------------------------------------
    // Global Variables Definition
    //----------------------------------------------------------------------------------
    private final Raylib context;
    private final Window window;
    private final Input input;

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Window and Graphics Device
    //----------------------------------------------------------------------------------

    public Desktop(Raylib context, Window window, Input input) {
        this.context = context;
        this.window = window;
        this.input = input;
    }
    
    // Check if application should close
    // NOTE: By default, if KEY_ESCAPE pressed or window close icon clicked
    public boolean WindowShouldClose() {
        if (window.isReady()){
            return window.isShouldClose();
        }
        else {
            return true;
        }
    }

    // Toggle fullscreen mode
    public void ToggleFullscreen() {
        if (!window.isFullscreen()){
            // Store previous window position (in case we exit fullscreen)
            Vector2 windowPositionVector = GetWindowPosition();
            window.getPosition().setX(windowPositionVector.x);
            window.getPosition().setY(windowPositionVector.y);


            int monitorCount;
            PointerBuffer monitors = glfwGetMonitors();
            monitorCount = monitors.sizeof();
            int monitorIndex = GetCurrentMonitor();
            long monitor = (monitorIndex < monitorCount) ? monitors.get(monitorIndex) : -1;

            if (monitor < 0){
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to get monitor");
                window.setFullscreen(false);
                window.flags &= ~FLAG_FULLSCREEN_MODE;

                glfwSetWindowMonitor(window.handle, GetCurrentMonitor(), 0, 0, window.getScreen().width, window.getScreen().height, GLFW_DONT_CARE); // NOTE: Resizing not allowed by default!
                return;
            }
            else {
                window.setFullscreen(true);
                window.flags |= FLAG_FULLSCREEN_MODE;

                glfwSetWindowMonitor(window.handle, monitor, 0, 0, window.getScreen().width, window.getScreen().height, GLFW_DONT_CARE);
            }
        }
        else{
            window.setFullscreen(false);
            window.flags &= ~FLAG_FULLSCREEN_MODE;

            glfwSetWindowMonitor(window.handle, 0, (int) window.getPosition().x, (int) window.getPosition().y, window.getScreen().width, window.getScreen().height, GLFW_DONT_CARE);
        }

        // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
        // NOTE: V-Sync can be enabled by graphic driver configuration
        if ((window.flags & FLAG_VSYNC_HINT) == 1){
            glfwSwapInterval(1);
        }
    }

    // Toggle borderless windowed mode
    public void ToggleBorderlessWindowed() {
        // Leave fullscreen before attempting to set borderless windowed mode and get screen position from it
        boolean wasOnFullscreen = false;
        if (window.isFullscreen()) {
            window.getPosition().setX(window.getPosition().x);
            window.getPosition().setY(window.getPosition().y);
            ToggleFullscreen();
            wasOnFullscreen = true;
        }

        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();
        int monitorIndex = GetCurrentMonitor();
        long monitor = (monitorIndex < monitorCount) ? monitors.get(monitorIndex) : -1;

        if ((monitorIndex >= 0) && (monitorIndex < monitorCount)){
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            if (mode != null) {
                if (!context.core.IsWindowState(FLAG_BORDERLESS_WINDOWED_MODE)) {
                    // Store screen position and size
                    // NOTE: If it was on fullscreen, screen position was already stored, so skip
                    // setting it here
                    if (!wasOnFullscreen) {
                        Vector2 windowPositionVector = GetWindowPosition();
                        window.getPreviousPosition().setX(windowPositionVector.x);
                        window.getPreviousPosition().setY(windowPositionVector.y);
                    }

                    window.getPreviousScreen().setWidth(window.getScreen().width);
                    window.getPreviousScreen().setHeight(window.getScreen().height);

                    // Set undecorated and topmost modes and flags
                    glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_FALSE);
                    window.flags |= FLAG_WINDOW_UNDECORATED;
                    glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_TRUE);
                    window.flags |= FLAG_WINDOW_TOPMOST;

                    // Get monitor position and size
                    int monitorPosX = 0;
                    int monitorPosY = 0;
                    glfwGetMonitorPos(monitor, new int[]{monitorPosX}, new int[]{monitorPosY});
                    int monitorWidth = mode.width();
                    int monitorHeight = mode.height();

                    // Set screen position and size
                    glfwSetWindowPos(window.handle, monitorPosX, monitorPosY);
                    glfwSetWindowSize(window.handle, monitorWidth, monitorHeight);

                    // Refocus window
                    glfwFocusWindow(window.handle);

                    window.flags |= FLAG_BORDERLESS_WINDOWED_MODE;
                } else {
                    // Remove topmost and undecorated modes and flags
                    glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_FALSE);
                    window.flags &= ~FLAG_WINDOW_TOPMOST;
                    glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_TRUE);
                    window.flags &= ~FLAG_WINDOW_UNDECORATED;

                    // Return previous screen size and position
                    // NOTE: The order matters here, it must set size first, then position, otherwise the screen will be position incorrectly
                    glfwSetWindowSize(window.handle, window.getPreviousScreen().width, window.getPreviousScreen().height);
                    glfwSetWindowPos(window.handle, (int) window.getPreviousPosition().x, (int) window.getPreviousPosition().y);

                    // Refocus window
                    glfwFocusWindow(window.handle);

                    window.flags &= ~FLAG_BORDERLESS_WINDOWED_MODE;
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
    }

    // Set window state: maximized, if resizable
    public void MaximizeWindow() {
        if (glfwGetWindowAttrib(window.handle, GLFW_RESIZABLE) == GLFW_TRUE) {
            glfwMaximizeWindow(window.handle);
            window.flags |= FLAG_WINDOW_MAXIMIZED;
        }
    }

    // Set window state: minimized
    public void MinimizeWindow() {
        // NOTE: Following function launches callback that sets appropriate flag!
        glfwIconifyWindow(window.handle);
    }

    // Set window state: not minimized/maximized
    public void RestoreWindow() {
        if (glfwGetWindowAttrib(window.handle, GLFW_RESIZABLE) == GLFW_TRUE) {
            // Restores the specified window if it was previously iconified (minimized) or maximized
            glfwRestoreWindow(window.handle);
            window.flags &= ~FLAG_WINDOW_MINIMIZED;
            window.flags &= ~FLAG_WINDOW_MAXIMIZED;
        }
    }

    // Set window configuration state using flags
    public void SetWindowState(int flags) {
        // Check previous state and requested state to apply required changes
        // NOTE: In most cases the functions already change the flags internally

        // State change: FLAG_VSYNC_HINT
        if (((window.flags & FLAG_VSYNC_HINT) != (flags & FLAG_VSYNC_HINT)) && ((flags & FLAG_VSYNC_HINT) > 0)){
            glfwSwapInterval(1);
            window.flags |= FLAG_VSYNC_HINT;
        }

        // State change: FLAG_FULLSCREEN_MODE
        if ((window.flags & FLAG_FULLSCREEN_MODE) != (flags & FLAG_FULLSCREEN_MODE)){
            ToggleFullscreen();     // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_RESIZABLE
        if (((window.flags & FLAG_WINDOW_RESIZABLE) != (flags & FLAG_WINDOW_RESIZABLE)) && ((flags & FLAG_WINDOW_RESIZABLE) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_RESIZABLE, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_RESIZABLE;
        }

        // State change: FLAG_WINDOW_UNDECORATED
        if (((window.flags & FLAG_WINDOW_UNDECORATED) != (flags & FLAG_WINDOW_UNDECORATED)) && (flags & FLAG_WINDOW_UNDECORATED) > 0){
            glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_FALSE);
            window.flags |= FLAG_WINDOW_UNDECORATED;
        }

        // State change: FLAG_WINDOW_HIDDEN
        if (((window.flags & FLAG_WINDOW_HIDDEN) != (flags & FLAG_WINDOW_HIDDEN)) && ((flags & FLAG_WINDOW_HIDDEN) > 0)){
            glfwHideWindow(window.handle);
            window.flags |= FLAG_WINDOW_HIDDEN;
        }

        // State change: FLAG_WINDOW_MINIMIZED
        if (((window.flags & FLAG_WINDOW_MINIMIZED) != (flags & FLAG_WINDOW_MINIMIZED)) && ((flags & FLAG_WINDOW_MINIMIZED) > 0)){
            //GLFW_ICONIFIED
            MinimizeWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_MAXIMIZED
        if (((window.flags & FLAG_WINDOW_MAXIMIZED) != (flags & FLAG_WINDOW_MAXIMIZED)) && ((flags & FLAG_WINDOW_MAXIMIZED) > 0)){
            //GLFW_MAXIMIZED
            MaximizeWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_UNFOCUSED
        if (((window.flags & FLAG_WINDOW_UNFOCUSED) != (flags & FLAG_WINDOW_UNFOCUSED)) && ((flags & FLAG_WINDOW_UNFOCUSED) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
            window.flags |= FLAG_WINDOW_UNFOCUSED;
        }

        // State change: FLAG_WINDOW_TOPMOST
        if (((window.flags & FLAG_WINDOW_TOPMOST) != (flags & FLAG_WINDOW_TOPMOST)) && ((flags & FLAG_WINDOW_TOPMOST) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_TOPMOST;
        }

        // State change: FLAG_WINDOW_ALWAYS_RUN
        if (((window.flags & FLAG_WINDOW_ALWAYS_RUN) != (flags & FLAG_WINDOW_ALWAYS_RUN)) && ((flags & FLAG_WINDOW_ALWAYS_RUN) > 0)){
            window.flags |= FLAG_WINDOW_ALWAYS_RUN;
        }

        // The following states can not be changed after window creation

        // State change: FLAG_WINDOW_TRANSPARENT
        if (((window.flags & FLAG_WINDOW_TRANSPARENT) != (flags & FLAG_WINDOW_TRANSPARENT)) && ((flags & FLAG_WINDOW_TRANSPARENT) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window " +
                    "initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI) != (flags & FLAG_WINDOW_HIGHDPI)) && ((flags & FLAG_WINDOW_HIGHDPI) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_MOUSE_PASSTHROUGH
        if (((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) != (flags & FLAG_WINDOW_MOUSE_PASSTHROUGH)) && ((flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_MOUSE_PASSTHROUGH;
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT) != (flags & FLAG_MSAA_4X_HINT)) && ((flags & FLAG_MSAA_4X_HINT) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT) != (flags & FLAG_INTERLACED_HINT)) && ((flags & FLAG_INTERLACED_HINT) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Clear window configuration state flags
    public void ClearWindowState(int flags) {
        // Check previous state and requested state to apply required changes
        // NOTE: In most cases the functions already change the flags internally

        // State change: FLAG_VSYNC_HINT
        if (((window.flags & FLAG_VSYNC_HINT) > 0) && ((flags & FLAG_VSYNC_HINT) > 0)){
            glfwSwapInterval(0);
            window.flags &= ~FLAG_VSYNC_HINT;
        }

        // State change: FLAG_FULLSCREEN_MODE
        if (((window.flags & FLAG_FULLSCREEN_MODE) > 0) && ((flags & FLAG_FULLSCREEN_MODE) > 0)){
            ToggleFullscreen();     // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_RESIZABLE
        if (((window.flags & FLAG_WINDOW_RESIZABLE) > 0) && ((flags & FLAG_WINDOW_RESIZABLE) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_RESIZABLE, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_RESIZABLE;
        }

        // State change: FLAG_WINDOW_UNDECORATED
        if (((window.flags & FLAG_WINDOW_UNDECORATED) > 0) && ((flags & FLAG_WINDOW_UNDECORATED) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_TRUE);
            window.flags &= ~FLAG_WINDOW_UNDECORATED;
        }

        // State change: FLAG_WINDOW_HIDDEN
        if (((window.flags & FLAG_WINDOW_HIDDEN) > 0) && ((flags & FLAG_WINDOW_HIDDEN) > 0)){
            glfwShowWindow(window.handle);
            window.flags &= ~FLAG_WINDOW_HIDDEN;
        }

        // State change: FLAG_WINDOW_MINIMIZED
        if (((window.flags & FLAG_WINDOW_MINIMIZED) > 0) && ((flags & FLAG_WINDOW_MINIMIZED) > 0)){
            RestoreWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_MAXIMIZED
        if (((window.flags & FLAG_WINDOW_MAXIMIZED) > 0) && ((flags & FLAG_WINDOW_MAXIMIZED) > 0)){
            RestoreWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_UNFOCUSED
        if (((window.flags & FLAG_WINDOW_UNFOCUSED) > 0) && ((flags & FLAG_WINDOW_UNFOCUSED) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
            window.flags &= ~FLAG_WINDOW_UNFOCUSED;
        }

        // State change: FLAG_WINDOW_TOPMOST
        if (((window.flags & FLAG_WINDOW_TOPMOST) > 0) && ((flags & FLAG_WINDOW_TOPMOST) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_TOPMOST;
        }

        // State change: FLAG_WINDOW_ALWAYS_RUN
        if (((window.flags & FLAG_WINDOW_ALWAYS_RUN) > 0) && ((flags & FLAG_WINDOW_ALWAYS_RUN) > 0)){
            window.flags &= ~FLAG_WINDOW_ALWAYS_RUN;
        }

        // The following states can not be changed after window creation

        // State change: FLAG_WINDOW_TRANSPARENT
        if (((window.flags & FLAG_WINDOW_TRANSPARENT) > 0) && ((flags & FLAG_WINDOW_TRANSPARENT) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI) > 0) && ((flags & FLAG_WINDOW_HIGHDPI) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_MOUSE_PASSTHROUGH
        if (((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0) && ((flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_MOUSE_PASSTHROUGH, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_MOUSE_PASSTHROUGH;
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT) > 0) && ((flags & FLAG_MSAA_4X_HINT) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT) > 0) && ((flags & FLAG_INTERLACED_HINT) > 0)){
            context.tracelog.TRACELOG(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Set icon for window
    // NOTE 1: Image must be in RGBA format, 8bit per channel
    // NOTE 2: Image is scaled by the OS for all required sizes
    public void SetWindowIcon(Image image) {
        if (image.getFormat() == PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
            byte[] imgData = image.getData();
            GLFWImage.Buffer iconBuffer = GLFWImage.malloc(1);
            GLFWImage icon = GLFWImage.malloc();
            ByteBuffer bb = ByteBuffer.allocateDirect(imgData.length);
            bb.put(imgData).flip();
            icon.set(image.width, image.height, bb);

            iconBuffer.put(icon);
            icon.free();
            iconBuffer.position(0);

            // NOTE 1: We only support one image icon
            // NOTE 2: The specified image data is copied before this function returns
            glfwSetWindowIcon(window.handle, iconBuffer);
            iconBuffer.free();
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: window icon image must be in R8G8B8A8 pixel format");
        }
    }

    // Set icon for window, multiple images
    // NOTE 1: Images must be in RGBA format, 8bit per channel
    // NOTE 2: The multiple images are used depending on provided sizes
    // Standard Windows icon sizes: 256, 128, 96, 64, 48, 32, 24, 16
    public void SetWindowIcons(List<Image> images) {
        int count = images.size();

        if (images.isEmpty()){
            glfwSetWindowIcon(window.handle, null);
        }
        else{
            GLFWImage.Buffer icons = GLFWImage.malloc(count);

            for (int i = 0; i < count; i++){
                Image image = images.get(i);

                if (image.getFormat() == PIXELFORMAT_UNCOMPRESSED_R8G8B8A8){
                    icons.get(i).set(
                            image.width,
                            image.height,
                            ByteBuffer.wrap(image.getData())
                    );
                }
                else{
                    context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Window icon image must be in R8G8B8A8 pixel format");
                }
            }
            // NOTE: Images data is copied internally before this function returns
            glfwSetWindowIcon(window.handle, icons);

            icons.free();
        }
    }

    // Set title for window
    public void SetWindowTitle(String title) {
        window.setTitle(title);
        glfwSetWindowTitle(window.handle, title);
    }

    // Set window position on screen (windowed mode)
    public void SetWindowPosition(int x, int y) {
        glfwSetWindowPos(window.handle, x, y);
    }

    // Set monitor for the current window
    public void SetWindowMonitor(int monitor) {
        int monitorCount = 0;
        PointerBuffer monitors = glfwGetMonitors();

        if ((monitor >= 0) && (monitor < monitorCount)){
            context.tracelog.TRACELOG(LOG_INFO, "GLFW: Selected fullscreen monitor: [" + monitor + "] " + glfwGetMonitorName(monitor));

            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window.handle, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
    }

    // Set window minimum dimensions (FLAG_WINDOW_RESIZABLE)
    public void SetWindowMinSize(int width, int height) {
        window.getScreenMin().width = width;
        window.getScreenMin().height = height;

        int minWidth  = (window.getScreenMin().width  == 0)? GLFW_DONT_CARE : window.getScreenMin().width;
        int minHeight = (window.getScreenMin().height == 0)? GLFW_DONT_CARE : window.getScreenMin().height;
        int maxWidth  = (window.getScreenMax().width  == 0)? GLFW_DONT_CARE : window.getScreenMax().width;
        int maxHeight = (window.getScreenMax().height == 0)? GLFW_DONT_CARE : window.getScreenMax().height;

        glfwSetWindowSizeLimits(window.handle, minWidth, minHeight, maxWidth, maxHeight);
    }

    // Set window maximum dimensions (FLAG_WINDOW_RESIZABLE)
    public void SetWindowMaxSize(int width, int height) {
        window.getScreenMax().width = width;
        window.getScreenMax().height = height;

        int minWidth  = (window.getScreenMin().width  == 0)? GLFW_DONT_CARE : window.getScreenMin().width;
        int minHeight = (window.getScreenMin().height == 0)? GLFW_DONT_CARE : window.getScreenMin().height;
        int maxWidth  = (window.getScreenMax().width  == 0)? GLFW_DONT_CARE : window.getScreenMax().width;
        int maxHeight = (window.getScreenMax().height == 0)? GLFW_DONT_CARE : window.getScreenMax().height;

        glfwSetWindowSizeLimits(window.handle, minWidth, minHeight, maxWidth, maxHeight);
    }

    // Set window dimensions
    public void SetWindowSize(int width, int height) {
        glfwSetWindowSize(window.handle, width, height);
    }

    // Set window opacity, value opacity is between 0.0 and 1.0
    public void SetWindowOpacity(float opacity) {
        if (opacity >= 1.0f) {
            opacity = 1.0f;
        }
        else if (opacity <= 0.0f) {
            opacity = 0.0f;
        }
        glfwSetWindowOpacity(window.handle, opacity);
    }

    // Set window focused
    public void SetWindowFocused() {
        glfwFocusWindow(window.handle);
    }

    // Get native window window.handle
    public long GetWindowHandle() {
        if (__WINDOWS__) {
            return glfwGetWin32Window(window.handle);
        }
        else if (__APPLE__) {
            return glfwGetCocoaWindow(window.handle);
        }
        else {
            return window.handle;
        }
    }

    // Get number of monitors
    public int GetMonitorCount() {
        int monitorCount = 0;
        PointerBuffer pb = glfwGetMonitors();
        monitorCount = pb.sizeof();
        return monitorCount;
    }

    // Get number of monitors
    public int GetCurrentMonitor() {
        return 0;
        /*
        int index = 0;
        PointerBuffer monitors = glfwGetMonitors();
        int monitorCount = monitors.capacity();
        long monitor = -1;

        if (monitorCount >= 1) {
            if (context.core.IsWindowFullscreen()) {
                // Get the window.handle of the monitor that the specified window is in full screen on
                monitor = glfwGetWindowMonitor(window.handle);

                for (int i = 0; i < monitorCount; i++) {
                    if (monitors.get(i) == monitor) {
                        index = i;
                        break;
                    }
                }
            }
            else {
                // In case the window is between two monitors, we use below logic
                // to try to detect the "current monitor" for that window, note that
                // this is probably an overengineered solution for a very side case
                // trying to match SDL behaviour

                int closestDist = 0x7FFFFFFF;

                // Window center position
                IntBuffer wcxb = IntBuffer.allocate(1);
                IntBuffer wcyb = IntBuffer.allocate(1);

                glfwGetWindowPos(window.handle, wcxb, wcyb);

                int wcx = wcxb.get();
                int wcy = wcyb.get();

                wcx += window.getScreen().width /2;
                wcy += window.getScreen().height /2;

                for (int i = 0; i < monitorCount; i++) {
                    // Monitor top-left position
                    IntBuffer mxb = IntBuffer.allocate(1);
                    IntBuffer myb = IntBuffer.allocate(1);

                    monitor = monitors.get(i);
                    glfwGetMonitorPos(monitor, mxb, myb);
                    GLFWVidMode mode = glfwGetVideoMode(monitor);

                    int mx = mxb.get();
                    int my = myb.get();

                    if (mode != null) {
                        int right = mx + mode.width() - 1;
                        int bottom = my + mode.height() - 1;

                        if ((wcx >= mx) && (wcx <= right) && (wcy >= my) && (wcy <= bottom)) {
                            index = i;
                            break;
                        }

                        int xclosest = wcx;
                        if (wcx < mx) {
                            xclosest = mx;
                        }
                        else if (wcx > right) {
                            xclosest = right;
                        }

                        int yclosest = wcy;
                        if (wcy < my) {
                            yclosest = my;
                        }
                        else if (wcy > bottom) {
                            yclosest = bottom;
                        }

                        int dx = wcx - xclosest;
                        int dy = wcy - yclosest;
                        int dist = (dx*dx) + (dy*dy);
                        if (dist < closestDist) {
                            index = i;
                            closestDist = dist;
                        }
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
                    }
                }
            }
        }

        return index;

         */
    }

    // Get selected monitor position
    public Vector2 GetMonitorPosition(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            IntBuffer x = IntBuffer.allocate(1);
            IntBuffer y = IntBuffer.allocate(1);
            glfwGetMonitorPos(monitor, x, y);
            return new Vector2(x.get(0), y.get(0));
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }

        return new Vector2();
    }

    // Get selected monitor width (currently used by monitor)
    public int GetMonitorWidth(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            GLFWVidMode.Buffer modes = glfwGetVideoModes(monitors.get(monitor));

            // We return the maximum resolution available, the last one in the modes array
            if (modes.sizeof() > 0){
                return modes.width();
            }
            else{
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor height (currently used by monitor)
    public int GetMonitorHeight(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            GLFWVidMode.Buffer modes = glfwGetVideoModes(monitors.get(monitor));

            // We return the maximum resolution available, the last one in the modes array
            if (modes.sizeof() > 0){
                return modes.height();
            }
            else{
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
            return 0;
        }
        return 0;
    }

    // Get selected monitor physical width in millimetres
    public int GetMonitorPhysicalWidth(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            int physicalWidth = 0;
            nglfwGetMonitorPhysicalSize(monitor, physicalWidth, 0);
            return physicalWidth;
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor physical height in millimetres
    public int GetMonitorPhysicalHeight(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            int physicalHeight = 0;
            nglfwGetMonitorPhysicalSize(monitor, 0, physicalHeight);
            return physicalHeight;
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor refresh rate
    public int GetMonitorRefreshRate(int monitor) {
        PointerBuffer monitors = glfwGetMonitors();
        int monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)) {
            GLFWVidMode vidMode = glfwGetVideoMode(monitors.get(monitor));
            return vidMode != null ? vidMode.refreshRate() : 0;
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }

        return 0;
    }

    // Get the human-readable, UTF-8 encoded name of the selected monitor
    public String GetMonitorName(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            return glfwGetMonitorName(monitor);
        }
        else{
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return "";
    }

    // Get window position XY on monitor
    public Vector2 GetWindowPosition() {
        // Memory-safe get window position
        try (MemoryStack stack = stackPush()){
            IntBuffer xBuffer = stack.mallocInt(1);
            IntBuffer yBuffer = stack.mallocInt(1);
            if (PLATFORM_DESKTOP){
                glfwGetWindowPos(window.handle, xBuffer, yBuffer);
                return new Vector2(xBuffer.get(0), yBuffer.get(0));
            }
            else {
                return new Vector2();
            }
        }
    }

    // Get window scale DPI factor for current monitor
    public Vector2 GetWindowScaleDPI() {
        Vector2 scale = new Vector2(1f,1f);

        try (MemoryStack stack = stackPush()){
            FloatBuffer xDpi = stack.mallocFloat(1);
            FloatBuffer yDpi = stack.mallocFloat(1);
            Vector2 windowPos = GetWindowPosition();

            PointerBuffer monitors = glfwGetMonitors();
            int monitorCount = monitors.sizeof();

            // Check window monitor
            for (int i = 0; i < monitorCount; i++){
                glfwGetMonitorContentScale(monitors.get(i), xDpi, yDpi);

                IntBuffer xPos, yPos, width, height;
                xPos = stack.mallocInt(1);
                yPos = stack.mallocInt(1);
                width = stack.mallocInt(1);
                height = stack.mallocInt(1);

                glfwGetMonitorWorkarea(monitors.get(i), xPos, yPos, width, height);

                if ((windowPos.x >= xPos.get(0)) && (windowPos.x < xPos.get(0) + width.get(0)) &&
                        (windowPos.y >= yPos.get(0)) && (windowPos.y < yPos.get(0) + height.get(0))){
                    scale.x = xDpi.get(i);
                    scale.y = yDpi.get(i);
                    break;
                }
            }
        }

        return scale;
    }

    // Set clipboard text content
    public void SetClipboardText(String text) {
        glfwSetClipboardString(window.handle, text);
    }

    // Get clipboard text content
    // NOTE: returned string is allocated and freed by GLFW
    public String GetClipboardText() {
        return glfwGetClipboardString(window.handle);
    }

    // Show mouse cursor
    public void ShowCursor() {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        input.getMouse().cursorHidden = false;
    }

    // Hides mouse cursor
    public void HideCursor()
    {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        input.getMouse().cursorHidden = true;
    }

    // Enables cursor (unlock cursor)
    public void EnableCursor() {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        // Set cursor position in the middle
        SetMousePosition(window.getScreen().width/2, window.getScreen().height/2);

        input.getMouse().cursorHidden = false;
    }

    // Disables cursor (lock cursor)
    public void DisableCursor() {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Set cursor position in the middle
        SetMousePosition(window.getScreen().width/2, window.getScreen().height/2);

        input.getMouse().cursorHidden = true;
    }

    // Swap back buffer with front buffer (screen drawing)
    public void SwapScreenBuffer() {
        glfwSwapBuffers(window.handle);
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Misc
    //----------------------------------------------------------------------------------

    // Get elapsed time measure in seconds since InitTimer()
    double GetTime() {
        // Elapsed time since glfwInit()
        return glfwGetTime();
    }

    // Open URL with default system browser (if available)
    // NOTE: This function is only safe to use if you control the URL given.
    // A user could craft a malicious string performing another action.
    // Only call this function yourself not with user input or make sure to check the string yourself.
    // Ref: https://github.com/raysan5/raylib/issues/686
    public void OpenURL(String url) {
        if (url.contains("'")) {
            context.tracelog.TRACELOG(LOG_WARNING, "SYSTEM: Provided URL could be potentially malicious, avoid ['] character");
        }
        else {
            Runtime rt = Runtime.getRuntime();

            if (__WINDOWS__) {
                try {
                    rt.exec(new String[]{"rundll32 url.dll,FileProtocolHandler " + url});
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (__APPLE__) {
                try {
                    rt.exec(new String[]{"open " + url});
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (__LINUX__) {
                try {
                    rt.exec(new String[]{"xdg-open " + url});
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Inputs
    //----------------------------------------------------------------------------------

    // Set internal gamepad mappings
    public boolean SetgamepadMappings(byte[] mappings){
        boolean result = false;

        ByteBuffer mappingsBuffer = ByteBuffer.allocateDirect(mappings.length);
        mappingsBuffer.put(mappings).flip();

        if (PLATFORM_DESKTOP){
            result = glfwUpdateGamepadMappings(mappingsBuffer);
        }

        return result;
    }

    // Set mouse position XY
    public void SetMousePosition(int x, int y){
        input.mouse.currentPosition = new Vector2(x, y);
        input.mouse.previousPosition = input.mouse.currentPosition;

        // NOTE: emscripten not implemented
        glfwSetCursorPos(window.handle, input.mouse.currentPosition.x, input.mouse.currentPosition.y);
    }

    // Set mouse cursor
    public void SetMouseCursor(Mouse.MouseCursor mouseCursor) {
        input.mouse.cursor = mouseCursor;
        if (mouseCursor == MOUSE_CURSOR_DEFAULT) {
            glfwSetCursor(window.handle, 0);
        }
        else {
            // NOTE: We are relating internal GLFW enum values to our mouseCursor enum values
            glfwSetCursor(window.handle, glfwCreateStandardCursor(0x00036000 + mouseCursor.GetValue()));
        }
    }


    //----------------------------------------------------------------------------------
    // Module Internal Functions Definition
    //----------------------------------------------------------------------------------

    // Initialize platform: graphics, inputs and more
    public int InitPlatform() {
        Callbacks callbacks = new Callbacks(context);
        glfwSetErrorCallback(callbacks.errorCallback);
        /*
            // TODO: Setup GLFW custom allocators to match raylib ones
            GLFWallocator allocator = {
                .allocate = MemAlloc,
                .deallocate = MemFree,
                .reallocate = MemRealloc,
                .user = NULL
            };

            glfwInitAllocator(&allocator);
        */

        if(__APPLE__) {
            glfwInitHint(GLFW_COCOA_CHDIR_RESOURCES, GLFW_FALSE);
        }
        // Initialize GLFW internal global state
        int result = glfwInit() ? 1 : -1;

        if (result == -1) {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to initialize GLFW"); 
            return -1; 
        }

        // Initialize graphic device: display/window and graphic context
        //----------------------------------------------------------------------------
        glfwDefaultWindowHints();                       // Set default windows hints
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        //glfwWindowHint(GLFW_RED_BITS, 8);             // Framebuffer red color component bits
        //glfwWindowHint(GLFW_GREEN_BITS, 8);           // Framebuffer green color component bits
        //glfwWindowHint(GLFW_BLUE_BITS, 8);            // Framebuffer blue color component bits
        //glfwWindowHint(GLFW_ALPHA_BITS, 8);           // Framebuffer alpha color component bits
        //glfwWindowHint(GLFW_DEPTH_BITS, 24);          // Depthbuffer bits
        //glfwWindowHint(GLFW_REFRESH_RATE, 0);         // Refresh rate for fullscreen window
        //glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API); // OpenGL API to use. Alternative: GLFW_OPENGL_ES_API
        //glfwWindowHint(GLFW_AUX_BUFFERS, 0);          // Number of auxiliar buffers

        // Check window creation flags
        if ((window.flags & FLAG_FULLSCREEN_MODE) > 0) {
            window.setFullscreen(true);
        }

        if ((window.flags & FLAG_WINDOW_HIDDEN) > 0) {
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Visible window
        }
        else {
            glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);     // Window initially hidden
        }

        if ((window.flags & FLAG_WINDOW_UNDECORATED) > 0) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE); // Border and buttons on Window
        }
        else {
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);   // Decorated window
        }

        if ((window.flags & FLAG_WINDOW_RESIZABLE) > 0) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Resizable window
        }
        else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);  // Avoid window being resizable
        }

        // Disable FLAG_WINDOW_MINIMIZED, not supported on initialization
        if ((window.flags & FLAG_WINDOW_MINIMIZED) > 0) {
            window.flags &= ~FLAG_WINDOW_MINIMIZED;
        }

        // Disable FLAG_WINDOW_MAXIMIZED, not supported on initialization
        if ((window.flags & FLAG_WINDOW_MAXIMIZED) > 0) {
            window.flags &= ~FLAG_WINDOW_MAXIMIZED;
        }

        if ((window.flags & FLAG_WINDOW_UNFOCUSED) > 0) {
            glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
        }
        else {
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        }

        if ((window.flags & FLAG_WINDOW_TOPMOST) > 0) {
            glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        }
        else {
            glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        }

        // NOTE: Some GLFW flags are not supported on HTML5
        if ((window.flags & FLAG_WINDOW_TRANSPARENT) > 0) {
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);     // Transparent framebuffer
        }
        else {
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE);  // Opaque framebuffer
        }

        if ((window.flags & FLAG_WINDOW_HIGHDPI) > 0) {
            // Resize window content area based on the monitor content scale.
            // NOTE: This hint only has an effect on platforms where screen coordinates and pixels always map 1:1 such as Windows and X11.
            // On platforms like macOS the resolution of the framebuffer is changed independently of the window size.
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);   // Scale content area based on the monitor content scale where window is placed on
            if(__APPLE__) {
                glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE);
            }
        }
        else {
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE);
        }

        // getMouse() passthrough
        if ((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0) {
            glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
        }
        else {
            glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_FALSE);
        }

        if ((window.getFlags() & FLAG_MSAA_4X_HINT) == 1) {
            // NOTE: MSAA is only enabled for main framebuffer, not user-created FBOs
            context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Trying to enable MSAA x4");
            glfwWindowHint(GLFW_SAMPLES, 4);   // Tries to enable multisampling x4 (MSAA), default is 0
        }

        // NOTE: When asking for an OpenGL context version, most drivers provide the highest supported version
        // with backward compatibility to older OpenGL versions.
        // For example, if using OpenGL 1.1, driver can provide a 4.3 backwards compatible context.

        // Check selection OpenGL version
        if (context.rlgl.rlGetVersion() == OPENGL_21) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);          // Choose OpenGL minor version (just hint)
        }
        else if (context.rlgl.rlGetVersion() == OPENGL_33) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);          // Choose OpenGL minor version (just hint)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Profiles Hint: Only 3.3 and above!
            // Values: GLFW_OPENGL_CORE_PROFILE, GLFW_OPENGL_ANY_PROFILE, GLFW_OPENGL_COMPAT_PROFILE
            if(__APPLE__) {
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);  // OSX Requires forward compatibility
            }
            else {
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE); // Forward Compatibility Hint: Only 3.3 and above!
            }
            //glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE); // Request OpenGL DEBUG context
        }
        else if (context.rlgl.rlGetVersion() == OPENGL_43) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);          // Choose OpenGL minor version (just hint)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);
            if(false) {
                glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);   // Enable OpenGL Debug Context
            }
        }
        else if (context.rlgl.rlGetVersion() == OPENGL_ES_20) {
            // Request OpenGL ES 2.0 context
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
        }
        else if (context.rlgl.rlGetVersion() == OPENGL_ES_30) { 
            // Request OpenGL ES 3.0 context
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
        }

        // NOTE: GLFW 3.4+ defers initialization of the Joystick subsystem on the first call to any Joystick related functions.
        // Forcing this initialization here avoids doing it on PollInputEvents() called by EndDrawing() after first frame has been just drawn.
        // The initialization will still happen and possible delays still occur, but before the window is shown, which is a nicer experience.
        // REF: https://github.com/raysan5/raylib/issues/1554
        glfwSetJoystickCallback(null);

        // Find monitor resolution
        long monitor = glfwGetPrimaryMonitor();
        if (monitor == -1) {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to get primary monitor");
            return -1;
        }

        GLFWVidMode mode = glfwGetVideoMode(monitor);

        window.getDisplay().width = mode.width();
        window.getDisplay().height = mode.height();

        // Set screen width/height to the display width/height if they are 0
        if (window.getScreen().width == 0) {
            window.getScreen().width = window.getDisplay().width;
        }
        if (window.getScreen().height == 0) {
            window.getScreen().height = window.getDisplay().height;
        }

        if (window.isFullscreen()) {
            // remember center for switchinging from fullscreen to window
            if ((window.getScreen().height == window.getDisplay().height) && (window.getScreen().width == window.getDisplay().width)) {
                // If screen width/height equal to the display, we can't calculate the window pos for toggling full-screened/windowed.
                // Toggling full-screened/windowed with pos(0, 0) can cause problems in some platforms, such as X11.
                window.getPosition().x = window.getDisplay().width/4;
                window.getPosition().y = window.getDisplay().height/4;
            }
            else {
                window.getPosition().x = window.getDisplay().width/2 - window.getScreen().width/2;
                window.getPosition().y = window.getDisplay().height/2 - window.getScreen().height/2;
            }

            if (window.getPosition().x < 0) window.getPosition().x = 0;
            if (window.getPosition().y < 0) window.getPosition().y = 0;

            // Obtain recommended window.getDisplay().width/window.getDisplay().height from a valid video mode for the monitor
            GLFWVidMode.Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
            int count = (modes != null) ? modes.sizeof() : 0;
            // Get the closest video mode to desired window.screen.width/window.screen.height
            for (int i = 0; i < count; i++){
                if (modes.width() >= window.getScreen().width){
                    if (modes.height() >= window.getScreen().height){
                        window.getDisplay().setWidth(modes.width());
                        window.getDisplay().setHeight(modes.height());
                        break;
                    }
                }
            }

            context.tracelog.TRACELOG(LOG_WARNING, "SYSTEM: Closest fullscreen videomode: %d x %d", window.getDisplay().width, window.getDisplay().height);

            // NOTE: ISSUE: Closest videomode could not match monitor aspect-ratio, for example,
            // for a desired screen size of 800x450 (16:9), closest supported videomode is 800x600 (4:3),
            // framebuffer is rendered correctly but once displayed on a 16:9 monitor, it gets stretched
            // by the sides to fit all monitor space...

            // Try to setup the most appropriate fullscreen framebuffer for the requested screenWidth/screenHeight
            // It considers device display resolution mode and setups a framebuffer with black bars if required (render size/offset)
            // Modified global variables: window.getScreen().width/window.getScreen().height - window.getRender().width/window.getRender().height - window.renderOffset.x/window.renderOffset.y - window.screenScale
            // TODO: It is a quite cumbersome solution to display size vs requested size, it should be reviewed or removed...
            // HighDPI monitors are properly considered in a following similar function: SetupViewport()
            context.core.SetupFramebuffer(window.getDisplay().width, window.getDisplay().height);

            window.handle = glfwCreateWindow(window.getDisplay().width, window.getDisplay().height, (!window.getTitle().isBlank() || window.getTitle() != null) ? window.getTitle() : " ", glfwGetPrimaryMonitor(), 0);

            // NOTE: Full-screen change, not working properly...
            //glfwSetWindowMonitor(window.handle, glfwGetPrimaryMonitor(), 0, 0, window.getScreen().width, window.getScreen().height, GLFW_DONT_CARE);
        }
        else {
            // If we are windowed fullscreen, ensures that window does not minimize when focus is lost
            if ((window.getScreen().height == window.getDisplay().height) && (window.getScreen().width == window.getDisplay().width)) {
                glfwWindowHint(GLFW_AUTO_ICONIFY, 0);
            }

            // No-fullscreen window creation
            window.handle = glfwCreateWindow(
                    window.getScreen().width,
                    window.getScreen().height,
                    (!window.getTitle().isBlank() || window.getTitle() != null) ? window.getTitle() : " ",
                    0,
                    0
            );

            if (window.handle != -1) {
                window.getRender().width = window.getScreen().width;
                window.getRender().height = window.getScreen().height;
            }
        }

        if (window.handle == -1) {
            glfwTerminate();
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to initialize Window");
            return -1;
        }
        PointerBuffer errorBuffer = PointerBuffer.allocateDirect(64);

        glfwMakeContextCurrent(window.handle);
        GL.createCapabilities();
        if (context.config.RLGL_ENABLE_OPENGL_DEBUG_CONTEXT) {
            GLUtil.setupDebugMessageCallback();
        }
        result = glfwGetError(errorBuffer);

        // Check context activation
        if ((result != GLFW_NO_WINDOW_CONTEXT) && (result != GLFW_PLATFORM_ERROR)) {
            window.setReady(true);

            glfwSwapInterval(0);        // No V-Sync by default

            // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
            // NOTE: V-Sync can be enabled by graphic driver configuration, it doesn't need
            // to be activated on web platforms since VSync is enforced there.
            if ((window.flags & FLAG_VSYNC_HINT) != 0) {
                // WARNING: It seems to hit a critical render path in Intel HD Graphics
                glfwSwapInterval(1);
                context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Trying to enable VSYNC");
            }

            int fbWidth = window.getScreen().width;
            int fbHeight = window.getScreen().height;

            if ((window.flags & FLAG_WINDOW_HIGHDPI) > 0) {
                // NOTE: On APPLE platforms system should manage window/input scaling and also framebuffer scaling.
                // Framebuffer scaling should be activated with: glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE);
                if(__APPLE__) {
                    IntBuffer width = IntBuffer.allocate(1);
                    IntBuffer height = IntBuffer.allocate(1);
                    glfwGetFramebufferSize(window.handle, width, height);

                    // Screen scaling matrix is required in case desired screen area is different from display area
                    window.setScreenScale(MatrixScale((float) width.get(0) / window.getScreen().width, (float) height.get(0) / window.getScreen().height, 1.0f));

                    // getMouse() input scaling for the new screen size
                    context.core.SetMouseScale((float) window.getScreen().width / width.get(0), (float) window.getScreen().height / height.get(0));
                }
            }

            window.getRender().width = fbWidth;
            window.getRender().height = fbHeight;
            window.getCurrentFbo().width = fbWidth;
            window.getCurrentFbo().height = fbHeight;

            context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Device initialized successfully");
            context.tracelog.TRACELOG(LOG_INFO, "    > Display size: %d x %d", window.getDisplay().width, window.getDisplay().height);
            context.tracelog.TRACELOG(LOG_INFO, "    > Screen size:  %d x %d", window.getScreen().width, window.getScreen().height);
            context.tracelog.TRACELOG(LOG_INFO, "    > Render size:  %d x %d", window.getRender().width, window.getRender().height);
            context.tracelog.TRACELOG(LOG_INFO, "    > Viewport offsets: %02f, %02f", window.getRenderOffset().x, window.getRenderOffset().y);
        }
        else {
            context.tracelog.TRACELOG(LOG_FATAL, "PLATFORM: Failed to initialize graphics device");
            return -1;
        }

        if ((window.flags & FLAG_WINDOW_MINIMIZED) > 0) {
            MinimizeWindow();
        }

        // If graphic device is no properly initialized, we end program
        if (!window.isReady()) {
            context.tracelog.TRACELOG(LOG_FATAL, "PLATFORM: Failed to initialize graphic device");
            return -1;
        }
        else {
            SetWindowPosition(GetMonitorWidth(GetCurrentMonitor())/2 - window.getScreen().width/2, GetMonitorHeight(GetCurrentMonitor())/2 - window.getScreen().height/2);
        }

        // Load OpenGL extensions
        // NOTE: GL procedures address loader is required to load extensions
        context.rlgl.rlLoadExtensions();
        //----------------------------------------------------------------------------

        // Initialize input events callbacks
        //----------------------------------------------------------------------------
        // Set window callback events
        glfwSetWindowSizeCallback(window.handle, callbacks.windowSizeCallback); // NOTE: Resizing not allowed by default!
        glfwSetWindowMaximizeCallback(window.handle, callbacks.windowMaximizeCallback);
        glfwSetWindowIconifyCallback(window.handle, callbacks.windowIconifyCallback);
        glfwSetWindowFocusCallback(window.handle, callbacks.windowFocusCallback);
        glfwSetDropCallback(window.handle, callbacks.windowDropCallback);


        // Set input callback events
        glfwSetKeyCallback(window.handle, callbacks.keyCallback);
        glfwSetCharCallback(window.handle, callbacks.charCallback);
        glfwSetMouseButtonCallback(window.handle, callbacks.mouseButtonCallback);
        glfwSetCursorPosCallback(window.handle, callbacks.mouseCursorPosCallback);

        // Track mouse position changes
        glfwSetScrollCallback(window.handle, callbacks.mouseScrollCallback);
        glfwSetCursorEnterCallback(window.handle, callbacks.cursorEnterCallback);

        glfwSetJoystickCallback(callbacks.joystickCallback);

        glfwSetInputMode(window.handle, GLFW_LOCK_KEY_MODS, GLFW_TRUE);    // Enable lock keys modifiers (CAPS, NUM)

        // Retrieve gamepad names
        for (int i = 0; i < MAX_GAMEPADS; i++) {
            if (glfwJoystickPresent(i)) {
                input.getGamepad().setName(glfwGetJoystickName(i), i);
            }
        }
        //----------------------------------------------------------------------------

        // Initialize timming system
        //----------------------------------------------------------------------------
        context.core.InitTimer();
        //----------------------------------------------------------------------------

        // Initialize storage system
        //----------------------------------------------------------------------------
        // CORE.Storage.basePath = GetWorkingDirectory();
        //----------------------------------------------------------------------------

        context.tracelog.TRACELOG(LOG_INFO, "PLATFORM: DESKTOP (GLFW): Initialized successfully");

        return 0;
    }

    /**
     * Poll (store) all input events
     */
    public void PollInputEvents() {
        if(SUPPORT_GESTURES_SYSTEM) {
            // NOTE: Gestures update must be called every frame to reset gestures correctly
            // because ProcessGestureEvent() is just called on an event, not every frame
            // context.core.UpdateGestures();
        }

        // Reset keys/chars pressed registered
        input.keyboard.keyPressedQueueCount = 0;
        input.keyboard.charPressedQueueCount = 0;

        // Reset last gamepad button/axis registered state
        input.gamepad.lastButtonPressed = 0;       // GAMEPAD_BUTTON_UNKNOWN
        //input.gamepad.axisCount = 0;

        // keyboard/mouse input polling (automatically managed by GLFW3 through callback)

        // Register previous keys states
        for (int i = 0; i < MAX_KEYBOARD_KEYS; i++)
        {
            input.keyboard.previousKeyState[i] = input.keyboard.currentKeyState[i];
            input.keyboard.keyRepeatInFrame[i] = false;
        }

        // Register previous mouse states
        for (int i = 0; i < MAX_MOUSE_BUTTONS; i++) input.mouse.previousButtonState[i] = input.mouse.currentButtonState[i];

        // Register previous mouse wheel state
        input.mouse.previousWheelMove = input.mouse.currentWheelMove;
        input.mouse.currentWheelMove = new Vector2();

        // Register previous mouse position
        input.mouse.previousPosition = input.mouse.currentPosition;

        // Register previous touch states
        for (int i = 0; i < MAX_TOUCH_POINTS; i++) {
            // input.touch.previoustouchState[i] = input.touch.currenttouchState[i];
        }

        // Reset touch positions
        //for (int i = 0; i < MAX_TOUCH_POINTS; i++) input.touch.position[i] = (Vector2){ 0, 0 };

        // Map touch position to mouse position for convenience
        // WARNING: If the target desktop device supports touch screen, this behavious should be reviewed!
        // TODO: GLFW does not support multi-touch input just yet
        // https://www.codeproject.com/Articles/668404/Programming-for-Multi-touch
        // https://docs.microsoft.com/en-us/windows/win32/wintouch/getting-started-with-multi-touch-messages
        input.touch.position[0] = input.mouse.currentPosition;

        // Check if gamepads are ready
        // NOTE: We do it here in case of disconnection
        for (int i = 0; i < MAX_GAMEPADS; i++) {
            input.gamepad.ready[i] = glfwJoystickPresent(i) && glfwJoystickIsGamepad(i);
        }

        // Register gamepads buttons events
        for (int i = 0; i < MAX_GAMEPADS; i++) {
            // Check if gamepad is available
            if (input.gamepad.ready[i]) {
                // Register previous gamepad states
                System.arraycopy(input.gamepad.currentButtonState[i], 0, input.gamepad.previousButtonState[i], 0, MAX_GAMEPAD_BUTTONS);

                // Get current gamepad state
                // NOTE: There is no callback available, so we get it manually
                ByteBuffer stateBuffer = ByteBuffer.allocateDirect(MAX_GAMEPAD_BUTTONS);
                GLFWGamepadState state = new GLFWGamepadState(stateBuffer);
                glfwGetGamepadState(i, state); // This remapps all gamepads so they have their buttons mapped like an xbox controller

                byte[] buttons = new byte[state.buttons().capacity()];
                state.buttons().get(buttons);

                for (int k = 0; (buttons != null) && (k < GLFW_GAMEPAD_BUTTON_DPAD_LEFT + 1) && (k < MAX_GAMEPAD_BUTTONS); k++) {
                    Gamepad.GamepadButton button = null;        // gamepadButton enum values assigned

                    switch (k) {
                        case GLFW_GAMEPAD_BUTTON_Y: button = GAMEPAD_BUTTON_RIGHT_FACE_UP; break;
                        case GLFW_GAMEPAD_BUTTON_B: button = GAMEPAD_BUTTON_RIGHT_FACE_RIGHT; break;
                        case GLFW_GAMEPAD_BUTTON_A: button = GAMEPAD_BUTTON_RIGHT_FACE_DOWN; break;
                        case GLFW_GAMEPAD_BUTTON_X: button = GAMEPAD_BUTTON_RIGHT_FACE_LEFT; break;

                        case GLFW_GAMEPAD_BUTTON_LEFT_BUMPER: button = GAMEPAD_BUTTON_LEFT_TRIGGER_1; break;
                        case GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER: button = GAMEPAD_BUTTON_RIGHT_TRIGGER_1; break;

                        case GLFW_GAMEPAD_BUTTON_BACK: button = GAMEPAD_BUTTON_MIDDLE_LEFT; break;
                        case GLFW_GAMEPAD_BUTTON_GUIDE: button = GAMEPAD_BUTTON_MIDDLE; break;
                        case GLFW_GAMEPAD_BUTTON_START: button = GAMEPAD_BUTTON_MIDDLE_RIGHT; break;

                        case GLFW_GAMEPAD_BUTTON_DPAD_UP: button = GAMEPAD_BUTTON_LEFT_FACE_UP; break;
                        case GLFW_GAMEPAD_BUTTON_DPAD_RIGHT: button = GAMEPAD_BUTTON_LEFT_FACE_RIGHT; break;
                        case GLFW_GAMEPAD_BUTTON_DPAD_DOWN: button = GAMEPAD_BUTTON_LEFT_FACE_DOWN; break;
                        case GLFW_GAMEPAD_BUTTON_DPAD_LEFT: button = GAMEPAD_BUTTON_LEFT_FACE_LEFT; break;

                        case GLFW_GAMEPAD_BUTTON_LEFT_THUMB: button = GAMEPAD_BUTTON_LEFT_THUMB; break;
                        case GLFW_GAMEPAD_BUTTON_RIGHT_THUMB: button = GAMEPAD_BUTTON_RIGHT_THUMB; break;
                        default: break;
                    }

                    // Check for valid button
                    if (button != null) {
                        if (buttons[k] == GLFW_PRESS) {
                            input.gamepad.currentButtonState[i][button.GetValue()] = 1;
                            input.gamepad.lastButtonPressed = button.GetValue();
                        }
                        else {
                            input.gamepad.currentButtonState[i][button.GetValue()] = 0;
                        }
                    }
                }

                // Get current axis state
                float[] axes = new float[6];
                state.axes().get(axes);

                for (int k = 0; (axes != null) && (k < GLFW_GAMEPAD_AXIS_LAST + 1) && (k < MAX_GAMEPAD_AXIS); k++) {
                    input.gamepad.axisState[i][k] = axes[k];
                }

                // Register buttons for 2nd triggers (because GLFW doesn't count these as buttons but rather axis)
                input.gamepad.currentButtonState[i][GAMEPAD_BUTTON_LEFT_TRIGGER_2.GetValue()] = (byte) ((input.gamepad.axisState[i][GAMEPAD_AXIS_LEFT_TRIGGER.GetValue()] > 0.1f) ? 1 : 0);
                input.gamepad.currentButtonState[i][GAMEPAD_BUTTON_RIGHT_TRIGGER_2.GetValue()] = (byte) ((input.gamepad.axisState[i][GAMEPAD_AXIS_RIGHT_TRIGGER.GetValue()] > 0.1f) ? 1 : 0);

                // input.gamepad.getAxisCount()[i] = GLFW_GAMEPAD_AXIS_LAST + 1;
            }
        }

        window.setResizedLastFrame(false);

        if (window.isEventWaiting()) glfwWaitEvents();     // Wait for in input events before continue (drawing is paused)
        else glfwPollEvents();      // Poll input events: keyboard/mouse/window events (callbacks) -> Update keys state

        // While window minimized, stop loop execution
        while (context.core.IsWindowState(FLAG_WINDOW_MINIMIZED) && !context.core.IsWindowState(FLAG_WINDOW_ALWAYS_RUN)) {
            glfwWaitEvents();
        }

        window.setShouldClose(glfwWindowShouldClose(window.handle));

        // Reset close status for next frame
        glfwSetWindowShouldClose(window.handle, false);
    }

    // Close platform
    public void ClosePlatform() {
        glfwDestroyWindow(window.handle);
        glfwTerminate();
    }

}
