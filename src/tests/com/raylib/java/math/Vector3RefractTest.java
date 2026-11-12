package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3RefractTest {
    @Test
    void testMethod() {
        float transmittedZ = (float) Math.sqrt(0.91);
        TestHelpers.assertVector3(0.3f, -transmittedZ, 0.0f,
                Raymath.Vector3Refract(new Vector3(0.6f, -0.8f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f), 0.5f));
        TestHelpers.assertVector3(0.0f, 0.0f, 0.0f,
                Raymath.Vector3Refract(new Vector3(1.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f), 2.0f));
    }
}