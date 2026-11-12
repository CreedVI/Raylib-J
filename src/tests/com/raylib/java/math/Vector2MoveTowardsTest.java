package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Vector2;
import org.junit.jupiter.api.Test;

import static com.raylib.java.math.TestHelpers.assertVector2;

class Vector2MoveTowardsTest {
    @Test
    void testMethod() {
        assertVector2(1.2f, 1.6f,
                Raymath.Vector2MoveTowards(new Vector2(0.0f, 0.0f), new Vector2(3.0f, 4.0f), 2.0f));
        assertVector2(3.0f, 4.0f,
                Raymath.Vector2MoveTowards(new Vector2(0.0f, 0.0f), new Vector2(3.0f, 4.0f), 5.0f));
    }
}