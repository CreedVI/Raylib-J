package com.raylib.java.core.input;

import static com.raylib.java.Config.MAX_KEY_PRESSED_QUEUE;

public class Keyboard{

    public int exitKey;                    // Default exit key
    public boolean[] currentKeyState;      // Registers current frame key state
    public boolean[] previousKeyState;     // Registers previous frame key state

    public int[] keyPressedQueue;     // Input keys queue
    public int keyPressedQueueCount;       // Input keys queue count

    int[] charPressedQueue;   // Input characters queue
    int charPressedQueueCount;      // Input characters queue count

    public static int
        // Alphanumeric keys
        KEY_APOSTROPHE     = 39,
        KEY_COMMA          = 44,
        KEY_MINUS          = 45,
        KEY_PERIOD         = 46,
        KEY_SLASH          = 47,
        KEY_ZERO           = 48,
        KEY_ONE            = 49,
        KEY_TWO            = 50,
        KEY_THREE          = 51,
        KEY_FOUR           = 52,
        KEY_FIVE           = 53,
        KEY_SIX            = 54,
        KEY_SEVEN          = 55,
        KEY_EIGHT          = 56,
        KEY_NINE           = 57,
        KEY_SEMICOLON      = 59,
        KEY_EQUAL          = 61,
        KEY_A              = 65,
        KEY_B              = 66,
        KEY_C              = 67,
        KEY_D              = 68,
        KEY_E              = 69,
        KEY_F              = 70,
        KEY_G              = 71,
        KEY_H              = 72,
        KEY_I              = 73,
        KEY_J              = 74,
        KEY_K              = 75,
        KEY_L              = 76,
        KEY_M              = 77,
        KEY_N              = 78,
        KEY_O              = 79,
        KEY_P              = 80,
        KEY_Q              = 81,
        KEY_R              = 82,
        KEY_S              = 83,
        KEY_T              = 84,
        KEY_U              = 85,
        KEY_V              = 86,
        KEY_W              = 87,
        KEY_X              = 88,
        KEY_Y              = 89,
        KEY_Z              = 90,

        // Function keys
        KEY_SPACE          = 32,
        KEY_ESCAPE         = 256,
        KEY_ENTER          = 257,
        KEY_TAB            = 258,
        KEY_BACKSPACE      = 259,
        KEY_INSERT         = 260,
        KEY_DELETE         = 261,
        KEY_RIGHT          = 262,
        KEY_LEFT           = 263,
        KEY_DOWN           = 264,
        KEY_UP             = 265,
        KEY_PAGE_UP        = 266,
        KEY_PAGE_DOWN      = 267,
        KEY_HOME           = 268,
        KEY_END            = 269,
        KEY_CAPS_LOCK      = 280,
        KEY_SCROLL_LOCK    = 281,
        KEY_NUM_LOCK       = 282,
        KEY_PRINT_SCREEN   = 283,
        KEY_PAUSE          = 284,
        KEY_F1             = 290,
        KEY_F2             = 291,
        KEY_F3             = 292,
        KEY_F4             = 293,
        KEY_F5             = 294,
        KEY_F6             = 295,
        KEY_F7             = 296,
        KEY_F8             = 297,
        KEY_F9             = 298,
        KEY_F10            = 299,
        KEY_F11            = 300,
        KEY_F12            = 301,
        KEY_LEFT_SHIFT     = 340,
        KEY_LEFT_CONTROL   = 341,
        KEY_LEFT_ALT       = 342,
        KEY_LEFT_SUPER     = 343,
        KEY_RIGHT_SHIFT    = 344,
        KEY_RIGHT_CONTROL  = 345,
        KEY_RIGHT_ALT      = 346,
        KEY_RIGHT_SUPER    = 347,
        KEY_KB_MENU        = 348,
        KEY_LEFT_BRACKET   = 91,
        KEY_BACKSLASH      = 92,
        KEY_RIGHT_BRACKET  = 93,
        KEY_GRAVE          = 96,

        // Keypad keys
        KEY_KP_0           = 320,
        KEY_KP_1           = 321,
        KEY_KP_2           = 322,
        KEY_KP_3           = 323,
        KEY_KP_4           = 324,
        KEY_KP_5           = 325,
        KEY_KP_6           = 326,
        KEY_KP_7           = 327,
        KEY_KP_8           = 328,
        KEY_KP_9           = 329,
        KEY_KP_DECIMAL     = 330,
        KEY_KP_DIVIDE      = 331,
        KEY_KP_MULTIPLY    = 332,
        KEY_KP_SUBTRACT    = 333,
        KEY_KP_ADD         = 334,
        KEY_KP_ENTER       = 335,
        KEY_KP_EQUAL       = 336;

    public Keyboard() {
        currentKeyState = new boolean[512];
        previousKeyState = new boolean[512];
        keyPressedQueue = new int[MAX_KEY_PRESSED_QUEUE];
        charPressedQueue = new int[MAX_KEY_PRESSED_QUEUE];
    }

    public boolean[] getCurrentKeyState() {
        return currentKeyState;
    }

    public void setCurrentKeyState(boolean[] currentKeyState) {
        this.currentKeyState = currentKeyState;
    }

    public boolean[] getPreviousKeyState() {
        return previousKeyState;
    }

    public void setPreviousKeyState(boolean[] previousKeyState) {
        this.previousKeyState = previousKeyState;
    }

    public int[] getKeyPressedQueue() {
        return keyPressedQueue;
    }

    public void setKeyPressedQueue(int[] keyPressedQueue) {
        this.keyPressedQueue = keyPressedQueue;
    }

    public int getKeyPressedQueueCount() {
        return keyPressedQueueCount;
    }

    public void setKeyPressedQueueCount(int keyPressedQueueCount) {
        this.keyPressedQueueCount = keyPressedQueueCount;
    }

    public int[] getCharPressedQueue() {
        return charPressedQueue;
    }

    public void setCharPressedQueue(int[] charPressedQueue) {
        this.charPressedQueue = charPressedQueue;
    }

    public int getCharPressedQueueCount() {
        return charPressedQueueCount;
    }

    public void setCharPressedQueueCount(int charPressedQueueCount) {
        this.charPressedQueueCount = charPressedQueueCount;
    }

    public int getExitKey() {
        return exitKey;
    }

    public void setExitKey(int exitKey) {
        this.exitKey = exitKey;
    }

}
