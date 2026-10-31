package com.raylib.java.structs;

/**
 * ModelAnimation, contains a full animation sequence
 */
public class ModelAnimation implements Cloneable {

    /**
     * Name of animation
     */
    public String name;
    /**
     * Number of bones
     */
    public int boneCount;
    /**
     * Number of animation frames
     */
    public int keyframeCount;
    /**
     * Animation sequence keyframe poses [keyframe][pose]
     */
    public Transform[][] keyframePoses;

    public ModelAnimation() {

    }

    @Override
    public ModelAnimation clone() {
        try {
            ModelAnimation clone = (ModelAnimation) super.clone();

            clone.name = name;
            clone.boneCount = boneCount;
            clone.keyframeCount = keyframeCount;

            for(int i = 0; i < keyframePoses.length; i++) {
                clone.keyframePoses[i] = new Transform[keyframePoses[i].length];
                System.arraycopy(keyframePoses[i], 0, clone.keyframePoses[i], 0, keyframePoses[i].length);
            }

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
