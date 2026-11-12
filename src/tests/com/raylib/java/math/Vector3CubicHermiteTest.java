package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3CubicHermiteTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(1.875f, 2.375f, 3.375f,
                Raymath.Vector3CubicHermite(new Vector3(0.0f, 1.0f, 2.0f), new Vector3(1.0f, 0.0f, -1.0f),
                        new Vector3(3.0f, 4.0f, 5.0f), new Vector3(-2.0f, 1.0f, 0.0f), 0.5f));
    }
}