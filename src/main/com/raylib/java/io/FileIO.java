package com.raylib.java.io;

import com.raylib.java.Raylib;
import com.raylib.java.structs.FilePathList;
import org.jetbrains.annotations.Contract;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.raylib.java.Config.SUPPORT_MODULE_RTEXT;
import static com.raylib.java.Config.SUPPORT_STANDARD_FILEIO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_INFO;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.LOG_WARNING;
import static com.raylib.java.io.FileIO.FileFilter.FILE_FILTER_TAG_ALL;

public class FileIO implements LoadFileDataCallback, SaveFileDataCallback, LoadFileTextCallback, SaveFileTextCallback {

    public enum FileFilter {
        FILE_FILTER_TAG_ALL("."),
        FILE_FILTER_TAG_FILE_ONLY("FILES*"),
        FILE_FILTER_TAG_DIR_ONLY("DIRS*");

        private final String filterTag;

        FileFilter(String filterTag) {
            this.filterTag = filterTag;
        }

        public String getFilterTag() {
            return this.filterTag;
        }
    }

    private final Raylib context;

    public String basePath;

    private LoadFileDataCallback loadFileDataCustomCallback;
    private LoadFileTextCallback loadFileTextCustomCallback;
    private SaveFileDataCallback saveFileDataCustomCallback;
    private SaveFileTextCallback saveFileTextCustomCallback;

    public FileIO(Raylib context) {
        this.context = context;
        basePath = GetWorkingDirectory();
    }

    /**
     * Get the base path for all file operations
     * @return {@code String} base path of file operations
     */
    public String GetBasePath() {
        return basePath;
    }

    public void SetBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Set custom file binary data loader.
     * WARNING: Custom implementations will always take priority over default functions. To restore default file io functionality, set the callback to {@code null}
     *
     * @param fileDataCallback Implementation of {@code LoadFileDataCallback}
     */
    public void SetLoadFileDataCallback(LoadFileDataCallback fileDataCallback) {
        this.loadFileDataCustomCallback = fileDataCallback;
    }

    /**
     * Load data from file into a buffer. <br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName Path and extension of file to read
     * @return {@code byte[]} of data loaded from file
     * @throws IOException If file fails to load form disk
     */
    @Override
    public byte[] LoadFileData(String fileName) throws IOException {
        if (loadFileDataCustomCallback != null) {
            return loadFileDataCustomCallback.LoadFileData(fileName);
        }
        else {
            byte[] fileData = null;

            if (fileName != null) {
                if (SUPPORT_STANDARD_FILEIO) {
                    Path filePath = Path.of(basePath + fileName);
                    try {
                        fileData = Files.readAllBytes(filePath);
                    }
                    catch (IOException e) {
                        context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to load file: " + filePath);
                    }
                }
                else {
                    context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
            }

            return fileData;
        }
    }

    /**
     * Unload file data allocated by LoadFileData()
     * @param data data buffer to unload
     */
    @Contract(mutates = "param")
    public void UnloadFileData(byte[] data) {
        data = new byte[0];
    }

    /**
     * Set custom file binary data saver.
     * WARNING: Custom implementations will always take priority over default functions. To restore default file io functionality, set the callback to {@code null}
     *
     * @param fileDataCallback Implementation of {@code SaveFileDataCallback}
     */
    public void SetSaveFileDataCallback(SaveFileDataCallback fileDataCallback) {
        this.saveFileDataCustomCallback = fileDataCallback;
    }

    /**
     * Save data to file from buffer.<br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName Path and extension of where the file should be created
     * @param data     Buffer of bytes to be written
     * @return {@code true} on successful file write
     * @throws IOException If file fails to write to disk
     */
    @Override
    public boolean SaveFileData(String fileName, byte[] data) throws IOException {
        if (saveFileDataCustomCallback != null) {
            return saveFileDataCustomCallback.SaveFileData(fileName, data);
        }
        else {
            boolean success = false;

            if (fileName != null) {
                if (SUPPORT_STANDARD_FILEIO) {
                    Path path = Path.of(basePath + fileName);

                    if (!path.toFile().exists()) {
                        try {
                            Files.write(path, data);
                            success = true;
                        }
                        catch (IOException exception) {
                            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                            throw exception;
                        }
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_INFO, "FILE IO: Overwriting file: " + path);
                        try {
                            Files.write(path, data);
                            success = true;
                        }
                        catch (IOException exception) {
                            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                            throw exception;
                        }
                    }
                }
                else {
                    context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
            }

            return success;
        }
    }

    /**
     * Set custom file text data loader.
     * WARNING: Custom implementations will always take priority over default functions. To restore default file io functionality, set the callback to {@code null}
     *
     * @param fileTextCallback Implementation of {@code LoadFileTextCallback}
     */
    public void SetLoadFileTextCallback(LoadFileTextCallback fileTextCallback) {
        this.loadFileTextCustomCallback = fileTextCallback;
    }

    /**
     * Load data from file as a String <br/>
     * Files are located using path relative to the application's current directory.
     *
     * @param fileName name and extension of file to be loaded
     * @return {@code String} of text loaded from file
     * @throws IOException If file fails to load form disk
     */
    @Override
    public String LoadFileText(String fileName) throws IOException {
        if (loadFileTextCustomCallback != null) {
            return loadFileTextCustomCallback.LoadFileText(fileName);
        }
        else {
            String text = "";

            if (fileName != null) {
                if (SUPPORT_STANDARD_FILEIO) {
                    Path path = Path.of(basePath + fileName);

                    try {
                        text = Files.readString(path);
                    }
                    catch (IOException exception) {
                        context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to read file: " + path);
                        throw exception;
                    }
                }
                else {
                    context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
            }
            return text;
        }
    }

    /**
     * Set custom file text data saver. </br>
     * WARNING: Custom implementations will always take priority over default functions. To restore default file io functionality, set the callback to {@code null}
     *
     * @param fileTextCallback Implementation of {@code LoadFileTextCallback}
     */
    public void SetSaveFileTextCallback(SaveFileTextCallback fileTextCallback) {
        this.saveFileTextCustomCallback = fileTextCallback;
    }

    /**
     * Save data as text to file. <br/>
     * Files are located using path relative to the application's current directory.
     * If the file exists on disk, it will be overwritten
     *
     * @param fileName Name and extension of the file to be saved.
     * @param text     String to be written to file
     * @return {@code true} on successful file write
     * @throws IOException If file fails to write to disk
     */
    @Override
    public boolean SaveFileText(String fileName, String text) throws IOException {
        if (saveFileTextCustomCallback != null) {
            return saveFileTextCustomCallback.SaveFileText(fileName, text);
        }
        else {
            boolean success = false;

            if (fileName != null) {
                if (SUPPORT_STANDARD_FILEIO) {
                    Path path = Path.of(basePath + fileName);

                    if (!path.toFile().exists()) {
                        try {
                            Files.writeString(path, text);
                            success = true;
                        }
                        catch (IOException exception) {
                            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                            throw exception;
                        }
                    }
                    else {
                        context.tracelog.TRACELOG(LOG_INFO, "FILE IO: Overwriting file: " + path);
                        try {
                            Files.writeString(path, text);
                            success = true;
                        }
                        catch (IOException exception) {
                            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to write file: " + path);
                            throw exception;
                        }
                    }
                }
                else {
                    context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Standard file io not supported, use custom file callback");
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: File name provided is not valid");
            }

            return success;
        }
    }

    /**
     * Rename file
     * @param fileName Original name of the file
     * @param fileRename Name the file should be set to
     * @return {@code true} if file operation is successful
     */
    public boolean FileRename(String fileName, String fileRename) {
        boolean result = false;

        if (FileExists(fileName)) {
            File file = new File(basePath + fileName);
            result = file.renameTo(new File(basePath + fileRename));
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file: %s", basePath + fileName);
        }

        return result;
    }

    /**
     * Delete file
     *
     * @param fileName Name of file to delete
     * @return {@code true} if file is successfully deleted
     */
    public boolean FileRemove(String fileName) {
        boolean result = false;

        if (FileExists(fileName)) {
            File file = new File(basePath + fileName);
            result = file.delete();
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file: %s", basePath + fileName);
        }

        return result;
    }

    /**
     * Copy file from one path to another
     * @param srcPath Source path of file to copy
     * @param dstPath Destination to paste source file
     * @return {@code true} if file operation is successful
     */
    public boolean FileCopy(String srcPath, String dstPath) {
        boolean result = false;

        if (FileExists(srcPath)) {
            Path source = Path.of(basePath + srcPath);
            Path target = Path.of(basePath + dstPath);

            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                result = true;
            }
            catch (IOException e) {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to copy file:\n%s", e.getMessage());
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file: %s", basePath + srcPath);
        }

        return result;
    }

    /**
     * Move file from one path to another
     *
     * @param srcPath Source path of file to move
     * @param dstPath Destination of the source file
     * @return {@code true} if file operation is successful
     */
    public boolean FileMove(String srcPath, String dstPath) {
        boolean result = false;

        if (FileExists(srcPath)) {
            Path source = Path.of(basePath + srcPath);
            Path target = Path.of(basePath + dstPath);

            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                result = true;
            }
            catch (IOException e) {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to move file:\n%s", e.getMessage());
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file: %s", basePath + srcPath);
        }

        return result;
    }

    /**
     * Replace text in an existing file
     * @param fileName File to replace text in
     * @param search String of text to replace
     * @param replacement String of text to replace {@code search}
     * @return {@code true} if file operation is successful
     */
    public boolean FileTextReplace(String fileName, String search, String replacement) {
        boolean result = false;

        if (SUPPORT_MODULE_RTEXT) {
            if (FileExists(fileName)) {
                try {
                    String fileText = LoadFileText(fileName);
                    String fileTextUpdated = context.text.TextReplace(fileText, search, replacement);
                    result = SaveFileText(fileName, fileTextUpdated);
                }
                catch (IOException e) {
                    context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to update file text:\n%s", e.getMessage());
                }
            }
            else {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file: %s", basePath + fileName);
            }
        }

        return result;
    }

    public int FileTextFindIndex(String fileName, String search) {
        int result = -1;

        if (FileExists(fileName)) {
            try {
                String fileText = LoadFileText(fileName);
                result = fileText.indexOf(search);
            }
            catch (IOException e) {
                context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file text:\n%s", e.getMessage());
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to find file: %s", basePath + fileName);
        }

        return result;
    }

    /**
     * Check if the file exists
     *
     * @param fileName
     * @return
     */
    public boolean FileExists(String fileName) {
        File file = new File(basePath + fileName);

        return file.exists();
    }

    /**
     * Check file extension <br/>
     * NOTE: Extensions checking is not case-sensitive
     *
     * @param fileName
     * @param ext      list of file extensions. Multiple extensions can be passed separated by a ";"
     * @return true if passed file name has an extension that matches
     */
    public boolean IsFileExtension(String fileName, String ext) {
        String fileExt = GetFileExtension(fileName);
        String[] extPattern = ext.split(";");
        boolean result = false;
        for (String s : extPattern) {
            result = fileExt.equalsIgnoreCase(s);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if directory exists <br/>
     * NOTE: Extensions checking is not case-sensitive
     *
     * @param directoryName
     * @return
     */
    public boolean DirectoryExists(String directoryName) {
        File tmp = new File(directoryName);
        return tmp.isDirectory();
    }

    /**
     * Get file length in byres
     *
     * @param fileName
     * @return
     */
    public int GetFileLength(String fileName) {
        File tmp = new File(fileName);
        return (int) tmp.length();
    }

    /**
     * Get file modification time (last write time)
     *
     * @param fileName
     * @return
     */
    public long GetFileModTime(String fileName) {
        long result = 0L;

        if (FileExists(fileName)) {
            File tmp = new File(fileName);
            result = tmp.lastModified();
        }

        return result;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public String GetFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    public String strptrbrk(String string, String charset) {
        int right = string.lastIndexOf(charset);
        return string.substring(right, right + charset.length());
    }

    /**
     * Get filename for a path string
     *
     * @param filePath
     * @return
     */
    public String GetFileName(String filePath) {
        filePath = filePath.replace('\\', '/');

        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf('/'));
        }
        else {
            return filePath;
        }
    }

    /**
     * Get filename string without extension (uses static string)
     *
     * @param filePath
     * @return
     */
    public String GetFileNameWithoutExt(String filePath) {

        filePath = filePath.replace('\\', '/');

        return filePath.substring(filePath.lastIndexOf('/'), filePath.lastIndexOf('.'));
    }

    /**
     * Get directory for a given filePath
     *
     * @param filePath
     * @return
     */
    public String GetDirectoryPath(String filePath) {
        String dirPath = "";

        if (filePath.contains("\\")) {
            dirPath = filePath.substring(0, filePath.lastIndexOf("\\"));
        }
        else if (filePath.contains("/")) {
            dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        }

        return dirPath;
    }

    /**
     * Get previous directory path for a given path
     *
     * @param dirPath
     * @return
     */
    public String GetPrevDirectoryPath(String dirPath) {
        String prevDirPath = "";

        if (dirPath.contains("\\")) {
            prevDirPath = dirPath.substring(0, dirPath.lastIndexOf("\\"));
        }
        else if (dirPath.contains("/")) {
            prevDirPath = dirPath.substring(0, dirPath.lastIndexOf("/"));
        }

        return prevDirPath;
    }

    /**
     * Get current working directory
     *
     * @return Location from where the application was initialised
     */
    public String GetWorkingDirectory() {
        return System.getProperty("user.dir") + "/";
    }

    /**
     * Get the location of the running .jar
     *
     * @return Location of the .jar, or {@code null} if an exception occurs.
     */
    public String GetApplicationDirectory() {
        try {
            File jarFile = new File(Raylib.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getParent() + "/";
        }
        catch (URISyntaxException e) {
            context.tracelog.TRACELOG(LOG_WARNING, "FILE IO: Failed to locate executing jar.");
            return null;
        }
    }

    /**
     * Get filenames in a directory path (max 512 files)
     *
     * @param dirPath
     * @return
     */
    public FilePathList LoadDirectoryFiles(String dirPath) {
        return LoadDirectoryFilesEx(dirPath, FILE_FILTER_TAG_ALL.filterTag, false);
    }

    /**
     *
     * @param basePath
     * @param filter
     * @param scanSubdirs
     * @return
     */
    public FilePathList LoadDirectoryFilesEx(String basePath, String filter, boolean scanSubdirs) {
        FilePathList files = new FilePathList();

        // It's a directory
        if (DirectoryExists(basePath)) {
            if (filter != null && filter.isBlank()) {
                filter = null;
            }

            // SCAN 1: Count files
            int fileCounter = GetDirectoryFileCountEx(basePath, filter, scanSubdirs);

            // Memory allocation for dirFileCount
            files.paths = new String[fileCounter];

            // SCAN 2: Read filepaths
            // WARNING: basePath is always prepended to scanned paths
            files = ScanDirectoryFiles(basePath, filter, false);

            // Security check: read files.count should match fileCounter
            if (files.count != fileCounter) {
                context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: Read files count (%u) does not match capacity allocated (%u)", files.count, fileCounter);
                files.count = fileCounter; // Avoid memory leak when unloading this FilePathList
            }
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: Directory cannot be opened (%s)", basePath);  // Maybe it's a file...
        }

        return files;
    }

    /**
     * Clear directory files paths buffers
     */
    public void UnloadDirectoryFiles(FilePathList files) {
        if (files.paths != null) {
            for (int i = 0; i < files.count; i++) {
                files.paths[i] = null;
            }
        }
    }

    // ChangeDirectory
    public boolean ChangeDirectory(String dirPath) {
        if (DirectoryExists(dirPath)) {
            this.basePath = dirPath;
            context.tracelog.TRACELOG(LOG_INFO, "SYSTEM: Working Directory: %s", dirPath);
            return true;
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "SYSTEM: Failed to change to directory: %s", dirPath);
            return false;
        }
    }

    /**
     * Check if a file has been dropped into window
     *
     * @return
     */
    public boolean IsFileDropped() {
        return (context.core.window.getDropFilesCount() > 0);
    }

    /**
     * Get dropped files names
     *
     * @return
     */
    public FilePathList LoadDroppedFiles() {
        FilePathList files = new FilePathList();

        files.count = context.core.window.getDropFilesCount();
        files.paths = context.core.window.getDropFilePaths();

        return files;
    }

    /**
     * Get number of dropped files
     *
     * @return number of registered dropped files
     */
    public int GetDroppedFilesCount() {
        return context.core.window.getDropFilesCount();
    }

    /**
     * Clear dropped file information from the system
     *
     */
    public void UnloadDroppedFiles() {
        if (context.core.window.getDropFilesCount() > 0) {
            for (int i = 0; i < context.core.window.getDropFilesCount(); i++) {
                context.core.window.getDropFilePaths()[i] = null;
            }
            context.core.window.setDropFilePaths(null);
            context.core.window.setDropFilesCount(0);
        }
    }

    /**
     * Clear dropped file information from the system
     *
     * @param files
     */
    public void UnloadDroppedFiles(FilePathList files) {
        if (context.core.window.getDropFilesCount() > 0) {
            for (int i = 0; i < context.core.window.getDropFilesCount(); i++) {
                context.core.window.getDropFilePaths()[i] = null;
            }
            context.core.window.setDropFilePaths(null);
            context.core.window.setDropFilesCount(0);
            files = null;
        }
    }

    // Get the file count in a directory
    public int GetDirectoryFileCount(String dirPath) {
        return GetDirectoryFileCountEx(dirPath, FILE_FILTER_TAG_ALL.filterTag, false);
    }

    // Get the file count in a directory with extension filtering and recursive directory scan. Use 'FILE_FILTER_TAG_DIR_ONLY' in the filter string to include directories in the result
    public int GetDirectoryFileCountEx(String basePath, String filter, boolean scanSubdirs) {
        int fileCounter = 0;

        File dir = new File(basePath);

        // Check if the path is a directory
        if (dir.isDirectory()) {
            fileCounter = dir.listFiles().length;
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: Directory cannot be opened (%s)", basePath);  // Maybe it's a file...
        }

        return fileCounter;
    }

    public FilePathList ScanDirectoryFiles(String basePath, String filter, boolean scanSubDirs) {
        FilePathList files = new FilePathList();

        File dir = new File(basePath);

        // Check if the path is a directory
        if (dir.isDirectory()) {
            files.count = dir.list().length;
            files.paths = dir.list();
        }
        else {
            context.tracelog.TRACELOG(LOG_WARNING, "FILEIO: Directory cannot be opened (%s)", basePath);  // Maybe it's a file...
        }

        return files;
    }

}
