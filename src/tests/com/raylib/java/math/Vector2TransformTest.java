package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2TransformTest {
    @Test
    void testMethod() {
        Matrix transform = new Matrix();
        transform.m0 = 2.0f;
        transform.m4 = 3.0f;
        transform.m12 = 5.0f;
        transform.m1 = 4.0f;
        transform.m5 = -1.0f;
        transform.m13 = 6.0f;

        assertVector2(13.0f, 8.0f, Raymath.Vector2Transform(new Vector2(1.0f, 2.0f), transform));
    }
}