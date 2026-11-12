package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class MatrixRotateTest {
    @Test
    void testMethod() {
        Matrix expected = new Matrix(
                0.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertMatrix(expected,
                Raymath.MatrixRotate(new Vector3(0.0f, 0.0f, 1.0f), (float) (Math.PI / 2.0)));
    }
}