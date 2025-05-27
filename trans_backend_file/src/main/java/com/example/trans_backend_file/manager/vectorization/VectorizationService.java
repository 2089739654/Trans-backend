package com.example.trans_backend_file.manager.vectorization;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.Filter;
import java.io.IOException;

public interface VectorizationService {
    /**
     * 向量化文本
     *
     * @param text 文本内容
     * @return 向量化结果
     * @throws IOException IO异常
     */
    Float[] vectorize(String text) throws IOException;
}
