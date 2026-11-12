package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3MultiplyTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(8.0f, 15.0f, -18.0f,
                Raymath.Vector3Multiply(new Vector3(2.0f, 3.0f, -6.0f), new Vector3(4.0f, 5.0f, 3.0f)));
    }
}