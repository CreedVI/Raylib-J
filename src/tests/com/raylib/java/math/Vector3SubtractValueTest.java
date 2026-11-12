package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3SubtractValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(-5.5f, -1.0f, -4.5f,
                Raymath.Vector3SubtractValue(new Vector3(-2.0f, 2.5f, -1.0f), 3.5f));
    }
}