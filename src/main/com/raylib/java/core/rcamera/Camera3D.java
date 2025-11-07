package com.raylib.java.core.rcamera;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.*;
import static com.raylib.java.raymath.Raymath.*;

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

    private final float CAMERA_CULL_DISTANCE_NEAR;
    private final float CAMERA_CULL_DISTANCE_FAR;

    private final float CAMERA_MOVE_SPEED = 0.09f;
    private final float CAMERA_ROTATION_SPEED = 0.03f;

    // Camera mouse movement sensitivity
    private final float CAMERA_MOUSE_MOVE_SENSITIVITY = 0.003f;    // TODO: it should be independant of framerate
    private final float CAMERA_MOUSE_SCROLL_SENSITIVITY = 1.5f;

    private final float CAMERA_ORBITAL_SPEED = 0.5f;               // Radians per second


    private final float CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER = 8.0f;
    private final float CAMERA_FIRST_PERSON_STEP_DIVIDER = 30.0f;
    private final float CAMERA_FIRST_PERSON_WAVING_DIVIDER = 200.0f;

    // PLAYER (used by camera)
    private final float PLAYER_MOVEMENT_SENSITIVITY = 20.0f;

    public Vector3 position;                // rCamera position
    public Vector3 target;
    public Vector3 up;                      // rCamera up vector (rotation over its axis)
    public float fovy;                      // rCamera field-of-view apperture in Y (degrees) in perspective, used as near plane width in orthographic
    public int projection;                  // rCamera projection, defines projection projection: CAMERA_PERSPECTIVE or CAMERA_ORTHOGRAPHIC

    private final Raylib context;

    public Camera3D(Raylib context){
        this.context = context;
        position = new Vector3();
        target = new Vector3();
        up = new Vector3();

        CAMERA_CULL_DISTANCE_NEAR = context.config.RL_CULL_DISTANCE_NEAR;
        CAMERA_CULL_DISTANCE_FAR = context.config.RL_CULL_DISTANCE_FAR;
    }

    public Camera3D(Raylib context, Vector3 position, Vector3 target, Vector3 up, Float fovy, int projection){
        this.context = context;
        this.position = position;
        this.target = target;
        this.up = up;
        this.fovy = fovy;
        this.projection = projection;

        CAMERA_CULL_DISTANCE_NEAR = context.config.RL_CULL_DISTANCE_NEAR;
        CAMERA_CULL_DISTANCE_FAR = context.config.RL_CULL_DISTANCE_FAR;
    }

    /**
     * Returns the camera's forward vector (normalised)
     * @return normalised forward vector
     */
    public Vector3 GetCameraForward() {
        return Vector3Normalize(Vector3Subtract(this.target, this.position));
    }

    /**
     * Returns the camera's up vector (normalised)
     * NOTE: The up vector might not be perpendicular to the forward vector.
     * @return normalised up vector
     */
    public Vector3 GetCameraUp() {
        return Vector3Normalize(this.up);
    }

    /**
     * Returns the camera's right vector (normalised)
     * @return normalised right vector
     */
    public Vector3 GetCameraRight() {
        Vector3 forward = this.GetCameraForward();
        Vector3 up = this.GetCameraUp();

        return Vector3CrossProduct(forward, up);
    }

    /**
     * Moves the camera forward in its forward direction
     * @param distance units the camera should move
     * @param moveInWorldPlane normalises the camera's forward vector
     */
    public void MoveForward(float distance, boolean moveInWorldPlane) {
        Vector3 forward = this.GetCameraForward();

        if (moveInWorldPlane) {
            // Project vector onto world plane
            forward.y = 0;
            forward = Vector3Normalize(forward);
        }

        // Scale by distance
        forward = Vector3Scale(forward, distance);

        // Move position and target
        this.position = Vector3Add(this.position, forward);
        this.target = Vector3Add(this.target, forward);
    }

    /**
     * Moves the camera along its up vector
     * @param distance units the camera should move
     */
    public void MoveUp(float distance) {
        Vector3 up = this.GetCameraUp();

        // Scale by distance
        up = Vector3Scale(up, distance);

        // Move position and target
        this.position = Vector3Add(this.position, up);
        this.target = Vector3Add(this.target, up);
    }

    /**
     * Moves the camera right in its current right direction
     * @param distance units the camera should move
     * @param moveInWorldPlane normalises the camera's right vector
     */
    public void MoveRight(float distance, boolean moveInWorldPlane) {
        Vector3 right = GetCameraRight();

        if (moveInWorldPlane) {
            // Project vector onto world plane
            right.y = 0;
            right = Vector3Normalize(right);
        }

        // Scale by distance
        right = Vector3Scale(right, distance);

        // Move position and target
        this.position = Vector3Add(this.position, right);
        this.target = Vector3Add(this.target, right);
    }

    /**
     * Moves the camera position closer/farther to/from the camera target
     * @param delta units the camera should move towards or away from the camera target
     */
    public void CameraMoveToTarget(float delta) {
        float distance = Vector3Distance(this.position, this.target);

        // Apply delta
        distance += delta;

        // Distance must be greater than 0
        if (distance < 0) distance = 0.001f;

        // Set new distance by moving the position along the forward vector
        Vector3 forward = this.GetCameraForward();
        this.position = Vector3Add(this.target, Vector3Scale(forward, -distance));
    }

    /**
     * Rotates the camera around its up vector
     * Yaw is "looking left and right"
     * @param angle angle of rotation in radians
     * @param rotateAroundTarget If rotateAroundTarget is false, the camera rotates around its position
     */
    public void Yaw(float angle, boolean rotateAroundTarget) {
        // Rotation axis
        Vector3 up = this.GetCameraUp();

        // View vector
        Vector3 targetPosition = Vector3Subtract(this.target, this.position);

        // Rotate view vector around up axis
        targetPosition = Vector3RotateByAxisAngle(targetPosition, up, angle);

        if (rotateAroundTarget) {
            // Move position relative to target
            this.position = Vector3Subtract(this.target, targetPosition);
        }
        // rotate around camera.position
        else {
            // Move target relative to position
            this.target = Vector3Add(this.position, targetPosition);
        }
    }

    /**
     * Rotates the camera around its right vector
     * Pitch is "looking up or down."
     * @param angle angle of rotation in radians
     * @param lockView prevent camera from overrotation (aka "somersaults")
     * @param rotateAroundTarget if false, the camera will rotate around its position
     * @param rotateUp apply rotation to up direction
     */
    public void Pitch(float angle, boolean lockView, boolean rotateAroundTarget, boolean rotateUp) {
        // Up direction
        Vector3 up = this.GetCameraUp();

        // View vector
        Vector3 targetPosition = Vector3Subtract(this.target, this.position);

        if (lockView)
        {
            // In these camera modes we clamp the Pitch angle
            // to allow only viewing straight up or down.

            // Clamp view up
            float maxAngleUp = Vector3Angle(up, targetPosition);
            maxAngleUp -= 0.001f; // avoid numerical errors
            if (angle > maxAngleUp) {
                angle = maxAngleUp;
            }

            // Clamp view down
            float maxAngleDown = Vector3Angle(Vector3Negate(up), targetPosition);
            maxAngleDown *= -1.0f; // downwards angle is negative
            maxAngleDown += 0.001f; // avoid numerical errors
            if (angle < maxAngleDown) {
                angle = maxAngleDown;
            }
        }

        // Rotation axis
        Vector3 right = this.GetCameraRight();

        // Rotate view vector around right axis
        targetPosition = Vector3RotateByAxisAngle(targetPosition, right, angle);

        if (rotateAroundTarget)
        {
            // Move position relative to target
            this.position = Vector3Subtract(this.target, targetPosition);
        }
        else // rotate around camera.position
        {
            // Move target relative to position
            this.target = Vector3Add(this.position, targetPosition);
        }

        if (rotateUp)
        {
            // Rotate up direction around right axis
            this.up = Vector3RotateByAxisAngle(this.up, right, angle);
        }
    }

    /**
     * Rotates the camera around its forward vector
     * Roll is tilting to the left or right
     * @param angle angle of roll in radians
      */
    public void Roll(float angle) {
        // Rotation axis
        Vector3 forward = GetCameraForward();

        // Rotate up direction around forward axis
        this.up = Vector3RotateByAxisAngle(this.up, forward, angle);
    }

    /**
     * Returns the camera view matrix
     * @return camera view matrix
     */
    public Matrix GetCameraViewMatrix() {
        return MatrixLookAt(this.position, this.target, this.up);
    }

    /**
     * Return the camera's projection matrix
     * @param aspect camera's aspect ratio as a decimal (width/height)
     * @return Camera's projection matrix.
     */
    public Matrix GetCameraProjectionMatrix(float aspect)
    {
        if (this.projection == CAMERA_PERSPECTIVE)
        {
            return MatrixPerspective(this.fovy*DEG2RAD, aspect, CAMERA_CULL_DISTANCE_NEAR, CAMERA_CULL_DISTANCE_FAR);
        }
        else if (this.projection == CAMERA_ORTHOGRAPHIC)
        {
            double top = this.fovy/2.0;
            double right = top*aspect;

            return MatrixOrtho(-right, right, -top, top, CAMERA_CULL_DISTANCE_NEAR, CAMERA_CULL_DISTANCE_FAR);
        }

        return MatrixIdentity();
    }

    /**
     * Update camera position for selected mode
     * @param mode camera mode defined in `CameraMode` class: CAMERA_FREE, CAMERA_FIRST_PERSON, CAMERA_THIRD_PERSON, CAMERA_ORBITAL or CUSTOM
     */
    public void Update(int mode) {
        Vector2 mousePositionDelta = this.context.core.GetMouseDelta();

        boolean moveInWorldPlane = ((mode == CAMERA_FIRST_PERSON) || (mode == CAMERA_THIRD_PERSON));
        boolean rotateAroundTarget = ((mode == CAMERA_THIRD_PERSON) || (mode == CAMERA_ORBITAL));
        boolean lockView = ((mode == CAMERA_FIRST_PERSON) || (mode == CAMERA_THIRD_PERSON) || (mode == CAMERA_ORBITAL));
        boolean rotateUp = (mode == CAMERA_FREE);

        if (mode == CAMERA_ORBITAL) {
            // Orbital can just orbit
            Matrix rotation = MatrixRotate(this.GetCameraUp(), CAMERA_ORBITAL_SPEED * this.context.core.GetFrameTime());
            Vector3 view = Vector3Subtract(this.position, this.target);
            view = Vector3Transform(view, rotation);
            this.position = Vector3Add(this.target, view);
        }
        else {
            // Camera rotation
            if (this.context.core.IsKeyDown(KEY_DOWN)) this.Pitch(-CAMERA_ROTATION_SPEED, lockView, rotateAroundTarget, rotateUp);
            if (this.context.core.IsKeyDown(KEY_UP)) this.Pitch(CAMERA_ROTATION_SPEED, lockView, rotateAroundTarget, rotateUp);
            if (this.context.core.IsKeyDown(KEY_RIGHT)) this.Yaw(-CAMERA_ROTATION_SPEED, rotateAroundTarget);
            if (this.context.core.IsKeyDown(KEY_LEFT)) this.Yaw(CAMERA_ROTATION_SPEED, rotateAroundTarget);
            if (this.context.core.IsKeyDown(KEY_Q)) this.Roll(-CAMERA_ROTATION_SPEED);
            if (this.context.core.IsKeyDown(KEY_E)) this.Roll(CAMERA_ROTATION_SPEED);

            this.Yaw(-mousePositionDelta.x*CAMERA_MOUSE_MOVE_SENSITIVITY, rotateAroundTarget);
            this.Pitch(-mousePositionDelta.y*CAMERA_MOUSE_MOVE_SENSITIVITY, lockView, rotateAroundTarget, rotateUp);

            // Camera movement
            if (this.context.core.IsKeyDown(KEY_W)) this.MoveForward(CAMERA_MOVE_SPEED, moveInWorldPlane);
            if (this.context.core.IsKeyDown(KEY_A)) this.MoveRight(-CAMERA_MOVE_SPEED, moveInWorldPlane);
            if (this.context.core.IsKeyDown(KEY_S)) this.MoveForward(-CAMERA_MOVE_SPEED, moveInWorldPlane);
            if (this.context.core.IsKeyDown(KEY_D)) this.MoveRight(CAMERA_MOVE_SPEED, moveInWorldPlane);
            //if (this.context.core.IsKeyDown(KEY_SPACE)) CameraMoveUp(CAMERA_MOVE_SPEED);
            //if (this.context.core.IsKeyDown(KEY_LEFT_CONTROL)) CameraMoveUp(-CAMERA_MOVE_SPEED);
        }

        if ((mode == CAMERA_THIRD_PERSON) || (mode == CAMERA_ORBITAL)) {
            // Zoom target distance
            CameraMoveToTarget(-this.context.core.GetMouseWheelMove());
            if (this.context.core.IsKeyPressed(KEY_KP_SUBTRACT)) CameraMoveToTarget(2.0f);
            if (this.context.core.IsKeyPressed(KEY_KP_ADD)) CameraMoveToTarget(-2.0f);
        }
    }

    /**
     * Update camera movement
     * @param movement `Vector3` specifying units to move in the X, Y, Z axis
     * @param rotation `Vector3` specifying *DEGREES* to rotate along the X, Y, Z axis
     * @param zoom Amount the camera should zoom
     */
    public void Update(Vector3 movement, Vector3 rotation, float zoom) {

        // Required values
        // movement.x - Move forward/backward
        // movement.y - Move right/left
        // movement.z - Move up/down
        // rotation.x - yaw
        // rotation.y - pitch
        // rotation.z - roll
        // zoom - Move towards target

        boolean lockView = true;
        boolean rotateAroundTarget = false;
        boolean rotateUp = false;
        boolean moveInWorldPlane = true;

        // Camera rotation
        this.Pitch(-rotation.y*DEG2RAD, lockView, rotateAroundTarget, rotateUp);
        this.Yaw(-rotation.x*DEG2RAD, rotateAroundTarget);
        this.Roll(rotation.z*DEG2RAD);

        // Camera movement
        this.MoveForward(movement.x, moveInWorldPlane);
        this.MoveRight(movement.y, moveInWorldPlane);
        this.MoveUp(movement.z);

        // Zoom target distance
        this.CameraMoveToTarget(zoom);
    }
}
