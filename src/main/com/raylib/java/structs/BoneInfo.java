package com.raylib.java.structs;

/**
 * Bone, skeletal animation bone
 */
public class BoneInfo implements Cloneable {

    /**
     * Bone name
     */
    public String name;
    /**
     * Bone parent ID
     */
    public int parent;

    /**
     * Create new {@code BoneInfo} with empty name and no parent
     */
    public BoneInfo() {
        name = "";
        parent = -1;
    }

    /**
     * Create new {@code BoneInfo} with specified name and parent
     *
     * @param name Name of current bone
     * @param parent ID of parent bone
     */
    public BoneInfo(String name, int parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public BoneInfo clone() {
        try {
            BoneInfo clone = (BoneInfo) super.clone();
            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
