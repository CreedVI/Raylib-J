package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

class Vector3ZeroTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(0.0f, 0.0f, 0.0f, Raymath.Vector3Zero());
    }
}