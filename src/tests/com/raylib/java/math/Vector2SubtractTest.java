package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2SubtractTest {
    @Test
    void testMethod() {
        assertVector2(7.0f, -6.0f, Raymath.Vector2Subtract(new Vector2(5.0f, -2.0f), new Vector2(-2.0f, 4.0f)));
    }
}