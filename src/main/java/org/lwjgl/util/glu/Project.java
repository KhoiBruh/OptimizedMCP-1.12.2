package org.lwjgl.util.glu;

public final class Project {

    private Project() {}

    public static void gluPerspective(float fovY, float aspect, float zNear, float zFar) {
        GLU.gluPerspective(fovY, aspect, zNear, zFar);
    }

    public static void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        GLU.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

}
