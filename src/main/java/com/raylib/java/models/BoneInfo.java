package com.raylib.java.models;

public class BoneInfo {

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

}
