package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixPerspectiveTest {
    @Test
    void testMethod() {
        Matrix expected = new Matrix(
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, -1.25f, -2.25f,
                0.0f, 0.0f, -1.0f, 0.0f);

        TestHelpers.assertMatrix(expected, Raymath.MatrixPerspective(Math.PI / 2.0, 1.0, 1.0, 9.0));
    }
}