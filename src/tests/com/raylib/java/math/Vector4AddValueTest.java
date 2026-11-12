package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4AddValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(1.5f, 5.0f, 2.5f, 7.5f,
                Raymath.Vector4AddValue(new Vector4(-2.0f, 1.5f, -1.0f, 4.0f), 3.5f));
    }
}