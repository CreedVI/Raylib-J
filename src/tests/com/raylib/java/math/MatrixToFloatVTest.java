package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Float16;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

class MatrixToFloatVTest {
    @Test
    void testMethod() {
        Matrix matrix = new Matrix(1.0f, 5.0f, 9.0f, 13.0f, 2.0f, 6.0f, 10.0f, 14.0f,
                3.0f, 7.0f, 11.0f, 15.0f, 4.0f, 8.0f, 12.0f, 16.0f);
        Float16 actual = Raymath.MatrixToFloatV(matrix);

        TestHelpers.assertFloatArray(new float[]{1.0f, 2.0f, 3.0f, 4.0f,
                5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f,
                13.0f, 14.0f, 15.0f, 16.0f}, actual.v);
    }
}