package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionDivideTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(4.0f, -3.0f, 2.0f, -2.0f,
                Raymath.QuaternionDivide(new Quaternion(8.0f, -9.0f, 12.0f, -20.0f),
                        new Quaternion(2.0f, 3.0f, 6.0f, 10.0f)));
    }
}