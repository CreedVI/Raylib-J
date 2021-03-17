package com.creedvi.raylib.java.rlj;

import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.text.Text;

public class Raylib{

    public Core core;
    Config config;
    public Text text;

    /**
     * Creates new Raylib instance.
     */
    public Raylib(){
        core = new Core();
        text = new Text();
    }


}
