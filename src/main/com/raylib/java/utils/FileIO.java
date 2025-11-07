package com.raylib.java.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static com.raylib.java.Config.SUPPORT_STANDARD_FILEIO;
import static com.raylib.java.utils.Tracelog.Tracelog;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_INFO;
import static com.raylib.java.utils.Tracelog.TracelogType.LOG_WARNING;

public class FileIO{

    //TODO: FIGURE OUT THIS MESS
    //Load data from file into a buffer
    public static byte[] LoadFileData(String fileName) throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        if (SUPPORT_STANDARD_FILEIO){

            InputStream inputStream;
            if (fileName.contains("/")) {
                inputStream = FileIO.class.getResourceAsStream(fileName.substring(fileName.lastIndexOf('/')));
            }
            else {
                inputStream = FileIO.class.getResourceAsStream("/"+fileName);
            }
            if(inputStream == null) {
                String ext = fileName.substring(fileName.lastIndexOf('.')).toUpperCase();
                inputStream = getFileFromResourceAsStream(fileName.substring(0, fileName.lastIndexOf('.'))+ext);
            }

            if (inputStream != null) {
                try {
                    int read;
                    byte[] input = new byte[4096];
                    while (-1 != (read = inputStream.read(input))) {
                        buffer.write(input, 0, read);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return buffer.toByteArray();
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
        }

        return null;
    }

    // Unload file data allocated by LoadFileData()
    public static void UnloadFileData(Object o){
        o = null;
    }

    // Save data to file from buffer
    public static boolean SaveFileData(String fileName, byte[] data, int bytesToWrite) throws IOException{
        boolean success = false;

        if (fileName != null){
            if (SUPPORT_STANDARD_FILEIO){
                Path path = Paths.get(fileName);

                if (!path.toFile().exists()){
                    try{
                        Files.write(path, data);
                    } catch (IOException exception){
                        Tracelog(LOG_WARNING, "FILEIO: Failed to write file" + fileName);
                        throw exception;
                    } finally{
                        success = true;
                    }
                }
                else{
                    Tracelog(LOG_INFO, "FILEIO: Overwriting file " + fileName);
                    try{
                        Files.write(path, data);
                    } catch (IOException exception){
                        Tracelog(LOG_WARNING, "FILEIO: Failed to write file" + fileName);
                        throw exception;
                    } finally{
                        success = true;
                    }
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: File name provided is not valid");
        }

        return success;
    }

    /**
     * Load text data from file
     * NOTE: text chars array should be freed manually
     *
     * @param fileName name and extension of file to be loaded
     */
    public static String LoadFileText(String fileName) throws IOException{
        if (fileName != null){

            if (SUPPORT_STANDARD_FILEIO){
                InputStream inputStream;
                if (fileName.contains("/")) {
                    inputStream = FileIO.class.getResourceAsStream(fileName.substring(fileName.lastIndexOf('/')));
                }
                else {
                    inputStream = FileIO.class.getResourceAsStream("/"+fileName);
                }
                if(inputStream == null) {
                    String ext = fileName.substring(fileName.lastIndexOf('.')).toUpperCase();
                    inputStream = getFileFromResourceAsStream(fileName.substring(0, fileName.lastIndexOf('.'))+ext);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String[] tmp = reader.lines().toArray(String[]::new);
                StringBuilder builder = new StringBuilder();

                for (String s : tmp) {
                    builder.append(s);
                    builder.append("\n");
                }

                if (tmp != null) {
                    return builder.toString();
                }
                else{
                    Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to open text file");
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
            }
        }
        return null;
    }

    // Unload file text data allocated by LoadFileText()
    public static Object UnloadFileText(){
        return null;
    }

    // Save text data to file (write), string must be '\0' terminated
    public static boolean SaveFileText(String fileName, String text) throws IOException{
        boolean success = false;

        if (fileName != null){

            if (SUPPORT_STANDARD_FILEIO){
                Path path = Paths.get(fileName);

                if (!path.toFile().exists()){
                    try{
                        Files.write(path, Collections.singleton(text));
                    } catch (IOException exception){
                        Tracelog(LOG_WARNING, "FILEIO: Failed to write file" + fileName);
                        throw exception;
                    } finally{
                        success = true;
                    }
                }
                else{
                    Tracelog(LOG_INFO, "FILEIO: Overwriting file " + fileName);
                    try{
                        Files.write(path, Collections.singleton(text));
                    } catch (IOException exception){
                        Tracelog(LOG_WARNING, "FILEIO: Failed to write file" + fileName);
                        throw exception;
                    } finally{
                        success = true;
                    }
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: File name provided is not valid");
        }

        return success;
    }

    private static InputStream getFileFromResourceAsStream(String fileName){
        return FileIO.class.getResourceAsStream(fileName);
    }
}