package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizeTest {

    @Test
    void testMethod() {
        assertEquals(0.5f, Raymath.Normalize(0.0f, -1.0f, 1.0f), 0.000001f);
        assertEquals(0.0f, Raymath.Normalize(2.0f, 2.0f, 6.0f), 0.000001f);
        assertEquals(1.0f, Raymath.Normalize(6.0f, 2.0f, 6.0f), 0.000001f);
    }
}