package com.example.trans_backend_common.common;

import com.example.trans_backend_common.exception.ErrorCode;
import lombok.Data;

@Data
public class BaseResponse<T> {
    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }


}
