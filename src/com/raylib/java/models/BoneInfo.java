package com.raylib.java.models;

public class BoneInfo {

    String name;          // Bone name
    int parent;             // Bone parent

    public BoneInfo() {
        name = "";
        parent = 0;
    }

    public BoneInfo(String name, int parent) {
        this.name = name;
        this.parent = parent;
    }

}
