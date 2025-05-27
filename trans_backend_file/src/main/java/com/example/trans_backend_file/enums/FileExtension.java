package com.example.trans_backend_file.enums;

import java.util.HashMap;

public enum FileExtension {
    DOC("doc",""),
    PPT("ppt",""),
    TXT("txt","txtResolveManager");



    private final String extension;

    private final String resolver;

    FileExtension(String extension,String resolver) {
        this.resolver=resolver;
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public String getResolver() {
        return resolver;
    }

    public static FileExtension getByExtension(String extension) {
        for (FileExtension fileExtension : values()) {
            if (fileExtension.getExtension().equals(extension)) {
                return fileExtension;
            }
        }
        return null;
    }
}
