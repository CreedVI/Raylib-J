package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixMultiplyTest {
    @Test
    void testMethod() {
        Matrix translation = new Matrix(
                1.0f, 0.0f, 0.0f, 5.0f,
                0.0f, 1.0f, 0.0f, -2.0f,
                0.0f, 0.0f, 1.0f, 7.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
        Matrix scale = new Matrix(
                2.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 3.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 4.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
        Matrix expected = new Matrix(
                2.0f, 0.0f, 0.0f, 10.0f,
                0.0f, 3.0f, 0.0f, -6.0f,
                0.0f, 0.0f, 4.0f, 28.0f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertMatrix(expected, Raymath.MatrixMultiply(translation, scale));
    }
}