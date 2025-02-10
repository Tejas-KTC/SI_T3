package com.example.si_t3;

public class FileItem {
    private String fileName;
    private String folderName;
    private long lastModified;

    public FileItem(String fileName, String folderName, long lastModified) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.lastModified = lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFolderName() {
        return folderName;
    }

    public long getLastModified() {
        return lastModified;
    }
}
