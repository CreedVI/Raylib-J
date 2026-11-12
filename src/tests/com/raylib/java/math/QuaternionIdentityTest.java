package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

class QuaternionIdentityTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(0.0f, 0.0f, 0.0f, 1.0f, Raymath.QuaternionIdentity());
    }
}