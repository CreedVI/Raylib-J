package com.raylib.java.models.iqm;

public class IQMHeader {

    public char[] magic;
    public int version;
    public int filesize;
    public int flags;
    public int num_text, ofs_text;
    public int num_meshes, ofs_meshes;
    public int num_vertexarrays, num_vertexes, ofs_vertexarrays;
    public int num_triangles, ofs_triangles, ofs_adjacency;
    public int num_joints, ofs_joints;
    public int num_poses, ofs_poses;
    public int num_anims, ofs_anims;
    public int num_frames, num_framechannels, ofs_frames, ofs_bounds;
    public int num_comment, ofs_comment;
    public int num_extensions, ofs_extensions;

    public IQMHeader() {
        magic = new char[16];
    }

}
