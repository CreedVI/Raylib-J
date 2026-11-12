package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionInvertTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(-1.0f / 13.0f, -2.0f / 13.0f, -2.0f / 13.0f, 2.0f / 13.0f,
                Raymath.QuaternionInvert(new Quaternion(1.0f, 2.0f, 2.0f, 2.0f)));
    }
}