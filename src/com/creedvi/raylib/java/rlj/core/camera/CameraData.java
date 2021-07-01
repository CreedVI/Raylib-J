package com.creedvi.raylib.java.rlj.core.camera;

import com.creedvi.raylib.java.rlj.core.input.Keyboard;
import com.creedvi.raylib.java.rlj.raymath.Vector2;

import static com.creedvi.raylib.java.rlj.core.input.Keyboard.KeyboardKey.*;

public class CameraData{

    public enum CameraMove {
        MOVE_FRONT(KEY_W.getKeyInt()),
        MOVE_BACK(KEY_S.getKeyInt()),
        MOVE_LEFT(KEY_A.getKeyInt()),
        MOVE_RIGHT(KEY_D.getKeyInt()),
        MOVE_UP(KEY_Q.getKeyInt()),
        MOVE_DOWN(KEY_E.getKeyInt())
        ;

        int key;
        CameraMove(int key){
            this.key = key;
        }

        public int getKey(){
            return key;
        }
        public void setKey(Keyboard.KeyboardKey key){
            this.key = key.getKeyInt();
        }
        public void setKey(int key){
            this.key = key;
        }
    }

    int mode;              // Current camera mode
    float targetDistance;           // Camera distance from position to target
    float playerEyesPosition;       // Player eyes position from ground (in meters)
    Vector2 angle;                  // Camera angle in plane XZ

    // Camera movement control keys
    CameraMove[] moveControl;             // Move controls (CAMERA_FIRST_PERSON)
    int smoothZoomControl;          // Smooth zoom control key
    int altControl;                 // Alternative control key
    int panControl;                 // Pan view control key

    public CameraData(){
        mode = 0;
        targetDistance = 0;
        playerEyesPosition = 1.85f;
        angle = new Vector2();
        moveControl = new CameraMove[]{
            CameraMove.MOVE_FRONT, CameraMove.MOVE_BACK, CameraMove.MOVE_LEFT, CameraMove.MOVE_RIGHT, CameraMove.MOVE_UP,
            CameraMove.MOVE_DOWN
        };
        smoothZoomControl = 341;       // raylib: KEY_LEFT_CONTROL
        altControl = 342;              // raylib: KEY_LEFT_ALT
        panControl = 2;                 // raylib: MOUSE_MIDDLE_BUTTON
    }

}
