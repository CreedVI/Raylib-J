package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2AngleTest {
    @Test
    void testMethod() {
        assertEquals((float) Math.atan2(4.0, 3.0),
                Raymath.Vector2Angle(new Vector2(1.0f, 1.0f), new Vector2(4.0f, 5.0f)), 0.000001f);
    }
}