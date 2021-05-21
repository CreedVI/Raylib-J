package com.creedvi.raylib.java.rlj.rlgl.shader;

import static com.creedvi.raylib.java.rlj.rlgl.RLGL.MAX_SHADER_LOCATIONS;

public class Shader {

    int id;                 // Shader program id
    public int[] locs;              // Shader locations array (MAX_SHADER_LOCATIONS)

    public Shader(){
        id = 0;
        locs = new int[MAX_SHADER_LOCATIONS];
    }

    public Shader(int id, int[] locs){
        this.id = id;
        this.locs = locs;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int[] getLocs(){
        return locs;
    }

    public void setLocs(int[] locs){
        this.locs = locs;
    }

}
