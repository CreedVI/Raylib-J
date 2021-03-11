package com.creedvi.raylib.java.rlj.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_STANDARD_FILEIO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_WARNING;

public class Files{
    /*
    //TODO: FIGURE OUT THIS MESS
    // Load data from file into a buffer
    String LoadFileData(String fileName, int bytesRead){
        String data = null;
        bytesRead = 0;

        if (fileName != null){
            if (loadFileData){
                data = loadFileData(fileName, bytesRead);
                return data;
            }
            if (SUPPORT_STANDARD_FILEIO){
                FILE * file = fopen(fileName, "rb");

                if (file != null){
                    // WARNING: On binary streams SEEK_END could not be found,
                    // using fseek() and ftell() could not work in some (rare) cases
                    fseek(file, 0, SEEK_END);
                    int size = ftell(file);
                    fseek(file, 0, SEEK_SET);

                    if (size > 0){
                        data = (unsigned char *)RL_MALLOC(size * sizeof(unsigned char));

                        // NOTE: fread() returns number of read elements instead of bytes, so we read [1 byte, size elements]
                        unsigned int count = (unsigned int)fread(data, sizeof(unsigned char),size, file);
                            *bytesRead = count;

                        if (count != size){
                            Tracelog(LOG_WARNING, "FILEIO: [%s] File partially loaded", fileName);
                        }
                        else{
                            Tracelog(LOG_INFO, "FILEIO: [%s] File loaded successfully", fileName);
                        }
                    }
                    else{
                        Tracelog(LOG_WARNING, "FILEIO: [%s] Failed to read file", fileName);
                    }

                    fclose(file);
                }
                else{
                    Tracelog(LOG_WARNING, "FILEIO: [%s] Failed to open file", fileName);
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
    void UnloadFileData(unsigned char*data){
        RL_FREE(data);
    }

    // Save data to file from buffer
    boolean SaveFileData(const char *fileName, void *data, unsigned int bytesToWrite){
        boolean success = false;

        if (fileName != null){
            if (saveFileData){
                saveFileData(fileName, data, bytesToWrite);
                return success;
            }
            if (SUPPORT_STANDARD_FILEIO){
                FILE * file = fopen(fileName, "wb");

                if (file != null){
                    unsigned int count = (unsigned int)fwrite(data, sizeof(unsigned char),bytesToWrite, file);

                    if (count == 0){
                        Tracelog(LOG_WARNING, "FILEIO: [%s] Failed to write file", fileName);
                    }
                    else if (count != bytesToWrite){
                        Tracelog(LOG_WARNING, "FILEIO: [%s] File partially written", fileName);
                    }
                    else{
                        Tracelog(LOG_INFO, "FILEIO: [%s] File saved successfully", fileName);
                    }

                    int result = fclose(file);
                    if (result == 0) success = true;
                }
                else{
                    Tracelog(LOG_WARNING, "FILEIO: [%s] Failed to open file", fileName);
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
    */
    // Load text data from file, returns a '\0' terminated string
    // NOTE: text chars array should be freed manually
    public static String LoadFileText(String fileName) throws IOException{
        String text = "";
        File file = new File(fileName);
        if(SUPPORT_STANDARD_FILEIO){
            if (file.exists()){
                text = java.nio.file.Files.readString(Path.of(fileName));
                Tracelog(LOG_INFO, "FILEIO: [" + fileName + "] Text file loaded successfully");
            }
            else{
                Tracelog(LOG_WARNING, "FILEIO: [" + fileName + " Failed to open text file");
            }
        }
        else{
            Tracelog(LOG_WARNING, "FILEIO: Standard file io not supported, use custom file callback");
        }
        return text;
    }
    /*
    // Unload file text data allocated by LoadFileText()
    void UnloadFileText(unsigned char*text){
        RL_FREE(text);
    }

    // Save text data to file (write), string must be '\0' terminated
    boolean SaveFileText(String fileName, String text){
        boolean success = false;

        if (fileName != null){
            if (saveFileText){
                saveFileText(fileName, text);
                return success;
            }
            if (SUPPORT_STANDARD_FILEIO){
                FILE * file = fopen(fileName, "wt");

                if (file != null){
                    int count = fprintf(file, "%s", text);

                    if (count < 0){
                        Tracelog(LOG_WARNING, "FILEIO: [%s] Failed to write text file", fileName);
                    }
                    else{
                        Tracelog(LOG_INFO, "FILEIO: [%s] Text file saved successfully", fileName);
                    }

                    int result = fclose(file);
                    if (result == 0) success = true;
                }
                else{
                    Tracelog(LOG_WARNING, "FILEIO: [" + fileName + "] Failed to open text file");
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
     */
}