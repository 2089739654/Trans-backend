package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.TranslationPairs;

import java.util.List;

public interface ElasticsearchService {


    void add(TranslationPairs translationPairs);

    void delete(Long id);

    void update(TranslationPairs translationPairs);

    List<TranslationPairs> search(String text, Long fileId);

}
