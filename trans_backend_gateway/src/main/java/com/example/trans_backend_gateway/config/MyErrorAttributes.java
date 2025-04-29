package com.example.trans_backend_gateway.config;

import com.example.trans_backend_common.exception.ErrorCode;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = super.getError(request);
        Map<String,Object> errorAttributes = new HashMap<>();
        errorAttributes.put("message",error.getMessage());
        errorAttributes.put("code", ErrorCode.GATEWAY_ERROR.getCode());
        errorAttributes.put("path",request.path());
        errorAttributes.put("method",request.methodName());
        return errorAttributes;
    }
}
