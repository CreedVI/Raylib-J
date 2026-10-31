package com.raylib.java.structs;

/**
 * Skeleton, animation bones hierarchy
 */
public class ModelSkeleton implements Cloneable {
    /**
     * Number of bones
     */
    public int boneCount;

    /**
     * Bones information (skeleton)
     */
    public BoneInfo[] bones;

    /**
     * Bones base transformation
     */
    public Transform[] bindPose;

    public ModelSkeleton() {

    }

    @Override
    public ModelSkeleton clone() {
        try {
            ModelSkeleton clone = (ModelSkeleton) super.clone();

            clone.boneCount = boneCount;

            clone.bones = new BoneInfo[clone.boneCount];
            System.arraycopy(bones, 0, clone.bones, 0, clone.boneCount);

            clone.bindPose = new Transform[bindPose.length];
            System.arraycopy(bindPose, 0, clone.bindPose, 0, bindPose.length);

            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
