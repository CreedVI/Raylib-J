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

    /**
     * Skeleton for animation
     */
    public ModelSkeleton skeleton;

    /**
     * Current animation pose
     */
    public Transform[] currentPose;
    /**
     * Bones animated transformation matrices
     */
    public Matrix[] boneMatrices;

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

        skeleton = new ModelSkeleton();
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

            clone.skeleton = skeleton.clone();

            clone.currentPose = new Transform[currentPose.length];
            System.arraycopy(currentPose, 0, clone.currentPose, 0, currentPose.length);
            clone.boneMatrices = new Matrix[boneMatrices.length];
            System.arraycopy(boneMatrices, 0, clone.boneMatrices, 0, boneMatrices.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
