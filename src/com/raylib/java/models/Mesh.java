package com.raylib.java.models;

public class Mesh {

    int vertexCount;        // Number of vertices stored in arrays
    int triangleCount;      // Number of triangles stored (indexed or not)

    // Vertex attributes data
    float[] vertices;        // Vertex position (XYZ - 3 components per vertex) (shader-location = 0)
    float[] texcoords;       // Vertex texture coordinates (UV - 2 components per vertex) (shader-location = 1)
    float[] texcoords2;      // Vertex second texture coordinates (useful for lightmaps) (shader-location = 5)
    float[] normals;         // Vertex normals (XYZ - 3 components per vertex) (shader-location = 2)
    float[] tangents;        // Vertex tangents (XYZW - 4 components per vertex) (shader-location = 4)
    byte[] colors;      // Vertex colors (RGBA - 4 components per vertex) (shader-location = 3)
    float[] indices;    // Vertex indices (in case vertex data comes indexed)

    // Animation vertex data
    float[] animVertices;    // Animated vertex positions (after bones transformations)
    float[] animNormals;     // Animated normals (after bones transformations)
    byte[] boneIds; // Vertex bone ids, max 255 bone ids, up to 4 bones influence by vertex (skinning)
    float[] boneWeights;     // Vertex bone weight, up to 4 bones influence by vertex (skinning)

    // OpenGL identifiers
    int vaoId;     // OpenGL Vertex Array Object id
    int[] vboId;    // OpenGL Vertex Buffer Objects id (default vertex data)

    public Mesh() {

    }

}
