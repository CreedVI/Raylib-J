package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixInvertTest {
    @Test
    void testMethod() {
        Matrix matrix = new Matrix(
                2.0f, 0.0f, 0.0f, 5.0f,
                0.0f, 3.0f, 0.0f, -2.0f,
                0.0f, 0.0f, 4.0f, 7.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
        Matrix expected = new Matrix(
                0.5f, 0.0f, 0.0f, -2.5f,
                0.0f, 1.0f / 3.0f, 0.0f, 2.0f / 3.0f,
                0.0f, 0.0f, 0.25f, -1.75f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertMatrix(expected, Raymath.MatrixInvert(matrix));
    }
}