package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2LengthTest {
    @Test
    void testMethod() {
        assertEquals(5.0f, Raymath.Vector2Length(new Vector2(3.0f, 4.0f)), 0.000001f);
        assertEquals(0.0f, Raymath.Vector2Length(new Vector2(0.0f, 0.0f)), 0.000001f);
    }
}