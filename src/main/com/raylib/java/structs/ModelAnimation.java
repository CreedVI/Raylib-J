package com.raylib.java.structs;

/**
 * ModelAnimation, contains a full animation sequence
 */
public class ModelAnimation implements Cloneable {

    /**
     * Number of bones
     */
    public int boneCount;
    /**
     * Number of animation frames
     */
    public int frameCount;
    /**
     * Name of animation
     */
    public String name;
    /**
     * Bones information (skeleton)
     */
    public BoneInfo[] bones;
    /**
     * Poses array by frame
     */
    public Transform[][] framePoses;

    @Override
    public ModelAnimation clone() {
        try {
            ModelAnimation clone = (ModelAnimation) super.clone();

            clone.bones = new BoneInfo[bones.length];
            System.arraycopy(bones, 0, clone.bones, 0, bones.length);

            for(int i = 0; i < framePoses.length; i++) {
                clone.framePoses[i] = new Transform[framePoses[i].length];
                System.arraycopy(framePoses[i], 0, clone.framePoses[i], 0, framePoses[i].length);
            }

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
