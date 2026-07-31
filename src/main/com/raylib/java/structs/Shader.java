package com.raylib.java.structs;

import static com.raylib.java.rlgl.RLGL.MAX_SHADER_LOCATIONS;

public class Shader implements Cloneable {

    public int id;                 // Shader program id
    public int[] locs;              // Shader locations array (MAX_SHADER_LOCATIONS)

    public Shader() {
        id = 0;
        locs = new int[MAX_SHADER_LOCATIONS];
    }

    public Shader(int id, int[] locs) {
        this.id = id;
        this.locs = locs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[] getLocs() {
        return locs;
    }

    public void setLocs(int[] locs) {
        this.locs = locs;
    }

    @Override
    public Shader clone() {
        try {
            Shader clone = (Shader) super.clone();

            clone.locs = new int[locs.length];
            System.arraycopy(locs, 0, clone.locs, 0, locs.length);

            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
