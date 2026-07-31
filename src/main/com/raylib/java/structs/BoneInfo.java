package com.raylib.java.structs;

public class BoneInfo implements Cloneable{

    public String name;          // Bone name
    public int parent;             // Bone parent

    public BoneInfo() {
        name = "";
        parent = 0;
    }

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
