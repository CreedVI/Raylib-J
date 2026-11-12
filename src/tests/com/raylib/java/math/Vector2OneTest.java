package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2OneTest {
    @Test
    void testMethod() {
        assertVector2(1.0f, 1.0f, Raymath.Vector2One());
    }
}