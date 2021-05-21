package com.creedvi.raylib.java.rlj.rlgl.vr;

public class VRDeviceInfo{

    // Head-Mounted-Display device parameters

    int hResolution;                // HMD horizontal resolution in pixels
    int vResolution;                // HMD vertical resolution in pixels
    float hScreenSize;              // HMD horizontal size in meters
    float vScreenSize;              // HMD vertical size in meters
    float vScreenCenter;            // HMD screen center in meters
    float eyeToScreenDistance;      // HMD distance between eye and display in meters
    float lensSeparationDistance;   // HMD lens separation distance in meters
    float interpupillaryDistance;   // HMD IPD (distance between pupils) in meters
    float lensDistortionValues[];  // HMD lens distortion constant parameters
    float chromaAbCorrection[];    // HMD chromatic aberration correction parameters

}
