package com.raylib.java.core.callback;

import org.lwjgl.glfw.GLFWErrorCallback;

public class ErrorCallback extends GLFWErrorCallback {
    @Override
    public void invoke(int error, long description){
        System.out.println("GLFW: Error: " + error + " Description: " + description);
    }
}
