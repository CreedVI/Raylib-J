package com.raylib.java.core;

import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.core.input.Input;
import com.raylib.java.core.ray.Ray;
import com.raylib.java.core.rcamera.Camera2D;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Quaternion;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.rlgl.shader.Shader;
import com.raylib.java.rlgl.vr.VrDeviceInfo;
import com.raylib.java.rlgl.vr.VrStereoConfig;
import com.raylib.java.shapes.Rectangle;
import com.raylib.java.textures.Image;
import com.raylib.java.textures.RenderTexture;
import com.raylib.java.textures.Texture2D;
import com.raylib.java.utils.FileIO;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.raylib.java.Config.ConfigFlag.*;
import static com.raylib.java.Config.*;
import static com.raylib.java.core.AutomationEvent.AutomationEventType.*;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.GAMEPAD_AXIS_LEFT_TRIGGER;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.GAMEPAD_AXIS_RIGHT_TRIGGER;
import static com.raylib.java.core.input.Gamepad.GamepadButton.*;
import static com.raylib.java.core.input.Keyboard.KEY_ESCAPE;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_ARROW;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_DEFAULT;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_ORTHOGRAPHIC;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static com.raylib.java.shapes.rShapes.SetShapesTexture;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.MemoryUtil.NULL;

public class rCore{

    public RLGL rlgl;

    Window window;
    Input input;
    Time time;

    Callbacks callback;

    ArrayList<AutomationEvent> events;
    int eventCount = 0;                 // Events count
    boolean eventsPlaying = false;      // Play events
    boolean eventsRecording = false;    // Record events
    short eventsEnabled = 0b0000001111111111;    // Events enabled for checking

    String[] dirFilesPath;
    int dirFileCount;

    public int screenshotCounter;

    public int gifFrameCounter = 0; // GIF frames counter

    //Gloabls required for FPS calculation
    static int index = 0;
    static float[] history = new float[30]; //FPS_CAPTURE_FRAMES_COUNT
    static float average = 0, last = 0;

    private final Raylib context;

    public rCore(Raylib context){
        this.context = context;

        window = new Window();
        input = new Input();
        time = new Time();
        rlgl = new RLGL();

        events = new ArrayList<>();
    }

    Window getWindow(){
        return window;
    }

    Input getInput(){
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
     *
     * @param width  Window width in pixels
     * @param height Window height in pixels
     * @param title  Window title - passing null will use a default title
     */
    public void InitWindow(int width, int height, String title){
        Tracelog(LOG_INFO, "Initializing raylib " + RAYLIB_VERSION);
        Tracelog(LOG_INFO, "Supported raylib modules: ");
        Tracelog(LOG_INFO, "    > rcore:..... loaded (mandatory)");
        Tracelog(LOG_INFO, "    > rlgl:...... loaded (mandatory)");

        if(SUPPORT_MODULE_RSHAPES) {
            Tracelog(LOG_INFO, "    > rshapes:... loaded (optional)");
        }
        else {
            Tracelog(LOG_INFO, "    > rshapes:... not loaded (optional)");
        }

        if(SUPPORT_MODULE_RTEXTURES) {
            Tracelog(LOG_INFO, "    > rtextures:. loaded (optional)");
        }
        else {
            Tracelog(LOG_INFO, "    > rtextures:. not loaded (optional)");
        }

        if(SUPPORT_MODULE_RTEXT) {
            Tracelog(LOG_INFO, "    > rtext:..... loaded (optional)");
        }
        else {
            Tracelog(LOG_INFO, "    > rtext:..... not loaded (optional)");
        }

        if(SUPPORT_MODULE_RMODELS) {
            Tracelog(LOG_INFO, "    > rmodels:... loaded (optional)");
        }
        else {
            Tracelog(LOG_INFO, "    > rmodels:... not loaded (optional)");
        }

        if(SUPPORT_MODULE_RAUDIO) {
            Tracelog(LOG_INFO, "    > raudio:.... loaded (optional)");
        }
        else {
            Tracelog(LOG_INFO, "    > raudio:.... not loaded (optional)");
        }


        if(title == null || title.isEmpty()){
            title = "Raylib-J Application";
        }

        window.setTitle(title);

        // Initialize global input state
        input.keyboard.setExitKey(KEY_ESCAPE);
        input.mouse.setScale(new Vector2(1.0f, 1.0f));
        input.mouse.setCursor(MOUSE_CURSOR_ARROW.ordinal());
        input.gamepad.setLastButtonPressed(-1);

        if (SUPPORT_EVENTS_WAITING) {
            window.eventWaiting = true;
        }

        // Init graphics device (display device and OpenGL context)
        // NOTE: returns true if window and graphic device has been initialized successfully
        window.ready = InitGraphicsDevice(width, height);

        if (!window.ready){
            System.exit(-1);
            return;
        }

        // Init hi-res timer
        InitTimer();

        if (SUPPORT_MODULE_RTEXT && SUPPORT_DEFAULT_FONT){
            // Load default font
            // WARNING: External function: Module required: rtext
            context.text.LoadFontDefault();
            Rectangle rec = context.text.GetFontDefault().getRecs()[95];
            // NOTE: We set up a 1px padding on char rectangle to avoid pixel bleeding on MSAA filtering
            SetShapesTexture(context.text.GetFontDefault().getTexture(), new Rectangle(rec.getX() + 1, rec.getY() + 1,
                                                                                        rec.getWidth() - 2, rec.getHeight() - 2));
        }
        else if (SUPPORT_MODULE_RSHAPES){
            // Set default texture and rectangle to be used for shapes drawing
            // NOTE: rlgl default texture is a 1x1 pixel UNCOMPRESSED_R8G8B8A8
            Texture2D texture = new Texture2D(rlgl.rlGetTextureIdDefault(), 1, 1, 1, RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
            SetShapesTexture(texture, new Rectangle(0.0f, 0.0f, 1.0f, 1.0f));    // WARNING: Module required: rshapes
        }

        if (SUPPORT_MODULE_RTEXT && SUPPORT_MODULE_RTEXTURES) {
            if ((window.getFlags() & FLAG_WINDOW_HIGHDPI) > 0){
                // Set default font texture filter for HighDPI (blurry)
                // RL_TEXTURE_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                rlTextureParameters(context.text.GetFontDefault().texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR);
                rlTextureParameters(context.text.GetFontDefault().texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
            }
        }

        glfwShowWindow(window.handle);
    }

    /**
     * Close window and unload OpenGL context
     */
    public void CloseWindow(){
        if (SUPPORT_MODULE_RTEXT && SUPPORT_DEFAULT_FONT){
            context.text.UnloadFontDefault();        // WARNING: Module required: rtext
        }

        glfwSetWindowShouldClose(window.handle, true);
        glfwFreeCallbacks(window.handle);
        glfwDestroyWindow(window.handle);
        glfwTerminate();

        Tracelog(LOG_INFO, "Window closed successfully");
    }

    /**
     * Check if KEY_ESCAPE pressed or close icon pressed
     *
     * @return <code>true</code> if window is ready to close.
     * NOTE: Must be inverted for use in a while loop
     */
    public boolean WindowShouldClose(){
        if (window.isReady()){
            // While window minimized, stop loop execution
            while (IsWindowState(FLAG_WINDOW_MINIMIZED) && !IsWindowState(FLAG_WINDOW_ALWAYS_RUN)){
                glfwWaitEvents();
            }

            window.setShouldClose(glfwWindowShouldClose(window.handle));

            // Reset close status for next frame
            //glfwSetWindowShouldClose(window.handle, GLFW_FALSE);
            glfwSetWindowShouldClose(window.handle, false);
            return window.isShouldClose();
        }
        else{
            return true;
        }
    }

    /**
     * Check if window has been initialized successfully
     *
     * @return <code>true</code> if window was initialized successfully
     */
    public boolean IsWindowReady(){
        return window.ready;
    }

    /**
     * Check if window is currently fullscreen
     *
     * @return <code>true</code> if window is fullscreen
     */
    public boolean IsWindowFullscreen(){
        return window.fullscreen;
    }

    /**
     * Check if window is currently hidden
     *
     * @return <code>true</code> if window is hidden
     */
    public boolean IsWindowHidden(){
        return ((window.flags & FLAG_WINDOW_HIDDEN) > 0);
    }

    // Check if window has been minimized
    public boolean IsWindowMinimized(){
        return ((window.flags & FLAG_WINDOW_MINIMIZED) > 0);
    }

    // Check if window has been maximized (only PLATFORM_DESKTOP)
    public boolean IsWindowMaximized(){
        return ((window.flags & FLAG_WINDOW_MAXIMIZED) > 0);
    }

    // Check if window has the focus
    public boolean IsWindowFocused(){
        return ((window.flags & FLAG_WINDOW_UNFOCUSED) == 0);      // TODO!
    }

    // Check if window has been resizedLastFrame
    public boolean IsWindowResized(){
        return window.resizedLastFrame;
    }

    /**
     * Check if one specific window flag is enabled
     *
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
        if (!window.fullscreen){
            // Store previous window position (in case we exit fullscreen)
            glfwGetWindowPos(window.handle, new int[]{(int) window.position.x}, new int[]{(int) window.position.y});

            int monitorCount;
            PointerBuffer monitors = glfwGetMonitors();
            monitorCount = monitors.sizeof();
            int monitorIndex = GetCurrentMonitor();
            long monitor = (monitorIndex < monitorCount) ? monitors.get(monitorIndex) : -1;

            if (monitor < 0){
                Tracelog(LOG_WARNING, "GLFW: Failed to get monitor");
                window.setFullscreen(false);
                window.flags &= ~FLAG_FULLSCREEN_MODE;

                glfwSetWindowMonitor(window.handle, GetCurrentMonitor(), 0, 0, window.screen.getWidth(),
                                     window.screen.getHeight(), GLFW_DONT_CARE); // NOTE: Resizing not allowed by default!
                return;
            }
            else {
                window.setFullscreen(true);
                window.flags |= FLAG_FULLSCREEN_MODE;

                glfwSetWindowMonitor(window.handle, monitor, 0, 0, window.screen.getWidth(), window.screen.getHeight(), GLFW_DONT_CARE);
            }
        }
        else{
            window.setFullscreen(false);
            window.flags &= ~FLAG_FULLSCREEN_MODE;

            glfwSetWindowMonitor(window.handle, 0, (int) window.position.getX(), (int) window.position.getY(),
                                 window.screen.getWidth(), window.screen.getHeight(), GLFW_DONT_CARE);
        }

        // Try to enable GPU V-Sync, so frames are limited to screen refresh rate (60Hz -> 60 FPS)
        // NOTE: V-Sync can be enabled by graphic driver configuration
        if ((window.flags & FLAG_VSYNC_HINT) == 1){
            glfwSwapInterval(1);
        }
    }

    /**
     * Set window state: maximized, if resizable (only PLATFORM_DESKTOP)
     */
    public void MaximizeWindow(){
        if (glfwGetWindowAttrib(window.handle, GLFW_RESIZABLE) == GLFW_TRUE){
            glfwMaximizeWindow(window.handle);
            window.flags |= FLAG_WINDOW_MAXIMIZED;
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
            window.flags &= ~FLAG_WINDOW_MINIMIZED;
            window.flags &= ~FLAG_WINDOW_MAXIMIZED;
        }
    }

    // Set window configuration state using flags
    public void SetWindowState(int flags){
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
            Tracelog(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window " +
                    "initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI) != (flags & FLAG_WINDOW_HIGHDPI)) && ((flags & FLAG_WINDOW_HIGHDPI) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_MOUSE_PASSTHROUGH

        if (((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) != (flags & FLAG_WINDOW_MOUSE_PASSTHROUGH)) && ((flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
            window.flags |= FLAG_WINDOW_MOUSE_PASSTHROUGH;
        }



        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT) != (flags & FLAG_MSAA_4X_HINT)) && ((flags & FLAG_MSAA_4X_HINT) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT) != (flags & FLAG_INTERLACED_HINT)) && ((flags & FLAG_INTERLACED_HINT) > 0)){
            Tracelog(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Clear window configuration state flags
    public void ClearWindowState(int flags){
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
            Tracelog(LOG_WARNING, "WINDOW: Framebuffer transparency can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_HIGHDPI
        if (((window.flags & FLAG_WINDOW_HIGHDPI) > 0) && ((flags & FLAG_WINDOW_HIGHDPI) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: High DPI can only by configured before window initialization");
        }

        // State change: FLAG_WINDOW_MOUSE_PASSTHROUGH
        if (((window.flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0) && ((flags & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0)) {
            glfwSetWindowAttrib(window.handle, GLFW_MOUSE_PASSTHROUGH, GLFW_FALSE);
            window.flags &= ~FLAG_WINDOW_MOUSE_PASSTHROUGH;
        }

        // State change: FLAG_MSAA_4X_HINT
        if (((window.flags & FLAG_MSAA_4X_HINT) > 0) && ((flags & FLAG_MSAA_4X_HINT) > 0)){
            Tracelog(LOG_WARNING, "WINDOW: MSAA can only by configured before window initialization");
        }

        // State change: FLAG_INTERLACED_HINT
        if (((window.flags & FLAG_INTERLACED_HINT) > 0) && ((flags & FLAG_INTERLACED_HINT) > 0)){
            Tracelog(LOG_WARNING, "RPI: Interlaced mode can only by configured before window initialization");
        }
    }

    // Set icon for window (only PLATFORM_DESKTOP)
    // NOTE: Image must be in RGBA format, 8bit per channel
    public void SetWindowIcon(Image image){
        if (image.getFormat() == RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8){
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
            Tracelog(LOG_WARNING, "GLFW: window icon image must be in R8G8B8A8 pixel format");
        }
    }

    // Set title for window (only PLATFORM_DESKTOP)
    public void SetWindowTitle(String title){
        window.title = title;
        glfwSetWindowTitle(window.handle, title);
    }

    // Set window position on screen (windowed mode)
    public void SetWindowPosition(int x, int y){
        glfwSetWindowPos(window.handle, x, y);
    }

    // Set monitor for the current window (fullscreen mode)
    public void SetWindowMonitor(long monitor){
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
    public void SetWindowMinSize(int width, int height){
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowSizeLimits(window.handle, width, height, mode.width(), mode.height());
    }

    // Set window dimensions
    public void SetWindowSize(int width, int height){
        glfwSetWindowSize(window.handle, width, height);
    }

    // Set window opacity, value opacity is between 0.0 and 1.0

    public void SetWindowOpacity(float opacity) {
        if(PLATFORM_DESKTOP) {
            if (opacity >= 1.0f) {
                opacity = 1.0f;
            }
            else if (opacity <= 0.0f) {
                opacity = 0.0f;
            }

            glfwSetWindowOpacity(window.handle, opacity);
        }

    }

    /**
     * Get current screen width
     *
     * @return Width of current window
     */
    public int GetScreenWidth(){
        return window.currentFbo.getWidth();
    }

    /**
     * Get current screen height
     *
     * @return Height of current window
     */
    public int GetScreenHeight(){
        return window.currentFbo.getHeight();
    }

    // Get native window handle
    public long GetWindowHandle(){
        if (__WINDOWS__){
            return glfwGetWin32Window(window.handle);
        }
        else{
            return 0;
        }
    }

    // Get number of monitors
    public int GetMonitorCount(){
        int monitorCount = 0;
        PointerBuffer pb = glfwGetMonitors();
        monitorCount = pb.sizeof();
        return monitorCount;
    }

    // Get number of monitors
    public int GetCurrentMonitor(){
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

    // Get selected monitor position
    public Vector2 GetMonitorPosition(int monitor){
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
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return null;
    }

    // Get selected monitor width (currently used by monitor)
    public int GetMonitorWidth(int monitor){
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
                Tracelog(LOG_WARNING, "GLFW: Failed to find video mode for selected monitor");
            }
        }
        else{
            Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
        }
        return 0;
    }

    // Get selected monitor height (currently used by monitor)
    public int GetMonitorHeight(int monitor){
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
    public int GetMonitorPhysicalWidth(int monitor){
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
    public int GetMonitorPhysicalHeight(int monitor){
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

    int GetMonitorRefreshRate(int monitor) {
        if (PLATFORM_DESKTOP) {
            PointerBuffer monitors = glfwGetMonitors();
            int monitorCount = monitors.sizeof();

            if ((monitor >= 0) && (monitor < monitorCount)) {
                GLFWVidMode vidmode = glfwGetVideoMode(monitors.get(monitor));
                return vidmode.refreshRate();
            }
            else {
                Tracelog(LOG_WARNING, "GLFW: Failed to find selected monitor");
            }
        }
        /*
        if (PLATFORM_DRM) {
            if ((CORE.Window.connector) && (CORE.Window.modeIndex >= 0)) {
                return CORE.Window.connector->modes[CORE.Window.modeIndex].vrefresh;
            }
        }
        */
        return 0;
    }

    // Get window position XY on monitor
    public Vector2 GetWindowPosition() {
        IntBuffer x = IntBuffer.allocate(1);
        IntBuffer y = IntBuffer.allocate(1);
        if(PLATFORM_DESKTOP) {
            glfwGetWindowPos(window.handle, x, y);
        }
        return new Vector2(x.get(0), y.get(0));
    }

    // Get window scale DPI factor
    public Vector2 GetWindowScaleDPI() {
        Vector2 scale = new Vector2(1f,1f);

        if(PLATFORM_DESKTOP) {
            FloatBuffer xdpi = FloatBuffer.allocate(1);
            FloatBuffer ydpi = FloatBuffer.allocate(1);
            Vector2 windowPos = GetWindowPosition();

            PointerBuffer monitors = glfwGetMonitors();
            int monitorCount = monitors.sizeof();

            // Check window monitor
            for (int i = 0; i < monitorCount; i++) {
                glfwGetMonitorContentScale(monitors.get(i), xdpi, ydpi);

                IntBuffer xpos, ypos, width, height;
                xpos = IntBuffer.allocate(1);
                ypos = IntBuffer.allocate(1);
                width = IntBuffer.allocate(1);
                height = IntBuffer.allocate(1);

                glfwGetMonitorWorkarea(monitors.get(i), xpos, ypos, width, height);

                if ((windowPos.x >= xpos.get(0)) && (windowPos.x < xpos.get(0) + width.get(0)) &&
                        (windowPos.y >= ypos.get(0)) && (windowPos.y < ypos.get(0) + height.get(0))) {
                    scale.x = xdpi.get(i);
                    scale.y = ydpi.get(i);
                    break;
                }
            }
        }

        return scale;
    }

    // Get the human-readable, UTF-8 encoded name of the primary monitor
    public String GetMonitorName(int monitor){
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
    public String GetClipboardText(){
        return glfwGetClipboardString(window.handle);
    }

    // Set clipboard text content
    public void SetClipboardText(String text){
        glfwSetClipboardString(window.handle, text);
    }

    // Enable waiting for events on EndDrawing(), no automatic event polling

    public void EnableEventWaiting() {
        window.eventWaiting = true;
    }

    // Disable waiting for events on EndDrawing(), automatic events polling
    public void DisableEventWaiting() {
        window.eventWaiting = false;
    }

    // Show mouse cursor
    public void ShowCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        input.mouse.setCursorHidden(false);
    }

    // Hides mouse cursor
    public void HideCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        input.mouse.setCursorHidden(true);
    }

    // Check if cursor is not visible
    public boolean IsCursorHidden(){
        return input.mouse.isCursorHidden();
    }

    // Enables cursor (unlock cursor)
    public void EnableCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        input.mouse.setCursorHidden(false);
    }

    // Disables cursor (lock cursor)
    public void DisableCursor(){
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        input.mouse.setCursorHidden(true);
    }

    /**
     * @return <code>true</code> if cursor is within window bounds
     */
    // Check if cursor is on the current screen.
    public boolean IsCursorOnScreen(){
        return input.mouse.isCursorOnScreen();
    }

    /**
     * Clear window background
     *
     * @param color Color to fill the background
     */
    public void ClearBackground(Color color){
        RLGL.rlClearColor(color.getR(), color.getG(), color.getB(), color.getA());   // Set clear color
        RLGL.rlClearScreenBuffers();                             // Clear current framebuffers
    }

    /**
     * Setup canvas (framebuffer) to start drawing
     */
    public void BeginDrawing(){
        // WARNING: Previously to BeginDrawing() other render textures drawing could happen,
        // consequently the measure for update vs draw is not accurate (only the total frame time is accurate)

        time.current = GetTime();      // Number of elapsed seconds since InitTimer()
        time.update = time.current - time.previous;
        time.previous = time.current;

        rlLoadIdentity();                   // Reset current matrix (modelview)
        rlMultMatrixf(MatrixToFloat(window.screenScale)); // Apply screen scaling

        //rlTranslatef(0.375, 0.375, 0);    // HACK to have 2D pixel-perfect drawing on OpenGL 1.1
        // NOTE: Not required with OpenGL 3.3+

    }

    /**
     * End canvas drawing and swap buffers (double buffering)
     */
    public void EndDrawing(){
        rlgl.rlDrawRenderBatchActive();      // Update and draw internal render batch

        if(SUPPORT_GIF_RECORDING) {
            // Draw record indicator
            /*TODO
            if (gifRecording) {
            #define GIF_RECORD_FRAMERATE    10
                gifFrameCounter++;

                // NOTE: We record one gif frame every 10 game frames
                if ((gifFrameCounter%GIF_RECORD_FRAMERATE) == 0)
                {
                    // Get image data for the current frame (from backbuffer)
                    // NOTE: This process is quite slow... :(
                    unsigned char *screenData = rlReadScreenPixels(CORE.Window.screen.width, CORE.Window.screen.height);
                    msf_gif_frame(&gifState, screenData, 10, 16, CORE.Window.screen.width*4);

                    RL_FREE(screenData);    // Free image data
                }

                if (((gifFrameCounter/15)%2) == 1)
                {
                    DrawCircle(30, CORE.Window.screen.height - 20, 10, MAROON);
                    DrawText("GIF RECORDING", 50, CORE.Window.screen.height - 25, 10, RED);
                }

                rlDrawRenderBatchActive();  // Update and draw internal render batch
            }*/
        }

        if(SUPPORT_EVENTS_AUTOMATION) {
            // Draw record/play indicator
            /* TODO
            if (eventsRecording) {
                gifFrameCounter++;

                if (((gifFrameCounter/15)%2) == 1) {
                    DrawCircle(30, CORE.Window.screen.height - 20, 10, MAROON);
                    DrawText("EVENTS RECORDING", 50, CORE.Window.screen.height - 25, 10, RED);
                }

                rlDrawRenderBatchActive();  // Update and draw internal render batch
            }
            else if (eventsPlaying) {
                gifFrameCounter++;

                if (((gifFrameCounter/15)%2) == 1) {
                    DrawCircle(30, CORE.Window.screen.height - 20, 10, LIME);
                    DrawText("EVENTS PLAYING", 50, CORE.Window.screen.height - 25, 10, GREEN);
                }

                rlgl.rlDrawRenderBatchActive();  // Update and draw internal render batch
            }*/
        }

        if(!SUPPORT_CUSTOM_FRAME_CONTROL) {
            SwapScreenBuffer();                  // Copy back buffer to front buffer (screen)

            // Frame time control system
            time.current = GetTime();
            time.draw = time.current - time.previous;
            time.previous = time.current;

            time.frame = time.update + time.draw;

            // Wait for some milliseconds...
            if (time.frame < time.target) {
                WaitTime((float) (time.target - time.frame));

                time.current = GetTime();
                double waitTime = time.current - time.previous;
                time.previous = time.current;

                time.frame += waitTime;    // Total frame time: update + draw + wait
            }

            PollInputEvents();      // Poll user events (before next frame update)
        }

        if(SUPPORT_EVENTS_AUTOMATION) {
            // Events recording and playing logic
            if (eventsRecording) {
                RecordAutomationEvent(time.frameCounter);
            }
            else if (eventsPlaying) {
                // TODO: When should we play? After/before/replace PollInputEvents()?
                if (time.frameCounter >= eventCount) eventsPlaying = false;
                PlayAutomationEvent(time.frameCounter);
            }
        }

        time.frameCounter++;
    }

    // Initialize 2D mode with custom camera (2D)
    public void BeginMode2D(Camera2D camera){
        rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        RLGL.rlLoadIdentity();                   // Reset current matrix (modelview)

        // Apply 2d camera transformation to modelview
        RLGL.rlMultMatrixf(MatrixToFloat(GetCameraMatrix2D(camera)));

        // Apply screen scaling if required
        RLGL.rlMultMatrixf(MatrixToFloat(window.getScreenScale()));
    }

    // Ends 2D mode with custom camera
    public void EndMode2D(){
        rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        RLGL.rlLoadIdentity();                   // Reset current matrix (modelview)
        RLGL.rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling if required
    }

    // Initializes 3D mode with custom camera (3D)
    public void BeginMode3D(Camera3D camera){
        rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        RLGL.rlMatrixMode(RLGL.RL_PROJECTION);        // Switch to projection matrix
        RLGL.rlPushMatrix();                     // Save previous matrix, which contains the settings for the 2d ortho projection
        RLGL.rlLoadIdentity();                   // Reset current matrix (projection)

        float aspect = (float) window.currentFbo.getWidth() / (float) window.currentFbo.getHeight();

        if (camera.projection == CAMERA_PERSPECTIVE){
            // Setup perspective projection
            double top = RL_CULL_DISTANCE_NEAR * Math.tan(camera.getFovy() * 0.5 * DEG2RAD);
            double right = top * aspect;

            RLGL.rlFrustum(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);

        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
            // Setup orthographic projection
            double top = camera.getFovy() / 2.0;
            double right = top * aspect;

            RLGL.rlOrtho(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }

        // NOTE: zNear and zFar values are important when computing depth buffer values

        RLGL.rlMatrixMode(RLGL.RL_MODELVIEW);         // Switch back to modelview matrix
        RLGL.rlLoadIdentity();                   // Reset current matrix (modelview)

        // Setup rCamera view
        Matrix matView = MatrixLookAt(camera.getPosition(), camera.getTarget(), camera.getUp());
        RLGL.rlMultMatrixf(MatrixToFloat(matView));      // Multiply modelview matrix by view matrix (camera)
        rlgl.rlEnableDepthTest();                // Enable DEPTH_TEST for 3D
    }

    // Ends 3D mode and returns to default 2D orthographic mode
    public void EndMode3D(){
        rlgl.rlDrawRenderBatchActive();                         // Process internal buffers (update + draw)

        RLGL.rlMatrixMode(RLGL.RL_PROJECTION);        // Switch to projection matrix
        RLGL.rlPopMatrix();                      // Restore previous matrix (projection) from matrix stack

        RLGL.rlMatrixMode(RLGL.RL_MODELVIEW);         // Switch back to modelview matrix
        RLGL.rlLoadIdentity();                   // Reset current matrix (modelview)

        RLGL.rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling if required

        rlgl.rlDisableDepthTest();               // Disable DEPTH_TEST for 2D
    }

    // Initializes render texture for drawing
    public void BeginTextureMode(RenderTexture target){
        rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        RLGL.rlEnableFramebuffer(target.getId());     // Enable render target

        // Set viewport and RLGL internal framebuffer size
        RLGL.rlViewport(0, 0, target.texture.width, target.texture.height);
        rlSetFramebufferWidth(target.texture.width);
        rlSetFramebufferHeight(target.texture.height);

        RLGL.rlMatrixMode(RLGL.RL_PROJECTION);        // Switch to projection matrix
        RLGL.rlLoadIdentity();                   // Reset current matrix (projection)

        // Set orthographic projection to current framebuffer size
        // NOTE: Configured top-left corner as (0, 0)
        RLGL.rlOrtho(0, target.texture.width, target.texture.height, 0, 0.0f, 1.0f);

        RLGL.rlMatrixMode(RLGL.RL_MODELVIEW);         // Switch back to modelview matrix
        RLGL.rlLoadIdentity();                   // Reset current matrix (modelview)

        //rlScalef(0.0f, -1.0f, 0.0f);      // Flip Y-drawing (?)

        // Setup current width/height for proper aspect ratio
        // calculation when using BeginMode3D()
        window.currentFbo.setWidth(target.texture.width);
        window.currentFbo.setHeight(target.texture.height);
    }

    // Ends drawing to render texture
    public void EndTextureMode(){
        rlgl.rlDrawRenderBatchActive();                 // Draw Buffers (Only OpenGL 3+ and ES2)

        RLGL.rlDisableFramebuffer();     // Disable render target (fbo)

        // Set viewport to default framebuffer size
        SetupViewport(window.render.width, window.render.height);

        // Reset current screen size
        window.currentFbo.width = window.render.width;
        window.currentFbo.height = window.render.height;
    }

    // Begin custom shader mode
    public void BeginShaderMode(Shader shader){
        rlgl.rlSetShader(shader.getId(), shader.getLocs());
    }

    // End custom shader mode (returns to default shader)
    public void EndShaderMode(){
        rlgl.rlSetShader(rlgl.rlGetShaderIdDefault(), rlgl.rlGetShaderLocsDefault());
    }

    // Begin blending mode (alpha, additive, multiplied)
    // NOTE: Only 3 blending modes supported, default blend mode is alpha
    public void BeginBlendMode(int mode){
        rlgl.rlSetBlendMode(mode);
    }

    // End blending mode (reset to default: alpha blending)
    public void EndBlendMode(){
        rlgl.rlSetBlendMode(rlBlendMode.RL_BLEND_ALPHA);
    }

    // Begin scissor mode (define screen area for following drawing)
    // NOTE: Scissor rec refers to bottom-left corner, we change it to upper-left
    public void BeginScissorMode(int x, int y, int width, int height){
        rlgl.rlDrawRenderBatchActive(); // Force drawing elements

        rlgl.rlEnableScissorTest();

        if (__APPLE__) {
            Vector2 scale = GetWindowScaleDPI();

            rlgl.rlScissor((int)(x*scale.x), (int)(GetScreenHeight()*scale.y - (((y + height)*scale.y))), (int)(width*scale.x), (int)(height*scale.y));
        }
        else {
            if ((window.flags & FLAG_WINDOW_HIGHDPI) > 0) {
                Vector2 scale = GetWindowScaleDPI();
                rlgl.rlScissor((int)(x*scale.x), (int)(window.currentFbo.height - (y + height)*scale.y), (int)(width*scale.x), (int)(height*scale.y));
            }
            else {
                rlgl.rlScissor(x, window.currentFbo.height - (y + height), width, height);
            }
        }

    }

    // End scissor mode
    public void EndScissorMode(){
        rlgl.rlDrawRenderBatchActive(); // Force drawing elements
        rlgl.rlDisableScissorTest();
    }

    // Begin VR drawing configuration
    public void BeginVrStereoMode(VrStereoConfig config){
        rlgl.rlEnableStereoRenderer();

        // Set stereo render matrices
        RLGL.rlSetMatrixProjectionStereo(config.projection[0], config.projection[1]);
        RLGL.rlSetMatrixViewOffsetStereo(config.viewOffset[0], config.viewOffset[1]);

    }

    // End VR drawing process (and desktop mirror)
    public void EndVrStereoMode(){
        rlgl.rlDisableStereoRenderer();
    }

    // Load VR stereo config for VR simulator device parameters
    public VrStereoConfig LoadVrStereoConfig(VrDeviceInfo device){
        VrStereoConfig config = new VrStereoConfig();

        if (RLGL.GRAPHICS_API_OPENGL_33 || RLGL.GRAPHICS_API_OPENGL_ES2){
            // Compute aspect ratio
            float aspect = ((float) device.gethResolution() * 0.5f) / (float) device.getvResolution();

            // Compute lens parameters
            float lensShift = (device.hScreenSize * 0.25f - device.lensSeparationDistance * 0.5f) / device.hScreenSize;
            config.leftLensCenter[0] = 0.25f + lensShift;
            config.leftLensCenter[1] = 0.5f;
            config.rightLensCenter[0] = 0.75f - lensShift;
            config.rightLensCenter[1] = 0.5f;
            config.leftScreenCenter[0] = 0.25f;
            config.leftScreenCenter[1] = 0.5f;
            config.rightScreenCenter[0] = 0.75f;
            config.rightScreenCenter[1] = 0.5f;

            // Compute distortion scale parameters
            // NOTE: To get lens max radius, lensShift must be normalized to [-1..1]
            float lensRadius = Math.abs(-1.0f - 4.0f * lensShift);
            float lensRadiusSq = lensRadius * lensRadius;
            float distortionScale = device.lensDistortionValues[0] +
                    device.lensDistortionValues[1] * lensRadiusSq +
                    device.lensDistortionValues[2] * lensRadiusSq * lensRadiusSq +
                    device.lensDistortionValues[3] * lensRadiusSq * lensRadiusSq * lensRadiusSq;

            float normScreenWidth = 0.5f;
            float normScreenHeight = 1.0f;
            config.scaleIn[0] = 2.0f / normScreenWidth;
            config.scaleIn[1] = 2.0f / normScreenHeight / aspect;
            config.scale[0] = normScreenWidth * 0.5f / distortionScale;
            config.scale[1] = normScreenHeight * 0.5f * aspect / distortionScale;

            // Fovy is normally computed with: 2*atan2f(device.vScreenSize, 2*device.eyeToScreenDistance)
            // ...but with lens distortion it is increased (see Oculus SDK Documentation)
            float fovy = (float) (2.0f*Math.atan2(device.vScreenSize*0.5f*distortionScale, device.eyeToScreenDistance));     // Really need distortionScale?
            //float fovy = 2.0f * (float) Math.atan2(device.vScreenSize * 0.5f, device.eyeToScreenDistance);

            // Compute camera projection matrices
            float projOffset = 4.0f * lensShift;      // Scaled to projection space coordinates [-1..1]
            Matrix proj = MatrixPerspective(fovy, aspect, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);

            config.projection[0] = MatrixMultiply(proj, MatrixTranslate(projOffset, 0.0f, 0.0f));
            config.projection[1] = MatrixMultiply(proj, MatrixTranslate(-projOffset, 0.0f, 0.0f));

            // Compute camera transformation matrices
            // NOTE: rCamera movement might seem more natural if we model the head.
            // Our axis of rotation is the base of our head, so we might want to add
            // some y (base of head to eye level) and -z (center of head to eye protrusion) to the camera positions.
            config.viewOffset[0] = MatrixTranslate(-device.interpupillaryDistance * 0.5f, 0.075f, 0.045f);
            config.viewOffset[1] = MatrixTranslate(device.interpupillaryDistance * 0.5f, 0.075f, 0.045f);

            // Compute eyes Viewports
            /*
            config.eyeViewportRight[0] = 0;
            config.eyeViewportRight[1] = 0;
            config.eyeViewportRight[2] = device.hResolution/2;
            config.eyeViewportRight[3] = device.vResolution;

            config.eyeViewportLeft[0] = device.hResolution/2;
            config.eyeViewportLeft[1] = 0;
            config.eyeViewportLeft[2] = device.hResolution/2;
            config.eyeViewportLeft[3] = device.vResolution;
            */
        }
        else{
            Tracelog(LOG_WARNING, "RLGL: VR Simulator not supported on OpenGL 1.1");
        }

        return config;
    }

    // Unload VR stereo config properties
    public void UnloadVrStereoConfig(VrStereoConfig config){
        //...
    }

    public Shader LoadShader(String vsFileName, String fsFileName){
        Shader shader = new Shader();

        String vShaderStr = null;
        String fShaderStr = null;

        if (vsFileName != null){
            try{
                vShaderStr = FileIO.LoadFileText(vsFileName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        if (fsFileName != null){
            try{
                fShaderStr = FileIO.LoadFileText(fsFileName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        shader = LoadShaderFromMemory(vShaderStr, fShaderStr);

        return shader;
    }

    // Load shader from code strings and bind default locations
    public Shader LoadShaderFromMemory(String vsCode, String fsCode){
        Shader shader = new Shader();
        shader.locs = new int[RLGL.MAX_SHADER_LOCATIONS];

        // NOTE: All locations must be reseted to -1 (no location)
        for (int i = 0; i < RL_MAX_SHADER_LOCATIONS; i++) shader.locs[i] = -1;

        shader.setId(rlgl.rlLoadShaderCode(vsCode, fsCode));

        // After shader loading, we TRY to set default location names
        if (shader.getId() > 0){
            // Default shader attrib locations have been fixed before linking:
            //          vertex position location    = 0
            //          vertex texcoord location    = 1
            //          vertex normal location      = 2
            //          vertex color location       = 3
            //          vertex tangent location     = 4
            //          vertex texcoord2 location   = 5

            // NOTE: If any location is not found, loc point becomes -1

            // Get handles to GLSL input attibute locations
            shader.locs[RL_SHADER_LOC_VERTEX_POSITION] = rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION);
            shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD01] = rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
            shader.locs[RL_SHADER_LOC_VERTEX_TEXCOORD02] = rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2);
            shader.locs[RL_SHADER_LOC_VERTEX_NORMAL] = rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL);
            shader.locs[RL_SHADER_LOC_VERTEX_TANGENT] = rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT);
            shader.locs[RL_SHADER_LOC_VERTEX_COLOR] = rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR);

            // Get handles to GLSL uniform locations (vertex shader)
            shader.locs[RL_SHADER_LOC_MATRIX_MVP] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_MVP);
            shader.locs[RL_SHADER_LOC_MATRIX_VIEW] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_VIEW);
            shader.locs[RL_SHADER_LOC_MATRIX_PROJECTION] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_PROJECTION);
            shader.locs[RL_SHADER_LOC_MATRIX_MODEL] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_MODEL);
            shader.locs[RL_SHADER_LOC_MATRIX_NORMAL] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_NORMAL);

            // Get handles to GLSL uniform locations (fragment shader)
            shader.locs[RL_SHADER_LOC_COLOR_DIFFUSE] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_COLOR);
            shader.locs[RL_SHADER_LOC_MAP_DIFFUSE] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE0);  // SHADER_LOC_MAP_ALBEDO
            shader.locs[RL_SHADER_LOC_MAP_SPECULAR] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE1); // SHADER_LOC_MAP_METALNESS
            shader.locs[RL_SHADER_LOC_MAP_NORMAL] = rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE2);
        }

        return shader;
    }

    // Unload shader from GPU memory (VRAM)
    public static void UnloadShader(Shader shader){
        if (shader.getId() != rlGetShaderIdDefault()){
            rlUnloadShaderProgram(shader.getId());
            shader.setLocs(null);
        }
    }

    // Get shader uniform location
    public static int GetShaderLocation(Shader shader, String uniformName){
        return RLGL.rlGetLocationUniform(shader.getId(), uniformName);
    }

    // Get shader attribute location
    public int GetShaderLocationAttrib(Shader shader, String attribName){
        return rlgl.rlGetLocationAttrib(shader.id, attribName);
    }

    // Set shader uniform value
    public static void SetShaderValue(Shader shader, int locIndex, float[] value, int uniformType){
        SetShaderValueV(shader, locIndex, value, uniformType, 1);
    }

    // Set shader uniform value vector
    public static void SetShaderValueV(Shader shader, int locIndex, float[] value, int uniformType, int count){
        RLGL.rlEnableShader(shader.getId());
        RLGL.rlSetUniform(locIndex, value, uniformType, count);
        //rlDisableShader();      // Avoid reseting current shader program, in case other uniforms are set
    }

    // Set shader uniform value (matrix 4x4)
    public void SetShaderValueMatrix(Shader shader, int locIndex, Matrix mat){
        rlEnableShader(shader.getId());
        rlgl.rlSetUniformMatrix(locIndex, mat);
        //rlDisableShader();
    }

    // Set shader uniform value for texture
    public void SetShaderValueTexture(Shader shader, int locIndex, Texture2D texture){
        rlEnableShader(shader.getId());
        rlgl.rlSetUniformSampler(locIndex, texture.getId());
        //rlDisableShader();
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

        if (camera.projection == CAMERA_PERSPECTIVE){
            // Calculate projection matrix from perspective
            matProj = MatrixPerspective(camera.getFovy() * DEG2RAD,
                                        ((double) GetScreenWidth() / (double) GetScreenHeight()), RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
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

        if (camera.projection == CAMERA_PERSPECTIVE){
            ray.position = camera.getPosition();
        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
            ray.position = cameraPlanePointerPos;
        }

        // Apply calculated vectors to ray
        ray.direction = direction;

        return ray;
    }

    // Get transform matrix for camera
    public Matrix GetCameraMatrix(Camera3D camera){
        return MatrixLookAt(camera.getPosition(), camera.getTarget(), camera.getUp());
    }

    /**
     * Returns camera 2d transform matrix
     *
     * @param camera
     * @return Transform matrix
     */
    public Matrix GetCameraMatrix2D(Camera2D camera){
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
    public Vector2 GetWorldToScreen(Vector3 position, Camera3D camera){

        return GetWorldToScreenEx(position, camera, GetScreenWidth(), GetScreenHeight());
    }

    // Returns size position for a 3d world space position (useful for texture drawing)
    public Vector2 GetWorldToScreenEx(Vector3 position, Camera3D camera, int width, int height){
        // Calculate projection matrix (from perspective instead of frustum
        Matrix matProj = MatrixIdentity();

        if (camera.projection == CAMERA_PERSPECTIVE){
            // Calculate projection matrix from perspective
            matProj = MatrixPerspective(camera.getFovy() * DEG2RAD, ((double) width / (double) height),
                                        RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
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
    public Vector2 GetWorldToScreen2D(Vector2 position, Camera2D camera){
        Matrix matCamera = GetCameraMatrix2D(camera);
        Vector3 transform = Vector3Transform(new Vector3(position.getX(), position.getY(), 0), matCamera);

        return new Vector2(transform.getX(), transform.getY());
    }

    // Returns the world space position for a 2d camera screen space position
    public Vector2 GetScreenToWorld2D(Vector2 position, Camera2D camera){
        Matrix invMatCamera = MatrixInvert(GetCameraMatrix2D(camera));
        Vector3 transform = Vector3Transform(new Vector3(position.getX(), position.getY(), 0), invMatCamera);

        return new Vector2(transform.getX(), transform.getY());
    }

    /**
     * Set target FPS (maximum)
     *
     * @param fps FPS limit
     */
    public void SetTargetFPS(int fps){
        if (fps < 1){
            time.setTarget(0.0f);
        }
        else{
            time.setTarget(1.0 / (double) fps);
        }

        Tracelog(LOG_INFO, "TIMER: Target time per frame: " + time.getTarget() * 1000.0f + " milliseconds");
    }

    /**
     * Returns current FPS
     * NOTE: We calculate an average framerate
     *
     * @return Current average framerate
     */
    public int GetFPS(){

        int FPS_CAPTURE_FRAMES_COUNT = 30;      // 30 captures
        float FPS_AVERAGE_TIME_SECONDS = 0.5f;     // 500 millisecondes
        float FPS_STEP = (FPS_AVERAGE_TIME_SECONDS / FPS_CAPTURE_FRAMES_COUNT);

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
     *
     * @return Seconds taken for last frame
     */
    public float GetFrameTime(){
        return (float) time.getFrame();
    }

    /**
     * Get elapsed time measure in seconds since InitTimer()
     * NOTE: On PLATFORM_DESKTOP InitTimer() is called on InitWindow()
     * NOTE: On PLATFORM_DESKTOP, timer is initialized on glfwInit()
     *
     * @return Time program has been running in seconds
     */
    public double GetTime(){
        return glfwGetTime();
    }

    // Setup window configuration flags (view FLAGS)
    // NOTE: This function is expected to be called before window creation,
    // because it sets up some flags for the window creation process.
    // To configure window states after creation, just use SetWindowState()
    public void SetConfigFlags(int flags){
        // Selected flags are set but not evaluated at this point,
        // flag evaluation happens at InitWindow() or SetWindowState()
        window.flags |= flags;
    }

    // Takes a screenshot of current screen (saved a .png)
    // NOTE: This function could work in any platform but some platforms: PLATFORM_ANDROID and PLATFORM_WEB
    // have their own internal file-systems, to dowload image to user file-system some additional mechanism is required
    public void TakeScreenshot(String fileName){
        if (SUPPORT_MODULE_RTEXTURES) {
            Vector2 scale = GetWindowScaleDPI();

            short[] imgData = rlgl.rlReadScreenPixels((int)((float)window.render.width*scale.x), (int)((float)window.render.height*scale.y));
            byte[] dataB = new byte[imgData.length];
            IntStream.range(0, dataB.length).forEach(i -> dataB[i] = (byte) imgData[i]);
            Image image = new Image(dataB, window.render.width, window.render.height, 1,
                                    RL_PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);

            String path = "" + fileName;

            context.textures.ExportImage(image, path); // WARNING: Module required: rtextures

            // TODO: Verification required for log
            Tracelog(LOG_INFO, "SYSTEM: [" + path + "] Screenshot taken successfully");
        }
        else {
            Tracelog(LOG_WARNING,"IMAGE: ExportImage() requires module: rtextures");
        }
    }

    /**
     * Returns a random value between min and max (both included)
     *
     * @param min Minimum value of random number
     * @param max Maximum value of random number
     * @return Random value between the <code>min/code> and <code>max</code>
     */
    public int GetRandomValue(int min, int max){
        if (min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }

        return (int) (Math.random() * (max - min + 1) + min);
    }

    // Check if the file exists
    public boolean FileExists(String fileName){
        File file = new File(fileName);

        return file.exists();
    }

    // Check file extension
    // NOTE: Extensions checking is not case-sensitive
    public static boolean IsFileExtension(String fileName, String ext){
        String fileExt = GetFileExtension(fileName);
        return fileExt.equals(ext);
    }

    public boolean DirectoryExists(String fileName){
        File tmp = new File(fileName);
        return tmp.isDirectory();
    }

    //Get file length in byres
    public int GetFileLength(String fileName) {
        File tmp = new File(fileName);
        return (int) tmp.length();
    }

    public static String GetFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    //TODO: FILE Utils
    // StrPrBrk

    // Get filename for a path string
    public String GetFileName(String filePath){
        filePath = filePath.replace('\\', '/');

        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf('/'));
        }
        else {
            return filePath;
        }
    }

    // Get filename string without extension (uses static string)
    public static String GetFileNameWithoutExt(String filePath){

        filePath = filePath.replace('\\', '/');

        return filePath.substring(filePath.lastIndexOf('/'), filePath.lastIndexOf('.'));
    }

    // Get directory for a given filePath
    public static String GetDirectoryPath(String filePath) {
        String dirPath = "";

        if (filePath.contains("\\")) {
            dirPath = filePath.substring(0, filePath.lastIndexOf("\\"));
        }
        else if (filePath.contains("/")) {
            dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        }

        return dirPath;
    }

    // Get previous directory path for a given path
    public String GetPrevDirectoryPath(String dirPath) {
        String prevDirPath = "";

        if (dirPath.contains("\\")) {
            prevDirPath = dirPath.substring(0, dirPath.lastIndexOf("\\"));
        }
        else if (dirPath.contains("/")) {
            prevDirPath = dirPath.substring(0, dirPath.lastIndexOf("/"));
        }

        return prevDirPath;
    }

    /**
     * Get current working directory
     * @return Current working directory
     */
    public String GetWorkingDirectory() {
        return Paths.get("").toAbsolutePath() + "/";
    }

    // Get filenames in a directory path (max 512 files)
    public String[] LoadDirectoryFiles(String dirPath) {

        final int MAX_DIRECTORY_FILES = 512;

        UnloadDirectoryFiles();

        Set<String> files = Stream.of(new File(dirPath).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());

        dirFileCount = (files.size() < MAX_DIRECTORY_FILES ? files.size() : MAX_DIRECTORY_FILES);
        dirFilesPath = new String[dirFileCount];

        for (int i = 0; i < dirFileCount; i++) {
            dirFilesPath[i] = (String) files.toArray()[i];
        }

        return dirFilesPath;
    }

    // Clear directory files paths buffers
    public void UnloadDirectoryFiles() {
        if (dirFileCount > 0) {
            dirFilesPath = null;
            dirFileCount = 0;
        }
    }

    // ChangeDirectory

    // Check if a file has been dropped into window
    public boolean IsFileDropped(){
        return (window.dropFilesCount > 0);
    }

    // Get dropped files names
    public String[] LoadDroppedFiles(){
        return window.dropFilePaths;
    }

    public int GetDroppedFilesCount(){
        return window.dropFilesCount;
    }

    public void ClearDroppedFiles(){
        if (window.getDropFilesCount() > 0){
            for (int i = 0; i < window.getDropFilesCount(); i++){
                window.dropFilePaths[i] = null;
            }

            window.setDropFilePaths(null);

            window.setDropFilesCount(0);
        }
    }

    // Get file modification time (last write time)
    public long GetFileModTime(String fileName){
        long result = 0L;

        if (FileExists(fileName)){
            File tmp = new File(fileName);
            result = tmp.lastModified();
        }

        return result;
    }

    //TODO: 3/20/21
    // CompressData
    // DecompressData


    // Encode data to Base64 string
    public byte[] EncodeDataBase64(byte[] data, int dataLength, int outputLength)
    {
        char[] base64encodeTable = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        };

        int[] modTable = { 0, 2, 1 };

        outputLength = 4*((dataLength + 2)/3);

        byte[] encodedData = new byte[outputLength];

        if (encodedData == null){
            return null;
        }

        for (int i = 0, j = 0; i < dataLength;)
        {
            int octetA = (i < dataLength)? data[i++] : 0;
            int octetB = (i < dataLength)? data[i++] : 0;
            int octetC = (i < dataLength)? data[i++] : 0;

            int triple = (octetA << 0x10) + (octetB << 0x08) + octetC;

            encodedData[j++] = (byte) base64encodeTable[(triple >> 3*6) & 0x3F];
            encodedData[j++] = (byte) base64encodeTable[(triple >> 2*6) & 0x3F];
            encodedData[j++] = (byte) base64encodeTable[(triple >> 1*6) & 0x3F];
            encodedData[j++] = (byte) base64encodeTable[(triple >> 0*6) & 0x3F];
        }

        for (int i = 0; i < modTable[dataLength%3]; i++){
            encodedData[outputLength - 1 - i] = '=';
        }

        return encodedData;
    }

    // Decode Base64 string data
    public byte[] DecodeDataBase64(byte[] data, int outputLength) {
        byte[] base64decodeTable = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
            37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
        };

        // Get output size of Base64 input data
        int outLength = 0;
        for (int i = 0; data[4*i] != 0; i++) {
            if (data[4*i + 3] == '=')
            {
                if (data[4*i + 2] == '=') outLength += 1;
                else outLength += 2;
            }
            else outLength += 3;
        }

        // Allocate memory to store decoded Base64 data
        byte[] decodedData = new byte[outLength];

        for (int i = 0; i < outLength/3; i++){
            byte a = base64decodeTable[data[4*i]];
            byte b = base64decodeTable[data[4*i + 1]];
            byte c = base64decodeTable[data[4*i + 2]];
            byte d = base64decodeTable[data[4*i + 3]];

            decodedData[3*i] = (byte) ((byte) (a << 2) | (b >> 4));
            decodedData[3*i + 1] = (byte) ((byte) (b << 4) | (c >> 2));
            decodedData[3*i + 2] = (byte) ((byte) (c << 6) | d);
        }

        if (outLength%3 == 1)
        {
            int n = outLength/3;
            byte a = base64decodeTable[data[4*n]];
            byte b = base64decodeTable[data[4*n + 1]];
            decodedData[outLength - 1] = (byte) ((byte) (a << 2) | (b >> 4));
        }
        else if (outLength%3 == 2)
        {
            int n = outLength/3;
            byte a = base64decodeTable[data[4*n]];
            byte b = base64decodeTable[data[4*n + 1]];
            byte c = base64decodeTable[data[4*n + 2]];
            decodedData[outLength - 2] = (byte) ((byte) (a << 2) | (b >> 4));
            decodedData[outLength - 1] = (byte) ((byte) (b << 4) | (c >> 2));
        }

        outputLength = outLength;
        return decodedData;
    }

    //TODO:
    // OpenURL

    //----------------------------------------------------------------------------------
    // Module Functions Definition - Input (Keyboard, Mouse, Gamepad) Functions
    //----------------------------------------------------------------------------------
    // Detect if a key has been pressed once
    public boolean IsKeyPressed(int key){
        return ((!input.keyboard.getPreviousKeyState()[key]) && (input.keyboard.getCurrentKeyState()[key]));
    }

    // Detect if a key is being pressed (key held down)
    public boolean IsKeyDown(int key){
        return input.keyboard.getCurrentKeyState()[key];
    }

    // Detect if a key has been released once
    public boolean IsKeyReleased(int key){
        return (input.keyboard.getPreviousKeyState()[key] && !input.keyboard.getCurrentKeyState()[key]);
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
    public void SetExitKey(int key){
        input.keyboard.setExitKey(key);
    }

    // NOTE: Gamepad support not implemented in emscripten GLFW3 (PLATFORM_WEB)
    // Detect if a gamepad is available
    public boolean IsGamepadAvailable(int gamepad){
        return (gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad];
    }

    // Return gamepad internal name id
    public String GetGamepadName(int gamepad){
        if (PLATFORM_DESKTOP){
            if (input.gamepad.getReady()[gamepad]){
                return glfwGetJoystickName(gamepad);
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

    // Return gamepad axis count
    public int GetGamepadAxisCount(int gamepad){
        return input.gamepad.getAxisCount();
    }

    // Return axis movement vector for a gamepad
    public float GetGamepadAxisMovement(int gamepad, int axis){
        float value = 0;

        if ((gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (axis < MAX_GAMEPAD_AXIS) &&
                (Math.abs(input.gamepad.getAxisState()[gamepad][axis]) > 0.1f)){
            value =
                    input.gamepad.getAxisState()[gamepad][axis];      // 0.1f = GAMEPAD_AXIS_MINIMUM_DRIFT/DELTA
        }

        return value;
    }

    // Detect if a gamepad button has been pressed once
    public boolean IsGamepadButtonPressed(int gamepad, int button){
        boolean pressed = false;

        pressed = (gamepad < MAX_GAMEPADS) && input.gamepad.ready[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.previousButtonState[gamepad][button] == 0) && (input.gamepad.currentButtonState[gamepad][button] == 1);

        return pressed;
    }

    // Detect if a gamepad button is being pressed
    public boolean IsGamepadButtonDown(int gamepad, int button){
        boolean result = (gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.getCurrentButtonState()[gamepad][button] == 1);

        return result;
    }

    // Detect if a gamepad button has NOT been pressed once
    public boolean IsGamepadButtonReleased(int gamepad, int button){
        boolean released = false;

        released = (gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.getPreviousButtonState()[gamepad][button] == 1) && (input.gamepad.getCurrentButtonState()[gamepad][button] == 0);

        return released;
    }

    // Detect if a mouse button is NOT being pressed
    public boolean IsGamepadButtonUp(int gamepad, int button){
        boolean result = (gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.getCurrentButtonState()[gamepad][button] == 0);

        return result;
    }

    // Get the last gamepad button pressed
    public int GetGamepadButtonPressed(){
        return input.gamepad.getLastButtonPressed();
    }

    // Set internal gamepad mappings
    public boolean SetGamepadMappings(byte[] mappings){
        boolean result = false;

        ByteBuffer mappingsBuffer = ByteBuffer.allocateDirect(mappings.length);
        mappingsBuffer.put(mappings).flip();

        if (PLATFORM_DESKTOP){
            result = glfwUpdateGamepadMappings(mappingsBuffer);
        }

        return result;
    }

    // Detect if a mouse button has been pressed once
    public boolean IsMouseButtonPressed(int button){
        boolean pressed = (input.mouse.getCurrentButtonState()[button] == 1) &&
                (input.mouse.getPreviousButtonState()[button] == 0);

        /* TODO: Touch support - MODULE GESTURES
        // Map touches to mouse buttons checking
        if ((input.touch.currentTouchState[button] == 1) && (input.touch.previousTouchState[button] == 0)){
            pressed = true;
        }
        */

        return pressed;
    }

    // Detect if a mouse button is being pressed
    public boolean IsMouseButtonDown(int button){
        boolean down = input.mouse.getCurrentButtonState()[button] == 1;

        /*
        TODO: Touch support - MODULE GESTURES
        Map touches to mouse buttons checking
        if (input.Touch.currentTouchState[button] == 1) down = true;
        */

        return down;
    }

    // Detect if a mouse button has been released once
    public boolean IsMouseButtonReleased(int button){
        boolean released = (input.mouse.getCurrentButtonState()[button] == 0) &&
                (input.mouse.getPreviousButtonState()[button] == 1);

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
        return (int) ((input.mouse.currentPosition.x + input.mouse.getOffset().getX()) * input.mouse.getScale().getX());
        //#endif
    }

    // Returns mouse position Y
    public int GetMouseY(){
        /* TODO: Touch support - MODULE GESTURES
        #if defined(PLATFORM_ANDROID)
            return (int)input.Touch.position[0].y;
        #else
        */
        return (int) ((input.mouse.currentPosition.y + input.mouse.getOffset().getY()) * input.mouse.getScale().getY());
        //#endif
    }

    // Returns mouse position XY
    public Vector2 GetMousePosition(){
        Vector2 position = new Vector2();

        /*
        TODO: Touch support - MODULE GESTURES
        #if defined(PLATFORM_ANDROID) || defined(PLATFORM_WEB)
        position = GetTouchPosition(0);
        #else
        */
        position.setX((input.mouse.currentPosition.x + input.mouse.getOffset().getX()) * input.mouse.getScale().getX());
        position.setY((input.mouse.currentPosition.y + input.mouse.getOffset().getY()) * input.mouse.getScale().getY());
        //#endif

        return position;
    }

    // Get mouse delta between frames
    public Vector2 GetMouseDelta() {
        Vector2 delta = new Vector2();

        delta.x = input.mouse.currentPosition.x - input.mouse.previousPosition.x;
        delta.y = input.mouse.currentPosition.y - input.mouse.previousPosition.y;

        return delta;
    }

    // Set mouse position XY
    public void SetMousePosition(int x, int y){
        input.mouse.setCurrentPosition(new Vector2((float) x, (float) y));
        // NOTE: emscripten not implemented
        glfwSetCursorPos(window.handle, input.mouse.currentPosition.x, input.mouse.currentPosition.y);
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
    public float GetMouseWheelMove(){
        float result = 0.0f;

        if(Math.abs(input.mouse.currentWheelMove.x) > Math.abs(input.mouse.currentWheelMove.y)) {
            result = input.mouse.currentWheelMove.x;
        }
        else {
            result = input.mouse.currentWheelMove.y;
        }

        return result;
    }

    // Get mouse wheel movement X/Y as a vector
    public Vector2 GetMouseWheelMoveV() {
       return input.mouse.currentWheelMove;
    }

    // Set mouse cursor
    // NOTE: This is a no-op on platforms other than PLATFORM_DESKTOP
    public void SetMouseCursor(int cursor){
        input.mouse.setCursor(cursor);
        if (cursor == MOUSE_CURSOR_DEFAULT.ordinal()){
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

        callback = new Callbacks(this);
        glfwSetErrorCallback(callback.errorCallback);

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
        if ((window.getFlags() & FLAG_FULLSCREEN_MODE) > 0){
            window.fullscreen = true;
        }

        if ((window.getFlags() & FLAG_WINDOW_HIDDEN) > 0){
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Visible window
        }
        else{
            glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);     // Window initially hidden
        }

        if ((window.getFlags() & FLAG_WINDOW_UNDECORATED) > 0){
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE); // Border and buttons on Window
        }
        else{
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);   // Decorated window
        }

        if ((window.getFlags() & FLAG_WINDOW_RESIZABLE) > 0){
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Resizable window
        }
        else{
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);  // Avoid window being resizable
        }

        // Disable FLAG_WINDOW_MINIMIZED, not supported on initialization
        if ((window.getFlags() & FLAG_WINDOW_MINIMIZED) > 0){
            window.flags &= ~FLAG_WINDOW_MINIMIZED;
        }

        // Disable FLAG_WINDOW_MAXIMIZED, not supported on initialization
        if ((window.getFlags() & FLAG_WINDOW_MAXIMIZED) > 0){
            window.flags &= ~FLAG_WINDOW_MAXIMIZED;
        }

        if ((window.getFlags() & FLAG_WINDOW_UNFOCUSED) > 0){
            glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
        }
        else{
            glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        }

        if ((window.getFlags() & FLAG_WINDOW_TOPMOST) > 0){
            glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        }
        else{
            glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        }

        if ((window.getFlags() & FLAG_WINDOW_TRANSPARENT) > 0){
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);     // Transparent framebuffer
        }
        else{
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE);     // Transparent framebuffer
        }

        if ((window.getFlags() & FLAG_WINDOW_HIGHDPI) > 0){
            // Resize window content area based on the monitor content scale.
            // NOTE: This hint only has an effect on platforms where screen coordinates and pixels always map 1:1 such as Windows and X11.
            // On platforms like macOS the resolution of the framebuffer is changed independently of the window size.
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);  // Scale content area based on the monitor content scale
            // where window is placed on
            if (__APPLE__) {
                glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE);
            }
        }
        else{
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE);
        }

        // Mouse Passthrough
        if((window.getFlags() & FLAG_WINDOW_MOUSE_PASSTHROUGH) > 0){
            glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
        }
        else {
            glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_FALSE);
        }

        if ((window.getFlags() & FLAG_MSAA_4X_HINT) > 0){
            Tracelog(LOG_INFO, "DISPLAY: Trying to enable MSAA x4");
            glfwWindowHint(GLFW_SAMPLES, 4);   // Tries to enable multisampling x4 (MSAA), default is 0
        }

        // NOTE: When asking for an OpenGL context version, most drivers provide highest supported version
        // with forward compatibility to older OpenGL versions.
        // For example, if using OpenGL 1.1, driver can provide a 4.3 context forward compatible.

        // Check selection OpenGL version

        if (RLGL.rlGetVersion() == rlGlVersion.OPENGL_21){
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);          // Choose OpenGL minor version (just hint)
        }
        else if (RLGL.rlGetVersion() == rlGlVersion.OPENGL_33){
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);          // Choose OpenGL major version (just hint)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);          // Choose OpenGL minor version (just hint)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Profiles Hint: Only 3.3 and above!
            if (__APPLE__){
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            }
            else{
                glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);
            }
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }
        else if (RLGL.rlGetVersion() == rlGlVersion.OPENGL_ES_20){
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            if (PLATFORM_DESKTOP){
                glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
            }
            else{
                glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
            }
        }

        if (Config.MAX_GAMEPADS > 0){
            // NOTE: GLFW 3.4+ defers initialization of the Joystick subsystem on the first call to any Joystick related functions.
            // Forcing this initialization here avoids doing it on `PollInputEvents` called by `EndDrawing` after first frame has been just drawn.
            // The initialization will still happen and possible delays still occur, but before the window is shown, which is a nicer experience.
            // REF: https://github.com/raysan5/raylib/issues/1554
            glfwSetJoystickCallback(null);
        }

        if (window.fullscreen){
            // remember center for switchinging from fullscreen to window
            window.position.setX(window.display.getWidth() / 2 - window.screen.getWidth() / 2);
            window.position.setY(window.display.getHeight() / 2 - window.screen.getHeight() / 2);

            if (window.position.getX() < 0){
                window.position.setX(0);
            }
            if (window.position.getX() < 0){
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

            Tracelog(LOG_WARNING, "SYSTEM: Closest fullscreen videomode: " + window.display.getWidth() + "x" + window.display.getHeight());

            // NOTE: ISSUE: Closest video mode could not match monitor aspect-ratio, for example,
            // for a desired screen size of 800x450 (16:9), the closest supported video mode is 800x600 (4:3),
            // framebuffer is rendered correctly but once displayed on a 16:9 monitor, it gets stretched
            // by the sides to fit all monitor space...

            // Try to setup the most appropriate fullscreen framebuffer for the requested screenWidth/screenHeight
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
            if (PLATFORM_DESKTOP) {
                // If we are windowed fullscreen, ensures that window does not minimize when focus is lost
                if ((window.screen.height == window.display.height) && (window.screen.width == window.display.width)) {
                    glfwWindowHint(GLFW_AUTO_ICONIFY, 0);
                }
            }

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
        glfwSetWindowMaximizeCallback(window.handle, callback.windowMaximizeCallback);
        glfwSetWindowSizeCallback(window.handle, callback.windowSizeCallback);
        // NOTE: Resizing not allowed by default!
        glfwSetWindowIconifyCallback(window.handle, callback.windowIconifyCallback);
        glfwSetWindowFocusCallback(window.handle, callback.windowFocusCallback);
        glfwSetDropCallback(window.handle, callback.windowDropCallback);
        // Set input callback events
        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window.handle, callback.keyCallback);
        glfwSetCharCallback(window.handle, callback.charCallback);
        glfwSetMouseButtonCallback(window.handle, callback.mouseButtonCallback);
        glfwSetCursorPosCallback(window.handle, callback.mouseCursorPosCallback);
        // Track mouse position changes
        glfwSetScrollCallback(window.handle, callback.mouseScrollCallback);
        glfwSetCursorEnterCallback(window.handle, callback.cursorEnterCallback);

        glfwMakeContextCurrent(window.handle);
        GL.createCapabilities();

        // Load OpenGL 3.3 extensions
        // NOTE: GLFW loader function is passed as parameter
        //TODO - rlLoadExtensions uses GLAD.
        //rlLoadExtensions(glfwGetProcAddress());
        RLGL.rlLoadExtensions();

        // Initialize OpenGL context (states and resources)
        // NOTE: window.screen.getWidth() and window.screen.getHeight() not used, just stored as globals in rlgl
        RLGL.rlglInit(window.screen.getWidth(), window.screen.getHeight());

        int fbWidth = window.render.getWidth();
        int fbHeight = window.render.getHeight();

        if ((window.getFlags() & FLAG_WINDOW_HIGHDPI) > 0){
            glfwGetFramebufferSize(window.handle, new int[]{fbWidth}, new int[]{fbHeight});

            // Screen scaling matrix is required in case desired screen area is different from display area
            window.screenScale = MatrixScale((float) fbWidth / window.screen.getWidth(), (float) fbHeight / window.screen.getHeight(), 1.0f);
        }

        // Setup default viewport
        SetupViewport(fbWidth, fbHeight);

        window.currentFbo.setWidth(window.screen.getWidth());
        window.currentFbo.setHeight(window.screen.getHeight());

        glfwShowWindow(window.handle);

        if ((window.getFlags() & FLAG_WINDOW_MINIMIZED) > 0){
            MinimizeWindow();
        }

        return true;
    }

     void SetupViewport(int width, int height){
        window.render.setWidth(width);
        window.render.setHeight(height);

        // Set viewport width and height
        // NOTE: We consider render size and offset in case black bars are required and
        // render area does not match full display area (this situation is only applicable on fullscreen mode)
        RLGL.rlViewport((int) window.renderOffset.x / 2, (int) window.renderOffset.y / 2,
                        (int) (window.render.getWidth() - window.renderOffset.getX()),
                        (int) (window.render.getHeight() - window.renderOffset.getY()));

        RLGL.rlMatrixMode(RLGL.RL_PROJECTION);        // Switch to projection matrix
        RLGL.rlLoadIdentity();                   // Reset current matrix (projection)

        // Set orthographic projection to current framebuffer size
        // NOTE: Configured top-left corner as (0, 0)
        RLGL.rlOrtho(0, window.render.getWidth(), window.render.getHeight(), 0, 0.0f, 1.0f);

        RLGL.rlMatrixMode(RLGL.RL_MODELVIEW);         // Switch back to modelview matrix
        RLGL.rlLoadIdentity();                   // Reset current matrix (modelview)
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
     * Wait for some time (stop program execution)
     *
     * @param seconds Time to wait in seconds
     */
    public void WaitTime(double seconds){
        if (SUPPORT_WINMM_HIGHRES_TIMER){
            double prevTime = GetTime();
            double nextTime = 0.0;

            // Busy wait loop
            while ((nextTime - prevTime) < seconds) nextTime = GetTime();
        }
        else{
            if (SUPPORT_HALFBUSY_WAIT_LOOP){
                double destTime = GetTime() + seconds;
                while (GetTime() < destTime){
                }
            }
        }
    }

    /**
     * Poll (store) all input events
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
        input.mouse.setCurrentWheelMove(new Vector2());

        // Check if gamepads are ready
        // NOTE: We do it here in case of disconnection
        boolean[] gamepadReady = new boolean[MAX_GAMEPADS];
        for (int i = 0; i < MAX_GAMEPADS; i++){
            gamepadReady[i] = glfwJoystickPresent(i);
        }
        input.gamepad.setReady(gamepadReady);

        // Register gamepads buttons events
        for (int i = 0; i < MAX_GAMEPADS; i++){
            if (input.gamepad.getReady()[i]) {     // Check if gamepad is available
                // Register previous gamepad states
                System.arraycopy(input.gamepad.currentButtonState[i], 0, input.gamepad.previousButtonState[i], 0, input.gamepad.currentButtonState[i].length);

                // Get current gamepad state
                // NOTE: There is no callback available, so we get it manually
                // Get remapped buttons
                GLFWGamepadState state = new GLFWGamepadState(ByteBuffer.allocateDirect(40));
                glfwGetGamepadState(i, state); // This remaps all gamepads, so they have their buttons mapped like an xbox
                // controller

                ByteBuffer buttons = state.buttons();

                for (int k = 0; (buttons != null) && (k < GLFW_GAMEPAD_BUTTON_DPAD_LEFT + 1) && (k < MAX_GAMEPAD_BUTTONS); k++){
                    int button = -1;

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

                    if (button != -1) {  // Check for valid button
                        if (buttons.get(k) == GLFW_PRESS) {
                            input.gamepad.currentButtonState[i][button] = 1;
                            input.gamepad.lastButtonPressed = button;
                        }
                        else {
                            input.gamepad.currentButtonState[i][button] = 0;
                        }
                    }
                }

                // Get current axis state
                FloatBuffer axes = state.axes();

                for (int k = 0; (axes != null) && (k < GLFW_GAMEPAD_AXIS_LAST + 1) && (k < MAX_GAMEPAD_AXIS); k++){
                    input.gamepad.getAxisState()[i][k] = axes.get(k);
                }

                // Register buttons for 2nd triggers (because GLFW doesn't count these as buttons but rather axis)
                input.gamepad.getCurrentButtonState()[i][GAMEPAD_BUTTON_LEFT_TRIGGER_2] = (byte) (input.gamepad.getAxisState()[i][GAMEPAD_AXIS_LEFT_TRIGGER] > 0.1 ? 0 : 1);
                input.gamepad.getCurrentButtonState()[i][GAMEPAD_BUTTON_RIGHT_TRIGGER_2] = (byte) (input.gamepad.getAxisState()[i][GAMEPAD_AXIS_RIGHT_TRIGGER] > 0.1 ? 0 : 1);

                input.gamepad.setAxisCount(GLFW_GAMEPAD_AXIS_LAST);
            }
        }

        window.setResizedLastFrame(false);

        if (SUPPORT_EVENTS_WAITING){
            glfwWaitEvents();
        }
        else{
            glfwPollEvents();       // Register keyboard/mouse events (callbacks)... and window events!
        }
    }

    /**
     * Swap back buffer with front buffer (screen drawing)
     */
    void SwapScreenBuffer(){
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
    //END RPI

    //FindMatching
    //FindExact
    //FindNearest
    //END DRM

    // NOTE: Loading happens over AutomationEvent *events
    public void LoadAutomationEvents(String fileName) {
        //unsigned char fileId[4] = { 0 };

        // Load binary
        /*
        FILE *repFile = fopen(fileName, "rb");
        fread(fileId, 4, 1, repFile);

        if ((fileId[0] == 'r') && (fileId[1] == 'E') && (fileId[2] == 'P') && (fileId[1] == ' '))
        {
            fread(&eventCount, sizeof(int), 1, repFile);
            TraceLog(LOG_WARNING, "Events loaded: %i\n", eventCount);
            fread(events, sizeof(AutomationEvent), eventCount, repFile);
        }

        fclose(repFile);
        */

        // Load events (text file)
        String[] repFile = new String[0];
        try{
            repFile = FileIO.LoadFileText(fileName).split("\n");
        } catch (IOException e){
            e.printStackTrace();
        }

        if (repFile != null) {
            int count = 0;

            while (count < repFile.length) {
                if (repFile[count].charAt(0) == 'c' && !repFile[count].contains("#")){
                    eventCount = Integer.parseInt(repFile[count].substring(2));
                }
                else if (repFile[count].charAt(0) == 'e') {
                    String[] eLine = repFile[count].split(" ");
                    events.get(count).frame = Integer.parseInt(eLine[1]);
                    events.get(count).type = Integer.parseInt(eLine[2]);
                    events.get(count).params[0] = Integer.parseInt(eLine[3]);
                    events.get(count).params[1] = Integer.parseInt(eLine[4]);
                    events.get(count).params[2] = Integer.parseInt(eLine[5]);

                    count++;
                }

            }

            if (count != eventCount) {
                Tracelog(LOG_WARNING, "Events count provided is different than count");
            }

        }

        Tracelog(LOG_WARNING, "Events loaded: " + eventCount);
    }

    // Export recorded events into a file
    public void ExportAutomationEvents(String fileName) {
        // Save as binary
        /*
        FILE *repFile = fopen(fileName, "wb");
        fwrite(fileId, 4, 1, repFile);
        fwrite(&eventCount, sizeof(int), 1, repFile);
        fwrite(events, sizeof(AutomationEvent), eventCount, repFile);
        fclose(repFile);
        */

        // Export events as text
        StringBuilder repFileText = new StringBuilder();

        if (fileName != null || fileName != "") {
            repFileText.append("# Automation events list\n");
            repFileText.append("#    c <events_count>\n");
            repFileText.append("#    e <frame> <event_type> <param0> <param1> <param2> // <event_type_name>\n");

            repFileText.append("c ").append(eventCount).append("\n");
            for (int i = 0; i < eventCount; i++) {
                repFileText.append("e ").append(events.get(i).frame).append(" ").append(events.get(i).type)
                        .append(" ").append(events.get(i).params[0]).append(" ").append(events.get(i).params[1])
                        .append(" ").append(events.get(i).params[2]).append(" // ").append(AutomationEvent.EventType.values()[events.get(i).type].name().toLowerCase())
                        .append("\n");
            }

            try{
                FileIO.SaveFileText(fileName, repFileText.toString());
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    // EndDrawing() -> After PollInputEvents()
    // Check event in current frame and save into the events[i] array
    public void RecordAutomationEvent(int frame) {
        for (int key = 0; key < Config.MAX_KEYBOARD_KEYS; key++) {
            // INPUT_KEY_UP (only saved once)
            if (input.keyboard.previousKeyState[key] && !input.keyboard.currentKeyState[key]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_KEY_UP.ordinal();
                events.get(eventCount).params[0] = key;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_KEY_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_KEY_DOWN
            if (input.keyboard.currentKeyState[key]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_KEY_DOWN.ordinal();
                events.get(eventCount).params[0] = key;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_KEY_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }
        }

        for (int button = 0; button < MAX_MOUSE_BUTTONS; button++) {
            // INPUT_MOUSE_BUTTON_UP
            if (input.mouse.previousButtonState[button] == 1 && !(input.mouse.currentButtonState[button] == 1)) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_MOUSE_BUTTON_UP.ordinal();
                events.get(eventCount).params[0] = button;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_BUTTON_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_MOUSE_BUTTON_DOWN
            if (input.mouse.currentButtonState[button] == 1) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_MOUSE_BUTTON_DOWN.ordinal();
                events.get(eventCount).params[0] = button;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_BUTTON_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }
        }

        // INPUT_MOUSE_POSITION (only saved if changed)
        if (((int)input.mouse.currentPosition.x != (int)input.mouse.previousPosition.x) || ((int)input.mouse.currentPosition.y != (int)input.mouse.previousPosition.y)) {
            events.get(eventCount).frame = frame;
            events.get(eventCount).type = INPUT_MOUSE_POSITION.ordinal();
            events.get(eventCount).params[0] = (int)input.mouse.currentPosition.x;
            events.get(eventCount).params[1] = (int)input.mouse.currentPosition.y;
            events.get(eventCount).params[2] = 0;

            Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_POSITION: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
            eventCount++;
        }

        // INPUT_MOUSE_WHEEL_MOTION
        if (input.mouse.currentWheelMove != input.mouse.previousWheelMove) {
            events.get(eventCount).frame = frame;
            events.get(eventCount).type = INPUT_MOUSE_WHEEL_MOTION.ordinal();
            events.get(eventCount).params[0] = (int) input.mouse.currentWheelMove.x;
            events.get(eventCount).params[1] = (int) input.mouse.currentWheelMove.y;
            events.get(eventCount).params[2] = 0;

            Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_WHEEL_MOTION: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
            eventCount++;
        }

        for (int id = 0; id < MAX_TOUCH_POINTS; id++) {
            // INPUT_TOUCH_UP
            if (input.touch.previousTouchState[id] && !input.touch.currentTouchState[id]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_TOUCH_UP.ordinal();
                events.get(eventCount).params[0] = id;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_TOUCH_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_TOUCH_DOWN
            if (input.touch.currentTouchState[id]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_TOUCH_DOWN.ordinal();
                events.get(eventCount).params[0] = id;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_TOUCH_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_TOUCH_POSITION
            // TODO: It requires the id!
            /*
            if (((int)input.touch.currentPosition[id].x != (int)input.touch.previousPosition[id].x) || ((int)input.touch.currentPosition[id].y != (int)input.touch.previousPosition[id].y)) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_TOUCH_POSITION;
                events.get(eventCount).params[0] = id;
                events.get(eventCount).params[1] = (int)input.touch.currentPosition[id].x;
                events.get(eventCount).params[2] = (int)input.touch.currentPosition[id].y;

                Tracelog(LOG_INFO, "[%i] INPUT_TOUCH_POSITION: %i, %i, %i", events.get(eventCount).frame, events.get(eventCount).params[0], events.get(eventCount).params[1], events.get(eventCount).params[2]);
                eventCount++;
            }
            */
        }

        for (int gamepad = 0; gamepad < MAX_GAMEPADS; gamepad++) {
            // INPUT_GAMEPAD_CONNECT
            /*
            if ((input.gamepad.currentState[gamepad] != input.gamepad.previousState[gamepad]) && (input.gamepad.currentState[gamepad] == true)) {
                // Check if changed to ready
                // TODO: Save gamepad connect event
            }
            */

            // INPUT_GAMEPAD_DISCONNECT
            /*
            if ((input.gamepad.currentState[gamepad] != input.gamepad.previousState[gamepad]) && (input.gamepad.currentState[gamepad] == false)) {
                // Check if changed to not-ready
                // TODO: Save gamepad disconnect event
            }
            */

            for (int button = 0; button < MAX_GAMEPAD_BUTTONS; button++) {
                // INPUT_GAMEPAD_BUTTON_UP
                if (input.gamepad.previousButtonState[gamepad][button] == 1 && !(input.gamepad.currentButtonState[gamepad][button] == 1)) {
                    events.get(eventCount).frame = frame;
                    events.get(eventCount).type = INPUT_GAMEPAD_BUTTON_UP.ordinal();
                    events.get(eventCount).params[0] = gamepad;
                    events.get(eventCount).params[1] = button;
                    events.get(eventCount).params[2] = 0;

                    Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GAMEPAD_BUTTON_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                    eventCount++;
                }

                // INPUT_GAMEPAD_BUTTON_DOWN
                if (input.gamepad.currentButtonState[gamepad][button] == 1) {
                    events.get(eventCount).frame = frame;
                    events.get(eventCount).type = INPUT_GAMEPAD_BUTTON_DOWN.ordinal();
                    events.get(eventCount).params[0] = gamepad;
                    events.get(eventCount).params[1] = button;
                    events.get(eventCount).params[2] = 0;

                    Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GAMEPAD_BUTTON_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                    eventCount++;
                }
            }

            for (int axis = 0; axis < MAX_GAMEPAD_AXIS; axis++) {
                // INPUT_GAMEPAD_AXIS_MOTION
                if (input.gamepad.axisState[gamepad][axis] > 0.1f) {
                    events.get(eventCount).frame = frame;
                    events.get(eventCount).type = INPUT_GAMEPAD_AXIS_MOTION.ordinal();
                    events.get(eventCount).params[0] = gamepad;
                    events.get(eventCount).params[1] = axis;
                    events.get(eventCount).params[2] = (int)(input.gamepad.axisState[gamepad][axis]*32768.0f);

                    Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GAMEPAD_AXIS_MOTION: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                    eventCount++;
                }
            }
        }

        // INPUT_GESTURE
        /* TODO
        if (gestures.current != GESTURE_NONE) {
            events.get(eventCount).frame = frame;
            events.get(eventCount).type = INPUT_GESTURE;
            events.get(eventCount).params[0] = gestures.current;
            events.get(eventCount).params[1] = 0;
            events.get(eventCount).params[2] = 0;

            Tracelog(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GESTURE: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
            eventCount++;
        }
        */
    }

    // Play automation event
    public void PlayAutomationEvent(int frame) {
        for (int i = 0; i < eventCount; i++) {
            if (events.get(i).frame == frame) {
                switch (AutomationEvent.AutomationEventType.values()[events.get(i).type]) {
                    // Input events
                    case INPUT_KEY_UP:    // param[0]: key
                        input.keyboard.currentKeyState[events.get(i).params[0]] = false;
                        break;
                    case INPUT_KEY_DOWN:  // param[0]: key
                        input.keyboard.currentKeyState[events.get(i).params[0]] = true;
                        break;
                    case INPUT_MOUSE_BUTTON_UP:    // param[0]: key
                        input.mouse.currentButtonState[events.get(i).params[0]] = 0;
                        break;
                    case INPUT_MOUSE_BUTTON_DOWN:   // param[0]: key
                        input.mouse.currentButtonState[events.get(i).params[0]] = 1;
                        break;
                    case INPUT_MOUSE_POSITION:      // param[0]: x, param[1]: y
                        input.mouse.currentPosition.x = (float)events.get(i).params[0];
                        input.mouse.currentPosition.y = (float)events.get(i).params[1];
                        break;
                    case INPUT_MOUSE_WHEEL_MOTION:   // param[0]: delta
                        input.mouse.currentWheelMove.x = (float)events.get(i).params[0];
                        input.mouse.currentWheelMove.y = (float)events.get(i).params[1];
                        break;
                    case INPUT_TOUCH_UP:     // param[0]: id
                        input.touch.currentTouchState[events.get(i).params[0]] = false;
                        break;
                    case INPUT_TOUCH_DOWN:   // param[0]: id
                        input.touch.currentTouchState[events.get(i).params[0]] = true;
                        break;
                    case INPUT_TOUCH_POSITION:      // param[0]: id, param[1]: x, param[2]: y
                        input.touch.position[events.get(i).params[0]].x = (float)events.get(i).params[1];
                        input.touch.position[events.get(i).params[0]].y = (float)events.get(i).params[2];
                        break;
                    case INPUT_GAMEPAD_CONNECT:     // param[0]: gamepad
                        input.gamepad.ready[events.get(i).params[0]] = true;
                        break;
                    case INPUT_GAMEPAD_DISCONNECT:    // param[0]: gamepad
                        input.gamepad.ready[events.get(i).params[0]] = false;
                        break;
                    case INPUT_GAMEPAD_BUTTON_UP:    // param[0]: gamepad, param[1]: button
                        input.gamepad.currentButtonState[events.get(i).params[0]][events.get(i).params[1]] = 0;
                        break;
                    case INPUT_GAMEPAD_BUTTON_DOWN:  // param[0]: gamepad, param[1]: button
                        input.gamepad.currentButtonState[events.get(i).params[0]][events.get(i).params[1]] = 1;
                        break;
                    case INPUT_GAMEPAD_AXIS_MOTION: // param[0]: gamepad, param[1]: axis, param[2]: delta
                        input.gamepad.axisState[events.get(i).params[0]][events.get(i).params[1]] = ((float)events.get(i).params[2]/32768.0f);
                        break;
                    case INPUT_GESTURE: // param[0]: gesture (enum Gesture) -> rgestures.h: GESTURES.current
                        //TODO
                        //GESTURES.current = events.get(i).params[0];
                        break;

                    // Window events
                    case WINDOW_CLOSE:
                        window.shouldClose = true;
                        break;
                    case WINDOW_MAXIMIZE:
                        MaximizeWindow();
                        break;
                    case WINDOW_MINIMIZE:
                        MinimizeWindow();
                        break;
                    case WINDOW_RESIZE:
                        SetWindowSize(events.get(i).params[0], events.get(i).params[1]);
                        break;

                    // Custom events
                    case ACTION_TAKE_SCREENSHOT:
                        TakeScreenshot("screenshot" + screenshotCounter + ".png");
                        screenshotCounter++;
                        break;
                    case ACTION_SETTARGETFPS:
                        SetTargetFPS(events.get(i).params[0]);
                        break;
                    default:
                        break;
                }
            }
        }
    }

}