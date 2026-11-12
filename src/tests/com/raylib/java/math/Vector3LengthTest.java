package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector3LengthTest {
    @Test
    void testMethod() {
        assertEquals(7.0f, Raymath.Vector3Length(new Vector3(2.0f, -3.0f, 6.0f)), 0.00001f);
        assertEquals(0.0f, Raymath.Vector3Length(new Vector3(0.0f, 0.0f, 0.0f)), 0.00001f);
    }
}