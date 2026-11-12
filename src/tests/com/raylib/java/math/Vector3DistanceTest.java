package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector3DistanceTest {
    @Test
    void testMethod() {
        assertEquals(5.0f,
                Raymath.Vector3Distance(new Vector3(-1.0f, 2.0f, 3.0f), new Vector3(2.0f, 6.0f, 3.0f)), 0.00001f);
    }
}