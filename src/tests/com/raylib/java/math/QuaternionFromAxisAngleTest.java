package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class QuaternionFromAxisAngleTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        TestHelpers.assertVector4(0.0f, 0.0f, half, half,
                Raymath.QuaternionFromAxisAngle(new Vector3(0.0f, 0.0f, 1.0f), (float) (Math.PI / 2.0)));
    }
}