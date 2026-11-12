package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2AddTest {
    @Test
    void testMethod() {
        assertVector2(3.0f, 2.0f, Raymath.Vector2Add(new Vector2(5.0f, -2.0f), new Vector2(-2.0f, 4.0f)));
    }
}