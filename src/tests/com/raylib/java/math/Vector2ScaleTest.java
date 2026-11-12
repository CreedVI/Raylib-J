package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2ScaleTest {
    @Test
    void testMethod() {
        assertVector2(-6.0f, 9.0f, Raymath.Vector2Scale(new Vector2(2.0f, -3.0f), -3.0f));
    }
}