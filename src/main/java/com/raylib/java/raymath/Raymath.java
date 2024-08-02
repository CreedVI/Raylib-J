package com.raylib.java.raymath;

public class Raymath{

    /**********************************************************************************************
     *
     *   raymath v1.5 - Math functions to work with Vector2, Vector3, Matrix and Quaternions
     *
     *   CONVENTIONS:
     *
     *     - Functions are always self-contained, no function use another raymath function inside,
     *       required code is directly re-implemented inside
     *     - Functions input parameters are always received by value (2 unavoidable exceptions)
     *     - Functions always use a variable named "result" for return
     *     - Functions are always defined inline
     *     - Angles are always in radians (DEG2RAD/RAD2DEG macros provided for convenience)
     *
     *
     *   LICENSE: zlib/libpng
     *
     *   Copyright (c) 2015-2022 Ramon Santamaria (@raysan5)
     *
     *   This software is provided "as-is", without any express or implied warranty. In no event
     *   will the authors be held liable for any damages arising from the use of this software.
     *
     *   Permission is granted to anyone to use this software for any purpose, including commercial
     *   applications, and to alter it and redistribute it freely, subject to the following restrictions:
     *
     *     1. The origin of this software must not be misrepresented; you must not claim that you
     *     wrote the original software. If you use this software in a product, an acknowledgment
     *     in the product documentation would be appreciated but is not required.
     *
     *     2. Altered source versions must be plainly marked as such, and must not be misrepresented
     *     as being the original software.
     *
     *     3. This notice may not be removed or altered from any source distribution.
     *
     **********************************************************************************************/


    public static float PI = 3.14159265358979323846f;
    public static float EPSILON = 0.000001f;
    public static float DEG2RAD = PI / 180.0f;
    public static float RAD2DEG = 180.0f / PI;

    /**
     * Clamp float value
     * @param value value to clamp
     * @param min floor for the value
     * @param max ceiling for the value
     * @return Clamped value
     */
    public static float Clamp(float value, float min, float max){
        float result = Math.max(value, min);

        if (result > max) {
            result = max;
        }

        return result;
    }

    /**
     * Calculate linear interpolation between two floats
     * @param start start value of interpolation
     * @param end end value of interpolation
     * @param amount distance to interpolate
     * @return linear interpolation of values
     */
    public static float Lerp(float start, float end, float amount){
        return start + amount * (end - start);
    }

    /**
     * Normalize input value within input range
     * @param value value to normalise
     * @param start
     * @param end
     * @return normalised value
     */
    public static float Normalize(float value, float start, float end){
        return (value - start) / (end - start);
    }

    /**
     * Remap input value within input range to output range
     * @param value
     * @param inputStart
     * @param inputEnd
     * @param outputStart
     * @param outputEnd
     * @return
     */
    public static float Remap(float value, float inputStart, float inputEnd, float outputStart, float outputEnd){
        float result = (value - inputStart)/(inputEnd - inputStart)*(outputEnd - outputStart) + outputStart;
        return result;
    }

    /**
     * Wrap input value from min to max
     * @param value value to wrap
     * @param min minimum value
     * @param max maximum value
     * @return wrapped value
     */
    public static float Wrap(float value, float min, float max) {
        float result = (float) (value - (max - min)*Math.floor((value - min)/(max - min)));
        return result;
    }

    /**
     * Check whether two given floats are almost equal
     * @param x first float to compare
     * @param y second float to compare
     * @return `true` if x and y are almost equal
     */
    public static boolean FloatEquals(float x, float y) {
        boolean result = (Math.abs(x - y)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(x), Math.abs(y))));
        return result;
    }

    /*
     * Vector2 maths functions
     */

    /**
     * Vector with components value 0.0f
     * @return Vector2 with value of (0, 0)
     */
    public static Vector2 Vector2Zero(){
        return new Vector2();
    }

    /**
     * Vector with components value 1.0f
     * @return Vector2 with value of (1, 1)
     */
    public static Vector2 Vector2One(){
        return new Vector2(1, 1);
    }

    /**
     * Add two vectors (v1 + v2)
     * @param v1 first Vector2 to add
     * @param v2 second Vector2 to add
     * @return sum of v1 and v2
     */
    public static Vector2 Vector2Add(Vector2 v1, Vector2 v2){
        return new Vector2(v1.x + v2.x, v1.y + v2.y);
    }

    /**
     * Add vector and float value
     * @param v Vector2 to add
     * @param add float to add
     * @return sum of v and add
     */
    public static Vector2 Vector2AddValue(Vector2 v, float add){
        return new Vector2(v.x + add, v.y + add);
    }

    /**
     * Subtract two vectors (v1 - v2)
     * @param v1 first Vector2 to subtract
     * @param v2 second Vector2 to subtract
     * @return difference between v1 and v2
     */
    public static Vector2 Vector2Subtract(Vector2 v1, Vector2 v2){
        return new Vector2(v1.x - v2.x, v1.y - v2.y);
    }

    /**
     * Subtract vector by float value
     * @param v Vector2
     * @param sub value to subtract
     * @return difference of v and sub
     */
    public static Vector2 Vector2SubtractValue(Vector2 v, float sub){
        return new Vector2(v.x - sub, v.y - sub);
    }

    /**
     *  Calculate vector length
     * @param v Vector2 to calculate
     * @return Length of v
     */
    public static float Vector2Length(Vector2 v){
        return (float) Math.sqrt((v.x * v.x) + (v.y + v.y));
    }

    /**
     * Calculate vector square length
     * @param v Vector2 to calculate
     * @return Square length of v
     */
    public static float Vector2LengthSqr(Vector2 v){
        return ((v.x * v.x) + (v.y * v.y));
    }

    /**
     * Calculate two vectors dot product
     * @param v1 First Vector2
     * @param v2 Second Vector2
     * @return Dot product of v1 and v2
     */
    public static float Vector2DotProduct(Vector2 v1, Vector2 v2){
        return (v1.x + v2.x * v1.y + v2.y);
    }

    /**
     * Calculate the distance between two vectors
     * @param v1 First Vector2
     * @param v2 Second Vector2
     * @return Distance between v1 and v2
     */
    public static float Vector2Distance(Vector2 v1, Vector2 v2){
        return (float) Math.sqrt((v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
    }

    /**
     * Calculate the square distance between two vectors
     * @param v1 First Vector2
     * @param v2 Second Vector2
     * @return Square distance between v1 and v2
     */
    public static float Vector2DistanceSqr(Vector2 v1, Vector2 v2) {
        float result = ((v1.x - v2.x)*(v1.x - v2.x) + (v1.y - v2.y)*(v1.y - v2.y));

        return result;
    }

    /**
     * Calculate the angle from two vectors
     * @param v1 First Vector2
     * @param v2 Second Vector2
     * @return angle from v1 and v2
     */
    public static float Vector2Angle(Vector2 v1, Vector2 v2){
        float result = (float) (Math.atan2(v2.y - v1.y, v2.x - v1.x) * (RAD2DEG));
        if (result < 0)
            result += 360.0f;
        return result;
    }

    public static Vector2 Vector2Scale(Vector2 v, float scale){
        return new Vector2(v.x * scale, v.y * scale);
    }

    public static Vector2 Vector2Multiply(Vector2 v1, Vector2 v2){
        return new Vector2(v1.x * v2.x, v2.x * v2.y);
    }

    public static Vector2 Vector2Negate(Vector2 v){
        return new Vector2(-v.x, -v.y);
    }

    public static Vector2 Vector2Divide(Vector2 v1, Vector2 v2){
        return new Vector2(v1.x / v2.x, v1.y / v2.y);
    }

    public static Vector2 Vector2Normalize(Vector2 v){
        Vector2 result = new Vector2();
        float length = (float) Math.sqrt((v.x*v.x) + (v.y*v.y));
        if (length > 0) {
            float ilength = 1.0f/length;
            result.x = v.x*ilength;
            result.y = v.y*ilength;
        }
        return result;
    }

    // Transforms a Vector2 by a given Matrix
    public static Vector2 Vector2Transform(Vector2 v, Matrix mat) {
        Vector2 result = new Vector2();

        float x = v.x;
        float y = v.y;
        float z = 0;

        result.x = mat.m0*x + mat.m4*y + mat.m8*z + mat.m12;
        result.y = mat.m1*x + mat.m5*y + mat.m9*z + mat.m13;

        return result;
    }

    public static Vector2 Vector2Lerp(Vector2 v1, Vector2 v2, float amount){
        Vector2 result = new Vector2();

        result.x = v1.x + amount * (v2.x - v1.x);
        result.y = v1.y + amount * (v2.y - v1.y);

        return result;
    }

    public static Vector2 Vector2Reflect(Vector2 v, Vector2 normal){
        Vector2 result = new Vector2();

        float dotProduct = Vector2DotProduct(v, normal);

        result.x = v.x - (2.0f * normal.x) * dotProduct;
        result.y = v.y - (2.0f * normal.y) * dotProduct;

        return result;
    }

    public static Vector2 Vector2Rotate(Vector2 v, float angle){
        Vector2 result = new Vector2();

        float cosres = (float) Math.cos(angle);
        float sinres = (float) Math.sin(angle);

        result.x = v.x*cosres - v.y*sinres;
        result.y = v.x*sinres + v.y*cosres;

        return result;
    }

    public static Vector2 Vector2MoveTowards(Vector2 v, Vector2 target, float maxDistance){
        Vector2 result = new Vector2();

        float dx = target.x - v.x;
        float dy = target.y - v.y;
        float value = (dx * dx) + (dy * dy);

        if (value == 0 || maxDistance >= 0 && value <= maxDistance * maxDistance) {
            result.x = target.x;
            result.y = target.y;
            return result;
        }

        float dist = (float) Math.sqrt(value);

        result.x = v.x + dx / dist * maxDistance;
        result.y = v.y + dy / dist * maxDistance;

        return result;
    }


    // Invert the given vector
    public static Vector2 Vector2Invert(Vector2 v) {
        Vector2 result = new Vector2(1.0f/v.x, 1.0f/v.y);
        return result;
    }

    // Clamp the components of the vector between
    // min and max values specified by the given vectors
    public static Vector2 Vector2Clamp(Vector2 v, Vector2 min, Vector2 max) {
        Vector2 result = new Vector2();

        result.x = Math.min(max.x, Math.max(min.x, v.x));
        result.y = Math.min(max.y, Math.max(min.y, v.y));

        return result;
    }

    // Clamp the magnitude of the vector between two min and max values
    public static Vector2 Vector2ClampValue(Vector2 v, float min, float max) {
        Vector2 result = v;
        float length = (v.x*v.x) + (v.y*v.y);
        if (length > 0.0f) {
            length = (float) Math.sqrt(length);
            if (length < min) {
                float scale = min/length;
                result.x = v.x*scale;
                result.y = v.y*scale;
            }
            else if (length > max) {
                float scale = max/length;
                result.x = v.x*scale;
                result.y = v.y*scale;
            }
        }
        return result;
    }

    // Check whether two given vectors are almost equal
    public static boolean Vector2Equals(Vector2 p, Vector2 q) {
        boolean result = ((Math.abs(p.x - q.x)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.x), Math.abs(q.x))))) &&
                ((Math.abs(p.y - q.y)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.y), Math.abs(q.y)))));
        return result;
    }

    /*
     * Vector3 maths functions
     */

    public static Vector3 Vector3Zero(){
        return new Vector3();
    }

    public static Vector3 Vector3One(){
        return new Vector3(1, 1, 1);
    }

    public static Vector3 Vector3Add(Vector3 v1, Vector3 v2){
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static Vector3 Vector3AddValue(Vector3 v, float add){
        return new Vector3(v.x + add, v.y + add, v.z + add);
    }

    public static Vector3 Vector3Subtract(Vector3 v1, Vector3 v2){
        return new Vector3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public static Vector3 Vector3SubtractValue(Vector3 v, float sub){
        return new Vector3(v.x - sub, v.y - sub, v.z - sub);
    }

    public static Vector3 Vector3Scale(Vector3 v, float scale){
        return new Vector3(v.x * scale, v.y * scale, v.z * scale);
    }

    public static Vector3 Vector3Multiply(Vector3 v1, Vector3 v2){
        return new Vector3(v1.x * v2.x, v1.y * v2.y, v1.z * v2.z);
    }

    public static Vector3 Vector3CrossProduct(Vector3 v1, Vector3 v2){
        Vector3 result = new Vector3();

        result.x = v1.y * v2.z - v1.z * v2.y;
        result.y = v1.z * v2.x - v1.x * v2.z;
        result.z = v1.x * v2.y - v1.y * v2.x;

        return result;
    }

    public static Vector3 Vector3Perpendicular(Vector3 v){
        Vector3 result;

        float min = Math.abs(v.x);
        Vector3 cardinalAxis = new Vector3(1.0f, 0.0f, 0.0f);

        if (Math.abs(v.y) < min){
            min = Math.abs(v.y);
            cardinalAxis = new Vector3(0.0f, 1.0f, 0.0f);
        }

        if (Math.abs(v.z) < min){
            cardinalAxis = new Vector3(0.0f, 0.0f, 1.0f);
        }

        result = Vector3CrossProduct(v, cardinalAxis);

        return result;
    }

    public static float Vector3Length(Vector3 v){
        return (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
    }

    public static float Vector3LengthSqr(Vector3 v){
        return v.x * v.x + v.y * v.y + v.z * v.z;
    }

    public static float Vector3DotProduct(Vector3 v1, Vector3 v2){
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static float Vector3Distance(Vector3 v1, Vector3 v2){
        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // Calculate square distance between two vectors
    public static float Vector3DistanceSqr(Vector3 v1, Vector3 v2) {
        float result = 0.0f;

        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;

        result = dx * dx + dy * dy + dz * dz;

        return result;
    }

    // Calculate angle between two vectors in XY and XZ
    public static float Vector3Angle(Vector3 v1, Vector3 v2){
        float result = 0.0f;

        Vector3 cross = new Vector3(v1.y*v2.z - v1.z*v2.y, v1.z*v2.x - v1.x*v2.z, v1.x*v2.y - v1.y*v2.x);

        float len = (float) Math.sqrt(cross.x*cross.x + cross.y*cross.y + cross.z*cross.z);
        float dot = (v1.x*v2.x + v1.y*v2.y + v1.z*v2.z);

        result = (float) Math.atan2(len, dot);

        return result;
    }

    public static Vector3 Vector3Negate(Vector3 v){
        return new Vector3(-v.x, -v.y, -v.z);
    }

    public static Vector3 Vector3Divide(Vector3 v1, Vector3 v2){
        return new Vector3(v1.x / v2.x, v1.y / v2.y, v1.z / v2.z);
    }

    public static Vector3 Vector3Normalize(Vector3 v){
        Vector3 result = new Vector3();
        result.x = v.x;
        result.y = v.y;
        result.z = v.z;

        float length, ilength;
        length = Vector3Length(result);
        if (length == 0.0f) length = 1.0f;
        ilength = 1.0f / length;

        result.x *= ilength;
        result.y *= ilength;
        result.z *= ilength;

        return result;
    }

    public static Vector3[] Vector3OrthoNormalize(Vector3 v1, Vector3 v2){
        v1 = Vector3Normalize(v1);
        Vector3 vn = Vector3CrossProduct(v1, v2);
        vn = Vector3Normalize(vn);
        v2 = Vector3CrossProduct(vn, v1);

        return new Vector3[]{v1, v2};
    }

    public static Vector3 Vector3Transform(Vector3 v, Matrix m){
        Vector3 result = new Vector3(v.x, v.y, v.z);

        result.x = (m.m0 * result.x) + (m.m4 * result.y) + (m.m8 * result.z) + m.m12;
        result.y = (m.m1 * result.x) + (m.m5 * result.y) + (m.m9 * result.z) + m.m13;
        result.z = (m.m2 * result.x) + (m.m6 * result.y) + (m.m10 * result.z) + m.m14;

        return result;
    }

    public static Vector3 Vector3RotateByQuaternion(Vector3 v, Quaternion q){
        Vector3 result = new Vector3();

        result.x = v.x*(q.x*q.x + q.w*q.w - q.y*q.y - q.z*q.z) + v.y*(2*q.x*q.y - 2*q.w*q.z) + v.z*(2*q.x*q.z + 2*q.w*q.y);
        result.y = v.x*(2*q.w*q.z + 2*q.x*q.y) + v.y*(q.w*q.w - q.x*q.x + q.y*q.y - q.z*q.z) + v.z*(-2*q.w*q.x + 2*q.y*q.z);
        result.z = v.x*(-2*q.w*q.y + 2*q.x*q.z) + v.y*(2*q.w*q.x + 2*q.y*q.z)+ v.z*(q.w*q.w - q.x*q.x - q.y*q.y + q.z*q.z);

        return result;
    }

    // Rotates a vector around an axis
    public static Vector3 Vector3RotateByAxisAngle(Vector3 v, Vector3 axis, float angle) {
        // Using Euler-Rodrigues Formula
        // Ref.: https://en.wikipedia.org/w/index.php?title=Euler%E2%80%93Rodrigues_formula
        Vector3 result = new Vector3(v.x, v.y, v.z);
        // Vector3Normalize(axis);
        float length = (float) Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);

        if (length == 0.0f) {
            length = 1.0f;
        }

        float ilength = 1.0f / length;
        axis.x *= ilength;
        axis.y *= ilength;
        axis.z *= ilength;

        angle /= 2.0f;
        float a = (float) Math.sin(angle);
        float b = axis.x * a;
        float c = axis.y * a;
        float d = axis.z * a;
        a = (float) Math.cos(angle);

        Vector3 w = new Vector3(b, c, d);

        // Vector3CrossProduct(w, v)
        Vector3 wv = new Vector3(w.y * v.z - w.z * v.y, w.z * v.x - w.x * v.z, w.x * v.y - w.y * v.x);

        // Vector3CrossProduct(w, wv)
        Vector3 wwv = new Vector3(w.y * wv.z - w.z * wv.y, w.z * wv.x - w.x * wv.z, w.x * wv.y - w.y * wv.x);

        // Vector3Scale(wv, 2 * a)
        a *= 2;
        wv.x *= a;
        wv.y *= a;
        wv.z *= a;

        // Vector3Scale(wwv, 2)
        wwv.x *= 2;
        wwv.y *= 2;
        wwv.z *= 2;

        result.x += wv.x;
        result.y += wv.y;
        result.z += wv.z;

        result.x += wwv.x;
        result.y += wwv.y;
        result.z += wwv.z;

        return result;
    }


    public static Vector3 Vector3Lerp(Vector3 v1, Vector3 v2, float amount){
        Vector3 result = new Vector3();

        result.x = v1.x + amount * (v2.x - v1.x);
        result.y = v1.y + amount * (v2.y - v1.y);
        result.z = v1.z + amount * (v2.z - v1.z);

        return result;
    }

    public static Vector3 Vector3Reflect(Vector3 v, Vector3 normal){
        Vector3 result = new Vector3();
        float dotProduct = Vector3DotProduct(v, normal);

        result.x = v.x - (2.0f * normal.x) * dotProduct;
        result.y = v.y - (2.0f * normal.y) * dotProduct;
        result.z = v.z - (2.0f * normal.z) * dotProduct;

        return result;
    }

    public static Vector3 Vector3Min(Vector3 v1, Vector3 v2){
        Vector3 result = new Vector3();

        result.x = Math.min(v1.x, v2.x);
        result.y = Math.min(v1.y, v2.y);
        result.z = Math.min(v1.z, v2.z);

        return result;
    }

    public static Vector3 Vector3Max(Vector3 v1, Vector3 v2){
        Vector3 result = new Vector3();

        result.x = Math.max(v1.x, v2.x);
        result.y = Math.max(v1.y, v2.y);
        result.z = Math.max(v1.z, v2.z);

        return result;
    }

    public static Vector3 Vector3Barycenter(Vector3 p, Vector3 a, Vector3 b, Vector3 c){
        //Vector v0 = b - a, v1 = c - a, v2 = p - a;

        Vector3 v0 = new Vector3(b.x - a.x, b.y - a.y, b.z - a.z);   // Vector3Subtract(b, a)
        Vector3 v1 = new Vector3(c.x - a.x, c.y - a.y, c.z - a.z);   // Vector3Subtract(c, a)
        Vector3 v2 = new Vector3(p.x - a.x, p.y - a.y, p.z - a.z);   // Vector3Subtract(p, a)
        float d00 = (v0.x*v0.x + v0.y*v0.y + v0.z*v0.z);    // Vector3DotProduct(v0, v0)
        float d01 = (v0.x*v1.x + v0.y*v1.y + v0.z*v1.z);    // Vector3DotProduct(v0, v1)
        float d11 = (v1.x*v1.x + v1.y*v1.y + v1.z*v1.z);    // Vector3DotProduct(v1, v1)
        float d20 = (v2.x*v0.x + v2.y*v0.y + v2.z*v0.z);    // Vector3DotProduct(v2, v0)
        float d21 = (v2.x*v1.x + v2.y*v1.y + v2.z*v1.z);    // Vector3DotProduct(v2, v1)

        float denom = d00 * d11 - d01 * d01;

        Vector3 result = new Vector3();

        result.y = (d11 * d20 - d01 * d21) / denom;
        result.z = (d00 * d21 - d01 * d20) / denom;
        result.x = 1.0f - (result.z + result.y);

        return result;
    }

    public static Vector3 Vector3Unproject(Vector3 source, Matrix projection, Matrix view){
        Vector3 result = new Vector3();

        // Calculate unproject matrix (multiply view patrix by projection matrix) and invert it
        Matrix matViewProj = MatrixMultiply(view, projection);
        matViewProj = MatrixInvert(matViewProj);

        // Create quaternion from source point
        Quaternion quat = new Quaternion(source.x, source.y, source.z, 1.0f );

        // Multiply quat point by unproject matrix
        quat = QuaternionTransform(quat, matViewProj);

        // Normalized world points in vectors
        result.x = quat.x/quat.w;
        result.y = quat.y/quat.w;
        result.z = quat.z/quat.w;

        return result;
    }

    public static Float3 Vector3ToFloatV(Vector3 v){
        return new Float3(v.x, v.y, v.z);
    }

    // Invert the given vector
    public static Vector3 Vector3Invert(Vector3 v) {
        Vector3 result = new Vector3(1.0f/v.x, 1.0f/v.y, 1.0f/v.z);

        return result;
    }
    // Clamp the components of the vector between
    // min and max values specified by the given vectors
    public static Vector3 Vector3Clamp(Vector3 v, Vector3 min, Vector3 max) {
        Vector3 result = new Vector3();

        result.x = Math.min(max.x, Math.max(min.x, v.x));
        result.y = Math.min(max.y, Math.max(min.y, v.y));
        result.z = Math.min(max.z, Math.max(min.z, v.z));

        return result;
    }

    // Clamp the magnitude of the vector between two values
    public static Vector3 Vector3ClampValue(Vector3 v, float min, float max) {
        Vector3 result = v;
        float length = (v.x*v.x) + (v.y*v.y) + (v.z*v.z);
        if (length > 0.0f)
        {
            length = (float) Math.sqrt(length);
            if (length < min)
            {
                float scale = min/length;
                result.x = v.x*scale;
                result.y = v.y*scale;
                result.z = v.z*scale;
            }
            else if (length > max)
            {
                float scale = max/length;
                result.x = v.x*scale;
                result.y = v.y*scale;
                result.z = v.z*scale;
            }
        }
        return result;
    }

    // Check whether two given vectors are almost equal
    public static boolean Vector3Equals(Vector3 p, Vector3 q) {
        boolean result = ((Math.abs(p.x - q.x)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.x), Math.abs(q.x))))) &&
                ((Math.abs(p.y - q.y)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.y), Math.abs(q.y))))) &&
                ((Math.abs(p.z - q.z)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.z), Math.abs(q.z)))));

        return result;
    }

    // Compute the direction of a refracted ray where v specifies the
    // normalized direction of the incoming ray, n specifies the
    // normalized normal vector of the interface of two optical media,
    // and r specifies the ratio of the refractive index of the medium
    // from where the ray comes to the refractive index of the medium
    // on the other side of the surface
    public static Vector3 Vector3Refract(Vector3 v, Vector3 n, float r) {
        Vector3 result = new Vector3();

        float dot = v.x*n.x + v.y*n.y + v.z*n.z;
        float d = 1.0f - r*r*(1.0f - dot*dot);

        if (d >= 0.0f) {
            d = (float) Math.sqrt(d);
            v.x = r*v.x - (r*dot + d)*n.x;
            v.y = r*v.y - (r*dot + d)*n.y;
            v.z = r*v.z - (r*dot + d)*n.z;
            result = v;
        }

        return result;
    }


    /*
     * ENTER THE MATRIX (math)
     */

    public static float MatrixDeterminant(Matrix mat){
        // Cache the matrix values (speed optimization)
        float a00 = mat.m0, a01 = mat.m1, a02 = mat.m2, a03 = mat.m3;
        float a10 = mat.m4, a11 = mat.m5, a12 = mat.m6, a13 = mat.m7;
        float a20 = mat.m8, a21 = mat.m9, a22 = mat.m10, a23 = mat.m11;
        float a30 = mat.m12, a31 = mat.m13, a32 = mat.m14, a33 = mat.m15;

        return a30 * a21 * a12 * a03 - a20 * a31 * a12 * a03 - a30 * a11 * a22 * a03 + a10 * a31 * a22 * a03 +
                a20 * a11 * a32 * a03 - a10 * a21 * a32 * a03 - a30 * a21 * a02 * a13 + a20 * a31 * a02 * a13 +
                a30 * a01 * a22 * a13 - a00 * a31 * a22 * a13 - a20 * a01 * a32 * a13 + a00 * a21 * a32 * a13 +
                a30 * a11 * a02 * a23 - a10 * a31 * a02 * a23 - a30 * a01 * a12 * a23 + a00 * a31 * a12 * a23 +
                a10 * a01 * a32 * a23 - a00 * a11 * a32 * a23 - a20 * a11 * a02 * a33 + a10 * a21 * a02 * a33 +
                a20 * a01 * a12 * a33 - a00 * a21 * a12 * a33 - a10 * a01 * a22 * a33 + a00 * a11 * a22 * a33;
    }

    public static float MatrixTrace(Matrix m){
        return m.m0 + m.m5 + m.m10 + m.m15;
    }

    public static Matrix MatrixTranspose(Matrix mat){
        Matrix result = new Matrix();

        result.m0 = mat.m0;
        result.m1 = mat.m4;
        result.m2 = mat.m8;
        result.m3 = mat.m12;
        result.m4 = mat.m1;
        result.m5 = mat.m5;
        result.m6 = mat.m9;
        result.m7 = mat.m13;
        result.m8 = mat.m2;
        result.m9 = mat.m6;
        result.m10 = mat.m10;
        result.m11 = mat.m14;
        result.m12 = mat.m3;
        result.m13 = mat.m7;
        result.m14 = mat.m11;
        result.m15 = mat.m15;

        return result;
    }

    public static Matrix MatrixInvert(Matrix mat){
        Matrix result = new Matrix();

        // Cache the matrix values (speed optimization)
        float a00 = mat.m0, a01 = mat.m1, a02 = mat.m2, a03 = mat.m3;
        float a10 = mat.m4, a11 = mat.m5, a12 = mat.m6, a13 = mat.m7;
        float a20 = mat.m8, a21 = mat.m9, a22 = mat.m10, a23 = mat.m11;
        float a30 = mat.m12, a31 = mat.m13, a32 = mat.m14, a33 = mat.m15;

        float b00 = a00 * a11 - a01 * a10;
        float b01 = a00 * a12 - a02 * a10;
        float b02 = a00 * a13 - a03 * a10;
        float b03 = a01 * a12 - a02 * a11;
        float b04 = a01 * a13 - a03 * a11;
        float b05 = a02 * a13 - a03 * a12;
        float b06 = a20 * a31 - a21 * a30;
        float b07 = a20 * a32 - a22 * a30;
        float b08 = a20 * a33 - a23 * a30;
        float b09 = a21 * a32 - a22 * a31;
        float b10 = a21 * a33 - a23 * a31;
        float b11 = a22 * a33 - a23 * a32;

        // Calculate the invert determinant (inlined to avoid double-caching)
        float invDet = 1.0f / (b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06);

        result.m0 = (a11 * b11 - a12 * b10 + a13 * b09) * invDet;
        result.m1 = (-a01 * b11 + a02 * b10 - a03 * b09) * invDet;
        result.m2 = (a31 * b05 - a32 * b04 + a33 * b03) * invDet;
        result.m3 = (-a21 * b05 + a22 * b04 - a23 * b03) * invDet;
        result.m4 = (-a10 * b11 + a12 * b08 - a13 * b07) * invDet;
        result.m5 = (a00 * b11 - a02 * b08 + a03 * b07) * invDet;
        result.m6 = (-a30 * b05 + a32 * b02 - a33 * b01) * invDet;
        result.m7 = (a20 * b05 - a22 * b02 + a23 * b01) * invDet;
        result.m8 = (a10 * b10 - a11 * b08 + a13 * b06) * invDet;
        result.m9 = (-a00 * b10 + a01 * b08 - a03 * b06) * invDet;
        result.m10 = (a30 * b04 - a31 * b02 + a33 * b00) * invDet;
        result.m11 = (-a20 * b04 + a21 * b02 - a23 * b00) * invDet;
        result.m12 = (-a10 * b09 + a11 * b07 - a12 * b06) * invDet;
        result.m13 = (a00 * b09 - a01 * b07 + a02 * b06) * invDet;
        result.m14 = (-a30 * b03 + a31 * b01 - a32 * b00) * invDet;
        result.m15 = (a20 * b03 - a21 * b01 + a22 * b00) * invDet;

        return result;
    }

    public static Matrix MatrixIdentity(){
        return new Matrix(
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
    }

    public static Matrix MatrixAdd(Matrix left, Matrix right){
        Matrix result = MatrixIdentity();

        result.m0 = left.m0 + right.m0;
        result.m1 = left.m1 + right.m1;
        result.m2 = left.m2 + right.m2;
        result.m3 = left.m3 + right.m3;
        result.m4 = left.m4 + right.m4;
        result.m5 = left.m5 + right.m5;
        result.m6 = left.m6 + right.m6;
        result.m7 = left.m7 + right.m7;
        result.m8 = left.m8 + right.m8;
        result.m9 = left.m9 + right.m9;
        result.m10 = left.m10 + right.m10;
        result.m11 = left.m11 + right.m11;
        result.m12 = left.m12 + right.m12;
        result.m13 = left.m13 + right.m13;
        result.m14 = left.m14 + right.m14;
        result.m15 = left.m15 + right.m15;

        return result;
    }

    public static Matrix MatrixSubtract(Matrix left, Matrix right){
        Matrix result = MatrixIdentity();

        result.m0 = left.m0 - right.m0;
        result.m1 = left.m1 - right.m1;
        result.m2 = left.m2 - right.m2;
        result.m3 = left.m3 - right.m3;
        result.m4 = left.m4 - right.m4;
        result.m5 = left.m5 - right.m5;
        result.m6 = left.m6 - right.m6;
        result.m7 = left.m7 - right.m7;
        result.m8 = left.m8 - right.m8;
        result.m9 = left.m9 - right.m9;
        result.m10 = left.m10 - right.m10;
        result.m11 = left.m11 - right.m11;
        result.m12 = left.m12 - right.m12;
        result.m13 = left.m13 - right.m13;
        result.m14 = left.m14 - right.m14;
        result.m15 = left.m15 - right.m15;

        return result;
    }

    public static Matrix MatrixMultiply(Matrix left, Matrix right){
        Matrix result = new Matrix();

        result.m0 = left.m0*right.m0 + left.m1*right.m4 + left.m2*right.m8 + left.m3*right.m12;
        result.m1 = left.m0*right.m1 + left.m1*right.m5 + left.m2*right.m9 + left.m3*right.m13;
        result.m2 = left.m0*right.m2 + left.m1*right.m6 + left.m2*right.m10 + left.m3*right.m14;
        result.m3 = left.m0*right.m3 + left.m1*right.m7 + left.m2*right.m11 + left.m3*right.m15;
        result.m4 = left.m4*right.m0 + left.m5*right.m4 + left.m6*right.m8 + left.m7*right.m12;
        result.m5 = left.m4*right.m1 + left.m5*right.m5 + left.m6*right.m9 + left.m7*right.m13;
        result.m6 = left.m4*right.m2 + left.m5*right.m6 + left.m6*right.m10 + left.m7*right.m14;
        result.m7 = left.m4*right.m3 + left.m5*right.m7 + left.m6*right.m11 + left.m7*right.m15;
        result.m8 = left.m8*right.m0 + left.m9*right.m4 + left.m10*right.m8 + left.m11*right.m12;
        result.m9 = left.m8*right.m1 + left.m9*right.m5 + left.m10*right.m9 + left.m11*right.m13;
        result.m10 = left.m8*right.m2 + left.m9*right.m6 + left.m10*right.m10 + left.m11*right.m14;
        result.m11 = left.m8*right.m3 + left.m9*right.m7 + left.m10*right.m11 + left.m11*right.m15;
        result.m12 = left.m12*right.m0 + left.m13*right.m4 + left.m14*right.m8 + left.m15*right.m12;
        result.m13 = left.m12*right.m1 + left.m13*right.m5 + left.m14*right.m9 + left.m15*right.m13;
        result.m14 = left.m12*right.m2 + left.m13*right.m6 + left.m14*right.m10 + left.m15*right.m14;
        result.m15 = left.m12*right.m3 + left.m13*right.m7 + left.m14*right.m11 + left.m15*right.m15;

        return result;
    }

    public static Matrix MatrixTranslate(float x, float y, float z){
        return new Matrix(
                1.0f, 0.0f, 0.0f, x,
                0.0f, 1.0f, 0.0f, y,
                0.0f, 0.0f, 1.0f, z,
                0.0f, 0.0f, 0.0f, 1.0f);
    }

    public static Matrix MatrixRotate(Vector3 axis, float angle){
        Matrix result = new Matrix();

        float x = axis.x, y = axis.y, z = axis.z;

        float lengthSquared = x*x + y*y + z*z;

        if ((lengthSquared != 1.0f) && (lengthSquared != 0.0f)) {
            float ilength = (float) (1.0f/Math.sqrt(lengthSquared));
            x *= ilength;
            y *= ilength;
            z *= ilength;
        }

        float sinres = (float) Math.sin(angle);
        float cosres = (float) Math.cos(angle);
        float t = 1.0f - cosres;

        result.m0 = x*x*t + cosres;
        result.m1 = y*x*t + z*sinres;
        result.m2 = z*x*t - y*sinres;
        result.m3 = 0.0f;

        result.m4 = x*y*t - z*sinres;
        result.m5 = y*y*t + cosres;
        result.m6 = z*y*t + x*sinres;
        result.m7 = 0.0f;

        result.m8 = x*z*t + y*sinres;
        result.m9 = y*z*t - x*sinres;
        result.m10 = z*z*t + cosres;
        result.m11 = 0.0f;

        result.m12 = 0.0f;
        result.m13 = 0.0f;
        result.m14 = 0.0f;
        result.m15 = 1.0f;


        return result;
    }

    // Get x-rotation matrix
    // NOTE: Angle must be provided in radians
    public static Matrix MatrixRotateX(float angle){
        Matrix result = MatrixIdentity();

        float cosres = (float) Math.cos(angle);
        float sinres = (float) Math.sin(angle);

        result.m5 = cosres;

        result.m6 = sinres;
        result.m9 = -sinres;

        result.m10 = cosres;

        return result;
    }

    // Get y-rotation matrix
    // NOTE: Angle must be provided in radians
    public static Matrix MatrixRotateY(float angle){
        Matrix result = MatrixIdentity();

        float cosres = (float) Math.cos(angle);
        float sinres = (float) Math.sin(angle);

        result.m0 = cosres;

        result.m2 = -sinres;
        result.m8 = sinres;

        result.m10 = cosres;

        return result;
    }

    // Get z-rotation matrix
    // NOTE: Angle must be provided in radians
    public static Matrix MatrixRotateZ(float angle){
        Matrix result = MatrixIdentity();

        float cosres = (float) Math.cos(angle);
        float sinres = (float) Math.sin(angle);

        result.m0 = cosres;

        result.m1 = sinres;
        result.m4 = -sinres;

        result.m5 = cosres;

        return result;
    }

    // Get xyz-rotation matrix
    // NOTE: Angle must be provided in radians
    public static Matrix MatrixRotateXYZ(Vector3 angle){
        Matrix result = MatrixIdentity();

        float cosz = (float) Math.cos(-angle.z);
        float sinz = (float) Math.sin(-angle.z);
        float cosy = (float) Math.cos(-angle.y);
        float siny = (float) Math.sin(-angle.y);
        float cosx = (float) Math.cos(-angle.x);
        float sinx = (float) Math.sin(-angle.x);

        result.m0 = cosz*cosy;
        result.m1 = (cosz*siny*sinx) - (sinz*cosx);
        result.m2 = (cosz*siny*cosx) + (sinz*sinx);

        result.m4 = sinz*cosy;
        result.m5 = (sinz*siny*sinx) + (cosz*cosx);
        result.m6 = (sinz*siny*cosx) - (cosz*sinx);

        result.m8 = -siny;
        result.m9 = cosy*sinx;
        result.m10= cosy * cosx;

        return result;
    }

    // Get zyx-rotation matrix
    // NOTE: Angle must be provided in radians
    public static Matrix MatrixRotateZYX(Vector3 angle){
        Matrix result = new Matrix();

        float cz = (float) Math.cos(angle.z);
        float sz = (float) Math.sin(angle.z);
        float cy = (float) Math.cos(angle.y);
        float sy = (float) Math.sin(angle.y);
        float cx = (float) Math.cos(angle.x);
        float sx = (float) Math.sin(angle.x);

        result.m0 = cz*cy;
        result.m4 = cz*sy*sx - cx*sz;
        result.m8 = sz*sx + cz*cx*sy;
        result.m12 = 0;

        result.m1 = cy*sz;
        result.m5 = cz*cx + sz*sy*sx;
        result.m9 = cx*sz*sy - cz*sx;
        result.m13 = 0;

        result.m2 = -sy;
        result.m6 = cy*sx;
        result.m10 = cy*cx;
        result.m14 = 0;

        result.m3 = 0;
        result.m7 = 0;
        result.m11 = 0;
        result.m15 = 1;

        return result;
    }

    public static Matrix MatrixScale(float x, float y, float z){
        return new Matrix(
                x, 0.0f, 0.0f, 0.0f,
                0.0f, y, 0.0f, 0.0f,
                0.0f, 0.0f, z, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
    }

    public static Matrix MatrixFrustum(double left, double right, double bottom, double top, double near, double far){
        Matrix result = new Matrix();

        float rl = (float)(right - left);
        float tb = (float)(top - bottom);
        float fn = (float)(far - near);

        result.m0 = ((float) near*2.0f)/rl;
        result.m1 = 0.0f;
        result.m2 = 0.0f;
        result.m3 = 0.0f;

        result.m4 = 0.0f;
        result.m5 = ((float) near*2.0f)/tb;
        result.m6 = 0.0f;
        result.m7 = 0.0f;

        result.m8 = ((float)right + (float)left)/rl;
        result.m9 = ((float)top + (float)bottom)/tb;
        result.m10 = -((float)far + (float)near)/fn;
        result.m11 = -1.0f;

        result.m12 = 0.0f;
        result.m13 = 0.0f;
        result.m14 = -((float)far*(float)near*2.0f)/fn;
        result.m15 = 0.0f;

        return result;
    }

    // Get perspective projection matrix
    // NOTE: Fovy angle must be provided in radian
    public static Matrix MatrixPerspective(double fovy, double aspect, double near, double far){
        Matrix result = new Matrix();

        double top = near*Math.tan(fovy*0.5);
        double bottom = -top;
        double right = top*aspect;
        double left = -right;

        // MatrixFrustum(-right, right, -top, top, near, far);
        float rl = (float)(right - left);
        float tb = (float)(top - bottom);
        float fn = (float)(far - near);

        result.m0 = ((float)near*2.0f)/rl;
        result.m5 = ((float)near*2.0f)/tb;
        result.m8 = ((float)right + (float)left)/rl;
        result.m9 = ((float)top + (float)bottom)/tb;
        result.m10 = -((float)far + (float)near)/fn;
        result.m11 = -1.0f;
        result.m14 = -((float)far*(float)near*2.0f)/fn;

        return result;
    }

    public static Matrix MatrixOrtho(double left, double right, double bottom, double top, double near, double far){
        Matrix result = new Matrix();

        float rl = (float)(right - left);
        float tb = (float)(top - bottom);
        float fn = (float)(far - near);

        result.m0 = 2.0f/rl;
        result.m1 = 0.0f;
        result.m2 = 0.0f;
        result.m3 = 0.0f;
        result.m4 = 0.0f;
        result.m5 = 2.0f/tb;
        result.m6 = 0.0f;
        result.m7 = 0.0f;
        result.m8 = 0.0f;
        result.m9 = 0.0f;
        result.m10 = -2.0f/fn;
        result.m11 = 0.0f;
        result.m12 = -((float)left + (float)right)/rl;
        result.m13 = -((float)top + (float)bottom)/tb;
        result.m14 = -((float)far + (float)near)/fn;
        result.m15 = 1.0f;

        return result;
    }

    public static Matrix MatrixLookAt(Vector3 eye, Vector3 target, Vector3 up){
        Matrix result = new Matrix();

        float length = 0.0f;
        float ilength = 0.0f;

        // Vector3Subtract(eye, target)
        Vector3 vz = new Vector3(eye.x - target.x, eye.y - target.y, eye.z - target.z);

        // Vector3Normalize(vz)
        Vector3 v = vz;
        length = (float) Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
        if (length == 0.0f) length = 1.0f;
        ilength = 1.0f/length;
        vz.x *= ilength;
        vz.y *= ilength;
        vz.z *= ilength;

        // Vector3CrossProduct(up, vz)
        Vector3 vx = new Vector3(up.y*vz.z - up.z*vz.y, up.z*vz.x - up.x*vz.z, up.x*vz.y - up.y*vz.x);

        // Vector3Normalize(x)
        v = vx;
        length = (float) Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
        if (length == 0.0f) length = 1.0f;
        ilength = 1.0f/length;
        vx.x *= ilength;
        vx.y *= ilength;
        vx.z *= ilength;

        // Vector3CrossProduct(vz, vx)
        Vector3 vy = new Vector3(vz.y*vx.z - vz.z*vx.y, vz.z*vx.x - vz.x*vx.z, vz.x*vx.y - vz.y*vx.x);

        result.m0 = vx.x;
        result.m1 = vy.x;
        result.m2 = vz.x;
        result.m3 = 0.0f;
        result.m4 = vx.y;
        result.m5 = vy.y;
        result.m6 = vz.y;
        result.m7 = 0.0f;
        result.m8 = vx.z;
        result.m9 = vy.z;
        result.m10 = vz.z;
        result.m11 = 0.0f;
        result.m12 = -(vx.x*eye.x + vx.y*eye.y + vx.z*eye.z);   // Vector3DotProduct(vx, eye)
        result.m13 = -(vy.x*eye.x + vy.y*eye.y + vy.z*eye.z);   // Vector3DotProduct(vy, eye)
        result.m14 = -(vz.x*eye.x + vz.y*eye.y + vz.z*eye.z);   // Vector3DotProduct(vz, eye)
        result.m15 = 1.0f;

        return result;
    }

    public static float[] MatrixToFloat(Matrix mat){
        return MatrixToFloatV(mat).v;
    }

    public static Float16 MatrixToFloatV(Matrix mat){
        Float16 buffer = new Float16();

        buffer.v[0] = mat.m0;
        buffer.v[1] = mat.m1;
        buffer.v[2] = mat.m2;
        buffer.v[3] = mat.m3;
        buffer.v[4] = mat.m4;
        buffer.v[5] = mat.m5;
        buffer.v[6] = mat.m6;
        buffer.v[7] = mat.m7;
        buffer.v[8] = mat.m8;
        buffer.v[9] = mat.m9;
        buffer.v[10] = mat.m10;
        buffer.v[11] = mat.m11;
        buffer.v[12] = mat.m12;
        buffer.v[13] = mat.m13;
        buffer.v[14] = mat.m14;
        buffer.v[15] = mat.m15;

        return buffer;
    }

    /*
     * Quaternion maths
     */

    public static Quaternion QuaternionAdd(Quaternion q1, Quaternion q2){
        Quaternion result = new Quaternion(q1.x + q2.x, q1.y + q2.y, q1.z + q2.z, q1.w + q2.w);

        return result;
    }

    public static Quaternion QuaternionAddValue(Quaternion q, float add){
        Quaternion result = new Quaternion(q.x + add, q.y + add, q.z + add, q.w + add);

        return result;
    }

    public static Quaternion QuaternionSubtract(Quaternion q1, Quaternion q2){
        Quaternion result = new Quaternion(q1.x - q2.x, q1.y - q2.y, q1.z - q2.z, q1.w - q2.w);

        return result;
    }

    public static Quaternion QuaternionSubtractValue(Quaternion q, float sub){
        Quaternion result = new Quaternion(q.x - sub, q.y - sub, q.z - sub, q.w - sub);

        return result;
    }

    public static Quaternion QuaternionIdentity(){
        Quaternion result = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

        return result;
    }

    public static float QuaternionLength(Quaternion q){
        float result = (float)Math.sqrt(q.x*q.x + q.y*q.y + q.z*q.z + q.w*q.w);

        return result;
    }

    public static Quaternion QuaternionNormalize(Quaternion q){
        Quaternion result = new Quaternion();

        float length, ilength;
        length = QuaternionLength(q);
        if (length == 0.0f) length = 1.0f;
        ilength = 1.0f/length;

        result.x = q.x*ilength;
        result.y = q.y*ilength;
        result.z = q.z*ilength;
        result.w = q.w*ilength;

        return result;
    }

    public static Quaternion QuaternionInvert(Quaternion q){
        Quaternion result = q;

        float lengthSq = q.x*q.x + q.y*q.y + q.z*q.z + q.w*q.w;

        if (lengthSq != 0.0f){
            float invLength = 1.0f/lengthSq;

            result.x *= -invLength;
            result.y *= -invLength;
            result.z *= -invLength;
            result.w *= invLength;
        }

        return result;
    }

    public static Quaternion QuaternionMultiply(Quaternion q1, Quaternion q2){
        Quaternion result = new Quaternion();

        float qax = q1.x, qay = q1.y, qaz = q1.z, qaw = q1.w;
        float qbx = q2.x, qby = q2.y, qbz = q2.z, qbw = q2.w;

        result.x = qax*qbw + qaw*qbx + qay*qbz - qaz*qby;
        result.y = qay*qbw + qaw*qby + qaz*qbx - qax*qbz;
        result.z = qaz*qbw + qaw*qbz + qax*qby - qay*qbx;
        result.w = qaw*qbw - qax*qbx - qay*qby - qaz*qbz;

        return result;
    }

    public static Quaternion QuaternionScale(Quaternion q, float mul){
        Quaternion result = new Quaternion();

        result.x = q.x*mul;
        result.y = q.y*mul;
        result.z = q.z*mul;
        result.w = q.w*mul;

        return result;
    }

    public static Quaternion QuaternionDivide(Quaternion q1, Quaternion q2){
        return new Quaternion(q1.x / q2.x, q1.y / q2.y, q1.z / q2.z, q1.w / q2.w);
    }

    public static Quaternion QuaternionLerp(Quaternion q1, Quaternion q2, float amount){
        Quaternion result = new Quaternion();

        result.x = q1.x + amount*(q2.x - q1.x);
        result.y = q1.y + amount*(q2.y - q1.y);
        result.z = q1.z + amount*(q2.z - q1.z);
        result.w = q1.w + amount*(q2.w - q1.w);

        return result;
    }

    public static Quaternion QuaternionNlerp(Quaternion q1, Quaternion q2, float amount){
        Quaternion result = QuaternionLerp(q1, q2, amount);
        result = QuaternionNormalize(result);

        return result;
    }

    public static Quaternion QuaternionSlerp(Quaternion q1, Quaternion q2, float amount){
        Quaternion result = new Quaternion();

        float cosHalfTheta = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;

        if (Math.abs(cosHalfTheta) >= 1.0f){
            result = q1;
        }
        else if (cosHalfTheta > 0.95f){
            result = QuaternionNlerp(q1, q2, amount);
        }
        else{
            float halfTheta = (float) Math.acos(cosHalfTheta);
            float sinHalfTheta = (float) Math.sqrt(1.0f - cosHalfTheta*cosHalfTheta);

            if (Math.abs(sinHalfTheta) < 0.001f){
                result.x = (q1.x*0.5f + q2.x*0.5f);
                result.y = (q1.y*0.5f + q2.y*0.5f);
                result.z = (q1.z*0.5f + q2.z*0.5f);
                result.w = (q1.w*0.5f + q2.w*0.5f);
            }
            else{
                float ratioA = (float) (Math.sin((1 - amount)*halfTheta)/sinHalfTheta);
                float ratioB = (float) (Math.sin(amount*halfTheta)/sinHalfTheta);

                result.x = (q1.x*ratioA + q2.x*ratioB);
                result.y = (q1.y*ratioA + q2.y*ratioB);
                result.z = (q1.z*ratioA + q2.z*ratioB);
                result.w = (q1.w*ratioA + q2.w*ratioB);
            }
        }
        return result;
    }

    public static Quaternion QuaternionFromVector3ToVector3(Vector3 from, Vector3 to){
        Quaternion result = new Quaternion();

        float cos2Theta = Vector3DotProduct(from, to);
        Vector3 cross = Vector3CrossProduct(from, to);

        result.x = cross.x;
        result.y = cross.y;
        result.z = cross.z;
        result.w = 1.0f + cos2Theta;     // NOTE: Added QuaternioIdentity()

        // Normalize to essentially nlerp the original and identity to 0.5
        result = QuaternionNormalize(result);

        // Above lines are equivalent to:
        //Quaternion result = QuaternionNlerp(q, QuaternionIdentity(), 0.5f);

        return result;
    }

    public static Quaternion QuaternionFromMatrix(Matrix mat){
        Quaternion result = new Quaternion();

        float fourWSquaredMinus1 = mat.m0 + mat.m5 + mat.m10;
        float fourXSquaredMinus1 = mat.m0 - mat.m5 - mat.m10;
        float fourYSquaredMinus1 = mat.m5 - mat.m0 - mat.m10;
        float fourZSquaredMinus1 = mat.m10 - mat.m0 - mat.m5;

        int biggestIndex = 0;
        float fourBiggestSquaredMinus1 = fourWSquaredMinus1;

        if (fourXSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourXSquaredMinus1;
            biggestIndex = 1;
        }

        if (fourYSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourYSquaredMinus1;
            biggestIndex = 2;
        }

        if (fourZSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourZSquaredMinus1;
            biggestIndex = 3;
        }

        float biggestVal = (float) (Math.sqrt(fourBiggestSquaredMinus1 + 1.0f) * 0.5f);
        float mult = 0.25f / biggestVal;

        switch (biggestIndex) {
            case 0:
                result.w = biggestVal;
                result.x = (mat.m6 - mat.m9) * mult;
                result.y = (mat.m8 - mat.m2) * mult;
                result.z = (mat.m1 - mat.m4) * mult;
                break;
            case 1:
                result.x = biggestVal;
                result.w = (mat.m6 - mat.m9) * mult;
                result.y = (mat.m1 + mat.m4) * mult;
                result.z = (mat.m8 + mat.m2) * mult;
                break;
            case 2:
                result.y = biggestVal;
                result.w = (mat.m8 - mat.m2) * mult;
                result.x = (mat.m1 + mat.m4) * mult;
                result.z = (mat.m6 + mat.m9) * mult;
                break;
            case 3:
                result.z = biggestVal;
                result.w = (mat.m1 - mat.m4) * mult;
                result.x = (mat.m8 + mat.m2) * mult;
                result.y = (mat.m6 + mat.m9) * mult;
                break;
        }

        return result;
    }

    // Get a matrix for a given quaternion
    public static Matrix QuaternionToMatrix(Quaternion q){
        Matrix result = MatrixIdentity();

        float a2 = q.x*q.x;
        float b2 = q.y*q.y;
        float c2 = q.z*q.z;
        float ac = q.x*q.z;
        float ab = q.x*q.y;
        float bc = q.y*q.z;
        float ad = q.w*q.x;
        float bd = q.w*q.y;
        float cd = q.w*q.z;

        result.m0 = 1 - 2*(b2 + c2);
        result.m1 = 2*(ab + cd);
        result.m2 = 2*(ac - bd);

        result.m4 = 2*(ab - cd);
        result.m5 = 1 - 2*(a2 + c2);
        result.m6 = 2*(bc + ad);

        result.m8 = 2*(ac + bd);
        result.m9 = 2*(bc - ad);
        result.m10 = 1 - 2*(a2 + b2);

        return result;
    }

    // Get rotation quaternion for an angle and axis
    // NOTE: Angle must be provided in radians
    public static Quaternion QuaternionFromAxisAngle(Vector3 axis, float angle){
        Quaternion result = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

        float axisLength = (float) Math.sqrt(axis.x*axis.x + axis.y*axis.y + axis.z*axis.z);

        if (axisLength != 0.0f)
        {
            angle *= 0.5f;

            float length;
            float ilength;

            // Vector3Normalize(axis)
            length = (float) Math.sqrt(axis.x* axis.x + axis.y* axis.y + axis.z* axis.z);
            if (length == 0.0f) length = 1.0f;
            ilength = 1.0f/length;
            axis.x *= ilength;
            axis.y *= ilength;
            axis.z *= ilength;

            float sinres = (float) Math.sin(angle);
            float cosres = (float) Math.cos(angle);

            result.x = axis.x*sinres;
            result.y = axis.y*sinres;
            result.z = axis.z*sinres;
            result.w = cosres;

            // QuaternionNormalize(q);
            length = (float) Math.sqrt(result.x* result.x + result.y* result.y + result.z* result.z + result.w* result.w);
            if (length == 0.0f) length = 1.0f;
            ilength = 1.0f/length;
            result.x = result.x*ilength;
            result.y = result.y*ilength;
            result.z = result.z*ilength;
            result.w = result.w*ilength;
        }

        return result;
    }

    // Returns the rotation angle and axis for a given quaternion
    public static void QuaternionToAxisAngle(Quaternion q, Vector3 outAxis, float outAngle){
        if (Math.abs(q.w) > 1.0f) q = QuaternionNormalize(q);

        Vector3 resAxis = new Vector3(0.0f, 0.0f, 0.0f);
        float resAngle = (float) (2.0f*Math.acos(q.w));
        float den = (float) Math.sqrt(1.0f - q.w*q.w);

        if (den > 0.0001f){
            resAxis.x = q.x/den;
            resAxis.y = q.y/den;
            resAxis.z = q.z/den;
        }
        else{
            // This occurs when the angle is zero.
            // Not a problem: just set an arbitrary normalized axis.
            resAxis.x = 1.0f;
        }

        outAxis = resAxis;
        outAngle = resAngle;
    }

    // Returns he quaternion equivalent to Euler angles
    public static Quaternion QuaternionFromEuler(float roll, float pitch, float yaw){
        Quaternion q = new Quaternion();

        float x0 = (float) Math.cos(pitch*0.5f);
        float x1 = (float) Math.sin(pitch*0.5f);
        float y0 = (float) Math.cos(yaw*0.5f);
        float y1 = (float) Math.sin(yaw*0.5f);
        float z0 = (float) Math.cos(roll*0.5f);
        float z1 = (float) Math.sin(roll*0.5f);

        q.x = x1*y0*z0 - x0*y1*z1;
        q.y = x0*y1*z0 + x1*y0*z1;
        q.z = x0*y0*z1 - x1*y1*z0;
        q.w = x0*y0*z0 + x1*y1*z1;

        return q;
    }

    public static Vector3 QuaternionToEuler(Quaternion q){
        Vector3 result = new Vector3();

        // roll (x-axis rotation)
        float x0 = 2.0f*(q.w*q.x + q.y*q.z);
        float x1 = 1.0f - 2.0f*(q.x*q.x + q.y*q.y);
        result.x = (float) (Math.atan2(x0, x1)*RAD2DEG);

        // pitch (y-axis rotation)
        float y0 = 2.0f*(q.w*q.y - q.z*q.x);
        y0 = Math.min(y0, 1.0f);
        y0 = Math.max(y0, -1.0f);
        result.y = (float) (Math.asin(y0)*RAD2DEG);

        // yaw (z-axis rotation)
        float z0 = 2.0f*(q.w*q.z + q.x*q.y);
        float z1 = 1.0f - 2.0f*(q.y*q.y + q.z*q.z);
        result.z = (float) (Math.atan2(z0, z1)*RAD2DEG);

        return result;
    }

    public static Quaternion QuaternionTransform(Quaternion q, Matrix mat){
        Quaternion result = new Quaternion();

        result.x = mat.m0*q.x + mat.m4*q.y + mat.m8*q.z + mat.m12*q.w;
        result.y = mat.m1*q.x + mat.m5*q.y + mat.m9*q.z + mat.m13*q.w;
        result.z = mat.m2*q.x + mat.m6*q.y + mat.m10*q.z + mat.m14*q.w;
        result.w = mat.m3*q.x + mat.m7*q.y + mat.m11*q.z + mat.m15*q.w;

        return result;
    }

    // Check whether two given quaternions are almost equal
    public static boolean QuaternionEquals(Quaternion p, Quaternion q) {
        boolean result = (((Math.abs(p.x - q.x)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.x), Math.abs(q.x))))) &&
                ((Math.abs(p.y - q.y)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.y), Math.abs(q.y))))) &&
                ((Math.abs(p.z - q.z)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.z), Math.abs(q.z))))) &&
                ((Math.abs(p.w - q.w)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.w), Math.abs(q.w)))))) ||
                (((Math.abs(p.x + q.x)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.x), Math.abs(q.x))))) &&
                        ((Math.abs(p.y + q.y)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.y), Math.abs(q.y))))) &&
                        ((Math.abs(p.z + q.z)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.z), Math.abs(q.z))))) &&
                        ((Math.abs(p.w + q.w)) <= (EPSILON*Math.max(1.0f, Math.max(Math.abs(p.w), Math.abs(q.w))))));
        return result;
    }

    //Custom implementation for when 1/0 boolean value is required

    public static int getIntFromBoolean(boolean b){
        return b ? 1 : 0;
    }

    public static boolean getBooleanFromInt(int i) {
        return i == 1;
    }
}
