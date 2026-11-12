package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2RefractTest {
    @Test
    void testMethod() {
        float transmittedY = (float) Math.sqrt(0.91);
        assertVector2(0.3f, -transmittedY,
                Raymath.Vector2Refract(new Vector2(0.6f, -0.8f), new Vector2(0.0f, 1.0f), 0.5f));
        assertVector2(0.0f, 0.0f,
                Raymath.Vector2Refract(new Vector2(1.0f, 0.0f), new Vector2(0.0f, 1.0f), 2.0f));
    }
}