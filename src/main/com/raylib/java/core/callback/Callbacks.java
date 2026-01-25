package com.raylib.java.core.callback;

import com.raylib.java.core.rCore;

public class Callbacks{

    private final rCore context;

    public final ErrorCallback errorCallback;
    public final WindowMaximizeCallback windowMaximizeCallback;
    public final WindowSizeCallback windowSizeCallback;
    public final WindowIconifyCallback windowIconifyCallback;
    public final WindowFocusCallback windowFocusCallback;
    public final WindowDropCallback windowDropCallback;
    public final KeyCallback keyCallback;
    public final CharCallback charCallback;
    public final MouseButtonCallback mouseButtonCallback;
    public final MouseCursorPosCallback mouseCursorPosCallback;
    public final MouseScrollCallback mouseScrollCallback;
    public final CursorEnterCallback cursorEnterCallback;


    public Callbacks(rCore context) {
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

}