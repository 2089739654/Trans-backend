package com.example.trans_backend_common.aop;

import com.example.trans_backend_common.enums.ResourceTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceCheck {
    String checkResource();

    ResourceTypeEnum resourceType();
}
