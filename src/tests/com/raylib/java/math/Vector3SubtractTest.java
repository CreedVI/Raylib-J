package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3SubtractTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(7.0f, -6.0f, -1.0f,
                Raymath.Vector3Subtract(new Vector3(5.0f, -2.0f, 4.0f), new Vector3(-2.0f, 4.0f, 5.0f)));
    }
}