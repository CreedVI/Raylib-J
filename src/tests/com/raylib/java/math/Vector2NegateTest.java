package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2NegateTest {
    @Test
    void testMethod() {
        assertVector2(2.5f, -4.0f, Raymath.Vector2Negate(new Vector2(-2.5f, 4.0f)));
    }
}