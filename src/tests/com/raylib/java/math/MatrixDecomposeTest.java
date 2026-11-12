package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Quaternion;
import com.raylib.java.structs.Vector3;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class MatrixDecomposeTest {
    @Test
    void testMethod() {
        Matrix matrix = new Matrix(
                2.0f, 0.0f, 0.0f, 5.0f,
                0.0f, 3.0f, 0.0f, -2.0f,
                0.0f, 0.0f, 4.0f, 7.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
        Vector3 translation = new Vector3(-1.0f, -1.0f, -1.0f);
        Quaternion rotation = new Quaternion(1.0f, 1.0f, 1.0f, 1.0f);
        Vector3 scale = new Vector3(-1.0f, -1.0f, -1.0f);

        Raymath.MatrixDecompose(matrix, translation, rotation, scale);

        TestHelpers.assertVector3(5.0f, -2.0f, 7.0f, translation);
        TestHelpers.assertVector4(0.0f, 0.0f, 0.0f, 1.0f,
                new Vector4(rotation.x, rotation.y, rotation.z, rotation.w));
        TestHelpers.assertVector3(2.0f, 3.0f, 4.0f, scale);
    }
}