package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Vector2;

import java.nio.charset.StandardCharsets;

import static com.raylib.java.Config.ConfigFlag.FLAG_MSAA_4X_HINT;
import static com.raylib.java.Config.ConfigFlag.FLAG_VSYNC_HINT;
import static com.raylib.java.core.input.Keyboard.KEY_SPACE;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;

public class Unicode{

    /*******************************************************************************************
     *
     *   raylib [text] example - Using unicode with raylib
     *
     *   This example has been created using raylib-j (Version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example written and copyright by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 Vlad Adrian (@demizdor)
     *
     ********************************************************************************************/

    //--------------------------------------------------------------------------------------
    // Global variables
    //--------------------------------------------------------------------------------------
    // Arrays that holds the random emojis
    static class Emoji{
        int index;      // Index inside `emojiCodepoints`
        int message;    // Message index
        Color color;    // Emoji color

        public Emoji(){
            index = 0;
            message = 0;
            color = new Color();
        }
    }

    static class Message{
        String text;
        String language;

        public Message(String t, String l){
            this.text = t;
            this.language = l;
        }
    }

    final static int EMOJI_PER_WIDTH = 8;
    final static int EMOJI_PER_HEIGHT = 4;

    // String containing 180 emoji codepoints separated by a '\0' char
    static String[] emojiCodepoints = {
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0x80}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x80}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x82}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA3}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x83}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x86}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x89}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x8B}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x8E}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x8D}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x98}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x97}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x99}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x9A}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x99,(byte) 0x82}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x97}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA9}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x94}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA8}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x90}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x91}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB6}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x99,(byte) 0x84}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x8F}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA3}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA5}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xAE}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x90}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xAF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xAA}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xAB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB4}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x8C}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x9B}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x9D}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA4}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x92}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x95}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x99,(byte) 0x83}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x91}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x99,(byte) 0x81}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x96}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x9E}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x9F}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA4}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xAD}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA6}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA9}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xAF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xAC}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB0}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB1}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB3}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xAA}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB5}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA1}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xA0}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xAC}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB7}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x92}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x95}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xAE}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA7}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x87}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xA0}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xAB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0xAD}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA7,(byte) 0x90}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x93}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0x88}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0xBF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0xB9}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0xBA}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x80}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0xBB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0xBD}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0xBE}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA4,(byte) 0x96}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0xA9}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xBA}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB8}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xB9}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xBB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xBD}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x99,(byte) 0x80}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x98,(byte) 0xBF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xBE}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xBF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x80}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x83}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x87}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x93}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x9D}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x85}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA5}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x91}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x86}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x94}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x95}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xBD}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xB6}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x92}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA6}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x84}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x9C}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xB0}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x9E}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x90}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x96}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA8}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x9E}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA7,(byte) 0x80}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x96}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x97}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA9}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x93}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x94}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x9F}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x95}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xAD}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xAA}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xAE}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8C,(byte) 0xAF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x99}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x9A}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB3}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x98}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA3}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x97}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xBF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xAB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB1}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x98}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0x9D}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xA0}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xA2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xA5}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xA1}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x9F}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA1}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xA6}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xAA}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8E,(byte) 0x82}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB0}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA7}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xAB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xAF}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xBC}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x9B}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB5}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB6}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xBE}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xB7}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x8D,(byte) 0xBB}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x82}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0x83}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA4}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA5,(byte) 0xA2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0x81}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0x85}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x91,(byte) 0x84}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x8B}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x98}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x93}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x97}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x99}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x9B}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0xA7,(byte) 0xA1}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x9C}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x96,(byte) 0xA4}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x9D}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x9F}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0x8C}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0xA4}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0xA2}, StandardCharsets.UTF_8),
            new String(new byte[] {(byte) 0xF0,(byte) 0x9F,(byte) 0x92,(byte) 0xA3}, StandardCharsets.UTF_8),
    };
    
    static Message[] messages = { 
            // Array containing all of the emojis messages
            new Message(new String(new byte[] {(byte) 0x46, (byte) 0x61, (byte) 0x6C, (byte) 0x73, (byte) 0x63, (byte) 0x68, (byte) 0x65, (byte) 0x73, (byte) 0x20, (byte) 0xC3, (byte) 0x9C, (byte) 0x62, (byte) 0x65, (byte) 0x6E, (byte) 0x20, (byte) 0x76, (byte) 0x6F, (byte) 0x6E, (byte) 0x20, (byte) 0x58, (byte) 0x79, (byte) 0x6C, (byte) 0x6F, (byte) 0x70, (byte) 0x68, (byte) 0x6F, (byte) 0x6E, (byte) 0x6D, (byte) 0x75, (byte) 0x73, (byte) 0x69, (byte) 0x6B, (byte) 0x20, (byte) 0x71, (byte) 0x75, (byte) 0xC3, (byte) 0xA4, (byte) 0x6C, (byte) 0x74, (byte) 0x20, (byte) 0x6A, (byte) 0x65, (byte) 0x64, (byte) 0x65, (byte) 0x6E, (byte) 0x20, (byte) 0x67, (byte) 0x72, (byte) 0xC3, (byte) 0xB6, (byte) 0xC3, (byte) 0x9F, (byte) 0x65, (byte) 0x72, (byte) 0x65, (byte) 0x6E, (byte) 0x20, (byte) 0x5A, (byte) 0x77, (byte) 0x65, (byte) 0x72, (byte) 0x67}, StandardCharsets.UTF_8), "German"),
            new Message(new String(new byte[] {(byte) 0x42, (byte) 0x65, (byte) 0x69, (byte) 0xC3, (byte) 0x9F, (byte) 0x20, (byte) 0x6E, (byte) 0x69, (byte) 0x63, (byte) 0x68, (byte) 0x74, (byte) 0x20, (byte) 0x69, (byte) 0x6E, (byte) 0x20, (byte) 0x64, (byte) 0x69, (byte) 0x65, (byte) 0x20, (byte) 0x48, (byte) 0x61, (byte) 0x6E, (byte) 0x64, (byte) 0x2C, (byte) 0x20, (byte) 0x64, (byte) 0x69, (byte) 0x65, (byte) 0x20, (byte) 0x64, (byte) 0x69, (byte) 0x63, (byte) 0x68, (byte) 0x20, (byte) 0x66, (byte) 0xC3, (byte) 0xBC, (byte) 0x74, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x74, (byte) 0x2E}, StandardCharsets.UTF_8), "German"),
            new Message(new String(new byte[] {(byte) 0x41, (byte) 0x75, (byte) 0xC3, (byte) 0x9F, (byte) 0x65, (byte) 0x72, (byte) 0x6F, (byte) 0x72, (byte) 0x64, (byte) 0x65, (byte) 0x6E, (byte) 0x74, (byte) 0x6C, (byte) 0x69, (byte) 0x63, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0xC3, (byte) 0x9C, (byte) 0x62, (byte) 0x65, (byte) 0x6C, (byte) 0x20, (byte) 0x65, (byte) 0x72, (byte) 0x66, (byte) 0x6F, (byte) 0x72, (byte) 0x64, (byte) 0x65, (byte) 0x72, (byte) 0x6E, (byte) 0x20, (byte) 0x61, (byte) 0x75, (byte) 0xC3, (byte) 0x9F, (byte) 0x65, (byte) 0x72, (byte) 0x6F, (byte) 0x72, (byte) 0x64, (byte) 0x65, (byte) 0x6E, (byte) 0x74, (byte) 0x6C, (byte) 0x69, (byte) 0x63, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x4D, (byte) 0x69, (byte) 0x74, (byte) 0x74, (byte) 0x65, (byte) 0x6C, (byte) 0x2E}, StandardCharsets.UTF_8), "German"),
            new Message(new String(new byte[] {(byte) 0xD4, (byte) 0xBF, (byte) 0xD6, (byte) 0x80, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xB4, (byte) 0x20, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xBA, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xAF, (byte) 0xD5, (byte) 0xAB, (byte) 0x20, (byte) 0xD5, (byte) 0xB8, (byte) 0xD6, (byte) 0x82, (byte) 0xD5, (byte) 0xBF, (byte) 0xD5, (byte) 0xA5, (byte) 0xD5, (byte) 0xAC, (byte) 0x20, (byte) 0xD6, (byte) 0x87, (byte) 0x20, (byte) 0xD5, (byte) 0xAB, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xAE, (byte) 0xD5, (byte) 0xAB, (byte) 0x20, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xB0, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xA3, (byte) 0xD5, (byte) 0xAB, (byte) 0xD5, (byte) 0xBD, (byte) 0xD5, (byte) 0xBF, (byte) 0x20, (byte) 0xD5, (byte) 0xB9, (byte) 0xD5, (byte) 0xA8, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xA5, (byte) 0xD6, (byte) 0x80}, StandardCharsets.UTF_8), "Armenian"),
            new Message(new String(new byte[] {(byte) 0xD4, (byte) 0xB5, (byte) 0xD6, (byte) 0x80, (byte) 0xD5, (byte) 0xA2, (byte) 0x20, (byte) 0xD5, (byte) 0xB8, (byte) 0xD6, (byte) 0x80, (byte) 0x20, (byte) 0xD5, (byte) 0xAF, (byte) 0xD5, (byte) 0xA1, (byte) 0xD6, (byte) 0x81, (byte) 0xD5, (byte) 0xAB, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xA8, (byte) 0x20, (byte) 0xD5, (byte) 0xA5, (byte) 0xD5, (byte) 0xAF, (byte) 0xD5, (byte) 0xA1, (byte) 0xD6, (byte) 0x82, (byte) 0x20, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xBF, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xBC, (byte) 0x2C, (byte) 0x20, (byte) 0xD5, (byte) 0xAE, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xBC, (byte) 0xD5, (byte) 0xA5, (byte) 0xD6, (byte) 0x80, (byte) 0xD5, (byte) 0xA8, (byte) 0x20, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xBD, (byte) 0xD5, (byte) 0xA1, (byte) 0xD6, (byte) 0x81, (byte) 0xD5, (byte) 0xAB, (byte) 0xD5, (byte) 0xB6, (byte) 0x2E, (byte) 0x2E, (byte) 0x2E, (byte) 0x20, (byte) 0xC2, (byte) 0xAB, (byte) 0xD4, (byte) 0xBF, (byte) 0xD5, (byte) 0xB8, (byte) 0xD5, (byte) 0xBF, (byte) 0xD5, (byte) 0xA8, (byte) 0x20, (byte) 0xD5, (byte) 0xB4, (byte) 0xD5, (byte) 0xA5, (byte) 0xD6, (byte) 0x80, (byte) 0xD5, (byte) 0xB8, (byte) 0xD5, (byte) 0xB6, (byte) 0xD6, (byte) 0x81, (byte) 0xD5, (byte) 0xAB, (byte) 0xD6, (byte) 0x81, (byte) 0x20, (byte) 0xD5, (byte) 0xA7, (byte) 0x3A, (byte) 0xC2, (byte) 0xBB}, StandardCharsets.UTF_8), "Armenian"),
            new Message(new String(new byte[] {(byte) 0xD4, (byte) 0xB3, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xBC, (byte) 0xD5, (byte) 0xA8, (byte) 0xD5, (byte) 0x9D, (byte) 0x20, (byte) 0xD5, (byte) 0xA3, (byte) 0xD5, (byte) 0xA1, (byte) 0xD6, (byte) 0x80, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xB6, (byte) 0x2C, (byte) 0x20, (byte) 0xD5, (byte) 0xB1, (byte) 0xD5, (byte) 0xAB, (byte) 0xD6, (byte) 0x82, (byte) 0xD5, (byte) 0xB6, (byte) 0xD5, (byte) 0xA8, (byte) 0xD5, (byte) 0x9D, (byte) 0x20, (byte) 0xD5, (byte) 0xB1, (byte) 0xD5, (byte) 0xB4, (byte) 0xD5, (byte) 0xBC, (byte) 0xD5, (byte) 0xA1, (byte) 0xD5, (byte) 0xB6}, StandardCharsets.UTF_8), "Armenian"),
            new Message(new String(new byte[] {(byte) 0x4A, (byte) 0x65, (byte) 0xC5, (byte) 0xBC, (byte) 0x75, (byte) 0x20, (byte) 0x6B, (byte) 0x6C, (byte) 0xC4, (byte) 0x85, (byte) 0x74, (byte) 0x77, (byte) 0x2C, (byte) 0x20, (byte) 0x73, (byte) 0x70, (byte) 0xC5, (byte) 0x82, (byte) 0xC3, (byte) 0xB3, (byte) 0x64, (byte) 0xC5, (byte) 0xBA, (byte) 0x20, (byte) 0x46, (byte) 0x69, (byte) 0x6E, (byte) 0x6F, (byte) 0x6D, (byte) 0x20, (byte) 0x63, (byte) 0x7A, (byte) 0xC4, (byte) 0x99, (byte) 0xC5, (byte) 0x9B, (byte) 0xC4, (byte) 0x87, (byte) 0x20, (byte) 0x67, (byte) 0x72, (byte) 0x79, (byte) 0x20, (byte) 0x68, (byte) 0x61, (byte) 0xC5, (byte) 0x84, (byte) 0x62, (byte) 0x21}, StandardCharsets.UTF_8), "Polish"),
            new Message(new String(new byte[] {(byte) 0x44, (byte) 0x6F, (byte) 0x62, (byte) 0x72, (byte) 0x79, (byte) 0x6D, (byte) 0x69, (byte) 0x20, (byte) 0x63, (byte) 0x68, (byte) 0xC4, (byte) 0x99, (byte) 0x63, (byte) 0x69, (byte) 0x61, (byte) 0x6D, (byte) 0x69, (byte) 0x20, (byte) 0x6A, (byte) 0x65, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x70, (byte) 0x69, (byte) 0x65, (byte) 0x6B, (byte) 0xC5, (byte) 0x82, (byte) 0x6F, (byte) 0x20, (byte) 0x77, (byte) 0x79, (byte) 0x62, (byte) 0x72, (byte) 0x75, (byte) 0x6B, (byte) 0x6F, (byte) 0x77, (byte) 0x61, (byte) 0x6E, (byte) 0x65, (byte) 0x2E}, StandardCharsets.UTF_8), "Polish"),
            new Message(new String(new byte[] {(byte) 0xC3, (byte) 0x8E, (byte) 0xC8, (byte) 0x9B, (byte) 0x69, (byte) 0x20, (byte) 0x6D, (byte) 0x75, (byte) 0x6C, (byte) 0xC8, (byte) 0x9B, (byte) 0x75, (byte) 0x6D, (byte) 0x65, (byte) 0x73, (byte) 0x63, (byte) 0x20, (byte) 0x63, (byte) 0xC4, (byte) 0x83, (byte) 0x20, (byte) 0x61, (byte) 0x69, (byte) 0x20, (byte) 0x61, (byte) 0x6C, (byte) 0x65, (byte) 0x73, (byte) 0x20, (byte) 0x72, (byte) 0x61, (byte) 0x79, (byte) 0x6C, (byte) 0x69, (byte) 0x62, (byte) 0x2E, (byte) 0xA0, (byte) 0xC8, (byte) 0x98, (byte) 0x69, (byte) 0x20, (byte) 0x73, (byte) 0x70, (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x73, (byte) 0xC4, (byte) 0x83, (byte) 0x20, (byte) 0x61, (byte) 0x69, (byte) 0x20, (byte) 0x6F, (byte) 0x20, (byte) 0x7A, (byte) 0x69, (byte) 0x20, (byte) 0x62, (byte) 0x75, (byte) 0x6E, (byte) 0xC4, (byte) 0x83, (byte) 0x21}, StandardCharsets.UTF_8), "Romanian"),
            new Message(new String(new byte[] {(byte) 0xD0, (byte) 0xAD, (byte) 0xD1, (byte) 0x85, (byte) 0x2C, (byte) 0x20, (byte) 0xD1, (byte) 0x87, (byte) 0xD1, (byte) 0x83, (byte) 0xD0, (byte) 0xB6, (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xBA, (byte) 0x2C, (byte) 0x20, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xB1, (byte) 0xD1, (byte) 0x89, (byte) 0xD0, (byte) 0xB8, (byte) 0xD0, (byte) 0xB9, (byte) 0x20, (byte) 0xD1, (byte) 0x81, (byte) 0xD1, (byte) 0x8A, (byte) 0xD1, (byte) 0x91, (byte) 0xD0, (byte) 0xBC, (byte) 0x20, (byte) 0xD1, (byte) 0x86, (byte) 0xD0, (byte) 0xB5, (byte) 0xD0, (byte) 0xBD, (byte) 0x20, (byte) 0xD1, (byte) 0x88, (byte) 0xD0, (byte) 0xBB, (byte) 0xD1, (byte) 0x8F, (byte) 0xD0, (byte) 0xBF, (byte) 0x20, (byte) 0x28, (byte) 0xD1, (byte) 0x8E, (byte) 0xD1, (byte) 0x84, (byte) 0xD1, (byte) 0x82, (byte) 0xD1, (byte) 0x8C, (byte) 0x29, (byte) 0x20, (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xB4, (byte) 0xD1, (byte) 0x80, (byte) 0xD1, (byte) 0x8B, (byte) 0xD0, (byte) 0xB7, (byte) 0xD0, (byte) 0xB3, (byte) 0x21}, StandardCharsets.UTF_8), "Russian"),
            new Message(new String(new byte[] {(byte) 0xD0, (byte) 0xAF, (byte) 0x20, (byte) 0xD0, (byte) 0xBB, (byte) 0xD1, (byte) 0x8E, (byte) 0xD0, (byte) 0xB1, (byte) 0xD0, (byte) 0xBB, (byte) 0xD1, (byte) 0x8E, (byte) 0x20, (byte) 0x72, (byte) 0x61, (byte) 0x79, (byte) 0x6C, (byte) 0x69, (byte) 0x62, (byte) 0x21}, StandardCharsets.UTF_8), "Russian"),
            new Message(new String(new byte[] {(byte) 0xD0, (byte) 0x9C, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xBB, (byte) 0xD1, (byte) 0x87, (byte) 0xD0, (byte) 0xB8, (byte) 0x2C, (byte) 0x20, (byte) 0xD1, (byte) 0x81, (byte) 0xD0, (byte) 0xBA, (byte) 0xD1, (byte) 0x80, (byte) 0xD1, (byte) 0x8B, (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xB9, (byte) 0xD1, (byte) 0x81, (byte) 0xD1, (byte) 0x8F, (byte) 0x20, (byte) 0xD0, (byte) 0xB8, (byte) 0x20, (byte) 0xD1, (byte) 0x82, (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xB8, (byte) 0xA0, (byte) 0xD0, (byte) 0x98, (byte) 0x20, (byte) 0xD1, (byte) 0x87, (byte) 0xD1, (byte) 0x83, (byte) 0xD0, (byte) 0xB2, (byte) 0xD1, (byte) 0x81, (byte) 0xD1, (byte) 0x82, (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xB0, (byte) 0x20, (byte) 0xD0, (byte) 0xB8, (byte) 0x20, (byte) 0xD0, (byte) 0xBC, (byte) 0xD0, (byte) 0xB5, (byte) 0xD1, (byte) 0x87, (byte) 0xD1, (byte) 0x82, (byte) 0xD1, (byte) 0x8B, (byte) 0x20, (byte) 0xD1, (byte) 0x81, (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xB8, (byte) 0x20, (byte) 0xE2, (byte) 0x80, (byte) 0x93, (byte) 0xA0, (byte) 0xD0, (byte) 0x9F, (byte) 0xD1, (byte) 0x83, (byte) 0xD1, (byte) 0x81, (byte) 0xD0, (byte) 0xBA, (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xB9, (byte) 0x20, (byte) 0xD0, (byte) 0xB2, (byte) 0x20, (byte) 0xD0, (byte) 0xB4, (byte) 0xD1, (byte) 0x83, (byte) 0xD1, (byte) 0x88, (byte) 0xD0, (byte) 0xB5, (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xBD, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xB9, (byte) 0x20, (byte) 0xD0, (byte) 0xB3, (byte) 0xD0, (byte) 0xBB, (byte) 0xD1, (byte) 0x83, (byte) 0xD0, (byte) 0xB1, (byte) 0xD0, (byte) 0xB8, (byte) 0xD0, (byte) 0xBD, (byte) 0xD0, (byte) 0xB5, (byte) 0xA0, (byte) 0xD0, (byte) 0x98, (byte) 0x20, (byte) 0xD0, (byte) 0xB2, (byte) 0xD1, (byte) 0x81, (byte) 0xD1, (byte) 0x85, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xB4, (byte) 0xD1, (byte) 0x8F, (byte) 0xD1, (byte) 0x82, (byte) 0x20, (byte) 0xD0, (byte) 0xB8, (byte) 0x20, (byte) 0xD0, (byte) 0xB7, (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xB9, (byte) 0xD0, (byte) 0xB4, (byte) 0xD1, (byte) 0x83, (byte) 0xD1, (byte) 0x82, (byte) 0x20, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xBD, (byte) 0xD0, (byte) 0xB5, (byte) 0xA0, (byte) 0xD0, (byte) 0x9A, (byte) 0xD0, (byte) 0xB0, (byte) 0xD0, (byte) 0xBA, (byte) 0x20, (byte) 0xD0, (byte) 0xB7, (byte) 0xD0, (byte) 0xB2, (byte) 0xD0, (byte) 0xB5, (byte) 0xD0, (byte) 0xB7, (byte) 0xD0, (byte) 0xB4, (byte) 0xD1, (byte) 0x8B, (byte) 0x20, (byte) 0xD1, (byte) 0x8F, (byte) 0xD1, (byte) 0x81, (byte) 0xD0, (byte) 0xBD, (byte) 0xD1, (byte) 0x8B, (byte) 0xD0, (byte) 0xB5, (byte) 0x20, (byte) 0xD0, (byte) 0xB2, (byte) 0x20, (byte) 0xD0, (byte) 0xBD, (byte) 0xD0, (byte) 0xBE, (byte) 0xD1, (byte) 0x87, (byte) 0xD0, (byte) 0xB8, (byte) 0x2D, (byte) 0xA0, (byte) 0xD0, (byte) 0x9B, (byte) 0xD1, (byte) 0x8E, (byte) 0xD0, (byte) 0xB1, (byte) 0xD1, (byte) 0x83, (byte) 0xD0, (byte) 0xB9, (byte) 0xD1, (byte) 0x81, (byte) 0xD1, (byte) 0x8F, (byte) 0x20, (byte) 0xD0, (byte) 0xB8, (byte) 0xD0, (byte) 0xBC, (byte) 0xD0, (byte) 0xB8, (byte) 0x20, (byte) 0xE2, (byte) 0x80, (byte) 0x93, (byte) 0x20, (byte) 0xD0, (byte) 0xB8, (byte) 0x20, (byte) 0xD0, (byte) 0xBC, (byte) 0xD0, (byte) 0xBE, (byte) 0xD0, (byte) 0xBB, (byte) 0xD1, (byte) 0x87, (byte) 0xD0, (byte) 0xB8, (byte) 0x2E}, StandardCharsets.UTF_8), "Russian"),
            new Message(new String(new byte[] {(byte) 0x56, (byte) 0x6F, (byte) 0x69, (byte) 0x78, (byte) 0x20, (byte) 0x61, (byte) 0x6D, (byte) 0x62, (byte) 0x69, (byte) 0x67, (byte) 0x75, (byte) 0xC3, (byte) 0xAB, (byte) 0x20, (byte) 0x64, (byte) 0xE2, (byte) 0x80, (byte) 0x99, (byte) 0x75, (byte) 0x6E, (byte) 0x20, (byte) 0x63, (byte) 0xC5, (byte) 0x93, (byte) 0x75, (byte) 0x72, (byte) 0x20, (byte) 0x71, (byte) 0x75, (byte) 0x69, (byte) 0x20, (byte) 0x61, (byte) 0x75, (byte) 0x20, (byte) 0x7A, (byte) 0xC3, (byte) 0xA9, (byte) 0x70, (byte) 0x68, (byte) 0x79, (byte) 0x72, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0xC3, (byte) 0xA9, (byte) 0x66, (byte) 0xC3, (byte) 0xA8, (byte) 0x72, (byte) 0x65, (byte) 0x20, (byte) 0x6C, (byte) 0x65, (byte) 0x73, (byte) 0x20, (byte) 0x6A, (byte) 0x61, (byte) 0x74, (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x20, (byte) 0x64, (byte) 0x65, (byte) 0x20, (byte) 0x6B, (byte) 0x69, (byte) 0x77, (byte) 0x69}, StandardCharsets.UTF_8), "French"),
            new Message(new String(new byte[] {(byte) 0x42, (byte) 0x65, (byte) 0x6E, (byte) 0x6A, (byte) 0x61, (byte) 0x6D, (byte) 0xC3, (byte) 0xAD, (byte) 0x6E, (byte) 0x20, (byte) 0x70, (byte) 0x69, (byte) 0x64, (byte) 0x69, (byte) 0xC3, (byte) 0xB3, (byte) 0x20, (byte) 0x75, (byte) 0x6E, (byte) 0x61, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x62, (byte) 0x69, (byte) 0x64, (byte) 0x61, (byte) 0x20, (byte) 0x64, (byte) 0x65, (byte) 0x20, (byte) 0x6B, (byte) 0x69, (byte) 0x77, (byte) 0x69, (byte) 0x20, (byte) 0x79, (byte) 0x20, (byte) 0x66, (byte) 0x72, (byte) 0x65, (byte) 0x73, (byte) 0x61, (byte) 0x3B, (byte) 0x20, (byte) 0x4E, (byte) 0x6F, (byte) 0xC3, (byte) 0xA9, (byte) 0x2C, (byte) 0x20, (byte) 0x73, (byte) 0x69, (byte) 0x6E, (byte) 0x20, (byte) 0x76, (byte) 0x65, (byte) 0x72, (byte) 0x67, (byte) 0xC3, (byte) 0xBC, (byte) 0x65, (byte) 0x6E, (byte) 0x7A, (byte) 0x61, (byte) 0x2C, (byte) 0x20, (byte) 0x6C, (byte) 0x61, (byte) 0x20, (byte) 0x6D, (byte) 0xC3, (byte) 0xA1, (byte) 0x73, (byte) 0x20, (byte) 0x65, (byte) 0x78, (byte) 0x71, (byte) 0x75, (byte) 0x69, (byte) 0x73, (byte) 0x69, (byte) 0x74, (byte) 0x61, (byte) 0x20, (byte) 0x63, (byte) 0x68, (byte) 0x61, (byte) 0x6D, (byte) 0x70, (byte) 0x61, (byte) 0xC3, (byte) 0xB1, (byte) 0x61, (byte) 0x20, (byte) 0x64, (byte) 0x65, (byte) 0x6C, (byte) 0x20, (byte) 0x6D, (byte) 0x65, (byte) 0x6E, (byte) 0xC3, (byte) 0xBA, (byte) 0x2E}, StandardCharsets.UTF_8), "Spanish"),
            new Message(new String(new byte[] {(byte) 0xCE, (byte) 0xA4, (byte) 0xCE, (byte) 0xB1, (byte) 0xCF, (byte) 0x87, (byte) 0xCE, (byte) 0xAF, (byte) 0xCF, (byte) 0x83, (byte) 0xCF, (byte) 0x84, (byte) 0xCE, (byte) 0xB7, (byte) 0x20, (byte) 0xCE, (byte) 0xB1, (byte) 0xCE, (byte) 0xBB, (byte) 0xCF, (byte) 0x8E, (byte) 0xCF, (byte) 0x80, (byte) 0xCE, (byte) 0xB7, (byte) 0xCE, (byte) 0xBE, (byte) 0x20, (byte) 0xCE, (byte) 0xB2, (byte) 0xCE, (byte) 0xB1, (byte) 0xCF, (byte) 0x86, (byte) 0xCE, (byte) 0xAE, (byte) 0xCF, (byte) 0x82, (byte) 0x20, (byte) 0xCF, (byte) 0x88, (byte) 0xCE, (byte) 0xB7, (byte) 0xCE, (byte) 0xBC, (byte) 0xCE, (byte) 0xAD, (byte) 0xCE, (byte) 0xBD, (byte) 0xCE, (byte) 0xB7, (byte) 0x20, (byte) 0xCE, (byte) 0xB3, (byte) 0xCE, (byte) 0xB7, (byte) 0x2C, (byte) 0x20, (byte) 0xCE, (byte) 0xB4, (byte) 0xCF, (byte) 0x81, (byte) 0xCE, (byte) 0xB1, (byte) 0xCF, (byte) 0x83, (byte) 0xCE, (byte) 0xBA, (byte) 0xCE, (byte) 0xB5, (byte) 0xCE, (byte) 0xBB, (byte) 0xCE, (byte) 0xAF, (byte) 0xCE, (byte) 0xB6, (byte) 0xCE, (byte) 0xB5, (byte) 0xCE, (byte) 0xB9, (byte) 0x20, (byte) 0xCF, (byte) 0x85, (byte) 0xCF, (byte) 0x80, (byte) 0xCE, (byte) 0xAD, (byte) 0xCF, (byte) 0x81, (byte) 0x20, (byte) 0xCE, (byte) 0xBD, (byte) 0xCF, (byte) 0x89, (byte) 0xCE, (byte) 0xB8, (byte) 0xCF, (byte) 0x81, (byte) 0xCE, (byte) 0xBF, (byte) 0xCF, (byte) 0x8D, (byte) 0x20, (byte) 0xCE, (byte) 0xBA, (byte) 0xCF, (byte) 0x85, (byte) 0xCE, (byte) 0xBD, (byte) 0xCF, (byte) 0x8C, (byte) 0xCF, (byte) 0x82}, StandardCharsets.UTF_8), "Greek"),
            new Message(new String(new byte[] {(byte) 0xCE, (byte) 0x97, (byte) 0x20, (byte) 0xCE, (byte) 0xBA, (byte) 0xCE, (byte) 0xB1, (byte) 0xCE, (byte) 0xBB, (byte) 0xCF, (byte) 0x8D, (byte) 0xCF, (byte) 0x84, (byte) 0xCE, (byte) 0xB5, (byte) 0xCF, (byte) 0x81, (byte) 0xCE, (byte) 0xB7, (byte) 0x20, (byte) 0xCE, (byte) 0xAC, (byte) 0xCE, (byte) 0xBC, (byte) 0xCF, (byte) 0x85, (byte) 0xCE, (byte) 0xBD, (byte) 0xCE, (byte) 0xB1, (byte) 0x20, (byte) 0xCE, (byte) 0xB5, (byte) 0xCE, (byte) 0xAF, (byte) 0xCE, (byte) 0xBD, (byte) 0xCE, (byte) 0xB1, (byte) 0xCE, (byte) 0xB9, (byte) 0x20, (byte) 0xCE, (byte) 0xB7, (byte) 0x20, (byte) 0xCE, (byte) 0xB5, (byte) 0xCF, (byte) 0x80, (byte) 0xCE, (byte) 0xAF, (byte) 0xCE, (byte) 0xB8, (byte) 0xCE, (byte) 0xB5, (byte) 0xCF, (byte) 0x83, (byte) 0xCE, (byte) 0xB7, (byte) 0x2E}, StandardCharsets.UTF_8), "Greek"),
            new Message(new String(new byte[] {(byte) 0xCE, (byte) 0xA7, (byte) 0xCF, (byte) 0x81, (byte) 0xCF, (byte) 0x8C, (byte) 0xCE, (byte) 0xBD, (byte) 0xCE, (byte) 0xB9, (byte) 0xCE, (byte) 0xB1, (byte) 0x20, (byte) 0xCE, (byte) 0xBA, (byte) 0xCE, (byte) 0xB1, (byte) 0xCE, (byte) 0xB9, (byte) 0x20, (byte) 0xCE, (byte) 0xB6, (byte) 0xCE, (byte) 0xB1, (byte) 0xCE, (byte) 0xBC, (byte) 0xCE, (byte) 0xAC, (byte) 0xCE, (byte) 0xBD, (byte) 0xCE, (byte) 0xB9, (byte) 0xCE, (byte) 0xB1, (byte) 0x21}, StandardCharsets.UTF_8), "Greek"),
            new Message(new String(new byte[] {(byte) 0xCE, (byte) 0xA0, (byte) 0xCF, (byte) 0x8E, (byte) 0xCF, (byte) 0x82, (byte) 0x20, (byte) 0xCF, (byte) 0x84, (byte) 0xCE, (byte) 0xB1, (byte) 0x20, (byte) 0xCF, (byte) 0x80, (byte) 0xCE, (byte) 0xB1, (byte) 0xCF, (byte) 0x82, (byte) 0x20, (byte) 0xCF, (byte) 0x83, (byte) 0xCE, (byte) 0xAE, (byte) 0xCE, (byte) 0xBC, (byte) 0xCE, (byte) 0xB5, (byte) 0xCF, (byte) 0x81, (byte) 0xCE, (byte) 0xB1, (byte) 0x3B}, StandardCharsets.UTF_8), "Greek"),

            new Message(new String(new byte[] {(byte) 0xE6, (byte) 0x88, (byte) 0x91, (byte) 0xE8, (byte) 0x83, (byte) 0xBD, (byte) 0xE5, (byte) 0x90, (byte) 0x9E, (byte) 0xE4, (byte) 0xB8, (byte) 0x8B, (byte) 0xE7, (byte) 0x8E, (byte) 0xBB, (byte) 0xE7, (byte) 0x92, (byte) 0x83, (byte) 0xE8, (byte) 0x80, (byte) 0x8C, (byte) 0xE4, (byte) 0xB8, (byte) 0x8D, (byte) 0xE4, (byte) 0xBC, (byte) 0xA4, (byte) 0xE8, (byte) 0xBA, (byte) 0xAB, (byte) 0xE4, (byte) 0xBD, (byte) 0x93, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE4, (byte) 0xBD, (byte) 0xA0, (byte) 0xE5, (byte) 0x90, (byte) 0x83, (byte) 0xE4, (byte) 0xBA, (byte) 0x86, (byte) 0xE5, (byte) 0x90, (byte) 0x97, (byte) 0xEF, (byte) 0xBC, (byte) 0x9F}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE4, (byte) 0xB8, (byte) 0x8D, (byte) 0xE4, (byte) 0xBD, (byte) 0x9C, (byte) 0xE4, (byte) 0xB8, (byte) 0x8D, (byte) 0xE6, (byte) 0xAD, (byte) 0xBB, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE6, (byte) 0x9C, (byte) 0x80, (byte) 0xE8, (byte) 0xBF, (byte) 0x91, (byte) 0xE5, (byte) 0xA5, (byte) 0xBD, (byte) 0xE5, (byte) 0x90, (byte) 0x97, (byte) 0xEF, (byte) 0xBC, (byte) 0x9F}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE5, (byte) 0xA1, (byte) 0x9E, (byte) 0xE7, (byte) 0xBF, (byte) 0x81, (byte) 0xE5, (byte) 0xA4, (byte) 0xB1, (byte) 0xE9, (byte) 0xA9, (byte) 0xAC, (byte) 0xEF, (byte) 0xBC, (byte) 0x8C, (byte) 0xE7, (byte) 0x84, (byte) 0x89, (byte) 0xE7, (byte) 0x9F, (byte) 0xA5, (byte) 0xE9, (byte) 0x9D, (byte) 0x9E, (byte) 0xE7, (byte) 0xA6, (byte) 0x8F, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE5, (byte) 0x8D, (byte) 0x83, (byte) 0xE5, (byte) 0x86, (byte) 0x9B, (byte) 0xE6, (byte) 0x98, (byte) 0x93, (byte) 0xE5, (byte) 0xBE, (byte) 0x97, (byte) 0x2C, (byte) 0x20, (byte) 0xE4, (byte) 0xB8, (byte) 0x80, (byte) 0xE5, (byte) 0xB0, (byte) 0x86, (byte) 0xE9, (byte) 0x9A, (byte) 0xBE, (byte) 0xE6, (byte) 0xB1, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE4, (byte) 0xB8, (byte) 0x87, (byte) 0xE4, (byte) 0xBA, (byte) 0x8B, (byte) 0xE5, (byte) 0xBC, (byte) 0x80, (byte) 0xE5, (byte) 0xA4, (byte) 0xB4, (byte) 0xE9, (byte) 0x9A, (byte) 0xBE, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE9, (byte) 0xA3, (byte) 0x8E, (byte) 0xE6, (byte) 0x97, (byte) 0xA0, (byte) 0xE5, (byte) 0xB8, (byte) 0xB8, (byte) 0xE9, (byte) 0xA1, (byte) 0xBA, (byte) 0xEF, (byte) 0xBC, (byte) 0x8C, (byte) 0xE5, (byte) 0x85, (byte) 0xB5, (byte) 0xE6, (byte) 0x97, (byte) 0xA0, (byte) 0xE5, (byte) 0xB8, (byte) 0xB8, (byte) 0xE8, (byte) 0x83, (byte) 0x9C, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE6, (byte) 0xB4, (byte) 0xBB, (byte) 0xE5, (byte) 0x88, (byte) 0xB0, (byte) 0xE8, (byte) 0x80, (byte) 0x81, (byte) 0xEF, (byte) 0xBC, (byte) 0x8C, (byte) 0xE5, (byte) 0xAD, (byte) 0xA6, (byte) 0xE5, (byte) 0x88, (byte) 0xB0, (byte) 0xE8, (byte) 0x80, (byte) 0x81, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE4, (byte) 0xB8, (byte) 0x80, (byte) 0xE8, (byte) 0xA8, (byte) 0x80, (byte) 0xE6, (byte) 0x97, (byte) 0xA2, (byte) 0xE5, (byte) 0x87, (byte) 0xBA, (byte) 0xEF, (byte) 0xBC, (byte) 0x8C, (byte) 0xE9, (byte) 0xA9, (byte) 0xB7, (byte) 0xE9, (byte) 0xA9, (byte) 0xAC, (byte) 0xE9, (byte) 0x9A, (byte) 0xBE, (byte) 0xE8, (byte) 0xBF, (byte) 0xBD, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE8, (byte) 0xB7, (byte) 0xAF, (byte) 0xE9, (byte) 0x81, (byte) 0xA5, (byte) 0xE7, (byte) 0x9F, (byte) 0xA5, (byte) 0xE9, (byte) 0xA9, (byte) 0xAC, (byte) 0xE5, (byte) 0x8A, (byte) 0x9B, (byte) 0xEF, (byte) 0xBC, (byte) 0x8C, (byte) 0xE6, (byte) 0x97, (byte) 0xA5, (byte) 0xE4, (byte) 0xB9, (byte) 0x85, (byte) 0xE8, (byte) 0xA7, (byte) 0x81, (byte) 0xE4, (byte) 0xBA, (byte) 0xBA, (byte) 0xE5, (byte) 0xBF, (byte) 0x83}, StandardCharsets.UTF_8), "Chinese"),
            new Message(new String(new byte[] {(byte) 0xE6, (byte) 0x9C, (byte) 0x89, (byte) 0xE7, (byte) 0x90, (byte) 0x86, (byte) 0xE8, (byte) 0xB5, (byte) 0xB0, (byte) 0xE9, (byte) 0x81, (byte) 0x8D, (byte) 0xE5, (byte) 0xA4, (byte) 0xA9, (byte) 0xE4, (byte) 0xB8, (byte) 0x8B, (byte) 0xEF, (byte) 0xBC, (byte) 0x8C, (byte) 0xE6, (byte) 0x97, (byte) 0xA0, (byte) 0xE7, (byte) 0x90, (byte) 0x86, (byte) 0xE5, (byte) 0xAF, (byte) 0xB8, (byte) 0xE6, (byte) 0xAD, (byte) 0xA5, (byte) 0xE9, (byte) 0x9A, (byte) 0xBE, (byte) 0xE8, (byte) 0xA1, (byte) 0x8C, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Chinese"),

            new Message(new String(new byte[] {(byte) 0xE7, (byte) 0x8C, (byte) 0xBF, (byte) 0xE3, (byte) 0x82, (byte) 0x82, (byte) 0xE6, (byte) 0x9C, (byte) 0xA8, (byte) 0xE3, (byte) 0x81, (byte) 0x8B, (byte) 0xE3, (byte) 0x82, (byte) 0x89, (byte) 0xE8, (byte) 0x90, (byte) 0xBD, (byte) 0xE3, (byte) 0x81, (byte) 0xA1, (byte) 0xE3, (byte) 0x82, (byte) 0x8B}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE4, (byte) 0xBA, (byte) 0x80, (byte) 0xE3, (byte) 0x81, (byte) 0xAE, (byte) 0xE7, (byte) 0x94, (byte) 0xB2, (byte) 0xE3, (byte) 0x82, (byte) 0x88, (byte) 0xE3, (byte) 0x82, (byte) 0x8A, (byte) 0xE5, (byte) 0xB9, (byte) 0xB4, (byte) 0xE3, (byte) 0x81, (byte) 0xAE, (byte) 0xE5, (byte) 0x8A, (byte) 0x9F}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE3, (byte) 0x81, (byte) 0x86, (byte) 0xE3, (byte) 0x82, (byte) 0x89, (byte) 0xE3, (byte) 0x82, (byte) 0x84, (byte) 0xE3, (byte) 0x81, (byte) 0xBE, (byte) 0xE3, (byte) 0x81, (byte) 0x97, (byte) 0x20, (byte) 0x20, (byte) 0xE6, (byte) 0x80, (byte) 0x9D, (byte) 0xE3, (byte) 0x81, (byte) 0xB2, (byte) 0xE5, (byte) 0x88, (byte) 0x87, (byte) 0xE3, (byte) 0x82, (byte) 0x8B, (byte) 0xE6, (byte) 0x99, (byte) 0x82, (byte) 0x20, (byte) 0x20, (byte) 0xE7, (byte) 0x8C, (byte) 0xAB, (byte) 0xE3, (byte) 0x81, (byte) 0xAE, (byte) 0xE6, (byte) 0x81, (byte) 0x8B}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE8, (byte) 0x99, (byte) 0x8E, (byte) 0xE7, (byte) 0xA9, (byte) 0xB4, (byte) 0xE3, (byte) 0x81, (byte) 0xAB, (byte) 0xE5, (byte) 0x85, (byte) 0xA5, (byte) 0xE3, (byte) 0x82, (byte) 0x89, (byte) 0xE3, (byte) 0x81, (byte) 0x9A, (byte) 0xE3, (byte) 0x82, (byte) 0x93, (byte) 0xE3, (byte) 0x81, (byte) 0xB0, (byte) 0xE8, (byte) 0x99, (byte) 0x8E, (byte) 0xE5, (byte) 0xAD, (byte) 0x90, (byte) 0xE3, (byte) 0x82, (byte) 0x92, (byte) 0xE5, (byte) 0xBE, (byte) 0x97, (byte) 0xE3, (byte) 0x81, (byte) 0x9A, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE4, (byte) 0xBA, (byte) 0x8C, (byte) 0xE5, (byte) 0x85, (byte) 0x8E, (byte) 0xE3, (byte) 0x82, (byte) 0x92, (byte) 0xE8, (byte) 0xBF, (byte) 0xBD, (byte) 0xE3, (byte) 0x81, (byte) 0x86, (byte) 0xE8, (byte) 0x80, (byte) 0x85, (byte) 0xE3, (byte) 0x81, (byte) 0xAF, (byte) 0xE4, (byte) 0xB8, (byte) 0x80, (byte) 0xE5, (byte) 0x85, (byte) 0x8E, (byte) 0xE3, (byte) 0x82, (byte) 0x92, (byte) 0xE3, (byte) 0x82, (byte) 0x82, (byte) 0xE5, (byte) 0xBE, (byte) 0x97, (byte) 0xE3, (byte) 0x81, (byte) 0x9A, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE9, (byte) 0xA6, (byte) 0xAC, (byte) 0xE9, (byte) 0xB9, (byte) 0xBF, (byte) 0xE3, (byte) 0x81, (byte) 0xAF, (byte) 0xE6, (byte) 0xAD, (byte) 0xBB, (byte) 0xE3, (byte) 0x81, (byte) 0xAA, (byte) 0xE3, (byte) 0x81, (byte) 0xAA, (byte) 0xE3, (byte) 0x81, (byte) 0x8D, (byte) 0xE3, (byte) 0x82, (byte) 0x83, (byte) 0xE6, (byte) 0xB2, (byte) 0xBB, (byte) 0xE3, (byte) 0x82, (byte) 0x89, (byte) 0xE3, (byte) 0x81, (byte) 0xAA, (byte) 0xE3, (byte) 0x81, (byte) 0x84, (byte) 0xE3, (byte) 0x80, (byte) 0x82}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE6, (byte) 0x9E, (byte) 0xAF, (byte) 0xE9, (byte) 0x87, (byte) 0x8E, (byte) 0xE8, (byte) 0xB7, (byte) 0xAF, (byte) 0xE3, (byte) 0x81, (byte) 0xAB, (byte) 0xE3, (byte) 0x80, (byte) 0x80, (byte) 0xE5, (byte) 0xBD, (byte) 0xB1, (byte) 0xE3, (byte) 0x81, (byte) 0x8B, (byte) 0xE3, (byte) 0x81, (byte) 0x95, (byte) 0xE3, (byte) 0x81, (byte) 0xAA, (byte) 0xE3, (byte) 0x82, (byte) 0x8A, (byte) 0xE3, (byte) 0x81, (byte) 0xA6, (byte) 0xE3, (byte) 0x80, (byte) 0x80, (byte) 0xE3, (byte) 0x82, (byte) 0x8F, (byte) 0xE3, (byte) 0x81, (byte) 0x8B, (byte) 0xE3, (byte) 0x82, (byte) 0x8C, (byte) 0xE3, (byte) 0x81, (byte) 0x91, (byte) 0xE3, (byte) 0x82, (byte) 0x8A}, StandardCharsets.UTF_8), "Japanese"),
            new Message(new String(new byte[] {(byte) 0xE7, (byte) 0xB9, (byte) 0xB0, (byte) 0xE3, (byte) 0x82, (byte) 0x8A, (byte) 0xE8, (byte) 0xBF, (byte) 0x94, (byte) 0xE3, (byte) 0x81, (byte) 0x97, (byte) 0xE9, (byte) 0xBA, (byte) 0xA6, (byte) 0xE3, (byte) 0x81, (byte) 0xAE, (byte) 0xE7, (byte) 0x95, (byte) 0x9D, (byte) 0xE7, (byte) 0xB8, (byte) 0xAB, (byte) 0xE3, (byte) 0x81, (byte) 0xB5, (byte) 0xE8, (byte) 0x83, (byte) 0xA1, (byte) 0xE8, (byte) 0x9D, (byte) 0xB6, (byte) 0xE5, (byte) 0x93, (byte) 0x89}, StandardCharsets.UTF_8), "Japanese"),

            new Message(new String(new byte[] {(byte) 0xEC, (byte) 0x95, (byte) 0x84, (byte) 0xEB, (byte) 0x93, (byte) 0x9D, (byte) 0xED, (byte) 0x95, (byte) 0x9C, (byte) 0x20, (byte) 0xEB, (byte) 0xB0, (byte) 0x94, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4, (byte) 0x20, (byte) 0xEC, (byte) 0x9C, (byte) 0x84, (byte) 0xEC, (byte) 0x97, (byte) 0x90, (byte) 0x20, (byte) 0xEA, (byte) 0xB0, (byte) 0x88, (byte) 0xEB, (byte) 0xA7, (byte) 0xA4, (byte) 0xEA, (byte) 0xB8, (byte) 0xB0, (byte) 0x20, (byte) 0xEB, (byte) 0x91, (byte) 0x90, (byte) 0xEC, (byte) 0x97, (byte) 0x87, (byte) 0x20, (byte) 0xEB, (byte) 0x82, (byte) 0xA0, (byte) 0xEC, (byte) 0x95, (byte) 0x84, (byte) 0x20, (byte) 0xEB, (byte) 0x8F, (byte) 0x88, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4, (byte) 0x2E, (byte) 0xA0, (byte) 0xEB, (byte) 0x84, (byte) 0x88, (byte) 0xED, (byte) 0x9B, (byte) 0x8C, (byte) 0xEB, (byte) 0x84, (byte) 0x88, (byte) 0xED, (byte) 0x9B, (byte) 0x8C, (byte) 0x20, (byte) 0xEC, (byte) 0x8B, (byte) 0x9C, (byte) 0xEB, (byte) 0xA5, (byte) 0xBC, (byte) 0x20, (byte) 0xEC, (byte) 0x93, (byte) 0xB4, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4, (byte) 0x2E, (byte) 0x20, (byte) 0xEB, (byte) 0xAA, (byte) 0xA8, (byte) 0xEB, (byte) 0xA5, (byte) 0xB4, (byte) 0xEB, (byte) 0x8A, (byte) 0x94, (byte) 0x20, (byte) 0xEB, (byte) 0x82, (byte) 0x98, (byte) 0xEB, (byte) 0x9D, (byte) 0xBC, (byte) 0x20, (byte) 0xEA, (byte) 0xB8, (byte) 0x80, (byte) 0xEC, (byte) 0x9E, (byte) 0x90, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4, (byte) 0x2E, (byte) 0xA0, (byte) 0xEB, (byte) 0x84, (byte) 0x90, (byte) 0xEB, (byte) 0x94, (byte) 0xB0, (byte) 0xEB, (byte) 0x9E, (byte) 0x80, (byte) 0x20, (byte) 0xED, (byte) 0x95, (byte) 0x98, (byte) 0xEB, (byte) 0x8A, (byte) 0x98, (byte) 0x20, (byte) 0xEB, (byte) 0xB3, (byte) 0xB5, (byte) 0xED, (byte) 0x8C, (byte) 0x90, (byte) 0xEC, (byte) 0x97, (byte) 0x90, (byte) 0x20, (byte) 0xEB, (byte) 0x82, (byte) 0x98, (byte) 0xEB, (byte) 0x8F, (byte) 0x84, (byte) 0x20, (byte) 0xEA, (byte) 0xB0, (byte) 0x99, (byte) 0xEC, (byte) 0x9D, (byte) 0xB4, (byte) 0x20, (byte) 0xEC, (byte) 0x8B, (byte) 0x9C, (byte) 0xEB, (byte) 0xA5, (byte) 0xBC, (byte) 0x20, (byte) 0xEC, (byte) 0x93, (byte) 0xB4, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4, (byte) 0x2E}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEC, (byte) 0xA0, (byte) 0x9C, (byte) 0x20, (byte) 0xEB, (byte) 0x88, (byte) 0x88, (byte) 0xEC, (byte) 0x97, (byte) 0x90, (byte) 0x20, (byte) 0xEC, (byte) 0x95, (byte) 0x88, (byte) 0xEA, (byte) 0xB2, (byte) 0xBD, (byte) 0xEC, (byte) 0x9D, (byte) 0xB4, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEA, (byte) 0xBF, (byte) 0xA9, (byte) 0x20, (byte) 0xEB, (byte) 0xA8, (byte) 0xB9, (byte) 0xEA, (byte) 0xB3, (byte) 0xA0, (byte) 0x20, (byte) 0xEC, (byte) 0x95, (byte) 0x8C, (byte) 0x20, (byte) 0xEB, (byte) 0xA8, (byte) 0xB9, (byte) 0xEB, (byte) 0x8A, (byte) 0x94, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEB, (byte) 0xA1, (byte) 0x9C, (byte) 0xEB, (byte) 0xA7, (byte) 0x88, (byte) 0xEB, (byte) 0x8A, (byte) 0x94, (byte) 0x20, (byte) 0xED, (byte) 0x95, (byte) 0x98, (byte) 0xEB, (byte) 0xA3, (byte) 0xA8, (byte) 0xEC, (byte) 0x95, (byte) 0x84, (byte) 0xEC, (byte) 0xB9, (byte) 0xA8, (byte) 0xEC, (byte) 0x97, (byte) 0x90, (byte) 0x20, (byte) 0xEC, (byte) 0x9D, (byte) 0xB4, (byte) 0xEB, (byte) 0xA3, (byte) 0xA8, (byte) 0xEC, (byte) 0x96, (byte) 0xB4, (byte) 0xEC, (byte) 0xA7, (byte) 0x84, (byte) 0x20, (byte) 0xEA, (byte) 0xB2, (byte) 0x83, (byte) 0xEC, (byte) 0x9D, (byte) 0xB4, (byte) 0x20, (byte) 0xEC, (byte) 0x95, (byte) 0x84, (byte) 0xEB, (byte) 0x8B, (byte) 0x88, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEA, (byte) 0xB3, (byte) 0xA0, (byte) 0xEC, (byte) 0x83, (byte) 0x9D, (byte) 0x20, (byte) 0xEB, (byte) 0x81, (byte) 0x9D, (byte) 0xEC, (byte) 0x97, (byte) 0x90, (byte) 0x20, (byte) 0xEB, (byte) 0x82, (byte) 0x99, (byte) 0xEC, (byte) 0x9D, (byte) 0xB4, (byte) 0x20, (byte) 0xEC, (byte) 0x98, (byte) 0xA8, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEA, (byte) 0xB0, (byte) 0x9C, (byte) 0xEC, (byte) 0xB2, (byte) 0x9C, (byte) 0xEC, (byte) 0x97, (byte) 0x90, (byte) 0xEC, (byte) 0x84, (byte) 0x9C, (byte) 0x20, (byte) 0xEC, (byte) 0x9A, (byte) 0xA9, (byte) 0x20, (byte) 0xEB, (byte) 0x82, (byte) 0x9C, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEC, (byte) 0x95, (byte) 0x88, (byte) 0xEB, (byte) 0x85, (byte) 0x95, (byte) 0xED, (byte) 0x95, (byte) 0x98, (byte) 0xEC, (byte) 0x84, (byte) 0xB8, (byte) 0xEC, (byte) 0x9A, (byte) 0x94, (byte) 0x3F}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xEB, (byte) 0xA7, (byte) 0x8C, (byte) 0xEB, (byte) 0x82, (byte) 0x98, (byte) 0xEC, (byte) 0x84, (byte) 0x9C, (byte) 0x20, (byte) 0xEB, (byte) 0xB0, (byte) 0x98, (byte) 0xEA, (byte) 0xB0, (byte) 0x91, (byte) 0xEC, (byte) 0x8A, (byte) 0xB5, (byte) 0xEB, (byte) 0x8B, (byte) 0x88, (byte) 0xEB, (byte) 0x8B, (byte) 0xA4}, StandardCharsets.UTF_8), "Korean"),
            new Message(new String(new byte[] {(byte) 0xED, (byte) 0x95, (byte) 0x9C, (byte) 0xEA, (byte) 0xB5, (byte) 0xAD, (byte) 0xEB, (byte) 0xA7, (byte) 0x90, (byte) 0x20, (byte) 0xED, (byte) 0x95, (byte) 0x98, (byte) 0xEC, (byte) 0x8B, (byte) 0xA4, (byte) 0x20, (byte) 0xEC, (byte) 0xA4, (byte) 0x84, (byte) 0x20, (byte) 0xEC, (byte) 0x95, (byte) 0x84, (byte) 0xEC, (byte) 0x84, (byte) 0xB8, (byte) 0xEC, (byte) 0x9A, (byte) 0x94, (byte) 0x3F}, StandardCharsets.UTF_8), "Korean")

    };

    static Emoji[] emoji = new Emoji[EMOJI_PER_WIDTH * EMOJI_PER_HEIGHT];
    static int hovered = -1, selected = -1;
    static Raylib rlj;

    // TODO: Fix

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 450;

        rlj = new Raylib();

        rlj.core.SetConfigFlags(FLAG_MSAA_4X_HINT | FLAG_VSYNC_HINT);
        rlj.core.InitWindow(screenWidth, screenHeight, "raylib [text] example - unicode");

        // Load the font resources
        // NOTE: fontAsian is for asian languages,
        // fontEmoji is the emojis and fontDefault is used for everything else
        Font fontDefault = rlj.text.LoadFont("src/tests/resources/text/dejavu.fnt");
        Font fontAsian = rlj.text.LoadFont("src/tests/resources/text/noto_cjk.fnt");
        Font fontEmoji = rlj.text.LoadFont("src/tests/resources/text/symbola.fnt");

        Vector2 hoveredPos = new Vector2();
        Vector2 selectedPos = new Vector2();

        // Set a random set of emojis when starting up
        RandomizeEmoji();

        rlj.core.SetTargetFPS(60);               // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main loop
        while (!rlj.core.WindowShouldClose()){    // Detect window close button or ESC key

            // Update
            //----------------------------------------------------------------------------------
            // Add a new set of emojis when SPACE is pressed
            if (rlj.core.IsKeyPressed(KEY_SPACE)) {
                RandomizeEmoji();
            }

            // Set the selected emoji and copy its text to clipboard
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && (hovered != -1) && (hovered != selected)) {
                selected = hovered;
                selectedPos.x = hoveredPos.x;
                selectedPos.y = hoveredPos.y;
            }

            Vector2 mouse = rlj.core.GetMousePosition();
            Vector2 pos = new Vector2(28.8f, 10.0f);
            hovered = -1;
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.RAYWHITE);

            // Draw random emojis in the background
            //------------------------------------------------------------------------------
            for (int i = 0; i < emoji.length; ++i) {
                String txt = emojiCodepoints[emoji[i].index];
                Rectangle emojiRect = new Rectangle(pos.x, pos.y, (float) fontEmoji.baseSize, (float) fontEmoji.baseSize);

                if (!rlj.shapes.CheckCollisionPointRec(mouse, emojiRect)) {
                    rlj.text.DrawTextEx(fontEmoji, txt, pos, (float) fontEmoji.baseSize, 1.0f, selected == i ? emoji[i].color :
                            rlj.textures.Fade(Color.LIGHTGRAY, 0.4f));
                }
                else {
                    rlj.text.DrawTextEx(fontEmoji, txt, pos, (float) fontEmoji.baseSize, 1.0f, emoji[i].color);
                    hovered = i;
                    hoveredPos.x = pos.x;
                    hoveredPos.y = pos.y;
                }

                if ((i != 0) && (i % EMOJI_PER_WIDTH == 0)) {
                    pos.y += fontEmoji.baseSize + 24.25f;
                    pos.x = 28.8f;
                }
                else {
                    pos.x += fontEmoji.baseSize + 28.8f;
                }
            }
            //------------------------------------------------------------------------------
            // Draw the message when an emoji is selected
            //------------------------------------------------------------------------------
            if (selected != -1) {
                int message = emoji[selected].message;
                int horizontalPadding = 20;
                int verticalPadding = 30;
                Font font = fontDefault;

                // Set correct font for asian languages
                if (rlj.text.TextIsEqual(messages[message].language, "Chinese") ||
                        rlj.text.TextIsEqual(messages[message].language, "Korean") ||
                        rlj.text.TextIsEqual(messages[message].language, "Japanese")) {
                    font = fontAsian;
                }

                // Calculate size for the message box (approximate the height and width)
                Vector2 sz = rlj.text.MeasureTextEx(font, messages[message].text, (float) font.baseSize, 1.0f);
                if (sz.x > 300) {
                    sz.y *= sz.x / 300;
                    sz.x = 300;
                }
                else if (sz.x < 160) {
                    sz.x = 160;
                }

                Rectangle msgRect = new Rectangle(selectedPos.x - 38.8f, selectedPos.y, 2 * horizontalPadding + sz.x, 2 * verticalPadding + sz.y);
                msgRect.y -= msgRect.height;

                // Coordinates for the chat bubble triangle
                Vector2 a = new Vector2(selectedPos.x, msgRect.y + msgRect.height);
                Vector2 b = new Vector2(a.x + 8, a.y + 10);
                Vector2 c = new Vector2(a.x + 10, a.y);

                // Don't go outside the screen
                if (msgRect.x < 10) {
                    msgRect.x += 28;
                }

                if (msgRect.y < 10) {
                    msgRect.y = selectedPos.y + 84;
                    a.y = msgRect.y;
                    c.y = a.y;
                    b.y = a.y - 10;

                    // Swap values so we can actually render the triangle :(
                    Vector2 tmp = a;
                    a = b;
                    b = tmp;
                }

                if(msgRect.x + msgRect.width > screenWidth) {
                    msgRect.x -= (msgRect.x + msgRect.width) - screenWidth + 10;
                }

                // Draw chat bubble
                rlj.shapes.DrawRectangleRec(msgRect, emoji[selected].color);
                rlj.shapes.DrawTriangle(a, b, c, emoji[selected].color);

                // Draw the main text message
                Rectangle textRect = new Rectangle(msgRect.x + horizontalPadding / 2.0f, msgRect.y + verticalPadding / 2.0f, msgRect.width - horizontalPadding, msgRect.height);
                DrawTextBoxed(font, messages[message].text, textRect, (float) font.baseSize, 1.0f, true, Color.WHITE);

                // Draw the info text below the main message
                int size = rlj.text.GetCodepointCount(messages[message].text);
                int len = messages[message].text.length();
                String info = messages[message].language + " " + len + " characters " + size + " bytes";
                sz = rlj.text.MeasureTextEx(rlj.text.GetFontDefault(), info, 10, 1.0f);
                pos = new Vector2(textRect.x + textRect.width - sz.x, msgRect.y + msgRect.height - sz.y - 2);
                rlj.text.DrawText(info, (int) pos.x, (int) pos.y, 10, Color.RAYWHITE);
            }
            //------------------------------------------------------------------------------

            // Draw the info text
            rlj.text.DrawText("These emojis have something to tell you, click each to find out!", (screenWidth - 650) / 2, screenHeight - 40, 20, Color.GRAY);
            rlj.text.DrawText("Each emoji is a unicode character from a font, not a texture... Press [SPACEBAR] to refresh", (screenWidth - 484) / 2, screenHeight - 16, 10, Color.GRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        rlj.text.UnloadFont(fontDefault);    // Unload font resource
        rlj.text.UnloadFont(fontAsian);      // Unload font resource
        rlj.text.UnloadFont(fontEmoji);      // Unload font resource

        rlj.core.CloseWindow();
        //--------------------------------------------------------------------------------------
    }

    // Fills the emoji array with random emoji (only those emojis present in fontEmoji)
    static void RandomizeEmoji(){
        hovered = selected = -1;
        int start = rlj.core.GetRandomValue(45, 360);

        for (int i = 0; i < emoji.length; ++i){
            //Initialize emoji
            emoji[i] = new Emoji();

            // 0-179 emoji codepoints (from emoji char array)
            emoji[i].index = rlj.core.GetRandomValue(0, emojiCodepoints.length-1);

            // Generate a random color for this emoji
            emoji[i].color = rlj.textures.Fade(rlj.textures.ColorFromHSV((float) ((start * (i + 1)) % 360), 0.6f, 0.85f), 0.8f);

            // Set a random message for this emoji
            emoji[i].message = rlj.core.GetRandomValue(0, messages.length - 1);
        }
    }
    //--------------------------------------------------------------------------------------
    // Module functions definition
    //--------------------------------------------------------------------------------------

    // Draw text using font inside rectangle limits
    public static void DrawTextBoxed(Font font, String text, Rectangle rec, float fontSize, float spacing, boolean wordWarp, Color tint) {
        DrawTextBoxedSelectable(font, text, rec, fontSize, spacing, wordWarp, tint, 0, 0, Color.WHITE, Color.WHITE);
    }

    public static final int MEASURE_STATE = 0;
    public static final int DRAW_STATE = 1;

    // Draw text using font inside rectangle limits with support for text selection
    public static void DrawTextBoxedSelectable(Font font, String text, Rectangle rec, float fontSize, float spacing, boolean wordWrap, Color tint, int selectStart, int selectLength, Color selectTint, Color selectBackTint) {
        int length = rlj.text.TextLength(text);  // Total length in bytes of the text, scanned by codepoints in loop

        float textOffsetY = 0;       // Offset between lines (on line break '\n')
        float textOffsetX = 0;       // Offset X to next character to draw

        float scaleFactor = fontSize / (float) font.baseSize;     // Character rectangle scaling factor

        // Word/character wrapping mechanism variables
        int state = wordWrap ? MEASURE_STATE : DRAW_STATE;

        int startLine = -1;         // Index where to begin drawing (where a line begins)
        int endLine = -1;           // Index where to stop drawing (where a line ends)
        int lastk = -1;             // Holds last value of the character position

        for(int i = 0, k = 0; i < length; i++, k++) {
            // Get next codepoint from byte string and glyph index in font
            int codepoint = rlj.text.GetCodepointNext(text, i);
            int codepointByteCount = rlj.text.GetCodePointByteCount(codepoint);
            int index = rlj.text.GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return (byte) 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == (byte) 0x3f) {
                codepointByteCount = 1;
            }
            float glyphWidth = 0;
            if (codepoint != '\n') {
                glyphWidth = (font.glyphs[index].advanceX == 0) ? font.recs[index].width*scaleFactor : font.glyphs[index].advanceX*scaleFactor;

                if (i + 1 < length) {
                    glyphWidth = glyphWidth + spacing;
                }
            }

            // NOTE: When wordWrap is ON we first measure how much of the text we can draw before going outside of the rec container
            // We store this info in startLine and endLine, then we change states, draw the text between those two variables
            // and change states again and again recursively until the end of the text (or until we get outside of the container).
            // When wordWrap is OFF we don't need the measure state so we go to the drawing state immediately
            // and begin drawing on the next line before we can get outside the container.
            if (state == MEASURE_STATE) {

                // TODO: There are multiple types of spaces in UNICODE, maybe it's a good idea to add support for more
                // Ref: http://jkorpela.fi/chars/spaces.html
                if ((codepoint == ' ') || (codepoint == '\t') || (codepoint == '\n')) endLine = i;
                if ((textOffsetX + glyphWidth) > rec.width) {
                    endLine = (endLine < 1)? i : endLine;
                    if (i == endLine) endLine -= codepointByteCount;
                    if ((startLine + codepointByteCount) == endLine) endLine = (i - codepointByteCount);

                    state = DRAW_STATE;
                }
                else if ((i + 1) == length) {
                    endLine = i;
                    state = DRAW_STATE;
                }
                else if (codepoint == '\n') state = DRAW_STATE;

                if (state == DRAW_STATE) {
                    textOffsetX = 0;
                    i = startLine;
                    glyphWidth = 0;

                    // Save character position when we switch states
                    int tmp = lastk;
                    lastk = k - 1;
                    k = tmp;
                }
            }
            else {
                if (codepoint == '\n') {
                    if (!wordWrap) {
                        textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                        textOffsetX = 0;
                    }
                }
                else {
                    if (!wordWrap && ((textOffsetX + glyphWidth) > rec.width)) {
                        textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                        textOffsetX = 0;
                    }

                    // When text overflows rectangle height limit, just stop drawing
                    if ((textOffsetY + font.baseSize*scaleFactor) > rec.height) {
                        break;
                    }

                    // Draw selection background
                    boolean isGlyphSelected = false;
                    if ((selectStart >= 0) && (k >= selectStart) && (k < (selectStart + selectLength))) {
                        rlj.shapes.DrawRectangleRec(new Rectangle(rec.x + textOffsetX - 1, rec.y + textOffsetY, glyphWidth, (float)font.baseSize*scaleFactor), selectBackTint);
                        isGlyphSelected = true;
                    }

                    // Draw current character glyph
                    if ((codepoint != ' ') && (codepoint != '\t')) {
                        rlj.text.DrawTextCodepoint(font, codepoint, new Vector2(rec.x + textOffsetX, rec.y + textOffsetY), fontSize, isGlyphSelected? selectTint : tint);
                    }
                }

                if (wordWrap && (i == endLine)) {
                    textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                    textOffsetX = 0;
                    startLine = endLine;
                    endLine = -1;
                    glyphWidth = 0;
                    selectStart += lastk - k;
                    k = lastk;

                    state = MEASURE_STATE;
                }
            }

            textOffsetX += glyphWidth;
        }
    }

}
