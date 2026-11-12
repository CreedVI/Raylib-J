package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Quaternion;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class QuaternionToAxisTest {
    @Test
    void testMethod() {
        float half = (float) Math.sqrt(0.5);

        // The primitive angle parameter is passed by value and cannot be observed by callers.
        Vector3 axis = Raymath.QuaternionToAxis(new Quaternion(0.0f, 0.0f, half, half));

        // This assertion intentionally exposes that reassigning outAxis does not mutate the caller's vector.
        TestHelpers.assertVector3(0.0f, 0.0f, 1.0f, axis);
    }
}