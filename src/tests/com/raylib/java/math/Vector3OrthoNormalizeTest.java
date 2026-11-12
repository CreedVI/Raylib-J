package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3OrthoNormalizeTest {
    @Test
    void testMethod() {
        Vector3 first = new Vector3(2.0f, 0.0f, 0.0f);
        Vector3 second = new Vector3(1.0f, 2.0f, 0.0f);

        Raymath.Vector3OrthoNormalize(first, second);

        TestHelpers.assertVector3(1.0f, 0.0f, 0.0f, first);
        TestHelpers.assertVector3(0.0f, 1.0f, 0.0f, second);
    }
}