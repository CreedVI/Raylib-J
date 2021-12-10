package com.raylib.java.core;

public class AutomationEvent{

    public static class AutomationEventType{
        // Input events
        public static final int
                EVENT_NONE = 0,
                INPUT_KEY_UP = 1,                   // param[0]: key
                INPUT_KEY_DOWN = 2,                 // param[0]: key
                INPUT_KEY_PRESSED = 3,              // param[0]: key
                INPUT_KEY_RELEASED = 4,             // param[0]: key
                INPUT_MOUSE_BUTTON_UP = 5,          // param[0]: button
                INPUT_MOUSE_BUTTON_DOWN = 6,        // param[0]: button
                INPUT_MOUSE_POSITION = 7,           // param[0]: x, param[1]: y
                INPUT_MOUSE_WHEEL_MOTION = 8,       // param[0]: delta
                INPUT_GAMEPAD_CONNECT = 9,          // param[0]: gamepad
                INPUT_GAMEPAD_DISCONNECT = 10,       // param[0]: gamepad
                INPUT_GAMEPAD_BUTTON_UP = 11,        // param[0]: button
                INPUT_GAMEPAD_BUTTON_DOWN = 12,      // param[0]: button
                INPUT_GAMEPAD_AXIS_MOTION = 13,      // param[0]: axis, param[1]: delta
                INPUT_TOUCH_UP = 14,                 // param[0]: id
                INPUT_TOUCH_DOWN = 15,               // param[0]: id
                INPUT_TOUCH_POSITION = 16,           // param[0]: x, param[1]: y
                INPUT_GESTURE = 17,                  // param[0]: gesture
                // Window events
                WINDOW_CLOSE = 18,                   // no params
                WINDOW_MAXIMIZE = 19,                // no params
                WINDOW_MINIMIZE = 20,                // no params
                WINDOW_RESIZE = 21,                  // param[0]: width, param[1]: height
                // Custom events
                ACTION_TAKE_SCREENSHOT = 22,
                ACTION_SETTARGETFPS = 23;
    }

    int frame;                 // Event frame
    int type;                  // Event type (AutoEventType)
    int params[];              // Event parameters (if required)

    public AutomationEvent(){
        params = new int[3];
    }

}
