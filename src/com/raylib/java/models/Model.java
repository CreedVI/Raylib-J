package com.raylib.java.models;

import com.raylib.java.raymath.Matrix;

public class Model {

    Matrix transform;       // Local transform matrix

    int meshCount;          // Number of meshes
    int materialCount;      // Number of materials
    Mesh[] meshes;           // Meshes array
    public Material[] materials;    // Materials array
    int[] meshMaterial;      // Mesh material number

    // Animation data
    int boneCount;          // Number of bones
    BoneInfo[] bones;        // Bones information (skeleton)
    Transform[] bindPose;    // Bones base transformation (pose)

    public Model() {
        transform = new Matrix();

        meshes = new Mesh[16];
        materials = new Material[16];
        meshMaterial = new int[16];

        bones = new BoneInfo[64];
        bindPose = new Transform[64];
    }

}
