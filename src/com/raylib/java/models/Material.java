package com.raylib.java.models;

import com.raylib.java.rlgl.shader.Shader;

import static com.raylib.java.rlgl.RLGL.MAX_MATERIAL_MAPS;

public class Material {

    public Shader shader;          // Material shader
    public MaterialMap[] maps;      // Material maps array (MAX_MATERIAL_MAPS)
    public float[] params;        // Material generic parameters (if required)

    public Material(){
        shader = new Shader();
        maps = new MaterialMap[MAX_MATERIAL_MAPS];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new MaterialMap();
        }
        params = new float[4];
    }

}
