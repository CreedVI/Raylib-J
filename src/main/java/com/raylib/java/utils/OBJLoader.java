package com.raylib.java.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class OBJLoader {

    /**
     * OB-J Loader v1.0
     * written by CreedVI for use in Raylib-J.
     * Based on TinyOBJLoader-C by Syoyo https://github.com/syoyo/tinyobjloader-c
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

        public OBJShape[] shapes;
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
                diffuse[i] = 1.0f;
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
        String mtllibName;

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
        cmds = parseLines(fileText, triangulate);

        int numV = 0, numVN = 0, numVT = 0, numF = 0, numFaces = 0;
        int mtllibLineIndex = -1;

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
                    mtllibLineIndex = i;
            }
        }

        /* Load material(if exits) */
        if (mtllibLineIndex >= 0 && cmds[mtllibLineIndex].mtllibName != null && cmds[mtllibLineIndex].mtllibName.length() > 0) {
            String filename = cmds[mtllibLineIndex].mtllibName;

            boolean ret = ReadMTL(filename);

            if (!ret) {
                /* warning. */
                System.out.println("OB-J: Failed to parse material file " + filename);
            }

        }

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
                        objInfo.numVPerLine[faceCount + k] = cmds[i].numFNumVerts;
                    }
                    
                    fCount += cmds[i].numF;
                    faceCount += cmds[i].numFNumVerts;
                    break;
                case COMMAND_USEMTL:
                    if (cmds[i].materialName != null && cmds[i].materialName.length() > 0) {
                        for (int j = 0; j < mtlInfo.materials.length; j++) {
                            if (mtlInfo.materials[j].name.equals(cmds[i].materialName)) {
                                materialID = j;
                                break;
                            }
                            else {
                                materialID= -1;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

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
                }
                else {
                    if (shapeIndex == 0) {
                        shapes[shapeIndex].name = prevShapeName;
                        shapes[shapeIndex].faceOffset = prevShape.faceOffset;
                        shapes[shapeIndex].length = faceCount - prevFaceOffset;

                        shapeIndex++;
                        prevFaceOffset = faceCount;
                    }
                    else {
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
            else if (cmds[i].type == CommandType.COMMAND_F) {
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
        objInfo.shapes = shapes;

        objInfo.totalMaterials = mtlInfo.numberOfMaterials;

        cmds = null;
        return true;
    }

    public boolean ReadMTL(String filetext) {
        mtlInfo = new MTLInfo();
        String[] token;
        try {
            token = FileIO.LoadFileText(filetext).split("(\\r\\n|\\r|\\n)");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean hasPreviousMaterial = false;
        ArrayList<Material> materials = new ArrayList<>();
        Material material = new Material();
        int numMaterials = 0;

        for (int i = 0; i < token.length; i++) {

            if (token[i].startsWith("\0")) {
                continue; /* empty line */
            }

            if (token[i].startsWith("#")) {
                continue; /* comment line */
            }

            /* new mtl */
            if (token[i].startsWith("newmtl ")) {
                String name;

                /* flush previous material. */
                if (hasPreviousMaterial) {
                    materials.add(material);
                    numMaterials++;
                } else {
                    hasPreviousMaterial = true;
                }

                /* initial temporary material */
                material = new Material();

                /* set new mtl name */
                material.name = token[i].substring(7);

                continue;
            }

            /* ambient */
            if (token[i].startsWith("Ka ")) {
                String[] tmp = token[i].substring(3).split(" ");
                String[] rgb = new String[3];
                for (int k = 0, j = 0; k < rgb.length; k++) {
                    if (!Objects.equals(tmp[k], "")) {
                        rgb[j] = tmp[k];
                        j++;
                    }
                }

                material.ambient[0] = Float.parseFloat(rgb[0]);
                material.ambient[1] = Float.parseFloat(rgb[1]);
                material.ambient[2] = Float.parseFloat(rgb[2]);
                continue;
            }

            /* diffuse */
            if (token[i].startsWith("Kd ")) {
                String[] tmp = token[i].substring(3).split(" ");
                String[] rgb = new String[3];
                for (int k = 0, j = 0; k < rgb.length; k++) {
                    if (!Objects.equals(tmp[k], "")) {
                        rgb[j] = tmp[k];
                        j++;
                    }
                }
                material.diffuse[0] = Float.parseFloat(rgb[0]);
                material.diffuse[1] = Float.parseFloat(rgb[1]);
                material.diffuse[2] = Float.parseFloat(rgb[2]);
                continue;
            }

            /* specular */
            if (token[i].startsWith("Ks ")) {
                String[] tmp = token[i].substring(3).split(" ");
                String[] rgb = new String[3];
                for (int k = 0, j = 0; k < rgb.length; k++) {
                    if (!Objects.equals(tmp[k], "")) {
                        rgb[j] = tmp[k];
                        j++;
                    }
                }
                material.specular[0] = Float.parseFloat(rgb[0]);
                material.specular[1] = Float.parseFloat(rgb[1]);
                material.specular[2] = Float.parseFloat(rgb[2]);
                continue;
            }

            /* transmittance */
            if (token[i].startsWith("Kt ")) {
                String[] tmp = token[i].substring(3).split(" ");
                String[] rgb = new String[3];
                for (int k = 0, j = 0; k < rgb.length; k++) {
                    if (!Objects.equals(tmp[k], "")) {
                        rgb[j] = tmp[k];
                        j++;
                    }
                }
                material.transmittance[0] = Float.parseFloat(rgb[0]);
                material.transmittance[1] = Float.parseFloat(rgb[1]);
                material.transmittance[2] = Float.parseFloat(rgb[2]);
                continue;
            }

            /* ior(index of refraction) */
            if (token[i].startsWith("Ni ")) {
                material.ior = Float.parseFloat(token[i].substring(3));
                continue;
            }

            /* emission */
            if (token[i].startsWith("Ke ")) {
                String[] tmp = token[i].substring(3).split(" ");
                String[] rgb = new String[3];
                for (int k = 0, j = 0; k < rgb.length; k++) {
                    if (!Objects.equals(tmp[k], "")) {
                        rgb[j] = tmp[k];
                        j++;
                    }
                }
                material.emission[0] = Float.parseFloat(rgb[0]);
                material.emission[1] = Float.parseFloat(rgb[1]);
                material.emission[2] = Float.parseFloat(rgb[2]);
                continue;
            }

            /* shininess */
            if (token[i].startsWith("Ns ")) {
                material.shininess = Float.parseFloat(token[i].substring(3));
                continue;
            }

            /* illum model */
            if (token[i].startsWith("illum ")) {
                material.illum = Integer.parseInt(token[i].substring(6));
                continue;
            }

            /* dissolve */
            if (token[i].startsWith("d ")) {
                material.dissolve = Float.parseFloat(token[i].substring(2));
                continue;
            }
            if (token[i].startsWith("Tr ")) {
                /* Invert value of Tr(assume Tr is in range [0, 1]) */
                material.dissolve = 1.0f - Float.parseFloat(token[i].substring(3));
                continue;
            }

            /* ambient texture */
            if (token[i].startsWith("map_Ka ")) {
                material.ambient_texname = token[i].substring(7);
                continue;
            }

            /* diffuse texture */
            if (token[i].startsWith("map_Kd ")) {
                material.diffuse_texname = token[i].substring(7);
                continue;
            }

            /* specular texture */
            if (token[i].startsWith("map_Ks ")) {
                material.specular_texname = token[i].substring(7);
                continue;
            }

            /* specular highlight texture */
            if (token[i].startsWith("map_Ns ")) {
                material.specular_highlight_texname = token[i].substring(7);
                continue;
            }

            /* bump texture */
            if (token[i].startsWith("map_bump ")) {
                material.bump_texname = token[i].substring(8);
                continue;
            }

            /* alpha texture */
            if (token[i].startsWith("map_d ")) {
                material.alpha_texname = token[i].substring(6);
                continue;
            }

            /* bump texture */
            if (token[i].startsWith("bump ")) {
                material.bump_texname = token[i].substring(5);
                continue;
            }

            /* displacement texture */
            if (token[i].startsWith("disp ")) {
                material.displacement_texname = token[i].substring(5);
                continue;
            }

            /* @todo { unknown parameter } */
        }

        if (material.name != null) {
            /* Flush last material element */
            materials.add(material);
            numMaterials++;
        }

        mtlInfo.materials = materials.toArray(new Material[materials.size()]);
        mtlInfo.numberOfMaterials = mtlInfo.materials.length;

        return true;
    }

    private Command[] parseLines(String fileText, boolean triangulate) {
        int numV = 0, numVT = 0, numVN = 0, numF = 0, numG = 0, numM = 0, num_faces = 0, numO = 0;
        int line = 0;

        String[] lines = fileText.split("(\\r\\n|\\r|\\n)");
        Command[] cmds = new Command[lines.length];
        for (int i = 0; i < cmds.length; i++) {
            cmds[i] = new Command();
        }

        for (String l : lines) {
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
            }
            else if (l.startsWith("usemtl ")) {
                cmds[line].type = CommandType.COMMAND_USEMTL;
                cmds[line].materialName = l.substring(7);
            }
            else if (l.startsWith("mtllib ")) {
                cmds[line].mtllibName = l.substring(7);
                cmds[line].type = CommandType.COMMAND_MTLLIB;
            }
            else if(l.startsWith("g ")) {
                cmds[line].type = CommandType.COMMAND_G;
                cmds[line].groupName = l.substring(2);
            }
            else if(l.startsWith("o ")) {
                cmds[line].type = CommandType.COMMAND_O;
                cmds[line].objectName = l.substring(2);
            }
            else {
                cmds[line].type = CommandType.COMMAND_EMPTY;
            }
            line++;
        }
        return cmds;
    }

}
