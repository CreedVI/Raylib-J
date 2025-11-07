package com.raylib.java;

import com.raylib.java.core.rCore;
import com.raylib.java.models.rModels;
import com.raylib.java.extras.physac.Physac;
import com.raylib.java.raudioal.rAudioAL;
import com.raylib.java.shapes.rShapes;
import com.raylib.java.text.rText;
import com.raylib.java.textures.rTextures;

public class Raylib{

    public rAudioAL audio;
    public rCore core;
    public Config config;
    public rText text;
    public rShapes shapes;
    public rTextures textures;
    public rModels models;
    public Physac physac;

    /**
     * Creates new Raylib instance.
     */
    public Raylib(){
        config = new Config();
        core = new rCore(this);
        textures = new rTextures(this);
        text = new rText(this);
        audio = new rAudioAL(this);
        shapes = new rShapes(this);
        models = new rModels(this);
        physac = new Physac();
    }

    /**
     * Creates new Raylib instance and initializes the window
     * @param ScreenWidth width of the window in pixels
     * @param ScreenHeight height of the window in pixels
     * @param title title to display for the window
     */
    public Raylib(int ScreenWidth, int ScreenHeight, String title){
        config = new Config();
        core = new rCore(this);
        textures = new rTextures(this);
        text = new rText(this);
        audio = new rAudioAL(this);
        shapes = new rShapes(this);
        models = new rModels(this);
        physac = new Physac();

        core.InitWindow(ScreenWidth, ScreenHeight, title);
    }


}
