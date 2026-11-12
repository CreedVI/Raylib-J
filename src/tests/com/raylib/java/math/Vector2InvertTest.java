package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2InvertTest {
    @Test
    void testMethod() {
        assertVector2(0.5f, -0.25f, Raymath.Vector2Invert(new Vector2(2.0f, -4.0f)));
    }
}