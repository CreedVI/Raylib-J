package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixSubtractTest {
    @Test
    void testMethod() {
        Matrix left = new Matrix(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f,
                9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f);
        Matrix right = new Matrix(16.0f, 15.0f, 14.0f, 13.0f, 12.0f, 11.0f, 10.0f, 9.0f,
                8.0f, 7.0f, 6.0f, 5.0f, 4.0f, 3.0f, 2.0f, 1.0f);
        Matrix expected = new Matrix(-15.0f, -13.0f, -11.0f, -9.0f, -7.0f, -5.0f, -3.0f, -1.0f,
                1.0f, 3.0f, 5.0f, 7.0f, 9.0f, 11.0f, 13.0f, 15.0f);

        TestHelpers.assertMatrix(expected, Raymath.MatrixSubtract(left, right));
    }
}