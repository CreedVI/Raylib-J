package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3RotateByAxisAngleTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(0.0f, 1.0f, 0.0f,
                Raymath.Vector3RotateByAxisAngle(new Vector3(1.0f, 0.0f, 0.0f),
                        new Vector3(0.0f, 0.0f, 1.0f), (float) (Math.PI / 2.0)));
    }
}