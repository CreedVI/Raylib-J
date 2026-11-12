package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

class Vector4ZeroTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(0.0f, 0.0f, 0.0f, 0.0f, Raymath.Vector4Zero());
    }
}