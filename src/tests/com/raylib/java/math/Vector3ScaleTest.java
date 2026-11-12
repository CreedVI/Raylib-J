package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3ScaleTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(-2.5f, 5.0f, 7.5f,
                Raymath.Vector3Scale(new Vector3(-1.0f, 2.0f, 3.0f), 2.5f));
    }
}