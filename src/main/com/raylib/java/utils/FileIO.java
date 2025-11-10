package com.raylib.java.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.raylib.java.Config.SUPPORT_STANDARD_FILEIO;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;

public class FileIO{

    /**
     * Load data from file into a buffer
     * Supports relative and absolute paths.
     *
     * @param fileName Path and extension of file to read
     * @return byte array of data loaded from file
     * @throws IOException If Java fails to load file form disk
     */
    public static byte[] LoadFileData(String fileName) throws IOException{
        byte[] fileData = null;

        if (fileName != null) {
            if (SUPPORT_STANDARD_FILEIO) {
                Path path = Paths.get(fileName);

                try {
                    fileData = Files.readAllBytes(path);
                }
                catch (IOException exception) {
                    Tracelog(LOG_WARNING, "FILE IO: Failed to read file: " + path);
                    throw exception;
                }

            }
            else {
                Tracelog(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILE IO: File name provided is not valid");
        }

        return fileData;
    }

    /**
     * Save data to file from buffer.
     * Supports relative and absolute paths.
     *
     * @param fileName Path and extension of where the file should be created
     * @param data Buffer of bytes to be written
     * @return Success status of operation
     * @throws IOException If Java fails to write file to disk
     */
    public static boolean SaveFileData(String fileName, byte[] data) throws IOException{
        boolean success = false;

        if (fileName != null){
            if (SUPPORT_STANDARD_FILEIO){
                Path path = Paths.get(fileName);

                if (!path.toFile().exists()){
                    try{
                        Files.write(path, data);
                    } catch (IOException exception){
                        Tracelog(LOG_WARNING, "FILE IO: Failed to write file: " + fileName);
                        throw exception;
                    } finally{
                        success = true;
                    }
                }
                else{
                    Tracelog(LOG_INFO, "FILE IO: Overwriting file: " + fileName);
                    try{
                        Files.write(path, data);
                    } catch (IOException exception){
                        Tracelog(LOG_WARNING, "FILE IO: Failed to write file: " + fileName);
                        throw exception;
                    } finally{
                        success = true;
                    }
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILE IO: File name provided is not valid");
        }

        return success;
    }

    /**
     * Load text data from file
     * Supports relative and absolute paths.
     *
     * @param fileName name and extension of file to be loaded
     * @throws IOException If Java fails to load file form disk
     */
    public static String LoadFileText(String fileName) throws IOException{
        StringBuilder text = new StringBuilder();

        if (fileName != null){
            if (SUPPORT_STANDARD_FILEIO){
                try {
                    BufferedReader buffer = new BufferedReader(new FileReader(fileName));
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        text.append(line);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    Tracelog(LOG_WARNING, "FILE IO: Failed to read file: " + fileName);
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILE IO: File name provided is not valid");
        }
        return text.toString();
    }

    /**
     * Save text data to file (write).
     * Supports relative and absolute paths.
     * If the file exists on disk, it will be overwritten
     * @param fileName Name and extension of the file to be saved.
     * @param text String to be written to file
     * @return Returns `true` on successful file write
     * @throws IOException If Java fails to write file to disk
     */
    public static boolean SaveFileText(String fileName, String text) throws IOException{
        boolean success = false;

        if (fileName != null){

            //TODO: Check if file exists and warn overwrite

            if (SUPPORT_STANDARD_FILEIO){
                try {
                    BufferedWriter buffer = new BufferedWriter(new FileWriter(fileName));
                    buffer.write(text);
                    buffer.close();
                    success = true;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    Tracelog(LOG_WARNING, "FILE IO: Failed to write file: " + fileName);
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILE IO: File name provided is not valid");
        }

        return success;
    }

    private static InputStream getFileFromResourceAsStream(String fileName){
        return FileIO.class.getResourceAsStream(fileName);
    }
}