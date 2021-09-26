package com.creedvi.raylib.java.rlj;

import com.creedvi.raylib.java.rlj.core.Core;
import com.creedvi.raylib.java.rlj.models.Models;
import com.creedvi.raylib.java.rlj.shapes.Shapes;
import com.creedvi.raylib.java.rlj.text.Text;
import com.creedvi.raylib.java.rlj.textures.Textures;

public class Raylib{

    //public Audio audio;
    public Core core;
    public Config config;
    public Text text;
    public Shapes shapes;
    public Textures textures;
    public Models models;

    /**
     * Creates new Raylib instance.
     */
    public Raylib(){
        //audio = new Audio();
        core = new Core();
        config = new Config();
        text = new Text();
        shapes = new Shapes();
        textures = new Textures();
        models = new Models();
    }

    /**
     * Creates new Raylib instance and initializes the window
     * @param ScreenWidth width of the window in pixels
     * @param ScreenHeight height of the window in pixels
     * @param title title to display for the window
     */
    public Raylib(int ScreenWidth, int ScreenHeight, String title){
        //audio = new Audio();
        core = new Core();
        config = new Config();
        text = new Text();
        shapes = new Shapes();
        textures = new Textures();
        models = new Models();

        core.InitWindow(ScreenWidth, ScreenHeight, title);
    }


}
