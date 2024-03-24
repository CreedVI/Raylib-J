package com.raylib.java.extras.physac;

import com.raylib.java.raymath.Vector2;

import static com.raylib.java.extras.physac.Physac.PhysicsShapeType.PHYSICS_CIRCLE;
import static com.raylib.java.extras.physac.Physac.PhysicsShapeType.PHYSICS_POLYGON;

public class Physac{

    /**********************************************************************************************
     *
     *   Physac-J v1.1 - A Java port of Physac v1.1
     *
     *   DESCRIPTION:
     *   Physac-J is a Java wrapper for Physac for use in Raylib-J and for standalone implementations.
     *
     *   Physac is a small 2D physics engine written in pure C. The engine uses a fixed time-step thread loop
     *   to simulate physics. A physics step contains the following phases: get collision information,
     *   apply dynamics, collision solving and position correction. It uses a very simple struct for physic
     *   bodies with a position vector to be used in any 3D rendering API.
     *
     *   Physac is protected under the zlib/libpng licence as follows:
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2016-2021 Victor Fisac (@victorfisac) and Ramon Santamaria (@raysan5)
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     *     Physac-J was ported by @CreedVI (https://github.com/user/CreedVI)
     *
     */

    public static class PhysicsShapeType{
        public static final int
                PHYSICS_CIRCLE = 0,
                PHYSICS_POLYGON = 1;
    }

    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    static final int PHYSAC_MAX_BODIES = 64;                // Maximum number of physic bodies supported
    static final int PHYSAC_MAX_MANIFOLDS = 4096;           // Maximum number of physic bodies interactions (64x64)
    public static final int PHYSAC_MAX_VERTICES = 24;       // Maximum number of vertex for polygons shapes
    static final int PHYSAC_DEFAULT_CIRCLE_VERTICES = 24;   // Default number of vertices for circle shapes
    static final int PHYSAC_COLLISION_ITERATIONS = 100;

    static final float PHYSAC_PENETRATION_ALLOWANCE = 0.05f;
    static final float PHYSAC_PENETRATION_CORRECTION = 0.4f;
    static final float PHYSAC_PI = 3.14159265358979323846f;
    static final float PHYSAC_DEG2RAD = (PHYSAC_PI/180.0f);
    static final float PHYSAC_EPSILON = 0.000001f;
    static final float PHYSAC_K = 1.0f/3.0f;
    static final float PHYSAC_FLT_MAX = 3.402823466e+38f;

    //----------------------------------------------------------------------------------
    // Global Variables Definition
    //----------------------------------------------------------------------------------
    static double deltaTime = 1.0/60.0/10.0 * 10000;             // Delta time in milliseconds used for physics steps

    // Time measure variables
    static double baseClockTicks = 0.0;                         // Offset clock ticks for MONOTONIC clock
    static int frequency = 0;                                   // Hi-res clock frequency
    static double startTime = 0.0;                              // Start time in milliseconds
    static double currentTime = 0.0;                            // Current time in milliseconds

    // Physics system configuration
    static PhysicsBody[] bodies;                        // Physics bodies pointers array
    static int physicsBodiesCount = 0;                  // Physics world current bodies counter
    static PhysicsManifoldData[] contacts;                  // Physics bodies pointers array
    static int physicsManifoldsCount = 0;               // Physics world current manifolds counter

    static Vector2 gravityForce = new Vector2(0.0f, 9.81f);              // Physics world gravity force

    //Java specific variables
    PhysicsManifoldData PhysicsManifold;
    static boolean PHYSAC_STANDALONE = false;
    static boolean PHYSAC_DEBUG = false;

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    // Initializes physics values, pointers and creates physics loop thread
    public void InitPhysics() {
        PhysicsManifold = new PhysicsManifoldData();
        bodies = new PhysicsBody[PHYSAC_MAX_BODIES];
        contacts = new PhysicsManifoldData[PHYSAC_MAX_MANIFOLDS];

        if (PHYSAC_STANDALONE){
            InitTimerHiRes();
        }

        System.out.println("[PHYSAC] Physics module initialized successfully");
    }

    // Sets physics global gravity force
    public void SetPhysicsGravity(float x, float y) {
        gravityForce.x = x;
        gravityForce.y = y;
    }

    // Creates a new circle physics body with generic parameters
    public PhysicsBody CreatePhysicsBodyCircle(Vector2 pos, float radius, float density) {
        return CreatePhysicsBodyPolygon(pos, radius, PHYSAC_DEFAULT_CIRCLE_VERTICES, density);
    }

    // Creates a new rectangle physics body with generic parameters
    public PhysicsBody CreatePhysicsBodyRectangle(Vector2 pos, float width, float height, float density) {
        // NOTE: Make sure body data is initialized to 0
        PhysicsBody body = new PhysicsBody();

        int id = FindAvailableBodyIndex();
        if (id != -1) {
            // Initialize new body with generic values
            body.id = id;
            body.enabled = true;
            body.position = pos;
            body.shape.type = PHYSICS_POLYGON;
            body.shape.body = body;
            body.shape.transform = MathMatFromRadians(0.0f);
            body.shape.vertexData = CreateRectanglePolygon(pos, new Vector2(width, height));

            // Calculate centroid and moment of inertia
            Vector2 center = new Vector2();
            float area = 0.0f;
            float inertia = 0.0f;

            for (int i = 0; i < body.shape.vertexData.vertexCount; i++) {
                // Triangle vertices, third vertex implied as (0, 0)
                Vector2 p1 = body.shape.vertexData.positions[i];
                int nextIndex = (((i + 1) < body.shape.vertexData.vertexCount) ? (i + 1) : 0);
                Vector2 p2 = body.shape.vertexData.positions[nextIndex];

                float D = MathVector2CrossProduct(p1, p2);
                float triangleArea = D/2;

                area += triangleArea;

                // Use area to weight the centroid average, not just vertex position
                center.x += triangleArea*PHYSAC_K*(p1.x + p2.x);
                center.y += triangleArea*PHYSAC_K*(p1.y + p2.y);

                float intx2 = p1.x*p1.x + p2.x*p1.x + p2.x*p2.x;
                float inty2 = p1.y*p1.y + p2.y*p1.y + p2.y*p2.y;
                inertia += (0.25f*PHYSAC_K*D)*(intx2 + inty2);
            }

            center.x *= 1.0f/area;
            center.y *= 1.0f/area;

            // Translate vertices to centroid (make the centroid (0, 0) for the polygon in model space)
            // Note: this is not really necessary
            for (int i = 0; i < body.shape.vertexData.vertexCount; i++) {
                body.shape.vertexData.positions[i].x -= center.x;
                body.shape.vertexData.positions[i].y -= center.y;
            }

            body.mass = density*area;
            body.inverseMass = ((body.mass != 0.0f) ? 1.0f/body.mass : 0.0f);
            body.inertia = density*inertia;
            body.inverseInertia = ((body.inertia != 0.0f) ? 1.0f/body.inertia : 0.0f);
            body.staticFriction = 0.4f;
            body.dynamicFriction = 0.2f;
            body.restitution = 0.0f;
            body.useGravity = true;
            body.isGrounded = false;
            body.freezeOrient = false;

            // Add new body to bodies pointers array and update bodies count
            bodies[physicsBodiesCount] = body;
            physicsBodiesCount++;

            Tracelog("[PHYSAC] Physic body created successfully (id: " + body.id + ")");
        }
        else{
            Tracelog("[PHYSAC] Physic body could not be created, PHYSAC_MAX_BODIES reached");
        }

        return body;
    }

    // Creates a new polygon physics body with generic parameters
    public PhysicsBody CreatePhysicsBodyPolygon(Vector2 pos, float radius, int sides, float density) {
        PhysicsBody body = new PhysicsBody();

        int id = FindAvailableBodyIndex();
        if (id != -1) {
            // Initialize new body with generic values
            body.id = id;
            body.enabled = true;
            body.position = pos;
            body.velocity = new Vector2();
            body.force = new Vector2();
            body.angularVelocity = 0.0f;
            body.torque = 0.0f;
            body.orient = 0.0f;
            body.shape.type = PHYSICS_POLYGON;
            body.shape.body = body;
            body.shape.transform = MathMatFromRadians(0.0f);
            body.shape.vertexData = CreateDefaultPolygon(radius, sides);

            // Calculate centroid and moment of inertia
            Vector2 center = new Vector2();
            float area = 0.0f;
            float inertia = 0.0f;

            for (int i = 0; i < body.shape.vertexData.vertexCount; i++) {
                // Triangle vertices, third vertex implied as (0, 0)
                Vector2 position1 = body.shape.vertexData.positions[i];
                int nextIndex = (((i + 1) < body.shape.vertexData.vertexCount) ? (i + 1) : 0);
                Vector2 position2 = body.shape.vertexData.positions[nextIndex];

                float cross = MathVector2CrossProduct(position1, position2);
                float triangleArea = cross/2;

                area += triangleArea;

                // Use area to weight the centroid average, not just vertex position
                center.x += triangleArea*PHYSAC_K*(position1.x + position2.x);
                center.y += triangleArea*PHYSAC_K*(position1.y + position2.y);

                float intx2 = position1.x*position1.x + position2.x*position1.x + position2.x*position2.x;
                float inty2 = position1.y*position1.y + position2.y*position1.y + position2.y*position2.y;
                inertia += (0.25f*PHYSAC_K*cross)*(intx2 + inty2);
            }

            center.x *= 1.0f/area;
            center.y *= 1.0f/area;

            // Translate vertices to centroid (make the centroid (0, 0) for the polygon in model space)
            // Note: this is not really necessary
            for (int i = 0; i < body.shape.vertexData.vertexCount; i++) {
                body.shape.vertexData.positions[i].x -= center.x;
                body.shape.vertexData.positions[i].y -= center.y;
            }

            body.mass = density*area;
            body.inverseMass = ((body.mass != 0.0f) ? 1.0f/body.mass : 0.0f);
            body.inertia = density*inertia;
            body.inverseInertia = ((body.inertia != 0.0f) ? 1.0f/body.inertia : 0.0f);
            body.staticFriction = 0.4f;
            body.dynamicFriction = 0.2f;
            body.restitution = 0.0f;
            body.useGravity = true;
            body.isGrounded = false;
            body.freezeOrient = false;

            // Add new body to bodies pointers array and update bodies count
            bodies[physicsBodiesCount] = body;
            physicsBodiesCount++;

            Tracelog("[PHYSAC] Physic body created successfully (id: " + body.id + ")");
        }
        else {
            Tracelog("[PHYSAC] Physics body could not be created, PHYSAC_MAX_BODIES reached");
        }

        return body;
    }

    // Adds a force to a physics body
    public void PhysicsAddForce(PhysicsBody body, Vector2 force) {
        if (body != null) {
            body.force = MathVector2Add(body.force, force);
        }
    }

    // Adds an angular force to a physics body
    public void PhysicsAddTorque(PhysicsBody body, float amount) {
        if (body != null) {
            body.torque += amount;
        }
    }

    // Shatters a polygon shape physics body to little physics bodies with explosion force
    public void PhysicsShatter(PhysicsBody body, Vector2 position, float force) {
        if (body != null) {
            if (body.shape.type == PHYSICS_POLYGON) {
                PhysicsVertexData vertexData = body.shape.vertexData;
                boolean collision = false;

                for (int i = 0; i < vertexData.vertexCount; i++) {
                    Vector2 positionA = body.position;
                    Vector2 positionB = MathMatVector2Product(body.shape.transform, MathVector2Add(body.position, vertexData.positions[i]));
                    int nextIndex = (((i + 1) < vertexData.vertexCount) ? (i + 1) : 0);
                    Vector2 positionC = MathMatVector2Product(body.shape.transform, MathVector2Add(body.position, vertexData.positions[nextIndex]));

                    // Check collision between each triangle
                    float alpha = ((positionB.y - positionC.y)*(position.x - positionC.x) + (positionC.x - positionB.x)*(position.y - positionC.y))/
                            ((positionB.y - positionC.y)*(positionA.x - positionC.x) + (positionC.x - positionB.x)*(positionA.y - positionC.y));

                    float beta = ((positionC.y - positionA.y)*(position.x - positionC.x) + (positionA.x - positionC.x)*(position.y - positionC.y))/
                            ((positionB.y - positionC.y)*(positionA.x - positionC.x) + (positionC.x - positionB.x)*(positionA.y - positionC.y));

                    float gamma = 1.0f - alpha - beta;

                    if ((alpha > 0.0f) && (beta > 0.0f) & (gamma > 0.0f))
                    {
                        collision = true;
                        break;
                    }
                }

                if (collision)
                {
                    int count = vertexData.vertexCount;
                    Vector2 bodyPos = body.position;
                    Vector2[] vertices = new Vector2[count];
                    Matrix2x2 trans = body.shape.transform;
                    for (int i = 0; i < count; i++) {
                        vertices[i] = vertexData.positions[i];
                    }

                    // Destroy shattered physics body
                    DestroyPhysicsBody(body);

                    for (int i = 0; i < count; i++) {
                        int nextIndex = (((i + 1) < count) ? (i + 1) : 0);
                        Vector2 center = MathTriangleBarycenter(vertices[i], vertices[nextIndex], new Vector2());
                        center = MathVector2Add(bodyPos, center);
                        Vector2 offset = MathVector2Subtract(center, bodyPos);

                        body = CreatePhysicsBodyPolygon(center, 10, 3, 10);     // Create polygon physics body with relevant values

                        vertexData = new PhysicsVertexData();
                        vertexData.vertexCount = 3;

                        vertexData.positions[0] = MathVector2Subtract(vertices[i], offset);
                        vertexData.positions[1] = MathVector2Subtract(vertices[nextIndex], offset);
                        vertexData.positions[2] = MathVector2Subtract(position, center);

                        // Separate vertices to avoid unnecessary physics collisions
                        vertexData.positions[0].x *= 0.95f;
                        vertexData.positions[0].y *= 0.95f;
                        vertexData.positions[1].x *= 0.95f;
                        vertexData.positions[1].y *= 0.95f;
                        vertexData.positions[2].x *= 0.95f;
                        vertexData.positions[2].y *= 0.95f;

                        // Calculate polygon faces normals
                        for (int j = 0; j < vertexData.vertexCount; j++) {
                            int nextVertex = (((j + 1) < vertexData.vertexCount) ? (j + 1) : 0);
                            Vector2 face = MathVector2Subtract(vertexData.positions[nextVertex], vertexData.positions[j]);

                            vertexData.normals[j] = MathVector2Normalize(new Vector2(face.y, -face.x));
                        }

                        // Apply computed vertex data to new physics body shape
                        body.shape.vertexData = vertexData;
                        body.shape.transform = trans;

                        // Calculate centroid and moment of inertia
                        center = new Vector2();
                        float area = 0.0f;
                        float inertia = 0.0f;

                        for (int j = 0; j < body.shape.vertexData.vertexCount; j++) {
                            // Triangle vertices, third vertex implied as (0, 0)
                            Vector2 p1 = body.shape.vertexData.positions[j];
                            int nextVertex = (((j + 1) < body.shape.vertexData.vertexCount) ? (j + 1) : 0);
                            Vector2 p2 = body.shape.vertexData.positions[nextVertex];

                            float D = MathVector2CrossProduct(p1, p2);
                            float triangleArea = D/2;

                            area += triangleArea;

                            // Use area to weight the centroid average, not just vertex position
                            center.x += triangleArea*PHYSAC_K*(p1.x + p2.x);
                            center.y += triangleArea*PHYSAC_K*(p1.y + p2.y);

                            float intx2 = p1.x*p1.x + p2.x*p1.x + p2.x*p2.x;
                            float inty2 = p1.y*p1.y + p2.y*p1.y + p2.y*p2.y;
                            inertia += (0.25f*PHYSAC_K*D)*(intx2 + inty2);
                        }

                        center.x *= 1.0f/area;
                        center.y *= 1.0f/area;

                        body.mass = area;
                        body.inverseMass = ((body.mass != 0.0f) ? 1.0f/body.mass : 0.0f);
                        body.inertia = inertia;
                        body.inverseInertia = ((body.inertia != 0.0f) ? 1.0f/body.inertia : 0.0f);

                        // Calculate explosion force direction
                        Vector2 pointA = body.position;
                        Vector2 pointB = MathVector2Subtract(vertexData.positions[1], vertexData.positions[0]);
                        pointB.x /= 2.0f;
                        pointB.y /= 2.0f;
                        Vector2 forceDirection = MathVector2Subtract(MathVector2Add(pointA, MathVector2Add(vertexData.positions[0], pointB)), body.position);
                        forceDirection = MathVector2Normalize(forceDirection);
                        forceDirection.x *= force;
                        forceDirection.y *= force;

                        // Apply force to new physics body
                        PhysicsAddForce(body, forceDirection);
                    }

                    vertices = null;
                }
            }
        }
        else {
            Tracelog("[PHYSAC] WARNING: PhysicsShatter: null physic body");
        }
    }

    // Returns the current amount of created physics bodies
    public int GetPhysicsBodiesCount() {
        return physicsBodiesCount;
    }

    // Returns a physics body of the bodies pool at a specific index
    public PhysicsBody GetPhysicsBody(int index) {
        PhysicsBody body = null;

        if (index < physicsBodiesCount) {
            body = bodies[index];

            if (body == null){
                Tracelog("[PHYSAC] WARNING: GetPhysicsBody: null physic body");
            }
        }
        else {
            Tracelog("[PHYSAC] WARNING: Physic body index is out of bounds");
        }

        return body;
    }

    // Returns the physics body shape type (PHYSICS_CIRCLE or PHYSICS_POLYGON)
    public int GetPhysicsShapeType(int index) {
        int result = -1;

        if (index < physicsBodiesCount) {
            PhysicsBody body = bodies[index];

            if (body != null){
                result = body.shape.type;
            }
            else {
                Tracelog("[PHYSAC] WARNING: GetPhysicsShapeType: null physic body");
            }
        }
        else {
            Tracelog("[PHYSAC] WARNING: Physic body index is out of bounds");
        }

        return result;
    }

    // Returns the amount of vertices of a physics body shape
    public int GetPhysicsShapeVerticesCount(int index) {
        int result = 0;

        if (index < physicsBodiesCount) {
            PhysicsBody body = bodies[index];

            if (body != null) {
                switch (body.shape.type) {
                    case PHYSICS_CIRCLE:
                        result = PHYSAC_DEFAULT_CIRCLE_VERTICES;
                        break;
                    case PHYSICS_POLYGON:
                        result = body.shape.vertexData.vertexCount;
                        break;
                    default:
                        break;
                }
            }
            else {
                Tracelog("[PHYSAC] WARNING: GetPhysicsShapeVerticesCount: null physic body");
            }
        }
        else {
            Tracelog("[PHYSAC] WARNING: Physic body index is out of bounds");
        }

        return result;
    }

    // Returns transformed position of a body shape (body position + vertex transformed position)
    public Vector2 GetPhysicsShapeVertex(PhysicsBody body, int vertex)
    {
        Vector2 position = new Vector2();

        if (body != null) {
            switch (body.shape.type) {
                case PHYSICS_CIRCLE: {
                    position.x = (float) (body.position.x + Math.cos(360.0f/PHYSAC_DEFAULT_CIRCLE_VERTICES*vertex*PHYSAC_DEG2RAD) *
                            body.shape.radius);
                    position.y = (float) (body.position.y + Math.sin(360.0f/PHYSAC_DEFAULT_CIRCLE_VERTICES * vertex * PHYSAC_DEG2RAD) *
                            body.shape.radius);
                } break;
                case PHYSICS_POLYGON: {
                    PhysicsVertexData vertexData = body.shape.vertexData;
                    position = MathVector2Add(body.position, MathMatVector2Product(body.shape.transform, vertexData.positions[vertex]));
                } break;
                default:
                    break;
            }
        }
        else{
            Tracelog("[PHYSAC] WARNING: GetPhysicsShapeVertex: null physic body");
        }

        return position;
    }

    // Sets physics body shape transform based on radians parameter
    public void SetPhysicsBodyRotation(PhysicsBody body, float radians) {
        if (body != null) {
            body.orient = radians;

            if (body.shape.type == PHYSICS_POLYGON) {
                body.shape.transform = MathMatFromRadians(radians);
            }
        }
    }

    // Unitializes and destroys a physics body
    public void DestroyPhysicsBody(PhysicsBody body) {
        if (body != null) {
            long id = body.id;
            int index = -1;

            for (int i = 0; i < physicsBodiesCount; i++) {
                if (bodies[i].id == id) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                Tracelog("[PHYSAC] WARNING: Requested body (id: " + id + ") can not be found");
                return;     // Prevent access to index -1
            }

            // Free body allocated memory
            bodies[index] = null;

            // Reorder physics bodies pointers array and its catched index
            for (int i = index; i < physicsBodiesCount; i++) {
                if ((i + 1) < physicsBodiesCount) bodies[i] = bodies[i + 1];
            }

            // Update physics bodies count
            physicsBodiesCount--;

            Tracelog("[PHYSAC] Physic body destroyed successfully (id: " + id + ")");
        }
        else {
            Tracelog("[PHYSAC] WARNING: DestroyPhysicsBody: null physic body");
        }
    }

    // Destroys created physics bodies and manifolds and resets global values
    public void ResetPhysics() {
        if (physicsBodiesCount > 0) {
            // Unitialize physics bodies dynamic memory allocations
            for (int i = physicsBodiesCount - 1; i >= 0; i--) {
                PhysicsBody body = bodies[i];

                if (body != null) {
                    bodies[i] = null;
                }
            }

            physicsBodiesCount = 0;
        }

        if (physicsManifoldsCount > 0) {
            // Unitialize physics manifolds dynamic memory allocations
            for (int i = physicsManifoldsCount - 1; i >= 0; i--) {
                PhysicsManifoldData manifold = contacts[i];

                if (manifold != null) {
                    contacts[i] = null;
                }
            }

            physicsManifoldsCount = 0;
        }

        Tracelog("[PHYSAC] Physics module reset successfully");
    }

    // Unitializes physics and exits physics loop
    public void ClosePhysics() {
        // Unitialize physics manifolds dynamic memory allocations
        if (physicsManifoldsCount > 0) {
            for (int i = physicsManifoldsCount - 1; i >= 0; i--) {
                DestroyPhysicsManifold(contacts[i]);
            }
        }

        // Unitialize physics bodies dynamic memory allocations
        if (physicsBodiesCount > 0) {
            for (int i = physicsBodiesCount - 1; i >= 0; i--) {
                DestroyPhysicsBody(bodies[i]);
            }
        }

        // Trace log info
        if (physicsBodiesCount > 0) {
            System.out.println("[PHYSAC] WARNING: Physics module closed with unallocated bodies (BODIES: " + physicsBodiesCount + ")");
        }
        else if (physicsManifoldsCount > 0) {
            System.out.println("[PHYSAC] WARNING: Pysics module closed with unallocated manifolds (MANIFOLDS: " + physicsManifoldsCount + ")");
        }
        else {
            System.out.println("[PHYSAC] Physics module closed successfully");
        }
    }

    // Update physics system
    // Physics steps are launched at a fixed time step if enabled
    public void UpdatePhysics() {
        if(PHYSAC_STANDALONE){
            double deltaTimeAccumulator = 0.0;

            // Calculate current time (ms)
            currentTime = GetCurrentTime();

            // Calculate current delta time (ms)
            double delta = currentTime - startTime;

            // Store the time elapsed since the last frame began
            deltaTimeAccumulator += delta;

            // Fixed time stepping loop
            while (deltaTimeAccumulator >= deltaTime)
            {
                UpdatePhysicsStep();
                deltaTimeAccumulator -= deltaTime;
            }

            // Record the starting of this frame
            startTime = currentTime;
        }
        else{
            UpdatePhysicsStep();
        }
    }

    public void SetPhysicsTimeStep(double delta) {
        deltaTime = delta;
    }

    //Begin PHYSAC_STANDALONE

    // Initializes hi-resolution MONOTONIC timer
    protected void InitTimerHiRes() {
        long i1 = System.nanoTime();
        long i2 = System.nanoTime();
        frequency = Math.toIntExact((i2 - i1));

        baseClockTicks = (double)GetClockTicks();      // Get MONOTONIC clock time offset
        startTime = GetCurrentTime();                  // Get current time in milliseconds
    }

    // Get hi-res MONOTONIC time measure in clock ticks
    private long GetClockTicks(){
        return System.nanoTime();
    }

    // Get current time in milliseconds
    private double GetCurrentTime() {
        return (GetClockTicks() - baseClockTicks)/frequency*1000;
    }
    // end PHYSAC_STANDALONE

    // Update physics step (dynamics, collisions and position corrections)
    public void UpdatePhysicsStep() {
        // Clear previous generated collisions information
        for (int i = physicsManifoldsCount - 1; i >= 0; i--) {
            PhysicsManifoldData manifold = contacts[i];
            if (manifold != null) {
                DestroyPhysicsManifold(manifold);
            }
        }

        // Reset physics bodies grounded state
        for (int i = 0; i < physicsBodiesCount; i++) {
            PhysicsBody body = bodies[i];
            body.isGrounded = false;
        }

        // Generate new collision information
        for (int i = 0; i < physicsBodiesCount; i++) {
            PhysicsBody bodyA = bodies[i];

            if (bodyA != null) {
                for (int j = i + 1; j < physicsBodiesCount; j++) {
                    PhysicsBody bodyB = bodies[j];

                    if (bodyB != null) {
                        if ((bodyA.inverseMass == 0) && (bodyB.inverseMass == 0)) {
                            continue;
                        }

                        PhysicsManifoldData manifold = CreatePhysicsManifold(bodyA, bodyB);
                        SolvePhysicsManifold(manifold);

                        if (manifold.contactsCount > 0) {
                            // Create a new manifold with same information as previously solved manifold and add it to the manifolds pool last slot
                            PhysicsManifoldData manifold1 = CreatePhysicsManifold(bodyA, bodyB);
                            manifold1.penetration = manifold.penetration;
                            manifold1.normal = manifold.normal;
                            manifold1.contacts[0] = manifold.contacts[0];
                            manifold1.contacts[1] = manifold.contacts[1];
                            manifold1.contactsCount = manifold.contactsCount;
                            manifold1.restitution = manifold.restitution;
                            manifold1.dynamicFriction = manifold.dynamicFriction;
                            manifold1.staticFriction = manifold.staticFriction;
                        }
                    }
                }
            }
        }

        // Integrate forces to physics bodies
        for (int i = 0; i < physicsBodiesCount; i++) {
            PhysicsBody body = bodies[i];
            if (body != null) {
                IntegratePhysicsForces(body);
            }
        }

        // Initialize physics manifolds to solve collisions
        for (int i = 0; i < physicsManifoldsCount; i++)
        {
            PhysicsManifoldData manifold = contacts[i];
            if (manifold != null) {
                InitializePhysicsManifolds(manifold);
            }
        }

        // Integrate physics collisions impulses to solve collisions
        for (int i = 0; i < PHYSAC_COLLISION_ITERATIONS; i++)
        {
            for (int j = 0; j < physicsManifoldsCount; j++)
            {
                PhysicsManifoldData manifold = contacts[i];
                if (manifold != null) {
                    IntegratePhysicsImpulses(manifold);
                }
            }
        }

        // Integrate velocity to physics bodies
        for (int i = 0; i < physicsBodiesCount; i++)
        {
            PhysicsBody body = bodies[i];
            if (body != null) {
                IntegratePhysicsVelocity(body);
            }
        }

        // Correct physics bodies positions based on manifolds collision information
        for (int i = 0; i < physicsManifoldsCount; i++)
        {
            PhysicsManifoldData manifold = contacts[i];
            if (manifold != null) {
                CorrectPhysicsPositions(manifold);
            }
        }

        // Clear physics bodies forces
        for (int i = 0; i < physicsBodiesCount; i++) {
            PhysicsBody body = bodies[i];
            if (body != null) {
                body.force = new Vector2();
                body.torque = 0.0f;
            }
        }
    }

    // Finds a valid index for a new physics body initialization
    public int FindAvailableBodyIndex() {
        int index = -1;
        for (int i = 0; i < PHYSAC_MAX_BODIES; i++) {
            int currentId = i;

            // Check if current id already exist in other physics body
            for (int k = 0; k < physicsBodiesCount; k++) {
                if (bodies[k].id == currentId) {
                    currentId++;
                    break;
                }
            }

            // If it is not used, use it as new physics body id
            if (currentId == i) {
                index = i;
                break;
            }
        }

        return index;
    }

    // Creates a default polygon shape with max vertex distance from polygon pivot
    public PhysicsVertexData CreateDefaultPolygon(float radius, int sides)
    {
        PhysicsVertexData data = new PhysicsVertexData();
        data.vertexCount = sides;

        // Calculate polygon vertices positions
        for (int i = 0; i < data.vertexCount; i++) {
            data.positions[i].x = (float)Math.cos(360.0f/sides*i*PHYSAC_DEG2RAD)*radius;
            data.positions[i].y = (float)Math.sin(360.0f/sides*i*PHYSAC_DEG2RAD)*radius;
        }

        // Calculate polygon faces normals
        for (int i = 0; i < data.vertexCount; i++) {
            int nextIndex = (((i + 1) < sides) ? (i + 1) : 0);
            Vector2 face = MathVector2Subtract(data.positions[nextIndex], data.positions[i]);

            data.normals[i] = new Vector2(face.y, -face.x);
            data.normals[i] = MathVector2Normalize(data.normals[i]);
        }

        return data;
    }

    // Creates a rectangle polygon shape based on a min and max positions
    PhysicsVertexData CreateRectanglePolygon(Vector2 pos, Vector2 size) {
        PhysicsVertexData data = new PhysicsVertexData();
        data.vertexCount = 4;

        // Calculate polygon vertices positions
        data.positions[0] = new Vector2( pos.x + size.x/2, pos.y - size.y/2);
        data.positions[1] = new Vector2( pos.x + size.x/2, pos.y + size.y/2);
        data.positions[2] = new Vector2( pos.x - size.x/2, pos.y + size.y/2);
        data.positions[3] = new Vector2( pos.x - size.x/2, pos.y - size.y/2);

        // Calculate polygon faces normals
        for (int i = 0; i < data.vertexCount; i++) {
            int nextIndex = (((i + 1) < data.vertexCount) ? (i + 1) : 0);
            Vector2 face = MathVector2Subtract(data.positions[nextIndex], data.positions[i]);

            data.normals[i] = new Vector2( face.y, -face.x);
            data.normals[i] = MathVector2Normalize(data.normals[i]);
        }

        return data;
    }

    // Finds a valid index for a new manifold initialization
    int FindAvailableManifoldIndex() {
        int index = -1;
        for (int i = 0; i < PHYSAC_MAX_MANIFOLDS; i++) {
            int currentId = i;

            // Check if current id already exist in other physics body
            for (int k = 0; k < physicsManifoldsCount; k++) {
                if (contacts[k].id == currentId) {
                    currentId++;
                    break;
                }
            }

            // If it is not used, use it as new physics body id
            if (currentId == i) {
                index = i;
                break;
            }
        }

        return index;
    }

    // Creates a new physics manifold to solve collision
    PhysicsManifoldData CreatePhysicsManifold(PhysicsBody a, PhysicsBody b) {
        PhysicsManifoldData manifold = new PhysicsManifoldData();

        int id = FindAvailableManifoldIndex();
        if (id != -1) {
            // Initialize new manifold with generic values
            manifold.id = id;
            manifold.bodyA = a;
            manifold.bodyB = b;
            manifold.penetration = 0;
            manifold.normal = new Vector2();
            manifold.contacts[0] = new Vector2();
            manifold.contacts[1] = new Vector2();
            manifold.contactsCount = 0;
            manifold.restitution = 0.0f;
            manifold.dynamicFriction = 0.0f;
            manifold.staticFriction = 0.0f;

            // Add new body to bodies pointers array and update bodies count
            contacts[physicsManifoldsCount] = manifold;
            physicsManifoldsCount++;
        }
        else {
            Tracelog("[PHYSAC] Physic manifold could not be created, PHYSAC_MAX_MANIFOLDS reached");
        }

        return manifold;
    }

    // Unitializes and destroys a physics manifold
    public void DestroyPhysicsManifold(PhysicsManifoldData manifold) {
        if (manifold != null) {
            long id = manifold.id;
            int index = -1;

            for (int i = 0; i < physicsManifoldsCount; i++) {
                if (contacts[i].id == id) {
                    index = i;
                    break;
                }
            }

            if (index == -1){
                return;     // Prevent access to index -1
            }

            // Free manifold allocated memory
            contacts[index] = null;

            // Reorder physics manifolds pointers array and its catched index
            for (int i = index; i < physicsManifoldsCount; i++)
            {
                if ((i + 1) < physicsManifoldsCount) contacts[i] = contacts[i + 1];
            }

            // Update physics manifolds count
            physicsManifoldsCount--;
        }
        else {
            Tracelog("[PHYSAC] WARNING: DestroyPhysicsManifold: null physic manifold");
        }
    }

    // Solves a created physics manifold between two physics bodies
    void SolvePhysicsManifold(PhysicsManifoldData manifold) {
        switch (manifold.bodyA.shape.type) {
            case PHYSICS_CIRCLE: {
                switch (manifold.bodyB.shape.type) {
                    case PHYSICS_CIRCLE:
                        SolveCircleToCircle(manifold);
                        break;
                    case PHYSICS_POLYGON:
                        SolveCircleToPolygon(manifold);
                        break;
                    default: break;
                }
            } break;
            case PHYSICS_POLYGON: {
                switch (manifold.bodyB.shape.type) {
                    case PHYSICS_CIRCLE:
                        SolvePolygonToCircle(manifold);
                        break;
                    case PHYSICS_POLYGON:
                        SolvePolygonToPolygon(manifold);
                        break;
                    default:
                        break;
                }
            } break;
            default:
                break;
        }

        // Update physics body grounded state if normal direction is down and grounded state is not set yet in previous manifolds
        if (!manifold.bodyB.isGrounded) {
            manifold.bodyB.isGrounded = (manifold.normal.y < 0);
        }
    }

    // Solves collision between two circle shape physics bodies
    void SolveCircleToCircle(PhysicsManifoldData manifold) {
        PhysicsBody bodyA = manifold.bodyA;
        PhysicsBody bodyB = manifold.bodyB;

        if ((bodyA == null) || (bodyB == null)){
            return;
        }

        // Calculate translational vector, which is normal
        Vector2 normal = MathVector2Subtract(bodyB.position, bodyA.position);

        float distSqr = MathVector2SqrLen(normal);
        float radius = bodyA.shape.radius + bodyB.shape.radius;

        // Check if circles are not in contact
        if (distSqr >= radius*radius) {
            manifold.contactsCount = 0;
            return;
        }

        float distance = (float) Math.sqrt(distSqr);
        manifold.contactsCount = 1;

        if (distance == 0.0f)
        {
            manifold.penetration = bodyA.shape.radius;
            manifold.normal = new Vector2(1,0);
            manifold.contacts[0] = bodyA.position;
        }
        else
        {
            manifold.penetration = radius - distance;
            manifold.normal = new Vector2(normal.x/distance, normal.y/distance); // Faster than using MathVector2Normalize()
            // due to sqrt is already performed
            manifold.contacts[0] = new Vector2(manifold.normal.x*bodyA.shape.radius + bodyA.position.x, manifold.normal.y*bodyA.shape.radius + bodyA.position.y);
        }

        // Update physics body grounded state if normal direction is down
        if (!bodyA.isGrounded) {
            bodyA.isGrounded = (manifold.normal.y < 0);
        }
    }

    // Solves collision between a circle to a polygon shape physics bodies
    void SolveCircleToPolygon(PhysicsManifoldData manifold) {
        PhysicsBody bodyA = manifold.bodyA;
        PhysicsBody bodyB = manifold.bodyB;

        if ((bodyA == null) || (bodyB == null)) return;

        manifold.contactsCount = 0;

        // Transform circle center to polygon transform space
        Vector2 center = bodyA.position;
        center = MathMatVector2Product(MathMatTranspose(bodyB.shape.transform), MathVector2Subtract(center, bodyB.position));

        // Find edge with minimum penetration
        // It is the same concept as using support points in SolvePolygonToPolygon
        float separation = -PHYSAC_FLT_MAX;
        int faceNormal = 0;
        PhysicsVertexData vertexData = bodyB.shape.vertexData;

        for (int i = 0; i < vertexData.vertexCount; i++) {
            float currentSeparation = MathVector2DotProduct(vertexData.normals[i], MathVector2Subtract(center, vertexData.positions[i]));

            if (currentSeparation > bodyA.shape.radius) {
                return;
            }

            if (currentSeparation > separation) {
                separation = currentSeparation;
                faceNormal = i;
            }
        }

        // Grab face's vertices
        Vector2 v1 = vertexData.positions[faceNormal];
        int nextIndex = (((faceNormal + 1) < vertexData.vertexCount) ? (faceNormal + 1) : 0);
        Vector2 v2 = vertexData.positions[nextIndex];

        // Check to see if center is within polygon
        if (separation < PHYSAC_EPSILON) {
            manifold.contactsCount = 1;
            Vector2 normal = MathMatVector2Product(bodyB.shape.transform, vertexData.normals[faceNormal]);
            manifold.normal = new Vector2(-normal.x, -normal.y);
            manifold.contacts[0] = new Vector2(manifold.normal.x*bodyA.shape.radius + bodyA.position.x, manifold.normal.y*bodyA.shape.radius + bodyA.position.y);
            manifold.penetration = bodyA.shape.radius;
            return;
        }

        // Determine which voronoi region of the edge center of circle lies within
        float dot1 = MathVector2DotProduct(MathVector2Subtract(center, v1), MathVector2Subtract(v2, v1));
        float dot2 = MathVector2DotProduct(MathVector2Subtract(center, v2), MathVector2Subtract(v1, v2));
        manifold.penetration = bodyA.shape.radius - separation;

        if (dot1 <= 0.0f) { // Closest to v1

            if (MathVector2SqrDistance(center, v1) > bodyA.shape.radius*bodyA.shape.radius) {
                return;
            }

            manifold.contactsCount = 1;
            Vector2 normal = MathVector2Subtract(v1, center);
            normal = MathMatVector2Product(bodyB.shape.transform, normal);
            normal = MathVector2Normalize(normal);
            manifold.normal = normal;
            v1 = MathMatVector2Product(bodyB.shape.transform, v1);
            v1 = MathVector2Add(v1, bodyB.position);
            manifold.contacts[0] = v1;
        }
        else if (dot2 <= 0.0f) { // Closest to v2
            if (MathVector2SqrDistance(center, v2) > bodyA.shape.radius*bodyA.shape.radius) {
                return;
            }

            manifold.contactsCount = 1;
            Vector2 normal = MathVector2Subtract(v2, center);
            v2 = MathMatVector2Product(bodyB.shape.transform, v2);
            v2 = MathVector2Add(v2, bodyB.position);
            manifold.contacts[0] = v2;
            normal = MathMatVector2Product(bodyB.shape.transform, normal);
            normal =MathVector2Normalize(normal);
            manifold.normal = normal;
        }
        else { // Closest to face
            Vector2 normal = vertexData.normals[faceNormal];

            if (MathVector2DotProduct(MathVector2Subtract(center, v1), normal) > bodyA.shape.radius) return;

            normal = MathMatVector2Product(bodyB.shape.transform, normal);
            manifold.normal = new Vector2(-normal.x, -normal.y);
            manifold.contacts[0] = new Vector2(manifold.normal.x*bodyA.shape.radius + bodyA.position.x, manifold.normal.y*bodyA.shape.radius + bodyA.position.y);
            manifold.contactsCount = 1;
        }
    }

    // Solves collision between a polygon to a circle shape physics bodies
    void SolvePolygonToCircle(PhysicsManifoldData manifold) {
        PhysicsBody bodyA = manifold.bodyA;
        PhysicsBody bodyB = manifold.bodyB;

        if ((bodyA == null) || (bodyB == null)) {
            return;
        }

        manifold.bodyA = bodyB;
        manifold.bodyB = bodyA;
        SolveCircleToPolygon(manifold);

        manifold.normal.x *= -1.0f;
        manifold.normal.y *= -1.0f;
    }

    // Solves collision between two polygons shape physics bodies
    void SolvePolygonToPolygon(PhysicsManifoldData manifold) {
        if ((manifold.bodyA == null) || (manifold.bodyB == null)) {
            return;
        }

        PhysicsShape bodyA = manifold.bodyA.shape;
        PhysicsShape bodyB = manifold.bodyB.shape;
        manifold.contactsCount = 0;

        // Check for separating axis with A shape's face planes
        int faceA = 0;
        float[] penetrationA = FindAxisLeastPenetration(bodyA, bodyB);
        faceA = (int) penetrationA[1];
        if (penetrationA[0] >= 0.0f) return;

        // Check for separating axis with B shape's face planes
        int faceB = 0;
        float[] penetrationB = FindAxisLeastPenetration(bodyB, bodyA);
        faceB = (int) penetrationB[1];
        if (penetrationB[0] >= 0.0f) return;

        int referenceIndex = 0;
        boolean flip = false;  // Always point from A shape to B shape

        PhysicsShape refPoly; // Reference
        PhysicsShape incPoly; // Incident

        // Determine which shape contains reference face
        // Checking bias range for penetration
        if (penetrationA[0] >= (penetrationB[0]*0.95f + penetrationA[0]*0.01f)) {
            refPoly = bodyA;
            incPoly = bodyB;
            referenceIndex = faceA;
        }
        else {
            refPoly = bodyB;
            incPoly = bodyA;
            referenceIndex = faceB;
            flip = true;
        }

        // World space incident face
        Vector2[] incidentFace = FindIncidentFace(refPoly, incPoly, referenceIndex);

        // Setup reference face vertices
        PhysicsVertexData refData = refPoly.vertexData;
        Vector2 v1 = refData.positions[referenceIndex];
        referenceIndex = (((referenceIndex + 1) < refData.vertexCount) ? (referenceIndex + 1) : 0);
        Vector2 v2 = refData.positions[referenceIndex];

        // Transform vertices to world space
        v1 = MathMatVector2Product(refPoly.transform, v1);
        v1 = MathVector2Add(v1, refPoly.body.position);
        v2 = MathMatVector2Product(refPoly.transform, v2);
        v2 = MathVector2Add(v2, refPoly.body.position);

        // Calculate reference face side normal in world space
        Vector2 sidePlaneNormal = MathVector2Subtract(v2, v1);
        sidePlaneNormal = MathVector2Normalize(sidePlaneNormal);

        // Orthogonalize
        Vector2 refFaceNormal = new Vector2(sidePlaneNormal.y, -sidePlaneNormal.x);
        float refC = MathVector2DotProduct(refFaceNormal, v1);
        float negSide = MathVector2DotProduct(sidePlaneNormal, v1)*-1;
        float posSide = MathVector2DotProduct(sidePlaneNormal, v2);

        // MathVector2Clip incident face to reference face side planes (due to floating point error, possible to not have required points
        if (MathVector2Clip(new Vector2(-sidePlaneNormal.x, -sidePlaneNormal.y), incidentFace[0], incidentFace[1], negSide) < 2){
            return;
        }
        if (MathVector2Clip(sidePlaneNormal, incidentFace[0], incidentFace[1], posSide) < 2) {
            return;
        }

        // Flip normal if required
        manifold.normal = (flip ? new Vector2( -refFaceNormal.x, -refFaceNormal.y) : refFaceNormal);

        // Keep points behind reference face
        int currentPoint = 0; // MathVector2Clipped points behind reference face
        float separation = MathVector2DotProduct(refFaceNormal, incidentFace[0]) - refC;
        if (separation <= 0.0f) {
            manifold.contacts[currentPoint] = incidentFace[0];
            manifold.penetration = -separation;
            currentPoint++;
        }
        else {
            manifold.penetration = 0.0f;
        }

        separation = MathVector2DotProduct(refFaceNormal, incidentFace[1]) - refC;

        if (separation <= 0.0f) {
            manifold.contacts[currentPoint] = incidentFace[1];
            manifold.penetration += -separation;
            currentPoint++;

            // Calculate total penetration average
            manifold.penetration /= currentPoint;
        }

        manifold.contactsCount = currentPoint;
    }

    // Integrates physics forces into velocity
    void IntegratePhysicsForces(PhysicsBody body) {
        if ((body == null) || (body.inverseMass == 0.0f) || !body.enabled) return;

        body.velocity.x += (float)((body.force.x*body.inverseMass)*(deltaTime/2.0));
        body.velocity.y += (float)((body.force.y*body.inverseMass)*(deltaTime/2.0));

        if (body.useGravity) {
            body.velocity.x += (float)(gravityForce.x*(deltaTime/1000/2.0));
            body.velocity.y += (float)(gravityForce.y*(deltaTime/1000/2.0));
        }

        if (!body.freezeOrient) body.angularVelocity += (float)(body.torque*body.inverseInertia*(deltaTime/2.0));
    }

    // Initializes physics manifolds to solve collisions
    void InitializePhysicsManifolds(PhysicsManifoldData manifold) {
        PhysicsBody bodyA = manifold.bodyA;
        PhysicsBody bodyB = manifold.bodyB;

        if ((bodyA == null) || (bodyB == null)) return;

        // Calculate average restitution, static and dynamic friction
        manifold.restitution = (float) Math.sqrt(bodyA.restitution*bodyB.restitution);
        manifold.staticFriction = (float) Math.sqrt(bodyA.staticFriction*bodyB.staticFriction);
        manifold.dynamicFriction = (float) Math.sqrt(bodyA.dynamicFriction*bodyB.dynamicFriction);

        for (int i = 0; i < manifold.contactsCount; i++)
        {
            // Caculate radius from center of mass to contact
            Vector2 radiusA = MathVector2Subtract(manifold.contacts[i], bodyA.position);
            Vector2 radiusB = MathVector2Subtract(manifold.contacts[i], bodyB.position);

            Vector2 crossA = MathVector2Product(radiusA, bodyA.angularVelocity);
            Vector2 crossB = MathVector2Product(radiusB, bodyB.angularVelocity);

            Vector2 radiusV = new Vector2(0.0f, 0.0f);
            radiusV.x = bodyB.velocity.x + crossB.x - bodyA.velocity.x - crossA.x;
            radiusV.y = bodyB.velocity.y + crossB.y - bodyA.velocity.y - crossA.y;

            // Determine if we should perform a resting collision or not;
            // The idea is if the only thing moving this object is gravity, then the collision should be performed without any restitution
            if (MathVector2SqrLen(radiusV) < (MathVector2SqrLen(new Vector2((float)(gravityForce.x*deltaTime/1000), (float)(gravityForce.y*deltaTime/1000))) + PHYSAC_EPSILON)){
                manifold.restitution = 0;
            }
        }
    }

    // Integrates physics collisions impulses to solve collisions
    void IntegratePhysicsImpulses(PhysicsManifoldData manifold){
        PhysicsBody bodyA = manifold.bodyA;
        PhysicsBody bodyB = manifold.bodyB;

        if ((bodyA == null) || (bodyB == null)){
            return;
        }

        // Early out and positional correct if both objects have infinite mass
        if (Math.abs(bodyA.inverseMass + bodyB.inverseMass) <= PHYSAC_EPSILON){
            bodyA.velocity = new Vector2();
            bodyB.velocity = new Vector2();
            return;
        }

        for (int i = 0; i < manifold.contactsCount; i++){
            // Calculate radius from center of mass to contact
            Vector2 radiusA = MathVector2Subtract(manifold.contacts[i], bodyA.position);
            Vector2 radiusB = MathVector2Subtract(manifold.contacts[i], bodyB.position);

            // Calculate relative velocity
            Vector2 radiusV = new Vector2();
            radiusV.x = bodyB.velocity.x + MathVector2Product(radiusB, bodyB.angularVelocity).x - bodyA.velocity.x - MathVector2Product(radiusA, bodyA.angularVelocity).x;
            radiusV.y = bodyB.velocity.y + MathVector2Product(radiusB, bodyB.angularVelocity).y - bodyA.velocity.y - MathVector2Product(radiusA, bodyA.angularVelocity).y;

            // Relative velocity along the normal
            float contactVelocity = MathVector2DotProduct(radiusV, manifold.normal);

            // Do not resolve if velocities are separating
            if (contactVelocity > 0.0f) return;

            float raCrossN = MathVector2CrossProduct(radiusA, manifold.normal);
            float rbCrossN = MathVector2CrossProduct(radiusB, manifold.normal);

            float inverseMassSum = bodyA.inverseMass + bodyB.inverseMass + (raCrossN * raCrossN) * bodyA.inverseInertia + (rbCrossN * rbCrossN) * bodyB.inverseInertia;

            // Calculate impulse scalar value
            float impulse = -(1.0f + manifold.restitution) * contactVelocity;
            impulse /= inverseMassSum;
            impulse /= (float) manifold.contactsCount;

            // Apply impulse to each physics body
            Vector2 impulseV = new Vector2(manifold.normal.x * impulse, manifold.normal.y * impulse);

            if (bodyA.enabled){
                bodyA.velocity.x += bodyA.inverseMass * (-impulseV.x);
                bodyA.velocity.y += bodyA.inverseMass * (-impulseV.y);
                if (!bodyA.freezeOrient)
                    bodyA.angularVelocity += bodyA.inverseInertia * MathVector2CrossProduct(radiusA, new Vector2(-impulseV.x, -impulseV.y));
            }

            if (bodyB.enabled){
                bodyB.velocity.x += bodyB.inverseMass * (impulseV.x);
                bodyB.velocity.y += bodyB.inverseMass * (impulseV.y);
                if (!bodyB.freezeOrient)
                    bodyB.angularVelocity += bodyB.inverseInertia * MathVector2CrossProduct(radiusB, impulseV);
            }

            // Apply friction impulse to each physics body
            radiusV.x = bodyB.velocity.x + MathVector2Product(radiusB, bodyB.angularVelocity).x - bodyA.velocity.x - MathVector2Product(radiusA, bodyA.angularVelocity).x;
            radiusV.y = bodyB.velocity.y + MathVector2Product(radiusB, bodyB.angularVelocity).y - bodyA.velocity.y - MathVector2Product(radiusA, bodyA.angularVelocity).y;

            Vector2 tangent = new Vector2(radiusV.x - (manifold.normal.x * MathVector2DotProduct(radiusV, manifold.normal)), radiusV.y - (manifold.normal.y * MathVector2DotProduct(radiusV, manifold.normal)));
            tangent = MathVector2Normalize(tangent);

            // Calculate impulse tangent magnitude
            float impulseTangent = -MathVector2DotProduct(radiusV, tangent);
            impulseTangent /= inverseMassSum;
            impulseTangent /= (float) manifold.contactsCount;

            float absImpulseTangent = Math.abs(impulseTangent);

            // Don't apply tiny friction impulses
            if (absImpulseTangent <= PHYSAC_EPSILON) {
                return;
            }

            // Apply coulumb's law
            Vector2 tangentImpulse;
            if (absImpulseTangent < impulse * manifold.staticFriction){
                tangentImpulse = new Vector2(tangent.x * impulseTangent, tangent.y * impulseTangent);
            }
            else{
                tangentImpulse = new Vector2(tangent.x * -impulse * manifold.dynamicFriction, tangent.y * -impulse * manifold.dynamicFriction);
            }

            // Apply friction impulse
            if (bodyA.enabled){
                bodyA.velocity.x += bodyA.inverseMass * (-tangentImpulse.x);
                bodyA.velocity.y += bodyA.inverseMass * (-tangentImpulse.y);

                if (!bodyA.freezeOrient){
                    bodyA.angularVelocity += bodyA.inverseInertia * MathVector2CrossProduct(radiusA, new Vector2(-tangentImpulse.x, -tangentImpulse.y));
                }
            }

            if (bodyB.enabled){
                bodyB.velocity.x += bodyB.inverseMass * (tangentImpulse.x);
                bodyB.velocity.y += bodyB.inverseMass * (tangentImpulse.y);

                if (!bodyB.freezeOrient){
                    bodyB.angularVelocity += bodyB.inverseInertia * MathVector2CrossProduct(radiusB, tangentImpulse);
                }

            }
        }
    }

    // Integrates physics velocity into position and forces
    void IntegratePhysicsVelocity(PhysicsBody body) {
        if ((body == null) ||!body.enabled) {
            return;
        }

        body.position.x += (float)(body.velocity.x*deltaTime);
        body.position.y += (float)(body.velocity.y*deltaTime);

        if (!body.freezeOrient) body.orient += (float)(body.angularVelocity*deltaTime);
        body.shape.transform = MathMatFromRadians(body.orient);

        IntegratePhysicsForces(body);
    }

    // Corrects physics bodies positions based on manifolds collision information
    void CorrectPhysicsPositions(PhysicsManifoldData manifold) {
        PhysicsBody bodyA = manifold.bodyA;
        PhysicsBody bodyB = manifold.bodyB;

        if ((bodyA == null) || (bodyB == null)) return;

        Vector2 correction = new Vector2();
        correction.x = (Math.max(manifold.penetration - PHYSAC_PENETRATION_ALLOWANCE, 0.0f)/(bodyA.inverseMass + bodyB.inverseMass))*manifold.normal.x*PHYSAC_PENETRATION_CORRECTION;
        correction.y = (Math.max(manifold.penetration - PHYSAC_PENETRATION_ALLOWANCE, 0.0f)/(bodyA.inverseMass + bodyB.inverseMass))*manifold.normal.y*PHYSAC_PENETRATION_CORRECTION;

        if (bodyA.enabled) {
            bodyA.position.x -= correction.x*bodyA.inverseMass;
            bodyA.position.y -= correction.y*bodyA.inverseMass;
        }

        if (bodyB.enabled) {
            bodyB.position.x += correction.x*bodyB.inverseMass;
            bodyB.position.y += correction.y*bodyB.inverseMass;
        }
    }

    // Returns the extreme point along a direction within a polygon
    Vector2 GetSupport(PhysicsShape shape, Vector2 dir) {
        float bestProjection = -PHYSAC_FLT_MAX;
        Vector2 bestVertex = new Vector2(0.0f, 0.0f);
        PhysicsVertexData data = shape.vertexData;

        for (int i = 0; i < data.vertexCount; i++) {
            Vector2 vertex = data.positions[i];
            float projection = MathVector2DotProduct(vertex, dir);

            if (projection > bestProjection) {
                bestVertex = vertex;
                bestProjection = projection;
            }
        }

        return bestVertex;
    }

    // Finds polygon shapes axis least penetration
    float[] FindAxisLeastPenetration(PhysicsShape shapeA, PhysicsShape shapeB) {
        float bestDistance = -PHYSAC_FLT_MAX;
        int bestIndex = 0;

        PhysicsVertexData dataA = shapeA.vertexData;
        //PhysicsVertexData dataB = shapeB.vertexData;

        for (int i = 0; i < dataA.vertexCount; i++) {
            // Retrieve a face normal from A shape
            Vector2 normal = dataA.normals[i];
            Vector2 transNormal = MathMatVector2Product(shapeA.transform, normal);

            // Transform face normal into B shape's model space
            Matrix2x2 buT = MathMatTranspose(shapeB.transform);
            normal = MathMatVector2Product(buT, transNormal);

            // Retrieve support point from B shape along -n
            Vector2 support = GetSupport(shapeB, new Vector2(-normal.x, -normal.y));

            // Retrieve vertex on face from A shape, transform into B shape's model space
            Vector2 vertex = dataA.positions[i];
            vertex = MathMatVector2Product(shapeA.transform, vertex);
            vertex = MathVector2Add(vertex, shapeA.body.position);
            vertex = MathVector2Subtract(vertex, shapeB.body.position);
            vertex = MathMatVector2Product(buT, vertex);

            // Compute penetration distance in B shape's model space
            float distance = MathVector2DotProduct(normal, MathVector2Subtract(support, vertex));

            // Store greatest distance
            if (distance > bestDistance)
            {
                bestDistance = distance;
                bestIndex = i;
            }
        }

        return new float[]{bestDistance, bestIndex};
    }

    // Finds two polygon shapes incident face
    Vector2[] FindIncidentFace(PhysicsShape ref, PhysicsShape inc, int index) {
        Vector2[] result = {new Vector2(), new Vector2()};

        PhysicsVertexData refData = ref.vertexData;
        PhysicsVertexData incData = inc.vertexData;

        Vector2 referenceNormal = refData.normals[index];

        // Calculate normal in incident's frame of reference
        referenceNormal = MathMatVector2Product(ref.transform, referenceNormal); // To world space
        referenceNormal = MathMatVector2Product(MathMatTranspose(inc.transform), referenceNormal); // To incident's model space

        // Find most anti-normal face on polygon
        int incidentFace = 0;
        float minDot = PHYSAC_FLT_MAX;

        for (int i = 0; i < incData.vertexCount; i++)
        {
            float dot = MathVector2DotProduct(referenceNormal, incData.normals[i]);

            if (dot < minDot)
            {
                minDot = dot;
                incidentFace = i;
            }
        }

        // Assign face vertices for incident face
        result[0] = MathMatVector2Product(inc.transform, incData.positions[incidentFace]);
        result[0] = MathVector2Add(result[0], inc.body.position);
        incidentFace = (((incidentFace + 1) < incData.vertexCount) ? (incidentFace + 1) : 0);
        result[1] = MathMatVector2Product(inc.transform, incData.positions[incidentFace]);
        result[1] = MathVector2Add(result[1], inc.body.position);

        return result;
    }

    // Returns clipping value based on a normal and two faces
    int MathVector2Clip(Vector2 normal, Vector2 faceA, Vector2 faceB, float clip) {
        int sp = 0;
        Vector2[] out = new Vector2[]{faceA, faceB};

        // Retrieve distances from each endpoint to the line
        float distanceA = MathVector2DotProduct(normal, faceA) - clip;
        float distanceB = MathVector2DotProduct(normal, faceB) - clip;

        // If negative (behind plane)
        if (distanceA <= 0.0f) out[sp++] = faceA;
        if (distanceB <= 0.0f) out[sp++] = faceB;

        // If the points are on different sides of the plane
        if ((distanceA*distanceB) < 0.0f) {
            // Push intersection point
            float alpha = distanceA/(distanceA - distanceB);
            out[sp] = faceA;
            Vector2 delta = MathVector2Subtract(faceB, faceA);
            delta.x *= alpha;
            delta.y *= alpha;
            out[sp] = MathVector2Add(out[sp], delta);
            sp++;
        }

        // Assign the new converted values
        faceA = out[0];
        faceB = out[1];

        return sp;
    }

    // Returns the barycenter of a triangle given by 3 points
    Vector2 MathTriangleBarycenter(Vector2 v1, Vector2 v2, Vector2 v3) {
        Vector2 result = new Vector2(0.0f, 0.0f);

        result.x = (v1.x + v2.x + v3.x)/3;
        result.y = (v1.y + v2.y + v3.y)/3;

        return result;
    }


    // Returns the cross product of a vector and a value
    public Vector2 MathVector2Product(Vector2 vector, float value) {
        return new Vector2(-value*vector.y, value*vector.x);
    }

    // Returns the cross product of two vectors
    public float MathVector2CrossProduct(Vector2 v1, Vector2 v2) {
        return (v1.x*v2.y - v1.y*v2.x);
    }

    // Returns the len square root of a vector
    public float MathVector2SqrLen(Vector2 vector) {
        return (vector.x*vector.x + vector.y*vector.y);
    }

    // Returns the dot product of two vectors
    public float MathVector2DotProduct(Vector2 v1, Vector2 v2) {
        return (v1.x*v2.x + v1.y*v2.y);
    }

    // Returns the square root of distance between two vectors
    public float MathVector2SqrDistance(Vector2 v1, Vector2 v2) {
        Vector2 dir = MathVector2Subtract(v1, v2);
        return MathVector2DotProduct(dir, dir);
    }

    // Returns the normalized values of a vector
    public Vector2 MathVector2Normalize(Vector2 vector) {
        float length, ilength;

        length = (float) Math.sqrt(vector.x* vector.x + vector.y* vector.y);

        if (length == 0) length = 1.0f;

        ilength = 1.0f/length;

        return new Vector2(vector.x*ilength, vector.y*ilength);
    }

    // Returns the sum of two given vectors
    public Vector2 MathVector2Add(Vector2 v1, Vector2 v2) {
        return new Vector2(v1.x + v2.x, v1.y + v2.y);
    }

    // Returns the subtract of two given vectors
    public Vector2 MathVector2Subtract(Vector2 v1, Vector2 v2) {
        return new Vector2(v1.x - v2.x, v1.y - v2.y);
    }

    // Creates a matrix 2x2 from a given radians value
    public Matrix2x2 MathMatFromRadians(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        return new Matrix2x2(cos, -sin, sin, cos);
    }

    // Returns the transpose of a given matrix 2x2
    public Matrix2x2 MathMatTranspose(Matrix2x2 matrix) {
        return new Matrix2x2(matrix.m00, matrix.m10, matrix.m01, matrix.m11);
    }

    // Multiplies a vector by a matrix 2x2
    public Vector2 MathMatVector2Product(Matrix2x2 matrix, Vector2 vector){
        return new Vector2(matrix.m00*vector.x + matrix.m01*vector.y, matrix.m10*vector.x + matrix.m11*vector.y);
    }

    //Set PHYSAC_DEBUG
    public void setDebug(boolean b){
        PHYSAC_DEBUG = b;
    }

    //Output Tracelog message if PHYSAC_DEBUG is true
    private void Tracelog(String message){
        if(PHYSAC_DEBUG){
            System.out.println(message);
        }
    }

}
