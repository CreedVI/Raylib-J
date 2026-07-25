package com.raylib.java.core.callback;

import com.raylib.java.Raylib;
import org.lwjgl.glfw.GLFWJoystickCallback;

import static org.lwjgl.glfw.GLFW.*;

public class JoystickCallback extends GLFWJoystickCallback {

    private final Raylib context;
    public JoystickCallback(Raylib context) {
        this.context = context;
    }

    @Override
    public void invoke(int jid, int event) {
        if (event == GLFW_CONNECTED) {
            context.core.input.getGamepad().name[jid] =  glfwGetJoystickName(jid);
        }
        else if (event == GLFW_DISCONNECTED) {
            context.core.input.getGamepad().name[jid] = null;
        }
    }
}
