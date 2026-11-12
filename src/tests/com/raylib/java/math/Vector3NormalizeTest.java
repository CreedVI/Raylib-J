package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3NormalizeTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(2.0f / 7.0f, -3.0f / 7.0f, 6.0f / 7.0f,
                Raymath.Vector3Normalize(new Vector3(2.0f, -3.0f, 6.0f)));
        TestHelpers.assertVector3(0.0f, 0.0f, 0.0f,
                Raymath.Vector3Normalize(new Vector3(0.0f, 0.0f, 0.0f)));
    }
}