package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3MoveTowardsTest {
    @Test
    void testMethod() {
        TestHelpers.assertVector3(2.2f, 2.6f, 1.0f,
                Raymath.Vector3MoveTowards(new Vector3(1.0f, 1.0f, 1.0f), new Vector3(4.0f, 5.0f, 1.0f), 2.0f));
    }
}