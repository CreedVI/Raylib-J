package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LerpTest {
    @Test
    void testMethod() {
        assertEquals(-2.0f, Raymath.Lerp(-2.0f, 6.0f, 0.0f), 0.000001f);
        assertEquals(2.0f, Raymath.Lerp(-2.0f, 6.0f, 0.5f), 0.000001f);
        assertEquals(6.0f, Raymath.Lerp(-2.0f, 6.0f, 1.0f), 0.000001f);
    }
}