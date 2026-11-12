package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3CrossProductTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(-3.0f, 6.0f, -3.0f,
                Raymath.Vector3CrossProduct(new Vector3(1.0f, 2.0f, 3.0f), new Vector3(4.0f, 5.0f, 6.0f)));
    }
}