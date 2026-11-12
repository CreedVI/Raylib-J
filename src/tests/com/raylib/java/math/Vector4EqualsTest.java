package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector4EqualsTest {
    @Test
    void testMethod() {
        assertTrue(Raymath.Vector4Equals(new Vector4(1.0f, 2.0f, 3.0f, 4.0f),
                new Vector4(1.000001f, 2.0f, 3.0f, 3.999999f)));
        assertFalse(Raymath.Vector4Equals(new Vector4(1.0f, 2.0f, 3.0f, 4.0f),
                new Vector4(1.0f, 2.0f, 3.0f, 4.00001f)));
    }
}