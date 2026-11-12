package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2CrossProductTest {
    @Test
    void testMethod() {
        assertEquals(11.0f, Raymath.Vector2CrossProduct(new Vector2(2.0f, 3.0f), new Vector2(-1.0f, 4.0f)), 0.000001f);
        assertEquals(1.0f, Raymath.Vector2CrossProduct(new Vector2(1.0f, 0.0f), new Vector2(0.0f, 1.0f)), 0.000001f);
    }
}