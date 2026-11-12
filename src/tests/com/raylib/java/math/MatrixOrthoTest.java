package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixOrthoTest {
    @Test
    void testMethod() {
        Matrix expected = new Matrix(
                0.25f, 0.0f, 0.0f, -0.5f,
                0.0f, 0.25f, 0.0f, 0.0f,
                0.0f, 0.0f, -0.2f, -0.8f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertMatrix(expected, Raymath.MatrixOrtho(-2.0, 6.0, -4.0, 4.0, -1.0, 9.0));
    }
}