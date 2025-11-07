package com.raylib.java.structs;

public class FilePathList {

    public final int MAX_FILEPATH_CAPACITY = 8192;

    public int capacity;                     // Filepaths max entries
    public int count;                        // Filepaths entries count
    public String[] paths;                   // Filepaths entries

    public FilePathList() {
        capacity = MAX_FILEPATH_CAPACITY;
        count = 0;
        paths = new String[MAX_FILEPATH_CAPACITY];
    }
}
