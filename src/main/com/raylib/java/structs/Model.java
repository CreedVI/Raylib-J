package com.raylib.java.structs;

/**
 * Model, meshes, materials and animation data
 */
public class Model implements Cloneable {

    /**
     * Local transform matrix
     */
    public Matrix transform;

    /**
     * Number of meshes
     */
    public int meshCount;
    /**
     * Number of materials
     */
    public int materialCount;
    /**
     * Meshes array
     */
    public Mesh[] meshes;
    /**
     * Materials array
     */
    public Material[] materials;
    /**
     * Mesh material number
     */
    public int[] meshMaterial;

    // Animation data
    /**
     * Number of bones
     */
    public int boneCount;
    /**
     * Bones information (skeleton)
     */
    public BoneInfo[] bones;
    /**
     * Bones base transformation (pose)
     */
    public Transform[] bindPose;

    public Model() {
        transform = new Matrix();

        meshes = new Mesh[16];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = new Mesh();
        }
        materials = new Material[16];
        for (int i = 0; i < materials.length; i++) {
            materials[i] = new Material();
        }
        meshMaterial = new int[16];

        bones = new BoneInfo[64];
        for (int i = 0; i < bones.length; i++) {
            bones[i] = new BoneInfo();
        }
        bindPose = new Transform[64];
        for (int i = 0; i < bindPose.length; i++) {
            bindPose[i] = new Transform();
        }
    }

    @Override
    public Model clone() {
        try {
            Model clone = (Model) super.clone();

            clone.transform = transform.clone();

            clone.meshes = new Mesh[meshes.length];
            System.arraycopy(meshes, 0, clone.meshes, 0, meshes.length);
            clone.materials = new Material[materials.length];
            System.arraycopy(materials, 0, clone.materials, 0, materials.length);
            clone.meshMaterial = new int[meshMaterial.length];
            System.arraycopy(meshMaterial, 0, clone.meshMaterial, 0, meshMaterial.length);

            clone.bones = new BoneInfo[bones.length];
            System.arraycopy(bones, 0, clone.bones, 0, bones.length);
            clone.bindPose = new Transform[bindPose.length];
            System.arraycopy(bindPose, 0, clone.bindPose, 0, bindPose.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
