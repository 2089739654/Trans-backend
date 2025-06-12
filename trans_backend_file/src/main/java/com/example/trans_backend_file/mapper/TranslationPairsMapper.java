package com.example.trans_backend_file.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trans_backend_file.model.entity.TranslationPairs;

import java.util.List;

/**
* @author 20897
* @description 针对表【translation_pairs】的数据库操作Mapper
* @createDate 2025-05-07 08:21:46
* @Entity generator.domain.TranslationPairs
*/
public interface TranslationPairsMapper extends BaseMapper<TranslationPairs> {

    List<TranslationPairs> selectTransText(Integer position, Integer size,Long fileId);


    Long getUserId(Long fileId);

    List<TranslationPairs> selectAllById(List<Long> ids);
}




