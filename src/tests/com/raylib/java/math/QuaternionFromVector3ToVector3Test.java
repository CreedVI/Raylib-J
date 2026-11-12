package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class QuaternionFromVector3ToVector3Test {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);
        TestHelpers.assertVector4(0.0f, 0.0f, half, half,
                Raymath.QuaternionFromVector3ToVector3(new Vector3(1.0f, 0.0f, 0.0f),
                        new Vector3(0.0f, 1.0f, 0.0f)));
    }
}