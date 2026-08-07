package com.raylib.java;

import com.raylib.java.core.rCore;
import com.raylib.java.core.tracelog.TraceLog;
import com.raylib.java.gestures.rGestures;
import com.raylib.java.io.FileIO;
import com.raylib.java.models.rModels;
import com.raylib.java.extras.physac.Physac;
import com.raylib.java.raudioal.rAudioAL;
import com.raylib.java.rlgl.RLGL;
import com.raylib.java.shapes.rShapes;
import com.raylib.java.text.rText;
import com.raylib.java.textures.rTextures;
import org.jetbrains.annotations.Contract;

public class Raylib {

    public rAudioAL audio;
    public RLGL rlgl;
    public rCore core;
    public FileIO files;
    public Config config;
    public rText text;
    public rShapes shapes;
    public rTextures textures;
    public rModels models;

    public Physac physac;
    public rGestures gestures;

    public TraceLog tracelog;

    /**
     * Creates new Raylib instance.
     */
    public Raylib() {
        Init();
    }

    /**
     * Creates new Raylib instance and initializes the window
     *
     * @param ScreenWidth  width of the window in pixels
     * @param ScreenHeight height of the window in pixels
     * @param title        title to display for the window
     */
    public Raylib(int ScreenWidth, int ScreenHeight, String title) {
        Init();

        core.InitWindow(ScreenWidth, ScreenHeight, title);
    }

    @Contract(mutates = "this")
    private void Init() {
        this.config = new Config();
        this.rlgl = new RLGL(this);
        this.core = new rCore(this);
        this.files = new FileIO(this);
        this.textures = new rTextures(this);
        this.text = new rText(this);
        this.audio = new rAudioAL(this);
        this.shapes = new rShapes(this);
        this.models = new rModels(this);
        this.physac = new Physac();
        this.gestures = new rGestures(this);
    }


}
