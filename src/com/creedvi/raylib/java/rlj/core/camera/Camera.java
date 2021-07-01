package com.creedvi.raylib.java.rlj.core.camera;

import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.raymath.Matrix;
import com.creedvi.raylib.java.rlj.raymath.RayMath;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.raymath.Vector3;

import static com.creedvi.raylib.java.rlj.core.camera.Camera.CameraMode.*;
import static com.creedvi.raylib.java.rlj.core.camera.CameraData.CameraMove.*;
import static com.creedvi.raylib.java.rlj.raymath.RayMath.DEG2RAD;

public abstract class Camera{

    // Camera mouse movement sensitivity
    float CAMERA_MOUSE_MOVE_SENSITIVITY = 0.003f;
    float CAMERA_MOUSE_SCROLL_SENSITIVITY = 1.5f;

    // FREE_CAMERA
    float CAMERA_FREE_MOUSE_SENSITIVITY = 0.01f;
    float CAMERA_FREE_DISTANCE_MIN_CLAMP = 0.3f;
    float CAMERA_FREE_DISTANCE_MAX_CLAMP = 120.0f;
    float CAMERA_FREE_MIN_CLAMP = 85.0f;
    float CAMERA_FREE_MAX_CLAMP = -85.0f;
    float CAMERA_FREE_SMOOTH_ZOOM_SENSITIVITY = 0.05f;
    float CAMERA_FREE_PANNING_DIVIDER = 5.1f;

    // ORBITAL_CAMERA
    float CAMERA_ORBITAL_SPEED = 0.01f; // Radians per frame

    // FIRST_PERSON
    //float CAMERA_FIRST_PERSON_MOUSE_SENSITIVITY           0.003f
    float CAMERA_FIRST_PERSON_FOCUS_DISTANCE = 25.0f;
    float CAMERA_FIRST_PERSON_MIN_CLAMP = 89.0f;
    float CAMERA_FIRST_PERSON_MAX_CLAMP = -89.0f;

    float CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER = 8.0f;
    float CAMERA_FIRST_PERSON_STEP_DIVIDER = 30.0f;
    float CAMERA_FIRST_PERSON_WAVING_DIVIDER = 200.0f;

    // THIRD_PERSON
    //float CAMERA_THIRD_PERSON_MOUSE_SENSITIVITY           0.003f
    float CAMERA_THIRD_PERSON_DISTANCE_CLAMP = 1.2f;
    float CAMERA_THIRD_PERSON_MIN_CLAMP = 5.0f;
    float CAMERA_THIRD_PERSON_MAX_CLAMP = -85.0f;
    Vector3 CAMERA_THIRD_PERSON_OFFSET = new Vector3(0.4f, 0.0f, 0.0f);

    // PLAYER (used by camera)
    float PLAYER_MOVEMENT_SENSITIVITY = 20.0f;

    CameraData CAMERA = new CameraData();

    //CAMERA 2D
    public Vector3 offset;         // Camera offset (displacement from target)
    public Vector3 target;         // Camera target (rotation and zoom origin)
    public float rotation;         // Camera rotation in degrees
    public float zoom;             // Camera zoom (scaling), should be 1.0f by default

    public Vector3 position;       // Camera position
    public Vector3 up;             // Camera up vector (rotation over its axis)
    public float fovy;             // Camera field-of-view apperture in Y (degrees) in perspective, used as near plane width in orthographic
    public int type;               // Camera type, defines projection type: CAMERA_PERSPECTIVE or CAMERA_ORTHOGRAPHIC
    CameraData data;

    enum CameraMode{
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
            for(CameraMode m : values()){
                if(m.camMode == i){
                    return m;
                }
            }
            return CAMERA_CUSTOM;
        }
    }

    // Camera projection modes
    public enum CameraProjection{
        CAMERA_PERSPECTIVE(0),
        CAMERA_ORTHOGRAPHIC(1);

        final int camType;

        CameraProjection(int i){
            camType = i;
        }

        public int getCamType(){
            return camType;
        }
    }

    abstract public void update();

    void SetCameraMode(Camera camera, int mode)
    {
        Vector3 v1 = camera.getPosition();
        Vector3 v2 = new Vector3(camera.getTarget().x, camera.getTarget().y, 0);

        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;

        CAMERA.targetDistance = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);   // Distance to target

        // Camera angle calculation
        CAMERA.angle.x = (float) Math.atan2(dx, dz); // Camera angle in plane XZ (0 aligned with Z, move positive CCW)
        CAMERA.angle.y = (float) Math.atan2(dy, Math.sqrt(dx*dx + dz*dz)); // Camera angle in plane XY (0 aligned with X,
        // move positive CW)

        CAMERA.playerEyesPosition = camera.position.y;          // Init player eyes position to camera Y position

        // Lock cursor for first person and third person cameras
        if ((mode == CAMERA_FIRST_PERSON.camMode) || (mode == CAMERA_THIRD_PERSON.camMode)) Core.DisableCursor();
        else Core.EnableCursor();

        CAMERA.mode = mode;
    }

    // Update camera depending on selected mode
    // NOTE: Camera controls depend on some raylib functions:
    //       System: EnableCursor(), DisableCursor()
    //       Mouse: IsMouseButtonDown(), GetMousePosition(), GetMouseWheelMove()
    //       Keys:  IsKeyDown()
    // TODO: Port to quaternion-based camera (?)
    void UpdateCamera(Camera camera)
    {
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
        boolean szoomKey = Core.IsKeyDown(CAMERA.smoothZoomControl);
        int[] direction = new int[]{
            Core.IsKeyDown(MOVE_FRONT.key) ? 1 : 0,
            Core.IsKeyDown(MOVE_BACK.key) ? 1 : 0,
            Core.IsKeyDown(MOVE_RIGHT.key) ? 1 : 0,
            Core.IsKeyDown(MOVE_LEFT.key) ? 1 : 0,
            Core.IsKeyDown(MOVE_UP.key) ? 1 : 0,
            Core.IsKeyDown(MOVE_DOWN.key) ? 1 : 0
        };

        if (CAMERA.mode != CAMERA_CUSTOM.camMode)
        {
            mousePositionDelta.x = mousePosition.x - previousMousePosition.x;
            mousePositionDelta.y = mousePosition.y - previousMousePosition.y;

            previousMousePosition = mousePosition;
        }

        CameraMode modus = CameraMode.getByInt(CAMERA.mode);

        // Support for multiple automatic camera modes
        // NOTE: In case of CAMERA_CUSTOM nothing happens here, user must update it manually
        switch (modus)
        {
            case CAMERA_FREE:           // Camera free controls, using standard 3d-content-creation scheme
            {
                // Camera zoom
                if ((CAMERA.targetDistance < CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0))
                {
                    CAMERA.targetDistance -= (mouseWheelMove*CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (CAMERA.targetDistance > CAMERA_FREE_DISTANCE_MAX_CLAMP) CAMERA.targetDistance = CAMERA_FREE_DISTANCE_MAX_CLAMP;
                }

                // Camera looking down
                else if ((camera.position.y > camera.target.y) && (CAMERA.targetDistance == CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0))
                {
                    camera.target.x += mouseWheelMove*(camera.target.x - camera.position.x)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove*(camera.target.y - camera.position.y)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove*(camera.target.z - camera.position.z)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                }
                else if ((camera.position.y > camera.target.y) && (camera.target.y >= 0))
                {
                    camera.target.x += mouseWheelMove*(camera.target.x - camera.position.x)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove*(camera.target.y - camera.position.y)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove*(camera.target.z - camera.position.z)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;

                    // if (camera.target.y < 0) camera.target.y = -0.001;
                }
                else if ((camera.position.y > camera.target.y) && (camera.target.y < 0) && (mouseWheelMove > 0))
                {
                    CAMERA.targetDistance -= (mouseWheelMove*CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (CAMERA.targetDistance < CAMERA_FREE_DISTANCE_MIN_CLAMP) CAMERA.targetDistance = CAMERA_FREE_DISTANCE_MIN_CLAMP;
                }
                // Camera looking up
                else if ((camera.position.y < camera.target.y) && (CAMERA.targetDistance == CAMERA_FREE_DISTANCE_MAX_CLAMP) && (mouseWheelMove < 0))
                {
                    camera.target.x += mouseWheelMove*(camera.target.x - camera.position.x)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove*(camera.target.y - camera.position.y)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove*(camera.target.z - camera.position.z)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                }
                else if ((camera.position.y < camera.target.y) && (camera.target.y <= 0))
                {
                    camera.target.x += mouseWheelMove*(camera.target.x - camera.position.x)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.y += mouseWheelMove*(camera.target.y - camera.position.y)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;
                    camera.target.z += mouseWheelMove*(camera.target.z - camera.position.z)*CAMERA_MOUSE_SCROLL_SENSITIVITY/CAMERA.targetDistance;

                    // if (camera.target.y > 0) camera.target.y = 0.001;
                }
                else if ((camera.position.y < camera.target.y) && (camera.target.y > 0) && (mouseWheelMove > 0))
                {
                    CAMERA.targetDistance -= (mouseWheelMove*CAMERA_MOUSE_SCROLL_SENSITIVITY);
                    if (CAMERA.targetDistance < CAMERA_FREE_DISTANCE_MIN_CLAMP) CAMERA.targetDistance = CAMERA_FREE_DISTANCE_MIN_CLAMP;
                }

                // Input keys checks
                if (keyPan)
                {
                    if (keyAlt)     // Alternative key behaviour
                    {
                        if (szoomKey)
                        {
                            // Camera smooth zoom
                            CAMERA.targetDistance += (mousePositionDelta.y*CAMERA_FREE_SMOOTH_ZOOM_SENSITIVITY);
                        }
                        else
                        {
                            // Camera rotation
                            CAMERA.angle.x += mousePositionDelta.x*-CAMERA_FREE_MOUSE_SENSITIVITY;
                            CAMERA.angle.y += mousePositionDelta.y*-CAMERA_FREE_MOUSE_SENSITIVITY;

                            // Angle clamp
                            if (CAMERA.angle.y > CAMERA_FREE_MIN_CLAMP*DEG2RAD) CAMERA.angle.y = CAMERA_FREE_MIN_CLAMP*DEG2RAD;
                            else if (CAMERA.angle.y < CAMERA_FREE_MAX_CLAMP*DEG2RAD) CAMERA.angle.y = CAMERA_FREE_MAX_CLAMP*DEG2RAD;
                        }
                    }
                    else
                    {
                        // Camera panning
                        camera.target.x += ((mousePositionDelta.x*CAMERA_FREE_MOUSE_SENSITIVITY)*Math.cos(CAMERA.angle.x) + (mousePositionDelta.y*CAMERA_FREE_MOUSE_SENSITIVITY)*Math.sin(CAMERA.angle.x)*Math.sin(CAMERA.angle.y))*(CAMERA.targetDistance/CAMERA_FREE_PANNING_DIVIDER);
                        camera.target.y += ((mousePositionDelta.y*CAMERA_FREE_MOUSE_SENSITIVITY)*Math.cos(CAMERA.angle.y))*(CAMERA.targetDistance/CAMERA_FREE_PANNING_DIVIDER);
                        camera.target.z += ((mousePositionDelta.x*-CAMERA_FREE_MOUSE_SENSITIVITY)*Math.sin(CAMERA.angle.x) + (mousePositionDelta.y*CAMERA_FREE_MOUSE_SENSITIVITY)*Math.cos(CAMERA.angle.x)*Math.sin(CAMERA.angle.y))*(CAMERA.targetDistance/CAMERA_FREE_PANNING_DIVIDER);
                    }
                }

                // Update camera position with changes
                camera.position.x = (float) (-Math.sin(CAMERA.angle.x)*CAMERA.targetDistance*Math.cos(CAMERA.angle.y) + camera.target.x);
                camera.position.y = (float) (-Math.sin(CAMERA.angle.y)*CAMERA.targetDistance + camera.target.y);
                camera.position.z = (float) (-Math.cos(CAMERA.angle.x)*CAMERA.targetDistance*Math.cos(CAMERA.angle.y) + camera.target.z);

            } break;
            case CAMERA_ORBITAL:        // Camera just orbits around target, only zoom allowed
            {
                CAMERA.angle.x += CAMERA_ORBITAL_SPEED;      // Camera orbit angle
                CAMERA.targetDistance -= (mouseWheelMove*CAMERA_MOUSE_SCROLL_SENSITIVITY);   // Camera zoom

                // Camera distance clamp
                if (CAMERA.targetDistance < CAMERA_THIRD_PERSON_DISTANCE_CLAMP) CAMERA.targetDistance = CAMERA_THIRD_PERSON_DISTANCE_CLAMP;

                // Update camera position with changes
                camera.position.x = (float) (Math.sin(CAMERA.angle.x)*CAMERA.targetDistance*Math.cos(CAMERA.angle.y) + camera.target.x);
                camera.position.y = (float) (((CAMERA.angle.y <= 0.0f)? 1 : -1)*Math.sin(CAMERA.angle.y)*CAMERA.targetDistance*Math.sin(CAMERA.angle.y) + camera.target.y);
                camera.position.z = (float) (Math.cos(CAMERA.angle.x)*CAMERA.targetDistance*Math.cos(CAMERA.angle.y) + camera.target.z);

            } break;
            case CAMERA_FIRST_PERSON:   // Camera moves as in a first-person game, controls are configurable
            {
                camera.position.x += (Math.sin(CAMERA.angle.x)*direction[1] -
                        Math.sin(CAMERA.angle.x)*direction[0] -
                        Math.cos(CAMERA.angle.x)*direction[3] +
                        Math.cos(CAMERA.angle.x)*direction[2])/PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.y += (Math.sin(CAMERA.angle.y)*direction[0] -
                        Math.sin(CAMERA.angle.y)*direction[1] +
                        1.0f*direction[4] - 1.0f*direction[5])/PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.z += (Math.cos(CAMERA.angle.x)*direction[1] -
                        Math.cos(CAMERA.angle.x)*direction[0] +
                        Math.sin(CAMERA.angle.x)*direction[3] -
                        Math.sin(CAMERA.angle.x)*direction[2])/PLAYER_MOVEMENT_SENSITIVITY;

                // Camera orientation calculation
                CAMERA.angle.x += (mousePositionDelta.x*-CAMERA_MOUSE_MOVE_SENSITIVITY);
                CAMERA.angle.y += (mousePositionDelta.y*-CAMERA_MOUSE_MOVE_SENSITIVITY);

                // Angle clamp
                if (CAMERA.angle.y > CAMERA_FIRST_PERSON_MIN_CLAMP*DEG2RAD) CAMERA.angle.y = CAMERA_FIRST_PERSON_MIN_CLAMP*DEG2RAD;
                else if (CAMERA.angle.y < CAMERA_FIRST_PERSON_MAX_CLAMP*DEG2RAD) CAMERA.angle.y = CAMERA_FIRST_PERSON_MAX_CLAMP*DEG2RAD;

                // Recalculate camera target considering translation and rotation
                Matrix translation = RayMath.MatrixTranslate(0, 0, (CAMERA.targetDistance/CAMERA_FREE_PANNING_DIVIDER));
                Matrix rotation = RayMath.MatrixRotateXYZ(new Vector3 ((float) Math.PI*2 - CAMERA.angle.y,
                        (float) Math.PI*2 - CAMERA.angle.x, 0));
                Matrix transform = RayMath.MatrixMultiply(translation, rotation);

                camera.target.x = camera.position.x - transform.m12;
                camera.target.y = camera.position.y - transform.m13;
                camera.target.z = camera.position.z - transform.m14;

                // If movement detected (some key pressed), increase swinging
                for (int i = 0; i < 6; i++) if (direction[i] == 1) { swingCounter++; break; }

                // Camera position update
                // NOTE: On CAMERA_FIRST_PERSON player Y-movement is limited to player 'eyes position'
                camera.position.y = (float) (CAMERA.playerEyesPosition - Math.sin(swingCounter/CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER)/CAMERA_FIRST_PERSON_STEP_DIVIDER);

                camera.up.x = (float) (Math.sin(swingCounter/(CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER*2))/CAMERA_FIRST_PERSON_WAVING_DIVIDER);
                camera.up.z = (float) (-Math.sin(swingCounter/(CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER*2))/CAMERA_FIRST_PERSON_WAVING_DIVIDER);

            } break;
            case CAMERA_THIRD_PERSON:   // Camera moves as in a third-person game, following target at a
                // distance, controls are configurable
            {
                camera.position.x += (Math.sin(CAMERA.angle.x)*direction[1] -
                        Math.sin(CAMERA.angle.x)*direction[0] -
                        Math.cos(CAMERA.angle.x)*direction[3] +
                        Math.cos(CAMERA.angle.x)*direction[2])/PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.y += (Math.sin(CAMERA.angle.y)*direction[0] -
                        Math.sin(CAMERA.angle.y)*direction[1] +
                        1.0f*direction[4] - 1.0f*direction[5])/PLAYER_MOVEMENT_SENSITIVITY;

                camera.position.z += (Math.cos(CAMERA.angle.x)*direction[1] -
                        Math.cos(CAMERA.angle.x)*direction[0] +
                        Math.sin(CAMERA.angle.x)*direction[3] -
                        Math.sin(CAMERA.angle.x)*direction[2])/PLAYER_MOVEMENT_SENSITIVITY;

                // Camera orientation calculation
                CAMERA.angle.x += (mousePositionDelta.x*-CAMERA_MOUSE_MOVE_SENSITIVITY);
                CAMERA.angle.y += (mousePositionDelta.y*-CAMERA_MOUSE_MOVE_SENSITIVITY);

                // Angle clamp
                if (CAMERA.angle.y > CAMERA_THIRD_PERSON_MIN_CLAMP*DEG2RAD) CAMERA.angle.y = CAMERA_THIRD_PERSON_MIN_CLAMP*DEG2RAD;
                else if (CAMERA.angle.y < CAMERA_THIRD_PERSON_MAX_CLAMP*DEG2RAD) CAMERA.angle.y = CAMERA_THIRD_PERSON_MAX_CLAMP*DEG2RAD;

                // Camera zoom
                CAMERA.targetDistance -= (mouseWheelMove*CAMERA_MOUSE_SCROLL_SENSITIVITY);

                // Camera distance clamp
                if (CAMERA.targetDistance < CAMERA_THIRD_PERSON_DISTANCE_CLAMP) CAMERA.targetDistance = CAMERA_THIRD_PERSON_DISTANCE_CLAMP;

                // TODO: It seems camera.position is not correctly updated or some rounding issue makes the camera move straight to camera.target...
                camera.position.x = (float) (Math.sin(CAMERA.angle.x)*CAMERA.targetDistance*Math.cos(CAMERA.angle.y) + camera.target.x);

                if (CAMERA.angle.y <= 0.0f) camera.position.y = (float) (Math.sin(CAMERA.angle.y)*CAMERA.targetDistance*Math.sin(CAMERA.angle.y) + camera.target.y);
                else camera.position.y = (float) (-Math.sin(CAMERA.angle.y)*CAMERA.targetDistance*Math.sin(CAMERA.angle.y) + camera.target.y);

                camera.position.z = (float) (Math.cos(CAMERA.angle.x)*CAMERA.targetDistance*Math.cos(CAMERA.angle.y) + camera.target.z);

            } break;
            case CAMERA_CUSTOM: break;
            default: break;
        }
    }

    // Set camera pan key to combine with mouse movement (free camera)
    void SetCameraPanControl(int keyPan) { CAMERA.panControl = keyPan; }

    // Set camera alt key to combine with mouse movement (free camera)
    void SetCameraAltControl(int keyAlt) { CAMERA.altControl = keyAlt; }

    // Set camera smooth zoom key to combine with mouse (free camera)
    void SetCameraSmoothZoomControl(int szoomKey) { CAMERA.smoothZoomControl = szoomKey; }

    // Set camera move controls (1st person and 3rd person cameras)
    void SetCameraMoveControls(int keyFront, int keyBack, int keyRight, int keyLeft, int keyUp, int keyDown)
    {
        MOVE_FRONT.setKey(keyFront);
        MOVE_BACK.setKey(keyBack);
        MOVE_RIGHT.setKey(keyRight);
        MOVE_LEFT.setKey(keyLeft);
        MOVE_UP.setKey(keyUp);
        MOVE_DOWN.setKey(keyDown);
    }

    public Vector3 getPosition(){
        return position;
    }

    public void setPosition(Vector3 position){
        this.position = position;
    }

    public void setPosition(float x, float y, float z){
        this.position = new Vector3(x,y,z);
    }

    public Vector3 getOffset(){
        return offset;
    }

    public void setOffset(Vector3 offset){
        this.offset = offset;
    }

    public Vector3 getTarget(){
        return target;
    }

    public void setTarget(Vector3 target){
        this.target = target;
    }

    public float getRotation(){
        return rotation;
    }

    public void setRotation(float rotation){
        this.rotation = rotation;
    }

    public float getZoom(){
        return zoom;
    }

    public void setZoom(float zoom){
        this.zoom = zoom;
    }

}