package com.raylib.java.models;

public class ModelAnimation {

    public int boneCount;          // Number of bones
    public int frameCount;         // Number of animation frames
    public BoneInfo[] bones;        // Bones information (skeleton)
    public Transform[][] framePoses; // Poses array by frame

}
