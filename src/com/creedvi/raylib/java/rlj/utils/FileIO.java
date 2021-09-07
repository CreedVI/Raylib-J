package com.creedvi.raylib.java.rlj.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_STANDARD_FILEIO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.LOG_WARNING;

public class FileIO{

    //TODO: FIGURE OUT THIS MESS
    //Load data from file into a buffer
    public static byte[] LoadFileData(String fileName) throws IOException{
        byte[] data = null;

        if (SUPPORT_STANDARD_FILEIO){

            InputStream inputStream = getFileFromResourceAsStream(fileName);

            if (inputStream != null){
                try{
                    BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream));
                    String contents = breader.lines().collect(Collectors.joining());
                    data = new byte[contents.length()];
                    for (int i = 0; i < data.length; i++){
                        data[i] = (byte) contents.charAt(i);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: File name provided is not valid\n\t" + fileName);
            }
        }

        else{
            Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
        }

        return data;
    }

    public static File LoadFileAsFile(String filePath){
        File tmp = null;
        if (SUPPORT_STANDARD_FILEIO){
            InputStream inputStream = getFileFromResourceAsStream(filePath.replace('\\', '/'));
            if (inputStream != null){
                tmp = new File(inputStream.toString());
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: File name provided is not valid");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
        }
        return tmp;
    }

    public static InputStream LoadFileAsInputStream(String filePath){
        if (SUPPORT_STANDARD_FILEIO){
            InputStream inputStream = getFileFromResourceAsStream(filePath.replace('\\', '/'));
            if (inputStream != null){
                return inputStream;
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: File name provided is not valid");
                return null;
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
        }
        return null;
    }


    // Unload file data allocated by LoadFileData()
    Object UnloadFileData(){
        return null;
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
            String text;
            Path path = Paths.get(fileName);
            if (SUPPORT_STANDARD_FILEIO){
                if (path.toFile().exists()){
                    text = Files.readAllLines(path).stream().collect(Collectors.joining(System.lineSeparator()));
                    Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Text file loaded successfully");
                    return text;
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

    private static InputStream getFileFromResourceAsStream(String fileName){
        // The class loader that loaded the class
        ClassLoader classLoader = FileIO.class.getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }
}