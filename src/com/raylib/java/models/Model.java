package com.raylib.java.models;

import com.raylib.java.raymath.Matrix;

public class Model {

    public Matrix transform;       // Local transform matrix

    public int meshCount;          // Number of meshes
    public int materialCount;      // Number of materials
    public Mesh[] meshes;           // Meshes array
    public Material[] materials;    // Materials array
    public int[] meshMaterial;      // Mesh material number

    // Animation data
    public int boneCount;          // Number of bones
    public BoneInfo[] bones;        // Bones information (skeleton)
    public Transform[] bindPose;    // Bones base transformation (pose)

    public Model() {
        transform = new Matrix();

        meshes = new Mesh[16];
        materials = new Material[16];
        meshMaterial = new int[16];

        bones = new BoneInfo[64];
        bindPose = new Transform[64];
    }

}
