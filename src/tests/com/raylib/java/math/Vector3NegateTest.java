package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3NegateTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(2.5f, -4.0f, 1.0f,
                Raymath.Vector3Negate(new Vector3(-2.5f, 4.0f, -1.0f)));
    }
}