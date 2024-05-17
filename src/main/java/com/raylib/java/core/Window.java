package com.raylib.java.core;

import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Point;
import com.raylib.java.raymath.Size;

import java.util.ArrayList;

public class Window{
    long handle;

    //EGLDisplay device;                  // Native display device (physical screen connection)
    //EGLSurface surface;                 // Surface to draw on, framebuffers (connected to context)
    //EGLContext context;                 // Graphic context, mode in which drawing can be done
    //EGLConfig config;                   // Graphic config

    String title;                  // window text title const pointer
    int flags;                 // Configuration flags (bit based), keeps window state
    boolean ready;                         // Check if window has been initialized successfully
    boolean fullscreen;                    // Check if fullscreen mode is enabled
    boolean shouldClose;                   // Check if window set for closing
    boolean resizedLastFrame;              // Check if window has been resized last frame

    boolean eventWaiting;               // Wait for events before ending frame

    Point position;                     // window position on screen (required on fullscreen toggle)
    Size display;                       // Display width and height (monitor, device-screen, LCD, ...)
    Size screen;                        // Screen width and height (used render area)
    Size currentFbo;                    // Current render width and height, it could change on BeginTextureMode()
    Size render;                        // Framebuffer width and height (render area, including black bars if required)
    Point renderOffset;                 // Offset from render area (must be divided by 2)
    Matrix screenScale;                 // Matrix to scale screen (framebuffer rendering)

    String[] dropFilePaths;               // Store dropped files paths as strings
    int dropFilesCount;                 // Count dropped files strings

    public Window(){
        handle = 0;

        title = "";
        flags = 0;
        ready = false;
        fullscreen = false;
        shouldClose = false;
        resizedLastFrame = false;

        position = new Point(0,0);
        display = new Size();
        screen = new Size();
        currentFbo = new Size();
        render = new Size();
        renderOffset = new Point();
        screenScale = new Matrix();

        dropFilePaths = new String[512];
        dropFilesCount = 0;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getFlags(){
        return flags;
    }

    public void setFlags(int flags){
        this.flags = flags;
    }

    public boolean isReady(){
        return ready;
    }

    public void setReady(boolean ready){
        this.ready = ready;
    }

    public boolean isFullscreen(){
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen){
        this.fullscreen = fullscreen;
    }

    public boolean isShouldClose(){
        return shouldClose;
    }

    public void setShouldClose(boolean shouldClose){
        this.shouldClose = shouldClose;
    }

    public boolean isResizedLastFrame(){
        return resizedLastFrame;
    }

    public void setResizedLastFrame(boolean resizedLastFrame){
        this.resizedLastFrame = resizedLastFrame;
    }

    public Point getPosition(){
        return position;
    }

    public void setPosition(Point position){
        this.position = position;
    }

    public Size getDisplay(){
        return display;
    }

    public void setDisplay(Size display){
        this.display = display;
    }

    public Size getScreen(){
        return screen;
    }

    public void setScreen(Size screen){
        this.screen = screen;
    }

    public Size getCurrentFbo(){
        return currentFbo;
    }

    public void setCurrentFbo(Size currentFbo){
        this.currentFbo = currentFbo;
    }

    public Size getRender(){
        return render;
    }

    public void setRender(Size render){
        this.render = render;
    }

    public Point getRenderOffset(){
        return renderOffset;
    }

    public void setRenderOffset(Point renderOffset){
        this.renderOffset = renderOffset;
    }

    public Matrix getScreenScale(){
        return screenScale;
    }

    public void setScreenScale(Matrix screenScale){
        this.screenScale = screenScale;
    }

    public String[] getDropFilePaths(){
        return dropFilePaths;
    }

    public void setDropFilePaths(String[] dropFilesPath){
        this.dropFilePaths = dropFilesPath;
    }

    public int getDropFilesCount(){
        return dropFilesCount;
    }

    public void setDropFilesCount(int dropFilesCount){
        this.dropFilesCount = dropFilesCount;
    }
}
