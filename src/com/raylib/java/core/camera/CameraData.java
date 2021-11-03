package com.raylib.java.core.camera;

import com.raylib.java.raymath.Vector2;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_MIDDLE_BUTTON;

public class CameraData{

    public int
            MoveFront = KEY_W,
            MoveBack = KEY_A,
            MoveLeft = KEY_A,
            MoveRight = KEY_D,
            MoveUp = KEY_Q,
            MoveDown = KEY_E,
            smoothZoomControl = KEY_LEFT_CONTROL,
            altControl = KEY_LEFT_ALT,
            panControl = MOUSE_MIDDLE_BUTTON;

    Camera.CameraMode mode;              // Current camera mode
    float targetDistance;           // Camera distance from position to target
    float playerEyesPosition;       // Player eyes position from ground (in meters)
    Vector2 angle;                  // Camera angle in plane XZ

    // Camera movement control keys
    int[] moveControl;             // Move controls (CAMERA_FIRST_PERSON)

    public CameraData(){
        mode = Camera.CameraMode.CAMERA_CUSTOM;
        targetDistance = 0;
        playerEyesPosition = 1.85f;
        angle = new Vector2();
        moveControl = new int[]{
            MoveFront, MoveBack, MoveLeft, MoveRight, MoveUp, MoveDown
        };
    }

}
