package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemapTest {
    @Test
    void testMethod() {
        assertEquals(1.0f, Raymath.Remap(4.0f, 2.0f, 6.0f, -3.0f, 5.0f), 0.000001f);
        assertEquals(-3.0f, Raymath.Remap(2.0f, 2.0f, 6.0f, -3.0f, 5.0f), 0.000001f);
        assertEquals(5.0f, Raymath.Remap(6.0f, 2.0f, 6.0f, -3.0f, 5.0f), 0.000001f);
    }
}