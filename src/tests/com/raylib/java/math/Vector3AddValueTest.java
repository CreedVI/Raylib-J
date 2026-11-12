package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3AddValueTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(1.5f, 6.0f, 2.5f,
                Raymath.Vector3AddValue(new Vector3(-2.0f, 2.5f, -1.0f), 3.5f));
    }
}