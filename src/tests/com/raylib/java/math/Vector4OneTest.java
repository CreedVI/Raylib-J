package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

class Vector4OneTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(1.0f, 1.0f, 1.0f, 1.0f, Raymath.Vector4One());
    }
}