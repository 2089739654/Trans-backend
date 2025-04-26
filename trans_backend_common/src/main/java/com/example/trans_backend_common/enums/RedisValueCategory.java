package com.example.trans_backend_common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
//表示Redis设置值类别的枚举类
 public enum RedisValueCategory {
    STRING(0) ,HASH(1);
    private  int category;
}
