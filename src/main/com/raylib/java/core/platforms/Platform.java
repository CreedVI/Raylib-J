package com.raylib.java.core.platforms;

import com.raylib.java.core.input.Mouse;
import com.raylib.java.structs.Image;
import com.raylib.java.structs.Vector2;

import java.util.List;

public interface Platform {

    //----------------------------------------------------------------------------------
    // Module Functions Declaration
    //----------------------------------------------------------------------------------
    int InitPlatform();
    void ClosePlatform();

    boolean WindowShouldClose();
    void ToggleFullscreen();
    void ToggleBorderlessWindowed();
    void MaximizeWindow();
    void MinimizeWindow();
    void RestoreWindow();

    void SetWindowState(int flags);
    void ClearWindowState(int flags);

    void SetWindowIcon(Image image);
    void SetWindowIcons(List<Image> images);
    void SetWindowTitle(String title);
    void SetWindowPosition(int x, int y);
    void SetWindowMonitor(int monitor);
    void SetWindowMinSize(int width, int height);
    void SetWindowMaxSize(int width, int height);
    void SetWindowSize(int width, int height);
    void SetWindowOpacity(float opacity);
    void SetWindowFocused();
    long GetWindowHandle();
    Vector2 GetWindowPosition();
    Vector2 GetWindowScaleDPI();

    int GetMonitorCount();
    int GetCurrentMonitor();
    int GetMonitorWidth(int monitor);
    int GetMonitorHeight(int monitor);
    int GetMonitorPhysicalWidth(int monitor);
    int GetMonitorPhysicalHeight(int monitor);
    int GetMonitorRefreshRate(int monitor);
    Vector2 GetMonitorPosition(int monitor);
    String GetMonitorName(int monitor);

    void SetClipboardText(String text);
    String GetClipboardText();

    void ShowCursor();
    void HideCursor();
    void SetMousePosition(int x, int y);
    void SetMouseCursor(Mouse.MouseCursor mouseCursor);
    void EnableCursor();
    void DisableCursor();

    void SwapScreenBuffer();
    void PollInputEvents();

    void OpenURL(String url);
}
