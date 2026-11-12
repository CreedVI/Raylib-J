package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Quaternion;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class MatrixComposeTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        Matrix expected = new Matrix(
                0.0f, -3.0f, 0.0f, 5.0f,
                2.0f, 0.0f, 0.0f, -2.0f,
                0.0f, 0.0f, 4.0f, 7.0f,
                0.0f, 0.0f, 0.0f, 1.0f);

        TestHelpers.assertMatrix(expected,
                Raymath.MatrixCompose(new Vector3(5.0f, -2.0f, 7.0f), new Quaternion(0.0f, 0.0f, half, half),
                        new Vector3(2.0f, 3.0f, 4.0f)));
    }
}