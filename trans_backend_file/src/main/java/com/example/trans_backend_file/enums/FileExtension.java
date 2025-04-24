package com.example.trans_backend_file.enums;

public enum FileExtension {
    PDF("pdf"),
    DOC("doc"),
    DOCX("docx"),
    XLS("xls"),
    XLSX("xlsx"),
    PPT("ppt"),
    PPTX("pptx"),
    TXT("txt"),
    CSV("csv"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    BMP("bmp"),
    MP4("mp4"),
    AVI("avi"),
    MKV("mkv"),
    MOV("mov");

    private final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
