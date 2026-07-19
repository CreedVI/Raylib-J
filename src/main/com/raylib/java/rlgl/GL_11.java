package com.raylib.java.rlgl;

import com.raylib.java.Raylib;
import com.raylib.java.structs.Ray;

import static com.raylib.java.rlgl.RLGL.*;
import static org.lwjgl.opengl.GL11.*;

public class GL_11{

    final private Raylib context;

    public GL_11(Raylib context) {
        this.context = context;
    }

    void rlMatrixMode(int mode) {
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

    void rlBegin(int mode) {
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
    void rlEnd() { glEnd(); }
    void rlVertex2i(int x, int y) { glVertex2i(x, y); }
    void rlVertex2f(float x, float y) { glVertex2f(x, y); }
    void rlVertex3f(float x, float y, float z) { glVertex3f(x, y, z); }
    void rlTexCoord2f(float x, float y) { glTexCoord2f(x, y); }
    void rlNormal3f(float x, float y, float z) { glNormal3f(x, y, z); }
    void rlColor4ub(int r, int g, int b, int a) { glColor4ub((byte)r, (byte)g, (byte)b, (byte)a); }
    void rlColor3f(float x, float y, float z) { glColor3f(x, y, z); }
    void rlColor4f(float x, float y, float z, float w) { glColor4f(x, y, z, w); }

    void rlFrustum(double left, double right, double bottom, double top, double znear, double zfar) {
        glFrustum(left, right, bottom, top, znear, zfar);
    }

    void rlOrtho(double left, double right, double bottom, double top, double znear, double zfar) {
        glOrtho(left, right, bottom, top, znear, zfar);
    }

    void rlPushMatrix() {
        glPushMatrix();
    }

    void rlPopMatrix() {
        glPopMatrix();
    }

    void rlLoadIdentity() {
        glLoadIdentity();
    }

    void rlTranslatef(float x, float y, float z) {
        glTranslatef(x, y, z);
    }

    void rlRotatef(float angle, float x, float y, float z) {
        glRotatef(angle, x, y, z);
    }

    void rlScalef(float x, float y, float z) {
        glScalef(x, y, z);
    }

    void rlMultMatrixf(float[] matf) {
        glMultMatrixf(matf);
    }
}
