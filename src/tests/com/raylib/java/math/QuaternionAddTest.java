package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionAddTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(6.0f, 4.0f, 4.0f, 4.0f,
                Raymath.QuaternionAdd(new Quaternion(1.0f, 2.0f, 3.0f, 4.0f),
                        new Quaternion(5.0f, 2.0f, 1.0f, 0.0f)));
    }
}