package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4DivideTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(4.0f, -3.0f, -3.0f, 4.0f,
                Raymath.Vector4Divide(new Vector4(8.0f, -9.0f, 12.0f, -20.0f), new Vector4(2.0f, 3.0f, -4.0f, -5.0f)));
    }
}