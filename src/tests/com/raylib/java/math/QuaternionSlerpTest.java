package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

class QuaternionSlerpTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        TestHelpers.assertVector4(0.0f, 0.0f, (float) Math.sin(Math.PI / 8.0),
                (float) Math.cos(Math.PI / 8.0),
                Raymath.QuaternionSlerp(new Quaternion(0.0f, 0.0f, 0.0f, 1.0f),
                        new Quaternion(0.0f, 0.0f, -half, -half), 0.5f));
    }
}