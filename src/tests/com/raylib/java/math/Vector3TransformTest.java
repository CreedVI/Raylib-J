package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3TransformTest {
    @Test
    void testMethod() {
        Matrix transform = new Matrix();
        transform.m0 = 2.0f;
        transform.m4 = 3.0f;
        transform.m8 = 4.0f;
        transform.m12 = 5.0f;
        transform.m1 = 6.0f;
        transform.m5 = 7.0f;
        transform.m9 = 8.0f;
        transform.m13 = 9.0f;
        transform.m2 = 10.0f;
        transform.m6 = 11.0f;
        transform.m10 = 12.0f;
        transform.m14 = 13.0f;

        TestHelpers.assertVector3(25.0f, 53.0f, 81.0f,
                Raymath.Vector3Transform(new Vector3(1.0f, 2.0f, 3.0f), transform));
    }
}