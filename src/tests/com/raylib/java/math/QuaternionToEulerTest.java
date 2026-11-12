package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionToEulerTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(90.0f, 0.0f, 0.0f,
                Raymath.QuaternionToEuler(new Quaternion((float) Math.sqrt(0.5), 0.0f, 0.0f,
                        (float) Math.sqrt(0.5))));
    }
}