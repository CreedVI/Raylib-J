package com.raylib.java.core.platforms;

import com.raylib.java.Raylib;
import com.raylib.java.core.Window;
import com.raylib.java.core.callback.Callbacks;
import com.raylib.java.core.callback.ErrorCallback;
import com.raylib.java.core.input.*;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Point;
import com.raylib.java.structs.Size;
import com.raylib.java.structs.Vector2;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWAllocator;
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
        if (window.isReady()) {
            return window.isShouldClose();
        }
        else {
            return true;
        }
    }

    // Toggle fullscreen mode
    public void ToggleFullscreen() {
        if (!FLAG_IS_SET(window.flags, FLAG_FULLSCREEN_MODE)) {
            // Store previous window position (in case we exit fullscreen)
            Vector2 windowPositionVector = GetWindowPosition();
            window.getPosition().setX(windowPositionVector.x);
            window.getPosition().setY(windowPositionVector.y);

            int monitorCount;
            PointerBuffer monitors = glfwGetMonitors();
            monitorCount = monitors.sizeof();
            int monitorIndex = GetCurrentMonitor();
            long monitor = (monitorIndex < monitorCount) ? monitors.get(monitorIndex) : -1;

            if (monitor >= 0) {
                GLFWVidMode mode = glfwGetVideoMode(monitor);
                window.getDisplay().width = mode.width();
                window.getDisplay().height = mode.height();

                window.setPosition(new Point());
                window.setScreen(window.getDisplay());


                // Set fullscreen flag to be processed on FramebufferSizeCallback() accordingly
                FLAG_SET(window.flags, FLAG_FULLSCREEN_MODE);

                if (_GLFW_X11 || _GLFW_WAYLAND) {
                    glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_FALSE);
                    FLAG_SET(window.flags, FLAG_WINDOW_UNDECORATED);
                }

                glfwSetWindowMonitor(
                        window.handle,
                        monitor,
                        0,
                        0,
                        window.getScreen().getWidth(),
                        window.getScreen().getHeight(),
                        GLFW_DONT_CARE
                );
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to get monitor");
            }
        }
        else {

            window.setPosition(window.getPreviousPosition());
            window.setScreen(window.getPreviousScreen());

            FLAG_CLEAR(window.flags, FLAG_FULLSCREEN_MODE);

            if (!__APPLE__ && !_GLFW_WAYLAND) {
                // Make sure to restore render size considering HighDPI scaling
                // NOTE: On Wayland, GLFW_SCALE_FRAMEBUFFER handles scaling, skip manual resize
                if ((window.flags & FLAG_WINDOW_HIGHDPI) > 0) {
                    Vector2 scaleDpi = GetWindowScaleDPI();
                    window.getScreen().width = (int) (window.getScreen().width * scaleDpi.x);
                    window.getScreen().height = (int) (window.getScreen().height * scaleDpi.y);
                }
            }

            // WARNING: This function launches FramebufferSizeCallback()
            glfwSetWindowMonitor(
                    context.core.GetPlatform().GetWindowHandle(),
                    0L,
                    (int) window.getPosition().x,
                    (int) window.getPosition().y,
                    window.getScreen().width,
                    window.getScreen().height,
                    GLFW_DONT_CARE
            );

            if (_GLFW_X11 || _GLFW_WAYLAND) {
                // NOTE: X11 requires restoring the decorated window after switching from
                // fullscreen to avoid issues with framebuffer scaling
                glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_TRUE);
                FLAG_CLEAR(window.flags, FLAG_WINDOW_UNDECORATED);
            }
        }

        // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
        // NOTE: V-Sync can be enabled by graphic driver configuration
        if (FLAG_IS_SET(window.flags, FLAG_VSYNC_HINT)) {
            glfwSwapInterval(1);
        }
    }

    // Toggle borderless windowed mode
    public void ToggleBorderlessWindowed() {
        // Leave fullscreen before attempting to set borderless windowed mode
        // NOTE: Fullscreen already saves the previous position so it does not need to be set again later
        if (FLAG_IS_SET(window.flags, FLAG_FULLSCREEN_MODE)) {
            ToggleFullscreen();
        }

        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();
        int monitorIndex = GetCurrentMonitor();
        long monitor = (monitorIndex < monitorCount) ? monitors.get(monitorIndex) : -1;

        if ((monitor >= 0) && (monitor < monitorCount)) {
            GLFWVidMode mode = glfwGetVideoMode(monitor);

            if (mode != null) {
                if (!FLAG_IS_SET(window.flags, FLAG_BORDERLESS_WINDOWED_MODE)) {
                    // Store screen position and size
                    // NOTE: If it was on fullscreen, screen position was already stored, so skip setting it here
                    window.setPreviousPosition(window.getPosition());
                    window.setPreviousScreen(window.getScreen());

                    // Set undecorated flag
                    glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_FALSE);
                    FLAG_SET(window.flags, FLAG_WINDOW_UNDECORATED);

                    // Get monitor position and size
                    IntBuffer x = IntBuffer.allocate(1);
                    IntBuffer y = IntBuffer.allocate(1);
                    glfwGetMonitorPos(monitor, x, y);
                    window.setPosition(new Point(x.get(0), y.get(0)));
                    window.setScreen(new Size(mode.width(), mode.height()));

                    // Set screen position and size
                    glfwSetWindowMonitor(window.handle, monitor, (int) window.getPosition().x, (int) window.getPosition().y, window.getScreen().width, window.getScreen().height, mode.refreshRate());

                    // Refocus window
                    glfwFocusWindow(window.handle);

                    FLAG_SET(window.flags, FLAG_BORDERLESS_WINDOWED_MODE);
                }
                else {
                    // Restore previous screen values
                    window.setPosition(window.getPreviousPosition());
                    window.setScreen(window.getPreviousScreen());

                    // Remove undecorated flag
                    glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_TRUE);
                    FLAG_CLEAR(window.flags, FLAG_WINDOW_UNDECORATED);

                    if (!__APPLE__ && !_GLFW_WAYLAND) {
                        // Make sure to restore size considering HighDPI scaling
                        // NOTE: On Wayland, GLFW_SCALE_FRAMEBUFFER handles scaling, skip manual resize
                        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_HIGHDPI)) {
                            Vector2 scaleDpi = GetWindowScaleDPI();
                            window.getScreen().width = (int) (window.getScreen().width * scaleDpi.x);
                            window.getScreen().height = (int) (window.getScreen().height * scaleDpi.y);
                        }
                    }

                    // Return to previous screen size and position
                    glfwSetWindowMonitor(window.handle, 0L, (int) window.getPosition().x, (int) window.getPosition().y,
                                         window.getScreen().width, window.getScreen().height, mode.refreshRate());

                    // Refocus window
                    glfwFocusWindow(window.handle);

                    FLAG_CLEAR(window.flags, FLAG_BORDERLESS_WINDOWED_MODE);
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
        if (((window.flags & FLAG_VSYNC_HINT) != (flags & FLAG_VSYNC_HINT)) && ((flags & FLAG_VSYNC_HINT) > 0)) {
            glfwSwapInterval(1);
            window.flags |= FLAG_VSYNC_HINT;
        }

        // State change: FLAG_FULLSCREEN_MODE
        if ((window.flags & FLAG_FULLSCREEN_MODE) != (flags & FLAG_FULLSCREEN_MODE)) {
            ToggleFullscreen();     // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_RESIZABLE
        if (((window.flags & FLAG_WINDOW_RESIZABLE) != (flags & FLAG_WINDOW_RESIZABLE)) && ((flags & FLAG_WINDOW_RESIZABLE) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_RESIZABLE, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_RESIZABLE;
        }

        // State change: FLAG_WINDOW_UNDECORATED
        if (((window.flags & FLAG_WINDOW_UNDECORATED) != (flags & FLAG_WINDOW_UNDECORATED)) && (flags & FLAG_WINDOW_UNDECORATED) > 0) {
            glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_FALSE);
            window.flags |= FLAG_WINDOW_UNDECORATED;
        }

        // State change: FLAG_WINDOW_HIDDEN
        if (((window.flags & FLAG_WINDOW_HIDDEN) != (flags & FLAG_WINDOW_HIDDEN)) && ((flags & FLAG_WINDOW_HIDDEN) > 0)) {
            glfwHideWindow(window.handle);
            window.flags |= FLAG_WINDOW_HIDDEN;
        }

        // State change: FLAG_WINDOW_MINIMIZED
        if (((window.flags & FLAG_WINDOW_MINIMIZED) != (flags & FLAG_WINDOW_MINIMIZED)) && ((flags & FLAG_WINDOW_MINIMIZED) > 0)) {
            //GLFW_ICONIFIED
            MinimizeWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_MAXIMIZED
        if (((window.flags & FLAG_WINDOW_MAXIMIZED) != (flags & FLAG_WINDOW_MAXIMIZED)) && ((flags & FLAG_WINDOW_MAXIMIZED) > 0)) {
            //GLFW_MAXIMIZED
            MaximizeWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_UNFOCUSED
        if (((window.flags & FLAG_WINDOW_UNFOCUSED) != (flags & FLAG_WINDOW_UNFOCUSED)) && ((flags & FLAG_WINDOW_UNFOCUSED) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
            window.flags |= FLAG_WINDOW_UNFOCUSED;
        }

        // State change: FLAG_WINDOW_TOPMOST
        if (((window.flags & FLAG_WINDOW_TOPMOST) != (flags & FLAG_WINDOW_TOPMOST)) && ((flags & FLAG_WINDOW_TOPMOST) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_TOPMOST;
        }

        // State change: FLAG_WINDOW_ALWAYS_RUN
        if (((window.flags & FLAG_WINDOW_ALWAYS_RUN) != (flags & FLAG_WINDOW_ALWAYS_RUN)) && ((flags & FLAG_WINDOW_ALWAYS_RUN) > 0)) {
            window.flags |= FLAG_WINDOW_ALWAYS_RUN;
        }

        // The following states can not be changed after window creation

        // State change: FLAG_WINDOW_TRANSPARENT
        if (((window.flags & FLAG_WINDOW_TRANSPARENT) != (flags & FLAG_WINDOW_TRANSPARENT)) && ((flags & FLAG_WINDOW_TRANSPARENT) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window " +
                    "initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI) != (flags & FLAG_WINDOW_HIGHDPI)) && ((flags & FLAG_WINDOW_HIGHDPI) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_MOUSE_PASSTHROUGH
        if (((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) != (flags & FLAG_WINDOW_MOUSE_PASSTHROUGH)) && ((flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_MOUSE_PASSTHROUGH;
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT) != (flags & FLAG_MSAA_4X_HINT)) && ((flags & FLAG_MSAA_4X_HINT) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT) != (flags & FLAG_INTERLACED_HINT)) && ((flags & FLAG_INTERLACED_HINT) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Clear window configuration state flags
    public void ClearWindowState(int flags) {
        // Check previous state and requested state to apply required changes
        // NOTE: In most cases the functions already change the flags internally

        // State change: FLAG_VSYNC_HINT
        if (((window.flags & FLAG_VSYNC_HINT) > 0) && ((flags & FLAG_VSYNC_HINT) > 0)) {
            glfwSwapInterval(0);
            window.flags &= ~FLAG_VSYNC_HINT;
        }

        // State change: FLAG_FULLSCREEN_MODE
        if (((window.flags & FLAG_FULLSCREEN_MODE) > 0) && ((flags & FLAG_FULLSCREEN_MODE) > 0)) {
            ToggleFullscreen();     // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_RESIZABLE
        if (((window.flags & FLAG_WINDOW_RESIZABLE) > 0) && ((flags & FLAG_WINDOW_RESIZABLE) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_RESIZABLE, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_RESIZABLE;
        }

        // State change: FLAG_WINDOW_UNDECORATED
        if (((window.flags & FLAG_WINDOW_UNDECORATED) > 0) && ((flags & FLAG_WINDOW_UNDECORATED) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_TRUE);
            window.flags &= ~FLAG_WINDOW_UNDECORATED;
        }

        // State change: FLAG_WINDOW_HIDDEN
        if (((window.flags & FLAG_WINDOW_HIDDEN) > 0) && ((flags & FLAG_WINDOW_HIDDEN) > 0)) {
            glfwShowWindow(window.handle);
            window.flags &= ~FLAG_WINDOW_HIDDEN;
        }

        // State change: FLAG_WINDOW_MINIMIZED
        if (((window.flags & FLAG_WINDOW_MINIMIZED) > 0) && ((flags & FLAG_WINDOW_MINIMIZED) > 0)) {
            RestoreWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_MAXIMIZED
        if (((window.flags & FLAG_WINDOW_MAXIMIZED) > 0) && ((flags & FLAG_WINDOW_MAXIMIZED) > 0)) {
            RestoreWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_UNFOCUSED
        if (((window.flags & FLAG_WINDOW_UNFOCUSED) > 0) && ((flags & FLAG_WINDOW_UNFOCUSED) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
            window.flags &= ~FLAG_WINDOW_UNFOCUSED;
        }

        // State change: FLAG_WINDOW_TOPMOST
        if (((window.flags & FLAG_WINDOW_TOPMOST) > 0) && ((flags & FLAG_WINDOW_TOPMOST) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_TOPMOST;
        }

        // State change: FLAG_WINDOW_ALWAYS_RUN
        if (((window.flags & FLAG_WINDOW_ALWAYS_RUN) > 0) && ((flags & FLAG_WINDOW_ALWAYS_RUN) > 0)) {
            window.flags &= ~FLAG_WINDOW_ALWAYS_RUN;
        }

        // The following states can not be changed after window creation

        // State change: FLAG_WINDOW_TRANSPARENT
        if (((window.flags & FLAG_WINDOW_TRANSPARENT) > 0) && ((flags & FLAG_WINDOW_TRANSPARENT) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI) > 0) && ((flags & FLAG_WINDOW_HIGHDPI) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_MOUSE_PASSTHROUGH
        if (((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0) && ((flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_MOUSE_PASSTHROUGH, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_MOUSE_PASSTHROUGH;
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT) > 0) && ((flags & FLAG_MSAA_4X_HINT) > 0)) {
            context.tracelog.TRACELOG(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT) > 0) && ((flags & FLAG_INTERLACED_HINT) > 0)) {
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
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: window icon image must be in R8G8B8A8 pixel format");
        }
    }

    // Set icon for window, multiple images
    // NOTE 1: Images must be in RGBA format, 8bit per channel
    // NOTE 2: The multiple images are used depending on provided sizes
    // Standard Windows icon sizes: 256, 128, 96, 64, 48, 32, 24, 16
    public void SetWindowIcons(List<Image> images) {
        int count = images.size();

        if (images.isEmpty()) {
            glfwSetWindowIcon(window.handle, null);
        }
        else {
            GLFWImage.Buffer icons = GLFWImage.malloc(count);

            for (int i = 0; i < count; i++) {
                Image image = images.get(i);

                if (image.getFormat() == PIXELFORMAT_UNCOMPRESSED_R8G8B8A8) {
                    icons.get(i).set(
                            image.width,
                            image.height,
                            ByteBuffer.wrap(image.getData())
                    );
                }
                else {
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

        if ((monitor >= 0) && (monitor < monitorCount)) {
            context.tracelog.TRACELOG(LOG_INFO, "GLFW: Selected fullscreen monitor: [" + monitor + "] " + glfwGetMonitorName(monitor));

            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window.handle, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
    }

    // Set window minimum dimensions (FLAG_WINDOW_RESIZABLE)
    public void SetWindowMinSize(int width, int height) {
        window.getScreenMin().width = width;
        window.getScreenMin().height = height;

        int minWidth = (window.getScreenMin().width == 0) ? GLFW_DONT_CARE : window.getScreenMin().width;
        int minHeight = (window.getScreenMin().height == 0) ? GLFW_DONT_CARE : window.getScreenMin().height;
        int maxWidth = (window.getScreenMax().width == 0) ? GLFW_DONT_CARE : window.getScreenMax().width;
        int maxHeight = (window.getScreenMax().height == 0) ? GLFW_DONT_CARE : window.getScreenMax().height;

        glfwSetWindowSizeLimits(window.handle, minWidth, minHeight, maxWidth, maxHeight);
    }

    // Set window maximum dimensions (FLAG_WINDOW_RESIZABLE)
    public void SetWindowMaxSize(int width, int height) {
        window.getScreenMax().width = width;
        window.getScreenMax().height = height;

        int minWidth = (window.getScreenMin().width == 0) ? GLFW_DONT_CARE : window.getScreenMin().width;
        int minHeight = (window.getScreenMin().height == 0) ? GLFW_DONT_CARE : window.getScreenMin().height;
        int maxWidth = (window.getScreenMax().width == 0) ? GLFW_DONT_CARE : window.getScreenMax().width;
        int maxHeight = (window.getScreenMax().height == 0) ? GLFW_DONT_CARE : window.getScreenMax().height;

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

        if ((monitor >= 0) && (monitor < monitorCount)) {
            IntBuffer x = IntBuffer.allocate(1);
            IntBuffer y = IntBuffer.allocate(1);
            glfwGetMonitorPos(monitor, x, y);
            return new Vector2(x.get(0), y.get(0));
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }

        return new Vector2();
    }

    // Get selected monitor width (currently used by monitor)
    public int GetMonitorWidth(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)) {
            GLFWVidMode.Buffer modes = glfwGetVideoModes(monitors.get(monitor));

            // We return the maximum resolution available, the last one in the modes array
            if (modes.sizeof() > 0) {
                return modes.width();
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor height (currently used by monitor)
    public int GetMonitorHeight(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)) {
            GLFWVidMode.Buffer modes = glfwGetVideoModes(monitors.get(monitor));

            // We return the maximum resolution available, the last one in the modes array
            if (modes.sizeof() > 0) {
                return modes.height();
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else {
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

        if ((monitor >= 0) && (monitor < monitorCount)) {
            int physicalWidth = 0;
            nglfwGetMonitorPhysicalSize(monitor, physicalWidth, 0);
            return physicalWidth;
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor physical height in millimetres
    public int GetMonitorPhysicalHeight(int monitor) {
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)) {
            int physicalHeight = 0;
            nglfwGetMonitorPhysicalSize(monitor, 0, physicalHeight);
            return physicalHeight;
        }
        else {
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

        if ((monitor >= 0) && (monitor < monitorCount)) {
            return glfwGetMonitorName(monitor);
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return "";
    }

    // Get window position XY on monitor
    public Vector2 GetWindowPosition() {
        // Memory-safe get window position
        try (MemoryStack stack = stackPush()) {
            IntBuffer xBuffer = stack.mallocInt(1);
            IntBuffer yBuffer = stack.mallocInt(1);
            if (PLATFORM_DESKTOP) {
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
        Vector2 scale = new Vector2(1f, 1f);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer xDpi = stack.mallocFloat(1);
            FloatBuffer yDpi = stack.mallocFloat(1);
            Vector2 windowPos = GetWindowPosition();

            PointerBuffer monitors = glfwGetMonitors();
            int monitorCount = monitors.sizeof();

            // Check window monitor
            for (int i = 0; i < monitorCount; i++) {
                glfwGetMonitorContentScale(monitors.get(i), xDpi, yDpi);

                IntBuffer xPos, yPos, width, height;
                xPos = stack.mallocInt(1);
                yPos = stack.mallocInt(1);
                width = stack.mallocInt(1);
                height = stack.mallocInt(1);

                glfwGetMonitorWorkarea(monitors.get(i), xPos, yPos, width, height);

                if ((windowPos.x >= xPos.get(0)) && (windowPos.x < xPos.get(0) + width.get(0)) &&
                        (windowPos.y >= yPos.get(0)) && (windowPos.y < yPos.get(0) + height.get(0))) {
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
    public void HideCursor() {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        input.getMouse().cursorHidden = true;
    }

    // Enables cursor (unlock cursor)
    public void EnableCursor() {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        // Set cursor position in the middle
        SetMousePosition(window.getScreen().width / 2, window.getScreen().height / 2);

        input.getMouse().cursorHidden = false;
    }

    // Disables cursor (lock cursor)
    public void DisableCursor() {
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Set cursor position in the middle
        SetMousePosition(window.getScreen().width / 2, window.getScreen().height / 2);

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
    public boolean SetgamepadMappings(byte[] mappings) {
        boolean result = false;

        ByteBuffer mappingsBuffer = ByteBuffer.allocateDirect(mappings.length);
        mappingsBuffer.put(mappings).flip();

        if (PLATFORM_DESKTOP) {
            result = glfwUpdateGamepadMappings(mappingsBuffer);
        }

        return result;
    }

    // Set mouse position XY
    public void SetMousePosition(int x, int y) {
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

        // GLFWAllocator allocator = new GLFWAllocator(ByteBuffer.allocateDirect(2048));
        // glfwInitAllocator(allocator);

        if (__APPLE__) {
            glfwInitHint(GLFW_COCOA_CHDIR_RESOURCES, GLFW_FALSE);
        }

        if (__LINUX__) {
            String session = System.getenv("XDG_SESSION_TYPE");
            if (session != null && session.equalsIgnoreCase("wayland")) {
                _GLFW_WAYLAND = true;
                _GLFW_X11 = false;
            }
            else {
                _GLFW_WAYLAND = false;
                _GLFW_X11 = true;
            }
        }

        // Initialize GLFW internal global state
        int result = glfwInit() ? 1 : 0;
        if (result == 0) {
            context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to initialize GLFW");
            return -1;
        }

        // Initialize graphic device: display/window and graphic context
        //----------------------------------------------------------------------------
        glfwDefaultWindowHints();                       // Set default windows hints
        //glfwWindowHint(GLFW_RED_BITS, 8);             // Framebuffer red color component bits
        //glfwWindowHint(GLFW_GREEN_BITS, 8);           // Framebuffer green color component bits
        //glfwWindowHint(GLFW_BLUE_BITS, 8);            // Framebuffer blue color component bits
        //glfwWindowHint(GLFW_ALPHA_BITS, 8);           // Framebuffer alpha color component bits
        //glfwWindowHint(GLFW_DEPTH_BITS, 24);          // Depthbuffer bits
        //glfwWindowHint(GLFW_REFRESH_RATE, 0);         // Refresh rate for fullscreen window
        //glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API); // OpenGL API to use. Alternative: GLFW_OPENGL_ES_API
        //glfwWindowHint(GLFW_AUX_BUFFERS, 0);          // Number of auxiliar buffers

        // Disable GlFW auto iconify behaviour
        // Auto Iconify automatically minimizes (iconifies) the window if the window loses focus
        // additionally auto iconify restores the hardware resolution of the monitor if the window that loses focus is a fullscreen window
        glfwWindowHint(GLFW_AUTO_ICONIFY, 0);

        // Window flags requested before initialization to be applied after initialization
        int requestedWindowFlags = window.flags;

        // Check window creation flags
        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_HIDDEN)) {
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Visible window
        }
        else {
            glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);     // Window initially hidden
        }

        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_UNDECORATED)) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE); // Border and buttons on Window
        }
        else {
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);   // Decorated window
        }

        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_RESIZABLE)) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Resizable window
        }
        else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);  // Avoid window being resizable
        }

        // Disable FLAG_WINDOW_MINIMIZED, not supported on initialization
        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_MINIMIZED)) {
            FLAG_CLEAR(window.flags, FLAG_WINDOW_MINIMIZED);
        }

        // Disable FLAG_WINDOW_MAXIMIZED, not supported on initialization
        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_MAXIMIZED)) {
            FLAG_CLEAR(window.flags, FLAG_WINDOW_MAXIMIZED);
        }

        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_UNFOCUSED)) {
            glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
        }
        else {
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        }

        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_TOPMOST)) {
            glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        }
        else {
            glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        }

        // NOTE: Some GLFW flags are not supported on HTML5
        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_TRANSPARENT)) {
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);     // Transparent framebuffer
        }
        else {
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE);  // Opaque framebuffer
        }

        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_HIGHDPI)) {
            if (__APPLE__) {
                glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_FALSE);
            }

            // Resize window content area based on the monitor content scale
            // NOTE: This hint only has an effect on platforms where screen coordinates and
            // pixels always map 1:1 such as Windows and X11
            // On platforms like macOS the resolution of the framebuffer is changed independently of the window size
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
            if (__APPLE__) {
                glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_TRUE);
            }

        }
        else {
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE);
            if (__APPLE__) {
                glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_FALSE);
            }

            if ((_GLFW_WAYLAND) && (!_GLFW_X11)) {
                // GLFW 3.4+ defaults GLFW_SCALE_FRAMEBUFFER to TRUE,
                // causing framebuffer/window size mismatch on Wayland with display scaling
                glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_FALSE);
            }
        }

        // Mouse passthrough
        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_MOUSE_PASSTHROUGH)) {
            glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
        }
        else {
            glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_FALSE);
        }

        if (FLAG_IS_SET(window.flags, FLAG_MSAA_4X_HINT)) {
            // NOTE: MSAA is only enabled for main framebuffer, not user-created FBOs
            context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Trying to enable MSAA x4");
            glfwWindowHint(GLFW_SAMPLES, 4);   // Tries to enable multisampling x4 (MSAA), default is 0
        }

        // NOTE: When asking for an OpenGL context version, most drivers provide the highest supported version
        // with backward compatibility to older OpenGL versions
        // For example, if using OpenGL 1.1, driver can provide a 4.3 backwards compatible context

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
            if (__APPLE__) {
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
            if (RLGL_ENABLE_OPENGL_DEBUG_CONTEXT) {
                glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);   // Enable OpenGL Debug Context
            }
        }
        else if (context.rlgl.rlGetVersion() == OPENGL_ES_20)                 // Request OpenGL ES 2.0 context
        {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
        }
        else if (context.rlgl.rlGetVersion() == OPENGL_ES_30)                 // Request OpenGL ES 3.0 context
        {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
        }

        // NOTE: GLFW 3.4+ defers initialization of the Joystick subsystem on the first call to any Joystick related functions
        // Forcing this initialization here avoids doing it on PollInputEvents() called by EndDrawing() after first frame has been drawn
        // The initialization will still happen and possible delays still occur, but before the window is shown, which is a nicer experience
        // REF: https://github.com/raysan5/raylib/issues/1554
        glfwSetJoystickCallback(null);

        if ((window.getScreen().width == 0) || (window.getScreen().height == 0)) {
            FLAG_SET(window.flags, FLAG_FULLSCREEN_MODE);
        }

        // Init window in fullscreen mode if requested
        // NOTE: Keeping original screen size for toggle
        if (FLAG_IS_SET(window.flags, FLAG_FULLSCREEN_MODE)) {
            // NOTE: Fullscreen applications default to the primary monitor
            long monitor = glfwGetPrimaryMonitor();
            if (monitor < 0) {
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to get primary monitor");
                return -1;
            }

            // Set dimensions from monitor
            GLFWVidMode mode = glfwGetVideoMode(monitor);

            // Default display resolution to that of the current mode
            window.getDisplay().width = mode.width();
            window.getDisplay().height = mode.height();

            // Check if user requested some screen size
            if ((window.getScreen().width == 0) || (window.getScreen().height == 0)) {
                // Set some default screen size in case user decides to exit fullscreen mode
                window.getPreviousScreen().width = 800;
                window.getPreviousScreen().height = 450;
                window.getPreviousPosition().x = (float) window.getDisplay().width / 2 - (float) 800 / 2;
                window.getPreviousPosition().y = (float) window.getDisplay().height / 2 - (float) 450 / 2;

                // Set screen width/height to the display width/height
                if (window.getScreen().width == 0) {
                    window.getScreen().width = window.getDisplay().width;
                }
                if (window.getScreen().height == 0) {
                    window.getScreen().height = window.getDisplay().height;
                }
            }
            else {
                window.setPreviousScreen(window.getScreen());
                window.setScreen(window.getDisplay());
            }

            window.handle = glfwCreateWindow(
                    window.getScreen().width,
                    window.getScreen().height,
                    (window.getTitle() != null) ? window.getTitle() : " ",
                    monitor,
                    0L
            );

            if (window.handle <= 0) {
                glfwTerminate();
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to initialize Window");
                return -1;
            }
        }
        else {
            // Default to at least one pixel in size, as creation with a zero dimension is not allowed
            if (window.getScreen().width == 0) {
                window.getScreen().width = 1;
            }
            if (window.getScreen().height == 0) {
                window.getScreen().height = 1;
            }

            window.handle = glfwCreateWindow(
                    window.getScreen().width,
                    window.getScreen().height,
                    (window.getTitle() != null) ? window.getTitle() : " ",
                    0,
                    0
            );

            if (window.handle <= 0) {
                glfwTerminate();
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to initialize Window");
                return -1;
            }

            // After the window was created, determine the monitor that the window manager assigned
            // Derive display sizes and, if possible, window size in case it was zero at beginning

            int monitorIndex = GetCurrentMonitor();
            PointerBuffer monitors = glfwGetMonitors();
            int monitorCount = monitors.capacity();

            if (monitorIndex < monitorCount) {
                long monitor = monitors.get(monitorIndex);
                GLFWVidMode mode = glfwGetVideoMode(monitor);

                // Default display resolution to that of the current mode
                window.getDisplay().width = mode.width();
                window.getDisplay().height = mode.height();

                // Set screen width/height to the display width/height if they are 0
                if (window.getScreen().width == 0) {
                    window.getScreen().width = window.getDisplay().width;
                }
                if (window.getScreen().height == 0) {
                    window.getScreen().height = window.getDisplay().height;
                }

                glfwSetWindowSize(window.handle, window.getScreen().width, window.getScreen().height);
            }
            else {
                // The monitor for the window-manager-created window can not be determined, so it can not be centered
                glfwTerminate();
                context.tracelog.TRACELOG(LOG_WARNING, "GLFW: Failed to determine Monitor to center Window");
                return -1;
            }

            if (__APPLE__) {
                // AppKit can constrain the requested window size to the visible work area during creation
                IntBuffer windowWidth = IntBuffer.allocate(1);
                IntBuffer windowHeight = IntBuffer.allocate(1);
                glfwGetWindowSize(window.handle, windowWidth, windowHeight);
                if ((windowWidth.get(0) > 0) && (windowHeight.get(0) > 0)) {
                    window.setScreen(new Size(windowWidth.get(0), windowHeight.get(0)));
                }
            }

            // NOTE: Not considering scale factor now, considered below
            window.getRender().width = window.getScreen().width;
            window.getRender().height = window.getScreen().height;
        }


        glfwMakeContextCurrent(window.handle);
        GL.createCapabilities();
        result = glfwGetError(null);
        if ((result != GLFW_NO_WINDOW_CONTEXT) && (result != GLFW_PLATFORM_ERROR)) {
            window.setReady(true); // Checking context activation
        }

        if (window.isReady()) {
            // Setup additional windows configs and register required window size info

            glfwSwapInterval(0); // No V-Sync by default

            // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
            // NOTE: V-Sync can be enabled by graphic driver configuration, it doesn't need
            // to be activated on web platforms since VSync is enforced there
            if (FLAG_IS_SET(window.flags, FLAG_VSYNC_HINT)) {
                // WARNING: It seems to hit a critical render path in Intel HD Graphics
                glfwSwapInterval(1);
                context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Trying to enable VSYNC");
            }

            if (FLAG_IS_SET(window.flags, FLAG_WINDOW_HIGHDPI)) {
                // Set screen size to logical pixel size, considering content scaling
                Vector2 scaleDpi = GetWindowScaleDPI();
                window.getRender().width = (int) (window.getScreen().width * scaleDpi.x);
                window.getRender().height = (int) (window.getScreen().height * scaleDpi.y);
                //TRACELOG(LOG_INFO, "DPI SCALING: %.2f, %.2f", scaleDpi.x, scaleDpi.y);

                // Screen scaling matrix is required in case desired screen area is different from display area
                window.setScreenScale(MatrixScale(scaleDpi.x, scaleDpi.y, 1.0f));

                // NOTE: On APPLE platforms system manage window and input scaling
                // Framebuffer scaling is activated with: glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_TRUE);

                if (!__APPLE__) {
                    if (glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) {
                        // On Wayland, GLFW_SCALE_FRAMEBUFFER handles scaling; read actual framebuffer size
                        // instead of resizing the window (which would double-scale)
                        IntBuffer fbWidth = IntBuffer.allocate(1);
                        IntBuffer fbHeight = IntBuffer.allocate(1);
                        glfwGetFramebufferSize(window.handle, fbWidth, fbHeight);

                        window.getRender().width = fbWidth.get(0);
                        window.getRender().height = fbHeight.get(0);
                    }
                    else {
                        // Mouse input scaling for the new screen size
                        context.core.SetMouseScale(1.0f / scaleDpi.x, 1.0f / scaleDpi.y);

                        // Force window size (and framebuffer) refresh
                        glfwSetWindowSize(window.handle, window.getRender().width, window.getRender().height);
                    }
                }
            }
            else {
                window.setRender(window.getScreen().clone());
            }

            // Current active framebuffer size is main framebuffer size
            window.setCurrentFbo(window.getRender().clone());

            context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Device initialized successfully %s", FLAG_IS_SET(window.flags, FLAG_WINDOW_HIGHDPI) ? "(HighDPI)" : "");
            context.tracelog.TRACELOG(LOG_INFO, "    > Display size: %d x %d", window.getDisplay().width, window.getDisplay().height);
            context.tracelog.TRACELOG(LOG_INFO, "    > Screen size:  %d x %d", window.getScreen().width, window.getScreen().height);
            context.tracelog.TRACELOG(LOG_INFO, "    > Render size:  %d x %d", window.getRender().width, window.getRender().height);
            context.tracelog.TRACELOG(LOG_INFO, "    > Viewport offsets: %02f, %02f", window.getRenderOffset().x, window.getRenderOffset().y);
            //TRACELOG(LOG_INFO, "    > Content Scaling: %.2f, %.2f", scaleDpi.x, scaleDpi.y);

            // Try to center window on screen but avoiding window-bar outside of screen
            int monitorIndex = GetCurrentMonitor();
            PointerBuffer monitors = glfwGetMonitors();
            long monitor = monitors.get(monitorIndex);

            IntBuffer monitorX = IntBuffer.allocate(1);
            IntBuffer monitorY = IntBuffer.allocate(1);
            IntBuffer monitorWidth = IntBuffer.allocate(1);
            IntBuffer monitorHeight = IntBuffer.allocate(1);
            // glfwGetMonitorWorkarea(monitor, monitorX, monitorY, monitorWidth, monitorHeight);

            // NOTE: It seems on macOS monitor size is not correct
            //TRACELOG(LOG_WARNING, "Monitor info: [%d, %d, %d, %d]", monitorX, monitorY, monitorWidth, monitorHeight);

            // Center window into current monitor
            if (__APPLE__) {
                window.getPosition().x = monitorX.get(0) + ((float) (monitorWidth.get(0) - window.getScreen().width) / 2);
                window.getPosition().y = monitorY.get(0) + ((float) (monitorHeight.get(0) - window.getScreen().height) / 2);
            }
            else {
                window.getPosition().x = monitorX.get(0) + (float) (monitorWidth.get(0) - window.getRender().width) / 2;
                window.getPosition().y = monitorY.get(0) + (float) (monitorHeight.get(0) - window.getRender().height) / 2;
            }
            SetWindowPosition((int) window.getPosition().x, (int) window.getPosition().y);

            if (FLAG_IS_SET(window.flags, FLAG_WINDOW_MINIMIZED)) {
                MinimizeWindow();
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_FATAL, "PLATFORM: Failed to initialize graphics device");
            return -1;
        }

        // Apply window flags requested previous to initialization
        SetWindowState(requestedWindowFlags);

        // Load OpenGL extensions
        // NOTE: GL procedures address loader is required to load extensions
        context.rlgl.rlLoadExtensions();
        //----------------------------------------------------------------------------

        // Initialize input events callbacks
        //----------------------------------------------------------------------------
        // Set window callback events
        glfwSetWindowSizeCallback(window.handle, callbacks.windowSizeCallback); // NOTE: Resizing is not enabled by default
        glfwSetFramebufferSizeCallback(window.handle, callbacks.frameBufferSizeCallback);
        glfwSetWindowPosCallback(window.handle, callbacks.windowPositionCallback);
        glfwSetWindowMaximizeCallback(window.handle, callbacks.windowMaximizeCallback);
        glfwSetWindowIconifyCallback(window.handle, callbacks.windowIconifyCallback);
        glfwSetWindowFocusCallback(window.handle, callbacks.windowFocusCallback);
        glfwSetDropCallback(window.handle, callbacks.windowDropCallback);
        if (FLAG_IS_SET(window.flags, FLAG_WINDOW_HIGHDPI)) {
            glfwSetWindowContentScaleCallback(window.handle, callbacks.windowContentScaleCallback);
        }

        // Set input callback events
        glfwSetKeyCallback(window.handle, callbacks.keyCallback);
        glfwSetCharCallback(window.handle, callbacks.charCallback);
        glfwSetMouseButtonCallback(window.handle, callbacks.mouseButtonCallback);
        glfwSetCursorPosCallback(window.handle, callbacks.mouseCursorPosCallback); // Track mouse position changes
        glfwSetScrollCallback(window.handle, callbacks.mouseScrollCallback);
        glfwSetCursorEnterCallback(window.handle, callbacks.cursorEnterCallback);
        glfwSetJoystickCallback(callbacks.joystickCallback);
        glfwSetInputMode(window.handle, GLFW_LOCK_KEY_MODS, GLFW_TRUE); // Enable lock keys modifiers (CAPS, NUM)

        // Retrieve gamepad names
        for (int i = 0; i < MAX_GAMEPADS; i++) {
            // WARNING: If glfwGetJoystickName() is longer than MAX_GAMEPAD_NAME_LENGTH,
            // only copying up to (MAX_GAMEPAD_NAME_LENGTH - 1)
            if (glfwJoystickPresent(i)) {
                context.core.input.gamepad.ready[i] = true;
                context.core.input.gamepad.setAxisCount(GLFW_GAMEPAD_AXIS_LAST + 1);
                context.core.input.gamepad.name[i] = glfwGetJoystickName(i);
            }
        }
        //----------------------------------------------------------------------------

        // Initialize timing system
        //----------------------------------------------------------------------------
        context.core.InitTimer();
        //----------------------------------------------------------------------------

        // Initialize storage system
        //----------------------------------------------------------------------------
        // CORE.Storage.basePath = GetWorkingDirectory();
        //----------------------------------------------------------------------------

        String glfwPlatform = "";
        switch (glfwGetPlatform()) {
            case GLFW_PLATFORM_WIN32:
                glfwPlatform = "Win32";
                break;
            case GLFW_PLATFORM_COCOA:
                glfwPlatform = "Cocoa";
                break;
            case GLFW_PLATFORM_WAYLAND:
                glfwPlatform = "Wayland";
                break;
            case GLFW_PLATFORM_X11:
                glfwPlatform = "X11";
                break;
            case GLFW_PLATFORM_NULL:
                glfwPlatform = "Null";
                break;
            default:
                break;
        }

        context.tracelog.TRACELOG(LOG_INFO, "PLATFORM: DESKTOP (GLFW - %s): Initialized successfully", glfwPlatform);

        return 0;
    }

    /**
     * Poll (store) all input events
     */
    public void PollInputEvents() {
        if (SUPPORT_GESTURES_SYSTEM) {
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
        for (int i = 0; i < MAX_KEYBOARD_KEYS; i++) {
            input.keyboard.previousKeyState[i] = input.keyboard.currentKeyState[i];
            input.keyboard.keyRepeatInFrame[i] = false;
        }

        // Register previous mouse states
        for (int i = 0; i < MAX_MOUSE_BUTTONS; i++) {
            input.mouse.previousButtonState[i] = input.mouse.currentButtonState[i];
        }

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
                        case GLFW_GAMEPAD_BUTTON_Y:
                            button = GAMEPAD_BUTTON_RIGHT_FACE_UP;
                            break;
                        case GLFW_GAMEPAD_BUTTON_B:
                            button = GAMEPAD_BUTTON_RIGHT_FACE_RIGHT;
                            break;
                        case GLFW_GAMEPAD_BUTTON_A:
                            button = GAMEPAD_BUTTON_RIGHT_FACE_DOWN;
                            break;
                        case GLFW_GAMEPAD_BUTTON_X:
                            button = GAMEPAD_BUTTON_RIGHT_FACE_LEFT;
                            break;

                        case GLFW_GAMEPAD_BUTTON_LEFT_BUMPER:
                            button = GAMEPAD_BUTTON_LEFT_TRIGGER_1;
                            break;
                        case GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER:
                            button = GAMEPAD_BUTTON_RIGHT_TRIGGER_1;
                            break;

                        case GLFW_GAMEPAD_BUTTON_BACK:
                            button = GAMEPAD_BUTTON_MIDDLE_LEFT;
                            break;
                        case GLFW_GAMEPAD_BUTTON_GUIDE:
                            button = GAMEPAD_BUTTON_MIDDLE;
                            break;
                        case GLFW_GAMEPAD_BUTTON_START:
                            button = GAMEPAD_BUTTON_MIDDLE_RIGHT;
                            break;

                        case GLFW_GAMEPAD_BUTTON_DPAD_UP:
                            button = GAMEPAD_BUTTON_LEFT_FACE_UP;
                            break;
                        case GLFW_GAMEPAD_BUTTON_DPAD_RIGHT:
                            button = GAMEPAD_BUTTON_LEFT_FACE_RIGHT;
                            break;
                        case GLFW_GAMEPAD_BUTTON_DPAD_DOWN:
                            button = GAMEPAD_BUTTON_LEFT_FACE_DOWN;
                            break;
                        case GLFW_GAMEPAD_BUTTON_DPAD_LEFT:
                            button = GAMEPAD_BUTTON_LEFT_FACE_LEFT;
                            break;

                        case GLFW_GAMEPAD_BUTTON_LEFT_THUMB:
                            button = GAMEPAD_BUTTON_LEFT_THUMB;
                            break;
                        case GLFW_GAMEPAD_BUTTON_RIGHT_THUMB:
                            button = GAMEPAD_BUTTON_RIGHT_THUMB;
                            break;
                        default:
                            break;
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

        if (window.isEventWaiting()) {
            glfwWaitEvents();     // Wait for in input events before continue (drawing is paused)
        }
        else {
            glfwPollEvents();      // Poll input events: keyboard/mouse/window events (callbacks) -> Update keys state
        }

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
