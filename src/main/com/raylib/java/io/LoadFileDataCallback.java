package com.raylib.java.io;

import java.io.IOException;

public interface LoadFileDataCallback {

    /**
     * Load data from file into a buffer. <br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName Path and extension of file to read
     * @return byte array of data loaded from file
     * @throws IOException If file fails to load form disk
     */
    byte[] LoadFileData(String fileName) throws IOException;

}
