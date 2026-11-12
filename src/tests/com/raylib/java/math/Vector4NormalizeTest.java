package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4NormalizeTest {
    @Test
    void testMethod() {
        float inverseLength = (float) (1.0 / Math.sqrt(13.0));
        TestHelpers.assertVector4(2.0f * inverseLength, -2.0f * inverseLength, inverseLength, 2.0f * inverseLength,
                Raymath.Vector4Normalize(new Vector4(2.0f, -2.0f, 1.0f, 2.0f)));
        TestHelpers.assertVector4(0.0f, 0.0f, 0.0f, 0.0f,
                Raymath.Vector4Normalize(new Vector4(0.0f, 0.0f, 0.0f, 0.0f)));
    }
}