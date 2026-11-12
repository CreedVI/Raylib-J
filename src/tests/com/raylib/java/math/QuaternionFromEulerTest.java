package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

class QuaternionFromEulerTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        TestHelpers.assertVector4(0.0f, 0.0f, half, half,
                Raymath.QuaternionFromEuler((float) (Math.PI / 2.0), 0.0f, 0.0f));
    }
}