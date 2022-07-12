package com.raylib.java.utils;

import java.util.Arrays;
import java.util.Objects;

public class OBJLoader {

    /**
     * OB-J Loader v0.1
     * written by CreedVI for use in Raylib-J.
     */
    public static class OBJInfo {
        public int totalVertices;
        public int totalNormals;
        public int totalTexcoords;
        public int totalFaces;
        public int totalGroups;
        public int totalMaterials;

        public OBJVertexIndex[] faces;
        public String[] materialNames;
        public float[] vertices;
        public float[] normals;
        public float[] texcoords;
        public int[] numVPerLine;
        public int[] materialIds;

    }

    public static class OBJVertexIndex {
        public int vIndex, vtIndex, vnIndex;
    }

    public static class OBJShape {
        String name;
        int faceOffset;
        int length;
    }

    public static class MTLInfo {
        public Material[] materials;
        public int numberOfMaterials;

        public MTLInfo() {
            materials = new Material[1];
            for (int i = 0; i < materials.length; i++) {
                materials[i] = new Material();
            }
        }
    }

    public static class Material {
        public String name;

        public float illum;
        public float dissolve;
        public float shininess;
        public float ior;

        public float[] ambient;
        public float[] diffuse;
        public float[] specular;
        public float[] transmittance;
        public float[] emission;


        public String ambient_texname;            /* map_Ka */
        public String diffuse_texname;            /* map_Kd */
        public String specular_texname;           /* map_Ks */
        public String specular_highlight_texname; /* map_Ns */
        public String bump_texname;               /* map_bump, bump */
        public String displacement_texname;       /* disp */
        public String alpha_texname;              /* map_d */

        public Material() {
            int i;
            name = null;
            ambient_texname = null;
            diffuse_texname = null;
            specular_texname = null;
            specular_highlight_texname = null;
            bump_texname = null;
            displacement_texname = null;
            alpha_texname = null;

            ambient = new float[3];
            diffuse =  new float[3];
            specular = new float[3];
            transmittance = new float[3];
            emission = new float[3];

            for (i = 0; i < 3; i++) {
                ambient[i] = 0.f;
                diffuse[i] = 0.f;
                specular[i] = 0.f;
                transmittance[i] = 0.f;
                emission[i] = 0.f;
            }
            illum = 0;
            dissolve = 1.f;
            shininess = 1.f;
            ior = 1.f;
        }
    }

    public static class Command {
        float vx, vy, vz;
        float nx, ny, nz;
        float tx, ty;

        OBJVertexIndex[] f;
        int numF;

        int[] fNumVerts;
        int numFNumVerts;
        
        String groupName;
        String objectName;
        String materialName;

        CommandType type;

        public Command() {
            f = new OBJVertexIndex[16];
            for (int i = 0; i <16; i++) {
                f[i] = new OBJVertexIndex();
            }
            fNumVerts = new int[16];
        }
    }

    private enum CommandType {
        COMMAND_EMPTY,
        COMMAND_V,
        COMMAND_VN,
        COMMAND_VT,
        COMMAND_F,
        COMMAND_G,
        COMMAND_O,
        COMMAND_USEMTL,
        COMMAND_MTLLIB
    }
    public OBJInfo objInfo;
    public MTLInfo mtlInfo;
    private Command[] cmds;

    public OBJShape[] shapes;

    public static final int FLAG_TRIANGULATE = (1<<0);

    public boolean ReadOBJ(String fileText, boolean triangulate) {
        objInfo = new OBJInfo();
        mtlInfo = new MTLInfo();
        cmds = parseLines(fileText, triangulate);

        int numV = 0, numVN = 0, numVT = 0, numF = 0, numFaces = 0;
        int mtlbLineIndex = -1;

        for (int i = 0; i < cmds.length; i++) {
            switch (cmds[i].type) {
                case COMMAND_V:
                    numV++;
                    break;
                case COMMAND_VN:
                    numVN++;
                    break;
                case COMMAND_VT:
                    numVT++;
                    break;
                case COMMAND_F:
                    numF += cmds[i].numF;
                    numFaces += cmds[i].numFNumVerts;
                    break;
                case COMMAND_MTLLIB:
                    mtlbLineIndex = i;
            }
        }

        // todo: load materialFile

        objInfo.totalVertices = numV;
        objInfo.totalNormals = numVN;
        objInfo.totalTexcoords = numVT;
        objInfo.totalFaces = numFaces;

        objInfo.numVPerLine = new int[numF];
        objInfo.materialIds = new int[numF];
        objInfo.vertices = new float[numV * 3];
        objInfo.normals = new float[numVN * 3];
        objInfo.texcoords = new float[numVT * 2];

        objInfo.faces = new OBJVertexIndex[numF];
        Arrays.setAll(objInfo.faces, i -> new OBJVertexIndex());

        int vCount = 0;
        int vtCount = 0;
        int vnCount = 0;
        int fCount = 0;
        int faceCount = 0;
        int materialID = -1;
        int i;

        for (i = 0; i < cmds.length; i++) {
            switch (cmds[i].type) {
                case COMMAND_V:
                    objInfo.vertices[3*vCount + 0] = cmds[i].vx;
                    objInfo.vertices[3*vCount + 1] = cmds[i].vy;
                    objInfo.vertices[3*vCount + 2] = cmds[i].vz;
                    vCount++;
                    break;
                case COMMAND_VN:
                    objInfo.normals[3*vnCount + 0] = cmds[i].nx;
                    objInfo.normals[3*vnCount + 1] = cmds[i].ny;
                    objInfo.normals[3*vnCount + 2] = cmds[i].nz;
                    vnCount++;
                    break;
                case COMMAND_VT:
                    objInfo.texcoords[2*vtCount + 0] = cmds[i].tx;
                    objInfo.texcoords[2*vtCount + 1] = cmds[i].ty;
                    vtCount++;
                    break;
                case COMMAND_F:
                    int k;
                    for (k = 0; k < cmds[i].numF; k++) {
                        OBJVertexIndex vi = cmds[i].f[k];
                        objInfo.faces[fCount + k].vIndex = vi.vIndex;
                        objInfo.faces[fCount + k].vnIndex = vi.vnIndex;
                        objInfo.faces[fCount + k].vtIndex = vi.vtIndex;
                    }

                    for (k = 0; k < cmds[i].numFNumVerts; k++) {
                        objInfo.materialIds[faceCount + k] = materialID;
                        objInfo.numVPerLine[faceCount + k] = cmds[i].fNumVerts[k];
                    }
                    
                    fCount += cmds[i].numF;
                    faceCount += cmds[i].fNumVerts[k];
                    break;
                default:
                    break;
            }
        }

        // TODO: 6/30/22 construct shape info
        faceCount = 0;
        i = 0;
        int n = 0;
        int shapeIndex = 0;

        String shapeName = "";
        String prevShapeName = "";
        int prevShapeFaceOffset = 0;
        int prevFaceOffset = 0;
        OBJShape prevShape = new OBJShape();

        for (i = 0; i < cmds.length; i++) {
            if (cmds[i].type == CommandType.COMMAND_O || cmds[i].type == CommandType.COMMAND_G) {
                n++;
            }
        }

        shapes = new OBJShape[n+1];
        for (int j = 0; j <shapes.length; j++) {
            shapes[j] = new OBJShape();
        }

        for (i = 0; i < cmds.length; i++) {
            if (cmds[i].type == CommandType.COMMAND_O || cmds[i].type == CommandType.COMMAND_G) {
                if (cmds[i].type == CommandType.COMMAND_O) {
                    shapeName = cmds[i].objectName;
                } else {
                    shapeName = cmds[i].groupName;
                }

                if (faceCount == 0) {
                    prevShapeName = shapeName;
                    prevShapeFaceOffset = faceCount;
                    prevFaceOffset = faceCount;
                } else {
                    if (shapeIndex == 0) {
                        shapes[shapeIndex].name = prevShapeName;
                        shapes[shapeIndex].faceOffset = prevShape.faceOffset;
                        shapes[shapeIndex].length = faceCount - prevFaceOffset;

                        shapeIndex++;
                        prevFaceOffset = faceCount;
                    } else {
                        if ((faceCount - prevFaceOffset) > 0) {
                            shapes[shapeIndex].name = prevShapeName;
                            shapes[shapeIndex].faceOffset = prevFaceOffset;
                            shapes[shapeIndex].length = faceCount - prevFaceOffset;
                            shapeIndex++;
                            prevFaceOffset = faceCount;
                        }
                    }

                    //record shape info
                    prevShapeName = shapeName;
                    prevShapeFaceOffset = faceCount;
                }
            }
            if (cmds[i].type == CommandType.COMMAND_F) {
                faceCount++;
            }
        }

        if ((faceCount - prevFaceOffset) > 0) {
            int length = faceCount - prevShapeFaceOffset;
            if (length > 0) {
                shapes[shapeIndex].name = prevShapeName;
                shapes[shapeIndex].faceOffset = prevFaceOffset;
                shapes[shapeIndex].length = faceCount - prevFaceOffset;
                shapeIndex++;
            }
        }
        else {
            /* Guess no 'v' line occurrence after 'o' or 'g', so discards current
             * shape information. */
        }

        cmds = null;
        return true;
    }

    public void ReadMTL(String filetext) {
        //todo
    }

    private Command[] parseLines(String fileText, boolean triangulate) {
        int numV = 0, numVT = 0, numVN = 0, numF = 0, numG = 0, numM = 0, num_faces = 0, numO = 0;
        int line = 0;

        String[] lines = fileText.split("(\\r\\n|\\r|\\n)");
        Command[] cmds = new Command[lines.length];
        for (int i = 0; i < cmds.length; i++) {
            cmds[i] = new Command();
        }

        for (String l :
                lines) {
            //System.out.println("Line "+line+": " + l);
            if(l.startsWith("v ")){
                cmds[line].type = CommandType.COMMAND_V;
                String[] tmp = l.substring(2).split(" ");
                String[] verts = new String[3];
                for (int i = 0, j = 0; i < tmp.length; i++) {
                    if (!Objects.equals(tmp[i], "")) {
                        verts[j] = tmp[i];
                        j++;
                    }
                }
                cmds[line].vx = Float.parseFloat(verts[0]);
                cmds[line].vy = Float.parseFloat(verts[1]);
                cmds[line].vz = Float.parseFloat(verts[2]);
                numV++;
            }
            else if(l.startsWith("vn ")){
                cmds[line].type = CommandType.COMMAND_VN;
                String[] tmp = l.substring(3).split(" ");
                String[] norms = new String[3];
                for (int i = 0, j = 0; i < tmp.length; i++) {
                    if (!Objects.equals(tmp[i], "")) {
                        norms[j] = tmp[i];
                        j++;
                    }
                }
                cmds[line].nx = Float.parseFloat(norms[0]);
                cmds[line].ny = Float.parseFloat(norms[1]);
                cmds[line].nz = Float.parseFloat(norms[2]);
                numVN++;
            }
            else if(l.startsWith("vt ")){
                cmds[line].type = CommandType.COMMAND_VT;
                String[] tmp = l.substring(2).split(" ");
                String[] tcs = new String[3];
                for (int i = 0, j = 0; i < tmp.length; i++) {
                    if (!Objects.equals(tmp[i], "")) {
                        tcs[j] = tmp[i];
                        j++;
                    }
                }
                cmds[line].tx = Float.parseFloat(tcs[0]);
                cmds[line].ty = Float.parseFloat(tcs[1]);
                numVT++;
            }
            else if (l.startsWith("f ")) {
                int num_f = 0;
                cmds[line].type = CommandType.COMMAND_F;
                OBJVertexIndex[] f = new OBJVertexIndex[16];
                for (int y = 0; y < f.length; y++) {
                    f[y] = new OBJVertexIndex();
                }
                // f vf/tf/nf vf/tf/nf vf/tf/nf
                String[] tmp = l.substring(2).split("[/ ]");

                for (int i = 0; i < 3; i++) {
                    OBJVertexIndex vi = new OBJVertexIndex();
                    vi.vIndex  = Integer.parseInt(tmp[0 + (3*i)]);
                    vi.vtIndex = Integer.parseInt(tmp[1 + (3*i)]);
                    vi.vnIndex = Integer.parseInt(tmp[2 + (3*i)]);

                    f[num_f] = vi;
                    num_f++;
                }

                if(triangulate) {
                    int k;
                    int n = 0;

                    OBJVertexIndex i0 = f[0];
                    OBJVertexIndex i1;
                    OBJVertexIndex i2 = f[1];

                    if (3 * num_f < 16) {
                        for (k = 2; k < num_f; k++) {
                            i1 = i2;
                            i2 = f[k];
                            cmds[line].f[3 * n + 0] = i0;
                            cmds[line].f[3 * n + 1] = i1;
                            cmds[line].f[3 * n + 2] = i2;

                            cmds[line].fNumVerts[n] = 3;
                            n++;
                        }
                        cmds[line].numF = 3 * n;
                        cmds[line].numFNumVerts = n;
                    }
                }
                else {
                    int k;
                    assert (numF < 16);
                    for (k = 0; k < numF; k++) {
                        cmds[line].f[k] = f[k];
                    }

                    cmds[line].numF = numF;
                    cmds[line].fNumVerts[0] = numF;
                    cmds[line].numFNumVerts = 1;
                }
                //numF += cmds[line].numF;
                //num_faces += cmds[line].numFNumVerts;
            }
            else if(l.startsWith("g ")) {
                cmds[line].type = CommandType.COMMAND_G;
                cmds[line].materialName = l.substring(2);
                numG++;
            }
            else if(l.startsWith("o ")) {
                cmds[line].type = CommandType.COMMAND_O;
                cmds[line].materialName = l.substring(2);
                numO++;
            }
            else if (l.startsWith("usemtl ")) {
                cmds[line].type = CommandType.COMMAND_USEMTL;
                cmds[line].materialName = l.substring(7);
                numM++;
            }
            else if(l.startsWith("#") || l.length() < 2) {
                cmds[line].type = CommandType.COMMAND_EMPTY;
            }
            line++;
        }
        return cmds;
    }

}
