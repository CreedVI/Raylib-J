package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3PerpendicularTest {
    @Test
    void testMethod() {
        Vector3 input = new Vector3(0.0f, 2.0f, 3.0f);
        Vector3 perpendicular = Raymath.Vector3Perpendicular(input);

        TestHelpers.assertVector3(0.0f, 3.0f, -2.0f, perpendicular);
    }
}