package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2LerpTest {
    @Test
    void testMethod() {
        assertVector2(2.0f, 3.0f, Raymath.Vector2Lerp(new Vector2(-2.0f, 7.0f), new Vector2(6.0f, -1.0f), 0.5f));
    }
}