package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionScaleTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(-2.5f, 5.0f, 7.5f, -10.0f,
                Raymath.QuaternionScale(new Quaternion(-1.0f, 2.0f, 3.0f, -4.0f), 2.5f));
    }
}