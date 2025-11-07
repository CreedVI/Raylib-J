package com.raylib.java.rlgl;

import static com.raylib.java.rlgl.RLGL.*;
import static org.lwjgl.opengl.GL11.*;

public class GL_11{

    static void rlMatrixMode(int mode) {
        switch (mode) {
            case RL_PROJECTION:
                glMatrixMode(GL_PROJECTION);
                break;
            case RL_MODELVIEW:
                glMatrixMode(GL_MODELVIEW);
                break;
            case RL_TEXTURE:
                glMatrixMode(GL_TEXTURE);
                break;
            default:
                break;
        }
    }

    static void rlBegin(int mode) {
        switch (mode) {
            case RL_LINES:
                glBegin(GL_LINES);
                break;
            case RL_TRIANGLES:
                glBegin(GL_TRIANGLES);
                break;
            case RL_QUADS:
                glBegin(GL_QUADS);
                break;
            default:
                break;
        }
    }
    static void rlEnd() { glEnd(); }
    static void rlVertex2i(int x, int y) { glVertex2i(x, y); }
    static void rlVertex2f(float x, float y) { glVertex2f(x, y); }
    static void rlVertex3f(float x, float y, float z) { glVertex3f(x, y, z); }
    static void rlTexCoord2f(float x, float y) { glTexCoord2f(x, y); }
    static void rlNormal3f(float x, float y, float z) { glNormal3f(x, y, z); }
    static void rlColor4ub(int r, int g, int b, int a) { glColor4ub((byte)r, (byte)g, (byte)b, (byte)a); }
    static void rlColor3f(float x, float y, float z) { glColor3f(x, y, z); }
    static void rlColor4f(float x, float y, float z, float w) { glColor4f(x, y, z, w); }

    static void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar) {
        glFrustum(left, right, bottom, top, znear, zfar);
    }

    static void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar) {
        glOrtho(left, right, bottom, top, znear, zfar);
    }

    static void rlPushMatrix() {
        glPushMatrix();
    }

    static void rlPopMatrix() {
        glPopMatrix();
    }

    static void rlLoadIdentity() {
        glLoadIdentity();
    }

    static void rlTranslatef(float x, float y, float z) {
        glTranslatef(x, y, z);
    }

    static void rlRotatef(float angle, float x, float y, float z) {
        glRotatef(angle, x, y, z);
    }

    static void rlScalef(float x, float y, float z) {
        glScalef(x, y, z);
    }

    static void rlMultMatrixf(float[] matf) {
        glMultMatrixf(matf);
    }
}
