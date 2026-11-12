package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector3AngleTest {
    @Test
    void testMethod() {
        assertEquals((float) (Math.PI / 2.0),
                Raymath.Vector3Angle(new Vector3(1.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f)), 0.00001f);
    }
}