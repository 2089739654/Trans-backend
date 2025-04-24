package com.example.trans_backend_file.enums;

public enum LanguageType {

    ENGLISH("en"),
    SPANISH("es"),
    FRENCH("fr"),
    GERMAN("de"),
    ITALIAN("it"),
    PORTUGUESE("pt"),
    RUSSIAN("ru"),
    JAPANESE("ja"),
    KOREAN("ko"),
    CHINESE("zh"),
    ARABIC("ar"),
    HINDI("hi");

    private final String code;

    LanguageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
