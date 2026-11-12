package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4InvertTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(0.5f, -0.25f, 2.0f, -4.0f,
                Raymath.Vector4Invert(new Vector4(2.0f, -4.0f, 0.5f, -0.25f)));
    }
}