package com.example.trans_backend_file.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.trans_backend_file.model.entity.TranslationPairs;

import java.util.List;

/**
* @author 20897
* @description 针对表【translation_pairs】的数据库操作Service
* @createDate 2025-05-07 08:21:46
*/
public interface TranslationPairsService extends IService<TranslationPairs> {

    List<TranslationPairs> selectTransText(Integer position, Integer size, Integer page,Long fileId);

    void saveTransText(List<TranslationPairs> list);

    List<TranslationPairs> selectAllOrderByPosition(Long fileId);

    Long getUserId(Long id);

    List<TranslationPairs> selectAllById(List<Long> ids);

    Integer getTransTextCount(Long fileId);

}
