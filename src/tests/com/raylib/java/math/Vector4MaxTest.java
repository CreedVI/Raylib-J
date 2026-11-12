package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4MaxTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(2.0f, 5.0f, 6.0f, 7.0f,
                Raymath.Vector4Max(new Vector4(2.0f, -4.0f, 6.0f, -6.0f),
                        new Vector4(-3.0f, 5.0f, -5.0f, 7.0f)));
    }
}