package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector4;
import org.junit.jupiter.api.Test;

class Vector4ScaleTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(-2.5f, 5.0f, 7.5f, -10.0f,
                Raymath.Vector4Scale(new Vector4(-1.0f, 2.0f, 3.0f, -4.0f), 2.5f));
    }
}