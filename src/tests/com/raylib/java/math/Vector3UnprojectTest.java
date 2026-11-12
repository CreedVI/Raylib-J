package com.raylib.java.math;

import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector3;
import org.junit.jupiter.api.Test;

class Vector3UnprojectTest {
    @Test
    void testMethod() {
        Matrix identity = new Matrix();
        identity.m0 = 1.0f;
        identity.m5 = 1.0f;
        identity.m10 = 1.0f;
        identity.m15 = 1.0f;

        TestHelpers.assertVector3(0.25f, -0.5f, 0.75f,
                Raymath.Vector3Unproject(new Vector3(0.25f, -0.5f, 0.75f), identity, identity));
    }
}