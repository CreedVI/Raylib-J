package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionMultiplyTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(24.0f, 48.0f, 48.0f, -6.0f,
                Raymath.QuaternionMultiply(new Quaternion(1.0f, 2.0f, 3.0f, 4.0f),
                        new Quaternion(5.0f, 6.0f, 7.0f, 8.0f)));
    }
}