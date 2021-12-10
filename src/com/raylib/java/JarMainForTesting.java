package com.raylib.java;

import com.raylib.java.core.Color;

public class JarMainForTesting{

    static Raylib rlj = new Raylib();

    public static void main(String[] args){
        rlj.core.InitWindow(800, 600, null);
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
        rlj.text.DrawText("Hello, World!", 400 - (rlj.text.MeasureText("Hello, World!", 20)/2), 300, 20, Color.DARKGRAY);
        rlj.core.EndDrawing();
    }

}
