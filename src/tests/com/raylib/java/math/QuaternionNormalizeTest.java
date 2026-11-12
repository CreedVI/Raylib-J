package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionNormalizeTest {
    @Test
    void testMethod() {
        float inverseLength = (float) (1.0 / Math.sqrt(13.0));
        TestHelpers.assertVector4(2.0f * inverseLength, -2.0f * inverseLength, inverseLength, 2.0f * inverseLength,
                Raymath.QuaternionNormalize(new Quaternion(2.0f, -2.0f, 1.0f, 2.0f)));
        TestHelpers.assertVector4(0.0f, 0.0f, 0.0f, 0.0f,
                Raymath.QuaternionNormalize(new Quaternion(0.0f, 0.0f, 0.0f, 0.0f)));
    }
}