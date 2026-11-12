package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3InvertTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(0.5f, -0.25f, 2.0f,
                Raymath.Vector3Invert(new Vector3(2.0f, -4.0f, 0.5f)));
    }
}