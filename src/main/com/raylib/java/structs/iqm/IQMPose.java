package com.raylib.java.structs.iqm;

public class IQMPose {

    public int parent;
    public int mask;
    public float channeloffset[];
    public float channelscale[];

    public IQMPose() {
        channeloffset = new float[10];
        channelscale = new float[10];
    }

}
