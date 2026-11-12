package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3ClampTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(-2.0f, 2.0f, 6.0f,
                Raymath.Vector3Clamp(new Vector3(-4.0f, 2.0f, 8.0f), new Vector3(-2.0f, 0.0f, 1.0f),
                        new Vector3(3.0f, 4.0f, 6.0f)));
    }
}