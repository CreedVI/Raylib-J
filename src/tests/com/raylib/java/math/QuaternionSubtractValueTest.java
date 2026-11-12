package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionSubtractValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(-2.5f, -6.5f, -3.0f, 0.5f,
                Raymath.QuaternionSubtractValue(new Quaternion(1.0f, -3.0f, 0.5f, 4.0f), 3.5f));
    }
}