package com.raylib.java.io;

import java.io.IOException;

public interface SaveFileDataCallback {

    /**
     * Save data to file from buffer.<br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName Path and extension of where the file should be created
     * @param data     Buffer of bytes to be written
     * @return Success status of operation
     * @throws IOException If file fails to write to disk
     */
    boolean SaveFileData(String fileName, byte[] data) throws IOException;

}
