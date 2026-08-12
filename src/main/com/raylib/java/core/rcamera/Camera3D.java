package com.raylib.java.core.rcamera;

import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;

import static com.raylib.java.core.input.Gamepad.GamepadAxis.*;
import static com.raylib.java.core.input.Keyboard.*;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_MIDDLE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.*;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.*;
import static com.raylib.java.raymath.Raymath.*;

public class Camera3D {

    // rCamera projection modes
    public enum CameraProjection {
        CAMERA_PERSPECTIVE(0),
        CAMERA_ORTHOGRAPHIC(1);

        private final int value;

        CameraProjection(int value) {
            this.value = value;
        }

        public int GetValue() {
            return value;
        }
    }

    public enum CameraMode {
        CAMERA_CUSTOM(0),
        CAMERA_FREE(1),
        CAMERA_ORBITAL(2),
        CAMERA_FIRST_PERSON(3),
        CAMERA_THIRD_PERSON(4);

        private final int value;

        CameraMode(int value) {
            this.value = value;
        }

        public int GetValue() {
            return value;
        }
    }

    private final float CAMERA_CULL_DISTANCE_NEAR;
    private final float CAMERA_CULL_DISTANCE_FAR;

    private final float CAMERA_MOVE_SPEED = 5.4f;
    private final float CAMERA_ROTATION_SPEED = 0.03f;
    private final float CAMERA_PAN_SPEED = 2.0f;

    // Camera mouse movement sensitivity
    private final float CAMERA_MOUSE_MOVE_SENSITIVITY = 0.003f;
    private final float CAMERA_MOUSE_SCROLL_SENSITIVITY = 1.5f;

    private final float CAMERA_ORBITAL_SPEED = 0.5f; // Radians per second


    private final float CAMERA_FIRST_PERSON_STEP_TRIGONOMETRIC_DIVIDER = 8.0f;
    private final float CAMERA_FIRST_PERSON_STEP_DIVIDER = 30.0f;
    private final float CAMERA_FIRST_PERSON_WAVING_DIVIDER = 200.0f;

    // PLAYER (used by camera)
    private final float PLAYER_MOVEMENT_SENSITIVITY = 20.0f;

    public Vector3 position;                // rCamera position
    public Vector3 target;
    public Vector3 up;                      // rCamera up vector (rotation over its axis)
    public float fovy;                      // rCamera field-of-view aperture in Y (degrees) in perspective, used as near plane width in orthographic
    public CameraProjection projection;     // rCamera projection, defines projection: CAMERA_PERSPECTIVE or CAMERA_ORTHOGRAPHIC

    private final Raylib context;

    public Camera3D(Raylib context) {
        this.context = context;
        this.position = new Vector3();
        this.target = new Vector3();
        this.up = new Vector3();
        this.projection = CAMERA_PERSPECTIVE;

        CAMERA_CULL_DISTANCE_NEAR = Config.RL_CULL_DISTANCE_NEAR;
        CAMERA_CULL_DISTANCE_FAR = Config.RL_CULL_DISTANCE_FAR;
    }

    public Camera3D(Raylib context, Vector3 position, Vector3 target, Vector3 up, Float fovy, CameraProjection projection) {
        this.context = context;
        this.position = position;
        this.target = target;
        this.up = up;
        this.fovy = fovy;
        this.projection = projection;

        CAMERA_CULL_DISTANCE_NEAR = Config.RL_CULL_DISTANCE_NEAR;
        CAMERA_CULL_DISTANCE_FAR = Config.RL_CULL_DISTANCE_FAR;
    }

    /**
     * Returns the camera's forward vector (normalised)
     *
     * @return normalised forward vector
     */
    public Vector3 GetCameraForward() {
        return Vector3Normalize(Vector3Subtract(this.target, this.position));
    }

    /**
     * Returns the camera's up vector (normalised) <br/>
     * NOTE: The up vector might not be perpendicular to the forward vector.
     *
     * @return normalised up vector
     */
    public Vector3 GetCameraUp() {
        return Vector3Normalize(this.up);
    }

    /**
     * Returns the camera's right vector (normalised)
     *
     * @return normalised right vector
     */
    public Vector3 GetCameraRight() {
        Vector3 forward = GetCameraForward();
        Vector3 up = GetCameraUp();

        return Vector3Normalize(Vector3CrossProduct(forward, up));
    }

    /**
     * Moves the camera forward in its forward direction
     *
     * @param distance         units the camera should move
     * @param moveInWorldPlane normalises the camera's forward vector
     */
    public void MoveForward(float distance, boolean moveInWorldPlane) {
        Vector3 forward = this.GetCameraForward();

        if (moveInWorldPlane) {
            // Project vector onto world plane (the plane defined by the up vector)
            if (Math.abs(this.up.z) > 0.7071f) {
                forward.z = 0;
            }
            else if (Math.abs(this.up.x) > 0.7071f) {
                forward.x = 0;
            }
            else {
                forward.y = 0;
            }
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
     *
     * @param distance units the camera should move
     */
    public void MoveUp(float distance) {
        Vector3 up = this.GetCameraUp();

        // Scale by distance
        up = Vector3Scale(up, distance);

        // Move position and target
        this.position = Vector3Add(position, up);
        this.target = Vector3Add(target, up);
    }

    /**
     * Moves the camera right in its current right direction
     *
     * @param distance         units the camera should move
     * @param moveInWorldPlane normalises the camera's right vector
     */
    public void MoveRight(float distance, boolean moveInWorldPlane) {
        Vector3 right = this.GetCameraRight();

        if (moveInWorldPlane) {
            // Project vector onto world plane (the plane defined by the up vector)
            if (Math.abs(this.up.z) > 0.7071f) {
                right.z = 0;
            }
            else if (Math.abs(this.up.x) > 0.7071f) {
                right.x = 0;
            }
            else {
                right.y = 0;
            }

            right = Vector3Normalize(right);
        }

        // Scale by distance
        right = Vector3Scale(right, distance);

        // Move position and target
        this.position = Vector3Add(position, right);
        this.target = Vector3Add(target, right);
    }

    /**
     * Moves the camera position closer/farther to/from the camera target
     *
     * @param delta units the camera should move towards or away from the camera target
     */
    public void MoveToTarget(float delta) {
        float distance = Vector3Distance(position, target);

        // Apply delta
        distance += delta;

        // Distance must be greater than 0
        if (distance <= 0) {
            distance = 0.001f;
        }

        // Set new distance by moving the position along the forward vector
        Vector3 forward = this.GetCameraForward();
        this.position = Vector3Add(target, Vector3Scale(forward, -distance));
    }

    /**
     * Rotates the camera around its up vector <br/>
     * Yaw is "looking left and right"
     *
     * @param angle              angle of rotation in radians
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
            this.position = Vector3Subtract(target, targetPosition);
        }
        // rotate around camera.position
        else {
            // Move target relative to position
            this.target = Vector3Add(this.position, targetPosition);
        }
    }

    /**
     * Rotates the camera around its right vector<br/>
     * Pitch is "looking up or down."
     *
     * @param angle              angle of rotation in radians
     * @param lockView           prevent camera from overrotation (aka "somersaults")
     * @param rotateAroundTarget if false, the camera will rotate around its position
     * @param rotateUp           apply rotation to up direction
     */
    public void Pitch(float angle, boolean lockView, boolean rotateAroundTarget, boolean rotateUp) {
        // Up direction
        Vector3 up = this.GetCameraUp();

        // View vector
        Vector3 targetPosition = Vector3Subtract(target, position);

        if (lockView) {
            // In these camera modes, clamp the Pitch angle
            // to allow only viewing straight up or down

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
        Vector3 right = GetCameraRight();

        // Rotate view vector around right axis
        targetPosition = Vector3RotateByAxisAngle(targetPosition, right, angle);

        if (rotateAroundTarget) {
            // Move position relative to target
            this.position = Vector3Subtract(target, targetPosition);
        }
        // Rotate around camera.position
        else {
            // Move target relative to position
            this.target = Vector3Add(this.position, targetPosition);
        }

        if (rotateUp) {
            // Rotate up direction around right axis
            this.up = Vector3RotateByAxisAngle(up, right, angle);
        }
    }

    /**
     * Rotates the camera around its forward vector<br/>
     * Roll is tilting to the left or right
     *
     * @param angle angle of roll in radians
     */
    public void Roll(float angle) {
        // Rotation axis
        Vector3 forward = GetCameraForward();

        // Rotate up direction around forward axis
        this.up = Vector3RotateByAxisAngle(up, forward, angle);
    }

    /**
     * Returns the camera view matrix
     *
     * @return camera view matrix
     */
    public Matrix GetCameraViewMatrix() {
        return MatrixLookAt(position, target, up);
    }

    /**
     * Return the camera's projection matrix
     *
     * @param aspect camera's aspect ratio as a decimal (width/height)
     * @return Camera's projection matrix.
     */
    public Matrix GetCameraProjectionMatrix(float aspect) {
        if (projection == CAMERA_PERSPECTIVE) {
            return MatrixPerspective(fovy * DEG2RAD, aspect, CAMERA_CULL_DISTANCE_NEAR, CAMERA_CULL_DISTANCE_FAR);
        }
        else if (projection == CAMERA_ORTHOGRAPHIC) {
            double top = fovy / 2.0;
            double right = top * aspect;

            return MatrixOrtho(-right, right, -top, top, CAMERA_CULL_DISTANCE_NEAR, CAMERA_CULL_DISTANCE_FAR);
        }

        return MatrixIdentity();
    }

    /**
     * Update camera position for selected mode
     *
     * @param mode Camera mode
     * @see CameraMode
     */
    public void Update(CameraMode mode) {
        Vector2 mousePositionDelta = context.core.GetMouseDelta();

        boolean moveInWorldPlane = ((mode == CAMERA_FIRST_PERSON) || (mode == CAMERA_THIRD_PERSON));
        boolean rotateAroundTarget = ((mode == CAMERA_THIRD_PERSON) || (mode == CAMERA_ORBITAL));
        boolean lockView = ((mode == CAMERA_FREE) || (mode == CAMERA_FIRST_PERSON) || (mode == CAMERA_THIRD_PERSON) || (mode == CAMERA_ORBITAL));
        boolean rotateUp = false;

        // Camera speeds based on frame time
        float cameraMoveSpeed = CAMERA_MOVE_SPEED * context.core.GetFrameTime();
        float cameraRotationSpeed = CAMERA_ROTATION_SPEED * context.core.GetFrameTime();
        float cameraPanSpeed = CAMERA_PAN_SPEED * context.core.GetFrameTime();
        float cameraOrbitalSpeed = CAMERA_ORBITAL_SPEED * context.core.GetFrameTime();

        if (mode == CAMERA_CUSTOM) {

        }
        else if (mode == CAMERA_ORBITAL) {
            Matrix rotation = MatrixRotate(this.GetCameraUp(), cameraOrbitalSpeed);
            Vector3 view = Vector3Subtract(this.position, this.target);
            view = Vector3Transform(view, rotation);
            this.position = Vector3Add(this.target, view);
        }
        else {
            // Camera rotation
            if (context.core.IsKeyDown(KEY_DOWN)) {
                this.Pitch(-cameraRotationSpeed, lockView, rotateAroundTarget, rotateUp);
            }
            if (context.core.IsKeyDown(KEY_UP)) {
                this.Pitch(cameraRotationSpeed, lockView, rotateAroundTarget, rotateUp);
            }
            if (context.core.IsKeyDown(KEY_RIGHT)) {
                this.Yaw(-cameraRotationSpeed, rotateAroundTarget);
            }
            if (context.core.IsKeyDown(KEY_LEFT)) {
                this.Yaw(cameraRotationSpeed, rotateAroundTarget);
            }
            if (context.core.IsKeyDown(KEY_Q)) {
                this.Roll(-cameraRotationSpeed);
            }
            if (context.core.IsKeyDown(KEY_E)) {
                this.Roll(cameraRotationSpeed);
            }

            // Camera movement
            // Camera pan (for CAMERA_FREE)
            if ((mode == CAMERA_FREE) && (context.core.IsMouseButtonDown(MOUSE_BUTTON_MIDDLE))) {
                Vector2 mouseDelta = context.core.GetMouseDelta();
                if (mouseDelta.x > 0.0f) {
                    this.MoveRight(cameraPanSpeed, moveInWorldPlane);
                }
                if (mouseDelta.x < 0.0f) {
                    this.MoveRight(-cameraPanSpeed, moveInWorldPlane);
                }
                if (mouseDelta.y > 0.0f) {
                    this.MoveUp(-cameraPanSpeed);
                }
                if (mouseDelta.y < 0.0f) {
                    this.MoveUp(cameraPanSpeed);
                }
            }
            else {
                // Mouse support
                this.Yaw(-mousePositionDelta.x * CAMERA_MOUSE_MOVE_SENSITIVITY, rotateAroundTarget);
                this.Pitch(-mousePositionDelta.y * CAMERA_MOUSE_MOVE_SENSITIVITY, lockView, rotateAroundTarget, rotateUp);
            }

            // Keyboard support
            if (context.core.IsKeyDown(KEY_W)) {
                this.MoveForward(cameraMoveSpeed, moveInWorldPlane);
            }
            if (context.core.IsKeyDown(KEY_A)) {
                this.MoveRight(-cameraMoveSpeed, moveInWorldPlane);
            }
            if (context.core.IsKeyDown(KEY_S)) {
                this.MoveForward(-cameraMoveSpeed, moveInWorldPlane);
            }
            if (context.core.IsKeyDown(KEY_D)) {
                this.MoveRight(cameraMoveSpeed, moveInWorldPlane);
            }

            // Gamepad movement
            if (context.core.IsGamepadAvailable(0)) {
                // Gamepad controller support
                this.Yaw(-(context.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_X) * 2) * CAMERA_MOUSE_MOVE_SENSITIVITY, rotateAroundTarget);
                this.Pitch(-(context.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_Y) * 2) * CAMERA_MOUSE_MOVE_SENSITIVITY, lockView, rotateAroundTarget, rotateUp);

                if (context.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_Y) <= -0.25f) {
                    this.MoveForward(cameraMoveSpeed, moveInWorldPlane);
                }
                if (context.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_X) <= -0.25f) {
                    this.MoveRight(-cameraMoveSpeed, moveInWorldPlane);
                }
                if (context.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_Y) >= 0.25f) {
                    this.MoveForward(-cameraMoveSpeed, moveInWorldPlane);
                }
                if (context.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_X) >= 0.25f) {
                    this.MoveRight(cameraMoveSpeed, moveInWorldPlane);
                }
            }

            if (mode == CAMERA_FREE) {
                if (context.core.IsKeyDown(KEY_SPACE)) {
                    this.MoveUp(cameraMoveSpeed);
                }
                if (context.core.IsKeyDown(KEY_LEFT_CONTROL)) {
                    this.MoveUp(-cameraMoveSpeed);
                }
            }
        }

        if ((mode == CAMERA_THIRD_PERSON) || (mode == CAMERA_ORBITAL) || (mode == CAMERA_FREE)) {
            // Zoom target distance
            this.MoveToTarget(-context.core.GetMouseWheelMove());
            if (context.core.IsKeyPressed(KEY_KP_SUBTRACT)) {
                this.MoveToTarget(2.0f);
            }
            if (context.core.IsKeyPressed(KEY_KP_ADD)) {
                this.MoveToTarget(-2.0f);
            }
        }
    }

    /**
     * Update camera movement
     *
     * @param movement `Vector3` specifying units to move in the X, Y, Z axis
     * @param rotation `Vector3` specifying *DEGREES* to rotate along the X, Y, Z axis
     * @param zoom     Amount the camera should zoom
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
        this.Pitch(-rotation.y * DEG2RAD, lockView, rotateAroundTarget, rotateUp);
        this.Yaw(-rotation.x * DEG2RAD, rotateAroundTarget);
        this.Roll(rotation.z * DEG2RAD);

        // Camera movement
        this.MoveForward(movement.x, moveInWorldPlane);
        this.MoveRight(movement.y, moveInWorldPlane);
        this.MoveUp(movement.z);

        // Zoom target distance
        this.MoveToTarget(zoom);
    }
}
