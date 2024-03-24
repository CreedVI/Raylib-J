package com.raylib.java.extras.physac;

import com.raylib.java.raymath.Vector2;

public class PhysicsBody{

    public long id;                                    // Unique identifier
    public boolean enabled;                            // Enabled dynamics state (collisions are calculated anyway)
    public Vector2 position;                           // Physics body shape pivot
    public Vector2 velocity;                           // Current linear velocity applied to position
    public Vector2 force;                              // Current linear force (reset to 0 every step)
    public float angularVelocity;                      // Current angular velocity applied to orient
    public float torque;                               // Current angular force (reset to 0 every step)
    public float orient;                               // Rotation in radians
    public float inertia;                              // Moment of inertia
    public float inverseInertia;                       // Inverse value of inertia
    public float mass;                                 // Physics body mass
    public float inverseMass;                          // Inverse value of mass
    public float staticFriction;                       // Friction when the body has not movement (0 to 1)
    public float dynamicFriction;                      // Friction when the body has movement (0 to 1)
    public float restitution;                          // Restitution coefficient of the body (0 to 1)
    public boolean useGravity;                         // Apply gravity force to dynamics
    public boolean isGrounded;                         // Physics grounded on other body state
    public boolean freezeOrient;                       // Physics rotation constraint
    public PhysicsShape shape;                         // Physics body shape information (type, radius, vertices, transform)

    public PhysicsBody(){
        position = new Vector2();
        velocity = new Vector2();
        force = new Vector2();
        shape = new PhysicsShape();
    }

}
