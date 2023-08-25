package com.raylib.java.models;

public class Mesh {

    public int vertexCount;        // Number of vertices stored in arrays
    public int triangleCount;      // Number of triangles stored (indexed or not)

    // Vertex attributes data
    public float[] vertices;        // Vertex position (XYZ - 3 components per vertex) (shader-location = 0)
    public float[] texcoords;       // Vertex texture coordinates (UV - 2 components per vertex) (shader-location = 1)
    public float[] texcoords2;      // Vertex second texture coordinates (useful for lightmaps) (shader-location = 5)
    public float[] normals;         // Vertex normals (XYZ - 3 components per vertex) (shader-location = 2)
    public float[] tangents;        // Vertex tangents (XYZW - 4 components per vertex) (shader-location = 4)
    public byte[] colors;           // Vertex colors (RGBA - 4 components per vertex) (shader-location = 3)
    public float[] indices;         // Vertex indices (in case vertex data comes indexed)
    public short[] indicesS;

    // Animation vertex data
    public float[] animVertices;    // Animated vertex positions (after bones transformations)
    public float[] animNormals;     // Animated normals (after bones transformations)
    public byte[] boneIds;          // Vertex bone ids, max 255 bone ids, up to 4 bones influence by vertex (skinning)
    public float[] boneWeights;     // Vertex bone weight, up to 4 bones influence by vertex (skinning)

    // OpenGL identifiers
    public int vaoId;               // OpenGL Vertex Array Object id
    public int[] vboId;             // OpenGL Vertex Buffer Objects id (default vertex data)

    public Mesh() {
        texcoords = new float[1];
        vertices = new float[0];
    }

}
