package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.callback.Callbacks;
import com.raylib.java.core.callback.TraceLogCallback;
import com.raylib.java.core.input.Gamepad;
import com.raylib.java.core.input.Input;
import com.raylib.java.core.input.Mouse;
import com.raylib.java.core.platforms.Desktop;
import com.raylib.java.core.platforms.Platform;
import com.raylib.java.core.tracelog.TraceLog;
import com.raylib.java.structs.*;
import com.raylib.java.core.rcamera.Camera2D;
import com.raylib.java.core.rcamera.Camera3D;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.raylib.java.Config.ConfigFlag.*;
import static com.raylib.java.Config.*;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_INFO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static com.raylib.java.rlgl.RLGL.*;
import static com.raylib.java.rlgl.RLGL.rlBlendMode.BLEND_ALPHA;
import static com.raylib.java.structs.AutomationEvent.AutomationEventType.*;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.GAMEPAD_AXIS_LEFT_TRIGGER;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.GAMEPAD_AXIS_RIGHT_TRIGGER;
import static com.raylib.java.core.input.Gamepad.GamepadButton.*;
import static com.raylib.java.core.input.Keyboard.KEY_ESCAPE;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_ARROW;
import static com.raylib.java.core.input.Mouse.MouseCursor.MOUSE_CURSOR_DEFAULT;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_ORTHOGRAPHIC;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.raymath.Raymath.*;
import static com.raylib.java.rlgl.RLGL.rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8;
import static com.raylib.java.rlgl.RLGL.rlShaderLocationIndex.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class rCore {

    /**********************************************************************************************
     *
     *   rcore - Window/display management, Graphic device/context management and input management
     *
     *   PLATFORMS SUPPORTED:
     *       > PLATFORM_DESKTOP (GLFW backend):
     *           - Windows (Win32, Win64)
     *           - Linux (X11/Wayland desktop mode)
     *           - macOS/OSX (x64, arm64)
     *           - FreeBSD, OpenBSD, NetBSD, DragonFly (X11 desktop)
     *       > PLATFORM_DESKTOP_SDL (SDL backend):
     *           - Windows (Win32, Win64)
     *           - Linux (X11/Wayland desktop mode)
     *           - Others (not tested)
     *       > PLATFORM_WEB:
     *           - HTML5 (WebAssembly)
     *       > PLATFORM_DRM:
     *           - Raspberry Pi 0-5 (DRM/KMS)
     *           - Linux DRM subsystem (KMS mode)
     *       > PLATFORM_ANDROID:
     *           - Android (ARM, ARM64)
     *
     *   CONFIGURATION:
     *       #define SUPPORT_DEFAULT_FONT (default)
     *           Default font is loaded on window initialization to be available for the user to render simple text.
     *           NOTE: If enabled, uses external module functions to load default raylib font (module: text)
     *
     *       #define SUPPORT_CAMERA_SYSTEM
     *           Camera module is included (rcamera.h) and multiple predefined cameras are available:
     *               free, 1st/3rd person, orbital, custom
     *
     *       #define SUPPORT_GESTURES_SYSTEM
     *           Gestures module is included (rgestures.h) to support gestures detection: tap, hold, swipe, drag
     *
     *       #define SUPPORT_MOUSE_GESTURES
     *           Mouse gestures are directly mapped like touches and processed by gestures system.
     *
     *       #define SUPPORT_BUSY_WAIT_LOOP
     *           Use busy wait loop for timing sync, if not defined, a high-resolution timer is setup and used
     *
     *       #define SUPPORT_PARTIALBUSY_WAIT_LOOP
     *           Use a partial-busy wait loop, in this case frame sleeps for most of the time and runs a busy-wait-loop at the end
     *
     *       #define SUPPORT_SCREEN_CAPTURE
     *           Allow automatic screen capture of current screen pressing F12, defined in KeyCallback()
     *
     *       #define SUPPORT_GIF_RECORDING
     *           Allow automatic gif recording of current screen pressing CTRL+F12, defined in KeyCallback()
     *
     *       #define SUPPORT_COMPRESSION_API
     *           Support CompressData() and DecompressData() functions, those functions use zlib implementation
     *           provided by stb_image and stb_image_write libraries, so, those libraries must be enabled on textures module
     *           for linkage
     *
     *       #define SUPPORT_AUTOMATION_EVENTS
     *           Support automatic events recording and playing, useful for automated testing systems or AI based game playing
     *
     *   DEPENDENCIES:
     *       raymath  - 3D math functionality (Vector2, Vector3, Matrix, Quaternion)
     *       camera   - Multiple 3D camera modes (free, orbital, 1st person, 3rd person)
     *       gestures - Gestures system for touch-ready devices (or simulated from mouse inputs)
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

    public final Window window;
    public final Input input;
    public final Time time;

    ArrayList<AutomationEvent> events;
    int eventCount = 0;                 // Events count
    public boolean eventsPlaying = false;      // Play events
    public boolean eventsRecording = false;    // Record events
    short eventsEnabled = 0b0000001111111111;    // Events enabled for checking

    String[] dirFilesPath;
    int dirFileCount;

    public int screenshotCounter;

    public int gifFrameCounter = 0; // GIF frames counter

    //Globals required for FPS calculation
    static int index = 0;
    static float[] history = new float[30]; //FPS_CAPTURE_FRAMES_COUNT
    static float average = 0, last = 0;

    private final Random random;

    private final Raylib context;
    private Platform platform;

    public rCore(Raylib context){
        this.context = context;

        SetTraceLogCallback(new TraceLogCallback());
        window = new Window();
        input = new Input();
        time = new Time();

        this.random = new Random();

        events = new ArrayList<>();

        this.platform = new Desktop(context, window, input);
    }

    /**
     * Set a specific platform backend. Can be used to set custom backends implementing the {@code Platform} interface.
     * @param platform Platform backend to use
     * @return {@code true} if platform backend is successfully set.
     */
    public boolean SetPlatform(Platform platform) {
        if (!window.isReady()) {
            this.platform = platform;
            return true;
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "Attempted to set platform backend after initialising window.");
            return false;
        }
    }

    /**
     * Initialize window and OpenGL context. <br/>
     * NOTE: data parameter could be used to pass any kind of required data to the initialization
     *
     * @param width  Window width in pixels
     * @param height Window height in pixels
     * @param title  Window title - passing null will use a default title
     */
    public void InitWindow(int width, int height, String title){
        context.tracelog.TRACELOG(LOG_INFO, "Initializing raylib %s", RAYLIB_VERSION);

        if(PLATFORM_DESKTOP) {
            context.tracelog.TRACELOG(LOG_INFO, "Platform backend: DESKTOP (GLFW)");
        }
        /*
        TODO: ..?
        else if(PLATFORM_DESKTOP_SDL) {
            context.tracelog.TRACELOG(LOG_INFO, "Platform backend: DESKTOP (SDL)");
        }
        else if(PLATFORM_WEB) {
            context.tracelog.TRACELOG(LOG_INFO, "Platform backend: WEB (HTML5)");
        }
        else if(PLATFORM_DRM) {
            context.tracelog.TRACELOG(LOG_INFO, "Platform backend: NATIVE DRM");
        }
        else if(PLATFORM_ANDROID) {
            context.tracelog.TRACELOG(LOG_INFO, "Platform backend: ANDROID");
        }
         */
        else {
            // TODO: Include your custom platform backend!
            // i.e software rendering backend or console backend!
            context.tracelog.TRACELOG(LOG_INFO, "Platform backend: CUSTOM");
        }

        context.tracelog.TRACELOG(LOG_INFO, "Supported raylib modules: ");
        context.tracelog.TRACELOG(LOG_INFO, "    > rcore:..... loaded (mandatory)");
        context.tracelog.TRACELOG(LOG_INFO, "    > rlgl:...... loaded (mandatory)");

        if(SUPPORT_MODULE_RSHAPES) {
            context.tracelog.TRACELOG(LOG_INFO, "    > rshapes:... loaded (optional)");
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "    > rshapes:... not loaded (optional)");
        }

        if(SUPPORT_MODULE_RTEXTURES) {
            context.tracelog.TRACELOG(LOG_INFO, "    > rtextures:. loaded (optional)");
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "    > rtextures:. not loaded (optional)");
        }

        if(SUPPORT_MODULE_RTEXT) {
            context.tracelog.TRACELOG(LOG_INFO, "    > rtext:..... loaded (optional)");
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "    > rtext:..... not loaded (optional)");
        }

        if(SUPPORT_MODULE_RMODELS) {
            context.tracelog.TRACELOG(LOG_INFO, "    > rmodels:... loaded (optional)");
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "    > rmodels:... not loaded (optional)");
        }

        if(SUPPORT_MODULE_RAUDIO) {
            context.tracelog.TRACELOG(LOG_INFO, "    > raudio:.... loaded (optional)");
        }
        else {
            context.tracelog.TRACELOG(LOG_INFO, "    > raudio:.... not loaded (optional)");
        }

        // Initialize window data
        window.screen.width = width;
        window.screen.height = height;
        window.eventWaiting = false;
        window.screenScale = MatrixIdentity();     // No draw scaling required by default
        if ((title != null) && (!title.isEmpty())) {
            window.title = title;
        }
        else {
            window.title = "Raylib-J Application";
        }

        // Initialize global input state
        input.keyboard.setExitKey(KEY_ESCAPE);
        input.mouse.setScale(new Vector2(1.0f, 1.0f));
        input.mouse.setCursor(MOUSE_CURSOR_ARROW);
        input.gamepad.setLastButtonPressed(0);

        if (SUPPORT_EVENTS_WAITING) {
            window.eventWaiting = true;
        }

        // Initialize platform
        //--------------------------------------------------------------
        platform.InitPlatform();
        //--------------------------------------------------------------

        // Initialize rlgl default data (buffers and shaders)
        // NOTE: window.currentFbo.width and window.currentFbo.height not used, just stored as globals in rlgl
        context.rlgl.rlglInit(window.currentFbo.width, window.currentFbo.height);

        // Setup default viewport
        SetupViewport(window.currentFbo.width, window.currentFbo.height);

        if((SUPPORT_MODULE_RTEXT) && (SUPPORT_DEFAULT_FONT)) {
            // Load default font
            // WARNING: External function: Module required: rtext
            context.text.LoadFontDefault();
            if (SUPPORT_MODULE_RSHAPES) {
                // Set font white rectangle for shapes drawing, so shapes and text can be batched together
                // WARNING: rshapes module is required, if not available, default internal white rectangle is used
                Rectangle rec = context.text.GetFontDefault().recs[0];
                if ((window.flags & FLAG_MSAA_4X_HINT) != 0) {
                    // NOTE: We try to maxime rec padding to avoid pixel bleeding on MSAA filtering
                    context.shapes.SetShapesTexture(context.text.GetFontDefault().texture, new Rectangle(rec.x + 2, rec.y + 2, 1, 1));
                }
                else {
                    // NOTE: We set up a 1px padding on char rectangle to avoid pixel bleeding
                    context.shapes.SetShapesTexture(context.text.GetFontDefault().texture, new Rectangle(rec.x + 1, rec.y + 1, rec.width - 2, rec.height - 2));
                }
            }
        }
        else {
            if (SUPPORT_MODULE_RSHAPES) {
                // Set default texture and rectangle to be used for shapes drawing
                // NOTE: rlgl default texture is a 1x1 pixel UNCOMPRESSED_R8G8B8A8
                Texture2D texture = new Texture2D(context.rlgl.rlGetTextureIdDefault(), 1, 1, 1, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8);
                context.shapes.SetShapesTexture(texture, new Rectangle(0.0f, 0.0f, 1.0f, 1.0f));    // WARNING: Module required: rshapes
            }
        }
        if((SUPPORT_MODULE_RTEXT) && (SUPPORT_DEFAULT_FONT)) {
            if ((window.flags & FLAG_WINDOW_HIGHDPI) > 0) {
                // Set default font texture filter for HighDPI (blurry)
                // RL_TEXTURE_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                context.rlgl.rlTextureParameters(context.text.GetFontDefault().texture.id, RL_TEXTURE_MIN_FILTER, RL_TEXTURE_FILTER_LINEAR);
                context.rlgl.rlTextureParameters(context.text.GetFontDefault().texture.id, RL_TEXTURE_MAG_FILTER, RL_TEXTURE_FILTER_LINEAR);
            }
        }

        time.frameCounter = 0;
        window.shouldClose = false;

        // Initialize random seed
        SetRandomSeed((long) time.frame);
    }

    /**
     * Close window and unload OpenGL context
     */
    public void CloseWindow() {
        if (SUPPORT_MODULE_RTEXT && SUPPORT_DEFAULT_FONT){
            context.text.UnloadFontDefault();        // WARNING: Module required: rtext
        }
        context.rlgl.rlglClose();                // De-init rlgl

        // De-initialize platform
        //--------------------------------------------------------------
        platform.ClosePlatform();
        //--------------------------------------------------------------

        window.ready = false;
        context.tracelog.TRACELOG(LOG_INFO, "Window closed successfully");
    }

    /**
     * Check if KEY_ESCAPE pressed or close icon pressed
     *
     * @return {@code true} if the window is ready to close.
     * NOTE: Must be inverted for use in a while loop
     */
    public boolean WindowShouldClose(){
        return platform.WindowShouldClose();
    }

    /**
     * Check if the window has been initialized successfully
     *
     * @return {@code true} if the window was initialized successfully
     */
    public boolean IsWindowReady(){
        return window.ready;
    }

    /**
     * Check if the window is currently fullscreen
     *
     * @return {@code true} if the window is fullscreen
     */
    public boolean IsWindowFullscreen(){
        return window.fullscreen;
    }

    /**
     * Check if the window is currently hidden
     *
     * @return {@code true} if the window is hidden
     */
    public boolean IsWindowHidden(){
        return ((window.flags & FLAG_WINDOW_HIDDEN) > 0);
    }

    /**
     * Check if the window has been minimized
     */
    public boolean IsWindowMinimized(){
        return ((window.flags & FLAG_WINDOW_MINIMIZED) > 0);
    }

    /**
     * Check if the window has been maximized (only PLATFORM_DESKTOP)
     * @return {@code true} if the window is maximized.
     */
    public boolean IsWindowMaximized(){
        return ((window.flags & FLAG_WINDOW_MAXIMIZED) > 0);
    }

    /**
     * Check if the window has the focus
     * @return {@code true} if the window is focused.
     */
    public boolean IsWindowFocused(){
        return ((window.flags & FLAG_WINDOW_UNFOCUSED) == 0);
    }

    /**
     * Check if the window has been resized since the last frame.
     * @return {@code true} if the window was resized since the last frame.
     */
    public boolean IsWindowResized(){
        return window.resizedLastFrame;
    }

    /**
     * Check if one specific window flag is enabled
     *
     * @param flag Window flag to be checked
     * @return {@code true} if flag is enabled
     */
    public boolean IsWindowState(int flag){
        return ((window.flags & flag) > 0);
    }

    /**
     * Toggle fullscreen mode (only PLATFORM_DESKTOP)
     */
    public void ToggleFullscreen(){
        platform.ToggleFullscreen();
    }

    /**
     * Toggle borderless windowed mode (only PLATFORM_DESKTOP)
     */
    public void ToggleBorderlessWindowed() {
        platform.ToggleBorderlessWindowed();
    }

    /**
     * Set window state: maximized, if resizable (only PLATFORM_DESKTOP)
     */
    public void MaximizeWindow(){
        platform.MaximizeWindow();
    }

    /**
     * Set window state: minimized (only PLATFORM_DESKTOP)
     */
    public void MinimizeWindow(){
        platform.MinimizeWindow();
    }

    /**
     * Set window state: not minimized/maximized (only PLATFORM_DESKTOP)
     */
    public void RestoreWindow(){
        platform.RestoreWindow();
    }

    /**
     * Set the window's configuration state using flags.
     * @param flags {@code int} whose binary value represents the flags to set.
     */
    public void SetWindowState(int flags){
        platform.SetWindowState(flags);
    }

    /**
     * Clear window configuration state flags
     * @param flags {@code int} whose binary value represents the flags to clear.
     */
    public void ClearWindowState(int flags){
        platform.ClearWindowState(flags);
    }

    /**
     * Set icon for window (only PLATFORM_DESKTOP)
     * @param image RGBA, 8 bits per channel formatted image
     */
    public void SetWindowIcon(Image image){
        platform.SetWindowIcon(image);
    }

    /**
     * Set icons for window (multiple images, only PLATFORM_DESKTOP)<br/>
     * The images are used depending on provided sizes<br/>
     * Standard Windows icon sizes: 256, 128, 96, 64, 48, 32, 24, 16
     *
     * @param images {@code List} of RGBA, 8 bits per channel formatted images to set as window icons.
     */
    public void SetWindowIcons(List<Image> images){
        platform.SetWindowIcons(images);
    }

    /**
     * Set title for window (only PLATFORM_DESKTOP)
     *
     * @param title String to be displayed on window title bar
     */
    public void SetWindowTitle(String title){
        platform.SetWindowTitle(title);
    }

    /**
     * Set window position on screen (windowed mode)
     * @param x position on the x-axis of the screen for the top-left corner of the window
     * @param y position on the y-axis of the screen for the top-left corner of the window
     */
    public void SetWindowPosition(int x, int y){
        platform.SetWindowPosition(x, y);
    }

    /**
     * Set window position on screen (windowed mode)
     * @param position x,y coordinate pair for the top-left corner of the window
     */
    public void SetWindowPosition(Vector2 position) {
        platform.SetWindowPosition((int) position.x, (int) position.y);
    }

    /**
     * Set monitor for the current window (fullscreen mode)
     * @param monitor GLFW monitor handle to display on.
     */
    public void SetWindowMonitor(int monitor){
        platform.SetWindowMonitor(monitor);
    }

    /**
     * Set window minimum dimensions (FLAG_WINDOW_RESIZABLE)
     * @param width minimum width (in pixels) the window must be
     * @param height minimum height (in pixels) the window must be
     */
    public void SetWindowMinSize(int width, int height){
        platform.SetWindowMinSize(width, height);
    }

    /**
     * Set window maximum dimensions (FLAG_WINDOW_RESIZABLE)
     * @param width maximum width (in pixels) the window must be
     * @param height maximum height (in pixels) the window must be
     */
    public void SetWindowMaxSize(int width, int height){
        platform.SetWindowMaxSize(width, height);
    }

    /**
     * Set window dimensions
     * @param width width (in pixels) the window should be
     * @param height height (in pixels) the window should be
     */
    public void SetWindowSize(int width, int height){
        platform.SetWindowSize(width, height);
    }

    /**
     * Set window opacity
     * @param opacity Normalised (between 0.0 and 1.0) value.<br/>
     * 1.0f is fully opaque <br/>
     * 0.0f is fully transparent
     */
    public void SetWindowOpacity(float opacity) {
        platform.SetWindowOpacity(opacity);
    }

    /**
     * Set window focused (only PLATFORM_DESKTOP)
     */
    public void SetWindowFocused(){
        platform.SetWindowFocused();
    }

    /**
     * Get current screen width
     *
     * @return Width of current window
     */
    public int GetScreenWidth(){
        return window.screen.width;
    }

    /**
     * Get current screen height
     *
     * @return Height of current window
     */
    public int GetScreenHeight(){
        return window.screen.height;
    }

    /**
     * Get current render width
     * @return Current render width taking HiDPI into account
     */
    public int GetRenderWidth(){
        if (__APPLE__){
            Vector2 scale = GetWindowScaleDPI();
            return (int) (window.render.width * scale.x);
        }
        else {
            return window.render.width;
        }
    }

    /**
     * Get current render height
     * @return Current render height taking HiDPI into account
     */
    public int GetRenderHeight(){
        if (__APPLE__){
            Vector2 scale = GetWindowScaleDPI();
            return (int) (window.render.height * scale.y);
        }
        else {
            return window.render.height;
        }
    }

    /**
     *  Get native window handle
     * @return native (GLFW) window handle
     */
    public long GetWindowHandle(){
        return platform.GetWindowHandle();
    }

    /**
     * Get number of monitors
     * @return number of monitors detected
     */
    public int GetMonitorCount() {
        return platform.GetMonitorCount();
    }

    /**
     * Get monitor the window is presently on
     * @return monitor the window is rendered on
     */
    public int GetCurrentMonitor() {
        return platform.GetCurrentMonitor();
    }

    /**
     * Get selected monitor position
     * @param monitor desired monitor number
     * @return x,y pair that defines monitor's upper left corner in pixel-space
     */
    public Vector2 GetMonitorPosition(int monitor){
        return platform.GetMonitorPosition(monitor);
    }

    /**
     * Get selected monitor width (currently used by monitor)
     * @param monitor desired monitor number
     * @return monitor width
     */
    public int GetMonitorWidth(int monitor){
        return platform.GetMonitorWidth(monitor);
    }

    /**
     * Get selected monitor height (currently used by monitor)
     * @param monitor desired monitor number
     * @return monitor height
     */
    public int GetMonitorHeight(int monitor){
        return platform.GetMonitorHeight(monitor);
    }

    /**
     *  Get selected monitor physical width in millimetres
     * @param monitor Monitor to evaluate
     * @return Width in millimeters
     */
    public int GetMonitorPhysicalWidth(int monitor){
        return platform.GetMonitorPhysicalWidth(monitor);
    }

    /**
     *  Get selected monitor physical height in millimetres
     * @param monitor Monitor to evaluate
     * @return Height in millimeters
     */
    public int GetMonitorPhysicalHeight(int monitor){
        return platform.GetMonitorPhysicalHeight(monitor);
    }

    /**
     * Get selected monitor refresh rate
     * @param monitor Monitor to evaluate
     * @return Refresh rate (in hertz)
     */
    public int GetMonitorRefreshRate(int monitor) {
        return platform.GetMonitorRefreshRate(monitor);
    }

    /**
     *  Get window position
     * @return x,y coordinate of window's top left corner
     */
    public Vector2 GetWindowPosition() {
       return platform.GetWindowPosition();
    }

    /**
     * Get window scale DPI factor
     * @return DPI scale factor
     */
    public Vector2 GetWindowScaleDPI() {
        return platform.GetWindowScaleDPI();
    }

    /**
     * Get the human-readable, UTF-8 encoded name of the primary monitor
     * @param monitor monitor to evaluate
     * @return Monitor name
     */
    public String GetMonitorName(int monitor){
        return platform.GetMonitorName(monitor);
    }

    /**
     * Get the clipboard content
     * @return clipboard content
     */
    public String GetClipboardText(){
        return platform.GetClipboardText();
    }

    /**
     * Set clipboard text content
     * @param text clipboard content
     */
    public void SetClipboardText(String text){
        platform.SetClipboardText(text);
    }

    /**
     * Enable waiting for events on EndDrawing(), no automatic event polling
     */
    public void EnableEventWaiting() {
        window.eventWaiting = true;
    }

    /**
     * Disable waiting for events on EndDrawing(), automatic events polling
     */
    public void DisableEventWaiting() {
        window.eventWaiting = false;
    }

    /**
     * Show the mouse cursor
     */
    public void ShowCursor(){
        platform.ShowCursor();
    }

    /**
     * Hide the mouse cursor
     */
    public void HideCursor(){
        platform.HideCursor();
    }

    /**
     * Check if mouse cursor is not visible
     * @return {@code true} if cursor is hidden
     */
    public boolean IsCursorHidden(){
        return input.mouse.isCursorHidden();
    }

    /**
     * Enable mouse cursor
     */
    public void EnableCursor(){
        platform.EnableCursor();
    }

    /**
     * Disable mouse cursor
     */
    public void DisableCursor(){
        platform.DisableCursor();
    }

    /**
     *  Check if cursor is on the current screen.
     *
     * @return {@code true} if cursor is within window bounds
     */
    public boolean IsCursorOnScreen(){
        return input.mouse.isCursorOnScreen();
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Screen Drawing
    //----------------------------------------------------------------------------------

    /**
     * Clear window background
     *
     * @param color Color to fill the background
     */
    public void ClearBackground(Color color){
        context.rlgl.rlClearColor(color.getR(), color.getG(), color.getB(), color.getA());   // Set clear color
        context.rlgl.rlClearScreenBuffers();                             // Clear current framebuffers
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

        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)
        context.rlgl.rlMultMatrixf(MatrixToFloat(window.screenScale)); // Apply screen scaling

        //rlTranslatef(0.375, 0.375, 0);    // HACK to have 2D pixel-perfect drawing on OpenGL 1.1
        // NOTE: Not required with OpenGL 3.3+
    }

    /**
     * End canvas drawing and swap buffers (double buffering)
     */
    public void EndDrawing(){
        context.rlgl.rlDrawRenderBatchActive();      // Update and draw internal render batch

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
                    unsigned char *screenData = rlReadScreenPixels(window.screen.width, window.screen.height);
                    msf_gif_frame(&gifState, screenData, 10, 16, window.screen.width*4);

                    RL_FREE(screenData);    // Free image data
                }

                if (((gifFrameCounter/15)%2) == 1)
                {
                    DrawCircle(30, window.screen.height - 20, 10, MAROON);
                    DrawText("GIF RECORDING", 50, window.screen.height - 25, 10, RED);
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
                    DrawCircle(30, window.screen.height - 20, 10, MAROON);
                    DrawText("EVENTS RECORDING", 50, window.screen.height - 25, 10, RED);
                }

                rlDrawRenderBatchActive();  // Update and draw internal render batch
            }
            else if (eventsPlaying) {
                gifFrameCounter++;

                if (((gifFrameCounter/15)%2) == 1) {
                    DrawCircle(30, window.screen.height - 20, 10, LIME);
                    DrawText("EVENTS PLAYING", 50, window.screen.height - 25, 10, GREEN);
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

        if (SUPPORT_SCREEN_CAPTURE) {
            //TODO::
        }

        time.frameCounter++;
    }

    /**
     * Initialize 2D mode with custom camera
     * @param camera rendering camera
     */
    public void BeginMode2D(Camera2D camera){
        context.rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)

        // Apply 2d camera transformation to modelview
        context.rlgl.rlMultMatrixf(MatrixToFloat(GetCameraMatrix2D(camera)));

        // Apply screen scaling if required
        context.rlgl.rlMultMatrixf(MatrixToFloat(window.getScreenScale()));
    }

    /**
     * Ends 2D mode with custom camera
     */
    public void EndMode2D(){
        context.rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)
        context.rlgl.rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling if required
    }

    /**
     * Initializes 3D mode with custom camera
     * @param camera rendering camera
     */
    public void BeginMode3D(Camera3D camera){
        context.rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        context.rlgl.rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        context.rlgl.rlPushMatrix();                     // Save previous matrix, which contains the settings for the 2d ortho projection
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (projection)

        float aspect = (float) window.currentFbo.width / (float) window.currentFbo.height;

        if (camera.projection == CAMERA_PERSPECTIVE){
            // Setup perspective projection
            double top = RL_CULL_DISTANCE_NEAR * Math.tan(camera.fovy * 0.5 * DEG2RAD);
            double right = top * aspect;

            context.rlgl.rlFrustum(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);

        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
            // Setup orthographic projection
            double top = camera.fovy / 2.0;
            double right = top * aspect;

            context.rlgl.rlOrtho(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }

        // NOTE: zNear and zFar values are important when computing depth buffer values

        context.rlgl.rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)

        // Setup rCamera view
        Matrix matView = MatrixLookAt(camera.position, camera.target, camera.up);
        context.rlgl.rlMultMatrixf(MatrixToFloat(matView));      // Multiply modelview matrix by view matrix (camera)
        context.rlgl.rlEnableDepthTest();                // Enable DEPTH_TEST for 3D
    }

    /**
     * Ends 3D mode and returns to default 2D orthographic mode
     */
    public void EndMode3D(){
        context.rlgl.rlDrawRenderBatchActive();                         // Process internal buffers (update + draw)

        context.rlgl.rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        context.rlgl.rlPopMatrix();                      // Restore previous matrix (projection) from matrix stack

        context.rlgl.rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)

        context.rlgl.rlMultMatrixf(MatrixToFloat(window.getScreenScale())); // Apply screen scaling if required

        context.rlgl.rlDisableDepthTest();               // Disable DEPTH_TEST for 2D
    }

    /**
     * Initializes render texture for drawing
     * @param target render texture target
     */
    public void BeginTextureMode(RenderTexture target){
        context.rlgl.rlDrawRenderBatchActive();                         // Draw Buffers (Only OpenGL 3+ and ES2)

        context.rlgl.rlEnableFramebuffer(target.getId());     // Enable render target

        // Set viewport and RLGL internal framebuffer size
        context.rlgl.rlViewport(0, 0, target.texture.width, target.texture.height);
        context.rlgl.rlSetFramebufferWidth(target.texture.width);
        context.rlgl.rlSetFramebufferHeight(target.texture.height);

        context.rlgl.rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (projection)

        // Set orthographic projection to current framebuffer size
        // NOTE: Configured top-left corner as (0, 0)
        context.rlgl.rlOrtho(0, target.texture.width, target.texture.height, 0, 0.0f, 1.0f);

        context.rlgl.rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)

        //context.rlgl.rlScalef(0.0f, -1.0f, 0.0f);      // Flip Y-drawing (?)

        // Setup current width/height for proper aspect ratio
        // calculation when using BeginMode3D()
        window.currentFbo.setWidth(target.texture.width);
        window.currentFbo.setHeight(target.texture.height);
        window.usingFbo = true;
    }

    /**
     * Ends drawing to render texture
     */
    public void EndTextureMode(){
        context.rlgl.rlDrawRenderBatchActive();                 // Draw Buffers (Only OpenGL 3+ and ES2)

        context.rlgl.rlDisableFramebuffer();     // Disable render target (fbo)

        // Set viewport to default framebuffer size
        SetupViewport(window.render.width, window.render.height);

        // Reset current screen size
        window.currentFbo.width = window.render.width;
        window.currentFbo.height = window.render.height;
        window.usingFbo = false;
    }

    /**
     * Begin custom shader mode
     * @param shader Shader to render
     */
    public void BeginShaderMode(Shader shader){
        context.rlgl.rlSetShader(shader.getId(), shader.getLocs());
    }

    /**
     * End custom shader mode and return to default shader
     */
    public void EndShaderMode(){
        context.rlgl.rlSetShader(context.rlgl.rlGetShaderIdDefault(), context.rlgl.rlGetShaderLocsDefault());
    }

    /**
     * Begin blending mode (alpha, additive, multiplied)<br/>
     * NOTE: Only 3 blending modes supported, default blend mode is alpha
     * @param mode
     */
    public void BeginBlendMode(rlBlendMode mode){
        context.rlgl.rlSetBlendMode(mode);
    }

    /**
     * End blending mode <br/>
     * Resets to default blending mode (alpha blending)
     */
    public void EndBlendMode(){
        context.rlgl.rlSetBlendMode(BLEND_ALPHA);
    }

    /**
     * Begin scissor mode (define screen area for following drawing)<br/>
     * @param x x coordinate for the top-left of the screen area
     * @param y y coordinate for the top-left of the screen area
     * @param width width of the screen area
     * @param height height of the screen area
     */
    public void BeginScissorMode(int x, int y, int width, int height){
        context.rlgl.rlDrawRenderBatchActive(); // Force drawing elements

        context.rlgl.rlEnableScissorTest();

        if (__APPLE__) {
            if (!window.usingFbo) {
                Vector2 scale = GetWindowScaleDPI();

                context.rlgl.rlScissor((int) (x * scale.x), (int) (GetScreenHeight() * scale.y - (((y + height) * scale.y))), (int) (width * scale.x), (int) (height * scale.y));
            }
        }
        else {
            if (!window.usingFbo) {
                if ((window.flags & FLAG_WINDOW_HIGHDPI) > 0) {
                    Vector2 scale = GetWindowScaleDPI();
                    context.rlgl.rlScissor((int) (x * scale.x), (int) (window.currentFbo.height - (y + height) * scale.y), (int) (width * scale.x), (int) (height * scale.y));
                }
            }
            else {
                context.rlgl.rlScissor(x, window.currentFbo.height - (y + height), width, height);
            }
        }

    }

    /**
     * End scissor mode
     */
    public void EndScissorMode(){
        context.rlgl.rlDrawRenderBatchActive(); // Force drawing elements
        context.rlgl.rlDisableScissorTest();
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: VR Stereo Rendering
    //----------------------------------------------------------------------------------

    /**
     * Begin VR drawing configuration
     * @param config <code>VrStereoConfig</code> to use in rendering
     */
    public void BeginVrStereoMode(VrStereoConfig config){
        context.rlgl.rlEnableStereoRenderer();

        // Set stereo render matrices
        context.rlgl.rlSetMatrixProjectionStereo(config.projection[0], config.projection[1]);
        context.rlgl.rlSetMatrixViewOffsetStereo(config.viewOffset[0], config.viewOffset[1]);

    }

    /**
     *  End VR drawing process (and desktop mirror)
     */
    public void EndVrStereoMode(){
        context.rlgl.rlDisableStereoRenderer();
    }

    /**
     * Load VR stereo config for VR simulator device parameters
     * @param device
     * @return
     */
    public VrStereoConfig LoadVrStereoConfig(VrDeviceInfo device){
        VrStereoConfig config = new VrStereoConfig();

        if (!GRAPHICS_API_OPENGL_11) {
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

            // Fovy is normally computed with: 2*atan2(device.vScreenSize, 2*device.eyeToScreenDistance)
            // ...but with lens distortion it is increased (see Oculus SDK Documentation)
            float fovY = (float) (2.0f*Math.atan2(device.vScreenSize*0.5f*distortionScale, device.eyeToScreenDistance));     // Really need distortionScale?
            //float fovY = 2.0f * (float) Math.atan2(device.vScreenSize * 0.5f, device.eyeToScreenDistance);

            // Compute camera projection matrices
            float projOffset = 4.0f * lensShift;      // Scaled to projection space coordinates [-1..1]
            Matrix proj = MatrixPerspective(fovY, aspect, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);

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
            context.tracelog.TRACELOG(LOG_WARNING, "RLGL: VR Simulator not supported on OpenGL 1.1");
        }

        return config;
    }

    /**
     * Unload VR stereo config properties
     * @param config
     */
    public void UnloadVrStereoConfig(VrStereoConfig config){
        context.tracelog.TRACELOG(LOG_INFO, "UnloadVrStereoConfig not implemented in rcore");
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Shaders Management
    //----------------------------------------------------------------------------------

    /**
     * Load shader from files and bind default locations
     * @param vsFileName path to vertex shader file
     * @param fsFileName path to fragment shader file
     * @return Compiled Shader
     */
    public Shader LoadShader(String vsFileName, String fsFileName){
        Shader shader = new Shader();

        String vShaderStr = null;
        String fShaderStr = null;

        if (vsFileName != null){
            try{
                vShaderStr = context.files.LoadFileText(vsFileName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        if (fsFileName != null){
            try{
                fShaderStr = context.files.LoadFileText(fsFileName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        shader = LoadShaderFromMemory(vShaderStr, fShaderStr);

        return shader;
    }

    /**
     * Load shader from code strings and bind default locations
     * @param vsCode vertex shader code
     * @param fsCode fragment shader code
     * @return Compiled Shader
     */
    public Shader LoadShaderFromMemory(String vsCode, String fsCode){
        Shader shader = new Shader();

        shader.id = context.rlgl.rlLoadShaderCode(vsCode, fsCode);

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
            shader.locs = new int[RL_MAX_SHADER_LOCATIONS];

            // All locations reset to -1 (no location)
            for (int i = 0; i < RL_MAX_SHADER_LOCATIONS; i++) {
                shader.locs[i] = -1;
            }

            // Get handles to GLSL input attribute locations
            shader.locs[SHADER_LOC_VERTEX_POSITION.GetLocation()] = context.rlgl.rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION);
            shader.locs[SHADER_LOC_VERTEX_TEXCOORD01.GetLocation()] = context.rlgl.rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD);
            shader.locs[SHADER_LOC_VERTEX_TEXCOORD02.GetLocation()] = context.rlgl.rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2);
            shader.locs[SHADER_LOC_VERTEX_NORMAL.GetLocation()] = context.rlgl.rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL);
            shader.locs[SHADER_LOC_VERTEX_TANGENT.GetLocation()] = context.rlgl.rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT);
            shader.locs[SHADER_LOC_VERTEX_COLOR.GetLocation()] = context.rlgl.rlGetLocationAttrib(shader.id, RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR);

            // Get handles to GLSL uniform locations (vertex shader)
            shader.locs[SHADER_LOC_MATRIX_MVP.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_MVP);
            shader.locs[SHADER_LOC_MATRIX_VIEW.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_VIEW);
            shader.locs[SHADER_LOC_MATRIX_PROJECTION.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_PROJECTION);
            shader.locs[SHADER_LOC_MATRIX_MODEL.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_MODEL);
            shader.locs[SHADER_LOC_MATRIX_NORMAL.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_NORMAL);

            // Get handles to GLSL uniform locations (fragment shader)
            shader.locs[SHADER_LOC_COLOR_DIFFUSE.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_UNIFORM_NAME_COLOR);
            shader.locs[SHADER_LOC_MAP_DIFFUSE.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE0);  // SHADER_LOC_MAP_ALBEDO
            shader.locs[SHADER_LOC_MAP_SPECULAR.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE1); // SHADER_LOC_MAP_METALNESS
            shader.locs[SHADER_LOC_MAP_NORMAL.GetLocation()] = context.rlgl.rlGetLocationUniform(shader.id, RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE2);
        }

        return shader;
    }

    /**
     * Check if a shader is ready
     * @param shader Shader to check
     * @return {@code true} if shader is ready for use
     */
    public boolean IsShaderReady(Shader shader){
        return shader.getLocs() != null;
    }

    /**
     * Unload shader from GPU memory (VRAM)
     * @param shader Shader to free
     */
    public void UnloadShader(Shader shader){
        if (shader.getId() != context.rlgl.rlGetShaderIdDefault()){
            context.rlgl.rlUnloadShaderProgram(shader.getId());
            shader.setLocs(null);
        }
    }

    /**
     * Get shader uniform location
     * @param shader Shader to evaluate
     * @param uniformName Name of uniform to locate
     * @return position of shader uniform
     */
    public int GetShaderLocation(Shader shader, String uniformName){
        return context.rlgl.rlGetLocationUniform(shader.getId(), uniformName);
    }

    /**
     * Get shader attribute location
     * @param shader Shader to evaluate
     * @param attribName Name of attribute to locate
     * @return position of shader attribute
     */
    public int GetShaderLocationAttrib(Shader shader, String attribName){
        return context.rlgl.rlGetLocationAttrib(shader.id, attribName);
    }

    /**
     * Set shader uniform value
     * @param shader
     * @param locIndex
     * @param value
     * @param uniformType
     */
    public void SetShaderValue(Shader shader, int locIndex, float[] value, rlShaderUniformDataType uniformType){
        SetShaderValueV(shader, locIndex, value, uniformType);
    }

    /**
     * Set shader uniform value vector
     * @param shader
     * @param locIndex
     * @param value
     * @param uniformType
     */
    public void SetShaderValueV(Shader shader, int locIndex, float[] value, rlShaderUniformDataType uniformType){
        if(locIndex > -1) {
            context.rlgl.rlEnableShader(shader.getId());
            context.rlgl.rlSetUniform(locIndex, value, uniformType);
            //rlDisableShader();      // Avoid resting current shader program, in case other uniforms are set
        }
    }

    /**
     * Set shader uniform value (matrix 4x4)
     * @param shader
     * @param locIndex
     * @param mat
     */
    public void SetShaderValueMatrix(Shader shader, int locIndex, Matrix mat){
        if(locIndex > -1) {
            context.rlgl.rlEnableShader(shader.getId());
            context.rlgl.rlSetUniformMatrix(locIndex, mat);
            //rlDisableShader();    // Avoid resting current shader program, in case other uniforms are set
        }
    }

    // Set shader uniform value for texture
    public void SetShaderValueTexture(Shader shader, int locIndex, Texture2D texture){
        if(locIndex > -1) {
            context.rlgl.rlEnableShader(shader.getId());
            context.rlgl.rlSetUniformSampler(locIndex, texture.getId());
            //rlDisableShader();    // Avoid resting current shader program, in case other uniforms are set
        }
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Screen-space Queries
    //----------------------------------------------------------------------------------

    /**
     * Returns a ray trace from mouse position
     * @param mouse
     * @param camera
     * @return
     */
    public Ray GetMouseRay(Vector2 mouse, Camera3D camera){
        Ray ray = new Ray();

        // Calculate normalized device coordinates
        // NOTE: y value is negative
        float x = (2.0f * mouse.x) / (float) GetScreenWidth() - 1.0f;
        float y = 1.0f - (2.0f * mouse.y) / (float) GetScreenHeight();
        float z = 1.0f;

        // Store values in a vector
        Vector3 deviceCoords = new Vector3(x, y, z);

        // Calculate view matrix from camera look at
        Matrix matView = MatrixLookAt(camera.position, camera.target, camera.up);

        Matrix matProj = MatrixIdentity();

        if (camera.projection == CAMERA_PERSPECTIVE){
            // Calculate projection matrix from perspective
            matProj = MatrixPerspective(camera.fovy * DEG2RAD,
                                        ((double) GetScreenWidth() / (double) GetScreenHeight()), RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
            float aspect = (float) window.screen.width / (float) window.screen.height;
            double top = camera.fovy / 2.0;
            double right = top * aspect;

            // Calculate projection matrix from orthographic
            matProj = MatrixOrtho(-right, right, -top, top, 0.01, 1000.0);
        }

        // Unproject far/near points
        Vector3 nearPoint = Vector3Unproject(new Vector3(deviceCoords.x, deviceCoords.y, 0.0f), matProj,
                                             matView);
        Vector3 farPoint = Vector3Unproject(new Vector3(deviceCoords.x, deviceCoords.y, 1.0f), matProj,
                                            matView);

        // Unproject the mouse cursor in the near plane.
        // We need this as the source position because orthographic projects, compared to perspective doesn't have a
        // convergence point, meaning that the "eye" of the camera is more like a plane than a point.
        Vector3 cameraPlanePointerPos = Vector3Unproject(new Vector3(deviceCoords.x, deviceCoords.y, -1.0f),
                                                         matProj, matView);

        // Calculate normalized direction vector
        Vector3 direction = Vector3Normalize(Vector3Subtract(farPoint, nearPoint));

        if (camera.projection == CAMERA_PERSPECTIVE){
            ray.position = camera.position;
        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
            ray.position = cameraPlanePointerPos;
        }

        // Apply calculated vectors to ray
        ray.direction = direction;

        return ray;
    }

    /**
     * Get transform matrix for camera
     * @param camera camera to evaluate
     * @return transform matrix
     */
    public Matrix GetCameraMatrix(Camera3D camera){
        return MatrixLookAt(camera.position, camera.target, camera.up);
    }

    /**
     * Returns camera 2D transform matrix
     *
     * @param camera 2D camera to evaluate
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
        Matrix matOrigin = MatrixTranslate(-camera.target.x, -camera.target.y, 0.0f);
        Matrix matRotation = MatrixRotate(new Vector3(0.0f, 0.0f, 1.0f), camera.getRotation() * DEG2RAD);
        Matrix matScale = MatrixScale(camera.getZoom(), camera.getZoom(), 1.0f);
        Matrix matTranslation = MatrixTranslate(camera.offset.x, camera.offset.y, 0.0f);

        matTransform = MatrixMultiply(MatrixMultiply(matOrigin, MatrixMultiply(matScale, matRotation)), matTranslation);

        return matTransform;
    }

    /**
     * Returns the screen space position from a 3d world space position
     * @param position
     * @param camera
     * @return
     */
    public Vector2 GetWorldToScreen(Vector3 position, Camera3D camera){
        return GetWorldToScreenEx(position, camera, GetScreenWidth(), GetScreenHeight());
    }

    /**
     * Returns size position for a 3d world space position (useful for texture drawing)
     * @param position
     * @param camera
     * @param width
     * @param height
     * @return
     */
    public Vector2 GetWorldToScreenEx(Vector3 position, Camera3D camera, int width, int height){
        // Calculate projection matrix (from perspective instead of frustum
        Matrix matProj = MatrixIdentity();

        if (camera.projection == CAMERA_PERSPECTIVE){
            // Calculate projection matrix from perspective
            matProj = MatrixPerspective(camera.fovy * DEG2RAD, ((double) width / (double) height),
                                        RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }
        else if (camera.projection == CAMERA_ORTHOGRAPHIC){
            float aspect = (float) window.screen.width / (float) window.screen.height;
            double top = camera.fovy / 2.0;
            double right = top * aspect;

            // Calculate projection matrix from orthographic
            matProj = MatrixOrtho(-right, right, -top, top, RL_CULL_DISTANCE_NEAR, RL_CULL_DISTANCE_FAR);
        }

        // Calculate view matrix from camera look at (and transpose it)
        Matrix matView = MatrixLookAt(camera.position, camera.position, camera.up);

        // Convert world position vector to quaternion
        Quaternion worldPos = new Quaternion(position.x, position.y, position.getZ(), 1.0f);

        // Transform world position to view
        worldPos = QuaternionTransform(worldPos, matView);

        // Transform result to projection (clip space position)
        worldPos = QuaternionTransform(worldPos, matProj);

        // Calculate normalized device coordinates (inverted y)
        Vector3 ndcPos = new Vector3(worldPos.x / worldPos.getW(), -worldPos.y / worldPos.getW(),
                                     worldPos.getZ() / worldPos.getW());

        // Calculate 2d screen position vector
        Vector2 screenPosition = new Vector2((ndcPos.x + 1.0f) / 2.0f * (float) width,
                                             (ndcPos.y + 1.0f) / 2.0f * (float) height);

        return screenPosition;
    }

    /**
     * Returns the screen space position for a 2d camera world space position
     * @param position
     * @param camera
     * @return
     */
    public Vector2 GetWorldToScreen2D(Vector2 position, Camera2D camera){
        Matrix matCamera = GetCameraMatrix2D(camera);
        Vector3 transform = Vector3Transform(new Vector3(position.x, position.y, 0), matCamera);

        return new Vector2(transform.x, transform.y);
    }

    /**
     * Returns the world space position for a 2d camera screen space position
     * @param position
     * @param camera
     * @return
     */
    public Vector2 GetScreenToWorld2D(Vector2 position, Camera2D camera){
        Matrix invMatCamera = MatrixInvert(GetCameraMatrix2D(camera));
        Vector3 transform = Vector3Transform(new Vector3(position.x, position.y, 0), invMatCamera);

        return new Vector2(transform.x, transform.y);
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Timming
    //----------------------------------------------------------------------------------

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

        context.tracelog.TRACELOG(LOG_INFO, "TIMER: Target time per frame: " + time.getTarget() * 1000.0f + " milliseconds");
    }

    /**
     * Returns current FPS
     * NOTE: We calculate an average frame rate
     *
     * @return Current average frame rate
     */
    public int GetFPS() {
        int fps = 0;

        if(!SUPPORT_CUSTOM_FRAME_CONTROL) {
            int FPS_CAPTURE_FRAMES_COUNT = 30;      // 30 captures
            float FPS_AVERAGE_TIME_SECONDS = 0.5f;     // 500 milliseconds
            float FPS_STEP = (FPS_AVERAGE_TIME_SECONDS / FPS_CAPTURE_FRAMES_COUNT);

            int index = 0;
            float[] history = new float[FPS_CAPTURE_FRAMES_COUNT];
            float average = 0, last = 0;
            float fpsFrame = GetFrameTime();

            // if we reset the window, reset the FPS info
            if (time.frameCounter == 0) {
                average = 0;
                last = 0;
            }

            if (fpsFrame == 0) {
                return 0;
            }

            if ((GetTime() - last) > FPS_STEP) {
                last = (float) GetTime();
                index = (index + 1) % FPS_CAPTURE_FRAMES_COUNT;
                average -= history[index];
                history[index] = fpsFrame / FPS_CAPTURE_FRAMES_COUNT;
                average += history[index];
            }

            fps =  Math.round(1.0f / average);
        }

        return fps;
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
     * Get elapsed time measure in seconds since InitTimer()<br/>
     * NOTE: On PLATFORM_DESKTOP InitTimer() is called on InitWindow()<br/>
     * NOTE: On PLATFORM_DESKTOP, timer is initialized on glfwInit()
     *
     * @return Time program has been running in seconds
     */
    public double GetTime(){
        return glfwGetTime();
    }

    /**
     * Set the seed for the random number generator
      * @param seed RNG seed
     */
    public void SetRandomSeed(long seed){
        random.setSeed(seed);
    }

    /**
     * Returns a random value between min and max (both included)
     *
     * @param min Minimum value of random number
     * @param max Maximum value of random number
     * @return Random value between the <code>min</code> and <code>max</code>
     */
    public int GetRandomValue(int min, int max){
        if (min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }

        return (int) (random.nextDouble() * (max - min + 1) + min);
    }

    /**
     * Load random values sequence, no values repeated
     * @param count
     * @param min
     * @param max
     * @return
     */
    public int[] LoadRandomSequence(long count, int min, int max){
        int[] values = new int[(int) count];

        // Security check
        if (count > (Math.abs(max - min) + 1)){
            return null;
        }

        int value;
        boolean dupValue;

        for (int i = 0; i < count; ){
            value = GetRandomValue(min, max);
            dupValue = false;

            for (int j = 0; j < i; j++){
                if (values[j] == value){
                    dupValue = true;
                    break;
                }
            }

            if (!dupValue){
                values[i] = value;
                i++;
            }
        }

        return values;
    }

    /**
     * Unload random values sequence
     */
    public void UnloadRandomSequence(@SuppressWarnings({"ReassignedVariable", "ParameterCanBeLocal"}) int[] sequence){
        // noinspection UnusedAssignment
        sequence = null;
    }

    /**
     * Takes a screenshot of current screen (saved a .png)<br/>
     * NOTE: This function could work in any platform but some platforms: PLATFORM_ANDROID and PLATFORM_WEB have their own internal file-systems,
     * to download image to user file-system some additional mechanism is required
     * @param fileName
     */
    public void TakeScreenshot(String fileName){
        if (SUPPORT_MODULE_RTEXTURES) {
            if (fileName.contains("\\")) {
                context.tracelog.TRACELOG(LOG_WARNING, "SYSTEM: Provided fileName could be potentially malicious, avoid [\\'] character");
                return;
            }

            Vector2 scale = GetWindowScaleDPI();

            short[] imgData = context.rlgl.rlReadScreenPixels((int)((float)window.render.width*scale.x), (int)((float)window.render.height*scale.y));
            byte[] dataB = new byte[imgData.length];
            IntStream.range(0, dataB.length).forEach(i -> dataB[i] = (byte) imgData[i]);
            Image image = new Image(dataB, window.render.width, window.render.height, PIXELFORMAT_UNCOMPRESSED_R8G8B8A8, 1);

            String path = GetWorkingDirectory() + fileName;

            context.textures.ExportImage(image, path); // WARNING: Module required: rtextures

            // TODO: Verification required for log
            context.tracelog.TRACELOG(LOG_INFO, "SYSTEM: [" + path + "] Screenshot taken successfully");
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "IMAGE: ExportImage() requires module: rtextures");
        }
    }

    /**
     * Setup window configuration flags (view FLAGS)<br/>
     * NOTE: This function is expected to be called before window creation, because it sets up some flags for the window creation process.<br/>
     * To configure window states after creation, just use SetWindowState()
     * @param flags
     */
    public void SetConfigFlags(int flags){
        // Selected flags are set but not evaluated at this point,
        // flag evaluation happens at InitWindow() or SetWindowState()
        window.flags |= flags;
    }

    public void SetTraceLogCallback(TraceLog callback) {
        context.tracelog = callback;
    }

    /**
     * Check if the file exists
     * @param fileName
     * @return
     */
    public boolean FileExists(String fileName){
        File file = new File(fileName);

        return file.exists();
    }

    /**
     * Check file extension <br/>
     * NOTE: Extensions checking is not case-sensitive
     * @param fileName
     * @param ext list of file extensions. Multiple extensions can be passed separated by a ";"
     * @return true if passed file name has an extension that matches
     */
    public boolean IsFileExtension(String fileName, String ext){
        String fileExt = GetFileExtension(fileName);
        String[] extPattern = ext.split(";");
        boolean result = false;
        for (String s : extPattern) {
            result = fileExt.equalsIgnoreCase(s);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if directory exists <br/>
     * NOTE: Extensions checking is not case-sensitive
     * @param directoryName
     * @return
     */
    public boolean DirectoryExists(String directoryName){
        File tmp = new File(directoryName);
        return tmp.isDirectory();
    }

    /**
     * Get file length in byres
     * @param fileName
     * @return
     */
    public int GetFileLength(String fileName) {
        File tmp = new File(fileName);
        return (int) tmp.length();
    }

    /**
     *
     * @param fileName
     * @return
     */
    public String GetFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    public String strptrbrk(String string, String charset) {
        int right = string.lastIndexOf(charset);
        return string.substring(right, right + charset.length());
    }

    /**
     * Get filename for a path string
     * @param filePath
     * @return
     */
    public String GetFileName(String filePath){
        filePath = filePath.replace('\\', '/');

        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf('/'));
        }
        else {
            return filePath;
        }
    }

    /**
     * Get filename string without extension (uses static string)
     * @param filePath
     * @return
     */
    public String GetFileNameWithoutExt(String filePath){

        filePath = filePath.replace('\\', '/');

        return filePath.substring(filePath.lastIndexOf('/'), filePath.lastIndexOf('.'));
    }

    /**
     * Get directory for a given filePath
     * @param filePath
     * @return
     */
    public String GetDirectoryPath(String filePath) {
        String dirPath = "";

        if (filePath.contains("\\")) {
            dirPath = filePath.substring(0, filePath.lastIndexOf("\\"));
        }
        else if (filePath.contains("/")) {
            dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        }

        return dirPath;
    }

    /**
     * Get previous directory path for a given path
     * @param dirPath
     * @return
     */
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

    /**
     *
     * @return
     */
    public String GetApplicationDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * Get filenames in a directory path (max 512 files)
     * @param dirPath
     * @return
     */
    public FilePathList LoadDirectoryFiles(String dirPath) {
        FilePathList files = new FilePathList();

        //Todo

        return files;
    }

    /**
     *
     * @param basePath
     * @param filter
     * @param scanSubdirs
     * @return
     */
    public FilePathList LoadDirectoryFilesEx(String basePath, String filter, boolean scanSubdirs) {
        FilePathList files = new FilePathList();

        if(scanSubdirs) {
            files = ScanDirectoryFilesRecursively(basePath, filter);
        }
        else {
            files = ScanDirectoryFiles(basePath, filter);
        }

        return files;
    }

    /**
     * Clear directory files paths buffers
     */
    public void UnloadDirectoryFiles() {
        if (dirFileCount > 0) {
            dirFilesPath = null;
            dirFileCount = 0;
        }
    }

    // ChangeDirectory

    /**
     * Check if a file has been dropped into window
     * @return
     */
    public boolean IsFileDropped(){
        return (window.dropFilesCount > 0);
    }

    /**
     * Get dropped files names
     * @return
     */
    public FilePathList LoadDroppedFiles(){
        FilePathList files = new FilePathList();

        files.count = window.dropFilesCount;
        files.paths = window.dropFilePaths;

        return files;
    }

    /**
     * Get number of dropped files
     * @return number of registered dropped files
     */
    public int GetDroppedFilesCount(){
        return window.dropFilesCount;
    }

    /**
     * Clear dropped file information from the system
     * @param files
     */
    public void UnloadDroppedFiles(FilePathList files){
        if (window.getDropFilesCount() > 0){
            for (int i = 0; i < window.getDropFilesCount(); i++){
                window.dropFilePaths[i] = null;
            }
            window.setDropFilePaths(null);
            window.setDropFilesCount(0);
            files = null;
        }
    }

    /**
     * Clear dropped file information from the system
     */
    public void UnloadDroppedFiles(){
        if (window.getDropFilesCount() > 0){
            for (int i = 0; i < window.getDropFilesCount(); i++){
                window.dropFilePaths[i] = null;
            }
            window.setDropFilePaths(null);
            window.setDropFilesCount(0);
        }
    }

    /**
     * Get file modification time (last write time)
     * @param fileName
     * @return
     */
    public long GetFileModTime(String fileName) {
        long result = 0L;

        if (FileExists(fileName)){
            File tmp = new File(fileName);
            result = tmp.lastModified();
        }

        return result;
    }


    //----------------------------------------------------------------------------------
    // Module Functions Definition: Compression and Encoding
    //----------------------------------------------------------------------------------

    //TODO: 3/20/21
    // CompressData
    // DecompressData


    /**
     * Encode data to Base64 string
     * @param data
     * @param dataLength
     * @param outputLength
     * @return
     */
    public byte[] EncodeDataBase64(byte[] data, int dataLength, int outputLength) {
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

        for (int i = 0, j = 0; i < dataLength; ) {
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

    /**
     * Decode Base64 string data
     * @param data
     * @param outputLength
     * @return
     */
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

        for (int i = 0; i < outLength/3; i++) {
            byte a = base64decodeTable[data[4*i]];
            byte b = base64decodeTable[data[4*i + 1]];
            byte c = base64decodeTable[data[4*i + 2]];
            byte d = base64decodeTable[data[4*i + 3]];

            decodedData[3*i] = (byte) ((byte) (a << 2) | (b >> 4));
            decodedData[3*i + 1] = (byte) ((byte) (b << 4) | (c >> 2));
            decodedData[3*i + 2] = (byte) ((byte) (c << 6) | d);
        }

        if (outLength%3 == 1) {
            int n = outLength/3;
            byte a = base64decodeTable[data[4*n]];
            byte b = base64decodeTable[data[4*n + 1]];
            decodedData[outLength - 1] = (byte) ((byte) (a << 2) | (b >> 4));
        }
        else if (outLength%3 == 2) {
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

    public void OpenURL(String url) {
        platform.OpenURL(url);
    }

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Input Handling: Keyboard
    //----------------------------------------------------------------------------------

    // Detect if a key has been pressed once
    public boolean IsKeyPressed(int key){
        return ((!input.keyboard.getPreviousKeyState()[key]) && (input.keyboard.getCurrentKeyState()[key]));
    }

    // Detect if a key has been pressed again (Only PLATFORM_DESKTOP)
    public boolean IsKeyPressedRepeat(int key){
        if ((key > 0) && (key < MAX_KEYBOARD_KEYS)){
            return input.keyboard.keyRepeatInFrame[key];
        }

        return false;
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
    public int GetKeyPressed() {
        int value = 0;
        if (input.keyboard.keyPressedQueueCount > 0) {
            // Get character from the queue head
            value = input.keyboard.keyPressedQueue[0];
            // Shift elements 1 step toward the head.
            for (int i = 0; i < (input.keyboard.keyPressedQueueCount - 1); i++) {
                input.keyboard.keyPressedQueue[i] = input.keyboard.keyPressedQueue[i + 1];
            }
        }

        // Reset last character in the queue
        input.keyboard.charPressedQueue[input.keyboard.charPressedQueueCount - 1] = 0;
        input.keyboard.charPressedQueueCount--;

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

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Input Handling: Gamepad
    //----------------------------------------------------------------------------------

    // NOTE: Gamepad support not implemented in emscripten GLFW3 (PLATFORM_WEB)
    // Detect if a gamepad is available
    public boolean IsGamepadAvailable(int gamepad) {
        return (gamepad < MAX_GAMEPADS) & input.gamepad.getReady()[gamepad];
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

    public float GetGamepadAxisMovement(int gamepad, Gamepad.GamepadAxis axis) {
        return GetGamepadAxisMovement(gamepad, axis.GetValue());
    }

    // Return axis movement vector for a gamepad
    public float GetGamepadAxisMovement(int gamepad, int axis){
        float value = 0;

        if ((gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (axis < MAX_GAMEPAD_AXIS) &&
                (Math.abs(input.gamepad.getAxisState()[gamepad][axis]) > 0.1f)){
            value = input.gamepad.getAxisState()[gamepad][axis];      // 0.1f = GAMEPAD_AXIS_MINIMUM_DRIFT/DELTA
        }

        return value;
    }

    public boolean IsGamepadButtonPressed(int gamepad, Gamepad.GamepadButton button) {
        return IsGamepadButtonPressed(gamepad, button.GetValue());
    }

    // Detect if a gamepad button has been pressed once
    public boolean IsGamepadButtonPressed(int gamepad, int button){
        return ((gamepad < MAX_GAMEPADS) && input.gamepad.ready[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.previousButtonState[gamepad][button] == 0) && (input.gamepad.currentButtonState[gamepad][button] == 1));
    }

    public boolean IsGamepadButtonDown(int gamepad, Gamepad.GamepadButton button) {
        return IsGamepadButtonDown(gamepad, button.GetValue());
    }

    // Detect if a gamepad button is being pressed
    public boolean IsGamepadButtonDown(int gamepad, int button){
        return ((gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.getCurrentButtonState()[gamepad][button] == 1));
    }

    public boolean IsGamepadButtonReleased(int gamepad, Gamepad.GamepadButton button) {
        return IsGamepadButtonReleased(gamepad, button.GetValue());
    }

    // Detect if a gamepad button has NOT been pressed once
    public boolean IsGamepadButtonReleased(int gamepad, int button){
        return ((gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.getPreviousButtonState()[gamepad][button] == 1) && (input.gamepad.getCurrentButtonState()[gamepad][button] == 0));
    }

    public boolean IsGamepadButtonUp(int gamepad, Gamepad.GamepadButton button) {
        return IsGamepadButtonUp(gamepad, button.GetValue());
    }

    // Detect if a gamepad button is NOT being pressed
    public boolean IsGamepadButtonUp(int gamepad, int button){
        return ((gamepad < MAX_GAMEPADS) && input.gamepad.getReady()[gamepad] && (button < MAX_GAMEPAD_BUTTONS) &&
                (input.gamepad.getCurrentButtonState()[gamepad][button] == 0));
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

    //----------------------------------------------------------------------------------
    // Module Functions Definition: Input Handling: Mouse
    //----------------------------------------------------------------------------------

    public boolean IsMouseButtonPressed(Mouse.MouseButton button) {
        return IsMouseButtonPressed(button.GetValue());
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

    public boolean IsMouseButtonDown(Mouse.MouseButton button) {
        return IsMouseButtonDown(button.GetValue());
    }

    // Detect if a mouse button is being pressed
    public boolean IsMouseButtonDown(int button){
        boolean up = false;

        if (input.mouse.currentButtonState[button] == 0) {
            up = true;
        }

        // NOTE: Touches are considered like mouse buttons
        if (!input.touch.currentTouchState[button]) {
            up = true;
        }

        return up;
    }

    public boolean IsMouseButtonReleased(Mouse.MouseButton button) {
        return IsMouseButtonReleased(button.GetValue());
    }

    // Detect if a mouse button has been released once
    public boolean IsMouseButtonReleased(int button){
        boolean released = false;

        if ((input.mouse.getCurrentButtonState()[button] == 0) && (input.mouse.getPreviousButtonState()[button] == 1)) {
            released = true;
        }

        // Map touches to mouse buttons checking
        if ((!input.touch.currentTouchState[button]) && (input.touch.previousTouchState[button])){
            released = true;
        }

        return released;
    }

    public boolean IsMouseButtonUp(Mouse.MouseButton button) {
        return !IsMouseButtonDown(button.GetValue());
    }

    // Detect if a mouse button is NOT being pressed
    public boolean IsMouseButtonUp(int button){
        return !IsMouseButtonDown(button);
    }

    // Returns mouse position X
    public int GetMouseX(){
        return (int) ((input.mouse.currentPosition.x + input.mouse.offset.x) * input.mouse.scale.x);
    }

    // Returns mouse position Y
    public int GetMouseY(){
        return (int) ((input.mouse.currentPosition.y + input.mouse.offset.y) * input.mouse.scale.y);
    }

    // Returns mouse position XY
    public Vector2 GetMousePosition(){
        Vector2 position = new Vector2();

        position.x = ((input.mouse.currentPosition.x + input.mouse.offset.x) * input.mouse.scale.x);
        position.y = ((input.mouse.currentPosition.y + input.mouse.offset.y) * input.mouse.scale.y);

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
        platform.SetMousePosition(x, y);
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
    public void SetMouseCursor(Mouse.MouseCursor cursor){
        platform.SetMouseCursor(cursor);
    }

    /* TODO: Touch support - MODULE GESTURES
    // Returns touch position X for touch point 0 (relative to screen size)
    int GetTouchX(void)
    {
        if(PLATFORM_ANDROID) || defined(PLATFORM_WEB) || defined(PLATFORM_UWP)
            return (int)input.Touch.position[0].x;
        #else   // PLATFORM_DESKTOP, PLATFORM_RPI, PLATFORM_DRM
            return GetMouseX();
        #endif
    }

    // Returns touch position Y for touch point 0 (relative to screen size)
    int GetTouchY(void)
    {
        if(PLATFORM_ANDROID) || defined(PLATFORM_WEB) || defined(PLATFORM_UWP)
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

        if(PLATFORM_DESKTOP)
            // TODO: GLFW does not support multi-touch input just yet
            // https://www.codeproject.com/Articles/668404/Programming-for-Multi-Touch
            // https://docs.microsoft.com/en-us/windows/win32/wintouch/getting-started-with-multi-touch-messages
            if (index == 0) position = GetMousePosition();
        #endif
        if(PLATFORM_ANDROID)
            if (index < MAX_TOUCH_POINTS) position = input.Touch.position[index];
            else context.logger.logger(LOG_WARNING, "INPUT: Required touch point out of range (Max touch points: %i)", MAX_TOUCH_POINTS);

            if ((window.screen.width > window.display.width) || (window.screen.height > window.display.height))
            {
                position.x = position.x*((float)window.screen.width/(float)(window.display.width - window.renderOffset.x)) - window.renderOffset.x/2;
                position.y = position.y*((float)window.screen.height/(float)(window.display.height - window.renderOffset.y)) - window.renderOffset.y/2;
            }
            else
            {
                position.x = position.x*((float)window.render.width/(float)window.display.width) - window.renderOffset.x/2;
                position.y = position.y*((float)window.render.height/(float)window.display.height) - window.renderOffset.y/2;
            }
        #endif
        if(PLATFORM_WEB) || defined(PLATFORM_RPI) || defined(PLATFORM_DRM) || defined(PLATFORM_UWP)
            if (index < MAX_TOUCH_POINTS) position = input.Touch.position[index];
            else context.logger.logger(LOG_WARNING, "INPUT: Required touch point out of range (Max touch points: %i)", MAX_TOUCH_POINTS);

            // TODO: Touch position scaling required?
        #endif

        return position;
    }
    */

    //----------------------------------------------------------------------------------
    // Module internal Functions Definition
    //----------------------------------------------------------------------------------

    /**
     * Initialize hi-resolution timer
     */
    public void InitTimer(){
        time.setPrevious(GetTime());       // Get time as double
    }

    /**
     *
     * @param width
     * @param height
     */
    public void SetupViewport(int width, int height){
        window.render.setWidth(width);
        window.render.setHeight(height);

        // Set viewport width and height
        // NOTE: We consider render size and offset in case black bars are required and
        // render area does not match full display area (this situation is only applicable on fullscreen mode)
        context.rlgl.rlViewport((int) window.renderOffset.x / 2, (int) window.renderOffset.y / 2,
                        (int) (window.render.width - window.renderOffset.x),
                        (int) (window.render.height - window.renderOffset.y));

        context.rlgl.rlMatrixMode(RL_PROJECTION);        // Switch to projection matrix
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (projection)

        // Set orthographic projection to current framebuffer size
        // NOTE: Configured top-left corner as (0, 0)
        context.rlgl.rlOrtho(0, window.render.width, window.render.height, 0, 0.0f, 1.0f);

        context.rlgl.rlMatrixMode(RL_MODELVIEW);         // Switch back to modelview matrix
        context.rlgl.rlLoadIdentity();                   // Reset current matrix (modelview)
    }

    /**
     * Compute framebuffer size relative to screen size and display size <br/>
     * NOTE: Global variables window.render.width/window.render.height and window.renderOffset.x/window.renderOffset.y can be modified
     * @param width
     * @param height
     */
    public void SetupFramebuffer(int width, int height){
        // Calculate window.render.width and window.render.height, we have the display size (input params) and the desired screen size (global var)
        if ((window.screen.width > window.display.width) || (window.screen.height > window.display.height)){
            context.tracelog.TRACELOG(LOG_WARNING, "DISPLAY: Downscaling required: Screen size (" + window.screen.width + "x" +
                    window.screen.height + ") is bigger than display size " + "(" + window.display.width + "x" +
                    window.display.height + ")");

            // Downscaling to fit display with border-bars
            float widthRatio = (float) window.display.width / (float) window.screen.width;
            float heightRatio = (float) window.display.height / (float) window.screen.height;

            if (widthRatio <= heightRatio){
                window.render.setWidth(window.display.width);
                window.render.setHeight(Math.round((float) window.screen.height * widthRatio));
                window.renderOffset.x = 0;
                window.renderOffset.y = (window.display.height - window.render.height);
            }
            else{
                window.render.setWidth((Math.round((float) window.screen.width * heightRatio)));
                window.render.setHeight(window.display.height);
                window.renderOffset.x = (window.display.width - window.render.width);
                window.renderOffset.y = 0;
            }

            // Screen scaling required
            float scaleRatio = (float) window.render.width / (float) window.screen.width;
            window.screenScale = MatrixScale(scaleRatio, scaleRatio, 1.0f);

            // NOTE: We render to full display resolution!
            // We just need to calculate above parameters for downscale matrix and offsets
            window.render.setWidth(window.display.width);
            window.render.setHeight(window.display.height);

            context.tracelog.TRACELOG(LOG_WARNING, "DISPLAY: Downscale matrix generated, content will be rendered at (" +
                    window.render.width + "x" + window.render.height + ")");
        }
        else if ((window.screen.width < window.display.width) || (window.screen.height < window.display.height)){
            // Required screen size is smaller than display size
            context.tracelog.TRACELOG(LOG_INFO, "DISPLAY: Upscaling required: Screen size (" + window.screen.width + "x" +
                    window.screen.height + ") smaller than display size (" + window.display.width + "x" +
                    window.display.height + ")");

            if ((window.screen.width == 0) || (window.screen.height == 0)){
                window.screen.setWidth(window.display.width);
                window.screen.setHeight(window.display.height);
            }

            // Upscaling to fit display with border-bars
            float displayRatio = (float) window.display.width / (float) window.display.height;
            float screenRatio = (float) window.screen.width / (float) window.screen.height;

            if (displayRatio <= screenRatio){
                window.render.setWidth(window.screen.width);
                window.render.setHeight(Math.round((float) window.screen.width / displayRatio));
                window.renderOffset.setX(0);
                window.renderOffset.setY((window.render.height - window.screen.height));
            }
            else{
                window.render.setWidth(Math.round((float) window.screen.height * displayRatio));
                window.render.setHeight(window.screen.height);
                window.renderOffset.setX((window.render.width - window.screen.width));
                window.renderOffset.setY(0);
            }
        }
        else{
            window.render.setWidth(window.screen.width);
            window.render.setHeight(window.screen.height);
            window.renderOffset.setX(0);
            window.renderOffset.setY(0);
        }
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

    //todo
    private FilePathList ScanDirectoryFiles(String basePath, String filter) {
        FilePathList files = new FilePathList();



        return null;
    }

    //todo
    private FilePathList ScanDirectoryFilesRecursively(String basePath, String filter) {
        FilePathList files = new FilePathList();



        return null;
    }

    /**
     * Swap back buffer with front buffer (screen drawing)
     */
    public void SwapScreenBuffer(){
        platform.SwapScreenBuffer();
    }

    /**
     * Poll (store) all input events
     */
    public void PollInputEvents() {
        platform.PollInputEvents();
    }

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
            repFile = context.files.LoadFileText(fileName).split("\n");
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
                context.tracelog.TRACELOG(LOG_WARNING, "Events count provided is different than count");
            }

        }

        context.tracelog.TRACELOG(LOG_WARNING, "Events loaded: " + eventCount);
    }

    /**
     * Export recorded events into a file
     * @param fileName
     */
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
                context.files.SaveFileText(fileName, repFileText.toString());
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    /**
     * Check event in current frame and save into the events[i] array
     * @param frame
     */
    public void RecordAutomationEvent(int frame) {
        for (int key = 0; key < MAX_KEYBOARD_KEYS; key++) {
            // INPUT_KEY_UP (only saved once)
            if (input.keyboard.previousKeyState[key] && !input.keyboard.currentKeyState[key]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_KEY_UP.ordinal();
                events.get(eventCount).params[0] = key;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_KEY_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_KEY_DOWN
            if (input.keyboard.currentKeyState[key]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_KEY_DOWN.ordinal();
                events.get(eventCount).params[0] = key;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_KEY_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
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

                context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_BUTTON_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_MOUSE_BUTTON_DOWN
            if (input.mouse.currentButtonState[button] == 1) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_MOUSE_BUTTON_DOWN.ordinal();
                events.get(eventCount).params[0] = button;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_BUTTON_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
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

            context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_POSITION: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
            eventCount++;
        }

        // INPUT_MOUSE_WHEEL_MOTION
        if (input.mouse.currentWheelMove != input.mouse.previousWheelMove) {
            events.get(eventCount).frame = frame;
            events.get(eventCount).type = INPUT_MOUSE_WHEEL_MOTION.ordinal();
            events.get(eventCount).params[0] = (int) input.mouse.currentWheelMove.x;
            events.get(eventCount).params[1] = (int) input.mouse.currentWheelMove.y;
            events.get(eventCount).params[2] = 0;

            context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_MOUSE_WHEEL_MOTION: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
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

                context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_TOUCH_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                eventCount++;
            }

            // INPUT_TOUCH_DOWN
            if (input.touch.currentTouchState[id]) {
                events.get(eventCount).frame = frame;
                events.get(eventCount).type = INPUT_TOUCH_DOWN.ordinal();
                events.get(eventCount).params[0] = id;
                events.get(eventCount).params[1] = 0;
                events.get(eventCount).params[2] = 0;

                context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_TOUCH_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
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

                context.logger.logger(LOG_INFO, "[%i] INPUT_TOUCH_POSITION: %i, %i, %i", events.get(eventCount).frame, events.get(eventCount).params[0], events.get(eventCount).params[1], events.get(eventCount).params[2]);
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

                    context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GAMEPAD_BUTTON_UP: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
                    eventCount++;
                }

                // INPUT_GAMEPAD_BUTTON_DOWN
                if (input.gamepad.currentButtonState[gamepad][button] == 1) {
                    events.get(eventCount).frame = frame;
                    events.get(eventCount).type = INPUT_GAMEPAD_BUTTON_DOWN.ordinal();
                    events.get(eventCount).params[0] = gamepad;
                    events.get(eventCount).params[1] = button;
                    events.get(eventCount).params[2] = 0;

                    context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GAMEPAD_BUTTON_DOWN: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
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

                    context.tracelog.TRACELOG(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GAMEPAD_AXIS_MOTION: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
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

            context.logger.logger(LOG_INFO, "[" + events.get(eventCount).frame + "] INPUT_GESTURE: " + events.get(eventCount).params[0] + ", " + events.get(eventCount).params[1] + ", " + events.get(eventCount).params[2]);
            eventCount++;
        }
        */
    }

    /**
     * Play automation event
     * @param frame
     */
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
