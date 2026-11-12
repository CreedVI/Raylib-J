package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClampTest {
    @Test
    void testMethod() {
        assertEquals(2.0f, Raymath.Clamp(2.0f, 1.0f, 3.0f), 0.000001f);
        assertEquals(1.0f, Raymath.Clamp(-4.0f, 1.0f, 3.0f), 0.000001f);
        assertEquals(3.0f, Raymath.Clamp(8.0f, 1.0f, 3.0f), 0.000001f);
    }
}