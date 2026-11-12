package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4SubtractValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(-5.5f, -2.0f, -4.5f, 0.5f,
                Raymath.Vector4SubtractValue(new Vector4(-2.0f, 1.5f, -1.0f, 4.0f), 3.5f));
    }
}