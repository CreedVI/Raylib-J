package com.raylib.java.structs;

public class Mesh implements Cloneable {

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
    public Matrix[] boneMatrices;   // Bones animated transformation matrices
    public int boneCount;          // Number of bones

    // OpenGL identifiers
    public int vaoId;               // OpenGL Vertex Array Object id
    public int[] vboId;             // OpenGL Vertex Buffer Objects id (default vertex data)

    public Mesh() {
        texcoords = new float[1];
        vertices = new float[0];
    }

    @Override
    public Mesh clone() {
        try {
            Mesh clone = (Mesh) super.clone();

            clone.vertices = new float[vertices.length];
            System.arraycopy(vertices, 0, clone.vertices, 0, vertices.length);
            clone.texcoords = new float[texcoords.length];
            System.arraycopy(texcoords, 0, clone.texcoords, 0, texcoords.length);
            clone.texcoords2 = new float[texcoords2.length];
            System.arraycopy(texcoords2, 0, clone.texcoords2, 0, texcoords2.length);
            clone.normals = new float[normals.length];
            System.arraycopy(normals, 0, clone.normals, 0, normals.length);
            clone.tangents = new float[tangents.length];
            System.arraycopy(tangents, 0, clone.tangents, 0, tangents.length);
            clone.colors = new byte[colors.length];
            System.arraycopy(colors, 0, clone.colors, 0, colors.length);
            clone.indices = new float[indices.length];
            System.arraycopy(indices, 0, clone.indices, 0, indices.length);
            clone.indicesS = new short[indicesS.length];
            System.arraycopy(indicesS, 0, clone.indicesS, 0, indicesS.length);

            clone.animVertices = new float[animVertices.length];
            System.arraycopy(animVertices, 0, clone.animVertices, 0, animVertices.length);
            clone.animNormals = new float[animNormals.length];
            System.arraycopy(animNormals, 0, clone.animNormals, 0, animNormals.length);
            clone.boneIds = new byte[boneIds.length];
            System.arraycopy(boneIds, 0, clone.boneIds, 0, boneIds.length);
            clone.boneWeights = new float[boneWeights.length];
            System.arraycopy(boneWeights, 0, clone.boneWeights, 0, boneWeights.length);
            clone.boneMatrices = new Matrix[boneMatrices.length];
            System.arraycopy(boneMatrices, 0, clone.boneMatrices, 0, boneMatrices.length);

            clone.vboId = new int[vboId.length];
            System.arraycopy(vboId, 0, clone.vboId, 0, vboId.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
