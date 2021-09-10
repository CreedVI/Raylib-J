package com.raylib.examples.core;

import com.raylib.Raylib;
import com.raylib.core.Color;
import com.raylib.utils.Tracelog;

import java.time.LocalTime;

public class CustomLogging{

    /*******************************************************************************************
     *
     *   raylib [core] example - Custom logging
     *
     *   This example has been created using raylib 2.1 (www.raylib.com)
     *   raylib is licensed under an unmodified zlib/libpng license (View raylib.h for details)
     *
     *   Example contributed by Pablo Marcos Oltra (@pamarcos) and reviewed by Ramon Santamaria (@raysan5)
     *
     *   Copyright (c) 2018 Pablo Marcos Oltra (@pamarcos) and Ramon Santamaria (@raysan5)
     *
     ********************************************************************************************/

    // Custom logging funtion
    static void LogCustom(Tracelog.TracelogType msgType, String text)
    {
        LocalTime t = LocalTime.now();
        System.out.println(t.toString());

        switch (msgType)
        {
            case LOG_INFO: System.out.print("[INFO] : "); break;
            case LOG_ERROR: System.out.print("[ERROR]: "); break;
            case LOG_WARNING: System.out.print("[WARN] : "); break;
            case LOG_DEBUG: System.out.print("[DEBUG]: "); break;
            default: break;
        }

        System.out.println(text);
    }

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;

        // First thing we do is setting our custom logger to ensure everything raylib logs
        // will use our own logger instead of its internal one
        //Tracelog.SetTraceLogCallback();

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib [core] example - custom logging");

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            // TODO: Update your variables here
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            rlj.text.DrawText("Check out the console output to see the custom logger in action!", 60, 200, 20,
                    Color.LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }
}