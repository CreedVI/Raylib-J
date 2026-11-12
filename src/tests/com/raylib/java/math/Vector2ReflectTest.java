package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2ReflectTest {
    @Test
    void testMethod() {
        assertVector2(2.0f, 3.0f, Raymath.Vector2Reflect(new Vector2(2.0f, -3.0f), new Vector2(0.0f, 1.0f)));
    }
}