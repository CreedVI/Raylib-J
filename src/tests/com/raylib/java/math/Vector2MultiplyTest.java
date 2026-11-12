package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2MultiplyTest {
    @Test
    void testMethod() {
        assertVector2(8.0f, 15.0f, Raymath.Vector2Multiply(new Vector2(2.0f, 3.0f), new Vector2(4.0f, 5.0f)));
    }
}