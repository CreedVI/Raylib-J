package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2NormalizeTest {
    @Test
    void testMethod() {
        assertVector2(0.6f, 0.8f, Raymath.Vector2Normalize(new Vector2(3.0f, 4.0f)));
        assertVector2(0.0f, 0.0f, Raymath.Vector2Normalize(new Vector2(0.0f, 0.0f)));
    }
}