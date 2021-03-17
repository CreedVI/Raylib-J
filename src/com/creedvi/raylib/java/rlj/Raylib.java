package com.creedvi.raylib.java.rlj;

import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.shapes.Shapes;
import com.creedvi.raylib.java.rlj.text.Text;

public class Raylib{

    public Core core;
    public Config config;
    public Text text;
    public Shapes shapes;

    /**
     * Creates new Raylib instance.
     */
    public Raylib(){
        core = new Core();
        config = new Config();
        text = new Text();
        shapes = new Shapes();
    }


}
