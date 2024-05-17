package com.raylib.java.core;

public class AutomationEvent{

    // Input events
    public enum AutomationEventType{
                EVENT_NONE,
                INPUT_KEY_UP,                   // param[0]: key
                INPUT_KEY_DOWN,                 // param[0]: key
                INPUT_KEY_PRESSED,              // param[0]: key
                INPUT_KEY_RELEASED,             // param[0]: key
                INPUT_MOUSE_BUTTON_UP,          // param[0]: button
                INPUT_MOUSE_BUTTON_DOWN,        // param[0]: button
                INPUT_MOUSE_POSITION,           // param[0]: x, param[1]: y
                INPUT_MOUSE_WHEEL_MOTION,       // param[0]: x delta, param[1] y delta
                INPUT_GAMEPAD_CONNECT,          // param[0]: gamepad
                INPUT_GAMEPAD_DISCONNECT,       // param[0]: gamepad
                INPUT_GAMEPAD_BUTTON_UP,        // param[0]: button
                INPUT_GAMEPAD_BUTTON_DOWN,      // param[0]: button
                INPUT_GAMEPAD_AXIS_MOTION,      // param[0]: axis, param[1]: delta
                INPUT_TOUCH_UP,                 // param[0]: id
                INPUT_TOUCH_DOWN,               // param[0]: id
                INPUT_TOUCH_POSITION,           // param[0]: x, param[1]: y
                INPUT_GESTURE,                  // param[0]: gesture
                // Window events
                WINDOW_CLOSE,                   // no params
                WINDOW_MAXIMIZE,                // no params
                WINDOW_MINIMIZE,                // no params
                WINDOW_RESIZE,                  // param[0]: width, param[1]: height
                // Custom events
                ACTION_TAKE_SCREENSHOT,
                ACTION_SETTARGETFPS;
    }

    public enum EventType {
        EVENT_INPUT_KEYBOARD,
        EVENT_INPUT_MOUSE,
        EVENT_INPUT_GAMEPAD,
        EVENT_INPUT_TOUCH,
        EVENT_INPUT_GESTURE,
        EVENT_WINDOW,
        EVENT_CUSTOM
    }

    int frame;                 // Event frame
    int type;                  // Event type (AutoEventType)
    int params[];              // Event parameters (if required)

    public AutomationEvent(){
        params = new int[3];
    }

}
