package com.example.si_t3;

public class FileItem {
    private String name;
    private String path;
    private boolean isFolder;
    private String parentFolder;
    private long creationTimeMillis;
    private boolean isSelected;

    public FileItem(String name, String path, boolean isFolder, String parentFolder, long creationTimeMillis) {
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
        this.parentFolder = parentFolder;
        this.creationTimeMillis = creationTimeMillis;
        this.isSelected = false;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getParentFolder() {
        return parentFolder;
    }

    public long getCreationTimeMillis() {
        return creationTimeMillis;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
