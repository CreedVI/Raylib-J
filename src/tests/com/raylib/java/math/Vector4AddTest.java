package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4AddTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(-1.0f, 5.0f, 2.0f, 3.0f,
                Raymath.Vector4Add(new Vector4(1.0f, 2.0f, -3.0f, 4.0f), new Vector4(-2.0f, 3.0f, 5.0f, -1.0f)));
    }
}