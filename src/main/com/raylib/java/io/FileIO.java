package com.raylib.java.io;

import com.raylib.java.Raylib;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.raylib.java.Config.SUPPORT_STANDARD_FILEIO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_INFO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;

public class FileIO {

    private final Raylib context;
    private final String workingDirectory;

    public FileIO(Raylib context) {
        this.context = context;
        workingDirectory = context.core.GetApplicationDirectory() + "/";
    }

    /**
     * Load data from file into a buffer. <br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName Path and extension of file to read
     * @return byte array of data loaded from file
     * @throws IOException If file fails to load form disk
     */
    public byte[] LoadFileData(String fileName) throws IOException{
        byte[] fileData = null;

        if (fileName != null) {
            if (SUPPORT_STANDARD_FILEIO) {
                Path path = Paths.get(workingDirectory + fileName);

                try {
                    fileData = Files.readAllBytes(path);
                }
                catch (IOException exception) {
                    context.logger.TRACELOG(LOG_WARNING, "FILE IO: Failed to read file: " + path);
                    throw exception;
                }

            }
            else {
                context.logger.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            context.logger.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
        }

        return fileData;
    }

    /**
     * Save data to file from buffer.<br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName Path and extension of where the file should be created
     * @param data Buffer of bytes to be written
     * @return Success status of operation
     * @throws IOException If file fails to write to disk
     */
    public boolean SaveFileData(String fileName, byte[] data) throws IOException{
        boolean success = false;

        if (fileName != null){
            if (SUPPORT_STANDARD_FILEIO){
                Path path = Paths.get(workingDirectory + fileName);

                if (!path.toFile().exists()){
                    try{
                        Files.write(path, data);
                        success = true;
                    }
                    catch (IOException exception){
                        context.logger.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                        throw exception;
                    }
                }
                else{
                    context.logger.TRACELOG(LOG_INFO, "FILE IO: Overwriting file: " + path);
                    try{
                        Files.write(path, data);
                        success = true;
                    }
                    catch (IOException exception){
                        context.logger.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                        throw exception;
                    }
                }
            }
            else{
                context.logger.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            context.logger.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
        }

        return success;
    }

    /**
     * Load data from file as a String <br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName name and extension of file to be loaded
     * @throws IOException If file fails to load form disk
     */
    public String LoadFileText(String fileName) throws IOException{
        String text = new String();

        if (fileName != null){
            if (SUPPORT_STANDARD_FILEIO){
                Path path = Paths.get(workingDirectory + fileName);

                try {
                    text = Files.readString(path);
                }
                catch (IOException exception) {
                    context.logger.TRACELOG(LOG_WARNING, "FILE IO: Failed to read file: " + path);
                    throw exception;
                }
            }
            else{
                context.logger.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            context.logger.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
        }
        return text;
    }

    /**
     * Save data as text to file. <br/>
     * Files are located using path relative to the application's current directory.
     * If the file exists on disk, it will be overwritten
     * @param fileName Name and extension of the file to be saved.
     * @param text String to be written to file
     * @return Returns `true` on successful file write
     * @throws IOException If file fails to write to disk
     */
    public boolean SaveFileText(String fileName, String text) throws IOException{
        boolean success = false;

        if (fileName != null){
            if (SUPPORT_STANDARD_FILEIO){
                Path path = Paths.get(workingDirectory + fileName);

                if (!path.toFile().exists()){
                    try{
                        Files.writeString(path, text);
                        success = true;
                    }
                    catch (IOException exception){
                        context.logger.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                        throw exception;
                    }
                }
                else{
                    context.logger.TRACELOG(LOG_INFO, "FILE IO: Overwriting file: " + path);
                    try{
                        Files.writeString(path, text);
                        success = true;
                    }
                    catch (IOException exception){
                        context.logger.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                        throw exception;
                    }
                }
            }
            else{
                context.logger.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            context.logger.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
        }

        return success;
    }

}
