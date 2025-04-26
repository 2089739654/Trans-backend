package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.TranslationPairs;

public interface ElasticsearchService {


    void add(TranslationPairs translationPairs);

}
