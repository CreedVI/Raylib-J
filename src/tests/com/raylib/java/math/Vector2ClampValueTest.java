package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2ClampValueTest {
    @Test
    void testMethod() {
        assertVector2(2.0f, 0.0f, Raymath.Vector2ClampValue(new Vector2(1.0f, 0.0f), 2.0f, 4.0f));
        assertVector2(3.0f, 4.0f, Raymath.Vector2ClampValue(new Vector2(6.0f, 8.0f), 1.0f, 5.0f));
    }
}