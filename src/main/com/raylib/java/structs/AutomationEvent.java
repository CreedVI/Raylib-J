package com.raylib.java.structs;

/**
 * Automation events
 */
public class AutomationEvent implements Cloneable {

    /**
     * Input events
     */
    public enum AutomationEventType {
        /**
         * No event registered
         */
        EVENT_NONE,
        /**
         * param[0]: key
         */
        INPUT_KEY_UP,
        /**
         * param[0]: key
         */
        INPUT_KEY_DOWN,
        /**
         * param[0]: key
         */
        INPUT_KEY_PRESSED,
        /**
         * param[0]: key
         */
        INPUT_KEY_RELEASED,
        /**
         * param[0]: button
         */
        INPUT_MOUSE_BUTTON_UP,
        /**
         * param[0]: button
         */
        INPUT_MOUSE_BUTTON_DOWN,
        /**
         * param[0]: x, param[1]: y
         */
        INPUT_MOUSE_POSITION,
        /**
         * param[0]: x delta, param[1] y delta
         */
        INPUT_MOUSE_WHEEL_MOTION,
        /**
         * param[0]: gamepad
         */
        INPUT_GAMEPAD_CONNECT,
        /**
         * param[0]: gamepad
         */
        INPUT_GAMEPAD_DISCONNECT,
        /**
         * param[0]: button
         */
        INPUT_GAMEPAD_BUTTON_UP,
        /**
         * param[0]: button
         */
        INPUT_GAMEPAD_BUTTON_DOWN,
        /**
         * param[0]: axis, param[1]: delta
         */
        INPUT_GAMEPAD_AXIS_MOTION,
        /**
         * param[0]: id
         */
        INPUT_TOUCH_UP,
        /**
         * param[0]: id
         */
        INPUT_TOUCH_DOWN,
        /**
         * param[0]: x, param[1]: y
         */
        INPUT_TOUCH_POSITION,
        /**
         * param[0]: gesture
         */
        INPUT_GESTURE,
        // Window events
        /**
         * no params
         */
        WINDOW_CLOSE,
        /**
         * no params
         */
        WINDOW_MAXIMIZE,
        /**
         * no params
         */
        WINDOW_MINIMIZE,
        /**
         * param[0]: width, param[1]: height
         */
        WINDOW_RESIZE,
        // Custom events

        /**
         * no params
         */
        ACTION_TAKE_SCREENSHOT,

        /**
         * no params
         */
        ACTION_SETTARGETFPS;
    }

    /**
     * Automation event types
     */
    public enum EventType {
        /**
         * Keyboard input event
         */
        EVENT_INPUT_KEYBOARD,
        /**
         * Mouse input event
         */
        EVENT_INPUT_MOUSE,
        /**
         * Gamepad input event
         */
        EVENT_INPUT_GAMEPAD,
        /**
         * Touch input event
         */
        EVENT_INPUT_TOUCH,
        /**
         * Gesture input event
         */
        EVENT_INPUT_GESTURE,
        /**
         * Window input event
         */
        EVENT_WINDOW,
        /**
         * Custom input event
         */
        EVENT_CUSTOM
    }

    /**
     *  Event frame
     */
    public int frame;
    /**
     *  Event type (AutoEventType)
     */
    public int type;
    /**
     * Event parameters (if required)
     */
    public int[] params;

    public AutomationEvent(){
        params = new int[3];
    }

    @Override
    public AutomationEvent clone() {
        try {
            AutomationEvent clone = (AutomationEvent) super.clone();
            clone.params = new int[params.length];
            System.arraycopy(params, 0, clone.params, 0, params.length);
            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


}
