package com.raylib.java.core;

import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Point;
import com.raylib.java.structs.Size;

/**
 * System window management
 */
public class Window {

    /**
     * Window title text
     */
    protected String title;

    /**
     * Configuration flags (bit based), keeps window state
     */
    public int flags;

    /**
     * Check if window has been initialized successfully
     */
    protected boolean ready;

    /**
     * Check if window set for closing
     */
    protected boolean shouldClose;

    /**
     * Check if window has been resized last frame
     */
    protected boolean resizedLastFrame;

    /**
     * Using FBO (RenderTexture) for rendering instead of default framebuffer
     */
    protected boolean usingFbo;

    /**
     * Wait for events before ending frame
     */
    protected boolean eventWaiting;

    /**
     * Display width and height (monitor, device-screen, LCD, ...)
     */
    protected Size display;

    /**
     * Screen width and height (used render area)
     */
    protected Size screen;

    /**
     * Window position on screen (required on fullscreen toggle)
     */
    protected Point position;

    /**
     * Screen previous width and height (required on borderless windowed toggle)
     */
    protected Size previousScreen;

    /**
     * Window previous position (required on borderless windowed toggle)
     */
    protected Point previousPosition;

    /**
     * Current render width and height, it could change on BeginTextureMode()
     */
    protected Size currentFbo;

    /**
     * Framebuffer width and height (render area, including black bars if required)
     */
    protected Size render;

    /**
     * Offset from render area (must be divided by 2)
     */
    protected Point renderOffset;

    /**
     * Screen minimum width and height (for resizable window)
     */
    protected Size screenMin;

    /**
     * Screen maximum width and height (for resizable window)
     */
    protected Size screenMax;

    /**
     * Matrix to scale screen (framebuffer rendering)
     */
    protected Matrix screenScale;

    /**
     * Store dropped files paths as strings
     */
    protected String[] dropFilePaths;

    /**
     * Count dropped files strings
     */
    protected int dropFilesCount;

    public Window() {
        title = "";
        flags = 0;
        ready = false;
        shouldClose = false;
        resizedLastFrame = false;

        display = new Size();
        screen = new Size();
        position = new Point(0, 0);
        previousScreen = new Size();
        previousPosition = new Point(0, 0);
        currentFbo = new Size();
        render = new Size();
        renderOffset = new Point();
        screenMin = new Size();
        screenMax = new Size();
        screenScale = new Matrix();

        dropFilePaths = new String[512];
        dropFilesCount = 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public boolean isResizedLastFrame() {
        return resizedLastFrame;
    }

    public void setResizedLastFrame(boolean resizedLastFrame) {
        this.resizedLastFrame = resizedLastFrame;
    }

    public boolean isUsingFbo() {
        return usingFbo;
    }

    public void setUsingFbo(boolean usingFbo) {
        this.usingFbo = usingFbo;
    }

    public boolean isEventWaiting() {
        return eventWaiting;
    }

    public void setEventWaiting(boolean eventWaiting) {
        this.eventWaiting = eventWaiting;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Point previousPosition) {
        this.previousPosition = previousPosition;
    }

    public Size getDisplay() {
        return display;
    }

    public void setDisplay(Size display) {
        this.display = display;
    }

    public Size getScreen() {
        return screen;
    }

    public void setScreen(Size screen) {
        this.screen = screen;
    }

    public Size getPreviousScreen() {
        return previousScreen;
    }

    public void setPreviousScreen(Size previousScreen) {
        this.previousScreen = previousScreen;
    }

    public Size getCurrentFbo() {
        return currentFbo;
    }

    public void setCurrentFbo(Size currentFbo) {
        this.currentFbo = currentFbo;
    }

    public Size getRender() {
        return render;
    }

    public void setRender(Size render) {
        this.render = render;
    }

    public Point getRenderOffset() {
        return renderOffset;
    }

    public void setRenderOffset(Point renderOffset) {
        this.renderOffset = renderOffset;
    }

    public Size getScreenMin() {
        return screenMin;
    }

    public void setScreenMin(Size screenMin) {
        this.screenMin = screenMin;
    }

    public Size getScreenMax() {
        return screenMax;
    }

    public void setScreenMax(Size screenMax) {
        this.screenMax = screenMax;
    }

    public Matrix getScreenScale() {
        return screenScale;
    }

    public void setScreenScale(Matrix screenScale) {
        this.screenScale = screenScale;
    }

    public String[] getDropFilePaths() {
        return dropFilePaths;
    }

    public void setDropFilePaths(String[] dropFilePaths) {
        this.dropFilePaths = dropFilePaths;
    }

    public int getDropFilesCount() {
        return dropFilesCount;
    }

    public void setDropFilesCount(int dropFilesCount) {
        this.dropFilesCount = dropFilesCount;
    }
}
