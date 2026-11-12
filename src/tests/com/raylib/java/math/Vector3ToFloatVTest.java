package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Float3;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3ToFloatVTest {
    @Test
    void testMethod() {
        Float3 actual = new Raymath().Vector3ToFloatV(new Vector3(2.5f, -4.0f, 7.25f));

        TestHelpers.assertFloatArray(new float[]{2.5f, -4.0f, 7.25f}, actual.v);
    }
}