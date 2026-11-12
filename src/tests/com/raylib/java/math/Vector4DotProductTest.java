package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector4DotProductTest {
    @Test
    void testMethod() {
        assertEquals(4.0f, Raymath.Vector4DotProduct(new Vector4(1.0f, 2.0f, -3.0f, 4.0f),
                new Vector4(-2.0f, 5.0f, 2.0f, 0.5f)), 0.00001f);
    }
}