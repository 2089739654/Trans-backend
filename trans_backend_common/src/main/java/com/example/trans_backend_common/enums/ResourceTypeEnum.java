package com.example.trans_backend_common.enums;

public enum ResourceTypeEnum {

    FILE("file"),
    TRANSLATION_PAIRS("translationPairs"),
    PROJECT("project");


    private final String value;


    private ResourceTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }




}
