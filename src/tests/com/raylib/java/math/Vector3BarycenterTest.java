package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3BarycenterTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(0.25f, 0.25f, 0.5f,
                Raymath.Vector3Barycenter(new Vector3(0.25f, 0.5f, 0.0f), new Vector3(0.0f, 0.0f, 0.0f),
                        new Vector3(1.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f)));
    }
}