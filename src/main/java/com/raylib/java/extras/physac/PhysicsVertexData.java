package com.raylib.java.extras.physac;

import com.raylib.java.raymath.Vector2;

public class PhysicsVertexData{

    public int vertexCount;         // Vertex count (positions and normals)
    public Vector2[] positions;     // Vertex positions vectors
    public Vector2[] normals;       // Vertex normals vectors

    public PhysicsVertexData(){
        positions = new Vector2[Physac.PHYSAC_MAX_VERTICES];
        for (int i = 0; i < positions.length; i++){
            positions[i] = new Vector2();
        }
        normals = new Vector2[Physac.PHYSAC_MAX_VERTICES];
        for (int i = 0; i < normals.length; i++){
            normals[i] = new Vector2();
        }
    }

}
