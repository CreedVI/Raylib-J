package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector3EqualsTest {
    @Test
    void testMethod() {
        assertTrue(Raymath.Vector3Equals(new Vector3(1.0f, 2.0f, 3.0f),
                new Vector3(1.000001f, 2.0f, 2.999999f)));
        assertFalse(Raymath.Vector3Equals(new Vector3(1.0f, 2.0f, 3.0f),
                new Vector3(1.00001f, 2.0f, 3.0f)));
    }
}