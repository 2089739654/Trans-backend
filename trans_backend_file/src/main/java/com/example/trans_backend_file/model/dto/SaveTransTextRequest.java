package com.example.trans_backend_file.model.dto;

import com.example.trans_backend_file.model.entity.TranslationPairs;
import lombok.Data;

import java.util.List;

@Data
public class SaveTransTextRequest {

    private List<TranslationPairs> list;

    private Long fileId;

}
