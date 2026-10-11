package com.raylib.java.structs;

import static com.raylib.java.rlgl.RLGL.MAX_MATERIAL_MAPS;

/**
 * Material, includes shader and maps
 */
public class Material implements Cloneable {

    /**
     * Material shader
     */
    public Shader shader;
    /**
     * Material maps array (MAX_MATERIAL_MAPS)
     */
    public MaterialMap[] maps;
    /**
     * Material generic parameters (if required)
     */
    public float[] params;

    public Material(){
        shader = new Shader();
        maps = new MaterialMap[MAX_MATERIAL_MAPS];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new MaterialMap();
        }
        params = new float[4];
    }

    @Override
    public Material clone() {
        try {
            Material clone = (Material) super.clone();
            clone.shader = shader.clone();
            clone.maps = new MaterialMap[maps.length];
            System.arraycopy(maps, 0, clone.maps, 0, maps.length);
            clone.params = new float[params.length];
            System.arraycopy(params, 0, clone.params, 0, params.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
