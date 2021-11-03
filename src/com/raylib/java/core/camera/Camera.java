package com.raylib.java.core.camera;

import com.raylib.java.core.Core;
import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;

import static com.raylib.java.core.camera.Camera.CameraMode.*;
import static com.raylib.java.raymath.Raymath.DEG2RAD;

public abstract class Camera{

    // Camera mouse movement sensitivity
    static float CAMERA_MOUSE_MOVE_SENSITIVITY = 0.003f;
    static float CAMERA_MOUSE_SCROLL_SENSITIVITY = 1.5f;

    // FREE_CAMERA
    static float CAMERA_FREE_MOUSE_SENSITIVITY = 0.01f;
    static float CAMERA_FREE_DISTANCE_MIN_CLAMP = 0.3f;
    static float CAMERA_FREE_DISTANCE_MAX_CLAMP = 120.0f;
    static float CAMERA_FREE_MIN_CLAMP = 85.0f;
    static float CAMERA_FREE_MAX_CLAMP = -85.0f;
    static float CAMERA_FREE_SMOOTH_ZOOM_SENSITIVITY = 0.05f;
    static float CAMERA_FREE_PANNING_DIVIDER = 5.1f;

    // ORBITAL_CAMERA
    static float CAMERA_ORBITAL_SPEED = 0.01f; // Radians per frame

    // FIRST_PERSON
    //float CAMERA_FIRST_PERSON_MOUSE_SENSITIVITY           0.003f
    float CAMERA_FIRST_PERSON_FOCUS_DISTANCE = 25.0f;
    static float CAMERA_FIRST_PERSON_MIN_CLAMP = 89.0f;
    static float CAMERA_FIRST_PERSON_MAX_CLAMP = -89.0f;

    static float CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER = 8.0f;
    static float CAMERA_FIRST_PERSON_STEP_DIVIDER = 30.0f;
    static float CAMERA_FIRST_PERSON_WAVING_DIVIDER = 200.0f;

    // THIRD_PERSON
    //float CAMERA_THIRD_PERSON_MOUSE_SENSITIVITY           0.003f
    static float CAMERA_THIRD_PERSON_DISTANCE_CLAMP = 1.2f;
    static float CAMERA_THIRD_PERSON_MIN_CLAMP = 5.0f;
    static float CAMERA_THIRD_PERSON_MAX_CLAMP = -85.0f;
    Vector3 CAMERA_THIRD_PERSON_OFFSET = new Vector3(0.4f, 0.0f, 0.0f);

    // PLAYER (used by camera)
    static float PLAYER_MOVEMENT_SENSITIVITY = 20.0f;

    static CameraData CAMERA = new CameraData();

    public enum CameraMode{
        CAMERA_CUSTOM(0),
        CAMERA_FREE(1),
        CAMERA_ORBITAL(2),
        CAMERA_FIRST_PERSON(3),
        CAMERA_THIRD_PERSON(4);

        final int camMode;

        CameraMode(int i){
            camMode = i;
        }

        public int getCamMode(){
            return camMode;
        }

        static CameraMode getByInt(int i){
            for (CameraMode m: values()){
                if (m.camMode == i){
                    return m;
                }
            }
            return CAMERA_CUSTOM;
        }
    }

    // Camera projection modes
    public static class CameraProjection{
        public static int
                CAMERA_PERSPECTIVE = 0,
                CAMERA_ORTHOGRAPHIC = 1;
    }

    abstract public void update();

    public static void SetCameraMode(Camera3D camera, CameraMode mode){
        Vector3 v1 = camera.getPosition();
        Vector3 v2 = new Vector3(camera.getTarget().x, camera.getTarget().y, 0);

        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;

        CAMERA.targetDistance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);   // Distance to target

        // Camera angle calculation
        CAMERA.angle.x = (float) Math.atan2(dx, dz); // Camera angle in plane XZ (0 aligned with Z, move positive CCW)
        CAMERA.angle.y = (float) Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)); // Camera angle in plane XY (0 aligned with X,
        // move positive CW)

        CAMERA.playerEyesPosition = camera.position.y;          // Init player eyes position to camera Y position

        // Lock cursor for first person and third person cameras
        if ((mode == CAMERA_FIRST_PERSON) || (mode == CAMERA_THIRD_PERSON)){
            Core.DisableCursor();
        }
        else{
            Core.EnableCursor();
        }

        CAMERA.mode = mode;
    }

    // Update camera depending on selected mode
    // NOTE: Camera controls depend on some raylib functions:
    //       System: EnableCursor(), DisableCursor()
    //       Mouse: IsMouseButtonDown(), GetMousePosition(), GetMouseWheelMove()
    //       Keys:  IsKeyDown()
    // TODO: Port to quaternion-based camera (?)
    public static void UpdateCamera(Camera3D camera){
        int swingCounter = 0;    // Used for 1st person swinging movement
        Vector2 previousMousePosition = new Vector2(0.0f, 0.0f);

        // TODO: Compute CAMERA.targetDistance and CAMERA.angle here (?)

        // Mouse movement detection
        Vector2 mousePositionDelta = new Vector2(0.0f, 0.0f);
        Vector2 mousePosition = Core.GetMousePosition();
        float mouseWheelMove = Core.GetMouseWheelMove();

        // Keys input detection
        // TODO: Input detection is raylib-dependant, it could be moved outside the module
        boolean keyPan = Core.IsMouseButtonDown(CAMERA.panControl);
        boolean keyAlt = Core.IsKeyDown(CAMERA.altControl);
        boolean sZoomKey = Core.IsKeyDown(CAMERA.smoothZoomControl);
        int[] direction = new int[]{
                Core.IsKeyDown(CAMERA.MoveFront) ? 1 : 0,
                Core.IsKeyDown(CAMERA.MoveBack) ? 1 : 0,
                Core.IsKeyDown(CAMERA.MoveRight) ? 1 : 0,
                Core.IsKeyDown(CAMERA.MoveLeft) ? 1 : 0,
                Core.IsKeyDown(CAMERA.MoveUp) ? 1 : 0,
                Core.IsKeyDown(CAMERA.MoveDown) ? 1 : 0
        };

        if (CAMERA.mode != CAMERA_CUSTOM){
            mousePositionDelta.x = mousePosition.x - previousMousePosition.x;
            mousePositionDelta.y = mousePosition.y - previousMousePosition.y;
        }


        // Support for multiple automatic camera modes
        // NOTE: In case of CAMERA_CUSTOM nothing happens here, user must update it manually
        switch (CAMERA.mode){
            case CAMERA_FREE:           // Camera free controls, using standard 3d-content-creation scheme
            {
                // Camera zoom
                if ((CAMERA.targetDistance < CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0)){
                    CAMERA.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (CAMERA.targetDistance > CAMERA_FREE_DISTANCE_MAX_CLAMP){
                        CAMERA.targetDistance = CAMERA_FREE_DISTANCE_MAX_CLAMP;
                    }
                }

                // Camera looking down
                else if ((camera.position.y > camera.target.y) && (CAMERA.targetDistance == CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0)){
                    camera.target.x += mouseWheelMove * (camera.target.x - camera.position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove * (camera.target.y - camera.position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove * (camera.target.z - camera.position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                }
                else if ((camera.position.y > camera.target.y) && (camera.target.y >= 0)){
                    camera.target.x += mouseWheelMove * (camera.target.x - camera.position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove * (camera.target.y - camera.position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove * (camera.target.z - camera.position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;

                    // if (camera.target.y < 0) camera.target.y = -0.001;
                }
                else if ((camera.position.y > camera.target.y) && (camera.target.y < 0) && (mouseWheelMove > 0)){
                    CAMERA.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (CAMERA.targetDistance < CAMERA_FREE_DISTANCE_MIN_CLAMP){
                        CAMERA.targetDistance = CAMERA_FREE_DISTANCE_MIN_CLAMP;
                    }
                }
                // Camera looking up
                else if ((camera.position.y < camera.target.y) && (CAMERA.targetDistance == CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0)){
                    camera.target.x += mouseWheelMove * (camera.target.x - camera.position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove * (camera.target.y - camera.position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove * (camera.target.z - camera.position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                }
                else if ((camera.position.y < camera.target.y) && (camera.target.y <= 0)){
                    camera.target.x += mouseWheelMove * (camera.target.x - camera.position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove * (camera.target.y - camera.position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove * (camera.target.z - camera.position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / CAMERA.targetDistance;

                    // if (camera.target.y > 0) camera.target.y = 0.001;
                }
                else if ((camera.position.y < camera.target.y) && (camera.target.y > 0) && (mouseWheelMove > 0)){
                    CAMERA.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (CAMERA.targetDistance < CAMERA_FREE_DISTANCE_MIN_CLAMP){
                        CAMERA.targetDistance = CAMERA_FREE_DISTANCE_MIN_CLAMP;
                    }
                }

                // Input keys checks
                if (keyPan){
                    if (keyAlt)     // Alternative key behaviour
                    {
                        if (sZoomKey){
                            // Camera smooth zoom
                            CAMERA.targetDistance += (mousePositionDelta.y * CAMERA_FREE_SMOOTH_ZOOM_SENSITIVITY);
                        }
                        else{
                            // Camera rotation
                            CAMERA.angle.x += mousePositionDelta.x * -CAMERA_FREE_MOUSE_SENSITIVITY;
                            CAMERA.angle.y += mousePositionDelta.y * -CAMERA_FREE_MOUSE_SENSITIVITY;

                            // Angle clamp
                            if (CAMERA.angle.y > CAMERA_FREE_MIN_CLAMP * Raymath.DEG2RAD){
                                CAMERA.angle.y = CAMERA_FREE_MIN_CLAMP * Raymath.DEG2RAD;
                            }
                            else if (CAMERA.angle.y < CAMERA_FREE_MAX_CLAMP * Raymath.DEG2RAD){
                                CAMERA.angle.y = CAMERA_FREE_MAX_CLAMP * Raymath.DEG2RAD;
                            }
                        }
                    }
                    else{
                        // Camera panning
                        camera.target.x += ((mousePositionDelta.x * CAMERA_FREE_MOUSE_SENSITIVITY) * Math.cos(CAMERA.angle.x) + (mousePositionDelta.y * CAMERA_FREE_MOUSE_SENSITIVITY) * Math.sin(CAMERA.angle.x) * Math.sin(CAMERA.angle.y)) * (CAMERA.targetDistance / CAMERA_FREE_PANNING_DIVIDER);
                        camera.target.y += ((mousePositionDelta.y * CAMERA_FREE_MOUSE_SENSITIVITY) * Math.cos(CAMERA.angle.y)) * (CAMERA.targetDistance / CAMERA_FREE_PANNING_DIVIDER);
                        camera.target.z += ((mousePositionDelta.x * -CAMERA_FREE_MOUSE_SENSITIVITY) * Math.sin(CAMERA.angle.x) + (mousePositionDelta.y * CAMERA_FREE_MOUSE_SENSITIVITY) * Math.cos(CAMERA.angle.x) * Math.sin(CAMERA.angle.y)) * (CAMERA.targetDistance / CAMERA_FREE_PANNING_DIVIDER);
                    }
                }

                // Update camera position with changes
                camera.position.x = (float) (-Math.sin(CAMERA.angle.x) * CAMERA.targetDistance * Math.cos(CAMERA.angle.y) + camera.target.x);
                camera.position.y = (float) (-Math.sin(CAMERA.angle.y) * CAMERA.targetDistance + camera.target.y);
                camera.position.z = (float) (-Math.cos(CAMERA.angle.x) * CAMERA.targetDistance * Math.cos(CAMERA.angle.y) + camera.target.z);

            }
            break;
            case CAMERA_ORBITAL:        // Camera just orbits around target, only zoom allowed
            {
                CAMERA.angle.x += CAMERA_ORBITAL_SPEED;      // Camera orbit angle
                CAMERA.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);   // Camera zoom

                // Camera distance clamp
                if (CAMERA.targetDistance < CAMERA_THIRD_PERSON_DISTANCE_CLAMP){
                    CAMERA.targetDistance = CAMERA_THIRD_PERSON_DISTANCE_CLAMP;
                }

                // Update camera position with changes
                camera.position.x = (float) (Math.sin(CAMERA.angle.x) * CAMERA.targetDistance * Math.cos(CAMERA.angle.y) + camera.target.x);
                camera.position.y = (float) (((CAMERA.angle.y <= 0.0f) ? 1 : -1) * Math.sin(CAMERA.angle.y) * CAMERA.targetDistance * Math.sin(CAMERA.angle.y) + camera.target.y);
                camera.position.z = (float) (Math.cos(CAMERA.angle.x) * CAMERA.targetDistance * Math.cos(CAMERA.angle.y) + camera.target.z);

            }
            break;
            case CAMERA_FIRST_PERSON:   // Camera moves as in a first-person game, controls are configurable
            {
                camera.position.x += (Math.sin(CAMERA.angle.x) * direction[1] -
                        Math.sin(CAMERA.angle.x) * direction[0] -
                        Math.cos(CAMERA.angle.x) * direction[3] +
                        Math.cos(CAMERA.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.y += (Math.sin(CAMERA.angle.y) * direction[0] -
                        Math.sin(CAMERA.angle.y) * direction[1] +
                        1.0f * direction[4] - 1.0f * direction[5]) / PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.z += (Math.cos(CAMERA.angle.x) * direction[1] -
                        Math.cos(CAMERA.angle.x) * direction[0] +
                        Math.sin(CAMERA.angle.x) * direction[3] -
                        Math.sin(CAMERA.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                // Camera orientation calculation
                CAMERA.angle.x += (mousePositionDelta.x * -CAMERA_MOUSE_MOVE_SENSITIVITY);
                CAMERA.angle.y += (mousePositionDelta.y * -CAMERA_MOUSE_MOVE_SENSITIVITY);

                // Angle clamp
                if (CAMERA.angle.y > CAMERA_FIRST_PERSON_MIN_CLAMP * Raymath.DEG2RAD){
                    CAMERA.angle.y = CAMERA_FIRST_PERSON_MIN_CLAMP * Raymath.DEG2RAD;
                }
                else if (CAMERA.angle.y < CAMERA_FIRST_PERSON_MAX_CLAMP * Raymath.DEG2RAD){
                    CAMERA.angle.y = CAMERA_FIRST_PERSON_MAX_CLAMP * Raymath.DEG2RAD;
                }

                // Recalculate camera target considering translation and rotation
                Matrix translation = Raymath.MatrixTranslate(0, 0, (CAMERA.targetDistance / CAMERA_FREE_PANNING_DIVIDER));
                Matrix rotation = Raymath.MatrixRotateXYZ(new Vector3((float) Math.PI * 2 - CAMERA.angle.y,
                        (float) Math.PI * 2 - CAMERA.angle.x, 0));
                Matrix transform = Raymath.MatrixMultiply(translation, rotation);

                camera.target.x = camera.position.x - transform.m12;
                camera.target.y = camera.position.y - transform.m13;
                camera.target.z = camera.position.z - transform.m14;

                // If movement detected (some key pressed), increase swinging
                for (int i = 0; i < 6; i++){
                    if (direction[i] == 1){
                        swingCounter++;
                        break;
                    }
                }

                // Camera position update
                // NOTE: On CAMERA_FIRST_PERSON player Y-movement is limited to player 'eyes position'
                camera.position.y = (float) (CAMERA.playerEyesPosition - Math.sin(swingCounter / CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER) / CAMERA_FIRST_PERSON_STEP_DIVIDER);

                camera.up.x = (float) (Math.sin(swingCounter / (CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER * 2)) / CAMERA_FIRST_PERSON_WAVING_DIVIDER);
                camera.up.z = (float) (-Math.sin(swingCounter / (CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER * 2)) / CAMERA_FIRST_PERSON_WAVING_DIVIDER);

            }
            break;
            case CAMERA_THIRD_PERSON:   // Camera moves as in a third-person game, following target at a
                // distance, controls are configurable
            {
                camera.position.x += (Math.sin(CAMERA.angle.x) * direction[1] -
                        Math.sin(CAMERA.angle.x) * direction[0] -
                        Math.cos(CAMERA.angle.x) * direction[3] +
                        Math.cos(CAMERA.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.y += (Math.sin(CAMERA.angle.y) * direction[0] -
                        Math.sin(CAMERA.angle.y) * direction[1] +
                        1.0f * direction[4] - 1.0f * direction[5]) / PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.z += (Math.cos(CAMERA.angle.x) * direction[1] -
                        Math.cos(CAMERA.angle.x) * direction[0] +
                        Math.sin(CAMERA.angle.x) * direction[3] -
                        Math.sin(CAMERA.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                // Camera orientation calculation
                CAMERA.angle.x += (mousePositionDelta.x * -CAMERA_MOUSE_MOVE_SENSITIVITY);
                CAMERA.angle.y += (mousePositionDelta.y * -CAMERA_MOUSE_MOVE_SENSITIVITY);

                // Angle clamp
                if (CAMERA.angle.y > CAMERA_THIRD_PERSON_MIN_CLAMP * Raymath.DEG2RAD){
                    CAMERA.angle.y = CAMERA_THIRD_PERSON_MIN_CLAMP * Raymath.DEG2RAD;
                }
                else if (CAMERA.angle.y < CAMERA_THIRD_PERSON_MAX_CLAMP * Raymath.DEG2RAD){
                    CAMERA.angle.y = CAMERA_THIRD_PERSON_MAX_CLAMP * Raymath.DEG2RAD;
                }

                // Camera zoom
                CAMERA.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);

                // Camera distance clamp
                if (CAMERA.targetDistance < CAMERA_THIRD_PERSON_DISTANCE_CLAMP){
                    CAMERA.targetDistance = CAMERA_THIRD_PERSON_DISTANCE_CLAMP;
                }

                // TODO: It seems camera.position is not correctly updated or some rounding issue makes the camera move straight to camera.target...
                camera.position.x = (float) (Math.sin(CAMERA.angle.x) * CAMERA.targetDistance * Math.cos(CAMERA.angle.y) + camera.target.x);

                if (CAMERA.angle.y <= 0.0f){
                    camera.position.y = (float) (Math.sin(CAMERA.angle.y) * CAMERA.targetDistance * Math.sin(CAMERA.angle.y) + camera.target.y);
                }
                else{
                    camera.position.y = (float) (-Math.sin(CAMERA.angle.y) * CAMERA.targetDistance * Math.sin(CAMERA.angle.y) + camera.target.y);
                }

                camera.position.z = (float) (Math.cos(CAMERA.angle.x) * CAMERA.targetDistance * Math.cos(CAMERA.angle.y) + camera.target.z);

            }
            break;
            case CAMERA_CUSTOM:
                break;
            default:
                break;
        }
    }

    // Set camera pan key to combine with mouse movement (free camera)
    void SetCameraPanControl(int keyPan){
        CAMERA.panControl = keyPan;
    }

    // Set camera alt key to combine with mouse movement (free camera)
    void SetCameraAltControl(int keyAlt){
        CAMERA.altControl = keyAlt;
    }

    // Set camera smooth zoom key to combine with mouse (free camera)
    void SetCameraSmoothZoomControl(int szoomKey){
        CAMERA.smoothZoomControl = szoomKey;
    }

    // Set camera move controls (1st person and 3rd person cameras)
    void SetCameraMoveControls(int keyFront, int keyBack, int keyRight, int keyLeft, int keyUp, int keyDown){
        CAMERA.MoveFront = keyFront;
        CAMERA.MoveBack = keyBack;
        CAMERA.MoveRight = keyRight;
        CAMERA.MoveLeft = keyLeft;
        CAMERA.MoveUp = keyUp;
        CAMERA.MoveDown = keyDown;

    }

}