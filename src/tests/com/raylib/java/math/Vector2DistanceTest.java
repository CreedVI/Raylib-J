package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2DistanceTest {
    @Test
    void testMethod() {
        assertEquals(5.0f, Raymath.Vector2Distance(new Vector2(-1.0f, 2.0f), new Vector2(2.0f, 6.0f)), 0.000001f);
    }
}