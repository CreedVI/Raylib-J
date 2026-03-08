package com.raylib.java.text;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Font;
import com.raylib.java.structs.Rectangle;
import com.raylib.java.structs.Vector2;

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
            "\u00F0\u009F\u008C\u0080", "\u00F0\u009F\u0098\u0080", "\u00F0\u009F\u0098\u0082", "\u00F0\u009F\u00A4\u00A3",
            "\u00F0\u009F\u0098\u0083", "\u00F0\u009F\u0098\u0086", "\u00F0\u009F\u0098\u0089", "\u00F0\u009F\u0098\u008B",
            "\u00F0\u009F\u0098\u008E", "\u00F0\u009F\u0098\u008D", "\u00F0\u009F\u0098\u0098", "\u00F0\u009F\u0098\u0097",
            "\u00F0\u009F\u0098\u0099", "\u00F0\u009F\u0098\u009A", "\u00F0\u009F\u0099\u0082", "\u00F0\u009F\u00A4\u0097",
            "\u00F0\u009F\u00A4\u00A9", "\u00F0\u009F\u00A4\u0094", "\u00F0\u009F\u00A4\u00A8", "\u00F0\u009F\u0098\u0090",
            "\u00F0\u009F\u0098\u0091", "\u00F0\u009F\u0098\u00B6", "\u00F0\u009F\u0099\u0084", "\u00F0\u009F\u0098\u008F",
            "\u00F0\u009F\u0098\u00A3", "\u00F0\u009F\u0098\u00A5", "\u00F0\u009F\u0098\u00AE", "\u00F0\u009F\u00A4\u0090",
            "\u00F0\u009F\u0098\u00AF", "\u00F0\u009F\u0098\u00AA", "\u00F0\u009F\u0098\u00AB", "\u00F0\u009F\u0098\u00B4",
            "\u00F0\u009F\u0098\u008C", "\u00F0\u009F\u0098\u009B", "\u00F0\u009F\u0098\u009D", "\u00F0\u009F\u00A4\u00A4",
            "\u00F0\u009F\u0098\u0092", "\u00F0\u009F\u0098\u0095", "\u00F0\u009F\u0099\u0083", "\u00F0\u009F\u00A4\u0091",
            "\u00F0\u009F\u0098\u00B2", "\u00F0\u009F\u0099\u0081", "\u00F0\u009F\u0098\u0096", "\u00F0\u009F\u0098\u009E",
            "\u00F0\u009F\u0098\u009F", "\u00F0\u009F\u0098\u00A4", "\u00F0\u009F\u0098\u00A2", "\u00F0\u009F\u0098\u00AD",
            "\u00F0\u009F\u0098\u00A6", "\u00F0\u009F\u0098\u00A9", "\u00F0\u009F\u00A4\u00AF", "\u00F0\u009F\u0098\u00AC",
            "\u00F0\u009F\u0098\u00B0", "\u00F0\u009F\u0098\u00B1", "\u00F0\u009F\u0098\u00B3", "\u00F0\u009F\u00A4\u00AA",
            "\u00F0\u009F\u0098\u00B5", "\u00F0\u009F\u0098\u00A1", "\u00F0\u009F\u0098\u00A0", "\u00F0\u009F\u00A4\u00AC",
            "\u00F0\u009F\u0098\u00B7", "\u00F0\u009F\u00A4\u0092", "\u00F0\u009F\u00A4\u0095", "\u00F0\u009F\u00A4\u00A2",
            "\u00F0\u009F\u00A4\u00AE", "\u00F0\u009F\u00A4\u00A7", "\u00F0\u009F\u0098\u0087", "\u00F0\u009F\u00A4\u00A0",
            "\u00F0\u009F\u00A4\u00AB", "\u00F0\u009F\u00A4\u00AD", "\u00F0\u009F\u00A7\u0090", "\u00F0\u009F\u00A4\u0093",
            "\u00F0\u009F\u0098\u0088", "\u00F0\u009F\u0091\u00BF", "\u00F0\u009F\u0091\u00B9", "\u00F0\u009F\u0091\u00BA",
            "\u00F0\u009F\u0092\u0080", "\u00F0\u009F\u0091\u00BB", "\u00F0\u009F\u0091\u00BD", "\u00F0\u009F\u0091\u00BE",
            "\u00F0\u009F\u00A4\u0096", "\u00F0\u009F\u0092\u00A9", "\u00F0\u009F\u0098\u00BA", "\u00F0\u009F\u0098\u00B8",
            "\u00F0\u009F\u0098\u00B9", "\u00F0\u009F\u0098\u00BB", "\u00F0\u009F\u0098\u00BD", "\u00F0\u009F\u0099\u0080",
            "\u00F0\u009F\u0098\u00BF", "\u00F0\u009F\u008C\u00BE", "\u00F0\u009F\u008C\u00BF", "\u00F0\u009F\u008D\u0080",
            "\u00F0\u009F\u008D\u0083", "\u00F0\u009F\u008D\u0087", "\u00F0\u009F\u008D\u0093", "\u00F0\u009F\u00A5\u009D",
            "\u00F0\u009F\u008D\u0085", "\u00F0\u009F\u00A5\u00A5", "\u00F0\u009F\u00A5\u0091", "\u00F0\u009F\u008D\u0086",
            "\u00F0\u009F\u00A5\u0094", "\u00F0\u009F\u00A5\u0095", "\u00F0\u009F\u008C\u00BD", "\u00F0\u009F\u008C\u00B6",
            "\u00F0\u009F\u00A5\u0092", "\u00F0\u009F\u00A5\u00A6", "\u00F0\u009F\u008D\u0084", "\u00F0\u009F\u00A5\u009C",
            "\u00F0\u009F\u008C\u00B0", "\u00F0\u009F\u008D\u009E", "\u00F0\u009F\u00A5\u0090", "\u00F0\u009F\u00A5\u0096",
            "\u00F0\u009F\u00A5\u00A8", "\u00F0\u009F\u00A5\u009E", "\u00F0\u009F\u00A7\u0080", "\u00F0\u009F\u008D\u0096",
            "\u00F0\u009F\u008D\u0097", "\u00F0\u009F\u00A5\u00A9", "\u00F0\u009F\u00A5\u0093", "\u00F0\u009F\u008D\u0094",
            "\u00F0\u009F\u008D\u009F", "\u00F0\u009F\u008D\u0095", "\u00F0\u009F\u008C\u00AD", "\u00F0\u009F\u00A5\u00AA",
            "\u00F0\u009F\u008C\u00AE", "\u00F0\u009F\u008C\u00AF", "\u00F0\u009F\u00A5\u0099", "\u00F0\u009F\u00A5\u009A",
            "\u00F0\u009F\u008D\u00B3", "\u00F0\u009F\u00A5\u0098", "\u00F0\u009F\u008D\u00B2", "\u00F0\u009F\u00A5\u00A3",
            "\u00F0\u009F\u00A5\u0097", "\u00F0\u009F\u008D\u00BF", "\u00F0\u009F\u00A5\u00AB", "\u00F0\u009F\u008D\u00B1",
            "\u00F0\u009F\u008D\u0098", "\u00F0\u009F\u008D\u009D", "\u00F0\u009F\u008D\u00A0", "\u00F0\u009F\u008D\u00A2",
            "\u00F0\u009F\u008D\u00A5", "\u00F0\u009F\u008D\u00A1", "\u00F0\u009F\u00A5\u009F", "\u00F0\u009F\u00A5\u00A1",
            "\u00F0\u009F\u008D\u00A6", "\u00F0\u009F\u008D\u00AA", "\u00F0\u009F\u008E\u0082", "\u00F0\u009F\u008D\u00B0",
            "\u00F0\u009F\u00A5\u00A7", "\u00F0\u009F\u008D\u00AB", "\u00F0\u009F\u008D\u00AF", "\u00F0\u009F\u008D\u00BC",
            "\u00F0\u009F\u00A5\u009B", "\u00F0\u009F\u008D\u00B5", "\u00F0\u009F\u008D\u00B6", "\u00F0\u009F\u008D\u00BE",
            "\u00F0\u009F\u008D\u00B7", "\u00F0\u009F\u008D\u00BB", "\u00F0\u009F\u00A5\u0082", "\u00F0\u009F\u00A5\u0083",
            "\u00F0\u009F\u00A5\u00A4", "\u00F0\u009F\u00A5\u00A2", "\u00F0\u009F\u0091\u0081", "\u00F0\u009F\u0091\u0085",
            "\u00F0\u009F\u0091\u0084", "\u00F0\u009F\u0092\u008B", "\u00F0\u009F\u0092\u0098", "\u00F0\u009F\u0092\u0093",
            "\u00F0\u009F\u0092\u0097", "\u00F0\u009F\u0092\u0099", "\u00F0\u009F\u0092\u009B", "\u00F0\u009F\u00A7\u00A1",
            "\u00F0\u009F\u0092\u009C", "\u00F0\u009F\u0096\u00A4", "\u00F0\u009F\u0092\u009D", "\u00F0\u009F\u0092\u009F",
            "\u00F0\u009F\u0092\u008C", "\u00F0\u009F\u0092\u00A4", "\u00F0\u009F\u0092\u00A2", "\u00F0\u009F\u0092\u00A3"
    };
    static Message[] messages = { // Array containing all of the emojis messages
            new Message("\u0046\u0061\u006C\u0073\u0063\u0068\u0065\u0073\u0020\u00C3\u009C\u0062\u0065\u006E\u0020\u0076\u006F\u006E\u0020\u0058\u0079\u006C\u006F\u0070\u0068\u006F\u006E\u006D\u0075\u0073\u0069\u006B\u0020\u0071\u0075\u00C3\u00A4\u006C\u0074\u0020\u006A\u0065\u0064\u0065\u006E\u0020\u0067\u0072\u00C3\u00B6\u00C3\u009F\u0065\u0072\u0065\u006E\u0020\u005A\u0077\u0065\u0072\u0067", "German"),
            new Message("\u0042\u0065\u0069\u00C3\u009F\u0020\u006E\u0069\u0063\u0068\u0074\u0020\u0069\u006E\u0020\u0064\u0069\u0065\u0020\u0048\u0061\u006E\u0064\u002C\u0020\u0064\u0069\u0065\u0020\u0064\u0069\u0063\u0068\u0020\u0066\u00C3\u00BC\u0074\u0074\u0065\u0072\u0074\u002E", "German"),
            new Message("\u0041\u0075\u00C3\u009F\u0065\u0072\u006F\u0072\u0064\u0065\u006E\u0074\u006C\u0069\u0063\u0068\u0065\u0020\u00C3\u009C\u0062\u0065\u006C\u0020\u0065\u0072\u0066\u006F\u0072\u0064\u0065\u0072\u006E\u0020\u0061\u0075\u00C3\u009F\u0065\u0072\u006F\u0072\u0064\u0065\u006E\u0074\u006C\u0069\u0063\u0068\u0065\u0020\u004D\u0069\u0074\u0074\u0065\u006C\u002E", "German"),
            new Message("\u00D4\u00BF\u00D6\u0080\u00D5\u00B6\u00D5\u00A1\u00D5\u00B4\u0020\u00D5\u00A1\u00D5\u00BA\u00D5\u00A1\u00D5\u00AF\u00D5\u00AB\u0020\u00D5\u00B8\u00D6\u0082\u00D5\u00BF\u00D5\u00A5\u00D5\u00AC\u0020\u00D6\u0087\u0020\u00D5\u00AB\u00D5\u00B6\u00D5\u00AE\u00D5\u00AB\u0020\u00D5\u00A1\u00D5\u00B6\u00D5\u00B0\u00D5\u00A1\u00D5\u00B6\u00D5\u00A3\u00D5\u00AB\u00D5\u00BD\u00D5\u00BF\u0020\u00D5\u00B9\u00D5\u00A8\u00D5\u00B6\u00D5\u00A5\u00D6\u0080", "Armenian"),
            new Message("\u00D4\u00B5\u00D6\u0080\u00D5\u00A2\u0020\u00D5\u00B8\u00D6\u0080\u0020\u00D5\u00AF\u00D5\u00A1\u00D6\u0081\u00D5\u00AB\u00D5\u00B6\u00D5\u00A8\u0020\u00D5\u00A5\u00D5\u00AF\u00D5\u00A1\u00D6\u0082\u0020\u00D5\u00A1\u00D5\u00B6\u00D5\u00BF\u00D5\u00A1\u00D5\u00BC\u002C\u0020\u00D5\u00AE\u00D5\u00A1\u00D5\u00BC\u00D5\u00A5\u00D6\u0080\u00D5\u00A8\u0020\u00D5\u00A1\u00D5\u00BD\u00D5\u00A1\u00D6\u0081\u00D5\u00AB\u00D5\u00B6\u002E\u002E\u002E\u0020\u00C2\u00AB\u00D4\u00BF\u00D5\u00B8\u00D5\u00BF\u00D5\u00A8\u0020\u00D5\u00B4\u00D5\u00A5\u00D6\u0080\u00D5\u00B8\u00D5\u00B6\u00D6\u0081\u00D5\u00AB\u00D6\u0081\u0020\u00D5\u00A7\u003A\u00C2\u00BB", "Armenian"),
            new Message("\u00D4\u00B3\u00D5\u00A1\u00D5\u00BC\u00D5\u00A8\u00D5\u009D\u0020\u00D5\u00A3\u00D5\u00A1\u00D6\u0080\u00D5\u00B6\u00D5\u00A1\u00D5\u00B6\u002C\u0020\u00D5\u00B1\u00D5\u00AB\u00D6\u0082\u00D5\u00B6\u00D5\u00A8\u00D5\u009D\u0020\u00D5\u00B1\u00D5\u00B4\u00D5\u00BC\u00D5\u00A1\u00D5\u00B6", "Armenian"),
            new Message("\u004A\u0065\u00C5\u00BC\u0075\u0020\u006B\u006C\u00C4\u0085\u0074\u0077\u002C\u0020\u0073\u0070\u00C5\u0082\u00C3\u00B3\u0064\u00C5\u00BA\u0020\u0046\u0069\u006E\u006F\u006D\u0020\u0063\u007A\u00C4\u0099\u00C5\u009B\u00C4\u0087\u0020\u0067\u0072\u0079\u0020\u0068\u0061\u00C5\u0084\u0062\u0021", "Polish"),
            new Message("\u0044\u006F\u0062\u0072\u0079\u006D\u0069\u0020\u0063\u0068\u00C4\u0099\u0063\u0069\u0061\u006D\u0069\u0020\u006A\u0065\u0073\u0074\u0020\u0070\u0069\u0065\u006B\u00C5\u0082\u006F\u0020\u0077\u0079\u0062\u0072\u0075\u006B\u006F\u0077\u0061\u006E\u0065\u002E", "Polish"),
            new Message("\u00C3\u008E\u00C8\u009B\u0069\u0020\u006D\u0075\u006C\u00C8\u009B\u0075\u006D\u0065\u0073\u0063\u0020\u0063\u00C4\u0083\u0020\u0061\u0069\u0020\u0061\u006C\u0065\u0073\u0020\u0072\u0061\u0079\u006C\u0069\u0062\u002E\u00A0\u00C8\u0098\u0069\u0020\u0073\u0070\u0065\u0072\u0020\u0073\u00C4\u0083\u0020\u0061\u0069\u0020\u006F\u0020\u007A\u0069\u0020\u0062\u0075\u006E\u00C4\u0083\u0021", "Romanian"),
            new Message("\u00D0\u00AD\u00D1\u0085\u002C\u0020\u00D1\u0087\u00D1\u0083\u00D0\u00B6\u00D0\u00B0\u00D0\u00BA\u002C\u0020\u00D0\u00BE\u00D0\u00B1\u00D1\u0089\u00D0\u00B8\u00D0\u00B9\u0020\u00D1\u0081\u00D1\u008A\u00D1\u0091\u00D0\u00BC\u0020\u00D1\u0086\u00D0\u00B5\u00D0\u00BD\u0020\u00D1\u0088\u00D0\u00BB\u00D1\u008F\u00D0\u00BF\u0020\u0028\u00D1\u008E\u00D1\u0084\u00D1\u0082\u00D1\u008C\u0029\u0020\u00D0\u00B2\u00D0\u00B4\u00D1\u0080\u00D1\u008B\u00D0\u00B7\u00D0\u00B3\u0021", "Russian"),
            new Message("\u00D0\u00AF\u0020\u00D0\u00BB\u00D1\u008E\u00D0\u00B1\u00D0\u00BB\u00D1\u008E\u0020\u0072\u0061\u0079\u006C\u0069\u0062\u0021", "Russian"),
            new Message("\u00D0\u009C\u00D0\u00BE\u00D0\u00BB\u00D1\u0087\u00D0\u00B8\u002C\u0020\u00D1\u0081\u00D0\u00BA\u00D1\u0080\u00D1\u008B\u00D0\u00B2\u00D0\u00B0\u00D0\u00B9\u00D1\u0081\u00D1\u008F\u0020\u00D0\u00B8\u0020\u00D1\u0082\u00D0\u00B0\u00D0\u00B8\u00A0\u00D0\u0098\u0020\u00D1\u0087\u00D1\u0083\u00D0\u00B2\u00D1\u0081\u00D1\u0082\u00D0\u00B2\u00D0\u00B0\u0020\u00D0\u00B8\u0020\u00D0\u00BC\u00D0\u00B5\u00D1\u0087\u00D1\u0082\u00D1\u008B\u0020\u00D1\u0081\u00D0\u00B2\u00D0\u00BE\u00D0\u00B8\u0020\u00E2\u0080\u0093\u00A0\u00D0\u009F\u00D1\u0083\u00D1\u0081\u00D0\u00BA\u00D0\u00B0\u00D0\u00B9\u0020\u00D0\u00B2\u0020\u00D0\u00B4\u00D1\u0083\u00D1\u0088\u00D0\u00B5\u00D0\u00B2\u00D0\u00BD\u00D0\u00BE\u00D0\u00B9\u0020\u00D0\u00B3\u00D0\u00BB\u00D1\u0083\u00D0\u00B1\u00D0\u00B8\u00D0\u00BD\u00D0\u00B5\u00A0\u00D0\u0098\u0020\u00D0\u00B2\u00D1\u0081\u00D1\u0085\u00D0\u00BE\u00D0\u00B4\u00D1\u008F\u00D1\u0082\u0020\u00D0\u00B8\u0020\u00D0\u00B7\u00D0\u00B0\u00D0\u00B9\u00D0\u00B4\u00D1\u0083\u00D1\u0082\u0020\u00D0\u00BE\u00D0\u00BD\u00D0\u00B5\u00A0\u00D0\u009A\u00D0\u00B0\u00D0\u00BA\u0020\u00D0\u00B7\u00D0\u00B2\u00D0\u00B5\u00D0\u00B7\u00D0\u00B4\u00D1\u008B\u0020\u00D1\u008F\u00D1\u0081\u00D0\u00BD\u00D1\u008B\u00D0\u00B5\u0020\u00D0\u00B2\u0020\u00D0\u00BD\u00D0\u00BE\u00D1\u0087\u00D0\u00B8\u002D\u00A0\u00D0\u009B\u00D1\u008E\u00D0\u00B1\u00D1\u0083\u00D0\u00B9\u00D1\u0081\u00D1\u008F\u0020\u00D0\u00B8\u00D0\u00BC\u00D0\u00B8\u0020\u00E2\u0080\u0093\u0020\u00D0\u00B8\u0020\u00D0\u00BC\u00D0\u00BE\u00D0\u00BB\u00D1\u0087\u00D0\u00B8\u002E", "Russian"),
            new Message("\u0056\u006F\u0069\u0078\u0020\u0061\u006D\u0062\u0069\u0067\u0075\u00C3\u00AB\u0020\u0064\u00E2\u0080\u0099\u0075\u006E\u0020\u0063\u00C5\u0093\u0075\u0072\u0020\u0071\u0075\u0069\u0020\u0061\u0075\u0020\u007A\u00C3\u00A9\u0070\u0068\u0079\u0072\u0020\u0070\u0072\u00C3\u00A9\u0066\u00C3\u00A8\u0072\u0065\u0020\u006C\u0065\u0073\u0020\u006A\u0061\u0074\u0074\u0065\u0073\u0020\u0064\u0065\u0020\u006B\u0069\u0077\u0069", "French"),
            new Message("\u0042\u0065\u006E\u006A\u0061\u006D\u00C3\u00AD\u006E\u0020\u0070\u0069\u0064\u0069\u00C3\u00B3\u0020\u0075\u006E\u0061\u0020\u0062\u0065\u0062\u0069\u0064\u0061\u0020\u0064\u0065\u0020\u006B\u0069\u0077\u0069\u0020\u0079\u0020\u0066\u0072\u0065\u0073\u0061\u003B\u0020\u004E\u006F\u00C3\u00A9\u002C\u0020\u0073\u0069\u006E\u0020\u0076\u0065\u0072\u0067\u00C3\u00BC\u0065\u006E\u007A\u0061\u002C\u0020\u006C\u0061\u0020\u006D\u00C3\u00A1\u0073\u0020\u0065\u0078\u0071\u0075\u0069\u0073\u0069\u0074\u0061\u0020\u0063\u0068\u0061\u006D\u0070\u0061\u00C3\u00B1\u0061\u0020\u0064\u0065\u006C\u0020\u006D\u0065\u006E\u00C3\u00BA\u002E", "Spanish"),
            new Message("\u00CE\u00A4\u00CE\u00B1\u00CF\u0087\u00CE\u00AF\u00CF\u0083\u00CF\u0084\u00CE\u00B7\u0020\u00CE\u00B1\u00CE\u00BB\u00CF\u008E\u00CF\u0080\u00CE\u00B7\u00CE\u00BE\u0020\u00CE\u00B2\u00CE\u00B1\u00CF\u0086\u00CE\u00AE\u00CF\u0082\u0020\u00CF\u0088\u00CE\u00B7\u00CE\u00BC\u00CE\u00AD\u00CE\u00BD\u00CE\u00B7\u0020\u00CE\u00B3\u00CE\u00B7\u002C\u0020\u00CE\u00B4\u00CF\u0081\u00CE\u00B1\u00CF\u0083\u00CE\u00BA\u00CE\u00B5\u00CE\u00BB\u00CE\u00AF\u00CE\u00B6\u00CE\u00B5\u00CE\u00B9\u0020\u00CF\u0085\u00CF\u0080\u00CE\u00AD\u00CF\u0081\u0020\u00CE\u00BD\u00CF\u0089\u00CE\u00B8\u00CF\u0081\u00CE\u00BF\u00CF\u008D\u0020\u00CE\u00BA\u00CF\u0085\u00CE\u00BD\u00CF\u008C\u00CF\u0082", "Greek"),
            new Message("\u00CE\u0097\u0020\u00CE\u00BA\u00CE\u00B1\u00CE\u00BB\u00CF\u008D\u00CF\u0084\u00CE\u00B5\u00CF\u0081\u00CE\u00B7\u0020\u00CE\u00AC\u00CE\u00BC\u00CF\u0085\u00CE\u00BD\u00CE\u00B1\u0020\u00CE\u00B5\u00CE\u00AF\u00CE\u00BD\u00CE\u00B1\u00CE\u00B9\u0020\u00CE\u00B7\u0020\u00CE\u00B5\u00CF\u0080\u00CE\u00AF\u00CE\u00B8\u00CE\u00B5\u00CF\u0083\u00CE\u00B7\u002E", "Greek"),
            new Message("\u00CE\u00A7\u00CF\u0081\u00CF\u008C\u00CE\u00BD\u00CE\u00B9\u00CE\u00B1\u0020\u00CE\u00BA\u00CE\u00B1\u00CE\u00B9\u0020\u00CE\u00B6\u00CE\u00B1\u00CE\u00BC\u00CE\u00AC\u00CE\u00BD\u00CE\u00B9\u00CE\u00B1\u0021", "Greek"),
            new Message("\u00CE\u00A0\u00CF\u008E\u00CF\u0082\u0020\u00CF\u0084\u00CE\u00B1\u0020\u00CF\u0080\u00CE\u00B1\u00CF\u0082\u0020\u00CF\u0083\u00CE\u00AE\u00CE\u00BC\u00CE\u00B5\u00CF\u0081\u00CE\u00B1\u003B", "Greek"),

            new Message("\u00E6\u0088\u0091\u00E8\u0083\u00BD\u00E5\u0090\u009E\u00E4\u00B8\u008B\u00E7\u008E\u00BB\u00E7\u0092\u0083\u00E8\u0080\u008C\u00E4\u00B8\u008D\u00E4\u00BC\u00A4\u00E8\u00BA\u00AB\u00E4\u00BD\u0093\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E4\u00BD\u00A0\u00E5\u0090\u0083\u00E4\u00BA\u0086\u00E5\u0090\u0097\u00EF\u00BC\u009F", "Chinese"),
            new Message("\u00E4\u00B8\u008D\u00E4\u00BD\u009C\u00E4\u00B8\u008D\u00E6\u00AD\u00BB\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E6\u009C\u0080\u00E8\u00BF\u0091\u00E5\u00A5\u00BD\u00E5\u0090\u0097\u00EF\u00BC\u009F", "Chinese"),
            new Message("\u00E5\u00A1\u009E\u00E7\u00BF\u0081\u00E5\u00A4\u00B1\u00E9\u00A9\u00AC\u00EF\u00BC\u008C\u00E7\u0084\u0089\u00E7\u009F\u00A5\u00E9\u009D\u009E\u00E7\u00A6\u008F\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E5\u008D\u0083\u00E5\u0086\u009B\u00E6\u0098\u0093\u00E5\u00BE\u0097\u002C\u0020\u00E4\u00B8\u0080\u00E5\u00B0\u0086\u00E9\u009A\u00BE\u00E6\u00B1\u0082", "Chinese"),
            new Message("\u00E4\u00B8\u0087\u00E4\u00BA\u008B\u00E5\u00BC\u0080\u00E5\u00A4\u00B4\u00E9\u009A\u00BE\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E9\u00A3\u008E\u00E6\u0097\u00A0\u00E5\u00B8\u00B8\u00E9\u00A1\u00BA\u00EF\u00BC\u008C\u00E5\u0085\u00B5\u00E6\u0097\u00A0\u00E5\u00B8\u00B8\u00E8\u0083\u009C\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E6\u00B4\u00BB\u00E5\u0088\u00B0\u00E8\u0080\u0081\u00EF\u00BC\u008C\u00E5\u00AD\u00A6\u00E5\u0088\u00B0\u00E8\u0080\u0081\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E4\u00B8\u0080\u00E8\u00A8\u0080\u00E6\u0097\u00A2\u00E5\u0087\u00BA\u00EF\u00BC\u008C\u00E9\u00A9\u00B7\u00E9\u00A9\u00AC\u00E9\u009A\u00BE\u00E8\u00BF\u00BD\u00E3\u0080\u0082", "Chinese"),
            new Message("\u00E8\u00B7\u00AF\u00E9\u0081\u00A5\u00E7\u009F\u00A5\u00E9\u00A9\u00AC\u00E5\u008A\u009B\u00EF\u00BC\u008C\u00E6\u0097\u00A5\u00E4\u00B9\u0085\u00E8\u00A7\u0081\u00E4\u00BA\u00BA\u00E5\u00BF\u0083", "Chinese"),
            new Message("\u00E6\u009C\u0089\u00E7\u0090\u0086\u00E8\u00B5\u00B0\u00E9\u0081\u008D\u00E5\u00A4\u00A9\u00E4\u00B8\u008B\u00EF\u00BC\u008C\u00E6\u0097\u00A0\u00E7\u0090\u0086\u00E5\u00AF\u00B8\u00E6\u00AD\u00A5\u00E9\u009A\u00BE\u00E8\u00A1\u008C\u00E3\u0080\u0082", "Chinese"),

            new Message("\u00E7\u008C\u00BF\u00E3\u0082\u0082\u00E6\u009C\u00A8\u00E3\u0081\u008B\u00E3\u0082\u0089\u00E8\u0090\u00BD\u00E3\u0081\u00A1\u00E3\u0082\u008B", "Japanese"),
            new Message("\u00E4\u00BA\u0080\u00E3\u0081\u00AE\u00E7\u0094\u00B2\u00E3\u0082\u0088\u00E3\u0082\u008A\u00E5\u00B9\u00B4\u00E3\u0081\u00AE\u00E5\u008A\u009F", "Japanese"),
            new Message("\u00E3\u0081\u0086\u00E3\u0082\u0089\u00E3\u0082\u0084\u00E3\u0081\u00BE\u00E3\u0081\u0097\u0020\u0020\u00E6\u0080\u009D\u00E3\u0081\u00B2\u00E5\u0088\u0087\u00E3\u0082\u008B\u00E6\u0099\u0082\u0020\u0020\u00E7\u008C\u00AB\u00E3\u0081\u00AE\u00E6\u0081\u008B", "Japanese"),
            new Message("\u00E8\u0099\u008E\u00E7\u00A9\u00B4\u00E3\u0081\u00AB\u00E5\u0085\u00A5\u00E3\u0082\u0089\u00E3\u0081\u009A\u00E3\u0082\u0093\u00E3\u0081\u00B0\u00E8\u0099\u008E\u00E5\u00AD\u0090\u00E3\u0082\u0092\u00E5\u00BE\u0097\u00E3\u0081\u009A\u00E3\u0080\u0082", "Japanese"),
            new Message("\u00E4\u00BA\u008C\u00E5\u0085\u008E\u00E3\u0082\u0092\u00E8\u00BF\u00BD\u00E3\u0081\u0086\u00E8\u0080\u0085\u00E3\u0081\u00AF\u00E4\u00B8\u0080\u00E5\u0085\u008E\u00E3\u0082\u0092\u00E3\u0082\u0082\u00E5\u00BE\u0097\u00E3\u0081\u009A\u00E3\u0080\u0082", "Japanese"),
            new Message("\u00E9\u00A6\u00AC\u00E9\u00B9\u00BF\u00E3\u0081\u00AF\u00E6\u00AD\u00BB\u00E3\u0081\u00AA\u00E3\u0081\u00AA\u00E3\u0081\u008D\u00E3\u0082\u0083\u00E6\u00B2\u00BB\u00E3\u0082\u0089\u00E3\u0081\u00AA\u00E3\u0081\u0084\u00E3\u0080\u0082", "Japanese"),
            new Message("\u00E6\u009E\u00AF\u00E9\u0087\u008E\u00E8\u00B7\u00AF\u00E3\u0081\u00AB\u00E3\u0080\u0080\u00E5\u00BD\u00B1\u00E3\u0081\u008B\u00E3\u0081\u0095\u00E3\u0081\u00AA\u00E3\u0082\u008A\u00E3\u0081\u00A6\u00E3\u0080\u0080\u00E3\u0082\u008F\u00E3\u0081\u008B\u00E3\u0082\u008C\u00E3\u0081\u0091\u00E3\u0082\u008A", "Japanese"),
            new Message("\u00E7\u00B9\u00B0\u00E3\u0082\u008A\u00E8\u00BF\u0094\u00E3\u0081\u0097\u00E9\u00BA\u00A6\u00E3\u0081\u00AE\u00E7\u0095\u009D\u00E7\u00B8\u00AB\u00E3\u0081\u00B5\u00E8\u0083\u00A1\u00E8\u009D\u00B6\u00E5\u0093\u0089", "Japanese"),

            new Message("\u00EC\u0095\u0084\u00EB\u0093\u009D\u00ED\u0095\u009C\u0020\u00EB\u00B0\u0094\u00EB\u008B\u00A4\u0020\u00EC\u009C\u0084\u00EC\u0097\u0090\u0020\u00EA\u00B0\u0088\u00EB\u00A7\u00A4\u00EA\u00B8\u00B0\u0020\u00EB\u0091\u0090\u00EC\u0097\u0087\u0020\u00EB\u0082\u00A0\u00EC\u0095\u0084\u0020\u00EB\u008F\u0088\u00EB\u008B\u00A4\u002E\u00A0\u00EB\u0084\u0088\u00ED\u009B\u008C\u00EB\u0084\u0088\u00ED\u009B\u008C\u0020\u00EC\u008B\u009C\u00EB\u00A5\u00BC\u0020\u00EC\u0093\u00B4\u00EB\u008B\u00A4\u002E\u0020\u00EB\u00AA\u00A8\u00EB\u00A5\u00B4\u00EB\u008A\u0094\u0020\u00EB\u0082\u0098\u00EB\u009D\u00BC\u0020\u00EA\u00B8\u0080\u00EC\u009E\u0090\u00EB\u008B\u00A4\u002E\u00A0\u00EB\u0084\u0090\u00EB\u0094\u00B0\u00EB\u009E\u0080\u0020\u00ED\u0095\u0098\u00EB\u008A\u0098\u0020\u00EB\u00B3\u00B5\u00ED\u008C\u0090\u00EC\u0097\u0090\u0020\u00EB\u0082\u0098\u00EB\u008F\u0084\u0020\u00EA\u00B0\u0099\u00EC\u009D\u00B4\u0020\u00EC\u008B\u009C\u00EB\u00A5\u00BC\u0020\u00EC\u0093\u00B4\u00EB\u008B\u00A4\u002E", "Korean"),
            new Message("\u00EC\u00A0\u009C\u0020\u00EB\u0088\u0088\u00EC\u0097\u0090\u0020\u00EC\u0095\u0088\u00EA\u00B2\u00BD\u00EC\u009D\u00B4\u00EB\u008B\u00A4", "Korean"),
            new Message("\u00EA\u00BF\u00A9\u0020\u00EB\u00A8\u00B9\u00EA\u00B3\u00A0\u0020\u00EC\u0095\u008C\u0020\u00EB\u00A8\u00B9\u00EB\u008A\u0094\u00EB\u008B\u00A4", "Korean"),
            new Message("\u00EB\u00A1\u009C\u00EB\u00A7\u0088\u00EB\u008A\u0094\u0020\u00ED\u0095\u0098\u00EB\u00A3\u00A8\u00EC\u0095\u0084\u00EC\u00B9\u00A8\u00EC\u0097\u0090\u0020\u00EC\u009D\u00B4\u00EB\u00A3\u00A8\u00EC\u0096\u00B4\u00EC\u00A7\u0084\u0020\u00EA\u00B2\u0083\u00EC\u009D\u00B4\u0020\u00EC\u0095\u0084\u00EB\u008B\u0088\u00EB\u008B\u00A4", "Korean"),
            new Message("\u00EA\u00B3\u00A0\u00EC\u0083\u009D\u0020\u00EB\u0081\u009D\u00EC\u0097\u0090\u0020\u00EB\u0082\u0099\u00EC\u009D\u00B4\u0020\u00EC\u0098\u00A8\u00EB\u008B\u00A4", "Korean"),
            new Message("\u00EA\u00B0\u009C\u00EC\u00B2\u009C\u00EC\u0097\u0090\u00EC\u0084\u009C\u0020\u00EC\u009A\u00A9\u0020\u00EB\u0082\u009C\u00EB\u008B\u00A4", "Korean"),
            new Message("\u00EC\u0095\u0088\u00EB\u0085\u0095\u00ED\u0095\u0098\u00EC\u0084\u00B8\u00EC\u009A\u0094\u003F", "Korean"),
            new Message("\u00EB\u00A7\u008C\u00EB\u0082\u0098\u00EC\u0084\u009C\u0020\u00EB\u00B0\u0098\u00EA\u00B0\u0091\u00EC\u008A\u00B5\u00EB\u008B\u0088\u00EB\u008B\u00A4", "Korean"),
            new Message("\u00ED\u0095\u009C\u00EA\u00B5\u00AD\u00EB\u00A7\u0090\u0020\u00ED\u0095\u0098\u00EC\u008B\u00A4\u0020\u00EC\u00A4\u0084\u0020\u00EC\u0095\u0084\u00EC\u0084\u00B8\u00EC\u009A\u0094\u003F", "Korean")
    };
    static Emoji[] emoji = new Emoji[EMOJI_PER_WIDTH * EMOJI_PER_HEIGHT];
    static int hovered = -1, selected = -1;
    static Raylib rlj;

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
        Font fontDefault = rlj.text.LoadFont("resources/dejavu.fnt");
        Font fontAsian = rlj.text.LoadFont("resources/noto_cjk.fnt");
        Font fontEmoji = rlj.text.LoadFont("resources/symbola.fnt");

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
            if (rlj.core.IsKeyPressed(KEY_SPACE)) RandomizeEmoji();

            // Set the selected emoji and copy its text to clipboard
            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && (hovered != -1) && (hovered != selected)){
                selected = hovered;
                selectedPos = hoveredPos;
                rlj.core.SetClipboardText(messages[emoji[selected].message].text);
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
            for (int i = 0; i < emoji.length; ++i){
                String txt = emojiCodepoints[emoji[i].index];
                Rectangle emojiRect = new Rectangle(pos.x, pos.y, (float) fontEmoji.baseSize, (float) fontEmoji.baseSize);

                if (!rlj.shapes.CheckCollisionPointRec(mouse, emojiRect)){
                    rlj.text.DrawTextEx(fontEmoji, txt, pos, (float) fontEmoji.baseSize, 1.0f, selected == i ? emoji[i].color :
                            rlj.textures.Fade(Color.LIGHTGRAY, 0.4f));
                }
                else{
                    rlj.text.DrawTextEx(fontEmoji, txt, pos, (float) fontEmoji.baseSize, 1.0f, emoji[i].color);
                    hovered = i;
                    hoveredPos = pos;
                }

                if ((i != 0) && (i % EMOJI_PER_WIDTH == 0)){
                    pos.y += fontEmoji.baseSize + 24.25f;
                    pos.x = 28.8f;
                }
                else{
                    pos.x += fontEmoji.baseSize + 28.8f;
                }
            }
            //------------------------------------------------------------------------------
            // Draw the message when an emoji is selected
            //------------------------------------------------------------------------------
            if (selected != -1){
                int message = emoji[selected].message;
                int horizontalPadding = 20, verticalPadding = 30;
                Font font = fontDefault;

                // Set correct font for asian languages
                if (rlj.text.TextIsEqual(messages[message].language, "Chinese") ||
                        rlj.text.TextIsEqual(messages[message].language, "Korean") ||
                        rlj.text.TextIsEqual(messages[message].language, "Japanese")){
                    font = fontAsian;
                }

                // Calculate size for the message box (approximate the height and width)
                Vector2 sz = rlj.text.MeasureTextEx(font, messages[message].text, (float) font.baseSize, 1.0f);
                if (sz.x > 300){
                    sz.y *= sz.x / 300;
                    sz.x = 300;
                }
                else if (sz.x < 160){
                    sz.x = 160;
                }

                Rectangle msgRect = new Rectangle(selectedPos.x - 38.8f, selectedPos.y, 2 * horizontalPadding + sz.x, 2 * verticalPadding + sz.y);
                msgRect.y -= msgRect.height;

                // Coordinates for the chat bubble triangle
                Vector2 a = new Vector2(selectedPos.x, msgRect.y + msgRect.height),
                        b = new Vector2(a.x + 8, a.y + 10),
                        c = new Vector2(a.x + 10, a.y);

                // Don't go outside the screen
                if (msgRect.x < 10){
                    msgRect.x += 28;
                }
                if (msgRect.y < 10){
                    msgRect.y = selectedPos.y + 84;
                    a.y = msgRect.y;
                    c.y = a.y;
                    b.y = a.y - 10;

                    // Swap values so we can actually render the triangle :(
                    Vector2 tmp = a;
                    a = b;
                    b = tmp;
                }
                if(msgRect.x + msgRect.width > screenWidth){
                    msgRect.x -= (msgRect.x + msgRect.width) - screenWidth + 10;
                }

                // Draw chat bubble
                rlj.shapes.DrawRectangleRec(msgRect, emoji[selected].color);
                rlj.shapes.DrawTriangle(a, b, c, emoji[selected].color);

                // Draw the main text message
                Rectangle textRect = new Rectangle(msgRect.x + horizontalPadding / 2.0f, msgRect.y + verticalPadding / 2.0f, msgRect.width - horizontalPadding, msgRect.height);
                DrawTextBoxed(font, messages[message].text, textRect, (float) font.baseSize, 1.0f, true, Color.WHITE);

                // Draw the info text below the main message
                int size = messages[message].text.length();
                int len = rlj.text.GetCodepointsCount(messages[message].text);
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
            int codepointByteCount = 0;
            int codepoint = rlj.text.GetCodepointNext(new char[ text.charAt(i) ]);
            int index = rlj.text.GetGlyphIndex(font, codepoint);

            // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
            // but we need to draw all of the bad bytes using the '?' symbol moving one byte
            if (codepoint == 0x3f) codepointByteCount = 1;
            i += (codepointByteCount - 1);

            float glyphWidth = 0;
            if (codepoint != '\n')
            {
                glyphWidth = (font.glyphs[index].advanceX == 0) ? font.recs[index].width*scaleFactor : font.glyphs[index].advanceX*scaleFactor;

                if (i + 1 < length) glyphWidth = glyphWidth + spacing;
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
            }else {
                if (codepoint == '\n') {
                    if (!wordWrap) {
                        textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                        textOffsetX = 0;
                    }
                }else {
                    if (!wordWrap && ((textOffsetX + glyphWidth) > rec.width)) {
                        textOffsetY += (font.baseSize + font.baseSize/2.0f)*scaleFactor;
                        textOffsetX = 0;
                    }

                    // When text overflows rectangle height limit, just stop drawing
                    if ((textOffsetY + font.baseSize*scaleFactor) > rec.height) break;

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

                if (wordWrap && (i == endLine))
                {
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
