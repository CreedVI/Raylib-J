package com.raylib.java.extras.physac;

import com.raylib.java.raymath.Vector2;

public class PhysicsManifoldData{

    public long id;                                    // Unique identifier
    public PhysicsBody bodyA;                          // Manifold first physics body reference
    public PhysicsBody bodyB;                          // Manifold second physics body reference
    public float penetration;                          // Depth of penetration from collision
    public Vector2 normal;                             // Normal direction vector from 'a' to 'b'
    public Vector2[] contacts;                        // Points of contact during collision
    public int contactsCount;                          // Current collision number of contacts
    public float restitution;                          // Mixed restitution during collision
    public float dynamicFriction;                      // Mixed dynamic friction during collision
    public float staticFriction;                       // Mixed static friction during collision

    public PhysicsManifoldData(){
        normal = new Vector2();
        contacts = new Vector2[]{new Vector2(), new Vector2()};

        bodyA = new PhysicsBody();
        bodyB = new PhysicsBody();
    }

}
