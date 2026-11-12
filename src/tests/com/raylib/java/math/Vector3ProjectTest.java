package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3ProjectTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(0.0f, 3.0f, 0.0f,
                Raymath.Vector3Project(new Vector3(2.0f, 3.0f, 4.0f), new Vector3(0.0f, 2.0f, 0.0f)));
    }
}