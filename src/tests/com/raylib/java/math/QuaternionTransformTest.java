package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionTransformTest {
    @Test
    void testMethod() {
        Matrix transform = new Matrix(
                2.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 3.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 4.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 5.0f);

        TestHelpers.assertVector4(2.0f, 6.0f, 12.0f, 20.0f,
                Raymath.QuaternionTransform(new Quaternion(1.0f, 2.0f, 3.0f, 4.0f), transform));
    }
}