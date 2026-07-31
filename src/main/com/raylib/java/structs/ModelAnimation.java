package com.raylib.java.structs;

public class ModelAnimation implements Cloneable {

    public int boneCount;          // Number of bones
    public int frameCount;         // Number of animation frames
    public String name;
    public BoneInfo[] bones;        // Bones information (skeleton)
    public Transform[][] framePoses; // Poses array by frame

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
