package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3ClampValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(1.2f, 1.6f, 0.0f,
                Raymath.Vector3ClampValue(new Vector3(0.6f, 0.8f, 0.0f), 2.0f, 5.0f));
    }
}