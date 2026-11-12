package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector2EqualsTest {
    @Test
    void testMethod() {
        assertTrue(Raymath.Vector2Equals(new Vector2(3.0f, 4.0f), new Vector2(3.000001f, 3.999999f)));
        assertFalse(Raymath.Vector2Equals(new Vector2(3.0f, 4.0f), new Vector2(3.00001f, 4.0f)));
    }
}