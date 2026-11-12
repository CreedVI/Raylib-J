package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class QuaternionFromMatrixTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        Matrix rotation = new Matrix(
                0.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertVector4(0.0f, 0.0f, half, half, Raymath.QuaternionFromMatrix(rotation));
    }
}