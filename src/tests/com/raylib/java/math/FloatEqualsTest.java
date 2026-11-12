package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloatEqualsTest {
    @Test
    void testMethod() {
        assertTrue(Raymath.FloatEquals(1.0f, 1.0f + 0.0000005f));
        assertFalse(Raymath.FloatEquals(1.0f, 1.0f + 0.000002f));
        assertTrue(Raymath.FloatEquals(1000.0f, 1000.0005f));
        assertFalse(Raymath.FloatEquals(1000.0f, 1000.002f));
    }
}