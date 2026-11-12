package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4NegateTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(2.5f, -4.0f, 1.0f, -3.0f,
                Raymath.Vector4Negate(new Vector4(-2.5f, 4.0f, -1.0f, 3.0f)));
    }
}