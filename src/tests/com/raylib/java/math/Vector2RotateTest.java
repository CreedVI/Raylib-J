package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2RotateTest {
    @Test
    void testMethod() {
        assertVector2(0.0f, 1.0f, Raymath.Vector2Rotate(new Vector2(1.0f, 0.0f), (float) (Math.PI / 2.0)));
    }
}