package com.creedvi.raylib.java.rlj.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_STANDARD_FILEIO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_WARNING;

public class FileIO{

    //TODO: FIGURE OUT THIS MESS
    // Load data from file into a buffer
    public static byte[] LoadFileData(String fileName) throws IOException{
        byte[] data = null;
        Path path = Paths.get(fileName);

        if (path.toFile().exists()){
            if (SUPPORT_STANDARD_FILEIO){
                try{
                    data = Files.readAllBytes(path);
                } catch (IOException exception){
                    Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to open file");
                    throw exception;
                }

            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: File name provided is not valid");
        }

        return data;
    }

    // Unload file data allocated by LoadFileData()
     Object UnloadFileData(){
        return null;
    }

    // Save data to file from buffer
    boolean SaveFileData(String fileName, byte[] data, int bytesToWrite) throws IOException{
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
     * @param fileName name and extension of file to be loaded
     */
    public static String LoadFileText(String fileName) throws IOException{
        StringBuilder text = new StringBuilder();
        Path path = Paths.get(fileName);
        if(SUPPORT_STANDARD_FILEIO){
            if (path.toFile().exists()){
                byte[] bytes = Files.readAllBytes(path);
                for (byte aByte: bytes){
                    text.append((char) aByte);
                }
                text = new StringBuilder(Arrays.toString(bytes));
                Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Text file loaded successfully");
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: [" + fileName + " Failed to open text file");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
        }
        return text.toString();
    }

    // Unload file text data allocated by LoadFileText()
    Object UnloadFileText(){
        return null;
    }

    // Save text data to file (write), string must be '\0' terminated
    boolean SaveFileText(String fileName, String text) throws IOException{
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
}