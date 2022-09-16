package com.raylib.java.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 *    Vox-J translated by CreedVI for use within Raylib-J
 *
 *    The MIT License (MIT)
 *
 *    Copyright (c) 2021 Johann Nadalutti.
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *    THE SOFTWARE.
 */

public class VoxLoader {

    // VoxColor, 4 components, R8G8B8A8 (32bit)
    public static class VoxColor {
        public byte r, g, b, a;

        public VoxColor(byte r, byte g, byte b, byte a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    // VoxVector3, 3 components
    public static class VoxVector3 {
        public float x, y, z;

        public VoxVector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public VoxVector3() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
    }

    public static class ArrayVector3 {
        public VoxVector3[] array;
        public int used, size;

        protected ArrayVector3() {
            initArrayVector3(64);
        }

        protected void initArrayVector3(int initialSize) {
            this.array = new VoxVector3[initialSize];
            this.used = 0;
            this.size = initialSize;
        }

        protected void insertArrayVector3(VoxVector3 element) {
            if (this.used == this.size) {
                VoxVector3[] tmp = new VoxVector3[this.size * 2];
                if (this.size >= 0) {
                    System.arraycopy(this.array, 0, tmp, 0, this.size);
                }
                this.size *= 2;
                this.array = tmp;
            }
            this.array[this.used++] = element;
        }

        protected void freeArrayVector3() {
            this.array = null;
            this.used = this.size = 0;
        }
    }

    public static class ArrayColor {
        public VoxColor[] array;
        public int used, size;

        protected ArrayColor() {
            initArrayColor(64);
        }

        protected void initArrayColor(int initialSize) {
            this.array = new VoxColor[initialSize];
            this.used = 0;
            this.size = initialSize;
        }

        protected void insertArrayColor(VoxColor element) {
            if (this.used == this.size) {
                VoxColor[] tmp = new VoxColor[this.size * 2];
                System.arraycopy(this.array, 0, tmp, 0, this.size);
                this.size *= 2;
                this.array = tmp;
            }
            this.array[this.used++] = element;
        }

        protected void freeArrayColor(ArrayColor a) {
            this.array = null;
            this.used = this.size = 0;
        }
    }

    public static class ArrayUShort {
        public short[] array;
        public int used, size;

        protected ArrayUShort(){
            initArrayUShort( 64);
        }

        protected void initArrayUShort(int initialSize) {
            this.array = new short[initialSize];
            this.used = 0;
            this.size = initialSize;
        }

        protected void insertArrayUShort(short element) {
            if (this.used == this.size) {
                short[] tmp = new short[this.size * 2];
                if (this.size >= 0) {
                    System.arraycopy(this.array, 0, tmp, 0, this.size);
                }
                this.size *= 2;
                this.array = tmp;
            }
            this.array[this.used++] = element;
        }

        protected void freeArrayUShort() {
            this.array = null;
            this.used = this.size = 0;
        }
    }

    // A chunk that contain voxels
    public static class CubeChunk3D{
        byte[] m_array; //If Sparse != null
        int arraySize; //Size for m_array in bytes (DEBUG ONLY)
    }

    // Array for voxels
    // Array is divised into chunks of CHUNKSIZE*CHUNKSIZE*CHUNKSIZE voxels size
    public static class VoxArray3D {
        // Array size in voxels
        int sizeX;
        int sizeY;
        int sizeZ;

        // Chunks size into array (array is divised into chunks)
        int chunksSizeX;
        int chunksSizeY;
        int chunksSizeZ;

        // Chunks array
        CubeChunk3D[] m_arrayChunks;
        int arrayChunksSize; // Size for m_arrayChunks in bytes (DEBUG ONLY)

        int ChunkFlattenOffset;
        int chunksAllocated;
        int chunksTotal;

        // Arrays for mesh build
        public ArrayVector3 vertices;
        public ArrayUShort indices;
        public ArrayColor colors;

        //Palette for voxels
        public VoxColor[] palette; //default size 256

        public VoxArray3D() {
            vertices = new ArrayVector3();
            indices = new ArrayUShort();
            colors = new ArrayColor();
            palette = new VoxColor[256];
        }

    }

    public static final int VOX_SUCCESS = 0;
    public static final int VOX_ERROR_FILE_NOT_FOUND = -1;
    public static final int VOX_ERROR_INVALID_FORMAT = -2;
    public static final int VOX_ERROR_FILE_VERSION_TOO_OLD = -3;

    private static final int CHUNKSIZE                   = 16;      // chunk size (CHUNKSIZE*CHUNKSIZE*CHUNKSIZE) in voxels
    private static final int CHUNKSIZE_OPSHIFT           =  4;      // 1<<4=16 -> Warning depend of CHUNKSIZE
    private static final int CHUNK_FLATTENOFFSET_OPSHIFT =  8;      // Warning depend of CHUNKSIZE

    //
    // used right handed system and CCW face
    //
    // indexes for voxelcoords, per face orientation
    //

    //#      Y
    //#      |
    //#      o----X
    //#     /
    //#    Z     2------------3
    //#         /|           /|
    //#        6------------7 |
    //#        | |          | |
    //#        |0 ----------|- 1
    //#        |/           |/
    //#        4------------5

    //
    // CCW
    final int[][] fv = {
            {0, 2, 6, 4 }, //-X
            {5, 7, 3, 1 }, //+X
            {0, 4, 5, 1 }, //-y
            {6, 2, 3, 7 }, //+y
            {1, 3, 2, 0 }, //-Z
            {4, 6, 7, 5 }  //+Z
        };

    final VoxVector3[] SolidVertex = {
            new VoxVector3(0, 0, 0),   //0
            new VoxVector3(1, 0, 0),   //1
            new VoxVector3(0, 1, 0),   //2
            new VoxVector3(1, 1, 0),   //3
            new VoxVector3(0, 0, 1),   //4
            new VoxVector3(1, 0, 1),   //5
            new VoxVector3(0, 1, 1),   //6
            new VoxVector3(1, 1, 1)    //7
    };

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Vox Loader
    /////////////////////////////////////////////////////////////////////////////////////////////

    public VoxArray3D pvoxArray;

    public VoxLoader() {
        this.pvoxArray = Vox_AllocArray(256,256,256);
    }

    // Allocated VoxArray3D size
    private VoxArray3D Vox_AllocArray(int _sx, int _sy, int _sz) {
        VoxArray3D result = new VoxArray3D();

        int sx = _sx + ((CHUNKSIZE - (_sx % CHUNKSIZE)) % CHUNKSIZE);
        int sy = _sy + ((CHUNKSIZE - (_sy % CHUNKSIZE)) % CHUNKSIZE);
        int sz = _sz + ((CHUNKSIZE - (_sz % CHUNKSIZE)) % CHUNKSIZE);

        int chx = sx >> CHUNKSIZE_OPSHIFT; //Chunks Count in X
        int chy = sy >> CHUNKSIZE_OPSHIFT; //Chunks Count in Y
        int chz = sz >> CHUNKSIZE_OPSHIFT; //Chunks Count in Z

        //VoxArray3D* parray = (VoxArray3D*)VOX_MALLOC(sizeof(VoxArray3D));
        result.sizeX = sx;
        result.sizeY = sy;
        result.sizeZ = sz;

        result.chunksSizeX = chx;
        result.chunksSizeY = chy;
        result.chunksSizeZ = chz;

        result.ChunkFlattenOffset = (chy * chz); //m_arrayChunks[(x * (sy*sz)) + (z * sy) + y]

        // Alloc chunks array
        int size = chx * chy * chz;
        result.m_arrayChunks = new CubeChunk3D[size];
        result.arrayChunksSize = size;

        // Init chunks array
        size = chx * chy * chz;
        result.chunksTotal = size;
        result.chunksAllocated = 0;

        for (int i = 0; i < size; i++) {
            result.m_arrayChunks[i] = new CubeChunk3D();
            result.m_arrayChunks[i].m_array = new byte[size];
            result.m_arrayChunks[i].arraySize = 0;
        }
        return result;
    }

    // Set voxel ID from its position into VoxArray3D
    private void Vox_SetVoxel(int x, int y, int z, int id) {
        // Get chunk from array pos
        int chX = x >> CHUNKSIZE_OPSHIFT; //x / CHUNKSIZE;
        int chY = y >> CHUNKSIZE_OPSHIFT; //y / CHUNKSIZE;
        int chZ = z >> CHUNKSIZE_OPSHIFT; //z / CHUNKSIZE;
        int offset = (chX * this.pvoxArray.ChunkFlattenOffset) + (chZ * this.pvoxArray.chunksSizeY) + chY;

        //if (offset > voxarray.arrayChunksSize)
        //{
        //	TraceLog(LOG_ERROR, "Out of array");
        //}

        CubeChunk3D chunk = this.pvoxArray.m_arrayChunks[offset];

        // Set Chunk
        chX = x - (chX << CHUNKSIZE_OPSHIFT); //x - (bx * CHUNKSIZE);
        chY = y - (chY << CHUNKSIZE_OPSHIFT); //y - (by * CHUNKSIZE);
        chZ = z - (chZ << CHUNKSIZE_OPSHIFT); //z - (bz * CHUNKSIZE);

        if (chunk.m_array == null) {
            int size = CHUNKSIZE * CHUNKSIZE * CHUNKSIZE;
            chunk.m_array = new byte[size];
            chunk.arraySize = size;
            Arrays.fill(chunk.m_array, 0, size, (byte) 0);

            this.pvoxArray.chunksAllocated++;
        }

        offset = (chX << CHUNK_FLATTENOFFSET_OPSHIFT) + (chZ << CHUNKSIZE_OPSHIFT) + chY;

        //if (offset > chunk.arraySize)
        //{
        //	TraceLog(LOG_ERROR, "Out of array");
        //}

        chunk.m_array[offset] = (byte) id;
    }

    // Get voxel ID from its position into VoxArray3D
    private byte Vox_GetVoxel(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) return 0;

        if (x >= this.pvoxArray.sizeX || y >= this.pvoxArray.sizeY || z >= this.pvoxArray.sizeZ) return 0;

        // Get chunk from array pos
        int chX = x >> CHUNKSIZE_OPSHIFT; //x / CHUNKSIZE;
        int chY = y >> CHUNKSIZE_OPSHIFT; //y / CHUNKSIZE;
        int chZ = z >> CHUNKSIZE_OPSHIFT; //z / CHUNKSIZE;
        int offset = (chX * this.pvoxArray.ChunkFlattenOffset) + (chZ * this.pvoxArray.chunksSizeY) + chY;

        //if (offset > voxarray.arrayChunksSize)
        //{
        //	TraceLog(LOG_ERROR, "Out of array");
        //}

        CubeChunk3D chunk = this.pvoxArray.m_arrayChunks[offset];

        // Set Chunk
        chX = x - (chX << CHUNKSIZE_OPSHIFT); //x - (bx * CHUNKSIZE);
        chY = y - (chY << CHUNKSIZE_OPSHIFT); //y - (by * CHUNKSIZE);
        chZ = z - (chZ << CHUNKSIZE_OPSHIFT); //z - (bz * CHUNKSIZE);

        if (chunk.m_array == null) {
            return 0;
        }

        offset = (chX << CHUNK_FLATTENOFFSET_OPSHIFT) + (chZ << CHUNKSIZE_OPSHIFT) + chY;

        //if (offset > chunk.arraySize)
        //{
        //	TraceLog(LOG_ERROR, "Out of array");
        //}
        return chunk.m_array[offset];

    }

    // Calc visibles faces from a voxel position
    private byte Vox_CalcFacesVisible(int cx, int cy, int cz) {
        byte idXp1 = Vox_GetVoxel(cx + 1, cy, cz);
        byte idXm1 = Vox_GetVoxel(cx - 1, cy, cz);

        byte idYm1 = Vox_GetVoxel(cx, cy - 1, cz);
        byte idYp1 = Vox_GetVoxel(cx, cy + 1, cz);

        byte idZm1 = Vox_GetVoxel(cx, cy, cz - 1);
        byte idZp1 = Vox_GetVoxel(cx, cy, cz + 1);

        byte byVFMask = 0;

        //#-x
        if (idXm1 == 0) byVFMask |= (1 << 0);

        //#+x
        if (idXp1 == 0) byVFMask |= (1 << 1);

        //#-y
        if (idYm1 == 0) byVFMask |= (1 << 2);

        //#+y
        if (idYp1 == 0) byVFMask |= (1 << 3);

        //#-z
        if (idZm1 == 0) byVFMask |= (1 << 4);

        //#+z
        if (idZp1 == 0) byVFMask |= (1 << 5);

        return byVFMask;
    }

    // Get a vertex position from a voxel's corner
    private VoxVector3 Vox_GetVertexPosition(int _wcx, int _wcy, int _wcz, int _nNumVertex) {
        float scale = 0.25f;

        VoxVector3 vtx = new VoxVector3();
        
        vtx.x = (SolidVertex[_nNumVertex].x + _wcx) * scale;
        vtx.y = (SolidVertex[_nNumVertex].y + _wcy) * scale;
        vtx.z = (SolidVertex[_nNumVertex].z + _wcz) * scale;

        return vtx;
    }

    // Build a voxel vertices/colors/indices
    private void Vox_Build_Voxel(int x, int y, int z, int matID) {

        int byVFMask = Byte.toUnsignedInt(Vox_CalcFacesVisible(x, y, z));

        if (byVFMask == 0) {
            return;
        }

        int i, j;
        VoxVector3[] vertComputed = new VoxVector3[8];
        int[] bVertexComputed = new int[8];
        Arrays.fill(vertComputed, new VoxVector3(0, 0, 0));
        Arrays.fill(bVertexComputed, 0);

        //For each Cube's faces
        for (i = 0; i < 6; i++) { // 6 faces
            if ((byVFMask & (1 << i)) != 0) {	//If face is visible
                for (j = 0; j < 4; j++) {  // 4 corners
                    int  nNumVertex = fv[i][j];  //Face,Corner
                    if (bVertexComputed[nNumVertex] == 0) { //if never calc
                        bVertexComputed[nNumVertex] = 1;
                        vertComputed[nNumVertex] = Vox_GetVertexPosition(x, y, z, nNumVertex);
                    }
                }
            }
        }

        //Add face
        for (i = 0; i < 6; i++) { // 6 faces

            if ((byVFMask & (1 << i)) == 0) {
                continue; //Face invisible
            }

            int v0 = fv[i][0];  //Face, Corner
            int v1 = fv[i][1];  //Face, Corner
            int v2 = fv[i][2];  //Face, Corner
            int v3 = fv[i][3];  //Face, Corner

            //Arrays
            int idx = this.pvoxArray.vertices.used;
            this.pvoxArray.vertices.insertArrayVector3(vertComputed[v0]);
            this.pvoxArray.vertices.insertArrayVector3(vertComputed[v1]);
            this.pvoxArray.vertices.insertArrayVector3(vertComputed[v2]);
            this.pvoxArray.vertices.insertArrayVector3(vertComputed[v3]);

            VoxColor col = this.pvoxArray.palette[matID];

            this.pvoxArray.colors.insertArrayColor(col);
            this.pvoxArray.colors.insertArrayColor(col);
            this.pvoxArray.colors.insertArrayColor(col);
            this.pvoxArray.colors.insertArrayColor(col);

            //v0 - v1 - v2, v0 - v2 - v3
            this.pvoxArray.indices.insertArrayUShort((short) (idx + 0));
            this.pvoxArray.indices.insertArrayUShort((short) (idx + 2));
            this.pvoxArray.indices.insertArrayUShort((short) (idx + 1));

            this.pvoxArray.indices.insertArrayUShort((short) (idx + 0));
            this.pvoxArray.indices.insertArrayUShort((short) (idx + 3));
            this.pvoxArray.indices.insertArrayUShort((short) (idx + 2));
        }

    }

    // MagicaVoxel *.vox file format Loader
    public int Vox_LoadFromMemory(byte[] pvoxData) {
        int voxDataSize = pvoxData.length;
        //////////////////////////////////////////////////
        // Read VOX file
        // 4 bytes: magic number ('V' 'O' 'X' 'space')
        // 4 bytes: version number (current version is 150)

        // @raysan5: Reviewed (unsigned long) -> (unsigned int), possible issue with Ubuntu 18.04 64bit

        // @raysan5: reviewed signature loading
        try(ByteArrayInputStream fileData = new ByteArrayInputStream(pvoxData)) {
            byte[] signature = new byte[4];
            int fileDataPtr = 0;

            fileData.read(signature);
            fileDataPtr += 4;

            if ((signature[0] != 'V') && (signature[1] != 'O') && (signature[3] != 'X') && (signature[4] != ' ')) {
                return VOX_ERROR_INVALID_FORMAT; //"Not an MagicaVoxel File format"
            }

            // @raysan5: reviewed version loading
            byte[] tmp = new byte[Integer.BYTES];
            fileData.read(tmp);
            int version = Vox_toInt(tmp);
            fileDataPtr += 4;

            if (Integer.compareUnsigned(version, 150) < 0) {
                return VOX_ERROR_FILE_VERSION_TOO_OLD; //"MagicaVoxel version too old"
            }

            // header
            //4 bytes: chunk id
            //4 bytes: size of chunk contents (n)
            //4 bytes: total size of children chunks(m)

            //// chunk content
            //n bytes: chunk contents

            //// children chunks : m bytes
            //{ child chunk 0 }
            //{ child chunk 1 }
            int sizeX, sizeY, sizeZ;
            int numVoxels;

            while (fileDataPtr < voxDataSize) {
                byte[] szChunkName = new byte[4];
                fileData.read(szChunkName);
                fileDataPtr += 4;

                fileData.read(tmp);
                int chunkSize = Vox_toInt(tmp);
                fileDataPtr += Integer.BYTES;

                //unsigned long chunkTotalChildSize = *((unsigned long*)fileDataPtr);
                fileData.read(tmp);
                fileDataPtr += Integer.BYTES;
                String chunk = "";
                for (byte b : szChunkName) {
                    chunk += (char) b;
                }

                if (chunk.equals("SIZE")) {
                    //(4 bytes x 3 : x, y, z )
                    fileData.read(tmp);
                    sizeX = Vox_toInt(tmp);
                    fileDataPtr += Integer.BYTES;

                    fileData.read(tmp);
                    sizeY = Vox_toInt(tmp);
                    fileDataPtr += Integer.BYTES;

                    fileData.read(tmp);
                    sizeZ = Vox_toInt(tmp);
                    fileDataPtr += Integer.BYTES;

                    //Alloc vox array
                    Vox_AllocArray(sizeX, sizeZ, sizeY);    //Reverse Y<>Z for left to right handed system
                }
                else if (chunk.equals("XYZI")) {
                    int vx, vy, vz, vi;

                    //(numVoxels : 4 bytes )
                    //(each voxel: 1 byte x 4 : x, y, z, colorIndex ) x numVoxels
                    fileData.read(tmp);
                    numVoxels  = Vox_toInt(tmp);
                    fileDataPtr += Integer.BYTES;

                    while (numVoxels > 0) {
                        vx = fileData.read(); fileDataPtr++;
                        vy = fileData.read(); fileDataPtr++;
                        vz = fileData.read(); fileDataPtr++;
                        vi = fileData.read(); fileDataPtr++;

                        Vox_SetVoxel(vx, vz, this.pvoxArray.sizeZ - vy - 1, vi); //Reverse Y<>Z for left to right handed system

                        numVoxels--;
                    }
                }
                else if (chunk.equals("RGBA")) {
                    //(each pixel: 1 byte x 4 : r, g, b, a ) x 256
                    for (int i = 0; i < 256 - 1; i++) {
                        VoxColor col = new VoxColor((byte) 0, (byte) 0, (byte) 0, (byte) 0);
                        col.r = (byte) fileData.read(); fileDataPtr++;
                        col.g = (byte) fileData.read(); fileDataPtr++;
                        col.b = (byte) fileData.read(); fileDataPtr++;
                        col.a = (byte) fileData.read(); fileDataPtr++;

                        this.pvoxArray.palette[i + 1] = col;
                    }

                }
                else {
                    //move ahead in the data...
                    fileData.skip(chunkSize);
                    fileDataPtr += chunkSize;
                }
            }

            //////////////////////////////////////////////////////////
            // Building Mesh
            //   TODO compute globals indices array

            // Init Arrays
            this.pvoxArray.vertices.initArrayVector3(3 * 1024);
            this.pvoxArray.indices.initArrayUShort(3 * 1024);
            this.pvoxArray.colors.initArrayColor(3 * 1024);

            // Create vertices and indices buffers
            int x, y, z;

            for (x = 0; x <= this.pvoxArray.sizeX; x++) {
                for (z = 0; z <= this.pvoxArray.sizeZ; z++) {
                    for (y = 0; y <= this.pvoxArray.sizeY; y++) {
                        byte matID = Vox_GetVoxel(x, y, z);
                        if (matID != 0) {
                            Vox_Build_Voxel(x, y, z, Byte.toUnsignedInt(matID));
                        }
                    }
                }
            }

            return VOX_SUCCESS;
        }
        catch (IOException e) {
            return VOX_ERROR_FILE_NOT_FOUND;
        }
    }

    public static void Vox_FreeArrays(VoxArray3D voxarray) {
        // Free chunks
        if (voxarray.m_arrayChunks != null) {
            for (int i = 0; i < voxarray.chunksTotal; i++) {
                CubeChunk3D chunk = voxarray.m_arrayChunks[i];
                if (chunk.m_array != null) {
                    chunk.arraySize = 0;
                    chunk.m_array = null;
                }
            }

            voxarray.m_arrayChunks = null;
            voxarray.arrayChunksSize = 0;

            voxarray.chunksSizeX = voxarray.chunksSizeY = voxarray.chunksSizeZ = 0;
            voxarray.chunksTotal = 0;
            voxarray.chunksAllocated = 0;
            voxarray.ChunkFlattenOffset = 0;
            voxarray.sizeX = voxarray.sizeY = voxarray.sizeZ = 0;
        }

        // Free arrays
        voxarray.vertices = null;
        voxarray.indices = null;
        voxarray.colors = null;
    }

    // Convert array of four bytes to int
    private static int Vox_toInt(byte[] bytes) {
        int ret = 0;
        // flip the bytes because .vox file format...
        byte[] tmp = new byte[bytes.length];
        System.arraycopy(bytes, 0, tmp, 0, bytes.length);
        for (int i = 0, k = bytes.length-1; i < bytes.length; i++, k--) {
            bytes[k] = tmp [i];
        }

        for (int i=0; i<4; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }
}
