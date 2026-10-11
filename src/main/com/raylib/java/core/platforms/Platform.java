package com.raylib.java.core.platforms;

import com.raylib.java.core.input.Mouse;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Vector2;

import java.util.List;

public interface Platform {

    //----------------------------------------------------------------------------------
    // Module Functions Declaration
    //----------------------------------------------------------------------------------

    /**
     * Initialize platform: graphics, inputs and more
     * @return {@code 0} if platform successfully initializes, else returns a value greater than 0 on error
     */
    int InitPlatform();

    /**
     * Close platform
     */
    void ClosePlatform();

    /**
     * Check if application should close <br/>
     * NOTE: By default, if KEY_ESCAPE pressed or window close icon clicked
     * @return {@code true} if application close event has been triggered
     */
    boolean WindowShouldClose();

    /**
     * Toggle fullscreen mode
     */
    void ToggleFullscreen();

    /**
     * Toggle borderless windowed mode
     */
    void ToggleBorderlessWindowed();

    /**
     * Set window state: maximized, if resizable
     */
    void MaximizeWindow();

    /**
     * Set window state: minimized
     */
    void MinimizeWindow();

    /**
     * Set window state: not minimized/maximized
     */
    void RestoreWindow();

    /**
     * Set window configuration state using flags
     * @param flags configuration flags to set
     * @see com.raylib.java.Config.ConfigFlag
     */
    void SetWindowState(int flags);

    /**
     * Clear window configuration state flags
     * @param flags configuration flags to clear
     * @see com.raylib.java.Config.ConfigFlag
     */
    void ClearWindowState(int flags);

    /**
     * Set icon for window
     * @param image {@code Imgae} to be set formated as {@code rlPixelFormat.PIXELFORMAT_UNCOMPRESSED_R8G8B8A8}
     * @see com.raylib.java.rlgl.RLGL.rlPixelFormat#PIXELFORMAT_UNCOMPRESSED_R8G8B8A8
     */
    void SetWindowIcon(Image image);

    /**
     * Set icons for window (multiple images, only PLATFORM_DESKTOP)<br/>
     * The images are used depending on provided sizes<br/>
     * Standard Windows icon sizes: 256, 128, 96, 64, 48, 32, 24, 16
     *
     * @param images {@code List} of RGBA, 8 bits per channel formatted images to set as window icons.
     */
    void SetWindowIcons(List<Image> images);

    /**
     * Set title for window (only PLATFORM_DESKTOP)
     *
     * @param title String to be displayed on window title bar
     */
    void SetWindowTitle(String title);

    /**
     * Set window position on screen (windowed mode)
     * @param x Window upper-left corner x coordinate
     * @param y Window upper-left corner y coordinate
     */
    void SetWindowPosition(int x, int y);

    /**
     * Set monitor for the current window (fullscreen mode)
     *
     * @param monitor GLFW monitor handle to display on.
     */
    void SetWindowMonitor(int monitor);

    /**
     * Set window minimum dimensions (FLAG_WINDOW_RESIZABLE)
     *
     * @param width  minimum width (in pixels) the window must be
     * @param height minimum height (in pixels) the window must be
     */
    void SetWindowMinSize(int width, int height);

    /**
     * Set window maximum dimensions (FLAG_WINDOW_RESIZABLE)
     *
     * @param width  maximum width (in pixels) the window must be
     * @param height maximum height (in pixels) the window must be
     */
    void SetWindowMaxSize(int width, int height);

    /**
     * Set window dimensions
     * @param width Window width
     * @param height Window height
     */
    void SetWindowSize(int width, int height);

    /**
     * Set window opacity
     * @param opacity Normalised opacity value [0.0f ... 1.0f]
     */
    void SetWindowOpacity(float opacity);

    /**
     * Set window focused
     */
    void SetWindowFocused();

    /**
     * Get native window handle
     * @return Window handle
     */
    long GetWindowHandle();

    /**
     * Get window position XY on monitor
     * @return XY coordinate of window on the current monitor
     */
    Vector2 GetWindowPosition();

    /**
     * Get window scale DPI factor for current monitor
     * @return DPI for current monitor
     */
    Vector2 GetWindowScaleDPI();

    /**
     * Get number of monitors
     * @return Number of monitors
     */
    int GetMonitorCount();

    /**
     * Get monitor the window is presently on
     *
     * @return monitor the window is rendered on
     */
    int GetCurrentMonitor();

    /**
     * Get selected monitor width (currently used by monitor)
     * @param monitor Monitor to query
     * @return Width of monitor in pixels
     */
    int GetMonitorWidth(int monitor);

    /**
     * Get selected monitor height (currently used by monitor)
     * @param monitor Monitor to query
     * @return Height of monitor in pixels
     */
    int GetMonitorHeight(int monitor);

    /**
     * Get selected monitor physical width in millimetres
     * @param monitor Monitor to query
     * @return Physical width of the monitor in millimetres
     */
    int GetMonitorPhysicalWidth(int monitor);

    /**
     * Get selected monitor physical height in millimetres
     * @param monitor Monitor to query
     * @return Physical height of the monitor in millimetres
     */
    int GetMonitorPhysicalHeight(int monitor);

    /**
     * Get selected monitor refresh rate
     * @param monitor Monitor to query
     * @return Refresh rate in hertz
     */
    int GetMonitorRefreshRate(int monitor);

    /**
     * Get selected monitor position
     *
     * @param monitor desired monitor number
     * @return x,y pair that defines monitor's upper left corner in pixel-space
     */
    Vector2 GetMonitorPosition(int monitor);

    /**
     * Get the human-readable, UTF-8 encoded name of the selected monitor
     * @param monitor Monitor to query
     * @return Name of queried monitor
     */
    String GetMonitorName(int monitor);

    /**
     * Set clipboard text content
     * @param text Text to put in the system clipboard
     */
    void SetClipboardText(String text);

    /**
     * Get clipboard text content
     * @return Text from the system clipboard
     */
    String GetClipboardText();

    /**
     * Show mouse cursor
     */
    void ShowCursor();

    /**
     * Hides mouse cursor
     */
    void HideCursor();

    /**
     * Set mouse position XY
     * @param x Mouse X position
     * @param y Mouse Y position
     */
    void SetMousePosition(int x, int y);

    /**
     * Set mouse cursor
     * @param mouseCursor
     * @see Mouse.MouseCursor
     */
    void SetMouseCursor(Mouse.MouseCursor mouseCursor);

    /**
     * Enables cursor (unlock cursor)
     */
    void EnableCursor();

    /**
     * Disables cursor (lock cursor)
     */
    void DisableCursor();

    /**
     * Swap back buffer with front buffer (screen drawing)
     */
    void SwapScreenBuffer();

    /**
     * Poll (store) all input events
     */
    void PollInputEvents();

    /**
     * Open URL with default system browser (if available) <br/>
     * WARNING: This function is only safe to use if you control the URL given.
     * A user could craft a malicious string performing another action.
     * Only call this function yourself not with user input or make sure to check the string yourself.
     * Ref: https://github.com/raysan5/raylib/issues/686
     * @param url
     */
    void OpenURL(String url);

    /**
     * Set a flag
     * @param n flag collection
     * @param f Position to set
     */
    default void FLAG_SET(int n, int f) { n = ((n) |= (f)); }

    /**
     * Clear a flag
     * @param n flag collection
     * @param f Position to clear
     */
    default void FLAG_CLEAR(int n, int f) { n = ((n) &= ~(f)); }

    /**
     * Toggle a flag
     * @param n flag collection
     * @param f Position to toggle
     */
    default void FLAG_TOGGLE(int n, int f) { n =((n) ^= (f)); }

    /**
     * Check if a flag is set
     * @param n flag collection
     * @param f Position to check
     * @return {@code true} if flag is set
     */
    default boolean FLAG_IS_SET(int n, int f) { return (((n) & (f)) == (f)); }
}
