package com.example.trans_backend_file.manager;

import com.example.trans_backend_file.manager.vectorization.VectorizationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class TextVectorizationManager {

    @Resource
    private VectorizationService vectorizationService;

    public Float[] vectorize(String text) {
        try {
            return vectorizationService.vectorize(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
