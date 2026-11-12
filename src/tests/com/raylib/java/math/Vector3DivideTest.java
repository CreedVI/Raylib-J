package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3DivideTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(4.0f, -3.0f, -3.0f,
                Raymath.Vector3Divide(new Vector3(8.0f, -9.0f, 12.0f), new Vector3(2.0f, 3.0f, -4.0f)));
    }
}