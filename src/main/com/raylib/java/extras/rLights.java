package com.raylib.java.extras;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Color;
import com.raylib.java.structs.Vector3;
import com.raylib.java.structs.Shader;

import static com.raylib.java.rlgl.RLGL.rlShaderUniformDataType.*;

public class rLights{

    /**********************************************************************************************
     *
     *   raylib.lights - Some useful functions to deal with lights data
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2017-2020 Victor Fisac (@victorfisac) and Ramon Santamaria (@raysan5)
     *   Ported to Raylib-J by CreedVI
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     **********************************************************************************************/

    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    public static final int MAX_LIGHTS = 4;         // Max dynamic lights supported by shader

    //----------------------------------------------------------------------------------
    // Types and Structures Definition
    //----------------------------------------------------------------------------------

    // Light data
    public static class Light{
        public int type;
        public Vector3 position;
        public Vector3 target;
        public Color color;
        public boolean enabled;

        // Shader locations
        public int enabledLoc;
        public int typeLoc;
        public int posLoc;
        public int targetLoc;
        public int colorLoc;
    }

    // Light type
    public static int
        LIGHT_DIRECTIONAL = 0,
        LIGHT_POINT = 1;


    //----------------------------------------------------------------------------------
    // Defines and Macros
    //----------------------------------------------------------------------------------
    // ...

    //----------------------------------------------------------------------------------
    // Types and Structures Definition
    //----------------------------------------------------------------------------------
    // ...

    //----------------------------------------------------------------------------------
    // Global Variables Definition
    //----------------------------------------------------------------------------------
    static int lightsCount = 0;    // Current amount of created lights

    //----------------------------------------------------------------------------------
    // Module specific Functions Declaration
    //----------------------------------------------------------------------------------
    // ...

    //----------------------------------------------------------------------------------
    // Module Functions Definition
    //----------------------------------------------------------------------------------

    // Create a light and get shader locations
    public static Light CreateLight(Raylib context, int type, Vector3 position, Vector3 target, Color color, Shader shader){
        Light light = new Light();

        if (lightsCount < MAX_LIGHTS){
            light.enabled = true;
            light.type = type;
            light.position = position;
            light.target = target;
            light.color = color;

            // TODO: Below code doesn't look good to me,
            // it assumes a specific shader naming and structure
            // Probably this implementation could be improved

            // Set location name [x] depending on lights count
            String enabledName = "lights[" + lightsCount + "].enabled";
            String typeName = "lights[" + lightsCount + "].type";
            String posName = "lights[" + lightsCount + "].position";
            String targetName = "lights[" + lightsCount + "].target";
            String colorName = "lights[" + lightsCount + "].color";

            light.enabledLoc = context.core.GetShaderLocation(shader, enabledName);
            light.typeLoc = context.core.GetShaderLocation(shader, typeName);
            light.posLoc = context.core.GetShaderLocation(shader, posName);
            light.targetLoc = context.core.GetShaderLocation(shader, targetName);
            light.colorLoc = context.core.GetShaderLocation(shader, colorName);

            UpdateLightValues(context, shader, light);

            lightsCount++;
        }

        return light;
    }

    // Send light properties to shader
    // NOTE: Light shader locations should be available
    public static void UpdateLightValues(Raylib context, Shader shader, Light light){
        // Send to shader light enabled state and type
        context.core.SetShaderValue(shader, light.enabledLoc, new float[]{(light.enabled ? 1 : 0)}, SHADER_UNIFORM_INT);
        context.core.SetShaderValue(shader, light.typeLoc, new float[]{light.type}, SHADER_UNIFORM_INT);

        // Send to shader light position values
        float[] position = new float[]{light.position.x, light.position.y, light.position.z};
        context.core.SetShaderValue(shader, light.posLoc, position, SHADER_UNIFORM_VEC3);

        // Send to shader light target position values
        float[] target = new float[]{light.target.x, light.target.y, light.target.z};
        context.core.SetShaderValue(shader, light.targetLoc, target, SHADER_UNIFORM_VEC3);

        // Send to shader light color values
        float[] color = new float[]{
                (float) light.color.r / (float) 255,
                (float) light.color.g / (float) 255,
                (float) light.color.b / (float) 255,
                (float) light.color.a / (float) 255
        };
        context.core.SetShaderValue(shader, light.colorLoc, color, SHADER_UNIFORM_VEC4);
    }

}
