package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuaternionToAngleTest {
    @Test
    void testMethod() {
        float result = Raymath.QuaternionToAngle(new Quaternion(1.0f, 2.0f, 3.0f, 1.0f));

        assertEquals(0.0f, result);
    }
}
