package com.raylib.java.extras.physac;

public class PhysicsShape{

    public int type;                                   // Shape type (circle or polygon)
    public PhysicsBody body;                           // Shape physics body data pointer
    public PhysicsVertexData vertexData;               // Shape vertices data (used for polygon shapes)
    public float radius;                               // Shape radius (used for circle shapes)
    public Matrix2x2 transform;                        // Vertices transform matrix 2x2

    public PhysicsShape(){
        vertexData = new PhysicsVertexData();
        transform = new Matrix2x2();
    }

}
