package com.creedvi.raylib.java.rlj.rlgl.shader;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static com.creedvi.raylib.java.rlj.rlgl.RLGL.MAX_SHADER_LOCATIONS;

public class Shader {

    int id;                 // Shader program id
    IntBuffer locs;              // Shader locations array (MAX_SHADER_LOCATIONS)

    public Shader(){
        id = 0;
        locs = MemoryUtil.memAllocInt(MAX_SHADER_LOCATIONS);
    }

    public Shader(int id, IntBuffer locs){
        this.id = id;
        this.locs = locs;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public IntBuffer getLocs(){
        return locs;
    }

    public void setLocs(IntBuffer locs){
        this.locs = locs;
    }

}
