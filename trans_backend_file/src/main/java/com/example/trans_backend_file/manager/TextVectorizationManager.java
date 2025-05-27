package com.example.trans_backend_file.manager;

import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.vectorization.VectorizationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Component
public class TextVectorizationManager {

    private static final String DEFAULT_VECTORIZATION_TYPE = "vectorizationByAliYun";

    @Resource
    private Map<String,VectorizationService> vectorizationServiceMap;

    public Float[] vectorization(String text,String type) {
        VectorizationService vectorizationService = vectorizationServiceMap.get(type);
        ThrowUtils.throwIf(vectorizationService==null,ErrorCode.PARAMS_ERROR,"不支持的向量化类型");
        try {
            return vectorizationService.vectorize(text);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文本向量化失败");
        }
    }

    public Float[] vectorization(String text) {
        VectorizationService vectorizationService = vectorizationServiceMap.get(DEFAULT_VECTORIZATION_TYPE);
        try {
            return vectorizationService.vectorize(text);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文本向量化失败");
        }
    }

}
