package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Vector2;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import java.nio.IntBuffer;

import static com.raylib.java.Config.*;
import static com.raylib.java.Config.ConfigFlag.FLAG_FULLSCREEN_MODE;
import static com.raylib.java.Config.ConfigFlag.FLAG_WINDOW_HIGHDPI;
import static com.raylib.java.raymath.Raymath.MatrixScale;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class FrameBufferSizeCallback extends GLFWFramebufferSizeCallback {

    private final Raylib context;

    public FrameBufferSizeCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(long window, int width, int height) {
        //TRACELOG(LOG_INFO, "GLFW3: Window framebuffer size callback called [%i,%i]", width, height);

        // WARNING: On window minimization, callback is called with 0 values,
        // but internal screen values should not be changed, it breaks things
        if ((width == 0) || (height == 0)) {
            return;
        }

        // Reset viewport and projection matrix for new size
        // NOTE: Stores current render size: CORE.Window.render
        context.core.SetupViewport(width, height);

        // Set render size
        context.core.window.getRender().width = width;
        context.core.window.getRender().height = height;
        context.core.window.setCurrentFbo(context.core.window.getRender());
        context.core.window.setResizedLastFrame(true);

        if (FLAG_IS_SET(context.core.window.flags, FLAG_FULLSCREEN_MODE))
        {
            // On fullscreen mode, strategy is ignoring high-dpi and
            // use the all available display size

            // Set screen size to render size (physical pixel size)
            context.core.window.getScreen().width = width;
            context.core.window.getScreen().height = height;
            context.core.window.setScreenScale(MatrixScale(1.0f, 1.0f, 1.0f));
            context.core.SetMouseScale(1.0f, 1.0f);

            // On Wayland with GLFW_SCALE_FRAMEBUFFER, the framebuffer is still scaled in fullscreen, use logical window size as screen and apply screenScale
            if (_GLFW_WAYLAND && !_GLFW_X11) {
                if (FLAG_IS_SET(context.core.window.flags, FLAG_WINDOW_HIGHDPI)) {
                    IntBuffer winWidth = IntBuffer.allocate(1);
                    IntBuffer winHeight = IntBuffer.allocate(1);
                    glfwGetWindowSize(context.core.GetPlatform().GetWindowHandle(), winWidth, winHeight);

                    if ((winWidth.get(0) != width) || (winHeight.get(0) != height)) {
                        context.core.window.getScreen().width = winWidth.get(0);
                        context.core.window.getScreen().height = winHeight.get(0);
                        float scaleX = (float) width / winWidth.get(0);
                        float scaleY = (float) height / winHeight.get(0);

                        context.core.window.setScreenScale(MatrixScale(scaleX, scaleY, 1.0f));
                    }
                }
            }
        }
        else // Window mode (including borderless window)
        {
            // Check if render size was actually scaled for high-dpi
            if (FLAG_IS_SET(context.core.window.flags, FLAG_WINDOW_HIGHDPI))
            {
                // Set screen size to logical pixel size, considering content scaling
                Vector2 scaleDpi = context.core.GetWindowScaleDPI();
                context.core.window.getScreen().width = (int)((float)width/scaleDpi.x);
                context.core.window.getScreen().height = (int)((float)height/scaleDpi.y);
                context.core.window.setScreenScale(MatrixScale(scaleDpi.x, scaleDpi.y, 1.0f));
                if (!__APPLE__ && !_GLFW_WAYLAND) {
                    // On macOS and Linux-Wayland, mouse coords are already in logical space
                    context.core.SetMouseScale(1.0f / scaleDpi.x, 1.0f / scaleDpi.y);
                }
            }
            else
            {
                // Set screen size to render size (physical pixel size)
                context.core.window.getScreen().width = width;
                context.core.window.getScreen().height = height;
            }
        }

        // WARNING: If using a render texture, it is not scaled to new size
    }

    private void FLAG_SET(int n, int f) { n = ((n) |= (f)); }
    private void FLAG_CLEAR(int n, int f) { n = ((n) &= ~(f)); }
    private void FLAG_TOGGLE(int n, int f) { n =((n) ^= (f)); }
    private boolean FLAG_IS_SET(int n, int f) { return (((n) & (f)) == (f)); }
}
