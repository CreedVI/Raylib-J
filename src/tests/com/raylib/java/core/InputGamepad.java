package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Texture2D;
import com.raylib.java.structs.Vector2;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.Config.MAX_GAMEPADS;
import static com.raylib.java.core.input.Gamepad.GamepadAxis.*;
import static com.raylib.java.core.input.Gamepad.GamepadButton.*;
import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.structs.Color.*;

public class InputGamepad {

    /*******************************************************************************************
     *
     *   raylib-j [core] example - Input Gamepad
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     ********************************************************************************************/

    final static String XBOX_ALIAS_1 = "xbox";
    final static String XBOX_ALIAS_2 = "x-box";
    final static String PS_ALIAS = "playstation";


    public static void main(String[] args){
        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        Raylib rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT);  // Set MSAA 4X hint before windows creation

        rlj.core.InitWindow(screenWidth, screenHeight, "raylib-j [core] example - gamepad input");

        Texture2D texPs3Pad = rlj.textures.LoadTexture("resources/ps3.png");
        Texture2D texXboxPad = rlj.textures.LoadTexture("resources/xbox.png");


        // Set axis deadzones
        float leftStickDeadzoneX = 0.1f;
        float leftStickDeadzoneY = 0.1f;
        float rightStickDeadzoneX = 0.1f;
        float rightStickDeadzoneY = 0.1f;
        float leftTriggerDeadzone = -0.9f;
        float rightTriggerDeadzone = -0.9f;

        Rectangle vibrateButton;

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        int gamepad = 0;

        // Main game loop
        while (!rlj.core.WindowShouldClose()) {   // Detect window close button or ESC key
            // Update
            //----------------------------------------------------------------------------------
            if (rlj.core.IsKeyPressed(KEY_LEFT) && gamepad > 0) {
                gamepad--;
            }

            if (rlj.core.IsKeyPressed(KEY_RIGHT) && gamepad < MAX_GAMEPADS) {
                gamepad++;
            }

            Vector2 mousePosition = rlj.core.GetMousePosition();

            int vibrateButtonNumber = rlj.core.GetGamepadAxisCount(gamepad);

            vibrateButton = new Rectangle(10, 70.0f + 20*vibrateButtonNumber  + 20, 75, 24);
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && rlj.shapes.CheckCollisionPointRec(mousePosition, vibrateButton)) {
                // rlj.core.SetGamepadVibration(gamepad, 1.0, 1.0, 1.0);
            }

            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(RAYWHITE);

            if (rlj.core.IsGamepadAvailable(gamepad)){
                rlj.text.DrawText("GP" + gamepad + ": " + rlj.core.GetGamepadName(gamepad), 10, 10, 10, BLACK);

                // Get axis values
                float leftStickX = rlj.core.GetGamepadAxisMovement(gamepad, GAMEPAD_AXIS_LEFT_X);
                float leftStickY = rlj.core.GetGamepadAxisMovement(gamepad, GAMEPAD_AXIS_LEFT_Y);
                float rightStickX = rlj.core.GetGamepadAxisMovement(gamepad, GAMEPAD_AXIS_RIGHT_X);
                float rightStickY = rlj.core.GetGamepadAxisMovement(gamepad, GAMEPAD_AXIS_RIGHT_Y);
                float leftTrigger = rlj.core.GetGamepadAxisMovement(gamepad, GAMEPAD_AXIS_LEFT_TRIGGER);
                float rightTrigger = rlj.core.GetGamepadAxisMovement(gamepad, GAMEPAD_AXIS_RIGHT_TRIGGER);

                // Calculate deadzones
                if (leftStickX > -leftStickDeadzoneX && leftStickX < leftStickDeadzoneX) leftStickX = 0.0f;
                if (leftStickY > -leftStickDeadzoneY && leftStickY < leftStickDeadzoneY) leftStickY = 0.0f;
                if (rightStickX > -rightStickDeadzoneX && rightStickX < rightStickDeadzoneX) rightStickX = 0.0f;
                if (rightStickY > -rightStickDeadzoneY && rightStickY < rightStickDeadzoneY) rightStickY = 0.0f;
                if (leftTrigger < leftTriggerDeadzone) leftTrigger = -1.0f;
                if (rightTrigger < rightTriggerDeadzone) rightTrigger = -1.0f;

                if (rlj.core.GetGamepadName(0).toLowerCase().contains(XBOX_ALIAS_1) ||
                        rlj.core.GetGamepadName(0).toLowerCase().contains(XBOX_ALIAS_2)) {

                    rlj.textures.DrawTexture(texXboxPad, 0, 0, DARKGRAY);

                    // Draw buttons: xbox home
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE)) rlj.shapes.DrawCircle(394, 89, 19, RED);

                    // Draw buttons: basic
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE_RIGHT)){
                        rlj.shapes.DrawCircle(436, 150, 9, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE_LEFT)){
                        rlj.shapes.DrawCircle(352, 150, 9, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_LEFT)){
                        rlj.shapes.DrawCircle(501, 151, 15, BLUE);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_DOWN)){
                        rlj.shapes.DrawCircle(536, 187, 15, LIME);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_RIGHT)){
                        rlj.shapes.DrawCircle(572, 151, 15, MAROON);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_UP)){
                        rlj.shapes.DrawCircle(536, 115, 15, GOLD);
                    }

                    // Draw buttons: d-pad
                    rlj.shapes.DrawRectangle(317, 202, 19, 71, BLACK);
                    rlj.shapes.DrawRectangle(293, 228, 69, 19, BLACK);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_UP)){
                        rlj.shapes.DrawRectangle(317, 202, 19, 26, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_DOWN)){
                        rlj.shapes.DrawRectangle(317, 202 + 45, 19, 26, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_LEFT)){
                        rlj.shapes.DrawRectangle(292, 228, 25, 19, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_RIGHT)){
                        rlj.shapes.DrawRectangle(292 + 44, 228, 26, 19, RED);
                    }

                    // Draw buttons: left-right back
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(259, 61, 20, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(536, 61, 20, RED);
                    }

                    // Draw axis: left joystick
                    rlj.shapes.DrawCircle(259, 152, 39, BLACK);
                    rlj.shapes.DrawCircle(259, 152, 34, LIGHTGRAY);
                    rlj.shapes.DrawCircle(259 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_X) * 20),
                                          152 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_Y) * 20), 25, BLACK);

                    // Draw axis: right joystick
                    rlj.shapes.DrawCircle(461, 237, 38, BLACK);
                    rlj.shapes.DrawCircle(461, 237, 33, LIGHTGRAY);
                    rlj.shapes.DrawCircle(461 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_X) * 20),
                                          237 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_Y) * 20), 25, BLACK);

                    // Draw axis: left-right triggers
                    rlj.shapes.DrawRectangle(170, 30, 15, 70, GRAY);
                    rlj.shapes.DrawRectangle(604, 30, 15, 70, GRAY);
                    rlj.shapes.DrawRectangle(170, 30, 15, (((1 + (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_TRIGGER)) / 2) * 70), RED);
                    rlj.shapes.DrawRectangle(604, 30, 15, (((1 + (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2) * 70), RED);

                    //rlj.text.DrawText(TextFormat("Xbox axis LT: %02.02f", rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_TRIGGER)), 10, 40, 10, BLACK);
                    //rlj.text.DrawText(TextFormat("Xbox axis RT: %02.02f", rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_TRIGGER)), 10, 60, 10, BLACK);
                }
                else if (rlj.core.GetGamepadName(0).toLowerCase().contains(PS_ALIAS)){
                    rlj.textures.DrawTexture(texPs3Pad, 0, 0, DARKGRAY);

                    // Draw buttons: ps
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE)) rlj.shapes.DrawCircle(396, 222, 13, RED);

                    // Draw buttons: basic
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE_LEFT)){
                        rlj.shapes.DrawRectangle(328, 170, 32, 13, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE_RIGHT)){
                        rlj.shapes.DrawTriangle(new Vector2(436, 168), new Vector2(436, 185), new Vector2(464, 177), RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_UP)){
                        rlj.shapes.DrawCircle(557, 144, 13, LIME);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_RIGHT)){
                        rlj.shapes.DrawCircle(586, 173, 13, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_DOWN)){
                        rlj.shapes.DrawCircle(557, 203, 13, VIOLET);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_LEFT)){
                        rlj.shapes.DrawCircle(527, 173, 13, PINK);
                    }

                    // Draw buttons: d-pad
                    rlj.shapes.DrawRectangle(225, 132, 24, 84, BLACK);
                    rlj.shapes.DrawRectangle(195, 161, 84, 25, BLACK);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_UP)){
                        rlj.shapes.DrawRectangle(225, 132, 24, 29, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_DOWN)){
                        rlj.shapes.DrawRectangle(225, 132 + 54, 24, 30, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_LEFT)){
                        rlj.shapes.DrawRectangle(195, 161, 30, 25, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_RIGHT)){
                        rlj.shapes.DrawRectangle(195 + 54, 161, 30, 25, RED);
                    }

                    // Draw buttons: left-right back buttons
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(239, 82, 20, RED);
                    }
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_TRIGGER_1)){
                        rlj.shapes.DrawCircle(557, 82, 20, RED);
                    }

                    // Draw axis: left joystick
                    rlj.shapes.DrawCircle(319, 255, 35, BLACK);
                    rlj.shapes.DrawCircle(319, 255, 31, LIGHTGRAY);
                    rlj.shapes.DrawCircle(319 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_X) * 20),
                                          255 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_Y) * 20), 25, BLACK);

                    // Draw axis: right joystick
                    rlj.shapes.DrawCircle(475, 255, 35, BLACK);
                    rlj.shapes.DrawCircle(475, 255, 31, LIGHTGRAY);
                    rlj.shapes.DrawCircle(475 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_X) * 20),
                                          255 + ((int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_Y) * 20), 25, BLACK);

                    // Draw axis: left-right triggers
                    rlj.shapes.DrawRectangle(169, 48, 15, 70, GRAY);
                    rlj.shapes.DrawRectangle(611, 48, 15, 70, GRAY);
                    rlj.shapes.DrawRectangle(169, 48, 15, (((1 - (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_LEFT_TRIGGER)) / 2) * 70), RED);
                    rlj.shapes.DrawRectangle(611, 48, 15, (((1 - (int) rlj.core.GetGamepadAxisMovement(0, GAMEPAD_AXIS_RIGHT_TRIGGER)) / 2) * 70), RED);
                }
                else{
                    rlj.shapes.DrawRectangleRounded(new Rectangle(175, 110, 460, 220), 0.3f, 16, DARKGRAY);

                    // Draw buttons: basic
                    rlj.shapes.DrawCircle(365, 170, 12, RAYWHITE);
                    rlj.shapes.DrawCircle(405, 170, 12, RAYWHITE);
                    rlj.shapes.DrawCircle(445, 170, 12, RAYWHITE);
                    rlj.shapes.DrawCircle(516, 191, 17, RAYWHITE);
                    rlj.shapes.DrawCircle(551, 227, 17, RAYWHITE);
                    rlj.shapes.DrawCircle(587, 191, 17, RAYWHITE);
                    rlj.shapes.DrawCircle(551, 155, 17, RAYWHITE);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE_LEFT)) rlj.shapes.DrawCircle(365, 170, 10, RED);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE)) rlj.shapes.DrawCircle(405, 170, 10, GREEN);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_MIDDLE_RIGHT)) rlj.shapes.DrawCircle(445, 170, 10, BLUE);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_LEFT)) rlj.shapes.DrawCircle(516, 191, 15, GOLD);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_DOWN)) rlj.shapes.DrawCircle(551, 227, 15, BLUE);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_RIGHT)) rlj.shapes.DrawCircle(587, 191, 15, GREEN);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_FACE_UP)) rlj.shapes.DrawCircle(551, 155, 15, RED);

                    // Draw buttons: d-pad
                    rlj.shapes.DrawRectangle(245, 145, 28, 88, RAYWHITE);
                    rlj.shapes.DrawRectangle(215, 174, 88, 29, RAYWHITE);
                    rlj.shapes.DrawRectangle(247, 147, 24, 84, BLACK);
                    rlj.shapes.DrawRectangle(217, 176, 84, 25, BLACK);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_UP)) rlj.shapes.DrawRectangle(247, 147, 24, 29, RED);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_DOWN)) rlj.shapes.DrawRectangle(247, 147 + 54, 24, 30, RED);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_LEFT)) rlj.shapes.DrawRectangle(217, 176, 30, 25, RED);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_FACE_RIGHT)) rlj.shapes.DrawRectangle(217 + 54, 176, 30, 25, RED);

                    // Draw buttons: left-right back
                    rlj.shapes.DrawRectangleRounded(new Rectangle(215, 98, 100, 10), 0.5f, 16, DARKGRAY);
                    rlj.shapes.DrawRectangleRounded(new Rectangle(495, 98, 100, 10), 0.5f, 16, DARKGRAY);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_TRIGGER_1)) rlj.shapes.DrawRectangleRounded(new Rectangle(215, 98, 100, 10), 0.5f, 16, RED);
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_TRIGGER_1)) rlj.shapes.DrawRectangleRounded(new Rectangle(495, 98, 100, 10), 0.5f, 16, RED);

                    // Draw axis: left joystick
                    Color leftGamepadColor = BLACK;
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_LEFT_THUMB)) leftGamepadColor = RED;
                    rlj.shapes.DrawCircle(345, 260, 40, BLACK);
                    rlj.shapes.DrawCircle(345, 260, 35, LIGHTGRAY);
                    rlj.shapes.DrawCircle(345 + (int)(leftStickX*20), 260 + (int)(leftStickY*20), 25, leftGamepadColor);

                    // Draw axis: right joystick
                    Color rightGamepadColor = BLACK;
                    if (rlj.core.IsGamepadButtonDown(gamepad, GAMEPAD_BUTTON_RIGHT_THUMB)) rightGamepadColor = RED;
                    rlj.shapes.DrawCircle(465, 260, 40, BLACK);
                    rlj.shapes.DrawCircle(465, 260, 35, LIGHTGRAY);
                    rlj.shapes.DrawCircle(465 + (int)(rightStickX*20), 260 + (int)(rightStickY*20), 25, rightGamepadColor);

                    // Draw axis: left-right triggers
                    rlj.shapes.DrawRectangle(151, 110, 15, 70, GRAY);
                    rlj.shapes.DrawRectangle(644, 110, 15, 70, GRAY);
                    rlj.shapes.DrawRectangle(151, 110, 15, (int)(((1 + leftTrigger)/2)*70), RED);
                    rlj.shapes.DrawRectangle(644, 110, 15, (int)(((1 + rightTrigger)/2)*70), RED);
                }

                rlj.text.DrawText("DETECTED AXIS [" + rlj.core.GetGamepadAxisCount(0) + "]:", 10, 50, 10, MAROON);

                for (int i = 0; i < rlj.core.GetGamepadAxisCount(0); i++){
                    rlj.text.DrawText("AXIS " + i + ": " + rlj.core.GetGamepadAxisMovement(0, i), 20, 70 + 20 * i, 10, DARKGRAY);
                }

                if (rlj.core.GetGamepadButtonPressed() != -1){
                    rlj.text.DrawText("DETECTED BUTTON: " + rlj.core.GetGamepadButtonPressed(), 10, 430, 10, RED);
                }
                else{
                    rlj.text.DrawText("DETECTED BUTTON: NONE", 10, 430, 10, GRAY);
                }
            }
            else{
                rlj.text.DrawText("GP1: NOT DETECTED", 10, 10, 10, GRAY);

                rlj.textures.DrawTexture(texXboxPad, 0, 0, LIGHTGRAY);
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
