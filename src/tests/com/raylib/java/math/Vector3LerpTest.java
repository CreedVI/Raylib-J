package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3LerpTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(-0.5f, 1.5f, 5.5f,
                Raymath.Vector3Lerp(new Vector3(-2.0f, 3.0f, 7.0f), new Vector3(4.0f, -3.0f, 1.0f), 0.25f));
    }
}