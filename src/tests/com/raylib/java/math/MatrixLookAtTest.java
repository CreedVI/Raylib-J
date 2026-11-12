package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class MatrixLookAtTest {
    @Test
    void testMethod() {
        Matrix expected = new Matrix(
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, -5.0f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertMatrix(expected,
                Raymath.MatrixLookAt(new Vector3(0.0f, 0.0f, 5.0f), new Vector3(0.0f, 0.0f, 0.0f),
                        new Vector3(0.0f, 1.0f, 0.0f)));
    }
}