package com.raylib.java.io;

import java.io.IOException;

public interface LoadFileTextCallback {

    /**
     * Load data from file as a String <br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName name and extension of file to be loaded
     * @throws IOException If file fails to load form disk
     */
    String LoadFileText(String fileName) throws IOException;

}
