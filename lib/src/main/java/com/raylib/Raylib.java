package com.raylib;

import com.raylib.core.Core;
import com.raylib.shapes.Shapes;
import com.raylib.text.Text;
import com.raylib.textures.Textures;
import com.raylib.audio.Audio;

public class Raylib{

    public Core core;
    public Config config;
    public Text text;
    public Shapes shapes;
    public Textures textures;
    public Audio audio;

    /**
     * Creates new Raylib instance.
     */
    public Raylib(){
        core = new Core();
        config = new Config();
        text = new Text();
        shapes = new Shapes();
        textures = new Textures();
        audio = new Audio();
    }

    /**
     * Creates new Raylib instance and Initializes the window
     * @param ScreenWidth width of the window in pixels
     * @param ScreenHeight height of the window in pixels
     * @param title title to display for the window
     */
    public Raylib(int ScreenWidth, int ScreenHeight, String title){
        core = new Core();
        config = new Config();
        text = new Text();
        shapes = new Shapes();
        textures = new Textures();

        core.InitWindow(ScreenWidth, ScreenHeight, title);
    }


}
