package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixIdentityTest {
    @Test
    void testMethod() {
        TestHelpers.assertMatrix(new Matrix(
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f), Raymath.MatrixIdentity());
    }
}