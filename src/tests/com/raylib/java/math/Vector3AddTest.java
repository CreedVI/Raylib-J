package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3AddTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(3.0f, 2.0f, 9.0f,
                Raymath.Vector3Add(new Vector3(5.0f, -2.0f, 4.0f), new Vector3(-2.0f, 4.0f, 5.0f)));
    }
}