package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2SubtractValueTest {
    @Test
    void testMethod() {
        assertVector2(-5.5f, -1.0f, Raymath.Vector2SubtractValue(new Vector2(-2.0f, 2.5f), 3.5f));
    }
}