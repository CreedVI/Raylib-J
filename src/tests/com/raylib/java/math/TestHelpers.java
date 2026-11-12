package com.raylib.java.math;

import com.raylib.java.structs.Matrix;
import com.raylib.java.structs.Vector2;
import com.raylib.java.structs.Vector3;
import com.raylib.java.structs.Vector4;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class TestHelpers {

    private static final float DELTA = 0.00001f;

    static void assertVector2(float expectedX, float expectedY, Vector2 actual) {
        assertEquals(expectedX, actual.x, DELTA, "x component");
        assertEquals(expectedY, actual.y, DELTA, "y component");
    }

    static void assertVector3(float expectedX, float expectedY, float expectedZ, Vector3 actual) {
        assertEquals(expectedX, actual.x, DELTA, "x component");
        assertEquals(expectedY, actual.y, DELTA, "y component");
        assertEquals(expectedZ, actual.z, DELTA, "z component");
    }

    static void assertVector4(float expectedX, float expectedY, float expectedZ, float expectedW, Vector4 actual) {
        assertEquals(expectedX, actual.x, DELTA, "x component");
        assertEquals(expectedY, actual.y, DELTA, "y component");
        assertEquals(expectedZ, actual.z, DELTA, "z component");
        assertEquals(expectedW, actual.w, DELTA, "w component");
    }

    static void assertFloatArray(float[] expected, float[] actual) {
        assertArrayEquals(expected, actual, DELTA);
    }

    static void assertMatrix(Matrix expected, Matrix actual) {
        assertEquals(expected.m0, actual.m0, DELTA, "m0");
        assertEquals(expected.m1, actual.m1, DELTA, "m1");
        assertEquals(expected.m2, actual.m2, DELTA, "m2");
        assertEquals(expected.m3, actual.m3, DELTA, "m3");
        assertEquals(expected.m4, actual.m4, DELTA, "m4");
        assertEquals(expected.m5, actual.m5, DELTA, "m5");
        assertEquals(expected.m6, actual.m6, DELTA, "m6");
        assertEquals(expected.m7, actual.m7, DELTA, "m7");
        assertEquals(expected.m8, actual.m8, DELTA, "m8");
        assertEquals(expected.m9, actual.m9, DELTA, "m9");
        assertEquals(expected.m10, actual.m10, DELTA, "m10");
        assertEquals(expected.m11, actual.m11, DELTA, "m11");
        assertEquals(expected.m12, actual.m12, DELTA, "m12");
        assertEquals(expected.m13, actual.m13, DELTA, "m13");
        assertEquals(expected.m14, actual.m14, DELTA, "m14");
        assertEquals(expected.m15, actual.m15, DELTA, "m15");
    }

}
