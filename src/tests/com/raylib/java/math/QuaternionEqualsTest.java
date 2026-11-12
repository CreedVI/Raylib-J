package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuaternionEqualsTest {
    @Test
    void testMethod() {
        assertTrue(Raymath.QuaternionEquals(new Quaternion(0.0f, 0.0f, 0.5f, 0.5f),
                new Quaternion(0.0f, 0.0f, -0.5f, -0.5f)));
        assertFalse(Raymath.QuaternionEquals(new Quaternion(0.0f, 0.0f, 0.5f, 0.5f),
                new Quaternion(0.0f, 0.0f, 0.5f, -0.5f)));
    }
}