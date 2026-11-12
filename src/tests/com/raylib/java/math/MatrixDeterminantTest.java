package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatrixDeterminantTest {
    @Test
    void testMethod() {
        Matrix diagonal = new Matrix(2.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 3.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 4.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 5.0f);

        assertEquals(120.0f, Raymath.MatrixDeterminant(diagonal), 0.00001f);
    }
}