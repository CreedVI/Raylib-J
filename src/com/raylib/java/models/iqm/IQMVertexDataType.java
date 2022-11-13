package com.raylib.java.models.iqm;

public enum IQMVertexDataType {
    IQM_POSITION,
    IQM_TEXCOORD,
    IQM_NORMAL,
    IQM_TANGENT,           // NOTE: Tangents unused by default
    IQM_BLENDINDEXES,
    IQM_BLENDWEIGHTS,
    IQM_COLOR,
    // Values 7, 8, 9 reserved for future use IAW http://sauerbraten.org/iqm/iqm.txt
    IQM_7,
    IQM_8,
    IQM_9,
    IQM_CUSTOM            // NOTE: Custom vertex values unused by default
}
