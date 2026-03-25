package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.tracelog.TraceLog;
import com.raylib.java.structs.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLogging {

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Window Test
     *
     *   This example has been created using raylib-j (Version 0.5.5)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    public static void main(String[] args) {

        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 450;

        Raylib rlj = new Raylib();
        rlj.core.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raylib-J [core] example -- Custom Logging");
        rlj.core.SetTargetFPS(60);

        rlj.core.SetTraceLogCallback(new CustomLoggerCallback());

        while(!rlj.core.WindowShouldClose()){
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.RAYWHITE);
            rlj.text.DrawText("Congrats! You created your first window!", 190, 200, 20, Color.LIGHTGRAY);
            rlj.core.EndDrawing();
        }

        rlj.core.CloseWindow();
    }

    public static class CustomLoggerCallback implements TraceLog {
        @Override
        public void TRACELOG(TracelogType messageType, String text, Object... args) {
            StringBuilder logMessage = new StringBuilder();

            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String now = LocalDateTime.now().format(format);
            logMessage.append(String.format("[%s] ", now));

            if (messageType != null) {
                switch (messageType) {
                    case LOG_INFO:
                        logMessage.append("INFO :: ");
                        break;
                    case LOG_ERROR:
                        logMessage.append("ERROR :: ");
                        break;
                    case LOG_WARNING:
                        logMessage.append("WARNING :: ");
                        break;
                    case LOG_DEBUG:
                        logMessage.append("DEBUG :: ");
                        break;
                    default:
                        logMessage.append(String.format("Priority %d :: ", messageType.GetLevel()));
                        break;
                }
            }

            logMessage.append(String.format(text, args));

            System.out.println(logMessage);
        }
    }

}
