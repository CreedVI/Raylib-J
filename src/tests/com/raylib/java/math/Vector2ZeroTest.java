package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2ZeroTest {
    @Test
    void testMethod() {
        assertVector2(0.0f, 0.0f, Raymath.Vector2Zero());
    }
}