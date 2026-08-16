package com.raylib.java.io;

import java.io.IOException;

public interface SaveFileTextCallback {

    /**
     * Save data as text to file. <br/>
     * Files are located using path relative to the application's current directory.
     * If the file exists on disk, it will be overwritten
     *
     * @param fileName Name and extension of the file to be saved.
     * @param text     String to be written to file
     * @return Returns `true` on successful file write
     * @throws IOException If file fails to write to disk
     */
    boolean SaveFileText(String fileName, String text) throws IOException;

}
