package com.creedvi.raylib.java.rlj.core;

import com.creedvi.raylib.java.rlj.Config;
import com.creedvi.raylib.java.rlj.core.camera.Camera2D;
import com.creedvi.raylib.java.rlj.core.camera.Camera3D;
import com.creedvi.raylib.java.rlj.core.input.Input;
import com.creedvi.raylib.java.rlj.core.input.Keyboard;
import com.creedvi.raylib.java.rlj.core.input.Mouse;
import com.creedvi.raylib.java.rlj.core.ray.Ray;
import com.creedvi.raylib.java.rlj.raymath.Matrix;
import com.creedvi.raylib.java.rlj.raymath.Quaternion;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.raymath.Vector3;
import com.creedvi.raylib.java.rlj.rlgl.RLGL;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;
import com.creedvi.raylib.java.rlj.text.Text;
import com.creedvi.raylib.java.rlj.textures.Image;
import com.creedvi.raylib.java.rlj.textures.RenderTexture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import java.io.File;
import java.util.Arrays;

import static com.creedvi.raylib.java.rlj.Config.*;
import static com.creedvi.raylib.java.rlj.Config.ConfigFlag.*;
import static com.creedvi.raylib.java.rlj.core.Color.RAYWHITE;
import static com.creedvi.raylib.java.rlj.core.camera.Camera.CameraProjection.*;
import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KeyboardKey.KEY_ESCAPE;
import static com.creedvi.raylib.java.rlj.core.input.Mouse.MouseCursor.*;
import static com.creedvi.raylib.java.rlj.raymath.RayMath.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.GlVersion.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.PixelFormat.UNCOMPRESSED_R8G8B8A8;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;
import static com.creedvi.raylib.java.rlj.rlgl.RLGL.TextureFilterMode.FILTER_BILINEAR;
import static com.creedvi.raylib.java.rlj.text.Text.GetFontDefault;
import static com.creedvi.raylib.java.rlj.textures.Textures.SetTextureFilter;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Core{

    public RLGL rlgl;

    static Window window;
    static Input input;
    static Time time;
    private Callbacks.KeyCallback keyCallback;
    private Callbacks.CharCallback charCallback;
    private Callbacks.MouseButtonCallback mouseBtnCallback;
    private Callbacks.MouseCursorPosCallback cursorPosCallback;
    private Callbacks.MouseScrollCallback scrollCallback;
    private Callbacks.CursorEnterCallback cursorEnterCallback;
    private Callbacks.WindowSizeCallback sizeCallback;
    private Callbacks.WindowMaximizeCallback maximizeCallback;
    private Callbacks.WindowIconifyCallback iconifyCallback;
    private Callbacks.WindowFocusCallback focusCallback;
    private Callbacks.WindowDropCallback dropCallback;
    private Callbacks.ErrorCallback errorCallback;

    public Core(){
        window = new Window();
        input = new Input();
        time = new Time();
        rlgl = new RLGL();
    }

    static Window getWindow(){
        return window;
    }

    public static Input getInput(){
        return input;
    }

    //ANDROID
    //extern main
    //android_main
    //GetAndroidApp
    //END ANDROID

    //RPI | DRM
    //InitTerminal
    //RestoreTerminal
    //END RPI | DRM

    /**
     * Initialize window and OpenGL context.
     * NOTE: data parameter could be used to pass any kind of required data to the initialization
     * @param width Window width in pixels
     * @param height Window height in pixels
     * @param title Window title
     */
    public void InitWindow(int width, int height, String title){
        Tracelog(LOG_INFO, "Initializing raylib " + RAYLIB_VERSION);

        if ((title != null) && (title.charAt(0) != 0)){
            window.setTitle(title);
        }

        // Initialize required global values different than 0
        input.keyboard.setExitKey(input.keyboard.getKeyboardKey(KEY_ESCAPE));
        input.mouse.setScale(new Vector2(1.0f, 1.0f));
        input.mouse.setCursor(input.mouse.getMouseCursor(MOUSE_CURSOR_ARROW));
        input.gamepad.setLastButtonPressed(-1);

        // Init graphics device (display device and OpenGL context)
        // NOTE: returns true if window and graphic device has been initialized successfully
        window.ready = InitGraphicsDevice(width, height);

        if (!window.ready){
            return;
        }

        // Init hi-res timer
        InitTimer();

        if (SUPPORT_DEFAULT_FONT){
            // Load default font
            // NOTE: External function (defined in module: text)
            Text.LoadFontDefault();
            Rectangle rec = GetFontDefault().getRecs()[95];
            // NOTE: We setup a 1px padding on char rectangle to avoid pixel bleeding on MSAA filtering
            RLGL.SetShapesTexture(GetFontDefault().getTexture(), new Rectangle(rec.getX() + 1, rec.getY() + 1,
                    rec.getWidth() - 2, rec.getHeight() - 2));
        }

        if ((window.getFlags() & FLAG_WINDOW_HIGHDPI.getFlag()) > 0){
            // Set default font texture filter for HighDPI (blurry)
            SetTextureFilter(GetFontDefault().getTexture(), FILTER_BILINEAR.getTextureFilterInt());
        }
;
        glfwShowWindow(window.handle);
    }

    /**
     * Close window and unload OpenGL context
     */
    public void CloseWindow(){
        if (SUPPORT_DEFAULT_FONT){
            Text.UnloadFontDefault();
        }

        RLGL.rlglClose();                // De-init rlgl

        glfwSetWindowShouldClose(window.handle, true);
        glfwFreeCallbacks(window.handle);
        glfwDestroyWindow(window.handle);
        glfwTerminate();

        Tracelog(LOG_INFO, "Window closed successfully");
    }

    /**
     * Check if KEY_ESCAPE pressed or close icon pressed
     * @return <code>true</code> if window is ready to close.
     *      NOTE: Must be inverted for use in a while loop
     */
    public boolean WindowShouldClose(){
        if (window.isReady()){
            // While window minimized, stop loop execution
            while (IsWindowState(FLAG_WINDOW_MINIMIZED.getFlag()) && !IsWindowState(FLAG_WINDOW_ALWAYS_RUN.getFlag())){
                glfwWaitEvents();
            }

            window.setShouldClose(glfwWindowShouldClose(window.handle));

            // Reset close status for next frame
            //glfwSetWindowShouldClose(window.handle, GLFW_FALSE);
            glfwSetWindowShouldClose(window.handle, false);
            return window.isShouldClose();
        }
        else{
            CloseWindow();

            return true;
        }
    }

    /**
     * Check if window has been initialized successfully
     * @return <code>true</code> if window was initialized successfully
     */
    public boolean IsWindowReady(){
        return window.ready;
    }

    /**
     * Check if window is currently fullscreen
     * @return <code>true</code> if window is fullscreen
     */
    public static boolean IsWindowFullscreen(){
        return window.fullscreen;
    }

    /**
     * Check if window is currently hidden
     * @return <code>true</code> if window is hidden
     */
    public boolean IsWindowHidden(){
        return ((window.flags & FLAG_WINDOW_HIDDEN.getFlag()) > 0);
    }

    // Check if window has been minimized
    public boolean IsWindowMinimized(){
        return ((window.flags & FLAG_WINDOW_MINIMIZED.getFlag()) > 0);
    }

    // Check if window has been maximized (only PLATFORM_DESKTOP)
    public boolean IsWindowMaximized(){
        return ((window.flags & FLAG_WINDOW_MAXIMIZED.getFlag()) > 0);
    }

    // Check if window has the focus
    public boolean IsWindowFocused(){
        return ((window.flags & FLAG_WINDOW_UNFOCUSED.getFlag()) == 0);      // TODO!
    }

    // Check if window has been resizedLastFrame
    public boolean IsWindowResized(){
        return window.resizedLastFrame;
    }

    /**
     * Check if one specific window flag is enabled
     * @param flag Window flag to be checked
     * @return <code>true</code> if flag is enabled
     */
    public boolean IsWindowState(int flag){
        return ((window.flags & flag) > 0);
    }

    /**
     * Toggle fullscreen mode (only PLATFORM_DESKTOP)
     */
    public void ToggleFullscreen(){
        // NOTE: glfwSetWindowMonitor() doesn't work properly (bugs)
        if (!window.fullscreen){
            // Store previous window position (in case we exit fullscreen)
            nglfwGetWindowPos(window.handle, (long) window.position.x, (long) window.position.y);

            long monitor = glfwGetWindowMonitor(window.handle);
            if (monitor <= 0){
                Tracelog(LOG_WARNING, "GLFW: Failed to get monitor");
                glfwSetWindowSizeCallback(window.handle, sizeCallback = new Callbacks.WindowSizeCallback());
                glfwSetWindowMonitor(window.handle, glfwGetPrimaryMonitor(), 0, 0, window.screen.getWidth(),
                        window.screen.getHeight(), GLFW_DONT_CARE);
                glfwSetWindowSizeCallback(window.handle, new Callbacks.WindowSizeCallback()); // NOTE: Resizing not allowed by default!);
                return;
            }

            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwSetWindowSizeCallback(window.handle, sizeCallback = new Callbacks.WindowSizeCallback());
            glfwSetWindowMonitor(window.handle, monitor, 0, 0, window.screen.getWidth(), window.screen.getHeight(),
                    GLFW_DONT_CARE);
            glfwSetWindowSizeCallback(window.handle, new Callbacks.WindowSizeCallback()); // NOTE: Resizing not allowed by default!);

            // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
            // NOTE: V-Sync can be enabled by graphic driver configuration
            if ((window.flags & FLAG_VSYNC_HINT.getFlag()) == 0){
                glfwSwapInterval(1);
            }
        }
        else{
            glfwSetWindowSizeCallback(window.handle, null);
            glfwSetWindowMonitor(window.handle, 0, (int) window.position.getX(), (int) window.position.getY(),
                    window.screen.getWidth(),
                    window.screen.getHeight(), GLFW_DONT_CARE);
            glfwSetWindowSizeCallback(window.handle, new Callbacks.WindowSizeCallback());
            // NOTE: Resizing not allowed by default!);
        }

        window.fullscreen = !window.fullscreen;          // Toggle fullscreen flag
        window.flags ^= FLAG_FULLSCREEN_MODE.getFlag();

    }

    /**
     * Set window state: maximized, if resizable (only PLATFORM_DESKTOP)
     */
    public void MaximizeWindow(){
        if (glfwGetWindowAttrib(window.handle, GLFW_RESIZABLE) == GLFW_TRUE){
            glfwMaximizeWindow(window.handle);
            window.flags |= FLAG_WINDOW_MAXIMIZED.getFlag();
        }
    }

    /**
     * Set window state: minimized (only PLATFORM_DESKTOP)
     */
    public void MinimizeWindow(){
        // NOTE: Following function launches callback that sets appropiate flag!
        glfwIconifyWindow(window.handle);
    }

    /**
     * Set window state: not minimized/maximized (only PLATFORM_DESKTOP)
     */
    public void RestoreWindow(){
        if (glfwGetWindowAttrib(window.handle, GLFW_RESIZABLE) == GLFW_TRUE){
            // Restores the specified window if it was previously iconified (minimized) or maximized
            glfwRestoreWindow(window.handle);
            window.flags &= ~FLAG_WINDOW_MINIMIZED.getFlag();
            window.flags &= ~FLAG_WINDOW_MAXIMIZED.getFlag();
        }
    }

    // Set window configuration state using flags
    public void SetWindowState(int flags){
        // Check previous state and requested state to apply required changes
        // NOTE: In most cases the functions already change the flags internally

        // State change: FLAG_VSYNC_HINT
        if (((window.flags & FLAG_VSYNC_HINT.getFlag()) != (flags & FLAG_VSYNC_HINT.getFlag())) && ((flags & FLAG_VSYNC_HINT.getFlag()) > 0)){
            glfwSwapInterval(1);
            window.flags |= FLAG_VSYNC_HINT.getFlag();
        }

        // State change: FLAG_FULLSCREEN_MODE
        if ((window.flags & FLAG_FULLSCREEN_MODE.getFlag()) != (flags & FLAG_FULLSCREEN_MODE.getFlag())){
            ToggleFullscreen();     // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_RESIZABLE
        if (((window.flags & FLAG_WINDOW_RESIZABLE.getFlag()) != (flags & FLAG_WINDOW_RESIZABLE.getFlag())) && ((flags & FLAG_WINDOW_RESIZABLE.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_RESIZABLE, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_RESIZABLE.getFlag();
        }

        // State change: FLAG_WINDOW_UNDECORATED
        if (((window.flags & FLAG_WINDOW_UNDECORATED.getFlag()) != (flags & FLAG_WINDOW_UNDECORATED.getFlag())) && (flags & FLAG_WINDOW_UNDECORATED.getFlag()) > 0){
            glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_FALSE);
            window.flags |= FLAG_WINDOW_UNDECORATED.getFlag();
        }

        // State change: FLAG_WINDOW_HIDDEN
        if (((window.flags & FLAG_WINDOW_HIDDEN.getFlag()) != (flags & FLAG_WINDOW_HIDDEN.getFlag())) && ((flags & FLAG_WINDOW_HIDDEN.getFlag()) > 0)){
            glfwHideWindow(window.handle);
            window.flags |= FLAG_WINDOW_HIDDEN.getFlag();
        }

        // State change: FLAG_WINDOW_MINIMIZED
        if (((window.flags & FLAG_WINDOW_MINIMIZED.getFlag()) != (flags & FLAG_WINDOW_MINIMIZED.getFlag())) && ((flags & FLAG_WINDOW_MINIMIZED.getFlag()) > 0)){
            //GLFW_ICONIFIED
            MinimizeWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_MAXIMIZED
        if (((window.flags & FLAG_WINDOW_MAXIMIZED.getFlag()) != (flags & FLAG_WINDOW_MAXIMIZED.getFlag())) && ((flags & FLAG_WINDOW_MAXIMIZED.getFlag()) > 0)){
            //GLFW_MAXIMIZED
            MaximizeWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_UNFOCUSED
        if (((window.flags & FLAG_WINDOW_UNFOCUSED.getFlag()) != (flags & FLAG_WINDOW_UNFOCUSED.getFlag())) && ((flags & FLAG_WINDOW_UNFOCUSED.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
            window.flags |= FLAG_WINDOW_UNFOCUSED.getFlag();
        }

        // State change: FLAG_WINDOW_TOPMOST
        if (((window.flags & FLAG_WINDOW_TOPMOST.getFlag()) != (flags & FLAG_WINDOW_TOPMOST.getFlag())) && ((flags & FLAG_WINDOW_TOPMOST.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_TOPMOST.getFlag();
        }

        // State change: FLAG_WINDOW_ALWAYS_RUN
        if (((window.flags & FLAG_WINDOW_ALWAYS_RUN.getFlag()) != (flags & FLAG_WINDOW_ALWAYS_RUN.getFlag())) && ((flags & FLAG_WINDOW_ALWAYS_RUN.getFlag()) > 0)){
            window.flags |= FLAG_WINDOW_ALWAYS_RUN.getFlag();
        }

        // The following states can not be changed after window creation

        // State change: FLAG_WINDOW_TRANSPARENT
        if (((window.flags & FLAG_WINDOW_TRANSPARENT.getFlag()) != (flags & FLAG_WINDOW_TRANSPARENT.getFlag())) && ((flags & FLAG_WINDOW_TRANSPARENT.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window " +
                    "initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI.getFlag()) != (flags & FLAG_WINDOW_HIGHDPI.getFlag())) && ((flags & FLAG_WINDOW_HIGHDPI.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT.getFlag()) != (flags & FLAG_MSAA_4X_HINT.getFlag())) && ((flags & FLAG_MSAA_4X_HINT.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT.getFlag()) != (flags & FLAG_INTERLACED_HINT.getFlag())) && ((flags & FLAG_INTERLACED_HINT.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Clear window configuration state flags
    public void ClearWindowState(int flags){
        // Check previous state and requested state to apply required changes
        // NOTE: In most cases the functions already change the flags internally

        // State change: FLAG_VSYNC_HINT
        if (((window.flags & FLAG_VSYNC_HINT.getFlag()) > 0) && ((flags & FLAG_VSYNC_HINT.getFlag()) > 0)){
            glfwSwapInterval(0);
            window.flags &= ~FLAG_VSYNC_HINT.getFlag();
        }

        // State change: FLAG_FULLSCREEN_MODE
        if (((window.flags & FLAG_FULLSCREEN_MODE.getFlag()) > 0) && ((flags & FLAG_FULLSCREEN_MODE.getFlag()) > 0)){
            ToggleFullscreen();     // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_RESIZABLE
        if (((window.flags & FLAG_WINDOW_RESIZABLE.getFlag()) > 0) && ((flags & FLAG_WINDOW_RESIZABLE.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_RESIZABLE, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_RESIZABLE.getFlag();
        }

        // State change: FLAG_WINDOW_UNDECORATED
        if (((window.flags & FLAG_WINDOW_UNDECORATED.getFlag()) > 0) && ((flags & FLAG_WINDOW_UNDECORATED.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_DECORATED, GLFW_TRUE);
            window.flags &= ~FLAG_WINDOW_UNDECORATED.getFlag();
        }

        // State change: FLAG_WINDOW_HIDDEN
        if (((window.flags & FLAG_WINDOW_HIDDEN.getFlag()) > 0) && ((flags & FLAG_WINDOW_HIDDEN.getFlag()) > 0)){
            glfwShowWindow(window.handle);
            window.flags &= ~FLAG_WINDOW_HIDDEN.getFlag();
        }

        // State change: FLAG_WINDOW_MINIMIZED
        if (((window.flags & FLAG_WINDOW_MINIMIZED.getFlag()) > 0) && ((flags & FLAG_WINDOW_MINIMIZED.getFlag()) > 0)){
            RestoreWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_MAXIMIZED
        if (((window.flags & FLAG_WINDOW_MAXIMIZED.getFlag()) > 0) && ((flags & FLAG_WINDOW_MAXIMIZED.getFlag()) > 0)){
            RestoreWindow();       // NOTE: window state flag updated inside function
        }

        // State change: FLAG_WINDOW_UNFOCUSED
        if (((window.flags & FLAG_WINDOW_UNFOCUSED.getFlag()) > 0) && ((flags & FLAG_WINDOW_UNFOCUSED.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
            window.flags &= ~FLAG_WINDOW_UNFOCUSED.getFlag();
        }

        // State change: FLAG_WINDOW_TOPMOST
        if (((window.flags & FLAG_WINDOW_TOPMOST.getFlag()) > 0) && ((flags & FLAG_WINDOW_TOPMOST.getFlag()) > 0)){
            glfwSetWindowAttrib(window.handle, GLFW_FLOATING, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_TOPMOST.getFlag();
        }

        // State change: FLAG_WINDOW_ALWAYS_RUN
        if (((window.flags & FLAG_WINDOW_ALWAYS_RUN.getFlag()) > 0) && ((flags & FLAG_WINDOW_ALWAYS_RUN.getFlag()) > 0)){
            window.flags &= ~FLAG_WINDOW_ALWAYS_RUN.getFlag();
        }

        // The following states can not be changed after window creation

        // State change: FLAG_WINDOW_TRANSPARENT
        if (((window.flags & FLAG_WINDOW_TRANSPARENT.getFlag()) > 0) && ((flags & FLAG_WINDOW_TRANSPARENT.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI.getFlag()) > 0) && ((flags & FLAG_WINDOW_HIGHDPI.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT.getFlag()) > 0) && ((flags & FLAG_MSAA_4X_HINT.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT.getFlag()) > 0) && ((flags & FLAG_INTERLACED_HINT.getFlag()) > 0)){
            Tracelog(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Set icon for window (only PLATFORM_DESKTOP)
    // NOTE: Image must be in RGBA format, 8bit per channel
    public void SetWindowIcon(Image image){
        if (image.getFormat() == UNCOMPRESSED_R8G8B8A8.getPixForInt()){
            GLFWImage[] icon = new GLFWImage[1];

            icon[0].width(image.getWidth());
            icon[0].height(image.getHeight());
            icon[0].pixels(Math.toIntExact(image.getData()[0]));

            // NOTE 1: We only support one image icon
            // NOTE 2: The specified image data is copied before this function returns
            nglfwSetWindowIcon(window.handle, 1, Arrays.stream(icon).count());
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: window icon image must be in R8G8B8A8 pixel format");
        }
    }

    // Set title for window (only PLATFORM_DESKTOP)
    void SetWindowTitle(String title){
        window.title = title;
        glfwSetWindowTitle(window.handle, title);
    }

    // Set window position on screen (windowed mode)
    void SetWindowPosition(int x, int y){
        glfwSetWindowPos(window.handle, x, y);
    }

    // Set monitor for the current window (fullscreen mode)
    void SetWindowMonitor(long monitor){
        int monitorCount = 0;
        PointerBuffer monitors = glfwGetMonitors();

        if ((monitor >= 0) && (monitor < monitorCount)){
            Tracelog(LOG_INFO, "GLFW: Selected fullscreen monitor: [" + monitor + "] " + glfwGetMonitorName(monitor));

            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window.handle, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
    }

    // Set window minimum dimensions (FLAG_WINDOW_RESIZABLE)
    void SetWindowMinSize(int width, int height){
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowSizeLimits(window.handle, width, height, mode.width(), mode.height());
    }

    // Set window dimensions
    // TODO: Issues on HighDPI scaling
    void SetWindowSize(int width, int height){
        glfwSetWindowSize(window.handle, width, height);
    }

    /**
     * Get current screen width
     * @return Width of current window
     */
    public static int GetScreenWidth(){
        return window.screen.getWidth();
    }

    /**
     * Get current screen height
     * @return Height of current window
     */
    public static int GetScreenHeight(){
        return window.screen.getHeight();
    }

    // Get native window handle
    long GetWindowHandle(){
        return glfwGetWin32Window(window.handle);
    }

    // Get number of monitors
    int GetMonitorCount(){
        int monitorCount = 0;
        PointerBuffer pb = glfwGetMonitors();
        monitorCount = pb.sizeof();
        return monitorCount;
    }

    // Get number of monitors
    int GetCurrentMonitor(){
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();
        long monitor;

        if (monitorCount == 1){
            return 0;
        }

        if (IsWindowFullscreen()){
            monitor = glfwGetWindowMonitor(window.handle);
            for (int i = 0; i < monitorCount; i++){
                if (monitors.get(i) == monitor){
                    return i;
                }
            }
            return 0;
        }
        else{
            int x = 0;
            int y = 0;

            nglfwGetWindowPos(window.handle, x, y);

            for (int i = 0; i < monitorCount; i++){
                int mx = 0;
                int my = 0;

                int width = 0;
                int height = 0;

                monitor = monitors.get(i);
                nglfwGetMonitorWorkarea(monitor, mx, my, width, height);
                if (x >= mx && x <= (mx + width) && y >= my && y <= (my + height)){
                    return i;
                }
            }
        }
        return 0;
    }

    // Get selected monitor width
    Vector2 GetMonitorPosition(int monitor){
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();
        if ((monitor >= 0) && (monitor < monitorCount)){
            int x = 0, y = 0;
            nglfwGetMonitorPos(monitor, x, y);
            return new Vector2((float) x, (float) y);
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return null;
    }

    // Get selected monitor width (max available by monitor)
    int GetMonitorWidth(int monitor){
        int monitorCount = 0;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            int count = 0;
            GLFWVidMode.Buffer modes = glfwGetVideoModes(monitor);

            // We return the maximum resolution available, the last one in the modes array
            if (count > 0){
                return modes.width();
            }
            else{
                Tracelog(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor width (max available by monitor)
    int GetMonitorHeight(int monitor){
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            int count = 0;
            GLFWVidMode.Buffer modes = glfwGetVideoModes(monitor);

            // We return the maximum resolution available, the last one in the modes array
            if (count > 0){
                return modes.height();
            }
            else{
                Tracelog(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
            return 0;
        }
        return 0;
    }

    // Get selected monitor physical width in millimetres
    int GetMonitorPhysicalWidth(int monitor){
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            int physicalWidth = 0;
            nglfwGetMonitorPhysicalSize(monitor, physicalWidth, 0);
            return physicalWidth;
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get primary monitor physical height in millimetres
    int GetMonitorPhysicalHeight(int monitor){
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            int physicalHeight = 0;
            nglfwGetMonitorPhysicalSize(monitor, 0, physicalHeight);
            return physicalHeight;
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get the human-readable, UTF-8 encoded name of the primary monitor
    String GetMonitorName(int monitor){
        int monitorCount;
        PointerBuffer monitors = glfwGetMonitors();
        monitorCount = monitors.sizeof();

        if ((monitor >= 0) && (monitor < monitorCount)){
            return glfwGetMonitorName(monitor);
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return "";
    }

    // Get clipboard text content
    // NOTE: returned string is allocated and freed by GLFW
    String GetClipboardText(){
        return glfwGetClipboardString(window.handle);
    }

    // Set clipboard text content
    void SetClipboardText(String text){
        glfwSetClipboardString(window.handle, text);
    }

    // Show mouse cursor
    void ShowCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        input.mouse.setCursorHidden(false);
    }

    // Hides mouse cursor
    void HideCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        input.mouse.setCursorHidden(true);
    }

    // Check if cursor is not visible
    boolean IsCursorHidden(){
        return input.mouse.isCursorHidden();
    }

    // Enables cursor (unlock cursor)
    public static void EnableCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        input.mouse.setCursorHidden(false);
    }

    // Disables cursor (lock cursor)
    public static void DisableCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        input.mouse.setCursorHidden(true);
    }

    /**
     * @return <code>true</code> if cursor is within window bounds
     */
    // Check if cursor is on the current screen.
    boolean IsCursorOnScreen(){
        return input.mouse.isCursorOnScreen();
    }

    /**
     * Clear window background
     * @param color Color to fill the background
     */
    public void ClearBackground(Color color){
        rlClearColor(color.getR(), color.getG(), color.getB(), color.getA());   // Set clear color
        rlClearScreenBuffers();                             // Clear current framebuffers
    }

    /**
     * Setup canvas (framebuffer) to start drawing
     */
    public void BeginDrawing(){
        time.setCurrent(GetTime());            // Number of elapsed seconds since InitTimer()
        time.setUpdate(time.getCurrent() - time.getPrevious());
        time.setPrevious(time.getCurrent());

        rlLoadIdentity();                   // Reset current matrix (modelview)
        rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling

        //rlTranslatef(0.375, 0.375, 0);    // HACK to have 2D pixel-perfect drawing on OpenGL 1.1
        // NOTE: Not required with OpenGL 3.3+
    }

    /**
     * End canvas drawing and swap buffers (double buffering)
     */
    public void EndDrawing(){

        rlglDraw();                     // Draw Buffers (Only OpenGL 3+ and ES2)
        SwapBuffers();                  // Copy back buffer to front buffer

        // Frame time control system
        time.setCurrent(GetTime());
        time.setDraw(time.getCurrent() - time.getPrevious());
        time.previous = time.current;

        time.setFrame(time.getUpdate() + time.getDraw());

        // Wait for some milliseconds...
        if (time.frame < time.target){
            Wait((float) ((time.target - time.frame)*1000.0f));

            time.current = GetTime();
            double waitTime = time.current - time.previous;
            time.previous = time.current;

            time.frame += waitTime;      // Total frame time: update + draw + wait
        }

        PollInputEvents();              // Poll user events
    }

    // Initialize 2D mode with custom camera (2D)
    public void BeginMode2D(Camera2D camera){
        rlglDraw();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        rlLoadIdentity();                   // Reset current matrix (modelview)

        // Apply 2d camera transformation to modelview
        rlMultMatrixf(MatrixToFloat(GetCameraMatrix2D(camera)));

        // Apply screen scaling if required
        rlMultMatrixf(MatrixToFloat(window.getScreenScale()));
    }

    // Ends 2D mode with custom camera
    public void EndMode2D(){
        rlglDraw();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        rlLoadIdentity();                   // Reset current matrix (modelview)
        rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling if required
    }

    // Initializes 3D mode with custom camera (3D)
    void BeginMode3D(Camera3D camera){
        rlglDraw();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        rlPushMatrix();                     // Save previous matrix, which contains the settings for the 2d ortho projection
        rlLoadIdentity();                   // Reset current matrix (projection)

        float aspect = (float) window.currentFbo.getWidth() / (float) window.currentFbo.getHeight();

        if (camera.getTypeI() == CAMERA_PERSPECTIVE.getCamType()){
            // Setup perspective projection
            double top = RL_CULL_DISTANCE_NEAR * Math.tan(camera.getFovy() * 0.5 * DEG2RAD);
            double right = top * aspect;

            rlFrustum(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.getTypeI() == CAMERA_ORTHOGRAPHIC.getCamType()){
            // Setup orthographic projection
            double top = camera.getFovy() / 2.0;
            double right = top * aspect;

            rlOrtho(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }

        // NOTE: zNear and zFar values are important when computing depth buffer values

        rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        rlLoadIdentity();                   // Reset current matrix (modelview)

        // Setup Camera view
        Matrix matView = MatrixLookAt(camera.getPosition(), camera.getTarget(), camera.getUp());
        rlMultMatrixf(MatrixToFloat(matView));      // Multiply modelview matrix by view matrix (camera)

        rlgl.rlEnableDepthTest();                // Enable DEPTH_TEST for 3D
    }

    // Ends 3D mode and returns to default 2D orthographic mode
    void EndMode3D(){
        rlglDraw();                         // Process internal buffers (update + draw)

        rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        rlPopMatrix();                      // Restore previous matrix (projection) from matrix stack

        rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        rlLoadIdentity();                   // Reset current matrix (modelview)

        rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling if required

        rlgl.rlDisableDepthTest();               // Disable DEPTH_TEST for 2D
    }

    // Initializes render texture for drawing
    void BeginTextureMode(RenderTexture target){
        rlglDraw();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        rlEnableFramebuffer(target.getId());     // Enable render target

        // Set viewport to framebuffer size
        rlViewport(0, 0, target.getTexture().getWidth(), target.getTexture().getHeight());

        rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        rlLoadIdentity();                   // Reset current matrix (projection)

        // Set orthographic projection to current framebuffer size
        // NOTE: Configured top-left corner as (0, 0)
        rlOrtho(0, target.getTexture().getWidth(), target.getTexture().getHeight(), 0, 0.0f, 1.0f);

        rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        rlLoadIdentity();                   // Reset current matrix (modelview)

        //rlScalef(0.0f, -1.0f, 0.0f);      // Flip Y-drawing (?)

        // Setup current width/height for proper aspect ratio
        // calculation when using BeginMode3D()
        window.currentFbo.setWidth(target.getTexture().getWidth());
        window.currentFbo.setHeight(target.getTexture().getHeight());
    }

    // Ends drawing to render texture
    void EndTextureMode(){
        rlglDraw();                 // Draw Buffers (Only OpenGL 3+ and ES2)

        rlDisableFramebuffer();     // Disable render target (fbo)

        // Set viewport to default framebuffer size
        SetupViewport(window.render.getWidth(), window.render.getHeight());

        // Reset current screen size
        window.currentFbo.setWidth(GetScreenWidth());
        window.currentFbo.setHeight(GetScreenHeight());
    }

    // Begin scissor mode (define screen area for following drawing)
    // NOTE: Scissor rec refers to bottom-left corner, we change it to upper-left
    void BeginScissorMode(int x, int y, int width, int height){
        rlglDraw(); // Force drawing elements

        rlgl.rlEnableScissorTest();
        rlgl.rlScissor(x, window.currentFbo.getHeight() - (y + height), width, height);
    }

    // End scissor mode
    void EndScissorMode(){
        rlglDraw(); // Force drawing elements
        rlgl.rlDisableScissorTest();
    }

    // Returns a ray trace from mouse position
    public Ray GetMouseRay(Vector2 mouse, Camera3D camera){
        Ray ray = new Ray();

        // Calculate normalized device coordinates
        // NOTE: y value is negative
        float x = (2.0f * mouse.getX()) / (float) GetScreenWidth() - 1.0f;
        float y = 1.0f - (2.0f * mouse.getY()) / (float) GetScreenHeight();
        float z = 1.0f;

        // Store values in a vector
        Vector3 deviceCoords = new Vector3(x, y, z);

        // Calculate view matrix from camera look at
        Matrix matView = MatrixLookAt(camera.getPosition(), camera.getTarget(), camera.getUp());

        Matrix matProj = MatrixIdentity();

        if (camera.getType() == CAMERA_PERSPECTIVE){
            // Calculate projection matrix from perspective
            matProj = MatrixPerspective(camera.getFovy() * DEG2RAD,
                    ((double) GetScreenWidth() / (double) GetScreenHeight()), RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.getType() == CAMERA_ORTHOGRAPHIC){
            float aspect = (float) window.screen.getWidth() / (float) window.screen.getHeight();
            double top = camera.getFovy() / 2.0;
            double right = top * aspect;

            // Calculate projection matrix from orthographic
            matProj = MatrixOrtho(-right, right, -top, top, 0.01, 1000.0);
        }

        // Unproject far/near points
        Vector3 nearPoint = Vector3Unproject(new Vector3(deviceCoords.getX(), deviceCoords.getY(), 0.0f), matProj,
                matView);
        Vector3 farPoint = Vector3Unproject(new Vector3(deviceCoords.getX(), deviceCoords.getY(), 1.0f), matProj,
                matView);

        // Unproject the mouse cursor in the near plane.
        // We need this as the source position because orthographic projects, compared to perspect doesn't have a
        // convergence point, meaning that the "eye" of the camera is more like a plane than a point.
        Vector3 cameraPlanePointerPos = Vector3Unproject(new Vector3(deviceCoords.getX(), deviceCoords.getY(), -1.0f),
                matProj, matView);

        // Calculate normalized direction vector
        Vector3 direction = Vector3Normalize(Vector3Subtract(farPoint, nearPoint));

        if (camera.getType() == CAMERA_PERSPECTIVE){
            ray.position = camera.getPosition();
        }
        else if (camera.getType() == CAMERA_ORTHOGRAPHIC){
            ray.position = cameraPlanePointerPos;
        }

        // Apply calculated vectors to ray
        ray.direction = direction;

        return ray;
    }

    // Get transform matrix for camera
    Matrix GetCameraMatrix(Camera3D camera){
        return MatrixLookAt(camera.getPosition(), camera.getTarget(), camera.getUp());
    }

    /**
     * Returns camera 2d transform matrix
     * @param camera
     * @return Transform matrix
     */
    Matrix GetCameraMatrix2D(Camera2D camera){
        Matrix matTransform = new Matrix();
        // The camera in world-space is set by
        //   1. Move it to target
        //   2. Rotate by -rotation and scale by (1/zoom)
        //      When setting higher scale, it's more intuitive for the world to become bigger (= camera become smaller),
        //      not for the camera getting bigger, hence the invert. Same deal with rotation.
        //   3. Move it by (-offset);
        //      Offset defines target transform relative to screen, but since we're effectively "moving" screen (camera)
        //      we need to do it into opposite direction (inverse transform)

        // Having camera transform in world-space, inverse of it gives the modelview transform.
        // Since (A*B*C)' = C'*B'*A', the modelview is
        //   1. Move to offset
        //   2. Rotate and Scale
        //   3. Move by -target
        Matrix matOrigin = MatrixTranslate(-camera.getTarget().getX(), -camera.getTarget().getY(), 0.0f);
        Matrix matRotation = MatrixRotate(new Vector3(0.0f, 0.0f, 1.0f), camera.getRotation() * DEG2RAD);
        Matrix matScale = MatrixScale(camera.getZoom(), camera.getZoom(), 1.0f);
        Matrix matTranslation = MatrixTranslate(camera.getOffset().getX(), camera.getOffset().getY(), 0.0f);

        matTransform = MatrixMultiply(MatrixMultiply(matOrigin, MatrixMultiply(matScale, matRotation)), matTranslation);

        return matTransform;
    }

    // Returns the screen space position from a 3d world space position
    Vector2 GetWorldToScreen(Vector3 position, Camera3D camera){

        return GetWorldToScreenEx(position, camera, GetScreenWidth(), GetScreenHeight());
    }

    // Returns size position for a 3d world space position (useful for texture drawing)
    Vector2 GetWorldToScreenEx(Vector3 position, Camera3D camera, int width, int height){
        // Calculate projection matrix (from perspective instead of frustum
        Matrix matProj = MatrixIdentity();

        if (camera.getType() == CAMERA_PERSPECTIVE){
            // Calculate projection matrix from perspective
            matProj = MatrixPerspective(camera.getFovy() * DEG2RAD, ((double) width / (double) height),
                    RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.getType() == CAMERA_ORTHOGRAPHIC){
            float aspect = (float) window.screen.getWidth() / (float) window.screen.getHeight();
            double top = camera.getFovy() / 2.0;
            double right = top * aspect;

            // Calculate projection matrix from orthographic
            matProj = MatrixOrtho(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }

        // Calculate view matrix from camera look at (and transpose it)
        Matrix matView = MatrixLookAt(camera.getPosition(), camera.getPosition(), camera.getUp());

        // Convert world position vector to quaternion
        Quaternion worldPos = new Quaternion(position.getX(), position.getY(), position.getZ(), 1.0f);

        // Transform world position to view
        worldPos = QuaternionTransform(worldPos, matView);

        // Transform result to projection (clip space position)
        worldPos = QuaternionTransform(worldPos, matProj);

        // Calculate normalized device coordinates (inverted y)
        Vector3 ndcPos = new Vector3(worldPos.getX() / worldPos.getW(), -worldPos.getY() / worldPos.getW(),
                worldPos.getZ() / worldPos.getW());

        // Calculate 2d screen position vector
        Vector2 screenPosition = new Vector2((ndcPos.getX() + 1.0f) / 2.0f * (float) width,
                (ndcPos.getY() + 1.0f) / 2.0f * (float) height);

        return screenPosition;
    }

    // Returns the screen space position for a 2d camera world space position
    Vector2 GetWorldToScreen2D(Vector2 position, Camera2D camera){
        Matrix matCamera = GetCameraMatrix2D(camera);
        Vector3 transform = Vector3Transform(new Vector3(position.getX(), position.getY(), 0), matCamera);

        return new Vector2(transform.getX(), transform.getY());
    }

    // Returns the world space position for a 2d camera screen space position
    Vector2 GetScreenToWorld2D(Vector2 position, Camera2D camera){
        Matrix invMatCamera = MatrixInvert(GetCameraMatrix2D(camera));
        Vector3 transform = Vector3Transform(new Vector3(position.getX(), position.getY(), 0), invMatCamera);

        return new Vector2(transform.getX(), transform.getY());
    }

    /**
     * Set target FPS (maximum)
     * @param fps FPS limit
     */
    public void SetTargetFPS(int fps){
        if (fps < 1){
            time.setTarget(0.0f);
        }
        else{
            time.setTarget(1.0 / fps);
        }

        Tracelog(LOG_INFO, "TIMER: Target time per frame: " + time.getTarget() * 1000 + " milliseconds");
    }

    /**
     * Returns current FPS
     * NOTE: We calculate an average framerate
     * @return Current average framerate
     */
    public static int GetFPS(){

        int FPS_CAPTURE_FRAMES_COUNT = 30;      // 30 captures
        float FPS_AVERAGE_TIME_SECONDS = 0.5f;     // 500 millisecondes
        float FPS_STEP = (FPS_AVERAGE_TIME_SECONDS / FPS_CAPTURE_FRAMES_COUNT);

        int index = 0;
        float[] history = new float[FPS_CAPTURE_FRAMES_COUNT];
        float average = 0, last = 0;
        float fpsFrame = GetFrameTime();

        if (fpsFrame == 0) return 0;

        if ((GetTime() - last) > FPS_STEP){
            last = (float) GetTime();
            index = (index + 1) % FPS_CAPTURE_FRAMES_COUNT;
            average -= history[index];
            history[index] = fpsFrame / FPS_CAPTURE_FRAMES_COUNT;
            average += history[index];
        }

        return Math.round(1.0f / average);
    }

    /**
     * Returns time in seconds for last frame drawn
     * @return Seconds taken for last frame
     */
    public static float GetFrameTime(){
        return (float) time.getFrame();
    }

    /**
     * Get elapsed time measure in seconds since InitTimer()
     * NOTE: On PLATFORM_DESKTOP InitTimer() is called on InitWindow()
     * NOTE: On PLATFORM_DESKTOP, timer is initialized on glfwInit()
     * @return Time program has been running in seconds
     */
    static double GetTime(){
        return glfwGetTime();
    }

    // Setup window configuration flags (view FLAGS)
    // NOTE: This function is expected to be called before window creation,
    // because it setups some flags for the window creation process.
    // To configure window states after creation, just use SetWindowState()
    public static void SetConfigFlags(ConfigFlag flags){
        // Selected flags are set but not evaluated at this point,
        // flag evaluation happens at InitWindow() or SetWindowState()
        window.flags |= flags.getFlag();
    }

    //TakeScreenShot

    /**
     * Returns a random value between min and max (both included)
     * @param min Minimum value of random number
     * @param max Maximum value of random number
     * @return Random value between the <code>min/code> and <code>max</code>
     */
    public static int GetRandomValue(int min, int max){
        if (min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }

        return (int) (Math.random() * (max - min + 1) + min);
    }

    // Check if the file exists
    boolean FileExists(String fileName){
        File file = new File(fileName);

        return file.exists();
    }

    // TODO: 3/20/21
    //IsFileExtension

    //DirectoryExists

    //GetFileExtension

    //StrPrBrk

    //GetFileName

    //GetFileNameWithoutExt

    //GetDirectoryPath

    //GetPrevDirectoryPath

    //GetWorkingDirectory

    //GetDirectoryFiles

    //ClearDirectoryFiles

    //ChangeDirectory

    //IsFileDropped

    //GetDroppedFiles

    static void ClearDroppedFiles(){
        if (window.getDropFilesCount() > 0){
            for (int i = 0; i < window.getDropFilesCount(); i++){
                window.dropFilesPath[i] = null;
            }

            window.setDropFilesPath(null);

            window.setDropFilesCount(0);
        }
    }

    // TODO: 3/20/21
    //GetFileModTime

    //CompressData

    //DecompressData

    //SaveStorageValue

    //LoadStorageValue

    //OpenURL

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Input (Keyboard, Mouse, Gamepad) Functions
    //----------------------------------------------------------------------------------
    // Detect if a key has been pressed once
    public boolean IsKeyPressed(Keyboard.KeyboardKey key){
        int kkey = key.getKeyInt();

        return ((!input.keyboard.getPreviousKeyState()[kkey]) && (input.keyboard.getCurrentKeyState()[kkey]));

    }

    public boolean IsKeyPressed(int key){
        return ((!input.keyboard.getPreviousKeyState()[key]) && (input.keyboard.getCurrentKeyState()[key]));

    }

    // Detect if a key is being pressed (key held down)
    public static boolean IsKeyDown(Keyboard.KeyboardKey key){
        return input.keyboard.getCurrentKeyState()[key.getKeyInt()];
    }

    public static boolean IsKeyDown(int key){
        return input.keyboard.getCurrentKeyState()[key];
    }

    // Detect if a key has been released once
    public boolean IsKeyReleased(Keyboard.KeyboardKey key){
        int kkey = key.getKeyInt();

        return (input.keyboard.getPreviousKeyState()[kkey] && !input.keyboard.getCurrentKeyState()[kkey]);
    }

    // Detect if a key has been released once
    public boolean IsKeyReleased(int key){
        return (input.keyboard.getPreviousKeyState()[key] && !input.keyboard.getCurrentKeyState()[key]);
    }

    // Detect if a key is NOT being pressed (key not held down)
    public boolean IsKeyUp(Keyboard.KeyboardKey key){
        return !input.keyboard.getCurrentKeyState()[key.getKeyInt()];
    }

    // Detect if a key is NOT being pressed (key not held down)
    public boolean IsKeyUp(int key){
        return !input.keyboard.getCurrentKeyState()[key];
    }

    // Get the last key pressed
    public int GetKeyPressed(){
        int value = 0;

        if (input.keyboard.getKeyPressedQueueCount() > 0){
            // Get character from the queue head
            value = input.keyboard.getKeyPressedQueue()[0];

            // Shift elements 1 step toward the head.
            for (int i = 0; i < (input.keyboard.getKeyPressedQueueCount() - 1); i++){
                input.keyboard.getKeyPressedQueue()[i] = input.keyboard.getKeyPressedQueue()[i + 1];
            }

            // Reset last character in the queue
            input.keyboard.getKeyPressedQueue()[input.keyboard.getKeyPressedQueueCount()] = 0;
            input.keyboard.setKeyPressedQueueCount(input.keyboard.getKeyPressedQueueCount() - 1);
        }

        return value;
    }

    // Get the last char pressed
    public int GetCharPressed(){
        int value = 0;

        if (input.keyboard.getCharPressedQueueCount() > 0){
            // Get character from the queue head
            value = input.keyboard.getCharPressedQueue()[0];

            // Shift elements 1 step toward the head.
            if (input.keyboard.getCharPressedQueueCount() - 1 >= 0){
                System.arraycopy(input.keyboard.getKeyPressedQueue(), 1, input.keyboard.getKeyPressedQueue(), 0, input.keyboard.getCharPressedQueueCount() - 1);
            }

            // Reset last character in the queue
            input.keyboard.getCharPressedQueue()[input.keyboard.getCharPressedQueueCount()] = 0;
            input.keyboard.setCharPressedQueueCount(input.keyboard.getCharPressedQueueCount() - 1);
        }

        return value;
    }

    // Set a custom key to exit program
    // NOTE: default exitKey is ESCAPE
    public void SetExitKey(Keyboard.KeyboardKey key){
        input.keyboard.setExitKey(key.getKeyInt());
    }

    /*
    TODO: Gamepad support
    // NOTE: Gamepad support not implemented in emscripten GLFW3 (PLATFORM_WEB)

    // Detect if a gamepad is available
    boolean IsGamepadAvailable(int gamepad)
    {
        boolean result = false;

        if ((gamepad < MAX_GAMEPADS) && input.Gamepad.ready[gamepad]) result = true;

        return result;
    }

    // Check gamepad name (if available)
    boolean IsGamepadName(int gamepad, const char *name)
    {
        boolean result = false;
        const char *currentName = NULL;

        if (input.Gamepad.ready[gamepad]) currentName = GetGamepadName(gamepad);
        if ((name != NULL) && (currentName != NULL)) result = (strcmp(name, currentName) == 0);

        return result;
    }

    // Return gamepad internal name id
    const char *GetGamepadName(int gamepad)
    {
        #if defined(PLATFORM_DESKTOP)
            if (input.Gamepad.ready[gamepad]) return glfwGetJoystickName(gamepad);
            else return NULL;
        #endif
        #if defined(PLATFORM_RPI) || defined(PLATFORM_DRM)
            if (input.Gamepad.ready[gamepad]) ioctl(input.Gamepad.streamId[gamepad], JSIOCGNAME(64), &input.Gamepad.name);
            return input.Gamepad.name;
        #endif
        return NULL;
    }

    // Return gamepad axis count
    int GetGamepadAxisCount(int gamepad)
    {
        #if defined(PLATFORM_RPI) || defined(PLATFORM_DRM)
            int axisCount = 0;
            if (input.Gamepad.ready[gamepad]) ioctl(input.Gamepad.streamId[gamepad], JSIOCGAXES, &axisCount);
            input.Gamepad.axisCount = axisCount;
        #endif

        return input.Gamepad.axisCount;
    }

    // Return axis movement vector for a gamepad
    float GetGamepadAxisMovement(int gamepad, int axis)
    {
        float value = 0;

        if ((gamepad < MAX_GAMEPADS) && input.Gamepad.ready[gamepad] && (axis < MAX_GAMEPAD_AXIS) &&
                (fabsf(input.Gamepad.axisState[gamepad][axis]) > 0.1f)) value = input.Gamepad.axisState[gamepad][axis];      // 0.1f = GAMEPAD_AXIS_MINIMUM_DRIFT/DELTA

        return value;
    }

    // Detect if a gamepad button has been pressed once
    boolean IsGamepadButtonPressed(int gamepad, int button)
    {
        boolean pressed = false;

        if ((gamepad < MAX_GAMEPADS) && input.Gamepad.ready[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.Gamepad.previousState[gamepad][button] == 0) && (input.Gamepad.currentState[gamepad][button] == 1)) pressed = true;
        else pressed = false;

        return pressed;
    }

    // Detect if a gamepad button is being pressed
    boolean IsGamepadButtonDown(int gamepad, int button)
    {
        boolean result = false;

        if ((gamepad < MAX_GAMEPADS) && input.Gamepad.ready[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.Gamepad.currentState[gamepad][button] == 1)) result = true;

        return result;
    }

    // Detect if a gamepad button has NOT been pressed once
    boolean IsGamepadButtonReleased(int gamepad, int button)
    {
        boolean released = false;

        if ((gamepad < MAX_GAMEPADS) && input.Gamepad.ready[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.Gamepad.previousState[gamepad][button] == 1) && (input.Gamepad.currentState[gamepad][button] == 0)) released = true;
        else released = false;

        return released;
    }

    // Detect if a mouse button is NOT being pressed
    boolean IsGamepadButtonUp(int gamepad, int button)
    {
        boolean result = false;

        if ((gamepad < MAX_GAMEPADS) && input.Gamepad.ready[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.Gamepad.currentState[gamepad][button] == 0)) result = true;

        return result;
    }

    // Get the last gamepad button pressed
    int GetGamepadButtonPressed(void)
    {
        return input.Gamepad.lastButtonPressed;
    }

    // Set internal gamepad mappings
    int SetGamepadMappings(const char *mappings)
    {
        int result = 0;

        #if defined(PLATFORM_DESKTOP)
            result = glfwUpdateGamepadMappings(mappings);
        #endif

        return result;
    }
     */

    // Detect if a mouse button has been pressed once
    public boolean IsMouseButtonPressed(Mouse.MouseButton button){
        boolean pressed = false;

        if ((input.mouse.getCurrentButtonState()[button.getButtonInt()] == 1) &&
                (input.mouse.getPreviousButtonState()[button.getButtonInt()] == 0)){
            pressed = true;
        }

        /* TODO: Touch support - MODULE GESTURES
        // Map touches to mouse buttons checking
        if ((input.touch.currentTouchState[button] == 1) && (input.touch.previousTouchState[button] == 0)){
            pressed = true;
        }
        */

        return pressed;
    }

    // Detect if a mouse button has been pressed once
    public boolean IsMouseButtonPressed(int button){
        boolean pressed = false;

        if ((input.mouse.getCurrentButtonState()[button] == 1) &&
                (input.mouse.getPreviousButtonState()[button] == 0)){
            pressed = true;
        }

        /* TODO: Touch support - MODULE GESTURES
        // Map touches to mouse buttons checking
        if ((input.touch.currentTouchState[button] == 1) && (input.touch.previousTouchState[button] == 0)){
            pressed = true;
        }
        */

        return pressed;
    }

    // Detect if a mouse button is being pressed
    public static boolean IsMouseButtonDown(Mouse.MouseButton button){
        boolean down = false;

        if (input.mouse.getCurrentButtonState()[button.getButtonInt()] == 1){
            down = true;
        }

        /*
        TODO: Touch support - MODULE GESTURES
        Map touches to mouse buttons checking
        if (input.Touch.currentTouchState[button] == 1) down = true;
        */

        return down;
    }

    // Detect if a mouse button is being pressed
    public static boolean IsMouseButtonDown(int button){
        boolean down = false;

        if (input.mouse.getCurrentButtonState()[button] == 1){
            down = true;
        }

        /*
        TODO: Touch support - MODULE GESTURES
        Map touches to mouse buttons checking
        if (input.Touch.currentTouchState[button] == 1) down = true;
        */

        return down;
    }

    // Detect if a mouse button has been released once
    public boolean IsMouseButtonReleased(Mouse.MouseButton button){
        boolean released = false;

        if ((input.mouse.getCurrentButtonState()[button.getButtonInt()] == 0) &&
                (input.mouse.getCurrentButtonState()[button.getButtonInt()] == 1)){
            released = true;
        }

        /*
        TODO: Touch support - MODULE GESTURES
        Map touches to mouse buttons checking
        if ((input.Touch.currentTouchState[button] == 0) && (input.Touch.previousTouchState[button] == 1)){
            released = true;
        }
         */
        return released;
    }

    // Detect if a mouse button has been released once
    public boolean IsMouseButtonReleased(int button){
        boolean released = false;

        if ((input.mouse.getCurrentButtonState()[button] == 0) &&
                (input.mouse.getCurrentButtonState()[button] == 1)){
            released = true;
        }

        /*
        TODO: Touch support - MODULE GESTURES
        Map touches to mouse buttons checking
        if ((input.Touch.currentTouchState[button] == 0) && (input.Touch.previousTouchState[button] == 1)){
            released = true;
        }
         */
        return released;
    }

    // Detect if a mouse button is NOT being pressed
    public boolean IsMouseButtonUp(Mouse.MouseButton button){
        return !IsMouseButtonDown(button);
    }

    // Detect if a mouse button is NOT being pressed
    public boolean IsMouseButtonUp(int button){
        return !IsMouseButtonDown(button);
    }

    // Returns mouse position X
    public int GetMouseX(){
        /* TODO: Touch support - MODULE GESTURES
        #if defined(PLATFORM_ANDROID)
            return (int)input.Touch.position[0].x;
        #else
        */
        return (int) ((input.mouse.getPosition().getX() + input.mouse.getOffset().getX()) * input.mouse.getScale().getX());
        //#endif
    }

    // Returns mouse position Y
    public int GetMouseY(){
        /* TODO: Touch support - MODULE GESTURES
        #if defined(PLATFORM_ANDROID)
            return (int)input.Touch.position[0].y;
        #else
        */
        return (int) ((input.mouse.getPosition().getY() + input.mouse.getOffset().getY()) * input.mouse.getScale().getY());
        //#endif
    }

    // Returns mouse position XY
    public static Vector2 GetMousePosition(){
        Vector2 position = new Vector2();

        /*
        TODO: Touch support - MODULE GESTURES
        #if defined(PLATFORM_ANDROID) || defined(PLATFORM_WEB)
        position = GetTouchPosition(0);
        #else
        */
        position.setX((input.mouse.getPosition().getX() + input.mouse.getOffset().getX()) * input.mouse.getScale().getX());
        position.setY((input.mouse.getPosition().getY() + input.mouse.getOffset().getY()) * input.mouse.getScale().getY());
        //#endif

        return position;
    }

    // Set mouse position XY
    public void SetMousePosition(int x, int y){
        input.mouse.setPosition(new Vector2((float) x, (float) y));
        // NOTE: emscripten not implemented
        glfwSetCursorPos(window.handle, input.mouse.getPosition().getX(), input.mouse.getPosition().getY());
    }

    // Set mouse offset
    // NOTE: Useful when rendering to different size targets
    public void SetMouseOffset(int offsetX, int offsetY){
        input.mouse.setOffset(new Vector2((float) offsetX, (float) offsetY));
    }

    // Set mouse scaling
    // NOTE: Useful when rendering to different size targets
    public void SetMouseScale(float scaleX, float scaleY){
        input.mouse.setScale(new Vector2(scaleX, scaleY));
    }

    // Returns mouse wheel movement Y
    public static float GetMouseWheelMove(){
        return input.mouse.getPreviousWheelMove();
    }

    // Returns mouse cursor
    public long GetMouseCursor(){
        return input.mouse.getCursor();
    }

    // Set mouse cursor
    // NOTE: This is a no-op on platforms other than PLATFORM_DESKTOP
    public void SetMouseCursor(int cursor){
        input.mouse.setCursor(cursor);
        if (cursor == MOUSE_CURSOR_DEFAULT.getMouseCursorInt()){
            glfwSetCursor(window.handle, 0);
        }
        else{
            // NOTE: We are relating internal GLFW enum values to our MouseCursor enum values
            glfwSetCursor(window.handle, glfwCreateStandardCursor(0x00036000 + cursor));
        }
    }

    /* TODO: Touch support - MODULE GESTURES
    // Returns touch position X for touch point 0 (relative to screen size)
    int GetTouchX(void)
    {
        #if defined(PLATFORM_ANDROID) || defined(PLATFORM_WEB) || defined(PLATFORM_UWP)
            return (int)input.Touch.position[0].x;
        #else   // PLATFORM_DESKTOP, PLATFORM_RPI, PLATFORM_DRM
            return GetMouseX();
        #endif
    }

    // Returns touch position Y for touch point 0 (relative to screen size)
    int GetTouchY(void)
    {
        #if defined(PLATFORM_ANDROID) || defined(PLATFORM_WEB) || defined(PLATFORM_UWP)
            return (int)input.Touch.position[0].y;
        #else   // PLATFORM_DESKTOP, PLATFORM_RPI, PLATFORM_DRM
            return GetMouseY();
        #endif
    }

    // Returns touch position XY for a touch point index (relative to screen size)
    // TODO: Touch position should be scaled depending on display size and render size
    Vector2 GetTouchPosition(int index)
    {
        Vector2 position = { -1.0f, -1.0f };

        #if defined(PLATFORM_DESKTOP)
            // TODO: GLFW does not support multi-touch input just yet
            // https://www.codeproject.com/Articles/668404/Programming-for-Multi-Touch
            // https://docs.microsoft.com/en-us/windows/win32/wintouch/getting-started-with-multi-touch-messages
            if (index == 0) position = GetMousePosition();
        #endif
        #if defined(PLATFORM_ANDROID)
            if (index < MAX_TOUCH_POINTS) position = input.Touch.position[index];
            else TRACELOG(LOG_WARNING, "INPUT: Required touch point out of range (Max touch points: %i)", MAX_TOUCH_POINTS);

            if ((CORE.Window.screen.width > CORE.Window.display.width) || (CORE.Window.screen.height > CORE.Window.display.height))
            {
                position.x = position.x*((float)CORE.Window.screen.width/(float)(CORE.Window.display.width - CORE.Window.renderOffset.x)) - CORE.Window.renderOffset.x/2;
                position.y = position.y*((float)CORE.Window.screen.height/(float)(CORE.Window.display.height - CORE.Window.renderOffset.y)) - CORE.Window.renderOffset.y/2;
            }
            else
            {
                position.x = position.x*((float)CORE.Window.render.width/(float)CORE.Window.display.width) - CORE.Window.renderOffset.x/2;
                position.y = position.y*((float)CORE.Window.render.height/(float)CORE.Window.display.height) - CORE.Window.renderOffset.y/2;
            }
        #endif
        #if defined(PLATFORM_WEB) || defined(PLATFORM_RPI) || defined(PLATFORM_DRM) || defined(PLATFORM_UWP)
            if (index < MAX_TOUCH_POINTS) position = input.Touch.position[index];
            else TRACELOG(LOG_WARNING, "INPUT: Required touch point out of range (Max touch points: %i)", MAX_TOUCH_POINTS);

            // TODO: Touch position scaling required?
        #endif

        return position;
    }
    */

    //----------------------------------------------------------------------------------
    // Module specific Functions Definition
    //----------------------------------------------------------------------------------

    // Initialize display device and framebuffer
    // NOTE: width and height represent the screen (framebuffer) desired size, not actual display size
    // If width or height are 0, default display size will be used for framebuffer size
    // NOTE: returns false in case graphic device could not be created
    boolean InitGraphicsDevice(int width, int height){
        window.screen.setWidth(width);            // User desired width
        window.screen.setHeight(height);          // User desired height
        window.setScreenScale(MatrixIdentity());  // No draw scaling required by default

        // NOTE: Framebuffer (render area - window.render.getWidth(), window.render.height) could include black bars...
        // ...in top-down or left-right to match display aspect ratio (no weird scalings)

        glfwSetErrorCallback(errorCallback = new Callbacks.ErrorCallback());

        if (!glfwInit()){
            Tracelog(LOG_WARNING, "GLFW: Failed to initialize GLFW");
            return false;
        }

        // Find monitor resolution
        long monitor = glfwGetPrimaryMonitor();
        if (monitor < 0){
            Tracelog(LOG_WARNING, "GLFW: Failed to get primary monitor");
            return false;
        }
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        window.display.setWidth(mode.width());
        window.display.setHeight(mode.height());

        // Screen size security check
        if (window.screen.getWidth() == 0){
            window.screen.setWidth(window.display.getWidth());
        }

        if (window.screen.getHeight() == 0){
            window.screen.setHeight(window.display.getHeight());
        }

        glfwDefaultWindowHints();                       // Set default windows hints
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);       // the window will stay hidden after creation
        //glfwWindowHint(GLFW_RED_BITS, 8);             // Framebuffer red color component bits
        //glfwWindowHint(GLFW_GREEN_BITS, 8);           // Framebuffer green color component bits
        //glfwWindowHint(GLFW_BLUE_BITS, 8);            // Framebuffer blue color component bits
        //glfwWindowHint(GLFW_ALPHA_BITS, 8);           // Framebuffer alpha color component bits
        //glfwWindowHint(GLFW_DEPTH_BITS, 24);          // Depthbuffer bits
        //glfwWindowHint(GLFW_REFRESH_RATE, 0);         // Refresh rate for fullscreen window
        //glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API); // OpenGL API to use. Alternative: GLFW_OPENGL_ES_API
        //glfwWindowHint(GLFW_AUX_BUFFERS, 0);          // Number of auxiliar buffers

        // Check window creation flags
        if ((window.getFlags() & FLAG_FULLSCREEN_MODE.getFlag()) > 0){
            window.fullscreen = true;
        }

        if ((window.getFlags() & FLAG_WINDOW_HIDDEN.getFlag()) > 0){
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Visible window
        }
        else{
            glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);     // Window initially hidden
        }

        if ((window.getFlags() & FLAG_WINDOW_UNDECORATED.getFlag()) > 0){
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE); // Border and buttons on Window
        }
        else{
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);   // Decorated window
        }

        if ((window.getFlags() & FLAG_WINDOW_RESIZABLE.getFlag()) > 0){
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Resizable window
        }
        else{
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);  // Avoid window being resizable
        }

        // Disable FLAG_WINDOW_MINIMIZED, not supported on initialization
        if ((window.getFlags() & FLAG_WINDOW_MINIMIZED.getFlag()) > 0){
            window.flags &= ~FLAG_WINDOW_MINIMIZED.getFlag();
        }

        // Disable FLAG_WINDOW_MAXIMIZED, not supported on initialization
        if ((window.getFlags() & FLAG_WINDOW_MAXIMIZED.getFlag()) > 0){
            window.flags &= ~FLAG_WINDOW_MAXIMIZED.getFlag();
        }

        if ((window.getFlags() & FLAG_WINDOW_UNFOCUSED.getFlag()) > 0){
            glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
        }
        else{
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        }

        if ((window.getFlags() & FLAG_WINDOW_TOPMOST.getFlag()) > 0){
            glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        }
        else{
            glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        }

        if ((window.getFlags() & FLAG_WINDOW_TRANSPARENT.getFlag()) > 0){
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);     // Transparent framebuffer
        }
        else{
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);     // Transparent framebuffer
        }

        if ((window.getFlags() & FLAG_WINDOW_HIGHDPI.getFlag()) > 0){
            // Resize window content area based on the monitor content scale.
            // NOTE: This hint only has an effect on platforms where screen coordinates and pixels always map 1:1 such as Windows and X11.
            // On platforms like macOS the resolution of the framebuffer is changed independently of the window size.
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);  // Scale content area based on the monitor content scale
            // where window is placed on
        }
        else{
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE);
        }

        if ((window.getFlags() & FLAG_MSAA_4X_HINT.getFlag()) > 0){
            Tracelog(LOG_INFO, "DISPLAY: Trying to enable MSAA x4");
            glfwWindowHint(GLFW_SAMPLES, 4);   // Tries to enable multisampling x4 (MSAA), default is 0
        }

        // NOTE: When asking for an OpenGL context version, most drivers provide highest supported version
        // with forward compatibility to older OpenGL versions.
        // For example, if using OpenGL 1.1, driver can provide a 4.3 context forward compatible.

        // Check selection OpenGL version

        if (RLGL.rlGetVersion() == OPENGL_21.getGlType()){
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);          // Choose OpenGL minor version (just hint)
        }
        else if (RLGL.rlGetVersion() == OPENGL_33.getGlType()){
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);          // Choose OpenGL minor version (just hint)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Profiles Hint: Only 3.3 and above!
            if(__APPLE__){
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            }
            else{
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);
            }
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }
        else if(RLGL.rlGetVersion() == OPENGL_ES_20.getGlType()){
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            if(PLATFORM_DESKTOP){
                glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
            }
            else {
                glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
            }
        }

        if (Config.MAX_GAMEPADS > 0){
            //TODO - GAMEPAD SUPPORT
            glfwSetJoystickCallback(null);
        }

        if (window.fullscreen){
            // remember center for switchinging from fullscreen to window
            window.position.setX(window.display.getWidth() / 2 - window.screen.getWidth() / 2);
            window.position.setY(window.display.getHeight() / 2 - window.screen.getHeight() / 2);

            if (window.position.getX() < 0) {
                window.position.setX(0);
            }
            if (window.position.getX() < 0) {
                window.position.setY(0);
            }

            // Obtain recommended window.display.getWidth()/window.display.getHeight() from a valid videomode for the monitor
            int count = 0;
            GLFWVidMode.Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
            count = modes != null ? modes.sizeof() : 0;
            // Get closest video mode to desired window.screen.getWidth()/window.screen.getHeight()
            for (int i = 0; i < count; i++){
                if (modes.width() >= window.screen.getWidth()){
                    if (modes.height() >= window.screen.getHeight()){
                        window.display.setWidth(modes.width());
                        window.display.setHeight(modes.height());
                        break;
                    }
                }
            }

            if ((window.screen.getHeight() == window.display.getHeight()) && (window.screen.getWidth() == window.display.getWidth())){
                glfwWindowHint(GLFW_AUTO_ICONIFY, 0);
            }

            Tracelog(LOG_WARNING, "SYSTEM: Closest fullscreen videomode: " + window.display.getWidth() + "x" + window.display.getHeight());

            // NOTE: ISSUE: Closest videomode could not match monitor aspect-ratio, for example,
            // for a desired screen size of 800x450 (16:9), closest supported videomode is 800x600 (4:3),
            // framebuffer is rendered correctly but once displayed on a 16:9 monitor, it gets stretched
            // by the sides to fit all monitor space...

            // Try to setup the most appropiate fullscreen framebuffer for the requested screenWidth/screenHeight
            // It considers device display resolution mode and setups a framebuffer with black bars if required (render size/offset)
            // Modified global variables: window.screen.getWidth()/window.screen.getHeight() - window.render.getWidth()/window.render.getHeight() - window.renderOffset.x/window.renderOffset.y - window.screenScale
            // TODO: It is a quite cumbersome solution to display size vs requested size, it should be reviewed or removed...
            // HighDPI monitors are properly considered in a following similar function: SetupViewport()
            SetupFramebuffer(window.display.getWidth(), window.display.getHeight());

            window.handle = glfwCreateWindow(window.display.getWidth(), window.display.getHeight(),
                    (window.title != null) ? window.title : " ", glfwGetPrimaryMonitor(), 0);

            // NOTE: Full-screen change, not working properly...
            //glfwSetWindowMonitor(window.handle, glfwGetPrimaryMonitor(), 0, 0, window.screen.getWidth(), window.screen.getHeight(), GLFW_DONT_CARE);
        }
        else{
            // No-fullscreen window creation
            window.handle = glfwCreateWindow(window.screen.getWidth(), window.screen.getHeight(), (window.title != null)
                    ? window.title : " ", NULL, NULL);

            if (window.handle > 0){
                // Center window on screen
                int windowPosX = window.display.getWidth() / 2 - window.screen.getWidth() / 2;
                int windowPosY = window.display.getHeight() / 2 - window.screen.getHeight() / 2;

                if (windowPosX < 0) windowPosX = 0;
                if (windowPosY < 0) windowPosY = 0;

                glfwSetWindowPos(window.handle, windowPosX, windowPosY);

                window.render.setWidth(window.screen.getWidth());
                window.render.setHeight(window.screen.getHeight());
            }
        }

        if (window.handle <= 0){
            glfwTerminate();
            Tracelog(LOG_WARNING, "GLFW: Failed to initialize Window");
            return false;
        }
        else{
            Tracelog(LOG_INFO, "DISPLAY: Device initialized successfully");
            Tracelog(LOG_INFO,
                    "    > Display size: " + window.display.getWidth() + " x " + window.display.getHeight());
            Tracelog(LOG_INFO,
                    "    > Render size:  " + window.render.getWidth() + " x " + window.render.getHeight());
            Tracelog(LOG_INFO,
                    "    > Screen size:  " + window.screen.getWidth() + " x " + window.screen.getHeight());
            Tracelog(LOG_INFO, "    > Viewport offsets: " + window.renderOffset.x + ", " + window.renderOffset.y);
        }

        // Set window callback events
        glfwSetWindowMaximizeCallback(window.handle, maximizeCallback = new Callbacks.WindowMaximizeCallback());
        glfwSetWindowSizeCallback(window.handle, sizeCallback = new Callbacks.WindowSizeCallback());
        // NOTE: Resizing not allowed by default!
        glfwSetWindowIconifyCallback(window.handle, iconifyCallback = new Callbacks.WindowIconifyCallback());
        glfwSetWindowFocusCallback(window.handle, focusCallback = new Callbacks.WindowFocusCallback());
        glfwSetDropCallback(window.handle, dropCallback = new Callbacks.WindowDropCallback());
        // Set input callback events
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window.handle, keyCallback = new Callbacks.KeyCallback());
        glfwSetCharCallback(window.handle, charCallback = new Callbacks.CharCallback());
        glfwSetMouseButtonCallback(window.handle, mouseBtnCallback = new Callbacks.MouseButtonCallback());
        glfwSetCursorPosCallback(window.handle, cursorPosCallback = new Callbacks.MouseCursorPosCallback());
        // Track mouse position changes
        glfwSetScrollCallback(window.handle, scrollCallback = new Callbacks.MouseScrollCallback());
        glfwSetCursorEnterCallback(window.handle, cursorEnterCallback = new Callbacks.CursorEnterCallback());

        glfwMakeContextCurrent(window.handle);
        GL.createCapabilities();

        // Load OpenGL 3.3 extensions
        // NOTE: GLFW loader function is passed as parameter
        //TODO - rlLoadExtensions uses GLAD.
        //rlLoadExtensions(glfwGetProcAddress());
        rlLoadExtensions();

        // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
        // NOTE: V-Sync can be enabled by graphic driver configuration
        if ((window.getFlags() & FLAG_VSYNC_HINT.getFlag()) > 0){
            // WARNING: It seems to hits a critical render path in Intel HD Graphics
            glfwSwapInterval(1);
            Tracelog(LOG_INFO, "DISPLAY: Trying to enable VSYNC");
        }

        // Initialize OpenGL context (states and resources)
        // NOTE: window.screen.getWidth() and window.screen.getHeight() not used, just stored as globals in rlgl
        rlglInit(window.screen.getWidth(), window.screen.getHeight());

        int fbWidth = window.render.getWidth();
        int fbHeight = window.render.getHeight();

        if ((window.getFlags() & FLAG_WINDOW_HIGHDPI.getFlag()) > 0){
            glfwGetFramebufferSize(window.handle, new int[]{fbWidth}, new int[]{fbHeight});

            // Screen scaling matrix is required in case desired screen area is different than display area
            window.screenScale = MatrixScale((float) fbWidth / window.screen.getWidth(), (float) fbHeight / window.screen.getHeight(), 1.0f);
        }

        // Setup default viewport
        SetupViewport(fbWidth, fbHeight);

        window.currentFbo.setWidth(window.screen.getWidth());
        window.currentFbo.setHeight(window.screen.getHeight());

        ClearBackground(RAYWHITE);      // Default background color for raylib games :P

        glfwShowWindow(window.handle);

        if ((window.getFlags() & FLAG_WINDOW_MINIMIZED.getFlag()) > 0){
            MinimizeWindow();
        }

        return true;
    }

    static void SetupViewport(int width, int height){
        window.render.setWidth(width);
        window.render.setHeight(height);

        // Set viewport width and height
        // NOTE: We consider render size and offset in case black bars are required and
        // render area does not match full display area (this situation is only applicable on fullscreen mode)
        rlViewport((int) window.renderOffset.x / 2, (int) window.renderOffset.y / 2,
                (int) (window.render.getWidth() - window.renderOffset.getX()),
                (int) (window.render.getHeight() - window.renderOffset.getY()));

        rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        rlLoadIdentity();                   // Reset current matrix (projection)

        // Set orthographic projection to current framebuffer size
        // NOTE: Configured top-left corner as (0, 0)
        rlOrtho(0, window.render.getWidth(), window.render.getHeight(), 0, 0.0f, 1.0f);

        rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        rlLoadIdentity();                   // Reset current matrix (modelview)
    }

    // Compute framebuffer size relative to screen size and display size
    // NOTE: Global variables CORE.Window.render.width/CORE.Window.render.height and
    // CORE.Window.renderOffset.x/CORE.Window.renderOffset.y can be modified
    void SetupFramebuffer(int width, int height){
        // Calculate window.render.getWidth() and window.render.getHeight(), we have the display size (input params) and the desired screen size (global var)
        if ((window.screen.getWidth() > window.display.getWidth()) || (window.screen.getHeight() > window.display.getHeight())){
            Tracelog(LOG_WARNING, "DISPLAY: Downscaling required: Screen size (" + window.screen.getWidth() + "x" +
                    window.screen.getHeight() + ") is bigger than display size " + "(" + window.display.getWidth() + "x" +
                    window.display.getHeight() + ")");

            // Downscaling to fit display with border-bars
            float widthRatio = (float) window.display.getWidth() / (float) window.screen.getWidth();
            float heightRatio = (float) window.display.getHeight() / (float) window.screen.getHeight();

            if (widthRatio <= heightRatio){
                window.render.setWidth(window.display.getWidth());
                window.render.setHeight(Math.round((float) window.screen.getHeight() * widthRatio));
                window.renderOffset.x = 0;
                window.renderOffset.y = (window.display.getHeight() - window.render.getHeight());
            }
            else{
                window.render.setWidth((Math.round((float) window.screen.getWidth() * heightRatio)));
                window.render.setHeight(window.display.getHeight());
                window.renderOffset.x = (window.display.getWidth() - window.render.getWidth());
                window.renderOffset.y = 0;
            }

            // Screen scaling required
            float scaleRatio = (float) window.render.getWidth() / (float) window.screen.getWidth();
            window.screenScale = MatrixScale(scaleRatio, scaleRatio, 1.0f);

            // NOTE: We render to full display resolution!
            // We just need to calculate above parameters for downscale matrix and offsets
            window.render.setWidth(window.display.getWidth());
            window.render.setHeight(window.display.getHeight());

            Tracelog(LOG_WARNING, "DISPLAY: Downscale matrix generated, content will be rendered at (" +
                    window.render.getWidth() + "x" + window.render.getHeight() + ")");
        }
        else if ((window.screen.getWidth() < window.display.getWidth()) || (window.screen.getHeight() < window.display.getHeight())){
            // Required screen size is smaller than display size
            Tracelog(LOG_INFO, "DISPLAY: Upscaling required: Screen size (" + window.screen.getWidth() + "x" +
                    window.screen.getHeight() + ") smaller than display size (" + window.display.getWidth() + "x" +
                    window.display.getHeight() + ")");

            if ((window.screen.getWidth() == 0) || (window.screen.getHeight() == 0)){
                window.screen.setWidth(window.display.getWidth());
                window.screen.setHeight(window.display.getHeight());
            }

            // Upscaling to fit display with border-bars
            float displayRatio = (float) window.display.getWidth() / (float) window.display.getHeight();
            float screenRatio = (float) window.screen.getWidth() / (float) window.screen.getHeight();

            if (displayRatio <= screenRatio){
                window.render.setWidth(window.screen.getWidth());
                window.render.setHeight(Math.round((float) window.screen.getWidth() / displayRatio));
                window.renderOffset.setX(0);
                window.renderOffset.setY((window.render.getHeight() - window.screen.getHeight()));
            }
            else{
                window.render.setWidth(Math.round((float) window.screen.getHeight() * displayRatio));
                window.render.setHeight(window.screen.getHeight());
                window.renderOffset.setX((window.render.getWidth() - window.screen.getWidth()));
                window.renderOffset.setY(0);
            }
        }
        else{
            window.render.setWidth(window.screen.getWidth());
            window.render.setHeight(window.screen.getHeight());
            window.renderOffset.setX(0);
            window.renderOffset.setY(0);
        }
    }

    /**
     * Initialize hi-resolution timer
     */
    void InitTimer(){
        time.setPrevious(GetTime());       // Get time as double
    }

    /**
     * Wait for some milliseconds (stop program execution)
     * @param ms Time to wait in milliseconds
     */
    void Wait(float ms){
        if(SUPPORT_BUSY_WAIT_LOOP){
            double prevTime = GetTime();
            double nextTime = 0.0;

            // Busy wait loop
            while ((nextTime - prevTime) < ms/1000.0f){
                nextTime = GetTime();
            }
        }
        else{
            if(SUPPORT_HALFBUSY_WAIT_LOOP){
                int MAX_HALFBUSY_WAIT_TIME = 4;
                double destTime = GetTime() + ms/1000;
                if (ms > MAX_HALFBUSY_WAIT_TIME) ms -= MAX_HALFBUSY_WAIT_TIME;
            }
        }
    }

    /**
     *
     Poll (store) all input events
     */
    void PollInputEvents(){
        // Reset keys/chars pressed registered
        input.keyboard.setKeyPressedQueueCount(0);
        input.keyboard.setCharPressedQueueCount(0);

        // Keyboard/Mouse input polling (automatically managed by GLFW3 through callback)

        // Register previous keys states
        for (int i = 0; i < 512; i++) input.keyboard.getPreviousKeyState()[i] = input.keyboard.getCurrentKeyState()[i];

        // Register previous mouse states
        for (int i = 0; i < 3; i++) input.mouse.getPreviousButtonState()[i] = input.mouse.getCurrentButtonState()[i];

        // Register previous mouse wheel state
        input.mouse.setPreviousWheelMove(input.mouse.getCurrentWheelMove());
        input.mouse.setCurrentWheelMove(0.0f);

        /*
        //TODO: GAMEPAD SUPPORT
        // Check if gamepads are ready
        // NOTE: We do it here in case of disconnection
        boolean[] gamepadReady = new boolean[MAX_GAMEPADS];
        for (int i = 0; i < MAX_GAMEPADS; i++){
            gamepadReady[i] = glfwJoystickPresent(i);
        }
        input.gamepad.setReady(gamepadReady);

        // Register gamepads buttons events

        for (int i = 0; i < MAX_GAMEPADS; i++) {
            if (input.gamepad.getReady()[i]) {     // Check if gamepad is available
                // Register previous gamepad states
                input.gamepad.setPreviousState(input.gamepad.getCurrentState());

                // Get current gamepad state
                // NOTE: There is no callback available, so we get it manually
                // Get remapped buttons
                GLFWgamepadstate state = { 0 };
                glfwGetGamepadState(i, &state); // This remapps all gamepads so they have their buttons mapped like an xbox controller

            const unsigned char *buttons = state.buttons;

                for (int k = 0; (buttons != NULL) && (k < GLFW_GAMEPAD_BUTTON_DPAD_LEFT + 1) && (k < MAX_GAMEPAD_BUTTONS); k++)
                {
                    GamepadButton button = -1;

                    switch (k)
                    {
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

                    if (button != -1)   // Check for valid button
                    {
                        if (buttons[k] == GLFW_PRESS)
                        {
                            input.Gamepad.currentState[i][button] = 1;
                            input.Gamepad.lastButtonPressed = button;
                        }
                        else input.Gamepad.currentState[i][button] = 0;
                    }
                }

                // Get current axis state
            const float *axes = state.axes;

                for (int k = 0; (axes != NULL) && (k < GLFW_GAMEPAD_AXIS_LAST + 1) && (k < MAX_GAMEPAD_AXIS); k++)
                {
                    input.Gamepad.axisState[i][k] = axes[k];
                }

                // Register buttons for 2nd triggers (because GLFW doesn't count these as buttons but rather axis)
                input.Gamepad.currentState[i][GAMEPAD_BUTTON_LEFT_TRIGGER_2] = (char)(input.Gamepad.axisState[i][GAMEPAD_AXIS_LEFT_TRIGGER] > 0.1);
                input.Gamepad.currentState[i][GAMEPAD_BUTTON_RIGHT_TRIGGER_2] = (char)(input.Gamepad.axisState[i][GAMEPAD_AXIS_RIGHT_TRIGGER] > 0.1);

                input.Gamepad.axisCount = GLFW_GAMEPAD_AXIS_LAST;
            }
        }*/

        window.setResizedLastFrame(false);

        if (SUPPORT_EVENTS_WAITING){
            glfwWaitEvents();
        }
        else{
            glfwPollEvents();       // Register keyboard/mouse events (callbacks)... and window events!
        }
    }

    /**
     * Copy back buffer to front buffers
     */
    void SwapBuffers(){
        glfwSwapBuffers(window.handle);
    }

    //AndroidCommandCallback
    //AndroidInputCallback
    //NOTE:No android support currently implemented

    //EmscriptenFullscreenChangeCallback
    //EmscriptenKeyboardCallback
    //EmscriptenMouseCallback
    //EmscriptenTouchCallback
    //EmscriptenGamepadCallback
    //EmscriptenWindowResizedCallback
    //NOTE:No web support currently implemented

    //RPI | DRM
    //InitKeyboard

    //ProcessKeyboard

    //RestoreKeyboard

    //InitEvdevInput

    //ConfigureEvdevDevice

    //PollKeyboardEvents

    //EventThread

    //InitGamepad

    //GamepadThread
    //END RPI | DRM

    //UWP
    //UWPIsConfigured
    //... i really don't want to type all that mess out
    //END UWP

    //DRM
    //sigh
    //...
    //END DRM
}