package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WrapTest {
    @Test
    void testMethod() {
        assertEquals(4.0f, Raymath.Wrap(-1.0f, 0.0f, 5.0f), 0.000001f);
        assertEquals(0.0f, Raymath.Wrap(5.0f, 0.0f, 5.0f), 0.000001f);
        assertEquals(2.0f, Raymath.Wrap(12.0f, 2.0f, 7.0f), 0.000001f);
    }
}