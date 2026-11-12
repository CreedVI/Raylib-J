package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionAddValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(4.5f, 0.5f, 4.0f, 7.5f,
                Raymath.QuaternionAddValue(new Quaternion(1.0f, -3.0f, 0.5f, 4.0f), 3.5f));
    }
}