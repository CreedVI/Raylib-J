package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4SubtractTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(3.0f, -1.0f, -8.0f, 5.0f,
                Raymath.Vector4Subtract(new Vector4(1.0f, 2.0f, -3.0f, 4.0f), new Vector4(-2.0f, 3.0f, 5.0f, -1.0f)));
    }
}