package com.raylib.java.structs;

/**
 * List of file paths
 */
public class FilePathList implements Cloneable {

    public final int MAX_FILEPATH_CAPACITY = 8192;

    /**
     * Filepaths entries count
     */
    public int count;
    /**
     * Filepaths entries
     */
    public String[] paths;

    public FilePathList() {
        count = 0;
        paths = new String[MAX_FILEPATH_CAPACITY];
    }

    @Override
    public FilePathList clone() {
        try {
            FilePathList clone = (FilePathList) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.paths = new String[paths.length];
            System.arraycopy(paths, 0, clone.paths, 0, paths.length);
            return clone;
        } catch(CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
