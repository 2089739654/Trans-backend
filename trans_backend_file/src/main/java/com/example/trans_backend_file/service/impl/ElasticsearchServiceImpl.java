package com.example.trans_backend_file.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.utils.MapUtils;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.ElasticsearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.x.protobuf.MysqlxExpr;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Resource
    RestHighLevelClient restHighLevelClient;

    private final String INDEX_NAME = "trans_pairs";


    @Override
    public void add(TranslationPairs translationPairs) {
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,Object> map = objectMapper.convertValue(translationPairs, Map.class);
        MapUtils.transfer(map);
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

    @Override
    public void delete(Long id) {
        IndexRequest indexRequest=new IndexRequest(INDEX_NAME).id(id.toString()).opType("delete");
        try {
            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if(response.status()!= RestStatus.CREATED&&response.status()!=RestStatus.OK){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除es失败");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除es失败");
        }
    }

    @Override
    public void update(TranslationPairs translationPairs) {
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,Object> map = objectMapper.convertValue(translationPairs, Map.class);
        MapUtils.transfer(map);
        IndexRequest indexRequest=new IndexRequest(INDEX_NAME).id(translationPairs.getId().toString()).source(map, XContentType.JSON);

        try {
            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if(response.status()!= RestStatus.CREATED&&response.status()!=RestStatus.OK){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新es失败");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新es失败");
        }
    }

    @Override
    public List<TranslationPairs> search(String text, Long fileId) {

        SearchRequest searchRequest = getSearchRequest(text, fileId);
        List<TranslationPairs> list=new ArrayList<>();
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.status()!= RestStatus.OK) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"es搜索失败");
            }
            SearchHit[] hits = response.getHits().getHits();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                TranslationPairs translationPairs = JSONUtil.toBean(sourceAsString, TranslationPairs.class);
                list.add(translationPairs);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"es搜索失败");
        }
        return list;
    }

    private SearchRequest getSearchRequest(String text, Long fileId) {
        SearchRequest searchRequest=new SearchRequest(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();

        MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("sourceText", text);
        matchQueryBuilder.minimumShouldMatch("80%");

        MatchQueryBuilder matchQueryBuilder1=new MatchQueryBuilder("fileId", fileId);
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.must(matchQueryBuilder1);

        MatchPhraseQueryBuilder matchPhraseQueryBuilder=new MatchPhraseQueryBuilder("sourceText", text);
        matchPhraseQueryBuilder.slop(2);
        boolQueryBuilder.should(matchPhraseQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(2);

        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }


}
