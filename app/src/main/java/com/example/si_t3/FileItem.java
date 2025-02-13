package com.example.si_t3;

import java.util.Date;

public class FileItem {
    private String fileName;
    private String folderName;
    private Date lastModified;
    private String filePath; // Add this field

    public FileItem(String fileName, String folderName, long lastModified, String filePath) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.lastModified = new Date(lastModified);
        this.filePath = filePath; // Initialize it
    }

    public String getFileName() {
        return fileName;
    }

    public String getFolderName() {
        return folderName;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getFilePath() { // Add this getter
        return filePath;
    }
}
