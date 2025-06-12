package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.TranslationPairsHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 20897
* @description 针对表【translation_pairs_history】的数据库操作Service
* @createDate 2025-05-27 19:03:11
*/
public interface TranslationPairsHistoryService extends IService<TranslationPairsHistory> {

    TranslationPairsHistory getByIdAndVersion(Long id,Integer version);
}
