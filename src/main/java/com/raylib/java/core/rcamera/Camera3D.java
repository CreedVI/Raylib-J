package com.raylib.java.core.rcamera;

import com.raylib.java.Raylib;
import com.raylib.java.core.rCore;
import com.raylib.java.raymath.Matrix;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.raymath.Vector2;
import com.raylib.java.raymath.Vector3;

public class Camera3D {

    // rCamera projection modes
    public static class CameraProjection{
        public static int
                CAMERA_PERSPECTIVE = 0,
                CAMERA_ORTHOGRAPHIC = 1;
    }

    public static class CameraMode{
        public static final int CAMERA_CUSTOM = 0,
        CAMERA_FREE = 1,
        CAMERA_ORBITAL = 2,
        CAMERA_FIRST_PERSON = 3,
        CAMERA_THIRD_PERSON = 4;
    }

    // rCamera mouse movement sensitivity
    private static float CAMERA_MOUSE_MOVE_SENSITIVITY = 0.003f;
    private static float CAMERA_MOUSE_SCROLL_SENSITIVITY = 1.5f;

    // FREE_CAMERA
    private static float CAMERA_FREE_MOUSE_SENSITIVITY = 0.01f;
    private static float CAMERA_FREE_DISTANCE_MIN_CLAMP = 0.3f;
    private static float CAMERA_FREE_DISTANCE_MAX_CLAMP = 120.0f;
    private static float CAMERA_FREE_MIN_CLAMP = 85.0f;
    private static float CAMERA_FREE_MAX_CLAMP = -85.0f;
    private static float CAMERA_FREE_SMOOTH_ZOOM_SENSITIVITY = 0.05f;
    private static float CAMERA_FREE_PANNING_DIVIDER = 5.1f;

    // ORBITAL_CAMERA
    private static float CAMERA_ORBITAL_SPEED = 0.01f; // Radians per frame

    // FIRST_PERSON
    //float CAMERA_FIRST_PERSON_MOUSE_SENSITIVITY           0.003f
    private float CAMERA_FIRST_PERSON_FOCUS_DISTANCE = 25.0f;
    private static float CAMERA_FIRST_PERSON_MIN_CLAMP = 89.0f;
    private static float CAMERA_FIRST_PERSON_MAX_CLAMP = -89.0f;

    private static float CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER = 8.0f;
    private static float CAMERA_FIRST_PERSON_STEP_DIVIDER = 30.0f;
    private static float CAMERA_FIRST_PERSON_WAVING_DIVIDER = 200.0f;

    // THIRD_PERSON
    //float CAMERA_THIRD_PERSON_MOUSE_SENSITIVITY           0.003f
    private static float CAMERA_THIRD_PERSON_DISTANCE_CLAMP = 1.2f;
    private static float CAMERA_THIRD_PERSON_MIN_CLAMP = 5.0f;
    private static float CAMERA_THIRD_PERSON_MAX_CLAMP = -85.0f;
    private Vector3 CAMERA_THIRD_PERSON_OFFSET = new Vector3(0.4f, 0.0f, 0.0f);

    // PLAYER (used by camera)
    private static float PLAYER_MOVEMENT_SENSITIVITY = 20.0f;

    public Vector3 position;                // rCamera position
    public Vector3 up;                      // rCamera up vector (rotation over its axis)
    public Vector3 target;
    private Vector2 previousMousePosition;
    public float fovy;                      // rCamera field-of-view apperture in Y (degrees) in perspective, used as near plane width in orthographic
    public int projection;                  // rCamera projection, defines projection projection: CAMERA_PERSPECTIVE or CAMERA_ORTHOGRAPHIC
    CameraData cameraData;

    private final Raylib context;

    public Camera3D(Raylib context){
        this.context = context;
        position = new Vector3();
        target = new Vector3();
        up = new Vector3();

        previousMousePosition = new Vector2();

        cameraData = new CameraData();
    }

    public Camera3D(Raylib context, CameraData cameraData){
        this.context = context;
        position = new Vector3();
        target = new Vector3();
        up = new Vector3();
        previousMousePosition = new Vector2();
        this.cameraData = cameraData;
    }

    public Camera3D(Raylib context, Vector3 position, Vector3 target, Vector3 up, Float fovy, int projection){
        this.context = context;
        this.position = position;
        this.target = target;
        this.up = up;
        this.fovy = fovy;
        this.projection = projection;
        previousMousePosition = new Vector2();
        this.cameraData = new CameraData();
    }

    // Update camera depending on selected mode
    // NOTE: rCamera controls depend on some raylib functions:
    //       System: EnableCursor(), DisableCursor()
    //       Mouse: IsMouseButtonDown(), GetMousePosition(), GetMouseWheelMove()
    //       Keys:  IsKeyDown()
    // TODO: Port to quaternion-based camera (?)
    public void UpdateCamera(){
        int swingCounter = 0;    // Used for 1st person swinging movement

        // TODO: Compute cameraData.targetDistance and cameraData.angle here (?)

        // Mouse movement detection
        Vector2 mousePositionDelta = new Vector2(0.0f, 0.0f);
        Vector2 mousePosition = context.core.GetMousePosition();
        float mouseWheelMove = context.core.GetMouseWheelMove();

        // Keys input detection
        // TODO: Input detection is raylib-dependant, it could be moved outside the module
        boolean keyPan = context.core.IsMouseButtonDown(cameraData.panControl);
        boolean keyAlt = context.core.IsKeyDown(cameraData.altControl);
        boolean sZoomKey = context.core.IsKeyDown(cameraData.smoothZoomControl);
        int[] direction = new int[]{
                context.core.IsKeyDown(cameraData.MoveFront) ? 1 : 0,
                context.core.IsKeyDown(cameraData.MoveBack) ? 1 : 0,
                context.core.IsKeyDown(cameraData.MoveRight) ? 1 : 0,
                context.core.IsKeyDown(cameraData.MoveLeft) ? 1 : 0,
                context.core.IsKeyDown(cameraData.MoveUp) ? 1 : 0,
                context.core.IsKeyDown(cameraData.MoveDown) ? 1 : 0
        };

        if (cameraData.mode != CameraMode.CAMERA_CUSTOM) {
            mousePositionDelta.x = mousePosition.x - previousMousePosition.x;
            mousePositionDelta.y = mousePosition.y - previousMousePosition.y;

            previousMousePosition = mousePosition;
        }


        // Support for multiple automatic camera modes
        // NOTE: In case of CAMERA_CUSTOM nothing happens here, user must update it manually
        switch (cameraData.mode) {
            case CameraMode.CAMERA_FREE: {          // rCamera free controls, using standard 3d-content-creation scheme
                // rCamera zoom
                if ((cameraData.targetDistance < CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0)){
                    cameraData.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (cameraData.targetDistance > CAMERA_FREE_DISTANCE_MAX_CLAMP) {
                        cameraData.targetDistance = CAMERA_FREE_DISTANCE_MAX_CLAMP;
                    }
                }

                // rCamera looking down
                else if ((position.y > target.y) && (cameraData.targetDistance == CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0)){
                    target.x += mouseWheelMove * (target.x - position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.y += mouseWheelMove * (target.y - position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.z += mouseWheelMove * (target.z - position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                }
                else if ((position.y > target.y) && (target.y >= 0)){
                    target.x += mouseWheelMove * (target.x - position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.y += mouseWheelMove * (target.y - position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.z += mouseWheelMove * (target.z - position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;

                    // if (target.y < 0) target.y = -0.001;
                }
                else if ((position.y > target.y) && (target.y < 0) && (mouseWheelMove > 0)){
                    cameraData.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (cameraData.targetDistance < CAMERA_FREE_DISTANCE_MIN_CLAMP) {
                        cameraData.targetDistance = CAMERA_FREE_DISTANCE_MIN_CLAMP;
                    }
                }
                // rCamera looking up
                else if ((position.y < target.y) && (cameraData.targetDistance == CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0)){
                    target.x += mouseWheelMove * (target.x - position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.y += mouseWheelMove * (target.y - position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.z += mouseWheelMove * (target.z - position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                }
                else if ((position.y < target.y) && (target.y <= 0)){
                    target.x += mouseWheelMove * (target.x - position.x) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.y += mouseWheelMove * (target.y - position.y) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;
                    target.z += mouseWheelMove * (target.z - position.z) * CAMERA_MOUSE_SCROLL_SENSITIVITY / cameraData.targetDistance;

                    // if (target.y > 0) target.y = 0.001;
                }
                else if ((position.y < target.y) && (target.y > 0) && (mouseWheelMove > 0)){
                    cameraData.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (cameraData.targetDistance < CAMERA_FREE_DISTANCE_MIN_CLAMP) {
                        cameraData.targetDistance = CAMERA_FREE_DISTANCE_MIN_CLAMP;
                    }
                }

                // Input keys checks
                if (keyPan) {
                    if (keyAlt)     // Alternative key behaviour
                    {
                        if (sZoomKey) {
                            // rCamera smooth zoom
                            cameraData.targetDistance += (mousePositionDelta.y * CAMERA_FREE_SMOOTH_ZOOM_SENSITIVITY);
                        }
                        else{
                            // rCamera rotation
                            cameraData.angle.x += mousePositionDelta.x * -CAMERA_FREE_MOUSE_SENSITIVITY;
                            cameraData.angle.y += mousePositionDelta.y * -CAMERA_FREE_MOUSE_SENSITIVITY;

                            // Angle clamp
                            if (cameraData.angle.y > CAMERA_FREE_MIN_CLAMP * Raymath.DEG2RAD) {
                                cameraData.angle.y = CAMERA_FREE_MIN_CLAMP * Raymath.DEG2RAD;
                            }
                            else if (cameraData.angle.y < CAMERA_FREE_MAX_CLAMP * Raymath.DEG2RAD) {
                                cameraData.angle.y = CAMERA_FREE_MAX_CLAMP * Raymath.DEG2RAD;
                            }
                        }
                    }
                    else{
                        // rCamera panning
                        target.x += ((mousePositionDelta.x*CAMERA_FREE_MOUSE_SENSITIVITY)*Math.cos(cameraData.angle.x) + (mousePositionDelta.y*-CAMERA_FREE_MOUSE_SENSITIVITY)*Math.sin(cameraData.angle.x)*Math.sin(cameraData.angle.y))*(cameraData.targetDistance/CAMERA_FREE_PANNING_DIVIDER);
                        target.y += ((mousePositionDelta.y*CAMERA_FREE_MOUSE_SENSITIVITY)*Math.cos(cameraData.angle.y))*(cameraData.targetDistance/CAMERA_FREE_PANNING_DIVIDER);
                        target.z += ((mousePositionDelta.x*-CAMERA_FREE_MOUSE_SENSITIVITY)*Math.sin(cameraData.angle.x) + (mousePositionDelta.y*-CAMERA_FREE_MOUSE_SENSITIVITY)*Math.cos(cameraData.angle.x)*Math.sin(cameraData.angle.y))*(cameraData.targetDistance/CAMERA_FREE_PANNING_DIVIDER);
                    }

                }

                // Update camera position with changes
                position.x = (float) -Math.sin(cameraData.angle.x) * cameraData.targetDistance * (float) Math.cos(cameraData.angle.y) + target.x;
                position.y = (float) -Math.sin(cameraData.angle.y) * cameraData.targetDistance + target.y;
                position.z = (float) -Math.cos(cameraData.angle.x) * cameraData.targetDistance * (float) Math.cos(cameraData.angle.y) + target.z;

            }
            break;
            case CameraMode.CAMERA_ORBITAL: {        // rCamera just orbits around target, only zoom allowed
                cameraData.angle.x += CAMERA_ORBITAL_SPEED;      // rCamera orbit angle
                cameraData.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);   // rCamera zoom

                // rCamera distance clamp
                if (cameraData.targetDistance < CAMERA_THIRD_PERSON_DISTANCE_CLAMP) {
                    cameraData.targetDistance = CAMERA_THIRD_PERSON_DISTANCE_CLAMP;
                }

                // Update camera position with changes
                position.x = (float) (Math.sin(cameraData.angle.x) * cameraData.targetDistance * Math.cos(cameraData.angle.y) + target.x);
                position.y = (float) (((cameraData.angle.y <= 0.0f) ? 1 : -1) * Math.sin(cameraData.angle.y) * cameraData.targetDistance * Math.sin(cameraData.angle.y) + target.y);
                position.z = (float) (Math.cos(cameraData.angle.x) * cameraData.targetDistance * Math.cos(cameraData.angle.y) + target.z);

            }
            break;
            case CameraMode.CAMERA_FIRST_PERSON: {   // rCamera moves as in a first-person game, controls are configurable
                position.x += (Math.sin(cameraData.angle.x) * direction[1] -
                        Math.sin(cameraData.angle.x) * direction[0] -
                        Math.cos(cameraData.angle.x) * direction[3] +
                        Math.cos(cameraData.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                position.y += (Math.sin(cameraData.angle.y) * direction[0] -
                        Math.sin(cameraData.angle.y) * direction[1] +
                        1.0f * direction[4] - 1.0f * direction[5]) / PLAYER_MOVEMENT_SENSITIVITY;

                position.z += (Math.cos(cameraData.angle.x) * direction[1] -
                        Math.cos(cameraData.angle.x) * direction[0] +
                        Math.sin(cameraData.angle.x) * direction[3] -
                        Math.sin(cameraData.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                // rCamera orientation calculation
                cameraData.angle.x += (mousePositionDelta.x * -CAMERA_MOUSE_MOVE_SENSITIVITY);
                cameraData.angle.y += (mousePositionDelta.y * -CAMERA_MOUSE_MOVE_SENSITIVITY);

                // Angle clamp
                if (cameraData.angle.y > CAMERA_FIRST_PERSON_MIN_CLAMP * Raymath.DEG2RAD) {
                    cameraData.angle.y = CAMERA_FIRST_PERSON_MIN_CLAMP * Raymath.DEG2RAD;
                }
                else if (cameraData.angle.y < CAMERA_FIRST_PERSON_MAX_CLAMP * Raymath.DEG2RAD) {
                    cameraData.angle.y = CAMERA_FIRST_PERSON_MAX_CLAMP * Raymath.DEG2RAD;
                }

                // Recalculate camera target considering translation and rotation
                Matrix translation = Raymath.MatrixTranslate(0, 0, (cameraData.targetDistance / CAMERA_FREE_PANNING_DIVIDER));
                Matrix rotation = Raymath.MatrixRotateXYZ(new Vector3((float) Math.PI * 2 - cameraData.angle.y,
                        (float) Math.PI * 2 - cameraData.angle.x, 0));
                Matrix transform = Raymath.MatrixMultiply(translation, rotation);

                target.x = position.x - transform.m12;
                target.y = position.y - transform.m13;
                target.z = position.z - transform.m14;

                // If movement detected (some key pressed), increase swinging
                for (int i = 0; i < 6; i++){
                    if (direction[i] == 1){
                        swingCounter++;
                        break;
                    }
                }

                // rCamera position update
                // NOTE: On CAMERA_FIRST_PERSON player Y-movement is limited to player 'eyes position'
                position.y = (float) (cameraData.playerEyesPosition - Math.sin(swingCounter / CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER) / CAMERA_FIRST_PERSON_STEP_DIVIDER);

                up.x = (float) (Math.sin(swingCounter / (CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER * 2)) / CAMERA_FIRST_PERSON_WAVING_DIVIDER);
                up.z = (float) (-Math.sin(swingCounter / (CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER * 2)) / CAMERA_FIRST_PERSON_WAVING_DIVIDER);

            }
            break;
            case CameraMode.CAMERA_THIRD_PERSON:{
                // rCamera moves as in a third-person game, following target at a
                // distance, controls are configurable
                position.x += (Math.sin(cameraData.angle.x) * direction[1] -
                        Math.sin(cameraData.angle.x) * direction[0] -
                        Math.cos(cameraData.angle.x) * direction[3] +
                        Math.cos(cameraData.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                position.y += (Math.sin(cameraData.angle.y) * direction[0] -
                        Math.sin(cameraData.angle.y) * direction[1] +
                        1.0f * direction[4] - 1.0f * direction[5]) / PLAYER_MOVEMENT_SENSITIVITY;

                position.z += (Math.cos(cameraData.angle.x) * direction[1] -
                        Math.cos(cameraData.angle.x) * direction[0] +
                        Math.sin(cameraData.angle.x) * direction[3] -
                        Math.sin(cameraData.angle.x) * direction[2]) / PLAYER_MOVEMENT_SENSITIVITY;

                // rCamera orientation calculation
                cameraData.angle.x += (mousePositionDelta.x * -CAMERA_MOUSE_MOVE_SENSITIVITY);
                cameraData.angle.y += (mousePositionDelta.y * -CAMERA_MOUSE_MOVE_SENSITIVITY);

                // Angle clamp
                if (cameraData.angle.y > CAMERA_THIRD_PERSON_MIN_CLAMP * Raymath.DEG2RAD){
                    cameraData.angle.y = CAMERA_THIRD_PERSON_MIN_CLAMP * Raymath.DEG2RAD;
                }
                else if (cameraData.angle.y < CAMERA_THIRD_PERSON_MAX_CLAMP * Raymath.DEG2RAD){
                    cameraData.angle.y = CAMERA_THIRD_PERSON_MAX_CLAMP * Raymath.DEG2RAD;
                }

                // rCamera zoom
                cameraData.targetDistance -= (mouseWheelMove * CAMERA_MOUSE_SCROLL_SENSITIVITY);

                // rCamera distance clamp
                if (cameraData.targetDistance < CAMERA_THIRD_PERSON_DISTANCE_CLAMP){
                    cameraData.targetDistance = CAMERA_THIRD_PERSON_DISTANCE_CLAMP;
                }

                // TODO: It seems position is not correctly updated or some rounding issue makes the camera move straight to target...
                position.x = (float) (Math.sin(cameraData.angle.x) * cameraData.targetDistance * Math.cos(cameraData.angle.y) + target.x);

                if (cameraData.angle.y <= 0.0f){
                    position.y = (float) (Math.sin(cameraData.angle.y) * cameraData.targetDistance * Math.sin(cameraData.angle.y) + target.y);
                }
                else{
                    position.y = (float) (-Math.sin(cameraData.angle.y) * cameraData.targetDistance * Math.sin(cameraData.angle.y) + target.y);
                }

                position.z = (float) (Math.cos(cameraData.angle.x) * cameraData.targetDistance * Math.cos(cameraData.angle.y) + target.z);

            }
            break;
            case CameraMode.CAMERA_CUSTOM:
            default:
                break;
        }
    }

    public void SetCameraMode(int mode) {
        Vector3 v1 = position;
        Vector3 v2 = new Vector3(target.x, target.y, 0);

        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;

        cameraData.targetDistance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);   // Distance to target

        // rCamera angle calculation
        cameraData.angle.x = (float) Math.atan2(dx, dz); // rCamera angle in plane XZ (0 aligned with Z, move positive CCW)
        cameraData.angle.y = (float) Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)); // rCamera angle in plane XY (0 aligned with X,
        // move positive CW)

        cameraData.playerEyesPosition = this.position.y;          // Init player eyes position to camera Y position

        // Lock cursor for first person and third person cameras
        if ((mode == CameraMode.CAMERA_FIRST_PERSON) || (mode == CameraMode.CAMERA_THIRD_PERSON)){
            context.core.DisableCursor();
        }
        else{
            context.core.EnableCursor();
        }

        cameraData.mode = mode;
    }

    // Set camera pan key to combine with mouse movement (free camera)
    public void SetCameraPanControl(int keyPan){
        cameraData.panControl = keyPan;
    }

    // Set camera alt key to combine with mouse movement (free camera)
    public void SetCameraAltControl(int keyAlt){
        cameraData.altControl = keyAlt;
    }

    // Set camera smooth zoom key to combine with mouse (free camera)
    public void SetCameraSmoothZoomControl(int szoomKey){
        cameraData.smoothZoomControl = szoomKey;
    }

    // Set camera move controls (1st person and 3rd person cameras)
    public void SetCameraMoveControls(int keyFront, int keyBack, int keyRight, int keyLeft, int keyUp, int keyDown){
        cameraData.MoveFront = keyFront;
        cameraData.MoveBack = keyBack;
        cameraData.MoveRight = keyRight;
        cameraData.MoveLeft = keyLeft;
        cameraData.MoveUp = keyUp;
        cameraData.MoveDown = keyDown;
    }
    
    public Vector3 getPosition(){
        return position;
    }

    public Vector3 getTarget(){
        return target;
    }

    public Vector3 getUp(){
        return up;
    }

    public float getFovy(){
        return fovy;
    }

    public int getProjection(){
        return projection;
    }

    public int getCameraMode() {
        return cameraData.mode;
    }

    public void setTarget(Vector3 target){
        this.target = target;
    }

    public void setFovy(float fovy){
        this.fovy = fovy;
    }

    public void setPosition(Vector3 position){
        this.position = position;
    }

    public void setprojection(int projection){
        this.projection = projection;
    }

    public void setUp(Vector3 up){
        this.up = up;
    }

}
