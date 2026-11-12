package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import org.junit.jupiter.api.Test;

class Vector3OneTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(1.0f, 1.0f, 1.0f, Raymath.Vector3One());
    }
}