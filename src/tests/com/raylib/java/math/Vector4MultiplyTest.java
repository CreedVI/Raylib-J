package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4MultiplyTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(8.0f, -6.0f, -12.0f, -5.0f,
                Raymath.Vector4Multiply(new Vector4(2.0f, 3.0f, -4.0f, 5.0f),
                        new Vector4(4.0f, -2.0f, 3.0f, -1.0f)));
    }
}