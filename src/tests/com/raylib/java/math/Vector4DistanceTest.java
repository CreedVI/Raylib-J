package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector4DistanceTest {
    @Test
    void testMethod() {
        assertEquals((float) Math.sqrt(41.0),
                Raymath.Vector4Distance(new Vector4(-1.0f, 2.0f, 0.0f, 3.0f),
                        new Vector4(2.0f, 6.0f, 0.0f, -1.0f)), 0.00001f);
    }
}