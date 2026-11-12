package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionNlerpTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector4(0.0f, 0.0f, (float) Math.sin(Math.PI / 8.0),
                (float) Math.cos(Math.PI / 8.0),
                Raymath.QuaternionNlerp(new Quaternion(0.0f, 0.0f, 0.0f, 1.0f),
                        new Quaternion(0.0f, 0.0f, (float) Math.sqrt(0.5), (float) Math.sqrt(0.5)), 0.5f));
    }
}