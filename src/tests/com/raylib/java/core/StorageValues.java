package com.raylib.java.core;/*
package core;

import com.raylib.java.Config;
import com.raylib.java.Raylib;
import com.raylib.java.core.input.Keyboard;
import com.raylib.java.utils.rlj.files;

import java.io.IOException;

import static com.raylib.java.Config.STORAGE_DATA_FILE;
import static com.raylib.java.structs.Color.*;
import static com.raylib.java.core.input.Keyboard.KEY_ENTER;
import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.utils.rlj.files.SaveFileData;
import static com.raylib.java.utils.Tracelog.logger;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;
import static core.StorageValues.StorageData.STORAGE_POSITION_HISCORE;
import static core.StorageValues.StorageData.STORAGE_POSITION_SCORE;

public class StorageValues{

    static class StorageData{
        final static int
            STORAGE_POSITION_SCORE = 0,
            STORAGE_POSITION_HISCORE = 1;
    }

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - storage save/load values");

        int score = 0;
        int hiscore = 0;
        int framesCounter = 0;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(Keyboard.KEY_R)){
                score = rlj.core.GetRandomValue(1000, 2000);
                hiscore = rlj.core.GetRandomValue(2000, 4000);
            }

            if (rlj.core.IsKeyPressed(KEY_ENTER)){
                SaveStorageValue(STORAGE_POSITION_SCORE, score);
                SaveStorageValue(STORAGE_POSITION_HISCORE, hiscore);
            }
            else if (rlj.core.IsKeyPressed(KEY_SPACE)){
                // NOTE: If requested position could not be found, value 0 is returned
                score = LoadStorageValue(STORAGE_POSITION_SCORE);
                hiscore = LoadStorageValue(STORAGE_POSITION_HISCORE);
            }

            framesCounter++;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            rlj.text.DrawText("SCORE: " + score, 280, 130, 40, MAROON);
            rlj.text.DrawText("HI-SCORE: " + hiscore, 210, 200, 50, BLACK);

            rlj.text.DrawText("frames: " + framesCounter, 10, 10, 20, LIME);

            rlj.text.DrawText("Press R to generate random numbers", 220, 40, 20, LIGHTGRAY);
            rlj.text.DrawText("Press ENTER to SAVE values", 250, 310, 20, LIGHTGRAY);
            rlj.text.DrawText("Press SPACE to LOAD values", 252, 350, 20, LIGHTGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        //TODO.txt: Unload values here
        //--------------------------------------------------------------------------------------

    }

    // Save integer value to storage file (to defined position)
    // NOTE: Storage positions is directly related to file memory layout (4 bytes each integer)
    static boolean SaveStorageValue(int position, int value) {
        boolean success = false;
        int dataSize = 0;
        int newDataSize = 0;
        byte[] fileData = null;
        try {
            fileData = rlj.files.LoadFileData(Config.STORAGE_DATA_FILE);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] newFileData = null;

        if (fileData != null) {
            if (dataSize <= position) {
                // Increase data size up to position and store value
                newDataSize = (position + 1);
                newFileData = new byte[newDataSize];

                if (newFileData != null) {
                    newFileData[position] = value;
                }
                else {
                    // RL_REALLOC failed
                    logger(LOG_WARNING, "FILEIO: [" + STORAGE_DATA_FILE + "] Failed to realloc data (" + dataSize + "), " +
                            "position in bytes (" + position + ") bigger than actual file size");

                    // We store the old size of the file
                    newFileData = fileData;
                    newDataSize = dataSize;
                }
            }
            else
            {
                // Store the old size of the file
                newFileData = fileData;
                newDataSize = dataSize;

                // Replace value on selected position
                newFileData[position] = value;
            }

            try {
                success = SaveFileData(STORAGE_DATA_FILE, newFileData);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            logger(LOG_INFO, "FILEIO: [" + STORAGE_DATA_FILE + "] Saved storage value: " + value);
        }
        else
        {
            logger(LOG_INFO, "FILEIO: [" + STORAGE_DATA_FILE + "] File created successfully");

            dataSize = (position + 1)*sizeof(int);
            fileData = (char *)RL_MALLOC(dataSize);
            int *dataPtr = (int *)fileData;
            dataPtr[position] = value;

            try {
                success = SaveFileData(STORAGE_DATA_FILE, fileData);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            logger(LOG_INFO, "FILEIO: [" + STORAGE_DATA_FILE + "] Saved storage value: " + value);
        }

        return success;
    }

    // Load integer value from storage file (from defined position)
    // NOTE: If requested position could not be found, value 0 is returned
    static int LoadStorageValue(int position) {
        int value = 0;
        int dataSize = 0;
        byte[] fileData = null;
        try {
            fileData = rlj.files.LoadFileData(Config.STORAGE_DATA_FILE);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (fileData != null) {
            if (dataSize < (position*4)) {
                logger(LOG_WARNING, "FILEIO: [" + STORAGE_DATA_FILE + "] Failed to find storage position: " + position);
            }
            else {
                value = fileData[position];
            }

            logger(LOG_INFO, "FILEIO: [" + STORAGE_DATA_FILE + "] Loaded storage value: " + value);
        }

        return value;
    }

}

*/