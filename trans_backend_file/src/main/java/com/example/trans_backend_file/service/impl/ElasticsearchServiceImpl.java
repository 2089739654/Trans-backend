package com.example.trans_backend_file.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.ElasticsearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.x.protobuf.MysqlxExpr;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Resource
    RestHighLevelClient restHighLevelClient;

    private final String INDEX_NAME = "translation_pairs";


    @Override
    public void add(TranslationPairs translationPairs) {
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,Object> map = objectMapper.convertValue(translationPairs, Map.class);
        IndexRequest indexRequest=new IndexRequest(INDEX_NAME).source(map, XContentType.JSON);

        try {
            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if(response.status()!= RestStatus.CREATED&&response.status()!=RestStatus.OK){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加es失败");
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加es失败");
        }

    }
}
