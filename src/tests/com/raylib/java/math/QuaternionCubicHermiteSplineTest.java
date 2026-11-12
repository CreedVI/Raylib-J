package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionCubicHermiteSplineTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        TestHelpers.assertVector4(half, half, 0.0f, 0.0f,
                Raymath.QuaternionCubicHermiteSpline(new Quaternion(1.0f, 0.0f, 0.0f, 0.0f),
                        new Quaternion(0.0f, 0.0f, 0.0f, 0.0f), new Quaternion(0.0f, 1.0f, 0.0f, 0.0f),
                        new Quaternion(0.0f, 0.0f, 0.0f, 0.0f), 0.5f));
    }
}