package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuaternionLengthTest {
    @Test
    void testMethod() {
        assertEquals(5.0f, Raymath.QuaternionLength(new Quaternion(1.0f, 2.0f, 2.0f, 4.0f)), 0.00001f);
    }
}