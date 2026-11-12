package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2LineAngleTest {
    @Test
    void testMethod() {
        assertEquals((float) Math.acos(0.6), Raymath.Vector2LineAngle(new Vector2(1.0f, 0.0f),
                new Vector2(0.6f, 0.8f)), 0.000001f);
    }
}