package com.creedvi.raylib.java.rlj.core.camera;

public class Camera{

    enum CameraMode {
        CAMERA_CUSTOM(0),
        CAMERA_FREE(1),
        CAMERA_ORBITAL(2),
        CAMERA_FIRST_PERSON(3),
        CAMERA_THIRD_PERSON(4)
        ;

        final int camMode;
        CameraMode(int i){
            camMode = i;
        }

        public int getCamMode(){
            return camMode;
        }
    }

    // Camera projection modes
    public enum CameraType {
        CAMERA_PERSPECTIVE(0),
        CAMERA_ORTHOGRAPHIC(1)
        ;

        final int camType;
        CameraType(int i){
            camType = i;
        }

        public int getCamType(){
            return camType;
        }
    }

}
