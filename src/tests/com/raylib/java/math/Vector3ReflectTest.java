package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3ReflectTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(1.0f, 2.0f, 3.0f,
                Raymath.Vector3Reflect(new Vector3(1.0f, -2.0f, 3.0f), new Vector3(0.0f, 1.0f, 0.0f)));
    }
}