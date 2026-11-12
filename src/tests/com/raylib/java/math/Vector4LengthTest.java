package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector4LengthTest {
    @Test
    void testMethod() {
        assertEquals(5.0f, Raymath.Vector4Length(new Vector4(1.0f, 2.0f, 2.0f, 4.0f)), 0.00001f);
        assertEquals(0.0f, Raymath.Vector4Length(new Vector4(0.0f, 0.0f, 0.0f, 0.0f)), 0.00001f);
    }
}