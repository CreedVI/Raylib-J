package com.creedvi.raylib.java.tests;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;

public class EnduranceTest{

    static int width = 800;
    static int height = 600;
    static int frames = 0;
    static Color bgClearColor;

    public static void main(String[] args){
        Raylib rlj = new Raylib();
        bgClearColor = new Color(66, 66, 66, 255);

        rlj.core.initWindow(width, height, "SEIZURE WARNING");
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            ++frames;

            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(bgClearColor);
            rlj.core.EndDrawing();

            bgClearColor.setR(bgClearColor.getR() + 2);
            bgClearColor.setG(bgClearColor.getG() + 4);
            bgClearColor.setB(bgClearColor.getB() + 6);

            if(bgClearColor.getR() >= 255){
                bgClearColor.setR(0);
            }
            if(bgClearColor.getG() >= 255){
                bgClearColor.setG(0);
            }
            if(bgClearColor.getB() >= 255){
                bgClearColor.setB(0);
            }
            if(frames%60 == 0){
                System.out.println(frames/60);
            }
        }
    }
}
