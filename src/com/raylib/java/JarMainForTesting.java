package com.raylib.java;

import com.raylib.java.core.Color;

public class JarMainForTesting{

    static Raylib rlj;

    public static void main(String[] args){
        rlj = new Raylib(800, 600, null);
        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()){
            update();
            render();
        }

    }

    private static void update(){

    }

    private static void render(){
        rlj.core.BeginDrawing();
        rlj.core.ClearBackground(Color.WHITE);
        rlj.shapes.DrawRectangle(0,0,200,200,Color.BLACK);
        rlj.text.DrawText("Hello, World!", 400 - (rlj.text.MeasureText("Hello, World!", 20)/2), 300, 20, Color.DARKGRAY);
        rlj.core.EndDrawing();
    }

}
