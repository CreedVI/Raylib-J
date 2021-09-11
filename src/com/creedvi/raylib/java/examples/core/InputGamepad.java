package com.creedvi.raylib.java.examples.core;

import com.creedvi.raylib.java.rlj.Raylib;
import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.textures.Texture2D;

import static com.creedvi.raylib.java.rlj.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.creedvi.raylib.java.rlj.core.input.Gamepad.GamepadAxis.*;
import static com.creedvi.raylib.java.rlj.core.input.Gamepad.GamepadButton.*;

public class InputGamepad{

    static String XBOX360_LEGACY_NAME_ID = "Xbox Controller";
    static String XBOX360_NAME_ID = "Xbox 360 Controller";
    static String XBOX1S_NAME_ID = "Microsoft X-Box One S pad";
    static String PS3_NAME_ID = "PLAYSTATION(R)3 Controller";
    static String PS4_NAME_ID = "Sony Interactive Entertainment Wireless Controller";

    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT);  // Set MSAA 4X hint before windows creation

        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [core] example - gamepad input");

        Texture2D texPs3Pad = rlj.textures.LoadTexture("resources/ps3.png");
        Texture2D texXboxPad = rlj.textures.LoadTexture("resources/xbox.png");

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {   // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            // ...
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            if (rlj.core.IsGamepadAvailable(0)){
                rlj.text.DrawText("GP1: " + rlj.core.GetGamepadName(0), 10, 10, 10, Color.BLACK);

                if (rlj.core.IsGamepadName(0, XBOX360_NAME_ID) || rlj.core.IsGamepadName(0, XBOX360_LEGACY_NAME_ID)
                        || rlj.core.IsGamepadName(0, XBOX1S_NAME_ID)){
                    rlj.textures.DrawTexture(texXboxPad, 0, 0, Color.DARKGRAY);

                    // Draw buttons: xbox home
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE)) rlj.shapes.DrawCircle(394, 89, 19, Color.RED);

                    // Draw buttons: basic
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE_RIGHT)){
                        rlj.shapes.DrawCircle(436, 150, 9, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE_LEFT)){
                        rlj.shapes.DrawCircle(352, 150, 9, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_LEFT)){
                        rlj.shapes.DrawCircle(501, 151, 15, Color.BLUE);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_DOWN)){
                        rlj.shapes.DrawCircle(536, 187, 15, Color.LIME);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_RIGHT)){
                        rlj.shapes.DrawCircle(572, 151, 15, Color.MAROON);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_UP)){
                        rlj.shapes.DrawCircle(536, 115, 15, Color.GOLD);
                    }

                    // Draw buttons: d-pad
                    rlj.shapes.DrawRectangle(317, 202, 19, 71, Color.BLACK);
                    rlj.shapes.DrawRectangle(293, 228, 69, 19, Color.BLACK);
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_UP)){
                        rlj.shapes.DrawRectangle(317, 202, 19, 26, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_DOWN)){
                        rlj.shapes.DrawRectangle(317, 202 + 45, 19, 26, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_LEFT)){
                        rlj.shapes.DrawRectangle(292, 228, 25, 19, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_RIGHT)){
                        rlj.shapes.DrawRectangle(292 + 44, 228, 26, 19, Color.RED);
                    }

                    // Draw buttons: left-right back
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(259, 61, 20, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(536, 61, 20, Color.RED);
                    }

                    // Draw axis: left joystick
                    rlj.shapes.DrawCircle(259, 152, 39, Color.BLACK);
                    rlj.shapes.DrawCircle(259, 152, 34, Color.LIGHTGRAY);
                    rlj.shapes.DrawCircle(259 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_X) * 20),
                                          152 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_Y) * 20), 25, Color.BLACK);

                    // Draw axis: right joystick
                    rlj.shapes.DrawCircle(461, 237, 38, Color.BLACK);
                    rlj.shapes.DrawCircle(461, 237, 33, Color.LIGHTGRAY);
                    rlj.shapes.DrawCircle(461 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_X) * 20),
                                          237 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_Y) * 20), 25, Color.BLACK);

                    // Draw axis: left-right triggers
                    rlj.shapes.DrawRectangle(170, 30, 15, 70, Color.GRAY);
                    rlj.shapes.DrawRectangle(604, 30, 15, 70, Color.GRAY);
                    rlj.shapes.DrawRectangle(170, 30, 15, (((1 + (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_TRIGGER)) / 2) * 70), Color.RED);
                    rlj.shapes.DrawRectangle(604, 30, 15, (((1 + (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2) * 70), Color.RED);

                    //rlj.text.DrawText(TextFormat("Xbox axis LT: %02.02f", rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_TRIGGER)), 10, 40, 10, Color.BLACK);
                    //rlj.text.DrawText(TextFormat("Xbox axis RT: %02.02f", rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_TRIGGER)), 10, 60, 10, Color.BLACK);
                }
                else if (rlj.core.IsGamepadName(0, PS3_NAME_ID) || rlj.core.IsGamepadName(0, PS4_NAME_ID)){
                    rlj.textures.DrawTexture(texPs3Pad, 0, 0, Color.DARKGRAY);

                    // Draw buttons: ps
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE)) rlj.shapes.DrawCircle(396, 222, 13, Color.RED);

                    // Draw buttons: basic
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE_LEFT)){
                        rlj.shapes.DrawRectangle(328, 170, 32, 13, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_MIDDLE_RIGHT)){
                        rlj.shapes.DrawTriangle(new Vector2(436, 168), new Vector2(436, 185), new Vector2(464, 177), Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_UP)){
                        rlj.shapes.DrawCircle(557, 144, 13, Color.LIME);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_RIGHT)){
                        rlj.shapes.DrawCircle(586, 173, 13, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_DOWN)){
                        rlj.shapes.DrawCircle(557, 203, 13, Color.VIOLET);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_FACE_LEFT)){
                        rlj.shapes.DrawCircle(527, 173, 13, Color.PINK);
                    }

                    // Draw buttons: d-pad
                    rlj.shapes.DrawRectangle(225, 132, 24, 84, Color.BLACK);
                    rlj.shapes.DrawRectangle(195, 161, 84, 25, Color.BLACK);
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_UP)){
                        rlj.shapes.DrawRectangle(225, 132, 24, 29, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_DOWN)){
                        rlj.shapes.DrawRectangle(225, 132 + 54, 24, 30, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_LEFT)){
                        rlj.shapes.DrawRectangle(195, 161, 30, 25, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_FACE_RIGHT)){
                        rlj.shapes.DrawRectangle(195 + 54, 161, 30, 25, Color.RED);
                    }

                    // Draw buttons: left-right back buttons
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_LEFT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(239, 82, 20, Color.RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(0, GAMEPAD_BUTTON_RIGHT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(557, 82, 20, Color.RED);
                    }

                    // Draw axis: left joystick
                    rlj.shapes.DrawCircle(319, 255, 35, Color.BLACK);
                    rlj.shapes.DrawCircle(319, 255, 31, Color.LIGHTGRAY);
                    rlj.shapes.DrawCircle(319 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_X) * 20),
                                          255 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_Y) * 20), 25, Color.BLACK);

                    // Draw axis: right joystick
                    rlj.shapes.DrawCircle(475, 255, 35, Color.BLACK);
                    rlj.shapes.DrawCircle(475, 255, 31, Color.LIGHTGRAY);
                    rlj.shapes.DrawCircle(475 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_X) * 20),
                                          255 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_Y) * 20), 25, Color.BLACK);

                    // Draw axis: left-right triggers
                    rlj.shapes.DrawRectangle(169, 48, 15, 70, Color.GRAY);
                    rlj.shapes.DrawRectangle(611, 48, 15, 70, Color.GRAY);
                    rlj.shapes.DrawRectangle(169, 48, 15, (((1 - (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_TRIGGER)) / 2) * 70), Color.RED);
                    rlj.shapes.DrawRectangle(611, 48, 15, (((1 - (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2) * 70), Color.RED);
                }
                else{
                    rlj.text.DrawText("- GENERIC GAMEPAD -", 280, 180, 20, Color.GRAY);

                    // TODO: Draw generic gamepad
                }

                rlj.text.DrawText("DETECTED AXIS [" + rlj.core.GetGamepadAxisCount(0) + "]:", 10, 50, 10, Color.MAROON);

                for (int i = 0; i < rlj.core.GetGamepadAxisCount(0); i++){
                    rlj.text.DrawText("AXIS " + i + ": " + rlj.core.GetGamepadAxisMovement(0, i), 20, 70 + 20 * i, 10, Color.DARKGRAY);
                }

                if (rlj.core.GetGamepadButtonPressed() != -1){
                    rlj.text.DrawText("DETECTED BUTTON: " + rlj.core.GetGamepadButtonPressed(), 10, 430, 10, Color.RED);
                }
                else{
                    rlj.text.DrawText("DETECTED BUTTON: NONE", 10, 430, 10, Color.GRAY);
                }
            }
            else{
                rlj.text.DrawText("GP1: NOT DETECTED", 10, 10, 10, Color.GRAY);

                rlj.textures.DrawTexture(texXboxPad, 0, 0, Color.LIGHTGRAY);
            }

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.textures.UnloadTexture(texPs3Pad);
        rlj.textures.UnloadTexture(texXboxPad);
    }

}
